package anthony.flagdefense.shop;

import anthony.util.ItemHelper;
import anthony.flagdefense.FDTeam;
import anthony.flagdefense.flag.TeamLevel;
import anthony.flagdefense.items.GrapplingHookItem;
import anthony.flagdefense.items.TeleportBowItem;
import anthony.flagdefense.items.ThrowableTntItem;
import anthony.flagdefense.resources.FDResourceType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.List;

/**
 * Static catalog data for FDShopGUI, split into tabs the same way Hypixel's
 * own Bedwars shop is (Blocks, Melee, Armor, Tools, Enchantments, Potions,
 * Utility), plus a curated Quick Buy tab. The Upgrades tab is generated
 * separately from FDTeamUpgrade since those entries are tiered rather than
 * flat purchases.
 */
public final class FDShopCatalog {

	private FDShopCatalog() {
	}

	public static List<FDShopItem> blocks(FDTeam team) {
		return Arrays.asList(
				new FDShopItem("Wool", new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Ladder", new ItemStack(Material.LADDER, 16),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Wood Planks", new ItemStack(Material.WOOD, 16),
						FDResourceType.IRON, 6, TeamLevel.LEVEL_1),
				new FDShopItem("Glass", new ItemStack(Material.GLASS, 8),
						FDResourceType.GOLD, 5, TeamLevel.LEVEL_1),
				new FDShopItem("End Stone", new ItemStack(Material.ENDER_STONE, 8),
						FDResourceType.IRON, 12, TeamLevel.LEVEL_1),
				new FDShopItem("Hardened Clay", new ItemStack(Material.STAINED_CLAY, 16),
						FDResourceType.GOLD, 16, TeamLevel.LEVEL_2),
				new FDShopItem("Obsidian", new ItemStack(Material.OBSIDIAN, 4),
						FDResourceType.EMERALD, 4, TeamLevel.LEVEL_2)
		);
	}

	public static List<FDShopItem> melee() {
		return Arrays.asList(
				new FDShopItem("Wood Sword", new ItemStack(Material.WOOD_SWORD),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Stone Sword", new ItemStack(Material.STONE_SWORD),
						FDResourceType.IRON, 10, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Sword", new ItemStack(Material.IRON_SWORD),
						FDResourceType.GOLD, 7, TeamLevel.LEVEL_1),
				new FDShopItem("Diamond Sword", new ItemStack(Material.DIAMOND_SWORD),
						FDResourceType.EMERALD, 4, TeamLevel.LEVEL_2),
				new FDShopItem("Knockback Stick", ItemHelper.addEnchant(new ItemStack(Material.STICK), Enchantment.KNOCKBACK, 1),
						FDResourceType.GOLD, 5, TeamLevel.LEVEL_2)
		);
	}

	/** Full armor sets at four tiers - Chainmail (cheap starter), Iron, Gold, Diamond. */
	public static List<FDShopItem> armor() {
		return Arrays.asList(
				new FDShopItem("Chainmail Helmet", new ItemStack(Material.CHAINMAIL_HELMET),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Chainmail Chestplate", new ItemStack(Material.CHAINMAIL_CHESTPLATE),
						FDResourceType.IRON, 8, TeamLevel.LEVEL_1),
				new FDShopItem("Chainmail Leggings", new ItemStack(Material.CHAINMAIL_LEGGINGS),
						FDResourceType.IRON, 6, TeamLevel.LEVEL_1),
				new FDShopItem("Chainmail Boots", new ItemStack(Material.CHAINMAIL_BOOTS),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),

				new FDShopItem("Iron Helmet", new ItemStack(Material.IRON_HELMET),
						FDResourceType.GOLD, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Chestplate", new ItemStack(Material.IRON_CHESTPLATE),
						FDResourceType.GOLD, 6, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Leggings", new ItemStack(Material.IRON_LEGGINGS),
						FDResourceType.GOLD, 5, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Boots", new ItemStack(Material.IRON_BOOTS),
						FDResourceType.GOLD, 3, TeamLevel.LEVEL_1),

				new FDShopItem("Gold Helmet", new ItemStack(Material.GOLD_HELMET),
						FDResourceType.EMERALD, 3, TeamLevel.LEVEL_2),
				new FDShopItem("Gold Chestplate", new ItemStack(Material.GOLD_CHESTPLATE),
						FDResourceType.EMERALD, 4, TeamLevel.LEVEL_2),
				new FDShopItem("Gold Leggings", new ItemStack(Material.GOLD_LEGGINGS),
						FDResourceType.EMERALD, 3, TeamLevel.LEVEL_2),
				new FDShopItem("Gold Boots", new ItemStack(Material.GOLD_BOOTS),
						FDResourceType.EMERALD, 2, TeamLevel.LEVEL_2),

				new FDShopItem("Diamond Helmet", new ItemStack(Material.DIAMOND_HELMET),
						FDResourceType.EMERALD, 4, TeamLevel.LEVEL_3),
				new FDShopItem("Diamond Chestplate", new ItemStack(Material.DIAMOND_CHESTPLATE),
						FDResourceType.EMERALD, 6, TeamLevel.LEVEL_3),
				new FDShopItem("Diamond Leggings", new ItemStack(Material.DIAMOND_LEGGINGS),
						FDResourceType.EMERALD, 5, TeamLevel.LEVEL_3),
				new FDShopItem("Diamond Boots", new ItemStack(Material.DIAMOND_BOOTS),
						FDResourceType.EMERALD, 3, TeamLevel.LEVEL_3)
		);
	}

