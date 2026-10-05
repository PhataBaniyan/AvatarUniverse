package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FireDisc} (ProjectAddons port).
 * Click to throw a spinning disc from the eyes. Steers by adding the gaze
 * to the heading each tick. Solid blocks are cut when cuttable (reverted
 * after 10s, optionally dropped) and stop the disc otherwise; liquids stop
 * it. Ring particles r 0..1 / theta PI/(r*12), 1.5 hit radius, knockback
 * along the heading. Reference values: Cooldown 3000ms, Damage 4, Range 20,
 * Knockback 1.5, Controllable true, Revert true, Drop true.
 */
public class FireDisc extends BendingAbility {
    public static final String ID = "FireDisc";

    /** Reference Cooldown 3000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FIREDISC_COOLDOWN_TICKS.get();

    private static final float DAMAGE = Config.FIREDISC_DAMAGE.get().floatValue();
    private static final double RANGE = Config.FIREDISC_RANGE.get();
    private static final double KNOCKBACK = Config.FIREDISC_KNOCKBACK.get();
    private static final boolean CONTROLLABLE = Config.FIREDISC_CONTROLLABLE.get();
    private static final boolean REVERT = Config.FIREDISC_REVERT.get();
    private static final boolean DROP = Config.FIREDISC_DROP.get();
    /** Reference cut-block regen 10000ms, in server ticks. */
    private static final long REVERT_TICKS = Config.FIREDISC_REVERT_TICKS.get();

    private final ServerPlayer player;
    private final ServerLevel level;
    private final boolean controllable = CONTROLLABLE;
    private final boolean revert = REVERT;
    private final boolean drop = DROP;
    private Vec3 pos;
    private Vec3 dir;
    private double travelled = 0;

    public FireDisc(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
        this.pos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        this.dir = player.getLookAngle().normalize();
        cool(player.getUUID(), player, ID, COOLDOWN_TICKS);
        level.playSound(
                null, this.pos.x, this.pos.y, this.pos.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 1.3F);
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || player.hasDisconnected() || !gate(owner)) {
            return false;
        }
        // Upstream steering: heading absorbs the gaze, step normalizes.
        if (this.controllable) {
            this.dir = this.dir.add(player.getLookAngle().normalize());
        }
        level.playSound(
                null, this.pos.x, this.pos.y, this.pos.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.25F, 1.4F);
        this.pos = this.pos.add(this.dir.normalize().scale(Config.FIREDISC_SPEED.get()));
        this.travelled += Config.FIREDISC_SPEED.get();
        if (this.travelled >= RANGE) {
            return false;
        }
        BlockPos bp = BlockPos.containing(this.pos);
        if (!level.isLoaded(bp)) {
            return false;
        }
        BlockState state = level.getBlockState(bp);
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        if (state.isSolidRender(level, bp)) {
            if (!isCuttable(state)) {
                return false;
            }
            ItemStack stack = new ItemStack(state.getBlock().asItem());
            if (this.revert) {
                TempBlock temp = new TempBlock(level, bp.immutable(), Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
                BendingManager.scheduleRevert(temp, level.getGameTime() + REVERT_TICKS);
            } else {
                level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
            }
            if (this.drop && !stack.isEmpty()) {
                ItemEntity item = new ItemEntity(level, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5, stack);
                level.addFreshEntity(item);
            }
        }
        // Orthogonal disc rings, upstream r/theta lattice.
        Vec3 normal = orthogonal(this.dir);
        for (double r = 0; r < 1; r += 0.2) {
            if (r < 1.0e-6) {
                Vec3 p = this.pos;
                level.sendParticles(
                        BendingTheme.particle(Config.FIREDISC_RING_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        p.x,
                        p.y,
                        p.z,
                        Config.FIREDISC_RING_PARTICLE_COUNT.get(),
                        0.03,
                        0.03,
                        0.03,
                        0.03);
                continue;
            }
            double step = Math.PI / (r * 12);
            for (double theta = 0; theta < 2 * Math.PI; theta += step) {
                Vec3 ortho = rotateAround(normal, this.dir, Math.toDegrees(theta), r);
                Vec3 p = this.pos.add(ortho);
                level.sendParticles(
                        BendingTheme.particle(Config.FIREDISC_RING_PARTICLE.get(), owner, ParticleTypes.FLAME),
                        p.x,
                        p.y,
                        p.z,
                        Config.FIREDISC_RING_PARTICLE_COUNT.get(),
                        0.03,
                        0.03,
                        0.03,
                        0.03);
            }
        }
        for (Entity e :
                level.getEntities(player, new AABB(this.pos, this.pos).inflate(Config.FIREDISC_HIT_RADIUS.get()))) {
            if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                continue;
            }
            if (e instanceof LivingEntity living) {
                living.hurt(player.damageSources().magic(), DAMAGE);
                living.igniteForSeconds(Config.FIREDISC_FIRE_SECONDS.get());
            } else {
                e.igniteForSeconds(Config.FIREDISC_FIRE_SECONDS.get());
            }
            e.setDeltaMovement(this.dir.normalize().scale(KNOCKBACK));
            e.hurtMarked = true;
            return false;
        }
        return true;
    }

    private static Vec3 orthogonal(Vec3 dir) {
        Vec3 up = Math.abs(dir.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 n = dir.cross(up);
        if (n.lengthSqr() < 1.0e-6) {
            return new Vec3(1, 0, 0);
        }
        return n.normalize();
    }

    private static Vec3 rotateAround(Vec3 normal, Vec3 axis, double degrees, double r) {
        double rad = Math.toRadians(degrees);
        Vec3 u = axis.normalize();
        Vec3 v = normal.normalize();
        Vec3 w = u.cross(v).normalize();
        return v.scale(Math.cos(rad) * r).add(w.scale(Math.sin(rad) * r));
    }

    private static final Set<String> CUTTABLE = Set.of(
            "cobweb",
            "short_grass",
            "tall_grass",
            "fern",
            "large_fern",
            "seagrass",
            "tall_seagrass",
            "vine",
            "cave_vines",
            "glow_lichen",
            "dead_bush",
            "dandelion",
            "poppy",
            "blue_orchid",
            "allium",
            "azure_bluet",
            "tulip",
            "oxeye_daisy",
            "cornflower",
            "lily_of_the_valley",
            "wither_rose",
            "sunflower",
            "lilac",
            "rose_bush",
            "peony",
            "wheat",
            "carrots",
            "potatoes",
            "beetroots",
            "nether_wart",
            "sugar_cane",
            "bamboo",
            "snow",
            "oak_sapling",
            "spruce_sapling",
            "birch_sapling",
            "jungle_sapling",
            "acacia_sapling",
            "dark_oak_sapling",
            "mangrove_propagule",
            "red_mushroom",
            "brown_mushroom",
            "sweet_berry_bush",
            "glow_berries",
            "twisting_vines",
            "weeping_vines");

    private static boolean isCuttable(BlockState state) {
        String id = state.getBlock().builtInRegistryHolder().key().location().getPath();
        if (CUTTABLE.contains(id)) {
            return true;
        }
        String s = id.toUpperCase();
        return s.contains("LEAVES") || s.equals("COBWEB") || s.contains("SAPLING");
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
