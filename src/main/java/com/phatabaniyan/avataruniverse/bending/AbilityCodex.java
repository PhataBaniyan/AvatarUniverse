package com.phatabaniyan.avataruniverse.bending;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-source codex of bending-ability description + usage text, written originally
 * for AvatarUniverse. Ability mechanics take inspiration from ProjectKorra,
 * ProjectAvatar, ProjectAddons, JedCore and Hyperion (see CREDITS.md), but all
 * text below is original — nothing copied from those projects.
 */
public final class AbilityCodex {
    private AbilityCodex() {}

    public record Info(String name, BendingElement element, String description, String usage) {}

    private static final Map<String, Info> BY_NAME = new LinkedHashMap<>();

    static {
        register(
                "WaterManipulation",
                BendingElement.WATER,
                "The first lesson of every waterbender: seize a nearby water source and hurl it as a guided bolt. Fast, cheap, and spammable — ideal for finishing weakened targets.",
                "Sneak while facing a water source, then click to launch the bolt toward your cursor. Click again mid-flight to redirect it — including bolts thrown by other benders.");
        register(
                "Torrent",
                BendingElement.WATER,
                "A ring of living water raised around the bender that can be fired as a crushing stream, burst outward as a wave, or flash-frozen solid on impact.",
                "(Torrent) Click a water source and hold sneak to shape the ring, then click to fire it down your gaze — click once more before impact to freeze it.\\n(Wave) Shape the ring, then release sneak to blast a wave outward in every direction, hurling entities back.");
        register(
                "WaterSpout",
                BendingElement.WATER,
                "A travelling column of water that lifts its rider above seas and battlefields — the waterbender's answer to flight, with a wave to ride when there is somewhere to be.",
                "(Spout) Click to raise the column; hold jump to climb, sneak to sink, click again to step off.\\n(Wave) Click a water source and hold sneak until water gathers around you, then release to ride the wave forward; click with WaterSpout bound to cancel.");
        register(
                "WaterArms",
                BendingElement.WATER,
                "Twin tendrils of water grafted to the bender's arms that punch, pull, grapple, grab, and freeze — the most versatile tool in a waterbender's arsenal.",
                "Tap sneak at a water source to grow the arms; hold sneak and click to dismiss.\\n(Pull) Click a target to drag it toward you.\\n(Punch) Click to lash an arm out for damage.\\n(Grapple) Click terrain to yank yourself to it.\\n(Grab) Click an entity to seize and steer it; click again to throw.\\n(Freeze) Click to spray chilling ice shards.\\n(Spear) Click to fire a freezing ice spear.");
        register(
                "HealingWaters",
                BendingElement.HEALING,
                "Draw on water's restorative nature to knit wounds closed — your own, or an ally's, so long as water is close at hand.",
                "Hold sneak while in or near water to heal yourself, or hold sneak while facing a hurt ally to channel healing into them. Bottled water works when no source is near.");
        register(
                "WaterBubble",
                BendingElement.WATER,
                "Push the surrounding water back into a pocket of air that travels with you — for diving deep, building dry, and fighting beneath the waves.",
                "Click for a short-lived bubble, or hold sneak with it bound to carry one that follows you. Click again nearby to refresh it; it melts when released or left behind.");
        register(
                "FrostBreath",
                BendingElement.ICE,
                "Exhale winter itself: a freezing cone that slows, harms, and encases victims in a cage of ice.",
                "Hold sneak while facing your target to breathe frost over them.");
        register(
                "IceBlast",
                BendingElement.ICE,
                "A fast shard of ice shaped from a frozen source — quick to loose and deadly against fleeing targets.",
                "Tap sneak while facing ice, then click to fire the blast down your gaze.");
        register(
                "IceSpike",
                BendingElement.ICE,
                "Two faces of ice: hurl a steerable blast, or erupt a pillar of spikes from frozen ground that launches whatever it catches skyward.",
                "(Blast) Tap sneak at a water source, then click to fire; click mid-flight to steer the blast.\\n(Spike) While near ice, tap sneak without facing ice or water to erupt pillars that fling caught targets upward — or click an ice block directly to raise a single spike.");
        register(
                "PhaseChange",
                BendingElement.WATER,
                "Walk on water by freezing it beneath your feet — or melt ice back to water. Reshape the surface of the battlefield at will.",
                "(Freeze) Click water to freeze a platform that holds while you stay near and melts once you leave.\\n(Melt) Hold sneak facing ice to melt it back to water.");
        register(
                "Bloodbending",
                BendingElement.BLOOD,
                "The forbidden art: seize the water inside a living body and puppet its movements. Fellow bloodbenders resist your grip, as you resist theirs.",
                "(Control) Hold sneak while facing an entity to steer it with your gaze.\\n(Throw) Click while holding to hurl the victim where you look.");
        register(
                "BloodPuppet",
                BendingElement.BLOOD,
                "A master's refinement of bloodbending: seize a victim's limbs and force them to strike a target of your choosing.",
                "Sneak while facing a mob or player to take hold of it; click to make it attack. Release sneak to let go. No cooldown, but may be night-only depending on configuration.");
        register(
                "Drain",
                BendingElement.WATER,
                "Wring water from leaves, open sources, and even falling rain — fill your bottles with it, or shape the dregs into stinging blasts.",
                "Sneak near plants to draw water into empty bottles or buckets you carry; with nothing left to fill, the gathered water fires off as small blasts instead. Works on rain and plain water sources too.");
        register(
                "IceClaws",
                BendingElement.ICE,
                "Condense moisture from thin air into claws of ice for fast melee strikes that chill whatever they rake.",
                "Hold sneak to form the claws at your fingertips, then strike enemies to damage and slow them; the charged claws can also be hurled.");
        register(
                "IceWall",
                BendingElement.ICE,
                "Raise a curved rampart of ice from water, ice, or snow — cover that bursts into damaging shards when it falls.",
                "Sneak facing water, ice, or snow to raise the wall; sneak facing the wall to collapse it. Rival icebenders can break your walls, and standing too close to a collapse hurts.");
        register(
                "WakeFishing",
                BendingElement.WATER,
                "Stir a watery wake and hold it steady until curious fish swim out to meet you.",
                "Hold sneak facing a water block without looking away; keep the wake alive and fish will eventually investigate it.");
        register(
                "RazorLeaf",
                BendingElement.PLANT,
                "Spin leaves into a razor-edged disc you can aim, loose, and call back to your hand.",
                "Sneak at plants to spin up the disc, hold to aim, release to throw — sneak again to pull it back toward you.");
        register(
                "IceCrawl",
                BendingElement.ICE,
                "Send a narrow runner of ice racing along the ground that bites the feet of whatever it reaches, rooting them in place.",
                "Tap sneak at a water or ice source, then click to launch the crawl down your gaze.");
        register(
                "PlantArmor",
                BendingElement.PLANT,
                "Wrap yourself in living vines and leaves: protection from falls and drowning, boosts to speed, leap, and swimming — and the armor itself becomes a source for vine sub-techniques.",
                "Sneak to grow the armor.\\nClick for VineWhip, Tangle, Leap, Grapple, Disperse.\\nHold sneak for RazorLeaf, LeafShield, LeafDome, Regenerate.");
        register(
                "LeafStorm",
                BendingElement.PLANT,
                "A PlantArmor-only combo: shed your leaves as a whirling storm that shreds whatever it touches until it lands or you release.",
                "While wearing PlantArmor: double-click with RazorLeaf bound, then hold sneak with VineWhip bound.");
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
                "Slam the earth to throw blocks skyward, then drive them all onto a single point where they pile onto the victim — every block adding hurt and slowness.",
                "Sneak to blast blocks upward, then click before they land to hurl the cluster at your target.");
        register(
                "Catapult",
                BendingElement.EARTH,
                "Kick the earth itself to launch skyward — a traveller's leap and a duelist's escape in one.",
                "Hold sneak until particles gather and you hear the charge build, then release to launch where you look; click without charging for a weaker hop.");
        register(
                "EarthBlast",
                BendingElement.EARTH,
                "The earthbender's bread and butter: rip stone free and fire it as a rapid, redirectable bolt — strongest up close.",
                "Tap sneak at bendable earth, then click to fire; click mid-flight to redirect — including rival benders' blasts, which you can turn back on them.");
        register(
                "EarthArmor",
                BendingElement.EARTH,
                "Call stone around your body as ablative armor that soaks up hits in place of your health.",
                "Tap sneak facing bendable earth to plate yourself in extra hearts; hold sneak and click with it bound to shed the armor early.");
        register(
                "CollapseWall",
                BendingElement.EARTH,
                "Compress the ground downward into pits and cave-ins — deadly work where the ceiling runs low and anything caught inside is crushed.",
                "Click bendable earth to drop it where there is space below; tap sneak to collapse a wider area at once.");
        register(
                "RaiseEarth",
                BendingElement.EARTH,
                "Lift the ground into pillars for escape routes and high ground, or raise walls to block incoming attacks and combo with shockwaves.",
                "(Pillar) Click bendable earth to raise a column beneath the target.\\n(Wall) Tap sneak on bendable earth to raise a wall.");
        register(
                "Shockwave",
                BendingElement.EARTH,
                "Stomp a ring of shattering earth outward that batters everything around you — or focus it into a forward cone. It even breaks your own falls.",
                "Hold sneak until particles gather, then release for the radial wave or click for a cone. Falling far with it bound triggers it automatically to cushion the landing.");
        register(
                "EarthKick",
                BendingElement.EARTH,
                "A short-range kick that sprays rock shards into whatever stands ahead of you.",
                "Sneak while facing bendable earth ahead to kick up the shard fan.");
        register(
                "Extraction",
                BendingElement.METAL,
                "Coax raw ore out of stone — a miner's trick that sometimes shakes loose double or triple yield.",
                "Tap sneak while facing an ore vein to extract its minerals.");
        register(
                "MetalClips",
                BendingElement.METAL,
                "Throw iron that wraps victims in binding bands, then puppet the bound — or call loose metal back to your hand.",
                "(Clips) Click while carrying iron ingots to throw bands that sear, armor, and seize an entity; hold sneak to steer it, release to hurl it.\\n(Magnet) Hold sneak to draw loose ingots toward you.");
        register(
                "LavaFlow",
                BendingElement.LAVA,
                "Bloom pools of lava outward across stone — or cool existing lava back to rock. Devastating when paired with grabs.",
                "(Flow) Hold sneak to spread lava outward; tap sneak to revert your flows to earth.\\n(Pool) Click earth to melt it into a lava pool.\\n(Solidify) Click lava to freeze it into stone.");
        register(
                "LavaSurge",
                BendingElement.LAVA,
                "Gather molten rock and throw it as a rolling surge down your gaze.",
                "Sneak to gather a lava source — or select an existing one — then click to hurl the wave.");
        register(
                "QuickWeld",
                BendingElement.EARTH,
                "Mend damaged iron gear in the field by flowing fresh metal into the cracks.",
                "Hold the item to repair in your main hand and sneak; iron ingots are consumed from your inventory.");
        register(
                "RockSlide",
                BendingElement.EARTH,
                "Ride loose chunks of rock across the ground — a combo that turns a shockwave into a slide.",
                "Charge Shockwave (hold sneak), trigger it against a block (click), then release into EarthSmash to slide.");
        register(
                "Shrapnel",
                BendingElement.EARTH,
                "Spend carried gold and iron nuggets as a hail of metal shot — single precise shots, or a sneaking scattergun blast.",
                "Click to fire one nugget at high velocity; sneak-click to loose a shotgun spread. Requires nuggets in your inventory.");
        register(
                "EarthPillar",
                BendingElement.EARTH,
                "Extrude a pillar of earth out of any bendable face — floor, wall, or ceiling.",
                "Tap sneak on a bendable surface to grow a pillar along the face direction.");
        register(
                "EarthShard",
                BendingElement.EARTH,
                "A lighter cousin of EarthBlast: lift a stone to eye height, then fling it — easy to rapid-fire.",
                "Sneak at bendable earth to raise a shard, then click toward your target to launch it.");
        register(
                "Fissure",
                BendingElement.EARTH,
                "Tear a lava-filled crevice through the ground that swallows enemies — then seal it shut over them.",
                "Click toward an enemy to crack the ground open; tap sneak to widen the fissure, and sneak facing it at full width to close it.");
        register(
                "LavaDisc",
                BendingElement.LAVA,
                "Spin lava into a returning disc at your fingertips that burns down your gaze — and comes back when called.",
                "Hold sneak on a lava source to shape the disc; release to throw it. Sneak again to recall it to your hand.");
        register(
                "LavaFlux",
                BendingElement.LAVA,
                "Loose a fast wave of lava that rolls forward, burning and battering everything in its path.",
                "Click toward your target to send the flux rolling.");
        register(
                "MagnetShield",
                BendingElement.METAL,
                "A magnetic ward that turns aside incoming metal projectiles before they reach you.",
                "Hold sneak with it bound to raise the shield.");
        register(
                "MetalFragments",
                BendingElement.METAL,
                "Levitate metal from a source, then fling a fan of sharp fragments that injure whatever they strike.",
                "Tap sneak at a metal source to lift it, then click your target to shred it with fragments.");
        register(
                "MetalHook",
                BendingElement.METAL,
                "Bend grappling hooks from carried iron to swing across terrain — several hooks can hold at once, letting you hang mid-air.",
                "Click to fire a hook where you look (needs iron carried, or an iron/chainmail chestplate worn). Hold sneak or sprint to release. Multiple hooks can anchor you at once.");
        register(
                "MudSurge",
                BendingElement.EARTH,
                "Hurl a surge of blinding mud that knocks foes back and wounds them.",
                "Select an earth source, then click in any direction to send the surge.");
        register(
                "LavaDisk",
                BendingElement.LAVA,
                "A spinning disk of molten earth that chews through soft ground — deadliest up close, where it spins fastest and hits hardest.",
                "Tap sneak near an earth or lava source to shape the disk.");
        register(
                "EarthGlove",
                BendingElement.EARTH,
                "The Dai Li's signature trick: launch stone gauntlets to seize targets at range — or smash rivals' gloves out of the air.",
                "Click to launch a glove at your target; hold sneak to reel your gloves back. Sneak while facing another player's glove to destroy it.");
        register(
                "EarthDome",
                BendingElement.EARTH,
                "Seal yourself — or a victim — inside an earthen dome that nothing crosses.",
                "(Self) RaiseEarth (click) followed by Shockwave (click) at your feet.\\n(Projection) Aim the Shockwave click at another entity to entomb them instead.");
        register(
                "Dig",
                BendingElement.EARTH,
                "Swim through stone, carving tunnels as you go — the badgermoles' own way of travel.",
                "Sneak while facing bendable earth to burrow through it.");
        register(
                "EarthTunnel",
                BendingElement.EARTH,
                "Bore a descending shaft for escapes, ambushes, or brand-new cave systems.",
                "Hold sneak while facing bendable earth to drill downward; release, or face non-earth, to stop.");
        register(
                "EarthSurf",
                BendingElement.EARTH,
                "Catch a wave of earth and ride it — anything caught in the crest rides along with you.",
                "While airborne just above the ground, click to mount the wave.");
        register(
                "EarthGrab",
                BendingElement.EARTH,
                "Send your will through the ground to root fleeing enemies in stone — or drag loose items to your feet.",
                "(Grab) Click toward a target to trap it where it stands; sneak or click again to release. Victims escape by breaking the trap, being force-moved, or striking it repeatedly.\\n(Drag) Sneak to pull items, arrows, and crops resting on earth toward you.");
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
