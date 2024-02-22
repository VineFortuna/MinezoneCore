package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class FadeClass extends BaseClass {

	public FadeClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.15;
	}

	@Override
	public ClassType getType() {
		return null;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack head = new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData());

		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.BLACK);
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.BLACK);

		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.BLACK);
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		playerEquip.setHelmet(head);
		playerEquip.setChestplate(chestplate);
		playerEquip.setLeggings(leggings);
		playerEquip.setBoots(boots);
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack melee = new ItemStack(Material.STAINED_CLAY, 1, (short) 15);
		return melee;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.INK_SACK, 1, (short) 15),
				"" + ChatColor.RESET + "Fade Ability"));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

}
