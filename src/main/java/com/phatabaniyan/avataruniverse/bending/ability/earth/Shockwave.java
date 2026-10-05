package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingManager;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.BendingTheme;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectKorra {@code Shockwave}
 * (core/.../earthbending/Shockwave.java from the local ProjectKorra-master
 * copy). Hold sneak 2.5s to charge (smoke wisps while charged), then
 * release - or click while charged - for a full 360-degree ring of
 * {@link ShockwaveRing}. Landing a 12+ block fall on earthbendable ground slams
 * the ring with no charge needed. Reference values: ChargeTime 2500ms,
 * Cooldown 6000ms, Range 15, FallThreshold 12.
 */
public class Shockwave extends EarthAbility {
    public static final String ID = "Shockwave";

    /** Reference ChargeTime 2500ms, in server ticks. */
    private static final long CHARGE_TICKS = Config.SHOCKWAVE_CHARGE_TICKS.get();
    /** Reference Cooldown 6000ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.SHOCKWAVE_COOLDOWN_TICKS.get();
    /** Reference FallThreshold 12. */
    private static final double FALL_THRESHOLD = Config.SHOCKWAVE_FALL_THRESHOLD.get();

    private final long bornTick;
    private boolean charged;
    private float peakFall;

    public Shockwave(ServerPlayer player) {
        super(player);
        this.bornTick = player.level().getGameTime();
    }

    @Override
    public String name() {
        return ID;
    }

    public boolean isCharged() {
        return charged;
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner)) {
            return false;
        }
        if (!player.onGround()) {
            peakFall = Math.max(peakFall, player.fallDistance);
        } else if (peakFall >= FALL_THRESHOLD) {
            peakFall = 0.0F;
            if (tryFallSlam()) {
                return false;
            }
        } else {
            peakFall = 0.0F;
        }
        if (level.getGameTime() - bornTick >= CHARGE_TICKS) {
            charged = true;
        }
        if (!player.isShiftKeyDown()) {
            if (charged) {
                areaShockwave();
                cool(owner, player, ID, COOLDOWN_TICKS);
                return false;
            }
            return false;
        }
        if (charged) {
            Vec3 puff = player.getEyePosition().add(player.getLookAngle().normalize());
            level.sendParticles(
                    BendingTheme.particle(Config.SHOCKWAVE_PUFF_PARTICLE.get(), ParticleTypes.SMOKE),
                    puff.x,
                    puff.y,
                    puff.z,
                    Config.SHOCKWAVE_PUFF_PARTICLE_COUNT.get(),
                    0.0,
                    0.0,
                    0.0,
                    0.0);
        }
        return true;
    }

    /** Reference fallShockwave: hard landings on earth slam the full ring. */
    private boolean tryFallSlam() {
        if (!Accretion.isEarthbendable(level, player.blockPosition().below())) {
            return false;
        }
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending == null || bending.isOnCooldown(ID, level.getGameTime())) {
            return false;
        }
        areaShockwave();
        cool(owner, player, ID, COOLDOWN_TICKS);
        return true;
    }

    /** Reference areaShockwave: one solid ring rolling outward. */
    private void areaShockwave() {
        BendingManager.start(new ShockwaveRing(player));
    }

    /** Click path: a charged click fires the full ring, same as release. */
    public void clickFire() {
        if (!charged) {
            return;
        }
        areaShockwave();
        cool(owner, player, ID, COOLDOWN_TICKS);
        BendingManager.remove(this);
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
