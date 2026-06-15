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
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Score;

import java.util.Random;

public class SheepClass extends BaseClass {

	private WoolType currentWool = WoolType.WHITE;
	private int lastWool = -1;
	private ItemStack weapon;

	public SheepClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.0;
		createArmor(
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTIyYWIwNTA4OTAwNjRlMjhhOWY0MDFiMmZjYjgyOThlODI0OWMzYTlmOWI2MGVkZmEwMDc4YzRiMDI1YjllMyJ9fX0=",
				6,
				"FFFFFF",
				"FFFFFF",
				"B59984"
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
		currentWool = WoolType.WHITE; // To reset each life

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
		
		weapon = getStartWool();
		playerInv.setItem(0, weapon);
		ItemStack enchanter = getStartEnchanter();
		playerInv.setItem(1, enchanter);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (!(event.getEntity() instanceof Player)) return;

		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
						&& heldItem != null
						&& heldItem.equals(weapon);

		if (!isWeaponMelee) return;

		Player target = (Player) event.getEntity();

		Random r = new Random();
		int chance = r.nextInt(5);

		switch (currentWool) {
			case GREEN:
				target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 70, 0, true));
				break;
			case GRAY:
				if (chance < 3)
					target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 65, 1, true));
				break;
			case BLACK:
				if (chance < 3)
					target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 55, 1, true));
				break;
			case PINK:
				if (chance < 2)
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 70, 1, true));
				break;
			case LIME:
				if (chance < 3)
					target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 2, true));
				break;
			default:
				break;
		}
	}

	private void setWeapon() {

	}

	public void Items() {
		ItemStack redWool = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()),
						"" + ChatColor.RED + ChatColor.BOLD + "Red Wool"), Enchantment.FIRE_ASPECT, 1),
				Enchantment.DAMAGE_ALL, 3);

		ItemStack blackWool = ItemHelper
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
												instance.getGameManager().getMain().color("&d&lPink Wool"), "",
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

		ItemStack blueWool = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()),
						"" + ChatColor.BLUE + ChatColor.BOLD + "Blue Wool"), Enchantment.KNOCKBACK, 3),
				Enchantment.DAMAGE_ALL, 3);

		ItemStack purpleWool = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.PURPLE.getData()),
								"" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "Purple Wool"),
						Enchantment.KNOCKBACK, 6), Enchantment.DAMAGE_ALL, 4);

		ItemStack greenWool = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData()),
								"" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Green Wool", "",
								instance.getGameManager().getMain().color("&7Green Wool ability:"),
								instance.getGameManager().getMain().color("   &r3 sec Poison I")),
						Enchantment.KNOCKBACK, 1), Enchantment.DAMAGE_ALL, 3);

		ItemStack grayWool = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.GRAY.getData()),
								"" + ChatColor.GRAY + ChatColor.BOLD + "Gray Wool", "",
								instance.getGameManager().getMain().color("&7Gray Wool ability:"),
								instance.getGameManager().getMain().color("   &r3 sec Slowness II")),
						Enchantment.KNOCKBACK, 2), Enchantment.DAMAGE_ALL, 3);

		ItemStack[] itemList = { redWool, blueWool, purpleWool, redWool, blueWool, redWool, blueWool, redWool, blueWool, redWool, blueWool, redWool, blueWool,
				redWool, blueWool, redWool, blueWool, redWool, blueWool, greenWool, greenWool, greenWool, greenWool, greenWool, greenWool, greenWool, grayWool, grayWool,
				grayWool, grayWool, grayWool, blackWool, blackWool, blackWool, blackWool, blackWool, blackWool, pinkWool, pinkWool, pinkWool, pinkWool,
				limeWool, limeWool, limeWool, limeWool };

		Random rand = new Random();
		int randomNum = rand.nextInt(itemList.length);
		if (lastWool >= 0) {
			while (itemList[randomNum].isSimilar(itemList[lastWool])) {
				randomNum = rand.nextInt(itemList.length);
			}
		}
		lastWool = randomNum;

		BaseClass bc = instance.classes.get(player);
		bc.score.getScoreboard().resetScores(bc.score.getEntry());
		player.playSound(player.getLocation(), Sound.SHEEP_IDLE, 1, 1);
		
		WoolType type;
		if (itemList[randomNum] == redWool)
			type = WoolType.RED;
		else if (itemList[randomNum] == blueWool)
			type = WoolType.BLUE;
		else if (itemList[randomNum] == blackWool)
			type = WoolType.BLACK;
		else if (itemList[randomNum] == purpleWool)
			type = WoolType.PURPLE;
		else if (itemList[randomNum] == greenWool)
			type = WoolType.GREEN;
		else if (itemList[randomNum] == grayWool)
			type = WoolType.GRAY;
		else if (itemList[randomNum] == pinkWool)
			type = WoolType.PINK;
		else
			type = WoolType.LIME;

		switchWoolType(type, new ItemStack(itemList[randomNum]));
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
		return weapon;
	}

	public enum WoolType {
		WHITE(ChatColor.WHITE, "FFFFFF"),
		RED(ChatColor.RED, "FF0000"),
		BLUE(ChatColor.BLUE, "0000FF"),
		PURPLE(ChatColor.DARK_PURPLE, "800080"),
		GREEN(ChatColor.DARK_GREEN, "00FF00"),
		GRAY(ChatColor.GRAY, "808080"),
		BLACK(ChatColor.BLACK, "000000"),
		PINK(ChatColor.LIGHT_PURPLE, "FF69B4"),
		LIME(ChatColor.GREEN, "00FF00");

		private final ChatColor chatColor;
		private final String armorColor;

		WoolType(ChatColor chatColor, String armorColor) {
			this.chatColor = chatColor;
			this.armorColor = armorColor;
		}

		public ChatColor getChatColor() {
			return chatColor;
		}

		public String getArmorColor() {
			return armorColor;
		}
	}

	private void updateScoreboard() {
		BaseClass bc = instance.classes.get(player);

		ChatColor color = currentWool.getChatColor();

		String entry = instance.truncateString(
				"" + color + ChatColor.BOLD + "Sheep"
						+ ChatColor.RESET + " "
						+ getTeamColor() + player.getName(),
				40);

		Score newScore = instance.livesObjective.getScore(entry);
		bc.score = newScore;
		newScore.setScore(bc.getLives());
	}

	private void updateArmor() {
		String color = currentWool.getArmorColor();

		chestplate = ItemHelper.createColoredArmor(
				Material.LEATHER_CHESTPLATE,
				color,
				"&rSheep Chestplate");

		leggings = ItemHelper.createColoredArmor(
				Material.LEATHER_LEGGINGS,
				color,
				"&rSheep Leggings");

		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
	}

	private void switchWoolType(WoolType type, ItemStack item) {
		currentWool = type;

		ChatColor color = type.getChatColor();

		player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET
				+ "You were given " + color + ChatColor.BOLD + type.name() + " WOOL");

		updateScoreboard();
		updateArmor();
		weapon = item;

		player.getInventory().setItem(0, weapon);
	}
}
