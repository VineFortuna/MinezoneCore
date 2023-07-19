package anthony.SuperCraftBrawl.Game.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Score;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Timer;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public abstract class BaseClass {

	protected final GameInstance instance;
	protected final Player player;
	public int lives = 5;
	public boolean isDead = false;
	public int tokens = 0;
	public Score score;
	public int totalTokens = 0;
	public int totalKills = 0;
	public int eachLifeKills = 0;
	public int totalExp = 0;
	public double baseVerticalJump = 1.0;
	public boolean flintUsed = false;
	public Timer bedrockLava = new Timer();
	public Timer pearlTimer = new Timer();
	public Timer tnt = new Timer();
	public Timer witherBow = new Timer();
	public Timer slimeBall = new Timer();
	public Timer skeleAttack = new Timer();
	public Timer tntItem = new Timer();
	public Timer notch = new Timer();
	public Timer jeb = new Timer();
	public Timer enderman = new Timer();
	public Timer ice = new Timer();
	public Timer blazeRod = new Timer();
	public Timer golem = new Timer();
	public Timer villager = new Timer();
	public Timer herobrine = new Timer();
	protected Timer anvil = new Timer();
	public Timer bazooka = new Timer();
	public Timer ninja = new Timer();
	public Timer bee = new Timer();
	public Timer aggressiveGift = new Timer();
	public Timer defensiveGift = new Timer();
	public Timer mythicalGift = new Timer();
	public Timer wizard = new Timer();
	public boolean bedrockInvincibility = false;
	
	public int goldAmt = 0; // For Steve Class
	public int coalAmt = 0; // For Steve Class
	public int diaAmt = 0; // For Steve Class

	public Player bountyTarget = null;

	//This would also take in a SuperClass.
	public BaseClass(GameInstance instance, Player player) {
		this.instance = instance;
		this.player = player;
	}

	public int getLives() {
		return lives;
	}

	public int getTokens() {
		return tokens;
	}

	public abstract ClassType getType();

	public abstract void SetArmour(EntityEquipment playerEquip);

	public abstract ItemStack getAttackWeapon();

	public abstract void SetNameTag();

	public abstract void SetItems(Inventory playerInv);

	public abstract void UseItem(PlayerInteractEvent event);

	public void TakeDamage(EntityDamageEvent event) {
	}; // To override

	public void ProjectileLaunch(ProjectileLaunchEvent event) {
	}; // To override

	public void ProjectileHit(ProjectileHitEvent event) {
	}; // To override

	public void DoDamage(EntityDamageByEntityEvent event) {
	} // To override

	public void DoDamage2(EntityDamageByEntityEvent event) {
	} // To override

	public void onEntityTarget(EntityTargetLivingEntityEvent event) {
	} // To override

	public void PlayerMove(PlayerMoveEvent event) {

	}

	public void Tick(int gameTicks) {
	} // To override

	public void GameEnd() {
	} // To override

	public void LoadPlayer() {
		Inventory inv = player.getInventory();
		SetArmour(player.getEquipment());
		SetItems(inv);
	}

	public void LoadArmor(Player player) {
		SetArmour(player.getEquipment());
	}

	private String getPlayerRank(Player p) {
		return instance.getManager().getMain().getRankManager().getRank(p).getTagWithSpace();
	}

	// Event for EnderDragon class:
	private void enderdragonEvent(Player p, Player killer, PlayerData data2, BaseClass baseClass) {
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
		Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
			for (Item particle : deathParticles) {
				particle.remove();
			}
		}, 5 * 20);

		if (baseClass != null) {
			if (baseClass.getType() == ClassType.Enderdragon) {
				if (killer != null) {
					Location pLoc = p.getLocation();
					EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc, EntityType.ENDER_CRYSTAL);
					HealTask task = new HealTask(killer, crystal, instance.getManager().getMain());
					BukkitTask bukkit = Bukkit.getScheduler()
							.runTaskTimerAsynchronously(instance.getManager().getMain(), task, 0, 2L);
					task.set(bukkit);
				}
			}
		}
	}

	public void Death2(PlayerDeathEvent e) {
		isDead = false;
		if (player.getName() != null && lives > 0) {
			lives--;
			score.setScore(lives);

			Player killer = player.getKiller();
			Player p = player.getPlayer();

			for (Entity en : p.getWorld().getEntities())
				if (!(en instanceof Player))
					if (en.getName().contains(p.getName()))
						en.remove();

			if (p.getGameMode() != GameMode.SPECTATOR) {
				PlayerData pData = instance.getManager().getMain().getDataManager().getPlayerData(p); // Data of player
				PlayerData kData = instance.getManager().getMain().getDataManager().getPlayerData(killer); // Data of
																											// killer
				BaseClass pClass = null;
				BaseClass kClass = null;

				if (instance.classes.containsKey(p)) // Makes sure player is still in the game
					pClass = instance.classes.get(p);

				if (instance.classes.containsKey(killer)) // Same thing here
					kClass = instance.classes.get(killer);

				pData.deaths += 1;
				p.setFireTicks(0);
				for (PotionEffect type : p.getActivePotionEffects()) // Removing all player's effects
					p.removePotionEffect(type.getType());

				if (pClass != null) {
					pClass.eachLifeKills = 0; // When player dies, reset kills they get each life
					this.enderdragonEvent(p, killer, kData, kClass);

					if (p.getLocation().getY() <= 50) {
						if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
							EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p
									.getLastDamageCause();
							Entity damager = entityDamageEvent.getDamager();

							if (damager instanceof Player) {
								Player d = (Player) damager;

								if (instance.classes.containsKey(d)) {
									kClass = instance.classes.get(d);
									kData = instance.getManager().getMain().getDataManager().getPlayerData(d);
									if (kData != null && kData.killMsgs == 1 && kClass != null) {
										d.playSound(d.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
										kData.kills += 1;
										kData.exp += 29;
										kClass.totalExp += 29;

										// If tournament mode is on, give 1 point for kill:
										if (instance.getManager().getMain().tournament == true)
											kData.points++;

										// kClass.totalTokens += 1;
										kClass.totalKills++;
										kClass.eachLifeKills++;
										TellAll(instance.getManager().getMain()
												.color("&2&l(!) &cHello? AND GOODBYE TO " + getPlayerRank(p)
														+ ChatColor.WHITE + p.getPlayer().getName() + " "
														+ pClass.getType().getTag() + " &cAND ANYONE ELSE STANDING IN "
														+ getPlayerRank(d) + ChatColor.WHITE + d.getName() + " "
														+ kClass.getType().getTag() + "'s &cWAY!"));

									} else {
										d.playSound(d.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
										kData.kills += 1;
										kData.exp += 29;
										kClass.totalExp += 29;

										// If tournament mode is on, give 1 point for kill:
										if (instance.getManager().getMain().tournament == true)
											kData.points++;

										// kClass.totalTokens += 1;
										kClass.totalKills++;
										kClass.eachLifeKills++;
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ pClass.getType().getTag() + ChatColor.RED + " was doomed to fall by "
												+ ChatColor.WHITE + getPlayerRank(d) + d.getName() + " "
												+ kClass.getType().getTag());
									}
									p.teleport(d.getLocation());
								} else {
									Random r = new Random();
									int chance = r.nextInt(2);

									if (pData != null && pData.killMsgs == 1) {
										if (chance == 0) {
											TellAll(instance.getManager().getMain()
													.color("&2&l(!) "
															+ instance.getManager().getMain().getRankManager()
																	.getRank(player).getTagWithSpace()
															+ "&r " + p.getPlayer().getName() + " "
															+ pClass.getType().getTag()
															+ " &csaid NO THANK YOU and took the easy way out"));
										} else {
											TellAll(instance.getManager().getMain().color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " " + pClass.getType().getTag()
													+ " &cwalked off the edge.."));
										}
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ ChatColor.RED + " fell into the void");
									}
									p.teleport(instance.GetSpecLoc());
								}
							} else if (damager instanceof Arrow) {
								Arrow a = (Arrow) damager;

								if (a.getShooter() instanceof Player && a.getShooter() != null) {
									Player d = (Player) a.getShooter();

									if (instance.classes.containsKey(d)) {
										kClass = instance.classes.get(d);
										kData = instance.getManager().getMain().getDataManager().getPlayerData(d);
										d.playSound(d.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
										kData.kills += 1;
										kData.exp += 29;
										kClass.totalExp += 29;

										// If tournament mode is on, give 1 point for kill:
										if (instance.getManager().getMain().tournament == true)
											kData.points++;

										// kClass.totalTokens += 1;
										kClass.totalKills++;
										kClass.eachLifeKills++;

										if (kData != null && kData.killMsgs == 1 && kClass != null) {
											TellAll(instance.getManager().getMain()
													.color("&2&l(!) &cHello? AND GOODBYE TO " + getPlayerRank(p)
															+ ChatColor.WHITE + p.getPlayer().getName() + " "
															+ pClass.getType().getTag()
															+ " &cAND ANYONE ELSE STANDING IN " + getPlayerRank(d)
															+ ChatColor.WHITE + d.getName() + " "
															+ kClass.getType().getTag() + "'s &cWAY!"));
										} else {
											TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
													+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName() + " "
													+ pClass.getType().getTag() + ChatColor.RED
													+ " was doomed to fall by " + ChatColor.WHITE + getPlayerRank(d)
													+ d.getName() + " " + kClass.getType().getTag());
										}
									} else {
										Random r = new Random();
										int chance = r.nextInt(2);

										if (pData != null && pData.killMsgs == 1) {
											if (chance == 0) {
												TellAll(instance.getManager().getMain().color("&2&l(!) "
														+ instance.getManager().getMain().getRankManager()
																.getRank(player).getTagWithSpace()
														+ "&r " + p.getPlayer().getName() + " "
														+ pClass.getType().getTag()
														+ " &csaid NO THANK YOU and took the easy way out"));
											} else {
												TellAll(instance.getManager().getMain().color("&2&l(!) "
														+ instance.getManager().getMain().getRankManager()
																.getRank(player).getTagWithSpace()
														+ "&r " + p.getPlayer().getName() + " "
														+ pClass.getType().getTag() + " &cwalked off the edge.."));
											}
										} else {
											TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
													+ ChatColor.RESET
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ p.getPlayer().getName() + " " + pClass.getType().getTag()
													+ ChatColor.RED + " fell into the void");
										}
										p.teleport(instance.GetSpecLoc()); // Teleports to spectator loc
									}
								} else {
									Random r = new Random();
									int chance = r.nextInt(2);

									if (pData != null && pData.killMsgs == 1) {
										if (chance == 0) {
											TellAll(instance.getManager().getMain()
													.color("&2&l(!) "
															+ instance.getManager().getMain().getRankManager()
																	.getRank(player).getTagWithSpace()
															+ "&r " + p.getPlayer().getName() + " "
															+ pClass.getType().getTag()
															+ " &csaid NO THANK YOU and took the easy way out"));
										} else {
											TellAll(instance.getManager().getMain().color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " " + pClass.getType().getTag()
													+ " &cwalked off the edge.."));
										}
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ ChatColor.RED + " fell into the void");
									}
									p.teleport(instance.GetSpecLoc()); // Teleports to spectator loc
								}
							} else {
								Random r = new Random();
								int chance = r.nextInt(2);

								if (pData != null && pData.killMsgs == 1) {
									if (chance == 0) {
										TellAll(instance.getManager().getMain()
												.color("&2&l(!) "
														+ instance.getManager().getMain().getRankManager()
																.getRank(player).getTagWithSpace()
														+ "&r " + p.getPlayer().getName() + " "
														+ pClass.getType().getTag()
														+ " &csaid NO THANK YOU and took the easy way out"));
									} else {
										TellAll(instance.getManager().getMain().color("&2&l(!) "
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r " + p.getPlayer().getName() + " " + pClass.getType().getTag()
												+ " &cwalked off the edge.."));
									}
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ instance.getManager().getMain().getRankManager().getRank(player)
													.getTagWithSpace()
											+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
											+ " fell into the void");
								}
								p.teleport(instance.GetSpecLoc()); // Teleports to spectator loc
							}
						} else {
							Random r = new Random();
							int chance = r.nextInt(2);

							if (pData != null && pData.killMsgs == 1) {
								if (chance == 0) {
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " " + pClass.getType().getTag()
													+ " &csaid NO THANK YOU and took the easy way out"));
								} else {
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " " + pClass.getType().getTag()
													+ " &cwalked off the edge.."));
								}
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ instance.getManager().getMain().getRankManager().getRank(player)
												.getTagWithSpace()
										+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
										+ " fell into the void");
							}
							p.teleport(instance.GetSpecLoc()); // Teleports to spectator loc
						}
					} else if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
							&& p.getLastDamageCause().getCause() == DamageCause.MAGIC) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
								+ " was murdered via the dark arts");
						p.teleport(instance.GetSpecLoc());
					} else if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
							&& p.getLastDamageCause().getCause() == DamageCause.WITHER) {
						if (killer == null) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " withered away");
							p.teleport(instance.GetSpecLoc());
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " was withered by " + ChatColor.WHITE + getPlayerRank(killer)
									+ killer.getName() + " " + kClass.getType().getTag());
							p.teleport(instance.GetSpecLoc());
						}
					} else if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
						EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p
								.getLastDamageCause();
						Entity damager = entityDamageEvent.getDamager();

						if (damager instanceof Player) {
							Player d = (Player) damager;

							if (instance.classes.containsKey(d)) {
								kClass = instance.classes.get(d);
								kData = instance.getManager().getMain().getDataManager().getPlayerData(d);
								if (d != p || killer != p) {
									d.playSound(d.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
									kData.kills += 1;
									kData.exp += 29;
									kClass.totalExp += 29;

									// If tournament mode is on, give 1 point for kill:
									if (instance.getManager().getMain().tournament == true)
										kData.points++;

									// kClass.totalTokens += 1;
									kClass.totalKills++;
									kClass.eachLifeKills++;

									if (kData != null && kData.killMsgs == 1) {
										TellAll(instance.getManager().getMain()
												.color("&2&l(!) " + getPlayerRank(p) + "&r " + p.getPlayer().getName()
														+ " " + pClass.getType().getTag()
														+ " &cwas not strong enough to encounter " + getPlayerRank(d)
														+ " &r" + d.getName() + " " + kClass.getType().getTag()));
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ /* baseClass2.getType().getTag() */pClass.getType().getTag()
												+ ChatColor.RED + " was killed by " + ChatColor.WHITE + getPlayerRank(d)
												+ d.getName() + " " + kClass.getType().getTag());
									}
									p.teleport(d);
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ /* baseClass2.getType().getTag() */pClass.getType().getTag()
											+ ChatColor.RED + " died");
									p.teleport(instance.GetSpecLoc());
								}
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " "
										+ /* baseClass2.getType().getTag() */pClass.getType().getTag() + ChatColor.RED
										+ " committed suicide");
								p.teleport(instance.GetSpecLoc());
								// }
							}
						} else if (damager instanceof Zombie) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " was killed by a " + ChatColor.YELLOW + "zombie");
						} else if (damager instanceof Skeleton) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " was shot to death by a " + ChatColor.YELLOW + "skeleton");
						} else if (damager instanceof Arrow) {
							Arrow a = (Arrow) damager;
							if (a.getShooter() instanceof Player && a.getShooter() != null) {
								Player d = (Player) a.getShooter();

								if (instance.classes.containsKey(player)) {
									kClass = instance.classes.get(d);
									PlayerData killerData = instance.getManager().getMain().getDataManager()
											.getPlayerData(d);
									if (d != p || killer != p) {
										ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
												"" + ChatColor.YELLOW + ChatColor.BOLD + "Health Pot");
										Potion pot = new Potion(1);
										pot.setType(PotionType.INSTANT_HEAL);
										pot.setSplash(true);
										pot.apply(item);

										d.playSound(d.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
										kData.kills += 1;
										kData.exp += 29;
										kClass.totalExp += 29;

										// If tournament mode is on, give 1 point for kill:
										if (instance.getManager().getMain().tournament == true)
											kData.points++;

										// kClass.totalTokens += 1;
										kClass.totalKills++;
										kClass.eachLifeKills++;

										d.getInventory().addItem(item);
										d.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
												+ ChatColor.RESET + ChatColor.YELLOW + "You killed " + ChatColor.RESET
												+ p.getPlayer().getName() + ChatColor.YELLOW + " and got rewarded a "
												+ ChatColor.YELLOW + ChatColor.BOLD + "Health Pot");

										if (killerData != null && killerData.killMsgs == 1) {
											TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
													+ "&r " + p.getPlayer().getName() + " " + pClass.getType().getTag()
													+ " &cwas not strong enough to encounter " + getPlayerRank(d)
													+ " &r" + d.getName() + " " + kClass.getType().getTag()));
										} else {
											TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
													+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName() + " "
													+ pClass.getType().getTag() + ChatColor.RED + " was killed by "
													+ ChatColor.WHITE + getPlayerRank(d) + d.getName() + " "
													+ kClass.getType().getTag());
										}
										p.teleport(d);
									}
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ pClass.getType().getTag() + ChatColor.RED + " committed suicide");
									p.teleport(d);
									// }
								}
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
										+ ChatColor.RED + " died");
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " just died SO badly");
						}
					} else if (DamageCause.VOID != null) {
						if (instance.gameType == GameType.FRENZY) {
							if (lives > 0) {
								BaseClass bc = instance.oldClasses.get(p);
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " "
										+ /* baseClass2.getType().getTag() */bc.getType().getTag() + ChatColor.RED
										+ " just died SO badly");
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " "
										+ /* baseClass2.getType().getTag() */pClass.getType().getTag() + ChatColor.RED
										+ " just died SO badly");
							}
							p.getPlayer().setFireTicks(0);
						} else {
							if (lives == 0) {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
										+ ChatColor.RED + " just died SO badly");
							} else if (lives > 0) {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
										+ ChatColor.RED + " just died SO badly");
							}
							p.getPlayer().setFireTicks(0);
						}
					} else if (DamageCause.SUICIDE != null) {
						if (instance.gameType == GameType.FRENZY) {
							BaseClass bc = instance.oldClasses.get(player);
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " "
									+ /* baseClass2.getType().getTag() */bc.getType().getTag() + ChatColor.RED
									+ " committed suicide");
							p.getPlayer().setFireTicks(0);
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " committed suicide");
							p.getPlayer().setFireTicks(0);
						}
					} else if (killer == null && !(player.getKiller() instanceof Player) && DamageCause.LAVA != null
							|| DamageCause.FIRE != null || DamageCause.FIRE_TICK != null) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ p.getPlayer().getName() + ChatColor.RED + " just burnt to death");
					} else {
						if (lives == 0) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " just died SO badly");
						} else if (lives > 0) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + pClass.getType().getTag()
									+ ChatColor.RED + " just died SO badly");
						}
						p.getPlayer().setFireTicks(0);
					}

					kClass = instance.classes.get(killer);

					if (lives == 0) {
						PlayerData data = instance.getManager().getMain().getDataManager().getPlayerData(p);

						if (data != null) {
							data.losses += 1;
						}
						if (killer != null) {
							String msg = instance.getManager().getMain().color("&4&lELIMINATED &e" + p.getName());
							PacketPlayOutChat packet = new PacketPlayOutChat(
									ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
							CraftPlayer craft = (CraftPlayer) killer;
							craft.getHandle().playerConnection.sendPacket(packet);
						}
						p.setDisplayName("" + p.getName() + " " + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC
								+ "Spectator" + ChatColor.RESET);
						Random r = new Random();
						int chance = r.nextInt(100);

						if (chance >= 0 && chance <= 15) {

							if (data != null) {
								data.mysteryChests++;
								player.sendMessage(instance.getManager().getMain()
										.color("&5&l(!) &rYou have found &e1 Mystery Chest!"));
							}
						}

						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED
								+ " has been eliminated!");

						if (instance.getMap() != null) {
							p.sendMessage("" + ChatColor.BOLD + "=====================");
							p.sendMessage("" + ChatColor.BOLD + "||");
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
							pClass.tokens += tokensEarned;
							pClass.totalTokens += tokensEarned;
							p.sendMessage("        " + "       Placed #" + instance.alivePlayers + ": " + tokensEarned
									+ " Tokens");

							if (pClass != null && pClass.totalKills >= 0) {
								player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
										+ ChatColor.BOLD + "  " + pClass.totalKills + " Kills: " + ChatColor.RESET
										+ ChatColor.YELLOW + (pClass.totalKills * 2) + " Tokens");
								pClass.tokens += pClass.totalKills * 2;
								pClass.totalTokens += pClass.totalKills;
							}
							if (p.hasPermission("scb.rankBonus")) {
								p.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE + ChatColor.BOLD
										+ "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW + "10 Tokens");
								pClass.tokens += 10;
								pClass.totalTokens += 10;
							}
							p.sendMessage("" + ChatColor.BOLD + "||");
							p.sendMessage("" + ChatColor.BOLD + "||");
							p.sendMessage("" + ChatColor.BOLD + "||");
							p.sendMessage("" + ChatColor.BOLD + "=====================");
							p.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have gained " + ChatColor.YELLOW + pClass.totalExp + " EXP!");
							p.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have earned " + ChatColor.YELLOW + pClass.totalTokens + " Tokens!");

							if (pData.exp >= 2500) {
								pData.level++;
								pData.exp -= 2500;
								p.sendMessage(instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
								p.sendMessage("You are now Level: " + pData.level + "!");
							}
						} // START HERE
						/*
						 * else { List<String> aliveTeam = new ArrayList<String>(); for (Entry<Player,
						 * BaseClass> entry : instance.classes.entrySet()) { if
						 * (entry.getValue().getLives() > 0) { if
						 * (!(aliveTeam.contains(instance.team.get(entry.getKey())))) {
						 * aliveTeam.add(instance.team.get(entry.getKey())); instance.teamsAlive++; } }
						 * } instance.teamsAlive++; if (instance.team.get(p).equals("Red")) { if
						 * (!(aliveTeam.contains("Red"))) { TellAll(instance.getManager().getMain()
						 * .color("&2&l(!) &c&lRed Team &r has been eliminated!"));
						 * 
						 * for (Player losers : instance.redTeam) { BaseClass loserBc =
						 * instance.classes.get(losers); PlayerData data3 =
						 * instance.getManager().getMain().getDataManager() .getPlayerData(losers);
						 * losers.sendMessage("" + ChatColor.BOLD + "=====================");
						 * losers.sendMessage("" + ChatColor.BOLD + "||"); losers.sendMessage("" +
						 * ChatColor.BOLD + "||"); losers.sendMessage("" + ChatColor.BOLD + "|| " +
						 * "        " + ChatColor.RED + ChatColor.BOLD + "  GAME LOST");
						 * losers.sendMessage("" + ChatColor.BOLD + "||");
						 * 
						 * int tokensEarned = 0; if (instance.aliveTeams == 3) tokensEarned = 5; else if
						 * (instance.aliveTeams == 2) tokensEarned = 7;
						 * 
						 * losers.sendMessage("        " + "    " + instance.aliveTeams + " Place: " +
						 * tokensEarned + " Tokens"); data3.tokens += tokensEarned; loserBc.totalTokens
						 * += tokensEarned;
						 * 
						 * if (loserBc.totalKills >= 0) { player.sendMessage("" + ChatColor.BOLD + "|| "
						 * + "        " + ChatColor.BLUE + ChatColor.BOLD + "  " + loserBc.totalKills +
						 * " Kills: " + ChatColor.RESET + ChatColor.YELLOW + loserBc.totalKills +
						 * " Tokens"); data3.tokens += loserBc.totalKills; loserBc.totalTokens +=
						 * loserBc.totalKills; } if (losers.hasPermission("scb.rankBonus")) {
						 * losers.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
						 * + ChatColor.BOLD + "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW +
						 * "10 Tokens"); data3.tokens += 10; loserBc.totalTokens += 10; }
						 * losers.sendMessage("" + ChatColor.BOLD + "||"); losers.sendMessage("" +
						 * ChatColor.BOLD + "||"); losers.sendMessage("" + ChatColor.BOLD + "||");
						 * losers.sendMessage("" + ChatColor.BOLD + "=====================");
						 * losers.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " +
						 * ChatColor.RESET + "You have gained " + ChatColor.YELLOW + loserBc.totalExp +
						 * " EXP!");
						 * 
						 * if (data3.exp >= 2500) { data3.level++; data3.exp -= 2500;
						 * losers.sendMessage(
						 * instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
						 * losers.sendMessage("You are now Level: " + data3.level + "!"); }
						 * 
						 * losers.sendMessage(instance.getManager().getMain().color(
						 * "&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens")); } } }
						 * else if (instance.team.get(p).equals("Blue")) { if
						 * (!(aliveTeam.contains("Blue"))) { TellAll(instance.getManager().getMain()
						 * .color("&2&l(!) &b&lBlue Team &r has been eliminated!"));
						 * 
						 * for (Player losers : instance.blueTeam) { BaseClass loserBc =
						 * instance.classes.get(losers); PlayerData data3 =
						 * instance.getManager().getMain().getDataManager() .getPlayerData(losers);
						 * losers.sendMessage("" + ChatColor.BOLD + "=====================");
						 * losers.sendMessage("" + ChatColor.BOLD + "||"); losers.sendMessage("" +
						 * ChatColor.BOLD + "||"); losers.sendMessage("" + ChatColor.BOLD + "|| " +
						 * "        " + ChatColor.RED + ChatColor.BOLD + "  GAME LOST");
						 * losers.sendMessage("" + ChatColor.BOLD + "||");
						 * 
						 * int tokensEarned = 0; if (instance.aliveTeams == 3) tokensEarned = 5; else if
						 * (instance.aliveTeams == 2) tokensEarned = 7;
						 * 
						 * losers.sendMessage("        " + "    " + instance.aliveTeams + " Place: " +
						 * tokensEarned + " Tokens"); data3.tokens += tokensEarned; loserBc.totalTokens
						 * += tokensEarned;
						 * 
						 * if (loserBc.totalKills >= 0) { player.sendMessage("" + ChatColor.BOLD + "|| "
						 * + "        " + ChatColor.BLUE + ChatColor.BOLD + "  " + loserBc.totalKills +
						 * " Kills: " + ChatColor.RESET + ChatColor.YELLOW + loserBc.totalKills +
						 * " Tokens"); data3.tokens += loserBc.totalKills; loserBc.totalTokens +=
						 * loserBc.totalKills; } if (losers.hasPermission("scb.rankBonus")) {
						 * losers.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
						 * + ChatColor.BOLD + "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW +
						 * "10 Tokens"); data3.tokens += 10; loserBc.totalTokens += 10; }
						 * losers.sendMessage("" + ChatColor.BOLD + "||"); losers.sendMessage("" +
						 * ChatColor.BOLD + "||"); losers.sendMessage("" + ChatColor.BOLD + "||");
						 * losers.sendMessage("" + ChatColor.BOLD + "=====================");
						 * losers.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " +
						 * ChatColor.RESET + "You have gained " + ChatColor.YELLOW + loserBc.totalExp +
						 * " EXP!");
						 * 
						 * if (data3.exp >= 2500) { data3.level++; data3.exp -= 2500;
						 * losers.sendMessage(
						 * instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
						 * losers.sendMessage("You are now Level: " + data3.level + "!"); }
						 * losers.sendMessage(instance.getManager().getMain().color(
						 * "&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens")); } } }
						 * else if (instance.team.get(p).equals("Black")) { if
						 * (!(aliveTeam.contains("Black"))) { TellAll(instance.getManager().getMain()
						 * .color("&2&l(!) &0&lBlack Team &r has been eliminated!"));
						 * 
						 * for (Player losers : instance.blackTeam) { BaseClass loserBc =
						 * instance.classes.get(losers); PlayerData data3 =
						 * instance.getManager().getMain().getDataManager() .getPlayerData(losers);
						 * losers.sendMessage("" + ChatColor.BOLD + "=====================");
						 * losers.sendMessage("" + ChatColor.BOLD + "||"); losers.sendMessage("" +
						 * ChatColor.BOLD + "||"); losers.sendMessage("" + ChatColor.BOLD + "|| " +
						 * "        " + ChatColor.RED + ChatColor.BOLD + "  GAME LOST");
						 * losers.sendMessage("" + ChatColor.BOLD + "||");
						 * 
						 * int tokensEarned = 0; if (instance.aliveTeams == 3) tokensEarned = 5; else if
						 * (instance.aliveTeams == 2) tokensEarned = 7;
						 * 
						 * losers.sendMessage("        " + "    " + instance.aliveTeams + " Place: " +
						 * tokensEarned + " Tokens"); data3.tokens += tokensEarned; loserBc.totalTokens
						 * += tokensEarned;
						 * 
						 * if (loserBc.totalKills >= 0) { player.sendMessage("" + ChatColor.BOLD + "|| "
						 * + "        " + ChatColor.BLUE + ChatColor.BOLD + "  " + loserBc.totalKills +
						 * " Kills: " + ChatColor.RESET + ChatColor.YELLOW + loserBc.totalKills +
						 * " Tokens"); data3.tokens += loserBc.totalKills; loserBc.totalTokens +=
						 * loserBc.totalKills; } if (losers.hasPermission("scb.rankBonus")) {
						 * losers.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
						 * + ChatColor.BOLD + "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW +
						 * "10 Tokens"); data3.tokens += 10; loserBc.totalTokens += 10; }
						 * losers.sendMessage("" + ChatColor.BOLD + "||"); losers.sendMessage("" +
						 * ChatColor.BOLD + "||"); losers.sendMessage("" + ChatColor.BOLD + "||");
						 * losers.sendMessage("" + ChatColor.BOLD + "=====================");
						 * losers.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " +
						 * ChatColor.RESET + "You have gained " + ChatColor.YELLOW + loserBc.totalExp +
						 * " EXP!");
						 * 
						 * if (data3.exp >= 2500) { data3.level++; data3.exp -= 2500;
						 * losers.sendMessage(
						 * instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
						 * losers.sendMessage("You are now Level: " + data3.level + "!"); }
						 * losers.sendMessage(instance.getManager().getMain().color(
						 * "&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens")); } } }
						 * instance.aliveTeams--; }
						 */

					} else if (lives == 1) {
						if (killer != null) {
							String msg = instance.getManager().getMain().color("&4&lKILLED &e" + p.getName());
							PacketPlayOutChat packet = new PacketPlayOutChat(
									ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
							CraftPlayer craft = (CraftPlayer) killer;
							craft.getHandle().playerConnection.sendPacket(packet);
						}
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED + " has "
								+ lives + " life left");

					} else {
						if (killer != null) {
							String msg = instance.getManager().getMain().color("&4&lKILLED &e" + p.getName());
							PacketPlayOutChat packet = new PacketPlayOutChat(
									ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
							CraftPlayer craft = (CraftPlayer) killer;
							craft.getHandle().playerConnection.sendPacket(packet);
						}
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + pClass.getType().getTag() + ChatColor.RED + " has "
								+ lives + " lives left");
					}
				}
			}
		}

	}

	private void checkBountyKill(BaseClass kClass, Player playerKilled, Player killer) {
		PlayerData data = instance.getManager().getMain().getDataManager().getPlayerData(killer);
		if (data != null) {
			if (kClass.bountyTarget != null) {
				if (kClass.bountyTarget == playerKilled) {
					kClass.bountyTarget = null;
					data.tokens += 25;
					killer.sendMessage("");
					player.sendMessage("");
					killer.sendMessage(instance.getManager().getMain()
							.color("&2&l(!) &e&lBOUNTY CLAIMED! &rYou earned &e25 Bonus Tokens!"));
					player.sendMessage(instance.getManager().getMain().color("&2&l(!) &e&lBOUNTY CLAIMED! &e"
							+ killer.getName() + " &rhas claimed their bounty on you!"));
					killer.sendTitle(instance.getManager().getMain().color("&e&lBOUNTY"),
							instance.getManager().getMain().color("&rYou claimed &e25 Bonus Tokens!"));
					player.sendTitle(instance.getManager().getMain().color("&e&lBOUNTY"), instance.getManager()
							.getMain().color("&e" + killer.getName() + " &rhas claimed their bounty on you!"));
					killer.sendMessage("");
					player.sendMessage("");
				}
			}
		}
	}

	public void Death(PlayerDeathEvent e) {
		if (player.getName() != null && lives > 0) {
			lives--;
			score.setScore(lives);

			Player killer = player.getKiller();
			Player p = player.getPlayer();

			for (Entity en : p.getWorld().getEntities())
				if (!(en instanceof Player))
					if (en.getName().contains(p.getName()))
						en.remove();

			if (isDead) {
				PlayerData data2 = instance.getManager().getMain().getDataManager().getPlayerData(p);
				data2.deaths += 1;
				for (PotionEffect type : p.getActivePotionEffects())
					p.removePotionEffect(type.getType());

				p.setFireTicks(0);
				p.setLastDamage(0);
				// data2.winstreak = 0;
				BaseClass baseClass = instance.classes.get(killer);
				BaseClass baseClass2 = instance.classes.get(p);
				baseClass2.eachLifeKills = 0;

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
				Bukkit.getScheduler().runTaskLater(instance.getManager().getMain(), () -> {
					for (Item particle : deathParticles) {
						particle.remove();
					}
				}, 5 * 20);

				if (baseClass != null) {
					if (baseClass.getType() == ClassType.Enderdragon) {
						if (killer != null) {
							Location pLoc = p.getLocation();
							EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc,
									EntityType.ENDER_CRYSTAL);
							HealTask task = new HealTask(killer, crystal, instance.getManager().getMain());
							BukkitTask bukkit = Bukkit.getScheduler()
									.runTaskTimerAsynchronously(instance.getManager().getMain(), task, 0, 2L);
							task.set(bukkit);
						}
					}
				}

				if (p.getLocation().getY() <= 50) {
					if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
						EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p
								.getLastDamageCause();
						Entity damager = entityDamageEvent.getDamager();

						if (damager instanceof Player) {
							Player d = (Player) damager;

							if (instance.classes.containsKey(d)) {
								baseClass = instance.classes.get(d);
								PlayerData killerData = instance.getManager().getMain().getDataManager()
										.getPlayerData(d);

								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(d, p);
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) &cHello? AND GOODBYE TO " + getPlayerRank(p)
													+ ChatColor.WHITE + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag() + " &cAND ANYONE ELSE STANDING IN "
													+ getPlayerRank(d) + ChatColor.WHITE + d.getName() + " "
													+ baseClass.getType().getTag() + "'s &cWAY!"));
								} else {
									this.giveStats(d, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ baseClass2.getType().getTag() + ChatColor.RED + " was doomed to fall by "
											+ ChatColor.WHITE + getPlayerRank(d) + d.getName() + " "
											+ baseClass.getType().getTag());
								}
								p.teleport(d.getLocation());
							} else {
								Random r = new Random();
								int chance = r.nextInt(2);

								if (data2 != null && data2.killMsgs == 1) {
									if (chance == 0) {
										TellAll(instance.getManager().getMain()
												.color("&2&l(!) "
														+ instance.getManager().getMain().getRankManager()
																.getRank(player).getTagWithSpace()
														+ "&r " + p.getPlayer().getName() + " "
														+ baseClass2.getType().getTag()
														+ " &csaid NO THANK YOU and took the easy way out"));
									} else {
										TellAll(instance.getManager().getMain().color("&2&l(!) "
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r " + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
												+ " &cwalked off the edge.."));
									}
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ instance.getManager().getMain().getRankManager().getRank(player)
													.getTagWithSpace()
											+ p.getPlayer().getName() + " " + baseClass2.getType().getTag()
											+ ChatColor.RED + " fell into the void");
								}
								p.teleport(instance.GetSpecLoc());
							}
						} else if (damager instanceof Arrow) {
							Arrow a = (Arrow) damager;

							if (a.getShooter() instanceof Player && a.getShooter() != null) {
								Player d = (Player) a.getShooter();

								if (instance.classes.containsKey(d)) {
									baseClass = instance.classes.get(d);
									PlayerData killerData = instance.getManager().getMain().getDataManager()
											.getPlayerData(d);
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(d, p);
										TellAll(instance.getManager().getMain().color("&2&l(!) &cHello? AND GOODBYE TO "
												+ getPlayerRank(p) + ChatColor.WHITE + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + " &cAND ANYONE ELSE STANDING IN "
												+ getPlayerRank(d) + ChatColor.WHITE + d.getName() + " "
												+ baseClass.getType().getTag() + "'s &cWAY!"));
									} else {
										this.giveStats(d, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + ChatColor.RED
												+ " was doomed to fall by " + ChatColor.WHITE + getPlayerRank(d)
												+ d.getName() + " " + baseClass.getType().getTag());
									}
								} else {
									Random r = new Random();
									int chance = r.nextInt(2);

									if (data2 != null && data2.killMsgs == 1) {
										if (chance == 0) {
											TellAll(instance.getManager().getMain().color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag()
													+ " &csaid NO THANK YOU and took the easy way out"));
										} else {
											TellAll(instance.getManager().getMain().color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag() + " &cwalked off the edge.."));
										}
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ p.getPlayer().getName() + " " + baseClass2.getType().getTag()
												+ ChatColor.RED + " fell into the void");
									}
									p.teleport(instance.GetSpecLoc());
								}
							} else {
								Random r = new Random();
								int chance = r.nextInt(2);

								if (data2 != null && data2.killMsgs == 1) {
									if (chance == 0) {
										TellAll(instance.getManager().getMain()
												.color("&2&l(!) "
														+ instance.getManager().getMain().getRankManager()
																.getRank(player).getTagWithSpace()
														+ "&r " + p.getPlayer().getName() + " "
														+ baseClass2.getType().getTag()
														+ " &csaid NO THANK YOU and took the easy way out"));
									} else {
										TellAll(instance.getManager().getMain().color("&2&l(!) "
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r " + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
												+ " &cwalked off the edge.."));
									}
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ instance.getManager().getMain().getRankManager().getRank(player)
													.getTagWithSpace()
											+ p.getPlayer().getName() + " " + baseClass2.getType().getTag()
											+ ChatColor.RED + " fell into the void");
								}
								p.teleport(instance.GetSpecLoc());
							}
						} else {
							Random r = new Random();
							int chance = r.nextInt(2);

							if (data2 != null && data2.killMsgs == 1) {
								if (chance == 0) {
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag()
													+ " &csaid NO THANK YOU and took the easy way out"));
								} else {
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) "
													+ instance.getManager().getMain().getRankManager().getRank(player)
															.getTagWithSpace()
													+ "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag() + " &cwalked off the edge.."));
								}
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ instance.getManager().getMain().getRankManager().getRank(player)
												.getTagWithSpace()
										+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
										+ " fell into the void");
							}
							p.teleport(instance.GetSpecLoc());
						}
					} else if (killer != null && instance.classes.containsKey(killer)) {
						PlayerData killerData = instance.getManager().getMain().getDataManager().getPlayerData(killer);
						if (killer != p) {
							if (instance.gameType == GameType.FRENZY) {
								BaseClass bc = instance.oldClasses.get(p);
								if (lives > 0) {

									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(killer, p);
										TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
												+ "&r " + p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &cwas not strong enough to encounter " + getPlayerRank(killer)
												+ " &r" + killer.getName() + " " + baseClass.getType().getTag()));
									} else {
										this.giveStats(killer, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ /* baseClass2.getType().getTag() */bc.getType().getTag()
												+ ChatColor.RED + " was killed by " + ChatColor.WHITE
												+ getPlayerRank(killer) + killer.getName() + " "
												+ baseClass.getType().getTag());
									}
								} else {
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(killer, p);
										TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
												+ "&r " + p.getPlayer().getName() + " " + bc.getType().getTag()
												+ " &cwas not strong enough to encounter " + getPlayerRank(killer)
												+ " &r" + killer.getName() + " " + baseClass.getType().getTag()));
									} else {
										this.giveStats(killer, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag()
												+ ChatColor.RED + " was killed by " + ChatColor.WHITE
												+ getPlayerRank(killer) + killer.getName() + " "
												+ baseClass.getType().getTag());
									}
								}
								p.teleport(killer);
							} else {
								if (lives == 0) {
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(killer, p);
										TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
												+ "&r " + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
												+ " &cwas not strong enough to encounter " + getPlayerRank(killer)
												+ " &r" + killer.getName() + " " + baseClass.getType().getTag()));
									} else {
										this.giveStats(killer, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + ChatColor.RED + " was killed by "
												+ ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
												+ baseClass.getType().getTag());
									}
								} else if (lives > 0) {
									if (killerData != null && killerData.killMsgs == 1) {
										this.giveStats(killer, p);
										TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
												+ "&r " + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
												+ " &cwas not strong enough to encounter " + getPlayerRank(killer)
												+ " &r" + killer.getName() + " " + baseClass.getType().getTag()));
									} else {
										this.giveStats(killer, p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + ChatColor.RED + " was killed by "
												+ ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
												+ baseClass.getType().getTag());
									}
								}
								p.teleport(killer);
							}
						} else {
							if (instance.gameType == GameType.FRENZY) {
								if (lives > 0) {
									BaseClass bc = instance.oldClasses.get(p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ /* baseClass2.getType().getTag() */bc.getType().getTag() + ChatColor.RED
											+ " committed suicide");
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag()
											+ ChatColor.RED + " committed suicide");
								}
								p.teleport(killer);
							} else {
								if (lives > 0) {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
								}
								p.teleport(killer);
							}
							// }
						}
					} else {
						Random r = new Random();
						int chance = r.nextInt(2);

						if (data2 != null && data2.killMsgs == 1) {
							if (chance == 0) {
								TellAll(instance.getManager().getMain()
										.color("&2&l(!) "
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r " + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
												+ " &csaid NO THANK YOU and took the easy way out"));
							} else {
								TellAll(instance.getManager().getMain()
										.color("&2&l(!) "
												+ instance.getManager().getMain().getRankManager().getRank(player)
														.getTagWithSpace()
												+ "&r " + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
												+ " &cwalked off the edge.."));
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ instance.getManager().getMain().getRankManager().getRank(player).getTagWithSpace()
									+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
									+ " fell into the void");
						}
						p.teleport(instance.GetSpecLoc());
					}
				} else if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
						&& p.getLastDamageCause().getCause() == DamageCause.MAGIC) {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
							+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
							+ " was murdered via the dark arts");
					p.teleport(instance.GetSpecLoc());
				} else if (p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null
						&& p.getLastDamageCause().getCause() == DamageCause.WITHER) {
					if (killer == null) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " withered away");
						p.teleport(instance.GetSpecLoc());
					} else {
						this.giveStats(killer, p);
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " was withered by " + ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
								+ baseClass.getType().getTag());
						p.teleport(instance.GetSpecLoc());
						this.healthPots(killer);
					}
				} else if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p.getLastDamageCause();
					Entity damager = entityDamageEvent.getDamager();

					if (damager instanceof Player) {
						Player d = (Player) damager;

						if (instance.classes.containsKey(player)) {
							baseClass = instance.classes.get(d);
							PlayerData killerData = instance.getManager().getMain().getDataManager().getPlayerData(d);
							if (d != p || killer != p) {
								if (instance.gameType == GameType.FRENZY) {
									BaseClass bc = instance.oldClasses.get(p);
									if (lives > 0) {

										if (killerData != null && killerData.killMsgs == 1) {
											this.giveStats(d, p);
											TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
													+ "&r " + p.getPlayer().getName() + " " + bc.getType().getTag()
													+ " &cwas not strong enough to encounter " + getPlayerRank(d)
													+ " &r" + d.getName() + " " + baseClass.getType().getTag()));
											this.healthPots(d);
										} else {
											this.giveStats(d, p);
											TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
													+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName() + " "
													+ /* baseClass2.getType().getTag() */bc.getType().getTag()
													+ ChatColor.RED + " was killed by " + ChatColor.WHITE
													+ getPlayerRank(d) + d.getName() + " "
													+ baseClass.getType().getTag());
											this.healthPots(d);
										}
									} else {
										if (killerData != null && killerData.killMsgs == 1) {
											this.giveStats(d, p);
											TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
													+ "&r " + p.getPlayer().getName() + " " + bc.getType().getTag()
													+ " &cwas not strong enough to encounter " + getPlayerRank(d)
													+ " &r" + d.getName() + " " + baseClass.getType().getTag()));
											this.healthPots(d);
										} else {
											this.giveStats(d, p);
											TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
													+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName() + " "
													+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag()
													+ ChatColor.RED + " was killed by " + ChatColor.WHITE
													+ getPlayerRank(d) + d.getName() + " "
													+ baseClass.getType().getTag());
											this.healthPots(d);
										}
									}
									p.teleport(d);
								} else {
									if (lives == 0) {
										if (killerData != null && killerData.killMsgs == 1) {
											this.giveStats(d, p);
											TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
													+ "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag()
													+ " &cwas not strong enough to encounter " + getPlayerRank(d)
													+ " &r" + d.getName() + " " + baseClass.getType().getTag()));
											this.healthPots(d);
										} else {
											this.giveStats(d, p);
											TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
													+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag() + ChatColor.RED + " was killed by "
													+ ChatColor.WHITE + getPlayerRank(d) + d.getName() + " "
													+ baseClass.getType().getTag());
											this.healthPots(d);
										}
									} else if (lives > 0) {
										if (killerData != null && killerData.killMsgs == 1) {
											this.giveStats(d, p);
											TellAll(instance.getManager().getMain().color("&2&l(!) " + getPlayerRank(p)
													+ "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag()
													+ " &cwas not strong enough to encounter " + getPlayerRank(d)
													+ " &r" + d.getName() + " " + baseClass.getType().getTag()));
											this.healthPots(d);
										} else {
											this.giveStats(d, p);
											TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
													+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag() + ChatColor.RED + " was killed by "
													+ ChatColor.WHITE + getPlayerRank(d) + d.getName() + " "
													+ baseClass.getType().getTag());
											this.healthPots(d);
										}
									}
									p.teleport(d);
								}
							} else {
								if (instance.gameType == GameType.FRENZY) {
									if (lives > 0) {
										BaseClass bc = instance.oldClasses.get(p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ /* baseClass2.getType().getTag() */bc.getType().getTag()
												+ ChatColor.RED + " committed suicide");
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag()
												+ ChatColor.RED + " committed suicide");
									}
									p.teleport(d);
								} else {
									if (lives > 0) {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
									}
									p.teleport(d);
								}
								// }
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
									+ ChatColor.RED + " died");
						}
					} else if (damager instanceof Zombie) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " was killed by a " + ChatColor.YELLOW + "zombie");
					} else if (damager instanceof Skeleton) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " was shot to death by a " + ChatColor.YELLOW + "skeleton");
					} else if (damager instanceof Arrow) {
						Arrow a = (Arrow) damager;
						if (a.getShooter() instanceof Player && a.getShooter() != null) {
							Player d = (Player) a.getShooter();

							if (instance.classes.containsKey(player)) {
								baseClass = instance.classes.get(d);
								PlayerData killerData = instance.getManager().getMain().getDataManager()
										.getPlayerData(d);
								if (d != p || killer != p) {
									if (instance.gameType == GameType.FRENZY) {
										BaseClass bc = instance.oldClasses.get(p);
										if (lives > 0) {

											if (killerData != null && killerData.killMsgs == 1) {
												this.giveStats(d, p);
												TellAll(instance.getManager().getMain()
														.color("&2&l(!) " + getPlayerRank(p) + "&r "
																+ p.getPlayer().getName() + " " + bc.getType().getTag()
																+ " &cwas not strong enough to encounter "
																+ getPlayerRank(d) + " &r" + d.getName() + " "
																+ baseClass.getType().getTag()));
												this.healthPots(d);
											} else {
												this.giveStats(d, p);
												TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
														+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName()
														+ " " + /* baseClass2.getType().getTag() */bc.getType().getTag()
														+ ChatColor.RED + " was killed by " + ChatColor.WHITE
														+ getPlayerRank(d) + d.getName() + " "
														+ baseClass.getType().getTag());
												this.healthPots(d);
											}
										} else {
											if (killerData != null && killerData.killMsgs == 1) {
												this.giveStats(d, p);
												TellAll(instance.getManager().getMain()
														.color("&2&l(!) " + getPlayerRank(p) + "&r "
																+ p.getPlayer().getName() + " " + bc.getType().getTag()
																+ " &cwas not strong enough to encounter "
																+ getPlayerRank(d) + " &r" + d.getName() + " "
																+ baseClass.getType().getTag()));
												this.healthPots(d);
											} else {
												this.giveStats(d, p);
												TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
														+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName()
														+ " "
														+ /* baseClass2.getType().getTag() */baseClass2.getType()
																.getTag()
														+ ChatColor.RED + " was killed by " + ChatColor.WHITE
														+ getPlayerRank(d) + d.getName() + " "
														+ baseClass.getType().getTag());
												this.healthPots(d);
											}
										}
										p.teleport(d);
									} else {
										if (lives == 0) {
											if (killerData != null && killerData.killMsgs == 1) {
												this.giveStats(d, p);
												TellAll(instance.getManager().getMain().color("&2&l(!) "
														+ getPlayerRank(p) + "&r " + p.getPlayer().getName() + " "
														+ baseClass2.getType().getTag()
														+ " &cwas not strong enough to encounter " + getPlayerRank(d)
														+ " &r" + d.getName() + " " + baseClass.getType().getTag()));
												this.healthPots(d);
											} else {
												this.giveStats(d, p);
												TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
														+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName()
														+ " " + baseClass2.getType().getTag() + ChatColor.RED
														+ " was killed by " + ChatColor.WHITE + getPlayerRank(d)
														+ d.getName() + " " + baseClass.getType().getTag());
												this.healthPots(d);
											}
										} else if (lives > 0) {
											if (killerData != null && killerData.killMsgs == 1) {
												this.giveStats(d, p);
												TellAll(instance.getManager().getMain().color("&2&l(!) "
														+ getPlayerRank(p) + "&r " + p.getPlayer().getName() + " "
														+ baseClass2.getType().getTag()
														+ " &cwas not strong enough to encounter " + getPlayerRank(d)
														+ " &r" + d.getName() + " " + baseClass.getType().getTag()));
												this.healthPots(d);
											} else {
												this.giveStats(d, p);
												TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
														+ ChatColor.RESET + getPlayerRank(p) + p.getPlayer().getName()
														+ " " + baseClass2.getType().getTag() + ChatColor.RED
														+ " was killed by " + ChatColor.WHITE + getPlayerRank(d)
														+ d.getName() + " " + baseClass.getType().getTag());
												this.healthPots(d);
											}
										}
										p.teleport(d);
									}
								}
							} else {
								if (instance.gameType == GameType.FRENZY) {
									if (lives > 0) {
										BaseClass bc = instance.oldClasses.get(p);
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ /* baseClass2.getType().getTag() */bc.getType().getTag()
												+ ChatColor.RED + " committed suicide");
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag()
												+ ChatColor.RED + " committed suicide");
									}
									p.teleport(d);
								} else {
									if (lives > 0) {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
									} else {
										TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
												+ getPlayerRank(p) + p.getPlayer().getName() + " "
												+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
									}
									p.teleport(d);
								}
								// }
							}
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
									+ ChatColor.RED + " died");
						}
					} else {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					}
				} else if (killer != null) {
					PlayerData killerData = instance.getManager().getMain().getDataManager().getPlayerData(killer);
					if (killer != p) {
						if (instance.gameType == GameType.FRENZY) {
							BaseClass bc = instance.oldClasses.get(p);
							if (lives > 0) {
								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(killer, p);
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) " + getPlayerRank(p) + "&r " + p.getPlayer().getName() + " "
													+ bc.getType().getTag() + " &cwas not strong enough to encounter "
													+ getPlayerRank(killer) + " &r" + killer.getName() + " "
													+ baseClass.getType().getTag()));
									this.healthPots(killer);
								} else {
									this.giveStats(killer, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ /* baseClass2.getType().getTag() */bc.getType().getTag() + ChatColor.RED
											+ " was killed by " + ChatColor.WHITE + getPlayerRank(killer)
											+ killer.getName() + " " + baseClass.getType().getTag());
									this.healthPots(killer);
								}
							} else {
								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(killer, p);
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) " + getPlayerRank(p) + "&r " + p.getPlayer().getName() + " "
													+ bc.getType().getTag() + " &cwas not strong enough to encounter "
													+ getPlayerRank(killer) + " &r" + killer.getName() + " "
													+ baseClass.getType().getTag()));
									this.healthPots(killer);
								} else {
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag()
											+ ChatColor.RED + " was killed by " + ChatColor.WHITE
											+ getPlayerRank(killer) + killer.getName() + " "
											+ baseClass.getType().getTag());
									this.giveStats(killer, p);
									this.healthPots(killer);
								}
							}
							p.teleport(killer);
						} else {
							if (lives == 0) {
								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(killer, p);
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) " + getPlayerRank(p) + "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag()
													+ " &cwas not strong enough to encounter " + getPlayerRank(killer)
													+ " &r" + killer.getName() + " " + baseClass.getType().getTag()));
									this.healthPots(killer);
								} else {
									this.giveStats(killer, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ baseClass2.getType().getTag() + ChatColor.RED + " was killed by "
											+ ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
											+ baseClass.getType().getTag());
									this.healthPots(killer);
								}
							} else if (lives > 0) {
								if (killerData != null && killerData.killMsgs == 1) {
									this.giveStats(killer, p);
									TellAll(instance.getManager().getMain()
											.color("&2&l(!) " + getPlayerRank(p) + "&r " + p.getPlayer().getName() + " "
													+ baseClass2.getType().getTag()
													+ " &cwas not strong enough to encounter " + getPlayerRank(killer)
													+ " &r" + killer.getName() + " " + baseClass.getType().getTag()));
									this.healthPots(killer);
								} else {
									this.giveStats(killer, p);
									TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ getPlayerRank(p) + p.getPlayer().getName() + " "
											+ baseClass2.getType().getTag() + ChatColor.RED + " was killed by "
											+ ChatColor.WHITE + getPlayerRank(killer) + killer.getName() + " "
											+ baseClass.getType().getTag());
									this.healthPots(killer);
								}
							}
							p.teleport(killer);
						}
					} else {
						if (instance.gameType == GameType.FRENZY) {
							if (lives > 0) {
								BaseClass bc = instance.oldClasses.get(p);
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " "
										+ /* baseClass2.getType().getTag() */bc.getType().getTag() + ChatColor.RED
										+ " committed suicide");
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " "
										+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag()
										+ ChatColor.RED + " committed suicide");
							}
							p.teleport(killer);
						} else {
							if (lives > 0) {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " "
										+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
							} else {
								TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ getPlayerRank(p) + p.getPlayer().getName() + " "
										+ baseClass2.getType().getTag() + ChatColor.RED + " committed suicide");
							}
							p.teleport(killer);
						}
						// }
					}
				} else if (DamageCause.VOID != null) {
					if (instance.gameType == GameType.FRENZY) {
						if (lives > 0) {
							BaseClass bc = instance.oldClasses.get(p);
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " "
									+ /* baseClass2.getType().getTag() */bc.getType().getTag() + ChatColor.RED
									+ " just died SO badly");
						} else {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " "
									+ /* baseClass2.getType().getTag() */baseClass2.getType().getTag() + ChatColor.RED
									+ " just died SO badly");
						}
						p.getPlayer().setFireTicks(0);
					} else {
						if (lives == 0) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
									+ ChatColor.RED + " just died SO badly");
						} else if (lives > 0) {
							TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ getPlayerRank(p) + p.getPlayer().getName() + " " + baseClass2.getType().getTag()
									+ ChatColor.RED + " just died SO badly");
						}
						p.getPlayer().setFireTicks(0);
					}
				} else if (DamageCause.SUICIDE != null) {
					if (instance.gameType == GameType.FRENZY) {
						BaseClass bc = instance.oldClasses.get(player);
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " "
								+ /* baseClass2.getType().getTag() */bc.getType().getTag() + ChatColor.RED
								+ " committed suicide");
						p.getPlayer().setFireTicks(0);
					} else {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " committed suicide");
						p.getPlayer().setFireTicks(0);
					}
				} else if (killer == null && !(player.getKiller() instanceof Player) && DamageCause.LAVA != null
						|| DamageCause.FIRE != null || DamageCause.FIRE_TICK != null) {
					TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + ChatColor.RED + " just burnt to death");
				} else {
					if (lives == 0) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					} else if (lives > 0) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " just died SO badly");
					}
					p.getPlayer().setFireTicks(0);
				}

				baseClass = instance.classes.get(killer);

				if (lives == 0) {
					PlayerData data = instance.getManager().getMain().getDataManager().getPlayerData(p);

					if (data != null) {
						data.losses += 1;
					}
					if (killer != null) {
						String msg = instance.getManager().getMain().color("&4&lELIMINATED &e" + p.getName());
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) killer;
						craft.getHandle().playerConnection.sendPacket(packet);
					}
					p.setDisplayName("" + p.getName() + " " + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC
							+ "Spectator" + ChatColor.RESET);
					Random r = new Random();
					int chance = r.nextInt(100);

					if (chance >= 0 && chance <= 15) {

						if (data != null) {
							data.mysteryChests++;
							player.sendMessage(instance.getManager().getMain()
									.color("&5&l(!) &rYou have found &e1 Mystery Chest!"));
						}
					}

					if (data2.withersk != 3)
						data2.withersk = 0;

					if (instance.gameType == GameType.FRENZY) {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " has been eliminated!");
					} else {
						TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
								+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED
								+ " has been eliminated!");

					}

					if (instance.getMap() != null) {
						PlayerData data3 = instance.getManager().getMain().getDataManager().getPlayerData(p);
						p.sendMessage("" + ChatColor.BOLD + "=====================");
						p.sendMessage("" + ChatColor.BOLD + "||");
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
						baseClass2.totalTokens += tokensEarned;
						p.sendMessage("        " + "       Placed #" + instance.alivePlayers + ": " + tokensEarned
								+ " Tokens");

						if (baseClass2 != null && baseClass2.totalKills >= 0) {
							player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
									+ ChatColor.BOLD + "  " + baseClass2.totalKills + " Kills: " + ChatColor.RESET
									+ ChatColor.YELLOW + (baseClass2.totalKills * 2) + " Tokens");
							data3.tokens += baseClass2.totalKills * 2;
							baseClass2.totalTokens += baseClass2.totalKills;
						}
						if (baseClass2 != null && instance.firstBlood == player) {
							player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE
									+ ChatColor.BOLD + "    First Blood: " + ChatColor.RESET
									+ "10 Tokens");
							data3.tokens += 10;
						}
						if (p.hasPermission("scb.rankBonus")) {
							p.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.BLUE + ChatColor.BOLD
									+ "  RANK BONUS: " + ChatColor.RESET + ChatColor.YELLOW + "10 Tokens");
							data3.tokens += 10;
							baseClass2.totalTokens += 10;
						}
						p.sendMessage("" + ChatColor.BOLD + "||");
						p.sendMessage("" + ChatColor.BOLD + "||");
						p.sendMessage("" + ChatColor.BOLD + "||");
						p.sendMessage("" + ChatColor.BOLD + "=====================");
						p.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have gained " + ChatColor.YELLOW + baseClass2.totalExp + " EXP!");
						p.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have earned " + ChatColor.YELLOW + baseClass2.totalTokens + " Tokens!");

						if (data3.exp >= 2500) {
							data3.level++;
							data3.exp -= 2500;
							p.sendMessage(instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
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
								TellAll(instance.getManager().getMain()
										.color("&2&l(!) &c&lRed Team &r has been eliminated!"));

								for (Player losers : instance.redTeam) {
									BaseClass loserBc = instance.classes.get(losers);
									PlayerData data3 = instance.getManager().getMain().getDataManager()
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
												instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
										losers.sendMessage("You are now Level: " + data3.level + "!");
									}

									losers.sendMessage(instance.getManager().getMain()
											.color("&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens"));
								}
							}
						} else if (instance.team.get(p).equals("Blue")) {
							if (!(aliveTeam.contains("Blue"))) {
								TellAll(instance.getManager().getMain()
										.color("&2&l(!) &b&lBlue Team &r has been eliminated!"));

								for (Player losers : instance.blueTeam) {
									BaseClass loserBc = instance.classes.get(losers);
									PlayerData data3 = instance.getManager().getMain().getDataManager()
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
												instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
										losers.sendMessage("You are now Level: " + data3.level + "!");
									}
									losers.sendMessage(instance.getManager().getMain()
											.color("&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens"));
								}
							}
						} else if (instance.team.get(p).equals("Black")) {
							if (!(aliveTeam.contains("Black"))) {
								TellAll(instance.getManager().getMain()
										.color("&2&l(!) &0&lBlack Team &r has been eliminated!"));

								for (Player losers : instance.blackTeam) {
									BaseClass loserBc = instance.classes.get(losers);
									PlayerData data3 = instance.getManager().getMain().getDataManager()
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
												instance.getManager().getMain().color("&e&lLEVEL UPGRADED!"));
										losers.sendMessage("You are now Level: " + data3.level + "!");
									}
									losers.sendMessage(instance.getManager().getMain()
											.color("&2&l(!) &rYou have gained &e" + loserBc.totalTokens + " Tokens"));
								}
							}
						}
						instance.aliveTeams--;
					}

				} else if (lives == 1) {
					if (killer != null) {
						String msg = instance.getManager().getMain().color("&4&lKILLED &e" + p.getName());
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) killer;
						craft.getHandle().playerConnection.sendPacket(packet);
					}
					TellAll(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
							+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED + " has "
							+ lives + " life left");

				} else {
					if (killer != null) {
						String msg = instance.getManager().getMain().color("&4&lKILLED &e" + p.getName());
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) killer;
						craft.getHandle().playerConnection.sendPacket(packet);
					}
					TellAll(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET + getPlayerRank(p)
							+ p.getPlayer().getName() + " " + baseClass2.getType().getTag() + ChatColor.RED + " has "
							+ lives + " lives left");
				}
				/*
				 * if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				 * EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent)
				 * p.getLastDamageCause(); Entity damager = entityDamageEvent.getDamager();
				 * 
				 * if (damager instanceof Player) { Player d = (Player) damager; PlayerData data
				 * = instance.getManager().getMain().getDataManager().getPlayerData(d);
				 * 
				 * if (instance.classes.containsKey(d)) { BaseClass baseClass3 =
				 * instance.classes.get(d); // data.tokens += 1; data.kills += 1; data.exp +=
				 * 29; baseClass3.totalExp += 29;
				 * 
				 * if (instance.getManager().getMain().tournament == true) data.points++;
				 * 
				 * d.playSound(d.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
				 * 
				 * if (baseClass3 != null) { baseClass3.totalTokens += 1;
				 * baseClass3.totalKills++; baseClass3.eachLifeKills++; } } } else if (damager
				 * instanceof Arrow) { Arrow a = (Arrow) damager;
				 * 
				 * if (a.getShooter() instanceof Player) { Player shooter = (Player)
				 * a.getShooter(); PlayerData data =
				 * instance.getManager().getMain().getDataManager().getPlayerData(shooter);
				 * 
				 * if (instance.classes.containsKey(shooter)) { BaseClass baseClass3 =
				 * instance.classes.get(shooter); // data.tokens += 1; data.kills += 1; data.exp
				 * += 29;
				 * 
				 * if (instance.getManager().getMain().tournament == true) data.points++;
				 * 
				 * shooter.playSound(shooter.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
				 * 
				 * if (baseClass3 != null) { baseClass3.totalTokens += 1;
				 * baseClass3.totalKills++; baseClass3.eachLifeKills++; baseClass3.totalExp +=
				 * 29; } } } } else if (killer != null) { PlayerData data =
				 * instance.getManager().getMain().getDataManager().getPlayerData(killer);
				 * 
				 * if (instance.classes.containsKey(killer)) { BaseClass baseClass3 =
				 * instance.classes.get(killer); // data.tokens += 1; data.kills += 1; data.exp
				 * += 29;
				 * 
				 * if (instance.getManager().getMain().tournament == true) data.points++;
				 * 
				 * killer.playSound(killer.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);
				 * 
				 * if (baseClass3 != null) { baseClass3.totalTokens += 1;
				 * baseClass3.totalKills++; baseClass3.eachLifeKills++; baseClass3.totalExp +=
				 * 29; } } } } else { p.teleport(instance.GetSpecLoc()); }
				 */

				if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p.getLastDamageCause();
					Entity damager = entityDamageEvent.getDamager();

					if (damager instanceof Player) {
						Player d = (Player) damager;

						if (instance.classes.containsKey(d)) {
							baseClass = instance.classes.get(d);
							this.classesEvent(d, baseClass);
						}
					} else {
						if (killer != null) {
							if (instance.classes.containsKey(killer)) {
								baseClass = instance.classes.get(killer);
								this.classesEvent(killer, baseClass);
							}
						}
					}
				} else if (killer != null) {
					if (instance.classes.containsKey(killer)) {
						baseClass = instance.classes.get(killer);
						this.classesEvent(killer, baseClass);
					}
				}
			}
		}

	}
	
	private void healthPots(Player d) {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
				String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + "Health Pot");
		Potion pot = new Potion(1);
		pot.setType(PotionType.INSTANT_HEAL);
		pot.setSplash(true);
		pot.apply(item);
		d.getInventory().addItem(item);
		d.sendMessage(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.YELLOW
				+ "You got a kill and got rewarded a " + ChatColor.YELLOW + ChatColor.BOLD + "Health Pot");
	}

	// Gives the killer kills for stats, points for tourney, etc
	private void giveStats(Player d, Player p) {
		if (d != null) {
			if (instance.classes.containsKey(d)) {
				BaseClass baseClass3 = instance.classes.get(d);
				// For first blood:
				if (instance.firstBlood == null) {
					instance.firstBlood = d;
					TellAll("");
					TellAll(instance.getManager().getMain().color("&2&l(!) &r" + getPlayerRank(d) + d.getName() + " "
							+ baseClass3.getType().getTag() + " &edrew first blood!"));
					TellAll("");
					baseClass3.totalTokens += 10;
				}
				PlayerData data = instance.getManager().getMain().getDataManager().getPlayerData(d);

				// data.tokens += 1;
				data.kills += 1;
				data.exp += 29;
				baseClass3.totalExp += 29;

				if (instance.getManager().getMain().tournament)
					data.points++;

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

	// Classes such as Sheep & Hunter that when they get a kill, they one of their
	// abilities back
	private void classesEvent(Player d, BaseClass baseClass) {
		if (instance.classes.containsKey(d)) {
			baseClass = instance.classes.get(d);
			if (baseClass.getType() == ClassType.Sheep && baseClass.getLives() > 0) {
				d.getInventory().addItem(new ItemStack(Material.ENCHANTMENT_TABLE));
				d.sendMessage(instance.getManager().getMain()
						.color("&r&l(!) &rYou got a kill and now you can switch your wool color if you'd like!"));
			} else if (baseClass.getType() == ClassType.Hunter) {
				if (!(d.getInventory().contains(Material.FEATHER))) {
					d.sendMessage(instance.getManager().getMain()
							.color("&r&l(!) &rYour &r&lDash &rhas been regenerated for getting a kill!"));
					d.getInventory()
							.addItem(ItemHelper.setDetails(new ItemStack(Material.FEATHER),
									instance.getManager().getMain().color("&b&lDash"),
									instance.getManager().getMain().color("&7A quick escape or attack")));
				}
			} else if (baseClass.getType() == ClassType.Present) {
				d.sendMessage(instance.getManager().getMain().color(
						"&r&l(!) &rYour &r&lAggressive Gift has regenerated and you can get a new weapon if you'd like!"));
				d.getInventory()
						.addItem(ItemHelper.setDetails(new ItemStack(Material.CHEST, 1),
								String.valueOf(ChatColor.RESET) + ChatColor.ITALIC + "Agressive Gift", "",
								String.valueOf(ChatColor.RESET) + ChatColor.YELLOW + "Steals another player's main item"));
			} else if (baseClass.getType() == ClassType.ButterGolem) {
				ItemStack item = ItemHelper.setDetails(new ItemStack(Material.GOLD_BLOCK, 1),
						ChatColor.GREEN + "Butter Balls",
						ChatColor.YELLOW + "Right click to throw DEADLY butter balls!");
				d.sendMessage(instance.getManager().getMain()
						.color("&2&l(!) &rYou got a kill and gained an extra &2Butter Ball"));
				d.getInventory().addItem(item);
			}
		}
	}

	public void TellAll(String msg) {
		for (Player player : instance.players)
			player.sendMessage(msg);

		for (Player spectator : instance.spectators)
			spectator.sendMessage(msg);
	}
}
