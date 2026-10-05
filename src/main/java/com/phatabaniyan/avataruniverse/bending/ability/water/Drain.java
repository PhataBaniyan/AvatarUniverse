package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code Drain}: hold sneak to rip water out of the
 * environment. Motes stream in from nearby water, plants and open rain; with
 * a bottle or bucket held they fill it, otherwise charges bank toward water
 * blasts (click to fire, up to four). Own constructs are never drained, and
 * the held-water visual is particles instead of a per-tick temp block.
 */
public class Drain extends BendingAbility {
    public static final String ID = "Drain";

    private final ServerLevel level;
    private final boolean fillMode;
    private final long endTick;
    private final List<Vec3> motes = new ArrayList<>();
    private int absorbed;
    private int charges;
    private int blasts;

    public Drain(ServerPlayer player, boolean fillMode) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.fillMode = fillMode;
        this.endTick = player.level().getGameTime() + Config.DRAIN_DURATION_TICKS.get();
    }

    /** Click fire (Korra fireBlast): spend one banked charge on a blast. */
    public boolean fireBlast(ServerPlayer player) {
        if (fillMode || charges <= 0 || blasts >= Config.DRAIN_MAX_BLASTS.get()) {
            return false;
        }
        charges--;
        blasts++;
        BendingManager.start(new DrainBlast(player));
        return true;
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
        if (fillMode) {
            if (!player.isShiftKeyDown() || level.getGameTime() > endTick || !hasFillable(player)) {
                return false;
            }
            if (absorbed >= Config.DRAIN_ABSORB_RATE.get()) {
                absorbed = 0;
                fillContainer(player);
            }
            sampleSources(player);
        } else {
            if (blasts >= Config.DRAIN_MAX_BLASTS.get()) {
                return false;
            }
            if (player.isShiftKeyDown()) {
                sampleSources(player);
                if (absorbed >= Config.DRAIN_ABSORB_RATE.get()) {
                    absorbed = 0;
                    charges++;
                }
            } else {
                return false;
            }
        }
        dragMotes(player);
        return true;
    }

    private void fillContainer(ServerPlayer player) {
        if (player.getOffhandItem().is(Items.GLASS_BOTTLE)) {
            com.phatabaniyan.avataruniverse.bending.BendingBottles.fillWaterBottle(player);
            return;
        }
        if (player.getOffhandItem().is(Items.BUCKET)) {
            var off = player.getOffhandItem();
            if (off.getCount() == 1) {
                player.setItemInHand(
                        net.minecraft.world.InteractionHand.OFF_HAND,
                        new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET, 1));
            } else {
                off.shrink(1);
                if (!player.getInventory().add(new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET, 1))) {
                    player.drop(new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET, 1), false);
                }
            }
            return;
        }
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            var stack = items.get(i);
            if (stack.is(Items.BUCKET)) {
                if (stack.getCount() == 1) {
                    items.set(i, new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET, 1));
                } else {
                    stack.shrink(1);
                    if (!player.getInventory().add(new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET, 1))) {
                        player.drop(new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET, 1), false);
                    }
                }
                return;
            }
            if (stack.is(Items.GLASS_BOTTLE)) {
                com.phatabaniyan.avataruniverse.bending.BendingBottles.fillWaterBottle(player);
                return;
            }
        }
    }

    /** Sample random cells in radius (Korra checkForValidSource, simplified rate). */
    private void sampleSources(ServerPlayer player) {
        int radius = Config.DRAIN_RADIUS.get();
        int chance = Config.DRAIN_ABSORB_CHANCE.get();
        for (int i = 0; i < 40; i++) {
            if (level.random.nextInt(chance) != 0) {
                continue;
            }
            BlockPos pos = player.blockPosition()
                    .offset(
                            level.random.nextInt(radius * 2 + 1) - radius,
                            level.random.nextInt(radius * 2 + 1) - radius,
                            level.random.nextInt(radius * 2 + 1) - radius);
            if (pos.getY() <= level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
                continue;
            }
            if (tryRainMote(player, pos)) {
                continue;
            }
            var state = level.getBlockState(pos);
            if (BendingSources.isPlant(level, pos) && hasLineOfSight(player, pos)) {
                drainPlant(pos);
            } else if (level.getFluidState(pos).is(Fluids.WATER) && !TempBlock.isTemp(level, pos)) {
                drainWater(pos);
            }
        }
    }

    private boolean tryRainMote(ServerPlayer player, BlockPos pos) {
        if (!Config.DRAIN_ALLOW_RAIN.get() || !level.isRaining()) {
            return false;
        }
        if (!level.getBiome(player.blockPosition()).value().hasPrecipitation()) {
            return false;
        }
        if (!level.canSeeSky(player.blockPosition())) {
            return false;
        }
        if (pos.getY() < player.getY()) {
            return false;
        }
        motes.add(Vec3.atCenterOf(pos));
        return true;
    }

    private boolean hasLineOfSight(ServerPlayer player, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = Vec3.atCenterOf(pos).subtract(eye);
        double dist = dir.length();
        if (dist < 1e-6) {
            return true;
        }
        Vec3 step = dir.normalize();
        for (double d = 1.0; d <= dist; d += 1.0) {
            if (!BendingSources.isTransparentForBend(level, BlockPos.containing(eye.add(step.scale(d))))) {
                return false;
            }
        }
        return true;
    }

    private void drainPlant(BlockPos pos) {
        motes.add(Vec3.atCenterOf(pos));
        BendingManager.consumePlantSource(level, pos, Config.DRAIN_REGEN_SECONDS.get());
    }

    private void drainWater(BlockPos pos) {
        var state = level.getBlockState(pos);
        if (!level.getBlockState(pos.above()).isAir()
                || level.getFluidState(pos.above()).is(Fluids.WATER)) {
            return;
        }
        motes.add(Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0));
        TempBlock lowered;
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            lowered =
                    new TempBlock(level, pos, state.setValue(BlockStateProperties.WATERLOGGED, false), TempBlock.QUIET);
        } else {
            BlockState shallow = Blocks.WATER.defaultBlockState();
            if (shallow.hasProperty(BlockStateProperties.LEVEL)) {
                shallow = shallow.setValue(BlockStateProperties.LEVEL, 2);
            }
            lowered = new TempBlock(level, pos, shallow, TempBlock.QUIET);
        }
        BendingManager.scheduleRevert(lowered, level.getGameTime() + Config.DRAIN_REGEN_SECONDS.get() * 20L);
    }

    private void dragMotes(ServerPlayer player) {
        Vec3 goal = player.position().add(0.0, 1.0, 0.0);
        for (int i = motes.size() - 1; i >= 0; i--) {
            Vec3 mote = motes.get(i);
            Vec3 dir = goal.subtract(mote);
            Vec3 moved = mote.add(dir.normalize().scale(Config.DRAIN_ABSORB_SPEED.get()));
            level.sendParticles(
                    BendingTheme.particle(Config.DRAIN_MAIN_PARTICLE.get(), ParticleTypes.SPLASH),
                    moved.x,
                    moved.y,
                    moved.z,
                    Config.DRAIN_MAIN_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.0);
            if (moved.distanceToSqr(goal) < 1.0) {
                motes.remove(i);
                absorbed++;
            } else {
                motes.set(i, moved);
            }
        }
    }

    /** Fill mode iff the player carries a bottle or bucket (Korra canFill). */
    public static boolean hasFillable(ServerPlayer player) {
        if (player.getOffhandItem().is(Items.GLASS_BOTTLE)
                || player.getOffhandItem().is(Items.BUCKET)) {
            return true;
        }
        for (var stack : player.getInventory().items) {
            if (stack.is(Items.GLASS_BOTTLE) || stack.is(Items.BUCKET)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRemove() {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.DRAIN_COOLDOWN_TICKS.get());
        }
    }
}
