package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of JedCore {@code LavaDisc}
 * (jedcore/.../earthbending/LavaDisc.java): sneak holding lava (or earth)
 * in sight within 4 to spin a lava disc at your fingertips; release to
 * throw it steering with your gaze, sneak mid-flight to recall it (3
 * recalls), anything touched takes 4 damage plus fire. The disc melts a
 * lava trail through earth and wood that heals itself. Reference values:
 * Cooldown 7000ms, Duration 1000ms, Damage 4, Particles 3, RecallLimit 3,
 * RegenTime 5000ms, Source Regen 10000ms, Source Range 4. Reference gates
 * on lavabending; here any earthbender may use it.
 */
public class LavaDisc extends EarthAbility {
    public static final String ID = "LavaDisc";

    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.LAVADISC_COOLDOWN_MS.get());
    /** Reference Duration 1000ms of flight, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.LAVADISC_DURATION_MS.get());

    private static final float DAMAGE = Config.LAVADISC_DAMAGE.get().floatValue();
    private static final int PARTICLES = Config.LAVADISC_PARTICLES.get();
    private static final int RECALL_LIMIT = Config.LAVADISC_RECALL_LIMIT.get();
    /** Reference Destroy.RegenTime 5000ms, in server ticks. */
    private static final long TRAIL_REVERT_TICKS = Config.msToTicks(Config.LAVADISC_TRAIL_REVERT_MS.get());
    /** Reference Source.RegenTime 10000ms, in server ticks. */
    private static final long SOURCE_REVERT_TICKS = Config.msToTicks(Config.LAVADISC_SOURCE_REVERT_MS.get());

    private static final double SOURCE_RANGE = Config.LAVADISC_SOURCE_RANGE.get();
    private static final double SPEED = Config.LAVADISC_SPEED.get();
    private static final int FIRE_TICKS = Config.msToTicks(Config.LAVADISC_FIRE_MS.get());
    private static final double HOLD_DISTANCE = Config.LAVADISC_HOLD_DISTANCE.get();
    private static final double HIT_RADIUS = Config.LAVADISC_HIT_RADIUS.get();

    private enum State {
        HOLD,
        FLY,
        RECALL
    }

    private State state = State.HOLD;
    private Vec3 disc;
    private Vec3 direction;
    private final long bornTick;
    private long flyStart;
    private int recalls;
    private int angle;
    private final List<TempBlock> trail = new ArrayList<>();

    public LavaDisc(ServerPlayer player) {
        super(player);
        this.bornTick = player.level().getGameTime();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        this.disc = eye.add(look.scale(HOLD_DISTANCE));
        this.direction = new Vec3(look.x, look.y, look.z);
        claimSource();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: lava preferred, else earth, in sight within range. */
    public static boolean canBegin(ServerPlayer player) {
        return findSource(player) != null;
    }

    private static BlockPos findSource(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        BlockPos lava = null;
        for (double d = 0.5; d <= SOURCE_RANGE; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            BlockState state = player.serverLevel().getBlockState(pos);
            if (state.isAir() || TempBlock.isTemp(player.serverLevel(), pos)) {
                continue;
            }
            if (state.getFluidState().is(Fluids.LAVA)) {
                return pos.immutable();
            }
            if (lava == null && Accretion.isEarthbendable(player.serverLevel(), pos)) {
                lava = pos.immutable();
            }
        }
        return lava;
    }

    private void claimSource() {
        BlockPos source = findSource(player);
        if (source == null) {
            return;
        }
        TempBlock temp = new TempBlock(level, source, Blocks.LAVA.defaultBlockState(), TempBlock.QUIET);
        BendingManager.scheduleRevert(temp, level.getGameTime() + SOURCE_REVERT_TICKS);
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !ID.equalsIgnoreCase(activeBound())) {
            return cleanup();
        }
        if (state == State.HOLD) {
            disc = holdPoint();
            render(disc, false);
            if (!player.isShiftKeyDown()) {
                state = State.FLY;
                flyStart = level.getGameTime();
                direction = player.getLookAngle().normalize();
            }
            return true;
        }
        if (state == State.RECALL) {
            if (!player.isShiftKeyDown()) {
                state = State.FLY;
                flyStart = level.getGameTime();
                return true;
            }
            Vec3 home = holdPoint();
            Vec3 to = home.subtract(disc);
            if (to.lengthSqr() < 0.25) {
                recalls++;
                state = State.HOLD;
                return true;
            }
            direction = to.normalize();
            if (!glide()) {
                return cleanup();
            }
            render(disc, true);
            return true;
        }
        if (level.getGameTime() - flyStart > DURATION_TICKS || !safe(disc)) {
            return cleanup();
        }
        if (player.isShiftKeyDown() && recalls < RECALL_LIMIT) {
            state = State.RECALL;
            return true;
        }
        steer();
        if (!glide()) {
            return cleanup();
        }
        render(disc, true);
        return true;
    }

    /** Held disc point 3 ahead of the eyes, pulled out of solids. */
    private Vec3 holdPoint() {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 at = eye.add(look.scale(HOLD_DISTANCE));
        for (int i = 0; i < 6; i++) {
            BlockPos pos = BlockPos.containing(at);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.isSolid()) {
                break;
            }
            at = at.subtract(look.scale(0.5));
            if (at.distanceToSqr(eye) > HOLD_DISTANCE * HOLD_DISTANCE) {
                break;
            }
        }
        return at;
    }

    /** Gaze steering with the reference +-20 degree pitch clamp. */
    private void steer() {
        Vec3 look = player.getLookAngle().normalize();
        double pitch = Math.asin(Math.max(-1.0, Math.min(1.0, look.y)));
        double clamped = Math.max(Math.toRadians(-20), Math.min(Math.toRadians(20), pitch));
        double yaw = Math.atan2(-look.x, look.z);
        double horizontal = Math.cos(clamped);
        direction = new Vec3(-Math.sin(yaw) * horizontal, Math.sin(clamped), Math.cos(yaw) * horizontal);
    }

    /** Fly in 0.15 substeps, hurting anything within 2 (reference move). */
    private boolean glide() {
        for (int i = 0; i < 5; i++) {
            disc = disc.add(direction.scale(0.15));
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(
                            disc.subtract(HIT_RADIUS, HIT_RADIUS, HIT_RADIUS),
                            disc.add(HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                if (entity.position().distanceToSqr(disc) > HIT_RADIUS * HIT_RADIUS
                        && entity.getEyePosition().distanceToSqr(disc) > HIT_RADIUS * HIT_RADIUS) {
                    continue;
                }
                entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
                entity.setRemainingFireTicks(FIRE_TICKS);
                level.sendParticles(
                        BendingTheme.particle(Config.LAVADISC_GLIDE_PARTICLE.get(), ParticleTypes.LAVA),
                        disc.x,
                        disc.y,
                        disc.z,
                        Config.LAVADISC_GLIDE_PARTICLE_COUNT.get(),
                        0.3,
                        0.3,
                        0.3,
                        0.1);
                return false;
            }
        }
        return true;
    }

    private boolean safe(Vec3 at) {
        return at.y >= 2.0 && at.y <= level.getMaxBuildHeight() - 1;
    }

    /** Spinning lava/flame/smoke rings plus the melting trail. */
    private void render(Vec3 at, boolean large) {
        level.sendParticles(
                BendingTheme.particle(Config.LAVADISC_RENDER_PARTICLE.get(), ParticleTypes.LAVA),
                at.x,
                at.y,
                at.z,
                large ? Config.LAVADISC_RENDER_PARTICLE_COUNT.get() : 1,
                0.3,
                0.3,
                0.3,
                0.1);
        angle = (angle + 8) % 360;
        ring(at, 20, 1.0, large);
        for (int i = 0; i < 10; i++) {
            double theta = Math.toRadians(angle) + 2 * Math.PI * i / 10;
            Vec3 p = new Vec3(at.x + Math.cos(theta) * 0.5, at.y, at.z + Math.sin(theta) * 0.5);
            level.sendParticles(
                    BendingTheme.particle(Config.LAVADISC_FLAME_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    p.x,
                    p.y,
                    p.z,
                    Config.LAVADISC_FLAME_PARTICLE_COUNT.get(),
                    0,
                    0,
                    0,
                    0.01);
            level.sendParticles(
                    BendingTheme.particle(Config.LAVADISC_SMOKE_PARTICLE.get(), ParticleTypes.SMOKE),
                    p.x,
                    p.y,
                    p.z,
                    Config.LAVADISC_SMOKE_PARTICLE_COUNT.get(),
                    0,
                    0,
                    0,
                    0.05);
            if (large) {
                melt(p);
            }
        }
    }

    private void ring(Vec3 at, int points, double radius, boolean large) {
        DustParticleOptions ember = new DustParticleOptions(new Vector3f(196 / 255.0F, 93 / 255.0F, 0), 1.0F);
        for (int i = 0; i < points; i++) {
            double theta = Math.toRadians(angle) + 2 * Math.PI * i / points;
            Vec3 p = new Vec3(at.x + Math.cos(theta) * radius, at.y, at.z + Math.sin(theta) * radius);
            level.sendParticles(ember, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            if (large) {
                melt(p);
            }
        }
    }

    /** Melt earth, metal and wood into timed lava along the trail. */
    private void melt(Vec3 at) {
        BlockPos pos = BlockPos.containing(at);
        if (TempBlock.isTemp(level, pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return;
        }
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        boolean meltable = Accretion.isEarthbendable(level, pos)
                || isMetalState(state)
                || key.contains("COBBLESTONE")
                || key.contains("_LOG")
                || key.contains("_PLANKS");
        if (!meltable) {
            return;
        }
        TempBlock temp = new TempBlock(level, pos, Blocks.LAVA.defaultBlockState(), TempBlock.QUIET);
        trail.add(temp);
        BendingManager.scheduleRevert(temp, level.getGameTime() + TRAIL_REVERT_TICKS);
        level.sendParticles(
                BendingTheme.particle(Config.LAVADISC_MELT_PARTICLE.get(), ParticleTypes.LAVA),
                at.x,
                at.y,
                at.z,
                Config.LAVADISC_MELT_PARTICLE_COUNT.get(),
                0.3,
                0.3,
                0.3,
                0.2);
    }

    /** End the flight, keep the scheduled trail reverts, take the cooldown. */
    private boolean cleanup() {
        cool(owner, player, ID, COOLDOWN_TICKS);
        return false;
    }

    @Override
    public void onRemove() {}

    private String activeBound() {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending == null ? "" : bending.boundAbility(player.getInventory().selected + 1);
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
