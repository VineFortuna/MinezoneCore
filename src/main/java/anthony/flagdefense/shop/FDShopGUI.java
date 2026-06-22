package anthony.flagdefense.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import anthony.flagdefense.FDGameInstance;
import anthony.flagdefense.FDTeam;
import anthony.flagdefense.flag.TeamLevel;
import anthony.flagdefense.resources.FDEconomy;
import anthony.flagdefense.resources.FDResourceType;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The FlagDefense shop, built with the same smart-invs library SCB's
 * own GUIs use. Hypixel-Bedwars-style: category tabs along the bottom row
 * switch what's shown in the main grid, including an Upgrades tab for the
 * team-wide Forge / Sharpened Swords / Reinforced Armor / Heal Pool tracks
 * plus the overall Team Level track (gates shop items/mechanics - used to
 * be bought by right-clicking the objective villager, now there's no
 * entity left to click so it lives here instead).
 * One instance is opened per shopping session so it can track which team is
 * buying and which tab they currently have open.
 */
public class FDShopGUI implements InventoryProvider {

	private enum Category {
		QUICK_BUY("Quick Buy", Material.GOLD_NUGGET),
		BLOCKS("Blocks", Material.BRICK),
		MELEE("Melee Weapons", Material.IRON_SWORD),
		ARMOR("Armor", Material.IRON_CHESTPLATE),
		TOOLS("Tools", Material.IRON_PICKAXE),
		ENCHANTMENTS("Enchantments", Material.ENCHANTED_BOOK),
		POTIONS("Potions", Material.POTION),
		UTILITY("Utility", Material.FISHING_ROD),
		UPGRADES("Upgrades", Material.ANVIL);

		final String label;
		final Material icon;

		Category(String label, Material icon) {
			this.label = label;
			this.icon = icon;
		}
	}

	private final Core main;
	private final FDGameInstance instance;
	private final FDTeam team;
	private final SmartInventory inv;

	private Category category = Category.QUICK_BUY;

	public FDShopGUI(FDGameInstance instance, FDTeam team) {
		this.instance = instance;
		this.main = instance.getManager().getMain();
		this.team = team;
		this.inv = SmartInventory.builder()
				.id("fdShop")
				.provider(this)
				.size(6, 9)
				.title(main.color("&8&lFlagDefense Shop"))
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
		render(player, contents);
	}

	private void render(Player player, InventoryContents contents) {
		List<ClickableItem> items = category == Category.UPGRADES ? upgradeClickables(player) : catalogClickables(player);
		ItemStack filler = ItemHelper.getGlassFiller();

		for (int row = 0; row < 5; row++) {
			for (int column = 0; column < 7; column++) {
				int index = row * 7 + column;
				contents.set(row, column + 1, index < items.size() ? items.get(index) : ClickableItem.empty(filler));
			}
			contents.set(row, 0, ClickableItem.empty(filler));
			contents.set(row, 8, ClickableItem.empty(filler));
		}

		renderCategoryButtons(contents, filler);
	}

	private List<ClickableItem> catalogClickables(Player player) {
		List<ClickableItem> items = new ArrayList<>();
		for (FDShopItem shopItem : catalogFor(category)) {
			items.add(ClickableItem.of(shopItem.toDisplayItem(team, player, main), e -> purchase(player, shopItem)));
		}
		return items;
	}

	private List<FDShopItem> catalogFor(Category category) {
		switch (category) {
			case BLOCKS: return FDShopCatalog.blocks(team);
			case MELEE: return FDShopCatalog.melee();
			case ARMOR: return FDShopCatalog.armor();
			case TOOLS: return FDShopCatalog.tools();
			case ENCHANTMENTS: return FDShopCatalog.enchantments();
			case POTIONS: return FDShopCatalog.potions();
			case UTILITY: return FDShopCatalog.utility();
			default: return FDShopCatalog.quickBuy(team);
		}
	}

