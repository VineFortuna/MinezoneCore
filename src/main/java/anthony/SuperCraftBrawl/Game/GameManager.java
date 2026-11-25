package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.christmas.ReindeerFlight;
import anthony.SuperCraftBrawl.Game.classes.all.GrinchClass;
import anthony.SuperCraftBrawl.Game.classes.all.SantaClass;
import anthony.SuperCraftBrawl.Game.map.DuosMaps;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileManager;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.gui.games.CustomGamesGUI;
import anthony.SuperCraftBrawl.gui.games.GameModifiersGUI;
import anthony.SuperCraftBrawl.gui.games.OneForAllClassGUI;
import anthony.SuperCraftBrawl.gui.games.OneForAllVotesGUI;
import anthony.SuperCraftBrawl.party.Party;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
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
	public List<GameInstance> games; // every (non-Duos) game - a map can have several, each on its own world copy
	public HashMap<DuosMaps, GameInstance> gameMap2;
	public HashMap<Player, BukkitRunnable> spawnProt;
	public static HashMap<String, Integer> playercount;

	private final ProjectileManager projManager;
	private final CustomGameMaps customGameMaps; // each player's default custom game map
	private final Core main;
	public GameState state;
	public GameType gameType;

	private static final int SPAWN_PROT_DURATION = 5;

	public GameManager(Core main) {
		this.main = main;
		this.playercount = new HashMap<>();
		this.games = new ArrayList<>();
		this.gameMap2 = new HashMap<>();
		this.spawnProt = new HashMap<>();
		this.main.getServer().getPluginManager().registerEvents(this, main);
		this.main.getServer().getPluginManager().registerEvents(new FakeLootListener(main), main); // the Grinch's fake drops
		this.projManager = new ProjectileManager(this);
		this.customGameMaps = new CustomGameMaps(main);
	}

	public CustomGameMaps getCustomGameMaps() {
		return customGameMaps;
	}

	// GETTERS:

	public ProjectileManager getProjManager() {
		return projManager;
	}

    /*
    * This function removes a player from the ready players of their game.
    * Function is called in onQuit in Core.java
     */
    public void removePlayerFromVotes(Player player) {
        GameInstance game = GetInstanceOfPlayer(player);

        if (game != null && game.getGameSettings() != null) {
            game.getGameSettings().removeFromStartVotes(player);
        }
    }

    /*
    * This function returns an integer of the number of token classes
    * a player has unlocked
     */
    public int numOfUnlockedTokenClasses (Player player) {
        int num = 0;
        PlayerData data = main.getDataManager().getPlayerData(player);

        if (data != null) {
            for (ClassType type : ClassType.values()) {
                if (type.getTokenCost() > 0 && !type.isVaulted()) {
                    if (data.playerClasses.get(type.getID()) != null
                            && data.playerClasses.get(type.getID()).purchased) {
                        num++;
                    }
                }
            }
        }

        return num;
    }

    /*
    * This function checks how many token classes there are total
     */
    public int totalNumOfTokenClasses() {
        int num = 0;

        for (ClassType type : ClassType.values()) {
            if (type.getTokenCost() > 0 && !type.isVaulted()) {
                num++;
            }
        }

        return num;
    }

    /*
    * This function returns an integer of how many level classes
    * a player has unlocked
     */
    public int numOfUnlockedLevelClasses(Player player) {
        int num = 0;
        PlayerData data = main.getDataManager().getPlayerData(player);

        if (data != null) {
            for (ClassType type : ClassType.values()) {
                if (type.getLevel() > 0 && !type.isVaulted()) {
                    if (data.level >= type.getLevel()) {
                        num++;
                    }
                }
            }
        }

        return num;
    }

    /*
     * This function checks how many level classes there are total
     */
    public int totalNumOfLevelClasses() {
        int num = 0;

        for (ClassType type : ClassType.values()) {
            if (type.getLevel() > 0 && !type.isVaulted()) {
                num++;
            }
        }

        return num;
    }


    /*
    * This function returns a boolean to check if a game is full, but
    * if a player has a rank, they can bypass the full restriction
    * unless the gamemode is duels
     */
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
        for (GameInstance gi : games) {
            if (gi.state == GameState.WAITING && gi.gameType == type && !checkIfFull(player, gi, type)) {
                return gi; //Found a game
            }
        }

        List<Maps> candidates = new ArrayList<>();
        for (Maps m : Maps.values()) {
            if (m.getGamemode() == type && getInstanceOfMap(m) == null) {
                candidates.add(m);
            }
        }

        if (candidates.isEmpty())
            return null;

        Maps map = candidates.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(candidates.size()));
        return createGame(map);
    }

    public void shootProjectile(GameInstance instance, Player player, double dmg, Material mat, Sound s, int volume, Effect e, int pitch) {
        Vector direction = player.getLocation().getDirection();
        ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
            @Override
            public void onHit(Player hit) {
                if (hit == null || hit.getGameMode() != GameMode.SPECTATOR) {
                    Location hitLoc = this.getBaseProj().getEntity().getLocation();

                    for (Player gamePlayer : this.getNearby(3.0)) {
                        EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer, DamageCause.VOID, dmg);
                        instance.getGameManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
                        gamePlayer.damage(dmg, player);
                        if (gamePlayer == hit) {
                            gamePlayer.setVelocity(new Vector(0, 0.7, 0));
                        }
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

    /*
    * This function checks multiple cases to disable players from hitting other
    * entities if they're a spectator, or if a player tries attacking one of
    * their own creatures they spawned, then the event will be cancelled
     */
	@EventHandler
	public void Target(EntityTargetLivingEntityEvent event) {
		// Flag Wars has its own mob targeting (FWPokeballManager)
		if (main.getFwGameManager() != null && main.getFwGameManager().getInstanceForWorld(event.getEntity().getWorld()) != null)
			return;

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
	public void onCaveSpiderPlayerHit(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();
		if (!(damager instanceof CaveSpider)) return;
		Entity damagee = event.getEntity();
		if (!(damagee instanceof Player)) return;
		Player playerDamagee = (Player) damagee;

		playerDamagee.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 7 * 20, 0));
	}

	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event) {
		for (GameInstance game : new ArrayList<>(games)) {
			if (game.PlayerInteract(event))
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

		Player player = event.getPlayer();
		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance == null) return; // not in an SCB game - let vanilla (or FW) handle it

		event.setCancelled(true);

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

		EnderPearl fakePearl = FakeLoot.findFakePearl(player, to);

		// (a fake pearl plays it when the teleport really happens, further down)
        if (baseClass.getType() == ClassType.Enderman && fakePearl == null)
            SoundManager.playSoundToAll(player, Sound.ENDERMAN_TELEPORT, 1, 1);

		// A fake Ender Pearl (the Grinch's): the teleport is held back and the spot it landed on is kept, and the
		// player goes there 5 seconds after the THROW (straight away if the flight took longer than that). Not at all
		// if they died since they threw it
		if (fakePearl != null) {
			int deathsAtThrow = FakeLoot.deathsAtThrow(fakePearl);
			if (baseClass.totalDeaths != deathsAtThrow)
				return;

			long remainingMs = FAKE_PEARL_DELAY_MS - (System.currentTimeMillis() - FakeLoot.thrownAt(fakePearl));
			if (remainingMs > 0) {
				teleportAfterDelay(instance, player, to, (remainingMs + 49) / 50, deathsAtThrow);
				return;
			}
			fakePearlTeleport(player, to, false);
			return;
		}

		player.teleport(to);
	}

	private static final long FAKE_PEARL_DELAY_MS = 5000L;

	/**
	 * The held back teleport of a fake Ender Pearl: only goes through if they are still alive and playing when it
	 * comes, and have not died since they threw it (dying cancels it, even if they respawned by then). They keep
	 * looking the way they are by then.
	 */
	private void teleportAfterDelay(final GameInstance instance, final Player player, final Location to, long ticks,
			final int deathsAtThrow) {
		instance.trackClassTask(Bukkit.getScheduler().runTaskLater(main, new Runnable() {
			@Override
			public void run() {
				if (!player.isOnline() || instance.state != GameState.STARTED || GetInstanceOfPlayer(player) != instance)
					return;
				BaseClass baseClass = instance.classes.get(player);
				if (baseClass == null || baseClass.isDead || baseClass.getLives() <= 0
						|| baseClass.totalDeaths != deathsAtThrow || !instance.isInBounds(to))
					return;

				fakePearlTeleport(player, to, true);
			}
		}, ticks));
	}

	/**
	 * Takes the player to where their fake pearl landed. Once they are there, everyone in the world hears the Enderman
	 * teleport sound, both where they left and where they arrived.
	 */
	private void fakePearlTeleport(Player player, Location to, boolean keepLooking) {
		Location from = player.getLocation();
		Location destination = to.clone();
		if (keepLooking) {
			destination.setYaw(from.getYaw());
			destination.setPitch(from.getPitch());
		}
		player.teleport(destination);

		// Sent to each player (not just to who the server thinks is in range) at both spots
		for (Player listener : new ArrayList<>(destination.getWorld().getPlayers())) {
			listener.playSound(from, Sound.ENDERMAN_TELEPORT, 1, 1);
			listener.playSound(destination, Sound.ENDERMAN_TELEPORT, 1, 1);
		}
	}

	/**
	 * This function gets the number of active games running, used for Active Games
	 * GUI
	 *
	 * @return num of games in progress
	 */
	public int getNumOfGames() {
		int num = 0;
		for (GameInstance entry : new ArrayList<>(games)) {
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
		for (GameInstance entry : games) {
			if (entry.state == GameState.STARTED && entry.gameType == GameType.FRENZY) {
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
		for (GameInstance entry : games) {
			if (entry.state == GameState.STARTED && entry.gameType == GameType.CLASSIC) {
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
		for (GameInstance entry : games) {
			if (entry.state == GameState.STARTED && entry.gameType == GameType.DUEL) {
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
		for (GameInstance game : new ArrayList<>(games)) {
			if (game.state == GameState.WAITING || game.state == GameState.STARTED) {
				num++;
				mapName = game.getMap().toString();
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
	 * The Christmas 'Present' lightning drop: right clicking it uses it up (1 use) and gives 2 items, one
	 * COMMON/UNCOMMON and one RARE/LEGENDARY (see GameLootDrops#openPresent).
	 *
	 * @param e which is the Player interaction event with items
	 */
	@EventHandler
	public void present(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack item = e.getItem();
		if (!GameLootDrops.isPresent(item)) return;

		Player player = e.getPlayer();
		GameInstance game = this.GetInstanceOfPlayer(player);
		if (game == null || game.state != GameState.STARTED) return;

		e.setCancelled(true); // it's a chest: don't place it

		// A fake Present (the Grinch's) opens into 2 fake drops (his), not the real ones
		boolean fakePresent = FakeLoot.isFake(item);
		UUID fakeOwner = FakeLoot.ownerOf(item);

		// Use up one Present - only the one in hand, not every trapped chest the player has
		PlayerInventory inventory = player.getInventory();
		int slot = inventory.getHeldItemSlot();
		if (item.getAmount() > 1) {
			ItemStack remaining = item.clone();
			remaining.setAmount(item.getAmount() - 1);
			inventory.setItem(slot, remaining);
		} else {
			inventory.setItem(slot, null);
		}

		ItemStack[] rewards = fakePresent ? FakeLoot.openPresent(game.getGameSettings().enabledDrops, fakeOwner)
				: GameLootDrops.openPresent(game.getGameSettings().enabledDrops);
		StringBuilder names = new StringBuilder();
		for (ItemStack reward : rewards) {
			if (!GameLootDrops.giveToPlayer(player, reward)) {
				player.getWorld().dropItem(player.getLocation(), reward); // no room: don't lose it
			}
			if (names.length() > 0) names.append("&r and ");
			names.append("&e").append(reward.getItemMeta().getDisplayName());
		}

		player.updateInventory();
		player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1f, 1f);
		player.sendMessage(main.color("&c&l(!) &c&lMerry Christmas! &rYou received " + names + "&r!"));
		if (game.savesStats() && !fakePresent) { // a fake Present doesn't count for the quest
			int battlePassXp = main.battlePassQuest(player, anthony.SuperCraftBrawl.battlepass.BattlePassQuest.EARLY_GIFT, 1);
			if (game.classes.get(player) != null)
				game.classes.get(player).totalBattlePassXp += battlePassXp;
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
				// On the map 'Tropical', void level is 70 because of the water
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
									player.setHealth(player.getMaxHealth()); // the Max Health modifier can make it lower than 20
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

    /*
    * This function deals with the 'Instagib' lightning drop item. When you right click, it
    * captures anyone within a 30 block range that you're clicking towards and will set an
    * upward velocity and do damage.
     */
	@EventHandler
	public void instagibItem(PlayerInteractEvent event) {
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
					// A fake Instagib (the Grinch's) shoots the opposite way to where you look
						Location eye = player.getEyeLocation();
						if (FakeLoot.isFake(item))
							if (FakeLoot.sneaky()) // 2 times out of 3: the beam, the damage and the launch all go that way
									eye.setDirection(eye.getDirection().multiply(-1));
						Location endLoc = eye.clone();
					BlockIterator b = new BlockIterator(eye, 0, range);

					while (b.hasNext()) {
						Block block = b.next();
						endLoc = block.getLocation();

						if (block.getType().isSolid())
							break;
					}

					Vector dir = eye.getDirection();
					double maxDist = endLoc.distance(eye);

					for (double t = 1; t < maxDist; t += 0.5) {
						ParticleEffect.EXPLOSION_LARGE.display(eye.clone().add(dir.clone().multiply(t)));
					}

					for (Player p : i.players) {
						p.playSound(p.getLocation(), Sound.EXPLODE, 1, 2);
						if (p != player && i.classes.containsKey(p) && i.classes.get(player).getLives() > 0
								&& !FakeLoot.isOut(p, i.classes.get(p))) {
							Vector d = p.getLocation().add(0, 1, 0).subtract(eye).toVector();
							double dist = d.dot(dir);

							if (dist > 0 && dist < maxDist) { // in front of the shot, never behind whoever fires it
								Location closest = eye.clone().add(dir.clone().multiply(dist));

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
		// Not in a Super Craft Bros game (e.g. eating a golden apple in Flag Wars), so there is no class to tell
		if (gameInstance == null) return;
		BaseClass playerClass = gameInstance.classes.get(player);
		if (playerClass == null) return;
		playerClass.onConsumingItem(event);
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
		// Battle Pass quest: using a BOMB (it counts when it lands, hit or miss)
		// (a fake Bomb, from the Grinch, doesn't count)
		if (gameInstance.savesStats() && GameLootDrops.isBomb(thrownPotion) && !FakeLoot.isFake(thrownPotion.getItem())) {
			int battlePassXp = main.battlePassQuest(shooter, anthony.SuperCraftBrawl.battlepass.BattlePassQuest.BOMB_SQUAD, 1);
			if (gameInstance.classes.get(shooter) != null)
				gameInstance.classes.get(shooter).totalBattlePassXp += battlePassXp;
		}
		gameInstance.classes.get(shooter).potionSplashEvent(event);
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
	 * This function gets rid of loot drops & exp that mobs can drop
	 *
	 * @param event to remove loot drops/exp from
	 */
	@EventHandler
	public void EntityDeathEvent(EntityDeathEvent event) {
		event.getDrops().clear();
		event.setDroppedExp(0);
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

							if (FakeLoot.isFake(item)) // a fake Blooper (the Grinch's) gives Night Vision instead
								target.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0, true));
							else if (chance == 0)
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

    /*
    * These 3 functions below disables the default minecraft bone interactivity
    * with a Wolf so your bone doesn't get consumed
     */
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

    /*
    * This function handles the interactivity with the One For All 'Voting Paper'
    * in a custom game lobby, and will open its GUI if the item is a paper
     */
	@EventHandler
	public void votePaper(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		GameInstance game = this.GetInstanceOfPlayer(player);

		if (game != null)
			if (item != null && item.getType() == Material.PAPER && game.isOneForAllVoting())
				new OneForAllVotesGUI(main, game).inv.open(player);
	}

    /*
    * This function handles the interactivity with the ready-up dye in a game lobby:
    * right clicking it flips the player between ready (green) and not ready (gray)
     */
	@EventHandler
	public void readyUpItem(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Player player = event.getPlayer();
		GameInstance game = this.GetInstanceOfPlayer(player);

		if (game != null && GameInstance.isReadyItem(event.getItem()))
			game.toggleReady(player);
	}

	/** Right clicking the Frenzy vote TNT in a game lobby votes for Frenzy (or takes the vote back). */
	@EventHandler
	public void frenzyVoteItem(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Player player = event.getPlayer();
		GameInstance game = this.GetInstanceOfPlayer(player);

		if (game != null && game.state == GameState.WAITING && GameInstance.isFrenzyVoteItem(event.getItem())) {
			event.setCancelled(true); // it's TNT, so it would be placed
			game.toggleFrenzyVote(player);
		}
	}

    /*
    * This function handles the interactivity with the 'Extra Life' item. When
    * used, a player will receieve an additional life in a game
     */
	@EventHandler
	public void extraLifeItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		PlayerData data = getMain().getDataManager().getPlayerData(player);
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null) {
			if (instance.state == GameState.STARTED) {
				if (item != null && item.getType() == Material.PRISMARINE_SHARD
						&& (event.getAction() == Action.RIGHT_CLICK_AIR
								|| event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
					BaseClass baseClass = instance.classes.get(player);
					int amount = item.getAmount();
					if (FakeLoot.isFake(item)) {
						// A fake Extra Life (the Grinch's) costs a life instead. On the last life it fails: no life is
						// lost, nobody is told, and it only fizzles (it is still used up)
						if (baseClass.lives > 1) {
							baseClass.lives -= 1;
							baseClass.score.setScore(baseClass.lives);
							baseClass.tellAll(main.color("&c&l(!) &c" + player.getName() + "&r LOST A LIFE!"));
							player.getWorld().playSound(player.getLocation(), Sound.LEVEL_UP, 1, 2);
						} else {
							player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 1, 1);
						}
					} else {
						baseClass.lives += 1;
						baseClass.score.setScore(baseClass.lives);
						baseClass.tellAll(main.color("&2&l(!) &a" + player.getName() + "&r used an extra life!"));
						player.getWorld().playSound(player.getLocation(), Sound.LEVEL_UP, 1, 2);
					}

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

    /*
    * This function disables fire arrows from igniting TNT
     */
	@EventHandler
	public void tntChange(EntityChangeBlockEvent event) {
		Entity e = event.getEntity();

		if (e instanceof Arrow)
			if (event.getBlock().getType() == Material.TNT)
				event.setCancelled(true);
	}

	/**
	 * This function disables players from dropping items
	 *
	 * @param e
	 */
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		final Player player = e.getPlayer();
		anthony.flagwars.FWGameInstance fwInstance = getMain().getFwGameManager().getInstanceOfPlayer(player);
		if (fwInstance != null && fwInstance.getState() == anthony.flagwars.FWGameState.IN_PROGRESS) {
			return; // Flag Wars players can drop items mid-match
		}

		e.setCancelled(true);

		// Tag Team: dropping any item swaps classes. A tick later, once the cancelled drop has put the item back
		final GameInstance instance = GetInstanceOfPlayer(player);
		if (instance != null && instance.isTagTeamSwapDrop(player)) {
			Bukkit.getScheduler().runTask(main, new Runnable() {
				@Override
				public void run() {
					instance.swapTagTeamClass(player);
				}
			});
		}
	}

	@EventHandler
	public void bazookaItem(PlayerInteractEvent event) {
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
						if (bc.bazooka.getTime() < 3000) { //3 second usage cooldown
							int seconds = (3000 - bc.bazooka.getTime()) / 1000 + 1;
							event.setCancelled(true);
                            player.sendMessage(main.color("&2&l(!) &rYour Bazooka is still regenerating for &a" +
                                    seconds + "s"));
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
							// A fake Bazooka (the Grinch's) heals instead 2 times out of 3, the rest of the time it's the real one
							if (FakeLoot.isFake(item) && FakeLoot.sneaky()) {
								shootFakeBazooka(instance, player);
								event.setCancelled(true);
								return;
							}
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

	private static final double FAKE_BAZOOKA_HEAL = 4.0; // a bit less than the 5.5 the real one does as damage

	/**
	 * A fake Bazooka (the Grinch's): the same shot and the same explosion, but it heals everyone it hits, enemies
	 * and yourself included, instead of hurting them.
	 */
	private void shootFakeBazooka(final GameInstance instance, Player player) {
		ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				if (hit != null && hit.getGameMode() == GameMode.SPECTATOR)
					return;
				Location hitLoc = this.getBaseProj().getEntity().getLocation();

				for (Player gamePlayer : this.getNearby(3.0)) {
					BaseClass healed = instance.classes.get(gamePlayer);
					if (healed == null || FakeLoot.isOut(gamePlayer, healed))
						continue;
					gamePlayer.setHealth(Math.min(gamePlayer.getMaxHealth(), gamePlayer.getHealth() + FAKE_BAZOOKA_HEAL));
				}
				for (Player gamePlayer : instance.players) {
					gamePlayer.playSound(hitLoc, Sound.EXPLODE, 2, 1);
					gamePlayer.playEffect(hitLoc, Effect.EXPLOSION_HUGE, 1);
				}
			}
		}, new ItemStack(Material.TNT));
		instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(2.0D));
	}

	/** A fake Fire Flower (the Grinch's): thrown like the real one, but it only plants a poppy where it lands. */
	private void shootFakeFireFlower(GameInstance instance, Player player) {
		ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				FakeLoot.plantPoppy(this.getBaseProj().getEntity().getLocation());
			}
		}, new ItemStack(Material.BLAZE_POWDER));
		instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(2.0D));
	}

	private static final int SNOWBALL_LAUNCHER_SHOTS = 3;
	private static final long SNOWBALL_LAUNCHER_SHOT_DELAY_TICKS = 6; // 0.3s between snowballs
	private static final int SNOWBALL_LAUNCHER_COOLDOWN_MS = 1000; // covers the burst so uses can't overlap
	private static final double SNOWBALL_LAUNCHER_KB_HORIZONTAL = 2.5;
	private static final double SNOWBALL_LAUNCHER_KB_VERTICAL = 1.0;
	private static final String SNOWBALL_LAUNCHER_META = "SnowballLauncher";
	private static final int SNOWBALL_LAUNCHER_TRAIL_MAX_TICKS = 5 * 20; // in case it never lands

	/*
	 * This function deals with the 'Snowball Launcher' lightning drop item. Each right click uses
	 * one charge and fires a burst of 3 snowballs, 0.3s apart. Any enemy hit is launched back and up.
	 */
	@EventHandler
	public void snowballLauncherItem(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
			return;

		ItemStack item = event.getItem();
		if (item == null || item.getType() != Material.IRON_BARDING || !item.hasItemMeta()
				|| !item.getItemMeta().hasDisplayName()
				|| !ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("SNOWBALL LAUNCHER"))
			return;

		Player player = event.getPlayer();
		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance == null || instance.state != GameState.STARTED || player.getGameMode() == GameMode.SPECTATOR)
			return;

		BaseClass bc = instance.classes.get(player);
		if (bc == null)
			return;

		event.setCancelled(true);
		if (bc.snowballLauncher.getTime() < SNOWBALL_LAUNCHER_COOLDOWN_MS)
			return;
		bc.snowballLauncher.restart();

		// A fake one (the Grinch's): its snowballs mostly launch whoever fired them instead, see snowballLauncherHit
		final boolean fake = FakeLoot.isFake(item);
		useSnowballLauncherCharge(player, item);

		new BukkitRunnable() {
			private int shotsFired = 0;

			@Override
			public void run() {
				// Stop the burst early if the shooter died, left, or the game ended mid-burst
				if (instance.state != GameState.STARTED || !player.isOnline()
						|| !instance.classes.containsKey(player) || player.getGameMode() == GameMode.SPECTATOR) {
					cancel();
					return;
				}

				shootLauncherSnowball(instance, player, shotsFired, fake);
				if (++shotsFired >= SNOWBALL_LAUNCHER_SHOTS)
					cancel();
			}
		}.runTaskTimer(main, 0, SNOWBALL_LAUNCHER_SHOT_DELAY_TICKS);
	}

	/** Takes one use off the held launcher (its item amount), removing it on the last use. */
	private void useSnowballLauncherCharge(Player player, ItemStack item) {
		if (item.getAmount() <= 1)
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
		else
			item.setAmount(item.getAmount() - 1);
	}

	private void shootLauncherSnowball(GameInstance instance, Player shooter, int shotIndex, boolean fake) {
		// Cartoony "bloop... bloop... BLOOP!" - each shot in the burst pops at a higher pitch
		Location shootLoc = shooter.getLocation();
		shooter.getWorld().playSound(shootLoc, Sound.CHICKEN_EGG_POP, 1.5f, 0.6f + shotIndex * 0.4f);
		shooter.getWorld().playSound(shootLoc, Sound.SLIME_WALK, 1, 1.5f);

		// A real snowball, tagged so only snowballLauncherHit handles it (never the Slowballs effect)
		Snowball snowball = shooter.launchProjectile(Snowball.class);
		snowball.setMetadata(SNOWBALL_LAUNCHER_META, new FixedMetadataValue(main, true));
		if (fake)
			FakeLoot.markEntity(snowball);

		// Frosty trail until the snowball lands
		instance.trackClassTask(new BukkitRunnable() {
			private int ticks = 0;

			@Override
			public void run() {
				if (!snowball.isValid() || ++ticks > SNOWBALL_LAUNCHER_TRAIL_MAX_TICKS) {
					cancel();
					return;
				}
				Location loc = snowball.getLocation();
				ParticleEffect.SNOW_SHOVEL.display(loc, 0.08f, 0.08f, 0.08f, 0.01f, 2, null);
				if (ticks % 2 == 0) ParticleEffect.FIREWORKS_SPARK.display(loc, 0.05f, 0.05f, 0.05f, 0.0f, 1, null);
			}
		}.runTaskTimer(main, 1, 1));
	}

	/*
	 * Launches enemies hit by a Snowball Launcher snowball back (along the snowball's flight) and up.
	 * Runs first and always cancels the hit, so vanilla snowball knockback can't override the launch.
	 * (A fake launcher from the Grinch launches the one who fired it instead, 2 hits out of 3.)
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void snowballLauncherHit(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Snowball) || !event.getDamager().hasMetadata(SNOWBALL_LAUNCHER_META))
			return;
		event.setCancelled(true);

		Snowball snowball = (Snowball) event.getDamager();
		if (!(snowball.getShooter() instanceof Player) || !(event.getEntity() instanceof Player))
			return;

		Player shooter = (Player) snowball.getShooter();
		Player hit = (Player) event.getEntity();
		GameInstance instance = this.GetInstanceOfPlayer(hit);
		if (instance == null || instance.state != GameState.STARTED || instance != this.GetInstanceOfPlayer(shooter)
				|| hit.getGameMode() == GameMode.SPECTATOR || spawnProt.containsKey(hit))
			return;

		BaseClass hitClass = instance.classes.get(hit);
		if (hitClass == null || hitClass.getLives() <= 0 || hitClass.bedrockInvincibility)
			return;
		if (instance.duosMap != null && Objects.equals(instance.team.get(hit), instance.team.get(shooter)))
			return;

		Vector push = snowball.getVelocity().setY(0);
		push = push.lengthSquared() > 1.0E-4 ? push.normalize().multiply(SNOWBALL_LAUNCHER_KB_HORIZONTAL) : new Vector();
		// A fake launcher (the Grinch's): each snowball that hits has a 2 in 3 chance of launching whoever fired it
		// instead of the one it hit, and the other way round (back, away from who was hit). Not if the shooter is out
		Player launched = hit;
		if (FakeLoot.isMarked(snowball) && FakeLoot.sneaky()
				&& !FakeLoot.isOut(shooter, instance.classes.get(shooter))) {
			launched = shooter;
			push.multiply(-1);
		}
		launched.setVelocity(push.setY(SNOWBALL_LAUNCHER_KB_VERTICAL));

		// Goofy "BOING" + a squeaky yelp from whoever got launched
		Location launchLoc = launched.getLocation();
		launched.getWorld().playSound(launchLoc, Sound.MAGMACUBE_JUMP, 1.5f, 0.6f);
		launched.getWorld().playSound(launchLoc, Sound.VILLAGER_HIT, 1, 2.0f);

		if (launched != hit) // the one fired at wasn't pushed: nothing to credit
			return;

		// Credit the shooter if the launch knocks them into the void
		hit.setLastDamageCause(new EntityDamageByEntityEvent(shooter, hit, DamageCause.ENTITY_ATTACK, 0));
	}

	/*
	 * Keeps Santa on his flying Reindeer when he holds Sneak (Sneak flies down instead).
	 */
	@EventHandler
	public void onReindeerExit(VehicleExitEvent event) {
		if (!(event.getExited() instanceof Player))
			return;

		Player player = (Player) event.getExited();
		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance == null)
			return;

		BaseClass bc = instance.classes.get(player);
		if (bc instanceof SantaClass && ((SantaClass) bc).shouldBlockReindeerExit(event.getVehicle()))
			event.setCancelled(true);

		ReindeerFlight winFlight = getWinEffectReindeer(instance, player);
		if (winFlight != null && winFlight.shouldBlockExit(event.getVehicle()))
			event.setCancelled(true);
	}

	/** The Santa win effect's Reindeer the player is flying, if any. */
	private static ReindeerFlight getWinEffectReindeer(GameInstance instance, Player player) {
		WinEffects effects = instance.effects.get(player);
		return effects == null ? null : effects.getReindeerFlight();
	}

	/*
	 * Teleports dismount the rider right after this event fires, so end the Reindeer flight first
	 * (otherwise the blocked dismount would teleport Santa while still mounted). The server's own
	 * position corrections (UNKNOWN) don't dismount anyone and happen all the time for newer clients
	 * joining through ViaVersion while riding, so they mustn't end the flight.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onReindeerRiderTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (event.getCause() == TeleportCause.UNKNOWN || !(player.getVehicle() instanceof Horse))
			return;

		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance == null)
			return;

		BaseClass bc = instance.classes.get(player);
		if (bc instanceof SantaClass)
			((SantaClass) bc).endReindeerFlight();

		ReindeerFlight winFlight = getWinEffectReindeer(instance, player);
		if (winFlight != null)
			winFlight.end();
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
					// A fake Broom (the Grinch's) dashes you down instead, 2 times out of 3
					boolean dashDown = FakeLoot.isFake(item) && FakeLoot.sneaky();
					int amount = item.getAmount();
					if (amount > 0) {
						amount--;
						if (amount == 0)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(amount);
						e.setCancelled(true);
						player.setVelocity(new Vector(0, dashDown ? -1 : 1, 0).multiply(1.3D));
						for (Player gamePlayer : instance.players)
							gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1);
					}
				}
			}
		}
	}

	@EventHandler
	public void onBroomWinEffectUse(PlayerInteractEvent event) {
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

					if (FakeLoot.isFake(item)) {
						// A fake Milk (the Grinch's) takes away one of your good effects instead, and leaves any fire burning
						FakeLoot.removeOneGoodEffect(player);
					} else {
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
					}
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

	/**
	 * This function cancels item pickup if the item is a projectile thrown by a
	 * player in game
	 *
	 * @param event
	 */
	@EventHandler
	public void OnPickupItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();

		if (projManager.isProjectile(item)) {
			event.setCancelled(true);
			return;
		}

		// The Grinch can't pick up the fake drops he threw (anyone else can, and he's told)
		if (FakeLoot.handlePickup(main, event.getPlayer(), item)) {
			event.setCancelled(true);
			return;
		}

		// The Snowball Launcher is a single item whose amount is its uses: the vanilla pickup would split it
		// into three launchers, so it's moved into the inventory whole instead
		ItemStack stack = item.getItemStack();
		if (GameLootDrops.isOversizedStack(stack) && GameLootDrops.isLootDropItem(stack)) {
			event.setCancelled(true);
			if (GameLootDrops.giveToPlayer(event.getPlayer(), stack)) {
				item.remove();
				event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_PICKUP, 0.2f, 1.5f);
				grinchTookRealDrop(event.getPlayer(), item, stack);
			}
			return;
		}
		grinchTookRealDrop(event.getPlayer(), item, stack);
	}

	/*
	 * When a Grinch picks up a real lightning drop, a counterfeit copy of it is left where it lay, so the next
	 * player to walk by takes the fake. Fakes (any Grinch's) are never copied.
	 */
	private void grinchTookRealDrop(Player picker, Item item, ItemStack stack) {
		GameInstance instance = GetInstanceOfPlayer(picker);
		BaseClass baseClass = instance == null ? null : instance.classes.get(picker);
		if (!(baseClass instanceof GrinchClass) || FakeLoot.isFake(stack))
			return;

		GameLootDrops drop = GameLootDrops.fromItem(stack);
		if (FakeLoot.isFakeable(drop))
			((GrinchClass) baseClass).leaveFakeBehind(item.getLocation(), drop);
	}

	// Disables portals usage
	@EventHandler
	public void onPortal(EntityPortalEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void snowballsItem(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Snowball) {
			Snowball snowball = (Snowball) event.getDamager();
			if (snowball.hasMetadata(SNOWBALL_LAUNCHER_META))
				return; // Snowball Launcher hits are handled by snowballLauncherHit, not Slowballs
			if (FakeLoot.isMarked(snowball)) {
				// Fake Slowballs (the Grinch's) give Speed II for 3s instead of slowing
				if (snowball.getShooter() instanceof Player && event.getEntity() instanceof Player) {
					Player shooter = (Player) snowball.getShooter();
					Player hitPlayer = (Player) event.getEntity();
					GameInstance i = this.GetInstanceOfPlayer(hitPlayer);
					if (i != null && (i.duosMap == null || !Objects.equals(i.team.get(shooter), i.team.get(hitPlayer))))
						hitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3 * 20, 1));
				}
				return;
			}
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
        int neededSlots = 1;

        if (main.getPartyManager() != null) {
            Party party = main.getPartyManager().getParty(player);

            if (party != null) {
                if (!party.isLeader(player.getUniqueId())) {
                    player.sendMessage(main.color("&c&l(!) &rOnly the party leader can join games."));
                    return;
                }

                neededSlots = party.getOnlineMembers().size();
            }
        }

        List<Maps> existingMaps = new ArrayList<>();

        // Maps with a running game can still be picked - the new game gets its own copy of the map
        for (GameInstance instance : new ArrayList<>(games)) {
            if (instance.getMap().GetInstance().gameType == type && instance == getInstanceOfMap(instance.getMap())) {

                if (instance.isOpen()) {
                    int currentPlayers = instance.players.size();
                    int maxPlayers = instance.getMaxPlayerCount();

                    if (currentPlayers + neededSlots <= maxPlayers) {
                        this.JoinMap(player, instance.getMap());
                        return;
                    }
                }

                existingMaps.add(instance.getMap());
            }
        }

        List<Maps> maps = Maps.getGameType(type);
        maps.removeAll(existingMaps);

        if (maps.size() > 0) {
            Maps map = maps.get(new Random().nextInt(maps.size()));
            this.JoinMap(player, map);
        } else {
            player.sendMessage(main.color("&c&l(!) &rNo game has room for your party."));
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
					// Dead players waiting to respawn are in adventure mode with full health, so nothing may hurt
					// them - with low max health any hit would count as another death and take another life
					if (base.isDead) {
						event.setCancelled(true);
						return;
					}
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
										baseClass.doDamage2(damageEvent);

								baseClass.doDamage(damageEvent);
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
									shooterBaseClass.doDamage(damageEvent);
								}
							}
						}
					} else if (damageEvent.getDamager() instanceof FishHook) {
						FishHook damager = (FishHook) damageEvent.getDamager();
						if (damager.getShooter() instanceof Player) {
							Player p = (Player) damager.getShooter();
							BaseClass bc = instance.classes.get(p);
							if (bc.getType() == ClassType.Fisherman) {
								event.setCancelled(true);
							}
						}
					}
				}

				if (!event.isCancelled() && event.getFinalDamage() >= player.getHealth() - 0.2) {
					event.setCancelled(true);

					if (event instanceof EntityDamageByEntityEvent) {
						player.setLastDamageCause(event);
					}

					instance.PlayerDeath(player);

				} else if (instance.classes.containsKey(player)) {
					instance.classes.get(player).takeDamage(event);
				}
			}
		}
	}

	// Only creative players can break paintings
	@EventHandler
	public void onPaintingBreak(HangingBreakEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Painting) && !(entity instanceof ItemFrame)) return;
		if (event instanceof HangingBreakByEntityEvent) {
			Entity remover = ((HangingBreakByEntityEvent) event).getRemover();
			if (remover instanceof Player && ((Player) remover).getGameMode() == GameMode.CREATIVE
					&& !main.getLobbyBuildManager().isLobbyBuilder((Player) remover))
				return;
		}
		event.setCancelled(true);
	}

	// Only creative players can interact with items inside item frames
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemFrameInteract(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof ItemFrame)) return;
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE
				&& !main.getLobbyBuildManager().isLobbyBuilder(event.getPlayer())) {
			event.setCancelled(false);
			return;
		}
		event.setCancelled(true);
	}

	// Only creative players can remove items from item frames by hitting them
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemFrameDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof ItemFrame)) return;
		if (event.getDamager() instanceof Player && ((Player) event.getDamager()).getGameMode() == GameMode.CREATIVE
				&& !main.getLobbyBuildManager().isLobbyBuilder((Player) event.getDamager())) {
			event.setCancelled(false);
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void FireballDamage(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			GameInstance instance = this.GetInstanceOfPlayer(player);
			if (instance != null) {
				BaseClass baseClass = instance.classes.get(player);
				baseClass.projectileLaunch(event);
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
				baseClass.projectileHit(event);
			}
		}
	}

	public Core getMain() {
		return main;
	}

    /*
    * This function handles player class selection, and will set it
    * if they are in a game, but only if they're in waiting lobby
     */
	/*
	 * Narrows a class menu down to what the player can pick in their current game: the non-vaulted classes
	 * normally, or exactly the classes the leader enabled in a private game.
	 */
	public ClassType[] filterSelectableClasses(Player player, ClassType[] types) {
		GameInstance instance = GetInstanceOfPlayer(player);
		boolean customGame = instance != null && instance.isCustomGame();
		List<ClassType> selectable = new ArrayList<>();
		for (ClassType type : types) {
			if (type.isPlayable() && (customGame ? instance.isClassSelectable(type) : !type.isVaulted()))
				selectable.add(type);
		}
		return selectable.toArray(new ClassType[0]);
	}

	/** Whether the player can pick this class regardless of its requirements (an enabled vaulted class in a private game). */
	public boolean ignoresClassRequirements(Player player, ClassType type) {
		GameInstance instance = GetInstanceOfPlayer(player);
		return instance != null && instance.ignoresClassRequirements(type);
	}

	public void playerSelectClass(Player player, ClassType type) {
		GameInstance instance = this.GetInstanceOfPlayer(player);
		if (instance != null)
			instance.setClass(player, type);
	}

	public GameInstance getWaitingMap() {
		for (GameInstance entry : new ArrayList<>(games)) {
			if (entry.state == GameState.WAITING && entry.players.size() > 0)
				// if (entry.gameType == GameType.NORMAL &&
				// entry.players.size() == 5)
				// return null;
				/* else */ if (entry.gameType == GameType.CLASSIC && entry.players.size() < 5)
					return entry;
				// else if (entry.gameType == GameType.DUEL &&
				// entry.players.size() == 1)
				// return null;
				else if (entry.gameType == GameType.DUEL && entry.players.size() < 2)
					return entry;
				else if (entry.gameType == GameType.FRENZY)
					return entry;
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

    public void JoinMap(Player player, Maps map) {
        Party party = getPartyOf(player);

        if (party != null) {
            if (!party.isLeader(player.getUniqueId())) {
                // A party member can still go back to the game their party leader is in (e.g. after /leave)
                GameInstance leaderGame = getPartyLeaderGame(party);
                if (leaderGame != null && leaderGame.getMap() == map) {
                    if (leaderGame.state == GameState.STARTED) SpectatorJoinGame(player, leaderGame);
                    else JoinGame(player, leaderGame);
                    return;
                }

                player.sendMessage(main.color("&c&l(!) &rOnly the party leader can join games."));
                return;
            }

            joinPartyToGame(player, party, map, null);
            return;
        }

        joinSinglePlayerToGame(player, map, null);
    }

    /*
     * Joins this specific game (e.g. a friend's, or a custom game from the Custom Games NPC), with the same
     * party rules as any game: the party leader brings the whole party, and other party members can only
     * (re)join the game their party leader is in.
     */
    public void JoinGame(Player player, GameInstance game) {
        if (game.getMap() == null) return;
        Party party = getPartyOf(player);

        if (party == null) {
            joinSinglePlayerToGame(player, game.getMap(), game);
        } else if (party.isLeader(player.getUniqueId())) {
            joinPartyToGame(player, party, game.getMap(), game);
        } else if (getPartyLeaderGame(party) == game) {
            joinSinglePlayerToGame(player, game.getMap(), game);
        } else {
            player.sendMessage(main.color("&c&l(!) &rOnly the party leader can join games."));
        }
    }

    private Party getPartyOf(Player player) {
        return main.getPartyManager() == null ? null : main.getPartyManager().getParty(player);
    }

    /** The game the party's leader is playing or spectating, if any. */
    private GameInstance getPartyLeaderGame(Party party) {
        Player leader = Bukkit.getPlayer(party.getLeaderId());
        if (leader == null) return null;

        GameInstance game = GetInstanceOfPlayer(leader);
        return game != null ? game : GetInstanceOfSpectator(leader);
    }

    /** The party leader brings the whole party into the given game, or the map's open game (a new one if none). */
    private void joinPartyToGame(Player leader, Party party, Maps map, GameInstance game) {
        GameInstance existingInstance = game != null ? game : getInstanceOfMap(map);

        if (existingInstance != null && existingInstance.isCustomGame() && !existingInstance.canAccessCustomGame(leader)) {
            leader.sendMessage(existingInstance.getCustomGameAccessMessage());
            return;
        }
        if (existingInstance != null && existingInstance.state != GameState.WAITING) {
            leader.sendMessage(main.color("&c&l(!) &rThis game is already playing!"));
            return;
        }

        List<Player> partyMembers = party.getOnlineMembers();

        if (partyMembers.isEmpty()) {
            leader.sendMessage(main.color("&c&l(!) &rYour party has no online members."));
            return;
        }

        int currentPlayers = existingInstance == null ? 0 : existingInstance.players.size();
        int maxPlayers = existingInstance == null ? map.getGamemode().getMaxPlayers() : existingInstance.getMaxPlayerCount();

        if (currentPlayers + partyMembers.size() > maxPlayers) {
            leader.sendMessage(main.color("&c&l(!) &rYour party is too large for this game."));
            leader.sendMessage(main.color("&7Players: &e" + currentPlayers + "/" + maxPlayers + " &7| Party Size: &e" + partyMembers.size()));
            return;
        }

        if (!canAllJoin(leader, partyMembers)) return;

        GameInstance targetInstance = existingInstance != null ? existingInstance : createGame(map);

        for (Player member : partyMembers) {
            joinSinglePlayerToGame(member, map, targetInstance);
        }

        for (Player member : partyMembers) {
            member.sendMessage(main.color("&a&l(!) &rYour party has joined &e" + map.toString() + "&r."));
        }
    }

    /** Tells the leader and returns false if any of these players is in a game, spectating, or in parkour. */
    public boolean canAllJoin(Player leader, List<Player> members) {
        for (Player member : members) {
            if (GetInstanceOfPlayer(member) != null) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris already in a game."));
                return false;
            }

            if (GetInstanceOfSpectator(member) != null) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris already spectating a game."));
                return false;
            }

            if (main.getFwGameManager().getInstanceOfPlayer(member) != null
                    || main.getFwGameManager().getInstanceOfSpectator(member) != null) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris already in a game."));
                return false;
            }

            if (main.getParkour().hasPlayer(member)) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris currently in parkour."));
                return false;
            }
        }
        return true;
    }

    /** Only used when there is no curated map to pick from: players who haven't chosen a default map get a random one. */
    public static final Maps DEFAULT_CUSTOM_GAME_MAP = Maps.PileOfBodies;

    /*
     * Custom Games NPC: a new custom game on the leader's default map (on its own copy named <leader>_CustomGame).
     * The leader (and their party) are taken straight there and the leader gets the Game Settings / Start Game /
     * Maps items. Game Settings can make it public, so anyone can join it from the Custom Games NPC.
     */
    public void createCustomGame(Player leader) {
        if (!main.canHostCustomGames(leader)) {
            leader.sendMessage(main.color("&c&l(!) &rCustom Games need &6&lLEGENDARY&r!"));
            return;
        }

        GameInstance running = getRunningCustomGame(leader);
        if (running != null) {
            leader.sendMessage(main.color("&e&l(!) &rYour custom game is still playing."));
            returnHostToGame(leader, running);
            return;
        }

        Party party = main.getPartyManager() == null ? null : main.getPartyManager().getParty(leader);
        if (party != null && !party.isLeader(leader.getUniqueId())) {
            leader.sendMessage(main.color("&c&l(!) &rOnly the party leader can create a custom game."));
            return;
        }

        List<Player> members = party == null ? Collections.singletonList(leader) : party.getOnlineMembers();
        if (!canAllJoin(leader, members)) return;

        Maps map = customGameMaps.resolve(leader);
        GameInstance game = new GameInstance(this, map,
                main.getMapWorldManager().createCustomGameWorld(map.GetInstance().worldName, leader.getName()));
        games.add(game);
        game.enableCustomGame(leader);

        for (Player member : members) {
            joinSinglePlayerToGame(member, map, game);
        }
        if (!game.HasPlayer(leader)) {
            removeGame(game);
            return;
        }

        game.giveCustomGameHostItems(leader);
        CustomGamesGUI.refreshOpenMenus();
    }

    /** The custom game this player hosts that is still playing, if any (a game keeps running when its host leaves). */
    private GameInstance getRunningCustomGame(Player host) {
        for (GameInstance game : games) {
            if (game.isCustomGame() && game.isCustomGameHost(host) && game.state == GameState.STARTED) return game;
        }
        return null;
    }

    /** The host of a playing custom game goes back to it as a spectator, on their own (their party isn't brought along). */
    private void returnHostToGame(Player host, GameInstance game) {
        if (game.getMap() != null) spectateSinglePlayerGame(host, game.getMap(), game);
    }

    /*
     * /joincustomgame <host> (the "Click here to Join" in custom game announcements): joins that host's custom
     * game, or spectates it if it's already playing - with the same party and private game rules as anywhere else.
     */
    public void joinCustomGame(Player player, String hostName) {
        for (GameInstance game : games) {
            if (!game.isCustomGame() || !game.getCustomGameHostName().equalsIgnoreCase(hostName)) continue;

            if (game.state == GameState.STARTED) SpectatorJoinGame(player, game);
            else if (game.state == GameState.WAITING) {
                if (leaveLobbyToJoin(player, game, game.getMap())) JoinGame(player, game);
            }
            else player.sendMessage(main.color("&c&l(!) &rThat custom game has already ended."));
            return;
        }
        player.sendMessage(main.color("&c&l(!) &rThat custom game isn't running anymore."));
    }

    /*
     * Join links (the /invite and "game is starting" messages) work from inside another game's lobby too: the
     * player leaves that lobby, with their party if they lead one, and joins the new game. Returns false if they
     * can't join it (they're told why) and stay where they are. With no target game, any open game on the map will do.
     */
    public boolean leaveLobbyToJoin(Player player, GameInstance target, Maps map) {
        GameInstance current = GetInstanceOfPlayer(player);
        if (current == null || current.state != GameState.WAITING) return true; // nothing to leave (a running game is left with /leave)
        if (current == target || (target == null && current.getMap() == map)) return true; // the join says they're already there

        Party party = getPartyOf(player);
        if (party != null && !party.isLeader(player.getUniqueId())) return true; // only the leader moves the party, the join says so

        if (current.isCustomGame() && current.isCustomGameHost(player)) {
            player.sendMessage(main.color("&c&l(!) &rLeave your custom game first."));
            return false;
        }

        // Don't leave for a game they can't get into
        GameInstance joining = target != null ? target : (map != null ? getInstanceOfMap(map) : null);
        if (joining != null && joining.state == GameState.WAITING) {
            if (joining.isCustomGame() && !joining.canAccessCustomGame(player)) {
                player.sendMessage(joining.getCustomGameAccessMessage());
                return false;
            }
            if (!joining.isOpen()) {
                player.sendMessage(main.color("&c&l(!) &rThis game is full!"));
                return false;
            }
        }

        main.getCommands().leaveGame(player);
        return GetInstanceOfPlayer(player) == null;
    }

    /*
     * /joingame <world> (the /invite "Click here to join!"): joins that exact game. Every game plays on its own
     * copy of the map's world, so /join <map> could send you to a different game on the same map.
     */
    public void joinGameByWorld(Player player, String worldName) {
        for (GameInstance game : games) {
            if (game.getMapWorld() == null || !game.getMapWorld().getName().equals(worldName)) continue;

            if (game.state == GameState.STARTED) SpectatorJoinGame(player, game);
            else if (game.state == GameState.WAITING) {
                if (leaveLobbyToJoin(player, game, game.getMap())) JoinGame(player, game);
            }
            else player.sendMessage(main.color("&c&l(!) &rThat game has already ended."));
            return;
        }
        player.sendMessage(main.color("&c&l(!) &rThat game isn't running anymore."));
    }

    /** The leader picked a different map with the Maps item (or a preset): everyone moves to a new game on it. Null if nothing changed. */
    public GameInstance changeCustomGameMap(Player leader, Maps map) {
        GameInstance game = GetInstanceOfPlayer(leader);
        if (game == null || !game.isCustomGame() || !game.canEditCustomGame(leader)
                || game.state != GameState.WAITING || game.isCustomGameCountdownStarted()) {
            leader.sendMessage(main.color("&c&l(!) &rThe map can only be changed in the lobby."));
            return null;
        }
        if (game.getMap() == map) {
            leader.sendMessage(main.color("&c&l(!) &rAlready on &e" + map.toString() + "&r."));
            return null;
        }

        GameInstance next = moveCustomGame(game, map, null);
        next.TellAll(main.color("&6&l(!) &e" + leader.getName() + " &rchanged the map to &e" + map.toString() + "&r."));
        return next;
    }

    /*
     * A custom game just ended (everyone is back in the lobby): its players go straight into a new custom game
     * on a fresh copy of the same map with the same leader, Game Modifiers and public/private setting, so the
     * leader only has to change what they want. Returns false (and nothing happens) if it isn't a custom game or its leader
     * isn't there anymore.
     */
    public boolean restartCustomGame(GameInstance old) {
        if (!old.isCustomGame() || old.getMap() == null || !games.contains(old)) return false;

        Player leader = old.getCustomGameHost() == null ? null : Bukkit.getPlayer(old.getCustomGameHost());
        if (leader == null || !old.players.contains(leader) || !main.canHostCustomGames(leader)) return false;

        moveCustomGame(old, old.getMap(), null); // a fresh copy of the map just played
        return true;
    }

    /*
     * Moves a custom game's players to a new custom game on the given map (a new copy of it), keeping the
     * host (even if a co-host triggered the move), the co-host, Game Modifiers, game type, time of day and
     * visibility. Every player moves on, even if the visibility no longer fits them. The old game and its world
     * copy are removed.
     */
    private GameInstance moveCustomGame(GameInstance old, Maps map, Player spectatingHost) {
        List<Player> moving = new ArrayList<>();
        for (Player p : old.players) {
            if (p.isOnline()) moving.add(p);
        }
        // The host watching their own game moves too (see endPlayingCustomGame)
        if (spectatingHost != null && spectatingHost.isOnline() && !moving.contains(spectatingHost)) moving.add(spectatingHost);

        Player host = old.getCustomGameHost() == null ? null : Bukkit.getPlayer(old.getCustomGameHost());
        if (host == null) return old; // the host leaving closes the custom game, so this shouldn't happen

        GameInstance next = new GameInstance(this, map,
                main.getMapWorldManager().createCustomGameWorld(map.GetInstance().worldName, host.getName()));
        next.getMapWorld().setTime(old.getMapWorld().getTime()); // the Time of Day modifier
        games.add(next);
        next.enableCustomGame(host);
        next.setCustomGameVisibility(old.getCustomGameVisibility());
        next.gameType = old.gameType;
        next.getGameSettings().copyModifiersFrom(old.getGameSettings());
        next.copyKickedPlayersFrom(old);

        next.keepPlayers(moving);
        for (Player p : moving) {
            old.closeEditorMenu(p); // an open menu would still be editing the old game
            old.forceRemovePlayer(p);
        }
        removeGame(old); // its world is deleted once everyone has moved over

        for (Player p : moving) {
            joinSinglePlayerToGame(p, map, next);
        }
        next.clearKeptPlayers();
        if (next.HasPlayer(host)) next.giveCustomGameHostItems(host);
        Player coHost = old.getCustomGameCoHost() == null ? null : Bukkit.getPlayer(old.getCustomGameCoHost());
        if (coHost != null && next.HasPlayer(coHost)) next.setCustomGameCoHost(coHost);
        CustomGamesGUI.refreshOpenMenus();
        return next;
    }

    /*
     * The host ended their playing custom game (the Red Dye): it's over with no winner, and everyone in it, the
     * host included, goes to a new lobby on the same map with the same host and settings.
     */
    public void endPlayingCustomGame(GameInstance old, Player host) {
        if (!games.contains(old) || !old.isCustomGame() || old.state != GameState.STARTED || old.getMap() == null) return;
        if (!old.isCustomGameHost(host) || !host.isOnline()) return;

        old.abortCustomGame("&e&l(!) &rThe host ended the game.", host);
        moveCustomGame(old, old.getMap(), host);
    }

    public void sendAcceptedPartyMemberToLeaderGame(Player leader, Player newMember) {
        if (leader == null || newMember == null) return;

        // A custom game host who joined a party closes their lobby (for everyone in it) and follows the leader
        GameInstance ownGame = GetInstanceOfPlayer(newMember);
        if (ownGame != null && ownGame.isCustomGame() && ownGame.isCustomGameHost(newMember)
                && ownGame.state == GameState.WAITING) {
            if (GetInstanceOfPlayer(leader) == ownGame) return; // the leader is already in their lobby

            main.getCommands().removeFromGame(newMember);
            newMember.sendMessage(main.color("&e&l(!) &rYour custom game was cancelled."));
        }

        // A leader in Flag Wars: the new member follows them there
        if (main.getFwGameManager().sendAcceptedPartyMemberToLeaderMatch(leader, newMember)) return;

        GameInstance leaderInstance = GetInstanceOfPlayer(leader);

        if (leaderInstance == null) {
            leaderInstance = GetInstanceOfSpectator(leader);
        }

        if (leaderInstance == null) return;

        Maps map = leaderInstance.getMap();

        if (map == null) return;

        if (GetInstanceOfPlayer(newMember) != null) {
            newMember.sendMessage(main.color("&c&l(!) &rYou are already in a game."));
            return;
        }

        if (GetInstanceOfSpectator(newMember) != null) {
            newMember.sendMessage(main.color("&c&l(!) &rYou are already spectating a game."));
            return;
        }

        if (main.getParkour().hasPlayer(newMember)) {
            newMember.sendMessage(main.color("&c&l(!) &rYou cannot join the leader while in parkour."));
            return;
        }

        if (leaderInstance.state == GameState.WAITING) {
            GameReason result = AddPlayerToGame(newMember, leaderInstance);

            if (result == GameReason.SUCCESS) {
                newMember.setGameMode(GameMode.ADVENTURE);
                main.getListener().resetDoubleJump(newMember);
                main.getLobbyItems().gameLobbyItems(newMember);

                if (main.getSignManager() != null && map.GetInstance() != null) {
                    main.getSignManager().updateSign(map.GetInstance(), leaderInstance);
                }

                newMember.sendMessage(main.color("&a&l(!) &rYou joined the leader's game."));
                leader.sendMessage(main.color("&a&l(!) &e" + newMember.getName() + " &rjoined your game."));
            } else {
                newMember.sendMessage(main.color("&c&l(!) &rYou could not join the leader's game."));
            }

            return;
        }

        if (leaderInstance.state == GameState.STARTED) {
            spectateSinglePlayerGame(newMember, map, leaderInstance);

            newMember.sendMessage(main.color("&a&l(!) &rYou are now spectating the leader's game."));
            leader.sendMessage(main.color("&a&l(!) &e" + newMember.getName() + " &ris now spectating your game."));
            return;
        }

        newMember.sendMessage(main.color("&e&l(!) &rThe leader's game is no longer available."));
    }

    @EventHandler
    public void privateGameItems(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) return;
        boolean rightClick = event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
        boolean leftClick = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        if (!rightClick && !leftClick) return; // only the Maps item reacts to a left click

        GameInstance instance = GetInstanceOfPlayer(player);

        if (instance == null) return;
        if (instance.state != GameState.WAITING) return;
        if (!instance.isCustomGame()) return;

        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? ChatColor.stripColor(item.getItemMeta().getDisplayName()) : "";

        if (item.getType() == Material.EMPTY_MAP && itemName.contains("Maps")) {
            event.setCancelled(true); // right clicking an empty map would make a real map

            // Newer clients also swing their arm on a right click, which arrives as a left click once the menu is open
            if (leftClick && player.getOpenInventory().getType() != org.bukkit.event.inventory.InventoryType.CRAFTING) return;

            if (!instance.canEditCustomGame(player)) {
                player.sendMessage(main.color("&c&l(!) &rOnly the host and co-host can change the map."));
                return;
            }

            if (leftClick) {
                // The map this player's own custom games start on (Random until they pick one)
                new GenericModeGUI(main, null, GameType.CLASSIC, "Default Map", 6, 9, map -> {
                    customGameMaps.set(player, map);
                    player.sendMessage(main.color("&2&l(!) &rYour default map is now "
                            + (map == null ? GameInstance.ONE_FOR_ALL_RANDOM : "&e" + map.getName()) + "&r."));

                    GameInstance current = GetInstanceOfPlayer(player);
                    if (current != null) current.refreshMapsItem(player); // the Maps item shows it
                }).withRandomOption().withSelected(customGameMaps.getPreferred(player), "Your default map").inv.open(player);
            } else {
                // This game's map: the Randomizer picks a map (Left Click) and selects it (Right Click)
                new GenericModeGUI(main, null, GameType.CLASSIC, "Select Map", 6, 9,
                        map -> changeCustomGameMap(player, map)
                ).withSelected(instance.getMap(), "This game's map").inv.open(player);
            }
            return;
        }

        if (!rightClick) return;

        if (item.getType() == Material.EMERALD && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && ChatColor.stripColor(item.getItemMeta().getDisplayName()).contains("Start Game")) {

            event.setCancelled(true);
            // Starting swaps the host's and co-host's items; if it can't start, they keep theirs
            instance.startCustomGameCountdown(player);
            return;
        }

        if (item.getType() == Material.REDSTONE_COMPARATOR
                && itemName.contains("Game Settings")) {

            event.setCancelled(true);

            // Once the countdown is running, Game Settings stops it and gives back the Game Settings + Start Game items
            if (instance.isCustomGameCountdownStarted()) {
                if (!instance.canEditCustomGame(player)) {
                    player.sendMessage(main.color("&c&l(!) &rOnly the host and co-host can stop the countdown."));
                    return;
                }
                instance.cancelCustomGameCountdown(player);
                return;
            }

            if (!instance.canEditCustomGame(player)) {
                player.sendMessage(main.color("&c&l(!) &rOnly the host and co-host can change the settings."));
                return;
            }

            new GameModifiersGUI(main, instance).inv.open(player);
            return;
        }
    }

    /** Joins the given game, or the map's open game (a new one if it has none) when game is null. */
    private void joinSinglePlayerToGame(Player player, Maps map, GameInstance game) {
        GameReason result = game == null ? AddPlayerToMap(player, map) : AddPlayerToGame(player, game);
        GameInstance instance = this.GetInstanceOfPlayer(player);
        MapInstance mi = map.GetInstance();

        if (instance != null) {
            main.getSignManager().updateSign(mi, instance);
        }

        switch (result) {
            case SUCCESS:
                if (instance != null && instance.gameType == GameType.FRENZY) {
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

            case FULL:
                player.sendMessage(main.color("&c&l(!) &rThis game is full!"));
                break;

            default:
                break;
        }
    }

    /** Spectates a game on this map (a running one if there is one). */
    public void SpectatorJoinMap(Player player, Maps map) {
        SpectatorJoinGame(player, map, null);
    }

    /** Spectates this specific game (e.g. a friend's). */
    public void SpectatorJoinGame(Player player, GameInstance game) {
        if (game.getMap() != null) SpectatorJoinGame(player, game.getMap(), game);
    }

    private void SpectatorJoinGame(Player player, Maps map, GameInstance game) {
        // The host of a playing custom game goes back to it on their own, as a spectator
        if (game != null && game.isCustomGame() && game.isCustomGameHost(player)) {
            spectateSinglePlayerGame(player, map, game);
            return;
        }

        if (main.getPartyManager() != null) {
            Party party = main.getPartyManager().getParty(player);

            if (party != null) {
                if (!party.isLeader(player.getUniqueId())) {
                    GameInstance leaderGame = getPartyLeaderGame(party);
                    if (leaderGame != null && (game != null ? leaderGame == game : leaderGame.getMap() == map)) {
                        spectateSinglePlayerGame(player, map, leaderGame);
                        return;
                    }

                    player.sendMessage(main.color("&c&l(!) &rOnly the party leader can spectate games."));
                    return;
                }

                spectatePartyMap(player, party, map, game);
                return;
            }
        }

        spectateSinglePlayerGame(player, map, game);
    }

    private void spectatePartyMap(Player leader, Party party, Maps map, GameInstance game) {
        List<Player> partyMembers = party.getOnlineMembers();

        if (partyMembers.isEmpty()) {
            leader.sendMessage(main.color("&c&l(!) &rYour party has no online members."));
            return;
        }

        for (Player member : partyMembers) {
            if (GetInstanceOfPlayer(member) != null) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris already in a game."));
                return;
            }

            if (GetInstanceOfSpectator(member) != null) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris already spectating a game."));
                return;
            }

            if (main.getFwGameManager().getInstanceOfPlayer(member) != null
                    || main.getFwGameManager().getInstanceOfSpectator(member) != null) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris already in a game."));
                return;
            }

            if (main.getParkour().hasPlayer(member)) {
                leader.sendMessage(main.color("&c&l(!) &e" + member.getName() + " &ris currently in parkour."));
                return;
            }
        }

        if (game == null) game = getSpectatableGame(map); // the whole party watches the same game
        for (Player member : partyMembers) {
            spectateSinglePlayerGame(member, map, game);
        }

        for (Player member : partyMembers) {
            member.sendMessage(main.color("&a&l(!) &rYour party is now spectating &e" + map.toString() + "&r."));
        }
    }

    private void spectateSinglePlayerGame(Player player, Maps map, GameInstance game) {
        if (game != null && game.spectators.contains(player)) {
            player.sendMessage(main.color("&c&l(!) &rYou are already spectating this game."));
            return;
        }

        GameReason result = game == null ? AddSpectatorToMap(player, map) : AddSpectatorToGame(player, game);

        switch (result) {
            case SPECTATOR:
                player.sendMessage(main.color("&2&l(!) &rYou are now spectating &a" + map.toString() +
                        "&r. Use &a/leave &rto leave"));
                player.setGameMode(GameMode.ADVENTURE);
                player.spigot().setCollidesWithEntities(false);
                player.getInventory().clear();
                main.getLobbyItems().spectatorItems(player);
                if (game != null) game.giveHostEndGameItem(player); // the host can end their own game
                break;

            case ALREADY_IN:
                player.sendMessage(main.color("&c&l(!) &rYou have to leave your game to Spectate"));
                break;

            case IN_ANOTHER:
                player.sendMessage(main.color("&c&l(!) &rYou have to leave your game to Spectate"));
                break;

            case FAIL:
                player.sendMessage(main.color("&c&l(!) &rThis game is not playing!"));
                break;

            default:
                break;
        }
    }

	public GameReason AddSpectatorToMap(Player player, Maps map) {
		GameInstance instance = null;

        // Playing or spectating any game (SCB or Flag Wars) blocks spectating another one. Without the
        // spectator half, a spectator could be listed in two games at once and the first game ending
        // would drag them out of the second. The caller already tells the player why.
		if (main.isPlayerInAnyGame(player)) {
			return GameReason.IN_ANOTHER;
		}

		instance = getSpectatableGame(map);
		if (instance == null) return GameReason.FAIL; // nothing to watch on this map

		return instance.AddSpectator(player);
	}

	public GameReason AddSpectatorToGame(Player player, GameInstance game) {
		if (GetInstanceOfPlayer(player) != null) return GameReason.IN_ANOTHER; // the caller tells them
		if (game == null || !games.contains(game)) return GameReason.FAIL;

		// Visibility only decides who can join: anyone can watch a custom game, whoever it's open to
		return game.AddSpectator(player);
	}

	/*
	 * This function adds player to the game they are joining as long as they are
	 * not in another game
	 */
	public GameReason AddPlayerToMap(Player player, Maps map) {
		GameInstance instance = null;

		// Includes spectators of SCB and Flag Wars games, not just players. A Flag Wars spectator who joined
		// an SCB game stayed listed in both, so the Flag Wars game ending teleported them out of the SCB game
		// while they were still in its player list.
		if (main.isPlayerInAnyGame(player) || getMain().getParkour().hasPlayer(player))
			return GameReason.IN_ANOTHER;

        instance = getInstanceOfMap(map);
        if (instance == null) instance = createGame(map); // a new game on its own copy of the map

        return AddPlayerToGame(player, instance);
	}

	/** Adds the player to this specific game (e.g. their party leader's). */
	public GameReason AddPlayerToGame(Player player, GameInstance instance) {
		if (GetInstanceOfPlayer(player) != null || getMain().getParkour().hasPlayer(player))
			return GameReason.IN_ANOTHER;
		if (!games.contains(instance) && !gameMap2.containsValue(instance)) return GameReason.FAIL;

        if (instance.isCustomGame() && !instance.canAccessCustomGame(player)) {
            player.sendMessage(instance.getCustomGameAccessMessage());
            discardIfEmpty(instance);
            return GameReason.FAIL;
        }

        GameReason reason = instance.AddPlayer(player);
        if (reason != GameReason.SUCCESS) discardIfEmpty(instance); // don't keep a world copy around for nobody

		return reason;
	}

	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent event) {
		if ((event.getAction() == Action.PHYSICAL) && (event.getClickedBlock().getType() == Material.SOIL))
			event.setCancelled(true);
	}

	@EventHandler
	public void fireFlowerItem(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance instance = this.GetInstanceOfPlayer(player);

		if (instance != null && instance.state == GameState.STARTED) {
			if (item != null && item.getType() == Material.RED_ROSE) {
				ItemMeta meta = item.getItemMeta();

				if (meta != null && meta.getDisplayName() != null &&
						ChatColor.stripColor(meta.getDisplayName()).contains("FIRE FLOWER")) {
					player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1, 1);
					if (FakeLoot.isFake(item)) {
						// A fake Fire Flower (the Grinch's): used up like the real one, but it only plants a poppy
						if (item.getAmount() <= 1)
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
						else
							item.setAmount(item.getAmount() - 1);
						shootFakeFireFlower(instance, player);
						return;
					}
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
										if (gamePlayer == player) continue;
										if (instance.getGameManager().spawnProt.containsKey(gamePlayer)) continue;
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
	public void nukeItem(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		GameInstance i = this.GetInstanceOfPlayer(player);

		if (i != null && i.state == GameState.STARTED) {
			if (item != null && item.getType() == Material.TNT) {
				Action action = event.getAction();
				if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
					return;

				if (item.getAmount() % 3 == 0) {
					boolean fakeNuke = FakeLoot.isFake(item); // a fake Nuke (the Grinch's): TNT that only knocks back
					if (item.getAmount() == 3)
						player.getInventory().clear(player.getInventory().getHeldItemSlot());
					else
						item.setAmount(item.getAmount() - 3);
					Location loc = player.getTargetBlock((HashSet<Byte>) null, 25).getLocation();
					TNTPrimed tnt = player.getWorld().spawn(loc.add(0, 1, 0), TNTPrimed.class);
					if (fakeNuke) FakeLoot.markEntity(tnt);
					tnt.setFuseTicks(50);
					tnt = player.getWorld().spawn(loc.add(1, 1, 0), TNTPrimed.class);
					if (fakeNuke) FakeLoot.markEntity(tnt);
					tnt.setFuseTicks(50);
					tnt = player.getWorld().spawn(loc.add(1, 1, 1), TNTPrimed.class);
					if (fakeNuke) FakeLoot.markEntity(tnt);
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
		// Flag Wars' own Fire Charge handles its own explosion (FWPlayerListener#onFireChargeExplode) - skip it here.
		if (e.getEntity().hasMetadata("fw_fire_charge")) return;

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
		// Flag Wars' own Fire Charge/TNT handle their own explosions (FWPlayerListener#onFireChargeExplode/onTntExplode) - skip them here.
		if (event.getEntity().hasMetadata("fw_fire_charge") || event.getEntity().hasMetadata("fw_tnt")) return;

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

    /*
    * This function returns if a player is in a game
     */
	public GameInstance GetInstanceOfPlayer(Player player) {
		for (GameInstance game : games)
			if (game.HasPlayer(player))
				return game;
		return null;
	}

    /*
     * This function returns if a player is spectating a game
     */
	public GameInstance GetInstanceOfSpectator(Player spectator) {
		for (GameInstance game : games)
			if (game.HasSpectator(spectator))
				return game;
		return null;
	}

	/*
	 * The game players joining this map go to: its open (waiting, not private) game, or null if it has none -
	 * joining then starts a new game on a new copy of the map, even while other games are playing on it.
	 */
	public GameInstance getInstanceOfMap(Maps map) {
		for (GameInstance game : games) {
			if (game.getMap() == map && game.state == GameState.WAITING && !game.isCustomGame()) return game;
		}
		return null;
	}

	/** A game on this map to spectate: a running one if there is one, otherwise one waiting to start. */
	public GameInstance getSpectatableGame(Maps map) {
		GameInstance waiting = null;
		for (GameInstance game : games) {
			if (game.getMap() != map || game.isCustomGame()) continue;
			if (game.state == GameState.STARTED) return game;
			if (game.state == GameState.WAITING && waiting == null) waiting = game;
		}
		return waiting;
	}

	/** A new game on its own copy of the map. */
	public GameInstance createGame(Maps map) {
		GameInstance game = new GameInstance(this, map);
		games.add(game);
		return game;
	}

	/*
	 * Removes a game (when it's over, or everyone left) and deletes its map copy. Spectators still watching are
	 * sent back first, while the game can still be found for them. Safe to call more than once.
	 */
	public void removeGame(GameInstance game) {
		if (games.contains(game) || gameMap2.containsValue(game)) {
			for (Player spectator : new ArrayList<>(game.spectators)) main.getCommands().removeFromGame(spectator);
			games.remove(game);
			gameMap2.values().remove(game);
			if (game.isCustomGame()) CustomGamesGUI.refreshOpenMenus();
		}
		game.discardWorld();
	}

	/*
	 * A custom game's host is leaving its lobby (/leave, quitting, ...): the custom game is cancelled for everyone.
	 * A game that's already playing isn't closed (see RemovePlayerFromAll). The host carries on leaving like anyone else.
	 */
	private void closeCustomGame(GameInstance game, Player leaving) {
		if (game.players.size() <= 1 && game.spectators.isEmpty()) return; // nobody else - removed once they're out
		game.cancelCustomGame("&e&l(!) &rThe custom game was closed because its host left.", leaving);
	}

	/** A game nobody ended up joining (e.g. the join failed) is removed again. */
	private void discardIfEmpty(GameInstance game) {
		if (game.players.isEmpty() && game.spectators.isEmpty()) removeGame(game);
	}

	public boolean RemovePlayerFromAll(Player player) {
		GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
		boolean found = false;

		// The host leaving the lobby closes the game, but one that's already playing keeps running without them
		if (instance != null && instance.isCustomGame() && instance.isCustomGameHost(player) && !instance.isCancellingCustomGame()
				&& instance.state == GameState.WAITING)
			closeCustomGame(instance, player);
		if (instance != null && instance.isCustomGameCoHost(player) && !instance.isCancellingCustomGame()) {
			instance.setCustomGameCoHost(null);
			instance.TellAll(main.color("&e&l(!) &e" + player.getName() + " &rleft. There is no co-host now."));
		}

		if (instance != null) {
			if (instance.getMap() != null) {
				List<GameInstance> toRemove = new ArrayList<>();

				for (GameInstance other : new ArrayList<>(games)) {
					if (other.RemovePlayer(player)) {
						if (other.players.isEmpty()) {
							toRemove.add(other);
						}

						found = true;
					}
				}

				for (GameInstance game : toRemove)
					removeGame(game);
			} else if (instance.duosMap != null) {
				List<GameInstance> toRemove = new ArrayList<>();

				for (GameInstance other : new ArrayList<>(gameMap2.values())) {
					if (other.RemovePlayer(player)) {
						if (other.players.size() == 0)
							toRemove.add(other);

						found = true;
					}
				}

				for (GameInstance game : toRemove)
					removeGame(game);
			}

			instance.getGameSettings().removeFromStartVotes(player);
		}

		return found;
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

    /*
    * This function adds a player to the spawn protection for 5 seconds when the
    * game starts, or a player just respawned
     */
    public void addSpawnProtection(Player player) {
        BukkitRunnable runnable = this.spawnProt.get(player);
        GameInstance instance = this.GetInstanceOfPlayer(player);

        // Spawn Protection modifier (custom games): 0 turns it off, otherwise it's how many seconds it lasts
        int duration = instance != null && instance.isCustomGame()
                ? instance.getGameSettings().spawnProtectionSeconds : SPAWN_PROT_DURATION;

        if (duration <= 0) {
            if (runnable != null) {
                runnable.cancel();
                this.spawnProt.remove(player);
            }

            return;
        }

        if (runnable == null) {
			runnable = new BukkitRunnable() {
				int ticks = duration;

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
			spawnProtParticles(player, instance, duration);
		}
	}

	/**
	 * This function spawns Spawn Protection particles around the player
	 *
	 * @param player
	 * @param instance
	 */
	private void spawnProtParticles(Player player, GameInstance instance, int durationSeconds) {
		int durationTicks = durationSeconds * 20;
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
				if (gameInstance.isChoosingOneForAllClass()) {
					new OneForAllClassGUI(main, gameInstance).inv.open(player);
				} else if (gameInstance.state == GameState.WAITING) {
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
		case INK_SACK:
			// Red Dye: the host of a playing custom game, spectating it, can end it for everyone
			if (gameInstanceSpectator != null && gameInstanceSpectator.spectators.contains(player)
					&& gameInstanceSpectator.isCustomGameHost(player) && GameInstance.isEndGameItem(item)
					&& (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK))
				new EndGameGUI(this.main, gameInstanceSpectator).inv.open(player);
			break;
		case MONSTER_EGG:
			if (gameInstance != null && gameInstance.state == GameState.STARTED) {
				if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
					return;
				// A fake Pokeball (the Grinch's) spawns something harmless or feeble instead
				if (FakeLoot.isFake(item)) {
					throwFakePokeball(gameInstance, player, item);
					break;
				}
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
				} else if (meta.getDisplayName().toLowerCase().contains("cave spider")) {
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
								CaveSpider mob = (CaveSpider) player.getWorld().spawnCreature(hitLoc, EntityType.CAVE_SPIDER);
								customizeMob(mob, player);
								customizeCaveSpider(mob);
								mob.setTarget(gameInstance.getNearestPlayer(player, mob, 150));
							}

						}, ItemHelper.createMonsterEgg(EntityType.CAVE_SPIDER, 1));
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

	public static Player getMobOwner(LivingEntity creature) {
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

	private void customizeCaveSpider(CaveSpider caveSpider) {
		caveSpider.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false));
	}

	private static final int FAKE_BAT_SECONDS = 30;

	/**
	 * A fake Pokeball (the Grinch's): used up and thrown like the real one, and what it spawns is named after whoever
	 * threw it like the real mobs are (so it goes when they die). The Witch Pokeball has no fake.
	 */
	private void throwFakePokeball(final GameInstance instance, final Player player, ItemStack item) {
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
			return;
		String name = item.getItemMeta().getDisplayName().toLowerCase();

		final EntityType type;
		if (name.contains("zombie"))
			type = EntityType.ZOMBIE;
		else if (name.contains("skeleton"))
			type = EntityType.SKELETON;
		else if (name.contains("slime"))
			type = EntityType.SLIME;
		else if (name.contains("silverfish"))
			type = EntityType.SILVERFISH;
		else if (name.contains("creeper"))
			type = EntityType.CREEPER;
		else if (name.contains("cave spider"))
			type = EntityType.CAVE_SPIDER;
		else
			return;

		// The Grinch who threw this fake drop: the Zombie and the Skeleton are named after him, not the player
		UUID grinchId = FakeLoot.ownerOf(item);
		final Player grinch = grinchId == null ? null : Bukkit.getPlayer(grinchId);

		if (item.getAmount() <= 1)
			player.getInventory().clear(player.getInventory().getHeldItemSlot());
		else
			item.setAmount(item.getAmount() - 1);

		ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				spawnFakePokeballMobs(instance, player, grinch, type, this.getBaseProj().getEntity().getLocation());
			}
		}, ItemHelper.createMonsterEgg(type, 1));
		instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(2.0D));
	}

	/**
	 * What a fake Pokeball spawns instead of the real mob: the Zombie a feeble one (leather, a wooden sword, 1 health),
	 * the Skeleton one with 1 health (and its bow), the Slime 4 small slimes, the Silverfish 3 bats, the Creeper a
	 * pig and the Cave Spider a chicken.
	 * <p>
	 * The Zombie and the Skeleton are the Grinch's: they carry his name (so they go when he dies and never go after
	 * him), and they go after whoever used the Pokeball (owner). The grinch is null if he has left.
	 */
	private void spawnFakePokeballMobs(final GameInstance instance, Player owner, Player grinch, EntityType type,
			Location loc) {
		World world = loc.getWorld();
		Player namedAfter = grinch != null ? grinch : owner;

		switch (type) {
		case ZOMBIE: {
			Zombie zombie = world.spawn(loc, Zombie.class);
			customizeMob(zombie, namedAfter);
			zombie.setBaby(false);
			EntityEquipment equipment = zombie.getEquipment();
			equipment.setHelmet(new ItemStack(Material.LEATHER_HELMET));
			equipment.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
			equipment.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
			equipment.setBoots(new ItemStack(Material.LEATHER_BOOTS));
			equipment.setItemInHand(new ItemStack(Material.WOOD_SWORD));
			zombie.setMaxHealth(1.0);
			zombie.setHealth(1.0);
			zombie.setTarget(owner);
			FakeLoot.markEntity(zombie); // any hit kills it, see FakeLootListener
			reassertFakeMob(instance, zombie, owner);
			break;
		}
		case SKELETON: {
			Skeleton skeleton = world.spawn(loc, Skeleton.class);
			customizeMob(skeleton, namedAfter);
			customizeSkeleton(skeleton); // its bow, the same as the real one's
			skeleton.setMaxHealth(1.0);
			skeleton.setHealth(1.0);
			skeleton.setTarget(owner);
			FakeLoot.markEntity(skeleton); // 1 health: any hit kills it, see FakeLootListener
			reassertFakeMob(instance, skeleton, owner);
			break;
		}
		case SLIME:
			for (int i = 0; i < 4; i++) {
				Slime slime = world.spawn(spreadAround(loc), Slime.class);
				slime.setSize(1);
				nameFakeMob(slime, owner);
			}
			break;
		case SILVERFISH: {
			final List<Bat> bats = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				Bat bat = world.spawn(spreadAround(loc), Bat.class);
				nameFakeMob(bat, owner);
				bats.add(bat);
			}
			// Bats flap about forever, so they go after a while
			instance.trackClassTask(Bukkit.getScheduler().runTaskLater(main, new Runnable() {
				@Override
				public void run() {
					for (Bat bat : bats)
						bat.remove();
				}
			}, FAKE_BAT_SECONDS * 20L));
			break;
		}
		case CREEPER:
			customizeMob(world.spawn(loc, Pig.class), owner);
			break;
		case CAVE_SPIDER:
			customizeMob(world.spawn(loc, Chicken.class), owner);
			break;
		default:
			break;
		}
	}

	/**
	 * A tick after it spawns, makes sure the fake Zombie and Skeleton really have 1 health and still go after the
	 * player who used the Pokeball, in case spawning (which picks the mob a target) changed them back.
	 */
	private void reassertFakeMob(GameInstance instance, final LivingEntity mob, final Player target) {
		Runnable reassert = new Runnable() {
			@Override
			public void run() {
				if (!mob.isValid())
					return;
				mob.setMaxHealth(1.0);
				mob.setHealth(1.0);
				if (mob instanceof Creature && ((Creature) mob).getTarget() != target && target.isOnline()
						&& !FakeLoot.isOut(target, instance.classes.get(target)))
					((Creature) mob).setTarget(target);
			}
		};
		// Twice, a tick and half a second after the spawn. Tracked, so they are cancelled if the game ends first
		instance.trackClassTask(Bukkit.getScheduler().runTaskLater(main, reassert, 1L));
		instance.trackClassTask(Bukkit.getScheduler().runTaskLater(main, reassert, 10L));
	}

	private Location spreadAround(Location loc) {
		return loc.clone().add(Math.random() * 1.2 - 0.6, 0, Math.random() * 1.2 - 0.6);
	}

	/** Names a mob that isn't a Creature (a Slime, a Bat) after its owner, like customizeMob does for the others. */
	private void nameFakeMob(LivingEntity mob, Player owner) {
		mob.setRemoveWhenFarAway(false);
		mob.setCustomName(ChatColorHelper.color("&c" + owner.getName() + "'s &e" + getMobTypeName(mob.getType())));
		mob.setCustomNameVisible(true);
	}

	private void customizeMob(Creature mob, Player player) {
		// Setting Mob to not de-spawn when far away
		mob.setRemoveWhenFarAway(false);
		// Setting Mob Name to owner's
		mob.setCustomName(ChatColorHelper.color("&c" + player.getName() + "'s &e" + getMobTypeName(mob.getType())));
		// Setting Custom name visible
		mob.setCustomNameVisible(true);
	}

	public static String getMobTypeName(EntityType entityType) {
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
		case BAT:
			return "Bat";
		case PIG:
			return "Pig";
		case CHICKEN:
			return "Chicken";
		case SPIDER:
			return "Spider";
		case CAVE_SPIDER:
			return "Cave Spider";
		case SLIME:
			return "Slime";
		case BLAZE:
			return "Blaze";
		}
		return "Creature";
	}
}
