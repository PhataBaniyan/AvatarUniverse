package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code IceSpikeBlast}: sneak facing water or ice to
 * ready a spike, click to throw it (rising two blocks, then flying at the
 * target's feet or the gaze point, clamped to range). A re-click mid-flight
 * re-aims it; hits deal damage plus slowness; walls shatter it. Single water
 * sources are consumed on throw. Gaze deflects and caster bending-slows are
 * cut with the reference's cross-ability systems.
 */
public class IceSpikeBlast extends BendingAbility {
    public static final String ID = "IceSpikeBlast";
    /** Shared player-facing bind (Korra getName "IceSpike" for all three). */
    public static final String BIND = "IceSpike";

    private final ServerLevel level;
    private BlockPos sourceBlock;
    private Vec3 location;
    private Vec3 firstDestination;
    private Vec3 destination;
    private boolean prepared;
    private boolean settingUp;
    private boolean progressing;

    public IceSpikeBlast(ServerPlayer player, BlockPos source) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.sourceBlock = source.immutable();
        this.location = Vec3.atCenterOf(source);
        this.prepared = true;
    }

    /** Throw the readied spike (Korra throwIce). */
    public void throwIce(ServerPlayer player) {
        if (!prepared) {
            return;
        }
        double range = Config.ICESPIKE_BLAST_RANGE.get();
        LivingEntity caught = nearestInCone(player, range);
        Vec3 dest;
        if (caught != null) {
            dest = caught.position();
        } else {
            dest = WaterSpoutWave.gazeTarget(player, range);
        }
        if (dest.distanceToSqr(location) < 1.0) {
            return;
        }
        firstDestination = location;
        if (dest.y - location.y > 2.0) {
            firstDestination = new Vec3(firstDestination.x, dest.y - 1.0, firstDestination.z);
        } else {
            firstDestination = firstDestination.add(0.0, 2.0, 0.0);
        }
        Vec3 dir = dest.subtract(firstDestination);
        double len = dir.length();
        destination = len <= range ? dest : firstDestination.add(dir.normalize().scale(range));
        progressing = true;
        settingUp = true;
        prepared = false;
        if (BendingSources.isPlant(level, sourceBlock) || BendingSources.isSnow(level, sourceBlock)) {
            BendingManager.consumePlantSource(
                    level, sourceBlock, Config.msToTicks(Config.WATERMANIP_PLANT_REGROW_MS.get()));
        } else if (level.getFluidState(sourceBlock).is(net.minecraft.world.level.material.Fluids.WATER)
                && adjacentWaterCount(sourceBlock) < 3) {
            level.setBlock(sourceBlock, Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
        }
    }

    private int adjacentWaterCount(BlockPos pos) {
        int count = 0;
        if (level.getFluidState(pos.above()).is(net.minecraft.world.level.material.Fluids.WATER)) {
            count++;
        }
        if (level.getFluidState(pos.below()).is(net.minecraft.world.level.material.Fluids.WATER)) {
            count++;
        }
        if (level.getFluidState(pos.north()).is(net.minecraft.world.level.material.Fluids.WATER)) {
            count++;
        }
        if (level.getFluidState(pos.south()).is(net.minecraft.world.level.material.Fluids.WATER)) {
            count++;
        }
        if (level.getFluidState(pos.east()).is(net.minecraft.world.level.material.Fluids.WATER)) {
            count++;
        }
        if (level.getFluidState(pos.west()).is(net.minecraft.world.level.material.Fluids.WATER)) {
            count++;
        }
        return count;
    }

    /** Re-aim a flying spike at the current gaze (Korra redirect, self only). */
    public void redirect(ServerPlayer player) {
        if (!progressing) {
            return;
        }
        double range = Config.ICESPIKE_BLAST_RANGE.get();
        LivingEntity caught = nearestInCone(player, range);
        Vec3 dest;
        if (caught != null) {
            dest = caught.position();
        } else {
            dest = WaterSpoutWave.gazeTarget(player, range);
        }
        Vec3 dir = dest.subtract(location);
        double len = dir.length();
        destination = len <= range * 2.0 ? dest : location.add(dir.normalize().scale(range * 2.0));
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

    public boolean isPrepared() {
        return prepared;
    }

    public boolean isProgressing() {
        return progressing;
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
        double range = Config.ICESPIKE_BLAST_RANGE.get();
        if (player.getEyePosition().distanceToSqr(location) >= range * range) {
            return false;
        }
        if (prepared) {
            if (!BIND.equalsIgnoreCase(bending.boundAbility(player.getInventory().selected + 1))) {
                return false;
            }
            if (!BendingSources.isWaterSource(level, sourceBlock)) {
                return false;
            }
            if (level.getGameTime() % 4 == 0) {
                Vec3 c = Vec3.atCenterOf(sourceBlock);
                level.sendParticles(
                        BendingTheme.particle(Config.ICESPIKEBLAST_SETUP_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                        c.x,
                        c.y + 0.5,
                        c.z,
                        Config.ICESPIKEBLAST_SETUP_PARTICLE_COUNT.get(),
                        0.2,
                        0.2,
                        0.2,
                        0.02);
            }
            return true;
        }
        if (BlockPos.containing(location).getY()
                == BlockPos.containing(firstDestination).getY()) {
            settingUp = false;
        }
        if (location.distanceToSqr(destination) <= 4.0) {
            return false;
        }
        Vec3 target = settingUp ? firstDestination : destination;
        Vec3 step = target.subtract(location).normalize();
        location = location.add(step);
        BlockPos cell = BlockPos.containing(location);
        if (cell.equals(sourceBlock)) {
            return true;
        }
        var state = level.getBlockState(cell);
        if (!BendingSources.isTransparentForBend(level, cell)
                && state.getFluidState().isEmpty()) {
            level.sendParticles(
                    BendingTheme.particle(Config.ICESPIKEBLAST_SHATTER_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                    location.x,
                    location.y,
                    location.z,
                    Config.ICESPIKEBLAST_SHATTER_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.05);
            return false;
        }
        // Short-lived trail (Korra 130ms revert): every step melts behind the head on its own.
        BendingManager.scheduleRevert(
                new TempBlock(level, cell.immutable(), Blocks.ICE.defaultBlockState(), TempBlock.QUIET),
                level.getGameTime() + 3L);
        double hitR = Config.ICESPIKE_BLAST_COLLISION_RADIUS.get() + 0.5;
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        location.x - hitR,
                        location.y - hitR,
                        location.z - hitR,
                        location.x + hitR,
                        location.y + hitR,
                        location.z + hitR),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            entity.hurt(
                    level.damageSources().magic(),
                    Config.ICESPIKE_BLAST_DAMAGE.get().floatValue());
            entity.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    Config.msToTicks(Config.ICESPIKE_BLAST_SLOW_DURATION_MS.get()),
                    Config.ICESPIKE_BLAST_SLOW_POTENCY.get()));
            return false;
        }
        sourceBlock = cell.immutable();
        location = location.add(step);
        return true;
    }

    @Override
    public void onRemove() {
        // Trail cells melt on their own timers; nothing held.
    }
}
