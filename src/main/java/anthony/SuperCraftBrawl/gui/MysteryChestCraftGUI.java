package anthony.SuperCraftBrawl.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Main;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class MysteryChestCraftGUI implements InventoryProvider {

	public Main main;
	public SmartInventory inv;
	public int cost = 100;

	public MysteryChestCraftGUI(Main main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.YELLOW + ChatColor.BOLD + "Mystery Chest").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.ENDER_CHEST),
				main.color("&ePurchase &9&lMystery Chest &efor " + cost + " tokens?")), e -> {
				}));

		PlayerData data = main.getDataManager().getPlayerData(player);
		int a = 0;
		int b = 5;

		for (int i = 0; i < 9; i++) {
			b++;
			if (b > 8) {
				if (a == 0) {
					a = 1;
					b = 6;
				} else if (a == 1) {
					a = 2;
					b = 6;
				}
			}
			contents.set(a, b,
					ClickableItem.of(ItemHelper.setDetails(
							new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
							main.color("&e&lConfirm")), e -> {
								data.tokens -= 100;
								data.mysteryChests++;
								main.LobbyBoard(player);
								player.sendMessage(main.color("&9&l(!) &rSuccessfully purchased &e1 Mystery Chest!"));
								inv.close(player);
							}));
		}

		a = 0;
		b = 0;

		for (int i = 0; i < 9; i++) {
			if (b > 2) {
				if (a == 0) {
					a = 1;
					b = 0;
				} else if (a == 1) {
					a = 2;
					b = 0;
				}
			}
			contents.set(a, b, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), main.color("&c&lCancel")),
					e -> {
						inv.close(player);
					}));
			b++;
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		// TODO Auto-generated method stub

	}

}
