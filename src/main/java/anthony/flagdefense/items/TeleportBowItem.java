package anthony.flagdefense.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Shooting a player with this bow teleports the shooter to them, per the
 * user's preferred variant (no ground-teleport option). Behavior is wired
 * up in FDPlayerListener once items are interactive (see matches()).
 */
public final class TeleportBowItem {

	public static final String NAME = "Teleport Bow";

	private TeleportBowItem() {
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.BOW), "&d" + NAME,
				"&7Shoot a player to teleport",
				"&7yourself to them.");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.BOW) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName()
				&& meta.getDisplayName().contains(NAME);
	}
}
