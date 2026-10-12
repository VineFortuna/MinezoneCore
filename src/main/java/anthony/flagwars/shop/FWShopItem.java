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
 * One purchasable shop entry, pinned to a fixed (row, col) slot - same spot
 * Hypixel's own Bedwars shop puts it in - rather than auto-packed into a
 * grid. Cost is checked against the buyer's actual inventory, like Hypixel
 * Bedwars - there's no hidden per-team currency. Armor entries equip their
 * pieces directly (see FWShopGUI#purchase) instead of dropping them loose
 * into your inventory, exactly like buying "Diamond Armor" on Hypixel
 * instantly suits you up.
 */
public class FWShopItem {

	private final String name;
	private final int row;
	private final int col;
	private final ItemStack[] giveItems;
	private final boolean armorSet;
	private final FWResourceType costType;
	private final int cost;
	private int displayAmount; // 0 = use item's actual amount

	private String description = "";

	public FWShopItem(String name, int row, int col, ItemStack giveItem, FWResourceType costType, int cost) {
		this(name, row, col, new ItemStack[]{giveItem}, false, costType, cost);
	}

	/**
	 * Hypixel Bedwars armor is leggings + boots only - there's no helmet/chestplate upgrade in the
	 * shop, the team-colored leather on those slots (see FWGameInstance#giveStarterArmorIfUnequipped)
	 * just stays put forever. Varargs so this also covers single-piece equip-on-purchase entries
	 * like Feather Falling Boots, not just the two-piece tiers.
	 */
	public static FWShopItem armorSet(String name, int row, int col, FWResourceType costType, int cost, ItemStack... pieces) {
		return new FWShopItem(name, row, col, pieces, true, costType, cost);
	}

	private FWShopItem(String name, int row, int col, ItemStack[] giveItems, boolean armorSet,
			FWResourceType costType, int cost) {
		this.name = name;
		this.row = row;
		this.col = col;
		this.giveItems = giveItems;
		this.armorSet = armorSet;
		this.costType = costType;
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
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

	public FWResourceType getCostType() {
		return costType;
	}

	public int getCost() {
		return cost;
	}

	public boolean isAffordableFor(Player player) {
		return FWEconomy.canAfford(player, costType, cost);
	}

	public FWShopItem withDescription(String description) {
		this.description = description;
		return this;
	}

	/** Override the stack-size shown in the shop GUI without changing what the player actually receives. */
	public FWShopItem withDisplayAmount(int n) {
		this.displayAmount = n;
		return this;
	}

	/** Which Quick Buy lore line/state to show: not added yet, already added by the player, or always-on by default. */
	public enum QuickBuyHint {
		ADD, REMOVE, DEFAULT
	}

	public ItemStack toDisplayItem(Player player, Core main, QuickBuyHint quickBuyHint) {
		boolean affordable = isAffordableFor(player);
		String resourceName = costType.getDisplayName();

		List<String> lore = new ArrayList<>();
		if (!description.isEmpty()) lore.add(main.color("&7" + description));
		lore.add("");
		lore.add(main.color("&rCost: " + costType.getColorCode() + cost + " " + resourceName));
		lore.add(main.color(affordable ? "&eClick to purchase" : "&cNot enough resources!"));
		lore.add("");
		if (quickBuyHint == QuickBuyHint.REMOVE) {
			lore.add(main.color("&rShift-Click to &cremove&r from Quick Buy"));
		} else {
			lore.add(main.color("&rShift-Click to &aadd&r to Quick Buy"));
		}

		String color = affordable ? "&a" : "&c";
		ItemStack icon = getDisplayIcon();
		if (displayAmount > 0) icon.setAmount(displayAmount);
		return ItemHelper.setDetails(icon, main.color(color + name), lore);
	}
}
