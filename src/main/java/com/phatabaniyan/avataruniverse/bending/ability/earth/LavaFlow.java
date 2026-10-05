package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code LavaFlow}
 * (core/.../earthbending/lava/LavaFlow.java from the local
 * ProjectKorra-master copy). Hold sneak to bloom lava outward from under
 * your feet to radius 7 (a 1.5 safe platform stays put); release and it
 * melts back staggered. Click air or earth to blast a long lava
 * rectangle from 1 block out down the click direction with no delay
 * (reverts after 7s), click lava to slowly petrify it to stone for 20s. All lava and stone are tracked TempBlocks, so everything
 * restores. Reference values: ShiftCooldown 20s, ClickLavaCooldown 10s,
 * ClickLandCooldown 0.5s, cleanups 10s/7s/20s, ClickRange 10, ShiftRadius
 * 7, ClickRadius 5, RevertMaterial stone. Reference gates on lavabending;
 * here any earthbender may use it.
 */
public class LavaFlow extends EarthAbility {
    public static final String ID = "LavaFlow";

    private static final double SHIFT_RADIUS = Config.LAVAFLOW_SHIFT_RADIUS.get();
    private static final double SHIFT_PLATFORM = Config.LAVAFLOW_SHIFT_PLATFORM.get();
    private static final double CLICK_RANGE = Config.LAVAFLOW_CLICK_RANGE.get();
    private static final double CLICK_RADIUS = Config.LAVAFLOW_CLICK_RADIUS.get();
    /** Directional lava rectangle: length and half-width. */
    private static final double RECT_LENGTH = Config.LAVAFLOW_RECT_LENGTH.get();

    private static final double RECT_HALF_WIDTH = Config.LAVAFLOW_RECT_HALF_WIDTH.get();
    private static final long SHIFT_COOLDOWN = Config.LAVAFLOW_SHIFT_COOLDOWN_TICKS.get();
    private static final long CLICK_LAVA_COOLDOWN = Config.LAVAFLOW_CLICK_LAVA_COOLDOWN_TICKS.get();
    private static final long CLICK_LAND_COOLDOWN = Config.LAVAFLOW_CLICK_LAND_COOLDOWN_TICKS.get();
    private static final long SHIFT_CLEANUP = Config.LAVAFLOW_SHIFT_CLEANUP_TICKS.get();
    private static final long CLICK_LAVA_CLEANUP = Config.LAVAFLOW_CLICK_LAVA_CLEANUP_TICKS.get();
    private static final long CLICK_LAND_CLEANUP = Config.LAVAFLOW_CLICK_LAND_CLEANUP_TICKS.get();
    private static final long CLICK_LAVA_DELAY = Config.LAVAFLOW_CLICK_LAVA_DELAY_TICKS.get();
    private static final long CLICK_LAND_DELAY = Config.LAVAFLOW_CLICK_LAND_DELAY_TICKS.get();

    private final boolean shiftMode;
    private final long bornTick;
    private BlockPos origin;
    /** Frozen click direction for the lava sector. */
    private Vec3 clickDir = new Vec3(0, 0, 1);

    private double radius;
    private boolean makeLava = true;
    private boolean removing;
    private final List<TempBlock> affected = new ArrayList<>();

    public LavaFlow(ServerPlayer player, boolean shiftMode) {
        super(player);
        this.shiftMode = shiftMode;
        this.bornTick = player.level().getGameTime();
        if (!shiftMode) {
            BlockPos source = eyeTarget(CLICK_RANGE);
            Vec3 look = player.getLookAngle().normalize();
            Vec3 flat = new Vec3(look.x, 0, look.z);
            clickDir = flat.lengthSqr() < 1.0e-6 ? new Vec3(0, 0, 1) : flat.normalize();
            if (source == null) {
                // Click in air: lava sector from the caster down the gaze.
                origin = player.blockPosition().immutable();
                makeLava = true;
                cool(owner, player, ID, CLICK_LAVA_COOLDOWN);
            } else {
                makeLava = !isLava(level.getBlockState(source));
                origin = (makeLava ? player.blockPosition() : source).immutable();
                cool(owner, player, ID, makeLava ? CLICK_LAVA_COOLDOWN : CLICK_LAND_COOLDOWN);
            }
        }
    }

    @Override
    public String name() {
        return ID;
    }

    public boolean isShiftMode() {
        return shiftMode;
    }

