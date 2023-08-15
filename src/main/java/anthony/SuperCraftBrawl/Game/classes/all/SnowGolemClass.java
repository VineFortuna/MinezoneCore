package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnowGolemClass extends BaseClass {

	private Cooldown pumpkinCooldown = new Cooldown(200);
	private Cooldown platformCooldown = new Cooldown(20 * 1000);

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
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		ItemMeta meta = item.getItemMeta();

		if (item != null) {
			// SNOW PLATFORM ABILITY
			if (item.getType() == Material.SNOW_BLOCK
					&& (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
				if (platformCooldown.useAndResetCooldown()) {
					if (player.getGameMode() != GameMode.SPECTATOR) {
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

										platformBlock.removeMetadata("SnowPlatform", instance.getManager().getMain());
									}
								}
							}
						}, 3 * 20);

						// PLAYING SOUND FOR REMOVING PLATFORM
						for (Player gamePlayer : instance.players)
							gamePlayer.playSound(playerLocation, Sound.DIG_SNOW, 4, 4);
					}
				}
			}

			// PUMPKIN HEAD ABILITY
			if (item.getType() == Material.PUMPKIN
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					if (pumpkinCooldown.useAndResetCooldown()) {
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
								player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 3, 1);

								if (player != gamePlayer) {

									// Pumpkin Head Sound
									gamePlayer.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 3, 4);

									// Pumpkin Head Duration and Application
									BukkitRunnable runTimer = new BukkitRunnable() {

										int ticks = 10;

										@Override
										public void run() {
											if (ticks == 10) {
												if (gamePlayer.getGameMode() != GameMode.SPECTATOR)
													gamePlayer.getInventory()
															.setHelmet(new ItemStack(Material.PUMPKIN));
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
