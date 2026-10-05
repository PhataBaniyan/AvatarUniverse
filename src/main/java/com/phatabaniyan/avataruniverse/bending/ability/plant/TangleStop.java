package com.phatabaniyan.avataruniverse.bending.ability.plant;

import com.phatabaniyan.avataruniverse.bending.TempBlock;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code MovementHandler} used by Tangle: a hard root
 * for a fixed duration. Mobs get {@code setNoAi(true)} (the reference
 * disables AI); players are re-aligned to their capture point each tick so
 * they cannot walk/jump/teleport during the effect and get the actionbar
 * message. Restores on expiry. Every root pairs with an oak-leaves
 * {@link TempBlock} at the feet.
 */
public final class TangleStop {
    private static final Map<UUID, Root> STOPPED = new ConcurrentHashMap<>();

    private static final class Root {
        LivingEntity entity;
        Vec3 storedPos;
        long expireAt;
        boolean mob;
        TempBlock leaves;
    }

    private TangleStop() {}

    public static void stop(LivingEntity target, ServerLevel level, int durationTicks) {
        Root root = new Root();
        root.entity = target;
        root.storedPos = target.position();
        root.expireAt = level.getGameTime() + durationTicks;
        root.mob = target instanceof Mob;
        root.leaves = new TempBlock(level, target.blockPosition(), Blocks.OAK_LEAVES.defaultBlockState());
        if (root.mob) {
            ((Mob) target).setNoAi(true);
        }
        STOPPED.put(target.getUUID(), root);
    }

    public static void tick(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        for (Root root : new java.util.ArrayList<>(STOPPED.values())) {
            if (root.entity == null || !root.entity.isAlive()) {
                release(root);
                continue;
            }
            if (gameTime >= root.expireAt) {
                release(root);
                continue;
            }
            root.entity.setDeltaMovement(Vec3.ZERO);
            root.entity.hurtMarked = true;
            if (!root.mob && root.entity instanceof ServerPlayer player) {
                player.teleportTo(
                        player.serverLevel(),
                        root.storedPos.x,
                        root.storedPos.y,
                        root.storedPos.z,
                        java.util.Collections.emptySet(),
                        player.getYRot(),
                        player.getXRot());
                player.displayClientMessage(Component.literal("* Tangled *"), true);
            }
        }
    }

    private static void release(Root root) {
        UUID id = root.entity == null ? null : root.entity.getUUID();
        try {
            if (root.mob && root.entity instanceof Mob mob) {
                mob.setNoAi(false);
            }
        } catch (Throwable ignored) {
        }
        if (id != null) {
            STOPPED.remove(id);
        }
        if (root.leaves != null) {
            root.leaves.revert();
        }
    }

    public static boolean isStopped(LivingEntity entity) {
        return STOPPED.containsKey(entity.getUUID());
    }
}
