package anthony.SuperCraftBrawl.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class ShopCWGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ShopCWGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Shop").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(0, 0,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WOOL, 4), "" + ChatColor.RESET + "Wool",
						"", "" + ChatColor.GRAY + "Right click to buy for " + ChatColor.YELLOW + "4 Iron"), e -> {
							int totalAmt = 0;

							for (ItemStack i : player.getInventory().getContents()) {
								if (i != null && i.getType() == Material.IRON_INGOT) {
									totalAmt += i.getAmount();
									if (totalAmt >= 4) {
										takeAway(player);
										return;
									}
								}
							}
							player.sendMessage(main.color("&c&l(!) &rYou do not have enough iron!"));
						}));
	}

	private void takeAway(Player player) {
		int amt = 0;
		for (ItemStack i : player.getInventory().getContents()) {
			if (i != null && i.getType() == Material.IRON_INGOT) {
				for (int x = 0; x < i.getAmount(); x++) {
					amt += 1;
					i.setAmount(i.getAmount() - 1);

					if (i.getAmount() <= 0)
						i.setType(Material.AIR);

					if (amt >= 4) {
						player.sendMessage(main.color("&2&l(!) &rSuccessfully purchased &e4 WOOL"));
						player.getInventory().addItem(new ItemStack(Material.WOOL, 4));
						return;
					}
				}
			}
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
