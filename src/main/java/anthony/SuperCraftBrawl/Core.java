package anthony.SuperCraftBrawl;

import anthony.SuperCraftBrawl.Game.*;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.armorstands.ArmorStandManager;
import anthony.SuperCraftBrawl.commands.Commands;
import anthony.SuperCraftBrawl.cosmetics.CosmeticsManager;
import anthony.SuperCraftBrawl.doublejump.DoubleJumpManager;
import anthony.SuperCraftBrawl.fishing.FishArea;
import anthony.SuperCraftBrawl.fishing.Fishing;
import anthony.SuperCraftBrawl.floatingblock.FloatingBlockManager;
import anthony.SuperCraftBrawl.floatingblock.FloatingBlocks;
import anthony.SuperCraftBrawl.friends.FriendsManager;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.halloween.CandyAuraManager;
import anthony.SuperCraftBrawl.halloween.HalloweenHuntManager;
import anthony.SuperCraftBrawl.halloween.TreatsAdminCommand;
import anthony.SuperCraftBrawl.halloween.TrickTitleCommand;
import anthony.SuperCraftBrawl.halloween.TrickTitleManager;
import anthony.SuperCraftBrawl.halloween.TrickTitlePackets;
import anthony.SuperCraftBrawl.leaderboards.*;
import anthony.SuperCraftBrawl.levels.LevelManager;
import anthony.SuperCraftBrawl.lobbyexplorer.LobbyExplorerManager;
import anthony.SuperCraftBrawl.lobbyexplorer.LobbyExplorers;
import anthony.SuperCraftBrawl.mysterychest.MysteryChestManager;
import anthony.SuperCraftBrawl.npcs.NPC;
import anthony.SuperCraftBrawl.npcs.NPCManager;
import anthony.SuperCraftBrawl.npcs.VisibleHook;
import anthony.SuperCraftBrawl.packets.PacketMain;
import anthony.SuperCraftBrawl.party.PartyManager;
import anthony.SuperCraftBrawl.playerdata.DatabaseManager;
import anthony.SuperCraftBrawl.playerdata.GameDataManager;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.playerdata.PlayerDataManager;
import anthony.SuperCraftBrawl.practice.BowPractice;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.SuperCraftBrawl.ranks.RankManager;
import anthony.SuperCraftBrawl.signs.SignManager;
import anthony.SuperCraftBrawl.staffhelp.StaffHelpManager;
import anthony.SuperCraftBrawl.tablist.TablistAnimationManager;
import anthony.SuperCraftBrawl.tablist.TablistManager;
import anthony.SuperCraftBrawl.titles.TitleSequence;
import anthony.parkour.Arenas;
import anthony.parkour.Parkour;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Core extends JavaPlugin implements Listener {

    static Core plugin;

    private ActionBarManager actionBarManager;
    public GameManager gameManager;
    public MysteryChestManager mysteryChestManager;
    public ScoreboardManager scoreboardManager;
    public TablistManager tabManager;
    public Version version;
    public FreeClassesGUI inventoryGUI;
    public anthony.CrystalWars.game.GameManager gm;
    public DonorClassesGUI donorGUI;
    public GameSelectorGUI hubGUI;
    public Commands commands;
    public World lobbyWorld;
    public PlayerListener listener;
    public DoubleJumpManager djManager;
    protected final Cooldown cooldownTime = null;
    public RankManager rankManager;
    public List<Player> staffchat;
    public List<Player> globalchat;
    public PlayerDataManager dataManager;
    public GameDataManager gameDataManager;
    public DatabaseManager databaseManager;
    public PacketMain packetMain;
    public NPCManager npcManager;
    public ActiveGamesGUI ag;
    public boolean tournament = false;
    public boolean tourneyreset = false;
    public boolean tournamentend = false;
    public Map<String, Integer> tourney = new HashMap<>();
    public HashMap<Player, Boolean> ao = new HashMap<>();
    public HashMap<Player, Boolean> so = new HashMap<>();
    public HashMap<Player, Boolean> po = new HashMap<>();
    public Parkour p;
    public Leaderboard lb;
    public FishingBoard fb;
    public KillsBoard kb;
    public LevelBoard levelBoard;
    public BoardSettings boardSettings;
    public WinstreakBoard streakBoard;
    public FlawlessWinsBoard flawlessWinsBoard;
    public List<ParkourBoard> parkourBoards = new ArrayList<>();
    public Fishing fishing;
    private ArrayList<String> msg;
    public Map<Player, Player> wagers = new HashMap<Player, Player>();
    public anthony.SuperCraftBrawl.lobbyitems.LobbyItems lobbyItems;
    public CandyAuraManager candyAura;

    //GAME SIGNS MANAGER
    public SignManager signManager;

    // Player's game stats
    public Map<Player, GameInstance> gameStats = new HashMap<Player, GameInstance>();

    public boolean finalEvent = false;

    private long tickCounter = 0;

    // HALLOWEEN CLASSES:
    private HalloweenHuntManager halloweenHunt;
    private TrickTitleManager trickTitleOld;
    private TrickTitlePackets trickTitle;

    //PARKOUR VARIABLES:
    public final Set<UUID> sentMysteryHolos = new HashSet<>();
    public final Set<UUID> sentParkourHolos = new HashSet<>();

    //NPCS:
    public LobbyExplorerManager explorerManager;

    //LEADERBOARDS:
    public final java.util.Map<java.util.UUID, anthony.SuperCraftBrawl.leaderboards.LeaderboardScope>
            leaderboardScopeByViewer = new java.util.HashMap<>();
    public anthony.SuperCraftBrawl.leaderboards.StatSnapshotDAO snapshotDAO;
    public SettingsHologram lbSettingsHolo;

    //SCOREBOARDS:
    private anthony.SuperCraftBrawl.scoreboards.TitleAnimationManager titleAnimationManager;

    //TABLIST:
    public TablistAnimationManager tablistAnim;

    //FLOATING BLOCK:
    private FloatingBlockManager floating;
    private FloatingBlocks floatingBlocks;
    private FloatingBlocks.Entry dailyRewardEntry; // kept so claimDailyReward can push instant subtitle updates

    //ARMOR STANDS
    public ArmorStandManager armorStandManager;

    //MYSTERY CHESTS:
    public Map<Player, EntityArmorStand> msHologram = new HashMap<Player, EntityArmorStand>();

    //FRIENDS:
    public FriendsManager friendsManager;

    //PARTY:
    private PartyManager partyManager;

    //COSMETICS:
    public CosmeticsManager cosmeticsManager;

    //STAFF HELP:
    public StaffHelpManager  staffHelpManager;

    //LEVEL MANAGER:
    public LevelManager levelManager;

    public Core() {
        this.staffchat = new ArrayList<Player>();
        this.globalchat = new ArrayList<Player>();
    }

    public static Core inst() {
        return plugin;
    }

    public List<NPC> getAllNPCs() {
        return npcs;
    }

    // Getters:

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public CosmeticsManager getCosmeticsManager() {
        return cosmeticsManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public MysteryChestManager getMysteryChestManager() {
        return mysteryChestManager;
    }

    public FriendsManager getFriendsManager() {
        return friendsManager;
    }

    public StaffHelpManager getStaffHelpManager() {
        return staffHelpManager;
    }

    public ActionBarManager getActionBarManager() {
        return this.actionBarManager;
    }

    public SettingsHologram getLbSettingsHologram() {
        return this.lbSettingsHolo;
    }

    public ArmorStandManager getArmorStandManager() { return this.armorStandManager; }

    public anthony.SuperCraftBrawl.scoreboards.TitleAnimationManager getTitleAnimationManager() { return titleAnimationManager; }

    public CandyAuraManager getCandyAuraManager() {
        return this.candyAura;
    }

    public TrickTitleManager getTrickTitle() {
        return this.trickTitleOld;
    }

    public TrickTitlePackets getTrickPacket() {
        return this.trickTitle;
    }

    public HalloweenHuntManager getHalloweenManager() {
        return this.halloweenHunt;
    }

    public LevelBoard getLevelBoard() {
        return this.levelBoard;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public anthony.SuperCraftBrawl.lobbyitems.LobbyItems getLobbyItems() {
        return this.lobbyItems;
    }

    public TablistManager getTabManager() {
        return this.tabManager;
    }

    public SignManager getSignManager() {
        return this.signManager;
    }

    public long getCurrentTick() {
        return this.tickCounter;
    }

    public Parkour getParkour() {
        return this.p;
    }

    public anthony.CrystalWars.game.GameManager getCwManager() {
        return gm;
    }

    public FlawlessWinsBoard getFlawlessWinsBoard() {
        return this.flawlessWinsBoard;
    }

    public BoardSettings getBoardSettings() {
        return this.boardSettings;
    }

    public WinstreakBoard getWinstreakBoard() {
        return this.streakBoard;
    }

    public PlayerDataManager getDataManager() {
        return dataManager;
    }

    public GameDataManager getGameDataManager() {
        return gameDataManager;
    }

    public Fishing getFishing() {
        return fishing;
    }

    public Version getVersion() {
        return this.version;
    }

    public Leaderboard getLeaderboard() {
        return lb;
    }

    public FishingBoard getFishingLeaderboard() {
        return fb;
    }

    public KillsBoard getKillsLeaderboard() {
        return kb;
    }

    public List<ParkourBoard> getParkourLeaderboards() {
        return parkourBoards;
    }

    public String color(String c) {
        return ChatColor.translateAlternateColorCodes('&', c);
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }

    public ActiveGamesGUI getActiveGames() {
        return ag;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public Location getSCBLoc() {
        return new Location(lobbyWorld, -8.531, 161, -406.493);
    }

    public void SendPlayerToSCB(Player player) {
        player.teleport(getSCBLoc());
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public PacketMain getPacketMain() {
        return packetMain;
    }

    public GameSelectorGUI getHubGUI() {
        return hubGUI;
    }

    public FreeClassesGUI getInventoryGUI() {
        return inventoryGUI;
    }

    public DonorClassesGUI getDonorGUI() {
        return donorGUI;
    }

    public Cooldown getCooldown() {
        return cooldownTime;
    }

    public PlayerListener getListener() {
        return listener;
    }

    public Commands getCommands() {
        return commands;
    }

    public String format(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    // For tab organization.
    private Scoreboard lobbyScoreBoard;

    @Override
    public void onEnable() {
        initVariables(); //Initializes all class variables

        msg.add(color("&lReminder to thank the staff"));
        msg.add(color("&lThank you for playing, you're awesome"));
        msg.add(color("&lShare Minezone with your friends"));

        for (Player onlinePlayer : this.getServer().getOnlinePlayers())
            this.ResetPlayer(onlinePlayer);

        registrations();
        getListener().messages();
        enableCommands();
        enablePracticeModes();
        //spawnLobbyNPCs();
        spawnSelfStatsNPC();
        showNPCs();
        enableTitlesCosmetic();
        enableLeaderboardSnapshotTables();
        spawnFloatingBlocks();
        //enableTablist();
        //Spawn leaderboard settings holograms after world & chunks are ready. Delay 3 seconds
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLbSettingsHologram().spawnLeaderboardSettingsHologram(195.5, 105.2, 675.5); // main
            getLbSettingsHologram().spawnLeaderboardSettingsHologram(184.5, 106, 568.5); // parkour
            getLbSettingsHologram().spawnLeaderboardSettingsHologram(226.5, 105.2, 625.5); // fishing pond
        }, 60L);
    }

    /*
     * This function spawns the floating blocks in the lobby, for
     * Socials and Daily Rewards
     */
    private static final long DAILY_REWARD_COOLDOWN = 24 * 60 * 60 * 1000L; // 24 hours in ms

    /**
     * Called when a player clicks the Daily Reward floating block.
     * Checks the 24-hour cooldown and either gives the reward or tells
     * the player how long they have to wait.
     */
    public void claimDailyReward(org.bukkit.entity.Player player) {
        PlayerData data = getDataManager().getPlayerData(player);
        if (data == null) return;

        long now      = System.currentTimeMillis();
        long elapsed  = now - data.lastDailyReward;

        if (data.lastDailyReward > 0 && elapsed < DAILY_REWARD_COOLDOWN) {
            long remaining = DAILY_REWARD_COOLDOWN - elapsed;
            player.sendMessage(color("&c&l(!) &rYou already claimed your daily reward!"));
            player.sendMessage(color("&rNext reward available in: &a" + formatCooldown(remaining)));
            player.playSound(player.getLocation(), org.bukkit.Sound.NOTE_BASS, 1f, 0.5f);
            return;
        }

        // Give the reward here
        player.sendMessage(color("&8&m------------------------------"));
        player.sendMessage(color("&6&l★ DAILY REWARD CLAIMED! ★"));
        player.sendMessage(color(""));
        player.sendMessage(color("&a+20 Tokens"));
        player.sendMessage(color("&a+100 EXP"));
        player.sendMessage(color("&a+1 Mystery Chest"));
        player.sendMessage(color(""));
        player.sendMessage(color("&rCome back tomorrow for more!"));
        player.sendMessage(color("&8&m------------------------------"));
        player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1f, 1f);

        data.tokens += 20;
        data.exp += 100;
        data.mysteryChests++;
        getLevelManager().checkLevelUp(player);
        getScoreboardManager().lobbyBoard(player);

        data.lastDailyReward = now;
        getDataManager().saveData(data);

        // Push an immediate subtitle update so the player sees "Next Reward: ..."
        // right away without waiting for the next 10-second refresh cycle
        if (dailyRewardEntry != null) {
            floatingBlocks.sendSubtitlePacket(player, dailyRewardEntry);
        }
    }

    /** Formats a millisecond duration into "X hours and Y minutes" style text. */
    private String formatCooldown(long millis) {
        long totalSeconds = millis / 1000;

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        if (days > 0) {
            return days + "d " + hours + "h";
        }

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }

        if (minutes > 0) {
            return minutes + "m";
        }

        return "less than 1m";
    }

    private void spawnFloatingBlocks() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            World w = Bukkit.getWorld("lobby-1");

            if (w == null) {
                getLogger().warning("[FloatingBlocks] lobby-1 is null. Retrying in 40 ticks.");
                spawnFloatingBlocks();
                return;
            }

            Location dailyRewardLoc = new Location(w, 192.5, 108.2, 632.5);
            Location socialsLoc     = new Location(w, 186.5, 108.2, 626.5);
            Location storeLoc       = new Location(w, 192.5, 108.2, 626.5);
            Location classesLoc     = new Location(w, 186.5, 108.2, 632.5);

            /*
             * Fixes duplicate floating blocks after /save-all + restart.
             *
             * /save-all saves the armor stands to the world.
             * On restart, Bukkit metadata is gone, so old title/subtitle stands
             * like "Click to Claim!" and "Next Reward..." can stay behind.
             *
             * This removes old saved floating-block armor stands around these locations
             * before spawning the fresh plugin-controlled ones.
             */
            cleanupSavedFloatingBlockStands(w, dailyRewardLoc, socialsLoc, storeLoc, classesLoc);

            // 1) DAILY REWARD — subtitle is per-player via NMS metadata packets
            dailyRewardEntry = floatingBlocks.add(
                    dailyRewardLoc,
                    new ItemStack(Material.CHEST, 1, (short) 0),
                    "&e&lDAILY REWARD",
                    (player) -> {
                        anthony.SuperCraftBrawl.playerdata.PlayerData d = getDataManager().getPlayerData(player);
                        if (d == null) return "&aClick to Claim!";

                        long elapsed = System.currentTimeMillis() - d.lastDailyReward;

                        if (d.lastDailyReward > 0 && elapsed < DAILY_REWARD_COOLDOWN) {
                            return "&rNext Reward: &a" + formatCooldown(DAILY_REWARD_COOLDOWN - elapsed);
                        }

                        return "&aClick to Claim!";
                    },
                    (player) -> {
                        floatingBlocks.playDailyRewardClaimAnimation(dailyRewardEntry);
                        claimDailyReward(player);
                    }
            );

            // 2) SOCIALS
            floatingBlocks.add(
                    socialsLoc,
                    new ItemStack(Material.BOOKSHELF, 1, (short) 0),
                    "&6&lSOCIALS",
                    "&aRight Click",
                    (player) -> {
                        player.performCommand("socials");
                    }
            );

            // 3) STORE
            floatingBlocks.add(
                    storeLoc,
                    new ItemStack(Material.EMERALD_BLOCK, 1, (short) 0),
                    "&6&lSTORE",
                    "&aRight Click",
                    (player) -> {
                        player.performCommand("store");
                    }
            );

            floatingBlocks.add(
                    classesLoc,
                    new ItemStack(Material.ENCHANTMENT_TABLE, 1, (short) 0),
                    "&6&lSCB CLASSES",
                    "&aRight Click",
                    (player) -> {
                        player.performCommand("classes");
                    }
            );

            floatingBlocks.spawnAll();
        }, 40L);
    }

    private void cleanupSavedFloatingBlockStands(World world, Location... baseLocations) {
        if (world == null || baseLocations == null) return;

        for (ArmorStand stand : new ArrayList<ArmorStand>(world.getEntitiesByClass(ArmorStand.class))) {
            if (stand == null || stand.isDead()) continue;

            boolean nearFloatingBlock = false;

            for (Location baseLoc : baseLocations) {
                if (baseLoc == null) continue;

                if (isNearFloatingBlockLocation(stand.getLocation(), baseLoc)) {
                    nearFloatingBlock = true;
                    break;
                }
            }

            if (!nearFloatingBlock) continue;

            if (isOldFloatingBlockStand(stand)) {
                try {
                    stand.remove();
                } catch (Throwable ignored) {}
            }
        }
    }

    private boolean isNearFloatingBlockLocation(Location standLoc, Location baseLoc) {
        if (standLoc == null || baseLoc == null) return false;
        if (standLoc.getWorld() == null || baseLoc.getWorld() == null) return false;
        if (!standLoc.getWorld().equals(baseLoc.getWorld())) return false;

        Location titleLoc = baseLoc.clone().add(0, 2.55, 0);
        Location subLoc   = baseLoc.clone().add(0, 2.30, 0);

        double blockRadiusSq = 1.25 * 1.25;
        double textRadiusSq  = 1.25 * 1.25;

        return standLoc.distanceSquared(baseLoc) <= blockRadiusSq
                || standLoc.distanceSquared(titleLoc) <= textRadiusSq
                || standLoc.distanceSquared(subLoc) <= textRadiusSq;
    }

    private boolean isOldFloatingBlockStand(ArmorStand stand) {
        String name = stand.getCustomName();
        String strippedName = "";

        if (name != null) {
            strippedName = org.bukkit.ChatColor.stripColor(name).toLowerCase(java.util.Locale.US);
        }

        if (strippedName.startsWith("fb:")) return true;
        if (strippedName.contains("daily reward")) return true;
        if (strippedName.contains("click to claim")) return true;
        if (strippedName.contains("next reward")) return true;
        if (strippedName.contains("socials")) return true;
        if (strippedName.contains("store")) return true;
        if (strippedName.contains("right click")) return true;

        try {
            ItemStack helmet = stand.getEquipment() == null ? null : stand.getEquipment().getHelmet();
            return helmet != null && helmet.getType() != Material.AIR;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public void removeLeaderboardSettingsHologram() {
        org.bukkit.World w = getLobbyWorld();
        if (w == null) return;

        for (org.bukkit.entity.ArmorStand as : w.getEntitiesByClass(org.bukkit.entity.ArmorStand.class)) {
            String name = as.getCustomName();
            if (name == null) continue;

            // Strip colors and compare against the known lines we spawn
            String plain = org.bukkit.ChatColor.stripColor(name).trim().toLowerCase();
            if (plain.equals("leaderboard settings") || plain.equals("click to change settings")) {
                try { as.remove(); } catch (Throwable ignored) {}
            }
        }
    }

    private void enableTablist() {
        new BukkitRunnable() {
            @Override
            public void run() {
                PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
                Object header = new ChatComponentText(color("\n&6&lMINEZONE NETWORK\n"));
                Object footer = new ChatComponentText(
                        color("\n&7  /help&f for a list of commands" + "  \n&7/store&f to purchase a rank"
                                + "  \n&7/discord&f to join our Discord" + "\n\n&ewww.minezone.club\n"));
                try {
                    Field a = packet.getClass().getDeclaredField("a");
                    a.setAccessible(true);
                    Field b = packet.getClass().getDeclaredField("b");
                    b.setAccessible(true);

                    a.set(packet, header);
                    b.set(packet, footer);

                    if (Bukkit.getOnlinePlayers().size() == 0)
                        return;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(this, 0, 20);

        new BukkitRunnable() {

            @Override
            public void run() {
                tickCounter++;
            }
        }.runTaskTimer(this, 0, 1);
    }

    private void enableLeaderboardSnapshotTables() {
        getDatabaseManager().ensureSnapshotTable();
        getDatabaseManager().ensurePeriodWinstreakTable();
        getDatabaseManager().ensurePeriodParkourTable();

        this.snapshotDAO = new anthony.SuperCraftBrawl.leaderboards.StatSnapshotDAO(this);

        ensureAllLeaderboardSnapshots();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            ensureAllLeaderboardSnapshots();

            try {
                if (getLeaderboard() != null) {
                    getLeaderboard().asyncUpdate();
                }
            } catch (Throwable ignored) {
            }

            try {
                if (getKillsLeaderboard() != null) {
                    getKillsLeaderboard().asyncUpdate();
                }
            } catch (Throwable ignored) {
            }

            try {
                if (getFlawlessWinsBoard() != null) {
                    getFlawlessWinsBoard().asyncUpdate();
                }
            } catch (Throwable ignored) {
            }

            try {
                if (getWinstreakBoard() != null) {
                    getWinstreakBoard().asyncUpdate();
                }
            } catch (Throwable ignored) {
            }

            try {
                if (getFishingLeaderboard() != null) {
                    getFishingLeaderboard().asyncUpdate();
                }
            } catch (Throwable ignored) {
            }

            try {
                if (getParkourLeaderboards() != null) {
                    for (anthony.SuperCraftBrawl.leaderboards.ParkourBoard parkourBoard : getParkourLeaderboards()) {
                        if (parkourBoard != null) {
                            parkourBoard.asyncUpdate();
                        }
                    }
                }
            } catch (Throwable ignored) {
            }

            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    if (getLeaderboard() != null) {
                        getLeaderboard().updateLeaderboard(false);
                    }

                    if (getKillsLeaderboard() != null) {
                        getKillsLeaderboard().updateLeaderboard(false);
                    }

                    if (getFlawlessWinsBoard() != null) {
                        getFlawlessWinsBoard().updateLeaderboard(false);
                    }

                    if (getWinstreakBoard() != null) {
                        getWinstreakBoard().updateLeaderboard(false);
                    }

                    if (getFishingLeaderboard() != null) {
                        getFishingLeaderboard().updateLeaderboard(false);
                    }

                    if (getParkourLeaderboards() != null) {
                        for (anthony.SuperCraftBrawl.leaderboards.ParkourBoard parkourBoard : getParkourLeaderboards()) {
                            if (parkourBoard != null) {
                                parkourBoard.updateLeaderboard(false);
                            }
                        }
                    }

                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        anthony.SuperCraftBrawl.leaderboards.LeaderboardScope scope =
                                leaderboardScopeByViewer.getOrDefault(
                                        p.getUniqueId(),
                                        anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.LIFETIME
                                );

                        repaintLeaderboardsFor(p, scope);
                    }
                } catch (Throwable ignored) {
                }
            });
        }, 20L, 20L * 60L);
    }

    private void ensureAllLeaderboardSnapshots() {
        if (snapshotDAO == null) {
            return;
        }

        try {
            snapshotDAO.ensureSnapshotsForAll(
                    "Wins",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,
                    "Wins"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "Wins",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,
                    "Wins"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "Wins",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY,
                    "Wins"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "Kills",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,
                    "Kills"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "Kills",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,
                    "Kills"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "Kills",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY,
                    "Kills"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "FlawlessWins",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,
                    "FlawlessWins"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "FlawlessWins",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,
                    "FlawlessWins"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "FlawlessWins",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY,
                    "FlawlessWins"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "TotalCaught",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,
                    "TotalCaught"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "TotalCaught",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,
                    "TotalCaught"
            );

            snapshotDAO.ensureSnapshotsForAll(
                    "TotalCaught",
                    anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY,
                    "TotalCaught"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enableTitlesCosmetic() {
        trickTitleOld = new TrickTitleManager(this, "lobby-1");
        this.trickTitle = new TrickTitlePackets(this, "lobby-1"); // change world name if needed
        this.trickTitle.registerTitle("Trick-or-Treater", color("&6&lTrick-or-Treater"), 0.2);
        this.trickTitle.registerTitle("Freddy Fazbear", color("&6&lFreddy Fazbear"), 0.2);
        this.trickTitle.registerTitle("Fiesta De La Noche", color("&b&lFIESTA DE LA NOCHE"), 0.2);

        this.trickTitle.registerTitle("SCB Summer Champ 2021", color("&b&lSCB SUMMER CHAMP (2021)"), 0.2);
        this.trickTitle.registerTitle("SCB Summer Champ 2022", color("&b&lSCB SUMMER CHAMP (2022)"), 0.2);
        this.trickTitle.registerTitle("SCB Cash Cup Champ 2023", color("&a&lSCB CASH CUP CHAMP (2023)"), 0.2);
        this.trickTitle.registerTitle("SCB Halloween Champ 2024", color("&6&lSCB HALLOWEEN CHAMP (2024)"), 0.2);
        this.trickTitle.registerTitle("SCB Winter Champ 2025", color("&b&lSCB WINTER CHAMP (2025)"), 0.2);

        getCommand("tricktitle").setExecutor(new TrickTitleCommand(trickTitle));
    }

    /*
     * This function registers & enables all commands from Commands.java
     */
    private void enableCommands() {
        String[] commandTypes = { "maps", "join", /*"party", "partychat",*/ "friends", "token", "cosmetics", "fishing", "server", "fly", "leave", "players",
                "class", "socials", "spectate", "startgame", "frenzy", "gamestats", "setlives", "purchases", "kit",
                "items", "color", "sound", "soundnms", "heal", "forceclass", "lactate", "sh", "shr" };

        for (String command : commandTypes) {
            PluginCommand pluginCommand = this.getCommand(command);
            if (pluginCommand != null) {
                pluginCommand.setExecutor(commands);
                pluginCommand.setTabCompleter(commands);
            } else
                System.out.print(command + " was null!");
        }

        getCommand("treatsadmin").setExecutor(new TreatsAdminCommand(halloweenHunt));
    }

    private void registrations() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", gameManager);
    }

    private void initVariables() {
        saveDefaultConfig();
        plugin = this;
        msg = new ArrayList<>();
        listener = new PlayerListener(this);
        gameManager = new GameManager(this);
        mysteryChestManager = new MysteryChestManager(this);
        cosmeticsManager = new CosmeticsManager(this);
        scoreboardManager = new ScoreboardManager(this);
        titleAnimationManager = new anthony.SuperCraftBrawl.scoreboards.TitleAnimationManager(this);
        tabManager = new TablistManager(this);
        commands = new Commands(this);
        partyManager = new PartyManager(this);
        djManager = new DoubleJumpManager(this);
        databaseManager = new DatabaseManager(this);
        packetMain = new PacketMain(this);
        dataManager = new PlayerDataManager(this);
        gameDataManager = new GameDataManager(this);
        rankManager = new RankManager(this);
        actionBarManager = new ActionBarManager(this);
        ag = new ActiveGamesGUI(this);
        p = new Parkour(this);
        lb = new Leaderboard(this);
        kb = new KillsBoard(this);
        fb = new FishingBoard(this);
        levelBoard = new LevelBoard(this);
        boardSettings = new BoardSettings(this);
        streakBoard = new WinstreakBoard(this);
        flawlessWinsBoard = new FlawlessWinsBoard(this);
        fishing = new Fishing(this);
        signManager = new SignManager(this);
        lobbyItems = new anthony.SuperCraftBrawl.lobbyitems.LobbyItems(this);
        halloweenHunt = new HalloweenHuntManager(this);
        candyAura = new CandyAuraManager(this, "lobby-1");
        lobbyWorld = getServer().createWorld(new WorldCreator("lobby-1"));
        if (lobbyWorld != null) {
            lobbyWorld.setAutoSave(false);
        }
        lobbyScoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        explorerManager = new LobbyExplorerManager(this);
        npcManager = new NPCManager(this);
        getDatabaseManager().ensureSnapshotTable();
        getDatabaseManager().ensureGameTables();
        friendsManager = new FriendsManager(this);
        friendsManager.ensureTables();
        tablistAnim = new TablistAnimationManager(this);
        tablistAnim.start();
        floating = new FloatingBlockManager(this);
        floatingBlocks = new FloatingBlocks(this);
        armorStandManager = new ArmorStandManager(this);
        lbSettingsHolo = new SettingsHologram(this);
        staffHelpManager = new StaffHelpManager(this);
        levelManager = new LevelManager(this);

        for (Arenas arena : Arenas.values()) {
            parkourBoards.add(new ParkourBoard(this, arena));
        }

        getLogger().info("(!) You have enabled Minezone-Core");
    }

    private void showNPCs() {
        Bukkit.getPluginManager().registerEvents(new VisibleHook(() -> getAllNPCs()), this);

        // Ensure already-online players (e.g., on /reload) are injected and see NPCs
        for (Player p : Bukkit.getOnlinePlayers()) {
            anthony.SuperCraftBrawl.npcs.ChannelInjector.inject(p);
            for (NPC n : npcs) n.showTo(p);
        }
    }

    private void spawnSelfStatsNPC() {
        World w = lobbyWorld;
        Location loc = new Location(w, 180.5, 105, 650.5, 0f, 1f); // where the NPC stands

        NPC selfNPC = new NPC(
                this,
                color("&eClick to View"),
                loc,
                null, null,
                (clicker) -> new StatsGUI(this).inv.open(clicker),
                null
        )
                .mimicViewerSkin()
//                .disableHeadTracking()
                .enableHeadTracking()
                .perViewerLines(p -> Collections.emptyList());  // does nothing

        npcs.add(selfNPC);
        selfNPC.showToAll(); // spawn now for everyone online; VisibleHook will handle future joins
    }

    private final List<NPC> npcs = new ArrayList<>();

    private void spawnLobbyNPCs() {
        // Example location – replace with your actual world/coords
        org.bukkit.World w = this.lobbyWorld;
        Location loc = new Location(w, 164.347, 105, 657.741, -126, -0);

        // Skin (Base64 value + signature). Put yours here or read from config.
        String SKIN_VALUE = getConfig().getString("npc.amy.skin.value");
        String SKIN_SIG   = getConfig().getString("npc.amy.skin.signature");

        // Example 1: “Explorer Amy” uses the default explorer behavior (calls ExplorerManager)
        NPC amy = new NPC(
                this,
                "Amy",              // max 16 chars (class trims if longer)
                loc,
                SKIN_VALUE,
                SKIN_SIG,
                null,               // no custom onRightClick -> uses explorer fallback
                LobbyExplorers.Amy  // this triggers core.getExplorerManager().checkSelectedExplorer(...)
        ).setNameLines(
                "&d&lAMY",
                "&7Click to explore"
        );

        npcs.add(amy);
        amy.showToAll();

        Location dailyRewardNPC = loc.clone().add(3, 0, 0);
        NPC mailman = new NPC(
                this,
                color("&bThe Mailman"),
                dailyRewardNPC,
                null, null,
                (player) -> player.sendMessage(color("&a[Mailman] &fHello &e" + player.getName() +
                        "! &rI have some deliveries for you")),
                null
        ).setNameLines("&a&lDAILY REWARDS", "&eRight Click");

        npcs.add(mailman);
        mailman.showToAll();

    }


    public static BowPractice bowPractice;

    private void enablePracticeModes() {
        this.bowPractice = new BowPractice();
    }

    public Location GetLobbyLoc() {
        return new Location(lobbyWorld, -5.533, 143, 19.468);
    }

    public void SendPlayerToMap(Player player) {
        player.teleport(GetLobbyLoc());
    }

    public Location GetStaffLoc() {
        return new Location(lobbyWorld, 953.529, 177, 1036.495);
    }

    public void SendPlayerToStaff(Player player) {
        player.teleport(GetStaffLoc());
    }

    public Location GetHubLoc() {
        // return new Location(lobbyWorld, -199, 86, -7);
        // return new Location(lobbyWorld, -5.457, 143, 19.522);
        // return new Location(lobbyWorld, 288.507, 119, 2346.529);

        // return new Location(lobbyWorld, -58.507, 125, -18.519, -179, -1);
        // if (this.getCommands() != null || this.getSWCommands() != null)
        return new Location(lobbyWorld, 189.495, 115, 629.438, -0, 1);
        // else
        // return new Location(lobbyWorld, 0.478, 51, 0.550);
    }

    public void SendPlayerToHub(Player player) {
        player.teleport(GetHubLoc());
    }

    private ItemStack enchantments(ItemStack item, Enchantment ench, int level) {
        item.addUnsafeEnchantment(ench, level);
        return item;
    }

    private Material testMaterial(String st) {
        try {
            return Material.getMaterial(st.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private Enchantment testEnchant(String st) {
        try {
            return Enchantment.getByName(st.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings({ "null", "deprecation" })
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setrank")) {
            if (sender.hasPermission("scb.setrank")) {
                if (args.length > 1) {
                    Rank rank = Rank.getRankFromName(args[1]);
                    Player target = Bukkit.getServer().getPlayerExact(args[0]);

                    if (target != null) {
                        getRankManager().setRank(target, rank);
                        String temp = "" + getRankManager().getRank(target);
                        String temp2 = temp.toUpperCase();
                        sender.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + target.getName()
                                + "'s rank was set to " + ChatColor.YELLOW + temp2);
                        target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Your rank has been set to "
                                + ChatColor.YELLOW + temp2);
                    } else {
                        boolean success;
                        try {
                            success = dataManager.setOfflinePlayerRank(args[0], rank);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        if (success) {
                            String temp = rank.name();
                            String temp2 = temp.toUpperCase();
                            sender.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + args[0]
                                    + "'s rank was set to " + ChatColor.YELLOW + temp2);
                        } else {
                            sender.sendMessage(color("&c&l(!) &rFailed to update player rank."));
                        }
                    }
                } else {
                    sender.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
                            + ChatColor.GREEN + "/setrank <player> <rank>");
                }
            } else {
                sender.sendMessage(color("&c&l(!) &rYou do not have permission for that!"));
            }
        } else if (cmd.getName().equalsIgnoreCase("list")) {
            String players = "";
            int count = 0;
            int totalPlayers = Bukkit.getOnlinePlayers().size();
            sender.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "There are " + ChatColor.YELLOW
                    + totalPlayers + ChatColor.RESET + " players online:");

            for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                count++;
                players += "" + ChatColor.YELLOW + onlinePlayers.getName() + "";

                if (count < totalPlayers) {
                    players += "" + ChatColor.RESET + ", ";
                }
            }
            sender.sendMessage(players);
        } else if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("broadcast")) {
                if (player.hasPermission("scb.broadcast")) {
                    if (args.length == 0) {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "Incorrect usage! Try doing: " + ChatColor.GREEN + "/broadcast <message>");
                        return true;
                    } else {
                        String message = "";

                        for (int i = 0; i < args.length; i++) {
                            message += args[i] + " ";
                        }

                        for (Player allPlayers : Bukkit.getOnlinePlayers()) {
                            allPlayers.sendTitle(
                                    "" + ChatColor.GREEN + ChatColor.BOLD + ChatColor.UNDERLINE + "ANNOUNCEMENT",
                                    "" + ChatColor.RESET + message.trim() + " - " + ChatColor.YELLOW
                                            + player.getName().substring(0, 3));
                            allPlayers.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + message.trim() + " - " + ChatColor.YELLOW + player.getName());
                        }
                    }
                } else
                    player.sendMessage(color("&c&l(!) &rYou need the rank &c&lADMIN &rto use this command!"));
            }

            if (cmd.getName().equalsIgnoreCase("sc") && sender instanceof Player) {
                if (player.hasPermission("scb.staffchat")) {
                    if (!(staffchat.contains(player))) {
                        staffchat.add(player);
                        player.sendMessage(color("&e&l(!) &rYou have &eenabled &rStaffChat"));
                    } else {
                        staffchat.remove(player);
                        player.sendMessage(color("&e&l(!) &rYou have &cdisabled &rStaffChat"));
                    }
                } else
                    player.sendMessage(color("&c&l(!) &rYou need the rank &6&lTRAINEE &rto use this comamnd!"));
            }

            if (cmd.getName().equalsIgnoreCase("world")) {
                if (player.hasPermission("scb.tpWorld")) {
                    World oldLobby = getServer().createWorld(new WorldCreator("world"));
                    player.teleport(oldLobby.getSpawnLocation());
                }
            }

            if (cmd.getName().equalsIgnoreCase("hub")) {
                /*
                 * Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                 *
                 * ByteArrayOutputStream b = new ByteArrayOutputStream(); DataOutputStream out =
                 * new DataOutputStream(b);
                 *
                 * try { out.writeUTF("Connect"); out.writeUTF("lobby-1");
                 * player.sendMessage(color("&e&l(!) &rConnecting to &elobby-1")); } catch
                 * (Exception ex) { player.sendMessage(
                 * color("&c&l(!) &rThere was a problem connecting to &elobby-1")); }
                 * player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
                 */
                if (this.getGameManager().GetInstanceOfPlayer(player) != null
                        || this.getGameManager().GetInstanceOfSpectator(player) != null) {
                    this.getCommands().leaveGame(player);
                } else if (this.getParkour().hasPlayer(player)) {
                    this.getParkour().removePlayer(player);
                    this.ResetPlayer(player);
                } else {
                    this.ResetPlayer(player);
                }
                player.sendMessage(this.color("&r&l(!) &rSending you to the Hub"));
            }

            if (cmd.getName().equalsIgnoreCase("setlevel")) {
                if (player.hasPermission("scb.setlevel")) {
                    PlayerData data = getDataManager().getPlayerData(player);
                    if (args.length > 0) {
                        try {
                            int num = Integer.parseInt(args[0]);

                            if (num >= 0) {
                                if (data != null) {
                                    data.level = num;
                                    player.sendMessage(color("&2&l(!) &rYou set your level to &e" + num + "!"));
                                    player.setLevel(num);
                                    if (this.getGameManager().GetInstanceOfPlayer(player) == null)
                                        getScoreboardManager().lobbyBoard(player);
                                    this.getDataManager().saveData(data);
                                }
                            } else {
                                player.sendMessage(color("&c&l(!) &rPlease enter a number that is greater/equal to 0"));
                            }
                        } catch (Exception e) {
                            player.sendMessage(color("&c&l(!) &rPlease enter a valid number!"));
                            e.printStackTrace();
                        }
                    } else
                        player.sendMessage(color("&r&l(!) &rIncorrect usage! Try doing: &e/setlevel <level>"));
                } else
                    player.sendMessage(color("&c&l(!) &rYou need the rank &c&lADMIN &rto use this command!"));
            }

            if (cmd.getName().equalsIgnoreCase("give")) {
                if (player.hasPermission("scb.give")) {
                    if (args.length > 0 && args.length < 4) {
                        Player target = Bukkit.getServer().getPlayerExact(args[0]);
                        Material mat = testMaterial(args[1]);
                        int amount = Integer.parseInt(args[2]);
                        ItemStack item = null;
                        if (mat != null) {
                            item = new ItemStack(mat, amount);
                            target.getInventory().addItem(item);
                        } else {
                            player.sendMessage(color("&c&l(!) &rInvalid item!"));
                            return false;
                        }
                        if (target != player) {
                            target.sendMessage(color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()));
                        } else {
                            player.sendMessage(color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()));
                        }
                    } else if (args.length > 3 && args.length < 6) {
                        Player target = Bukkit.getServer().getPlayerExact(args[0]);
                        Material mat = testMaterial(args[1]);
                        int amount = Integer.parseInt(args[2]);
                        Enchantment ench = testEnchant(args[3]);
                        int level = Integer.parseInt(args[4]);
                        ItemStack item = null;

                        if (level > 0) {
                            if (mat != null) {
                                item = new ItemStack(mat, amount);
                                enchantments(item, ench, level);
                                target.getInventory().addItem(item);
                            } else {
                                player.sendMessage(color("&c&l(!) &rInvalid item!"));
                                return false;
                            }
                            if (target != player) {
                                target.sendMessage(
                                        color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()));
                            } else {
                                player.sendMessage(color("&e&l(!) &rYou were given &e " + amount + " " + item.getType()
                                        + " &rwith &e " + ench.getName() + " " + level));
                            }
                        } else {
                            player.sendMessage(color("&c&l(!) &rPlease enter an Enchantment level higher than 0!"));
                        }
                    } else {
                        player.sendMessage(color(
                                "&c&l(!) &rIncorrect usage! Try doing: &e/give <player> <item> <amount> <enchantment> <level>"));
                    }
                } else {
                    player.sendMessage(color("&c&l(!) &rYou need the rank &c&lADMIN &rto use this command!"));
                }
            }

            if (cmd.getName().equalsIgnoreCase("list")) {
                String players = "";
                int count = 0;
                int totalPlayers = Bukkit.getOnlinePlayers().size();
                player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "There are " + ChatColor.YELLOW
                        + totalPlayers + ChatColor.RESET + " players online:");

                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    count++;
                    players += "" + ChatColor.YELLOW + onlinePlayers.getName() + "";

                    if (count < totalPlayers) {
                        players += "" + ChatColor.RESET + ", ";
                    }
                }

                player.sendMessage(players);
            }

            if (cmd.getName().equalsIgnoreCase("online")) {
                int online = Bukkit.getOnlinePlayers().size();
                player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET + "There are "
                        + ChatColor.YELLOW + online + ChatColor.RESET + " players online");
            }

            if (cmd.getName().equalsIgnoreCase("vanish") && sender instanceof Player) {
                if (sender.hasPermission("scb.vanish")) {
                    player.sendMessage(
                            "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You are now in vanish");
                    player.setGameMode(GameMode.SPECTATOR);
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED
                            + "You need the rank " + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET
                            + ChatColor.RED + "to use this command");
                }
            }

            if (cmd.getName().equalsIgnoreCase("unvanish") && sender instanceof Player) {
                if (sender.hasPermission("scb.unvanish")) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GREEN + "You are now "
                            + ChatColor.RESET + ChatColor.RED + "unvanished");
                    player.setGameMode(GameMode.ADVENTURE);
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED
                            + "You need the rank " + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET
                            + ChatColor.RED + "to use this command");
                }
            }

            if (cmd.getName().equalsIgnoreCase("rules") && sender instanceof Player) {

                player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.WHITE
                        + "The rules can be found at, " + ChatColor.RESET + ChatColor.GREEN + "discord.gg/B9eHKg7");
            }

            if (cmd.getName().equalsIgnoreCase("staff") && sender instanceof Player) {
                if (sender.hasPermission("scb.staff")) {
                    GameInstance instance = this.getGameManager().GetInstanceOfPlayer(player);

                    if (instance != null) {
                        player.sendMessage(color("&r&l(!) &rYou cannot teleport to &eStaff &rwhile in a game!"));
                    } else {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Sending you to "
                                + ChatColor.GREEN + "Staff");
                        SendPlayerToStaff(player);
                    }
                } else {
                    player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED
                            + "You don't have permission to join the " + ChatColor.RESET + ChatColor.GREEN
                            + "Staff Server");
                }

            }
            if (cmd.getName().equalsIgnoreCase("gmc") && sender instanceof Player) {
                if (sender.hasPermission("scb.gmc")) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "Your gamemode has been updated to " + ChatColor.RESET + ChatColor.GREEN + "Creative!");
                    player.setGameMode(GameMode.CREATIVE);
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
                }
            }
            if (cmd.getName().equalsIgnoreCase("gms") && sender instanceof Player) {
                if (sender.hasPermission("scb.gms")) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "Your gamemode has been updated to " + ChatColor.RESET + ChatColor.GREEN + "Survival!");
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(true);
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
                }
            }
            if (cmd.getName().equalsIgnoreCase("gmsp") && sender instanceof Player) {
                if (sender.hasPermission("scb.gmsp")) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "Your gamemode has been updated to " + ChatColor.RESET + ChatColor.GREEN + "Spectator!");
                    player.setGameMode(GameMode.SPECTATOR);
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
                }
            }
            if (cmd.getName().equalsIgnoreCase("gma") && sender instanceof Player) {
                if (sender.hasPermission("scb.gma")) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "Your gamemode has been updated to " + ChatColor.RESET + ChatColor.GREEN + "Adventure!");
                    player.setGameMode(GameMode.ADVENTURE);
                    player.setAllowFlight(true);
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
                }
            }
            if (cmd.getName().equalsIgnoreCase("gm") && sender instanceof Player) {
                if (sender.hasPermission("scb.gm")) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.RED
                            + "Incorrect usage! Try doing: " + ChatColor.RESET + ChatColor.GREEN
                            + "/gms, /gmc, /gmsp or /gma");
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
                }
            }

