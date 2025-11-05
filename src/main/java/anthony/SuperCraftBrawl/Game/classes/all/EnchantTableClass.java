package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

import java.util.Random;

public class EnchantTableClass extends BaseClass {

	private final Ability enchantAbility = new Ability("&d&lEnchant", 0, player);
	private ItemStack weapon;
	private int xpLevelsAmount = 0;
	private int levelEnchanted = 0;
	private int xpHit = 1;

	public EnchantTableClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc2MmExNWIwNDY5MmEyZTRiM2ZiMzY2M2JkNGI3ODQzNGRjZTE3MzJiOGViMWM3YTlmN2MwZmJmNmYifX19",
				"120021",
				6,
				"EnchantmentTable"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.EnchantTable;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetNameTag() {}

	@Override
	public void SetItems(Inventory playerInv) {
		// Base weapon
		weapon = ItemHelper.setDetails(ItemHelper.create(Material.WOOD_SWORD),
				"&dNot so great of a Sword",
				"",
				"&7Gain:",
				"&7▶ &b&o1 XP &eon hit",
				"&7▶ &a&o1 Level &eon kill");
		ItemHelper.setUnbreakable(weapon);

		// Enchant item
		ItemStack enchantItem = ItemHelper.setDetails(ItemHelper.create(Material.ENCHANTMENT_TABLE),
				enchantAbility.getAbilityNameRightClickMessage(),
				ChatColorHelper.color("&7Right click to enchant your sword"),
				"",
				ChatColorHelper.color("&7Left click to view enchantments")
		);

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, enchantItem);

		// Reset player XP
		player.setExp(0);

		player.setLevel(xpLevelsAmount);
		levelEnchanted = 0;

		// Help message
		infoMessage();
	}

	public void infoMessage() {
		player.sendMessage("" + ChatColor.BOLD + "===============================");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "|| " + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "  Enchantments:");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "|| " + ChatColor.YELLOW + "  1 Level: Knockback I");
		player.sendMessage("" + ChatColor.BOLD + "|| " + ChatColor.YELLOW + "  2 Levels: Sharpness I & Knockback I");
		player.sendMessage("" + ChatColor.BOLD + "|| " + ChatColor.YELLOW + "  3 Levels: Sharpness I & Knockback II");
		player.sendMessage("" + ChatColor.BOLD + "|| " + ChatColor.YELLOW + "  5 Levels: Sharpness I & Knockback I & Fire Aspect I");
		player.sendMessage("" + ChatColor.BOLD + "|| " + ChatColor.YELLOW + "  8 Levels: Sharpness I & Knockback II & Fire Aspect I");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "===============================");
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;
		int levelBefore = player.getLevel();
		player.giveExp(xpHit);
		int levelAfter = player.getLevel();

		if (levelAfter > levelBefore) {
			xpLevelsAmount++;
			player.sendMessage(ChatColorHelper.color("&b&l(!) &r&eYou gained an XP level"));
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item == null) return;

		if (item.getType() == Material.ENCHANTMENT_TABLE) {
			if (event.getAction().name().contains("RIGHT_CLICK")) {

				int playerXP = xpLevelsAmount;
				int xpSpent;

				if (playerXP < 1) {
					enchantAbility.sendCustomMessage("&c&l(!) &rYou do not have enough levels to enchant");
					return;
				}

				// Enchant tiers (≥ thresholds)
				if (playerXP >= 8 && levelEnchanted < 8) {
					applyEnchantments(1, 2, 1);
					xpSpent = 8;
					levelEnchanted = 8;
				} else if (playerXP >= 5 && levelEnchanted < 5) {
					applyEnchantments(1, 1, 1);
					xpSpent = 5;
					levelEnchanted = 5;
				} else if (playerXP >= 3 && levelEnchanted < 3) {
					applyEnchantments(1, 2, 0);
					xpSpent = 3;
					levelEnchanted = 3;
				} else if (playerXP >= 2 && levelEnchanted < 2) {
					applyEnchantments(1, 1, 0);
					xpSpent = 2;
					levelEnchanted = 2;
				} else if (playerXP >= 1 && levelEnchanted < 1) {
					applyEnchantments(0, 1, 0);
					xpSpent = 1;
					levelEnchanted = 1;
				} else {
					enchantAbility.sendCustomMessage("&a&l(!) &rYou already have that enchantment");
					return;
				}

				// Set weapon name
				ItemHelper.setDetails(weapon, "&dNow that is something");

				// Update weapon in inventory
				player.getInventory().setItem(0, weapon);

				// Remove used XP levels
				player.giveExpLevels(-xpSpent);
				xpLevelsAmount = Math.max(0, xpLevelsAmount - xpSpent);
			} else if (event.getAction().name().contains("LEFT_CLICK")) {
				infoMessage();
			}
		}
	}

	private void applyEnchantments(int sharpness, int knockback, int fireAspect) {
		if (sharpness > 0) {
			weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
			enchantAbility.sendCustomMessage("&a&l(!) &rWeapon enchanted with &c&lSharpness " + sharpness);
		}
		if (knockback > 0) {
			weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, knockback);
			enchantAbility.sendCustomMessage("&a&l(!) &rWeapon enchanted with &b&lKnockback " + knockback);
		}
		if (fireAspect > 0) {
			weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, fireAspect);
			enchantAbility.sendCustomMessage("&a&l(!) &rWeapon enchanted with &6&lFire Aspect " + fireAspect);
		}
	}

	@Override
	public void classesEvent(Player damagerPlayer, BaseClass baseClass) {
		if (instance.classes.containsKey(damagerPlayer)) {
			xpLevelsAmount++;
			damagerPlayer.giveExpLevels(1);
			damagerPlayer.sendMessage(ChatColorHelper.color("&b&l(!) &r&eYou gained an XP level"));
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
