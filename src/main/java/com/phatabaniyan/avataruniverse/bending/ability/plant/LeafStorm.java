package com.phatabaniyan.avataruniverse.bending.ability.plant;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Standalone port of ProjectAddons combo {@code LeafStorm}: hold sneak on
 * hotbar slot 1 to whirl ten leaves around the eyes. Leaves die on blocks
 * and victims, dealing a half-heart bite each. Reference values: Cooldown
 * 7000ms, LeafCount 10, LeafSpeed 14 degrees/tick, Damage 0.5, Radius 6.
 * The reference's PlantArmor shell and armor cost are cut — this is a free
 * storm of its own.
 */
public class LeafStorm extends BendingAbility {
    public static final String ID = "LeafStorm";

    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.LEAFSTORM_COOLDOWN_MS.get());

    private static final int LEAF_COUNT = Config.LEAFSTORM_LEAF_COUNT.get();
    private static final double LEAF_SPEED = Config.LEAFSTORM_LEAF_SPEED.get();
    private static final double DAMAGE = Config.LEAFSTORM_DAMAGE.get();
    private static final double RADIUS = Config.LEAFSTORM_RADIUS.get();
    private static final BlockParticleOption LEAF =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_LEAVES.defaultBlockState());

    private final ServerLevel level;
    private final ServerPlayer player;
    private final List<Leaf> leaves = new ArrayList<>();

    private static final class Leaf {
        Vec3 pos;
        double angle;
        final double radius;

        Leaf(Vec3 pos, double angle, double radius) {
            this.pos = pos;
            this.angle = angle;
            this.radius = radius;
        }
    }

    public LeafStorm(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        for (int i = 0; i < LEAF_COUNT; i++) {
            double angle = level.random.nextDouble() * 360.0;
            double offset = level.random.nextDouble() * RADIUS + 0.5;
            leaves.add(new Leaf(eye.add(0, level.random.nextDouble() * 2.0 - 1.0, 0), angle, offset));
        }
    }

    @Override
    public String name() {
        return ID;
    }

    /** Standalone storm: needs only a waterbender holding sneak. */
    public static boolean canBegin(ServerPlayer player) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        return bending != null && bending.hasElement(BendingElement.WATER) && bending.isToggled();
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!player.isShiftKeyDown() || player.getInventory().selected != 0) {
            return false;
        }
        Vec3 eye = player.getEyePosition();
        Iterator<Leaf> it = leaves.iterator();
        while (it.hasNext()) {
            Leaf leaf = it.next();
            double x = leaf.radius * Math.cos(Math.toRadians(leaf.angle));
            double z = leaf.radius * Math.sin(Math.toRadians(leaf.angle));
            leaf.pos = new Vec3(eye.x + x, leaf.pos.y, eye.z + z);
            leaf.angle += LEAF_SPEED;
            if (!level.getBlockState(BlockPos.containing(leaf.pos)).isAir()
                    && level.getBlockState(BlockPos.containing(leaf.pos)).isSolid()) {
                it.remove();
                continue;
            }
            level.sendParticles(LEAF, leaf.pos.x, leaf.pos.y, leaf.pos.z, 2, 0.2, 0.2, 0.2, 0.0);
            boolean bitten = false;
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(leaf.pos.subtract(0.5, 0.5, 0.5), leaf.pos.add(0.5, 0.5, 0.5)),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                entity.hurt(player.damageSources().playerAttack(player), (float) DAMAGE);
                entity.invulnerableTime = 0;
                bitten = true;
                break;
            }
            if (bitten) {
                it.remove();
            }
        }
        return !leaves.isEmpty();
    }

    @Override
    public void onRemove() {
        cool(owner, player, ID, COOLDOWN_TICKS);
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.WATER) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