//			if (cmd.getName().equalsIgnoreCase("help") && sender instanceof Player) {
//				player.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "(!) " + ChatColor.AQUA
//						+ "Need help? Go to our Discord Server for Help!");
//				player.sendMessage("- " + ChatColor.RED + ChatColor.BOLD + "Discord: " + ChatColor.GREEN
//						+ "discord.gg/FSZpmY9FZB");
//			}

            if (cmd.getName().equalsIgnoreCase("classes") && sender instanceof Player) {
                GameInstance game = getGameManager().GetInstanceOfPlayer(player);

                if (game != null && game.state == GameState.STARTED) {
                    player.sendMessage(color("&c&l(!) &rYou cannot select a class in game!"));
                    return false;
                }
                new ClassesGUI(this).inv.open(player);
            }
            if (cmd.getName().equalsIgnoreCase("scb") && sender instanceof Player) {
//				player.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "[SUPER CRAFT BLOCKS]");
//				player.sendMessage("" + ChatColor.GREEN + "Custom coded plugin by: VineFortuna & CowNecromancer");
//				player.sendMessage("" + ChatColor.GREEN + "Version: " + Version.SCB.getVersion());
//				player.sendMessage("" + ChatColor.GREEN + "Type " + ChatColor.WHITE + "/help " + ChatColor.GREEN
//						+ "for more information");
            }
            if (cmd.getName().equalsIgnoreCase("help") && sender instanceof Player) {
                player.sendMessage(color("&6&lSCB COMMANDS"));
                player.sendMessage(color("&e/join -> &rJoin a game"));
                player.sendMessage(color("&e/maps -> &rSee all playable maps"));
                player.sendMessage(color("&e/classes -> &rSee all playable classes"));
                player.sendMessage(color("&e/class -> &rChoose a class"));
                player.sendMessage(color("&e/spectate -> &rSpectate a game"));
                player.sendMessage(color("&e/leave -> &rLeave your game"));
                player.sendMessage("");
                player.sendMessage(color("&6&lFISHING COMMANDS"));
                player.sendMessage(color("&e/fishing -> &rOpens Fishing menu"));
                player.sendMessage("");
                player.sendMessage(color("&6&lSOCIAL COMMANDS"));
                //player.sendMessage(color("&e/party help -> &rShow list of party commands"));
                player.sendMessage(color("&e/friend help -> &rShow list of friends commands"));
                player.sendMessage(color("&e/staffhelp -> &rRequest help from a staff member"));
            }

            if (cmd.getName().equalsIgnoreCase("exp")) {
                if (player.hasPermission("scb.exp")) {
                    if (args.length == 0) {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "Incorrect usage! Try doing: " + ChatColor.GREEN + "/exp <amount>");
                    } else if (args.length == 1) {
                        int num = Integer.parseInt(args[0]);
                        PlayerData data = this.getDataManager().getPlayerData(player);
                        data.exp += num;
                        player.sendMessage(color("&6&l(!) &rAdded &e" + num + " EXP &rto your account"));
                        getLevelManager().checkLevelUp(player); //Check if player should level up
                        if (this.getGameManager().GetInstanceOfPlayer(player) == null)
                            getScoreboardManager().lobbyBoard(player);
                        this.getDataManager().saveData(data);
                    }
                }
            }

            if (cmd.getName().equalsIgnoreCase("unmute")) {
                if (player.hasPermission("scb.unmute")) {
                    if (args.length > 0) {
                        Player target = Bukkit.getPlayerExact(args[0]);

                        if (target != null) {
                            PlayerData data = this.getDataManager().getPlayerData(target);
                            data.muted = 0;
                            player.sendMessage(color("&r&l(!) &e" + target.getName() + " &rhas been unmuted"));
                        } else {
                            player.sendMessage(color("&c&l(!) &rPlease specify a player!"));
                        }
                    } else {
                        player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/unmute <player>"));
                    }
                } else {
                    player.sendMessage(color("&c&l(!) &rYou need the rank &6&lTRAINEE &rto use this command!"));
                }
            }

            if (cmd.getName().equalsIgnoreCase("invite")) {
                GameInstance instance = this.getGameManager().GetInstanceOfPlayer(player);

                if (player.hasPermission("scb.invite")) {
                    if (instance != null) {
                        if (instance.state == GameState.WAITING) {
                            String mapName = "";
                            if (instance.getMap() != null)
                                mapName = "" + instance.getMap();
                            else
                                mapName = "" + instance.duosMap;

                            Bukkit.broadcastMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + getRankManager().getRank(player).getTagWithSpace() + ChatColor.YELLOW
                                    + player.getName() + " " + ChatColor.RESET
                                    + "invited all players in the Lobby to join " + ChatColor.YELLOW + mapName);
                            TextComponent message = new TextComponent(
                                    "" + "     " + ChatColor.GREEN + ChatColor.BOLD + "Click here to join!");
                            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + mapName));
                            Bukkit.spigot().broadcast(message);
                        } else {
                            player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You must be in a Waiting Lobby to use this command");
                        }
                    } else {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "You need to be in a game to use this command");
                    }
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.BLUE + ChatColor.BOLD + "PRO " + ChatColor.RESET + "to use this command");
                }
            }

            if (cmd.getName().equalsIgnoreCase("fac")) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);

                try {
                    out.writeUTF("Connect");
                    out.writeUTF("factions");
                } catch (IOException ex) {

                }
                player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
            }

            if (cmd.getName().equalsIgnoreCase("store")) {
                player.sendMessage(color("&d&l(!) &rWant to help support the server? Purchase a rank" +
                        " at &ahttps://minezone.club/store"));
            }

            if (cmd.getName().equalsIgnoreCase("tp")) {
                if (player.hasPermission("scb.tp")) {
                    if (args.length == 0) {
                        player.sendMessage(color("&r&l(!) &rList of Teleport Commands:"));
                        player.sendMessage(color("&r- &e/tp <player>"));
                        player.sendMessage(color("&r- &e/tp <X> <Y> <Z>"));
                    } else if (args.length == 1) {
                        Player target = Bukkit.getServer().getPlayerExact(args[0]);

                        if (target != null) {
                            player.teleport(target.getLocation());
                            player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Teleporting to "
                                    + ChatColor.YELLOW + target.getName());
                        } else {
                            player.sendMessage(
                                    "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please enter a player!");
                        }
                    } else if (args.length == 2) {
                        player.sendMessage(color("&r&l(!) &rList of Teleport Commands:"));
                        player.sendMessage(color("&r- &e/tp <player>"));
                        player.sendMessage(color("&r- &e/tp <X> <Y> <Z>"));
                    } else if (args.length == 3) {
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        double z = Double.parseDouble(args[2]);

                        player.teleport(new Location(player.getWorld(), x, y, z));
                        player.sendMessage(color("&r&l(!) &rTeleporting to &e" + x + "&r, &e" + y + "&r, &e" + z));
                    }
                } else
                    player.sendMessage(color("&c&l(!) &rYou need the rank " + ChatColor.GOLD + ChatColor.BOLD
                            + "TRAINEE &rto use this command!"));
            }

            if (cmd.getName().equalsIgnoreCase("nick")) {
                GameInstance instance = this.getGameManager().GetInstanceOfPlayer(player);
                if (instance == null) {
                    if (player.hasPermission("scb.nickname.use")) {
                        if (args.length == 0 || args[0].equals(player.getName())) {
                            player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.YELLOW
                                    + "Your nickname has been reset!");
                            player.setDisplayName("" + player.getName());
                            return true;
                        }

                        String nick = "";
                        if (!args[0].matches("^[a-zA-Z0-9_]*$")) {
                            player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "Please enter a name with only alphanumeric characters!");
                            return true;
                        }
                        if (Bukkit.getPlayer(args[0]) != null) {
                            player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "You cannot name yourself as another player!");
                            return true;
                        }
                        if (args[0].length() <= 16) {
                            nick += args[0] + " ";
                        } else {
                            player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "Please enter a name up to 16 characters!");
                            return true;
                        }

                        nick = nick.substring(0, nick.length() - 1);

                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You changed your name to "
                                + ChatColor.YELLOW + nick);
                        player.setDisplayName("" + nick);
                    } else {
                        player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_RED + ChatColor.BOLD + "(!) "
                                + ChatColor.RESET + "You need a " + ChatColor.YELLOW + ChatColor.BOLD + "DONOR "
                                + ChatColor.RESET + "rank to access this command!");
                    }
                } else {
                    player.sendMessage(
                            "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You cannot use this while in a game");
                }
            }
            if (cmd.getName().equalsIgnoreCase("tell") || cmd.getName().equalsIgnoreCase("msg")) {
                if (args.length == 0) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
                            + ChatColor.GREEN + "/tell <player> <message>");
                    return true;
                }
                Player target = Bukkit.getServer().getPlayerExact(args[0]);
                PlayerData data = this.getDataManager().getPlayerData(target);

                if (target != null) {
                    if (data.pm == 0) {
                        String message = "";

                        for (int i = 1; i != args.length; i++) {
                            message += args[i] + " ";
                        }

                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GRAY + "You --> "
                                + target.getName() + ChatColor.RESET + ": " + ChatColor.RESET + message);
                        target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.GRAY
                                + player.getName() + " --> You" + ChatColor.RESET + ": " + ChatColor.RESET + message);
                    } else if (data.pm == 1) {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + ChatColor.YELLOW
                                + target.getName() + ChatColor.LIGHT_PURPLE + " has private messaging disabled!");
                    }

                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
                }
                return true;
            }

            if (cmd.getName().equalsIgnoreCase("activegames"))
                new ActiveGamesGUI(this).inv.open(player);

            if (cmd.getName().equalsIgnoreCase("tournament")) {
                if (player.hasPermission("scb.toggleTournament")) {
                    if (args.length != 1) {
                        player.sendMessage(
                                "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
                                        + ChatColor.GREEN + "/tournament <toggle/reset/clear/end>");
                    } else if (args[0].equalsIgnoreCase("toggle")) {
                        if (tournament) {
                            tournament = false;
                            player.sendMessage(color("&e&l(!) &eTournament mode disabled!"));
                            for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                                getScoreboardManager().lobbyBoard(onlinePlayers);
                                onlinePlayers.getInventory().setItem(2, null);
                            }
                        } else {
                            tournament = true;
                            player.sendMessage(color("&e&l(!) &eTournament mode now enabled!"));
                            for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                                PlayerData data = this.getDataManager().getPlayerData(onlinePlayers);
                                getScoreboardManager().lobbyBoard(onlinePlayers);
                                ItemStack tournament = ItemHelper.createSkullTexture(
                                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM0YTU5MmE3OTM5N2E4ZGYzOTk3YzQzMDkxNjk0ZmMyZmI3NmM4ODNhNzZjY2U4OWYwMjI3ZTVjOWYxZGZlIn19fQ==");
                                onlinePlayers.getInventory().setItem(2,
                                        ItemHelper.setDetails(tournament, "&7>&f>&6&lTournament&f<&7<"));
                                tourney.put(onlinePlayers.getName(), data.points);
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("reset")) {
                        player.sendMessage(color("&e&l(!) &eResetting points!"));
                        tourneyreset = true;
                        for (String s : tourney.keySet()) {
                            if (Bukkit.getOfflinePlayer(s).isOnline()) {
                                Player p = Bukkit.getPlayer(s);
                                PlayerData data = this.getDataManager().getPlayerData(p);
                                data.points = 0;
                                getScoreboardManager().lobbyBoard(p);
                                this.getDataManager().saveData(data);
                            }
                            tourney.put(s, 0);
                        }
                    } else if (args[0].equalsIgnoreCase("clear")) {
                        player.sendMessage(color("&e&l(!) &eRemoving all participants!"));
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            getScoreboardManager().lobbyBoard(p);
                            p.getInventory().setItem(2, null);
                        }
                        tourney.clear();
                    } else if (args[0].equalsIgnoreCase("end")) {
                        if (!tournament) {
                            player.sendMessage(
                                    "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Tournament mode is not enabled");
                            return false;
                        }
                        Bukkit.broadcastMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "Ending tournament");
                        // Hide tournament stats
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.getInventory().setItem(6, null);
                        }
                        // Shoot fireworks
                        Location newLoc = LobbyLoc();
                        BukkitRunnable runnable = new BukkitRunnable() {
                            int sec = 0;

                            @Override
                            public void run() {
                                if (sec == 4) {
                                    this.cancel();
                                } else {
                                    Firework fw = (Firework) newLoc.getWorld().spawnEntity(newLoc, EntityType.FIREWORK);
                                    FireworkMeta fwm = fw.getFireworkMeta();
                                    fwm.setPower(1);

                                    Color c = null;
                                    if (sec == 0)
                                        c = Color.BLUE;
                                    else if (sec == 1)
                                        c = Color.LIME;
                                    else if (sec == 2)
                                        c = Color.GREEN;
                                    else
                                        c = Color.YELLOW;
                                    fwm.addEffect(FireworkEffect.builder().withColor(c)
                                            .with(FireworkEffect.Type.BALL_LARGE).flicker(true).build());
                                    fw.setFireworkMeta(fwm);
                                }
                                sec++;
                            }

                        };
                        runnable.runTaskTimer(this, 0, 20);

                        sortTourney();
                        ArrayList<String> names = new ArrayList<>(tourney.keySet());

                        new BukkitRunnable() {
                            int size = Math.min(tourney.keySet().size(), 5);

                            @Override
                            public void run() {
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    if (size == 1) {
                                        p.sendTitle(color("&6And the winner is..."), "");
                                    } else {
                                        p.sendTitle(color("&aPlacing #" + size), "");
                                    }
                                    String name = names.get(size - 1);
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            p.sendTitle(color("&a" + name),
                                                    color("&a" + tourney.get(name) + " points"));
                                        }
                                    }.runTaskLater(plugin, 50);
                                }
                                size--;
                                if (size == 0)
                                    this.cancel();
                            }
                        }.runTaskTimer(plugin, 50, 150);

                        // Display scores
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                tournamentend = true;
                                String winnerName = names.get(0);
                                if (Bukkit.getOfflinePlayer(winnerName).isOnline()) {
                                    Player winner = Bukkit.getPlayer(winnerName);
                                    Firework fw = (Firework) winner.getWorld().spawnEntity(winner.getLocation(),
                                            EntityType.FIREWORK);
                                    FireworkMeta fwm = fw.getFireworkMeta();
                                    fwm.setPower(1);
                                    fwm.addEffect(FireworkEffect.builder().withColor(Color.ORANGE)
                                            .with(FireworkEffect.Type.STAR).flicker(true).build());
                                    fw.setFireworkMeta(fwm);
                                }
                                for (Player p : Bukkit.getOnlinePlayers()) {
                                    ItemStack tournament = ItemHelper.createSkullTexture(
                                            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM0YTU5MmE3OTM5N2E4ZGYzOTk3YzQzMDkxNjk0ZmMyZmI3NmM4ODNhNzZjY2U4OWYwMjI3ZTVjOWYxZGZlIn19fQ==");
                                    p.getInventory().setItem(6,
                                            ItemHelper.setDetails(tournament, "" + ChatColor.GRAY + "Tournament"));
                                    p.playSound(p.getLocation(), Sound.FIREWORK_TWINKLE2, 1, 0);
                                    p.sendMessage(color("&aTournament Scores:"));
                                    int count = 1;
                                    int placement = 0;
                                    for (String s : tourney.keySet()) {
                                        if (s.equals(p.getName())) {
                                            p.sendMessage(color("&a#" + count + " &e" + s + "&a - " + tourney.get(s)));
                                            placement = count;
                                        } else
                                            p.sendMessage(color("&a#" + count + " " + s + " - " + tourney.get(s)));
                                        count++;
                                    }
                                    p.sendMessage(color("&eYou placed #" + placement));
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            new TournamentGUI(plugin).inv.open(player);
                                        }
                                    }.runTaskLater(plugin, 100);
                                }
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.sendMessage(color("&e&l(!) &eTournament mode disabled!"));
                                        tournament = false;
                                        tourneyreset = false;
                                        tournamentend = false;
                                        for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                                            getScoreboardManager().lobbyBoard(onlinePlayers);
                                            onlinePlayers.getInventory().setItem(6, null);
                                        }
                                    }
                                }.runTaskLater(plugin, 600);
                            }
                        }.runTaskLater(plugin, 150 * Math.min(tourney.keySet().size(), 5) - 50);

                    } else {
                        player.sendMessage(
                                "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
                                        + ChatColor.GREEN + "/tournament <toggle/reset/clear/end>");
                    }
                } else {
                    player.sendMessage(color("&r&l(!) &rYou need the rank &c&lOWNER &rto use this command!"));
                }
            }

            if (cmd.getName().equalsIgnoreCase("points")) {
                if (player.hasPermission("scb.points")) {
                    if (args.length == 0) {
                        player.sendMessage(color("&r&l(!) &rIncorrect usage! Try doing: &e/points <player>"));
                    } else {
                        Player target = Bukkit.getServer().getPlayerExact(args[0]);
                        PlayerData data = this.getDataManager().getPlayerData(target);

                        if (target != null) {
                            if (data != null) {
                                player.sendMessage(
                                        color("&r&l(!) &e" + target.getName() + "'s points: " + data.points));
                            }
                        } else {
                            player.sendMessage(color("&r&l(!) &rPlease specify a player!"));
                        }
                    }
                } else {
                    player.sendMessage(color("&r&l(!) &rYou need the rank &c&lOWNER &rto use this command!"));
                }
            }

            if (cmd.getName().equalsIgnoreCase("setpoints") && sender instanceof Player) {
                if (player.hasPermission("scb.setpoints")) {
                    if (args.length < 2) {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "Incorrect usage! Try doing: " + ChatColor.GREEN + "/setpoints <player> <amount>");
                    } else {
                        Player target = Bukkit.getServer().getPlayerExact(args[0]);
                        try {
                            int num = Integer.parseInt(args[1]);

                            PlayerData data = this.getDataManager().getPlayerData(target);
                            if (target != null) {
                                data.points = num;

                                player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You set "
                                        + ChatColor.GREEN + target.getName() + ChatColor.RESET + "'s points to " + num);
                                target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "Your points were set to " + num);
                                if (tournament && this.getGameManager().GetInstanceOfPlayer(player) == null)
                                    getScoreboardManager().lobbyBoard(target);
                                this.getDataManager().saveData(data);
                            } else {
                                player.sendMessage(
                                        "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
                            }
                        } catch (Exception e) {
                            player.sendMessage(
                                    "" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please enter a number!");
                        }
                    }
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.RED + ChatColor.BOLD + "OWNER" + ChatColor.RESET + "to use this command!");
                }
            }

            if (cmd.getName().equalsIgnoreCase("stats")) {
                GameInstance i = this.getGameManager().GetInstanceOfPlayer(player);

                if (i != null && i.state == GameState.STARTED)
                    player.sendMessage(color("&c&l(!) &rYou cannot use this in a game!"));
                else {
                    if (args.length == 0 || args[0].equals(player.getName())) {
                        new StatsGUI(this).inv.open(player);
                    } else if (args.length == 1) {
                        Player target = Bukkit.getServer().getPlayerExact(args[0]);

                        if (target != null) {
                            new StatsGUI(this, target).inv.open(player);
                            player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                    + "Opening " + ChatColor.YELLOW + target.getName() + "'s" + ChatColor.RESET
                                    + " statistics");
                            return true;
                        }

                        final String targetName = args[0];

                        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                            try {
                                PlayerData offlineData = this.getDataManager().getSavedDataByName(targetName);

                                Bukkit.getScheduler().runTask(this, () -> {
                                    if (offlineData == null) {
                                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "That player has never joined the server!");
                                        return;
                                    }

                                    new StatsGUI(this, offlineData).inv.open(player);
                                    player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                            + "Opening " + ChatColor.YELLOW + offlineData.playerName + "'s" + ChatColor.RESET
                                            + " statistics");
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                                Bukkit.getScheduler().runTask(this, () -> {
                                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                            + "Could not load that player's stats.");
                                });
                            }
                        });
                    }
                }
            }
            if (cmd.getName().equalsIgnoreCase("seen")) {
                if (args.length == 0) {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
                            + ChatColor.GREEN + "/seen <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[0]);
                if (target != null && target.hasPlayedBefore()) {
                    long t = System.currentTimeMillis() - target.getLastPlayed();
                    long h = TimeUnit.MILLISECONDS.toHours(t);
                    long m = TimeUnit.MILLISECONDS.toMinutes(t);
                    long s = TimeUnit.MILLISECONDS.toSeconds(t);
                    if (!target.isOnline()) {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + target.getName()
                                + " was last online " + ChatColor.GREEN + h + " hours, " + (m - h * 60)
                                + " minutes, and " + (s - m * 60) + " seconds ago");
                    } else {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + target.getName()
                                + " was last online " + ChatColor.GREEN + "now");
                    }
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
                }
            }
            if (cmd.getName().equalsIgnoreCase("ignite")) {
                if (player.hasPermission("scb.ignite")) {
                    if (args.length == 0) {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                + "Incorrect usage! Try doing: " + ChatColor.GREEN + "/ignite <player>");
                        return true;
                    }
                    Player target = Bukkit.getServer().getPlayerExact(args[0]);

                    if (target != null) {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have ignited "
                                + ChatColor.YELLOW + target.getName());
                        target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You were ignited by "
                                + ChatColor.YELLOW + player.getName());
                        target.setFireTicks(1000);
                    } else {
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please specify a player!");
                        return false;
                    }
                } else {
                    player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
                            + ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to perform this command!");
                }
            }
        } else
            sender.sendMessage("Hey! You can't use this in the terminal!");

        return false;
    }

    public void restoreLobbyNameTag(Player player) {
        if (player == null) return;

        try {
            player.setDisplayName(player.getName());

            if (getTabManager() != null) {
                getTabManager().setPlayerTeam(player);
            }
        } catch (Throwable ignored) {}
    }

    public void sendScoreboardUpdate(Player trigger) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                boolean inGame =
                        getGameManager().GetInstanceOfPlayer(p) != null
                                || getGameManager().GetInstanceOfSpectator(p) != null;

                if (!inGame) {
                    restoreLobbyNameTag(p);
                }
                // else: the game scoreboard owns nametag/class; do nothing.
            } catch (Throwable ignored) {}
        }
    }

    public void showNPCs(Player player) {
        for (NPC npc : npcs) {
            npc.showTo(player);
        }
    }

    public Map<Player, Holograms> holograms = new HashMap<Player, Holograms>();

    public String getColorForNames(Player player, Rank rank) {
        String msg = "";

        if (rank == Rank.OWNER || rank == Rank.ADMIN)
            msg = color("&c");
        else if (rank == Rank.DEVELOPER)
            msg = color("&6");
        else if (rank == Rank.PRO)
            msg = color("&9");
        else if (rank == Rank.VIP)
            msg = color("&e");

        return msg += player.getName();
    }

    public void mysteryChestHologram(Player p) {
        PlayerData data = this.getDataManager().getPlayerData(p);

        if (data != null) {
            Location loc = new Location(this.getLobbyWorld(), 198.5, 105.5, 650.5);
            String name = color("&d&lMYSTERY CHEST");
            this.armorStandManager.addMysteryChestHologram(p, loc, name);

            loc = new Location(this.getLobbyWorld(), 198.5, 105.2, 650.5);
            name = color("&a" + data.mysteryChests + " &rto open!");
            this.armorStandManager.addMysteryChestHologram(p, loc, name);
        }
    }

    public void parkourHolograms(Player p) {
        for (Arenas arena : Arenas.values()) {
            Location loc = arena.getInstance().startLoc.clone().add(0.5, -0.75, 0.5);
            WorldServer s = ((CraftWorld) loc.getWorld()).getHandle();
            EntityArmorStand stand = new EntityArmorStand(s);

            stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            stand.setCustomName(color("&e&lParkour &b&lStart"));
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setInvisible(true);
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

            loc = arena.getInstance().startLoc.clone().add(0.5, -1.05, 0.5);
            stand = new EntityArmorStand(s);

            stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            stand.setCustomName(color("&r&l" + arena.getName()));
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setInvisible(true);
            packet = new PacketPlayOutSpawnEntityLiving(stand);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

            loc = arena.getInstance().endLoc.clone().add(0.5, -0.75, 0.5);
            stand = new EntityArmorStand(s);

            stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            stand.setCustomName(color("&e&lParkour &b&lEnd"));
            stand.setCustomNameVisible(true);
            stand.setInvisible(true);
            stand.setGravity(false);

            packet = new PacketPlayOutSpawnEntityLiving(stand);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

            loc = arena.getInstance().endLoc.clone().add(0.5, -1.05, 0.5);
            stand = new EntityArmorStand(s);

            stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            stand.setCustomName(color("&r&l" + arena.getName()));
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setInvisible(true);
            packet = new PacketPlayOutSpawnEntityLiving(stand);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @EventHandler
    public void serverMotd(ServerListPingEvent p) {
        String msg = color(
                "                     &eMinezone &7[1.8-26.1] \n        &c&lSUPER CRAFT BROS &7- &b&lLOBBY UPDATE!");
        p.setMotd(msg);
        p.setMaxPlayers(1);
    }

    @EventHandler
    public void leave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Rank rank = getRankManager().getRank(player); // Gets the player's rank
        String tag = rank.getTagWithSpace(); // Gets the player's rank tag

        if (getPartyManager() != null) {
            getPartyManager().handleQuit(player);
        }

        if (getParkour() != null && getParkour().hasPlayer(player)) {
            try { getParkour().removePlayer(player); } catch (Throwable ignored) {}
        }

        // this.packetMain.removePlayer(player);
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            try {
                out.writeUTF("PlayerCount");
                out.writeUTF("scb-1");
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
            b = new ByteArrayOutputStream();
            out = new DataOutputStream(b);

            try {
                out.writeUTF("PlayerCount");
                out.writeUTF("scb-2");
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
        }, 10L);

        getGameManager().removePlayerFromVotes(player);
        e.setQuitMessage(color("" + rank.getArrowColor() + "► " + tag
                + getColorForNames(player, getRankManager().getRank(player)) + " &7has left!"));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDescending(Map<K, V> map) {
        return map.entrySet().stream().sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public void sortTourney() {
        tourney = sortMapByValueDescending(tourney);
    }

    public Location LobbyLoc() {
        // return new Location(lobbyWorld, -199.517, 89.98466, -7.519);
        // return new Location(lobbyWorld, -5.457, 143, 19.522);
        // return new Location(lobbyWorld, 288.507, 119, 2346.529);

        // return new Location(lobbyWorld, -58.507, 125, -18.519, -179, -1);

        // if (this.getCommands() != null || this.getSWCommands() != null)
        //return new Location(lobbyWorld, 106.5, 112, -41.5, 180, 0);
        return GetHubLoc();
        // else
        // return new Location(lobbyWorld, 0.478, 51, 0.550);
    }

    public World getLobbyWorld() {
        return lobbyWorld;
    }

    /*
     * This function gives a player the main lobby
     * items when in lobby
     */
    public void LobbyItems(Player player) {
        getLobbyItems().mainLobbyItems(player);
    }

    public void ResetPlayer(Player player) {
        PlayerData playerData = this.getDataManager().getPlayerData(player);

        if (player != null && playerData != null) {
            player.getInventory().clear();
            player.teleport(LobbyLoc());
            LobbyItems(player);
            player.setHealth(20.0f);
            player.setFireTicks(0);
            player.setLevel(playerData.level);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            mysteryChestHologram(player);
            parkourHolograms(player);
            updateLeaderboards();

            anthony.SuperCraftBrawl.leaderboards.LeaderboardScope scope =
                    leaderboardScopeByViewer.getOrDefault(player.getUniqueId(),
                            anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.LIFETIME);

            try {
                repaintLeaderboardsFor(player, scope);
            } catch (Throwable ignored) {
            }

            getScoreboardManager().lobbyBoard(player);
            restoreLobbyNameTag(player);
            sendScoreboardUpdate(player);

            if (!(holograms.containsKey(player)))
                holograms.put(player, new Holograms(this, player));
        }
    }

    public Location GetSpawnLocation() {
        // return new Location(lobbyWorld, -199.517, 89.98466, -7.519);
        // return new Location(lobbyWorld, 288.507, 119, 2346.529);

        // return new Location(lobbyWorld, -58.507, 125, -18.519, -179, -1);
        return new Location(lobbyWorld, 189.495, 115, 629.438, -0, 1);
    }

    public String progressBar(int progress, int nextLevel, int segments) {
        String str = "";
        str += this.color("&8[");
        double frac = (double) progress / nextLevel;
        for (int i = 0; i < segments; i++) {
            if (i < Math.floor(frac * segments))
                str += this.color("&a|");
            else
                str += this.color("&7|");
        }
        str += this.color("&8] &7(" + progress + "/" + nextLevel + ")");
        return str;
    }

    public void SendToFactions(Player player) {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF("scb-2");
        } catch (IOException ex) {

        }
        player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
    }

    private void disablePacketNPCs() {
        try {
            for (anthony.SuperCraftBrawl.npcs.NPC n : npcs) {
                try { n.hideFromAll(); } catch (Throwable ignored) {}
            }
            npcs.clear();
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        disablePacketNPCs();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        for (GameInstance instance : gameManager.gameMap.values()) {
            for (Map.Entry<Player, WinEffects> entry : instance.effects.entrySet())
                entry.getValue().removeWinEffects();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = this.getDataManager().getPlayerData(player);
            String string;

            if (playerData.getRank() == Rank.DEFAULT) {
                string = "&lSupport us, buy a rank!";
            } else {
                string = msg.get(new Random().nextInt(msg.size()));
            }

            player.kickPlayer(color("&c&lSERVER IS RESTARTING\n &e\n" + string));
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(color("&4&l(!) &eServer Restarting..."));
        Bukkit.broadcastMessage("");

        // Saving data for players on server restart
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = this.getDataManager().getPlayerData(player);
            this.getDataManager().saveData(data);
        }

        closeLeaderboards();

        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin);
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        getLogger().info("(!) You have disabled Minezone-Core");
        for (World world : Bukkit.getWorlds()) {
            Bukkit.unloadWorld(world, false);
        }

        Bukkit.getScheduler().cancelTasks(this);
        titleAnimationManager.stopAll();

        if (tablistAnim != null) tablistAnim.stop();

        if (floating != null) floating.remove();

        if (floatingBlocks != null) floatingBlocks.removeAll();

        removeLeaderboardSettingsHologram();
        disableVariables();
        shutdownEverything();
    }

    private void disableVariables() {
        getScoreboardManager().removeAllBoards();
    }

    public ItemStack getFishingRod(Player player) {
        PlayerData data = getDataManager().getPlayerData(player);

        ItemStack fishingRod = ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD), "&3Go Fishing!",
                "&7Anywhere with water", "&7Fish for junk, fish and treasure", "&7Earn unique rewards");
        ItemHelper.setUnbreakable(fishingRod);
        if (data != null) {
            if (data.lure == 1 && data.lureLevel > 0) {
                ItemHelper.addEnchant(fishingRod, Enchantment.LURE, data.lureLevel);
            }
        }

        return fishingRod;
    }

    public String tokenCostString(Player player, int cost) {
        PlayerData data = this.getDataManager().getPlayerData(player);
        if (data != null) {
            if (data.tokens >= cost) {
                return this.color("&a" + cost + " Tokens");
            } else {
                return this.color("&c" + cost + " Tokens");
            }
        }
        return this.color("&cInvalid");
    }

    public FishArea getFishingArea(Location loc) {
        for (FishArea area : FishArea.values()) {
            if (area.isInBounds(loc)) {
                return area;
            }
        }
        return null;
    }

    public void closeLeaderboards() {
        if (lb != null) lb.close();
        if (fb != null) fb.close();
        if (kb != null) kb.close();
        if (streakBoard != null) streakBoard.close();
        if (flawlessWinsBoard != null) flawlessWinsBoard.close();
        if (levelBoard != null) {
            try { levelBoard.updateLeaderboard(true); } catch (Throwable ignored) {}
        }
        if (parkourBoards != null) {
            for (ParkourBoard pb : parkourBoards) {
                if (pb != null) pb.close();
            }
        }
    }

    public void updateLeaderboards() {
        getLeaderboard().updateLeaderboard(true);
        getFishingLeaderboard().updateLeaderboard(true);
        getKillsLeaderboard().updateLeaderboard(true);
        getWinstreakBoard().updateLeaderboard(true);
        getFlawlessWinsBoard().updateLeaderboard(true);
        getLevelBoard().updateLeaderboard(true);
        for (ParkourBoard parkourBoard : getParkourLeaderboards()) {
            parkourBoard.updateLeaderboard(true);
        }
    }

    public void repaintLeaderboardsFor(org.bukkit.entity.Player p,
                                       anthony.SuperCraftBrawl.leaderboards.LeaderboardScope scope) {
        try {
            if (this.getLeaderboard() != null) {
                this.getLeaderboard().paintFor(p, scope);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (this.getKillsLeaderboard() != null) {
                this.getKillsLeaderboard().paintFor(p, scope);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (this.getFlawlessWinsBoard() != null) {
                this.getFlawlessWinsBoard().paintFor(p, scope);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (this.getWinstreakBoard() != null) {
                this.getWinstreakBoard().paintFor(p, scope);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (this.getFishingLeaderboard() != null) {
                this.getFishingLeaderboard().paintFor(p, scope);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (this.getParkourLeaderboards() != null) {
                for (anthony.SuperCraftBrawl.leaderboards.ParkourBoard parkourBoard : this.getParkourLeaderboards()) {
                    if (parkourBoard != null) {
                        parkourBoard.paintFor(p, scope);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public LobbyExplorerManager getExplorerManager() {
        return this.explorerManager;
    }

    public void hologramCleanup(Player p) {
        Holograms h = holograms != null ? holograms.remove(p) : null;
        if (h != null) {
            try { h.destroyBoards(); } catch (Throwable ignored) {}
        }

        EntityArmorStand stand = msHologram != null ? msHologram.remove(p) : null;
        if (stand != null) {
            try {
                PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(stand.getId());
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroy);
            } catch (Throwable ignored) {}
        }
    }

    public void forgetPlayerEverywhere(Player p) {
        // Example toggles / wagers / stats maps keyed by Player:
        if (gameStats != null) gameStats.remove(p);
        if (ao != null) ao.remove(p); // ability toggles
        if (so != null) so.remove(p);
        if (po != null) po.remove(p);
        if (wagers != null) wagers.remove(p);
    }

    public void shutdownEverything() {
        try { getScoreboardManager().removeAllBoards(); } catch (Throwable ignored) {}

        if (getActionBarManager() != null) {
            try { getActionBarManager().shutdown(); } catch (Throwable ignored) {}
        }

        // Holograms/packet stands
        if (holograms != null) {
            for (Holograms h : holograms.values()) {
                try { h.destroyBoards(); } catch (Throwable ignored) {}
            }
            holograms.clear();
        }
        if (msHologram != null) {
            msHologram.clear(); // we only sent destroy packets per-player; nothing server-side to kill
        }

        // Fishing
        if (getFishing() != null) {
            try { getFishing().cleanupAll(); } catch (Throwable ignored) {}
        }

        if (getNPCManager() != null) {
            try { getNPCManager().shutdown(); } catch (Throwable ignored) {}
        }

        try { if (getParkour() != null) getParkour().cleanupAll(); } catch (Throwable ignored) {}

        getListener().cancelMessagesTask();
    }
}