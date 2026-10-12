package anthony.flagwars.resources;

import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWTeam;
import anthony.flagwars.map.FWMapConfig;
import anthony.flagwars.map.FWTeamSite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ticking generator that drops physical resource items at each team's base
 * (iron/gold) and at the map's shared mid points (emerald/diamond). Players
 * pick these up normally into their inventory - there's no hidden currency,
 * see FWEconomy. Uses plain dropItem (not dropItemNaturally) with velocity
 * zeroed so the item sits exactly where it's generated instead of scattering.
 * Owns a FWGeneratorDisplay for the cosmetic spinning block + hologram above
 * each emerald/diamond spot, fed the live "Regenerates In" countdown here.
 */
public class FWResourceGenerator {

	private static final int PARTICLE_WARNING_SECONDS = 3;

	/**
	 * Real Hypixel Bedwars timing, re-confirmed against the official wiki's own wording ("a
	 * steady flow") plus several independent forum threads: both base generators run at 2x rate
	 * for the first 30 seconds of the match, then drop to one constant (Forge-tier-dependent) rate
	 * for the rest of the game - there's no further, ongoing slowdown after that single cutover.
	 * With no Forge bought: gold goes from 5s to 10s per ingot, iron from 1s to 2s (see
	 * FWTeamUpgradeManager#goldIntervalSecondsFor/ironIntervalSecondsFor) - iron's amount per drop
	 * (ironAmountFor) is untouched by this, only how often it drops.
	 */
	private static final int EARLY_GAME_BOOST_SECONDS = 30;

	/**
	 * Real Hypixel Bedwars timings: both emerald and diamond generators start at Tier I and
	 * automatically upgrade themselves (independent of any team's own purchases) at 6 and then
	 * 18 minutes into the match, topping out at Tier III - there's no Tier IV for either.
	 */
	private static final int TIER_2_AT_SECONDS = 6 * 60;
	private static final int TIER_3_AT_SECONDS = 18 * 60;

	private static final int[] DIAMOND_TIER_SECONDS = {30, 24, 12};
	private static final int[] EMERALD_TIER_SECONDS = {56, 40, 28};

	/**
	 * Caps on how much of each resource can be sitting unclaimed at a generator before it stops
	 * spawning more - iron/gold at 47/16 (Hypixel only raises these for the owning team's own base
	 * generator via Forge, which this doesn't replicate - flat caps here regardless of Forge tier).
	 * Diamond/emerald per the brief: 4 and 2.
	 */
	private static final int IRON_GROUND_CAP = 47;
	private static final int GOLD_GROUND_CAP = 16;
	private static final int EMERALD_GROUND_CAP = 2;
	private static final int DIAMOND_GROUND_CAP = 4;
	private static final double GROUND_CAP_CHECK_RADIUS = 3.0;

	private final FWGameInstance instance;
	private final FWGeneratorDisplay generatorDisplay;
	private BukkitTask task;
	private int elapsedSeconds = 0;
	/** Per-generator-id countdown, since a generator with a custom /fwsetup interval override ticks independently of the rest. */
	private final Map<Integer, Integer> emeraldCountdowns = new LinkedHashMap<>();
	private final Map<Integer, Integer> diamondCountdowns = new LinkedHashMap<>();
	private boolean doubleRate = false;

	public FWResourceGenerator(FWGameInstance instance) {
		this.instance = instance;
		this.generatorDisplay = new FWGeneratorDisplay(instance);
	}

	/** Applied once at match start when the lobby's "Generator Spawn Rate -> 2x" vote passes - stacks with whichever tier is currently active. */
	public void doubleSpawnRate() {
		doubleRate = true;
	}

	/** 1, 2, or 3 - same tier for both resources, just different per-tier intervals (see DIAMOND_TIER_SECONDS/EMERALD_TIER_SECONDS). */
	public int getCurrentTier() {
		if (elapsedSeconds >= TIER_3_AT_SECONDS) return 3;
		return elapsedSeconds >= TIER_2_AT_SECONDS ? 2 : 1;
	}

	/** Seconds until the next tier kicks in - same "time until the next Generator Upgrade" Hypixel's own sidebar shows - or -1 once already at Tier III, since there's nothing left to upgrade to. */
	public int getSecondsUntilNextTier() {
		if (elapsedSeconds < TIER_2_AT_SECONDS) return TIER_2_AT_SECONDS - elapsedSeconds;
		if (elapsedSeconds < TIER_3_AT_SECONDS) return TIER_3_AT_SECONDS - elapsedSeconds;
		return -1;
	}

