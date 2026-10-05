package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shockwave's rolling earth wave, rebuilt clean from the original
 * Ripple's motion (core/.../earthbending/Ripple.java): a dense contiguous
 * ring racing outward one cell per tick, ground-snapped so it climbs and
 * drops with the terrain. The swell itself is Torrent-style - crest ground
 * pops up as falling blocks on small arcs and drops straight back home a
 * few ticks later, so the front glides instead of snapping. Starts 3 blocks
 * out, runs to range 15; anything the crest catches takes 4 damage with a
 * massive radial shove, once per cast. Holes always restore, so the ground
 * is never griefed.
 */
public class ShockwaveRing extends EarthAbility {
    public static final String ID = "Shockwave";

    private static final double RANGE = Config.SHOCKWAVE_RING_RANGE.get();
    private static final double START_RADIUS = Config.SHOCKWAVE_RING_START_RADIUS.get();
    private static final float DAMAGE = Config.SHOCKWAVE_RING_DAMAGE.get().floatValue();
    /** Massive shove for anyone the crest catches. */
    private static final double KNOCKBACK = Config.SHOCKWAVE_RING_KNOCKBACK.get();
    /** Cells the ring races per tick, like the original. */
    private static final double SPEED = Config.SHOCKWAVE_RING_SPEED.get();
    /** Crest thickness in cells. */
    private static final int BAND = Config.SHOCKWAVE_RING_BAND.get();
    /** Ticks a popped block flies before its hole restores. */
    private static final long HOP_TICKS = Config.SHOCKWAVE_RING_HOP_TICKS.get();
    /** Hop shape: gentle up-pop with a breath of outward drift. */
    private static final double UP_POP = Config.SHOCKWAVE_RING_UP_POP.get();

    private static final double OUT_DRIFT = Config.SHOCKWAVE_RING_OUT_DRIFT.get();

    private final Vec3 center;
    private final int centerY;
    /** Open holes awaiting restore, in lift order. */
    private final Map<BlockPos, Hole> holes = new LinkedHashMap<>();
    /** Flying crest blocks. */
    private final List<Flight> flights = new ArrayList<>();

    private final Set<UUID> struck = new HashSet<>();
    private int step;

    private static final class Hole {
        final BlockState state;
        final long liftedAt;

        Hole(BlockState state, long liftedAt) {
            this.state = state;
            this.liftedAt = liftedAt;
        }
    }

    private static final class Flight {
        final UUID id;
        final BlockPos hole;
        int age;

        Flight(UUID id, BlockPos hole) {
            this.id = id;
            this.hole = hole;
        }
    }

    public ShockwaveRing(ServerPlayer player) {
        super(player);
        this.center = player.position();
        this.centerY = player.blockPosition().getY();
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
        double radius = START_RADIUS + step * SPEED;
        if (radius <= RANGE + BAND) {
            expand(radius);
        }
        flyHome();
        restoreDue();
        strike(radius);
        if (step % 6 == 0) {
            level.playSound(
                    null, center.x, center.y, center.z, SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 0.5F, 0.6F);
        }
        step++;
        return radius <= RANGE + BAND + HOP_TICKS;
    }

    /** Pop one contiguous band at the current radius. */
    private void expand(double radius) {
        int slices = Math.max(16, (int) (2 * Math.PI * radius) + 1);
        for (int s = 0; s < slices; s++) {
            double theta = 2 * Math.PI * s / slices;
            Vec3 dir = new Vec3(Math.cos(theta), 0, Math.sin(theta));
            for (int b = 0; b < BAND; b++) {
                double rr = radius - b;
                if (rr < START_RADIUS) {
                    continue;
                }
                BlockPos ground = snap(center.add(dir.scale(rr)));
                if (ground != null) {
                    pop(ground, dir);
                }
            }
        }
    }

