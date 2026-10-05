package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code EarthArmor}. Sneak targeting a 2-high earth
 * column: the two blocks tear out of the ground and fly onto the body as
 * {@link Display.BlockDisplay} cubes, then settle as earth-dyed leather
 * armor plus 8 absorption hearts (Absorption II, like the reference's
 * GoldHearts 4). Sneak-click shatters the shell early. Lasts 350t, cooldown
 * 150t.
 */
public class EarthArmor extends EarthAbility {
    public static final String ID = "EarthArmor";
    /** Reference MaxDuration 17500ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.EARTHARMOR_DURATION_MS.get());
    /** Reference Cooldown 7500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.EARTHARMOR_COOLDOWN_MS.get());

    private static final int FORM_TICKS = Config.msToTicks(Config.EARTHARMOR_FORM_MS.get());
    private static final double SELECT_RANGE = Config.EARTHARMOR_SELECT_RANGE.get();
    private static final float ABSORPTION = Config.EARTHARMOR_ABSORPTION.get().floatValue();
    private static final double MIN_ABSORPTION = Config.EARTHARMOR_MIN_ABSORPTION.get();

    private boolean forming;
    private boolean formed;
    private long formedAt;
    private int formAge;
    private final List<UUID> shards = new ArrayList<>();
    private final List<net.minecraft.world.item.ItemStack> savedArmor = new ArrayList<>();
    private BlockState headMaterial = Blocks.DIRT.defaultBlockState();
    private BlockState legsMaterial = Blocks.DIRT.defaultBlockState();

    public EarthArmor(ServerPlayer player) {
        super(player);
    }

    @Override
    public String name() {
        return ID;
    }

    public boolean isFormed() {
        return formed;
    }

    /** True when either source column was metal (iron/gold/copper), for MetalArmor. */
    public boolean isMetalSourced() {
        return isMetalState(headMaterial) || isMetalState(legsMaterial);
    }

    /** True when the head column was gold, for golden MetalArmor. */
    public boolean isGoldSourced() {
        return headMaterial
                .getBlock()
                .getDescriptionId()
                .toUpperCase(java.util.Locale.ROOT)
                .contains("GOLD");
    }

    /** Sneak-start gate: a 2-high earthbendable column in view. */
    public static boolean canBegin(ServerPlayer player) {
        BlockPos target = eyeTarget(player, SELECT_RANGE);
        if (target == null) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        return Accretion.isEarthbendable(level, target) && Accretion.isEarthbendable(level, target.below());
    }

