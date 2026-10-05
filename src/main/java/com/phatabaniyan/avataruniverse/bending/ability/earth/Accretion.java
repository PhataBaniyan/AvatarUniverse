package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Faithful port of ProjectAddons {@code Accretion}: sneak to slam the earth
 * and send blocks into the air on pure ballistics, then left-click before
 * they land to shoot them all at one point. Landed lumps become temporary
 * terrain; hits stack slowness. Reference values: Damage 1, Blocks 8,
 * SelectRange 6, RevertTime 400 ticks, Cooldown 200 ticks, ThrowSpeed 1.6.
 */
public class Accretion extends BendingAbility {
    public static final String ID = "Accretion";
    public static final String TAG = "avataruniverse_accretion";

    private final ServerLevel level;
    private final ServerPlayer caster;
    private final double damage;
    private final int blocks;
    private final int selectRange;
    private final long revertTimeTicks;
    private final double throwSpeed;
    private final long startedAt;
    private final long decayAt;
    private final List<UUID> tracker = new ArrayList<>();
    /** Torn holes awaiting restore, keyed by riding lump. */
    private final Map<UUID, TempBlock> homes = new HashMap<>();

    private boolean shot;

    public Accretion(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.caster = player;
        this.damage = Config.ACCRETION_DAMAGE.get();
        this.blocks = Config.ACCRETION_BLOCKS.get();
        this.selectRange = Config.ACCRETION_SELECT_RANGE.get();
        this.revertTimeTicks = Config.ACCRETION_REVERT_TIME_TICKS.get();
        this.throwSpeed = Config.ACCRETION_THROW_SPEED.get();
        this.startedAt = System.currentTimeMillis();
        this.decayAt = this.startedAt + 6_000L;
        spawnLumps();
    }

