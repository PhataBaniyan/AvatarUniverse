package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code avatar.sphere.ESAir}: steerable gust bolt
 * fired from hotbar slot 1 while ElementSphere is active. Consumes one air
 * use. Reference values: Cooldown 1500ms (30 ticks), Range 25, Damage 3,
 * Knockback 1.5, Speed 3.
 */
public class ESAir extends SphereAttack {
    public static final String ID = "ESAir";

    /** Reference Cooldown 1500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ESAIR_COOLDOWN_MS.get());

    private static final double RANGE = Config.ESAIR_RANGE.get();
    private static final double DAMAGE = Config.ESAIR_DAMAGE.get();
    private static final double KNOCKBACK = Config.ESAIR_KNOCKBACK.get();
    private static final int SPEED = Config.ESAIR_SPEED.get();

    private Vec3 pos;
    private Vec3 dir;
    private double travelled = 0;

    public ESAir(ServerPlayer player) {
        super(player);
        if (!gate(player.getUUID())) {
            return;
        }
        ElementSphere sphere = BendingManager.find(player.getUUID(), ElementSphere.class);
        if (sphere == null) {
            return;
        }
        if (sphere.getAirUses() == 0) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || bending.isOnCooldown(ID, level.getGameTime())) {
            return;
        }
        bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        sphere.setAirUses(sphere.getAirUses() - 1);

        Vec3 eye = eyePos(player);
        this.dir = player.getLookAngle().normalize();
        this.pos = eye.add(this.dir.scale(1));
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive()) {
            return false;
        }
        if (this.travelled >= RANGE) {
            return false;
        }
        ServerPlayer sp = player;
        for (int i = 0; i < SPEED; i++) {
            this.travelled++;
            if (this.travelled >= RANGE) {
                return true;
            }
            if (sp.isAlive()) {
                this.dir = sp.getLookAngle().normalize();
            }
            this.pos = this.pos.add(this.dir);
            BlockPos bp = BlockPos.containing(this.pos);
            var state = level.getBlockState(bp);
            if (!state.isAir() || !state.getFluidState().isEmpty()) {
                this.travelled = RANGE;
                return true;
            }
            level.sendParticles(
                    BendingTheme.particle(Config.ESAIR_TRAIL_PARTICLE.get(), ParticleTypes.SMALL_GUST),
                    pos.x,
                    pos.y,
                    pos.z,
                    Config.ESAIR_TRAIL_PARTICLE_COUNT.get(),
                    0.35,
                    0.35,
                    0.35,
                    0.04);
            for (Entity entity : level.getEntities(sp, new AABB(pos, pos).inflate(2.5))) {
                if (skipTarget(sp, entity)) {
                    continue;
                }
                if (entity instanceof LivingEntity living) {
                    living.hurt(sp.damageSources().magic(), (float) DAMAGE);
                    living.setDeltaMovement(this.dir.scale(KNOCKBACK));
                    living.hurtMarked = true;
                    this.travelled = RANGE;
                    return true;
                }
            }
        }
        return true;
    }

    private static boolean skipTarget(ServerPlayer sp, Entity entity) {
        if (entity.getUUID().equals(sp.getUUID())) {
            return true;
        }
        if (!(entity instanceof LivingEntity)) {
            return true;
        }
        return entity instanceof ArmorStand;
    }
}
