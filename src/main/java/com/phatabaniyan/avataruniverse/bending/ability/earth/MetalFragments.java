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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of Cozmyc JedCore {@code MetalFragments}: sneak at metal within 5
 * to pop up to 3 sources hovering at eye height; click to fling an ingot
 * from a random source down the gaze at speed 2, 4 damage on touch. Each
 * source holds 10 fragments, then drops. Reference values: Cooldown
 * 5000ms, MaxSources 3, SourceRange 5, MaxFragments 10, Damage 4,
 * Velocity 2. Reference gates on metalbending; here any earthbender may
 * use it.
 */
public class MetalFragments extends EarthAbility {
    public static final String ID = "MetalFragments";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.METALFRAGMENTS_COOLDOWN_TICKS.get();

    private static final int MAX_SOURCES = Config.METALFRAGMENTS_MAX_SOURCES.get();
    private static final double SOURCE_RANGE = Config.METALFRAGMENTS_SOURCE_RANGE.get();
    private static final int MAX_FRAGMENTS = Config.METALFRAGMENTS_MAX_FRAGMENTS.get();
    private static final float DAMAGE = Config.METALFRAGMENTS_DAMAGE.get().floatValue();
    private static final double VELOCITY = Config.METALFRAGMENTS_VELOCITY.get();
    private static final double AIM_RANGE = Config.METALFRAGMENTS_AIM_RANGE.get();
    private static final double LEASH_RADIUS = Config.METALFRAGMENTS_LEASH_RADIUS.get();

    /** Hovering sources in select order. */
    private final List<BlockPos> sources = new ArrayList<>();
    /** Hover states by cell, for clean drops. */
    private final Map<BlockPos, BlockState> hoverStates = new HashMap<>();
    /** Hover temps by cell, for revert. */
    private final Map<BlockPos, TempBlock> hoverTemps = new HashMap<>();
    /** Rising blocks still climbing to hover height. */
    private final Map<UUID, BlockPos> rising = new HashMap<>();
    /** Source holes (temp air). */
    private final Map<BlockPos, TempBlock> holes = new HashMap<>();
    /** Fragments spent per source. */
    private final Map<BlockPos, Integer> counters = new HashMap<>();
    /** Flying ingots. */
    private final List<UUID> fragments = new ArrayList<>();

    public MetalFragments(ServerPlayer player) {
        super(player);
        select();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: metal in sight to pop. */
    public static boolean canBegin(ServerPlayer player) {
        return findSource(player) != null;
    }

    private static BlockPos findSource(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= SOURCE_RANGE; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            BlockState state = player.serverLevel().getBlockState(pos);
            if (state.isAir() || TempBlock.isTemp(player.serverLevel(), pos)) {
                continue;
            }
            if (!isMetalState(state)) {
                return null;
            }
            return pos.immutable();
        }
        return null;
    }

