package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code EarthShard}
 * (jedcore/.../earthbending/EarthShard.java): sneak at earthbendable
 * ground within 5 to pop a block up (up to 3, each a fresh column); it
 * floats to 2 above its hole and hovers ready. Sneak again to select
 * more. Click once all hover to hurl them down the gaze at speed 2, 1
 * damage (1.5 for metal) with multi-hit pacing. Shards that land burst
 * and never place; holes return when it ends. Reference values: Cooldown
 * 1000ms, Damage Normal 1 / Metal 1.5, PrepareRange 5, AbilityRange 30,
 * MaxShards 3, EntityCollisionRadius 1.4.
 */
public class EarthShard extends EarthAbility {
    public static final String ID = "EarthShard";
    /** Landing guard tag: shards burst, never place. */
    public static final String TAG = "avataruniverse_earthshard";

    /** Reference Cooldown 1000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.EARTHSHARD_COOLDOWN_MS.get());

    private static final float NORMAL_DAMAGE =
            Config.EARTHSHARD_NORMAL_DAMAGE.get().floatValue();
    private static final float METAL_DAMAGE =
            Config.EARTHSHARD_METAL_DAMAGE.get().floatValue();
    private static final double PREPARE_RANGE = Config.EARTHSHARD_PREPARE_RANGE.get();
    private static final double ABILITY_RANGE = Config.EARTHSHARD_ABILITY_RANGE.get();
    private static final int MAX_SHARDS = Config.EARTHSHARD_MAX_SHARDS.get();
    private static final double HIT_RADIUS = Config.EARTHSHARD_HIT_RADIUS.get();
    private static final double THROW_SPEED = Config.EARTHSHARD_THROW_SPEED.get();
    private static final int HEADROOM = Config.EARTHSHARD_HEADROOM.get();
    private static final double HOVER_HEIGHT = Config.EARTHSHARD_HOVER_HEIGHT.get();
    private static final double RISE_SPEED = Config.EARTHSHARD_RISE_SPEED.get();
    private static final double KNOCKUP_RADIUS = Config.EARTHSHARD_KNOCKUP_RADIUS.get();
    private static final double KNOCKUP_POWER = Config.EARTHSHARD_KNOCKUP_POWER.get();

    /** Selected holes (temp air) in select order. */
    private final List<BlockPos> selected = new ArrayList<>();
    /** Hovering ready blocks. */
    private final Map<BlockPos, TempBlock> ready = new HashMap<>();
    /** Rising blocks still climbing to hover height. */
    private final Map<UUID, BlockPos> rising = new HashMap<>();
    /** Flying shards after the throw. */
    private final List<Shard> shards = new ArrayList<>();

    private boolean thrown;

    private static final class Shard {
        final UUID id;
        final BlockState state;

        Shard(UUID id, BlockState state) {
            this.id = id;
            this.state = state;
        }
    }

    public EarthShard(ServerPlayer player) {
        super(player);
        select();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: earthbendable ground in sight with headroom. */
    public static boolean canBegin(ServerPlayer player) {
        return findSource(player) != null;
    }

    private static BlockPos findSource(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= PREPARE_RANGE; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (player.serverLevel().getBlockState(pos).isAir() || TempBlock.isTemp(player.serverLevel(), pos)) {
                continue;
            }
            if (!Accretion.isEarthbendable(player.serverLevel(), pos)) {
                return null;
            }
            return pos.immutable();
        }
        return null;
    }

    /** Sneak again before the throw: select one more source column. */
    public void select() {
        if (thrown || selected.size() >= MAX_SHARDS) {
            return;
        }
        BlockPos source = findSource(player);
        if (source == null || TempBlock.isTemp(level, source)) {
            return;
        }
        for (BlockPos already : selected) {
            if (already.getX() == source.getX() && already.getZ() == source.getZ()) {
                return;
            }
        }
        for (int i = 1; i <= HEADROOM; i++) {
            if (!level.getBlockState(source.above(i)).isAir()) {
                return;
            }
        }
        BlockState state = level.getBlockState(source);
        BlockState flying = stabilize(state);
        if (isMetalState(state)) {
            level.playSound(
                    null,
                    source.getX(),
                    source.getY(),
                    source.getZ(),
                    SoundEvents.ANVIL_USE,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.4F);
        } else {
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    source.getX() + 0.5,
                    source.getY() + 1.0,
                    source.getZ() + 0.5,
                    20,
                    0.0,
                    0.0,
                    0.0,
                    0.0);
            level.playSound(
                    null,
                    source.getX(),
                    source.getY(),
                    source.getZ(),
                    SoundEvents.STONE_BREAK,
                    SoundSource.PLAYERS,
                    0.7F,
                    1.0F);
        }
        selected.add(source);
        TempBlock hole = new TempBlock(
                level, source, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
        holes().put(source, hole);
        FallingBlockEntity fb = spawnShard(Vec3.atCenterOf(source).add(0, 0.5, 0), flying);
        fb.setDeltaMovement(new Vec3(0, RISE_SPEED, 0));
        level.addFreshEntity(fb);
        rising.put(fb.getUUID(), source);
        knockup(Vec3.atCenterOf(source));
    }

