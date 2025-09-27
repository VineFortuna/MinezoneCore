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

public class PremiumPassGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public PremiumPassGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Battle Pass").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		contents.set(0, 0,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.REDSTONE), main.color("&b&lTIER 1"),
						"", main.color("&4&l&n[Spooky]"),
						"", main.color("&bCurrent Battle EXP: &70/500")), e -> {
							
						}));
		contents.set(0, 1,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.SUGAR), main.color("&b&lTIER 2"),
						"", main.color("&c&lCandy &rPop Death Effect"),
						"", main.color("&bCurrent Battle EXP: &70/500")), e -> {
							
						}));	
		contents.set(0, 2,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.CAKE), main.color("&b&lTIER 3"),
						"", main.color("&rRainbow Double-Jump Trail"),
						"", main.color("&bCurrent Battle EXP: &70/750")), e -> {
							
						}));	
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}