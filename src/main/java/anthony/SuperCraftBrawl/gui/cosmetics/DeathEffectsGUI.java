package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DeathEffectsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public DeathEffectsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Death Effects").parent(parent).build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.fillBorders(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		int col = 1;
		for (Cosmetic effect : main.getCosmeticManager().registry().getAll(CosmeticCategory.DEATH_EFFECT)) {
			contents.set(1, col, CosmeticGuiUtil.toggleItem(main, main.getCosmeticManager(), effect, player,
					effect.getIcon(player), inv));
			col++;
		}

		contents.set(2, 8, ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
					inv.getParent().get().open(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
