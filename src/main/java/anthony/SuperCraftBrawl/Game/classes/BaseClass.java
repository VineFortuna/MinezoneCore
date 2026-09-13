package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.all.SpiderClass;
import anthony.SuperCraftBrawl.PlayerListener;
import anthony.SuperCraftBrawl.Timer;
import anthony.SuperCraftBrawl.gui.ClassMasteryGUI;
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
import java.util.Random;

public abstract class BaseClass {

	protected final GameInstance instance;
	protected final Player player;
	public int lives = 5;
	public boolean isDead = false;
	public boolean fadeAbilityActive = false;
	public int tokens = 0;
	public int totalTokens = 0;
	public int totalKills = 0;
	public int totalDeaths = 0;
	public int placement = 0;
	public int eachLifeKills = 0;
	public int totalExp = 0;
    public Score score;

	public double baseVerticalJump = 1.0;

	public Timer bazooka = new Timer();

	public boolean flintUsed = false;
	public Timer pearlTimer = new Timer();
	public Timer tnt = new Timer();
	public Timer slimeBall = new Timer();
	public Timer tntItem = new Timer();
	public Timer ice = new Timer();
	public Timer golem = new Timer();
	public Timer herobrine = new Timer();
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
	public Timer freddyCooldown = new Timer();
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

	public void setNameTag(){}

	public abstract void setItems(Inventory playerInv);

	public void useItem(PlayerInteractEvent event){}

	public void onConsumingItem(PlayerItemConsumeEvent event) {}

	public void takeDamage(EntityDamageEvent event) {}

	public void projectileLaunch(ProjectileLaunchEvent event) {}

	public void projectileHit(ProjectileHitEvent event) {}

	public void potionSplashEvent(PotionSplashEvent event) {}

	public void doDamage(EntityDamageByEntityEvent event) {}

	public void doDamage2(EntityDamageByEntityEvent event) {}

	public void onEntityTarget(EntityTargetLivingEntityEvent event) {}

	public void onPlayerMove(PlayerMoveEvent event) {}

	public void onFish(PlayerFishEvent event) {}

