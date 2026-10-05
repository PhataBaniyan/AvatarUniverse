package com.phatabaniyan.avataruniverse.bending.ability.plant;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code PlantArmor} (MultiAbility). While sneaking,
 * 14 plants within 9 blocks are woven into a leaf shell: leather armour is
 * swapped in, the player gains Dolphin's Grace/Speed/Jump boosts, a set of 9
 * sub-ability binds replaces the hotbar, and one durability pool pays for
 * every sub. Fall and drowning damage is absorbed by the shell instead.
 *
 * <p>Sub-ability trigger model follows Korra's MultiAbilityManager exactly:
 * a hold-type sub (RazorLeaf/LeafShield/LeafDome/Regenerate) is active while
 * sneak is held with it bound to the selected slot, and is dismissed by
 * releasing sneak or switching slots; a click-type sub (VineWhip/Tangle/
 * Grapple/Leap/Disperse) fires on the left click that casts the slot.</p>
 */
public class PlantArmor extends BendingAbility {
    public static final String ID = "PlantArmor";

    /** Korra {@code Util.LEAF_COLOR} = #48B518. */
    private static final net.minecraft.core.particles.BlockParticleOption LEAF =
            new net.minecraft.core.particles.BlockParticleOption(
                    net.minecraft.core.particles.ParticleTypes.BLOCK,
                    net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState());

    /** Multi-ability order on slots 1-9 (Korra {@code ArmorAbility}). */
    public static final String[] SUBS = {
        "VineWhip", "RazorLeaf", "Tangle", "Grapple", "Leap", "LeafShield", "LeafDome", "Regenerate", "Disperse"
    };

    private enum State {
        FORMING,
        FORMED,
        DISPERSING
    }

    private final ServerLevel level;
    private final List<BlockPos> sources = new ArrayList<>();
    private final Set<TempBlock> shield = new HashSet<>();
    /** Water held out of the LeafDome, WaterBubble-style (never per-tick reverted). */
    private final Map<BlockPos, TempBlock> drained = new HashMap<>();

    private final List<ItemStack> savedArmor = new ArrayList<>();
    private final Map<Integer, String> savedSlots = new java.util.HashMap<>();
    private State state = State.FORMING;
    private String active;
    private double durability;
    private double maxDurability;
    private double durabilityDecay;
    private final int requiredPlants;
    private final double selectRange;
    private final long duration;
    private final int swimBoost;
    private final int speedBoost;
    private final int jumpBoost;

    // VineWhip
    private int whipRange;
    private boolean whipForward;

    // Tangle
    private Vec3 tanglePos;
    private Vec3 tangleDir;

    // Tangle
    private double angle = 0;

    // Grapple
    private Vec3 grappleCurrent;
    private Vec3 grappleTarget;
    private int grappleRange;
    private boolean pulling;

