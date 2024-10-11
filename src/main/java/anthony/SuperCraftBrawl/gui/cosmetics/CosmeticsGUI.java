package anthony.SuperCraftBrawl.gui.cosmetics;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.RankManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class CosmeticsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private RankManager rm;

	// Creating Cosmetics Inventory
	public CosmeticsGUI(Core main) {
		inv = SmartInventory.builder()
				.id("myInventory")
				.provider(this)
				.size(3, 9)
				.title(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "Cosmetics")
				.build();
		this.main = main;
	}

	public RankManager getRankManager() {
		return rm;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init(Player player, InventoryContents contents) {
		// Icon Items
			// Lobby Cosmetics
		ItemStack lobby = ItemHelper.create(Material.BOOKSHELF, ChatColor.YELLOW + "Lobby Cosmetics");

			// Game Cosmetics
		ItemStack ingame = ItemHelper.create(Material.ENDER_PORTAL_FRAME, ChatColor.YELLOW + "Game Cosmetics");

		// Setting Icons
		contents.fill(ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

		contents.set(1, 2, ClickableItem.of(
				lobby,
				e -> {
					inv.close(player);
					new LobbyCosmeticsGUI(main, inv).inv.open(player);
				}));

		contents.set(1, 6, ClickableItem.of(
				ingame,
				e -> {
					inv.close(player);
					new GameCosmeticsGUI(main, inv).inv.open(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}
}
