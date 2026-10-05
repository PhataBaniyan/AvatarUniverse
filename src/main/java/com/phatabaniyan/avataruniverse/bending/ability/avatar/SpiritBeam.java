package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code SpiritBeam}: sneak-hold to fire a spirit beam
 * along the gaze — witch/portals motes per step, 5s ignite plus magic damage
 * to each living entity hit once per tick, and a block hit triggers a
 * zero-power explosion plus a mineable-only crater that reverts shortly
 * after. Cooldown starts on press (AirShield pattern) and is free while the
 * Avatar State holds. Reference values: Duration 5000ms (100 ticks),
 * Cooldown 8000ms (160 ticks), Damage 6, Range 30, BlockRadius 3,
 * BlockRevert 8000ms (160 ticks), block damage on.
 */
public class SpiritBeam extends BendingAbility {
    public static final String ID = "SpiritBeam";

    /** Reference Duration 5000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.msToTicks(Config.SPIRITBEAM_DURATION_MS.get());
    /** Reference Cooldown 8000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.msToTicks(Config.SPIRITBEAM_COOLDOWN_MS.get());

    private static final double BASE_DAMAGE = Config.SPIRITBEAM_DAMAGE.get();
    private static final double BASE_RANGE = Config.SPIRITBEAM_RANGE.get();
    private static final int BLOCK_RADIUS = Config.SPIRITBEAM_BLOCK_RADIUS.get();
    /** Reference BlockRevert 8000ms, in server ticks. */
    private static final long BLOCK_REVERT_TICKS = Config.msToTicks(Config.SPIRITBEAM_BLOCK_REVERT_MS.get());

    private static final int IGNITE_SECONDS = Config.SPIRITBEAM_IGNITE_MS.get() / 1000;

    private final ServerLevel level;
    private final double damage;
    private final double range;
    private boolean started = false;

    public SpiritBeam(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();

        SpiritBeam old = BendingManager.find(player.getUUID(), SpiritBeam.class);
        if (old != null) {
            BendingManager.remove(old);
        }

        this.damage = AvatarState.scaleDamage(player, BASE_DAMAGE);
        this.range = AvatarState.scaleRange(player, BASE_RANGE);
        // Cooldown starts on press; free while the Avatar State holds.
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending != null) {
            long ticks = AvatarState.isActive(player) ? 0 : COOLDOWN_TICKS;
            bending.setCooldown(ID, player.level().getGameTime() + ticks);
        }
        this.started = true;
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        if (!started) {
            return false;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (level.getGameTime() > startTime + DURATION_TICKS) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            return false;
        }
        fireBeam(player);
        return true;
    }

    private void fireBeam(ServerPlayer player) {
        Vec3 dir = player.getLookAngle().normalize();
        Vec3 origin = new Vec3(player.getX(), player.getY() + 1.2, player.getZ());
        // 1.0 steps with a 2.0 hitbox keep full coverage at half the iterations.
        // Hits are deduped so each entity takes damage once per tick (previously an
        // entity on the beam centerline was hit ~8x per tick by overlapping steps).
        Set<UUID> hitThisTick = new HashSet<>();
        double px = origin.x;
        double py = origin.y;
        double pz = origin.z;
        for (double i = 0; i < this.range; i += 1.0) {
            px += dir.x;
            py += dir.y;
            pz += dir.z;
            // Source Fx.near: broadcast to viewers within 64 of the origin only.
            double r2 = 64.0 * 64.0;
            for (ServerPlayer viewer : level.players()) {
                if (viewer.position().distanceToSqr(origin) <= r2) {
                    level.sendParticles(
                            viewer,
                            BendingTheme.particle(Config.SPIRITBEAM_WITCH_PARTICLE.get(), ParticleTypes.WITCH),
                            true,
                            px,
                            py,
                            pz,
                            Config.SPIRITBEAM_WITCH_PARTICLE_COUNT.get(),
                            0.1,
                            0.1,
                            0.1,
                            0.01);
                    level.sendParticles(
                            viewer,
                            BendingTheme.particle(Config.SPIRITBEAM_PORTAL_PARTICLE.get(), ParticleTypes.PORTAL),
                            true,
                            px,
                            py,
                            pz,
                            Config.SPIRITBEAM_PORTAL_PARTICLE_COUNT.get(),
                            0.2,
                            0.2,
                            0.2,
                            0.1);
                }
            }

            for (Entity entity :
                    level.getEntities(player, new AABB(px - 2.0, py - 2.0, pz - 2.0, px + 2.0, py + 2.0, pz + 2.0))) {
                if (!(entity instanceof LivingEntity living) || entity instanceof ArmorStand) {
                    continue;
                }
                if (!hitThisTick.add(entity.getUUID())) {
                    continue;
                }
                living.igniteForSeconds(IGNITE_SECONDS);
                living.hurt(player.damageSources().magic(), (float) this.damage);
            }

            BlockPos bp = BlockPos.containing(px, py, pz);
            if (!level.getBlockState(bp).isAir()) {
                level.explode(null, px, py, pz, 0.0F, Level.ExplosionInteraction.NONE);
                crater(level, new Vec3(px, py, pz), BLOCK_RADIUS, BLOCK_REVERT_TICKS);
                return;
            }
        }
    }

    /** Source SphereBlast.crater, reverted via TempBlock + BendingManager.scheduleRevert. */
    private static void crater(ServerLevel level, Vec3 center, int radius, long revertTicks) {
        BlockPos c = BlockPos.containing(center);
        for (BlockPos pos :
                BlockPos.betweenClosed(c.offset(-radius, -radius, -radius), c.offset(radius, radius, radius))) {
            if (pos.distSqr(c) > (double) radius * radius) {
                continue;
            }
            var state = level.getBlockState(pos);
            if (state.isAir() || state.is(BlockTags.WITHER_IMMUNE)) {
                continue;
            }
            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                continue;
            }
            BlockPos immutable = pos.immutable();
            TempBlock temp = new TempBlock(level, immutable, Blocks.AIR.defaultBlockState());
            if (revertTicks > 0) {
                BendingManager.scheduleRevert(temp, level.getGameTime() + revertTicks);
            }
        }
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AVATAR) && bending.isToggled();
    }
}
