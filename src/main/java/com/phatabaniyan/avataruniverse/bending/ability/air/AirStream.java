package com.phatabaniyan.avataruniverse.bending.ability.air;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code AirStream}: hand-cast carry stream — hold
 * sneak and a slow stream pours from the hand where you look. Anything in
 * its path is caught and held, riding the head until you let go, then
 * gravity takes them back.
 * Reference values: Cooldown 6000ms (120 ticks), Speed 0.5, Range 25,
 * CarryHeight 5.
 */
public class AirStream extends BendingAbility {
    public static final String ID = "AirStream";

    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.AIRSTREAM_COOLDOWN_TICKS.get();

    private static final double SPEED = Config.AIRSTREAM_SPEED.get();
    private static final double RANGE = Config.AIRSTREAM_RANGE.get();
    private static final double CARRY_HEIGHT = Config.AIRSTREAM_CARRY_HEIGHT.get();
    private static final double CATCH_RADIUS = Config.AIRSTREAM_CATCH_RADIUS.get();

    private final ServerLevel level;
    private Vec3 head;
    private final List<Entity> held = new ArrayList<>();
    private double ringAngle = 0;

    public AirStream(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.head = handPos(player);
        cool(owner, level, ID, COOLDOWN_TICKS);
    }

    @Override
    public String name() {
        return ID;
    }

    private static Vec3 handPos(ServerPlayer player) {
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        return new Vec3(
                eye.x + look.x * 0.4 + side.x * 0.35, eye.y - 0.3 + look.y * 0.4, eye.z + look.z * 0.4 + side.z * 0.35);
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            // Let go: everyone drops where they hang.
            return false;
        }
        Vec3 hand = handPos(player);
        Vec3 look = player.getLookAngle().normalize();
        this.head = this.head.add(look.scale(SPEED));
        if (player.position().distanceToSqr(this.head) > RANGE * RANGE) {
            this.head = hand.add(look.scale(RANGE));
        }
        BlockPos bp = BlockPos.containing(this.head);
        if (level.isLoaded(bp)) {
            var state = level.getBlockState(bp);
            if (!state.isSolidRender(level, bp)
                    && state.getFluidState().isEmpty()
                    && this.head.y - hand.y <= CARRY_HEIGHT) {
                drawStream(hand);
                catchAndHold(player, hand, look);
                return true;
            }
        }
        // Head hit a wall, water, or the height cap: pin it, keep holding.
        drawStream(hand);
        catchAndHold(player, hand, look);
        return true;
    }

    private void drawStream(Vec3 hand) {
        double dist = hand.distanceTo(this.head);
        Vec3 dir = dist < 1.0e-4 ? new Vec3(0, 0, 0) : this.head.subtract(hand).normalize();
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize();
        Vec3 up = side.cross(dir).normalize();
        this.ringAngle += 0.7;
        double steps = Math.max(1, dist / 2.0);
        for (double s = 0; s <= steps; s++) {
            Vec3 p = hand.add(dir.scale(dist * s / steps));
            double a = this.ringAngle + s * 1.1;
            Vec3 ring = p.add(side.scale(Math.cos(a) * 0.45)).add(up.scale(Math.sin(a) * 0.45));
            level.sendParticles(
                    BendingTheme.particle(Config.AIRSTREAM_STREAM_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    ring.x,
                    ring.y,
                    ring.z,
                    Config.AIRSTREAM_STREAM_PARTICLE_COUNT.get(),
                    0.08,
                    0.08,
                    0.08,
                    0.02);
        }
        level.sendParticles(
                BendingTheme.particle(Config.AIRSTREAM_HEAD_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                this.head.x,
                this.head.y,
                this.head.z,
                Config.AIRSTREAM_HEAD_PARTICLE_COUNT.get(),
                0.25,
                0.25,
                0.25,
                0.03);
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null && player.getRandom().nextInt(10) == 0) {
            level.playSound(
                    null,
                    this.head.x,
                    this.head.y,
                    this.head.z,
                    SoundEvents.WIND_CHARGE_BURST,
                    SoundSource.PLAYERS,
                    0.25F,
                    1.3F);
        }
    }

    private void catchAndHold(ServerPlayer player, Vec3 hand, Vec3 look) {
        // Scoop along the whole stream body, not just the tip.
        double dist = hand.distanceTo(this.head);
        int steps = Math.max(1, (int) (dist / 1.5));
        for (int s = 0; s <= steps; s++) {
            Vec3 p = hand.add(this.head.subtract(hand).scale((double) s / steps));
            for (Entity e : level.getEntities(player, new AABB(p, p).inflate(CATCH_RADIUS))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (!this.held.contains(e)) {
                    e.setDeltaMovement(new Vec3(0, 0, 0));
                    e.hurtMarked = true;
                    this.held.add(e);
                }
            }
        }
        // Hold: ride the current at stream speed plus a tug back to the line.
        Vec3 seg = this.head.subtract(hand);
        double segLen2 = Math.max(1.0e-6, seg.lengthSqr());
        // The grip only breaks on death or sneak release. Nothing else lets go.
        this.held.removeIf(e -> !e.isAlive() || e.isRemoved());
        for (Entity e : new ArrayList<>(this.held)) {
            Vec3 ep = e.position().add(0, e.getBbHeight() * 0.5, 0);
            double t = Math.min(1.0, Math.max(0.0, ep.subtract(hand).dot(seg) / segLen2));
            Vec3 linePt = hand.add(seg.scale(t));
            Vec3 toLine = linePt.subtract(ep);
            double toLen = toLine.length();
            Vec3 center =
                    toLen < 1.0e-4 ? new Vec3(0, 0, 0) : toLine.normalize().scale(Math.min(1.5, toLen * 0.5));
            Vec3 v = look.scale(SPEED).add(center);
            e.setDeltaMovement(v);
            e.hurtMarked = true;
            e.resetFallDistance();
            if (e.isOnFire()) {
                e.clearFire();
            }
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AIR) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
