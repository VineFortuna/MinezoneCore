package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class SnowGolemClass extends BaseClass {

	private int cooldownSec = 0;

	private ItemStack weapon;

	public SnowGolemClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.3;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		// Head (helmet)
		ItemStack playerHead = ItemHelper.createSkullHeadPlayer(1, "SnowGolem", ChatColor.WHITE + "SnowGolem Head");

		// Chestplate
		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.WHITE,
				ChatColor.WHITE + "SnowGolem's Chestplate");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Leggings
		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.WHITE,
				ChatColor.WHITE + "SnowGolem's Leggings");

		// Boots
		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE,
				ChatColor.WHITE + "SnowGolem's Boots");
		boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Setting armor
		playerEquip.setHelmet(playerHead);
		playerEquip.setChestplate(chestplate);
		playerEquip.setLeggings(leggings);
		playerEquip.setBoots(boots);
	}

	@Override
	public void SetItems(Inventory playerInv) {

		// Weapon
		ItemStack weapon = ItemHelper.create(Material.STICK, ChatColor.GREEN + "Map Knocker");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);

		this.weapon = weapon;

		// Snow Platform
		ItemStack snowPlatform = ItemHelper.create(Material.SNOW_BLOCK, ChatColor.WHITE + "Snow Platform",
				Collections.singletonList(ChatColor.GRAY + "Right click to save yourself from falling"));

		// Slowballs
		ItemStack slowballs = new ItemStack(
				ItemHelper.create(Material.SNOW_BALL, "" + ChatColor.RED + ChatColor.BOLD + "Slowballs").getType(), 5);

		// Pumpkin
		List<String> pumpkinList = new ArrayList<>();
		pumpkinList.add(ChatColor.RESET + "Right click to annoy other players");
		ItemStack pumpkin = ItemHelper.create(Material.PUMPKIN, ChatColor.GRAY + "Pumpkin", pumpkinList);

		// Setting items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, snowPlatform);
		playerInv.setItem(2, slowballs);
		playerInv.setItem(3, pumpkin);
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.SnowGolem
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (20000 - snowGolem.getTime()) / 1000 + 1;

			if (snowGolem.getTime() < 20000) {
				String msg = instance.getManager().getMain()
						.color("&bSnow Platform &rregenerates in: &e" + this.cooldownSec + "s");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			} else {
				String msg = instance.getManager().getMain().color("&rYou can use &bSnow Platform");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			// SNOW PLATFORM ABILITY
			if (item.getType() == Material.SNOW_BLOCK
					&& (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					if (snowGolem.getTime() < 20000) {
						int seconds = (20000 - snowGolem.getTime()) / 1000 + 1;
						event.setCancelled(true);
						player.sendMessage(instance.getManager().getMain().color(
								"&c&l(!) &rYour &bSnow Platform &ris still regenerating for &e" + seconds + "s"));
					} else {
						snowGolem.restart();
						BukkitRunnable runnable = new BukkitRunnable() {
							int ticks = 0;

							@Override
							public void run() {
								if (ticks == 8) {
									this.cancel();
								} else {
									Location targetLocation = new Location(player.getWorld(),
											player.getLocation().getX(), player.getLocation().getY() + 1,
											player.getLocation().getZ()); // Replace with
																			// your target
																			// location

									float originalYaw = player.getLocation().getYaw();
									float originalPitch = player.getLocation().getPitch();

									player.teleport(targetLocation);

									Location newLocation = player.getLocation();
									newLocation.setYaw(originalYaw);
									newLocation.setPitch(originalPitch);

									player.teleport(newLocation);
									World playerWorld = player.getWorld();
									Location playerLocation = player.getLocation();

									// CREATING PLATFORM
									int platformLength = 3; // Platform Length
									int platformWidth = 3; // Platform Width

									for (int x = -platformLength / 2; x <= platformLength / 2; x++) {
										for (int z = -platformWidth / 2; z <= platformWidth / 2; z++) {
											Location platformLocation = playerLocation.clone().add(x, -1, z);
											Block platformBlock = playerWorld.getBlockAt(platformLocation);

											if (platformBlock.getType() == Material.AIR) {
												platformBlock.setType(Material.SNOW_BLOCK);
												platformBlock.setMetadata("SnowPlatform",
														new FixedMetadataValue(instance.getManager().getMain(), true));
											}
										}
									}

									// PLAYING SOUND FOR CREATING PLATFORM
									for (Player gamePlayer : instance.players)
										gamePlayer.playSound(playerLocation, Sound.STEP_SNOW, 4, 2);

									// REMOVING PLATFORM
									Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
										for (int x = -platformLength / 2; x <= platformLength / 2; x++) {
											for (int z = -platformWidth / 2; z <= platformWidth / 2; z++) {
												Location platformLocation = playerLocation.clone().add(x, -1, z);
												Block platformBlock = playerWorld.getBlockAt(platformLocation);

												if (platformBlock.hasMetadata("SnowPlatform")) {
													platformBlock.setType(Material.AIR);

													platformBlock.removeMetadata("SnowPlatform",
															instance.getManager().getMain());
												}
											}
										}
									}, 3 * 20);

									// PLAYING SOUND FOR REMOVING PLATFORM
									for (Player gamePlayer : instance.players)
										gamePlayer.playSound(playerLocation, Sound.DIG_SNOW, 4, 4);

									ticks++;
								}
							}

						};
						runnable.runTaskTimer(instance.getManager().getMain(), 0, 2);
					}
				}
			}

			// PUMPKIN HEAD ABILITY
			if (item.getType() == Material.PUMPKIN
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);

						for (Player gamePlayer : instance.players) {
							BaseClass baseClass = instance.classes.get(gamePlayer);

							// Pumpkin Head Feedback Sound
							player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

							if (player != gamePlayer) {

								// Pumpkin Head Sound
								gamePlayer.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1, 4);

								// Pumpkin Head Duration and Application
								BukkitRunnable runTimer = new BukkitRunnable() {

									int ticks = 10;

									@Override
									public void run() {
										if (ticks == 10) {
											if (gamePlayer.getGameMode() != GameMode.SPECTATOR)
												gamePlayer.getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
										} else if (ticks == 0) {
											baseClass.LoadArmor(gamePlayer);
											this.cancel();
										}

										ticks--;
									}
								};
								runTimer.runTaskTimer(instance.getManager().getMain(), 0, 20);
							}
						}
					}
					event.setCancelled(true);
				}
			}
		}
	}

	@Override
	public ClassType getType() {
		return ClassType.SnowGolem;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}
}
