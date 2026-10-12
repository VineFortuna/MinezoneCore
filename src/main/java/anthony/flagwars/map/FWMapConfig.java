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

	/** No building within this many blocks of a team spawn. */
	private static final double SPAWN_PROTECTED_RADIUS = 7.0;
	/** No building within this many blocks (XZ only, any Y) of any generator (iron, gold, emerald, diamond). */
	private static final double GENERATOR_PROTECTED_RADIUS = 4.0;
	/** No building within this many blocks of a team's flag. */
	private static final double FLAG_PROTECTED_RADIUS = 1.5;

	private final String worldName;
	private Location lobby;
	private Location spectatorLocation;
	private final Map<Integer, FWTeamSite> teamSites = new LinkedHashMap<>();
	private final Map<Integer, Location> emeraldGenerators = new LinkedHashMap<>();
	private final Map<Integer, Location> diamondGenerators = new LinkedHashMap<>();
	private final Map<Integer, Location> chestHolograms = new LinkedHashMap<>();
	/** Custom fixed generation interval (seconds) per generator id, overriding the tier-based timing entirely. Absent means "use the default tiered timing". */
	private final Map<Integer, Integer> emeraldGeneratorIntervals = new LinkedHashMap<>();
	private final Map<Integer, Integer> diamondGeneratorIntervals = new LinkedHashMap<>();

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

	public Location getSpectatorLocation() {
		return spectatorLocation;
	}

	public void setSpectatorLocation(Location spectatorLocation) {
		this.spectatorLocation = spectatorLocation;
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

	/** Null if this generator uses the default tiered timing. */
	public Integer getEmeraldGeneratorInterval(int id) {
		return emeraldGeneratorIntervals.get(id);
	}

	/** Pass null to clear the override and fall back to the default tiered timing. */
	public void setEmeraldGeneratorInterval(int id, Integer seconds) {
		if (seconds == null) emeraldGeneratorIntervals.remove(id);
		else emeraldGeneratorIntervals.put(id, seconds);
	}

	/** Null if this generator uses the default tiered timing. */
	public Integer getDiamondGeneratorInterval(int id) {
		return diamondGeneratorIntervals.get(id);
	}

	/** Pass null to clear the override and fall back to the default tiered timing. */
	public void setDiamondGeneratorInterval(int id, Integer seconds) {
		if (seconds == null) diamondGeneratorIntervals.remove(id);
		else diamondGeneratorIntervals.put(id, seconds);
	}

	public Map<Integer, Location> getChestHolograms() {
		return chestHolograms;
	}

	public void setChestHologram(int id, Location loc) {
		chestHolograms.put(id, loc);
	}

	/** Returns missing pieces of config, for /fwsetup list. Empty if fully configured. Only created team slots (see FWTeamSite#isCreated) are checked - an uncreated slot just isn't in play on this map. */
	public java.util.List<String> findMissing() {
		java.util.List<String> missing = new java.util.ArrayList<>();
		if (lobby == null) missing.add("lobby");

		int createdCount = 0;
		for (FWTeamSite site : teamSites.values()) {
			if (!site.isCreated()) continue;
			createdCount++;

			String color = FWGameConstants.TEAM_NAMES[site.getId() - 1];
			if (site.getSpawn() == null) missing.add("team " + color + " spawn");
			if (site.getFlagLoc() == null) missing.add("team " + color + " flag");
			if (site.getShopLoc() == null) missing.add("team " + color + " shop");
			if (site.getIronGenerator() == null) missing.add("team " + color + " iron generator");
			if (site.getGoldGenerator() == null) missing.add("team " + color + " gold generator");
		}

		if (createdCount < 2) missing.add("at least 2 created teams (run /fwsetup team <color>)");
		if (emeraldGenerators.isEmpty()) missing.add("at least one emerald generator");
		if (diamondGenerators.isEmpty()) missing.add("at least one diamond generator");
		return missing;
	}

	public boolean isReady() {
		return findMissing().isEmpty();
	}

	/** True if loc is within the protected radius of any team spawn (7 blocks), any team flag (1.5 blocks), or any generator (4 blocks XZ, any Y). */
	public boolean isProtectedFromBuilding(Location loc) {
		for (FWTeamSite site : teamSites.values()) {
			if (withinRadius(loc, site.getSpawn(), SPAWN_PROTECTED_RADIUS)) return true;
			if (withinRadius(loc, site.getFlagLoc(), FLAG_PROTECTED_RADIUS)) return true;
			if (withinXZRadius(loc, site.getIronGenerator(), GENERATOR_PROTECTED_RADIUS)) return true;
			if (withinXZRadius(loc, site.getGoldGenerator(), GENERATOR_PROTECTED_RADIUS)) return true;
		}
		for (Location gen : emeraldGenerators.values()) {
			if (withinXZRadius(loc, gen, GENERATOR_PROTECTED_RADIUS)) return true;
		}
		for (Location gen : diamondGenerators.values()) {
			if (withinXZRadius(loc, gen, GENERATOR_PROTECTED_RADIUS)) return true;
		}
		return false;
	}

	private boolean withinRadius(Location loc, Location point, double radius) {
		return point != null
				&& loc.getWorld().equals(point.getWorld())
				&& loc.distanceSquared(point) <= radius * radius;
	}

	private boolean withinXZRadius(Location loc, Location point, double radius) {
		if (point == null || !loc.getWorld().equals(point.getWorld())) return false;
		double dx = loc.getX() - point.getX();
		double dz = loc.getZ() - point.getZ();
		return dx * dx + dz * dz <= radius * radius;
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
		copy.setSpectatorLocation(translateLocation(spectatorLocation, targetWorld));

		for (Map.Entry<Integer, FWTeamSite> entry : teamSites.entrySet()) {
			FWTeamSite source = entry.getValue();
			FWTeamSite target = copy.getTeamSite(entry.getKey());
			target.setCreated(source.isCreated());
			target.setSpawn(translateLocation(source.getSpawn(), targetWorld));
			target.setFlagLoc(translateLocation(source.getFlagLoc(), targetWorld));
			target.setShopLoc(translateLocation(source.getShopLoc(), targetWorld));
			target.setIronGenerator(translateLocation(source.getIronGenerator(), targetWorld));
			target.setGoldGenerator(translateLocation(source.getGoldGenerator(), targetWorld));
			target.setIronGeneratorInterval(source.getIronGeneratorInterval());
			target.setGoldGeneratorInterval(source.getGoldGeneratorInterval());
			target.setBoundaryCorner1(translateLocation(source.getBoundaryCorner1(), targetWorld));
			target.setBoundaryCorner2(translateLocation(source.getBoundaryCorner2(), targetWorld));
		}

		for (Map.Entry<Integer, Location> entry : emeraldGenerators.entrySet()) {
			copy.setEmeraldGenerator(entry.getKey(), translateLocation(entry.getValue(), targetWorld));
		}
		for (Map.Entry<Integer, Location> entry : diamondGenerators.entrySet()) {
			copy.setDiamondGenerator(entry.getKey(), translateLocation(entry.getValue(), targetWorld));
		}
		for (Map.Entry<Integer, Integer> entry : emeraldGeneratorIntervals.entrySet()) {
			copy.setEmeraldGeneratorInterval(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<Integer, Integer> entry : diamondGeneratorIntervals.entrySet()) {
			copy.setDiamondGeneratorInterval(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<Integer, Location> entry : chestHolograms.entrySet()) {
			copy.setChestHologram(entry.getKey(), translateLocation(entry.getValue(), targetWorld));
		}

		return copy;
	}

	private static Location translateLocation(Location loc, World targetWorld) {
		if (loc == null) return null;
		return new Location(targetWorld, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	}
}
