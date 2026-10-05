package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code IceWall}: click facing water, ice or snow to raise
 * an arched ice wall that climbs one layer per tick and then stands until
 * clicked down. Collapsing it shatters ice outward, hurting whoever stands
 * too close. Ability-collision damage hooks and wall sourcing are cut with
 * the reference's shared combat systems (all ice stays a source here anyway).
 */
public class IceWall extends BendingAbility {
    public static final String ID = "IceWall";

    private static final Set<IceWall> INSTANCES = ConcurrentHashMap.newKeySet();

    private final ServerLevel level;
    private final long startTick;
    private final List<BlockPos> affected = new ArrayList<>();
    private final List<BlockPos> lastBlocks = new ArrayList<>();
    private final Map<BlockPos, TempBlock> temps = new HashMap<>();
    private boolean rising = true;
    private boolean done;

    public IceWall(ServerPlayer player, List<BlockPos> affected) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.startTick = player.level().getGameTime();
        this.affected.addAll(affected);
        for (BlockPos pos : affected) {
            if (!affected.contains(pos.below())) {
                lastBlocks.add(pos);
            }
        }
        INSTANCES.add(this);
    }

    /** Eye-ray water/ice/snow within range (Korra source march). */
    public static BlockPos findSource(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!level.getFluidState(pos).isEmpty() || BendingSources.isIce(level, pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** Arch columns from a source (Korra loadAffectedBlocks, heights ramp 3-5-3). */
    public static List<BlockPos> planWall(ServerPlayer player, BlockPos origin) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 across = new Vec3(-look.z, 0.0, look.x);
        if (across.lengthSqr() < 1e-6) {
            across = new Vec3(1.0, 0.0, 0.0);
        }
        across = across.normalize();
        int width = Config.ICEWALL_WIDTH.get();
        int minHeight = Config.ICEWALL_MIN_HEIGHT.get();
        int maxHeight = Config.ICEWALL_MAX_HEIGHT.get();
        List<BlockPos> out = new ArrayList<>();
        Set<IceWall> live = new HashSet<>(INSTANCES);
        int height = minHeight;
        boolean increasing = true;
        int half = width / 2;
        for (int i = -half; i <= half; i++) {
            if (width % 2 == 0 && i == half) {
                continue;
            }
            BlockPos column = BlockPos.containing(Vec3.atCenterOf(origin).add(across.scale(i)));
            while (level.getBlockState(column).isAir() && column.getY() > level.getMinBuildHeight()) {
                column = column.below();
            }
            while (!level.getBlockState(column.above()).isAir() && column.getY() < level.getMaxBuildHeight()) {
                column = column.above();
            }
            boolean taken = false;
            for (IceWall wall : live) {
                if (wall.affected.contains(column)) {
                    taken = true;
                    break;
                }
            }
            if (taken) {
                continue;
            }
            if (BendingSources.isWaterSource(level, column) || BendingSources.isIce(level, column)) {
                out.add(column.immutable());
                for (int h = 1; h <= height; h++) {
                    BlockPos up = column.above(h);
                    if (level.getBlockState(up).isAir()) {
                        out.add(up.immutable());
                    }
                }
                if (height < maxHeight && increasing) {
                    height++;
                }
                if (i == 0) {
                    increasing = false;
                }
                if (!increasing && height > minHeight) {
                    height--;
                }
            }
        }
        return out;
    }

    /** Clicked block belongs to this wall (manual demolish). */
    public boolean owns(BlockPos pos) {
        return affected.contains(pos);
    }

    /** Demolish whichever live wall owns the clicked cell, if any. */
    public static boolean collapseAt(ServerPlayer player, BlockPos pos) {
        for (IceWall wall : new HashSet<>(INSTANCES)) {
            if (wall.owns(pos.immutable())) {
                wall.collapse(player, false);
                return true;
            }
        }
        return false;
    }

    /** Demolish (Korra collapse): shatter outward, hurting the careless nearby. */
    public void collapse(ServerPlayer player, boolean forceful) {
        if (done) {
            return;
        }
        done = true;
        for (TempBlock temp : new ArrayList<>(temps.values())) {
            Vec3 c = Vec3.atCenterOf(temp.pos());
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                    c.x,
                    c.y + 0.5,
                    c.z,
                    5,
                    0.0,
                    0.0,
                    0.0,
                    0.0);
            temp.revert();
        }
        temps.clear();
        level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.5F);
        double damageRadius = Config.ICEWALL_DAMAGE_RADIUS.get();
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(damageRadius * 2.0), LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            boolean near = false;
            for (BlockPos pos : affected) {
                if (entity.position().distanceToSqr(Vec3.atCenterOf(pos)) <= damageRadius * damageRadius) {
                    near = true;
                    break;
                }
            }
            if (near) {
                if (forceful) {
                    entity.invulnerableTime = 0;
                }
                entity.hurt(
                        level.damageSources().magic(),
                        Config.ICEWALL_DAMAGE.get().floatValue());
            }
        }
        INSTANCES.remove(this);
        BendingManager.remove(this);
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
        if (done) {
            return false;
        }
        if (Config.ICEWALL_LIFETIME_ENABLED.get()
                && level.getGameTime() - startTick > Config.msToTicks(Config.ICEWALL_LIFETIME_MS.get())) {
            collapse(player, false);
            return false;
        }
        if (rising) {
            if (lastBlocks.isEmpty()) {
                rising = false;
                return true;
            }
            List<BlockPos> layer = new ArrayList<>(lastBlocks);
            lastBlocks.clear();
            for (BlockPos pos : layer) {
                if (!temps.containsKey(pos) && !TempBlock.isTemp(level, pos)) {
                    temps.put(pos, new TempBlock(level, pos, Blocks.ICE.defaultBlockState(), TempBlock.QUIET));
                    level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.3F, 1.8F);
                }
                BlockPos up = pos.above();
                if (affected.contains(up)) {
                    lastBlocks.add(up);
                }
            }
        }
        return true;
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : new java.util.ArrayList<>(temps.values())) {
            temp.revert();
        }
        temps.clear();
        INSTANCES.remove(this);
    }
}
