package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code IceSpikePillarField}: a one-shot sneak burst
 * that raises up to nine spikes from icebendable ground around the caster
 * (radius 6), preferring columns under nearby entities and scattering the
 * rest at random. pays the field cooldown when at least one spike rises.
 */
public class IceSpikePillarField extends BendingAbility {
    public static final String ID = "IceSpikePillarField";
    /** Shared player-facing bind (Korra getName "IceSpike" for all three). */
    public static final String BIND = "IceSpike";

    private final ServerLevel level;
    private boolean done;

    public IceSpikePillarField(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
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
        if (done) {
            return false;
        }
        done = true;
        double radius = Config.ICESPIKE_FIELD_RADIUS.get();
        int spikes = (int) (radius * radius / 4.0);
        BlockPos feet = player.blockPosition();
        List<BlockPos> ground = new ArrayList<>();
        for (int x = -(int) (radius - 1); x <= radius - 1; x++) {
            column:
            for (int z = -(int) (radius - 1); z <= radius - 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos pos = feet.offset(x, y, z);
                    Vec3 dummy = player.position().add(0.0, y, 0.0);
                    if (BendingSources.isIce(level, pos)
                            && level.getBlockState(pos.above()).isAir()
                            && dummy.distanceToSqr(Vec3.atCenterOf(pos)) > 1.5 * 1.5
                            && !pos.equals(feet)) {
                        ground.add(pos.immutable());
                        continue column;
                    }
                }
            }
        }
        List<Entity> entities = new ArrayList<>(level.getEntitiesOfClass(
                Entity.class,
                new AABB(
                        player.getX() - radius,
                        player.getY() - radius,
                        player.getZ() - radius,
                        player.getX() + radius,
                        player.getY() + radius,
                        player.getZ() + radius),
                e -> e.isAlive()));
        int raised = 0;
        for (int i = 0; i < spikes && !ground.isEmpty(); i++) {
            BlockPos targetBlock = null;
            for (Entity entity : new ArrayList<>(entities)) {
                if (!(entity instanceof LivingEntity) || entity.getUUID().equals(owner)) {
                    continue;
                }
                for (BlockPos pos : ground) {
                    if (pos.getX() == entity.blockPosition().getX()
                            && pos.getZ() == entity.blockPosition().getZ()) {
                        targetBlock = pos;
                        entities.remove(entity);
                        level.playSound(
                                null,
                                pos,
                                net.minecraft.sounds.SoundEvents.GLASS_BREAK,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                0.5F,
                                1.3F);
                        break;
                    }
                }
                if (targetBlock != null) {
                    break;
                }
            }
            if (targetBlock == null) {
                targetBlock = ground.get(level.random.nextInt(ground.size()));
            }
            if (!level.getBlockState(targetBlock.above()).is(Blocks.ICE)) {
                IceSpikePillar pillar = new IceSpikePillar(
                        player, targetBlock, Config.ICESPIKE_FIELD_DAMAGE.get(), Config.ICESPIKE_FIELD_KNOCKUP.get());
                if (pillar.canInstantiate(player)) {
                    BendingManager.start(pillar);
                    raised++;
                }
                ground.remove(targetBlock);
            }
        }
        if (raised > 0) {
            bending.setCooldown(ID, level.getGameTime() + Config.ICESPIKE_FIELD_COOLDOWN_TICKS.get());
        }
        return false;
    }

    @Override
    public void onRemove() {}
}
