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

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

public class SethBlingClass extends BaseClass {

	public SethBlingClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.5;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==",
				"FF4E4A",
				"4193FF",
				"9A562B",
				6,
				"SethBling"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	public ItemStack getCmdBlock() {
		return ItemHelper.setDetails(new ItemStack(Material.COMMAND, 1),
				"" + ChatColor.GRAY + ChatColor.ITALIC + "Right Click", ChatColor.YELLOW + "");
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, this.getCmdBlock());
	}

	public void TestItems() {
		player.getInventory().addItem(new ItemStack(instance.getItemToDrop()));
		player.getInventory().addItem(new ItemStack(instance.getItemToDrop()));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.COMMAND
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "Your command block skills rewarded you with special items!");
			int amount = item.getAmount();
			if (amount > 0) {
				amount--;
				if (amount == 0)
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				else
					item.setAmount(amount);
				event.setCancelled(true);
				TestItems();
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.SethBling;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.REDSTONE_BLOCK, 1),
						"" + ChatColor.RED + ChatColor.BOLD + "Seth's Block", ChatColor.YELLOW + ""),
				Enchantment.KNOCKBACK, 2), Enchantment.DAMAGE_ALL, 3);
		return item;
	}

}
