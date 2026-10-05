package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
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
 * Port of ProjectAvatar {@code ArcSpark} (ProjectAddons lineage): hold sneak
 * to charge, click while charged to loose a seeking arc from alternating
 * hands. The arc advances speed*length steps of 0.3 per tick, bends toward
 * entities within 3 and metallic blocks within 2 inside a 60-degree cone, and
 * grounds out on water or metal. Reference values: Cooldown 4000ms
 * (80 ticks), Charge 1500ms (30 ticks), Damage 4, Range 16, Duration 1500ms
 * (30 ticks).
 */
public class ArcSpark extends BendingAbility {
    public static final String ID = "ArcSpark";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ARCSPARK_COOLDOWN_MS.get());
    /** Reference charge 1500ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.msToTicks(Config.ARCSPARK_CHARGE_MS.get());
    /** Reference active window after the shot, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.ARCSPARK_DURATION_MS.get());

    private static final int SPEED = Config.ARCSPARK_SPEED.get();
    private static final int LENGTH = Config.ARCSPARK_LENGTH.get();
    private static final double DAMAGE = Config.ARCSPARK_DAMAGE.get();
    private static final double RANGE = Config.ARCSPARK_RANGE.get();
    private static final double SEEK_RADIUS = Config.ARCSPARK_SEEK_RADIUS.get();
    private static final int METAL_SCAN_RADIUS = Config.ARCSPARK_METAL_SCAN_RADIUS.get();
    private static final int CONE_ANGLE_DEG = Config.ARCSPARK_CONE_ANGLE_DEG.get();
    private static final double HIT_DISTANCE = Config.ARCSPARK_HIT_DISTANCE.get();
    private static final double STEP_LENGTH = Config.ARCSPARK_STEP_LENGTH.get();

    private final ServerLevel level;
    private boolean charged = false;
    private boolean shooting = false;
    private boolean left = false;
    private long chargedTill;
    private Vec3 head;
    private Vec3 lastDir = null;
    private Vec3 lastPos = null;
    private double travelled = 0;
    private int ticks = 0;

    public ArcSpark(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.chargedTill = player.level().getGameTime();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Click while charged (still sneaking): loose the arc. */
    public static void shoot(ServerPlayer player) {
        ArcSpark inst = BendingManager.find(player.getUUID(), ArcSpark.class);
        if (inst != null) {
            inst.beginShot(player);
        }
    }

    private void beginShot(ServerPlayer player) {
        if (this.shooting || !this.charged) {
            return;
        }
        this.shooting = true;
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize().scale(this.left ? -0.55 : 0.55);
        this.left = !this.left;
        this.head = new Vec3(player.getX() + side.x, player.getY() + 1.2, player.getZ() + side.z);
        this.travelled = 0;
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
        // Upstream dies the moment sneak drops, fired or not.
        if (!player.isShiftKeyDown()) {
            return false;
        }
        this.ticks++;
        if (!this.charged) {
            if (level.getGameTime() - this.startTime >= CHARGE_TICKS) {
                this.charged = true;
            } else {
                Vec3 at = new Vec3(player.getX(), player.getY() + 1.0, player.getZ());
                level.sendParticles(
                        BendingTheme.particle(Config.ARCSPARK_CHARGE_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                        at.x,
                        at.y,
                        at.z,
                        Config.ARCSPARK_CHARGE_PARTICLE_COUNT.get(),
                        0.36,
                        0.36,
                        0.36,
                        0.03);
            }
            this.chargedTill = level.getGameTime();
            if (this.ticks % 30 == 0) {
                level.playSound(
                        null,
                        player.getX(),
                        player.getEyeY(),
                        player.getZ(),
                        SoundEvents.CREEPER_PRIMED,
                        SoundSource.PLAYERS,
                        0.3F,
                        0.6F);
            }
            return true;
        }
        if (!this.shooting) {
            Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
            Vec3 out = eye.add(player.getLookAngle().normalize().scale(1.3));
            level.sendParticles(
                    BendingTheme.particle(Config.ARCSPARK_READY_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    out.x,
                    out.y,
                    out.z,
                    Config.ARCSPARK_READY_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.0);
            this.chargedTill = level.getGameTime();
            if (this.ticks % 30 == 0) {
                level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 0.3F, 0.6F);
            }
            return true;
        }
        if (level.getGameTime() - this.chargedTill > DURATION_TICKS) {
            return false;
        }
        // Alternate hands every volley step like upstream's left/right flip.
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0, look.x);
        if (side.lengthSqr() < 1.0e-4) {
            side = new Vec3(1, 0, 0);
        }
        side = side.normalize().scale(this.left ? -0.55 : 0.55);
        this.left = !this.left;
        Vec3 hand = new Vec3(player.getX() + side.x, player.getY() + 1.2, player.getZ() + side.z);
        Vec3 handDir = look;
        boolean persist = true;
        for (int i = 0; i < SPEED * LENGTH && persist; i++) {
            persist = arc(player, level, hand, handDir);
            handDir = this.lastDir == null ? handDir : this.lastDir;
            hand = this.lastPos == null ? hand : this.lastPos;
            this.travelled += STEP_LENGTH;
            if (this.travelled >= RANGE) {
                persist = false;
            }
        }
        return persist;
    }

    private boolean arc(ServerPlayer sp, ServerLevel level, Vec3 loc, Vec3 dir) {
        Vec3 cur = new Vec3(loc.x, loc.y, loc.z);
        Vec3 curDir = dir;
        double shortest = Double.MAX_VALUE;
        LivingEntity prey = null;
        Vec3 to = null;
        for (Entity e : level.getEntities(sp, new AABB(cur, cur).inflate(SEEK_RADIUS))) {
            if (!(e instanceof LivingEntity living) || e.getUUID().equals(sp.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            Vec3 chest = e.position().add(0, 1, 0);
            double dist = cur.distanceTo(chest);
            if (dist <= HIT_DISTANCE) {
                living.hurt(sp.damageSources().lightningBolt(), (float) DAMAGE);
                return false;
            }
            if (dist < shortest) {
                shortest = dist;
                prey = living;
                to = chest;
            }
        }
        if (prey == null) {
            BlockPos base = BlockPos.containing(cur);
            for (int dx = -METAL_SCAN_RADIUS; dx <= METAL_SCAN_RADIUS; dx++) {
                for (int dy = -METAL_SCAN_RADIUS; dy <= METAL_SCAN_RADIUS; dy++) {
                    for (int dz = -METAL_SCAN_RADIUS; dz <= METAL_SCAN_RADIUS; dz++) {
                        BlockPos bp = base.offset(dx, dy, dz);
                        if (!level.isLoaded(bp)) {
                            continue;
                        }
                        var st = level.getBlockState(bp);
                        if (st.isAir() || (!isMetal(st) && st.getFluidState().isEmpty())) {
                            continue;
                        }
                        Vec3 c = new Vec3(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5);
                        double d = cur.distanceTo(c);
                        if (d < shortest) {
                            shortest = d;
                            to = c;
                        }
                    }
                }
            }
        }
        Vec3 movement;
        if (to != null) {
            movement = to.subtract(cur).normalize();
        } else {
            movement = new Vec3(Math.random() / 5 - 0.1, Math.random() / 5 - 0.1, Math.random() / 5 - 0.1);
        }
        double angle = Math.toDegrees(angleBetween(curDir, curDir.add(movement)));
        if (angle < CONE_ANGLE_DEG) {
            curDir = curDir.add(movement).normalize();
        }
        curDir = curDir.normalize();
        cur = cur.add(curDir.scale(STEP_LENGTH));
        BlockPos bp = BlockPos.containing(cur);
        if (!level.isLoaded(bp)) {
            return false;
        }
        var state = level.getBlockState(bp);
        if (!state.getFluidState().isEmpty() || isMetal(state)) {
            // No Electrify class in this mod: ground out with the same spark
            // burst instead of a lingering electrified field.
            level.sendParticles(
                    BendingTheme.particle(Config.ARCSPARK_GROUND_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                    cur.x,
                    cur.y,
                    cur.z,
                    Config.ARCSPARK_GROUND_PARTICLE_COUNT.get(),
                    0.05,
                    0.05,
                    0.05,
                    0.01);
            return false;
        }
        if (state.isSolidRender(level, bp)) {
            return false;
        }
        level.sendParticles(
                BendingTheme.particle(Config.ARCSPARK_TRAIL_PARTICLE.get(), ParticleTypes.ELECTRIC_SPARK),
                cur.x,
                cur.y,
                cur.z,
                Config.ARCSPARK_TRAIL_PARTICLE_COUNT.get(),
                0.0,
                0.0,
                0.0,
                0.0);
        if (Math.random() < 0.01) {
            level.playSound(null, cur.x, cur.y, cur.z, SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.3F, 1.6F);
        }
        this.head = cur;
        this.lastDir = curDir;
        this.lastPos = cur;
        return true;
    }

    private static double angleBetween(Vec3 a, Vec3 b) {
        double dot = a.normalize().dot(b.normalize());
        dot = Math.max(-1, Math.min(1, dot));
        return Math.acos(dot);
    }

    private static boolean isMetal(net.minecraft.world.level.block.state.BlockState state) {
        String id = state.getBlock()
                .builtInRegistryHolder()
                .key()
                .location()
                .getPath()
                .toUpperCase();
        return id.contains("IRON")
                || id.contains("GOLD")
                || id.contains("COPPER")
                || id.contains("LIGHTNING_ROD")
                || id.contains("CAULDRON")
                || id.contains("CHAIN");
    }

    @Override
    public void onRemove() {
        // Cooldown only when a shot actually left the hands, like upstream.
        if (this.charged && this.shooting) {
            cool(owner, level, ID, COOLDOWN_TICKS);
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