	private void purchase(Player player, FDShopItem shopItem) {
		if (!shopItem.isUnlockedFor(team)) {
			player.sendMessage(main.color("&c&l(!) &rUpgrade your team to &e" + shopItem.getRequiredLevel().name()
					+ " &rfirst!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		if (!FDEconomy.withdraw(player, shopItem.getCostType(), shopItem.getCost())) {
			player.sendMessage(main.color("&c&l(!) &rYou don't have enough " + shopItem.getCostType().name() + "!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		ItemStack giveItem = instance.getTeamUpgradeManager().applyUpgrades(team, shopItem.getGiveItem());
		player.getInventory().addItem(giveItem);
		player.sendMessage(main.color("&2&l(!) &rPurchased &e" + shopItem.getName() + "&r."));
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
	}

	private List<ClickableItem> upgradeClickables(Player player) {
		List<ClickableItem> items = new ArrayList<>();
		items.add(ClickableItem.of(teamLevelDisplayItem(), e -> purchaseTeamLevel(player)));
		for (FDTeamUpgrade upgrade : FDTeamUpgrade.values()) {
			items.add(ClickableItem.of(upgradeDisplayItem(upgrade), e -> purchaseUpgrade(player, upgrade)));
		}
		return items;
	}

	private ItemStack teamLevelDisplayItem() {
		TeamLevel level = team.getLevel();
		TeamLevel next = level.next();

		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color("&7Tier: &e" + level.getTier() + "&7/&e3"));

		if (next != null) {
			lore.add(main.color("&7Cost: " + formatCost(next.getUpgradeCost())));
			lore.add(main.color("&a&lClick to upgrade"));
		} else {
			lore.add(main.color("&a&lMax tier reached"));
		}

		return ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
				main.color((next == null ? "&a" : "&e") + "Team Level"), lore);
	}

	private void purchaseTeamLevel(Player player) {
		if (team.getLevel().isMaxLevel()) {
			player.sendMessage(main.color("&c&l(!) &rYour team is already max level!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		if (!instance.getTeamUpgradeManager().upgradeTeamLevel(team, player)) {
			player.sendMessage(main.color("&c&l(!) &rYou can't afford that upgrade yet!"));
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
			return;
		}

		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
	}

	private ItemStack upgradeDisplayItem(FDTeamUpgrade upgrade) {
		int tier = team.getUpgradeTier(upgrade);
		boolean maxed = tier >= upgrade.getMaxTier();
		Map<FDResourceType, Integer> cost = maxed ? null : upgrade.costForTier(tier + 1);

		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color("&7Tier: &e" + tier + "&7/&e" + upgrade.getMaxTier()));

		if (cost != null) {
			lore.add(main.color("&7Cost: " + formatCost(cost)));
			lore.add(main.color("&a&lClick to upgrade"));
		} else {
			lore.add(main.color("&a&lMax tier reached"));
		}

		return ItemHelper.setDetails(new ItemStack(iconFor(upgrade)), main.color((maxed ? "&a" : "&e") + upgrade.getDisplayName()), lore);
	}

	private Material iconFor(FDTeamUpgrade upgrade) {
		switch (upgrade) {
			case FORGE: return Material.FURNACE;
			case SHARPENED_SWORDS: return Material.IRON_SWORD;
			case REINFORCED_ARMOR: return Material.IRON_CHESTPLATE;
			case HEAL_POOL: return Material.SPECKLED_MELON;
			default: return Material.ANVIL;
		}
	}

	private String formatCost(Map<FDResourceType, Integer> cost) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<FDResourceType, Integer> entry : cost.entrySet()) {
			if (builder.length() > 0) builder.append("&7, ");
			builder.append("&b").append(entry.getValue()).append(' ').append(entry.getKey().name());
		}
		return builder.toString();
	}

	private void purchaseUpgrade(Player player, FDTeamUpgrade upgrade) {
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
	}

	private void renderCategoryButtons(InventoryContents contents, ItemStack filler) {
		Category[] categories = Category.values();

		for (int i = 0; i < categories.length; i++) {
			Category cat = categories[i];
			boolean selected = cat == category;

			List<String> lore = new ArrayList<>();
			lore.add(main.color(selected ? "&7Currently viewing" : "&7Click to view"));

			ItemStack icon = ItemHelper.setDetails(new ItemStack(cat.icon),
					main.color((selected ? "&a&l" : "&e&l") + cat.label), lore);
			if (selected) icon = ItemHelper.setGlowing(icon, true);

			contents.set(5, i, ClickableItem.of(icon, e -> category = cat));
		}

		for (int i = categories.length; i < 9; i++) {
			contents.set(5, i, ClickableItem.empty(filler));
		}
	}
}
