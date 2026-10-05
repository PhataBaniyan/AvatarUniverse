package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code EarthGrab}: left-click sends power through the
 * ground that reaches up and roots the first living thing it touches, while
 * sneaking drags loose items and arrows across earth toward you and harvests
 * ripe crops. The trap is an invisible small armor stand wearing the ground
 * (3 HP, hurt by punching or right-clicking it every 400ms); taking 4 damage
 * also breaks the victim free. Reference values: Cooldown 5000ms, Range 14,
 * DragSpeed 0.8, TrapHitInterval 400ms, TrapHP 3, DamageThreshold 4.
 */
public class EarthGrab extends EarthAbility {
    public static final String ID = "EarthGrab";
    /** Tag stamped on the trap armor stand so anyone can punch it free. */
    public static final String TRAP_TAG = "avataruniverse_earthgrab_trap";

    /** Reference Cooldown 5000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.EARTHGRAB_COOLDOWN_MS.get());

    private static final double RANGE = Config.EARTHGRAB_RANGE.get();
    private static final double DRAG_SPEED = Config.EARTHGRAB_DRAG_SPEED.get();
    private static final long HIT_INTERVAL_TICKS = Config.msToTicks(Config.EARTHGRAB_HIT_INTERVAL_MS.get());
    private static final int TRAP_HP = Config.EARTHGRAB_TRAP_HP.get();
    private static final float DAMAGE_THRESHOLD =
            Config.EARTHGRAB_DAMAGE_THRESHOLD.get().floatValue();
    private static final int SLOW_TICKS = Config.msToTicks(Config.EARTHGRAB_SLOW_MS.get());
    private static final int SLOW_AMPLIFIER = Config.EARTHGRAB_SLOW_AMPLIFIER.get();
    private static final double TRAP_LEASH = Config.EARTHGRAB_TRAP_LEASH.get();

    public enum GrabMode {
        TRAP,
        DRAG,
        PROJECTING
    }

    private GrabMode mode;
    private Vec3 origin;
    private Vec3 direction;
    private UUID targetId;
    private float trappedHp;
    private int trapHp = TRAP_HP;
    private long lastHitTick;
    private boolean initiated;
    private UUID standId;
    private ItemStack savedBoots = ItemStack.EMPTY;
    private ItemStack savedLegs = ItemStack.EMPTY;
    private boolean armored;
    private int slowTick;

    public EarthGrab(ServerPlayer player, GrabMode mode) {
        super(player);
        this.mode = mode;
        this.origin = player.position();
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        this.direction = flat.lengthSqr() < 1.0e-6 ? new Vec3(0, 0, 1) : flat.normalize();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Earth underfoot, like the reference constructor gate. */
    public static boolean canBegin(ServerPlayer player) {
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null || !bending.hasElement(BendingElement.EARTH) || !bending.isToggled()) {
            return false;
        }
        return Accretion.isEarthbendable(
                player.serverLevel(), player.blockPosition().below());
    }

    public GrabMode mode() {
        return mode;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        LivingEntity target = target();
        if (target != null && !target.isAlive()) {
            return false;
        }
        switch (mode) {
            case PROJECTING:
                project();
                return true;
            case TRAP:
                return trap(target);
            case DRAG:
                return drag();
            default:
                return false;
        }
    }

    /** Wave the grab forward one block through the ground, catching a victim. */
    private void project() {
        origin = origin.add(direction);
        if (origin.distanceTo(player.position()) > RANGE) {
            BendingManager.remove(this);
            return;
        }
        BlockPos base = BlockPos.containing(origin);
        BlockPos top = null;
        for (int dy = 2; dy >= -2; dy--) {
            BlockPos candidate = base.offset(0, dy, 0);
            if (level.getBlockState(candidate).isSolid() && isTransparent(candidate.above())) {
                top = candidate.immutable();
                break;
            }
        }
        if (top == null) {
            BendingManager.remove(this);
            return;
        }
        if (!Accretion.isEarthbendable(level, top)) {
            if (!isTransparent(top) || !Accretion.isEarthbendable(level, top.below())) {
                BendingManager.remove(this);
                return;
            }
            top = top.below().immutable();
        }
        origin = new Vec3(origin.x, top.getY() + 1.0, origin.z);
        BlockState ground = level.getBlockState(top);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, ground),
                origin.x,
                origin.y,
                origin.z,
                6,
                0.2,
                0.5,
                0.2,
                0.0);
        playBreak(ground, origin);
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(origin.subtract(1.0, 1.5, 1.0), origin.add(1.0, 1.5, 1.0)),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            if (!Accretion.isEarthbendable(level, entity.blockPosition().below())) {
                continue;
            }
            targetId = entity.getUUID();
            trappedHp = entity.getHealth();
            mode = GrabMode.TRAP;
            origin = entity.position();
            return;
        }
    }

    /** Hold the victim still until the trap breaks. */
    private boolean trap(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (!initiated) {
            BlockPos below = target.blockPosition().below();
            BlockState ground = level.getBlockState(below);
            ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, level);
            stand.setPos(target.getX(), target.getY(), target.getZ());
            stand.setInvisible(true);
            net.minecraft.nbt.CompoundTag small = new net.minecraft.nbt.CompoundTag();
            small.putBoolean("Invisible", true);
            small.putBoolean("Small", true);
            stand.readAdditionalSaveData(small);
            stand.setHealth(TRAP_HP);
            stand.addTag(TRAP_TAG);
            stand.getPersistentData().putUUID("earthgrab_owner", owner);
            if (ground.getBlock().asItem() != Items.AIR) {
                stand.setItemSlot(
                        EquipmentSlot.HEAD, new ItemStack(ground.getBlock().asItem()));
            }
            level.addFreshEntity(stand);
            standId = stand.getUUID();
            dress(target, ground, below);
            playBreak(ground, target.position());
            initiated = true;
        }
        ArmorStand stand = stand();
        if (stand == null || !stand.isAlive()) {
            return false;
        }
        BlockState ground = level.getBlockState(target.blockPosition().below());
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, ground),
                target.getX(),
                target.getY() + 0.5,
                target.getZ(),
                4,
                0.3,
                0.6,
                0.3,
                0.0);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.resetFallDistance();
        // Slowness 6 freezes without the inverted-speed slide of higher
        // amplifiers, and the anchor snap below pins client-side walking in
        // every horizontal direction (server motion alone cannot stop it).
        target.addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, SLOW_AMPLIFIER, false, false, false));
        if (target instanceof ServerPlayer victim) {
            victim.displayClientMessage(Component.literal("* Trapped *"), true);
        }
        if (!Accretion.isEarthbendable(level, target.blockPosition().below())) {
            return false;
        }
        if (stand.position().distanceTo(target.position()) > TRAP_LEASH) {
            return false;
        }
        Vec3 anchor = stand.position();
        double pinX = target.getX() - anchor.x;
        double pinZ = target.getZ() - anchor.z;
        if (pinX * pinX + pinZ * pinZ > 0.35 * 0.35) {
            target.teleportTo(anchor.x, target.getY(), anchor.z);
            target.setDeltaMovement(Vec3.ZERO);
            target.hurtMarked = true;
        }
        if (trappedHp - target.getHealth() >= DAMAGE_THRESHOLD) {
            return false;
        }
        if (trapHp <= 0) {
            return false;
        }
        if (player.position().distanceTo(target.position()) > RANGE) {
            return false;
        }
        if (!level.getBlockState(target.blockPosition().below()).isSolid()) {
            return false;
        }
        return !level.getBlockState(target.blockPosition()).isSolid();
    }

    /** Sneak-hold: harvest ripe crops, drag items and arrows across earth. */
    private boolean drag() {
        if (!player.onGround()) {
            return true;
        }
        if (!player.isShiftKeyDown()) {
            return false;
        }
        if (++slowTick % 10 == 0) {
            harvest();
        }
        boolean pulled = false;
        Vec3 center = player.position();
        for (net.minecraft.world.entity.Entity entity : level.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class,
                new AABB(center.subtract(RANGE, RANGE, RANGE), center.add(RANGE, RANGE, RANGE)),
                net.minecraft.world.entity.Entity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            if (entity instanceof ThrownTrident) {
                continue;
            }
            BlockPos below = entity.blockPosition().below();
            BlockState floor = level.getBlockState(below);
            boolean farmland = floor.is(Blocks.FARMLAND);
            if (!farmland && !Accretion.isEarthbendable(level, below)) {
                continue;
            }
            if (entity instanceof AbstractArrow arrow) {
                if (arrow.pickup != AbstractArrow.Pickup.ALLOWED) {
                    continue;
                }
                ItemEntity drop =
                        new ItemEntity(level, arrow.getX(), arrow.getY(), arrow.getZ(), new ItemStack(Items.ARROW, 1));
                level.addFreshEntity(drop);
                arrow.discard();
                entity = drop;
            } else if (!(entity instanceof ItemEntity)) {
                continue;
            }
            Vec3 pull = center.subtract(entity.position()).normalize().scale(DRAG_SPEED);
            entity.setDeltaMovement(pull);
            entity.hurtMarked = true;
            pulled = true;
        }
        if (pulled) {
            level.sendParticles(
                    BendingTheme.particle(Config.EARTHGRAB_DRAG_PARTICLE.get(), ParticleTypes.POOF),
                    center.x,
                    center.y + 0.5,
                    center.z,
                    Config.EARTHGRAB_DRAG_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.0);
        }
        return true;
    }

    /** Break mature crops in range, like the reference circle sweep. */
    private void harvest() {
        BlockPos center = player.blockPosition();
        int radius = (int) Math.floor(RANGE);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof CropBlock crop) {
                        if (crop.isMaxAge(state)) {
                            level.destroyBlock(pos, true);
                        }
                    } else if (state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN)) {
                        level.destroyBlock(pos, true);
                    }
                }
            }
        }
    }

    /** One trap-HP chip per interval when anyone works the stand. */
    public void damageTrap() {
        long now = level.getGameTime();
        if (now - lastHitTick < HIT_INTERVAL_TICKS) {
            return;
        }
        lastHitTick = now;
        trapHp--;
        ArmorStand stand = stand();
        LivingEntity target = target();
        if (stand != null) {
            stand.setHealth(Math.max(0.5F, stand.getHealth() - 1.0F));
        }
        Vec3 at = stand != null ? stand.position().add(0, 1, 0) : player.position();
        BlockState ground = level.getBlockState(BlockPos.containing(at).below());
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y, at.z, 7, 0.06, 0.3, 0.06, 0.0);
        playBreak(ground, at);
        if (trapHp <= 0 && target != null) {
            trappedHp = Math.min(trappedHp, target.getHealth() + DAMAGE_THRESHOLD);
        }
    }

    /** Tint spare leg slots to the ground, like the reference TempArmor. */
    private void dress(LivingEntity target, BlockState ground, BlockPos pos) {
        int color = ground.getMapColor(level, pos).col;
        ItemStack legs = target.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = target.getItemBySlot(EquipmentSlot.FEET);
        if (!legs.isEmpty() || !boots.isEmpty()) {
            return;
        }
        savedLegs = legs.copy();
        savedBoots = boots.copy();
        ItemStack tintedLegs = new ItemStack(Items.LEATHER_LEGGINGS);
        ItemStack tintedBoots = new ItemStack(Items.LEATHER_BOOTS);
        tintedLegs.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
        tintedBoots.set(DataComponents.DYED_COLOR, new DyedItemColor(color, false));
        target.setItemSlot(EquipmentSlot.LEGS, tintedLegs);
        target.setItemSlot(EquipmentSlot.FEET, tintedBoots);
        armored = true;
    }

    private void undress(LivingEntity target) {
        if (!armored) {
            return;
        }
        target.setItemSlot(EquipmentSlot.LEGS, savedLegs);
        target.setItemSlot(EquipmentSlot.FEET, savedBoots);
        armored = false;
    }

    private LivingEntity target() {
        if (targetId == null) {
            return null;
        }
        if (level.getEntity(targetId) instanceof LivingEntity target) {
            return target;
        }
        return null;
    }

    private ArmorStand stand() {
        if (standId == null) {
            return null;
        }
        if (level.getEntity(standId) instanceof ArmorStand stand) {
            return stand;
        }
        return null;
    }

    private void playBreak(BlockState state, Vec3 at) {
        level.playSound(
                null,
                at.x,
                at.y,
                at.z,
                state.getSoundType().getBreakSound(),
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.4F,
                0.8F);
    }

    @Override
    public void onRemove() {
        LivingEntity target = target();
        if (mode == GrabMode.TRAP && initiated) {
            ArmorStand stand = stand();
            if (stand != null) {
                stand.discard();
            }
            if (target != null) {
                undress(target);
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(ID, level.getGameTime() + COOLDOWN_TICKS);
        }
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }
}
