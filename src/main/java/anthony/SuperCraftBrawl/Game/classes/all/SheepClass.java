package anthony.SuperCraftBrawl.Game.classes.all;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Score;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class SheepClass extends BaseClass {

	private boolean green = false, gray = false, black = false;

	public SheepClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
	}

	public ItemStack setArmour(ItemStack armour, Color c) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(c);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String skullOwner = "e8c98d01-1b7d-5934-9dfc-005a43f86ac9";
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTIyYWIwNTA4OTAwNjRlMjhhOWY0MDFiMmZjYjgyOThlODI0OWMzYTlmOWI2MGVkZmEwMDc4YzRiMDI1YjllMyJ9fX0=";
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		GameProfile profile = new GameProfile(UUID.fromString(skullOwner), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		Field profileField = null;

		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		skull.setItemMeta(meta);
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(setArmour(new ItemStack(Material.LEATHER_CHESTPLATE), Color.WHITE));
		playerEquip.setLeggings(setArmour(new ItemStack(Material.LEATHER_LEGGINGS), Color.WHITE));
		playerEquip.setBoots(setArmour(new ItemStack(Material.LEATHER_BOOTS), Color.WHITE));
	}

	public ItemStack getStartWool() {
		player.setDisplayName(
				"" + player.getName() + " " + ChatColor.RESET + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
		return ItemHelper.setDetails(new ItemStack(Material.WOOL, 1), "" + ChatColor.RESET + "White Wool",
				ChatColor.YELLOW + "Right click!");
	}

	public ItemStack getStartEnchanter() {
		return ItemHelper.setDetails(new ItemStack(Material.ENCHANTMENT_TABLE, 1),
				"" + ChatColor.BLUE + "Wool Enchanter", ChatColor.YELLOW + "Right click!");
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
		// To reset Sheep's scoreboard color
		BaseClass bc = instance.classes.get(player);
		if (bc.getLives() > 0 && bc.getLives() != 5) {
			bc.score.getScoreboard().resetScores(bc.score.getEntry());
			Score newScore = instance.livesObjective.getScore(instance
					.truncateString("" + bc.getType().getTag() + " " + getTeamColor() + player.getName() + "", 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		}

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

		if (green == true) {
			if (event.getEntity() instanceof LivingEntity) {
				((LivingEntity) event.getEntity())
						.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 70, 0, true));
			}
		} else if (gray == true) {
			if (event.getEntity() instanceof LivingEntity) {
				Random r = new Random();
				int chance = r.nextInt(5);
				if (chance == 1 || chance == 3 || chance == 0)
					((LivingEntity) event.getEntity())
							.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 65, 1, true));
			}
		} else if (black == true) {
			if (event.getEntity() instanceof LivingEntity) {
				Random r = new Random();
				int chance = r.nextInt(5);
				if (chance == 1 || chance == 3 || chance == 0)
					((LivingEntity) event.getEntity())
							.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 55, 1, true));
			}
		}
	}

	public void Items() {
		green = false; // To reset each life
		gray = false; // Also same
		black = false; // Also also same lol
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("DerpTheSheep");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		ItemStack item3 = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()),
						"" + ChatColor.RED + ChatColor.BOLD + "Red Wool"), Enchantment.FIRE_ASPECT, 1),
				Enchantment.DAMAGE_ALL, 3);

		ItemStack item7 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()),
								"" + ChatColor.BLACK + ChatColor.BOLD + "Black Wool", "",
								instance.getManager().getMain().color("&7Black Wool ability:"),
								instance.getManager().getMain().color("   &r3 sec Blindness II")),
						Enchantment.DAMAGE_ALL, 4), Enchantment.KNOCKBACK, 1);

		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData()),
						"" + ChatColor.BLUE + ChatColor.BOLD + "Blue Wool"), Enchantment.KNOCKBACK, 2),
				Enchantment.DAMAGE_ALL, 3);

		ItemStack item4 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.PURPLE.getData()),
								"" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Purple Wool"),
						Enchantment.KNOCKBACK, 6), Enchantment.DAMAGE_ALL, 4);

		ItemStack item5 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData()),
								"" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Green Wool", "",
								instance.getManager().getMain().color("&7Green Wool ability:"),
								instance.getManager().getMain().color("   &r3 sec Poison I")),
						Enchantment.KNOCKBACK, 1), Enchantment.DAMAGE_ALL, 3);

		ItemStack item6 = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.GRAY.getData()),
								"" + ChatColor.GRAY + ChatColor.BOLD + "Gray Wool", "",
								instance.getManager().getMain().color("&7Gray Wool ability:"),
								instance.getManager().getMain().color("   &r3 sec Slowness II")),
						Enchantment.KNOCKBACK, 2), Enchantment.DAMAGE_ALL, 3);

		ItemStack[] itemList = { item3, item, item4, item3, item, item3, item, item3, item, item3, item, item3, item,
				item3, item, item3, item, item3, item, item5, item5, item5, item5, item5, item5, item5, item6, item6,
				item6, item6, item6, item7, item7, item7, item7, item7, item7 };
		Random rand = new Random();
		int randomNum = rand.nextInt(itemList.length);

		BaseClass bc2 = instance.classes.get(player);
		bc2.score.getScoreboard().resetScores(bc2.score.getEntry());

		if (itemList[randomNum] == item3) {
			player.getInventory()
					.setChestplate(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
							Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.RED));
			player.getInventory().setLeggings(setArmour(new ItemStack(Material.LEATHER_LEGGINGS), Color.RED));
			player.getInventory().setBoots(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS),
					Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.RED));

			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.RED
					+ ChatColor.BOLD + "RED WOOL");
			player.setDisplayName(
					"" + player.getName() + " " + ChatColor.RED + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.RED
					+ ChatColor.BOLD + bc.getType().getTag() + " " + getTeamColor() + player.getName() + "", 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		} else if (itemList[randomNum] == item) {
			player.getInventory()
					.setChestplate(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
							Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.BLUE));
			player.getInventory().setLeggings(setArmour(new ItemStack(Material.LEATHER_LEGGINGS), Color.BLUE));
			player.getInventory().setBoots(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS),
					Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.BLUE));

			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.BLUE
					+ ChatColor.BOLD + "BLUE WOOL");
			player.setDisplayName(
					"" + player.getName() + " " + ChatColor.BLUE + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.BLUE
					+ ChatColor.BOLD + bc.getType().getTag() + " " + getTeamColor() + player.getName() + "", 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		} else if (itemList[randomNum] == item7) {
			player.getInventory()
					.setChestplate(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
							Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.BLACK));
			player.getInventory().setLeggings(setArmour(new ItemStack(Material.LEATHER_LEGGINGS), Color.BLACK));
			player.getInventory().setBoots(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS),
					Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.BLACK));

			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.BLACK
					+ ChatColor.BOLD + "BLACK WOOL");
			player.setDisplayName(
					"" + player.getName() + " " + ChatColor.BLUE + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.BLACK
					+ ChatColor.BOLD + bc.getType().getTag() + " " + getTeamColor() + player.getName() + "", 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			black = true;
		} else if (itemList[randomNum] == item4) {
			player.getInventory()
					.setChestplate(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
							Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.PURPLE));
			player.getInventory().setLeggings(setArmour(new ItemStack(Material.LEATHER_LEGGINGS), Color.PURPLE));
			player.getInventory().setBoots(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS),
					Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.PURPLE));

			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given "
					+ ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "PURPLE WOOL");
			player.setDisplayName(
					"" + player.getName() + " " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.LIGHT_PURPLE
					+ ChatColor.BOLD + bc.getType().getTag() + " " + getTeamColor() + player.getName() + "", 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
		} else if (itemList[randomNum] == item5) {
			player.getInventory()
					.setChestplate(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
							Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.GREEN));
			player.getInventory().setLeggings(setArmour(new ItemStack(Material.LEATHER_LEGGINGS), Color.GREEN));
			player.getInventory().setBoots(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS),
					Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.GREEN));

			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.DARK_GREEN
					+ ChatColor.BOLD + "GREEN WOOL");
			player.setDisplayName(
					"" + player.getName() + " " + ChatColor.DARK_GREEN + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.DARK_GREEN
					+ ChatColor.BOLD + bc.getType().getTag() + " " + getTeamColor() + player.getName() + "", 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			green = true;
		} else if (itemList[randomNum] == item6) {
			player.getInventory()
					.setChestplate(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
							Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.GRAY));
			player.getInventory().setLeggings(setArmour(new ItemStack(Material.LEATHER_LEGGINGS), Color.GRAY));
			player.getInventory().setBoots(setArmour(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS),
					Enchantment.PROTECTION_ENVIRONMENTAL, 4), Color.GRAY));

			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were given " + ChatColor.GRAY
					+ ChatColor.BOLD + "GRAY WOOL");
			player.setDisplayName(
					"" + player.getName() + " " + ChatColor.GRAY + ChatColor.BOLD + "Sheep" + ChatColor.RESET);
			BaseClass bc = instance.classes.get(player);
			Score newScore = instance.livesObjective.getScore(instance.truncateString("" + ChatColor.GRAY
					+ ChatColor.BOLD + bc.getType().getTag() + " " + getTeamColor() + player.getName() + "", 40));
			bc.score = newScore;
			newScore.setScore(bc.getLives());
			gray = true;
		}

		player.getInventory().setItem(0, new ItemStack(itemList[randomNum]));
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
