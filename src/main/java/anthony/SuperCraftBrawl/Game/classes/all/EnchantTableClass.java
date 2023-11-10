package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.classes.Ability;
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

	private Ability enchantAbility = new Ability("Enchant Ability", 0, playerBaseClass);
	private ItemStack weapon;
	private int xpLevelsAmount = 0;
	int levelEnchanted = 0;

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
		// Weapon
		ItemStack weapon = ItemHelper.create(Material.WOOD_SWORD, ChatColorHelper.color("&dNot so great of a Sword"));
		ItemHelper.setUnbreakable(weapon);

		this.weapon = weapon;

		// Enchant Ability
		ItemStack enchantItem = ItemHelper.create(Material.ENCHANTMENT_TABLE, ChatColorHelper.color("&dEnchant Weapon"),
				ChatColorHelper.color("&7Right click to enchant your sword"),
				ChatColorHelper.color("&7Get level by getting kills"));

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, enchantItem);

		playerBaseClass.setTotalExperience(0);

		if (xpLevelsAmount> 0) {
			playerBaseClass.giveExpLevels(xpLevelsAmount);
		}

		levelEnchanted = 0;

		playerBaseClass.sendMessage("" + ChatColor.BOLD + "===============================");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "||");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "|| " + ChatColor.AQUA + "      Get experience by killing players");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "||");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "  Enchantments:");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "||");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  1 Level: Sharpness 1");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  2 Levels: Sharpness 1 & Knockback 1");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  3 Levels: Sharpness 1 & Knockback 2");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  5 Levels: Sharpness 1 & Knockback 1 & Fire Aspect 1");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  8 Levels: Sharpness 2 & Knockback 1 & Fire Aspect 1");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "||");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "||");
		playerBaseClass.sendMessage("" + ChatColor.BOLD + "===============================");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		BaseClass bc = instance.classes.get(playerBaseClass);
		Inventory inventory = playerBaseClass.getInventory();
		int xpSpent = 0;

		// Enchant Ability
		if (item != null && item.getType() == Material.ENCHANTMENT_TABLE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

			// Checks if player has experience
			if (xpLevelsAmount == 0) {
				enchantAbility.sendPlayerCustomUseAbilityChatMessage("&c&l(!) &rYou do not have enough levels to enchant");
				// 1 Level
			} else if (xpLevelsAmount == 1) {
				if (levelEnchanted == 1) {
					return;
				} else {
					weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &cSharpness 1");

					xpSpent = 1;
					xpLevelsAmount -= 1;
					levelEnchanted = 1;
				}
				// 2 Levels
			} else if (xpLevelsAmount == 2) {
				if (levelEnchanted == 2) {
					return;
				} else {
					weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
					weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &cSharpness 1");
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &bKnockback 1");

					xpSpent = 2;
					xpLevelsAmount -= 2;
					levelEnchanted = 2;
				}

				// 3 and 4 Levels
			} else if (xpLevelsAmount == 3 || xpLevelsAmount == 4) {
				if (levelEnchanted == 3) {
					return;
				} else {
					weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
					weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &cSharpness 1");
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &bKnockback 2");

					xpSpent = 3;
					xpLevelsAmount -= 3;
					levelEnchanted = 3;
				}

				// 5, 6 and 7 Levels
			} else if (xpLevelsAmount == 5 || xpLevelsAmount == 6 || xpLevelsAmount == 7) {
				if (levelEnchanted == 5) {
					return;
				} else {
					weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
					weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
					weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &cSharpness 1");
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &bKnockback 1");
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &6Fire Aspect 1");

					xpSpent = 4;
					xpLevelsAmount -= 5;
					levelEnchanted = 5;
				}

			} else {
				if (levelEnchanted == 8) {
					return;
				} else {
					weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
					weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
					weapon.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &cSharpness 2");
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &bKnockback 1");
					enchantAbility.sendPlayerCustomUseAbilityChatMessage("&a&l(!) &rWeapon enchanted with &6Fire Aspect 1");

					xpSpent = 8;
					xpLevelsAmount -= 8;
					levelEnchanted = 8;
				}
			}
			// Changing sword display name
			weapon.getItemMeta().setDisplayName(ChatColorHelper.color("&dNow that is something"));

			// Setting Weapon
			inventory.setItem(0, weapon);

			// Setting updated xp value
			playerBaseClass.giveExpLevels(-xpSpent);
		}
	}



	@Override
	public void classesEvent(Player d, BaseClass baseClass) {
		if (instance.classes.containsKey(d)) {
			xpLevelsAmount++;
			d.giveExpLevels(1);

			// Playing XP Sound

			d.sendMessage(ChatColorHelper.color("&b&l(!) &r&eYou got rewarded with a XP level"));
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

}
