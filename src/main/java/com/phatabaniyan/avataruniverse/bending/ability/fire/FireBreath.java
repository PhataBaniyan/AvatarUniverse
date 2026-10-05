package com.phatabaniyan.avataruniverse.bending.ability.fire;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FireBreath}: hold sneak to breathe a cone of
 * dragonfire down the gaze. Everything in the ray catches fire, with split
 * player/mob damage. Cooldown applies on release or when the duration runs
 * out, not on gate failure. Reference values: Cooldown 3500ms (70 ticks),
 * Duration 3000ms (60 ticks), Particles 6, PlayerDamage 2.0, MobDamage 3.0,
 * FireTicks 3, Range 12.
 */
public class FireBreath extends BendingAbility {
    public static final String ID = "FireBreath";

    /** Reference Cooldown 3500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.FIREBREATH_COOLDOWN_MS.get());
    /** Reference Duration 3000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.FIREBREATH_DURATION_MS.get());

    private static final int PARTICLES = Config.FIREBREATH_PARTICLES.get();
    private static final double PLAYER_DAMAGE = Config.FIREBREATH_PLAYER_DAMAGE.get();
    private static final double MOB_DAMAGE = Config.FIREBREATH_MOB_DAMAGE.get();
    private static final int FIRE_SECONDS = Config.FIREBREATH_FIRE_MS.get() / 1000;
    private static final double RANGE = Config.FIREBREATH_RANGE.get();

    private final ServerPlayer player;
    private final ServerLevel level;

    public FireBreath(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        if (level.getGameTime() - this.startTime > DURATION_TICKS) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 dir = player.getLookAngle().normalize();
        for (double i = 0; i < RANGE; i += 1.0) {
            Vec3 loc = new Vec3(eye.x + dir.x * i, eye.y + dir.y * i, eye.z + dir.z * i);
            BlockPos bp = BlockPos.containing(loc);
            if (!level.isLoaded(bp)) {
                break;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                break;
            }
            for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(1.5))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    float dmg = (float) (e instanceof ServerPlayer ? PLAYER_DAMAGE : MOB_DAMAGE);
                    living.hurt(player.damageSources().magic(), dmg);
                    living.igniteForSeconds(FIRE_SECONDS);
                } else {
                    e.igniteForSeconds(FIRE_SECONDS);
                }
            }
            level.sendParticles(
                    BendingTheme.particle(Config.FIREBREATH_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    loc.x,
                    loc.y,
                    loc.z,
                    PARTICLES,
                    0.3,
                    0.3,
                    0.3,
                    0.05);
            level.sendParticles(
                    BendingTheme.particle(Config.FIREBREATH_TRAIL_PARTICLE.get(), ParticleTypes.SMOKE),
                    loc.x,
                    loc.y,
                    loc.z,
                    Config.FIREBREATH_TRAIL_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.04);
        }
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 0.8F);
        return true;
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
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
