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
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code Bolt} (Hyperion lineage): hold sneak to
 * charge, release to snap a sky strike onto the targeted entity else the
 * targeted block. 5-block falloff: full inside 1.5, then base - dist/2
 * (dist/3 in water), doubled when wet or metal-armored. Creepers get powered.
 * A second Bolt channeling within 4 blocks shares the charge and skips
 * damage, like upstream. Reference values: Cooldown 3500ms (70 ticks), Charge
 * 1500ms (30 ticks), Damage 5, Range 25.
 */
public class Bolt extends BendingAbility {
    public static final String ID = "Bolt";

    /** Reference Cooldown 3500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.BOLT_COOLDOWN_MS.get());
    /** Reference charge 1500ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.BOLT_CHARGE_MS.get());

    private static final double DAMAGE = Config.BOLT_DAMAGE.get();
    private static final double RANGE = Config.BOLT_RANGE.get();
    private static final int POINT_GENERATION = Config.BOLT_POINT_GENERATION.get();
    private static final double FALLOFF_RADIUS = Config.BOLT_FALLOFF_RADIUS.get();
    private static final double FULL_DAMAGE_RADIUS = Config.BOLT_FULL_DAMAGE_RADIUS.get();
    private static final double CHANNEL_SHARE_RADIUS = Config.BOLT_CHANNEL_SHARE_RADIUS.get();

    /** Live instances, for the nearby-channel share (no global query exists here). */
    private static final Set<Bolt> LIVE = ConcurrentHashMap.newKeySet();

    private final ServerLevel level;
    private boolean charged = false;

    public Bolt(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        LIVE.add(this);
    }

