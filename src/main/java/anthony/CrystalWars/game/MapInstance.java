package anthony.CrystalWars.game;

import org.bukkit.util.Vector;

public class MapInstance {
	
	public String worldName;
	public Vector lobbyLoc = new Vector(0, 100, 0);
	public Vector bluePos = new Vector(0, 100, 0);
	public Vector redPos = new Vector(0, 100, 0);
	public Vector center = new Vector(0, 100, 0), center2 = new Vector(0, 100, 0);
	public double boundsX, boundsZ, boundsX2, boundsZ2;
	
	public MapInstance(String worldName) {
		this.worldName = worldName;
	}
	
	public MapInstance setLobbyLoc(Vector lobbyLoc) {
		this.lobbyLoc = lobbyLoc;
		return this;
	}
	
	public MapInstance setBluePos(Vector bluePos) {
		this.bluePos = bluePos;
		return this;
	}
	
	public MapInstance setRedPos(Vector redPos) {
		this.redPos = redPos;
		return this;
	}
	
	public MapInstance setBlueBounds(Vector center, double boundsX, double boundsZ) {
		this.center = center;
		this.boundsX = boundsX;
		this.boundsZ = boundsZ;
		return this;
	}
	
	public MapInstance setRedBounds(Vector center2, double boundsX2, double boundsZ2) {
		this.center2 = center2;
		this.boundsX2 = boundsX2;
		this.boundsZ2 = boundsZ2;
		return this;
	}

}
