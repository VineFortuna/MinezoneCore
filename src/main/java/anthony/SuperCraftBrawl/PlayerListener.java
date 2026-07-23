package anthony.SuperCraftBrawl;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.fishing.FishArea;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.gui.christmas.ChristmasRewardsGUI;
import anthony.SuperCraftBrawl.gui.cosmetics.CosmeticsGUI;
import anthony.SuperCraftBrawl.leaderboards.LeaderboardScope;
import anthony.SuperCraftBrawl.npcs.ChannelInjector;
import anthony.SuperCraftBrawl.npcs.NPC;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.SuperCraftBrawl.titles.TitleSequence;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerListener implements Listener {

    private final Core main;
    public ScoreboardManager scoreManager = Bukkit.getScoreboardManager();
    public Scoreboard c;
    private BukkitTask announcementsTask;

    public PlayerListener(Core main) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, main);
        this.c = scoreManager.getNewScoreboard();
    }

    /*
     * This function shows the server messages that appear every 5 minutes to players
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

    /*
     * This function handles if a player has 2500 exp or more, it will
     * level them up
     */
    public void checkIfLevelUp(Player player) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        if (data != null) {
            if (data.exp >= 2500) {
                data.level++;
                data.exp -= 2500;
                player.sendMessage(main.color("&8&m----------------------------------------"));
                player.sendMessage(main.color("&6&l✦✦ &e&lLEVEL UP! &6&l✦✦"));
                player.sendMessage(main.color("&rYou are now &e&lLevel &6&l" + data.level + " &r- nice work!"));
                player.sendMessage(main.color("&8&m----------------------------------------"));
                player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.15f);

                if (player.getWorld() == main.getLobbyWorld())
                    main.getScoreboardManager().lobbyBoard(player); //Will update lobby scoreboard with new level
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
            p.setPlayerListName(s + " " + r.getColorForNames(p, r));
        } else
            p.setPlayerListName(rank + r.getColorForNames(p, r));

        if (main.getRankManager().getRank(p) == Rank.DEFAULT)
            p.setPlayerListName(rank + r.getColorForNames(p, r));

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

    public int getHalloweenEventProgress(Player player) {
        int progress = (main.getHalloweenManager() != null)
                ? main.getHalloweenManager().getFoundCount(player.getUniqueId())
                : 0;

        return progress;
    }

    // Clicking leaderboard settings in lobby
    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        ArmorStand stand = (ArmorStand) event.getRightClicked();
        Player player = event.getPlayer();

        String raw = stand.getCustomName();

        if (raw == null) {
            return;
        }

        if (main.getLbSettingsHologram().isSettingsHologram(event.getRightClicked().getUniqueId())) {
            SoundManager.playClickSound(player);
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
                data.points = main.tourney.getOrDefault(p.getName(), 0);
            }
            main.tourney.put(p.getName(), data.points);
        }
    }

    // EVENTS:

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Safety net: if this player is somehow still riding an EnderDragon from a previous session, eject and remove it
        if (player.isInsideVehicle()) {
            Entity vehicle = player.getVehicle();
            player.leaveVehicle();
            if (vehicle instanceof EnderDragon) vehicle.remove();
        }

        PlayerData data = main.getDataManager().getPlayerData(player); // Gets the player data from database
        String name = player.getName();
        Rank rank = main.getRankManager().getRank(player); // Gets the player's rank
        String tag = rank.getTagWithSpace(); // Gets the player's rank tag

        hitGlitchFix(player);
        giveLeaderboards(player);
        resetDoubleJump(player);
        resetArmor(player);
        resetPotionEffects(player);
        checkIfTournament(player);
        setPlayerOnTablist(player);
        chatAnnouncementOnJoin(player);
        main.getScoreboardManager().lobbyBoard(player); // Gives the lobby scoreboard to player
        main.sendScoreboardUpdate(player); // This sets the rank next to player name above their head

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == null || !other.isOnline() || other == player) continue;
            main.sendScoreboardUpdate(other);
        }
        main.showNPCs(player);

        event.setJoinMessage(main.color("" + rank.getArrowColor() + "► " + tag +
                main.getColorForNames(player, rank) + "&7 has joined!"));

        if (data != null) {
            player.setLevel(data.level); // Indication what the player's level is
            // Give Christmas rewards if not received
            boolean update = false;
            if (data.december18 == -1 && data.snowmanPet == 0) {
                data.snowmanPet = 1;
                update = true;
            }
            if (data.december19 == -1 && data.candycaneParticles == 0) {
                data.candycaneParticles = 1;
                update = true;
            }
            if (data.december23 == -1 && data.snowballDeathEffect == 0) {
                data.snowballDeathEffect = 1;
                update = true;
            }
            if (update)
                main.getDataManager().saveData(data);
        }

        if (main.tablistAnim != null) main.tablistAnim.applyTo(player);

        player.setHealth(20);
        player.setFoodLevel(20);

        //Send news to players with titles when they join
        TitleSequence.sendChained(main, player,
                new TitleSequence.TitleSpec("&6&lMINEZONE", "&c&lHUGE CLASSES REVAMP!", 10, 70, 0),
                new TitleSequence.TitleSpec("&6&lMINEZONE", "&e&lFRIENDS LIST! &e-> /friends", 0, 70, 10)
        );

        listFriendsOnline(player);
    }

    private void listFriendsOnline(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            int onlineFriends = main.getFriendsManager().getOnlineFriendsCount(player.getUniqueId());
            int incomingRequests = main.getFriendsManager().getPendingRequestCount(player.getUniqueId());
            List<UUID> friendUuids = main.getFriendsManager().getFriendUuids(player.getUniqueId());

            Bukkit.getScheduler().runTask(main, () -> {
                if (!player.isOnline()) {
                    return;
                }

                player.sendMessage(main.color("&rYou have &a" + onlineFriends + " &rfriends online"));

                if (incomingRequests > 0) {
                    player.sendMessage(main.color("&rYou have &a" + incomingRequests + " &rincoming friend requests"));
                }

                for (UUID friendUuid : friendUuids) {
                    Player friend = Bukkit.getPlayer(friendUuid);

                    if (friend != null && friend.isOnline() && !friend.getUniqueId().equals(player.getUniqueId())) {
                        friend.sendMessage(main.color("&rYour friend &a" + player.getName() + " &ris online!"));
                    }
                }
            });
        });
    }

    @SuppressWarnings("deprecation")
    public void chatAnnouncementOnJoin(Player p) {
        p.sendMessage("----------------------------------------------");
        p.sendMessage("");
        p.sendMessage(main.color("          &6&lMINEZONE NETWORK"));
        p.sendMessage("");
        p.sendMessage("" + "         Enjoy Super Craft Bros!");
        p.sendMessage("");
        p.sendMessage("" + " Be sure to join our Discord Server with " + ChatColor.GREEN + "/socials");
        p.sendMessage("");
        p.sendMessage("----------------------------------------------");
        p.sendMessage("");

        if (Bukkit.getOnlinePlayers().size() == 1) {
            Bukkit.getScheduler().runTaskLater(main, () -> {
                p.sendMessage("");

                BaseComponent[] tip = new ComponentBuilder("TIP ")
                        .color(net.md_5.bungee.api.ChatColor.YELLOW).bold(true) // &e&l
                        .append("No players online? Join our ")
                        .color(net.md_5.bungee.api.ChatColor.WHITE).bold(false)
                        .append("Discord")
                        .color(net.md_5.bungee.api.ChatColor.BLUE)     // &9
                        .underlined(true)                                   // &n
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/653vJzmrPz"))
                        .event(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Click here to join the Discord!")
                                        .color(ChatColor.BLUE) // &9
                                        .create()))
                        .append(" with 400+ members!")
                        .color(net.md_5.bungee.api.ChatColor.WHITE)
                        .underlined(false)
                        .create();

                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1, 1);
                p.spigot().sendMessage(tip);
                p.sendMessage("");
            }, 60L);
        }
    }

    /*
     * This function fixes the minecraft hit glitch by placing a sword
     * in a player's hand and removing it after a second
     */
    private void hitGlitchFix(Player player) {
        player.teleport(main.GetHubLoc());
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, new ItemStack(Material.WOOD_SWORD));
        }
        Bukkit.getScheduler().runTaskLater(main, () -> {
            player.getInventory().clear();
            PlayerData playerData = main.getDataManager().getPlayerData(player);

            if (player != null && playerData != null) {
                player.getInventory().clear();
                main.LobbyItems(player);
                player.setHealth(20.0f);
                player.setFireTicks(0);
                player.setLevel(playerData.level);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                main.mysteryChestHologram(player);
                main.parkourHolograms(player);
                main.updateLeaderboards();

                anthony.SuperCraftBrawl.leaderboards.LeaderboardScope scope =
                        main.leaderboardScopeByViewer.getOrDefault(player.getUniqueId(),
                                anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.LIFETIME);

                try {
                    main.repaintLeaderboardsFor(player, scope);
                } catch (Throwable ignored) {
                }

                main.getScoreboardManager().lobbyBoard(player);
                main.sendScoreboardUpdate(player);

                if (!(main.holograms.containsKey(player)))
                    main.holograms.put(player, new Holograms(main, player));
            }
        }, 20);
    }

    private void giveLeaderboards(Player p) {
        Bukkit.getScheduler().runTaskLater(main, () -> {
            // Default their personal view to Lifetime (only set once)
            main.leaderboardScopeByViewer.putIfAbsent(p.getUniqueId(), LeaderboardScope.LIFETIME);

            // Get their current totals from PlayerData
            PlayerData data = main.getDataManager().getPlayerData(p);
            if (data == null) return; // safety

            // If your fields are getters, swap to data.getWins() etc.
            int wins = data.wins;
            int kills = data.kills;
            int flawlessWins = data.flawlessWins;

            try {
                if (main.snapshotDAO == null) {
                    return;
                }

                String uuid = p.getUniqueId().toString();

                // Wins
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Wins", LeaderboardScope.DAILY, wins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Wins", LeaderboardScope.WEEKLY, wins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Wins", LeaderboardScope.MONTHLY, wins);

                // Kills
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Kills", LeaderboardScope.DAILY, kills);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Kills", LeaderboardScope.WEEKLY, kills);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "Kills", LeaderboardScope.MONTHLY, kills);

                // Flawless Wins
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "FlawlessWins", LeaderboardScope.DAILY, flawlessWins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "FlawlessWins", LeaderboardScope.WEEKLY, flawlessWins);
                main.snapshotDAO.ensureSnapshotForPlayer(uuid, "FlawlessWins", LeaderboardScope.MONTHLY, flawlessWins);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 40L); // ~2 seconds after join; adjust if your data load needs more/less time
    }

    @EventHandler
    public void OnPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);

        // --- EnderDragon win-effect fix ---
        // Must run BEFORE RemovePlayerFromAll since instance is still valid here.
        // 1. Eject the player from any vehicle so Minecraft doesn't restore the mount on reconnect.
        // 2. Remove the dragon from the world so it doesn't linger.
        if (player.isInsideVehicle()) {
            Entity vehicle = player.getVehicle();
            player.leaveVehicle();
            if (vehicle instanceof EnderDragon) {
                vehicle.remove();
            }
        }
        // Also clean up via the effects map in case the dragon is tracked there
        if (instance != null && instance.effects.containsKey(player)) {
            instance.effects.get(player).removeWinEffects();
            instance.effects.remove(player);
        }

        if (instance != null)
            main.getGameManager().RemovePlayerFromAll(player);

        main.getScoreboardManager().removeLobbyBoard(player);
        Player p = event.getPlayer();

        // Scoreboards
        main.getScoreboardManager().removeLobbyBoard(p);
        try { Holograms h = main.holograms.remove(p); if (h != null) h.destroyBoards(); } catch (Throwable ignored) {}

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
        main.sentMysteryHolos.remove(player.getUniqueId());
        main.sentParkourHolos.remove(player.getUniqueId());
        ChannelInjector.uninject(player);
        removeLeaderboards(event);
        main.getTitleAnimationManager().stop(player);
    }

    private void removeLeaderboards(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clearLeaderboardHolograms(player);
    }

    private void clearLeaderboardHolograms(Player player) {
        try {
            if (main.getKillsLeaderboard() != null) {
                main.getKillsLeaderboard().clearViewerHologram(player);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getLeaderboard() != null) {
                main.getLeaderboard().clearViewerHologram(player);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getFlawlessWinsBoard() != null) {
                main.getFlawlessWinsBoard().clearViewerHologram(player);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getWinstreakBoard() != null) {
                main.getWinstreakBoard().clearViewerHologram(player);
            }
        } catch (Throwable ignored) {
        }
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
            main.getFishing().cleanup(p); // safe no-op if nothing to do
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

        // 5) Hide or show NPCS
        for (NPC npc : main.getAllNPCs()) {
            // Leaving NPC world
            if (!p.getWorld().equals(npc.getLocation().getWorld())) {
                npc.hideFrom(p);
                continue;
            }
            // Returning to NPC world
            Bukkit.getScheduler().runTaskLater(main, () -> {
                npc.showTo(p);
            }, 5L);
        }

        clearLeaderboardHolograms(p);
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

    /*
     * This function handles the interactivty with the 'Active Games' item in
     * the lobby. It checks if the item is an eye of ender, then if the player
     * is in the lobby, then will open the GUI if all conditions met
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void activeGames(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

        if (event.getItem() != null && event.getItem().getType() == Material.EYE_OF_ENDER) {
            event.setCancelled(true);
            if (player.getWorld() == main.getLobbyWorld())
                new ActiveGamesGUI(main).inv.open(player);
        }

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

    //Disables interactivity with an end crystal
    @EventHandler
    public void endCrystal(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.ENDER_CRYSTAL) {
            e.setCancelled(true);
        }
    }

    //Disables interactivity with an end crystal
    @EventHandler
    public void endCrystal(EntityExplodeEvent e) {
        if (e.getEntity().getType() == EntityType.ENDER_CRYSTAL)
            e.setCancelled(true);
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

        if (main.getPartyManager() != null && main.getPartyManager().isPartyChatToggled(event.getPlayer())) {
            final Player chatPlayer = event.getPlayer();
            final String chatMessage = event.getMessage();

            Bukkit.getScheduler().runTask(main, new Runnable() {
                @Override
                public void run() {
                    main.getPartyManager().sendPartyMessage(chatPlayer, chatMessage);
                }
            });

            return;
        }

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
    public void onHookHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof FishHook)
            event.setCancelled(true);
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