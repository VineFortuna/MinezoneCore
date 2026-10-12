package anthony.flagwars.items;

import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Bedwars' "rod" reimagined as a grappling hook per the design doc: pulls
 * the user toward whatever block they're looking at. Behavior is wired up
 * in FWPlayerListener once items are interactive (see matches()).
 */
public final class GrapplingHookItem {

	public static final String NAME = "Grappling Hook";
	public static final int MAX_USES = 3;
	private static final short MAX_DUR = Material.FISHING_ROD.getMaxDurability(); // 64

	private GrapplingHookItem() {
	}

	public static ItemStack create() {
		return create(MAX_USES);
	}

	/**
	 * Creates a grappling hook with the given remaining uses.
	 * The item amount mirrors the use count; the durability bar drains proportionally
	 * (same visual system as the SCB Instagib item).
	 */
	public static ItemStack create(int usesLeft) {
		String useLine = "&c&l" + usesLeft + " use" + (usesLeft == 1 ? "" : "s") + " remaining.";
		ItemStack rod = ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD, usesLeft), "&a&l" + NAME,
				"&7Right-click to cast it like a",
				"&7fishing rod - it'll yank you toward",
				"&7whatever block it lands on.",
				"", useLine);
		rod.setDurability((short) ((MAX_DUR / MAX_USES) * (MAX_USES - usesLeft)));
		return rod;
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.FISHING_ROD) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName()
				&& meta.getDisplayName().contains(NAME);
	}
}
