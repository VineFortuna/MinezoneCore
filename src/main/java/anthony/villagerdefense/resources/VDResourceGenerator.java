package anthony.villagerdefense.resources;

import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.map.VDMapConfig;
import anthony.villagerdefense.map.VDTeamSite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

/**
 * Ticking generator that drops physical resource items at each team's base
 * (iron/gold) and at the map's shared mid points (emerald/diamond), the same
 * dropItemNaturally approach CrystalWars' GameInstance#ironGoldGen uses.
 * Picking the item up credits the player's team currency directly
 * (see VDPlayerListener#onItemPickup) instead of cluttering their inventory.
 */
public class VDResourceGenerator {

	private static final int IRON_INTERVAL_SECONDS = 1;
	private static final int GOLD_INTERVAL_SECONDS = 4;
	private static final int EMERALD_INTERVAL_SECONDS = 45;
	private static final int DIAMOND_INTERVAL_SECONDS = 60;

	private final VDGameInstance instance;
	private BukkitTask task;
	private int elapsedSeconds = 0;

	public VDResourceGenerator(VDGameInstance instance) {
		this.instance = instance;
	}

	public void start() {
		if (task != null) return;
		task = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), this::tick, 0L, 20L);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		elapsedSeconds = 0;
	}

	private void tick() {
		elapsedSeconds++;
		World world = instance.getWorld();
		VDMapConfig config = instance.getManager().getMapConfigManager().getConfig();

		for (VDTeam team : instance.getTeams().values()) {
			if (team.isEliminated() || team.isEmpty()) continue;

			VDTeamSite site = config.getTeamSite(team.getSiteId());

			if (elapsedSeconds % IRON_INTERVAL_SECONDS == 0 && site.getIronGenerator() != null) {
				world.dropItemNaturally(site.getIronGenerator(), new ItemStack(Material.IRON_INGOT));
			}
			if (elapsedSeconds % GOLD_INTERVAL_SECONDS == 0 && site.getGoldGenerator() != null) {
				world.dropItemNaturally(site.getGoldGenerator(), new ItemStack(Material.GOLD_INGOT));
			}
		}

		if (elapsedSeconds % EMERALD_INTERVAL_SECONDS == 0) {
			for (Location loc : config.getEmeraldGenerators().values()) {
				world.dropItemNaturally(loc, new ItemStack(Material.EMERALD));
			}
		}
		if (elapsedSeconds % DIAMOND_INTERVAL_SECONDS == 0) {
			for (Location loc : config.getDiamondGenerators().values()) {
				world.dropItemNaturally(loc, new ItemStack(Material.DIAMOND));
			}
		}
	}
}
