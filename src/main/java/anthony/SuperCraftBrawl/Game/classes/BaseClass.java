package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Timer;
import anthony.SuperCraftBrawl.gui.ClassRewardsGUI;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public abstract class BaseClass {

	protected final GameInstance instance;
	protected final Player player;
	public int lives = 5;
	public boolean isDead = false;
	public boolean fadeAbilityActive = false;
	public int tokens = 0;
	public Score score;
	public int totalTokens = 0;
	public int totalKills = 0;
	public int totalDeaths = 0;
	public int placement = 0;
	public int eachLifeKills = 0;
	public int totalExp = 0;
	public double baseVerticalJump = 1.0;
	public boolean flintUsed = false;
	public Timer pearlTimer = new Timer();
	public Timer tnt = new Timer();
	public Timer slimeBall = new Timer();
	public Timer tntItem = new Timer();
	public Timer ice = new Timer();
	public Timer golem = new Timer();
	public Timer herobrine = new Timer();
	public Timer bazooka = new Timer();
	public Timer ninja = new Timer();
	public Timer bee = new Timer();
	public Timer aggressiveGift = new Timer();
	public Timer defensiveGift = new Timer();
	public Timer mythicalGift = new Timer();
	public Timer wizard = new Timer();
	public Timer magmaCube = new Timer();
	public Timer ocelot = new Timer();
	public Timer cloud = new Timer();
	public Timer snowGolem = new Timer();
	public Timer santa = new Timer();
	public Timer cookie = new Timer();
	public Timer vindication = new Timer();
	public Timer fadeAbility = new Timer();
	public Timer summon = new Timer();
	public Timer grimReaper = new Timer();
	public Timer alexBrewingStand = new Timer();
	public Timer wolfBite = new Timer();
	public boolean bedrockInvincibility = false;
	public boolean hunterDash = true;
	public boolean wolfPackAdd = false;

	public int goldAmt = 0; // For Steve Class
	public int coalAmt = 0; // For Steve Class
	public int diaAmt = 0; // For Steve Class

	// Armor fields
	protected ItemStack playerHead;
	protected ItemStack chestplate;
	protected ItemStack leggings;
	protected ItemStack boots;

	public Player bountyTarget = null;

	public int gunGamePos = 0;

	// This would also take in a SuperClass.
	public BaseClass(GameInstance instance, Player player) {
		this.instance = instance;
		this.player = player;
	}

	public ActionBarManager getActionBarManager() {
		return instance.getGameManager().getMain().getActionBarManager();
	}

	public int getLives() {
		return lives;
	}

	public int getTokens() {
		return tokens;
	}

	public abstract ClassType getType();

	protected void setArmor(EntityEquipment playerEquip){
		setArmorNew(playerEquip);
	}

	public abstract ItemStack getAttackWeapon();

	public void SetNameTag(){};

	public abstract void SetItems(Inventory playerInv);

	public void UseItem(PlayerInteractEvent event){};

	public void onConsumingItem(PlayerItemConsumeEvent event) {
	};

	public void TakeDamage(EntityDamageEvent event) {
	}; // To override

	public void ProjectileLaunch(ProjectileLaunchEvent event) {
	}; // To override

	public void ProjectileHit(ProjectileHitEvent event) {
	}; // To override

	public void PotionSplashEvent(PotionSplashEvent event) {
	}; // To override

	public void DoDamage(EntityDamageByEntityEvent event) {
	} // To override

	public void DoDamage2(EntityDamageByEntityEvent event) {
	} // To override

	public void onEntityTarget(EntityTargetLivingEntityEvent event) {
	} // To override

	public void onPlayerMove(PlayerMoveEvent event) {
	}

	public void onFish(PlayerFishEvent event) {
	} // To override

	public void Tick(int gameTicks) {
	}// To override

	public boolean isPlayerAlive() {
		return instance.classes.containsKey(player)
				&& player.getGameMode() == GameMode.ADVENTURE
				&& instance.classes.get(player).getType() == getType()
				&& instance.classes.get(player).getLives() > 0;
	}

	public void GameEnd() {
	} // To override

	/**
	 * Equip class armor and custom head.
	 *
	 */
	protected void setArmorNew(EntityEquipment entityEquipment) {
		if (playerHead != null) {
			entityEquipment.setHelmet(playerHead);
		}

		if (chestplate != null) {
			entityEquipment.setChestplate(chestplate);
		}

		if (leggings != null) {
			entityEquipment.setLeggings(leggings);
		}
		if (boots != null) {
			entityEquipment.setBoots(boots);
		}
	}

	public void loadPlayer() {
		Inventory inv = player.getInventory();
		setArmor(player.getEquipment());
		SetItems(inv);
	}

	public void loadArmor(Player player) {
		setArmor(player.getEquipment());
	}

	public boolean checkIfDead(Player player, GameInstance gameInstance) {
		if (player.getGameMode() == GameMode.SPECTATOR) {
			return true;
		}
		else if (gameInstance.classes.get(player) != null && gameInstance.classes.get(player).getLives() <= 0) {
			return true;
		}

		return false;
	}

	/**
	 * This function displays an Action Bar for cooldowns of each class
	 * 
	 * @param cooldownSec  which is calculated in each class to display the seconds
	 *                     of cooldown
	 * @param type         which is the class the player is playing
	 * @param cooldownName which is the name of the cooldown
	 * @param itemName     which is the name of the item on cooldown
	 */
	public void cooldownActionBar(int cooldownSec, int duration, Timer cooldown, ClassType type, String cooldownName,
			String itemName) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == type) {
			if (instance.classes.get(player).getLives() > 0) {
				cooldownSec = (duration - cooldown.getTime()) / 1000 + 1;

				if (cooldown.getTime() < duration) {
					String msg = instance.getGameManager().getMain()
							.color("&e" + itemName + " &rcooldown: &e" + cooldownSec + "s");
					getActionBarManager().setActionBar(player, cooldownName, msg, 2);
				} else {
					String msg = instance.getGameManager().getMain().color("&rYou can use &e" + itemName);
					getActionBarManager().setActionBar(player, cooldownName, msg, 2);
				}
			}
		}
	}

	public ItemStack getHelmet(ItemStack helmet) {
		PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(player);
		ClassDetails details = data.playerClasses.get(this.getType().getID());
		if (details != null && details.reward5)
			return ClassRewardsGUI.headReward(this.getType());
		return helmet;
	}

	private String getPlayerRank(Player p) {
		return instance.getGameManager().getMain().getRankManager().getRank(p).getTagWithSpace();
	}

	// Event for EnderDragon class:
	private void enderDragonEvent(Player p, Player killer, PlayerData data2, BaseClass baseClass) {
		Location loc = p.getLocation();
		List<Item> deathParticles = new ArrayList<>();

		// Spawn the particles in a circle around the player
		for (int i = 0; i < 10; i++) {
			double angle = i * Math.PI / 5;
			double x = loc.getX() + Math.cos(angle) * 0.5;
			double y = loc.getY() + 1.5;
			double z = loc.getZ() + Math.sin(angle) * 0.5;

			Material mat = Material.INK_SACK; // Default
			if (data2 != null && p.hasPermission("scb.deathParticles")) {
				if (data2.goldApple == 1) {
					mat = Material.GOLDEN_APPLE;
				} else if (data2.glowstone == 1) {
					mat = Material.GLOWSTONE_DUST;
				} else if (data2.redstone == 1) {
					mat = Material.REDSTONE;
				} else if (data2.web == 1) {
					mat = Material.WEB;
				} else if (data2.bottleEXP == 1) {
					mat = Material.EXP_BOTTLE;
				}
			}

			ItemStack particleItem = null;

			if (mat == Material.INK_SACK)
				particleItem = new ItemStack(Material.INK_SACK, 1, (short) 15);
			else
				particleItem = new ItemStack(mat);
			Item particle = loc.getWorld().dropItem(new Location(loc.getWorld(), x, y, z), particleItem);
			particle.setPickupDelay(Integer.MAX_VALUE);
			deathParticles.add(particle);
		}

		// Schedule a task to remove the particles after 5 seconds
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
			for (Item particle : deathParticles) {
				particle.remove();
			}
		}, 5 * 20);

		if (baseClass != null) {
			if (baseClass.getType() == ClassType.Enderdragon) {
				if (killer != null) {
					Location pLoc = p.getLocation();
					EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc, EntityType.ENDER_CRYSTAL);
					HealTask task = new HealTask(killer, crystal, instance.getGameManager().getMain());
					BukkitTask bukkit = Bukkit.getScheduler()
							.runTaskTimerAsynchronously(instance.getGameManager().getMain(), task, 0, 2L);
					task.set(bukkit);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void checkBountyKill(BaseClass kClass, Player playerKilled, Player killer) {
		PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
		if (data != null) {
			if (kClass.bountyTarget != null) {
				if (kClass.bountyTarget == playerKilled) {
					kClass.bountyTarget = null;
					data.tokens += 25;
					killer.sendMessage("");
					player.sendMessage("");
					killer.sendMessage(instance.getGameManager().getMain()
							.color("&2&l(!) &e&lBOUNTY CLAIMED! &rYou earned &e25 Bonus Tokens!"));
					player.sendMessage(instance.getGameManager().getMain().color("&2&l(!) &e&lBOUNTY CLAIMED! &e"
							+ killer.getName() + " &rhas claimed their bounty on you!"));
					killer.sendTitle(instance.getGameManager().getMain().color("&e&lBOUNTY"),
							instance.getGameManager().getMain().color("&rYou claimed &e25 Bonus Tokens!"));
					player.sendTitle(instance.getGameManager().getMain().color("&e&lBOUNTY"), instance.getGameManager()
							.getMain().color("&e" + killer.getName() + " &rhas claimed their bounty on you!"));
					killer.sendMessage("");
					player.sendMessage("");
				}
			}
		}
	}

	public void Death(PlayerDeathEvent e) {
		if (player.getName() != null && lives > 0) {
			if (instance.gameType != GameType.GUNGAME) {
				lives--;
			}
			score.setScore(lives);

			Player killer = player.getKiller();
			Player p = player.getPlayer();

			// Remove mobs spawned by the player
			removeMobs(p);
			resetMobTarget(p);

			if (isDead) {
				PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(p);
				data.deaths += 1;
				for (PotionEffect type : p.getActivePotionEffects())
					p.removePotionEffect(type.getType());

				p.setFireTicks(0);
				p.getInventory().clear();
				BaseClass baseClassKiller = instance.classes.get(killer);
				BaseClass baseClassDead = instance.classes.get(p);
				baseClassDead.totalDeaths++;
				baseClassDead.eachLifeKills = 0;

				BaseClass bc = (instance.gameType == GameType.FRENZY || instance.gameType == GameType.GUNGAME)
						? instance.oldClasses.get(p)
						: baseClassDead;

				if (baseClassDead.lives < 1)
					bc = baseClassDead;

				// DEATH PARTICLES
				deathParticles(data, p);

				if (p.getLocation().getY() <= 50) { // VOID KILLS
					if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
						EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p
								.getLastDamageCause();
						Entity damager = entityDamageEvent.getDamager();

						if (damager instanceof Player) {
							Player d = (Player) damager;

							if (instance.classes.containsKey(d)) {
								baseClassKiller = instance.classes.get(d);
								PlayerData killerData = instance.getGameManager().getMain().getDataManager()
										.getPlayerData(d);

								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(d, p);
									TellAll(instance.getGameManager().getMain()
											.color("&2&l(!) &cHello? AND GOODBYE TO " + ChatColor.WHITE
													+ p.getPlayer().getName() + " " + bc.getType().getTag()
													+ " &cAND ANYONE ELSE STANDING IN " + ChatColor.WHITE + d.getName()
													+ " " + baseClassKiller.getType().getTag() + "'s &cWAY!"));
								} else {
									this.giveStats(d, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " was doomed to fall by " + ChatColor.WHITE + d.getName() + " "
											+ baseClassKiller.getType().getTag());
								}
								p.teleport(d.getLocation());
								this.healthPots(d);
							} else {
								Random r = new Random();
								int chance = r.nextInt(2);

								if (data != null && data.killMsgs == 1) {
									if (chance == 0) {
										TellAll(instance.getGameManager().getMain().color("&2&l(!) "

												+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &csaid NO THANK YOU and took the easy way out"));
									} else {
										TellAll(instance.getGameManager().getMain().color("&2&l(!) "

												+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &cwalked off the edge..."));
									}
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET

											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " fell into the void");
								}
								p.teleport(instance.GetSpecLoc());
							}
						} else if (damager instanceof Arrow) {
							Arrow a = (Arrow) damager;

							if (a.getShooter() instanceof Player && a.getShooter() != null) {
								Player d = (Player) a.getShooter();

								if (instance.classes.containsKey(d)) {
									baseClassKiller = instance.classes.get(d);
									PlayerData killerData = instance.getGameManager().getMain().getDataManager()
											.getPlayerData(d);
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(d, p);
										this.healthPots(d);
										TellAll(instance.getGameManager().getMain()
												.color("&2&l(!) &cHello? AND GOODBYE TO " + ChatColor.WHITE
														+ p.getPlayer().getName() + " " + bc.getType().getTag()
														+ " &cAND ANYONE ELSE STANDING IN " + ChatColor.WHITE
														+ d.getName() + " " + baseClassKiller.getType().getTag()
														+ "'s &cWAY!"));
									} else {
										this.giveStats(d, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
												+ " was doomed to fall by " + ChatColor.WHITE + d.getName() + " "
												+ baseClassKiller.getType().getTag());
									}
								} else {
									Random r = new Random();
									int chance = r.nextInt(2);

									if (data != null && data.killMsgs == 1) {
										if (chance == 0) {
											TellAll(instance.getGameManager().getMain().color("&2&l(!) "

													+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
													+ " &csaid NO THANK YOU and took the easy way out"));
										} else {
											TellAll(instance.getGameManager().getMain().color("&2&l(!) "

													+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
													+ " &cwalked off the edge..."));
										}
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET

												+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
												+ " fell into the void");
									}
									p.teleport(instance.GetSpecLoc());
								}
							} else {
								Random r = new Random();
								int chance = r.nextInt(2);

								if (data != null && data.killMsgs == 1) {
									if (chance == 0) {
										TellAll(instance.getGameManager().getMain().color("&2&l(!) "

												+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &csaid NO THANK YOU and took the easy way out"));
									} else {
										TellAll(instance.getGameManager().getMain().color("&2&l(!) "

												+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &cwalked off the edge..."));
									}
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET

											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " fell into the void");
								}
								p.teleport(instance.GetSpecLoc());
							}
						} else {
							Random r = new Random();
							int chance = r.nextInt(2);

							if (data != null && data.killMsgs == 1) {
								if (chance == 0) {
									TellAll(instance.getGameManager().getMain().color("&2&l(!) "

											+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
											+ " &csaid NO THANK YOU and took the easy way out"));
								} else {
									TellAll(instance.getGameManager().getMain().color("&2&l(!) "

											+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
											+ " &cwalked off the edge..."));
								}
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET

										+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
										+ " fell into the void");
							}
							p.teleport(instance.GetSpecLoc());
						}
					} else if (killer != null && instance.classes.containsKey(killer)) {
						PlayerData killerData = instance.getGameManager().getMain().getDataManager()
								.getPlayerData(killer);
						if (killer != p) {
							if (lives == 0) {
								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(killer, p);
									this.healthPots(killer);
									TellAll(instance.getGameManager().getMain()
											.color("&2&l(!) " + "&r" + p.getPlayer().getName() + " "
													+ bc.getType().getTag() + " &cwas not strong enough to encounter "
													+ "&r" + killer.getName() + " "
													+ baseClassKiller.getType().getTag()));
								} else {
									this.giveStats(killer, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " was killed by " + ChatColor.WHITE + killer.getName() + " "
											+ baseClassKiller.getType().getTag());
								}
							} else if (lives > 0) {
								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(killer, p);
									this.healthPots(killer);
									TellAll(instance.getGameManager().getMain()
											.color("&2&l(!) " + "&r" + p.getPlayer().getName() + " "
													+ bc.getType().getTag() + " &cwas not strong enough to encounter "
													+ "&r" + killer.getName() + " "
													+ baseClassKiller.getType().getTag()));
								} else {
									this.giveStats(killer, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " was killed by " + ChatColor.WHITE + killer.getName() + " "
											+ baseClassKiller.getType().getTag());
								}
							}
							p.teleport(killer);
						} else {
							if (lives > 0) {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
										+ " committed suicide");
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
										+ " committed suicide");
							}
							p.teleport(killer);
						}
					} else {
						Random r = new Random();
						int chance = r.nextInt(2);

						if (data != null && data.killMsgs == 1) {
							if (chance == 0) {
								TellAll(instance.getGameManager().getMain().color("&2&l(!) "

										+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
										+ " &csaid NO THANK YOU and took the easy way out"));
							} else {
								TellAll(instance.getGameManager().getMain().color("&2&l(!) "

										+ "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
										+ " &cwalked off the edge..."));
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET

									+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
									+ " fell into the void");
						}
						p.teleport(instance.GetSpecLoc());
					}
				} else if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
						&& p.getLastDamageCause().getCause() == DamageCause.MAGIC) {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
							+ " was murdered via the dark arts");
					p.teleport(instance.GetSpecLoc());
				} else if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
						&& p.getLastDamageCause().getCause() == DamageCause.WITHER) {
					if (killer == null) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " withered away");
						p.teleport(instance.GetSpecLoc());
					} else {
						this.giveStats(killer, p);
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " was withered by " + ChatColor.WHITE + killer.getName() + " "
								+ baseClassKiller.getType().getTag());
						p.teleport(instance.GetSpecLoc());
						this.healthPots(killer);
					}
				} else if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
						&& (p.getLastDamageCause().getCause() == DamageCause.FIRE_TICK
								|| p.getLastDamageCause().getCause() == DamageCause.FIRE
								|| p.getLastDamageCause().getCause() == DamageCause.LAVA)) {
					if (killer == null) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " burned to death");
						p.teleport(instance.GetSpecLoc());
					} else {
						this.giveStats(killer, p);
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " was burned to death by " + ChatColor.WHITE + killer.getName() + " "
								+ baseClassKiller.getType().getTag());
						p.teleport(killer);
						this.healthPots(killer);
					}
				} else if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p.getLastDamageCause();
					Entity damager = entityDamageEvent.getDamager();

					if (damager instanceof Player) {
						Player d = (Player) damager;

						if (instance.classes.containsKey(d)) {
							baseClassKiller = instance.classes.get(d);
							PlayerData killerData = instance.getGameManager().getMain().getDataManager()
									.getPlayerData(d);
							if (d != p || killer != p) {
								if (lives == 0) {
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(d, p);
										TellAll(instance.getGameManager().getMain()
												.color("&2&l(!) " + "&r" + p.getPlayer().getName() + " "
														+ bc.getType().getTag()
														+ " &cwas not strong enough to encounter " + "&r" + d.getName()
														+ " " + baseClassKiller.getType().getTag()));
										this.healthPots(d);
									} else {
										this.giveStats(d, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
												+ " was killed by " + ChatColor.WHITE + d.getName() + " "
												+ baseClassKiller.getType().getTag());
										this.healthPots(d);
									}
								} else if (lives > 0) {
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(d, p);
										TellAll(instance.getGameManager().getMain()
												.color("&2&l(!) " + "&r" + p.getPlayer().getName() + " "
														+ bc.getType().getTag()
														+ " &cwas not strong enough to encounter " + "&r" + d.getName()
														+ " " + baseClassKiller.getType().getTag()));
										this.healthPots(d);
									} else {
										this.giveStats(d, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
												+ " was killed by " + ChatColor.WHITE + d.getName() + " "
												+ baseClassKiller.getType().getTag());
										this.healthPots(d);
									}
								}
								p.teleport(d);
							} else {
								if (lives > 0) {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " committed suicide");
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " committed suicide");
								}
								p.teleport(d);
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED + " died");
						}
					} else if (damager instanceof Creature) {
						if (damager.getCustomName() != null) {
							String owner = ChatColor.stripColor(
									damager.getCustomName().substring(0, damager.getCustomName().indexOf("'")));
							Player d = Bukkit.getPlayer(owner);
							killer = d;

							if (instance.classes.containsKey(d)) {
								baseClassKiller = instance.classes.get(d);
								if (d != p) {
									this.giveStats(d, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " was killed by " + ChatColor.RESET + d.getName() + " "
											+ baseClassKiller.getType().getTag() + ChatColor.RED + "'s "
											+ ChatColor.YELLOW
											+ instance.getGameManager().getMobTypeName(damager.getType()));
									p.teleport(d);
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " was killed by a " + ChatColor.YELLOW
											+ instance.getGameManager().getMobTypeName(damager.getType()));
								}
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
										+ " was killed by a " + ChatColor.YELLOW
										+ instance.getGameManager().getMobTypeName(damager.getType()));
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
									+ " was killed by a " + ChatColor.YELLOW
									+ instance.getGameManager().getMobTypeName(damager.getType()));
						}
					} else if (damager instanceof Projectile) {
						Projectile a = (Projectile) damager;
						if (a.getShooter() instanceof Player && a.getShooter() != null) {
							Player shooter = (Player) a.getShooter();

							if (instance.classes.containsKey(shooter)) {
								baseClassKiller = instance.classes.get(shooter);
								PlayerData killerData = instance.getGameManager().getMain().getDataManager()
										.getPlayerData(shooter);
								if (shooter != p || killer != p) {
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(shooter, p);
										TellAll(instance.getGameManager().getMain().color("&2&l(!) " + "&r"
												+ p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &cwas not strong enough to encounter " + "&r" + shooter.getName()
												+ " " + baseClassKiller.getType().getTag()));
										this.healthPots(shooter);
									} else {
										this.giveStats(shooter, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
												+ " was killed by " + ChatColor.WHITE + shooter.getName() + " "
												+ baseClassKiller.getType().getTag());
										this.healthPots(shooter);
									}
									p.teleport(shooter);
								}
							} else {
								if (lives > 0) {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " committed suicide");
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " committed suicide");
								}
								p.teleport(shooter);
							}
						} else if (a.getShooter() instanceof Creature && a.getShooter() != null) {
							Creature shooter = (Creature) a.getShooter();
							if (shooter.getCustomName() != null) {
								String owner = ChatColor.stripColor(
										shooter.getCustomName().substring(0, shooter.getCustomName().indexOf("'")));
								Player d = Bukkit.getPlayer(owner);
								killer = d;

								if (instance.classes.containsKey(d)) {
									baseClassKiller = instance.classes.get(d);
									if (d != p) {
										this.giveStats(d, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
												+ " was killed by " + ChatColor.RESET + d.getName() + " "
												+ baseClassKiller.getType().getTag() + ChatColor.RED + "'s "
												+ ChatColor.YELLOW
												+ instance.getGameManager().getMobTypeName(damager.getType()));
										p.teleport(d);
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
												+ " was killed by a " + ChatColor.YELLOW
												+ instance.getGameManager().getMobTypeName(damager.getType()));
									}
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
											+ " was killed by a " + ChatColor.YELLOW
											+ instance.getGameManager().getMobTypeName(damager.getType()));
								}
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED + " died");
						}
					} else {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					}
				} else if (killer != null) {
					PlayerData killerData = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
					if (killer != p) {
						if (lives == 0) {
							if (killerData != null && killerData.killMsgs == 1) {
								this.giveStats(killer, p);
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) " + getPlayerRank(p) + "&r" + p.getPlayer().getName() + " "
												+ bc.getType().getTag() + " &cwas not strong enough to encounter "
												+ "&r" + killer.getName() + " " + baseClassKiller.getType().getTag()));
								this.healthPots(killer);
							} else {
								this.giveStats(killer, p);
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
										+ " was killed by " + ChatColor.WHITE + killer.getName() + " "
										+ baseClassKiller.getType().getTag());
								this.healthPots(killer);
							}
						} else if (lives > 0) {
							if (killerData != null && killerData.killMsgs == 1) {
								this.giveStats(killer, p);
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) " + "&r" + p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &cwas not strong enough to encounter " + "&r" + killer.getName()
												+ " " + baseClassKiller.getType().getTag()));
								this.healthPots(killer);
							} else {
								this.giveStats(killer, p);
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
										+ " was killed by " + ChatColor.WHITE + killer.getName() + " "
										+ baseClassKiller.getType().getTag());
								this.healthPots(killer);
							}
						}
						p.teleport(killer);
					} else {
						if (lives > 0) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
									+ " committed suicide");
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
									+ " committed suicide");
						}
						p.teleport(killer);
					}
				} else if (DamageCause.VOID != null) {
					if (lives == 0) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					} else if (lives > 0) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					}
					p.getPlayer().setFireTicks(0);
				} else if (DamageCause.SUICIDE != null) {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
							+ " committed suicide");
					p.getPlayer().setFireTicks(0);
				} else if (DamageCause.LAVA != null || DamageCause.FIRE != null || DamageCause.FIRE_TICK != null) {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + ChatColor.RED + " just burned to death");
				} else {
					if (lives == 0) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					} else if (lives > 0) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + " " + bc.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					}
					p.getPlayer().setFireTicks(0);
				}

				if (lives == 0) {
					if (data != null) {
						data.losses += 1;
						ClassType type = baseClassDead.getType();
						ClassDetails details = data.playerClasses.get(type.getID());
						if (details == null) {
							details = new ClassDetails();
							data.playerClasses.put(type.getID(), details);
						}
						details.playGame();
						data.winstreak = 0;
					}
					if (killer != null) {
						String msg = instance.getGameManager().getMain().color("&4&lELIMINATED &e" + p.getName());
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) killer;
						craft.getHandle().playerConnection.sendPacket(packet);
					}
					p.setDisplayName("" + p.getName() + " " + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC
							+ "Spectator" + ChatColor.RESET);
					instance.sendScoreboardUpdate(player);

					Random r = new Random();
					int chance = r.nextInt(100);

					if (chance >= 0 && chance <= 15) {

						if (data != null) {
							data.mysteryChests++;
							player.sendMessage(instance.getGameManager().getMain()
									.color("&5&l(!) &rYou have found &e1 MysteryChest!"));
						}
					}

					if (data.withersk != 3)
						data.withersk = 0;

					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + " " + baseClassDead.getType().getTag() + ChatColor.RED
							+ " has been eliminated!");

					if (instance.getMap() != null) {
						PlayerData data3 = instance.getGameManager().getMain().getDataManager().getPlayerData(p);
						p.sendMessage("" + ChatColor.BOLD + "=====================");
						p.sendMessage("" + ChatColor.BOLD + "||");
						p.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RED + ChatColor.BOLD
								+ "  GAME LOST");
						p.sendMessage("" + ChatColor.BOLD + "||");

						int tokensEarned = 0;
						if (instance.alivePlayers == 5) {
							tokensEarned = 1;
						} else if (instance.alivePlayers == 4) {
							tokensEarned = 3;
						} else if (instance.alivePlayers == 3) {
							tokensEarned = 5;
						} else if (instance.alivePlayers == 2) {
							tokensEarned = 7;
						}
						data3.tokens += tokensEarned;
						baseClassDead.totalTokens += tokensEarned;
						p.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Placed #"
								+ instance.alivePlayers + ": " + ChatColor.GREEN + tokensEarned + " Tokens");
						baseClassDead.placement = instance.alivePlayers;

						if (baseClassDead != null && baseClassDead.totalKills >= 0) {
							player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  "
									+ baseClassDead.totalKills + " Kills: " + ChatColor.GREEN
									+ (baseClassDead.totalKills * 2) + " Tokens");
							data3.tokens += baseClassDead.totalKills * 2;
							baseClassDead.totalTokens += baseClassDead.totalKills;
						}
						if (baseClassDead != null && instance.firstBlood == player) {
							player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET
									+ "  First Blood: " + ChatColor.GREEN + "10 Tokens");
							data3.tokens += 10;
						}
						if (p.hasPermission("scb.rankBonus")) {
							p.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Rank Bonus: "
									+ ChatColor.GREEN + "10 Tokens");
							data3.tokens += 10;
							baseClassDead.totalTokens += 10;
						}
						p.sendMessage("" + ChatColor.BOLD + "||");
						p.sendMessage("" + ChatColor.BOLD + "=====================");
						p.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have gained " + ChatColor.GREEN + baseClassDead.totalExp + " EXP!");
						p.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have earned " + ChatColor.GREEN + baseClassDead.totalTokens + " Tokens!");

						if (data3.exp >= 2500) {
							data3.level++;
							data3.exp -= 2500;
							p.sendMessage(instance.getGameManager().getMain().color("&e&lLEVEL UPGRADED!"));
							p.sendMessage("You are now Level: " + data3.level + "!");
						}
					} else {
						List<String> aliveTeam = new ArrayList<String>();
						for (Entry<Player, BaseClass> entry : instance.classes.entrySet()) {
							if (entry.getValue().getLives() > 0) {
								if (!(aliveTeam.contains(instance.team.get(entry.getKey())))) {
									aliveTeam.add(instance.team.get(entry.getKey()));
									instance.teamsAlive++;
								}
							}
						}
						instance.teamsAlive++;
						if (instance.team.get(p).equals("Red")) {
							if (!(aliveTeam.contains("Red"))) {
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) &c&lRed Team &r has been eliminated!"));

								for (Player losers : instance.redTeam) {
									BaseClass loserBc = instance.classes.get(losers);
									PlayerData data3 = instance.getGameManager().getMain().getDataManager()
											.getPlayerData(losers);
									losers.sendMessage(ChatColor.BOLD + "=====================");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RED
											+ ChatColor.BOLD + "  GAME LOST");
									losers.sendMessage(ChatColor.BOLD + "||");

									int tokensEarned = 0;
									if (instance.aliveTeams == 3)
										tokensEarned = 5;
									else if (instance.aliveTeams == 2)
										tokensEarned = 7;

									losers.sendMessage("        " + "    " + instance.aliveTeams + " Place: "
											+ tokensEarned + " Tokens");
									data3.tokens += tokensEarned;
									loserBc.totalTokens += tokensEarned;

									if (loserBc.totalKills >= 0) {
										player.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
												+ ChatColor.BOLD + "  " + loserBc.totalKills + " Kills: "
												+ ChatColor.RESET + ChatColor.YELLOW + loserBc.totalKills + " Tokens");
										data3.tokens += loserBc.totalKills;
										loserBc.totalTokens += loserBc.totalKills;
									}
									if (losers.hasPermission("scb.rankBonus")) {
										losers.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
												+ ChatColor.BOLD + "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW
												+ "10 Tokens");
										data3.tokens += 10;
										loserBc.totalTokens += 10;
									}
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "=====================");
									losers.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + ChatColor.BOLD + "(!) "
											+ ChatColor.RESET + "You have gained " + ChatColor.YELLOW + loserBc.totalExp
											+ " EXP!");

									if (data3.exp >= 2500) {
										data3.level++;
										data3.exp -= 2500;
										losers.sendMessage(
												instance.getGameManager().getMain().color("&e&lLEVEL UPGRADED!"));
										losers.sendMessage("You are now Level: " + data3.level + "!");
									}

									losers.sendMessage(instance.getGameManager().getMain()
											.color("&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens"));
								}
							}
						} else if (instance.team.get(p).equals("Blue")) {
							if (!(aliveTeam.contains("Blue"))) {
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) &b&lBlue Team &r has been eliminated!"));

								for (Player losers : instance.blueTeam) {
									BaseClass loserBc = instance.classes.get(losers);
									PlayerData data3 = instance.getGameManager().getMain().getDataManager()
											.getPlayerData(losers);
									losers.sendMessage(ChatColor.BOLD + "=====================");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RED
											+ ChatColor.BOLD + "  GAME LOST");
									losers.sendMessage(ChatColor.BOLD + "||");

									int tokensEarned = 0;
									if (instance.aliveTeams == 3)
										tokensEarned = 5;
									else if (instance.aliveTeams == 2)
										tokensEarned = 7;

									losers.sendMessage("        " + "    " + instance.aliveTeams + " Place: "
											+ tokensEarned + " Tokens");
									data3.tokens += tokensEarned;
									loserBc.totalTokens += tokensEarned;

									if (loserBc.totalKills >= 0) {
										player.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
												+ ChatColor.BOLD + "  " + loserBc.totalKills + " Kills: "
												+ ChatColor.RESET + ChatColor.YELLOW + loserBc.totalKills + " Tokens");
										data3.tokens += loserBc.totalKills;
										loserBc.totalTokens += loserBc.totalKills;
									}
									if (losers.hasPermission("scb.rankBonus")) {
										losers.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
												+ ChatColor.BOLD + "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW
												+ "10 Tokens");
										data3.tokens += 10;
										loserBc.totalTokens += 10;
									}
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "=====================");
									losers.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + ChatColor.BOLD + "(!) "
											+ ChatColor.RESET + "You have gained " + ChatColor.YELLOW + loserBc.totalExp
											+ " EXP!");

									if (data3.exp >= 2500) {
										data3.level++;
										data3.exp -= 2500;
										losers.sendMessage(
												instance.getGameManager().getMain().color("&e&lLEVEL UPGRADED!"));
										losers.sendMessage("You are now Level: " + data3.level + "!");
									}
									losers.sendMessage(instance.getGameManager().getMain()
											.color("&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens"));
								}
							}
						} else if (instance.team.get(p).equals("Black")) {
							if (!(aliveTeam.contains("Black"))) {
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) &0&lBlack Team &r has been eliminated!"));

								for (Player losers : instance.blackTeam) {
									BaseClass loserBc = instance.classes.get(losers);
									PlayerData data3 = instance.getGameManager().getMain().getDataManager()
											.getPlayerData(losers);
									losers.sendMessage(ChatColor.BOLD + "=====================");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RED
											+ ChatColor.BOLD + "  GAME LOST");
									losers.sendMessage(ChatColor.BOLD + "||");

									int tokensEarned = 0;
									if (instance.aliveTeams == 3)
										tokensEarned = 5;
									else if (instance.aliveTeams == 2)
										tokensEarned = 7;

									losers.sendMessage("        " + "    " + instance.aliveTeams + " Place: "
											+ tokensEarned + " Tokens");
									data3.tokens += tokensEarned;
									loserBc.totalTokens += tokensEarned;

									if (loserBc.totalKills >= 0) {
										player.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
												+ ChatColor.BOLD + "  " + loserBc.totalKills + " Kills: "
												+ ChatColor.RESET + ChatColor.YELLOW + loserBc.totalKills + " Tokens");
										data3.tokens += loserBc.totalKills;
										loserBc.totalTokens += loserBc.totalKills;
									}
									if (losers.hasPermission("scb.rankBonus")) {
										losers.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
												+ ChatColor.BOLD + "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW
												+ "10 Tokens");
										data3.tokens += 10;
										loserBc.totalTokens += 10;
									}
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "||");
									losers.sendMessage(ChatColor.BOLD + "=====================");
									losers.sendMessage(String.valueOf(ChatColor.LIGHT_PURPLE) + ChatColor.BOLD + "(!) "
											+ ChatColor.RESET + "You have gained " + ChatColor.YELLOW + loserBc.totalExp
											+ " EXP!");

									if (data3.exp >= 2500) {
										data3.level++;
										data3.exp -= 2500;
										losers.sendMessage(
												instance.getGameManager().getMain().color("&e&lLEVEL UPGRADED!"));
										losers.sendMessage("You are now Level: " + data3.level + "!");
									}
									losers.sendMessage(instance.getGameManager().getMain()
											.color("&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens"));
								}
							}
						}
						instance.aliveTeams--;
					}

				} else if (lives == 1) {
					if (killer != null) {
						String msg = instance.getGameManager().getMain().color("&4&lKILLED &e" + p.getName());
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) killer;
						craft.getHandle().playerConnection.sendPacket(packet);
					}
					TellAll(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + " " + baseClassDead.getType().getTag() + ChatColor.RED + " has "
							+ lives + " life left");

				} else {
					if (killer != null) {
						String msg = instance.getGameManager().getMain().color("&4&lKILLED &e" + p.getName());
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) killer;
						craft.getHandle().playerConnection.sendPacket(packet);
					}
					TellAll(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + " " + baseClassDead.getType().getTag() + ChatColor.RED + " has "
							+ lives + " lives left");
				}

				// END CRYSTAL
				if (baseClassKiller != null) {
					if (baseClassKiller.getType() == ClassType.Enderdragon) {
						if (killer != null) {
							Location pLoc = p.getLocation();
							EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc,
									EntityType.ENDER_CRYSTAL);
							HealTask task = new HealTask(killer, crystal, instance.getGameManager().getMain());
							BukkitTask bukkit = Bukkit.getScheduler()
									.runTaskTimerAsynchronously(instance.getGameManager().getMain(), task, 0, 20L);
							task.set(bukkit);
						}
					}
				}

				if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p.getLastDamageCause();
					Entity damager = entityDamageEvent.getDamager();

					if (damager instanceof Player) {
						Player d = (Player) damager;

						if (instance.classes.containsKey(d)) {
							baseClassKiller = instance.classes.get(d);
							baseClassKiller.classesEvent(d, baseClassKiller);
						}
					} else {
						if (killer != null) {
							if (instance.classes.containsKey(killer)) {
								baseClassKiller = instance.classes.get(killer);
								baseClassKiller.classesEvent(killer, baseClassKiller);
							}
						}
					}
				} else if (killer != null) {
					if (instance.classes.containsKey(killer)) {
						baseClassKiller = instance.classes.get(killer);
						baseClassKiller.classesEvent(killer, baseClassKiller);
					}
				}
				// EntityDamageEvent event = new EntityDamageEvent(p, DamageCause.VOID, 0);
				p.setLastDamageCause(null);
				// Bukkit.getServer().getPluginManager().callEvent(event);
			}
		}

	}

	private boolean foundDeath = false;

	private void checkRegularKill(Player p, Player killer, BaseClass pClass) {
		if (foundDeath)
			return;

		BaseClass kClass = null;
		PlayerData kData = null;

		if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p.getLastDamageCause();
			Entity damager = entityDamageEvent.getDamager();

			if (damager instanceof Player) {
				Player d = (Player) damager;

				if (instance.classes.containsKey(d)) {
					kClass = instance.classes.get(d);
					kData = instance.getGameManager().getMain().getDataManager().getPlayerData(d);

					if (d != p || killer != p) {
						if (kData != null && kData.killMsgs == 1) {
							giveStats(d, p);
							TellAll(instance.getGameManager().getMain()
									.color("&2&l(!) " + getPlayerRank(p) + "&r" + p.getPlayer().getName() + " "
											+ pClass.getType().getTag() + " &cwas not strong enough to encounter "
											+ getPlayerRank(d) + "&r" + d.getName() + " " + kClass.getType().getTag()));
							healthPots(d);
						} else {
							giveStats(d, p);
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " was killed by " + ChatColor.WHITE + getPlayerRank(d)
									+ d.getName() + " " + kClass.getType().getTag());
							healthPots(d);
						}
						p.teleport(d);
					} else {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
								+ " committed suicide");
						p.teleport(instance.GetSpecLoc());
					}
				} else {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
							+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED + " died");
					p.teleport(instance.GetSpecLoc());
				}
			} else if (damager instanceof Zombie) {
				TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
						+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
						+ " was killed by a " + ChatColor.YELLOW + "zombie");
			} else if (damager instanceof Skeleton) {
				TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
						+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
						+ " was shot to death by a " + ChatColor.YELLOW + "skeleton");
			} else if (damager instanceof Arrow) {
				Arrow a = (Arrow) damager;

				if (a.getShooter() instanceof Player && a.getShooter() != null) {
					Player shooter = (Player) a.getShooter();

					if (instance.classes.containsKey(shooter)) {
						kClass = instance.classes.get(shooter);
						kData = instance.getGameManager().getMain().getDataManager().getPlayerData(shooter);

						if (shooter != p || killer != p) {
							if (kData != null && kData.killMsgs == 1) {
								giveStats(shooter, p);
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) " + getPlayerRank(p) + "&r" + p.getPlayer().getName() + " "
												+ pClass.getType().getTag() + " &cwas not strong enough to encounter "
												+ getPlayerRank(shooter) + "&r" + shooter.getName() + " "
												+ kClass.getType().getTag()));
								healthPots(shooter);
							} else {
								giveStats(shooter, p);
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
										+ ChatColor.RED + " was killed by " + ChatColor.WHITE + getPlayerRank(shooter)
										+ shooter.getName() + " " + kClass.getType().getTag());
								healthPots(shooter);
							}
							p.teleport(shooter);
						}
					} else {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
								+ " committed suicide");
						p.teleport(instance.GetSpecLoc());
					}
				} else {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
							+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED + " died");
					p.teleport(instance.GetSpecLoc());
				}
			} else {
				TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
						+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
						+ " just died SO badly");
				p.teleport(instance.GetSpecLoc());
			}
		} else if (killer != null && instance.classes.get(killer) != null) {
			kData = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
			kClass = instance.classes.get(killer);

			if (killer != p) {
				if (kData != null && kData.killMsgs == 1) {
					this.giveStats(killer, p);
					TellAll(instance.getGameManager().getMain()
							.color("&2&l(!) " + getPlayerRank(p) + "&r" + p.getPlayer().getName() + " "
									+ pClass.getType().getTag() + " &cwas not strong enough to encounter "
									+ getPlayerRank(killer) + "&r" + killer.getName() + " "
									+ kClass.getType().getTag()));
					this.healthPots(killer);
				} else {
					this.giveStats(killer, p);
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
							+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
							+ " was killed by " + ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
							+ kClass.getType().getTag());
					this.healthPots(killer);
				}
				p.teleport(killer);
			} else {
				TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
						+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
						+ " committed suicide");
				p.teleport(instance.GetSpecLoc());
			}

			this.foundDeath = true;
		}
	}

	private void checkWitherKill(Player p, Player killer, BaseClass pClass, BaseClass kClass) {
		if (foundDeath)
			return;

		if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
				&& p.getLastDamageCause().getCause() == DamageCause.WITHER) {
			if (killer == null) {
				TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
						+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED + " withered away");
				p.teleport(instance.GetSpecLoc());
			} else {
				this.giveStats(killer, p);
				TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
						+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
						+ " was withered by " + ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
						+ kClass.getType().getTag());
				p.teleport(instance.GetSpecLoc());
				healthPots(killer);
			}

			this.foundDeath = true;
		}
	}

	private void checkMagicKill(Player p, BaseClass pClass) {
		if (foundDeath)
			return;

		if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
				&& p.getLastDamageCause().getCause() == DamageCause.MAGIC) {
			TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
					+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
					+ " was murdered via the dark arts");
			p.teleport(instance.GetSpecLoc());
			this.foundDeath = true;
		}
	}

	private void checkVoidKill(Player p, Player killer, BaseClass pClass, PlayerData pData) {
		if (foundDeath)
			return;

		BaseClass kClass = null;
		PlayerData kData = null;

		if (p.getLocation().getY() <= 50) {
			if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p.getLastDamageCause();
				Entity damager = entityDamageEvent.getDamager();

				if (damager instanceof Player && damager != null) {
					Player d = (Player) damager;
					if (instance.classes.containsKey(d)) { // Verify they're still in the same game
						kClass = instance.classes.get(d);
						kData = instance.getGameManager().getMain().getDataManager().getPlayerData(d);

						if (kData != null && kData.killMsgs == 1) { // If they have custom kill msgs enabled
							this.giveStats(d, p);
							TellAll(instance.getGameManager().getMain()
									.color("&2&l(!) &cHello? AND GOODBYE TO " + getPlayerRank(p) + ChatColor.WHITE
											+ p.getPlayer().getName() + " " + pClass.getType().getTag()
											+ " &cAND ANYONE ELSE STANDING IN " + getPlayerRank(d) + ChatColor.WHITE
											+ d.getName() + " " + kClass.getType().getTag() + "'s &cWAY!"));
						} else {
							this.giveStats(d, p);
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " was doomed to fall by " + ChatColor.WHITE + getPlayerRank(d)
									+ d.getName() + " " + kClass.getType().getTag());
						}
						p.teleport(d.getLocation());
					} else {
						Random r = new Random();
						int chance = r.nextInt(2);

						if (pData != null && pData.killMsgs == 1) {
							if (chance == 0) {
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) "
												+ instance.getGameManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ " &csaid NO THANK YOU and took the easy way out"));
							} else {
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) "
												+ instance.getGameManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ " &cwalked off the edge..."));
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ instance.getGameManager().getMain().getRankManager().getRank(player)
											.getTagWithSpace()
									+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
									+ " fell into the void");
						}
						p.teleport(instance.GetSpecLoc());
					}

				} else if (damager instanceof Arrow) {
					Arrow a = (Arrow) damager;

					if (a.getShooter() instanceof Player && a.getShooter() != null) {
						Player d = (Player) a.getShooter();

						if (instance.classes.containsKey(d)) {
							kClass = instance.classes.get(d);
							kData = instance.getGameManager().getMain().getDataManager().getPlayerData(d);

							if (kData != null && kData.killMsgs == 1) {
								this.giveStats(d, p);
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) &cHello? AND GOODBYE TO " + getPlayerRank(p) + ChatColor.WHITE
												+ p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ " &cAND ANYONE ELSE STANDING IN " + getPlayerRank(d) + ChatColor.WHITE
												+ d.getName() + " " + kClass.getType().getTag() + "'s &cWAY!"));
							} else {
								this.giveStats(d, p);
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
										+ ChatColor.RED + " was doomed to fall by " + ChatColor.WHITE + getPlayerRank(d)
										+ d.getName() + " " + kClass.getType().getTag());
							}
						} else {
							Random r = new Random();
							int chance = r.nextInt(2);

							if (pData != null && pData.killMsgs == 1) {
								if (chance == 0) {
									TellAll(instance.getGameManager().getMain()
											.color("&2&l(!) "
													+ instance.getGameManager().getMain().getRankManager()
															.getRank(player).getTagWithSpace()
													+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
													+ " &csaid NO THANK YOU and took the easy way out"));
								} else {
									TellAll(instance.getGameManager().getMain().color("&2&l(!) "
											+ instance.getGameManager().getMain().getRankManager().getRank(player)
													.getTagWithSpace()
											+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
											+ " &cwalked off the edge..."));
								}
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ instance.getGameManager().getMain().getRankManager().getRank(player)
												.getTagWithSpace()
										+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
										+ " fell into the void");
							}
							p.teleport(instance.GetSpecLoc());
						}
					} else {
						Random r = new Random();
						int chance = r.nextInt(2);

						if (pData != null && pData.killMsgs == 1) {
							if (chance == 0) {
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) "
												+ instance.getGameManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ " &csaid NO THANK YOU and took the easy way out"));
							} else {
								TellAll(instance.getGameManager().getMain()
										.color("&2&l(!) "
												+ instance.getGameManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ " &cwalked off the edge..."));
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ instance.getGameManager().getMain().getRankManager().getRank(player)
											.getTagWithSpace()
									+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
									+ " fell into the void");
						}
						p.teleport(instance.GetSpecLoc());
					}
				} else {
					Random r = new Random();
					int chance = r.nextInt(2);

					if (pData != null && pData.killMsgs == 1) {
						if (chance == 0) {
							TellAll(instance.getGameManager().getMain().color("&2&l(!) "
									+ instance.getGameManager().getMain().getRankManager().getRank(player)
											.getTagWithSpace()
									+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ " &csaid NO THANK YOU and took the easy way out"));
						} else {
							TellAll(instance.getGameManager().getMain()
									.color("&2&l(!) "
											+ instance.getGameManager().getMain().getRankManager().getRank(player)
													.getTagWithSpace()
											+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
											+ " &cwalked off the edge..."));
						}
					} else {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ instance.getGameManager().getMain().getRankManager().getRank(player).getTagWithSpace()
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
								+ " fell into the void");
					}
					p.teleport(instance.GetSpecLoc());
				}
			} else if (killer != null && instance.classes.containsKey(killer)) {
				kData = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
				kClass = instance.classes.get(killer);

				if (killer != p) {
					if (kData != null && kData.killMsgs == 1) {
						this.giveStats(killer, p);
						TellAll(instance.getGameManager().getMain()
								.color("&2&l(!) " + getPlayerRank(p) + "&r" + p.getPlayer().getName() + " "
										+ pClass.getType().getTag() + " &cwas not strong enough to encounter "
										+ getPlayerRank(killer) + "&r" + killer.getName() + " "
										+ kClass.getType().getTag()));
					} else {
						this.giveStats(killer, p);
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
								+ " was killed by " + ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
								+ kClass.getType().getTag());
					}
					p.teleport(killer);
				} else {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
							+ p.getPlayer().getName() + " "
							+ /* baseClass2.getType().getTag() */pClass.getType().getTag() + ChatColor.RED
							+ " committed suicide");
					p.teleport(instance.GetSpecLoc());
				}
			} else {
				Random r = new Random();
				int chance = r.nextInt(2);

				if (pData != null && pData.killMsgs == 1) {
					if (chance == 0) {
						TellAll(instance.getGameManager().getMain()
								.color("&2&l(!) "
										+ instance.getGameManager().getMain().getRankManager().getRank(player)
												.getTagWithSpace()
										+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
										+ " &csaid NO THANK YOU and took the easy way out"));
					} else {
						TellAll(instance.getGameManager().getMain()
								.color("&2&l(!) "
										+ instance.getGameManager().getMain().getRankManager().getRank(player)
												.getTagWithSpace()
										+ "&r" + p.getPlayer().getName() + " " + pClass.getType().getTag()
										+ " &cwalked off the edge..."));
					}
				} else {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ instance.getGameManager().getMain().getRankManager().getRank(player).getTagWithSpace()
							+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
							+ " fell into the void");
				}
				p.teleport(instance.GetSpecLoc());
			}

			this.foundDeath = true;
		}
	}

	// This function removes any mobs that the player who died has spawned
	private void removeMobs(Player p) {
		for (Entity en : p.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(p.getName()))
					en.remove();
	}

	private void resetMobTarget(Player p) {
		for (Creature en : p.getWorld().getEntitiesByClass(Creature.class)) {
			if (en.getTarget() != null && en.getTarget() == p) {
				if (getMobOwner(en) != null)
					en.setTarget(instance.getNearestPlayer(getMobOwner(en), en, 150));
			}
		}
	}

	private Player getMobOwner(Creature creature) {
		if (creature.getCustomName() != null) {
			String customName = ChatColor.stripColor(creature.getCustomName());

			if (!customName.contains("'")) {
				return null;
			}

			String owner = customName.substring(0, customName.indexOf("'"));
			return Bukkit.getPlayer(owner);
		}
		return null;
	}

	private void deathParticles(PlayerData pData, Player p) {
		Location pLocation = p.getLocation();
		List<Item> deathParticles = new ArrayList<>();
		Material mat = Material.INK_SACK;

		// Particles
		if (pData != null && p.hasPermission("scb.deathParticles")) {
			if (pData.goldApple == 1)
				mat = Material.GOLDEN_APPLE;
			else if (pData.glowstone == 1)
				mat = Material.GLOWSTONE_DUST;
			else if (pData.redstone == 1)
				mat = Material.REDSTONE;
			else if (pData.web == 1)
				mat = Material.WEB;
			else if (pData.bottleEXP == 1)
				mat = Material.EXP_BOTTLE;
		}

		ItemStack particleItem;

		if (mat == Material.INK_SACK)
			particleItem = new ItemStack(Material.INK_SACK, 1, (short) 15);
		else
			particleItem = new ItemStack(mat);

		// Spawn the particles in a circle around the player
		for (int i = 0; i < 10; i++) {
			double angle = i * Math.PI / 5;
			double x = pLocation.getX() + Math.cos(angle) * 0.5;
			double y = pLocation.getY() + 1.5;
			double z = pLocation.getZ() + Math.sin(angle) * 0.5;

			Item particle = pLocation.getWorld().dropItem(new Location(pLocation.getWorld(), x, y, z), particleItem);
			particle.setPickupDelay(Integer.MAX_VALUE);
			deathParticles.add(particle);
		}

		// Schedule a task to remove the particles after 5 seconds
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), () -> {
			for (Item particle : deathParticles) {
				particle.remove();
			}
		}, 5 * 20);
	}

	private void enderDragonCrystal(BaseClass pClass, Player killer, Player p) {
		if (pClass != null) {
			if (pClass.getType() == ClassType.Enderdragon) {
				if (killer != null) {
					Location pLoc = p.getLocation();
					EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc, EntityType.ENDER_CRYSTAL);
					HealTask task = new HealTask(killer, crystal, instance.getGameManager().getMain());
					BukkitTask bukkit = Bukkit.getScheduler()
							.runTaskTimerAsynchronously(instance.getGameManager().getMain(), task, 0, 20L);
					task.set(bukkit);
				}
			}
		}
	}

	// Giving health potions on kill
	protected void healthPots(Player d) {
		if (checkIfDead(d, instance) || instance.classes.get(d).getType() == ClassType.Horse)
			return;

		if (d.getHealth() / d.getMaxHealth() >= 0.5) return;

		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				String.valueOf(ChatColor.RED) + ChatColor.BOLD + "HEALING I");
		Potion pot = new Potion(1);
		pot.setType(PotionType.INSTANT_HEAL);
		pot.setSplash(true);
		pot.apply(item);
		d.getInventory().addItem(item);

		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new Runnable() {
			@Override
			public void run() {
				d.sendMessage(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ ChatColor.YELLOW + "You got a kill and got rewarded a " + ChatColor.YELLOW + ChatColor.BOLD
						+ "Health Pot");
			}
		}, 1L);
	}

	// Gives the killer kills for stats, points for tourney, etc
	private void giveStats(Player d, Player p) {
		PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(d);
		if (d != null) {
			if (instance.classes.containsKey(d)) {
				PlayerData data2 = instance.getGameManager().getMain().getDataManager().getPlayerData(d);
				BaseClass baseClass3 = instance.classes.get(d);
				// For first blood:
				if (instance.firstBlood == null) {
					instance.firstBlood = d;
					TellAll("");
					TellAll(instance.getGameManager().getMain().color("&2&l(!) &r" + getPlayerRank(d) + d.getName()
							+ " " + baseClass3.getType().getTag() + " &edrew first blood!"));
					TellAll("");
					baseClass3.totalTokens += 10;
					if (instance.getGameManager().getMain().tournament) {
						if (instance.gameType != GameType.DUEL) {
							data2.points += 2;
							instance.getGameManager().getMain().tourney.put(d.getName(), data2.points);
						}
					}
				}

				if (data2 != null) {
					data2.kills += 1;
					data2.exp += 29;
					baseClass3.totalExp += 29;
					if (instance.getGameManager().getMain().tournament) {
						if (instance.gameType != GameType.DUEL) {
							data2.points++;
							instance.getGameManager().getMain().tourney.put(d.getName(), data2.points);
						}
					}

					d.playSound(d.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);

					if (baseClass3 != null) {
						baseClass3.totalTokens += 1;
						baseClass3.totalKills++;
						baseClass3.eachLifeKills++;
						this.checkBountyKill(baseClass3, p, d);
					}
				}
			}
		}
	}

	// Classes such as Sheep & Hunter that when they get a kill, they one of their
	// abilities back
	public void classesEvent(Player damagerPlayer, BaseClass baseClass) {
		if (instance.classes.containsKey(damagerPlayer) && !checkIfDead(player, instance)) {
			// Sheep
			baseClass = instance.classes.get(damagerPlayer);
			if (baseClass.getType() == ClassType.Sheep) {
				damagerPlayer.getInventory().addItem(new ItemStack(Material.ENCHANTMENT_TABLE));
				damagerPlayer.sendMessage(instance.getGameManager().getMain()
						.color("&r&l(!) &rYou got a kill and now you can switch your wool color if you'damagerPlayer like!"));

			} else if (baseClass.getType() == ClassType.Hunter) {
				if (!hunterDash) {
					damagerPlayer.sendMessage(instance.getGameManager().getMain()
							.color("&r&l(!) &rYour &r&lDash &rhas been regenerated for getting a kill!"));
					hunterDash = true;
					ItemStack dash = ItemHelper.addEnchant(
							ItemHelper.setDetails(new ItemStack(Material.FEATHER),
									instance.getGameManager().getMain().color("&b&lDash"),
									instance.getGameManager().getMain().color("&7A quick escape or attack")),
							Enchantment.PROTECTION_ENVIRONMENTAL, 1);
					player.getInventory().setItem(1, dash);
				}
			} else if (baseClass.getType() == ClassType.Present) {
				damagerPlayer.sendMessage(instance.getGameManager().getMain().color(
						"&r&l(!) &rYour &r&lAggressive Gift has regenerated and you can get a new weapon if you'damagerPlayer like!"));
				damagerPlayer.getInventory().addItem(ItemHelper.setDetails(new ItemStack(Material.CHEST, 1),
						String.valueOf(ChatColor.RESET) + ChatColor.ITALIC + "Agressive Gift", "",
						String.valueOf(ChatColor.RESET) + ChatColor.YELLOW + "Steals another player's main item"));
			} else if (baseClass.getType() == ClassType.PiglinBrute) {
				ItemStack item = ItemHelper.setDetails(new ItemStack(Material.GOLD_BLOCK, 1),
						"&eGold Balls",
						"&7Right click to throw DEADLY gold balls!");
				damagerPlayer.sendMessage(instance.getGameManager().getMain()
						.color("&2&l(!) &rYou got a kill and gained an extra &eGold Ball"));
				damagerPlayer.getInventory().addItem(item);
			} else if (baseClass.getType() == ClassType.GrimReaper) {
				ItemStack zombieEgg = ItemHelper.createMonsterEgg(EntityType.ZOMBIE, 1, "&2&lZOMBIE POKEBALL");
				damagerPlayer.getInventory().setItem(2, zombieEgg);
			} else if (baseClass.getType() == ClassType.Wolf) {
				wolfPackAdd = true;
			}
		}
	}

	public void TellAll(String msg) {
		for (Player player : instance.players)
			player.sendMessage(msg);

		for (Player spectator : instance.spectators)
			spectator.sendMessage(msg);
	}

	/**
	 * Create the armor and custom head of the class. Includes Custom Head Texture,
	 * Armor Color and Protection Level. To call in the subclass constructor.
	 *
	 */
	protected void createArmor(Material blockMaterial, String textureUrl, String hexCodeChestplate,
			String hexCodeLeggings, String hexCodeBoots, int protectionLevel, String className) {
		// Head (helmet)
		if (blockMaterial != null) {
			playerHead = new ItemStack(blockMaterial);
		} else if (textureUrl != null) {
			playerHead = ItemHelper.createSkullTexture(textureUrl);
		}
		playerHead = ItemHelper.setDetails(getHelmet(playerHead), "&r&f" + className + " Head");
		if (protectionLevel > 0)
			playerHead.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);

		// Chestplate
		if (hexCodeChestplate != null) {
			chestplate = ItemHelper.setDetails(
					ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, hexCodeChestplate),
					"&r" + className + " Chestplate");
		} else {
			chestplate = ItemHelper.create(Material.AIR);
		}

		// Leggings
		if (hexCodeLeggings != null) {
			leggings = ItemHelper.setDetails(ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, hexCodeLeggings),
					"&r" + className + " Leggings");
		} else {
			leggings = ItemHelper.create(Material.AIR);
		}

		// Boots
		if (hexCodeBoots != null) {
			boots = ItemHelper.setDetails(ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, hexCodeBoots),
					"&r" + className + " Boots");
		} else {
			boots = ItemHelper.create(Material.AIR);
		}
	}

	/**
	 * Create the armor and custom head of the class. Includes Custom Head Texture,
	 * Armor Color and Protection Level. Single Color for full armor. To call in the
	 * subclass constructor.
	 *
	 */
	protected void createArmor(Material blockMaterial, String textureUrl, String hexCodeAllArmor, int protectionLevel,
			String className) {
		createArmor(blockMaterial, textureUrl, hexCodeAllArmor, hexCodeAllArmor, hexCodeAllArmor, protectionLevel,
				className);
	}

	public void resetHead() {
		player.getEquipment().setHelmet(playerHead);
	}
}
