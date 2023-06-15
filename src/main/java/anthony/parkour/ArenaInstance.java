package anthony.parkour;

import org.bukkit.util.Vector;

public class ArenaInstance {
	
	public Vector spawnLoc = new Vector(0, 100, 0);
	public Vector center = new Vector(0, 100, 0);
	public double boundsX, boundsY, boundsZ;
	
	public ArenaInstance setSpawnLoc(Vector v) {
		this.spawnLoc = v;
		return this;
	}
	
	public ArenaInstance setBounds(Vector center, double boundsX, double boundsY, double boundsZ) {
		this.center = center;
		this.boundsX = boundsX;
		this.boundsY = boundsY;
		this.boundsZ = boundsZ;
		return this;
	}

}
