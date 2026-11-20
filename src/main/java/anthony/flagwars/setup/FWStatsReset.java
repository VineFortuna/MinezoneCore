package anthony.flagwars.setup;

import anthony.SuperCraftBrawl.playerdata.DatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The SQL and wording behind /fwsetup resetstats. Kept apart from the command so it can be checked on its own.
 * <p>
 * A player's Flag Wars stats live in three places, and a reset has to clear all of them: the FW columns of
 * PlayerData, their rows in fw_stat_snapshots (the totals the daily, weekly and monthly leaderboards count
 * from) and their rows in fw_period_winstreaks. Zeroing only the totals would leave the old snapshots behind,
 * and a leaderboard period shows the total minus its snapshot, so the player would show a negative amount and
 * stay off the boards until they passed their old total again.
 */
public final class FWStatsReset {

	/** A real Minecraft name, so it is safe to put in a query. */
	private static final Pattern PLAYER_NAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");
	private static final Pattern UUID_TEXT = Pattern.compile(
			"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	private FWStatsReset() {
	}

	public static boolean isPlayerName(String name) {
		return name != null && PLAYER_NAME.matcher(name).matches();
	}

	/** The query that finds a player by the last name they played with: UUID, name, then the eight stats. */
	public static String lookupSql(String playerName) {
		if (!isPlayerName(playerName)) throw new IllegalArgumentException("Not a player name: " + playerName);
		return "SELECT UUID, LastPlayerName, " + String.join(", ", DatabaseManager.FLAG_WARS_STAT_COLUMNS)
				+ " FROM PlayerData WHERE LastPlayerName = '" + playerName + "'";
	}

	/** The statements that reset one player's Flag Wars stats, or everyone's when the UUID is null. */
	public static List<String> resetStatements(String uuid) {
		if (uuid != null && !UUID_TEXT.matcher(uuid).matches()) throw new IllegalArgumentException("Not a UUID: " + uuid);

		StringBuilder set = new StringBuilder();
		for (String column : DatabaseManager.FLAG_WARS_STAT_COLUMNS) {
			if (set.length() > 0) set.append(", ");
			set.append(column).append(" = 0");
		}

		String playerData = uuid == null ? "" : " WHERE UUID = '" + uuid + "'";
		String byUuid = uuid == null ? "" : " WHERE uuid = '" + uuid + "'";

		List<String> statements = new ArrayList<>();
		statements.add("UPDATE PlayerData SET " + set + playerData);
		statements.add("DELETE FROM fw_stat_snapshots" + byUuid);
		statements.add("DELETE FROM fw_period_winstreaks" + byUuid);
		return statements;
	}

	/** What a player had, in a line, for the chat message and the console log of what was erased. */
	public static String describe(int wins, int matchMvps, int losses, int winstreak, int bestWinstreak,
			int kills, int deaths, int flagsCaptured) {
		return "Wins " + wins + ", MVPs " + matchMvps + ", Losses " + losses + ", Winstreak " + winstreak
				+ " (best " + bestWinstreak + "), Kills " + kills + ", Deaths " + deaths
				+ ", Flags captured " + flagsCaptured;
	}
}
