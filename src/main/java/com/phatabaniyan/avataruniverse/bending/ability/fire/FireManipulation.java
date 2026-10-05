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
 * Port of ProjectAvatar {@code FireManipulation} (ProjectKorra port).
 * Hold sneak to gather a burning orb at your cursor that sears what it
 * touches; left-click to discharge it as a stream down your current gaze.
 * Releasing sneak ends the stream, like the source.
 * Reference values: StreamCooldown 4000ms, StreamRange 20, StreamDamage 4,
 * StreamSpeed 1.5, AuraDamage 2, AuraRadius 2.
 *
 * <p>Note: the source dials the range live by scrolling the hotbar. This mod
 * has no scroll packets, so the range stays fixed at the source default.</p>
 */
public class FireManipulation extends BendingAbility {
    public static final String ID = "FireManipulation";

    /** Reference StreamCooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FIREMANIPULATION_COOLDOWN_TICKS.get();

    private static final double STREAM_RANGE = Config.FIREMANIPULATION_STREAM_RANGE.get();
    private static final float STREAM_DAMAGE =
            Config.FIREMANIPULATION_STREAM_DAMAGE.get().floatValue();
    private static final double STREAM_SPEED = Config.FIREMANIPULATION_STREAM_SPEED.get();
    private static final float AURA_DAMAGE =
            Config.FIREMANIPULATION_AURA_DAMAGE.get().floatValue();
    private static final double AURA_RADIUS = Config.FIREMANIPULATION_AURA_RADIUS.get();
    private static final double STREAM_HIT_RADIUS = Config.FIREMANIPULATION_STREAM_HIT_RADIUS.get();
    private static final int AURA_FIRE_SECONDS = Config.FIREMANIPULATION_AURA_FIRE_SECONDS.get();
    private static final int STREAM_FIRE_SECONDS = Config.FIREMANIPULATION_STREAM_FIRE_SECONDS.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private final boolean started;
    private Vec3 orb;
    private boolean firing = false;
    private boolean cooled = false;
    private Vec3 shotDir;
    private Vec3 shotPos;
    private double shotDist = 0;

    public FireManipulation(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.orb = cursorPoint(player, STREAM_RANGE);
        this.started = this.orb != null;
    }

    @Override
    public String name() {
        return ID;
    }

    private static Vec3 cursorPoint(ServerPlayer player, double range) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 last = eye;
        for (double d = 0; d <= range; d += 0.5) {
            Vec3 p = new Vec3(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            BlockPos bp = BlockPos.containing(p);
            if (!level.isLoaded(bp)) {
                return last;
            }
            if (level.getBlockState(bp).isSolidRender(level, bp)) {
                return last;
            }
            last = p;
        }
        return last;
    }

    @Override
    public boolean progress() {
        if (!started || !alive(player) || !gate(owner)) {
            return false;
        }
        if (!this.firing) {
            if (!player.isShiftKeyDown()) {
                return false;
            }
            this.orb = cursorPoint(player, STREAM_RANGE);
            if (this.orb == null) {
                return false;
            }
            level.sendParticles(
                    BendingTheme.particle(Config.FIREMANIPULATION_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    this.orb.x,
                    this.orb.y,
                    this.orb.z,
                    Config.FIREMANIPULATION_FLAME_PARTICLE_COUNT.get(),
                    0.4,
                    0.4,
                    0.4,
                    0.04);
            level.sendParticles(
                    BendingTheme.particle(Config.FIREMANIPULATION_ORB_PARTICLE.get(), ParticleTypes.SMOKE),
                    this.orb.x,
                    this.orb.y,
                    this.orb.z,
                    Config.FIREMANIPULATION_ORB_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.03);
            for (Entity e : level.getEntities(player, new AABB(this.orb, this.orb).inflate(AURA_RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.hurt(player.damageSources().magic(), AURA_DAMAGE);
                    living.igniteForSeconds(AURA_FIRE_SECONDS);
                } else {
                    e.igniteForSeconds(AURA_FIRE_SECONDS);
                }
            }
            return true;
        }
        // Firing stream head.
        double step = Math.max(0.3, STREAM_SPEED);
        Vec3 next = this.shotPos.add(this.shotDir.scale(step));
        this.shotDist += step;
        BlockPos bp = BlockPos.containing(next);
        if (!level.isLoaded(bp) || this.shotDist > STREAM_RANGE) {
            coolOnce();
            return false;
        }
        var state = level.getBlockState(bp);
        if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
            coolOnce();
            return false;
        }
        this.shotPos = next;
        level.sendParticles(
                BendingTheme.particle(Config.FIREMANIPULATION_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                this.shotPos.x,
                this.shotPos.y,
                this.shotPos.z,
                Config.FIREMANIPULATION_FLAME_PARTICLE_COUNT.get(),
                0.4,
                0.4,
                0.4,
                0.05);
        if (player.getRandom().nextInt(5) == 0) {
            level.playSound(
                    null,
                    this.shotPos.x,
                    this.shotPos.y,
                    this.shotPos.z,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.4F,
                    1.0F);
        }
        for (Entity e : level.getEntities(player, new AABB(this.shotPos, this.shotPos).inflate(STREAM_HIT_RADIUS))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), STREAM_DAMAGE);
                living.igniteForSeconds(STREAM_FIRE_SECONDS);
            } else {
                e.igniteForSeconds(STREAM_FIRE_SECONDS);
            }
        }
        if (!player.isShiftKeyDown()) {
            coolOnce();
            return false;
        }
        return true;
    }

    /** Called by the click router: orb becomes a stream down the live gaze. */
    public void fireStream(ServerPlayer sp) {
        if (this.firing) {
            return;
        }
        this.shotDir = sp.getLookAngle().normalize();
        this.shotPos = this.orb == null ? new Vec3(sp.getX(), sp.getEyeY(), sp.getZ()) : this.orb;
        this.shotDist = 0;
        this.firing = true;
        sp.serverLevel()
                .playSound(
                        null,
                        sp.getX(),
                        sp.getEyeY(),
                        sp.getZ(),
                        SoundEvents.FIRECHARGE_USE,
                        SoundSource.PLAYERS,
                        0.7F,
                        0.9F);
    }

    @Override
    public void onRemove() {
        if (this.firing) {
            coolOnce();
        }
    }

    private void coolOnce() {
        if (this.cooled) {
            return;
        }
        this.cooled = true;
        cool(owner, level);
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }
}
