package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code Lightning} (ProjectKorra master port).
 * Hold sneak to charge, release to throw a jagged main bolt with recursive
 * sub-arcs. Chains between victims, stuns, powers creepers, fans out in
 * water and walks copper / lightning rods. Reference values: Cooldown 4000ms
 * (80 ticks), Charge 2000ms (40 ticks), Damage 6, Range 30, ChainRange 12,
 * MaxChains 3, ChainChance 0.5, StunChance 0.4, StunTicks 40.
 */
public class Lightning extends BendingAbility {
    public static final String ID = "Lightning";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.LIGHTNING_COOLDOWN_MS.get());
    /** Reference charge 2000ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.LIGHTNING_CHARGE_MS.get());
    /** Bolt linger after the strike (~400ms), in server ticks. */
    private static final long LINGER_TICKS = Config.msToTicks(Config.LIGHTNING_LINGER_MS.get());

    private static final double DAMAGE = Config.LIGHTNING_DAMAGE.get();
    private static final double RANGE = Config.LIGHTNING_RANGE.get();
    private static final double CHAIN_RANGE = Config.LIGHTNING_CHAIN_RANGE.get();
    private static final int MAX_CHAINS = Config.LIGHTNING_MAX_CHAINS.get();
    private static final double CHAIN_CHANCE = Config.LIGHTNING_CHAIN_CHANCE.get();
    private static final double STUN_CHANCE = Config.LIGHTNING_STUN_CHANCE.get();
    private static final long STUN_TICKS = Config.msToTicks(Config.LIGHTNING_STUN_MS.get());

    private static final int POINT_GENERATION = Config.LIGHTNING_POINT_GENERATION.get();
    private static final double SUB_ARC_CHANCE = Config.LIGHTNING_SUB_ARC_CHANCE.get();
    private static final double MAX_ARC_ANGLE_DEG = Config.LIGHTNING_MAX_ARC_ANGLE_DEG.get();
    private static final int WATER_ARCS = Config.LIGHTNING_WATER_ARCS.get();
    private static final double WATER_ARC_RANGE = Config.LIGHTNING_WATER_ARC_RANGE.get();
    private static final int MAX_COPPER_ARCS = Config.LIGHTNING_MAX_COPPER_ARCS.get();
    private static final double CONDUCTIVITY_RANGE = Config.LIGHTNING_CONDUCTIVITY_RANGE.get();
    private static final double WET_HIT_RADIUS = Config.LIGHTNING_WET_HIT_RADIUS.get();
    private static final double DRY_HIT_RADIUS = Config.LIGHTNING_DRY_HIT_RADIUS.get();
    private static final int IGNITE_SECONDS = Config.LIGHTNING_IGNITE_MS.get() / 1000;

    private final ServerPlayer player;
    private final ServerLevel level;
    private boolean charged = false;
    private double ringAngle = 0;
    private List<Vec3> boltPoints = null;
    private long struckAt = 0;
    private Vec3 strikeDest = null;

    public Lightning(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
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
        // Block while riding a FireJet, like AllowOnFireJet=false upstream.
        if (BendingManager.find(owner, FireJet.class) != null) {
            return false;
        }
        if (this.struckAt != 0) {
            return linger();
        }
        if (player.isShiftKeyDown()) {
            if (!this.charged && level.getGameTime() - this.startTime >= CHARGE_TICKS) {
                this.charged = true;
            }
            if (this.charged) {
                chargeParticles();
            } else {
                gatherParticles();
            }
            return true;
        }
        if (!this.charged) {
            return false;
        }
        strike();
        cool(owner, level, ID, COOLDOWN_TICKS);
        this.struckAt = level.getGameTime();
        return true;
    }

    private boolean linger() {
        if (this.boltPoints == null) {
            return false;
        }
        long age = level.getGameTime() - this.struckAt;
        if (age > LINGER_TICKS) {
            return false;
        }
        int count =
                Math.max(1, (int) (Config.LIGHTNING_LINGER_PARTICLE_COUNT.get() * (LINGER_TICKS - age) / LINGER_TICKS));
        for (Vec3 p : this.boltPoints) {
            level.sendParticles(
                    BendingTheme.particle(Config.LIGHTNING_LINGER_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    p.x,
                    p.y,
                    p.z,
                    count,
                    0.03,
                    0.03,
                    0.03,
                    0.005);
        }
        return true;
    }

    /** Uncharged spiral at the feet, like the reference rotating gather point. */
    private void gatherParticles() {
        Vec3 base = player.position().add(0, 1, 0);
        this.ringAngle += 0.5;
        double x = base.x + Math.cos(this.ringAngle) * 1.0;
        double z = base.z + Math.sin(this.ringAngle) * 1.0;
        double y = base.y + 1.0 + Math.cos(this.ringAngle * 0.4) * 1.0;
        level.sendParticles(
                BendingTheme.particle(Config.LIGHTNING_GATHER_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                x,
                y,
                z,
                Config.LIGHTNING_GATHER_PARTICLE_COUNT.get(),
                0.05,
                0.05,
                0.05,
                0.01);
    }

    private void chargeParticles() {
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        level.sendParticles(
                BendingTheme.particle(Config.LIGHTNING_CHARGE_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                eye.x,
                eye.y,
                eye.z,
                Config.LIGHTNING_CHARGE_PARTICLE_COUNT.get(),
                0.3,
                0.3,
                0.3,
                0.03);
        if (ThreadLocalRandom.current().nextDouble() < 0.2) {
            level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.3F, 1.6F);
        }
    }

    private void strike() {
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        Vec3 dest = null;
        LivingEntity target = null;
        for (double d = 0; d <= RANGE && target == null; d += 0.5) {
            Vec3 p = new Vec3(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
            for (Entity e : level.getEntities(player, new AABB(p, p).inflate(1.0))) {
                if (e instanceof LivingEntity living
                        && !e.getUUID().equals(player.getUUID())
                        && !(e instanceof ArmorStand)) {
                    target = living;
                    break;
                }
            }
        }
        if (target != null) {
            dest = target.position().add(0, 1, 0);
        } else {
            dest = null;
            BlockPos rodFound = null;
            for (double d = 0; d <= RANGE; d += 0.5) {
                Vec3 p = new Vec3(eye.x + look.x * d, eye.y + look.y * d, eye.z + look.z * d);
                BlockPos bp = BlockPos.containing(p);
                if (!level.isLoaded(bp)) {
                    break;
                }
                if (isRod(level, bp) || isCopper(level, bp)) {
                    rodFound = bp.immutable();
                    dest = new Vec3(bp.getX() + 0.5, bp.getY() + 0.6, bp.getZ() + 0.5);
                    break;
                }
                if (level.getBlockState(bp).isSolidRender(level, bp)) {
                    dest = new Vec3(bp.getX() + 0.5, bp.getY() + 1.0, bp.getZ() + 0.5);
                    break;
                }
                dest = p;
            }
            // Magnet to a nearby rod/copper like the reference 1.25 scan.
            if (rodFound == null && dest != null) {
                BlockPos around = BlockPos.containing(dest);
                outer:
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos bp = around.offset(dx, dy, dz);
                            if (!level.isLoaded(bp)) {
                                continue;
                            }
                            if (isRod(level, bp) || isCopper(level, bp)) {
                                dest = new Vec3(bp.getX() + 0.5, bp.getY() + 0.6, bp.getZ() + 0.5);
                                break outer;
                            }
                        }
                    }
                }
            }
        }
        if (dest == null) {
            return;
        }
        this.strikeDest = dest;
        List<Vec3> main = drawBolt(level, eye, dest);
        this.boltPoints = new ArrayList<>(main);
        // Recursive sub-arcs off the main bolt.
        for (Vec3 p : new ArrayList<>(main)) {
            if (ThreadLocalRandom.current().nextDouble() < SUB_ARC_CHANCE) {
                Vec3 branch = randomBranch(p, look, RANGE / 2.0);
                this.boltPoints.addAll(drawBolt(level, p, branch));
            }
        }
        // Water fan-out.
        BlockPos dbp = BlockPos.containing(dest);
        boolean wet =
                level.isLoaded(dbp) && !level.getBlockState(dbp).getFluidState().isEmpty();
        if (wet) {
            for (int i = 0; i < WATER_ARCS; i++) {
                Vec3 origin = new Vec3(
                        dest.x + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2,
                        dest.y,
                        dest.z + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2);
                Vec3 end = origin.add(new Vec3(
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * WATER_ARC_RANGE,
                        ThreadLocalRandom.current().nextDouble() - 0.7,
                        (ThreadLocalRandom.current().nextDouble() - 0.5) * WATER_ARC_RANGE));
                this.boltPoints.addAll(drawBolt(level, origin, end));
            }
        }
        // Copper / rod walk.
        chainCopper(eye, dest);
        skyStrike(level, dest);
        level.playSound(
                null, dest.x, dest.y, dest.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.0F, 1.0F);

        double hitRadius = wet ? WET_HIT_RADIUS : DRY_HIT_RADIUS;
        Set<UUID> hit = new HashSet<>();
        List<LivingEntity> victims = new ArrayList<>();
        for (Entity e : level.getEntities(player, new AABB(dest, dest).inflate(hitRadius))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living) {
                victims.add(living);
                hit.add(e.getUUID());
            }
        }
        for (LivingEntity living : victims) {
            transformMob(living);
            electrocute(living);
        }
        int chains = MAX_CHAINS;
        Vec3 from = dest;
        while (chains > 0 && ThreadLocalRandom.current().nextDouble() <= CHAIN_CHANCE) {
            LivingEntity next = null;
            double best = CHAIN_RANGE * CHAIN_RANGE;
            for (Entity e : level.getEntities(player, new AABB(from, from).inflate(CHAIN_RANGE))) {
                if (!(e instanceof LivingEntity living)
                        || e.getUUID().equals(player.getUUID())
                        || e instanceof ArmorStand
                        || hit.contains(e.getUUID())) {
                    continue;
                }
                double d2 = e.position().distanceToSqr(from);
                if (d2 < best) {
                    best = d2;
                    next = living;
                }
            }
            if (next == null) {
                break;
            }
            Vec3 np = next.position().add(0, 1, 0);
            this.boltPoints.addAll(drawBolt(level, from, np));
            transformMob(next);
            electrocute(next);
            hit.add(next.getUUID());
            from = np;
            chains--;
        }
    }

    private static Vec3 randomBranch(Vec3 from, Vec3 dir, double length) {
        double angle = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * MAX_ARC_ANGLE_DEG;
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        Vec3 rotated = new Vec3(dir.x * cos - dir.z * sin, dir.y, dir.x * sin + dir.z * cos);
        double len = length * (0.33 + ThreadLocalRandom.current().nextDouble() * 0.67);
        return from.add(rotated.normalize().scale(len));
    }

    private void chainCopper(Vec3 eye, Vec3 dest) {
        BlockPos start = BlockPos.containing(dest);
        List<BlockPos> chain = new ArrayList<>();
        chain.add(start);
        Set<BlockPos> seen = new HashSet<>();
        seen.add(start);
        for (int i = 0; i < MAX_COPPER_ARCS; i++) {
            BlockPos last = chain.get(chain.size() - 1);
            Vec3 lc = new Vec3(last.getX() + 0.5, last.getY() + 0.6, last.getZ() + 0.5);
            BlockPos best = null;
            double bestD2 = CONDUCTIVITY_RANGE * CONDUCTIVITY_RANGE;
            int r = (int) Math.ceil(CONDUCTIVITY_RANGE);
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        BlockPos bp = last.offset(dx, dy, dz);
                        if (seen.contains(bp) || !level.isLoaded(bp)) {
                            continue;
                        }
                        if (!isRod(level, bp) && !isCopper(level, bp)) {
                            continue;
                        }
                        double d2 = new Vec3(bp.getX() + 0.5, bp.getY() + 0.6, bp.getZ() + 0.5).distanceToSqr(lc);
                        if (d2 < bestD2) {
                            bestD2 = d2;
                            best = bp.immutable();
                        }
                    }
                }
            }
            if (best == null) {
                break;
            }
            Vec3 nc = new Vec3(best.getX() + 0.5, best.getY() + 0.6, best.getZ() + 0.5);
            this.boltPoints.addAll(drawBolt(level, lc, nc));
            powerRod(level, best);
            chain.add(best);
            seen.add(best);
        }
    }

    private static boolean isCopper(ServerLevel level, BlockPos pos) {
        String name = level.getBlockState(pos).getBlock().toString().toUpperCase();
        // Block.toString is noisy; check the registry path via block state name instead.
        String id = level.getBlockState(pos)
                .getBlock()
                .builtInRegistryHolder()
                .key()
                .location()
                .getPath()
                .toUpperCase();
        if (id.contains("COPPER") && !id.contains("LIGHTNING_ROD")) {
            return true;
        }
        return name.contains("COPPER") && !name.contains("LIGHTNING_ROD");
    }

    private static boolean isRod(ServerLevel level, BlockPos pos) {
        String id = level.getBlockState(pos)
                .getBlock()
                .builtInRegistryHolder()
                .key()
                .location()
                .getPath();
        return id.equalsIgnoreCase("lightning_rod");
    }

    private static void powerRod(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getBlock().builtInRegistryHolder().key().location().getPath().equalsIgnoreCase("lightning_rod")) {
            level.sendParticles(
                    BendingTheme.particle(Config.LIGHTNING_ROD_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    pos.getX() + 0.5,
                    pos.getY() + 0.6,
                    pos.getZ() + 0.5,
                    Config.LIGHTNING_ROD_PARTICLE_COUNT.get(),
                    0.12,
                    0.12,
                    0.12,
                    0.05);
        }
    }

    private void transformMob(LivingEntity living) {
        if (living instanceof Creeper creeper) {
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
            bolt.moveTo(living.getX(), living.getY(), living.getZ());
            creeper.thunderHit(level, bolt);
            return;
        }
        if (living.getType() == EntityType.VILLAGER) {
            Mob witch = (Mob) EntityType.WITCH.create(level);
            if (witch != null) {
                witch.moveTo(living.getX(), living.getY(), living.getZ());
                level.addFreshEntity(witch);
                living.discard();
            }
        } else if (living.getType() == EntityType.PIG) {
            Mob piglin = (Mob) EntityType.ZOMBIFIED_PIGLIN.create(level);
            if (piglin != null) {
                piglin.moveTo(living.getX(), living.getY(), living.getZ());
                level.addFreshEntity(piglin);
                living.discard();
            }
        }
    }

    private void electrocute(LivingEntity living) {
        living.hurt(player.damageSources().lightningBolt(), (float) DAMAGE);
        living.igniteForSeconds(IGNITE_SECONDS);
        if (ThreadLocalRandom.current().nextDouble() <= STUN_CHANCE) {
            living.addEffect(
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) STUN_TICKS, 5, false, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int) STUN_TICKS, 1, false, false, false));
        }
        level.playSound(
                null,
                living.getX(),
                living.getY(),
                living.getZ(),
                SoundEvents.TRIDENT_THUNDER,
                SoundSource.PLAYERS,
                0.7F,
                1.0F);
    }

    public static List<Vec3> drawBolt(ServerLevel level, Vec3 from, Vec3 to) {
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
                    BendingTheme.particle(Config.LIGHTNING_BEAM_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    p.x,
                    p.y,
                    p.z,
                    Config.LIGHTNING_BEAM_PARTICLE_COUNT.get(),
                    0.03,
                    0.03,
                    0.03,
                    0.005);
        }
        return points;
    }

    /** The real thing: a vanilla lightning bolt where the strike lands. */
    private static void skyStrike(ServerLevel level, Vec3 at) {
        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        bolt.moveTo(at.x, at.y, at.z);
        level.addFreshEntity(bolt);
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
