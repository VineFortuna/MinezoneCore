package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameManager;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Timer;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticManager;
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
import org.bukkit.event.EventHandler;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
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
	public Timer snowballLauncher = new Timer();

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
		player.setMaxHealth(instance.getMaxHealthPoints()); // Max Health modifier
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
		else return gameInstance.classes.get(player) != null && gameInstance.classes.get(player).getLives() <= 0;
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

	@SuppressWarnings("deprecation")
	private void checkBountyKill(BaseClass kClass, Player playerKilled, Player killer) {
		PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
		if (data != null && instance.savesStats()) {
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

	private void handleElimination(Player p, Player killer, BaseClass baseClassDead, PlayerData data) {
		if (data != null && instance.savesStats()) {
			data.losses += 1;
			data.winstreak = 0;
			instance.getGameManager().getMain().giveBattlePassXp(p,
					anthony.SuperCraftBrawl.battlepass.BattlePassSeason.XP_GAME_PLAYED);

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

			if (data != null && instance.savesStats()) {
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
			baseClassDead.placement = instance.alivePlayers;

			if (!instance.savesStats()) {
				p.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Placed #" + instance.alivePlayers);
				p.sendMessage(ChatColor.BOLD + "||");
				p.sendMessage(ChatColor.BOLD + "=====================");
				p.sendMessage(instance.color("&2&l(!) &rPrivate games don't give Tokens, EXP or stats."));
				return;
			}

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
			int rankBonus = instance.getGameManager().getMain().getRankBonusTokens(p, "scb.rankBonus");
			if (rankBonus > 0) {
				p.sendMessage(ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Rank Bonus: "
						+ ChatColor.GREEN + rankBonus + " Tokens");
				data.tokens += rankBonus;
				baseClassDead.totalTokens += rankBonus;
			}
			p.sendMessage(ChatColor.BOLD + "||");
			p.sendMessage(ChatColor.BOLD + "=====================");
			p.sendMessage(instance.color("&2&l(!) &rYou earned &a" + baseClassDead.totalTokens +
					" &rTokens and &a" + baseClassDead.totalExp + " &rEXP!"));

			instance.getGameManager().getMain().getLevelManager().checkLevelUp(p);
		}
	}

	@EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (player.getName() != null && lives > 0) {
            decreaseLives();

            Player killer = player.getKiller();
            Player p = player.getPlayer();

            // Remove mobs spawned by the player
            removeMobs(p);
            resetMobTarget(p);

            if (isDead) {
				PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(p);

				if (instance.savesStats())
					data.deaths += 1;

				for (PotionEffect type : p.getActivePotionEffects())
					p.removePotionEffect(type.getType());

				p.setFireTicks(0);
				p.getInventory().clear();

				LivingEntity creature = null;

				EntityDamageEvent lastDamage = p.getLastDamageCause();

				if (lastDamage instanceof EntityDamageByEntityEvent) {
					Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();

					if (damager instanceof Player && killer == null) {
						killer = (Player) damager;
					} else if (damager instanceof LivingEntity && damager.getCustomName() != null) {
						creature = (LivingEntity) damager;

						Player mobOwner = GameManager.getMobOwner(creature);

						if (mobOwner != null && instance.classes.containsKey(mobOwner)) {
							killer = mobOwner;
						}
					} else if (damager instanceof Projectile) {
						Projectile projectile = (Projectile) damager;

						if (projectile.getShooter() instanceof Player) {
                            killer = (Player) projectile.getShooter();

						} else if (projectile.getShooter() instanceof LivingEntity && ((LivingEntity) projectile.getShooter()).getCustomName() != null) {
							creature = (LivingEntity) projectile.getShooter();
							Player mobOwner = GameManager.getMobOwner(creature);

							if (mobOwner != null && instance.classes.containsKey(mobOwner)) {
								killer = mobOwner;
							}
						}
					}
				}

				BaseClass baseClassDead = instance.classes.get(p);

				baseClassDead.totalDeaths++;
				baseClassDead.eachLifeKills = 0;

				if (killer != null && !killer.equals(player)) {
					giveStats(killer, player);
				}

				// Death message
				DeathMessageProvider deathMessage = new DeathMessageProvider();
				PlayerData killerData = instance.getGameManager().getMain().getDataManager().getPlayerData(killer);
				// check if killer has custom kill messages enabled
				// if there is no killer, use victim's settings
				boolean customEnabled = killer != null ? killerData.killMsgs == 1 : data.killMsgs == 1;

				tellAll(instance.color(deathMessage.getDeathMessage(player, killer, creature, lastDamage, customEnabled)));

				// DEATH PARTICLES
				deathParticles(data, p);

				if (killer != null && !killer.equals(player)) {
					giveHealthPot(killer);
					p.teleport(killer);
				}

				// PLAYER ELIMINATED
				if (lives == 0) {
					handleElimination(p, killer, baseClassDead, data);
				}

				// Send actionbar to killer
				if (killer != null) {
					String msg = instance.getGameManager().getMain().color("&4&lKILLED &e" + p.getName());
					PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
							(byte) 2);
					CraftPlayer craft = (CraftPlayer) killer;
					craft.getHandle().playerConnection.sendPacket(packet);
				}

				if (lives > 0) {
					tellAll(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ p.getPlayer().getDisplayName() + ChatColor.RED + " has " + lives +
							(lives == 1 ? " life " : " lives ") + "left");
				}

				handleKillEvent(p, killer);
            }
        }
    }

	private void handleKillEvent(Player p, Player killer) {
		if (p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent entityDamageEvent =
					(EntityDamageByEntityEvent) p.getLastDamageCause();

			Entity damager = entityDamageEvent.getDamager();

			if (damager instanceof Player) {
				Player d = (Player) damager;

				if (instance.classes.containsKey(d)) {
					instance.classes.get(d).killEvent(d);
				}
			} else if (killer != null) {
				if (instance.classes.containsKey(killer)) {
					instance.classes.get(killer).killEvent(killer);
				}
			}
		} else if (killer != null) {
			if (instance.classes.containsKey(killer)) {
				instance.classes.get(killer).killEvent(killer);
			}
		}

		p.setLastDamageCause(null);
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

	private Material checkDeathEffect(PlayerData data, Player p) {
		CosmeticManager cosmeticManager = instance.getGameManager().getMain().getCosmeticManager();
		Cosmetic cosmetic = cosmeticManager.getEquipped(p, CosmeticCategory.DEATH_EFFECT);

		if (data != null && cosmetic != null && p.hasPermission("scb.deathParticles")) {
			return cosmetic.getIcon(p).getType();
		}
		return Material.INK_SACK;
	}

	private void deathParticles(PlayerData data, Player p) {
		Location pLocation = p.getLocation();
		List<Item> deathParticles = new ArrayList<>();

		Material mat = checkDeathEffect(data, p);

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

	// Giving health potions on kill
	protected void giveHealthPot(Player killer) {
		GameSettings.HealthPotMode potMode = instance.getGameSettings().healthPotMode;
		if (potMode == GameSettings.HealthPotMode.DISABLED)
			return;

		Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(), new Runnable() {
			@Override
			public void run() {
				if (checkIfDead(killer, instance)) return;
				if (instance.alivePlayers == 1) return;

				// Instant Full Heal: no potion, straight back to max health
				if (potMode == GameSettings.HealthPotMode.FULL_HEAL) {
					killer.setHealth(killer.getMaxHealth());
					killer.sendMessage(String.valueOf(ChatColor.DARK_GREEN) + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ ChatColor.YELLOW + "You got a kill and were " + ChatColor.YELLOW + ChatColor.BOLD + "fully healed");
					return;
				}

				if (instance.classes.get(killer).getType() == ClassType.Horse) return;

				// Half Health: only when at or below half health. Always: every kill
				if (potMode == GameSettings.HealthPotMode.HALF_HEALTH && killer.getHealth() / killer.getMaxHealth() > 0.5)
					return;

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
					if (instance.getGameManager().getMain().tournament && instance.savesStats()) {
						if (instance.gameType != GameType.DUEL) {
							data.points += 2;
							instance.getGameManager().getMain().tourney.put(killer.getName(), data.points);
						}
					}
				}

				if (data != null) {
					if (instance.savesStats()) {
						data.kills += 1;
						data.exp += 29;
						baseClass.totalExp += 29;
						instance.getGameManager().getMain().giveBattlePassXp(killer,
								anthony.SuperCraftBrawl.battlepass.BattlePassSeason.XP_KILL);
						if (instance.getGameManager().getMain().tournament) {
							if (instance.gameType != GameType.DUEL) {
								data.points++;
								instance.getGameManager().getMain().tourney.put(killer.getName(), data.points);
							}
						}
					}

					killer.playSound(killer.getLocation(), Sound.SUCCESSFUL_HIT, 2, 1);

					if (baseClass != null) {
						baseClass.totalTokens += 1;
						baseClass.totalKills++;
						baseClass.eachLifeKills++;

						int strengthSeconds = instance.getGameSettings().strengthOnKillSeconds;
						if (strengthSeconds > 0) {
							killer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, strengthSeconds * 20, 0), true);
							killer.sendMessage(instance.color("&2&l(!) &rYou gained &cStrength I &rfor &e" + strengthSeconds + " seconds&r."));
						}

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
		player.getEquipment().setHelmet(playerHead);
	}
}