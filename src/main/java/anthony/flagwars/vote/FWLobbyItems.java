package anthony.flagwars.vote;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Pre-game lobby items - mirrors SuperCraftBros' own game lobby items (Ready Up / My Profile / Leave Game). */
public final class FWLobbyItems {

	private static final ItemStack SPECTATE_COMPASS = ItemHelper.setDetails(new ItemStack(Material.COMPASS),
			"&eSpectate &7(Right Click)", "", "&7Click to spectate a player!");
	private static final ItemStack SPECTATOR_LEAVE = ItemHelper.setDetails(new ItemStack(Material.BARRIER),
			"&eLeave &7(Right Click)", "", "&7Click to leave your game");

	private FWLobbyItems() {
	}

	/** My Profile + Leave Game - given to every player immediately on join, same as SCB's gameLobbyItems. */
	public static void give(Player player) {
		ItemStack profile = ItemHelper.setDetails(ItemHelper.createSkullHeadPlayer(1, player.getName()),
				"&eMy Profile &7(Right Click)", "", "&7Click to see your profile");
		ItemStack leaveGame = ItemHelper.setDetails(new ItemStack(Material.BARRIER),
				"&eLeave Game &7(Right Click)", "", "&7Click to leave your game");

		player.getInventory().setItem(4, profile);
		player.getInventory().setItem(8, leaveGame);
	}

	/** The ready-up dye: gray while the player isn't ready, lime once they are. Same item as Super Craft Bros' own ready-up. */
	public static ItemStack getReadyItem(boolean ready) {
		if (ready) {
			return ItemHelper.setDetails(new ItemStack(Material.INK_SACK, 1, (short) 10),
					"&aReady &7(Right Click)", "", "&7Click to cancel", "&7The game starts early once", "&7everyone is ready");
		}
		return ItemHelper.setDetails(new ItemStack(Material.INK_SACK, 1, (short) 8),
				"&7Not Ready &7(Right Click)", "", "&7Click to ready up", "&7The game starts early once", "&7everyone is ready");
	}

	/** The waiting lobby has no other dyes. */
	public static boolean isReadyItem(ItemStack item) {
		return item != null && item.getType() == Material.INK_SACK;
	}

	private static int findReadyItemSlot(Player player) {
		ItemStack[] contents = player.getInventory().getContents();
		for (int slot = 0; slot < contents.length; slot++) {
			if (isReadyItem(contents[slot])) return slot;
		}
		return -1;
	}

	/** Gives the player the ready-up dye, showing their current ready state, if they don't have it yet. Only handed out once the countdown is running (2+ players). */
	public static void giveReadyItem(Player player, boolean ready) {
		if (findReadyItemSlot(player) == -1) player.getInventory().addItem(getReadyItem(ready));
	}

	/** Flips the dye the player has to show their new ready state. */
	public static void updateReadyItem(Player player, boolean ready) {
		int slot = findReadyItemSlot(player);
		if (slot != -1) player.getInventory().setItem(slot, getReadyItem(ready));
		player.updateInventory();
	}

	/** Takes the ready-up dye away, once it is too late to ready up (5 seconds before the match starts) or the countdown stops. */
	public static void removeReadyItem(Player player) {
		player.getInventory().remove(Material.INK_SACK);
	}

	/** Compass (spectate a player) + Leave - mirrors SCB's own LobbyItems#spectatorItems exactly, slots included. */
	public static void spectatorItems(Player player) {
		player.getInventory().setItem(0, SPECTATE_COMPASS);
		player.getInventory().setItem(8, SPECTATOR_LEAVE);
	}
}
