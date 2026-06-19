package anthony.villagerdefense.defense;

import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.map.VDTeamSite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

import java.util.HashMap;
import java.util.Map;

/**
 * A weak Zombie that guards each team's villager once they reach Level 2.
 * Respawns after a delay if killed; never targets its own team
 * (see VDPlayerListener#onEntityTarget).
 */
public class ZombieGuardMechanic {

	private static final double GUARD_HEALTH = 10.0;
	private static final long RESPAWN_DELAY_TICKS = 45L * 20L;

	private final VDGameInstance instance;
	private final Map<Integer, Zombie> guards = new HashMap<>();

	public ZombieGuardMechanic(VDGameInstance instance) {
		this.instance = instance;
	}

	public void activate(VDTeam team) {
		spawn(team);
	}

	private void spawn(VDTeam team) {
		if (team.isEliminated() || team.isEmpty()) return;

		VDTeamSite site = instance.getManager().getMapConfigManager().getConfig().getTeamSite(team.getSiteId());
		Location loc = site.getVillagerLoc().clone().add(1, 0, 0);

		Zombie zombie = (Zombie) instance.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
		zombie.setCustomName(instance.getManager().getMain().color("&2" + team.getName() + "'s Guard"));
		zombie.setCustomNameVisible(true);
		zombie.setMaxHealth(GUARD_HEALTH);
		zombie.setHealth(GUARD_HEALTH);
		zombie.setRemoveWhenFarAway(false);
		guards.put(team.getSiteId(), zombie);
	}

	public Integer teamSiteOf(Entity entity) {
		for (Map.Entry<Integer, Zombie> entry : guards.entrySet()) {
			if (entry.getValue().equals(entity)) return entry.getKey();
		}
		return null;
	}

	/** Called when a guard dies; schedules a respawn if the team is still active. */
	public void onGuardKilled(int siteId) {
		guards.remove(siteId);
		VDTeam team = instance.getTeams().get(siteId);
		if (team == null) return;

		Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
			if (team.getLevel().hasZombieGuard() && !team.isEliminated() && !team.isEmpty()) {
				spawn(team);
			}
		}, RESPAWN_DELAY_TICKS);
	}

	public void stopAll() {
		for (Zombie zombie : guards.values()) {
			zombie.remove();
		}
		guards.clear();
	}
}
