package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BatClass extends BaseClass {

	public BatClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.6;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU5OWRlZWY5MTlkYjY2YWMyYmQyOGQ2MzAyNzU2Y2NkNTdjN2Y4YjEyYjlkY2E4ZjQxYzNlMGEwNGFjMWNjIn19fQ==",
				null,
				6,
				"Bat"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 1));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
		else if (!(player.getActivePotionEffects().contains(PotionEffectType.INVISIBILITY)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 1));
		else if (!(player.getActivePotionEffects().contains(PotionEffectType.DAMAGE_RESISTANCE)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

	@Override
	public ClassType getType() {
		return ClassType.Bat;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.setUnbreakable(ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.SHEARS), ChatColor.GREEN + "Shears",
						ChatColor.GRAY + "Beat your enemies to pieces!", ChatColor.YELLOW + ""),
				Enchantment.KNOCKBACK, 1), Enchantment.DAMAGE_ALL, 3));
		return item;
	}

}
