package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code DrainBlast}: a gathered water shot that steers
 * toward the gaze each step, laying a quick-melting trail, and bursts for
 * damage on contact or walls. Internal to {@link Drain}, never bound.
 */
public class DrainBlast extends BendingAbility {
    public static final String ID = "DrainBlast";

    private final ServerLevel level;
    private Vec3 location;
    private double travelled;

    public DrainBlast(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        this.location = eye.add(player.getLookAngle().normalize().scale(Config.DRAIN_HOLD_RANGE.get()));
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
        double range = Config.DRAIN_BLAST_RANGE.get();
        double speed = Config.DRAIN_BLAST_SPEED.get();
        if (travelled >= range) {
            return false;
        }
        int steps = Math.max(1, (int) Math.ceil(speed));
        for (int i = 0; i < steps; i++) {
            travelled += 1.0;
            if (travelled >= range) {
                return false;
            }
            Vec3 dest = WaterSpoutWave.gazeTarget(player, range);
            Vec3 dir = dest.subtract(location);
            if (dir.lengthSqr() < 1e-6) {
                return false;
            }
            location = location.add(dir.normalize());
            BlockPos cell = BlockPos.containing(location);
            if (!BendingSources.isTransparentForBend(level, cell)) {
                return false;
            }
            BendingManager.scheduleRevert(
                    new TempBlock(level, cell.immutable(), Blocks.WATER.defaultBlockState(), TempBlock.QUIET),
                    level.getGameTime() + 2L);
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(
                            location.x - 2.5,
                            location.y - 2.5,
                            location.z - 2.5,
                            location.x + 2.5,
                            location.y + 2.5,
                            location.z + 2.5),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                entity.hurt(
                        level.damageSources().magic(),
                        Config.DRAIN_BLAST_DAMAGE.get().floatValue());
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRemove() {}
}
