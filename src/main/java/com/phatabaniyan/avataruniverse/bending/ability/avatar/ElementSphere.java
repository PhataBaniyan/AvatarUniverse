package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of ProjectAvatar {@code avatar.ElementSphere}: sneak to summon an
 * orbiting four-element shell and fly along the look direction while
 * sneaking near the ground. Clicks on hotbar slots 1-5 fire the
 * Air/Earth/Fire/Water/Stream sub-attacks via {@link #fireSub}; sneaking
 * twice within 12 ticks dismisses the shell. Ends on duration expiry or
 * when every element's uses run out.
 *
 * <p>Reference values: Cooldown 12000ms (240 ticks, set on start), Duration
 * 60000ms (1200 ticks), MaxHeight 6, FlySpeed 0.7, AirUses 5, FireUses 5,
 * WaterUses 5, EarthUses 3.</p>
 */
public class ElementSphere extends BendingAbility {
    public static final String ID = "ElementSphere";

    /** Reference Cooldown 12000ms, in server ticks. Set on start. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ELEMENTSPHERE_COOLDOWN_MS.get());
    /** Reference Duration 60000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.ELEMENTSPHERE_DURATION_MS.get());

    private static final double MAX_HEIGHT = Config.ELEMENTSPHERE_MAX_HEIGHT.get();
    private static final double FLY_SPEED = Config.ELEMENTSPHERE_FLY_SPEED.get();
    /** Double-sneak dismiss window (reference 600ms). */
    private static final long DISMISS_WINDOW_TICKS = Config.msToTicks(Config.ELEMENTSPHERE_DISMISS_WINDOW_MS.get());

    private static final DustParticleOptions WATER_DUST =
            new DustParticleOptions(new Vector3f(0.35F, 0.85F, 1.0F), 1.2F);
    private static final net.minecraft.core.particles.BlockParticleOption EARTH_BLOCK =
            new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());

    private final ServerLevel level;

    private int airUses = Config.ELEMENTSPHERE_AIR_USES.get();
    private int fireUses = Config.ELEMENTSPHERE_FIRE_USES.get();
    private int waterUses = Config.ELEMENTSPHERE_WATER_USES.get();
    private int earthUses = Config.ELEMENTSPHERE_EARTH_USES.get();
    private boolean prevMayfly = false;
    private boolean prevFlying = false;
    private double yaw = 0;
    private int point = 0;
    private boolean started = false;
    private int tick = 0;
    private int groundTick = -10;
    private boolean groundCache = false;
    private boolean wasSneaking = false;
    private long lastRisingTick = -1;

    public ElementSphere(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        if (!gate(player.getUUID())) {
            return;
        }

        ElementSphere old = BendingManager.find(player.getUUID(), ElementSphere.class);
        if (old != null) {
            BendingManager.remove(old);
            return;
        }

        this.prevMayfly = player.getAbilities().mayfly;
        this.prevFlying = player.getAbilities().flying;
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
        this.wasSneaking = player.isShiftKeyDown();
        this.started = true;
        player.getInventory().selected = 0;
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }

    public void fireSub(int slot, ServerPlayer player) {
        SphereAttack atk =
                switch (slot) {
                    case 1 -> new ESAir(player);
                    case 2 -> new ESEarth(player);
                    case 3 -> new ESFire(player);
                    case 4 -> new ESWater(player);
                    case 5 -> new ESStream(player);
                    default -> null;
                };
        if (atk != null && atk.isStarted()) {
            BendingManager.start(atk);
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
        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(owner);
        if (sp == null || !sp.isAlive() || sp.hasDisconnected()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        long now = level.getGameTime();
        if (now > startTime + DURATION_TICKS) {
            return false;
        }
        boolean sneaking = sp.isShiftKeyDown();
        if (sneaking && !wasSneaking) {
            if (lastRisingTick >= 0 && now - lastRisingTick <= DISMISS_WINDOW_TICKS) {
                return false;
            }
            lastRisingTick = now;
        }
        wasSneaking = sneaking;
        if (this.airUses == 0 && this.fireUses == 0 && this.waterUses == 0 && this.earthUses == 0) {
            return false;
        }
        sp.resetFallDistance();

        if (sp.getInventory().selected > 4) {
            sp.getInventory().selected = 4;
        }

        this.tick++;
        if (this.tick - this.groundTick >= 5) {
            this.groundTick = this.tick;
            this.groundCache = withinHeight(sp);
        }

        if (sp.isCrouching() && this.groundCache) {
            Vec3 look = sp.getLookAngle().normalize().scale(FLY_SPEED);
            sp.setDeltaMovement(look);
            sp.hurtMarked = true;
        }

        Vec3 center = sp.position();
        for (Entity entity :
                level.getEntities(sp, new AABB(center, center).inflate(Config.ELEMENTSPHERE_PUSH_RADIUS.get()))) {
            if (entity.getUUID().equals(sp.getUUID()) || !(entity instanceof LivingEntity)) {
                continue;
            }
            if (entity instanceof ArmorStand) {
                continue;
            }
            Vec3 push = entity.position().subtract(center);
            entity.setDeltaMovement(push);
            entity.hurtMarked = true;
        }

        playParticles(sp);
        return true;
    }

    private boolean withinHeight(ServerPlayer sp) {
        BlockPos feet = sp.blockPosition();
        int limit = (int) MAX_HEIGHT + 5;
        for (int i = 0; i <= limit; i++) {
            BlockState s = level.getBlockState(feet.below(i));
            if (!s.isAir()) {
                return (sp.getY() - (feet.getY() - i)) <= MAX_HEIGHT;
            }
        }
        return false;
    }

    private void playParticles(ServerPlayer sp) {
        Vec3 base = sp.position();
        double baseY = base.y + 1.0;
        double baseX = base.x;
        double baseZ = base.z;

        this.yaw += 40;
        if (this.yaw >= 360) {
            this.yaw -= 360;
        }
        // Stagger ring sets across alternating ticks: each ring still renders at 10 Hz,
        // but per-tick packet count is roughly halved.
        boolean even = (this.tick & 1) == 0;
        if (even && this.airUses != 0) {
            for (double j = -180; j <= 180; j += 45) {
                double rad = Math.toRadians(j);
                double lx = baseX + 3 * Math.cos(rad) * Math.cos(Math.toRadians(this.yaw));
                double lz = baseZ + 3 * Math.cos(rad) * Math.sin(Math.toRadians(this.yaw));
                double ly = baseY + 3 * Math.sin(rad);
                level.sendParticles(
                        BendingTheme.particle(Config.ELEMENTSPHERE_AIR_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        lx,
                        ly,
                        lz,
                        Config.ELEMENTSPHERE_AIR_PARTICLE_COUNT.get(),
                        0.1,
                        0.1,
                        0.1,
                        0.01);
            }
        }

        this.point++;
        if (even && this.fireUses != 0) {
            for (int i = -180; i < 180; i += 40) {
                double angle = i * Math.PI / 180 + this.point;
                level.sendParticles(
                        BendingTheme.particle(Config.ELEMENTSPHERE_FIRE_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        baseX + 3 * Math.cos(angle),
                        baseY,
                        baseZ + 3 * Math.sin(angle),
                        Config.ELEMENTSPHERE_FIRE_PARTICLE_COUNT.get(),
                        0.05,
                        0.05,
                        0.05,
                        0.01);
            }
        }

        this.point++;
        if (this.point >= 360) {
            this.point = 0;
        }
        if (!even && (this.waterUses != 0 || this.earthUses != 0)) {
            double tilt = Math.PI / 3 * 2.1 / 2;
            double yawRad = Math.toRadians(sp.getYRot());
            for (int i = -180; i < 180; i += 30) {
                double a = i * Math.PI / 180 + this.point;
                Vec3 v = new Vec3(Math.cos(a) * 3, Math.sin(a) * 3, 0);
                v = rotX(v, tilt);
                v = rotY(v, -(yawRad - 1.575));
                Vec3 v1 = new Vec3(Math.cos(a) * 3, Math.sin(a) * 3, 0);
                v1 = rotX(v1, -tilt);
                v1 = rotY(v1, -(yawRad - 1.575));
                if (this.waterUses != 0) {
                    level.sendParticles(WATER_DUST, baseX + v.x, baseY + v.y, baseZ + v.z, 2, 0.1, 0.1, 0.1, 0.01);
                    level.sendParticles(
                            BendingTheme.particle(Config.ELEMENTSPHERE_BUBBLE_PARTICLE.get(), ParticleTypes.BUBBLE),
                            baseX + v.x,
                            baseY + v.y,
                            baseZ + v.z,
                            Config.ELEMENTSPHERE_BUBBLE_PARTICLE_COUNT.get(),
                            0.15,
                            0.15,
                            0.15,
                            0.02);
                }
                if (this.earthUses != 0) {
                    level.sendParticles(
                            EARTH_BLOCK, baseX + v1.x, baseY + v1.y, baseZ + v1.z, 2, 0.15, 0.15, 0.15, 0.02);
                }
            }
        }

        if (level.random.nextInt(40) == 0) {
            level.playSound(
                    null, base.x, base.y, base.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.35F, 1.1F);
        }
    }

    private static Vec3 rotX(Vec3 v, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        return new Vec3(v.x, v.y * cos - v.z * sin, v.y * sin + v.z * cos);
    }

    private static Vec3 rotY(Vec3 v, double angle) {
        double cos = Math.cos(angle), sin = Math.sin(angle);
        return new Vec3(v.x * cos + v.z * sin, v.y, -v.x * sin + v.z * cos);
    }

    @Override
    public void onRemove() {
        if (!started) {
            return;
        }
        started = false;
        ServerPlayer sp = level.getServer().getPlayerList().getPlayer(owner);
        if (sp != null) {
            sp.getAbilities().mayfly = prevMayfly;
            sp.getAbilities().flying = prevFlying;
            sp.onUpdateAbilities();
            sp.resetFallDistance();
        }
    }

    public int getAirUses() {
        return airUses;
    }

    public void setAirUses(int uses) {
        this.airUses = Math.max(0, uses);
    }

    public int getEarthUses() {
        return earthUses;
    }

    public void setEarthUses(int uses) {
        this.earthUses = Math.max(0, uses);
    }

    public int getFireUses() {
        return fireUses;
    }

    public void setFireUses(int uses) {
        this.fireUses = Math.max(0, uses);
    }

    public int getWaterUses() {
        return waterUses;
    }

    public void setWaterUses(int uses) {
        this.waterUses = Math.max(0, uses);
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AVATAR) && bending.isToggled();
    }
}
