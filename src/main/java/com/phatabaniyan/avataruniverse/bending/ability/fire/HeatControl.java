package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code HeatControl} (ProjectKorra port: cook +
 * extinguish + melt + solidify). One bind, hold sneak: cooks the held food,
 * melts ice/snow at the cursor once, grows a solidify ring on targeted lava
 * (magma then stone/cobble), and snuffs fire around you. Fallout order on
 * start: cookable in hand wins, then lava in sight, else extinguish.
 * Reference values: Cooldown 2000ms, CookMs 1500, ExtinguishRadius 6,
 * ExtinguishCooldown 2000ms, MeltRange 10, MeltRadius 3, SolidifyRange 10,
 * SolidifyMaxRadius 5, SolidifyRevert true after 600000ms, magma-to-stone
 * 1000ms, ring step 50ms, melt-water revert 5min.
 */
public class HeatControl extends BendingAbility {
    public static final String ID = "HeatControl";

    /** Reference Cooldown 2000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.HEATCONTROL_COOLDOWN_TICKS.get();
    /** Reference CookMs 1500ms, in server ticks. */
    private static final long COOK_INTERVAL_TICKS = Config.HEATCONTROL_COOK_INTERVAL_TICKS.get();
    /** Reference ExtinguishCooldown 2000ms, in server ticks. */
    private static final long EXTINGUISH_COOLDOWN_TICKS = Config.HEATCONTROL_EXTINGUISH_COOLDOWN_TICKS.get();
    /** Reference magma-to-stone 1000ms, in server ticks. */
    private static final long MAGMA_DELAY_TICKS = Config.HEATCONTROL_MAGMA_DELAY_TICKS.get();
    /** Reference ring step 50ms, in server ticks. */
    private static final long SOLIDIFY_STEP_TICKS = Config.HEATCONTROL_SOLIDIFY_STEP_TICKS.get();
    /** Reference melt-water revert 5min, in server ticks. */
    private static final long MELT_REVERT_TICKS = Config.HEATCONTROL_MELT_REVERT_TICKS.get();
    /** Reference SolidifyRevertMs 600000ms, in server ticks. */
    private static final long SOLIDIFY_REVERT_TICKS = Config.HEATCONTROL_SOLIDIFY_REVERT_TICKS.get();

    private static final double EXTINGUISH_RADIUS = Config.HEATCONTROL_EXTINGUISH_RADIUS.get();
    private static final double MELT_RANGE = Config.HEATCONTROL_MELT_RANGE.get();
    private static final double MELT_RADIUS = Config.HEATCONTROL_MELT_RADIUS.get();
    private static final double SOLIDIFY_RANGE = Config.HEATCONTROL_SOLIDIFY_RANGE.get();
    private static final double SOLIDIFY_MAX_RADIUS = Config.HEATCONTROL_SOLIDIFY_MAX_RADIUS.get();
    private static final boolean SOLIDIFY_REVERT = Config.HEATCONTROL_SOLIDIFY_REVERT.get();

    public enum Mode {
        COOK,
        EXTINGUISH,
        SOLIDIFY
    }

    private final ServerPlayer player;
    private final ServerLevel level;
    private final Mode mode;
    private long lastCookTick;
    private int solidifyRadius = 1;
    private long lastSolidifyTick;
    private BlockPos solidifyCenter = null;
    private final Random rand = new Random();
    private final Map<BlockPos, Long> pendingStone = new ConcurrentHashMap<>();
    private final Map<BlockPos, BlockState> pendingOrig = new ConcurrentHashMap<>();
    private final Map<BlockPos, StoneRevert> pendingReverts = new ConcurrentHashMap<>();

    private record StoneRevert(BlockState original, long at) {}

