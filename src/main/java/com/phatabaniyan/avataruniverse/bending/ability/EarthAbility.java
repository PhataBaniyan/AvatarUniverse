package com.phatabaniyan.avataruniverse.bending.ability;

import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Shared earthbending plumbing: block movement with per-ability original
 * tracking and revert, used by Dig, EarthTunnel and EarthSurf.
 */
public abstract class EarthAbility extends BendingAbility {
    protected int noiseReduction = 0;
    protected final ServerPlayer player;
    protected final ServerLevel level;
    /**
     * Tag stamped on every transient projectile falling block. The guard
     * discards these on landing so shots never grief the terrain.
     */
    public static final String SHOT_TAG = "avataruniverse_shot";
    /**
     * Natural block at every touched cell, in the order it was first moved.
     * Reverting in reverse order restores chains even when several earth
     * movers touch the same ground.
     */
    protected final java.util.LinkedHashMap<BlockPos, BlockState> movedEarth = new java.util.LinkedHashMap<>();

    protected EarthAbility(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
    }

    protected boolean isEarthbendableCheck(BlockPos pos) {
        return Accretion.isEarthbendable(level, pos);
    }

    public int getNoiseReduction() {
        return noiseReduction;
    }

    public void setNoiseReduction(int noiseReduction) {
        this.noiseReduction = Math.max(0, noiseReduction);
    }

    /**
     * Move one earth cell one block along {@code direction}, dragging a
     * short chain behind it like ProjectKorra. Returns false when the head
     * cell cannot receive the moved block.
     */
    public boolean moveEarth(BlockPos source, Vec3 direction, int chainlength, boolean throwplayer) {
        Vec3 norm = direction.normalize();
        int dx = (int) Math.round(norm.x);
        int dy = (int) Math.round(norm.y);
        int dz = (int) Math.round(norm.z);
        BlockPos current = source.immutable();

        if (!isEarthbendableCheck(current)) {
            return false;
        }

        BlockPos affected = current.offset(dx, dy, dz);
        if (!isTransparent(affected)) {
            return false;
        }
        moveEarthBlock(current, affected);

        for (int i = 1; i < chainlength; i++) {
            BlockPos behind = current.offset(-dx * i, -dy * i, -dz * i);
            if (!isEarthbendableCheck(behind)) {
                break;
            }
            moveEarthBlock(behind, current);
            current = behind;
        }
        return true;
    }

    public boolean moveEarth(BlockPos source, Vec3 direction, int chainlength) {
        return moveEarth(source, direction, chainlength, true);
    }

    protected boolean isBlockEarth(BlockPos pos) {
        return isEarthbendableCheck(pos);
    }

