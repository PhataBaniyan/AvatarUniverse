package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of Hyperion {@code LavaDisk}: sneak at lava (or earth) within 5
 * to spin up a lava disc, then drive it with the hotbar - 1 Follow, 2
 * Advance, 3 Return, 4 Rotate (sneak reverses), 5 Shatter. It eats
 * through leaves and earth, damages what it touches, and dies past range
 * or in water. Reference values: MaxDamage 6, MinDamage 1, Cooldown
 * 7000ms, Range 24, RegenDelay 10000ms, pass-through on.
 */
public class LavaDisk extends EarthAbility {
    public static final String ID = "LavaDisk";

    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.LAVADISK_COOLDOWN_TICKS.get();

    private static final float MAX_DAMAGE = Config.LAVADISK_MAX_DAMAGE.get().floatValue();
    private static final float MIN_DAMAGE = Config.LAVADISK_MIN_DAMAGE.get().floatValue();
    private static final double RANGE = Config.LAVADISK_RANGE.get();
    /** Reference RegenDelay 10000ms, in server ticks. */
    private static final long REGEN_TICKS = Config.LAVADISK_REGEN_TICKS.get();

    private static final double SOURCE_RANGE = Config.LAVADISK_SOURCE_RANGE.get();

    private static final String[] COLORS = {
        "2F1600", "5E2C00", "8C4200", "B05300", "C45D00", "F05A00", "F0A000", "F0BE00"
    };

    private Vec3 disc;
    private double distance;
    private int angle;
    private int rotationAngle;
    private int ticks;
    private String mode = "Follow";

    /** Current slot mode for the disk sub-board. */
    public String diskMode() {
        return mode;
    }

    public LavaDisk(ServerPlayer player) {
        super(player);
        BlockPos source = findSource(player);
        if (source == null) {
            return;
        }
        TempBlock hole = new TempBlock(level, source, Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
        BendingManager.scheduleRevert(hole, level.getGameTime() + REGEN_TICKS);
        disc = new Vec3(source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5);
        distance = disc.distanceTo(player.getEyePosition());
        player.getInventory().selected = 0;
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: lava preferred, else earth, in sight within 5. */
    public static boolean canBegin(ServerPlayer player) {
        return findSource(player) != null;
    }

    private static BlockPos findSource(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        BlockPos earth = null;
        for (double d = 0.5; d <= SOURCE_RANGE; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            BlockState state = player.serverLevel().getBlockState(pos);
            if (state.isAir() || TempBlock.isTemp(player.serverLevel(), pos)) {
                continue;
            }
            if (state.getFluidState().is(Fluids.LAVA)) {
                return pos.immutable();
            }
            if (earth == null && Accretion.isEarthbendable(player.serverLevel(), pos)) {
                if (isMetalState(state)) {
                    return null;
                }
                earth = pos.immutable();
            }
        }
        return earth;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || disc == null) {
            return false;
        }
        if (disc.distanceToSqr(player.getEyePosition()) > RANGE * RANGE || !safe(disc)) {
            return false;
        }
        Vec3 look = player.getLookAngle().normalize();
        Vec3 dir = new Vec3(look.x, look.y, look.z);
        int slot = player.getInventory().selected;
        if (slot == 1) {
            mode = "Advance";
            distance = disc.distanceTo(player.getEyePosition());
            dir = dir.scale(RANGE + 5);
        } else if (slot == 2) {
            mode = "Return";
            distance = disc.distanceTo(player.getEyePosition());
            dir = dir.scale(2.5);
        } else if (slot == 3) {
            mode = "Rotate";
            angle = player.isShiftKeyDown() ? angle + 4 : angle - 4;
            angle = angle % 360;
            dir = dir.scale(distance);
        } else if (slot == 4) {
            shatter();
            return false;
        } else {
            mode = "Follow";
            dir = dir.scale(distance);
        }
        player.displayClientMessage(Component.literal("LavaDisk [" + mode + "]"), true);
        Vec3 target = player.getEyePosition().add(dir);
        Vec3 step = target.subtract(disc).normalize();
        int times = (mode.equals("Advance") || mode.equals("Return")) ? 3 : 2;
        if (times == 3 && player.isShiftKeyDown()) {
            times = 1;
        }
        for (int i = 0; i < times; i++) {
            if (disc.distanceToSqr(target) < 0.25) {
                break;
            }
            disc = disc.add(step.scale(0.4));
        }
        double distanceModifier = distance < 5 ? 1 : (distance >= RANGE ? 0 : 1 - distance / RANGE);
        int spin = Math.max(2, (int) Math.ceil(16 * distanceModifier));
        rotationAngle = (rotationAngle + (spin % 2 == 0 ? spin : spin + 1)) % 360;
        render(mode.equals("Advance") || mode.equals("Return"));
        if (ticks % 4 == 0) {
            float damage;
            if (mode.equals("Advance") || mode.equals("Return")) {
                damage = MAX_DAMAGE;
            } else {
                damage = (float) Math.max(MIN_DAMAGE, MAX_DAMAGE * distanceModifier);
            }
            for (LivingEntity entity : level.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(disc.subtract(1.4, 1.4, 1.4), disc.add(1.4, 1.4, 1.4)),
                    LivingEntity::isAlive)) {
                if (entity.getUUID().equals(owner)) {
                    continue;
                }
                entity.hurt(player.damageSources().playerAttack(player), damage);
                level.sendParticles(
                        BendingTheme.particle(Config.LAVADISK_HIT_PARTICLE.get(), ParticleTypes.LAVA),
                        disc.x,
                        disc.y,
                        disc.z,
                        Config.LAVADISK_HIT_PARTICLE_COUNT.get(),
                        0.5,
                        0.5,
                        0.5,
                        0.1);
            }
        }
        ticks++;
        return true;
    }

