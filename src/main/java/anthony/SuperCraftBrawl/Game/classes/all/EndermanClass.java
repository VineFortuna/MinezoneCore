package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Banner;
import org.bukkit.material.Door;
import org.bukkit.material.Skull;
import org.bukkit.util.Vector;

public class EndermanClass extends BaseClass {

	private ItemStack stick;
	private ItemStack newItem = null;
	private boolean used = false;
	private int pearlCooldownSec, itemPickupCooldownSec;

	public EndermanClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E1OWJiMGE3YTMyOTY1YjNkOTBkOGVhZmE4OTlkMTgzNWY0MjQ1MDllYWRkNGU2YjcwOWFkYTUwYjljZiJ9fX0",
				"101010",
				6,
				"Enderman"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		stick = ItemHelper.setDetails(new ItemStack(Material.STICK),
				instance.getGameManager().getMain().color("&c&lBlock Pickup"), "",
				instance.getGameManager().getMain().color("&7Right click to grab the block you're standing on"));
		used = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL, 10),
				"" + ChatColor.BLACK + ChatColor.BOLD + "Teleporters"));
		playerInv.setItem(2, stick);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() > 0
				&& instance.classes.get(player).getType() == ClassType.Enderman)
			if (!(player.getInventory().contains(this.getAttackWeapon())))
				player.getInventory().setItem(0, this.getAttackWeapon()); // If some rare chance the player throws away
																			// their melee

		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Enderman
				&& instance.classes.get(player).getLives() > 0) {
			this.pearlCooldownSec = (10000 - pearlTimer.getTime()) / 1000 + 1;
			this.itemPickupCooldownSec = (10000 - enderman.getTime()) / 1000 + 1;

			//For pearl cooldown message
			if (pearlTimer.getTime() < 10000) {
				String msg = instance.getGameManager().getMain()
						.color("&c&lTeleporter &rin: &e" + this.pearlCooldownSec + "s");
				getActionBarManager().setActionBar(player, "teleport.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &c&lTeleporter");
				getActionBarManager().setActionBar(player, "teleport.cooldown", msg, 2);
			}
			
			//For Item Pickup cooldown message
			if (enderman.getTime() < 10000) {
				String msg = instance.getGameManager().getMain()
						.color("&c&lBlock Pickup &rin: &e" + this.itemPickupCooldownSec + "s");
				getActionBarManager().setActionBar(player, "itemPickup.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &c&lBlock Pickup");
				getActionBarManager().setActionBar(player, "itemPickup.cooldown", msg, 2);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.EYE_OF_ENDER
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
						|| event.getAction() == Action.LEFT_CLICK_AIR
						|| event.getAction() == Action.LEFT_CLICK_BLOCK)) {
			event.setCancelled(true);
		} else if (item != null && item.getType() == Material.STICK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (used == true) {
				player.sendMessage(
						instance.getGameManager().getMain().color("&c&l(!) &rYou already have a block in your inventory!"));
				return;
			} else if (!(player.isOnGround())) {
				player.sendMessage(
						instance.getGameManager().getMain().color("&r&l(!) &rYou must be on the ground to use this item!"));
				return;
			}

			if (enderman.getTime() < 10000) {
				int seconds = (10000 - enderman.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your stick is still on cooldown for " + ChatColor.YELLOW + seconds + " more seconds ");
			} else {
				Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(),
						player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
				Block blockInside = player.getWorld().getBlockAt(player.getLocation());
				
				if (blockInside.getType().isSolid() && player.getLocation().getY() % 1 != 0) {
					newItem = new ItemStack(blockInside.getType(), 1);
				} else if (block.getType().isSolid()) {
					newItem = new ItemStack(block.getType(), 1);
				} else {
					player.sendMessage(instance.getGameManager().getMain()
							.color("&c&l(!) &rThere is no block under you. Please try again."));
					return;
				}
				enderman.restart();
				if (newItem.getType() == Material.DOUBLE_STEP || newItem.getType() == Material.DOUBLE_STONE_SLAB2 ||
						newItem.getType() == Material.WOOD_DOUBLE_STEP)
					newItem.setType(Material.valueOf(newItem.getType().name().replace("DOUBLE_", "")));
				else if (newItem.getData() instanceof Door)
					newItem.setType(Material.WOOD_DOOR);
				else if (newItem.getData() instanceof Skull)
					newItem.setType(Material.SKULL_ITEM);
				else if (newItem.getType() == Material.SOIL)
					newItem.setType(Material.DIRT);
				else if (newItem.getType() == Material.BED_BLOCK)
					newItem.setType(Material.BED);
				else if (newItem.getData() instanceof Banner)
					newItem.setType(Material.BANNER);
				else if (newItem.getType() == Material.BREWING_STAND)
					newItem.setType(Material.BREWING_STAND_ITEM);
				else if (newItem.getType() == Material.CAULDRON)
					newItem.setType(Material.CAULDRON_ITEM);
				ItemHelper.setDetails(newItem, instance.getGameManager().getMain().color(
						"&e&lBlock"));
				player.getInventory().addItem(newItem);
				player.sendMessage(
						instance.getGameManager().getMain().color("&r&l(!) &rYou picked up &e1 " +
								WordUtils.capitalizeFully(newItem.getType().name().replace('_', ' ').replaceAll("[0-9]", ""))));
				used = true;
			}
		} else if (item != null && newItem != null && item.hasItemMeta() && item.isSimilar(newItem)) {
			Vector direction = player.getLocation().getDirection();
			player.getInventory().remove(newItem);
			ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
				@Override
				public void onHit(Player hit) {
					if (instance.duosMap != null)
						if (instance.team.get(hit).equals(instance.team.get(player)))
							return;

					player.playSound(hit.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
					Location loc = hit.getLocation();
					Vector v = direction;
					EntityDamageEvent damageEvent = new EntityDamageEvent(hit, DamageCause.PROJECTILE, 4.5);
					instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
					hit.damage(4.5, player);
					v.setY(1.0);
					hit.setVelocity(v);
					for (Player gamePlayer : instance.players)
						gamePlayer.playSound(loc, Sound.CHICKEN_EGG_POP, 1, 1);
				}

			}, new ItemStack(newItem));
			instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
					player.getLocation().getDirection().multiply(2.0D));

			newItem = null;
			used = false;
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Enderman;
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(
				ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER),
						"" + ChatColor.RED + ChatColor.BOLD + "Enderman Soul"), Enchantment.DAMAGE_ALL, 4),
				Enchantment.KNOCKBACK, 1);
		return item;
	}

}
