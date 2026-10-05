package com.phatabaniyan.avataruniverse;

import net.neoforged.neoforge.common.ModConfigSpec;

// AvatarUniverse configuration. COMMON type, synced via neoforge.mods.toml + config screen.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING = BUILDER.comment(
                    "Whether AvatarUniverse logs extra debug information on server start")
            .define("enableDebugLogging", false);

    public static final ModConfigSpec.BooleanValue ENABLE_BENDING = BUILDER.comment(
                    "Master switch for the ProjectKorra bending port (commands, manager, abilities)")
            .define("enableBending", true);

    public static final ModConfigSpec.IntValue WATERMANIP_COOLDOWN_TICKS = BUILDER.comment(
                    "WaterManipulation cooldown in ticks (Korra Cooldown 1000ms)")
            .defineInRange("watermanipCooldownTicks", 20, 0, 1200);

    public static final ModConfigSpec.DoubleValue WATERMANIP_RANGE = BUILDER.comment(
                    "WaterManipulation travel range in blocks")
            .defineInRange("watermanipRange", 18.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue WATERMANIP_DAMAGE =
            BUILDER.comment("WaterManipulation magic damage on hit").defineInRange("watermanipDamage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue WATERMANIP_SOURCE_FOCUS_RANGE = BUILDER.comment(
                    "How far (blocks) a selected water source keeps its focus shimmer and stays usable")
            .defineInRange("watermanipSourceFocusRange", 10.0, 4.0, 32.0);

    public static final ModConfigSpec.IntValue WATERMANIP_PLANT_REGROW_SECONDS = BUILDER.comment(
                    "Base delay before a plant source consumed by a cast grows back (Korra: 180s, regrows in 50-100% of this)")
            .defineInRange("watermanipPlantRegrowSeconds", 180, 1, 1200);

    // Torrent defaults mirror ProjectKorra's config.yml (Abilities.Water.Torrent.*).
    // Kept deviations: ring radius 4 (Korra 3, bigger wave per request), arc
    // capped at 150deg (Korra 220), issuing from tap-select instead of
    // BlockSource raycast. Skipped: day/night factors, AvatarState scaling,
    // region protection, bottle sourcing.
    public static final ModConfigSpec.DoubleValue TORRENT_RANGE = BUILDER.comment(
                    "Torrent wave travel range in blocks (flight time scales with range at 1 block/tick)")
            .defineInRange("torrentRange", 40.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue TORRENT_DAMAGE =
            BUILDER.comment("Torrent wave magic damage on first hit").defineInRange("torrentDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue TORRENT_SUCCESSIVE_DAMAGE = BUILDER.comment(
                    "Torrent wave magic damage on repeated hits")
            .defineInRange("torrentSuccessiveDamage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue TORRENT_MAX_HITS = BUILDER.comment(
                    "How many times the wave can damage the same entity")
            .defineInRange("torrentMaxHits", 2, 1, 10);

    public static final ModConfigSpec.IntValue TORRENT_MAX_LAYER = BUILDER.comment(
                    "How many layers the wave can climb over obstructions (Korra MaxLayer)")
            .defineInRange("torrentMaxLayer", 3, 0, 8);

    public static final ModConfigSpec.DoubleValue TORRENT_RADIUS =
            BUILDER.comment("Torrent ring radius in blocks").defineInRange("torrentRadius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.DoubleValue TORRENT_KNOCKBACK =
            BUILDER.comment("Torrent knockback strength").defineInRange("torrentKnockback", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue TORRENT_KNOCKUP =
            BUILDER.comment("Torrent upward pop on hit").defineInRange("torrentKnockup", 0.2, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue TORRENT_DEFLECT_DAMAGE = BUILDER.comment(
                    "Magic damage dealt by the forming ring defense")
            .defineInRange("torrentDeflectDamage", 1.0, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue TORRENT_FREEZE_RADIUS = BUILDER.comment(
                    "Radius around the frozen wave that traps entities")
            .defineInRange("torrentFreezeRadius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue TORRENT_FROZEN_REVERT_SECONDS = BUILDER.comment(
                    "How long frozen torrent ice persists before thawing (Korra RevertTime 60s)")
            .defineInRange("torrentFrozenRevertSeconds", 60, 1, 600);

    public static final ModConfigSpec.IntValue TORRENT_COOLDOWN_TICKS = BUILDER.comment(
                    "Torrent cooldown in ticks (Korra default: 0)")
            .defineInRange("torrentCooldownTicks", 0, 0, 1200);

    public static final ModConfigSpec.IntValue TORRENT_FORMED_TIMEOUT_TICKS = BUILDER.comment(
                    "How long a completed ring waits for launch before fizzling (0 = hold indefinitely like Korra)")
            .defineInRange("torrentFormedTimeoutTicks", 0, 0, 1200);

    public static final ModConfigSpec.IntValue TORRENT_SNEAK_WAIT_TICKS = BUILDER.comment(
                    "How long a readied torrent waits for sneak before fizzling (0 = wait indefinitely like Korra)")
            .defineInRange("torrentSneakWaitTicks", 0, 0, 1200);

    public static final ModConfigSpec.DoubleValue TORRENT_BURST_MAX_RADIUS = BUILDER.comment(
                    "Torrent burst maximum radius in blocks (Korra Wave.Radius 12)")
            .defineInRange("torrentBurstMaxRadius", 12.0, 2.0, 24.0);

    public static final ModConfigSpec.DoubleValue TORRENT_BURST_GROW = BUILDER.comment(
                    "Torrent burst growth per tick in blocks (Korra Wave.GrowSpeed 0.5)")
            .defineInRange("torrentBurstGrow", 0.5, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue TORRENT_BURST_KNOCKBACK = BUILDER.comment(
                    "Torrent burst radial knockback strength (Korra Wave.Knockback 1.5; burst knocks back only, no damage)")
            .defineInRange("torrentBurstKnockback", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue WATERSPOUT_HEIGHT =
            BUILDER.comment("WaterSpout column cap in blocks").defineInRange("waterspoutHeight", 16.0, 4.0, 32.0);

    public static final ModConfigSpec.DoubleValue WATERSPOUT_HOP_POWER = BUILDER.comment(
                    "WaterSpout hop launch strength along the look direction (Korra SpoutHop Power 0.85)")
            .defineInRange("waterspoutHopPower", 0.85, 0.0, 3.0);

    public static final ModConfigSpec.BooleanValue WATERSPOUT_SPIRAL = BUILDER.comment(
                    "WaterSpout rotating block-spiral ring around the column (Korra BlockSpiral)")
            .define("waterspoutSpiral", true);

    public static final ModConfigSpec.BooleanValue WATERSPOUT_PARTICLES = BUILDER.comment(
                    "WaterSpout splash spray and sounds (Korra Particles)")
            .define("waterspoutParticles", true);

    public static final ModConfigSpec.DoubleValue ICEWAVE_DAMAGE = BUILDER.comment(
                    "IceWave contact magic damage once per entity (Korra IceWave Damage 3)")
            .defineInRange("icewaveDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICEWAVE_SPHERE_RADIUS = BUILDER.comment(
                    "IceWave trapping sphere radius in blocks (Korra IceSphereRadius 2.5)")
            .defineInRange("icewaveSphereRadius", 2.5, 1.0, 8.0);

    public static final ModConfigSpec.IntValue ICEWAVE_REVERT_SECONDS = BUILDER.comment(
                    "How long IceWave ice persists before thawing (Korra RevertSphereTime 30s)")
            .defineInRange("icewaveRevertSeconds", 30, 1, 600);

    public static final ModConfigSpec.DoubleValue ICEWAVE_THAW_RADIUS = BUILDER.comment(
                    "IceWave ice thaws beyond this owner distance (Korra ThawRadius 10)")
            .defineInRange("icewaveThawRadius", 10.0, 4.0, 64.0);

    public static final ModConfigSpec.BooleanValue WATERARMS_ALLOW_PLANT_SOURCE = BUILDER.comment(
                    "WaterArms may start from plant/snow sources, consuming them (Korra AllowPlantSource)")
            .define("waterarmsAllowPlantSource", true);

    public static final ModConfigSpec.IntValue WATERARMS_INITIAL_LENGTH = BUILDER.comment(
                    "WaterArms forward reach past the shoulder joints in blocks (Korra InitialLength 4)")
            .defineInRange("waterarmsInitialLength", 4, 1, 12);

    public static final ModConfigSpec.DoubleValue WATERARMS_SOURCE_GRAB_RANGE = BUILDER.comment(
                    "WaterArms eye-ray source grab range in blocks (Korra SourceGrabRange 12)")
            .defineInRange("waterarmsSourceGrabRange", 12.0, 4.0, 32.0);

    public static final ModConfigSpec.IntValue WATERARMS_MAX_PUNCHES = BUILDER.comment(
                    "Damaging hits before the arms collapse (Korra MaxAttacks 10)")
            .defineInRange("waterarmsMaxPunches", 10, 0, 200);

    public static final ModConfigSpec.IntValue WATERARMS_MAX_USES = BUILDER.comment(
                    "Total sub-ability uses before the arms collapse (Korra MaxAlternateUsage 50)")
            .defineInRange("waterarmsMaxUses", 50, 0, 500);

    public static final ModConfigSpec.IntValue WATERARMS_COOLDOWN_TICKS = BUILDER.comment(
                    "WaterArms cooldown after removal in ticks (Korra Cooldown 20000ms)")
            .defineInRange("waterarmsCooldownTicks", 400, 0, 1200);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_MAX_LENGTH = BUILDER.comment(
                    "Whip max length in blocks (Korra Whip.MaxLength 12)")
            .defineInRange("waterarmsWhipMaxLength", 12, 4, 32);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_MAX_LENGTH_WEAK = BUILDER.comment(
                    "Whip max length from weak sources in blocks (Korra MaxLengthWeak 8)")
            .defineInRange("waterarmsWhipMaxLengthWeak", 8, 2, 16);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_PUNCH_MAX_LENGTH = BUILDER.comment(
                    "Punch whip max length in blocks (Korra Punch.MaxLength 8)")
            .defineInRange("waterarmsWhipPunchMaxLength", 8, 2, 16);

    public static final ModConfigSpec.DoubleValue WATERARMS_WHIP_PULL_MULTIPLIER = BUILDER.comment(
                    "Pull strength multiplier (Korra Pull.Multiplier 0.15)")
            .defineInRange("waterarmsWhipPullMultiplier", 0.15, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue WATERARMS_WHIP_PUNCH_DAMAGE = BUILDER.comment(
                    "Punch magic damage (Korra Punch.Damage 0.5)")
            .defineInRange("waterarmsWhipPunchDamage", 0.5, 0.0, 20.0);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_GRAB_DURATION_TICKS = BUILDER.comment(
                    "Grab hold duration in ticks (Korra Grab.Duration 3500ms)")
            .defineInRange("waterarmsWhipGrabDurationTicks", 70, 0, 600);

    public static final ModConfigSpec.BooleanValue WATERARMS_WHIP_USAGE_COOLDOWN_ENABLED = BUILDER.comment(
                    "Enable per-arm usage cooldowns on whip casts (Korra UsageCooldown.Enabled, default off)")
            .define("waterarmsWhipUsageCooldownEnabled", false);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_PULL_TICKS = BUILDER.comment(
                    "Pull arm cooldown in ticks (Korra 200ms)")
            .defineInRange("waterarmsWhipCooldownPullTicks", 4, 0, 100);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_PUNCH_TICKS = BUILDER.comment(
                    "Punch arm cooldown in ticks (Korra 200ms)")
            .defineInRange("waterarmsWhipCooldownPunchTicks", 4, 0, 100);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_GRAPPLE_TICKS = BUILDER.comment(
                    "Grapple arm cooldown in ticks (Korra 200ms)")
            .defineInRange("waterarmsWhipCooldownGrappleTicks", 4, 0, 100);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_GRAB_TICKS = BUILDER.comment(
                    "Grab arm cooldown in ticks (Korra 200ms)")
            .defineInRange("waterarmsWhipCooldownGrabTicks", 4, 0, 100);

    public static final ModConfigSpec.BooleanValue WATERARMS_GRAPPLE_RESPECT_REGIONS = BUILDER.comment(
                    "Stub for future claim hooks; always unprotected today (Korra Grapple.RespectRegions false)")
            .define("waterarmsGrappleRespectRegions", false);

    public static final ModConfigSpec.DoubleValue WATERARMS_SPEAR_RANGE = BUILDER.comment(
                    "Spear travel range in blocks (Korra Spear.Range 30)")
            .defineInRange("waterarmsSpearRange", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.IntValue WATERARMS_SPEAR_LENGTH = BUILDER.comment(
                    "Spear ice length in blocks on impact (Korra Spear.Length 18)")
            .defineInRange("waterarmsSpearLength", 18, 4, 40);

    public static final ModConfigSpec.DoubleValue WATERARMS_SPEAR_DAMAGE = BUILDER.comment(
                    "Spear contact damage (Korra Spear.Damage 3)")
            .defineInRange("waterarmsSpearDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.BooleanValue WATERARMS_SPEAR_DAMAGE_ENABLED = BUILDER.comment(
                    "Spear deals contact damage (Korra Spear.DamageEnabled)")
            .define("waterarmsSpearDamageEnabled", true);

    public static final ModConfigSpec.DoubleValue WATERARMS_SPEAR_SPHERE_RADIUS = BUILDER.comment(
                    "Spear victim ice-ball radius, 0 disables (Korra Spear.SphereRadius 2)")
            .defineInRange("waterarmsSpearSphereRadius", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue WATERARMS_SPEAR_DURATION_MS = BUILDER.comment(
                    "Spear ice lifetime in ms plus up to 500ms jitter (Korra Spear.Duration 4500)")
            .defineInRange("waterarmsSpearDurationMs", 4500, 0, 30000);

    public static final ModConfigSpec.IntValue WATERARMS_SPEAR_COOLDOWN_TICKS = BUILDER.comment(
                    "Spear arm cooldown in ticks (Korra 200ms)")
            .defineInRange("waterarmsSpearCooldownTicks", 4, 0, 100);

    public static final ModConfigSpec.DoubleValue HEALING_RANGE = BUILDER.comment(
                    "HealingWaters target range in blocks (Korra Range 5)")
            .defineInRange("healingRange", 5.0, 2.0, 32.0);

    public static final ModConfigSpec.IntValue HEALING_POTENCY = BUILDER.comment(
                    "HealingWaters regeneration amplifier (Korra PotionPotency 1)")
            .defineInRange("healingPotency", 1, 0, 5);

    public static final ModConfigSpec.BooleanValue HEALING_PARTICLES = BUILDER.comment(
                    "HealingWaters charge and ring particles (Korra EnableParticles)")
            .define("healingParticles", true);

    public static final ModConfigSpec.IntValue WATERBUBBLE_CLICK_DURATION_TICKS = BUILDER.comment(
                    "WaterBubble click-mode lifetime in ticks (Korra ClickDuration 2000ms)")
            .defineInRange("waterbubbleClickDurationTicks", 40, 10, 1200);

    public static final ModConfigSpec.DoubleValue WATERBUBBLE_RADIUS = BUILDER.comment(
                    "WaterBubble max radius in blocks (Korra Radius 4.0)")
            .defineInRange("waterbubbleRadius", 4.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue WATERBUBBLE_SPEED = BUILDER.comment(
                    "WaterBubble grow/shrink per tick in blocks (Korra Speed 0.5)")
            .defineInRange("waterbubbleSpeed", 0.5, 0.1, 2.0);

    public static final ModConfigSpec.BooleanValue WATERBUBBLE_MUST_START_ABOVE_WATER = BUILDER.comment(
                    "WaterBubble fizzles when the eyes start inside solid/liquid (Korra MustStartAboveWater, default false)")
            .define("waterbubbleMustStartAboveWater", false);

    public static final ModConfigSpec.IntValue FROSTBREATH_DURATION_TICKS = BUILDER.comment(
                    "FrostBreath max breath in ticks (JedCore Duration 3000ms)")
            .defineInRange("frostbreathDurationTicks", 60, 10, 600);

    public static final ModConfigSpec.IntValue FROSTBREATH_COOLDOWN_TICKS = BUILDER.comment(
                    "FrostBreath cooldown in ticks (JedCore Cooldown 15000ms)")
            .defineInRange("frostbreathCooldownTicks", 300, 0, 2400);

    public static final ModConfigSpec.IntValue FROSTBREATH_RANGE = BUILDER.comment(
                    "FrostBreath beam range in blocks (JedCore Range 10)")
            .defineInRange("frostbreathRange", 10, 2, 32);

    public static final ModConfigSpec.IntValue FROSTBREATH_PARTICLES = BUILDER.comment(
                    "FrostBreath snow particles per beam step (JedCore Particles 3)")
            .defineInRange("frostbreathParticles", 3, 0, 20);

    public static final ModConfigSpec.IntValue FROSTBREATH_FROST_DURATION_TICKS = BUILDER.comment(
                    "FrostBreath entity-cage ice lifetime in ticks (JedCore FrostDuration 5000ms)")
            .defineInRange("frostbreathFrostDurationTicks", 100, 0, 1200);

    public static final ModConfigSpec.IntValue FROSTBREATH_FROZEN_WATER_DURATION_TICKS = BUILDER.comment(
                    "FrostBreath frozen-water ice lifetime in ticks (JedCore FrozenWaterDuration 10000ms)")
            .defineInRange("frostbreathFrozenWaterDurationTicks", 200, 0, 2400);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_SNOW_ENABLED = BUILDER.comment(
                    "FrostBreath dusts the ground with snow (JedCore Snow)")
            .define("frostbreathSnowEnabled", true);

    public static final ModConfigSpec.IntValue FROSTBREATH_SNOW_DURATION_TICKS = BUILDER.comment(
                    "FrostBreath ground snow lifetime in ticks (JedCore SnowDuration 5000ms)")
            .defineInRange("frostbreathSnowDurationTicks", 100, 0, 1200);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_DAMAGE_ENABLED = BUILDER.comment(
                    "FrostBreath deals damage (JedCore Damage.Enabled defaults false; enabled here)")
            .define("frostbreathDamageEnabled", true);

    public static final ModConfigSpec.DoubleValue FROSTBREATH_PLAYER_DAMAGE = BUILDER.comment(
                    "FrostBreath damage to players (JedCore Damage.Player 1.0)")
            .defineInRange("frostbreathPlayerDamage", 1.0, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue FROSTBREATH_MOB_DAMAGE = BUILDER.comment(
                    "FrostBreath damage to mobs (JedCore Damage.Mob 2.0)")
            .defineInRange("frostbreathMobDamage", 2.0, 0.0, 20.0);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_SLOW_ENABLED = BUILDER.comment(
                    "FrostBreath slows breathed entities (JedCore Slow.Enabled)")
            .define("frostbreathSlowEnabled", true);

    public static final ModConfigSpec.IntValue FROSTBREATH_SLOW_DURATION_TICKS = BUILDER.comment(
                    "FrostBreath slowness duration in ticks (JedCore Slow.Duration 4000ms)")
            .defineInRange("frostbreathSlowDurationTicks", 80, 0, 1200);

    public static final ModConfigSpec.IntValue FROSTBREATH_SLOW_AMPLIFIER = BUILDER.comment(
                    "FrostBreath slowness amplifier (JedCore hardcodes 5)")
            .defineInRange("frostbreathSlowAmplifier", 5, 0, 10);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_RESTRICT_BIOMES = BUILDER.comment(
                    "FrostBreath fizzles in hot dry biomes (JedCore RestrictBiomes)")
            .define("frostbreathRestrictBiomes", true);

    public static final ModConfigSpec.DoubleValue ICEBLAST_DAMAGE =
            BUILDER.comment("IceBlast damage (Korra Damage 3)").defineInRange("iceblastDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICEBLAST_RANGE = BUILDER.comment(
                    "IceBlast range in blocks (Korra Range 20)")
            .defineInRange("iceblastRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ICEBLAST_COLLISION_RADIUS = BUILDER.comment(
                    "IceBlast hit radius in blocks (Korra CollisionRadius 1.0)")
            .defineInRange("iceblastCollisionRadius", 1.0, 0.2, 6.0);

    public static final ModConfigSpec.IntValue ICEBLAST_COOLDOWN_TICKS = BUILDER.comment(
                    "IceBlast cooldown in ticks (Korra Cooldown 1500ms)")
            .defineInRange("iceblastCooldownTicks", 30, 0, 2400);

    public static final ModConfigSpec.BooleanValue ICEBLAST_ALLOW_SNOW = BUILDER.comment(
                    "IceBlast accepts snow as source (Korra AllowSnow, default false)")
            .define("iceblastAllowSnow", false);

    public static final ModConfigSpec.DoubleValue ICESPIKE_BLAST_RANGE = BUILDER.comment(
                    "IceSpike blast range in blocks (Korra Blast.Range 20)")
            .defineInRange("icespikeBlastRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_BLAST_DAMAGE = BUILDER.comment(
                    "IceSpike blast damage (Korra Blast.Damage 1)")
            .defineInRange("icespikeBlastDamage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_BLAST_COLLISION_RADIUS = BUILDER.comment(
                    "IceSpike blast hit radius in blocks (Korra Blast.CollisionRadius 1.0)")
            .defineInRange("icespikeBlastCollisionRadius", 1.0, 0.2, 6.0);

    public static final ModConfigSpec.IntValue ICESPIKE_BLAST_COOLDOWN_TICKS = BUILDER.comment(
                    "IceSpike blast cooldown in ticks (Korra Blast.Cooldown 500ms)")
            .defineInRange("icespikeBlastCooldownTicks", 10, 0, 2400);

    public static final ModConfigSpec.IntValue ICESPIKE_BLAST_SLOW_DURATION_TICKS = BUILDER.comment(
                    "IceSpike blast slowness in ticks (Korra Blast.SlowDuration 70)")
            .defineInRange("icespikeBlastSlowDurationTicks", 70, 0, 1200);

    public static final ModConfigSpec.IntValue ICESPIKE_BLAST_SLOW_POTENCY = BUILDER.comment(
                    "IceSpike blast slowness amplifier (Korra Blast.SlowPotency 2)")
            .defineInRange("icespikeBlastSlowPotency", 2, 0, 10);

    public static final ModConfigSpec.DoubleValue ICESPIKE_DAMAGE =
            BUILDER.comment("IceSpike pillar damage (Korra Damage 2)").defineInRange("icespikeDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_PUSH = BUILDER.comment(
                    "IceSpike pillar launch strength (Korra Push 0.7)")
            .defineInRange("icespikePush", 0.7, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_RANGE = BUILDER.comment(
                    "IceSpike pillar reach in blocks (Korra Range 20)")
            .defineInRange("icespikeRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue ICESPIKE_HEIGHT = BUILDER.comment(
                    "IceSpike pillar height in blocks (Korra Height 6)")
            .defineInRange("icespikeHeight", 6, 1, 16);

    public static final ModConfigSpec.IntValue ICESPIKE_COOLDOWN_TICKS = BUILDER.comment(
                    "IceSpike pillar cooldown in ticks (Korra Cooldown 2000ms)")
            .defineInRange("icespikeCooldownTicks", 40, 0, 2400);

    public static final ModConfigSpec.IntValue ICESPIKE_SLOW_DURATION_TICKS = BUILDER.comment(
                    "IceSpike pillar slowness in ticks (Korra SlowDuration 70)")
            .defineInRange("icespikeSlowDurationTicks", 70, 0, 1200);

    public static final ModConfigSpec.IntValue ICESPIKE_SLOW_POTENCY = BUILDER.comment(
                    "IceSpike pillar slowness amplifier (Korra reads SlowPower, unconfigured upstream = 0)")
            .defineInRange("icespikeSlowPotency", 0, 0, 10);

    public static final ModConfigSpec.DoubleValue ICESPIKE_FIELD_DAMAGE = BUILDER.comment(
                    "IceSpike field pillar damage (Korra Field.Damage 2)")
            .defineInRange("icespikeFieldDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_FIELD_RADIUS = BUILDER.comment(
                    "IceSpike field scan radius in blocks (Korra Field.Radius 6)")
            .defineInRange("icespikeFieldRadius", 6.0, 2.0, 16.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_FIELD_KNOCKUP = BUILDER.comment(
                    "IceSpike field pillar launch strength (Korra Field.Knockup 1)")
            .defineInRange("icespikeFieldKnockup", 1.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue ICESPIKE_FIELD_COOLDOWN_TICKS = BUILDER.comment(
                    "IceSpike field cooldown in ticks (Korra Field.Cooldown 2000ms)")
            .defineInRange("icespikeFieldCooldownTicks", 40, 0, 2400);

    public static final ModConfigSpec.DoubleValue PHASE_SOURCE_RANGE = BUILDER.comment(
                    "PhaseChange gaze range in blocks (Korra SourceRange 12)")
            .defineInRange("phaseSourceRange", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue PHASE_FREEZE_RADIUS = BUILDER.comment(
                    "PhaseChange freeze radius in blocks (Korra Freeze.Radius 4)")
            .defineInRange("phaseFreezeRadius", 4.0, 1.0, 12.0);

    public static final ModConfigSpec.IntValue PHASE_FREEZE_DEPTH = BUILDER.comment(
                    "PhaseChange freeze depth in blocks (Korra Freeze.Depth 1)")
            .defineInRange("phaseFreezeDepth", 1, 1, 4);

    public static final ModConfigSpec.DoubleValue PHASE_FREEZE_CONTROL_RADIUS = BUILDER.comment(
                    "PhaseChange ice control radius in blocks (Korra Freeze.ControlRadius 25)")
            .defineInRange("phaseFreezeControlRadius", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue PHASE_FREEZE_COOLDOWN_TICKS = BUILDER.comment(
                    "PhaseChange freeze cooldown in ticks (Korra Freeze.Cooldown 250ms)")
            .defineInRange("phaseFreezeCooldownTicks", 5, 0, 1200);

    public static final ModConfigSpec.DoubleValue PHASE_MELT_RADIUS = BUILDER.comment(
                    "PhaseChange max melt radius in blocks (Korra Melt.Radius 7)")
            .defineInRange("phaseMeltRadius", 7.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue PHASE_MELT_SPEED = BUILDER.comment(
                    "PhaseChange melt speed (Korra Melt.Speed 8)")
            .defineInRange("phaseMeltSpeed", 8.0, 1.0, 40.0);

    public static final ModConfigSpec.IntValue PHASE_MELT_COOLDOWN_TICKS = BUILDER.comment(
                    "PhaseChange melt cooldown in ticks (Korra Melt.Cooldown 2000ms)")
            .defineInRange("phaseMeltCooldownTicks", 40, 0, 2400);

    public static final ModConfigSpec.BooleanValue PHASE_MELT_ALLOW_FLOW = BUILDER.comment(
                    "PhaseChange melt makes real flowing water (Korra Melt.AllowFlow, default true)")
            .define("phaseMeltAllowFlow", true);

    public static final ModConfigSpec.IntValue PHASE_SNOW_MELT_TICKS = BUILDER.comment(
                    "PhaseChange melted-snow lifetime in ticks (Korra 120s snow temps)")
            .defineInRange("phaseSnowMeltTicks", 2400, 20, 7200);

    public static final ModConfigSpec.DoubleValue BLOODBENDING_RANGE = BUILDER.comment(
                    "Bloodbending grip range in blocks (Korra Range 10)")
            .defineInRange("bloodbendingRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.IntValue BLOODBENDING_DURATION_TICKS = BUILDER.comment(
                    "Bloodbending max hold in ticks, 0 is endless (Korra Duration 0)")
            .defineInRange("bloodbendingDurationTicks", 0, 0, 7200);

    public static final ModConfigSpec.IntValue BLOODBENDING_COOLDOWN_TICKS = BUILDER.comment(
                    "Bloodbending cooldown in ticks (Korra Cooldown 3000ms)")
            .defineInRange("bloodbendingCooldownTicks", 60, 0, 2400);

    public static final ModConfigSpec.DoubleValue BLOODBENDING_KNOCKBACK = BUILDER.comment(
                    "Bloodbending throw strength (Korra Knockback 2)")
            .defineInRange("bloodbendingKnockback", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.BooleanValue BLOODBENDING_AFFECT_UNDEAD = BUILDER.comment(
                    "Bloodbending grips the undead (Korra CanBeUsedOnUndeadMobs, default true)")
            .define("bloodbendingAffectUndead", true);

    public static final ModConfigSpec.BooleanValue BLOODBENDING_AFFECT_BLOODBENDERS = BUILDER.comment(
                    "Bloodbending grips other waterbenders (Korra CanBloodbendOtherBloodbenders, default false)")
            .define("bloodbendingAffectBloodbenders", false);

    public static final ModConfigSpec.IntValue BLOODPUPPET_DISTANCE = BUILDER.comment(
                    "BloodPuppet grab range in blocks (JedCore Distance 6)")
            .defineInRange("bloodpuppetDistance", 6, 2, 24);

    public static final ModConfigSpec.IntValue BLOODPUPPET_HOLD_TICKS = BUILDER.comment(
                    "BloodPuppet max hold in ticks (JedCore HoldTime 10000ms)")
            .defineInRange("bloodpuppetHoldTicks", 200, 20, 2400);

    public static final ModConfigSpec.IntValue BLOODPUPPET_COOLDOWN_TICKS = BUILDER.comment(
                    "BloodPuppet cooldown in ticks (JedCore Cooldown 4000ms)")
            .defineInRange("bloodpuppetCooldownTicks", 80, 0, 2400);

    public static final ModConfigSpec.BooleanValue BLOODPUPPET_ALLOW_UNDEAD = BUILDER.comment(
                    "BloodPuppet grabs the undead (JedCore UndeadMobs, default true)")
            .define("bloodpuppetAllowUndead", true);

    public static final ModConfigSpec.BooleanValue BLOODPUPPET_IGNORE_WALLS = BUILDER.comment(
                    "BloodPuppet grabs through walls (JedCore IgnoreWalls, default false)")
            .define("bloodpuppetIgnoreWalls", false);

    public static final ModConfigSpec.BooleanValue BLOODPUPPET_AFFECT_BLOODBENDERS = BUILDER.comment(
                    "BloodPuppet grabs other waterbenders (JedCore AffectBloodbenders, default false)")
            .define("bloodpuppetAffectBloodbenders", false);

    public static final ModConfigSpec.IntValue DRAIN_DURATION_TICKS = BUILDER.comment(
                    "Drain fill-mode duration in ticks (JedCore Duration 2000ms, extended so distant motes arrive)")
            .defineInRange("drainDurationTicks", 200, 10, 2400);

    public static final ModConfigSpec.IntValue DRAIN_COOLDOWN_TICKS = BUILDER.comment(
                    "Drain cooldown in ticks (JedCore Cooldown 2000ms)")
            .defineInRange("drainCooldownTicks", 40, 0, 2400);

    public static final ModConfigSpec.DoubleValue DRAIN_ABSORB_SPEED = BUILDER.comment(
                    "Drain mote speed in blocks per tick (JedCore AbsorbSpeed 0.1)")
            .defineInRange("drainAbsorbSpeed", 0.1, 0.01, 2.0);

    public static final ModConfigSpec.IntValue DRAIN_ABSORB_CHANCE = BUILDER.comment(
                    "Drain one-in-N cell chance (JedCore AbsorbChance 20)")
            .defineInRange("drainAbsorbChance", 20, 1, 200);

    public static final ModConfigSpec.IntValue DRAIN_ABSORB_RATE = BUILDER.comment(
                    "Drain motes per fill or charge (JedCore AbsorbRate 6, faster gathering)")
            .defineInRange("drainAbsorbRate", 4, 1, 64);

    public static final ModConfigSpec.IntValue DRAIN_RADIUS = BUILDER.comment(
                    "Drain sampling radius in blocks (JedCore Radius 6, widened)")
            .defineInRange("drainRadius", 8, 2, 16);

    public static final ModConfigSpec.IntValue DRAIN_HOLD_RANGE = BUILDER.comment(
                    "Drain blast origin reach in blocks (JedCore HoldRange 2)")
            .defineInRange("drainHoldRange", 2, 1, 8);

    public static final ModConfigSpec.BooleanValue DRAIN_ALLOW_RAIN = BUILDER.comment(
                    "Drain draws motes from open rain (JedCore AllowRainSource, default true)")
            .define("drainAllowRain", true);

    public static final ModConfigSpec.IntValue DRAIN_MAX_BLASTS = BUILDER.comment(
                    "Drain blasts per gathering (JedCore MaxBlasts 4)")
            .defineInRange("drainMaxBlasts", 4, 1, 16);

    public static final ModConfigSpec.DoubleValue DRAIN_BLAST_RANGE = BUILDER.comment(
                    "Drain blast range in blocks (JedCore BlastRange 20)")
            .defineInRange("drainBlastRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue DRAIN_BLAST_DAMAGE = BUILDER.comment(
                    "Drain blast damage (JedCore BlastDamage 1.5, empowered)")
            .defineInRange("drainBlastDamage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue DRAIN_BLAST_SPEED = BUILDER.comment(
                    "Drain blast speed in blocks per tick (JedCore BlastSpeed 1, quickened)")
            .defineInRange("drainBlastSpeed", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.IntValue DRAIN_REGEN_SECONDS = BUILDER.comment(
                    "Drain plant and water regrow delay in seconds (JedCore RegenDelay 15000ms)")
            .defineInRange("drainRegenSeconds", 15, 1, 300);

    public static final ModConfigSpec.IntValue ICECLAWS_CHARGE_TICKS = BUILDER.comment(
                    "IceClaws charge time in ticks (JedCore ChargeTime 1000ms)")
            .defineInRange("iceclawsChargeTicks", 20, 0, 400);

    public static final ModConfigSpec.DoubleValue ICECLAWS_RANGE = BUILDER.comment(
                    "IceClaws throw range in blocks (JedCore Range 10)")
            .defineInRange("iceclawsRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue ICECLAWS_THROW_SPEED = BUILDER.comment(
                    "IceClaws throw speed in blocks per tick (JedCore Throw.Speed 1.0)")
            .defineInRange("iceclawsThrowSpeed", 1.0, 0.5, 6.0);

    public static final ModConfigSpec.BooleanValue ICECLAWS_THROWABLE = BUILDER.comment(
                    "IceClaws can be thrown (JedCore Throwable, default true)")
            .define("iceclawsThrowable", true);

    public static final ModConfigSpec.BooleanValue ICECLAWS_THROW_COOLDOWN_ON_THROW = BUILDER.comment(
                    "IceClaws cools on throw (JedCore Throw.CooldownOnThrow, default true)")
            .define("iceclawsThrowCooldownOnThrow", true);

    public static final ModConfigSpec.IntValue ICECLAWS_THROW_COOLDOWN_TICKS = BUILDER.comment(
                    "IceClaws throw cooldown in ticks (JedCore Throw.Cooldown 4000ms)")
            .defineInRange("iceclawsThrowCooldownTicks", 80, 0, 2400);

    public static final ModConfigSpec.DoubleValue ICECLAWS_THROW_DAMAGE = BUILDER.comment(
                    "IceClaws throw damage (JedCore Throw.Damage 2.0)")
            .defineInRange("iceclawsThrowDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ICECLAWS_THROW_SLOW_TICKS = BUILDER.comment(
                    "IceClaws throw slowness in ticks (JedCore Throw.SlowDuration 5000ms)")
            .defineInRange("iceclawsThrowSlowTicks", 100, 0, 1200);

    public static final ModConfigSpec.IntValue ICECLAWS_THROW_SLOW_LEVEL = BUILDER.comment(
                    "IceClaws throw slowness amplifier (JedCore Throw.Slowness 3)")
            .defineInRange("iceclawsThrowSlowLevel", 3, 0, 10);

    public static final ModConfigSpec.IntValue ICECLAWS_PUNCH_COOLDOWN_TICKS = BUILDER.comment(
                    "IceClaws punch cooldown in ticks (JedCore Punch.Cooldown 4000ms)")
            .defineInRange("iceclawsPunchCooldownTicks", 80, 0, 2400);

    public static final ModConfigSpec.DoubleValue ICECLAWS_PUNCH_DAMAGE = BUILDER.comment(
                    "IceClaws punch damage (JedCore Punch.Damage 2.0)")
            .defineInRange("iceclawsPunchDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ICECLAWS_PUNCH_SLOW_TICKS = BUILDER.comment(
                    "IceClaws punch slowness in ticks (JedCore Punch.SlowDuration 5000ms)")
            .defineInRange("iceclawsPunchSlowTicks", 100, 0, 1200);

    public static final ModConfigSpec.IntValue ICECLAWS_PUNCH_SLOW_LEVEL = BUILDER.comment(
                    "IceClaws punch slowness amplifier (JedCore Punch.Slowness 3)")
            .defineInRange("iceclawsPunchSlowLevel", 3, 0, 10);

    public static final ModConfigSpec.IntValue ICEWALL_COOLDOWN_TICKS = BUILDER.comment(
                    "IceWall cooldown in ticks (JedCore Cooldown 4000ms)")
            .defineInRange("icewallCooldownTicks", 80, 0, 2400);

    public static final ModConfigSpec.IntValue ICEWALL_WIDTH =
            BUILDER.comment("IceWall width in blocks (JedCore Width 6)").defineInRange("icewallWidth", 6, 2, 16);

    public static final ModConfigSpec.IntValue ICEWALL_MAX_HEIGHT = BUILDER.comment(
                    "IceWall arch peak in blocks (JedCore MaxHeight 5)")
            .defineInRange("icewallMaxHeight", 5, 1, 12);

    public static final ModConfigSpec.IntValue ICEWALL_MIN_HEIGHT = BUILDER.comment(
                    "IceWall arch edge in blocks (JedCore MinHeight 3)")
            .defineInRange("icewallMinHeight", 3, 1, 12);

    public static final ModConfigSpec.IntValue ICEWALL_RANGE =
            BUILDER.comment("IceWall source range in blocks (JedCore Range 8)").defineInRange("icewallRange", 8, 2, 24);

    public static final ModConfigSpec.DoubleValue ICEWALL_DAMAGE = BUILDER.comment(
                    "IceWall collapse damage (JedCore Damage 4.0)")
            .defineInRange("icewallDamage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICEWALL_DAMAGE_RADIUS = BUILDER.comment(
                    "IceWall collapse hurt radius in blocks (JedCore DamageRadius 2.5)")
            .defineInRange("icewallDamageRadius", 2.5, 0.5, 8.0);

    public static final ModConfigSpec.BooleanValue ICEWALL_LIFETIME_ENABLED = BUILDER.comment(
                    "IceWall auto-collapses on a timer (JedCore LifeTime.Enabled, default false)")
            .define("icewallLifetimeEnabled", false);

    public static final ModConfigSpec.IntValue ICEWALL_LIFETIME_TICKS = BUILDER.comment(
                    "IceWall lifetime in ticks (JedCore LifeTime.Duration 10000ms)")
            .defineInRange("icewallLifetimeTicks", 200, 20, 7200);

    public static final ModConfigSpec.IntValue WAKEFISHING_COOLDOWN_TICKS = BUILDER.comment(
                    "WakeFishing cooldown in ticks (JedCore Cooldown 10000ms)")
            .defineInRange("wakefishingCooldownTicks", 200, 0, 2400);

    public static final ModConfigSpec.IntValue WAKEFISHING_DURATION_TICKS = BUILDER.comment(
                    "WakeFishing max duration in ticks (JedCore Duration 20000ms)")
            .defineInRange("wakefishingDurationTicks", 400, 20, 2400);

    public static final ModConfigSpec.IntValue WAKEFISHING_RANGE = BUILDER.comment(
                    "WakeFishing source range in blocks (JedCore Range 5)")
            .defineInRange("wakefishingRange", 5, 2, 16);

    public static final ModConfigSpec.IntValue RAZORLEAF_COOLDOWN_TICKS = BUILDER.comment(
                    "RazorLeaf cooldown in ticks (Addons Cooldown 3000ms)")
            .defineInRange("razorleafCooldownTicks", 60, 0, 2400);

    public static final ModConfigSpec.DoubleValue RAZORLEAF_DAMAGE =
            BUILDER.comment("RazorLeaf damage (Addons Damage 2)").defineInRange("razorleafDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue RAZORLEAF_RADIUS = BUILDER.comment(
                    "RazorLeaf shred radius in blocks (Addons Radius 0.7)")
            .defineInRange("razorleafRadius", 0.7, 0.2, 4.0);

    public static final ModConfigSpec.DoubleValue RAZORLEAF_RANGE = BUILDER.comment(
                    "RazorLeaf range in blocks (Addons Range 24)")
            .defineInRange("razorleafRange", 24.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue RAZORLEAF_MAX_RECALLS = BUILDER.comment(
                    "RazorLeaf mid-flight re-steers (Addons MaxRecalls 3)")
            .defineInRange("razorleafMaxRecalls", 3, 0, 12);

    public static final ModConfigSpec.IntValue RAZORLEAF_PARTICLES = BUILDER.comment(
                    "RazorLeaf spiral density (Addons Particles 300)")
            .defineInRange("razorleafParticles", 150, 10, 600);

    public static final ModConfigSpec.DoubleValue ICECRAWL_DAMAGE =
            BUILDER.comment("IceCrawl damage (Hyperion Damage 5.0)").defineInRange("icecrawlDamage", 5.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ICECRAWL_COOLDOWN_TICKS = BUILDER.comment(
                    "IceCrawl cooldown in ticks (Hyperion Cooldown 5000ms)")
            .defineInRange("icecrawlCooldownTicks", 100, 0, 2400);

    public static final ModConfigSpec.IntValue ICECRAWL_RANGE = BUILDER.comment(
                    "IceCrawl travel range in blocks (Hyperion Range 24)")
            .defineInRange("icecrawlRange", 24, 4, 64);

    public static final ModConfigSpec.IntValue ICECRAWL_SELECT_RANGE = BUILDER.comment(
                    "IceCrawl source range in blocks (Hyperion SelectRange 8)")
            .defineInRange("icecrawlSelectRange", 8, 2, 24);

    public static final ModConfigSpec.IntValue ICECRAWL_FREEZE_TICKS = BUILDER.comment(
                    "IceCrawl victim root in ticks (Hyperion FreezeDuration 2000ms)")
            .defineInRange("icecrawlFreezeTicks", 40, 10, 1200);

    public static final ModConfigSpec.IntValue ICECRAWL_ICE_TICKS =
            BUILDER.comment("IceCrawl ice lifetime after freeze ends").defineInRange("icecrawlIceTicks", 160, 20, 2400);

    // -- PlantArmor (ProjectAddons MultiAbility) --
    public static final ModConfigSpec.IntValue PLANTARMOR_COOLDOWN_TICKS = BUILDER.comment(
                    "PlantArmor cooldown after removal (Korra Cooldown 10000)")
            .defineInRange("plantarmorCooldownTicks", 200, 0, 2400);
    public static final ModConfigSpec.IntValue PLANTARMOR_DURATION_TICKS = BUILDER.comment(
                    "PlantArmor duration in ticks; 0 = until durability spent (Korra Duration -1)")
            .defineInRange("plantarmorDurationTicks", 0, -1, 72000);
    public static final ModConfigSpec.DoubleValue PLANTARMOR_DURABILITY = BUILDER.comment(
                    "PlantArmor durability pool (Korra 2000)")
            .defineInRange("plantarmorDurability", 2000.0, 100.0, 100000.0);
    public static final ModConfigSpec.IntValue PLANTARMOR_SELECT_RANGE = BUILDER.comment(
                    "PlantArmor plant search range (Korra SelectRange 9)")
            .defineInRange("plantarmorSelectRange", 9, 2, 24);
    public static final ModConfigSpec.IntValue PLANTARMOR_REQUIRED_PLANTS = BUILDER.comment(
                    "Plants to weave for a shell (Korra RequiredPlants 14)")
            .defineInRange("plantarmorRequiredPlants", 14, 1, 64);
    public static final ModConfigSpec.IntValue PLANTARMOR_BOOST_SWIM =
            BUILDER.comment("Dolphin's Grace level (Korra Boost.Swim 3)").defineInRange("plantarmorBoostSwim", 3, 1, 5);
    public static final ModConfigSpec.IntValue PLANTARMOR_BOOST_SPEED =
            BUILDER.comment("Speed Boost level (Korra Boost.Speed 2)").defineInRange("plantarmorBoostSpeed", 2, 1, 5);
    public static final ModConfigSpec.IntValue PLANTARMOR_BOOST_JUMP =
            BUILDER.comment("Jump Boost level (Korra Boost.Jump 2)").defineInRange("plantarmorBoostJump", 2, 1, 5);

    public static final ModConfigSpec.IntValue VINEWHIP_COST =
            BUILDER.comment("VineWhip shell cost").defineInRange("vinewhipCost", 50, 0, 2000);
    public static final ModConfigSpec.IntValue VINEWHIP_COOLDOWN_TICKS =
            BUILDER.comment("VineWhip cooldown").defineInRange("vinewhipCooldownTicks", 40, 0, 1200);
    public static final ModConfigSpec.DoubleValue VINEWHIP_DAMAGE =
            BUILDER.comment("VineWhip damage").defineInRange("vinewhipDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue VINEWHIP_RANGE =
            BUILDER.comment("VineWhip range").defineInRange("vinewhipRange", 18, 2, 64);
    public static final ModConfigSpec.IntValue VINEWHIP_SPEED =
            BUILDER.comment("VineWhip growth steps/tick").defineInRange("vinewhipSpeed", 3, 1, 16);

    public static final ModConfigSpec.IntValue TANGLE_COST =
            BUILDER.comment("Tangle shell cost").defineInRange("tangleCost", 200, 0, 2000);
    public static final ModConfigSpec.IntValue TANGLE_COOLDOWN_TICKS =
            BUILDER.comment("Tangle cooldown").defineInRange("tangleCooldownTicks", 140, 0, 1200);
    public static final ModConfigSpec.DoubleValue TANGLE_RADIUS =
            BUILDER.comment("Tangle cone radius").defineInRange("tangleRadius", 0.45, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue TANGLE_RANGE =
            BUILDER.comment("Tangle projectile range").defineInRange("tangleRange", 18.0, 2.0, 64.0);
    public static final ModConfigSpec.IntValue TANGLE_DURATION_TICKS = BUILDER.comment(
                    "Tangle victim stop-duration (Korra Tangle Duration 3000ms)")
            .defineInRange("tangleDurationTicks", 60, 5, 1200);

    public static final ModConfigSpec.IntValue GRAPPLE_COST =
            BUILDER.comment("Grapple shell cost").defineInRange("grappleCost", 100, 0, 2000);
    public static final ModConfigSpec.IntValue GRAPPLE_COOLDOWN_TICKS =
            BUILDER.comment("Grapple cooldown").defineInRange("grappleCooldownTicks", 40, 0, 1200);
    public static final ModConfigSpec.IntValue GRAPPLE_RANGE =
            BUILDER.comment("Grapple latch range").defineInRange("grappleRange", 25, 4, 64);
    public static final ModConfigSpec.DoubleValue GRAPPLE_SPEED =
            BUILDER.comment("Grapple reel speed").defineInRange("grappleSpeed", 1.24, 0.2, 4.0);

    public static final ModConfigSpec.IntValue LEAP_COST =
            BUILDER.comment("Leap shell cost").defineInRange("leapCost", 100, 0, 2000);
    public static final ModConfigSpec.IntValue LEAP_COOLDOWN_TICKS =
            BUILDER.comment("Leap cooldown").defineInRange("leapCooldownTicks", 50, 0, 1200);
    public static final ModConfigSpec.DoubleValue LEAP_POWER =
            BUILDER.comment("Leap launch power").defineInRange("leapPower", 1.4, 0.2, 4.0);

    public static final ModConfigSpec.IntValue LEAFSHIELD_COST =
            BUILDER.comment("LeafShield shell cost").defineInRange("leafshieldCost", 100, 0, 2000);
    public static final ModConfigSpec.IntValue LEAFSHIELD_COOLDOWN_TICKS =
            BUILDER.comment("LeafShield cooldown").defineInRange("leafshieldCooldownTicks", 30, 0, 1200);
    public static final ModConfigSpec.IntValue LEAFSHIELD_RADIUS =
            BUILDER.comment("LeafShield radius").defineInRange("leafshieldRadius", 2, 1, 6);

    public static final ModConfigSpec.IntValue LEAFDOME_COST =
            BUILDER.comment("LeafDome shell cost").defineInRange("leafdomeCost", 400, 0, 2000);
    public static final ModConfigSpec.IntValue LEAFDOME_COOLDOWN_TICKS =
            BUILDER.comment("LeafDome cooldown").defineInRange("leafdomeCooldownTicks", 100, 0, 1200);
    public static final ModConfigSpec.IntValue LEAFDOME_RADIUS =
            BUILDER.comment("LeafDome radius").defineInRange("leafdomeRadius", 3, 1, 8);

    public static final ModConfigSpec.IntValue REGENERATE_COOLDOWN_TICKS =
            BUILDER.comment("Regenerate finish cd").defineInRange("regenerateCooldownTicks", 200, 0, 2000);
    public static final ModConfigSpec.IntValue REGENERATE_AMOUNT =
            BUILDER.comment("Regenerate shell repair").defineInRange("regenerateAmount", 150, 10, 2000);

    public static final ModConfigSpec.IntValue RAZORLEAF_SUB_COST =
            BUILDER.comment("RazorLeaf sub shell cost").defineInRange("razorleafSubCost", 150, 0, 2000);

    public static final ModConfigSpec.IntValue ACCRETION_COOLDOWN_TICKS =
            BUILDER.comment("Accretion cooldown").defineInRange("accretionCooldownTicks", 200, 0, 2400);
    public static final ModConfigSpec.DoubleValue ACCRETION_DAMAGE =
            BUILDER.comment("Accretion damage per hit").defineInRange("accretionDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue ACCRETION_BLOCKS =
            BUILDER.comment("Max blocks per salvo").defineInRange("accretionBlocks", 8, 1, 32);
    public static final ModConfigSpec.IntValue ACCRETION_SELECT_RANGE =
            BUILDER.comment("Accretion source radius").defineInRange("accretionSelectRange", 6, 2, 24);
    public static final ModConfigSpec.IntValue ACCRETION_REVERT_TIME_TICKS =
            BUILDER.comment("Landed TempBlock lifetime ticks").defineInRange("accretionRevertTimeTicks", 400, 20, 1200);
    public static final ModConfigSpec.DoubleValue ACCRETION_THROW_SPEED =
            BUILDER.comment("Accretion launch speed").defineInRange("accretionThrowSpeed", 1.6, 0.5, 4.0);

    // -- FireClick (11 click fire abilities) --
    public static final ModConfigSpec.IntValue FIREJET_DURATION_TICKS = BUILDER.comment(
                    "FireJet max ride in ticks (Reference Duration 2000ms)")
            .defineInRange("firejetDurationTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue FIREJET_COOLDOWN_TICKS = BUILDER.comment(
                    "FireJet cooldown in ticks (Reference Cooldown 7000ms)")
            .defineInRange("firejetCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIREJET_SPEED = BUILDER.comment(
                    "FireJet thrust speed in blocks per tick (Reference Speed 0.8)")
            .defineInRange("firejetSpeed", 0.8, 0.0, 6.0);
    public static final ModConfigSpec.IntValue FIREJET_RESIST_TICKS = BUILDER.comment(
                    "FireJet launch fire-resistance cover in ticks")
            .defineInRange("firejetResistTicks", 30, 0, 1200);

    public static final ModConfigSpec.IntValue FIREKICK_COOLDOWN_TICKS = BUILDER.comment(
                    "FireKick cooldown in ticks (Reference Cooldown 4000ms)")
            .defineInRange("firekickCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIREKICK_DAMAGE = BUILDER.comment(
                    "FireKick magic damage on hit (Reference Damage 3)")
            .defineInRange("firekickDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIREKICK_SPEED = BUILDER.comment(
                    "FireKick head speed in blocks per tick (Reference Speed 1.2)")
            .defineInRange("firekickSpeed", 1.2, 0.0, 6.0);
    public static final ModConfigSpec.DoubleValue FIREKICK_RANGE = BUILDER.comment(
                    "FireKick head travel range in blocks (Reference Range 20)")
            .defineInRange("firekickRange", 20.0, 1.0, 96.0);
    public static final ModConfigSpec.DoubleValue FIREKICK_PUSH = BUILDER.comment(
                    "FireKick shove strength (Reference Push 1.0)")
            .defineInRange("firekickPush", 1.0, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue FIREKICK_RADIUS = BUILDER.comment(
                    "FireKick head hit radius in blocks (Reference Radius 1.5)")
            .defineInRange("firekickRadius", 1.5, 0.2, 8.0);
    public static final ModConfigSpec.IntValue FIREKICK_FIRE_SECONDS =
            BUILDER.comment("FireKick ignite duration in seconds").defineInRange("firekickFireSeconds", 3, 0, 60);

    public static final ModConfigSpec.IntValue FIRESPIN_COOLDOWN_TICKS = BUILDER.comment(
                    "FireSpin cooldown in ticks (Reference Cooldown 5000ms)")
            .defineInRange("firespinCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIRESPIN_DAMAGE = BUILDER.comment(
                    "FireSpin magic damage on hit (Reference Damage 3)")
            .defineInRange("firespinDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIRESPIN_SPEED = BUILDER.comment(
                    "FireSpin head speed in blocks per tick (Reference Speed 1.0)")
            .defineInRange("firespinSpeed", 1.0, 0.0, 6.0);
    public static final ModConfigSpec.DoubleValue FIRESPIN_RANGE = BUILDER.comment(
                    "FireSpin head travel range in blocks (Reference Range 16)")
            .defineInRange("firespinRange", 16.0, 1.0, 96.0);
    public static final ModConfigSpec.DoubleValue FIRESPIN_PUSH = BUILDER.comment(
                    "FireSpin shove strength (Reference Push 1.5)")
            .defineInRange("firespinPush", 1.5, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue FIRESPIN_RADIUS = BUILDER.comment(
                    "FireSpin head hit radius in blocks (Reference Radius 1.5)")
            .defineInRange("firespinRadius", 1.5, 0.2, 8.0);
    public static final ModConfigSpec.IntValue FIRESPIN_FIRE_SECONDS =
            BUILDER.comment("FireSpin ignite duration in seconds").defineInRange("firespinFireSeconds", 3, 0, 60);
    public static final ModConfigSpec.DoubleValue FIRESPIN_SCORCH_RADIUS = BUILDER.comment(
                    "FireSpin launch ground-scorch radius in blocks")
            .defineInRange("firespinScorchRadius", 6.0, 1.0, 16.0);
    public static final ModConfigSpec.IntValue FIRESPIN_SCORCH_MAX =
            BUILDER.comment("FireSpin launch max ground fires lit").defineInRange("firespinScorchMax", 32, 0, 128);
    public static final ModConfigSpec.IntValue FIRESPIN_SCORCH_TRIES = BUILDER.comment(
                    "FireSpin launch ground-scorch sampling tries")
            .defineInRange("firespinScorchTries", 110, 1, 1000);

    public static final ModConfigSpec.IntValue FIREWHEEL_COOLDOWN_TICKS = BUILDER.comment(
                    "FireWheel cooldown in ticks (Reference Cooldown 5000ms)")
            .defineInRange("firewheelCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIREWHEEL_DAMAGE = BUILDER.comment(
                    "FireWheel magic damage on hit (Reference Damage 4)")
            .defineInRange("firewheelDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIREWHEEL_SPEED = BUILDER.comment(
                    "FireWheel roll speed in blocks per tick (Reference Speed 1.0)")
            .defineInRange("firewheelSpeed", 1.0, 0.0, 6.0);
    public static final ModConfigSpec.DoubleValue FIREWHEEL_RANGE = BUILDER.comment(
                    "FireWheel travel range in blocks (Reference Range 20)")
            .defineInRange("firewheelRange", 20.0, 1.0, 96.0);
    public static final ModConfigSpec.DoubleValue FIREWHEEL_HEIGHT = BUILDER.comment(
                    "FireWheel height in blocks, radius is half this (Reference Height 4)")
            .defineInRange("firewheelHeight", 4.0, 1.0, 16.0);
    public static final ModConfigSpec.IntValue FIREWHEEL_FIRE_SECONDS = BUILDER.comment(
                    "FireWheel ignite duration in seconds (Reference FireTicks 3)")
            .defineInRange("firewheelFireSeconds", 3, 0, 60);
    public static final ModConfigSpec.DoubleValue FIREWHEEL_PUSH =
            BUILDER.comment("FireWheel shove strength along the roll").defineInRange("firewheelPush", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.IntValue FIREDISC_COOLDOWN_TICKS = BUILDER.comment(
                    "FireDisc cooldown in ticks (Reference Cooldown 3000ms)")
            .defineInRange("firediscCooldownTicks", 60, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIREDISC_DAMAGE = BUILDER.comment(
                    "FireDisc magic damage on hit (Reference Damage 4)")
            .defineInRange("firediscDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIREDISC_RANGE = BUILDER.comment(
                    "FireDisc travel range in blocks (Reference Range 20)")
            .defineInRange("firediscRange", 20.0, 1.0, 96.0);
    public static final ModConfigSpec.DoubleValue FIREDISC_SPEED =
            BUILDER.comment("FireDisc flight step in blocks per tick").defineInRange("firediscSpeed", 1.0, 0.1, 6.0);
    public static final ModConfigSpec.DoubleValue FIREDISC_KNOCKBACK = BUILDER.comment(
                    "FireDisc knockback strength along the heading (Reference Knockback 1.5)")
            .defineInRange("firediscKnockback", 1.5, 0.0, 5.0);
    public static final ModConfigSpec.IntValue FIREDISC_FIRE_SECONDS =
            BUILDER.comment("FireDisc ignite duration in seconds").defineInRange("firediscFireSeconds", 3, 0, 60);
    public static final ModConfigSpec.DoubleValue FIREDISC_HIT_RADIUS =
            BUILDER.comment("FireDisc hit radius in blocks").defineInRange("firediscHitRadius", 1.5, 0.2, 8.0);
    public static final ModConfigSpec.IntValue FIREDISC_REVERT_TICKS = BUILDER.comment(
                    "FireDisc cut-block regen delay in ticks (Reference 10000ms)")
            .defineInRange("firediscRevertTicks", 200, 0, 7200);
    public static final ModConfigSpec.BooleanValue FIREDISC_CONTROLLABLE = BUILDER.comment(
                    "FireDisc steers toward the gaze mid-flight (Reference Controllable true)")
            .define("firediscControllable", true);
    public static final ModConfigSpec.BooleanValue FIREDISC_REVERT = BUILDER.comment(
                    "FireDisc cut blocks grow back (Reference Revert true)")
            .define("firediscRevert", true);
    public static final ModConfigSpec.BooleanValue FIREDISC_DROP = BUILDER.comment(
                    "FireDisc cut blocks drop their item (Reference Drop true)")
            .define("firediscDrop", true);

    public static final ModConfigSpec.IntValue FIREBALL_COOLDOWN_TICKS = BUILDER.comment(
                    "FireBall cooldown in ticks (Reference Cooldown 2500ms)")
            .defineInRange("fireballCooldownTicks", 50, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIREBALL_DAMAGE = BUILDER.comment(
                    "FireBall magic damage on hit (Reference Damage 4)")
            .defineInRange("fireballDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIREBALL_RANGE = BUILDER.comment(
                    "FireBall travel range in blocks (Reference Range 20)")
            .defineInRange("fireballRange", 20.0, 1.0, 96.0);
    public static final ModConfigSpec.DoubleValue FIREBALL_SPEED = BUILDER.comment(
                    "FireBall flight speed in blocks per tick (Reference Speed 2)")
            .defineInRange("fireballSpeed", 2.0, 0.0, 6.0);
    public static final ModConfigSpec.IntValue FIREBALL_FIRE_SECONDS = BUILDER.comment(
                    "FireBall ignite duration in seconds (Reference FireTicks 3)")
            .defineInRange("fireballFireSeconds", 3, 0, 60);
    public static final ModConfigSpec.DoubleValue FIREBALL_HIT_RADIUS = BUILDER.comment(
                    "FireBall hit radius in blocks (Reference HitRadius 1.5)")
            .defineInRange("fireballHitRadius", 1.5, 0.2, 8.0);
    public static final ModConfigSpec.IntValue FIREBALL_MAX_TICKS =
            BUILDER.comment("FireBall max flight lifetime in ticks").defineInRange("fireballMaxTicks", 200, 0, 7200);
    public static final ModConfigSpec.BooleanValue FIREBALL_CONTROLLABLE = BUILDER.comment(
                    "FireBall bends toward the gaze mid-flight (Reference Controllable true)")
            .define("fireballControllable", true);

    public static final ModConfigSpec.IntValue FIRESKI_COOLDOWN_TICKS = BUILDER.comment(
                    "FireSki cooldown in ticks (Reference Cooldown 6000ms)")
            .defineInRange("fireskiCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.IntValue FIRESKI_DURATION_TICKS = BUILDER.comment(
                    "FireSki max ride in ticks (Reference Duration 12000ms)")
            .defineInRange("fireskiDurationTicks", 240, 0, 7200);
    public static final ModConfigSpec.IntValue FIRESKI_ARM_TICKS = BUILDER.comment(
                    "FireSki grounded click arm window in ticks (Reference 600ms)")
            .defineInRange("fireskiArmTicks", 12, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIRESKI_SPEED = BUILDER.comment(
                    "FireSki descent speed in blocks per tick (Reference Speed 1.2)")
            .defineInRange("fireskiSpeed", 1.2, 0.0, 6.0);
    public static final ModConfigSpec.DoubleValue FIRESKI_DAMAGE =
            BUILDER.comment("FireSki magic damage to buzzed entities").defineInRange("fireskiDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIRESKI_HIT_RADIUS = BUILDER.comment(
                    "FireSki buzz radius under the rider in blocks")
            .defineInRange("fireskiHitRadius", 2.0, 0.2, 8.0);
    public static final ModConfigSpec.IntValue FIRESKI_FIRE_SECONDS = BUILDER.comment(
                    "FireSki ignite duration in seconds (Reference FireTicks 3)")
            .defineInRange("fireskiFireSeconds", 3, 0, 60);
    public static final ModConfigSpec.DoubleValue FIRESKI_MIN_HEIGHT = BUILDER.comment(
                    "FireSki minimum air height to start a ride in blocks (Reference MinHeight 1)")
            .defineInRange("fireskiMinHeight", 1.0, 0.0, 8.0);
    public static final ModConfigSpec.BooleanValue FIRESKI_IGNITE = BUILDER.comment(
                    "FireSki torches buzzed entities (Reference Ignite true)")
            .define("fireskiIgnite", true);

    public static final ModConfigSpec.IntValue WALLOFFIRE_COOLDOWN_TICKS = BUILDER.comment(
                    "WallOfFire cooldown in ticks (Reference Cooldown 6000ms)")
            .defineInRange("walloffireCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.IntValue WALLOFFIRE_DURATION_TICKS = BUILDER.comment(
                    "WallOfFire lifetime in ticks (Reference Duration 8000ms)")
            .defineInRange("walloffireDurationTicks", 160, 0, 7200);
    public static final ModConfigSpec.IntValue WALLOFFIRE_DAMAGE_INTERVAL_TICKS = BUILDER.comment(
                    "WallOfFire sear interval in ticks (Reference DamageInterval 1000ms)")
            .defineInRange("walloffireDamageIntervalTicks", 20, 0, 7200);
    public static final ModConfigSpec.IntValue WALLOFFIRE_FX_INTERVAL_TICKS = BUILDER.comment(
                    "WallOfFire flame redraw interval in ticks (Reference FxInterval 100ms)")
            .defineInRange("walloffireFxIntervalTicks", 2, 0, 200);
    public static final ModConfigSpec.IntValue WALLOFFIRE_FOOT_REVERT_TICKS = BUILDER.comment(
                    "WallOfFire base-flame revert delay in ticks (Reference 3000ms)")
            .defineInRange("walloffireFootRevertTicks", 60, 0, 7200);
    public static final ModConfigSpec.DoubleValue WALLOFFIRE_RANGE = BUILDER.comment(
                    "WallOfFire aim reach cap in blocks (Reference Range 10)")
            .defineInRange("walloffireRange", 10.0, 1.0, 96.0);
    public static final ModConfigSpec.DoubleValue WALLOFFIRE_AIM_RANGE = BUILDER.comment(
                    "WallOfFire gaze aim distance in blocks (source raycast cap 6)")
            .defineInRange("walloffireAimRange", 6.0, 1.0, 32.0);
    public static final ModConfigSpec.DoubleValue WALLOFFIRE_WIDTH = BUILDER.comment(
                    "WallOfFire half-width in blocks (Reference Width 6)")
            .defineInRange("walloffireWidth", 6.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue WALLOFFIRE_HEIGHT = BUILDER.comment(
                    "WallOfFire height in blocks (Reference Height 6)")
            .defineInRange("walloffireHeight", 6.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue WALLOFFIRE_DAMAGE = BUILDER.comment(
                    "WallOfFire magic damage per sear (Reference Damage 3)")
            .defineInRange("walloffireDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue WALLOFFIRE_FIRE_SECONDS = BUILDER.comment(
                    "WallOfFire ignite duration in seconds (Reference FireTicks 3)")
            .defineInRange("walloffireFireSeconds", 3, 0, 60);
    public static final ModConfigSpec.DoubleValue WALLOFFIRE_THICKNESS = BUILDER.comment(
                    "WallOfFire sear half-thickness in blocks")
            .defineInRange("walloffireThickness", 1.5, 0.2, 8.0);

    public static final ModConfigSpec.IntValue ILLUMINATION_COOLDOWN_TICKS = BUILDER.comment(
                    "Illumination cooldown in ticks (Reference Cooldown 1000ms)")
            .defineInRange("illuminationCooldownTicks", 20, 0, 7200);
    public static final ModConfigSpec.IntValue ILLUMINATION_THRESHOLD = BUILDER.comment(
                    "Illumination max darkness to start, as raw brightness (Reference Threshold 7)")
            .defineInRange("illuminationThreshold", 7, 0, 15);
    public static final ModConfigSpec.IntValue ILLUMINATION_LIGHT_LEVEL = BUILDER.comment(
                    "Illumination carried glow level (Reference Light 14)")
            .defineInRange("illuminationLightLevel", 14, 0, 15);
    // ================= BATCH: FireSneak (9 sneak fire abilities) =================
    public static final ModConfigSpec.IntValue FIREBURST_COOLDOWN_TICKS = BUILDER.comment(
                    "FireBurst cooldown in ticks (Reference Cooldown 3000ms)")
            .defineInRange("fireburstCooldownTicks", 60, 0, 7200);
    public static final ModConfigSpec.IntValue FIREBURST_CHARGE_TICKS = BUILDER.comment(
                    "FireBurst charge time in ticks (Reference Charge 1500ms)")
            .defineInRange("fireburstChargeTicks", 30, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIREBURST_DAMAGE =
            BUILDER.comment("FireBurst magic damage on hit").defineInRange("fireburstDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIREBURST_RADIUS =
            BUILDER.comment("FireBurst burst radius in blocks").defineInRange("fireburstRadius", 12.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue FIREBURST_PUSH =
            BUILDER.comment("FireBurst knockback strength").defineInRange("fireburstPush", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.IntValue FIREBURST_FIRE_SECONDS =
            BUILDER.comment("FireBurst ignite duration in seconds").defineInRange("fireburstFireSeconds", 3, 0, 30);
    public static final ModConfigSpec.IntValue FIRESHIELD_COOLDOWN_TICKS = BUILDER.comment(
                    "FireShield cooldown in ticks (Reference Cooldown 4000ms)")
            .defineInRange("fireshieldCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIRESHIELD_RADIUS =
            BUILDER.comment("FireShield shield radius in blocks").defineInRange("fireshieldRadius", 3.0, 1.0, 8.0);
    public static final ModConfigSpec.IntValue FIRESHIELD_FIRE_SECONDS =
            BUILDER.comment("FireShield ignite duration in seconds").defineInRange("fireshieldFireSeconds", 3, 0, 30);
    public static final ModConfigSpec.DoubleValue FIRESHIELD_PUSH =
            BUILDER.comment("FireShield knockback strength").defineInRange("fireshieldPush", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.IntValue FLAMEBREATH_COOLDOWN_TICKS = BUILDER.comment(
                    "FlameBreath cooldown in ticks (Reference Cooldown 4000ms)")
            .defineInRange("flamebreathCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.IntValue FLAMEBREATH_DURATION_TICKS = BUILDER.comment(
                    "FlameBreath max breath duration in ticks (Reference Duration 5000ms)")
            .defineInRange("flamebreathDurationTicks", 100, 10, 1200);
    public static final ModConfigSpec.DoubleValue FLAMEBREATH_RANGE =
            BUILDER.comment("FlameBreath range in blocks").defineInRange("flamebreathRange", 14.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue FLAMEBREATH_DAMAGE =
            BUILDER.comment("FlameBreath magic damage on hit").defineInRange("flamebreathDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue FLAMEBREATH_FIRE_SECONDS =
            BUILDER.comment("FlameBreath ignite duration in seconds").defineInRange("flamebreathFireSeconds", 3, 0, 30);
    public static final ModConfigSpec.DoubleValue FLAMEBREATH_SPEED =
            BUILDER.comment("FlameBreath advance per tick in blocks").defineInRange("flamebreathSpeed", 1.0, 0.1, 6.0);
    public static final ModConfigSpec.IntValue FIREBREATH_COOLDOWN_TICKS = BUILDER.comment(
                    "FireBreath cooldown in ticks (Reference Cooldown 3500ms)")
            .defineInRange("firebreathCooldownTicks", 70, 0, 2400);
    public static final ModConfigSpec.IntValue FIREBREATH_DURATION_TICKS = BUILDER.comment(
                    "FireBreath max breath duration in ticks (Reference Duration 3000ms)")
            .defineInRange("firebreathDurationTicks", 60, 10, 1200);
    public static final ModConfigSpec.IntValue FIREBREATH_PARTICLES =
            BUILDER.comment("FireBreath flame particles per beam step").defineInRange("firebreathParticles", 6, 0, 20);
    public static final ModConfigSpec.DoubleValue FIREBREATH_PLAYER_DAMAGE = BUILDER.comment(
                    "FireBreath magic damage to players")
            .defineInRange("firebreathPlayerDamage", 2.0, 0.0, 20.0);
    public static final ModConfigSpec.DoubleValue FIREBREATH_MOB_DAMAGE =
            BUILDER.comment("FireBreath magic damage to mobs").defineInRange("firebreathMobDamage", 3.0, 0.0, 20.0);
    public static final ModConfigSpec.IntValue FIREBREATH_FIRE_SECONDS =
            BUILDER.comment("FireBreath ignite duration in seconds").defineInRange("firebreathFireSeconds", 3, 0, 30);
    public static final ModConfigSpec.DoubleValue FIREBREATH_RANGE =
            BUILDER.comment("FireBreath beam range in blocks").defineInRange("firebreathRange", 12.0, 2.0, 32.0);
    public static final ModConfigSpec.IntValue FIREWAVE_COOLDOWN_TICKS = BUILDER.comment(
                    "FireWave cooldown in ticks (Reference Cooldown 7000ms)")
            .defineInRange("firewaveCooldownTicks", 140, 0, 2400);
    public static final ModConfigSpec.IntValue FIREWAVE_DURATION_TICKS = BUILDER.comment(
                    "FireWave max duration in ticks (Reference Duration 6000ms)")
            .defineInRange("firewaveDurationTicks", 120, 10, 2400);
    public static final ModConfigSpec.DoubleValue FIREWAVE_RANGE =
            BUILDER.comment("FireWave travel range in blocks").defineInRange("firewaveRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue FIREWAVE_SPEED =
            BUILDER.comment("FireWave advance per tick in blocks").defineInRange("firewaveSpeed", 0.8, 0.1, 6.0);
    public static final ModConfigSpec.DoubleValue FIREWAVE_WIDTH =
            BUILDER.comment("FireWave wall half-width in blocks").defineInRange("firewaveWidth", 3.0, 1.0, 12.0);
    public static final ModConfigSpec.DoubleValue FIREWAVE_HEIGHT =
            BUILDER.comment("FireWave wall height in blocks").defineInRange("firewaveHeight", 3.0, 1.0, 12.0);
    public static final ModConfigSpec.DoubleValue FIREWAVE_DAMAGE =
            BUILDER.comment("FireWave magic damage on hit").defineInRange("firewaveDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue FIREWAVE_FIRE_SECONDS =
            BUILDER.comment("FireWave ignite duration in seconds").defineInRange("firewaveFireSeconds", 3, 0, 30);
    public static final ModConfigSpec.IntValue FIRECOMET_COOLDOWN_TICKS = BUILDER.comment(
                    "FireComet cooldown in ticks (Reference Cooldown 7000ms)")
            .defineInRange("firecometCooldownTicks", 140, 0, 2400);
    public static final ModConfigSpec.IntValue FIRECOMET_CHARGE_TICKS = BUILDER.comment(
                    "FireComet charge time in ticks (Reference Charge 2500ms)")
            .defineInRange("firecometChargeTicks", 50, 0, 7200);
    public static final ModConfigSpec.IntValue FIRECOMET_SCORCH_REVERT_TICKS = BUILDER.comment(
                    "FireComet scorch-fire revert delay in ticks")
            .defineInRange("firecometScorchRevertTicks", 60, 0, 7200);
    public static final ModConfigSpec.DoubleValue FIRECOMET_DAMAGE =
            BUILDER.comment("FireComet magic damage on blast").defineInRange("firecometDamage", 7.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIRECOMET_BLAST_RADIUS =
            BUILDER.comment("FireComet blast radius in blocks").defineInRange("firecometBlastRadius", 4.0, 1.0, 12.0);
    public static final ModConfigSpec.DoubleValue FIRECOMET_RANGE =
            BUILDER.comment("FireComet travel range in blocks").defineInRange("firecometRange", 25.0, 4.0, 64.0);
    public static final ModConfigSpec.IntValue FIRECOMET_BLAST_FIRE_SECONDS = BUILDER.comment(
                    "FireComet blast ignite duration in seconds")
            .defineInRange("firecometBlastFireSeconds", 4, 0, 30);
    public static final ModConfigSpec.IntValue FIRESHOTS_COOLDOWN_TICKS = BUILDER.comment(
                    "FireShots cooldown in ticks (Reference Cooldown 3000ms)")
            .defineInRange("fireshotsCooldownTicks", 60, 0, 2400);
    public static final ModConfigSpec.IntValue FIRESHOTS_STOCK =
            BUILDER.comment("FireShots fireball stock per gather").defineInRange("fireshotsStock", 4, 1, 16);
    public static final ModConfigSpec.DoubleValue FIRESHOTS_RANGE =
            BUILDER.comment("FireShots travel range in blocks").defineInRange("fireshotsRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue FIRESHOTS_DAMAGE =
            BUILDER.comment("FireShots magic damage on hit").defineInRange("fireshotsDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue FIRESHOTS_FIRE_SECONDS =
            BUILDER.comment("FireShots ignite duration in seconds").defineInRange("fireshotsFireSeconds", 3, 0, 30);
    public static final ModConfigSpec.DoubleValue FIRESHOTS_HIT_RADIUS =
            BUILDER.comment("FireShots hit radius in blocks").defineInRange("fireshotsHitRadius", 1.5, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue FIRESHOTS_SPEED =
            BUILDER.comment("FireShots travel per tick in blocks").defineInRange("fireshotsSpeed", 2.0, 0.5, 6.0);
    public static final ModConfigSpec.IntValue FIREMANIPULATION_COOLDOWN_TICKS = BUILDER.comment(
                    "FireManipulation stream cooldown in ticks (Reference StreamCooldown 4000ms)")
            .defineInRange("firemanipulationCooldownTicks", 80, 0, 2400);
    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_RANGE = BUILDER.comment(
                    "FireManipulation stream range in blocks")
            .defineInRange("firemanipulationStreamRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_DAMAGE = BUILDER.comment(
                    "FireManipulation stream magic damage on hit")
            .defineInRange("firemanipulationStreamDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_SPEED = BUILDER.comment(
                    "FireManipulation stream advance per tick in blocks")
            .defineInRange("firemanipulationStreamSpeed", 1.5, 0.1, 6.0);
    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_AURA_DAMAGE = BUILDER.comment(
                    "FireManipulation gathering-orb magic damage on touch")
            .defineInRange("firemanipulationAuraDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_AURA_RADIUS = BUILDER.comment(
                    "FireManipulation gathering-orb hurt radius in blocks")
            .defineInRange("firemanipulationAuraRadius", 2.0, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_HIT_RADIUS = BUILDER.comment(
                    "FireManipulation stream hit radius in blocks")
            .defineInRange("firemanipulationStreamHitRadius", 2.0, 0.2, 6.0);
    public static final ModConfigSpec.IntValue FIREMANIPULATION_AURA_FIRE_SECONDS = BUILDER.comment(
                    "FireManipulation gathering-orb ignite duration in seconds")
            .defineInRange("firemanipulationAuraFireSeconds", 2, 0, 30);
    public static final ModConfigSpec.IntValue FIREMANIPULATION_STREAM_FIRE_SECONDS = BUILDER.comment(
                    "FireManipulation stream ignite duration in seconds")
            .defineInRange("firemanipulationStreamFireSeconds", 3, 0, 30);
    public static final ModConfigSpec.IntValue HEATCONTROL_COOLDOWN_TICKS = BUILDER.comment(
                    "HeatControl cooldown in ticks (Reference Cooldown 2000ms)")
            .defineInRange("heatcontrolCooldownTicks", 40, 0, 2400);
    public static final ModConfigSpec.IntValue HEATCONTROL_COOK_INTERVAL_TICKS = BUILDER.comment(
                    "HeatControl cook interval in ticks (Reference CookMs 1500)")
            .defineInRange("heatcontrolCookIntervalTicks", 30, 1, 7200);
    public static final ModConfigSpec.IntValue HEATCONTROL_EXTINGUISH_COOLDOWN_TICKS = BUILDER.comment(
                    "HeatControl extinguish cooldown in ticks (Reference ExtinguishCooldown 2000ms)")
            .defineInRange("heatcontrolExtinguishCooldownTicks", 40, 0, 2400);
    public static final ModConfigSpec.IntValue HEATCONTROL_MAGMA_DELAY_TICKS = BUILDER.comment(
                    "HeatControl magma-to-stone delay in ticks (Reference 1000ms)")
            .defineInRange("heatcontrolMagmaDelayTicks", 20, 0, 7200);
    public static final ModConfigSpec.IntValue HEATCONTROL_SOLIDIFY_STEP_TICKS = BUILDER.comment(
                    "HeatControl solidify ring step in ticks (Reference ring step 50ms)")
            .defineInRange("heatcontrolSolidifyStepTicks", 1, 1, 20);
    public static final ModConfigSpec.IntValue HEATCONTROL_MELT_REVERT_TICKS = BUILDER.comment(
                    "HeatControl melt-water revert delay in ticks (Reference 5min)")
            .defineInRange("heatcontrolMeltRevertTicks", 6000, 0, 72000);
    public static final ModConfigSpec.IntValue HEATCONTROL_SOLIDIFY_REVERT_TICKS = BUILDER.comment(
                    "HeatControl solidified-stone revert delay in ticks (Reference SolidifyRevert 600000ms)")
            .defineInRange("heatcontrolSolidifyRevertTicks", 12000, 0, 72000);
    public static final ModConfigSpec.DoubleValue HEATCONTROL_EXTINGUISH_RADIUS = BUILDER.comment(
                    "HeatControl extinguish radius in blocks")
            .defineInRange("heatcontrolExtinguishRadius", 6.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue HEATCONTROL_MELT_RANGE = BUILDER.comment(
                    "HeatControl melt gaze range in blocks")
            .defineInRange("heatcontrolMeltRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue HEATCONTROL_MELT_RADIUS = BUILDER.comment(
                    "HeatControl melt pulse radius in blocks")
            .defineInRange("heatcontrolMeltRadius", 3.0, 1.0, 12.0);
    public static final ModConfigSpec.DoubleValue HEATCONTROL_SOLIDIFY_RANGE = BUILDER.comment(
                    "HeatControl solidify gaze range in blocks")
            .defineInRange("heatcontrolSolidifyRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue HEATCONTROL_SOLIDIFY_MAX_RADIUS = BUILDER.comment(
                    "HeatControl solidify max ring radius in blocks")
            .defineInRange("heatcontrolSolidifyMaxRadius", 5.0, 1.0, 12.0);
    public static final ModConfigSpec.BooleanValue HEATCONTROL_SOLIDIFY_REVERT = BUILDER.comment(
                    "HeatControl solidified stone reverts to lava after the revert delay")
            .define("heatcontrolSolidifyRevert", true);
    public static final ModConfigSpec.IntValue ARCSPARK_COOLDOWN_TICKS = BUILDER.comment(
                    "ArcSpark cooldown in ticks (reference Cooldown 4000ms)")
            .defineInRange("arcsparkCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.IntValue ARCSPARK_CHARGE_TICKS = BUILDER.comment(
                    "ArcSpark sneak charge in ticks (reference Charge 1500ms)")
            .defineInRange("arcsparkChargeTicks", 30, 0, 7200);
    public static final ModConfigSpec.IntValue ARCSPARK_DURATION_TICKS = BUILDER.comment(
                    "ArcSpark active window after the shot in ticks (reference Duration 1500ms)")
            .defineInRange("arcsparkDurationTicks", 30, 0, 7200);
    public static final ModConfigSpec.IntValue ARCSPARK_SPEED = BUILDER.comment(
                    "ArcSpark volley steps multiplier (speed * length steps of 0.3 per tick)")
            .defineInRange("arcsparkSpeed", 2, 1, 8);
    public static final ModConfigSpec.IntValue ARCSPARK_LENGTH = BUILDER.comment(
                    "ArcSpark volley length multiplier (speed * length steps of 0.3 per tick)")
            .defineInRange("arcsparkLength", 8, 1, 16);
    public static final ModConfigSpec.DoubleValue ARCSPARK_DAMAGE =
            BUILDER.comment("ArcSpark lightning damage on contact").defineInRange("arcsparkDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue ARCSPARK_RANGE =
            BUILDER.comment("ArcSpark travel range in blocks").defineInRange("arcsparkRange", 16.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue ARCSPARK_SEEK_RADIUS =
            BUILDER.comment("ArcSpark entity seek radius in blocks").defineInRange("arcsparkSeekRadius", 3.0, 0.5, 8.0);
    public static final ModConfigSpec.IntValue ARCSPARK_METAL_SCAN_RADIUS = BUILDER.comment(
                    "ArcSpark metallic block scan radius in blocks")
            .defineInRange("arcsparkMetalScanRadius", 2, 1, 4);
    public static final ModConfigSpec.IntValue ARCSPARK_CONE_ANGLE_DEG =
            BUILDER.comment("ArcSpark steering cone in degrees").defineInRange("arcsparkConeAngleDeg", 60, 5, 180);
    public static final ModConfigSpec.DoubleValue ARCSPARK_HIT_DISTANCE =
            BUILDER.comment("ArcSpark contact distance in blocks").defineInRange("arcsparkHitDistance", 1.0, 0.2, 4.0);
    public static final ModConfigSpec.DoubleValue ARCSPARK_STEP_LENGTH =
            BUILDER.comment("ArcSpark advance per step in blocks").defineInRange("arcsparkStepLength", 0.3, 0.05, 1.0);

    public static final ModConfigSpec.IntValue CHARGEBOLT_COOLDOWN_TICKS = BUILDER.comment(
                    "ChargeBolt cooldown in ticks (reference Cooldown 4000ms)")
            .defineInRange("chargeboltCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.IntValue CHARGEBOLT_CHARGE_TICKS = BUILDER.comment(
                    "ChargeBolt sneak charge in ticks (reference Charge 1500ms)")
            .defineInRange("chargeboltChargeTicks", 30, 0, 7200);
    public static final ModConfigSpec.DoubleValue CHARGEBOLT_DAMAGE =
            BUILDER.comment("ChargeBolt lightning damage per bolt").defineInRange("chargeboltDamage", 5.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue CHARGEBOLT_BOLT_RANGE = BUILDER.comment(
                    "ChargeBolt single-throw range in blocks")
            .defineInRange("chargeboltBoltRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue CHARGEBOLT_BLAST_RADIUS = BUILDER.comment(
                    "ChargeBolt discharge cone bolt range in blocks")
            .defineInRange("chargeboltBlastRadius", 3.0, 1.0, 16.0);
    public static final ModConfigSpec.IntValue CHARGEBOLT_SPEED =
            BUILDER.comment("ChargeBolt advances per tick").defineInRange("chargeboltSpeed", 2, 1, 8);
    public static final ModConfigSpec.IntValue CHARGEBOLT_STOCK =
            BUILDER.comment("ChargeBolt bolt stock per charge").defineInRange("chargeboltStock", 5, 1, 16);
    public static final ModConfigSpec.DoubleValue CHARGEBOLT_HIT_RADIUS =
            BUILDER.comment("ChargeBolt hit radius in blocks").defineInRange("chargeboltHitRadius", 0.62, 0.1, 4.0);
    public static final ModConfigSpec.IntValue CHARGEBOLT_JITTER_DEGREES = BUILDER.comment(
                    "ChargeBolt per-step yaw/pitch jitter in degrees")
            .defineInRange("chargeboltJitterDegrees", 8, 0, 30);
    public static final ModConfigSpec.IntValue CHARGEBOLT_SPREAD_YAW = BUILDER.comment(
                    "ChargeBolt discharge cone half yaw in degrees")
            .defineInRange("chargeboltSpreadYaw", 30, 0, 90);
    public static final ModConfigSpec.IntValue CHARGEBOLT_SPREAD_PITCH = BUILDER.comment(
                    "ChargeBolt discharge cone half pitch in degrees")
            .defineInRange("chargeboltSpreadPitch", 23, 0, 90);

    public static final ModConfigSpec.IntValue BOLT_COOLDOWN_TICKS = BUILDER.comment(
                    "Bolt cooldown in ticks (reference Cooldown 3500ms)")
            .defineInRange("boltCooldownTicks", 70, 0, 7200);
    public static final ModConfigSpec.IntValue BOLT_CHARGE_TICKS = BUILDER.comment(
                    "Bolt sneak charge in ticks (reference Charge 1500ms)")
            .defineInRange("boltChargeTicks", 30, 0, 7200);
    public static final ModConfigSpec.DoubleValue BOLT_DAMAGE =
            BUILDER.comment("Bolt base lightning damage").defineInRange("boltDamage", 5.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue BOLT_RANGE =
            BUILDER.comment("Bolt targeting range in blocks").defineInRange("boltRange", 25.0, 4.0, 64.0);
    public static final ModConfigSpec.IntValue BOLT_POINT_GENERATION =
            BUILDER.comment("Bolt midpoint-displacement generations").defineInRange("boltPointGeneration", 5, 1, 8);
    public static final ModConfigSpec.DoubleValue BOLT_FALLOFF_RADIUS =
            BUILDER.comment("Bolt damage falloff radius in blocks").defineInRange("boltFalloffRadius", 5.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue BOLT_FULL_DAMAGE_RADIUS =
            BUILDER.comment("Bolt full damage radius in blocks").defineInRange("boltFullDamageRadius", 1.5, 0.2, 8.0);
    public static final ModConfigSpec.DoubleValue BOLT_CHANNEL_SHARE_RADIUS = BUILDER.comment(
                    "Bolt nearby-channel share radius in blocks")
            .defineInRange("boltChannelShareRadius", 4.0, 1.0, 16.0);

    public static final ModConfigSpec.IntValue DISCHARGE_COOLDOWN_TICKS = BUILDER.comment(
                    "Discharge cooldown in ticks (reference Cooldown 3000ms)")
            .defineInRange("dischargeCooldownTicks", 60, 0, 7200);
    public static final ModConfigSpec.IntValue DISCHARGE_DURATION_TICKS = BUILDER.comment(
                    "Discharge crawl duration in ticks (reference Duration 2000ms)")
            .defineInRange("dischargeDurationTicks", 40, 0, 7200);
    public static final ModConfigSpec.DoubleValue DISCHARGE_DAMAGE =
            BUILDER.comment("Discharge shock damage").defineInRange("dischargeDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue DISCHARGE_RANGE =
            BUILDER.comment("Discharge branch travel range in blocks").defineInRange("dischargeRange", 18.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue DISCHARGE_HIT_RADIUS =
            BUILDER.comment("Discharge touch hit radius in blocks").defineInRange("dischargeHitRadius", 1.5, 0.2, 6.0);
    public static final ModConfigSpec.IntValue DISCHARGE_SPLIT_INTERVAL = BUILDER.comment(
                    "Discharge branch split interval in spaces")
            .defineInRange("dischargeSplitInterval", 3, 1, 12);
    public static final ModConfigSpec.IntValue DISCHARGE_STEPS_PER_TICK =
            BUILDER.comment("Discharge branch steps per tick").defineInRange("dischargeStepsPerTick", 5, 1, 16);
    public static final ModConfigSpec.DoubleValue DISCHARGE_STEP_LENGTH = BUILDER.comment(
                    "Discharge branch step length in blocks")
            .defineInRange("dischargeStepLength", 0.2, 0.05, 1.0);
    public static final ModConfigSpec.DoubleValue DISCHARGE_KNOCKBACK =
            BUILDER.comment("Discharge shock shove strength").defineInRange("dischargeKnockback", 0.8, 0.0, 5.0);
    public static final ModConfigSpec.IntValue DISCHARGE_IGNITE_SECONDS =
            BUILDER.comment("Discharge ignite duration in seconds").defineInRange("dischargeIgniteSeconds", 1, 0, 20);

    public static final ModConfigSpec.IntValue LIGHTNING_COOLDOWN_TICKS = BUILDER.comment(
                    "Lightning cooldown in ticks (reference Cooldown 4000ms)")
            .defineInRange("lightningCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.IntValue LIGHTNING_CHARGE_TICKS = BUILDER.comment(
                    "Lightning sneak charge in ticks (reference Charge 2000ms)")
            .defineInRange("lightningChargeTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue LIGHTNING_LINGER_TICKS = BUILDER.comment(
                    "Lightning bolt linger after the strike in ticks")
            .defineInRange("lightningLingerTicks", 8, 0, 200);
    public static final ModConfigSpec.DoubleValue LIGHTNING_DAMAGE =
            BUILDER.comment("Lightning strike damage").defineInRange("lightningDamage", 6.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue LIGHTNING_RANGE =
            BUILDER.comment("Lightning targeting range in blocks").defineInRange("lightningRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.DoubleValue LIGHTNING_CHAIN_RANGE = BUILDER.comment(
                    "Lightning victim chain range in blocks")
            .defineInRange("lightningChainRange", 12.0, 2.0, 32.0);
    public static final ModConfigSpec.IntValue LIGHTNING_MAX_CHAINS =
            BUILDER.comment("Lightning max chain jumps").defineInRange("lightningMaxChains", 3, 0, 12);
    public static final ModConfigSpec.DoubleValue LIGHTNING_CHAIN_CHANCE =
            BUILDER.comment("Lightning chain continuation chance").defineInRange("lightningChainChance", 0.5, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue LIGHTNING_STUN_CHANCE =
            BUILDER.comment("Lightning stun chance per victim").defineInRange("lightningStunChance", 0.4, 0.0, 1.0);
    public static final ModConfigSpec.IntValue LIGHTNING_STUN_TICKS =
            BUILDER.comment("Lightning stun duration in ticks").defineInRange("lightningStunTicks", 40, 0, 1200);
    public static final ModConfigSpec.IntValue LIGHTNING_POINT_GENERATION = BUILDER.comment(
                    "Lightning bolt midpoint-displacement generations")
            .defineInRange("lightningPointGeneration", 5, 1, 8);
    public static final ModConfigSpec.DoubleValue LIGHTNING_SUB_ARC_CHANCE = BUILDER.comment(
                    "Lightning sub-arc chance per bolt point")
            .defineInRange("lightningSubArcChance", 0.12, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue LIGHTNING_MAX_ARC_ANGLE_DEG = BUILDER.comment(
                    "Lightning sub-arc spread in degrees")
            .defineInRange("lightningMaxArcAngleDeg", 25.0, 0.0, 90.0);
    public static final ModConfigSpec.IntValue LIGHTNING_WATER_ARCS =
            BUILDER.comment("Lightning water fan-out arc count").defineInRange("lightningWaterArcs", 4, 0, 16);
    public static final ModConfigSpec.DoubleValue LIGHTNING_WATER_ARC_RANGE = BUILDER.comment(
                    "Lightning water fan-out range in blocks")
            .defineInRange("lightningWaterArcRange", 12.0, 2.0, 32.0);
    public static final ModConfigSpec.IntValue LIGHTNING_MAX_COPPER_ARCS =
            BUILDER.comment("Lightning max copper/rod walk jumps").defineInRange("lightningMaxCopperArcs", 5, 0, 16);
    public static final ModConfigSpec.DoubleValue LIGHTNING_CONDUCTIVITY_RANGE = BUILDER.comment(
                    "Lightning copper/rod walk range in blocks")
            .defineInRange("lightningConductivityRange", 6.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue LIGHTNING_WET_HIT_RADIUS = BUILDER.comment(
                    "Lightning strike hit radius in water in blocks")
            .defineInRange("lightningWetHitRadius", 4.0, 0.5, 12.0);
    public static final ModConfigSpec.DoubleValue LIGHTNING_DRY_HIT_RADIUS = BUILDER.comment(
                    "Lightning strike hit radius on land in blocks")
            .defineInRange("lightningDryHitRadius", 2.5, 0.5, 12.0);
    public static final ModConfigSpec.IntValue LIGHTNING_IGNITE_SECONDS =
            BUILDER.comment("Lightning ignite duration in seconds").defineInRange("lightningIgniteSeconds", 2, 0, 20);

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_COOLDOWN_TICKS = BUILDER.comment(
                    "LightningBurst cooldown in ticks (reference Cooldown 6000ms)")
            .defineInRange("lightningburstCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_CHARGE_TICKS = BUILDER.comment(
                    "LightningBurst sneak charge in ticks (reference Charge 2000ms)")
            .defineInRange("lightningburstChargeTicks", 40, 0, 7200);
    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_DAMAGE =
            BUILDER.comment("LightningBurst damage per bolt").defineInRange("lightningburstDamage", 5.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_RADIUS = BUILDER.comment(
                    "LightningBurst bolt travel radius in blocks")
            .defineInRange("lightningburstRadius", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_YAW_STEP = BUILDER.comment(
                    "LightningBurst sphere lattice yaw step in degrees")
            .defineInRange("lightningburstYawStep", 55, 5, 180);
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_PITCH_STEP = BUILDER.comment(
                    "LightningBurst sphere lattice pitch step in degrees")
            .defineInRange("lightningburstPitchStep", 55, 5, 180);
    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_STEP_LENGTH = BUILDER.comment(
                    "LightningBurst advance per step in blocks")
            .defineInRange("lightningburstStepLength", 0.2, 0.05, 1.0);
    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_GAP_LENGTH = BUILDER.comment(
                    "LightningBurst advance per tick in blocks")
            .defineInRange("lightningburstGapLength", 1.0, 0.2, 4.0);
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_JITTER_DEGREES = BUILDER.comment(
                    "LightningBurst per-tick yaw/pitch jitter in degrees")
            .defineInRange("lightningburstJitterDegrees", 20, 0, 60);
    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_HIT_RADIUS = BUILDER.comment(
                    "LightningBurst hit radius in blocks")
            .defineInRange("lightningburstHitRadius", 2.0, 0.2, 6.0);
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_IGNITE_SECONDS = BUILDER.comment(
                    "LightningBurst ignite duration in seconds")
            .defineInRange("lightningburstIgniteSeconds", 1, 0, 20);

    public static final ModConfigSpec.IntValue ELECTRIFY_COOLDOWN_TICKS = BUILDER.comment(
                    "Electrify cooldown in ticks (reference Cooldown 5000ms)")
            .defineInRange("electrifyCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.IntValue ELECTRIFY_DURATION_TICKS = BUILDER.comment(
                    "Electrify field duration in ticks (reference Duration 8000ms)")
            .defineInRange("electrifyDurationTicks", 160, 0, 7200);
    public static final ModConfigSpec.DoubleValue ELECTRIFY_RANGE =
            BUILDER.comment("Electrify target range in blocks").defineInRange("electrifyRange", 12.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue ELECTRIFY_WATER_DAMAGE = BUILDER.comment(
                    "Electrify water damage per tick-window")
            .defineInRange("electrifyWaterDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue ELECTRIFY_SPREAD_DEPTH =
            BUILDER.comment("Electrify spread passes to the 6 faces").defineInRange("electrifySpreadDepth", 2, 0, 8);
    public static final ModConfigSpec.IntValue ELECTRIFY_DEBUFF_TICKS = BUILDER.comment(
                    "Electrify slowness/weakness duration in ticks")
            .defineInRange("electrifyDebuffTicks", 10, 0, 1200);
    public static final ModConfigSpec.DoubleValue ELECTRIFY_HIT_RADIUS = BUILDER.comment(
                    "Electrify victim scan radius in blocks")
            .defineInRange("electrifyHitRadius", 1.0, 0.2, 4.0);

    public static final ModConfigSpec.IntValue COMBUSTBEAM_COOLDOWN_TICKS = BUILDER.comment(
                    "CombustBeam cooldown in ticks (reference Cooldown 6000ms)")
            .defineInRange("combustbeamCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.IntValue COMBUSTBEAM_MIN_CHARGE_TICKS = BUILDER.comment(
                    "CombustBeam minimum charge in ticks (reference MinCharge 1000ms)")
            .defineInRange("combustbeamMinChargeTicks", 20, 0, 7200);
    public static final ModConfigSpec.IntValue COMBUSTBEAM_MAX_CHARGE_TICKS = BUILDER.comment(
                    "CombustBeam maximum charge in ticks (reference MaxCharge 3000ms)")
            .defineInRange("combustbeamMaxChargeTicks", 60, 1, 7200);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_RANGE =
            BUILDER.comment("CombustBeam travel range in blocks").defineInRange("combustbeamRange", 25.0, 4.0, 96.0);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MIN_POWER = BUILDER.comment(
                    "CombustBeam beam power at minimum charge")
            .defineInRange("combustbeamMinPower", 1.0, 0.1, 12.0);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MAX_POWER = BUILDER.comment(
                    "CombustBeam beam power at maximum charge")
            .defineInRange("combustbeamMaxPower", 4.0, 0.1, 12.0);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MIN_DAMAGE = BUILDER.comment(
                    "CombustBeam blast damage at minimum charge")
            .defineInRange("combustbeamMinDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MAX_DAMAGE = BUILDER.comment(
                    "CombustBeam blast damage at maximum charge")
            .defineInRange("combustbeamMaxDamage", 9.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MAX_ANGLE = BUILDER.comment(
                    "CombustBeam steering limit in degrees")
            .defineInRange("combustbeamMaxAngle", 15.0, 0.0, 90.0);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_BLAST_BASE_RADIUS = BUILDER.comment(
                    "CombustBeam blast base radius in blocks (radius = base + power)")
            .defineInRange("combustbeamBlastBaseRadius", 2.0, 0.0, 12.0);
    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_KNOCKBACK_CAP =
            BUILDER.comment("CombustBeam blast knockback cap").defineInRange("combustbeamKnockbackCap", 4.0, 0.0, 12.0);
    public static final ModConfigSpec.IntValue COMBUSTBEAM_IGNITE_SECONDS = BUILDER.comment(
                    "CombustBeam ignite duration in seconds")
            .defineInRange("combustbeamIgniteSeconds", 3, 0, 20);

    public static final ModConfigSpec.IntValue EXPLODE_COOLDOWN_TICKS = BUILDER.comment(
                    "Explode cooldown in ticks (reference Cooldown 5000ms)")
            .defineInRange("explodeCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.DoubleValue EXPLODE_DAMAGE =
            BUILDER.comment("Explode detonation damage").defineInRange("explodeDamage", 6.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue EXPLODE_RADIUS =
            BUILDER.comment("Explode detonation radius in blocks").defineInRange("explodeRadius", 5.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue EXPLODE_KNOCKBACK =
            BUILDER.comment("Explode detonation knockback strength").defineInRange("explodeKnockback", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.DoubleValue EXPLODE_RANGE =
            BUILDER.comment("Explode target painting range in blocks").defineInRange("explodeRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.IntValue EXPLODE_IGNITE_SECONDS =
            BUILDER.comment("Explode ignite duration in seconds").defineInRange("explodeIgniteSeconds", 2, 0, 20);

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_COOLDOWN_TICKS = BUILDER.comment(
                    "CombustionBlast cooldown in ticks (reference Cooldown 5000ms)")
            .defineInRange("combustionblastCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_CHARGE_TICKS = BUILDER.comment(
                    "CombustionBlast sneak charge in ticks (reference Charge 2000ms)")
            .defineInRange("combustionblastChargeTicks", 40, 0, 7200);
    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_DAMAGE =
            BUILDER.comment("CombustionBlast detonation damage").defineInRange("combustionblastDamage", 5.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_BLAST_RADIUS = BUILDER.comment(
                    "CombustionBlast entity blast radius in blocks")
            .defineInRange("combustionblastBlastRadius", 4.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_RANGE = BUILDER.comment(
                    "CombustionBlast beam travel range in blocks")
            .defineInRange("combustionblastRange", 25.0, 4.0, 96.0);
    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_HIT_RADIUS = BUILDER.comment(
                    "CombustionBlast beam contact radius in blocks")
            .defineInRange("combustionblastHitRadius", 1.5, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_EXPLOSION_POWER = BUILDER.comment(
                    "CombustionBlast vanilla explosion power")
            .defineInRange("combustionblastExplosionPower", 3.0, 0.0, 12.0);
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_IGNITE_SECONDS = BUILDER.comment(
                    "CombustionBlast ignite duration in seconds")
            .defineInRange("combustionblastIgniteSeconds", 3, 0, 20);

    public static final ModConfigSpec.IntValue COMBUSTION_COOLDOWN_TICKS = BUILDER.comment(
                    "Combustion cooldown in ticks (reference Cooldown 6000ms)")
            .defineInRange("combustionCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.DoubleValue COMBUSTION_DAMAGE =
            BUILDER.comment("Combustion detonation damage").defineInRange("combustionDamage", 6.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue COMBUSTION_RADIUS = BUILDER.comment(
                    "Combustion entity blast radius in blocks")
            .defineInRange("combustionRadius", 4.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue COMBUSTION_SPEED = BUILDER.comment(
                    "Combustion beam speed in blocks per second")
            .defineInRange("combustionSpeed", 20.0, 1.0, 60.0);
    public static final ModConfigSpec.DoubleValue COMBUSTION_RANGE =
            BUILDER.comment("Combustion beam travel range in blocks").defineInRange("combustionRange", 25.0, 4.0, 96.0);
    public static final ModConfigSpec.DoubleValue COMBUSTION_HIT_RADIUS = BUILDER.comment(
                    "Combustion beam contact radius in blocks")
            .defineInRange("combustionHitRadius", 2.0, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue COMBUSTION_EXPLOSION_POWER = BUILDER.comment(
                    "Combustion vanilla explosion power")
            .defineInRange("combustionExplosionPower", 3.0, 0.0, 12.0);
    public static final ModConfigSpec.DoubleValue COMBUSTION_KNOCKBACK = BUILDER.comment(
                    "Combustion detonation knockback strength")
            .defineInRange("combustionKnockback", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.IntValue COMBUSTION_IGNITE_SECONDS =
            BUILDER.comment("Combustion ignite duration in seconds").defineInRange("combustionIgniteSeconds", 3, 0, 20);
    public static final ModConfigSpec.IntValue COMBUSTION_MAX_TICKS = BUILDER.comment(
                    "Combustion beam maximum flight time in ticks")
            .defineInRange("combustionMaxTicks", 200, 20, 2400);
    // ---- AirOne: 10 air abilities (AirJet, AirScooter, AirSpout, AirPunch, AirSlam, AirFlight, AirShield, AirBurst,
    // AirBlast, AirSwipe) ----
    public static final ModConfigSpec.IntValue AIRJET_DURATION_TICKS = BUILDER.comment(
                    "AirJet ride duration in ticks (Reference Duration 2000ms)")
            .defineInRange("airjetDurationTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue AIRJET_COOLDOWN_TICKS = BUILDER.comment(
                    "AirJet cooldown in ticks (Reference Cooldown 7000ms)")
            .defineInRange("airjetCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRJET_SPEED =
            BUILDER.comment("AirJet thrust speed (Reference Speed 0.8)").defineInRange("airjetSpeed", 0.8, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue AIRSCOOTER_SPEED = BUILDER.comment(
                    "AirScooter ride speed (Reference Speed 0.675)")
            .defineInRange("airscooterSpeed", 0.675, 0.1, 4.0);
    public static final ModConfigSpec.IntValue AIRSCOOTER_INTERVAL_TICKS = BUILDER.comment(
                    "AirScooter spin/move-check interval in ticks (Reference Interval 100ms)")
            .defineInRange("airscooterIntervalTicks", 2, 1, 100);
    public static final ModConfigSpec.IntValue AIRSCOOTER_COOLDOWN_TICKS = BUILDER.comment(
                    "AirScooter cooldown in ticks (Reference Cooldown 500ms)")
            .defineInRange("airscooterCooldownTicks", 10, 0, 7200);
    public static final ModConfigSpec.IntValue AIRSCOOTER_DURATION_TICKS = BUILDER.comment(
                    "AirScooter max ride in ticks, 0 is infinite (Reference Duration 0)")
            .defineInRange("airscooterDurationTicks", 0, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRSCOOTER_MAX_HEIGHT = BUILDER.comment(
                    "AirScooter ground-scan height in blocks (Reference MaxHeight 7)")
            .defineInRange("airscooterMaxHeight", 7.0, 1.0, 32.0);
    public static final ModConfigSpec.IntValue AIRSCOOTER_CHIME_TICKS = BUILDER.comment(
                    "AirScooter chime interval in ticks (3000ms)")
            .defineInRange("airscooterChimeTicks", 60, 0, 7200);
    public static final ModConfigSpec.IntValue AIRSPOUT_COOLDOWN_TICKS = BUILDER.comment(
                    "AirSpout cooldown in ticks (Reference Cooldown 5000ms)")
            .defineInRange("airspoutCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.IntValue AIRSPOUT_DURATION_TICKS = BUILDER.comment(
                    "AirSpout max ride in ticks, 0 is infinite (Reference Duration 0)")
            .defineInRange("airspoutDurationTicks", 0, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRSPOUT_HEIGHT = BUILDER.comment(
                    "AirSpout column cap in blocks (Reference Height 16)")
            .defineInRange("airspoutHeight", 16.0, 4.0, 32.0);
    public static final ModConfigSpec.IntValue AIRSPOUT_INTERVAL_TICKS = BUILDER.comment(
                    "AirSpout spiral animation interval in ticks (Reference Interval 100ms)")
            .defineInRange("airspoutIntervalTicks", 2, 1, 100);
    public static final ModConfigSpec.DoubleValue AIRSPOUT_THRESHOLD = BUILDER.comment(
                    "AirSpout start/ride height tolerance in blocks")
            .defineInRange("airspoutThreshold", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.IntValue AIRPUNCH_COOLDOWN_TICKS = BUILDER.comment(
                    "AirPunch cooldown in ticks (Reference Cooldown 1500ms)")
            .defineInRange("airpunchCooldownTicks", 30, 0, 7200);
    public static final ModConfigSpec.IntValue AIRPUNCH_THRESHOLD_TICKS = BUILDER.comment(
                    "AirPunch flurry window in ticks (Reference Flurry 800ms)")
            .defineInRange("airpunchThresholdTicks", 16, 0, 1200);
    public static final ModConfigSpec.IntValue AIRPUNCH_SHOTS =
            BUILDER.comment("AirPunch shots per flurry (Reference Shots 5)").defineInRange("airpunchShots", 5, 1, 32);
    public static final ModConfigSpec.DoubleValue AIRPUNCH_RANGE = BUILDER.comment(
                    "AirPunch bolt range in blocks (Reference Range 20)")
            .defineInRange("airpunchRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue AIRPUNCH_DAMAGE = BUILDER.comment(
                    "AirPunch magic damage on hit (Reference Damage 2)")
            .defineInRange("airpunchDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue AIRPUNCH_HIT_RADIUS = BUILDER.comment(
                    "AirPunch bolt hit radius in blocks (Reference HitRadius 1)")
            .defineInRange("airpunchHitRadius", 1.0, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue AIRPUNCH_PUSH =
            BUILDER.comment("AirPunch knockback strength on hit").defineInRange("airpunchPush", 0.8, 0.0, 5.0);
    public static final ModConfigSpec.IntValue AIRSLAM_COOLDOWN_TICKS = BUILDER.comment(
                    "AirSlam cooldown in ticks (Reference Cooldown 4000ms)")
            .defineInRange("airslamCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRSLAM_POWER = BUILDER.comment(
                    "AirSlam spike push strength (Reference Power 2)")
            .defineInRange("airslamPower", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.DoubleValue AIRSLAM_RANGE = BUILDER.comment(
                    "AirSlam target acquire range in blocks (Reference Range 15)")
            .defineInRange("airslamRange", 15.0, 2.0, 64.0);
    public static final ModConfigSpec.IntValue AIRSLAM_SPIKE_TICKS =
            BUILDER.comment("AirSlam spike delay in ticks (~50ms)").defineInRange("airslamSpikeTicks", 1, 0, 100);
    public static final ModConfigSpec.IntValue AIRSLAM_LIFETIME_TICKS =
            BUILDER.comment("AirSlam lifetime in ticks (~400ms)").defineInRange("airslamLifetimeTicks", 8, 1, 200);
    public static final ModConfigSpec.DoubleValue AIRSLAM_LIFT =
            BUILDER.comment("AirSlam pop-up velocity on click").defineInRange("airslamLift", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.IntValue AIRFLIGHT_COOLDOWN_TICKS = BUILDER.comment(
                    "AirFlight cooldown in ticks (Reference Cooldown 5000ms)")
            .defineInRange("airflightCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SPEED = BUILDER.comment(
                    "AirFlight base Soar speed (Reference Speed 1)")
            .defineInRange("airflightSpeed", 1.0, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SOAR_SLOW = BUILDER.comment(
                    "AirFlight Soar SLOW step (Reference 0.6)")
            .defineInRange("airflightSoarSlow", 0.6, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SOAR_NORMAL = BUILDER.comment(
                    "AirFlight Soar NORMAL step (Reference 1.0)")
            .defineInRange("airflightSoarNormal", 1.0, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SOAR_FAST = BUILDER.comment(
                    "AirFlight Soar FAST step (Reference 1.6)")
            .defineInRange("airflightSoarFast", 1.6, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue AIRFLIGHT_GLIDE_BOOST = BUILDER.comment(
                    "AirFlight Glide entry rescue boost when slow")
            .defineInRange("airflightGlideBoost", 1.2, 0.0, 4.0);
    public static final ModConfigSpec.DoubleValue AIRFLIGHT_RAM_RADIUS = BUILDER.comment(
                    "AirFlight Soar ram hit radius in blocks")
            .defineInRange("airflightRamRadius", 1.5, 0.5, 6.0);
    public static final ModConfigSpec.DoubleValue AIRFLIGHT_RAM_THRESHOLD =
            BUILDER.comment("AirFlight Soar ram minimum speed").defineInRange("airflightRamThreshold", 0.5, 0.0, 4.0);
    public static final ModConfigSpec.IntValue AIRSHIELD_COOLDOWN_TICKS = BUILDER.comment(
                    "AirShield cooldown in ticks (Reference Cooldown 7000ms)")
            .defineInRange("airshieldCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.IntValue AIRSHIELD_DURATION_TICKS = BUILDER.comment(
                    "AirShield duration in ticks for cooldown math (Reference Duration 6500ms)")
            .defineInRange("airshieldDurationTicks", 130, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRSHIELD_MAX_RADIUS = BUILDER.comment(
                    "AirShield max radius in blocks (Reference MaxRadius 4)")
            .defineInRange("airshieldMaxRadius", 4.0, 1.0, 12.0);
    public static final ModConfigSpec.DoubleValue AIRSHIELD_INITIAL_RADIUS = BUILDER.comment(
                    "AirShield starting radius in blocks (Reference InitialRadius 1)")
            .defineInRange("airshieldInitialRadius", 1.0, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue AIRSHIELD_SPEED = BUILDER.comment(
                    "AirShield ring spin speed (Reference Speed 10)")
            .defineInRange("airshieldSpeed", 10.0, 1.0, 30.0);
    public static final ModConfigSpec.DoubleValue AIRSHIELD_PUSH_FACTOR = BUILDER.comment(
                    "AirShield entity shove strength (Reference Push 1.5)")
            .defineInRange("airshieldPushFactor", 1.5, 0.0, 5.0);
    public static final ModConfigSpec.IntValue AIRSHIELD_STREAMS =
            BUILDER.comment("AirShield wind streams (Reference Streams 5)").defineInRange("airshieldStreams", 5, 1, 16);
    public static final ModConfigSpec.IntValue AIRSHIELD_PARTICLES = BUILDER.comment(
                    "AirShield particles per ring point (Reference Particles 5)")
            .defineInRange("airshieldParticles", 5, 0, 32);
    public static final ModConfigSpec.DoubleValue AIRSHIELD_GROWTH = BUILDER.comment(
                    "AirShield radius growth per tick in blocks")
            .defineInRange("airshieldGrowth", 0.3, 0.05, 2.0);
    public static final ModConfigSpec.IntValue AIRSHIELD_PUSH_ANGLE_DEGREES = BUILDER.comment(
                    "AirShield swirl angle in degrees (Reference 50-degree swirl)")
            .defineInRange("airshieldPushAngleDegrees", 50, 0, 90);
    public static final ModConfigSpec.IntValue AIRBURST_COOLDOWN_TICKS = BUILDER.comment(
                    "AirBurst cooldown in ticks (Reference Cooldown 2500ms)")
            .defineInRange("airburstCooldownTicks", 50, 0, 7200);
    public static final ModConfigSpec.IntValue AIRBURST_CHARGE_TICKS = BUILDER.comment(
                    "AirBurst sneak charge in ticks (Reference Charge 1500ms)")
            .defineInRange("airburstChargeTicks", 30, 0, 1200);
    public static final ModConfigSpec.DoubleValue AIRBURST_DAMAGE = BUILDER.comment(
                    "AirBurst magic damage (Reference Damage 2)")
            .defineInRange("airburstDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue AIRBURST_RADIUS = BUILDER.comment(
                    "AirBurst sphere radius in blocks (Reference Radius 7)")
            .defineInRange("airburstRadius", 7.0, 2.0, 16.0);
    public static final ModConfigSpec.DoubleValue AIRBURST_PUSH =
            BUILDER.comment("AirBurst push strength (Reference Push 2.2)").defineInRange("airburstPush", 2.2, 0.0, 8.0);
    public static final ModConfigSpec.DoubleValue AIRBURST_FALL_THRESHOLD = BUILDER.comment(
                    "AirBurst fall-burst trigger distance in blocks (Reference FallThreshold 8)")
            .defineInRange("airburstFallThreshold", 8.0, 0.0, 32.0);
    public static final ModConfigSpec.DoubleValue AIRBURST_CONE_ANGLE_DEGREES = BUILDER.comment(
                    "AirBurst cone half-angle in degrees (Reference 30-degree cone)")
            .defineInRange("airburstConeAngleDegrees", 30.0, 5.0, 90.0);
    public static final ModConfigSpec.DoubleValue AIRBURST_CONE_RANGE_MULT = BUILDER.comment(
                    "AirBurst cone range multiplier over sphere radius")
            .defineInRange("airburstConeRangeMult", 1.3, 0.5, 3.0);
    public static final ModConfigSpec.DoubleValue AIRBURST_CONE_PUSH_MULT = BUILDER.comment(
                    "AirBurst cone push multiplier over sphere push")
            .defineInRange("airburstConePushMult", 1.2, 0.0, 3.0);
    public static final ModConfigSpec.DoubleValue AIRBURST_LIFT =
            BUILDER.comment("AirBurst upward pop factor on hit").defineInRange("airburstLift", 0.45, 0.0, 2.0);
    public static final ModConfigSpec.IntValue AIRBLAST_COOLDOWN_TICKS = BUILDER.comment(
                    "AirBlast cooldown in ticks (Reference Cooldown 2000ms)")
            .defineInRange("airblastCooldownTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue AIRBLAST_ORIGIN_TICKS = BUILDER.comment(
                    "AirBlast selected-origin memory in ticks (Reference OriginMemory 10s)")
            .defineInRange("airblastOriginTicks", 200, 20, 7200);
    public static final ModConfigSpec.IntValue AIRBLAST_MAX_TICKS =
            BUILDER.comment("AirBlast max flight time in ticks").defineInRange("airblastMaxTicks", 200, 20, 7200);
    public static final ModConfigSpec.DoubleValue AIRBLAST_SPEED =
            BUILDER.comment("AirBlast speed (Reference Speed 25)").defineInRange("airblastSpeed", 25.0, 1.0, 60.0);
    public static final ModConfigSpec.DoubleValue AIRBLAST_RANGE = BUILDER.comment(
                    "AirBlast range in blocks (Reference Range 20)")
            .defineInRange("airblastRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue AIRBLAST_RADIUS = BUILDER.comment(
                    "AirBlast hit radius in blocks (Reference Radius 2)")
            .defineInRange("airblastRadius", 2.0, 0.5, 6.0);
    public static final ModConfigSpec.DoubleValue AIRBLAST_DAMAGE = BUILDER.comment(
                    "AirBlast magic damage (Reference Damage 3)")
            .defineInRange("airblastDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue AIRBLAST_PUSH_SELF = BUILDER.comment(
                    "AirBlast self-launch strength (Reference PushSelf 1.5)")
            .defineInRange("airblastPushSelf", 1.5, 0.0, 8.0);
    public static final ModConfigSpec.DoubleValue AIRBLAST_PUSH_OTHERS = BUILDER.comment(
                    "AirBlast knockback to others (Reference PushOthers 3)")
            .defineInRange("airblastPushOthers", 3.0, 0.0, 8.0);
    public static final ModConfigSpec.IntValue AIRBLAST_PARTICLES = BUILDER.comment(
                    "AirBlast particles per tick (Reference Particles 10)")
            .defineInRange("airblastParticles", 10, 0, 32);
    public static final ModConfigSpec.DoubleValue AIRBLAST_SELECT_RANGE = BUILDER.comment(
                    "AirBlast origin-select range in blocks (Reference SelectRange 10)")
            .defineInRange("airblastSelectRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.BooleanValue AIRBLAST_CONTROLLABLE = BUILDER.comment(
                    "AirBlast sneaking mid-flight steers it (Reference Controllable true)")
            .define("airblastControllable", true);
    public static final ModConfigSpec.IntValue AIRSWIPE_COOLDOWN_TICKS = BUILDER.comment(
                    "AirSwipe cooldown in ticks (Reference Cooldown 2000ms)")
            .defineInRange("airswipeCooldownTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue AIRSWIPE_MAX_CHARGE_TICKS = BUILDER.comment(
                    "AirSwipe full charge in ticks (Reference Charge 2000ms)")
            .defineInRange("airswipeMaxChargeTicks", 40, 0, 1200);
    public static final ModConfigSpec.DoubleValue AIRSWIPE_DAMAGE = BUILDER.comment(
                    "AirSwipe magic damage (Reference Damage 3)")
            .defineInRange("airswipeDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue AIRSWIPE_PUSH = BUILDER.comment(
                    "AirSwipe knockback strength (Reference Push 1)")
            .defineInRange("airswipePush", 1.0, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue AIRSWIPE_SPEED =
            BUILDER.comment("AirSwipe speed (Reference Speed 18)").defineInRange("airswipeSpeed", 18.0, 1.0, 60.0);
    public static final ModConfigSpec.DoubleValue AIRSWIPE_RANGE = BUILDER.comment(
                    "AirSwipe range in blocks (Reference Range 16)")
            .defineInRange("airswipeRange", 16.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue AIRSWIPE_RADIUS = BUILDER.comment(
                    "AirSwipe hit radius in blocks (Reference Radius 1.5)")
            .defineInRange("airswipeRadius", 1.5, 0.5, 6.0);
    public static final ModConfigSpec.IntValue AIRSWIPE_ARC = BUILDER.comment(
                    "AirSwipe fan half-arc in degrees (Reference Arc 20)")
            .defineInRange("airswipeArc", 20, 5, 90);
    public static final ModConfigSpec.IntValue AIRSWIPE_ARC_STEP = BUILDER.comment(
                    "AirSwipe fan step in degrees (Reference ArcStep 5)")
            .defineInRange("airswipeArcStep", 5, 1, 20);
    public static final ModConfigSpec.DoubleValue AIRSWIPE_CHARGE_FACTOR = BUILDER.comment(
                    "AirSwipe full-charge damage/push multiplier (Reference ChargeFactor 2)")
            .defineInRange("airswipeChargeFactor", 2.0, 1.0, 5.0);
    public static final ModConfigSpec.IntValue AIRSWIPE_PARTICLES = BUILDER.comment(
                    "AirSwipe particles per stream tick (Reference Particles 6)")
            .defineInRange("airswipeParticles", 6, 0, 32);
    // ================= BATCH: AirTwo (9 air: AirSuction, Suffocate, Tornado, AirBreath, AirBullet, Meditate,
    // SonicBlast, Zephyr, AirStream) =================
    public static final ModConfigSpec.IntValue AIRSUCTION_COOLDOWN_TICKS = BUILDER.comment(
                    "AirSuction cooldown in ticks (ref Cooldown 2000ms)")
            .defineInRange("airsuctionCooldownTicks", 40, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRSUCTION_RANGE = BUILDER.comment(
                    "AirSuction reach in blocks (ref Range 20)")
            .defineInRange("airsuctionRange", 20.0, 2.0, 64.0);
    public static final ModConfigSpec.DoubleValue AIRSUCTION_RADIUS = BUILDER.comment(
                    "AirSuction close-range grab radius in blocks (ref Radius 3)")
            .defineInRange("airsuctionRadius", 3.0, 0.5, 12.0);
    public static final ModConfigSpec.DoubleValue AIRSUCTION_PUSH =
            BUILDER.comment("AirSuction pull strength (ref Push 1.2)").defineInRange("airsuctionPush", 1.2, 0.0, 5.0);
    public static final ModConfigSpec.IntValue AIRSUCTION_CONE_DEGREES = BUILDER.comment(
                    "AirSuction frontal cone half-angle in degrees (ref 35)")
            .defineInRange("airsuctionConeDegrees", 35, 0, 180);

    public static final ModConfigSpec.IntValue SUFFOCATE_COOLDOWN_TICKS = BUILDER.comment(
                    "Suffocate cooldown in ticks (ref Cooldown 2000ms)")
            .defineInRange("suffocateCooldownTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue SUFFOCATE_CHARGE_TICKS = BUILDER.comment(
                    "Suffocate charge-up in ticks (ref Charge 2000ms)")
            .defineInRange("suffocateChargeTicks", 40, 0, 7200);
    public static final ModConfigSpec.DoubleValue SUFFOCATE_RANGE = BUILDER.comment(
                    "Suffocate grip range in blocks (ref Range 30)")
            .defineInRange("suffocateRange", 30.0, 2.0, 64.0);
    public static final ModConfigSpec.DoubleValue SUFFOCATE_RADIUS = BUILDER.comment(
                    "Suffocate spiral visual radius in blocks (ref Radius 2)")
            .defineInRange("suffocateRadius", 2.0, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue SUFFOCATE_DAMAGE = BUILDER.comment(
                    "Suffocate magic damage per tick (ref Damage 2)")
            .defineInRange("suffocateDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue SUFFOCATE_DAMAGE_DELAY_TICKS = BUILDER.comment(
                    "Suffocate first damage delay in ticks (ref DamageDelay 2000ms)")
            .defineInRange("suffocateDamageDelayTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue SUFFOCATE_DAMAGE_REPEAT_TICKS = BUILDER.comment(
                    "Suffocate damage repeat in ticks (ref DamageRepeat 1000ms)")
            .defineInRange("suffocateDamageRepeatTicks", 20, 1, 7200);
    public static final ModConfigSpec.IntValue SUFFOCATE_SLOW_AMPLIFIER = BUILDER.comment(
                    "Suffocate slowness amplifier (ref SlowAmp 1)")
            .defineInRange("suffocateSlowAmplifier", 1, 0, 10);
    public static final ModConfigSpec.IntValue SUFFOCATE_SLOW_REPEAT_TICKS = BUILDER.comment(
                    "Suffocate slow reapply in ticks (ref SlowRepeat 1000ms)")
            .defineInRange("suffocateSlowRepeatTicks", 20, 1, 7200);
    public static final ModConfigSpec.IntValue SUFFOCATE_SLOW_DELAY_TICKS = BUILDER.comment(
                    "Suffocate first slow delay in ticks (ref SlowDelay 500ms)")
            .defineInRange("suffocateSlowDelayTicks", 10, 0, 7200);
    public static final ModConfigSpec.IntValue SUFFOCATE_BLIND_AMPLIFIER = BUILDER.comment(
                    "Suffocate blindness amplifier (ref BlindAmp 0)")
            .defineInRange("suffocateBlindAmplifier", 0, 0, 10);
    public static final ModConfigSpec.IntValue SUFFOCATE_BLIND_REPEAT_TICKS = BUILDER.comment(
                    "Suffocate blindness reapply in ticks (ref BlindRepeat 2000ms)")
            .defineInRange("suffocateBlindRepeatTicks", 40, 1, 7200);
    public static final ModConfigSpec.IntValue SUFFOCATE_BLIND_DELAY_TICKS = BUILDER.comment(
                    "Suffocate first blindness delay in ticks (ref BlindDelay 1000ms)")
            .defineInRange("suffocateBlindDelayTicks", 20, 0, 7200);
    public static final ModConfigSpec.DoubleValue SUFFOCATE_AIM_RADIUS = BUILDER.comment(
                    "Suffocate gaze-aim tolerance radius in blocks (ref AimRadius 2.5)")
            .defineInRange("suffocateAimRadius", 2.5, 0.5, 8.0);

    public static final ModConfigSpec.IntValue TORNADO_COOLDOWN_TICKS = BUILDER.comment(
                    "Tornado cooldown in ticks (ref Cooldown 5000ms)")
            .defineInRange("tornadoCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.IntValue TORNADO_DURATION_TICKS = BUILDER.comment(
                    "Tornado max duration in ticks (ref Duration 10000ms)")
            .defineInRange("tornadoDurationTicks", 200, 0, 7200);
    public static final ModConfigSpec.DoubleValue TORNADO_MAX_HEIGHT = BUILDER.comment(
                    "Tornado funnel height in blocks (ref Height 15)")
            .defineInRange("tornadoMaxHeight", 15.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue TORNADO_RADIUS = BUILDER.comment(
                    "Tornado funnel radius in blocks (ref Radius 5)")
            .defineInRange("tornadoRadius", 5.0, 1.0, 12.0);
    public static final ModConfigSpec.DoubleValue TORNADO_RANGE = BUILDER.comment(
                    "Tornado gaze-target range in blocks (ref Range 25)")
            .defineInRange("tornadoRange", 25.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue TORNADO_PLAYER_PUSH = BUILDER.comment(
                    "Tornado lift factor for players (ref PlayerPush 1)")
            .defineInRange("tornadoPlayerPush", 1.0, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue TORNADO_NPC_PUSH = BUILDER.comment(
                    "Tornado lift factor for non-players (ref NpcPush 1)")
            .defineInRange("tornadoNpcPush", 1.0, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue TORNADO_SUCTION = BUILDER.comment(
                    "Tornado outer-band suction strength (ref Suction 1)")
            .defineInRange("tornadoSuction", 1.0, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue TORNADO_SPEED =
            BUILDER.comment("Tornado swirl speed (ref Speed 10)").defineInRange("tornadoSpeed", 10.0, 0.5, 40.0);
    public static final ModConfigSpec.IntValue TORNADO_PARTICLES = BUILDER.comment(
                    "Tornado particles per ring step (ref Particles 8)")
            .defineInRange("tornadoParticles", 8, 0, 32);

    public static final ModConfigSpec.IntValue AIRBREATH_COOLDOWN_TICKS = BUILDER.comment(
                    "AirBreath cooldown in ticks (ref Cooldown 3000ms)")
            .defineInRange("airbreathCooldownTicks", 60, 0, 7200);
    public static final ModConfigSpec.IntValue AIRBREATH_DURATION_TICKS = BUILDER.comment(
                    "AirBreath max breath in ticks (ref Duration 3000ms)")
            .defineInRange("airbreathDurationTicks", 60, 10, 7200);
    public static final ModConfigSpec.DoubleValue AIRBREATH_RANGE = BUILDER.comment(
                    "AirBreath cone range in blocks (ref Range 12)")
            .defineInRange("airbreathRange", 12.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue AIRBREATH_KNOCKBACK = BUILDER.comment(
                    "AirBreath shove strength (ref Knockback 1)")
            .defineInRange("airbreathKnockback", 1.0, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue AIRBREATH_PLAYER_DAMAGE = BUILDER.comment(
                    "AirBreath damage to players (ref PlayerDamage 2)")
            .defineInRange("airbreathPlayerDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue AIRBREATH_MOB_DAMAGE = BUILDER.comment(
                    "AirBreath damage to mobs (ref MobDamage 3)")
            .defineInRange("airbreathMobDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue AIRBREATH_LAUNCH = BUILDER.comment(
                    "AirBreath wall-blast self launch strength (ref Launch 1.5)")
            .defineInRange("airbreathLaunch", 1.5, 0.0, 5.0);
    public static final ModConfigSpec.IntValue AIRBREATH_PARTICLES = BUILDER.comment(
                    "AirBreath particles per beam step (ref Particles 6)")
            .defineInRange("airbreathParticles", 6, 0, 32);
    public static final ModConfigSpec.DoubleValue AIRBREATH_HIT_RADIUS =
            BUILDER.comment("AirBreath beam hit radius in blocks").defineInRange("airbreathHitRadius", 1.5, 0.2, 6.0);
    public static final ModConfigSpec.IntValue AIRBREATH_OXYGEN_DURATION_TICKS = BUILDER.comment(
                    "AirBreath water-breathing lend duration in ticks")
            .defineInRange("airbreathOxygenDurationTicks", 100, 0, 2400);
    public static final ModConfigSpec.IntValue AIRBREATH_OXYGEN_AMPLIFIER =
            BUILDER.comment("AirBreath water-breathing amplifier").defineInRange("airbreathOxygenAmplifier", 2, 0, 10);

    public static final ModConfigSpec.IntValue AIRBULLET_COOLDOWN_TICKS = BUILDER.comment(
                    "AirBullet cooldown in ticks, paid on hit (ref Cooldown 6000ms)")
            .defineInRange("airbulletCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.IntValue AIRBULLET_CHARGE_TICKS = BUILDER.comment(
                    "AirBullet sneak charge in ticks (ref Charge 2000ms)")
            .defineInRange("airbulletChargeTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue AIRBULLET_ARMED_TICKS = BUILDER.comment(
                    "AirBullet loaded hold time in ticks (ref Armed 15000ms)")
            .defineInRange("airbulletArmedTicks", 300, 20, 7200);
    public static final ModConfigSpec.DoubleValue AIRBULLET_DAMAGE =
            BUILDER.comment("AirBullet magic damage (ref Damage 12)").defineInRange("airbulletDamage", 12.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue AIRBULLET_RANGE = BUILDER.comment(
                    "AirBullet travel range in blocks (ref Range 30)")
            .defineInRange("airbulletRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.DoubleValue AIRBULLET_SPEED = BUILDER.comment(
                    "AirBullet speed in blocks per second (ref Speed 40)")
            .defineInRange("airbulletSpeed", 40.0, 1.0, 120.0);
    public static final ModConfigSpec.DoubleValue AIRBULLET_HIT_RADIUS = BUILDER.comment(
                    "AirBullet hit radius in blocks (ref HitRadius 0.8)")
            .defineInRange("airbulletHitRadius", 0.8, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue AIRBULLET_KNOCKBACK =
            BUILDER.comment("AirBullet hit knockback strength").defineInRange("airbulletKnockback", 3.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue MEDITATE_WARMUP_TICKS = BUILDER.comment(
                    "Meditate focus warmup in ticks (ref Warmup 3000ms)")
            .defineInRange("meditateWarmupTicks", 60, 0, 7200);
    public static final ModConfigSpec.IntValue MEDITATE_COOLDOWN_TICKS = BUILDER.comment(
                    "Meditate cooldown in ticks, paid on payoff (ref Cooldown 10000ms)")
            .defineInRange("meditateCooldownTicks", 200, 0, 7200);
    public static final ModConfigSpec.IntValue MEDITATE_BOOST_TICKS = BUILDER.comment(
                    "Meditate blessing duration in ticks (ref Boost 60000ms)")
            .defineInRange("meditateBoostTicks", 1200, 20, 7200);
    public static final ModConfigSpec.IntValue MEDITATE_PARTICLES = BUILDER.comment(
                    "Meditate particles per tick (ref Particles 6)")
            .defineInRange("meditateParticles", 6, 0, 32);
    public static final ModConfigSpec.IntValue MEDITATE_ABSORPTION_AMPLIFIER = BUILDER.comment(
                    "Meditate absorption blessing level (ref amp 1)")
            .defineInRange("meditateAbsorptionAmplifier", 1, 1, 5);
    public static final ModConfigSpec.IntValue MEDITATE_SPEED_AMPLIFIER = BUILDER.comment(
                    "Meditate speed blessing level (ref amp 1)")
            .defineInRange("meditateSpeedAmplifier", 1, 1, 5);
    public static final ModConfigSpec.IntValue MEDITATE_JUMP_AMPLIFIER =
            BUILDER.comment("Meditate jump blessing level (ref amp 1)").defineInRange("meditateJumpAmplifier", 1, 1, 5);

    public static final ModConfigSpec.IntValue SONICBLAST_COOLDOWN_TICKS = BUILDER.comment(
                    "SonicBlast cooldown in ticks (ref Cooldown 3000ms)")
            .defineInRange("sonicblastCooldownTicks", 60, 0, 7200);
    public static final ModConfigSpec.IntValue SONICBLAST_WARMUP_TICKS = BUILDER.comment(
                    "SonicBlast scream charge in ticks (ref Warmup 1500ms)")
            .defineInRange("sonicblastWarmupTicks", 30, 0, 7200);
    public static final ModConfigSpec.DoubleValue SONICBLAST_DAMAGE =
            BUILDER.comment("SonicBlast magic damage (ref Damage 4)").defineInRange("sonicblastDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue SONICBLAST_RANGE = BUILDER.comment(
                    "SonicBlast wave range in blocks (ref Range 20)")
            .defineInRange("sonicblastRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue SONICBLAST_HIT_RADIUS = BUILDER.comment(
                    "SonicBlast wave hit radius in blocks (ref HitRadius 1.5)")
            .defineInRange("sonicblastHitRadius", 1.5, 0.2, 6.0);
    public static final ModConfigSpec.IntValue SONICBLAST_NAUSEA_TICKS = BUILDER.comment(
                    "SonicBlast nausea duration in ticks (ref Nausea 100)")
            .defineInRange("sonicblastNauseaTicks", 100, 0, 2400);
    public static final ModConfigSpec.IntValue SONICBLAST_BLIND_TICKS = BUILDER.comment(
                    "SonicBlast blindness duration in ticks (ref Blind 60)")
            .defineInRange("sonicblastBlindTicks", 60, 0, 2400);
    public static final ModConfigSpec.IntValue SONICBLAST_NAUSEA_AMPLIFIER =
            BUILDER.comment("SonicBlast nausea amplifier").defineInRange("sonicblastNauseaAmplifier", 1, 0, 10);
    public static final ModConfigSpec.IntValue SONICBLAST_BLIND_AMPLIFIER =
            BUILDER.comment("SonicBlast blindness amplifier").defineInRange("sonicblastBlindAmplifier", 1, 0, 10);
    public static final ModConfigSpec.DoubleValue SONICBLAST_KNOCKBACK =
            BUILDER.comment("SonicBlast wave knockback strength").defineInRange("sonicblastKnockback", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.IntValue ZEPHYR_COOLDOWN_TICKS = BUILDER.comment(
                    "Zephyr cooldown in ticks (ref Cooldown 4000ms)")
            .defineInRange("zephyrCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.DoubleValue ZEPHYR_RADIUS = BUILDER.comment(
                    "Zephyr gentle-current radius in blocks (ref Radius 6)")
            .defineInRange("zephyrRadius", 6.0, 1.0, 16.0);
    public static final ModConfigSpec.IntValue ZEPHYR_SLOW_DURATION_TICKS = BUILDER.comment(
                    "Zephyr slow-falling duration in ticks")
            .defineInRange("zephyrSlowDurationTicks", 70, 10, 2400);
    public static final ModConfigSpec.IntValue ZEPHYR_SLOW_AMPLIFIER =
            BUILDER.comment("Zephyr slow-falling amplifier").defineInRange("zephyrSlowAmplifier", 2, 0, 10);
    public static final ModConfigSpec.IntValue ZEPHYR_REFRESH_THRESHOLD_TICKS = BUILDER.comment(
                    "Zephyr slow-falling refresh threshold in ticks")
            .defineInRange("zephyrRefreshThresholdTicks", 30, 0, 1200);

    public static final ModConfigSpec.IntValue AIRSTREAM_COOLDOWN_TICKS = BUILDER.comment(
                    "AirStream cooldown in ticks (ref Cooldown 6000ms)")
            .defineInRange("airstreamCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.DoubleValue AIRSTREAM_SPEED = BUILDER.comment(
                    "AirStream head advance in blocks per tick (ref Speed 0.5)")
            .defineInRange("airstreamSpeed", 0.5, 0.05, 4.0);
    public static final ModConfigSpec.DoubleValue AIRSTREAM_RANGE = BUILDER.comment(
                    "AirStream max length in blocks (ref Range 25)")
            .defineInRange("airstreamRange", 25.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue AIRSTREAM_CARRY_HEIGHT = BUILDER.comment(
                    "AirStream max lift above the hand in blocks (ref CarryHeight 5)")
            .defineInRange("airstreamCarryHeight", 5.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue AIRSTREAM_CATCH_RADIUS =
            BUILDER.comment("AirStream catch radius in blocks").defineInRange("airstreamCatchRadius", 2.0, 0.5, 6.0);
    // ================= BATCH: AvatarSpirit (12 + passives) =================
    // AvatarState (ProjectAvatar AvatarState toggle buffs + proportional cooldown).
    public static final ModConfigSpec.DoubleValue AVATARSTATE_DAMAGE_FACTOR =
            BUILDER.comment("AvatarState damage multiplier").defineInRange("avatarstateDamageFactor", 2.0, 1.0, 4.0);
    public static final ModConfigSpec.DoubleValue AVATARSTATE_RANGE_FACTOR =
            BUILDER.comment("AvatarState range multiplier").defineInRange("avatarstateRangeFactor", 1.5, 1.0, 3.0);
    public static final ModConfigSpec.IntValue AVATARSTATE_EFFECT_DURATION_TICKS = BUILDER.comment(
                    "AvatarState buff tick duration")
            .defineInRange("avatarstateEffectDurationTicks", 70, 10, 600);
    public static final ModConfigSpec.IntValue AVATARSTATE_REGEN_AMP =
            BUILDER.comment("AvatarState regeneration amplifier").defineInRange("avatarstateRegenAmp", 1, 0, 4);
    public static final ModConfigSpec.IntValue AVATARSTATE_RESIST_AMP =
            BUILDER.comment("AvatarState resistance amplifier").defineInRange("avatarstateResistAmp", 0, 0, 4);
    public static final ModConfigSpec.IntValue AVATARSTATE_SPEED_AMP =
            BUILDER.comment("AvatarState speed amplifier").defineInRange("avatarstateSpeedAmp", 0, 0, 4);
    public static final ModConfigSpec.IntValue AVATARSTATE_COOLDOWN_MAX_MS = BUILDER.comment(
                    "AvatarState max cooldown in ms")
            .defineInRange("avatarstateCooldownMaxMs", 60000, 0, 300000);
    public static final ModConfigSpec.IntValue AVATARSTATE_COOLDOWN_MIN_MS =
            BUILDER.comment("AvatarState min cooldown in ms").defineInRange("avatarstateCooldownMinMs", 3000, 0, 60000);
    public static final ModConfigSpec.IntValue AVATARSTATE_COOLDOWN_DIVISOR = BUILDER.comment(
                    "AvatarState cooldown divisor of time served")
            .defineInRange("avatarstateCooldownDivisor", 4, 1, 10);

    // ElementSphere (ProjectAvatar avatar.ElementSphere shell + flight).
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_COOLDOWN_TICKS = BUILDER.comment(
                    "ElementSphere cooldown in ticks")
            .defineInRange("elementsphereCooldownTicks", 240, 0, 7200);
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_DURATION_TICKS = BUILDER.comment(
                    "ElementSphere duration in ticks")
            .defineInRange("elementsphereDurationTicks", 1200, 20, 7200);
    public static final ModConfigSpec.DoubleValue ELEMENTSPHERE_MAX_HEIGHT = BUILDER.comment(
                    "ElementSphere flight ceiling in blocks")
            .defineInRange("elementsphereMaxHeight", 6.0, 1.0, 24.0);
    public static final ModConfigSpec.DoubleValue ELEMENTSPHERE_FLY_SPEED =
            BUILDER.comment("ElementSphere flight speed").defineInRange("elementsphereFlySpeed", 0.7, 0.1, 3.0);
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_DISMISS_WINDOW_TICKS = BUILDER.comment(
                    "ElementSphere double-sneak dismiss window in ticks")
            .defineInRange("elementsphereDismissWindowTicks", 12, 0, 100);
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_AIR_USES =
            BUILDER.comment("ElementSphere air uses").defineInRange("elementsphereAirUses", 5, 1, 20);
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_FIRE_USES =
            BUILDER.comment("ElementSphere fire uses").defineInRange("elementsphereFireUses", 5, 1, 20);
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_WATER_USES =
            BUILDER.comment("ElementSphere water uses").defineInRange("elementsphereWaterUses", 5, 1, 20);
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_EARTH_USES =
            BUILDER.comment("ElementSphere earth uses").defineInRange("elementsphereEarthUses", 3, 1, 20);
    public static final ModConfigSpec.DoubleValue ELEMENTSPHERE_PUSH_RADIUS = BUILDER.comment(
                    "ElementSphere shove radius in blocks")
            .defineInRange("elementspherePushRadius", 2.5, 0.5, 8.0);

    // ESAir (ProjectAvatar avatar.sphere.ESAir gust bolt).
    public static final ModConfigSpec.IntValue ESAIR_COOLDOWN_TICKS =
            BUILDER.comment("ESAir cooldown in ticks").defineInRange("esairCooldownTicks", 30, 0, 1200);
    public static final ModConfigSpec.DoubleValue ESAIR_RANGE =
            BUILDER.comment("ESAir range in blocks").defineInRange("esairRange", 25.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue ESAIR_DAMAGE =
            BUILDER.comment("ESAir magic damage").defineInRange("esairDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue ESAIR_KNOCKBACK =
            BUILDER.comment("ESAir knockback strength").defineInRange("esairKnockback", 1.5, 0.0, 5.0);
    public static final ModConfigSpec.IntValue ESAIR_SPEED =
            BUILDER.comment("ESAir steps per tick").defineInRange("esairSpeed", 3, 1, 8);

    // ESEarth (ProjectAvatar avatar.sphere.ESEarth boulder).
    public static final ModConfigSpec.IntValue ESEARTH_COOLDOWN_TICKS =
            BUILDER.comment("ESEarth cooldown in ticks").defineInRange("esearthCooldownTicks", 80, 0, 1200);
    public static final ModConfigSpec.DoubleValue ESEARTH_DAMAGE =
            BUILDER.comment("ESEarth magic damage").defineInRange("esearthDamage", 5.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue ESEARTH_CRATER =
            BUILDER.comment("ESEarth crater radius in blocks").defineInRange("esearthCrater", 3, 0, 8);
    public static final ModConfigSpec.IntValue ESEARTH_REVERT_MS =
            BUILDER.comment("ESEarth crater revert in ms").defineInRange("esearthRevertMs", 5000, 0, 30000);
    public static final ModConfigSpec.DoubleValue ESEARTH_SPEED =
            BUILDER.comment("ESEarth boulder speed").defineInRange("esearthSpeed", 3.0, 0.5, 6.0);

    // ESFire (ProjectAvatar avatar.sphere.ESFire flame bolt).
    public static final ModConfigSpec.IntValue ESFIRE_COOLDOWN_TICKS =
            BUILDER.comment("ESFire cooldown in ticks").defineInRange("esfireCooldownTicks", 30, 0, 1200);
    public static final ModConfigSpec.DoubleValue ESFIRE_RANGE =
            BUILDER.comment("ESFire range in blocks").defineInRange("esfireRange", 25.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue ESFIRE_DAMAGE =
            BUILDER.comment("ESFire magic damage").defineInRange("esfireDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue ESFIRE_BURN_TICKS =
            BUILDER.comment("ESFire burn duration in ticks").defineInRange("esfireBurnTicks", 60, 0, 1200);
    public static final ModConfigSpec.IntValue ESFIRE_SPEED =
            BUILDER.comment("ESFire steps per tick").defineInRange("esfireSpeed", 3, 1, 8);
    public static final ModConfigSpec.BooleanValue ESFIRE_CONTROLLABLE =
            BUILDER.comment("ESFire steers with the caster gaze").define("esfireControllable", true);

    // ESWater (ProjectAvatar avatar.sphere.ESWater water bolt).
    public static final ModConfigSpec.IntValue ESWATER_COOLDOWN_TICKS =
            BUILDER.comment("ESWater cooldown in ticks").defineInRange("eswaterCooldownTicks", 30, 0, 1200);
    public static final ModConfigSpec.DoubleValue ESWATER_RANGE =
            BUILDER.comment("ESWater range in blocks").defineInRange("eswaterRange", 25.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue ESWATER_DAMAGE =
            BUILDER.comment("ESWater magic damage").defineInRange("eswaterDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue ESWATER_SPEED =
            BUILDER.comment("ESWater steps per tick").defineInRange("eswaterSpeed", 3, 1, 8);
    public static final ModConfigSpec.IntValue ESWATER_TRAIL_REVERT_TICKS =
            BUILDER.comment("ESWater trail revert in ticks").defineInRange("eswaterTrailRevertTicks", 3, 0, 100);

    // ESStream (ProjectAvatar avatar.sphere.ESStream four-element finisher).
    public static final ModConfigSpec.IntValue ESSTREAM_COOLDOWN_TICKS =
            BUILDER.comment("ESStream cooldown in ticks").defineInRange("esstreamCooldownTicks", 160, 0, 2400);
    public static final ModConfigSpec.DoubleValue ESSTREAM_RANGE =
            BUILDER.comment("ESStream range in blocks").defineInRange("esstreamRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.DoubleValue ESSTREAM_DAMAGE =
            BUILDER.comment("ESStream magic damage").defineInRange("esstreamDamage", 6.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue ESSTREAM_KNOCKBACK =
            BUILDER.comment("ESStream knockback strength").defineInRange("esstreamKnockback", 2.0, 0.0, 5.0);
    public static final ModConfigSpec.IntValue ESSTREAM_REQUIRED_USES =
            BUILDER.comment("ESStream uses required per element").defineInRange("esstreamRequiredUses", 1, 1, 8);
    public static final ModConfigSpec.BooleanValue ESSTREAM_END_ABILITY =
            BUILDER.comment("ESStream ends the sphere on cast").define("esstreamEndAbility", true);
    public static final ModConfigSpec.IntValue ESSTREAM_CRATER =
            BUILDER.comment("ESStream crater radius in blocks").defineInRange("esstreamCrater", 3, 0, 8);
    public static final ModConfigSpec.IntValue ESSTREAM_REVERT_MS =
            BUILDER.comment("ESStream crater revert in ms").defineInRange("esstreamRevertMs", 8000, 0, 30000);
    public static final ModConfigSpec.DoubleValue ESSTREAM_SPEED =
            BUILDER.comment("ESStream travel speed").defineInRange("esstreamSpeed", 1.2, 0.2, 4.0);

    // SpiritBeam (ProjectAvatar SpiritBeam gaze beam).
    public static final ModConfigSpec.IntValue SPIRITBEAM_DURATION_TICKS =
            BUILDER.comment("SpiritBeam duration in ticks").defineInRange("spiritbeamDurationTicks", 100, 10, 1200);
    public static final ModConfigSpec.IntValue SPIRITBEAM_COOLDOWN_TICKS =
            BUILDER.comment("SpiritBeam cooldown in ticks").defineInRange("spiritbeamCooldownTicks", 160, 0, 2400);
    public static final ModConfigSpec.DoubleValue SPIRITBEAM_DAMAGE =
            BUILDER.comment("SpiritBeam magic damage").defineInRange("spiritbeamDamage", 6.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue SPIRITBEAM_RANGE =
            BUILDER.comment("SpiritBeam range in blocks").defineInRange("spiritbeamRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.IntValue SPIRITBEAM_BLOCK_RADIUS =
            BUILDER.comment("SpiritBeam crater radius in blocks").defineInRange("spiritbeamBlockRadius", 3, 0, 8);
    public static final ModConfigSpec.IntValue SPIRITBEAM_BLOCK_REVERT_TICKS = BUILDER.comment(
                    "SpiritBeam crater revert in ticks")
            .defineInRange("spiritbeamBlockRevertTicks", 160, 0, 2400);
    public static final ModConfigSpec.IntValue SPIRITBEAM_IGNITE_SECONDS =
            BUILDER.comment("SpiritBeam ignite duration in seconds").defineInRange("spiritbeamIgniteSeconds", 5, 0, 30);

    // SpiritGrasp (ProjectAvatar SpiritGrasp snaring zone).
    public static final ModConfigSpec.IntValue SPIRITGRASP_COOLDOWN_TICKS =
            BUILDER.comment("SpiritGrasp cooldown in ticks").defineInRange("spiritgraspCooldownTicks", 160, 0, 2400);
    public static final ModConfigSpec.DoubleValue SPIRITGRASP_REACH =
            BUILDER.comment("SpiritGrasp placement reach in blocks").defineInRange("spiritgraspReach", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue SPIRITGRASP_RADIUS =
            BUILDER.comment("SpiritGrasp zone radius in blocks").defineInRange("spiritgraspRadius", 4.0, 1.0, 12.0);
    public static final ModConfigSpec.DoubleValue SPIRITGRASP_DAMAGE =
            BUILDER.comment("SpiritGrasp magic damage").defineInRange("spiritgraspDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue SPIRITGRASP_DURATION_TICKS =
            BUILDER.comment("SpiritGrasp duration in ticks").defineInRange("spiritgraspDurationTicks", 200, 20, 2400);
    public static final ModConfigSpec.IntValue SPIRITGRASP_SLOW_DURATION_TICKS = BUILDER.comment(
                    "SpiritGrasp slowness duration in ticks")
            .defineInRange("spiritgraspSlowDurationTicks", 40, 0, 1200);
    public static final ModConfigSpec.IntValue SPIRITGRASP_SLOW_AMP =
            BUILDER.comment("SpiritGrasp slowness amplifier").defineInRange("spiritgraspSlowAmp", 2, 0, 10);

    // SpiritProjection (ProjectAvatar SpiritProjection out-of-body drift).
    public static final ModConfigSpec.IntValue SPIRITPROJECTION_COOLDOWN_TICKS = BUILDER.comment(
                    "SpiritProjection cooldown in ticks")
            .defineInRange("spiritprojectionCooldownTicks", 160, 0, 2400);
    public static final ModConfigSpec.IntValue SPIRITPROJECTION_CHARGE_TICKS = BUILDER.comment(
                    "SpiritProjection charge in ticks")
            .defineInRange("spiritprojectionChargeTicks", 100, 10, 1200);
    public static final ModConfigSpec.DoubleValue SPIRITPROJECTION_TETHER = BUILDER.comment(
                    "SpiritProjection tether in blocks")
            .defineInRange("spiritprojectionTether", 128.0, 8.0, 256.0);
    public static final ModConfigSpec.DoubleValue SPIRITPROJECTION_RETURN_RANGE = BUILDER.comment(
                    "SpiritProjection click-return reach in blocks")
            .defineInRange("spiritprojectionReturnRange", 6.0, 2.0, 16.0);

    // SpiritStep (ProjectAvatar SpiritStep ground step).
    public static final ModConfigSpec.IntValue SPIRITSTEP_COOLDOWN_TICKS =
            BUILDER.comment("SpiritStep cooldown in ticks").defineInRange("spiritstepCooldownTicks", 120, 0, 2400);
    public static final ModConfigSpec.DoubleValue SPIRITSTEP_RANGE =
            BUILDER.comment("SpiritStep range in blocks").defineInRange("spiritstepRange", 24.0, 4.0, 64.0);

    // LeafStorm (ProjectAddons LeafStorm combo whirl).
    public static final ModConfigSpec.IntValue LEAFSTORM_COOLDOWN_TICKS =
            BUILDER.comment("LeafStorm cooldown in ticks").defineInRange("leafstormCooldownTicks", 140, 0, 2400);
    public static final ModConfigSpec.IntValue LEAFSTORM_LEAF_COUNT =
            BUILDER.comment("LeafStorm leaf count").defineInRange("leafstormLeafCount", 10, 1, 40);
    public static final ModConfigSpec.DoubleValue LEAFSTORM_LEAF_SPEED =
            BUILDER.comment("LeafStorm degrees per tick").defineInRange("leafstormLeafSpeed", 14.0, 1.0, 45.0);
    public static final ModConfigSpec.DoubleValue LEAFSTORM_DAMAGE =
            BUILDER.comment("LeafStorm magic damage").defineInRange("leafstormDamage", 0.5, 0.0, 20.0);
    public static final ModConfigSpec.DoubleValue LEAFSTORM_RADIUS =
            BUILDER.comment("LeafStorm radius in blocks").defineInRange("leafstormRadius", 6.0, 1.0, 16.0);

    // Bending passives (ProjectKorra always-on per-element upkeep).
    public static final ModConfigSpec.IntValue PASSIVE_AIR_AGILITY_SPEED_AMP =
            BUILDER.comment("Air agility speed amplifier").defineInRange("passiveAirAgilitySpeedAmp", 1, 0, 5);
    public static final ModConfigSpec.IntValue PASSIVE_AIR_AGILITY_JUMP_AMP =
            BUILDER.comment("Air agility jump amplifier").defineInRange("passiveAirAgilityJumpAmp", 2, 0, 5);
    public static final ModConfigSpec.IntValue PASSIVE_AGILITY_DURATION_TICKS = BUILDER.comment(
                    "Agility refresh duration in ticks")
            .defineInRange("passiveAgilityDurationTicks", 10, 2, 100);
    public static final ModConfigSpec.DoubleValue PASSIVE_SATURATION_FACTOR =
            BUILDER.comment("Air hunger exhaustion factor").defineInRange("passiveSaturationFactor", 0.3, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue PASSIVE_FERRO_REACH =
            BUILDER.comment("FerroControl reach in blocks").defineInRange("passiveFerroReach", 5.0, 1.0, 16.0);
    public static final ModConfigSpec.IntValue PASSIVE_FERRO_DEBOUNCE_TICKS =
            BUILDER.comment("FerroControl debounce in ticks").defineInRange("passiveFerroDebounceTicks", 4, 0, 40);
    public static final ModConfigSpec.IntValue PASSIVE_FIRE_GLOW_INTERVAL_TICKS = BUILDER.comment(
                    "Fire glow check interval in ticks")
            .defineInRange("passiveFireGlowIntervalTicks", 20, 1, 200);
    // ================= BATCH: WaterEarthAudit =================
    // Jets (ProjectAddons jets)
    public static final ModConfigSpec.IntValue JETS_COOLDOWN_MIN_MS =
            BUILDER.comment("Jets minimum cooldown in ms").defineInRange("jetsCooldownMinMs", 4000, 0, 60000);
    public static final ModConfigSpec.IntValue JETS_COOLDOWN_MAX_MS =
            BUILDER.comment("Jets maximum cooldown in ms").defineInRange("jetsCooldownMaxMs", 12000, 0, 120000);
    public static final ModConfigSpec.IntValue JETS_DURATION_MS =
            BUILDER.comment("Jets duration in ms").defineInRange("jetsDurationMs", 20000, 0, 120000);
    public static final ModConfigSpec.DoubleValue JETS_FLY_SPEED =
            BUILDER.comment("Jets glide speed").defineInRange("jetsFlySpeed", 0.65, 0.05, 4.0);
    public static final ModConfigSpec.DoubleValue JETS_HOVER_SPEED = BUILDER.comment(
                    "Jets creative-flight speed while hovering")
            .defineInRange("jetsHoverSpeed", 0.065, 0.005, 2.0);
    public static final ModConfigSpec.DoubleValue JETS_SPEED_THRESHOLD = BUILDER.comment(
                    "Speed threshold for auto-glide on activation")
            .defineInRange("jetsSpeedThreshold", 2.4, 0.0, 10.0);
    public static final ModConfigSpec.DoubleValue JETS_DAMAGE_THRESHOLD = BUILDER.comment(
                    "Jets drops you when your health falls below its start minus this")
            .defineInRange("jetsDamageThreshold", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue JETS_MAX_HEIGHT = BUILDER.comment(
                    "Jets ceiling check below-height (-1 disables)")
            .defineInRange("jetsMaxHeight", -1, -1, 128);
    // P1 Particles FireClick: particle type+count (defaults = current values; flame() emissions skipped).
    public static final ModConfigSpec.ConfigValue<String> FIREJET_EXTINGUISH_PARTICLE =
            BUILDER.comment("FireJet extinguish particle id").define("firejetExtinguishParticle", "minecraft:cloud");
    public static final ModConfigSpec.IntValue FIREJET_EXTINGUISH_PARTICLE_COUNT = BUILDER.comment(
                    "FireJet extinguish particle count")
            .defineInRange("firejetExtinguishParticleCount", 12, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBALL_SMOKE_PARTICLE =
            BUILDER.comment("FireBall smoke particle id").define("fireballSmokeParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIREBALL_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("FireBall smoke particle count").defineInRange("fireballSmokeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESKI_SMOKE_PARTICLE =
            BUILDER.comment("FireSki smoke particle id").define("fireskiSmokeParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIRESKI_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("FireSki smoke particle count").defineInRange("fireskiSmokeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBURST_RING_PARTICLE =
            BUILDER.comment("FireBurst ring particle id").define("fireburstRingParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIREBURST_RING_PARTICLE_COUNT =
            BUILDER.comment("FireBurst ring particle count").defineInRange("fireburstRingParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESHIELD_SHELL_PARTICLE =
            BUILDER.comment("FireShield shell particle id").define("fireshieldShellParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIRESHIELD_SHELL_PARTICLE_COUNT =
            BUILDER.comment("FireShield shell particle count").defineInRange("fireshieldShellParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBREATH_TRAIL_PARTICLE =
            BUILDER.comment("FireBreath trail particle id").define("firebreathTrailParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIREBREATH_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireBreath trail particle count").defineInRange("firebreathTrailParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREWAVE_WALL_PARTICLE =
            BUILDER.comment("FireWave wall particle id").define("firewaveWallParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIREWAVE_WALL_PARTICLE_COUNT =
            BUILDER.comment("FireWave wall particle count").defineInRange("firewaveWallParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_RING_PARTICLE =
            BUILDER.comment("FireComet ring particle id").define("firecometRingParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIRECOMET_RING_PARTICLE_COUNT =
            BUILDER.comment("FireComet ring particle count").defineInRange("firecometRingParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_BLAST_PARTICLE =
            BUILDER.comment("FireComet blast particle id").define("firecometBlastParticle", "minecraft:explosion");
    public static final ModConfigSpec.IntValue FIRECOMET_BLAST_PARTICLE_COUNT =
            BUILDER.comment("FireComet blast particle count").defineInRange("firecometBlastParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_SPARK_PARTICLE =
            BUILDER.comment("FireComet spark particle id").define("firecometSparkParticle", "minecraft:firework");
    public static final ModConfigSpec.IntValue FIRECOMET_SPARK_PARTICLE_COUNT =
            BUILDER.comment("FireComet spark particle count").defineInRange("firecometSparkParticleCount", 10, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESHOTS_TRAIL_PARTICLE =
            BUILDER.comment("FireShots trail particle id").define("fireshotsTrailParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIRESHOTS_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireShots trail particle count").defineInRange("fireshotsTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREMANIPULATION_ORB_PARTICLE = BUILDER.comment(
                    "FireManipulation orb particle id")
            .define("firemanipulationOrbParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue FIREMANIPULATION_ORB_PARTICLE_COUNT = BUILDER.comment(
                    "FireManipulation orb particle count")
            .defineInRange("firemanipulationOrbParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_COOK_PARTICLE =
            BUILDER.comment("HeatControl cook particle id").define("heatcontrolCookParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue HEATCONTROL_COOK_PARTICLE_COUNT =
            BUILDER.comment("HeatControl cook particle count").defineInRange("heatcontrolCookParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_EXTINGUISH_PARTICLE = BUILDER.comment(
                    "HeatControl extinguish particle id")
            .define("heatcontrolExtinguishParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue HEATCONTROL_EXTINGUISH_PARTICLE_COUNT = BUILDER.comment(
                    "HeatControl extinguish particle count")
            .defineInRange("heatcontrolExtinguishParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_SOLIDIFY_PARTICLE = BUILDER.comment(
                    "HeatControl solidify particle id")
            .define("heatcontrolSolidifyParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue HEATCONTROL_SOLIDIFY_PARTICLE_COUNT = BUILDER.comment(
                    "HeatControl solidify particle count")
            .defineInRange("heatcontrolSolidifyParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_STONE_PARTICLE =
            BUILDER.comment("HeatControl stone particle id").define("heatcontrolStoneParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue HEATCONTROL_STONE_PARTICLE_COUNT = BUILDER.comment(
                    "HeatControl stone particle count")
            .defineInRange("heatcontrolStoneParticleCount", 3, 0, 64);
    // P1 follow-up: flame emission type+count (defaults = current literals; blue-fire holders stay soul-blue via
    // helper).
    public static final ModConfigSpec.ConfigValue<String> FIREJET_TRAIL_PARTICLE =
            BUILDER.comment("FireJet trail particle id").define("firejetTrailParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREJET_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireJet trail particle count").defineInRange("firejetTrailParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREKICK_TRAIL_PARTICLE =
            BUILDER.comment("FireKick trail particle id").define("firekickTrailParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREKICK_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireKick trail particle count").defineInRange("firekickTrailParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESPIN_TRAIL_PARTICLE =
            BUILDER.comment("FireSpin trail particle id").define("firespinTrailParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIRESPIN_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireSpin trail particle count").defineInRange("firespinTrailParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREWHEEL_RING_PARTICLE =
            BUILDER.comment("FireWheel ring particle id").define("firewheelRingParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREWHEEL_RING_PARTICLE_COUNT =
            BUILDER.comment("FireWheel ring particle count").defineInRange("firewheelRingParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREDISC_RING_PARTICLE =
            BUILDER.comment("FireDisc ring particle id").define("firediscRingParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREDISC_RING_PARTICLE_COUNT =
            BUILDER.comment("FireDisc ring particle count").defineInRange("firediscRingParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBALL_HIT_PARTICLE =
            BUILDER.comment("FireBall hit particle id").define("fireballHitParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREBALL_HIT_PARTICLE_COUNT =
            BUILDER.comment("FireBall hit particle count").defineInRange("fireballHitParticleCount", 10, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBALL_TRAIL_PARTICLE =
            BUILDER.comment("FireBall trail particle id").define("fireballTrailParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREBALL_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireBall trail particle count").defineInRange("fireballTrailParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESKI_RING_PARTICLE =
            BUILDER.comment("FireSki ring particle id").define("fireskiRingParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIRESKI_RING_PARTICLE_COUNT =
            BUILDER.comment("FireSki ring particle count").defineInRange("fireskiRingParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WALLOFFIRE_WALL_PARTICLE =
            BUILDER.comment("WallOfFire wall particle id").define("walloffireWallParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue WALLOFFIRE_WALL_PARTICLE_COUNT =
            BUILDER.comment("WallOfFire wall particle count").defineInRange("walloffireWallParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ILLUMINATION_WISP_PARTICLE =
            BUILDER.comment("Illumination wisp particle id").define("illuminationWispParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue ILLUMINATION_WISP_PARTICLE_COUNT = BUILDER.comment(
                    "Illumination wisp particle count")
            .defineInRange("illuminationWispParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBURST_CHARGE_PARTICLE =
            BUILDER.comment("FireBurst charge particle id").define("fireburstChargeParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREBURST_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst charge particle count").defineInRange("fireburstChargeParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBURST_BURST_PARTICLE =
            BUILDER.comment("FireBurst burst particle id").define("fireburstBurstParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREBURST_BURST_PARTICLE_COUNT =
            BUILDER.comment("FireBurst burst particle count").defineInRange("fireburstBurstParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBURST_SPHERE_PARTICLE =
            BUILDER.comment("FireBurst sphere particle id").define("fireburstSphereParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREBURST_SPHERE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst sphere particle count").defineInRange("fireburstSphereParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBURST_CONE_PARTICLE =
            BUILDER.comment("FireBurst cone particle id").define("fireburstConeParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREBURST_CONE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst cone particle count").defineInRange("fireburstConeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREBURST_CORE_PARTICLE =
            BUILDER.comment("FireBurst core particle id").define("fireburstCoreParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREBURST_CORE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst core particle count").defineInRange("fireburstCoreParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESHIELD_FLAME_PARTICLE =
            BUILDER.comment("FireShield flame particle id").define("fireshieldFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIRESHIELD_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireShield flame particle count").defineInRange("fireshieldFlameParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESHIELD_DEFLECT_PARTICLE =
            BUILDER.comment("FireShield deflect particle id").define("fireshieldDeflectParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIRESHIELD_DEFLECT_PARTICLE_COUNT = BUILDER.comment(
                    "FireShield deflect particle count")
            .defineInRange("fireshieldDeflectParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FLAMEBREATH_BREATH_PARTICLE =
            BUILDER.comment("FlameBreath breath particle id").define("flamebreathBreathParticle", "minecraft:flame");
    public static final ModConfigSpec.ConfigValue<String> FIREBREATH_FLAME_PARTICLE =
            BUILDER.comment("FireBreath flame particle id").define("firebreathFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.ConfigValue<String> FIREWAVE_FLAME_PARTICLE =
            BUILDER.comment("FireWave flame particle id").define("firewaveFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREWAVE_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireWave flame particle count").defineInRange("firewaveFlameParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_FLAME_PARTICLE =
            BUILDER.comment("FireComet flame particle id").define("firecometFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIRECOMET_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireComet flame particle count").defineInRange("firecometFlameParticleCount", 20, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESHOTS_STOCK_PARTICLE =
            BUILDER.comment("FireShots stock particle id").define("fireshotsStockParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIRESHOTS_STOCK_PARTICLE_COUNT = BUILDER.comment(
                    "FireShots stock particle base count")
            .defineInRange("fireshotsStockParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIRESHOTS_FLAME_PARTICLE =
            BUILDER.comment("FireShots flame particle id").define("fireshotsFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIRESHOTS_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireShots flame particle count").defineInRange("fireshotsFlameParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FIREMANIPULATION_FLAME_PARTICLE = BUILDER.comment(
                    "FireManipulation flame particle id")
            .define("firemanipulationFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue FIREMANIPULATION_FLAME_PARTICLE_COUNT = BUILDER.comment(
                    "FireManipulation flame particle count")
            .defineInRange("firemanipulationFlameParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_FLAME_PARTICLE =
            BUILDER.comment("HeatControl flame particle id").define("heatcontrolFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue HEATCONTROL_FLAME_PARTICLE_COUNT = BUILDER.comment(
                    "HeatControl flame particle count")
            .defineInRange("heatcontrolFlameParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_FLAME_PARTICLE =
            BUILDER.comment("CombustBeam flame particle id").define("combustbeamFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue COMBUSTBEAM_FLAME_PARTICLE_COUNT = BUILDER.comment(
                    "CombustBeam flame particle count")
            .defineInRange("combustbeamFlameParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_BURST_PARTICLE =
            BUILDER.comment("CombustBeam burst particle id").define("combustbeamBurstParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue COMBUSTBEAM_BURST_PARTICLE_COUNT = BUILDER.comment(
                    "CombustBeam burst particle count")
            .defineInRange("combustbeamBurstParticleCount", 12, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> EXPLODE_FLAME_PARTICLE =
            BUILDER.comment("Explode flame particle id").define("explodeFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue EXPLODE_FLAME_PARTICLE_COUNT =
            BUILDER.comment("Explode flame particle count").defineInRange("explodeFlameParticleCount", 14, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_FLAME_PARTICLE = BUILDER.comment(
                    "CombustionBlast flame particle id")
            .define("combustionblastFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_FLAME_PARTICLE_COUNT = BUILDER.comment(
                    "CombustionBlast flame particle count")
            .defineInRange("combustionblastFlameParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_RING_PARTICLE = BUILDER.comment(
                    "CombustionBlast ring particle id")
            .define("combustionblastRingParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_RING_PARTICLE_COUNT = BUILDER.comment(
                    "CombustionBlast ring particle count")
            .defineInRange("combustionblastRingParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_BURST_PARTICLE = BUILDER.comment(
                    "CombustionBlast burst particle id")
            .define("combustionblastBurstParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_BURST_PARTICLE_COUNT = BUILDER.comment(
                    "CombustionBlast burst particle count")
            .defineInRange("combustionblastBurstParticleCount", 16, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_BURST_PARTICLE =
            BUILDER.comment("Combustion burst particle id").define("combustionBurstParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue COMBUSTION_BURST_PARTICLE_COUNT =
            BUILDER.comment("Combustion burst particle count").defineInRange("combustionBurstParticleCount", 16, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_FLAME_PARTICLE =
            BUILDER.comment("Combustion flame particle id").define("combustionFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue COMBUSTION_FLAME_PARTICLE_COUNT =
            BUILDER.comment("Combustion flame particle count").defineInRange("combustionFlameParticleCount", 3, 0, 64);
    // P3 Particles LightningCombustion: particle type+count (defaults = previous literals).
    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_CHARGE_PARTICLE =
            BUILDER.comment("ArcSpark charge particle id").define("arcsparkChargeParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue ARCSPARK_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark charge particle count").defineInRange("arcsparkChargeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_READY_PARTICLE =
            BUILDER.comment("ArcSpark ready particle id").define("arcsparkReadyParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue ARCSPARK_READY_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark ready particle count").defineInRange("arcsparkReadyParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_GROUND_PARTICLE =
            BUILDER.comment("ArcSpark ground particle id").define("arcsparkGroundParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue ARCSPARK_GROUND_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark ground particle count").defineInRange("arcsparkGroundParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_TRAIL_PARTICLE =
            BUILDER.comment("ArcSpark trail particle id").define("arcsparkTrailParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue ARCSPARK_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark trail particle count").defineInRange("arcsparkTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> CHARGEBOLT_CHARGE_PARTICLE = BUILDER.comment(
                    "ChargeBolt charge particle id")
            .define("chargeboltChargeParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue CHARGEBOLT_CHARGE_PARTICLE_COUNT = BUILDER.comment(
                    "ChargeBolt charge particle count")
            .defineInRange("chargeboltChargeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> CHARGEBOLT_TRAIL_PARTICLE = BUILDER.comment(
                    "ChargeBolt trail particle id")
            .define("chargeboltTrailParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue CHARGEBOLT_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ChargeBolt trail particle count").defineInRange("chargeboltTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> BOLT_CHARGE_PARTICLE =
            BUILDER.comment("Bolt charge particle id").define("boltChargeParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue BOLT_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("Bolt charge particle count").defineInRange("boltChargeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> BOLT_STRIKE_PARTICLE =
            BUILDER.comment("Bolt strike particle id").define("boltStrikeParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue BOLT_STRIKE_PARTICLE_COUNT =
            BUILDER.comment("Bolt strike particle count").defineInRange("boltStrikeParticleCount", 12, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> BOLT_BEAM_PARTICLE =
            BUILDER.comment("Bolt beam particle id").define("boltBeamParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue BOLT_BEAM_PARTICLE_COUNT =
            BUILDER.comment("Bolt beam particle count").defineInRange("boltBeamParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> DISCHARGE_TRAIL_PARTICLE =
            BUILDER.comment("Discharge trail particle id").define("dischargeTrailParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue DISCHARGE_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("Discharge trail particle count").defineInRange("dischargeTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> DISCHARGE_HIT_PARTICLE =
            BUILDER.comment("Discharge hit particle id").define("dischargeHitParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue DISCHARGE_HIT_PARTICLE_COUNT =
            BUILDER.comment("Discharge hit particle count").defineInRange("dischargeHitParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_LINGER_PARTICLE = BUILDER.comment(
                    "Lightning linger particle id")
            .define("lightningLingerParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNING_LINGER_PARTICLE_COUNT = BUILDER.comment(
                    "Lightning linger particle base count")
            .defineInRange("lightningLingerParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_GATHER_PARTICLE = BUILDER.comment(
                    "Lightning gather particle id")
            .define("lightningGatherParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNING_GATHER_PARTICLE_COUNT =
            BUILDER.comment("Lightning gather particle count").defineInRange("lightningGatherParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_CHARGE_PARTICLE = BUILDER.comment(
                    "Lightning charge particle id")
            .define("lightningChargeParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNING_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("Lightning charge particle count").defineInRange("lightningChargeParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_ROD_PARTICLE =
            BUILDER.comment("Lightning rod particle id").define("lightningRodParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNING_ROD_PARTICLE_COUNT =
            BUILDER.comment("Lightning rod particle count").defineInRange("lightningRodParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_BEAM_PARTICLE =
            BUILDER.comment("Lightning beam particle id").define("lightningBeamParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNING_BEAM_PARTICLE_COUNT =
            BUILDER.comment("Lightning beam particle count").defineInRange("lightningBeamParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNINGBURST_TRAIL_PARTICLE = BUILDER.comment(
                    "LightningBurst trail particle id")
            .define("lightningburstTrailParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_TRAIL_PARTICLE_COUNT = BUILDER.comment(
                    "LightningBurst trail particle count")
            .defineInRange("lightningburstTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNINGBURST_CHARGE_PARTICLE = BUILDER.comment(
                    "LightningBurst charge particle id")
            .define("lightningburstChargeParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_CHARGE_PARTICLE_COUNT = BUILDER.comment(
                    "LightningBurst charge particle count")
            .defineInRange("lightningburstChargeParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LIGHTNINGBURST_CORE_PARTICLE = BUILDER.comment(
                    "LightningBurst core particle id")
            .define("lightningburstCoreParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue LIGHTNINGBURST_CORE_PARTICLE_COUNT = BUILDER.comment(
                    "LightningBurst core particle count")
            .defineInRange("lightningburstCoreParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ELECTRIFY_FIELD_PARTICLE =
            BUILDER.comment("Electrify field particle id").define("electrifyFieldParticle", "minecraft:electric_spark");
    public static final ModConfigSpec.IntValue ELECTRIFY_FIELD_PARTICLE_COUNT =
            BUILDER.comment("Electrify field particle count").defineInRange("electrifyFieldParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_SMOKE_PARTICLE =
            BUILDER.comment("CombustBeam smoke particle id").define("combustbeamSmokeParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue COMBUSTBEAM_SMOKE_PARTICLE_COUNT = BUILDER.comment(
                    "CombustBeam smoke particle count")
            .defineInRange("combustbeamSmokeParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_BLAST_PARTICLE =
            BUILDER.comment("CombustBeam blast particle id").define("combustbeamBlastParticle", "minecraft:explosion");
    public static final ModConfigSpec.IntValue COMBUSTBEAM_BLAST_PARTICLE_COUNT = BUILDER.comment(
                    "CombustBeam blast particle count")
            .defineInRange("combustbeamBlastParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> EXPLODE_TARGET_PARTICLE =
            BUILDER.comment("Explode target particle id").define("explodeTargetParticle", "minecraft:crit");
    public static final ModConfigSpec.IntValue EXPLODE_TARGET_PARTICLE_COUNT =
            BUILDER.comment("Explode target particle count").defineInRange("explodeTargetParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> EXPLODE_BLAST_PARTICLE =
            BUILDER.comment("Explode blast particle id").define("explodeBlastParticle", "minecraft:explosion");
    public static final ModConfigSpec.IntValue EXPLODE_BLAST_PARTICLE_COUNT =
            BUILDER.comment("Explode blast particle count").defineInRange("explodeBlastParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> EXPLODE_HIT_PARTICLE =
            BUILDER.comment("Explode hit particle id").define("explodeHitParticle", "minecraft:crit");
    public static final ModConfigSpec.IntValue EXPLODE_HIT_PARTICLE_COUNT =
            BUILDER.comment("Explode hit particle count").defineInRange("explodeHitParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_TRAIL_PARTICLE = BUILDER.comment(
                    "CombustionBlast trail particle id")
            .define("combustionblastTrailParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_TRAIL_PARTICLE_COUNT = BUILDER.comment(
                    "CombustionBlast trail particle count")
            .defineInRange("combustionblastTrailParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_SPARK_PARTICLE = BUILDER.comment(
                    "CombustionBlast spark particle id")
            .define("combustionblastSparkParticle", "minecraft:firework");
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_SPARK_PARTICLE_COUNT = BUILDER.comment(
                    "CombustionBlast spark particle count")
            .defineInRange("combustionblastSparkParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_CHARGE_PARTICLE = BUILDER.comment(
                    "CombustionBlast charge particle id")
            .define("combustionblastChargeParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_CHARGE_PARTICLE_COUNT = BUILDER.comment(
                    "CombustionBlast charge particle count")
            .defineInRange("combustionblastChargeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_BLAST_PARTICLE = BUILDER.comment(
                    "CombustionBlast blast particle id")
            .define("combustionblastBlastParticle", "minecraft:explosion");
    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_BLAST_PARTICLE_COUNT = BUILDER.comment(
                    "CombustionBlast blast particle count")
            .defineInRange("combustionblastBlastParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_BLAST_PARTICLE =
            BUILDER.comment("Combustion blast particle id").define("combustionBlastParticle", "minecraft:explosion");
    public static final ModConfigSpec.IntValue COMBUSTION_BLAST_PARTICLE_COUNT =
            BUILDER.comment("Combustion blast particle count").defineInRange("combustionBlastParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_FIZZ_PARTICLE =
            BUILDER.comment("Combustion fizz particle id").define("combustionFizzParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue COMBUSTION_FIZZ_PARTICLE_COUNT =
            BUILDER.comment("Combustion fizz particle count").defineInRange("combustionFizzParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_TRAIL_PARTICLE =
            BUILDER.comment("Combustion trail particle id").define("combustionTrailParticle", "minecraft:firework");
    public static final ModConfigSpec.IntValue COMBUSTION_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("Combustion trail particle count").defineInRange("combustionTrailParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_SMOKE_PARTICLE =
            BUILDER.comment("Combustion smoke particle id").define("combustionSmokeParticle", "minecraft:large_smoke");
    public static final ModConfigSpec.IntValue COMBUSTION_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("Combustion smoke particle count").defineInRange("combustionSmokeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRJET_EXTINGUISH_PARTICLE =
            BUILDER.comment("AirJet extinguish particle id").define("airjetExtinguishParticle", "minecraft:bubble");
    public static final ModConfigSpec.IntValue AIRJET_EXTINGUISH_PARTICLE_COUNT = BUILDER.comment(
                    "AirJet extinguish particle count")
            .defineInRange("airjetExtinguishParticleCount", 12, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRJET_TRAIL_LEFT_PARTICLE =
            BUILDER.comment("AirJet trail left particle id").define("airjetTrailLeftParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRJET_TRAIL_LEFT_PARTICLE_COUNT =
            BUILDER.comment("AirJet trail left particle count").defineInRange("airjetTrailLeftParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRJET_TRAIL_RIGHT_PARTICLE = BUILDER.comment(
                    "AirJet trail right particle id")
            .define("airjetTrailRightParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRJET_TRAIL_RIGHT_PARTICLE_COUNT = BUILDER.comment(
                    "AirJet trail right particle count")
            .defineInRange("airjetTrailRightParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSCOOTER_SPIN_PARTICLE =
            BUILDER.comment("AirScooter spin particle id").define("airscooterSpinParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSCOOTER_SPIN_PARTICLE_COUNT =
            BUILDER.comment("AirScooter spin particle count").defineInRange("airscooterSpinParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSCOOTER_SPIN_MIRROR_PARTICLE = BUILDER.comment(
                    "AirScooter spin mirror particle id")
            .define("airscooterSpinMirrorParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSCOOTER_SPIN_MIRROR_PARTICLE_COUNT = BUILDER.comment(
                    "AirScooter spin mirror particle count")
            .defineInRange("airscooterSpinMirrorParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSCOOTER_RING_PARTICLE =
            BUILDER.comment("AirScooter ring particle id").define("airscooterRingParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSCOOTER_RING_PARTICLE_COUNT =
            BUILDER.comment("AirScooter ring particle count").defineInRange("airscooterRingParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSPOUT_COLUMN_PARTICLE =
            BUILDER.comment("AirSpout column particle id").define("airspoutColumnParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSPOUT_COLUMN_PARTICLE_COUNT =
            BUILDER.comment("AirSpout column particle count").defineInRange("airspoutColumnParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRPUNCH_BOLT_PARTICLE =
            BUILDER.comment("AirPunch bolt particle id").define("airpunchBoltParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRPUNCH_BOLT_PARTICLE_COUNT =
            BUILDER.comment("AirPunch bolt particle count").defineInRange("airpunchBoltParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSLAM_TARGET_PARTICLE =
            BUILDER.comment("AirSlam target particle id").define("airslamTargetParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSLAM_TARGET_PARTICLE_COUNT =
            BUILDER.comment("AirSlam target particle count").defineInRange("airslamTargetParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRFLIGHT_TRAIL_PARTICLE =
            BUILDER.comment("AirFlight trail particle id").define("airflightTrailParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRFLIGHT_TRAIL_PARTICLE_COUNT = BUILDER.comment(
                    "AirFlight trail base particle count (plus 2 per Soar speed step)")
            .defineInRange("airflightTrailParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSHIELD_RING_PARTICLE =
            BUILDER.comment("AirShield ring particle id").define("airshieldRingParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSHIELD_RING_PARTICLE_COUNT =
            BUILDER.comment("AirShield ring particle count").defineInRange("airshieldRingParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBURST_CHARGE_PARTICLE =
            BUILDER.comment("AirBurst charge particle id").define("airburstChargeParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBURST_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("AirBurst charge particle count").defineInRange("airburstChargeParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBURST_RING_PARTICLE =
            BUILDER.comment("AirBurst ring particle id").define("airburstRingParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBURST_RING_PARTICLE_COUNT =
            BUILDER.comment("AirBurst ring particle count").defineInRange("airburstRingParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBURST_SHELL_PARTICLE =
            BUILDER.comment("AirBurst shell particle id").define("airburstShellParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBURST_SHELL_PARTICLE_COUNT =
            BUILDER.comment("AirBurst shell particle count").defineInRange("airburstShellParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBLAST_MARKER_PARTICLE =
            BUILDER.comment("AirBlast marker particle id").define("airblastMarkerParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBLAST_MARKER_PARTICLE_COUNT =
            BUILDER.comment("AirBlast marker particle count").defineInRange("airblastMarkerParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBLAST_TRAIL_PARTICLE =
            BUILDER.comment("AirBlast trail particle id").define("airblastTrailParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBLAST_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("AirBlast trail particle count").defineInRange("airblastTrailParticleCount", 10, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSWIPE_CHARGE_PARTICLE =
            BUILDER.comment("AirSwipe charge particle id").define("airswipeChargeParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSWIPE_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("AirSwipe charge particle count").defineInRange("airswipeChargeParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSWIPE_STREAM_PARTICLE =
            BUILDER.comment("AirSwipe stream particle id").define("airswipeStreamParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSWIPE_STREAM_PARTICLE_COUNT =
            BUILDER.comment("AirSwipe stream particle count").defineInRange("airswipeStreamParticleCount", 6, 0, 64);
    // ================= P5 Particles AirTwo (particle type+count; defaults = current values) =================
    public static final ModConfigSpec.ConfigValue<String> AIRSUCTION_MAIN_PARTICLE =
            BUILDER.comment("AirSuction main particle id").define("airsuctionMainParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSUCTION_MAIN_PARTICLE_COUNT =
            BUILDER.comment("AirSuction main particle count").defineInRange("airsuctionMainParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSUCTION_STREAM_PARTICLE =
            BUILDER.comment("AirSuction stream particle id").define("airsuctionStreamParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSUCTION_STREAM_PARTICLE_COUNT = BUILDER.comment(
                    "AirSuction stream particle count")
            .defineInRange("airsuctionStreamParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_CHARGE_PARTICLE =
            BUILDER.comment("Suffocate charge particle id").define("suffocateChargeParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue SUFFOCATE_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("Suffocate charge particle count").defineInRange("suffocateChargeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_SPIRAL_PARTICLE =
            BUILDER.comment("Suffocate spiral particle id").define("suffocateSpiralParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue SUFFOCATE_SPIRAL_PARTICLE_COUNT =
            BUILDER.comment("Suffocate spiral particle count").defineInRange("suffocateSpiralParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_SPIRAL_MIRROR_PARTICLE = BUILDER.comment(
                    "Suffocate spiral mirror particle id")
            .define("suffocateSpiralMirrorParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue SUFFOCATE_SPIRAL_MIRROR_PARTICLE_COUNT = BUILDER.comment(
                    "Suffocate spiral mirror particle count")
            .defineInRange("suffocateSpiralMirrorParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_SPIRAL_UPRIGHT_PARTICLE = BUILDER.comment(
                    "Suffocate spiral upright particle id")
            .define("suffocateSpiralUprightParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue SUFFOCATE_SPIRAL_UPRIGHT_PARTICLE_COUNT = BUILDER.comment(
                    "Suffocate spiral upright particle count")
            .defineInRange("suffocateSpiralUprightParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> TORNADO_FUNNEL_PARTICLE =
            BUILDER.comment("Tornado funnel particle id").define("tornadoFunnelParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue TORNADO_FUNNEL_PARTICLE_COUNT =
            BUILDER.comment("Tornado funnel particle count").defineInRange("tornadoFunnelParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBREATH_CONE_PARTICLE =
            BUILDER.comment("AirBreath cone particle id").define("airbreathConeParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBREATH_CONE_PARTICLE_COUNT =
            BUILDER.comment("AirBreath cone particle count").defineInRange("airbreathConeParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_ARMED_RING_PARTICLE = BUILDER.comment(
                    "AirBullet armed ring particle id")
            .define("airbulletArmedRingParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBULLET_ARMED_RING_PARTICLE_COUNT = BUILDER.comment(
                    "AirBullet armed ring particle count")
            .defineInRange("airbulletArmedRingParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_ARMED_CORE_PARTICLE = BUILDER.comment(
                    "AirBullet armed core particle id")
            .define("airbulletArmedCoreParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBULLET_ARMED_CORE_PARTICLE_COUNT = BUILDER.comment(
                    "AirBullet armed core particle count")
            .defineInRange("airbulletArmedCoreParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_IMPACT_PARTICLE =
            BUILDER.comment("AirBullet impact particle id").define("airbulletImpactParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBULLET_IMPACT_PARTICLE_COUNT =
            BUILDER.comment("AirBullet impact particle count").defineInRange("airbulletImpactParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_TRACER_PARTICLE =
            BUILDER.comment("AirBullet tracer particle id").define("airbulletTracerParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBULLET_TRACER_PARTICLE_COUNT =
            BUILDER.comment("AirBullet tracer particle count").defineInRange("airbulletTracerParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_HIT_PARTICLE =
            BUILDER.comment("AirBullet hit particle id").define("airbulletHitParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBULLET_HIT_PARTICLE_COUNT =
            BUILDER.comment("AirBullet hit particle count").defineInRange("airbulletHitParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_GATHER_PARTICLE =
            BUILDER.comment("AirBullet gather particle id").define("airbulletGatherParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBULLET_GATHER_PARTICLE_COUNT =
            BUILDER.comment("AirBullet gather particle count").defineInRange("airbulletGatherParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_CHARGED_PARTICLE =
            BUILDER.comment("AirBullet charged particle id").define("airbulletChargedParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRBULLET_CHARGED_PARTICLE_COUNT = BUILDER.comment(
                    "AirBullet charged particle count")
            .defineInRange("airbulletChargedParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> MEDITATE_BLESSING_PARTICLE =
            BUILDER.comment("Meditate blessing particle id").define("meditateBlessingParticle", "minecraft:witch");
    public static final ModConfigSpec.IntValue MEDITATE_BLESSING_PARTICLE_COUNT = BUILDER.comment(
                    "Meditate blessing particle count")
            .defineInRange("meditateBlessingParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> MEDITATE_FOCUS_PARTICLE =
            BUILDER.comment("Meditate focus particle id").define("meditateFocusParticle", "minecraft:enchant");
    public static final ModConfigSpec.IntValue MEDITATE_FOCUS_PARTICLE_COUNT =
            BUILDER.comment("Meditate focus particle count").defineInRange("meditateFocusParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SONICBLAST_CHARGE_PARTICLE =
            BUILDER.comment("SonicBlast charge particle id").define("sonicblastChargeParticle", "minecraft:note");
    public static final ModConfigSpec.IntValue SONICBLAST_CHARGE_PARTICLE_COUNT = BUILDER.comment(
                    "SonicBlast charge particle count")
            .defineInRange("sonicblastChargeParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SONICBLAST_RING_PARTICLE =
            BUILDER.comment("SonicBlast ring particle id").define("sonicblastRingParticle", "minecraft:note");
    public static final ModConfigSpec.IntValue SONICBLAST_RING_PARTICLE_COUNT =
            BUILDER.comment("SonicBlast ring particle count").defineInRange("sonicblastRingParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ZEPHYR_DRIFT_PARTICLE =
            BUILDER.comment("Zephyr drift particle id").define("zephyrDriftParticle", "minecraft:cloud");
    public static final ModConfigSpec.IntValue ZEPHYR_DRIFT_PARTICLE_COUNT =
            BUILDER.comment("Zephyr drift particle count").defineInRange("zephyrDriftParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ZEPHYR_RING_PARTICLE =
            BUILDER.comment("Zephyr ring particle id").define("zephyrRingParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue ZEPHYR_RING_PARTICLE_COUNT =
            BUILDER.comment("Zephyr ring particle count").defineInRange("zephyrRingParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSTREAM_STREAM_PARTICLE =
            BUILDER.comment("AirStream stream particle id").define("airstreamStreamParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSTREAM_STREAM_PARTICLE_COUNT =
            BUILDER.comment("AirStream stream particle count").defineInRange("airstreamStreamParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AIRSTREAM_HEAD_PARTICLE =
            BUILDER.comment("AirStream head particle id").define("airstreamHeadParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue AIRSTREAM_HEAD_PARTICLE_COUNT =
            BUILDER.comment("AirStream head particle count").defineInRange("airstreamHeadParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> AVATARSTATE_MAIN_PARTICLE =
            BUILDER.comment("AvatarState main particle id").define("avatarstateMainParticle", "minecraft:end_rod");
    public static final ModConfigSpec.IntValue AVATARSTATE_MAIN_PARTICLE_COUNT =
            BUILDER.comment("AvatarState main particle count").defineInRange("avatarstateMainParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ELEMENTSPHERE_AIR_PARTICLE =
            BUILDER.comment("ElementSphere air particle id").define("elementsphereAirParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_AIR_PARTICLE_COUNT = BUILDER.comment(
                    "ElementSphere air particle count")
            .defineInRange("elementsphereAirParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ELEMENTSPHERE_FIRE_PARTICLE =
            BUILDER.comment("ElementSphere fire particle id").define("elementsphereFireParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_FIRE_PARTICLE_COUNT = BUILDER.comment(
                    "ElementSphere fire particle count")
            .defineInRange("elementsphereFireParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ELEMENTSPHERE_BUBBLE_PARTICLE = BUILDER.comment(
                    "ElementSphere bubble particle id")
            .define("elementsphereBubbleParticle", "minecraft:bubble");
    public static final ModConfigSpec.IntValue ELEMENTSPHERE_BUBBLE_PARTICLE_COUNT = BUILDER.comment(
                    "ElementSphere bubble particle count")
            .defineInRange("elementsphereBubbleParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESAIR_TRAIL_PARTICLE =
            BUILDER.comment("ESAir trail particle id").define("esairTrailParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue ESAIR_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESAir trail particle count").defineInRange("esairTrailParticleCount", 14, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESEARTH_TRAIL_PARTICLE =
            BUILDER.comment("ESEarth trail particle id").define("esearthTrailParticle", "minecraft:large_smoke");
    public static final ModConfigSpec.IntValue ESEARTH_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESEarth trail particle count").defineInRange("esearthTrailParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESEARTH_BLAST_PARTICLE =
            BUILDER.comment("ESEarth blast particle id").define("esearthBlastParticle", "minecraft:explosion");
    public static final ModConfigSpec.IntValue ESEARTH_BLAST_PARTICLE_COUNT =
            BUILDER.comment("ESEarth blast particle count").defineInRange("esearthBlastParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESEARTH_BLAST_SMOKE_PARTICLE = BUILDER.comment(
                    "ESEarth blast smoke particle id")
            .define("esearthBlastSmokeParticle", "minecraft:large_smoke");
    public static final ModConfigSpec.IntValue ESEARTH_BLAST_SMOKE_PARTICLE_COUNT = BUILDER.comment(
                    "ESEarth blast smoke particle count")
            .defineInRange("esearthBlastSmokeParticleCount", 15, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESFIRE_TRAIL_PARTICLE =
            BUILDER.comment("ESFire trail particle id").define("esfireTrailParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue ESFIRE_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESFire trail particle count").defineInRange("esfireTrailParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESFIRE_SMOKE_PARTICLE =
            BUILDER.comment("ESFire smoke particle id").define("esfireSmokeParticle", "minecraft:large_smoke");
    public static final ModConfigSpec.IntValue ESFIRE_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("ESFire smoke particle count").defineInRange("esfireSmokeParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESWATER_TRAIL_PARTICLE =
            BUILDER.comment("ESWater trail particle id").define("eswaterTrailParticle", "minecraft:bubble");
    public static final ModConfigSpec.IntValue ESWATER_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESWater trail particle count").defineInRange("eswaterTrailParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_TRAIL_PARTICLE =
            BUILDER.comment("ESStream trail particle id").define("esstreamTrailParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue ESSTREAM_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESStream trail particle count").defineInRange("esstreamTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_GUST_PARTICLE =
            BUILDER.comment("ESStream gust particle id").define("esstreamGustParticle", "minecraft:small_gust");
    public static final ModConfigSpec.IntValue ESSTREAM_GUST_PARTICLE_COUNT =
            BUILDER.comment("ESStream gust particle count").defineInRange("esstreamGustParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_IMPACT_PARTICLE =
            BUILDER.comment("ESStream impact particle id").define("esstreamImpactParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue ESSTREAM_IMPACT_PARTICLE_COUNT =
            BUILDER.comment("ESStream impact particle count").defineInRange("esstreamImpactParticleCount", 20, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_IMPACT_SMOKE_PARTICLE = BUILDER.comment(
                    "ESStream impact smoke particle id")
            .define("esstreamImpactSmokeParticle", "minecraft:large_smoke");
    public static final ModConfigSpec.IntValue ESSTREAM_IMPACT_SMOKE_PARTICLE_COUNT = BUILDER.comment(
                    "ESStream impact smoke particle count")
            .defineInRange("esstreamImpactSmokeParticleCount", 20, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_SPARK_PARTICLE =
            BUILDER.comment("ESStream spark particle id").define("esstreamSparkParticle", "minecraft:firework");
    public static final ModConfigSpec.IntValue ESSTREAM_SPARK_PARTICLE_COUNT =
            BUILDER.comment("ESStream spark particle count").defineInRange("esstreamSparkParticleCount", 20, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_BLAST_PARTICLE =
            BUILDER.comment("ESStream blast particle id").define("esstreamBlastParticle", "minecraft:explosion");
    public static final ModConfigSpec.IntValue ESSTREAM_BLAST_PARTICLE_COUNT =
            BUILDER.comment("ESStream blast particle count").defineInRange("esstreamBlastParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SPIRITBEAM_WITCH_PARTICLE =
            BUILDER.comment("SpiritBeam witch particle id").define("spiritbeamWitchParticle", "minecraft:witch");
    public static final ModConfigSpec.IntValue SPIRITBEAM_WITCH_PARTICLE_COUNT =
            BUILDER.comment("SpiritBeam witch particle count").defineInRange("spiritbeamWitchParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SPIRITBEAM_PORTAL_PARTICLE =
            BUILDER.comment("SpiritBeam portal particle id").define("spiritbeamPortalParticle", "minecraft:portal");
    public static final ModConfigSpec.IntValue SPIRITBEAM_PORTAL_PARTICLE_COUNT = BUILDER.comment(
                    "SpiritBeam portal particle count")
            .defineInRange("spiritbeamPortalParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SPIRITGRASP_RING_PARTICLE = BUILDER.comment(
                    "SpiritGrasp ring particle id")
            .define("spiritgraspRingParticle", "minecraft:happy_villager");
    public static final ModConfigSpec.IntValue SPIRITGRASP_RING_PARTICLE_COUNT =
            BUILDER.comment("SpiritGrasp ring particle count").defineInRange("spiritgraspRingParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SPIRITGRASP_INNER_PARTICLE =
            BUILDER.comment("SpiritGrasp inner particle id").define("spiritgraspInnerParticle", "minecraft:witch");
    public static final ModConfigSpec.IntValue SPIRITGRASP_INNER_PARTICLE_COUNT = BUILDER.comment(
                    "SpiritGrasp inner particle count")
            .defineInRange("spiritgraspInnerParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SPIRITPROJECTION_CHARGE_PARTICLE = BUILDER.comment(
                    "SpiritProjection charge particle id")
            .define("spiritprojectionChargeParticle", "minecraft:end_rod");
    public static final ModConfigSpec.IntValue SPIRITPROJECTION_CHARGE_PARTICLE_COUNT = BUILDER.comment(
                    "SpiritProjection charge particle count")
            .defineInRange("spiritprojectionChargeParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SPIRITSTEP_BURST_PARTICLE =
            BUILDER.comment("SpiritStep burst particle id").define("spiritstepBurstParticle", "minecraft:portal");
    public static final ModConfigSpec.IntValue SPIRITSTEP_BURST_PARTICLE_COUNT =
            BUILDER.comment("SpiritStep burst particle count").defineInRange("spiritstepBurstParticleCount", 12, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SPIRITSTEP_WISP_PARTICLE =
            BUILDER.comment("SpiritStep wisp particle id").define("spiritstepWispParticle", "minecraft:end_rod");
    public static final ModConfigSpec.IntValue SPIRITSTEP_WISP_PARTICLE_COUNT =
            BUILDER.comment("SpiritStep wisp particle count").defineInRange("spiritstepWispParticleCount", 6, 0, 64);
    // P7 Particles WaterEarth: particle type+count (defaults = previous literals).
    public static final ModConfigSpec.ConfigValue<String> DIG_MAIN_PARTICLE =
            BUILDER.comment("Dig boost particle id").define("digMainParticle", "minecraft:crit");
    public static final ModConfigSpec.IntValue DIG_MAIN_PARTICLE_COUNT =
            BUILDER.comment("Dig boost particle count").defineInRange("digMainParticleCount", 7, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> DRAIN_MAIN_PARTICLE =
            BUILDER.comment("Drain mote particle id").define("drainMainParticle", "minecraft:splash");
    public static final ModConfigSpec.IntValue DRAIN_MAIN_PARTICLE_COUNT =
            BUILDER.comment("Drain mote particle count").defineInRange("drainMainParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> EARTHGLOVE_SHATTER_PARTICLE =
            BUILDER.comment("EarthGlove shatter particle id").define("earthgloveShatterParticle", "minecraft:poof");
    public static final ModConfigSpec.IntValue EARTHGLOVE_SHATTER_PARTICLE_COUNT = BUILDER.comment(
                    "EarthGlove shatter particle count")
            .defineInRange("earthgloveShatterParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> EARTHGRAB_DRAG_PARTICLE =
            BUILDER.comment("EarthGrab drag particle id").define("earthgrabDragParticle", "minecraft:poof");
    public static final ModConfigSpec.IntValue EARTHGRAB_DRAG_PARTICLE_COUNT =
            BUILDER.comment("EarthGrab drag particle count").defineInRange("earthgrabDragParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FISSURE_CRACK_PARTICLE =
            BUILDER.comment("Fissure crack particle id").define("fissureCrackParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue FISSURE_CRACK_PARTICLE_COUNT =
            BUILDER.comment("Fissure crack particle count").defineInRange("fissureCrackParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> FROSTBREATH_BEAM_PARTICLE =
            BUILDER.comment("FrostBreath beam particle id").define("frostbreathBeamParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue FROSTBREATH_BEAM_PARTICLE_COUNT =
            BUILDER.comment("FrostBreath beam particle count").defineInRange("frostbreathBeamParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICEBLAST_SETUP_PARTICLE =
            BUILDER.comment("IceBlast setup particle id").define("iceblastSetupParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICEBLAST_SETUP_PARTICLE_COUNT =
            BUILDER.comment("IceBlast setup particle count").defineInRange("iceblastSetupParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICEBLAST_TRAIL_PARTICLE =
            BUILDER.comment("IceBlast trail particle id").define("iceblastTrailParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICEBLAST_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("IceBlast trail particle count").defineInRange("iceblastTrailParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICEBLAST_SHATTER_PARTICLE =
            BUILDER.comment("IceBlast shatter particle id").define("iceblastShatterParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICEBLAST_SHATTER_PARTICLE_COUNT =
            BUILDER.comment("IceBlast shatter particle count").defineInRange("iceblastShatterParticleCount", 12, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICECLAWS_CHARGE_PARTICLE =
            BUILDER.comment("IceClaws charge particle id").define("iceclawsChargeParticle", "minecraft:splash");
    public static final ModConfigSpec.IntValue ICECLAWS_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("IceClaws charge particle count").defineInRange("iceclawsChargeParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICECLAWS_TRAIL_PARTICLE =
            BUILDER.comment("IceClaws trail particle id").define("iceclawsTrailParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICECLAWS_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("IceClaws trail particle count").defineInRange("iceclawsTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICECRAWL_SOURCE_PARTICLE =
            BUILDER.comment("IceCrawl source particle id").define("icecrawlSourceParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICECRAWL_SOURCE_PARTICLE_COUNT =
            BUILDER.comment("IceCrawl source particle count").defineInRange("icecrawlSourceParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICECRAWL_SETUP_PARTICLE =
            BUILDER.comment("IceCrawl setup particle id").define("icecrawlSetupParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICECRAWL_SETUP_PARTICLE_COUNT =
            BUILDER.comment("IceCrawl setup particle count").defineInRange("icecrawlSetupParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICECRAWL_TRAIL_PARTICLE =
            BUILDER.comment("IceCrawl trail particle id").define("icecrawlTrailParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICECRAWL_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("IceCrawl trail particle count").defineInRange("icecrawlTrailParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICESPIKEBLAST_SETUP_PARTICLE = BUILDER.comment(
                    "IceSpikeBlast setup particle id")
            .define("icespikeblastSetupParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICESPIKEBLAST_SETUP_PARTICLE_COUNT = BUILDER.comment(
                    "IceSpikeBlast setup particle count")
            .defineInRange("icespikeblastSetupParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> ICESPIKEBLAST_SHATTER_PARTICLE = BUILDER.comment(
                    "IceSpikeBlast shatter particle id")
            .define("icespikeblastShatterParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue ICESPIKEBLAST_SHATTER_PARTICLE_COUNT = BUILDER.comment(
                    "IceSpikeBlast shatter particle count")
            .defineInRange("icespikeblastShatterParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISC_GLIDE_PARTICLE =
            BUILDER.comment("LavaDisc glide particle id").define("lavadiscGlideParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVADISC_GLIDE_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc glide particle count").defineInRange("lavadiscGlideParticleCount", 15, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISC_RENDER_PARTICLE =
            BUILDER.comment("LavaDisc render particle id").define("lavadiscRenderParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVADISC_RENDER_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc render particle count").defineInRange("lavadiscRenderParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISC_FLAME_PARTICLE =
            BUILDER.comment("LavaDisc flame particle id").define("lavadiscFlameParticle", "minecraft:flame");
    public static final ModConfigSpec.IntValue LAVADISC_FLAME_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc flame particle count").defineInRange("lavadiscFlameParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISC_SMOKE_PARTICLE =
            BUILDER.comment("LavaDisc smoke particle id").define("lavadiscSmokeParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue LAVADISC_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc smoke particle count").defineInRange("lavadiscSmokeParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISC_MELT_PARTICLE =
            BUILDER.comment("LavaDisc melt particle id").define("lavadiscMeltParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVADISC_MELT_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc melt particle count").defineInRange("lavadiscMeltParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISK_HIT_PARTICLE =
            BUILDER.comment("LavaDisk hit particle id").define("lavadiskHitParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVADISK_HIT_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk hit particle count").defineInRange("lavadiskHitParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISK_MELT_PARTICLE =
            BUILDER.comment("LavaDisk melt particle id").define("lavadiskMeltParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVADISK_MELT_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk melt particle count").defineInRange("lavadiskMeltParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISK_FIZZ_PARTICLE =
            BUILDER.comment("LavaDisk fizz particle id").define("lavadiskFizzParticle", "minecraft:cloud");
    public static final ModConfigSpec.IntValue LAVADISK_FIZZ_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk fizz particle count").defineInRange("lavadiskFizzParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVADISK_SHATTER_PARTICLE =
            BUILDER.comment("LavaDisk shatter particle id").define("lavadiskShatterParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVADISK_SHATTER_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk shatter particle count").defineInRange("lavadiskShatterParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVAFLOW_RIM_PARTICLE =
            BUILDER.comment("LavaFlow rim particle id").define("lavaflowRimParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVAFLOW_RIM_PARTICLE_COUNT =
            BUILDER.comment("LavaFlow rim particle count").defineInRange("lavaflowRimParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVAFLUX_FLOW_PARTICLE =
            BUILDER.comment("LavaFlux flow particle id").define("lavafluxFlowParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVAFLUX_FLOW_PARTICLE_COUNT =
            BUILDER.comment("LavaFlux flow particle count").defineInRange("lavafluxFlowParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVASURGE_SOURCE_PARTICLE =
            BUILDER.comment("LavaSurge source particle id").define("lavasurgeSourceParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVASURGE_SOURCE_PARTICLE_COUNT =
            BUILDER.comment("LavaSurge source particle count").defineInRange("lavasurgeSourceParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVASURGE_TRAIL_PARTICLE =
            BUILDER.comment("LavaSurge trail particle id").define("lavasurgeTrailParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVASURGE_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("LavaSurge trail particle count").defineInRange("lavasurgeTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> LAVASURGE_BURST_PARTICLE =
            BUILDER.comment("LavaSurge burst particle id").define("lavasurgeBurstParticle", "minecraft:lava");
    public static final ModConfigSpec.IntValue LAVASURGE_BURST_PARTICLE_COUNT =
            BUILDER.comment("LavaSurge burst particle count").defineInRange("lavasurgeBurstParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SHOCKWAVE_PUFF_PARTICLE =
            BUILDER.comment("Shockwave puff particle id").define("shockwavePuffParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue SHOCKWAVE_PUFF_PARTICLE_COUNT =
            BUILDER.comment("Shockwave puff particle count").defineInRange("shockwavePuffParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> SHRAPNEL_TRAIL_PARTICLE =
            BUILDER.comment("Shrapnel trail particle id").define("shrapnelTrailParticle", "minecraft:crit");
    public static final ModConfigSpec.IntValue SHRAPNEL_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("Shrapnel trail particle count").defineInRange("shrapnelTrailParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> TORRENT_WAVE_PARTICLE =
            BUILDER.comment("Torrent wave particle id").define("torrentWaveParticle", "minecraft:splash");
    public static final ModConfigSpec.IntValue TORRENT_WAVE_PARTICLE_COUNT =
            BUILDER.comment("Torrent wave particle count").defineInRange("torrentWaveParticleCount", 6, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> TORRENT_FREEZE_PARTICLE =
            BUILDER.comment("Torrent freeze particle id").define("torrentFreezeParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue TORRENT_FREEZE_PARTICLE_COUNT =
            BUILDER.comment("Torrent freeze particle count").defineInRange("torrentFreezeParticleCount", 20, 0, 64);
    public static final ModConfigSpec.IntValue TORRENTBURST_COOLDOWN_TICKS =
            BUILDER.comment("TorrentBurst cooldown in ticks").defineInRange("torrentburstCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.IntValue TORRENT_WAVE_HEIGHT = BUILDER.comment(
                    "Torrent wave wall height above the ring (Reference Height 1)")
            .defineInRange("torrentWaveHeight", 1, 0, 8);
    public static final ModConfigSpec.ConfigValue<String> TORRENTBURST_SPRAY_PARTICLE =
            BUILDER.comment("TorrentBurst spray particle id").define("torrentburstSprayParticle", "minecraft:splash");
    public static final ModConfigSpec.IntValue TORRENTBURST_SPRAY_PARTICLE_COUNT = BUILDER.comment(
                    "TorrentBurst spray particle count")
            .defineInRange("torrentburstSprayParticleCount", 8, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WAKEFISHING_SPLASH_PARTICLE =
            BUILDER.comment("WakeFishing splash particle id").define("wakefishingSplashParticle", "minecraft:splash");
    public static final ModConfigSpec.IntValue WAKEFISHING_SPLASH_PARTICLE_COUNT = BUILDER.comment(
                    "WakeFishing splash particle count")
            .defineInRange("wakefishingSplashParticleCount", 3, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WAKEFISHING_BUBBLE_PARTICLE =
            BUILDER.comment("WakeFishing bubble particle id").define("wakefishingBubbleParticle", "minecraft:bubble");
    public static final ModConfigSpec.IntValue WAKEFISHING_BUBBLE_PARTICLE_COUNT = BUILDER.comment(
                    "WakeFishing bubble particle count")
            .defineInRange("wakefishingBubbleParticleCount", 1, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WAKEFISHING_SMOKE_PARTICLE =
            BUILDER.comment("WakeFishing smoke particle id").define("wakefishingSmokeParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue WAKEFISHING_SMOKE_PARTICLE_COUNT = BUILDER.comment(
                    "WakeFishing smoke particle count")
            .defineInRange("wakefishingSmokeParticleCount", 2, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WATERMANIPULATION_FROST_PARTICLE = BUILDER.comment(
                    "WaterManipulation frost particle id")
            .define("watermanipulationFrostParticle", "minecraft:snowflake");
    public static final ModConfigSpec.IntValue WATERMANIPULATION_FROST_PARTICLE_COUNT = BUILDER.comment(
                    "WaterManipulation frost particle count")
            .defineInRange("watermanipulationFrostParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WATERMANIPULATION_SPRAY_PARTICLE = BUILDER.comment(
                    "WaterManipulation spray particle id")
            .define("watermanipulationSprayParticle", "minecraft:splash");
    public static final ModConfigSpec.IntValue WATERMANIPULATION_SPRAY_PARTICLE_COUNT = BUILDER.comment(
                    "WaterManipulation spray particle count")
            .defineInRange("watermanipulationSprayParticleCount", 4, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WATERSPOUT_SPRAY_PARTICLE =
            BUILDER.comment("WaterSpout spray particle id").define("waterspoutSprayParticle", "minecraft:splash");
    public static final ModConfigSpec.IntValue WATERSPOUT_SPRAY_PARTICLE_COUNT =
            BUILDER.comment("WaterSpout spray particle count").defineInRange("waterspoutSprayParticleCount", 5, 0, 64);
    public static final ModConfigSpec.ConfigValue<String> WATERSPOUTWAVE_TRAIL_PARTICLE = BUILDER.comment(
                    "WaterSpoutWave trail particle id")
            .define("waterspoutwaveTrailParticle", "minecraft:smoke");
    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_TRAIL_PARTICLE_COUNT = BUILDER.comment(
                    "WaterSpoutWave trail particle count")
            .defineInRange("waterspoutwaveTrailParticleCount", 3, 0, 64);
    // W1 (AirSwipe + sphere helpers): AirSwipe keys defined above; SphereAttack/SphereBlast take params, no literals.
    // ================= BATCH: W2 (earth A-M) =================
    public static final ModConfigSpec.IntValue CATAPULT_ANGLE_DEG = BUILDER.comment(
                    "Catapult upward-launch gaze threshold in degrees (reference Angle 45)")
            .defineInRange("catapultAngleDeg", 45, 0, 90);
    public static final ModConfigSpec.DoubleValue CATAPULT_THROW_RADIUS = BUILDER.comment(
                    "Catapult nearby-entity throw radius in blocks")
            .defineInRange("catapultThrowRadius", 2.0, 0.5, 8.0);
    public static final ModConfigSpec.IntValue CATAPULT_EARTH_DISTANCE =
            BUILDER.comment("Catapult moved-earth distance in blocks").defineInRange("catapultEarthDistance", 3, 1, 8);
    public static final ModConfigSpec.IntValue CATAPULT_MAX_STAGE =
            BUILDER.comment("Catapult max charge stages").defineInRange("catapultMaxStage", 4, 1, 8);
    public static final ModConfigSpec.DoubleValue CATAPULT_LAUNCH_BONUS = BUILDER.comment(
                    "Catapult launch distance bonus added to the charge stage")
            .defineInRange("catapultLaunchBonus", 1.5, 0.0, 8.0);
    public static final ModConfigSpec.DoubleValue COLLAPSEWALL_DISTANCE = BUILDER.comment(
                    "CollapseWall forward offset from the caster in blocks")
            .defineInRange("collapsewallDistance", 2.0, 1.0, 8.0);
    public static final ModConfigSpec.DoubleValue DIG_FAIL_PUSH = BUILDER.comment(
                    "Dig push speed in blocks per tick when no earth is targeted")
            .defineInRange("digFailPush", 0.9, 0.0, 4.0);
    public static final ModConfigSpec.IntValue EARTHARMOR_FORM_TICKS =
            BUILDER.comment("EarthArmor shard-forming time in ticks").defineInRange("eartharmorFormTicks", 10, 1, 200);
    public static final ModConfigSpec.DoubleValue EARTHARMOR_MIN_ABSORPTION = BUILDER.comment(
                    "EarthArmor shell breaks below this absorption amount")
            .defineInRange("eartharmorMinAbsorption", 0.9, 0.0, 20.0);
    public static final ModConfigSpec.IntValue EARTHDOME_RINGS =
            BUILDER.comment("EarthDome concentric pillar rings").defineInRange("earthdomeRings", 2, 1, 4);
    public static final ModConfigSpec.DoubleValue EARTHGLOVE_CATCH_RADIUS = BUILDER.comment(
                    "EarthGlove return catch distance in blocks")
            .defineInRange("earthgloveCatchRadius", 1.0, 0.2, 4.0);
    public static final ModConfigSpec.IntValue EARTHGRAB_SLOW_TICKS = BUILDER.comment(
                    "EarthGrab trap slowness duration in ticks")
            .defineInRange("earthgrabSlowTicks", 25, 1, 1200);
    public static final ModConfigSpec.IntValue EARTHGRAB_SLOW_AMPLIFIER =
            BUILDER.comment("EarthGrab trap slowness amplifier").defineInRange("earthgrabSlowAmplifier", 6, 0, 10);
    public static final ModConfigSpec.DoubleValue EARTHGRAB_TRAP_LEASH = BUILDER.comment(
                    "EarthGrab trap breaks beyond this stand distance in blocks")
            .defineInRange("earthgrabTrapLeash", 2.0, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_START_AHEAD = BUILDER.comment(
                    "EarthKick train start distance ahead of the kicker in blocks")
            .defineInRange("earthkickStartAhead", 2.0, 0.0, 8.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_SPACING = BUILDER.comment(
                    "EarthKick spacing between train cubes in blocks")
            .defineInRange("earthkickSpacing", 0.8, 0.2, 4.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_KNOCKBACK =
            BUILDER.comment("EarthKick forward shove strength").defineInRange("earthkickKnockback", 0.6, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_KNOCKUP =
            BUILDER.comment("EarthKick upward pop strength").defineInRange("earthkickKnockup", 0.4, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_TRAVEL_RANGE = BUILDER.comment(
                    "EarthKick train max travel distance in blocks")
            .defineInRange("earthkickTravelRange", 18.0, 4.0, 64.0);
    public static final ModConfigSpec.IntValue EARTHKICK_MAX_TICKS =
            BUILDER.comment("EarthKick train max lifetime in ticks").defineInRange("earthkickMaxTicks", 80, 10, 1200);
    public static final ModConfigSpec.IntValue EARTHPILLAR_AIM_SETTLE_TICKS = BUILDER.comment(
                    "EarthPillar aim settle time before locking the source in ticks")
            .defineInRange("earthpillarAimSettleTicks", 5, 0, 200);
    public static final ModConfigSpec.DoubleValue EARTHPILLAR_TOGGLE_RANGE = BUILDER.comment(
                    "EarthPillar standing-pillar toggle reach in blocks")
            .defineInRange("earthpillarToggleRange", 6.0, 1.0, 24.0);
    public static final ModConfigSpec.IntValue EARTHSHARD_HEADROOM = BUILDER.comment(
                    "EarthShard required open blocks above a source (JedCore 3)")
            .defineInRange("earthshardHeadroom", 3, 1, 6);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_HOVER_HEIGHT = BUILDER.comment(
                    "EarthShard hover height above its hole in blocks")
            .defineInRange("earthshardHoverHeight", 2.0, 0.5, 6.0);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_RISE_SPEED = BUILDER.comment(
                    "EarthShard select pop-up speed in blocks per tick")
            .defineInRange("earthshardRiseSpeed", 0.8, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_KNOCKUP_RADIUS = BUILDER.comment(
                    "EarthShard select pop-up bounce radius in blocks")
            .defineInRange("earthshardKnockupRadius", 1.5, 0.5, 6.0);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_KNOCKUP_POWER = BUILDER.comment(
                    "EarthShard select pop-up bounce strength")
            .defineInRange("earthshardKnockupPower", 1.0, 0.0, 5.0);
    public static final ModConfigSpec.IntValue EARTHSURF_STALL_CHECK_TICKS = BUILDER.comment(
                    "EarthSurf stall cutout delay in ticks (AirScooter reference)")
            .defineInRange("earthsurfStallCheckTicks", 2, 0, 200);
    public static final ModConfigSpec.IntValue EARTHTUNNEL_INTERVAL_TICKS = BUILDER.comment(
                    "EarthTunnel ticks between bore steps (reference Interval 30ms)")
            .defineInRange("earthtunnelIntervalTicks", 1, 1, 40);
    public static final ModConfigSpec.IntValue EARTHTUNNEL_BLOCKS_PER_INTERVAL = BUILDER.comment(
                    "EarthTunnel blocks cleared per step (reference BlocksPerInterval 1)")
            .defineInRange("earthtunnelBlocksPerInterval", 1, 1, 8);
    public static final ModConfigSpec.DoubleValue EARTHTUNNEL_START_RADIUS = BUILDER.comment(
                    "EarthTunnel starting bore radius in blocks (reference Radius 0.25)")
            .defineInRange("earthtunnelStartRadius", 0.25, 0.05, 2.0);
    public static final ModConfigSpec.IntValue EARTHTUNNEL_COOLDOWN_TICKS = BUILDER.comment(
                    "EarthTunnel cooldown in ticks (reference Cooldown 0)")
            .defineInRange("earthtunnelCooldownTicks", 0, 0, 7200);
    public static final ModConfigSpec.IntValue EARTHTUNNEL_MAX_DEVIATION_DEG = BUILDER.comment(
                    "EarthTunnel max gaze deviation before the bore stops in degrees")
            .defineInRange("earthtunnelMaxDeviationDeg", 20, 0, 90);
    public static final ModConfigSpec.IntValue EXTRACTION_GOLEM_DROPS = BUILDER.comment(
                    "Extraction iron nuggets shaken from an iron golem")
            .defineInRange("extractionGolemDrops", 1, 0, 16);
    public static final ModConfigSpec.IntValue FISSURE_SEAL_REVERT_TICKS = BUILDER.comment(
                    "Fissure seal-to-stone revert delay in ticks")
            .defineInRange("fissureSealRevertTicks", 20, 0, 7200);
    public static final ModConfigSpec.DoubleValue FISSURE_AIM_RANGE =
            BUILDER.comment("Fissure seal-aim check range in blocks").defineInRange("fissureAimRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue FISSURE_ORIGIN_OFFSET = BUILDER.comment(
                    "Fissure crack origin distance ahead of the caster in blocks")
            .defineInRange("fissureOriginOffset", 3.0, 1.0, 8.0);
    // ================= BATCH: W3 (earth N-Z + misc) =================
    // W3 follow-up: remaining gameplay numbers for the LavaDisc/MudSurge/ShockwaveRing/etc. pass.
    public static final ModConfigSpec.IntValue LAVADISC_SOURCE_REVERT_TICKS = BUILDER.comment(
                    "LavaDisc source lava regen in ticks (JedCore Source Regen 10000ms)")
            .defineInRange("lavadiscSourceRevertTicks", 200, 10, 2400);
    public static final ModConfigSpec.IntValue LAVADISC_PARTICLES = BUILDER.comment(
                    "LavaDisc particles per render (JedCore Particles 3)")
            .defineInRange("lavadiscParticles", 3, 0, 20);
    public static final ModConfigSpec.IntValue LAVADISC_FIRE_TICKS =
            BUILDER.comment("LavaDisc touch ignite duration in ticks").defineInRange("lavadiscFireTicks", 40, 0, 600);
    public static final ModConfigSpec.DoubleValue LAVADISC_HOLD_DISTANCE = BUILDER.comment(
                    "LavaDisc held disc distance ahead of the eyes in blocks")
            .defineInRange("lavadiscHoldDistance", 3.0, 1.0, 8.0);
    public static final ModConfigSpec.DoubleValue LAVADISC_HIT_RADIUS =
            BUILDER.comment("LavaDisc touch hit radius in blocks").defineInRange("lavadiscHitRadius", 2.0, 0.5, 6.0);
    public static final ModConfigSpec.DoubleValue LAVADISK_SOURCE_RANGE = BUILDER.comment(
                    "LavaDisk source scan range in blocks (Hyperion 5)")
            .defineInRange("lavadiskSourceRange", 5.0, 1.0, 24.0);
    public static final ModConfigSpec.DoubleValue LAVAFLOW_SHIFT_PLATFORM = BUILDER.comment(
                    "LavaFlow shift safe platform radius in blocks")
            .defineInRange("lavaflowShiftPlatform", 1.5, 0.0, 4.0);
    public static final ModConfigSpec.DoubleValue LAVAFLOW_RECT_HALF_WIDTH = BUILDER.comment(
                    "LavaFlow click rectangle half-width in blocks")
            .defineInRange("lavaflowRectHalfWidth", 2.5, 0.5, 8.0);
    public static final ModConfigSpec.IntValue LAVAFLOW_SHIFT_CLEANUP_TICKS = BUILDER.comment(
                    "LavaFlow shift bloom lifetime in ticks")
            .defineInRange("lavaflowShiftCleanupTicks", 200, 20, 7200);
    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAVA_CLEANUP_TICKS = BUILDER.comment(
                    "LavaFlow click-lava pool lifetime in ticks")
            .defineInRange("lavaflowClickLavaCleanupTicks", 140, 20, 7200);
    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAND_CLEANUP_TICKS = BUILDER.comment(
                    "LavaFlow click-land stone lifetime in ticks")
            .defineInRange("lavaflowClickLandCleanupTicks", 400, 20, 7200);
    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAVA_DELAY_TICKS = BUILDER.comment(
                    "LavaFlow click-lava start delay in ticks")
            .defineInRange("lavaflowClickLavaDelayTicks", 0, 0, 1200);
    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAND_DELAY_TICKS = BUILDER.comment(
                    "LavaFlow click-land start delay in ticks")
            .defineInRange("lavaflowClickLandDelayTicks", 10, 0, 1200);
    public static final ModConfigSpec.IntValue LAVAFLUX_CLEANUP_TICKS = BUILDER.comment(
                    "LavaFlux stone seal lifetime in ticks (JedCore Cleanup 1000ms)")
            .defineInRange("lavafluxCleanupTicks", 20, 5, 1200);
    public static final ModConfigSpec.IntValue LAVAFLUX_SPLASH_REVERT_TICKS = BUILDER.comment(
                    "LavaFlux head splash revert in ticks")
            .defineInRange("lavafluxSplashRevertTicks", 6, 1, 200);
    public static final ModConfigSpec.DoubleValue LAVAFLUX_HIT_RADIUS =
            BUILDER.comment("LavaFlux head burn half-box in blocks").defineInRange("lavafluxHitRadius", 1.5, 0.5, 6.0);
    public static final ModConfigSpec.IntValue LAVAFLUX_FIRE_TICKS =
            BUILDER.comment("LavaFlux touch ignite duration in ticks").defineInRange("lavafluxFireTicks", 60, 0, 600);
    public static final ModConfigSpec.DoubleValue LAVAFLUX_KNOCKUP =
            BUILDER.comment("LavaFlux touch upward pop").defineInRange("lavafluxKnockup", 1.0, 0.0, 4.0);
    public static final ModConfigSpec.DoubleValue LAVASURGE_SOURCE_RADIUS = BUILDER.comment(
                    "LavaSurge gather radius in blocks (Addons SourceRadius 3)")
            .defineInRange("lavasurgeSourceRadius", 3.0, 1.0, 8.0);
    public static final ModConfigSpec.IntValue LAVASURGE_FLASH_TICKS = BUILDER.comment(
                    "LavaSurge magma flash before the source goes live in ticks")
            .defineInRange("lavasurgeFlashTicks", 20, 0, 200);
    public static final ModConfigSpec.IntValue LAVASURGE_SHARD_LIFE_TICKS = BUILDER.comment(
                    "LavaSurge shard lifetime in ticks (reference 4s)")
            .defineInRange("lavasurgeShardLifeTicks", 80, 10, 1200);
    public static final ModConfigSpec.IntValue LAVASURGE_CRATER_REVERT_TICKS = BUILDER.comment(
                    "LavaSurge source crater lifetime in ticks (reference 3s)")
            .defineInRange("lavasurgeCraterRevertTicks", 60, 10, 1200);
    public static final ModConfigSpec.IntValue METALCLIPS_SHOT_LIFE_TICKS = BUILDER.comment(
                    "MetalClips thrown ingot lifetime in ticks")
            .defineInRange("metalclipsShotLifeTicks", 200, 20, 2400);
    public static final ModConfigSpec.IntValue METALCLIPS_MAX_CLIPS =
            BUILDER.comment("MetalClips max clips per victim").defineInRange("metalclipsMaxClips", 4, 1, 8);
    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_LEASH_RADIUS = BUILDER.comment(
                    "MetalFragments source leash radius in blocks")
            .defineInRange("metalfragmentsLeashRadius", 10.0, 4.0, 32.0);
    public static final ModConfigSpec.DoubleValue METALHOOK_PULL_SPEED = BUILDER.comment(
                    "MetalHook max haul speed in blocks per tick")
            .defineInRange("metalhookPullSpeed", 0.8, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue METALHOOK_PULL_FACTOR = BUILDER.comment(
                    "MetalHook haul speed factor of anchor distance")
            .defineInRange("metalhookPullFactor", 0.4, 0.05, 2.0);
    public static final ModConfigSpec.DoubleValue MUDSURGE_SOURCE_RADIUS =
            BUILDER.comment("MudSurge gather radius in blocks").defineInRange("mudsurgeSourceRadius", 3.0, 1.0, 8.0);
    public static final ModConfigSpec.IntValue MUDSURGE_BLIND_CHANCE = BUILDER.comment(
                    "MudSurge blinding splash chance percent (reference BlindChance 10)")
            .defineInRange("mudsurgeBlindChance", 10, 0, 100);
    public static final ModConfigSpec.IntValue MUDSURGE_FLASH_TICKS = BUILDER.comment(
                    "MudSurge dust flash before the source goes live in ticks")
            .defineInRange("mudsurgeFlashTicks", 20, 0, 200);
    public static final ModConfigSpec.IntValue MUDSURGE_SHARD_LIFE_TICKS =
            BUILDER.comment("MudSurge shard lifetime in ticks").defineInRange("mudsurgeShardLifeTicks", 80, 10, 1200);
    public static final ModConfigSpec.IntValue MUDSURGE_CRATER_REVERT_TICKS = BUILDER.comment(
                    "MudSurge source crater lifetime in ticks (reference 3s)")
            .defineInRange("mudsurgeCraterRevertTicks", 60, 10, 1200);
    public static final ModConfigSpec.DoubleValue MUDSURGE_KNOCKBACK =
            BUILDER.comment("MudSurge shard knockback strength").defineInRange("mudsurgeKnockback", 0.5, 0.0, 4.0);
    public static final ModConfigSpec.DoubleValue ROCKSLIDE_HIT_RADIUS = BUILDER.comment(
                    "RockSlide contact hit radius in blocks")
            .defineInRange("rockslideHitRadius", 2.0, 0.5, 6.0);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_START_RADIUS = BUILDER.comment(
                    "ShockwaveRing starting radius in blocks")
            .defineInRange("shockwaveRingStartRadius", 3.0, 1.0, 8.0);
    public static final ModConfigSpec.IntValue SHOCKWAVE_RING_BAND =
            BUILDER.comment("ShockwaveRing crest thickness in cells").defineInRange("shockwaveRingBand", 2, 1, 4);
    public static final ModConfigSpec.IntValue SHOCKWAVE_RING_HOP_TICKS = BUILDER.comment(
                    "ShockwaveRing popped block flight time in ticks")
            .defineInRange("shockwaveRingHopTicks", 12, 2, 200);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_UP_POP =
            BUILDER.comment("ShockwaveRing crest up-pop strength").defineInRange("shockwaveRingUpPop", 0.32, 0.0, 2.0);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_OUT_DRIFT =
            BUILDER.comment("ShockwaveRing crest outward drift").defineInRange("shockwaveRingOutDrift", 0.2, 0.0, 2.0);
    public static final ModConfigSpec.DoubleValue SHRAPNEL_BLAST_SPREAD_DEGREES = BUILDER.comment(
                    "Shrapnel blast cone half-angle in degrees (Addons Spread)")
            .defineInRange("shrapnelBlastSpreadDegrees", 6.0, 0.0, 45.0);
    public static final ModConfigSpec.IntValue CATAPULT_COOLDOWN_TICKS = BUILDER.comment(
                    "Catapult cooldown in ticks (Korra 7000ms)")
            .defineInRange("catapultCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.IntValue CATAPULT_HOLD_TICKS = BUILDER.comment(
                    "Catapult launched earth hold time in ticks")
            .defineInRange("catapultHoldTicks", 40, 1, 1200);
    public static final ModConfigSpec.DoubleValue CATAPULT_STAGE_MULT =
            BUILDER.comment("Catapult charge stage time multiplier").defineInRange("catapultStageMult", 2.0, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue COLLAPSEWALL_COOLDOWN_TICKS = BUILDER.comment(
                    "CollapseWall cooldown in ticks (Korra 500ms)")
            .defineInRange("collapsewallCooldownTicks", 10.0, 0.0, 7200.0);
    public static final ModConfigSpec.IntValue COLLAPSEWALL_HEIGHT =
            BUILDER.comment("CollapseWall height in blocks").defineInRange("collapsewallHeight", 6, 1, 12);
    public static final ModConfigSpec.IntValue COLLAPSEWALL_HALF_WIDTH =
            BUILDER.comment("CollapseWall half width in blocks").defineInRange("collapsewallHalfWidth", 3, 1, 8);
    public static final ModConfigSpec.IntValue COLLAPSEWALL_LAYER_INTERVAL_TICKS = BUILDER.comment(
                    "CollapseWall ticks between layers")
            .defineInRange("collapsewallLayerIntervalTicks", 2, 1, 40);
    public static final ModConfigSpec.IntValue DIG_COOLDOWN_TICKS =
            BUILDER.comment("Dig cooldown in ticks (Korra 3000ms)").defineInRange("digCooldownTicks", 60, 0, 7200);
    public static final ModConfigSpec.IntValue DIG_REVERT_TICKS = BUILDER.comment(
                    "Dig temp air revert time in ticks (Korra 3500ms)")
            .defineInRange("digRevertTicks", 70, 1, 7200);
    public static final ModConfigSpec.DoubleValue DIG_SPEED =
            BUILDER.comment("Dig glide speed in blocks per tick").defineInRange("digSpeed", 0.51, 0.05, 4.0);
    public static final ModConfigSpec.DoubleValue DIG_TARGET_RANGE =
            BUILDER.comment("Dig gaze target range in blocks").defineInRange("digTargetRange", 4.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue DIG_CLEAR_RANGE =
            BUILDER.comment("Dig clear radius in blocks").defineInRange("digClearRange", 2.4, 0.5, 8.0);
    public static final ModConfigSpec.IntValue EARTHARMOR_DURATION_TICKS = BUILDER.comment(
                    "EarthArmor max duration in ticks (Korra 17500ms)")
            .defineInRange("eartharmorDurationTicks", 350, 20, 7200);
    public static final ModConfigSpec.IntValue EARTHARMOR_COOLDOWN_TICKS = BUILDER.comment(
                    "EarthArmor cooldown in ticks (Korra 7500ms)")
            .defineInRange("eartharmorCooldownTicks", 150, 0, 7200);
    public static final ModConfigSpec.DoubleValue EARTHARMOR_SELECT_RANGE = BUILDER.comment(
                    "EarthArmor source column range in blocks")
            .defineInRange("eartharmorSelectRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue EARTHARMOR_ABSORPTION =
            BUILDER.comment("EarthArmor absorption hearts").defineInRange("eartharmorAbsorption", 8.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue EARTHBLAST_SELECT_RANGE = BUILDER.comment(
                    "EarthBlast source select range in blocks (Korra 10)")
            .defineInRange("earthblastSelectRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue EARTHBLAST_RANGE = BUILDER.comment(
                    "EarthBlast flight range in blocks (Korra 30)")
            .defineInRange("earthblastRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.DoubleValue EARTHBLAST_SPEED = BUILDER.comment(
                    "EarthBlast speed in blocks per tick (Korra 35 blocks/s)")
            .defineInRange("earthblastSpeed", 1.75, 0.2, 8.0);
    public static final ModConfigSpec.DoubleValue EARTHBLAST_DAMAGE =
            BUILDER.comment("EarthBlast damage (Korra 3)").defineInRange("earthblastDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue EARTHBLAST_PUSH =
            BUILDER.comment("EarthBlast knockback strength (Korra 0.3)").defineInRange("earthblastPush", 0.3, 0.0, 4.0);
    public static final ModConfigSpec.DoubleValue EARTHBLAST_COLLISION_RADIUS = BUILDER.comment(
                    "EarthBlast hit radius in blocks (Korra 1.5)")
            .defineInRange("earthblastCollisionRadius", 1.5, 0.2, 6.0);
    public static final ModConfigSpec.IntValue EARTHBLAST_COOLDOWN_TICKS = BUILDER.comment(
                    "EarthBlast cooldown in ticks (Korra 500ms)")
            .defineInRange("earthblastCooldownTicks", 10, 0, 7200);
    public static final ModConfigSpec.IntValue EARTHDOME_COOLDOWN_TICKS = BUILDER.comment(
                    "EarthDome cooldown in ticks (Korra 10000ms)")
            .defineInRange("earthdomeCooldownTicks", 200, 0, 7200);
    public static final ModConfigSpec.DoubleValue EARTHDOME_RADIUS = BUILDER.comment(
                    "EarthDome ring radius in blocks (Korra 2)")
            .defineInRange("earthdomeRadius", 2.0, 1.0, 8.0);
    public static final ModConfigSpec.IntValue EARTHDOME_HEIGHT =
            BUILDER.comment("EarthDome pillar height in blocks").defineInRange("earthdomeHeight", 7, 1, 16);
    public static final ModConfigSpec.IntValue EARTHDOME_STAND_TICKS =
            BUILDER.comment("EarthDome stand time in ticks").defineInRange("earthdomeStandTicks", 600, 20, 7200);
    public static final ModConfigSpec.DoubleValue EARTHDOME_TARGET_RANGE = BUILDER.comment(
                    "EarthDome other-target range in blocks")
            .defineInRange("earthdomeTargetRange", 14.0, 2.0, 32.0);
    public static final ModConfigSpec.IntValue EARTHGLOVE_COOLDOWN_TICKS = BUILDER.comment(
                    "EarthGlove cooldown in ticks (Hyperion 5000ms)")
            .defineInRange("earthgloveCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.DoubleValue EARTHGLOVE_DAMAGE =
            BUILDER.comment("EarthGlove touch damage").defineInRange("earthgloveDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue EARTHGLOVE_RANGE = BUILDER.comment(
                    "EarthGlove range in blocks (Hyperion 24)")
            .defineInRange("earthgloveRange", 24.0, 4.0, 64.0);
    public static final ModConfigSpec.DoubleValue EARTHGLOVE_FLY_SPEED = BUILDER.comment(
                    "EarthGlove flight speed in blocks per tick")
            .defineInRange("earthgloveFlySpeed", 1.2, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue EARTHGLOVE_CARRY_SPEED = BUILDER.comment(
                    "EarthGlove carry speed in blocks per tick")
            .defineInRange("earthgloveCarrySpeed", 0.6, 0.1, 4.0);
    public static final ModConfigSpec.IntValue EARTHGRAB_COOLDOWN_TICKS = BUILDER.comment(
                    "EarthGrab cooldown in ticks (Korra 5000ms)")
            .defineInRange("earthgrabCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.DoubleValue EARTHGRAB_RANGE =
            BUILDER.comment("EarthGrab range in blocks (Korra 14)").defineInRange("earthgrabRange", 14.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue EARTHGRAB_DRAG_SPEED =
            BUILDER.comment("EarthGrab item drag speed").defineInRange("earthgrabDragSpeed", 0.8, 0.05, 4.0);
    public static final ModConfigSpec.IntValue EARTHGRAB_HIT_INTERVAL_TICKS = BUILDER.comment(
                    "EarthGrab trap hit interval in ticks (Korra 400ms)")
            .defineInRange("earthgrabHitIntervalTicks", 8, 1, 100);
    public static final ModConfigSpec.IntValue EARTHGRAB_TRAP_HP =
            BUILDER.comment("EarthGrab trap HP").defineInRange("earthgrabTrapHp", 3, 1, 20);
    public static final ModConfigSpec.DoubleValue EARTHGRAB_DAMAGE_THRESHOLD = BUILDER.comment(
                    "EarthGrab damage threshold to break free")
            .defineInRange("earthgrabDamageThreshold", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_DAMAGE =
            BUILDER.comment("EarthKick damage").defineInRange("earthkickDamage", 3.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue EARTHKICK_COUNT =
            BUILDER.comment("EarthKick train cubes (Korra MaxBlocks 9)").defineInRange("earthkickCount", 9, 1, 24);
    public static final ModConfigSpec.DoubleValue EARTHKICK_RANGE =
            BUILDER.comment("EarthKick target range in blocks").defineInRange("earthkickRange", 12.0, 2.0, 48.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_SPEED =
            BUILDER.comment("EarthKick glide speed in blocks per tick").defineInRange("earthkickSpeed", 0.9, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue EARTHKICK_HIT_RADIUS =
            BUILDER.comment("EarthKick hit radius (Korra 1.5)").defineInRange("earthkickHitRadius", 1.5, 0.2, 6.0);
    public static final ModConfigSpec.IntValue EARTHPILLAR_HEIGHT =
            BUILDER.comment("EarthPillar max height in blocks").defineInRange("earthpillarHeight", 6, 1, 16);
    public static final ModConfigSpec.DoubleValue EARTHPILLAR_RANGE =
            BUILDER.comment("EarthPillar source range in blocks").defineInRange("earthpillarRange", 20.0, 4.0, 64.0);
    public static final ModConfigSpec.IntValue EARTHSHARD_COOLDOWN_TICKS = BUILDER.comment(
                    "EarthShard cooldown in ticks (JedCore 1000ms)")
            .defineInRange("earthshardCooldownTicks", 20, 0, 7200);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_NORMAL_DAMAGE = BUILDER.comment(
                    "EarthShard normal damage (JedCore 1)")
            .defineInRange("earthshardNormalDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_METAL_DAMAGE = BUILDER.comment(
                    "EarthShard metal damage (JedCore 1.5)")
            .defineInRange("earthshardMetalDamage", 1.5, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_PREPARE_RANGE = BUILDER.comment(
                    "EarthShard source range in blocks (JedCore 5)")
            .defineInRange("earthshardPrepareRange", 5.0, 1.0, 24.0);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_ABILITY_RANGE = BUILDER.comment(
                    "EarthShard throw range in blocks (JedCore 30)")
            .defineInRange("earthshardAbilityRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.IntValue EARTHSHARD_MAX_SHARDS =
            BUILDER.comment("EarthShard max shards (JedCore 3)").defineInRange("earthshardMaxShards", 3, 1, 12);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_HIT_RADIUS =
            BUILDER.comment("EarthShard hit radius (JedCore 1.4)").defineInRange("earthshardHitRadius", 1.4, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue EARTHSHARD_THROW_SPEED = BUILDER.comment(
                    "EarthShard throw speed in blocks per tick")
            .defineInRange("earthshardThrowSpeed", 2.0, 0.5, 6.0);
    public static final ModConfigSpec.DoubleValue EARTHSURF_RIDE_SPEED = BUILDER.comment(
                    "EarthSurf ride speed in blocks per tick")
            .defineInRange("earthsurfRideSpeed", 0.675, 0.1, 4.0);
    public static final ModConfigSpec.IntValue EARTHSURF_COOLDOWN_TICKS =
            BUILDER.comment("EarthSurf cooldown in ticks").defineInRange("earthsurfCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.IntValue EARTHSURF_MAX_HEIGHT =
            BUILDER.comment("EarthSurf floor scan height in blocks").defineInRange("earthsurfMaxHeight", 7, 2, 16);
    public static final ModConfigSpec.DoubleValue EARTHTUNNEL_RANGE = BUILDER.comment(
                    "EarthTunnel range in blocks (Korra 10)")
            .defineInRange("earthtunnelRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue EARTHTUNNEL_MAX_RADIUS = BUILDER.comment(
                    "EarthTunnel max radius in blocks (Korra 1)")
            .defineInRange("earthtunnelMaxRadius", 1.0, 0.25, 4.0);
    public static final ModConfigSpec.IntValue EARTHTUNNEL_REVERT_TICKS = BUILDER.comment(
                    "EarthTunnel revert time in ticks (Korra 300000ms)")
            .defineInRange("earthtunnelRevertTicks", 6000, 20, 12000);
    public static final ModConfigSpec.DoubleValue EXTRACTION_SELECT_RANGE = BUILDER.comment(
                    "Extraction ore sight range in blocks")
            .defineInRange("extractionSelectRange", 5.0, 1.0, 24.0);
    public static final ModConfigSpec.IntValue EXTRACTION_COOLDOWN_TICKS = BUILDER.comment(
                    "Extraction cooldown in ticks (Korra 500ms)")
            .defineInRange("extractionCooldownTicks", 10, 0, 7200);
    public static final ModConfigSpec.DoubleValue EXTRACTION_DOUBLE_CHANCE = BUILDER.comment(
                    "Extraction double-drop chance percent")
            .defineInRange("extractionDoubleChance", 30.0, 0.0, 100.0);
    public static final ModConfigSpec.DoubleValue EXTRACTION_TRIPLE_CHANCE = BUILDER.comment(
                    "Extraction triple-drop chance percent")
            .defineInRange("extractionTripleChance", 10.0, 0.0, 100.0);
    public static final ModConfigSpec.DoubleValue EXTRACTION_GOLEM_DAMAGE =
            BUILDER.comment("Extraction iron-golem damage").defineInRange("extractionGolemDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue FISSURE_COOLDOWN_TICKS = BUILDER.comment(
                    "Fissure cooldown in ticks (JedCore 20000ms)")
            .defineInRange("fissureCooldownTicks", 400, 0, 7200);
    public static final ModConfigSpec.IntValue FISSURE_DURATION_TICKS = BUILDER.comment(
                    "Fissure duration in ticks (JedCore 15000ms)")
            .defineInRange("fissureDurationTicks", 300, 20, 7200);
    public static final ModConfigSpec.IntValue FISSURE_MAX_WIDTH =
            BUILDER.comment("Fissure max width in blocks (JedCore 3)").defineInRange("fissureMaxWidth", 5, 1, 9);
    public static final ModConfigSpec.IntValue FISSURE_SLAP_RANGE = BUILDER.comment(
                    "Fissure length in blocks (JedCore SlapRange 12)")
            .defineInRange("fissureSlapRange", 12, 2, 32);
    public static final ModConfigSpec.IntValue LAVADISC_COOLDOWN_TICKS = BUILDER.comment(
                    "LavaDisc cooldown in ticks (JedCore 7000ms)")
            .defineInRange("lavadiscCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.IntValue LAVADISC_DURATION_TICKS = BUILDER.comment(
                    "LavaDisc flight duration in ticks (JedCore 1000ms)")
            .defineInRange("lavadiscDurationTicks", 20, 5, 1200);
    public static final ModConfigSpec.DoubleValue LAVADISC_DAMAGE =
            BUILDER.comment("LavaDisc touch damage (JedCore 4)").defineInRange("lavadiscDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue LAVADISC_RECALL_LIMIT =
            BUILDER.comment("LavaDisc recall limit (JedCore 3)").defineInRange("lavadiscRecallLimit", 3, 0, 12);
    public static final ModConfigSpec.IntValue LAVADISC_TRAIL_REVERT_TICKS = BUILDER.comment(
                    "LavaDisc trail revert in ticks (JedCore 5000ms)")
            .defineInRange("lavadiscTrailRevertTicks", 100, 10, 2400);
    public static final ModConfigSpec.DoubleValue LAVADISC_SOURCE_RANGE = BUILDER.comment(
                    "LavaDisc source range in blocks (JedCore 4)")
            .defineInRange("lavadiscSourceRange", 4.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue LAVADISC_SPEED = BUILDER.comment(
                    "LavaDisc flight step in blocks per substep")
            .defineInRange("lavadiscSpeed", 0.75, 0.1, 4.0);
    public static final ModConfigSpec.IntValue LAVADISK_COOLDOWN_TICKS = BUILDER.comment(
                    "LavaDisk cooldown in ticks (Hyperion 7000ms)")
            .defineInRange("lavadiskCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.DoubleValue LAVADISK_MAX_DAMAGE =
            BUILDER.comment("LavaDisk max damage (Hyperion 6)").defineInRange("lavadiskMaxDamage", 6.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue LAVADISK_MIN_DAMAGE =
            BUILDER.comment("LavaDisk min damage (Hyperion 1)").defineInRange("lavadiskMinDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue LAVADISK_RANGE =
            BUILDER.comment("LavaDisk range in blocks (Hyperion 24)").defineInRange("lavadiskRange", 24.0, 4.0, 64.0);
    public static final ModConfigSpec.IntValue LAVADISK_REGEN_TICKS = BUILDER.comment(
                    "LavaDisk source regen in ticks (Hyperion 10000ms)")
            .defineInRange("lavadiskRegenTicks", 200, 10, 2400);
    public static final ModConfigSpec.DoubleValue LAVAFLOW_SHIFT_RADIUS = BUILDER.comment(
                    "LavaFlow shift bloom radius in blocks (Korra 7)")
            .defineInRange("lavaflowShiftRadius", 7.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue LAVAFLOW_CLICK_RANGE = BUILDER.comment(
                    "LavaFlow click range in blocks (Korra 10)")
            .defineInRange("lavaflowClickRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue LAVAFLOW_CLICK_RADIUS = BUILDER.comment(
                    "LavaFlow click radius in blocks (Korra 5)")
            .defineInRange("lavaflowClickRadius", 5.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue LAVAFLOW_RECT_LENGTH = BUILDER.comment(
                    "LavaFlow click rectangle length in blocks")
            .defineInRange("lavaflowRectLength", 12.0, 2.0, 32.0);
    public static final ModConfigSpec.IntValue LAVAFLOW_SHIFT_COOLDOWN_TICKS = BUILDER.comment(
                    "LavaFlow shift cooldown in ticks (Korra 20s)")
            .defineInRange("lavaflowShiftCooldownTicks", 400, 0, 7200);
    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAVA_COOLDOWN_TICKS = BUILDER.comment(
                    "LavaFlow click-lava cooldown in ticks (Korra 10s)")
            .defineInRange("lavaflowClickLavaCooldownTicks", 200, 0, 7200);
    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAND_COOLDOWN_TICKS = BUILDER.comment(
                    "LavaFlow click-land cooldown in ticks (Korra 0.5s)")
            .defineInRange("lavaflowClickLandCooldownTicks", 10, 0, 7200);
    public static final ModConfigSpec.IntValue LAVAFLUX_COOLDOWN_TICKS = BUILDER.comment(
                    "LavaFlux cooldown in ticks (JedCore 8000ms)")
            .defineInRange("lavafluxCooldownTicks", 160, 0, 7200);
    public static final ModConfigSpec.IntValue LAVAFLUX_DURATION_TICKS = BUILDER.comment(
                    "LavaFlux hold duration in ticks (JedCore 4000ms)")
            .defineInRange("lavafluxDurationTicks", 80, 10, 2400);
    public static final ModConfigSpec.IntValue LAVAFLUX_RANGE =
            BUILDER.comment("LavaFlux length in blocks (JedCore 12)").defineInRange("lavafluxRange", 12, 2, 32);
    public static final ModConfigSpec.DoubleValue LAVAFLUX_DAMAGE =
            BUILDER.comment("LavaFlux touch damage (JedCore 1)").defineInRange("lavafluxDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue LAVASURGE_COOLDOWN_TICKS = BUILDER.comment(
                    "LavaSurge cooldown in ticks (Addons 4000ms)")
            .defineInRange("lavasurgeCooldownTicks", 80, 0, 7200);
    public static final ModConfigSpec.DoubleValue LAVASURGE_DAMAGE =
            BUILDER.comment("LavaSurge shard damage (Addons 0.5)").defineInRange("lavasurgeDamage", 0.5, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue LAVASURGE_SPEED =
            BUILDER.comment("LavaSurge shard speed in blocks per tick").defineInRange("lavasurgeSpeed", 1.14, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue LAVASURGE_SELECT_RANGE = BUILDER.comment(
                    "LavaSurge source range in blocks (Addons 5)")
            .defineInRange("lavasurgeSelectRange", 5.0, 1.0, 24.0);
    public static final ModConfigSpec.IntValue LAVASURGE_MAX_BLOCKS =
            BUILDER.comment("LavaSurge max blocks (Addons 10)").defineInRange("lavasurgeMaxBlocks", 10, 1, 32);
    public static final ModConfigSpec.IntValue LAVASURGE_BURN_TICKS = BUILDER.comment(
                    "LavaSurge burn time in ticks (Addons 3s)")
            .defineInRange("lavasurgeBurnTicks", 60, 0, 1200);
    public static final ModConfigSpec.DoubleValue LAVASURGE_HIT_RADIUS =
            BUILDER.comment("LavaSurge hit radius in blocks").defineInRange("lavasurgeHitRadius", 0.9, 0.2, 4.0);
    public static final ModConfigSpec.IntValue MAGNETSHIELD_DURATION_TICKS = BUILDER.comment(
                    "MagnetShield duration in ticks (JedCore 6000ms)")
            .defineInRange("magnetshieldDurationTicks", 120, 10, 2400);
    public static final ModConfigSpec.IntValue MAGNETSHIELD_COOLDOWN_TICKS = BUILDER.comment(
                    "MagnetShield cooldown in ticks (JedCore 5000ms)")
            .defineInRange("magnetshieldCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.DoubleValue MAGNETSHIELD_RANGE = BUILDER.comment(
                    "MagnetShield range in blocks (JedCore 5)")
            .defineInRange("magnetshieldRange", 5.0, 1.0, 16.0);
    public static final ModConfigSpec.DoubleValue MAGNETSHIELD_VELOCITY =
            BUILDER.comment("MagnetShield repel velocity").defineInRange("magnetshieldVelocity", 0.1, 0.0, 2.0);
    public static final ModConfigSpec.IntValue METALARMOR_RESIST_TICKS = BUILDER.comment(
                    "MetalArmor resistance duration in ticks (JedCore 4000ms)")
            .defineInRange("metalarmorResistTicks", 80, 10, 2400);
    public static final ModConfigSpec.IntValue METALARMOR_RESIST_AMPLIFIER = BUILDER.comment(
                    "MetalArmor resistance amplifier (JedCore 2)")
            .defineInRange("metalarmorResistAmplifier", 2, 0, 5);
    public static final ModConfigSpec.DoubleValue METALCLIPS_RANGE =
            BUILDER.comment("MetalClips shot range in blocks").defineInRange("metalclipsRange", 10.0, 2.0, 32.0);
    public static final ModConfigSpec.DoubleValue METALCLIPS_SHOOT_SPEED = BUILDER.comment(
                    "MetalClips shot speed in blocks per tick")
            .defineInRange("metalclipsShootSpeed", 3.0, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue METALCLIPS_HIT_DAMAGE =
            BUILDER.comment("MetalClips plain-hit damage").defineInRange("metalclipsHitDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue METALCLIPS_CRUSH_DAMAGE =
            BUILDER.comment("MetalClips crush damage").defineInRange("metalclipsCrushDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.IntValue METALCLIPS_ARMOR_TICKS =
            BUILDER.comment("MetalClips wrap duration in ticks").defineInRange("metalclipsArmorTicks", 200, 20, 2400);
    public static final ModConfigSpec.DoubleValue METALCLIPS_MAGNET_RANGE = BUILDER.comment(
                    "MetalClips sneak magnet range in blocks")
            .defineInRange("metalclipsMagnetRange", 20.0, 4.0, 48.0);
    public static final ModConfigSpec.DoubleValue METALCLIPS_MAGNET_SPEED =
            BUILDER.comment("MetalClips magnet pull speed").defineInRange("metalclipsMagnetSpeed", 0.6, 0.05, 4.0);
    public static final ModConfigSpec.IntValue METALFRAGMENTS_COOLDOWN_TICKS = BUILDER.comment(
                    "MetalFragments cooldown in ticks (JedCore 5000ms)")
            .defineInRange("metalfragmentsCooldownTicks", 100, 0, 7200);
    public static final ModConfigSpec.IntValue METALFRAGMENTS_MAX_SOURCES = BUILDER.comment(
                    "MetalFragments max sources (JedCore 3)")
            .defineInRange("metalfragmentsMaxSources", 3, 1, 12);
    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_SOURCE_RANGE = BUILDER.comment(
                    "MetalFragments source range in blocks (JedCore 5)")
            .defineInRange("metalfragmentsSourceRange", 5.0, 1.0, 24.0);
    public static final ModConfigSpec.IntValue METALFRAGMENTS_MAX_FRAGMENTS = BUILDER.comment(
                    "MetalFragments fragments per source (JedCore 10)")
            .defineInRange("metalfragmentsMaxFragments", 10, 1, 64);
    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_DAMAGE =
            BUILDER.comment("MetalFragments damage (JedCore 4)").defineInRange("metalfragmentsDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_VELOCITY = BUILDER.comment(
                    "MetalFragments velocity in blocks per tick (JedCore 2)")
            .defineInRange("metalfragmentsVelocity", 2.0, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_AIM_RANGE = BUILDER.comment(
                    "MetalFragments aim range in blocks")
            .defineInRange("metalfragmentsAimRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.IntValue METALHOOK_COOLDOWN_TICKS = BUILDER.comment(
                    "MetalHook cooldown in ticks (JedCore 3000ms)")
            .defineInRange("metalhookCooldownTicks", 60, 0, 7200);
    public static final ModConfigSpec.DoubleValue METALHOOK_RANGE =
            BUILDER.comment("MetalHook range in blocks (JedCore 30)").defineInRange("metalhookRange", 30.0, 4.0, 96.0);
    public static final ModConfigSpec.IntValue METALHOOK_MAX_HOOKS =
            BUILDER.comment("MetalHook max hooks (JedCore 3)").defineInRange("metalhookMaxHooks", 3, 1, 12);
    public static final ModConfigSpec.DoubleValue METALHOOK_HOOK_SPEED = BUILDER.comment(
                    "MetalHook flight speed in blocks per tick")
            .defineInRange("metalhookHookSpeed", 3.0, 0.5, 8.0);
    public static final ModConfigSpec.IntValue METALHOOK_SNEAK_RELEASE_TICKS = BUILDER.comment(
                    "MetalHook sneak-hold release time in ticks")
            .defineInRange("metalhookSneakReleaseTicks", 20, 5, 200);
    public static final ModConfigSpec.IntValue MUDSURGE_COOLDOWN_TICKS = BUILDER.comment(
                    "MudSurge cooldown in ticks (Korra 6000ms)")
            .defineInRange("muds surgeCooldownTicks".replace(" ", ""), 120, 0, 7200);
    public static final ModConfigSpec.DoubleValue MUDSURGE_DAMAGE = BUILDER.comment("MudSurge shard damage (Korra 1)")
            .defineInRange("muds surgeDamage".replace(" ", ""), 1.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue MUDSURGE_SPEED = BUILDER.comment(
                    "MudSurge shard speed in blocks per tick")
            .defineInRange("muds surgeSpeed".replace(" ", ""), 1.14, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue MUDSURGE_SELECT_RANGE = BUILDER.comment(
                    "MudSurge source range in blocks")
            .defineInRange("muds surgeSelectRange".replace(" ", ""), 5.0, 1.0, 24.0);
    public static final ModConfigSpec.IntValue MUDSURGE_MAX_BLOCKS =
            BUILDER.comment("MudSurge max blocks").defineInRange("muds surgeMaxBlocks".replace(" ", ""), 10, 1, 32);
    public static final ModConfigSpec.IntValue MUDSURGE_BLIND_TICKS = BUILDER.comment(
                    "MudSurge blindness duration in ticks")
            .defineInRange("muds surgeBlindTicks".replace(" ", ""), 60, 0, 1200);
    public static final ModConfigSpec.DoubleValue MUDSURGE_HIT_RADIUS = BUILDER.comment("MudSurge hit radius in blocks")
            .defineInRange("muds surgeHitRadius".replace(" ", ""), 0.9, 0.2, 4.0);
    public static final ModConfigSpec.IntValue QUICKWELD_COOLDOWN_TICKS = BUILDER.comment(
                    "QuickWeld cooldown in ticks (Addons 1000ms)")
            .defineInRange("quickweldCooldownTicks", 20, 0, 7200);
    public static final ModConfigSpec.IntValue QUICKWELD_REPAIR_AMOUNT = BUILDER.comment(
                    "QuickWeld durability per ingot (Addons 25)")
            .defineInRange("quickweldRepairAmount", 25, 1, 500);
    public static final ModConfigSpec.IntValue QUICKWELD_REPAIR_EVERY_TICKS = BUILDER.comment(
                    "QuickWeld repair interval in ticks (Addons 1250ms)")
            .defineInRange("quickweldRepairEveryTicks", 25, 1, 1200);
    public static final ModConfigSpec.IntValue RAISEEARTH_STAND_TICKS =
            BUILDER.comment("RaiseEarth wall stand time in ticks").defineInRange("raiseearthStandTicks", 600, 20, 7200);
    public static final ModConfigSpec.IntValue ROCKSLIDE_COOLDOWN_TICKS = BUILDER.comment(
                    "RockSlide cooldown in ticks (Addons 7000ms)")
            .defineInRange("rockslideCooldownTicks", 140, 0, 7200);
    public static final ModConfigSpec.DoubleValue ROCKSLIDE_DAMAGE =
            BUILDER.comment("RockSlide contact damage (Addons 1)").defineInRange("rockslideDamage", 1.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue ROCKSLIDE_KNOCKBACK =
            BUILDER.comment("RockSlide knockback (Addons 0.9)").defineInRange("rockslideKnockback", 0.9, 0.0, 5.0);
    public static final ModConfigSpec.DoubleValue ROCKSLIDE_KNOCKUP =
            BUILDER.comment("RockSlide knockup (Addons 0.4)").defineInRange("rockslideKnockup", 0.4, 0.0, 4.0);
    public static final ModConfigSpec.DoubleValue ROCKSLIDE_SPEED = BUILDER.comment(
                    "RockSlide ride speed in blocks per tick (Addons 0.68)")
            .defineInRange("rockslideSpeed", 0.68, 0.1, 4.0);
    public static final ModConfigSpec.DoubleValue ROCKSLIDE_TURNING = BUILDER.comment(
                    "RockSlide steering rate (Addons 0.086)")
            .defineInRange("rockslideTurning", 0.086, 0.01, 1.0);
    public static final ModConfigSpec.IntValue SHOCKWAVE_CHARGE_TICKS = BUILDER.comment(
                    "Shockwave charge time in ticks (Korra 2500ms)")
            .defineInRange("shockwaveChargeTicks", 50, 1, 1200);
    public static final ModConfigSpec.IntValue SHOCKWAVE_COOLDOWN_TICKS = BUILDER.comment(
                    "Shockwave cooldown in ticks (Korra 6000ms)")
            .defineInRange("shockwaveCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_FALL_THRESHOLD = BUILDER.comment(
                    "Shockwave fall-slam threshold in blocks (Korra 12)")
            .defineInRange("shockwaveFallThreshold", 12.0, 2.0, 40.0);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_RANGE = BUILDER.comment(
                    "ShockwaveRing range in blocks (Korra 15)")
            .defineInRange("shockwaveRingRange", 15.0, 4.0, 48.0);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_DAMAGE =
            BUILDER.comment("ShockwaveRing crest damage").defineInRange("shockwaveRingDamage", 4.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_KNOCKBACK =
            BUILDER.comment("ShockwaveRing radial shove").defineInRange("shockwaveRingKnockback", 3.0, 0.0, 8.0);
    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_SPEED =
            BUILDER.comment("ShockwaveRing cells per tick").defineInRange("shockwaveRingSpeed", 1.0, 0.25, 4.0);
    public static final ModConfigSpec.IntValue SHRAPNEL_SHOT_COOLDOWN_TICKS = BUILDER.comment(
                    "Shrapnel shot cooldown in ticks (Addons 2000ms)")
            .defineInRange("shrapnelShotCooldownTicks", 40, 0, 7200);
    public static final ModConfigSpec.IntValue SHRAPNEL_BLAST_COOLDOWN_TICKS = BUILDER.comment(
                    "Shrapnel blast cooldown in ticks (Addons 8000ms)")
            .defineInRange("shrapnelBlastCooldownTicks", 160, 0, 7200);
    public static final ModConfigSpec.DoubleValue SHRAPNEL_DAMAGE =
            BUILDER.comment("Shrapnel max damage (Addons 2)").defineInRange("shrapnelDamage", 2.0, 0.0, 40.0);
    public static final ModConfigSpec.DoubleValue SHRAPNEL_SHOT_SPEED = BUILDER.comment(
                    "Shrapnel shot speed in blocks per tick (Addons 2.3)")
            .defineInRange("shrapnelShotSpeed", 2.3, 0.5, 8.0);
    public static final ModConfigSpec.DoubleValue SHRAPNEL_BLAST_SPEED = BUILDER.comment(
                    "Shrapnel blast speed in blocks per tick (Addons 1.7)")
            .defineInRange("shrapnelBlastSpeed", 1.7, 0.5, 8.0);
    public static final ModConfigSpec.IntValue SHRAPNEL_BLAST_SHOTS =
            BUILDER.comment("Shrapnel blast shells (Addons 9)").defineInRange("shrapnelBlastShots", 9, 1, 32);
    public static final ModConfigSpec.DoubleValue WATERMANIP_SPEED = BUILDER.comment(
                    "WaterManipulation bolt speed in blocks per tick")
            .defineInRange("watermanipSpeed", 1.2, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue WATERMANIP_RADIUS =
            BUILDER.comment("WaterManipulation hit radius in blocks").defineInRange("watermanipRadius", 1.6, 0.2, 6.0);
    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_SELECT_RANGE = BUILDER.comment(
                    "WaterSpoutWave source range in blocks")
            .defineInRange("waterspoutwaveSelectRange", 6.0, 2.0, 16.0);
    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_RADIUS = BUILDER.comment(
                    "WaterSpoutWave charge ring radius in blocks")
            .defineInRange("waterspoutwaveRadius", 3.8, 1.0, 8.0);
    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_WAVE_RADIUS = BUILDER.comment(
                    "WaterSpoutWave trail radius in blocks")
            .defineInRange("waterspoutwaveWaveRadius", 1.5, 0.5, 4.0);
    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_CHARGE_MS =
            BUILDER.comment("WaterSpoutWave charge time in ms").defineInRange("waterspoutwaveChargeMs", 500, 0, 5000);
    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_FLIGHT_MS = BUILDER.comment(
                    "WaterSpoutWave ride duration in ms")
            .defineInRange("waterspoutwaveFlightMs", 2500, 500, 15000);
    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_SPEED = BUILDER.comment(
                    "WaterSpoutWave ride speed in blocks per tick")
            .defineInRange("waterspoutwaveSpeed", 1.3, 0.2, 6.0);
    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_COOLDOWN_TICKS = BUILDER.comment(
                    "WaterSpoutWave shared cooldown in ticks")
            .defineInRange("waterspoutwaveCooldownTicks", 120, 0, 7200);
    public static final ModConfigSpec.DoubleValue ICEBLAST_SLOW_DURATION_TICKS = BUILDER.comment(
                    "IceBlast slowness duration in ticks")
            .defineInRange("iceblastSlowDurationTicks", 70.0, 0.0, 1200.0);
    public static final ModConfigSpec.IntValue ICEBLAST_SLOW_POTENCY =
            BUILDER.comment("IceBlast slowness amplifier").defineInRange("iceblastSlowPotency", 2, 0, 10);
    public static final ModConfigSpec.DoubleValue ICECRAWL_SPEED =
            BUILDER.comment("IceCrawl skim speed in blocks per tick").defineInRange("icecrawlSpeed", 0.7, 0.1, 4.0);

    static final ModConfigSpec SPEC = BUILDER.build();
}
