package anthony.flagwars.shop;

import anthony.util.ItemHelper;
import anthony.flagwars.FWTeam;
import anthony.flagwars.items.BridgeEggItem;
import anthony.flagwars.items.FreezeRayItem;
import anthony.flagwars.items.GrapplingHookItem;
import anthony.flagwars.items.PokeballItem;
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
 * Static catalog data for FWShopGUI, modelled on Hypixel's Bedwars Item Shop: same tabs (Quick Buy /
 * Blocks / Melee / Armor / Tools / Bows &amp; Arrows / Potions / Utility), same items, same prices.
 * Items have no slot of their own: FWShopGUI sorts each tab (resource, cost, name) and lays it out
 * from the second column. No level gating - every item here is purchasable the moment you can
 * afford it. The Upgrades tab is separate (see FWTeamUpgrade) since that's Hypixel's
 * own Team Upgrades NPC bolted onto this same GUI rather than a shop tab.
 * Grappling Hook / Throwable TNT / Teleport Bow and the Zombie / Skeleton Pokeballs (the same ones Super Craft
 * Bros has) are Flag Wars originals (not on Hypixel) kept in Utility since they're already wired into the game.
 */
public final class FWShopCatalog {

	private FWShopCatalog() {
	}

	public static List<FWShopItem> blocks(FWTeam team) {
		return Arrays.asList(
				new FWShopItem("Wool", new ItemStack(Material.WOOL, 16, team.getColor().getWoolData()),
						FWResourceType.IRON, 4).withDescription("Cheap blocks for bridging."),
				new FWShopItem("Hardened Clay", new ItemStack(Material.STAINED_CLAY, 13),
						FWResourceType.IRON, 12).withDescription("Tougher than wool."),
				new FWShopItem("End Stone", new ItemStack(Material.ENDER_STONE, 9),
						FWResourceType.IRON, 24).withDescription("Explosions can't break it."),
				new FWShopItem("Blast-Proof Glass", new ItemStack(Material.STAINED_GLASS, 4, team.getColor().getWoolData()),
						FWResourceType.IRON, 12).withDescription("Clear blocks that explosions\ncan't break."),
				new FWShopItem("Wood Planks", new ItemStack(Material.WOOD, 16),
						FWResourceType.GOLD, 4).withDescription("Strong blocks. Break them\nfaster with an axe."),
				new FWShopItem("Ladder", new ItemStack(Material.LADDER, 16),
						FWResourceType.IRON, 4).withDescription("Climb up walls."),
				new FWShopItem("Obsidian", new ItemStack(Material.OBSIDIAN, 2),
						FWResourceType.EMERALD, 2).withDescription("Very hard to mine.\nExplosions can't break it."),
				new FWShopItem("Packed Ice", new ItemStack(Material.PACKED_ICE, 8),
						FWResourceType.IRON, 8).withDescription("Slippery blocks for bridging.")
		);
	}

	public static List<FWShopItem> melee() {
		return Arrays.asList(
				new FWShopItem("Stone Sword", new ItemStack(Material.STONE_SWORD),
						FWResourceType.IRON, 10).withDescription("A basic sword."),
				new FWShopItem("Iron Sword", new ItemStack(Material.IRON_SWORD),
						FWResourceType.GOLD, 7).withDescription("A stronger sword."),
				new FWShopItem("Diamond Sword", new ItemStack(Material.DIAMOND_SWORD),
						FWResourceType.EMERALD, 4).withDescription("The strongest sword."),
				new FWShopItem("Knockback Stick", ItemHelper.addEnchant(new ItemStack(Material.STICK), Enchantment.KNOCKBACK, 1),
						FWResourceType.GOLD, 5).withDescription("Knocks enemies back.")
		);
	}

