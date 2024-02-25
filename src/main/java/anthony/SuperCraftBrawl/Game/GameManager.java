package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.map.DuosMaps;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileManager;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerFishEvent.State;
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

	public GameManager(Core main) {
		this.main = main;
		this.playercount = new HashMap<>();
		this.gameMap = new HashMap<>();
		this.gameMap2 = new HashMap<>();
		this.spawnProt = new HashMap<>();
		this.main.getServer().getPluginManager().registerEvents(this, main);
		this.projManager = new ProjectileManager(this);
	}

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
		}
	}

	/*
	 * @EventHandler public void throwTnt(PlayerInteractEvent event) { ItemStack
	 * item = event.getItem(); Player player = event.getPlayer();
	 * 
	 * if (item != null && item.getType() == Material.TNT) { if (event.getAction()
	 * == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	 * TNTPrimed tnt = (TNTPrimed) player.getWorld().spawn(player.getEyeLocation(),
	 * TNTPrimed.class);
	 * tnt.setVelocity(player.getEyeLocation().getDirection().multiply(1));
	 * tnt.setFuseTicks(30); int amount =
	 * player.getInventory().getItemInHand().getAmount(); amount--; if (amount == 0)
	 * { player.getInventory().removeItem(new ItemStack[] { new
	 * ItemStack(Material.TNT) }); } else
	 * player.getInventory().getItemInHand().setAmount(amount); } } }
	 */

	@EventHandler
	public void onTestEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			GameInstance instance = this.GetInstanceOfPlayer(player);
			if (player.getWorld() == main.getLobbyWorld())
				event.setCancelled(true);

			if (instance != null) {
				if (instance.state == GameState.STARTED) {
					if (instance.classes.containsKey(player) && instance.classes.get(player).fadeAbilityActive == true) {
						event.setCancelled(true);
					}
					if (instance.classes.containsKey(player) && instance.classes.get(player).getLives() <= 0)
						event.setCancelled(true);
					else
						event.setCancelled(false);
				} else
					event.setCancelled(true);
			} else {
				instance = this.GetInstanceOfSpectator(player);

				if (instance != null) {
					if (instance.spectators.contains(player) && player.getWorld() == instance.getMapWorld()) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	public ProjectileManager getProjManager() {
		return projManager;
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

	@EventHandler
	public void onEntityDamage(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		GameInstance instance = this.GetInstanceOfPlayer(player);
		BaseClass baseClass = null;
		if (instance != null)
			baseClass = instance.classes.get(player);

		if (instance != null && baseClass != null)
			if (event.getCause() == TeleportCause.ENDER_PEARL) {
				event.setCancelled(true);
				if (instance.isInBounds(event.getTo())) {
					if (baseClass.isDead == true) {
						if (event.getTo() == null || event.getTo() != null) {

						}
					} else {
						player.teleport(event.getTo());

						for (Player gamePlayer : instance.players) {
							if (player != gamePlayer) {
								int radius = 7;
								if (!(player.getLocation().distance(gamePlayer.getLocation()) <= radius)) {
									EntityDamageEvent damageEvent = new EntityDamageEvent(player,
											DamageCause.PROJECTILE, 1.5);
									instance.getGameManager().getMain().getServer().getPluginManager()
											.callEvent(damageEvent);
									player.damage(1.5);
								}
							}
						}
					}
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You cannot teleport there!");
					player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
				}
			}
	}

	public int getNumOfGames() {
		int num = 0;
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().state == GameState.STARTED) {
				num++;
			}
		}
		return num;
	}

	public int getNumOfGamesFrenzy() {
		int num = 0;
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().state == GameState.STARTED && entry.getValue().gameType == GameType.FRENZY) {
				num++;
			}
		}
		return num;
	}

	public int getNumOfGamesNormal() {
		int num = 0;
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().state == GameState.STARTED && entry.getValue().gameType == GameType.CLASSIC) {
				num++;
			}
		}
		return num;
	}

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

	public int getActiveGames() {
		int count = 0;
		for (Entry<Maps, GameInstance> games : gameMap.entrySet()) {
			if (games.getValue().state == GameState.WAITING || games.getValue().state == GameState.STARTED) {
				count++;
				mapName = games.getValue().getMap().toString();
			}
		}
		return count;
	}

	public String getActiveMapName() {
		String mapName = "";
		for (Entry<Maps, GameInstance> games : gameMap.entrySet()) {
			if (games.getValue().state == GameState.WAITING || games.getValue().state == GameState.STARTED) {
				mapName = games.getValue().getMap().toString();
			}
		}

		return mapName;
	}

	@EventHandler
	public void MobBurn(EntityCombustEvent event) {
		if (event.getEntityType() == EntityType.ZOMBIE || event.getEntityType() == EntityType.SKELETON)
			event.setCancelled(true);
	}

	public List<Block> pBlocks = new ArrayList<Block>();
	public List<Material> pMat = new ArrayList<Material>();
	public BukkitRunnable pRunnable;

	@EventHandler
	public void present(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null) {
			if (instance.state == GameState.STARTED) {
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
							player.getInventory().addItem(instance.getItemToDrop());
							player.getInventory().addItem(instance.getItemToDrop());
							player.sendMessage(
									main.color("&r(&c&l!&r&l) &c&lMerry Christmas! &rYou received 2 items!"));
						}
					}
				}
			}

		}
	}

	@EventHandler
	public void easterEgg(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null) {
			if (instance.state == GameState.STARTED) {
				if (item != null) {
					if (item.getType() == Material.EGG && (event.getAction() == Action.RIGHT_CLICK_AIR
							|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
						ItemMeta meta = item.getItemMeta();

						if (meta.getDisplayName().contains("Easter")) {
							Random r = new Random();
							int chance = r.nextInt(100);
							player.getInventory().clear(player.getInventory().getHeldItemSlot());

							if (chance >= 0 && chance < 30) {
								player.sendMessage(getMain().color("&2&l(!) &rYour Easter gift is: &r&lExtra Life"));
								player.getInventory()
										.addItem(ItemHelper.setDetails(new ItemStack(Material.PRISMARINE_SHARD),
												"" + ChatColor.RESET + ChatColor.BOLD + "Extra Life"));
							} else if (chance >= 30 && chance < 65) {
								player.sendMessage(getMain().color("&2&l(!) &rYour Easter gift is: &c&lBomb"));
								player.getInventory().addItem(ItemHelper.setDetails(new ItemStack(Material.POTION, 1),
										"" + ChatColor.RED + ChatColor.BOLD + "Bomb"));
							} else {
								player.sendMessage(getMain().color("&2&l(!) &rYour Easter gift is: &0&lMagic Broom"));
								player.getInventory().addItem(ItemHelper.setDetails(new ItemStack(Material.WHEAT, 4),
										this.getMain().color("&0&lBroom")));
							}
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

		if (player.getWorld() == main.getLobbyWorld()) {
			if (e.getPlayer().getLocation().getY() < 0) {
				main.SendPlayerToHub(player);
			}
		}
		if (instance != null) {
			if (instance.state == GameState.STARTED) {
				if (e.getPlayer().getLocation().getY() < 50 && e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
					EntityDamageEvent damageEvent = new EntityDamageEvent(e.getPlayer(), DamageCause.VOID, 1000);
					main.getServer().getPluginManager().callEvent(damageEvent);
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
		} /*
			 * else { anthony.CrystalWars.game.GameInstance i =
			 * main.gm.getInstanceOfPlayer(player);
			 * 
			 * if (i != null) { anthony.CrystalWars.game.GameState state = i.getState(); if
			 * (state == anthony.CrystalWars.game.GameState.IN_PROGRESS) { if
			 * (player.getLocation().getY() < 50 && player.getGameMode() !=
			 * GameMode.SPECTATOR) { EntityDamageEvent damageEvent = new
			 * EntityDamageEvent(player, DamageCause.VOID, 1000);
			 * main.getServer().getPluginManager().callEvent(damageEvent); } } } }
			 */
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
							+ "This mystery chest is already in use!");
				}
			}
		} else {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK && list.contains(e.getClickedBlock().getType())) {
				// e.setCancelled(true);
				// REMOVE LATER
			}
		}

	}

	private Cooldown boosterCooldown = new Cooldown(0), shurikenCooldown = new Cooldown(3000);

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
			if (item != null) {
				if (item.getType() == Material.GOLD_HOE && (event.getAction() == Action.RIGHT_CLICK_AIR
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);
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
						if (p != player) {
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
										i.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
										p.damage(damage, player);
									}
									p.setVelocity(new Vector(0, 1, 0).multiply(height));
								}
							}
						}
					}
				} else if (item.getType() == Material.WATER_BUCKET && (event.getAction() == Action.RIGHT_CLICK_AIR
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
					player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1000, 1));
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 125, 1));
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

	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		GameInstance gameInstance = GetInstanceOfPlayer(player);
		gameInstance.classes.get(player).onPlayerMove(event);
	}

	@EventHandler
	public void shieldPotions(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		ItemMeta meta = item.getItemMeta();

		if (item != null) {
			if (item.getType() == Material.POTION) {
				if (meta.getDisplayName().contains("Mini-Shield Potion")) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1000, 0));
					event.setCancelled(true);
					player.getInventory().clear(player.getInventory().getHeldItemSlot());
				}
			}
		}
	}

	@EventHandler
	public void EntityDeathEvent(EntityDeathEvent entity) {
		List<EntityType> entities = new ArrayList<>(Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON,
				EntityType.CREEPER, EntityType.PIG_ZOMBIE, EntityType.MAGMA_CUBE, EntityType.SILVERFISH,
				EntityType.WITCH));
		if (entities.contains(entity.getEntityType())) {
			entity.getDrops().clear();
			entity.setDroppedExp(0);
		}
	}

	@EventHandler
	public void blooper(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (item != null && item.getType() == Material.RABBIT_FOOT
				&& event.getAction().toString().contains("RIGHT_CLICK")) {
			ItemMeta meta = item.getItemMeta();
			if (meta.getDisplayName().contains("Blooper")) {
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
							player.sendMessage(main.color("&6&l(!) &rYou blooped &e" + target.getName()));
							target.sendMessage(main.color("&6&l(!) &rYou were blooped by &e" + player.getName()));
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
				player.sendMessage(main.color("&6&l(!) &rYou tried blooping a Spectator"));
			}
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent entity) {
		if (entity.getEntityType() == EntityType.ZOMBIE || entity.getEntityType() == EntityType.SKELETON) {
			entity.getDrops().clear();
			entity.setDroppedExp(0);
		}
	}

	@EventHandler
	public void votePaper(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		GameInstance instance = this.GetInstanceOfPlayer(player);
		PlayerData data = main.getDataManager().getPlayerData(player);

		if (instance != null) {
			if (instance.state == GameState.WAITING && instance.players.size() >= 2) {
				if (item != null && item.getType() == Material.PAPER && (event.getAction() == Action.RIGHT_CLICK_AIR
						|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					if (data.votes == 0) {
						instance.totalVotes++;
						data.votes = 1;
						instance.TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ ChatColor.YELLOW + player.getName() + ChatColor.YELLOW + ChatColor.BOLD + " is Ready "
								+ ChatColor.RED + "(" + ChatColor.GREEN + instance.totalVotes + "/"
								+ instance.players.size() + ChatColor.RED + ")");
						player.getInventory().remove(Material.PAPER);
						player.getInventory().addItem(instance.paper2);
					} else if (data.votes == 1) {
						instance.totalVotes--;
						data.votes = 0;
						instance.TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ ChatColor.YELLOW + player.getName() + ChatColor.RED + ChatColor.BOLD
								+ " is no longer Ready " + ChatColor.RED + "(" + ChatColor.GREEN + instance.totalVotes
								+ "/" + instance.players.size() + ChatColor.RED + ")");
						player.getInventory().remove(Material.PAPER);
						player.getInventory().addItem(instance.paper);
					}
				}
			}
		}
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
							player.sendMessage(getMain().color("&9&l(!) &rYou are now level &e" + data.level));
						}
					}
					BaseClass baseClass = instance.classes.get(player);
					int amount = item.getAmount();
					baseClass.lives += 1;
					baseClass.score.setScore(baseClass.lives);
					baseClass.TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ ChatColor.YELLOW + player.getName() + ChatColor.RESET + " used an extra life!");
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
	public void onFish(PlayerFishEvent e) {
		if (boosterCooldown.useAndResetCooldown()) {
			if (e.getState() == State.IN_GROUND) {
				e.getPlayer().setVelocity(
						new Vector(e.getPlayer().getVelocity().getX(), 2.5, e.getPlayer().getVelocity().getY()));
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
					if (shurikenCooldown.useAndResetCooldown()) {
						if (data.melon > 0) {
							data.melon--;
							main.getDataManager().saveData(data);
							String msg = main.color("&9&l(!) &rYou have &e" + data.melon + " melons");
							PacketPlayOutChat packet = new PacketPlayOutChat(
									ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
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
	}

	@EventHandler
	public void tntChange(EntityChangeBlockEvent event) {
		Entity e = event.getEntity();

		if (e instanceof Arrow)
			if (event.getBlock().getType() == Material.TNT)
				event.setCancelled(true);
	}

	@EventHandler
	public void onInv(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		//if (instance != null) {
			/*
			 * if (e.getCurrentItem().getType() == Material.COAL) { e.setCancelled(true); }
			 * else if (e.getCurrentItem().getType() == Material.IRON_INGOT) {
			 * e.setCancelled(true); } else if (e.getCurrentItem().getType() ==
			 * Material.GOLD_INGOT) { e.setCancelled(true); } else if
			 * (e.getCurrentItem().getType() == Material.DIAMOND) { e.setCancelled(true); }
			 */
			if (!(player.isOp()))
				e.setCancelled(true);
		//}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void bazooka(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null && instance.state == GameState.STARTED) {
			BaseClass bc = instance.classes.get(player);
			if (item != null && item.getType() == Material.DIAMOND_HOE) {
				if (player.getGameMode() != GameMode.SPECTATOR) {
					if (bc != null) {
						if (bc.bazooka.getTime() < 3000) {
							int seconds = (3000 - bc.bazooka.getTime()) / 1000 + 1;
							e.setCancelled(true);
							player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "Your Bazooka is still regenerating for " + ChatColor.YELLOW + seconds
									+ " more seconds ");
						} else {
							bc.bazooka.restart();
							item.setAmount(item.getAmount() - 1);

							if (item.getAmount() == 0)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
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
						e.setCancelled(true);
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
					// Remove bad effects only: poison, wither, slowness, weakness, blindness,
					// nausea
					for (PotionEffect pe : player.getActivePotionEffects())
						if (pe.getType().equals(PotionEffectType.POISON)
								|| pe.getType().equals(PotionEffectType.SLOW)
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
					if (bc != null && bc.getType() != ClassType.BabyCow) { // BabyCow milk bucket has its own behaviour
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

	@EventHandler
	public void activeGames(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (e.getItem() != null && e.getItem().getType() == Material.EYE_OF_ENDER) {
			e.setCancelled(true);
			if (player.getWorld() == main.getLobbyWorld())
				new ActiveGamesGUI(getMain()).inv.open(player);
			else if (i != null)
				e.setCancelled(true);
		}

	}

	@EventHandler
	public void OnPickupItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		if (projManager.isProjectile(item)) {
			event.setCancelled(true);
		}
	}

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
							hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 2)); // Slowness 3
																											// -
																											// Snowgolem
						else
							hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 0)); // Slowness 1
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
		} else {
			player.sendMessage(
					"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "All games are full! Please try again later");
		}
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
								if (instance.classes.containsKey(damager) && instance.classes.get(damager).fadeAbilityActive == true) {
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
						Arrow damager = (Arrow) damageEvent.getDamager();
						if (damager.getShooter() instanceof Player) {
							Player p = (Player) damager.getShooter();
							BaseClass bc = instance.classes.get(p);
							BaseClass pBc = instance.classes.get(player);

							if (instance.duosMap != null) {
								if (instance.team.get(p).equals(instance.team.get(player))) {
									event.setCancelled(true);
									return;
								}
							}

							if (bc != null && pBc != null) {
								if (bc.getType() == ClassType.Vampire || bc.getType() == ClassType.WitherSk
										|| bc.getType() == ClassType.Shulker || bc.getType() == ClassType.Firework
										|| bc.getType() == ClassType.Skeleton) {
									if (this.spawnProt.containsKey(p) || bc.bedrockInvincibility == true) {
										event.setCancelled(true);
										return;
									}
									if (this.spawnProt.containsKey(player) || pBc.bedrockInvincibility == true) {
										event.setCancelled(true);
										return;
									}
									player.setLastDamageCause(new EntityDamageByEntityEvent(p, player, event.getCause(),
											event.getDamage()));
									bc.DoDamage(damageEvent);
								}
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
				} else
					if (instance.classes.containsKey(player))
						instance.classes.get(player).TakeDamage(event);
			}
		}
	}

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
		Sign s = (Sign) e.getBlock().getState();
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
				s.setLine(3, main.color("&0" + instance.ticksTilStart + "s"));
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

			player.sendMessage(
					"" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You are already in a map!");

			break;

		case IN_ANOTHER:
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You are already in a game!");
			break;

		case ALREADYPLAYING:
			player.sendMessage(
					"" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + "This game is already playing!");
			break;
		}
	}

	public void JoinMap(Player player, Maps map) {
		GameReason result = main.getGameManager().AddPlayerToMap(player, map);
		GameInstance instance = this.GetInstanceOfPlayer(player);
		MapInstance mi = map.GetInstance();
		Vector v = new Vector(0, 100, 0);
		Vector newV = new Vector(mi.signLoc.getX(), mi.signLoc.getY(), mi.signLoc.getZ());

		Location loc = new Location(main.getLobbyWorld(), mi.signLoc.getX(), mi.signLoc.getY(), mi.signLoc.getZ());
		Block b = main.getLobbyWorld().getBlockAt(loc);

		if (b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
			if (instance != null) {
				Sign s = (Sign) b.getState();
				instance.setSign(s);
				s.setLine(2, main.color("&0Players: " + instance.players.size() + "/"
						+ instance.getMap().GetInstance().gameType.getMaxPlayers()));
				s.setLine(3, main.color("&0" + instance.ticksTilStart + "s"));
				s.update();
			}
		}
		switch (result) {
		case SUCCESS:
			if (instance.gameType == GameType.FRENZY) {
				player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have joined a Frenzy game, your class will be randomly selected each life");
			}

			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(true);

			if (player.getWorld() != main.getLobbyWorld()) {
				player.getInventory().clear();
				if (instance.gameType != GameType.FRENZY) {
					ItemStack classItem = ItemHelper.setDetails(new ItemStack(Material.COMPASS),
							"" + ChatColor.GREEN + ChatColor.BOLD + "Class Selector",
							ChatColor.GRAY + "Click to choose a class!");
					player.getInventory().setItem(0, classItem);
				}

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
			player.sendMessage(
					"" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You are already in a map!");
			break;

		case IN_ANOTHER:
			player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You are already in a game!");
			break;

		case ALREADYPLAYING:
			player.sendMessage(
					"" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + "This game is already playing!");
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
			player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have to leave your game to Spectate");
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
			ItemStack spec = ItemHelper.setDetails(new ItemStack(Material.COMPASS),
					"" + ChatColor.GREEN + "Spectate a Player",
					ChatColor.GRAY + "Click to Spectate a specific player!");
			player.getInventory().setItem(0, spec);
			ItemStack leave = ItemHelper.setDetails(new ItemStack(Material.BARRIER), "" + ChatColor.RED + "Leave",
					ChatColor.GRAY + "Click to leave game");
			player.getInventory().setItem(8, leave);
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

	public GameReason AddPlayerToMap(Player player, Maps map) {
		GameInstance instance = null;

		if (GetInstanceOfPlayer(player) != null) {
			return GameReason.IN_ANOTHER;
		}
		if (gameMap.containsKey(map))
			instance = gameMap.get(map);
		else {
			instance = new GameInstance(this, map);
			gameMap.put(map, instance);
		}

		GameReason reason = instance.AddPlayer(player);

		return reason;
	}

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
			if (e.getDamager() instanceof Player) {
				Player player = (Player) e.getDamager();

				if (main.getCwManager() == null) {
					e.setCancelled(true);
				}
				anthony.CrystalWars.game.GameInstance i = main.getCwManager().getInstanceOfPlayer(player);

				if (i != null) {
					if (i.getTeam(player).equals("Blue")) {
						if (i.isInBlue(player.getLocation())) {
							player.sendMessage(main.color("&c&l(!) &rYou cannot destroy your own crystal!"));
							e.setCancelled(true);
						} else if (i.isInRed(player.getLocation())) {
							i.TellAll(main.color("&2&l(!) &r&lRed Crystal &rwas destroyed by &e" + player.getName()));

							for (Player p : i.getPlayers()) {
								if (i.getTeam(p).equals("Red")) {
									p.sendTitle(main.color("&cCRYSTAL DESTROYED"),
											main.color("&rYou will no longer respawn"));
									i.crystal.remove(p);
								}
							}

							e.setCancelled(false);
						}
					} else if (i.getTeam(player).equals("Red")) {
						if (i.isInRed(player.getLocation())) {
							player.sendMessage(main.color("&c&l(!) &rYou cannot destroy your own crystal!"));
							e.setCancelled(true);
						} else if (i.isInBlue(player.getLocation())) {
							i.TellAll(main.color("&2&l(!) &r&lBlue Crystal &rwas destroyed by &e" + player.getName()));

							for (Player p : i.getPlayers()) {
								if (i.getTeam(p).equals("Blue")) {
									p.sendTitle(main.color("&cCRYSTAL DESTROYED"),
											main.color("&rYou will no longer respawn"));
									i.crystal.remove(p);
								}
							}

							e.setCancelled(false);
						}
					}
				} else {
					e.setCancelled(true);
				}
			} else {
				e.setCancelled(true);
			}
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
	public void Nuke(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (i != null && i.state == GameState.STARTED) {
			if (item != null && item.getType() == Material.TNT) {
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
			((Explosive) event).setIsIncendiary(false);
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
						EntityDamageEvent damageEvent = new EntityDamageEvent(p, DamageCause.PROJECTILE, 15.0);
						getMain().getServer().getPluginManager().callEvent(damageEvent);
						p.damage(15.0, e);
					}
				}
			}
		} else if (event.getEntity() instanceof TNTPrimed) {
			event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.EXPLOSION_HUGE, 1);
			event.blockList().clear();
		}
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		/*
		 * Random r = new Random(); Vector v = new Vector(); v.setY(0.6);
		 * v.setZ(r.nextDouble());
		 * 
		 * for (Block b : event.blockList()) { if (b.getType().equals(Material.AIR)) {
		 * return; }
		 * 
		 * BlockState saved = b.getState(); b.setType(Material.AIR);
		 * 
		 * FallingBlock fb =
		 * Bukkit.getWorld("village").spawnFallingBlock(saved.getLocation(),
		 * saved.getType(), saved.getData().getData()); fb.setVelocity(v); }
		 */
	}

	/*
	 * @EventHandler public void onDmg(EntityDamageEvent event) { Player player =
	 * (Player) event.getEntity();
	 * 
	 * if (player instanceof Player) { GameInstance i =
	 * this.GetInstanceOfPlayer(player); BaseClass bc = i.classes.get(player);
	 * 
	 * if (i != null && i.state == GameState.STARTED) { if (bc != null) { if
	 * (bc.bedrockInvincibility) { event.setCancelled(true); } } } } }
	 * 
	 * @EventHandler public void onDmg(EntityDamageByEntityEvent event) { Player
	 * player = (Player) event.getDamager();
	 * 
	 * if (player instanceof Player) { GameInstance i =
	 * this.GetInstanceOfPlayer(player); BaseClass bc = i.classes.get(player);
	 * 
	 * if (i != null && i.state == GameState.STARTED) { if (bc != null) { if
	 * (bc.bedrockInvincibility) { event.setCancelled(true); } } } } }
	 */

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
		return gameMap.get(map);
	}

	public void RemovePlayerFromMap(Player player, Maps map, Player player2) {
		if (gameMap.containsKey(map))
			gameMap.get(map).RemovePlayer(player);
	}

	public boolean RemovePlayerFromAll(Player player) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);

		if (data.votes == 1) {
			if (instance != null) {
				instance.totalVotes--;
				data.votes = 0;
			}
		}

		boolean found = false;

		if (instance != null) {
			if (instance.getMap() != null) {
				List<Maps> toRemove = new ArrayList<>();

				for (Entry<Maps, GameInstance> games : gameMap.entrySet()) {
					if (games.getValue().RemovePlayer(player)) {
						if (games.getValue().players.size() == 0)
							toRemove.add(games.getKey());

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
		}

		return found;
	}

	public void RemoveMap(Maps maps) {
		gameMap.remove(maps);
	}

	public void RemoveDuosMap(DuosMaps maps) {
		gameMap2.remove(maps);
	}

	@EventHandler
	public void spawnProtection(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();

			if (event.getDamager() instanceof Player) {
				Player k = (Player) event.getDamager();
				GameInstance i = this.GetInstanceOfPlayer(k);

				if (i != null) {
					BaseClass bc = i.classes.get(k);
					BaseClass bc2 = i.classes.get(p);

					if (bc != null && bc2 != null) {
						if (bc.bedrockInvincibility == true) {
							event.setCancelled(true);
						} else if (bc2.bedrockInvincibility == true) {
							event.setCancelled(true);
						}
					}
				}

				if (this.spawnProt.containsKey(p)) {
					event.setCancelled(true);
				}
				if (this.spawnProt.containsKey(k)) {
					event.setCancelled(true);
				}

				// For spectators:
				if (i != null && i.classes.get(k).getLives() <= 0) {
					event.setCancelled(true);
				} else {
					i = this.GetInstanceOfSpectator(k);

					if (i != null && i.spectators.contains(k) && k.getWorld() == i.getMapWorld()) {
						event.setCancelled(true);
					}
				}
			} else {
				GameInstance i = this.GetInstanceOfPlayer(p);

				if (i != null) {
					BaseClass bc = i.classes.get(p);

					if (bc != null) {
						if (bc.bedrockInvincibility == true) {
							event.setCancelled(true);
						}
					}
				}
				if (this.spawnProt.containsKey(p)) {
					event.setCancelled(true);
				}
			}
		} else {
			if (event.getDamager() instanceof Player) {
				Player k = (Player) event.getDamager();
				GameInstance i = this.GetInstanceOfPlayer(k);

				if (i != null && i.classes.get(k).getLives() <= 0) {
					event.setCancelled(true);
				} else {
					i = this.GetInstanceOfSpectator(k);

					if (i != null && i.spectators.contains(k) && k.getWorld() == i.getMapWorld()) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	public void spawnProtection2(Player player) {
		BukkitRunnable r = this.spawnProt.get(player);
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (r == null) {
			r = new BukkitRunnable() {
				int ticks = 5;

				@Override
				public void run() {
					if (ticks == 0 || (i != null && i.state == GameState.ENDED)) {
						spawnProt.remove(player);
						this.cancel();
					}

					ticks--;
				}
			};
			r.runTaskTimer(main, 0, 20);
			this.spawnProt.put(player, r);
			spawnProtParticles(player, i);
		}
	}

	private void spawnProtParticles(Player player, GameInstance i) {
		int durationTicks = 5 * 20; // 5 seconds in ticks
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

					if (i != null) {
						for (Player gamePlayer : i.players)
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

	// All item interactions:
	@EventHandler
	public void itemInteract(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		final Player player = event.getPlayer();
		GameInstance i = GetInstanceOfPlayer(player);
		GameInstance i2 = GetInstanceOfSpectator(player);
		if (item != null) {
			ItemMeta meta = item.getItemMeta();
			switch (item.getType()) {
			case COMPASS:
				if (i != null) {
					if (i.state == GameState.WAITING) {
						(new ClassSelectorGUI(this.main)).inv.open(player);
					} else if (((BaseClass) i.classes.get(player)).getLives() <= 0) {
						(new SpectatorGUI(this.main)).inv.open(player);
					}
				} else if (i2 != null && i2.spectators.contains(player) && player.getWorld() == i2.getMapWorld()) {
					(new SpectatorGUI(this.main)).inv.open(player);
				}
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
				break;
			case BARRIER:
				if ((i != null && i.classes.containsKey(player) && ((BaseClass) i.classes.get(player)).getLives() <= 0)
						|| i2 != null)
					(new LeaveGameGUI(this.main)).inv.open(player);
				if (i != null && i.state == GameState.WAITING)
					new LeaveGameGUI(this.main).inv.open(player);
				break;
			case MONSTER_EGG:
				if (i != null && i.state == GameState.STARTED) {
					// Zombie Monster Egg
					if (meta.getDisplayName().contains("Zombie") && !(meta.getDisplayName().contains("Pigman"))) {
						int amount = item.getAmount();

						if (amount > 0) {
							if (amount == 1)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else {
								amount--;
								item.setAmount(amount);
							}
							ItemProjectile proj = new ItemProjectile(i, player, new ProjectileOnHit() {
								@Override
								public void onHit(Player hit) {
									Location hitLoc = this.getBaseProj().getEntity().getLocation();
									@SuppressWarnings("deprecation")

									// Spawning Zombie
									Zombie zombie = (Zombie) player.getWorld().spawnCreature(hitLoc, EntityType.ZOMBIE);
									// Customizing Zombie
									customizeMob(zombie, player);
									customizeZombie(zombie);

									// If ClassType == Summoner
									if (i.classes.get(player).getType() == ClassType.Summoner) {
										// Spawning Second Zombie
										Zombie zombie2 = (Zombie) player.getWorld().spawnCreature(hitLoc.add(1, 0, 1),
												EntityType.ZOMBIE);
										// Customizing Second Zombie
										customizeMob(zombie2, player);
										customizeZombie(zombie2);
									}
								}

							}, new ItemStack(Material.MONSTER_EGG));
							i.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
									player.getLocation().getDirection().multiply(2.0D));
						}
						// Skeleton Monster Egg
					} else if (meta.getDisplayName().contains("Skeleton")) {
						int amount = item.getAmount();

						if (amount > 0) {
							if (amount == 1)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else {
								amount--;
								item.setAmount(amount);
							}
							ItemProjectile proj = new ItemProjectile(i, player, new ProjectileOnHit() {
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

									// If ClassType == Summoner
									if (i.classes.get(player).getType() == ClassType.Summoner) {
										// Spawning Second Skeleton
										Skeleton skeleton2 = (Skeleton) player.getWorld()
												.spawnCreature(hitLoc.add(1, 0, 1), EntityType.SKELETON);
										// Customizing Second Skeleton
										customizeMob(skeleton2, player);
										customizeSkeleton(skeleton2);
									}
								}
							}, new ItemStack(Material.MONSTER_EGG));
							i.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
									player.getLocation().getDirection().multiply(2.0D));
						}
						// Witch Monster Egg
					} else if (meta.getDisplayName().contains("Witch Pokeball")) {
						int amount = item.getAmount();

						if (amount > 0) {
							if (amount == 1)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else {
								amount--;
								item.setAmount(amount);
							}
							ItemProjectile proj = new ItemProjectile(i, player, new ProjectileOnHit() {
								@Override
								public void onHit(Player hit) {
									Location hitLoc = this.getBaseProj().getEntity().getLocation();
									@SuppressWarnings("deprecation")

									// Spawning Witch
									Witch witch = (Witch) player.getWorld().spawnCreature(hitLoc, EntityType.WITCH);
									// Customizing Witch
									customizeMob(witch, player);
								}

							}, new ItemStack(Material.MONSTER_EGG));
							i.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
									player.getLocation().getDirection().multiply(2.0D));
						}
						// Creeper Monster Egg
					} else if (meta.getDisplayName().contains("Creeper")) {
						int amount = item.getAmount();

						if (amount > 0) {
							if (amount == 1)
								player.getInventory().clear(player.getInventory().getHeldItemSlot());
							else {
								amount--;
								item.setAmount(amount);
							}
							ItemProjectile proj = new ItemProjectile(i, player, new ProjectileOnHit() {
								@Override
								public void onHit(Player hit) {
									Location hitLoc = this.getBaseProj().getEntity().getLocation();
									@SuppressWarnings("deprecation")

									// Spawning Creeper
									Creeper creeper = (Creeper) player.getWorld().spawnCreature(hitLoc,
											EntityType.CREEPER);
									// Customizing Creeper
									customizeMob(creeper, player);
									customizeCreeper(creeper);

									// If ClassType == Summoner
									// Setting to Charged Creeper
									if (i.classes.get(player).getType() == ClassType.Summoner) {
										creeper.setPowered(true);
									}

								}

							}, new ItemStack(Material.MONSTER_EGG));
							i.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
									player.getLocation().getDirection().multiply(2.0D));
						}
					}
				}
				break;

			case NETHER_STAR:
				if (i != null && i.state == GameState.STARTED && meta != null && meta.getDisplayName().contains("Bounty")) {
					int amount = item.getAmount();
					if (amount > 0) {
						if (amount == 1) {
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						} else {
							amount--;
							item.setAmount(amount);
						}
						if (i.classes.containsKey(player)) {
							BaseClass bc = i.classes.get(player);
							if (bc != null)
								while (true) {
									Random random = new Random();
									int index = random.nextInt(i.players.size());
									Player target = i.players.get(index);
									if (target != null && target != player && i.classes.containsKey(target)
											&& ((BaseClass) i.classes.get(target)).getLives() > 0) {
										bc.bountyTarget = target;
										player.sendMessage(this.main.color("&2&l(!) &e&lBOUNTY SET! &rKill &e"
												+ target.getName() + " &rfor 25 Token award!"));
										target.sendMessage(
												this.main.color("&2&l(!) &e&lBOUNTY SET! &rYou are being targeted!"));
										player.sendTitle(this.main.color("&e&lBOUNTY"),
												this.main.color("&rYou are targetting &e" + target.getName()));
										target.sendTitle(this.main.color("&e&lBOUNTY"),
												this.main.color("&rYou are being targetted!"));
										break;
									}
								}
						}
					}
				}
				break;
			}
		}
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
		creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, false, false));
	}

	private void customizeMob(Creature mob, Player player) {
		// Setting Mob to not de-spawn when far away
		mob.setRemoveWhenFarAway(false);
		// Setting Mob Name to owner's
		mob.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &e" + getMobTypeName(mob.getType())));
		// Setting Custom name visible
		mob.setCustomNameVisible(true);
	}

	private String getMobTypeName(EntityType entityType) {
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
		}
		return null;
	}
}