    protected void addTempAirBlock(BlockPos pos) {
        rememberOriginal(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }

    /**
     * Move the visible state of {@code source} onto {@code target}; the cell
     * left behind becomes air. Natural states from the first touch are kept
     * in {@link #movedEarth} and restored by {@link #revertMovedEarth()}.
     * Unstable blocks are stabilized exactly like the reference: sand becomes
     * sandstone (red sand becomes red sandstone, gravel becomes stone,
     * concrete powder becomes concrete).
     */
    public void moveEarthBlock(BlockPos source, BlockPos target) {
        if (source.equals(target)) {
            return;
        }
        rememberOriginal(source);
        rememberOriginal(target);
        BlockState moving = stabilize(level.getBlockState(source));
        level.setBlock(target, moving, 2);
        level.setBlock(source, Blocks.AIR.defaultBlockState(), 2);
    }

    protected static BlockState stabilize(BlockState state) {
        net.minecraft.world.level.block.Block block = state.getBlock();
        if (block == Blocks.SAND) {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        if (block == Blocks.RED_SAND) {
            return Blocks.RED_SANDSTONE.defaultBlockState();
        }
        if (block == Blocks.GRAVEL) {
            return Blocks.STONE.defaultBlockState();
        }
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(block)
                .getPath()
                .toUpperCase(java.util.Locale.ROOT);
        if (path.endsWith("_POWDER")) {
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    "minecraft", path.toLowerCase(java.util.Locale.ROOT).replace("_powder", ""));
            if (net.minecraft.core.registries.BuiltInRegistries.BLOCK.containsKey(id)) {
                return net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .get(id)
                        .defaultBlockState();
            }
        }
        return state;
    }

    private void rememberOriginal(BlockPos pos) {
        remember(pos);
    }

    /** Record a cell's natural state without changing it. */
    protected void remember(BlockPos pos) {
        movedEarth.putIfAbsent(pos.immutable(), level.getBlockState(pos.immutable()));
    }

    /** Restore every touched cell to its natural state, newest move first. */
    public void revertMovedEarth() {
        List<BlockPos> order = new ArrayList<>(movedEarth.keySet());
        java.util.Collections.reverse(order);
        for (BlockPos pos : order) {
            try {
                level.setBlock(pos, movedEarth.get(pos), 2);
            } catch (RuntimeException ignored) {
                // A concurrent revert must never take the whole ability down.
            }
        }
        movedEarth.clear();
    }

    /** Restore one touched cell to its natural state, if this ability moved it. */
    public void revertCell(BlockPos pos) {
        BlockState original = movedEarth.remove(pos.immutable());
        if (original != null) {
            try {
                level.setBlock(pos.immutable(), original, 2);
            } catch (RuntimeException ignored) {
                // A concurrent revert must never take the whole ability down.
            }
        }
    }

    public boolean isTransparent(BlockPos pos) {
        return !level.getBlockState(pos).isSolid();
    }

    /**
     * Fire a transient projectile: the launch cell becomes temp air (reverts
     * with the block) and a tagged falling block carries the motion. Returns
     * the projectile, or null when the cell holds no launchable state.
     */
    protected net.minecraft.world.entity.item.FallingBlockEntity spawnShot(
            BlockPos launchCell, net.minecraft.world.level.block.state.BlockState state, Vec3 velocity) {
        if (state.isAir()) {
            return null;
        }
        TempBlock hole = new TempBlock(level, launchCell.immutable(), Blocks.AIR.defaultBlockState());
        BendingManager.scheduleRevert(hole, level.getGameTime() + 200L);
        net.minecraft.world.entity.item.FallingBlockEntity fb =
                net.minecraft.world.entity.item.FallingBlockEntity.fall(level, launchCell.immutable(), state);
        fb.dropItem = false;
        fb.addTag(SHOT_TAG);
        fb.setDeltaMovement(velocity);
        fb.hurtMarked = true;
        return fb;
    }

    /** First non-air block along the gaze within range, like getTargetBlock. */
    protected BlockPos eyeTarget(double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!level.getBlockState(pos).isAir()) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** First living entity (other than owner) along the gaze within range. */
    protected LivingEntity eyeVictim(double range, double tolerance) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        LivingEntity best = null;
        double bestAlong = Double.MAX_VALUE;
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(eye.subtract(range, range, range), eye.add(range, range, range)),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            Vec3 to = entity.position().subtract(eye);
            double along = to.dot(look);
            if (along < 0.5 || along > range) {
                continue;
            }
            if (to.subtract(look.scale(along)).length() <= tolerance && along < bestAlong) {
                bestAlong = along;
                best = entity;
            }
        }
        return best;
    }

    protected void strike(LivingEntity victim, float damage) {
        victim.hurt(player.damageSources().playerAttack(player), damage);
    }

    protected static boolean isMetalState(net.minecraft.world.level.block.state.BlockState state) {
        String name = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        return name.contains("IRON") || name.contains("GOLD") || name.contains("COPPER") || name.contains("NETHERITE");
    }

    protected static boolean isSandState(net.minecraft.world.level.block.state.BlockState state) {
        String name = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        return name.contains("SAND") || name.contains("GRAVEL");
    }

    protected static boolean isLavaState(net.minecraft.world.level.block.state.BlockState state) {
        return !state.getFluidState().isEmpty()
                && state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA);
    }

    protected boolean startFromSourceNearest(BlockPos guess, List<BlockPos> out) {
        out.clear();
        out.add(guess);
        return true;
    }

    protected BlockState prevState(BlockPos pos) {
        return level.getBlockState(pos);
    }
}
