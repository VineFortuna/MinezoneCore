package anthony.villagerdefense.map;

import anthony.villagerdefense.VDGameConstants;
import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Full configuration for one VillagerDefense map: the lobby point, each
 * team's site, and the shared emerald/diamond generator points. Filled in
 * via /vdsetup and persisted by VDMapConfigManager.
 */
public class VDMapConfig {

	public static final int TEAM_COUNT = 8;
	public static final int TEAM_CAPACITY = 1; // Solos for this map; data-driven for future duos/squads maps.

	private final String worldName;
	private Location lobby;
	private final Map<Integer, VDTeamSite> teamSites = new LinkedHashMap<>();
	private final Map<Integer, Location> emeraldGenerators = new LinkedHashMap<>();
	private final Map<Integer, Location> diamondGenerators = new LinkedHashMap<>();

	public VDMapConfig(String worldName) {
		this.worldName = worldName;
		for (int i = 1; i <= TEAM_COUNT; i++) {
			teamSites.put(i, new VDTeamSite(i));
		}
	}

	public String getWorldName() {
		return worldName;
	}

	public Location getLobby() {
		return lobby;
	}

	public void setLobby(Location lobby) {
		this.lobby = lobby;
	}

	public VDTeamSite getTeamSite(int id) {
		return teamSites.get(id);
	}

	public Map<Integer, VDTeamSite> getTeamSites() {
		return teamSites;
	}

	public Map<Integer, Location> getEmeraldGenerators() {
		return emeraldGenerators;
	}

	public Map<Integer, Location> getDiamondGenerators() {
		return diamondGenerators;
	}

	public void setEmeraldGenerator(int id, Location loc) {
		emeraldGenerators.put(id, loc);
	}

	public void setDiamondGenerator(int id, Location loc) {
		diamondGenerators.put(id, loc);
	}

	/** Returns missing pieces of config, for /vdsetup list. Empty if fully configured. */
	public java.util.List<String> findMissing() {
		java.util.List<String> missing = new java.util.ArrayList<>();
		if (lobby == null) missing.add("lobby");
		for (VDTeamSite site : teamSites.values()) {
			String color = VDGameConstants.TEAM_NAMES[site.getId() - 1];
			if (site.getSpawn() == null) missing.add("team " + color + " spawn");
			if (site.getVillagerLoc() == null) missing.add("team " + color + " villager");
			if (site.getShopLoc() == null) missing.add("team " + color + " shop");
			if (site.getIronGenerator() == null) missing.add("team " + color + " iron generator");
			if (site.getGoldGenerator() == null) missing.add("team " + color + " gold generator");
		}
		if (emeraldGenerators.isEmpty()) missing.add("at least one emerald generator");
		if (diamondGenerators.isEmpty()) missing.add("at least one diamond generator");
		return missing;
	}

	public boolean isReady() {
		return findMissing().isEmpty();
	}
}
