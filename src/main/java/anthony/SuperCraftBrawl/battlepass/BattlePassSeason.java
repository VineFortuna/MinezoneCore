package anthony.SuperCraftBrawl.battlepass;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Settings for the currently running Battle Pass season.
 * When a new season starts, change ID (so everyone's progress starts fresh in the DB),
 * the dates, and the rewards in BattlePassRewards.
 */
public final class BattlePassSeason {

	/** Stored in every DB row, so old seasons' progress is kept but never mixed with the new one. */
	public static final String ID = "christmas_2026";
	public static final String NAME = "&c&lChristmas &f&lPass";

	public static final ZoneId ZONE = ZoneId.of("America/New_York");
	public static final LocalDate START = LocalDate.of(2026, 12, 1);
	public static final LocalDate END = LocalDate.of(2026, 12, 31); // inclusive

	public static final int MAX_TIER = 20;
	public static final int XP_PER_TIER = 500;

	// XP sources
	public static final int XP_WIN = 100;
	public static final int XP_KILL = 10;
	public static final int XP_GAME_PLAYED = 25;

	// TESTING FLAGS - flip both to false before the real launch
	/** Everyone is treated as owning the Premium Pass. */
	public static final boolean PREMIUM_FREE_FOR_TESTING = true;
	/** Ignore START/END so the pass can be earned and claimed right now. */
	public static final boolean IGNORE_DATES_FOR_TESTING = true;

	private BattlePassSeason() {
	}

	public static LocalDate today() {
		return LocalDate.now(ZONE);
	}

	public static boolean hasStarted() {
		return IGNORE_DATES_FOR_TESTING || !today().isBefore(START);
	}

	public static boolean hasEnded() {
		return !IGNORE_DATES_FOR_TESTING && today().isAfter(END);
	}

	public static boolean isActive() {
		return hasStarted() && !hasEnded();
	}

	/** XP is capped at the amount needed for the final tier. */
	public static int maxXp() {
		return MAX_TIER * XP_PER_TIER;
	}

	public static int tierForXp(int xp) {
		return Math.min(MAX_TIER, xp / XP_PER_TIER);
	}
}