	public void tick(int gameTicks) {}

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
		setItems(inv);
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
			return ClassMasteryGUI.headReward(this.getType());
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
				} else if (data2.pumpkinPie == 1) {
					mat = Material.PUMPKIN_PIE;
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
			if (baseClass.getType() == ClassType.EnderDragon) {
				if (killer != null) {
					Location pLoc = p.getLocation();
					EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc, EntityType.ENDER_CRYSTAL);
					HealTask task = new HealTask(killer, crystal, instance);
					BukkitTask bukkit = Bukkit.getScheduler()
							.runTaskTimer(instance.getGameManager().getMain(), task, 0L, 2L);
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

    /*
    * This function decreases lives on death and sets it
    * on the scoreboard too
     */
    private void decreaseLives() {
        if (instance.gameType != GameType.GUNGAME) {
            lives--;
        }
        score.setScore(lives);
    }

    private void giveGameStats(Player p, BaseClass pBc, PlayerData data) {
        if (data != null && pBc != null) {
            data.deaths++;
            pBc.totalDeaths++;
            pBc.eachLifeKills = 0;
        }
    }

    public void Death2(PlayerDeathEvent e) {
        if (player.getName() != null && lives > 0) {
            Player p = player.getPlayer();
            Player killer = player.getKiller();
			Location deathLocation = p.getLocation().clone();
            Core core =  instance.getGameManager().getMain();
            PlayerListener listener = core.getListener();
            PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(p);

            decreaseLives();
            removeMobs(p); //Removes mobs spawned by the player
            resetMobTarget(p);
            listener.resetPotionEffects(p);

            if (data != null && isDead) { //Checks if player is dead, then run through rest of function
                BaseClass killerBc = instance.classes.get(killer);
                BaseClass pBc = instance.classes.get(p);
                BaseClass baseClass = this;

                giveGameStats(p, pBc, data);
                deathParticles(data, p);
                p.setFireTicks(0);
                p.getInventory().clear();
            }
        }
    }

    /*
     * This function checks if the death
     * was caused due to void
     */
    private void voidDeath(PlayerDeathEvent e, Player p) {
        if (p.getLocation().getY() <= 50) { //If they're below Y = 50, die
            if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p
                        .getLastDamageCause();
                Entity damager = entityDamageEvent.getDamager();

                if (damager instanceof Player) {
                    Player d = (Player) damager;
                }
            }
        }
    }

	private void elimination() {

	}

    public void Death(PlayerDeathEvent e) {
        if (player.getName() != null && lives > 0) {
            decreaseLives();

            Player killer = player.getKiller();
            Player p = player.getPlayer();
			Location deathLocation = p.getLocation().clone();

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

				Creature creature = null;

				EntityDamageEvent lastDamage = p.getLastDamageCause();

				if (lastDamage instanceof EntityDamageByEntityEvent) {
					Entity damager;

					damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();

					if (damager instanceof Creature && damager.getCustomName() != null) {
						creature = (Creature) damager;

						String owner = ChatColor.stripColor(
								creature.getCustomName().substring(0, creature.getCustomName().indexOf("'")));
						Player mobOwner = Bukkit.getPlayer(owner);

						if (mobOwner != null && instance.classes.containsKey(mobOwner)) {
							killer = mobOwner;
						}
					}

					if (damager instanceof Projectile) {
						Projectile projectile = (Projectile) damager;

						if (projectile.getShooter() instanceof Creature && ((Creature) projectile.getShooter()).getCustomName() != null) {
							creature = (Creature) projectile.getShooter();

							String owner = ChatColor.stripColor(
									creature.getCustomName().substring(0, creature.getCustomName().indexOf("'")));
							Player mobOwner = Bukkit.getPlayer(owner);

							if (mobOwner != null && instance.classes.containsKey(mobOwner)) {
								killer = mobOwner;
							}
						}
					}
				}

                BaseClass baseClassKiller = instance.classes.get(killer);
                BaseClass baseClassDead = instance.classes.get(p);

                baseClassDead.totalDeaths++;
                baseClassDead.eachLifeKills = 0;

                BaseClass baseClass = this;

				// Death message
				DeathMessageProvider deathMessage = new DeathMessageProvider();
				PlayerData killerData = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
				boolean customEnabled = killer != null ? killerData.killMsgs == 1 : data.killMsgs == 1;

				tellAll(instance.color(deathMessage.getDeathMessage(player, killer, creature, customEnabled)));

				if (killer != null) {
					giveStats(killer, player);
					giveHealthPot(killer);
					p.teleport(killer);
				}

				// DEATH PARTICLES
                deathParticles(data, p);

				if (lives == 0) {
					if (data != null) {
						data.losses += 1;
						data.winstreak = 0;

						ClassType type = baseClassDead.getType();
						ClassDetails details = data.playerClasses.get(type.getID());
						if (details == null) {
							details = new ClassDetails();
							data.playerClasses.put(type.getID(), details);
						}
						details.playGame();
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
					int chance = r.nextInt(1000);

					if (chance >= 0 && chance <= 1) {

						if (data != null) {
							data.mysteryChests++;
							player.sendMessage(instance.getGameManager().getMain()
									.color("&5&l(!) &rYou have found &e1 MysteryChest!"));
						}
					}

					tellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getName() + " " + baseClassDead.getType().getTag() + ChatColor.RED
							+ " has been eliminated!");

					if (instance.getMap() != null) {
						p.sendMessage(ChatColor.BOLD + "=====================");
						p.sendMessage(ChatColor.BOLD + "||");
						p.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RED + ChatColor.BOLD
								+ "  GAME LOST");
						p.sendMessage(ChatColor.BOLD + "||");

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

						data.tokens += tokensEarned;
						baseClassDead.totalTokens += tokensEarned;
						p.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Placed #"
								+ instance.alivePlayers + ": " + ChatColor.GREEN + tokensEarned + " Tokens");
						baseClassDead.placement = instance.alivePlayers;

						if (baseClassDead.totalKills >= 0) {
							player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  "
									+ baseClassDead.totalKills + " Kills: " + ChatColor.GREEN
									+ (baseClassDead.totalKills * 2) + " Tokens");
							data.tokens += baseClassDead.totalKills * 2;
							baseClassDead.totalTokens += baseClassDead.totalKills;
						}
						if (instance.firstBlood == player) {
							player.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RESET
									+ "  First Blood: " + ChatColor.GREEN + "10 Tokens");
							data.tokens += 10;
						}
						if (p.hasPermission("scb.rankBonus")) {
							p.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Rank Bonus: "
									+ ChatColor.GREEN + "10 Tokens");
							data.tokens += 10;
							baseClassDead.totalTokens += 10;
						}
						p.sendMessage(ChatColor.BOLD + "||");
						p.sendMessage(ChatColor.BOLD + "=====================");
						p.sendMessage(instance.color("&2&l(!) &rYou earned &a" + baseClassDead.totalTokens +
								" &rTokens and &a" + baseClassDead.totalExp + " &rEXP!"));

						instance.getGameManager().getMain().getLevelManager().checkLevelUp(p);
					}
				}

				if (killer != null) {
					String msg = instance.getGameManager().getMain().color("&4&lKILLED &e" + p.getName());
					PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
							(byte) 2);
					CraftPlayer craft = (CraftPlayer) killer;
					craft.getHandle().playerConnection.sendPacket(packet);
				}

				tellAll(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ p.getPlayer().getDisplayName() + ChatColor.RED + " has " + lives + " live" + (lives == 1 ? "" : "s") + " left");

                // END CRYSTAL
                if (baseClassKiller != null) {
					// END CRYSTAL
					spawnEnderDragonCrystal(baseClassKiller, killer, deathLocation);
                }

                if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) p.getLastDamageCause();
                    Entity damager = entityDamageEvent.getDamager();

                    if (damager instanceof Player) {
                        Player d = (Player) damager;

                        if (instance.classes.containsKey(d)) {
                            baseClassKiller = instance.classes.get(d);
                            baseClassKiller.killEvent(d);
                        }
                    } else {
                        if (killer != null) {
                            if (instance.classes.containsKey(killer)) {
                                baseClassKiller = instance.classes.get(killer);
                                baseClassKiller.killEvent(killer);
                            }
                        }
                    }
                } else if (killer != null) {
                    if (instance.classes.containsKey(killer)) {
                        baseClassKiller = instance.classes.get(killer);
                        baseClassKiller.killEvent(killer);
                    }
                }
                p.setLastDamageCause(null);
            }
        }
    }

	private void spawnEnderDragonCrystal(BaseClass dragonClass, Player killer, Location deathLocation) {
		if (dragonClass == null || killer == null || deathLocation == null || deathLocation.getWorld() == null) {
			return;
		}

		if (dragonClass.getType() != ClassType.EnderDragon) {
			return;
		}

		Location crystalLocation = deathLocation.clone();

		EnderCrystal crystal = (EnderCrystal) crystalLocation.getWorld().spawnEntity(
				crystalLocation,
				EntityType.ENDER_CRYSTAL
		);

		HealTask task = new HealTask(killer, crystal, instance);
		BukkitTask bukkit = Bukkit.getScheduler()
				.runTaskTimer(instance.getGameManager().getMain(), task, 0L, 2L);
		task.set(bukkit);
	}

	// Removes any mobs that the player who died has spawned
	private void removeMobs(Player p) {
		for (Entity en : p.getWorld().getEntities())
			if (!(en instanceof Player))
				if (en.getName().contains(p.getName()))
					en.remove();
	}

	// Resets target for any mob targeting the player who died
	private void resetMobTarget(Player p) {
		for (Creature en : p.getWorld().getEntitiesByClass(Creature.class)) {
			if (en.getTarget() != null && en.getTarget() == p) {
				if (instance.getGameManager().getMobOwner(en) != null)
					en.setTarget(instance.getNearestPlayer(instance.getGameManager().getMobOwner(en), en, 150));
			}
		}
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
			else if (pData.pumpkinPie == 1)
				mat = Material.PUMPKIN_PIE;
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
			if (pClass.getType() == ClassType.EnderDragon) {
				if (killer != null) {
					Location pLoc = p.getLocation();
					EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc, EntityType.ENDER_CRYSTAL);
					HealTask task = new HealTask(killer, crystal, instance);
					BukkitTask bukkit = Bukkit.getScheduler()
							.runTaskTimer(instance.getGameManager().getMain(), task, 0L, 2L);
					task.set(bukkit);
				}
			}
		}
	}

	// Giving health potions on kill
	protected void giveHealthPot(Player killer) {
		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new Runnable() {
			@Override
			public void run() {
				if (checkIfDead(killer, instance) || instance.classes.get(killer).getType() == ClassType.Horse)
					return;

				if (instance.alivePlayers == 1) return;

				if (killer.getHealth() / killer.getMaxHealth() >= 0.5) return;

				ItemStack item = ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
						String.valueOf(ChatColor.RED) + ChatColor.BOLD + "HEALING I");

				Potion pot = new Potion(1);
				pot.setType(PotionType.INSTANT_HEAL);
				pot.setSplash(true);
				pot.apply(item);

				killer.getInventory().addItem(item);

				Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new Runnable() {
					@Override
					public void run() {
						killer.sendMessage(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ ChatColor.YELLOW + "You got a kill and got rewarded a " + ChatColor.YELLOW + ChatColor.BOLD
								+ "Health Pot");
					}
				}, 1L);
			}
		}, 1L);
	}

	// Gives the killer kills for stats, points for tourney, etc
	private void giveStats(Player killer, Player victim) {
		if (killer != null && instance.classes.containsKey(killer)) {
				PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
				BaseClass baseClass = instance.classes.get(killer);

				// For first blood:
				if (instance.firstBlood == null) {
					instance.firstBlood = killer;
					tellAll("");
					tellAll(instance.color("&2&l(!) &r" + killer.getName() + " &adrew first blood!"));
					tellAll("");
					baseClass.totalTokens += 10;
					if (instance.getGameManager().getMain().tournament) {
						if (instance.gameType != GameType.DUEL) {
							data.points += 2;
							instance.getGameManager().getMain().tourney.put(killer.getName(), data.points);
						}
					}
				}

				if (data != null) {
					data.kills += 1;
					data.exp += 29;
					baseClass.totalExp += 29;
					if (instance.getGameManager().getMain().tournament) {
						if (instance.gameType != GameType.DUEL) {
							data.points++;
							instance.getGameManager().getMain().tourney.put(killer.getName(), data.points);
						}
					}

					killer.playSound(killer.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);

					if (baseClass != null) {
						baseClass.totalTokens += 1;
						baseClass.totalKills++;
						baseClass.eachLifeKills++;
						this.checkBountyKill(baseClass, victim, killer);
					}
				}
			}

	}

	// Classes such as Sheep & Hunter for example that when they get a kill,
	// they one of their abilities back
	public void killEvent(Player damagerPlayer) {}

	public void tellAll(String msg) {
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
		if (this.getType() == ClassType.Spider) {
			SpiderClass spiderClass = (SpiderClass) this;
			if (spiderClass.invisTaskId != -1) {
				ItemStack invisHelmet = playerHead.clone();
				invisHelmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 8);
				player.getEquipment().setHelmet(invisHelmet);
				return;
			}
		}
		if (this.getType() == ClassType.Fade) {
			if (fadeAbilityActive) {
				player.getEquipment().setHelmet(ItemHelper.create(Material.AIR));
				return;
			}
		}

		player.getEquipment().setHelmet(playerHead);
	}
}
