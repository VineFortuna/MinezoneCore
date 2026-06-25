package anthony.flagwars;

import org.bukkit.DyeColor;

public final class FWGameConstants {

	/** Bukkit world folder name for the arena, placed on the server by the user. */
	public static final String MAP_WORLD_NAME = "Speedway";

	public static final int MIN_PLAYERS = 2;
	public static final int STARTING_COUNTDOWN_SECONDS = 30;

	/** Capture target never goes higher than this, no matter how many players are in the match. */
	public static final int MAX_WINNING_FLAG_CAPTURES = 4;

	/** How close a carrier must be to their own base's flag spot to bank a capture. */
	public static final double FLAG_CAPTURE_RADIUS = 3.0;

	/** Indexed by (siteId - 1), matching FWMapConfig.TEAM_COUNT (8). */
	public static final DyeColor[] TEAM_COLORS = {
			DyeColor.LIME, DyeColor.YELLOW, DyeColor.CYAN, DyeColor.BLUE,
			DyeColor.LIGHT_BLUE, DyeColor.WHITE, DyeColor.PINK, DyeColor.GRAY
	};
	public static final String[] TEAM_NAMES = {
			"Lime", "Yellow", "Cyan", "Blue", "Light Blue", "White", "Pink", "Gray"
	};

	private FWGameConstants() {
	}

	/**
	 * Scales the win target down for small matches so they can't drag on forever waiting for 4
	 * captures that may never happen - one fewer than the number of occupied islands (not raw
	 * player count, which would overcount in Duos - a team can only ever capture each other
	 * team's flag once, so the ceiling is always teamCount - 1), floored at 1 and capped at
	 * MAX_WINNING_FLAG_CAPTURES. E.g. 2 teams need 1 capture, 3 need 2, 4 need 3, 5+ need the full 4.
	 */
	public static int requiredCapturesFor(int teamCount) {
		return Math.max(1, Math.min(teamCount - 1, MAX_WINNING_FLAG_CAPTURES));
	}

	/**
	 * Resolves a team color name (e.g. "lime", "LightBlue", "light blue") to its
	 * site id (1-based). Used by /fwsetup so staff identify teams by color
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

	/** Display-friendly list of valid /fwsetup color identifiers, e.g. "Lime, Yellow, ...". */
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

	/**
	 * Compact tag for the above-head nametag: the first letter of each word - "Lime" -> "L",
	 * "Light Blue" -> "LB" - rather than the full name. Multi-word names naturally get more than
	 * one letter, which is what keeps "Lime" and "Light Blue" from both collapsing to the same "L".
	 */
	public static String abbreviateTeamName(String name) {
		StringBuilder builder = new StringBuilder();
		for (String word : name.split(" ")) {
			if (!word.isEmpty()) builder.append(Character.toUpperCase(word.charAt(0)));
		}
		return builder.toString();
	}
}
