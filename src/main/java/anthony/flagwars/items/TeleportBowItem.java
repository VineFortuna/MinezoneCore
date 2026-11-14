package anthony.flagwars.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Shooting with this bow teleports the shooter to wherever the arrow lands (a player or a block), once:
 * the bow is used up by the shot. Behavior is wired up in FWPlayerListener (see matches()).
 */
public final class TeleportBowItem {

	public static final String NAME = "Teleport Bow";

	private TeleportBowItem() {
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.BOW), "&d" + NAME,
				"&7Shoot an arrow to teleport to",
				"&7where it lands.",
				"",
				"&cSingle use");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.BOW) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName()
				&& meta.getDisplayName().contains(NAME);
	}
}