	/**
	 * Each tier is one purchase that equips its pieces at once, same as Hypixel - no per-piece
	 * buying. Hypixel Bedwars armor is leggings + boots only (the helmet/chestplate slots just
	 * keep the team-colored leather everyone starts with, forever), so that's all these give.
	 */
	public static List<FWShopItem> armor() {
		return Arrays.asList(
				FWShopItem.armorSet("Chainmail Armor", FWResourceType.IRON, 24,
						new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.CHAINMAIL_BOOTS))
						.withDescription("Chainmail leggings and boots."),
				FWShopItem.armorSet("Iron Armor", FWResourceType.GOLD, 12,
						new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_BOOTS))
						.withDescription("Iron leggings and boots."),
				FWShopItem.armorSet("Diamond Armor", FWResourceType.EMERALD, 6,
						new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_BOOTS))
						.withDescription("Diamond leggings and boots.")
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
			items.add(new FWShopItem("Wood Pickaxe",
					ItemHelper.addEnchant(new ItemStack(Material.WOOD_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 10).withDescription("Breaks hard blocks faster."));
		} else if (tier == 1) {
			items.add(new FWShopItem("Stone Pickaxe",
					ItemHelper.addEnchant(new ItemStack(Material.STONE_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 15).withDescription("Faster than the wood pickaxe."));
		} else if (tier == 2) {
			items.add(new FWShopItem("Iron Pickaxe",
					ItemHelper.addEnchant(new ItemStack(Material.IRON_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.GOLD, 3).withDescription("Faster than the stone pickaxe."));
		} else if (tier == 3) {
			items.add(new FWShopItem("Diamond Pickaxe",
					ItemHelper.addEnchant(new ItemStack(Material.DIAMOND_PICKAXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.GOLD, 6).withDescription("The fastest pickaxe."));
		}
		// tier 4 = diamond already owned, nothing more to show

		int axeTier = axeTierOf(player);
		if (axeTier == 0) {
			items.add(new FWShopItem("Wood Axe",
					ItemHelper.addEnchant(new ItemStack(Material.WOOD_AXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 10).withDescription("Breaks wood faster."));
		} else if (axeTier == 1) {
			items.add(new FWShopItem("Stone Axe",
					ItemHelper.addEnchant(new ItemStack(Material.STONE_AXE), Enchantment.DIG_SPEED, 1),
					FWResourceType.IRON, 10).withDescription("Faster than the wood axe."));
		} else if (axeTier == 2) {
			items.add(new FWShopItem("Iron Axe",
					ItemHelper.addEnchant(new ItemStack(Material.IRON_AXE), Enchantment.DIG_SPEED, 2),
					FWResourceType.GOLD, 3).withDescription("Faster than the stone axe."));
		} else if (axeTier == 3) {
			items.add(new FWShopItem("Diamond Axe",
					ItemHelper.addEnchant(new ItemStack(Material.DIAMOND_AXE), Enchantment.DIG_SPEED, 3),
					FWResourceType.GOLD, 6).withDescription("The fastest axe."));
		}
		// tier 4 = diamond already owned, nothing more to show

		items.add(new FWShopItem("Shears", new ItemStack(Material.SHEARS), FWResourceType.IRON, 20)
				.withDescription("Breaks wool fast.").permanent());
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
				new FWShopItem("Arrow", new ItemStack(Material.ARROW, 8),
						FWResourceType.GOLD, 2).withDescription("Ammo for your bow."),
				new FWShopItem("Bow", new ItemStack(Material.BOW),
						FWResourceType.GOLD, 12).withDescription("Shoots arrows."),
				new FWShopItem("Power Bow", ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1),
						FWResourceType.GOLD, 20).withDescription("Arrows hit harder."),
				new FWShopItem("Punch Bow",
						ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 1), Enchantment.ARROW_KNOCKBACK, 1),
						FWResourceType.EMERALD, 6).withDescription("Arrows hit harder and knock back.")
		);
	}

	public static List<FWShopItem> potions() {
		return Arrays.asList(
				new FWShopItem("Speed Potion", ItemHelper.hidePotionEffectLore(ItemHelper.createPotionItem(PotionType.SPEED, 1, 45, true, true, false)),
						FWResourceType.EMERALD, 1).withDescription("Speed II for 45 seconds."),
				new FWShopItem("Jump Potion", ItemHelper.hidePotionEffectLore(ItemHelper.createPotionItem(PotionType.JUMP, 4, 45, true, true, false)),
						FWResourceType.EMERALD, 1).withDescription("Jump Boost V for 45 seconds."),
				new FWShopItem("Invisibility Potion", ItemHelper.hidePotionEffectLore(ItemHelper.createPotionItem(PotionType.INVISIBILITY, 0, 30, true, true, false)),
						FWResourceType.EMERALD, 2).withDescription("Invisible for 30 seconds.")
		);
	}

	public static List<FWShopItem> utility() {
		return Arrays.asList(
				new FWShopItem("Golden Apple", new ItemStack(Material.GOLDEN_APPLE),
						FWResourceType.GOLD, 3).withDescription("Heals you and gives extra hearts."),
				new FWShopItem("Water Bucket", new ItemStack(Material.WATER_BUCKET),
						FWResourceType.GOLD, 2).withDescription("Place water to land safely."),
				new FWShopItem("Fire Charge", new ItemStack(Material.FIREBALL),
						FWResourceType.IRON, 35).withDescription("Shoots a fireball that breaks blocks\nand knocks players back."),
				new FWShopItem("TNT", new ItemStack(Material.TNT),
						FWResourceType.GOLD, 4).withDescription("Lights when you place it.\nExplodes after 3 seconds."),
				new FWShopItem("Ender Pearl", new ItemStack(Material.ENDER_PEARL),
						FWResourceType.EMERALD, 4).withDescription("Teleports you to where it lands."),
				new FWShopItem(GrapplingHookItem.NAME, GrapplingHookItem.create(),
						FWResourceType.GOLD, 12).withDescription("Cast it like a fishing rod to pull\nyourself to where it lands.").withStat("&rUses: &a" + GrapplingHookItem.MAX_USES),
				new FWShopItem(ThrowableTntItem.NAME, ThrowableTntItem.create(),
						FWResourceType.GOLD, 6).withDescription("Throw lit TNT.\nExplodes after 3 seconds."),
				new FWShopItem(BridgeEggItem.NAME, BridgeEggItem.create(),
						FWResourceType.EMERALD, 1).withDescription("Throw it to build a bridge of\nyour team's wool.")
						.withStat("&rLength: &a20 blocks").withStat("&rWidth: &a3 blocks"),
				new FWShopItem(TeleportBowItem.NAME, TeleportBowItem.create(),
						FWResourceType.EMERALD, 4).withDescription("Teleports you to where your\narrow lands.").withStat("&rUses: &a1"),
				new FWShopItem(FreezeRayItem.NAME, FreezeRayItem.create(),
						FWResourceType.IRON, 30).withDescription("Slows and damages enemies\nin a straight line.")
						.withStat("&rRange: &a10 blocks"),
				new FWShopItem(UnstableBridgeItem.NAME, UnstableBridgeItem.create(),
						FWResourceType.GOLD, 6).withDescription("Builds a sand bridge that falls\napart 5 seconds after it is done.")
						.withStat("&rSize: &a3x10 blocks"),
				new FWShopItem(PokeballItem.Type.ZOMBIE.getName(), PokeballItem.create(PokeballItem.Type.ZOMBIE),
						FWResourceType.IRON, 20).withDescription(PokeballItem.Type.ZOMBIE.getDescription()),
				new FWShopItem(PokeballItem.Type.SKELETON.getName(), PokeballItem.create(PokeballItem.Type.SKELETON),
						FWResourceType.GOLD, 6).withDescription(PokeballItem.Type.SKELETON.getDescription())
		);
	}

	/**
	 * What a player's Quick Buy starts with, in order. Names only: each one has to match an item
	 * in the tabs above (see allItems), which is where the actual item comes from.
	 */
	public static List<String> defaultQuickBuyNames() {
		return Arrays.asList("Wool", "Stone Sword", "Bow", "Iron Armor", "Golden Apple", "Ender Pearl");
	}

	/** Every purchasable catalog entry across all tabs - used to look up a player's Quick Buy items by name. */
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
