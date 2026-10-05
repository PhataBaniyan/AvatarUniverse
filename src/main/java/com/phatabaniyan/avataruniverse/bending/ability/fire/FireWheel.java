package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
 * Port of ProjectAvatar {@code FireWheel}: click to roll a burning wheel
 * down the gaze. It hugs the ground, climbs small steps, and brands
 * everything it touches once while shoving it along. Ends at max range or
 * when no ground is found. Reference values: Cooldown 5000ms (100 ticks),
 * Damage 4, Speed 1.0, Range 20, Height 4 (radius 2), FireTicks 3.
 */
public class FireWheel extends BendingAbility {
    public static final String ID = "FireWheel";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.FIREWHEEL_COOLDOWN_MS.get());

    private static final double DAMAGE = Config.FIREWHEEL_DAMAGE.get();
    private static final double SPEED = Config.FIREWHEEL_SPEED.get();
    private static final double RANGE = Config.FIREWHEEL_RANGE.get();
    private static final double HEIGHT = Config.FIREWHEEL_HEIGHT.get();
    private static final int FIRE_TICKS = Config.FIREWHEEL_FIRE_MS.get() / 1000;

    private final ServerLevel level;
    private final double radius;
    private Vec3 pos;
    private Vec3 dir;
    private Vec3 origin;
    private double ringAngle = 0;
    private boolean valid = false;
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public FireWheel(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        Vec3 look = player.getLookAngle().normalize();
        this.dir = new Vec3(look.x, 0, look.z);
        if (this.dir.lengthSqr() < 1.0e-4) {
            this.dir = new Vec3(1, 0, 0);
        }
        this.dir = this.dir.normalize();
        this.radius = HEIGHT / 2;
        BlockPos ground = findGround(level, player.blockPosition());
        if (ground == null) {
            return;
        }
        this.pos = new Vec3(player.getX(), ground.getY() + 1.0 + this.radius, player.getZ());
        this.origin = this.pos;
        cool(owner, level, ID, COOLDOWN_TICKS);
        level.playSound(
                null, this.pos.x, this.pos.y, this.pos.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.7F, 0.8F);
        this.valid = true;
    }

    @Override
    public String name() {
        return ID;
    }

    private static BlockPos findGround(ServerLevel level, BlockPos near) {
        for (int i = 3; i >= -3; i--) {
            BlockPos p = near.above(i);
            if (!level.isLoaded(p) || !level.isLoaded(p.below())) {
                continue;
            }
            if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).isSolidRender(level, p.below())) {
                return p.below().immutable();
            }
        }
        if (level.isLoaded(near) && level.getBlockState(near).isSolidRender(level, near)) {
            return near.immutable();
        }
        return null;
    }

    @Override
    public boolean progress() {
        if (!valid) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || !player.isAlive() || player.hasDisconnected()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        Vec3 next = this.pos.add(this.dir.scale(SPEED));
        if (next.distanceToSqr(this.origin) > RANGE * RANGE) {
            return false;
        }
        BlockPos ground = findGround(level, BlockPos.containing(next.x, next.y, next.z));
        if (ground == null) {
            return false;
        }
        this.pos = new Vec3(next.x, ground.getY() + 1.0 + this.radius, next.z);
        this.ringAngle += 0.4;
        for (int k = 0; k < 24; k++) {
            double a = this.ringAngle + k * (Math.PI / 12);
            Vec3 p = this.pos
                    .add(this.dir.scale(Math.cos(a) * this.radius))
                    .add(new Vec3(0, Math.sin(a) * this.radius, 0));
            level.sendParticles(
                    BendingTheme.particle(Config.FIREWHEEL_RING_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    p.x,
                    p.y,
                    p.z,
                    Config.FIREWHEEL_RING_PARTICLE_COUNT.get(),
                    0.08,
                    0.08,
                    0.08,
                    0.02);
        }
        if (player.getRandom().nextInt(4) == 0) {
            level.playSound(
                    null,
                    this.pos.x,
                    this.pos.y,
                    this.pos.z,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.PLAYERS,
                    0.5F,
                    0.9F);
        }
        for (Entity e : level.getEntities(player, new AABB(this.pos, this.pos).inflate(this.radius + 0.5))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living && this.affected.add(e.getUUID())) {
                living.hurt(player.damageSources().magic(), (float) DAMAGE);
                living.igniteForSeconds(FIRE_TICKS);
            }
            e.setDeltaMovement(this.dir.scale(Config.FIREWHEEL_PUSH.get()));
            e.hurtMarked = true;
        }
        return true;
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
