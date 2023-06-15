package anthony.CrystalWars.game;

import org.bukkit.util.Vector;

public enum Maps {

	Default1("Default1", new MapInstance("testtt").setLobbyLoc(new Vector(0.463, 118, 0.487))
			.setBluePos(new Vector(67.555, 67, 0.486)).setRedPos(new Vector(0.447, 67, 67.463))
			.setBlueBounds(new Vector(56.501, 67, 0.491), 5, 5).setRedBounds(new Vector(0.457, 67, 56.513), 5, 5));

	private String name;
	private MapInstance i;

	Maps(String name, MapInstance i) {
		this.name = name;
		this.i = i;
	}

	public MapInstance getMapInstance() {
		return this.i;
	}

}
