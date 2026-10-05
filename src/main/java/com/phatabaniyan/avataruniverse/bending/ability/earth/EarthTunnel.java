package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code EarthTunnel} (hold sneak, bore an expanding
 * spiral shaft along the gaze; release or look away to stop). Reference
 * values: Interval 30ms, BlocksPerInterval 1, MaxRadius 1, Range 10, Radius
 * 0.25, Revert true (RevertCheckTime 300000ms = 6000 ticks), Cooldown 0,
 * DropLootIfNotRevert false. Cleared cells are temp-air with the same 6000
 * tick revert; plant/snow cover is cleared one cell up (two for tall grass).
 */
public class EarthTunnel extends EarthAbility {
    public static final String ID = "EarthTunnel";

    /** Reference Interval 30ms, converted against the 50ms server tick. */
    private static final long INTERVAL_TICKS = Config.msToTicks(Config.EARTHTUNNEL_INTERVAL_MS.get());

    private static final int BLOCKS_PER_INTERVAL = Config.EARTHTUNNEL_BLOCKS_PER_INTERVAL.get();
    private static final double MAX_RADIUS = Config.EARTHTUNNEL_MAX_RADIUS.get();
    private static final double RANGE = Config.EARTHTUNNEL_RANGE.get();
    private static final double START_RADIUS = Config.EARTHTUNNEL_START_RADIUS.get();
    /** Reference RevertCheckTime 300000ms, in server ticks. */
    private static final long REVERT_TICKS = Config.msToTicks(Config.EARTHTUNNEL_REVERT_MS.get());
    /** Reference Cooldown 0. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.EARTHTUNNEL_COOLDOWN_MS.get());

    private static final double MAX_GAZE_DEVIATION_DEGREES = Config.EARTHTUNNEL_MAX_DEVIATION_DEG.get();

    private final Vec3 originEye;
    private final BlockPos originBlock;
    private final Vec3 direction;
    private double depth;
    private double radius;
    private double angle;
    private BlockPos block;
    private long lastRunGameTime;

    public EarthTunnel(ServerPlayer player) {
        super(player);
        this.originEye = player.getEyePosition();
        this.originBlock = targetedBlock(RANGE);
        this.block = originBlock;
        this.direction = player.getLookAngle().normalize();
        this.depth = Math.max(0, originEye.distanceTo(Vec3.atCenterOf(originBlock)) - 1);
        this.angle = 0;
        this.radius = START_RADIUS;
        this.lastRunGameTime = player.level().getGameTime();
    }

    /**
     * Sneak-start gate: the aimed cell must exist and be earthbendable or
     * transparent (air), matching the constructor's bend check.
     */
    public static boolean canBegin(ServerPlayer player) {
        EarthTunnel probe = new EarthTunnel(player);
        BlockPos target = probe.originBlock;
        if (target == null) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        return Accretion.isEarthbendable(level, target)
                || !level.getBlockState(target).isSolid();
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (player == null || player.isRemoved() || player.isDeadOrDying() || player.level() != level) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.hasElement(BendingElement.EARTH) || !bending.isToggled()) {
            return false;
        }
        long now = level.getGameTime();
        if (now - lastRunGameTime < INTERVAL_TICKS) {
            return true;
        }
        lastRunGameTime = now;

        for (int i = 1; i <= BLOCKS_PER_INTERVAL; i++) {
            if (gazeDeviationDegrees() > MAX_GAZE_DEVIATION_DEGREES || !player.isShiftKeyDown()) {
                return false;
            }
            while ((!isEarth(block) && !isSand(block)) || shouldIgnoreBlock(block)) {
                if (!isTransparent(block) && !shouldIgnoreBlock(block)) {
                    return false;
                }
                if (angle >= 360) {
                    angle = 0;
                    if (radius >= MAX_RADIUS) {
                        radius = START_RADIUS;
                        if (depth >= RANGE) {
                            bending.setCooldown(ID, now + COOLDOWN_TICKS);
                            return false;
                        }
                        depth += 0.5;
                    } else {
                        radius += START_RADIUS;
                    }
                } else {
                    angle += 20;
                }
                Vec3 offset = orthogonal(direction, angle, radius);
                Vec3 cursor = originEye.add(direction.scale(depth)).add(offset);
                block = BlockPos.containing(cursor).immutable();
            }

            clearWithCover(block);
        }
        return true;
    }

    /** Angle between the current gaze and the locked boring direction. */
    private double gazeDeviationDegrees() {
        Vec3 look = player.getLookAngle().normalize();
        double cos = look.dot(direction) / Math.max(look.length() * direction.length(), 1.0e-9);
        cos = Math.max(-1.0, Math.min(1.0, cos));
        return Math.toDegrees(Math.acos(cos));
    }

    /** Exact port of {@code GeneralMethods.getOrthogonalVector}. */
    private static Vec3 orthogonal(Vec3 axis, double degrees, double length) {
        Vec3 ortho = new Vec3(axis.y, -axis.x, 0).normalize().scale(length);
        return rotateAround(axis, ortho, degrees);
    }

    /** Exact port of {@code GeneralMethods.rotateVectorAroundVector}. */
    private static Vec3 rotateAround(Vec3 axis, Vec3 rotator, double degrees) {
        double angle = Math.toRadians(degrees);
        Vec3 rotation = axis.normalize();
        Vec3 third = rotation.cross(rotator).normalize().scale(rotator.length());
        return rotator.scale(Math.cos(angle)).add(third.scale(Math.sin(angle)));
    }

    private boolean isEarth(BlockPos pos) {
        return Accretion.isEarthbendable(level, pos);
    }

    private boolean isSand(BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.SAND);
    }

    /** Reference IgnoredBlocks default: all ore tags plus ancient debris, gilded blackstone, nether quartz ore. */
    private boolean shouldIgnoreBlock(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(Blocks.ANCIENT_DEBRIS)
                || state.is(Blocks.GILDED_BLACKSTONE)
                || state.is(Blocks.NETHER_QUARTZ_ORE);
    }

    private boolean isPlant(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.VINE)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.LARGE_FERN);
    }

    private boolean isSnow(BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.SNOW);
    }

    private void clearWithCover(BlockPos pos) {
        if (!TempBlock.isTemp(level, pos)) {
            TempBlock temp = new TempBlock(level, pos, Blocks.AIR.defaultBlockState());
            BendingManager.scheduleRevert(temp, level.getGameTime() + REVERT_TICKS);
        }
        BlockPos above = pos.above();
        if (isPlant(above) || isSnow(above)) {
            if (!TempBlock.isTemp(level, above)) {
                TempBlock temp = new TempBlock(level, above, Blocks.AIR.defaultBlockState());
                BendingManager.scheduleRevert(temp, level.getGameTime() + REVERT_TICKS);
            }
            BlockPos above2 = above.above();
            if (isPlant(above2) && level.getBlockState(above2).is(Blocks.TALL_GRASS)) {
                if (!TempBlock.isTemp(level, above2)) {
                    TempBlock temp = new TempBlock(level, above2, Blocks.AIR.defaultBlockState());
                    BendingManager.scheduleRevert(temp, level.getGameTime() + REVERT_TICKS);
                }
            }
        }
    }

    /** First non-air block along the gaze, like {@code getTargetBlock(null, range)}. */
    private BlockPos targetedBlock(double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!level.getBlockState(pos).isAir()) {
                return pos.immutable();
            }
        }
        return BlockPos.containing(eye.add(look.scale(range))).immutable();
    }

    @Override
    public void onRemove() {}
}
