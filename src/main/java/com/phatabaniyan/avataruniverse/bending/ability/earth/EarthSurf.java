package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * EarthSurf wave rider. The wave is a carpet of visible
 * {@link FallingBlockEntity} riders steered with real physics, 5 long in
 * the ride direction by 3 wide, the center ridge floating highest. The
 * AirScooter-style drive holds height over the floor.
 */
public class EarthSurf extends EarthAbility {
    public static final String ID = "EarthSurf";
    public static final String TAG = "avataruniverse_earthsurf";

    private static final int LUMP_COUNT = 5;
    /** Wave profile along the ride direction: 0.25 / 0.5 / 0.75 / 0.5 / 0.25. */
    private static final double[] LUMP_OFFSETS = {-2.0, -1.0, 0.0, 1.0, 2.0};

    private static final double[] LUMP_HEIGHTS = {0.25, 0.5, 0.75, 0.5, 0.25};
    /** Lateral columns: a 3-wide carpet. */
    private static final double[] LANE_OFFSETS = {-1.0, 0.0, 1.0};
    /** AirScooter reference values: speed, stall-check interval, floor ceiling, cooldown. */
    private static final double RIDE_SPEED = Config.EARTHSURF_RIDE_SPEED.get();

    private static final long STALL_CHECK_TICKS = Config.msToTicks(Config.EARTHSURF_STALL_CHECK_MS.get());
    private static final int MAX_HEIGHT_FROM_GROUND = Config.EARTHSURF_MAX_HEIGHT.get();
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.EARTHSURF_COOLDOWN_MS.get());

    private final Map<Integer, Lump> lumps = new HashMap<>();
    private final long startGameTime;
    private BlockPos floorBlock;

    public EarthSurf(ServerPlayer player) {
        super(player);
        this.startGameTime = player.level().getGameTime();
    }

    /**
     * Sprint + jump + left-click gate (AirScooter trigger): must be sprinting,
     * airborne, and have bendable ground reachable below.
     */
    public static boolean canBegin(ServerPlayer player) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.hasElement(BendingElement.EARTH)) {
            return false;
        }
        if (!player.isSprinting()) {
            return false;
        }
        if (!levelBlockBelow(player, 1).isAir()) {
            return false;
        }
        BlockPos feet = player.blockPosition();
        for (int dy = 1; dy <= MAX_HEIGHT_FROM_GROUND; dy++) {
            if (Accretion.isEarthbendable(player.serverLevel(), feet.below(dy))) {
                return true;
            }
        }
        return false;
    }

    private static BlockState levelBlockBelow(ServerPlayer player, int down) {
        BlockPos pos = player.blockPosition().below(down);
        return player.serverLevel().getBlockState(pos);
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
        getFloor();
        if (floorBlock == null) {
            return false;
        }
        if (player.isShiftKeyDown()) {
            return false;
        }
        updateWave();
        driveScooter();
        return true;
    }

    /** Scan down from the eyes for the first solid floor, like AirScooter. */
    private void getFloor() {
        floorBlock = null;
        Vec3 eye = player.getEyePosition();
        for (int i = 0; i <= MAX_HEIGHT_FROM_GROUND; i++) {
            BlockPos pos = BlockPos.containing(eye.x, eye.y - i, eye.z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() || !state.getFluidState().isEmpty()) {
                floorBlock = pos.immutable();
                return;
            }
        }
    }

    /**
     * AirScooter drive ported to earth: gaze-speed velocity, stall cutout
     * after the interval, height band hold around the floor, step-up hop and
     * gap dip ahead. No particles: the wave is the block displays.
     */
    private void driveScooter() {
        Vec3 velocity = player.getLookAngle().normalize().scale(RIDE_SPEED);
        if (level.getGameTime() > startGameTime + STALL_CHECK_TICKS) {
            if (player.getDeltaMovement().length() < RIDE_SPEED * 0.3) {
                BendingManager.remove(this);
                return;
            }
        }

        double distance = player.position().y - floorBlock.getY();
        double vertical;
        if (distance > 2.75) {
            vertical = -0.25;
        } else if (distance < 2.0) {
            vertical = 0.25;
        } else {
            vertical = 0.0;
        }

        Vec3 flat = new Vec3(velocity.x, 0, velocity.z).normalize();
        BlockPos ahead = BlockPos.containing(
                floorBlock.getX() + 0.5 + flat.x * 1.2,
                floorBlock.getY() + 0.5,
                floorBlock.getZ() + 0.5 + flat.z * 1.2);
        BlockState aheadState = level.getBlockState(ahead);
        boolean aheadSolid = !aheadState.isAir() && aheadState.getFluidState().isEmpty();
        if (!aheadSolid && aheadState.getFluidState().isEmpty()) {
            vertical -= 0.1;
        } else if (!level.getBlockState(ahead.above()).isAir()
                || !level.getBlockState(ahead.above()).getFluidState().isEmpty()) {
            vertical += 0.7;
        }

        player.setDeltaMovement(new Vec3(velocity.x, vertical, velocity.z));
        player.hurtMarked = true;
    }

    private void updateWave() {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 forward = new Vec3(look.x, 0, look.z);
        if (forward.lengthSqr() < 1.0e-6) {
            forward = new Vec3(0, 0, 1);
        } else {
            forward = forward.normalize();
        }
        Vec3 side = new Vec3(-forward.z, 0, forward.x);
        Vec3 feet = player.position();

        for (int i = 0; i < LUMP_COUNT; i++) {
            for (int j = 0; j < LANE_OFFSETS.length; j++) {
                int key = i * LANE_OFFSETS.length + j;
                Vec3 slot = feet.add(forward.scale(LUMP_OFFSETS[i])).add(side.scale(LANE_OFFSETS[j]));
                GroundSample sample = findGround(slot);
                Lump lump = lumps.get(key);
                if (sample == null) {
                    if (lump != null) {
                        discardLump(lump);
                        lumps.remove(key);
                    }
                    continue;
                }
                if (lump == null || !fbAlive(lump)) {
                    if (lump != null) {
                        discardLump(lump);
                    }
                    lumps.put(key, spawnLump(sample, LUMP_HEIGHTS[i]));
                    continue;
                }
                steerLump(lump, sample.spawn.add(0, LUMP_HEIGHTS[i], 0));
            }
        }
    }

    private GroundSample findGround(Vec3 anchor) {
        int x = (int) Math.floor(anchor.x);
        int z = (int) Math.floor(anchor.z);
        int top = (int) Math.floor(anchor.y) + 2;
        int bottom = (int) Math.floor(anchor.y) - 4;
        for (int y = top; y >= bottom; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                continue;
            }
            if (!Accretion.isEarthbendable(level, pos)) {
                continue;
            }
            BlockPos spawn = pos.above();
            if (!level.getBlockState(spawn).isAir()) {
                continue;
            }
            return new GroundSample(new Vec3(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5), state);
        }
        return null;
    }
    /**
     * Spawn one visible rider without tearing the ground: pure-position
     * constructor, so the wave leaves no holes behind.
     */
    private Lump spawnLump(GroundSample sample, double height) {
        FallingBlockEntity fb = spawnRider(sample);
        fb.dropItem = false;
        fb.addTag(TAG);
        fb.setDeltaMovement(new Vec3(0, 0.25, 0));
        fb.hurtMarked = true;
        level.addFreshEntity(fb);
        return new Lump(fb.getUUID());
    }

    private FallingBlockEntity spawnRider(GroundSample sample) {
        try {
            java.lang.reflect.Constructor<FallingBlockEntity> ctor = FallingBlockEntity.class.getDeclaredConstructor(
                    Level.class, double.class, double.class, double.class, BlockState.class);
            ctor.setAccessible(true);
            return ctor.newInstance(level, sample.spawn.x - 0.5, sample.spawn.y, sample.spawn.z - 0.5, sample.state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean fbAlive(Lump lump) {
        return level.getEntity(lump.fb) instanceof FallingBlockEntity fb && fb.isAlive();
    }

    private void steerLump(Lump lump, Vec3 target) {
        if (!(level.getEntity(lump.fb) instanceof FallingBlockEntity fb)) {
            return;
        }
        Vec3 to = target.subtract(fb.position());
        Vec3 velocity = to.scale(0.28).add(new Vec3(0, 0.07, 0));
        if (velocity.length() > 1.2) {
            velocity = velocity.normalize().scale(1.2);
        }
        fb.setDeltaMovement(velocity);
        fb.hurtMarked = true;
    }

    private void discardLump(Lump lump) {
        if (level.getEntity(lump.fb) instanceof FallingBlockEntity fb) {
            fb.discard();
        }
    }

    @Override
    public void onRemove() {
        for (Lump lump : lumps.values()) {
            discardLump(lump);
        }
        lumps.clear();
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }

    private static final class Lump {
        private final UUID fb;

        private Lump(UUID fb) {
            this.fb = fb;
        }
    }

    private static final class GroundSample {
        private final Vec3 spawn;
        private final BlockState state;

        private GroundSample(Vec3 spawn, BlockState state) {
            this.spawn = spawn;
            this.state = state;
        }
    }
}
