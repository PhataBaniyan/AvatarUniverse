package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of JedCore {@code IceClaws}: hold sneak to pull frost into claws at
 * your fingertips (about a second of charging), then click while sneaking to
 * hurl them, or simply strike in melee. Hits slow hard; punch and throw share
 * their cooldowns. Hand-swapping is cosmetic-only upstream and omitted.
 */
public class IceClaws extends BendingAbility {
    public static final String ID = "IceClaws";

    private static final DustParticleOptions CYAN = new DustParticleOptions(new Vector3f(0.4F, 1.0F, 1.0F), 1.0F);
    private static final DustParticleOptions PALE = new DustParticleOptions(new Vector3f(0.8F, 1.0F, 1.0F), 1.0F);

    private final ServerLevel level;
    private final long startTick;
    private boolean launched;
    private Vec3 head;
    private Vec3 origin;
    private Vec3 launchDir;

    public IceClaws(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.startTick = player.level().getGameTime();
    }

    public boolean isCharged() {
        return level.getGameTime() - startTick >= Config.msToTicks(Config.ICECLAWS_CHARGE_MS.get());
    }

    /** Hurl the charged claws (Korra throwClaws, sneak held). */
    public void throwClaws(ServerPlayer player) {
        if (!isCharged() || launched) {
            return;
        }
        launched = true;
        head = player.getEyePosition();
        origin = head;
        launchDir = player.getLookAngle().normalize();
        if (Config.ICECLAWS_THROW_COOLDOWN_ON_THROW.get()) {
            BendingPlayer bending = BendingPlayer.get(owner);
            if (bending != null) {
                bending.setCooldown(
                        ID, level.getGameTime() + Config.msToTicks(Config.ICECLAWS_THROW_COOLDOWN_MS.get()));
            }
        }
    }

    /** Melee strike with live claws (Korra punch path). */
    public void punch(LivingEntity victim) {
        victim.removeEffect(MobEffects.MOVEMENT_SPEED);
        victim.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                Config.msToTicks(Config.ICECLAWS_PUNCH_SLOW_MS.get()),
                Config.ICECLAWS_PUNCH_SLOW_LEVEL.get()));
        victim.hurt(
                level.damageSources().magic(),
                Config.ICECLAWS_PUNCH_DAMAGE.get().floatValue());
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.msToTicks(Config.ICECLAWS_PUNCH_COOLDOWN_MS.get()));
        }
        BendingManager.remove(this);
    }

    private Vec3 handPos(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0.0, look.x);
        double len = side.length();
        if (len > 1e-6) {
            side = side.scale(1.0 / len);
        }
        return player.getEyePosition()
                .add(look.scale(0.75))
                .add(side.scale(0.55))
                .add(0.0, -0.4, 0.0);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.isRemoved() || player.isDeadOrDying()) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null || !bending.hasElement(BendingElement.WATER) || !bending.isToggled()) {
            return false;
        }
        if (!isCharged()) {
            if (!player.isShiftKeyDown()) {
                return false;
            }
            Vec3 hand = handPos(player);
            level.sendParticles(
                    BendingTheme.particle(Config.ICECLAWS_CHARGE_PARTICLE.get(), ParticleTypes.SPLASH),
                    hand.x,
                    hand.y,
                    hand.z,
                    Config.ICECLAWS_CHARGE_PARTICLE_COUNT.get(),
                    0.2,
                    0.2,
                    0.2,
                    0.0);
            return true;
        }
        if (!launched && Config.ICECLAWS_THROWABLE.get()) {
            Vec3 hand = handPos(player);
            level.sendParticles(CYAN, hand.x, hand.y, hand.z, 1, 0.1, 0.1, 0.1, 0.0);
            level.sendParticles(PALE, hand.x, hand.y, hand.z, 1, 0.1, 0.1, 0.1, 0.0);
            return true;
        }
        return shoot();
    }

    private boolean shoot() {
        double speed = Config.ICECLAWS_THROW_SPEED.get();
        double range = Config.ICECLAWS_RANGE.get();
        for (double i = 0.0; i < 1.0; i += speed) {
            head = head.add(launchDir.scale(speed));
            if (head.distanceToSqr(origin) >= range * range) {
                return false;
            }
            if (!com.phatabaniyan.avataruniverse.bending.BendingSources.isTransparentForBend(
                    level, net.minecraft.core.BlockPos.containing(head))) {
                return false;
            }
            level.sendParticles(CYAN, head.x, head.y, head.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(PALE, head.x, head.y, head.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(
                    BendingTheme.particle(Config.ICECLAWS_TRAIL_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                    head.x,
                    head.y,
                    head.z,
                    Config.ICECLAWS_TRAIL_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.0);
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(head.x - 1.5, head.y - 1.5, head.z - 1.5, head.x + 1.5, head.y + 1.5, head.z + 1.5),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)
                        || entity instanceof net.minecraft.world.entity.decoration.ArmorStand) {
                    continue;
                }
                entity.removeEffect(MobEffects.MOVEMENT_SPEED);
                entity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        Config.msToTicks(Config.ICECLAWS_THROW_SLOW_MS.get()),
                        Config.ICECLAWS_THROW_SLOW_LEVEL.get()));
                entity.hurt(
                        level.damageSources().magic(),
                        Config.ICECLAWS_THROW_DAMAGE.get().floatValue());
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRemove() {}
}
