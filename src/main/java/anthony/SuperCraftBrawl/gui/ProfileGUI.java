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
		inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Profile").build();
		this.main = main;
	}

	public RankManager getRankManager() {
		return rm;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.fillRow(4, ClickableItem.of(ItemHelper.setDetails(
				new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
		
		contents.set(4, 3, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.REDSTONE_COMPARATOR),
				"" + ChatColor.RESET + ChatColor.YELLOW + "Preferences"), e -> {
					new PrefsGUI(main).inv.open(player);
				}));
		contents.set(4, 5, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BOOK),
				"" + ChatColor.RESET + ChatColor.YELLOW + "My Stats"), e -> {
					new StatsGUI(main).inv.open(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}