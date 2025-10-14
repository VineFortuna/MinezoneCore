package anthony.SuperCraftBrawl.lobbyitems;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;

public class LobbyItems {

	private Core core;

	public LobbyItems(Core core) {
		this.core = core;
	}

    /*
    * This function gives a player the main lobby items
    * when in lobby
     */
    public void mainLobbyItems(Player player) {
        if (core.getCommands() != null) {
            player.getInventory().setItem(1,
                    ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER), "&bActive Games &7(Right Click)"));
            player.getInventory().setItem(3,
                    ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK), "&bClasses &7(Right Click)"));
            player.getInventory().setItem(8,
                    ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR), "&bChallenges &7(Right Click)"));
        }
        player.getInventory().setItem(0,
                ItemHelper.setDetails(new ItemStack(Material.COMPASS), "&bGame Selector &7(Right Click)"));

        player.getInventory().setItem(4,
                ItemHelper.setDetails(new ItemStack(Material.CHEST), "&bCosmetics &7(Right Click)"));
        ItemStack stats = ItemHelper.createSkullHeadPlayer(1, player.getName());
        player.getInventory().setItem(7, ItemHelper.setDetails(stats, "&bProfile &7(Right Click)"));

        player.getInventory().setItem(5, core.getFishingRod(player));

        if (core.tournament) {
            ItemStack tournament = ItemHelper.createSkullTexture(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM0YTU5MmE3OTM5N2E4ZGYzOTk3YzQzMDkxNjk0ZmMyZmI3NmM4ODNhNzZjY2U4OWYwMjI3ZTVjOWYxZGZlIn19fQ==");
            player.getInventory().setItem(2, ItemHelper.setDetails(tournament, "&7>&f>&6&lTournament&f<&7<"));
        }
        player.setAllowFlight(true);
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
	
	/*
	 * This function assigns Spectator items to a player
	 * spectating a game
	 */
	public void spectatorItems(Player player) {
		ItemStack spec = ItemHelper.setDetails(new ItemStack(Material.COMPASS),
				core.color("&bSpectate &7(Right Click)"), "", core.color("&7Click to spectate a player!"));
		player.getInventory().setItem(0, spec);
		ItemStack leave = ItemHelper.setDetails(new ItemStack(Material.BARRIER),
				core.color("&bLeave &7(Right Click)"), "", core.color("&7Click to leave your game"));
		player.getInventory().setItem(8, leave);
	}

}
