package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of Cozmyc JedCore {@code MetalHook}: click to fire iron grappling
 * hooks (up to 3) that bite into blocks and haul you toward their average
 * anchor. Needs an iron or chainmail chestplate worn, or an iron ingot or
 * block in the pocket. Sneak a full second or sprint to cut loose.
 * Reference values: Cooldown 3000ms, Range 30, MaxHooks 3, TotalHooks
 * unlimited, RequireItems on. Reference gates on metalbending; here any
 * earthbender may use it.
 */
public class MetalHook extends EarthAbility {
    public static final String ID = "MetalHook";

    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.METALHOOK_COOLDOWN_TICKS.get();

    private static final double RANGE = Config.METALHOOK_RANGE.get();
    private static final int MAX_HOOKS = Config.METALHOOK_MAX_HOOKS.get();
    private static final double HOOK_SPEED = Config.METALHOOK_HOOK_SPEED.get();
    /** Ticks of sneak held to cut loose. */
    private static final long SNEAK_RELEASE_TICKS = Config.METALHOOK_SNEAK_RELEASE_TICKS.get();

    private static final double PULL_SPEED = Config.METALHOOK_PULL_SPEED.get();
    private static final double PULL_FACTOR = Config.METALHOOK_PULL_FACTOR.get();

    /** Live hooks to stuck state. */
    private final Map<UUID, Boolean> hooks = new LinkedHashMap<>();

    private final Map<UUID, Integer> slowTicks = new HashMap<>();
    private boolean wasSprinting;
    private long sneakSince = -1L;
    private boolean hasHook;

    public MetalHook(ServerPlayer player) {
        super(player);
        this.wasSprinting = player.isSprinting();
        launchHook();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Click-start gate: iron on the chest or in the pocket. */
    public static boolean hasRequiredInv(ServerPlayer player) {
        ItemStack chest = player.getInventory().armor.get(2);
        if (chest.is(Items.IRON_CHESTPLATE) || chest.is(Items.CHAINMAIL_CHESTPLATE)) {
            return true;
        }
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_BLOCK)) {
                return true;
            }
        }
        return false;
    }

    /** Click path: fire another hook, oldest drops past the maximum. */
    public void launchHook() {
        if (!hasRequiredInv(player)) {
            player.displayClientMessage(Component.literal("Need iron on chest or in pocket."), true);
            return;
        }
        List<UUID> ids = new ArrayList<>(hooks.keySet());
        if (ids.size() > MAX_HOOKS - 1) {
            UUID oldest = ids.get(0);
            if (level.getEntity(oldest) instanceof AbstractArrow old) {
                old.discard();
            }
            hooks.remove(oldest);
            slowTicks.remove(oldest);
            bornTicks.remove(oldest);
        }
        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getLookAngle().normalize();
        Arrow arrow = new Arrow(net.minecraft.world.entity.EntityType.ARROW, level);
        arrow.setPos(eye.x + dir.x * 2, eye.y + dir.y * 2, eye.z + dir.z * 2);
        arrow.setDeltaMovement(dir.scale(HOOK_SPEED));
        level.addFreshEntity(arrow);
        hooks.put(arrow.getUUID(), false);
        slowTicks.put(arrow.getUUID(), 0);
        bornTicks.put(arrow.getUUID(), level.getGameTime());
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (hooks.isEmpty()) {
            return false;
        }
        if (!wasSprinting && player.isSprinting()) {
            return false;
        }
        wasSprinting = player.isSprinting();
        if (player.isShiftKeyDown()) {
            if (sneakSince < 0) {
                sneakSince = level.getGameTime();
            }
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
            if (level.getGameTime() - sneakSince > SNEAK_RELEASE_TICKS) {
                return false;
            }
        } else {
            sneakSince = -1L;
        }
        hasHook = false;
        Vec3 anchorSum = Vec3.ZERO;
        int anchors = 0;
        for (UUID id : new ArrayList<>(hooks.keySet())) {
            if (!(level.getEntity(id) instanceof AbstractArrow arrow) || !arrow.isAlive()) {
                hooks.remove(id);
                slowTicks.remove(id);
                bornTicks.remove(id);
                continue;
            }
            if (eyeDistance(arrow) > RANGE * RANGE) {
                arrow.discard();
                hooks.remove(id);
                slowTicks.remove(id);
                bornTicks.remove(id);
                continue;
            }
            boolean stuck = arrow.onGround() || solidAt(arrow.blockPosition()) || solidAt(nosePos(arrow));
            hooks.put(id, stuck);
            if (stuck) {
                hasHook = true;
            }
            chainParticles(arrow);
            if (stuck) {
                anchorSum = anchorSum.add(arrow.position());
                anchors++;
            }
        }
        if (hasHook && anchors > 0) {
            Vec3 dest = anchorSum.scale(1.0 / anchors);
            Vec3 to = dest.subtract(player.position());
            double dist = to.length();
            if (dist < 1.0) {
                player.setDeltaMovement(new Vec3(0, 0.08, 0));
            } else {
                player.setDeltaMovement(to.normalize().scale(Math.min(PULL_SPEED, dist * PULL_FACTOR)));
            }
            player.hurtMarked = true;
            player.resetFallDistance();
        }
        return true;
    }

    private final Map<UUID, Long> bornTicks = new java.util.HashMap<>();

    private long bornTick(UUID id) {
        return bornTicks.getOrDefault(id, level.getGameTime());
    }

    /** The cell the arrowhead is buried in, for nose-first wall sticks. */
    private BlockPos nosePos(AbstractArrow arrow) {
        Vec3 flight = arrow.getDeltaMovement();
        if (flight.lengthSqr() < 1.0e-6) {
            return arrow.blockPosition();
        }
        Vec3 nose = arrow.position().add(flight.normalize().scale(0.7));
        return BlockPos.containing(nose);
    }

    private boolean solidAt(BlockPos pos) {
        return !level.getBlockState(pos).isAir();
    }

    private double eyeDistance(AbstractArrow arrow) {
        return player.getEyePosition().distanceToSqr(arrow.position());
    }

    /** Gray chain links from shoulder to every hook. */
    private void chainParticles(AbstractArrow arrow) {
        Vec3 from = player.position().add(0, 1, 0);
        Vec3 to = arrow.position();
        double dist = from.distanceTo(to);
        int points = Math.max(2, (int) (dist * 2));
        DustParticleOptions link = new DustParticleOptions(new Vector3f(0.8F, 0.8F, 0.8F), 1.0F);
        for (int i = 0; i <= points; i++) {
            Vec3 p = from.add(to.subtract(from).scale((double) i / points));
            level.sendParticles(link, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void onRemove() {
        for (UUID id : new ArrayList<>(hooks.keySet())) {
            if (level.getEntity(id) instanceof AbstractArrow arrow) {
                arrow.discard();
            }
        }
        hooks.clear();
        slowTicks.clear();
        bornTicks.clear();
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
