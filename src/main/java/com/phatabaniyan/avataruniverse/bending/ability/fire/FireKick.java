package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
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
 * Port of ProjectAvatar {@code FireKick}: click to snap a thirteen-stream
 * fan of flame down the gaze (-30 to +30 degrees in 5-degree steps). Each
 * head travels until it hits a wall, fluid, unloaded chunk or max range,
 * branding each living target once and shoving everything it touches.
 * Reference values: Cooldown 4000ms (80 ticks), Damage 3, Speed 1.2,
 * Range 20, Push 1.0, Radius 1.5.
 */
public class FireKick extends BendingAbility {
    public static final String ID = "FireKick";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FIREKICK_COOLDOWN_TICKS.get();

    private static final double DAMAGE = Config.FIREKICK_DAMAGE.get();
    private static final double SPEED = Config.FIREKICK_SPEED.get();
    private static final double RANGE = Config.FIREKICK_RANGE.get();
    private static final double PUSH = Config.FIREKICK_PUSH.get();
    private static final double RADIUS = Config.FIREKICK_RADIUS.get();

    private record Head(Vec3 pos, Vec3 dir, double dist) {}

    private final ServerLevel level;
    private final List<Head> heads = new ArrayList<>();
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public FireKick(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        for (int i = -30; i <= 30; i += 5) {
            double angle = Math.toRadians(i);
            Vec3 dir =
                    look.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).normalize();
            this.heads.add(new Head(eye, dir, 0));
        }
        cool(owner, level, ID, COOLDOWN_TICKS);
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 1.1F);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || !player.isAlive() || player.hasDisconnected()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (this.heads.isEmpty()) {
            return false;
        }
        for (int idx = 0; idx < this.heads.size(); idx++) {
            Head h = this.heads.get(idx);
            Vec3 loc = h.pos().add(h.dir().scale(SPEED));
            double dist = h.dist() + SPEED;
            BlockPos bp = BlockPos.containing(loc);
            if (!level.isLoaded(bp) || dist > RANGE) {
                this.heads.remove(idx--);
                continue;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                this.heads.remove(idx--);
                continue;
            }
            this.heads.set(idx, new Head(loc, h.dir(), dist));
            level.sendParticles(
                    BendingTheme.particle(Config.FIREKICK_TRAIL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    loc.x,
                    loc.y,
                    loc.z,
                    Config.FIREKICK_TRAIL_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.03);
            for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living && this.affected.add(e.getUUID())) {
                    living.hurt(player.damageSources().magic(), (float) DAMAGE);
                    living.igniteForSeconds(Config.FIREKICK_FIRE_SECONDS.get());
                }
                e.setDeltaMovement(h.dir().scale(PUSH));
                e.hurtMarked = true;
            }
        }
        return !this.heads.isEmpty();
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
