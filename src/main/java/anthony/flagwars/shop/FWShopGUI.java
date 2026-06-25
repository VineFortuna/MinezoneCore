package anthony.flagwars.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWTeam;
import anthony.flagwars.resources.FWEconomy;
import anthony.flagwars.resources.FWResourceType;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Flag Wars shop - a 1-1 recreation of Hypixel's own Bedwars Item Shop:
 * same 54-slot size, same category tabs along the bottom row, same items
 * pinned to the same slots within each tab. No team-level gating - every
 * catalog item is purchasable the moment you can afford it (see
 * FWShopItem/FWShopCatalog). The Upgrades tab is FD's own stand-in for
 * Hypixel's separate Team Upgrades NPC (Forge / Sharpened Swords /
 * Reinforced Armor / Heal Pool / Feather Falling), folded into this
 * same GUI since there's no second shopkeeper entity to talk to.
 * One instance is opened per shopping session so it can track which team is
 * buying and which tab they currently have open.
 */
public class FWShopGUI implements InventoryProvider {

	private enum Category {
		QUICK_BUY("Quick Buy", Material.NETHER_STAR),
		BLOCKS("Blocks", Material.WOOL),
		MELEE("Melee Weapons", Material.STONE_SWORD),
		ARMOR("Armor", Material.CHAINMAIL_BOOTS),
		TOOLS("Tools", Material.STONE_PICKAXE),
		BOWS_AND_ARROWS("Bows & Arrows", Material.BOW),
		POTIONS("Potions", Material.BREWING_STAND_ITEM),
		UTILITY("Utility", Material.TNT),
		UPGRADES("Upgrades", Material.ANVIL);

		final String label;
		final Material icon;

		Category(String label, Material icon) {
			this.label = label;
			this.icon = icon;
		}
	}

	private final Core main;
	private final FWGameInstance instance;
	private final FWTeam team;
	private final SmartInventory inv;

	private Category category = Category.QUICK_BUY;

	/**
	 * SmartInvs calls update() on every open inventory every single tick (see InventoryManager's
	 * InvTask) - blindly calling render() on all of them, every tick, for every player with the
	 * shop open meant constantly redoing all ~50 Inventory#setItem calls 20x/second even when
	 * nothing changed, which is exactly what was making every click feel laggy (it was competing
	 * with itself). Real state changes (tab switch, purchase, upgrade) now render() immediately
	 * from their own click handlers instead of waiting on this; this counter only exists so the
	 * "Not enough resources" -> "Click to purchase" lore still refreshes live as resources trickle
	 * in, just at a much cheaper cadence.
	 */
	private int tickCounter = 0;
	private static final int LIVE_REFRESH_INTERVAL_TICKS = 10;

	public FWShopGUI(FWGameInstance instance, FWTeam team) {
		this.instance = instance;
		this.main = instance.getManager().getMain();
		this.team = team;
		this.inv = SmartInventory.builder()
				.id("fdShop")
				.provider(this)
				.size(6, 9)
				.title(main.color("&8&lFlag Wars Shop"))
				.build();
	}

	public void open(Player player) {
		inv.open(player);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		render(player, contents);
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		tickCounter++;
		if (tickCounter % LIVE_REFRESH_INTERVAL_TICKS != 0) return;
		render(player, contents);
	}

	private void render(Player player, InventoryContents contents) {
		ItemStack filler = ItemHelper.getGlassFiller();

		for (int row = 0; row < 5; row++) {
			for (int col = 0; col < 9; col++) {
				contents.set(row, col, ClickableItem.empty(filler));
			}
		}

		if (category == Category.UPGRADES) {
			renderUpgrades(player, contents);
		} else if (category == Category.QUICK_BUY) {
			renderQuickBuy(player, contents);
		} else {
			renderCatalogSorted(player, contents, catalogFor(category));
		}

		renderCategoryButtons(player, contents, filler);
	}

