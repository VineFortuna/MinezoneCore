package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class SatermelonClass extends BaseClass {

	private Cooldown shurikenCooldown = new Cooldown(200);

	public SatermelonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.4;
	}

	public ItemStack makeGreen(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.GREEN);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("AnthonyFortuna");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getWatermelon() {
		return ItemHelper.setDetails(new ItemStack(Material.MELON, 1), "" + ChatColor.RESET + "#yolo (Right Click)",
				ChatColor.YELLOW + "", "Right click for special feature!");
	}

	@Override
	public void SetItems(Inventory playerInv) {
		ItemStack melon = getWatermelon();
		melon.setAmount(1);
		playerInv.setItem(0, melon);
	}

	public ItemStack getMelon() {
		return ItemHelper
				.addEnchant(
						ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.MELON_BLOCK, 1),
								"" + ChatColor.RESET + "Melon Block"), Enchantment.DAMAGE_ALL, 3),
						Enchantment.KNOCKBACK, 1);
	}

	public ItemStack getSoup() {
		return ItemHelper.setDetails(new ItemStack(Material.MUSHROOM_SOUP, 1), "" + ChatColor.RESET + "Mushroom Soup");
	}

	public ItemStack getBeacon() {
		return ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.BEACON, 1), "" + ChatColor.RESET + "Beacon"),
				Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
	}

	public ItemStack getRegenerators() {
		return ItemHelper.setDetails(new ItemStack(Material.INK_SACK, 1),
				"" + ChatColor.RESET + ChatColor.GREEN + "Regenerators");
	}

	public ItemStack getGlisteringMelon() {
		return ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.SPECKLED_MELON, 1),
				"" + ChatColor.RESET + "Special Melon"), Enchantment.DAMAGE_ALL, 4);
	}

	public ItemStack getGold() {
		return ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.GOLD_NUGGET, 1), "" + ChatColor.RESET + "Gold Nugget"),
				Enchantment.KNOCKBACK, 3);
	}

	public void PermItems() {
		Random rand = new Random();

		int chance = rand.nextInt(3);
		if (chance == 0) {
			ItemStack melon = getMelon();
			melon.setAmount(1);
			player.getInventory().setItem(0, melon);

			ItemStack soup = getSoup();
			soup.setAmount(1);
			player.getInventory().setItem(1, soup);
		} else if (chance == 1) {
			ItemStack beacon = getBeacon();
			beacon.setAmount(1);
			player.getInventory().setItem(0, beacon);

			ItemStack regen = getRegenerators();
			regen.setAmount(3);
			player.getInventory().setItem(1, regen);
		} else if (chance == 2) {
			ItemStack melon2 = getGlisteringMelon();
			melon2.setAmount(1);
			player.getInventory().setItem(0, melon2);

			ItemStack nugget = getGold();
			nugget.setAmount(1);
			player.getInventory().setItem(1, nugget);
		} else {
			return;
		}
	}

	public void SoupEffect() {
		Random rand = new Random();

		int chance = rand.nextInt(5);
		if (chance == 0) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 0));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "ABSORPTION I");
		} else if (chance == 1) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 800, 3));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "SPEED IV");
		} else if (chance == 2) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 300, 0));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "STRENGTH I");
		} else if (chance == 3) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 1));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "RESISTANCE II");
		} else if (chance == 4) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 600, 1));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "HEALTH BOOST II");
		} else {
			return;
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.MELON
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (shurikenCooldown.useAndResetCooldown()) {
				int amount = item.getAmount();
				if (amount > 0) {
					amount--;
					if (amount == 0)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(amount);
				}
				event.setCancelled(true);
			}
			PermItems();
		} else if (item != null && item.getType() == Material.MUSHROOM_SOUP
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			player.playSound(player.getLocation(), Sound.EAT, 1, 2);
			if (shurikenCooldown.useAndResetCooldown()) {
				int amount = item.getAmount();
				if (amount > 0) {
					amount--;
					if (amount == 0)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(amount);
				}
				event.setCancelled(true);
			}
			SoupEffect();
		} else if (item != null && item.getType() == Material.INK_SACK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			player.playSound(player.getLocation(), Sound.EAT, 1, 2);
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0));
			if (shurikenCooldown.useAndResetCooldown()) {
				int amount = item.getAmount();
				if (amount > 0) {
					amount--;
					if (amount == 0)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(amount);
				}
				event.setCancelled(true);
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Melon;
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
