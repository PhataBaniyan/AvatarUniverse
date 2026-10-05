package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of Hyperion {@code EarthGlove}: click to hurl a stone glove that
 * boomerangs. Touching a mob hits for 1 (or grabs it back to you if
 * you're sneaking); past range it returns, and catching it drops any
 * passenger gently. Shapeless flight, ground touch or a 45-degree knock
 * shatters it into debris. One glove at a time. Reference values:
 * Cooldown 5000ms, Damage 1, Range 24, fly 1.2, carry 0.6.
 */
public class EarthGlove extends EarthAbility {
    public static final String ID = "EarthGlove";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.EARTHGLOVE_COOLDOWN_TICKS.get();

    private static final float DAMAGE = Config.EARTHGLOVE_DAMAGE.get().floatValue();
    private static final double RANGE = Config.EARTHGLOVE_RANGE.get();
    private static final double FLY_SPEED = Config.EARTHGLOVE_FLY_SPEED.get();
    private static final double CARRY_SPEED = Config.EARTHGLOVE_CARRY_SPEED.get();
    private static final double CATCH_RADIUS = Config.EARTHGLOVE_CATCH_RADIUS.get();

    private UUID gloveId;
    private Vec3 lastVelocity = new Vec3(0, 0, 1);
    private UUID grabbedId;
    private boolean returning;

    public EarthGlove(ServerPlayer player) {
        super(player);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-6) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        double handed = (player.getInventory().selected % 2 == 0) ? 0.5 : -0.5;
        Vec3 spawn = eye.add(side.scale(handed)).add(0, -0.8, 0);
        LivingEntity victim = eyeVictim(RANGE, 2.5);
        Vec3 dest = victim != null ? victim.position().add(0, 1, 0) : eyeTargetPoint();
        Vec3 velocity = dest.subtract(spawn).normalize().scale(FLY_SPEED);
        ItemEntity glove = new ItemEntity(level, spawn.x, spawn.y, spawn.z, new ItemStack(Items.STONE, 1));
        glove.setDefaultPickUpDelay();
        glove.setNoGravity(true);
        glove.setDeltaMovement(velocity);
        level.addFreshEntity(glove);
        gloveId = glove.getUUID();
        lastVelocity = velocity;
        cool(owner, player, ID, COOLDOWN_TICKS);
    }

    @Override
    public String name() {
        return ID;
    }

    private Vec3 eyeTargetPoint() {
        BlockPos hit = eyeTarget(RANGE);
        if (hit != null) {
            return Vec3.atCenterOf(hit);
        }
        Vec3 eye = player.getEyePosition();
        return eye.add(player.getLookAngle().normalize().scale(RANGE));
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!(level.getEntity(gloveId) instanceof ItemEntity glove) || !glove.isAlive()) {
            return false;
        }
        if (!glove.level().equals(level)
                || glove.position().distanceToSqr(player.position()) > (RANGE + 5) * (RANGE + 5)) {
            return false;
        }
        if (glove.position().distanceToSqr(player.position()) > RANGE * RANGE) {
            returning = true;
        }
        Vec3 recorded = lastVelocity;
        if (returning) {
            if (!player.isShiftKeyDown()) {
                shatter(glove.position());
                return false;
            }
            Vec3 home = player.getEyePosition()
                    .add(player.getLookAngle().normalize().scale(1.5));
            if (glove.position().distanceToSqr(home) < CATCH_RADIUS * CATCH_RADIUS) {
                if (grabbedId != null && level.getEntity(grabbedId) instanceof LivingEntity victim) {
                    victim.setDeltaMovement(Vec3.ZERO);
                    victim.hurtMarked = true;
                }
                grabbedId = null;
                return false;
            }
            if (grabbedId != null) {
                if (!(level.getEntity(grabbedId) instanceof LivingEntity victim) || !victim.isAlive()) {
                    shatter(glove.position());
                    return false;
                }
                victim.setDeltaMovement(
                        home.subtract(victim.position()).normalize().scale(CARRY_SPEED));
                victim.hurtMarked = true;
                glove.setPos(victim.getX(), victim.getY() + 1.0, victim.getZ());
                return true;
            }
            Vec3 back = home.subtract(glove.position()).normalize().scale(FLY_SPEED);
            glove.setDeltaMovement(back);
            lastVelocity = back;
            return true;
        }
        glove.setDeltaMovement(recorded);
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        glove.position().subtract(0.8, 0.8, 0.8),
                        glove.position().add(0.8, 0.8, 0.8)),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            if (player.isShiftKeyDown()) {
                returning = true;
                grabbedId = entity.getUUID();
                glove.setPos(entity.getX(), entity.getY() + 1.0, entity.getZ());
                return true;
            }
            entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
            entity.invulnerableTime = 0;
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hurtMarked = true;
            shatter(glove.position());
            return false;
        }
        if (grabbedId != null) {
            grabbedId = null;
        }
        Vec3 flight = glove.getDeltaMovement();
        if (glove.onGround() || angleBetween(recorded, flight) > Math.PI / 4 || flight.length() < FLY_SPEED - 0.2) {
            shatter(glove.position());
            return false;
        }
        lastVelocity = flight;
        return true;
    }

    private static double angleBetween(Vec3 a, Vec3 b) {
        if (a.lengthSqr() < 1.0e-6 || b.lengthSqr() < 1.0e-6) {
            return 0.0;
        }
        double cos = a.dot(b) / (a.length() * b.length());
        return Math.acos(Math.max(-1.0, Math.min(1.0, cos)));
    }

    private void shatter(Vec3 at) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                at.x,
                at.y,
                at.z,
                3,
                0.0,
                0.0,
                0.0,
                0.0);
        level.sendParticles(
                BendingTheme.particle(Config.EARTHGLOVE_SHATTER_PARTICLE.get(), ParticleTypes.POOF),
                at.x,
                at.y,
                at.z,
                Config.EARTHGLOVE_SHATTER_PARTICLE_COUNT.get(),
                0,
                0,
                0,
                0.02);
    }

    @Override
    public void onRemove() {
        if (level.getEntity(gloveId) instanceof ItemEntity glove) {
            glove.discard();
        }
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
