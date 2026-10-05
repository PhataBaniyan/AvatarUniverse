package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code WaterArms} (manager only). Grows two water arms
 * (shoulder side-offset, two joints, then forward along the look) that
 * diff-update every tick like the spout column — held cells are never
 * re-placed, so the arms read solid instead of strobing. Snapshot hotbar
 * active, and count down uses. Sub-abilities: Pull/Punch/Grapple/Grab on
 * slots 1-4, Spear on 5. Sneak + double-click removes.
 *
 * <p>Every arm cell is a quiet {@link TempBlock} with fluid ticks cleared;
 * all revert on removal. Lightning interaction, day/night scaling,
 * permissions and region hooks are out of scope (see skill section 9).</p>
 */
public class WaterArms extends BendingAbility {
    public static final String ID = "WaterArms";

    public enum Arm {
        LEFT,
        RIGHT
    }

    /** Sub-ability IDs shown on slots 1-6 while arms are active (Korra multiability). */
    public static final String[] SUB_BINDS = {"Pull", "Punch", "Grapple", "Grab", "Spear"};

    private final ServerLevel level;
    private final List<TempBlock> right = new ArrayList<>();
    private final List<TempBlock> left = new ArrayList<>();
    private final boolean fullSource;
    private boolean leftConsumed;
    private boolean rightConsumed;
    private boolean leftCooldown;
    private boolean rightCooldown;
    private int maxPunches;
    private int maxUses;
    private long lastClickGameTime = Long.MIN_VALUE;
    private Arm activeArm = Arm.RIGHT;
    private final Map<Integer, String> savedSlots = new HashMap<>();

