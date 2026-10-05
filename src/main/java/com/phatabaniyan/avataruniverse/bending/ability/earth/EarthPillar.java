package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code EarthPillar}
 * (jedcore/.../earthbending/EarthPillar.java): sneak at an earthbendable
 * surface within 10 to push a pillar out of the clicked face, one block
 * per tick up to 6, capped by the bendable chain behind it. Sneak at a
 * standing pillar again to lay it back down. The pillar persists after
 * building (tracked cells transfer to a static record, so the builder's
 * own revert leaves it alone). No cooldown, like the reference.
 */
public class EarthPillar extends EarthAbility {
    public static final String ID = "EarthPillar";

    private static final int HEIGHT = Config.EARTHPILLAR_HEIGHT.get();
    private static final double RANGE = Config.EARTHPILLAR_RANGE.get();

    /** Standing pillars: every moved cell back to its record. */
    private static final Map<BlockPos, Pillar> PILLAR_BY_CELL = new HashMap<>();

    private static final class Pillar {
        final Map<BlockPos, BlockState> cells = new LinkedHashMap<>();
    }

    private BlockPos head;
    private Vec3 faceDir = new Vec3(0, 1, 0);
    private int height = HEIGHT;
    private int step;
    private boolean valid;
    private boolean captured;
    private final long bornTick;
    /** Ticks to let the aim settle before locking the source. */
    private static final long AIM_SETTLE_TICKS = Config.msToTicks(Config.EARTHPILLAR_AIM_SETTLE_MS.get());

    private static final double TOGGLE_RANGE = Config.EARTHPILLAR_TOGGLE_RANGE.get();

    public EarthPillar(ServerPlayer player) {
        super(player);
        this.bornTick = player.level().getGameTime();
        valid = true;
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: bendable surface or a standing pillar in sight. */
    public static boolean canBegin(ServerPlayer player) {
        return findHit(player) != null || targetedPillar(player) != null;
    }

    /** First solid cell along the gaze plus the air cell entered from, like Bukkit's two-target-blocks. */
    private static SourceHit findHit(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        BlockPos prevAir = null;
        boolean seenAir = false;
        for (double d = 0.5; d <= RANGE; d += 0.1) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            BlockState state = player.serverLevel().getBlockState(pos);
            if (state.isAir() || isSoftVegetation(state)) {
                if (!pos.equals(prevAir)) {
                    prevAir = pos.immutable();
                }
                seenAir = true;
                continue;
            }
            if (PILLAR_BY_CELL.containsKey(pos)) {
                if (!pos.equals(prevAir)) {
                    prevAir = pos.immutable();
                }
                continue;
            }
            if (!Accretion.isEarthbendable(player.serverLevel(), pos)) {
                return null;
            }
            Vec3 face;
            if (seenAir && prevAir != null && !prevAir.equals(pos)) {
                face = dominantAxis(new Vec3(
                        prevAir.getX() - pos.getX(), prevAir.getY() - pos.getY(), prevAir.getZ() - pos.getZ()));
            } else {
                face = dominantAxis(new Vec3(-look.x, -look.y, -look.z));
            }
            return new SourceHit(pos.immutable(), face);
        }
        return null;
    }

    private static final class SourceHit {
        final BlockPos hit;
        final Vec3 face;

        SourceHit(BlockPos hit, Vec3 face) {
            this.hit = hit;
            this.face = face;
        }
    }

    /** Snap to the dominant axis, keeping the sign. */
    private static Vec3 dominantAxis(Vec3 v) {
        double ax = Math.abs(v.x);
        double ay = Math.abs(v.y);
        double az = Math.abs(v.z);
        if (ax >= ay && ax >= az) {
            return new Vec3(Math.signum(v.x), 0, 0);
        } else if (ay >= ax && ay >= az) {
            return new Vec3(0, Math.signum(v.y), 0);
        }
        return new Vec3(0, 0, Math.signum(v.z));
    }

    /** Grass tufts, flowers, snow and leaves never block a source ray. */
    private static boolean isSoftVegetation(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (!state.isSolid()) {
            return true;
        }
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        return key.contains("LEAVES")
                || key.contains("SHORT_GRASS")
                || key.contains("TALL_GRASS")
                || key.contains("FERN")
                || key.contains("FLOWER")
                || key.contains("VINE")
                || key.contains("SNOW");
    }

    /**
     * A standing pillar only toggles when it is the first solid hit close
     * up: farther pillars never swallow builds aimed past them.
     */
    private static Pillar targetedPillar(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= RANGE; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            BlockState state = player.serverLevel().getBlockState(pos);
            if (state.isAir() || isSoftVegetation(state)) {
                continue;
            }
            if (d > TOGGLE_RANGE) {
                return null;
            }
            return PILLAR_BY_CELL.get(pos);
        }
        return null;
    }

    /** Second sneak on a standing pillar lays it back down, all at once. */
    private boolean sinkWhole(Pillar standing) {
        List<BlockPos> order = new ArrayList<>(standing.cells.keySet());
        java.util.Collections.reverse(order);
        for (BlockPos pos : order) {
            try {
                level.setBlock(pos, standing.cells.get(pos), 2);
            } catch (RuntimeException ignored) {
                // A concurrent revert must never take the whole ability down.
            }
            PILLAR_BY_CELL.remove(pos);
        }
        Vec3 at = player.getEyePosition().add(player.getLookAngle().normalize().scale(3));
        level.playSound(null, at.x, at.y, at.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.7F, 1.0F);
        return false;
    }

    private boolean prepare(ServerPlayer player) {
        SourceHit source = findHit(player);
        if (source == null || PILLAR_BY_CELL.containsKey(source.hit)) {
            return false;
        }
        BlockPos hit = source.hit;
        faceDir = source.face;
        height = 0;
        for (int i = 0; i < HEIGHT; i++) {
            BlockPos cell = hit.offset(
                    (int) -Math.round(faceDir.x) * i,
                    (int) -Math.round(faceDir.y) * i,
                    (int) -Math.round(faceDir.z) * i);
            if (!Accretion.isEarthbendable(level, cell)) {
                break;
            }
            height++;
        }
        if (height == 0) {
            return false;
        }
        head = hit.immutable();
        return true;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !valid) {
            return false;
        }
        if (!captured) {
            if (level.getGameTime() - bornTick < AIM_SETTLE_TICKS) {
                return true;
            }
            Pillar standing = targetedPillar(player);
            if (standing != null) {
                return sinkWhole(standing);
            }
            if (!prepare(player)) {
                return false;
            }
            captured = true;
        }
        if (step < height) {
            step++;
            moveEarth(head, faceDir, height, true);
            head = head.offset((int) Math.round(faceDir.x), (int) Math.round(faceDir.y), (int) Math.round(faceDir.z));
            return true;
        }
        Pillar pillar = new Pillar();
        for (Map.Entry<BlockPos, BlockState> entry : movedEarth.entrySet()) {
            BlockPos pos = entry.getKey().immutable();
            pillar.cells.putIfAbsent(pos, entry.getValue());
            PILLAR_BY_CELL.putIfAbsent(pos, pillar);
        }
        movedEarth.clear();
        return false;
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
}
