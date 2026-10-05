package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code Electrify} (ProjectAddons port).
 * Click water or metal to electrify it. Each block is its own instance with
 * a static electrified set, spreading once to all 6 faces. Victims in the
 * block take water damage; everyone gets 10-tick slowness + weakness +
 * jump-lock. Firebenders skip the debuffs but not the water damage.
 * Reference values: Cooldown 5000ms (100 ticks), Duration 8000ms
 * (160 ticks), Range 12, WaterDamage 3.
 */
public class Electrify extends BendingAbility {
    public static final String ID = "Electrify";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ELECTRIFY_COOLDOWN_MS.get());
    /** Reference Duration 8000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.ELECTRIFY_DURATION_MS.get());

    private static final double RANGE = Config.ELECTRIFY_RANGE.get();
    private static final double WATER_DAMAGE = Config.ELECTRIFY_WATER_DAMAGE.get();
    private static final int SPREAD_DEPTH = Config.ELECTRIFY_SPREAD_DEPTH.get();
    private static final int DEBUFF_TICKS = Config.msToTicks(Config.ELECTRIFY_DEBUFF_MS.get());
    private static final double HIT_RADIUS = Config.ELECTRIFY_HIT_RADIUS.get();

    private static final Set<BlockPos> ELECTRIFIED = ConcurrentHashMap.newKeySet();

    private final ServerPlayer player;
    private final ServerLevel level;
    private BlockPos block;
    private Vec3 center;
    private int spread;
    private int ticks = 0;

    public Electrify(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();

        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        BlockPos found = null;
        for (double d = 0; d <= RANGE; d += 0.5) {
            BlockPos bp = BlockPos.containing(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            if (!level.isLoaded(bp)) {
                break;
            }
            var state = level.getBlockState(bp);
            if (isMetal(state) || !state.getFluidState().isEmpty()) {
                found = bp.immutable();
                break;
            }
            if (state.isSolidRender(level, bp)) {
                break;
            }
        }
        // Invalid target: leave center null so the first progress tick ends
        // the ability immediately (constructor cannot remove, not started yet).
        if (found == null || !ELECTRIFIED.add(found)) {
            return;
        }
        this.block = found;
        this.center = new Vec3(found.getX() + 0.5, found.getY() + 0.5, found.getZ() + 0.5);
        this.spread = SPREAD_DEPTH;
        cool(owner, level, ID, COOLDOWN_TICKS);
        level.playSound(
                null,
                this.center.x,
                this.center.y,
                this.center.z,
                SoundEvents.TRIDENT_THUNDER,
                SoundSource.PLAYERS,
                0.5F,
                1.6F);
    }

    /** Spread link: electrify a specific block without cooldown. */
    private Electrify(ServerPlayer player, BlockPos at, int spread) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        if (!ELECTRIFIED.add(at)) {
            return;
        }
        this.block = at;
        this.center = new Vec3(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5);
        this.spread = spread;
    }

    public static void electrifyAt(ServerPlayer player, BlockPos at) {
        if (!(player.serverLevel().isLoaded(at))) {
            return;
        }
        var state = player.serverLevel().getBlockState(at);
        if (!isMetal(state) && state.getFluidState().isEmpty()) {
            return;
        }
        BendingManager.start(new Electrify(player, at.immutable(), 0));
    }

    private static boolean isMetal(net.minecraft.world.level.block.state.BlockState state) {
        String id = state.getBlock()
                .builtInRegistryHolder()
                .key()
                .location()
                .getPath()
                .toUpperCase();
        return id.contains("IRON")
                || id.contains("GOLD")
                || id.contains("COPPER")
                || id.contains("LIGHTNING_ROD")
                || id.contains("CAULDRON")
                || id.contains("CHAIN")
                || id.contains("NETHERITE")
                || id.equals("ANVIL");
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || player.hasDisconnected() || !gate(owner)) {
            return false;
        }
        if (this.center == null || level.getGameTime() - this.startTime > DURATION_TICKS) {
            return false;
        }
        if (!level.isLoaded(this.block)) {
            return false;
        }
        var state = level.getBlockState(this.block);
        if (!isMetal(state) && state.getFluidState().isEmpty()) {
            return false;
        }
        if (this.spread > 0) {
            this.spread = 0;
            for (Direction dir : Direction.values()) {
                BlockPos nb = this.block.relative(dir).immutable();
                if (!level.isLoaded(nb) || ELECTRIFIED.contains(nb)) {
                    continue;
                }
                var ns = level.getBlockState(nb);
                if (isMetal(ns) || !ns.getFluidState().isEmpty()) {
                    BendingManager.start(new Electrify(player, nb, 0));
                }
            }
        }
        this.ticks++;
        level.sendParticles(
                BendingTheme.particle(Config.ELECTRIFY_FIELD_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                this.center.x,
                this.center.y,
                this.center.z,
                Config.ELECTRIFY_FIELD_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.04);
        if (this.ticks % 100 == 0) {
            level.playSound(
                    null,
                    this.center.x,
                    this.center.y,
                    this.center.z,
                    SoundEvents.CREEPER_PRIMED,
                    SoundSource.PLAYERS,
                    0.3F,
                    0.6F);
        }
        boolean isWater = !state.getFluidState().isEmpty();
        for (Entity e : level.getEntities(player, new AABB(this.center, this.center).inflate(HIT_RADIUS))) {
            if (!(e instanceof LivingEntity living)
                    || e instanceof ArmorStand
                    || e.getUUID().equals(player.getUUID())) {
                continue;
            }
            BlockPos eb = BlockPos.containing(e.getX(), e.getY(), e.getZ());
            if (isWater) {
                if (!eb.equals(this.block)) {
                    continue;
                }
                living.hurt(player.damageSources().lightningBolt(), (float) WATER_DAMAGE);
            } else {
                if (!eb.equals(this.block.above())) {
                    continue;
                }
            }
            BendingPlayer victim = e instanceof ServerPlayer vsp ? BendingPlayer.get(vsp.getUUID()) : null;
            boolean immune = victim != null && victim.hasElement(BendingElement.FIRE);
            if (!immune) {
                living.addEffect(
                        new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_TICKS, 1, false, false, false));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_TICKS, 1, false, false, false));
                living.addEffect(new MobEffectInstance(MobEffects.JUMP, DEBUFF_TICKS, 128, false, false, false));
            }
        }
        return true;
    }

    @Override
    public void onRemove() {
        if (this.block != null) {
            ELECTRIFIED.remove(this.block);
        }
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
