package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class StatsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public StatsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Your Statistics").build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		if (data != null) {
			contents.set(1, 4,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.DIAMOND_SWORD),
							"" + ChatColor.RESET + ChatColor.GREEN + "Wins: " + ChatColor.RESET + data.wins,
							"" + ChatColor.RESET + ChatColor.GREEN + "Winstreak: " + ChatColor.RESET + data.winstreak,
							"" + ChatColor.RESET + ChatColor.GREEN + "Flawless Wins: " + ChatColor.RESET
									+ data.flawlessWins,
							"" + ChatColor.RESET + ChatColor.GREEN + "Losses: " + ChatColor.RESET + data.losses,
							"" + ChatColor.RESET + ChatColor.GREEN + "Match MVPs: " + ChatColor.RESET
									+ data.matchMvps, "",
							"" + ChatColor.RESET + ChatColor.GREEN + "Kills: " + ChatColor.RESET + data.kills,
							"" + ChatColor.RESET + ChatColor.GREEN + "Deaths: " + ChatColor.RESET + data.deaths), e -> {
							}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
