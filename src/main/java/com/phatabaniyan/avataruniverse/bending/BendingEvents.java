package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.ability.plant.RazorLeaf;
import com.phatabaniyan.avataruniverse.bending.ability.water.BloodPuppet;
import com.phatabaniyan.avataruniverse.bending.ability.water.Bloodbending;
import com.phatabaniyan.avataruniverse.bending.ability.water.Drain;
import com.phatabaniyan.avataruniverse.bending.ability.water.FrostBreath;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceBlast;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceClaws;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceCrawl;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceSpikeBlast;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceSpikePillar;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceWall;
import com.phatabaniyan.avataruniverse.bending.ability.water.PhaseChange;
import com.phatabaniyan.avataruniverse.bending.ability.water.Torrent;
import com.phatabaniyan.avataruniverse.bending.ability.water.WakeFishing;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterArms;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterArmsSpear;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterArmsWhip;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterBubble;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterManipulation;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterSpout;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterSpoutWave;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/**
 * Port of ProjectKorra {@code PKListener} essentials (79KB -> starter set).
 * Maps: PlayerJoin/Quit -> BendingPlayer load/unload; respawn Clone -> data
 * copy; left-click -> select a tapped water source, left-click again -> cast
 * the held slot's bound ability from it (Korra's click + source-selection
 * trigger, no sneak needed). Air clicks send no vanilla packet, so the client sends a {@link BendingCastPayload}
 * for crosshair-miss clicks while block and entity aims arrive via the
 * server-side interact events; the event is cancelled only when a cast
 * actually fires. Region-protection hooks (WorldGuard/Factions/Towny/...)
 * and chat formatting are explicitly deferred (skill section 9).
 */
