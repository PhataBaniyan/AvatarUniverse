package com.phatabaniyan.avataruniverse.bending.ability.avatar;

import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.ability.BendingAbility;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Port of ProjectAvatar {@code avatar.sphere.SphereAttack}: shared base for
 * the ElementSphere sub-attacks (eye anchor, liveness check, construction
 * gate). Reference values are inlined per subclass (no config in this mod).
 */
public abstract class SphereAttack extends BendingAbility {
    protected final ServerPlayer player;
    protected final ServerLevel level;
    protected boolean started = false;

    protected SphereAttack(ServerPlayer player) {
        super(player.getUUID(), player.level().getGameTime());
        this.player = player;
        this.level = player.serverLevel();
    }

    /** True when construction passed every gate and the attack may be started. */
    public boolean isStarted() {
        return started;
    }

    protected boolean alive() {
        return player.isAlive() && !player.hasDisconnected();
    }

    protected static Vec3 eyePos(ServerPlayer sp) {
        return new Vec3(sp.getX(), sp.getEyeY(), sp.getZ());
    }

    protected static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.AVATAR) && bending.isToggled();
    }
}
