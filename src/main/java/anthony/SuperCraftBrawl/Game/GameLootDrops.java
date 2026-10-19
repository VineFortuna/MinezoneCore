package anthony.SuperCraftBrawl.Game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;

public enum GameLootDrops {

	// COMMON:
	SLOWNESS(Rarity.COMMON), SLOWBALLS(Rarity.COMMON), BLOOPER(Rarity.COMMON), ZOMBIE_EGG(Rarity.COMMON),
	SKELE_EGG(Rarity.COMMON), MILK(Rarity.COMMON), SLIME_EGG(Rarity.COMMON), SILVERFISH_EGG(Rarity.COMMON),

	// UNCOMMON:
	NUKE(Rarity.RARE), SPEED2(Rarity.RARE), INSTAGIB(Rarity.RARE), BAZOOKA(Rarity.RARE),
	WITCH_EGG(Rarity.RARE), CREEPER_EGG(Rarity.RARE), FIRE_FLOWER(Rarity.RARE), CAVE_SPIDER_EGG(Rarity.RARE),
	SNOWBALL_LAUNCHER(Rarity.RARE),

	// RARE:
	HEALTH2(Rarity.MYTHIC), BROOMS(Rarity.MYTHIC), HAMMER(Rarity.MYTHIC), ENDER_PEARL(Rarity.MYTHIC),
	GOLDEN_APPLE(Rarity.MYTHIC),

	// LEGENDARY:
	BOMB(Rarity.LEGENDARY), NOTCH_APPLE(Rarity.LEGENDARY), EXTRA_LIFE(Rarity.LEGENDARY);

	// Iron horse armor has no durability bar, so the Snowball Launcher tracks its uses in a lore line
	public static final int SNOWBALL_LAUNCHER_USES = 3;
	public static final String SNOWBALL_LAUNCHER_USES_PREFIX = "&7Uses left: &e";

	private static final Random RANDOM = new Random();

	private Rarity r;

	GameLootDrops(Rarity r) {
		this.r = r;
	}

	public Rarity getRarity() {
		return this.r;
	}

	/*
	 * A random lightning drop out of the enabled ones (all of them when none are enabled). A rarity is rolled
	 * first, out of the rarities that have an enabled item, so disabling a whole rarity doesn't stop drops.
	 * OP drops only give Rare and Legendary items (no Extra Lives), or any enabled item if none of those are enabled.
	 */
	public static GameLootDrops getDrop(Set<GameLootDrops> enabled, boolean op) {
		if (enabled.isEmpty()) enabled = EnumSet.allOf(GameLootDrops.class);

		if (op) {
			GameLootDrops drop = rollDrop(enabled, true);
			if (drop != null) return drop;
		}
		return rollDrop(enabled, false);
	}

	private static GameLootDrops rollDrop(Set<GameLootDrops> enabled, boolean op) {
		Map<Rarity, List<GameLootDrops>> byRarity = new EnumMap<>(Rarity.class);
		for (GameLootDrops drop : enabled) {
			if (drop.r.getChance(op) == 0 || (op && drop == EXTRA_LIFE)) continue;
			byRarity.computeIfAbsent(drop.r, k -> new ArrayList<>()).add(drop);
		}

		int total = 0;
		for (Rarity rarity : byRarity.keySet()) total += rarity.getChance(op);
		if (total == 0) return null;

		int roll = RANDOM.nextInt(total);
		for (Map.Entry<Rarity, List<GameLootDrops>> entry : byRarity.entrySet()) {
			roll -= entry.getKey().getChance(op);
			if (roll < 0) {
				List<GameLootDrops> drops = entry.getValue();
				return drops.get(RANDOM.nextInt(drops.size()));
			}
		}
		return null;
	}

