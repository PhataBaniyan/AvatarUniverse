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
 * Port of Hyperion {@code IceCrawl}: click to ready a crawl from ice (else
 * water), click again to loose it. A shard skims the ground toward your gaze
 * -- or locked onto a caught target -- freezing the water it crosses and
 * rooting the first thing it touches with ice at its feet. Armor-stand shard
 * visuals are snow instead.
 */
public class IceCrawl extends BendingAbility {
    public static final String ID = "IceCrawl";

    private final ServerLevel level;
    private BlockPos sourceBlock;
    private Vec3 location;
    private Vec3 endLocation;
    private Vec3 direction;
    private LivingEntity target;
    private boolean launched;
    private boolean locked;

    public IceCrawl(ServerPlayer player, BlockPos source) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.sourceBlock = source.immutable();
        this.location = Vec3.atCenterOf(source);
    }

    public boolean isLaunched() {
        return launched;
    }

    /** Second click (Korra shootLine): lock a victim or fire down the gaze. */
    public void shootLine(ServerPlayer player) {
        if (launched) {
            return;
        }
        double range = Config.ICECRAWL_RANGE.get();
        LivingEntity caught = nearestInCone(player, range + Config.ICECRAWL_SELECT_RANGE.get());
        if (caught != null && caught.position().distanceToSqr(location) <= range * range) {
            locked = true;
            target = caught;
            endLocation = target.position();
        } else {
            endLocation = WaterSpoutWave.gazeTarget(player, range);
        }
        location = Vec3.atCenterOf(sourceBlock).add(0.0, 1.25, 0.0);
        direction = flatDirection(Vec3.atCenterOf(sourceBlock), endLocation);
        launched = true;
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.ICECRAWL_COOLDOWN_TICKS.get());
        }
        Vec3 c = Vec3.atCenterOf(sourceBlock);
        level.sendParticles(
                BendingTheme.particle(Config.ICECRAWL_SOURCE_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                c.x,
                c.y + 1.0,
                c.z,
                Config.ICECRAWL_SOURCE_PARTICLE_COUNT.get(),
                0.4,
                0.4,
                0.4,
                0.05);
    }

    private static Vec3 flatDirection(Vec3 from, Vec3 to) {
        Vec3 flat = new Vec3(to.x - from.x, 0.0, to.z - from.z);
        if (flat.lengthSqr() < 1e-6) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        return flat.normalize();
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
        if (!launched) {
            if (player.blockPosition().distSqr(sourceBlock) > Math.pow(Config.ICECRAWL_SELECT_RANGE.get() + 5.0, 2.0)) {
                return false;
            }
            if (!BendingSources.isWaterSource(level, sourceBlock)) {
                return false;
            }
            if (level.getGameTime() % 4 == 0) {
                Vec3 c = Vec3.atCenterOf(sourceBlock);
                level.sendParticles(
                        BendingTheme.particle(Config.ICECRAWL_SETUP_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                        c.x,
                        c.y + 0.5,
                        c.z,
                        Config.ICECRAWL_SETUP_PARTICLE_COUNT.get(),
                        0.2,
                        0.2,
                        0.2,
                        0.02);
            }
            return true;
        }
        if (locked) {
            if (target == null || target.isRemoved() || !target.isAlive()) {
                locked = false;
            } else if (target.position().distanceToSqr(endLocation) < 25.0) {
                endLocation = target.position();
                direction = flatDirection(Vec3.atCenterOf(sourceBlock), endLocation);
            } else {
                locked = false;
            }
        }
        location = location.add(direction.scale(0.7));
        BlockPos base = BlockPos.containing(location).below();
        if (!isCrawlable(base)) {
            BlockPos up = base.above();
            BlockPos down = base.below();
            if (isCrawlable(up) && level.getBlockState(up.above()).isAir()) {
                location = location.add(0.0, 1.0, 0.0);
                base = up;
            } else if (isCrawlable(down)) {
                location = location.add(0.0, -1.0, 0.0);
                base = down;
            } else {
                return false;
            }
        }
        if (location.distanceToSqr(Vec3.atCenterOf(sourceBlock))
                > Config.ICECRAWL_RANGE.get() * Config.ICECRAWL_RANGE.get()) {
            return false;
        }
        if (level.getFluidState(base).is(net.minecraft.world.level.material.Fluids.WATER)
                && !TempBlock.isTemp(level, base)) {
            BendingManager.scheduleRevert(
                    new TempBlock(level, base.immutable(), Blocks.ICE.defaultBlockState(), TempBlock.QUIET),
                    level.getGameTime() + Config.ICECRAWL_ICE_TICKS.get());
        }
        level.sendParticles(
                BendingTheme.particle(Config.ICECRAWL_TRAIL_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                location.x,
                location.y,
                location.z,
                Config.ICECRAWL_TRAIL_PARTICLE_COUNT.get(),
                0.2,
                0.2,
                0.2,
                0.02);
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        location.x - 0.8,
                        location.y - 0.8,
                        location.z - 0.8,
                        location.x + 0.8,
                        location.y + 0.8,
                        location.z + 0.8),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            entity.hurt(
                    level.damageSources().magic(), Config.ICECRAWL_DAMAGE.get().floatValue());
            entity.addEffect(
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Config.ICECRAWL_FREEZE_TICKS.get(), 5));
            BlockPos feet = entity.blockPosition().below();
            if (BendingSources.isTransparentForBend(level, feet) && !TempBlock.isTemp(level, feet)) {
                BendingManager.scheduleRevert(
                        new TempBlock(level, feet.immutable(), Blocks.PACKED_ICE.defaultBlockState(), TempBlock.QUIET),
                        level.getGameTime() + Config.ICECRAWL_FREEZE_TICKS.get());
            }
            return false;
        }
        return true;
    }

    private boolean isCrawlable(BlockPos base) {
        if (!level.getBlockState(base.above()).isAir()) {
            return false;
        }
        var state = level.getBlockState(base);
        return !level.getFluidState(base).isEmpty()
                || BendingSources.isIce(level, base)
                || (!state.isAir() && state.isSolid());
    }

    @Override
    public void onRemove() {}
}
