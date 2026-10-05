package com.phatabaniyan.avataruniverse.bending.ability.air;

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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirBullet}: hold sneak to spiral air around
 * the body and concentrate it into the hand. Release once concentrated to
 * hold the loaded bullet, then left-click to fire a sharp thin round down the
 * gaze for massive damage.
 * Reference values: Cooldown 6000ms (120 ticks, paid only by a fired round
 * that lands a hit), Charge 2000ms (40 ticks), Damage 12, Range 30,
 * Speed 40, HitRadius 0.8, Armed 15000ms (300 ticks).
 */
public class AirBullet extends BendingAbility {
    public static final String ID = "AirBullet";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.AIRBULLET_COOLDOWN_MS.get());
    /** Reference Charge 2000ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.AIRBULLET_CHARGE_MS.get());
    /** Reference Armed 15000ms, in server ticks. */
    private static final long ARMED_TICKS = Config.msToTicks(Config.AIRBULLET_ARMED_MS.get());

    private static final double DAMAGE = Config.AIRBULLET_DAMAGE.get();
    private static final double RANGE = Config.AIRBULLET_RANGE.get();
    private static final double SPEED = Config.AIRBULLET_SPEED.get();
    private static final double HIT_RADIUS = Config.AIRBULLET_HIT_RADIUS.get();
    private static final double KNOCKBACK = Config.AIRBULLET_KNOCKBACK.get();

    private enum Phase {
        CHARGING,
        ARMED,
        FLYING
    }

    private final ServerLevel level;
    private final boolean invalid;
    private long armedAt = 0;
    private Phase phase = Phase.CHARGING;
    private Vec3 pos;
    private Vec3 dir;
    private double travelled = 0;
    private double ringAngle = 0;

    public AirBullet(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.invalid = player.isEyeInFluid(FluidTags.WATER);
    }

    public boolean isArmed() {
        return this.phase == Phase.ARMED;
    }

    /** Left-click with AirBullet bound: loose the loaded round. */
    public static void tryFire(ServerPlayer player) {
        AirBullet bullet = BendingManager.find(player.getUUID(), AirBullet.class);
        if (bullet == null || !bullet.isArmed()) {
            return;
        }
        bullet.fire(player);
    }

    private void fire(ServerPlayer player) {
        this.pos = handPos(player);
        this.dir = player.getLookAngle().normalize();
        this.travelled = 0;
        this.phase = Phase.FLYING;
        player.serverLevel()
                .playSound(
                        null,
                        this.pos.x,
                        this.pos.y,
                        this.pos.z,
                        SoundEvents.WIND_CHARGE_BURST,
                        SoundSource.PLAYERS,
                        1.0F,
                        0.6F);
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (invalid) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (this.phase == Phase.CHARGING) {
            if (!player.isShiftKeyDown()) {
                if (level.getGameTime() - this.startTime >= CHARGE_TICKS) {
                    this.phase = Phase.ARMED;
                    this.armedAt = level.getGameTime();
                    level.playSound(
                            null,
                            player.getX(),
                            player.getEyeY(),
                            player.getZ(),
                            SoundEvents.BUCKET_EMPTY,
                            SoundSource.PLAYERS,
                            0.5F,
                            1.6F);
                } else {
                    return false;
                }
                return true;
            }
            gatherParticles(player);
            return true;
        }
        if (this.phase == Phase.ARMED) {
            if (level.getGameTime() - this.armedAt > ARMED_TICKS) {
                return false;
            }
            // Dense bullet ball kneaded at the hand.
            Vec3 hand = handPos(player);
            this.ringAngle += 1.2;
            for (int k = 0; k < 6; k++) {
                double a = this.ringAngle + k * (Math.PI / 3);
                level.sendParticles(
                        BendingTheme.particle(Config.AIRBULLET_ARMED_RING_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        hand.x + Math.cos(a) * 0.12,
                        hand.y,
                        hand.z + Math.sin(a) * 0.12,
                        Config.AIRBULLET_ARMED_RING_PARTICLE_COUNT.get(),
                        0.02,
                        0.02,
                        0.02,
                        0.005);
            }
            level.sendParticles(
                    BendingTheme.particle(Config.AIRBULLET_ARMED_CORE_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    hand.x,
                    hand.y,
                    hand.z,
                    Config.AIRBULLET_ARMED_CORE_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.005);
            return true;
        }
        // FLYING: thin fast tracer, dies on first contact.
        double step = Math.max(0.5, SPEED / 20.0);
        int cells = Math.max(1, (int) Math.ceil(step));
        for (int i = 0; i < cells; i++) {
            Vec3 next = this.pos.add(this.dir.scale(step / cells));
            this.travelled += step / cells;
            if (this.travelled >= RANGE) {
                return false;
            }
            BlockPos bp = BlockPos.containing(next);
            if (!level.isLoaded(bp)) {
                return false;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                level.sendParticles(
                        BendingTheme.particle(Config.AIRBULLET_IMPACT_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        next.x,
                        next.y,
                        next.z,
                        Config.AIRBULLET_IMPACT_PARTICLE_COUNT.get(),
                        0.2,
                        0.2,
                        0.2,
                        0.05);
                return false;
            }
            this.pos = next;
            level.sendParticles(
                    BendingTheme.particle(Config.AIRBULLET_TRACER_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    Config.AIRBULLET_TRACER_PARTICLE_COUNT.get(),
                    0.03,
                    0.03,
                    0.03,
                    0.005);
            for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(HIT_RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.hurt(player.damageSources().magic(), (float) DAMAGE);
                }
                e.setDeltaMovement(this.dir.scale(KNOCKBACK));
                e.hurtMarked = true;
                level.sendParticles(
                        BendingTheme.particle(Config.AIRBULLET_HIT_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        this.pos.x,
                        this.pos.y,
                        this.pos.z,
                        Config.AIRBULLET_HIT_PARTICLE_COUNT.get(),
                        0.25,
                        0.25,
                        0.25,
                        0.05);
                level.playSound(
                        null,
                        this.pos.x,
                        this.pos.y,
                        this.pos.z,
                        SoundEvents.GENERIC_EXPLODE,
                        SoundSource.PLAYERS,
                        0.5F,
                        1.5F);
                cool(owner, level, ID, COOLDOWN_TICKS);
                return false;
            }
        }
        return true;
    }

    private static Vec3 handPos(ServerPlayer sp) {
        Vec3 eye = new Vec3(sp.getX(), sp.getEyeY(), sp.getZ());
        Vec3 look = sp.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        return new Vec3(
                eye.x + look.x * 0.35 + side.x * 0.35,
                eye.y - 0.35 + look.y * 0.35,
                eye.z + look.z * 0.35 + side.z * 0.35);
    }

    private void gatherParticles(ServerPlayer sp) {
        double done = Math.min(1.0, (double) (level.getGameTime() - this.startTime) / (double) CHARGE_TICKS);
        Vec3 hand = handPos(sp);
        // True helix around the body, collapsing toward the hand as it charges.
        double r = 1.5 - done * 1.15;
        double yCenter = (sp.getY() + 0.9) + (hand.y - (sp.getY() + 0.9)) * done;
        double ySpan = 1.6 - done * 1.35;
        this.ringAngle += 0.9;
        for (int k = 0; k < 10; k++) {
            double a = this.ringAngle + k * (Math.PI / 5);
            double frac = ((a % (Math.PI * 2)) + Math.PI * 2) % (Math.PI * 2) / (Math.PI * 2);
            level.sendParticles(
                    BendingTheme.particle(Config.AIRBULLET_GATHER_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    sp.getX() + Math.cos(a) * r,
                    yCenter + (frac - 0.5) * ySpan,
                    sp.getZ() + Math.sin(a) * r,
                    Config.AIRBULLET_GATHER_PARTICLE_COUNT.get(),
                    0.06,
                    0.06,
                    0.06,
                    0.03);
        }
        if (done >= 1.0) {
            level.sendParticles(
                    BendingTheme.particle(Config.AIRBULLET_CHARGED_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    hand.x,
                    hand.y,
                    hand.z,
                    Config.AIRBULLET_CHARGED_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.01);
        }
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