    public static boolean isLootDropItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;

        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        for (GameLootDrops drop : GameLootDrops.values()) {
            ItemStack dropItem = drop.getItem();

            if (dropItem == null) continue;
            if (!dropItem.hasItemMeta() || !dropItem.getItemMeta().hasDisplayName()) continue;

            String dropName = ChatColor.stripColor(dropItem.getItemMeta().getDisplayName());

            if (item.getType() == dropItem.getType()
                    && item.getDurability() == dropItem.getDurability()
                    && itemName.equalsIgnoreCase(dropName)) {
                return true;
            }
        }

        return false;
    }

	public ItemStack getItem() {
	    ItemStack item = null;

	    switch (this) {
	        // ===== COMMON =====
	        case SLOWNESS:
	            item = ItemHelper.createPotionItem(PotionType.SLOWNESS, 1, 15, true, true, true);
	            ItemHelper.setDetails(item, "&8&lSLOWNESS II &7(15 sec)", "", "&7&lCOMMON");
	            break;

	        case SLOWBALLS:
	            item = ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL, 8),
	                    "&f&lSLOWBALLS",
	                    "&7Give Slowness 1 for 3s to an enemy", "", "&7&lCOMMON");
	            break;

	        case BLOOPER:
	            item = ItemHelper.setDetails(new ItemStack(Material.RABBIT_FOOT),
	                    "&e&lBLOOPER",
	                    "&7Give Blindness or Nausea to an enemy", "", "&7&lCOMMON");
	            break;

	        case ZOMBIE_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.ZOMBIE, 1);
	            ItemHelper.setDetails(item, "&2&lZOMBIE POKEBALL", "&7Spawns an armored Zombie", "", "&7&lCOMMON");
	            break;

	        case SKELE_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.SKELETON, 1);
	            ItemHelper.setDetails(item, "&7&lSKELETON POKEBALL", "&7Spawns a Skeleton with Punch 2", "", "&7&lCOMMON");
	            break;

	        case MILK:
	            item = ItemHelper.setDetails(new ItemStack(Material.MILK_BUCKET),
	                    "&f&lMILK",
	                    "&7Removes fire and all negative effects", "", "&7&lCOMMON");
	            break;
	            
	        case SLIME_EGG:
	        	item = ItemHelper.createMonsterEgg(EntityType.SLIME, 1);
	            ItemHelper.setDetails(item, "&2&lSLIME POKEBALL", "&7Spawns a big Slime", "", "&7&lCOMMON");
	            break;

			case SILVERFISH_EGG:
				item = ItemHelper.createMonsterEgg(EntityType.SILVERFISH, 1);
				ItemHelper.setDetails(item, "&7&lSILVERFISH POKEBALL", "&7Spawns 3 Silverfish", "", "&7&lCOMMON");
				break;

	        // ===== UNCOMMON =====
			case NUKE:
				item = ItemHelper.setDetails(new ItemStack(Material.TNT, 3),
						"&4&lNUKE",
						"&7Spawn TNTs where you're looking",
						"",
						"&2&lUNCOMMON");
				ItemHelper.setGlowing(item, true);
				break;

	        case SPEED2:
	            item = ItemHelper.createPotionItem(PotionType.SPEED, 1, 30, true, true, true);
	            ItemHelper.setDetails(item, "&b&lSPEED II &7(30 sec)", "", "&2&lUNCOMMON");
	            break;

	        case INSTAGIB:
	            item = ItemHelper.setDetails(new ItemStack(Material.GOLD_HOE, 5, (short) 1),
	                    "&e&lINSTAGIB",
	                    "&7Do damage and send your enemies to the sky!", "", "&2&lUNCOMMON");
	            break;

	        case BAZOOKA:
	            item = ItemHelper.setDetails(new ItemStack(Material.DIAMOND_HOE, 3, (short) 1),
	                    "&b&lBAZOOKA",
	                    "&7Explode the area it lands", "", "&2&lUNCOMMON");
	            ItemHelper.setHideFlags(item, true);
	            break;

	        case WITCH_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.WITCH, 1);
	            ItemHelper.setDetails(item, "&5&lWITCH POKEBALL", "&7Spawns a Witch to help you", "", "&2&lUNCOMMON");
	            break;

	        case CREEPER_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.CREEPER, 1);
	            ItemHelper.setDetails(item,
	                    "&a&lCREEPER POKEBALL",
	                    "&7Spawns a Creeper",
	                    "&7Be careful!", "", "&2&lUNCOMMON");
	            break;
	            
	        case FIRE_FLOWER:
	        	 item = ItemHelper.setDetails(new ItemStack(Material.RED_ROSE, 3),
		                    "&c&lFIRE &e&lFLOWER",
		                    "&7Throw a fire flower at an enemy to set", "&7them on fire!", "", "&2&lUNCOMMON");
	        	 break;

			case CAVE_SPIDER_EGG:
				item = ItemHelper.createMonsterEgg(EntityType.CAVE_SPIDER, 1);
				ItemHelper.setDetails(item, "&c&lCAVE SPIDER POKEBALL", "&7Spawns a Cave Spider",
						"", "&2&lUNCOMMON");
				break;

			case SNOWBALL_LAUNCHER:
				item = ItemHelper.setDetails(new ItemStack(Material.IRON_BARDING),
						"&f&lSNOWBALL LAUNCHER",
						"&7Fire a burst of 3 snowballs that", "&7launch enemies back and up!", "",
						SNOWBALL_LAUNCHER_USES_PREFIX + SNOWBALL_LAUNCHER_USES, "", "&2&lUNCOMMON");
				break;

	        // ===== RARE =====
	        case HEALTH2:
	            item = ItemHelper.createPotionItem(PotionType.INSTANT_HEAL, 1, 0, true, true, true);
	            ItemHelper.setDetails(item, "&c&lHEALING II", "", "&9&lRARE");
	            break;

	        case BROOMS:
	            item = ItemHelper.setDetails(new ItemStack(Material.WHEAT, 4),
	                    "&5&lBROOM",
	                    "&7Sends you up and saves you from the void", "", "&9&lRARE");
	            break;

	        case HAMMER:
	            item = ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD, 1, (short) 250),
	                    "&d&lHAMMER", "", "&9&lRARE");
	            item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 10);
	            break;

	        case ENDER_PEARL:
	            item = ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL),
	                    "&5&lENDER PEARL", "", "&9&lRARE");
	            break;

	        case GOLDEN_APPLE:
	            item = ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE),
	                    "&6&lGOLDEN APPLE", "", "&9&lRARE");
	            break;

	        // ===== LEGENDARY =====
	        case BOMB:
	            item = ItemHelper.createPotionItem(PotionType.INSTANT_DAMAGE, 1000, 0, true, true, true);
	            ItemHelper.setDetails(item, "&4&lBOMB", "&7Be careful!", "&7Instantly kills... anyone", "", "&6&lLEGENDARY");
	            break;

	        case NOTCH_APPLE:
	            item = ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1),
	                    "&d&lNOTCH APPLE", "", "&6&lLEGENDARY");
	            break;

	        case EXTRA_LIFE:
	            item = ItemHelper.setDetails(new ItemStack(Material.PRISMARINE_SHARD),
	                    "&3&lEXTRA LIFE",
	                    "&7Receive an extra life", "", "&6&lLEGENDARY");
	            break;

	        default:
	            throw new IllegalStateException("getItem() not implemented for " + this.name());
	    }

	    return item;
	}

	// Shown as Common / Uncommon / Rare / Legendary (the item lore names). Chances are out of 100.
	public enum Rarity {
		COMMON("Common", "&7", 50, 0), RARE("Uncommon", "&2", 35, 0), MYTHIC("Rare", "&9", 12, 70),
		LEGENDARY("Legendary", "&6", 3, 30);

		private final String name;
		private final String color;
		private final int chance;
		private final int opChance; // with OP Item Drops on

		Rarity(String name, String color, int chance, int opChance) {
			this.name = name;
			this.color = color;
			this.chance = chance;
			this.opChance = opChance;
		}

		public String getName() {
			return name;
		}

		public String getColor() {
			return color;
		}

		public int getChance(boolean op) {
			return op ? opChance : chance;
		}
	}
}
