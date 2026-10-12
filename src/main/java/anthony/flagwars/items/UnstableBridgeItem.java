package anthony.flagwars.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Unstable Bridge: right-click to gradually build a 3-wide, 10-long bridge of
 * Sand/Red Sand/Gravel straight out in the direction you're facing, at your
 * feet's height. 5 seconds after it finishes building, it starts collapsing
 * row by row into real falling blocks - a cheap, fast, but temporary crossing.
 *
 * Build/collapse logic lives in FWPlayerListener (see launchUnstableBridge).
 */
public final class UnstableBridgeItem {

	public static final String NAME = "Unstable Bridge";

	private UnstableBridgeItem() {
	}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.SAND), "&e" + NAME,
				"&7Right-click to build a bridge of",
				"&7Sand, Red Sand and Gravel",
				"",
				"&rCollapses 5s after it finishes building",
				"&rSize: &e3x10 blocks");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.SAND) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(NAME);
	}
}
