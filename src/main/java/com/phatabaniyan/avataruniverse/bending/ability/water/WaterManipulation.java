package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingSources;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Reference port of ProjectKorra {@code WaterManipulation}
 * (waterbending/WaterManipulation.java): the fundamental water ability. Like
 * Korra it bends a real block: a 3-block comet (source head plus two fading
 * tail blocks) advances exactly one block per tick, each previous position
 * reverting only after the new head is placed, so the bolt never strobes or
 * gaps and the world is untouched when it ends. The bolt originates at the
 * player's tapped source (Korra source selection) and steers toward the live
 * gaze target on every click (Korra redirect); an isolated source is
 * consumed unless it sits in a large body (Korra 3+ source ocean
 * exemption). Ice and snow sources fire a packed-ice bolt with snowflake
 * spray instead of water. (A particle spray rides along for the visual
 * Korra adds on top of the block.)
 */
public class WaterManipulation extends BendingAbility {
    public static final String ID = "WaterManipulation";

    private static final double STEP = 1.0;
    private static final double ARRIVE_DIST_SQR = 1.0;
    private static final double COLLISION_RADIUS = 1.0;
    private static final double KNOCKBACK = 0.3;

    private final ServerLevel level;
    private final boolean icy;
    private Vec3 position;
    private Vec3 target;
    private TempBlock head;
    private TempBlock tail;
    private TempBlock tail2;
    private double traveled;

    public WaterManipulation(ServerPlayer player, Vec3 origin, Vec3 target, boolean icy) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.position = origin;
        this.target = target;
        this.icy = icy;
        this.traveled = 0.0;
        level.playSound(
                null,
                BlockPos.containing(origin),
                SoundEvents.PLAYER_SPLASH,
                SoundSource.PLAYERS,
                0.7F,
                icy ? 0.7F : 1.1F);
    }

    @Override
    public String name() {
        return ID;
    }

    /** Korra redirect: an in-flight bolt adopts the caster's latest target. */
    public void redirectTo(Vec3 target) {
        this.target = target;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.isRemoved()) {
            return false;
        }
        double range = Config.WATERMANIP_RANGE.get();
        if (traveled >= range || position.distanceToSqr(target) <= ARRIVE_DIST_SQR) {
            return false;
        }
        Vec3 to = target.subtract(position);
        if (to.lengthSqr() < 1.0e-6) {
            return false;
        }
        position = position.add(to.normalize().scale(STEP));
        traveled += STEP;
        BlockPos cell = BlockPos.containing(position);

        // Walls stop the bolt (Korra collide); replaceable cover is brushed
        // aside by simply bending through it.
        BlockState state = level.getBlockState(cell);
        if (!state.isAir() && state.getFluidState().isEmpty() && !BendingSources.isTransparentForBend(level, cell)) {
            return false;
        }

        // Place the new head first, then slide the fading tail forward
        // (Korra order): held cells are never touched, so nothing flickers.
        TempBlock newHead = new TempBlock(
                level, cell.immutable(), icy ? Blocks.PACKED_ICE.defaultBlockState() : waterLevel(7), TempBlock.QUIET);
        if (tail2 != null) {
            tail2.revert();
        }
        tail2 = tail;
        tail = head;
        head = newHead;
        if (tail != null) {
            tail.updateReplacement(icy ? Blocks.PACKED_ICE.defaultBlockState() : waterLevel(6));
        }

        if (icy) {
            level.sendParticles(
                    BendingTheme.particle(Config.WATERMANIPULATION_FROST_PARTICLE.get(), ParticleTypes.SNOWFLAKE),
                    position.x,
                    position.y,
                    position.z,
                    Config.WATERMANIPULATION_FROST_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.03);
        } else {
            level.sendParticles(
                    BendingTheme.particle(Config.WATERMANIPULATION_SPRAY_PARTICLE.get(), ParticleTypes.SPLASH),
                    position.x,
                    position.y,
                    position.z,
                    Config.WATERMANIPULATION_SPRAY_PARTICLE_COUNT.get(),
                    0.3,
                    0.3,
                    0.3,
                    0.05);
        }

        AABB box = new AABB(
                position.subtract(COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS),
                position.add(COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS));
        boolean hit = false;
        for (LivingEntity targetEntity : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (targetEntity.getUUID().equals(owner)) {
                continue;
            }
            Vec3 push = player.getLookAngle().normalize().scale(KNOCKBACK).add(new Vec3(0.0, 0.1, 0.0));
            targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(push));
            targetEntity.hurtMarked = true;
            targetEntity.hurt(level.damageSources().magic(), (float) (double) Config.WATERMANIP_DAMAGE.get());
            hit = true;
        }
        return !hit;
    }

    private static BlockState waterLevel(int level) {
        BlockState state = Blocks.WATER.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.LEVEL)) {
            state = state.setValue(BlockStateProperties.LEVEL, level);
        }
        return state;
    }

    @Override
    public void onRemove() {
        if (tail2 != null) {
            tail2.revert();
            tail2 = null;
        }
        if (tail != null) {
            tail.revert();
            tail = null;
        }
        if (head != null) {
            head.revert();
            head = null;
        }
    }
}
