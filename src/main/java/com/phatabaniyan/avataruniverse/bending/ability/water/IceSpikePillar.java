package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code IceSpikePillar}: a spike of ice erupts from an
 * icebendable base, rising one block per tick to its full height, damaging and
 * launching whoever it catches (once each) plus slowness, then sinking straight
 * back down (Korra duration is unset, so there is no linger). Throws only from
 * ice with a clear column above that does not cross the caster's eyes.
 */
public class IceSpikePillar extends BendingAbility {
    public static final String ID = "IceSpikePillar";
    /** Shared player-facing bind (Korra getName "IceSpike" for all three). */
    public static final String BIND = "IceSpike";

    private final ServerLevel level;
    private final double damage;
    private final double throwForce;
    private final int height;
    private BlockPos sourceBlock;
    private Vec3 location;
    private int progress;
    private final Map<BlockPos, TempBlock> ice = new HashMap<>();
    private final List<LivingEntity> damaged = new ArrayList<>();

    public IceSpikePillar(ServerPlayer player, BlockPos base, double damage, double throwForce) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.damage = damage;
        this.throwForce = throwForce;
        this.height = Config.ICESPIKE_HEIGHT.get();
        this.sourceBlock = base.immutable();
        this.location = Vec3.atCenterOf(base);
    }

    /** Korra canInstantiate: icebendable base, clear air column, never through the eyes. */
    public boolean canInstantiate(ServerPlayer player) {
        if (!BendingSources.isIce(level, sourceBlock)) {
            return false;
        }
        BlockPos eye = BlockPos.containing(player.getEyePosition());
        for (int i = 1; i <= height; i++) {
            BlockPos pos = sourceBlock.above(i);
            if (!level.getBlockState(pos).isAir()) {
                return false;
            }
            if (pos.getX() == eye.getX() && pos.getZ() == eye.getZ()) {
                return false;
            }
        }
        return true;
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
        if (progress < height) {
            progress++;
            location = location.add(0.0, 1.0, 0.0);
            BlockPos cell = BlockPos.containing(location);
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(
                            location.x - 1.4,
                            location.y - 1.4,
                            location.z - 1.4,
                            location.x + 1.4,
                            location.y + 1.4,
                            location.z + 1.4),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner) || damaged.contains(entity)) {
                    continue;
                }
                affect(entity);
            }
            ice.put(cell.immutable(), new TempBlock(level, cell.immutable(), Blocks.ICE.defaultBlockState()));
            return true;
        }
        if (!sinkPillar()) {
            return false;
        }
        return true;
    }

    private void affect(LivingEntity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, throwForce, 0.0));
        entity.hurtMarked = true;
        entity.hurt(level.damageSources().magic(), (float) damage);
        damaged.add(entity);
        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                Config.ICESPIKE_SLOW_DURATION_TICKS.get(),
                Config.ICESPIKE_SLOW_POTENCY.get()));
    }

    /** Reverse of the rise (Korra sinkPillar). */
    private boolean sinkPillar() {
        TempBlock top = ice.remove(BlockPos.containing(location));
        if (top != null) {
            top.revert();
        }
        location = location.add(0.0, -1.0, 0.0);
        return !BlockPos.containing(location).equals(sourceBlock);
    }

    @Override
    public void onRemove() {
        for (TempBlock temp : new java.util.ArrayList<>(ice.values())) {
            temp.revert();
        }
        ice.clear();
    }
}
