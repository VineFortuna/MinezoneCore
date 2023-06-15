package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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

public class HorseClass extends BaseClass {

	private Cooldown boosterCooldown = new Cooldown(3000), shurikenCooldown = new Cooldown(200);

	public HorseClass(GameInstance instance, Player player) {
		super(instance, player);
		// TODO Auto-generated constructor stub
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

		meta.setOwner("kuba432110");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeGreen(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGreen(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGreen(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getHay() {
		return ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.HAY_BLOCK, 1),
						"" + ChatColor.RESET + ChatColor.DARK_GREEN + "Hay Bale", ChatColor.YELLOW + ""),
				Enchantment.DAMAGE_ALL, 3);
	}

	public ItemStack getLeash() {
		return ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.LEASH, 1),
				"" + ChatColor.BLUE + "Lead", ChatColor.YELLOW + ""), Enchantment.KNOCKBACK, 2);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		ItemStack hay = getHay();
		hay.setAmount(1);
		playerInv.setItem(0, hay);

		ItemStack lead = getLeash();
		lead.setAmount(1);
		playerInv.setItem(1, lead);

		Random rand = new Random();

		int chance = rand.nextInt(5);
		if (chance == 0) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 3));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "JUMP IV");
		} else if (chance == 1) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 2));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "SPEED III");
		} else if (chance == 2) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999999, 1));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "STRENGTH II");
		} else if (chance == 3) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "RESISTANCE I");
		} else if (chance == 4) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999999, 1));
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You were given "
					+ ChatColor.YELLOW + ChatColor.BOLD + "FIRE RESISTANCE II");
		} else {
			return;
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

	@Override
	public ClassType getType() {
		return ClassType.Horse;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.HAY_BLOCK, 1),
						"" + ChatColor.RESET + ChatColor.DARK_GREEN + "Hay Bale", ChatColor.YELLOW + ""),
				Enchantment.DAMAGE_ALL, 3);
		return item;
	}
}
