package anthony.SuperCraftBrawl.gui.battlepass;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class BattlePassGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public BattlePassGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Battle Pass").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		contents.set(0, 0,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WOOD), main.color("&eFree Pass"),
						data.challenge1 == 1 ? main.color("&a&lCOMPLETED") : "",
						main.color("&rGet a win with Pig class")), e -> {
							
						}));
		
		contents.set(0, 1,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR), main.color("&ePremium Battle Pass"),
						 main.color("&Premium pass"),
						main.color("&rPremium pass")), e -> {
							new PremiumPassGUI(main).inv.open(player);
						}));

	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
