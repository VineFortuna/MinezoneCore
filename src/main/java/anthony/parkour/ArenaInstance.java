package anthony.parkour;

import org.bukkit.util.Vector;

import java.util.List;

public class ArenaInstance {

	public Vector startLoc = new Vector(0, 100, 0);
	public Vector endLoc = new Vector(0, 100, 0);
	public Vector leaderboardLoc = new Vector(0, 100, 0);
	public List<Vector> checkpoints;
	
	public ArenaInstance setStartLoc(Vector v) {
		this.startLoc = v;
		return this;
	}

	public ArenaInstance setEndLoc(Vector v) {
		this.endLoc = v;
		return this;
	}

	public ArenaInstance setLeaderboardLoc(Vector v) {
		this.leaderboardLoc = v;
		return this;
	}

	public ArenaInstance setCheckpoints(List<Vector> checkpoints) {
		this.checkpoints = checkpoints;
		return this;
	}

}
