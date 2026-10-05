package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code IceBlast}: sneak facing water or ice to ready a
 * blast, click to throw it. A packed-ice block hops toward the target (aimed
 * at a caught entity's eyes, else the gaze point), rising two blocks first,
 * dealing damage plus slowness on contact and shattering on walls. Re-aiming
 * mid-flight, gaze deflects and caster bending-slows are cut with the rest of
 * the reference's cross-ability systems.
 */
public class IceBlast extends BendingAbility {
    public static final String ID = "IceBlast";

    private final ServerLevel level;
    private BlockPos sourceBlock;
    private Vec3 location;
    private Vec3 firstDestination;
    private Vec3 destination;
    private boolean prepared;
    private boolean settingUp;
    private boolean progressing;
    private TempBlock travelling;

    public IceBlast(ServerPlayer player, BlockPos source) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.sourceBlock = source.immutable();
        this.location = Vec3.atCenterOf(source);
        this.prepared = true;
    }

    /** Eye-ray source: water or ice within range (snow only when allowed). */
    public static BlockPos findSource(ServerPlayer player, double range, boolean allowSnow) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!level.getFluidState(pos).isEmpty()
                    && level.getFluidState(pos).is(net.minecraft.world.level.material.Fluids.WATER)) {
                return pos.immutable();
            }
            if (BendingSources.isIce(level, pos) && (allowSnow || !BendingSources.isSnow(level, pos))) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** Throw the readied blast (Korra throwIce). */
    public void throwIce(ServerPlayer player) {
        if (!prepared) {
            return;
        }
        double range = Config.ICEBLAST_RANGE.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        LivingEntity caught = nearestInCone(player, eye, look, range);
        Vec3 dest;
        if (caught != null) {
            dest = caught.getEyePosition();
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
    }

    private static LivingEntity nearestInCone(ServerPlayer player, Vec3 eye, Vec3 look, double range) {
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
        double range = Config.ICEBLAST_RANGE.get();
        if (player.getEyePosition().distanceToSqr(location) >= range * range) {
            if (progressing) {
                shatter();
            }
            return false;
        }
        if (prepared) {
            if (!ID.equalsIgnoreCase(bending.boundAbility(player.getInventory().selected + 1))) {
                return false;
            }
            if (!BendingSources.isWaterSource(level, sourceBlock)) {
                return false;
            }
            if (level.getGameTime() % 4 == 0) {
                Vec3 c = Vec3.atCenterOf(sourceBlock);
                level.sendParticles(
                        BendingTheme.particle(Config.ICEBLAST_SETUP_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                        c.x,
                        c.y + 0.5,
                        c.z,
                        Config.ICEBLAST_SETUP_PARTICLE_COUNT.get(),
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
        if (travelling != null) {
            travelling.revert();
            travelling = null;
        }
        var state = level.getBlockState(cell);
        if (!BendingSources.isTransparentForBend(level, cell)
                && state.getFluidState().isEmpty()) {
            shatter();
            return false;
        }
        travelling = new TempBlock(level, cell.immutable(), Blocks.PACKED_ICE.defaultBlockState(), TempBlock.QUIET);
        level.sendParticles(
                BendingTheme.particle(Config.ICEBLAST_TRAIL_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                location.x,
                location.y,
                location.z,
                Config.ICEBLAST_TRAIL_PARTICLE_COUNT.get(),
                0.2,
                0.2,
                0.2,
                0.02);
        double hitR = Config.ICEBLAST_COLLISION_RADIUS.get();
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
                    level.damageSources().magic(), Config.ICEBLAST_DAMAGE.get().floatValue());
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 2));
            return false;
        }
        sourceBlock = cell.immutable();
        location = location.add(step);
        return true;
    }

    private void shatter() {
        level.sendParticles(
                BendingTheme.particle(Config.ICEBLAST_SHATTER_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                location.x,
                location.y,
                location.z,
                Config.ICEBLAST_SHATTER_PARTICLE_COUNT.get(),
                0.4,
                0.4,
                0.4,
                0.1);
        level.playSound(null, BlockPos.containing(location), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.7F, 1.3F);
    }

    @Override
    public void onRemove() {
        if (travelling != null) {
            travelling.revert();
            travelling = null;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.ICEBLAST_COOLDOWN_TICKS.get());
        }
    }
}
