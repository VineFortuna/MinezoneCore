package anthony.flagwars.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Thrown to grief an enemy base. Behavior (launching a primed TNT entity)
 * is wired up in FWPlayerListener once items are interactive (see matches()).
 */
public final class ThrowableTntItem {

	public static final String NAME = "Throwable TNT";

	private ThrowableTntItem() {
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.TNT), "&c" + NAME,
				"&7Right click to throw lit TNT.",
				"&7It explodes after 3 seconds.");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.TNT) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName()
				&& meta.getDisplayName().contains(NAME);
	}
}