	/** Soonest upcoming drop across every diamond generator on the map (they can differ once a custom interval override is set). */
	public int getDiamondSecondsLeft() {
		return diamondCountdowns.values().stream().mapToInt(Integer::intValue).min().orElse(0);
	}

	/** Soonest upcoming drop across every emerald generator on the map (they can differ once a custom interval override is set). */
	public int getEmeraldSecondsLeft() {
		return emeraldCountdowns.values().stream().mapToInt(Integer::intValue).min().orElse(0);
	}

	/** True for the first EARLY_GAME_BOOST_SECONDS of the match - see the field doc above. */
	private boolean earlyGameBoostActive() {
		return elapsedSeconds < EARLY_GAME_BOOST_SECONDS;
	}

	/**
	 * A /fwsetup-configured interval override is a flat, fixed rate - no tier scaling, no
	 * doubleRate vote, no early-game boost. It fully replaces the default timing rather than
	 * adjusting it, so a builder setting "diamond generator 1 interval 20" gets exactly 20s, always.
	 */
	private int diamondIntervalSeconds(int id) {
		Integer override = instance.getMapConfig().getDiamondGeneratorInterval(id);
		if (override != null) return override;
		int base = DIAMOND_TIER_SECONDS[getCurrentTier() - 1];
		return doubleRate ? Math.max(1, base / 2) : base;
	}

	private int emeraldIntervalSeconds(int id) {
		Integer override = instance.getMapConfig().getEmeraldGeneratorInterval(id);
		if (override != null) return override;
		int base = EMERALD_TIER_SECONDS[getCurrentTier() - 1];
		return doubleRate ? Math.max(1, base / 2) : base;
	}

