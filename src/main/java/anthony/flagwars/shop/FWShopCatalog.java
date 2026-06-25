package anthony.flagwars.shop;

import anthony.util.ItemHelper;
import anthony.flagwars.FWTeam;
import anthony.flagwars.items.GrapplingHookItem;
import anthony.flagwars.items.TeleportBowItem;
import anthony.flagwars.items.ThrowableTntItem;
import anthony.flagwars.resources.FWResourceType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static catalog data for FWShopGUI - a 1-1 recreation of Hypixel's own
 * Bedwars Item Shop: same tabs (Quick Buy / Blocks / Melee / Armor / Tools /
 * Bows &amp; Arrows / Potions / Utility), same items, same prices, and each
 * item pinned to the same (row, col) slot Hypixel puts it in. No level
 * gating - every item here is purchasable the moment you can afford it.
 * The Upgrades tab is separate (see FWTeamUpgrade) since that's Hypixel's
 * own Team Upgrades NPC bolted onto this same GUI rather than a shop tab.
 * Grappling Hook / Throwable TNT / Teleport Bow are Flag Wars originals
 * (not on Hypixel) kept in Utility since they're already wired into the game.
 */
public final class FWShopCatalog {

	private FWShopCatalog() {
	}

	public static List<FWShopItem> blocks(FWTeam team) {
		return Arrays.asList(
				new FWShopItem("Wool", 2, 1, new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						FWResourceType.IRON, 4),
				new FWShopItem("Hardened Clay", 2, 2, new ItemStack(Material.STAINED_CLAY, 16),
						FWResourceType.IRON, 16),
				new FWShopItem("End Stone", 2, 3, new ItemStack(Material.ENDER_STONE, 8),
						FWResourceType.IRON, 24),
				new FWShopItem("Blast-Proof Glass", 2, 4, new ItemStack(Material.GLASS, 4),
						FWResourceType.IRON, 12),
				new FWShopItem("Wood Planks", 2, 5, new ItemStack(Material.WOOD, 16),
						FWResourceType.GOLD, 4),
				new FWShopItem("Ladder", 2, 6, new ItemStack(Material.LADDER, 16),
						FWResourceType.IRON, 4),
				new FWShopItem("Obsidian", 2, 7, new ItemStack(Material.OBSIDIAN, 4),
						FWResourceType.EMERALD, 4)
		);
	}

	public static List<FWShopItem> melee() {
		return Arrays.asList(
				new FWShopItem("Stone Sword", 2, 2, new ItemStack(Material.STONE_SWORD),
						FWResourceType.IRON, 10),
				new FWShopItem("Iron Sword", 2, 3, new ItemStack(Material.IRON_SWORD),
						FWResourceType.GOLD, 7),
				new FWShopItem("Diamond Sword", 2, 4, new ItemStack(Material.DIAMOND_SWORD),
						FWResourceType.EMERALD, 4),
				new FWShopItem("Knockback Stick", 2, 5, ItemHelper.addEnchant(new ItemStack(Material.STICK), Enchantment.KNOCKBACK, 1),
						FWResourceType.GOLD, 5)
		);
	}

