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
			.setMaxPlayers(4)),
	Modern("SkyWars",
			new MapInstance("modern").setLobbyLoc(new Vector(1000.403, 191, 980.492))
					.setSpawnPos(new Vector(1053.567, 105, 980.467), new Vector(1044.531, 105, 1004.497),
							new Vector(1024.464, 105, 1024.533), new Vector(1000.488, 105, 1033.485),
							new Vector(976.463, 105, 1024.526), new Vector(956.418, 105, 1004.469),
							new Vector(947.447, 105, 980.495), new Vector(956.372, 105, 956.488),
							new Vector(974.421, 105, 938.499), new Vector(1000.479, 105, 927.408),
							new Vector(1024.494, 105, 936.422), new Vector(1044.480, 105, 956.463))
					.setSpecLoc(new Vector(1000.500, 116, 980.500))
					.setChestLoc(new Vector(1047.479, 101, 981.502), new Vector(1049.451, 101, 982.430),
							new Vector(1050.488, 101, 978.461), new Vector(1038.504, 101, 1005.480),
							new Vector(1040.479, 101, 1006.462), new Vector(1041.484, 101, 1002.487),
							new Vector(1020.476, 101, 1023.478), new Vector(1022.462, 101, 1024.434),
							new Vector(1023.491, 101, 1020.509), new Vector(999.482, 101, 1027.486),
							new Vector(998.488, 101, 1029.472), new Vector(1002.403, 101, 1030.477),
							new Vector(975.475, 101, 1018.498), new Vector(974.512, 101, 1020.465),
							new Vector(978.426, 101, 1021.490), new Vector(957.469, 101, 1000.509),
							new Vector(956.537, 101, 1002.488), new Vector(960.384, 101, 1003.494),
							new Vector(953.459, 101, 979.487), new Vector(951.482, 101, 978.504),
							new Vector(950.494, 101, 982.432), new Vector(962.428, 101, 955.506),
							new Vector(960.467, 101, 954.512), new Vector(959.477, 101, 958.434),
							new Vector(980.425, 101, 937.490), new Vector(978.499, 101, 936.528),
							new Vector(977.464, 101, 940.513), new Vector(1001.472, 101, 933.420),
							new Vector(1002.460, 101, 931.483), new Vector(998.541, 101, 930.461),
							new Vector(1025.478, 101, 942.397), new Vector(1026.438, 101, 940.501),
							new Vector(1022.538, 101, 939.476), new Vector(1043.485, 101, 960.461),
							new Vector(1044.445, 101, 958.464), new Vector(1040.492, 101, 957.470))
					.setOPChestLoc(new Vector(1028.518, 103, 980.490), new Vector(1000.510, 103, 1008.493),
							new Vector(972.410, 103, 980.461), new Vector(1000.472, 103, 952.436),
							new Vector(1000.480, 102, 978.459), new Vector(1002.484, 102, 980.486),
							new Vector(1000.472, 102, 982.520), new Vector(998.401, 102, 980.475),
							new Vector(998.431, 111, 980.499), new Vector(1000.490, 111, 978.441),
							new Vector(1002.517, 111, 980.485), new Vector(1000.489, 111, 982.526))
					.setMaxPlayers(12));

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