	public void start() {
		if (task != null) return;
		FWMapConfig config = instance.getMapConfig();
		for (int id : config.getEmeraldGenerators().keySet()) {
			emeraldCountdowns.put(id, emeraldIntervalSeconds(id));
		}
		for (int id : config.getDiamondGenerators().keySet()) {
			diamondCountdowns.put(id, diamondIntervalSeconds(id));
		}
		generatorDisplay.start();
		task = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), this::tick, 0L, 20L);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		elapsedSeconds = 0;
		emeraldCountdowns.clear();
		diamondCountdowns.clear();
		generatorDisplay.stop();
	}

	private static final String[] TIER_ROMAN = {"I", "II", "III"};

	private void tick() {
		elapsedSeconds++;

		// Fires exactly once, the instant elapsedSeconds crosses each threshold - the actual
		// speed-up (diamondIntervalSeconds(id) reading the new tier) already happens automatically
		// next time each generator's countdown resets, this is just the player-facing announcement of it.
		if (elapsedSeconds == TIER_2_AT_SECONDS) {
			announceDiamondTierUpgrade(2);
		} else if (elapsedSeconds == TIER_3_AT_SECONDS) {
			announceDiamondTierUpgrade(3);
		}

		World world = instance.getWorld();
		FWMapConfig config = instance.getMapConfig();

		for (FWTeam team : instance.getTeams().values()) {
			if (team.isInactive() || team.isEmpty()) continue;

			FWTeamSite site = config.getTeamSite(team.getSiteId());

			// A custom /fwsetup override is a flat rate - it replaces the upgrade-based interval
			// entirely, so early-game boost (which only makes sense against the default timing)
			// doesn't apply on top of it either.
			Integer ironOverride = site.getIronGeneratorInterval();
			int ironInterval = ironOverride != null ? ironOverride : instance.getTeamUpgradeManager().ironIntervalSecondsFor(team);
			if (ironOverride == null && earlyGameBoostActive()) ironInterval = Math.max(1, ironInterval / 2);
			if (elapsedSeconds % ironInterval == 0 && site.getIronGenerator() != null) {
				Location ironLoc = site.getIronGenerator();
				if (groundCount(world, ironLoc, Material.IRON_INGOT) < IRON_GROUND_CAP) {
					int amount = instance.getTeamUpgradeManager().ironAmountFor(team);
					dropSplittable(world, ironLoc, new ItemStack(Material.IRON_INGOT, amount));
				}
			}

			Integer goldOverride = site.getGoldGeneratorInterval();
			int goldInterval = goldOverride != null ? goldOverride : instance.getTeamUpgradeManager().goldIntervalSecondsFor(team);
			if (goldOverride == null && earlyGameBoostActive()) goldInterval = Math.max(1, goldInterval / 2);
			if (elapsedSeconds % goldInterval == 0 && site.getGoldGenerator() != null) {
				Location goldLoc = site.getGoldGenerator();
				if (groundCount(world, goldLoc, Material.GOLD_INGOT) < GOLD_GROUND_CAP) {
					dropSplittable(world, goldLoc, new ItemStack(Material.GOLD_INGOT));
				}
			}
		}

		// Countdown-based (recomputed fresh from the *current* tier/override each time it resets)
		// rather than a fixed modulo against elapsedSeconds, so a tier change mid-cycle can't desync
		// it. Tracked per generator id since a custom interval override makes one generator's rate
		// independent of the rest of that resource type.
		for (Map.Entry<Integer, Location> entry : config.getEmeraldGenerators().entrySet()) {
			int id = entry.getKey();
			int countdown = emeraldCountdowns.getOrDefault(id, emeraldIntervalSeconds(id)) - 1;
			if (countdown <= 0) {
				Location dropLoc = FWGeneratorDisplay.blockCenterLocation(entry.getValue());
				if (groundCount(world, dropLoc, Material.EMERALD) < EMERALD_GROUND_CAP) {
					drop(world, dropLoc, new ItemStack(Material.EMERALD));
				}
				countdown = emeraldIntervalSeconds(id);
			}
			emeraldCountdowns.put(id, countdown);
			generatorDisplay.updateCountdown(FWResourceType.EMERALD, id, countdown);
			generatorDisplay.setParticlesActive(FWResourceType.EMERALD, id, countdown <= PARTICLE_WARNING_SECONDS);
		}

		for (Map.Entry<Integer, Location> entry : config.getDiamondGenerators().entrySet()) {
			int id = entry.getKey();
			int countdown = diamondCountdowns.getOrDefault(id, diamondIntervalSeconds(id)) - 1;
			if (countdown <= 0) {
				Location dropLoc = FWGeneratorDisplay.blockCenterLocation(entry.getValue());
				if (groundCount(world, dropLoc, Material.DIAMOND) < DIAMOND_GROUND_CAP) {
					drop(world, dropLoc, new ItemStack(Material.DIAMOND));
				}
				countdown = diamondIntervalSeconds(id);
			}
			diamondCountdowns.put(id, countdown);
			generatorDisplay.updateCountdown(FWResourceType.DIAMOND, id, countdown);
			generatorDisplay.setParticlesActive(FWResourceType.DIAMOND, id, countdown <= PARTICLE_WARNING_SECONDS);
		}

		instance.refreshScoreboards(); // ticks every second so the "Diamond Tier X: Ym Zs" line counts down live
	}

	private void announceDiamondTierUpgrade(int tier) {
		instance.TellAll("");
		instance.TellAll(instance.getManager().getMain().color(
				"&2&l(!) &bDiamond Generator &rupgraded to &aTier " + TIER_ROMAN[tier - 1]));
	}

	/** Total amount of the given material currently sitting uncollected near a generator - the basis for the ground caps above. */
	private int groundCount(World world, Location loc, Material material) {
		int count = 0;
		for (Entity entity : world.getNearbyEntities(loc, GROUND_CAP_CHECK_RADIUS, GROUND_CAP_CHECK_RADIUS, GROUND_CAP_CHECK_RADIUS)) {
			if (entity instanceof Item && ((Item) entity).getItemStack().getType() == material) {
				count += ((Item) entity).getItemStack().getAmount();
			}
		}
		return count;
	}

	/** Tag read by FWPlayerListener#onPickupGeneratorItem - marks an item as eligible for the gen-splitting mechanic below. */
	public static final String GENERATOR_ITEM_METADATA = "fw_generator_item";

	/** Same drop as drop() below, just tagged so any teammate standing next to the picker also gets a copy - see FWPlayerListener. */
	private void dropSplittable(World world, Location loc, ItemStack item) {
		Item dropped = world.dropItem(loc, item);
		dropped.setVelocity(new Vector(0, 0, 0));
		dropped.setMetadata(GENERATOR_ITEM_METADATA,
				new org.bukkit.metadata.FixedMetadataValue(instance.getManager().getMain(), true));
	}

	/** dropItemNaturally scatters with random velocity; this keeps the item exactly where it spawns. */
	private void drop(World world, Location loc, ItemStack item) {
		Item dropped = world.dropItem(loc, item);
		dropped.setVelocity(new Vector(0, 0, 0));
	}
}
