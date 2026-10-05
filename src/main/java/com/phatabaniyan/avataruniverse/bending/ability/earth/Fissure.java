package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code Fissure}
 * (jedcore/.../earthbending/Fissure.java): sneak to tear a lava crack
 * from 2 blocks ahead, extending along your gaze up to 12 cells, one
 * cell per tick. Sneak again to widen it one each side up to 3; sneak at
 * it once wide to seal it to stone early. Everything cools to stone and
 * heals on its own timers. Reference values: Cooldown 20000ms, Duration
 * 15000ms, MaxWidth 3, SlapRange 12, SlapDelay 50ms. Reference gates on
 * lavabending; here any earthbender may use it.
 */
public class Fissure extends EarthAbility {
    public static final String ID = "Fissure";

    /** Reference Cooldown 20000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FISSURE_COOLDOWN_TICKS.get();
    /** Reference Duration 15000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.FISSURE_DURATION_TICKS.get();

    private static final int MAX_WIDTH = Config.FISSURE_MAX_WIDTH.get();
    private static final int SLAP_RANGE = Config.FISSURE_SLAP_RANGE.get();
    private static final long SEAL_REVERT_TICKS = Config.FISSURE_SEAL_REVERT_TICKS.get();
    private static final double AIM_RANGE = Config.FISSURE_AIM_RANGE.get();
    private static final double ORIGIN_OFFSET = Config.FISSURE_ORIGIN_OFFSET.get();

    private final List<BlockPos> center = new ArrayList<>();
    private final List<TempBlock> lava = new ArrayList<>();
    /** Cells already converted: never re-slap, never skip twice. */
    private final java.util.Set<BlockPos> cracked = new java.util.HashSet<>();

    private final long bornTick;
    private Vec3 direction = new Vec3(0, 0, 1);
    private int slap;
    private int width;
    private boolean valid;

    public Fissure(ServerPlayer player) {
        super(player);
        this.bornTick = player.level().getGameTime();
        this.valid = prepare();
        if (valid) {
            cool(owner, player, ID, COOLDOWN_TICKS);
        }
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: bendable ground two ahead to crack from. */
    public static boolean canBegin(ServerPlayer player) {
        return originOf(player) != null;
    }

    private static BlockPos originOf(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) {
            return null;
        }
        flat = flat.normalize();
        BlockPos probe = BlockPos.containing(
                player.getX() + flat.x * ORIGIN_OFFSET, player.getY(), player.getZ() + flat.z * ORIGIN_OFFSET);
        BlockPos ground = snap(player, probe);
        if (ground != null && Accretion.isEarthbendable(player.serverLevel(), ground)) {
            return ground.immutable();
        }
        return null;
    }

