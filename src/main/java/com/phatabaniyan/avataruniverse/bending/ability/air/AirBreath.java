package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirBreath}: hold sneak to exhale a battering
 * cone of wind down the look vector that shoves and batters everything ahead,
 * snuffs fire, and lends breath to the drowning. Aiming steeply down at a
 * wall blasts you backwards off it.
 * Reference values: Cooldown 3000ms (60 ticks), Duration 3000ms (60 ticks),
 * Range 12, Knockback 1, PlayerDamage 2, MobDamage 3, Launch 1.5,
 * Particles 6.
 */
public class AirBreath extends BendingAbility {
    public static final String ID = "AirBreath";

    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.AIRBREATH_COOLDOWN_TICKS.get();
    /** Reference Duration 3000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.AIRBREATH_DURATION_TICKS.get();

    private static final double RANGE = Config.AIRBREATH_RANGE.get();
    private static final double KNOCKBACK = Config.AIRBREATH_KNOCKBACK.get();
    private static final double PLAYER_DAMAGE = Config.AIRBREATH_PLAYER_DAMAGE.get();
    private static final double MOB_DAMAGE = Config.AIRBREATH_MOB_DAMAGE.get();
    private static final double LAUNCH = Config.AIRBREATH_LAUNCH.get();
    private static final int PARTICLES = Config.AIRBREATH_PARTICLES.get();
    private static final double HIT_RADIUS = Config.AIRBREATH_HIT_RADIUS.get();
    private static final int OXYGEN_DURATION_TICKS = Config.AIRBREATH_OXYGEN_DURATION_TICKS.get();
    private static final int OXYGEN_AMPLIFIER = Config.AIRBREATH_OXYGEN_AMPLIFIER.get();
    private static final boolean DAMAGE_ENABLED = true;
    private static final boolean EXTINGUISH_FIRE = true;
    private static final boolean EXTINGUISH_MOBS = true;
    private static final boolean REGEN_OXYGEN = true;

    private final ServerLevel level;

    public AirBreath(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        if (level.getGameTime() - startTime > DURATION_TICKS) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 loc = eye;
        double step = 1.0;
        for (double i = 0; i < RANGE; i += step) {
            loc = new Vec3(eye.x + dir.x * i, eye.y + dir.y * i, eye.z + dir.z * i);
            BlockPos bp = BlockPos.containing(loc);
            if (!level.isLoaded(bp)) {
                break;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp)) {
                if (player.getXRot() > 30) {
                    Vec3 push = dir.scale(-LAUNCH);
                    player.setDeltaMovement(push);
                    player.hurtMarked = true;
                }
                break;
            }
            if (EXTINGUISH_FIRE && state.is(Blocks.FIRE)) {
                level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
            }
            for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(HIT_RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                e.setDeltaMovement(dir.scale(KNOCKBACK));
                e.hurtMarked = true;
                if (e.isOnFire() && EXTINGUISH_MOBS) {
                    e.clearFire();
                }
                if (e instanceof LivingEntity living) {
                    if (DAMAGE_ENABLED) {
                        float dmg = (float) (e instanceof ServerPlayer ? PLAYER_DAMAGE : MOB_DAMAGE);
                        // Damage first so hurt() cannot eat the shove.
                        living.hurt(player.damageSources().magic(), dmg);
                        e.setDeltaMovement(dir.scale(KNOCKBACK));
                        e.hurtMarked = true;
                    }
                    if (REGEN_OXYGEN
                            && living.isEyeInFluid(FluidTags.WATER)
                            && !living.hasEffect(MobEffects.WATER_BREATHING)) {
                        living.addEffect(new MobEffectInstance(
                                MobEffects.WATER_BREATHING,
                                OXYGEN_DURATION_TICKS,
                                OXYGEN_AMPLIFIER,
                                false,
                                false,
                                false));
                    }
                }
            }
            level.sendParticles(
                    BendingTheme.particle(Config.AIRBREATH_CONE_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    loc.x,
                    loc.y,
                    loc.z,
                    Config.AIRBREATH_CONE_PARTICLE_COUNT.get(),
                    0.4,
                    0.4,
                    0.4,
                    0.05);
        }
        if (player.getRandom().nextInt(3) == 0) {
            level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.3F, 0.8F);
        }
        return true;
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
