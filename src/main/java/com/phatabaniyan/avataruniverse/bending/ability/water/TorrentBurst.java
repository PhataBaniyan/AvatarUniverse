package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code TorrentWave} (waterbending/TorrentWave.java):
 * the radial burst released from a formed torrent ring. An expanding ring
 * of real water grows to max radius, knocking each entity back once
 * radially (Korra's wave deals knockback, no damage), then reverts. Ends
 * via {@link #onRemove()} like everything else: no leaked blocks.
 */
public class TorrentBurst extends BendingAbility {
    public static final String ID = "TorrentBurst";

    private final ServerLevel level;
    private final BlockPos center;
    private final List<TempBlock> ring = new ArrayList<>();
    private final Set<UUID> affected = new HashSet<>();
    /** Cells that struck wall: never re-filled, like the source angles. */
    private final Set<BlockPos> blocked = new HashSet<>();

    private double radius = 1.0;

    public TorrentBurst(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.center = BlockPos.containing(player.getEyePosition());
        level.playSound(null, center, SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.9F, 0.8F);
        com.phatabaniyan.avataruniverse.bending.BendingPlayer bending =
                com.phatabaniyan.avataruniverse.bending.BendingPlayer.get(player.getUUID());
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.msToTicks(Config.TORRENTBURST_COOLDOWN_MS.get()));
        }
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (radius > Config.TORRENT_BURST_MAX_RADIUS.get()) {
            return false;
        }
        // Diff-updated expanding wall (Korra formBurst rebuilds; diffing the
        // same shape strobes nothing): only entering/leaving cells churn.
        // Rushing water, never a source, so rapids never glass over inside it.
        java.util.Set<BlockPos> want = new java.util.HashSet<>();
        int bound = (int) Math.ceil(radius) + 1;
        for (int dx = -bound; dx <= bound; dx++) {
            for (int dz = -bound; dz <= bound; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (Math.abs(dist - radius) > 0.6) {
                    continue;
                }
                for (int dy = -1; dy <= Config.TORRENT_WAVE_HEIGHT.get(); dy++) {
                    BlockPos pos = center.offset(dx, dy, dz).immutable();
                    if (blocked.contains(pos)) {
                        continue;
                    }
                    if (!BendingSources.isTransparentForBend(level, pos)) {
                        blocked.add(pos);
                        continue;
                    }
                    want.add(pos);
                }
            }
        }
        for (TempBlock temp : new java.util.ArrayList<>(ring)) {
            if (!want.contains(temp.pos())) {
                temp.revert();
                ring.remove(temp);
            }
        }
        java.util.Set<BlockPos> held = new java.util.HashSet<>();
        for (TempBlock temp : ring) {
            held.add(temp.pos());
        }
        for (BlockPos pos : want) {
            if (!held.contains(pos)) {
                ring.add(TempBlock.cube(level, pos, 0.25F));
                held.add(pos);
            }
        }
        affected.clear();
        Vec3 centerVec = Vec3.atCenterOf(center);
        double knockback = Config.TORRENT_BURST_KNOCKBACK.get();
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        centerVec.subtract(radius + 2.0, 3.0, radius + 2.0),
                        centerVec.add(radius + 2.0, 3.0, radius + 2.0)),
                e -> !e.getUUID().equals(owner) && e.isAlive())) {
            if (!affected.add(entity.getUUID())) {
                continue;
            }
            boolean nearWall = false;
            for (TempBlock temp : ring) {
                if (entity.position().distanceToSqr(Vec3.atCenterOf(temp.pos())) <= 4.0) {
                    nearWall = true;
                    break;
                }
            }
            if (!nearWall) {
                continue;
            }
            Vec3 away = entity.position().subtract(centerVec);
            away = new Vec3(away.x, 0.0, away.z);
            if (away.lengthSqr() < 1.0e-4) {
                away = new Vec3(0.0, 0.0, 1.0);
            }
            away = away.normalize();
            entity.setDeltaMovement(entity.getDeltaMovement().add(away.scale(knockback)));
            entity.hurtMarked = true;
        }
        Vec3 spray = Vec3.atCenterOf(center).add(0.0, 1.0, 0.0);
        level.sendParticles(
                BendingTheme.particle(Config.TORRENTBURST_SPRAY_PARTICLE.get(), ParticleTypes.SPLASH),
                spray.x,
                spray.y,
                spray.z,
                Config.TORRENTBURST_SPRAY_PARTICLE_COUNT.get(),
                radius * 0.5,
                0.5,
                radius * 0.5,
                0.08);
        radius += Config.TORRENT_BURST_GROW.get();
        return true;
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : ring) {
            temp.revert();
        }
        ring.clear();
    }
}
