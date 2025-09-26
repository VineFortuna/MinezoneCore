package anthony.SuperCraftBrawl.Game;

import java.util.Random;

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
	SKELE_EGG(Rarity.COMMON), MILK(Rarity.COMMON),

	// UNCOMMON:
	SPEED2(Rarity.UNCOMMON), FIRE_RES(Rarity.UNCOMMON), INSTAGIB(Rarity.UNCOMMON), BAZOOKA(Rarity.UNCOMMON),
	WITCH_EGG(Rarity.UNCOMMON), CREEPER_EGG(Rarity.UNCOMMON),

	// RARE:
	HEALTH2(Rarity.RARE), BROOMS(Rarity.RARE), HAMMER(Rarity.RARE), ENDER_PEARL(Rarity.RARE), BOUNTY(Rarity.RARE),
	GOLDEN_APPLE(Rarity.RARE),

	// LEGENDARY:
	BOMB(Rarity.LEGENDARY), NOTCH_APPLE(Rarity.LEGENDARY), EXTRA_LIFE(Rarity.LEGENDARY);

	private Rarity r;

	GameLootDrops(Rarity r) {
		this.r = r;
	}

	public Rarity getRarity() {
		return this.r;
	}

	public static GameLootDrops getDrop() {
		Random r = new Random();
		int chance = r.nextInt(100);
		GameLootDrops[] drops = GameLootDrops.values();
		GameLootDrops randomDrop = null; // Default value

		if (chance >= 40 && chance <= 100) {
			randomDrop = helper(Rarity.COMMON, randomDrop, drops, r);
		} else if (chance < 40 && chance >= 15) {
			randomDrop = helper(Rarity.UNCOMMON, randomDrop, drops, r);
		} else if (chance < 15 && chance >= 3) {
			randomDrop = helper(Rarity.RARE, randomDrop, drops, r);
		} else if (chance >= 0 && chance < 3) {
			randomDrop = helper(Rarity.LEGENDARY, randomDrop, drops, r);
		}

		return randomDrop;
	}

	public static GameLootDrops helper(Rarity rarity, GameLootDrops randomDrop, GameLootDrops[] drops, Random r) {
		boolean found = false;

		while (found == false) {
			randomDrop = drops[r.nextInt(drops.length)];

			if (randomDrop.getRarity() == rarity)
				found = true;
		}

		return randomDrop;
	}

	public ItemStack getItem() {
	    ItemStack item = null;

	    switch (this) {
	        // ===== COMMON =====
	        case SLOWNESS:
	            item = ItemHelper.createPotionItem(PotionType.SLOWNESS, 1, 15, true, true, true);
	            ItemHelper.setDetails(item, "&8&lSLOWNESS II &7(15 sec)");
	            break;

	        case SLOWBALLS:
	            item = ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL, 8),
	                    "&f&lSLOWBALLS",
	                    "&7Give Slowness 1 for 3s to an enemy");
	            break;

	        case BLOOPER:
	            item = ItemHelper.setDetails(new ItemStack(Material.RABBIT_FOOT),
	                    "&e&lBLOOPER",
	                    "&7Give Blindness or Nausea to an enemy");
	            break;

	        case ZOMBIE_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.ZOMBIE, 1);
	            ItemHelper.setDetails(item, "&2&lZOMBIE POKEBALL", "&7Spawns an equipped zombie");
	            break;

	        case SKELE_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.SKELETON, 1);
	            ItemHelper.setDetails(item, "&7&lSKELETON POKEBALL", "&7Spawns a skeleton with punch 2");
	            break;

	        case MILK:
	            item = ItemHelper.setDetails(new ItemStack(Material.MILK_BUCKET),
	                    "&f&lMILK",
	                    "&7Removes fire and all negative effects");
	            break;

	        // ===== UNCOMMON =====
	        case SPEED2:
	            item = ItemHelper.createPotionItem(PotionType.SPEED, 1, 30, true, true, true);
	            ItemHelper.setDetails(item, "&b&lSPEED II &7(30 sec)");
	            break; // <<< IMPORTANT (prevents fall-through to default)

	        case FIRE_RES:
	            item = ItemHelper.createPotionItem(PotionType.FIRE_RESISTANCE, 0, 30, true, true, true);
	            ItemHelper.setDetails(item, "&6&lFIRE RESISTANCE &7(30 sec)");
	            break;

	        case INSTAGIB:
	            item = ItemHelper.setDetails(new ItemStack(Material.GOLD_HOE, 5, (short) 1),
	                    "&e&lINSTAGIB",
	                    "&7Do damage and send your enemies up");
	            break;

	        case BAZOOKA:
	            item = ItemHelper.setDetails(new ItemStack(Material.DIAMOND_HOE, 3, (short) 1),
	                    "&b&lBAZOOKA",
	                    "&7Explode the area it lands");
	            ItemHelper.setHideFlags(item, true);
	            break;

	        case WITCH_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.WITCH, 1);
	            ItemHelper.setDetails(item, "&5&lWITCH POKEBALL", "&7Spawns a witch to help you");
	            break;

	        case CREEPER_EGG:
	            item = ItemHelper.createMonsterEgg(EntityType.CREEPER, 1);
	            ItemHelper.setDetails(item,
	                    "&a&lCREEPER POKEBALL",
	                    "&7Spawns a creeper",
	                    "&7Be careful!");
	            break;

	        // ===== RARE =====
	        case HEALTH2:
	            item = ItemHelper.createPotionItem(PotionType.INSTANT_HEAL, 1, 0, true, true, true);
	            ItemHelper.setDetails(item, "&c&lHEALING II");
	            break;

	        case BROOMS:
	            item = ItemHelper.setDetails(new ItemStack(Material.WHEAT, 4),
	                    "&5&lBROOM",
	                    "&7Sends you up and saves you from the void");
	            break;

	        case HAMMER:
	            item = ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD, 1, (short) 250),
	                    "&d&lHAMMER");
	            item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 10);
	            break;

	        case ENDER_PEARL:
	            item = ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL),
	                    "&5&lENDER PEARL");
	            break;

	        case BOUNTY:
	            item = ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR, 1),
	                    "&a&lBOUNTY",
	                    "&7Set a bounty on a random player",
	                    "&7Kill them to claim extra tokens");
	            break;

	        case GOLDEN_APPLE:
	            item = ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE),
	                    "&6&lGOLDEN APPLE");
	            break;

	        // ===== LEGENDARY =====
	        case BOMB:
	            item = ItemHelper.createPotionItem(PotionType.INSTANT_DAMAGE, 1000, 0, true, true, true);
	            ItemHelper.setDetails(item, "&4&lBOMB", "&7Be careful!", "&7Instantly kills... anyone");
	            break;

	        case NOTCH_APPLE:
	            item = ItemHelper.setDetails(new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1),
	                    "&d&lNOTCH APPLE");
	            break;

	        case EXTRA_LIFE:
	            item = ItemHelper.setDetails(new ItemStack(Material.PRISMARINE_SHARD),
	                    "&3&lEXTRA LIFE",
	                    "&7Receive an extra life");
	            break;

	        default:
	            throw new IllegalStateException("getItem() not implemented for " + this.name());
	    }

	    return item;
	}

	public enum Rarity {
		COMMON(60), UNCOMMON(25), RARE(12), LEGENDARY(3);

		private final int weight;

		Rarity(int weight) {
			this.weight = weight;
		}

		public int weight() {
			return weight;
		}
	}
}
