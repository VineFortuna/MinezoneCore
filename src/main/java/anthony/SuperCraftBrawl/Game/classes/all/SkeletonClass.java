package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;

public class SkeletonClass extends BaseClass {

	private int cooldownSec;

	public SkeletonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjI3OTVjM2M2ZjM2ZDY3ZGVjZjlhMzE5NWUxMjgwNDBiZWM1MjI2YjA1NWYyYjE2ZDQ2ZmExOWE5MTgwZTAyMyJ9fX0=",
				"D6D6D6",
				8,
				"Skeleton"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Skeleton
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (5000 - skeleAttack.getTime()) / 1000 + 1;

			if (skeleAttack.getTime() < 5000) {
				String msg = instance.getGameManager().getMain()
						.color("&7Attack Arrow &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "arrow.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &7Attack Arrow");
				getActionBarManager().setActionBar(player, "arrow.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		skeleAttack.startTime = System.currentTimeMillis() - 100000;
		playerInv
				.setItem(0,
						ItemHelper.addEnchant(ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.BONE), "", "",
										instance.getGameManager().getMain()
												.color("&7Right click to shoot an Attack Arrow!")),
								Enchantment.DAMAGE_ALL, 2), Enchantment.KNOCKBACK, 1));
		playerInv.setItem(1, this.getAttackWeapon());
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getPlayer().getItemInHand();
		if (item.getType() == Material.BONE
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				if (skeleAttack.getTime() < 5000) {
					int seconds = (5000 - skeleAttack.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Your attack arrow is still regenerating for " + ChatColor.YELLOW + seconds
							+ " more seconds ");
				} else {
					skeleAttack.restart();
					player.launchProjectile(Arrow.class);
				}
				event.setCancelled(true);
			}

			for (Player worldPlayer : player.getWorld().getPlayers())
				worldPlayer.playSound(player.getLocation(), Sound.SKELETON_HURT, 1, 1);
		}
	}
	
	@Override
    public void DoDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            double arrowVelocity = arrow.getVelocity().length();
            double modifiedDamage = event.getDamage() * arrowVelocity;
            
            if (modifiedDamage >= 10) //Nerf arrow damage when fully charged
            	modifiedDamage = 7.50;

            event.setDamage(modifiedDamage);
        }
    }

	@Override
	public ClassType getType() {
		return ClassType.Skeleton;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(ItemHelper.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.BOW), ChatColor.GRAY + "Skeleton Bow",
								ChatColor.GRAY + "", ChatColor.YELLOW + "Snipe your enemies with this!"),
						Enchantment.ARROW_INFINITE, 1), Enchantment.DURABILITY, 10000), Enchantment.ARROW_KNOCKBACK, 1);
		return item;
	}

}