	/** Material (cost type) >> cost amount >> alphabetical, cheapest/lowest-tier items first - same convention the Upgrades tab sorts by. */
	private static final Comparator<FWShopItem> SHOP_ITEM_ORDER = Comparator
			.comparingInt((FWShopItem item) -> item.getCostType().ordinal())
			.thenComparingInt(FWShopItem::getCost)
			.thenComparing(FWShopItem::getName);

	private static final int CATALOG_ROW = 2;
	private static final int CATALOG_FIRST_COL = 1;
	private static final int CATALOG_COLS_PER_ROW = 7;

	/**
	 * Each catalog method's own (row, col) on FWShopItem is ignored here in favor of a fresh
	 * sorted layout - cheapest/lowest-tier material first, wrapping to the next row after 7 items
	 * (cols 1-7, matching Hypixel's own inset margin) exactly like Utility's 8th item already did
	 * before this existed.
	 */
	private void renderCatalogSorted(Player player, InventoryContents contents, List<FWShopItem> items) {
		List<FWShopItem> sorted = new ArrayList<>(items);
		sorted.sort(SHOP_ITEM_ORDER);

		int index = 0;
		for (FWShopItem shopItem : sorted) {
			int row = CATALOG_ROW + index / CATALOG_COLS_PER_ROW;
			int col = CATALOG_FIRST_COL + index % CATALOG_COLS_PER_ROW;
			contents.set(row, col, shopClickable(player, shopItem, contents));
			index++;
		}
	}

	/** Rows tried first for a new addition, closest to the default row (2) outward, then that row's own leftover corners last. */
	private static final int[] QUICK_BUY_PREFERRED_ROWS = {1, 3, 0, 4, 2};
	/** Columns tried first - directly above/below an existing default item before the far corners. */
	private static final int[] QUICK_BUY_PREFERRED_COLS = {1, 2, 3, 4, 5, 6, 7, 0, 8};

	/**
	 * Default Quick Buy items keep their pinned Hypixel-style slots; anything the player
	 * shift-clicked onto their Quick Buy elsewhere gets packed into the nearest free slot above
	 * or below that row instead of starting from the top-left corner, so additions stay visually
	 * next to the items already there.
	 */
	private void renderQuickBuy(Player player, InventoryContents contents) {
		Set<Integer> usedSlots = new HashSet<>();
		Set<String> shown = new HashSet<>();

		for (FWShopItem shopItem : FWShopCatalog.quickBuy(team)) {
			contents.set(shopItem.getRow(), shopItem.getCol(), shopClickable(player, shopItem, contents));
			usedSlots.add(shopItem.getRow() * 9 + shopItem.getCol());
			shown.add(shopItem.getName());
		}

		List<Integer> freeSlotsByPriority = new ArrayList<>();
		for (int row : QUICK_BUY_PREFERRED_ROWS) {
			for (int col : QUICK_BUY_PREFERRED_COLS) {
				int slot = row * 9 + col;
				if (!usedSlots.contains(slot)) freeSlotsByPriority.add(slot);
			}
		}

		List<FWShopItem> allItems = FWShopCatalog.allItems(team);
		int nextIndex = 0;
		for (String addedName : FWQuickBuyManager.getAdded(main, player)) {
			if (shown.contains(addedName)) continue;

			FWShopItem match = findByName(allItems, addedName);
			if (match == null) continue;
			if (nextIndex >= freeSlotsByPriority.size()) break;

			int slot = freeSlotsByPriority.get(nextIndex++);
			contents.set(slot / 9, slot % 9, shopClickable(player, match, contents));
			shown.add(addedName);
		}
	}

	private FWShopItem findByName(List<FWShopItem> items, String name) {
		for (FWShopItem item : items) {
			if (item.getName().equals(name)) return item;
		}
		return null;
	}

