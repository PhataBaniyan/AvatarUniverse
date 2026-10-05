package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code EarthKick}
 * (addons/.../ability/earth/EarthKick.java): left click with a living
 * target in sight to shiver the earth toward them, down any horizontal
 * yaw. A train of 9 half-size earth cubes (reference MaxBlocks 9) starts 2
 * blocks ahead and glides single-file to the target, riding on top of the
 * soil and bobbing gently - no pop-in, no tearing, nothing to restore. The
 * head reaching the target hits for 3 damage with a forward pop and stops
 * the whole train. No cooldown: click again the moment it lands.
 * Reference values: MaxBlocks 9, hit radius 1.5.
 */
public class EarthKick extends EarthAbility {
    public static final String ID = "EarthKick";

    private static final float DAMAGE = Config.EARTHKICK_DAMAGE.get().floatValue();
    /** Reference MaxBlocks 9 cubes in the train. */
    private static final int COUNT = Config.EARTHKICK_COUNT.get();

    private static final double RANGE = Config.EARTHKICK_RANGE.get();
    /** The train starts this far ahead of the kicker. */
    private static final double START_AHEAD = Config.EARTHKICK_START_AHEAD.get();
    /** Cells the head glides per tick. */
    private static final double SPEED = Config.EARTHKICK_SPEED.get();
    /** Spacing between train cars, in cells. */
    private static final double SPACING = Config.EARTHKICK_SPACING.get();
    /** Half-size cubes, riding just above the soil. */
    private static final float SIZE = 0.5F;
    /** Reference hit radius 1.5. */
    private static final double HIT_RADIUS = Config.EARTHKICK_HIT_RADIUS.get();

    private static final double KNOCKBACK = Config.EARTHKICK_KNOCKBACK.get();
    private static final double KNOCKUP = Config.EARTHKICK_KNOCKUP.get();
    private static final double TRAVEL_RANGE = Config.EARTHKICK_TRAVEL_RANGE.get();
    private static final int MAX_TICKS = Config.msToTicks(Config.EARTHKICK_MAX_MS.get());

    private Vec3 direction;
    private final Vec3 start;
    private final List<Car> cars = new ArrayList<>();
    /** Head breadcrumb trail so cars corner through turns. */
    private final List<Vec3> trail = new ArrayList<>();

    private Vec3 head;
    private double traveled;
    private int step;
    private boolean valid;

    private static final class Car {
        final UUID id;
        /** Cells behind the head. */
        final double back;

        final BlockState texture;

        Car(UUID id, double back, BlockState texture) {
            this.id = id;
            this.back = back;
            this.texture = texture;
        }
    }

