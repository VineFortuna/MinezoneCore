package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WolfClass extends BaseClass {

	private boolean used = false;
	private List<Wolf> wolves = new ArrayList<>();
	private List<Player> hitPlayers = new ArrayList<>();
	private int biteCooldownSec, callCooldownSec;

	public WolfClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
		createArmor(
				null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjE5Y2MwNDdhM2ExYzJhNmZjZjVlMjNkNzk4OTUwOTQ5ZjBlYTc2YTU1Mzc3MDJjODBlNTQ1NDA5ZjBiODc0NiJ9fX0=",
				"DDDBDB",
				"DDDBDB",
				"DDDBDB",
				6,
				"Wolf"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Wolf;
	}
	
	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack bone = ItemHelper.addEnchant(
				ItemHelper.addEnchant(new ItemStack(Material.BONE), Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK,
				1);
		if (instance.classes.containsKey(player)) {
			BaseClass bc = instance.classes.get(player);

			if (bc != null) {
				if (bc.getLives() == 1) {
					bone = ItemHelper.addEnchant(
							ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.BONE), "&eBite"),
									Enchantment.DAMAGE_ALL, 4),
							Enchantment.KNOCKBACK, 2);
				}

			}
		}
		return bone;
	}

	@Override
	public void SetNameTag() {

	}
	
	@Override
	public void Tick(int gameTicks) {
		if (used) {
			List<Entity> nearby = player.getNearbyEntities(1D, 1D, 1D);
			for (Entity entity : nearby) {
				if (entity instanceof Player) {
					Player target = (Player) entity;
					if (target != player && !hitPlayers.contains(target)) {
						
						target.setVelocity((new Vector(0, 1, 0)).multiply(0.5D));
						EntityDamageEvent damageEvent = new EntityDamageEvent(target,
								EntityDamageEvent.DamageCause.MAGIC, 6);
						target.damage(6, player);
						player.getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 0.8f, 0);
						hitPlayers.add(target);
						
						int radius = 2;
						for (int t = 0; t < 2 * Math.PI * radius; t += 1) {
							player.getWorld().playEffect(player.getLocation().add(radius * Math.cos(t), 0,
									radius * Math.sin((t))), Effect.CRIT, 1);
						}
					}
				}
			}
			if (player.isOnGround()) {
				used = false;
				hitPlayers.clear();
			}
		}
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Wolf
				&& instance.classes.get(player).getLives() > 0) {
			this.biteCooldownSec = (12000 - wolfBite.getTime()) / 1000 + 1;
			
			if (wolfBite.getTime() < 12000) {
				String msg = instance.getGameManager().getMain()
						.color("&eBite &rregenerates in: &e" + biteCooldownSec + "s");
				getActionBarManager().setActionBar(player, "wolf.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &eBite");
				getActionBarManager().setActionBar(player, "wolf.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		wolfBite.startTime = System.currentTimeMillis() - 100000;
		wolves.clear();
		hitPlayers.clear();

		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.RAW_BEEF),
				instance.getGameManager().getMain().color("&6&lThrill of the Hunt")));
		used = false;
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			ItemMeta meta = item.getItemMeta();
			if (item.getType() == Material.RAW_BEEF) {
				if (meta != null && meta.getDisplayName().contains("Thrill of the Hunt")) {
					event.setCancelled(true);
					for (int i = 0; i < 3; i++) {
						@SuppressWarnings("deprecation")
						Wolf wolf = (Wolf) player.getWorld().spawnCreature(player.getLocation(), EntityType.WOLF);
						wolf.setCustomName(
								"" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Wolf Army");
						wolf.setBreed(false);
						wolf.setTarget(instance.getNearestPlayer(player, wolf, 150));
                        wolf.setAngry(false);
						wolf.setMaxHealth(wolf.getMaxHealth()*1.5);
						wolf.setHealth(wolf.getMaxHealth());
						this.wolves.add(wolf);
					}
					player.getWorld().playSound(player.getLocation(), Sound.WOLF_HOWL, 1, 0);
					player.getInventory().clear(1);
				}
			} else if (item.getType() == Material.BONE) {
				if (wolfBite.getTime() < 12000) {
					int seconds = (12000 - wolfBite.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "Your Bite Attack is still regenerating for " + ChatColor.YELLOW + seconds + " more seconds ");
				} else {
					wolfBite.restart();
					startDash();
				}
			}
		}
	}
	
	@Override
	public void TakeDamage(EntityDamageEvent event) {
		for (Wolf wolf : wolves) {
			if (!wolf.isDead()) {
				wolf.setAngry(true);
			}
		}
	};
	
	private void startDash() {
		double boosterStrength = 1.1;
		player.getWorld().playSound(player.getLocation(), Sound.WOLF_GROWL, 1, 0);
		Vector vel = player.getLocation().getDirection().multiply(boosterStrength).add(new Vector(0, 0.5, 0));
		player.setVelocity(vel);
		used = true;
	}
}
