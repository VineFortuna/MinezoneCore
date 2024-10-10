package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class WitherSkeletonClass extends BaseClass {

	private int cooldown = 0;
	private boolean used = false;
	private ItemStack bow;

	public WitherSkeletonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return ClassType.WitherSk;
	}

	public ItemStack makeBlack(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.BLACK);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.WITHER.ordinal());
		playerEquip.setHelmet(getHelmet(playerskull));
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.addEnchant(
						ItemHelper
								.addEnchant(
										ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER),
												instance.getGameManager().getMain()
														.color("&rEye of Ender &7(Right Click)")),
										Enchantment.DAMAGE_ALL, 1),
						Enchantment.KNOCKBACK, 1);
		return item;
	}

	@Override
	public void ProjectileLaunch(ProjectileLaunchEvent event) {
		Entity e = event.getEntity();

		if (this.used == true) {
			if (e instanceof Arrow)
				event.setCancelled(true);
		} else {
			if (e instanceof Arrow) {
				for (Player p : instance.players)
					p.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);

				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
				this.used = true;
			}
		}
	}

	@Override
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() > 0
				&& instance.classes.get(player).getType() == ClassType.WitherSk)
			if (!(player.getInventory().contains(this.getAttackWeapon())))
				player.getInventory().setItem(0, this.getAttackWeapon()); // If some rare chance the player throws away
																			// their melee

		if (this.used == true) {
			for (Entity e : instance.getMapWorld().getEntities()) {
				if (e instanceof Arrow) {
					Arrow a = (Arrow) e;

					if (a.getShooter() instanceof Player) {
						if (a.getShooter() == player) {
							if (a.isOnGround()) {
								for (Player gamePlayer : instance.players)
									gamePlayer.playEffect(a.getLocation(), Effect.EXPLOSION_HUGE, 1);
								List<Entity> near = a.getNearbyEntities(2.5D, 2.0D, 2.5D);

								for (Entity en : near) {
									if (en instanceof Player) {
										Player p = (Player) en;

										if (instance.duosMap != null) {
											if (!(instance.team.get(p).equals(instance.team.get(player)))) {
												p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
												@SuppressWarnings("deprecation")
												EntityDamageEvent damageEvent = new EntityDamageEvent(p,
														DamageCause.WITHER, 8.0);
												instance.getGameManager().getMain().getServer().getPluginManager()
														.callEvent(damageEvent);
												p.damage(8.0, player);
											}
										} else {
											p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
											@SuppressWarnings("deprecation")
											EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.WITHER,
													8.0);
											instance.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											p.damage(8.0, player);
										}
									}
								}
								e.remove();
							}
						}
					}
				}
			}
			if (gameTicks % 20 == 0) {
				if (this.bow != null) {
					if (instance.state == GameState.ENDED) {
						player.getInventory().clear();
						return;
					}

					if (cooldown == 0)
						this.bow.setDurability((short) 385);
					else if (cooldown == 1) {
						this.bow.setDurability((short) 320);
					} else if (cooldown == 2) {
						this.bow.setDurability((short) 280);
					} else if (cooldown == 3) {
						this.bow.setDurability((short) 230);
					} else if (cooldown == 4) {
						this.bow.setDurability((short) 202);
					} else if (cooldown == 5) {
						this.bow.setDurability((short) 170);
					} else if (cooldown == 6) {
						this.bow.setDurability((short) 145);
					} else if (cooldown == 7) {
						this.bow.setDurability((short) 100);
					} else if (cooldown == 8) {
						this.bow.setDurability((short) 50);
					} else if (cooldown == 9) {
						this.bow.setDurability((short) 0);
					}

					player.getInventory().remove(Material.BOW);
					player.getInventory().addItem(this.bow);
					cooldown++;

					if (cooldown >= 9) {
						this.used = false;
						this.cooldown = 0;
						player.getInventory().remove(Material.BOW);
						player.getInventory()
								.addItem(
										ItemHelper
												.addEnchant(
														ItemHelper.addEnchant(
																ItemHelper.setDetails(new ItemStack(Material.BOW),
																		instance.getGameManager().getMain()
																				.color("&7&lWitherSk Bow")),
																Enchantment.DURABILITY, 1000),
														Enchantment.ARROW_INFINITE, 1));
						String msg = instance.getGameManager().getMain()
								.color("&9&l(!) &eYou can now use &7&lWither's Bow");
						getActionBarManager().setActionBar(player, "wither.skeleton.cooldown", msg, 2);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow) {
			Arrow a = (Arrow) event.getDamager();
			if (a.getShooter() instanceof Player) {
				if (event.getEntity() instanceof Player) {
					Player p = (Player) event.getEntity();
					if (instance.duosMap != null) {
						if (instance.team.get(p).equals(instance.team.get(player))) {
							event.setCancelled(true);
							return;
						}
					}

					Random r = new Random();
					int chance = r.nextInt(100);
					double dmg = 5.0;

					if (chance >= 0 && chance < 2)
						dmg = 18.0;
					else if (chance >= 10 && chance <= 50)
						dmg = 10.0;
					else
						dmg = 8.0;

					@SuppressWarnings("deprecation")
					EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.WITHER, dmg);
					instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
					p.damage(dmg, player);
					p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
					Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();
					fwm.setPower(1);
					fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());
					fw.setFireworkMeta(fwm);

					for (Player gamePlayer : instance.players)
						gamePlayer.playEffect(p.getLocation(), Effect.EXPLOSION_HUGE, 1);
					return;
				}
			}
		} else if (event.getDamager() instanceof Player) {
			BaseClass bc = instance.classes.get(player);
			if (bc != null && bc.getLives() <= 0)
				return;
			Random rand = new Random();
			int chance = rand.nextInt(9);

			if (chance == 5 || chance == 1 || chance == 3 || chance == 7) {
				if (event.getEntity() instanceof Player) {
					Player p = (Player) event.getEntity();
					if (instance.duosMap != null)
						if (instance.team.get(p).equals(instance.team.get(player)))
							return;

					p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 0, true));
				}
			}
		}
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.cooldown = 0; // Reset each life
		this.used = false; // Same here
		this.bow = ItemHelper
				.addEnchant(ItemHelper.addEnchant(
						ItemHelper.setDetails(new ItemStack(Material.BOW),
								instance.getGameManager().getMain().color("&7&lWitherSk Bow")),
						Enchantment.DURABILITY, 1000), Enchantment.ARROW_INFINITE, 1);

		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, this.bow);
		playerInv.setItem(35, new ItemStack(Material.ARROW));
	}

	private void abilityMsg() {
		player.sendMessage("");
		player.sendMessage(instance.getGameManager().getMain().color(
				"&e&lCLASS TIP> &rShoot players with your bow to give Wither effect, & do damage. Also very rare chance to do INSANE damage :)"));
		player.sendMessage("");
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null) {
			if (item.getType() == Material.EYE_OF_ENDER
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				event.setCancelled(true);
				this.abilityMsg();
			}
		}
	}

}
