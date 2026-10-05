package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code Bloodbending}: hold sneak while facing an entity
 * to seize it, dragging it toward your gaze with slowed limbs until you let
 * go. Click while gripping to hurl it instead. Night, full-moon and avatar
 * gates are cut per mod rules (always usable); bloodless kinds, undead rules
 * and bloodbender immunity mirror the reference.
 */
public class Bloodbending extends BendingAbility {
    public static final String ID = "Bloodbending";

    private static final Set<EntityType<?>> BLOODLESS = Set.of(
            EntityType.SKELETON,
            EntityType.IRON_GOLEM,
            EntityType.BLAZE,
            EntityType.MAGMA_CUBE,
            EntityType.SHULKER,
            EntityType.SKELETON_HORSE,
            EntityType.WITHER_SKELETON,
            EntityType.STRAY);

    private static final Set<EntityType<?>> UNDEAD = Set.of(
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.BOGGED,
            EntityType.WITHER_SKELETON,
            EntityType.SKELETON_HORSE,
            EntityType.ZOMBIE,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.PHANTOM,
            EntityType.WITHER);

    private LivingEntity target;
    private final ServerLevel level;
    private final long startTick;

    public Bloodbending(ServerPlayer player, LivingEntity target) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.target = target;
        this.startTick = player.level().getGameTime();
    }

    /** Shared undead check (Korra isUndead approximation for blood rules). */
    public static boolean isUndead(LivingEntity entity) {
        return UNDEAD.contains(entity.getType());
    }

    /** First grippable entity along the gaze ray (Korra 1.7-block tolerance). */
    public static LivingEntity findTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        for (double d = 1.0; d <= range; d += 1.0) {
            Vec3 at = eye.add(look.scale(d));
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(at.x - 1.7, at.y - 1.7, at.z - 1.7, at.x + 1.7, at.y + 1.7, at.z + 1.7),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(player.getUUID()) || BLOODLESS.contains(entity.getType())) {
                    continue;
                }
                if (entity instanceof ServerPlayer victim) {
                    BendingPlayer bending = BendingPlayer.get(victim.getUUID());
                    if (bending != null
                            && bending.hasElement(BendingElement.WATER)
                            && bending.isToggled()
                            && !Config.BLOODBENDING_AFFECT_BLOODBENDERS.get()) {
                        continue;
                    }
                } else if (isUndead(entity) && !Config.BLOODBENDING_AFFECT_UNDEAD.get()) {
                    continue;
                }
                return entity;
            }
        }
        return null;
    }

    /** Hurl the gripped victim (Korra launch). */
    public void launch(ServerPlayer player) {
        if (target == null) {
            return;
        }
        Vec3 from = player.position();
        Vec3 dest = WaterSpoutWave.gazeTarget(player, from.distanceTo(target.position()));
        Vec3 push = dest.subtract(from).normalize().scale(Config.BLOODBENDING_KNOCKBACK.get());
        target.setDeltaMovement(push);
        target.hurtMarked = true;
        target.fallDistance = 0.0F;
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.msToTicks(Config.BLOODBENDING_COOLDOWN_MS.get()));
        }
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
        if (!player.isShiftKeyDown()) {
            endQuietly(bending);
            return false;
        }
        long duration = Config.msToTicks(Config.BLOODBENDING_DURATION_MS.get());
        if (duration > 0 && level.getGameTime() - startTick > duration) {
            endQuietly(bending);
            return false;
        }
        if (target == null || target.isRemoved() || !target.isAlive()) {
            return false;
        }
        if (target instanceof ServerPlayer victim) {
            BendingPlayer victimBending = BendingPlayer.get(victim.getUUID());
            if (victimBending != null
                    && victimBending.hasElement(BendingElement.WATER)
                    && victimBending.isToggled()
                    && !Config.BLOODBENDING_AFFECT_BLOODBENDERS.get()) {
                return false;
            }
        } else if (isUndead(target) && !Config.BLOODBENDING_AFFECT_UNDEAD.get()) {
            return false;
        }
        Vec3 dest = WaterSpoutWave.gazeTarget(player, 6.0);
        Vec3 to = dest.subtract(target.position());
        Vec3 push;
        if (to.lengthSqr() < 0.36) {
            push = Vec3.ZERO;
        } else {
            push = to.normalize().scale(0.5);
        }
        target.setDeltaMovement(push);
        target.hurtMarked = true;
        target.fallDistance = 0.0F;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        if (target instanceof Mob mob) {
            mob.setTarget(null);
        }
        if (target instanceof ServerPlayer victim && level.getGameTime() % 20 == 0) {
            victim.displayClientMessage(Component.literal("* Bloodbent *"), true);
        }
        return true;
    }

    private void endQuietly(BendingPlayer bending) {
        if (level.getGameTime() - startTick < 24L) {
            bending.setCooldown(ID, level.getGameTime() + Config.msToTicks(Config.BLOODBENDING_COOLDOWN_MS.get()));
        }
    }

    @Override
    public void onRemove() {}
}
