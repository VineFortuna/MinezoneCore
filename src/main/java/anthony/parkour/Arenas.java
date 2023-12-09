package anthony.parkour;

import org.bukkit.util.Vector;

public enum Arenas {

	DeathParkour(new ArenaInstance().setSpawnLoc(new Vector(148.488, 99, 629.481))
			.setBounds(new Vector(148.488, 99, 629.481), 40, 50, 30), 2, "Jessey2105");

	private ArenaInstance i;
	private int checkpointNum;
	private String designer;

	Arenas(ArenaInstance i, int checkpointNum, String designer) {
		this.i = i;
		this.checkpointNum = checkpointNum;
		this.designer = designer;
	}

	public ArenaInstance getInstance() {
		return this.i;
	}

	public int getCheckpoints() {
		return this.checkpointNum;
	}
	
	public String getDesigner() {
		return this.designer;
	}

}