	/** Regular click purchases; shift-click toggles the item in/out of the player's personal Quick Buy. */
	private ClickableItem shopClickable(Player player, FWShopItem shopItem, InventoryContents contents) {
		ItemStack display = shopItem.toDisplayItem(player, main, quickBuyHint(player, shopItem));
		return ClickableItem.of(display, e -> {
			if (e.isShiftClick()) {
				toggleQuickBuy(player, shopItem);
			} else {
				purchase(player, shopItem);
			}
			render(player, contents);
		});
	}

	private FWShopItem.QuickBuyHint quickBuyHint(Player player, FWShopItem shopItem) {
		if (isDefaultQuickBuy(shopItem.getName())) return FWShopItem.QuickBuyHint.DEFAULT;
		return FWQuickBuyManager.isAdded(main, player, shopItem.getName())
				? FWShopItem.QuickBuyHint.REMOVE : FWShopItem.QuickBuyHint.ADD;
	}

	private boolean isDefaultQuickBuy(String name) {
		for (FWShopItem item : FWShopCatalog.quickBuy(team)) {
			if (item.getName().equals(name)) return true;
		}
		return false;
	}

	private void toggleQuickBuy(Player player, FWShopItem shopItem) {
		if (isDefaultQuickBuy(shopItem.getName())) {
			player.sendMessage(main.color("&e&l(!) &r" + shopItem.getName() + " is already in your Quick Buy by default."));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		boolean nowAdded = FWQuickBuyManager.toggle(main, player, shopItem.getName());
		player.sendMessage(main.color(nowAdded
				? "&2&l(!) &rAdded &e" + shopItem.getName() + "&r to your Quick Buy."
				: "&c&l(!) &rRemoved &e" + shopItem.getName() + "&r from your Quick Buy."));
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, nowAdded ? 1.4f : 0.8f);
	}

	private List<FWShopItem> catalogFor(Category category) {
		switch (category) {
			case BLOCKS: return FWShopCatalog.blocks(team);
			case MELEE: return FWShopCatalog.melee();
			case ARMOR: return FWShopCatalog.armor();
			case TOOLS: return FWShopCatalog.tools();
			case BOWS_AND_ARROWS: return FWShopCatalog.bowsAndArrows();
			case POTIONS: return FWShopCatalog.potions();
			case UTILITY: return FWShopCatalog.utility();
			default: return FWShopCatalog.quickBuy(team);
		}
	}

