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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code LavaFlux}
 * (jedcore/.../earthbending/LavaFlux.java): click to send a wave of lava
 * racing forward from 2 blocks ahead, 3 wide, up to 12 long. The head
 * sprays lava particles and burns what it touches for 1 damage; an extra
 * splash rides above it. Afterwards everything cools to stone and heals.
 * Reference values: Range 12, Cooldown 8000ms, Duration 4000ms, Cleanup
 * 1000ms, Damage 1, Speed 1, Wave on. Reference gates on lavabending;
 * here any earthbender may use it.
 */
public class LavaFlux extends EarthAbility {
    public static final String ID = "LavaFlux";

    /** Reference Cooldown 8000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.LAVAFLUX_COOLDOWN_MS.get());
    /** Reference Duration 4000ms of hold, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.LAVAFLUX_DURATION_MS.get());
    /** Reference Cleanup 1000ms, in server ticks. */
    private static final long CLEANUP_TICKS = Config.msToTicks(Config.LAVAFLUX_CLEANUP_MS.get());

    private static final int RANGE = Config.LAVAFLUX_RANGE.get();
    private static final float DAMAGE = Config.LAVAFLUX_DAMAGE.get().floatValue();
    private static final long SPLASH_REVERT_TICKS = Config.msToTicks(Config.LAVAFLUX_SPLASH_REVERT_MS.get());
    private static final double HIT_RADIUS = Config.LAVAFLUX_HIT_RADIUS.get();
    private static final int FIRE_TICKS = Config.msToTicks(Config.LAVAFLUX_FIRE_MS.get());
    private static final double KNOCKUP = Config.LAVAFLUX_KNOCKUP.get();

    private final List<BlockPos> flux = new ArrayList<>();
    private final List<TempBlock> lava = new ArrayList<>();
    private final long bornTick;
    private int step;
    private int counter;
    private boolean complete;
    private boolean valid;

    public LavaFlux(ServerPlayer player) {
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

    /** Click-start gate: bendable ground two ahead to surge from. */
    public static boolean canBegin(ServerPlayer player) {
        return firstGround(player) != null;
    }

    private static BlockPos firstGround(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) {
            return null;
        }
        flat = flat.normalize();
        BlockPos probe = BlockPos.containing(player.getX() + flat.x * 2, player.getY(), player.getZ() + flat.z * 2);
        return snap(player, probe);
    }

    /** Topmost bendable ground with open headroom near a probe. */
    private static BlockPos snap(ServerPlayer player, BlockPos probe) {
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            if (cell.getY() <= player.serverLevel().getMinBuildHeight()) {
                continue;
            }
            BlockState state = player.serverLevel().getBlockState(cell);
            BlockState cap = player.serverLevel().getBlockState(cell.above());
            if (state.isAir() || cap.isSolid() || !cap.getFluidState().isEmpty()) {
                continue;
            }
            if (Accretion.isEarthbendable(player.serverLevel(), cell)) {
                return cell.immutable();
            }
        }
        return null;
    }

    private boolean prepare() {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) {
            return false;
        }
        flat = flat.normalize();
        double ax = Math.abs(flat.x);
        double az = Math.abs(flat.z);
        Vec3 step = ax >= az ? new Vec3(Math.signum(flat.x), 0, 0) : new Vec3(0, 0, Math.signum(flat.z));
        if (step.lengthSqr() < 1.0e-6) {
            return false;
        }
        Vec3 side = new Vec3(-step.z, 0, step.x);
        BlockPos origin = firstGround(player);
        if (origin == null) {
            return false;
        }
        for (int i = 0; i < RANGE; i++) {
            BlockPos probe = origin.offset((int) Math.round(step.x) * i, 0, (int) Math.round(step.z) * i);
            boolean stop = false;
            for (int s = -1; s <= 1; s++) {
                BlockPos lateral = probe.offset((int) Math.round(side.x * s), 0, (int) Math.round(side.z * s));
                if (!level.getFluidState(lateral).isEmpty()) {
                    stop = true;
                    break;
                }
                BlockPos ground = snap(player, lateral);
                if (ground == null) {
                    stop = true;
                    break;
                }
                flux.add(ground);
            }
            if (stop) {
                break;
            }
        }
        return !flux.isEmpty();
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !valid) {
            return false;
        }
        if (!complete) {
            counter++;
            for (int i = 0; i < 3; i++) {
                step++;
                flowStep();
            }
            if (step >= flux.size()) {
                complete = true;
            }
            return true;
        }
        if (level.getGameTime() - bornTick > DURATION_TICKS) {
            seal();
            return false;
        }
        return true;
    }

    /** Convert everything up to the head, spraying and burning at the tip. */
    private void flowStep() {
        for (int i = 0; i < flux.size() && i <= step; i++) {
            BlockPos cell = flux.get(i);
            if (!TempBlock.isTemp(level, cell)) {
                lava.add(new TempBlock(level, cell, Blocks.LAVA.defaultBlockState(), TempBlock.QUIET));
            }
            if (i == step) {
                level.sendParticles(
                        BendingTheme.particle(Config.LAVAFLUX_FLOW_PARTICLE.get(), ParticleTypes.LAVA),
                        cell.getX() + 0.5,
                        cell.getY() + 1.0,
                        cell.getZ() + 0.5,
                        Config.LAVAFLUX_FLOW_PARTICLE_COUNT.get(),
                        0.3,
                        0.3,
                        0.3,
                        0.0);
                BlockPos above = cell.above();
                if (level.getBlockState(above).isAir() && !TempBlock.isTemp(level, above)) {
                    TempBlock splash = new TempBlock(level, above, Blocks.LAVA.defaultBlockState(), TempBlock.QUIET);
                    lava.add(splash);
                    BendingManager.scheduleRevert(splash, level.getGameTime() + SPLASH_REVERT_TICKS);
                }
                for (LivingEntity entity : level.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(
                                Vec3.atCenterOf(cell).subtract(HIT_RADIUS, HIT_RADIUS, HIT_RADIUS),
                                Vec3.atCenterOf(cell).add(HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)),
                        LivingEntity::isAlive)) {
                    if (entity.getUUID().equals(owner)) {
                        continue;
                    }
                    entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
                    entity.setRemainingFireTicks(FIRE_TICKS);
                    Vec3 away = entity.position().subtract(player.position());
                    away = new Vec3(away.x, 0, away.z);
                    if (away.lengthSqr() < 1.0e-6) {
                        away = new Vec3(0, 0, 1);
                    }
                    away = away.normalize();
                    entity.setDeltaMovement(new Vec3(away.x, KNOCKUP, away.z));
                    entity.hurtMarked = true;
                }
            }
        }
    }

    /** Cool every lava cell to stone on a short timer, then let go. */
    private void seal() {
        for (TempBlock temp : new ArrayList<>(lava)) {
            BlockPos pos = temp.pos();
            temp.revert();
            TempBlock stone = new TempBlock(level, pos, Blocks.STONE.defaultBlockState(), TempBlock.QUIET);
            BendingManager.scheduleRevert(stone, level.getGameTime() + CLEANUP_TICKS + level.random.nextInt(20));
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
