package com.phatabaniyan.avataruniverse.bending.ability.spiritual;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code SpiritProjection}: hold sneak to charge, then
 * the spirit leaves the body behind — an armor-stand double wearing your
 * skin — while you drift as a spectator tethered to it. Any hit on the body
 * snaps you back; drift home and click the double to return. Reference
 * values: Cooldown 8000ms, Charge 5000ms, Tether 128.
 */
public class SpiritProjection extends BendingAbility {
    public static final String ID = "SpiritProjection";

    /** Reference Cooldown 8000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.SPIRITPROJECTION_COOLDOWN_TICKS.get();

    private static final long CHARGE_TICKS = Config.SPIRITPROJECTION_CHARGE_TICKS.get();
    private static final double TETHER = Config.SPIRITPROJECTION_TETHER.get();

    private enum Phase {
        CHARGING,
        PROJECTED
    }

    private static final Map<UUID, UUID> BODIES = new ConcurrentHashMap<>();
    private static final Map<UUID, SpiritProjection> LIVE = new ConcurrentHashMap<>();

    private final ServerLevel level;
    private final ServerPlayer player;
    private final long startTick;
    private Phase phase = Phase.CHARGING;
    private boolean prevMayfly;
    private boolean prevFlying;
    private GameType prevMode = GameType.SURVIVAL;
    private Vec3 bodyPos;
    private UUID bodyId;
    private boolean started;
    private double ringAngle;

    public SpiritProjection(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.startTick = level.getGameTime();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Any damage to a body double snaps its owner back. */
    public static void returnOwnerIfBody(UUID standId) {
        UUID ownerId = BODIES.get(standId);
        if (ownerId == null) {
            return;
        }
        SpiritProjection projection = LIVE.get(ownerId);
        if (projection != null) {
            projection.returnToBody(false);
            return;
        }
        BODIES.remove(standId);
    }

    /** Click while projected: return only when gazing at the double. */
    public void tryReturnByClick() {
        if (phase != Phase.PROJECTED || bodyPos == null) {
            return;
        }
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle().normalize();
        double returnRange = Config.SPIRITPROJECTION_RETURN_RANGE.get();
        for (double d = 0; d <= returnRange; d += 0.5) {
            Vec3 at = eye.add(look.scale(d));
            if (at.distanceToSqr(bodyPos.add(0, 1, 0)) <= returnRange * returnRange / 5.76) {
                returnToBody(true);
                return;
            }
        }
    }

    private static ItemStack orRobe(ItemStack worn, net.minecraft.world.item.Item fallback) {
        if (worn != null && !worn.isEmpty()) {
            return worn.copy();
        }
        ItemStack robe = new ItemStack(fallback);
        robe.set(DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(0xE8821E, true));
        return robe;
    }

    private void project() {
        bodyPos = player.position();
        prevMayfly = player.getAbilities().mayfly;
        prevFlying = player.getAbilities().flying;
        prevMode = player.gameMode.getGameModeForPlayer();
        player.stopRiding();

        ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);
        stand.moveTo(bodyPos.x, bodyPos.y, bodyPos.z, player.getYRot(), 0);
        stand.setNoGravity(true);
        stand.setShowArms(true);
        stand.setYRot(player.getYRot());
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
        stand.setItemSlot(EquipmentSlot.HEAD, head);
        stand.setItemSlot(
                EquipmentSlot.CHEST, orRobe(player.getItemBySlot(EquipmentSlot.CHEST), Items.LEATHER_CHESTPLATE));
        stand.setItemSlot(EquipmentSlot.LEGS, orRobe(player.getItemBySlot(EquipmentSlot.LEGS), Items.LEATHER_LEGGINGS));
        stand.setItemSlot(EquipmentSlot.FEET, orRobe(player.getItemBySlot(EquipmentSlot.FEET), Items.LEATHER_BOOTS));
        level.addFreshEntity(stand);
        bodyId = stand.getUUID();
        BODIES.put(bodyId, owner);
        LIVE.put(owner, this);

        player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        player.displayClientMessage(
                Component.literal("Spirit free — the tether holds " + (int) TETHER + " blocks."), true);
        level.playSound(
                null, bodyPos.x, bodyPos.y, bodyPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.4F);
        phase = Phase.PROJECTED;
        started = true;
    }

    private void returnToBody(boolean voluntary) {
        cleanupBody();
        if (bodyPos != null && player.isAlive() && !player.hasDisconnected()) {
            player.teleportTo(level, bodyPos.x, bodyPos.y, bodyPos.z, player.getYRot(), player.getXRot());
            player.displayClientMessage(
                    Component.literal(voluntary ? "Spirit home." : "The body was struck — snapped back!"), true);
        }
        player.gameMode.changeGameModeForPlayer(prevMode);
        player.getAbilities().mayfly = prevMayfly;
        player.getAbilities().flying = prevFlying;
        player.onUpdateAbilities();
        player.resetFallDistance();
        cool(owner, player, ID, COOLDOWN_TICKS);
        BendingManager.remove(this);
    }

    private void cleanupBody() {
        LIVE.remove(owner);
        if (bodyId != null) {
            BODIES.remove(bodyId);
            Entity found = level.getEntity(bodyId);
            if (found != null) {
                found.discard();
            }
            bodyId = null;
        }
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            cleanupBody();
            return false;
        }
        if (phase == Phase.CHARGING) {
            if (!player.isShiftKeyDown()) {
                return false;
            }
            if (level.getGameTime() - startTick >= CHARGE_TICKS) {
                project();
                return true;
            }
            Vec3 center = player.position().add(0, 1.2, 0);
            ringAngle += 0.7;
            for (int k = 0; k < 6; k++) {
                double a = ringAngle + k * (Math.PI / 3);
                level.sendParticles(
                        BendingTheme.particle(Config.SPIRITPROJECTION_CHARGE_PARTICLE.get(), ParticleTypes.END_ROD),
                        player.getX() + Math.cos(a),
                        center.y,
                        player.getZ() + Math.sin(a),
                        Config.SPIRITPROJECTION_CHARGE_PARTICLE_COUNT.get(),
                        0.05,
                        0.05,
                        0.05,
                        0.01);
            }
            return true;
        }
        if (bodyPos != null && player.position().distanceToSqr(bodyPos) > TETHER * TETHER) {
            player.displayClientMessage(Component.literal("The tether snaps!"), true);
            returnToBody(false);
            return false;
        }
        return true;
    }

    @Override
    public void onRemove() {
        cleanupBody();
        if (!started) {
            return;
        }
        started = false;
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            player.gameMode.changeGameModeForPlayer(prevMode);
            player.getAbilities().mayfly = prevMayfly;
            player.getAbilities().flying = prevFlying;
            player.onUpdateAbilities();
        }
        if (bodyPos != null && player.isAlive() && !player.hasDisconnected()) {
            player.teleportTo(level, bodyPos.x, bodyPos.y, bodyPos.z, player.getYRot(), player.getXRot());
        }
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying() && !player.hasDisconnected();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.SPIRITUAL) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
