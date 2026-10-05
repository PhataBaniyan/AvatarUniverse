package com.phatabaniyan.avataruniverse;

import net.neoforged.neoforge.common.ModConfigSpec;

// AvatarUniverse configuration. COMMON type, synced via neoforge.mods.toml + config screen.
//
// Layout mirrors ProjectKorra's config.yml: general switches, then
// abilities grouped by element and ability. All times are in MILLISECONDS
// (Korra convention); game code converts to ticks via msToTicks().
// Renaming a key resets that entry to its default on next launch.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.comment("General mod switches").push("general");
    }

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING = BUILDER.comment(
                    "Whether AvatarUniverse logs extra debug information on server start")
            .define("enableDebugLogging", false);

    public static final ModConfigSpec.BooleanValue ENABLE_BENDING = BUILDER.comment(
                    "Master switch for the ProjectKorra bending port (commands, manager, abilities)")
            .define("enableBending", true);

    public static final ModConfigSpec.BooleanValue MASTERY_ENABLED = BUILDER.comment(
                    "Time mastery: attuned online ticks automatically unlock each base element's sub-elements in order")
            .define("masteryEnabled", true);

    public static final ModConfigSpec.IntValue MASTERY_ATTUNEMENT_MS = BUILDER.comment(
                    "Attunement per sub-element unlock in milliseconds (default 12000000 = ten Minecraft days)")
            .defineInRange("masteryAttunementMs", 12000000, 0, 864000000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Ability settings by element").push("abilities");
    }

    static {
        BUILDER.comment("Water abilities").push("water");
    }

    static {
        BUILDER.comment("Bloodbending settings").push("Bloodbending");
    }

    public static final ModConfigSpec.DoubleValue BLOODBENDING_RANGE = BUILDER.comment(
                    "Bloodbending grip range in blocks (Korra Range 10)")
            .defineInRange("range", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.IntValue BLOODBENDING_DURATION_MS = BUILDER.comment(
                    "Bloodbending max hold in milliseconds, 0 is endless (Korra Duration 0)")
            .defineInRange("durationMs", 0, 0, 360000);

    public static final ModConfigSpec.IntValue BLOODBENDING_COOLDOWN_MS = BUILDER.comment(
                    "Bloodbending cooldown in milliseconds (Korra Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 120000);

    public static final ModConfigSpec.DoubleValue BLOODBENDING_KNOCKBACK = BUILDER.comment(
                    "Bloodbending throw strength (Korra Knockback 2)")
            .defineInRange("knockback", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.BooleanValue BLOODBENDING_AFFECT_UNDEAD = BUILDER.comment(
                    "Bloodbending grips the undead (Korra CanBeUsedOnUndeadMobs, default true)")
            .define("affectUndead", true);

    public static final ModConfigSpec.BooleanValue BLOODBENDING_AFFECT_BLOODBENDERS = BUILDER.comment(
                    "Bloodbending grips other waterbenders (Korra CanBloodbendOtherBloodbenders, default false)")
            .define("affectBloodbenders", false);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("BloodPuppet settings").push("BloodPuppet");
    }

    public static final ModConfigSpec.IntValue BLOODPUPPET_DISTANCE = BUILDER.comment(
                    "BloodPuppet grab range in blocks (JedCore Distance 6)")
            .defineInRange("distance", 6, 2, 24);

    public static final ModConfigSpec.IntValue BLOODPUPPET_HOLD_MS = BUILDER.comment(
                    "BloodPuppet max hold in milliseconds (JedCore HoldTime 10000ms)")
            .defineInRange("holdMs", 10000, 1000, 120000);

    public static final ModConfigSpec.IntValue BLOODPUPPET_COOLDOWN_MS = BUILDER.comment(
                    "BloodPuppet cooldown in milliseconds (JedCore Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 120000);

    public static final ModConfigSpec.BooleanValue BLOODPUPPET_ALLOW_UNDEAD = BUILDER.comment(
                    "BloodPuppet grabs the undead (JedCore UndeadMobs, default true)")
            .define("allowUndead", true);

    public static final ModConfigSpec.BooleanValue BLOODPUPPET_IGNORE_WALLS = BUILDER.comment(
                    "BloodPuppet grabs through walls (JedCore IgnoreWalls, default false)")
            .define("ignoreWalls", false);

    public static final ModConfigSpec.BooleanValue BLOODPUPPET_AFFECT_BLOODBENDERS = BUILDER.comment(
                    "BloodPuppet grabs other waterbenders (JedCore AffectBloodbenders, default false)")
            .define("affectBloodbenders", false);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Drain settings").push("Drain");
    }

    public static final ModConfigSpec.IntValue DRAIN_DURATION_MS = BUILDER.comment(
                    "Drain fill-mode duration in milliseconds (JedCore Duration 2000ms, extended so distant motes arrive)")
            .defineInRange("durationMs", 10000, 500, 120000);

    public static final ModConfigSpec.IntValue DRAIN_COOLDOWN_MS = BUILDER.comment(
                    "Drain cooldown in milliseconds (JedCore Cooldown 2000ms)")
            .defineInRange("cooldownMs", 2000, 0, 120000);

    public static final ModConfigSpec.DoubleValue DRAIN_ABSORB_SPEED = BUILDER.comment(
                    "Drain mote speed in blocks per tick (JedCore AbsorbSpeed 0.1)")
            .defineInRange("absorbSpeed", 0.1, 0.01, 2.0);

    public static final ModConfigSpec.IntValue DRAIN_ABSORB_CHANCE = BUILDER.comment(
                    "Drain one-in-N cell chance (JedCore AbsorbChance 20)")
            .defineInRange("absorbChance", 20, 1, 200);

    public static final ModConfigSpec.IntValue DRAIN_ABSORB_RATE = BUILDER.comment(
                    "Drain motes per fill or charge (JedCore AbsorbRate 6, faster gathering)")
            .defineInRange("absorbRate", 4, 1, 64);

    public static final ModConfigSpec.IntValue DRAIN_RADIUS = BUILDER.comment(
                    "Drain sampling radius in blocks (JedCore Radius 6, widened)")
            .defineInRange("radius", 8, 2, 16);

    public static final ModConfigSpec.IntValue DRAIN_HOLD_RANGE = BUILDER.comment(
                    "Drain blast origin reach in blocks (JedCore HoldRange 2)")
            .defineInRange("holdRange", 2, 1, 8);

    public static final ModConfigSpec.BooleanValue DRAIN_ALLOW_RAIN = BUILDER.comment(
                    "Drain draws motes from open rain (JedCore AllowRainSource, default true)")
            .define("allowRain", true);

    public static final ModConfigSpec.IntValue DRAIN_MAX_BLASTS =
            BUILDER.comment("Drain blasts per gathering (JedCore MaxBlasts 4)").defineInRange("maxBlasts", 4, 1, 16);

    public static final ModConfigSpec.DoubleValue DRAIN_BLAST_RANGE = BUILDER.comment(
                    "Drain blast range in blocks (JedCore BlastRange 20)")
            .defineInRange("blastRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue DRAIN_BLAST_DAMAGE = BUILDER.comment(
                    "Drain blast damage (JedCore BlastDamage 1.5, empowered)")
            .defineInRange("blastDamage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue DRAIN_BLAST_SPEED = BUILDER.comment(
                    "Drain blast speed in blocks per tick (JedCore BlastSpeed 1, quickened)")
            .defineInRange("blastSpeed", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.IntValue DRAIN_REGEN_MS = BUILDER.comment(
                    "Drain plant and water regrow delay in milliseconds (JedCore RegenDelay 15000ms)")
            .defineInRange("regenMs", 15000, 1000, 300000);

    public static final ModConfigSpec.ConfigValue<String> DRAIN_MAIN_PARTICLE =
            BUILDER.comment("Drain mote particle id").define("mainParticle", "minecraft:splash");

    public static final ModConfigSpec.IntValue DRAIN_MAIN_PARTICLE_COUNT =
            BUILDER.comment("Drain mote particle count").defineInRange("mainParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FrostBreath settings").push("FrostBreath");
    }

    public static final ModConfigSpec.IntValue FROSTBREATH_DURATION_MS = BUILDER.comment(
                    "FrostBreath max breath in milliseconds (JedCore Duration 3000ms)")
            .defineInRange("durationMs", 3000, 500, 30000);

    public static final ModConfigSpec.IntValue FROSTBREATH_COOLDOWN_MS = BUILDER.comment(
                    "FrostBreath cooldown in milliseconds (JedCore Cooldown 15000ms)")
            .defineInRange("cooldownMs", 15000, 0, 120000);

    public static final ModConfigSpec.IntValue FROSTBREATH_RANGE = BUILDER.comment(
                    "FrostBreath beam range in blocks (JedCore Range 10)")
            .defineInRange("range", 10, 2, 32);

    public static final ModConfigSpec.IntValue FROSTBREATH_PARTICLES = BUILDER.comment(
                    "FrostBreath snow particles per beam step (JedCore Particles 3)")
            .defineInRange("particles", 3, 0, 20);

    public static final ModConfigSpec.IntValue FROSTBREATH_FROST_DURATION_MS = BUILDER.comment(
                    "FrostBreath entity-cage ice lifetime in milliseconds (JedCore FrostDuration 5000ms)")
            .defineInRange("frostDurationMs", 5000, 0, 60000);

    public static final ModConfigSpec.IntValue FROSTBREATH_FROZEN_WATER_DURATION_MS = BUILDER.comment(
                    "FrostBreath frozen-water ice lifetime in milliseconds (JedCore FrozenWaterDuration 10000ms)")
            .defineInRange("frozenWaterDurationMs", 10000, 0, 120000);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_SNOW_ENABLED = BUILDER.comment(
                    "FrostBreath dusts the ground with snow (JedCore Snow)")
            .define("snowEnabled", true);

    public static final ModConfigSpec.IntValue FROSTBREATH_SNOW_DURATION_MS = BUILDER.comment(
                    "FrostBreath ground snow lifetime in milliseconds (JedCore SnowDuration 5000ms)")
            .defineInRange("snowDurationMs", 5000, 0, 60000);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_DAMAGE_ENABLED = BUILDER.comment(
                    "FrostBreath deals damage (JedCore Damage.Enabled defaults false; enabled here)")
            .define("damageEnabled", true);

    public static final ModConfigSpec.DoubleValue FROSTBREATH_PLAYER_DAMAGE = BUILDER.comment(
                    "FrostBreath damage to players (JedCore Damage.Player 1.0)")
            .defineInRange("playerDamage", 1.0, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue FROSTBREATH_MOB_DAMAGE = BUILDER.comment(
                    "FrostBreath damage to mobs (JedCore Damage.Mob 2.0)")
            .defineInRange("mobDamage", 2.0, 0.0, 20.0);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_SLOW_ENABLED = BUILDER.comment(
                    "FrostBreath slows breathed entities (JedCore Slow.Enabled)")
            .define("slowEnabled", true);

    public static final ModConfigSpec.IntValue FROSTBREATH_SLOW_DURATION_MS = BUILDER.comment(
                    "FrostBreath slowness duration in milliseconds (JedCore Slow.Duration 4000ms)")
            .defineInRange("slowDurationMs", 4000, 0, 60000);

    public static final ModConfigSpec.IntValue FROSTBREATH_SLOW_AMPLIFIER = BUILDER.comment(
                    "FrostBreath slowness amplifier (JedCore hardcodes 5)")
            .defineInRange("slowAmplifier", 5, 0, 10);

    public static final ModConfigSpec.BooleanValue FROSTBREATH_RESTRICT_BIOMES = BUILDER.comment(
                    "FrostBreath fizzles in hot dry biomes (JedCore RestrictBiomes)")
            .define("restrictBiomes", true);

    public static final ModConfigSpec.ConfigValue<String> FROSTBREATH_BEAM_PARTICLE =
            BUILDER.comment("FrostBreath beam particle id").define("beamParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue FROSTBREATH_BEAM_PARTICLE_COUNT =
            BUILDER.comment("FrostBreath beam particle count").defineInRange("beamParticleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("HealingWaters settings").push("HealingWaters");
    }

    public static final ModConfigSpec.DoubleValue HEALING_RANGE = BUILDER.comment(
                    "HealingWaters target range in blocks (Korra Range 5)")
            .defineInRange("range", 5.0, 2.0, 32.0);

    public static final ModConfigSpec.IntValue HEALING_POTENCY = BUILDER.comment(
                    "HealingWaters regeneration amplifier (Korra PotionPotency 1)")
            .defineInRange("potency", 1, 0, 5);

    public static final ModConfigSpec.BooleanValue HEALING_PARTICLES = BUILDER.comment(
                    "HealingWaters charge and ring particles (Korra EnableParticles)")
            .define("particles", true);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("IceBlast settings").push("IceBlast");
    }

    public static final ModConfigSpec.DoubleValue ICEBLAST_DAMAGE =
            BUILDER.comment("IceBlast damage (Korra Damage 3)").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICEBLAST_RANGE =
            BUILDER.comment("IceBlast range in blocks (Korra Range 20)").defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ICEBLAST_COLLISION_RADIUS = BUILDER.comment(
                    "IceBlast hit radius in blocks (Korra CollisionRadius 1.0)")
            .defineInRange("collisionRadius", 1.0, 0.2, 6.0);

    public static final ModConfigSpec.IntValue ICEBLAST_COOLDOWN_MS = BUILDER.comment(
                    "IceBlast cooldown in milliseconds (Korra Cooldown 1500ms)")
            .defineInRange("cooldownMs", 1500, 0, 120000);

    public static final ModConfigSpec.BooleanValue ICEBLAST_ALLOW_SNOW = BUILDER.comment(
                    "IceBlast accepts snow as source (Korra AllowSnow, default false)")
            .define("allowSnow", false);

    public static final ModConfigSpec.ConfigValue<String> ICEBLAST_SETUP_PARTICLE =
            BUILDER.comment("IceBlast setup particle id").define("setupParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICEBLAST_SETUP_PARTICLE_COUNT =
            BUILDER.comment("IceBlast setup particle count").defineInRange("setupParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ICEBLAST_TRAIL_PARTICLE =
            BUILDER.comment("IceBlast trail particle id").define("trailParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICEBLAST_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("IceBlast trail particle count").defineInRange("trailParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ICEBLAST_SHATTER_PARTICLE =
            BUILDER.comment("IceBlast shatter particle id").define("shatterParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICEBLAST_SHATTER_PARTICLE_COUNT =
            BUILDER.comment("IceBlast shatter particle count").defineInRange("shatterParticleCount", 12, 0, 64);

    public static final ModConfigSpec.DoubleValue ICEBLAST_SLOW_DURATION_MS = BUILDER.comment(
                    "IceBlast slowness duration in milliseconds")
            .defineInRange("slowDurationMs", 3500.0, 0.0, 60000.0);

    public static final ModConfigSpec.IntValue ICEBLAST_SLOW_POTENCY =
            BUILDER.comment("IceBlast slowness amplifier").defineInRange("slowPotency", 2, 0, 10);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("IceClaws settings").push("IceClaws");
    }

    public static final ModConfigSpec.IntValue ICECLAWS_CHARGE_MS = BUILDER.comment(
                    "IceClaws charge time in milliseconds (JedCore ChargeTime 1000ms)")
            .defineInRange("chargeMs", 1000, 0, 20000);

    public static final ModConfigSpec.DoubleValue ICECLAWS_RANGE = BUILDER.comment(
                    "IceClaws throw range in blocks (JedCore Range 10)")
            .defineInRange("range", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue ICECLAWS_THROW_SPEED = BUILDER.comment(
                    "IceClaws throw speed in blocks per tick (JedCore Throw.Speed 1.0)")
            .defineInRange("throwSpeed", 1.0, 0.5, 6.0);

    public static final ModConfigSpec.BooleanValue ICECLAWS_THROWABLE = BUILDER.comment(
                    "IceClaws can be thrown (JedCore Throwable, default true)")
            .define("throwable", true);

    public static final ModConfigSpec.BooleanValue ICECLAWS_THROW_COOLDOWN_ON_THROW = BUILDER.comment(
                    "IceClaws cools on throw (JedCore Throw.CooldownOnThrow, default true)")
            .define("throwCooldownOnThrow", true);

    public static final ModConfigSpec.IntValue ICECLAWS_THROW_COOLDOWN_MS = BUILDER.comment(
                    "IceClaws throw cooldown in milliseconds (JedCore Throw.Cooldown 4000ms)")
            .defineInRange("throwCooldownMs", 4000, 0, 120000);

    public static final ModConfigSpec.DoubleValue ICECLAWS_THROW_DAMAGE = BUILDER.comment(
                    "IceClaws throw damage (JedCore Throw.Damage 2.0)")
            .defineInRange("throwDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ICECLAWS_THROW_SLOW_MS = BUILDER.comment(
                    "IceClaws throw slowness in milliseconds (JedCore Throw.SlowDuration 5000ms)")
            .defineInRange("throwSlowMs", 5000, 0, 60000);

    public static final ModConfigSpec.IntValue ICECLAWS_THROW_SLOW_LEVEL = BUILDER.comment(
                    "IceClaws throw slowness amplifier (JedCore Throw.Slowness 3)")
            .defineInRange("throwSlowLevel", 3, 0, 10);

    public static final ModConfigSpec.IntValue ICECLAWS_PUNCH_COOLDOWN_MS = BUILDER.comment(
                    "IceClaws punch cooldown in milliseconds (JedCore Punch.Cooldown 4000ms)")
            .defineInRange("punchCooldownMs", 4000, 0, 120000);

    public static final ModConfigSpec.DoubleValue ICECLAWS_PUNCH_DAMAGE = BUILDER.comment(
                    "IceClaws punch damage (JedCore Punch.Damage 2.0)")
            .defineInRange("punchDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ICECLAWS_PUNCH_SLOW_MS = BUILDER.comment(
                    "IceClaws punch slowness in milliseconds (JedCore Punch.SlowDuration 5000ms)")
            .defineInRange("punchSlowMs", 5000, 0, 60000);

    public static final ModConfigSpec.IntValue ICECLAWS_PUNCH_SLOW_LEVEL = BUILDER.comment(
                    "IceClaws punch slowness amplifier (JedCore Punch.Slowness 3)")
            .defineInRange("punchSlowLevel", 3, 0, 10);

    public static final ModConfigSpec.ConfigValue<String> ICECLAWS_CHARGE_PARTICLE =
            BUILDER.comment("IceClaws charge particle id").define("chargeParticle", "minecraft:splash");

    public static final ModConfigSpec.IntValue ICECLAWS_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("IceClaws charge particle count").defineInRange("chargeParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ICECLAWS_TRAIL_PARTICLE =
            BUILDER.comment("IceClaws trail particle id").define("trailParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICECLAWS_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("IceClaws trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("IceCrawl settings").push("IceCrawl");
    }

    public static final ModConfigSpec.DoubleValue ICECRAWL_DAMAGE =
            BUILDER.comment("IceCrawl damage (Hyperion Damage 5.0)").defineInRange("damage", 5.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ICECRAWL_COOLDOWN_MS = BUILDER.comment(
                    "IceCrawl cooldown in milliseconds (Hyperion Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 120000);

    public static final ModConfigSpec.IntValue ICECRAWL_RANGE = BUILDER.comment(
                    "IceCrawl travel range in blocks (Hyperion Range 24)")
            .defineInRange("range", 24, 4, 64);

    public static final ModConfigSpec.IntValue ICECRAWL_SELECT_RANGE = BUILDER.comment(
                    "IceCrawl source range in blocks (Hyperion SelectRange 8)")
            .defineInRange("selectRange", 8, 2, 24);

    public static final ModConfigSpec.IntValue ICECRAWL_FREEZE_MS = BUILDER.comment(
                    "IceCrawl victim root in milliseconds (Hyperion FreezeDuration 2000ms)")
            .defineInRange("freezeMs", 2000, 500, 60000);

    public static final ModConfigSpec.IntValue ICECRAWL_ICE_MS =
            BUILDER.comment("IceCrawl ice lifetime after freeze ends").defineInRange("iceMs", 8000, 1000, 120000);

    public static final ModConfigSpec.ConfigValue<String> ICECRAWL_SOURCE_PARTICLE =
            BUILDER.comment("IceCrawl source particle id").define("sourceParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICECRAWL_SOURCE_PARTICLE_COUNT =
            BUILDER.comment("IceCrawl source particle count").defineInRange("sourceParticleCount", 8, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ICECRAWL_SETUP_PARTICLE =
            BUILDER.comment("IceCrawl setup particle id").define("setupParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICECRAWL_SETUP_PARTICLE_COUNT =
            BUILDER.comment("IceCrawl setup particle count").defineInRange("setupParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ICECRAWL_TRAIL_PARTICLE =
            BUILDER.comment("IceCrawl trail particle id").define("trailParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICECRAWL_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("IceCrawl trail particle count").defineInRange("trailParticleCount", 2, 0, 64);

    public static final ModConfigSpec.DoubleValue ICECRAWL_SPEED =
            BUILDER.comment("IceCrawl skim speed in blocks per tick").defineInRange("speed", 0.7, 0.1, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("IceSpike settings").push("IceSpike");
    }

    public static final ModConfigSpec.DoubleValue ICESPIKE_BLAST_RANGE = BUILDER.comment(
                    "IceSpike blast range in blocks (Korra Blast.Range 20)")
            .defineInRange("blastRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_BLAST_DAMAGE = BUILDER.comment(
                    "IceSpike blast damage (Korra Blast.Damage 1)")
            .defineInRange("blastDamage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_BLAST_COLLISION_RADIUS = BUILDER.comment(
                    "IceSpike blast hit radius in blocks (Korra Blast.CollisionRadius 1.0)")
            .defineInRange("blastCollisionRadius", 1.0, 0.2, 6.0);

    public static final ModConfigSpec.IntValue ICESPIKE_BLAST_COOLDOWN_MS = BUILDER.comment(
                    "IceSpike blast cooldown in milliseconds (Korra Blast.Cooldown 500ms)")
            .defineInRange("blastCooldownMs", 500, 0, 120000);

    public static final ModConfigSpec.IntValue ICESPIKE_BLAST_SLOW_DURATION_MS = BUILDER.comment(
                    "IceSpike blast slowness in milliseconds (Korra Blast.SlowDuration 70)")
            .defineInRange("blastSlowDurationMs", 3500, 0, 60000);

    public static final ModConfigSpec.IntValue ICESPIKE_BLAST_SLOW_POTENCY = BUILDER.comment(
                    "IceSpike blast slowness amplifier (Korra Blast.SlowPotency 2)")
            .defineInRange("blastSlowPotency", 2, 0, 10);

    public static final ModConfigSpec.DoubleValue ICESPIKE_DAMAGE =
            BUILDER.comment("IceSpike pillar damage (Korra Damage 2)").defineInRange("damage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_PUSH =
            BUILDER.comment("IceSpike pillar launch strength (Korra Push 0.7)").defineInRange("push", 0.7, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_RANGE =
            BUILDER.comment("IceSpike pillar reach in blocks (Korra Range 20)").defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue ICESPIKE_HEIGHT =
            BUILDER.comment("IceSpike pillar height in blocks (Korra Height 6)").defineInRange("height", 6, 1, 16);

    public static final ModConfigSpec.IntValue ICESPIKE_COOLDOWN_MS = BUILDER.comment(
                    "IceSpike pillar cooldown in milliseconds (Korra Cooldown 2000ms)")
            .defineInRange("cooldownMs", 2000, 0, 120000);

    public static final ModConfigSpec.IntValue ICESPIKE_SLOW_DURATION_MS = BUILDER.comment(
                    "IceSpike pillar slowness in milliseconds (Korra SlowDuration 70)")
            .defineInRange("slowDurationMs", 3500, 0, 60000);

    public static final ModConfigSpec.IntValue ICESPIKE_SLOW_POTENCY = BUILDER.comment(
                    "IceSpike pillar slowness amplifier (Korra reads SlowPower, unconfigured upstream = 0)")
            .defineInRange("slowPotency", 0, 0, 10);

    public static final ModConfigSpec.DoubleValue ICESPIKE_FIELD_DAMAGE = BUILDER.comment(
                    "IceSpike field pillar damage (Korra Field.Damage 2)")
            .defineInRange("fieldDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_FIELD_RADIUS = BUILDER.comment(
                    "IceSpike field scan radius in blocks (Korra Field.Radius 6)")
            .defineInRange("fieldRadius", 6.0, 2.0, 16.0);

    public static final ModConfigSpec.DoubleValue ICESPIKE_FIELD_KNOCKUP = BUILDER.comment(
                    "IceSpike field pillar launch strength (Korra Field.Knockup 1)")
            .defineInRange("fieldKnockup", 1.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue ICESPIKE_FIELD_COOLDOWN_MS = BUILDER.comment(
                    "IceSpike field cooldown in milliseconds (Korra Field.Cooldown 2000ms)")
            .defineInRange("fieldCooldownMs", 2000, 0, 120000);

    public static final ModConfigSpec.ConfigValue<String> ICESPIKEBLAST_SETUP_PARTICLE =
            BUILDER.comment("IceSpikeBlast setup particle id").define("blastSetupParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICESPIKEBLAST_SETUP_PARTICLE_COUNT =
            BUILDER.comment("IceSpikeBlast setup particle count").defineInRange("blastSetupParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ICESPIKEBLAST_SHATTER_PARTICLE =
            BUILDER.comment("IceSpikeBlast shatter particle id").define("blastShatterParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue ICESPIKEBLAST_SHATTER_PARTICLE_COUNT = BUILDER.comment(
                    "IceSpikeBlast shatter particle count")
            .defineInRange("blastShatterParticleCount", 8, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("IceWall settings").push("IceWall");
    }

    public static final ModConfigSpec.IntValue ICEWALL_COOLDOWN_MS = BUILDER.comment(
                    "IceWall cooldown in milliseconds (JedCore Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 120000);

    public static final ModConfigSpec.IntValue ICEWALL_WIDTH =
            BUILDER.comment("IceWall width in blocks (JedCore Width 6)").defineInRange("width", 6, 2, 16);

    public static final ModConfigSpec.IntValue ICEWALL_MAX_HEIGHT =
            BUILDER.comment("IceWall arch peak in blocks (JedCore MaxHeight 5)").defineInRange("maxHeight", 5, 1, 12);

    public static final ModConfigSpec.IntValue ICEWALL_MIN_HEIGHT =
            BUILDER.comment("IceWall arch edge in blocks (JedCore MinHeight 3)").defineInRange("minHeight", 3, 1, 12);

    public static final ModConfigSpec.IntValue ICEWALL_RANGE =
            BUILDER.comment("IceWall source range in blocks (JedCore Range 8)").defineInRange("range", 8, 2, 24);

    public static final ModConfigSpec.DoubleValue ICEWALL_DAMAGE =
            BUILDER.comment("IceWall collapse damage (JedCore Damage 4.0)").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICEWALL_DAMAGE_RADIUS = BUILDER.comment(
                    "IceWall collapse hurt radius in blocks (JedCore DamageRadius 2.5)")
            .defineInRange("damageRadius", 2.5, 0.5, 8.0);

    public static final ModConfigSpec.BooleanValue ICEWALL_LIFETIME_ENABLED = BUILDER.comment(
                    "IceWall auto-collapses on a timer (JedCore LifeTime.Enabled, default false)")
            .define("lifetimeEnabled", false);

    public static final ModConfigSpec.IntValue ICEWALL_LIFETIME_MS = BUILDER.comment(
                    "IceWall lifetime in milliseconds (JedCore LifeTime.Duration 10000ms)")
            .defineInRange("lifetimeMs", 10000, 1000, 360000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("iceWave settings").push("iceWave");
    }

    public static final ModConfigSpec.DoubleValue ICEWAVE_DAMAGE = BUILDER.comment(
                    "IceWave contact magic damage once per entity (Korra IceWave Damage 3)")
            .defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ICEWAVE_SPHERE_RADIUS = BUILDER.comment(
                    "IceWave trapping sphere radius in blocks (Korra IceSphereRadius 2.5)")
            .defineInRange("sphereRadius", 2.5, 1.0, 8.0);

    public static final ModConfigSpec.IntValue ICEWAVE_REVERT_MS = BUILDER.comment(
                    "How long IceWave ice persists before thawing (Korra RevertSphereTime 30s)")
            .defineInRange("revertMs", 30000, 1000, 600000);

    public static final ModConfigSpec.DoubleValue ICEWAVE_THAW_RADIUS = BUILDER.comment(
                    "IceWave ice thaws beyond this owner distance (Korra ThawRadius 10)")
            .defineInRange("thawRadius", 10.0, 4.0, 64.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("LeafStorm settings").push("LeafStorm");
    }

    public static final ModConfigSpec.IntValue LEAFSTORM_COOLDOWN_MS =
            BUILDER.comment("LeafStorm cooldown in milliseconds").defineInRange("cooldownMs", 7000, 0, 120000);

    public static final ModConfigSpec.IntValue LEAFSTORM_LEAF_COUNT =
            BUILDER.comment("LeafStorm leaf count").defineInRange("leafCount", 10, 1, 40);

    public static final ModConfigSpec.DoubleValue LEAFSTORM_LEAF_SPEED =
            BUILDER.comment("LeafStorm degrees per tick").defineInRange("leafSpeed", 14.0, 1.0, 45.0);

    public static final ModConfigSpec.DoubleValue LEAFSTORM_DAMAGE =
            BUILDER.comment("LeafStorm magic damage").defineInRange("damage", 0.5, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue LEAFSTORM_RADIUS =
            BUILDER.comment("LeafStorm radius in blocks").defineInRange("radius", 6.0, 1.0, 16.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("PhaseChange settings").push("PhaseChange");
    }

    public static final ModConfigSpec.DoubleValue PHASE_SOURCE_RANGE = BUILDER.comment(
                    "PhaseChange gaze range in blocks (Korra SourceRange 12)")
            .defineInRange("sourceRange", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue PHASE_FREEZE_RADIUS = BUILDER.comment(
                    "PhaseChange freeze radius in blocks (Korra Freeze.Radius 4)")
            .defineInRange("freezeRadius", 4.0, 1.0, 12.0);

    public static final ModConfigSpec.IntValue PHASE_FREEZE_DEPTH = BUILDER.comment(
                    "PhaseChange freeze depth in blocks (Korra Freeze.Depth 1)")
            .defineInRange("freezeDepth", 1, 1, 4);

    public static final ModConfigSpec.DoubleValue PHASE_FREEZE_CONTROL_RADIUS = BUILDER.comment(
                    "PhaseChange ice control radius in blocks (Korra Freeze.ControlRadius 25)")
            .defineInRange("freezeControlRadius", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue PHASE_FREEZE_COOLDOWN_MS = BUILDER.comment(
                    "PhaseChange freeze cooldown in milliseconds (Korra Freeze.Cooldown 250ms)")
            .defineInRange("freezeCooldownMs", 250, 0, 60000);

    public static final ModConfigSpec.DoubleValue PHASE_MELT_RADIUS = BUILDER.comment(
                    "PhaseChange max melt radius in blocks (Korra Melt.Radius 7)")
            .defineInRange("meltRadius", 7.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue PHASE_MELT_SPEED =
            BUILDER.comment("PhaseChange melt speed (Korra Melt.Speed 8)").defineInRange("meltSpeed", 8.0, 1.0, 40.0);

    public static final ModConfigSpec.IntValue PHASE_MELT_COOLDOWN_MS = BUILDER.comment(
                    "PhaseChange melt cooldown in milliseconds (Korra Melt.Cooldown 2000ms)")
            .defineInRange("meltCooldownMs", 2000, 0, 120000);

    public static final ModConfigSpec.BooleanValue PHASE_MELT_ALLOW_FLOW = BUILDER.comment(
                    "PhaseChange melt makes real flowing water (Korra Melt.AllowFlow, default true)")
            .define("meltAllowFlow", true);

    public static final ModConfigSpec.IntValue PHASE_SNOW_MELT_MS = BUILDER.comment(
                    "PhaseChange melted-snow lifetime in milliseconds (Korra 120s snow temps)")
            .defineInRange("snowMeltMs", 120000, 1000, 360000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("PlantArmor settings").push("PlantArmor");
    }

    public static final ModConfigSpec.IntValue PLANTARMOR_COOLDOWN_MS = BUILDER.comment(
                    "PlantArmor cooldown after removal (Korra Cooldown 10000)")
            .defineInRange("cooldownMs", 10000, 0, 120000);

    public static final ModConfigSpec.IntValue PLANTARMOR_DURATION_MS = BUILDER.comment(
                    "PlantArmor duration in milliseconds; 0 = until durability spent (Korra Duration -1)")
            .defineInRange("durationMs", 0, -50, 3600000);

    public static final ModConfigSpec.DoubleValue PLANTARMOR_DURABILITY = BUILDER.comment(
                    "PlantArmor durability pool (Korra 2000)")
            .defineInRange("durability", 2000.0, 100.0, 100000.0);

    public static final ModConfigSpec.IntValue PLANTARMOR_SELECT_RANGE = BUILDER.comment(
                    "PlantArmor plant search range (Korra SelectRange 9)")
            .defineInRange("selectRange", 9, 2, 24);

    public static final ModConfigSpec.IntValue PLANTARMOR_REQUIRED_PLANTS = BUILDER.comment(
                    "Plants to weave for a shell (Korra RequiredPlants 14)")
            .defineInRange("requiredPlants", 14, 1, 64);

    public static final ModConfigSpec.IntValue PLANTARMOR_BOOST_SWIM =
            BUILDER.comment("Dolphin's Grace level (Korra Boost.Swim 3)").defineInRange("boostSwim", 3, 1, 5);

    public static final ModConfigSpec.IntValue PLANTARMOR_BOOST_SPEED =
            BUILDER.comment("Speed Boost level (Korra Boost.Speed 2)").defineInRange("boostSpeed", 2, 1, 5);

    public static final ModConfigSpec.IntValue PLANTARMOR_BOOST_JUMP =
            BUILDER.comment("Jump Boost level (Korra Boost.Jump 2)").defineInRange("boostJump", 2, 1, 5);

    public static final ModConfigSpec.IntValue VINEWHIP_COST =
            BUILDER.comment("VineWhip shell cost").defineInRange("vineWhipCost", 50, 0, 2000);

    public static final ModConfigSpec.IntValue VINEWHIP_COOLDOWN_MS =
            BUILDER.comment("VineWhip cooldown").defineInRange("vineWhipCooldownMs", 2000, 0, 60000);

    public static final ModConfigSpec.DoubleValue VINEWHIP_DAMAGE =
            BUILDER.comment("VineWhip damage").defineInRange("vineWhipDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue VINEWHIP_RANGE =
            BUILDER.comment("VineWhip range").defineInRange("vineWhipRange", 18, 2, 64);

    public static final ModConfigSpec.IntValue VINEWHIP_SPEED =
            BUILDER.comment("VineWhip growth steps/tick").defineInRange("vineWhipSpeed", 3, 1, 16);

    public static final ModConfigSpec.IntValue TANGLE_COST =
            BUILDER.comment("Tangle shell cost").defineInRange("tangleCost", 200, 0, 2000);

    public static final ModConfigSpec.IntValue TANGLE_COOLDOWN_MS =
            BUILDER.comment("Tangle cooldown").defineInRange("tangleCooldownMs", 7000, 0, 60000);

    public static final ModConfigSpec.DoubleValue TANGLE_RADIUS =
            BUILDER.comment("Tangle cone radius").defineInRange("tangleRadius", 0.45, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue TANGLE_RANGE =
            BUILDER.comment("Tangle projectile range").defineInRange("tangleRange", 18.0, 2.0, 64.0);

    public static final ModConfigSpec.IntValue TANGLE_DURATION_MS = BUILDER.comment(
                    "Tangle victim stop-duration (Korra Tangle Duration 3000ms)")
            .defineInRange("tangleDurationMs", 3000, 250, 60000);

    public static final ModConfigSpec.IntValue GRAPPLE_COST =
            BUILDER.comment("Grapple shell cost").defineInRange("grappleCost", 100, 0, 2000);

    public static final ModConfigSpec.IntValue GRAPPLE_COOLDOWN_MS =
            BUILDER.comment("Grapple cooldown").defineInRange("grappleCooldownMs", 2000, 0, 60000);

    public static final ModConfigSpec.IntValue GRAPPLE_RANGE =
            BUILDER.comment("Grapple latch range").defineInRange("grappleRange", 25, 4, 64);

    public static final ModConfigSpec.DoubleValue GRAPPLE_SPEED =
            BUILDER.comment("Grapple reel speed").defineInRange("grappleSpeed", 1.24, 0.2, 4.0);

    public static final ModConfigSpec.IntValue LEAP_COST =
            BUILDER.comment("Leap shell cost").defineInRange("leapCost", 100, 0, 2000);

    public static final ModConfigSpec.IntValue LEAP_COOLDOWN_MS =
            BUILDER.comment("Leap cooldown").defineInRange("leapCooldownMs", 2500, 0, 60000);

    public static final ModConfigSpec.DoubleValue LEAP_POWER =
            BUILDER.comment("Leap launch power").defineInRange("leapPower", 1.4, 0.2, 4.0);

    public static final ModConfigSpec.IntValue LEAFSHIELD_COST =
            BUILDER.comment("LeafShield shell cost").defineInRange("leafShieldCost", 100, 0, 2000);

    public static final ModConfigSpec.IntValue LEAFSHIELD_COOLDOWN_MS =
            BUILDER.comment("LeafShield cooldown").defineInRange("leafShieldCooldownMs", 1500, 0, 60000);

    public static final ModConfigSpec.IntValue LEAFSHIELD_RADIUS =
            BUILDER.comment("LeafShield radius").defineInRange("leafShieldRadius", 2, 1, 6);

    public static final ModConfigSpec.IntValue LEAFDOME_COST =
            BUILDER.comment("LeafDome shell cost").defineInRange("leafDomeCost", 400, 0, 2000);

    public static final ModConfigSpec.IntValue LEAFDOME_COOLDOWN_MS =
            BUILDER.comment("LeafDome cooldown").defineInRange("leafDomeCooldownMs", 5000, 0, 60000);

    public static final ModConfigSpec.IntValue LEAFDOME_RADIUS =
            BUILDER.comment("LeafDome radius").defineInRange("leafDomeRadius", 3, 1, 8);

    public static final ModConfigSpec.IntValue REGENERATE_COOLDOWN_MS =
            BUILDER.comment("Regenerate finish cd").defineInRange("regenerateCooldownMs", 10000, 0, 100000);

    public static final ModConfigSpec.IntValue REGENERATE_AMOUNT =
            BUILDER.comment("Regenerate shell repair").defineInRange("regenerateAmount", 150, 10, 2000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("RazorLeaf settings").push("RazorLeaf");
    }

    public static final ModConfigSpec.IntValue RAZORLEAF_COOLDOWN_MS = BUILDER.comment(
                    "RazorLeaf cooldown in milliseconds (Addons Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 120000);

    public static final ModConfigSpec.DoubleValue RAZORLEAF_DAMAGE =
            BUILDER.comment("RazorLeaf damage (Addons Damage 2)").defineInRange("damage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue RAZORLEAF_RADIUS = BUILDER.comment(
                    "RazorLeaf shred radius in blocks (Addons Radius 0.7)")
            .defineInRange("radius", 0.7, 0.2, 4.0);

    public static final ModConfigSpec.DoubleValue RAZORLEAF_RANGE =
            BUILDER.comment("RazorLeaf range in blocks (Addons Range 24)").defineInRange("range", 24.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue RAZORLEAF_MAX_RECALLS = BUILDER.comment(
                    "RazorLeaf mid-flight re-steers (Addons MaxRecalls 3)")
            .defineInRange("maxRecalls", 3, 0, 12);

    public static final ModConfigSpec.IntValue RAZORLEAF_PARTICLES =
            BUILDER.comment("RazorLeaf spiral density (Addons Particles 300)").defineInRange("particles", 150, 10, 600);

    public static final ModConfigSpec.IntValue RAZORLEAF_SUB_COST =
            BUILDER.comment("RazorLeaf sub shell cost").defineInRange("subCost", 150, 0, 2000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Torrent settings").push("Torrent");
    }

    public static final ModConfigSpec.DoubleValue TORRENT_RANGE = BUILDER.comment(
                    "Torrent wave travel range in blocks (flight time scales with range at 1 block/tick)")
            .defineInRange("range", 40.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue TORRENT_DAMAGE =
            BUILDER.comment("Torrent wave magic damage on first hit").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue TORRENT_SUCCESSIVE_DAMAGE = BUILDER.comment(
                    "Torrent wave magic damage on repeated hits")
            .defineInRange("successiveDamage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue TORRENT_MAX_HITS = BUILDER.comment(
                    "How many times the wave can damage the same entity")
            .defineInRange("maxHits", 2, 1, 10);

    public static final ModConfigSpec.IntValue TORRENT_MAX_LAYER = BUILDER.comment(
                    "How many layers the wave can climb over obstructions (Korra MaxLayer)")
            .defineInRange("maxLayer", 3, 0, 8);

    public static final ModConfigSpec.DoubleValue TORRENT_RADIUS =
            BUILDER.comment("Torrent ring radius in blocks").defineInRange("radius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.DoubleValue TORRENT_KNOCKBACK =
            BUILDER.comment("Torrent knockback strength").defineInRange("knockback", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue TORRENT_KNOCKUP =
            BUILDER.comment("Torrent upward pop on hit").defineInRange("knockup", 0.2, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue TORRENT_DEFLECT_DAMAGE = BUILDER.comment(
                    "Magic damage dealt by the forming ring defense")
            .defineInRange("deflectDamage", 1.0, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue TORRENT_FREEZE_RADIUS = BUILDER.comment(
                    "Radius around the frozen wave that traps entities")
            .defineInRange("freezeRadius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue TORRENT_FROZEN_REVERT_MS = BUILDER.comment(
                    "How long frozen torrent ice persists before thawing (Korra RevertTime 60s)")
            .defineInRange("frozenRevertMs", 60000, 1000, 600000);

    public static final ModConfigSpec.IntValue TORRENT_COOLDOWN_MS = BUILDER.comment(
                    "Torrent cooldown in milliseconds (Korra default: 0)")
            .defineInRange("cooldownMs", 0, 0, 60000);

    public static final ModConfigSpec.IntValue TORRENT_FORMED_TIMEOUT_MS = BUILDER.comment(
                    "How long a completed ring waits for launch before fizzling (0 = hold indefinitely like Korra)")
            .defineInRange("formedTimeoutMs", 0, 0, 60000);

    public static final ModConfigSpec.IntValue TORRENT_SNEAK_WAIT_MS = BUILDER.comment(
                    "How long a readied torrent waits for sneak before fizzling (0 = wait indefinitely like Korra)")
            .defineInRange("sneakWaitMs", 0, 0, 60000);

    public static final ModConfigSpec.DoubleValue TORRENT_BURST_MAX_RADIUS = BUILDER.comment(
                    "Torrent burst maximum radius in blocks (Korra Wave.Radius 12)")
            .defineInRange("burstMaxRadius", 12.0, 2.0, 24.0);

    public static final ModConfigSpec.DoubleValue TORRENT_BURST_GROW = BUILDER.comment(
                    "Torrent burst growth per tick in blocks (Korra Wave.GrowSpeed 0.5)")
            .defineInRange("burstGrow", 0.5, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue TORRENT_BURST_KNOCKBACK = BUILDER.comment(
                    "Torrent burst radial knockback strength (Korra Wave.Knockback 1.5; burst knocks back only, no damage)")
            .defineInRange("burstKnockback", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.ConfigValue<String> TORRENT_WAVE_PARTICLE =
            BUILDER.comment("Torrent wave particle id").define("waveParticle", "minecraft:splash");

    public static final ModConfigSpec.IntValue TORRENT_WAVE_PARTICLE_COUNT =
            BUILDER.comment("Torrent wave particle count").defineInRange("waveParticleCount", 6, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> TORRENT_FREEZE_PARTICLE =
            BUILDER.comment("Torrent freeze particle id").define("freezeParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue TORRENT_FREEZE_PARTICLE_COUNT =
            BUILDER.comment("Torrent freeze particle count").defineInRange("freezeParticleCount", 20, 0, 64);

    public static final ModConfigSpec.IntValue TORRENTBURST_COOLDOWN_MS =
            BUILDER.comment("TorrentBurst cooldown in milliseconds").defineInRange("burstCooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.IntValue TORRENT_WAVE_HEIGHT = BUILDER.comment(
                    "Torrent wave wall height above the ring (Reference Height 1)")
            .defineInRange("waveHeight", 1, 0, 8);

    public static final ModConfigSpec.ConfigValue<String> TORRENTBURST_SPRAY_PARTICLE =
            BUILDER.comment("TorrentBurst spray particle id").define("burstSprayParticle", "minecraft:splash");

    public static final ModConfigSpec.IntValue TORRENTBURST_SPRAY_PARTICLE_COUNT =
            BUILDER.comment("TorrentBurst spray particle count").defineInRange("burstSprayParticleCount", 8, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("WakeFishing settings").push("WakeFishing");
    }

    public static final ModConfigSpec.IntValue WAKEFISHING_COOLDOWN_MS = BUILDER.comment(
                    "WakeFishing cooldown in milliseconds (JedCore Cooldown 10000ms)")
            .defineInRange("cooldownMs", 10000, 0, 120000);

    public static final ModConfigSpec.IntValue WAKEFISHING_DURATION_MS = BUILDER.comment(
                    "WakeFishing max duration in milliseconds (JedCore Duration 20000ms)")
            .defineInRange("durationMs", 20000, 1000, 120000);

    public static final ModConfigSpec.IntValue WAKEFISHING_RANGE = BUILDER.comment(
                    "WakeFishing source range in blocks (JedCore Range 5)")
            .defineInRange("range", 5, 2, 16);

    public static final ModConfigSpec.ConfigValue<String> WAKEFISHING_SPLASH_PARTICLE =
            BUILDER.comment("WakeFishing splash particle id").define("splashParticle", "minecraft:splash");

    public static final ModConfigSpec.IntValue WAKEFISHING_SPLASH_PARTICLE_COUNT =
            BUILDER.comment("WakeFishing splash particle count").defineInRange("splashParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> WAKEFISHING_BUBBLE_PARTICLE =
            BUILDER.comment("WakeFishing bubble particle id").define("bubbleParticle", "minecraft:bubble");

    public static final ModConfigSpec.IntValue WAKEFISHING_BUBBLE_PARTICLE_COUNT =
            BUILDER.comment("WakeFishing bubble particle count").defineInRange("bubbleParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> WAKEFISHING_SMOKE_PARTICLE =
            BUILDER.comment("WakeFishing smoke particle id").define("smokeParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue WAKEFISHING_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("WakeFishing smoke particle count").defineInRange("smokeParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("WaterArms settings").push("WaterArms");
    }

    public static final ModConfigSpec.BooleanValue WATERARMS_ALLOW_PLANT_SOURCE = BUILDER.comment(
                    "WaterArms may start from plant/snow sources, consuming them (Korra AllowPlantSource)")
            .define("allowPlantSource", true);

    public static final ModConfigSpec.IntValue WATERARMS_INITIAL_LENGTH = BUILDER.comment(
                    "WaterArms forward reach past the shoulder joints in blocks (Korra InitialLength 4)")
            .defineInRange("initialLength", 4, 1, 12);

    public static final ModConfigSpec.DoubleValue WATERARMS_SOURCE_GRAB_RANGE = BUILDER.comment(
                    "WaterArms eye-ray source grab range in blocks (Korra SourceGrabRange 12)")
            .defineInRange("sourceGrabRange", 12.0, 4.0, 32.0);

    public static final ModConfigSpec.IntValue WATERARMS_MAX_PUNCHES = BUILDER.comment(
                    "Damaging hits before the arms collapse (Korra MaxAttacks 10)")
            .defineInRange("maxPunches", 10, 0, 200);

    public static final ModConfigSpec.IntValue WATERARMS_MAX_USES = BUILDER.comment(
                    "Total sub-ability uses before the arms collapse (Korra MaxAlternateUsage 50)")
            .defineInRange("maxUses", 50, 0, 500);

    public static final ModConfigSpec.IntValue WATERARMS_COOLDOWN_MS = BUILDER.comment(
                    "WaterArms cooldown after removal in milliseconds (Korra Cooldown 20000ms)")
            .defineInRange("cooldownMs", 20000, 0, 60000);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_MAX_LENGTH = BUILDER.comment(
                    "Whip max length in blocks (Korra Whip.MaxLength 12)")
            .defineInRange("whipMaxLength", 12, 4, 32);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_MAX_LENGTH_WEAK = BUILDER.comment(
                    "Whip max length from weak sources in blocks (Korra MaxLengthWeak 8)")
            .defineInRange("whipMaxLengthWeak", 8, 2, 16);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_PUNCH_MAX_LENGTH = BUILDER.comment(
                    "Punch whip max length in blocks (Korra Punch.MaxLength 8)")
            .defineInRange("whipPunchMaxLength", 8, 2, 16);

    public static final ModConfigSpec.DoubleValue WATERARMS_WHIP_PULL_MULTIPLIER = BUILDER.comment(
                    "Pull strength multiplier (Korra Pull.Multiplier 0.15)")
            .defineInRange("whipPullMultiplier", 0.15, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue WATERARMS_WHIP_PUNCH_DAMAGE = BUILDER.comment(
                    "Punch magic damage (Korra Punch.Damage 0.5)")
            .defineInRange("whipPunchDamage", 0.5, 0.0, 20.0);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_GRAB_DURATION_MS = BUILDER.comment(
                    "Grab hold duration in milliseconds (Korra Grab.Duration 3500ms)")
            .defineInRange("whipGrabDurationMs", 3500, 0, 30000);

    public static final ModConfigSpec.BooleanValue WATERARMS_WHIP_USAGE_COOLDOWN_ENABLED = BUILDER.comment(
                    "Enable per-arm usage cooldowns on whip casts (Korra UsageCooldown.Enabled, default off)")
            .define("whipUsageCooldownEnabled", false);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_PULL_MS = BUILDER.comment(
                    "Pull arm cooldown in milliseconds (Korra 200ms)")
            .defineInRange("whipCooldownPullMs", 200, 0, 5000);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_PUNCH_MS = BUILDER.comment(
                    "Punch arm cooldown in milliseconds (Korra 200ms)")
            .defineInRange("whipCooldownPunchMs", 200, 0, 5000);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_GRAPPLE_MS = BUILDER.comment(
                    "Grapple arm cooldown in milliseconds (Korra 200ms)")
            .defineInRange("whipCooldownGrappleMs", 200, 0, 5000);

    public static final ModConfigSpec.IntValue WATERARMS_WHIP_COOLDOWN_GRAB_MS = BUILDER.comment(
                    "Grab arm cooldown in milliseconds (Korra 200ms)")
            .defineInRange("whipCooldownGrabMs", 200, 0, 5000);

    public static final ModConfigSpec.BooleanValue WATERARMS_GRAPPLE_RESPECT_REGIONS = BUILDER.comment(
                    "Stub for future claim hooks; always unprotected today (Korra Grapple.RespectRegions false)")
            .define("grappleRespectRegions", false);

    public static final ModConfigSpec.DoubleValue WATERARMS_SPEAR_RANGE = BUILDER.comment(
                    "Spear travel range in blocks (Korra Spear.Range 30)")
            .defineInRange("spearRange", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.IntValue WATERARMS_SPEAR_LENGTH = BUILDER.comment(
                    "Spear ice length in blocks on impact (Korra Spear.Length 18)")
            .defineInRange("spearLength", 18, 4, 40);

    public static final ModConfigSpec.DoubleValue WATERARMS_SPEAR_DAMAGE =
            BUILDER.comment("Spear contact damage (Korra Spear.Damage 3)").defineInRange("spearDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.BooleanValue WATERARMS_SPEAR_DAMAGE_ENABLED = BUILDER.comment(
                    "Spear deals contact damage (Korra Spear.DamageEnabled)")
            .define("spearDamageEnabled", true);

    public static final ModConfigSpec.DoubleValue WATERARMS_SPEAR_SPHERE_RADIUS = BUILDER.comment(
                    "Spear victim ice-ball radius, 0 disables (Korra Spear.SphereRadius 2)")
            .defineInRange("spearSphereRadius", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue WATERARMS_SPEAR_DURATION_MS = BUILDER.comment(
                    "Spear ice lifetime in ms plus up to 500ms jitter (Korra Spear.Duration 4500)")
            .defineInRange("spearDurationMs", 4500, 0, 30000);

    public static final ModConfigSpec.IntValue WATERARMS_SPEAR_COOLDOWN_MS = BUILDER.comment(
                    "Spear arm cooldown in milliseconds (Korra 200ms)")
            .defineInRange("spearCooldownMs", 200, 0, 5000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("WaterBubble settings").push("WaterBubble");
    }

    public static final ModConfigSpec.IntValue WATERBUBBLE_CLICK_DURATION_MS = BUILDER.comment(
                    "WaterBubble click-mode lifetime in milliseconds (Korra ClickDuration 2000ms)")
            .defineInRange("clickDurationMs", 2000, 500, 60000);

    public static final ModConfigSpec.DoubleValue WATERBUBBLE_RADIUS = BUILDER.comment(
                    "WaterBubble max radius in blocks (Korra Radius 4.0)")
            .defineInRange("radius", 4.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue WATERBUBBLE_SPEED = BUILDER.comment(
                    "WaterBubble grow/shrink per tick in blocks (Korra Speed 0.5)")
            .defineInRange("speed", 0.5, 0.1, 2.0);

    public static final ModConfigSpec.BooleanValue WATERBUBBLE_MUST_START_ABOVE_WATER = BUILDER.comment(
                    "WaterBubble fizzles when the eyes start inside solid/liquid (Korra MustStartAboveWater, default false)")
            .define("mustStartAboveWater", false);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("WaterManipulation settings").push("WaterManipulation");
    }

    public static final ModConfigSpec.IntValue WATERMANIP_COOLDOWN_MS = BUILDER.comment(
                    "WaterManipulation cooldown in milliseconds (Korra Cooldown 1000ms)")
            .defineInRange("cooldownMs", 1000, 0, 60000);

    public static final ModConfigSpec.DoubleValue WATERMANIP_RANGE =
            BUILDER.comment("WaterManipulation travel range in blocks").defineInRange("range", 18.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue WATERMANIP_DAMAGE =
            BUILDER.comment("WaterManipulation magic damage on hit").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue WATERMANIP_SOURCE_FOCUS_RANGE = BUILDER.comment(
                    "How far (blocks) a selected water source keeps its focus shimmer and stays usable")
            .defineInRange("sourceFocusRange", 10.0, 4.0, 32.0);

    public static final ModConfigSpec.IntValue WATERMANIP_PLANT_REGROW_MS = BUILDER.comment(
                    "Base delay before a plant source consumed by a cast grows back (Korra: 180s, regrows in 50-100% of this)")
            .defineInRange("plantRegrowMs", 180000, 1000, 1200000);

    public static final ModConfigSpec.ConfigValue<String> WATERMANIPULATION_FROST_PARTICLE =
            BUILDER.comment("WaterManipulation frost particle id").define("frostParticle", "minecraft:snowflake");

    public static final ModConfigSpec.IntValue WATERMANIPULATION_FROST_PARTICLE_COUNT =
            BUILDER.comment("WaterManipulation frost particle count").defineInRange("frostParticleCount", 5, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> WATERMANIPULATION_SPRAY_PARTICLE =
            BUILDER.comment("WaterManipulation spray particle id").define("sprayParticle", "minecraft:splash");

    public static final ModConfigSpec.IntValue WATERMANIPULATION_SPRAY_PARTICLE_COUNT =
            BUILDER.comment("WaterManipulation spray particle count").defineInRange("sprayParticleCount", 4, 0, 64);

    public static final ModConfigSpec.DoubleValue WATERMANIP_SPEED =
            BUILDER.comment("WaterManipulation bolt speed in blocks per tick").defineInRange("speed", 1.2, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue WATERMANIP_RADIUS =
            BUILDER.comment("WaterManipulation hit radius in blocks").defineInRange("radius", 1.6, 0.2, 6.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("WaterSpout settings").push("WaterSpout");
    }

    public static final ModConfigSpec.DoubleValue WATERSPOUT_HEIGHT =
            BUILDER.comment("WaterSpout column cap in blocks").defineInRange("height", 16.0, 4.0, 32.0);

    public static final ModConfigSpec.DoubleValue WATERSPOUT_HOP_POWER = BUILDER.comment(
                    "WaterSpout hop launch strength along the look direction (Korra SpoutHop Power 0.85)")
            .defineInRange("hopPower", 0.85, 0.0, 3.0);

    public static final ModConfigSpec.BooleanValue WATERSPOUT_SPIRAL = BUILDER.comment(
                    "WaterSpout rotating overlay ring around the leg streams (Korra BlockSpiral)")
            .define("spiral", true);

    public static final ModConfigSpec.BooleanValue WATERSPOUT_PARTICLES = BUILDER.comment(
                    "WaterSpout splash spray and sounds (Korra Particles)")
            .define("particles", true);

    public static final ModConfigSpec.ConfigValue<String> WATERSPOUT_SPRAY_PARTICLE =
            BUILDER.comment("WaterSpout spray particle id").define("sprayParticle", "minecraft:splash");

    public static final ModConfigSpec.IntValue WATERSPOUT_SPRAY_PARTICLE_COUNT =
            BUILDER.comment("WaterSpout spray particle count").defineInRange("sprayParticleCount", 5, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> WATERSPOUTWAVE_TRAIL_PARTICLE =
            BUILDER.comment("WaterSpoutWave trail particle id").define("waveTrailParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("WaterSpoutWave trail particle count").defineInRange("waveTrailParticleCount", 3, 0, 64);

    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_SELECT_RANGE =
            BUILDER.comment("WaterSpoutWave source range in blocks").defineInRange("waveSelectRange", 6.0, 2.0, 16.0);

    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_RADIUS =
            BUILDER.comment("WaterSpoutWave charge ring radius in blocks").defineInRange("waveRadius", 3.8, 1.0, 8.0);

    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_WAVE_RADIUS =
            BUILDER.comment("WaterSpoutWave trail radius in blocks").defineInRange("waveWaveRadius", 1.5, 0.5, 4.0);

    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_CHARGE_MS =
            BUILDER.comment("WaterSpoutWave charge time in ms").defineInRange("waveChargeMs", 500, 0, 5000);

    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_FLIGHT_MS =
            BUILDER.comment("WaterSpoutWave ride duration in ms").defineInRange("waveFlightMs", 2500, 500, 15000);

    public static final ModConfigSpec.DoubleValue WATERSPOUTWAVE_SPEED =
            BUILDER.comment("WaterSpoutWave ride speed in blocks per tick").defineInRange("waveSpeed", 1.3, 0.2, 6.0);

    public static final ModConfigSpec.IntValue WATERSPOUTWAVE_COOLDOWN_MS = BUILDER.comment(
                    "WaterSpoutWave shared cooldown in milliseconds")
            .defineInRange("waveCooldownMs", 6000, 0, 360000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Earth abilities").push("earth");
    }

    static {
        BUILDER.comment("Accretion settings").push("Accretion");
    }

    public static final ModConfigSpec.IntValue ACCRETION_COOLDOWN_MS =
            BUILDER.comment("Accretion cooldown").defineInRange("cooldownMs", 10000, 0, 120000);

    public static final ModConfigSpec.DoubleValue ACCRETION_DAMAGE =
            BUILDER.comment("Accretion damage per hit").defineInRange("damage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ACCRETION_BLOCKS =
            BUILDER.comment("Max blocks per salvo").defineInRange("blocks", 8, 1, 32);

    public static final ModConfigSpec.IntValue ACCRETION_SELECT_RANGE =
            BUILDER.comment("Accretion source radius").defineInRange("selectRange", 6, 2, 24);

    public static final ModConfigSpec.IntValue ACCRETION_REVERT_TIME_MS =
            BUILDER.comment("Landed TempBlock lifetime ticks").defineInRange("revertTimeMs", 20000, 1000, 60000);

    public static final ModConfigSpec.DoubleValue ACCRETION_THROW_SPEED =
            BUILDER.comment("Accretion launch speed").defineInRange("throwSpeed", 1.6, 0.5, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Catapult settings").push("Catapult");
    }

    public static final ModConfigSpec.IntValue CATAPULT_ANGLE_DEG = BUILDER.comment(
                    "Catapult upward-launch gaze threshold in degrees (reference Angle 45)")
            .defineInRange("angleDeg", 45, 0, 90);

    public static final ModConfigSpec.DoubleValue CATAPULT_THROW_RADIUS = BUILDER.comment(
                    "Catapult nearby-entity throw radius in blocks")
            .defineInRange("throwRadius", 2.0, 0.5, 8.0);

    public static final ModConfigSpec.IntValue CATAPULT_EARTH_DISTANCE =
            BUILDER.comment("Catapult moved-earth distance in blocks").defineInRange("earthDistance", 3, 1, 8);

    public static final ModConfigSpec.IntValue CATAPULT_MAX_STAGE =
            BUILDER.comment("Catapult max charge stages").defineInRange("maxStage", 4, 1, 8);

    public static final ModConfigSpec.DoubleValue CATAPULT_LAUNCH_BONUS = BUILDER.comment(
                    "Catapult launch distance bonus added to the charge stage")
            .defineInRange("launchBonus", 1.5, 0.0, 8.0);

    public static final ModConfigSpec.IntValue CATAPULT_COOLDOWN_MS = BUILDER.comment(
                    "Catapult cooldown in milliseconds (Korra 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.IntValue CATAPULT_HOLD_MS = BUILDER.comment(
                    "Catapult launched earth hold time in milliseconds")
            .defineInRange("holdMs", 2000, 50, 60000);

    public static final ModConfigSpec.DoubleValue CATAPULT_STAGE_MULT =
            BUILDER.comment("Catapult charge stage time multiplier").defineInRange("stageMult", 2.0, 0.5, 8.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("CollapseWall settings").push("CollapseWall");
    }

    public static final ModConfigSpec.DoubleValue COLLAPSEWALL_DISTANCE = BUILDER.comment(
                    "CollapseWall forward offset from the caster in blocks")
            .defineInRange("distance", 2.0, 1.0, 8.0);

    public static final ModConfigSpec.DoubleValue COLLAPSEWALL_COOLDOWN_MS = BUILDER.comment(
                    "CollapseWall cooldown in milliseconds (Korra 500ms)")
            .defineInRange("cooldownMs", 500.0, 0.0, 360000.0);

    public static final ModConfigSpec.IntValue COLLAPSEWALL_HEIGHT =
            BUILDER.comment("CollapseWall height in blocks").defineInRange("height", 6, 1, 12);

    public static final ModConfigSpec.IntValue COLLAPSEWALL_HALF_WIDTH =
            BUILDER.comment("CollapseWall half width in blocks").defineInRange("halfWidth", 3, 1, 8);

    public static final ModConfigSpec.IntValue COLLAPSEWALL_LAYER_INTERVAL_MS =
            BUILDER.comment("CollapseWall ticks between layers").defineInRange("layerIntervalMs", 100, 50, 2000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Dig settings").push("Dig");
    }

    public static final ModConfigSpec.ConfigValue<String> DIG_MAIN_PARTICLE =
            BUILDER.comment("Dig boost particle id").define("mainParticle", "minecraft:crit");

    public static final ModConfigSpec.IntValue DIG_MAIN_PARTICLE_COUNT =
            BUILDER.comment("Dig boost particle count").defineInRange("mainParticleCount", 7, 0, 64);

    public static final ModConfigSpec.DoubleValue DIG_FAIL_PUSH = BUILDER.comment(
                    "Dig push speed in blocks per tick when no earth is targeted")
            .defineInRange("failPush", 0.9, 0.0, 4.0);

    public static final ModConfigSpec.IntValue DIG_COOLDOWN_MS =
            BUILDER.comment("Dig cooldown in milliseconds (Korra 3000ms)").defineInRange("cooldownMs", 3000, 0, 360000);

    public static final ModConfigSpec.IntValue DIG_REVERT_MS = BUILDER.comment(
                    "Dig temp air revert time in milliseconds (Korra 3500ms)")
            .defineInRange("revertMs", 3500, 50, 360000);

    public static final ModConfigSpec.DoubleValue DIG_SPEED =
            BUILDER.comment("Dig glide speed in blocks per tick").defineInRange("speed", 0.51, 0.05, 4.0);

    public static final ModConfigSpec.DoubleValue DIG_TARGET_RANGE =
            BUILDER.comment("Dig gaze target range in blocks").defineInRange("targetRange", 4.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue DIG_CLEAR_RANGE =
            BUILDER.comment("Dig clear radius in blocks").defineInRange("clearRange", 2.4, 0.5, 8.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthArmor settings").push("EarthArmor");
    }

    public static final ModConfigSpec.IntValue EARTHARMOR_FORM_MS =
            BUILDER.comment("EarthArmor shard-forming time in milliseconds").defineInRange("formMs", 500, 50, 10000);

    public static final ModConfigSpec.DoubleValue EARTHARMOR_MIN_ABSORPTION = BUILDER.comment(
                    "EarthArmor shell breaks below this absorption amount")
            .defineInRange("minAbsorption", 0.9, 0.0, 20.0);

    public static final ModConfigSpec.IntValue EARTHARMOR_DURATION_MS = BUILDER.comment(
                    "EarthArmor max duration in milliseconds (Korra 17500ms)")
            .defineInRange("durationMs", 17500, 1000, 360000);

    public static final ModConfigSpec.IntValue EARTHARMOR_COOLDOWN_MS = BUILDER.comment(
                    "EarthArmor cooldown in milliseconds (Korra 7500ms)")
            .defineInRange("cooldownMs", 7500, 0, 360000);

    public static final ModConfigSpec.DoubleValue EARTHARMOR_SELECT_RANGE =
            BUILDER.comment("EarthArmor source column range in blocks").defineInRange("selectRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue EARTHARMOR_ABSORPTION =
            BUILDER.comment("EarthArmor absorption hearts").defineInRange("absorption", 8.0, 0.0, 40.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthBlast settings").push("EarthBlast");
    }

    public static final ModConfigSpec.DoubleValue EARTHBLAST_SELECT_RANGE = BUILDER.comment(
                    "EarthBlast source select range in blocks (Korra 10)")
            .defineInRange("selectRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue EARTHBLAST_RANGE =
            BUILDER.comment("EarthBlast flight range in blocks (Korra 30)").defineInRange("range", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue EARTHBLAST_SPEED = BUILDER.comment(
                    "EarthBlast speed in blocks per tick (Korra 35 blocks/s)")
            .defineInRange("speed", 1.75, 0.2, 8.0);

    public static final ModConfigSpec.DoubleValue EARTHBLAST_DAMAGE =
            BUILDER.comment("EarthBlast damage (Korra 3)").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue EARTHBLAST_PUSH =
            BUILDER.comment("EarthBlast knockback strength (Korra 0.3)").defineInRange("push", 0.3, 0.0, 4.0);

    public static final ModConfigSpec.DoubleValue EARTHBLAST_COLLISION_RADIUS = BUILDER.comment(
                    "EarthBlast hit radius in blocks (Korra 1.5)")
            .defineInRange("collisionRadius", 1.5, 0.2, 6.0);

    public static final ModConfigSpec.IntValue EARTHBLAST_COOLDOWN_MS = BUILDER.comment(
                    "EarthBlast cooldown in milliseconds (Korra 500ms)")
            .defineInRange("cooldownMs", 500, 0, 360000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthDome settings").push("EarthDome");
    }

    public static final ModConfigSpec.IntValue EARTHDOME_RINGS =
            BUILDER.comment("EarthDome concentric pillar rings").defineInRange("rings", 2, 1, 4);

    public static final ModConfigSpec.IntValue EARTHDOME_COOLDOWN_MS = BUILDER.comment(
                    "EarthDome cooldown in milliseconds (Korra 10000ms)")
            .defineInRange("cooldownMs", 10000, 0, 360000);

    public static final ModConfigSpec.DoubleValue EARTHDOME_RADIUS =
            BUILDER.comment("EarthDome ring radius in blocks (Korra 2)").defineInRange("radius", 2.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue EARTHDOME_HEIGHT =
            BUILDER.comment("EarthDome pillar height in blocks").defineInRange("height", 7, 1, 16);

    public static final ModConfigSpec.IntValue EARTHDOME_STAND_MS =
            BUILDER.comment("EarthDome stand time in milliseconds").defineInRange("standMs", 30000, 1000, 360000);

    public static final ModConfigSpec.DoubleValue EARTHDOME_TARGET_RANGE =
            BUILDER.comment("EarthDome other-target range in blocks").defineInRange("targetRange", 14.0, 2.0, 32.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthGlove settings").push("EarthGlove");
    }

    public static final ModConfigSpec.ConfigValue<String> EARTHGLOVE_SHATTER_PARTICLE =
            BUILDER.comment("EarthGlove shatter particle id").define("shatterParticle", "minecraft:poof");

    public static final ModConfigSpec.IntValue EARTHGLOVE_SHATTER_PARTICLE_COUNT =
            BUILDER.comment("EarthGlove shatter particle count").defineInRange("shatterParticleCount", 2, 0, 64);

    public static final ModConfigSpec.DoubleValue EARTHGLOVE_CATCH_RADIUS =
            BUILDER.comment("EarthGlove return catch distance in blocks").defineInRange("catchRadius", 1.0, 0.2, 4.0);

    public static final ModConfigSpec.IntValue EARTHGLOVE_COOLDOWN_MS = BUILDER.comment(
                    "EarthGlove cooldown in milliseconds (Hyperion 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.DoubleValue EARTHGLOVE_DAMAGE =
            BUILDER.comment("EarthGlove touch damage").defineInRange("damage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue EARTHGLOVE_RANGE =
            BUILDER.comment("EarthGlove range in blocks (Hyperion 24)").defineInRange("range", 24.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue EARTHGLOVE_FLY_SPEED =
            BUILDER.comment("EarthGlove flight speed in blocks per tick").defineInRange("flySpeed", 1.2, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue EARTHGLOVE_CARRY_SPEED =
            BUILDER.comment("EarthGlove carry speed in blocks per tick").defineInRange("carrySpeed", 0.6, 0.1, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthGrab settings").push("EarthGrab");
    }

    public static final ModConfigSpec.ConfigValue<String> EARTHGRAB_DRAG_PARTICLE =
            BUILDER.comment("EarthGrab drag particle id").define("dragParticle", "minecraft:poof");

    public static final ModConfigSpec.IntValue EARTHGRAB_DRAG_PARTICLE_COUNT =
            BUILDER.comment("EarthGrab drag particle count").defineInRange("dragParticleCount", 1, 0, 64);

    public static final ModConfigSpec.IntValue EARTHGRAB_SLOW_MS = BUILDER.comment(
                    "EarthGrab trap slowness duration in milliseconds")
            .defineInRange("slowMs", 1250, 50, 60000);

    public static final ModConfigSpec.IntValue EARTHGRAB_SLOW_AMPLIFIER =
            BUILDER.comment("EarthGrab trap slowness amplifier").defineInRange("slowAmplifier", 6, 0, 10);

    public static final ModConfigSpec.DoubleValue EARTHGRAB_TRAP_LEASH = BUILDER.comment(
                    "EarthGrab trap breaks beyond this stand distance in blocks")
            .defineInRange("trapLeash", 2.0, 0.5, 8.0);

    public static final ModConfigSpec.IntValue EARTHGRAB_COOLDOWN_MS = BUILDER.comment(
                    "EarthGrab cooldown in milliseconds (Korra 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.DoubleValue EARTHGRAB_RANGE =
            BUILDER.comment("EarthGrab range in blocks (Korra 14)").defineInRange("range", 14.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue EARTHGRAB_DRAG_SPEED =
            BUILDER.comment("EarthGrab item drag speed").defineInRange("dragSpeed", 0.8, 0.05, 4.0);

    public static final ModConfigSpec.IntValue EARTHGRAB_HIT_INTERVAL_MS = BUILDER.comment(
                    "EarthGrab trap hit interval in milliseconds (Korra 400ms)")
            .defineInRange("hitIntervalMs", 400, 50, 5000);

    public static final ModConfigSpec.IntValue EARTHGRAB_TRAP_HP =
            BUILDER.comment("EarthGrab trap HP").defineInRange("trapHp", 3, 1, 20);

    public static final ModConfigSpec.DoubleValue EARTHGRAB_DAMAGE_THRESHOLD = BUILDER.comment(
                    "EarthGrab damage threshold to break free")
            .defineInRange("damageThreshold", 4.0, 0.0, 40.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthKick settings").push("EarthKick");
    }

    public static final ModConfigSpec.DoubleValue EARTHKICK_START_AHEAD = BUILDER.comment(
                    "EarthKick train start distance ahead of the kicker in blocks")
            .defineInRange("startAhead", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue EARTHKICK_SPACING =
            BUILDER.comment("EarthKick spacing between train cubes in blocks").defineInRange("spacing", 0.8, 0.2, 4.0);

    public static final ModConfigSpec.DoubleValue EARTHKICK_KNOCKBACK =
            BUILDER.comment("EarthKick forward shove strength").defineInRange("knockback", 0.6, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue EARTHKICK_KNOCKUP =
            BUILDER.comment("EarthKick upward pop strength").defineInRange("knockup", 0.4, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue EARTHKICK_TRAVEL_RANGE = BUILDER.comment(
                    "EarthKick train max travel distance in blocks")
            .defineInRange("travelRange", 18.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue EARTHKICK_MAX_MS =
            BUILDER.comment("EarthKick train max lifetime in milliseconds").defineInRange("maxMs", 4000, 500, 60000);

    public static final ModConfigSpec.DoubleValue EARTHKICK_DAMAGE =
            BUILDER.comment("EarthKick damage").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue EARTHKICK_COUNT =
            BUILDER.comment("EarthKick train cubes (Korra MaxBlocks 9)").defineInRange("count", 9, 1, 24);

    public static final ModConfigSpec.DoubleValue EARTHKICK_RANGE =
            BUILDER.comment("EarthKick target range in blocks").defineInRange("range", 12.0, 2.0, 48.0);

    public static final ModConfigSpec.DoubleValue EARTHKICK_SPEED =
            BUILDER.comment("EarthKick glide speed in blocks per tick").defineInRange("speed", 0.9, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue EARTHKICK_HIT_RADIUS =
            BUILDER.comment("EarthKick hit radius (Korra 1.5)").defineInRange("hitRadius", 1.5, 0.2, 6.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthPillar settings").push("EarthPillar");
    }

    public static final ModConfigSpec.IntValue EARTHPILLAR_AIM_SETTLE_MS = BUILDER.comment(
                    "EarthPillar aim settle time before locking the source in milliseconds")
            .defineInRange("aimSettleMs", 250, 0, 10000);

    public static final ModConfigSpec.DoubleValue EARTHPILLAR_TOGGLE_RANGE = BUILDER.comment(
                    "EarthPillar standing-pillar toggle reach in blocks")
            .defineInRange("toggleRange", 6.0, 1.0, 24.0);

    public static final ModConfigSpec.IntValue EARTHPILLAR_HEIGHT =
            BUILDER.comment("EarthPillar max height in blocks").defineInRange("height", 6, 1, 16);

    public static final ModConfigSpec.DoubleValue EARTHPILLAR_RANGE =
            BUILDER.comment("EarthPillar source range in blocks").defineInRange("range", 20.0, 4.0, 64.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthShard settings").push("EarthShard");
    }

    public static final ModConfigSpec.IntValue EARTHSHARD_HEADROOM = BUILDER.comment(
                    "EarthShard required open blocks above a source (JedCore 3)")
            .defineInRange("headroom", 3, 1, 6);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_HOVER_HEIGHT = BUILDER.comment(
                    "EarthShard hover height above its hole in blocks")
            .defineInRange("hoverHeight", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_RISE_SPEED = BUILDER.comment(
                    "EarthShard select pop-up speed in blocks per tick")
            .defineInRange("riseSpeed", 0.8, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_KNOCKUP_RADIUS = BUILDER.comment(
                    "EarthShard select pop-up bounce radius in blocks")
            .defineInRange("knockupRadius", 1.5, 0.5, 6.0);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_KNOCKUP_POWER =
            BUILDER.comment("EarthShard select pop-up bounce strength").defineInRange("knockupPower", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.IntValue EARTHSHARD_COOLDOWN_MS = BUILDER.comment(
                    "EarthShard cooldown in milliseconds (JedCore 1000ms)")
            .defineInRange("cooldownMs", 1000, 0, 360000);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_NORMAL_DAMAGE =
            BUILDER.comment("EarthShard normal damage (JedCore 1)").defineInRange("normalDamage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_METAL_DAMAGE =
            BUILDER.comment("EarthShard metal damage (JedCore 1.5)").defineInRange("metalDamage", 1.5, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_PREPARE_RANGE = BUILDER.comment(
                    "EarthShard source range in blocks (JedCore 5)")
            .defineInRange("prepareRange", 5.0, 1.0, 24.0);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_ABILITY_RANGE = BUILDER.comment(
                    "EarthShard throw range in blocks (JedCore 30)")
            .defineInRange("abilityRange", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.IntValue EARTHSHARD_MAX_SHARDS =
            BUILDER.comment("EarthShard max shards (JedCore 3)").defineInRange("maxShards", 3, 1, 12);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_HIT_RADIUS =
            BUILDER.comment("EarthShard hit radius (JedCore 1.4)").defineInRange("hitRadius", 1.4, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue EARTHSHARD_THROW_SPEED =
            BUILDER.comment("EarthShard throw speed in blocks per tick").defineInRange("throwSpeed", 2.0, 0.5, 6.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthSurf settings").push("EarthSurf");
    }

    public static final ModConfigSpec.IntValue EARTHSURF_STALL_CHECK_MS = BUILDER.comment(
                    "EarthSurf stall cutout delay in milliseconds (AirScooter reference)")
            .defineInRange("stallCheckMs", 100, 0, 10000);

    public static final ModConfigSpec.DoubleValue EARTHSURF_RIDE_SPEED =
            BUILDER.comment("EarthSurf ride speed in blocks per tick").defineInRange("rideSpeed", 0.675, 0.1, 4.0);

    public static final ModConfigSpec.IntValue EARTHSURF_COOLDOWN_MS =
            BUILDER.comment("EarthSurf cooldown in milliseconds").defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.IntValue EARTHSURF_MAX_HEIGHT =
            BUILDER.comment("EarthSurf floor scan height in blocks").defineInRange("maxHeight", 7, 2, 16);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("EarthTunnel settings").push("EarthTunnel");
    }

    public static final ModConfigSpec.IntValue EARTHTUNNEL_INTERVAL_MS = BUILDER.comment(
                    "EarthTunnel ticks between bore steps (reference Interval 30ms)")
            .defineInRange("intervalMs", 50, 50, 2000);

    public static final ModConfigSpec.IntValue EARTHTUNNEL_BLOCKS_PER_INTERVAL = BUILDER.comment(
                    "EarthTunnel blocks cleared per step (reference BlocksPerInterval 1)")
            .defineInRange("blocksPerInterval", 1, 1, 8);

    public static final ModConfigSpec.DoubleValue EARTHTUNNEL_START_RADIUS = BUILDER.comment(
                    "EarthTunnel starting bore radius in blocks (reference Radius 0.25)")
            .defineInRange("startRadius", 0.25, 0.05, 2.0);

    public static final ModConfigSpec.IntValue EARTHTUNNEL_COOLDOWN_MS = BUILDER.comment(
                    "EarthTunnel cooldown in milliseconds (reference Cooldown 0)")
            .defineInRange("cooldownMs", 0, 0, 360000);

    public static final ModConfigSpec.IntValue EARTHTUNNEL_MAX_DEVIATION_DEG = BUILDER.comment(
                    "EarthTunnel max gaze deviation before the bore stops in degrees")
            .defineInRange("maxDeviationDeg", 20, 0, 90);

    public static final ModConfigSpec.DoubleValue EARTHTUNNEL_RANGE =
            BUILDER.comment("EarthTunnel range in blocks (Korra 10)").defineInRange("range", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue EARTHTUNNEL_MAX_RADIUS =
            BUILDER.comment("EarthTunnel max radius in blocks (Korra 1)").defineInRange("maxRadius", 1.0, 0.25, 4.0);

    public static final ModConfigSpec.IntValue EARTHTUNNEL_REVERT_MS = BUILDER.comment(
                    "EarthTunnel revert time in milliseconds (Korra 300000ms)")
            .defineInRange("revertMs", 300000, 1000, 600000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Extraction settings").push("Extraction");
    }

    public static final ModConfigSpec.IntValue EXTRACTION_GOLEM_DROPS =
            BUILDER.comment("Extraction iron nuggets shaken from an iron golem").defineInRange("golemDrops", 1, 0, 16);

    public static final ModConfigSpec.DoubleValue EXTRACTION_SELECT_RANGE =
            BUILDER.comment("Extraction ore sight range in blocks").defineInRange("selectRange", 5.0, 1.0, 24.0);

    public static final ModConfigSpec.IntValue EXTRACTION_COOLDOWN_MS = BUILDER.comment(
                    "Extraction cooldown in milliseconds (Korra 500ms)")
            .defineInRange("cooldownMs", 500, 0, 360000);

    public static final ModConfigSpec.DoubleValue EXTRACTION_DOUBLE_CHANCE =
            BUILDER.comment("Extraction double-drop chance percent").defineInRange("doubleChance", 30.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue EXTRACTION_TRIPLE_CHANCE =
            BUILDER.comment("Extraction triple-drop chance percent").defineInRange("tripleChance", 10.0, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue EXTRACTION_GOLEM_DAMAGE =
            BUILDER.comment("Extraction iron-golem damage").defineInRange("golemDamage", 4.0, 0.0, 40.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Fissure settings").push("Fissure");
    }

    public static final ModConfigSpec.ConfigValue<String> FISSURE_CRACK_PARTICLE =
            BUILDER.comment("Fissure crack particle id").define("crackParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue FISSURE_CRACK_PARTICLE_COUNT =
            BUILDER.comment("Fissure crack particle count").defineInRange("crackParticleCount", 1, 0, 64);

    public static final ModConfigSpec.IntValue FISSURE_SEAL_REVERT_MS = BUILDER.comment(
                    "Fissure seal-to-stone revert delay in milliseconds")
            .defineInRange("sealRevertMs", 1000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FISSURE_AIM_RANGE =
            BUILDER.comment("Fissure seal-aim check range in blocks").defineInRange("aimRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue FISSURE_ORIGIN_OFFSET = BUILDER.comment(
                    "Fissure crack origin distance ahead of the caster in blocks")
            .defineInRange("originOffset", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue FISSURE_COOLDOWN_MS = BUILDER.comment(
                    "Fissure cooldown in milliseconds (JedCore 20000ms)")
            .defineInRange("cooldownMs", 20000, 0, 360000);

    public static final ModConfigSpec.IntValue FISSURE_DURATION_MS = BUILDER.comment(
                    "Fissure duration in milliseconds (JedCore 15000ms)")
            .defineInRange("durationMs", 15000, 1000, 360000);

    public static final ModConfigSpec.IntValue FISSURE_MAX_WIDTH =
            BUILDER.comment("Fissure max width in blocks (JedCore 3)").defineInRange("maxWidth", 5, 1, 9);

    public static final ModConfigSpec.IntValue FISSURE_SLAP_RANGE =
            BUILDER.comment("Fissure length in blocks (JedCore SlapRange 12)").defineInRange("slapRange", 12, 2, 32);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("LavaDisc settings").push("LavaDisc");
    }

    public static final ModConfigSpec.ConfigValue<String> LAVADISC_GLIDE_PARTICLE =
            BUILDER.comment("LavaDisc glide particle id").define("glideParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVADISC_GLIDE_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc glide particle count").defineInRange("glideParticleCount", 15, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVADISC_RENDER_PARTICLE =
            BUILDER.comment("LavaDisc render particle id").define("renderParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVADISC_RENDER_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc render particle count").defineInRange("renderParticleCount", 6, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVADISC_FLAME_PARTICLE =
            BUILDER.comment("LavaDisc flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue LAVADISC_FLAME_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc flame particle count").defineInRange("flameParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVADISC_SMOKE_PARTICLE =
            BUILDER.comment("LavaDisc smoke particle id").define("smokeParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue LAVADISC_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc smoke particle count").defineInRange("smokeParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVADISC_MELT_PARTICLE =
            BUILDER.comment("LavaDisc melt particle id").define("meltParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVADISC_MELT_PARTICLE_COUNT =
            BUILDER.comment("LavaDisc melt particle count").defineInRange("meltParticleCount", 6, 0, 64);

    public static final ModConfigSpec.IntValue LAVADISC_SOURCE_REVERT_MS = BUILDER.comment(
                    "LavaDisc source lava regen in milliseconds (JedCore Source Regen 10000ms)")
            .defineInRange("sourceRevertMs", 10000, 500, 120000);

    public static final ModConfigSpec.IntValue LAVADISC_PARTICLES = BUILDER.comment(
                    "LavaDisc particles per render (JedCore Particles 3)")
            .defineInRange("particles", 3, 0, 20);

    public static final ModConfigSpec.IntValue LAVADISC_FIRE_MS =
            BUILDER.comment("LavaDisc touch ignite duration in milliseconds").defineInRange("fireMs", 2000, 0, 30000);

    public static final ModConfigSpec.DoubleValue LAVADISC_HOLD_DISTANCE = BUILDER.comment(
                    "LavaDisc held disc distance ahead of the eyes in blocks")
            .defineInRange("holdDistance", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.DoubleValue LAVADISC_HIT_RADIUS =
            BUILDER.comment("LavaDisc touch hit radius in blocks").defineInRange("hitRadius", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.IntValue LAVADISC_COOLDOWN_MS = BUILDER.comment(
                    "LavaDisc cooldown in milliseconds (JedCore 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.IntValue LAVADISC_DURATION_MS = BUILDER.comment(
                    "LavaDisc flight duration in milliseconds (JedCore 1000ms)")
            .defineInRange("durationMs", 1000, 250, 60000);

    public static final ModConfigSpec.DoubleValue LAVADISC_DAMAGE =
            BUILDER.comment("LavaDisc touch damage (JedCore 4)").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue LAVADISC_RECALL_LIMIT =
            BUILDER.comment("LavaDisc recall limit (JedCore 3)").defineInRange("recallLimit", 3, 0, 12);

    public static final ModConfigSpec.IntValue LAVADISC_TRAIL_REVERT_MS = BUILDER.comment(
                    "LavaDisc trail revert in milliseconds (JedCore 5000ms)")
            .defineInRange("trailRevertMs", 5000, 500, 120000);

    public static final ModConfigSpec.DoubleValue LAVADISC_SOURCE_RANGE =
            BUILDER.comment("LavaDisc source range in blocks (JedCore 4)").defineInRange("sourceRange", 4.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue LAVADISC_SPEED =
            BUILDER.comment("LavaDisc flight step in blocks per substep").defineInRange("speed", 0.75, 0.1, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("LavaDisk settings").push("LavaDisk");
    }

    public static final ModConfigSpec.ConfigValue<String> LAVADISK_HIT_PARTICLE =
            BUILDER.comment("LavaDisk hit particle id").define("hitParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVADISK_HIT_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk hit particle count").defineInRange("hitParticleCount", 4, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVADISK_MELT_PARTICLE =
            BUILDER.comment("LavaDisk melt particle id").define("meltParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVADISK_MELT_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk melt particle count").defineInRange("meltParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVADISK_FIZZ_PARTICLE =
            BUILDER.comment("LavaDisk fizz particle id").define("fizzParticle", "minecraft:cloud");

    public static final ModConfigSpec.IntValue LAVADISK_FIZZ_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk fizz particle count").defineInRange("fizzParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVADISK_SHATTER_PARTICLE =
            BUILDER.comment("LavaDisk shatter particle id").define("shatterParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVADISK_SHATTER_PARTICLE_COUNT =
            BUILDER.comment("LavaDisk shatter particle count").defineInRange("shatterParticleCount", 2, 0, 64);

    public static final ModConfigSpec.DoubleValue LAVADISK_SOURCE_RANGE = BUILDER.comment(
                    "LavaDisk source scan range in blocks (Hyperion 5)")
            .defineInRange("sourceRange", 5.0, 1.0, 24.0);

    public static final ModConfigSpec.IntValue LAVADISK_COOLDOWN_MS = BUILDER.comment(
                    "LavaDisk cooldown in milliseconds (Hyperion 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.DoubleValue LAVADISK_MAX_DAMAGE =
            BUILDER.comment("LavaDisk max damage (Hyperion 6)").defineInRange("maxDamage", 6.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue LAVADISK_MIN_DAMAGE =
            BUILDER.comment("LavaDisk min damage (Hyperion 1)").defineInRange("minDamage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue LAVADISK_RANGE =
            BUILDER.comment("LavaDisk range in blocks (Hyperion 24)").defineInRange("range", 24.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue LAVADISK_REGEN_MS = BUILDER.comment(
                    "LavaDisk source regen in milliseconds (Hyperion 10000ms)")
            .defineInRange("regenMs", 10000, 500, 120000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("LavaFlow settings").push("LavaFlow");
    }

    public static final ModConfigSpec.ConfigValue<String> LAVAFLOW_RIM_PARTICLE =
            BUILDER.comment("LavaFlow rim particle id").define("rimParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVAFLOW_RIM_PARTICLE_COUNT =
            BUILDER.comment("LavaFlow rim particle count").defineInRange("rimParticleCount", 1, 0, 64);

    public static final ModConfigSpec.DoubleValue LAVAFLOW_SHIFT_PLATFORM = BUILDER.comment(
                    "LavaFlow shift safe platform radius in blocks")
            .defineInRange("shiftPlatform", 1.5, 0.0, 4.0);

    public static final ModConfigSpec.DoubleValue LAVAFLOW_RECT_HALF_WIDTH = BUILDER.comment(
                    "LavaFlow click rectangle half-width in blocks")
            .defineInRange("rectHalfWidth", 2.5, 0.5, 8.0);

    public static final ModConfigSpec.IntValue LAVAFLOW_SHIFT_CLEANUP_MS = BUILDER.comment(
                    "LavaFlow shift bloom lifetime in milliseconds")
            .defineInRange("shiftCleanupMs", 10000, 1000, 360000);

    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAVA_CLEANUP_MS = BUILDER.comment(
                    "LavaFlow click-lava pool lifetime in milliseconds")
            .defineInRange("clickLavaCleanupMs", 7000, 1000, 360000);

    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAND_CLEANUP_MS = BUILDER.comment(
                    "LavaFlow click-land stone lifetime in milliseconds")
            .defineInRange("clickLandCleanupMs", 20000, 1000, 360000);

    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAVA_DELAY_MS = BUILDER.comment(
                    "LavaFlow click-lava start delay in milliseconds")
            .defineInRange("clickLavaDelayMs", 0, 0, 60000);

    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAND_DELAY_MS = BUILDER.comment(
                    "LavaFlow click-land start delay in milliseconds")
            .defineInRange("clickLandDelayMs", 500, 0, 60000);

    public static final ModConfigSpec.DoubleValue LAVAFLOW_SHIFT_RADIUS = BUILDER.comment(
                    "LavaFlow shift bloom radius in blocks (Korra 7)")
            .defineInRange("shiftRadius", 7.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue LAVAFLOW_CLICK_RANGE =
            BUILDER.comment("LavaFlow click range in blocks (Korra 10)").defineInRange("clickRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue LAVAFLOW_CLICK_RADIUS =
            BUILDER.comment("LavaFlow click radius in blocks (Korra 5)").defineInRange("clickRadius", 5.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue LAVAFLOW_RECT_LENGTH =
            BUILDER.comment("LavaFlow click rectangle length in blocks").defineInRange("rectLength", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.IntValue LAVAFLOW_SHIFT_COOLDOWN_MS = BUILDER.comment(
                    "LavaFlow shift cooldown in milliseconds (Korra 20s)")
            .defineInRange("shiftCooldownMs", 20000, 0, 360000);

    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAVA_COOLDOWN_MS = BUILDER.comment(
                    "LavaFlow click-lava cooldown in milliseconds (Korra 10s)")
            .defineInRange("clickLavaCooldownMs", 10000, 0, 360000);

    public static final ModConfigSpec.IntValue LAVAFLOW_CLICK_LAND_COOLDOWN_MS = BUILDER.comment(
                    "LavaFlow click-land cooldown in milliseconds (Korra 0.5s)")
            .defineInRange("clickLandCooldownMs", 500, 0, 360000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("LavaFlux settings").push("LavaFlux");
    }

    public static final ModConfigSpec.ConfigValue<String> LAVAFLUX_FLOW_PARTICLE =
            BUILDER.comment("LavaFlux flow particle id").define("flowParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVAFLUX_FLOW_PARTICLE_COUNT =
            BUILDER.comment("LavaFlux flow particle count").defineInRange("flowParticleCount", 2, 0, 64);

    public static final ModConfigSpec.IntValue LAVAFLUX_CLEANUP_MS = BUILDER.comment(
                    "LavaFlux stone seal lifetime in milliseconds (JedCore Cleanup 1000ms)")
            .defineInRange("cleanupMs", 1000, 250, 60000);

    public static final ModConfigSpec.IntValue LAVAFLUX_SPLASH_REVERT_MS = BUILDER.comment(
                    "LavaFlux head splash revert in milliseconds")
            .defineInRange("splashRevertMs", 300, 50, 10000);

    public static final ModConfigSpec.DoubleValue LAVAFLUX_HIT_RADIUS =
            BUILDER.comment("LavaFlux head burn half-box in blocks").defineInRange("hitRadius", 1.5, 0.5, 6.0);

    public static final ModConfigSpec.IntValue LAVAFLUX_FIRE_MS =
            BUILDER.comment("LavaFlux touch ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 30000);

    public static final ModConfigSpec.DoubleValue LAVAFLUX_KNOCKUP =
            BUILDER.comment("LavaFlux touch upward pop").defineInRange("knockup", 1.0, 0.0, 4.0);

    public static final ModConfigSpec.IntValue LAVAFLUX_COOLDOWN_MS = BUILDER.comment(
                    "LavaFlux cooldown in milliseconds (JedCore 8000ms)")
            .defineInRange("cooldownMs", 8000, 0, 360000);

    public static final ModConfigSpec.IntValue LAVAFLUX_DURATION_MS = BUILDER.comment(
                    "LavaFlux hold duration in milliseconds (JedCore 4000ms)")
            .defineInRange("durationMs", 4000, 500, 120000);

    public static final ModConfigSpec.IntValue LAVAFLUX_RANGE =
            BUILDER.comment("LavaFlux length in blocks (JedCore 12)").defineInRange("range", 12, 2, 32);

    public static final ModConfigSpec.DoubleValue LAVAFLUX_DAMAGE =
            BUILDER.comment("LavaFlux touch damage (JedCore 1)").defineInRange("damage", 1.0, 0.0, 40.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("LavaSurge settings").push("LavaSurge");
    }

    public static final ModConfigSpec.ConfigValue<String> LAVASURGE_SOURCE_PARTICLE =
            BUILDER.comment("LavaSurge source particle id").define("sourceParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVASURGE_SOURCE_PARTICLE_COUNT =
            BUILDER.comment("LavaSurge source particle count").defineInRange("sourceParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVASURGE_TRAIL_PARTICLE =
            BUILDER.comment("LavaSurge trail particle id").define("trailParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVASURGE_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("LavaSurge trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LAVASURGE_BURST_PARTICLE =
            BUILDER.comment("LavaSurge burst particle id").define("burstParticle", "minecraft:lava");

    public static final ModConfigSpec.IntValue LAVASURGE_BURST_PARTICLE_COUNT =
            BUILDER.comment("LavaSurge burst particle count").defineInRange("burstParticleCount", 4, 0, 64);

    public static final ModConfigSpec.DoubleValue LAVASURGE_SOURCE_RADIUS = BUILDER.comment(
                    "LavaSurge gather radius in blocks (Addons SourceRadius 3)")
            .defineInRange("sourceRadius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue LAVASURGE_FLASH_MS = BUILDER.comment(
                    "LavaSurge magma flash before the source goes live in milliseconds")
            .defineInRange("flashMs", 1000, 0, 10000);

    public static final ModConfigSpec.IntValue LAVASURGE_SHARD_LIFE_MS = BUILDER.comment(
                    "LavaSurge shard lifetime in milliseconds (reference 4s)")
            .defineInRange("shardLifeMs", 4000, 500, 60000);

    public static final ModConfigSpec.IntValue LAVASURGE_CRATER_REVERT_MS = BUILDER.comment(
                    "LavaSurge source crater lifetime in milliseconds (reference 3s)")
            .defineInRange("craterRevertMs", 3000, 500, 60000);

    public static final ModConfigSpec.IntValue LAVASURGE_COOLDOWN_MS = BUILDER.comment(
                    "LavaSurge cooldown in milliseconds (Addons 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.DoubleValue LAVASURGE_DAMAGE =
            BUILDER.comment("LavaSurge shard damage (Addons 0.5)").defineInRange("damage", 0.5, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue LAVASURGE_SPEED =
            BUILDER.comment("LavaSurge shard speed in blocks per tick").defineInRange("speed", 1.14, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue LAVASURGE_SELECT_RANGE =
            BUILDER.comment("LavaSurge source range in blocks (Addons 5)").defineInRange("selectRange", 5.0, 1.0, 24.0);

    public static final ModConfigSpec.IntValue LAVASURGE_MAX_BLOCKS =
            BUILDER.comment("LavaSurge max blocks (Addons 10)").defineInRange("maxBlocks", 10, 1, 32);

    public static final ModConfigSpec.IntValue LAVASURGE_BURN_MS =
            BUILDER.comment("LavaSurge burn time in milliseconds (Addons 3s)").defineInRange("burnMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue LAVASURGE_HIT_RADIUS =
            BUILDER.comment("LavaSurge hit radius in blocks").defineInRange("hitRadius", 0.9, 0.2, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("MagnetShield settings").push("MagnetShield");
    }

    public static final ModConfigSpec.IntValue MAGNETSHIELD_DURATION_MS = BUILDER.comment(
                    "MagnetShield duration in milliseconds (JedCore 6000ms)")
            .defineInRange("durationMs", 6000, 500, 120000);

    public static final ModConfigSpec.IntValue MAGNETSHIELD_COOLDOWN_MS = BUILDER.comment(
                    "MagnetShield cooldown in milliseconds (JedCore 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.DoubleValue MAGNETSHIELD_RANGE =
            BUILDER.comment("MagnetShield range in blocks (JedCore 5)").defineInRange("range", 5.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue MAGNETSHIELD_VELOCITY =
            BUILDER.comment("MagnetShield repel velocity").defineInRange("velocity", 0.1, 0.0, 2.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("metalArmor settings").push("metalArmor");
    }

    public static final ModConfigSpec.IntValue METALARMOR_RESIST_MS = BUILDER.comment(
                    "MetalArmor resistance duration in milliseconds (JedCore 4000ms)")
            .defineInRange("resistMs", 4000, 500, 120000);

    public static final ModConfigSpec.IntValue METALARMOR_RESIST_AMPLIFIER =
            BUILDER.comment("MetalArmor resistance amplifier (JedCore 2)").defineInRange("resistAmplifier", 2, 0, 5);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("MetalClips settings").push("MetalClips");
    }

    public static final ModConfigSpec.IntValue METALCLIPS_SHOT_LIFE_MS = BUILDER.comment(
                    "MetalClips thrown ingot lifetime in milliseconds")
            .defineInRange("shotLifeMs", 10000, 1000, 120000);

    public static final ModConfigSpec.IntValue METALCLIPS_MAX_CLIPS =
            BUILDER.comment("MetalClips max clips per victim").defineInRange("maxClips", 4, 1, 8);

    public static final ModConfigSpec.DoubleValue METALCLIPS_RANGE =
            BUILDER.comment("MetalClips shot range in blocks").defineInRange("range", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue METALCLIPS_SHOOT_SPEED =
            BUILDER.comment("MetalClips shot speed in blocks per tick").defineInRange("shootSpeed", 3.0, 0.5, 8.0);

    public static final ModConfigSpec.DoubleValue METALCLIPS_HIT_DAMAGE =
            BUILDER.comment("MetalClips plain-hit damage").defineInRange("hitDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue METALCLIPS_CRUSH_DAMAGE =
            BUILDER.comment("MetalClips crush damage").defineInRange("crushDamage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue METALCLIPS_ARMOR_MS =
            BUILDER.comment("MetalClips wrap duration in milliseconds").defineInRange("armorMs", 10000, 1000, 120000);

    public static final ModConfigSpec.DoubleValue METALCLIPS_MAGNET_RANGE =
            BUILDER.comment("MetalClips sneak magnet range in blocks").defineInRange("magnetRange", 20.0, 4.0, 48.0);

    public static final ModConfigSpec.DoubleValue METALCLIPS_MAGNET_SPEED =
            BUILDER.comment("MetalClips magnet pull speed").defineInRange("magnetSpeed", 0.6, 0.05, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("MetalFragments settings").push("MetalFragments");
    }

    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_LEASH_RADIUS = BUILDER.comment(
                    "MetalFragments source leash radius in blocks")
            .defineInRange("leashRadius", 10.0, 4.0, 32.0);

    public static final ModConfigSpec.IntValue METALFRAGMENTS_COOLDOWN_MS = BUILDER.comment(
                    "MetalFragments cooldown in milliseconds (JedCore 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.IntValue METALFRAGMENTS_MAX_SOURCES =
            BUILDER.comment("MetalFragments max sources (JedCore 3)").defineInRange("maxSources", 3, 1, 12);

    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_SOURCE_RANGE = BUILDER.comment(
                    "MetalFragments source range in blocks (JedCore 5)")
            .defineInRange("sourceRange", 5.0, 1.0, 24.0);

    public static final ModConfigSpec.IntValue METALFRAGMENTS_MAX_FRAGMENTS = BUILDER.comment(
                    "MetalFragments fragments per source (JedCore 10)")
            .defineInRange("maxFragments", 10, 1, 64);

    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_DAMAGE =
            BUILDER.comment("MetalFragments damage (JedCore 4)").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_VELOCITY = BUILDER.comment(
                    "MetalFragments velocity in blocks per tick (JedCore 2)")
            .defineInRange("velocity", 2.0, 0.5, 8.0);

    public static final ModConfigSpec.DoubleValue METALFRAGMENTS_AIM_RANGE =
            BUILDER.comment("MetalFragments aim range in blocks").defineInRange("aimRange", 30.0, 4.0, 96.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("MetalHook settings").push("MetalHook");
    }

    public static final ModConfigSpec.DoubleValue METALHOOK_PULL_SPEED =
            BUILDER.comment("MetalHook max haul speed in blocks per tick").defineInRange("pullSpeed", 0.8, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue METALHOOK_PULL_FACTOR = BUILDER.comment(
                    "MetalHook haul speed factor of anchor distance")
            .defineInRange("pullFactor", 0.4, 0.05, 2.0);

    public static final ModConfigSpec.IntValue METALHOOK_COOLDOWN_MS = BUILDER.comment(
                    "MetalHook cooldown in milliseconds (JedCore 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 360000);

    public static final ModConfigSpec.DoubleValue METALHOOK_RANGE =
            BUILDER.comment("MetalHook range in blocks (JedCore 30)").defineInRange("range", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.IntValue METALHOOK_MAX_HOOKS =
            BUILDER.comment("MetalHook max hooks (JedCore 3)").defineInRange("maxHooks", 3, 1, 12);

    public static final ModConfigSpec.DoubleValue METALHOOK_HOOK_SPEED =
            BUILDER.comment("MetalHook flight speed in blocks per tick").defineInRange("hookSpeed", 3.0, 0.5, 8.0);

    public static final ModConfigSpec.IntValue METALHOOK_SNEAK_RELEASE_MS = BUILDER.comment(
                    "MetalHook sneak-hold release time in milliseconds")
            .defineInRange("sneakReleaseMs", 1000, 250, 10000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("MudSurge settings").push("MudSurge");
    }

    public static final ModConfigSpec.DoubleValue MUDSURGE_SOURCE_RADIUS =
            BUILDER.comment("MudSurge gather radius in blocks").defineInRange("sourceRadius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue MUDSURGE_BLIND_CHANCE = BUILDER.comment(
                    "MudSurge blinding splash chance percent (reference BlindChance 10)")
            .defineInRange("blindChance", 10, 0, 100);

    public static final ModConfigSpec.IntValue MUDSURGE_FLASH_MS = BUILDER.comment(
                    "MudSurge dust flash before the source goes live in milliseconds")
            .defineInRange("flashMs", 1000, 0, 10000);

    public static final ModConfigSpec.IntValue MUDSURGE_SHARD_LIFE_MS =
            BUILDER.comment("MudSurge shard lifetime in milliseconds").defineInRange("shardLifeMs", 4000, 500, 60000);

    public static final ModConfigSpec.IntValue MUDSURGE_CRATER_REVERT_MS = BUILDER.comment(
                    "MudSurge source crater lifetime in milliseconds (reference 3s)")
            .defineInRange("craterRevertMs", 3000, 500, 60000);

    public static final ModConfigSpec.DoubleValue MUDSURGE_KNOCKBACK =
            BUILDER.comment("MudSurge shard knockback strength").defineInRange("knockback", 0.5, 0.0, 4.0);

    public static final ModConfigSpec.IntValue MUDSURGE_COOLDOWN_MS = BUILDER.comment(
                    "MudSurge cooldown in milliseconds (Korra 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.DoubleValue MUDSURGE_DAMAGE =
            BUILDER.comment("MudSurge shard damage (Korra 1)").defineInRange("damage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue MUDSURGE_SPEED =
            BUILDER.comment("MudSurge shard speed in blocks per tick").defineInRange("speed", 1.14, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue MUDSURGE_SELECT_RANGE =
            BUILDER.comment("MudSurge source range in blocks").defineInRange("selectRange", 5.0, 1.0, 24.0);

    public static final ModConfigSpec.IntValue MUDSURGE_MAX_BLOCKS =
            BUILDER.comment("MudSurge max blocks").defineInRange("maxBlocks", 10, 1, 32);

    public static final ModConfigSpec.IntValue MUDSURGE_BLIND_MS =
            BUILDER.comment("MudSurge blindness duration in milliseconds").defineInRange("blindMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue MUDSURGE_HIT_RADIUS =
            BUILDER.comment("MudSurge hit radius in blocks").defineInRange("hitRadius", 0.9, 0.2, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("passives settings").push("passives");
    }

    public static final ModConfigSpec.DoubleValue PASSIVE_FERRO_REACH =
            BUILDER.comment("FerroControl reach in blocks").defineInRange("ferroReach", 5.0, 1.0, 16.0);

    public static final ModConfigSpec.IntValue PASSIVE_FERRO_DEBOUNCE_MS =
            BUILDER.comment("FerroControl debounce in milliseconds").defineInRange("ferroDebounceMs", 200, 0, 2000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("QuickWeld settings").push("QuickWeld");
    }

    public static final ModConfigSpec.IntValue QUICKWELD_COOLDOWN_MS = BUILDER.comment(
                    "QuickWeld cooldown in milliseconds (Addons 1000ms)")
            .defineInRange("cooldownMs", 1000, 0, 360000);

    public static final ModConfigSpec.IntValue QUICKWELD_REPAIR_AMOUNT =
            BUILDER.comment("QuickWeld durability per ingot (Addons 25)").defineInRange("repairAmount", 25, 1, 500);

    public static final ModConfigSpec.IntValue QUICKWELD_REPAIR_EVERY_MS = BUILDER.comment(
                    "QuickWeld repair interval in milliseconds (Addons 1250ms)")
            .defineInRange("repairEveryMs", 1250, 50, 60000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("RaiseEarth settings").push("RaiseEarth");
    }

    public static final ModConfigSpec.IntValue RAISEEARTH_STAND_MS =
            BUILDER.comment("RaiseEarth wall stand time in milliseconds").defineInRange("standMs", 30000, 1000, 360000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("RockSlide settings").push("RockSlide");
    }

    public static final ModConfigSpec.DoubleValue ROCKSLIDE_HIT_RADIUS =
            BUILDER.comment("RockSlide contact hit radius in blocks").defineInRange("hitRadius", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.IntValue ROCKSLIDE_COOLDOWN_MS = BUILDER.comment(
                    "RockSlide cooldown in milliseconds (Addons 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.DoubleValue ROCKSLIDE_DAMAGE =
            BUILDER.comment("RockSlide contact damage (Addons 1)").defineInRange("damage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ROCKSLIDE_KNOCKBACK =
            BUILDER.comment("RockSlide knockback (Addons 0.9)").defineInRange("knockback", 0.9, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue ROCKSLIDE_KNOCKUP =
            BUILDER.comment("RockSlide knockup (Addons 0.4)").defineInRange("knockup", 0.4, 0.0, 4.0);

    public static final ModConfigSpec.DoubleValue ROCKSLIDE_SPEED = BUILDER.comment(
                    "RockSlide ride speed in blocks per tick (Addons 0.68)")
            .defineInRange("speed", 0.68, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue ROCKSLIDE_TURNING =
            BUILDER.comment("RockSlide steering rate (Addons 0.086)").defineInRange("turning", 0.086, 0.01, 1.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Shockwave settings").push("Shockwave");
    }

    public static final ModConfigSpec.ConfigValue<String> SHOCKWAVE_PUFF_PARTICLE =
            BUILDER.comment("Shockwave puff particle id").define("puffParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue SHOCKWAVE_PUFF_PARTICLE_COUNT =
            BUILDER.comment("Shockwave puff particle count").defineInRange("puffParticleCount", 1, 0, 64);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_START_RADIUS =
            BUILDER.comment("ShockwaveRing starting radius in blocks").defineInRange("ringStartRadius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue SHOCKWAVE_RING_BAND =
            BUILDER.comment("ShockwaveRing crest thickness in cells").defineInRange("ringBand", 2, 1, 4);

    public static final ModConfigSpec.IntValue SHOCKWAVE_RING_HOP_MS = BUILDER.comment(
                    "ShockwaveRing popped block flight time in milliseconds")
            .defineInRange("ringHopMs", 600, 100, 10000);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_UP_POP =
            BUILDER.comment("ShockwaveRing crest up-pop strength").defineInRange("ringUpPop", 0.32, 0.0, 2.0);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_OUT_DRIFT =
            BUILDER.comment("ShockwaveRing crest outward drift").defineInRange("ringOutDrift", 0.2, 0.0, 2.0);

    public static final ModConfigSpec.IntValue SHOCKWAVE_CHARGE_MS = BUILDER.comment(
                    "Shockwave charge time in milliseconds (Korra 2500ms)")
            .defineInRange("chargeMs", 2500, 50, 60000);

    public static final ModConfigSpec.IntValue SHOCKWAVE_COOLDOWN_MS = BUILDER.comment(
                    "Shockwave cooldown in milliseconds (Korra 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_FALL_THRESHOLD = BUILDER.comment(
                    "Shockwave fall-slam threshold in blocks (Korra 12)")
            .defineInRange("fallThreshold", 12.0, 2.0, 40.0);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_RANGE =
            BUILDER.comment("ShockwaveRing range in blocks (Korra 15)").defineInRange("ringRange", 15.0, 4.0, 48.0);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_DAMAGE =
            BUILDER.comment("ShockwaveRing crest damage").defineInRange("ringDamage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_KNOCKBACK =
            BUILDER.comment("ShockwaveRing radial shove").defineInRange("ringKnockback", 3.0, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue SHOCKWAVE_RING_SPEED =
            BUILDER.comment("ShockwaveRing cells per tick").defineInRange("ringSpeed", 1.0, 0.25, 4.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Shrapnel settings").push("Shrapnel");
    }

    public static final ModConfigSpec.ConfigValue<String> SHRAPNEL_TRAIL_PARTICLE =
            BUILDER.comment("Shrapnel trail particle id").define("trailParticle", "minecraft:crit");

    public static final ModConfigSpec.IntValue SHRAPNEL_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("Shrapnel trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    public static final ModConfigSpec.DoubleValue SHRAPNEL_BLAST_SPREAD_DEGREES = BUILDER.comment(
                    "Shrapnel blast cone half-angle in degrees (Addons Spread)")
            .defineInRange("blastSpreadDegrees", 6.0, 0.0, 45.0);

    public static final ModConfigSpec.IntValue SHRAPNEL_SHOT_COOLDOWN_MS = BUILDER.comment(
                    "Shrapnel shot cooldown in milliseconds (Addons 2000ms)")
            .defineInRange("shotCooldownMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue SHRAPNEL_BLAST_COOLDOWN_MS = BUILDER.comment(
                    "Shrapnel blast cooldown in milliseconds (Addons 8000ms)")
            .defineInRange("blastCooldownMs", 8000, 0, 360000);

    public static final ModConfigSpec.DoubleValue SHRAPNEL_DAMAGE =
            BUILDER.comment("Shrapnel max damage (Addons 2)").defineInRange("damage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue SHRAPNEL_SHOT_SPEED = BUILDER.comment(
                    "Shrapnel shot speed in blocks per tick (Addons 2.3)")
            .defineInRange("shotSpeed", 2.3, 0.5, 8.0);

    public static final ModConfigSpec.DoubleValue SHRAPNEL_BLAST_SPEED = BUILDER.comment(
                    "Shrapnel blast speed in blocks per tick (Addons 1.7)")
            .defineInRange("blastSpeed", 1.7, 0.5, 8.0);

    public static final ModConfigSpec.IntValue SHRAPNEL_BLAST_SHOTS =
            BUILDER.comment("Shrapnel blast shells (Addons 9)").defineInRange("blastShots", 9, 1, 32);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Fire abilities").push("fire");
    }

    static {
        BUILDER.comment("ArcSpark settings").push("ArcSpark");
    }

    public static final ModConfigSpec.IntValue ARCSPARK_COOLDOWN_MS = BUILDER.comment(
                    "ArcSpark cooldown in milliseconds (reference Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.IntValue ARCSPARK_CHARGE_MS = BUILDER.comment(
                    "ArcSpark sneak charge in milliseconds (reference Charge 1500ms)")
            .defineInRange("chargeMs", 1500, 0, 360000);

    public static final ModConfigSpec.IntValue ARCSPARK_DURATION_MS = BUILDER.comment(
                    "ArcSpark active window after the shot in milliseconds (reference Duration 1500ms)")
            .defineInRange("durationMs", 1500, 0, 360000);

    public static final ModConfigSpec.IntValue ARCSPARK_SPEED = BUILDER.comment(
                    "ArcSpark volley steps multiplier (speed * length steps of 0.3 per tick)")
            .defineInRange("speed", 2, 1, 8);

    public static final ModConfigSpec.IntValue ARCSPARK_LENGTH = BUILDER.comment(
                    "ArcSpark volley length multiplier (speed * length steps of 0.3 per tick)")
            .defineInRange("length", 8, 1, 16);

    public static final ModConfigSpec.DoubleValue ARCSPARK_DAMAGE =
            BUILDER.comment("ArcSpark lightning damage on contact").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ARCSPARK_RANGE =
            BUILDER.comment("ArcSpark travel range in blocks").defineInRange("range", 16.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ARCSPARK_SEEK_RADIUS =
            BUILDER.comment("ArcSpark entity seek radius in blocks").defineInRange("seekRadius", 3.0, 0.5, 8.0);

    public static final ModConfigSpec.IntValue ARCSPARK_METAL_SCAN_RADIUS =
            BUILDER.comment("ArcSpark metallic block scan radius in blocks").defineInRange("metalScanRadius", 2, 1, 4);

    public static final ModConfigSpec.IntValue ARCSPARK_CONE_ANGLE_DEG =
            BUILDER.comment("ArcSpark steering cone in degrees").defineInRange("coneAngleDeg", 60, 5, 180);

    public static final ModConfigSpec.DoubleValue ARCSPARK_HIT_DISTANCE =
            BUILDER.comment("ArcSpark contact distance in blocks").defineInRange("hitDistance", 1.0, 0.2, 4.0);

    public static final ModConfigSpec.DoubleValue ARCSPARK_STEP_LENGTH =
            BUILDER.comment("ArcSpark advance per step in blocks").defineInRange("stepLength", 0.3, 0.05, 1.0);

    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_CHARGE_PARTICLE =
            BUILDER.comment("ArcSpark charge particle id").define("chargeParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue ARCSPARK_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark charge particle count").defineInRange("chargeParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_READY_PARTICLE =
            BUILDER.comment("ArcSpark ready particle id").define("readyParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue ARCSPARK_READY_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark ready particle count").defineInRange("readyParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_GROUND_PARTICLE =
            BUILDER.comment("ArcSpark ground particle id").define("groundParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue ARCSPARK_GROUND_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark ground particle count").defineInRange("groundParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ARCSPARK_TRAIL_PARTICLE =
            BUILDER.comment("ArcSpark trail particle id").define("trailParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue ARCSPARK_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ArcSpark trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Bolt settings").push("Bolt");
    }

    public static final ModConfigSpec.IntValue BOLT_COOLDOWN_MS = BUILDER.comment(
                    "Bolt cooldown in milliseconds (reference Cooldown 3500ms)")
            .defineInRange("cooldownMs", 3500, 0, 360000);

    public static final ModConfigSpec.IntValue BOLT_CHARGE_MS = BUILDER.comment(
                    "Bolt sneak charge in milliseconds (reference Charge 1500ms)")
            .defineInRange("chargeMs", 1500, 0, 360000);

    public static final ModConfigSpec.DoubleValue BOLT_DAMAGE =
            BUILDER.comment("Bolt base lightning damage").defineInRange("damage", 5.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue BOLT_RANGE =
            BUILDER.comment("Bolt targeting range in blocks").defineInRange("range", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue BOLT_POINT_GENERATION =
            BUILDER.comment("Bolt midpoint-displacement generations").defineInRange("pointGeneration", 5, 1, 8);

    public static final ModConfigSpec.DoubleValue BOLT_FALLOFF_RADIUS =
            BUILDER.comment("Bolt damage falloff radius in blocks").defineInRange("falloffRadius", 5.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue BOLT_FULL_DAMAGE_RADIUS =
            BUILDER.comment("Bolt full damage radius in blocks").defineInRange("fullDamageRadius", 1.5, 0.2, 8.0);

    public static final ModConfigSpec.DoubleValue BOLT_CHANNEL_SHARE_RADIUS = BUILDER.comment(
                    "Bolt nearby-channel share radius in blocks")
            .defineInRange("channelShareRadius", 4.0, 1.0, 16.0);

    public static final ModConfigSpec.ConfigValue<String> BOLT_CHARGE_PARTICLE =
            BUILDER.comment("Bolt charge particle id").define("chargeParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue BOLT_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("Bolt charge particle count").defineInRange("chargeParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> BOLT_STRIKE_PARTICLE =
            BUILDER.comment("Bolt strike particle id").define("strikeParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue BOLT_STRIKE_PARTICLE_COUNT =
            BUILDER.comment("Bolt strike particle count").defineInRange("strikeParticleCount", 12, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> BOLT_BEAM_PARTICLE =
            BUILDER.comment("Bolt beam particle id").define("beamParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue BOLT_BEAM_PARTICLE_COUNT =
            BUILDER.comment("Bolt beam particle count").defineInRange("beamParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("ChargeBolt settings").push("ChargeBolt");
    }

    public static final ModConfigSpec.IntValue CHARGEBOLT_COOLDOWN_MS = BUILDER.comment(
                    "ChargeBolt cooldown in milliseconds (reference Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.IntValue CHARGEBOLT_CHARGE_MS = BUILDER.comment(
                    "ChargeBolt sneak charge in milliseconds (reference Charge 1500ms)")
            .defineInRange("chargeMs", 1500, 0, 360000);

    public static final ModConfigSpec.DoubleValue CHARGEBOLT_DAMAGE =
            BUILDER.comment("ChargeBolt lightning damage per bolt").defineInRange("damage", 5.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue CHARGEBOLT_BOLT_RANGE =
            BUILDER.comment("ChargeBolt single-throw range in blocks").defineInRange("boltRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue CHARGEBOLT_BLAST_RADIUS = BUILDER.comment(
                    "ChargeBolt discharge cone bolt range in blocks")
            .defineInRange("blastRadius", 3.0, 1.0, 16.0);

    public static final ModConfigSpec.IntValue CHARGEBOLT_SPEED =
            BUILDER.comment("ChargeBolt advances per tick").defineInRange("speed", 2, 1, 8);

    public static final ModConfigSpec.IntValue CHARGEBOLT_STOCK =
            BUILDER.comment("ChargeBolt bolt stock per charge").defineInRange("stock", 5, 1, 16);

    public static final ModConfigSpec.DoubleValue CHARGEBOLT_HIT_RADIUS =
            BUILDER.comment("ChargeBolt hit radius in blocks").defineInRange("hitRadius", 0.62, 0.1, 4.0);

    public static final ModConfigSpec.IntValue CHARGEBOLT_JITTER_DEGREES =
            BUILDER.comment("ChargeBolt per-step yaw/pitch jitter in degrees").defineInRange("jitterDegrees", 8, 0, 30);

    public static final ModConfigSpec.IntValue CHARGEBOLT_SPREAD_YAW =
            BUILDER.comment("ChargeBolt discharge cone half yaw in degrees").defineInRange("spreadYaw", 30, 0, 90);

    public static final ModConfigSpec.IntValue CHARGEBOLT_SPREAD_PITCH =
            BUILDER.comment("ChargeBolt discharge cone half pitch in degrees").defineInRange("spreadPitch", 23, 0, 90);

    public static final ModConfigSpec.ConfigValue<String> CHARGEBOLT_CHARGE_PARTICLE =
            BUILDER.comment("ChargeBolt charge particle id").define("chargeParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue CHARGEBOLT_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("ChargeBolt charge particle count").defineInRange("chargeParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> CHARGEBOLT_TRAIL_PARTICLE =
            BUILDER.comment("ChargeBolt trail particle id").define("trailParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue CHARGEBOLT_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ChargeBolt trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("CombustBeam settings").push("CombustBeam");
    }

    public static final ModConfigSpec.IntValue COMBUSTBEAM_COOLDOWN_MS = BUILDER.comment(
                    "CombustBeam cooldown in milliseconds (reference Cooldown 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.IntValue COMBUSTBEAM_MIN_CHARGE_MS = BUILDER.comment(
                    "CombustBeam minimum charge in milliseconds (reference MinCharge 1000ms)")
            .defineInRange("minChargeMs", 1000, 0, 360000);

    public static final ModConfigSpec.IntValue COMBUSTBEAM_MAX_CHARGE_MS = BUILDER.comment(
                    "CombustBeam maximum charge in milliseconds (reference MaxCharge 3000ms)")
            .defineInRange("maxChargeMs", 3000, 50, 360000);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_RANGE =
            BUILDER.comment("CombustBeam travel range in blocks").defineInRange("range", 25.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MIN_POWER =
            BUILDER.comment("CombustBeam beam power at minimum charge").defineInRange("minPower", 1.0, 0.1, 12.0);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MAX_POWER =
            BUILDER.comment("CombustBeam beam power at maximum charge").defineInRange("maxPower", 4.0, 0.1, 12.0);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MIN_DAMAGE =
            BUILDER.comment("CombustBeam blast damage at minimum charge").defineInRange("minDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MAX_DAMAGE =
            BUILDER.comment("CombustBeam blast damage at maximum charge").defineInRange("maxDamage", 9.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_MAX_ANGLE =
            BUILDER.comment("CombustBeam steering limit in degrees").defineInRange("maxAngle", 15.0, 0.0, 90.0);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_BLAST_BASE_RADIUS = BUILDER.comment(
                    "CombustBeam blast base radius in blocks (radius = base + power)")
            .defineInRange("blastBaseRadius", 2.0, 0.0, 12.0);

    public static final ModConfigSpec.DoubleValue COMBUSTBEAM_KNOCKBACK_CAP =
            BUILDER.comment("CombustBeam blast knockback cap").defineInRange("knockbackCap", 4.0, 0.0, 12.0);

    public static final ModConfigSpec.IntValue COMBUSTBEAM_IGNITE_MS =
            BUILDER.comment("CombustBeam ignite duration in milliseconds").defineInRange("igniteMs", 3000, 0, 20000);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_FLAME_PARTICLE =
            BUILDER.comment("CombustBeam flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue COMBUSTBEAM_FLAME_PARTICLE_COUNT =
            BUILDER.comment("CombustBeam flame particle count").defineInRange("flameParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_BURST_PARTICLE =
            BUILDER.comment("CombustBeam burst particle id").define("burstParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue COMBUSTBEAM_BURST_PARTICLE_COUNT =
            BUILDER.comment("CombustBeam burst particle count").defineInRange("burstParticleCount", 12, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_SMOKE_PARTICLE =
            BUILDER.comment("CombustBeam smoke particle id").define("smokeParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue COMBUSTBEAM_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("CombustBeam smoke particle count").defineInRange("smokeParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTBEAM_BLAST_PARTICLE =
            BUILDER.comment("CombustBeam blast particle id").define("blastParticle", "minecraft:explosion");

    public static final ModConfigSpec.IntValue COMBUSTBEAM_BLAST_PARTICLE_COUNT =
            BUILDER.comment("CombustBeam blast particle count").defineInRange("blastParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Combustion settings").push("Combustion");
    }

    public static final ModConfigSpec.IntValue COMBUSTION_COOLDOWN_MS = BUILDER.comment(
                    "Combustion cooldown in milliseconds (reference Cooldown 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.DoubleValue COMBUSTION_DAMAGE =
            BUILDER.comment("Combustion detonation damage").defineInRange("damage", 6.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue COMBUSTION_RADIUS =
            BUILDER.comment("Combustion entity blast radius in blocks").defineInRange("radius", 4.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue COMBUSTION_SPEED =
            BUILDER.comment("Combustion beam speed in blocks per second").defineInRange("speed", 20.0, 1.0, 60.0);

    public static final ModConfigSpec.DoubleValue COMBUSTION_RANGE =
            BUILDER.comment("Combustion beam travel range in blocks").defineInRange("range", 25.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue COMBUSTION_HIT_RADIUS =
            BUILDER.comment("Combustion beam contact radius in blocks").defineInRange("hitRadius", 2.0, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue COMBUSTION_EXPLOSION_POWER =
            BUILDER.comment("Combustion vanilla explosion power").defineInRange("explosionPower", 3.0, 0.0, 12.0);

    public static final ModConfigSpec.DoubleValue COMBUSTION_KNOCKBACK =
            BUILDER.comment("Combustion detonation knockback strength").defineInRange("knockback", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue COMBUSTION_IGNITE_MS =
            BUILDER.comment("Combustion ignite duration in milliseconds").defineInRange("igniteMs", 3000, 0, 20000);

    public static final ModConfigSpec.IntValue COMBUSTION_MAX_MS = BUILDER.comment(
                    "Combustion beam maximum flight time in milliseconds")
            .defineInRange("maxMs", 10000, 1000, 120000);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_BURST_PARTICLE =
            BUILDER.comment("Combustion burst particle id").define("burstParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue COMBUSTION_BURST_PARTICLE_COUNT =
            BUILDER.comment("Combustion burst particle count").defineInRange("burstParticleCount", 16, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_FLAME_PARTICLE =
            BUILDER.comment("Combustion flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue COMBUSTION_FLAME_PARTICLE_COUNT =
            BUILDER.comment("Combustion flame particle count").defineInRange("flameParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_FIZZ_PARTICLE =
            BUILDER.comment("Combustion fizz particle id").define("fizzParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue COMBUSTION_FIZZ_PARTICLE_COUNT =
            BUILDER.comment("Combustion fizz particle count").defineInRange("fizzParticleCount", 8, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_TRAIL_PARTICLE =
            BUILDER.comment("Combustion trail particle id").define("trailParticle", "minecraft:firework");

    public static final ModConfigSpec.IntValue COMBUSTION_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("Combustion trail particle count").defineInRange("trailParticleCount", 4, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_SMOKE_PARTICLE =
            BUILDER.comment("Combustion smoke particle id").define("smokeParticle", "minecraft:large_smoke");

    public static final ModConfigSpec.IntValue COMBUSTION_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("Combustion smoke particle count").defineInRange("smokeParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("CombustionBlast settings").push("CombustionBlast");
    }

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_COOLDOWN_MS = BUILDER.comment(
                    "CombustionBlast cooldown in milliseconds (reference Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_CHARGE_MS = BUILDER.comment(
                    "CombustionBlast sneak charge in milliseconds (reference Charge 2000ms)")
            .defineInRange("chargeMs", 2000, 0, 360000);

    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_DAMAGE =
            BUILDER.comment("CombustionBlast detonation damage").defineInRange("damage", 5.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_BLAST_RADIUS = BUILDER.comment(
                    "CombustionBlast entity blast radius in blocks")
            .defineInRange("blastRadius", 4.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_RANGE =
            BUILDER.comment("CombustionBlast beam travel range in blocks").defineInRange("range", 25.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_HIT_RADIUS =
            BUILDER.comment("CombustionBlast beam contact radius in blocks").defineInRange("hitRadius", 1.5, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue COMBUSTIONBLAST_EXPLOSION_POWER =
            BUILDER.comment("CombustionBlast vanilla explosion power").defineInRange("explosionPower", 3.0, 0.0, 12.0);

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_IGNITE_MS = BUILDER.comment(
                    "CombustionBlast ignite duration in milliseconds")
            .defineInRange("igniteMs", 3000, 0, 20000);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_FLAME_PARTICLE =
            BUILDER.comment("CombustionBlast flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_FLAME_PARTICLE_COUNT =
            BUILDER.comment("CombustionBlast flame particle count").defineInRange("flameParticleCount", 4, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_RING_PARTICLE =
            BUILDER.comment("CombustionBlast ring particle id").define("ringParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_RING_PARTICLE_COUNT =
            BUILDER.comment("CombustionBlast ring particle count").defineInRange("ringParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_BURST_PARTICLE =
            BUILDER.comment("CombustionBlast burst particle id").define("burstParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_BURST_PARTICLE_COUNT =
            BUILDER.comment("CombustionBlast burst particle count").defineInRange("burstParticleCount", 16, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_TRAIL_PARTICLE =
            BUILDER.comment("CombustionBlast trail particle id").define("trailParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("CombustionBlast trail particle count").defineInRange("trailParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_SPARK_PARTICLE =
            BUILDER.comment("CombustionBlast spark particle id").define("sparkParticle", "minecraft:firework");

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_SPARK_PARTICLE_COUNT =
            BUILDER.comment("CombustionBlast spark particle count").defineInRange("sparkParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_CHARGE_PARTICLE =
            BUILDER.comment("CombustionBlast charge particle id").define("chargeParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("CombustionBlast charge particle count").defineInRange("chargeParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTIONBLAST_BLAST_PARTICLE =
            BUILDER.comment("CombustionBlast blast particle id").define("blastParticle", "minecraft:explosion");

    public static final ModConfigSpec.IntValue COMBUSTIONBLAST_BLAST_PARTICLE_COUNT =
            BUILDER.comment("CombustionBlast blast particle count").defineInRange("blastParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> COMBUSTION_BLAST_PARTICLE =
            BUILDER.comment("Combustion blast particle id").define("particle", "minecraft:explosion");

    public static final ModConfigSpec.IntValue COMBUSTION_BLAST_PARTICLE_COUNT =
            BUILDER.comment("Combustion blast particle count").defineInRange("particleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Discharge settings").push("Discharge");
    }

    public static final ModConfigSpec.IntValue DISCHARGE_COOLDOWN_MS = BUILDER.comment(
                    "Discharge cooldown in milliseconds (reference Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 360000);

    public static final ModConfigSpec.IntValue DISCHARGE_DURATION_MS = BUILDER.comment(
                    "Discharge crawl duration in milliseconds (reference Duration 2000ms)")
            .defineInRange("durationMs", 2000, 0, 360000);

    public static final ModConfigSpec.DoubleValue DISCHARGE_DAMAGE =
            BUILDER.comment("Discharge shock damage").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue DISCHARGE_RANGE =
            BUILDER.comment("Discharge branch travel range in blocks").defineInRange("range", 18.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue DISCHARGE_HIT_RADIUS =
            BUILDER.comment("Discharge touch hit radius in blocks").defineInRange("hitRadius", 1.5, 0.2, 6.0);

    public static final ModConfigSpec.IntValue DISCHARGE_SPLIT_INTERVAL =
            BUILDER.comment("Discharge branch split interval in spaces").defineInRange("splitInterval", 3, 1, 12);

    public static final ModConfigSpec.IntValue DISCHARGE_STEPS_PER_TICK =
            BUILDER.comment("Discharge branch steps per tick").defineInRange("stepsPerTick", 5, 1, 16);

    public static final ModConfigSpec.DoubleValue DISCHARGE_STEP_LENGTH =
            BUILDER.comment("Discharge branch step length in blocks").defineInRange("stepLength", 0.2, 0.05, 1.0);

    public static final ModConfigSpec.DoubleValue DISCHARGE_KNOCKBACK =
            BUILDER.comment("Discharge shock shove strength").defineInRange("knockback", 0.8, 0.0, 5.0);

    public static final ModConfigSpec.IntValue DISCHARGE_IGNITE_MS =
            BUILDER.comment("Discharge ignite duration in milliseconds").defineInRange("igniteMs", 1000, 0, 20000);

    public static final ModConfigSpec.ConfigValue<String> DISCHARGE_TRAIL_PARTICLE =
            BUILDER.comment("Discharge trail particle id").define("trailParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue DISCHARGE_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("Discharge trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> DISCHARGE_HIT_PARTICLE =
            BUILDER.comment("Discharge hit particle id").define("hitParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue DISCHARGE_HIT_PARTICLE_COUNT =
            BUILDER.comment("Discharge hit particle count").defineInRange("hitParticleCount", 5, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Electrify settings").push("Electrify");
    }

    public static final ModConfigSpec.IntValue ELECTRIFY_COOLDOWN_MS = BUILDER.comment(
                    "Electrify cooldown in milliseconds (reference Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.IntValue ELECTRIFY_DURATION_MS = BUILDER.comment(
                    "Electrify field duration in milliseconds (reference Duration 8000ms)")
            .defineInRange("durationMs", 8000, 0, 360000);

    public static final ModConfigSpec.DoubleValue ELECTRIFY_RANGE =
            BUILDER.comment("Electrify target range in blocks").defineInRange("range", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue ELECTRIFY_WATER_DAMAGE =
            BUILDER.comment("Electrify water damage per tick-window").defineInRange("waterDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ELECTRIFY_SPREAD_DEPTH =
            BUILDER.comment("Electrify spread passes to the 6 faces").defineInRange("spreadDepth", 2, 0, 8);

    public static final ModConfigSpec.IntValue ELECTRIFY_DEBUFF_MS = BUILDER.comment(
                    "Electrify slowness/weakness duration in milliseconds")
            .defineInRange("debuffMs", 500, 0, 60000);

    public static final ModConfigSpec.DoubleValue ELECTRIFY_HIT_RADIUS =
            BUILDER.comment("Electrify victim scan radius in blocks").defineInRange("hitRadius", 1.0, 0.2, 4.0);

    public static final ModConfigSpec.ConfigValue<String> ELECTRIFY_FIELD_PARTICLE =
            BUILDER.comment("Electrify field particle id").define("fieldParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue ELECTRIFY_FIELD_PARTICLE_COUNT =
            BUILDER.comment("Electrify field particle count").defineInRange("fieldParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Explode settings").push("Explode");
    }

    public static final ModConfigSpec.IntValue EXPLODE_COOLDOWN_MS = BUILDER.comment(
                    "Explode cooldown in milliseconds (reference Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.DoubleValue EXPLODE_DAMAGE =
            BUILDER.comment("Explode detonation damage").defineInRange("damage", 6.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue EXPLODE_RADIUS =
            BUILDER.comment("Explode detonation radius in blocks").defineInRange("radius", 5.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue EXPLODE_KNOCKBACK =
            BUILDER.comment("Explode detonation knockback strength").defineInRange("knockback", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue EXPLODE_RANGE =
            BUILDER.comment("Explode target painting range in blocks").defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue EXPLODE_IGNITE_MS =
            BUILDER.comment("Explode ignite duration in milliseconds").defineInRange("igniteMs", 2000, 0, 20000);

    public static final ModConfigSpec.ConfigValue<String> EXPLODE_FLAME_PARTICLE =
            BUILDER.comment("Explode flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue EXPLODE_FLAME_PARTICLE_COUNT =
            BUILDER.comment("Explode flame particle count").defineInRange("flameParticleCount", 14, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> EXPLODE_TARGET_PARTICLE =
            BUILDER.comment("Explode target particle id").define("targetParticle", "minecraft:crit");

    public static final ModConfigSpec.IntValue EXPLODE_TARGET_PARTICLE_COUNT =
            BUILDER.comment("Explode target particle count").defineInRange("targetParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> EXPLODE_BLAST_PARTICLE =
            BUILDER.comment("Explode blast particle id").define("blastParticle", "minecraft:explosion");

    public static final ModConfigSpec.IntValue EXPLODE_BLAST_PARTICLE_COUNT =
            BUILDER.comment("Explode blast particle count").defineInRange("blastParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> EXPLODE_HIT_PARTICLE =
            BUILDER.comment("Explode hit particle id").define("hitParticle", "minecraft:crit");

    public static final ModConfigSpec.IntValue EXPLODE_HIT_PARTICLE_COUNT =
            BUILDER.comment("Explode hit particle count").defineInRange("hitParticleCount", 6, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireBall settings").push("FireBall");
    }

    public static final ModConfigSpec.IntValue FIREBALL_COOLDOWN_MS = BUILDER.comment(
                    "FireBall cooldown in milliseconds (Reference Cooldown 2500ms)")
            .defineInRange("cooldownMs", 2500, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIREBALL_DAMAGE = BUILDER.comment(
                    "FireBall magic damage on hit (Reference Damage 4)")
            .defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIREBALL_RANGE = BUILDER.comment(
                    "FireBall travel range in blocks (Reference Range 20)")
            .defineInRange("range", 20.0, 1.0, 96.0);

    public static final ModConfigSpec.DoubleValue FIREBALL_SPEED = BUILDER.comment(
                    "FireBall flight speed in blocks per tick (Reference Speed 2)")
            .defineInRange("speed", 2.0, 0.0, 6.0);

    public static final ModConfigSpec.IntValue FIREBALL_FIRE_MS = BUILDER.comment(
                    "FireBall ignite duration in milliseconds (Reference FireTicks 3)")
            .defineInRange("fireMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue FIREBALL_HIT_RADIUS = BUILDER.comment(
                    "FireBall hit radius in blocks (Reference HitRadius 1.5)")
            .defineInRange("hitRadius", 1.5, 0.2, 8.0);

    public static final ModConfigSpec.IntValue FIREBALL_MAX_MS =
            BUILDER.comment("FireBall max flight lifetime in milliseconds").defineInRange("maxMs", 10000, 0, 360000);

    public static final ModConfigSpec.BooleanValue FIREBALL_CONTROLLABLE = BUILDER.comment(
                    "FireBall bends toward the gaze mid-flight (Reference Controllable true)")
            .define("controllable", true);

    public static final ModConfigSpec.ConfigValue<String> FIREBALL_SMOKE_PARTICLE =
            BUILDER.comment("FireBall smoke particle id").define("smokeParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIREBALL_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("FireBall smoke particle count").defineInRange("smokeParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBALL_HIT_PARTICLE =
            BUILDER.comment("FireBall hit particle id").define("hitParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREBALL_HIT_PARTICLE_COUNT =
            BUILDER.comment("FireBall hit particle count").defineInRange("hitParticleCount", 10, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBALL_TRAIL_PARTICLE =
            BUILDER.comment("FireBall trail particle id").define("trailParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREBALL_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireBall trail particle count").defineInRange("trailParticleCount", 5, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireBreath settings").push("FireBreath");
    }

    public static final ModConfigSpec.IntValue FIREBREATH_COOLDOWN_MS = BUILDER.comment(
                    "FireBreath cooldown in milliseconds (Reference Cooldown 3500ms)")
            .defineInRange("cooldownMs", 3500, 0, 120000);

    public static final ModConfigSpec.IntValue FIREBREATH_DURATION_MS = BUILDER.comment(
                    "FireBreath max breath duration in milliseconds (Reference Duration 3000ms)")
            .defineInRange("durationMs", 3000, 500, 60000);

    public static final ModConfigSpec.IntValue FIREBREATH_PARTICLES =
            BUILDER.comment("FireBreath flame particles per beam step").defineInRange("particles", 6, 0, 20);

    public static final ModConfigSpec.DoubleValue FIREBREATH_PLAYER_DAMAGE =
            BUILDER.comment("FireBreath magic damage to players").defineInRange("playerDamage", 2.0, 0.0, 20.0);

    public static final ModConfigSpec.DoubleValue FIREBREATH_MOB_DAMAGE =
            BUILDER.comment("FireBreath magic damage to mobs").defineInRange("mobDamage", 3.0, 0.0, 20.0);

    public static final ModConfigSpec.IntValue FIREBREATH_FIRE_MS =
            BUILDER.comment("FireBreath ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 30000);

    public static final ModConfigSpec.DoubleValue FIREBREATH_RANGE =
            BUILDER.comment("FireBreath beam range in blocks").defineInRange("range", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.ConfigValue<String> FIREBREATH_TRAIL_PARTICLE =
            BUILDER.comment("FireBreath trail particle id").define("trailParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIREBREATH_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireBreath trail particle count").defineInRange("trailParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBREATH_FLAME_PARTICLE =
            BUILDER.comment("FireBreath flame particle id").define("flameParticle", "minecraft:flame");

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireBurst settings").push("FireBurst");
    }

    public static final ModConfigSpec.IntValue FIREBURST_COOLDOWN_MS = BUILDER.comment(
                    "FireBurst cooldown in milliseconds (Reference Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 360000);

    public static final ModConfigSpec.IntValue FIREBURST_CHARGE_MS = BUILDER.comment(
                    "FireBurst charge time in milliseconds (Reference Charge 1500ms)")
            .defineInRange("chargeMs", 1500, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIREBURST_DAMAGE =
            BUILDER.comment("FireBurst magic damage on hit").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIREBURST_RADIUS =
            BUILDER.comment("FireBurst burst radius in blocks").defineInRange("radius", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue FIREBURST_PUSH =
            BUILDER.comment("FireBurst knockback strength").defineInRange("push", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue FIREBURST_FIRE_MS =
            BUILDER.comment("FireBurst ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 30000);

    public static final ModConfigSpec.ConfigValue<String> FIREBURST_RING_PARTICLE =
            BUILDER.comment("FireBurst ring particle id").define("ringParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIREBURST_RING_PARTICLE_COUNT =
            BUILDER.comment("FireBurst ring particle count").defineInRange("ringParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBURST_CHARGE_PARTICLE =
            BUILDER.comment("FireBurst charge particle id").define("chargeParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREBURST_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst charge particle count").defineInRange("chargeParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBURST_BURST_PARTICLE =
            BUILDER.comment("FireBurst burst particle id").define("burstParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREBURST_BURST_PARTICLE_COUNT =
            BUILDER.comment("FireBurst burst particle count").defineInRange("burstParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBURST_SPHERE_PARTICLE =
            BUILDER.comment("FireBurst sphere particle id").define("sphereParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREBURST_SPHERE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst sphere particle count").defineInRange("sphereParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBURST_CONE_PARTICLE =
            BUILDER.comment("FireBurst cone particle id").define("coneParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREBURST_CONE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst cone particle count").defineInRange("coneParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREBURST_CORE_PARTICLE =
            BUILDER.comment("FireBurst core particle id").define("coreParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREBURST_CORE_PARTICLE_COUNT =
            BUILDER.comment("FireBurst core particle count").defineInRange("coreParticleCount", 4, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireComet settings").push("FireComet");
    }

    public static final ModConfigSpec.IntValue FIRECOMET_COOLDOWN_MS = BUILDER.comment(
                    "FireComet cooldown in milliseconds (Reference Cooldown 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 120000);

    public static final ModConfigSpec.IntValue FIRECOMET_CHARGE_MS = BUILDER.comment(
                    "FireComet charge time in milliseconds (Reference Charge 2500ms)")
            .defineInRange("chargeMs", 2500, 0, 360000);

    public static final ModConfigSpec.IntValue FIRECOMET_SCORCH_REVERT_MS = BUILDER.comment(
                    "FireComet scorch-fire revert delay in milliseconds")
            .defineInRange("scorchRevertMs", 3000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIRECOMET_DAMAGE =
            BUILDER.comment("FireComet magic damage on blast").defineInRange("damage", 7.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIRECOMET_BLAST_RADIUS =
            BUILDER.comment("FireComet blast radius in blocks").defineInRange("blastRadius", 4.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue FIRECOMET_RANGE =
            BUILDER.comment("FireComet travel range in blocks").defineInRange("range", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue FIRECOMET_BLAST_FIRE_MS = BUILDER.comment(
                    "FireComet blast ignite duration in milliseconds")
            .defineInRange("blastFireMs", 4000, 0, 30000);

    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_RING_PARTICLE =
            BUILDER.comment("FireComet ring particle id").define("ringParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIRECOMET_RING_PARTICLE_COUNT =
            BUILDER.comment("FireComet ring particle count").defineInRange("ringParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_BLAST_PARTICLE =
            BUILDER.comment("FireComet blast particle id").define("blastParticle", "minecraft:explosion");

    public static final ModConfigSpec.IntValue FIRECOMET_BLAST_PARTICLE_COUNT =
            BUILDER.comment("FireComet blast particle count").defineInRange("blastParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_SPARK_PARTICLE =
            BUILDER.comment("FireComet spark particle id").define("sparkParticle", "minecraft:firework");

    public static final ModConfigSpec.IntValue FIRECOMET_SPARK_PARTICLE_COUNT =
            BUILDER.comment("FireComet spark particle count").defineInRange("sparkParticleCount", 10, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRECOMET_FLAME_PARTICLE =
            BUILDER.comment("FireComet flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIRECOMET_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireComet flame particle count").defineInRange("flameParticleCount", 20, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireDisc settings").push("FireDisc");
    }

    public static final ModConfigSpec.IntValue FIREDISC_COOLDOWN_MS = BUILDER.comment(
                    "FireDisc cooldown in milliseconds (Reference Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIREDISC_DAMAGE = BUILDER.comment(
                    "FireDisc magic damage on hit (Reference Damage 4)")
            .defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIREDISC_RANGE = BUILDER.comment(
                    "FireDisc travel range in blocks (Reference Range 20)")
            .defineInRange("range", 20.0, 1.0, 96.0);

    public static final ModConfigSpec.DoubleValue FIREDISC_SPEED =
            BUILDER.comment("FireDisc flight step in blocks per tick").defineInRange("speed", 1.0, 0.1, 6.0);

    public static final ModConfigSpec.DoubleValue FIREDISC_KNOCKBACK = BUILDER.comment(
                    "FireDisc knockback strength along the heading (Reference Knockback 1.5)")
            .defineInRange("knockback", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.IntValue FIREDISC_FIRE_MS =
            BUILDER.comment("FireDisc ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue FIREDISC_HIT_RADIUS =
            BUILDER.comment("FireDisc hit radius in blocks").defineInRange("hitRadius", 1.5, 0.2, 8.0);

    public static final ModConfigSpec.IntValue FIREDISC_REVERT_MS = BUILDER.comment(
                    "FireDisc cut-block regen delay in milliseconds (Reference 10000ms)")
            .defineInRange("revertMs", 10000, 0, 360000);

    public static final ModConfigSpec.BooleanValue FIREDISC_CONTROLLABLE = BUILDER.comment(
                    "FireDisc steers toward the gaze mid-flight (Reference Controllable true)")
            .define("controllable", true);

    public static final ModConfigSpec.BooleanValue FIREDISC_REVERT = BUILDER.comment(
                    "FireDisc cut blocks grow back (Reference Revert true)")
            .define("revert", true);

    public static final ModConfigSpec.BooleanValue FIREDISC_DROP = BUILDER.comment(
                    "FireDisc cut blocks drop their item (Reference Drop true)")
            .define("drop", true);

    public static final ModConfigSpec.ConfigValue<String> FIREDISC_RING_PARTICLE =
            BUILDER.comment("FireDisc ring particle id").define("ringParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREDISC_RING_PARTICLE_COUNT =
            BUILDER.comment("FireDisc ring particle count").defineInRange("ringParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireJet settings").push("FireJet");
    }

    public static final ModConfigSpec.IntValue FIREJET_DURATION_MS = BUILDER.comment(
                    "FireJet max ride in milliseconds (Reference Duration 2000ms)")
            .defineInRange("durationMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue FIREJET_COOLDOWN_MS = BUILDER.comment(
                    "FireJet cooldown in milliseconds (Reference Cooldown 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIREJET_SPEED = BUILDER.comment(
                    "FireJet thrust speed in blocks per tick (Reference Speed 0.8)")
            .defineInRange("speed", 0.8, 0.0, 6.0);

    public static final ModConfigSpec.IntValue FIREJET_RESIST_MS = BUILDER.comment(
                    "FireJet launch fire-resistance cover in milliseconds")
            .defineInRange("resistMs", 1500, 0, 60000);

    public static final ModConfigSpec.ConfigValue<String> FIREJET_EXTINGUISH_PARTICLE =
            BUILDER.comment("FireJet extinguish particle id").define("extinguishParticle", "minecraft:cloud");

    public static final ModConfigSpec.IntValue FIREJET_EXTINGUISH_PARTICLE_COUNT =
            BUILDER.comment("FireJet extinguish particle count").defineInRange("extinguishParticleCount", 12, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREJET_TRAIL_PARTICLE =
            BUILDER.comment("FireJet trail particle id").define("trailParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREJET_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireJet trail particle count").defineInRange("trailParticleCount", 4, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireKick settings").push("FireKick");
    }

    public static final ModConfigSpec.IntValue FIREKICK_COOLDOWN_MS = BUILDER.comment(
                    "FireKick cooldown in milliseconds (Reference Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIREKICK_DAMAGE = BUILDER.comment(
                    "FireKick magic damage on hit (Reference Damage 3)")
            .defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIREKICK_SPEED = BUILDER.comment(
                    "FireKick head speed in blocks per tick (Reference Speed 1.2)")
            .defineInRange("speed", 1.2, 0.0, 6.0);

    public static final ModConfigSpec.DoubleValue FIREKICK_RANGE = BUILDER.comment(
                    "FireKick head travel range in blocks (Reference Range 20)")
            .defineInRange("range", 20.0, 1.0, 96.0);

    public static final ModConfigSpec.DoubleValue FIREKICK_PUSH =
            BUILDER.comment("FireKick shove strength (Reference Push 1.0)").defineInRange("push", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue FIREKICK_RADIUS = BUILDER.comment(
                    "FireKick head hit radius in blocks (Reference Radius 1.5)")
            .defineInRange("radius", 1.5, 0.2, 8.0);

    public static final ModConfigSpec.IntValue FIREKICK_FIRE_MS =
            BUILDER.comment("FireKick ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 60000);

    public static final ModConfigSpec.ConfigValue<String> FIREKICK_TRAIL_PARTICLE =
            BUILDER.comment("FireKick trail particle id").define("trailParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREKICK_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireKick trail particle count").defineInRange("trailParticleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireManipulation settings").push("FireManipulation");
    }

    public static final ModConfigSpec.IntValue FIREMANIPULATION_COOLDOWN_MS = BUILDER.comment(
                    "FireManipulation stream cooldown in milliseconds (Reference StreamCooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 120000);

    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_RANGE =
            BUILDER.comment("FireManipulation stream range in blocks").defineInRange("streamRange", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_DAMAGE = BUILDER.comment(
                    "FireManipulation stream magic damage on hit")
            .defineInRange("streamDamage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_SPEED = BUILDER.comment(
                    "FireManipulation stream advance per tick in blocks")
            .defineInRange("streamSpeed", 1.5, 0.1, 6.0);

    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_AURA_DAMAGE = BUILDER.comment(
                    "FireManipulation gathering-orb magic damage on touch")
            .defineInRange("auraDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_AURA_RADIUS = BUILDER.comment(
                    "FireManipulation gathering-orb hurt radius in blocks")
            .defineInRange("auraRadius", 2.0, 0.5, 8.0);

    public static final ModConfigSpec.DoubleValue FIREMANIPULATION_STREAM_HIT_RADIUS = BUILDER.comment(
                    "FireManipulation stream hit radius in blocks")
            .defineInRange("streamHitRadius", 2.0, 0.2, 6.0);

    public static final ModConfigSpec.IntValue FIREMANIPULATION_AURA_FIRE_MS = BUILDER.comment(
                    "FireManipulation gathering-orb ignite duration in milliseconds")
            .defineInRange("auraFireMs", 2000, 0, 30000);

    public static final ModConfigSpec.IntValue FIREMANIPULATION_STREAM_FIRE_MS = BUILDER.comment(
                    "FireManipulation stream ignite duration in milliseconds")
            .defineInRange("streamFireMs", 3000, 0, 30000);

    public static final ModConfigSpec.ConfigValue<String> FIREMANIPULATION_ORB_PARTICLE =
            BUILDER.comment("FireManipulation orb particle id").define("orbParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIREMANIPULATION_ORB_PARTICLE_COUNT =
            BUILDER.comment("FireManipulation orb particle count").defineInRange("orbParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREMANIPULATION_FLAME_PARTICLE =
            BUILDER.comment("FireManipulation flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREMANIPULATION_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireManipulation flame particle count").defineInRange("flameParticleCount", 8, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireShield settings").push("FireShield");
    }

    public static final ModConfigSpec.IntValue FIRESHIELD_COOLDOWN_MS = BUILDER.comment(
                    "FireShield cooldown in milliseconds (Reference Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIRESHIELD_RADIUS =
            BUILDER.comment("FireShield shield radius in blocks").defineInRange("radius", 3.0, 1.0, 8.0);

    public static final ModConfigSpec.IntValue FIRESHIELD_FIRE_MS =
            BUILDER.comment("FireShield ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 30000);

    public static final ModConfigSpec.DoubleValue FIRESHIELD_PUSH =
            BUILDER.comment("FireShield knockback strength").defineInRange("push", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.ConfigValue<String> FIRESHIELD_SHELL_PARTICLE =
            BUILDER.comment("FireShield shell particle id").define("shellParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIRESHIELD_SHELL_PARTICLE_COUNT =
            BUILDER.comment("FireShield shell particle count").defineInRange("shellParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRESHIELD_FLAME_PARTICLE =
            BUILDER.comment("FireShield flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIRESHIELD_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireShield flame particle count").defineInRange("flameParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRESHIELD_DEFLECT_PARTICLE =
            BUILDER.comment("FireShield deflect particle id").define("deflectParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIRESHIELD_DEFLECT_PARTICLE_COUNT =
            BUILDER.comment("FireShield deflect particle count").defineInRange("deflectParticleCount", 4, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireShots settings").push("FireShots");
    }

    public static final ModConfigSpec.IntValue FIRESHOTS_COOLDOWN_MS = BUILDER.comment(
                    "FireShots cooldown in milliseconds (Reference Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 120000);

    public static final ModConfigSpec.IntValue FIRESHOTS_STOCK =
            BUILDER.comment("FireShots fireball stock per gather").defineInRange("stock", 4, 1, 16);

    public static final ModConfigSpec.DoubleValue FIRESHOTS_RANGE =
            BUILDER.comment("FireShots travel range in blocks").defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue FIRESHOTS_DAMAGE =
            BUILDER.comment("FireShots magic damage on hit").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue FIRESHOTS_FIRE_MS =
            BUILDER.comment("FireShots ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 30000);

    public static final ModConfigSpec.DoubleValue FIRESHOTS_HIT_RADIUS =
            BUILDER.comment("FireShots hit radius in blocks").defineInRange("hitRadius", 1.5, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue FIRESHOTS_SPEED =
            BUILDER.comment("FireShots travel per tick in blocks").defineInRange("speed", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.ConfigValue<String> FIRESHOTS_TRAIL_PARTICLE =
            BUILDER.comment("FireShots trail particle id").define("trailParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIRESHOTS_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireShots trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRESHOTS_STOCK_PARTICLE =
            BUILDER.comment("FireShots stock particle id").define("stockParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIRESHOTS_STOCK_PARTICLE_COUNT =
            BUILDER.comment("FireShots stock particle base count").defineInRange("stockParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRESHOTS_FLAME_PARTICLE =
            BUILDER.comment("FireShots flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIRESHOTS_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireShots flame particle count").defineInRange("flameParticleCount", 4, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireSki settings").push("FireSki");
    }

    public static final ModConfigSpec.IntValue FIRESKI_COOLDOWN_MS = BUILDER.comment(
                    "FireSki cooldown in milliseconds (Reference Cooldown 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.IntValue FIRESKI_DURATION_MS = BUILDER.comment(
                    "FireSki max ride in milliseconds (Reference Duration 12000ms)")
            .defineInRange("durationMs", 12000, 0, 360000);

    public static final ModConfigSpec.IntValue FIRESKI_ARM_MS = BUILDER.comment(
                    "FireSki grounded click arm window in milliseconds (Reference 600ms)")
            .defineInRange("armMs", 600, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIRESKI_SPEED = BUILDER.comment(
                    "FireSki descent speed in blocks per tick (Reference Speed 1.2)")
            .defineInRange("speed", 1.2, 0.0, 6.0);

    public static final ModConfigSpec.DoubleValue FIRESKI_DAMAGE =
            BUILDER.comment("FireSki magic damage to buzzed entities").defineInRange("damage", 1.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIRESKI_HIT_RADIUS =
            BUILDER.comment("FireSki buzz radius under the rider in blocks").defineInRange("hitRadius", 2.0, 0.2, 8.0);

    public static final ModConfigSpec.IntValue FIRESKI_FIRE_MS = BUILDER.comment(
                    "FireSki ignite duration in milliseconds (Reference FireTicks 3)")
            .defineInRange("fireMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue FIRESKI_MIN_HEIGHT = BUILDER.comment(
                    "FireSki minimum air height to start a ride in blocks (Reference MinHeight 1)")
            .defineInRange("minHeight", 1.0, 0.0, 8.0);

    public static final ModConfigSpec.BooleanValue FIRESKI_IGNITE = BUILDER.comment(
                    "FireSki torches buzzed entities (Reference Ignite true)")
            .define("ignite", true);

    public static final ModConfigSpec.ConfigValue<String> FIRESKI_SMOKE_PARTICLE =
            BUILDER.comment("FireSki smoke particle id").define("smokeParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIRESKI_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("FireSki smoke particle count").defineInRange("smokeParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIRESKI_RING_PARTICLE =
            BUILDER.comment("FireSki ring particle id").define("ringParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIRESKI_RING_PARTICLE_COUNT =
            BUILDER.comment("FireSki ring particle count").defineInRange("ringParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireSpin settings").push("FireSpin");
    }

    public static final ModConfigSpec.IntValue FIRESPIN_COOLDOWN_MS = BUILDER.comment(
                    "FireSpin cooldown in milliseconds (Reference Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIRESPIN_DAMAGE = BUILDER.comment(
                    "FireSpin magic damage on hit (Reference Damage 3)")
            .defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIRESPIN_SPEED = BUILDER.comment(
                    "FireSpin head speed in blocks per tick (Reference Speed 1.0)")
            .defineInRange("speed", 1.0, 0.0, 6.0);

    public static final ModConfigSpec.DoubleValue FIRESPIN_RANGE = BUILDER.comment(
                    "FireSpin head travel range in blocks (Reference Range 16)")
            .defineInRange("range", 16.0, 1.0, 96.0);

    public static final ModConfigSpec.DoubleValue FIRESPIN_PUSH =
            BUILDER.comment("FireSpin shove strength (Reference Push 1.5)").defineInRange("push", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue FIRESPIN_RADIUS = BUILDER.comment(
                    "FireSpin head hit radius in blocks (Reference Radius 1.5)")
            .defineInRange("radius", 1.5, 0.2, 8.0);

    public static final ModConfigSpec.IntValue FIRESPIN_FIRE_MS =
            BUILDER.comment("FireSpin ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue FIRESPIN_SCORCH_RADIUS = BUILDER.comment(
                    "FireSpin launch ground-scorch radius in blocks")
            .defineInRange("scorchRadius", 6.0, 1.0, 16.0);

    public static final ModConfigSpec.IntValue FIRESPIN_SCORCH_MAX =
            BUILDER.comment("FireSpin launch max ground fires lit").defineInRange("scorchMax", 32, 0, 128);

    public static final ModConfigSpec.IntValue FIRESPIN_SCORCH_TRIES =
            BUILDER.comment("FireSpin launch ground-scorch sampling tries").defineInRange("scorchTries", 110, 1, 1000);

    public static final ModConfigSpec.ConfigValue<String> FIRESPIN_TRAIL_PARTICLE =
            BUILDER.comment("FireSpin trail particle id").define("trailParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIRESPIN_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("FireSpin trail particle count").defineInRange("trailParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireWave settings").push("FireWave");
    }

    public static final ModConfigSpec.IntValue FIREWAVE_COOLDOWN_MS = BUILDER.comment(
                    "FireWave cooldown in milliseconds (Reference Cooldown 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 120000);

    public static final ModConfigSpec.IntValue FIREWAVE_DURATION_MS = BUILDER.comment(
                    "FireWave max duration in milliseconds (Reference Duration 6000ms)")
            .defineInRange("durationMs", 6000, 500, 120000);

    public static final ModConfigSpec.DoubleValue FIREWAVE_RANGE =
            BUILDER.comment("FireWave travel range in blocks").defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue FIREWAVE_SPEED =
            BUILDER.comment("FireWave advance per tick in blocks").defineInRange("speed", 0.8, 0.1, 6.0);

    public static final ModConfigSpec.DoubleValue FIREWAVE_WIDTH =
            BUILDER.comment("FireWave wall half-width in blocks").defineInRange("width", 3.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue FIREWAVE_HEIGHT =
            BUILDER.comment("FireWave wall height in blocks").defineInRange("height", 3.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue FIREWAVE_DAMAGE =
            BUILDER.comment("FireWave magic damage on hit").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue FIREWAVE_FIRE_MS =
            BUILDER.comment("FireWave ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 30000);

    public static final ModConfigSpec.ConfigValue<String> FIREWAVE_WALL_PARTICLE =
            BUILDER.comment("FireWave wall particle id").define("wallParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue FIREWAVE_WALL_PARTICLE_COUNT =
            BUILDER.comment("FireWave wall particle count").defineInRange("wallParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> FIREWAVE_FLAME_PARTICLE =
            BUILDER.comment("FireWave flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREWAVE_FLAME_PARTICLE_COUNT =
            BUILDER.comment("FireWave flame particle count").defineInRange("flameParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FireWheel settings").push("FireWheel");
    }

    public static final ModConfigSpec.IntValue FIREWHEEL_COOLDOWN_MS = BUILDER.comment(
                    "FireWheel cooldown in milliseconds (Reference Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.DoubleValue FIREWHEEL_DAMAGE = BUILDER.comment(
                    "FireWheel magic damage on hit (Reference Damage 4)")
            .defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue FIREWHEEL_SPEED = BUILDER.comment(
                    "FireWheel roll speed in blocks per tick (Reference Speed 1.0)")
            .defineInRange("speed", 1.0, 0.0, 6.0);

    public static final ModConfigSpec.DoubleValue FIREWHEEL_RANGE = BUILDER.comment(
                    "FireWheel travel range in blocks (Reference Range 20)")
            .defineInRange("range", 20.0, 1.0, 96.0);

    public static final ModConfigSpec.DoubleValue FIREWHEEL_HEIGHT = BUILDER.comment(
                    "FireWheel height in blocks, radius is half this (Reference Height 4)")
            .defineInRange("height", 4.0, 1.0, 16.0);

    public static final ModConfigSpec.IntValue FIREWHEEL_FIRE_MS = BUILDER.comment(
                    "FireWheel ignite duration in milliseconds (Reference FireTicks 3)")
            .defineInRange("fireMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue FIREWHEEL_PUSH =
            BUILDER.comment("FireWheel shove strength along the roll").defineInRange("push", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.ConfigValue<String> FIREWHEEL_RING_PARTICLE =
            BUILDER.comment("FireWheel ring particle id").define("ringParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue FIREWHEEL_RING_PARTICLE_COUNT =
            BUILDER.comment("FireWheel ring particle count").defineInRange("ringParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("FlameBreath settings").push("FlameBreath");
    }

    public static final ModConfigSpec.IntValue FLAMEBREATH_COOLDOWN_MS = BUILDER.comment(
                    "FlameBreath cooldown in milliseconds (Reference Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.IntValue FLAMEBREATH_DURATION_MS = BUILDER.comment(
                    "FlameBreath max breath duration in milliseconds (Reference Duration 5000ms)")
            .defineInRange("durationMs", 5000, 500, 60000);

    public static final ModConfigSpec.DoubleValue FLAMEBREATH_RANGE =
            BUILDER.comment("FlameBreath range in blocks").defineInRange("range", 14.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue FLAMEBREATH_DAMAGE =
            BUILDER.comment("FlameBreath magic damage on hit").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue FLAMEBREATH_FIRE_MS =
            BUILDER.comment("FlameBreath ignite duration in milliseconds").defineInRange("fireMs", 3000, 0, 30000);

    public static final ModConfigSpec.DoubleValue FLAMEBREATH_SPEED =
            BUILDER.comment("FlameBreath advance per tick in blocks").defineInRange("speed", 1.0, 0.1, 6.0);

    public static final ModConfigSpec.ConfigValue<String> FLAMEBREATH_BREATH_PARTICLE =
            BUILDER.comment("FlameBreath breath particle id").define("breathParticle", "minecraft:flame");

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("HeatControl settings").push("HeatControl");
    }

    public static final ModConfigSpec.IntValue HEATCONTROL_COOLDOWN_MS = BUILDER.comment(
                    "HeatControl cooldown in milliseconds (Reference Cooldown 2000ms)")
            .defineInRange("cooldownMs", 2000, 0, 120000);

    public static final ModConfigSpec.IntValue HEATCONTROL_COOK_INTERVAL_MS = BUILDER.comment(
                    "HeatControl cook interval in milliseconds (Reference CookMs 1500)")
            .defineInRange("cookIntervalMs", 1500, 50, 360000);

    public static final ModConfigSpec.IntValue HEATCONTROL_EXTINGUISH_COOLDOWN_MS = BUILDER.comment(
                    "HeatControl extinguish cooldown in milliseconds (Reference ExtinguishCooldown 2000ms)")
            .defineInRange("extinguishCooldownMs", 2000, 0, 120000);

    public static final ModConfigSpec.IntValue HEATCONTROL_MAGMA_DELAY_MS = BUILDER.comment(
                    "HeatControl magma-to-stone delay in milliseconds (Reference 1000ms)")
            .defineInRange("magmaDelayMs", 1000, 0, 360000);

    public static final ModConfigSpec.IntValue HEATCONTROL_SOLIDIFY_STEP_MS = BUILDER.comment(
                    "HeatControl solidify ring step in milliseconds (Reference ring step 50ms)")
            .defineInRange("solidifyStepMs", 50, 50, 1000);

    public static final ModConfigSpec.IntValue HEATCONTROL_MELT_REVERT_MS = BUILDER.comment(
                    "HeatControl melt-water revert delay in milliseconds (Reference 5min)")
            .defineInRange("meltRevertMs", 300000, 0, 3600000);

    public static final ModConfigSpec.IntValue HEATCONTROL_SOLIDIFY_REVERT_MS = BUILDER.comment(
                    "HeatControl solidified-stone revert delay in milliseconds (Reference SolidifyRevert 600000ms)")
            .defineInRange("solidifyRevertMs", 600000, 0, 3600000);

    public static final ModConfigSpec.DoubleValue HEATCONTROL_EXTINGUISH_RADIUS = BUILDER.comment(
                    "HeatControl extinguish radius in blocks")
            .defineInRange("extinguishRadius", 6.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue HEATCONTROL_MELT_RANGE =
            BUILDER.comment("HeatControl melt gaze range in blocks").defineInRange("meltRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue HEATCONTROL_MELT_RADIUS =
            BUILDER.comment("HeatControl melt pulse radius in blocks").defineInRange("meltRadius", 3.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue HEATCONTROL_SOLIDIFY_RANGE = BUILDER.comment(
                    "HeatControl solidify gaze range in blocks")
            .defineInRange("solidifyRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue HEATCONTROL_SOLIDIFY_MAX_RADIUS = BUILDER.comment(
                    "HeatControl solidify max ring radius in blocks")
            .defineInRange("solidifyMaxRadius", 5.0, 1.0, 12.0);

    public static final ModConfigSpec.BooleanValue HEATCONTROL_SOLIDIFY_REVERT = BUILDER.comment(
                    "HeatControl solidified stone reverts to lava after the revert delay")
            .define("solidifyRevert", true);

    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_COOK_PARTICLE =
            BUILDER.comment("HeatControl cook particle id").define("cookParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue HEATCONTROL_COOK_PARTICLE_COUNT =
            BUILDER.comment("HeatControl cook particle count").defineInRange("cookParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_EXTINGUISH_PARTICLE =
            BUILDER.comment("HeatControl extinguish particle id").define("extinguishParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue HEATCONTROL_EXTINGUISH_PARTICLE_COUNT =
            BUILDER.comment("HeatControl extinguish particle count").defineInRange("extinguishParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_SOLIDIFY_PARTICLE =
            BUILDER.comment("HeatControl solidify particle id").define("solidifyParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue HEATCONTROL_SOLIDIFY_PARTICLE_COUNT =
            BUILDER.comment("HeatControl solidify particle count").defineInRange("solidifyParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_STONE_PARTICLE =
            BUILDER.comment("HeatControl stone particle id").define("stoneParticle", "minecraft:smoke");

    public static final ModConfigSpec.IntValue HEATCONTROL_STONE_PARTICLE_COUNT =
            BUILDER.comment("HeatControl stone particle count").defineInRange("stoneParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> HEATCONTROL_FLAME_PARTICLE =
            BUILDER.comment("HeatControl flame particle id").define("flameParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue HEATCONTROL_FLAME_PARTICLE_COUNT =
            BUILDER.comment("HeatControl flame particle count").defineInRange("flameParticleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Illumination settings").push("Illumination");
    }

    public static final ModConfigSpec.IntValue ILLUMINATION_COOLDOWN_MS = BUILDER.comment(
                    "Illumination cooldown in milliseconds (Reference Cooldown 1000ms)")
            .defineInRange("cooldownMs", 1000, 0, 360000);

    public static final ModConfigSpec.IntValue ILLUMINATION_THRESHOLD = BUILDER.comment(
                    "Illumination max darkness to start, as raw brightness (Reference Threshold 7)")
            .defineInRange("threshold", 7, 0, 15);

    public static final ModConfigSpec.IntValue ILLUMINATION_LIGHT_LEVEL = BUILDER.comment(
                    "Illumination carried glow level (Reference Light 14)")
            .defineInRange("lightLevel", 14, 0, 15);

    public static final ModConfigSpec.ConfigValue<String> ILLUMINATION_WISP_PARTICLE =
            BUILDER.comment("Illumination wisp particle id").define("wispParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue ILLUMINATION_WISP_PARTICLE_COUNT =
            BUILDER.comment("Illumination wisp particle count").defineInRange("wispParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Jets settings").push("Jets");
    }

    public static final ModConfigSpec.IntValue JETS_COOLDOWN_MIN_MS =
            BUILDER.comment("Jets minimum cooldown in ms").defineInRange("cooldownMinMs", 4000, 0, 60000);

    public static final ModConfigSpec.IntValue JETS_COOLDOWN_MAX_MS =
            BUILDER.comment("Jets maximum cooldown in ms").defineInRange("cooldownMaxMs", 12000, 0, 120000);

    public static final ModConfigSpec.IntValue JETS_DURATION_MS =
            BUILDER.comment("Jets duration in ms").defineInRange("durationMs", 20000, 0, 120000);

    public static final ModConfigSpec.DoubleValue JETS_FLY_SPEED =
            BUILDER.comment("Jets glide speed").defineInRange("flySpeed", 0.65, 0.05, 4.0);

    public static final ModConfigSpec.DoubleValue JETS_HOVER_SPEED =
            BUILDER.comment("Jets creative-flight speed while hovering").defineInRange("hoverSpeed", 0.065, 0.005, 2.0);

    public static final ModConfigSpec.DoubleValue JETS_SPEED_THRESHOLD = BUILDER.comment(
                    "Speed threshold for auto-glide on activation")
            .defineInRange("speedThreshold", 2.4, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue JETS_DAMAGE_THRESHOLD = BUILDER.comment(
                    "Jets drops you when your health falls below its start minus this")
            .defineInRange("damageThreshold", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue JETS_MAX_HEIGHT =
            BUILDER.comment("Jets ceiling check below-height (-1 disables)").defineInRange("maxHeight", -1, -1, 128);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Lightning settings").push("Lightning");
    }

    public static final ModConfigSpec.IntValue LIGHTNING_COOLDOWN_MS = BUILDER.comment(
                    "Lightning cooldown in milliseconds (reference Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.IntValue LIGHTNING_CHARGE_MS = BUILDER.comment(
                    "Lightning sneak charge in milliseconds (reference Charge 2000ms)")
            .defineInRange("chargeMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue LIGHTNING_LINGER_MS = BUILDER.comment(
                    "Lightning bolt linger after the strike in milliseconds")
            .defineInRange("lingerMs", 400, 0, 10000);

    public static final ModConfigSpec.DoubleValue LIGHTNING_DAMAGE =
            BUILDER.comment("Lightning strike damage").defineInRange("damage", 6.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue LIGHTNING_RANGE =
            BUILDER.comment("Lightning targeting range in blocks").defineInRange("range", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue LIGHTNING_CHAIN_RANGE =
            BUILDER.comment("Lightning victim chain range in blocks").defineInRange("chainRange", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.IntValue LIGHTNING_MAX_CHAINS =
            BUILDER.comment("Lightning max chain jumps").defineInRange("maxChains", 3, 0, 12);

    public static final ModConfigSpec.DoubleValue LIGHTNING_CHAIN_CHANCE =
            BUILDER.comment("Lightning chain continuation chance").defineInRange("chainChance", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LIGHTNING_STUN_CHANCE =
            BUILDER.comment("Lightning stun chance per victim").defineInRange("stunChance", 0.4, 0.0, 1.0);

    public static final ModConfigSpec.IntValue LIGHTNING_STUN_MS =
            BUILDER.comment("Lightning stun duration in milliseconds").defineInRange("stunMs", 2000, 0, 60000);

    public static final ModConfigSpec.IntValue LIGHTNING_POINT_GENERATION = BUILDER.comment(
                    "Lightning bolt midpoint-displacement generations")
            .defineInRange("pointGeneration", 5, 1, 8);

    public static final ModConfigSpec.DoubleValue LIGHTNING_SUB_ARC_CHANCE =
            BUILDER.comment("Lightning sub-arc chance per bolt point").defineInRange("subArcChance", 0.12, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LIGHTNING_MAX_ARC_ANGLE_DEG =
            BUILDER.comment("Lightning sub-arc spread in degrees").defineInRange("maxArcAngleDeg", 25.0, 0.0, 90.0);

    public static final ModConfigSpec.IntValue LIGHTNING_WATER_ARCS =
            BUILDER.comment("Lightning water fan-out arc count").defineInRange("waterArcs", 4, 0, 16);

    public static final ModConfigSpec.DoubleValue LIGHTNING_WATER_ARC_RANGE =
            BUILDER.comment("Lightning water fan-out range in blocks").defineInRange("waterArcRange", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.IntValue LIGHTNING_MAX_COPPER_ARCS =
            BUILDER.comment("Lightning max copper/rod walk jumps").defineInRange("maxCopperArcs", 5, 0, 16);

    public static final ModConfigSpec.DoubleValue LIGHTNING_CONDUCTIVITY_RANGE = BUILDER.comment(
                    "Lightning copper/rod walk range in blocks")
            .defineInRange("conductivityRange", 6.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue LIGHTNING_WET_HIT_RADIUS = BUILDER.comment(
                    "Lightning strike hit radius in water in blocks")
            .defineInRange("wetHitRadius", 4.0, 0.5, 12.0);

    public static final ModConfigSpec.DoubleValue LIGHTNING_DRY_HIT_RADIUS = BUILDER.comment(
                    "Lightning strike hit radius on land in blocks")
            .defineInRange("dryHitRadius", 2.5, 0.5, 12.0);

    public static final ModConfigSpec.IntValue LIGHTNING_IGNITE_MS =
            BUILDER.comment("Lightning ignite duration in milliseconds").defineInRange("igniteMs", 2000, 0, 20000);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_LINGER_PARTICLE =
            BUILDER.comment("Lightning linger particle id").define("lingerParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNING_LINGER_PARTICLE_COUNT =
            BUILDER.comment("Lightning linger particle base count").defineInRange("lingerParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_GATHER_PARTICLE =
            BUILDER.comment("Lightning gather particle id").define("gatherParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNING_GATHER_PARTICLE_COUNT =
            BUILDER.comment("Lightning gather particle count").defineInRange("gatherParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_CHARGE_PARTICLE =
            BUILDER.comment("Lightning charge particle id").define("chargeParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNING_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("Lightning charge particle count").defineInRange("chargeParticleCount", 4, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_ROD_PARTICLE =
            BUILDER.comment("Lightning rod particle id").define("rodParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNING_ROD_PARTICLE_COUNT =
            BUILDER.comment("Lightning rod particle count").defineInRange("rodParticleCount", 6, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNING_BEAM_PARTICLE =
            BUILDER.comment("Lightning beam particle id").define("beamParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNING_BEAM_PARTICLE_COUNT =
            BUILDER.comment("Lightning beam particle count").defineInRange("beamParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("LightningBurst settings").push("LightningBurst");
    }

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_COOLDOWN_MS = BUILDER.comment(
                    "LightningBurst cooldown in milliseconds (reference Cooldown 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_CHARGE_MS = BUILDER.comment(
                    "LightningBurst sneak charge in milliseconds (reference Charge 2000ms)")
            .defineInRange("chargeMs", 2000, 0, 360000);

    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_DAMAGE =
            BUILDER.comment("LightningBurst damage per bolt").defineInRange("damage", 5.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_RADIUS =
            BUILDER.comment("LightningBurst bolt travel radius in blocks").defineInRange("radius", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_YAW_STEP =
            BUILDER.comment("LightningBurst sphere lattice yaw step in degrees").defineInRange("yawStep", 55, 5, 180);

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_PITCH_STEP = BUILDER.comment(
                    "LightningBurst sphere lattice pitch step in degrees")
            .defineInRange("pitchStep", 55, 5, 180);

    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_STEP_LENGTH =
            BUILDER.comment("LightningBurst advance per step in blocks").defineInRange("stepLength", 0.2, 0.05, 1.0);

    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_GAP_LENGTH =
            BUILDER.comment("LightningBurst advance per tick in blocks").defineInRange("gapLength", 1.0, 0.2, 4.0);

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_JITTER_DEGREES = BUILDER.comment(
                    "LightningBurst per-tick yaw/pitch jitter in degrees")
            .defineInRange("jitterDegrees", 20, 0, 60);

    public static final ModConfigSpec.DoubleValue LIGHTNINGBURST_HIT_RADIUS =
            BUILDER.comment("LightningBurst hit radius in blocks").defineInRange("hitRadius", 2.0, 0.2, 6.0);

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_IGNITE_MS =
            BUILDER.comment("LightningBurst ignite duration in milliseconds").defineInRange("igniteMs", 1000, 0, 20000);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNINGBURST_TRAIL_PARTICLE =
            BUILDER.comment("LightningBurst trail particle id").define("trailParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("LightningBurst trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNINGBURST_CHARGE_PARTICLE =
            BUILDER.comment("LightningBurst charge particle id").define("chargeParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("LightningBurst charge particle count").defineInRange("chargeParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> LIGHTNINGBURST_CORE_PARTICLE =
            BUILDER.comment("LightningBurst core particle id").define("coreParticle", "minecraft:electric_spark");

    public static final ModConfigSpec.IntValue LIGHTNINGBURST_CORE_PARTICLE_COUNT =
            BUILDER.comment("LightningBurst core particle count").defineInRange("coreParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("passives settings").push("passives");
    }

    public static final ModConfigSpec.IntValue PASSIVE_FIRE_GLOW_INTERVAL_MS = BUILDER.comment(
                    "Fire glow check interval in milliseconds")
            .defineInRange("fireGlowIntervalMs", 1000, 50, 10000);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("WallOfFire settings").push("WallOfFire");
    }

    public static final ModConfigSpec.IntValue WALLOFFIRE_COOLDOWN_MS = BUILDER.comment(
                    "WallOfFire cooldown in milliseconds (Reference Cooldown 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.IntValue WALLOFFIRE_DURATION_MS = BUILDER.comment(
                    "WallOfFire lifetime in milliseconds (Reference Duration 8000ms)")
            .defineInRange("durationMs", 8000, 0, 360000);

    public static final ModConfigSpec.IntValue WALLOFFIRE_DAMAGE_INTERVAL_MS = BUILDER.comment(
                    "WallOfFire sear interval in milliseconds (Reference DamageInterval 1000ms)")
            .defineInRange("damageIntervalMs", 1000, 0, 360000);

    public static final ModConfigSpec.IntValue WALLOFFIRE_FX_INTERVAL_MS = BUILDER.comment(
                    "WallOfFire flame redraw interval in milliseconds (Reference FxInterval 100ms)")
            .defineInRange("fxIntervalMs", 100, 0, 10000);

    public static final ModConfigSpec.IntValue WALLOFFIRE_FOOT_REVERT_MS = BUILDER.comment(
                    "WallOfFire base-flame revert delay in milliseconds (Reference 3000ms)")
            .defineInRange("footRevertMs", 3000, 0, 360000);

    public static final ModConfigSpec.DoubleValue WALLOFFIRE_RANGE = BUILDER.comment(
                    "WallOfFire aim reach cap in blocks (Reference Range 10)")
            .defineInRange("range", 10.0, 1.0, 96.0);

    public static final ModConfigSpec.DoubleValue WALLOFFIRE_AIM_RANGE = BUILDER.comment(
                    "WallOfFire gaze aim distance in blocks (source raycast cap 6)")
            .defineInRange("aimRange", 6.0, 1.0, 32.0);

    public static final ModConfigSpec.DoubleValue WALLOFFIRE_WIDTH = BUILDER.comment(
                    "WallOfFire half-width in blocks (Reference Width 6)")
            .defineInRange("width", 6.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue WALLOFFIRE_HEIGHT =
            BUILDER.comment("WallOfFire height in blocks (Reference Height 6)").defineInRange("height", 6.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue WALLOFFIRE_DAMAGE = BUILDER.comment(
                    "WallOfFire magic damage per sear (Reference Damage 3)")
            .defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue WALLOFFIRE_FIRE_MS = BUILDER.comment(
                    "WallOfFire ignite duration in milliseconds (Reference FireTicks 3)")
            .defineInRange("fireMs", 3000, 0, 60000);

    public static final ModConfigSpec.DoubleValue WALLOFFIRE_THICKNESS =
            BUILDER.comment("WallOfFire sear half-thickness in blocks").defineInRange("thickness", 1.5, 0.2, 8.0);

    public static final ModConfigSpec.ConfigValue<String> WALLOFFIRE_WALL_PARTICLE =
            BUILDER.comment("WallOfFire wall particle id").define("wallParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue WALLOFFIRE_WALL_PARTICLE_COUNT =
            BUILDER.comment("WallOfFire wall particle count").defineInRange("wallParticleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Air abilities").push("air");
    }

    static {
        BUILDER.comment("AirBlast settings").push("AirBlast");
    }

    public static final ModConfigSpec.IntValue AIRBLAST_COOLDOWN_MS = BUILDER.comment(
                    "AirBlast cooldown in milliseconds (Reference Cooldown 2000ms)")
            .defineInRange("cooldownMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRBLAST_ORIGIN_MS = BUILDER.comment(
                    "AirBlast selected-origin memory in milliseconds (Reference OriginMemory 10s)")
            .defineInRange("originMs", 10000, 1000, 360000);

    public static final ModConfigSpec.IntValue AIRBLAST_MAX_MS =
            BUILDER.comment("AirBlast max flight time in milliseconds").defineInRange("maxMs", 10000, 1000, 360000);

    public static final ModConfigSpec.DoubleValue AIRBLAST_SPEED =
            BUILDER.comment("AirBlast speed (Reference Speed 25)").defineInRange("speed", 25.0, 1.0, 60.0);

    public static final ModConfigSpec.DoubleValue AIRBLAST_RANGE =
            BUILDER.comment("AirBlast range in blocks (Reference Range 20)").defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue AIRBLAST_RADIUS = BUILDER.comment(
                    "AirBlast hit radius in blocks (Reference Radius 2)")
            .defineInRange("radius", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.DoubleValue AIRBLAST_DAMAGE =
            BUILDER.comment("AirBlast magic damage (Reference Damage 3)").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue AIRBLAST_PUSH_SELF = BUILDER.comment(
                    "AirBlast self-launch strength (Reference PushSelf 1.5)")
            .defineInRange("pushSelf", 1.5, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue AIRBLAST_PUSH_OTHERS = BUILDER.comment(
                    "AirBlast knockback to others (Reference PushOthers 3)")
            .defineInRange("pushOthers", 3.0, 0.0, 8.0);

    public static final ModConfigSpec.IntValue AIRBLAST_PARTICLES = BUILDER.comment(
                    "AirBlast particles per tick (Reference Particles 10)")
            .defineInRange("particles", 10, 0, 32);

    public static final ModConfigSpec.DoubleValue AIRBLAST_SELECT_RANGE = BUILDER.comment(
                    "AirBlast origin-select range in blocks (Reference SelectRange 10)")
            .defineInRange("selectRange", 10.0, 2.0, 32.0);

    public static final ModConfigSpec.BooleanValue AIRBLAST_CONTROLLABLE = BUILDER.comment(
                    "AirBlast sneaking mid-flight steers it (Reference Controllable true)")
            .define("controllable", true);

    public static final ModConfigSpec.ConfigValue<String> AIRBLAST_MARKER_PARTICLE =
            BUILDER.comment("AirBlast marker particle id").define("markerParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBLAST_MARKER_PARTICLE_COUNT =
            BUILDER.comment("AirBlast marker particle count").defineInRange("markerParticleCount", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBLAST_TRAIL_PARTICLE =
            BUILDER.comment("AirBlast trail particle id").define("trailParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBLAST_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("AirBlast trail particle count").defineInRange("trailParticleCount", 10, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirBreath settings").push("AirBreath");
    }

    public static final ModConfigSpec.IntValue AIRBREATH_COOLDOWN_MS = BUILDER.comment(
                    "AirBreath cooldown in milliseconds (ref Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRBREATH_DURATION_MS = BUILDER.comment(
                    "AirBreath max breath in milliseconds (ref Duration 3000ms)")
            .defineInRange("durationMs", 3000, 500, 360000);

    public static final ModConfigSpec.DoubleValue AIRBREATH_RANGE =
            BUILDER.comment("AirBreath cone range in blocks (ref Range 12)").defineInRange("range", 12.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue AIRBREATH_KNOCKBACK =
            BUILDER.comment("AirBreath shove strength (ref Knockback 1)").defineInRange("knockback", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue AIRBREATH_PLAYER_DAMAGE = BUILDER.comment(
                    "AirBreath damage to players (ref PlayerDamage 2)")
            .defineInRange("playerDamage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue AIRBREATH_MOB_DAMAGE =
            BUILDER.comment("AirBreath damage to mobs (ref MobDamage 3)").defineInRange("mobDamage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue AIRBREATH_LAUNCH = BUILDER.comment(
                    "AirBreath wall-blast self launch strength (ref Launch 1.5)")
            .defineInRange("launch", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.IntValue AIRBREATH_PARTICLES = BUILDER.comment(
                    "AirBreath particles per beam step (ref Particles 6)")
            .defineInRange("particles", 6, 0, 32);

    public static final ModConfigSpec.DoubleValue AIRBREATH_HIT_RADIUS =
            BUILDER.comment("AirBreath beam hit radius in blocks").defineInRange("hitRadius", 1.5, 0.2, 6.0);

    public static final ModConfigSpec.IntValue AIRBREATH_OXYGEN_DURATION_MS = BUILDER.comment(
                    "AirBreath water-breathing lend duration in milliseconds")
            .defineInRange("oxygenDurationMs", 5000, 0, 120000);

    public static final ModConfigSpec.IntValue AIRBREATH_OXYGEN_AMPLIFIER =
            BUILDER.comment("AirBreath water-breathing amplifier").defineInRange("oxygenAmplifier", 2, 0, 10);

    public static final ModConfigSpec.ConfigValue<String> AIRBREATH_CONE_PARTICLE =
            BUILDER.comment("AirBreath cone particle id").define("coneParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBREATH_CONE_PARTICLE_COUNT =
            BUILDER.comment("AirBreath cone particle count").defineInRange("coneParticleCount", 6, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirBullet settings").push("AirBullet");
    }

    public static final ModConfigSpec.IntValue AIRBULLET_COOLDOWN_MS = BUILDER.comment(
                    "AirBullet cooldown in milliseconds, paid on hit (ref Cooldown 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRBULLET_CHARGE_MS = BUILDER.comment(
                    "AirBullet sneak charge in milliseconds (ref Charge 2000ms)")
            .defineInRange("chargeMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRBULLET_ARMED_MS = BUILDER.comment(
                    "AirBullet loaded hold time in milliseconds (ref Armed 15000ms)")
            .defineInRange("armedMs", 15000, 1000, 360000);

    public static final ModConfigSpec.DoubleValue AIRBULLET_DAMAGE =
            BUILDER.comment("AirBullet magic damage (ref Damage 12)").defineInRange("damage", 12.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue AIRBULLET_RANGE =
            BUILDER.comment("AirBullet travel range in blocks (ref Range 30)").defineInRange("range", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue AIRBULLET_SPEED = BUILDER.comment(
                    "AirBullet speed in blocks per second (ref Speed 40)")
            .defineInRange("speed", 40.0, 1.0, 120.0);

    public static final ModConfigSpec.DoubleValue AIRBULLET_HIT_RADIUS = BUILDER.comment(
                    "AirBullet hit radius in blocks (ref HitRadius 0.8)")
            .defineInRange("hitRadius", 0.8, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue AIRBULLET_KNOCKBACK =
            BUILDER.comment("AirBullet hit knockback strength").defineInRange("knockback", 3.0, 0.0, 8.0);

    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_ARMED_RING_PARTICLE =
            BUILDER.comment("AirBullet armed ring particle id").define("armedRingParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBULLET_ARMED_RING_PARTICLE_COUNT =
            BUILDER.comment("AirBullet armed ring particle count").defineInRange("armedRingParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_ARMED_CORE_PARTICLE =
            BUILDER.comment("AirBullet armed core particle id").define("armedCoreParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBULLET_ARMED_CORE_PARTICLE_COUNT =
            BUILDER.comment("AirBullet armed core particle count").defineInRange("armedCoreParticleCount", 4, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_IMPACT_PARTICLE =
            BUILDER.comment("AirBullet impact particle id").define("impactParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBULLET_IMPACT_PARTICLE_COUNT =
            BUILDER.comment("AirBullet impact particle count").defineInRange("impactParticleCount", 6, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_TRACER_PARTICLE =
            BUILDER.comment("AirBullet tracer particle id").define("tracerParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBULLET_TRACER_PARTICLE_COUNT =
            BUILDER.comment("AirBullet tracer particle count").defineInRange("tracerParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_HIT_PARTICLE =
            BUILDER.comment("AirBullet hit particle id").define("hitParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBULLET_HIT_PARTICLE_COUNT =
            BUILDER.comment("AirBullet hit particle count").defineInRange("hitParticleCount", 8, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_GATHER_PARTICLE =
            BUILDER.comment("AirBullet gather particle id").define("gatherParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBULLET_GATHER_PARTICLE_COUNT =
            BUILDER.comment("AirBullet gather particle count").defineInRange("gatherParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBULLET_CHARGED_PARTICLE =
            BUILDER.comment("AirBullet charged particle id").define("chargedParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBULLET_CHARGED_PARTICLE_COUNT =
            BUILDER.comment("AirBullet charged particle count").defineInRange("chargedParticleCount", 4, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirBurst settings").push("AirBurst");
    }

    public static final ModConfigSpec.IntValue AIRBURST_COOLDOWN_MS = BUILDER.comment(
                    "AirBurst cooldown in milliseconds (Reference Cooldown 2500ms)")
            .defineInRange("cooldownMs", 2500, 0, 360000);

    public static final ModConfigSpec.IntValue AIRBURST_CHARGE_MS = BUILDER.comment(
                    "AirBurst sneak charge in milliseconds (Reference Charge 1500ms)")
            .defineInRange("chargeMs", 1500, 0, 60000);

    public static final ModConfigSpec.DoubleValue AIRBURST_DAMAGE =
            BUILDER.comment("AirBurst magic damage (Reference Damage 2)").defineInRange("damage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue AIRBURST_RADIUS = BUILDER.comment(
                    "AirBurst sphere radius in blocks (Reference Radius 7)")
            .defineInRange("radius", 7.0, 2.0, 16.0);

    public static final ModConfigSpec.DoubleValue AIRBURST_PUSH =
            BUILDER.comment("AirBurst push strength (Reference Push 2.2)").defineInRange("push", 2.2, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue AIRBURST_FALL_THRESHOLD = BUILDER.comment(
                    "AirBurst fall-burst trigger distance in blocks (Reference FallThreshold 8)")
            .defineInRange("fallThreshold", 8.0, 0.0, 32.0);

    public static final ModConfigSpec.DoubleValue AIRBURST_CONE_ANGLE_DEGREES = BUILDER.comment(
                    "AirBurst cone half-angle in degrees (Reference 30-degree cone)")
            .defineInRange("coneAngleDegrees", 30.0, 5.0, 90.0);

    public static final ModConfigSpec.DoubleValue AIRBURST_CONE_RANGE_MULT = BUILDER.comment(
                    "AirBurst cone range multiplier over sphere radius")
            .defineInRange("coneRangeMult", 1.3, 0.5, 3.0);

    public static final ModConfigSpec.DoubleValue AIRBURST_CONE_PUSH_MULT = BUILDER.comment(
                    "AirBurst cone push multiplier over sphere push")
            .defineInRange("conePushMult", 1.2, 0.0, 3.0);

    public static final ModConfigSpec.DoubleValue AIRBURST_LIFT =
            BUILDER.comment("AirBurst upward pop factor on hit").defineInRange("lift", 0.45, 0.0, 2.0);

    public static final ModConfigSpec.ConfigValue<String> AIRBURST_CHARGE_PARTICLE =
            BUILDER.comment("AirBurst charge particle id").define("chargeParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBURST_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("AirBurst charge particle count").defineInRange("chargeParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBURST_RING_PARTICLE =
            BUILDER.comment("AirBurst ring particle id").define("ringParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBURST_RING_PARTICLE_COUNT =
            BUILDER.comment("AirBurst ring particle count").defineInRange("ringParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRBURST_SHELL_PARTICLE =
            BUILDER.comment("AirBurst shell particle id").define("shellParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRBURST_SHELL_PARTICLE_COUNT =
            BUILDER.comment("AirBurst shell particle count").defineInRange("shellParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirFlight settings").push("AirFlight");
    }

    public static final ModConfigSpec.IntValue AIRFLIGHT_COOLDOWN_MS = BUILDER.comment(
                    "AirFlight cooldown in milliseconds (Reference Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SPEED =
            BUILDER.comment("AirFlight base Soar speed (Reference Speed 1)").defineInRange("speed", 1.0, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SOAR_SLOW =
            BUILDER.comment("AirFlight Soar SLOW step (Reference 0.6)").defineInRange("soarSlow", 0.6, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SOAR_NORMAL =
            BUILDER.comment("AirFlight Soar NORMAL step (Reference 1.0)").defineInRange("soarNormal", 1.0, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue AIRFLIGHT_SOAR_FAST =
            BUILDER.comment("AirFlight Soar FAST step (Reference 1.6)").defineInRange("soarFast", 1.6, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue AIRFLIGHT_GLIDE_BOOST =
            BUILDER.comment("AirFlight Glide entry rescue boost when slow").defineInRange("glideBoost", 1.2, 0.0, 4.0);

    public static final ModConfigSpec.DoubleValue AIRFLIGHT_RAM_RADIUS =
            BUILDER.comment("AirFlight Soar ram hit radius in blocks").defineInRange("ramRadius", 1.5, 0.5, 6.0);

    public static final ModConfigSpec.DoubleValue AIRFLIGHT_RAM_THRESHOLD =
            BUILDER.comment("AirFlight Soar ram minimum speed").defineInRange("ramThreshold", 0.5, 0.0, 4.0);

    public static final ModConfigSpec.ConfigValue<String> AIRFLIGHT_TRAIL_PARTICLE =
            BUILDER.comment("AirFlight trail particle id").define("trailParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRFLIGHT_TRAIL_PARTICLE_COUNT = BUILDER.comment(
                    "AirFlight trail base particle count (plus 2 per Soar speed step)")
            .defineInRange("trailParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirJet settings").push("AirJet");
    }

    public static final ModConfigSpec.IntValue AIRJET_DURATION_MS = BUILDER.comment(
                    "AirJet ride duration in milliseconds (Reference Duration 2000ms)")
            .defineInRange("durationMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRJET_COOLDOWN_MS = BUILDER.comment(
                    "AirJet cooldown in milliseconds (Reference Cooldown 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRJET_SPEED =
            BUILDER.comment("AirJet thrust speed (Reference Speed 0.8)").defineInRange("speed", 0.8, 0.1, 4.0);

    public static final ModConfigSpec.ConfigValue<String> AIRJET_EXTINGUISH_PARTICLE =
            BUILDER.comment("AirJet extinguish particle id").define("extinguishParticle", "minecraft:bubble");

    public static final ModConfigSpec.IntValue AIRJET_EXTINGUISH_PARTICLE_COUNT =
            BUILDER.comment("AirJet extinguish particle count").defineInRange("extinguishParticleCount", 12, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRJET_TRAIL_LEFT_PARTICLE =
            BUILDER.comment("AirJet trail left particle id").define("trailLeftParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRJET_TRAIL_LEFT_PARTICLE_COUNT =
            BUILDER.comment("AirJet trail left particle count").defineInRange("trailLeftParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRJET_TRAIL_RIGHT_PARTICLE =
            BUILDER.comment("AirJet trail right particle id").define("trailRightParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRJET_TRAIL_RIGHT_PARTICLE_COUNT =
            BUILDER.comment("AirJet trail right particle count").defineInRange("trailRightParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirPunch settings").push("AirPunch");
    }

    public static final ModConfigSpec.IntValue AIRPUNCH_COOLDOWN_MS = BUILDER.comment(
                    "AirPunch cooldown in milliseconds (Reference Cooldown 1500ms)")
            .defineInRange("cooldownMs", 1500, 0, 360000);

    public static final ModConfigSpec.IntValue AIRPUNCH_THRESHOLD_MS = BUILDER.comment(
                    "AirPunch flurry window in milliseconds (Reference Flurry 800ms)")
            .defineInRange("thresholdMs", 800, 0, 60000);

    public static final ModConfigSpec.IntValue AIRPUNCH_SHOTS =
            BUILDER.comment("AirPunch shots per flurry (Reference Shots 5)").defineInRange("shots", 5, 1, 32);

    public static final ModConfigSpec.DoubleValue AIRPUNCH_RANGE = BUILDER.comment(
                    "AirPunch bolt range in blocks (Reference Range 20)")
            .defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue AIRPUNCH_DAMAGE = BUILDER.comment(
                    "AirPunch magic damage on hit (Reference Damage 2)")
            .defineInRange("damage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue AIRPUNCH_HIT_RADIUS = BUILDER.comment(
                    "AirPunch bolt hit radius in blocks (Reference HitRadius 1)")
            .defineInRange("hitRadius", 1.0, 0.2, 6.0);

    public static final ModConfigSpec.DoubleValue AIRPUNCH_PUSH =
            BUILDER.comment("AirPunch knockback strength on hit").defineInRange("push", 0.8, 0.0, 5.0);

    public static final ModConfigSpec.ConfigValue<String> AIRPUNCH_BOLT_PARTICLE =
            BUILDER.comment("AirPunch bolt particle id").define("boltParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRPUNCH_BOLT_PARTICLE_COUNT =
            BUILDER.comment("AirPunch bolt particle count").defineInRange("boltParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirScooter settings").push("AirScooter");
    }

    public static final ModConfigSpec.DoubleValue AIRSCOOTER_SPEED =
            BUILDER.comment("AirScooter ride speed (Reference Speed 0.675)").defineInRange("speed", 0.675, 0.1, 4.0);

    public static final ModConfigSpec.IntValue AIRSCOOTER_INTERVAL_MS = BUILDER.comment(
                    "AirScooter spin/move-check interval in milliseconds (Reference Interval 100ms)")
            .defineInRange("intervalMs", 100, 50, 5000);

    public static final ModConfigSpec.IntValue AIRSCOOTER_COOLDOWN_MS = BUILDER.comment(
                    "AirScooter cooldown in milliseconds (Reference Cooldown 500ms)")
            .defineInRange("cooldownMs", 500, 0, 360000);

    public static final ModConfigSpec.IntValue AIRSCOOTER_DURATION_MS = BUILDER.comment(
                    "AirScooter max ride in milliseconds, 0 is infinite (Reference Duration 0)")
            .defineInRange("durationMs", 0, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRSCOOTER_MAX_HEIGHT = BUILDER.comment(
                    "AirScooter ground-scan height in blocks (Reference MaxHeight 7)")
            .defineInRange("maxHeight", 7.0, 1.0, 32.0);

    public static final ModConfigSpec.IntValue AIRSCOOTER_CHIME_MS = BUILDER.comment(
                    "AirScooter chime interval in milliseconds (3000ms)")
            .defineInRange("chimeMs", 3000, 0, 360000);

    public static final ModConfigSpec.ConfigValue<String> AIRSCOOTER_SPIN_PARTICLE =
            BUILDER.comment("AirScooter spin particle id").define("spinParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSCOOTER_SPIN_PARTICLE_COUNT =
            BUILDER.comment("AirScooter spin particle count").defineInRange("spinParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRSCOOTER_SPIN_MIRROR_PARTICLE =
            BUILDER.comment("AirScooter spin mirror particle id").define("spinMirrorParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSCOOTER_SPIN_MIRROR_PARTICLE_COUNT =
            BUILDER.comment("AirScooter spin mirror particle count").defineInRange("spinMirrorParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRSCOOTER_RING_PARTICLE =
            BUILDER.comment("AirScooter ring particle id").define("ringParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSCOOTER_RING_PARTICLE_COUNT =
            BUILDER.comment("AirScooter ring particle count").defineInRange("ringParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirShield settings").push("AirShield");
    }

    public static final ModConfigSpec.IntValue AIRSHIELD_COOLDOWN_MS = BUILDER.comment(
                    "AirShield cooldown in milliseconds (Reference Cooldown 7000ms)")
            .defineInRange("cooldownMs", 7000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRSHIELD_DURATION_MS = BUILDER.comment(
                    "AirShield duration in milliseconds for cooldown math (Reference Duration 6500ms)")
            .defineInRange("durationMs", 6500, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRSHIELD_MAX_RADIUS = BUILDER.comment(
                    "AirShield max radius in blocks (Reference MaxRadius 4)")
            .defineInRange("maxRadius", 4.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue AIRSHIELD_INITIAL_RADIUS = BUILDER.comment(
                    "AirShield starting radius in blocks (Reference InitialRadius 1)")
            .defineInRange("initialRadius", 1.0, 0.5, 8.0);

    public static final ModConfigSpec.DoubleValue AIRSHIELD_SPEED =
            BUILDER.comment("AirShield ring spin speed (Reference Speed 10)").defineInRange("speed", 10.0, 1.0, 30.0);

    public static final ModConfigSpec.DoubleValue AIRSHIELD_PUSH_FACTOR = BUILDER.comment(
                    "AirShield entity shove strength (Reference Push 1.5)")
            .defineInRange("pushFactor", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.IntValue AIRSHIELD_STREAMS =
            BUILDER.comment("AirShield wind streams (Reference Streams 5)").defineInRange("streams", 5, 1, 16);

    public static final ModConfigSpec.IntValue AIRSHIELD_PARTICLES = BUILDER.comment(
                    "AirShield particles per ring point (Reference Particles 5)")
            .defineInRange("particles", 5, 0, 32);

    public static final ModConfigSpec.DoubleValue AIRSHIELD_GROWTH =
            BUILDER.comment("AirShield radius growth per tick in blocks").defineInRange("growth", 0.3, 0.05, 2.0);

    public static final ModConfigSpec.IntValue AIRSHIELD_PUSH_ANGLE_DEGREES = BUILDER.comment(
                    "AirShield swirl angle in degrees (Reference 50-degree swirl)")
            .defineInRange("pushAngleDegrees", 50, 0, 90);

    public static final ModConfigSpec.ConfigValue<String> AIRSHIELD_RING_PARTICLE =
            BUILDER.comment("AirShield ring particle id").define("ringParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSHIELD_RING_PARTICLE_COUNT =
            BUILDER.comment("AirShield ring particle count").defineInRange("ringParticleCount", 5, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirSlam settings").push("AirSlam");
    }

    public static final ModConfigSpec.IntValue AIRSLAM_COOLDOWN_MS = BUILDER.comment(
                    "AirSlam cooldown in milliseconds (Reference Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRSLAM_POWER =
            BUILDER.comment("AirSlam spike push strength (Reference Power 2)").defineInRange("power", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.DoubleValue AIRSLAM_RANGE = BUILDER.comment(
                    "AirSlam target acquire range in blocks (Reference Range 15)")
            .defineInRange("range", 15.0, 2.0, 64.0);

    public static final ModConfigSpec.IntValue AIRSLAM_SPIKE_MS =
            BUILDER.comment("AirSlam spike delay in milliseconds (~50ms)").defineInRange("spikeMs", 50, 0, 5000);

    public static final ModConfigSpec.IntValue AIRSLAM_LIFETIME_MS =
            BUILDER.comment("AirSlam lifetime in milliseconds (~400ms)").defineInRange("lifetimeMs", 400, 50, 10000);

    public static final ModConfigSpec.DoubleValue AIRSLAM_LIFT =
            BUILDER.comment("AirSlam pop-up velocity on click").defineInRange("lift", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.ConfigValue<String> AIRSLAM_TARGET_PARTICLE =
            BUILDER.comment("AirSlam target particle id").define("targetParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSLAM_TARGET_PARTICLE_COUNT =
            BUILDER.comment("AirSlam target particle count").defineInRange("targetParticleCount", 6, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirSpout settings").push("AirSpout");
    }

    public static final ModConfigSpec.IntValue AIRSPOUT_COOLDOWN_MS = BUILDER.comment(
                    "AirSpout cooldown in milliseconds (Reference Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRSPOUT_DURATION_MS = BUILDER.comment(
                    "AirSpout max ride in milliseconds, 0 is infinite (Reference Duration 0)")
            .defineInRange("durationMs", 0, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRSPOUT_HEIGHT = BUILDER.comment(
                    "AirSpout column cap in blocks (Reference Height 16)")
            .defineInRange("height", 16.0, 4.0, 32.0);

    public static final ModConfigSpec.IntValue AIRSPOUT_INTERVAL_MS = BUILDER.comment(
                    "AirSpout spiral animation interval in milliseconds (Reference Interval 100ms)")
            .defineInRange("intervalMs", 100, 50, 5000);

    public static final ModConfigSpec.DoubleValue AIRSPOUT_THRESHOLD =
            BUILDER.comment("AirSpout start/ride height tolerance in blocks").defineInRange("threshold", 2.0, 0.0, 8.0);

    public static final ModConfigSpec.ConfigValue<String> AIRSPOUT_COLUMN_PARTICLE =
            BUILDER.comment("AirSpout column particle id").define("columnParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSPOUT_COLUMN_PARTICLE_COUNT =
            BUILDER.comment("AirSpout column particle count").defineInRange("columnParticleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirStream settings").push("AirStream");
    }

    public static final ModConfigSpec.IntValue AIRSTREAM_COOLDOWN_MS = BUILDER.comment(
                    "AirStream cooldown in milliseconds (ref Cooldown 6000ms)")
            .defineInRange("cooldownMs", 6000, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRSTREAM_SPEED = BUILDER.comment(
                    "AirStream head advance in blocks per tick (ref Speed 0.5)")
            .defineInRange("speed", 0.5, 0.05, 4.0);

    public static final ModConfigSpec.DoubleValue AIRSTREAM_RANGE =
            BUILDER.comment("AirStream max length in blocks (ref Range 25)").defineInRange("range", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue AIRSTREAM_CARRY_HEIGHT = BUILDER.comment(
                    "AirStream max lift above the hand in blocks (ref CarryHeight 5)")
            .defineInRange("carryHeight", 5.0, 1.0, 16.0);

    public static final ModConfigSpec.DoubleValue AIRSTREAM_CATCH_RADIUS =
            BUILDER.comment("AirStream catch radius in blocks").defineInRange("catchRadius", 2.0, 0.5, 6.0);

    public static final ModConfigSpec.ConfigValue<String> AIRSTREAM_STREAM_PARTICLE =
            BUILDER.comment("AirStream stream particle id").define("streamParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSTREAM_STREAM_PARTICLE_COUNT =
            BUILDER.comment("AirStream stream particle count").defineInRange("streamParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRSTREAM_HEAD_PARTICLE =
            BUILDER.comment("AirStream head particle id").define("headParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSTREAM_HEAD_PARTICLE_COUNT =
            BUILDER.comment("AirStream head particle count").defineInRange("headParticleCount", 4, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirSuction settings").push("AirSuction");
    }

    public static final ModConfigSpec.IntValue AIRSUCTION_COOLDOWN_MS = BUILDER.comment(
                    "AirSuction cooldown in milliseconds (ref Cooldown 2000ms)")
            .defineInRange("cooldownMs", 2000, 0, 360000);

    public static final ModConfigSpec.DoubleValue AIRSUCTION_RANGE =
            BUILDER.comment("AirSuction reach in blocks (ref Range 20)").defineInRange("range", 20.0, 2.0, 64.0);

    public static final ModConfigSpec.DoubleValue AIRSUCTION_RADIUS = BUILDER.comment(
                    "AirSuction close-range grab radius in blocks (ref Radius 3)")
            .defineInRange("radius", 3.0, 0.5, 12.0);

    public static final ModConfigSpec.DoubleValue AIRSUCTION_PUSH =
            BUILDER.comment("AirSuction pull strength (ref Push 1.2)").defineInRange("push", 1.2, 0.0, 5.0);

    public static final ModConfigSpec.IntValue AIRSUCTION_CONE_DEGREES = BUILDER.comment(
                    "AirSuction frontal cone half-angle in degrees (ref 35)")
            .defineInRange("coneDegrees", 35, 0, 180);

    public static final ModConfigSpec.ConfigValue<String> AIRSUCTION_MAIN_PARTICLE =
            BUILDER.comment("AirSuction main particle id").define("mainParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSUCTION_MAIN_PARTICLE_COUNT =
            BUILDER.comment("AirSuction main particle count").defineInRange("mainParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRSUCTION_STREAM_PARTICLE =
            BUILDER.comment("AirSuction stream particle id").define("streamParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSUCTION_STREAM_PARTICLE_COUNT =
            BUILDER.comment("AirSuction stream particle count").defineInRange("streamParticleCount", 2, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("AirSwipe settings").push("AirSwipe");
    }

    public static final ModConfigSpec.IntValue AIRSWIPE_COOLDOWN_MS = BUILDER.comment(
                    "AirSwipe cooldown in milliseconds (Reference Cooldown 2000ms)")
            .defineInRange("cooldownMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue AIRSWIPE_MAX_CHARGE_MS = BUILDER.comment(
                    "AirSwipe full charge in milliseconds (Reference Charge 2000ms)")
            .defineInRange("maxChargeMs", 2000, 0, 60000);

    public static final ModConfigSpec.DoubleValue AIRSWIPE_DAMAGE =
            BUILDER.comment("AirSwipe magic damage (Reference Damage 3)").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue AIRSWIPE_PUSH =
            BUILDER.comment("AirSwipe knockback strength (Reference Push 1)").defineInRange("push", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue AIRSWIPE_SPEED =
            BUILDER.comment("AirSwipe speed (Reference Speed 18)").defineInRange("speed", 18.0, 1.0, 60.0);

    public static final ModConfigSpec.DoubleValue AIRSWIPE_RANGE =
            BUILDER.comment("AirSwipe range in blocks (Reference Range 16)").defineInRange("range", 16.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue AIRSWIPE_RADIUS = BUILDER.comment(
                    "AirSwipe hit radius in blocks (Reference Radius 1.5)")
            .defineInRange("radius", 1.5, 0.5, 6.0);

    public static final ModConfigSpec.IntValue AIRSWIPE_ARC = BUILDER.comment(
                    "AirSwipe fan half-arc in degrees (Reference Arc 20)")
            .defineInRange("arc", 20, 5, 90);

    public static final ModConfigSpec.IntValue AIRSWIPE_ARC_STEP = BUILDER.comment(
                    "AirSwipe fan step in degrees (Reference ArcStep 5)")
            .defineInRange("arcStep", 5, 1, 20);

    public static final ModConfigSpec.DoubleValue AIRSWIPE_CHARGE_FACTOR = BUILDER.comment(
                    "AirSwipe full-charge damage/push multiplier (Reference ChargeFactor 2)")
            .defineInRange("chargeFactor", 2.0, 1.0, 5.0);

    public static final ModConfigSpec.IntValue AIRSWIPE_PARTICLES = BUILDER.comment(
                    "AirSwipe particles per stream tick (Reference Particles 6)")
            .defineInRange("particles", 6, 0, 32);

    public static final ModConfigSpec.ConfigValue<String> AIRSWIPE_CHARGE_PARTICLE =
            BUILDER.comment("AirSwipe charge particle id").define("chargeParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSWIPE_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("AirSwipe charge particle count").defineInRange("chargeParticleCount", 6, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> AIRSWIPE_STREAM_PARTICLE =
            BUILDER.comment("AirSwipe stream particle id").define("streamParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue AIRSWIPE_STREAM_PARTICLE_COUNT =
            BUILDER.comment("AirSwipe stream particle count").defineInRange("streamParticleCount", 6, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Meditate settings").push("Meditate");
    }

    public static final ModConfigSpec.IntValue MEDITATE_WARMUP_MS = BUILDER.comment(
                    "Meditate focus warmup in milliseconds (ref Warmup 3000ms)")
            .defineInRange("warmupMs", 3000, 0, 360000);

    public static final ModConfigSpec.IntValue MEDITATE_COOLDOWN_MS = BUILDER.comment(
                    "Meditate cooldown in milliseconds, paid on payoff (ref Cooldown 10000ms)")
            .defineInRange("cooldownMs", 10000, 0, 360000);

    public static final ModConfigSpec.IntValue MEDITATE_BOOST_MS = BUILDER.comment(
                    "Meditate blessing duration in milliseconds (ref Boost 60000ms)")
            .defineInRange("boostMs", 60000, 1000, 360000);

    public static final ModConfigSpec.IntValue MEDITATE_PARTICLES =
            BUILDER.comment("Meditate particles per tick (ref Particles 6)").defineInRange("particles", 6, 0, 32);

    public static final ModConfigSpec.IntValue MEDITATE_ABSORPTION_AMPLIFIER = BUILDER.comment(
                    "Meditate absorption blessing level (ref amp 1)")
            .defineInRange("absorptionAmplifier", 1, 1, 5);

    public static final ModConfigSpec.IntValue MEDITATE_SPEED_AMPLIFIER =
            BUILDER.comment("Meditate speed blessing level (ref amp 1)").defineInRange("speedAmplifier", 1, 1, 5);

    public static final ModConfigSpec.IntValue MEDITATE_JUMP_AMPLIFIER =
            BUILDER.comment("Meditate jump blessing level (ref amp 1)").defineInRange("jumpAmplifier", 1, 1, 5);

    public static final ModConfigSpec.ConfigValue<String> MEDITATE_BLESSING_PARTICLE =
            BUILDER.comment("Meditate blessing particle id").define("blessingParticle", "minecraft:witch");

    public static final ModConfigSpec.IntValue MEDITATE_BLESSING_PARTICLE_COUNT =
            BUILDER.comment("Meditate blessing particle count").defineInRange("blessingParticleCount", 6, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> MEDITATE_FOCUS_PARTICLE =
            BUILDER.comment("Meditate focus particle id").define("focusParticle", "minecraft:enchant");

    public static final ModConfigSpec.IntValue MEDITATE_FOCUS_PARTICLE_COUNT =
            BUILDER.comment("Meditate focus particle count").defineInRange("focusParticleCount", 6, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("passives settings").push("passives");
    }

    public static final ModConfigSpec.IntValue PASSIVE_AIR_AGILITY_SPEED_AMP =
            BUILDER.comment("Air agility speed amplifier").defineInRange("airAgilitySpeedAmp", 1, 0, 5);

    public static final ModConfigSpec.IntValue PASSIVE_AIR_AGILITY_JUMP_AMP =
            BUILDER.comment("Air agility jump amplifier").defineInRange("airAgilityJumpAmp", 2, 0, 5);

    public static final ModConfigSpec.IntValue PASSIVE_AGILITY_DURATION_MS = BUILDER.comment(
                    "Agility refresh duration in milliseconds")
            .defineInRange("agilityDurationMs", 500, 100, 5000);

    public static final ModConfigSpec.DoubleValue PASSIVE_SATURATION_FACTOR =
            BUILDER.comment("Air hunger exhaustion factor").defineInRange("saturationFactor", 0.3, 0.0, 1.0);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("SonicBlast settings").push("SonicBlast");
    }

    public static final ModConfigSpec.IntValue SONICBLAST_COOLDOWN_MS = BUILDER.comment(
                    "SonicBlast cooldown in milliseconds (ref Cooldown 3000ms)")
            .defineInRange("cooldownMs", 3000, 0, 360000);

    public static final ModConfigSpec.IntValue SONICBLAST_WARMUP_MS = BUILDER.comment(
                    "SonicBlast scream charge in milliseconds (ref Warmup 1500ms)")
            .defineInRange("warmupMs", 1500, 0, 360000);

    public static final ModConfigSpec.DoubleValue SONICBLAST_DAMAGE =
            BUILDER.comment("SonicBlast magic damage (ref Damage 4)").defineInRange("damage", 4.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue SONICBLAST_RANGE =
            BUILDER.comment("SonicBlast wave range in blocks (ref Range 20)").defineInRange("range", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue SONICBLAST_HIT_RADIUS = BUILDER.comment(
                    "SonicBlast wave hit radius in blocks (ref HitRadius 1.5)")
            .defineInRange("hitRadius", 1.5, 0.2, 6.0);

    public static final ModConfigSpec.IntValue SONICBLAST_NAUSEA_MS = BUILDER.comment(
                    "SonicBlast nausea duration in milliseconds (ref Nausea 100)")
            .defineInRange("nauseaMs", 5000, 0, 120000);

    public static final ModConfigSpec.IntValue SONICBLAST_BLIND_MS = BUILDER.comment(
                    "SonicBlast blindness duration in milliseconds (ref Blind 60)")
            .defineInRange("blindMs", 3000, 0, 120000);

    public static final ModConfigSpec.IntValue SONICBLAST_NAUSEA_AMPLIFIER =
            BUILDER.comment("SonicBlast nausea amplifier").defineInRange("nauseaAmplifier", 1, 0, 10);

    public static final ModConfigSpec.IntValue SONICBLAST_BLIND_AMPLIFIER =
            BUILDER.comment("SonicBlast blindness amplifier").defineInRange("blindAmplifier", 1, 0, 10);

    public static final ModConfigSpec.DoubleValue SONICBLAST_KNOCKBACK =
            BUILDER.comment("SonicBlast wave knockback strength").defineInRange("knockback", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.ConfigValue<String> SONICBLAST_CHARGE_PARTICLE =
            BUILDER.comment("SonicBlast charge particle id").define("chargeParticle", "minecraft:note");

    public static final ModConfigSpec.IntValue SONICBLAST_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("SonicBlast charge particle count").defineInRange("chargeParticleCount", 5, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> SONICBLAST_RING_PARTICLE =
            BUILDER.comment("SonicBlast ring particle id").define("ringParticle", "minecraft:note");

    public static final ModConfigSpec.IntValue SONICBLAST_RING_PARTICLE_COUNT =
            BUILDER.comment("SonicBlast ring particle count").defineInRange("ringParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Suffocate settings").push("Suffocate");
    }

    public static final ModConfigSpec.IntValue SUFFOCATE_COOLDOWN_MS = BUILDER.comment(
                    "Suffocate cooldown in milliseconds (ref Cooldown 2000ms)")
            .defineInRange("cooldownMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue SUFFOCATE_CHARGE_MS = BUILDER.comment(
                    "Suffocate charge-up in milliseconds (ref Charge 2000ms)")
            .defineInRange("chargeMs", 2000, 0, 360000);

    public static final ModConfigSpec.DoubleValue SUFFOCATE_RANGE =
            BUILDER.comment("Suffocate grip range in blocks (ref Range 30)").defineInRange("range", 30.0, 2.0, 64.0);

    public static final ModConfigSpec.DoubleValue SUFFOCATE_RADIUS = BUILDER.comment(
                    "Suffocate spiral visual radius in blocks (ref Radius 2)")
            .defineInRange("radius", 2.0, 0.5, 8.0);

    public static final ModConfigSpec.DoubleValue SUFFOCATE_DAMAGE =
            BUILDER.comment("Suffocate magic damage per tick (ref Damage 2)").defineInRange("damage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue SUFFOCATE_DAMAGE_DELAY_MS = BUILDER.comment(
                    "Suffocate first damage delay in milliseconds (ref DamageDelay 2000ms)")
            .defineInRange("damageDelayMs", 2000, 0, 360000);

    public static final ModConfigSpec.IntValue SUFFOCATE_DAMAGE_REPEAT_MS = BUILDER.comment(
                    "Suffocate damage repeat in milliseconds (ref DamageRepeat 1000ms)")
            .defineInRange("damageRepeatMs", 1000, 50, 360000);

    public static final ModConfigSpec.IntValue SUFFOCATE_SLOW_AMPLIFIER =
            BUILDER.comment("Suffocate slowness amplifier (ref SlowAmp 1)").defineInRange("slowAmplifier", 1, 0, 10);

    public static final ModConfigSpec.IntValue SUFFOCATE_SLOW_REPEAT_MS = BUILDER.comment(
                    "Suffocate slow reapply in milliseconds (ref SlowRepeat 1000ms)")
            .defineInRange("slowRepeatMs", 1000, 50, 360000);

    public static final ModConfigSpec.IntValue SUFFOCATE_SLOW_DELAY_MS = BUILDER.comment(
                    "Suffocate first slow delay in milliseconds (ref SlowDelay 500ms)")
            .defineInRange("slowDelayMs", 500, 0, 360000);

    public static final ModConfigSpec.IntValue SUFFOCATE_BLIND_AMPLIFIER =
            BUILDER.comment("Suffocate blindness amplifier (ref BlindAmp 0)").defineInRange("blindAmplifier", 0, 0, 10);

    public static final ModConfigSpec.IntValue SUFFOCATE_BLIND_REPEAT_MS = BUILDER.comment(
                    "Suffocate blindness reapply in milliseconds (ref BlindRepeat 2000ms)")
            .defineInRange("blindRepeatMs", 2000, 50, 360000);

    public static final ModConfigSpec.IntValue SUFFOCATE_BLIND_DELAY_MS = BUILDER.comment(
                    "Suffocate first blindness delay in milliseconds (ref BlindDelay 1000ms)")
            .defineInRange("blindDelayMs", 1000, 0, 360000);

    public static final ModConfigSpec.DoubleValue SUFFOCATE_AIM_RADIUS = BUILDER.comment(
                    "Suffocate gaze-aim tolerance radius in blocks (ref AimRadius 2.5)")
            .defineInRange("aimRadius", 2.5, 0.5, 8.0);

    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_CHARGE_PARTICLE =
            BUILDER.comment("Suffocate charge particle id").define("chargeParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue SUFFOCATE_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("Suffocate charge particle count").defineInRange("chargeParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_SPIRAL_PARTICLE =
            BUILDER.comment("Suffocate spiral particle id").define("spiralParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue SUFFOCATE_SPIRAL_PARTICLE_COUNT =
            BUILDER.comment("Suffocate spiral particle count").defineInRange("spiralParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_SPIRAL_MIRROR_PARTICLE = BUILDER.comment(
                    "Suffocate spiral mirror particle id")
            .define("spiralMirrorParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue SUFFOCATE_SPIRAL_MIRROR_PARTICLE_COUNT = BUILDER.comment(
                    "Suffocate spiral mirror particle count")
            .defineInRange("spiralMirrorParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> SUFFOCATE_SPIRAL_UPRIGHT_PARTICLE = BUILDER.comment(
                    "Suffocate spiral upright particle id")
            .define("spiralUprightParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue SUFFOCATE_SPIRAL_UPRIGHT_PARTICLE_COUNT = BUILDER.comment(
                    "Suffocate spiral upright particle count")
            .defineInRange("spiralUprightParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Tornado settings").push("Tornado");
    }

    public static final ModConfigSpec.IntValue TORNADO_COOLDOWN_MS = BUILDER.comment(
                    "Tornado cooldown in milliseconds (ref Cooldown 5000ms)")
            .defineInRange("cooldownMs", 5000, 0, 360000);

    public static final ModConfigSpec.IntValue TORNADO_DURATION_MS = BUILDER.comment(
                    "Tornado max duration in milliseconds (ref Duration 10000ms)")
            .defineInRange("durationMs", 10000, 0, 360000);

    public static final ModConfigSpec.DoubleValue TORNADO_MAX_HEIGHT = BUILDER.comment(
                    "Tornado funnel height in blocks (ref Height 15)")
            .defineInRange("maxHeight", 15.0, 2.0, 32.0);

    public static final ModConfigSpec.DoubleValue TORNADO_RADIUS =
            BUILDER.comment("Tornado funnel radius in blocks (ref Radius 5)").defineInRange("radius", 5.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue TORNADO_RANGE = BUILDER.comment(
                    "Tornado gaze-target range in blocks (ref Range 25)")
            .defineInRange("range", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue TORNADO_PLAYER_PUSH = BUILDER.comment(
                    "Tornado lift factor for players (ref PlayerPush 1)")
            .defineInRange("playerPush", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue TORNADO_NPC_PUSH = BUILDER.comment(
                    "Tornado lift factor for non-players (ref NpcPush 1)")
            .defineInRange("npcPush", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue TORNADO_SUCTION = BUILDER.comment(
                    "Tornado outer-band suction strength (ref Suction 1)")
            .defineInRange("suction", 1.0, 0.0, 5.0);

    public static final ModConfigSpec.DoubleValue TORNADO_SPEED =
            BUILDER.comment("Tornado swirl speed (ref Speed 10)").defineInRange("speed", 10.0, 0.5, 40.0);

    public static final ModConfigSpec.IntValue TORNADO_PARTICLES =
            BUILDER.comment("Tornado particles per ring step (ref Particles 8)").defineInRange("particles", 8, 0, 32);

    public static final ModConfigSpec.ConfigValue<String> TORNADO_FUNNEL_PARTICLE =
            BUILDER.comment("Tornado funnel particle id").define("funnelParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue TORNADO_FUNNEL_PARTICLE_COUNT =
            BUILDER.comment("Tornado funnel particle count").defineInRange("funnelParticleCount", 8, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Zephyr settings").push("Zephyr");
    }

    public static final ModConfigSpec.IntValue ZEPHYR_COOLDOWN_MS = BUILDER.comment(
                    "Zephyr cooldown in milliseconds (ref Cooldown 4000ms)")
            .defineInRange("cooldownMs", 4000, 0, 360000);

    public static final ModConfigSpec.DoubleValue ZEPHYR_RADIUS = BUILDER.comment(
                    "Zephyr gentle-current radius in blocks (ref Radius 6)")
            .defineInRange("radius", 6.0, 1.0, 16.0);

    public static final ModConfigSpec.IntValue ZEPHYR_SLOW_DURATION_MS = BUILDER.comment(
                    "Zephyr slow-falling duration in milliseconds")
            .defineInRange("slowDurationMs", 3500, 500, 120000);

    public static final ModConfigSpec.IntValue ZEPHYR_SLOW_AMPLIFIER =
            BUILDER.comment("Zephyr slow-falling amplifier").defineInRange("slowAmplifier", 2, 0, 10);

    public static final ModConfigSpec.IntValue ZEPHYR_REFRESH_THRESHOLD_MS = BUILDER.comment(
                    "Zephyr slow-falling refresh threshold in milliseconds")
            .defineInRange("refreshThresholdMs", 1500, 0, 60000);

    public static final ModConfigSpec.ConfigValue<String> ZEPHYR_DRIFT_PARTICLE =
            BUILDER.comment("Zephyr drift particle id").define("driftParticle", "minecraft:cloud");

    public static final ModConfigSpec.IntValue ZEPHYR_DRIFT_PARTICLE_COUNT =
            BUILDER.comment("Zephyr drift particle count").defineInRange("driftParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ZEPHYR_RING_PARTICLE =
            BUILDER.comment("Zephyr ring particle id").define("ringParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue ZEPHYR_RING_PARTICLE_COUNT =
            BUILDER.comment("Zephyr ring particle count").defineInRange("ringParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("Avatar abilities").push("avatar");
    }

    static {
        BUILDER.comment("AvatarState settings").push("AvatarState");
    }

    public static final ModConfigSpec.DoubleValue AVATARSTATE_DAMAGE_FACTOR =
            BUILDER.comment("AvatarState damage multiplier").defineInRange("damageFactor", 2.0, 1.0, 4.0);

    public static final ModConfigSpec.DoubleValue AVATARSTATE_RANGE_FACTOR =
            BUILDER.comment("AvatarState range multiplier").defineInRange("rangeFactor", 1.5, 1.0, 3.0);

    public static final ModConfigSpec.IntValue AVATARSTATE_EFFECT_DURATION_MS =
            BUILDER.comment("AvatarState buff tick duration").defineInRange("effectDurationMs", 3500, 500, 30000);

    public static final ModConfigSpec.IntValue AVATARSTATE_REGEN_AMP =
            BUILDER.comment("AvatarState regeneration amplifier").defineInRange("regenAmp", 1, 0, 4);

    public static final ModConfigSpec.IntValue AVATARSTATE_RESIST_AMP =
            BUILDER.comment("AvatarState resistance amplifier").defineInRange("resistAmp", 0, 0, 4);

    public static final ModConfigSpec.IntValue AVATARSTATE_SPEED_AMP =
            BUILDER.comment("AvatarState speed amplifier").defineInRange("speedAmp", 0, 0, 4);

    public static final ModConfigSpec.IntValue AVATARSTATE_COOLDOWN_MAX_MS =
            BUILDER.comment("AvatarState max cooldown in ms").defineInRange("cooldownMaxMs", 60000, 0, 300000);

    public static final ModConfigSpec.IntValue AVATARSTATE_COOLDOWN_MIN_MS =
            BUILDER.comment("AvatarState min cooldown in ms").defineInRange("cooldownMinMs", 3000, 0, 60000);

    public static final ModConfigSpec.IntValue AVATARSTATE_COOLDOWN_DIVISOR =
            BUILDER.comment("AvatarState cooldown divisor of time served").defineInRange("cooldownDivisor", 4, 1, 10);

    public static final ModConfigSpec.ConfigValue<String> AVATARSTATE_MAIN_PARTICLE =
            BUILDER.comment("AvatarState main particle id").define("mainParticle", "minecraft:end_rod");

    public static final ModConfigSpec.IntValue AVATARSTATE_MAIN_PARTICLE_COUNT =
            BUILDER.comment("AvatarState main particle count").defineInRange("mainParticleCount", 8, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("ElementSphere settings").push("ElementSphere");
    }

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_COOLDOWN_MS =
            BUILDER.comment("ElementSphere cooldown in milliseconds").defineInRange("cooldownMs", 12000, 0, 360000);

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_DURATION_MS =
            BUILDER.comment("ElementSphere duration in milliseconds").defineInRange("durationMs", 60000, 1000, 360000);

    public static final ModConfigSpec.DoubleValue ELEMENTSPHERE_MAX_HEIGHT =
            BUILDER.comment("ElementSphere flight ceiling in blocks").defineInRange("maxHeight", 6.0, 1.0, 24.0);

    public static final ModConfigSpec.DoubleValue ELEMENTSPHERE_FLY_SPEED =
            BUILDER.comment("ElementSphere flight speed").defineInRange("flySpeed", 0.7, 0.1, 3.0);

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_DISMISS_WINDOW_MS = BUILDER.comment(
                    "ElementSphere double-sneak dismiss window in milliseconds")
            .defineInRange("dismissWindowMs", 600, 0, 5000);

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_AIR_USES =
            BUILDER.comment("ElementSphere air uses").defineInRange("airUses", 5, 1, 20);

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_FIRE_USES =
            BUILDER.comment("ElementSphere fire uses").defineInRange("fireUses", 5, 1, 20);

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_WATER_USES =
            BUILDER.comment("ElementSphere water uses").defineInRange("waterUses", 5, 1, 20);

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_EARTH_USES =
            BUILDER.comment("ElementSphere earth uses").defineInRange("earthUses", 3, 1, 20);

    public static final ModConfigSpec.DoubleValue ELEMENTSPHERE_PUSH_RADIUS =
            BUILDER.comment("ElementSphere shove radius in blocks").defineInRange("pushRadius", 2.5, 0.5, 8.0);

    public static final ModConfigSpec.ConfigValue<String> ELEMENTSPHERE_AIR_PARTICLE =
            BUILDER.comment("ElementSphere air particle id").define("airParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_AIR_PARTICLE_COUNT =
            BUILDER.comment("ElementSphere air particle count").defineInRange("airParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ELEMENTSPHERE_FIRE_PARTICLE =
            BUILDER.comment("ElementSphere fire particle id").define("fireParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_FIRE_PARTICLE_COUNT =
            BUILDER.comment("ElementSphere fire particle count").defineInRange("fireParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ELEMENTSPHERE_BUBBLE_PARTICLE =
            BUILDER.comment("ElementSphere bubble particle id").define("bubbleParticle", "minecraft:bubble");

    public static final ModConfigSpec.IntValue ELEMENTSPHERE_BUBBLE_PARTICLE_COUNT =
            BUILDER.comment("ElementSphere bubble particle count").defineInRange("bubbleParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("esAir settings").push("esAir");
    }

    public static final ModConfigSpec.IntValue ESAIR_COOLDOWN_MS =
            BUILDER.comment("ESAir cooldown in milliseconds").defineInRange("cooldownMs", 1500, 0, 60000);

    public static final ModConfigSpec.DoubleValue ESAIR_RANGE =
            BUILDER.comment("ESAir range in blocks").defineInRange("range", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ESAIR_DAMAGE =
            BUILDER.comment("ESAir magic damage").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ESAIR_KNOCKBACK =
            BUILDER.comment("ESAir knockback strength").defineInRange("knockback", 1.5, 0.0, 5.0);

    public static final ModConfigSpec.IntValue ESAIR_SPEED =
            BUILDER.comment("ESAir steps per tick").defineInRange("speed", 3, 1, 8);

    public static final ModConfigSpec.ConfigValue<String> ESAIR_TRAIL_PARTICLE =
            BUILDER.comment("ESAir trail particle id").define("trailParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue ESAIR_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESAir trail particle count").defineInRange("trailParticleCount", 14, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("esEarth settings").push("esEarth");
    }

    public static final ModConfigSpec.IntValue ESEARTH_COOLDOWN_MS =
            BUILDER.comment("ESEarth cooldown in milliseconds").defineInRange("cooldownMs", 4000, 0, 60000);

    public static final ModConfigSpec.DoubleValue ESEARTH_DAMAGE =
            BUILDER.comment("ESEarth magic damage").defineInRange("damage", 5.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ESEARTH_CRATER =
            BUILDER.comment("ESEarth crater radius in blocks").defineInRange("crater", 3, 0, 8);

    public static final ModConfigSpec.IntValue ESEARTH_REVERT_MS =
            BUILDER.comment("ESEarth crater revert in ms").defineInRange("revertMs", 5000, 0, 30000);

    public static final ModConfigSpec.DoubleValue ESEARTH_SPEED =
            BUILDER.comment("ESEarth boulder speed").defineInRange("speed", 3.0, 0.5, 6.0);

    public static final ModConfigSpec.ConfigValue<String> ESEARTH_TRAIL_PARTICLE =
            BUILDER.comment("ESEarth trail particle id").define("trailParticle", "minecraft:large_smoke");

    public static final ModConfigSpec.IntValue ESEARTH_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESEarth trail particle count").defineInRange("trailParticleCount", 6, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESEARTH_BLAST_PARTICLE =
            BUILDER.comment("ESEarth blast particle id").define("blastParticle", "minecraft:explosion");

    public static final ModConfigSpec.IntValue ESEARTH_BLAST_PARTICLE_COUNT =
            BUILDER.comment("ESEarth blast particle count").defineInRange("blastParticleCount", 5, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESEARTH_BLAST_SMOKE_PARTICLE =
            BUILDER.comment("ESEarth blast smoke particle id").define("blastSmokeParticle", "minecraft:large_smoke");

    public static final ModConfigSpec.IntValue ESEARTH_BLAST_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("ESEarth blast smoke particle count").defineInRange("blastSmokeParticleCount", 15, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("esFire settings").push("esFire");
    }

    public static final ModConfigSpec.IntValue ESFIRE_COOLDOWN_MS =
            BUILDER.comment("ESFire cooldown in milliseconds").defineInRange("cooldownMs", 1500, 0, 60000);

    public static final ModConfigSpec.DoubleValue ESFIRE_RANGE =
            BUILDER.comment("ESFire range in blocks").defineInRange("range", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ESFIRE_DAMAGE =
            BUILDER.comment("ESFire magic damage").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ESFIRE_BURN_MS =
            BUILDER.comment("ESFire burn duration in milliseconds").defineInRange("burnMs", 3000, 0, 60000);

    public static final ModConfigSpec.IntValue ESFIRE_SPEED =
            BUILDER.comment("ESFire steps per tick").defineInRange("speed", 3, 1, 8);

    public static final ModConfigSpec.BooleanValue ESFIRE_CONTROLLABLE =
            BUILDER.comment("ESFire steers with the caster gaze").define("controllable", true);

    public static final ModConfigSpec.ConfigValue<String> ESFIRE_TRAIL_PARTICLE =
            BUILDER.comment("ESFire trail particle id").define("trailParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue ESFIRE_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESFire trail particle count").defineInRange("trailParticleCount", 8, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESFIRE_SMOKE_PARTICLE =
            BUILDER.comment("ESFire smoke particle id").define("smokeParticle", "minecraft:large_smoke");

    public static final ModConfigSpec.IntValue ESFIRE_SMOKE_PARTICLE_COUNT =
            BUILDER.comment("ESFire smoke particle count").defineInRange("smokeParticleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("esStream settings").push("esStream");
    }

    public static final ModConfigSpec.IntValue ESSTREAM_COOLDOWN_MS =
            BUILDER.comment("ESStream cooldown in milliseconds").defineInRange("cooldownMs", 8000, 0, 120000);

    public static final ModConfigSpec.DoubleValue ESSTREAM_RANGE =
            BUILDER.comment("ESStream range in blocks").defineInRange("range", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.DoubleValue ESSTREAM_DAMAGE =
            BUILDER.comment("ESStream magic damage").defineInRange("damage", 6.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue ESSTREAM_KNOCKBACK =
            BUILDER.comment("ESStream knockback strength").defineInRange("knockback", 2.0, 0.0, 5.0);

    public static final ModConfigSpec.IntValue ESSTREAM_REQUIRED_USES =
            BUILDER.comment("ESStream uses required per element").defineInRange("requiredUses", 1, 1, 8);

    public static final ModConfigSpec.BooleanValue ESSTREAM_END_ABILITY =
            BUILDER.comment("ESStream ends the sphere on cast").define("endAbility", true);

    public static final ModConfigSpec.IntValue ESSTREAM_CRATER =
            BUILDER.comment("ESStream crater radius in blocks").defineInRange("crater", 3, 0, 8);

    public static final ModConfigSpec.IntValue ESSTREAM_REVERT_MS =
            BUILDER.comment("ESStream crater revert in ms").defineInRange("revertMs", 8000, 0, 30000);

    public static final ModConfigSpec.DoubleValue ESSTREAM_SPEED =
            BUILDER.comment("ESStream travel speed").defineInRange("speed", 1.2, 0.2, 4.0);

    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_TRAIL_PARTICLE =
            BUILDER.comment("ESStream trail particle id").define("trailParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue ESSTREAM_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESStream trail particle count").defineInRange("trailParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_GUST_PARTICLE =
            BUILDER.comment("ESStream gust particle id").define("gustParticle", "minecraft:small_gust");

    public static final ModConfigSpec.IntValue ESSTREAM_GUST_PARTICLE_COUNT =
            BUILDER.comment("ESStream gust particle count").defineInRange("gustParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_IMPACT_PARTICLE =
            BUILDER.comment("ESStream impact particle id").define("impactParticle", "minecraft:flame");

    public static final ModConfigSpec.IntValue ESSTREAM_IMPACT_PARTICLE_COUNT =
            BUILDER.comment("ESStream impact particle count").defineInRange("impactParticleCount", 20, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_IMPACT_SMOKE_PARTICLE =
            BUILDER.comment("ESStream impact smoke particle id").define("impactSmokeParticle", "minecraft:large_smoke");

    public static final ModConfigSpec.IntValue ESSTREAM_IMPACT_SMOKE_PARTICLE_COUNT = BUILDER.comment(
                    "ESStream impact smoke particle count")
            .defineInRange("impactSmokeParticleCount", 20, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_SPARK_PARTICLE =
            BUILDER.comment("ESStream spark particle id").define("sparkParticle", "minecraft:firework");

    public static final ModConfigSpec.IntValue ESSTREAM_SPARK_PARTICLE_COUNT =
            BUILDER.comment("ESStream spark particle count").defineInRange("sparkParticleCount", 20, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> ESSTREAM_BLAST_PARTICLE =
            BUILDER.comment("ESStream blast particle id").define("blastParticle", "minecraft:explosion");

    public static final ModConfigSpec.IntValue ESSTREAM_BLAST_PARTICLE_COUNT =
            BUILDER.comment("ESStream blast particle count").defineInRange("blastParticleCount", 5, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("esWater settings").push("esWater");
    }

    public static final ModConfigSpec.IntValue ESWATER_COOLDOWN_MS =
            BUILDER.comment("ESWater cooldown in milliseconds").defineInRange("cooldownMs", 1500, 0, 60000);

    public static final ModConfigSpec.DoubleValue ESWATER_RANGE =
            BUILDER.comment("ESWater range in blocks").defineInRange("range", 25.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue ESWATER_DAMAGE =
            BUILDER.comment("ESWater magic damage").defineInRange("damage", 3.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue ESWATER_SPEED =
            BUILDER.comment("ESWater steps per tick").defineInRange("speed", 3, 1, 8);

    public static final ModConfigSpec.IntValue ESWATER_TRAIL_REVERT_MS =
            BUILDER.comment("ESWater trail revert in milliseconds").defineInRange("trailRevertMs", 150, 0, 5000);

    public static final ModConfigSpec.ConfigValue<String> ESWATER_TRAIL_PARTICLE =
            BUILDER.comment("ESWater trail particle id").define("trailParticle", "minecraft:bubble");

    public static final ModConfigSpec.IntValue ESWATER_TRAIL_PARTICLE_COUNT =
            BUILDER.comment("ESWater trail particle count").defineInRange("trailParticleCount", 6, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("SpiritBeam settings").push("SpiritBeam");
    }

    public static final ModConfigSpec.IntValue SPIRITBEAM_DURATION_MS =
            BUILDER.comment("SpiritBeam duration in milliseconds").defineInRange("durationMs", 5000, 500, 60000);

    public static final ModConfigSpec.IntValue SPIRITBEAM_COOLDOWN_MS =
            BUILDER.comment("SpiritBeam cooldown in milliseconds").defineInRange("cooldownMs", 8000, 0, 120000);

    public static final ModConfigSpec.DoubleValue SPIRITBEAM_DAMAGE =
            BUILDER.comment("SpiritBeam magic damage").defineInRange("damage", 6.0, 0.0, 40.0);

    public static final ModConfigSpec.DoubleValue SPIRITBEAM_RANGE =
            BUILDER.comment("SpiritBeam range in blocks").defineInRange("range", 30.0, 4.0, 96.0);

    public static final ModConfigSpec.IntValue SPIRITBEAM_BLOCK_RADIUS =
            BUILDER.comment("SpiritBeam crater radius in blocks").defineInRange("blockRadius", 3, 0, 8);

    public static final ModConfigSpec.IntValue SPIRITBEAM_BLOCK_REVERT_MS =
            BUILDER.comment("SpiritBeam crater revert in milliseconds").defineInRange("blockRevertMs", 8000, 0, 120000);

    public static final ModConfigSpec.IntValue SPIRITBEAM_IGNITE_MS =
            BUILDER.comment("SpiritBeam ignite duration in milliseconds").defineInRange("igniteMs", 5000, 0, 30000);

    public static final ModConfigSpec.ConfigValue<String> SPIRITBEAM_WITCH_PARTICLE =
            BUILDER.comment("SpiritBeam witch particle id").define("witchParticle", "minecraft:witch");

    public static final ModConfigSpec.IntValue SPIRITBEAM_WITCH_PARTICLE_COUNT =
            BUILDER.comment("SpiritBeam witch particle count").defineInRange("witchParticleCount", 2, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> SPIRITBEAM_PORTAL_PARTICLE =
            BUILDER.comment("SpiritBeam portal particle id").define("portalParticle", "minecraft:portal");

    public static final ModConfigSpec.IntValue SPIRITBEAM_PORTAL_PARTICLE_COUNT =
            BUILDER.comment("SpiritBeam portal particle count").defineInRange("portalParticleCount", 3, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("SpiritGrasp settings").push("SpiritGrasp");
    }

    public static final ModConfigSpec.IntValue SPIRITGRASP_COOLDOWN_MS =
            BUILDER.comment("SpiritGrasp cooldown in milliseconds").defineInRange("cooldownMs", 8000, 0, 120000);

    public static final ModConfigSpec.DoubleValue SPIRITGRASP_REACH =
            BUILDER.comment("SpiritGrasp placement reach in blocks").defineInRange("reach", 20.0, 4.0, 64.0);

    public static final ModConfigSpec.DoubleValue SPIRITGRASP_RADIUS =
            BUILDER.comment("SpiritGrasp zone radius in blocks").defineInRange("radius", 4.0, 1.0, 12.0);

    public static final ModConfigSpec.DoubleValue SPIRITGRASP_DAMAGE =
            BUILDER.comment("SpiritGrasp magic damage").defineInRange("damage", 2.0, 0.0, 40.0);

    public static final ModConfigSpec.IntValue SPIRITGRASP_DURATION_MS =
            BUILDER.comment("SpiritGrasp duration in milliseconds").defineInRange("durationMs", 10000, 1000, 120000);

    public static final ModConfigSpec.IntValue SPIRITGRASP_SLOW_DURATION_MS = BUILDER.comment(
                    "SpiritGrasp slowness duration in milliseconds")
            .defineInRange("slowDurationMs", 2000, 0, 60000);

    public static final ModConfigSpec.IntValue SPIRITGRASP_SLOW_AMP =
            BUILDER.comment("SpiritGrasp slowness amplifier").defineInRange("slowAmp", 2, 0, 10);

    public static final ModConfigSpec.ConfigValue<String> SPIRITGRASP_RING_PARTICLE =
            BUILDER.comment("SpiritGrasp ring particle id").define("ringParticle", "minecraft:happy_villager");

    public static final ModConfigSpec.IntValue SPIRITGRASP_RING_PARTICLE_COUNT =
            BUILDER.comment("SpiritGrasp ring particle count").defineInRange("ringParticleCount", 1, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> SPIRITGRASP_INNER_PARTICLE =
            BUILDER.comment("SpiritGrasp inner particle id").define("innerParticle", "minecraft:witch");

    public static final ModConfigSpec.IntValue SPIRITGRASP_INNER_PARTICLE_COUNT =
            BUILDER.comment("SpiritGrasp inner particle count").defineInRange("innerParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("SpiritProjection settings").push("SpiritProjection");
    }

    public static final ModConfigSpec.IntValue SPIRITPROJECTION_COOLDOWN_MS =
            BUILDER.comment("SpiritProjection cooldown in milliseconds").defineInRange("cooldownMs", 8000, 0, 120000);

    public static final ModConfigSpec.IntValue SPIRITPROJECTION_CHARGE_MS =
            BUILDER.comment("SpiritProjection charge in milliseconds").defineInRange("chargeMs", 5000, 500, 60000);

    public static final ModConfigSpec.DoubleValue SPIRITPROJECTION_TETHER =
            BUILDER.comment("SpiritProjection tether in blocks").defineInRange("tether", 128.0, 8.0, 256.0);

    public static final ModConfigSpec.DoubleValue SPIRITPROJECTION_RETURN_RANGE = BUILDER.comment(
                    "SpiritProjection click-return reach in blocks")
            .defineInRange("returnRange", 6.0, 2.0, 16.0);

    public static final ModConfigSpec.ConfigValue<String> SPIRITPROJECTION_CHARGE_PARTICLE =
            BUILDER.comment("SpiritProjection charge particle id").define("chargeParticle", "minecraft:end_rod");

    public static final ModConfigSpec.IntValue SPIRITPROJECTION_CHARGE_PARTICLE_COUNT =
            BUILDER.comment("SpiritProjection charge particle count").defineInRange("chargeParticleCount", 1, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.comment("SpiritStep settings").push("SpiritStep");
    }

    public static final ModConfigSpec.IntValue SPIRITSTEP_COOLDOWN_MS =
            BUILDER.comment("SpiritStep cooldown in milliseconds").defineInRange("cooldownMs", 6000, 0, 120000);

    public static final ModConfigSpec.DoubleValue SPIRITSTEP_RANGE =
            BUILDER.comment("SpiritStep range in blocks").defineInRange("range", 24.0, 4.0, 64.0);

    public static final ModConfigSpec.ConfigValue<String> SPIRITSTEP_BURST_PARTICLE =
            BUILDER.comment("SpiritStep burst particle id").define("burstParticle", "minecraft:portal");

    public static final ModConfigSpec.IntValue SPIRITSTEP_BURST_PARTICLE_COUNT =
            BUILDER.comment("SpiritStep burst particle count").defineInRange("burstParticleCount", 12, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> SPIRITSTEP_WISP_PARTICLE =
            BUILDER.comment("SpiritStep wisp particle id").define("wispParticle", "minecraft:end_rod");

    public static final ModConfigSpec.IntValue SPIRITSTEP_WISP_PARTICLE_COUNT =
            BUILDER.comment("SpiritStep wisp particle count").defineInRange("wispParticleCount", 6, 0, 64);

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.pop();
    }

    static {
        BUILDER.pop();
    }

    /** Milliseconds to game ticks, rounding up so short durations still tick at least once. */
    public static int msToTicks(long ms) {
        return ms >= 0 ? (int) ((ms + 49) / 50) : (int) (ms / 50);
    }

    /** Milliseconds to game ticks, rounding up so short durations still tick at least once. */
    public static int msToTicks(double ms) {
        return ms >= 0 ? (int) Math.ceil(ms / 50.0) : (int) (ms / 50.0);
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
