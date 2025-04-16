package anthony.parkour;

import org.bukkit.util.Vector;

import java.util.Arrays;

public enum Arenas {

	MainParkour(
			"Main",
			new ArenaInstance()
					.setStartLoc(new Vector(189.5, 105, 567.5))
					.setEndLoc(new Vector(178.5, 107, 547.5))
					.setCheckpoints(
							Arrays.asList(
									new Vector(182, 106, 557),
									new Vector(178, 106, 553)
							)
					)
	);

	private String name;
	private ArenaInstance i;

	Arenas(String name, ArenaInstance i) {
		this.name = name;
		this.i = i;
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
