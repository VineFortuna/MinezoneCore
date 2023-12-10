package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.ChatColorHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ranks.Rank;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class FreeClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	public int totalRows = 3;
	public int totalColumns = 9;

	public FreeClassesGUI(Core main) {
		inv = SmartInventory.builder()
				.id("myInventory")
				.provider(this)
				.size(totalRows, totalColumns)
				.title(ChatColorHelper.color("&8&lFree Classes"))
				.build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int a = 0;
		int b = 0;

		for (ClassType type : ClassType.values()) {
			if (type.getTokenCost() == 0 && type.getMinRank() != Rank.VIP && type.getLevel() == 0) {
				ItemStack item = type.getItem();
				
				if (item == null)
					item = new ItemStack(Material.WOOD);
				contents.set(a, b, ClickableItem.of(ItemHelper.setDetails(new ItemStack(item),
						"" + type.getTag(),
						"" + ChatColor.GRAY + type.getClassDesc()),
						e -> {
							main.getGameManager().playerSelectClass(player, type);
							;
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
									+ "==============================================");
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
									+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
									+ type.getTag());
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
									+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
									+ ChatColor.YELLOW + type.getClassDesc());
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
							player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
									+ "==============================================");
							inv.close(player);
						}));
				b++;

				if (b > 8) {
					if (a == 0) {
						a = 1;
						b = 0;
					} else if (a == 1) {
						a = 2;
						b = 0;
					}
				}
			}
		}

		// Setting "Go Back" Button
		contents.set(totalRows - 1, totalColumns - 1, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER),
				"&7Go back"), e -> {
			new ClassSelectorGUI(main).inv.open(player);
		}));

	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

}
