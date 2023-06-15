package anthony.skywars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

public class MapInstance {

	public String worldName;
	public Vector lobbyLoc = new Vector(0, 100, 0);
	public Vector specLoc = new Vector(0, 100, 0);
	public List<Vector> spawnPos = new ArrayList<>();
	public List<Vector> chestLocs = new ArrayList<>();
	public List<Vector> opChestLocs = new ArrayList<>();
	private int maxPlayers = 4; // Default

	public MapInstance(String worldName) {
		this.worldName = worldName;
	}

	// Getters:

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	// Setters:

	public MapInstance setLobbyLoc(Vector lobbyLoc) {
		this.lobbyLoc = lobbyLoc;
		return this;
	}
	
	public MapInstance setSpecLoc(Vector specLoc) {
		this.specLoc = specLoc;
		return this;
	}

	public MapInstance setSpawnPos(Vector... vec) {
		for (Vector v : vec)
			this.spawnPos.add(v);
		return this;
	}

	public MapInstance setChestLoc(Vector... vec) {
		for (Vector v : vec)
			this.chestLocs.add(v);
		return this;
	}

	public MapInstance setOPChestLoc(Vector... vec) {
		for (Vector v : vec)
			this.opChestLocs.add(v);
		return this;
	}

	public MapInstance setMaxPlayers(int maxPl) {
		this.maxPlayers = maxPl;
		return this;
	}
}
