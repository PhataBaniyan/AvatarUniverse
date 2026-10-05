package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code avatar.sphere.ESWater}: steerable water bolt
 * fired from hotbar slot 4 while ElementSphere is active. Requires water or
 * ice in sight (IceBlast raycast), consumes one water use, and leaves a
 * short-lived water trail that reverts to air. Reference values: Cooldown
 * 1500ms (30 ticks), Range 25, Damage 3, Speed 3, TrailRevert 150ms
 * (3 ticks).
 */
public class ESWater extends SphereAttack {
    public static final String ID = "ESWater";

    /** Reference Cooldown 1500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ESWATER_COOLDOWN_MS.get());

    private static final double RANGE = Config.ESWATER_RANGE.get();
    private static final double DAMAGE = Config.ESWATER_DAMAGE.get();
    private static final int SPEED = Config.ESWATER_SPEED.get();
    /** Reference trail revert 150ms, in server ticks. */
    private static final long TRAIL_REVERT_TICKS = Config.msToTicks(Config.ESWATER_TRAIL_REVERT_MS.get());

    private Vec3 pos;
    private Vec3 dir;
    private double travelled = 0;

    public ESWater(ServerPlayer player) {
        super(player);
        if (!gate(player.getUUID())) {
            return;
        }
        ElementSphere sphere = BendingManager.find(player.getUUID(), ElementSphere.class);
        if (sphere == null) {
            return;
        }
        if (sphere.getWaterUses() == 0) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || bending.isOnCooldown(ID, level.getGameTime())) {
            return;
        }
        bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        sphere.setWaterUses(sphere.getWaterUses() - 1);

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
            if (!level.getBlockState(bp).isAir()) {
                this.travelled = RANGE;
                return true;
            }
            level.sendParticles(
                    BendingTheme.particle(Config.ESWATER_TRAIL_PARTICLE.get(), ParticleTypes.BUBBLE),
                    pos.x,
                    pos.y,
                    pos.z,
                    Config.ESWATER_TRAIL_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.03);
            if (level.getBlockState(bp).isAir()) {
                BlockPos immutable = bp.immutable();
                TempBlock trail = TempBlock.cube(level, immutable, 0.25F);
                BendingManager.scheduleRevert(trail, level.getGameTime() + TRAIL_REVERT_TICKS);
            }
            // Explicit head cube so the bolt reads as more than spray.
            if (level.getBlockState(bp).isAir()) {
                BendingManager.scheduleRevert(TempBlock.cube(level, bp.immutable(), 0.3F), level.getGameTime() + 2L);
            }
            for (Entity entity : level.getEntities(sp, new AABB(pos, pos).inflate(2.5))) {
                if (entity.getUUID().equals(sp.getUUID())
                        || !(entity instanceof LivingEntity living)
                        || entity instanceof ArmorStand) {
                    continue;
                }
                living.hurt(sp.damageSources().magic(), (float) DAMAGE);
                this.travelled = RANGE;
                return true;
            }
        }
        return true;
    }
}