    /** Topmost bendable ground with headroom, near the fire level. */
    private BlockPos snap(Vec3 at) {
        BlockPos probe = BlockPos.containing(at.x, center.y, at.z);
        for (int dy = 3; dy >= -3; dy--) {
            BlockPos ground = probe.offset(0, dy, 0);
            if (Math.abs(ground.getY() - centerY) > 4) {
                continue;
            }
            BlockState state = level.getBlockState(ground);
            BlockState cap = level.getBlockState(ground.above());
            if (state.isAir() || !Accretion.isEarthbendable(level, ground)) {
                continue;
            }
            if ((!cap.isAir() && cap.isSolid()) || !cap.getFluidState().isEmpty()) {
                continue;
            }
            return ground.immutable();
        }
        return null;
    }

    /** Knock one ground block into its hop, tracking the hole home. */
    private void pop(BlockPos ground, Vec3 radial) {
        if (holes.containsKey(ground)) {
            return;
        }
        BlockState state = level.getBlockState(ground);
        FallingBlockEntity fb = FallingBlockEntity.fall(level, ground, state);
        fb.setDeltaMovement(radial.scale(OUT_DRIFT).add(0, UP_POP, 0));
        holes.put(ground, new Hole(state, level.getGameTime()));
        flights.add(new Flight(fb.getUUID(), ground));
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                ground.getX() + 0.5,
                ground.getY() + 1.0,
                ground.getZ() + 0.5,
                1,
                0.2,
                0.2,
                0.2,
                0.01);
    }

    /** Land old hops: hole restores, block is gone either way. */
    private void flyHome() {
        long now = level.getGameTime();
        for (Flight flight : new ArrayList<>(flights)) {
            flight.age++;
            boolean done = flight.age >= HOP_TICKS;
            if (!done && level.getEntity(flight.id) instanceof FallingBlockEntity fb) {
                done = fb.onGround();
            }
            if (!done) {
                continue;
            }
            if (level.getEntity(flight.id) instanceof FallingBlockEntity fb) {
                fb.discard();
            }
            restore(flight.hole);
            flights.remove(flight);
        }
    }

    /** Backstop: any hole open too long restores (lost entity, logout edge). */
    private void restoreDue() {
        long now = level.getGameTime();
        for (Map.Entry<BlockPos, Hole> entry : new ArrayList<>(holes.entrySet())) {
            if (now - entry.getValue().liftedAt >= HOP_TICKS) {
                restore(entry.getKey());
            }
        }
    }

    /** Fill one hole with its own original, never anything else's build. */
    private void restore(BlockPos pos) {
        Hole hole = holes.remove(pos);
        if (hole == null) {
            return;
        }
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, hole.state, 2);
            level.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, hole.state),
                    pos.getX() + 0.5,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5,
                    2,
                    0.25,
                    0.15,
                    0.25,
                    0.02);
        }
    }

    /** Hit anything the crest band touches, once per cast. */
    private void strike(double radius) {
        Vec3 corner = new Vec3(RANGE + 3, 4, RANGE + 3);
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class, new AABB(center.subtract(corner), center.add(corner)), LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner) || !struck.add(entity.getUUID())) {
                continue;
            }
            double dist = Math.hypot(entity.getX() - center.x, entity.getZ() - center.z);
            if (Math.abs(dist - radius) > BAND || Math.abs(entity.getY() - center.y) > 3.0) {
                struck.remove(entity.getUUID());
                continue;
            }
            Vec3 push = new Vec3(entity.getX() - center.x, 0, entity.getZ() - center.z);
            if (push.lengthSqr() < 1.0e-6) {
                push = new Vec3(1, 0, 0);
            }
            push = new Vec3(push.x, 0.5, push.z).normalize().scale(KNOCKBACK);
            entity.setDeltaMovement(push);
            entity.hurtMarked = true;
            entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
        }
    }

    @Override
    public void onRemove() {
        for (Flight flight : new ArrayList<>(flights)) {
            if (level.getEntity(flight.id) instanceof FallingBlockEntity fb) {
                fb.discard();
            }
        }
        flights.clear();
        for (BlockPos pos : new ArrayList<>(holes.keySet())) {
            restore(pos);
        }
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }
}
