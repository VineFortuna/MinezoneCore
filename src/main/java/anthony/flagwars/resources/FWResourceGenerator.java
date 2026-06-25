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

	private static final int IRON_INTERVAL_SECONDS = 1;
	private static final int PARTICLE_WARNING_SECONDS = 3;

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
	 * Real Hypixel caps on how much of each resource can be sitting unclaimed at a generator
	 * before it stops spawning more - iron/gold confirmed at 48/16 (Hypixel only raises these for
	 * the owning team's own base generator via Forge, which this doesn't replicate - flat caps
	 * here regardless of Forge tier). Diamond/emerald per the brief: 4 and 2.
	 */
	private static final int IRON_GROUND_CAP = 48;
	private static final int GOLD_GROUND_CAP = 16;
	private static final int EMERALD_GROUND_CAP = 2;
	private static final int DIAMOND_GROUND_CAP = 4;
	private static final double GROUND_CAP_CHECK_RADIUS = 3.0;

	private final FWGameInstance instance;
	private final FWGeneratorDisplay generatorDisplay;
	private BukkitTask task;
	private int elapsedSeconds = 0;
	private int emeraldCountdown;
	private int diamondCountdown;
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

	public int getDiamondSecondsLeft() {
		return diamondCountdown;
	}

	public int getEmeraldSecondsLeft() {
		return emeraldCountdown;
	}

	private int diamondIntervalSeconds() {
		int base = DIAMOND_TIER_SECONDS[getCurrentTier() - 1];
		return doubleRate ? Math.max(1, base / 2) : base;
	}

	private int emeraldIntervalSeconds() {
		int base = EMERALD_TIER_SECONDS[getCurrentTier() - 1];
		return doubleRate ? Math.max(1, base / 2) : base;
	}

	public void start() {
		if (task != null) return;
		emeraldCountdown = emeraldIntervalSeconds();
		diamondCountdown = diamondIntervalSeconds();
		generatorDisplay.start();
		task = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), this::tick, 0L, 20L);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		elapsedSeconds = 0;
		generatorDisplay.stop();
	}

	private static final String[] TIER_ROMAN = {"I", "II", "III"};

	private void tick() {
		elapsedSeconds++;

		// Fires exactly once, the instant elapsedSeconds crosses each threshold - the actual
		// speed-up (diamondIntervalSeconds() reading the new tier) already happens automatically
		// next time diamondCountdown resets, this is just the player-facing announcement of it.
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

			if (elapsedSeconds % IRON_INTERVAL_SECONDS == 0 && site.getIronGenerator() != null) {
				Location ironLoc = site.getIronGenerator();
				if (groundCount(world, ironLoc, Material.IRON_INGOT) < IRON_GROUND_CAP) {
					int amount = instance.getTeamUpgradeManager().ironAmountFor(team);
					dropSplittable(world, ironLoc, new ItemStack(Material.IRON_INGOT, amount));
				}
			}

			int goldInterval = instance.getTeamUpgradeManager().goldIntervalSecondsFor(team);
			if (elapsedSeconds % goldInterval == 0 && site.getGoldGenerator() != null) {
				Location goldLoc = site.getGoldGenerator();
				if (groundCount(world, goldLoc, Material.GOLD_INGOT) < GOLD_GROUND_CAP) {
					dropSplittable(world, goldLoc, new ItemStack(Material.GOLD_INGOT));
				}
			}
		}

		// Countdown-based (recomputed fresh from the *current* tier each time it resets) rather
		// than a fixed modulo against elapsedSeconds, so a tier change mid-cycle can't desync it.
		emeraldCountdown--;
		if (emeraldCountdown <= 0) {
			for (Location loc : config.getEmeraldGenerators().values()) {
				Location dropLoc = FWGeneratorDisplay.blockCenterLocation(loc);
				if (groundCount(world, dropLoc, Material.EMERALD) < EMERALD_GROUND_CAP) {
					drop(world, dropLoc, new ItemStack(Material.EMERALD));
				}
			}
			emeraldCountdown = emeraldIntervalSeconds();
		}

		diamondCountdown--;
		if (diamondCountdown <= 0) {
			for (Location loc : config.getDiamondGenerators().values()) {
				Location dropLoc = FWGeneratorDisplay.blockCenterLocation(loc);
				if (groundCount(world, dropLoc, Material.DIAMOND) < DIAMOND_GROUND_CAP) {
					drop(world, dropLoc, new ItemStack(Material.DIAMOND));
				}
			}
			diamondCountdown = diamondIntervalSeconds();
		}

		generatorDisplay.updateCountdown(FWResourceType.EMERALD, emeraldCountdown);
		generatorDisplay.updateCountdown(FWResourceType.DIAMOND, diamondCountdown);
		generatorDisplay.setParticlesActive(FWResourceType.EMERALD, emeraldCountdown <= PARTICLE_WARNING_SECONDS);
		generatorDisplay.setParticlesActive(FWResourceType.DIAMOND, diamondCountdown <= PARTICLE_WARNING_SECONDS);

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
