package anthony.flagdefense.resources;

import anthony.flagdefense.FDGameInstance;
import anthony.flagdefense.FDTeam;
import anthony.flagdefense.map.FDMapConfig;
import anthony.flagdefense.map.FDTeamSite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * Ticking generator that drops physical resource items at each team's base
 * (iron/gold) and at the map's shared mid points (emerald/diamond). Players
 * pick these up normally into their inventory - there's no hidden currency,
 * see FDEconomy. Uses plain dropItem (not dropItemNaturally) with velocity
 * zeroed so the item sits exactly where it's generated instead of scattering.
 * Owns a FDGeneratorDisplay for the cosmetic spinning block + hologram above
 * each emerald/diamond spot, fed the live "Regenerates In" countdown here.
 */
public class FDResourceGenerator {

	private static final int IRON_INTERVAL_SECONDS = 1;
	private static final int EMERALD_INTERVAL_SECONDS = 45;
	private static final int DIAMOND_INTERVAL_SECONDS = 60;

	private final FDGameInstance instance;
	private final FDGeneratorDisplay generatorDisplay;
	private BukkitTask task;
	private int elapsedSeconds = 0;

	public FDResourceGenerator(FDGameInstance instance) {
		this.instance = instance;
		this.generatorDisplay = new FDGeneratorDisplay(instance);
	}

	public void start() {
		if (task != null) return;
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

	private void tick() {
		elapsedSeconds++;
		World world = instance.getWorld();
		FDMapConfig config = instance.getManager().getMapConfigManager().getConfig();

		for (FDTeam team : instance.getTeams().values()) {
			if (team.isInactive() || team.isEmpty()) continue;

			FDTeamSite site = config.getTeamSite(team.getSiteId());

			if (elapsedSeconds % IRON_INTERVAL_SECONDS == 0 && site.getIronGenerator() != null) {
				int amount = instance.getTeamUpgradeManager().ironAmountFor(team);
				drop(world, site.getIronGenerator(), new ItemStack(Material.IRON_INGOT, amount));
			}

			int goldInterval = instance.getTeamUpgradeManager().goldIntervalSecondsFor(team);
			if (elapsedSeconds % goldInterval == 0 && site.getGoldGenerator() != null) {
				drop(world, site.getGoldGenerator(), new ItemStack(Material.GOLD_INGOT));
			}
		}

		if (elapsedSeconds % EMERALD_INTERVAL_SECONDS == 0) {
			for (Location loc : config.getEmeraldGenerators().values()) {
				drop(world, loc, new ItemStack(Material.EMERALD));
			}
		}
		if (elapsedSeconds % DIAMOND_INTERVAL_SECONDS == 0) {
			for (Location loc : config.getDiamondGenerators().values()) {
				drop(world, loc, new ItemStack(Material.DIAMOND));
			}
		}

		generatorDisplay.updateCountdown(FDResourceType.EMERALD,
				EMERALD_INTERVAL_SECONDS - (elapsedSeconds % EMERALD_INTERVAL_SECONDS));
		generatorDisplay.updateCountdown(FDResourceType.DIAMOND,
				DIAMOND_INTERVAL_SECONDS - (elapsedSeconds % DIAMOND_INTERVAL_SECONDS));
	}

	/** dropItemNaturally scatters with random velocity; this keeps the item exactly where it spawns. */
	private void drop(World world, Location loc, ItemStack item) {
		Item dropped = world.dropItem(loc, item);
		dropped.setVelocity(new Vector(0, 0, 0));
	}
}
