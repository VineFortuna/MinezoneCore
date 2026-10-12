package anthony.flagwars.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Bridge Egg: use it and a 3-wide bridge of your team's colored Wool instantly appears straight
 * out in the direction you're looking, up to 20 blocks (or wherever the first solid block is in
 * the way). Real Hypixel Bridge Egg behavior - no thrown egg to wait on, the bridge is just there.
 *
 * Build logic lives in FWPlayerListener (see launchBridgeEgg / buildBridgeInLookDirection).
 */
public final class BridgeEggItem {

	public static final String NAME = "Bridge Egg";

	private BridgeEggItem() {}

	public static ItemStack create() {
		return ItemHelper.setDetails(new ItemStack(Material.EGG), "&b" + NAME,
				"&7Right-click to instantly build a bridge",
				"&7of your team's color, straight ahead of you",
				"",
				"&rBridge: &bUp to 20 blocks, 3 blocks wide");
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.EGG) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(NAME);
	}
}
