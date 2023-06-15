package anthony.skywars.gui;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import anthony.SuperCraftBrawl.Core;
import anthony.skywars.GameInstance;
import anthony.skywars.kits.Kit;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class KitSelectorGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public KitSelectorGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Kit Selector").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int x = 0, y = 0;
		String msg = "";
		GameInstance instance = main.getSWManager().getInstanceOfPlayer(player); // Gets if player is in a game

		for (Kit kits : Kit.values()) { // Looping through all kits, displaying them & their features
			String[] loreArray = new String[kits.getKitItems().size() + 1];
			loreArray[0] = " "; // Nothing on first line
			ItemStack item = kits.getMenuItem();
			ItemMeta meta = item.getItemMeta();
			for (int i = 0; i < kits.getKitItems().size(); i++) {
				msg = "" + ChatColor.RESET + ChatColor.GRAY + kits.getKitItems().get(i).getAmount() + " "
						+ kits.getKitItems().get(i).getType();

				if (kits.getKitItems().get(i).getItemMeta().hasEnchants()) {
					for (Map.Entry<Enchantment, Integer> entry : kits.getKitItems().get(i).getItemMeta().getEnchants()
							.entrySet()) {
						Enchantment enchantment = entry.getKey();
						int level = entry.getValue();
						String enchantmentName = enchantment.getName();
						msg += " -> " + enchantmentName + " " + level;
					}
				}
				loreArray[i + 1] = msg;
			}
			meta.setDisplayName("" + ChatColor.YELLOW + kits.toString());
			meta.setLore(Arrays.asList(loreArray));
			item.setItemMeta(meta);
			contents.set(x, y, ClickableItem.of(item, e -> {
				inv.close(player);

				if (instance != null) {
					instance.selectedKit.put(player, kits);
					player.sendMessage(main.color("&2&l(!) &rYou have selected &e" + kits.getKitName()));
					instance.boards.get(player).updateLine(5, " " + kits.getKitName());
				}
			}));
			y++;

			if (y > 8) {
				x++;
				y = 0;
			}

			msg = ""; // Reset after each loop
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
