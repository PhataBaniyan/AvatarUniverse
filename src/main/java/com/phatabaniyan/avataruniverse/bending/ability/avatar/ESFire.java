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
 * Port of ProjectAvatar {@code avatar.sphere.ESFire}: steerable flame bolt
 * fired from hotbar slot 3 while ElementSphere is active. Consumes one fire
 * use, scorches the ground it crosses, burns what it hits. Reference values:
 * Cooldown 1500ms (30 ticks), Range 25, Damage 3, Burn 60 ticks,
 * Speed 3, Controllable true.
 */
public class ESFire extends SphereAttack {
    public static final String ID = "ESFire";

    /** Reference Cooldown 1500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.ESFIRE_COOLDOWN_TICKS.get();

    private static final double RANGE = Config.ESFIRE_RANGE.get();
    private static final double DAMAGE = Config.ESFIRE_DAMAGE.get();
    private static final int BURN_TICKS = Config.ESFIRE_BURN_TICKS.get();
    private static final int SPEED = Config.ESFIRE_SPEED.get();
    private static final boolean CONTROLLABLE = Config.ESFIRE_CONTROLLABLE.get();

    private Vec3 pos;
    private Vec3 dir;
    private double travelled = 0;

    public ESFire(ServerPlayer player) {
        super(player);
        if (!gate(player.getUUID())) {
            return;
        }
        ElementSphere sphere = BendingManager.find(player.getUUID(), ElementSphere.class);
        if (sphere == null) {
            return;
        }
        if (sphere.getFireUses() == 0) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || bending.isOnCooldown(ID, level.getGameTime())) {
            return;
        }
        bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        sphere.setFireUses(sphere.getFireUses() - 1);

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
            if (CONTROLLABLE && sp.isAlive()) {
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
                    BendingTheme.particle(Config.ESFIRE_TRAIL_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    pos.x,
                    pos.y,
                    pos.z,
                    Config.ESFIRE_TRAIL_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.03);
            level.sendParticles(
                    BendingTheme.particle(Config.ESFIRE_SMOKE_PARTICLE.get(), ParticleTypes.LARGE_SMOKE),
                    pos.x,
                    pos.y,
                    pos.z,
                    Config.ESFIRE_SMOKE_PARTICLE_COUNT.get(),
                    0.25,
                    0.25,
                    0.25,
                    0.02);
            BlockPos below = bp.below();
            if (level.getBlockState(below).isSolidRender(level, below)
                    && level.getBlockState(bp).isAir()) {
                level.setBlockAndUpdate(bp, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
            }
            for (Entity entity : level.getEntities(sp, new AABB(pos, pos).inflate(2.5))) {
                if (entity.getUUID().equals(sp.getUUID())
                        || !(entity instanceof LivingEntity living)
                        || entity instanceof ArmorStand) {
                    continue;
                }
                living.hurt(sp.damageSources().magic(), (float) DAMAGE);
                living.igniteForSeconds(BURN_TICKS / 20.0F);
                this.travelled = RANGE;
                return true;
            }
        }
        return true;
    }
}
