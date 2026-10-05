package com.phatabaniyan.avataruniverse.bending.ability.fire;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code FlameBreath}: hold sneak and breathe a growing
 * cone of flame down the look vector that burns bodies and scorches the ground
 * it crawls over. Reference values: Cooldown 4000ms (80 ticks), Duration
 * 5000ms (100 ticks), Range 14, Damage 3, FireTicks 3, Speed 1.
 */
public class FlameBreath extends BendingAbility {
    public static final String ID = "FlameBreath";

    /** Reference Cooldown 4000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.FLAMEBREATH_COOLDOWN_TICKS.get();
    /** Reference Duration 5000ms, in server ticks. */
    private static final long DURATION_TICKS = Config.FLAMEBREATH_DURATION_TICKS.get();

    private static final double RANGE = Config.FLAMEBREATH_RANGE.get();
    private static final double DAMAGE = Config.FLAMEBREATH_DAMAGE.get();
    private static final int FIRE_SECONDS = Config.FLAMEBREATH_FIRE_SECONDS.get();
    private static final double SPEED = Config.FLAMEBREATH_SPEED.get();

    private final ServerLevel level;
    private double currentRange;

    public FlameBreath(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.level = player.serverLevel();
    }

    @Override
    public String name() {
        return ID;
    }

    @Override
    public boolean progress() {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player == null || player.hasDisconnected() || !player.isAlive()) {
            return false;
        }
        if (!gate(owner)) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        if (level.getGameTime() - startTime > DURATION_TICKS) {
            cool(owner, level, ID, COOLDOWN_TICKS);
            return false;
        }
        Vec3 eye = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 dir = player.getLookAngle().normalize().scale(SPEED);
        if (currentRange < RANGE) {
            currentRange += SPEED;
        }
        for (double d = 0; d <= currentRange; d += SPEED) {
            Vec3 loc = new Vec3(eye.x + dir.x * d, eye.y + dir.y * d, eye.z + dir.z * d);
            BlockPos bp = BlockPos.containing(loc);
            if (!level.isLoaded(bp)) {
                break;
            }
            var state = level.getBlockState(bp);
            if (state.isSolidRender(level, bp) || !state.getFluidState().isEmpty()) {
                break;
            }
            double spread = 0.1 * d;
            int amount = Math.max(1, (int) Math.ceil(d));
            level.sendParticles(
                    BendingTheme.particle(Config.FLAMEBREATH_BREATH_PARTICLE.get(), owner, ParticleTypes.FLAME),
                    loc.x,
                    loc.y,
                    loc.z,
                    amount,
                    spread,
                    spread,
                    spread,
                    0.04);
            if (player.getRandom().nextDouble() > 0.9) {
                level.playSound(null, loc.x, loc.y, loc.z, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.3F, 1.0F);
            }
            for (Entity e : level.getEntities(player, new AABB(loc, loc).inflate(spread * 2.5))) {
                if (e.getUUID().equals(player.getUUID()) || e instanceof ArmorStand) {
                    continue;
                }
                if (e instanceof LivingEntity living) {
                    living.hurt(player.damageSources().magic(), (float) DAMAGE);
                    living.igniteForSeconds(FIRE_SECONDS);
                } else if (e instanceof net.minecraft.world.entity.item.ItemEntity item) {
                    item.igniteForSeconds(FIRE_SECONDS + 40);
                } else {
                    e.igniteForSeconds(FIRE_SECONDS);
                }
            }
        }
        if (player.getRandom().nextInt(20) == 0) {
            Vec3 tip = eye.add(dir.scale(currentRange));
            FireBurst.scorchGround(level, tip, 3.0, 6, 30);
        }
        return true;
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.FIRE) && bending.isToggled();
    }

    private static void cool(UUID owner, ServerLevel level, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, level.getGameTime() + ticks);
        }
    }
}
