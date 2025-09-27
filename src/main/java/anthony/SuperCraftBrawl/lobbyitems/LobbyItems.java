package anthony.SuperCraftBrawl.lobbyitems;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.util.ItemHelper;

public class LobbyItems {

	private Core core;

	public LobbyItems(Core core) {
		this.core = core;
	}

	/*
	 * This function gives a player the SCB game lobby items when
	 * they join a match
	 */
	public void gameLobbyItems(Player player) {
		if (player.getWorld() != core.getLobbyWorld()) {
			player.getInventory().clear();

			ItemStack classes = ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK),
					"&bClasses &7(Right Click)", "", "&7Click to choose a class");
			ItemStack cosmetics = ItemHelper.setDetails(new ItemStack(Material.CHEST), "&bCosmetics &7(Right Click)",
					"", "&7Click to open your cosmetics");
			ItemStack stats = ItemHelper.createSkullHeadPlayer(1, player.getName());
			ItemStack leaveGame = ItemHelper.setDetails(new ItemStack(Material.BARRIER), "&bLeave Game &7(Right Click)", "",
					"&7Click to leave your game");
			
			player.getInventory().setItem(0, classes);
			player.getInventory().setItem(4, cosmetics);
			player.getInventory().setItem(7,
					ItemHelper.setDetails(stats, "&bMy Profile &7(Right Click)", "", "&7Click to see your profile"));
			player.getInventory().setItem(8, leaveGame);
		}
	}
	
	/*
	 * This function gives a player the SCB Vote paper wheen
	 * they join a game to vote for game settings
	 */
	public void addVotePaper(Player player) {
		
	}

}
