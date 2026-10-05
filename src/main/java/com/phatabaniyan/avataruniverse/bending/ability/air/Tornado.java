package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code Tornado}: sneak-hold to raise a funnel at the
 * gaze target. It grabs everything inside, whips it around, and lifts it;
 * the caster rides the winds, steered by their look. Releasing sneak (or the
 * duration running out) ends it and starts the cooldown.
 * Reference values: Cooldown 5000ms (100 ticks), Duration 10000ms
 * (200 ticks), Height 15, Radius 5, Range 25, PlayerPush 1, NpcPush 1,
 * Suction 1, Speed 10, Particles 8.
 */
public class Tornado extends BendingAbility {
    public static final String ID = "Tornado";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.TORNADO_COOLDOWN_TICKS.get();
    /** Reference Duration 10000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.TORNADO_DURATION_TICKS.get();

    private static final double MAX_HEIGHT = Config.TORNADO_MAX_HEIGHT.get();
    private static final double RADIUS = Config.TORNADO_RADIUS.get();
    private static final double RANGE = Config.TORNADO_RANGE.get();
    private static final double PLAYER_PUSH = Config.TORNADO_PLAYER_PUSH.get();
    private static final double NPC_PUSH = Config.TORNADO_NPC_PUSH.get();
    private static final double SUCTION = Config.TORNADO_SUCTION.get();
    private static final double SPEED = Config.TORNADO_SPEED.get();
    private static final int PARTICLES = Config.TORNADO_PARTICLES.get();
    private static final double PUSH_COS = Math.cos(Math.toRadians(100));
    private static final double PUSH_SIN = Math.sin(Math.toRadians(100));

    private final ServerLevel level;
    private double currentHeight = 2.0;
    private Vec3 origin;
    private int retargetTick = 0;
    private boolean invalid = false;
    private boolean started = false;
    private final Map<Integer, Integer> angles = new HashMap<>();

    public Tornado(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        if (player.isEyeInFluid(FluidTags.WATER)) {
            this.invalid = true;
            return;
        }
        Vec3 base = findBase(player, RANGE);
        if (base == null) {
            this.invalid = true;
            return;
        }
        this.origin = base;

        int streams = Math.max(2, (int) (0.3 * MAX_HEIGHT));
        int angle = 0;
        int step = Math.max(1, (int) (MAX_HEIGHT / streams));
        for (int i = 0; i <= (int) MAX_HEIGHT; i += step) {
            this.angles.put(i, angle);
            angle += 90;
            if (angle >= 360) {
                angle = 0;
            }
        }
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    private static Vec3 findBase(ServerPlayer player, double range) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = eye(player);
        Vec3 look = player.getLookAngle().normalize();
        BlockPos solid = raycast(level, eye, look, range);
        if (solid == null) {
            return null;
        }
        return new Vec3(solid.getX() + 0.5, solid.getY() + 1.0, solid.getZ() + 0.5);
    }

    /** March along dir; returns the first solid cell within range, if any. */
    private static BlockPos raycast(ServerLevel level, Vec3 from, Vec3 dir, double range) {
        for (double d = 0; d <= range; d += 0.5) {
            Vec3 p = new Vec3(from.x + dir.x * d, from.y + dir.y * d, from.z + dir.z * d);
            BlockPos bp = BlockPos.containing(p);
            if (!level.isLoaded(bp)) {
                return null;
            }
            if (level.getBlockState(bp).isSolidRender(level, bp)) {
                return bp.immutable();
            }
        }
        return null;
    }

