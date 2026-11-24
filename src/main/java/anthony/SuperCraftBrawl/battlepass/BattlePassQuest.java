package anthony.SuperCraftBrawl.battlepass;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Every Battle Pass quest. They are all available for the whole season and can be finished whenever, once each.
 * The order here is the order of the quests menu.
 *
 * To add a quest: add it here, then call Core#battlePassQuest or Core#battlePassQuestReach from the code that knows
 * it happened. The ids are saved in the database, so never rename one.
 */
public enum BattlePassQuest {

	// Classic games (custom games give no rewards, so they never count)
	KILLING_SPREE("five_kills", "Killing Spree", Material.DIAMOND_SWORD, 0, 5, Mode.BEST,
			"Get 5 kills in a single game"),
	FIRST_BLOOD("first_blood", "First Blood", Material.REDSTONE, 0, 1, Mode.TOTAL,
			"Get the first kill of a game"),
	VICTORY("win_game", "Victory", Material.GOLD_INGOT, 0, 1, Mode.TOTAL,
			"Win a game"),
	WARM_UP("play_games", "Warm Up", Material.COMPASS, 0, 3, Mode.TOTAL,
			"Play 3 games"),

	// A specific class
	CREEPER_WIN("win_creeper", "Boom Time", Material.SKULL_ITEM, 4, 1, Mode.TOTAL,
			"Win a game with the Creeper class"),

	// Loot drops
	BOMB_SQUAD("use_bomb", "Bomb Squad", Material.TNT, 0, 1, Mode.TOTAL,
			"Find a Bomb loot drop and use it"),
	EARLY_GIFT("open_present", "Early Gift", Material.TRAPPED_CHEST, 0, 1, Mode.TOTAL,
			"Open a Present loot drop"),

	// Flag Wars
	FLAG_WARS_PLAY("play_flagwars", "Flag Bearer", Material.BANNER, 0, 1, Mode.TOTAL,
			"Play a game of Flag Wars"),
	FLAG_WARS_WIN("win_flagwars", "Flag Champion", Material.GOLD_BLOCK, 0, 1, Mode.TOTAL,
			"Win a game of Flag Wars"),
	FLAG_WARS_CAPTURE("capture_flag", "Flag Thief", Material.IRON_SWORD, 0, 1, Mode.TOTAL,
			"Capture a flag in Flag Wars"),

	// Fishing
	GONE_FISHING("catch_fish", "Gone Fishing", Material.FISHING_ROD, 0, 10, Mode.TOTAL,
			"Catch 10 fish"),

	// The lobby
	DAILY_REWARD("claim_daily", "Freebie", Material.EMERALD, 0, 1, Mode.TOTAL,
			"Claim your daily reward");

	/** TOTAL adds up every time it happens, BEST keeps the highest value reached (kills in one game). */
	public enum Mode {
		TOTAL, BEST
	}

	private final String id;
	private final String name;
	private final Material icon;
	private final short iconData;
	private final int goal;
	private final Mode mode;
	private final List<String> description;

	BattlePassQuest(String id, String name, Material icon, int iconData, int goal, Mode mode, String... description) {
		this.id = id;
		this.name = name;
		this.icon = icon;
		this.iconData = (short) iconData;
		this.goal = goal;
		this.mode = mode;
		this.description = Collections.unmodifiableList(Arrays.asList(description));
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getGoal() {
		return goal;
	}

	public Mode getMode() {
		return mode;
	}

	/** Plain description lines (no color codes), each short enough for a menu. */
	public List<String> getDescription() {
		return description;
	}

	public ItemStack createIcon() {
		return new ItemStack(icon, 1, iconData);
	}

	/** Null for an id that no longer exists (an old row in the database). */
	public static BattlePassQuest byId(String id) {
		for (BattlePassQuest quest : values()) {
			if (quest.id.equals(id))
				return quest;
		}
		return null;
	}
}
