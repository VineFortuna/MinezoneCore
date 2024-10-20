package anthony.SuperCraftBrawl.Game.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Game.Game;
import anthony.SuperCraftBrawl.Game.GameType;

public class MapInstance {
	public String worldName;
	public GameType gameType = GameType.CLASSIC;
	public MapSource mapSource;
	public Game two_vs_two = Game.FREE_FOR_ALL;
	public List<Vector> spawnPos = new ArrayList<>();
	public Vector lobbyLoc = new Vector(0, 100, 0);
	public Vector specLoc = new Vector(0, 100, 0);
	public Vector center = new Vector(0, 100, 0);
	public Vector signLoc = new Vector(0, 100, 0);
	public double boundsX, boundsZ;
	public boolean mapEnabled = true;

	public MapInstance(String worldName) {
		this.worldName = worldName;
	}
	
	public MapInstance setSource(MapSource mapSource) {
		this.mapSource = mapSource;
		return this;
	}

	public MapInstance setGameType(GameType gameType) {
		this.gameType = gameType;
		return this;
	}
	
	public MapInstance setSignLoc(Vector loc) {
		this.signLoc = loc;
		return this;
	}
	
	public MapInstance setTwos(Game two_vs_two) {
		this.two_vs_two = two_vs_two;
		return this;
	}

	public MapInstance setSpawnPos(Vector... vec) {
		for (Vector v : vec)
			spawnPos.add(v);
		return this;
	}

	public MapInstance setLobbyLoc(Vector vec2) {
		lobbyLoc = vec2;
		return this;
	}

	public MapInstance setSpecLoc(Vector vec3) {
		specLoc = vec3;
		return this;
	}

	public MapInstance setBounds(Vector center, double boundsX, double boundsZ) {
		this.center = center;
		this.boundsX = boundsX;
		this.boundsZ = boundsZ;
		return this;
	}
	
	public MapInstance setEnabled(boolean mapEnabled) {
		this.mapEnabled = mapEnabled;
		return this;
	}
}
