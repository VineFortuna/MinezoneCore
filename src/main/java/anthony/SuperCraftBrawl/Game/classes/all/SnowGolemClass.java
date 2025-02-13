package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SnowGolemClass extends BaseClass {

	private int cooldownSec = 0;

	private ItemStack weapon;
	private final Ability pumpkinAbility = new Ability("&6&lPumpkin Head", player);
	private final PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) (PUMPKIN_ABILITY_DURATION * 20), 0, false, true);
	private static final double PUMPKIN_ABILITY_DURATION = 5;
	private static final double PUMPKIN_ABILITY_RANGE = 10;

	public SnowGolemClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU2MzdhY2FkYWY3Nzc1OGFiYzdkMjQyZDRiODVmY2MyMGNhODM1NDU4MWI5MzNjMDE1Y2Y4NDVhYWFkMzQ4NSJ9fX0=",
				"FFFFFF",
				6,
				"SnowGolem"
		);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		// Weapon
		ItemStack weapon = ItemHelper.create(Material.STICK, ChatColor.GREEN + "Map Knocker");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1); // Knockback 1

		this.weapon = weapon;

		// Snow Platform
		ItemStack snowPlatform = ItemHelper.create(Material.SNOW_BLOCK, ChatColor.WHITE + "Snow Platform", ChatColor.GRAY + "Right click to save yourself from falling");

		// Slowballs
		ItemStack slowballs =
				ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL, 5),
				"&f&lSLOWBALLS &7(Right click)",
				"&7Give Slowness 1 for 3s to an enemy"
		);

		// Pumpkin
		String radiusDisplay = ItemHelper.formatDouble(PUMPKIN_ABILITY_RANGE);
		String durationDisplay = ItemHelper.formatDouble(PUMPKIN_ABILITY_DURATION);

		ItemStack pumpkin = ItemHelper.setDetails(
				new ItemStack(Material.PUMPKIN),
				pumpkinAbility.getAbilityNameRightClickMessage(),
				"&7Put a pumpkin on your enemies' head",
				"",
				"&7Gives you &4&oStrength &e" + (strength.getAmplifier() + 1) + " &7for &e" + strength.getDuration() / 20 + "s",
				"&7Duration: &a" + durationDisplay + "s",
				"&7Range: &a" + radiusDisplay + " &7blocks"
		);

		// Setting items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, snowPlatform);
		playerInv.setItem(2, slowballs);
		playerInv.setItem(3, pumpkin);
		snowGolem.startTime = System.currentTimeMillis() - 100000;
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.SnowGolem
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (20000 - snowGolem.getTime()) / 1000 + 1;

			if (snowGolem.getTime() < 20000) {
				String msg = instance.getGameManager().getMain()
						.color("&bSnow Platform &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "platform.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &bSnow Platform");
				getActionBarManager().setActionBar(player, "platform.cooldown", msg, 2);
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
						player.sendMessage(instance.getGameManager().getMain().color(
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
											player.getLocation().getZ());

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
														new FixedMetadataValue(instance.getGameManager().getMain(), true));

												// PLAYING SOUND FOR CREATING PLATFORM
												SoundManager.playSoundToAll(player, platformLocation, Sound.STEP_SNOW, 2, 1.5f);
											}
										}
									}

									// REMOVING PLATFORM
									Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
										for (int x = -platformLength / 2; x <= platformLength / 2; x++) {
											for (int z = -platformWidth / 2; z <= platformWidth / 2; z++) {
												Location platformLocation = playerLocation.clone().add(x, -1, z);
												Block platformBlock = playerWorld.getBlockAt(platformLocation);

												if (platformBlock.hasMetadata("SnowPlatform")) {
													platformBlock.setType(Material.AIR);

													platformBlock.removeMetadata("SnowPlatform",
															instance.getGameManager().getMain());
													// PLAYING SOUND FOR REMOVING PLATFORM
													SoundManager.playSoundToAll(player, platformLocation, Sound.DIG_SNOW, 2, 2);
												}
											}
										}
									}, 4 * 20);

									ticks++;
								}
							}

						};
						runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 2);
					}
				}
			}

			// PUMPKIN HEAD ABILITY
			if (item.getType() == Material.PUMPKIN
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					int amount = item.getAmount();
					if (amount > 0) {
						boolean foundPlayers = false;

						for (Entity entity : player.getWorld().getNearbyEntities(
								player.getLocation(),
								PUMPKIN_ABILITY_RANGE,
								PUMPKIN_ABILITY_RANGE,
								PUMPKIN_ABILITY_RANGE
						)) {
							if (entity instanceof Player && !entity.equals(player)) {
								Player playerInRange = (Player) entity;
								if (!checkIfDead(playerInRange, instance) && !instance.HasSpectator(playerInRange)) {
									usePumpkinAbility(playerInRange);
									foundPlayers = true;
								}
							}
						}
						if (foundPlayers) {
							amount--;
							if (amount == 0) player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else item.setAmount(amount);
							// Adding strength
							player.addPotionEffect(strength);
							// Pumpkin Head Feedback Sound
							player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
						} else player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
					}
					event.setCancelled(true);
				}
			}
		}
	}

	private void usePumpkinAbility(Player playerInRange) {
		// Pumpkin Head Sound
		playerInRange.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 1, 2);

		// Pumpkin Head Duration and Application
		BukkitRunnable runTimer = new BukkitRunnable() {

			int ticks = (int) PUMPKIN_ABILITY_DURATION;
			BaseClass baseClass = instance.classes.get(playerInRange);

			@Override
			public void run() {
				if (ticks == PUMPKIN_ABILITY_DURATION) {
					if (!checkIfDead(playerInRange, instance)) {
						ItemStack pumpkin = new ItemStack(Material.PUMPKIN);
						pumpkin.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6);
						playerInRange.getInventory().setHelmet(pumpkin);
					}
				} else if (ticks == 0) {
					if (baseClass.getType() == ClassType.Fade && baseClass.fadeAbilityActive) {
						playerInRange.getEquipment().setHelmet(ItemHelper.create(Material.AIR));
					} else {
						baseClass.resetHead();
					}
					this.cancel();
				}

				ticks--;
			}
		};
		runTimer.runTaskTimer(instance.getGameManager().getMain(), 0, 20); // 20 ticks = 1 second
	}

	@Override
	public ClassType getType() {
		return ClassType.SnowGolem;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}

}
