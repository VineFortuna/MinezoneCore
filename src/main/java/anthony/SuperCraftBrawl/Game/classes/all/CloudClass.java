package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CloudClass extends BaseClass {

	// All different wools
	private boolean whiteWool = false, lightGrayWool = false, cyanWool = false, blackWool = false, brownWool = false;
	private int cooldownSec = 0;
	// Checking if sword was used or not
	private boolean used = false;
	private ItemStack wool = ItemHelper.setDetails(new ItemStack(Material.WOOL),
			instance.getGameManager().getMain().color("&b&lStorm Cloud"));
	private List<ItemStack> list = new ArrayList<>(Arrays.asList(
			ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BLACK.getData()),
					instance.getGameManager().getMain().color("&b&lStorm Cloud")),
			ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.CYAN.getData()),
					instance.getGameManager().getMain().color("&b&lStorm Cloud")),
			ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.GRAY.getData()),
					instance.getGameManager().getMain().color("&b&lStorm Cloud")),
			ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.RED.getData()),
					instance.getGameManager().getMain().color("&b&lStorm Cloud")),
			ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, DyeColor.BROWN.getData()),
					instance.getGameManager().getMain().color("&b&lStorm Cloud"))));

	public CloudClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.3;
	}

	@Override
	public ClassType getType() {
		return ClassType.Cloud;
	}

	public ItemStack makeGray(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.GRAY);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		String texture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDQ4Mjg3NzM3MTU0NDZlNDczZjVhMTY0OTMzYTVkODgyM2RmYjg1OGFmOTA0OTAxMzVjNDE3ZWZlMTY1OTgxZCJ9fX0=";
		ItemStack skull = ItemHelper.createSkullTexture(texture, "");
		
		playerEquip.setHelmet(skull);
		playerEquip.setChestplate(makeGray(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeGray(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeGray(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 1));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.WOOD_SWORD),
				instance.getGameManager().getMain().color("&b&lCloud Sword"));
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	private void pushPlayersBack(Player player) {
		List<Entity> near = player.getNearbyEntities(10.0D, 10.0D, 10.0D);
		for (Entity target : near) {
			if (target instanceof Player) {
				if (target != player) {
					// Calculate the vector between the player and the target
					Vector direction = target.getLocation().subtract(player.getLocation()).toVector();

					// Adjust the vector to push the target backwards
					direction.setY(2).normalize().multiply(2.0);
					target.setVelocity(direction);
				}
			}
		}
	}

	private void strikeNearbyPlayers(Player player) {
		List<Entity> near = player.getNearbyEntities(20.0D, 20.0D, 20.0D);

		for (Entity e : near) {
			if (e instanceof Player) {
				Player gamePlayer = (Player) e;
				if (instance.classes.containsKey(gamePlayer) && instance.classes.get(gamePlayer).getLives() > 0) {
					gamePlayer.getWorld().strikeLightningEffect(gamePlayer.getLocation());
					gamePlayer.setFireTicks(100);
					gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 1));
					gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 90, 0));
					gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 2));
				}
			}
		}
	}

	// Spawns tnt rain above every player in game
	private void tntRain(Player player) {
		BukkitRunnable runnable = new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 5)
					this.cancel();

				for (Player gamePlayer : instance.players) {
					if (gamePlayer != player) {
						if (instance.classes.containsKey(gamePlayer)
								&& instance.classes.get(gamePlayer).getLives() > 0) {
							Location playerLoc = gamePlayer.getLocation();
							TNTPrimed tnt = gamePlayer.getWorld().spawn(playerLoc.add(0, 20, 0), TNTPrimed.class);
							tnt.setFuseTicks(100);
							gamePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 140, 2));
						}
					}
				}

				ticks++;
			}

		};
		runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
	}

	private void resetAllWools() {
		this.whiteWool = false;
		this.blackWool = false;
		this.lightGrayWool = false;
		this.cyanWool = false;
		this.brownWool = false;
	}

	private void stormCloud() {
		if (player.getInventory().contains(this.list.get(3)))
			if (cloud.getTime() < 20000)
				return;

		// Storm cloud stuff:
		int playerY = player.getLocation().getBlockY();
		int groundY = player.getWorld().getHighestBlockYAt(player.getLocation().getBlockX(),
				player.getLocation().getBlockZ());

		if (player.getHealth() > 12 && player.isOnGround()) { // For white wool
			if (whiteWool == false) {
				resetAllWools();
				this.whiteWool = true;
				player.getInventory().setItem(1, this.wool);
			}
		} else if (player.getHealth() <= 8 && player.isOnGround()) { // For light gray wool
			if (lightGrayWool == false) {
				resetAllWools();
				this.lightGrayWool = true;
				player.getInventory().setItem(1, this.list.get(2));
			}
		} else if (playerY - groundY > 5) {
			if (cyanWool == false) {
				resetAllWools();
				this.cyanWool = true;
				player.getInventory().setItem(1, this.list.get(1));
			}
		} else if (player.getHealth() > 8 && player.getHealth() <= 12) {
			if (brownWool == false) {
				resetAllWools();
				this.brownWool = true;
				player.getInventory().setItem(1, this.list.get(4));
			}
		} else {
			if (blackWool == false) {
				resetAllWools();
				this.blackWool = true;
				player.getInventory().setItem(1, this.list.get(0));
			}
		}

		if (this.used) {
			if (whiteWool) {
				pushPlayersBack(player);
				this.whiteWool = false;
				player.getInventory().setItem(1, this.list.get(3));
			}
			if (blackWool) {
				shootSnowballs(player);
				this.blackWool = false;
				player.getInventory().setItem(1, this.list.get(3));
				instance.TellAll(instance.getGameManager().getMain().color("&b&lStorm Cloud: &rGET OUTTA MY WAY!"));
			}
			if (lightGrayWool) {
				if (getNearestPlayer()) {
					this.lightGrayWool = false;
					player.getInventory().setItem(1, this.list.get(3));
				}
			}
			if (cyanWool) {
				strikeNearbyPlayers(player);
				this.cyanWool = false;
				player.getInventory().setItem(1, this.list.get(3));
				instance.TellAll(instance.getGameManager().getMain().color("&b&lStorm Cloud: &rLightning blast away!"));
			}
			if (brownWool) {
				tntRain(player);
				brownWool = false;
				player.getInventory().setItem(1, this.list.get(3));
				instance.TellAll(instance.getGameManager().getMain().color("&b&lStorm Cloud: &rIncoming rain!"));
			}
			this.used = false;
		}
	}

	@Override
	public void Tick(int gameTicks) {
		stormCloud();
		// Cooldown stuff:
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Cloud
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (20000 - cloud.getTime()) / 1000 + 1;

			if (cloud.getTime() < 20000) {
				String msg = instance.getGameManager().getMain()
						.color("&b&lStorm Cloud &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "cloud.cooldown", msg, 2);

			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &b&lStorm Cloud");
				getActionBarManager().setActionBar(player, "cloud.cooldown", msg, 2);
			}
		}
	}

	// Send messages to player each life about abilities
	private void forcesOfNature() {
		player.sendMessage(instance.getGameManager().getMain().color("&9&lForces Of Nature"));
		player.sendMessage(instance.getGameManager().getMain().color("&r&lStorm Wind: &rBlow away nearby enemies"));
		player.sendMessage("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Lightning Strike: " + ChatColor.RESET
				+ "Channel lightning to nearest player");
		player.sendMessage(instance.getGameManager().getMain()
				.color("&7&lLightning Blast: &rChannel lightning to all near players & do effects"));
		player.sendMessage(instance.getGameManager().getMain().color("&0&lSnow Blast: &rPelt enemies with snow"));
		player.sendMessage(instance.getGameManager().getMain().color("&c&lTNT Rain: &rSend TNT rain on all enemies"));
	}

	@Override
	public void SetItems(Inventory playerInv) {
		cloud.startTime = System.currentTimeMillis() - 100000;
		this.whiteWool = false; // Default when spawning in
		this.blackWool = false;
		this.lightGrayWool = false;
		this.cyanWool = false;
		this.brownWool = false;
		this.used = false;
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, this.wool);
		forcesOfNature();
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();

		// Check if the player right-clicked with a snowball
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = player.getItemInHand();
			if (item != null && item.getType() == Material.WOOD_SWORD && this.used == false) {
				if (cloud.getTime() < 20000) {
					int seconds = (20000 - cloud.getTime()) / 1000 + 1;
					event.setCancelled(true);
					player.sendMessage(instance.getGameManager().getMain()
							.color("&c&l(!) &rYour &b&lStorm Cloud &ris still regenerating for &e" + seconds + "s"));
				} else {
					cloud.restart();
					this.used = true; // When sword is used, it'll do different things in the Tick() function above
				}
			}
		}
	}

	private boolean getNearestPlayer() {
		List<Entity> near = player.getNearbyEntities(20.0D, 20.0D, 20.0D);
		Player nearestPlayer = null;
		double shortestDistance = Double.MAX_VALUE;

		for (Entity entity : near) {
			if (entity instanceof Player) {
				Player target = (Player) entity;
				double distance = target.getLocation().distance(player.getLocation());

				if (distance < shortestDistance) {
					nearestPlayer = target;
					shortestDistance = distance;
				}
			}
		}

		if (nearestPlayer != null) {
			nearestPlayer.getWorld().strikeLightningEffect(nearestPlayer.getLocation());
			damageTarget(nearestPlayer, 8.0);
			nearestPlayer.setFireTicks(80);
			return true; // Found player
		}

		player.sendMessage(
				instance.getGameManager().getMain().color("&2&l(!) &rNo nearby players found. Please try again."));
		return false; // Didn't find cuz get good kid
	}

	private void damageTarget(Player target, double value) { // Damage a specific player
		@SuppressWarnings("deprecation")
		EntityDamageEvent damageEvent = new EntityDamageEvent(target, DamageCause.PROJECTILE, value);
		instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
		target.damage(value, player);
	}

	private void shootSnowballs(Player player) {
		BukkitRunnable runnable = new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 130 || player.getGameMode() == GameMode.SPECTATOR
						|| (instance.classes.containsKey(player) && instance.classes.get(player).getLives() <= 0)) {
					this.cancel();
					return;
				}
				// Calculate the player's rotation
				double rotation = (player.getLocation().getYaw() - 90) % 360;
				if (rotation < 0)
					rotation += 360.0;

				// Calculate the cardinal direction based on the rotation
				int direction = (int) Math.round(rotation / 45.0) % 8;

				// Spawn snowball entities with the appropriate velocities based on the
				// direction
				for (int i = 0; i < 8; i++) {
					Snowball snowball = player.launchProjectile(Snowball.class);
					snowball.setVelocity(player.getLocation().getDirection().multiply(2.0));
					switch (direction) {
					case 0:
						snowball.setVelocity(player.getLocation().getDirection().setZ(-1).multiply(2.0));
						break;
					case 1:
						snowball.setVelocity(player.getLocation().getDirection().setX(1).multiply(2.0));
						break;
					case 2:
						snowball.setVelocity(player.getLocation().getDirection().setZ(1).multiply(2.0));
						break;
					case 3:
						snowball.setVelocity(player.getLocation().getDirection().setX(-1).multiply(2.0));
						break;
					case 4:
						snowball.setVelocity(player.getLocation().getDirection().setZ(-1)
								.add(player.getLocation().getDirection().setX(1)).multiply(2.0));
						break;
					case 5:
						snowball.setVelocity(player.getLocation().getDirection().setZ(1)
								.add(player.getLocation().getDirection().setX(1)).multiply(2.0));
						break;
					case 6:
						snowball.setVelocity(player.getLocation().getDirection().setZ(1)
								.add(player.getLocation().getDirection().setX(-1)).multiply(2.0));
						break;
					case 7:
						snowball.setVelocity(player.getLocation().getDirection().setZ(-1)
								.add(player.getLocation().getDirection().setX(-1)).multiply(2.0));
						break;
					}
					direction = (direction + 1) % 8;
				}

				ticks += 5;
			}
		};
		runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 5);
	}
}
