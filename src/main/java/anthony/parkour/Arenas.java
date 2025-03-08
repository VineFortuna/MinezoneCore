package anthony.parkour;

import org.bukkit.util.Vector;

public enum Arenas {

	MainParkour(
			"Main",
			new ArenaInstance()
					.setStartLoc(new Vector(189.5, 105, 567.5))
					.setEndLoc(new Vector(178.5, 107, 547.5)),
			3);

	private String name;
	private ArenaInstance i;
	private int checkpointNum;

	Arenas(String name, ArenaInstance i, int checkpointNum) {
		this.name = name;
		this.i = i;
		this.checkpointNum = checkpointNum;
	}

	public String getName() {
		return this.name;
	}

	public ArenaInstance getInstance() {
		return this.i;
	}

	public int getCheckpoints() {
		return this.checkpointNum;
	}

}