    @Override
    public String name() {
        return ID;
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
        if (this.charged) {
            if (player.isShiftKeyDown()) {
                Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
                level.sendParticles(
                        BendingTheme.particle(Config.BOLT_CHARGE_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                        eye.x,
                        eye.y,
                        eye.z,
                        Config.BOLT_CHARGE_PARTICLE_COUNT.get(),
                        0.15,
                        0.15,
                        0.15,
                        0.02);
            } else {
                strike(player);
                return false;
            }
        } else {
            if (!player.isShiftKeyDown()) {
                return false;
            }
            if (level.getGameTime() > this.startTime + CHARGE_TICKS) {
                this.charged = true;
            }
        }
        return true;
    }

    private void strike(ServerPlayer sp) {
        Vec3 eye = new Vec3(sp.getX(), sp.getEyeY(), sp.getZ());
        Vec3 look = sp.getLookAngle().normalize();
        LivingEntity mark = null;
        for (double d = 0; d <= RANGE && mark == null; d += 0.5) {
            Vec3 p = new Vec3(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            for (Entity e : level.getEntities(sp, new AABB(p, p).inflate(1.0))) {
                if (e instanceof LivingEntity living
                        && !e.getUUID().equals(sp.getUUID())
                        && !(e instanceof ArmorStand)) {
                    mark = living;
                    break;
                }
            }
        }
        Vec3 dest;
        if (mark != null) {
            dest = mark.position();
        } else {
            dest = null;
            for (double d = 0; d <= RANGE; d += 0.5) {
                Vec3 p = new Vec3(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
                BlockPos bp = BlockPos.containing(p);
                if (!level.isLoaded(bp)) {
                    break;
                }
                if (level.getBlockState(bp).isSolidRender(level, bp)) {
                    dest = new Vec3(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5);
                    break;
                }
                dest = p;
            }
        }
        if (dest == null) {
            return;
        }
        cool(owner, level, ID, COOLDOWN_TICKS);
        drawBolt(level, eye, dest);
        level.playSound(
                null, dest.x, dest.y, dest.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 5.0F, 1.2F);
        level.sendParticles(
                BendingTheme.particle(Config.BOLT_STRIKE_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                dest.x,
                dest.y,
                dest.z,
                Config.BOLT_STRIKE_PARTICLE_COUNT.get(),
                0.4,
                0.4,
                0.4,
                0.05);
        if (isNearbyChannel(dest, sp)) {
            return;
        }
        dealDamage(sp, level, dest);
    }

    private void dealDamage(ServerPlayer sp, ServerLevel level, Vec3 at) {
        BlockPos bp = BlockPos.containing(at);
        boolean enhanced =
                level.isLoaded(bp) && !level.getBlockState(bp).getFluidState().isEmpty();
        for (Entity e : level.getEntities(sp, new AABB(at, at).inflate(FALLOFF_RADIUS))) {
            if (e instanceof Creeper creeper) {
                var bolt = new net.minecraft.world.entity.LightningBolt(
                        net.minecraft.world.entity.EntityType.LIGHTNING_BOLT, level);
                bolt.moveTo(e.getX(), e.getY(), e.getZ());
                creeper.thunderHit(level, bolt);
            }
            if (!(e instanceof LivingEntity living)
                    || e instanceof ArmorStand
                    || e.getUUID().equals(sp.getUUID())) {
                continue;
            }
            double dist = e.position().distanceTo(at);
            if (dist > FALLOFF_RADIUS) {
                continue;
            }
            boolean vulnerable = enhanced || hasMetalArmor(living);
            double base = vulnerable ? DAMAGE * 2 : DAMAGE;
            double modifier = enhanced ? dist / 3.0 : dist / 2.0;
            double dmg = dist < FULL_DAMAGE_RADIUS ? base : Math.max(0, base - modifier);
            living.hurt(sp.damageSources().lightningBolt(), (float) dmg);
        }
    }

    private static boolean hasMetalArmor(LivingEntity living) {
        for (var stack : living.getArmorSlots()) {
            String id = stack.getItem()
                    .builtInRegistryHolder()
                    .key()
                    .location()
                    .getPath()
                    .toUpperCase();
            if (id.contains("IRON")
                    || id.contains("GOLD")
                    || id.contains("COPPER")
                    || id.contains("CHAIN")
                    || id.contains("NETHERITE")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNearbyChannel(Vec3 at, ServerPlayer source) {
        for (Bolt other : LIVE) {
            if (other.owner.equals(source.getUUID())) {
                continue;
            }
            ServerPlayer op = other.level.getServer().getPlayerList().getPlayer(other.owner);
            if (op == null || !op.level().equals(source.level())) {
                continue;
            }
            if (op.position().distanceToSqr(at) < CHANNEL_SHARE_RADIUS * CHANNEL_SHARE_RADIUS) {
                other.charged = true;
                return true;
            }
        }
        return false;
    }

    /** Jagged midpoint-displacement bolt, same angles and flow as upstream. */
    private static List<Vec3> drawBolt(ServerLevel level, Vec3 from, Vec3 to) {
        List<Vec3> points = new ArrayList<>();
        points.add(from);
        points.add(to);
        for (int gen = 0; gen < POINT_GENERATION; gen++) {
            for (int i = 0; i < points.size() - 1; i += 2) {
                Vec3 a = points.get(i);
                Vec3 b = points.get(i + 1);
                Vec3 mid = a.add(b).scale(0.5);
                double jitter = a.distanceTo(b) * 0.22;
                mid = new Vec3(
                        mid.x + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * jitter,
                        mid.y + (ThreadLocalRandom.current().nextDouble() - 0.5) * jitter,
                        mid.z + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * jitter);
                points.add(i + 1, mid);
            }
        }
        for (Vec3 p : points) {
            level.sendParticles(
                    BendingTheme.particle(Config.BOLT_BEAM_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    p.x,
                    p.y,
                    p.z,
                    Config.BOLT_BEAM_PARTICLE_COUNT.get(),
                    0.03,
                    0.03,
                    0.03,
                    0.005);
        }
        return points;
    }

    @Override
    public void onRemove() {
        LIVE.remove(this);
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
