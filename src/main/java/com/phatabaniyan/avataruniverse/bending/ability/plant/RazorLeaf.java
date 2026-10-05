package com.phatabaniyan.avataruniverse.bending.ability.plant;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterSpoutWave;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code RazorLeaf}: sneak at plants to spin up a
 * shredding leaf disc, hold sneak to steer it, release to let it fly at
 * whatever you face. Re-sneak mid-flight to drag it back for another pass
 * (limited recalls); it dies on range, walls, or first blood.
 */
public class RazorLeaf extends BendingAbility {
    public static final String ID = "RazorLeaf";

    private static final net.minecraft.core.particles.BlockParticleOption LEAF =
            new net.minecraft.core.particles.BlockParticleOption(
                    net.minecraft.core.particles.ParticleTypes.BLOCK,
                    net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState());

    private final ServerLevel level;
    private Vec3 center;
    private Vec3 direction;
    private TempBlock sourceTemp;
    private int uses;
    private boolean counted = true;

    public RazorLeaf(ServerPlayer player, BlockPos plant, boolean sourced) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        if (sourced && plant != null) {
            this.sourceTemp = new TempBlock(level, plant.immutable(), Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
            this.center = Vec3.atCenterOf(plant);
        } else {
            this.center = player.getEyePosition()
                    .add(player.getLookAngle().normalize().scale(1.5));
        }
        this.direction = player.getLookAngle().normalize();
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.isRemoved() || player.isDeadOrDying()) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null || !bending.hasElement(BendingElement.WATER) || !bending.isToggled()) {
            return false;
        }
        double range = Config.RAZORLEAF_RANGE.get();
        double radius = Config.RAZORLEAF_RADIUS.get();
        if (center.distanceToSqr(player.getEyePosition()) >= range * range) {
            return false;
        }
        if (player.isShiftKeyDown() && uses < Config.RAZORLEAF_MAX_RECALLS.get()) {
            counted = true;
            Vec3 aim = player.getEyePosition()
                    .add(player.getLookAngle().normalize().scale(radius + 0.5));
            direction = aim.subtract(center).normalize();
        } else {
            if (counted) {
                counted = false;
                uses++;
            }
            LivingEntity caught = nearestInCone(player, range);
            Vec3 dest;
            if (caught != null) {
                dest = caught.position().add(0.0, 1.0, 0.0);
            } else {
                dest = WaterSpoutWave.gazeTarget(player, range)
                        .add(player.getLookAngle().normalize());
            }
            direction = dest.subtract(center).normalize();
        }
        if (direction.lengthSqr() > 1.0) {
            center = center.add(direction.normalize().scale(0.75));
        } else {
            center = center.add(direction);
        }
        BlockPos cell = BlockPos.containing(center);
        if (!BendingSources.isTransparentForBend(level, cell)) {
            return false;
        }
        int particles = Config.RAZORLEAF_PARTICLES.get();
        for (int n = 0; n < particles; n++) {
            double r = 0.075 * Math.sqrt(n);
            if (r > radius) {
                break;
            }
            double phi = Math.toRadians(n * 137.5);
            level.sendParticles(
                    LEAF, center.x + r * Math.cos(phi), center.y, center.z + r * Math.sin(phi), 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        center.x - radius - 1.0,
                        center.y - radius - 1.0,
                        center.z - radius - 1.0,
                        center.x + radius + 1.0,
                        center.y + radius + 1.0,
                        center.z + radius + 1.0),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            entity.hurt(
                    level.damageSources().magic(), Config.RAZORLEAF_DAMAGE.get().floatValue());
            return false;
        }
        return true;
    }

    private static LivingEntity nearestInCone(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double cosLimit = Math.cos(Math.toRadians(25.0));
        LivingEntity best = null;
        double bestDistSqr = Double.MAX_VALUE;
        for (LivingEntity entity : player.serverLevel()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(eye.subtract(range, range, range), eye.add(range, range, range)),
                        e -> !e.getUUID().equals(player.getUUID()) && e.isAlive())) {
            Vec3 to =
                    entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0).subtract(eye);
            double distSqr = to.lengthSqr();
            if (distSqr > range * range || distSqr >= bestDistSqr) {
                continue;
            }
            if (to.normalize().dot(look) < cosLimit) {
                continue;
            }
            best = entity;
            bestDistSqr = distSqr;
        }
        return best;
    }

    @Override
    public void onRemove() {
        if (sourceTemp != null) {
            if (level.getBlockState(sourceTemp.pos()).isAir()) {
                sourceTemp.revert();
            }
            sourceTemp = null;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.RAZORLEAF_COOLDOWN_TICKS.get());
        }
    }
}
