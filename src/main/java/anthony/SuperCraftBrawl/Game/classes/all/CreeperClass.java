package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class CreeperClass extends BaseClass {

	private int cooldownSec = 0;
	private ItemStack barrier = new ItemStack(Material.BARRIER);

	public CreeperClass(GameInstance instance, Player player) {
		super(instance, player);
	}

	@Override
	public ClassType getType() {
		return ClassType.Creeper;
	}

	public ItemStack makeGreen(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.ORANGE);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack creeperHelmet = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.CREEPER.ordinal());
		playerEquip.setHelmet(creeperHelmet);
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
	public void Tick(int gameTicks) {
		BaseClass bc = instance.classes.get(player);
		if (bc != null && bc.getLives() <= 0)
			return;
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				instance.getGameManager().getMain().color("&c&lCreeper Potion"));
		Potion pot3 = new Potion(1);
		pot3.setType(PotionType.INSTANT_DAMAGE);
		pot3.setSplash(true);
		pot3.apply(item);

		if (!(player.getInventory().contains(item)) && !(player.getInventory().contains(barrier))) {
			player.getInventory().addItem(barrier);
			this.cooldownSec = 3;
		}

		if (gameTicks % 20 == 0 && this.cooldownSec != 0) {
			cooldownSec--;

			if (this.cooldownSec <= 0) {
				if (!(player.getInventory().contains(item))) {
					player.getInventory().remove(barrier);
					player.getInventory().addItem(item);
				}
			}
		}
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof ThrownPotion) {
			ThrownPotion potion = (ThrownPotion) event.getEntity();

			if (potion.getShooter() instanceof Player) {
				Player player = (Player) potion.getShooter();
				if (player.getInventory().getItemInHand().getItemMeta().getDisplayName().contains("Creeper Potion")) {
					// Adjust the velocity of the potion
					Vector velocity = potion.getVelocity();
					potion.setVelocity(velocity.multiply(1.3));
				}
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.cooldownSec = 0;
		playerInv.setItem(0, ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.SULPHUR),
						"" + ChatColor.YELLOW + ChatColor.BOLD + "Creeper Essence"), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1));
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				instance.getGameManager().getMain().color("&c&lCreeper Potion"));
		Potion pot3 = new Potion(1);
		pot3.setType(PotionType.INSTANT_DAMAGE);
		pot3.setSplash(true);
		pot3.apply(item);
		playerInv.setItem(1, item);
		playerInv.setItem(2, ItemHelper.setDetails(new ItemStack(Material.TNT),
				"" + ChatColor.RED + ChatColor.BOLD + "Destructionators"));
		playerInv.setItem(3, ItemHelper.setDetails(new ItemStack(Material.STONE_BUTTON),
				"" + ChatColor.RED + ChatColor.BOLD + "Suicide Button"));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.POTION
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				int amount = item.getAmount();

				if (amount == 0) {
					ItemStack item2 = new ItemStack(Material.POTION, 1);
					Potion pot3 = new Potion(1);
					pot3.setType(PotionType.INSTANT_DAMAGE);
					pot3.setSplash(true);
					pot3.apply(item2);
					player.getInventory().setItem(1, item2);
				}
			} else if (item.getType() == Material.TNT
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (tnt.getTime() < 10000) {
					int seconds = (10000 - tnt.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have to wait "
							+ ChatColor.YELLOW + seconds + " seconds " + ChatColor.RESET + "to use this item again");
				} else {
					tnt.restart();
					TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
					tnt.setFuseTicks(40);
				}
			} else if (item.getType() == Material.STONE_BUTTON
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (!(instance.getGameManager().spawnProt.containsKey(player))) {
					if (player.getGameMode() != GameMode.SPECTATOR) {
						TNTPrimed tnt = player.getWorld().spawn(player.getLocation().add(0, 1, 0), TNTPrimed.class);
						tnt.setFuseTicks(0);
					}
				}
			}
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.SULPHUR),
						"" + ChatColor.YELLOW + ChatColor.BOLD + "Creeper Essence"), Enchantment.DAMAGE_ALL, 3),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

}