    private static final Map<net.minecraft.world.item.Item, net.minecraft.world.item.Item> COOKED = Map.ofEntries(
            Map.entry(Items.BEEF, Items.COOKED_BEEF),
            Map.entry(Items.CHICKEN, Items.COOKED_CHICKEN),
            Map.entry(Items.COD, Items.COOKED_COD),
            Map.entry(Items.PORKCHOP, Items.COOKED_PORKCHOP),
            Map.entry(Items.POTATO, Items.BAKED_POTATO),
            Map.entry(Items.RABBIT, Items.COOKED_RABBIT),
            Map.entry(Items.MUTTON, Items.COOKED_MUTTON),
            Map.entry(Items.SALMON, Items.COOKED_SALMON),
            Map.entry(Items.KELP, Items.DRIED_KELP),
            Map.entry(Items.CHORUS_FRUIT, Items.POPPED_CHORUS_FRUIT),
            Map.entry(Items.WET_SPONGE, Items.SPONGE),
            Map.entry(Items.STICK, Items.TORCH));

    public HeatControl(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.lastCookTick = startTime - COOK_INTERVAL_TICKS;
        this.lastSolidifyTick = startTime;

        // Melt pulse fires once at the cursor, whatever the hold mode is.
        meltPulse(player);

        if (isCookable(player.getMainHandItem().getItem())) {
            this.mode = Mode.COOK;
        } else if (findLava(player, SOLIDIFY_RANGE) != null) {
            this.mode = Mode.SOLIDIFY;
        } else {
            this.mode = Mode.EXTINGUISH;
        }
    }

    @Override
    public String name() {
        return ID;
    }

