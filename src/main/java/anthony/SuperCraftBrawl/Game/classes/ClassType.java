package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.all.*;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum ClassType {

	// Free Classes
	Cactus(1, 0, 0),
	Skeleton(4, 0, 0),
	Enderman(7, 0, 0),
	Horse(18, 0, 0),
	Rabbit(26, 0, 0),
	Squid(20, 0, 0),
	Spider(21, 0, 0),
	Pig(30, 0, 0, true),
	Blaze(31, 0, 0),
	Wither(33, 0, 0),
	Creeper(35, 0, 0),
	Present(38, 0, 0, true),
	Dweller(43, 0, 0, true),
	Zombie(44, 0, 0),
	Silverfish(45, 0, 0),
	Villager(49, 0, 0),
	FlintAndSteel(58, 0, 0),
	LargeFern(64, 0, 0),
	Fisherman(67, 0, 0),

	// Token Classes
	TNT(2, 350, 0),
	Ninja(5, 1000, 0),
	Chicken(9, 400, 0),
	DarkSethBling(12, 800, 0),
	SnowGolem(14, 800, 0),
	Sheep(17, 550, 0),
	Bunny(24, 450, 0),
	ButterBro(25, 1200, 0, true),
	Steve(28, 850, 0),
	Notch(29, 1000, 0),
	Potato(32, 750, 0),
	Ocelot(34, 250, 0),
	Noteblock(36, 800, 0),
	EnchantTable(37, 350, 0, true),
	Star(42, 850, 0, true),
	Anvil(46, 700, 0),
	Summoner(47, 525, 0),
	Vampire(53, 800, 0),
	Bee(55, 425, 0),
	Hunter(57, 500, 0),
	WitherSkeleton(59, 1500, 0),
	Wolf(71, 400, 0),
	BrewingStand(68, 350, 0),
	Parrot(72, 800, 0),

	// Level Classes
	ZombiePigman(51, 0, 3),
	Witch(13, 0, 5),
	ZombieVillager(50, 0, 10),
	Ice(54, 0, 15),
	Jeb(56, 0, 20),
	Shulker(60, 0, 25),
	Cloud(63, 0, 30),
	Guardian(69, 0, 30, true),

	// Donor Classes
	Enderdragon(3, 0, Rank.VIP),
	IronGolem(6, 0, Rank.VIP),
	Ghast(8, 0, Rank.VIP),
	Slime(10, 0, Rank.VIP),
	PiglinBrute(11, 0, Rank.VIP),
	Bat(15, 0, Rank.VIP),
	SethBling(16, 0, Rank.VIP),
	Melon(19, 0, Rank.VIP),
	Mooshroom(22, 0, Rank.VIP),
	Herobrine(23, 0, Rank.VIP),
	Wizard(41, 0, Rank.VIP),
	MagmaCube(48, 0, Rank.VIP),
	Bedrock(61, 0, Rank.VIP),
	Firework(62, 0, Rank.VIP),
	Vindicator(65, 0, Rank.VIP, true),
	Fade(66, 0, Rank.VIP),
	Endermite(70, 0, Rank.VIP),

	// Holiday Classes
	Elf(101, 0, 0, true),
	GingerBreadMan(102, 0, 0, true),
	Santa(103, 0, 0, true),
	GrimReaper(104, 0, 0, true),
	Freddy(105, 0, 0);

	private final int id;
	private int tokenCost = 0;
	private int level = 0;
	private Rank donor;
	private boolean isVaulted = false;

	ClassType(int id, int tokenCost, int level, boolean isVaulted) {
		this.id = id;
		this.tokenCost = tokenCost;
		this.level = level;
		this.isVaulted = isVaulted;
	}

	ClassType(int id, int tokenCost, Rank donor, boolean isVaulted) {
		this.id = id;
		this.tokenCost = tokenCost;
		this.donor = donor;
		this.isVaulted = isVaulted;
	}

	ClassType(int id, int tokenCost, int level) {
		this.id = id;
		this.tokenCost = tokenCost;
		this.level = level;
	}

	ClassType(int id, int tokenCost, Rank donor) {
		this.id = id;
		this.tokenCost = tokenCost;
		this.donor = donor;
	}

	public String getClassDesc() {
		switch (this) {
			case Cactus:
				return "A prickly living thing, made up of thorns and... blood";
			case Freddy:
				return "Uh oh, its 5 nights at Freddy's!!!";
			case Santa:
				return "HO HO HO! MERRRRRRYYYY CHRISTMASSSSS";
			case GingerBreadMan:
				return "DO YOU KNOW? THE GINGERBREADMAN!?!?!";
			case Elf:
				return "Santa's lil helper here to deliver";
			case GrimReaper:
				return "Harvest souls with deadly precision, wielding dark powers...";
			case BrewingStand:
				return "Use blaze powder to brew potions";
			case Fade:
				return "Fade out of existence from your opponents briefly!";
			case Cloud:
				return "Cast different effects on your opponents!";
			case Firework:
				return "Shoot fireworks and inflict different effects!";
			case Shulker:
				return "Levitate enemies into the air and damage them";
			case WitherSkeleton:
				return "Shoot your withering blasts at other players!";
			case Rabbit:
				return "Jump around and hyper kick enemies";
			case FlintAndSteel:
				return "Combo your Flint and Steel to fuse them and set enemies on fire";
			case Hunter:
				return "Damage your enemies to gain Blood Lust and special potions!";
			case Jeb:
				return "Shoot a beam that pushes enemies, stronger at closer distances";
			case Bee:
				return "Sting enemies and sustain yourself from the flower sources";
			case Ice:
				return "Freeze your enemies into place";
			case Vampire:
				return "Use your bow to poison your enemies, rewards precision";
			case ZombiePigman:
				return "Summon your brothers to fight with you";
			case ZombieVillager:
				return "Poisonous creature...";
			case Villager:
				return "Trade emeralds for items";
			case MagmaCube:
				return "Bounce around and spawn Fairly high double jump and ability to spawn 7 MagmaCube minions";
			case Summoner:
				return "Beware the Summoner!";
			case Anvil:
				return "Stomp your opponents from great heights";
			case Silverfish:
				return "Place a wall to block off enemies and spawn annoying little creatures";
			case Zombie:
				return "Summon an army to fight with you and infect enemies";
			case Dweller:
				return "Beware the Dweller of SCB!";
			case Star:
				return "Born in 1964 Jeffrey... Jeffrey Besos";
			case Wizard:
				return "A Wizard with different spells!";
			case Present:
				return "Copy other people's item, armor, or effect/double jump!";
			case Bedrock:
				return "Some say tough to defeat, I'd say invincible";
			case Noteblock:
				return "Play songs for some awesome effects!";
			case EnchantTable:
				return "Gain exp by fighting and upgrade your sword with awesome enchantments!";
			case Ocelot:
				return "Chase down your opponents with your high speed or Purr Attack!";
			case Creeper:
				return "Defeat your opponents with your explosive arsenal";
			case Potato:
				return "Who doesn't like potatoes?!";
			case Wither:
				return "Utilize your explosive skulls to defeat your enemies!";
			case Notch:
				return "Shoot a beam that pulls enemies, stronger at further distances";
			case Blaze:
				return "Use your fire abilities to defeat enemies";
			case Steve:
				return "Mine minerals to upgrade your pickaxe and get special items";
			case Skeleton:
				return "Effective shooter at taking down their targets";
			case Enderdragon:
				return "Move around your opponents and heal from your ender crystals!";
			case Enderman:
				return "Stare into the souls of your enemies whilst teleporting around them";
			case Horse:
				return "Different treats equals different effects";
			case Squid:
				return "Use your ink to blind enemies";
			case Spider:
				return "Web enemies into place and Mutate to get random effects";
			case Ninja:
				return "Ambush your enemies with sneaky attacks and agility";
			case TNT:
				return "Blow up your enemies with TNT!";
			case Chicken:
				return "Bock bock backaaack! Your eggs are a bit explosive";
			case DarkSethBling:
				return "The evil counterpart of the redstone King";
			case Witch:
				return "Throw potions at enemies and use brooms to fly away from tricky situations";
			case Sheep:
				return "Your different colors gives you different powers!";
			case SnowGolem:
				return "Build a snow platform to save yourself";
			case Bunny:
				return "Easter Bunny is coming to town!";
			case ButterBro:
				return "Yo, you there Sky??";
			case IronGolem:
				return "Smack down your enemies to defend your village!";
			case Ghast:
				return "Burn down your enemies with your sorrows";
			case Slime:
				return "Throw sticky grenades at enemies!";
			case PiglinBrute:
				return "Knock people away... or explode them!";
			case Bat:
				return "Move around your opponents and bite them to death!";
			case SethBling:
				return "The creator of SCB, wanna fight?";
			case Melon:
				return "Gain different weapon and powers";
			case Mooshroom:
				return "Your strength comes from the milk";
			case Herobrine:
				return "Play different tricks on enemies";
			case Pig:
				return "Hit and run. In your panic, you gain speed when hit";
			case LargeFern:
				return "??????";
			case Vindicator:
				return "Vindicate yourself in front of your enemies";
			case Fisherman:
				return "Let's go fishing!";
			case Endermite:
				return "Unleash chaos as you swap places with your swarm of Endermites";
			case Wolf:
				return "Have your pack defend you from enemies!";
			case Parrot:
				return "Regenerate health through music!";
			case Guardian:
				return "Guard thyself and thy family";
//		case Fluxty:
//			return: "We cannot have HATERS in the community... So use your Wood Axe to kick em all out!";
//		case Snowman:
//			return "This is a Snowman, not a SnowGolem. Get it right pleb!";
		}
		return null;
	}

	public String getTag() {
		switch (this) {
			case Bat:
				return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Bat" + ChatColor.RESET;
			case Freddy:
				return "" + ChatColor.ITALIC + color("&6&l&oFreddy&r");
			case Santa:
				return "" + ChatColor.RED + ChatColor.BOLD + ChatColor.ITALIC + "Santa" + ChatColor.RESET;
			case GingerBreadMan:
				return color("&6GingerBread&0Man&r");
			case Elf:
				return color("&2&lElf&r");
			case GrimReaper:
				return color("&8&l&oGrimReaper&r");
			case BrewingStand:
				return "" + ChatColor.YELLOW + "BrewingStand" + ChatColor.RESET;
			case Fade:
				return "" + ChatColor.DARK_GRAY + ChatColor.ITALIC + "Fade" + ChatColor.RESET;
			case Cloud:
				return "" + ChatColor.WHITE + ChatColor.ITALIC + "Cloud" + ChatColor.RESET;
			case Guardian:
				return "" + ChatColor.DARK_AQUA + ChatColor.BOLD + "Guardian" + ChatColor.RESET;
			case Firework:
				return "" + ChatColor.RED + ChatColor.BOLD + "Firework" + ChatColor.RESET;
			case Bedrock:
				return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Bedrock" + ChatColor.RESET;
			case Shulker:
				return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + ChatColor.ITALIC + "Shulker" + ChatColor.RESET;
			case WitherSkeleton:
				return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "WitherSkeleton" + ChatColor.RESET;
			case FlintAndSteel:
				return "" + ChatColor.DARK_GRAY + "Flint" + ChatColor.GRAY + "&" + ChatColor.WHITE + "Steel" + ChatColor.RESET;
			case Hunter:
				return "" + ChatColor.GOLD + ChatColor.BOLD + "Hunter" + ChatColor.RESET;
			case Jeb:
				return "" + ChatColor.GRAY + ChatColor.ITALIC + "Jeb" + ChatColor.RESET;
			case Bee:
				return "" + ChatColor.YELLOW + ChatColor.ITALIC + ChatColor.BOLD + "Bee" + ChatColor.RESET;
			case Ice:
				return "" + ChatColor.AQUA + ChatColor.BOLD + "Ice" + ChatColor.RESET;
			case Vampire:
				return "" + ChatColor.GRAY + ChatColor.BOLD + "Vampire" + ChatColor.RESET;
			case ZombiePigman:
				return "" + ChatColor.GREEN + ChatColor.ITALIC + "Zombie" + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + "Pigman" + ChatColor.RESET;
			case ZombieVillager:
				return "" + ChatColor.GREEN + ChatColor.BOLD + "Zombie" + ChatColor.GRAY + ChatColor.BOLD + "Villager" + ChatColor.RESET;
			case Villager:
				return "" + ChatColor.GREEN + "Villager" + ChatColor.RESET;
			case MagmaCube:
				return "" + ChatColor.DARK_RED + ChatColor.BOLD + "MagmaCube" + ChatColor.RESET;
			case Summoner:
				return "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + ChatColor.ITALIC + "Summoner" + ChatColor.RESET;
			case Anvil:
				return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Anvil" + ChatColor.RESET;
			case Silverfish:
				return "" + ChatColor.GRAY + ChatColor.ITALIC + "Silverfish" + ChatColor.RESET;
			case Zombie:
				return "" + ChatColor.DARK_GREEN + ChatColor.ITALIC + "Zombie" + ChatColor.RESET;
			case Dweller:
				return "" + ChatColor.GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Dweller" + ChatColor.RESET;
			case Star:
				return "" + ChatColor.DARK_GRAY + "Star" + ChatColor.RESET;
			case Wizard:
				return "" + ChatColor.GOLD + ChatColor.BOLD + ChatColor.ITALIC + "Wizard" + ChatColor.RESET;
			case Present:
				return "" + ChatColor.GOLD + ChatColor.BOLD + "Present" + ChatColor.RESET;
			case Wither:
				return "" + ChatColor.DARK_GRAY + "Wither" + ChatColor.RESET;
			case Noteblock:
				return "" + ChatColor.GOLD + "Noteblock" + ChatColor.RESET;
			case EnchantTable:
				return "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Enchant" + ChatColor.RED + ChatColor.BOLD + "Table"
						+ ChatColor.RESET;
			case Ocelot:
				return "" + ChatColor.YELLOW + ChatColor.BOLD + "Ocelot" + ChatColor.RESET;
			case Creeper:
				return "" + ChatColor.GREEN + "Creeper" + ChatColor.RESET;
			case Notch:
				return "" + ChatColor.DARK_AQUA + ChatColor.BOLD + "Notch" + ChatColor.RESET;
			case Blaze:
				return "" + ChatColor.DARK_RED + "Blaze" + ChatColor.RESET;
			case Potato:
				return "" + ChatColor.YELLOW + ChatColor.ITALIC + "Potato" + ChatColor.RESET;
			case Steve:
				return "" + ChatColor.AQUA + "Steve" + ChatColor.RESET;
			case PiglinBrute:
				return "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.ITALIC + "PiglinBrute" + ChatColor.RESET;
			case Herobrine:
				return "" + ChatColor.AQUA + ChatColor.BOLD + ChatColor.ITALIC + "Herobrine" + ChatColor.RESET;
			case Cactus:
				return "" + ChatColor.DARK_GREEN + "Cactus" + ChatColor.RESET;
			case Chicken:
				return "" + ChatColor.YELLOW + ChatColor.BOLD + "Chicken" + ChatColor.RESET;
			case DarkSethBling:
				return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "DarkSethBling" + ChatColor.RESET;
			case Enderdragon:
				return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Ender" + ChatColor.RESET + ChatColor.DARK_GRAY + ChatColor.BOLD + "Dragon" + ChatColor.RESET;
			case Enderman:
				return "" + ChatColor.DARK_PURPLE + "Enderman" + ChatColor.RESET;
			case Ghast:
				return "" + ChatColor.WHITE + ChatColor.BOLD + ChatColor.ITALIC + "Ghast" + ChatColor.RESET;
			case IronGolem:
				return "" + ChatColor.GRAY + ChatColor.BOLD + ChatColor.ITALIC + "IronGolem" + ChatColor.RESET;
			case Ninja:
				return "" + ChatColor.BLUE + ChatColor.BOLD + "Ninja" + ChatColor.RESET;
			case SethBling:
				return "" + ChatColor.RED + ChatColor.BOLD + ChatColor.ITALIC + "SethBling" + ChatColor.RESET;
			case Sheep:
				return "" + ChatColor.WHITE + ChatColor.BOLD + "Sheep" + ChatColor.RESET;
			case Skeleton:
				return "" + ChatColor.GRAY + "Skeleton" + ChatColor.RESET;
			case Slime:
				return "" + ChatColor.GREEN + ChatColor.BOLD + "Slime" + ChatColor.RESET;
			case SnowGolem:
				return "" + ChatColor.WHITE + ChatColor.BOLD + "SnowGolem" + ChatColor.RESET;
			case TNT:
				return "" + ChatColor.RED + ChatColor.BOLD + "T" + ChatColor.RESET + ChatColor.BOLD + "N" + ChatColor.RESET
						+ ChatColor.RED + ChatColor.BOLD + "T" + ChatColor.RESET;
			case Witch:
				return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Witch" + ChatColor.RESET;
			case Horse:
				return "" + ChatColor.GOLD + ChatColor.ITALIC + "Horse" + ChatColor.RESET;
			case Melon:
				return "" + ChatColor.YELLOW + "Melon" + ChatColor.RESET;
			case Rabbit:
				return "" + ChatColor.GREEN + ChatColor.ITALIC + "Rabbit" + ChatColor.RESET;
			case Squid:
				return "" + ChatColor.DARK_BLUE + ChatColor.ITALIC + "Squid" + ChatColor.RESET;
			case Spider:
				return "" + ChatColor.RED + ChatColor.ITALIC + "Spider" + ChatColor.RESET;
			case Mooshroom:
				return "" + ChatColor.RED + ChatColor.ITALIC + ChatColor.BOLD + "Mooshroom" + ChatColor.RESET;
			case Bunny:
				return "" + ChatColor.YELLOW + ChatColor.ITALIC + ChatColor.BOLD + "Bunny" + ChatColor.RESET;
			case ButterBro:
				return "" + ChatColor.YELLOW + ChatColor.BOLD + "ButterBro" + ChatColor.RESET;
			case Pig:
				return "" + ChatColor.LIGHT_PURPLE + "Pig" + ChatColor.RESET;
			case LargeFern:
				return "" + ChatColor.DARK_GREEN + ChatColor.ITALIC + ChatColor.BOLD + "LargeFern" + ChatColor.RESET;
			case Vindicator:
				return "" + ChatColor.GRAY + ChatColor.BOLD + "Vindicator" + ChatColor.RESET;
			case Fisherman:
				return "" + ChatColor.DARK_AQUA + ChatColor.ITALIC + "Fisherman" + ChatColor.RESET;
			case Endermite:
				return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + ChatColor.ITALIC + "Endermite" + ChatColor.RESET;
			case Wolf:
				return "" + ChatColor.WHITE  + ChatColor.ITALIC + "Wolf" + ChatColor.RESET;
			case Parrot:
				return "" + ChatColor.GREEN + "Parrot" + ChatColor.RESET;
// 		case Snowman:
// 			return "" + ChatColor.RESET + "Snow" + ChatColor.DARK_GREEN + "Man" + ChatColor.RESET;
// 		case Fluxty:
// 			return "" + ChatColor.GREEN + ChatColor.BOLD + "Fluxty" + ChatColor.RESET;
			default:
				break;
		}
		return this.toString();
	}

	public String getSecondTag() {
		switch (this) {
			case WitherSkeleton:
				return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "WSkele" + ChatColor.RESET;
//			case Enderdragon:
//				return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "EDragon" + ChatColor.RESET + ChatColor.DARK_GRAY + ChatColor.BOLD + "Dragon" + ChatColor.RESET;
//			case FlintAndSteel:
//				return "" + ChatColor.DARK_GRAY + "Fl" + ChatColor.GRAY + "&" + ChatColor.WHITE + "St" + ChatColor.RESET;
//			case LargeFern:
//				return "" + ChatColor.DARK_GREEN + ChatColor.ITALIC + ChatColor.BOLD + "LFern" + ChatColor.RESET;
//			case ZombiePigman:
//				return "" + ChatColor.GREEN + ChatColor.ITALIC + "Z" + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + "Pigman" + ChatColor.RESET;
//			case ZombieVillager:
//				return "" + ChatColor.GREEN + ChatColor.BOLD + "Z" + ChatColor.GRAY + ChatColor.BOLD + "Villager" + ChatColor.RESET;
		}
		return null;
	}

	public ItemStack getItem() {
		switch (this) {
		case Cactus:
			return new ItemStack(Material.CACTUS);
		case Freddy:
			return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRiMjdjY2I0ZjEyNjQwZjFiNThlYTYyZDkwY2RhY2U0NGMwZjJkYTlmMzkwOGUyNWViMTZiZGI1YmJiNWE2NSJ9fX0=");
		case Santa:
			return ItemHelper.createSkullTexture(
					"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19");
		case GingerBreadMan:
			return new ItemStack(Material.COOKIE);
		case Elf:
			return new ItemStack(Material.CAKE);
		case GrimReaper:
			return new ItemStack(Material.DIAMOND_HOE);
		case BrewingStand:
			return new ItemStack(Material.BREWING_STAND_ITEM);
		case Fade:
			return new ItemStack(Material.STRING);
		case Cloud:
			return new ItemStack(Material.WOOL);
		case Firework:
			return new ItemStack(Material.FIREWORK);
		case Shulker:
			return new ItemStack(Material.STAINED_CLAY, 1, (byte) DyeColor.PURPLE.getData());
		case Dweller:
			return new ItemStack(Material.BONE);
		case WitherSkeleton:
			return ItemHelper.createSkullHead(1, SkullType.WITHER);
		case Rabbit:
			return new ItemStack(Material.RABBIT_FOOT);
		case FlintAndSteel:
			return new ItemStack(Material.FLINT_AND_STEEL);
		case Hunter:
			return new ItemStack(Material.GOLD_SWORD);
		case Jeb:
			return new ItemStack(Material.STONE);
		case Bee:
			return new ItemStack(Material.GLOWSTONE_DUST);
		case Ice:
			return new ItemStack(Material.ICE);
		case Vampire:
			return new ItemStack(Material.GHAST_TEAR);
		case ZombiePigman:
			return ItemHelper.createSkullTexture(
					"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY5OGEzY2ZkZjhjMTNlZTY2MzQxNDBmOTQ1YjcxZDJlNDg4ZmY0ODVlMTBjMzNhZTI1ODIxZDgyZDg0OGE3MyJ9fX0=");
		case Villager:
			return new ItemStack(Material.EMERALD);
		case DarkSethBling:
			return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWE3MDVkYzkzYWIzYjIyNDUxNmI4YWZiMjMwNTI5MWIyODU1ZWIyMzVjZThlNGVkNDY2NjQyODBhYmRhIn19fQ==");
		case ZombieVillager:
			return new ItemStack(Material.ROTTEN_FLESH);
		case MagmaCube:
			return ItemHelper.createSkullTexture(
					"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFhNmM0NWIyY2Y3OTc1Yjk1ZmJjY2U0ZWQ5YjA2NDZhYzAwY2I5Y2M5ZjY2ZGM1YzI0ZTgxZDJjOTFlZTdjMSJ9fX0=");
		case Summoner:
			return new ItemStack(Material.ENCHANTED_BOOK);
		case Anvil:
			return new ItemStack(Material.ANVIL);
		case Silverfish:
			return ItemHelper.createSkullTexture(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5MWRhYjgzOTFhZjVmZGE1NGFjZDJjMGIxOGZiZDgxOWI4NjVlMWE4ZjFkNjIzODEzZmE3NjFlOTI0NTQwIn19fQ==");
		case Zombie:
			return ItemHelper.createSkullHead(1, SkullType.ZOMBIE);
		case Star:
			return new ItemStack(Material.NETHER_STAR);
		case Wizard:
			return new ItemStack(Material.BLAZE_POWDER);
		case Present:
			return new ItemStack(Material.CHEST);
		case Noteblock:
			return new ItemStack(Material.NOTE_BLOCK);
		case Bedrock:
			return new ItemStack(Material.BEDROCK);
		case EnchantTable:
			return new ItemStack(Material.ENCHANTMENT_TABLE);
		case Skeleton:
			return new ItemStack(Material.SKULL_ITEM);
		case Enderman:
			return new ItemStack(Material.ENDER_PEARL);
		case Horse:
			return new ItemStack(Material.LEASH);
		case Squid:
			return new ItemStack(Material.INK_SACK);
		case Spider:
			return new ItemStack(Material.SPIDER_EYE);
		case Pig:
			return new ItemStack(Material.PORK);
		case Blaze:
			return new ItemStack(Material.BLAZE_ROD);
		case Wither:
			return ItemHelper.createSkullTexture(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg2ZGMwY2ZjYWVlY2ZlMWFiNjkxNDZlNGQ0ZjExOTA4MzcwNzZhNjdkZWMxMzVmYWJkYTYyNzFmMzc1ZDAxZiJ9fX0=");
		case Creeper:
			return ItemHelper.createSkullHead(1, SkullType.CREEPER);
		case IronGolem:
			return ItemHelper.createSkullTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ2NTJjOTVmYzViZGY3ZWQwM2M1NjdlOTBmZjYyNWJlMDI4YWQ4NDg2M2QzMjcxZDZlNmMxYWEzMDhmMzEzZiJ9fX0=");
		case Ghast:
			return new ItemStack(new ItemStack(Material.GHAST_TEAR));
		case Slime:
			return new ItemStack(new ItemStack(Material.SLIME_BALL));
		case PiglinBrute:
			return new ItemStack(new ItemStack(Material.GOLD_AXE));
		case Enderdragon:
			return new ItemStack(new ItemStack(Material.DRAGON_EGG));
		case Bat:
			return ItemHelper.createSkullTexture(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5OWRlZWY5MTlkYjY2YWMyYmQyOGQ2MzAyNzU2Y2NkNTdjN2Y4YjEyYjlkY2E4ZjQxYzNlMGEwNGFjMWNjIn19fQ==");
		case SethBling:
			return ItemHelper.createSkullTexture(
					"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==");
		case Melon:
			return new ItemStack(new ItemStack(Material.MELON));
		case Mooshroom:
			return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDBiYzYxYjk3NTdhN2I4M2UwM2NkMjUwN2EyMTU3OTEzYzJjZjAxNmU3YzA5NmE0ZDZjZjFmZTFiOGRiIn19fQ==");
		case Herobrine:
			return new ItemStack(new ItemStack(Material.DIAMOND));
		case Ninja:
			return new ItemStack(Material.STICK);
		case TNT:
			return new ItemStack(Material.TNT);
		case Chicken:
			return new ItemStack(Material.EGG);
		case Witch:
			return new ItemStack(Material.WHEAT);
		case Sheep:
			return new ItemStack(Material.WOOL);
		case SnowGolem:
			return new ItemStack(Material.SNOW_BALL);
		case Bunny:
			return new ItemStack(Material.GOLDEN_CARROT);
		case ButterBro:
			return new ItemStack(Material.GOLD_INGOT);
		case Steve:
			return new ItemStack(Material.STONE_PICKAXE);
		case Notch:
			return new ItemStack(Material.GRASS);
		case Potato:
			return new ItemStack(Material.POTATO_ITEM);
		case Ocelot:
			return new ItemStack(Material.RAW_FISH);
		case LargeFern:
			return new ItemStack(Material.DOUBLE_PLANT, 1, (short) 3);
		case Vindicator:
			return new ItemStack(Material.IRON_AXE);
		case Fisherman:
			return new ItemStack(Material.FISHING_ROD);
		case Endermite:
			return ItemHelper.createSkullTexture(
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0=");
 		case Wolf:
 			return new ItemStack(Material.BONE);
		case Parrot:
			return new ItemStack(Material.FEATHER);
 		case Guardian:
 			return new ItemStack(Material.PRISMARINE_SHARD);
		}

		return null;
	}

	public BaseClass GetClassInstance(GameInstance instance, Player player) {
		switch (this) {
			case Cactus:
				return new CactusClass(instance, player);
			case Freddy:
				return new FreddyClass(instance, player);
			case Santa:
				return new SantaClass(instance, player);
			case GingerBreadMan:
				return new GingerBreadManClass(instance, player);
			case Elf:
				return new ElfClass(instance, player);
			case GrimReaper:
				return new GrimReaperClass(instance, player);
			case BrewingStand:
				return new BrewingStandClass(instance, player);
			case Fade:
				return new FadeClass(instance, player);
			case Cloud:
				return new CloudClass(instance, player);
			case Firework:
				return new FireworkClass(instance, player);
			case Shulker:
				return new ShulkerClass(instance, player);
			case WitherSkeleton:
				return new WitherSkeletonClass(instance, player);
			case Rabbit:
				return new RabbitClass(instance, player);
			case FlintAndSteel:
				return new FlintAndSteelClass(instance, player);
			case Hunter:
				return new HunterClass(instance, player);
			case Jeb:
				return new JebClass(instance, player);
			case Bee:
				return new BeeClass(instance, player);
			case Ice:
				return new IceClass(instance, player);
			case Vampire:
				return new VampireClass(instance, player);
			case ZombiePigman:
				return new ZombiePigmanClass(instance, player);
			case Villager:
				return new VillagerClass(instance, player);
			case ZombieVillager:
				return new ZombieVillagerClass(instance, player);
			case MagmaCube:
				return new MagmaCubeClass(instance, player);
			case Summoner:
				return new SummonerClass(instance, player);
			case Anvil:
				return new AnvilClass(instance, player);
			case Silverfish:
				return new SilverfishClass(instance, player);
			case Zombie:
				return new ZombieClass(instance, player);
			case Dweller:
				return new DwellerClass(instance, player);
			case Star:
				return new StarClass(instance, player);
			case Wizard:
				return new WizardClass(instance, player);
			case Present:
				return new PresentClass(instance, player);
			case Bedrock:
				return new BedrockClass(instance, player);
			case Notch:
				return new NotchClass(instance, player);
			case EnchantTable:
				return new EnchantTableClass(instance, player);
			case Noteblock:
				return new NoteblockClass(instance, player);
			case Ocelot:
				return new OcelotClass(instance, player);
			case Creeper:
				return new CreeperClass(instance, player);
			case Wither:
				return new WitherClass(instance, player);
			case Blaze:
				return new BlazeClass(instance, player);
			case Potato:
				return new PotatoClass(instance, player);
			case Steve:
				return new SteveClass(instance, player);
			case Enderdragon:
				return new EnderdragonClass(instance, player);
			case Skeleton:
				return new SkeletonClass(instance, player);
			case TNT:
				return new TNTClass(instance, player);
			case Ninja:
				return new NinjaClass(instance, player);
			case IronGolem:
				return new IrongolemClass(instance, player);
			case Enderman:
				return new EndermanClass(instance, player);
			case Ghast:
				return new GhastClass(instance, player);
			case Herobrine:
				return new HerobrineClass(instance, player);
			case Chicken:
				return new ChickenClass(instance, player);
			case Slime:
				return new SlimeClass(instance, player);
			case PiglinBrute:
				return new PiglinBruteClass(instance, player);
			case DarkSethBling:
				return new DarkSethBlingClass(instance, player);
			case Witch:
				return new WitchClass(instance, player);
			case SnowGolem:
				return new SnowGolemClass(instance, player);
			case Bat:
				return new BatClass(instance, player);
			case SethBling:
				return new SethBlingClass(instance, player);
			case Sheep:
				return new SheepClass(instance, player);
			case Horse:
				return new HorseClass(instance, player);
			case Melon:
				return new MelonClass(instance, player);
			case ButterBro:
				return new ButterBroClass(instance, player);
			case Squid:
				return new SquidClass(instance, player);
			case Spider:
				return new SpiderClass(instance, player);
			case Mooshroom:
				return new MooshroomClass(instance, player);
			case Bunny:
				return new BunnyClass(instance, player);
			case Pig:
				return new PigClass(instance, player);
			case LargeFern:
				return new LargeFernClass(instance, player);
			case Vindicator:
				return new VindicatorClass(instance, player);
			case Fisherman:
				return new FishermanClass(instance, player);
			case Endermite:
				return new EndermiteClass(instance, player);
			case Wolf:
				return new WolfClass(instance, player);
			case Parrot:
				return new ParrotClass(instance, player);
			case Guardian:
				return new GuardianClass(instance, player);
//		case Fluxty:
//			return new FluxtyClass(instance, player);
//		case Snowman:
//			return new SnowmanClass(instance, player);
		}
		return null;
	}

	public static ClassType[] sortAlphabetically(ClassType[] classes) {
		Arrays.sort(classes, Comparator.comparing(ClassType::toString));
		return classes;
	}

	public static ClassType[] getAvailableClasses() {
		return Arrays.stream(values())
				.filter(clazz -> !clazz.isVaulted())
				.toArray(ClassType[]::new);
	}
	public static ClassType[] getFreeClasses(boolean includeVaulted) {
		return Arrays.stream(values())
				.filter(clazz -> (includeVaulted || !clazz.isVaulted) && clazz.tokenCost == 0 && clazz.level == 0 && clazz.donor == null)
				.toArray(ClassType[]::new);
	}
	public static ClassType[] getTokenClasses(boolean includeVaulted) {
		return Arrays.stream(values())
				.filter(clazz -> (includeVaulted || !clazz.isVaulted) && clazz.tokenCost > 0)
				.toArray(ClassType[]::new);
	}
	public static ClassType[] getLevelClasses(boolean includeVaulted) {
		return Arrays.stream(values())
				.filter(clazz -> (includeVaulted || !clazz.isVaulted) && clazz.level > 0)
				.toArray(ClassType[]::new);
	}
	public static ClassType[] getDonorClasses(boolean includeVaulted) {
		return Arrays.stream(values())
				.filter(clazz -> (includeVaulted || !clazz.isVaulted) && clazz.donor != null)
				.toArray(ClassType[]::new);
	}

	public List<String> buildDescription() {
		String text = getClassDesc();
		if (text == null)
			return null;
		final int maxLength = 30;

		String[] split = text.split(" ");
		List<String> lines = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (String word : split) {
			if (current.length() + word.length() + 1 <= maxLength) {
				current.append(word).append(' ');
			} else {
				if (current.length() > 0)
					lines.add(ChatColor.GRAY + current.substring(0, current.length() - 1));
				current = new StringBuilder(word).append(' ');
			}
		}
		if (current.length() > 0)
			lines.add(ChatColor.GRAY + current.substring(0, current.length() - 1));
		return lines;
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}

	public int getID() {
		return id;
	}
	public int getTokenCost() {
		return tokenCost;
	}
	public int getLevel() {
		return level;
	}
	public Rank getMinRank() {
		return donor;
	}
	public boolean isVaulted() {
		return isVaulted;
	}
}