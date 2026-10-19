package anthony.flagwars.items;

import anthony.util.ItemHelper;
import org.bukkit.ChatColor;
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
	 * Creates a grappling hook with the given remaining uses. It's always a single item: the uses are a counter
	 * kept in its lore (buying another adds to it), and the durability bar shows how many are left out of all the
	 * uses it's had.
	 */
	public static ItemStack create(int usesLeft) {
		ItemStack rod = new ItemStack(Material.FISHING_ROD);
		setUses(rod, usesLeft, Math.max(usesLeft, MAX_USES));
		return rod;
	}

	/** Uses one up (in place): the total it's had stays the same, so the durability bar drains. */
	public static void setUses(ItemStack rod, int usesLeft) {
		setUses(rod, usesLeft, Math.max(getTotalUses(rod), usesLeft));
	}

	/** Another hook was bought: its uses are added to both what's left and the total, so the bar goes up by them. */
	public static void addUses(ItemStack rod, int uses) {
		setUses(rod, getUses(rod) + uses, getTotalUses(rod) + uses);
	}

	/** Rewrites this hook (in place): "<left>/<total> uses remaining." and a durability bar of left out of total. */
	private static void setUses(ItemStack rod, int usesLeft, int totalUses) {
		String useLine = "&c&l" + usesLeft + "/" + totalUses + " use" + (totalUses == 1 ? "" : "s") + " remaining.";
		ItemStack details = ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD), "&a&l" + NAME,
				"&7Right-click to cast it like a",
				"&7fishing rod - it'll yank you toward",
				"&7whatever block it lands on.",
				"", useLine);
		rod.setItemMeta(details.getItemMeta());
		// Full at all uses left; never reaches MAX_DUR, which would break it
		rod.setDurability((short) (MAX_DUR * (totalUses - usesLeft) / Math.max(totalUses, 1)));
	}

	/** The uses left on this hook, read from its lore. */
	public static int getUses(ItemStack rod) {
		int[] counts = readCounts(rod);
		return counts[0];
	}

	/** All the uses this hook has had (bought), read from its lore. */
	public static int getTotalUses(ItemStack rod) {
		int[] counts = readCounts(rod);
		return counts[1];
	}

	/** {left, total} from the lore's last line ("4/6 uses remaining."); an older "4 uses remaining." has a total of 3. */
	private static int[] readCounts(ItemStack rod) {
		ItemMeta meta = rod.getItemMeta();
		if (meta == null || !meta.hasLore() || meta.getLore().isEmpty()) return new int[]{0, MAX_USES};
		String line = ChatColor.stripColor(meta.getLore().get(meta.getLore().size() - 1)).trim();
		int space = line.indexOf(' ');
		String counts = space > 0 ? line.substring(0, space) : line;
		try {
			int slash = counts.indexOf('/');
			if (slash < 0) {
				int left = Integer.parseInt(counts);
				return new int[]{left, Math.max(left, MAX_USES)};
			}
			return new int[]{Integer.parseInt(counts.substring(0, slash)), Integer.parseInt(counts.substring(slash + 1))};
		} catch (NumberFormatException e) {
			return new int[]{0, MAX_USES};
		}
	}

	public static boolean matches(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.FISHING_ROD) return false;
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasDisplayName()
				&& meta.getDisplayName().contains(NAME);
	}
}