	private void purchase(Player player, FWShopItem shopItem) {
		if (shopItem.isArmorSet() && !isArmorUpgrade(player, shopItem)) {
			player.sendMessage(main.color("&c&l(!) &rYou already own this tier of armor or better!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		if (!FWEconomy.withdraw(player, shopItem.getCostType(), shopItem.getCost())) {
			player.sendMessage(main.color("&c&l(!) &rYou don't have enough " + shopItem.getCostType().name() + "!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		for (ItemStack piece : shopItem.getGiveItems()) {
			ItemStack upgraded = instance.getTeamUpgradeManager().applyUpgrades(team, piece);
			if (shopItem.isArmorSet()) {
				equip(player, upgraded);
			} else if (piece.getType().name().endsWith("_SWORD")) {
				giveSword(player, upgraded);
			} else {
				player.getInventory().addItem(upgraded);
			}
		}

		player.sendMessage(main.color("&2&l(!) &rPurchased &e" + shopItem.getName() + "&r."));
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		playShopkeeperTradeEffect();
	}

	/**
	 * Same trade feedback VillagerAbilityGUI uses in SCB: green Happy Villager particles
	 * scattered around a 1-block-radius circle at head height, plus the villager haggle
	 * sound - centered on the shopkeeper here instead of the buyer.
	 */
	private void playShopkeeperTradeEffect() {
		Villager shopkeeper = instance.getFlagManager().getShopkeeper(team.getSiteId());
		if (shopkeeper == null || shopkeeper.isDead()) return;

		// getEyeLocation() (not getLocation(), which is the villager's feet) so the ring circles its head.
		Location center = shopkeeper.getEyeLocation();
		shopkeeper.getWorld().playSound(center, Sound.VILLAGER_HAGGLE, 1f, 1f);

		for (int i = 0; i < 100; i++) {
			double angle = Math.random() * Math.PI * 2;
			double x = Math.cos(angle);
			double z = Math.sin(angle);
			shopkeeper.getWorld().playEffect(center.clone().add(x, 0, z), Effect.HAPPY_VILLAGER, 0);
		}
	}

	/** Leather is the team-colored starter armor every player begins with - everything above it is a real shop tier. */
	private static final List<Material> ARMOR_TIER_ORDER = Arrays.asList(
			Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS);

	/** Blocks re-buying the same tier (wasted resources) or a lower one (an accidental downgrade) - Hypixel's shop does the same. */
	private boolean isArmorUpgrade(Player player, FWShopItem armorSetItem) {
		ItemStack currentLeggings = player.getInventory().getLeggings();
		int currentTier = currentLeggings != null ? ARMOR_TIER_ORDER.indexOf(currentLeggings.getType()) : -1;

		int newTier = -1;
		for (ItemStack piece : armorSetItem.getGiveItems()) {
			if (piece.getType().name().endsWith("_LEGGINGS")) {
				newTier = ARMOR_TIER_ORDER.indexOf(piece.getType());
				break;
			}
		}

		return newTier > currentTier;
	}

	/** Equips one armor piece into its matching slot, instantly upgrading the player like Hypixel's full-set purchases do. */
	private void equip(Player player, ItemStack piece) {
		String type = piece.getType().name();
		if (type.endsWith("_HELMET")) player.getInventory().setHelmet(piece);
		else if (type.endsWith("_CHESTPLATE")) player.getInventory().setChestplate(piece);
		else if (type.endsWith("_LEGGINGS")) player.getInventory().setLeggings(piece);
		else if (type.endsWith("_BOOTS")) player.getInventory().setBoots(piece);
	}

	/**
	 * Buying a sword tier swaps out whatever sword the player's currently carrying - the starter
	 * Wood Sword, or an earlier purchase - in its own slot, instead of just adding the new one
	 * alongside it and leaving the old one cluttering the inventory.
	 */
	private void giveSword(Player player, ItemStack newSword) {
		ItemStack[] contents = player.getInventory().getContents();
		boolean replaced = false;

		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && item.getType().name().endsWith("_SWORD")) {
				player.getInventory().setItem(i, replaced ? null : newSword);
				replaced = true;
			}
		}

		if (!replaced) {
			player.getInventory().addItem(newSword);
		}
	}

	private void renderUpgrades(Player player, InventoryContents contents) {
		int col = 1;
		for (FWTeamUpgrade upgrade : sortedUpgrades()) {
			contents.set(2, col++, ClickableItem.of(upgradeDisplayItem(player, upgrade), e -> {
				purchaseUpgrade(player, upgrade);
				render(player, contents);
			}));
		}
	}

	/**
	 * FWTeamUpgrade.values() is declaration order, not a meaningful shop order - sort dynamically
	 * instead by material (the priciest resource in the next tier's cost) >> cost (that resource's
	 * amount) >> name, cheapest/lowest-tier upgrades first, same convention the regular catalog
	 * tabs already follow (iron-cost items before gold before emerald/diamond).
	 */
	private List<FWTeamUpgrade> sortedUpgrades() {
		List<FWTeamUpgrade> upgrades = new ArrayList<>(Arrays.asList(FWTeamUpgrade.values()));
		upgrades.sort((a, b) -> {
			Map<FWResourceType, Integer> costA = nextCostOf(a);
			Map<FWResourceType, Integer> costB = nextCostOf(b);

			int materialCompare = Integer.compare(primaryMaterialOrdinal(costA), primaryMaterialOrdinal(costB));
			if (materialCompare != 0) return materialCompare;

			int costCompare = Integer.compare(primaryAmount(costA), primaryAmount(costB));
			if (costCompare != 0) return costCompare;

			return a.getDisplayName().compareTo(b.getDisplayName());
		});
		return upgrades;
	}

	/** Empty (sorts last via ordinal -1) once an upgrade is maxed - there's no "next" cost to rank it by. */
	private Map<FWResourceType, Integer> nextCostOf(FWTeamUpgrade upgrade) {
		int tier = team.getUpgradeTier(upgrade);
		Map<FWResourceType, Integer> cost = upgrade.costForTier(tier + 1);
		return cost != null ? cost : Collections.emptyMap();
	}

	/** The highest-tier resource type in a cost map - e.g. {IRON, GOLD} ranks as GOLD. */
	private int primaryMaterialOrdinal(Map<FWResourceType, Integer> cost) {
		int max = -1;
		for (FWResourceType type : cost.keySet()) max = Math.max(max, type.ordinal());
		return max;
	}

	/** The amount of whichever resource determined primaryMaterialOrdinal. */
	private int primaryAmount(Map<FWResourceType, Integer> cost) {
		int ordinal = primaryMaterialOrdinal(cost);
		for (Map.Entry<FWResourceType, Integer> entry : cost.entrySet()) {
			if (entry.getKey().ordinal() == ordinal) return entry.getValue();
		}
		return 0;
	}

	private ItemStack upgradeDisplayItem(Player player, FWTeamUpgrade upgrade) {
		int tier = team.getUpgradeTier(upgrade);
		boolean maxed = tier >= upgrade.getMaxTier();
		Map<FWResourceType, Integer> cost = maxed ? null : upgrade.costForTier(tier + 1);

		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color("&7Tier: &e" + tier + "&7/&e" + upgrade.getMaxTier()));

		if (cost != null) {
			boolean affordable = FWEconomy.canAfford(player, cost);
			lore.add(main.color(FWResourceType.formatCost(cost)));
			lore.add(main.color(affordable ? "&a&lClick to upgrade" : "&c&lNot enough resources"));
		} else {
			lore.add(main.color("&a&lMax tier reached"));
		}

		return ItemHelper.setDetails(new ItemStack(iconFor(upgrade)), main.color((maxed ? "&a" : "&e") + upgrade.getDisplayName()), lore);
	}

	private Material iconFor(FWTeamUpgrade upgrade) {
		switch (upgrade) {
			case FORGE: return Material.FURNACE;
			case SHARPENED_SWORDS: return Material.IRON_SWORD;
			case REINFORCED_ARMOR: return Material.IRON_LEGGINGS;
			case HEAL_POOL: return Material.SPECKLED_MELON;
			case FEATHER_FALLING: return Material.FEATHER;
			default: return Material.ANVIL;
		}
	}

	private void purchaseUpgrade(Player player, FWTeamUpgrade upgrade) {
		if (team.getUpgradeTier(upgrade) >= upgrade.getMaxTier()) {
			player.sendMessage(main.color("&c&l(!) &r" + upgrade.getDisplayName() + " is already at max tier!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		if (!instance.getTeamUpgradeManager().purchase(team, upgrade, player)) {
			player.sendMessage(main.color("&c&l(!) &rYou can't afford that upgrade yet!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		playShopkeeperTradeEffect();
	}

	private void renderCategoryButtons(Player player, InventoryContents contents, ItemStack filler) {
		Category[] categories = Category.values();

		for (int i = 0; i < categories.length; i++) {
			Category cat = categories[i];
			boolean selected = cat == category;

			List<String> lore = new ArrayList<>();
			lore.add(main.color(selected ? "&7Currently viewing" : "&7Click to view"));

			ItemStack icon = ItemHelper.setDetails(new ItemStack(cat.icon),
					main.color((selected ? "&a&l" : "&e&l") + cat.label), lore);
			if (selected) icon = ItemHelper.setGlowing(icon, true);

			contents.set(5, i, ClickableItem.of(icon, e -> {
				category = cat;
				render(player, contents);
			}));
		}
	}
}