    private void meltPulse(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        BlockPos target = null;
        for (double d = 0; d <= MELT_RANGE; d += 0.5) {
            BlockPos bp = BlockPos.containing(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            if (!level.isLoaded(bp)) {
                break;
            }
            if (level.getBlockState(bp).isSolidRender(level, bp)) {
                target = bp.immutable();
                break;
            }
            target = bp.immutable();
        }
        if (target == null) {
            return;
        }
        int r = (int) Math.ceil(MELT_RADIUS);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) {
                        continue;
                    }
                    BlockPos bp = target.offset(dx, dy, dz);
                    if (!level.isLoaded(bp)) {
                        continue;
                    }
                    var state = level.getBlockState(bp);
                    if (!isMeltable(state)) {
                        continue;
                    }
                    if (state.is(Blocks.SNOW)) {
                        level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
                    } else {
                        TempBlock temp =
                                new TempBlock(level, bp.immutable(), Blocks.WATER.defaultBlockState(), TempBlock.QUIET);
                        BendingManager.scheduleRevert(temp, level.getGameTime() + MELT_REVERT_TICKS);
                    }
                }
            }
        }
    }

    private static boolean isMeltable(BlockState state) {
        return state.is(Blocks.ICE)
                || state.is(Blocks.FROSTED_ICE)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK);
    }

    private static boolean isCookable(net.minecraft.world.item.Item item) {
        return COOKED.containsKey(item);
    }

    public static BlockPos findLava(ServerPlayer player, double range) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0; d <= range; d += 0.5) {
            BlockPos bp = BlockPos.containing(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            if (!level.isLoaded(bp)) {
                break;
            }
            var state = level.getBlockState(bp);
            if (state.is(Blocks.LAVA) && state.getFluidState().isSource()) {
                return bp.immutable();
            }
            if (state.isSolidRender(level, bp) && !state.is(Blocks.LAVA)) {
                break;
            }
        }
        return null;
    }

    /** Upstream canBurn: channeling HeatControl or jetting clears fire ticks. */
    public static boolean canBurn(ServerPlayer player) {
        if (BendingManager.find(player.getUUID(), HeatControl.class) != null) {
            player.clearFire();
            return false;
        }
        if (BendingManager.find(player.getUUID(), FireJet.class) != null) {
            player.clearFire();
            return false;
        }
        return true;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        drainPending(level);
        if (!player.isShiftKeyDown()) {
            if (this.mode == Mode.EXTINGUISH) {
                cool(owner, level, EXTINGUISH_COOLDOWN_TICKS);
            } else {
                cool(owner, level, COOLDOWN_TICKS);
            }
            return false;
        }
        if (this.mode == Mode.COOK) {
            return progressCook(player, level);
        } else if (this.mode == Mode.EXTINGUISH) {
            progressExtinguish(player, level);
        } else {
            return progressSolidify(player, level);
        }
        return true;
    }

    private boolean progressCook(ServerPlayer sp, ServerLevel level) {
        ItemStack hand = sp.getMainHandItem();
        if (hand.isEmpty() || !isCookable(hand.getItem())) {
            cool(owner, level, COOLDOWN_TICKS);
            return false;
        }
        long now = level.getGameTime();
        if (now - this.lastCookTick >= COOK_INTERVAL_TICKS) {
            this.lastCookTick = now;
            net.minecraft.world.item.Item out = COOKED.get(hand.getItem());
            hand.shrink(1);
            ItemStack cooked = new ItemStack(out);
            if (!sp.addItem(cooked)) {
                sp.drop(cooked, false);
            }
            level.playSound(
                    null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.4F);
        }
        Vec3 at = sp.position().add(0, 1, 0);
        level.sendParticles(
                BendingTheme.particle(Config.HEATCONTROL_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                at.x,
                at.y,
                at.z,
                Config.HEATCONTROL_FLAME_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.05);
        level.sendParticles(
                BendingTheme.particle(Config.HEATCONTROL_COOK_PARTICLE.get(), ParticleTypes.SMOKE),
                at.x,
                at.y,
                at.z,
                Config.HEATCONTROL_COOK_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.05);
        return true;
    }

    private void progressExtinguish(ServerPlayer sp, ServerLevel level) {
        int r = (int) Math.ceil(EXTINGUISH_RADIUS);
        BlockPos center = sp.blockPosition();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos bp = center.offset(dx, dy, dz);
                    if (!level.isLoaded(bp)) {
                        continue;
                    }
                    var state = level.getBlockState(bp);
                    if (state.is(Blocks.FIRE)) {
                        level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
                        level.sendParticles(
                                BendingTheme.particle(
                                        Config.HEATCONTROL_EXTINGUISH_PARTICLE.get(), ParticleTypes.SMOKE),
                                bp.getX() + 0.5,
                                bp.getY() + 0.5,
                                bp.getZ() + 0.5,
                                Config.HEATCONTROL_EXTINGUISH_PARTICLE_COUNT.get(),
                                0.2,
                                0.2,
                                0.2,
                                0.03);
                    } else if (state.is(Blocks.WET_SPONGE)
                            && !hasWaterNeighbour(level, bp)
                            && this.rand.nextInt(5) == 0) {
                        level.setBlockAndUpdate(bp, Blocks.SPONGE.defaultBlockState());
                    }
                }
            }
        }
    }

    private static boolean hasWaterNeighbour(ServerLevel level, BlockPos bp) {
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos nb = bp.relative(dir);
            if (level.isLoaded(nb) && !level.getFluidState(nb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean progressSolidify(ServerPlayer sp, ServerLevel level) {
        if (this.solidifyRadius >= SOLIDIFY_MAX_RADIUS) {
            cool(owner, level, COOLDOWN_TICKS);
            return false;
        }
        Vec3 eye = new Vec3(sp.getX(), sp.getEyeY(), sp.getZ());
        Vec3 look = sp.getLookAngle().normalize();
        BlockPos target = null;
        for (double d = 0; d <= SOLIDIFY_RANGE; d += 0.5) {
            BlockPos bp = BlockPos.containing(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            if (!level.isLoaded(bp)) {
                break;
            }
            var state = level.getBlockState(bp);
            if (state.is(Blocks.LAVA)) {
                target = bp.immutable();
                break;
            }
            if (state.isSolidRender(level, bp)) {
                break;
            }
        }
        if (target == null) {
            return true;
        }
        if (this.solidifyCenter == null || !this.solidifyCenter.equals(target)) {
            this.solidifyCenter = target;
            this.solidifyRadius = 1;
        }
        long now = level.getGameTime();
        if (now < this.lastSolidifyTick + SOLIDIFY_STEP_TICKS) {
            return true;
        }
        this.lastSolidifyTick = now;
        List<BlockPos> lava = new ArrayList<>();
        int r = this.solidifyRadius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos bp = this.solidifyCenter.offset(dx, dy, dz);
                    if (!level.isLoaded(bp)) {
                        continue;
                    }
                    if (level.getBlockState(bp).is(Blocks.LAVA)) {
                        lava.add(bp.immutable());
                    }
                }
            }
        }
        if (lava.isEmpty()) {
            this.solidifyRadius++;
            return true;
        }
        BlockPos pick = lava.get(this.rand.nextInt(lava.size()));
        var orig = level.getBlockState(pick);
        level.setBlockAndUpdate(pick, Blocks.MAGMA_BLOCK.defaultBlockState());
        this.pendingOrig.put(pick, orig);
        this.pendingStone.put(pick, now + MAGMA_DELAY_TICKS);
        level.sendParticles(
                BendingTheme.particle(Config.HEATCONTROL_SOLIDIFY_PARTICLE.get(), ParticleTypes.SMOKE),
                pick.getX() + 0.5,
                pick.getY() + 1,
                pick.getZ() + 0.5,
                Config.HEATCONTROL_SOLIDIFY_PARTICLE_COUNT.get(),
                0.1,
                0.1,
                0.1,
                0.05);
        return true;
    }

    private void drainPending(ServerLevel level) {
        long now = level.getGameTime();
        if (!this.pendingStone.isEmpty()) {
            for (var it = this.pendingStone.entrySet().iterator(); it.hasNext(); ) {
                var e = it.next();
                if (now < e.getValue()) {
                    continue;
                }
                BlockPos bp = e.getKey();
                it.remove();
                var orig = this.pendingOrig.remove(bp);
                if (!level.isLoaded(bp)) {
                    continue;
                }
                boolean cobble = this.rand.nextBoolean();
                var stone = cobble ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                // A TempBlock captures whatever stands at construction, but the
                // cell currently holds our own transient magma, not the lava
                // the source reverts to — so the pre-magma original is kept in
                // pendingOrig and restored here on the source tick schedule.
                if (SOLIDIFY_REVERT && orig != null) {
                    this.pendingReverts.put(bp, new StoneRevert(orig, now + SOLIDIFY_REVERT_TICKS));
                }
                level.setBlockAndUpdate(bp, stone);
                level.sendParticles(
                        BendingTheme.particle(Config.HEATCONTROL_STONE_PARTICLE.get(), ParticleTypes.SMOKE),
                        bp.getX() + 0.5,
                        bp.getY() + 1,
                        bp.getZ() + 0.5,
                        Config.HEATCONTROL_STONE_PARTICLE_COUNT.get(),
                        0.1,
                        0.1,
                        0.1,
                        0.05);
                if (this.rand.nextInt(3) == 0) {
                    level.playSound(
                            null,
                            bp.getX(),
                            bp.getY(),
                            bp.getZ(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            0.5F,
                            1.0F);
                }
            }
        }
        if (!this.pendingReverts.isEmpty()) {
            for (var it = this.pendingReverts.entrySet().iterator(); it.hasNext(); ) {
                var e = it.next();
                if (now < e.getValue().at()) {
                    continue;
                }
                BlockPos bp = e.getKey();
                StoneRevert revert = e.getValue();
                it.remove();
                if (!level.isLoaded(bp)) {
                    continue;
                }
                // Mirrors TempRevert: only restore when nothing else claimed
                // the cell (mined to air); never overwrite other builds.
                if (level.getBlockState(bp).isAir()) {
                    level.setBlockAndUpdate(bp, revert.original());
                }
            }
        }
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + ticks);
        }
    }
}
