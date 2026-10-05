package com.phatabaniyan.avataruniverse.bending;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.phatabaniyan.avataruniverse.AvatarUniverseMod;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Port of ProjectKorra's {@code /bending} root (plugin.yml command
 * {@code projectkorra}, aliases b/bending/korra/pk/bend + ~20 subcommands).
 * Ports the core loop only: choose/add/bind/display/who/version/help/clear/
 * toggle. Presets, boards, reload, invincible, debug, copy/import/export are
 * follow-ups. Permission nodes from plugin.yml map to op levels: basic
 * (bending.player children) -> 0, admin (bending.admin) -> 2.
 */
@EventBusSubscriber(modid = AvatarUniverseMod.MODID)
public final class BendingCommands {
    private BendingCommands() {}

    private static final SuggestionProvider<CommandSourceStack> ELEMENTS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    List.of(
                            "air",
                            "water",
                            "earth",
                            "fire",
                            "avatar",
                            "flight",
                            "spiritual",
                            "blood",
                            "healing",
                            "ice",
                            "plant",
                            "lava",
                            "metal",
                            "sand",
                            "lightning",
                            "combustion",
                            "bluefire"),
                    builder);

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(commandTree("au"));
        event.getDispatcher().register(commandTree("avataruniverse"));
        event.getDispatcher().register(commandTree("bending"));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> commandTree(String name) {
        return Commands.literal(name)
                .requires(src -> src.hasPermission(0))
                .then(Commands.literal("version")
                        .executes(ctx -> reply(
                                ctx.getSource(), "AvatarUniverse bending core 0.1.0 — the four elements answer.")))
                .then(Commands.literal("help")
                        .executes(
                                ctx -> reply(
                                        ctx.getSource(),
                                        "Seek mastery: /au choose | add | bind | display | who | clear | remove | toggle — ponder /au help <ability> for wisdom."))
                        .then(Commands.argument("topic", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    java.util.List<String> topics = new java.util.ArrayList<>(AbilityCodex.names());
                                    for (BendingElement element : BendingElement.values()) {
                                        topics.add(element.key());
                                    }
                                    return SharedSuggestionProvider.suggest(topics, builder);
                                })
                                .executes(
                                        ctx -> helpTopic(ctx.getSource(), StringArgumentType.getString(ctx, "topic")))))
                .then(Commands.literal("display")
                        .executes(ctx -> displaySelf(ctx.getSource()))
                        .then(Commands.argument("element", StringArgumentType.word())
                                .suggests(ELEMENTS)
                                .executes(ctx ->
                                        displayElement(ctx.getSource(), StringArgumentType.getString(ctx, "element")))))
                .then(Commands.literal("info")
                        .then(Commands.argument("ability", StringArgumentType.word())
                                .suggests((ctx, builder) ->
                                        SharedSuggestionProvider.suggest(AbilityCodex.names(), builder))
                                .executes(ctx -> info(ctx.getSource(), StringArgumentType.getString(ctx, "ability")))))
                .then(Commands.literal("who")
                        .then(Commands.argument("target", StringArgumentType.word())
                                .executes(ctx ->
                                        displayTarget(ctx.getSource(), StringArgumentType.getString(ctx, "target")))))
                .then(Commands.literal("choose")
                        .then(Commands.argument("element", StringArgumentType.word())
                                .suggests(ELEMENTS)
                                .executes(ctx ->
                                        choose(ctx.getSource(), StringArgumentType.getString(ctx, "element"), true))))
                .then(Commands.literal("add")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("element", StringArgumentType.word())
                                .suggests(ELEMENTS)
                                .executes(ctx ->
                                        choose(ctx.getSource(), StringArgumentType.getString(ctx, "element"), false))))
                .then(Commands.literal("clear").executes(ctx -> clear(ctx.getSource())))
                .then(Commands.literal("reload")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> reload(ctx.getSource())))
                .then(Commands.literal("remove")
                        .then(Commands.argument("element", StringArgumentType.word())
                                .suggests(ELEMENTS)
                                .executes(
                                        ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, "element")))))
                .then(Commands.literal("toggle").executes(ctx -> toggle(ctx.getSource())))
                .then(Commands.literal("bind")
                        .then(Commands.argument("ability", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        bindableFor(
                                                ctx.getSource(),
                                                List.of(
                                                        "WaterManipulation",
                                                        "Torrent",
                                                        "WaterSpout",
                                                        "WaterArms",
                                                        "HealingWaters",
                                                        "WaterBubble",
                                                        "FrostBreath",
                                                        "IceBlast",
                                                        "IceSpike",
                                                        "PhaseChange",
                                                        "Bloodbending",
                                                        "BloodPuppet",
                                                        "Drain",
                                                        "IceClaws",
                                                        "IceWall",
                                                        "WakeFishing",
                                                        "RazorLeaf",
                                                        "IceCrawl",
                                                        "PlantArmor",
                                                        "LeafStorm",
                                                        "FireJet",
                                                        "FireKick",
                                                        "FireSpin",
                                                        "FireWheel",
                                                        "FireDisc",
                                                        "FireBall",
                                                        "FireSki",
                                                        "WallOfFire",
                                                        "Illumination",
                                                        "FireBurst",
                                                        "FireShield",
                                                        "FlameBreath",
                                                        "FireBreath",
                                                        "FireWave",
                                                        "FireComet",
                                                        "FireShots",
                                                        "FireManipulation",
                                                        "HeatControl",
                                                        "Jets",
                                                        "ArcSpark",
                                                        "ChargeBolt",
                                                        "Bolt",
                                                        "Discharge",
                                                        "LightningBurst",
                                                        "Lightning",
                                                        "Electrify",
                                                        "CombustBeam",
                                                        "Explode",
                                                        "CombustionBlast",
                                                        "Combustion",
                                                        "AirJet",
                                                        "AirScooter",
                                                        "AirSpout",
                                                        "AirPunch",
                                                        "AirSlam",
                                                        "AirFlight",
                                                        "AirShield",
                                                        "AirBurst",
                                                        "AirBlast",
                                                        "AirSwipe",
                                                        "AirSuction",
                                                        "Suffocate",
                                                        "Tornado",
                                                        "AirBreath",
                                                        "AirBullet",
                                                        "Meditate",
                                                        "SonicBlast",
                                                        "Zephyr",
                                                        "AirStream",
                                                        "AvatarState",
                                                        "ElementSphere",
                                                        "SpiritBeam",
                                                        "SpiritGrasp",
                                                        "SpiritProjection",
                                                        "SpiritStep",
                                                        "Accretion",
                                                        "Catapult",
                                                        "EarthBlast",
                                                        "EarthArmor",
                                                        "CollapseWall",
                                                        "RaiseEarth",
                                                        "Shockwave",
                                                        "EarthKick",
                                                        "Extraction",
                                                        "MetalClips",
                                                        "LavaFlow",
                                                        "LavaSurge",
                                                        "QuickWeld",
                                                        "RockSlide",
                                                        "Shrapnel",
                                                        "EarthPillar",
                                                        "EarthShard",
                                                        "Fissure",
                                                        "LavaDisc",
                                                        "LavaFlux",
                                                        "MagnetShield",
                                                        "MetalFragments",
                                                        "MetalHook",
                                                        "MudSurge",
                                                        "LavaDisk",
                                                        "EarthGlove",
                                                        "EarthDome",
                                                        "Dig",
                                                        "EarthTunnel",
                                                        "EarthSurf",
                                                        "EarthGrab")),
                                        builder))
                                .then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                        .executes(ctx -> bind(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "ability"),
                                                IntegerArgumentType.getInteger(ctx, "slot"))))
                                .executes(ctx ->
                                        bindSelected(ctx.getSource(), StringArgumentType.getString(ctx, "ability")))));
    }

    /** Bind to the held hotbar slot when no slot number is given. */
    private static int bindSelected(CommandSourceStack src, String ability) {
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            fail(src, "Only a living soul standing in this world may call upon the elements.");
            return 0;
        }
        return bind(src, ability, player.getInventory().selected + 1);
    }

    /** Only arts of the caller's elements (and the Avatar's all) are suggested. */
    private static java.util.List<String> bindableFor(CommandSourceStack src, java.util.List<String> all) {
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            return all;
        }
        BendingPlayer bending = BendingPlayer.get(player.getUUID());
        if (bending == null) {
            return all;
        }
        java.util.List<String> mine = new java.util.ArrayList<>();
        for (String ability : all) {
            BendingElement element = BendingTheme.elementOfAbility(ability);
            if (element == null || bending.hasElement(element)) {
                mine.add(ability);
            }
        }
        return mine;
    }

    private static int reply(CommandSourceStack src, String message) {
        return reply(src, message, null);
    }

    private static int reply(CommandSourceStack src, String message, BendingElement element) {
        src.sendSuccess(() -> BendingTheme.gradient(message, element), false);
        return 1;
    }

    private static void fail(CommandSourceStack src, String message) {
        src.sendFailure(Component.literal(message).withStyle(net.minecraft.network.chat.Style.EMPTY.withBold(true)));
    }

    private static ServerPlayer requirePlayer(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer player) {
            return player;
        }
        fail(src, "Only a living soul standing in this world may call upon the elements.");
        return null;
    }

    private static int choose(CommandSourceStack src, String elementName, boolean replace) {
        ServerPlayer player = requirePlayer(src);
        if (player == null) {
            return 0;
        }
        BendingElement element = BendingElement.byName(elementName);
        if (element == null) {
            fail(
                    src,
                    "No such element stirs in this world: " + elementName
                            + ". Name air, water, earth, fire, or avatar.");
            return 0;
        }
        BendingPlayer bending = BendingPlayer.getOrCreate(player.getUUID());
        if (replace) {
            bending.clearElements();
        }
        bending.addElement(element);
        if (replace) {
            // A new path forsakes the old arts: unbind everything the
            // player can no longer bend so the board never shows them.
            for (int slot = 1; slot <= 9; slot++) {
                String bound = bending.boundAbility(slot);
                if (bound != null) {
                    BendingElement need = BendingTheme.elementOfAbility(bound);
                    if (need != null && !bending.hasElement(need)) {
                        bending.bind(slot, null);
                    }
                }
            }
        }
        BendingBoardSync.sync(player);
        return reply(src, chooseProse(element), element);
    }

    /** /bending help <ability>: lore, usage, binding seals. Elements list their arts. */
    private static int helpTopic(CommandSourceStack src, String topic) {
        if (AbilityCodex.get(topic) != null) {
            return info(src, topic);
        }
        BendingElement element = BendingElement.byName(topic);
        if (element != null) {
            return displayElement(src, topic);
        }
        return reply(
                src,
                "Seek mastery: /au choose | add | bind | display | who | clear | remove | toggle — name an art or an element to unroll its scroll.");
    }

    /** /bending display <element>: every art of the element, click to learn. */
    private static int displayElement(CommandSourceStack src, String elementName) {
        BendingElement element = BendingElement.byName(elementName);
        if (element == null) {
            fail(src, "No such element stirs in this world: " + elementName + ".");
            return 0;
        }
        java.util.List<AbilityCodex.Info> arts = AbilityCodex.ofElement(element);
        if (arts.isEmpty()) {
            return reply(src, "No scrolls record " + elementTitle(element) + " arts yet.", element);
        }
        net.minecraft.network.chat.MutableComponent out =
                BendingTheme.gradient("— " + capitalize(element.key()) + " arts (" + arts.size() + ") —", element);
        for (AbilityCodex.Info art : arts) {
            out.append(Component.literal("\n"));
            out.append(clickable(art.name(), element, "/au info " + art.name(), "Click to learn " + art.name()));
        }
        src.sendSuccess(() -> out, false);
        return arts.size();
    }

    /** /bending info <ability>: lore, usage, and nine binding seals. */
    private static int info(CommandSourceStack src, String ability) {
        AbilityCodex.Info art = AbilityCodex.get(ability);
        if (art == null) {
            fail(src, "That art is unknown to this world: " + ability + ".");
            return 0;
        }
        net.minecraft.network.chat.MutableComponent out =
                BendingTheme.gradient("— " + art.name() + " —", art.element());
        out.append(Component.literal("\n"));
        out.append(Component.literal(art.description()));
        out.append(Component.literal("\n"));
        out.append(Component.literal(art.usage()).withStyle(s -> s.withItalic(true)));
        out.append(Component.literal("\n"));
        out.append(Component.literal("Bind: "));
        for (int slot = 1; slot <= 9; slot++) {
            if (slot > 1) {
                out.append(Component.literal(" "));
            }
            out.append(clickable(
                    "[" + slot + "]",
                    art.element(),
                    "/au bind " + art.name() + " " + slot,
                    "Bind " + art.name() + " to slot " + slot));
        }
        src.sendSuccess(() -> out, false);
        return 1;
    }

    private static net.minecraft.network.chat.MutableComponent clickable(
            String label, BendingElement element, String command, String hint) {
        net.minecraft.network.chat.MutableComponent grad = BendingTheme.gradient(label, element);
        net.minecraft.network.chat.MutableComponent out = Component.empty();
        for (Component sibling : grad.getSiblings()) {
            out.append(sibling.copy()
                    .withStyle(sibling.getStyle()
                            .withClickEvent(new net.minecraft.network.chat.ClickEvent(
                                    net.minecraft.network.chat.ClickEvent.Action.RUN_COMMAND, command))
                            .withHoverEvent(new net.minecraft.network.chat.HoverEvent(
                                    net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.literal(hint)))));
        }
        return out;
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static int displaySelf(CommandSourceStack src) {
        ServerPlayer player = requirePlayer(src);
        if (player == null) {
            return 0;
        }
        BendingPlayer bending = BendingPlayer.getOrCreate(player.getUUID());
        return reply(src, describe(player.getScoreboardName(), bending));
    }

    private static int displayTarget(CommandSourceStack src, String target) {
        List<String> lines = new ArrayList<>();
        for (ServerPlayer player : src.getServer().getPlayerList().getPlayers()) {
            if (player.getScoreboardName().equalsIgnoreCase(target)) {
                lines.add(describe(player.getScoreboardName(), BendingPlayer.getOrCreate(player.getUUID())));
            }
        }
        if (lines.isEmpty()) {
            fail(src, "No bender by that name walks this world: " + target);
            return 0;
        }
        lines.forEach(line -> src.sendSuccess(() -> Component.literal(line), false));
        return 1;
    }

    /** One proud sentence per element for the choose command. */
    private static String chooseProse(BendingElement element) {
        return switch (element) {
            case FIRE -> "You possess the power of the Fire element — let your passion burn, but never let it consume you.";
            case WATER -> "You possess the power of the Water element — flow like the tide, and the moon shall guide your hand.";
            case EARTH -> "You possess the power of the Earth element — stand unyielding as the mountain, and the stone shall obey.";
            case AIR -> "You possess the power of the Air element — be free as the wind, and no chain shall ever hold you.";
            case AVATAR -> "You possess the power of all four elements — ten thousand past lives speak as one through you.";
            case ICE -> "You possess the gift of Ice — winter itself kneels, and frost blooms where you tread.";
            case PLANT -> "You possess the gift of the swamps — every leaf and vine shall rise at your whisper.";
            case BLOOD -> "You possess the dread art of Blood — on moonless nights, even heartbeats obey.";
            case HEALING -> "You possess the gentle art of Healing — the waters shall knit what war has torn.";
            case LAVA -> "You possess the hunger of Lava — mountains melt before your stride.";
            case METAL -> "You possess the mastery of Metal — the purest earth, refined, answers only you.";
            case SAND -> "You possess the patience of Sand — countless grains, one will, no escape.";
            case LIGHTNING -> "You possess the fury of Lightning — cold fire from a clear sky, swifter than thought.";
            case COMBUSTION -> "You possess the third eye of Combustion — point, and the world ends where you look.";
            case BLUE_FIRE -> "You possess the sacred Blue Fire — hotter than rage, purer than grief.";
            case FLIGHT -> "You possess the freedom of Flight — sever your earthly tether, and the sky shall carry you.";
            case SPIRITUAL -> "You possess the sight of Spirits — walk both worlds, and neither shall bar your path.";
        };
    }

    private static String describe(String name, BendingPlayer bending) {
        String elements = bending.elements().isEmpty()
                ? "no element answers"
                : String.join(
                        ",",
                        bending.elements().stream().map(BendingElement::key).toList());
        return name
                + " bends "
                + elements.toLowerCase(Locale.ROOT)
                + " — spirit "
                + (bending.isToggled() ? "awake." : "asleep.");
    }

    private static int clear(CommandSourceStack src) {
        ServerPlayer player = requirePlayer(src);
        if (player == null) {
            return 0;
        }
        BendingPlayer bending = BendingPlayer.getOrCreate(player.getUUID());
        bending.clearElements();
        BendingManager.cancelFor(player.getUUID());
        BendingBoardSync.sync(player);
        return reply(src, "Your spirit is emptied — no element answers your call.");
    }

    /** /au reload: re-read every config scroll from disk (op only). */
    private static int reload(CommandSourceStack src) {
        try {
            net.neoforged.fml.config.ConfigTracker.INSTANCE.loadConfigs(
                    net.neoforged.fml.config.ModConfig.Type.COMMON, net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());
        } catch (RuntimeException e) {
            fail(src, "The scrolls resisted re-reading — check the server log.");
            return 0;
        }
        return reply(
                src, "The scrolls are re-read — altered numbers take hold after a restart, live boards at once.", null);
    }

    /** /au remove <element>: forsake one element and the arts that need it. */
    private static int remove(CommandSourceStack src, String elementName) {
        ServerPlayer player = requirePlayer(src);
        if (player == null) {
            return 0;
        }
        BendingElement element = BendingElement.byName(elementName);
        if (element == null) {
            fail(src, "No such element stirs in this world: " + elementName + ".");
            return 0;
        }
        BendingPlayer bending = BendingPlayer.getOrCreate(player.getUUID());
        if (!bending.removeElement(element)) {
            return reply(src, "The " + element.key() + " never answered you — nothing to forsake.", element);
        }
        for (int slot = 1; slot <= 9; slot++) {
            String bound = bending.boundAbility(slot);
            if (bound != null) {
                BendingElement need = BendingTheme.elementOfAbility(bound);
                if (need != null && !bending.hasElement(need)) {
                    bending.bind(slot, null);
                }
            }
        }
        BendingBoardSync.sync(player);
        return reply(src, "You forsake " + elementTitle(element) + " — its arts crumble from your grasp.", element);
    }

    private static int toggle(CommandSourceStack src) {
        ServerPlayer player = requirePlayer(src);
        if (player == null) {
            return 0;
        }
        BendingPlayer bending = BendingPlayer.getOrCreate(player.getUUID());
        bending.setToggled(!bending.isToggled());
        return reply(
                src,
                bending.isToggled()
                        ? "Your bending awakens — the elements listen once more."
                        : "Your bending sleeps — the elements turn away.");
    }

    private static int bind(CommandSourceStack src, String ability, int slot) {
        ServerPlayer player = requirePlayer(src);
        if (player == null) {
            return 0;
        }
        String canonical = null;
        if (ability.equalsIgnoreCase("WaterManipulation")) {
            canonical = "WaterManipulation";
        } else if (ability.equalsIgnoreCase("Torrent")) {
            canonical = "Torrent";
        } else if (ability.equalsIgnoreCase("WaterSpout")) {
            canonical = "WaterSpout";
        } else if (ability.equalsIgnoreCase("WaterArms")) {
            canonical = "WaterArms";
        } else if (ability.equalsIgnoreCase("HealingWaters")) {
            canonical = "HealingWaters";
        } else if (ability.equalsIgnoreCase("WaterBubble")) {
            canonical = "WaterBubble";
        } else if (ability.equalsIgnoreCase("FrostBreath")) {
            canonical = "FrostBreath";
        } else if (ability.equalsIgnoreCase("IceBlast")) {
            canonical = "IceBlast";
        } else if (ability.equalsIgnoreCase("IceSpike")) {
            canonical = "IceSpike";
        } else if (ability.equalsIgnoreCase("PhaseChange")) {
            canonical = "PhaseChange";
        } else if (ability.equalsIgnoreCase("Bloodbending")) {
            canonical = "Bloodbending";
        } else if (ability.equalsIgnoreCase("BloodPuppet")) {
            canonical = "BloodPuppet";
        } else if (ability.equalsIgnoreCase("Drain")) {
            canonical = "Drain";
        } else if (ability.equalsIgnoreCase("IceClaws")) {
            canonical = "IceClaws";
        } else if (ability.equalsIgnoreCase("IceWall")) {
            canonical = "IceWall";
        } else if (ability.equalsIgnoreCase("WakeFishing")) {
            canonical = "WakeFishing";
        } else if (ability.equalsIgnoreCase("RazorLeaf")) {
            canonical = "RazorLeaf";
        } else if (ability.equalsIgnoreCase("IceCrawl")) {
            canonical = "IceCrawl";
        } else if (ability.equalsIgnoreCase("PlantArmor")) {
            canonical = "PlantArmor";
        } else if (ability.equalsIgnoreCase("LeafStorm")) {
            canonical = "LeafStorm";
        } else if (ability.equalsIgnoreCase("FireJet")) {
            canonical = "FireJet";
        } else if (ability.equalsIgnoreCase("FireKick")) {
            canonical = "FireKick";
        } else if (ability.equalsIgnoreCase("FireSpin")) {
            canonical = "FireSpin";
        } else if (ability.equalsIgnoreCase("FireWheel")) {
            canonical = "FireWheel";
        } else if (ability.equalsIgnoreCase("FireDisc")) {
            canonical = "FireDisc";
        } else if (ability.equalsIgnoreCase("FireBall")) {
            canonical = "FireBall";
        } else if (ability.equalsIgnoreCase("FireSki")) {
            canonical = "FireSki";
        } else if (ability.equalsIgnoreCase("WallOfFire")) {
            canonical = "WallOfFire";
        } else if (ability.equalsIgnoreCase("Illumination")) {
            canonical = "Illumination";
        } else if (ability.equalsIgnoreCase("FireBurst")) {
            canonical = "FireBurst";
        } else if (ability.equalsIgnoreCase("FireShield")) {
            canonical = "FireShield";
        } else if (ability.equalsIgnoreCase("FlameBreath")) {
            canonical = "FlameBreath";
        } else if (ability.equalsIgnoreCase("FireBreath")) {
            canonical = "FireBreath";
        } else if (ability.equalsIgnoreCase("FireWave")) {
            canonical = "FireWave";
        } else if (ability.equalsIgnoreCase("FireComet")) {
            canonical = "FireComet";
        } else if (ability.equalsIgnoreCase("FireShots")) {
            canonical = "FireShots";
        } else if (ability.equalsIgnoreCase("FireManipulation")) {
            canonical = "FireManipulation";
        } else if (ability.equalsIgnoreCase("HeatControl")) {
            canonical = "HeatControl";
        } else if (ability.equalsIgnoreCase("Jets")) {
            canonical = "Jets";
        } else if (ability.equalsIgnoreCase("ArcSpark")) {
            canonical = "ArcSpark";
        } else if (ability.equalsIgnoreCase("ChargeBolt")) {
            canonical = "ChargeBolt";
        } else if (ability.equalsIgnoreCase("Bolt")) {
            canonical = "Bolt";
        } else if (ability.equalsIgnoreCase("Discharge")) {
            canonical = "Discharge";
        } else if (ability.equalsIgnoreCase("LightningBurst")) {
            canonical = "LightningBurst";
        } else if (ability.equalsIgnoreCase("Lightning")) {
            canonical = "Lightning";
        } else if (ability.equalsIgnoreCase("Electrify")) {
            canonical = "Electrify";
        } else if (ability.equalsIgnoreCase("CombustBeam")) {
            canonical = "CombustBeam";
        } else if (ability.equalsIgnoreCase("Explode")) {
            canonical = "Explode";
        } else if (ability.equalsIgnoreCase("CombustionBlast")) {
            canonical = "CombustionBlast";
        } else if (ability.equalsIgnoreCase("Combustion")) {
            canonical = "Combustion";
        } else if (ability.equalsIgnoreCase("AirJet")) {
            canonical = "AirJet";
        } else if (ability.equalsIgnoreCase("AirScooter")) {
            canonical = "AirScooter";
        } else if (ability.equalsIgnoreCase("AirSpout")) {
            canonical = "AirSpout";
        } else if (ability.equalsIgnoreCase("AirPunch")) {
            canonical = "AirPunch";
        } else if (ability.equalsIgnoreCase("AirSlam")) {
            canonical = "AirSlam";
        } else if (ability.equalsIgnoreCase("AirFlight")) {
            canonical = "AirFlight";
        } else if (ability.equalsIgnoreCase("AirShield")) {
            canonical = "AirShield";
        } else if (ability.equalsIgnoreCase("AirBurst")) {
            canonical = "AirBurst";
        } else if (ability.equalsIgnoreCase("AirBlast")) {
            canonical = "AirBlast";
        } else if (ability.equalsIgnoreCase("AirSwipe")) {
            canonical = "AirSwipe";
        } else if (ability.equalsIgnoreCase("AirSuction")) {
            canonical = "AirSuction";
        } else if (ability.equalsIgnoreCase("Suffocate")) {
            canonical = "Suffocate";
        } else if (ability.equalsIgnoreCase("Tornado")) {
            canonical = "Tornado";
        } else if (ability.equalsIgnoreCase("AirBreath")) {
            canonical = "AirBreath";
        } else if (ability.equalsIgnoreCase("AirBullet")) {
            canonical = "AirBullet";
        } else if (ability.equalsIgnoreCase("Meditate")) {
            canonical = "Meditate";
        } else if (ability.equalsIgnoreCase("SonicBlast")) {
            canonical = "SonicBlast";
        } else if (ability.equalsIgnoreCase("Zephyr")) {
            canonical = "Zephyr";
        } else if (ability.equalsIgnoreCase("AirStream")) {
            canonical = "AirStream";
        } else if (ability.equalsIgnoreCase("AvatarState")) {
            canonical = "AvatarState";
        } else if (ability.equalsIgnoreCase("ElementSphere")) {
            canonical = "ElementSphere";
        } else if (ability.equalsIgnoreCase("SpiritBeam")) {
            canonical = "SpiritBeam";
        } else if (ability.equalsIgnoreCase("SpiritGrasp")) {
            canonical = "SpiritGrasp";
        } else if (ability.equalsIgnoreCase("SpiritProjection")) {
            canonical = "SpiritProjection";
        } else if (ability.equalsIgnoreCase("SpiritStep")) {
            canonical = "SpiritStep";
        } else if (ability.equalsIgnoreCase("Accretion")) {
            canonical = "Accretion";
        } else if (ability.equalsIgnoreCase("Catapult")) {
            canonical = "Catapult";
        } else if (ability.equalsIgnoreCase("EarthBlast")) {
            canonical = "EarthBlast";
        } else if (ability.equalsIgnoreCase("EarthArmor")) {
            canonical = "EarthArmor";
        } else if (ability.equalsIgnoreCase("CollapseWall")) {
            canonical = "CollapseWall";
        } else if (ability.equalsIgnoreCase("RaiseEarth")) {
            canonical = "RaiseEarth";
        } else if (ability.equalsIgnoreCase("Shockwave")) {
            canonical = "Shockwave";
        } else if (ability.equalsIgnoreCase("EarthKick")) {
            canonical = "EarthKick";
        } else if (ability.equalsIgnoreCase("Extraction")) {
            canonical = "Extraction";
        } else if (ability.equalsIgnoreCase("MetalClips")) {
            canonical = "MetalClips";
        } else if (ability.equalsIgnoreCase("LavaFlow")) {
            canonical = "LavaFlow";
        } else if (ability.equalsIgnoreCase("LavaSurge")) {
            canonical = "LavaSurge";
        } else if (ability.equalsIgnoreCase("QuickWeld")) {
            canonical = "QuickWeld";
        } else if (ability.equalsIgnoreCase("RockSlide")) {
            canonical = "RockSlide";
        } else if (ability.equalsIgnoreCase("Shrapnel")) {
            canonical = "Shrapnel";
        } else if (ability.equalsIgnoreCase("EarthPillar")) {
            canonical = "EarthPillar";
        } else if (ability.equalsIgnoreCase("EarthShard")) {
            canonical = "EarthShard";
        } else if (ability.equalsIgnoreCase("Fissure")) {
            canonical = "Fissure";
        } else if (ability.equalsIgnoreCase("LavaDisc")) {
            canonical = "LavaDisc";
        } else if (ability.equalsIgnoreCase("LavaFlux")) {
            canonical = "LavaFlux";
        } else if (ability.equalsIgnoreCase("MagnetShield")) {
            canonical = "MagnetShield";
        } else if (ability.equalsIgnoreCase("MetalFragments")) {
            canonical = "MetalFragments";
        } else if (ability.equalsIgnoreCase("MetalHook")) {
            canonical = "MetalHook";
        } else if (ability.equalsIgnoreCase("MudSurge")) {
            canonical = "MudSurge";
        } else if (ability.equalsIgnoreCase("LavaDisk")) {
            canonical = "LavaDisk";
        } else if (ability.equalsIgnoreCase("EarthGlove")) {
            canonical = "EarthGlove";
        } else if (ability.equalsIgnoreCase("EarthDome")) {
            canonical = "EarthDome";
        } else if (ability.equalsIgnoreCase("Dig")) {
            canonical = "Dig";
        } else if (ability.equalsIgnoreCase("EarthTunnel")) {
            canonical = "EarthTunnel";
        } else if (ability.equalsIgnoreCase("EarthSurf")) {
            canonical = "EarthSurf";
        } else if (ability.equalsIgnoreCase("EarthGrab")) {
            canonical = "EarthGrab";
        }
        if (canonical == null) {
            fail(
                    src,
                    "That art is unknown to this world: "
                            + ability
                            + ". Consult the bound scroll — WaterManipulation, Torrent, WaterSpout, WaterArms, ...");
            return 0;
        }
        BendingPlayer binder = BendingPlayer.getOrCreate(player.getUUID());
        BendingElement boundElement = BendingTheme.elementOfAbility(canonical);
        if (boundElement != null && !binder.hasElement(boundElement)) {
            src.sendFailure(BendingTheme.gradient(
                    "The art of "
                            + canonical
                            + " bows only to "
                            + elementTitle(boundElement)
                            + " — it is not yours to command.",
                    boundElement));
            return 0;
        }
        binder.bind(slot, canonical);
        BendingBoardSync.sync(player); // Tint by the binder's own sub-element when it refines the art
        // (blue-fire holders see blue, not red).
        BendingElement flavor = boundElement;
        if (boundElement != null) {
            for (BendingElement owned : binder.elements()) {
                if (owned.parent() == boundElement) {
                    flavor = owned;
                    break;
                }
            }
        }
        return reply(src, canonical + " now rests in slot " + slot + ".", flavor);
    }

    /** "the Fire element" style titles for the bind-refusal sentence. */
    private static String elementTitle(BendingElement element) {
        return switch (element) {
            case FIRE -> "the Fire element";
            case WATER -> "the Water element";
            case EARTH -> "the Earth element";
            case AIR -> "the Air element";
            case AVATAR -> "the Avatar spirit";
            case ICE -> "the gift of Ice";
            case PLANT -> "the gift of the swamps";
            case BLOOD -> "the dread art of Blood";
            case HEALING -> "the gentle art of Healing";
            case LAVA -> "the hunger of Lava";
            case METAL -> "the mastery of Metal";
            case SAND -> "the patience of Sand";
            case LIGHTNING -> "the fury of Lightning";
            case COMBUSTION -> "the third eye of Combustion";
            case BLUE_FIRE -> "the sacred Blue Fire";
            case FLIGHT -> "the freedom of Flight";
            case SPIRITUAL -> "the sight of Spirits";
        };
    }
}
