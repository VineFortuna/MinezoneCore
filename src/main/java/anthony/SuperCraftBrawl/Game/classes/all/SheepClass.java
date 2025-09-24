package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Score;

import java.util.Random;

public class SheepClass extends BaseClass {

	private boolean green = false, gray = false, black = false, pink = false, lime = false;
	int lastWool = -1;

	public SheepClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTIyYWIwNTA4OTAwNjRlMjhhOWY0MDFiMmZjYjgyOThlODI0OWMzYTlmOWI2MGVkZmEwMDc4YzRiMDI1YjllMyJ9fX0=",
				"FFFFFF",
				"FFFFFF",
				"B59984",
				6,
				"Sheep"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	public ItemStack getStartWool() {
		player.setDisplayName(
				player.getName() + " " + ChatColor.RESET + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
		return ItemHelper.setDetails(new ItemStack(Material.WOOL, 1), ChatColor.RESET + "White Wool");
	}

	public ItemStack getStartEnchanter() {
		return ItemHelper.setDetails(new ItemStack(Material.ENCHANTMENT_TABLE, 1),
				ChatColor.BLUE + "Wool Enchanter", ChatColor.YELLOW + "Right click!");
	}

	private ChatColor getTeamColor() {
		ChatColor c = ChatColor.RESET;

		if (instance.getMap() != null)
			return c;

		if (instance.team.get(player).equals("Blue"))
			c = ChatColor.BLUE;
		else if (instance.team.get(player).equals("Red"))
			c = ChatColor.RED;
		else if (instance.team.get(player).equals("Black"))
			c = ChatColor.BLACK;

		return c;
	}

	@Override
	public void SetItems(Inventory playerInv) {
		green = false; // To reset each life
		gray = false; // Also same
		lime = false;
		black = false;
		pink = false;
		// To reset Sheep's scoreboard color
		BaseClass bc = instance.classes.get(player);
		if (bc.getLives() > 0 && bc.getLives() != 5) {
			bc.score.getScoreboard().resetScores(bc.score.getEntry());
			Score newScore = instance.livesObjective.getScore(instance
					.truncateString(instance.getGameManager().getMain().color("&r&lSheep &r" + player.getName()), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		}
		resetArmor();
		
		ItemStack whiteWool = getStartWool();
		playerInv.setItem(0, whiteWool);
		ItemStack enchanter = getStartEnchanter();
		playerInv.setItem(1, enchanter);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;

		if (green) {
			if (event.getEntity() instanceof LivingEntity) {
				((LivingEntity) event.getEntity())
						.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 70, 0, true));
			}
		} else if (gray) {
			if (event.getEntity() instanceof LivingEntity) {
				Random r = new Random();
				int chance = r.nextInt(5);
				if (chance == 1 || chance == 3 || chance == 0)
					((LivingEntity) event.getEntity())
							.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 65, 1, true));
			}
		} else if (black) {
			if (event.getEntity() instanceof LivingEntity) {
				Random r = new Random();
				int chance = r.nextInt(5);
				if (chance == 1 || chance == 3 || chance == 0)
					((LivingEntity) event.getEntity())
							.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 55, 1, true));
			}
		} else if (pink) {
			Random r = new Random();
			int chance = r.nextInt(5);

			if (chance == 0 || chance == 1)
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1, true));
		} else if (lime) {
			if (event.getEntity() instanceof LivingEntity) {
				Random r = new Random();
				int chance = r.nextInt(5);
				if (chance == 1 || chance == 3 || chance == 0)
					((LivingEntity) event.getEntity())
							.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 2, true));
			}
		}
	}

	public void Items() {
		green = false; // To reset each life
		gray = false; // Also same
		black = false; // Also also same lol
		pink = false; // ALSO SAME LOL!!!!
		lime = false; // AHHHHH!!!!!!!!!!

		ItemStack item3 = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()),
						"" + ChatColor.RED + ChatColor.BOLD + "Red Wool"), Enchantment.FIRE_ASPECT, 1),
				Enchantment.DAMAGE_ALL, 3);

		ItemStack item7 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()),
								"" + ChatColor.BLACK + ChatColor.BOLD + "Black Wool", "",
								instance.getGameManager().getMain().color("&7Black Wool ability:"),
								instance.getGameManager().getMain().color("   &r3 sec Blindness II")),
						Enchantment.DAMAGE_ALL, 4), Enchantment.KNOCKBACK, 1);

		ItemStack pinkWool = ItemHelper
				.addEnchant(
						ItemHelper
								.addEnchant(
										ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getData()),
												instance.getGameManager().getMain().color("&9&lPink Wool"), "",
												instance.getGameManager().getMain().color("&7Pink Wool ability:"),
												instance.getGameManager().getMain()
														.color("   &r3 sec Regen I chance on hit")),
										Enchantment.DAMAGE_ALL, 3),
						Enchantment.KNOCKBACK, 1);

		ItemStack limeWool = ItemHelper
				.addEnchant(
						ItemHelper
								.addEnchant(
										ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData()),
												instance.getGameManager().getMain().color("&2&lLime Wool"), "",
												instance.getGameManager().getMain().color("&7Lime Wool ability:"),
												instance.getGameManager().getMain()
														.color("   &r3 sec Nausea chance on hit")),
										Enchantment.DAMAGE_ALL, 4),
						Enchantment.KNOCKBACK, 1);

		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()),
						"" + ChatColor.BLUE + ChatColor.BOLD + "Blue Wool"), Enchantment.KNOCKBACK, 3),
				Enchantment.DAMAGE_ALL, 3);

		ItemStack item4 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.PURPLE.getData()),
								"" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Purple Wool"),
						Enchantment.KNOCKBACK, 6), Enchantment.DAMAGE_ALL, 4);

		ItemStack item5 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData()),
								"" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Green Wool", "",
								instance.getGameManager().getMain().color("&7Green Wool ability:"),
								instance.getGameManager().getMain().color("   &r3 sec Poison I")),
						Enchantment.KNOCKBACK, 1), Enchantment.DAMAGE_ALL, 3);

		ItemStack item6 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.GRAY.getData()),
								"" + ChatColor.GRAY + ChatColor.BOLD + "Gray Wool", "",
								instance.getGameManager().getMain().color("&7Gray Wool ability:"),
								instance.getGameManager().getMain().color("   &r3 sec Slowness II")),
						Enchantment.KNOCKBACK, 2), Enchantment.DAMAGE_ALL, 3);

		ItemStack[] itemList = { item3, item, item4, item3, item, item3, item, item3, item, item3, item, item3, item,
				item3, item, item3, item, item3, item, item5, item5, item5, item5, item5, item5, item5, item6, item6,
				item6, item6, item6, item7, item7, item7, item7, item7, item7, pinkWool, pinkWool, pinkWool, pinkWool,
				limeWool, limeWool, limeWool, limeWool };
		Random rand = new Random();
		int randomNum = rand.nextInt(itemList.length);
		if (lastWool >= 0) {
			while (itemList[randomNum].isSimilar(itemList[lastWool])) {
				randomNum = rand.nextInt(itemList.length);
			}
		}
		lastWool = randomNum;

		BaseClass bc2 = instance.classes.get(player);
		bc2.score.getScoreboard().resetScores(bc2.score.getEntry());
		player.playSound(player.getLocation(), Sound.SHEEP_IDLE, 1, 1);
		
		String color = "FFFFFF";
		if (itemList[randomNum] == item3) { // RED
			color = "FF0000";
			player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.RED
					+ ChatColor.BOLD + "RED WOOL");
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &c&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.RED
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " " + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		} else if (itemList[randomNum] == item) { // BLUE
			color = "FF";
			player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.BLUE
					+ ChatColor.BOLD + "BLUE WOOL");
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &9&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.BLUE
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " " + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		} else if (itemList[randomNum] == item7) { // BLACK
			color = "0";
			player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.BLACK
					+ ChatColor.BOLD + "BLACK WOOL");
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &0&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.BLACK
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " " + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			black = true;
		} else if (itemList[randomNum] == item4) { // PURPLE
			color = "800080";
			player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given "
					+ ChatColor.DARK_PURPLE + ChatColor.BOLD + "PURPLE WOOL");
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &5&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.DARK_PURPLE
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " " + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		} else if (itemList[randomNum] == item5) { // GREEN
			color = "8000";
			player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.DARK_GREEN
					+ ChatColor.BOLD + "GREEN WOOL");
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &2&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.DARK_GREEN
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " " + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			green = true;
		} else if (itemList[randomNum] == item6) { // GRAY
			color = "808080";
			player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.GRAY
					+ ChatColor.BOLD + "GRAY WOOL");
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &7&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.GRAY
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " " + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			gray = true;
		} else if (itemList[randomNum] == pinkWool) { // PINK
			color = "FF69B4";
			player.sendMessage(instance.getGameManager().getMain().color("&r&l(!) &rYou were given &d&lPINK WOOL"));
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &d&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.LIGHT_PURPLE
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " " + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			pink = true;
		} else if (itemList[randomNum] == limeWool) { // LIME
			color = "FF00";
			player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.GREEN
					+ ChatColor.BOLD + "LIME WOOL");
			player.setDisplayName(instance.getGameManager().getMain().color(player.getName() + " &a&lSheep&r"));
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.GREEN
					+ ChatColor.BOLD + "Sheep" + ChatColor.RESET + " "  + getTeamColor() + player.getName(), 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			lime = true;
		}
		chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, color, "&rSheep Chestplate");
		leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, color, "&rSheep Leggings");
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);

		player.getInventory().setItem(0, new ItemStack(itemList[randomNum]));
	}
	
	public void resetArmor() {
		String color = "FFFFFF";
		chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, color, "&rSheep Chestplate");
		leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, color, "&rSheep Leggings");
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
	}
	
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.ENCHANTMENT_TABLE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			int amount = item.getAmount();
			if (amount > 0) {
				amount--;
				if (amount == 0)
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				else
					item.setAmount(amount);
				event.setCancelled(true);
				Items();
			}
		}

	}

	@Override
	public ClassType getType() {
		return ClassType.Sheep;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = player.getInventory().getItem(0);
		return item;
	}

}
