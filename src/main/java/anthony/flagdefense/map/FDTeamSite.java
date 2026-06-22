package anthony.flagdefense.map;

import org.bukkit.Location;

/**
 * One team's set of points of interest on a FlagDefense map, captured
 * in-game via /fdsetup. Anything left null means that part of the map
 * hasn't been configured yet.
 */
public class FDTeamSite {

	private final int id;
	private Location spawn;
	private Location flagLoc;
	private Location shopLoc;
	private Location ironGenerator;
	private Location goldGenerator;

	public FDTeamSite(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
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

	public boolean isComplete() {
		return spawn != null && flagLoc != null && shopLoc != null
				&& ironGenerator != null && goldGenerator != null;
	}
}