	/**
	 * Each tier is one purchase that equips its pieces at once, same as Hypixel - no per-piece
	 * buying. Hypixel Bedwars armor is leggings + boots only (the helmet/chestplate slots just
	 * keep the team-colored leather everyone starts with, forever), so that's all these give.
	 */
	public static List<FWShopItem> armor() {
		return Arrays.asList(
				FWShopItem.armorSet("Chainmail Armor", 2, 2, FWResourceType.IRON, 32,
						new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.CHAINMAIL_BOOTS)),
				FWShopItem.armorSet("Iron Armor", 2, 4, FWResourceType.GOLD, 12,
						new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS)),
				FWShopItem.armorSet("Diamond Armor", 2, 6, FWResourceType.EMERALD, 6,
						new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_BOOTS))
		);
	}

	public static List<FWShopItem> tools() {
		return Arrays.asList(
				new FWShopItem("Wood Pickaxe", 2, 1, new ItemStack(Material.WOOD_PICKAXE),
						FWResourceType.IRON, 10),
				new FWShopItem("Iron Pickaxe", 2, 2, new ItemStack(Material.IRON_PICKAXE),
						FWResourceType.GOLD, 8),
				new FWShopItem("Wood Axe", 2, 3, new ItemStack(Material.WOOD_AXE),
						FWResourceType.IRON, 10),
				new FWShopItem("Iron Axe", 2, 4, new ItemStack(Material.IRON_AXE),
						FWResourceType.GOLD, 8),
				new FWShopItem("Shears", 2, 5, new ItemStack(Material.SHEARS),
						FWResourceType.IRON, 20)
		);
	}

	/** Pre-enchanted bows rather than books-to-combine (no anvil GUI on 1.8 without extra plumbing). */
	public static List<FWShopItem> bowsAndArrows() {
		return Arrays.asList(
				new FWShopItem("Arrow", 2, 2, new ItemStack(Material.ARROW, 8),
						FWResourceType.GOLD, 2),
				new FWShopItem("Bow", 2, 3, new ItemStack(Material.BOW),
						FWResourceType.GOLD, 12),
				new FWShopItem("Power Bow", 2, 4, ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1),
						FWResourceType.GOLD, 20),
				new FWShopItem("Punch Bow", 2, 5,
						ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1), Enchantment.ARROW_KNOCKBACK, 1),
						FWResourceType.EMERALD, 6)
		);
	}

	public static List<FWShopItem> potions() {
		return Arrays.asList(
				new FWShopItem("Speed Potion", 2, 3, ItemHelper.createPotionItem(PotionType.SPEED, 1, 45, true, true, false),
						FWResourceType.EMERALD, 1),
				new FWShopItem("Jump Potion", 2, 4, ItemHelper.createPotionItem(PotionType.JUMP, 1, 45, true, true, false),
						FWResourceType.EMERALD, 1),
				new FWShopItem("Invisibility Potion", 2, 5, ItemHelper.createPotionItem(PotionType.INVISIBILITY, 0, 30, true, true, false),
						FWResourceType.EMERALD, 2)
		);
	}

	public static List<FWShopItem> utility() {
		return Arrays.asList(
				new FWShopItem("Golden Apple", 2, 1, new ItemStack(Material.GOLDEN_APPLE),
						FWResourceType.GOLD, 3),
				new FWShopItem("Water Bucket", 2, 2, new ItemStack(Material.WATER_BUCKET),
						FWResourceType.GOLD, 4),
				new FWShopItem("Fire Charge", 2, 3, new ItemStack(Material.FIREBALL),
						FWResourceType.IRON, 35),
				new FWShopItem("TNT", 2, 4, new ItemStack(Material.TNT),
						FWResourceType.GOLD, 4),
				new FWShopItem("Ender Pearl", 2, 5, new ItemStack(Material.ENDER_PEARL),
						FWResourceType.EMERALD, 4),
				new FWShopItem(GrapplingHookItem.NAME, 2, 6, GrapplingHookItem.create(),
						FWResourceType.GOLD, 10),
				new FWShopItem(ThrowableTntItem.NAME, 2, 7, ThrowableTntItem.create(),
						FWResourceType.GOLD, 6),
				new FWShopItem(TeleportBowItem.NAME, 3, 4, TeleportBowItem.create(),
						FWResourceType.DIAMOND, 12)
		);
	}

	/** A handful of the most commonly-bought items, shortcut tab like Hypixel's "Quick Buy". */
	public static List<FWShopItem> quickBuy(FWTeam team) {
		return Arrays.asList(
				new FWShopItem("Wool", 2, 1, new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						FWResourceType.IRON, 4),
				new FWShopItem("Stone Sword", 2, 2, new ItemStack(Material.STONE_SWORD),
						FWResourceType.IRON, 10),
				new FWShopItem("Bow", 2, 3, new ItemStack(Material.BOW),
						FWResourceType.GOLD, 12),
				new FWShopItem("Iron Pickaxe", 2, 4, new ItemStack(Material.IRON_PICKAXE),
						FWResourceType.GOLD, 8),
				FWShopItem.armorSet("Iron Armor", 2, 5, FWResourceType.GOLD, 12,
						new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS)),
				new FWShopItem("Golden Apple", 2, 6, new ItemStack(Material.GOLDEN_APPLE),
						FWResourceType.GOLD, 3),
				new FWShopItem("Ender Pearl", 2, 7, new ItemStack(Material.ENDER_PEARL),
						FWResourceType.EMERALD, 4)
		);
	}

	/** Every purchasable catalog entry across all tabs (not Quick Buy's own curated list) - used to look up a player's shift-clicked Quick Buy additions by name. */
	public static List<FWShopItem> allItems(FWTeam team) {
		List<FWShopItem> all = new ArrayList<>();
		all.addAll(blocks(team));
		all.addAll(melee());
		all.addAll(armor());
		all.addAll(tools());
		all.addAll(bowsAndArrows());
		all.addAll(potions());
		all.addAll(utility());
		return all;
	}
}
