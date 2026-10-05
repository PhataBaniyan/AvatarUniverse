package com.phatabaniyan.avataruniverse.bending;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-source codex of bending-ability description + usage text, harvested verbatim
 * from ProjectAvatar AbilityBootstrap, ProjectKorra ConfigManager, ProjectAddons,
 * JedCore-Cozmyc JedCoreConfig and Hyperion ConfigManager. Nothing invented.
 */
public final class AbilityCodex {
    private AbilityCodex() {}

    public record Info(String name, BendingElement element, String description, String usage) {}

    private static final Map<String, Info> BY_NAME = new LinkedHashMap<>();

    static {
        register(
                "WaterManipulation",
                BendingElement.WATER,
                "WaterManipulation is a fundamental ability for waterbenders. Although it is a basic move, it allows for fast damage due to its rapid fire nature, which is incredibly useful when wanting to finish off low health targets.",
                "Tap sneak while looking at a water source and left click to send a water manipulation to the point that you clicked. Additionally, you can left click again to change the direction of this move. This includes other players\\' WaterManipulations.");
        register(
                "Torrent",
                BendingElement.WATER,
                "Torrent is one of the strongest moves in a waterbender\\'s arsenal. It has the potential to do immense damage and to be comboed with other abilities to perform a deal a large damage burst. Torrent is fundamental for waterbender\\'s.",
                "(Torrent) Left click at a water source and hold sneak to form the torrent. Then, left click and the torrent will shoot out, moving in the direction you\\'re looking. If the torrent hits an entity, it can drag them and deal damage. Additionally, if you left click before the torrent hits a surface or entity it will freeze on impact.\\n(Wave) Left click a water source and hold sneak to form a torrent around you. Then, release sneak to send a wave of water expanding outwards every direction that will push entities back.");
        register(
                "WaterSpout",
                BendingElement.WATER,
                "This ability provides a Waterbender with a means of transportation. It\\'s the most useful mobility move that a waterbender possesses and is great for chasing down targets or escaping.",
                "(Spout) Left click to activate a spout beneath you and hold spacebar to go higher. If you wish to go lower, simply hold sneak. To disable this ability, left click once again.\\n(SpoutHop) While WaterSpout is active, hold sneak and left-click to jump forward!\\n(Wave) Left click a water source and hold sneak until water has formed around you. Then, release sneak to ride a water wave that transports you in the direction you\\'re looking. To cancel this water wave, left click with WaterSpout.");
        register(
                "WaterArms",
                BendingElement.WATER,
                "One of the most diverse moves in a Waterbender\\'s arsenal, this move creates tendrils of water from the players arms to emulate their actual arms. It has the potential to do a variety of things that can either do mass amounts of damage, or used for mobility.",
                "To activate this ability, tap sneak at a water source. Additionally, to de-activate this ability, hold sneak and left click.\\n(Pull) Left click at a target and your arms will expand outwards, pulling entities towards you if they\\'re in range.\\n(Punch) Left click and one arm will expand outwards, punching anyone it hits and dealing damage.\\n(Grapple) Left click to send your arms forward, pulling you to whatever surface they land on.\\n(Grab) Left click to grab an entity that\\'s in range. They will then be controlled and moved in whatever direction you look. Additionally, if you left click again you can throw the target that you\\'re controlling.\\n(Freeze) Left click to rapidly fire ice blasts at a target, damaging the target and giving them slowness.\\n(Spear) Left click to send an ice spear out, damaging and freezing whoever it hits in ice blocks.");
        register(
                "HealingWaters",
                BendingElement.HEALING,
                "HealingWaters is an advanced waterbender skill that allows the player to heal themselves or others from the damage they\\'ve taken. If healing another player, you must continue to look at them to channel the ability.",
                "Hold sneak to begin healing yourself or right click while sneaking to begin healing another player. You or the player must be in water and damaged for this ability to work, or you need to have water bottles in your inventory.");
        register(
                "WaterBubble",
                BendingElement.WATER,
                "WaterBubble is a basic waterbending ability that allows the bender to create air pockets under water. This is incredibly useful for building under water.",
                "Hold sneak when in range of water to push the water back and create a water bubble. Alternatively, you can click to create a bubble for a short amount of time.");
        register(
                "FrostBreath",
                BendingElement.ICE,
                "FrostBreath is an ability that lets the user to icebend through their lungs. Breathe out a cold air to freeze your enemies!",
                "Hold sneak and look at your target to freeze them.");
        register(
                "IceBlast",
                BendingElement.ICE,
                "IceBlast is a powerful ability that deals damage to entities it comes into contact with. Because IceBlast\\'s travel time is pretty quick, it\\'s incredibly useful for finishing off low health targets.",
                "Tap sneak while looking at an ice block and then click in a direction to send an ice blast in that direction.");
        register(
                "IceSpike",
                BendingElement.ICE,
                "This ability offers a powerful ice utility for Waterbenders. It can be used to fire an ice blast or raise an ice spike. If the ice blast or ice spike comes into contact with another entity, it will give them slowness and deal some damage to them..",
                "(Blast) Tap sneak on a water source and then left click in a direction to fire an ice blast in a direction. Additionally, you can left click to manipulate the ice blast while it\\'s in the air to change the direction of the blast.\\n(Spike) While in range of ice, tap sneak to raise ice pillars from the ice. If a player is caught in these ice pillars they will be propelled into the air. You cannot be looking at ice or water or this feature will not activate. Alternatively, you can left click an ice block to raise a single pilar of ice.");
        register(
                "PhaseChange",
                BendingElement.WATER,
                "PhaseChange is one of the most useful utility moves that a waterbender possess. This ability is better used when fighting, allowing you to create a platform on water that you can fight on and being territorial by manipulating your environment. It\\'s also useful for travelling across seas.",
                "(Melt) To melt ice, hold sneak while looking at an ice block.\\n(Freeze) To freeze water and turn it into ice, simply left click at water. This ice will stay so long as you are in range, otherwise it will revert back to water. This only freezes the top layer of ice.");
        register(
                "Bloodbending",
                BendingElement.BLOOD,
                "Bloodbending is one of the most unique bending abilities that existed and it has immense power, which is why it was made illegal in the Avatar universe. People who are capable of bloodbending are immune to your technique, and you are immune to theirs.",
                "(Control) Hold sneak while looking at an entity to bloodbend them. You will then be controlling the entity, making them move wherever you look.\\n(Throw) While bloodbending an entity, left click to throw that entity in the direction you\\'re looking.");
        register(
                "BloodPuppet",
                BendingElement.BLOOD,
                "This very high-level bloodbending ability lets a master control entities\\' limbs, forcing them to attack the master\\'s target. To use this ability, you must be a bloodbender. Next, sneak while targeting a mob or player and you will start controlling them. To make the entity hit another, click. To release your target, stop sneaking. This ability has NO cooldown, but may only be usable during the night depending on the server configuration.",
                "Bind it and bend to learn its ways.");
        register(
                "Drain",
                BendingElement.WATER,
                "Inspired by how Hama drained water from the fire lilies, many benders have practiced in the skill of draining water from plants! With this ability bound, Sneak (Default: Shift) near/around plant sources to drain the water out of them to fill up any bottles/buckets in your inventory! Alternatively, if you have nothing to fill and blasts are enabled in the config, you will be able to create mini blasts of water to shoot at your targets! Aleternatively, this ability can also be used to quickly fill up bottles from straight water sources or from falling rain!",
                "Bind it and bend to learn its ways.");
        register(
                "IceClaws",
                BendingElement.ICE,
                "As demonstrated by Hama, a Waterbender can pull water out of thin air to create claws at the tips of their fingers. With IceClaws bound, hold Sneak (Default: Shift) to start pulling water out the air until you form claws at your finger tips, then attack an enemy to slow them down and do a bit of damage!",
                "Bind it and bend to learn its ways.");
        register(
                "IceWall",
                BendingElement.ICE,
                "IceWall allows an icebender to create a wall of ice, similar to raiseearth. To use, simply sneak while targeting either water, ice, or snow. To break the wall, you must sneak again while targeting it. Be aware that other icebenders can break your own shields, and if you are too close you can get hurt by the shards.",
                "Bind it and bend to learn its ways.");
        register(
                "WakeFishing",
                BendingElement.WATER,
                "With this ability bound, hold Sneak (Default: Shift) at a water block and don\\'t lose focus of that block. Eventually some fish will investigate the wake and swim out at you!",
                "Bind it and bend to learn its ways.");
        register(
                "RazorLeaf",
                BendingElement.PLANT,
                "Spin leaves around really fast and make them razor sharp!",
                "Sneak at plants to begin, hold to aim, and release to shoot it! Sneaking again will pull it back towards you!");
        register(
                "IceCrawl",
                BendingElement.ICE,
                "Tap sneak at a water or ice source block and then left click in a direction to launch forward a narrow line of ice. Upon colliding with an enemy, it deals damage and freezes the target\\'s feet.",
                "Bind it and bend to learn its ways.");
        register(
                "PlantArmor",
                BendingElement.PLANT,
                "Wrap your body in vines and leaves to create a protective armor which nullifies falling and drowning damage, while also giving speed, jump, and swim boosts! The armor then acts as a source for many subabilities!\\n[VineWhip] : Throw a whip of vines to damage entities!\\n[RazorLeaf] : Control a spinning disc of leaves to damage entities!\\n[LeafShield] : Hold a circular shield of leaves to block attacks!\\n[Tangle] : Shoot a bundle of vines to constrict enemies!\\n[Leap] : Launch yourself really high into the air from the ground!\\n[Grapple] : Grapple to a point with your vines!\\n[LeafDome] : Surround your body in a dome of leaves!\\n[Regenerate] : Gather more plants to repair armor!\\n[Disperse] : Deactivate your plantarmor!",
                "Press sneak to activate multiability\\n[VineWhip, Tangle, Leap, Grapple, Disperse]: Left Click\\n[RazorLeaf, LeafShield, LeafDome, Regenerate]: Hold Sneak");
        register(
                "LeafStorm",
                BendingElement.PLANT,
                "A combo only usable in with PlantArmor, create a whirling storm of leaves around you! Leaves disappear after hitting a block or entity, or you stop sneaking.",
                "RazorLeaf (Double Click) > VineWhip (Hold Sneak)");
        register(
                "FireJet",
                BendingElement.FIRE,
                "As Ozai and Azula proved during Sozin\\'s Comet - and Jeong Jeong before them - a master firebender can expel jets of flame to take flight, trading the ground for the sky itself.",
                "Left-click to ignite the jet and surge where you look. Click again mid-flight to cut the flame. Touching water snuffs it out.");
        register(
                "FireKick",
                BendingElement.FIRE,
                "The opening bell of every Agni Kai - a snapping fan of flame that crosses the arena before the other foot lands.",
                "Left-click to snap the fan.");
        register(
                "FireSpin",
                BendingElement.FIRE,
                "Azula\\'s coronation temper - a full circle of streams ripping outward so nothing standing gets to stay standing.",
                "Left-click to detonate the ring.");
        register(
                "FireWheel",
                BendingElement.FIRE,
                "A burning wheel with somewhere to be - it rolls the ground flat, climbs what it must, and brands everything it touches.",
                "Left-click to roll the wheel down your gaze.");
        register(
                "FireDisc",
                BendingElement.FIRE,
                "A sawblade that happens to be on fire - steerable mid-flight, and the first thing it meets gets branded.",
                "Left-click to throw. It follows your crosshair.");
        register(
                "FireBall",
                BendingElement.FIRE,
                "Zuko\\'s promise in a sphere - a steerable fireball that bends wherever you look until something stops it.",
                "Left-click to throw. It follows your crosshair.");
        register(
                "FireSki",
                BendingElement.FIRE,
                "JedCore\\'s downhill arson - drop in high and ski the sky itself, torching whatever you buzz on the way down.",
                "Left-click high in the air to drop in. Land, clip, or time out to end it.");
        register(
                "WallOfFire",
                BendingElement.FIRE,
                "Jeong Jeong\\'s curtain between the Avatar and the world - a hanging sheet of flame that sears whatever stands in it, block-free.",
                "Left-click the ground to raise the wall.");
        register(
                "Illumination",
                BendingElement.FIRE,
                "Zuko lit the dark with nothing but his own flame - click to carry a firebender\\'s glow that night itself respects.",
                "Left-click to toggle the glow. Click again to douse it.");
        register(
                "FireBurst",
                BendingElement.FIRE,
                "Azula\\'s temper made manifest - hold it in until it can\\'t be held, then either let the sphere take everyone or point the cone at someone.",
                "Hold sneak to charge. Release for a radial detonation, or left-click while charged for a cone of live blasts.");
        register(
                "FireShield",
                BendingElement.FIRE,
                "Zuko\\'s ring of fire around the boy in the ice - everything that touches it catches, and arrows die in it before they land.",
                "Hold sneak to burn. Release or run dry to drop it.");
        register(
                "FlameBreath",
                BendingElement.FIRE,
                "The dragons\\' homework: breathe a growing cone that burns bodies and chars the ground it crawls over.",
                "Hold sneak to breathe out.");
        register(
                "FireBreath",
                BendingElement.FIRE,
                "Dragons teach, students burn - hold the exhale and paint a hallway in dragonfire, softer on players than on monsters.",
                "Hold sneak to breathe out.");
        register(
                "FireWave",
                BendingElement.FIRE,
                "A wall with somewhere to be - hold sneak and march a burning curtain down your gaze that stops whatever it touches dead.",
                "Hold sneak to march the wall.");
        register(
                "FireComet",
                BendingElement.FIRE,
                "Sozin\\'s sky falling on demand - gather the comet ahead, release it down your gaze, and let the impact do the talking.",
                "Hold sneak to gather at a point ahead, release to send the comet.");
        register(
                "FireShots",
                BendingElement.FIRE,
                "Azula\\'s finger-guns made honest - gather a stock of shots at the hand and throw them one click at a time.",
                "Sneak to gather the stock. Left-click to throw until dry.");
        register(
                "FireManipulation",
                BendingElement.FIRE,
                "Mako\\'s factory-floor finesse - gather a burning orb at your cursor, then discharge it as a stream down your live gaze.",
                "Hold sneak to gather the orb. Scroll to stretch or close its range. Left-click to fire the stream.");
        register(
                "HeatControl",
                BendingElement.FIRE,
                "Iroh\\'s tea test - a master carries summer in his hands: dinner cooks itself, flames kneel, and lava remembers being stone.",
                "Hold sneak to cook held food, snuff nearby fire, and petrify lava ahead.");
        register(
                "Jets",
                BendingElement.FIRE,
                "Create jets of flames from your feet to hover off the ground or fly through the sky. Activating the ability while moving fast enough and looking in the direction you\\'re moving will automatically put you in flying mode!",
                "Left click to activate, Left click to switch modes, and Sneak + Left click to cancel ability");
        register(
                "ArcSpark",
                BendingElement.LIGHTNING,
                "Hold the storm in your palms until it crackles, then let one seeking arc hunt the nearest heartbeat and ground out.",
                "Hold sneak to charge. Left-click while charged to loose the seeker.");
        register(
                "ChargeBolt",
                BendingElement.LIGHTNING,
                "Bottle the storm, then spend it: single bolts down the gaze per click, or let go and wear the whole ring at once.",
                "Hold sneak to charge. Click to throw singles, release to discharge the ring.");
        register(
                "Bolt",
                BendingElement.LIGHTNING,
                "Charge the sky, point a finger, and let the storm do the paperwork - it prefers living targets and doubles down on wet ones.",
                "Hold sneak to charge, release to strike.");
        register(
                "Discharge",
                BendingElement.LIGHTNING,
                "Azula\\'s fingertips, fanned - a crackling fan of jittering branches that crawls forward shocking the first thing each touches.",
                "Left-click to spit the branches.");
        register(
                "LightningBurst",
                BendingElement.LIGHTNING,
                "The whole storm at once - charge it, release it, and wear a sphere of twenty-four jittering bolts.",
                "Hold sneak to charge, release for the radial burst.");
        register(
                "Lightning",
                BendingElement.LIGHTNING,
                "Azula\\'s cold fire and Iroh\\'s storm rebalanced - charge the sky in your palms, then throw a bolt that chains, stuns, and crowns creepers.",
                "Hold sneak to charge, release to strike down your gaze.");
        register(
                "Electrify",
                BendingElement.LIGHTNING,
                "Make the lake itself bite - click water or bare metal and everything sloshing through learns manners, except fellow lightningbenders.",
                "Left-click water or metal to electrify it.");
        register(
                "CombustBeam",
                BendingElement.COMBUSTION,
                "The third eye, bargain version - longer you hold, the wider the torrent, and flinching sets it off in your own face.",
                "Hold sneak to charge (watch the %). Release to fire; steer a little by looking.");
        register(
                "Explode",
                BendingElement.COMBUSTION,
                "Point at it, hiss, and stop pointing - a clean radial detonation that takes nothing from the world but lives.",
                "Hold sneak to paint the target, release to detonate.");
        register(
                "CombustionBlast",
                BendingElement.COMBUSTION,
                "JedCore\\'s long eye - charge the ring, loose the spark-beam that steers with your gaze, and take cover from your own misfire.",
                "Hold sneak to charge, release to fire the guided beam. Taking a hit while charging sets it off in your face.");
        register(
                "Combustion",
                BendingElement.COMBUSTION,
                "Sparky Sparky Boom Man needed no stance, no fist - only the eye. Loose the beam, and click to decide exactly where it ends.",
                "Sneak to fire the beam. Left-click mid-flight to detonate it early.");
        register(
                "AirJet",
                BendingElement.AIR,
                "Air is freedom, and none proved it like Zaheer, who severed his earthly tether - or Aang, who rode the winds across the world. AirJet wraps you in a rushing current and hurls you where you look.",
                "Left-click to ride the current. Click again mid-flight to step out of it.");
        register(
                "AirScooter",
                BendingElement.AIR,
                "Aang\\'s favorite ride - a spinning ball of air fast enough to outrun anything, even up walls and across the sea.",
                "Sprint, leap into the air, and left-click to mount. Sneak to dismount.");
        register(
                "AirSpout",
                BendingElement.AIR,
                "Aang\\'s elevator - a roaring column of wind that lifts its rider to the rooftops and beyond.",
                "Left-click to rise on the column. Hold space to climb. Click again to step off.");
        register(
                "AirPunch",
                BendingElement.AIR,
                "Jab-jab-jab - JedCore\\'s flurry of small bolts. Keep clicking and the volley keeps coming until the window lapses.",
                "Left-click to punch bolts of wind. Rapid clicks extend the flurry.");
        register(
                "AirSlam",
                BendingElement.AIR,
                "Pop them up, spike them down your gaze - JedCore\\'s two-beat bully combo in a single click.",
                "Left-click a target in range to pop then spike it.");
        register(
                "AirFlight",
                BendingElement.AIR,
                "Zaheer severed his earthly tether and never touched ground again. Leap, click, and the sky takes you - ram through anything too slow to dodge.",
                "Click mid-air to fly. Scroll hotbar 1-4: Soar, Glide, Levitate, Ending. Left-click in Soar to cycle speed. Land, touch water, or click again to land it.");
        register(
                "AirShield",
                BendingElement.AIR,
                "When cornered, Aang wrapped himself in a spinning sphere of wind that turned away flame and stone alike. The shield does not block - it refuses to be touched.",
                "Hold sneak to raise the shield. Release to drop it - ending early shortens the cooldown.");
        register(
                "AirBurst",
                BendingElement.AIR,
                "When Aang came down like a comet, the air itself broke his fall and threw the Fire Nation back. The burst does not strike - everything around it simply stops being there.",
                "Hold sneak to charge (keep holding once charged). Release for a sphere burst, or left-click while charged for a cone. Hard landings erupt on their own.");
        register(
                "AirBlast",
                BendingElement.AIR,
                "Aang\\'s first lesson - a punch of compressed air that hurls enemies back, snuffs flame, and slams doors. A master can even bend its path mid-flight.",
                "Sneak to fire. Left-click to mark a distant origin first and launch from there. Sneak mid-flight to steer.");
        register(
                "AirSwipe",
                BendingElement.AIR,
                "Katara\\'s razor rain answered by wind - Yangchen\\'s sweeping arcs cut across battlefields, and a charged swipe hits like a falling sky.",
                "Left-click for an instant fan. Hold sneak to charge, release for up to double damage and push.");
        register(
                "AirSuction",
                BendingElement.AIR,
                "Zaheer did not chase - he pulled. Hold your ground and the world comes to you: mobs, items, and players alike, dragged down your gaze.",
                "Hold sneak to vacuum everything in front of you toward yourself. Release to let go.");
        register(
                "Suffocate",
                BendingElement.AIR,
                "Zaheer\\'s final lesson - the air turns traitor inside the lungs. Hold a victim in your gaze and the breath simply stops coming.",
                "Hold sneak on a target to channel. Looking away, releasing, or taking a hit breaks it.");
        register(
                "Tornado",
                BendingElement.AIR,
                "Aang\\'s storm incarnate - a walking funnel that hurls friend and foe skyward alike. Even the rider answers to the winds.",
                "Hold sneak to raise the funnel at your cursor. Release or run out the clock to end it.");
        register(
                "AirBreath",
                BendingElement.AIR,
                "Aang\\'s bison-sized exhale - one long breath that batters a hallway clean, snuffs every flame, and lends breath to the drowning.",
                "Hold sneak to breathe out. Aim down at a wall to blast yourself backwards.");
        register(
                "AirBullet",
                BendingElement.AIR,
                "A single breath folded a hundred times - the air spirals in, kneels into the palm, and waits. What leaves the hand is not wind but a verdict.",
                "Hold sneak to gather the spiral into your hand. Release once concentrated, then left-click to fire.");
        register(
                "Meditate",
                BendingElement.AIR,
                "The monks sat still while storms passed through them. Gather focus, release it, and walk away faster, springier, and harder to kill.",
                "Hold sneak to focus. Release after the warmup for blessings; taking a hit breaks focus.");
        register(
                "SonicBlast",
                BendingElement.AIR,
                "JedCore\\'s scream given shape - a charged ring-wave that scrambles everything it touches into nausea and dark.",
                "Hold sneak to charge the scream, release to loose the ring.");
        register(
                "Zephyr",
                BendingElement.AIR,
                "Fall like thistledown - a ring of gentle currents that hands everything at your level a feather-slow descent.",
                "Hold sneak to stir the ring.");
        register(
                "AirStream",
                BendingElement.AIR,
                "Ride the river in the sky - a steerable stream head that grabs whatever it touches and carries it along your gaze.",
                "Hold sneak to send and steer the stream. Release to let go.");
        register(
                "AvatarState",
                BendingElement.AVATAR,
                "When Aang\\'s eyes glowed, the voices of ten thousand Avatars spoke as one. The Avatar State is the ultimate defense - every past life lending its power at once.",
                "Left-click to enter the state. All cooldowns vanish while it lasts. Click again to release it.");
        register(
                "ElementSphere",
                BendingElement.AVATAR,
                "JedCore\\'s masterpiece: sneak to surround yourself with a spinning sphere of all four elements that lifts you skyward. Click with hotbar slots 1-5 to hurl Air, Earth, Fire, Water, or the devastating Stream. Sneak twice to dismiss it.",
                "Sneak to summon and fly (hold sneak to steer). Click slots 1-5 for sub-attacks. Double-sneak to cancel.");
        register(
                "SpiritBeam",
                BendingElement.AVATAR,
                "Kuvira forged wild spirit energy into a cannon that shook Republic City. Korra answered in kind - a violet beam of pure spirit, burning all it touches.",
                "Hold sneak to pour the beam from your chest. Release to end it.");
        register(
                "SpiritGrasp",
                BendingElement.SPIRITUAL,
                "The vines remember every war - call them up and they will hold your enemies the way they once held Republic City.",
                "Left-click ground to root the snare. Everything inside slows and withers.");
        register(
                "SpiritProjection",
                BendingElement.SPIRITUAL,
                "Jinora walked the world without moving - hold still until the spirit slips out, then drift the tether while your double wears your skin.",
                "Hold sneak 5s to project (spectator, tethered). Drift home and left-click your double to return; any hit on it snaps you back.");
        register(
                "SpiritStep",
                BendingElement.SPIRITUAL,
                "Jinora crossed battlefields in a blink, there and gone between heartbeats with only wisps to say she\\'d passed.",
                "Left-click ground in range to step there.");
        register(
                "Accretion",
                BendingElement.EARTH,
                "Slam the earth to send blocks into the air, then shoot them all towards a single point! They will build up on an enemy, damaging and slowing them down! Each block that hits adds 1 second and level of slowness.",
                "Sneak to rise blocks, Left Click before they land to shoot!");
        register(
                "Catapult",
                BendingElement.EARTH,
                "Catapult is an advanced earthbending ability that allows you to forcefully push yourself using earth, reaching great heights. This technique is best used when travelling, but it can also be used to quickly escape a battle.",
                "Hold sneak until you see particles and hear a sound and then release to be propelled in the direction you\\'re looking. Additionally, you can left-click to be propelled with less power.");
        register(
                "EarthBlast",
                BendingElement.EARTH,
                "EarthBlast is a basic yet fundamental earthbending ability. It allows you to deal rapid fire damage to your target to finish low health targets off or deal burst damage to them. Although it can be used at long range, it\\'s potential is greater in close ranged combat.",
                "Tap sneak at an earthbendable block and then left click in a direction to send an earthblast. Additionally, you can left click again to change the direction of the earthblast. You can also redirect other earthbender\\'s earth blast by left clicking. If the earth blast hits an entity it will deal damage and knockback.");
        register(
                "EarthArmor",
                BendingElement.EARTH,
                "This ability encases the Earthbender in armor, giving them protection. It is a fundamental earthbending technique that\\'s used to survive longer in battles.",
                "Tap sneak while looking at an earthbendable block to bring those blocks towards you, forming earth armor. This ability will give you extra hearts and will be removed once those extra hearts are gone. You can disable this ability by holding sneak and left clicking with EarthArmor.");
        register(
                "CollapseWall",
                BendingElement.EARTH,
                "This ability is a basic earthbending ability that allows the earthbender great utility. It allows them to control earth blocks by compressing earth. Players and mobs can be trapped and killed if earth is collapsed and they\\'re stuck inside it, meaning this move is deadly when in cave systems.",
                "Left click an earthbendable block. If there\\'s space under that block, it will be collapsed. Alternatively, you can tap sneak to collapse multiple blocks at a time.");
        register(
                "RaiseEarth",
                BendingElement.EARTH,
                "RaiseEarth is a basic yet useful utility move. It has the potential to allow the earthbender to create great escape routes by raising earth underneath them to propel themselves upwards. It also offers synergy with other moves, such as shockwave. RaiseEarth is often used to block incoming abilities.",
                "(Pillar) To raise a pillar of earth, left click on an earthbendable block.\\n(Wall) To raise a wall of earth, tap sneak on an earthbendable block.");
        register(
                "Shockwave",
                BendingElement.EARTH,
                "Shockwave is one of the most powerful earthbending abilities. It allows the earthbender to deal mass damage to everyone around them and knock them back. It\\'s extremely useful when fighting more than one target or if you\\'re surrounded by mobs.",
                "Hold sneak until you see particles and then release sneak to send a wave of earth outwards, damaging and knocking entities back that it collides with. Additionally, instead of releasing sneak you can send a cone of earth forwards by left clicking. If you are on the Shockwave slot and you fall from a great height, your Shockwave will automatically activate.");
        register(
                "EarthKick",
                BendingElement.EARTH,
                "Earthbenders can kick the earth in front of them and send shards flying towards their enemies.",
                "Sneak at earth in front of you");
        register(
                "Extraction",
                BendingElement.METAL,
                "This ability allows metalbenders to extract the minerals from ore blocks. This ability is extremely useful for gathering materials as it has a chance to extract double or triple the ores.",
                "Tap sneak while looking at an earthbendable ore to extract the ore.");
        register(
                "MetalClips",
                BendingElement.METAL,
                "MetalClips is an advanced metalbending ability that allows you to take control of a fight. It gives the metalbender the ability to control an entity, create space between them and a player and even added utility.",
                "(Clips) This ability requires iron ingots in your inventory. Left click to throw an ingot at an entity, dealing damage to them. This ingot will form into armor, wrapping itself around the entity. Once enough armor pieces are around the entity, you can then control them. To control them, hold sneak while looking at them and then they will be moved in the direction you look. Additionally, you can release sneak to throw them in the direction you\\'re looking.\\n(Magnet) Hold sneak with this ability to pull iron ingots towards you.");
        register(
                "LavaFlow",
                BendingElement.LAVA,
                "LavaFlow is an extremely advanced, and dangerous ability. It allows the earthbender to create pools of lava around them, or to solidify existing lava. This ability can be deadly when comboed with EarthGrab.",
                "(Flow) Hold sneak and lava will begin expanding outwards. Once the lava has stopped expanding, you can release sneak. Additionally, if you tap sneak the lava you created will revert back to the earthbendable block.\\n(Lava Pool) Left click to slowly transform earthbendable blocks into a pool of lava.\\n(Solidify) Left click on lava to solidify it, turning it to stone.");
        register(
                "LavaSurge",
                BendingElement.LAVA,
                "Throw a surging wave of lava in the direction you are looking!",
                "Sneak to create your lava source (or select an existing source) and click to throw the wave!");
        register(
                "QuickWeld",
                BendingElement.EARTH,
                "Advanced metalbenders can use this to repair damaged iron weapons/armor/tools. This ability requires iron ingots in your inventory to work.",
                "Sneak with the item you want to repair in your main hand.");
        register(
                "RockSlide",
                BendingElement.EARTH,
                "Slide over the earth using loose chunks of rock",
                "Shockwave (hold sneak) > Shockwave (right click block) > EarthSmash (release sneak)");
        register(
                "Shrapnel",
                BendingElement.EARTH,
                "Use your metalbending to throw nuggets of gold and iron like pieces of shrapnel, dealing damage when they hit entities. This requires that you have gold or iron nuggets in your inventory to launch!",
                "Click to shoot a single piece of shrapnel at high velocity to the targeted location, click while sneaking to launch several shotgun-style.");
        register(
                "EarthPillar",
                BendingElement.EARTH,
                "With this ability bound, tap Sneak (Default: Shift) on any Earthbendable surface to create pillar of earth in the direction of the block face!",
                "Bind it and bend to learn its ways.");
        register(
                "EarthShard",
                BendingElement.EARTH,
                "EarthShard is a variation of EarthBlast which the earthbender may use to hit a target. This ability deals a fair amount of damage and is easy to rapid-fire. To use, simply shift at an earthbendable block, and it will ascend to your eye height. Then, click towards your target and the block will launch itself towards it.",
                "Bind it and bend to learn its ways.");
        register(
                "Fissure",
                BendingElement.EARTH,
                "Fissure is an advanced Lavabending ability enabling a lavabender to tear up the ground, swallowing up any enemies. To use, simply swing at an enemy and a line of lava will crack open. Then, tap Sneak (Default: Shift) to expand the crevice. The crevice has a maximum width and depth. Once the crevice has reached it\\'s maximum width, Sneak while looking at the crevice to close it!",
                "Bind it and bend to learn its ways.");
        register(
                "LavaDisc",
                BendingElement.LAVA,
                "Hold Sneak (Default: Shift) on a lava source block to generate a disc of lava at your finger tips. Releasing Sneak will shoot the disc off in the direction you are looking! If you tap or hold Sneak again, the disc will attempt to return to you!",
                "Bind it and bend to learn its ways.");
        register(
                "LavaFlux",
                BendingElement.LAVA,
                "This offensive ability enables a Lavabender to create a wave of lava, swiftly progressing forward and hurting/burning anything in its way. To use, simply swing your arm towards a target and the ability will activate.",
                "Bind it and bend to learn its ways.");
        register(
                "MagnetShield",
                BendingElement.METAL,
                "Repel any metal projectiles using a strong magnetic shield. To activate, simply hold sneak with this ability bound.",
                "Bind it and bend to learn its ways.");
        register(
                "MetalFragments",
                BendingElement.METAL,
                "MetalFragments allows you to select a source and shoot multiple fragments of metal out of that source block towards your target, injuring them on impact. To use, tap Sneak (Default: Shift) at a metal source block and it will float up. Then, turn around and click at your target to fling metal fragments at them.",
                "Bind it and bend to learn its ways.");
        register(
                "MetalHook",
                BendingElement.METAL,
                "This ability lets a Metalbender bend metal into grappling hooks, allowing them to easily manouver terrain. To use this ability, the user must either have Iron in their inventory or be wearing an Iron/Chainmail Chestplate. Left-Click in the direction you are looking to fire a grappling hook, several hooks can be active at once, allowing the bender to \\'hang\\' in locations. To disengage the hooks, hold Shift (Default: Sneak) or Sprint.",
                "Bind it and bend to learn its ways.");
        register(
                "MudSurge",
                BendingElement.EARTH,
                "This ability lets an earthbender send a surge of mud in any direction, knocking back enemies and dealing moderate damage. This ability has a chance of blinding the target. To use, select a source of earth and click in any direction.",
                "Bind it and bend to learn its ways.");
        register(
                "LavaDisk",
                BendingElement.LAVA,
                "Tap sneak to select a nearby earth or lava source. This disk made of molten earth will destroy any earthbendable and any soft materials it comes in contact with. The closer you are to the LavaDisk the faster it spins and the more damage it deals.",
                "Bind it and bend to learn its ways.");
        register(
                "EarthGlove",
                BendingElement.EARTH,
                "Dai Li agents use this technique for various purposes. Left click to launch your glove and attempt to grab your target. If you are holding sneak, the gloves will attempt to return to you. You can also destroy other players\\' gloves by tapping sneak while looking at them.",
                "Bind it and bend to learn its ways.");
        register(
                "EarthDome",
                BendingElement.EARTH,
                "EarthDome allows earthbenders to surround themselves or another entity in earth, temporarily preventing anything from entering or escaping the dome.",
                "(Self) RaiseEarth (Right click) > Shockwave (Right click)\\n(Projection) RaiseEarth (Right click) > Shockwave (Left click)");
        register(
                "Dig",
                BendingElement.EARTH,
                "Swim through the earth, digging a path with your earthbending. Inspired by toph\\'s learning from the badgermoles! You must also be looking at an earthbendable block for the ability to work!",
                "Sneak while on an earthbendable block");
        register(
                "EarthTunnel",
                BendingElement.EARTH,
                "Earth Tunnel is a completely utility ability for earthbenders. It allows you to dig a hole that lowers players down while you continue the ability, create fast escape routes or just great for making your own cave systems.",
                "Hold sneak while looking at an earthbendable block to tunnel the blocks away. If you release sneak or look at a block that isn\\'t earthbendable, the ability will cancel.");
        register(
                "EarthSurf",
                BendingElement.EARTH,
                "This ability allows an earth bender to ride up on a wave of earth, allowing them to travel a little faster than normal. To use, simply be in the air just above the ground, and Left Click! Additionally, if an entity just so happens to get caught in the wave, they will be moved with the wave.",
                "Bind it and bend to learn its ways.");
        register(
                "EarthGrab",
                BendingElement.EARTH,
                "EarthGrab is one of the best defence abilities in an earthbender\\'s arsenal. It allows you to trap someone who is running away so that you can catch up to someone. It is also of great utility use to an earthbender. It can be used to drag items, arrows, and crops that are on earthbendable blocks towards you, saving you the time of running to get them.",
                "(Grab) To grab an entity, left click in the direction of the target. Your power will be sent through the earth, and then it will reach up and root them in their spot upon contact. The ability can be manually be disabled by sneaking or clicking again on the EarthGrab slot.\\n(Drag) To drag items towards you, sneak\\n(Escaping) To escape, the trap must be destroyed or the user damaged. The trap can be destroyed by damage or the trapped entity right-clicking it a certain number of times. Additionally, forcefully moving the entity with another earth ability destroys the trap.");
    }

    private static void register(String name, BendingElement element, String description, String usage) {
        BY_NAME.put(name.toLowerCase(java.util.Locale.ROOT), new Info(name, element, description, usage));
    }

    public static Info get(String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase(java.util.Locale.ROOT));
    }

    public static List<Info> ofElement(BendingElement element) {
        List<Info> out = new ArrayList<>();
        for (Info info : BY_NAME.values()) {
            if (info.element() == element) {
                out.add(info);
            }
        }
        return out;
    }

    /** Every recorded ability name, for command suggestions. */
    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (Info info : BY_NAME.values()) {
            names.add(info.name());
        }
        return names;
    }
}
