package anthony.villagerdefense.defense;

import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.map.VDTeamSite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * A killable Iron Golem that slowly repairs nearby damaged defense blocks
 * once a team reaches Level 3, reusing VDGameInstance's match-reset undo log.
 */
public class RepairGolemMechanic {

	private static final double GOLEM_HEALTH = 50.0;
	private static final double REPAIR_RADIUS = 10.0;
	private static final int MAX_BLOCKS_PER_PASS = 3;
	private static final long REPAIR_INTERVAL_TICKS = 10L * 20L;

	private final VDGameInstance instance;
	private final Map<Integer, IronGolem> golems = new HashMap<>();
	private BukkitTask task;

	public RepairGolemMechanic(VDGameInstance instance) {
		this.instance = instance;
	}

	public void activate(VDTeam team) {
		if (team.isEliminated() || team.isEmpty()) return;

		VDTeamSite site = instance.getManager().getMapConfigManager().getConfig().getTeamSite(team.getSiteId());
		Location loc = site.getVillagerLoc().clone().add(-1, 0, 0);

		IronGolem golem = (IronGolem) instance.getWorld().spawnEntity(loc, EntityType.IRON_GOLEM);
		golem.setCustomName(instance.getManager().getMain().color("&b" + team.getName() + "'s Repair Golem"));
		golem.setCustomNameVisible(true);
		golem.setMaxHealth(GOLEM_HEALTH);
		golem.setHealth(GOLEM_HEALTH);
		golem.setRemoveWhenFarAway(false);
		golems.put(team.getSiteId(), golem);

		ensureRepairLoop();
	}

	private void ensureRepairLoop() {
		if (task != null) return;
		task = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), this::repairPass,
				REPAIR_INTERVAL_TICKS, REPAIR_INTERVAL_TICKS);
	}

	private void repairPass() {
		for (IronGolem golem : golems.values()) {
			if (golem.isDead()) continue;
			instance.repairNear(golem.getLocation(), REPAIR_RADIUS, MAX_BLOCKS_PER_PASS);
		}
	}

	public Integer teamSiteOf(Entity entity) {
		for (Map.Entry<Integer, IronGolem> entry : golems.entrySet()) {
			if (entry.getValue().equals(entity)) return entry.getKey();
		}
		return null;
	}

	public void onGolemKilled(int siteId) {
		golems.remove(siteId);
	}

	public void stopAll() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		for (IronGolem golem : golems.values()) {
			golem.remove();
		}
		golems.clear();
	}
}
