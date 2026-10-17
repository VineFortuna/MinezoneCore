package anthony.flagwars.map;

import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One team's set of points of interest on a Flag Wars map, captured
 * in-game via /fwsetup. Anything left null means that part of the map
 * hasn't been configured yet.
 */
public class FWTeamSite {

	private final int id;
	/** Whether this color slot is actually in play on this map - see FWGameInstance's team setup, which only instantiates a runtime FWTeam for created sites. Set via /fwsetup team &lt;color&gt;. */
	private boolean created = false;
	private Location spawn;
	private Location flagLoc;
	private Location shopLoc;
	private Location ironGenerator;
	private Location goldGenerator;
	/** Custom fixed generation interval (seconds) set via /fwsetup, overriding the team-upgrade/early-boost timing entirely. Null means "use the default timing". */
	private Integer ironGeneratorInterval;
	private Integer goldGeneratorInterval;
	/** Two opposite corners of this island's capture boundary box - optional; see isWithinBoundary(). */
	private Location boundaryCorner1;
	private Location boundaryCorner2;
	/** No-build zones on this island by id, set via /fwsetup importantarea &lt;color&gt; &lt;id&gt;. */
	private final Map<Integer, FWImportantArea> importantAreas = new LinkedHashMap<>();

	public FWTeamSite(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public boolean isCreated() {
		return created;
	}

	public void setCreated(boolean created) {
		this.created = created;
	}

	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}

	public Location getFlagLoc() {
		return flagLoc;
	}

	public void setFlagLoc(Location flagLoc) {
		this.flagLoc = flagLoc;
	}

	public Location getShopLoc() {
		return shopLoc;
	}

	public void setShopLoc(Location shopLoc) {
		this.shopLoc = shopLoc;
	}

	public Location getIronGenerator() {
		return ironGenerator;
	}

	public void setIronGenerator(Location ironGenerator) {
		this.ironGenerator = ironGenerator;
	}

	public Location getGoldGenerator() {
		return goldGenerator;
	}

	public void setGoldGenerator(Location goldGenerator) {
		this.goldGenerator = goldGenerator;
	}

	public Integer getIronGeneratorInterval() {
		return ironGeneratorInterval;
	}

	public void setIronGeneratorInterval(Integer ironGeneratorInterval) {
		this.ironGeneratorInterval = ironGeneratorInterval;
	}

	public Integer getGoldGeneratorInterval() {
		return goldGeneratorInterval;
	}

	public void setGoldGeneratorInterval(Integer goldGeneratorInterval) {
		this.goldGeneratorInterval = goldGeneratorInterval;
	}

	public boolean isComplete() {
		return spawn != null && flagLoc != null && shopLoc != null
				&& ironGenerator != null && goldGenerator != null;
	}

	public Location getBoundaryCorner1() {
		return boundaryCorner1;
	}

	public void setBoundaryCorner1(Location boundaryCorner1) {
		this.boundaryCorner1 = boundaryCorner1;
	}

	public Location getBoundaryCorner2() {
		return boundaryCorner2;
	}

	public void setBoundaryCorner2(Location boundaryCorner2) {
		this.boundaryCorner2 = boundaryCorner2;
	}

	public Map<Integer, FWImportantArea> getImportantAreas() {
		return importantAreas;
	}

	public void setImportantArea(int id, FWImportantArea area) {
		importantAreas.put(id, area);
	}

	/** Returns true if an area with that id existed. */
	public boolean removeImportantArea(int id) {
		return importantAreas.remove(id) != null;
	}

	public boolean isInImportantArea(Location loc) {
		for (FWImportantArea area : importantAreas.values()) {
			if (area.contains(loc)) return true;
		}
		return false;
	}

	public boolean hasBoundary() {
		return boundaryCorner1 != null && boundaryCorner2 != null;
	}

	/** True if loc falls inside this island's boundary box (inclusive on every face). Always false if no boundary is set. */
	public boolean isWithinBoundary(Location loc) {
		if (!hasBoundary() || !loc.getWorld().equals(boundaryCorner1.getWorld())) return false;

		double minX = Math.min(boundaryCorner1.getX(), boundaryCorner2.getX());
		double maxX = Math.max(boundaryCorner1.getX(), boundaryCorner2.getX());
		double minY = Math.min(boundaryCorner1.getY(), boundaryCorner2.getY());
		double maxY = Math.max(boundaryCorner1.getY(), boundaryCorner2.getY());
		double minZ = Math.min(boundaryCorner1.getZ(), boundaryCorner2.getZ());
		double maxZ = Math.max(boundaryCorner1.getZ(), boundaryCorner2.getZ());

		return loc.getX() >= minX && loc.getX() <= maxX
				&& loc.getY() >= minY && loc.getY() <= maxY
				&& loc.getZ() >= minZ && loc.getZ() <= maxZ;
	}
}
