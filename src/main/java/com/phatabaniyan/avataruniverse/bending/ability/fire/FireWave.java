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
 * Port of ProjectAvatar {@code FireWave}: hold sneak to march a burning wall
 * down the gaze. It stops whatever it touches dead and sets the survivors
 * alight. Cooldown applies up front in the constructor, so releasing sneak
 * early still costs the cooldown. Reference values: Cooldown 7000ms
 * (140 ticks), Duration 6000ms (120 ticks), Range 20, Speed 0.8, Width 3,
 * Height 3, Damage 4.
 */
public class FireWave extends BendingAbility {
    public static final String ID = "FireWave";

    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.FIREWAVE_COOLDOWN_MS.get());
    /** Reference Duration 6000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.FIREWAVE_DURATION_MS.get());

    private static final double RANGE = Config.FIREWAVE_RANGE.get();
    private static final double SPEED = Config.FIREWAVE_SPEED.get();
    private static final double WIDTH = Config.FIREWAVE_WIDTH.get();
    private static final double HEIGHT = Config.FIREWAVE_HEIGHT.get();
    private static final float DAMAGE = Config.FIREWAVE_DAMAGE.get().floatValue();
    private static final int FIRE_SECONDS = Config.FIREWAVE_FIRE_MS.get() / 1000;

    private final ServerPlayer player;
    private final ServerLevel level;
    private final Vec3 origin;
    private final Vec3 dir;
    private final Vec3 side;
    private double travelled = 0;

    public FireWave(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();

        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-4) {
            flat = new Vec3(1, 0, 0);
        }
        this.dir = flat.normalize();
        Vec3 s = new Vec3(-this.dir.z, 0, this.dir.x);
        this.side = s.lengthSqr() < 1.0e-4 ? new Vec3(1, 0, 0) : s.normalize();
        this.origin = player.position();
        cool(player.getUUID(), this.level, ID, COOLDOWN_TICKS);
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
            return false;
        }
        if (level.getGameTime() - this.startTime > DURATION_TICKS) {
            return false;
        }
        this.travelled += SPEED;
        if (this.travelled > RANGE) {
            return false;
        }
        Vec3 center = this.origin.add(this.dir.scale(this.travelled));
        BlockPos bp = BlockPos.containing(center.x, center.y, center.z);
        if (!level.isLoaded(bp)) {
            return false;
        }
        for (double i = -WIDTH; i <= WIDTH; i += 1.0) {
            for (double j = 0; j <= HEIGHT; j += 1.0) {
                Vec3 p = new Vec3(center.x + this.side.x * i, center.y + j, center.z + this.side.z * i);
                level.sendParticles(
                        BendingTheme.particle(Config.FIREWAVE_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        p.x,
                        p.y,
                        p.z,
                        Config.FIREWAVE_FLAME_PARTICLE_COUNT.get(),
                        0.25,
                        0.25,
                        0.25,
                        0.04);
                if (player.getRandom().nextInt(12) == 0) {
                    level.sendParticles(
                            BendingTheme.particle(Config.FIREWAVE_WALL_PARTICLE.get(), ParticleTypes.SMOKE),
                            p.x,
                            p.y,
                            p.z,
                            Config.FIREWAVE_WALL_PARTICLE_COUNT.get(),
                            0.2,
                            0.2,
                            0.2,
                            0.03);
                }
            }
        }
        if (player.getRandom().nextInt(8) == 0) {
            level.playSound(
                    null, center.x, center.y, center.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.5F, 0.9F);
        }
        for (Entity e : level.getEntities(
                player,
                new AABB(
                        center.x - WIDTH - 1,
                        center.y - 1,
                        center.z - WIDTH - 1,
                        center.x + WIDTH + 1,
                        center.y + HEIGHT + 1,
                        center.z + WIDTH + 1))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            Vec3 o = e.position().subtract(center);
            if (Math.abs(o.dot(this.side)) > WIDTH + 1
                    || o.y < -1
                    || o.y > HEIGHT + 1
                    || Math.abs(o.dot(this.dir)) > 1.5) {
                continue;
            }
            e.setDeltaMovement(new Vec3(0, 0, 0));
            e.hurtMarked = true;
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), DAMAGE);
                living.igniteForSeconds(FIRE_SECONDS);
            } else {
                e.igniteForSeconds(FIRE_SECONDS);
            }
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
