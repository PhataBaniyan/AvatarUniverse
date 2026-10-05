package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirFlight} (ProjectKorra Soar mode plus its
 * slot-modes), adapted to hotbar selection: slot 1 Soar (click again to
 * cycle speed), slot 2 Glide, slot 3 Levitate, slot 4 Ending. Clicking the
 * bound slot toggles the flight.
 * Reference values: Cooldown 5000ms (100 ticks), Speed 1. Soar speeds
 * SLOW 0.6 / NORMAL 1.0 / FAST 1.6.
 */
public class AirFlight extends BendingAbility {
    public static final String ID = "AirFlight";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.msToTicks(Config.AIRFLIGHT_COOLDOWN_MS.get());

    private static final double SPEED = Config.AIRFLIGHT_SPEED.get();
    private static final double SOAR_SLOW = Config.AIRFLIGHT_SOAR_SLOW.get();
    private static final double SOAR_NORMAL = Config.AIRFLIGHT_SOAR_NORMAL.get();
    private static final double SOAR_FAST = Config.AIRFLIGHT_SOAR_FAST.get();
    private static final double[] SOAR_SPEEDS = {SOAR_SLOW, SOAR_NORMAL, SOAR_FAST};
    private static final double GLIDE_BOOST = Config.AIRFLIGHT_GLIDE_BOOST.get();
    private static final double RAM_RADIUS = Config.AIRFLIGHT_RAM_RADIUS.get();
    private static final double RAM_THRESHOLD = Config.AIRFLIGHT_RAM_THRESHOLD.get();

    private enum Mode {
        SOAR,
        GLIDE,
        LEVITATE
    }

    /** Mode names that temporarily own slots 1-4 while flight runs. */
    public static final String[] SUB_BINDS = {"Soar", "Glide", "Levitate", "Ending"};

    private final ServerLevel level;
    private boolean prevMayfly = false;
    private boolean prevFlying = false;
    private boolean prevGliding = false;
    private boolean started = false;
    private int tick = 0;
    private int speedIdx = 1;
    private int lastSlot = -1;
    private Mode mode = Mode.SOAR;
    private final Set<UUID> rammed = new HashSet<>();
    private final Map<Integer, String> savedSlots = new HashMap<>();

    /** Mode index (1-4) for a live sub-bind name, else -1. */
    public static int subIndex(String name) {
        if (name == null) {
            return -1;
        }
        for (int i = 0; i < SUB_BINDS.length; i++) {
            if (SUB_BINDS[i].equalsIgnoreCase(name)) {
                return i + 1;
            }
        }
        return -1;
    }

    /** True while {@code name} is a flight mode sub-bind. */
    public static boolean isSubBind(String name) {
        return subIndex(name) > 0;
    }

