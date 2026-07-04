package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticManager;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GadgetsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public GadgetsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(4, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Gadgets").parent(parent).build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.fillBorders(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		CosmeticManager manager = main.getCosmeticManager();
		set(contents, player, manager, 1, 1, CosmeticCategory.GADGET, "magic_broom");
		set(contents, player, manager, 1, 2, CosmeticCategory.GADGET, "paintball_gun");
		set(contents, player, manager, 1, 3, CosmeticCategory.GADGET, "melon");
		set(contents, player, manager, 1, 4, CosmeticCategory.GADGET, "fishing_rod");
		set(contents, player, manager, 1, 5, CosmeticCategory.TRAIL, "snow_particles");
		set(contents, player, manager, 1, 6, CosmeticCategory.PET, "snowman");
		set(contents, player, manager, 1, 7, CosmeticCategory.TRAIL, "candy_cane_swirl");
		set(contents, player, manager, 2, 1, CosmeticCategory.TRAIL, "candy_aura");

		contents.set(3, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}

	private void set(InventoryContents contents, Player player, CosmeticManager manager, int row, int col,
	                  CosmeticCategory category, String id) {
		Cosmetic cosmetic = manager.registry().get(category, id);
		contents.set(row, col, CosmeticGuiUtil.toggleItem(main, manager, cosmetic, player, cosmetic.getIcon(player), inv));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
