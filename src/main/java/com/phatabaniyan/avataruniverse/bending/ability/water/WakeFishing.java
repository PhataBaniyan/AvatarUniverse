package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Port of JedCore {@code WakeFishing}: hold sneak facing a water block to
 * churn it. Splash rings rise while a fish works its way up, then flings out
 * toward you. Look away, release sneak, or run out the timer to stop.
 */
public class WakeFishing extends BendingAbility {
    public static final String ID = "WakeFishing";

    private final ServerLevel level;
    private final BlockPos focused;
    private final long startTick;
    private int point;

    public WakeFishing(ServerPlayer player, BlockPos focused) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.focused = focused.immutable();
        this.startTick = player.level().getGameTime();
    }

    /** Water block in sight within range (Korra SHIFT_DOWN eye-ray). */
    public static BlockPos findWater(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (level.getFluidState(pos).is(net.minecraft.world.level.material.Fluids.WATER)) {
                return pos.immutable();
            }
        }
        return null;
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
        BlockPos seen = findWater(player, Config.WAKEFISHING_RANGE.get());
        if (seen == null || !seen.equals(focused)) {
            return false;
        }
        if (level.getGameTime() - startTick > Config.WAKEFISHING_DURATION_TICKS.get()) {
            return false;
        }
        point = (point + 1) % 32;
        Vec3 center = Vec3.atCenterOf(focused);
        for (int i = 0; i < 4; i++) {
            double angle = Math.toRadians(point * (360.0 / 32.0) + i * 90.0);
            double x = center.x + Math.cos(angle) * 1.0;
            double z = center.z + Math.sin(angle) * 1.0;
            level.sendParticles(
                    BendingTheme.particle(Config.WAKEFISHING_SPLASH_PARTICLE.get(), ParticleTypes.SPLASH),
                    x,
                    center.y + 1.0,
                    z,
                    Config.WAKEFISHING_SPLASH_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.05);
            level.sendParticles(
                    BendingTheme.particle(Config.WAKEFISHING_BUBBLE_PARTICLE.get(), ParticleTypes.BUBBLE),
                    x,
                    center.y + 0.4,
                    z,
                    Config.WAKEFISHING_BUBBLE_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.02);
        }
        level.sendParticles(
                BendingTheme.particle(Config.WAKEFISHING_SMOKE_PARTICLE.get(), ParticleTypes.SMOKE),
                center.x,
                center.y + 0.5,
                center.z,
                Config.WAKEFISHING_SMOKE_PARTICLE_COUNT.get(),
                0.0,
                0.0,
                0.0,
                0.001);
        if (level.random.nextInt(50) == 0) {
            ItemStack fish;
            switch (level.random.nextInt(4)) {
                case 1:
                    fish = new ItemStack(Items.PUFFERFISH);
                    break;
                case 2:
                    fish = new ItemStack(Items.TROPICAL_FISH);
                    break;
                case 3:
                    fish = new ItemStack(Items.SALMON);
                    break;
                default:
                    fish = new ItemStack(Items.COD);
                    break;
            }
            ItemEntity item = new ItemEntity(level, center.x, center.y + 1.5, center.z, fish);
            Vec3 fling =
                    player.getEyePosition().subtract(center.add(0.0, 1.5, 0.0)).scale(0.15);
            item.setDeltaMovement(fling);
            level.addFreshEntity(item);
        }
        return true;
    }

    @Override
    public void onRemove() {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + Config.WAKEFISHING_COOLDOWN_TICKS.get());
        }
    }
}
