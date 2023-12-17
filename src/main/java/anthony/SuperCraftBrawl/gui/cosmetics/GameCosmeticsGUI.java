package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
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

	public GameCosmeticsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Game Cosmetics").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		// Button Items
		ItemStack arrowEffects = ItemHelper.create(Material.ARROW, ChatColor.YELLOW + "Arrow Effects");

		ItemStack killEffects = ItemHelper.create(Material.IRON_SWORD, ChatColor.YELLOW + "Kill Effects");

		ItemStack deathEffects = ItemHelper.create(Material.REDSTONE, ChatColor.YELLOW + "Death Effects");

		ItemStack winEffects = ItemHelper.create(Material.BEACON, ChatColor.YELLOW + "Win Effects");

		ItemStack gameOutfits = ItemHelper.create(Material.GOLD_CHESTPLATE, ChatColor.YELLOW + "Outfits");

		// Setting Items

		contents.set(1, 2, ClickableItem.of(arrowEffects, e -> {
			inv.close(player);
			new ArrowEffectsGUI(main).inv.open(player);
		}));

		contents.set(1, 6, ClickableItem.of(killEffects, e -> {
			inv.close(player);
			new KillEffectsGUI(main).inv.open(player);
		}));

		contents.set(1, 6, ClickableItem.of(deathEffects, e -> {
			inv.close(player);
			new DeathEffectsGUI(main).inv.open(player);
		}));

		contents.set(0, 4, ClickableItem.of(winEffects, e -> {
			inv.close(player);

			if (player.hasPermission("scb.winEffects"))
				new WinEffectsGUI(main).inv.open(player);
			else
				player.sendMessage(main.color(
						"&c&l(!) &rYou need the rank " + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN &rto use this!"));
		}));

		contents.set(3, 5, ClickableItem.of(gameOutfits, e -> {
			inv.close(player);
			new GameOutfitsGUI(main).inv.open(player);
		}));

	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
