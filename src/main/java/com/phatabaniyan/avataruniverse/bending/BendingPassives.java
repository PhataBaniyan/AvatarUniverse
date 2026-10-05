package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.Config;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * ProjectKorra passive abilities, always-on per element: GracefulDescent,
 * AirAgility, AirSaturation (air); HydroSink (water, in the fall handler);
 * FerroControl (earth, metalbenders); FirePassive auto-glow (fire);
 * Acrobatics, ChiAgility, BlockChi (chi). Reference
 * numbers: Agility Speed II/Jump III air and I/I chi on 10-tick refresh,
 * Saturation factor 0.3, FastSwim 0.7 velocity, DensityShift sand reverts in
 * 2500ms, FerroControl 5-block reach with 200ms debounce, BlockChi 25% for
 * 20 ticks, Acrobatics halves falls and negates tiny ones.
 */
public final class BendingPassives {
    private BendingPassives() {}

    private static final Map<UUID, Float> LAST_EXHAUSTION = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> WAS_SNEAKING = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> FERRO_DEBOUNCE = new ConcurrentHashMap<>();
    private static Boolean lastDay;

    /** Per-tick passive upkeep for every online player. */
    public static void tick(MinecraftServer server) {
        skyWatch(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending == null || !bending.isToggled()) {
                continue;
            }
            long gameTime = player.level().getGameTime();
            if (bending.hasElement(BendingElement.AIR)) {
                agility(player, Config.PASSIVE_AIR_AGILITY_SPEED_AMP.get(), Config.PASSIVE_AIR_AGILITY_JUMP_AMP.get());
                saturate(player);
            }
            if (bending.hasElement(BendingElement.METAL)) {
                ferroControl(player, bending, gameTime);
            }
            if (bending.hasElement(BendingElement.FIRE)
                    && gameTime % Config.PASSIVE_FIRE_GLOW_INTERVAL_TICKS.get() == 0) {
                fireGlow(player);
            }
            if (bending.hasElement(BendingElement.FIRE) && player.isOnFire()) {
                player.clearFire();
            }
        }
    }

    /** Dawn and dusk tell fire and water benders how the sky treats them. */
    private static void skyWatch(MinecraftServer server) {
        boolean day = server.overworld().isDay();
        if (lastDay != null && lastDay == day) {
            return;
        }
        boolean first = lastDay == null;
        lastDay = day;
        if (first) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending == null || !bending.isToggled()) {
                continue;
            }
            if (bending.hasElement(BendingElement.FIRE)) {
                BendingElement face = finest(
                        bending,
                        BendingElement.BLUE_FIRE,
                        BendingElement.LIGHTNING,
                        BendingElement.COMBUSTION,
                        BendingElement.FIRE);
                player.displayClientMessage(
                        BendingTheme.gradient(
                                day
                                        ? "Dawn breaks — the sun feeds your flames, and your power swells."
                                        : "Night falls — your flames sink to embers.",
                                face),
                        false);
            }
            if (bending.hasElement(BendingElement.WATER)) {
                BendingElement face = finest(
                        bending,
                        BendingElement.ICE,
                        BendingElement.PLANT,
                        BendingElement.BLOOD,
                        BendingElement.HEALING,
                        BendingElement.WATER);
                player.displayClientMessage(
                        BendingTheme.gradient(
                                day
                                        ? "Dawn breaks — the moon releases the tides; your power settles to its measure."
                                        : "Night falls — the moon swells the tides, and your power rises.",
                                face),
                        false);
            }
        }
    }

    private static BendingElement finest(BendingPlayer bending, BendingElement... kin) {
        for (BendingElement element : kin) {
            if (bending.elements().contains(element)) {
                return element;
            }
        }
        return kin[kin.length - 1];
    }

    private static void agility(ServerPlayer player, int speedAmp, int jumpAmp) {
        if (!player.isSprinting()) {
            return;
        }
        refresh(player, MobEffects.MOVEMENT_SPEED, speedAmp);
        refresh(player, MobEffects.JUMP, jumpAmp);
    }

    private static void refresh(
            ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amp) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && (current.getAmplifier() > amp || current.getDuration() > 1)) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, 10, amp, true, false, false));
    }

    /** Hunger drains at 30% for air and chi benders. */
    private static void saturate(ServerPlayer player) {
        float now = player.getFoodData().getExhaustionLevel();
        Float last = LAST_EXHAUSTION.get(player.getUUID());
        if (last == null || now < last) {
            LAST_EXHAUSTION.put(player.getUUID(), now);
            return;
        }
        if (now > last) {
            float scaled = last + (now - last) * 0.3F;
            player.getFoodData().setExhaustion(scaled);
            LAST_EXHAUSTION.put(player.getUUID(), scaled);
        }
    }

    /** Sneak-press at iron doors and trapdoors swings them for metalbenders. */
    private static void ferroControl(ServerPlayer player, BendingPlayer bending, long gameTime) {
        boolean sneaking = player.isShiftKeyDown();
        boolean was = WAS_SNEAKING.getOrDefault(player.getUUID(), false);
        WAS_SNEAKING.put(player.getUUID(), sneaking);
        if (!sneaking || was) {
            return;
        }
        Long debounce = FERRO_DEBOUNCE.get(player.getUUID());
        if (debounce != null && gameTime - debounce < 4L) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= 5.0; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!level.isLoaded(pos)) {
                return;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock && state.hasProperty(BlockStateProperties.OPEN)) {
                boolean open = !state.getValue(BlockStateProperties.OPEN);
                level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), 3);
                level.playSound(
                        null,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        open ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                        SoundSource.BLOCKS,
                        0.5F,
                        0.0F);
                FERRO_DEBOUNCE.put(player.getUUID(), gameTime);
                return;
            }
            if (state.getBlock() instanceof TrapDoorBlock && state.hasProperty(BlockStateProperties.OPEN)) {
                boolean open = !state.getValue(BlockStateProperties.OPEN);
                level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), 3);
                level.playSound(
                        null,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        open ? SoundEvents.IRON_TRAPDOOR_OPEN : SoundEvents.IRON_TRAPDOOR_CLOSE,
                        SoundSource.BLOCKS,
                        0.5F,
                        0.0F);
                FERRO_DEBOUNCE.put(player.getUUID(), gameTime);
                return;
            }
            if (state.isSolid()) {
                return;
            }
        }
    }

    /** Firebenders wandering in darkness kindle their own glow. */
    private static void fireGlow(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (BendingManager.find(
                        player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination.class)
                != null) {
            return;
        }
        if (!bendingHasFire(player)) {
            return;
        }
        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination(player));
    }

    private static boolean bendingHasFire(ServerPlayer player) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        return bending != null && bending.hasElement(BendingElement.FIRE);
    }

    /**
     * Fall landing for air (negated) and chi (halved, tiny negated).
     * Returns true when the fall was handled.
     */
    public static boolean softenLanding(
            ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingFallEvent event) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.isToggled()) {
            return false;
        }
        if (bending.hasElement(BendingElement.AIR)) {
            return true;
        }
        return false;
    }
}
