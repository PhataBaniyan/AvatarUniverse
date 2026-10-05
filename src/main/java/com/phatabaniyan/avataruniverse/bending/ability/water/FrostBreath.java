package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of JedCore (CozmycDev) {@code FrostBreath}: hold sneak and breathe a
 * freezing beam down the look vector. Each beam step encases nearby entities
 * in an ice cage, slows them, and optionally damages them (players and mobs
 * take separate values); ground around the beam freezes (water to ice, solid
 * ground to snow layers). Frozen blocks melt on their own timers after the
 * breath ends, and the cooldown lands the moment breathing stops.
 */
public class FrostBreath extends BendingAbility {
    public static final String ID = "FrostBreath";

    private static final DustParticleOptions PALE = new DustParticleOptions(new Vector3f(0.86F, 0.86F, 0.86F), 1.0F);
    private static final DustParticleOptions BLUE = new DustParticleOptions(new Vector3f(0.59F, 0.59F, 1.0F), 1.0F);

    private final ServerLevel level;
    private final List<FrozenBlock> frozen = new ArrayList<>();
    private boolean breathing = true;
    private boolean cooledDown;
    private int breathTicks;

    public FrostBreath(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
    }

    /** Hot dry biomes cannot be frozen (JedCore RestrictBiomes). */
    public static boolean isBiomeBlocked(ServerLevel level, BlockPos pos) {
        var biome = level.getBiome(pos).value();
        return biome.getBaseTemperature() >= 1.0F || !biome.hasPrecipitation();
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
        long now = level.getGameTime();
        frozen.removeIf(fb -> {
            if (now >= fb.endTime) {
                fb.temp.revert();
                return true;
            }
            return false;
        });
        if (breathing) {
            if (!player.isShiftKeyDown() || breathTicks >= Config.FROSTBREATH_DURATION_TICKS.get()) {
                breathing = false;
                if (!cooledDown) {
                    cooledDown = true;
                    bending.setCooldown(ID, now + Config.FROSTBREATH_COOLDOWN_TICKS.get());
                }
            } else {
                breathTicks++;
                createBeam(player);
            }
        }
        // Melting state: the instance lingers until every frozen block melts.
        return breathing || !frozen.isEmpty();
    }

    @Override
    public void onRemove() {
        for (FrozenBlock fb : frozen) {
            fb.temp.revert();
        }
        frozen.clear();
    }

