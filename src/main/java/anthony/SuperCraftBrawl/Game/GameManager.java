package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.map.DuosMaps;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileManager;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;
import java.util.Map.Entry;

public class GameManager implements Listener, PluginMessageListener {
	public HashMap<Maps, GameInstance> gameMap;
	public HashMap<DuosMaps, GameInstance> gameMap2;
	public HashMap<Player, BukkitRunnable> spawnProt;
	public static HashMap<String, Integer> playercount;

	private final ProjectileManager projManager;
	private final Core main;
	public GameState state;
	public GameType gameType;

	private static final int SPAWN_PROT_DURATION = 5;

	public GameManager(Core main) {
		this.main = main;
		this.playercount = new HashMap<>();
		this.gameMap = new HashMap<>();
		this.gameMap2 = new HashMap<>();
		this.spawnProt = new HashMap<>();
		this.main.getServer().getPluginManager().registerEvents(this, main);
		this.projManager = new ProjectileManager(this);
	}

	// GETTERS:

	public ProjectileManager getProjManager() {
		return projManager;
	}

    /*
    * This function removes a player from game settings votes.
    * Function is called in onQuit in Core.java
     */
    public void removePlayerFromVotes(Player player) {
        GameInstance game = GetInstanceOfPlayer(player);

        if (game != null && game.getGameSettings() != null) {
            game.getGameSettings().removeFromStartVotes(player);
            game.getGameSettings().removeFromGameTypeVotes(player);
            game.getGameSettings().removeFromLightningVotes(player);
            game.getGameSettings().removeFromTimeVotes(player);
        }
    }

    public boolean checkIfFull(Player player, GameInstance game, GameType type) {
        if (type == GameType.DUEL && game.players.size() == 2) {
            return true;
        } else if (type == GameType.CLASSIC && game.players.size() == 6) {
            if (!player.hasPermission("scb.bypassFull")) {
                return true;
            }
        }

        return false;
    }

