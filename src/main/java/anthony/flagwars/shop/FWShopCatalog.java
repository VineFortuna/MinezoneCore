package anthony.flagwars.shop;

import anthony.util.ItemHelper;
import anthony.flagwars.FWTeam;
import anthony.flagwars.items.BridgeEggItem;
import anthony.flagwars.items.FreezeRayItem;
import anthony.flagwars.items.GrapplingHookItem;
import anthony.flagwars.items.TeleportBowItem;
import anthony.flagwars.items.ThrowableTntItem;
import anthony.flagwars.items.UnstableBridgeItem;
import anthony.flagwars.resources.FWResourceType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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
						FWResourceType.IRON, 4).withDescription("Cheap and fast bridge material"),
				new FWShopItem("Hardened Clay", 2, 2, new ItemStack(Material.STAINED_CLAY, 13),
						FWResourceType.IRON, 12).withDescription("Tougher than Wool. Harder to break"),
				new FWShopItem("End Stone", 2, 3, new ItemStack(Material.ENDER_STONE, 9),
						FWResourceType.IRON, 24).withDescription("Blast-resistant building block"),
				new FWShopItem("Blast-Proof Glass", 2, 4, new ItemStack(Material.STAINED_GLASS, 4, team.getColor().getWoolData()),
						FWResourceType.IRON, 12).withDescription("Impervious to explosions"),
				new FWShopItem("Wood Planks", 2, 5, new ItemStack(Material.WOOD, 16),
						FWResourceType.GOLD, 4).withDescription("Sturdy mid-tier bridge block"),
				new FWShopItem("Ladder", 2, 6, new ItemStack(Material.LADDER, 16),
						FWResourceType.IRON, 4).withDescription("Climb walls and structures"),
				new FWShopItem("Obsidian", 2, 7, new ItemStack(Material.OBSIDIAN, 2),
						FWResourceType.EMERALD, 2).withDescription("Nearly indestructible. Protects your flag"),
				new FWShopItem("Packed Ice", 2, 8, new ItemStack(Material.PACKED_ICE, 8),
						FWResourceType.IRON, 8).withDescription("Slippery bridge block")
		);
	}

	public static List<FWShopItem> melee() {
		return Arrays.asList(
				new FWShopItem("Stone Sword", 2, 2, new ItemStack(Material.STONE_SWORD),
						FWResourceType.IRON, 10).withDescription("Basic combat blade"),
				new FWShopItem("Iron Sword", 2, 3, new ItemStack(Material.IRON_SWORD),
						FWResourceType.GOLD, 7).withDescription("A stronger mid-game weapon"),
				new FWShopItem("Diamond Sword", 2, 4, new ItemStack(Material.DIAMOND_SWORD),
						FWResourceType.EMERALD, 4).withDescription("The strongest sword available"),
				new FWShopItem("Knockback Stick", 2, 5, ItemHelper.addEnchant(new ItemStack(Material.STICK), Enchantment.KNOCKBACK, 1),
						FWResourceType.GOLD, 5).withDescription("Send enemies flying off the edge")
		);
	}

	/**
	 * Each tier is one purchase that equips its pieces at once, same as Hypixel - no per-piece
	 * buying. Hypixel Bedwars armor is leggings + boots only (the helmet/chestplate slots just
	 * keep the team-colored leather everyone starts with, forever), so that's all these give.
	 */
	public static List<FWShopItem> armor() {
		return Arrays.asList(
				FWShopItem.armorSet("Chainmail Armor", 2, 2, FWResourceType.IRON, 24,
						new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.CHAINMAIL_BOOTS))
						.withDescription("Basic protection for your legs and feet"),
				FWShopItem.armorSet("Iron Armor", 2, 4, FWResourceType.GOLD, 12,
						new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS))
						.withDescription("Solid mid-tier armor"),
				FWShopItem.armorSet("Diamond Armor", 2, 6, FWResourceType.EMERALD, 6,
						new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_BOOTS))
						.withDescription("Maximum protection")
		);
	}

	/**
	 * Pickaxe tier is progressive like Hypixel Bedwars: Wood (10 iron) → Stone (15 iron) → Iron (3 gold) →
	 * Diamond (6 gold). The shop only shows the next tier to buy based on what the player currently
	 * holds; at diamond (max) the slot is empty. All tiers have Efficiency I.
	 *
	 * Axe tier is the same idea, four tiers deep: Wood (10 iron, Efficiency I) → Stone (10 iron,
	 * Efficiency I) → Iron (3 gold, Efficiency II) → Diamond (6 gold, Efficiency III).
	 */
	public static List<FWShopItem> tools(Player player) {
		List<FWShopItem> items = new ArrayList<>();

		int tier = pickaxeTierOf(player);
		if (tier == 0) {
			items.add(new FWShopItem("Wood Pickaxe", 2, 1,
					ItemHelper.addEnchant(new ItemStack(Material.WOOD_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 10).withDescription("Mine blocks from enemy bases"));
		} else if (tier == 1) {
			items.add(new FWShopItem("Stone Pickaxe", 2, 1,
					ItemHelper.addEnchant(new ItemStack(Material.STONE_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 15).withDescription("Faster mining speed"));
		} else if (tier == 2) {
			items.add(new FWShopItem("Iron Pickaxe", 2, 2,
					ItemHelper.addEnchant(new ItemStack(Material.IRON_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.GOLD, 3).withDescription("Even faster mining speed"));
		} else if (tier == 3) {
			items.add(new FWShopItem("Diamond Pickaxe", 2, 3,
					ItemHelper.addEnchant(new ItemStack(Material.DIAMOND_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.GOLD, 6).withDescription("The fastest pickaxe available"));
		}
		// tier 4 = diamond already owned, nothing more to show

		int axeTier = axeTierOf(player);
		if (axeTier == 0) {
			items.add(new FWShopItem("Wood Axe", 2, 4,
					ItemHelper.addEnchant(new ItemStack(Material.WOOD_AXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 10).withDescription("Basic combat axe"));
		} else if (axeTier == 1) {
			items.add(new FWShopItem("Stone Axe", 2, 4,
					ItemHelper.addEnchant(new ItemStack(Material.STONE_AXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 10).withDescription("Sturdier combat axe"));
		} else if (axeTier == 2) {
			items.add(new FWShopItem("Iron Axe", 2, 4,
					ItemHelper.addEnchant(new ItemStack(Material.IRON_AXE), Enchantment.DIG_SPEED, 2),
					FWResourceType.GOLD, 3).withDescription("Stronger combat axe"));
		} else if (axeTier == 3) {
			items.add(new FWShopItem("Diamond Axe", 2, 4,
					ItemHelper.addEnchant(new ItemStack(Material.DIAMOND_AXE), Enchantment.DIG_SPEED, 3),
					FWResourceType.GOLD, 6).withDescription("The strongest combat axe available"));
		}
		// tier 4 = diamond already owned, nothing more to show

		items.add(new FWShopItem("Shears", 2, 5, new ItemStack(Material.SHEARS), FWResourceType.IRON, 20).withDescription("Instantly break Wool blocks"));
		return items;
	}

	/** 0=none, 1=wood, 2=stone, 3=iron, 4=diamond. */
	private static int pickaxeTierOf(Player player) {
		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null) continue;
			if (item.getType() == Material.DIAMOND_PICKAXE) return 4;
			if (item.getType() == Material.IRON_PICKAXE) return 3;
			if (item.getType() == Material.STONE_PICKAXE) return 2;
			if (item.getType() == Material.WOOD_PICKAXE) return 1;
		}
		return 0;
	}

	/** 0=none, 1=wood, 2=stone, 3=iron, 4=diamond. */
	private static int axeTierOf(Player player) {
		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null) continue;
			if (item.getType() == Material.DIAMOND_AXE) return 4;
			if (item.getType() == Material.IRON_AXE) return 3;
			if (item.getType() == Material.STONE_AXE) return 2;
			if (item.getType() == Material.WOOD_AXE) return 1;
		}
		return 0;
	}

	/** Pre-enchanted bows rather than books-to-combine (no anvil GUI on 1.8 without extra plumbing). */
	public static List<FWShopItem> bowsAndArrows() {
		return Arrays.asList(
				new FWShopItem("Arrow", 2, 2, new ItemStack(Material.ARROW, 8),
						FWResourceType.GOLD, 2).withDescription("Ammunition for your bow"),
				new FWShopItem("Bow", 2, 3, new ItemStack(Material.BOW),
						FWResourceType.GOLD, 12).withDescription("Standard ranged weapon"),
				new FWShopItem("Power Bow", 2, 4, ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1),
						FWResourceType.GOLD, 20).withDescription("Deals extra arrow damage"),
				new FWShopItem("Punch Bow", 2, 5,
						ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1), Enchantment.ARROW_KNOCKBACK, 1),
						FWResourceType.EMERALD, 6).withDescription("Extra damage and knockback on hit")
		);
	}

	public static List<FWShopItem> potions() {
		return Arrays.asList(
				new FWShopItem("Speed Potion", 2, 3, ItemHelper.hidePotionEffectLore(ItemHelper.createPotionItem(PotionType.SPEED, 1, 45, true, true, false)),
						FWResourceType.EMERALD, 1).withDescription("Move faster for 45 seconds"),
				new FWShopItem("Jump Potion", 2, 4, ItemHelper.hidePotionEffectLore(ItemHelper.createPotionItem(PotionType.JUMP, 4, 45, true, true, false)),
						FWResourceType.EMERALD, 1).withDescription("Jump higher for 45 seconds"),
				new FWShopItem("Invisibility Potion", 2, 5, ItemHelper.hidePotionEffectLore(ItemHelper.createPotionItem(PotionType.INVISIBILITY, 0, 30, true, true, false)),
						FWResourceType.EMERALD, 2).withDescription("Turn invisible for 30 seconds")
		);
	}

	public static List<FWShopItem> utility() {
		return Arrays.asList(
				new FWShopItem("Golden Apple", 2, 1, new ItemStack(Material.GOLDEN_APPLE),
						FWResourceType.GOLD, 3).withDescription("Instantly restore health"),
				new FWShopItem("Water Bucket", 2, 2, new ItemStack(Material.WATER_BUCKET),
						FWResourceType.GOLD, 2).withDescription("Block fire or break long falls"),
				new FWShopItem("Fire Charge", 2, 3, new ItemStack(Material.FIREBALL),
						FWResourceType.IRON, 35).withDescription("Launch a fireball at enemies"),
				new FWShopItem("TNT", 2, 4, new ItemStack(Material.TNT),
						FWResourceType.GOLD, 4).withDescription("Explosive block. Damages nearby players"),
				new FWShopItem("Ender Pearl", 2, 5, new ItemStack(Material.ENDER_PEARL),
						FWResourceType.EMERALD, 4).withDescription("Teleport to where it lands"),
				new FWShopItem(GrapplingHookItem.NAME, 2, 6, GrapplingHookItem.create(),
						FWResourceType.GOLD, 12).withDescription("Swing and grapple across the map"),
				new FWShopItem(ThrowableTntItem.NAME, 2, 7, ThrowableTntItem.create(),
						FWResourceType.GOLD, 6).withDescription("Toss an explosive at enemies"),
				new FWShopItem(BridgeEggItem.NAME, 3, 3, BridgeEggItem.create(),
						FWResourceType.EMERALD, 1).withDescription("Throw it to bridge a gap with your team's colored wool")
						.withStat("&rBridge: &bUp to 20 blocks, 3 blocks wide"),
				new FWShopItem(TeleportBowItem.NAME, 3, 4, TeleportBowItem.create(),
						FWResourceType.EMERALD, 4).withDescription("Teleport to where the arrow lands"),
				new FWShopItem(FreezeRayItem.NAME, 3, 5, FreezeRayItem.create(),
						FWResourceType.IRON, 30).withDescription("Freeze enemies in place")
						.withStat("&rRange: &a10 blocks"),
				new FWShopItem(UnstableBridgeItem.NAME, 3, 6, UnstableBridgeItem.create(),
						FWResourceType.GOLD, 6).withDescription("Gradually build a bridge that collapses 5s after it finishes")
						.withStat("&rSize: &e3x10 blocks")
		);
	}

	/** A handful of the most commonly-bought items, shortcut tab like Hypixel's "Quick Buy". */
	public static List<FWShopItem> quickBuy(FWTeam team) {
		return Arrays.asList(
				new FWShopItem("Wool", 2, 1, new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						FWResourceType.IRON, 4).withDescription("Cheap and fast bridge material"),
				new FWShopItem("Stone Sword", 2, 2, new ItemStack(Material.STONE_SWORD),
						FWResourceType.IRON, 10).withDescription("Basic combat blade"),
				new FWShopItem("Bow", 2, 3, new ItemStack(Material.BOW),
						FWResourceType.GOLD, 12).withDescription("Standard ranged weapon"),
				FWShopItem.armorSet("Iron Armor", 2, 4, FWResourceType.GOLD, 12,
						new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS))
						.withDescription("Solid mid-tier armor"),
				new FWShopItem("Golden Apple", 2, 5, new ItemStack(Material.GOLDEN_APPLE),
						FWResourceType.GOLD, 3).withDescription("Instantly restore health"),
				new FWShopItem("Ender Pearl", 2, 6, new ItemStack(Material.ENDER_PEARL),
						FWResourceType.EMERALD, 4).withDescription("Teleport to where it lands")
		);
	}

	/** Every purchasable catalog entry across all tabs (not Quick Buy's own curated list) - used to look up a player's shift-clicked Quick Buy additions by name. */
	public static List<FWShopItem> allItems(FWTeam team, Player player) {
		List<FWShopItem> all = new ArrayList<>();
		all.addAll(blocks(team));
		all.addAll(melee());
		all.addAll(armor());
		all.addAll(tools(player));
		all.addAll(bowsAndArrows());
		all.addAll(potions());
		all.addAll(utility());
		return all;
	}
}
