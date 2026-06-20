package anthony.villagerdefense;

import org.bukkit.DyeColor;

public final class VDGameConstants {

	/** Bukkit world folder name for the arena, placed on the server by the user. */
	public static final String MAP_WORLD_NAME = "Speedway";

	public static final int MIN_PLAYERS = 2;
	public static final int STARTING_COUNTDOWN_SECONDS = 30;

	/** Indexed by (siteId - 1), matching VDMapConfig.TEAM_COUNT (8). */
	public static final DyeColor[] TEAM_COLORS = {
			DyeColor.LIME, DyeColor.YELLOW, DyeColor.CYAN, DyeColor.BLUE,
			DyeColor.LIGHT_BLUE, DyeColor.WHITE, DyeColor.PINK, DyeColor.GRAY
	};
	public static final String[] TEAM_NAMES = {
			"Lime", "Yellow", "Cyan", "Blue", "Light Blue", "White", "Pink", "Gray"
	};

	private VDGameConstants() {
	}

	/**
	 * Resolves a team color name (e.g. "lime", "LightBlue", "light blue") to its
	 * site id (1-based). Used by /vdsetup so staff identify teams by color
	 * instead of an arbitrary number. Returns null if nothing matches.
	 */
	public static Integer siteIdForColorName(String name) {
		if (name == null) return null;
		String normalized = name.replace(" ", "").replace("_", "");

		for (int i = 0; i < TEAM_NAMES.length; i++) {
			if (TEAM_NAMES[i].replace(" ", "").equalsIgnoreCase(normalized)) {
				return i + 1;
			}
		}
		return null;
	}

	/** Display-friendly list of valid /vdsetup color identifiers, e.g. "Lime, Yellow, ...". */
	public static String colorNameList() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < TEAM_NAMES.length; i++) {
			if (i > 0) builder.append(", ");
			builder.append(TEAM_NAMES[i].replace(" ", ""));
		}
		return builder.toString();
	}

	/** Closest vanilla chat color code (with leading &) for a team's DyeColor, for scoreboards/chat. */
	public static String chatColorCodeFor(DyeColor color) {
		switch (color) {
			case LIME: return "&a";
			case YELLOW: return "&e";
			case CYAN: return "&3";
			case BLUE: return "&9";
			case LIGHT_BLUE: return "&b";
			case WHITE: return "&f";
			case PINK: return "&d";
			case GRAY: return "&7";
			default: return "&7";
		}
	}
}
