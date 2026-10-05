package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
 * Port of ProjectAvatar {@code AirPunch} (JedCore port): click to jab a quick
 * bolt of wind; keep clicking and the volley keeps coming until the flurry
 * window lapses. Like the source this is reentrant across clicks: a new click
 * while one is live adds a shot to the live instance instead of starting
 * over, so there is intentionally no singleton guard here.
 * Reference values: Cooldown 1500ms (30 ticks), Flurry 800ms (16 ticks),
 * Shots 5, Range 20, Damage 2, HitRadius 1.
 */
public class AirPunch extends BendingAbility {
    public static final String ID = "AirPunch";

    /** Reference Cooldown 1500ms, in server ticks. */
    private static final int COOLDOWN_TICKS = Config.msToTicks(Config.AIRPUNCH_COOLDOWN_MS.get());
    /** Reference flurry window 800ms, in server ticks. */
    private static final int THRESHOLD_TICKS = Config.msToTicks(Config.AIRPUNCH_THRESHOLD_MS.get());

    private static final int SHOTS = Config.AIRPUNCH_SHOTS.get();
    private static final double RANGE = Config.AIRPUNCH_RANGE.get();
    private static final double DAMAGE = Config.AIRPUNCH_DAMAGE.get();
    private static final double HIT_RADIUS = Config.AIRPUNCH_HIT_RADIUS.get();
    private static final double PUSH = Config.AIRPUNCH_PUSH.get();

    private record Bolt(Vec3 pos, Vec3 dir, double dist) {}

    private final ServerLevel level;
    private int shots = SHOTS;
    private long lastShotTick;
    private boolean started = false;
    private final Map<Bolt, Double> bolts = new ConcurrentHashMap<>();
    private final Set<UUID> affected = ConcurrentHashMap.newKeySet();

    public AirPunch(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        // Reentrant flurry: a click while one lives feeds the live instance.
        AirPunch old = BendingManager.find(player.getUUID(), AirPunch.class);
        if (old != null) {
            old.createShot(player.serverLevel().getGameTime());
            return;
        }
        if (player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null && bending.isOnCooldown(ID, player.level().getGameTime())) {
            return;
        }
        this.started = true;
        createShot(player.level().getGameTime());
    }

    private void createShot(long gameTime) {
        if (this.shots < 1) {
            return;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null) {
            return;
        }
        this.lastShotTick = gameTime;
        this.shots--;
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 pos = new Vec3(player.getX(), player.getEyeY(), player.getZ()).add(dir.scale(1.5));
        this.bolts.put(new Bolt(pos, dir, 0), 0.0);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.4F, 1.6F);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!started) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        progressBolts(player);
        long gameTime = level.getGameTime();
        if (this.shots == 0 || gameTime > this.lastShotTick + THRESHOLD_TICKS) {
            BendingPlayer bending = BendingPlayer.get(owner);
            if (bending != null && !bending.isOnCooldown(ID, gameTime)) {
                bending.setCooldown(ID, gameTime + COOLDOWN_TICKS);
            }
            if (this.bolts.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void progressBolts(ServerPlayer player) {
        for (Bolt bolt : new ArrayList<>(this.bolts.keySet())) {
            Vec3 loc = bolt.pos();
            double dist = bolt.dist();
            boolean dead = false;
            for (int i = 0; i < 3 && !dead; i++) {
                dist += 1;
                if (dist >= RANGE) {
                    dead = true;
                    break;
                }
                loc = loc.add(bolt.dir());
                BlockPos bp = BlockPos.containing(loc);
                if (!level.isLoaded(bp)) {
                    dead = true;
                    break;
                }
                var state = level.getBlockState(bp);
                if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                    dead = true;
                    break;
                }
                level.sendParticles(
                        BendingTheme.particle(Config.AIRPUNCH_BOLT_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                        loc.x,
                        loc.y,
                        loc.z,
                        Config.AIRPUNCH_BOLT_PARTICLE_COUNT.get(),
                        0.1,
                        0.1,
                        0.1,
                        0.01);
                for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(HIT_RADIUS))) {
                    if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                        continue;
                    }
                    if (e instanceof LivingEntity living && this.affected.add(e.getUUID())) {
                        living.hurt(player.damageSources().magic(), (float) DAMAGE);
                    }
                    e.setDeltaMovement(bolt.dir().scale(PUSH));
                    e.hurtMarked = true;
                    dead = true;
                    break;
                }
            }
            this.bolts.remove(bolt);
            if (!dead) {
                this.bolts.put(new Bolt(loc, bolt.dir(), dist), 0.0);
            }
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }
}
