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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FireSpin}: click to detonate a full circle
 * of fire streams ripping outward in every horizontal direction (one head
 * per 5 degrees). Each head travels until it hits a wall, fluid, unloaded
 * chunk or max range; every third head brands and shoves what it touches.
 * The ring scorches the ground under the caster on ignition. Reference
 * values: Cooldown 5000ms (100 ticks), Damage 3, Speed 1.0, Range 16,
 * Push 1.5, Radius 1.5.
 */
public class FireSpin extends BendingAbility {
    public static final String ID = "FireSpin";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.FIRESPIN_COOLDOWN_MS.get());

    private static final double DAMAGE = Config.FIRESPIN_DAMAGE.get();
    private static final double SPEED = Config.FIRESPIN_SPEED.get();
    private static final double RANGE = Config.FIRESPIN_RANGE.get();
    private static final double PUSH = Config.FIRESPIN_PUSH.get();
    private static final double RADIUS = Config.FIRESPIN_RADIUS.get();

    private record Head(Vec3 pos, Vec3 dir, double dist) {}

    private final ServerLevel level;
    private final List<Head> heads = new ArrayList<>();
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public FireSpin(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        Vec3 origin = player.position().add(0, 1, 0);
        for (int i = 0; i < 360; i += 5) {
            double a = Math.toRadians(i);
            this.heads.add(new Head(origin, new Vec3(Math.cos(a), 0, Math.sin(a)), 0));
        }
        cool(owner, level, ID, COOLDOWN_TICKS);
        level.playSound(
                null, origin.x, origin.y, origin.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.2F);
        // The ring happens around the caster: scorch the ground it stands on.
        scorchGround(
                level,
                origin.add(0, -1, 0),
                Config.FIRESPIN_SCORCH_RADIUS.get(),
                Config.FIRESPIN_SCORCH_MAX.get(),
                Config.FIRESPIN_SCORCH_TRIES.get());
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
                    BendingTheme.particle(Config.FIRESPIN_TRAIL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    loc.x,
                    loc.y,
                    loc.z,
                    Config.FIRESPIN_TRAIL_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.03);
            if (idx % 3 == 0) {
                for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(RADIUS))) {
                    if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                        continue;
                    }
                    if (e instanceof LivingEntity living && this.affected.add(e.getUUID())) {
                        living.hurt(player.damageSources().magic(), (float) DAMAGE);
                        living.igniteForSeconds(Config.FIRESPIN_FIRE_MS.get() / 1000);
                    }
                    e.setDeltaMovement(h.dir().scale(PUSH));
                    e.hurtMarked = true;
                }
            }
        }
        return !this.heads.isEmpty();
    }

    /**
     * Set the ground alight where the sphere touches down. Inline copy of
     * ProjectAvatar {@code FireBurst.scorchGround} (same random-disc
     * sampling, same air-over-solid placement, max 32 of 110 tries here).
     */
    static void scorchGround(ServerLevel level, Vec3 center, double radius, int max, int tryCount) {
        int lit = 0;
        int n = 0;
        var random = level.random;
        while (lit < max && n < tryCount) {
            n++;
            double a = random.nextDouble() * Math.PI * 2;
            double d = radius * Math.sqrt(random.nextDouble());
            double px = center.x + Math.cos(a) * d;
            double pz = center.z + Math.sin(a) * d;
            for (int dy = 3; dy >= -5; dy--) {
                BlockPos p = BlockPos.containing(px, center.y + dy, pz);
                if (!level.isLoaded(p) || !level.isLoaded(p.below())) {
                    break;
                }
                if (level.getBlockState(p).isAir()
                        && level.getBlockState(p.below()).isSolidRender(level, p.below())) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                    lit++;
                    break;
                }
                if (level.getBlockState(p).isSolidRender(level, p)) {
                    break;
                }
            }
        }
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
