package anthony.flagwars.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Single-use freeze ray: right-click to shoot an ice-particle beam that deals
 * 4.5 damage and applies Slowness II for 4 seconds to any enemy in the path.
 * Behavior is wired up in FWPlayerListener (see shootFreezeRay).
 */
public final class FreezeRayItem {

	public static final String NAME = "Freeze Ray";

	private FreezeRayItem() {}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.ICE), "&b" + NAME,
				"&7Right-click to shoot a freeze ray",
				"&7through enemies in its path.",
				"",
				"&rEffect: &aDamage &r& &aSlowness II",
				"&rRange: &a10 blocks");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.ICE) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(NAME);
	}
}
