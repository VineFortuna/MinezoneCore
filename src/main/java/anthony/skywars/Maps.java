package anthony.skywars;

import org.bukkit.util.Vector;

public enum Maps {

	Fortress("SkyWars", new MapInstance("skywars").setLobbyLoc(new Vector(0.526, 126, 0.524))
			.setSpawnPos(new Vector(0.494, 83, -18.554), new Vector(19.527, 83, 0.492), new Vector(0.491, 83, 19.525),
					new Vector(-18.513, 83, 0.512))
			.setSpecLoc(new Vector(0.500, 83, 0.500))
			.setChestLoc(new Vector(22.520, 81, -3.532), new Vector(-3.505, 81, -21.518),
					new Vector(20.523, 80, 18.493), new Vector(4.476, 81, 22.428), new Vector(0.517, 77, 23.434),
					new Vector(-21.517, 81, 4.469), new Vector(22.465, 81, -3.495), new Vector(23.481, 77, 0.457),
					new Vector(0.440, 77, 23.403), new Vector(-22.529, 77, 0.475), new Vector(0.448, 77, -22.497))
			.setOPChestLoc(new Vector(5.524, 81, 0.482), new Vector(0.514, 81, 5.500), new Vector(-4.578, 81, 0.465),
					new Vector(0.486, 81, -3.575), new Vector(0.480, 78, -3.502), new Vector(0.481, 78, 4.426))
			.setMaxPlayers(4)),
	Default("SkyWars", new MapInstance("skywars2").setLobbyLoc(new Vector(26.494, 115, 0.531))
			.setSpawnPos(new Vector(28.522, 67, -25.524), new Vector(51.547, 67, -48.527),
					new Vector(75.517, 67, -25.517), new Vector(28.456, 67, 26.500))
			.setSpecLoc(new Vector(25.500, 67, 0.500))
			.setChestLoc(new Vector(34.455, 66, 26.478), new Vector(26.490, 67, 26.463), new Vector(27.503, 63, 26.464),
					new Vector(77.422, 67, -25.512), new Vector(69.541, 66, -25.556), new Vector(76.432, 63, -25.510),
					new Vector(51.488, 67, -50.499), new Vector(51.487, 66, -42.548), new Vector(51.481, 63, -49.497),
					new Vector(26.493, 67, -25.524), new Vector(34.387, 66, -25.503), new Vector(27.514, 63, -25.533))
			.setOPChestLoc(new Vector(51.469, 67, -25.603), new Vector(51.517, 68, 4.439),
					new Vector(56.487, 68, -3.477), new Vector(46.448, 66, 3.396), new Vector(51.449, 67, 26.429),
					new Vector(0.501, 66, 26.472), new Vector(0.505, 66, -25.474), new Vector(7.539, 67, -0.534),
					new Vector(4.366, 65, -2.511))
			.setMaxPlayers(4)), 
	Default2("SkyWars", new MapInstance("skywars3").setLobbyLoc(new Vector(1000.517, 103, 997.462))
					.setSpawnPos(new Vector(1027.416, 110, 948.404), new Vector(1051.513, 110, 971.415),
							new Vector(1004.484, 110, 1023.491), new Vector(1004.494, 110, 971.535))
					.setSpecLoc(new Vector(25.500, 67, 0.500))
					.setChestLoc(new Vector(34.455, 66, 26.478), new Vector(26.490, 67, 26.463), new Vector(27.503, 63, 26.464),
							new Vector(77.422, 67, -25.512), new Vector(69.541, 66, -25.556), new Vector(76.432, 63, -25.510),
							new Vector(51.488, 67, -50.499), new Vector(51.487, 66, -42.548), new Vector(51.481, 63, -49.497),
							new Vector(26.493, 67, -25.524), new Vector(34.387, 66, -25.503), new Vector(27.514, 63, -25.533))
					.setOPChestLoc(new Vector(51.469, 67, -25.603), new Vector(51.517, 68, 4.439),
							new Vector(56.487, 68, -3.477), new Vector(46.448, 66, 3.396), new Vector(51.449, 67, 26.429),
							new Vector(0.501, 66, 26.472), new Vector(0.505, 66, -25.474), new Vector(7.539, 67, -0.534),
							new Vector(4.366, 65, -2.511))
					.setMaxPlayers(4));

	private final MapInstance instance;
	private final String name;

	Maps(String name, MapInstance instance) {
		this.name = name;
		this.instance = instance;
	}

	public MapInstance GetInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

}
