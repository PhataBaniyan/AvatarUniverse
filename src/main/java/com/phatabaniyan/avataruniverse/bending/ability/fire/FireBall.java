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
 * Port of ProjectAvatar {@code FireBall} (JedCore port).
 * Click to throw a steerable fireball: it bends toward wherever you look
 * while it flies, and sets ablaze whatever it catches. No block trail.
 * Reference values: Cooldown 2500ms, Damage 4, Range 20, Speed 2,
 * FireTicks 3, HitRadius 1.5, Controllable true.
 */
public class FireBall extends BendingAbility {
    public static final String ID = "FireBall";

    /** Reference Cooldown 2500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FIREBALL_COOLDOWN_TICKS.get();

    private static final float DAMAGE = Config.FIREBALL_DAMAGE.get().floatValue();
    private static final double RANGE = Config.FIREBALL_RANGE.get();
    private static final double SPEED = Config.FIREBALL_SPEED.get();
    private static final int FIRE_SECONDS = Config.FIREBALL_FIRE_SECONDS.get();
    private static final double HIT_RADIUS = Config.FIREBALL_HIT_RADIUS.get();
    private static final boolean CONTROLLABLE = Config.FIREBALL_CONTROLLABLE.get();
    private static final int MAX_TICKS = Config.FIREBALL_MAX_TICKS.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private final boolean controllable = CONTROLLABLE;
    private Vec3 pos;
    private Vec3 dir;
    private final Vec3 origin;
    private int ticks = 0;

    public FireBall(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        this.origin = this.pos;
        this.dir = player.getLookAngle().normalize();
        cool(player.getUUID(), player, ID, COOLDOWN_TICKS);
        level.playSound(
                null, this.pos.x, this.pos.y, this.pos.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.7F, 1.0F);
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
        if (++this.ticks > MAX_TICKS) {
            return false;
        }
        if (this.controllable && player.isAlive()) {
            Vec3 look = player.getLookAngle().normalize();
            this.dir = this.dir.scale(0.85).add(look.scale(0.15)).normalize();
        }
        double step = Math.max(0.2, SPEED);
        int cells = Math.max(1, (int) Math.ceil(step));
        for (int i = 0; i < cells; i++) {
            Vec3 next = this.pos.add(this.dir.scale(step / cells));
            BlockPos bp = BlockPos.containing(next);
            if (!level.isLoaded(bp)) {
                return false;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                level.sendParticles(
                        BendingTheme.particle(Config.FIREBALL_HIT_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        next.x,
                        next.y,
                        next.z,
                        Config.FIREBALL_HIT_PARTICLE_COUNT.get(),
                        0.4,
                        0.4,
                        0.4,
                        0.05);
                return false;
            }
            this.pos = next;
        }
        if (this.pos.distanceToSqr(this.origin) > RANGE * RANGE) {
            return false;
        }
        for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(HIT_RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), DAMAGE);
                living.igniteForSeconds(FIRE_SECONDS);
            } else {
                e.igniteForSeconds(FIRE_SECONDS);
            }
            return false;
        }
        level.sendParticles(
                BendingTheme.particle(Config.FIREBALL_TRAIL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                this.pos.x,
                this.pos.y,
                this.pos.z,
                Config.FIREBALL_TRAIL_PARTICLE_COUNT.get(),
                0.1,
                0.1,
                0.1,
                0.02);
        level.sendParticles(
                BendingTheme.particle(Config.FIREBALL_SMOKE_PARTICLE.get(), ParticleTypes.SMOKE),
                this.pos.x,
                this.pos.y,
                this.pos.z,
                Config.FIREBALL_SMOKE_PARTICLE_COUNT.get(),
                0.1,
                0.1,
                0.1,
                0.02);
        if (player.getRandom().nextInt(4) == 0) {
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.4F,
                    1.0F);
        }
        return true;
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
