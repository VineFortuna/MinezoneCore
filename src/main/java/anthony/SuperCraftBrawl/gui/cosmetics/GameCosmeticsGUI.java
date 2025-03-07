package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameCosmeticsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public GameCosmeticsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder()
				.id("myInventory")
				.provider(this)
				.size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Game Cosmetics")
				.parent(parent)
				.build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		// Icon Items
		ItemStack winEffects = ItemHelper.create(Material.BEACON, ChatColor.YELLOW + "Win Effects");
		ItemStack deathEffects = ItemHelper.create(Material.REDSTONE, ChatColor.YELLOW + "Death Effects");

		// Setting Items
		contents.fill(ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

		contents.set(1, 2, ClickableItem.of(winEffects, e -> {
			new WinEffectsGUI(main, inv).inv.open(player);

			/*if (player.hasPermission("scb.winEffects"))
				new WinEffectsGUI(main, inv).inv.open(player);
			else
				player.sendMessage(main.color(
						"&c&l(!) &rYou need the rank " + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN &rto use this!"));*/
		}));

		contents.set(1, 6, ClickableItem.of(deathEffects, e -> {
			new DeathEffectsGUI(main, inv).inv.open(player);
		}));

		contents.set(2, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}
		));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
