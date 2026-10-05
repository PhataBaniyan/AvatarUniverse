package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
 * Port of ProjectAvatar {@code Discharge} (JedCore port).
 * Click to crawl jittering branches forward from the eyes. Branches split
 * every 3rd space, wander with a growing branchSpace, and the first touch
 * shocks + shoves. Reference values: Cooldown 3000ms (60 ticks), Duration
 * 2000ms (40 ticks), Damage 4, Range 18, HitRadius 1.5.
 */
public class Discharge extends BendingAbility {
    public static final String ID = "Discharge";

    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.DISCHARGE_COOLDOWN_MS.get());
    /** Reference Duration 2000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.DISCHARGE_DURATION_MS.get());

    private static final double DAMAGE = Config.DISCHARGE_DAMAGE.get();
    private static final double RANGE = Config.DISCHARGE_RANGE.get();
    private static final double HIT_RADIUS = Config.DISCHARGE_HIT_RADIUS.get();
    private static final int SPLIT_INTERVAL = Config.DISCHARGE_SPLIT_INTERVAL.get();
    private static final int STEPS_PER_TICK = Config.DISCHARGE_STEPS_PER_TICK.get();
    private static final double STEP_LENGTH = Config.DISCHARGE_STEP_LENGTH.get();
    private static final double KNOCKBACK = Config.DISCHARGE_KNOCKBACK.get();
    private static final int IGNITE_SECONDS = Config.DISCHARGE_IGNITE_MS.get() / 1000;

    private final ServerPlayer player;
    private final ServerLevel level;
    private final Map<Integer, Vec3> branches = new HashMap<>();
    private final Map<Integer, Double> travelled = new HashMap<>();
    private int nextId = 1;
    private double branchSpace = 0.2;
    private int spaces = 0;
    private boolean hit = false;
    private final Random rand = new Random();
    private final Vec3 direction;

    public Discharge(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        this.direction = player.getLookAngle().normalize();
        this.branches.put(this.nextId, eye);
        this.travelled.put(this.nextId, 0.0);
        this.nextId++;
        cool(owner, level, ID, COOLDOWN_TICKS);
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.6F, 1.4F);
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
        if (this.branches.isEmpty() || this.hit || level.getGameTime() - this.startTime > DURATION_TICKS) {
            return false;
        }
        this.spaces++;
        // Every 3rd space the first branch buds a child, like the reference.
        if (this.spaces % SPLIT_INTERVAL == 0 && !this.branches.isEmpty()) {
            Integer first = this.branches.keySet().iterator().next();
            Vec3 at = this.branches.get(first);
            this.branches.put(this.nextId, at);
            this.travelled.put(this.nextId, this.travelled.getOrDefault(first, 0.0));
            this.nextId++;
        }
        List<Integer> cleanup = new ArrayList<>();
        for (Map.Entry<Integer, Vec3> e : new HashMap<>(this.branches).entrySet()) {
            int id = e.getKey();
            Vec3 origin = e.getValue();
            if (origin == null) {
                cleanup.add(id);
                continue;
            }
            BlockPos bp = BlockPos.containing(origin);
            if (level.isLoaded(bp)) {
                var state = level.getBlockState(bp);
                if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                    cleanup.add(id);
                    continue;
                }
            }
            Vec3 l = origin.add(new Vec3(createBranch(), createBranch(), createBranch()));
            this.branchSpace += 0.001;
            double dist = this.travelled.getOrDefault(id, 0.0);
            for (int j = 0; j < STEPS_PER_TICK; j++) {
                level.sendParticles(
                        BendingTheme.particle(Config.DISCHARGE_TRAIL_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                        l.x,
                        l.y,
                        l.z,
                        Config.DISCHARGE_TRAIL_PARTICLE_COUNT.get(),
                        0.0,
                        0.0,
                        0.0,
                        0.0);
                if (this.rand.nextInt(3) == 0) {
                    level.playSound(null, l.x, l.y, l.z, SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 0.4F, 1.2F);
                }
                for (Entity ent : level.getEntities(player, new AABB(l, l).inflate(HIT_RADIUS))) {
                    if (ent.getUUID().equals(player.getUUID()) || ent instanceof ArmorStand) {
                        continue;
                    }
                    if (ent instanceof LivingEntity living) {
                        Vec3 push = ent.position().subtract(l);
                        if (push.lengthSqr() < 1.0e-4) {
                            push = new Vec3(0, 1, 0);
                        }
                        ent.setDeltaMovement(push.normalize().scale(KNOCKBACK));
                        ent.hurtMarked = true;
                        living.hurt(player.damageSources().lightningBolt(), (float) DAMAGE);
                        living.igniteForSeconds(IGNITE_SECONDS);
                        level.sendParticles(
                                BendingTheme.particle(
                                        Config.DISCHARGE_HIT_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                                ent.getX(),
                                ent.getY() + 1,
                                ent.getZ(),
                                Config.DISCHARGE_HIT_PARTICLE_COUNT.get(),
                                0.3,
                                0.3,
                                0.3,
                                0.05);
                        this.hit = true;
                        break;
                    }
                }
                if (this.hit) {
                    break;
                }
                l = l.add(this.direction.scale(STEP_LENGTH));
                dist += STEP_LENGTH;
                if (dist > RANGE) {
                    break;
                }
            }
            if (this.hit || dist > RANGE) {
                cleanup.add(id);
                continue;
            }
            this.branches.put(id, l);
            this.travelled.put(id, dist);
        }
        for (int id : cleanup) {
            this.branches.remove(id);
            this.travelled.remove(id);
        }
        return !this.branches.isEmpty() && !this.hit;
    }

    private double createBranch() {
        int i = this.rand.nextInt(3);
        if (i == 0) {
            return this.branchSpace;
        } else if (i == 2) {
            return -this.branchSpace;
        }
        return 0.0;
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
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