    private static BlockPos eyeTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!player.serverLevel().getBlockState(pos).isAir()) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** Click path: shatter the shell with block cracks. */
    public void click() {
        if (!formed) {
            return;
        }
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, headMaterial),
                player.getX(),
                player.getY() + 1.6,
                player.getZ(),
                8,
                0.1,
                0.1,
                0.1,
                0.0);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, legsMaterial),
                player.getX(),
                player.getY() + 0.6,
                player.getZ(),
                8,
                0.1,
                0.1,
                0.1,
                0.0);
        BendingManager.remove(this);
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!formed) {
            if (!forming) {
                if (!beginForm()) {
                    return false;
                }
                forming = true;
            }
            flyShards();
            formAge++;
            if (formAge < FORM_TICKS) {
                return true;
            }
            formArmor();
            formed = true;
            formedAt = level.getGameTime();
            if (isMetalSourced() && BendingManager.find(owner, MetalArmor.class) == null) {
                BendingManager.start(new MetalArmor(player));
            }
            return true;
        }
        if (player.getAbsorptionAmount() < MIN_ABSORPTION || level.getGameTime() - formedAt > DURATION_TICKS) {
            return false;
        }
        return true;
    }

    /** Tear the column out and launch its blocks at the body as displays. */
    private boolean beginForm() {
        BlockPos target = eyeTarget(player, SELECT_RANGE);
        if (target == null
                || !Accretion.isEarthbendable(level, target)
                || !Accretion.isEarthbendable(level, target.below())) {
            return false;
        }
        headMaterial = level.getBlockState(target);
        legsMaterial = level.getBlockState(target.below());
        TempBlock headTemp = new TempBlock(level, target, Blocks.AIR.defaultBlockState());
        TempBlock legsTemp = new TempBlock(level, target.below(), Blocks.AIR.defaultBlockState());
        BendingManager.scheduleRevert(headTemp, level.getGameTime() + DURATION_TICKS);
        BendingManager.scheduleRevert(legsTemp, level.getGameTime() + DURATION_TICKS);
        spawnShard(headMaterial, target);
        spawnShard(legsMaterial, target.below());
        return true;
    }

    private void spawnShard(BlockState state, BlockPos cell) {
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        display.addTag("avataruniverse_eartharmor");
        setBlockState(display, state);
        display.setPos(cell.getX(), cell.getY(), cell.getZ());
        level.addFreshEntity(display);
        shards.add(display.getUUID());
    }

    /** Fly each shard toward its body slot with a crack trail. */
    private void flyShards() {
        Vec3 headSlot = player.position().add(0, 1.5, 0);
        Vec3 legsSlot = player.position().add(0, 0.6, 0);
        int i = 0;
        for (UUID id : new ArrayList<>(shards)) {
            if (!(level.getEntity(id) instanceof Display.BlockDisplay display) || !display.isAlive()) {
                shards.remove(id);
                continue;
            }
            Vec3 slot = (i % 2 == 0) ? headSlot : legsSlot;
            Vec3 to = slot.subtract(display.position());
            if (to.length() > 0.35) {
                Vec3 next = display.position().add(to.normalize().scale(Math.min(0.55, to.length())));
                display.setPos(next.x, next.y, next.z);
                level.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, i % 2 == 0 ? headMaterial : legsMaterial),
                        next.x,
                        next.y,
                        next.z,
                        1,
                        0.1,
                        0.1,
                        0.1,
                        0.0);
            }
            i++;
        }
    }

    private void formArmor() {
        List<net.minecraft.world.item.ItemStack> armor = player.getInventory().armor;
        for (int i = 0; i < 4 && i < armor.size(); i++) {
            savedArmor.add(armor.get(i).copy());
        }
        armor.set(3, dyedLeather(net.minecraft.world.item.Items.LEATHER_HELMET, headMaterial));
        armor.set(2, dyedLeather(net.minecraft.world.item.Items.LEATHER_CHESTPLATE, headMaterial));
        armor.set(1, dyedLeather(net.minecraft.world.item.Items.LEATHER_LEGGINGS, legsMaterial));
        armor.set(0, dyedLeather(net.minecraft.world.item.Items.LEATHER_BOOTS, legsMaterial));
        player.removeEffect(MobEffects.ABSORPTION);
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Integer.MAX_VALUE, 1, false, false, true));
        player.setAbsorptionAmount(ABSORPTION);
        player.containerMenu.broadcastChanges();
        for (UUID id : shards) {
            if (level.getEntity(id) instanceof Display.BlockDisplay display) {
                display.discard();
            }
        }
        shards.clear();
    }

    private static net.minecraft.world.item.ItemStack dyedLeather(
            net.minecraft.world.item.Item item, BlockState state) {
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item);
        stack.set(
                net.minecraft.core.component.DataComponents.DYED_COLOR,
                new net.minecraft.world.item.component.DyedItemColor(earthTone(state), true));
        return stack;
    }

    private static int earthTone(BlockState state) {
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        if (key.contains("SAND")) {
            return 0xDCD29B;
        }
        if (key.contains("GRAVEL")) {
            return 0x8A7B6C;
        }
        if (key.contains("GRASS") || key.contains("MOSS") || key.contains("LEAVES")) {
            return 0x6B8E3D;
        }
        if (key.contains("STONE") || key.contains("COBBLE") || key.contains("BRICK")) {
            return 0x7D7D7D;
        }
        return 0x8A5F3C;
    }

    private static void setBlockState(Display.BlockDisplay display, BlockState state) {
        try {
            java.lang.reflect.Method method =
                    Display.BlockDisplay.class.getDeclaredMethod("setBlockState", BlockState.class);
            method.setAccessible(true);
            method.invoke(display, state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRemove() {
        for (UUID id : shards) {
            if (level.getEntity(id) instanceof Display.BlockDisplay display) {
                display.discard();
            }
        }
        shards.clear();
        List<net.minecraft.world.item.ItemStack> armor = player.getInventory().armor;
        for (int i = 0; i < savedArmor.size() && i < armor.size(); i++) {
            armor.set(i, savedArmor.get(i));
        }
        savedArmor.clear();
        player.setAbsorptionAmount(0.0F);
        player.removeEffect(MobEffects.ABSORPTION);
        player.containerMenu.broadcastChanges();
        cool(owner, player, ID, COOLDOWN_TICKS);
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
