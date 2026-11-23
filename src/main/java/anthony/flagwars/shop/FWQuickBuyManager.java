package anthony.flagwars.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tracks each player's personal Quick Buy - Hypixel Bedwars lets you shift-click any shop item to
 * pin it to your Quick Buy tab. Persisted on PlayerData.flagWarsQuickBuy (a comma-separated item
 * name list, same convention as fishingWarps/treasureLoc) so it survives across matches and server
 * restarts instead of resetting every game.
 *
 * Stored entries are either an added item's name, in the order the player added it, or "~name" for
 * a default item the player hid. Defaults the player never touched aren't stored at all, they are
 * treated as added first, in the catalog's default order.
 */
public final class FWQuickBuyManager {

	/** Most items a player's Quick Buy can hold (three rows of seven). */
	public static final int LIMIT = 21;

	/** Prefix stored in the list for default items the player has explicitly hidden. */
	private static final String REMOVED_PREFIX = "~";

	private FWQuickBuyManager() {
	}

	/**
	 * The player's Quick Buy item names in the order they were added: untouched defaults first
	 * (catalog order), then everything else in the order it was added. A default that was hidden and
	 * then added back counts as freshly added, so it lands at the end.
	 */
	public static List<String> getOrderedNames(Core main, Player player, List<String> defaultNames) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		List<String> stored = data != null ? data.getFlagWarsQuickBuy() : new ArrayList<String>();

		Set<String> hidden = new HashSet<>();
		List<String> added = new ArrayList<>();
		for (String entry : stored) {
			if (entry.isEmpty()) continue;
			if (entry.startsWith(REMOVED_PREFIX)) {
				hidden.add(entry.substring(REMOVED_PREFIX.length()));
			} else {
				added.add(entry);
			}
		}

		List<String> ordered = new ArrayList<>();
		for (String name : defaultNames) {
			if (!hidden.contains(name) && !added.contains(name)) ordered.add(name);
		}
		ordered.addAll(added);
		return ordered;
	}

	/** Puts the item at the end of the player's Quick Buy. Saved immediately so it isn't lost on a crash/restart. */
	public static void add(Core main, Player player, String itemName, boolean isDefault) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return;

		if (isDefault) data.removeFlagWarsQuickBuy(REMOVED_PREFIX + itemName);
		data.addFlagWarsQuickBuy(itemName);
		main.getDataManager().saveData(data);
	}

	/** Takes the item out of the player's Quick Buy. A default is remembered as hidden so it doesn't come back on its own. */
	public static void remove(Core main, Player player, String itemName, boolean isDefault) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return;

		data.removeFlagWarsQuickBuy(itemName);
		if (isDefault) data.addFlagWarsQuickBuy(REMOVED_PREFIX + itemName);
		main.getDataManager().saveData(data);
	}
}
