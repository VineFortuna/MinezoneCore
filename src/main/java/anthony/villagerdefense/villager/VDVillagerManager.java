package anthony.villagerdefense.villager;

import anthony.villagerdefense.VDGameInstance;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.map.VDMapConfig;
import anthony.villagerdefense.map.VDTeamSite;
import anthony.villagerdefense.resources.VDEconomy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spawns and tracks each team's objective Villager (the bed equivalent)
 * and shopkeeper Villager, and handles villager destruction / upgrades.
 * Both are pinned in place every tick - no wandering, no knockback - and
 * the objective is destroyed after exactly 3 hits rather than by health.
 */
public class VDVillagerManager {

	private static final int HITS_TO_DESTROY = 3;

	private final VDGameInstance instance;
	private final Map<Integer, Villager> shopkeepers = new HashMap<>();
	private final Map<Entity, Location> pinned = new LinkedHashMap<>();
	private final Map<Integer, Integer> hitCounts = new HashMap<>();
	private BukkitTask pinTask;

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
			team.setVillagerEntity(objective);
			team.setVillagerAlive(true);
			pinned.put(objective, site.getVillagerLoc().clone());
			hitCounts.put(team.getSiteId(), 0);

			Villager shopkeeper = (Villager) world.spawnEntity(site.getShopLoc(), EntityType.VILLAGER);
			shopkeeper.setCustomName(instance.getManager().getMain().color("&aShopkeeper"));
			shopkeeper.setCustomNameVisible(true);
			shopkeeper.setRemoveWhenFarAway(false);
			shopkeepers.put(team.getSiteId(), shopkeeper);
			pinned.put(shopkeeper, site.getShopLoc().clone());
		}

		startPinning();
	}

	/** Forces every tracked Villager back to its spawn spot and zeroes velocity, so they never wander or show knockback. */
	private void startPinning() {
		if (pinTask != null) return;

		pinTask = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), () -> {
			for (Map.Entry<Entity, Location> entry : pinned.entrySet()) {
				Entity entity = entry.getKey();
				if (entity.isDead()) continue;

				Location pin = entry.getValue();
				Location current = entity.getLocation();
				if (current.getX() != pin.getX() || current.getY() != pin.getY() || current.getZ() != pin.getZ()) {
					entity.teleport(pin);
				}
				entity.setVelocity(new Vector(0, 0, 0));
			}
		}, 0L, 1L);
	}

	public void despawnAll() {
		if (pinTask != null) {
			pinTask.cancel();
			pinTask = null;
		}
		pinned.clear();
		hitCounts.clear();

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

	/** Registers one hit on a team's objective; destroys it once HITS_TO_DESTROY is reached. */
	public void registerHit(VDTeam team, Player attacker) {
		int hits = hitCounts.merge(team.getSiteId(), 1, Integer::sum);
		int remaining = HITS_TO_DESTROY - hits;

		if (remaining > 0) {
			attacker.sendMessage(instance.getManager().getMain().color("&e&l(!) &rVillager hit! &e" + remaining
					+ " &rmore to destroy it."));
			attacker.playSound(attacker.getLocation(), Sound.ANVIL_LAND, 1f, 1.4f);
			return;
		}

		destroyObjective(team, attacker);
	}

	public void destroyObjective(VDTeam team, Player killer) {
		Villager objective = team.getVillagerEntity();
		Location objectiveLoc = objective != null ? objective.getLocation() : null;

		if (objective != null) {
			pinned.remove(objective);
			objective.remove();
			team.setVillagerEntity(null);
		}
		team.setVillagerAlive(false);

		String killerName = killer != null ? killer.getName() : "the void";
		instance.TellAll(instance.getManager().getMain().color("&4&l(!) &eTeam " + team.getName()
				+ "'s &rvillager was destroyed by &e" + killerName + "&r! They can no longer respawn!"));

		if (objectiveLoc != null) {
			instance.getWorld().playSound(objectiveLoc, Sound.ENDERDRAGON_GROWL, 1f, 1f);
		}
		instance.playSoundForAll(Sound.WITHER_DEATH, 0.6f, 1.2f);
		instance.refreshScoreboards();
	}

	public boolean upgrade(VDTeam team, Player initiator) {
		VillagerLevel next = team.getLevel().next();
		if (next == null) {
			initiator.sendMessage(instance.getManager().getMain().color("&c&l(!) &rYour villager is already max level!"));
			return false;
		}

		if (!VDEconomy.withdraw(initiator, next.getUpgradeCost())) {
			initiator.sendMessage(instance.getManager().getMain().color("&c&l(!) &rYou can't afford that upgrade yet!"));
			return false;
		}

		team.setLevel(next);
		instance.TellAll(instance.getManager().getMain().color("&6&l(!) &eTeam " + team.getName()
				+ "&r upgraded their villager to &e" + next.name() + "&r!"));
		instance.playSoundForAll(Sound.LEVEL_UP, 1f, 1f);
		instance.getDefenseManager().onTeamUpgraded(team);
		return true;
	}
}