    @Override
    public boolean progress() {
        if (invalid || !started) {
            return false;
        }
        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(owner);
        if (sp == null || sp.hasDisconnected() || !sp.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (sp.isEyeInFluid(FluidTags.WATER) || !sp.isShiftKeyDown()) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        if (DURATION_TICKS > 0 && level.getGameTime() - this.startTime > DURATION_TICKS) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        if (++this.retargetTick >= 5) {
            this.retargetTick = 0;
            Vec3 next = findBase(sp, RANGE);
            if (next != null) {
                this.origin = next;
            }
        }
        rotateTornado(sp);
        return true;
    }

    private void rotateTornado(ServerPlayer sp) {
        double timefactor = Math.min(1.0, this.currentHeight / MAX_HEIGHT);
        double currentRadius = Math.max(0.5, timefactor * RADIUS);

        for (Entity e : level.getEntities(
                (Entity) null,
                new AABB(
                        this.origin.x - currentRadius * 2,
                        this.origin.y,
                        this.origin.z - currentRadius * 2,
                        this.origin.x + currentRadius * 2,
                        this.origin.y + this.currentHeight,
                        this.origin.z + currentRadius * 2))) {
            double y = e.getY();
            if (y < this.origin.y || y > this.origin.y + this.currentHeight) {
                continue;
            }
            double factor = (y - this.origin.y) / Math.max(0.5, this.currentHeight);
            double dx = e.getX() - this.origin.x;
            double dz = e.getZ() - this.origin.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            double wall = Math.max(0.8, currentRadius * Math.max(0.15, factor));
            boolean isUser = e.getUUID().equals(sp.getUUID());
            if (dist > wall) {
                // Outer band: suction drags stragglers into the funnel (not the caster).
                double grab = Math.max(0.8, currentRadius * 2.0);
                if (!isUser && dist <= grab) {
                    double mag = Math.max(1.0e-4, dist);
                    Vec3 pull = new Vec3(-dx / mag, 0.15, -dz / mag).scale(SUCTION);
                    e.setDeltaMovement(pull.x, e.getDeltaMovement().y * 0.9 + pull.y, pull.z);
                    e.hurtMarked = true;
                    e.resetFallDistance();
                }
                continue;
            }
            double mag = Math.max(1.0e-4, dist);
            double vx = (dx * PUSH_COS - dz * PUSH_SIN) / mag;
            double vz = (dx * PUSH_SIN + dz * PUSH_COS) / mag;
            double vy;
            if (isUser) {
                Vec3 look = sp.getLookAngle().normalize();
                vx = look.x;
                vz = look.z;
                double dy = sp.getY() - this.origin.y;
                if (dy >= this.currentHeight * 0.95) {
                    vy = 0;
                } else if (dy >= this.currentHeight * 0.85) {
                    vy = 6.0 * (0.95 - dy / this.currentHeight);
                } else {
                    vy = 0.6;
                }
            } else if (e instanceof Player) {
                vy = 0.05 * PLAYER_PUSH;
            } else {
                vy = 0.7 * NPC_PUSH;
            }
            // The swirl is set outright (scaled by growth): that is the revolution.
            e.setDeltaMovement(vx * timefactor, vy, vz * timefactor);
            e.hurtMarked = true;
            e.resetFallDistance();
        }

        for (var ring : this.angles.entrySet()) {
            int i = ring.getKey();
            double angle = Math.toRadians(ring.getValue());
            double y = this.origin.y + timefactor * i;
            double factor = i / Math.max(0.5, this.currentHeight);
            double x = this.origin.x + timefactor * factor * currentRadius * Math.cos(angle);
            double z = this.origin.z + timefactor * factor * currentRadius * Math.sin(angle);
            level.sendParticles(
                    BendingTheme.particle(Config.TORNADO_FUNNEL_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    x,
                    y,
                    z,
                    Config.TORNADO_FUNNEL_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.02);
            if (ThreadLocalRandom.current().nextInt(20) == 0) {
                level.playSound(null, x, y, z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.3F, 1.2F);
            }
            ring.setValue(ring.getValue() + (int) (25 * SPEED / 10.0));
        }
        if (this.currentHeight < MAX_HEIGHT) {
            this.currentHeight += 1.0;
        }
    }

    @Override
    public void onRemove() {
        if (!started) {
            return;
        }
        started = false;
        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(owner);
        if (sp != null) {
            sp.resetFallDistance();
        }
    }

    private static Vec3 eye(ServerPlayer player) {
        return new Vec3(player.getX(), player.getEyeY(), player.getZ());
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