    /** Sneak-start gate: bendable ground underfoot to bloom from. */
    public static boolean canBegin(ServerPlayer player) {
        return Accretion.isEarthbendable(
                player.serverLevel(), player.blockPosition().below());
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        long age = level.getGameTime() - bornTick;
        if (shiftMode) {
            if (age > SHIFT_CLEANUP) {
                return false;
            }
            if (!player.isShiftKeyDown()) {
                if (affected.isEmpty()) {
                    return false;
                }
                if (!removing) {
                    cool(owner, player, ID, SHIFT_COOLDOWN);
                }
                removing = true;
            }
            if (removing) {
                drain(10);
                return !affected.isEmpty();
            }
            if (origin == null) {
                origin = player.blockPosition().below().immutable();
                if (!Accretion.isEarthbendable(level, origin)) {
                    return false;
                }
            }
            radius = Math.min(SHIFT_RADIUS, radius + 0.35);
            int r = (int) Math.ceil(radius);
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    double d2 = x * x + z * z;
                    if (d2 <= SHIFT_PLATFORM * SHIFT_PLATFORM || d2 > radius * radius) {
                        continue;
                    }
                    BlockPos ground = snap(origin.offset(x, 0, z));
                    if (ground == null || TempBlock.isTemp(level, ground)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(ground);
                    if (!isLava(state) && convertible(ground)) {
                        affected.add(new TempBlock(level, ground, Blocks.LAVA.defaultBlockState(), TempBlock.QUIET));
                    }
                }
            }
            if (level.random.nextInt(3) == 0) {
                rimDust(origin, radius);
            }
            return true;
        }
        if (origin == null) {
            return false;
        }
        long cleanup = makeLava ? CLICK_LAVA_CLEANUP : CLICK_LAND_CLEANUP;
        if (age > cleanup) {
            return false;
        }
        long delay = makeLava ? CLICK_LAVA_DELAY : CLICK_LAND_DELAY;
        if (!makeLava && age < delay) {
            if (level.random.nextInt(5) == 0) {
                rimDust(origin, CLICK_RADIUS);
            }
            return true;
        }
        int budget = makeLava ? 12 : 8;
        if (makeLava) {
            budget = buildRectangle(budget);
        } else {
            int r = (int) CLICK_RADIUS;
            for (int x = -r; x <= r && budget > 0; x++) {
                for (int z = -r; z <= r && budget > 0; z++) {
                    if (x * x + z * z > CLICK_RADIUS * CLICK_RADIUS) {
                        continue;
                    }
                    BlockPos ground = snap(origin.offset(x, 0, z));
                    if (ground == null || TempBlock.isTemp(level, ground)) {
                        continue;
                    }
                    if (isLava(level.getBlockState(ground))) {
                        affected.add(new TempBlock(level, ground, Blocks.STONE.defaultBlockState(), TempBlock.QUIET));
                        budget--;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Grow the lava rectangle: cells 1..LENGTH out along the frozen click
     * direction within the half-width, converted a few per tick.
     */
    private int buildRectangle(int budget) {
        Vec3 side = new Vec3(-clickDir.z, 0, clickDir.x);
        int r = (int) Math.ceil(RECT_LENGTH + RECT_HALF_WIDTH);
        for (int x = -r; x <= r && budget > 0; x++) {
            for (int z = -r; z <= r && budget > 0; z++) {
                double along = x * clickDir.x + z * clickDir.z;
                double lateral = Math.abs(x * side.x + z * side.z);
                if (along < 1.0 || along > RECT_LENGTH || lateral > RECT_HALF_WIDTH) {
                    continue;
                }
                BlockPos ground = snap(origin.offset(x, 0, z));
                if (ground == null || TempBlock.isTemp(level, ground)) {
                    continue;
                }
                BlockState state = level.getBlockState(ground);
                if (!isLava(state) && convertible(ground)) {
                    affected.add(new TempBlock(level, ground, Blocks.LAVA.defaultBlockState(), TempBlock.QUIET));
                    budget--;
                }
            }
        }
        return budget;
    }

    /** Ground-snap: topmost convertible-or-lava cell with open headroom. */
    private BlockPos snap(BlockPos probe) {
        for (int dy = 2; dy >= -2; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = level.getBlockState(cell);
            BlockState cap = level.getBlockState(cell.above());
            if (state.isAir() || cap.isSolid() || !cap.getFluidState().isEmpty()) {
                continue;
            }
            if (isLava(state) || convertible(cell)) {
                return cell.immutable();
            }
        }
        return null;
    }

    /** Earth, sand or metal ground the flow may claim. */
    private boolean convertible(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return Accretion.isEarthbendable(level, pos) || isMetalState(state) || isSandState(state);
    }

    private static boolean isLava(BlockState state) {
        return state.getFluidState().is(Fluids.LAVA);
    }

    private void rimDust(BlockPos center, double radius) {
        for (int i = 0; i < 6; i++) {
            double theta = level.random.nextDouble() * 2 * Math.PI;
            level.sendParticles(
                    BendingTheme.particle(Config.LAVAFLOW_RIM_PARTICLE.get(), ParticleTypes.LAVA),
                    center.getX() + 0.5 + Math.cos(theta) * radius,
                    center.getY() + 1.0,
                    center.getZ() + 0.5 + Math.sin(theta) * radius,
                    Config.LAVAFLOW_RIM_PARTICLE_COUNT.get(),
                    0.1,
                    0.2,
                    0.1,
                    0.0);
        }
    }

    /** Staggered revert so pools melt back instead of blinking out. */
    private void drain(int perTick) {
        for (int i = 0; i < perTick && !affected.isEmpty(); i++) {
            affected.remove(affected.size() - 1).revert();
        }
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : new ArrayList<>(affected)) {
            temp.revert();
        }
        affected.clear();
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
