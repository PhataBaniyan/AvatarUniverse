package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
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
 * Port of ProjectAvatar {@code Combustion} (ProjectKorra's): the eye-beam
 * fires on cast and flies straight; left-click mid-flight (bound-slot click
 * routing calls {@link #detonate}) blows it up where it is. Fluids snuff it
 * with a smoke puff; solids and entities detonate it. Cooldown applies on
 * cast, matching the source. Reference values: Cooldown 6000ms (120 ticks),
 * Damage 6, Radius 4, Speed 20, Range 25, vanilla explosion power 3.0 with
 * block damage.
 */
public class Combustion extends BendingAbility {
    public static final String ID = "Combustion";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.COMBUSTION_COOLDOWN_MS.get());

    private static final double DAMAGE = Config.COMBUSTION_DAMAGE.get();
    private static final double RADIUS = Config.COMBUSTION_RADIUS.get();
    private static final double SPEED = Config.COMBUSTION_SPEED.get();
    private static final double RANGE = Config.COMBUSTION_RANGE.get();
    private static final double HIT_RADIUS = Config.COMBUSTION_HIT_RADIUS.get();
    private static final double EXPLOSION_POWER = Config.COMBUSTION_EXPLOSION_POWER.get();
    private static final double KNOCKBACK = Config.COMBUSTION_KNOCKBACK.get();
    private static final int IGNITE_SECONDS = Config.COMBUSTION_IGNITE_MS.get() / 1000;
    private static final int MAX_TICKS = Config.msToTicks(Config.COMBUSTION_MAX_MS.get());

    private final ServerPlayer player;
    private final ServerLevel level;
    private final double damage;
    private final double radius;
    private final double speed;
    private final double range;
    private Vec3 pos;
    private final Vec3 dir;
    private final Vec3 origin;
    private int ticks = 0;

    public Combustion(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.damage = DAMAGE;
        this.radius = RADIUS;
        this.speed = SPEED;
        this.range = RANGE;
        this.origin = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        this.dir = player.getLookAngle().normalize();
        this.pos = this.origin;
        cool(player.getUUID(), this.level, ID, COOLDOWN_TICKS);
        level.playSound(
                null, this.pos.x, this.pos.y, this.pos.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 0.6F);
    }

    /** Left-click with Combustion bound: blow the beam up where it is. */
    public static void detonate(ServerPlayer player) {
        Combustion c = BendingManager.find(player.getUUID(), Combustion.class);
        if (c == null) {
            return;
        }
        Vec3 at = c.pos;
        BendingManager.remove(c);
        if (at != null) {
            explodeAt(player, at, c.damage, c.radius);
        }
    }

    private static void explodeAt(ServerPlayer sp, Vec3 at, double damage, double radius) {
        ServerLevel level = sp.serverLevel();
        for (Entity e : level.getEntities(sp, new AABB(at, at).inflate(radius))) {
            if (e.getUUID().equals(sp.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living) {
                living.hurt(sp.damageSources().magic(), (float) damage);
                living.igniteForSeconds(IGNITE_SECONDS);
            }
            Vec3 push = e.position().subtract(at);
            if (push.lengthSqr() < 1.0e-4) {
                push = new Vec3(0, 1, 0);
            }
            e.setDeltaMovement(push.normalize().scale(KNOCKBACK));
            e.hurtMarked = true;
        }
        // Small TNT-like pop where it lands.
        level.explode(
                null,
                at.x,
                at.y,
                at.z,
                (float) EXPLOSION_POWER,
                net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTION_BLAST_PARTICLE.get(), ParticleTypes.EXPLOSION),
                at.x,
                at.y,
                at.z,
                Config.COMBUSTION_BLAST_PARTICLE_COUNT.get(),
                0.4,
                0.4,
                0.4,
                0.08);
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTION_BURST_PARTICLE.get(), sp.getUUID(), ParticleTypes.FLAME),
                at.x,
                at.y,
                at.z,
                Config.COMBUSTION_BURST_PARTICLE_COUNT.get(),
                0.7,
                0.7,
                0.7,
                0.08);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.9F, 0.8F);
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
        if (++this.ticks > MAX_TICKS) {
            return false;
        }
        double step = Math.max(0.2, this.speed / 20.0);
        int cells = Math.max(1, (int) Math.ceil(step));
        for (int i = 0; i < cells; i++) {
            Vec3 next = this.pos.add(this.dir.scale(step / cells));
            BlockPos bp = BlockPos.containing(next);
            if (!level.isLoaded(bp)) {
                return false;
            }
            var state = level.getBlockState(bp);
            if (!state.getFluidState().isEmpty()) {
                level.sendParticles(
                        BendingTheme.particle(Config.COMBUSTION_FIZZ_PARTICLE.get(), ParticleTypes.SMOKE),
                        next.x,
                        next.y,
                        next.z,
                        Config.COMBUSTION_FIZZ_PARTICLE_COUNT.get(),
                        0.3,
                        0.3,
                        0.3,
                        0.05);
                return false;
            }
            if (state.isSolidRender(level, bp)) {
                Vec3 at = this.pos;
                explodeAt(player, at, this.damage, this.radius);
                return false;
            }
            this.pos = next;
        }
        if (this.pos.distanceToSqr(this.origin) > this.range * this.range) {
            return false;
        }
        for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(HIT_RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            Vec3 at = this.pos;
            explodeAt(player, at, this.damage, this.radius);
            return false;
        }
        level.sendParticles(
                BendingTheme.particle(Config.COMBUSTION_TRAIL_PARTICLE.get(), ParticleTypes.FIREWORK),
                this.pos.x,
                this.pos.y,
                this.pos.z,
                Config.COMBUSTION_TRAIL_PARTICLE_COUNT.get(),
                0.08,
                0.08,
                0.08,
                0.02);
        if (this.ticks % 5 == 0) {
            level.sendParticles(
                    BendingTheme.particle(Config.COMBUSTION_SMOKE_PARTICLE.get(), ParticleTypes.LARGE_SMOKE),
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    Config.COMBUSTION_SMOKE_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.03);
            level.sendParticles(
                    BendingTheme.particle(Config.COMBUSTION_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    Config.COMBUSTION_FLAME_PARTICLE_COUNT.get(),
                    0.25,
                    0.25,
                    0.25,
                    0.03);
        }
        if (player.getRandom().nextInt(6) == 0) {
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.4F,
                    0.8F);
        }
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
