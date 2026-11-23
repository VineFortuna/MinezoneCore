package anthony.flagwars.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.resources.FWEconomy;
import anthony.flagwars.resources.FWResourceType;
import anthony.util.ItemHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * One purchasable shop entry. Where it shows up in a tab is decided by FWShopGUI (items are laid
 * out in sorted order, see compareTo), not by the item itself. Cost is checked against the buyer's
 * actual inventory, like Hypixel Bedwars - there's no hidden per-team currency. Armor entries equip
 * their pieces directly (see FWShopGUI#purchase) instead of dropping them loose into your
 * inventory, exactly like buying "Diamond Armor" on Hypixel instantly suits you up.
 */
public class FWShopItem implements Comparable<FWShopItem> {

	private final String name;
	private final ItemStack[] giveItems;
	private final boolean armorSet;
	private final FWResourceType costType;
	private final int cost;
	private int displayAmount; // 0 = use item's actual amount
	/** Survives death (see FWGameInstance#giveStarterKit). Shown as "Permanent" in the shop; the stored name stays the plain one. */
	private boolean permanent;

	private String description = "";
	/** Extra lines between Cost and the click lines, e.g. an item's range or size. */
	private final List<String> stats = new ArrayList<>();

	public FWShopItem(String name, ItemStack giveItem, FWResourceType costType, int cost) {
		this(name, new ItemStack[]{giveItem}, false, costType, cost);
	}

	/**
	 * Hypixel Bedwars armor is leggings + boots only - there's no helmet/chestplate upgrade in the
	 * shop, the team-colored leather on those slots (see FWGameInstance#giveStarterArmorIfUnequipped)
	 * just stays put forever. Varargs so this also covers single-piece equip-on-purchase entries
	 * like Feather Falling Boots, not just the two-piece tiers. Worn armor is kept through death, so
	 * armor sets are always permanent.
	 */
	public static FWShopItem armorSet(String name, FWResourceType costType, int cost, ItemStack... pieces) {
		FWShopItem item = new FWShopItem(name, pieces, true, costType, cost);
		item.permanent = true;
		return item;
	}

	private FWShopItem(String name, ItemStack[] giveItems, boolean armorSet,
			FWResourceType costType, int cost) {
		this.name = name;
		this.giveItems = giveItems;
		this.armorSet = armorSet;
		this.costType = costType;
		this.cost = cost;
	}

	/** The plain name. This is the key Quick Buy stores, so it must never change. */
	public String getName() {
		return name;
	}

	/** What the shop shows: the plain name, with "Permanent" in front for items kept through death. */
	public String getDisplayName() {
		return permanent ? "Permanent " + name : name;
	}

	/** The last give item (boots, for the leggings+boots armor sets) - otherwise it's the only one anyway. */
	public ItemStack getDisplayIcon() {
		return giveItems[giveItems.length - 1].clone();
	}

	public List<ItemStack> getGiveItems() {
		List<ItemStack> clones = new ArrayList<>();
		for (ItemStack item : giveItems) clones.add(item.clone());
		return clones;
	}

	public boolean isArmorSet() {
		return armorSet;
	}

	public boolean isPermanent() {
		return permanent;
	}

	/**
	 * Whether buying this more than once in a row makes sense, which is what right-click "buy max"
	 * needs. Armor, swords, pickaxes, axes and permanent items (Shears) only ever keep one, so
	 * buying a second would just waste resources.
	 */
	public boolean isRepeatable() {
		if (permanent || armorSet) return false;
		for (ItemStack piece : giveItems) {
			String type = piece.getType().name();
			if (type.endsWith("_SWORD") || type.endsWith("_PICKAXE") || type.endsWith("_AXE")) return false;
		}
		return true;
	}

	public FWResourceType getCostType() {
		return costType;
	}

	public int getCost() {
		return cost;
	}

	public boolean isAffordableFor(Player player) {
		return FWEconomy.canAfford(player, costType, cost);
	}

	/** The gray lines under the name. Use \n to start a new line. */
	public FWShopItem withDescription(String description) {
		this.description = description;
		return this;
	}

	/** Adds a line under Cost (color codes allowed), e.g. "&rRange: &a10 blocks". */
	public FWShopItem withStat(String stat) {
		this.stats.add(stat);
		return this;
	}

	/** Override the stack-size shown in the shop GUI without changing what the player actually receives. */
	public FWShopItem withDisplayAmount(int n) {
		this.displayAmount = n;
		return this;
	}

	/** Marks an item the player keeps through death (see FWGameInstance#giveStarterKit). */
	public FWShopItem permanent() {
		this.permanent = true;
		return this;
	}

	/** Shop tab order: resource (Iron, Gold, Emerald, Diamond), then cost, then name A to Z. */
	@Override
	public int compareTo(FWShopItem other) {
		int byResource = Integer.compare(costType.ordinal(), other.costType.ordinal());
		if (byResource != 0) return byResource;
		int byCost = Integer.compare(cost, other.cost);
		if (byCost != 0) return byCost;
		return name.compareToIgnoreCase(other.name);
	}

	/** Which Quick Buy lore line/state to show: not added yet, already added by the player, always-on by default, or no room left. */
	public enum QuickBuyHint {
		ADD, REMOVE, DEFAULT, FULL
	}

	/**
	 * Same tile layout as the Game Selector: gold bold name, blank line, gray description, blank line,
	 * then the yellow click lines ("&e&nLeft Click&r&e to ..."). Only the cost keeps its own color
	 * (gray Iron, gold Gold, ...). Whether the buyer can afford it shows in the first click line.
	 */
	public ItemStack toDisplayItem(Player player, Core main, QuickBuyHint quickBuyHint) {
		boolean affordable = isAffordableFor(player);
		String resourceName = costType.getDisplayName();

		List<String> lore = new ArrayList<>();
		lore.add("");
		if (!description.isEmpty()) {
			for (String line : description.split("\n")) lore.add(main.color("&7" + line));
			lore.add("");
		}
		lore.add(main.color("&rCost: " + costType.getColorCode() + cost + " " + resourceName));
		for (String stat : stats) lore.add(main.color(stat));
		lore.add("");
		lore.add(main.color(affordable ? "&e&nLeft Click&r&e to purchase" : "&cNot enough resources!"));
		if (isRepeatable()) lore.add(main.color("&e&nRight Click&r&e to buy max"));
		if (quickBuyHint == QuickBuyHint.REMOVE) {
			lore.add(main.color("&e&nShift Click&r&e to remove from Quick Buy"));
		} else if (quickBuyHint == QuickBuyHint.FULL) {
			lore.add(main.color("&cYour Quick Buy is full"));
		} else {
			lore.add(main.color("&e&nShift Click&r&e to add to Quick Buy"));
		}

		ItemStack icon = getDisplayIcon();
		if (displayAmount > 0) icon.setAmount(displayAmount);
		return ItemHelper.setDetails(icon, main.color("&6&l" + getDisplayName()), lore);
	}
}
