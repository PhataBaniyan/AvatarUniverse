package com.phatabaniyan.avataruniverse.bending.ability.earth;

import com.phatabaniyan.avataruniverse.Config;
import com.phatabaniyan.avataruniverse.bending.BendingElement;
import com.phatabaniyan.avataruniverse.bending.BendingPlayer;
import com.phatabaniyan.avataruniverse.bending.TempBlock;
import com.phatabaniyan.avataruniverse.bending.ability.EarthAbility;
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
 * Port of ProjectKorra {@code EarthBlast}
 * (core/.../earthbending/EarthBlast.java from the local ProjectKorra-master
 * copy). Sneak at an earthbendable block within 10 to select it (it flashes
 * to a focus state: sand to sandstone, stone to cobble, anything else to
 * stone), then left click to hurl it: the block first pops up or down to
 * firstDestination to clear the ground, then flies toward the aimed entity
 * or block up to 30 away. Clicking again mid-flight redirects it. The
 * flight oscillates vertically for its first second in the air, then flies
 * straight; the moment it touches a wall or the ground it vanishes (hole,
 * path and all revert at once). Hits deal 3 damage with knockback along the gaze. Reference values: SelectRange 10, Range 30, Speed 35,
 * Damage 3, Push 0.3, Cooldown 500ms, CollisionRadius 1.5, Revert on,
 * CanHitSelf off.
 */
public class EarthBlast extends EarthAbility {
    public static final String ID = "EarthBlast";

    private static final double SELECT_RANGE = Config.EARTHBLAST_SELECT_RANGE.get();
    private static final double RANGE = Config.EARTHBLAST_RANGE.get();
    /** Reference Speed 35 blocks/s, in blocks per server tick. */
    private static final double SPEED_PER_TICK = Config.EARTHBLAST_SPEED.get();

    private static final float DAMAGE = Config.EARTHBLAST_DAMAGE.get().floatValue();
    /** Reference Push 0.3. */
    private static final double PUSH = Config.EARTHBLAST_PUSH.get();

    private static final double COLLISION_RADIUS = Config.EARTHBLAST_COLLISION_RADIUS.get();
    /** Reference Cooldown 500ms, in server ticks. */
    private static final long COOLDOWN_TICKS = Config.EARTHBLAST_COOLDOWN_TICKS.get();
    /** Ticks of vertical oscillation after each hurl or redirect. */
    private static final int WOBBLE_TICKS = 20;
    /** Peak height of the flight wobble, in blocks. */
    private static final double WOBBLE_AMPLITUDE = 0.9;
    /** Cruise height above the aim line, so the wobble never grazes dirt. */
    private static final double CRUISE_LIFT = 1.2;
    /** Distance from the target where the flight dives onto it. */
    private static final double DIVE_RANGE_SQR = 36.0;
    /** How strongly the flight homes toward the aim each tick. */
    private static final double HOMING = 0.08;

    private boolean valid;
    private BlockPos source;
    private BlockState realState;
    private BlockState flyState;
    private Vec3 head;
    private Vec3 vel;
    private Vec3 destination;
    private Vec3 firstDestination;
    private boolean progressing;
    private boolean settingUp = true;
    private double budget;
    private int flightTicks;
    private double lastBob;
    private UUID displayId;

    public EarthBlast(ServerPlayer player) {
        super(player);
        this.valid = prepare();
    }

    @Override
    public String name() {
        return ID;
    }

    /** Sneak-start gate: an earthbendable source in view within select range. */
    public static boolean canBegin(ServerPlayer player) {
        return findSource(player) != null;
    }

    public boolean isProgressing() {
        return progressing;
    }

    /** Sneak again while aiming: drop the old focus and select a new source. */
    public boolean reprepare() {
        revertMovedEarth();
        progressing = false;
        settingUp = true;
        destination = null;
        firstDestination = null;
        source = null;
        budget = 0.0;
        valid = prepare();
        return valid;
    }

