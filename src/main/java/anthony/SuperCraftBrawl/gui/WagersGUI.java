package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class WagersGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private Player target;

	public WagersGUI(Core main, Player target) {
		this.target = target;
		this.main = main;
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title("" + target.getName() + " Wager").build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		PlayerData data2 = main.getDataManager().getPlayerData(target);
		int tokens = 0;

		if (data != null && data2 != null) {
			contents.set(0, 0, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EMERALD, 50),
					"" + ChatColor.YELLOW + "50 Tokens"), e -> {
						if (data.tokens >= 50 && data2.tokens >= 50)
							main.wagers.put(target, player);

						player.sendMessage("Wager created between " + player.getName() + " & " + target.getName());
						inv.close(player);
					}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
