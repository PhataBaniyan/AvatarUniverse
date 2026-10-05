package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code WaterSpout} with the reference implementation's
 * flight model: toggle water column that sustains while water is below,
 * follows the player, and lifts via vanilla flight (mayfly + thrust, never
 * velocity writes, so no moved-too-quickly snapbacks). Re-cast toggles off
 * (+hop launch when sneaking). Spiral ring, splash and ambient sound
 * included. Ice/snow count as base like Korra.
 *
 * <p>Every column and spiral block is a quiet {@link TempBlock} with fluid
 * ticks cleared, so nothing flows; all revert on removal.</p>
 */
public class WaterSpout extends BendingAbility {
    public static final String ID = "WaterSpout";

    /** Column cap (Korra Height 16); removal past base + cap + tolerance. */
    private static final int MAX_HEIGHT = 21;

    private static final double OVER_HEIGHT_TOLERANCE = 2.0;
    /** Canonical ~4 b/s cap as fly thrust (drag ~= 0.09), never velocity writes. */
    private static final float SPOUT_FLY_SPEED = 0.018F;

    private static final double FLY_RELEASE_ABOVE_TOP = 1.0;
    private static final int FLY_RELEASE_GRACE_TICKS = 5;
    private static final int ABILITIES_HEARTBEAT_TICKS = 40;
    private static final long PARTICLE_INTERVAL_MS = 50L;

    private final ServerLevel level;
    /**
     * Every block this spout owns (shaft + spiral ring): exactly one
     * TempBlock per position. The split maps previously allowed two temps on
     * one cell (double-capture, order-dependent restores) and let the shaft
     * cleanup reap live spiral cells — both read as vanishing blocks.
     */
    private final Map<BlockPos, TempBlock> owned = new HashMap<>();

    private BlockPos base;
    private TempBlock baseTemp;
    private final boolean prevMayfly;
    private final boolean prevFlying;
    private final float prevFlySpeed;
    private double rotation;
    private long lastParticleTime;
    private boolean flying = true;
    private boolean lastMayflySent;
    private boolean lastFlyingSent;
    private float lastFlySpeedSent = 0.05F;
    private int releaseGrace;
    private int heartbeat;

    public WaterSpout(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.prevMayfly = player.getAbilities().mayfly;
        this.prevFlying = player.getAbilities().flying;
        this.prevFlySpeed = player.getAbilities().getFlyingSpeed();
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
        if (bending == null
                || !bending.hasElement(com.phatabaniyan.avataruniverse.bending.BendingElement.WATER)
                || !bending.isToggled()) {
            return false;
        }
        // A live wave holds the bind (mutual exclusion both ways).
        if (BendingManager.find(owner, WaterSpoutWave.class) != null) {
            return false;
        }

        // Down-scan for a spout base: fluid water, ice or snow (Korra).
        // Solid non-bendable ground above any water vetoes the spout: no
        // columns through stone floors over cave lakes. Live temp cells are
        // never adopted (own column included): otherwise the rising scan
        // finds our own water a block below the feet, the column collapses
        // to a stub, and the spout dies a few blocks up.
        Vec3 origin = player.position().add(0.0, 0.2, 0.0);
        BlockPos found = null;
        int gap = -1;
        for (int i = 0; i <= MAX_HEIGHT; i++) {
            BlockPos pos = BlockPos.containing(origin).below(i);
            if (TempBlock.isTemp(level, pos)) {
                continue;
            }
            if (isSpoutBase(pos)) {
                found = pos.immutable();
                gap = i;
                break;
            }
            var scanState = level.getBlockState(pos);
            if (!scanState.isAir() && !BendingSources.isPlant(level, pos)) {
                return false;
            }
        }
        if (found == null) {
            return false;
        }
        if (player.getY() > found.getY() + MAX_HEIGHT + OVER_HEIGHT_TOLERANCE) {
            return false;
        }

        base = found;
        int height = (int) Math.min(gap, Config.WATERSPOUT_HEIGHT.get());
        int top = found.getY() + height;
        Set<BlockPos> shaftWant = new HashSet<>();
        for (int i = 1; i <= height; i++) {
            shaftWant.add(found.above(i).immutable());
        }
        Set<BlockPos> spiralWant = spiralCells(player);
        // Ice/snow bases get a water cover like Korra's baseBlock.
        if (!level.getFluidState(found).is(net.minecraft.world.level.material.Fluids.WATER)) {
            if (baseTemp == null) {
                baseTemp = new TempBlock(level, found, Blocks.WATER.defaultBlockState(), TempBlock.QUIET);
            }
        } else if (baseTemp != null) {
            baseTemp.revert();
            baseTemp = null;
        }
        BlockPos coveredBase = baseTemp != null ? found : null;
        for (TempBlock temp : new java.util.ArrayList<>(owned.values())) {
            if (!shaftWant.contains(temp.pos()) && !spiralWant.contains(temp.pos())) {
                temp.revert();
                owned.remove(temp.pos());
            }
        }
        for (BlockPos pos : shaftWant) {
            if (owned.containsKey(pos) || (coveredBase != null && coveredBase.equals(pos))) {
                continue;
            }
            if (BendingSources.isTransparentForBend(level, pos)) {
                owned.put(pos, new TempBlock(level, pos, Blocks.WATER.defaultBlockState(), TempBlock.QUIET));
            }
        }
        for (BlockPos pos : spiralWant) {
            if (owned.containsKey(pos) || (coveredBase != null && coveredBase.equals(pos))) {
                continue;
            }
            if (BendingSources.isTransparentForBend(level, pos)) {
                BlockState spiral = Blocks.WATER.defaultBlockState();
                if (spiral.hasProperty(BlockStateProperties.LEVEL)) {
                    spiral = spiral.setValue(BlockStateProperties.LEVEL, 7);
                }
                owned.put(pos, new TempBlock(level, pos, spiral, TempBlock.QUIET));
            }
        }

        // Flight lift: engage at/below the top immediately (descending always
        // re-lifts), graced release past the top (anti-flap), dirty-checked.
        double feetY = player.getY();
        if (BlockPos.containing(player.position()).getY() <= top) {
            flying = true;
            releaseGrace = 0;
        } else if (feetY > top + FLY_RELEASE_ABOVE_TOP) {
            if (++releaseGrace >= FLY_RELEASE_GRACE_TICKS) {
                flying = false;
            }
        } else {
            releaseGrace = 0;
        }
        syncAbilities(player, true, flying, SPOUT_FLY_SPEED);
        player.fallDistance = 0.0F;

        // Anti-sprint-exploit strip (Korra does this every tick).
        player.setSprinting(false);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);

