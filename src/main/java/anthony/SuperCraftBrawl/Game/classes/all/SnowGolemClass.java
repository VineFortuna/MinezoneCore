package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameManager;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SnowGolemClass extends BaseClass {

	private Cooldown shurikenCooldown = new Cooldown(200);
	private Cooldown platformCooldown = new Cooldown(20*1000);

	public SnowGolemClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.3;
	}

	public ItemStack makeWhite(ItemStack armour) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.WHITE);
		armour.setItemMeta(lm);
		return armour;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();

		meta.setOwner("SnowGolem");
		meta.setDisplayName("");

		playerskull.setItemMeta(meta);

		playerEquip.setHelmet(playerskull);
		playerEquip.setChestplate(makeWhite(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeWhite(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeWhite(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	public ItemStack getSnowballs() {
		return ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL, 12), ChatColor.GREEN + "Snowballs");
	}

	@Override
	public void SetItems(Inventory playerInv) {
		ItemStack slowballs = new ItemStack(ItemHelper.create(Material.SNOW_BALL, "" + ChatColor.RED + ChatColor.BOLD + "Slowballs").getType(), 5);


		playerInv.setItem(0,
				ItemHelper.addEnchant(
						ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.STICK),
								ChatColor.GREEN + "Map Knocker"), Enchantment.DAMAGE_ALL, 3),
						Enchantment.KNOCKBACK, 2));
		playerInv.setItem(1,
				ItemHelper.create(Material.SNOW_BLOCK,"&fSnow Platform", Collections.singletonList("&7Right click to save yourself from falling")));
		playerInv.setItem(2, slowballs);
		playerInv.setItem(3,
				ItemHelper.setDetails(new ItemStack(Material.PUMPKIN),
						instance.getManager().getMain().color("&rPumpkin"),
						instance.getManager().getMain().color("&7Right click to annoy other players")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		ItemMeta meta = item.getItemMeta();

		if (item != null) {
			// SNOW PLATFORM ABILITY
			if (item.getType() == Material.SNOW_BLOCK &&
					(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
				if (platformCooldown.useAndResetCooldown()) {
					if (player.getGameMode() != GameMode.SPECTATOR) {
						World playerWorld = player.getWorld();
						Location playerLocation = player.getLocation();

						// CREATING PLATFORM
						int platformLength = 3; // Platform Length
						int platformWidth = 3; // Platform Width

						for (int x = -platformLength / 2; x <= platformWidth / 2; x++) {
							for (int z = -platformWidth / 2; z <= platformLength / 2; z++) {
								Location platformLocation = playerLocation.clone().subtract(0, 1, 0);
								Block platformBlock = playerWorld.getBlockAt(platformLocation);

								if (platformBlock.getType() == Material.AIR) {
									platformBlock.setType(Material.SNOW_BLOCK);
									platformBlock.setMetadata("SnowPlatform", new FixedMetadataValue(instance.getManager().getMain(), true));
								}
							}
						}

						// PLAYING SOUND FOR CREATING PLATFORM
						playerWorld.playSound(playerLocation, Sound.DIG_SNOW, 2, 1);

						// REMOVING PLATFORM
						Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
							for (int x = -platformLength / 2; x <= platformWidth / 2; x++) {
								for (int z = -platformWidth / 2; z <= platformLength / 2; z++) {
									Location platformLocation = playerLocation.clone().subtract(0, 1, 0);
									Block platformBlock = playerWorld.getBlockAt(platformLocation);

									if (platformBlock.hasMetadata("SnowPlatform")) {
										platformBlock.setType(Material.AIR);

										platformBlock.removeMetadata("SnowPlatform", instance.getManager().getMain());
									}
								}
							}
						}, 3 * 20);

						// PLAYING SOUND FOR REMOVING PLATFORM
						playerWorld.playSound(playerLocation, Sound.DIG_SNOW, 2, 2);
					}
				}
			}

			if (item.getType() == Material.PUMPKIN
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					if (shurikenCooldown.useAndResetCooldown()) {
						int amount = item.getAmount();
						if (amount > 0) {
							amount--;
							if (amount == 0)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else
								item.setAmount(amount);

							for (Player gamePlayer : instance.players) {
								BaseClass baseClass = instance.classes.get(gamePlayer);
								if (player != gamePlayer) {
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
	public void SetNameTag() {

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.STICK), ChatColor.GREEN + "Map Knocker"),
				Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 2);
		return item;
	}

}
