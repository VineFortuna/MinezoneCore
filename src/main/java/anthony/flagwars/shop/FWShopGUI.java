package anthony.flagwars.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
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
			renderCatalog(player, contents, catalogFor(category, player));
		}

		renderCategoryButtons(player, contents, filler);
	}

	private void renderCatalog(Player player, InventoryContents contents, List<FWShopItem> items) {
		for (FWShopItem shopItem : items) {
			contents.set(shopItem.getRow(), shopItem.getCol(), shopClickable(player, shopItem, contents));
		}
	}

	/**
	 * Default items keep their pinned slots (skipped if the player hid them). Added favorites
	 * fill in insertion order starting from the default row (2) downward, cols 1-7 (no edges).
	 */
	private void renderQuickBuy(Player player, InventoryContents contents) {
		Set<String> shown = new HashSet<>();
		Set<Integer> occupied = new HashSet<>();

		for (FWShopItem shopItem : FWShopCatalog.quickBuy(team)) {
			if (FWQuickBuyManager.isDefaultRemoved(main, player, shopItem.getName())) continue;
			contents.set(shopItem.getRow(), shopItem.getCol(), shopClickable(player, shopItem, contents));
			shown.add(shopItem.getName());
			occupied.add(shopItem.getRow() * 9 + shopItem.getCol());
		}

		List<Integer> freeSlots = new ArrayList<>();
		for (int row = 2; row <= 4; row++) {
			for (int col = 1; col <= 7; col++) {
				int slot = row * 9 + col;
				if (!occupied.contains(slot)) freeSlots.add(slot);
			}
		}

		List<FWShopItem> allItems = FWShopCatalog.allItems(team, player);
		int nextIndex = 0;
		for (String addedName : FWQuickBuyManager.getAdded(main, player)) {
			if (shown.contains(addedName)) continue;
			FWShopItem match = findByName(allItems, addedName);
			if (match == null || nextIndex >= freeSlots.size()) continue;
			int slot = freeSlots.get(nextIndex++);
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
		if (isDefaultQuickBuy(shopItem.getName())) {
			// If the player removed this default, show ADD so they can restore it
			return FWQuickBuyManager.isDefaultRemoved(main, player, shopItem.getName())
					? FWShopItem.QuickBuyHint.ADD : FWShopItem.QuickBuyHint.REMOVE;
		}
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
		boolean nowInQuickBuy;
		if (isDefaultQuickBuy(shopItem.getName())) {
			nowInQuickBuy = FWQuickBuyManager.toggleDefault(main, player, shopItem.getName());
		} else {
			nowInQuickBuy = FWQuickBuyManager.toggle(main, player, shopItem.getName());
		}
		player.sendMessage(main.color(nowInQuickBuy
				? "&2&l(!) &rAdded &e" + shopItem.getName() + "&r to your Quick Buy"
				: "&c&l(!) &rRemoved &e" + shopItem.getName() + "&r from your Quick Buy"));
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, nowInQuickBuy ? 1.4f : 0.8f);
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
			default: return FWShopCatalog.quickBuy(team);
		}
	}

	private void purchase(Player player, FWShopItem shopItem) {
		if (shopItem.isArmorSet() && !isArmorUpgrade(player, shopItem)) {
			player.sendMessage(main.color("&c&l(!) &rYou already own this tier of armor or better!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		if (isSwordPurchase(shopItem) && !isSwordUpgrade(player, shopItem)) {
			player.sendMessage(main.color("&c&l(!) &rYou already own this tier of sword or better!"));
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
			} else if (piece.getType().name().endsWith("_PICKAXE")) {
				givePickaxe(player, upgraded);
			} else if (piece.getType().name().endsWith("_AXE")) {
				giveAxe(player, upgraded);
			} else {
				player.getInventory().addItem(upgraded);
			}
		}

		player.sendMessage(main.color("&2&l(!) &rPurchased &e" + shopItem.getName()));
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
		if (shopItem.isArmorSet()) {
			player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1.0f, 1.0f);
		}
		playShopkeeperTradeEffect();
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

	private void renderUpgrades(Player player, InventoryContents contents) {
		// Row 2: team-wide upgrades
		int col = 1;
		for (FWTeamUpgrade upgrade : FWTeamUpgrade.values()) {
			contents.set(2, col++, ClickableItem.of(upgradeDisplayItem(player, upgrade), e -> {
				purchaseUpgrade(player, upgrade);
				render(player, contents);
			}));
		}

		// Row 3: traps divider
		contents.set(3, 4, ClickableItem.empty(ItemHelper.setDetails(
				new ItemStack(Material.TRIPWIRE_HOOK),
				main.color("&e&lTraps"),
				Arrays.asList(
						main.color("&7Triggered when an enemy enters your base."),
						main.color("&7Max " + FWTrapManager.MAX_QUEUE_SIZE + " traps queued at once.")
				)
		)));

		// Row 4: trap items
		FWTrap[] traps = FWTrap.values();
		for (int i = 0; i < traps.length; i++) {
			FWTrap trap = traps[i];
			int trapCol = i + 2;
			contents.set(4, trapCol, ClickableItem.of(trapDisplayItem(player, trap), e -> {
				purchaseTrap(player, trap);
				render(player, contents);
			}));
		}
	}

	private ItemStack upgradeDisplayItem(Player player, FWTeamUpgrade upgrade) {
		int tier = team.getUpgradeTier(upgrade);
		boolean maxed = tier >= upgrade.getMaxTier();
		Map<FWResourceType, Integer> cost = maxed ? null : upgrade.costForTier(tier + 1);

		List<String> lore = new ArrayList<>();
		lore.add("");
		if (cost != null) {
			boolean affordable = FWEconomy.canAfford(player, cost);
			lore.add(main.color(FWResourceType.formatCost(cost)));
			lore.add(main.color(affordable ? "&eClick to upgrade" : "&cNot enough resources!"));
		} else {
			lore.add(main.color("&aMax tier reached!"));
		}
		lore.add("");
		lore.add(main.color("&rTier: &a" + tier + "/" + upgrade.getMaxTier()));

		return ItemHelper.setDetails(new ItemStack(iconFor(upgrade)), main.color((maxed ? "&a" : "&e") + upgrade.getDisplayName()), lore);
	}

	private Material iconFor(FWTeamUpgrade upgrade) {
		switch (upgrade) {
			case RESOURCE_RUSH: return Material.FURNACE;
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

	private ItemStack trapDisplayItem(Player player, FWTrap trap) {
		int queued = team.getTrapQueueSize();
		boolean full = queued >= FWTrapManager.MAX_QUEUE_SIZE;
		boolean affordable = FWEconomy.canAfford(player, FWResourceType.DIAMOND, trap.getDiamondCost());

		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color(trap.getDescription()));
		lore.add("");
		lore.add(main.color("&bCost: &f" + trap.getDiamondCost() + " Diamond" + (trap.getDiamondCost() > 1 ? "s" : "")));
		if (full) {
			lore.add(main.color("&c&lTrap queue is full! (" + queued + "/" + FWTrapManager.MAX_QUEUE_SIZE + ")"));
		} else {
			lore.add(main.color(affordable ? "&a&lClick to queue" : "&c&lNot enough Diamonds"));
			lore.add(main.color("&7Queue: &e" + queued + "&7/&e" + FWTrapManager.MAX_QUEUE_SIZE));
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
				main.color((full ? "&c" : "&e") + trap.getDisplayName()), lore);
	}

	private Material trapIconFor(FWTrap trap) {
		switch (trap) {
			case MINING_FATIGUE:     return Material.IRON_PICKAXE;
			case MLG:                return Material.WATER_BUCKET;
			case SLOWNESS_BLINDNESS: return Material.INK_SACK;
			default:                 return Material.TRIPWIRE_HOOK;
		}
	}

	private void purchaseTrap(Player player, FWTrap trap) {
		if (team.getTrapQueueSize() >= FWTrapManager.MAX_QUEUE_SIZE) {
			player.sendMessage(main.color("&c&l(!) &rYour trap queue is full! (" + FWTrapManager.MAX_QUEUE_SIZE + "/" + FWTrapManager.MAX_QUEUE_SIZE + ")"));
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
