package anthony.villagerdefense.shop;

import anthony.util.ItemHelper;
import anthony.villagerdefense.VDTeam;
import anthony.villagerdefense.items.GrapplingHookItem;
import anthony.villagerdefense.items.TeleportBowItem;
import anthony.villagerdefense.items.ThrowableTntItem;
import anthony.villagerdefense.resources.VDResourceType;
import anthony.villagerdefense.villager.VillagerLevel;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.List;

/**
 * Static catalog data for VDShopGUI, split into tabs the same way Hypixel's
 * own Bedwars shop is (Blocks, Melee, Armor, Tools, Enchantments, Potions,
 * Utility), plus a curated Quick Buy tab. The Upgrades tab is generated
 * separately from VDTeamUpgrade since those entries are tiered rather than
 * flat purchases.
 */
public final class VDShopCatalog {

	private VDShopCatalog() {
	}

	public static List<VDShopItem> blocks(VDTeam team) {
		return Arrays.asList(
				new VDShopItem("Wool", new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Ladder", new ItemStack(Material.LADDER, 16),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Planks", new ItemStack(Material.WOOD, 16),
						VDResourceType.IRON, 6, VillagerLevel.LEVEL_1),
				new VDShopItem("Glass", new ItemStack(Material.GLASS, 8),
						VDResourceType.GOLD, 5, VillagerLevel.LEVEL_1),
				new VDShopItem("End Stone", new ItemStack(Material.ENDER_STONE, 8),
						VDResourceType.IRON, 12, VillagerLevel.LEVEL_1),
				new VDShopItem("Hardened Clay", new ItemStack(Material.STAINED_CLAY, 16),
						VDResourceType.GOLD, 16, VillagerLevel.LEVEL_2),
				new VDShopItem("Obsidian", new ItemStack(Material.OBSIDIAN, 4),
						VDResourceType.EMERALD, 4, VillagerLevel.LEVEL_2)
		);
	}

	public static List<VDShopItem> melee() {
		return Arrays.asList(
				new VDShopItem("Wood Sword", new ItemStack(Material.WOOD_SWORD),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Stone Sword", new ItemStack(Material.STONE_SWORD),
						VDResourceType.IRON, 10, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Sword", new ItemStack(Material.IRON_SWORD),
						VDResourceType.GOLD, 7, VillagerLevel.LEVEL_1),
				new VDShopItem("Diamond Sword", new ItemStack(Material.DIAMOND_SWORD),
						VDResourceType.EMERALD, 4, VillagerLevel.LEVEL_2),
				new VDShopItem("Knockback Stick", ItemHelper.addEnchant(new ItemStack(Material.STICK), Enchantment.KNOCKBACK, 1),
						VDResourceType.GOLD, 5, VillagerLevel.LEVEL_2)
		);
	}

	/** Full armor sets at four tiers - Chainmail (cheap starter), Iron, Gold, Diamond. */
	public static List<VDShopItem> armor() {
		return Arrays.asList(
				new VDShopItem("Chainmail Helmet", new ItemStack(Material.CHAINMAIL_HELMET),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Chainmail Chestplate", new ItemStack(Material.CHAINMAIL_CHESTPLATE),
						VDResourceType.IRON, 8, VillagerLevel.LEVEL_1),
				new VDShopItem("Chainmail Leggings", new ItemStack(Material.CHAINMAIL_LEGGINGS),
						VDResourceType.IRON, 6, VillagerLevel.LEVEL_1),
				new VDShopItem("Chainmail Boots", new ItemStack(Material.CHAINMAIL_BOOTS),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),

				new VDShopItem("Iron Helmet", new ItemStack(Material.IRON_HELMET),
						VDResourceType.GOLD, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Chestplate", new ItemStack(Material.IRON_CHESTPLATE),
						VDResourceType.GOLD, 6, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Leggings", new ItemStack(Material.IRON_LEGGINGS),
						VDResourceType.GOLD, 5, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Boots", new ItemStack(Material.IRON_BOOTS),
						VDResourceType.GOLD, 3, VillagerLevel.LEVEL_1),

				new VDShopItem("Gold Helmet", new ItemStack(Material.GOLD_HELMET),
						VDResourceType.EMERALD, 3, VillagerLevel.LEVEL_2),
				new VDShopItem("Gold Chestplate", new ItemStack(Material.GOLD_CHESTPLATE),
						VDResourceType.EMERALD, 4, VillagerLevel.LEVEL_2),
				new VDShopItem("Gold Leggings", new ItemStack(Material.GOLD_LEGGINGS),
						VDResourceType.EMERALD, 3, VillagerLevel.LEVEL_2),
				new VDShopItem("Gold Boots", new ItemStack(Material.GOLD_BOOTS),
						VDResourceType.EMERALD, 2, VillagerLevel.LEVEL_2),

				new VDShopItem("Diamond Helmet", new ItemStack(Material.DIAMOND_HELMET),
						VDResourceType.EMERALD, 4, VillagerLevel.LEVEL_3),
				new VDShopItem("Diamond Chestplate", new ItemStack(Material.DIAMOND_CHESTPLATE),
						VDResourceType.EMERALD, 6, VillagerLevel.LEVEL_3),
				new VDShopItem("Diamond Leggings", new ItemStack(Material.DIAMOND_LEGGINGS),
						VDResourceType.EMERALD, 5, VillagerLevel.LEVEL_3),
				new VDShopItem("Diamond Boots", new ItemStack(Material.DIAMOND_BOOTS),
						VDResourceType.EMERALD, 3, VillagerLevel.LEVEL_3)
		);
	}

