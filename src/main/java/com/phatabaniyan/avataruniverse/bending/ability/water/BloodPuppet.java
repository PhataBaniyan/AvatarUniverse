package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code BloodPuppet}: sneak to seize a victim and steer it
 * around, click to make most puppets lash out (skeletons loose an arrow,
 * everyone else -- or an armed player puppet -- strikes in melee). Releases
 * on sneak release, timeout or death. Night gates and chi hooks are cut with
 * the reference's server systems; the unreachable creeper/ghast/blaze/witch
 * branches are omitted as dead upstream code.
 */
public class BloodPuppet extends BendingAbility {
    public static final String ID = "BloodPuppet";

    private static final Map<UUID, Long> GRABBED = new ConcurrentHashMap<>();

    private final ServerLevel level;
    private LivingEntity puppet;
    private final long startTick;

    public BloodPuppet(ServerPlayer player, LivingEntity puppet) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.puppet = puppet;
        this.startTick = player.level().getGameTime();
        GRABBED.put(puppet.getUUID(), player.level().getGameTime());
    }

    /** First grabbable victim along the gaze ray (JedCore grab). */
    public static LivingEntity findVictim(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        int distance = Config.BLOODPUPPET_DISTANCE.get();
        boolean throughWalls = Config.BLOODPUPPET_IGNORE_WALLS.get();
        for (int i = 1; i < distance; i++) {
            Vec3 at = eye.add(look.scale(i));
            BlockPos pos = BlockPos.containing(at);
            if (!throughWalls && !BendingSources.isTransparentForBend(level, pos)) {
                break;
            }
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(at.x - 1.7, at.y - 1.7, at.z - 1.7, at.x + 1.7, at.y + 1.7, at.z + 1.7),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(player.getUUID()) || GRABBED.containsKey(entity.getUUID())) {
                    continue;
                }
                if (Bloodbending.isUndead(entity) && !Config.BLOODPUPPET_ALLOW_UNDEAD.get()) {
                    continue;
                }
                if (entity instanceof ServerPlayer victim) {
                    BendingPlayer bending = BendingPlayer.get(victim.getUUID());
                    if (bending != null
                            && bending.hasElement(BendingElement.WATER)
                            && bending.isToggled()
                            && !Config.BLOODPUPPET_AFFECT_BLOODBENDERS.get()) {
                        continue;
                    }
                }
                return entity;
            }
        }
        return null;
    }

    private static boolean canAttack(EntityType<?> type) {
        return type == EntityType.SKELETON
                || type == EntityType.SPIDER
                || type == EntityType.GIANT
                || type == EntityType.ZOMBIE
                || type == EntityType.SLIME
                || type == EntityType.GHAST
                || type == EntityType.PIGLIN
                || type == EntityType.ZOMBIFIED_PIGLIN
                || type == EntityType.ENDERMAN
                || type == EntityType.CAVE_SPIDER
                || type == EntityType.SILVERFISH
                || type == EntityType.BLAZE
                || type == EntityType.MAGMA_CUBE
                || type == EntityType.WITCH
                || type == EntityType.ENDERMITE
                || type == EntityType.DROWNED
                || type == EntityType.PLAYER;
    }

    /** Puppet lash-out (JedCore attack). */
    public void attack() {
        if (puppet == null || !canAttack(puppet.getType())) {
            return;
        }
        if (puppet.getType() == EntityType.SKELETON) {
            for (LivingEntity victim : level.getEntitiesOfClass(
                    LivingEntity.class, puppet.getBoundingBox().inflate(5.0), LivingEntity::isAlive)) {
                if (victim.getUUID().equals(puppet.getUUID())) {
                    continue;
                }
                Vec3 dir = victim.getEyePosition()
                        .subtract(puppet.getEyePosition())
                        .normalize();
                Arrow arrow = new Arrow(EntityType.ARROW, level);
                arrow.setPos(puppet.getEyePosition().x, puppet.getEyePosition().y, puppet.getEyePosition().z);
                arrow.shoot(dir.x, dir.y, dir.z, 0.6F, 12.0F);
                level.addFreshEntity(arrow);
                if (victim instanceof Mob mob) {
                    mob.setTarget(puppet);
                }
                break;
            }
            return;
        }
        for (LivingEntity victim : level.getEntitiesOfClass(
                LivingEntity.class, puppet.getBoundingBox().inflate(2.0), LivingEntity::isAlive)) {
            if (victim.getUUID().equals(puppet.getUUID())) {
                continue;
            }
            float damage = 2.0F;
            if (puppet instanceof ServerPlayer holder) {
                ItemStack hand = holder.getMainHandItem();
                if (hand.is(Items.WOODEN_SWORD) || hand.is(Items.GOLDEN_SWORD)) {
                    damage = 5.0F;
                } else if (hand.is(Items.STONE_SWORD)) {
                    damage = 6.0F;
                } else if (hand.is(Items.IRON_SWORD)) {
                    damage = 7.0F;
                } else if (hand.is(Items.DIAMOND_SWORD)) {
                    damage = 8.0F;
                }
            }
            victim.hurt(level.damageSources().mobAttack(puppet), damage);
            victim.invulnerableTime = 0;
            if (victim instanceof Mob mob) {
                mob.setTarget(puppet);
            }
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
            return false;
        }
        if (level.getGameTime() - startTick > Config.BLOODPUPPET_HOLD_TICKS.get()) {
            return false;
        }
        if (puppet == null || puppet.isRemoved() || !puppet.isAlive()) {
            return false;
        }
        if (puppet instanceof ServerPlayer victim && (victim.hasDisconnected() || !victim.isAlive())) {
            return false;
        }
        Vec3 dest = WaterSpoutWave.gazeTarget(player, Config.BLOODPUPPET_DISTANCE.get() + 1.0);
        Vec3 to = dest.subtract(puppet.position());
        if (to.lengthSqr() > 0.25) {
            puppet.setDeltaMovement(to.normalize().scale(0.5));
        } else {
            puppet.setDeltaMovement(Vec3.ZERO);
        }
        puppet.hurtMarked = true;
        puppet.fallDistance = 0.0F;
        if (puppet instanceof Mob mob) {
            mob.setTarget(null);
        }
        return true;
    }

    @Override
    public void onRemove() {
        if (puppet != null) {
            GRABBED.remove(puppet.getUUID());
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.BLOODPUPPET_COOLDOWN_TICKS.get());
        }
    }
}
