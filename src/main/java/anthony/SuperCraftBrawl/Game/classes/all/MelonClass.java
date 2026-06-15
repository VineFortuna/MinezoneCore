package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import net.md_5.bungee.api.ChatColor;

public class MelonClass extends BaseClass {

	private Cooldown shurikenCooldown = new Cooldown(200);

	public MelonClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA5MmQ3NjBjNDUzNjI1NTk0NjYyYzlmYzg2ODE1MmEwMWExZjZmOGQ2MTM3ZmI4NjhkYTVhOTViYmQxZjU4In19fQ==",
				6,
				"B3B82E",
				"B3B82E",
				"B3B82E"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	public ItemStack getWatermelon() {
        ItemStack melon = ItemHelper.setDetails(new ItemStack(Material.MELON),
                instance.color("&r#yolo &7(Right Click)"),
                "",
                instance.color("&7Right click for special abilities!"));
		return melon;
	}

	@Override
	public void SetItems(Inventory playerInv) {
		ItemStack melon = getWatermelon();
		melon.setAmount(1);
		playerInv.setItem(0, melon);
	}

	public ItemStack getMelon() {
        ItemStack melon = ItemHelper
                .addEnchant(
                        ItemHelper.addEnchant(new ItemStack(Material.MELON_BLOCK, 1),
                                Enchantment.DAMAGE_ALL, 3),
                        Enchantment.KNOCKBACK, 1);
		return melon;
	}

	public ItemStack getSoup() {
        ItemStack soup = ItemHelper.setDetails(new ItemStack(Material.MUSHROOM_SOUP, 1),
                instance.color("&rMushroom Soup &7(Right Click)"),
                "",
                instance.color("&7Right click for one of the following:"),
                instance.color("&r- &e&lABSORPTION I"),
                instance.color("&r- &e&lAHEALTH BOOST II"),
                instance.color("&r- &4&lSTRENGTH I"),
                instance.color("&r- &7&lRESISTANCE I"),
                instance.color("&r- &b&lSPEED III"));
		return soup;
	}

	public ItemStack getBeacon() {
        ItemStack beacon = ItemHelper.addEnchant(ItemHelper.addEnchant(
                new ItemStack(Material.BEACON, 1),
                Enchantment.DAMAGE_ALL, 3),
                Enchantment.KNOCKBACK, 1);
		return beacon;
	}

	public ItemStack getRegenerators() {
        ItemStack regenerators = ItemHelper.setDetails(new ItemStack(Material.INK_SACK, 1),
                instance.color("&aRegenerators"),
                "",
                instance.color("&7Right click to gain:"),
                instance.color("&c&lREGEN II &a- 5s"));
		return regenerators;
	}

	public ItemStack getGlisteringMelon() {
        ItemStack specialMelon = ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.SPECKLED_MELON, 1),
                instance.color("&aSpecial Melon")),
                Enchantment.DAMAGE_ALL, 4);
		return specialMelon;
	}

	public ItemStack getGold() {
        ItemStack goldNugget = ItemHelper.addEnchant(
                new ItemStack(Material.GOLD_NUGGET, 1),
                Enchantment.KNOCKBACK, 3);
		return goldNugget;
	}

	public void PermItems() {
		Random rand = new Random();

		int chance = rand.nextInt(3);
		if (chance == 0) {
			ItemStack melon = getMelon();
			melon.setAmount(1);
			player.getInventory().addItem(melon);

			ItemStack soup = getSoup();
			soup.setAmount(1);
			player.getInventory().addItem(soup);
		} else if (chance == 1) {
			ItemStack beacon = getBeacon();
			beacon.setAmount(1);
			player.getInventory().addItem(beacon);

			ItemStack regen = getRegenerators();
			regen.setAmount(3);
			player.getInventory().addItem(regen);
		} else if (chance == 2) {
			ItemStack melon2 = getGlisteringMelon();
			melon2.setAmount(1);
			player.getInventory().addItem(melon2);

			ItemStack nugget = getGold();
			nugget.setAmount(1);
			player.getInventory().addItem(nugget);
		} else {
			return;
		}
	}

	public void SoupEffect() {
		Random rand = new Random();

		int chance = rand.nextInt(5);
		if (chance == 0) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 999999999, 0));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "ABSORPTION I");
		} else if (chance == 1) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "SPEED III");
		} else if (chance == 2) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999999, 0));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "STRENGTH I");
		} else if (chance == 3) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "RESISTANCE I");
		} else if (chance == 4) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 999999999, 1));
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
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 110, 1));
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
