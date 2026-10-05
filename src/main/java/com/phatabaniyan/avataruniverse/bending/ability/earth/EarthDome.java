package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Standalone port of the {@code EarthDome} combo
 * (core/.../earthbending/EarthDome.java from the local ProjectKorra-master
 * copy). Click to raise a dome: centered on yourself, or on the living
 * entity you are looking at within 14 blocks (port of the EarthDomeOthers
 * behavior). Two concentric rings of earth pillars, the outer ring one block
 * RaiseEarth pattern. Reference values: Radius 2, Cooldown 10000ms; height
 * raised to 5 here for a taller dome. The dome stands 600t (30s), then every
 * moved cell restores its natural state.
 */
public class EarthDome extends EarthAbility {
    public static final String ID = "EarthDome";
    /** Reference Cooldown 10000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.EARTHDOME_COOLDOWN_MS.get());

    private static final double RADIUS = Config.EARTHDOME_RADIUS.get();
    private static final int HEIGHT = Config.EARTHDOME_HEIGHT.get();
    /** Dome stand time before everything reverts, in server ticks. */
    private static final long STAND_TICKS = Config.msToTicks(Config.EARTHDOME_STAND_MS.get());

    private static final double TARGET_RANGE = Config.EARTHDOME_TARGET_RANGE.get();
    private static final int RINGS = Config.EARTHDOME_RINGS.get();

    private Vec3 center;
    private boolean done;
    private long bornAt;
    private final Set<BlockPos> raised = new HashSet<>();

    public EarthDome(ServerPlayer player) {
        super(player);
        this.bornAt = player.level().getGameTime();
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!done) {
            done = true;
            net.minecraft.world.entity.LivingEntity victim = eyeVictim(TARGET_RANGE, 2.5);
            center = victim != null ? victim.position() : player.position();
            buildDome();
            cool(owner, player, ID, COOLDOWN_TICKS);
        }
        return level.getGameTime() - bornAt < STAND_TICKS;
    }

    private void buildDome() {
        for (int i = 0; i < RINGS; i++) {
            for (double theta = 0; theta < 2 * Math.PI; theta += Math.toRadians(10)) {
                double r = RADIUS + i + level.random.nextDouble() / 3.1;
                BlockPos probe =
                        BlockPos.containing(center.x + Math.cos(theta) * r, center.y, center.z + Math.sin(theta) * r);
                BlockPos base = appropriateBlock(probe);
                if (base == null || raised.contains(base)) {
                    continue;
                }
                raised.add(base);
                moveEarth(base, new Vec3(0, 1, 0), Math.max(1, HEIGHT - i), false);
                BlockState state = level.getBlockState(base.above());
                level.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state.isAir() ? level.getBlockState(base) : state),
                        base.getX() + 0.5,
                        base.getY() + 1.0,
                        base.getZ() + 0.5,
                        4,
                        0.3,
                        0.3,
                        0.3,
                        0.02);
            }
        }
    }

    /**
     * Reference getAppropriateBlock: the topmost real ground cell within 2 of
     * the probe whose above is open. Short grass, flowers, snow and other
     * non-solid plants are seen through (never picked as the base), while
     * grass blocks, dirt, stone and sand all count as ground — so grass never
     * blocks the dome. The earthbendable check also keeps leaves/logs out.
     */
    private BlockPos appropriateBlock(BlockPos probe) {
        for (int dy = 2; dy >= -2; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState cellState = level.getBlockState(cell);
            if (cellState.isAir() || !cellState.isSolid() || !Accretion.isEarthbendable(level, cell)) {
                continue;
            }
            if (!level.getBlockState(cell.above()).isSolid()) {
                return cell.immutable();
            }
        }
        return null;
    }

    @Override
    public void onRemove() {
        revertMovedEarth();
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
