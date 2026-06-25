package anthony.flagwars.map;

import anthony.flagwars.FWGameConstants;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Full configuration for one Flag Wars map: the lobby point, each
 * team's site, and the shared emerald/diamond generator points. Filled in
 * via /fwsetup and persisted by FWMapConfigManager.
 */
public class FWMapConfig {

	public static final int TEAM_COUNT = 8;

	/** No building within this many blocks of a spawn or any generator - keeps those spots from getting blocked off. */
	private static final double PROTECTED_RADIUS = 4.0;

	private final String worldName;
	private Location lobby;
	private final Map<Integer, FWTeamSite> teamSites = new LinkedHashMap<>();
	private final Map<Integer, Location> emeraldGenerators = new LinkedHashMap<>();
	private final Map<Integer, Location> diamondGenerators = new LinkedHashMap<>();

	public FWMapConfig(String worldName) {
		this.worldName = worldName;
		for (int i = 1; i <= TEAM_COUNT; i++) {
			teamSites.put(i, new FWTeamSite(i));
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

	public FWTeamSite getTeamSite(int id) {
		return teamSites.get(id);
	}

	public Map<Integer, FWTeamSite> getTeamSites() {
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

	/** Returns missing pieces of config, for /fwsetup list. Empty if fully configured. */
	public java.util.List<String> findMissing() {
		java.util.List<String> missing = new java.util.ArrayList<>();
		if (lobby == null) missing.add("lobby");
		for (FWTeamSite site : teamSites.values()) {
			String color = FWGameConstants.TEAM_NAMES[site.getId() - 1];
			if (site.getSpawn() == null) missing.add("team " + color + " spawn");
			if (site.getFlagLoc() == null) missing.add("team " + color + " flag");
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

	/** True if loc is within PROTECTED_RADIUS of any team's spawn/iron/gold generator, or any shared emerald/diamond generator. */
	public boolean isProtectedFromBuilding(Location loc) {
		for (FWTeamSite site : teamSites.values()) {
			if (withinRadius(loc, site.getSpawn())) return true;
			if (withinRadius(loc, site.getIronGenerator())) return true;
			if (withinRadius(loc, site.getGoldGenerator())) return true;
		}
		for (Location gen : emeraldGenerators.values()) {
			if (withinRadius(loc, gen)) return true;
		}
		for (Location gen : diamondGenerators.values()) {
			if (withinRadius(loc, gen)) return true;
		}
		return false;
	}

	private boolean withinRadius(Location loc, Location point) {
		return point != null
				&& loc.getWorld().equals(point.getWorld())
				&& loc.distanceSquared(point) <= PROTECTED_RADIUS * PROTECTED_RADIUS;
	}

	/**
	 * A deep copy of this config with every Location re-pointed to targetWorld, same x/y/z/yaw/pitch.
	 * Needed because a cloned arena world is a byte-for-byte copy of the same coordinates but is a
	 * completely separate Bukkit World object - every FWGameInstance keeps one of these (see
	 * FWGameInstance#getMapConfig) instead of ever reading the shared, primary-world-bound config
	 * FWMapConfigManager holds directly.
	 */
	public FWMapConfig translateTo(World targetWorld) {
		FWMapConfig copy = new FWMapConfig(worldName);
		copy.setLobby(translateLocation(lobby, targetWorld));

		for (Map.Entry<Integer, FWTeamSite> entry : teamSites.entrySet()) {
			FWTeamSite source = entry.getValue();
			FWTeamSite target = copy.getTeamSite(entry.getKey());
			target.setSpawn(translateLocation(source.getSpawn(), targetWorld));
			target.setFlagLoc(translateLocation(source.getFlagLoc(), targetWorld));
			target.setShopLoc(translateLocation(source.getShopLoc(), targetWorld));
			target.setIronGenerator(translateLocation(source.getIronGenerator(), targetWorld));
			target.setGoldGenerator(translateLocation(source.getGoldGenerator(), targetWorld));
			target.setBoundaryCorner1(translateLocation(source.getBoundaryCorner1(), targetWorld));
			target.setBoundaryCorner2(translateLocation(source.getBoundaryCorner2(), targetWorld));
		}

		for (Map.Entry<Integer, Location> entry : emeraldGenerators.entrySet()) {
			copy.setEmeraldGenerator(entry.getKey(), translateLocation(entry.getValue(), targetWorld));
		}
		for (Map.Entry<Integer, Location> entry : diamondGenerators.entrySet()) {
			copy.setDiamondGenerator(entry.getKey(), translateLocation(entry.getValue(), targetWorld));
		}

		return copy;
	}

	private static Location translateLocation(Location loc, World targetWorld) {
		if (loc == null) return null;
		return new Location(targetWorld, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}
}
