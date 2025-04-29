package anthony.parkour;

import org.bukkit.util.Vector;

import java.util.Arrays;

public enum Arenas {

	MainParkour(
			1,
			"Main",
			new ArenaInstance()
					.setStartLoc(new Vector(189, 105, 567))
					.setEndLoc(new Vector(178, 107, 547))
					.setLeaderboardLoc(new Vector(192, 106, 566))
					.setCheckpoints(
							Arrays.asList(
									new Vector(182, 106, 557),
									new Vector(178, 106, 553)
							)
					)
	);

	private int id;
	private String name;
	private ArenaInstance i;

	Arenas(int id, String name, ArenaInstance i) {
		this.id = id;
		this.name = name;
		this.i = i;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public ArenaInstance getInstance() {
		return this.i;
	}

	public int getCheckpoints() {
		return this.getInstance().checkpoints.size();
	}

	public Vector getCheckpoint(int i) {
		return this.getInstance().checkpoints.get(i);
	}

}
