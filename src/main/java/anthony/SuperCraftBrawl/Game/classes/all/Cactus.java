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

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;

import java.util.concurrent.ThreadLocalRandom;

public class Cactus extends BaseClass {

	public Cactus(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(getHelmet(new ItemStack(Material.CACTUS)));
		playerEquip.setChestplate(ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE), Enchantment.THORNS, 1),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4));
		playerEquip.setLeggings(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_LEGGINGS), Enchantment.THORNS, 1));
		playerEquip.setBoots(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4));

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
