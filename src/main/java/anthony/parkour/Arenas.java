package anthony.parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Arrays;

public enum Arenas {

	MainParkour(
			1,
			"Main",
			new ArenaInstance()
					.setStartLoc(new Location(Bukkit.getWorld("lobby-1"), 189, 105, 567, 160, 0))
					.setEndLoc(new Location(Bukkit.getWorld("lobby-1"), 297, 92, 501))
					.setLeaderboardLoc(new Vector(194.5, 107, 568.5))
					.setCheckpoints(
							Arrays.asList(
									new Location(Bukkit.getWorld("lobby-1"), 185, 109, 520, -150, 0),
									new Location(Bukkit.getWorld("lobby-1"), 223, 123, 487, -90, 0),
									new Location(Bukkit.getWorld("lobby-1"), 271, 96, 489, -125, 25)
							)
					)
					.setTokenReward(250)
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

	public Location getCheckpoint(int i) {
		return this.getInstance().checkpoints.get(i);
	}

	public static Arenas getById(int id) {
		for (Arenas arena : values()) {
			if (arena.id == id) {
				return arena;
			}
		}
		return null;
	}
}
