package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

import java.util.concurrent.ThreadLocalRandom;

public class Cactus extends BaseClass {

	public Cactus(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmY1ODViNDFjYTVhMWI0YWMyNmY1NTY3NjBlZDExMzA3Yzk0ZjhmOGExYWRlNjE1YmQxMmNlMDc0ZjQ3OTMifX19",
				"10761D",
				8,
				"Cactus"
		);
		playerHead.addUnsafeEnchantment(Enchantment.THORNS, 1);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	public ItemStack getCactus() {
		return ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.CACTUS, 1),
				"" + ChatColor.RED + ChatColor.BOLD + "Spiker"), Enchantment.KNOCKBACK, 2);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOD_SWORD),
						"" + ChatColor.GREEN + ChatColor.BOLD + "Spikey Sword"), Enchantment.DAMAGE_ALL, 1),
				Enchantment.DURABILITY, 10000));
		ItemStack cactus = getCactus();
		playerInv.setItem(1, cactus);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		
	}

	@Override
	public ClassType getType() {
		return ClassType.Cactus;
	}

	@Override
	public void SetNameTag() {
		
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.WOOD_SWORD),
						"" + ChatColor.GREEN + ChatColor.BOLD + "Spikey Sword"), Enchantment.DAMAGE_ALL, 1),
				Enchantment.DURABILITY, 10000);
		return item;
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if(event.getEntity().equals(player) && event.getDamager() instanceof LivingEntity){
			ItemStack held = player.getItemInHand();
			if(held == null || held.getType() != Material.CACTUS) return;
			LivingEntity damager = (LivingEntity) event.getDamager();
			//Will deal 1-4 HP
			int randomDmg = ThreadLocalRandom.current().nextInt(1, 5);
			damager.damage(randomDmg, player);
		}
	}
}