    /** Eats leaves, plants, earth and listed meltables into air temps. */
    private void render(boolean large) {
        eat(disc);
        double yaw = Math.toRadians(-player.getYRot() + 90);
        double cos = Math.cos(Math.toRadians(angle));
        double sin = Math.sin(Math.toRadians(angle));
        int index = 0;
        float size = 0.8F;
        for (double pos = 0.1; pos <= 0.8; pos += 0.1) {
            for (int j = 0; j <= 288; j += 72) {
                double a = Math.toRadians(rotationAngle + j + index * 4);
                Vec3 p = new Vec3(disc.x + pos * Math.cos(a), disc.y, disc.z + pos * Math.sin(a));
                p = tiltX(p, cos, sin);
                p = tiltY(p, yaw);
                dust(colorsHex(index), p, size);
                if (pos > 0.5) {
                    eatBlock(p);
                }
            }
            index = Math.max(0, Math.min(COLORS.length - 1, index + 1));
            size -= 0.05F;
        }
        for (int i = 0; i < 10; i++) {
            double a = Math.toRadians(angle) + 2 * Math.PI * i / 10;
            Vec3 p = new Vec3(disc.x + Math.cos(a) * 0.5, disc.y, disc.z + Math.sin(a) * 0.5);
            if (large) {
                melt(p);
            }
        }
    }

    private Vec3 tiltX(Vec3 p, double cos, double sin) {
        double dy = p.y - disc.y;
        double dz = p.z - disc.z;
        return new Vec3(p.x, disc.y + dy * cos - dz * sin, disc.z + dy * sin + dz * cos);
    }

    private Vec3 tiltY(Vec3 p, double yaw) {
        double dx = p.x - disc.x;
        double dz = p.z - disc.z;
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        return new Vec3(disc.x + dx * cos - dz * sin, p.y, disc.z + dx * sin + dz * cos);
    }

    private void dust(String hex, Vec3 p, float size) {
        int rgb = Integer.parseInt(hex, 16);
        level.sendParticles(
                new DustParticleOptions(
                        new org.joml.Vector3f(
                                ((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F),
                        size),
                p.x,
                p.y,
                p.z,
                1,
                0.0,
                0.0,
                0.0,
                0.0);
    }

    private String colorsHex(int index) {
        return COLORS[Math.max(0, Math.min(COLORS.length - 1, index))];
    }

    private void eat(Vec3 at) {
        eatBlock(at);
    }

    private void melt(Vec3 at) {
        eatBlock(at);
    }

    private void eatBlock(Vec3 at) {
        BlockPos pos = BlockPos.containing(at);
        if (TempBlock.isTemp(level, pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty() || isMetalState(state)) {
            return;
        }
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        boolean meltable = Accretion.isEarthbendable(level, pos)
                || key.contains("LEAVES")
                || key.contains("FLOWER")
                || key.contains("GRASS")
                || key.contains("VINE")
                || key.contains("COBBLESTONE")
                || key.contains("_LOG")
                || key.contains("_PLANKS");
        if (!meltable) {
            return;
        }
        TempBlock temp = new TempBlock(level, pos, Blocks.AIR.defaultBlockState(), TempBlock.QUIET);
        BendingManager.scheduleRevert(temp, level.getGameTime() + REGEN_TICKS);
        level.sendParticles(
                BendingTheme.particle(Config.LAVADISK_MELT_PARTICLE.get(), ParticleTypes.LAVA),
                at.x,
                at.y,
                at.z,
                Config.LAVADISK_MELT_PARTICLE_COUNT.get(),
                0.5,
                0.5,
                0.5,
                0.2);
        if (level.random.nextInt(5) == 0) {
            level.playSound(null, at.x, at.y, at.z, SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.3F, 0.3F);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.3F, 1.5F);
        }
    }

    /** Water contact fizzles the disc out. */
    private boolean safe(Vec3 at) {
        BlockPos pos = BlockPos.containing(at);
        if (level.getBlockState(pos).getFluidState().is(Fluids.WATER)) {
            for (int i = 0; i < 10; i++) {
                level.sendParticles(
                        BendingTheme.particle(Config.LAVADISK_FIZZ_PARTICLE.get(), ParticleTypes.CLOUD),
                        at.x,
                        at.y,
                        at.z,
                        Config.LAVADISK_FIZZ_PARTICLE_COUNT.get(),
                        level.random.nextDouble(),
                        level.random.nextDouble(),
                        level.random.nextDouble(),
                        0.0);
            }
            level.playSound(null, at.x, at.y, at.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.isSolid()) {
            return true;
        }
        if (isMetalState(state) || !state.getFluidState().isEmpty()) {
            return false;
        }
        eatBlock(Vec3.atCenterOf(pos));
        return true;
    }

    private void shatter() {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MAGMA_BLOCK.defaultBlockState()),
                disc.x,
                disc.y,
                disc.z,
                20,
                0.1,
                0.1,
                0.1,
                0.0);
        level.playSound(null, disc.x, disc.y, disc.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0F, 1.5F);
        level.sendParticles(
                BendingTheme.particle(Config.LAVADISK_SHATTER_PARTICLE.get(), ParticleTypes.LAVA),
                disc.x,
                disc.y,
                disc.z,
                Config.LAVADISK_SHATTER_PARTICLE_COUNT.get(),
                0,
                0,
                0,
                0);
    }

    @Override
    public void onRemove() {
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
