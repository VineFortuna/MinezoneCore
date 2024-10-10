package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class BoardSettingsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public BoardSettingsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + "Board Settings").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		String line = "";

		if (data != null) {
			if (data.cwm == 0)
				line = main.color("&c&lDISABLED");
			else
				line = main.color("&a&lENABLED");
			
			contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.LADDER),
					main.color("&eLeaderboard Type"), "", line), e -> {
						if (data.cwm == 1) {
							player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have disabled " + ChatColor.YELLOW + "Custom Win Messages");
							data.cwm = 0;
						} else {
							data.cwm = 1;
							player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have enabled " + ChatColor.YELLOW + "Custom Win Messages");
						}
						
						inv.close(player);
					}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

}
