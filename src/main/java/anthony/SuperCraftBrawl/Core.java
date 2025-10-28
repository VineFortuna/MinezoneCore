package anthony.SuperCraftBrawl;

import anthony.SuperCraftBrawl.Game.*;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.Cooldown;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.armorstands.ArmorStandManager;
import anthony.SuperCraftBrawl.commands.Commands;
import anthony.SuperCraftBrawl.doublejump.DoubleJumpManager;
import anthony.SuperCraftBrawl.fishing.FishArea;
import anthony.SuperCraftBrawl.fishing.Fishing;
import anthony.SuperCraftBrawl.floatingblock.FloatingBlockManager;
import anthony.SuperCraftBrawl.floatingblock.FloatingBlocks;
import anthony.SuperCraftBrawl.gui.*;
import anthony.SuperCraftBrawl.halloween.CandyAuraManager;
import anthony.SuperCraftBrawl.halloween.HalloweenHuntManager;
import anthony.SuperCraftBrawl.halloween.TreatsAdminCommand;
import anthony.SuperCraftBrawl.halloween.TrickTitleCommand;
import anthony.SuperCraftBrawl.halloween.TrickTitleManager;
import anthony.SuperCraftBrawl.halloween.TrickTitlePackets;
import anthony.SuperCraftBrawl.leaderboards.*;
import anthony.SuperCraftBrawl.lobbyexplorer.LobbyExplorerManager;
import anthony.SuperCraftBrawl.lobbyexplorer.LobbyExplorers;
import anthony.SuperCraftBrawl.npcs.NPC;
import anthony.SuperCraftBrawl.npcs.NPCManager;
import anthony.SuperCraftBrawl.npcs.VisibleHook;
import anthony.SuperCraftBrawl.packets.PacketMain;
import anthony.SuperCraftBrawl.playerdata.DatabaseManager;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.playerdata.PlayerDataManager;
import anthony.SuperCraftBrawl.practice.BowPractice;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.SuperCraftBrawl.ranks.RankManager;
import anthony.SuperCraftBrawl.signs.SignManager;
import anthony.SuperCraftBrawl.tablist.TablistAnimationManager;
import anthony.SuperCraftBrawl.tablist.TablistManager;
import anthony.SuperCraftBrawl.titles.TitleSequence;
import anthony.SuperCraftBrawl.titles.TitleUtil;
import anthony.parkour.Arenas;
import anthony.parkour.Parkour;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.event.player.PlayerJoinEvent;
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
    private TablistAnimationManager tablistAnim;

    //FLOATING BLOCK:
    private FloatingBlockManager floating;
    private FloatingBlocks floatingBlocks;

    //ARMOR STANDS
    public ArmorStandManager armorStandManager;

    //MYSTERY CHESTS:
    public Map<Player, EntityArmorStand> msHologram = new HashMap<Player, EntityArmorStand>();

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

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		if (e.getClickedBlock() == null)
			return;
		if (e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST
				|| e.getClickedBlock().getType() == Material.SIGN) {
			Sign s = (Sign) e.getClickedBlock().getState();
			for (Maps map : Maps.values()) {
				if (s.getLine(1).equalsIgnoreCase(map.toString())) {
					this.getGameManager().JoinMap(player, map);
					GameInstance i = null;

					if (getGameManager().gameMap.containsKey(map)) {
						i = getGameManager().gameMap.get(map);

						if (i != null) {
							if (i.state == GameState.WAITING) {
								s.setLine(2, this.color("&0Players: " + i.players.size() + "/"
										+ i.getMap().GetInstance().gameType.getMaxPlayers()));
								s.setLine(3, this.color("&0" + i.timeToStartSeconds + "s"));
								s.update();
							} else if (i.state == GameState.STARTED) {
								player.sendMessage(this.color(
										"&2&l(!) &rSince the game you tried joining has started, you've joined as a Spectator"));
								getGameManager().SpectatorJoinMap(player, map);
							}
						}
						i.setSign(s);
					}
				}
			}
		}
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
		spawnLobbyNPCs();
        spawnSelfStatsNPC();
        showNPCs();
        enableTitlesCosmetic();
        enableLeaderboardSnapshotTables();
        spawnFloatingBlocks();
        //enableTablist();
        //Spawn after world & chunks are ready. Delay 3 seconds
        Bukkit.getScheduler().runTaskLater(this, () -> getLbSettingsHologram().spawnLeaderboardSettingsHologram(), 60L);
    }

    /*
     * This function spawns the floating blocks in the lobby, for
     * Socials and Daily Rewards
     */
    private void spawnFloatingBlocks() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            World w = Bukkit.getWorld("lobby-1");

            // 1) DAILY REWARD at 186.5,116,633.5
            floatingBlocks.add(
                    new Location(w, 186.5, 116.0, 633.5),
                    new ItemStack(Material.WOOL, 1, (short)0), // white wool
                    "&6&lDAILY REWARD",
                    "&aRight Click",
                    (player) -> {
                        player.sendMessage(color("&aOpening Daily Reward..."));
                    }
            );

            // 2) SOCIALS at 192.843,116,632.825
            floatingBlocks.add(
                    new Location(w, 192.843, 116.0, 632.825),
                    new ItemStack(Material.WOOL, 1, (short)0),
                    "&6&lSOCIALS",
                    "&aRight Click",
                    (player) -> {
                        player.performCommand("socials");
                    }
            );

            floatingBlocks.spawnAll();
        }, 40L);
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
                                + "  \n&7/discord&f to join our Discord" + "\n\n&eminezone.club\n"));
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
        this.snapshotDAO = new anthony.SuperCraftBrawl.leaderboards.StatSnapshotDAO(this);
        try {
            // Wins
            snapshotDAO.ensureSnapshotsForAll("Wins",         anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,   "Wins");
            snapshotDAO.ensureSnapshotsForAll("Wins",         anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,  "Wins");
            snapshotDAO.ensureSnapshotsForAll("Wins",         anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY, "Wins");

            // Kills
            snapshotDAO.ensureSnapshotsForAll("Kills",        anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,   "Kills");
            snapshotDAO.ensureSnapshotsForAll("Kills",        anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,  "Kills");
            snapshotDAO.ensureSnapshotsForAll("Kills",        anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY, "Kills");

            // Flawless wins
            snapshotDAO.ensureSnapshotsForAll("FlawlessWins", anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,   "FlawlessWins");
            snapshotDAO.ensureSnapshotsForAll("FlawlessWins", anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,  "FlawlessWins");
            snapshotDAO.ensureSnapshotsForAll("FlawlessWins", anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY, "FlawlessWins");

            // Fishing total caught
            snapshotDAO.ensureSnapshotsForAll("TotalCaught",  anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.DAILY,   "TotalCaught");
            snapshotDAO.ensureSnapshotsForAll("TotalCaught",  anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.WEEKLY,  "TotalCaught");
            snapshotDAO.ensureSnapshotsForAll("TotalCaught",  anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.MONTHLY, "TotalCaught");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            // 1) Refresh caches (async)
            try { if (getKillsLeaderboard() != null) getKillsLeaderboard().asyncUpdate(); } catch (Throwable ignored) {}
            // try { if (winsBoard      != null) winsBoard.asyncUpdate(); }      catch (Throwable ignored) {}
            // try { if (flawlessBoard  != null) flawlessBoard.asyncUpdate(); }  catch (Throwable ignored) {}
            // try { if (fishingBoard   != null) fishingBoard.asyncUpdate(); }   catch (Throwable ignored) {}

            // 2) Paint on main thread
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    // Draw global Lifetime once (so Lifetime viewers see the up-to-date list)
                    if (getKillsLeaderboard() != null) getKillsLeaderboard().updateLeaderboard(false);
                    // if (winsBoard     != null) winsBoard.updateLeaderboard(false);
                    // if (flawlessBoard != null) flawlessBoard.updateLeaderboard(false);
                    // if (fishingBoard  != null) fishingBoard.updateLeaderboard(false);

                    // 3) Now enforce each viewer's chosen scope
                    for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                        anthony.SuperCraftBrawl.leaderboards.LeaderboardScope scope =
                                leaderboardScopeByViewer.getOrDefault(p.getUniqueId(),
                                        anthony.SuperCraftBrawl.leaderboards.LeaderboardScope.LIFETIME);

                        // Kills (what you're testing)
                        try {
                            anthony.SuperCraftBrawl.leaderboards.KillsBoard kb =
                                    (anthony.SuperCraftBrawl.leaderboards.KillsBoard) getKillsLeaderboard();
                            if (kb != null) kb.paintFor(p, scope);
                        } catch (Throwable ignored) {}

                        // When you convert the other boards, do the same:
                        // if (winsBoard != null)     winsBoard.paintFor(p, scope);
                        // if (flawlessBoard != null) flawlessBoard.paintFor(p, scope);
                        // if (fishingBoard != null)  fishingBoard.paintFor(p, scope);
                    }
                } catch (Throwable ignored) {}
            });
        }, 20L, 20L * 60L); // run every 60s
    }

    private void enableTitlesCosmetic() {
        trickTitleOld = new TrickTitleManager(this, "lobby-1");
        this.trickTitle = new TrickTitlePackets(this, "lobby-1"); // change world name if needed
        this.trickTitle.registerTitle("Trick-or-Treater", color("&6&lTrick-or-Treater"), 0.2);
        this.trickTitle.registerTitle("Freddy Fazbear", color("&6&lFreddy Fazbear"), 0.2);
        this.trickTitle.registerTitle("Fiesta De La Noche", color("&b&lFIESTA DE LA NOCHE"), 0.2);
        this.trickTitle.registerTitle("i'm gay btw...", color("&di'm gay btw..."), 0.2);
        getCommand("tricktitle").setExecutor(new TrickTitleCommand(trickTitle));
    }

    private void enableCommands() {
        String[] commandTypes = { "maps", "join", "cosmetics", "fishing", "server", "fly", "leave", "players",
                "class", "socials", "spectate", "startgame", "frenzy", "gamestats", "setlives", "purchases", "kit",
                "items", "color", "sound", "heal", "forceclass", "lactate" };

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
        plugin = this;
        msg = new ArrayList<>();
        listener = new PlayerListener(this);
        gameManager = new GameManager(this);
        scoreboardManager = new ScoreboardManager(this);
        titleAnimationManager = new anthony.SuperCraftBrawl.scoreboards.TitleAnimationManager(this);
        tabManager = new TablistManager(this);
        commands = new Commands(this);
        djManager = new DoubleJumpManager(this);
        databaseManager = new DatabaseManager(this);
        packetMain = new PacketMain(this);
        dataManager = new PlayerDataManager(this);
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
        lobbyScoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        explorerManager = new LobbyExplorerManager(this);
        npcManager = new NPCManager(this);
        getDatabaseManager().ensureSnapshotTable();
        tablistAnim = new TablistAnimationManager(this);
        tablistAnim.start();
        floating = new FloatingBlockManager(this);
        floatingBlocks = new FloatingBlocks(this);
        armorStandManager = new ArmorStandManager(this);
        lbSettingsHolo = new SettingsHologram(this);

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
        Location loc = new Location(w, 207.5, 105.0, 643.5, 0f, 1f); // where the NPC stands

        NPC selfNPC = new NPC(
                this,
                "",
                loc,
                null, null,
                (clicker) -> new StatsGUI(this).inv.open(clicker),
                null
        )
                .mimicViewerSkin()
                .disableHeadTracking()
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

	public String staffhelp = "";
	public String staffhelpReply = "";

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

			if (cmd.getName().equalsIgnoreCase("sh")) {
				if (args.length == 0)
					player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/sh <message>"));
				else {
					staffhelp = "";

					for (int i = 0; i < args.length; i++) {
						staffhelp += args[i] + " ";
					}
					player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp> " + ChatColor.RESET
							+ getRankManager().getRank(player).getTagWithSpace() + ChatColor.RESET + player.getName()
							+ ": " + ChatColor.LIGHT_PURPLE + staffhelp);
					player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "If any staff is online, you will recieve a reply shortly");

					for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
						if (onlinePlayers.hasPermission("scb.staffhelp")) {
							onlinePlayers.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp> "
									+ ChatColor.RESET + getRankManager().getRank(player).getTagWithSpace()
									+ ChatColor.RESET + player.getName() + ": " + ChatColor.LIGHT_PURPLE + staffhelp);
						}
					}
				}
			} else if (cmd.getName().equalsIgnoreCase("shr")) {
				if (player.hasPermission("scb.staffhelpreply")) {
					if (args.length == 0) {
						player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/shr <player> <message>"));
					} else if (args.length == 1) {
						player.sendMessage(color("&c&l(!) &rIncorrect usage! Try doing: &e/shr <player> <message>"));
					} else {
						Player target = Bukkit.getServer().getPlayerExact(args[0]);
						staffhelpReply = "";

						if (target != null) {
							for (int i = 1; i < args.length; i++) {
								staffhelpReply += args[i] + " ";
							}
							player.sendMessage(
									"" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp REPLY> " + ChatColor.RESET
											+ getRankManager().getRank(player).getTagWithSpace() + ChatColor.RESET
											+ player.getName() + ": " + ChatColor.LIGHT_PURPLE + staffhelpReply);
							target.sendMessage(
									"" + ChatColor.YELLOW + ChatColor.BOLD + "StaffHelp REPLY> " + ChatColor.RESET
											+ getRankManager().getRank(player).getTagWithSpace() + ChatColor.RESET
											+ player.getName() + ": " + ChatColor.LIGHT_PURPLE + staffhelpReply);
						} else {
							player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "Please specify a player!");
						}
					}
				} else {
					player.sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
							+ "You need the rank " + ChatColor.GOLD + ChatColor.BOLD + "TRAINEE " + ChatColor.RESET
							+ "to use this command");
				}
			}

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
				new ClassesGUI(this).inv.open(player);
