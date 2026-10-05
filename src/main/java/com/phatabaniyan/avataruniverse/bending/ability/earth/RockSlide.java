package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAddons {@code RockSlide}
 * (addons/.../ability/earth/RockSlide.java): click to ride a compact 3x3
 * platform of half-size earth cubes packed edge to edge, glued under your
 * feet while your gaze steers it at ride speed. Anything within
 * 2 takes 1 damage with an upward pop. Ends if you are hurt, sneak, or run
 * out of ground. Reference values: Cooldown 7000ms, Damage 1, Knockback
 * 0.9, Knockup 0.4, Speed 0.68, TurningSpeed 0.086. Reference is a combo
 * and scatters full falling blocks; here it fires standalone on click as
 * one tight display raft you actually stand on.
 */
public class RockSlide extends EarthAbility {
    public static final String ID = "RockSlide";

    /** Reference Cooldown 7000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.ROCKSLIDE_COOLDOWN_MS.get());

    private static final float DAMAGE = Config.ROCKSLIDE_DAMAGE.get().floatValue();
    private static final double KNOCKBACK = Config.ROCKSLIDE_KNOCKBACK.get();
    private static final double KNOCKUP = Config.ROCKSLIDE_KNOCKUP.get();
    private static final double SPEED = Config.ROCKSLIDE_SPEED.get();
    private static final double TURNING = Config.ROCKSLIDE_TURNING.get();
    private static final double HIT_RADIUS = Config.ROCKSLIDE_HIT_RADIUS.get();
    /** Half-size cubes on a 0.5 grid: a gapless 1.5-wide raft. */
    private static final float SIZE = 0.5F;

    private static final double GRID = 0.5;

    private Vec3 direction;
    private final List<UUID> tiles = new ArrayList<>();
    private final Map<UUID, BlockState> textures = new HashMap<>();
    private float lastHealth;
    private double deckY;

