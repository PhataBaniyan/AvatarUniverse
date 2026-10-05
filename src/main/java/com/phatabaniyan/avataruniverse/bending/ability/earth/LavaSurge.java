package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code LavaSurge}
 * (addons/.../ability/earth/LavaSurge.java): sneak at earthbendable ground
 * within 5 to gather up to 10 blocks into a seething source (magma flash,
 * then live lava), sneak again to retarget, click once ready to hurl small
 * magma cubes one per tick down the gaze with a tight jitter. Touches deal
 * 0.5 with multi-hit pacing and 3s of burn; shards skim the terrain and
 * burst out at the end of their arc. Afterwards the source craters to air
 * for 3s, then the earth returns. Reference values: Cooldown 4000ms,
 * Damage 0.5, Speed 1.14, SelectRange 5, SourceRadius 3, MaxBlocks 10,
 * Burn 3s. Reference gates on lavabending; here any earthbender may use
 * it.
 */
public class LavaSurge extends EarthAbility {
    public static final String ID = "LavaSurge";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.LAVASURGE_COOLDOWN_MS.get());

    private static final float DAMAGE = Config.LAVASURGE_DAMAGE.get().floatValue();
    private static final double SPEED = Config.LAVASURGE_SPEED.get();
    private static final double SELECT_RANGE = Config.LAVASURGE_SELECT_RANGE.get();
    private static final double SOURCE_RADIUS = Config.LAVASURGE_SOURCE_RADIUS.get();
    /** Reference MaxBlocks 10. */
    private static final int MAX_BLOCKS = Config.LAVASURGE_MAX_BLOCKS.get();

    private static final int BURN_TICKS = Config.msToTicks(Config.LAVASURGE_BURN_MS.get());
    /** Magma flash before the source goes live, in ticks. */
    private static final long FLASH_TICKS = Config.msToTicks(Config.LAVASURGE_FLASH_MS.get());
    /** Shard lifetime, in ticks (reference 4s). */
    private static final long SHARD_LIFE = Config.msToTicks(Config.LAVASURGE_SHARD_LIFE_MS.get());

    private static final long CRATER_REVERT_TICKS = Config.msToTicks(Config.LAVASURGE_CRATER_REVERT_MS.get());

    private static final double HIT_RADIUS = Config.LAVASURGE_HIT_RADIUS.get();
    /** Half-size cubes. */
    private static final float SIZE = 0.5F;

    private final Map<BlockPos, TempBlock> source = new HashMap<>();
    private final Set<BlockPos> live = new HashSet<>();
    private final List<Shard> shards = new ArrayList<>();
    private final long bornTick;
    private Vec3 sourceCenter;
    private Vec3 direction;
    private boolean shot;
    private boolean gathered;
    private int launched;
    private boolean valid;

    private static final class Shard {
        final UUID id;
        final Vec3 origin;
        final Vec3 velocity;
        final long born;

        Shard(UUID id, Vec3 origin, Vec3 velocity, long born) {
            this.id = id;
            this.origin = origin;
            this.velocity = velocity;
            this.born = born;
        }
    }

    public LavaSurge(ServerPlayer player) {
        super(player);
        this.bornTick = player.level().getGameTime();
        this.valid = prepare();
        this.gathered = valid;
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: earthbendable ground in sight to gather. */
    public static boolean canBegin(ServerPlayer player) {
        BlockPos target = eyeTarget(player, SELECT_RANGE);
        return target != null && Accretion.isEarthbendable(player.serverLevel(), target);
    }

    private static BlockPos eyeTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!player.serverLevel().getBlockState(pos).isAir()) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** Gather up to 10 earthbendable cells around the targeted block. */
    private boolean prepare() {
        BlockPos target = eyeTarget(player, SELECT_RANGE);
        if (target == null) {
            return false;
        }
        sourceCenter = Vec3.atCenterOf(target);
        Set<BlockPos> total = new HashSet<>();
        for (double r = 1.0; r <= SOURCE_RADIUS && total.size() < MAX_BLOCKS; r += 0.5) {
            int bound = (int) Math.ceil(r);
            for (int x = -bound; x <= bound && total.size() < MAX_BLOCKS; x++) {
                for (int y = -bound; y <= bound && total.size() < MAX_BLOCKS; y++) {
                    for (int z = -bound; z <= bound && total.size() < MAX_BLOCKS; z++) {
                        if (x * x + y * y + z * z > r * r) {
                            continue;
                        }
                        BlockPos cell = target.offset(x, y, z).immutable();
                        if (total.contains(cell) || TempBlock.isTemp(level, cell)) {
                            continue;
                        }
                        if (Accretion.isEarthbendable(level, cell)) {
                            total.add(cell);
                        }
                    }
                }
            }
        }
        if (total.isEmpty()) {
            return false;
        }
        for (BlockPos cell : total) {
            source.put(cell, new TempBlock(level, cell, Blocks.MAGMA_BLOCK.defaultBlockState(), TempBlock.QUIET));
        }
        return true;
    }

    /** Sneak again before the shot: drop this source and gather anew. */
    public boolean retarget() {
        for (TempBlock temp : new ArrayList<>(source.values())) {
            temp.revert();
        }
        source.clear();
        live.clear();
        shot = false;
        launched = 0;
        valid = prepare();
        gathered = gathered || valid;
        return valid;
    }

    public boolean hasShot() {
        return shot;
    }

    public boolean ready() {
        if (source.isEmpty()) {
            return false;
        }
        return live.containsAll(source.keySet());
    }

    /** Click path: loose the volley down the frozen gaze. */
    public void shoot() {
        if (shot || !ready()) {
            return;
        }
        Vec3 look = player.getLookAngle().normalize();
        direction = new Vec3(look.x, look.y, look.z);
        shot = true;
        level.playSound(
                null,
                sourceCenter.x,
                sourceCenter.y,
                sourceCenter.z,
                SoundEvents.LAVA_POP,
                SoundSource.PLAYERS,
                0.8F,
                1.0F);
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !valid) {
            return false;
        }
        long now = level.getGameTime();
        long age = now - bornTick;
        if (!shot) {
            if (age >= FLASH_TICKS) {
                for (Map.Entry<BlockPos, TempBlock> entry : new ArrayList<>(source.entrySet())) {
                    if (!live.contains(entry.getKey())) {
                        entry.getValue().revert();
                        TempBlock lava =
                                new TempBlock(level, entry.getKey(), Blocks.LAVA.defaultBlockState(), TempBlock.QUIET);
                        entry.setValue(lava);
                        live.add(entry.getKey());
                    }
                }
            } else if (now % 5 == 0) {
                for (BlockPos cell : source.keySet()) {
                    level.sendParticles(
                            BendingTheme.particle(Config.LAVASURGE_SOURCE_PARTICLE.get(), ParticleTypes.LAVA),
                            cell.getX() + 0.5,
                            cell.getY() + 1.0,
                            cell.getZ() + 0.5,
                            Config.LAVASURGE_SOURCE_PARTICLE_COUNT.get(),
                            0.2,
                            0.2,
                            0.2,
                            0.0);
                }
            }
            return true;
        }
        if (launched < MAX_BLOCKS && launched < source.size()) {
            Vec3 jitter = new Vec3((level.random.nextDouble() - 0.5) / 4, 0.07, (level.random.nextDouble() - 0.5) / 4);
            Vec3 velocity = direction.add(jitter).normalize().scale(SPEED);
            Vec3 from = new Vec3(sourceCenter.x, sourceCenter.y + 0.7, sourceCenter.z);
            net.minecraft.world.entity.Display.BlockDisplay disp = new net.minecraft.world.entity.Display.BlockDisplay(
                    net.minecraft.world.entity.EntityType.BLOCK_DISPLAY, level);
            setDisplayState(disp, Blocks.MAGMA_BLOCK.defaultBlockState());
            setSize(disp);
            disp.setPos(from.x - 0.25, from.y - 0.25, from.z - 0.25);
            level.addFreshEntity(disp);
            shards.add(new Shard(disp.getUUID(), from, velocity, now));
            launched++;
        }
        for (Shard shard : new ArrayList<>(shards)) {
            if (!(level.getEntity(shard.id) instanceof net.minecraft.world.entity.Display.BlockDisplay disp)) {
                shards.remove(shard);
                continue;
            }
            long lived = now - shard.born;
            if (lived > SHARD_LIFE) {
                burst(new Vec3(disp.getX() + 0.25, disp.getY() + 0.25, disp.getZ() + 0.25));
                disp.discard();
                shards.remove(shard);
                continue;
            }
            Vec3 raw = shard.origin.add(shard.velocity.scale(lived)).add(0, -0.02 * lived * lived, 0);
            double y = Math.max(raw.y, skimHeight(raw));
            disp.setPos(raw.x - 0.25, y - 0.25, raw.z - 0.25);
            Vec3 at = new Vec3(raw.x, y, raw.z);
            if (lived % 4 == 0) {
                level.sendParticles(
                        BendingTheme.particle(Config.LAVASURGE_TRAIL_PARTICLE.get(), ParticleTypes.LAVA),
                        at.x,
                        at.y,
                        at.z,
                        Config.LAVASURGE_TRAIL_PARTICLE_COUNT.get(),
                        0.1,
                        0.1,
                        0.1,
                        0.0);
            }
            boolean spent = false;
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(at.subtract(1, 1, 1), at.add(1, 1, 1)).inflate(HIT_RADIUS),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                if (entity.position().distanceToSqr(at) > HIT_RADIUS * HIT_RADIUS
                        && entity.getEyePosition().distanceToSqr(at) > HIT_RADIUS * HIT_RADIUS) {
                    continue;
                }
                entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
                entity.invulnerableTime = 0;
                entity.setRemainingFireTicks(BURN_TICKS);
                spent = true;
                break;
            }
            if (spent) {
                burst(at);
                disp.discard();
                shards.remove(shard);
            }
        }
        if (launched >= Math.min(MAX_BLOCKS, source.size()) && shards.isEmpty()) {
            return false;
        }
        return true;
    }

    /** Glide height: skim the soil so arcs never tunnel underground. */
    private double skimHeight(Vec3 at) {
        BlockPos probe = BlockPos.containing(at.x, at.y, at.z);
        for (int dy = 4; dy >= -6; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = level.getBlockState(cell);
            if (!state.isAir() && (state.isSolid() || Accretion.isEarthbendable(level, cell))) {
                return cell.getY() + 1.0 + 0.3;
            }
        }
        return at.y;
    }

    private void burst(Vec3 at) {
        level.sendParticles(
                BendingTheme.particle(Config.LAVASURGE_BURST_PARTICLE.get(), ParticleTypes.LAVA),
                at.x,
                at.y,
                at.z,
                Config.LAVASURGE_BURST_PARTICLE_COUNT.get(),
                0.2,
                0.2,
                0.2,
                0.02);
    }

    private static void setDisplayState(net.minecraft.world.entity.Display.BlockDisplay disp, BlockState state) {
        try {
            java.lang.reflect.Method method = net.minecraft.world.entity.Display.BlockDisplay.class.getDeclaredMethod(
                    "setBlockState", BlockState.class);
            method.setAccessible(true);
            method.invoke(disp, state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setSize(net.minecraft.world.entity.Display.BlockDisplay disp) {
        try {
            java.lang.reflect.Method method = net.minecraft.world.entity.Display.class.getDeclaredMethod(
                    "setTransformation", com.mojang.math.Transformation.class);
            method.setAccessible(true);
            method.invoke(
                    disp,
                    new com.mojang.math.Transformation(
                            new org.joml.Vector3f(0, 0, 0),
                            new org.joml.Quaternionf(),
                            new org.joml.Vector3f(SIZE, SIZE, SIZE),
                            new org.joml.Quaternionf()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRemove() {
        for (Shard shard : new ArrayList<>(shards)) {
            if (level.getEntity(shard.id) instanceof net.minecraft.world.entity.Display.BlockDisplay disp) {
                disp.discard();
            }
        }
        shards.clear();
        for (TempBlock temp : new ArrayList<>(source.values())) {
            temp.revert();
        }
        source.clear();
        // Source craters to air for 3s, then the earth returns.
        for (BlockPos cell : new ArrayList<>(live)) {
            TempBlock hole = new TempBlock(level, cell, Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
            BendingManager.scheduleRevert(hole, level.getGameTime() + CRATER_REVERT_TICKS);
        }
        live.clear();
        if (gathered) {
            cool(owner, player, ID, COOLDOWN_TICKS);
        }
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
