package anthony.flagwars.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;

/**
 * Tracks each player's personal Quick Buy additions - Hypixel Bedwars lets you shift-click any
 * shop item to pin it to your Quick Buy tab. Persisted on PlayerData.flagWarsQuickBuy (a
 * comma-separated item name list, same convention as fishingWarps/treasureLoc) so it survives
 * across matches and server restarts instead of resetting every game.
 */
public final class FWQuickBuyManager {

	/** Prefix stored in the set for default items the player has explicitly hidden. */
	private static final String REMOVED_PREFIX = "~";

	private FWQuickBuyManager() {
	}

	public static boolean isAdded(Core main, Player player, String itemName) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		return data != null && data.getFlagWarsQuickBuy().contains(itemName);
	}

	/** Adds the item if it wasn't there, removes it if it was. Returns the new state (true = now added). Saved immediately so it isn't lost on a crash/restart. */
	public static boolean toggle(Core main, Player player, String itemName) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return false;

		boolean nowAdded = !data.getFlagWarsQuickBuy().contains(itemName);
		if (nowAdded) {
			data.addFlagWarsQuickBuy(itemName);
		} else {
			data.removeFlagWarsQuickBuy(itemName);
		}
		main.getDataManager().saveData(data);
		return nowAdded;
	}

	/** Whether the player has explicitly hidden a default Quick Buy item. */
	public static boolean isDefaultRemoved(Core main, Player player, String itemName) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		return data != null && data.getFlagWarsQuickBuy().contains(REMOVED_PREFIX + itemName);
	}

	/**
	 * Toggles a default item's visibility. Returns true = item is now visible (shown in Quick Buy),
	 * false = item is now hidden (removed from Quick Buy).
	 */
	public static boolean toggleDefault(Core main, Player player, String itemName) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return true;

		String key = REMOVED_PREFIX + itemName;
		boolean nowHidden = !data.getFlagWarsQuickBuy().contains(key);
		if (nowHidden) {
			data.addFlagWarsQuickBuy(key);
		} else {
			data.removeFlagWarsQuickBuy(key);
		}
		main.getDataManager().saveData(data);
		return !nowHidden; // true = now visible
	}

	/** Returns item names the player has personally added, in insertion order, excluding removed-default markers. */
	public static LinkedHashSet<String> getAdded(Core main, Player player) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data == null) return new LinkedHashSet<>();
		LinkedHashSet<String> result = new LinkedHashSet<>();
		for (String name : data.getFlagWarsQuickBuy()) {
			if (!name.startsWith(REMOVED_PREFIX)) result.add(name);
		}
		return result;
	}
}