    private void spawnLumps() {
        BlockPos origin = caster.blockPosition();
        List<BlockPos> candidates = new ArrayList<>();
        for (int dx = -selectRange; dx <= selectRange; dx++) {
            for (int dz = -selectRange; dz <= selectRange; dz++) {
                int r2 = dx * dx + dz * dz;
                if (r2 > selectRange * selectRange) {
                    continue;
                }
                // Highest earthbendable surface per column, so slopes and
                // ledges contribute their real tops instead of a flat band.
                for (int dy = 6; dy >= -4; dy--) {
                    BlockPos pos = origin.offset(dx, dy, dz).immutable();
                    if (!isEarthbendable(level, pos)) {
                        continue;
                    }
                    if (!level.getBlockState(pos.above()).isAir()) {
                        continue;
                    }
                    if (!level.getFluidState(pos).isEmpty()
                            || !level.getBlockState(pos).getFluidState().isEmpty()) {
                        continue;
                    }
                    if (TempBlock.isTemp(level, pos)) {
                        continue;
                    }
                    candidates.add(pos);
                    break;
                }
            }
        }
        Set<BlockPos> picked = new HashSet<>();
        Random rand = new Random(owner.getMostSignificantBits() ^ System.currentTimeMillis());
        while (picked.size() < blocks && !candidates.isEmpty()) {
            BlockPos pick = candidates.remove(rand.nextInt(candidates.size()));
            if (!picked.add(pick)) {
                continue;
            }
            BlockState state = level.getBlockState(pick);
            if (state.isAir()) {
                continue;
            }
            TempBlock hole = new TempBlock(level, pick, Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
            FallingBlockEntity fb = spawnLump(pick, state);
            fb.setDeltaMovement(new Vec3(0, 0.8, 0));
            level.addFreshEntity(fb);
            tracker.add(fb.getUUID());
            homes.put(fb.getUUID(), hole);
        }
    }

    /** Lumps spawn without tearing extra ground (the hole is already tracked). */
    private FallingBlockEntity spawnLump(BlockPos pick, BlockState state) {
        try {
            java.lang.reflect.Constructor<FallingBlockEntity> ctor = FallingBlockEntity.class.getDeclaredConstructor(
                    Level.class, double.class, double.class, double.class, BlockState.class);
            ctor.setAccessible(true);
            // Centered like the source: a corner spawn suffocates inside
            // neighbouring slope blocks and dies on the first tick.
            FallingBlockEntity fb =
                    ctor.newInstance(level, pick.getX() + 0.5, pick.getY() + 0.5, pick.getZ() + 0.5, state);
            fb.addTag(TAG);
            fb.dropItem = false;
            return fb;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEarthbendable(ServerLevel level, BlockPos pos) {
        ServerPlayer p = null;
        return isEarthbendableCheck(level, pos);
    }

    private static boolean isEarthbendableCheck(net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }
        Block b = state.getBlock();
        if (b == Blocks.BEDROCK || b == Blocks.BARRIER) {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container) {
            return false;
        }
        return state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL)
                || state.is(net.minecraft.tags.BlockTags.SAND)
                || state.is(net.minecraft.tags.BlockTags.DIRT);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (caster == null || caster.isRemoved() || caster.isDeadOrDying() || caster.level() != level) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null || !bending.hasElement(BendingElement.EARTH) || !bending.isToggled()) {
            return false;
        }
        for (UUID id : new ArrayList<>(tracker)) {
            if (!(level.getEntity(id) instanceof FallingBlockEntity fb) || !fb.isAlive()) {
                release(id);
                continue;
            }
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, fb.getBlockState()),
                    fb.getX(),
                    fb.getY() + 0.5,
                    fb.getZ(),
                    1,
                    0.1,
                    0.1,
                    0.1,
                    0.0);
            if (shot) {
                AABB box =
                        new AABB(fb.position().subtract(1, 1, 1), fb.position().add(1, 1, 1));
                for (LivingEntity hit : level.getEntitiesOfClass(LivingEntity.class, box)) {
                    if (!hit.getUUID().equals(owner) && hit.isAlive()) {
                        entityCollision(hit);
                        fb.discard();
                        release(id);
                        break;
                    }
                }
            }
        }
        if (tracker.isEmpty()) {
            return false;
        }
        if (System.currentTimeMillis() >= decayAt) {
            cleanupLumps();
            return false;
        }
        return true;
    }

    /** Drop a lump: discard it and heal its torn hole if still open. */
    private void release(UUID id) {
        if (level.getEntity(id) instanceof FallingBlockEntity fb) {
            fb.discard();
        }
        tracker.remove(id);
        TempBlock home = homes.remove(id);
        if (home != null && home.isAirReplacement()) {
            home.revert();
        }
    }

    private void cleanupLumps() {
        for (UUID id : new ArrayList<>(tracker)) {
            if (level.getEntity(id) instanceof FallingBlockEntity fb) {
                fb.discard();
            }
            tracker.remove(id);
            TempBlock home = homes.remove(id);
            if (home != null && home.isAirReplacement()) {
                home.revert();
            }
        }
    }

    public void shoot() {
        if (shot) {
            return;
        }
        if (tracker.isEmpty()) {
            return;
        }
        for (UUID id : tracker) {
            if (!(level.getEntity(id) instanceof FallingBlockEntity fb)) {
                continue;
            }
            Vec3 target = targetedLocation();
            if (target == null) {
                return;
            }
            Vec3 dir = target.subtract(fb.position())
                    .normalize()
                    .add(0, 0.185, 0)
                    .normalize()
                    .scale(throwSpeed);
            fb.setDeltaMovement(dir);
            fb.hurtMarked = true;
        }
        shot = true;
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.ACCRETION_COOLDOWN_TICKS.get());
        }
        caster.displayClientMessage(net.minecraft.network.chat.Component.literal("Accretion launched!"), true);
    }

    /** Each block that hits adds slowness time and level, like the source. */
    public void entityCollision(LivingEntity entity) {
        int duration = 20;
        int amp = 1;
        MobEffectInstance current = entity.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        if (current != null) {
            duration += current.getDuration();
            amp += current.getAmplifier();
            entity.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amp, true, false));
        entity.hurt(caster.damageSources().playerAttack(caster), (float) damage);
    }

    private Vec3 targetedLocation() {
        LivingEntity e = targetedEntity();
        if (e != null) {
            return e.position();
        }
        return targetedLocationRay();
    }

    private LivingEntity targetedEntity() {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        for (double d = 0.5; d <= 30.0; d += 0.5) {
            Vec3 p = eye.add(look.scale(d));
            for (LivingEntity e :
                    level.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(1.5), LivingEntity::isAlive)) {
                if (!e.getUUID().equals(owner)) {
                    return e;
                }
            }
        }
        return null;
    }

    private Vec3 targetedLocationRay() {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        for (double d = 0.5; d <= 30.0; d += 0.5) {
            Vec3 p = eye.add(look.scale(d));
            BlockPos pos = BlockPos.containing(p);
            if (!level.getBlockState(pos).isAir()) {
                return p;
            }
        }
        return eye.add(look.scale(30));
    }

    /** Landed lumps become timed terrain instead of permanent grief. */
    public static void land(ServerLevel level, BlockPos pos, BlockState placed, FallingBlockEntity fb) {
        if (fb == null || !fb.getTags().contains(TAG)) {
            return;
        }
        fb.discard();
        TempBlock existing = TempBlock.getAt(level, pos);
        if (existing != null) {
            existing.updateReplacement(placed);
            BendingManager.scheduleRevert(existing, level.getGameTime() + Config.ACCRETION_REVERT_TIME_TICKS.get());
            return;
        }
        TempBlock temp = new TempBlock(level, pos, placed, TempBlock.QUIET);
        BendingManager.scheduleRevert(temp, level.getGameTime() + Config.ACCRETION_REVERT_TIME_TICKS.get());
    }
}
