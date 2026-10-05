package com.phatabaniyan.avataruniverse.bending;

import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import com.phatabaniyan.avataruniverse.bending.ability.plant.RazorLeaf;
import com.phatabaniyan.avataruniverse.bending.ability.water.BloodPuppet;
import com.phatabaniyan.avataruniverse.bending.ability.water.Bloodbending;
import com.phatabaniyan.avataruniverse.bending.ability.water.Drain;
import com.phatabaniyan.avataruniverse.bending.ability.water.FrostBreath;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceBlast;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceClaws;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceSpikeBlast;
import com.phatabaniyan.avataruniverse.bending.ability.water.IceSpikePillarField;
import com.phatabaniyan.avataruniverse.bending.ability.water.PhaseChange;
import com.phatabaniyan.avataruniverse.bending.ability.water.Torrent;
import com.phatabaniyan.avataruniverse.bending.ability.water.WakeFishing;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterArms;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterArmsSpear;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterArmsWhip;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterBubble;
import com.phatabaniyan.avataruniverse.bending.ability.water.WaterSpoutWave;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Port of ProjectKorra {@code BendingManager} (BukkitRunnable timer).
 * Korra progresses every ability + combo + passive each tick from
 * {@code runTaskTimer}; here the driver is {@code ServerTickEvent.Post},
 * preserving 20-ticks = 1s timing.
 */
@EventBusSubscriber(modid = AvatarUniverseMod.MODID)
public final class BendingManager {
    private static final Set<BendingAbility> ACTIVE = ConcurrentHashMap.newKeySet();
    /** Consumed plant sources awaiting regrow, mapped to their revert game time. */
    private static final Map<TempBlock, Long> PENDING_REVERT = new ConcurrentHashMap<>();
    /** Last-tick sneak state per player, for sneak-look edge activation. */
    private static final Map<UUID, Boolean> SNEAK_STATE = new ConcurrentHashMap<>();
    /** Last seen hotbar slot per player (slot change clears the tapped source). */
    private static final Map<UUID, Integer> HELD_SLOT = new ConcurrentHashMap<>();

    private BendingManager() {}

    public static void start(BendingAbility ability) {
        ACTIVE.add(ability);
        // Rebinding shells park slot 1 (the first sub-bind); modal
        // abilities park slot 1 too, where the player then scrolls.
        if (ability instanceof com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor
                || ability instanceof com.phatabaniyan.avataruniverse.bending.ability.water.WaterArms
                || ability instanceof com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere
                || ability instanceof com.phatabaniyan.avataruniverse.bending.ability.air.AirFlight
                || ability instanceof com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk) {
            com.phatabaniyan.avataruniverse.bending.BendingBoardSync.queueForceSlot(ability.owner(), 0);
        }
    }

    /** Remove one ability, running its cleanup (reverts TempBlocks). */
    public static void remove(BendingAbility ability) {
        if (ACTIVE.remove(ability)) {
            ability.onRemove();
        }
    }

    /** The active ability of the given type owned by the player, if any. */
    public static <T extends BendingAbility> T find(java.util.UUID owner, Class<T> type) {
        for (BendingAbility ability : ACTIVE) {
            if (type.isInstance(ability) && ability.owner().equals(owner)) {
                return type.cast(ability);
            }
        }
        return null;
    }

    public static void cancelFor(java.util.UUID owner) {
        ACTIVE.removeIf(ability -> {
            if (ability.owner().equals(owner)) {
                ability.onRemove();
                return true;
            }
            return false;
        });
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        ACTIVE.removeIf(ability -> {
            boolean done;
            try {
                done = !ability.progress();
            } catch (Exception e) {
                AvatarUniverseMod.LOGGER.warn("Bending ability {} errored, removing", ability.name(), e);
                done = true;
            }
            if (done) {
                ability.onRemove();
            }
            return done;
        });
        tickSourceFocus(event.getServer());
        tickSneakActivate(event.getServer());
        tickSlotWatch(event.getServer());
        tickReverts(event.getServer());
        tickHealingWaters(event.getServer());
        tickWaterPassives(event.getServer());
        WaterArmsWhip.tickGrabs(event.getServer());
        WaterArmsSpear.tickFrozen(event.getServer());
        Torrent.tickFrozen(event.getServer());
        WaterSpoutWave.tickFrozen(event.getServer());
        com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast.tickOrigins(event.getServer());
        TempBlock.suppressFlowTicks();
        com.phatabaniyan.avataruniverse.bending.ability.plant.TangleStop.tick(event.getServer());
        tickBoardSync(event.getServer());
        com.phatabaniyan.avataruniverse.bending.BendingPassives.tick(event.getServer());
    }