    public WaterArms(ServerPlayer player, boolean fullSource) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.fullSource = fullSource;
        this.maxPunches = Config.WATERARMS_MAX_PUNCHES.get();
        this.maxUses = Config.WATERARMS_MAX_USES.get();
        BendingPlayer bending = BendingPlayer.getOrCreate(owner);
        for (int slot = 1; slot <= 5; slot++) {
            savedSlots.put(slot, bending.boundAbility(slot));
            bending.bind(slot, SUB_BINDS[slot - 1]);
        }
        player.getInventory().selected = 0;
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
                || !bending.hasElement(com.phatabaniyan.avataruniverse.bending.BendingElement.WATER)
                || !bending.isToggled()) {
            return false;
        }
        if (maxPunches == 0 || maxUses == 0 || (leftConsumed && rightConsumed)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("WaterArms collapsed - re-cast to regrow."), true);
            return false;
        }
        // Diff-update (Korra skips already-bent cells): only cells entering
        // or leaving an arm are touched, so held water never strobes.
        syncArm(right, computeRightArm(player));
        syncArm(left, computeLeftArm(player));
        // Own arm water slows swimming like any water: counter with a quiet
        // Dolphin's Grace refresh (lapses on its own ~1.5s after arms end).
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE, 30, 0, false, false, false));
        return true;
    }

    private void syncArm(List<TempBlock> held, List<BlockPos> want) {
        java.util.Set<BlockPos> wantSet = new java.util.HashSet<>(want);
        for (TempBlock temp : new ArrayList<>(held)) {
            if (!wantSet.contains(temp.pos())) {
                temp.revert();
                held.remove(temp);
            }
        }
        java.util.Set<BlockPos> heldSet = new java.util.HashSet<>();
        for (TempBlock temp : held) {
            heldSet.add(temp.pos());
        }
        for (BlockPos pos : want) {
            if (!heldSet.contains(pos)) {
                held.add(new TempBlock(level, pos, Blocks.WATER.defaultBlockState(), TempBlock.QUIET));
                heldSet.add(pos);
            }
        }
    }

    private void revertArms() {
        for (TempBlock temp : new ArrayList<>(right)) {
            temp.revert();
        }
        right.clear();
        for (TempBlock temp : new ArrayList<>(left)) {
            temp.revert();
        }
        left.clear();
    }

    /** Side vector: +side is left, -side is right (Korra getLeftSide/getRightSide). */
    static Vec3 sideVec(ServerPlayer player) {
        double yawRad = Math.toRadians(player.getYRot());
        return new Vec3(Math.cos(yawRad), 0.0, Math.sin(yawRad)).normalize();
    }

    /** First non-air block along the look ray, else the max-range point (Korra targeted location). */
    public static Vec3 aimedPoint(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        ServerLevel level = player.serverLevel();
        for (double d = 0.5; d <= range; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (!level.getBlockState(pos).isAir()) {
                return Vec3.atCenterOf(pos);
            }
        }
        return eye.add(look.scale(range));
    }

    private boolean canPlaceBlock(BlockPos pos) {
        return BendingSources.isTransparentForBend(level, pos);
    }

    /** Add a cell plus its outward lateral neighbor (2-wide arms). */
    private void addWide(List<BlockPos> built, BlockPos pos, Vec3 outward) {
        built.add(pos);
        Vec3 flat = new Vec3(outward.x, 0.0, outward.z);
        if (flat.lengthSqr() < 1.0e-6) {
            return;
        }
        BlockPos extra = Math.abs(flat.x) >= Math.abs(flat.z)
                ? pos.offset(flat.x >= 0.0 ? 1 : -1, 0, 0)
                : pos.offset(0, 0, flat.z >= 0.0 ? 1 : -1);
        if (!extra.equals(pos) && !built.contains(extra) && canPlaceBlock(extra)) {
            built.add(extra);
        }
    }

    /** Right arm: shoulder at side -1, joint at -2, then forward. Partial on block. */
    private List<BlockPos> computeRightArm(ServerPlayer player) {
        List<BlockPos> built = new ArrayList<>();
        if (rightConsumed) {
            return built;
        }
        Vec3 feet = player.position();
        Vec3 side = sideVec(player);
        Vec3 look = player.getLookAngle().normalize();
        BlockPos r1 = BlockPos.containing(feet.subtract(side.scale(1.0)).add(0.0, 1.5, 0.0));
        if (!canPlaceBlock(r1)) {
            return built;
        }
        BlockPos hand = BlockPos.containing(feet.subtract(side.scale(0.34)).add(0.0, 1.5, 0.0));
        if (!hand.equals(r1)) {
            addWide(built, r1, side.scale(-1.0));
        }
        BlockPos r2 = BlockPos.containing(feet.subtract(side.scale(2.0)).add(0.0, 1.5, 0.0));
        if (!canPlaceBlock(r2) || !canPlaceBlock(r1)) {
            return built;
        }
        addWide(built, r2, side.scale(-1.0));
        for (int j = 1; j <= Config.WATERARMS_INITIAL_LENGTH.get(); j++) {
            BlockPos pos =
                    BlockPos.containing(new Vec3(r2.getX() + 0.5, r2.getY() + 0.5, r2.getZ() + 0.5).add(look.scale(j)));
            if (!canPlaceBlock(pos) || !canPlaceBlock(r2) || !canPlaceBlock(r1)) {
                return built;
            }
            addWide(built, pos, side.scale(-1.0));
        }
        return built;
    }

    /** Left arm: mirror of the right (Korra displayLeftArm). */
    private List<BlockPos> computeLeftArm(ServerPlayer player) {
        List<BlockPos> built = new ArrayList<>();
        if (leftConsumed) {
            return built;
        }
        Vec3 feet = player.position();
        Vec3 side = sideVec(player);
        Vec3 look = player.getLookAngle().normalize();
        BlockPos l1 = BlockPos.containing(feet.add(side.scale(1.0)).add(0.0, 1.5, 0.0));
        if (!canPlaceBlock(l1)) {
            return built;
        }
        BlockPos hand = BlockPos.containing(feet.add(side.scale(0.34)).add(0.0, 1.5, 0.0));
        if (!hand.equals(l1)) {
            addWide(built, l1, side.scale(1.0));
        }
        BlockPos l2 = BlockPos.containing(feet.add(side.scale(2.0)).add(0.0, 1.5, 0.0));
        if (!canPlaceBlock(l2) || !canPlaceBlock(l1)) {
            return built;
        }
        addWide(built, l2, side.scale(1.0));
        for (int j = 1; j <= Config.WATERARMS_INITIAL_LENGTH.get(); j++) {
            BlockPos pos =
                    BlockPos.containing(new Vec3(l2.getX() + 0.5, l2.getY() + 0.5, l2.getZ() + 0.5).add(look.scale(j)));
            if (!canPlaceBlock(pos) || !canPlaceBlock(l2) || !canPlaceBlock(l1)) {
                return built;
            }
            addWide(built, pos, side.scale(1.0));
        }
        return built;
    }

    /** Theoretical full-length tip (Korra getRightArmEnd), for P2/P3 targeting. */
    public Vec3 getRightArmEnd(ServerPlayer player) {
        Vec3 feet = player.position();
        Vec3 side = sideVec(player);
        Vec3 look = player.getLookAngle().normalize();
        return feet.subtract(side.scale(2.0)).add(0.0, 1.5, 0.0).add(look.scale(Config.WATERARMS_INITIAL_LENGTH.get()));
    }

    /** Theoretical full-length tip (Korra getLeftArmEnd), for P2/P3 targeting. */
    public Vec3 getLeftArmEnd(ServerPlayer player) {
        Vec3 feet = player.position();
        Vec3 side = sideVec(player);
        Vec3 look = player.getLookAngle().normalize();
        return feet.add(side.scale(2.0)).add(0.0, 1.5, 0.0).add(look.scale(Config.WATERARMS_INITIAL_LENGTH.get()));
    }

    public Vec3 getActiveArmEnd(ServerPlayer player) {
        return activeArm == Arm.LEFT ? getLeftArmEnd(player) : getRightArmEnd(player);
    }

    /** First displayable arm wins, preferring the toggled side (Korra switchPreferredArm). */
    public Arm switchPreferredArm(ServerPlayer player) {
        switchActiveArm();
        if (activeArm == Arm.LEFT) {
            if (computeLeftArm(player).isEmpty()) {
                switchActiveArm();
            }
        }
        if (activeArm == Arm.RIGHT) {
            if (computeRightArm(player).isEmpty()) {
                switchActiveArm();
            }
        }
        return activeArm;
    }

    public void switchActiveArm() {
        activeArm = activeArm == Arm.LEFT ? Arm.RIGHT : Arm.LEFT;
    }

    public boolean canDisplayActiveArm(ServerPlayer player) {
        return activeArm == Arm.LEFT
                ? !computeLeftArm(player).isEmpty()
                : !computeRightArm(player).isEmpty();
    }

    public Arm getActiveArm() {
        return activeArm;
    }

    public boolean isFullSource() {
        return fullSource;
    }

    public void consumeArm(Arm arm) {
        if (arm == Arm.LEFT) {
            leftConsumed = true;
        } else {
            rightConsumed = true;
        }
    }

    public boolean isArmConsumed(Arm arm) {
        return arm == Arm.LEFT ? leftConsumed : rightConsumed;
    }

    public boolean isLeftArmCooldown() {
        return leftCooldown;
    }

    public boolean isRightArmCooldown() {
        return rightCooldown;
    }

    public void setLeftArmCooldown(boolean cooldown) {
        leftCooldown = cooldown;
    }

    public void setRightArmCooldown(boolean cooldown) {
        rightCooldown = cooldown;
    }

    public void setActiveArmCooldown(boolean cooldown) {
        if (activeArm == Arm.LEFT) {
            setLeftArmCooldown(cooldown);
        } else {
            setRightArmCooldown(cooldown);
        }
    }

    public int getMaxPunches() {
        return maxPunches;
    }

    public void decrementMaxPunches() {
        maxPunches = Math.max(0, maxPunches - 1);
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void decrementMaxUses() {
        maxUses = Math.max(0, maxUses - 1);
    }

    /**
     * Sneak double-click removal (Korra prepareCancel): second sneak-click
     * within 10 ticks removes; the first arms the timer and names the slot.
     * Returns true when removed.
     */
    public boolean prepareCancel(ServerPlayer player, BendingPlayer bending, long gameTime) {
        if (gameTime < lastClickGameTime + 10) {
            return true;
        }
        lastClickGameTime = gameTime;
        String sub = bending.boundAbility(player.getInventory().selected + 1);
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("Active Ability: " + (sub == null ? ID : sub)), true);
        return false;
    }

    @Override
    public void onRemove() {
        revertArms();
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            for (int slot = 1; slot <= 5; slot++) {
                bending.bind(slot, savedSlots.get(slot));
            }
            bending.setCooldown(ID, level.getGameTime() + Config.WATERARMS_COOLDOWN_TICKS.get());
        }
    }
}
