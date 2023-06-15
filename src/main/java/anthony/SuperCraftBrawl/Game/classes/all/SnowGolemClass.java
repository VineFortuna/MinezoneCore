package anthony.SuperCraftBrawl.Game.classes.all;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
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
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import net.md_5.bungee.api.ChatColor;

public class SnowGolemClass extends BaseClass {

	private Cooldown shurikenCooldown = new Cooldown(200);

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
		playerInv
				.setItem(0,
						ItemHelper.addEnchant(
								ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.STICK),
										ChatColor.GREEN + "Map Knocker"), Enchantment.DAMAGE_ALL, 3),
								Enchantment.KNOCKBACK, 2));
		playerInv.setItem(1, this.getSnowballs());
		playerInv.setItem(2,
				ItemHelper.setDetails(new ItemStack(Material.PUMPKIN),
						instance.getManager().getMain().color("&rPumpkin"),
						instance.getManager().getMain().color("&7Right click to annoy other players")));
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		ItemMeta meta = item.getItemMeta();

		if (item != null) {
			if (item.getType() == Material.SNOW_BALL) {
				if (!(meta.getDisplayName().contains("Slowball"))) {
					if (player.getGameMode() != GameMode.SPECTATOR) {
						if (shurikenCooldown.useAndResetCooldown()) {
							int amount = item.getAmount();
							if (amount > 0) {
								amount--;
								if (amount == 0)
									player.getInventory().clear(player.getInventory().getHeldItemSlot());
								else
									item.setAmount(amount);
								ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
									@Override
									public void onHit(Player hit) {
										if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
											Location hitLoc = this.getBaseProj().getEntity().getLocation();
											player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);

											for (Player gamePlayer : this.getNearby(3.0)) {
												if (instance.duosMap != null) {
													if (!(instance.team.get(gamePlayer)
															.equals(instance.team.get(player)))) {
														EntityDamageEvent damageEvent = new EntityDamageEvent(
																gamePlayer, DamageCause.VOID, 5.5);
														instance.getManager().getMain().getServer().getPluginManager()
																.callEvent(damageEvent);
														gamePlayer.damage(5.5, player);
													}
												} else {
													EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
															DamageCause.VOID, 5.5);
													instance.getManager().getMain().getServer().getPluginManager()
															.callEvent(damageEvent);
													gamePlayer.damage(5.5, player);
												}
											}
											for (Player gamePlayer : instance.players) {
												gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
												gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
											}
										}

									}

								}, new ItemStack(Material.SNOW_BALL));
								instance.getManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
										player.getLocation().getDirection().multiply(2.5D));
							}
							event.setCancelled(true);
						}
					}
				}
			} else if (item.getType() == Material.PUMPKIN
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
