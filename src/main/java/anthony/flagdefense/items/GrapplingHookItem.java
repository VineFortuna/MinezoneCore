package anthony.flagdefense.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Bedwars' "rod" reimagined as a grappling hook per the design doc: pulls
 * the user toward whatever block they're looking at. Behavior is wired up
 * in FDPlayerListener once items are interactive (see matches()).
 */
public final class GrapplingHookItem {

	public static final String NAME = "Grappling Hook";

	private GrapplingHookItem() {
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD), "&a" + NAME,
				"&7Right-click to cast it like a",
				"&7fishing rod - it'll yank you toward",
				"&7whatever block it lands on.");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.FISHING_ROD) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName()
				&& meta.getDisplayName().contains(NAME);
	}
}
