package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
 * Port of ProjectAvatar {@code ChargeBolt} (ProjectAddons lineage): hold
 * sneak to charge the stock; click to throw bolts one at a time down the
 * gaze, or release to discharge the rest in a gaze-centered cone (yaw +-30,
 * pitch +-23). Each bolt jitters +-8 deg per step, 0.62 hit radius,
 * no-damage-ticks reset. Reference values: Cooldown 4000ms (80 ticks), Charge
 * 1500ms (30 ticks), Damage 5, BoltRange 20, BlastRadius 3, Speed 2, Stock 5.
 */
public class ChargeBolt extends BendingAbility {
    public static final String ID = "ChargeBolt";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.CHARGEBOLT_COOLDOWN_TICKS.get();
    /** Reference charge 1500ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.CHARGEBOLT_CHARGE_TICKS.get();

    private static final double DAMAGE = Config.CHARGEBOLT_DAMAGE.get();
    private static final double BOLT_RANGE = Config.CHARGEBOLT_BOLT_RANGE.get();
    private static final double BLAST_RADIUS = Config.CHARGEBOLT_BLAST_RADIUS.get();
    private static final int SPEED = Config.CHARGEBOLT_SPEED.get();
    private static final int STOCK = Config.CHARGEBOLT_STOCK.get();
    private static final double HIT_RADIUS = Config.CHARGEBOLT_HIT_RADIUS.get();
    private static final int JITTER_DEGREES = Config.CHARGEBOLT_JITTER_DEGREES.get();
    private static final int SPREAD_YAW = Config.CHARGEBOLT_SPREAD_YAW.get();
    private static final int SPREAD_PITCH = Config.CHARGEBOLT_SPREAD_PITCH.get();

    private final ServerLevel level;
    private boolean released = false;
    private int stock = STOCK;
    private final Set<Bolt> bolts = new HashSet<>();

    public ChargeBolt(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
    }

    private boolean charged() {
        return level.getGameTime() - this.startTime >= CHARGE_TICKS;
    }

    /** Left-click while charged: throw a single bolt. */
    public static void fireOne(ServerPlayer player) {
        ChargeBolt inst = BendingManager.find(player.getUUID(), ChargeBolt.class);
        if (inst == null || !inst.charged() || inst.stock < 1 || !player.isShiftKeyDown()) {
            return;
        }
        inst.stock--;
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 dir = player.getLookAngle().normalize();
        inst.bolts.add(inst.new Bolt(eye, dir, BOLT_RANGE));
        if (inst.stock <= 0) {
            cool(player.getUUID(), player.serverLevel(), ID, COOLDOWN_TICKS);
        }
        player.serverLevel()
                .playSound(null, eye.x, eye.y, eye.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.5F, 1.4F);
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
        if (player.isShiftKeyDown() && charged()) {
            Vec3 hand = new Vec3(player.getX(), player.getEyeY(), player.getZ());
            level.sendParticles(
                    BendingTheme.particle(Config.CHARGEBOLT_CHARGE_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    hand.x,
                    hand.y,
                    hand.z,
                    Config.CHARGEBOLT_CHARGE_PARTICLE_COUNT.get(),
                    0.1,
                    0.1,
                    0.1,
                    0.02);
            if (Math.random() < 0.3) {
                level.playSound(
                        null, hand.x, hand.y, hand.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.4F, 1.5F);
            }
        } else if (!player.isShiftKeyDown() && charged() && !this.released) {
            this.released = true;
            discharge(player);
        } else if (!player.isShiftKeyDown() && !charged()) {
            return false;
        } else if (!charged()) {
            return true;
        }
        Set<Bolt> dead = new HashSet<>();
        for (Bolt bolt : this.bolts) {
            boolean alive = true;
            for (int i = 0; i < SPEED; i++) {
                if (!bolt.advance(player, level)) {
                    alive = false;
                    break;
                }
            }
            if (!alive) {
                dead.add(bolt);
            }
        }
        this.bolts.removeAll(dead);
        if (this.bolts.isEmpty() && this.stock < 1) {
            return false;
        }
        return true;
    }

    private void discharge(ServerPlayer sp) {
        cool(owner, level, ID, COOLDOWN_TICKS);
        Vec3 center = sp.position().add(0, 1, 0);
        Random rand = new Random();
        float baseYaw = sp.getYRot();
        float basePitch = sp.getXRot();
        for (int i = 0; i < this.stock; i++) {
            float yaw = baseYaw + rand.nextInt(SPREAD_YAW * 2) - SPREAD_YAW;
            float pitch = basePitch + rand.nextInt(SPREAD_PITCH * 2) - SPREAD_PITCH;
            double yr = Math.toRadians(yaw);
            double pr = Math.toRadians(pitch);
            Vec3 dir = new Vec3(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr)).normalize();
            this.bolts.add(new Bolt(center, dir, BLAST_RADIUS));
        }
        this.stock = 0;
        level.playSound(
                null, center.x, center.y, center.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    private class Bolt {
        private Vec3 loc;
        private final Vec3 start;
        private final double range;
        private final Random rand = new Random();
        private Vec3 heading;

        private Bolt(Vec3 start, Vec3 dir, double range) {
            this.start = start;
            this.loc = start;
            this.heading = dir.normalize();
            this.range = range * range;
        }

        private boolean advance(ServerPlayer sp, ServerLevel level) {
            double yaw = Math.toDegrees(Math.atan2(-heading.x, heading.z));
            double pitch = Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, heading.y))));
            yaw += this.rand.nextInt(JITTER_DEGREES * 2) - JITTER_DEGREES;
            pitch += this.rand.nextInt(JITTER_DEGREES * 2) - JITTER_DEGREES;
            double yr = Math.toRadians(yaw);
            double pr = Math.toRadians(pitch);
            this.heading =
                    new Vec3(-Math.sin(yr) * Math.cos(pr), Math.sin(pr), Math.cos(yr) * Math.cos(pr)).normalize();
            this.loc = this.loc.add(this.heading);
            if (this.loc.distanceToSqr(this.start) >= this.range) {
                return false;
            }
            BlockPos bp = BlockPos.containing(this.loc);
            if (!level.isLoaded(bp) || level.getBlockState(bp).isSolidRender(level, bp)) {
                return false;
            }
            level.sendParticles(
                    BendingTheme.particle(Config.CHARGEBOLT_TRAIL_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    this.loc.x,
                    this.loc.y,
                    this.loc.z,
                    Config.CHARGEBOLT_TRAIL_PARTICLE_COUNT.get(),
                    0.1,
                    0.1,
                    0.1,
                    0.02);
            if (Math.random() < 0.3) {
                level.playSound(
                        null,
                        this.loc.x,
                        this.loc.y,
                        this.loc.z,
                        SoundEvents.TRIDENT_THUNDER,
                        SoundSource.PLAYERS,
                        0.25F,
                        1.6F);
            }
            for (Entity e : level.getEntities(sp, new AABB(this.loc, this.loc).inflate(HIT_RADIUS))) {
                if (e.getUUID().equals(sp.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.hurt(sp.damageSources().lightningBolt(), (float) DAMAGE);
                    living.invulnerableTime = 0;
                    return false;
                }
            }
            return true;
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
