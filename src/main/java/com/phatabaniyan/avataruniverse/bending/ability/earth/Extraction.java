package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code Extraction} (metal)
 * (core/.../earthbending/metal/Extraction.java from the local
 * ProjectKorra-master copy). Tap sneak looking at an ore within 5 blocks:
 * the ore turns to its bare rock and the raw material drops at your feet,
 * with 30% double and 10% triple chances. Sneaking at an iron golem
 * instead shakes loose an iron nugget and deals 4 damage. Temp blocks are
 * never touched (reference anti-dupe). Cooldown 500ms. Reference gates on
 * metalbending; here any earthbender may use it.
 */
public class Extraction extends EarthAbility {
    public static final String ID = "Extraction";

    private static final double SELECT_RANGE = Config.EXTRACTION_SELECT_RANGE.get();
    /** Reference Cooldown 500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.EXTRACTION_COOLDOWN_MS.get());

    private static final double DOUBLE_CHANCE = Config.EXTRACTION_DOUBLE_CHANCE.get();
    private static final double TRIPLE_CHANCE = Config.EXTRACTION_TRIPLE_CHANCE.get();
    private static final int GOLEM_DROPS = Config.EXTRACTION_GOLEM_DROPS.get();
    private static final float GOLEM_DAMAGE =
            Config.EXTRACTION_GOLEM_DAMAGE.get().floatValue();

    private boolean done;

    public Extraction(ServerPlayer player) {
        super(player);
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: a golem or an extractable ore in sight. */
    public static boolean canBegin(ServerPlayer player) {
        if (golemInSight(player) != null) {
            return true;
        }
        BlockPos target = eyeTarget(player, SELECT_RANGE);
        return target != null
                && !TempBlock.isTemp(player.serverLevel(), target)
                && isOre(player.serverLevel().getBlockState(target));
    }

    /** Any of the nine extractable ores/gilded blocks. */
    private static boolean isOre(BlockState state) {
        return state.is(Blocks.IRON_ORE)
                || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.GOLD_ORE)
                || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.COPPER_ORE)
                || state.is(Blocks.DEEPSLATE_COPPER_ORE)
                || state.is(Blocks.NETHER_QUARTZ_ORE)
                || state.is(Blocks.NETHER_GOLD_ORE)
                || state.is(Blocks.GILDED_BLACKSTONE);
    }

    private static LivingEntity golemInSight(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        LivingEntity best = null;
        double bestAlong = Double.MAX_VALUE;
        for (LivingEntity entity : player.serverLevel()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        new net.minecraft.world.phys.AABB(
                                eye.subtract(SELECT_RANGE, SELECT_RANGE, SELECT_RANGE),
                                eye.add(SELECT_RANGE, SELECT_RANGE, SELECT_RANGE)),
                        LivingEntity::isAlive)) {
            if (!(entity instanceof IronGolem) || entity.getUUID().equals(player.getUUID())) {
                continue;
            }
            Vec3 to = entity.position().subtract(eye);
            double along = to.dot(look);
            if (along < 0.5 || along > SELECT_RANGE) {
                continue;
            }
            if (to.subtract(look.scale(along)).length() <= 2.0 && along < bestAlong) {
                bestAlong = along;
                best = entity;
            }
        }
        return best;
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

    private record Recipe(BlockState rock, ItemStack drops) {}

    private Recipe recipe(BlockState state) {
        if (state.is(Blocks.IRON_ORE)) {
            return new Recipe(Blocks.STONE.defaultBlockState(), new ItemStack(Items.RAW_IRON, getAmount(2)));
        }
        if (state.is(Blocks.DEEPSLATE_IRON_ORE)) {
            return new Recipe(Blocks.DEEPSLATE.defaultBlockState(), new ItemStack(Items.RAW_IRON, getAmount(2)));
        }
        if (state.is(Blocks.GOLD_ORE)) {
            return new Recipe(Blocks.STONE.defaultBlockState(), new ItemStack(Items.RAW_GOLD, getAmount(2)));
        }
        if (state.is(Blocks.DEEPSLATE_GOLD_ORE)) {
            return new Recipe(Blocks.DEEPSLATE.defaultBlockState(), new ItemStack(Items.RAW_GOLD, getAmount(2)));
        }
        if (state.is(Blocks.COPPER_ORE)) {
            return new Recipe(Blocks.STONE.defaultBlockState(), new ItemStack(Items.RAW_COPPER, getAmount(2)));
        }
        if (state.is(Blocks.DEEPSLATE_COPPER_ORE)) {
            return new Recipe(Blocks.DEEPSLATE.defaultBlockState(), new ItemStack(Items.RAW_COPPER, getAmount(2)));
        }
        if (state.is(Blocks.NETHER_QUARTZ_ORE)) {
            return new Recipe(Blocks.NETHERRACK.defaultBlockState(), new ItemStack(Items.QUARTZ, getAmount(1)));
        }
        if (state.is(Blocks.NETHER_GOLD_ORE)) {
            return new Recipe(Blocks.NETHERRACK.defaultBlockState(), new ItemStack(Items.GOLD_NUGGET, getAmount(6)));
        }
        if (state.is(Blocks.GILDED_BLACKSTONE)) {
            return new Recipe(Blocks.BLACKSTONE.defaultBlockState(), new ItemStack(Items.GOLD_NUGGET, getAmount(5)));
        }
        return null;
    }

    /** Reference getAmount: 10% triple, else 30% double, else single. */
    private int getAmount(int max) {
        int mult = level.random.nextDouble() * 100 <= TRIPLE_CHANCE
                ? 2
                : (level.random.nextDouble() * 100 <= DOUBLE_CHANCE ? 1 : 0);
        return level.random.nextInt(max) + mult * max + 1;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || done) {
            return false;
        }
        done = true;
        LivingEntity golem = golemInSight(player);
        if (golem != null) {
            drop(new ItemStack(Items.IRON_NUGGET, GOLEM_DROPS));
            golem.hurt(player.damageSources().playerAttack(player), GOLEM_DAMAGE);
            clink(golem.position());
            cool(owner, player, ID, COOLDOWN_TICKS);
            return false;
        }
        BlockPos target = eyeTarget(player, SELECT_RANGE);
        if (target == null || TempBlock.isTemp(level, target)) {
            return false;
        }
        Recipe recipe = recipe(level.getBlockState(target));
        if (recipe == null) {
            return false;
        }
        level.setBlock(target, recipe.rock(), 3);
        drop(recipe.drops());
        clink(Vec3.atCenterOf(target));
        cool(owner, player, ID, COOLDOWN_TICKS);
        return false;
    }

    private void drop(ItemStack stack) {
        ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), stack);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    private void clink(Vec3 at) {
        level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5F, 1.4F);
    }

    @Override
    public void onRemove() {}

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
