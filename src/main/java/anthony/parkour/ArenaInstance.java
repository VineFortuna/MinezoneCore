package anthony.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ArenaInstance {

	public Location startLoc = new Location(Bukkit.getWorld("lobby-1"), 0, 100, 0);
	public Location endLoc = new Location(Bukkit.getWorld("lobby-1"), 0, 100, 0);
	public Vector leaderboardLoc = new Vector(0, 100, 0);
	public List<Location> checkpoints = new ArrayList<>();
	public List<BlockVector> checkpointBlocks = new ArrayList<>();
	
	public ArenaInstance setStartLoc(Location loc) {
		this.startLoc = loc;
		return this;
	}

	public ArenaInstance setEndLoc(Location loc) {
		this.endLoc = loc;
		return this;
	}

	public ArenaInstance setLeaderboardLoc(Vector v) {
		this.leaderboardLoc = v;
		return this;
	}

	public ArenaInstance setCheckpoints(List<Location> checkpoints) {
		this.checkpoints = checkpoints;
		for (Location loc : checkpoints) {
			checkpointBlocks.add(loc.toVector().toBlockVector());
		}
		return this;
	}

}