@EventBusSubscriber(modid = AvatarUniverseMod.MODID)
public final class BendingEvents {
    private BendingEvents() {}

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BendingPlayer.getOrCreate(player.getUUID()).loadFrom(player);
            com.phatabaniyan.avataruniverse.bending.BendingBoardSync.sync(player);
        }
    }

    /**
     * Healing others: right-click a living entity while holding HealingWaters
     * with both half-or-fully submerged grants regeneration plus a debuff
     * purge. No sneak, no click-targeting dance.
     */
    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        // EarthGrab: anyone working the invisible trap stand chips 1 HP off
        // it (400ms interval), which frees the victim at zero. Cancelled so
        // the poke never strips the stand's ground helmet.
        if (event.getTarget() instanceof net.minecraft.world.entity.decoration.ArmorStand stand
                && stand.getTags().contains(com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.TRAP_TAG)) {
            if (stand.getPersistentData().hasUUID("earthgrab_owner")) {
                java.util.UUID trapOwner = stand.getPersistentData().getUUID("earthgrab_owner");
                com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab grab = BendingManager.find(
                        trapOwner, com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.class);
                if (grab != null) {
                    grab.damageTrap();
                }
            }
            event.setCanceled(true);
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.isToggled() || !bending.hasElement(BendingElement.WATER)) {
            return;
        }
        if (!"HealingWaters".equalsIgnoreCase(bending.boundAbility(player.getInventory().selected + 1))) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity target)
                || !target.isAlive()
                || target.getUUID().equals(player.getUUID())) {
            return;
        }
        if (!inWater(player) || !inWater(target)) {
            feedback(
                    player,
                    bending,
                    BendingElement.WATER,
                    "Healing waters demand two souls adrift — both must stand within the water.");
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, Config.HEALING_POTENCY.get()));
        for (MobEffectInstance active : new java.util.ArrayList<>(target.getActiveEffects())) {
            if (!active.getEffect().value().isBeneficial()) {
                target.removeEffect(active.getEffect());
            }
        }
        player.displayClientMessage(Component.literal("Healing " + target.getScoreboardName() + "."), true);
    }

    private static boolean inWater(LivingEntity entity) {
        return entity.level().getFluidState(entity.blockPosition()).is(net.minecraft.world.level.material.Fluids.WATER);
    }

    /**
     * SpiritProjection: any hit on a body double snaps its owner back,
     * like the source damage hook.
     */
    @SubscribeEvent
    static void onBodyHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.decoration.ArmorStand stand
                && !stand.level().isClientSide) {
            com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritProjection.returnOwnerIfBody(
                    stand.getUUID());
        }
    }

    /**
     * Fire immunity (ProjectAvatar passive): fire cannot burn its own
     * master — firebenders ignore flame and lava damage outright.
     */
    @SubscribeEvent
    static void onFireImmune(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer victim) || victim.level().isClientSide) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(victim.getUUID());
        if (bending == null || !bending.isToggled() || !bending.hasElement(BendingElement.FIRE)) {
            return;
        }
        net.minecraft.world.damagesource.DamageSource incoming = event.getSource();
        if (incoming.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE)
                || incoming.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)
                || incoming.is(net.minecraft.world.damagesource.DamageTypes.LAVA)
                || incoming.is(net.minecraft.world.damagesource.DamageTypes.HOT_FLOOR)) {
            event.getContainer().setNewDamage(0.0F);
        }
    }

    /**
     * Day/night bending factor: flames (+ lightning, combustion, blue fire)
     * strike 1.25x under the sun and 1.0x at night; tides (+ ice, plant,
     * blood, healing) strike 1.0x by day and 1.25x under the moon.
     */
    @SubscribeEvent
    static void onBenderHurt(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker) || attacker.level().isClientSide) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(attacker.getUUID());
        if (bending == null || !bending.isToggled()) {
            return;
        }
        boolean day = attacker.level().isDay();
        float factor = 1.0F;
        if (bending.hasElement(BendingElement.FIRE) && day) {
            factor = 1.25F;
        } else if (bending.hasElement(BendingElement.WATER) && !day) {
            factor = 1.25F;
        }
        if (factor != 1.0F) {
            event.getContainer().setNewDamage(event.getContainer().getNewDamage() * factor);
        }
    }

    /**
     * HydroSink (Korra passive): landing in water, ice or snow cancels fall
     * damage for waterbenders. Checked at the landing spot and one below.
     */
    @SubscribeEvent
    static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.isToggled()) {
            return;
        }
        if (bending.hasElement(BendingElement.AIR)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.tryFallBurst(player, event.getDistance());
        }
        if (com.phatabaniyan.avataruniverse.bending.BendingPassives.softenLanding(player, event)) {
            event.setCanceled(true);
            return;
        }
        if (!bending.hasElement(BendingElement.WATER)) {
            return;
        }
        ServerLevel level = player.serverLevel();
        com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor shell = BendingManager.find(
                player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.class);
        // Live PlantArmor absorbs ALL fall damage and is charged for it
        // (ProjectAddons MainListener parity: damage(dmg*10), always
        // cancelled), regardless of whether HydroSink would also zero it.
        if (shell != null) {
            event.setCanceled(true);
            float dmg = Math.max(event.getDistance() - 3.0F, 0.0F) * 10.0F;
            if (dmg > 0.0F) {
                shell.damage(dmg);
            }
            return;
        }
        BlockPos feet = player.blockPosition();
        if (isSinkSafe(level, feet) || isSinkSafe(level, feet.below())) {
            event.setDamageMultiplier(0.0F);
        }
    }

    /**
     * Dig (ProjectAddons MainListener parity): Elytra wall damage is cancelled
     * while the dig is active.
     */
    @SubscribeEvent
    static void onDigWallDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FLY_INTO_WALL)) {
            return;
        }
        if (BendingManager.find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Dig.class)
                != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.DROWN)) {
            return;
        }
        com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor shell = BendingManager.find(
                player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.class);
        if (shell == null) {
            return;
        }
        if (shell.damage(event.getAmount() * 5.0F)) {
            event.setCanceled(true);
        } else {
            event.setCanceled(false);
            BendingManager.remove(shell);
        }
    }

    private static boolean isSinkSafe(ServerLevel level, BlockPos pos) {
        if (level.getFluidState(pos).is(net.minecraft.world.level.material.Fluids.WATER)) {
            return true;
        }
        return BendingSources.isIce(level, pos);
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending != null) {
                bending.saveTo(player);
            }
            BendingManager.cancelFor(player.getUUID());
            BendingManager.forgetPlayer(player.getUUID());
            BendingPlayer.remove(player.getUUID());
        }
    }

    /** Server stop: snapshot every online bender so nothing is lost. */
    @SubscribeEvent
    static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()
                && event.getEntity() instanceof ServerPlayer player
                && event.getOriginal() instanceof ServerPlayer original) {
            BendingPlayer.copyTo(original.getUUID(), player.getUUID());
        }
    }

    @SubscribeEvent
    static void onServerStopping(ServerStoppingEvent event) {
        // Korra reverts all TempBlocks onDisable: never leak bent blocks.
        TempBlock.revertAll();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending != null) {
                try {
                    bending.saveTo(player);
                } catch (RuntimeException ignored) {
                    // A snapshot hiccup must never block shutdown.
                }
            }
        }
    }

    /**
     * Stop vanilla fluid physics from spreading bent water. A placed water
     * source schedules fluid ticks and would flow into neighbours, leaking
     * unrevertable water; cancelling spread that originates from a temp
     * position keeps the bolt to its single travelling block (Korra's
     * TempBlock behaves the same way). The reverse direction matters too:
     * active neighbours must never flow INTO a temp cell (WaterBubble air
     * pockets would come back patchy), so either end being temp cancels.
     */
    @SubscribeEvent
    static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level
                && (TempBlock.isTemp(level, event.getPos()) || TempBlock.isTemp(level, event.getLiquidPos()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        BendingPlayer pre = BendingPlayer.get(player.getUUID());
        if (pre != null
                && IceWall.ID.equalsIgnoreCase(pre.boundAbility(player.getInventory().selected + 1))
                && IceWall.collapseAt(player, event.getPos())) {
            return;
        }
        if (tryCast(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        if (event.getTarget() instanceof LivingEntity victim) {
            BendingPlayer pre = BendingPlayer.get(player.getUUID());
            if (pre != null
                    && IceClaws.ID.equalsIgnoreCase(pre.boundAbility(player.getInventory().selected + 1))
                    && BendingManager.find(player.getUUID(), IceClaws.class) != null) {
                BendingManager.find(player.getUUID(), IceClaws.class).punch(victim);
                return;
            }
        }
        if (tryCast(player)) {
            event.setCanceled(true);
        }
    }

    /**
     * Attempt to cast the held hotbar slot's bound ability (Korra slot model).
     * Package-visible so the {@link BendingCastPayload} server handler can
     * reuse the exact same validation as the interact-event paths.
     *
     * @return always false: abilities fire alongside the vanilla
     * interaction, which is never cancelled (attacks and block breaks always
     * go through no matter what is bound).
     */
    static boolean tryCast(ServerPlayer player) {
        if (!Config.ENABLE_BENDING.get()) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null) {
            return false;
        }
        if (!bending.isToggled()) {
            feedback(player, bending, null, "Your spirit is shuttered — whisper /au toggle to wake it.");
            return false;
        }
        int slot = player.getInventory().selected + 1;
        // Live sphere eats every click first: hotbar slots 1-5 hurl that
        // slot's element, like the source router (binds are ignored here).
        com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere sphere = BendingManager.find(
                player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere.class);
        if (sphere != null) {
            sphere.fireSub(slot, player);
            return false;
        }
        // Projected spirits answer clicks themselves: drift home and click
        // the double to return, like the source router.
        com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritProjection projection = BendingManager.find(
                player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritProjection.class);
        if (projection != null) {
            projection.tryReturnByClick();
            return false;
        }
        String bound = bending.boundAbility(slot);
        if (bound == null) {
            feedback(player, bending, null, "That slot holds no art — bind one with /au bind <ability> [slot].");
            return false;
        }
        // Flight mode sub-binds: picking a mode; re-picking Soar spins up speed.
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.isSubBind(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight fly = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.class);
            if (fly != null) {
                int idx = com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.subIndex(bound);
                if (idx == 1 && fly.modeName().equalsIgnoreCase("Soar")) {
                    fly.cycleSpeed();
                } else {
                    fly.selectMode(idx);
                }
            }
            return false;
        }
        if (!WaterManipulation.ID.equalsIgnoreCase(bound)
                && !Torrent.ID.equalsIgnoreCase(bound)
                && !WaterSpout.ID.equalsIgnoreCase(bound)
                && !WaterArms.ID.equalsIgnoreCase(bound)
                && !"HealingWaters".equalsIgnoreCase(bound)
                && !WaterBubble.ID.equalsIgnoreCase(bound)
                && !FrostBreath.ID.equalsIgnoreCase(bound)
                && !IceBlast.ID.equalsIgnoreCase(bound)
                && !IceSpikeBlast.BIND.equalsIgnoreCase(bound)
                && !PhaseChange.ID.equalsIgnoreCase(bound)
                && !Bloodbending.ID.equalsIgnoreCase(bound)
                && !BloodPuppet.ID.equalsIgnoreCase(bound)
                && !Drain.ID.equalsIgnoreCase(bound)
                && !IceClaws.ID.equalsIgnoreCase(bound)
                && !IceWall.ID.equalsIgnoreCase(bound)
                && !WakeFishing.ID.equalsIgnoreCase(bound)
                && !RazorLeaf.ID.equalsIgnoreCase(bound)
                && !IceCrawl.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.isClickSub(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.isHoldSub(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.RaiseEarth.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthKick.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.Extraction.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.RockSlide.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.Shrapnel.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthPillar.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlux.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGlove.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthDome.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.Dig.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthTunnel.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireJet.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireKick.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireSpin.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireWheel.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireDisc.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireBall.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireSki.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.WallOfFire.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireShield.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FlameBreath.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireBreath.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireWave.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireComet.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.HeatControl.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Bolt.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Discharge.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.LightningBurst.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Lightning.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Electrify.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.CombustBeam.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Explode.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Jets.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.CombustionBlast.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirJet.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirScooter.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirSpout.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirPunch.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirSlam.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirShield.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirSuction.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.Suffocate.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.Tornado.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirBreath.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.Meditate.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.SonicBlast.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.Zephyr.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.avatar.AvatarState.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.avatar.SpiritBeam.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritGrasp.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritProjection.ID.equalsIgnoreCase(
                        bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritStep.ID.equalsIgnoreCase(bound)
                && !com.phatabaniyan.avataruniverse.bending.ability.plant.LeafStorm.ID.equalsIgnoreCase(bound)
                && !isArmsSubBind(bound)) {
            feedback(
                    player,
                    bending,
                    null,
                    "That art is unknown — bind a true one: WaterManipulation, Torrent, WaterSpout, WaterArms, ...: "
                            + bound);
            return false;
        }
        boolean isEarthFallback = (com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.ID.equalsIgnoreCase(
                                bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.RaiseEarth.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthKick.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.Extraction.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.RockSlide.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.Shrapnel.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthPillar.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlux.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.ID.equalsIgnoreCase(
                                bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGlove.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthDome.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.Dig.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthTunnel.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.ID.equalsIgnoreCase(bound))
                && bending.hasElement(BendingElement.EARTH);
        boolean isFireFallback = (com.phatabaniyan.avataruniverse.bending.ability.fire.FireJet.ID.equalsIgnoreCase(
                                bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireKick.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireSpin.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireWheel.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireDisc.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireBall.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireSki.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.WallOfFire.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireShield.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FlameBreath.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireBreath.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireWave.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireComet.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation.ID.equalsIgnoreCase(
                                bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.HeatControl.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Bolt.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Discharge.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.LightningBurst.ID.equalsIgnoreCase(
                                bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Lightning.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Electrify.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.CombustBeam.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Jets.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Explode.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.CombustionBlast.ID.equalsIgnoreCase(
                                bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion.ID.equalsIgnoreCase(bound))
                && bending.hasElement(BendingElement.FIRE);
        boolean isAirFallback = (com.phatabaniyan.avataruniverse.bending.ability.air.AirJet.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirScooter.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirSpout.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirPunch.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirSlam.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirShield.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirSuction.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.Suffocate.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.Tornado.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirBreath.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.Meditate.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.SonicBlast.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.Zephyr.ID.equalsIgnoreCase(bound)
                        || com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.ID.equalsIgnoreCase(bound))
                && bending.hasElement(BendingElement.AIR);
        boolean isAvatarFallback =
                (com.phatabaniyan.avataruniverse.bending.ability.avatar.AvatarState.ID.equalsIgnoreCase(bound)
                                || com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere.ID
                                        .equalsIgnoreCase(bound)
                                || com.phatabaniyan.avataruniverse.bending.ability.avatar.SpiritBeam.ID
                                        .equalsIgnoreCase(bound))
                        && bending.hasElement(BendingElement.AVATAR);
        boolean isSpiritualFallback =
                (com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritGrasp.ID.equalsIgnoreCase(bound)
                                || com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritProjection.ID
                                        .equalsIgnoreCase(bound)
                                || com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritStep.ID
                                        .equalsIgnoreCase(bound))
                        && bending.hasElement(BendingElement.SPIRITUAL);
        if (!bending.hasElement(BendingElement.WATER)
                && !isEarthFallback
                && !isFireFallback
                && !isAirFallback
                && !isAvatarFallback
                && !isSpiritualFallback) {
            feedback(
                    player,
                    bending,
                    BendingTheme.elementOfAbility(bound),
                    "The elements do not answer — no fitting art stirs within you.");
            return false;
        }
        long gameTime = player.level().getGameTime();
        if ("HealingWaters".equalsIgnoreCase(bound)) {
            // Passive ability: hold it and enter water, no click or sneak.
            feedback(
                    player,
                    bending,
                    BendingElement.WATER,
                    "Drift into the water's embrace, and it shall knit your wounds.");
            return false;
        }
        if (WaterBubble.ID.equalsIgnoreCase(bound)) {
            // Korra re-click model: near the live bubble refresh it (follow,
            // adopt sneak state, extend timer); far away melts the old one
            // while a new one grows. Clicks never pop the bubble and never
            // cancel the vanilla interaction.
            boolean shift = player.isShiftKeyDown();
            WaterBubble live = BendingManager.find(player.getUUID(), WaterBubble.class);
            if (live != null) {
                double maxRadius = Config.WATERBUBBLE_RADIUS.get();
                if (live.covers(player.blockPosition())
                        || live.covers(player.blockPosition().above())
                        || player.position().distanceToSqr(live.center()) < maxRadius * maxRadius) {
                    live.refresh(player, shift);
                } else {
                    live.startRemoving();
                    if (WaterBubble.canStart(player)) {
                        BendingManager.start(new WaterBubble(player, shift));
                    }
                }
                return false;
            }
            if (WaterBubble.canStart(player)) {
                BendingManager.start(new WaterBubble(player, shift));
            }
            return false;
        }
        if (FrostBreath.ID.equalsIgnoreCase(bound)) {
            // Sneak ability (JedCore): clicks pass through to vanilla
            // attacks/block breaks, never swallowed by the ability.
            return false;
        }
        if (FrostBreath.ID.equalsIgnoreCase(bound)) {
            // Sneak ability (JedCore): clicks pass through to vanilla
            // attacks/block breaks, never swallowed by the ability.
            return false;
        }
        if (IceBlast.ID.equalsIgnoreCase(bound)) {
            IceBlast blast = BendingManager.find(player.getUUID(), IceBlast.class);
            if (blast != null) {
                if (blast.isPrepared()) {
                    blast.throwIce(player);
                }
            } else {
                feedback(
                        player,
                        bending,
                        BendingElement.WATER,
                        "Kneel before ice or water first, and the frost shall gather.");
            }
            return false;
        }
        if (IceSpikeBlast.BIND.equalsIgnoreCase(bound)) {
            IceSpikeBlast blast = BendingManager.find(player.getUUID(), IceSpikeBlast.class);
            if (blast != null && blast.isProgressing()) {
                blast.redirect(player);
                return false;
            }
            if (blast != null && blast.isPrepared()) {
                blast.throwIce(player);
                bending.setCooldown(IceSpikeBlast.ID, gameTime + Config.ICESPIKE_BLAST_COOLDOWN_TICKS.get());
                return false;
            }
            LivingEntity caught = null;
            double best = Double.MAX_VALUE;
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle().normalize();
            ServerLevel aimLevel = player.serverLevel();
            for (LivingEntity entity : aimLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(eye.subtract(20.0, 20.0, 20.0), eye.add(20.0, 20.0, 20.0)),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(player.getUUID())) {
                    continue;
                }
                Vec3 to = entity.position().subtract(eye);
                double along = to.dot(look);
                if (along < 0.0 || along > 20.0) {
                    continue;
                }
                if (to.subtract(look.scale(along)).length() <= 2.0 && along < best) {
                    best = along;
                    caught = entity;
                }
            }
            BlockPos base = null;
            if (caught != null) {
                base = caught.blockPosition().below();
            } else {
                for (double d = 0.5; d <= 20.0; d += 0.5) {
                    BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
                    if (BendingSources.isIce(aimLevel, pos)) {
                        base = pos.immutable();
                        break;
                    }
                }
            }
            if (base != null && BendingSources.isIce(aimLevel, base)) {
                if (bending.isOnCooldown(IceSpikePillar.ID, gameTime)) {
                    long left = (bending.cooldownExpiresAt(IceSpikePillar.ID) - gameTime + 19) / 20;
                    feedback(
                            player,
                            bending,
                            BendingElement.WATER,
                            "The ice has not yet reformed (" + Math.max(left, 1) + "s).");
                    return false;
                }
                IceSpikePillar pillar =
                        new IceSpikePillar(player, base, Config.ICESPIKE_DAMAGE.get(), Config.ICESPIKE_PUSH.get());
                if (!pillar.canInstantiate(player)) {
                    feedback(player, bending, BendingElement.WATER, "The ground is crowded — no spire can rise there.");
                    return false;
                }
                bending.setCooldown(IceSpikePillar.ID, gameTime + Config.ICESPIKE_COOLDOWN_TICKS.get());
                BendingManager.start(pillar);
                return false;
            }
            if (BendingBottles.consumeWaterBottle(player)) {
                IceSpikeBlast bottled = new IceSpikeBlast(
                        player, BlockPos.containing(eye.add(look)).immutable());
                bottled.throwIce(player);
                return false;
            }
            feedback(
                    player,
                    bending,
                    BendingElement.WATER,
                    "Turn your gaze to ice within twenty paces, or carry the sea in a bottle.");
            return false;
        }
        if (PhaseChange.ID.equalsIgnoreCase(bound)) {
            PhaseChange burst = new PhaseChange(player, PhaseChange.Mode.FREEZE);
            if (burst.freezeBurst(player)) {
                BendingManager.start(burst);
            }
            return false;
        }
        if (Bloodbending.ID.equalsIgnoreCase(bound)) {
            Bloodbending grip = BendingManager.find(player.getUUID(), Bloodbending.class);
            if (grip != null) {
                grip.launch(player);
                BendingManager.remove(grip);
            } else {
                feedback(
                        player,
                        bending,
                        BendingElement.WATER,
                        "Kneel with your foe in sight, and the blood shall obey.");
            }
            return false;
        }
        if (BloodPuppet.ID.equalsIgnoreCase(bound)) {
            BloodPuppet puppet = BendingManager.find(player.getUUID(), BloodPuppet.class);
            if (puppet != null) {
                puppet.attack();
            }
            return false;
        }
        if (Drain.ID.equalsIgnoreCase(bound)) {
            Drain drain = BendingManager.find(player.getUUID(), Drain.class);
            if (drain != null) {
                drain.fireBlast(player);
            }
            return false;
        }
        if (IceClaws.ID.equalsIgnoreCase(bound)) {
            IceClaws claws = BendingManager.find(player.getUUID(), IceClaws.class);
            if (claws != null && claws.isCharged() && player.isShiftKeyDown()) {
                claws.throwClaws(player);
            } else if (claws == null) {
                feedback(player, bending, BendingElement.WATER, "Crouch, and let winter arm your hands.");
            }
            return false;
        }
        if (IceWall.ID.equalsIgnoreCase(bound)) {
            if (bending.isOnCooldown(IceWall.ID, gameTime)) {
                long left = (bending.cooldownExpiresAt(IceWall.ID) - gameTime + 19) / 20;
                feedback(
                        player,
                        bending,
                        BendingElement.WATER,
                        "The wall has not yet refrozen (" + Math.max(left, 1) + "s).");
                return false;
            }
            BlockPos source = IceWall.findSource(player, Config.ICEWALL_RANGE.get());
            if (source == null) {
                feedback(player, bending, BendingElement.WATER, "Seek water, ice, or snow within eight paces.");
                return false;
            }
            java.util.List<BlockPos> plan = IceWall.planWall(player, source);
            if (plan.isEmpty()) {
                feedback(player, bending, BendingElement.WATER, "No wall can stand upon that ground.");
                return false;
            }
            bending.setCooldown(IceWall.ID, gameTime + Config.ICEWALL_COOLDOWN_TICKS.get());
            BendingManager.start(new IceWall(player, plan));
            return false;
        }
        if (WakeFishing.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (RazorLeaf.ID.equalsIgnoreCase(bound)) {
            // Standalone cast is sneak-driven; while a live PlantArmor owns this
            // slot, the shell's progress polls for the hold-sub instead.
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.ID.equalsIgnoreCase(bound)) {
            feedback(
                    player,
                    bending,
                    BendingElement.WATER,
                    "Linger among the leaves, and living armor shall weave itself around you.");
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf.class);
            if (live != null) {
                BendingManager.remove(live);
                feedback(player, bending, BendingElement.EARTH, "The earthen wave crumbles beneath you.");
                return false;
            }
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf.ID, gameTime)
                    && com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf.canBegin(player)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthSurf(player));
            }
            return false;
        }
        com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor plantArmor = BendingManager.find(
                player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.class);
        if (plantArmor != null && com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.isClickSub(bound)) {
            plantArmor.activateClickSub(player, bending, bound, gameTime);
            return false;
        }
        if (plantArmor != null && com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.isHoldSub(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.class);
            if (live != null) {
                live.click();
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.class);
            if (live != null) {
                if (live.isProgressing()) {
                    live.redirect();
                } else if (!bending.isOnCooldown(
                        com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.ID, gameTime)) {
                    live.throwEarth();
                }
            } else {
                feedback(player, bending, BendingElement.EARTH, "Kneel upon the stone to claim your weapon first.");
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.class);
            if (live != null) {
                live.launch();
            } else if (com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.canBegin(player)
                    && !bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult(player, false));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion acc = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.class);
            if (acc != null) {
                acc.shoot();
            } else {
                feedback(
                        player,
                        bending,
                        BendingElement.EARTH,
                        "First bid the earth rise — sneak, and it shall answer.");
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.RaiseEarth.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave.class);
            if (live != null) {
                if (live.isCharged()) {
                    live.clickFire();
                } else {
                    feedback(
                            player,
                            bending,
                            BendingElement.EARTH,
                            "Hold your stance and your breath — charge before you strike.");
                }
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthDome.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.earth.EarthDome.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthDome(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthKick.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.EarthKick live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthKick.class);
            if (live == null) {
                if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthKick.findTarget(player) != null) {
                    BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthKick(player));
                } else {
                    feedback(player, bending, BendingElement.EARTH, "Your gaze finds no foe.");
                }
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.Extraction.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips.class);
            if (live == null) {
                BendingManager.start(
                        new com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips(player, false));
            } else {
                live.click();
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.class);
            if (live == null
                    && !bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow(player, false));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.class);
            if (live != null) {
                live.shoot();
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthPillar.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.Shrapnel.ID.equalsIgnoreCase(bound)) {
            boolean blast = player.isShiftKeyDown();
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.earth.Shrapnel.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.Shrapnel(player, blast));
            } else {
                feedback(player, bending, BendingElement.EARTH, "The shrapnel has not yet reformed.");
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.class);
            if (live != null) {
                String status = live.throwShards();
                if (status != null) {
                    feedback(player, bending, BendingElement.EARTH, status);
                }
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlux.ID.equalsIgnoreCase(bound)) {
            if (BendingManager.find(
                                    player.getUUID(),
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlux.class)
                            == null
                    && !bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlux.ID, gameTime)
                    && com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlux.canBegin(player)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlux(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.ID, gameTime)) {
                BendingManager.start(
                        new com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield(player, true));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.class);
            if (live != null) {
                live.shoot();
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook.class);
            if (live == null
                    && !bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook.ID, gameTime)
                    && com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook.hasRequiredInv(player)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.MetalHook(player));
            } else if (live != null) {
                live.launchHook();
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.class);
            if (live != null) {
                live.shoot();
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.ID.equalsIgnoreCase(bound)) {
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGlove.ID.equalsIgnoreCase(bound)) {
            if (BendingManager.find(
                            player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGlove.class)
                    == null) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGlove(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.ID, gameTime)
                    && com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.canBegin(player)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab(
                        player, com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.GrabMode.PROJECTING));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.Jets.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.Jets live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.Jets.class);
            if (live != null) {
                live.clickFunction();
            } else if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.Jets.ID, gameTime)
                    && com.phatabaniyan.avataruniverse.bending.ability.fire.Jets.canBegin(player)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.Jets(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireJet.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.FireJet live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireJet.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireJet.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireJet(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireKick.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.FireKick.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireKick(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireSpin.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.FireSpin.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireSpin(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireWheel.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.FireWheel.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireWheel(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireDisc.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.FireDisc.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireDisc(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireBall.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.FireBall.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireBall(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireSki.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.FireSki live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireSki.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireSki.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireSki(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.WallOfFire.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.WallOfFire.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.WallOfFire(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.Illumination(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst.tryConeBurst(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots.tryFire(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation.class);
            if (live != null) {
                live.fireStream(player);
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.Discharge.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.Discharge.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.Discharge(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.Electrify.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.fire.Electrify.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.Electrify(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark.shoot(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt.fireOne(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion.detonate(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirJet.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirJet live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirJet.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.air.AirJet.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirJet(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirScooter.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirScooter live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirScooter.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.air.AirScooter.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirScooter(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirSpout.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirSpout live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirSpout.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.air.AirSpout.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirSpout(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirPunch.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.air.AirPunch.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirPunch(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirSlam.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(com.phatabaniyan.avataruniverse.bending.ability.air.AirSlam.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirSlam(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.class);
            if (live != null) {
                live.cycleSpeed();
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.tryConeBurst(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast.selectOrigin(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.fire(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet.tryFire(player);
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritGrasp.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritGrasp.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritGrasp(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritStep.ID.equalsIgnoreCase(bound)) {
            if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritStep.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.spiritual.SpiritStep(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.avatar.AvatarState.ID.equalsIgnoreCase(bound)) {
            com.phatabaniyan.avataruniverse.bending.ability.avatar.AvatarState live = BendingManager.find(
                    player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.avatar.AvatarState.class);
            if (live != null) {
                BendingManager.remove(live);
            } else if (!bending.isOnCooldown(
                    com.phatabaniyan.avataruniverse.bending.ability.avatar.AvatarState.ID, gameTime)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.avatar.AvatarState(player));
            }
            return false;
        }
        if (com.phatabaniyan.avataruniverse.bending.ability.earth.RockSlide.ID.equalsIgnoreCase(bound)) {
            if (BendingManager.find(
                                    player.getUUID(),
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.RockSlide.class)
                            == null
                    && !bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.earth.RockSlide.ID, gameTime)
                    && com.phatabaniyan.avataruniverse.bending.ability.earth.RockSlide.canBegin(player)) {
                BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.RockSlide(player));
            }
            return false;
        }
        if (IceCrawl.ID.equalsIgnoreCase(bound)) {
            IceCrawl crawl = BendingManager.find(player.getUUID(), IceCrawl.class);
            if (crawl != null) {
                if (!crawl.isLaunched()) {
                    crawl.shootLine(player);
                }
                return false;
            }
            BlockPos source = null;
            Vec3 gaze = player.getEyePosition();
            Vec3 gazeDir = player.getLookAngle().normalize();
            ServerLevel crawlLevel = player.serverLevel();
            double select = Config.ICECRAWL_SELECT_RANGE.get();
            for (double d = 0.5; d <= select; d += 0.5) {
                BlockPos pos = BlockPos.containing(gaze.add(gazeDir.scale(d)));
                if (BendingSources.isIce(crawlLevel, pos)
                        && crawlLevel.getBlockState(pos.above()).isAir()) {
                    source = pos.immutable();
                    break;
                }
            }
            if (source == null) {
                for (double d = 0.5; d <= select; d += 0.5) {
                    BlockPos pos = BlockPos.containing(gaze.add(gazeDir.scale(d)));
                    if (!crawlLevel.getFluidState(pos).isEmpty()
                            && crawlLevel.getBlockState(pos.above()).isAir()) {
                        source = pos.immutable();
                        break;
                    }
                }
            }
            if (source == null) {
                feedback(player, bending, BendingElement.WATER, "Turn to ice or water within eight paces.");
                return false;
            }
            BendingManager.start(new IceCrawl(player, source));
            return false;
        }
        if (isArmsSubBind(bound)) {
            WaterArms arms = BendingManager.find(player.getUUID(), WaterArms.class);
            if (arms == null) {
                feedback(
                        player,
                        bending,
                        BendingElement.WATER,
                        "First drape your arms in water — bind WaterArms, and the lesser arts shall follow.");
                return false;
            }
            if (player.isShiftKeyDown()) {
                // Sneak-click on any arm slot doubles as the removal
                // double-click: slots show sub-binds while arms are live, so
                // the WaterArms branch below is otherwise unreachable.
                if (arms.prepareCancel(player, bending, gameTime)) {
                    BendingManager.remove(arms);
                    feedback(player, bending, BendingElement.WATER, "Your watery arms fall back into the deep.");
                }
                return false;
            }
            WaterArmsWhip.WhipMode mode = armsSubMode(bound);
            if (mode == null) {
                return castArmsSpecial(player, bending, gameTime, bound, arms);
            }
            WaterArmsWhip whip = WaterArmsWhip.create(player, mode, arms, bending);
            if (whip == null) {
                feedback(player, bending, BendingElement.WATER, "That arm is already dancing.");
                return false;
            }
            BendingManager.start(whip);
            return false;
        }
        if (WaterArms.ID.equalsIgnoreCase(bound)) {
            return castWaterArms(player, bending, gameTime);
        }
        if (WaterSpout.ID.equalsIgnoreCase(bound)) {
            return castWaterSpout(player, bending, gameTime);
        }
        if (Torrent.ID.equalsIgnoreCase(bound)) {
            // Second click on a live torrent needs no source: it was already
            // validated and consumed at sneak-start. Gating it on selection
            // would fizzle every launch and freeze.
            Torrent active = BendingManager.find(player.getUUID(), Torrent.class);
            if (active != null) {
                return advanceTorrent(player, bending, active);
            }
        }
        BlockPos source = bending.selectedSource(gameTime);
        ServerLevel serverLevel = player.serverLevel();
        boolean bottled = false;
        if (source == null) {
            // Bottled source (Korra bottlebending): a water potion stands in
            // for a tapped source, manifesting at the eyes.
            if (BendingBottles.consumeWaterBottle(player)) {
                source = BlockPos.containing(player.getEyePosition());
                bending.giveBottledSource();
                bottled = true;
            } else {
                feedback(player, bending, BendingElement.WATER, "Strike the water with a click to claim your source.");
                return false;
            }
        }
        if (!bottled && !BendingSources.isWaterSource(serverLevel, source)) {
            bending.clearSource();
            feedback(player, bending, BendingElement.WATER, "Your source has slipped away — claim another.");
            return false;
        }
        double focusRange = Config.WATERMANIP_SOURCE_FOCUS_RANGE.get();
        if (player.blockPosition().distSqr(source) > focusRange * focusRange) {
            bending.clearSource();
            feedback(player, bending, BendingElement.WATER, "You have strayed too far from your source — draw nearer.");
            return false;
        }
        if (Torrent.ID.equalsIgnoreCase(bound)) {
            return startTorrent(player, bending, gameTime, source, serverLevel);
        }
        if (bending.isOnCooldown(WaterManipulation.ID, gameTime)) {
            long left = (bending.cooldownExpiresAt(WaterManipulation.ID) - gameTime + 19) / 20;
            feedback(player, bending, BendingElement.WATER, "The waters are spent (" + Math.max(left, 1) + "s).");
            return false;
        }
        bending.setCooldown(WaterManipulation.ID, gameTime + Config.WATERMANIP_COOLDOWN_TICKS.get());
        boolean icy = BendingSources.isIce(serverLevel, source);
        BendingManager.start(new WaterManipulation(player, Vec3.atCenterOf(source), icy));
        if (BendingSources.isPlant(serverLevel, source)) {
            BendingManager.consumePlantSource(serverLevel, source, Config.WATERMANIP_PLANT_REGROW_SECONDS.get());
        }
        // Selection persists across shots (Korra): the source is not consumed,
        // so repeat casts work until it goes invalid or out of range.
        if (Config.ENABLE_DEBUG_LOGGING.get()) {
            AvatarUniverseMod.LOGGER.info("Cast WaterManipulation for {}", player.getScoreboardName());
        }
        return false;
    }

    /**
     * Spear dispatch: element already checked, arms live. Fires from the
     * preferred arm and consumes it. Null means the arm was busy.
     */
    private static boolean castArmsSpecial(
            ServerPlayer player, BendingPlayer bending, long gameTime, String bound, WaterArms arms) {
        WaterArmsSpear spear = WaterArmsSpear.create(player, true, arms, bending);
        if (spear == null) {
            feedback(player, bending, BendingElement.WATER, "That arm is already dancing.");
            return false;
        }
        BendingManager.start(spear);
        return false;
    }
    /** Map a sub-bind ID to its whip mode (null for Freeze/Spear). */
    private static WaterArmsWhip.WhipMode armsSubMode(String bound) {
        if ("Pull".equalsIgnoreCase(bound)) {
            return WaterArmsWhip.WhipMode.PULL;
        } else if ("Punch".equalsIgnoreCase(bound)) {
            return WaterArmsWhip.WhipMode.PUNCH;
        } else if ("Grapple".equalsIgnoreCase(bound)) {
            return WaterArmsWhip.WhipMode.GRAPPLE;
        } else if ("Grab".equalsIgnoreCase(bound)) {
            return WaterArmsWhip.WhipMode.GRAB;
        }
        return null;
    }

    private static boolean isArmsSubBind(String bound) {
        for (String sub : WaterArms.SUB_BINDS) {
            if (sub.equalsIgnoreCase(bound)) {
                return true;
            }
        }
        return false;
    }

    /**
     * WaterArms dispatch (Korra constructor-as-click, adapted): live arms +
     * sneak double-clicks out, live arms + slot click fires the sub-ability
     * (next phase), otherwise a source (tap, eye-ray, bottle, plant-aware)
     * grows new arms.
     */
    private static boolean castWaterArms(ServerPlayer player, BendingPlayer bending, long gameTime) {
        WaterArms live = BendingManager.find(player.getUUID(), WaterArms.class);
        if (live != null) {
            if (player.isShiftKeyDown()) {
                if (live.prepareCancel(player, bending, gameTime)) {
                    BendingManager.remove(live);
                    feedback(player, bending, BendingElement.WATER, "Your watery arms fall back into the deep.");
                }
                return false;
            }
            feedback(player, bending, BendingElement.WATER, "The lesser arm-arts have not yet awakened.");
            return false;
        }
        if (bending.isOnCooldown(WaterArms.ID, gameTime)) {
            long left = (bending.cooldownExpiresAt(WaterArms.ID) - gameTime + 19) / 20;
            feedback(
                    player,
                    bending,
                    BendingElement.WATER,
                    "Your arms have not yet regathered (" + Math.max(left, 1) + "s).");
            return false;
        }
        return startWaterArms(player, bending, gameTime);
    }

    /**
     * Start a FrostBreath from the sneak poll (JedCore sneak ability: pressing
     * sneak with it bound breathes, no click). Silent on cooldown so repeated
     * sneak presses never spam chat.
     */
    static void startFrostBreath(ServerPlayer player, BendingPlayer bending) {
        long gameTime = player.level().getGameTime();
        if (bending.isOnCooldown(FrostBreath.ID, gameTime)) {
            return;
        }
        if (Config.FROSTBREATH_RESTRICT_BIOMES.get()
                && FrostBreath.isBiomeBlocked(player.serverLevel(), player.blockPosition())) {
            feedback(player, bending, BendingElement.WATER, "This air burns too hot and dry to freeze.");
            return;
        }
        BendingManager.start(new FrostBreath(player));
    }

    /**
     * Grow new arms from tap selection, eye-ray grab, or bottle (shared by
     * click-cast and sneak-look activation).
     */
    static boolean startWaterArms(ServerPlayer player, BendingPlayer bending, long gameTime) {
        boolean fullSource = true;
        BlockPos source = bending.selectedSource(gameTime);
        if (source != null && !BendingSources.isWaterSource(player.serverLevel(), source)) {
            bending.clearSource();
            source = null;
        }
        if (source == null) {
            source = WaterSpoutWave.raycastWaterSource(player, Config.WATERARMS_SOURCE_GRAB_RANGE.get());
        }
        if (source != null) {
            var level = player.serverLevel();
            boolean plant = BendingSources.isPlant(level, source) || BendingSources.isSnow(level, source);
            if (plant && !Config.WATERARMS_ALLOW_PLANT_SOURCE.get()) {
                feedback(player, bending, BendingElement.WATER, "The leaves refuse you this day.");
                return false;
            }
            if (plant) {
                BendingManager.consumePlantSource(level, source, Config.WATERMANIP_PLANT_REGROW_SECONDS.get());
                fullSource = false;
            } else if (!level.getFluidState(source).is(net.minecraft.world.level.material.Fluids.WATER)
                    && !BendingSources.isIce(level, source)) {
                source = null;
            }
        }
        if (source == null) {
            if (BendingBottles.consumeWaterBottle(player)) {
                fullSource = false;
            } else {
                feedback(player, bending, BendingElement.WATER, "Strike the water with a click to claim your source.");
                return false;
            }
        }
        BendingManager.start(new WaterArms(player, fullSource));
        String sub = bending.boundAbility(player.getInventory().selected + 1);
        feedback(player, bending, BendingElement.WATER, "The water answers as: " + (sub == null ? WaterArms.ID : sub));
        return false;
    }

    /**
     * WaterSpout shared-bind dispatch (reference order): live spout toggles
     * off (+hop launch when sneaking), live wave holds the bind, else a wave
     * starts near source, else a spout starts (fizzles silently if dry).
     */
    private static boolean castWaterSpout(ServerPlayer player, BendingPlayer bending, long gameTime) {
        if (bending.isOnCooldown(WaterSpout.ID, gameTime)) {
            long left = (bending.cooldownExpiresAt(WaterSpout.ID) - gameTime + 19) / 20;
            feedback(player, bending, BendingElement.WATER, "The spout has collapsed (" + Math.max(left, 1) + "s).");
            return false;
        }
        WaterSpout live = BendingManager.find(player.getUUID(), WaterSpout.class);
        if (live != null) {
            BendingManager.remove(live);
            if (player.isShiftKeyDown()) {
                Vec3 push = player.getLookAngle().normalize().scale(Config.WATERSPOUT_HOP_POWER.get());
                player.setDeltaMovement(push);
                player.hurtMarked = true;
            }
            return false;
        }
        if (BendingManager.find(player.getUUID(), WaterSpoutWave.class) != null) {
            return false;
        }
        WaterSpoutWave wave = WaterSpoutWave.create(player);
        if (wave == null) {
            // Eye-ray missed but a tapped source may exist: fall back to the
            // shared selection instead of fizzling.
            BlockPos selected = bending.selectedSource(gameTime);
            if (selected != null && BendingSources.isWaterSource(player.serverLevel(), selected)) {
                wave = WaterSpoutWave.createAt(player, selected);
            }
        }
        if (wave != null) {
            BendingManager.start(wave);
            return false;
        }
        BendingManager.start(new WaterSpout(player));
        return false;
    }

    /**
     * Second click on a live torrent: launch a completed ring, queue a
     * forming one, or freeze a flying wave. Deliberately needs no selected
     * source (it was consumed at sneak-start).
     */
    private static boolean advanceTorrent(ServerPlayer player, BendingPlayer bending, Torrent active) {
        if (active.isLaunching()) {
            if (active.freeze()) {
                BendingManager.remove(active);
                feedback(player, bending, BendingElement.WATER, "The torrent stands frozen!");
            } else {
                feedback(
                        player,
                        bending,
                        BendingElement.WATER,
                        "First drive your wave into flesh, then seal it in ice.");
            }
            return false;
        }
        if (!active.isFormed()) {
            // Clicks before the ring completes do nothing at all.
            return false;
        }
        if (active.fireIfFormed(player)) {
            feedback(player, bending, BendingElement.WATER, "The torrent is loosed — hold sneak and ride it!");
            return false;
        }
        BendingManager.remove(active);
        feedback(player, bending, BendingElement.WATER, "No waterway runs here — the torrent cannot launch.");
        return false;
    }

    /**
     * Torrent click cycle (Korra sneak model). First click readies the
     * torrent; holding sneak grows the deflecting ring around the player;
     * clicking a completed ring shoots it, releasing sneak launches the
     * wave; clicking mid-flight freezes it into trapping ice.
     */
    private static boolean startTorrent(
            ServerPlayer player, BendingPlayer bending, long gameTime, BlockPos source, ServerLevel serverLevel) {
        if (bending.isOnCooldown(Torrent.ID, gameTime)) {
            long left = (bending.cooldownExpiresAt(Torrent.ID) - gameTime + 19) / 20;
            feedback(player, bending, BendingElement.WATER, "The torrent is spent (" + Math.max(left, 1) + "s).");
            return false;
        }
        bending.setCooldown(Torrent.ID, gameTime + Config.TORRENT_COOLDOWN_TICKS.get());
        BendingManager.start(new Torrent(player, source));
        // Keep the focus shimmer while waiting: it marks the chosen source
        // until sneak-start consumes it.
        feedback(player, bending, BendingElement.WATER, "The waters gather — sneak to give them shape.");
        if (Config.ENABLE_DEBUG_LOGGING.get()) {
            AvatarUniverseMod.LOGGER.info("Started Torrent for {}", player.getScoreboardName());
        }
        return false;
    }

    /**
     * Actionbar feedback, throttled to one message per second per player so
     * hold-to-click and the air-click packet path cannot spam the HUD.
     */
    private static void feedback(ServerPlayer player, BendingPlayer bending, String message) {
        feedback(player, bending, null, message);
    }

    private static void feedback(ServerPlayer player, BendingPlayer bending, BendingElement element, String message) {
        if (!bending.tryFeedbackThrottle(player.level().getGameTime())) {
            return;
        }
        player.displayClientMessage(BendingTheme.gradient(message, element), true);
    }
}
