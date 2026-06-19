package anthony.villagerdefense.villager;

import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.map.VDMapConfig;
import anthony.villagerdefense.map.VDTeamSite;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.HashMap;
import java.util.Map;

/**
 * Spawns and tracks each team's objective Villager (the bed equivalent)
 * and shopkeeper Villager, and handles villager destruction / upgrades.
 */
public class VDVillagerManager {

	private static final double VILLAGER_HEALTH = 60.0;

	private final VDGameInstance instance;
	private final Map<Integer, Villager> shopkeepers = new HashMap<>();

	public VDVillagerManager(VDGameInstance instance) {
		this.instance = instance;
	}

	public void spawnAll() {
		World world = instance.getWorld();
		VDMapConfig config = instance.getManager().getMapConfigManager().getConfig();

		for (VDTeam team : instance.getTeams().values()) {
			if (team.isEmpty()) continue;

			VDTeamSite site = config.getTeamSite(team.getSiteId());

			Villager objective = (Villager) world.spawnEntity(site.getVillagerLoc(), EntityType.VILLAGER);
			objective.setCustomName(instance.getManager().getMain().color("&e" + team.getName() + "'s Villager"));
			objective.setCustomNameVisible(true);
			objective.setRemoveWhenFarAway(false);
			objective.setMaxHealth(VILLAGER_HEALTH);
			objective.setHealth(VILLAGER_HEALTH);
			team.setVillagerEntity(objective);
			team.setVillagerAlive(true);

			Villager shopkeeper = (Villager) world.spawnEntity(site.getShopLoc(), EntityType.VILLAGER);
			shopkeeper.setCustomName(instance.getManager().getMain().color("&aShopkeeper"));
			shopkeeper.setCustomNameVisible(true);
			shopkeeper.setRemoveWhenFarAway(false);
			shopkeepers.put(team.getSiteId(), shopkeeper);
		}
	}

	public void despawnAll() {
		for (VDTeam team : instance.getTeams().values()) {
			if (team.getVillagerEntity() != null) {
				team.getVillagerEntity().remove();
				team.setVillagerEntity(null);
			}
		}
		for (Villager shopkeeper : shopkeepers.values()) {
			shopkeeper.remove();
		}
		shopkeepers.clear();
	}

	public VDTeam findTeamByObjective(Entity entity) {
		for (VDTeam team : instance.getTeams().values()) {
			if (entity.equals(team.getVillagerEntity())) return team;
		}
		return null;
	}

	public Integer findSiteByShopkeeper(Entity entity) {
		for (Map.Entry<Integer, Villager> entry : shopkeepers.entrySet()) {
			if (entry.getValue().equals(entity)) return entry.getKey();
		}
		return null;
	}

	public void destroyObjective(VDTeam team, Player killer) {
		if (team.getVillagerEntity() != null) {
			team.getVillagerEntity().remove();
			team.setVillagerEntity(null);
		}
		team.setVillagerAlive(false);

		String killerName = killer != null ? killer.getName() : "the void";
		instance.TellAll(instance.getManager().getMain().color("&4&l(!) &eTeam " + team.getName()
				+ "'s &rvillager was destroyed by &e" + killerName + "&r! They can no longer respawn!"));
	}

	public boolean upgrade(VDTeam team, Player initiator) {
		VillagerLevel next = team.getLevel().next();
		if (next == null) {
			initiator.sendMessage(instance.getManager().getMain().color("&c&l(!) &rYour villager is already max level!"));
			return false;
		}

		if (!team.canAfford(next.getUpgradeCost())) {
			initiator.sendMessage(instance.getManager().getMain().color("&c&l(!) &rYou can't afford that upgrade yet!"));
			return false;
		}

		team.spend(next.getUpgradeCost());
		team.setLevel(next);
		instance.TellAll(instance.getManager().getMain().color("&6&l(!) &eTeam " + team.getName()
				+ "&r upgraded their villager to &e" + next.name() + "&r!"));
		instance.getDefenseManager().onTeamUpgraded(team);
		return true;
	}
}
