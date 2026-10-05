package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
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
 * Port of ProjectAvatar {@code FireShots} (JedCore port).
 * Sneak to gather a stock of fireballs at the hand; every left-click throws
 * one down your live gaze until the stock runs dry. Like the source this
 * instance is NOT sneak-gated in progress: it stays alive after release so
 * the remaining stock can still be thrown, and ends once the stock is spent
 * and every shot has landed or fizzled.
 * Reference values: Cooldown 3000ms, Stock 4, Range 20, Damage 3,
 * FireTicks 3, HitRadius 1.5, Speed 2.
 */
public class FireShots extends BendingAbility {
    public static final String ID = "FireShots";

    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FIRESHOTS_COOLDOWN_TICKS.get();

    private static final int STOCK = Config.FIRESHOTS_STOCK.get();
    private static final double RANGE = Config.FIRESHOTS_RANGE.get();
    private static final float DAMAGE = Config.FIRESHOTS_DAMAGE.get().floatValue();
    private static final int FIRE_SECONDS = Config.FIRESHOTS_FIRE_SECONDS.get();
    private static final double HIT_RADIUS = Config.FIRESHOTS_HIT_RADIUS.get();
    private static final double SPEED = Config.FIRESHOTS_SPEED.get();

    private record Shot(Vec3 pos, Vec3 dir, double dist) {}

    private final ServerPlayer player;
    private final ServerLevel level;
    private int stock = STOCK;
    private final List<Shot> shots = new ArrayList<>();
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public FireShots(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Left-click with FireShots bound: throw one from the stock. */
    public static void tryFire(ServerPlayer player) {
        FireShots inst = BendingManager.find(player.getUUID(), FireShots.class);
        if (inst == null || inst.stock <= 0) {
            return;
        }
        inst.stock--;
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 dir = player.getLookAngle().normalize();
        inst.shots.add(new Shot(eye, dir, 0));
        player.serverLevel()
                .playSound(null, eye.x, eye.y, eye.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 1.2F);
        if (inst.stock <= 0) {
            cool(player.getUUID(), player.serverLevel());
        }
    }

    private Vec3 handPos(ServerPlayer sp) {
        Vec3 look = sp.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        return new Vec3(
                sp.getX() + side.x * 0.55 + look.x * 0.8, sp.getY() + 1.2, sp.getZ() + side.z * 0.55 + look.z * 0.8);
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        // Stock display: one flame orb per remaining shot at the hand.
        if (this.stock > 0) {
            Vec3 hand = handPos(player);
            level.sendParticles(
                    BendingTheme.particle(Config.FIRESHOTS_STOCK_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    hand.x,
                    hand.y,
                    hand.z,
                    Config.FIRESHOTS_STOCK_PARTICLE_COUNT.get() + this.stock,
                    0.15,
                    0.15,
                    0.15,
                    0.02);
        } else if (this.shots.isEmpty()) {
            return false;
        }
        for (int idx = 0; idx < this.shots.size(); idx++) {
            Shot s = this.shots.get(idx);
            Vec3 loc = s.pos().add(s.dir().scale(SPEED));
            double dist = s.dist() + SPEED;
            BlockPos bp = BlockPos.containing(loc);
            if (!level.isLoaded(bp) || dist > RANGE) {
                this.shots.remove(idx--);
                continue;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                this.shots.remove(idx--);
                continue;
            }
            this.shots.set(idx, new Shot(loc, s.dir(), dist));
            level.sendParticles(
                    BendingTheme.particle(Config.FIRESHOTS_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    loc.x,
                    loc.y,
                    loc.z,
                    Config.FIRESHOTS_FLAME_PARTICLE_COUNT.get(),
                    0.1,
                    0.1,
                    0.1,
                    0.02);
            level.sendParticles(
                    BendingTheme.particle(Config.FIRESHOTS_TRAIL_PARTICLE.get(), ParticleTypes.SMOKE),
                    loc.x,
                    loc.y,
                    loc.z,
                    Config.FIRESHOTS_TRAIL_PARTICLE_COUNT.get(),
                    0.1,
                    0.1,
                    0.1,
                    0.02);
            for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(HIT_RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living && this.affected.add(e.getUUID())) {
                    living.hurt(player.damageSources().magic(), DAMAGE);
                    living.igniteForSeconds(FIRE_SECONDS);
                } else {
                    e.igniteForSeconds(FIRE_SECONDS);
                }
                this.shots.remove(idx--);
                break;
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

    private static void cool(UUID owner, ServerLevel level) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }
}
