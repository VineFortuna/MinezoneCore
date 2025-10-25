package anthony.SuperCraftBrawl;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.fishing.FishArea;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.gui.christmas.ChristmasRewardsGUI;
import anthony.SuperCraftBrawl.gui.cosmetics.CosmeticsGUI;
import anthony.SuperCraftBrawl.leaderboards.LeaderboardScope;
import anthony.SuperCraftBrawl.npcs.ChannelInjector;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.PathfinderGoalFollowPlayer;
import anthony.util.PathfinderHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerListener implements Listener {

	private final Core main;
	public ScoreboardManager scoreManager = Bukkit.getScoreboardManager();
	public Scoreboard c;
	public List<Player> snowParticlePlayers = new ArrayList<Player>();
	public Map<Player, Snowman> snowmanPetPlayers = new HashMap<>();
	public List<Player> candyCaneSwirlPlayers = new ArrayList<Player>();
	public List<Player> elfCosmeticPlayers = new ArrayList<Player>();
	public List<Player> goldenOutfitPlayers = new ArrayList<>();
	public List<Player> freddyOutfitPlayers = new ArrayList<>();
    private BukkitTask announcementsTask;

	public PlayerListener(Core main) {
		this.main = main;
		this.main.getServer().getPluginManager().registerEvents(this, main);
		this.c = scoreManager.getNewScoreboard();
	}

    /*
    * This function shows the server messages that appear every 5 minutes
     */
    public void messages() {
        // Don’t schedule more than once
        if (announcementsTask != null) return;

        announcementsTask = new BukkitRunnable() {
            @Override
            public void run() {
                Announcements[] all = Announcements.values();
                Announcements msg = all[java.util.concurrent.ThreadLocalRandom.current().nextInt(all.length)];

                String toSend = msg.getName();
                if (toSend == null || toSend.isEmpty()) return;

                for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    if (main.getGameManager().GetInstanceOfPlayer(p) != null) continue;
                    p.sendMessage(toSend);
                }
            }
        }.runTaskTimer(main, 0L, 5L * 60L * 20L); // every 5 minutes
    }

    // Gets called in onDisable() in Core class
    public void cancelMessagesTask() {
        if (announcementsTask != null) {
            announcementsTask.cancel();
            announcementsTask = null;
        }
    }

    /**
	 * This function just resets player double jump & sets gamemode to Adventure
	 * 
	 * @param p to be reset
	 */
	public void resetDoubleJump(Player p) {
		p.setAllowFlight(false);
		p.setAllowFlight(true);
		p.setGameMode(GameMode.ADVENTURE);
	}
	
	public void removeCosmetics(Player player) {
		main.getTrickTitle().disable(player);
	}

	/**
	 * This function resets the armor of a player
	 * 
	 * @param p which is Player to remove armor
	 */
	public void resetArmor(Player p) {
		p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
		p.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
		p.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
		p.getInventory().setBoots(new ItemStack(Material.AIR, 1));
	}

	/**
	 * This function resets the Player's potion effects if any is active
	 * 
	 * @param p which is Player to remove effects
	 */
	public void resetPotionEffects(Player p) {
		for (PotionEffect type : p.getActivePotionEffects()) // Loop through all active effects
			p.removePotionEffect(type.getType());
	}
	
	public void checkIfLevelUp(Player player) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		
		if (data != null) {
			if (data.exp >= 2500) {
				data.level++;
				data.exp -= 2500;
				player.sendMessage(main.color("&8&m----------------------------------------"));
				player.sendMessage(main.color("&6&l✦✦ &e&lLEVEL UP! &6&l✦✦"));
				player.sendMessage(main.color("&7You are now &e&lLevel &6&l" + data.level + " &7— nice work!"));
				player.sendMessage(main.color("&8&m----------------------------------------"));
				player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.15f);
				
				if (player.getWorld() == main.getLobbyWorld())
					main.getScoreboardManager().lobbyBoard(player);
			}
		}
	}

	/**
	 * This function sets the player's rank on the tablist to the left of their name
	 * 
	 * @param p which is Player to set rank on tablist
	 */
	@SuppressWarnings("deprecation")
	public void setPlayerOnTablist(Player p) {
		String rank = main.getRankManager().getRank(p).getTagWithSpace(); // Gets the player's rank
		Rank r = main.getRankManager().getRank(p);

		if (rank.length() >= 16) {
			String s = rank.substring(0, 9);
			p.setPlayerListName("" + s + " " + r.getColorForNames(p, r));
		} else
			p.setPlayerListName("" + rank + r.getColorForNames(p, r));

		if (main.getRankManager().getRank(p) == Rank.DEFAULT)
			p.setPlayerListName("" + rank + r.getColorForNames(p, r));

		/*
		 * Team captain = c.registerNewTeam("b_captain");
		 * captain.setPrefix(Rank.CAPTAIN.getTagWithSpace()); Team owner =
		 * c.registerNewTeam("a_owner"); owner.setPrefix(Rank.OWNER.getTagWithSpace());
		 * 
		 * if (main.getRankManager().getRank(p) == Rank.CAPTAIN) captain.addPlayer(p);
		 * else if (main.getRankManager().getRank(p) == Rank.OWNER) owner.addPlayer(p);
		 * 
		 * p.setScoreboard(c);
		 * 
		 * if (main.getTabManager() != null) main.getTabManager().setPlayerTeam(p);
		 */
	}

    // Track the follow task for each player's snowman so we can cancel it.
    private final Map<UUID, Integer> snowmanTasks = new HashMap<>();

    private void cancelSnowmanTask(UUID uuid) {
        Integer id = snowmanTasks.remove(uuid);
        if (id != null) {
            try { Bukkit.getScheduler().cancelTask(id); } catch (Throwable ignored) {}
        }
    }

    public int getHalloweenEventProgress(Player player) {
		int progress = (main.getHalloweenManager() != null)
				? main.getHalloweenManager().getFoundCount(player.getUniqueId())
				: 0;
		
		return progress;
	}

	// Clicking leaderboard settings in lobby
    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) return;

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        String raw = stand.getCustomName();
        if (raw == null) return;

        // Strip colors so either colored or plain names work
        String name = org.bukkit.ChatColor.stripColor(raw).trim().toLowerCase();

        // Open the scope picker when clicking your settings/title stands
        if (name.equals("leaderboard settings")
                || name.equals("click to change settings")
                // OPTIONAL: also allow clicking board titles themselves:
                || name.contains("wins")
                || name.contains("kills")
                || name.contains("flawless")
                || name.contains("fishing")) {

            new anthony.SuperCraftBrawl.gui.leaderboard.LeaderboardScopeGUI(main).inv().open(player);
            event.setCancelled(true);
        }
    }

    /**
	 * This function checks if tournament mode is active on Player Join
	 * 
	 * @param p which is Player to add to the tournament
	 */
	public void checkIfTournament(Player p) {
		if (main.tournament) {
			PlayerData data = main.getDataManager().getPlayerData(p);
			if (main.tourneyreset) {
				if (!main.tourney.containsKey(p.getName()))
					data.points = 0;
				else
					data.points = main.tourney.get(p.getName());
			}
			main.tourney.put(p.getName(), data.points);
		}
	}

	// EVENTS:

	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().teleport(main.GetHubLoc());
		for (int i = 0; i < 9; i++) {
			event.getPlayer().getInventory().setItem(i, new ItemStack(Material.WOOD_SWORD));
		}
		Bukkit.getScheduler().runTaskLater(main, () -> {
			event.getPlayer().getInventory().clear();
			main.ResetPlayer(event.getPlayer());
		}, 20);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            Player p = event.getPlayer();

            // Default their personal view to Lifetime (only set once)
            main.leaderboardScopeByViewer.putIfAbsent(p.getUniqueId(), LeaderboardScope.LIFETIME);

            // Get their current totals from PlayerData
            PlayerData data = main.getDataManager().getPlayerData(p);
            if (data == null) return; // safety

            // If your fields are getters, swap to data.getWins() etc.
            int wins         = data.wins;
            int kills        = data.kills;
            int flawlessWins = data.flawlessWins;
            int totalCaught  = data.totalcaught;

            // Seed snapshots for THIS player so daily/weekly/monthly = current - snapshot
            try {
                String uuid = p.getUniqueId().toString();

                // Wins
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Wins", LeaderboardScope.DAILY,   wins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Wins", LeaderboardScope.WEEKLY,  wins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Wins", LeaderboardScope.MONTHLY, wins);

                // Kills
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Kills", LeaderboardScope.DAILY,   kills);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Kills", LeaderboardScope.WEEKLY,  kills);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Kills", LeaderboardScope.MONTHLY, kills);

                // Flawless Wins
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "FlawlessWins", LeaderboardScope.DAILY,   flawlessWins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "FlawlessWins", LeaderboardScope.WEEKLY,  flawlessWins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "FlawlessWins", LeaderboardScope.MONTHLY, flawlessWins);

                // Fishing total caught
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "TotalCaught", LeaderboardScope.DAILY,   totalCaught);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "TotalCaught", LeaderboardScope.WEEKLY,  totalCaught);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "TotalCaught", LeaderboardScope.MONTHLY, totalCaught);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 40L); // ~2 seconds after join; adjust if your data load needs more/less time
	}

	@EventHandler
	public void OnPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
		candyCaneSwirlPlayers.remove(player);
		snowParticlePlayers.remove(player);
		if (snowmanPetPlayers.containsKey(player))
			snowmanPetPlayers.get(player).remove();
		snowmanPetPlayers.remove(player);
		elfCosmeticPlayers.remove(player);

		if (instance != null)
			main.getGameManager().RemovePlayerFromAll(player);

        main.getScoreboardManager().removeLobbyBoard(player);
        Player p = event.getPlayer();

        // Scoreboards
        main.getScoreboardManager().removeLobbyBoard(p);
        try { Holograms h = main.holograms.remove(p); if (h != null) h.destroyBoards(); } catch (Throwable ignored) {}
        try { main.getFishing().cleanupAll(); } catch (Throwable ignored) {}

        // Holograms / packet armor stands
        main.hologramCleanup(p);

        // Fishing
        safeFishingCleanup(p);

        // Game instance (ensure game structures release this player)
        GameInstance gi = main.getGameManager().GetInstanceOfPlayer(p);
        if (gi != null) {
            gi.forceRemovePlayer(p); // implement to clear maps/boards/cooldowns for this player
        }

        main.getScoreboardManager().removeLobbyBoard(player);
        main.staffchat.remove(player);
        main.globalchat.remove(player);

        // Any Player->... maps in Core
        main.forgetPlayerEverywhere(p);
        cancelSnowmanTask(player.getUniqueId());
        main.sentMysteryHolos.remove(player.getUniqueId());
        main.sentParkourHolos.remove(player.getUniqueId());
        ChannelInjector.uninject(player);
        removeLeaderboards(event);
        main.getTitleAnimationManager().stop(player);
    }

    private void removeLeaderboards(PlayerQuitEvent event) {
        try { if (main.getKillsLeaderboard() != null)    main.getKillsLeaderboard().clearViewerHologram(event.getPlayer()); } catch (Throwable ignored) {}
        try { if (main.getLeaderboard() != null)     main.getLeaderboard().clearViewerHologram(event.getPlayer()); } catch (Throwable ignored) {}
        //try { if (main.flawlessBoard != null) main.flawlessBoard.clearViewerHologram(event.getPlayer()); } catch (Throwable ignored) {}
        //try { if (main.fishingBoard != null)  main.fishingBoard.clearViewerHologram(event.getPlayer()); } catch (Throwable ignored) {}
    }

    private void safeFishingCleanup(Player p) {
        try {
            if (main.getFishing() != null) {
                main.getFishing().cleanup(p);
            }
        } catch (Throwable t) {
            // swallow – we never want a cleanup error to block logout flow
            main.getLogger().warning("[Fishing] cleanup failed for " + p.getName() + ": " + t.getMessage());
        }
    }

    @EventHandler
	public void waterNoFlow(BlockFromToEvent e) {
		if (main.getCommands() != null)
			e.setCancelled(true);
		else
			e.setCancelled(false);
	}

	@EventHandler
	public void onEnderChestInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			if (event.getClickedBlock().getType() == Material.ENDER_CHEST
					&& event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				event.setCancelled(true);
				new ChristmasRewardsGUI(main).inv.open(event.getPlayer());
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (this.snowParticlePlayers.contains(player) && player.getWorld() == main.getLobbyWorld()) {
			Location loc = player.getLocation().add(0, 0.2, 0);

			// Particle Settings
			EnumParticle particleType = EnumParticle.CLOUD; // Example: CLOUD looks like a snow effect
			boolean longDistance = false;
			float offsetX = 0.3f;
			float offsetY = 0.3f;
			float offsetZ = 0.3f;
			float speed = 0f;
			int count = 5;

			PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleType, // The EnumParticle type
					longDistance, // Long distance rendering
					(float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), offsetX, offsetY, offsetZ, speed,
					count);

			// Send the packet to all online players, so everyone can see the trail
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@EventHandler
	public void onJumpPadStep(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		// Lobby jump pad
		if (player.getWorld() == main.getLobbyWorld()) {
			Location location = player.getLocation();

			// Check if the block below the player is a gold block
			if (player.isOnGround() && location.getBlock().getType() == Material.GOLD_PLATE) {
				// Check if the player is facing south
				float yaw = location.getYaw();
				if (isFacingSouth(yaw)) {
					// Set the boost direction to south
					Vector direction = new Vector(0, 1.25, 3); // Current facing direction

					// Apply the velocity to the player
					player.setVelocity(direction);

					player.getWorld().playSound(location, Sound.BAT_TAKEOFF, 1, 5);
				}
			}
		}
	}

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        Core main = this.main; // adjust if you use a different accessor

        // 1) Remove lobby board if they left the lobby world
        try {
            World lobby = main.getLobbyWorld();
            if (lobby != null && !p.getWorld().equals(lobby)) {
                main.getScoreboardManager().removeLobbyBoard(p);
            }
        } catch (Throwable ignored) {}

        // 2) Kill any per-player holograms
        try {
            Holograms h = main.holograms.get(p);
            if (h != null) h.destroyBoards();
            EntityArmorStand stand = main.msHologram.remove(p);
            if (stand != null) {
                PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(stand.getId());
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroy);
            }
        } catch (Throwable ignored) {}

        // 3) Fishing cleanup (no dangling hooks)
        try {
            main.getFishing().cleanupAll(); // safe no-op if nothing to do
        } catch (Throwable ignored) {}

        // 4) If the player left a game, ensure the instance drops references
        try {
            GameInstance gi = main.getGameManager().GetInstanceOfPlayer(p);
            if (gi != null && gi.state != GameState.STARTED) {
                // Remove any per-player tasks/boards/effects by UUID
                UUID id = p.getUniqueId();
                gi.boards.remove(id);
                gi.effects.remove(id);
            }
        } catch (Throwable ignored) {}

        main.getKillsLeaderboard().clearViewerHologram(p);
        try { if (main.getKillsLeaderboard() != null)    main.getKillsLeaderboard().clearViewerHologram(e.getPlayer()); } catch (Throwable ignored) {}
        try { if (main.getLeaderboard() != null)     main.getLeaderboard().clearViewerHologram(e.getPlayer()); } catch (Throwable ignored) {}
        //try { if (main.flawlessBoard != null) main.flawlessBoard.clearViewerHologram(event.getPlayer()); } catch (Throwable ignored) {}
        //try { if (main.fishingBoard != null)  main.fishingBoard.clearViewerHologram(event.getPlayer()); } catch (Throwable ignored) {}
    }

    @EventHandler
	public void onEnterFishingArea(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();

		// Ensure the event occurs in the lobby world
		if (player.getWorld().equals(main.getLobbyWorld())) {
			// Ignore if the player hasn't moved to a new block
			if (to == null || to.equals(event.getFrom())) {
				return;
			}

			// Check if the player is entering a fishing area
			FishArea newArea = main.getFishingArea(to);
			FishArea previousArea = main.getFishingArea(event.getFrom());

			if (previousArea == null && newArea != null) {
				String msg = main.color("&3&l(!) &rEntering &e" + newArea.getName());
				PacketPlayOutChat packet = new PacketPlayOutChat(
						IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
				PlayerData data = main.getDataManager().getPlayerData(player);
				if (!data.getFishingWarps().contains(newArea.getID())) {
					player.sendTitle(main.color("&6" + newArea.getName()), main.color("&eArea discovered"));
					data.addFishingWarp(newArea.getID());
					main.getDataManager().saveData(data);
				}
			} else if (previousArea != null && newArea == null) {
				String msg = main.color("&3&l(!) &rLeaving &e" + previousArea.getName());
				PacketPlayOutChat packet = new PacketPlayOutChat(
						IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	private boolean isFacingSouth(float yaw) {
		// Normalize yaw to 0-360 degrees
		yaw = (yaw % 360 + 360) % 360;

		// Check if yaw is within the range for south direction
		return (yaw >= 337.5 || yaw <= 22.5);
	}

    public void snowmanPet(Player player) {
        UUID uid = player.getUniqueId();

        // If we already had a task running for this player, stop it first.
        cancelSnowmanTask(uid);

        if (!this.snowmanPetPlayers.containsKey(player)) {
            return; // nothing to follow right now
        }

        final Snowman snowman = snowmanPetPlayers.get(player);
        final int taskId = Bukkit.getScheduler().runTaskTimer(main, () -> {
            // End conditions: no player, no snowman, wrong world, or pet turned off
            if (!player.isOnline()
                    || !snowman.isValid()
                    || player.getWorld() != snowman.getWorld()
                    || !snowmanPetPlayers.containsKey(player)
                    || player.getWorld() != main.getLobbyWorld()) {

                // remove pet entity if still around
                try { snowman.remove(); } catch (Throwable ignored) {}
                cancelSnowmanTask(uid); // <- kill the ticker
                return;
            }

            // Follow logic
            Location playerLoc = player.getLocation();
            double distance = playerLoc.distance(snowman.getLocation());

            if (distance > 15) {
                Location behind = playerLoc.clone().add(playerLoc.getDirection().multiply(-2));
                behind.setY(Math.min(playerLoc.getWorld().getHighestBlockYAt(behind), playerLoc.getY() + 10));
                snowman.teleport(behind);
            }
        }, 20L, 20L).getTaskId();

        snowmanTasks.put(uid, taskId);
    }

    // Angle used to rotate the swirl; we store it as a field so it persists across
	// movements
	private double angle = 0;

	public void candyCaneSwirlCosmetic(Player player) {
		if (this.candyCaneSwirlPlayers.contains(player)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					// Check if player is still in the arraylist
					if (!candyCaneSwirlPlayers.contains(player)) {
						this.cancel();
						return;
					}

					if (player.getWorld() == main.getLobbyWorld()) {
						angle += Math.PI / 16; // adjust for speed of rotation

						// Set the radius of the swirl and the vertical height
						double radius = 1.0;
						double height = 1.0; // how high around the player the swirl appears

						// Calculate the positions for red and white particles in a circle
						double xRed = radius * Math.cos(angle);
						double zRed = radius * Math.sin(angle);

						double xWhite = radius * Math.cos(angle + Math.PI); // Opposite side for a striped effect
						double zWhite = radius * Math.sin(angle + Math.PI);

						// Get player location
						Location baseLoc = player.getLocation();

						// Red particle (REDSTONE)
						sendParticleToAll(EnumParticle.REDSTONE, baseLoc.getX() + xRed, baseLoc.getY() + height,
								baseLoc.getZ() + zRed, 0.1f, 0.1f, 0.1f, 0f, 5);

						// White particle (CLOUD)
						sendParticleToAll(EnumParticle.SNOW_SHOVEL, baseLoc.getX() + xWhite, baseLoc.getY() + height,
								baseLoc.getZ() + zWhite, 0.1f, 0.1f, 0.1f, 0f, 5);
					}
				}
			}.runTaskTimer(main, 0L, 1L); // Run every 20 ticks (1 second), adjust as needed
		}
	}

	// Sends the particle packet to all online players so everyone sees the swirl
	private void sendParticleToAll(EnumParticle particle, double x, double y, double z, float offsetX, float offsetY,
			float offsetZ, float speed, int count) {
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, false, // long distance
				(float) x, (float) y, (float) z, offsetX, offsetY, offsetZ, speed, count);

		for (Player online : Bukkit.getOnlinePlayers()) {
			((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			Player player = event.getPlayer();
			Player target = (Player) event.getRightClicked();

			if (player != null && target != null) {
				GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
				GameInstance spectating = main.getGameManager().GetInstanceOfSpectator(player);

				if ((game != null && game.state == GameState.STARTED) || spectating != null || main.getParkour().hasPlayer(player)) {
					return;
				}

				new StatsGUI(main, target).inv.open(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		// anthony.CrystalWars.game.GameInstance i =
		// main.getCwManager().getInstanceOfPlayer(player);
		Block b = event.getBlock();

//		if (i != null) {
//			if (i.getState() == GameState.IN_PROGRESS) {
//				if (i.blocksPlaced.contains(b.getLocation().toVector())) {
//					event.setCancelled(false);
//					return;
//				}
//				event.setCancelled(true);
//				player.sendMessage(main.color("&c&l(!) &rYou can only destroy blocks placed by players!"));
//			}
//		} else {
		if (player.isOp())
			event.setCancelled(false);
		else
			event.setCancelled(true);
//		}
//		i = null;
//		anthony.skywars.GameInstance i2 = main.getSWManager().getInstanceOfPlayer(player);
//
//		if (i2 != null) {
//			if (i2.getState() == anthony.skywars.GameState.STARTED) {
//				event.setCancelled(false);
//				return;
//			}
//		}
//		i2 = null;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		/*
		 * anthony.CrystalWars.game.GameInstance i =
		 * main.getCwManager().getInstanceOfPlayer(player);
		 * 
		 * if (i != null) { if (i.getState() == GameState.IN_PROGRESS) {
		 * event.setCancelled(false);
		 * i.blocksPlaced.add(event.getBlockPlaced().getLocation().toVector()); return;
		 * } } i = null;
		 * 
		 * anthony.skywars.GameInstance i2 =
		 * main.getSWManager().getInstanceOfPlayer(player);
		 * 
		 * if (i2 != null) { if (i2.getState() == anthony.skywars.GameState.STARTED) {
		 * event.setCancelled(false); return; } }
		 */

		if (!(player.isOp()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onFall(EntityDamageEvent e) {
		if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
			e.setCancelled(true);
	}

	@EventHandler
	public void armorStand(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player)
			if (e.getEntity() instanceof ArmorStand)
				if (((Player) e.getDamager()).getPlayer().getGameMode() != GameMode.CREATIVE)
					e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClick(InventoryClickEvent event) {
		if (event.getSlotType() == InventoryType.SlotType.ARMOR)
			event.setCancelled(true);
	}

	@EventHandler
	public void tokenClassGUI(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);
		if (e.getItem() != null && e.getItem().getType() == Material.ENCHANTED_BOOK) {
			e.setCancelled(true);
			if (i == null) {
				new ClassesGUI(main).inv.open(player);
			}
		}
	}

	@EventHandler
	public void cosmeticsGUI(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (e.getItem() != null && e.getItem().getType() == Material.CHEST) {
			if (i != null && i.state == GameState.WAITING)
				new CosmeticsGUI(main).inv.open(player);
			else if (player.getWorld() == main.getLobbyWorld())
				new CosmeticsGUI(main).inv.open(player);
		}
	}

	@EventHandler
	public void prefsGUI(PlayerInteractEvent e) {
		ItemStack item = e.getItem();
		Player player = e.getPlayer();

		if (item != null && item.getType() == Material.REDSTONE_COMPARATOR)
			new PrefsGUI(main).inv.open(player);
	}

	@EventHandler
	public void joinItem(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();

		if (item != null && item.getType() == Material.WATCH)
			new GameSelectorGUI(main).inv.open(player);
	}

	@EventHandler
	public void manipulate(PlayerArmorStandManipulateEvent e) {
		if (!e.getRightClicked().isVisible())
			e.setCancelled(true);
	}

	@EventHandler
	public void someGuis(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		ItemStack item = e.getItem();

		if (item != null) {
			if (item.getType() == Material.SKULL_ITEM) {
				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					if (item.getItemMeta().getDisplayName().contains("Profile"))
						new StatsGUI(main).inv.open(player);
					else if (item.getItemMeta().getDisplayName().contains("Tournament"))
						new TournamentGUI(main).inv.open(player);
				}
			} else if (item.getType() == Material.NETHER_STAR) {
				if (player.getWorld() == main.getLobbyWorld())
					new ChallengesGUI(main).inv.open(player);
			}
		}
	}

	@EventHandler
	public void containerInteract(PlayerInteractEvent e) {
		List<Material> list = new ArrayList<>(
				Arrays.asList(Material.FURNACE, Material.HOPPER, Material.ANVIL, Material.ENCHANTMENT_TABLE,
						Material.ANVIL, Material.WORKBENCH, Material.BREWING_STAND, Material.TRAPPED_CHEST,
						Material.ENDER_CHEST, Material.BEACON, Material.DISPENSER, Material.DROPPER, Material.CHEST));
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && list.contains(e.getClickedBlock().getType())) {
			Player player = e.getPlayer();
			if (!player.isOp())
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		// StaffChat
		event.setCancelled(true);
		if (main.staffchat.contains(event.getPlayer())) {
			String tag = main.getRankManager().getRank(event.getPlayer()).getTagWithSpace();
			String message = tag + event.getPlayer().getDisplayName() + ": " + event.getMessage();

			for (Player staff : main.staffchat) {
				TellAll(message);
				return;
			}
		} else {
			// Chat filter
			List<String> filteredWords = new ArrayList<>(Arrays.asList("nibba", "nigga", "niggas", "nigger", "niggers",
					"porn", "pornhub", "cum", "fuck you", "fuckyou", "fuck", "bitch", "pussy", "fucker", "motherfucker",
					"kys", "pu$$y", "fag", "faggot", "bitchass", "cunt", "retard", "penis", "fucker", "twat", "cock",
					"dick", "cumming", "fuckass", "vagina", "fuckers", "shit", "shitter", "shitters", "fucking"));
			PlayerData data = main.getDataManager().getPlayerData(event.getPlayer());
			String tag = main.getRankManager().getRank(event.getPlayer()).getTagWithSpace();
			String message = event.getMessage();

			event.setFormat(ChatColor.YELLOW + main.color("" + data.checkPlayerLevel(event.getPlayer(), data) + "✧")
					+ data.level + " " + tag);
			String displayName = main.getRankManager().getRank(event.getPlayer()).getColorForNames(event.getPlayer(),
					main.getRankManager().getRank(event.getPlayer()));

			if (!data.color.isEmpty() && !data.color.equals("0"))
				displayName = ChatColor.valueOf(data.color) + event.getPlayer().getDisplayName();

			if (event.getPlayer().hasPermission("scb.chat"))
				event.setFormat(main.color(event.getFormat() + displayName + ":&r "));
			else {
				event.setFormat(main.color(event.getFormat() + "&7" + displayName + ":&r "));
			}

			String tempmsg = "";
			for (String msgWord : message.split(" ")) { // Loop through each word and check if it is a banned word
				if (filteredWords.contains(msgWord.toLowerCase())) {
					tempmsg += StringUtils.repeat('*', msgWord.length()) + " ";
				} else
					tempmsg += msgWord + " ";
			}
			message = tempmsg.trim();

			if (event.getPlayer().hasPermission("scb.colorChat"))
				event.setMessage(main.color(message));
			else
				event.setMessage(message);

			Bukkit.broadcastMessage(event.getFormat() + event.getMessage());
		}
	}

	public void TellAll(String message) {
		for (Player staff : main.staffchat)
			staff.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "StaffChat> " + ChatColor.RESET + message);
	}

	// COSMETICS:

	@EventHandler
	public void cosmetics(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		PlayerData data = main.getDataManager().getPlayerData(player);
		ItemStack item = event.getItem();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (item != null) {
			if (item.getType() == Material.GOLD_BARDING
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				if ((player.getWorld() == main.getLobbyWorld())
						|| (i != null && i.state == GameState.WAITING)) {

					if (data != null) {
						if (data.paintball > 0) {
							Snowball snowball = player.launchProjectile(Snowball.class);
							snowball.setMetadata("paintball", new FixedMetadataValue(main, true));
							data.paintball--;
							main.getDataManager().saveData(data);

							String msg = main.color("&9&l(!) &rYou have &e" + data.paintball + " paintballs");
							PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
									(byte) 2);
							CraftPlayer craft = (CraftPlayer) player;
							craft.getHandle().playerConnection.sendPacket(packet);

							player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
						} else
							player.sendMessage(main.color("&c&l(!) &rYou do not have anymore &ePaintballs &r:("));
					}
				}
			}
		}
	}

	@EventHandler
	public void onHookHit(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof FishHook)
			event.setCancelled(true);
	}

	@EventHandler
	public void snowballHit(ProjectileHitEvent event) {
		Entity e = event.getEntity();
		Snowball s;

		if (e instanceof Snowball && e.hasMetadata("paintball")) {
			s = (Snowball) e;
			DyeColor col = DyeColor.values()[new Random().nextInt(DyeColor.values().length)];
			if (s.getShooter() instanceof Player) {
				Player p = (Player) s.getShooter();
				GameInstance i = main.getGameManager().GetInstanceOfPlayer(p);

				if (i != null && i.state == GameState.STARTED)
					return;

				Block center = s.getLocation().getBlock();
				int x = center.getX();
				int z = center.getZ();
				if (center.getType() != Material.AIR) {
					doTheWorkForMe(center, col);

				}

				int max = s.getLocation().getBlock().getY() + 1;
				int min = s.getLocation().getBlock().getY() - 1;
				Location loc;
				for (int y = min; y <= max; y++) {
					loc = new Location(center.getWorld(), x + 1, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x + 1, y, z + 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x, y, z + 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z + 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x - 1, y, z - 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x, y, z - 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
					loc = new Location(center.getWorld(), x + 1, y, z - 1);
					doTheWorkForMe(center.getWorld().getBlockAt(loc), col);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void randomizeColor(Block block, DyeColor color) {
		block.setData(color.getData());
	}

	@SuppressWarnings("deprecation")
	private void doTheWorkForMe(Block block, DyeColor color) {
		if (block.getType() != Material.AIR && block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST
				&& block.getType() != Material.WALL_SIGN && block.getType() != Material.WOOL
				&& block.getType() != Material.CHEST && block.getType() != Material.LONG_GRASS
				&& block.getType() != Material.RED_ROSE && block.getType() != Material.DEAD_BUSH
				&& block.getType() != Material.FLOWER_POT && block.getType() != Material.DOUBLE_PLANT
				&& block.getType() != Material.BED_BLOCK && !(block.getState().getData() instanceof Door)
				&& !(block.getState() instanceof InventoryHolder) && !(block.getState() instanceof Banner)
				&& block.getType() != Material.SKULL && block.getType() != Material.SOIL
				&& block.getType() != Material.SEA_LANTERN && block.getType() != Material.BEACON
				&& block.getType() != Material.GLOWSTONE && block.getType() != Material.LADDER) {
			Material og = block.getType();
			Byte data = block.getData();
			if (og == Material.WOOL) {
				randomizeColor(block, color);
				return;
			}
			Location loc = new Location(block.getWorld(), block.getX(), block.getY() + 1, block.getZ());

			if (loc.getBlock().getType().isSolid() == false && loc.getBlock().getType() != Material.AIR
					&& loc.getBlock().getType() != Material.TORCH)
				return;

			Bukkit.getScheduler().runTaskLater(main, () -> {
				block.setType(og);
				block.setData(data);
			}, 20 * 5L);
			block.setType(Material.WOOL);
			randomizeColor(block, color);
		}
	}

	public void trampoline(Player player) {
		Location loc = player.getLocation();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

		if (i != null || player.getWorld() != main.getLobbyWorld()) {
			player.sendMessage(main.color("&c&l(!) &rYou can only use this Cosmetic in spawn!"));
			return;
		}

		loc.setY(y - 1);
		loc = new Location(player.getWorld(), x + 1, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x + 1, y, z + 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x, y, z + 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z + 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x - 1, y, z - 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x, y, z - 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
		loc = new Location(player.getWorld(), x + 1, y, z - 1);
		doStuff(player.getWorld().getBlockAt(loc), player);
	}

	private void doStuff(Block block, Player player) {
		if (block.getType() != Material.AIR && block.getType() != Material.SIGN && block.getType() != Material.SIGN_POST
				&& block.getType() != Material.WALL_SIGN && block.getType() != Material.WOOL
				&& block.getType() != Material.CHEST && block.getType() != Material.LONG_GRASS
				&& block.getType() != Material.RED_ROSE && block.getType() != Material.DEAD_BUSH
				&& block.getType() != Material.FLOWER_POT) {
			Material og = block.getType();
			Byte data = block.getData();

			Bukkit.getScheduler().runTaskLater(main, () -> {
				block.setType(og);
				block.setData(data);
			}, 20 * 5L);
			block.setType(Material.SLIME_BLOCK);
		} else {
			player.sendMessage(main.color("&c&l(!) &rYou need to be in an open area to use this!"));
			return;
		}
	}
}