    public PlantArmor(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.maxDurability = this.durability = Config.PLANTARMOR_DURABILITY.get();
        this.duration = Config.PLANTARMOR_DURATION_TICKS.get();
        this.durabilityDecay = this.duration <= 0 ? 0 : maxDurability / (double) duration;
        this.requiredPlants = Config.PLANTARMOR_REQUIRED_PLANTS.get();
        this.selectRange = Config.PLANTARMOR_SELECT_RANGE.get();
        this.swimBoost = Config.PLANTARMOR_BOOST_SWIM.get();
        this.speedBoost = Config.PLANTARMOR_BOOST_SPEED.get();
        this.jumpBoost = Config.PLANTARMOR_BOOST_JUMP.get();
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null) {
            for (int slot = 1; slot <= 9; slot++) {
                savedSlots.put(slot, bending.boundAbility(slot));
                bending.bind(slot, SUBS[slot - 1]);
            }
        }
        player.getInventory().selected = 0;
    }

    public static boolean isHoldSub(String sub) {
        return "RazorLeaf".equalsIgnoreCase(sub)
                || "LeafShield".equalsIgnoreCase(sub)
                || "LeafDome".equalsIgnoreCase(sub)
                || "Regenerate".equalsIgnoreCase(sub);
    }

    public static boolean isClickSub(String sub) {
        return "VineWhip".equalsIgnoreCase(sub)
                || "Tangle".equalsIgnoreCase(sub)
                || "Grapple".equalsIgnoreCase(sub)
                || "Leap".equalsIgnoreCase(sub)
                || "Disperse".equalsIgnoreCase(sub);
    }

    /** Absorb fall/drown damage; Korra drains the shell instead. */
    public boolean damage(float amount) {
        if (durability < amount) {
            return false;
        }
        durability -= amount;
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
        if (bending == null
                || !bending.hasElement(BendingElement.WATER)
                || !bending.isToggled()
                || player.level() != level) {
            return false;
        }
        long now = level.getGameTime();
        if (durability <= 0) {
            return false;
        }
        if (duration > 0 && now >= startTime + duration) {
            return false;
        }
        switch (state) {
            case FORMING:
                if (!player.isShiftKeyDown()) {
                    return false;
                }
                chantAura(player);
                if (sources.size() >= requiredPlants) {
                    formShell(player);
                    state = State.FORMED;
                    player.displayClientMessage(
                            Component.literal(
                                    "PlantArmor formed! Slots: 1:VineWhip 2:RazorLeaf 3:Tangle 4:Grapple 5:Leap 6:LeafShield 7:LeafDome 8:Regenerate 9:Disperse"),
                            false);
                    return true;
                }
                BlockPos plant = randomPlantSource(player);
                if (plant != null) {
                    BendingManager.consumePlantSource(level, plant, Config.WATERMANIP_PLANT_REGROW_SECONDS.get());
                    sources.add(plant);
                }
                return true;
            case FORMED:
                tickFormed(player, bending, now);
                return true;
            case DISPERSING:
                return false;
        }
        return false;
    }

    private void tickFormed(ServerPlayer player, BendingPlayer bending, long now) {
        maxDurability -= durabilityDecay;
        if (durability > maxDurability) {
            durability = Math.max(durability, 0);
            durability = maxDurability;
        }
        player.displayClientMessage(
                Component.literal("[" + (player.getInventory().selected + 1) + "] " + activeText(bending, player)
                        + " | Durability [" + (int) durability + " / " + (int) maxDurability + "]"),
                true);
        player.addEffect(
                new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 10, Math.max(swimBoost - 1, 0), false, false, false));
        player.addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, Math.max(speedBoost - 1, 0), false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, Math.max(jumpBoost - 1, 0), false, false, false));
        String bound = bending.boundAbility(player.getInventory().selected + 1);
        if (active != null && (bound == null || !bound.equalsIgnoreCase(active))) {
            resetActive();
        }
        if (active == null) {
            if (isHoldSub(bound) && player.isShiftKeyDown()) {
                startSub(player, bending, bound, now);
            }
            return;
        }
        if (isHoldSub(active) && !player.isShiftKeyDown()) {
            resetActive();
            return;
        }
        switch (active) {
            case "VineWhip":
                progressWhip(player);
                break;
            case "Tangle":
                progressTangle(player);
                break;
            case "Grapple":
                progressGrapple(player);
                break;
            case "Leap":
                break;
            case "LeafShield":
                progressLeafShield(player);
                break;
            case "LeafDome":
                progressLeafDome(player);
                break;
            case "Regenerate":
                progressRegenerate(player, bending, now);
                break;
            case "RazorLeaf":
                break;
            default:
                break;
        }
    }

    /** Called from the click-cast path for click-type sub binds. */
    public void activateClickSub(ServerPlayer player, BendingPlayer bending, String sub, long now) {
        if (state != State.FORMED || active != null) {
            return;
        }
        if (bending.isOnCooldown(sub, now)) {
            long left = (bending.cooldownExpiresAt(sub) - now + 19) / 20;
            player.displayClientMessage(Component.literal(sub + " on cooldown (" + Math.max(left, 1) + "s)."), true);
            return;
        }
        switch (sub) {
            case "VineWhip":
                if (!spend(Config.VINEWHIP_COST.get())) {
                    return;
                }
                active = sub;
                whipRange = 0;
                whipForward = true;
                break;
            case "Tangle":
                if (!spend(Config.TANGLE_COST.get())) {
                    return;
                }
                active = sub;
                tanglePos = player.getEyePosition();
                tangleDir = player.getLookAngle().normalize().scale(0.8);
                break;
            case "Grapple":
                if (!spend(Config.GRAPPLE_COST.get())) {
                    return;
                }
                Vec3 target = targetedPoint(player, Config.GRAPPLE_RANGE.get());
                if (target == null) {
                    player.displayClientMessage(Component.literal("No latch in reach."), true);
                    regenerateCost(Config.GRAPPLE_COST.get());
                    return;
                }
                active = sub;
                grappleCurrent = player.getEyePosition();
                grappleTarget = target;
                grappleRange = 0;
                pulling = false;
                break;
            case "Leap":
                if (!player.onGround()) {
                    return;
                }
                if (!spend(Config.LEAP_COST.get())) {
                    return;
                }
                player.setDeltaMovement(player.getLookAngle()
                        .normalize()
                        .add(0.0, 0.8, 0.0)
                        .normalize()
                        .scale(Config.LEAP_POWER.get()));
                player.hurtMarked = true;
                bending.setCooldown(sub, now + Config.LEAP_COOLDOWN_TICKS.get());
                Vec3 ground = player.position();
                for (double i = 0; i < 1; i += 0.25) {
                    for (int ang = 0; ang < 360; ang += 15) {
                        double rad = Math.toRadians(ang);
                        double x = (1 - i) * Math.cos(rad);
                        double z = (1 - i) * Math.sin(rad);
                        level.sendParticles(LEAF, ground.x + x, ground.y + i, ground.z + z, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
                break;
            case "Disperse":
                state = State.DISPERSING;
                break;
            default:
                break;
        }
    }

    private void startSub(ServerPlayer player, BendingPlayer bending, String sub, long now) {
        if (bending.isOnCooldown(sub, now)) {
            long left = (bending.cooldownExpiresAt(sub) - now + 19) / 20;
            player.displayClientMessage(Component.literal(sub + " on cooldown (" + Math.max(left, 1) + "s)."), true);
            return;
        }
        switch (sub) {
            case "RazorLeaf":
                if (!spend(Config.RAZORLEAF_SUB_COST.get())) {
                    return;
                }
                BlockPos plant = findTargetPlant(player);
                bending.setCooldown(sub, now + Config.RAZORLEAF_COOLDOWN_TICKS.get());
                BendingManager.start(new RazorLeaf(player, plant, plant != null));
                active = sub;
                break;
            case "LeafShield":
                if (!spend(Config.LEAFSHIELD_COST.get())) {
                    return;
                }
                bending.setCooldown(sub, now + Config.LEAFSHIELD_COOLDOWN_TICKS.get());
                active = sub;
                break;
            case "LeafDome":
                if (!spend(Config.LEAFDOME_COST.get())) {
                    return;
                }
                bending.setCooldown(sub, now + Config.LEAFDOME_COOLDOWN_TICKS.get());
                active = sub;
                break;
            case "Regenerate":
                bending.setCooldown(sub, now + Config.REGENERATE_COOLDOWN_TICKS.get());
                active = sub;
                break;
            default:
                break;
        }
    }

    private void resetActive() {
        revertShield();
        revertDrained();
        whipRange = 0;
        whipForward = true;
        tanglePos = null;
        grappleCurrent = null;
        grappleTarget = null;
        pulling = false;
        active = null;
    }

    private String activeText(BendingPlayer bending, ServerPlayer player) {
        if (active != null) {
            return active;
        }
        String bound = bending.boundAbility(player.getInventory().selected + 1);
        return bound == null ? "PlantArmor" : bound;
    }

    private boolean spend(double amount) {
        if (durability < amount) {
            return false;
        }
        durability -= amount;
        return true;
    }

    private void regenerateCost(double amount) {
        durability += amount;
        if (durability > maxDurability) {
            durability = maxDurability;
        }
    }

    private void formShell(ServerPlayer player) {
        List<ItemStack> armor = player.getInventory().armor;
        for (ItemStack stack : armor) {
            savedArmor.add(stack.copy());
        }
        armor.set(0, leafLeather(Items.LEATHER_BOOTS));
        armor.set(1, leafLeather(Items.LEATHER_LEGGINGS));
        armor.set(2, leafLeather(Items.LEATHER_CHESTPLATE));
        armor.set(3, new ItemStack(Blocks.OAK_LEAVES));
        for (int slot = 1; slot <= 9; slot++) {
            BendingPlayer b = BendingPlayer.get(owner);
            if (b != null) {
                b.bind(slot, SUBS[slot - 1]);
            }
        }
    }

    private static ItemStack leafLeather(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        net.minecraft.world.item.component.DyedItemColor dyed =
                new net.minecraft.world.item.component.DyedItemColor(0x244A10, false);
        stack.set(net.minecraft.core.component.DataComponents.DYED_COLOR, dyed);
        return stack;
    }

    private void chantAura(ServerPlayer player) {
        Vec3 base = player.position();
        if (!sources.isEmpty()) {
            for (int y = 0; y < sources.size(); y++) {
                for (int angle = 0; angle < 360; angle += 15) {
                    double rad = Math.toRadians(angle);
                    double dy = ((double) y / requiredPlants) * player.getBbHeight();
                    level.sendParticles(
                            LEAF,
                            base.x + 0.5 * Math.cos(rad),
                            base.y + dy,
                            base.z + 0.5 * Math.sin(rad),
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0);
                }
            }
        }
    }

    private BlockPos randomPlantSource(ServerPlayer player) {
        List<BlockPos> plants = new ArrayList<>();
        BlockPos origin = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset((int) -selectRange, (int) -selectRange, (int) -selectRange),
                origin.offset((int) selectRange, (int) selectRange, (int) selectRange))) {
            if (BendingSources.isPlant(level, pos) && !sources.contains(pos)) {
                plants.add(pos.immutable());
            }
        }
        if (plants.isEmpty()) {
            return null;
        }
        return plants.get(player.getRandom().nextInt(plants.size()));
    }

    private BlockPos findTargetPlant(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= 7.0; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (BendingSources.isPlant(level, pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    private Vec3 targetedPoint(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (double d = 0.5; d <= range; d += 0.5) {
            Vec3 p = eye.add(look.scale(d));
            cursor.set(p.x, p.y, p.z);
            if (!level.getBlockState(cursor).isAir()) {
                return p;
            }
        }
        return null;
    }

    private void progressWhip(ServerPlayer player) {
        int speed = Config.VINEWHIP_SPEED.get();
        for (int s = 0; s < speed; s++) {
            if (whipForward) {
                whipRange++;
                if (whipRange >= Config.VINEWHIP_RANGE.get()) {
                    whipForward = false;
                }
            } else {
                whipRange--;
                if (whipRange <= 0) {
                    resetActive();
                    return;
                }
            }
            Vec3 origin = freshShoulderSide(player);
            Vec3 step = player.getLookAngle().normalize().scale(1.2);
            Vec3 cur = origin;
            for (int i = 0; i < Math.max(whipRange, 0); i++) {
                cur = cur.add(step);
                level.sendParticles(LEAF, cur.x, cur.y, cur.z, 2, 0.05, 0.05, 0.05, 0.0);
                BlockPos block = BlockPos.containing(cur);
                if (!level.getBlockState(block).isAir()) {
                    whipForward = false;
                    return;
                }
                for (LivingEntity hit : level.getEntitiesOfClass(LivingEntity.class, new AABB(cur, cur).inflate(1.0))) {
                    if (!hit.getUUID().equals(owner) && hit.isAlive()) {
                        hit.hurt(
                                player.damageSources().playerAttack(player),
                                Config.VINEWHIP_DAMAGE.get().floatValue());
                        whipForward = false;
                        BendingPlayer b = BendingPlayer.get(owner);
                        if (b != null) {
                            b.setCooldown("VineWhip", level.getGameTime() + Config.VINEWHIP_COOLDOWN_TICKS.get());
                        }
                        return;
                    }
                }
            }
        }
    }

    private void progressTangle(ServerPlayer player) {
        if (tanglePos == null || tanglePos.distanceTo(player.getEyePosition()) > Config.TANGLE_RANGE.get()) {
            resetActive();
            return;
        }
        tanglePos = tanglePos.add(tangleDir);
        if (!level.getBlockState(BlockPos.containing(tanglePos)).isAir()) {
            resetActive();
            return;
        }
        Vec3 dirTangle = tangleDir.normalize();
        for (int i = 0; i < 3; i++) {
            Vec3 off = orthogonal(dirTangle, angle + 120 * i, Config.TANGLE_RADIUS.get());
            level.sendParticles(
                    LEAF, tanglePos.x + off.x, tanglePos.y + off.y, tanglePos.z + off.z, 2, 0.05, 0.05, 0.05, 0.0);
        }
        level.sendParticles(LEAF, tanglePos.x, tanglePos.y, tanglePos.z, 4, 0.25, 0.25, 0.25, 0.0);
        angle = (angle + 30) % 360;
        for (LivingEntity hit : level.getEntitiesOfClass(
                LivingEntity.class, new AABB(tanglePos, tanglePos).inflate(Config.TANGLE_RADIUS.get()))) {
            if (!hit.getUUID().equals(owner) && hit.isAlive()) {
                TangleStop.stop(hit, level, Config.TANGLE_DURATION_TICKS.get());
                hit.hurt(player.damageSources().playerAttack(player), 0.0F);
                BendingPlayer b = BendingPlayer.get(owner);
                if (b != null) {
                    b.setCooldown("Tangle", level.getGameTime() + Config.TANGLE_COOLDOWN_TICKS.get());
                }
                resetActive();
                return;
            }
        }
    }

    private void progressGrapple(ServerPlayer player) {
        if (grappleCurrent == null || grappleTarget == null) {
            resetActive();
            return;
        }
        Vec3 origin = freshShoulderSide(player);
        Vec3 toTarget = grappleTarget.subtract(origin);
        double toTargetLen = toTarget.length();
        Vec3 stepDir = toTarget.normalize();
        if (!pulling) {
            // Grow the vine one block per tick from the shoulder toward the
            // latch, redrawing its full length (Korra: current advances
            // one step along direction each iteration, particles for each).
            for (int i = 0; i < grappleRange; i++) {
                Vec3 cur = origin.add(stepDir.scale(i));
                BlockPos at = BlockPos.containing(cur);
                level.sendParticles(LEAF, cur.x, cur.y, cur.z, 2, 0.05, 0.05, 0.05, 0.0);
                if (!level.getBlockState(at).getCollisionShape(level, at).isEmpty()) {
                    if (cur.distanceTo(grappleTarget) < 1.0) {
                        pulling = true;
                    } else {
                        resetActive();
                        return;
                    }
                }
            }
            grappleRange++;
            if (grappleRange > Config.GRAPPLE_RANGE.get()) {
                resetActive();
                return;
            }
        } else {
            // Latched: reel the bender toward the target; stop at its feet.
            if (player.position().distanceTo(grappleTarget) < 2.0) {
                BendingPlayer b = BendingPlayer.get(owner);
                if (b != null) {
                    b.setCooldown("Grapple", level.getGameTime() + Config.GRAPPLE_COOLDOWN_TICKS.get());
                }
                resetActive();
                return;
            }
            Vec3 pull =
                    grappleTarget.subtract(player.getEyePosition()).normalize().scale(Config.GRAPPLE_SPEED.get());
            player.setDeltaMovement(pull);
            player.hurtMarked = true;
            level.sendParticles(LEAF, origin.x, origin.y, origin.z, 4, 0.1, 0.1, 0.1, 0.0);
        }
    }

    private Vec3 side(ServerPlayer player) {
        double yaw = Math.toRadians(player.getYRot());
        return new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw))
                .cross(new Vec3(0, 1, 0))
                .normalize();
    }

    private Vec3 freshShoulderSide(ServerPlayer player) {
        // Korra: getRightSide(player.getLocation().add(0, 1, 0), 0.45)
        return player.position().add(0.0, 1.0, 0.0).add(side(player).scale(0.45));
    }

    private void progressLeafShield(ServerPlayer player) {
        revertShield();
        Vec3 dirLeaf = player.getLookAngle().normalize();
        Vec3 center = player.getEyePosition().add(dirLeaf.scale(3.5));
        int radius = Config.LEAFSHIELD_RADIUS.get();
        for (int i = 1; i <= radius; i++) {
            int steps = Math.max(i * 9, 9);
            for (double a = 0; a < 360; a += 360.0 / steps) {
                Vec3 off = orthogonal(dirLeaf, a, i);
                placeLeaf(BlockPos.containing(center.x + off.x, center.y + off.y, center.z + off.z));
            }
        }
        placeLeaf(BlockPos.containing(center));
    }

    private void progressLeafDome(ServerPlayer player) {
        revertShield();
        BlockPos c = player.blockPosition();
        int r = Config.LEAFDOME_RADIUS.get();
        Set<BlockPos> shell = new HashSet<>();
        for (double theta = 0; theta < Math.PI; theta += Math.PI / 8) {
            for (double phi = 0; phi < 2 * Math.PI; phi += Math.PI / 8) {
                Vec3 p = c.getCenter()
                        .add(
                                r * Math.sin(theta) * Math.cos(phi),
                                r * Math.cos(theta),
                                r * Math.sin(theta) * Math.sin(phi));
                shell.add(BlockPos.containing(p));
            }
        }
        // Leaves take priority: free shell cells from last tick's drain
        // claims first, otherwise placeLeaf skips them as temps and the
        // shell never appears underwater.
        for (BlockPos s : shell) {
            TempBlock claimed = drained.get(s);
            if (claimed != null) {
                claimed.revert();
                drained.remove(s);
            }
        }
        for (BlockPos s : shell) {
            placeLeaf(s);
        }
        syncDomeWater(c, r);
    }

    /**
     * Hold water out of the dome exactly like WaterBubble holds its pocket:
     * drained cells persist (no per-tick revert flicker), water that flows
     * back under a live claim is re-cleared in place, and scheduled fluid
     * ticks around the dome are starved so neighbours stop pouring in.
     * Everything restores when the dome drops.
     */
    private void syncDomeWater(BlockPos c, int r) {
        Set<BlockPos> want = new HashSet<>();
        Vec3 center = c.getCenter();
        // Full shell radius: leaves go down first and are skipped as temps,
        // so this also drains the band between the old inner sphere and the
        // shell plus every gap in the leaf ring.
        int in = r;
        for (int y = -in; y <= in; y++) {
            for (int x = -in; x <= in; x++) {
                for (int z = -in; z <= in; z++) {
                    if (x * x + y * y + z * z > in * in) {
                        continue;
                    }
                    BlockPos pos = c.offset(x, y, z);
                    TempBlock tracked = drained.get(pos);
                    if (tracked != null && !tracked.isReverted()) {
                        want.add(pos.immutable());
                        BlockState current = level.getBlockState(pos);
                        if (!current.getFluidState().isEmpty()
                                && !(current.hasProperty(BlockStateProperties.WATERLOGGED)
                                        && current.getValue(BlockStateProperties.WATERLOGGED))) {
                            level.setBlock(pos.immutable(), Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
                            level.getFluidTicks()
                                    .clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(
                                            pos.immutable()));
                        }
                        continue;
                    }
                    if (tracked != null) {
                        drained.remove(pos);
                    }
                    BlockState state = level.getBlockState(pos);
                    boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
                            && state.getValue(BlockStateProperties.WATERLOGGED);
                    if (state.getFluidState().isEmpty() && !waterlogged) {
                        continue;
                    }
                    want.add(pos.immutable());
                }
            }
        }
        for (Map.Entry<BlockPos, TempBlock> entry : new ArrayList<>(drained.entrySet())) {
            BlockPos tracked = entry.getKey();
            double dx = tracked.getX() + 0.5 - center.x;
            double dy = tracked.getY() + 0.5 - center.y;
            double dz = tracked.getZ() + 0.5 - center.z;
            double keep = in + 1;
            if (!want.contains(tracked) && dx * dx + dy * dy + dz * dz > keep * keep) {
                entry.getValue().revert();
                drained.remove(tracked);
            }
        }
        for (BlockPos pos : want) {
            if (drained.containsKey(pos) || TempBlock.isTemp(level, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            BlockState dry = state;
            if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                    && state.getValue(BlockStateProperties.WATERLOGGED)) {
                dry = state.setValue(BlockStateProperties.WATERLOGGED, false);
            } else {
                dry = Blocks.AIR.defaultBlockState();
            }
            drained.put(pos, new TempBlock(level, pos, dry, TempBlock.QUIET));
        }
        int halo = r + 1;
        level.getFluidTicks()
                .clearArea(new net.minecraft.world.level.levelgen.structure.BoundingBox(
                        c.getX() - halo,
                        c.getY() - halo,
                        c.getZ() - halo,
                        c.getX() + halo,
                        c.getY() + halo,
                        c.getZ() + halo));
    }

    private void revertDrained() {
        for (TempBlock temp : new ArrayList<>(drained.values())) {
            temp.revert();
        }
        drained.clear();
    }

    private void placeLeaf(BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        boolean vegetation = !current.isAir() && current.getFluidState().isEmpty() && current.canBeReplaced();
        if (!current.isAir() && !vegetation && current.getFluidState().isEmpty()) {
            return;
        }
        if (TempBlock.isTemp(level, pos)) {
            return;
        }
        shield.add(new TempBlock(level, pos, Blocks.OAK_LEAVES.defaultBlockState(), TempBlock.QUIET));
    }

    private Vec3 orthogonal(Vec3 d, double deg, double distance) {
        Vec3 axis = Math.abs(d.y) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 u = axis.cross(d).normalize();
        Vec3 v = d.cross(u).normalize();
        double t = Math.toRadians(deg);
        return u.scale(Math.cos(t) * distance).add(v.scale(Math.sin(t) * distance));
    }

    private void revertShield() {
        for (TempBlock temp : shield) {
            temp.revert();
        }
        shield.clear();
    }

    private void progressRegenerate(ServerPlayer player, BendingPlayer bending, long now) {
        if (durability >= maxDurability) {
            resetActive();
            return;
        }
        if (bending.isOnCooldown("PlantArmorRegenTick", now)) {
            return;
        }
        BlockPos plant = randomPlantSource(player);
        if (plant != null) {
            consumePlantSourceFor(plant, level);
            bending.setCooldown("PlantArmorRegenTick", now + 20L);
            durability = Math.min(durability + Config.REGENERATE_AMOUNT.get(), maxDurability);
        }
    }

    @Override
    public void onRemove() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        BendingPlayer bending = BendingPlayer.get(owner);
        if (player != null) {
            List<ItemStack> armor = player.getInventory().armor;
            for (int i = 0; i < savedArmor.size(); i++) {
                armor.set(i, savedArmor.get(i));
            }
        }
        if (bending != null) {
            for (Map.Entry<Integer, String> e : savedSlots.entrySet()) {
                bending.bind(e.getKey(), e.getValue());
            }
            bending.setCooldown(ID, level.getGameTime() + Config.PLANTARMOR_COOLDOWN_TICKS.get());
        }
        revertShield();
        revertDrained();
    }

    /** Plant consumed for a formed shell stays bent briefly (Korra TempBlock). */
    public static void consumePlantSourceFor(BlockPos pos, ServerLevel level) {
        BendingManager.consumePlantSource(level, pos, Config.WATERMANIP_PLANT_REGROW_SECONDS.get());
    }

    public static boolean isSubBind(String sub) {
        for (String s : SUBS) {
            if (s.equalsIgnoreCase(sub)) {
                return true;
            }
        }
        return false;
    }
}
