package anthony.flagwars.vote;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Pre-game lobby items - mirrors SuperCraftBros' own game lobby items (Vote / My Profile / Leave Game). */
public final class FWLobbyItems {

	private static final ItemStack VOTE_PAPER = ItemHelper.setDetails(new ItemStack(Material.PAPER),
			"&eVote &7(Right Click)");

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

	/** The vote paper - only handed out once the countdown is actually running (2+ players), mirroring SCB's StartGameTimer. */
	public static void giveVotePaper(Player player) {
		if (!player.getInventory().contains(VOTE_PAPER)) {
			player.getInventory().addItem(VOTE_PAPER);
		}
	}

	/** Mirrors SCB removing the vote paper once its settings get locked in 5 seconds before the match starts. */
	public static void removeVotePaper(Player player) {
		player.getInventory().remove(VOTE_PAPER);
	}

	/** Compass (spectate a player) + Leave - mirrors SCB's own LobbyItems#spectatorItems exactly, slots included. */
	public static void spectatorItems(Player player) {
		player.getInventory().setItem(0, SPECTATE_COMPASS);
		player.getInventory().setItem(8, SPECTATOR_LEAVE);
	}
}