    /** Cozmyc select pop: nearby entities (including the caster) bounce up. */
    private void knockup(Vec3 at) {
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        at.subtract(KNOCKUP_RADIUS, KNOCKUP_RADIUS, KNOCKUP_RADIUS),
                        at.add(KNOCKUP_RADIUS, KNOCKUP_RADIUS, KNOCKUP_RADIUS)),
                LivingEntity::isAlive)) {
            if (entity.position().distanceToSqr(at) > KNOCKUP_RADIUS * KNOCKUP_RADIUS) {
                continue;
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, KNOCKUP_POWER, 0));
            entity.hurtMarked = true;
        }
    }

    private final Map<BlockPos, TempBlock> holeTemps = new HashMap<>();

    private Map<BlockPos, TempBlock> holes() {
        return holeTemps;
    }

    public boolean hasShot() {
        return thrown;
    }

    /** Click path: hurl every hovering block down the gaze. Returns a status line unless fired. */
    public String throwShards() {
        if (thrown) {
            return null;
        }
        if (selected.isEmpty()) {
            return "Sneak at earth to select shards first.";
        }
        if (ready.size() < selected.size()) {
            return "Gathering shards... (" + ready.size() + "/" + selected.size() + ")";
        }
        LivingEntity victim = eyeVictim(ABILITY_RANGE, 2.5);
        Vec3 dest = victim != null ? victim.position() : eyeTargetPoint();
        for (Map.Entry<BlockPos, TempBlock> entry : new ArrayList<>(ready.entrySet())) {
            BlockPos cell = entry.getKey();
            BlockState state = level.getBlockState(cell);
            entry.getValue().revert();
            FallingBlockEntity fb = spawnShard(Vec3.atCenterOf(cell), state);
            Vec3 vel = dest.subtract(Vec3.atCenterOf(cell))
                    .normalize()
                    .scale(THROW_SPEED)
                    .add(0, 0.2, 0);
            fb.setDeltaMovement(vel);
            fb.addTag(TAG);
            level.addFreshEntity(fb);
            shards.add(new Shard(fb.getUUID(), state));
        }
        ready.clear();
        thrown = true;
        cool(owner, player, ID, COOLDOWN_TICKS);
        return null;
    }

    private Vec3 eyeTargetPoint() {
        BlockPos hit = eyeTarget(ABILITY_RANGE);
        if (hit != null) {
            return Vec3.atCenterOf(hit);
        }
        Vec3 eye = player.getEyePosition();
        return eye.add(player.getLookAngle().normalize().scale(ABILITY_RANGE));
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!thrown && !ID.equalsIgnoreCase(activeBound())) {
            return false;
        }
        if (!thrown) {
            if (selected.isEmpty()) {
                return false;
            }
            for (Map.Entry<UUID, BlockPos> entry : new ArrayList<>(rising.entrySet())) {
                BlockPos hole = entry.getValue();
                if (!(level.getEntity(entry.getKey()) instanceof FallingBlockEntity fb) || !fb.isAlive()) {
                    rising.remove(entry.getKey());
                    forgetHole(hole);
                    selected.remove(hole);
                    continue;
                }
                if (fb.position().y >= hole.getY() + HOVER_HEIGHT) {
                    fb.discard();
                    rising.remove(entry.getKey());
                    BlockPos hover = BlockPos.containing(fb.position());
                    ready.put(hover, new TempBlock(level, hover, fb.getBlockState(), TempBlock.QUIET));
                }
            }
            return true;
        }
        for (Shard shard : new ArrayList<>(shards)) {
            if (!(level.getEntity(shard.id) instanceof FallingBlockEntity fb) || !fb.isAlive()) {
                shards.remove(shard);
                continue;
            }
            if (fb.onGround()) {
                burst(fb.position());
                fb.discard();
                shards.remove(shard);
                continue;
            }
            Vec3 at = fb.position();
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
                entity.hurt(
                        player.damageSources().playerAttack(player),
                        isMetalState(shard.state) ? METAL_DAMAGE : NORMAL_DAMAGE);
                entity.invulnerableTime = 0;
                spent = true;
                break;
            }
            if (spent) {
                burst(at);
                fb.discard();
                shards.remove(shard);
            }
        }
        return !shards.isEmpty();
    }

    private void burst(Vec3 at) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                at.x,
                at.y + 0.5,
                at.z,
                4,
                0.2,
                0.2,
                0.2,
                0.02);
    }

    /** Shards spawn without tearing extra ground (holes already tracked). */
    private FallingBlockEntity spawnShard(Vec3 at, BlockState state) {
        try {
            java.lang.reflect.Constructor<FallingBlockEntity> ctor = FallingBlockEntity.class.getDeclaredConstructor(
                    Level.class, double.class, double.class, double.class, BlockState.class);
            ctor.setAccessible(true);
            return ctor.newInstance(level, at.x - 0.5, at.y - 0.5, at.z - 0.5, state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void forgetHole(BlockPos hole) {
        TempBlock temp = holes().remove(hole);
        if (temp != null) {
            temp.revert();
        }
        selected.remove(hole);
    }

    @Override
    public void onRemove() {
        for (UUID id : new ArrayList<>(rising.keySet())) {
            if (level.getEntity(id) instanceof FallingBlockEntity fb) {
                fb.discard();
            }
        }
        rising.clear();
        for (Shard shard : new ArrayList<>(shards)) {
            if (level.getEntity(shard.id) instanceof FallingBlockEntity fb) {
                fb.discard();
            }
        }
        shards.clear();
        for (TempBlock temp : new ArrayList<>(ready.values())) {
            temp.revert();
        }
        ready.clear();
        for (TempBlock temp : new ArrayList<>(holes().values())) {
            temp.revert();
        }
        holes().clear();
        selected.clear();
    }

    private String activeBound() {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending == null ? "" : bending.boundAbility(player.getInventory().selected + 1);
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
