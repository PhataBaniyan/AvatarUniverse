package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code Explode}: hold sneak to paint the target with
 * a primed hiss, let go for a clean radial detonation. Entity-only: the
 * world keeps its blocks (no {@code level.explode}, matching the source).
 * Releasing with no painted target fizzles with no cooldown. Reference
 * values: Cooldown 5000ms (100 ticks), Damage 6, Radius 5, Knockback 2,
 * Range 20.
 */
public class Explode extends BendingAbility {
    public static final String ID = "Explode";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.EXPLODE_COOLDOWN_TICKS.get();

    private static final double DAMAGE = Config.EXPLODE_DAMAGE.get();
    private static final double RADIUS = Config.EXPLODE_RADIUS.get();
    private static final double KNOCKBACK = Config.EXPLODE_KNOCKBACK.get();
    private static final double RANGE = Config.EXPLODE_RANGE.get();
    private static final int IGNITE_SECONDS = Config.EXPLODE_IGNITE_SECONDS.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private Vec3 target;

    public Explode(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
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
        if (player.isShiftKeyDown()) {
            Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
            Vec3 look = player.getLookAngle().normalize();
            this.target = null;
            for (double d = 0; d <= RANGE; d += 0.5) {
                Vec3 p = new Vec3(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
                BlockPos bp = BlockPos.containing(p);
                if (!level.isLoaded(bp)) {
                    break;
                }
                if (level.getBlockState(bp).isSolidRender(level, bp)) {
                    this.target = p;
                    break;
                }
                this.target = p;
            }
            if (this.target != null) {
                level.sendParticles(
                        BendingTheme.particle(Config.EXPLODE_TARGET_PARTICLE.get(), ParticleTypes.CRIT),
                        this.target.x,
                        this.target.y,
                        this.target.z,
                        Config.EXPLODE_TARGET_PARTICLE_COUNT.get(),
                        0.3,
                        0.3,
                        0.3,
                        0.03);
                if (player.getRandom().nextInt(6) == 0) {
                    level.playSound(
                            null,
                            this.target.x,
                            this.target.y,
                            this.target.z,
                            SoundEvents.FIRECHARGE_USE,
                            SoundSource.PLAYERS,
                            0.2F,
                            2.0F);
                }
            }
            return true;
        }
        if (this.target == null) {
            return false;
        }
        Vec3 at = this.target;
        for (Entity e : level.getEntities(player, new AABB(at, at).inflate(RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            Vec3 push = e.position().add(0, 1, 0).subtract(at);
            if (push.lengthSqr() < 1.0e-4) {
                push = new Vec3(0, 1, 0);
            }
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), (float) DAMAGE);
                living.igniteForSeconds(IGNITE_SECONDS);
            }
            e.setDeltaMovement(push.normalize().scale(KNOCKBACK));
            e.hurtMarked = true;
        }
        level.sendParticles(
                BendingTheme.particle(Config.EXPLODE_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                at.x,
                at.y,
                at.z,
                Config.EXPLODE_FLAME_PARTICLE_COUNT.get(),
                0.7,
                0.7,
                0.7,
                0.08);
        level.sendParticles(
                BendingTheme.particle(Config.EXPLODE_BLAST_PARTICLE.get(), ParticleTypes.EXPLOSION),
                at.x,
                at.y,
                at.z,
                Config.EXPLODE_BLAST_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.1);
        level.sendParticles(
                BendingTheme.particle(Config.EXPLODE_HIT_PARTICLE.get(), ParticleTypes.CRIT),
                at.x,
                at.y,
                at.z,
                Config.EXPLODE_HIT_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.06);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.9F, 1.3F);
        cool(owner, level, ID, COOLDOWN_TICKS);
        return false;
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
