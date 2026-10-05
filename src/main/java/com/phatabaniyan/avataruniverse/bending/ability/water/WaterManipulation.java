package com.phatabaniyan.avataruniverse.bending.ability.water;

import com.phatabaniyan.avataruniverse.Config;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Reference port of ProjectKorra {@code WaterManipulation}
 * (waterbending/WaterManipulation.java): the fundamental water ability. Like
 * Korra it bends a real block: a {@link TempBlock} travels the bolt path and
 * each previous position is reverted as it advances, so the world is
 * untouched when the ability ends. The bolt originates at the player's tapped
 * source (Korra source selection) and flies along the look direction. Ice and
 * snow sources fire a packed-ice bolt with snowflake spray instead of water.
 * (A particle spray rides along for the visual Korra adds on top of the
 * block.)
 */
public class WaterManipulation extends BendingAbility {
    public static final String ID = "WaterManipulation";

    private final ServerLevel level;
    private final Vec3 direction;
    private final boolean icy;
    private Vec3 position;
    private TempBlock bolt;
    private double traveled;

    public WaterManipulation(ServerPlayer player, Vec3 origin, boolean icy) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
        this.direction = player.getLookAngle().normalize();
        this.position = origin;
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

    @Override
    public boolean progress() {
        double range = Config.WATERMANIP_RANGE.get();
        double speed = 1.2;
        if (traveled >= range) {
            return false;
        }
        position = position.add(direction.scale(speed));
        traveled += speed;

        // Advance the real bolt block, reverting the previous position.
        if (bolt != null) {
            bolt.revert();
        }
        bolt = new TempBlock(
                level,
                BlockPos.containing(position),
                icy ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.WATER.defaultBlockState(),
                TempBlock.QUIET);

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

        double radius = 1.6;
        AABB box = new AABB(position.subtract(radius, radius, radius), position.add(radius, radius, radius));
        boolean hit = false;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target.getUUID().equals(owner)) {
                continue;
            }
            Vec3 push = direction.scale(1.0).add(new Vec3(0.0, 0.3, 0.0));
            target.setDeltaMovement(target.getDeltaMovement().add(push));
            target.hurtMarked = true;
            target.hurt(level.damageSources().magic(), (float) (double) Config.WATERMANIP_DAMAGE.get());
            hit = true;
        }
        return !hit;
    }

    @Override
    public void onRemove() {
        if (bolt != null) {
            bolt.revert();
            bolt = null;
        }
    }
}
