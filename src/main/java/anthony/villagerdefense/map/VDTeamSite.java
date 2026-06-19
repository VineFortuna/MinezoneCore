package anthony.villagerdefense.map;

import org.bukkit.Location;

/**
 * One team's set of points of interest on a VillagerDefense map, captured
 * in-game via /vdsetup. Anything left null means that part of the map
 * hasn't been configured yet.
 */
public class VDTeamSite {

	private final int id;
	private Location spawn;
	private Location villagerLoc;
	private Location shopLoc;
	private Location ironGenerator;
	private Location goldGenerator;

	public VDTeamSite(int id) {
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

	public Location getVillagerLoc() {
		return villagerLoc;
	}

	public void setVillagerLoc(Location villagerLoc) {
		this.villagerLoc = villagerLoc;
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
		return spawn != null && villagerLoc != null && shopLoc != null
				&& ironGenerator != null && goldGenerator != null;
	}
}