    /** First non-air cell along the gaze, if it is earthbendable. */
    private static BlockPos findSource(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (double d = 0.5; d <= SELECT_RANGE; d += 0.5) {
            BlockPos pos = BlockPos.containing(eye.add(look.scale(d)));
            if (player.serverLevel().getBlockState(pos).isAir()) {
                continue;
            }
            if (!Accretion.isEarthbendable(player.serverLevel(), pos)) {
                return null;
            }
            return pos.immutable();
        }
        return null;
    }

    /** Reference prepare: focus the source block in place. */
    private boolean prepare() {
        BlockPos target = findSource(player);
        if (target == null || TempBlock.isTemp(level, target)) {
            return false;
        }
        source = target;
        realState = level.getBlockState(source);
        flyState = stabilize(realState);
        remember(source);
        level.setBlock(source, focusDisplay(realState), 2);
        return true;
    }

    /**
     * Reference focusBlock: the selected source flashes to sandstone for
     * sand, red sandstone for red sand, cobblestone for stone, stone for
     * anything else.
     */
    private static BlockState focusDisplay(BlockState state) {
        var block = state.getBlock();
        if (block == Blocks.SAND) {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        if (block == Blocks.RED_SAND) {
            return Blocks.RED_SANDSTONE.defaultBlockState();
        }
        if (block == Blocks.STONE) {
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }

    /** Click path: hurl the focused block, or redirect it mid-flight. */
    public void throwEarth() {
        if (progressing || source == null) {
            return;
        }
        destination = currentTarget();
        head = Vec3.atCenterOf(source);
        if (head.distanceToSqr(destination) < 1.0) {
            return;
        }
        firstDestination = new Vec3(head.x, head.y, head.z);
        if (destination.y - head.y > 2.0) {
            firstDestination = new Vec3(head.x, destination.y - 1.0, head.z);
        } else if (head.y > player.getEyePosition().y && passable(source.above())) {
            firstDestination = new Vec3(head.x, head.y - 2.0, head.z);
        } else if (passable(source.above()) && passable(source.above(2))) {
            firstDestination = new Vec3(head.x, head.y + 2.0, head.z);
        } else {
            Vec3 flat = destination.subtract(head);
            flat = new Vec3(flat.x, 0.0, flat.z);
            if (flat.lengthSqr() < 1.0e-6) {
                flat = player.getLookAngle();
                flat = new Vec3(flat.x, 0.0, flat.z);
            }
            flat = flat.normalize();
            firstDestination = new Vec3(head.x + flat.x, head.y, head.z + flat.z);
        }
        level.setBlock(source, Blocks.AIR.defaultBlockState(), 2);
        spawnHead();
        progressing = true;
        settingUp = true;
        flightTicks = 0;
        lastBob = 0.0;
        vel = firstDestination.subtract(head);
        if (vel.lengthSqr() < 1.0e-6) {
            vel = destination.subtract(head);
        }
        vel = vel.normalize().scale(SPEED_PER_TICK);
        level.playSound(null, head.x, head.y, head.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.7F, 1.2F);
        cool(owner, player, ID, COOLDOWN_TICKS);
    }

    /**
     * The flying head is a BlockDisplay like Accretion's chunks: the client
     * interpolates entity positions between ticks, so the flight glides
     * instead of snapping cell to cell like real blocks do.
     */
    private void spawnHead() {
        net.minecraft.world.entity.Display.BlockDisplay disp = new net.minecraft.world.entity.Display.BlockDisplay(
                net.minecraft.world.entity.EntityType.BLOCK_DISPLAY, level);
        setDisplayState(disp, flyState);
        disp.setPos(source.getX(), source.getY(), source.getZ());
        level.addFreshEntity(disp);
        displayId = disp.getUUID();
    }

    private static void setDisplayState(net.minecraft.world.entity.Display.BlockDisplay disp, BlockState state) {
        try {
            java.lang.reflect.Method method = net.minecraft.world.entity.Display.BlockDisplay.class.getDeclaredMethod(
                    "setBlockState", BlockState.class);
            method.setAccessible(true);
            method.invoke(disp, state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private net.minecraft.world.entity.Display.BlockDisplay headDisplay() {
        if (displayId == null) {
            return null;
        }
        if (level.getEntity(displayId) instanceof net.minecraft.world.entity.Display.BlockDisplay disp
                && disp.isAlive()) {
            return disp;
        }
        return null;
    }

    /** Click again mid-flight: steer the block to the new aim. */
    public void redirect() {
        if (!progressing) {
            return;
        }
        if (head.distanceToSqr(player.position()) > RANGE * RANGE) {
            return;
        }
        settingUp = false;
        destination = currentTarget();
        flightTicks = 0;
        lastBob = 0.0;
        Vec3 fresh = destination.subtract(head);
        if (fresh.lengthSqr() >= 1.0e-6) {
            vel = fresh.normalize().scale(SPEED_PER_TICK);
        }
    }

    private Vec3 currentTarget() {
        LivingEntity victim = eyeVictim(RANGE, 2.5);
        if (victim != null) {
            return victim.position();
        }
        BlockPos hit = eyeTarget(RANGE);
        if (hit != null) {
            return Vec3.atCenterOf(hit);
        }
        Vec3 eye = player.getEyePosition();
        return eye.add(player.getLookAngle().normalize().scale(RANGE));
    }

    private boolean passable(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !state.isSolid();
    }

    @Override
    public boolean progress() {
        if (!alive(player) || !gate(owner) || !valid) {
            return false;
        }
        if (!progressing) {
            if (source == null
                    || !ID.equalsIgnoreCase(activeBound())
                    || source.distSqr(player.blockPosition()) > SELECT_RANGE * SELECT_RANGE) {
                return false;
            }
            return true;
        }
        budget += SPEED_PER_TICK;
        boolean steered = false;
        while (budget >= 0.5) {
            budget -= 0.5;
            if (!steered) {
                steered = true;
                flightTicks++;
                steerFlight();
            }
            if (!advanceHalfStep()) {
                impactBurst();
                return false;
            }
            if (hitEntities()) {
                return false;
            }
            if (checkArrival()) {
                impactBurst();
                return false;
            }
        }
        if (head.distanceToSqr(player.position()) > RANGE * RANGE * 2.25) {
            return false;
        }
        net.minecraft.world.entity.Display.BlockDisplay disp = headDisplay();
        if (disp == null) {
            return false;
        }
        disp.setPos(head.x - 0.5, head.y - 0.5, head.z - 0.5);
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, flyState), head.x, head.y, head.z, 2, 0.2, 0.2, 0.2, 0.01);
        return true;
    }

    /**
     * Once per tick: home toward the aim. For the first second the head also
     * bobs vertically (two smooth waves ramping in and out), then it flies
     * straight. The cruise rides above the aim line so the wobble never
     * grazes the ground, diving onto the target at the end. When the launch
     * pop-up reaches its height, snap onto the cruise toward the target.
     */
    private void steerFlight() {
        Vec3 goal = settingUp ? firstDestination : aimPoint();
        Vec3 want = goal.subtract(head);
        if (want.lengthSqr() < 1.0e-6) {
            want = vel;
        }
        want = want.normalize().scale(SPEED_PER_TICK);
        double blend = settingUp ? 0.4 : HOMING;
        vel = vel.add(want.subtract(vel).scale(blend));
        double bob = bobOffset();
        head = head.add(0.0, bob - lastBob, 0.0);
        lastBob = bob;
        if (settingUp
                && BlockPos.containing(head).getY()
                        == BlockPos.containing(firstDestination).getY()) {
            settingUp = false;
            Vec3 cruise = aimPoint().subtract(head);
            if (cruise.lengthSqr() >= 1.0e-6) {
                vel = cruise.normalize().scale(SPEED_PER_TICK);
            }
        }
    }

    /** Cruise above the aim line, diving straight at it near the target. */
    private Vec3 aimPoint() {
        if (head.distanceToSqr(destination) > DIVE_RANGE_SQR) {
            return destination.add(0.0, CRUISE_LIFT, 0.0);
        }
        return destination;
    }

    /** Vertical wobble for the first second of flight, else zero. */
    private double bobOffset() {
        if (flightTicks >= WOBBLE_TICKS) {
            return 0.0;
        }
        double t = (double) flightTicks / (double) WOBBLE_TICKS;
        return WOBBLE_AMPLITUDE * Math.sin(t * Math.PI * 4.0) * Math.sin(t * Math.PI);
    }

    /**
     * Reference entity hit, run after every block-step (before the arrival
     * check, exactly like the original): knockback along the gaze plus
     * damage, then the blast is spent.
     */
    private boolean hitEntities() {
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(head.subtract(1, 1, 1), head.add(1, 1, 1)).inflate(COLLISION_RADIUS - 1.0),
                LivingEntity::isAlive)) {
            if (entity.getUUID().equals(owner)) {
                continue;
            }
            if (entity.position().distanceToSqr(head) > COLLISION_RADIUS * COLLISION_RADIUS) {
                continue;
            }
            Vec3 knock = player.getLookAngle().normalize().scale(PUSH);
            entity.setDeltaMovement(knock);
            entity.hurtMarked = true;
            entity.hurt(player.damageSources().playerAttack(player), DAMAGE);
            return true;
        }
        return false;
    }

    /**
     * Advance the head half a block along the flight velocity. The fine
     * steps keep it from tunneling through thin walls or floors. Any solid
     * or liquid cell stops it cold; plants are broken through. The visible
     * head is the BlockDisplay, so no trail blocks are placed.
     */
    private boolean advanceHalfStep() {
        Vec3 step = vel.lengthSqr() < 1.0e-6
                ? new Vec3(0.0, -0.5, 0.0)
                : vel.normalize().scale(0.5);
        Vec3 next = head.add(step);
        BlockPos cell = BlockPos.containing(next);
        if (cell.equals(BlockPos.containing(head))) {
            head = next;
            return true;
        }
        if (!isFlightClear(cell)) {
            return false;
        }
        clearPlants(cell);
        head = next;
        return true;
    }

    private boolean checkArrival() {
        if (head.distanceToSqr(destination) < 2.25) {
            return true;
        }
        return vel.dot(destination.subtract(head)) < 0.0 && head.distanceToSqr(destination) < 25.0;
    }

    /** Impact feedback: a small burst of the flying block's own debris. */
    private void impactBurst() {
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, flyState), head.x, head.y, head.z, 8, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, head.x, head.y, head.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.7F, 1.0F);
    }

    private boolean isFlightClear(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isSolid() && state.getFluidState().isEmpty();
    }

    /** Break vegetation in the flight path, remembering it so it reverts. */
    private void clearPlants(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.isSolid() || !state.getFluidState().isEmpty()) {
            return;
        }
        String key = state.getBlock().getDescriptionId().toUpperCase(java.util.Locale.ROOT);
        if (key.contains("DOOR")
                || key.contains("FENCE_GATE")
                || key.contains("CHEST")
                || key.contains("BARREL")
                || key.contains("SHULKER")
                || key.contains("BED")) {
            return;
        }
        remember(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }

    @Override
    public void onRemove() {
        if (headDisplay() != null) {
            headDisplay().discard();
        }
        displayId = null;
        revertMovedEarth();
    }

    private static boolean alive(ServerPlayer player) {
        return player != null && !player.isRemoved() && !player.isDeadOrDying();
    }

    private static boolean gate(UUID owner) {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending != null && bending.hasElement(BendingElement.EARTH) && bending.isToggled();
    }

    private String activeBound() {
        BendingPlayer bending = BendingPlayer.get(owner);
        return bending == null ? "" : bending.boundAbility(player.getInventory().selected + 1);
    }

    private static void cool(UUID owner, ServerPlayer player, String id, long ticks) {
        BendingPlayer bending = BendingPlayer.get(owner);
        if (bending != null) {
            bending.setCooldown(id, player.level().getGameTime() + ticks);
        }
    }
}
