package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CactusClass extends BaseClass {

	private final ItemStack weapon;
	private final ItemStack cactusItem;

	public CactusClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmY1ODViNDFjYTVhMWI0YWMyNmY1NTY3NjBlZDExMzA3Yzk0ZjhmOGExYWRlNjE1YmQxMmNlMDc0ZjQ3OTMifX19",
				"10761D",
				6,
				"Cactus"
		);
		playerHead.addUnsafeEnchantment(Enchantment.THORNS, 1);

		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.WOOD_SWORD),
				"&2&lSpikey Sword"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
		ItemHelper.setUnbreakable(weapon);

		// Cactus
		cactusItem = ItemHelper.setDetails(
				new ItemStack(Material.CACTUS),
				"&2&lSpiker"
		);
		cactusItem.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
		cactusItem.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, cactusItem);
	}

	@Override
	public ClassType getType() {
		return ClassType.Cactus;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