    /** Sneak again: pop one more source, up to the maximum. */
    public void select() {
        if (sources.size() + rising.size() >= MAX_SOURCES) {
            return;
        }
        BlockPos source = findSource(player);
        if (source == null) {
            return;
        }
        for (BlockPos already : sources) {
            if (already.getX() == source.getX() && already.getZ() == source.getZ()) {
                return;
            }
        }
        if (!level.getBlockState(source.above()).isAir()) {
            return;
        }
        BlockState state = level.getBlockState(source);
        TempBlock hole = new TempBlock(level, source, Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
        holes.put(source, hole);
        FallingBlockEntity fb = spawnRiser(Vec3.atCenterOf(source).add(0, 0.5, 0), state);
        fb.setDeltaMovement(new Vec3(0, 0.8, 0));
        level.addFreshEntity(fb);
        rising.put(fb.getUUID(), source);
        level.playSound(
                null,
                source.getX(),
                source.getY(),
                source.getZ(),
                SoundEvents.ANVIL_USE,
                SoundSource.PLAYERS,
                0.5F,
                1.4F);
    }

    /** Click path: fling one fragment from a random hovering source. */
    public void shoot() {
        if (sources.isEmpty()) {
            return;
        }
        BlockPos source = sources.get(level.random.nextInt(sources.size()));
        ItemStack ammo = fragmentFor(level.getBlockState(source));
        Vec3 from = Vec3.atCenterOf(source).add(0, 0.5, 0);
        LivingEntity victim = eyeVictim(AIM_RANGE, 2.5);
        Vec3 dest = victim != null ? victim.position() : eyeTargetPoint();
        Vec3 direction = dest.subtract(from).normalize();
        ItemEntity shot = new ItemEntity(level, from.x, from.y, from.z, ammo);
        shot.setDeltaMovement(direction.scale(VELOCITY));
        level.addFreshEntity(shot);
        fragments.add(shot.getUUID());
        int count = counters.getOrDefault(source, 0) + 1;
        if (count >= MAX_FRAGMENTS) {
            counters.remove(source);
            sources.remove(source);
            dropSource(source);
            level.playSound(null, from.x, from.y, from.z, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8F, 1.5F);
        } else {
            counters.put(source, count);
        }
    }

    private static ItemStack fragmentFor(BlockState state) {
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        if (key.contains("GOLD")) {
            return new ItemStack(Items.GOLD_INGOT);
        }
        if (key.contains("COAL")) {
            return new ItemStack(Items.COAL);
        }
        return new ItemStack(Items.IRON_INGOT);
    }

    private Vec3 eyeTargetPoint() {
        BlockPos hit = eyeTarget(AIM_RANGE);
        if (hit != null) {
            return Vec3.atCenterOf(hit);
        }
        Vec3 eye = player.getEyePosition();
        return eye.add(player.getLookAngle().normalize().scale(AIM_RANGE));
    }

    /** Drop a spent source back to the world and heal its hole. */
    private void dropSource(BlockPos source) {
        BlockState state = hoverStates.remove(source);
        TempBlock hover = hoverTemps.remove(source);
        if (hover == null) {
            hover = TempBlock.getAt(level, source);
        }
        if (hover != null) {
            hover.revert();
        }
        if (state != null && !state.isAir()) {
            FallingBlockEntity fb = spawnRiser(Vec3.atCenterOf(source).add(0, 0.5, 0), state);
            fb.setDeltaMovement(new Vec3(0, 0.2, 0));
            level.addFreshEntity(fb);
        }
        TempBlock hole = holes.remove(source);
        if (hole != null) {
            hole.revert();
        }
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        for (Map.Entry<UUID, BlockPos> entry : new ArrayList<>(rising.entrySet())) {
            BlockPos hole = entry.getValue();
            if (!(level.getEntity(entry.getKey()) instanceof FallingBlockEntity fb) || !fb.isAlive()) {
                rising.remove(entry.getKey());
                forgetHole(hole);
                continue;
            }
            if (fb.position().y >= player.getEyePosition().y + 1.0) {
                fb.discard();
                rising.remove(entry.getKey());
                BlockPos hover = BlockPos.containing(fb.position());
                if (!TempBlock.isTemp(level, hover)) {
                    hoverTemps.put(hover, new TempBlock(level, hover, fb.getBlockState(), TempBlock.QUIET));
                }
                hoverStates.put(hover, fb.getBlockState());
                if (!sources.contains(hover)) {
                    sources.add(hover);
                    counters.put(hover, 0);
                }
            }
        }
        for (UUID id : new ArrayList<>(fragments)) {
            if (!(level.getEntity(id) instanceof ItemEntity shot) || !shot.isAlive()) {
                fragments.remove(id);
                continue;
            }
            if (shot.onGround()) {
                burst(shot.position(), shot.getItem());
                shot.discard();
                fragments.remove(id);
                continue;
            }
            Vec3 at = shot.position();
            boolean spent = false;
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class, new AABB(at.subtract(1, 1, 1), at.add(1, 1, 1)), LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                if (entity.position().distanceToSqr(at) > 1.0
                        && entity.getEyePosition().distanceToSqr(at) > 1.0) {
                    continue;
                }
                entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
                spent = true;
                break;
            }
            if (spent) {
                burst(at, shot.getItem());
                shot.discard();
                fragments.remove(id);
            }
        }
        for (Map.Entry<BlockPos, Integer> entry : new ArrayList<>(counters.entrySet())) {
            if (player.position().distanceToSqr(Vec3.atCenterOf(entry.getKey())) > LEASH_RADIUS * LEASH_RADIUS) {
                dropSource(entry.getKey());
                sources.remove(entry.getKey());
                counters.remove(entry.getKey());
            }
        }
        if (sources.isEmpty() && rising.isEmpty() && fragments.isEmpty()) {
            return false;
        }
        return true;
    }

    private void burst(Vec3 at, ItemStack ammo) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()),
                at.x,
                at.y + 0.3,
                at.z,
                3,
                0.3,
                0.3,
                0.3,
                0.2);
    }

    /** Risers spawn without tearing extra ground (holes already tracked). */
    private FallingBlockEntity spawnRiser(Vec3 at, BlockState state) {
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
        TempBlock temp = holes.remove(hole);
        if (temp != null) {
            temp.revert();
        }
    }

    @Override
    public void onRemove() {
        for (UUID id : new ArrayList<>(rising.keySet())) {
            if (level.getEntity(id) instanceof FallingBlockEntity fb) {
                fb.discard();
            }
        }
        rising.clear();
        for (UUID id : new ArrayList<>(fragments)) {
            if (level.getEntity(id) instanceof ItemEntity shot) {
                shot.discard();
            }
        }
        fragments.clear();
        for (TempBlock temp : new ArrayList<>(holes.values())) {
            temp.revert();
        }
        holes.clear();
        for (TempBlock temp : new ArrayList<>(hoverTemps.values())) {
            temp.revert();
        }
        hoverTemps.clear();
        hoverStates.clear();
        sources.clear();
        counters.clear();
        cool(owner, player, ID, COOLDOWN_TICKS);
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
