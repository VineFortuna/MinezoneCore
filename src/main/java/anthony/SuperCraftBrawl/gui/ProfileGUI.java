package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.RankManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class ProfileGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private RankManager rm;

	public ProfileGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Profile").build();
		this.main = main;
	}

	public RankManager getRankManager() {
		return rm;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(1, 3, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.REDSTONE_COMPARATOR),
				"" + ChatColor.RESET + ChatColor.YELLOW + "Preferences"), e -> {
					inv.close(player);
					new PrefsGUI(main).inv.open(player);
				}));
		contents.set(1, 5, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK),
				"" + ChatColor.RESET + ChatColor.YELLOW + "My Stats"), e -> {
					inv.close(player);
					new StatsGUI(main).inv.open(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}