	public static List<VDShopItem> tools() {
		return Arrays.asList(
				new VDShopItem("Wood Pickaxe", new ItemStack(Material.WOOD_PICKAXE),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Axe", new ItemStack(Material.WOOD_AXE),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Shears", new ItemStack(Material.SHEARS),
						VDResourceType.IRON, 2, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Pickaxe", new ItemStack(Material.IRON_PICKAXE),
						VDResourceType.IRON, 8, VillagerLevel.LEVEL_2),
				new VDShopItem("Iron Axe", new ItemStack(Material.IRON_AXE),
						VDResourceType.IRON, 8, VillagerLevel.LEVEL_2),
				new VDShopItem("Diamond Pickaxe", new ItemStack(Material.DIAMOND_PICKAXE),
						VDResourceType.EMERALD, 4, VillagerLevel.LEVEL_3)
		);
	}

	/**
	 * Pre-enchanted gear rather than books-to-combine (no anvil GUI on 1.8
	 * without extra plumbing) - click and the enchanted item lands straight
	 * in your inventory.
	 */
	public static List<VDShopItem> enchantments() {
		return Arrays.asList(
				new VDShopItem("Power I Bow", ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1),
						VDResourceType.GOLD, 8, VillagerLevel.LEVEL_2),
				new VDShopItem("Punch I Bow", ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_KNOCKBACK, 1),
						VDResourceType.GOLD, 8, VillagerLevel.LEVEL_2),
				new VDShopItem("Efficiency II Pickaxe", ItemHelper.addEnchant(new ItemStack(Material.IRON_PICKAXE), Enchantment.DIG_SPEED, 2),
						VDResourceType.GOLD, 6, VillagerLevel.LEVEL_1),
				new VDShopItem("Feather Falling II Boots", ItemHelper.addEnchant(new ItemStack(Material.IRON_BOOTS), Enchantment.PROTECTION_FALL, 2),
						VDResourceType.GOLD, 10, VillagerLevel.LEVEL_2)
		);
	}

	public static List<VDShopItem> potions() {
		return Arrays.asList(
				new VDShopItem("Bread", new ItemStack(Material.BREAD, 5),
						VDResourceType.IRON, 2, VillagerLevel.LEVEL_1),
				new VDShopItem("Golden Apple", new ItemStack(Material.GOLDEN_APPLE),
						VDResourceType.GOLD, 3, VillagerLevel.LEVEL_2),
				new VDShopItem("Healing Potion", ItemHelper.createPotionItem(PotionType.INSTANT_HEAL, 0, 0, true, true, false),
						VDResourceType.EMERALD, 2, VillagerLevel.LEVEL_2),
				new VDShopItem("Speed Potion", ItemHelper.createPotionItem(PotionType.SPEED, 1, 45, true, true, false),
						VDResourceType.EMERALD, 1, VillagerLevel.LEVEL_2),
				new VDShopItem("Jump Boost Potion", ItemHelper.createPotionItem(PotionType.JUMP, 0, 45, true, true, false),
						VDResourceType.GOLD, 2, VillagerLevel.LEVEL_2),
				new VDShopItem("Fire Resistance Potion", ItemHelper.createPotionItem(PotionType.FIRE_RESISTANCE, 0, 60, true, true, false),
						VDResourceType.EMERALD, 1, VillagerLevel.LEVEL_2),
				new VDShopItem("Invisibility Potion", ItemHelper.createPotionItem(PotionType.INVISIBILITY, 0, 30, true, true, false),
						VDResourceType.EMERALD, 1, VillagerLevel.LEVEL_3)
		);
	}

	public static List<VDShopItem> utility() {
		return Arrays.asList(
				new VDShopItem("Bow", new ItemStack(Material.BOW),
						VDResourceType.GOLD, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Arrow", new ItemStack(Material.ARROW, 8),
						VDResourceType.IRON, 2, VillagerLevel.LEVEL_1),
				new VDShopItem("Ender Pearl", new ItemStack(Material.ENDER_PEARL),
						VDResourceType.GOLD, 4, VillagerLevel.LEVEL_2),
				new VDShopItem(GrapplingHookItem.NAME, GrapplingHookItem.create(),
						VDResourceType.GOLD, 10, VillagerLevel.LEVEL_2),
				new VDShopItem(ThrowableTntItem.NAME, ThrowableTntItem.create(),
						VDResourceType.GOLD, 6, VillagerLevel.LEVEL_2),
				new VDShopItem(TeleportBowItem.NAME, TeleportBowItem.create(),
						VDResourceType.DIAMOND, 12, VillagerLevel.LEVEL_3)
		);
	}

	/** A handful of the most commonly-bought items, shortcut tab like Hypixel's "Quick Buy". */
	public static List<VDShopItem> quickBuy(VDTeam team) {
		return Arrays.asList(
				new VDShopItem("Wool", new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Sword", new ItemStack(Material.WOOD_SWORD),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Sword", new ItemStack(Material.IRON_SWORD),
						VDResourceType.GOLD, 7, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Chestplate", new ItemStack(Material.IRON_CHESTPLATE),
						VDResourceType.GOLD, 6, VillagerLevel.LEVEL_1),
				new VDShopItem("Iron Boots", new ItemStack(Material.IRON_BOOTS),
						VDResourceType.GOLD, 3, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Pickaxe", new ItemStack(Material.WOOD_PICKAXE),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Wood Axe", new ItemStack(Material.WOOD_AXE),
						VDResourceType.IRON, 4, VillagerLevel.LEVEL_1),
				new VDShopItem("Bread", new ItemStack(Material.BREAD, 5),
						VDResourceType.IRON, 2, VillagerLevel.LEVEL_1)
		);
	}
}