    public EarthKick(ServerPlayer player) {
        super(player);
        LivingEntity victim = findTarget(player);
        if (victim == null) {
            valid = false;
            direction = new Vec3(0, 0, 1);
            start = player.position();
            head = start;
            return;
        }
        Vec3 look = player.getLookAngle().normalize();
        Vec3 gaze = new Vec3(look.x, 0, look.z);
        if (gaze.lengthSqr() < 1.0e-6) {
            gaze = new Vec3(0, 0, 1);
        }
        this.direction = gaze.normalize();
        this.start = player.position().add(direction.scale(START_AHEAD));
        this.head = new Vec3(start.x, start.y, start.z);
        for (int k = COUNT; k >= 0; k--) {
            trail.add(start.subtract(direction.scale(k * SPACING)));
        }
        for (int i = 0; i < COUNT; i++) {
            Vec3 at = start.subtract(direction.scale(i * SPACING));
            BlockState texture = sampleTexture(at);
            net.minecraft.world.entity.Display.BlockDisplay disp = new net.minecraft.world.entity.Display.BlockDisplay(
                    net.minecraft.world.entity.EntityType.BLOCK_DISPLAY, level);
            setDisplayState(disp, texture);
            setSize(disp);
            disp.setPos(at.x - 0.25, rideHeight(at), at.z - 0.25);
            level.addFreshEntity(disp);
            cars.add(new Car(disp.getUUID(), i * SPACING, texture));
        }
        valid = true;
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.STONE_BREAK,
                SoundSource.PLAYERS,
                0.8F,
                1.2F);
    }

    @Override
    public String name() {
        return ID;
    }

    /** Click-start gate: a living target along the gaze yaw within range. No height factor. */
    public static LivingEntity findTarget(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flatLook = new Vec3(look.x, 0, look.z);
        if (flatLook.lengthSqr() < 1.0e-6) {
            return null;
        }
        flatLook = flatLook.normalize();
        LivingEntity best = null;
        double bestAlong = Double.MAX_VALUE;
        for (LivingEntity entity : player.serverLevel()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(eye.subtract(RANGE, RANGE, RANGE), eye.add(RANGE, RANGE, RANGE)),
                        LivingEntity::isAlive)) {
            if (entity.getUUID().equals(player.getUUID())) {
                continue;
            }
            Vec3 to = entity.position().subtract(eye);
            Vec3 flatTo = new Vec3(to.x, 0, to.z);
            double along = flatTo.dot(flatLook);
            if (along < 0.5 || along > RANGE) {
                continue;
            }
            if (flatTo.subtract(flatLook.scale(along)).length() <= 1.4 && along < bestAlong) {
                bestAlong = along;
                best = entity;
            }
        }
        return best;
    }

    /** Train texture from the soil under a point, dirt when there is none. */
    private BlockState sampleTexture(Vec3 at) {
        BlockPos probe = BlockPos.containing(at.x, at.y, at.z);
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = level.getBlockState(cell);
            if (Accretion.isEarthbendable(level, cell) && state.isSolid()) {
                return state;
            }
        }
        return Blocks.DIRT.defaultBlockState();
    }

    /** Glide height over the terrain: small cubes just above the soil. */
    private double rideHeight(Vec3 at) {
        Double top = surfaceTop(at);
        return top == null ? at.y : top + 0.05;
    }

    /** Soil top under a point, or null over the void. */
    private Double surfaceTop(Vec3 at) {
        BlockPos probe = BlockPos.containing(at.x, at.y, at.z);
        for (int dy = 4; dy >= -6; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = level.getBlockState(cell);
            if (!state.isAir() && (state.isSolid() || Accretion.isEarthbendable(level, cell))) {
                return (double) cell.getY() + 1.0;
            }
        }
        return null;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !valid) {
            return false;
        }
        // Controllable: the head chases the kicker's gaze yaw, so looking
        // around steers the train mid-flight like guiding it by hand.
        Vec3 look = player.getLookAngle().normalize();
        Vec3 want = new Vec3(look.x, 0, look.z);
        if (want.lengthSqr() > 1.0e-6) {
            want = want.normalize();
            direction = direction.scale(1 - 0.25).add(want.scale(0.25)).normalize();
        }
        head = head.add(direction.scale(SPEED));
        traveled += SPEED;
        // The logic point rides the terrain too, or slopes and ditches
        // never register hits even though the cubes visibly climb them.
        Double top = surfaceTop(head);
        if (top != null) {
            head = new Vec3(head.x, head.y + (top + 0.5 - head.y) * 0.5, head.z);
        }
        trail.add(new Vec3(head.x, head.y, head.z));
        while (trail.size() > 120) {
            trail.remove(0);
        }
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            if (!(level.getEntity(car.id) instanceof net.minecraft.world.entity.Display.BlockDisplay disp)) {
                continue;
            }
            int idx = Math.max(0, trail.size() - 1 - i);
            Vec3 at = trail.get(idx);
            double bob = 0.12 * Math.sin(step * 0.5 + i * 2.0);
            disp.setPos(at.x - 0.25, rideHeight(at) + bob, at.z - 0.25);
        }
        LivingEntity hit = nearestCar();
        if (hit != null) {
            entityHit(hit);
            burst(hit.position().add(0, 1, 0));
            return false;
        }
        if (step % 2 == 0) {
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                    head.x,
                    head.y + 0.5,
                    head.z,
                    2,
                    0.2,
                    0.2,
                    0.2,
                    0.01);
        }
        step++;
        return traveled <= TRAVEL_RANGE && step < MAX_TICKS;
    }

    /** Nearest mob to any live train car, planar: height plays no part. */
    private LivingEntity nearestCar() {
        LivingEntity best = null;
        double bestDist = HIT_RADIUS * HIT_RADIUS;
        for (Car car : cars) {
            if (!(level.getEntity(car.id) instanceof net.minecraft.world.entity.Display.BlockDisplay disp)) {
                continue;
            }
            Vec3 at = new Vec3(disp.getX() + 0.25, disp.getY() + 0.25, disp.getZ() + 0.25);
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class, new AABB(at.subtract(2, 8, 2), at.add(2, 8, 2)), LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                double dx = entity.getX() - at.x;
                double dz = entity.getZ() - at.z;
                double d = dx * dx + dz * dz;
                if (d < bestDist) {
                    bestDist = d;
                    best = entity;
                }
            }
        }
        return best;
    }

    private void entityHit(LivingEntity victim) {
        victim.hurt(player.damageSources().playerAttack(player), DAMAGE);
        Vec3 knock = direction.scale(KNOCKBACK).add(0, KNOCKUP, 0);
        victim.setDeltaMovement(victim.getDeltaMovement().add(knock));
        victim.hurtMarked = true;
    }

    private void burst(Vec3 at) {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                at.x,
                at.y,
                at.z,
                12,
                0.3,
                0.4,
                0.3,
                0.05);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void setDisplayState(net.minecraft.world.entity.Display.BlockDisplay disp, BlockState state) {
        disp.getEntityData().set(blockKey(), state);
    }

    /** Half-size cubes (a scale, tint simply stays default). */
    private static void setSize(net.minecraft.world.entity.Display.BlockDisplay disp) {
        disp.getEntityData().set(scaleKey(), new org.joml.Vector3f(SIZE, SIZE, SIZE));
    }

    @SuppressWarnings("unchecked")
    private static net.minecraft.network.syncher.EntityDataAccessor<BlockState> blockKey() {
        return (net.minecraft.network.syncher.EntityDataAccessor<BlockState>) BlockKeyHolder.KEY;
    }

    @SuppressWarnings("unchecked")
    private static net.minecraft.network.syncher.EntityDataAccessor<org.joml.Vector3f> scaleKey() {
        return (net.minecraft.network.syncher.EntityDataAccessor<org.joml.Vector3f>) ScaleKeyHolder.KEY;
    }

    private static final class BlockKeyHolder {
        static final Object KEY = dataKey(net.minecraft.world.entity.Display.BlockDisplay.class, "DATA_BLOCK_STATE_ID");
    }

    private static final class ScaleKeyHolder {
        static final Object KEY = dataKey(net.minecraft.world.entity.Display.class, "DATA_SCALE_ID");
    }

    private static Object dataKey(Class<?> owner, String field) {
        try {
            java.lang.reflect.Field f = owner.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRemove() {
        for (Car car : cars) {
            if (level.getEntity(car.id) instanceof net.minecraft.world.entity.Display.BlockDisplay disp) {
                disp.discard();
            }
        }
        cars.clear();
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }
}