    private void createBeam(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getLookAngle().normalize();
        int range = Config.FROSTBREATH_RANGE.get();
        double damageRegion = 1.5;
        for (int i = 1; i <= range; i++) {
            damageRegion += 0.01;
            Vec3 at = eye.add(dir.scale(i));
            BlockPos pos = BlockPos.containing(at);
            if (!BendingSources.isTransparentForBend(level, pos)) {
                return;
            }
            double r = damageRegion;
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(at.x - r, at.y - r, at.z - r, at.x + r, at.y + r, at.z + r),
                    e -> !e.getUUID().equals(owner) && e.isAlive())) {
                freezeCage(entity);
                if (Config.FROSTBREATH_SLOW_ENABLED.get()) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            Config.FROSTBREATH_SLOW_DURATION_TICKS.get(),
                            Config.FROSTBREATH_SLOW_AMPLIFIER.get()));
                }
                if (Config.FROSTBREATH_DAMAGE_ENABLED.get()) {
                    float damage = (entity instanceof ServerPlayer
                                    ? Config.FROSTBREATH_PLAYER_DAMAGE
                                    : Config.FROSTBREATH_MOB_DAMAGE)
                            .get()
                            .floatValue();
                    entity.hurt(level.damageSources().magic(), damage);
                }
            }
            if (Config.FROSTBREATH_SNOW_ENABLED.get()) {
                freezeGround(pos);
            }
            level.sendParticles(
                    BendingTheme.particle(Config.FROSTBREATH_BEAM_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                    at.x,
                    at.y,
                    at.z,
                    Config.FROSTBREATH_BEAM_PARTICLE_COUNT.get(),
                    0.4,
                    0.4,
                    0.4,
                    0.02);
            level.sendParticles(PALE, at.x, at.y, at.z, 1, Math.random(), Math.random(), Math.random(), 0.0);
            level.sendParticles(BLUE, at.x, at.y, at.z, 1, Math.random(), Math.random(), Math.random(), 0.0);
        }
    }

    /** Encase an entity in ice (JedCore cage: three 3x3 cross-planes plus headroom). */
    private void freezeCage(LivingEntity entity) {
        BlockPos base = entity.blockPosition();
        int bX = base.getX();
        int bY = base.getY();
        int bZ = base.getZ();
        Set<BlockPos> cells = new HashSet<>();
        for (int x = bX - 1; x <= bX + 1; x++) {
            for (int y = bY - 1; y <= bY + 1; y++) {
                cells.add(new BlockPos(x, y, bZ));
            }
        }
        for (int y = bY - 1; y <= bY + 2; y++) {
            cells.add(new BlockPos(bX, y, bZ));
        }
        for (int z = bZ - 1; z <= bZ + 1; z++) {
            for (int y = bY - 1; y <= bY + 1; y++) {
                cells.add(new BlockPos(bX, y, z));
            }
        }
        for (int x = bX - 1; x <= bX + 1; x++) {
            for (int z = bZ - 1; z <= bZ + 1; z++) {
                cells.add(new BlockPos(x, bY, z));
            }
        }
        BlockState ice = Blocks.ICE.defaultBlockState();
        for (BlockPos cell : cells) {
            if (BendingSources.isTransparentForBend(level, cell)) {
                updateFrozenBlock(cell, ice, Config.FROSTBREATH_FROST_DURATION_TICKS.get());
            }
        }
    }

    /** Freeze water to ice and dust solid ground with snow around the beam (JedCore radius-2 sphere). */
    private void freezeGround(BlockPos center) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx * dx + dy * dy + dz * dz > 4) {
                        continue;
                    }
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getFluidState(pos).is(Fluids.WATER)) {
                        updateFrozenBlock(
                                pos,
                                Blocks.ICE.defaultBlockState(),
                                Config.FROSTBREATH_FROZEN_WATER_DURATION_TICKS.get());
                        continue;
                    }
                    if (!BendingSources.isTransparentForBend(level, pos)) {
                        continue;
                    }
                    BlockPos below = pos.below();
                    BlockState belowState = level.getBlockState(below);
                    if (!belowState.isFaceSturdy(level, below, Direction.UP)) {
                        continue;
                    }
                    if (belowState.is(Blocks.ICE)) {
                        continue;
                    }
                    updateFrozenBlock(
                            pos, Blocks.SNOW.defaultBlockState(), Config.FROSTBREATH_SNOW_DURATION_TICKS.get());
                }
            }
        }
    }

    private void updateFrozenBlock(BlockPos pos, BlockState replacement, int durationTicks) {
        BlockPos p = pos.immutable();
        long now = level.getGameTime();
        for (FrozenBlock fb : frozen) {
            if (!fb.pos.equals(p)) {
                continue;
            }
            if (fb.state.getBlock() != replacement.getBlock()) {
                fb.temp.revert();
                frozen.remove(fb);
                break;
            }
            fb.endTime = now + durationTicks;
            return;
        }
        if (replacement.is(Blocks.ICE) && level.getBlockState(p).is(Blocks.ICE)) {
            return;
        }
        frozen.add(new FrozenBlock(
                new TempBlock(level, p, replacement, TempBlock.QUIET), replacement, now + durationTicks));
    }

    private static final class FrozenBlock {
        final TempBlock temp;
        final BlockPos pos;
        final BlockState state;
        long endTime;

        FrozenBlock(TempBlock temp, BlockState state, long endTime) {
            this.temp = temp;
            this.pos = temp.pos();
            this.state = state;
            this.endTime = endTime;
        }
    }
}