        sprayParticles(player, top);

        if (level.random.nextInt(10) == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
        return true;
    }

    private boolean isSpoutBase(BlockPos pos) {
        if (!level.getFluidState(pos).is(net.minecraft.world.level.material.Fluids.WATER)) {
            return BendingSources.isIce(level, pos);
        }
        return true;
    }

    /**
     * Sends the abilities packet only when something actually changed, plus
     * a heartbeat reconciling against the live flags so external changes
     * (client toggle, commands, other mods) self-heal.
     */
    private void syncAbilities(ServerPlayer player, boolean mayfly, boolean flying, float flySpeed) {
        var live = player.getAbilities();
        boolean heartbeatDue = ++heartbeat >= ABILITIES_HEARTBEAT_TICKS;
        if (heartbeatDue) {
            heartbeat = 0;
        }
        if (!heartbeatDue
                && mayfly == lastMayflySent
                && flying == lastFlyingSent
                && Float.compare(flySpeed, lastFlySpeedSent) == 0
                && live.mayfly == mayfly
                && live.flying == flying
                && Float.compare(live.getFlyingSpeed(), flySpeed) == 0) {
            return;
        }
        live.mayfly = mayfly;
        live.flying = flying;
        live.setFlyingSpeed(flySpeed);
        player.onUpdateAbilities();
        lastMayflySent = mayfly;
        lastFlyingSent = flying;
        lastFlySpeedSent = flySpeed;
    }

    /**
     * Rotating radius-1 ring of thin falling-water temps climbing the column
     * (Korra displayWaterSpiral shape: 20-degree steps every 0.4 blocks;
     * rotation slowed to 0.2/tick so the churn reads as a spin, not a strobe).
     * Returns the wanted cells; the caller diff-updates the single owned map.
     */
    private Set<BlockPos> spiralCells(ServerPlayer player) {
        Set<BlockPos> want = new HashSet<>();
        if (!Config.WATERSPOUT_SPIRAL.get() || base == null) {
            return want;
        }
        double maxHeight = player.getY() - base.getY() - 0.5;
        rotation += 0.2;
        double height = 0.0;
        int i = 0;
        while (height < maxHeight && height <= Config.WATERSPOUT_HEIGHT.get() + 5.0) {
            i += 20;
            height += 0.4;
            double angle = i * Math.PI / 180.0 + rotation;
            BlockPos pos = new BlockPos(
                    (int) Math.floor(base.getX() + 0.5 + Math.cos(angle)), (int) Math.floor(base.getY() + height), (int)
                            Math.floor(base.getZ() + 0.5 + Math.sin(angle)));
            if (BendingSources.isTransparentForBend(level, pos)) {
                want.add(pos.immutable());
            }
        }
        return want;
    }

    /** Splash burst cycling with the spiral angle, throttled like Korra's interval. */
    private void sprayParticles(ServerPlayer player, int top) {
        if (!Config.WATERSPOUT_PARTICLES.get() || base == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastParticleTime < PARTICLE_INTERVAL_MS) {
            return;
        }
        lastParticleTime = now;
        double midY = base.getY() + Math.max(1.0, (top - base.getY()) / 2.0);
        double x = base.getX() + 0.5 + Math.cos(rotation);
        double z = base.getZ() + 0.5 + Math.sin(rotation);
        level.sendParticles(
                BendingTheme.particle(Config.WATERSPOUT_SPRAY_PARTICLE.get(), ParticleTypes.SPLASH),
                x,
                midY,
                z,
                Config.WATERSPOUT_SPRAY_PARTICLE_COUNT.get(),
                0.2,
                0.5,
                0.2,
                0.1);
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : new java.util.ArrayList<>(owned.values())) {
            temp.revert();
        }
        owned.clear();
        if (baseTemp != null) {
            baseTemp.revert();
            baseTemp = null;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) {
            return;
        }
        // Gamemode-aware restore: Creative always keeps mayfly, Spectator
        // keeps flying; otherwise the snapshot. Fly speed restored too.
        boolean mayfly = prevMayfly || player.isCreative() || player.isSpectator();
        player.getAbilities().mayfly = mayfly;
        player.getAbilities().flying = (prevFlying && mayfly) || player.isSpectator();
        player.getAbilities().setFlyingSpeed(prevFlySpeed);
        player.onUpdateAbilities();
    }
}
