package anthony.villagerdefense;

import org.bukkit.DyeColor;

public final class VDGameConstants {

	/** Bukkit world folder name for the arena, placed on the server by the user. */
	public static final String MAP_WORLD_NAME = "VillagerDefenseMap";

	public static final int MIN_PLAYERS = 2;
	public static final int STARTING_COUNTDOWN_SECONDS = 30;

	/** Indexed by (siteId - 1), matching VDMapConfig.TEAM_COUNT (8). */
	public static final DyeColor[] TEAM_COLORS = {
			DyeColor.RED, DyeColor.BLUE, DyeColor.LIME, DyeColor.YELLOW,
			DyeColor.CYAN, DyeColor.WHITE, DyeColor.PINK, DyeColor.GRAY
	};
	public static final String[] TEAM_NAMES = {
			"Red", "Blue", "Green", "Yellow", "Aqua", "White", "Pink", "Gray"
	};

	private VDGameConstants() {
	}
}
