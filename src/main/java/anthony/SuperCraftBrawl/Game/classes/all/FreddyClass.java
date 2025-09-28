package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class FreddyClass extends BaseClass {

	private boolean isUsed = false;
	private int cooldownSec;

	public FreddyClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
		createArmor(null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmEyMGFhMmE5YTI4YzUzM2QwMWE3ZDk4MDdmZTcyYWYzZGRlOTg1Y2NlNzQyMTZkNzNhYzUxNTlkOTdmNmE3ZCJ9fX0=",
				"D87F33", 6, "Freddy");
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.isUsed = false;
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, getStunAbility());
		playerInv.setItem(2, getScareAbility());
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Freddy
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10000 - freddyCooldown.getTime()) / 1000 + 1;

			if (freddyCooldown.getTime() < 10000) {
				String msg = instance.getGameManager().getMain()
						.color("&cStun Ability &rregenerates in: &e" + cooldownSec + "s");
				getActionBarManager().setActionBar(player, "freddy.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &cStun Ability");
				getActionBarManager().setActionBar(player, "freddy.cooldown", msg, 2);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void UseItem(PlayerInteractEvent e) {
		ItemStack item = e.getItem();
		Player p = e.getPlayer();

		if (item != null) {
			if (item.getType() == Material.REDSTONE) {
				if (freddyCooldown.getTime() < 10000) {
					int sec = (10000 - freddyCooldown.getTime()) / 1000 + 1;
					player.sendMessage(
							instance.color("&c&l(!) &rYour &cStun Ability &ris still regenerating for &e" + sec + "s"));
				} else {
					freddyCooldown.restart();
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 1, true, false));
					// p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 5,
					// 0, true, false));

					Location cLoc = p.getLocation();
					World w = cLoc.getWorld();
					BaseClass bc = null;

					for (Player target : instance.players) {
						bc = instance.classes.get(target);

						if (bc != null && bc.isPlayerAlive() && target.getGameMode() != GameMode.SPECTATOR) {
							if (target == p)
								continue;
							if (!target.getWorld().equals(w))
								continue;

							// Check XZ distance
							double dx = target.getLocation().getX() - cLoc.getX();
							double dz = target.getLocation().getZ() - cLoc.getZ();
							double distXZ = Math.hypot(dx, dz);
							if (distXZ > 6.0)
								continue;

							// Check Y difference (target up to +2 blocks above)
							double dy = target.getLocation().getY() - cLoc.getY();
							if (dy < 0.0 || dy > 2.0)
								continue;

							// Apply stun effects
							target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 2, 0, true, false));
							target.addPotionEffect(
									new PotionEffect(PotionEffectType.CONFUSION, 20 * 3, 0, true, false));
							target.playSound(target.getLocation(), Sound.GHAST_SCREAM, 0.6f, 1.2f);
						}
					}
					spawnSubtleRing(w, cLoc);
					w.playSound(cLoc, Sound.ENDERMAN_SCREAM, 0.8f, 1.0f);
				}
			} else if (item.getType() == Material.BEACON) {
				if (isUsed) {
					player.sendMessage(instance.color("&c&l(!) &rYou already used your ability!"));
					return;
				}

				player.playSound(player.getLocation(), Sound.ENDERMAN_SCREAM, 0.8f, 3.0f);
				blindGamePlayers();
				this.isUsed = true;

				Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
					if (!instance.players.isEmpty()) {
						Player chosen = instance.players.get(new Random().nextInt(instance.players.size()));
						BaseClass bc = instance.classes.get(chosen);
						int roll = 200;
						boolean found = false;

						while (roll > 0) {
							if (bc != null && bc.isPlayerAlive() && chosen.getGameMode() != GameMode.SPECTATOR
									&& chosen != player) {
								found = true;
								break;
							}

							chosen = instance.players.get(new Random().nextInt(instance.players.size()));
							bc = instance.classes.get(chosen);
							roll--;
						}

						if (found) {
							player.getInventory().remove(Material.BEACON);
							chosen.sendMessage(instance.color("&2&l(!) &cBOO!! Freddy is coming for you..."));
							chosen.playSound(chosen.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
							player.playSound(chosen.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
							player.teleport(chosen);
						} else {
							player.sendMessage(instance.color("&c&l(!) &rNo player has been found! Please try again"));
							this.isUsed = false;
						}
					}

				}, 20L);
			}
		}
	}

	private void blindGamePlayers() {
		for (Player gamePlayer : instance.players) {
			if (gamePlayer != player)
				gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 6, 2, true, false));
		}
	}

	private void spawnSubtleRing(World w, Location center) {
		int points = 12; // low particle count to avoid lag
		double r = 6.0;
		for (int i = 0; i < points; i++) {
			double angle = (2 * Math.PI * i) / points;
			double x = center.getX() + r * Math.cos(angle);
			double z = center.getZ() + r * Math.sin(angle);
			Location spot = new Location(w, x, center.getY() + 0.1, z);

			// A little smoke + colored dust
			w.spigot().playEffect(spot, Effect.SMOKE, 0, 0, 0, 0, 0, 0.01f, 1, 16);
			w.spigot().playEffect(spot, Effect.COLOURED_DUST, 0, 0, 0.3f, 0.05f, 0.05f, 1f, 0, 16);
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.Freddy;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.setUnbreakable(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.IRON_AXE), instance.color("&2&lFreddy's Axe")),
				Enchantment.KNOCKBACK, 1));
		return item;
	}

	public ItemStack getStunAbility() {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.REDSTONE, 1), instance.color("&cStun Ability"),
				"", instance.color("&7Right click to stun players"), instance.color("&7within 6 blocks!"));
		return item;
	}

	public ItemStack getScareAbility() {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.BEACON, 1), instance.color("&cScare Ability"), "",
				instance.color("&7Right click to scare your opponents by blinding"),
				instance.color("&7them and teleporting to a random player"));
		return item;
	}

}