    /*
    * This function lists active games that are in waiting/lobby state
     */
    public GameInstance getLobbyActiveGames(Player player, GameType type) {
        for (Map.Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
            GameInstance gi = entry.getValue();
            if (gi.state == GameState.WAITING && gi.gameType == type && !checkIfFull(player, gi, type)) {
                return gi; //Found a game
            }
        }

        List<Maps> candidates = new ArrayList<>();
        for (Maps m : Maps.values()) {
            if (m.getGamemode() == type && !gameMap.containsKey(m)) {
                candidates.add(m);
            }
        }

        if (candidates.isEmpty())
            return null;

        Maps map = candidates.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(candidates.size()));
        GameInstance newGame = new GameInstance(this, map);
        gameMap.put(map, newGame);
        return newGame;
    }

    public void shootProjectile(GameInstance instance, Player player, double dmg, Material mat, Sound s, int volume, Effect e, int pitch) {
        ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
            @Override
            public void onHit(Player hit) {
                if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                    Location hitLoc = this.getBaseProj().getEntity().getLocation();

                    for (Player gamePlayer : this.getNearby(3.0)) {
                        EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer, DamageCause.VOID, dmg);
                        instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
                        gamePlayer.damage(dmg, player);
                    }
                   for (Player gamePlayer : instance.players) {
                        gamePlayer.playSound(hitLoc, s, volume, pitch);
                        gamePlayer.playEffect(hitLoc, e, 1);
                    }
                }

            }

        }, new ItemStack(mat));
        instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                player.getLocation().getDirection().multiply(2.0D));
    }

    // EVENTS:

	@EventHandler
	public void Target(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() instanceof Player) {
			Player player = (Player) event.getTarget();
			GameInstance i = this.GetInstanceOfPlayer(player);
			if (i != null) {
				BaseClass bc = i.classes.get(player);
				if (bc != null) {
					if (event.getTarget() instanceof LivingEntity) {
						if (i.classes.get(player).getLives() <= 0) {
							event.setCancelled(true);
						}
						if (i.getMap() != null) {
							if (event.getEntity().getName().contains(player.getName()))
								event.setCancelled(true);
						} else {
							List<Player> p = new ArrayList<Player>();
							if (i.team.get(player).equals("Red"))
								for (Player pl : i.redTeam)
									p.add(pl);
							else if (i.team.get(player).equals("Blue"))
								for (Player pl : i.blueTeam)
									p.add(pl);
							else if (i.team.get(player).equals("Black"))
								for (Player pl : i.blackTeam)
									p.add(pl);

							for (Player pl : p)
								if (event.getEntity().getName().contains(pl.getName()))
									event.setCancelled(true);
						}
					}
				}
			} else {
				i = this.GetInstanceOfSpectator(player);

				if (i != null && i.spectators.contains(player) && player.getWorld() == i.getMapWorld()) {
					event.setCancelled(true);
				}
			}
		} else if (event.getTarget() instanceof Creature) {
			Creature target = (Creature) event.getTarget();
			Creature creature = (Creature) event.getEntity();
			if (event.getEntity().getCustomName() != null && creature.getCustomName() != null) {
				String ownerE = getMobOwner(creature).getName();
				String ownerT = getMobOwner(target).getName();
				if (ownerE.equals(ownerT))
					event.setCancelled(true);
			}
		} else if (event.getTarget() == null && event.getEntity() instanceof Creature) {
			Creature creature = (Creature) event.getEntity();
			Player player = getMobOwner(creature);
			GameInstance i = this.GetInstanceOfPlayer(player);
			Bukkit.getScheduler().runTaskLater(main, () -> {
				if (creature.getTarget() == null) {
					LivingEntity newTarget = i.getNearestPlayer(player, creature, 150);
					if (newTarget != null) creature.setTarget(newTarget);
				}
			}, 1L);

		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();
		boolean cancel = false;

		if (player.getWorld() == main.getLobbyWorld())
			cancel = true;

		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance != null) {
			if (instance.state != GameState.STARTED)
				cancel = true;
			else {
				if (instance.classes.containsKey(player) && instance.classes.get(player).fadeAbilityActive)
					cancel = true;
				if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() <= 0)
					cancel = true;
			}
		} else {
			GameInstance spec = this.GetInstanceOfSpectator(player);
			if (spec != null && spec.spectators.contains(player) && player.getWorld() == spec.getMapWorld())
				cancel = true;
		}

		if (cancel)
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		Player attacker = null;

		if (e.getDamager() instanceof Player) {
			attacker = (Player) e.getDamager();
		} else if (e.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) e.getDamager();
			if (proj.getShooter() instanceof Player) {
				attacker = (Player) proj.getShooter();
			}
		}

		if (attacker == null)
			return;

		// Lobby safety
		if (attacker.getWorld() == main.getLobbyWorld()) {
			e.setCancelled(true);
			return;
		}

		// Treat “spectator” however your game defines it:
		// - in spectators set, or
		// - in an instance with lives <= 0, or
		// - found via a dedicated GetInstanceOfSpectator(attacker)
		GameInstance inst = this.GetInstanceOfPlayer(attacker);
		if (inst != null) {
			// If attacker is out of lives, they’re effectively a spectator
			if (inst.classes.containsKey(attacker) && inst.classes.get(attacker).getLives() <= 0) {
				e.setCancelled(true);
				return;
			}
			// Or if your instance tracks spectators explicitly:
			if (inst.spectators.contains(attacker)) {
				e.setCancelled(true);
				return;
			}
			// Optional: only allow damage when game STARTED
			if (inst.state != GameState.STARTED) {
				e.setCancelled(true);
				return;
			}
		} else {
			// Not in a running instance but still on a map? Block.
			GameInstance specInst = this.GetInstanceOfSpectator(attacker);
			if (specInst != null && specInst.spectators.contains(attacker)
					&& attacker.getWorld() == specInst.getMapWorld()) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event) {
		for (Entry<Maps, GameInstance> game : gameMap.entrySet()) {
			if (game.getValue().PlayerInteract(event))
				return;
		}
		for (Entry<DuosMaps, GameInstance> game : gameMap2.entrySet()) {
			if (game.getValue().PlayerInteract(event))
				return;
		}
	}

	/**
	 * This function handles damage done in Duos to make sure you can't hit your
	 * teammates, so then cancel the damage event
	 *
	 * @param event
	 */
	@EventHandler
	public void teamDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				GameInstance i = this.GetInstanceOfPlayer(damager);

				if (i != null) {
					if (i.team != null) {
						if (i.team.get(damager) != null) {
							if (i.team.get(damager).equals(i.team.get(player))) {
								event.setCancelled(true);
								return;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Checks if a player tried to teleport to a barrier block and it cancels it.
	 *
	 * @param event PlayerTeleportEvent
	 */
	@EventHandler
	public void onEnderPearl(PlayerTeleportEvent event) {
		if (event.getCause() != TeleportCause.ENDER_PEARL) return;
		event.setCancelled(true);

		Player player = event.getPlayer();
		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance == null) return;

		BaseClass baseClass = instance.classes.get(player);
		if (baseClass == null || baseClass.isDead) return;

		Location to = event.getTo();

		// Check for nearby barrier blocks (1-block radius around teleport location)
		boolean nearBarrier = false;
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (to.clone().add(x, y, z).getBlock().getType() == Material.BARRIER) {
						nearBarrier = true;
						break;
					}
				}
				if (nearBarrier) break;
			}
			if (nearBarrier) break;
		}

		// Cancel if near barrier or out of bounds
		if (nearBarrier || !instance.isInBounds(to)) {
			player.sendMessage(getMain().color("&c&l(!) &rYou cannot teleport there!"));
			return;
		}

		player.teleport(to);
	}

	/**
	 * This function gets the number of active games running, used for Active Games
	 * GUI
	 *
	 * @return num of games in progress
	 */
	public int getNumOfGames() {
		int num = 0;
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			num++;
		}
		return num;
	}

	/**
	 * This function gets the number of active 'Frenzy' games in progress
	 *
	 * @return num of Frenzy games in progress
	 */
	public int getNumOfGamesFrenzy() {
		int num = 0;
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().state == GameState.STARTED && entry.getValue().gameType == GameType.FRENZY) {
				num++;
			}
		}
		return num;
	}

	/**
	 * This function gets the number of active 'Classic' games in progress
	 *
	 * @return num of Classic games in progress
	 */
	public int getNumOfGamesNormal() {
		int num = 0;
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().state == GameState.STARTED && entry.getValue().gameType == GameType.CLASSIC) {
				num++;
			}
		}
		return num;
	}

	/**
	 * This function gets the number of active 'Duel' games in progress
	 *
	 * @return num of Duel games in progress
	 */
	public int getNumOfGamesDuel() {
		int num = 0;
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().state == GameState.STARTED && entry.getValue().gameType == GameType.DUEL) {
				num++;
			}
		}
		return num;
	}

	public String mapName = "";

	/**
	 * This function gets the number of active games whether its in Waiting Lobby or
	 * In Progress. This is used for Active Games GUI for players to join/spectate
	 *
	 * @return num of active games
	 */
	public int getActiveGames() {
		int num = 0;
		for (Entry<Maps, GameInstance> games : gameMap.entrySet()) {
			if (games.getValue().state == GameState.WAITING || games.getValue().state == GameState.STARTED) {
				num++;
				mapName = games.getValue().getMap().toString();
			}
		}
		return num;
	}

	/**
	 * This function cancels mob burning from fire/day time
	 *
	 * @param event
	 */
	@EventHandler
	public void MobBurn(EntityCombustEvent event) {
		if (event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.SKELETON)
			event.setCancelled(true);
	}

	public List<Block> pBlocks = new ArrayList<Block>();
	public List<Material> pMat = new ArrayList<Material>();
	public BukkitRunnable pRunnable;

	/**
	 * This function handles when a player right clicks a chest in their inventory
	 * and detects if name is "Present". This is used for Christmas updates for 2
	 * free game loot drops
	 *
	 * @param e which is the Player interaction event with items
	 */
	@EventHandler
	public void present(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		GameInstance game = this.GetInstanceOfPlayer(player);

		if (game != null) {
			if (game.state == GameState.STARTED) {
				if (item != null) {
					if (item.getType() == Material.TRAPPED_CHEST
							&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
						ItemMeta meta = item.getItemMeta();
						if (meta.getDisplayName().contains("Present")) {
							int amount = item.getAmount();
							if (amount > 0) {
								amount--;
								if (amount == 0)
									player.getInventory().remove(Material.TRAPPED_CHEST);
								else
									item.setAmount(amount);
							}
							player.getInventory().addItem(game.getItemToDrop());
							player.getInventory().addItem(game.getItemToDrop());
							player.sendMessage(
									main.color("&r(&c&l!&r&l) &c&lMerry Christmas! &rYou received 2 items!"));
						}
					}
				}
			}

		}
	}

	private HashMap<Player, BukkitRunnable> borderRunnables = new HashMap<>();

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		GameInstance instance = this.GetInstanceOfPlayer(player);
		GameInstance specInstance = this.GetInstanceOfSpectator(player);

		if (player.getWorld() == main.getLobbyWorld()) // If player is below Y = 50, teleport them back to lobby
			if (e.getPlayer().getLocation().getY() < 0)
				main.SendPlayerToHub(player);

		if (specInstance != null && specInstance.state == GameState.STARTED
				&& e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
			if (e.getPlayer().getLocation().getY() < 50 || !specInstance.isInBounds(player.getLocation())) {
				player.teleport(specInstance.GetSpecLoc());
			}
		}

		if (instance != null) {
			if (instance.state == GameState.STARTED) {
				// On Tropical, void level is 70 because of the water
				if (instance.getMap() == Maps.Tropical) {
					if (e.getPlayer().getLocation().getY() <= 71 && e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
						EntityDamageEvent damageEvent = new EntityDamageEvent(e.getPlayer(), DamageCause.VOID, 1000);
						main.getServer().getPluginManager().callEvent(damageEvent);
					}
				}

				if (e.getPlayer().getLocation().getY() < 50 && e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
					if (instance.getMap() == Maps.Tropical)
						return;
					EntityDamageEvent damageEvent = new EntityDamageEvent(e.getPlayer(), DamageCause.VOID, 1000);
					main.getServer().getPluginManager().callEvent(damageEvent);
				}
				if (e.getPlayer().getLocation().getY() < 50) {
					if (instance.spectators.contains(player)
							|| (instance.classes.containsKey(player) && instance.classes.get(player).getLives() <= 0)) {
						player.teleport(instance.GetSpecLoc());
						return;
					}
				}
				if (!(instance.isInBounds(e.getPlayer().getLocation()))
						&& e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
					if (instance.spectators.contains(player)
							|| (instance.classes.containsKey(player) && instance.classes.get(player).getLives() <= 0)) {
						player.teleport(instance.GetSpecLoc());
						return;
					}
					BukkitRunnable runnable = borderRunnables.get(player);
					if (runnable == null) {
						runnable = new BukkitRunnable() {
							int ticks = 7;

							@Override
							public void run() {

								if (player.getGameMode() == GameMode.SPECTATOR) {
									borderRunnables.remove(player);
									this.cancel();
								}

								if (instance.state == GameState.ENDED) {
									borderRunnables.remove(player);
									this.cancel();
									player.setGameMode(GameMode.ADVENTURE);
									player.setAllowFlight(true);
								}

								if (ticks == 0) {
									player.setHealth(20.0);
									player.setAllowFlight(true);
									if (player.getGameMode() != GameMode.SPECTATOR) {
										EntityDamageEvent damageEvent = new EntityDamageEvent(e.getPlayer(),
												DamageCause.VOID, 1000);
										main.getServer().getPluginManager().callEvent(damageEvent);
										borderRunnables.remove(player);
										this.cancel();
									} else {
										borderRunnables.remove(player);
										this.cancel();
									}
								} else if (ticks <= 5 && ticks > 0 && instance.state == GameState.STARTED) {
									if (instance.isInBounds(e.getPlayer().getLocation())) {
										player.sendTitle("", "");
										borderRunnables.remove(player);
										this.cancel();
									} else {
										player.sendTitle("" + ChatColor.RED + "Warning!", "" + ChatColor.RESET
												+ "Return to Safety in " + ChatColor.YELLOW + ticks);
										EntityDamageEvent damageEvent = new EntityDamageEvent(e.getPlayer(),
												DamageCause.VOID, 3.0);
										main.getServer().getPluginManager().callEvent(damageEvent);
										player.damage(3.0);
									}
								}
								ticks--;
							}
						};
						runnable.runTaskTimer(getMain(), 0, 20);
						borderRunnables.put(player, runnable);
					}
				} else if (!((instance.isInBounds(e.getPlayer().getLocation()))
						|| e.getPlayer().getLocation().getY() < 50)
						&& e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
					player.teleport(instance.GetSpecLoc());
				}
			}
		}
	}

	public boolean chestCanOpen = false;

	@EventHandler
	public void mysteryChest(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		List<Material> list = new ArrayList<>(Arrays.asList(Material.CHEST));

		if (player.getWorld() == main.getLobbyWorld()) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && list.contains(e.getClickedBlock().getType())) {
				if (chestCanOpen == false) {
					e.setCancelled(true);
					new MysteryChestsGUI(main, e.getClickedBlock().getLocation()).inv.open(player);
				} else {
					e.setCancelled(true);
					player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "This MysteryChest is already in use!");
				}
			}
		} else {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && list.contains(e.getClickedBlock().getType())) {
				// e.setCancelled(true);
				// REMOVE LATER
			}
		}

	}

	@EventHandler
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		PlayerData data = main.getDataManager().getPlayerData(player);
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if ((player.getWorld() == main.getLobbyWorld())
				|| (i != null && (i.state == GameState.WAITING || i.state == GameState.ENDED))) {
			if (item != null && item.getType() == Material.WHEAT
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				double boosterStrength = 2.0;
				Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
				player.setVelocity(vel);
				data.magicbroom = 1;
			}
		}
	}

	@EventHandler
	public void instagib(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (i != null) {
			if (item != null && item.getType() == Material.GOLD_HOE
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				ItemMeta meta = item.getItemMeta();
				if (meta.getDisplayName().toLowerCase().contains("instagib")
						&& player.getGameMode() != GameMode.SPECTATOR) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							item.setAmount(amount);
							int dur = item.getType().getMaxDurability();
							item.setDurability((short) (item.getDurability() + dur / 5));
						}
					}

					int range = 30;
					Location endLoc = player.getEyeLocation();
					BlockIterator b = new BlockIterator(player.getEyeLocation(), 0, range);

					while (b.hasNext()) {
						Block block = b.next();
						endLoc = block.getLocation();

						if (block.getType().isSolid())
							break;
					}

					Vector dir = player.getEyeLocation().getDirection();
					double maxDist = endLoc.distance(player.getEyeLocation());

					for (double t = 1; t < maxDist; t += 0.5) {
						ParticleEffect.EXPLOSION_LARGE.display(player.getEyeLocation().add(dir.clone().multiply(t)));
					}

					for (Player p : i.players) {
						p.playSound(p.getLocation(), Sound.EXPLODE, 1, 2);
						if (p != player && i.classes.containsKey(p) && i.classes.get(player).getLives() > 0) {
							Vector d = p.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
							double dist = d.dot(dir);

							if (dist < maxDist) {
								Location closest = player.getEyeLocation().add(dir.clone().multiply(dist));

								if (closest.distanceSquared(p.getLocation().add(0, 1, 0)) <= 1.5 * 1.5) {
									Random r = new Random();
									double damage = 2.0 + (r.nextDouble() * (6.0 - 2.0));
									double height = 0.2 + (r.nextDouble() * (1.5 - 0.2));

									if (i.duosMap != null) {
										if (!(i.team.get(p).equals(i.team.get(player)))) {
											@SuppressWarnings("deprecation")
											EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.VOID,
													damage);
											i.getGameManager().getMain().getServer().getPluginManager()
													.callEvent(damageEvent);
											p.damage(damage, player);
										}
									} else {
										@SuppressWarnings("deprecation")
										EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.VOID,
												damage);
										i.getGameManager().getMain().getServer().getPluginManager()
												.callEvent(damageEvent);
										p.damage(damage, player);
									}
									p.setVelocity(new Vector(0, 1, 0).multiply(height));
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onItemConsumeEvent(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		GameInstance gameInstance = GetInstanceOfPlayer(player);
		gameInstance.classes.get(player).onConsumingItem(event);
	}

	@EventHandler
	public void onPotionSplashEvent(PotionSplashEvent event) {
		ThrownPotion thrownPotion = event.getEntity();
		if (!(thrownPotion.getShooter() instanceof Player))
			return;
		Player shooter = (Player) thrownPotion.getShooter();
		GameInstance gameInstance = GetInstanceOfPlayer(shooter);
		if (gameInstance == null)
			return;
		gameInstance.classes.get(shooter).PotionSplashEvent(event);
	}

	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		GameInstance gameInstance = GetInstanceOfPlayer(player);
		gameInstance.classes.get(player).onPlayerMove(event);
	}

	@EventHandler
	public void onFish(PlayerFishEvent event) {
		Player player = event.getPlayer();
		GameInstance gameInstance = GetInstanceOfPlayer(player);
		if (gameInstance != null) {
			gameInstance.classes.get(player).onFish(event);
		}
	}

	@EventHandler
	public void shieldPotions(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		ItemMeta meta = item.getItemMeta();

		if (item != null) {
			if (item.getType() == Material.POTION) {
				if (meta.getDisplayName().toLowerCase().contains("mini-shield")) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1000, 0));
					player.playSound(player.getLocation(), Sound.LAVA, 1, 1);
					event.setCancelled(true);
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
			}
		}
	}

	/**
	 * This event listens to when a creature spawns This method removes all small
	 * magma cubes that spawns
	 *
	 * @param event on creature spawn event
	 */
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		EntityType entityType = event.getEntityType();

		if (entityType.equals(EntityType.MAGMA_CUBE)) {
			MagmaCube magmaCube = (MagmaCube) event.getEntity();
			if (magmaCube.getSize() < 3) { // Size 3 is the medium Magma Cube
				magmaCube.remove();
			}
		}
	}

	/**
	 * This function gets rid of loot drops & exp that certain mobs can drop
	 *
	 * @param event to remove loot drops/exp from
	 */
	@EventHandler
	public void EntityDeathEvent(EntityDeathEvent event) {
		EntityType entityType = event.getEntityType();
		List<EntityType> entities = new ArrayList<>(Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON,
				EntityType.CREEPER, EntityType.PIG_ZOMBIE, EntityType.MAGMA_CUBE, EntityType.SILVERFISH,
				EntityType.WITCH, EntityType.ENDERMITE, EntityType.CHICKEN, EntityType.BLAZE, EntityType.PIG,
				EntityType.MUSHROOM_COW, EntityType.COW, EntityType.WOLF, EntityType.SPIDER, EntityType.SLIME));
		if (entities.contains(entityType)) {
			event.getDrops().clear();
			event.setDroppedExp(0);
		}
	}

	/**
	 * This function disables fishing rods hitting players
	 *
	 * @param event
	 */
	@EventHandler
	public void onHookHit(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof FishHook)
			event.setCancelled(true);
	}

	@EventHandler
	public void blooper(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (item != null && item.getType() == Material.RABBIT_FOOT
				&& event.getAction().toString().contains("RIGHT_CLICK")) {
			ItemMeta meta = item.getItemMeta();
			if (meta.getDisplayName().toLowerCase().contains("blooper")) {
				int amount = item.getAmount();
				for (int x = 0; x < 100; x++) {
					int index = (int) (Math.random() * i.players.size());
					Player target = i.players.get(index);

					if (target != player && target.getGameMode() != GameMode.SPECTATOR) {
						if (i.classes.containsKey(target) && i.classes.get(target).getLives() > 0) {
							Random r = new Random();
							int chance = r.nextInt(2);

							if (chance == 0)
								target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 2, true));
							else
								target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, true));
							player.sendMessage(main.color("&2&l(!) &rYou blooped &e" + target.getName()));
							target.sendMessage(main.color("&2&l(!) &rYou were blooped by &e" + player.getName()));
							player.playSound(player.getLocation(), Sound.SLIME_ATTACK, 2, 1);
							target.playSound(target.getLocation(), Sound.SLIME_ATTACK, 2, 1);
							amount--;

							if (amount == 0)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else
								item.setAmount(amount);

							x = 100;
							return;
						}
					}
				}
				player.sendMessage(main.color("&c&l(!) &rYou tried blooping a Spectator"));
			}
		}
	}

	@EventHandler
	public void onWolfInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Wolf) {
			event.setCancelled(true);
			ItemStack i = event.getPlayer().getItemInHand();
			if (i.getType() == Material.BONE)
				event.getPlayer().setItemInHand(i);
		}
	}

	@EventHandler
	public void onWolfFeed(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Wolf) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onWolfTame(EntityTameEvent event) {
		if (event.getEntity() instanceof Wolf) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void votePaper(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		GameInstance game = this.GetInstanceOfPlayer(player);

		if (game != null)
			if (item != null && item.getType() == Material.PAPER)
				new VoteGameSettingsGUI(main).inv.open(player);
	}

	@EventHandler
	public void extraLife(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		PlayerData data = getMain().getDataManager().getPlayerData(player);
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null) {
			if (instance.state == GameState.STARTED) {
				if (item != null && item.getType() == Material.PRISMARINE_SHARD
						&& (event.getAction() == Action.RIGHT_CLICK_AIR
								|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					if (data != null) {
						if (data.challenge3 == 0) {
							player.sendMessage(getMain()
									.color("&9&l(!) &rYou used an extra life and now rewarded with &e1 Bonus Level"));
							data.level += 1;
							data.challenge3 = 1;
							player.sendMessage(instance.getGameManager().getMain().color("&e&lLEVEL UPGRADED!"));
							player.sendMessage(instance.getGameManager().getMain()
									.color("&r&l(!) &rYou are now Level " + data.level + "&r!"));
						}
					}
					BaseClass baseClass = instance.classes.get(player);
					int amount = item.getAmount();
					baseClass.lives += 1;
					baseClass.score.setScore(baseClass.lives);
					baseClass.TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ ChatColor.YELLOW + player.getName() + ChatColor.RESET + " used an extra life!");
					player.getWorld().playSound(player.getLocation(), Sound.LEVEL_UP, 1, 2);
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);
					}
				}
			}
		}
	}

	@EventHandler
	public void cosmeticMelon(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		PlayerData data = main.getDataManager().getPlayerData(player);
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if ((player.getWorld() == main.getLobbyWorld()) || (i != null && i.state == GameState.WAITING)) {
			if (item != null && item.getType() == Material.MELON) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					if (data.melon > 0) {
						data.melon--;
						main.getDataManager().saveData(data);
						String msg = main.color("&9&l(!) &rYou have &e" + data.melon + " melons");
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) player;
						craft.getHandle().playerConnection.sendPacket(packet);
						player.playSound(player.getLocation(), Sound.EAT, 2, 1);
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 3));
						player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 110, 3));
						if (data.melon == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
					}
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void tntChange(EntityChangeBlockEvent event) {
		Entity e = event.getEntity();

		if (e instanceof Arrow)
			if (event.getBlock().getType() == Material.TNT)
				event.setCancelled(true);
	}

	/**
	 * This function disables players from moving items in their inventory
	 *
	 * @param e
	 */
	@EventHandler
	public void onInv(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();

		if (!(player.isOp()))
			e.setCancelled(true);
	}

	/**
	 * This function disables players from dropping items
	 *
	 * @param e
	 */
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void bazooka(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		Action action = event.getAction();

		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
			return;

		if (instance != null && instance.state == GameState.STARTED) {
			BaseClass bc = instance.classes.get(player);
			if (item != null && item.getType() == Material.DIAMOND_HOE) {
				ItemMeta meta = item.getItemMeta();

				if (meta.getDisplayName().toLowerCase().contains("bazooka")
						&& player.getGameMode() != GameMode.SPECTATOR) {
					if (bc != null) {
						if (bc.bazooka.getTime() < 3000) {
							int seconds = (3000 - bc.bazooka.getTime()) / 1000 + 1;
							event.setCancelled(true);
							player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "Your Bazooka is still regenerating for " + ChatColor.YELLOW + seconds + "s");
						} else {
							int amount = item.getAmount();
							if (amount > 0) {
								amount--;
								if (amount == 0)
									player.getInventory().clear(player.getInventory().getHeldItemSlot());
								else {
									item.setAmount(amount);
									int dur = item.getType().getMaxDurability();
									item.setDurability((short) (item.getDurability() + dur / 3));
								}
							}
							bc.bazooka.restart();
							ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
								@Override
								public void onHit(Player hit) {
									if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
										Location hitLoc = this.getBaseProj().getEntity().getLocation();

										for (Player gamePlayer : this.getNearby(3.0)) {
											if (instance.duosMap != null) {
												if (!(instance.team.get(gamePlayer)
														.equals(instance.team.get(player)))) {
													EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
															DamageCause.VOID, 5.5);
													instance.getGameManager().getMain().getServer().getPluginManager()
															.callEvent(damageEvent);
													gamePlayer.damage(5.5, player);
												}
											} else {
												EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
														DamageCause.VOID, 5.5);
												instance.getGameManager().getMain().getServer().getPluginManager()
														.callEvent(damageEvent);
												gamePlayer.damage(5.5, player);
											}
										}
										for (Player gamePlayer : instance.players) {
											gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
											gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
										}
									}

								}

							}, new ItemStack(Material.TNT));
							instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
									player.getLocation().getDirection().multiply(2.0D));
						}
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void broom(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null && instance.state == GameState.STARTED) {
			if (item != null && item.getType() == Material.WHEAT) {
				ItemMeta meta = item.getItemMeta();
				if (!(meta.getDisplayName().contains("Magic"))) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);
						e.setCancelled(true);
						player.setVelocity(new Vector(0, 1, 0).multiply(1.3D));
						for (Player gamePlayer : instance.players)
							gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
					}
				}
			}
		}
	}

	@EventHandler
	public void milk(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null && instance.state == GameState.STARTED) {
			BaseClass bc = instance.classes.get(player);
			if (item != null && item.getType() == Material.MILK_BUCKET) {
				if (player.getGameMode() != GameMode.SPECTATOR) {

					ItemMeta meta = item.getItemMeta();

					if (meta != null && meta.getDisplayName().contains("Santa's Milk")) {
						return;
					}

					// Remove bad effects only: poison, wither, slowness, weakness, blindness,
					// nausea
					for (PotionEffect pe : player.getActivePotionEffects())
						if (pe.getType().equals(PotionEffectType.POISON) || pe.getType().equals(PotionEffectType.SLOW)
								|| pe.getType().equals(PotionEffectType.SLOW_DIGGING)
								|| pe.getType().equals(PotionEffectType.BLINDNESS)
								|| pe.getType().equals(PotionEffectType.WEAKNESS)
								|| pe.getType().equals(PotionEffectType.WITHER)
								|| pe.getType().equals(PotionEffectType.CONFUSION)
								|| pe.getType().equals(PotionEffectType.HUNGER)) {

							player.removePotionEffect(pe.getType());
						}
					// Remove fire by setting fire ticks to 0
					player.setFireTicks(0);
					player.playSound(player.getLocation(), Sound.DRINK, 1, 1);
					if (bc != null && bc.getType() != ClassType.Mooshroom) { // Mooshroom milk bucket has its own
																				// behaviour
						player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
								+ ChatColor.RESET + "You feel refreshed!");
						int amount = item.getAmount();
						if (amount > 0) {
							amount--;
							if (amount == 0)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else
								item.setAmount(amount);
						}
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void spectator(PlayerInteractEvent e) {
		ItemStack item = e.getItem();
		Player player = e.getPlayer();
		GameInstance i = this.GetInstanceOfPlayer(player);
		// Cancel interactions for spectators with anything besides these 2 items
		if (i != null) {
			if (i.classes.containsKey(player) && i.classes.get(player).getLives() <= 0) {
				e.setCancelled(true);
				if (item != null && (item.getType() == Material.COMPASS || item.getType() == Material.BARRIER)
						&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					e.setCancelled(false);
				}
				e.setCancelled(true);
			}
		} else {
			i = this.GetInstanceOfSpectator(player);

			if (i != null) {
				if (i.spectators.contains(player) && player.getWorld() == i.getMapWorld()) {
					e.setCancelled(true);
					if (item != null && (item.getType() == Material.COMPASS || item.getType() == Material.BARRIER)
							&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
						e.setCancelled(false);
					}
					e.setCancelled(true);
				}
			}
		}

		i = null;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void activeGames(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (event.getItem() != null && event.getItem().getType() == Material.EYE_OF_ENDER) {
			event.setCancelled(true);
			if (player.getWorld() == main.getLobbyWorld())
				new ActiveGamesGUI(getMain()).inv.open(player);
		}

	}

	/**
	 * This function cancels item pickup if the item is a projectile thrown by a
	 * player in game
	 *
	 * @param event
	 */
	@EventHandler
	public void OnPickupItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();

		if (projManager.isProjectile(item))
			event.setCancelled(true);
	}

	// Disables portals usage
	@EventHandler
	public void onPortal(EntityPortalEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void snowballs(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Snowball) {
			Snowball snowball = (Snowball) event.getDamager();
			if (snowball.getShooter() instanceof Player) {
				Player shooter = (Player) snowball.getShooter();
				if (event.getEntity() instanceof Player) {
					Player hitPlayer = (Player) event.getEntity();
					GameInstance i = this.GetInstanceOfPlayer(hitPlayer);

					if (i != null) {
						if (i.duosMap != null) {
							if (!(i.team.get(shooter).equals(i.team.get(hitPlayer)))) {
								if (i.classes.get(shooter).getType() == ClassType.SnowGolem)
									hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 2)); // Slowness
								// 3 -
								// Snowgolem
								else
									hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 0)); // Slowness
								// 1
							}
						} else {
							if (i.classes.get(shooter).getType() == ClassType.SnowGolem)
								hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 2)); // Slowness
							// 3
							// -
							// Snowgolem
							else
								hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 0)); // Slowness
							// 1
						}
					}
				}
			}
		}
	}

	public void joinRandomGame(Player player, GameType type) {
		// Loop through each map & find map that is open
		List<Maps> existingMaps = new ArrayList<>();

		for (Entry<Maps, GameInstance> entry : this.gameMap.entrySet()) {
			if (entry.getKey().GetInstance().gameType == type) {
				if (entry.getValue().isOpen()) {
					this.JoinMap(player, entry.getKey());
					return;
				}
				existingMaps.add(entry.getKey());
			}
		}

		List<Maps> maps = Maps.getGameType(type);
		maps.removeAll(existingMaps);
		if (maps.size() > 0) {
			Maps map = maps.get(new Random().nextInt(maps.size()));
			this.JoinMap(player, map);
		} else
			player.sendMessage(main.color("&c&l(!) &rAll games are full! Try again later"));
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();

			GameInstance instance = GetInstanceOfPlayer(player);
			if (instance != null) {
				if (instance.classes.containsKey(player)) {
					BaseClass base = instance.classes.get(player);
					if (this.spawnProt.containsKey(player) || base.bedrockInvincibility == true) {
						event.setCancelled(true);
					}
				}
				if (event instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
					if (damageEvent.getDamager() instanceof Player) {
						Player damager = (Player) damageEvent.getDamager();

						if (instance.state == GameState.STARTED) {
							if (instance.duosMap != null) {
								if (instance.team.get(player).equals(instance.team.get(damager))) {
									event.setCancelled(true);
									return;
								}
							}

							if (instance.classes.containsKey(damager) && instance.classes.containsKey(player)) {
								BaseClass baseClass = instance.classes.get(damager);
								BaseClass baseClass2 = instance.classes.get(player);

								if (this.spawnProt.containsKey(player) || baseClass2.bedrockInvincibility == true) {
									event.setCancelled(true);
									return;
								}
								if (this.spawnProt.containsKey(damager) || baseClass.bedrockInvincibility == true) {
									event.setCancelled(true);
									return;
								}
								if (instance.classes.containsKey(damager)
										&& instance.classes.get(damager).fadeAbilityActive == true) {
									event.setCancelled(true);
								}
							}
						}

						// For spectators:
						GameInstance specInstance = this.GetInstanceOfPlayer(damager);
						if (specInstance != null && specInstance.classes.containsKey(damager)
								&& specInstance.classes.get(damager).getLives() <= 0) {
							event.setCancelled(true);
						} else {
							specInstance = this.GetInstanceOfSpectator(damager);

							if (specInstance != null && specInstance.spectators.contains(damager)
									&& damager.getWorld() == specInstance.getMapWorld()) {
								event.setCancelled(true);
							}
						}

						if (instance.classes.containsKey(damager) && instance.classes.containsKey(player)) {
							BaseClass baseClass = instance.classes.get(damager);
							BaseClass baseClass2 = instance.classes.get(player);

							if (baseClass != null && baseClass2 != null) {
								if (this.spawnProt.containsKey(damager) || baseClass.bedrockInvincibility == true) {
									event.setCancelled(true);
									return;
								}
								if (this.spawnProt.containsKey(player) || baseClass2.bedrockInvincibility == true) {
									event.setCancelled(true);
									return;
								}
								if (baseClass.getType() == ClassType.FlintAndSteel)
									if (baseClass.flintUsed == true)
										baseClass.DoDamage2(damageEvent);

								baseClass.DoDamage(damageEvent);
							}
						}
					} else if (damageEvent.getDamager() instanceof WitherSkull) {
						WitherSkull s = (WitherSkull) damageEvent.getDamager();

						if (s.getShooter() instanceof Player) {
							Player shoot = (Player) s.getShooter();
							if (instance.duosMap != null) {
								if (!(instance.team.get(player).equals(instance.team.get(shoot)))) {
									player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 0, true));
								} else {
									event.setCancelled(true);
								}
							} else {
								player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 120, 0, true));
							}
							player.setLastDamageCause(
									new EntityDamageByEntityEvent(shoot, player, event.getCause(), event.getDamage()));
						}
					} else if (damageEvent.getDamager() instanceof SmallFireball) {
						SmallFireball sf = (SmallFireball) damageEvent.getDamager();

						if (sf.getShooter() instanceof Player) {
							Player shoot = (Player) sf.getShooter();
							if (instance.duosMap != null) {
								if (instance.team.get(player).equals(instance.team.get(shoot))) {
									event.setCancelled(true);
								}
							}
							player.setLastDamageCause(
									new EntityDamageByEntityEvent(shoot, player, event.getCause(), event.getDamage()));
						}
					} else if (damageEvent.getDamager() instanceof Fireball) {
						Fireball sf = (Fireball) damageEvent.getDamager();

						if (sf.getShooter() instanceof Player) {
							Player shoot = (Player) sf.getShooter();
							if (instance.duosMap != null) {
								if (instance.team.get(player).equals(instance.team.get(shoot))) {
									event.setCancelled(true);
								}
							}
							player.setLastDamageCause(
									new EntityDamageByEntityEvent(shoot, player, event.getCause(), event.getDamage()));
						}
					} else if (damageEvent.getDamager() instanceof Arrow) {
						Arrow arrow = (Arrow) damageEvent.getDamager();
						if (arrow.getShooter() instanceof Player) {
							Player shooterPlayer = (Player) arrow.getShooter();
							BaseClass shooterBaseClass = instance.classes.get(shooterPlayer);
							BaseClass pBc = instance.classes.get(player);

							if (instance.duosMap != null) {
								if (instance.team.get(shooterPlayer).equals(instance.team.get(player))) {
									event.setCancelled(true);
									return;
								}
							}

							if (shooterBaseClass != null && pBc != null) {
								if (shooterBaseClass.getType() == ClassType.Vampire
										|| shooterBaseClass.getType() == ClassType.WitherSkeleton
										|| shooterBaseClass.getType() == ClassType.Shulker
										|| shooterBaseClass.getType() == ClassType.Firework
										|| shooterBaseClass.getType() == ClassType.Skeleton
										|| shooterBaseClass.getType() == ClassType.Ghast) {
									if (this.spawnProt.containsKey(shooterPlayer)
											|| shooterBaseClass.bedrockInvincibility == true) {
										event.setCancelled(true);
										return;
									}
									if (this.spawnProt.containsKey(player) || pBc.bedrockInvincibility == true) {
										event.setCancelled(true);
										return;
									}
									player.setLastDamageCause(new EntityDamageByEntityEvent(shooterPlayer, player,
											event.getCause(), event.getDamage()));
									shooterBaseClass.DoDamage(damageEvent);
								}
							}
						}
					} else if (damageEvent.getDamager() instanceof FishHook) {
						FishHook damager = (FishHook) damageEvent.getDamager();
						if (damager.getShooter() instanceof Player) {
							Player p = (Player) damager.getShooter();
							BaseClass bc = instance.classes.get(p);
							if (bc.getType() == ClassType.Fisherman) {
								// damager.setBounce(true);
								event.setCancelled(true);
							}
						}
					}
				}

				if (!event.isCancelled() && event.getFinalDamage() >= player.getHealth() - 0.2) {
					event.setCancelled(true);
					if (event instanceof EntityDamageByEntityEvent) {
						EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
						if (damageEvent.getDamager() instanceof Player) {
							Player damager = (Player) damageEvent.getDamager();
							player.setLastDamageCause(new EntityDamageByEntityEvent(damager, player, event.getCause(),
									event.getDamage()));
						} else if (damageEvent.getDamager() instanceof Arrow) {
							Arrow a = (Arrow) damageEvent.getDamager();
							if (a.getShooter() instanceof Player) {
								Player damager = (Player) a.getShooter();
								player.setLastDamageCause(new EntityDamageByEntityEvent(damager, player,
										event.getCause(), event.getDamage()));
							}
						}
					}
					instance.PlayerDeath(player);
					return;
				} else if (instance.classes.containsKey(player))
					instance.classes.get(player).TakeDamage(event);
			}
		}
	}

	// Disables painting breaking
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPaintingBreak(PaintingBreakEvent event) {
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void FireballDamage(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			GameInstance instance = this.GetInstanceOfPlayer(player);
			if (instance != null) {
				BaseClass baseClass = instance.classes.get(player);
				baseClass.ProjectileLaunch(event);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void WitherSk(ProjectileHitEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			GameInstance instance = this.GetInstanceOfPlayer(player);
			if (instance != null) {
				BaseClass baseClass = instance.classes.get(player);
				baseClass.ProjectileHit(event);
			}
		}
	}

	public Core getMain() {
		return main;
	}

	public void playerSelectClass(Player player, ClassType type) {
		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance != null)
			instance.setClass(player, type);
	}

	public GameInstance getWaitingMap() {
		for (Entry<Maps, GameInstance> entry : this.gameMap.entrySet()) {
			if (entry.getValue().state == GameState.WAITING && entry.getValue().players.size() > 0)
				// if (entry.getValue().gameType == GameType.NORMAL &&
				// entry.getValue().players.size() == 5)
				// return null;
				/* else */ if (entry.getValue().gameType == GameType.CLASSIC && entry.getValue().players.size() < 5)
					return entry.getValue();
				// else if (entry.getValue().gameType == GameType.DUEL &&
				// entry.getValue().players.size() == 1)
				// return null;
				else if (entry.getValue().gameType == GameType.DUEL && entry.getValue().players.size() < 2)
					return entry.getValue();
				else if (entry.getValue().gameType == GameType.FRENZY)
					return entry.getValue();
		}
		return null;
	}

	@EventHandler
	public void onSignChange2(SignChangeEvent e) {
		for (Maps map : Maps.values()) {
			if (e.getLine(0).equalsIgnoreCase(map.toString())) {
				e.setLine(0, main.color("&2Lobby"));
				e.setLine(1, main.color("&0" + map.toString()));
				e.setLine(2, main.color("&0Players: 0/" + map.GetInstance().gameType.getMaxPlayers()));
				e.setLine(3, main.color("&030s"));
			}
		}
	}

	public void JoinDuosMap(Player player, DuosMaps map) {
		GameReason result = main.getGameManager().AddPlayerToDuosMap(player, map);
		GameInstance instance = this.GetInstanceOfPlayer(player);
		MapInstance mi = map.GetInstance();
		Location loc = new Location(main.getLobbyWorld(), mi.signLoc.getX(), mi.signLoc.getY(), mi.signLoc.getZ());
		Block b = main.getLobbyWorld().getBlockAt(loc);

		if (b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
			if (instance != null) {
				Sign s = (Sign) b.getState();
				instance.setSign(s);
				s.setLine(2, main.color("&0Players: " + instance.players.size() + "/6"));
				s.setLine(3, main.color("&0" + instance.timeToStartSeconds + "s"));
				s.update();
			}
		}
		switch (result) {
		case SUCCESS:
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(true);

			if (player.getWorld() != main.getLobbyWorld()) {
				player.getInventory().clear();
				ItemStack classItem = ItemHelper.setDetails(new ItemStack(Material.COMPASS),
						"" + ChatColor.GREEN + ChatColor.BOLD + "Class Selector",
						ChatColor.GRAY + "Click to choose a class!");
				ItemStack teamSel = ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE),
						"" + ChatColor.GREEN + ChatColor.BOLD + "Team Selector",
						ChatColor.GRAY + "Click to choose a team!");
				player.getInventory().setItem(0, classItem);
				player.getInventory().setItem(1, teamSel);

				ItemStack stats = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
				SkullMeta statsMeta = (SkullMeta) stats.getItemMeta();
				statsMeta.setOwner(player.getName());
				stats.setItemMeta(statsMeta);

				player.getInventory().setItem(7,
						ItemHelper.setDetails(stats, "" + ChatColor.RESET + ChatColor.BOLD + "Profile"));
				player.getInventory().setItem(4,
						ItemHelper.setDetails(new ItemStack(Material.CHEST), "" + ChatColor.GRAY + "Cosmetics"));

				ItemStack leaveItem = ItemHelper.setDetails(new ItemStack(Material.BARRIER),
						"" + ChatColor.RED + ChatColor.BOLD + "Leave Game",
						ChatColor.GRAY + "Click to leave your game");
				player.getInventory().setItem(8, leaveItem);
			}

			break;
		case ALREADY_IN:
			player.sendMessage(main.color("&c&l(!) &rYou are already in a map!"));
			break;

		case IN_ANOTHER:
			player.sendMessage(main.color("&c&l(!) &rYou are already in a game!"));
			break;

		case ALREADYPLAYING:
			player.sendMessage(main.color("&c&l(!) &rThis game is already playing!"));
			break;
		}
	}

	public void JoinMap(Player player, Maps map) {
		GameReason result = main.getGameManager().AddPlayerToMap(player, map);
		GameInstance instance = this.GetInstanceOfPlayer(player);
		MapInstance mi = map.GetInstance();
		main.getSignManager().updateSign(mi, instance); // Updates sign in lobby when a new player joins

		switch (result) {
		case SUCCESS:
			if (instance.gameType == GameType.FRENZY) {
				player.sendMessage(main.color(
						"&2&l(!) &rYou have joined a Frenzy game, your class will be randomly selected each life"));
			}

			player.setGameMode(GameMode.ADVENTURE);
			main.getListener().resetDoubleJump(player);
			main.getLobbyItems().gameLobbyItems(player);
			break;
		case ALREADY_IN:
			player.sendMessage(main.color("&c&l(!) &rYou are already in a map!"));
			break;

		case IN_ANOTHER:
			player.sendMessage(main.color("&c&l(!) &rYou are already in a game!"));
			break;

		case ALREADYPLAYING:
			player.sendMessage(main.color("&c&l(!) &rThis game is already playing!"));
			break;
		}
	}

	public void SpectatorJoinDuosMap(Player player, DuosMaps map) {
		GameReason result = main.getGameManager().AddSpectatorToDuosMap(player, map);

		switch (result) {
		case SPECTATOR:
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You are now spectating " + ""
					+ ChatColor.GREEN + map.toString() + "." + ChatColor.RESET + " Use " + ChatColor.GREEN + "/leave "
					+ ChatColor.RESET + "to leave");
			player.setGameMode(GameMode.SPECTATOR);
			break;

		case ALREADY_IN:
			player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have to leave your game to Spectate");
			break;

		case FAIL:
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "This game is not playing!");
			break;
		}
	}

	public GameReason AddSpectatorToDuosMap(Player player, DuosMaps map) {
		GameInstance instance = null;

		if (GetInstanceOfPlayer(player) != null) {
			player.sendMessage(main.color("&c&l(!) &rYou have to leave your game to Spectate"));
			return GameReason.IN_ANOTHER;
		}

		if (gameMap2.containsKey(map))
			instance = gameMap2.get(map);
		else {
			instance = new GameInstance(this, map);
			gameMap2.put(map, instance);
		}

		return instance.AddSpectator(player);
	}

	public void SpectatorJoinMap(Player player, Maps map) {
		GameReason result = main.getGameManager().AddSpectatorToMap(player, map);

		switch (result) {
		case SPECTATOR:
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You are now spectating " + ""
					+ ChatColor.GREEN + map.toString() + "." + ChatColor.RESET + " Use " + ChatColor.GREEN + "/leave "
					+ ChatColor.RESET + "to leave");
			player.setGameMode(GameMode.ADVENTURE); // Edit if needed
			player.spigot().setCollidesWithEntities(false);
			player.getInventory().clear();
			main.getLobbyItems().spectatorItems(player);
			break;

		case ALREADY_IN:
			player.sendMessage(main.color("&c&l(!) &rYou have to leave your game to Spectate"));
			break;

		case FAIL:
			player.sendMessage(main.color("&c&l(!) &rThis game is not playing!"));
			break;
		}
	}

	public GameReason AddSpectatorToMap(Player player, Maps map) {
		GameInstance instance = null;

		if (GetInstanceOfPlayer(player) != null) {
			player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have to leave your game to Spectate");
			return GameReason.IN_ANOTHER;
		}

		if (gameMap.containsKey(map))
			instance = gameMap.get(map);
		else {
			instance = new GameInstance(this, map);
			gameMap.put(map, instance);
		}

		return instance.AddSpectator(player);
	}

	public GameReason AddPlayerToDuosMap(Player player, DuosMaps map) {
		GameInstance instance = null;

		if (GetInstanceOfPlayer(player) != null) {
			return GameReason.IN_ANOTHER;
		}
		if (gameMap2.containsKey(map))
			instance = gameMap2.get(map);
		else {
			instance = new GameInstance(this, map);
			gameMap2.put(map, instance);
		}

		GameReason reason = instance.AddPlayer(player);

		return reason;
	}

	/*
	 * This function adds player to the game they are joining as long as they are
	 * not in another game
	 */
	public GameReason AddPlayerToMap(Player player, Maps map) {
		GameInstance instance = null;

		if (GetInstanceOfPlayer(player) != null || getMain().getParkour().hasPlayer(player))
			return GameReason.IN_ANOTHER;

		if (gameMap.containsKey(map)) // Checks if the game has already been initialized
			instance = gameMap.get(map);
		else {
			instance = new GameInstance(this, map); // Creates a new game if one doesn't already exist
			gameMap.put(map, instance);
		}

		GameReason reason = instance.AddPlayer(player);

		return reason;
	}

	/**
	 * This function disables weather from changing
	 *
	 * @param event
	 */
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event) {
		if ((event.getAction() == Action.PHYSICAL) && (event.getClickedBlock().getType() == Material.SOIL))
			event.setCancelled(true);
	}

	@EventHandler
	public void endCrystal(EntityDamageByEntityEvent e) {
		if (e.getEntity().getType() == EntityType.ENDER_CRYSTAL) {
			/*
			 * if (e.getDamager() instanceof Player) { Player player = (Player)
			 * e.getDamager();
			 *
			 * if (main.getCwManager() == null) { e.setCancelled(true); }
			 * anthony.CrystalWars.game.GameInstance i =
			 * main.getCwManager().getInstanceOfPlayer(player);
			 *
			 * if (i != null) { if (i.getTeam(player).equals("Blue")) { if
			 * (i.isInBlue(player.getLocation())) { player.sendMessage(main.
			 * color("&c&l(!) &rYou cannot destroy your own crystal!"));
			 * e.setCancelled(true); } else if (i.isInRed(player.getLocation())) {
			 * i.TellAll(main.color("&2&l(!) &r&lRed Crystal &rwas destroyed by &e" +
			 * player.getName()));
			 *
			 * for (Player p : i.getPlayers()) { if (i.getTeam(p).equals("Red")) {
			 * p.sendTitle(main.color("&cCRYSTAL DESTROYED"),
			 * main.color("&rYou will no longer respawn")); i.crystal.remove(p); } }
			 *
			 * e.setCancelled(false); } } else if (i.getTeam(player).equals("Red")) { if
			 * (i.isInRed(player.getLocation())) { player.sendMessage(main.
			 * color("&c&l(!) &rYou cannot destroy your own crystal!"));
			 * e.setCancelled(true); } else if (i.isInBlue(player.getLocation())) {
			 * i.TellAll(main.color("&2&l(!) &r&lBlue Crystal &rwas destroyed by &e" +
			 * player.getName()));
			 *
			 * for (Player p : i.getPlayers()) { if (i.getTeam(p).equals("Blue")) {
			 * p.sendTitle(main.color("&cCRYSTAL DESTROYED"),
			 * main.color("&rYou will no longer respawn")); i.crystal.remove(p); } }
			 *
			 * e.setCancelled(false); } } } else { e.setCancelled(true); } } else {
			 * e.setCancelled(true); }
			 */
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void endCrystal(EntityExplodeEvent e) {
		if (e.getEntity().getType() == EntityType.ENDER_CRYSTAL)
			e.setCancelled(true);
	}

	@EventHandler
	public void teamSelector(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (item != null) {
			if (i != null && i.state == GameState.WAITING) {
				if (item.getType() == Material.STAINED_GLASS_PANE) {
					new TeamSelectionGUI(getMain()).inv.open(player);
				}
			}
		}
	}

	@EventHandler
	public void fireFlower(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null && instance.state == GameState.STARTED) {
			if (item != null && item.getType() == Material.RED_ROSE) {
				ItemMeta meta = item.getItemMeta();

				if (meta != null && meta.getDisplayName() != null &&
						ChatColor.stripColor(meta.getDisplayName()).contains("FIRE FLOWER")) {
					player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
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

									for (Player gamePlayer : this.getNearby(3.0)) {
										if (instance.duosMap != null) {
											if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
												@SuppressWarnings("deprecation")
												EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
														DamageCause.VOID, 4.0);
												instance.getGameManager().getMain().getServer().getPluginManager()
														.callEvent(damageEvent);
												gamePlayer.damage(4.0, player);
												gamePlayer.setFireTicks(80);
											}
										} else {
											if (instance.classes.containsKey(gamePlayer) && instance.classes.get(gamePlayer).getLives() > 0) {
												EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer,
														DamageCause.VOID, 4.0);
												instance.getGameManager().getMain().getServer().getPluginManager()
														.callEvent(damageEvent);
												gamePlayer.damage(4.0, player);
												gamePlayer.setFireTicks(80);
											}
										}
									}
									for (Player gamePlayer : instance.players) {
										gamePlayer.playSound(hitLoc, Sound.ZOMBIE_INFECT, 2, 1);
										gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_LARGE, 1);
									}
								}

							}

						}, new ItemStack(Material.BLAZE_POWDER));
						instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				}
			}
		}
	}

	@EventHandler
	public void Nuke(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (i != null && i.state == GameState.STARTED) {
			if (item != null && item.getType() == Material.TNT) {
				Action action = event.getAction();
				if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
					return;

				if (item.getAmount() % 3 == 0) {
					if (item.getAmount() == 3)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(item.getAmount() - 3);
					Location loc = player.getTargetBlock((HashSet<Byte>) null, 25).getLocation();
					TNTPrimed tnt = player.getWorld().spawn(loc.add(0, 1, 0), TNTPrimed.class);
					tnt.setFuseTicks(50);
					tnt = player.getWorld().spawn(loc.add(1, 1, 0), TNTPrimed.class);
					tnt.setFuseTicks(50);
					tnt = player.getWorld().spawn(loc.add(1, 1, 1), TNTPrimed.class);
					tnt.setFuseTicks(50);

					// Playing Sound
					SoundManager.playSoundToAll(player, loc, Sound.FUSE, 1, 1);
				}
			}
		}
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (event.getEntity() instanceof EnderDragon)
			event.setCancelled(true);
	}

	@EventHandler
	public void onIgnite(BlockIgniteEvent e) {
		IgniteCause cause = e.getCause();
		if (cause == IgniteCause.EXPLOSION || cause == IgniteCause.FIREBALL)
			e.setCancelled(true);
	}

	@EventHandler
	public void onBlowUp(EntityExplodeEvent e) {
		if (e.getEntity() instanceof SmallFireball || e.getEntity() instanceof Fireball) {
			e.blockList().clear();
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void chunks(ChunkUnloadEvent e) {
		World w = main.getLobbyWorld();
		for (Maps map : Maps.values()) {
			MapInstance mi = map.GetInstance();

			if (w.getChunkAt(mi.signLoc.toLocation(w)) == e.getChunk())
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void FireballDamage(EntityExplodeEvent event) {
		if (event.getEntity() instanceof SmallFireball || event.getEntity() instanceof Fireball) {
			event.setCancelled(true);
			((Fireball) event.getEntity()).setIsIncendiary(false);
			event.getEntity().remove();
			event.blockList().clear();
		} else if (event.getEntity() instanceof WitherSkull) {
			event.setCancelled(true);
			event.blockList().clear();
		} else if (event.getEntity() instanceof Creeper) {
			event.setCancelled(true);
			List<Entity> nearby = event.getEntity().getNearbyEntities(15, 10, 15);
			for (Entity e : nearby) {
				if (e instanceof Player) {
					Player p = (Player) e;

					if (p.getGameMode() != GameMode.SPECTATOR) {
						@SuppressWarnings("deprecation")
						EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.PROJECTILE, 8.0);
						getMain().getServer().getPluginManager().callEvent(damageEvent);
						p.damage(8.0, e);
					}
				}
			}
		} else if (event.getEntity() instanceof TNTPrimed) {
			event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.EXPLOSION_HUGE, 1);
			event.blockList().clear();
		}
	}

	public GameInstance GetInstanceOfPlayer(Player player) {
		for (Entry<Maps, GameInstance> games : gameMap.entrySet())
			if (games.getValue().HasPlayer(player))
				return games.getValue();
		for (Entry<DuosMaps, GameInstance> games : gameMap2.entrySet())
			if (games.getValue().HasPlayer(player))
				return games.getValue();
		return null;
	}

	public GameInstance GetInstanceOfSpectator(Player spectator) {
		for (Entry<Maps, GameInstance> games : gameMap.entrySet())
			if (games.getValue().HasSpectator(spectator))
				return games.getValue();
		for (Entry<DuosMaps, GameInstance> games : gameMap2.entrySet())
			if (games.getValue().HasSpectator(spectator))
				return games.getValue();
		return null;
	}

	public GameInstance getInstanceOfMap(Maps map) {
		return this.gameMap.get(map);
	}

	public void RemovePlayerFromMap(Player player, Maps map, Player player2) {
		if (this.gameMap.containsKey(map))
			this.gameMap.get(map).RemovePlayer(player);
	}

	public boolean RemovePlayerFromAll(Player player) {
		GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
		boolean found = false;

		if (instance != null) {
			if (instance.getMap() != null) {
				List<Maps> toRemove = new ArrayList<>();

				for (Entry<Maps, GameInstance> games : gameMap.entrySet()) {
					if (games.getValue().RemovePlayer(player)) {
						if (games.getValue().players.isEmpty()) {
							toRemove.add(games.getKey());
						}

						found = true;
					}
				}

				for (Maps maps : toRemove)
					gameMap.remove(maps);
			} else if (instance.duosMap != null) {
				List<DuosMaps> toRemove = new ArrayList<>();

				for (Entry<DuosMaps, GameInstance> games : gameMap2.entrySet()) {
					if (games.getValue().RemovePlayer(player)) {
						if (games.getValue().players.size() == 0)
							toRemove.add(games.getKey());

						found = true;
					}
				}

				for (DuosMaps maps : toRemove)
					gameMap2.remove(maps);
			}

			instance.getGameSettings().removeFromStartVotes(player);
			instance.getGameSettings().removeFromLightningVotes(player);
			instance.getGameSettings().removeFromGameTypeVotes(player);
			instance.getGameSettings().removeFromTimeVotes(player);
		}

		return found;
	}

	// When a match is over it'll remove the game from the active games
	public void RemoveMap(Maps maps) {
		gameMap.remove(maps);
	}

	// When a match is over it'll remove the game from the active games
	public void RemoveDuosMap(DuosMaps maps) {
		gameMap2.remove(maps);
	}

	/**
	 * This function cancels spectators hitting entities
	 *
	 * @param event
	 */
	@EventHandler
	public void onSpectatorDamage(EntityDamageByEntityEvent event) {
		// Damager
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			GameInstance instance = this.GetInstanceOfPlayer(damager);
			if (instance == null)
				return;
			if (!(damager.getGameMode() == GameMode.SPECTATOR))
				return;
			event.setCancelled(true);
		}

		// Damagee
		if (event.getEntity() instanceof Player) {
			Player damagee = (Player) event.getEntity();
			GameInstance instance = this.GetInstanceOfPlayer(damagee);
			if (instance == null)
				return;
			if (!(damagee.getGameMode() == GameMode.SPECTATOR))
				return;
			event.setCancelled(true);
		}
	}

	/**
	 * This function handles spawn protection so players cant get damaged
	 *
	 * @param event
	 */
	@EventHandler
	public void onDamageSpawnProtection(EntityDamageByEntityEvent event) {
		// Damager
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			GameInstance instance = this.GetInstanceOfPlayer(damager);
			if (instance == null)
				return;
			if (!spawnProt.containsKey(damager))
				return;
			event.setCancelled(true);
			SoundManager.playNMSSoundToPlayer(damager, "mob.guardian.elder.hit", 1, 1.6f);
		}

		// Damagee
		if (event.getEntity() instanceof Player) {
			Player damagee = (Player) event.getEntity();
			GameInstance instance = this.GetInstanceOfPlayer(damagee);
			if (instance == null)
				return;
			if (!spawnProt.containsKey(damagee))
				return;
			event.setCancelled(true);
			SoundManager.playNMSSoundToPlayer(damagee, "mob.guardian.elder.hit", 1, 1.6f);
		}
	}

	public void addSpawnProtection(Player player) {
		BukkitRunnable runnable = this.spawnProt.get(player);
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (runnable == null) {
			runnable = new BukkitRunnable() {
				int ticks = SPAWN_PROT_DURATION;

				@Override
				public void run() {
					if (ticks == 0 || (instance != null && instance.state == GameState.ENDED)) {
						spawnProt.remove(player);
						this.cancel();
					}

					ticks--;
				}
			};

			runnable.runTaskTimer(main, 0, 20);
			this.spawnProt.put(player, runnable);
			spawnProtParticles(player, instance);
		}
	}

	/**
	 * This function spawns Spawn Protection particles around the player
	 *
	 * @param player
	 * @param instance
	 */
	private void spawnProtParticles(Player player, GameInstance instance) {
		int durationTicks = SPAWN_PROT_DURATION * 20; // 5 seconds in ticks
		double radius = 1.0;
		int particleCount = 20;

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= durationTicks) {
					cancel();
					return;
				}

				double angle = 2 * Math.PI * ticks / particleCount;
				double x = radius * Math.cos(angle);
				double z = radius * Math.sin(angle);

				for (double yOffset = 0; yOffset <= 2; yOffset += 0.1) { // Iterate through Y coordinates
					Location particleLoc = player.getLocation().clone().add(x, yOffset, z);

					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.ENCHANTMENT_TABLE,
							true, (float) particleLoc.getX(), (float) particleLoc.getY(), (float) particleLoc.getZ(),
							0F, 0F, 0F, 0F, 1);

					if (instance != null) {
						for (Player gamePlayer : instance.players)
							((CraftPlayer) gamePlayer).getHandle().playerConnection.sendPacket(packet);
					}
				}

				ticks++;
			}
		}.runTaskTimer(main, 0L, 1L);
	}

	@Override
	public void onPluginMessageReceived(String arg0, Player player, byte[] arg2) {
		if (!arg0.equalsIgnoreCase("BungeeCord"))
			return;

		ByteArrayDataInput in = ByteStreams.newDataInput(arg2);
		String sub = in.readUTF();
		if (sub.equalsIgnoreCase("PlayerCount")) {
			String server = in.readUTF();
			int num = in.readInt();
			playercount.put(server, num);
		}
		return;
	}

	@EventHandler
	public void onArmorBreak(PlayerItemDamageEvent event) {
		// Check if the item being damaged is a piece of armor
		if (event.getItem().getType().name().contains("HELMET")
				|| event.getItem().getType().name().contains("CHESTPLATE")
				|| event.getItem().getType().name().contains("LEGGINGS")
				|| event.getItem().getType().name().contains("BOOTS")) {

			// Cancel the event to prevent armor from breaking
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onSnowmanDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Snowman) {
			event.setCancelled(true); // Cancel all other damage
		}
	}

	// Prevent snow trails
	@EventHandler
	public void onEntityFormBlock(EntityBlockFormEvent event) {
		if (event.getEntity() instanceof Snowman)
			event.setCancelled(true);
	}

	// Prevent trampling
	@EventHandler
	public void onEntityInteract(EntityInteractEvent event) {
		if (event.getBlock().getType() == Material.SOIL) {
			event.setCancelled(true);
		}
	}

	// All item interactions:
	@EventHandler
	public void itemInteract(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		final Player player = event.getPlayer();
		GameInstance gameInstance = GetInstanceOfPlayer(player);
		GameInstance gameInstanceSpectator = GetInstanceOfSpectator(player);
		Action action = event.getAction();

		if (item == null)
			return;
		ItemMeta meta = item.getItemMeta();

		switch (item.getType()) {
		case COMPASS:
			if (player.getWorld() == this.main.getLobbyWorld()) {
				Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin) this.main, () -> {
					ByteArrayOutputStream b = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(b);
					try {
						out.writeUTF("PlayerCount");
						out.writeUTF("scb-1");
					} catch (Exception exc) {
						exc.printStackTrace();
					}
					player.sendPluginMessage((Plugin) this.main, "BungeeCord", b.toByteArray());
					b = new ByteArrayOutputStream();
					out = new DataOutputStream(b);
					try {
						out.writeUTF("PlayerCount");
						out.writeUTF("scb-2");
					} catch (Exception exc) {
						exc.printStackTrace();
					}
					player.sendPluginMessage((Plugin) this.main, "BungeeCord", b.toByteArray());
				}, 10L);
				(new GameSelectorGUI(this.main)).inv.open(player);
			}
		case ENCHANTED_BOOK:
			if (gameInstance != null) {
				if (gameInstance.state == GameState.WAITING) {
					PlayerData data = main.getDataManager().getPlayerData(player);
					for (ClassType type : ClassType.getAvailableClasses()) {
						ClassDetails details = data.playerClasses.get(type.getID());
						if (details == null) {
							details = new ClassDetails();
							data.playerClasses.put(type.getID(), details);
						}
					}
					(new ClassesGUI(this.main)).inv.open(player);
				} else if (((BaseClass) gameInstance.classes.get(player)).getLives() <= 0) {
					(new SpectatorGUI(this.main)).inv.open(player);
				}
			} else if (gameInstanceSpectator != null && gameInstanceSpectator.spectators.contains(player)
					&& player.getWorld() == gameInstanceSpectator.getMapWorld()) {
				(new SpectatorGUI(this.main)).inv.open(player);
			}
			break;
		case BARRIER:
			if ((gameInstance != null && gameInstance.classes.containsKey(player)
					&& ((BaseClass) gameInstance.classes.get(player)).getLives() <= 0) || gameInstanceSpectator != null)
				(new LeaveGameGUI(this.main)).inv.open(player);
			if (gameInstance != null && gameInstance.state == GameState.WAITING)
				new LeaveGameGUI(this.main).inv.open(player);
			break;
		case MONSTER_EGG:
			if (gameInstance != null && gameInstance.state == GameState.STARTED) {
				if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
					return;
				// Zombie Monster Egg
				if (meta.getDisplayName().toLowerCase().contains("zombie")
						&& !(meta.getDisplayName().toLowerCase().contains("pigman"))) {
					int amount = item.getAmount();

					if (amount > 0) {
						if (amount == 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							amount--;
							item.setAmount(amount);
						}
						ItemProjectile proj = new ItemProjectile(gameInstance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								@SuppressWarnings("deprecation")

								// Spawning Zombie
								Zombie zombie = (Zombie) player.getWorld().spawnCreature(hitLoc, EntityType.ZOMBIE);
								// Customizing Zombie
								customizeMob(zombie, player);
								customizeZombie(zombie);
								zombie.setTarget(gameInstance.getNearestPlayer(player, zombie, 150));

								// If ClassType == Summoner
								if (gameInstance.classes.get(player).getType() == ClassType.Summoner) {
									// Spawning Second Zombie
									Zombie zombie2 = (Zombie) player.getWorld().spawnCreature(hitLoc.add(1, 0, 1),
											EntityType.ZOMBIE);
									// Customizing Second Zombie
									customizeMob(zombie2, player);
									customizeZombie(zombie2);
									zombie2.setTarget(gameInstance.getNearestPlayer(player, zombie2, 150));
								}
							}

						}, ItemHelper.createMonsterEgg(EntityType.ZOMBIE, 1));
						gameInstance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
					// Skeleton Monster Egg
				} else if (meta.getDisplayName().toLowerCase().contains("skeleton")) {
					int amount = item.getAmount();

					if (amount > 0) {
						if (amount == 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							amount--;
							item.setAmount(amount);
						}
						ItemProjectile proj = new ItemProjectile(gameInstance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								@SuppressWarnings("deprecation")

								// Spawning Skeleton
								Skeleton skeleton = (Skeleton) player.getWorld().spawnCreature(hitLoc,
										EntityType.SKELETON);
								// Customizing Skeleton
								customizeMob(skeleton, player);
								customizeSkeleton(skeleton);
								skeleton.setTarget(gameInstance.getNearestPlayer(player, skeleton, 150));

								// If ClassType == Summoner
								if (gameInstance.classes.get(player).getType() == ClassType.Summoner) {
									// Spawning Second Skeleton
									Skeleton skeleton2 = (Skeleton) player.getWorld().spawnCreature(hitLoc.add(1, 0, 1),
											EntityType.SKELETON);
									// Customizing Second Skeleton
									customizeMob(skeleton2, player);
									customizeSkeleton(skeleton2);
									skeleton2.setTarget(gameInstance.getNearestPlayer(player, skeleton2, 150));
								}
							}
						}, ItemHelper.createMonsterEgg(EntityType.SKELETON, 1));
						gameInstance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
					// Witch Monster Egg
				} else if (meta.getDisplayName().toLowerCase().contains("witch")) {
					int amount = item.getAmount();

					if (amount > 0) {
						if (amount == 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							amount--;
							item.setAmount(amount);
						}
						ItemProjectile proj = new ItemProjectile(gameInstance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								@SuppressWarnings("deprecation")

								// Spawning Witch
								Witch witch = (Witch) player.getWorld().spawnCreature(hitLoc, EntityType.WITCH);
								// Customizing Witch
								customizeMob(witch, player);
								witch.setTarget(gameInstance.getNearestPlayer(player, witch, 150));
							}

						}, ItemHelper.createMonsterEgg(EntityType.WITCH, 1));
						gameInstance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
					// Creeper Monster Egg
				} else if (meta.getDisplayName().toLowerCase().contains("creeper")) {
					int amount = item.getAmount();

					if (amount > 0) {
						if (amount == 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							amount--;
							item.setAmount(amount);
						}
						ItemProjectile proj = new ItemProjectile(gameInstance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();
								@SuppressWarnings("deprecation")

								// Spawning Creeper
								Creeper creeper = (Creeper) player.getWorld().spawnCreature(hitLoc, EntityType.CREEPER);
								// Customizing Creeper
								customizeMob(creeper, player);
								customizeCreeper(creeper);
								creeper.setTarget(gameInstance.getNearestPlayer(player, creeper, 150));

								// If ClassType == Summoner
								// Setting to Charged Creeper
								if (gameInstance.classes.get(player).getType() == ClassType.Summoner) {
									creeper.setPowered(true);
								}

							}

						}, ItemHelper.createMonsterEgg(EntityType.CREEPER, 1));
						gameInstance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				} else if (meta.getDisplayName().toLowerCase().contains("slime")) {
					int amount = item.getAmount();

					if (amount > 0) {
						if (amount == 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							amount--;
							item.setAmount(amount);
						}
						ItemProjectile proj = new ItemProjectile(gameInstance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();

								// Spawning Slime
								Slime mob = (Slime) player.getWorld().spawnCreature(hitLoc, EntityType.SLIME);
								mob.setRemoveWhenFarAway(false);
								// Setting Mob Name to owner's
								mob.setCustomName(ChatColorHelper
										.color("&c" + player.getName() + "'s &e" + getMobTypeName(mob.getType())));
								// Setting Custom name visible
								mob.setCustomNameVisible(true);
								// Setting Slime Size
								mob.setSize(4);
							}

						}, ItemHelper.createMonsterEgg(EntityType.SLIME, 1));
						gameInstance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				} else if (meta.getDisplayName().toLowerCase().contains("silverfish")) {
					int amount = item.getAmount();

					if (amount > 0) {
						if (amount == 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							amount--;
							item.setAmount(amount);
						}
						ItemProjectile proj = new ItemProjectile(gameInstance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();

								for (int i = 0; i < 3; i++) {
									// Spawning Silverfish
									Silverfish mob = (Silverfish) player.getWorld().spawnCreature(hitLoc, EntityType.SILVERFISH);
									customizeMob(mob, player);
									mob.setTarget(gameInstance.getNearestPlayer(player, mob, 150));
								}
							}

						}, ItemHelper.createMonsterEgg(EntityType.SILVERFISH, 1));
						gameInstance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				} else if (meta.getDisplayName().toLowerCase().contains("spider")) {
					int amount = item.getAmount();

					if (amount > 0) {
						if (amount == 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else {
							amount--;
							item.setAmount(amount);
						}
						ItemProjectile proj = new ItemProjectile(gameInstance, player, new ProjectileOnHit() {
							@Override
							public void onHit(Player hit) {
								Location hitLoc = this.getBaseProj().getEntity().getLocation();

								// Spawning Spider
								Spider mob = (Spider) player.getWorld().spawnCreature(hitLoc, EntityType.SPIDER);
								customizeMob(mob, player);
								customizeSpider(mob);
								mob.setTarget(gameInstance.getNearestPlayer(player, mob, 150));
							}

						}, ItemHelper.createMonsterEgg(EntityType.SPIDER, 1));
						gameInstance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
								player.getLocation().getDirection().multiply(2.0D));
					}
				}
			}
			break;

		case NETHER_STAR:
			if (gameInstance != null && gameInstance.state == GameState.STARTED && meta != null && meta.hasDisplayName()
					&& meta.getDisplayName().toLowerCase().contains("bounty")) {
				if (gameInstance.alivePlayers > 1) {
					int amount = item.getAmount();
					if (amount > 0) {
						if (amount == 1) {
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						} else {
							amount--;
							item.setAmount(amount);
						}
						if (gameInstance.classes.containsKey(player)) {
							BaseClass bc = gameInstance.classes.get(player);
							if (bc != null)
								while (true) {
									Random random = new Random();
									int index = random.nextInt(gameInstance.players.size());
									Player target = gameInstance.players.get(index);
									if (target != null && target != player && gameInstance.classes.containsKey(target)
											&& ((BaseClass) gameInstance.classes.get(target)).getLives() > 0) {
										bc.bountyTarget = target;
										player.sendMessage(this.main.color("&2&l(!) &e&lBOUNTY SET! &rKill &e"
												+ target.getName() + " &rfor 25 Token reward!"));
										target.sendMessage(
												this.main.color("&2&l(!) &e&lBOUNTY SET! &rYou are being targeted!"));
										player.sendTitle(this.main.color("&e&lBOUNTY"),
												this.main.color("&rYou are targetting &e" + target.getName()));
										target.sendTitle(this.main.color("&e&lBOUNTY"),
												this.main.color("&rYou are being targetted!"));
										player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
										target.playSound(target.getLocation(), Sound.WITHER_SPAWN, 1, 0);
										break;
									}
								}
						}
					}
				} else {
					player.sendMessage(this.main.color("&2&l(!) &rNo players found!"));
				}
			}
			break;
		}
	}

	public Player getMobOwner(Creature creature) {
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

	private void customizeSkeleton(Skeleton skeleton) {
		// Setting Bow
		EntityEquipment equipment = skeleton.getEquipment();
		// Setting Punch
		ItemStack punchBow = ItemHelper.create(Material.BOW);
		punchBow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
		// Setting Unbreakable
		ItemHelper.setUnbreakable(punchBow);

		equipment.setItemInHand(punchBow);
	}

	private void customizeZombie(Zombie zombie) {
		// Setting adult zombie
		zombie.setBaby(false);

		// Setting full unbreakable diamond armor
		EntityEquipment equipment = zombie.getEquipment();

		// Helmet
		ItemStack helmet = ItemHelper.create(Material.DIAMOND_HELMET);
		ItemHelper.setUnbreakable(helmet);
		// Chestplate
		ItemStack chestplate = ItemHelper.create(Material.DIAMOND_CHESTPLATE);
		ItemHelper.setUnbreakable(chestplate);
		// Leggings
		ItemStack leggings = ItemHelper.create(Material.DIAMOND_LEGGINGS);
		ItemHelper.setUnbreakable(leggings);
		// Boots
		ItemStack boots = ItemHelper.create(Material.DIAMOND_BOOTS);
		ItemHelper.setUnbreakable(boots);

		equipment.setHelmet(helmet);
		equipment.setChestplate(chestplate);
		equipment.setLeggings(leggings);
		equipment.setBoots(boots);

		// Setting unbreakable diamond sword
		ItemStack sword = ItemHelper.create(Material.DIAMOND_SWORD);
		ItemHelper.setUnbreakable(sword);

		equipment.setItemInHand(sword);
	}

	private void customizeCreeper(Creeper creeper) {
		creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
	}

	private void customizeSpider(Spider spider) {
		spider.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
	}

	private void customizeMob(Creature mob, Player player) {
		// Setting Mob to not de-spawn when far away
		mob.setRemoveWhenFarAway(false);
		// Setting Mob Name to owner's
		mob.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &e" + getMobTypeName(mob.getType())));
		// Setting Custom name visible
		mob.setCustomNameVisible(true);
	}

	public String getMobTypeName(EntityType entityType) {
		switch (entityType) {
		case SKELETON:
			return "Skeleton";
		case CREEPER:
			return "Creeper";
		case ZOMBIE:
			return "Zombie";
		case WITCH:
			return "Witch";
		case SILVERFISH:
			return "Silverfish";
		case ENDERMITE:
			return "Endermite";
		case WOLF:
			return "Wolf";
		case MAGMA_CUBE:
			return "Magma Cube";
		case PIG_ZOMBIE:
			return "Zombie Pigman";
		case SPIDER:
			return "Spider";
		case SLIME:
			return "Slime";
		}
		return "Creature";
	}
}