    /** Topmost bendable ground with open headroom near a probe; lava cover passes through like the original. */
    private static BlockPos snap(ServerPlayer player, BlockPos probe) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = player.serverLevel().getBlockState(cell);
            BlockState cap = player.serverLevel().getBlockState(cell.above());
            if (state.isAir() || cap.isSolid() || isWater(cap)) {
                continue;
            }
            if (Accretion.isEarthbendable(player.serverLevel(), cell)) {
                return cell.immutable();
            }
        }
        return null;
    }

    private static boolean isWater(BlockState state) {
        return !state.getFluidState().isEmpty()
                && !state.getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA);
    }

    private boolean prepare() {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) {
            return false;
        }
        flat = flat.normalize();
        // 8-directional march: keep every meaningful axis so diagonal
        // gazes stair-step instead of collapsing to cardinal.
        double sx = Math.abs(flat.x) < 0.35 ? 0 : Math.signum(flat.x);
        double sz = Math.abs(flat.z) < 0.35 ? 0 : Math.signum(flat.z);
        Vec3 step = new Vec3(sx, 0, sz);
        if (step.lengthSqr() < 1.0e-6) {
            return false;
        }
        direction = step;
        BlockPos origin = originOf(player);
        if (origin == null) {
            return false;
        }
        // Rook-stepped march: diagonal gazes advance one axis at a time
        // so stairs land on edge-adjacent cells (solid line), never corner
        // touches (checkerboard). Relative stepping follows slopes too.
        List<Vec3> moves = unitMoves(step);
        int iters = moves.size() > 1 ? 9 : SLAP_RANGE;
        BlockPos cursor = origin;
        center.add(origin);
        for (int i = 0; i < iters && center.size() < SLAP_RANGE * 2; i++) {
            for (Vec3 m : moves) {
                BlockPos probe =
                        BlockPos.containing(cursor.getX() + 0.5 + m.x, cursor.getY() + 0.5, cursor.getZ() + 0.5 + m.z);
                BlockPos ground = snap(player, probe);
                if (ground == null) {
                    return !center.isEmpty();
                }
                if (!center.contains(ground)) {
                    center.add(ground);
                }
                cursor = ground;
            }
        }
        return !center.isEmpty();
    }

    /** Axis unit moves for a direction: one for cardinal, two for diagonal. */
    private static List<Vec3> unitMoves(Vec3 v) {
        List<Vec3> out = new ArrayList<>();
        if (Math.abs(v.x) > 0.5) {
            out.add(new Vec3(Math.signum(v.x), 0, 0));
        }
        if (Math.abs(v.z) > 0.5) {
            out.add(new Vec3(0, 0, Math.signum(v.z)));
        }
        if (out.isEmpty()) {
            out.add(new Vec3(0, 0, 1));
        }
        return out;
    }

    /** Sneak again: widen now; holding sneak keeps spreading on its own. */
    public void widenOrSeal() {
        if (width < MAX_WIDTH) {
            widenStep();
            return;
        }
        if (aimedAtFissure()) {
            seal();
            BendingManager.remove(this);
        }
    }

    private void widenStep() {
        width++;
        Vec3 side = new Vec3(-direction.z, 0, direction.x);
        List<Vec3> sideways = unitMoves(side);
        for (BlockPos middle : new ArrayList<>(center)) {
            for (int s : new int[] {-1, 1}) {
                BlockPos at = middle;
                for (int k = 0; k < width; k++) {
                    Vec3 m = sideways.get(k % sideways.size());
                    BlockPos probe =
                            BlockPos.containing(at.getX() + 0.5 + m.x * s, at.getY() + 0.5, at.getZ() + 0.5 + m.z * s);
                    BlockPos ground = snap(player, probe);
                    if (ground == null) {
                        break;
                    }
                    at = ground;
                }
                BlockPos ground = snap(player, at);
                if (ground != null && !cracked.contains(ground)) {
                    cracked.add(ground);
                    crack(ground);
                }
            }
        }
    }

    private boolean aimedAtFissure() {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= AIM_RANGE; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (isFissureCell(pos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFissureCell(BlockPos pos) {
        return TempBlock.isTemp(level, pos)
                && level.getBlockState(pos).getFluidState().is(net.minecraft.world.level.material.Fluids.LAVA);
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !valid) {
            return false;
        }
        if (level.getGameTime() - bornTick > DURATION_TICKS) {
            seal();
            return false;
        }
        if (slap < center.size()) {
            BlockPos cell = center.get(slap);
            slap++;
            if (!cracked.contains(cell)) {
                cracked.add(cell);
                crack(cell);
            }
        }
        return true;
    }

    /** Tear one cell into tracked lava with hiss and spray, halo-starved so it cannot ooze. */
    private void crack(BlockPos ground) {
        level.sendParticles(
                BendingTheme.particle(Config.FISSURE_CRACK_PARTICLE.get(), ParticleTypes.LAVA),
                ground.getX() + 0.5,
                ground.getY() + 1.0,
                ground.getZ() + 0.5,
                Config.FISSURE_CRACK_PARTICLE_COUNT.get(),
                0.1,
                0.2,
                0.1,
                0.0);
        level.playSound(
                null,
                ground.getX() + 0.5,
                ground.getY() + 1.0,
                ground.getZ() + 0.5,
                SoundEvents.STONE_BREAK,
                SoundSource.PLAYERS,
                0.7F,
                1.0F);
        lava.add(new TempBlock(level, ground, Blocks.LAVA.defaultBlockState(), TempBlock.QUIET));
        level.getFluidTicks()
                .clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(
                        ground.getX() - 1,
                        ground.getY() - 1,
                        ground.getZ() - 1,
                        ground.getX() + 1,
                        ground.getY() + 1,
                        ground.getZ() + 1));
    }

    /** Seal every lava cell to stone on a short timer, then let go. */
    private void seal() {
        for (TempBlock temp : new ArrayList<>(lava)) {
            BlockPos pos = temp.pos();
            temp.revert();
            TempBlock stone = new TempBlock(level, pos, Blocks.STONE.defaultBlockState(), TempBlock.QUIET);
            BendingManager.scheduleRevert(stone, level.getGameTime() + SEAL_REVERT_TICKS);
        }
        lava.clear();
    }

    @Override
    public void onRemove() {
        seal();
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