    public AirFlight(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        AirFlight old = BendingManager.find(player.getUUID(), AirFlight.class);
        if (old != null) {
            BendingManager.remove(old);
            return;
        }
        if (player.onGround()) {
            return;
        }
        if (player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }

        this.prevMayfly = player.getAbilities().mayfly;
        this.prevFlying = player.getAbilities().flying;
        this.prevGliding = player.isFallFlying();
        this.lastSlot = 0;
        applyModeFlags(player);
        this.started = true;
        // Park the four mode sub-binds on slots 1-4 and snap the caster to
        // slot 1, the first mode; restore the player's hotbar on remove.
        // Selection MUST move on the server now, before the first progress
        // tick, or the watcher reads the old slot and ends the flight.
        player.getInventory().selected = 0;
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            for (int slot = 1; slot <= 4; slot++) {
                savedSlots.put(slot, bending.boundAbility(slot));
                bending.bind(slot, SUB_BINDS[slot - 1]);
            }
            com.phatabaniyan.avataruniverse.bending.BendingBoardSync.queueForceSlot(owner, 0);
        }
    }

    /** Left-click while flying in Soar mode: cycle SLOW to NORMAL to FAST. */
    public void cycleSpeed() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (this.mode != Mode.SOAR || !this.started) {
            if (player != null) {
                player.displayClientMessage(
                        com.phatabaniyan.avataruniverse.bending.BendingTheme.gradient(
                                "Return to Soar to tune speed.",
                                com.phatabaniyan.avataruniverse.bending.BendingElement.AIR),
                        true);
            }
            return;
        }
        this.speedIdx = (this.speedIdx + 1) % SOAR_SPEEDS.length;
        if (player != null) {
            player.displayClientMessage(
                    com.phatabaniyan.avataruniverse.bending.BendingTheme.gradient(
                            "Soar speed: " + speedName(), com.phatabaniyan.avataruniverse.bending.BendingElement.AIR),
                    true);
        }
    }

    /** Current mode name for the flight sub-board (Soar/Glide/Levitate). */
    public String modeName() {
        return switch (mode) {
            case SOAR -> "Soar";
            case GLIDE -> "Glide";
            case LEVITATE -> "Levitate";
        };
    }

    /** Soar speed step name for the flight sub-board. */
    public String speedName() {
        return switch (speedIdx) {
            case 0 -> "SLOW";
            case 2 -> "FAST";
            default -> "NORMAL";
        };
    }

    /** Hotbar slot changes drive modes: 1 Soar, 2 Glide, 3 Levitate, 4 Ending. */
    public void selectMode(int slot) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || slot < 1 || slot > 4) {
            return;
        }
        if (slot == 4) {
            BendingManager.remove(this);
            return;
        }
        Mode next = Mode.values()[slot - 1];
        if (next == this.mode) {
            applyModeFlags(player);
            return;
        }
        Mode prev = this.mode;
        this.mode = next;
        applyModeFlags(player);
        player.displayClientMessage(
                com.phatabaniyan.avataruniverse.bending.BendingTheme.gradient(
                        modeName() + (next == Mode.SOAR ? " — " + speedName() : ""),
                        com.phatabaniyan.avataruniverse.bending.BendingElement.AIR),
                true);
        if (next == Mode.GLIDE && prev != Mode.SOAR) {
            // Slow entries (levitate/hover) get the 1.2 rescue; Soar hands
            // over its exact final velocity untouched.
            if (player.getDeltaMovement().length() < 1.0) {
                Vec3 boost = player.getLookAngle().normalize().scale(GLIDE_BOOST);
                player.setDeltaMovement(boost);
                player.hurtMarked = true;
            }
        }
    }

    private void applyModeFlags(ServerPlayer player) {
        if (this.mode == Mode.LEVITATE) {
            // Plain creative flight, no pose, no push.
            if (player.isFallFlying()) {
                player.stopFallFlying();
            }
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        } else {
            // Soar and Glide share the jets' proven pattern: mayfly on,
            // creative-hover off, elytra pose on. Soar adds thrust.
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            if (!player.onGround()) {
                player.startFallFlying();
            }
        }
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!started) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (player.isEyeInFluid(FluidTags.WATER) || player.onGround()) {
            return false;
        }
        this.tick++;
        if (this.tick % 20 == 0) {
            this.rammed.clear();
        }

        // Scrolling the hotbar steers the modes; no click needed. Slots
        // 1-4 carry the four mode sub-binds while flying, so the index
        // itself is the mode.
        int cur = player.getInventory().selected;
        if (cur != this.lastSlot) {
            this.lastSlot = cur;
            selectMode(cur + 1);
        }

        if (this.mode == Mode.GLIDE) {
            // Hands off: vanilla elytra glide with feet-level streams like Soar.
            if (!player.isFallFlying() && !player.onGround()) {
                player.startFallFlying();
            }
            soarTrail(player);
            player.resetFallDistance();
            return true;
        }
        if (this.mode == Mode.LEVITATE) {
            // Hands off: pure creative flight, any direction, no particles.
            player.resetFallDistance();
            return true;
        }
        Vec3 v;
        if (player.isCrouching()) {
            v = player.getDeltaMovement().scale(0.8);
        } else {
            v = player.getLookAngle().normalize().scale(SPEED * SOAR_SPEEDS[this.speedIdx]);
        }
        if (!player.isFallFlying()) {
            player.startFallFlying();
        }
        player.setDeltaMovement(v);
        player.hurtMarked = true;
        player.resetFallDistance();
        soarTrail(player);

        double ramSpeed = v.length();
        if (ramSpeed > RAM_THRESHOLD) {
            for (Entity e :
                    level.getEntities(player, new AABB(player.position(), player.position()).inflate(RAM_RADIUS))) {
                if (!(e instanceof LivingEntity living) || e instanceof ArmorStand || !this.rammed.add(e.getUUID())) {
                    continue;
                }
                living.hurt(player.damageSources().magic(), (float) (ramSpeed / 2));
                e.setDeltaMovement(v.scale(2.0 / 3.0));
                e.hurtMarked = true;
            }
        }
        return true;
    }

    private void soarTrail(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(0.25, 0, 0);
        } else {
            side = side.normalize().scale(0.25);
        }
        // AirJet-style twin streams at the feet, thickening with speed.
        double baseY = player.getY() + 0.1;
        int count = Config.AIRFLIGHT_TRAIL_PARTICLE_COUNT.get() + this.speedIdx * 2;
        level.sendParticles(
                BendingTheme.particle(Config.AIRFLIGHT_TRAIL_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                player.getX() + side.x,
                baseY,
                player.getZ() + side.z,
                count,
                0.12,
                0.12,
                0.12,
                0.01);
        level.sendParticles(
                BendingTheme.particle(Config.AIRFLIGHT_TRAIL_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                player.getX() - side.x,
                baseY,
                player.getZ() - side.z,
                count,
                0.12,
                0.12,
                0.12,
                0.01);
    }

    @Override
    public void onRemove() {
        if (!started) {
            return;
        }
        started = false;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
            player.getAbilities().mayfly = prevMayfly;
            player.getAbilities().flying = prevFlying;
            player.onUpdateAbilities();
            if (player.isFallFlying() && !prevGliding) {
                player.stopFallFlying();
            }
            player.resetFallDistance();
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            for (int slot = 1; slot <= 4; slot++) {
                bending.bind(slot, savedSlots.get(slot));
            }
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }
}
