package anthony.flagwars.shop;

import anthony.flagwars.items.GrapplingHookItem;
import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWTeam;
import anthony.flagwars.FWTrapManager;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Flag Wars shop - modelled on Hypixel's own Bedwars Item Shop for its
 * 54-slot size and items, but styled like our other menus: category tabs along
 * the top row with a divider under them (lime under the open tab) like the
 * Cosmetics menu, a gray pane border like the Game Selector, gold bold item
 * names and yellow click lines. Only the cost keeps its own color per resource.
 * Each tab is sorted (resource, then cost, then name) and laid out from the
 * second column, seven items to a row. No team-level gating - every
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
		UPGRADES("Upgrades & Traps", Material.ANVIL);

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
	 * "Not enough resources" -> "Left Click to purchase" lore still refreshes live as resources trickle
	 * in, just at a much cheaper cadence.
	 */
	private int tickCounter = 0;
	private static final int LIVE_REFRESH_INTERVAL_TICKS = 10;

	/** Row 0 holds the category tabs and row 1 the divider under them, same as the Cosmetics menu. */
	private static final int TAB_ROW = 0;
	private static final int DIVIDER_ROW = 1;
	/** Items fill columns 1 to 7 (the second column onward), so columns 0 and 8 are the pane border. */
	private static final int FIRST_ITEM_COL = 1;
	private static final int ITEMS_PER_ROW = 7;
	/** Items stop at row 4, row 5 is the bottom border. */
	private static final int LAST_ITEM_ROW = 4;
	private static final int FOOTER_ROW = 5;
	private static final int CATALOG_START_ROW = 2;
	private static final int QUICK_BUY_START_ROW = 2;
	/** Stained glass pane colors for the divider: white, and lime under the open tab. */
	private static final short PANE_WHITE = 0;
	private static final short PANE_LIME = 5;
	/** Upgrades tab: the traps sit on their own row, with the Trap Info item at the end of it (the 8th slot, index 7). */
	private static final int TRAPS_ROW = 4;
	private static final int TRAP_INFO_COL = 7;

	private static final String MSG_ARMOR_OWNED = "&c&l(!) &rYou already own this armor or better!";
	private static final String MSG_SWORD_OWNED = "&c&l(!) &rYou already own this sword or better!";
	private static final String MSG_INVENTORY_FULL = "&c&l(!) &rYour inventory is full!";
	/** Right-click "buy max" stops here even if the player could somehow keep going: a full inventory is 36 stacks of 64. */
	private static final int MAX_BUY_LIMIT = 36 * 64;

	/** Which items are in the viewer's Quick Buy right now, and whether it has no room left. Built once per render. */
	private static final class QuickBuyState {
		final Set<String> names;
		final boolean full;

		QuickBuyState(Set<String> names, boolean full) {
			this.names = names;
			this.full = full;
		}
	}

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
		// Gray pane border like the Game Selector: the side columns of the item area and the bottom row.
		// The inside is cleared (not filled) so a tab with fewer items doesn't keep the last tab's items.
		ClickableItem border = ClickableItem.empty(ItemHelper.getGlassFiller());
		for (int row = CATALOG_START_ROW; row <= LAST_ITEM_ROW; row++) {
			for (int col = 0; col < 9; col++) {
				boolean side = col < FIRST_ITEM_COL || col >= FIRST_ITEM_COL + ITEMS_PER_ROW;
				contents.set(row, col, side ? border : null);
			}
		}
		for (int col = 0; col < 9; col++) {
			contents.set(FOOTER_ROW, col, border);
		}

		if (category == Category.UPGRADES) {
			renderUpgrades(player, contents);
		} else if (category == Category.QUICK_BUY) {
			renderQuickBuy(player, contents);
		} else {
			renderCatalog(player, contents, catalogFor(category, player));
		}

		renderCategoryButtons(player, contents);
	}

	/** Every tab but Quick Buy and Upgrades: sorted by resource, then cost, then name, and laid out from the second column. */
	private void renderCatalog(Player player, InventoryContents contents, List<FWShopItem> items) {
		List<FWShopItem> sorted = new ArrayList<>(items);
		Collections.sort(sorted);
		placeInGrid(player, contents, sorted, CATALOG_START_ROW, quickBuyState(player));
	}

	/** Quick Buy shows the items in the order the player added them, starting on the first item row. */
	private void renderQuickBuy(Player player, InventoryContents contents) {
		List<FWShopItem> items = quickBuyItems(player);
		QuickBuyState state = stateOf(items);
		placeInGrid(player, contents, items.subList(0, Math.min(items.size(), FWQuickBuyManager.LIMIT)), QUICK_BUY_START_ROW, state);
	}

	private void placeInGrid(Player player, InventoryContents contents, List<FWShopItem> items, int startRow, QuickBuyState state) {
		for (int i = 0; i < items.size(); i++) {
			int row = gridRow(startRow, i);
			if (row > LAST_ITEM_ROW) break;
			contents.set(row, gridCol(i), shopClickable(player, items.get(i), contents, state));
		}
	}

	private static int gridRow(int startRow, int index) {
		return startRow + index / ITEMS_PER_ROW;
	}

	private static int gridCol(int index) {
		return FIRST_ITEM_COL + index % ITEMS_PER_ROW;
	}

	/**
	 * The player's Quick Buy items in the order they added them. Names that aren't in the shop right
	 * now (a pickaxe tier they already bought, say) are skipped so they can't take up room.
	 */
	private List<FWShopItem> quickBuyItems(Player player) {
		List<String> names = FWQuickBuyManager.getOrderedNames(main, player, FWShopCatalog.defaultQuickBuyNames());
		List<FWShopItem> all = FWShopCatalog.allItems(team, player);

		List<FWShopItem> items = new ArrayList<>();
		for (String name : names) {
			FWShopItem match = findByName(all, name);
			if (match != null) items.add(match);
		}
		return items;
	}

	private QuickBuyState stateOf(List<FWShopItem> items) {
		Set<String> names = new HashSet<>();
		for (FWShopItem item : items) names.add(item.getName());
		return new QuickBuyState(names, items.size() >= FWQuickBuyManager.LIMIT);
	}

	/**
	 * Cheap version of stateOf for the other tabs: the catalog is only searched once the stored list
	 * is long enough to possibly be full, so a normal render doesn't build every shop item each time.
	 */
	private QuickBuyState quickBuyState(Player player) {
		List<String> names = FWQuickBuyManager.getOrderedNames(main, player, FWShopCatalog.defaultQuickBuyNames());
		boolean full = names.size() >= FWQuickBuyManager.LIMIT && quickBuyItems(player).size() >= FWQuickBuyManager.LIMIT;
		return new QuickBuyState(new HashSet<>(names), full);
	}

	private FWShopItem findByName(List<FWShopItem> items, String name) {
		for (FWShopItem item : items) {
			if (item.getName().equals(name)) return item;
		}
		return null;
	}

	/** Click purchases, right-click buys as many as the player can afford, shift-click toggles the item in/out of their Quick Buy. */
	private ClickableItem shopClickable(Player player, FWShopItem shopItem, InventoryContents contents, QuickBuyState state) {
		ItemStack display = shopItem.toDisplayItem(player, main, quickBuyHint(shopItem, state));
		return ClickableItem.of(display, e -> {
			if (e.isShiftClick()) {
				toggleQuickBuy(player, shopItem);
			} else if (e.isRightClick()) {
				purchaseMax(player, shopItem);
			} else {
				purchase(player, shopItem);
			}
			render(player, contents);
		});
	}

	private FWShopItem.QuickBuyHint quickBuyHint(FWShopItem shopItem, QuickBuyState state) {
		if (state.names.contains(shopItem.getName())) return FWShopItem.QuickBuyHint.REMOVE;
		return state.full ? FWShopItem.QuickBuyHint.FULL : FWShopItem.QuickBuyHint.ADD;
	}

	private void toggleQuickBuy(Player player, FWShopItem shopItem) {
		String name = shopItem.getName();
		boolean isDefault = FWShopCatalog.defaultQuickBuyNames().contains(name);
		int count = quickBuyItems(player).size();
		boolean inQuickBuy = quickBuyState(player).names.contains(name);

		if (inQuickBuy) {
			FWQuickBuyManager.remove(main, player, name, isDefault);
			player.sendMessage(main.color("&c&l(!) &rRemoved &e" + name + "&r from your Quick Buy &7("
					+ (count - 1) + "/" + FWQuickBuyManager.LIMIT + ")"));
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 0.8f);
		} else if (count >= FWQuickBuyManager.LIMIT) {
			player.sendMessage(main.color("&c&l(!) &rYour Quick Buy is full &7("
					+ FWQuickBuyManager.LIMIT + "/" + FWQuickBuyManager.LIMIT + ")"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
		} else {
			FWQuickBuyManager.add(main, player, name, isDefault);
			player.sendMessage(main.color("&2&l(!) &rAdded &e" + name + "&r to your Quick Buy &7("
					+ (count + 1) + "/" + FWQuickBuyManager.LIMIT + ")"));
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1.4f);
		}
	}

	private List<FWShopItem> catalogFor(Category category, Player player) {
		switch (category) {
			case BLOCKS: return FWShopCatalog.blocks(team);
			case MELEE: return FWShopCatalog.melee();
			case ARMOR: return FWShopCatalog.armor();
			case TOOLS: return FWShopCatalog.tools(player);
			case BOWS_AND_ARROWS: return FWShopCatalog.bowsAndArrows();
			case POTIONS: return FWShopCatalog.potions();
			case UTILITY: return FWShopCatalog.utility();
			default: return Collections.emptyList();
		}
	}

	/** Regular click: buys one and tells the player how it went. */
	private void purchase(Player player, FWShopItem shopItem) {
		String failure = tryPurchase(player, shopItem);
		if (failure != null) {
			reject(player, failure);
			return;
		}

		player.sendMessage(main.color("&2&l(!) &rPurchased &e" + shopItem.getName()));
		playPurchaseFeedback(player, shopItem);
	}

	/**
	 * Right-click: buys the item over and over until the player can't afford another or has no room.
	 * Items that only ever keep one (armor, swords, pickaxes, axes, Shears) just buy once.
	 */
	private void purchaseMax(Player player, FWShopItem shopItem) {
		if (!shopItem.isRepeatable()) {
			purchase(player, shopItem);
			return;
		}

		int bought = 0;
		String failure = null;
		while (bought < MAX_BUY_LIMIT) {
			failure = tryPurchase(player, shopItem);
			if (failure != null) break;
			bought++;
		}

		if (bought == 0) {
			reject(player, failure);
			return;
		}

		int total = shopItem.getGiveItems().get(0).getAmount() * bought;
		player.sendMessage(main.color("&2&l(!) &rPurchased &e" + total + "x " + shopItem.getName()));
		if (MSG_INVENTORY_FULL.equals(failure)) {
			player.sendMessage(main.color(MSG_INVENTORY_FULL));
		}
		playPurchaseFeedback(player, shopItem);
	}

	private void reject(Player player, String message) {
		player.sendMessage(main.color(message));
		player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
	}

	private void playPurchaseFeedback(Player player, FWShopItem shopItem) {
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		if (shopItem.isArmorSet()) {
			player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1.0f, 1.0f);
		}
		playShopkeeperTradeEffect();
	}

	/**
	 * Buys one of the item. Returns null on success, or the message to show the player when it
	 * couldn't be bought (nothing is taken in that case). Sends no messages or sounds itself so
	 * "buy max" can call it in a loop.
	 */
	private String tryPurchase(Player player, FWShopItem shopItem) {
		if (shopItem.isArmorSet() && !isArmorUpgrade(player, shopItem)) return MSG_ARMOR_OWNED;
		if (isSwordPurchase(shopItem) && !isSwordUpgrade(player, shopItem)) return MSG_SWORD_OWNED;

		if (!shopItem.isAffordableFor(player)) {
			return "&c&l(!) &rYou don't have enough " + shopItem.getCostType().getDisplayName() + "!";
		}
		if (!hasRoomFor(player, shopItem)) return MSG_INVENTORY_FULL;

		if (!FWEconomy.withdraw(player, shopItem.getCostType(), shopItem.getCost())) {
			return "&c&l(!) &rYou don't have enough " + shopItem.getCostType().getDisplayName() + "!";
		}

		for (ItemStack piece : shopItem.getGiveItems()) {
			ItemStack upgraded = instance.getTeamUpgradeManager().applyUpgrades(team, piece);
			if (shopItem.isArmorSet()) {
				equip(player, upgraded);
			} else if (piece.getType().name().endsWith("_SWORD")) {
				giveSword(player, upgraded);
			} else if (piece.getType().name().endsWith("_PICKAXE")) {
				givePickaxe(player, upgraded);
			} else if (piece.getType().name().endsWith("_AXE")) {
				giveAxe(player, upgraded);
			} else if (GrapplingHookItem.matches(upgraded)) {
				giveGrapplingHook(player, upgraded);
			} else {
				player.getInventory().addItem(upgraded);
			}
		}
		return null;
	}

	/**
	 * Whether everything the purchase hands over has somewhere to go. Without this, a full
	 * inventory silently ate the items after the resources were already taken, which "buy max"
	 * would hit constantly.
	 */
	private boolean hasRoomFor(Player player, FWShopItem shopItem) {
		if (shopItem.isArmorSet()) return true; // worn straight away, never takes an inventory slot
		for (ItemStack piece : shopItem.getGiveItems()) {
			ItemStack upgraded = instance.getTeamUpgradeManager().applyUpgrades(team, piece);
			if (replacesOwnedItem(player, upgraded)) continue;
			if (!fitsInInventory(player, upgraded)) return false;
		}
		return true;
	}

	/** Swords, pickaxes, axes and the Grappling Hook swap into or merge with the one the player already carries, so they need no free slot then. */
	private boolean replacesOwnedItem(Player player, ItemStack piece) {
		String type = piece.getType().name();
		String suffix = null;
		if (type.endsWith("_SWORD")) suffix = "_SWORD";
		else if (type.endsWith("_PICKAXE")) suffix = "_PICKAXE";
		else if (type.endsWith("_AXE")) suffix = "_AXE";
		boolean hook = GrapplingHookItem.matches(piece);
		if (suffix == null && !hook) return false;

		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null) continue;
			if (hook ? GrapplingHookItem.matches(item) : item.getType().name().endsWith(suffix)) return true;
		}
		return false;
	}

	/** Same slots Inventory#addItem would use: the 36 storage slots (not the armor slots), topping up matching stacks first. */
	private boolean fitsInInventory(Player player, ItemStack stack) {
		int maxStack = stack.getMaxStackSize() > 0 ? stack.getMaxStackSize() : 64;
		Inventory inventory = player.getInventory();
		int slots = Math.min(36, inventory.getSize());

		int room = 0;
		for (int i = 0; i < slots; i++) {
			ItemStack slot = inventory.getItem(i);
			if (slot == null || slot.getType() == Material.AIR) {
				room += maxStack;
			} else if (slot.isSimilar(stack)) {
				room += Math.max(0, maxStack - slot.getAmount());
			}
			if (room >= stack.getAmount()) return true;
		}
		return false;
	}

	/**
	 * Same trade feedback VillagerAbilityGUI uses in SCB: green Happy Villager particles
	 * scattered around a 1-block-radius circle at head height, plus the villager haggle
	 * sound - centered on the shopkeeper villager.
	 */
	private void playShopkeeperTradeEffect() {
		Villager shopkeeper = instance.getFlagManager().getShopkeeper(team.getSiteId());
		if (shopkeeper == null) return;

		Location center = shopkeeper.getEyeLocation();
		center.getWorld().playSound(center, Sound.VILLAGER_HAGGLE, 1f, 1f);

		for (int i = 0; i < 100; i++) {
			double angle = Math.random() * Math.PI * 2;
			double x = Math.cos(angle);
			double z = Math.sin(angle);
			center.getWorld().playEffect(center.clone().add(x, 0, z), Effect.HAPPY_VILLAGER, 0);
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

	/** Wood is the untracked starter tier every player begins with - everything above it is a real shop tier. */
	private static final List<Material> SWORD_TIER_ORDER = Arrays.asList(
			Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD);

	private boolean isSwordPurchase(FWShopItem shopItem) {
		for (ItemStack piece : shopItem.getGiveItems()) {
			if (piece.getType().name().endsWith("_SWORD")) return true;
		}
		return false;
	}

	/** Blocks re-buying the same sword tier or a lower one (an accidental downgrade) - same rule as isArmorUpgrade. */
	private boolean isSwordUpgrade(Player player, FWShopItem swordItem) {
		int currentTier = -1;
		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null && item.getType().name().endsWith("_SWORD")) {
				currentTier = Math.max(currentTier, SWORD_TIER_ORDER.indexOf(item.getType()));
			}
		}

		int newTier = -1;
		for (ItemStack piece : swordItem.getGiveItems()) {
			if (piece.getType().name().endsWith("_SWORD")) {
				newTier = SWORD_TIER_ORDER.indexOf(piece.getType());
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
	 * alongside it and leaving the old one cluttering the inventory. Any other stray sword the
	 * player happens to be holding (e.g. a leftover from before this "one sword only" rule) is
	 * cleared out rather than kept.
	 *
	 * The held slot is always preferred as the swap target over just the first sword found by
	 * index: otherwise, if the sword actually in the player's hand isn't the earliest one in
	 * inventory order, the new sword lands elsewhere in the inventory and the held slot gets
	 * nulled out instead - the hand goes empty and it looks like the purchase removed every sword.
	 */
	private void giveSword(Player player, ItemStack newSword) {
		ItemStack[] contents = player.getInventory().getContents();
		int heldSlot = player.getInventory().getHeldItemSlot();

		int keeperSlot = -1;
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || !item.getType().name().endsWith("_SWORD")) continue;
			if (keeperSlot == -1 || i == heldSlot) keeperSlot = i;
		}

		if (keeperSlot == -1) {
			player.getInventory().addItem(newSword);
			return;
		}

		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && i != keeperSlot && item.getType().name().endsWith("_SWORD")) {
				player.getInventory().setItem(i, null);
			}
		}
		player.getInventory().setItem(keeperSlot, newSword);
	}

	/** Buying another Grappling Hook adds its uses to the one they already have, instead of a second hook. */
	private void giveGrapplingHook(Player player, ItemStack newHook) {
		for (ItemStack item : player.getInventory().getContents()) {
			if (GrapplingHookItem.matches(item)) {
				GrapplingHookItem.addUses(item, GrapplingHookItem.getUses(newHook));
				return;
			}
		}
		player.getInventory().addItem(newHook);
	}

	/** Buying a pickaxe tier replaces the previous one in-slot, same held-slot-preferring rule as giveSword. */
	private void givePickaxe(Player player, ItemStack newPickaxe) {
		ItemStack[] contents = player.getInventory().getContents();
		int heldSlot = player.getInventory().getHeldItemSlot();

		int keeperSlot = -1;
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || !item.getType().name().endsWith("_PICKAXE")) continue;
			if (keeperSlot == -1 || i == heldSlot) keeperSlot = i;
		}

		if (keeperSlot == -1) {
			player.getInventory().addItem(newPickaxe);
			return;
		}

		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && i != keeperSlot && item.getType().name().endsWith("_PICKAXE")) {
				player.getInventory().setItem(i, null);
			}
		}
		player.getInventory().setItem(keeperSlot, newPickaxe);
	}

	/** Buying an axe tier replaces the previous one in-slot, same held-slot-preferring rule as giveSword/givePickaxe. */
	private void giveAxe(Player player, ItemStack newAxe) {
		ItemStack[] contents = player.getInventory().getContents();
		int heldSlot = player.getInventory().getHeldItemSlot();

		int keeperSlot = -1;
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item == null || !item.getType().name().endsWith("_AXE")) continue;
			if (keeperSlot == -1 || i == heldSlot) keeperSlot = i;
		}

		if (keeperSlot == -1) {
			player.getInventory().addItem(newAxe);
			return;
		}

		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null && i != keeperSlot && item.getType().name().endsWith("_AXE")) {
				player.getInventory().setItem(i, null);
			}
		}
		player.getInventory().setItem(keeperSlot, newAxe);
	}

	/**
	 * Same look as the other tabs: items start on the second column, same gold bold names, same lore
	 * order. Upgrades go on the first item row and the traps get the last row, with the Trap Info
	 * item at the end of that row (the 8th slot). That leaves room for 6 traps before they would
	 * run into it, so a 7th trap needs the info item moved.
	 */
	private void renderUpgrades(Player player, InventoryContents contents) {
		List<FWTeamUpgrade> upgrades = sortedUpgrades();
		for (int i = 0; i < upgrades.size(); i++) {
			FWTeamUpgrade upgrade = upgrades.get(i);
			contents.set(gridRow(CATALOG_START_ROW, i), gridCol(i), ClickableItem.of(upgradeDisplayItem(player, upgrade), e -> {
				purchaseUpgrade(player, upgrade);
				render(player, contents);
			}));
		}

		List<FWTrap> traps = sortedTraps();
		for (int i = 0; i < traps.size(); i++) {
			FWTrap trap = traps.get(i);
			contents.set(TRAPS_ROW, gridCol(i), ClickableItem.of(trapDisplayItem(player, trap), e -> {
				purchaseTrap(player, trap);
				render(player, contents);
			}));
		}

		contents.set(TRAPS_ROW, TRAP_INFO_COL, ClickableItem.empty(ItemHelper.setDetails(
				new ItemStack(Material.TRIPWIRE_HOOK),
				main.color("&6&lTraps"),
				Arrays.asList(
						"",
						main.color("&7Goes off when an enemy enters your base."),
						main.color("&7You can queue up to " + FWTrapManager.MAX_QUEUE_SIZE + " traps."),
						main.color("&7Queued traps go off 30 seconds apart."),
						main.color("&7The Decoy Flag Trap is set right away.")
				)
		)));
	}

	/**
	 * Resource, then cost, then name, like the other tabs. An upgrade's cost changes with every
	 * tier, so the first tier's cost is used: sorting by the current cost would shuffle the row
	 * after every purchase and the next click could land on a different upgrade.
	 */
	private List<FWTeamUpgrade> sortedUpgrades() {
		List<FWTeamUpgrade> upgrades = new ArrayList<>(Arrays.asList(FWTeamUpgrade.values()));
		Collections.sort(upgrades, new Comparator<FWTeamUpgrade>() {
			@Override
			public int compare(FWTeamUpgrade a, FWTeamUpgrade b) {
				Map.Entry<FWResourceType, Integer> costA = a.costForTier(1).entrySet().iterator().next();
				Map.Entry<FWResourceType, Integer> costB = b.costForTier(1).entrySet().iterator().next();
				int byResource = Integer.compare(costA.getKey().ordinal(), costB.getKey().ordinal());
				if (byResource != 0) return byResource;
				int byCost = Integer.compare(costA.getValue(), costB.getValue());
				if (byCost != 0) return byCost;
				return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
			}
		});
		return upgrades;
	}

	/** Traps are all paid in diamonds, so this is cost, then name. */
	private List<FWTrap> sortedTraps() {
		List<FWTrap> traps = new ArrayList<>(Arrays.asList(FWTrap.values()));
		Collections.sort(traps, new Comparator<FWTrap>() {
			@Override
			public int compare(FWTrap a, FWTrap b) {
				int byCost = Integer.compare(a.getDiamondCost(), b.getDiamondCost());
				if (byCost != 0) return byCost;
				return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
			}
		});
		return traps;
	}

	private ItemStack upgradeDisplayItem(Player player, FWTeamUpgrade upgrade) {
		int tier = team.getUpgradeTier(upgrade);
		boolean maxed = tier >= upgrade.getMaxTier();
		Map<FWResourceType, Integer> cost = maxed ? null : upgrade.costForTier(tier + 1);
		boolean affordable = cost != null && FWEconomy.canAfford(player, cost);

		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color("&7" + upgrade.getDescription()));
		lore.add("");
		if (cost != null) lore.add(main.color(FWResourceType.formatCost(cost)));
		lore.add(main.color("&rTier: &a" + tier + "/" + upgrade.getMaxTier()));
		lore.add("");
		if (maxed) {
			lore.add(main.color("&aMax tier reached!"));
		} else {
			lore.add(main.color(affordable ? "&e&nLeft Click&r&e to purchase" : "&cNot enough resources!"));
		}

		return ItemHelper.setDetails(new ItemStack(iconFor(upgrade)),
				main.color("&6&l" + upgrade.getDisplayName()), lore);
	}

	private Material iconFor(FWTeamUpgrade upgrade) {
		switch (upgrade) {
			case RESOURCE_RUSH: return Material.FURNACE;
			case SHARPENED_SWORDS: return Material.IRON_SWORD;
			case REINFORCED_ARMOR: return Material.IRON_CHESTPLATE;
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
			player.sendMessage(main.color("&c&l(!) &rYou can't afford that upgrade!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		playShopkeeperTradeEffect();
	}

	private ItemStack trapDisplayItem(Player player, FWTrap trap) {
		int queued = team.getTrapQueueSize();
		boolean full = queued >= FWTrapManager.MAX_QUEUE_SIZE;
		boolean affordable = FWEconomy.canAfford(player, FWResourceType.DIAMOND, trap.getDiamondCost());

		Map<FWResourceType, Integer> cost = new EnumMap<>(FWResourceType.class);
		cost.put(FWResourceType.DIAMOND, trap.getDiamondCost());

		List<String> lore = new ArrayList<>();
		lore.add("");
		for (String line : trap.getDescription().split("\n")) lore.add(main.color("&7" + line));
		lore.add("");
		lore.add(main.color(FWResourceType.formatCost(cost)));

		// An instant trap (the decoy flag) has no queue, so what can stop it is its own problem instead
		String problem = trap.isInstant() ? instance.getTrapManager().decoyProblem(team) : null;
		if (!trap.isInstant()) lore.add(main.color("&rQueue: &a" + queued + "/" + FWTrapManager.MAX_QUEUE_SIZE));
		lore.add("");
		if (problem != null) {
			lore.add(main.color("&c" + problem));
		} else if (full && !trap.isInstant()) {
			lore.add(main.color("&cTrap queue is full!"));
		} else {
			lore.add(main.color(affordable ? "&e&nLeft Click&r&e to purchase" : "&cNot enough resources!"));
		}

		// Show what's currently queued so teammates can see
		List<FWTrap> queue = team.getTrapQueue();
		if (!queue.isEmpty()) {
			lore.add("");
			lore.add(main.color("&7Queued traps:"));
			for (int i = 0; i < queue.size(); i++) {
				lore.add(main.color("&7  " + (i + 1) + ". &e" + queue.get(i).getDisplayName()));
			}
		}

		return ItemHelper.setDetails(new ItemStack(trapIconFor(trap)),
				main.color("&6&l" + trap.getDisplayName()), lore);
	}

	private Material trapIconFor(FWTrap trap) {
		switch (trap) {
			case MINING_FATIGUE:     return Material.IRON_PICKAXE;
			case MLG:                return Material.WATER_BUCKET;
			case SLOWNESS_BLINDNESS: return Material.INK_SACK;
			case CAGE:               return Material.IRON_FENCE;
			case DECOY_FLAG:         return Material.BANNER;
			default:                 return Material.TRIPWIRE_HOOK;
		}
	}

	/**
	 * An instant trap (the decoy flag) goes off on the spot and never takes a queue slot. It is checked
	 * before any diamonds are taken, so a purchase that can't work costs nothing.
	 */
	private void purchaseInstantTrap(Player player, FWTrap trap) {
		String problem = instance.getTrapManager().decoyProblem(team);
		if (problem != null) {
			player.sendMessage(main.color("&c&l(!) &r" + problem));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		if (!FWEconomy.withdraw(player, FWResourceType.DIAMOND, trap.getDiamondCost())) {
			player.sendMessage(main.color("&c&l(!) &rYou don't have enough Diamonds!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		instance.getTrapManager().armDecoy(team);
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		playShopkeeperTradeEffect();
	}

	private void purchaseTrap(Player player, FWTrap trap) {
		if (trap.isInstant()) {
			purchaseInstantTrap(player, trap);
			return;
		}

		if (team.getTrapQueueSize() >= FWTrapManager.MAX_QUEUE_SIZE) {
			player.sendMessage(main.color("&c&l(!) &rYour trap queue is full!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		if (!FWEconomy.withdraw(player, FWResourceType.DIAMOND, trap.getDiamondCost())) {
			player.sendMessage(main.color("&c&l(!) &rYou don't have enough Diamonds!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		team.addTrap(trap);
		player.sendMessage(main.color("&2&l(!) &rQueued &e" + trap.getDisplayName() + " &r(&e"
				+ team.getTrapQueueSize() + "&r/&e" + FWTrapManager.MAX_QUEUE_SIZE + "&r)"));
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		playShopkeeperTradeEffect();

		// An enemy already standing in the base should set the new trap off right away
		instance.getTrapManager().onTrapPurchased(team);
	}

	/**
	 * Same tabs as the Cosmetics menu: yellow names across the top row, "Currently Viewing" on the open
	 * one, and a divider row under them that is white with a lime pane under the open tab.
	 */
	private void renderCategoryButtons(Player player, InventoryContents contents) {
		Category[] categories = Category.values();

		for (int i = 0; i < categories.length; i++) {
			Category cat = categories[i];
			boolean selected = cat == category;

			List<String> lore = new ArrayList<>();
			if (selected) {
				lore.add("");
				lore.add(main.color("&aCurrently Viewing"));
			}

			ItemStack icon = ItemHelper.setDetails(new ItemStack(cat.icon), main.color("&e" + cat.label), lore);

			contents.set(TAB_ROW, i, ClickableItem.of(icon, e -> {
				category = cat;
				SoundManager.playClickSound(player);
				render(player, contents);
			}));

			ItemStack divider = ItemHelper.setDetails(
					new ItemStack(Material.STAINED_GLASS_PANE, 1, selected ? PANE_LIME : PANE_WHITE), " ");
			contents.set(DIVIDER_ROW, i, ClickableItem.empty(divider));
		}
	}
}
