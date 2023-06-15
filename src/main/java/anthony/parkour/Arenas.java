package anthony.parkour;

import org.bukkit.util.Vector;

public enum Arenas {

	Parkour1(new ArenaInstance().setSpawnLoc(new Vector(-29.481, 137, 8.530))
			.setBounds(new Vector(-111.500, 150, 24.500), 90, 20, 20), 2),
	Default1(new ArenaInstance().setSpawnLoc(new Vector(8.456, 128, 492.479))
			.setBounds(new Vector(52.720, 155, 496.461), 50, 40, 20), 2),
	Default2(new ArenaInstance().setSpawnLoc(new Vector(-5.484, 128, 492.562))
			.setBounds(new Vector(-42.383, 143, 492.474), 42, 25, 20), 1);

	private ArenaInstance i;
	private int checkpointNum;

	Arenas(ArenaInstance i, int checkpointNum) {
		this.i = i;
		this.checkpointNum = checkpointNum;
	}

	public ArenaInstance getInstance() {
		return i;
	}

	public int getCheckpoints() {
		return checkpointNum;
	}

}
