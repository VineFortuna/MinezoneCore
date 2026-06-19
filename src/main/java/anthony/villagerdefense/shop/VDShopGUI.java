package anthony.villagerdefense.shop;

import anthony.SuperCraftBrawl.Core;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.items.GrapplingHookItem;
import anthony.villagerdefense.items.TeleportBowItem;
import anthony.villagerdefense.items.ThrowableTntItem;
import anthony.villagerdefense.resources.VDResourceType;
import anthony.villagerdefense.villager.VillagerLevel;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * The VillagerDefense shop, built with the same smart-invs library SCB's
 * own GUIs use. One instance is opened per shopping session so it can know
 * which team is buying.
 */
public class VDShopGUI implements InventoryProvider {

	private final Core main;
	private final VDTeam team;
	private final SmartInventory inv;

	public VDShopGUI(Core main, VDTeam team) {
		this.main = main;
		this.team = team;
		this.inv = SmartInventory.builder()
				.id("vdShop")
				.provider(this)
				.size(6, 9)
				.title(main.color("&8&lVillagerDefense Shop"))
				.build();
	}

	public void open(Player player) {
		inv.open(player);
	}

	private List<VDShopItem> catalog() {
		return Arrays.asList(
				new VDShopItem("Blocks", new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Sword", new ItemStack(Material.WOOD_SWORD),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Pickaxe", new ItemStack(Material.WOOD_PICKAXE),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Axe", new ItemStack(Material.WOOD_AXE),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Bread", new ItemStack(Material.BREAD, 5),
						VDResourceType.IRON, 2, VillagerLevel.LEVEL_1),

				new VDShopItem(GrapplingHookItem.NAME, GrapplingHookItem.create(),
						VDResourceType.GOLD, 10, VillagerLevel.LEVEL_2),
				new VDShopItem(ThrowableTntItem.NAME, ThrowableTntItem.create(),
						VDResourceType.GOLD, 6, VillagerLevel.LEVEL_2),
				new VDShopItem("Iron Chestplate", new ItemStack(Material.IRON_CHESTPLATE),
						VDResourceType.GOLD, 8, VillagerLevel.LEVEL_2),
				new VDShopItem("Iron Leggings", new ItemStack(Material.IRON_LEGGINGS),
						VDResourceType.GOLD, 6, VillagerLevel.LEVEL_2),

				new VDShopItem(TeleportBowItem.NAME, TeleportBowItem.create(),
						VDResourceType.DIAMOND, 12, VillagerLevel.LEVEL_3)
		);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		List<VDShopItem> catalog = catalog();

		for (int i = 0; i < catalog.size(); i++) {
			VDShopItem shopItem = catalog.get(i);
			int row = i / 7;
			int column = 1 + (i % 7);

			contents.set(row, column, ClickableItem.of(shopItem.toDisplayItem(team), e -> purchase(player, shopItem)));
		}

		ItemStack filler = ItemHelper.getGlassFiller();
		for (int row = 0; row < 6; row++) {
			contents.set(row, 0, ClickableItem.of(filler, e -> {
			}));
			contents.set(row, 8, ClickableItem.of(filler, e -> {
			}));
		}
	}

	private void purchase(Player player, VDShopItem shopItem) {
		if (!shopItem.isUnlockedFor(team)) {
			player.sendMessage(main.color("&c&l(!) &rUpgrade your villager to &e" + shopItem.getRequiredLevel().name()
					+ " &rfirst!"));
			return;
		}

		if (!shopItem.isAffordableFor(team)) {
			player.sendMessage(main.color("&c&l(!) &rYou don't have enough " + shopItem.getCostType().name() + "!"));
			return;
		}

		team.spend(java.util.Collections.singletonMap(shopItem.getCostType(), shopItem.getCost()));
		player.getInventory().addItem(shopItem.getGiveItem());
		player.sendMessage(main.color("&2&l(!) &rPurchased &e" + shopItem.getName() + "&r."));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		List<VDShopItem> catalog = catalog();

		for (int i = 0; i < catalog.size(); i++) {
			VDShopItem shopItem = catalog.get(i);
			int row = i / 7;
			int column = 1 + (i % 7);

			contents.set(row, column, ClickableItem.of(shopItem.toDisplayItem(team), e -> purchase(player, shopItem)));
		}
	}
}
