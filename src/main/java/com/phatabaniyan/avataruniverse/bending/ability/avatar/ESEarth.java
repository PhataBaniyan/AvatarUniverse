package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code avatar.sphere.ESEarth}: steerable dirt boulder
 * fired from hotbar slot 2 while ElementSphere is active. Requires
 * earthbendable ground under the caster, consumes one earth use, and bursts
 * into a reverting crater on landing. Reference values: Cooldown 4000ms
 * (80 ticks), Damage 5, Crater 3, Revert 5000ms.
 */
public class ESEarth extends SphereAttack {
    public static final String ID = "ESEarth";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ESEARTH_COOLDOWN_MS.get());

    private static final double DAMAGE = Config.ESEARTH_DAMAGE.get();
    private static final int CRATER = Config.ESEARTH_CRATER.get();
    private static final long REVERT_MS = Config.ESEARTH_REVERT_MS.get();
    private static final double SPEED = Config.ESEARTH_SPEED.get();

    private FallingBlockEntity rock;
    private final Set<UUID> hit = new HashSet<>();

    public ESEarth(ServerPlayer player) {
        super(player);
        if (!gate(player.getUUID())) {
            return;
        }
        ElementSphere sphere = BendingManager.find(player.getUUID(), ElementSphere.class);
        if (sphere == null) {
            return;
        }
        if (sphere.getEarthUses() == 0) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || bending.isOnCooldown(ID, level.getGameTime())) {
            return;
        }
        bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        sphere.setEarthUses(sphere.getEarthUses() - 1);

        Vec3 eye = eyePos(player);
        Vec3 dir = player.getLookAngle().normalize();
        this.rock = FallingBlockEntity.fall(level, BlockPos.containing(eye.add(dir)), Blocks.DIRT.defaultBlockState());
        this.rock.setDeltaMovement(dir.scale(SPEED));
        this.rock.hurtMarked = true;
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive()) {
            discardRock();
            return false;
        }
        ServerPlayer sp = player;
        if (this.rock == null || this.rock.isRemoved()) {
            return false;
        }
        if (sp.isAlive()) {
            this.rock.setDeltaMovement(sp.getLookAngle().normalize().scale(SPEED));
            this.rock.hurtMarked = true;
        }
        level.sendParticles(
                BendingTheme.particle(Config.ESEARTH_TRAIL_PARTICLE.get(), ParticleTypes.LARGE_SMOKE),
                rock.getX(),
                rock.getY(),
                rock.getZ(),
                Config.ESEARTH_TRAIL_PARTICLE_COUNT.get(),
                0.4,
                0.4,
                0.4,
                0.03);

        for (Entity entity : level.getEntities(sp, rock.getBoundingBox().inflate(2.0))) {
            if (entity.getUUID().equals(sp.getUUID())
                    || !(entity instanceof LivingEntity living)
                    || entity instanceof ArmorStand
                    || !hit.add(entity.getUUID())) {
                continue;
            }
            living.hurt(sp.damageSources().magic(), (float) DAMAGE);
        }

        if (this.rock.onGround() || this.rock.isRemoved()) {
            Vec3 at = this.rock.position();
            discardRock();
            SphereBlast.crater(level, at, CRATER, REVERT_MS);
            level.sendParticles(
                    BendingTheme.particle(Config.ESEARTH_BLAST_PARTICLE.get(), ParticleTypes.EXPLOSION),
                    at.x,
                    at.y,
                    at.z,
                    Config.ESEARTH_BLAST_PARTICLE_COUNT.get(),
                    0.5,
                    0.5,
                    0.5,
                    0.1);
            level.sendParticles(
                    BendingTheme.particle(Config.ESEARTH_BLAST_SMOKE_PARTICLE.get(), ParticleTypes.LARGE_SMOKE),
                    at.x,
                    at.y,
                    at.z,
                    Config.ESEARTH_BLAST_SMOKE_PARTICLE_COUNT.get(),
                    0.5,
                    0.5,
                    0.5,
                    0.1);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 0.6F);
            for (Entity entity : level.getEntities(sp, new AABB(at, at).inflate(CRATER + 1))) {
                if (entity.getUUID().equals(sp.getUUID())
                        || !(entity instanceof LivingEntity living)
                        || entity instanceof ArmorStand) {
                    continue;
                }
                living.hurt(sp.damageSources().magic(), (float) DAMAGE);
            }
            return false;
        }
        return true;
    }

    private void discardRock() {
        if (this.rock != null && !this.rock.isRemoved()) {
            this.rock.discard();
        }
        this.rock = null;
    }

    @Override
    public void onRemove() {
        discardRock();
    }
}