//				sendClassesList(player);
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

						if (data.exp >= 2500) {
							data.level++;
							data.exp -= 2500;
							player.sendMessage(color("&8&m----------------------------------------"));
							player.sendMessage(color("&6&l✦✦ &e&lLEVEL UP! &6&l✦✦"));
							player.sendMessage(color("&7You are now &e&lLevel &6&l" + data.level + " &7— nice work!"));
							player.sendMessage(color("&8&m----------------------------------------"));

							// (optional but fun) little audio feedback on 1.8:
							player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.15f);

						}
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
				player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Want to help support the server? Purchase a rank at " + ChatColor.GREEN
						+ "https://minezone.club/");
			}

			if (cmd.getName().equalsIgnoreCase("token") && sender instanceof Player) {
				if (player.hasPermission("scb.giveTokens")) {
					if (args.length == 0) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Incorrect usage! Try doing: " + ChatColor.GREEN + "/token add <player> <amount>");
					} else if (args[0].equalsIgnoreCase("add")) {
						if (args.length == 1) {
							player.sendMessage(
									"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
											+ ChatColor.GREEN + "/token add <player> <amount>");
						} else if (args.length == 2) {
							player.sendMessage(
									"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Incorrect usage! Try doing: "
											+ ChatColor.GREEN + "/token add <player> <amount>");
						} else if (args.length == 3) {
							Player target = Bukkit.getServer().getPlayerExact(args[1]);
							try {
								int num = Integer.parseInt(args[2]);

								PlayerData data = this.getDataManager().getPlayerData(target);
								if (target != null) {
									data.tokens += num;

									player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You gave "
											+ ChatColor.GREEN + target.getName() + ChatColor.RESET + " " + num
											+ " Tokens!");
									target.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "You were given " + num + " Tokens!");
									if (this.getGameManager().GetInstanceOfPlayer(player) == null)
										getScoreboardManager().lobbyBoard(target);
									this.getDataManager().saveData(data);
								} else {
									player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "Please specify a player!");
								}
							} catch (Exception e) {
								player.sendMessage(
										"" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Please enter a number!");
							}
						}
					} else {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "Incorrect usage! Try doing: " + ChatColor.GREEN + "/token add <player> <amount>");
					}
				} else {
					player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
							+ ChatColor.RED + ChatColor.BOLD + "ADMIN " + ChatColor.RESET + "to use this command!");
				}
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
						} else
							player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "The specified target is not online!");
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

	private void sendClassesList(Player player) {
		String dClasses = "";
		String tClasses = "";
		String lClasses = "";
		String rClasses = "";
		for (ClassType type : ClassType.sortAlphabetically(ClassType.getAvailableClasses())) {
			if (type.getTokenCost() == 0 && type.getLevel() == 0 && type.getMinRank() != Rank.VIP)
				dClasses += type.getTag() + " ";
			else if (type.getTokenCost() > 0)
				tClasses += type.getTag() + " ";
			else if (type.getLevel() > 0)
				lClasses += type.getTag() + " ";
			else if (type.getMinRank() == Rank.VIP)
				rClasses += type.getTag() + " ";
		}
		player.sendMessage(color("&f&l----------------------------------------"));
		player.sendMessage(color("&e&lFREE CLASSES:"));
		player.sendMessage(dClasses);
		player.sendMessage("");
		player.sendMessage(color("&e&lTOKEN CLASSES:"));
		player.sendMessage(tClasses);
		player.sendMessage("");
		player.sendMessage(color("&e&lLEVEL CLASSES:"));
		player.sendMessage(lClasses);
		player.sendMessage("");
		player.sendMessage(color("&e&lDONOR CLASSES:"));
		player.sendMessage(rClasses);
		player.sendMessage(color("&f&l----------------------------------------"));
	}

    public void sendScoreboardUpdate(Player trigger) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                boolean inGame =
                        getGameManager().GetInstanceOfPlayer(p) != null
                                || getGameManager().GetInstanceOfSpectator(p) != null;

                if (!inGame) {
                    getTabManager().setPlayerTeam(p);
                }
                // else: the game scoreboard owns nametag/class; do nothing.
            } catch (Throwable ignored) {}
        }
    }

    private void showNPCs(Player player) {
		for (NPC npc : npcs) {
	        npc.showTo(player);
	    }
	}

	public Map<Player, Holograms> holograms = new HashMap<Player, Holograms>();

	/**
	 * This function handles when a player joins the server
	 *
	 * @param e
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void joinEvent(PlayerJoinEvent e) {
		Player player = e.getPlayer(); // Gets the player that joined
		PlayerData data = this.getDataManager().getPlayerData(player); // Gets the player data from database
		String name = player.getName();

		getListener().resetDoubleJump(player);
		getListener().resetArmor(player);
		getListener().resetPotionEffects(player);
		getListener().checkIfTournament(player);
		getListener().setPlayerOnTablist(player);
		chatAnnouncementOnJoin(player);
		getScoreboardManager().lobbyBoard(player); // Gives the lobby scoreboard to player
		sendScoreboardUpdate(player); // This sets the rank next to player name above their head

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == null || !other.isOnline() || other == player) continue;
            sendScoreboardUpdate(other);
        }
		showNPCs(player);

		// For join message:
		Rank rank = getRankManager().getRank(player); // Gets the player's rank
		String tag = rank.getTagWithSpace(); // Gets the player's rank tag

		e.setJoinMessage(color("" + rank.getArrowColor() + "► " + tag
				+ getColorForNames(player, getRankManager().getRank(player)) + " &7has joined!"));

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
				this.getDataManager().saveData(data);
		}

        if (tablistAnim != null) tablistAnim.applyTo(e.getPlayer());

		player.setHealth(20);
		player.setFoodLevel(20);

        TitleSequence.sendChained(this, player,
                new TitleSequence.TitleSpec("&6&lMINEZONE", "&e&lNEW LOBBY", 10, 70, 0),
                new TitleSequence.TitleSpec("&6&lMINEZONE", "&c&lDaily/Monthly/Weekly &e&lLEADERBOARDS", 0, 70, 10)
        );
	}

	public String getColorForNames(Player player, Rank rank) {
		String msg = "";

		if (rank == Rank.OWNER || rank == Rank.ADMIN)
			msg = color("&c");
		else if (rank == Rank.PRO)
			msg = color("&9");
		else if (rank == Rank.VIP)
			msg = color("&e");

		return msg += player.getName();
	}

	@SuppressWarnings("deprecation")
	private void chatAnnouncementOnJoin(Player p) {
		p.sendMessage("----------------------------------------------");
		p.sendMessage("");
		p.sendMessage(color("          &6&lMINEZONE NETWORK"));
		p.sendMessage("");
		p.sendMessage("" + "         Enjoy Super Craft Bros!");
		p.sendMessage("");
		p.sendMessage("" + " Be sure to join our Discord Server with " + ChatColor.GREEN + "/socials");
		p.sendMessage("");
		p.sendMessage("----------------------------------------------");
		p.sendMessage("");
	}

	public void mysteryChestHologram(Player p) {
		PlayerData data = this.getDataManager().getPlayerData(p);

		if (data != null) {
			Location loc = new Location(this.getLobbyWorld(), 196.5, 105.5, 648.5);
            String name = color("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Mystery Chests");
            this.armorStandManager.addMysteryChestHologram(p, loc, name);

			loc = new Location(this.getLobbyWorld(), 196.5, 105.2, 648.5);
            name = color("&e&l" + data.mysteryChests + " &eto open!");
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
				"                     &eMinezone &7[1.8-1.21] \n      &c&lSUPER CRAFT BROS &7- &b&lLOBBY UPDATE!");
		p.setMotd(msg);
		p.setMaxPlayers(1);
	}

	@EventHandler
	public void leave(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Rank rank = getRankManager().getRank(player); // Gets the player's rank
		String tag = rank.getTagWithSpace(); // Gets the player's rank tag

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
		return new Location(lobbyWorld, 189.495, 115, 629.438, -0, 1);
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
                if (getKillsLeaderboard() != null) getKillsLeaderboard().paintFor(player, scope);
            } catch (Throwable ignored) {}
			getScoreboardManager().lobbyBoard(player);
			sendScoreboardUpdate(player);

			if (!(holograms.containsKey(player)))
				holograms.put(player, new Holograms(this, player)); // All players' holograms
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

		ItemStack fishingRod = ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD), "&3&lGo Fishing!",
				"&fAnywhere with water", "&fFish for junk, fish and treasure", "&fEarn unique rewards");
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
            if (this.getKillsLeaderboard() != null) {
                // paintFor() hides the global lifetime for scoped views
                this.getKillsLeaderboard().paintFor(p, scope);
            }
        } catch (Throwable ignored) {}

    try { if (this.getLeaderboard() != null)     this.getLeaderboard().paintFor(p, scope); } catch (Throwable ignored) {}
    /*try { if (this.flawlessBoard != null) this.flawlessBoard.paintFor(p, scope); } catch (Throwable ignored) {}
    try { if (this.getFishingLeaderboard() != null) this.getFishingLeaderboard().paintFor(p, scope); } catch (Throwable ignored) {}
    */
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
