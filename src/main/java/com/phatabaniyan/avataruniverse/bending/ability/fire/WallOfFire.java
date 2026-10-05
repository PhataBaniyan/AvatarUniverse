package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra's {@code WallOfFire}: click to raise a burning curtain
 * at the cursor — a pure particle wall (no world edits) that sears anything
 * standing in it on an interval, with real flames along the wall base that
 * revert away. Reference values: Cooldown 6000ms, Duration 8000ms, Range 10,
 * Width 6, Height 6, Damage 3, DamageInterval 1000ms, FireTicks 3,
 * FxInterval 100ms, foot-fire revert 3000ms.
 */
public class WallOfFire extends BendingAbility {
    public static final String ID = "WallOfFire";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.WALLOFFIRE_COOLDOWN_MS.get());
    /** Reference Duration 8000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.WALLOFFIRE_DURATION_MS.get());
    /** Reference DamageInterval 1000ms, in server ticks. */
    private static final long DAMAGE_INTERVAL_TICKS = Config.msToTicks(Config.WALLOFFIRE_DAMAGE_INTERVAL_MS.get());
    /** Reference FxInterval 100ms, in server ticks. */
    private static final long FX_INTERVAL_TICKS = Config.msToTicks(Config.WALLOFFIRE_FX_INTERVAL_MS.get());
    /** Foot-fire revert 3000ms, in server ticks. */
    private static final long FOOT_REVERT_TICKS = Config.msToTicks(Config.WALLOFFIRE_FOOT_REVERT_MS.get());

    private static final double RANGE = Config.WALLOFFIRE_RANGE.get();
    private static final double AIM_RANGE = Config.WALLOFFIRE_AIM_RANGE.get();
    private static final double WIDTH = Config.WALLOFFIRE_WIDTH.get();
    private static final double HEIGHT = Config.WALLOFFIRE_HEIGHT.get();
    private static final float DAMAGE = Config.WALLOFFIRE_DAMAGE.get().floatValue();
    private static final int FIRE_TICKS = Config.WALLOFFIRE_FIRE_MS.get() / 1000;

    private final ServerPlayer player;
    private final ServerLevel level;
    private long lastFx;
    private long lastDamage;
    private final Vec3 origin;
    private final Vec3 side;
    private final Vec3 up;
    private final Vec3 normal;

    public WallOfFire(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();

        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 base = aimBase(this.level, eye, look, Math.min(RANGE, AIM_RANGE));
        this.origin = base;
        Vec3 s = new Vec3(-look.z, 0, look.x);
        if (s.lengthSqr() < 1.0e-4) {
            s = new Vec3(1, 0, 0);
        }
        this.side = s.normalize();
        // Wall lies in the plane square to the gaze: look up and it lies flat.
        this.up = this.side.cross(look).normalize();
        this.normal = look;
        cool(player.getUUID(), this.level, ID, COOLDOWN_TICKS);
        level.playSound(null, base.x, base.y, base.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.7F, 0.9F);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        long now = level.getGameTime();
        if (now - this.startTime > DURATION_TICKS) {
            return false;
        }
        if (now - this.lastFx >= FX_INTERVAL_TICKS) {
            this.lastFx = now;
            for (double i = -WIDTH; i <= WIDTH; i += 1.0) {
                for (double j = 0; j <= HEIGHT; j += 1.0) {
                    Vec3 p = new Vec3(
                            this.origin.x + this.side.x * i + this.up.x * j,
                            this.origin.y + this.side.y * i + this.up.y * j,
                            this.origin.z + this.side.z * i + this.up.z * j);
                    level.sendParticles(
                            BendingTheme.particle(Config.WALLOFFIRE_WALL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                            p.x,
                            p.y,
                            p.z,
                            Config.WALLOFFIRE_WALL_PARTICLE_COUNT.get(),
                            0.3,
                            0.3,
                            0.3,
                            0.04);
                }
            }
            // PK-style footing: real flames along the wall base, reverting away.
            for (double i = -WIDTH; i <= WIDTH; i += 2.0) {
                BlockPos fp = BlockPos.containing(
                                this.origin.x + this.side.x * i,
                                this.origin.y + this.side.y * i,
                                this.origin.z + this.side.z * i)
                        .immutable();
                if (!level.isLoaded(fp)
                        || !level.getBlockState(fp).isAir()
                        || !level.getBlockState(fp.below()).isSolidRender(level, fp.below())
                        || TempBlock.isTemp(level, fp)) {
                    continue;
                }
                TempBlock temp = new TempBlock(level, fp, Blocks.FIRE.defaultBlockState(), TempBlock.QUIET);
                BendingManager.scheduleRevert(temp, now + FOOT_REVERT_TICKS);
            }
            if (player.getRandom().nextInt(7) == 0) {
                level.playSound(
                        null,
                        this.origin.x,
                        this.origin.y,
                        this.origin.z,
                        SoundEvents.FIRE_AMBIENT,
                        SoundSource.PLAYERS,
                        0.4F,
                        1.0F);
            }
        }
        if (now - this.lastDamage >= DAMAGE_INTERVAL_TICKS) {
            this.lastDamage = now;
            for (Entity e : level.getEntities(
                    player,
                    new AABB(
                            this.origin.x - WIDTH - 2,
                            this.origin.y - 2,
                            this.origin.z - WIDTH - 2,
                            this.origin.x + WIDTH + 2,
                            this.origin.y + HEIGHT + 2,
                            this.origin.z + WIDTH + 2))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                Vec3 o = new Vec3(e.getX() - this.origin.x, e.getY() - this.origin.y, e.getZ() - this.origin.z);
                double along = o.dot(this.side);
                double up = o.dot(this.up);
                double out = Math.abs(o.dot(this.normal));
                if (Math.abs(along) > WIDTH + 1
                        || up < -1
                        || up > HEIGHT + 1
                        || out > Config.WALLOFFIRE_THICKNESS.get()) {
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.hurt(player.damageSources().magic(), DAMAGE);
                    living.igniteForSeconds(FIRE_TICKS);
                } else {
                    e.igniteForSeconds(FIRE_TICKS);
                }
            }
        }
        return true;
    }

    /**
     * Gaze aim point: eye-ray march stopping at the first solid block (base
     * sits one block above its center), else the ray endpoint — mirrors the
     * source AirTargeting raycast capped at 6 blocks.
     */
    private static Vec3 aimBase(ServerLevel level, Vec3 eye, Vec3 look, double range) {
        Vec3 last = eye.add(look.scale(range));
        for (double d = 0.5; d <= range; d += 0.5) {
            Vec3 at = eye.add(look.scale(d));
            BlockPos pos = BlockPos.containing(at);
            if (!level.isLoaded(pos)) {
                break;
            }
            if (level.getBlockState(pos).isSolidRender(level, pos)) {
                return new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            }
            last = at;
        }
        return last;
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