    public RockSlide(ServerPlayer player) {
        super(player);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) {
            flat = new Vec3(0, 0, 1);
        }
        this.direction = flat.normalize().scale(SPEED);
        this.lastHealth = player.getHealth();
        this.deckY = player.position().y - 0.5;
        for (int i = 0; i < 9; i++) {
            Vec3 at = tilePos(i);
            BlockState texture = sampleTexture(at);
            net.minecraft.world.entity.Display.BlockDisplay disp = new net.minecraft.world.entity.Display.BlockDisplay(
                    net.minecraft.world.entity.EntityType.BLOCK_DISPLAY, level);
            setDisplayState(disp, texture);
            setSize(disp);
            disp.setPos(at.x - 0.25, deckY, at.z - 0.25);
            level.addFreshEntity(disp);
            tiles.add(disp.getUUID());
            textures.put(disp.getUUID(), texture);
        }
    }

    @Override
    public String name() {
        return ID;
    }

    /** Click-start gate: bendable ground underfoot within reach. */
    public static boolean canBegin(ServerPlayer player) {
        BlockPos feet = player.blockPosition();
        for (int dy = 1; dy <= 3; dy++) {
            if (Accretion.isEarthbendable(player.serverLevel(), feet.below(dy))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (player.getHealth() < lastHealth || player.isShiftKeyDown()) {
            return false;
        }
        lastHealth = player.getHealth();
        Double floor = floorTop();
        if (floor == null) {
            return false;
        }
        Vec3 look = player.getLookAngle().normalize();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() >= 1.0e-6) {
            Vec3 want = flat.normalize().scale(SPEED);
            direction = direction.scale(1 - TURNING).add(want.scale(TURNING));
            if (direction.lengthSqr() > 1.0e-6) {
                direction = direction.normalize().scale(SPEED);
            }
        }
        // The deck glues to the terrain; the rider glues mid-deck.
        deckY = deckY + (floor + 0.02 - deckY) * 0.5;
        double feetTarget = deckY + SIZE * 0.5;
        double vertical = (feetTarget - player.position().y) * 0.6;
        vertical = Math.max(-0.7, Math.min(0.7, vertical));
        // Step-up hop: a blocked face with open headroom lifts the ride over.
        Vec3 flatDir = new Vec3(direction.x, 0, direction.z);
        if (flatDir.lengthSqr() > 1.0e-6) {
            flatDir = flatDir.normalize();
            BlockPos aheadFeet = BlockPos.containing(
                    player.getX() + flatDir.x * 0.9, player.getY(), player.getZ() + flatDir.z * 0.9);
            BlockState face = level.getBlockState(aheadFeet);
            BlockState headroom = level.getBlockState(aheadFeet.above());
            if (!face.isAir() && face.isSolid() && (headroom.isAir() || !headroom.isSolid())) {
                vertical = Math.max(vertical, 0.7);
            }
        }
        player.setDeltaMovement(new Vec3(direction.x, vertical, direction.z));
        player.hurtMarked = true;
        player.resetFallDistance();
        Vec3 center = new Vec3(player.getX(), deckY, player.getZ());
        for (int i = 0; i < tiles.size(); i++) {
            UUID id = tiles.get(i);
            if (!(level.getEntity(id) instanceof net.minecraft.world.entity.Display.BlockDisplay disp)) {
                continue;
            }
            Vec3 at = tilePos(i);
            BlockState texture = sampleTexture(at);
            if (!texture.equals(textures.get(id))) {
                setDisplayState(disp, texture);
                textures.put(id, texture);
            }
            disp.setPos(at.x - 0.25, deckY, at.z - 0.25);
            if (level.random.nextDouble() < 0.08) {
                level.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, texture),
                        at.x,
                        deckY + 0.3,
                        at.z,
                        1,
                        0.2,
                        0.1,
                        0.2,
                        0.0);
            }
        }
        if (level.random.nextInt(20) == 0) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.STONE_BREAK,
                    SoundSource.PLAYERS,
                    0.3F,
                    0.8F);
        }
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        player.position().subtract(HIT_RADIUS, HIT_RADIUS, HIT_RADIUS),
                        player.position().add(HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            if (entity.position().distanceToSqr(player.position()) > HIT_RADIUS * HIT_RADIUS
                    && entity.getEyePosition().distanceToSqr(player.position()) > HIT_RADIUS * HIT_RADIUS) {
                continue;
            }
            entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
            Vec3 away = entity.position().subtract(player.position());
            away = new Vec3(away.x, KNOCKUP, away.z).normalize().scale(KNOCKBACK);
            entity.setDeltaMovement(away);
            entity.hurtMarked = true;
        }
        return true;
    }

    /**
     * 3x3 grid slots led one tick ahead of travel, half-cell spacing, no
     * gaps: leading keeps the rider centred instead of outrunning the
     * raft to its front edge.
     */
    private Vec3 tilePos(int i) {
        Vec3 anchor = new Vec3(player.getX(), deckY, player.getZ()).add(direction);
        double ox = (i % 3 - 1) * GRID;
        double oz = (i / 3 - 1) * GRID;
        return new Vec3(anchor.x + ox, deckY, anchor.z + oz);
    }

    /** Soil top under the rider centre; the deck dies over the void. */
    private Double floorTop() {
        BlockPos probe = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        for (int dy = 2; dy >= -6; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = level.getBlockState(cell);
            if (!state.isAir() && (state.isSolid() || Accretion.isEarthbendable(level, cell))) {
                return (double) cell.getY() + 1.0;
            }
        }
        return null;
    }

    /** Deck texture from the soil under a point, dirt when there is none. */
    private BlockState sampleTexture(Vec3 at) {
        BlockPos probe = BlockPos.containing(at.x, at.y, at.z);
        for (int dy = 2; dy >= -4; dy--) {
            BlockPos cell = probe.offset(0, dy, 0);
            BlockState state = level.getBlockState(cell);
            if (Accretion.isEarthbendable(level, cell) && state.isSolid()) {
                return state;
            }
        }
        return Blocks.DIRT.defaultBlockState();
    }

    private static void setDisplayState(net.minecraft.world.entity.Display.BlockDisplay disp, BlockState state) {
        disp.getEntityData().set(blockKey(), state);
    }

    private static void setSize(net.minecraft.world.entity.Display.BlockDisplay disp) {
        disp.getEntityData().set(scaleKey(), new org.joml.Vector3f(SIZE, SIZE, SIZE));
    }

    @SuppressWarnings("unchecked")
    private static net.minecraft.network.syncher.EntityDataAccessor<BlockState> blockKey() {
        return (net.minecraft.network.syncher.EntityDataAccessor<BlockState>) BlockKeyHolder.KEY;
    }

    @SuppressWarnings("unchecked")
    private static net.minecraft.network.syncher.EntityDataAccessor<org.joml.Vector3f> scaleKey() {
        return (net.minecraft.network.syncher.EntityDataAccessor<org.joml.Vector3f>) ScaleKeyHolder.KEY;
    }

    private static final class BlockKeyHolder {
        static final Object KEY = dataKey(net.minecraft.world.entity.Display.BlockDisplay.class, "DATA_BLOCK_STATE_ID");
    }

    private static final class ScaleKeyHolder {
        static final Object KEY = dataKey(net.minecraft.world.entity.Display.class, "DATA_SCALE_ID");
    }

    private static Object dataKey(Class<?> owner, String field) {
        try {
            java.lang.reflect.Field f = owner.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRemove() {
        for (UUID id : tiles) {
            if (level.getEntity(id) instanceof net.minecraft.world.entity.Display.BlockDisplay disp) {
                disp.discard();
            }
        }
        tiles.clear();
        textures.clear();
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
