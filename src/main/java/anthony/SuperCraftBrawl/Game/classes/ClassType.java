package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.classes.all.*;
import anthony.SuperCraftBrawl.ItemHelper;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.ranks.Rank;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum ClassType {

	Cactus(1, 0, 0), TNT(2, 350, 0), Enderdragon(3, 0, Rank.VIP), Skeleton(4, 0, 0), Ninja(5, 1000, 0),
	IronGolem(6, 0, Rank.VIP), Enderman(7, 0, 0), Ghast(8, 0, Rank.VIP), Chicken(9, 400, 0), Slime(10, 0, Rank.VIP),
	ButterGolem(11, 0, Rank.VIP), DarkSethBling(12, 800, 0), SnowGolem(14, 800, 0), Bat(15, 0, Rank.VIP),
	SethBling(16, 0, Rank.VIP), Sheep(17, 550, 0), Horse(18, 0, 0), Melon(19, 0, Rank.VIP), Rabbit(26, 0, 0),
	Squid(20, 0, 0), Spider(21, 0, 0), BabyCow(22, 0, Rank.VIP), Herobrine(23, 0, Rank.VIP), Bunny(24, 450, 0),
	ButterBro(25, 1200, 0), Steve(28, 1000, 0), Notch(29, 1000, 0), Pig(30, 0, 0), Blaze(31, 0, 0), Potato(32, 750, 0),
	Wither(33, 0, 0), Ocelot(34, 250, 0), Creeper(35, 0, 0), Noteblock(36, 800, 0), EnchantTable(37, 350, 0),
	Present(38, 0, 0), Wizard(41, 0, Rank.VIP), Star(42, 850, 0), Dweller(43, 0, 0), Zombie(44, 0, 0),
	Silverfish(45, 0, 0), Anvil(46, 700, 0), Summoner(47, 525, 0), MagmaCube(48, 0, Rank.VIP), Villager(49, 0, 0),
	ZombiePigman(51, 0, 3), Witch(13, 0, 5), ZombieVillager(50, 0, 10), Ice(54, 0, 15), Vampire(53, 800, 0),
	Bee(55, 425, 0), Jeb(56, 0, 20), Hunter(57, 500, 0), FlintAndSteel(58, 0, 0), WitherSk(59, 1500, 0),
	Levitator(60, 0, 25), Bedrock(61, 0, Rank.VIP), Firework(62, 0, Rank.VIP), Cloud(63, 0, 30),
	LargeFern(64, 0, Rank.DEFAULT);

	// Wolf(63, 0, 35)/* , Guardian(63, 0, 30) */;

	private int id;
	private int tokenCost = 0;
	private int level = 0;
	private Rank donor;

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

	public Rank getMinRank() {
		return donor;
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

	public static ClassType fromID(int id) {
		for (ClassType ct : ClassType.values()) {
			if (ct.getID() == id) {
				return ct;
			}
		}
		return null;
	}

	public BaseClass GetClassInstance(GameInstance instance, Player player) {
		switch (this) {
		case Cactus:
			return new Cactus(instance, player);
		case Cloud:
			return new CloudClass(instance, player);
		case Firework:
			return new FireworkClass(instance, player);
		case Levitator:
			return new LevitatorClass(instance, player);
		case WitherSk:
			return new WitherSkeletonClass(instance, player);
		case Rabbit:
			return new RabbitClass(instance, player);
		case FlintAndSteel:
			return new FlindAndSteelClass(instance, player);
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
		case ButterGolem:
			return new ButterGolemClass(instance, player);
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
			return new SatermelonClass(instance, player);
		case ButterBro:
			return new ButterBroClass(instance, player);
		case Squid:
			return new SquidClass(instance, player);
		case Spider:
			return new SpiderClass(instance, player);
		case BabyCow:
			return new BabyCowClass(instance, player);
		case Bunny:
			return new BunnyClass(instance, player);
		case Pig:
			return new PigClass(instance, player);
		case LargeFern:
			return new LargeFernClass(instance, player);
// 		case Wolf:
// 			return new WolfClass(instance, player);
//		case Guardian:
//			return new GuardianClass(instance, player);
//		case Fluxty:
//			return new FluxtyClass(instance, player);
//		case Snowman:
//			return new SnowmanClass(instance, player);
		}
		return null;
	}

	public ItemStack getItem() {
		switch (this) {
		case Cactus:
			return new ItemStack(Material.CACTUS);
		case Cloud:
			return new ItemStack(Material.WOOL);
		case Firework:
			return new ItemStack(Material.FIREWORK);
		case Levitator:
			ItemStack item = new ItemStack(Material.STAINED_CLAY, 1, (byte) DyeColor.PURPLE.getData());
			return item;
		case Dweller:
			return new ItemStack(Material.BONE);
		case WitherSk:
			return new ItemStack(Material.EYE_OF_ENDER);
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
			ItemStack skullZombiePigman = ItemHelper.createSkullHeadPlayer(1, "ZombiePigMan");
			return skullZombiePigman;
		case Villager:
			return new ItemStack(Material.EMERALD_BLOCK);
		case DarkSethBling:
			return new ItemStack(Material.COAL_BLOCK);
		case ZombieVillager:
			return new ItemStack(Material.ROTTEN_FLESH);
		case MagmaCube:
			ItemStack playerskull2 = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta meta2 = (SkullMeta) playerskull2.getItemMeta();
			meta2.setOwner("MagmaCube");
			meta2.setDisplayName("");
			playerskull2.setItemMeta(meta2);
			return new ItemStack(playerskull2);
		case Summoner:
			return new ItemStack(Material.ENCHANTED_BOOK);
		case Anvil:
			return new ItemStack(Material.ANVIL);
		case Silverfish:
			return new ItemStack(Material.IRON_HOE);
		case Zombie:
			ItemStack skullZombie = ItemHelper.createSkullHead(1, SkullType.ZOMBIE);
			return skullZombie;
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
			return new ItemStack(Material.NETHER_STAR);
		case Creeper:
			ItemStack creeperSkull = ItemHelper.createSkullHead(1, SkullType.CREEPER);
			return creeperSkull;
		case IronGolem:
			return new ItemStack(new ItemStack(Material.IRON_AXE));
		case Ghast:
			return new ItemStack(new ItemStack(Material.GHAST_TEAR));
		case Slime:
			return new ItemStack(new ItemStack(Material.SLIME_BALL));
		case ButterGolem:
			return new ItemStack(new ItemStack(Material.GOLD_AXE));
		case Enderdragon:
			return new ItemStack(new ItemStack(Material.DRAGON_EGG));
		case Bat:
			return new ItemStack(new ItemStack(Material.SHEARS));
		case SethBling:
			return new ItemStack(new ItemStack(Material.REDSTONE_BLOCK));
		case Melon:
			return new ItemStack(new ItemStack(Material.MELON));
		case BabyCow:
			return new ItemStack(new ItemStack(Material.RED_MUSHROOM));
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
// 		case Wolf:
// 			return new ItemStack(Material.BONE);
// 		case Guardian:
// 			return new ItemStack(Material.PRISMARINE_SHARD);
//		case Snowman:
//			return new ItemStack(Material.PUMPKIN);
//		case Fluxty:
//			return new ItemStack(Material.LEATHER_CHESTPLATE);
		}

		return null;
	}

	public String getClassDesc() {
		switch (this) {
		case Cactus:
			return "A pricklyyy living thing, made up of thornws & blood..";
		case Cloud:
			return "Use your powers to send cool effects on your opponents!";
		case Firework:
			return "Shoot colorful fireworks and inflict different effects!";
		case Levitator:
			return "You want me? I want you baby, my sugar boo, Im levitating";
		case WitherSk:
			return "Shoot your withering blasts at other players!";
		case Rabbit:
			return "Hit your enemies and eventually obtain a strong weapon";
		case FlintAndSteel:
			return "Combo your flint & steel for a special reward";
		case Hunter:
			return "Damage your enemies to gain Blood Lust to get special enchants!";
		case Jeb:
			return "Notch vs. Jeb, who wins?";
		case Bee:
			return "Mama taught ya to sting so use it!";
		case Ice:
			return "Freeze thy enemies!!";
		case Vampire:
			return "Use your bow to poison your enemies!";
		case ZombiePigman:
			return "Summon your brothers to help you fight your enemies while you smack them with your Gold Sword!";
		// case WitherSk:
		// return "A very powerful character ready to dominate";
		case ZombieVillager:
			return "A poisonous creature..";
		case Villager:
			return "Toss your potatoes to confuse your enemies";
		case MagmaCube:
			return "Fairly high double jump and ability to spawn 7 MagmaCube minions";
		case Summoner:
			return "Beware the Summoner...";
		case Anvil:
			return "Goomba stomp your opponents!";
		case Silverfish:
			return "A very annoying creature ready to eat your enemies!";
		case Zombie:
			return "What do I even write here";
		case Dweller:
			return "Beware the Dweller of SCB!";
		case Star:
			return "Born in 1964 Jeffrey.. Jeffrey Besos";
		case Wizard:
			return "A Wizard with different spells!";
		case Present:
			return "Copy other people's item, armor, or effect/double jump!";
		case Bedrock:
			return "Tough to defeat am I right haha";
		case Noteblock:
			return "Play some songs for some awesome effects!";
		case EnchantTable:
			return "Get kills to get some awesome enchantments!";
		case Ocelot:
			return "Chase down your opponents with your high speed or Purr Attack!";
		case Creeper:
			return "Defeat your opponents with your explosive arsenal";
		case Potato:
			return "Who doesn't like potatoes?!";
		case Wither:
			return "Utilize your explosive skulls to defeat your enemies!";
		case Notch:
			return "The owner of Minecraft..";
		case Blaze:
			return "ITS A BLAZE LOL!";
		case Steve:
			return "OMG OMG GET HYPED!!!";
		case Skeleton:
			return "A long range shooter effective at taking down their targets";
		case Enderdragon:
			return "Jump higher than your opponents and teleport around!";
		case Enderman:
			return "Stare into the souls of your enemies whilst teleporting around them";
		case Horse:
			return "Nayyy!! Different effects = different powers!";
		case Squid:
			return "UNDA DA SEA! UNDA DA SEA!";
		case Spider:
			return "Bite and poison your enemies while fighting them!";
		case Ninja:
			return "Ninja 2.0 (idk xD)";
		case TNT:
			return "Blow up your enemies with TNT!";
		case Chicken:
			return "Bock bock backaaack! One of the best classes hehe tip";
		case DarkSethBling:
			return "The evil counterpart of the redstone King";
		case Witch:
			return "She lives in daydreams with me! (She)";
		case Sheep:
			return "Different colors of wool gives you different powers!";
		case SnowGolem:
			return "This is a SnowGolem, not a Snowman. Get it right pleb!";
		case Bunny:
			return "Easter Bunny is coming to town!";
		case ButterBro:
			return "Yo, you there Sky??";
		case IronGolem:
			return "Smack your enemies into the air while defending your village!";
		case Ghast:
			return "Burn down your enemies with your sorrows";
		case Slime:
			return "Throw sticky grenades and attack enemies!";
		case ButterGolem:
			return "Once a proud member of the Sky Army, the ButterGolem now stands as a relic of a bygone era..";
		case Bat:
			return "Dance around your opponents with SUPER high jumps!";
		case SethBling:
			return "The creator of SCB, wanna fight?!?!";
		case Melon:
			return "The Owner of the server in the game?!";
		case BabyCow:
			return "moo.. MOO!!";
		case Herobrine:
			return "Use your Diamond of Despair to play tricks on your opponents!";
		case Pig:
			return "Hit and run. In your panic, you gain speed when hit";
		case LargeFern:
			return "??????";
//		case Wolf:
//			return "Have your brothers defend you from enemies!";
//		case Guardian:
//			return "Guard thyself and thy family (or something idk)";
//		case Fluxty:
//			return: "We cannot have HATERS in the community.. So use your Wood Axe to kick em all out!";
//		case Snowman:
//			return "This is a Snowman, not a SnowGolem. Get it right pleb!";
		}

		return null;
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
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

	public String getTag() {
		switch (this) {
		case Bat:
			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Bat" + ChatColor.RESET;
		case Cloud:
			return "" + ChatColor.GRAY + ChatColor.ITALIC + "Cloud" + ChatColor.RESET;
		// case Wolf:
		// return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Wolf"
		// + ChatColor.RESET;
		// case Guardian:
		// return "" + ChatColor.GRAY + ChatColor.BOLD + "Guardian" + ChatColor.RESET;

		case Firework:
			return "" + ChatColor.RED + ChatColor.BOLD + ChatColor.ITALIC + "Firework" + ChatColor.RESET;
		case Bedrock:
			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Bedrock" + ChatColor.RESET;
		case Levitator:
			return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + ChatColor.ITALIC + "Levitator" + ChatColor.RESET;
		case WitherSk:
			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "WitherSk" + ChatColor.RESET;
		case FlintAndSteel:
			return "" + ChatColor.DARK_GRAY + "Flint" + ChatColor.GRAY + "&" + ChatColor.WHITE + "Steel"
					+ ChatColor.RESET;
		case Hunter:
			return "" + ChatColor.RED + ChatColor.BOLD + "Hunter" + ChatColor.RESET;
		case Jeb:
			return "" + ChatColor.GRAY + ChatColor.ITALIC + "Jeb" + ChatColor.RESET;
		case Bee:
			return "" + ChatColor.YELLOW + ChatColor.ITALIC + "Bee" + ChatColor.RESET;
		case Ice:
			return color("&b&lIce") + ChatColor.RESET;
		case Vampire:
			return "" + ChatColor.GRAY + ChatColor.BOLD + "Vampire" + ChatColor.RESET;
		case ZombiePigman:
			return "" + ChatColor.GREEN + ChatColor.ITALIC + "ZombiePigman" + ChatColor.RESET;
		case ZombieVillager:
			return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Zombie" + ChatColor.GRAY + ChatColor.BOLD + "Villager"
					+ ChatColor.RESET;
		case Villager:
			return "" + ChatColor.GREEN + "Villager" + ChatColor.RESET;
		case MagmaCube:
			return "" + ChatColor.DARK_RED + ChatColor.BOLD + "MagmaCube" + ChatColor.RESET;
		case Summoner:
			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Summoner" + ChatColor.RESET;
		case Anvil:
			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Anvil" + ChatColor.RESET;
		case Silverfish:
			return "" + ChatColor.GRAY + ChatColor.ITALIC + "Silverfish" + ChatColor.RESET;
		case Zombie:
			return "" + ChatColor.GRAY + ChatColor.ITALIC + "Zombie" + ChatColor.RESET;
		case Dweller:
			return "" + ChatColor.GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Dweller" + ChatColor.RESET;
		case Star:
			return "" + ChatColor.DARK_GRAY + "Star";
		case Wizard:
			return "" + ChatColor.RED + ChatColor.BOLD + "Wizard" + ChatColor.RESET;
		case Present:
			return "" + ChatColor.GOLD + ChatColor.BOLD + "Present" + ChatColor.RESET;
		case Wither:
			return "" + ChatColor.DARK_GRAY + ChatColor.ITALIC + "Wither" + ChatColor.RESET;
		case Noteblock:
			return "" + ChatColor.YELLOW + ChatColor.ITALIC + "Noteblock" + ChatColor.RESET;
		case EnchantTable:
			return "" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Enchant" + ChatColor.RED + ChatColor.BOLD + "Table"
					+ ChatColor.RESET;
		case Ocelot:
			return "" + ChatColor.YELLOW + ChatColor.BOLD + "Ocelot" + ChatColor.RESET;
		case Creeper:
			return "" + ChatColor.YELLOW + "Creeper" + ChatColor.RESET;
		case Notch:
			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Notch" + ChatColor.RESET;
		case Blaze:
			return "" + ChatColor.DARK_RED + "Blaze" + ChatColor.RESET;
		case Potato:
			return "" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Potato" + ChatColor.RESET;
		case Steve:
			return "" + ChatColor.AQUA + "Steve" + ChatColor.RESET;
		case ButterGolem:
			return "" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.ITALIC + "ButterGolem" + ChatColor.RESET;
		case Herobrine:
			return "" + ChatColor.GRAY + ChatColor.BOLD + "Herobrine" + ChatColor.RESET;
		case Cactus:
			return "" + ChatColor.DARK_GREEN + "Cactus" + ChatColor.RESET;
		case Chicken:
			return "" + ChatColor.YELLOW + ChatColor.BOLD + "Chicken" + ChatColor.RESET;
		case DarkSethBling:
			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "DarkSethBling" + ChatColor.RESET;
		case Enderdragon:
			return "" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Ender" + ChatColor.RESET + ChatColor.DARK_GRAY
					+ ChatColor.BOLD + "Dragon" + ChatColor.RESET;
		case Enderman:
			return "" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "Enderman" + ChatColor.RESET;
		case Ghast:
			return "" + ChatColor.RESET + ChatColor.BOLD + ChatColor.ITALIC + "Ghast" + ChatColor.RESET;
		case IronGolem:
			return "" + ChatColor.GRAY + ChatColor.BOLD + ChatColor.ITALIC + "IronGolem" + ChatColor.RESET;
		case Ninja:
			return "" + ChatColor.BLUE + ChatColor.BOLD + "Ninja" + ChatColor.RESET;
		case SethBling:
			return "" + ChatColor.RED + ChatColor.BOLD + ChatColor.ITALIC + "SethBling" + ChatColor.RESET;
		case Sheep:
			return "" + ChatColor.BOLD + "Sheep" + ChatColor.RESET;
		case Skeleton:
			return "" + ChatColor.GRAY + ChatColor.ITALIC + "Skeleton" + ChatColor.RESET;
		case Slime:
			return "" + ChatColor.GREEN + ChatColor.BOLD + "Slime" + ChatColor.RESET;
		case SnowGolem:
			return "" + ChatColor.RESET + ChatColor.BOLD + "SnowGolem" + ChatColor.RESET;
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
		case BabyCow:
			return "" + ChatColor.RED + ChatColor.ITALIC + ChatColor.BOLD + "BabyCow" + ChatColor.RESET;
		case Bunny:
			return "" + ChatColor.YELLOW + ChatColor.ITALIC + ChatColor.BOLD + "Bunny" + ChatColor.RESET;
		case ButterBro:
			return "" + ChatColor.YELLOW + ChatColor.BOLD + "ButterBro" + ChatColor.RESET;
		case Pig:
			return "" + ChatColor.BLUE + ChatColor.ITALIC + "Pig" + ChatColor.RESET;
		case LargeFern:
			return "" + ChatColor.DARK_GREEN + ChatColor.ITALIC + "LargeFern" + ChatColor.RESET;
// 		case Wolf:
// 			return "" + ChatColor.DARK_GRAY + ChatColor.BOLD + ChatColor.ITALIC + "Wolf" + ChatColor.RESET;
// 		case Guardian:
// 			return "" + ChatColor.GRAY + ChatColor.BOLD + "Guardian" + ChatColor.RESET;
// 		case Snowman:
// 			return "" + ChatColor.RESET + "Snow" + ChatColor.DARK_GREEN + "Man" + ChatColor.RESET;
// 		case Fluxty:
// 			return "" + ChatColor.GREEN + ChatColor.BOLD + "Fluxty" + ChatColor.RESET;

		default:
			break;
		}
		return this.toString();
	}
}