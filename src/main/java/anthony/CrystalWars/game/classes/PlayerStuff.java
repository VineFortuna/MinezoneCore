package anthony.CrystalWars.game.classes;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import anthony.CrystalWars.game.GameInstance;

public class PlayerStuff extends BaseClass {

	public PlayerStuff(GameInstance i, Player player) {
		super(i, player);
	}

	public ItemStack colorArmor(ItemStack armor, Color c) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(c);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		if (i.getTeam(player).equals("Blue")) {
			playerEquip.setChestplate(colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.BLUE));
			playerEquip.setLeggings(colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.BLUE));
			playerEquip.setBoots(colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.BLUE));
		} else if (i.getTeam(player).equals("Red")) {
			playerEquip.setChestplate(colorArmor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.RED));
			playerEquip.setLeggings(colorArmor(new ItemStack(Material.LEATHER_LEGGINGS), Color.RED));
			playerEquip.setBoots(colorArmor(new ItemStack(Material.LEATHER_BOOTS), Color.RED));
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.addItem(new ItemStack(Material.WOOD_SWORD));
	}

}
