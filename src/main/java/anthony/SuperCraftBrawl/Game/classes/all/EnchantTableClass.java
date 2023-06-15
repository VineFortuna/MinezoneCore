package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class EnchantTableClass extends BaseClass {

	private ItemStack woodSword = ItemHelper.addEnchant(
			ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.DAMAGE_ALL, 1),
			Enchantment.DURABILITY, 1000);

	public EnchantTableClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.EnchantTable;
	}

	public ItemStack makeGreen(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.ORANGE);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(new ItemStack(Material.ENCHANTMENT_TABLE));
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		woodSword.removeEnchantment(Enchantment.KNOCKBACK);
		woodSword.removeEnchantment(Enchantment.FIRE_ASPECT);
		playerInv.setItem(0,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.DAMAGE_ALL, 1),
						Enchantment.DURABILITY, 1000));
		playerInv.setItem(1, new ItemStack(Material.ENCHANTMENT_TABLE));

		player.sendMessage("" + ChatColor.BOLD + "===============================");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD
				+ ChatColor.UNDERLINE + "  Enchantments:");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage(
				"" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  1 Kill: Sharpness 1 & Knockback 1");
		player.sendMessage(
				"" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  3 Kills: Sharpness 1 & Knockback 2");
		player.sendMessage(
				"" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  5 Kills: Fire Aspect 1 & Knockback 1");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "===============================");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		BaseClass bc = instance.classes.get(player);

		if (item != null && item.getType() == Material.ENCHANTMENT_TABLE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (bc.eachLifeKills < 1) {
				player.sendMessage(instance.getManager().getMain()
						.color("&r&l(!) &rUnable to enchant your weapon at this time: &eKills: " + bc.eachLifeKills));
			} else if (bc.eachLifeKills == 1 || bc.eachLifeKills == 2) {
				if (!(woodSword.containsEnchantment(Enchantment.KNOCKBACK))) {
					player.getInventory().setItem(0, ItemHelper.addEnchant(woodSword, Enchantment.KNOCKBACK, 1));
					player.sendMessage(instance.getManager().getMain()
							.color("&r&l(!) &rSuccessfully added &eKnockback I &rto your weapon"));
				} else {
					player.sendMessage(
							instance.getManager().getMain().color("&r&l(!) &rYou already claimed &eKnockback I"));
				}
			} else if (bc.eachLifeKills == 3 || bc.eachLifeKills == 4) {
				woodSword.removeEnchantment(Enchantment.KNOCKBACK);
				player.getInventory().setItem(0, ItemHelper.addEnchant(woodSword, Enchantment.KNOCKBACK, 2));
				player.sendMessage(instance.getManager().getMain()
						.color("&r&l(!) &rSuccessfully added &eKnockback II &rto your weapon"));
			} else if (bc.eachLifeKills == 5) {
				woodSword.removeEnchantment(Enchantment.DAMAGE_ALL);
				woodSword.removeEnchantment(Enchantment.KNOCKBACK);
				player.getInventory().setItem(0, ItemHelper.addEnchant(
						ItemHelper.addEnchant(woodSword, Enchantment.KNOCKBACK, 1), Enchantment.FIRE_ASPECT, 1));
				player.sendMessage(instance.getManager().getMain()
						.color("&r&l(!) &rSuccessfully added &eFire Aspect I &r& &eKnockback I &rto your weapon"));
			} else {
				player.sendMessage(instance.getManager().getMain()
						.color("&r&l(!) &rYou are unable to enchant your weapon anymore"));
			}
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = woodSword;
		return item;
	}

}