	public static List<FDShopItem> tools() {
		return Arrays.asList(
				new FDShopItem("Wood Pickaxe", new ItemStack(Material.WOOD_PICKAXE),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Wood Axe", new ItemStack(Material.WOOD_AXE),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Shears", new ItemStack(Material.SHEARS),
						FDResourceType.IRON, 2, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Pickaxe", new ItemStack(Material.IRON_PICKAXE),
						FDResourceType.IRON, 8, TeamLevel.LEVEL_2),
				new FDShopItem("Iron Axe", new ItemStack(Material.IRON_AXE),
						FDResourceType.IRON, 8, TeamLevel.LEVEL_2),
				new FDShopItem("Diamond Pickaxe", new ItemStack(Material.DIAMOND_PICKAXE),
						FDResourceType.EMERALD, 4, TeamLevel.LEVEL_3)
		);
	}

	/**
	 * Pre-enchanted gear rather than books-to-combine (no anvil GUI on 1.8
	 * without extra plumbing) - click and the enchanted item lands straight
	 * in your inventory.
	 */
	public static List<FDShopItem> enchantments() {
		return Arrays.asList(
				new FDShopItem("Power I Bow", ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1),
						FDResourceType.GOLD, 8, TeamLevel.LEVEL_2),
				new FDShopItem("Punch I Bow", ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_KNOCKBACK, 1),
						FDResourceType.GOLD, 8, TeamLevel.LEVEL_2),
				new FDShopItem("Efficiency II Pickaxe", ItemHelper.addEnchant(new ItemStack(Material.IRON_PICKAXE), Enchantment.DIG_SPEED, 2),
						FDResourceType.GOLD, 6, TeamLevel.LEVEL_1),
				new FDShopItem("Feather Falling II Boots", ItemHelper.addEnchant(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION_FALL, 2),
						FDResourceType.GOLD, 10, TeamLevel.LEVEL_2)
		);
	}

	public static List<FDShopItem> potions() {
		return Arrays.asList(
				new FDShopItem("Bread", new ItemStack(Material.BREAD, 5),
						FDResourceType.IRON, 2, TeamLevel.LEVEL_1),
				new FDShopItem("Golden Apple", new ItemStack(Material.GOLDEN_APPLE),
						FDResourceType.GOLD, 3, TeamLevel.LEVEL_2),
				new FDShopItem("Healing Potion", ItemHelper.createPotionItem(PotionType.INSTANT_HEAL, 0, 0, true, true, false),
						FDResourceType.EMERALD, 2, TeamLevel.LEVEL_2),
				new FDShopItem("Speed Potion", ItemHelper.createPotionItem(PotionType.SPEED, 1, 45, true, true, false),
						FDResourceType.EMERALD, 1, TeamLevel.LEVEL_2),
				new FDShopItem("Jump Boost Potion", ItemHelper.createPotionItem(PotionType.JUMP, 0, 45, true, true, false),
						FDResourceType.GOLD, 2, TeamLevel.LEVEL_2),
				new FDShopItem("Fire Resistance Potion", ItemHelper.createPotionItem(PotionType.FIRE_RESISTANCE, 0, 60, true, true, false),
						FDResourceType.EMERALD, 1, TeamLevel.LEVEL_2),
				new FDShopItem("Invisibility Potion", ItemHelper.createPotionItem(PotionType.INVISIBILITY, 0, 30, true, true, false),
						FDResourceType.EMERALD, 1, TeamLevel.LEVEL_3)
		);
	}

	public static List<FDShopItem> utility() {
		return Arrays.asList(
				new FDShopItem("Bow", new ItemStack(Material.BOW),
						FDResourceType.GOLD, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Arrow", new ItemStack(Material.ARROW, 8),
						FDResourceType.IRON, 2, TeamLevel.LEVEL_1),
				new FDShopItem("Ender Pearl", new ItemStack(Material.ENDER_PEARL),
						FDResourceType.GOLD, 4, TeamLevel.LEVEL_2),
				new FDShopItem(GrapplingHookItem.NAME, GrapplingHookItem.create(),
						FDResourceType.GOLD, 10, TeamLevel.LEVEL_2),
				new FDShopItem(ThrowableTntItem.NAME, ThrowableTntItem.create(),
						FDResourceType.GOLD, 6, TeamLevel.LEVEL_2),
				new FDShopItem(TeleportBowItem.NAME, TeleportBowItem.create(),
						FDResourceType.DIAMOND, 12, TeamLevel.LEVEL_3)
		);
	}

	/** A handful of the most commonly-bought items, shortcut tab like Hypixel's "Quick Buy". */
	public static List<FDShopItem> quickBuy(FDTeam team) {
		return Arrays.asList(
				new FDShopItem("Wool", new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Wood Sword", new ItemStack(Material.WOOD_SWORD),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Sword", new ItemStack(Material.IRON_SWORD),
						FDResourceType.GOLD, 7, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Chestplate", new ItemStack(Material.IRON_CHESTPLATE),
						FDResourceType.GOLD, 6, TeamLevel.LEVEL_1),
				new FDShopItem("Iron Boots", new ItemStack(Material.IRON_BOOTS),
						FDResourceType.GOLD, 3, TeamLevel.LEVEL_1),
				new FDShopItem("Wood Pickaxe", new ItemStack(Material.WOOD_PICKAXE),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Wood Axe", new ItemStack(Material.WOOD_AXE),
						FDResourceType.IRON, 4, TeamLevel.LEVEL_1),
				new FDShopItem("Bread", new ItemStack(Material.BREAD, 5),
						FDResourceType.IRON, 2, TeamLevel.LEVEL_1)
		);
	}
}