    /** Push board state to every player ten times a second. */
    private static void tickBoardSync(net.minecraft.server.MinecraftServer server) {
        if (server.getTickCount() % 2 != 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            try {
                com.phatabaniyan.avataruniverse.bending.BendingBoardSync.sync(player);
            } catch (RuntimeException ignored) {
                // A sync hiccup must never take the tick down.
            }
        }
    }

    /** Forget per-player tick state (call on logout). */
    public static void forgetPlayer(UUID uuid) {
        SNEAK_STATE.remove(uuid);
        HELD_SLOT.remove(uuid);
    }

    /**
     * Sneak-look edge activation for WaterArms: starting to sneak while the
     * bound slot holds WaterArms grows arms from whatever water is in view,
     * no clicks needed. Rising edge only, so a sustained sneak never loops;
     * removal cooldowns and live-instance checks still apply.
     */
    private static void tickSneakActivate(MinecraftServer server) {
        if (!Config.ENABLE_BENDING.get()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean sneaking = player.isShiftKeyDown();
            boolean was = SNEAK_STATE.getOrDefault(player.getUUID(), false);
            SNEAK_STATE.put(player.getUUID(), sneaking);
            if (!sneaking || was) {
                continue;
            }
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending == null || !bending.isToggled()) {
                continue;
            }
            String held = bending.boundAbility(player.getInventory().selected + 1);
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthArmor(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast live = find(
                            player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.class);
                    if (live != null && !live.isProgressing()) {
                        if (!live.reprepare()) {
                            BendingManager.remove(live);
                        }
                    } else if (live == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthBlast(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.Catapult(player, true));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion.isEarthbendable(
                                    player.serverLevel(), player.blockPosition().below())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.Accretion(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.RaiseEarth.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.RaiseEarth.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.RaiseEarth.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.CollapseWall.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.RaiseEarth(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.Shockwave(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.Extraction.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (!bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.Extraction.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.Extraction.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.Extraction(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips.class)
                            == null) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.MetalClips(player, true));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.LavaFlow(player, true));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge live = find(
                            player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.class);
                    if (live != null && !live.hasShot()) {
                        if (!live.retarget()) {
                            BendingManager.remove(live);
                        }
                    } else if (live == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.LavaSurge(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.QuickWeld(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthPillar.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    // No source gate here: the source locks 5 ticks in, once
                    // the aim has settled, so aim-after-press still works.
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthPillar.class)
                            == null) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthPillar(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard live = find(
                            player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.class);
                    if (live != null && !live.hasShot()) {
                        live.select();
                    } else if (live == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthShard(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure live =
                            find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure.class);
                    if (live != null) {
                        live.widenOrSeal();
                    } else if (!bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure.canBegin(player)) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.Fissure(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisc(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield live = find(
                            player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.class);
                    if (live != null) {
                        BendingManager.remove(live);
                    } else if (!bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield.ID,
                            player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.MagnetShield(player, false));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments live = find(
                            player.getUUID(),
                            com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.class);
                    if (live != null) {
                        live.select();
                    } else if (!bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.MetalFragments(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge live = find(
                            player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.class);
                    if (live != null && !live.hasShot()) {
                        if (!live.retarget()) {
                            BendingManager.remove(live);
                        }
                    } else if (live == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.MudSurge(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.LavaDisk(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab live = find(
                            player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.class);
                    if (live != null
                            && live.mode()
                                    != com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.GrabMode.DRAG) {
                        BendingManager.remove(live);
                    } else if (live == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.canBegin(player)) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab(
                                player, com.phatabaniyan.avataruniverse.bending.ability.earth.EarthGrab.GrabMode.DRAG));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.FireBurst(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireShield.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireShield.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireShield.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.FireShield(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FlameBreath.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FlameBreath.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FlameBreath.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.FlameBreath(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireBreath.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireBreath.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireBreath.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.FireBreath(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireWave.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireWave.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireWave.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.FireWave(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireComet.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireComet.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireComet.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.FireComet(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.FireShots(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(
                                            player.getUUID(),
                                            com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.FireManipulation(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.HeatControl.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.HeatControl.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.HeatControl.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.HeatControl(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.ArcSpark(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.ChargeBolt(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.Bolt.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.Bolt.class) == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.Bolt.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.Bolt(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.Lightning.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.Lightning.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.Lightning.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.Lightning(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.LightningBurst.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(
                                            player.getUUID(),
                                            com.phatabaniyan.avataruniverse.bending.ability.fire.LightningBurst.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.LightningBurst.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.LightningBurst(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.CombustBeam.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.CombustBeam.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.CombustBeam.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.CombustBeam(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.Explode.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.Explode.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.Explode.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.fire.Explode(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.CombustionBlast.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(
                                            player.getUUID(),
                                            com.phatabaniyan.avataruniverse.bending.ability.fire.CombustionBlast.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.CombustionBlast.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.CombustionBlast(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.FIRE)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.fire.Combustion(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirShield.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirShield.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.AirShield.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirShield(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirBurst(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (!bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast.ID,
                            player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirBlast(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (!bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.ID,
                            player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe(player, true));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (!bending.isOnCooldown(
                            com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe.ID,
                            player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.air.AirSwipe(player, true));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirSuction.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirSuction.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.AirSuction.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.air.AirSuction(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.Suffocate.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.Suffocate.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.Suffocate.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.Suffocate(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.Tornado.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.Tornado.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.Tornado.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.Tornado(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirBreath.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirBreath.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.AirBreath.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirBreath(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirBullet(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.Meditate.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.Meditate.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.Meditate.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.Meditate(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.SonicBlast.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.SonicBlast.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.SonicBlast.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.air.SonicBlast(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.Zephyr.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.Zephyr.class) == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.Zephyr.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.Zephyr(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirStream(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AIR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.air.AirStream.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.air.AirStream(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AVATAR)) {
                    if (find(
                                            player.getUUID(),
                                            com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.avatar.ElementSphere(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.avatar.SpiritBeam.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.AVATAR)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.avatar.SpiritBeam.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.avatar.SpiritBeam.ID,
                                    player.level().getGameTime())) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.avatar.SpiritBeam(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.plant.LeafStorm.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.WATER)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.plant.LeafStorm.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.plant.LeafStorm.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.plant.LeafStorm.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.plant.LeafStorm(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.Dig.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.Dig.class) == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.Dig.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.Dig.canBegin(player)) {
                        BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.earth.Dig(player));
                    }
                    continue;
                }
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.earth.EarthTunnel.ID.equalsIgnoreCase(held)) {
                if (bending.hasElement(BendingElement.EARTH)) {
                    if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.earth.EarthTunnel.class)
                                    == null
                            && !bending.isOnCooldown(
                                    com.phatabaniyan.avataruniverse.bending.ability.earth.EarthTunnel.ID,
                                    player.level().getGameTime())
                            && com.phatabaniyan.avataruniverse.bending.ability.earth.EarthTunnel.canBegin(player)) {
                        BendingManager.start(
                                new com.phatabaniyan.avataruniverse.bending.ability.earth.EarthTunnel(player));
                    }
                    continue;
                }
            }
            if (!bending.hasElement(BendingElement.WATER)) {
                continue;
            }
            com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor armor =
                    find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.class);
            if (armor != null && com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.isSubBind(held)) {
                // Sub-abilities are owned by the live shell: hold-type subs
                // start in PlantArmor.progress, and RazorLeaf must not spawn
                // as a standalone cast while the shell pays for it.
                continue;
            }
            if (com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.class)
                                == null
                        && !bending.isOnCooldown(
                                com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor.ID,
                                player.level().getGameTime())) {
                    BendingManager.start(new com.phatabaniyan.avataruniverse.bending.ability.plant.PlantArmor(player));
                }
                continue;
            }
            if (WaterArms.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), WaterArms.class) != null) {
                    continue;
                }
                long gameTime = player.level().getGameTime();
                if (bending.isOnCooldown(WaterArms.ID, gameTime)) {
                    continue;
                }
                BendingEvents.startWaterArms(player, bending, gameTime);
            } else if (FrostBreath.ID.equalsIgnoreCase(held)) {
                // JedCore sneak ability: pressing sneak with it bound starts
                // the breath, no click involved.
                if (find(player.getUUID(), FrostBreath.class) != null) {
                    continue;
                }
                BendingEvents.startFrostBreath(player, bending);
            } else if (WaterBubble.ID.equalsIgnoreCase(held)) {
                // Sneak-hold bubble: pressing sneak with it bound pushes the
                // water back for as long as sneak is held, following wherever
                // the player goes; releasing sneak melts it. No click needed.
                WaterBubble live = find(player.getUUID(), WaterBubble.class);
                if (live != null) {
                    live.refresh(player, true);
                    continue;
                }
                if (WaterBubble.canStart(player)) {
                    start(new WaterBubble(player, true));
                }
            } else if (IceBlast.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), IceBlast.class) == null) {
                    BlockPos source =
                            IceBlast.findSource(player, Config.ICEBLAST_RANGE.get(), Config.ICEBLAST_ALLOW_SNOW.get());
                    if (source != null) {
                        start(new IceBlast(player, source));
                    }
                }
            } else if (IceSpikeBlast.BIND.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), IceSpikeBlast.class) == null
                        && !bending.isOnCooldown(
                                IceSpikeBlast.ID, player.level().getGameTime())) {
                    BlockPos source = WaterSpoutWave.raycastWaterSource(player, Config.ICESPIKE_BLAST_RANGE.get());
                    if (source != null) {
                        start(new IceSpikeBlast(player, source));
                    } else if (!bending.isOnCooldown(
                            IceSpikePillarField.ID, player.level().getGameTime())) {
                        start(new IceSpikePillarField(player));
                    }
                }
            } else if (PhaseChange.ID.equalsIgnoreCase(held)) {
                PhaseChange live = find(player.getUUID(), PhaseChange.class);
                if ((live == null || !live.isMelt())
                        && !bending.isOnCooldown(
                                "PhaseChangeMelt", player.level().getGameTime())) {
                    start(new PhaseChange(player, PhaseChange.Mode.MELT));
                }
            } else if (Bloodbending.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), Bloodbending.class) == null) {
                    LivingEntity victim = Bloodbending.findTarget(player, Config.BLOODBENDING_RANGE.get());
                    if (victim != null) {
                        start(new Bloodbending(player, victim));
                    }
                }
            } else if (BloodPuppet.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), BloodPuppet.class) == null) {
                    LivingEntity victim = BloodPuppet.findVictim(player);
                    if (victim != null) {
                        start(new BloodPuppet(player, victim));
                    }
                }
            } else if (Drain.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), Drain.class) == null
                        && !bending.isOnCooldown(Drain.ID, player.level().getGameTime())) {
                    start(new Drain(player, Drain.hasFillable(player)));
                }
            } else if (IceClaws.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), IceClaws.class) == null) {
                    start(new IceClaws(player));
                }
            } else if (WakeFishing.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), WakeFishing.class) == null) {
                    BlockPos water = WakeFishing.findWater(player, Config.WAKEFISHING_RANGE.get());
                    if (water != null) {
                        start(new WakeFishing(player, water));
                    }
                }
            } else if (RazorLeaf.ID.equalsIgnoreCase(held)) {
                if (find(player.getUUID(), RazorLeaf.class) == null) {
                    BlockPos plant = null;
                    Vec3 eye = player.getEyePosition();
                    Vec3 look = player.getLookAngle().normalize();
                    for (double d = 0.5; d <= 7.0; d += 0.5) {
                        BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
                        if (BendingSources.isPlant(player.serverLevel(), pos)) {
                            plant = pos.immutable();
                            break;
                        }
                    }
                    start(new RazorLeaf(player, plant, plant != null));
                }
            }
        }
    }

    /**
     * Slot-change deselect: scrolling to another hotbar slot drops the tapped
     * water source on its own (any ability, water or not). Silent — slot
     * changes are frequent and the next tap re-selects.
     */
    private static void tickSlotWatch(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            int slot = player.getInventory().selected;
            Integer last = HELD_SLOT.put(id, slot);
            if (last != null && last != slot) {
                BendingPlayer bending = BendingPlayer.get(id);
                if (bending != null) {
                    bending.clearSource();
                }
            }
        }
    }

    /** Queue a TempBlock to revert at the given game time (plant regrow, wave trails). */
    public static void scheduleRevert(TempBlock temp, long revertAtGameTime) {
        PENDING_REVERT.put(temp, revertAtGameTime);
    }

    /**
     * Consume a plant source (Korra plantbending): the plant (both halves for
     * tall grass / large fern) is removed and grows back after 50-100% of
     * the base delay (Korra PlantRegrowth timing).
     */
    public static void consumePlantSource(ServerLevel level, BlockPos pos, int baseSeconds) {
        long revertAt = level.getGameTime()
                + baseSeconds * 20L / 2
                + (long) (level.random.nextDouble() * baseSeconds * 20L / 2);
        List<BlockPos> parts = new ArrayList<>();
        parts.add(pos);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DoublePlantBlock) {
            parts.add(state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below());
        }
        for (BlockPos part : parts) {
            PENDING_REVERT.put(new TempBlock(level, part, Blocks.AIR.defaultBlockState(), TempBlock.QUIET), revertAt);
        }
    }

    private static void tickReverts(MinecraftServer server) {
        if (PENDING_REVERT.isEmpty()) {
            return;
        }
        long gameTime = server.overworld().getGameTime();
        PENDING_REVERT.entrySet().removeIf(entry -> {
            if (gameTime >= entry.getValue()) {
                entry.getKey().revert();
                return true;
            }
            return false;
        });
    }

    /**
     * HealingWaters passive: holding it bound while standing in water keeps
     * regeneration topped up. No click, no sneak, no instance.
     */
    private static void tickHealingWaters(MinecraftServer server) {
        if (!Config.ENABLE_BENDING.get()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending == null || !bending.isToggled() || !bending.hasElement(BendingElement.WATER)) {
                continue;
            }
            if (!"HealingWaters".equalsIgnoreCase(bending.boundAbility(player.getInventory().selected + 1))) {
                continue;
            }
            if (!player.serverLevel()
                    .getFluidState(player.blockPosition())
                    .is(net.minecraft.world.level.material.Fluids.WATER)) {
                continue;
            }
            MobEffectInstance current = player.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION);
            if (current == null || current.getDuration() < 20) {
                player.addEffect(new MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.REGENERATION, 40, Config.HEALING_POTENCY.get()));
            }
            if (Config.HEALING_PARTICLES.get() && player.level().getGameTime() % 10 == 0) {
                Vec3 base = player.position().add(0.0, 1.0, 0.0);
                for (int i = 0; i < 36; i += 12) {
                    double angle = 2.0 * Math.PI * i / 36.0;
                    player.serverLevel()
                            .sendParticles(
                                    new net.minecraft.core.particles.DustParticleOptions(
                                            new org.joml.Vector3f(0.0F, 1.0F, 1.0F), 1.0F),
                                    base.x + Math.cos(angle) * 0.75,
                                    base.y,
                                    base.z + Math.sin(angle) * 0.75,
                                    1,
                                    0.0,
                                    0.0,
                                    0.0,
                                    0.0);
                }
            }
        }
    }
    /**
     * Water passives (no binds, no instances): half-or-fully submerged
     * waterbenders keep Dolphin's Grace topped up, and standing on any
     * ice/snow type grants Speed + Jump Boost. Effects lapse on their own
     * when the condition ends.
     */
    private static void tickWaterPassives(MinecraftServer server) {
        if (!Config.ENABLE_BENDING.get()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending == null || !bending.isToggled() || !bending.hasElement(BendingElement.WATER)) {
                continue;
            }
            ServerLevel level = player.serverLevel();
            if (level.getFluidState(player.blockPosition()).is(net.minecraft.world.level.material.Fluids.WATER)) {
                MobEffectInstance grace = player.getEffect(net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE);
                if (grace == null || grace.getDuration() < 20) {
                    player.addEffect(new MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE, 40, 0, false, false, false));
                }
            }
            if (BendingSources.isIce(level, player.blockPosition().below())) {
                MobEffectInstance speed = player.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED);
                if (speed == null || speed.getDuration() < 20) {
                    player.addEffect(new MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 0, false, false, false));
                }
                MobEffectInstance jump = player.getEffect(net.minecraft.world.effect.MobEffects.JUMP);
                if (jump == null || jump.getDuration() < 20) {
                    player.addEffect(new MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.JUMP, 40, 0, false, false, false));
                }
            }
        }
    }
    /**
     * Korra source-focus shimmer: every selected water source constantly emits
     * rising water particles until the owner moves out of focus range, taps
     * another source, or fires the ability (which clears the selection).
     * Server-side particles so everyone nearby sees the same focus.
     */
    private static void tickSourceFocus(MinecraftServer server) {
        if (!Config.ENABLE_BENDING.get()) {
            return;
        }
        double focusRange = Config.WATERMANIP_SOURCE_FOCUS_RANGE.get();
        double focusRangeSqr = focusRange * focusRange;
        long gameTime = server.overworld().getGameTime();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            BendingPlayer bending = BendingPlayer.get(player.getUUID());
            if (bending == null || !bending.isToggled() || !bending.hasElement(BendingElement.WATER)) {
                continue;
            }
            BlockPos source = bending.selectedSource(gameTime);
            if (source == null) {
                continue;
            }
            ServerLevel level = player.serverLevel();
            if (!BendingSources.isWaterSource(level, source)
                    || player.blockPosition().distSqr(source) > focusRangeSqr) {
                // Out of range or gone: drop the focus silently; the cast
                // attempt message covers the gone case if they try to fire.
                bending.clearSource();
                continue;
            }
            if ((gameTime & 1) == 0) {
                Vec3 center = Vec3.atCenterOf(source).add(0.0, 0.5, 0.0);
                level.sendParticles(
                        ParticleTypes.FALLING_WATER, center.x, center.y, center.z, 4, 0.25, 0.45, 0.25, 0.03);
            }
        }
    }
}
