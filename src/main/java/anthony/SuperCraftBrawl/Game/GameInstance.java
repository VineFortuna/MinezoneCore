package anthony.SuperCraftBrawl.Game;

import java.util.Optional;
import fr.minuskube.inv.SmartInvsPlugin;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryProvider;
import anthony.SuperCraftBrawl.gui.games.ItemListGUI;
import anthony.SuperCraftBrawl.gui.games.GameModifiersGUI;
import anthony.SuperCraftBrawl.gui.games.CoHostGUI;
import anthony.SuperCraftBrawl.gui.games.ClassListGUI;
import anthony.SuperCraftBrawl.gui.games.CustomGamesGUI;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.all.LargeFernClass;
import anthony.SuperCraftBrawl.Game.classes.all.ParrotClass;
import anthony.SuperCraftBrawl.Game.events.SantaFlyoverEvent;
import anthony.SuperCraftBrawl.Game.map.DuosMaps;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.PlayerListener;
import anthony.SuperCraftBrawl.Timer;
import anthony.SuperCraftBrawl.lobbyitems.LobbyItems;
import anthony.SuperCraftBrawl.party.Party;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.SuperCraftBrawl.signs.SignManager;
import anthony.SuperCraftBrawl.titles.TitleUtil;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.Map.Entry;
import java.util.UUID;

public class GameInstance {

    // Variables
    private final GameManager gameManager;
    private GameSettings gameSettings;
    public Objective livesObjective;
    public GameType gameType;
    private final Random random = new Random();
    private Maps map;
    public DuosMaps duosMap;
    private World mapWorld;
    public int gameTicks = -1;
    public GameState state;
    public List<Player> players;
    public List<Player> spectators;
    public Location recentDrop = null;
    public int lastSpawn = -1;

    // NOTE: Keeping these as Player-keyed for now to avoid huge cross-file refactor.
    // The big leak vectors (boards + task maps) are fixed to UUID below.
    public HashMap<Player, BaseClass> classes;
    public HashMap<Player, BaseClass> oldClasses;
    public HashMap<Player, BaseClass> allClasses; // Keep track of all players' BaseClass, even ones that left game before end

    public List<Player> playerPosition = new ArrayList<>();

    // Use UUID keys to avoid pinning Player (and the whole world graph)
    public Map<UUID, FastBoard> boards = new HashMap<>();

    public final HashMap<Player, ClassType> classSelection = new HashMap<>();
    public HashMap<Player, Timer> cooldowns = new HashMap<Player, Timer>();
    private final List<Player> winnerList;
    public BukkitRunnable gameStartTime;
    public int timeToStartSeconds = 30;
    List<BukkitRunnable> runnables = new ArrayList<>();
    public int blindness = 0;
    public ItemStack votePaper = ItemHelper.setDetails(new ItemStack(Material.PAPER),
            color("&eVote &7(Right Click)"));
    public int alivePlayers = 0;
    public int aliveTeams = 0;
    public Sign s;
    public HashMap<Player, String> team;
    public List<Player> redTeam;
    public List<Player> blueTeam;
    public List<Player> blackTeam;
    public int gameTime = 0;
    public Player firstBlood;
    private final Map<UUID, Location> lastKnownLocations = new HashMap<>();
    private int lightningDropCountdown = 0;
    private ItemStack nextItemToDrop;
    public List<ItemStack> allItemDrops = new ArrayList<>();
    public List<Player> favClassSelection = new ArrayList<>();
    // Classes each player has had this match, so Random Classes / Frenzy don't repeat one until they've had them all
    private final Map<UUID, Set<ClassType>> classHistory = new HashMap<>();
    public List<ClassType> classList = generateClassList();
    private SignManager sm;

    // Track all tasks to cancel reliably
    private final List<BukkitTask> instanceTasks = new ArrayList<>();
    // Per-player tasks keyed by UUID (not Player)
    private final Map<UUID, List<BukkitTask>> perPlayerTasks = new HashMap<>();
    // Repeating tasks started by class abilities (see trackClassTask)
    private final List<BukkitTask> classTasks = new ArrayList<>();
    // What /gamestats shows for this game - outlives the game without keeping it in memory
    private final MatchStats matchStats = new MatchStats(this);

    // DUEL COMMAND
    public boolean isDuel = false;

    //PARTY SYSTEM
    public static final int CUSTOM_GAME_MAX_PLAYERS = 100;
    private boolean customGame = false;
    private boolean cancellingCustomGame = false; // everyone's being sent home - nobody should be declared the winner
    private boolean customGameCountdownStarted = false;
    private UUID customGameHost;
    private boolean worldDiscarded = false;
    private boolean customGamePublic = false; // a custom game anyone can join, not just the leader's party
    private UUID customGameCoHost; // can change the Game Modifiers too (there's only ever one)
    private boolean classSelectorRemoved = false; // Frenzy: classes are random, so the selector goes once settings are final

    // Constructors:
    public GameInstance(GameManager gameManager, Maps map) {
        this(gameManager, map, null);
    }

    /** A game on an already loaded world (e.g. a custom game's own copy), or a new copy of the map if world is null. */
    public GameInstance(GameManager gameManager, Maps map, World world) {
        this.gameManager = gameManager;
        this.gameSettings = new GameSettings(this);
        this.map = map;
        this.state = GameState.WAITING; // Default game state
        this.gameType = map.GetInstance().gameType;
//		this.gameType = GameType.GUNGAME;
        this.players = new ArrayList<Player>();
        this.winnerList = new ArrayList<Player>();
        this.spectators = new ArrayList<Player>();
        this.firstBlood = null;
        classes = new HashMap<>();
        oldClasses = new HashMap<>();
        allClasses = new HashMap<>();
        if (world != null) mapWorld = world;
        else initialiseMap();
    }

    public GameInstance(GameManager gameManager, DuosMaps map) {
        this.gameManager = gameManager;
        this.duosMap = map;
        this.state = GameState.WAITING; // Default game state
        this.gameType = GameType.CLASSIC;
        this.players = new ArrayList<Player>();
        this.winnerList = new ArrayList<Player>();
        this.spectators = new ArrayList<Player>();
        this.firstBlood = null;
        this.sm = getGameManager().getMain().getSignManager();
        classes = new HashMap<>();
        oldClasses = new HashMap<>();
        allClasses = new HashMap<>();
        initializeTeams();
        initialiseMap();
    }

    // GETTER METHODS:

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public GameSettings getGameSettings() {
        return this.gameSettings;
    }

    public Maps getMap() {
        return this.map;
    }

    public DuosMaps getDuosMap() {
        return this.duosMap;
    }

    public World getMapWorld() {
        return this.mapWorld;
    }

    public void setSign(Sign s) {
        this.s = s;
    }

    /*
     * This function initializes the teams if game is Duos
     */
    private void initializeTeams() {
        team = new HashMap<Player, String>();
        redTeam = new ArrayList<Player>();
        blueTeam = new ArrayList<Player>();
        blackTeam = new ArrayList<Player>();
    }

    /*
     * This function loads this game's own copy of the map's world (so several games can be on the same map),
     * which is deleted again when the game is over. If the map doesn't exist in the files, it's a brand new void world
     */
    public void initialiseMap() {
        String worldName = map != null ? map.GetInstance().worldName : duosMap.GetInstance().worldName;
        mapWorld = gameManager.getMain().getMapWorldManager().createGameWorld(worldName);
    }

    /** Unloads and deletes this game's map copy (once everyone has left it). Safe to call more than once. */
    public void discardWorld() {
        if (worldDiscarded) return;
        worldDiscarded = true;
        cancelAllTasks(); // nothing of this game may run in the world once it's unloaded
        matchStats.freeze(); // /gamestats keeps working from a copy of the numbers, without this game
        gameManager.getMain().getMapWorldManager().discardGameWorld(mapWorld);
        mapWorld = null;
    }

    public boolean isCustomGame() {
        return customGame;
    }

    /** Private games don't count: no stats are saved and no tokens or EXP are given out. */
    public boolean savesStats() {
        return !customGame;
    }

    /** Classes players can choose (or be randomly given) in this game. */
    public ClassType[] getSelectableClasses() {
        if (!customGame) return ClassType.getAvailableClasses();

        List<ClassType> enabled = new ArrayList<>();
        for (ClassType type : getGameSettings().enabledClasses) {
            if (type.isPlayable()) enabled.add(type); // inactive holiday classes never count
        }
        return enabled.isEmpty() ? ClassType.getAvailableClasses() : enabled.toArray(new ClassType[0]);
    }

    /** Max health for each life in this game, from the Max Health modifier (20 = the normal 10 hearts). */
    public double getMaxHealthPoints() {
        // Only private games can change it - every other game is always the normal 10 hearts
        if (!customGame) return MaxHealthGuard.DEFAULT_MAX_HEALTH;
        return getGameSettings().maxHealthHearts * 2;
    }

    public boolean isClassSelectable(ClassType type) {
        return Arrays.asList(getSelectableClasses()).contains(type);
    }

    /**
     * Vaulted classes enabled in a private game are playable by everyone: their token, level and rank
     * requirements are skipped (but kept on the class for if it ever gets unvaulted).
     */
    public boolean ignoresClassRequirements(ClassType type) {
        return customGame && type.isVaulted() && isClassSelectable(type);
    }

    public UUID getCustomGameHost() {
        return customGameHost;
    }

    public boolean isCustomGameHost(Player player) {
        return player != null && customGameHost != null && customGameHost.equals(player.getUniqueId());
    }

    /** The host's name, which /joincustomgame takes. */
    public String getCustomGameHostName() {
        if (customGameHost == null) return "Unknown";
        String name = Bukkit.getOfflinePlayer(customGameHost).getName();
        return name != null ? name : "Unknown";
    }

    /*
     * The game lobby scoreboard's line while no countdown is running. In a custom game only the host waits for
     * players - everyone else (the co-host too) waits for the host to start it.
     */
    public String getWaitingLine(Player player) {
        return color(customGame && !isCustomGameHost(player) ? "&7&oWaiting for host.." : "&7&oWaiting for players..");
    }

    /** Puts the right waiting line back on the player's board (e.g. after they became host or co-host). */
    private void refreshWaitingLine(Player player) {
        if (player == null || state != GameState.WAITING || gameStartTime != null) return;
        FastBoard board = boards.get(player.getUniqueId());
        if (board != null) board.updateLine(7, getWaitingLine(player));
    }

    public UUID getCustomGameCoHost() {
        return customGameCoHost;
    }

    public boolean isCustomGameCoHost(Player player) {
        return player != null && customGameCoHost != null && customGameCoHost.equals(player.getUniqueId());
    }

    /** The host and the co-host can both change the Game Modifiers (only the host picks the co-host). */
    public boolean canEditCustomGame(Player player) {
        return isCustomGameHost(player) || isCustomGameCoHost(player);
    }

    /** Makes the player the co-host in place of the current one, or removes the co-host when null. */
    public void setCustomGameCoHost(Player coHost) {
        Player old = getOnlineCoHost();
        customGameCoHost = coHost == null ? null : coHost.getUniqueId();

        // The old co-host goes back to a normal player's items, the new one gets the host's
        if (old != null && old != coHost) {
            if (isCustomGameMenu(old)) old.closeInventory(); // they can't change the settings anymore
            if (state == GameState.WAITING) {
                old.getInventory().clear();
                gameManager.getMain().getLobbyItems().gameLobbyItems(old);
            }
        }
        if (coHost != null) giveEditorItems(coHost);
        refreshWaitingLine(old);
        refreshWaitingLine(coHost);
    }

    private Player getOnlineCoHost() {
        Player coHost = customGameCoHost == null ? null : Bukkit.getPlayer(customGameCoHost);
        return coHost != null && players.contains(coHost) ? coHost : null;
    }

    /** The host and co-host (whoever of them is in the game). */
    private List<Player> getEditors() {
        List<Player> editors = new ArrayList<>();
        for (Player p : players) {
            if (canEditCustomGame(p)) editors.add(p);
        }
        return editors;
    }

    /*
     * The host's and co-host's items: Game Modifiers / Start Game / Change Map (with Profile and Leave Game) in
     * the lobby, or the normal lobby items plus Game Settings (to stop it) once the countdown is running.
     */
    private void giveEditorItems(Player editor) {
        if (state != GameState.WAITING) return;
        if (customGameCountdownStarted) giveCountdownItems(editor);
        else giveCustomGameHostItems(editor);
    }

    public void giveCountdownItems(Player editor) {
        editor.getInventory().clear();
        gameManager.getMain().getLobbyItems().gameLobbyItems(editor);
        editor.getInventory().setItem(7, getGameSettingsItem());
    }

    /** Whether the player has one of the custom game settings menus open. */
    private static boolean isCustomGameMenu(Player player) {
        Optional<SmartInventory> open = SmartInvsPlugin.manager().getInventory(player);
        if (!open.isPresent()) return false;
        InventoryProvider provider = open.get().getProvider();
        return provider instanceof GameModifiersGUI || provider instanceof ClassListGUI
                || provider instanceof ItemListGUI || provider instanceof CoHostGUI;
    }

    /*
     * The host or co-host changed something: their partner's open custom game menus (Game Modifiers, Class List,
     * Item List, Co-Host) are reopened so they show it too.
     */
    public void refreshEditorMenus(Player changedBy) {
        CustomGamesGUI.refreshOpenMenus(); // its mode may have changed
        for (Player editor : getEditors()) {
            if (editor == changedBy) continue;

            if (!isCustomGameMenu(editor)) continue;
            Optional<SmartInventory> open = SmartInvsPlugin.manager().getInventory(editor);

            int page = SmartInvsPlugin.manager().getContents(editor)
                    .map(contents -> contents.pagination().getPage()).orElse(0);
            open.get().open(editor, page);
        }
    }

    public boolean isCustomGameCountdownStarted() {
        return customGameCountdownStarted;
    }

    public void enableCustomGame(Player leader) {
        if (leader == null) return;

        this.customGame = true;
        this.customGameHost = leader.getUniqueId();
        this.customGameCountdownStarted = false;
    }

    public boolean isCancellingCustomGame() {
        return cancellingCustomGame;
    }

    public boolean isCustomGamePublic() {
        return customGamePublic;
    }

    public void setCustomGamePublic(boolean customGamePublic) {
        this.customGamePublic = customGamePublic;
    }

    public boolean canAccessCustomGame(Player player) {
        if (!customGame || customGamePublic) return true;
        if (player == null) return false;

        if (isCustomGameHost(player)) return true;

        if (gameManager.getMain().getPartyManager() == null) {
            return false;
        }

        Player leader = Bukkit.getPlayer(customGameHost);

        if (leader == null) {
            return false;
        }

        Party party = gameManager.getMain().getPartyManager().getParty(leader);

        if (party == null) {
            return false;
        }

        return party.contains(player.getUniqueId());
    }

    /** Ends a private game with no winner: everyone in it (and watching it) goes back to the lobby. */
    public void cancelCustomGame(String reason) {
        cancelCustomGame(reason, null);
    }

    /** Same, but leaves out the player who is already leaving (they're removed by their own leave). */
    public void cancelCustomGame(String reason, Player leaving) {
        if (!customGame) return;
        cancellingCustomGame = true;

        if (gameStartTime != null) {
            gameStartTime.cancel();
            gameStartTime = null;
        }

        for (Player p : new ArrayList<>(players)) {
            if (p == leaving) continue;
            p.sendMessage(color(reason));
            gameManager.getMain().getCommands().removeFromGame(p);
        }
        for (Player p : new ArrayList<>(spectators)) {
            if (p == leaving) continue;
            p.sendMessage(color(reason));
            gameManager.getMain().getCommands().removeFromGame(p);
        }
    }

    /** Reopens the Game Modifiers menu - goes in the 8th hotbar slot once the leader has started the countdown. */
    public static ItemStack getGameSettingsItem() {
        return ItemHelper.setDetails(
                new ItemStack(Material.REDSTONE_COMPARATOR),
                "&eGame Settings &7(Right Click)",
                "",
                "&7Click to go back to the game settings."
        );
    }

    public void giveCustomGameHostItems(Player leader) {
        if (leader == null) return;
        if (!canEditCustomGame(leader)) return;
        if (state != GameState.WAITING) return;

        ItemStack modifiers = ItemHelper.setDetails(
                new ItemStack(Material.REDSTONE_COMPARATOR),
                color("&eGame Modifiers &7(Right Click)"),
                "",
                color("&7Click to modify this custom game.")
        );

        ItemStack startGame = ItemHelper.setDetails(
                new ItemStack(Material.EMERALD),
                color("&aStart Game &7(Right Click)"),
                "",
                color("&7Click to start the countdown.")
        );

        ItemStack changeMap = ItemHelper.setDetails(
                new ItemStack(Material.EMPTY_MAP),
                color("&eChange Map &7(Right Click)"),
                "",
                color("&7Click to move this game to another map.")
        );

        leader.getInventory().clear();
        leader.getInventory().setItem(0, modifiers);
        leader.getInventory().setItem(1, startGame);
        leader.getInventory().setItem(2, changeMap);
        leader.getInventory().setItem(4, LobbyItems.getProfileItem(leader));
        leader.getInventory().setItem(8, LobbyItems.getLeaveGameItem());
    }

    /** How players get their class: Classic, Frenzy, Class Switch or One For All. */
    public String getClassSelectionName() {
        GameSettings settings = getGameSettings();
        if (gameType == GameType.FRENZY) return "Frenzy";
        if (settings.randomClasses) return "Class Switch";
        if (settings.oneForAll) return "One For All";
        return "Classic";
    }

    /** Returns whether the countdown started (the host/co-host keep their items if it didn't). */
    public boolean startCustomGameCountdown(Player player) {
        if (!customGame) {
            player.sendMessage(color("&c&l(!) &rThis is not a custom game."));
            return false;
        }

        if (!canEditCustomGame(player)) {
            player.sendMessage(color("&c&l(!) &rOnly the custom game's host and co-host can start it."));
            return false;
        }

        if (players.size() < 2) {
            player.sendMessage(color("&c&l(!) &rYou need at least 2 players to start the game."));
            return false;
        }

        if (gameStartTime != null) {
            player.sendMessage(color("&c&l(!) &rThe game countdown has already started."));
            return false;
        }

        customGameCountdownStarted = true;
        removeClassSelectors(); // settings can't be changed anymore
        for (Player editor : getEditors()) giveCountdownItems(editor);

        TellAll(color("&e&l(!) &e" + player.getName() + " &rstarted the game"));
        announcePrivateGameSettings();
        StartGameTimer();
        return true;
    }

    /** Sends everyone in the private game a summary of its Game Modifiers. */
    private void announcePrivateGameSettings() {
        GameSettings settings = getGameSettings();

        String classSelection = getClassSelectionName();
        if (settings.randomClasses) classSelection += " (" + formatSeconds(settings.randomClassCycleSeconds) + ")";

        long time = mapWorld.getTime();
        String timeOfDay = time >= 13000 && time <= 23000 ? "Night" : time >= 11000 && time < 13000 ? "Sunset" : "Day";

        int enabledClasses = 0;
        for (ClassType type : settings.enabledClasses) {
            if (type.isPlayable()) enabledClasses++;
        }

        TellAll(color("&8&m------------------------------------"));
        TellAll(color("&6&lGAME SETTINGS"));
        TellAll(color("&fVisibility: " + (customGamePublic ? "&aPublic" : "&cPrivate")));
        TellAll(color("&fClass Selection: &e" + classSelection));
        TellAll(color("&fClass List: &e" + (enabledClasses == 0 ? "Default" : enabledClasses + "&7/&e"
                + ClassType.getPlayableClasses().length + " classes")));
        TellAll(color("&fLives: &e" + settings.startingLives));
        TellAll(color("&fRespawn Time: &e" + (settings.respawnSeconds == 0 ? "None" : settings.respawnSeconds + "s")));
        TellAll(color("&fSpawn Protection: &e" + formatSecondsOrDisabled(settings.spawnProtectionSeconds)));
        TellAll(color("&fMax Health: &e" + settings.maxHealthHearts + " Hearts"));
        TellAll(color("&fHealing on Kill: &e" + settings.healthPotMode.getName()));
        TellAll(color("&fStrength on Kill: &e" + formatSecondsOrDisabled(settings.strengthOnKillSeconds)));
        TellAll(color("&fLow Gravity: " + (settings.lowGravity ? "&aEnabled" : "&cDisabled")));
        TellAll(color("&fItem List: &e" + (settings.enabledDrops.isEmpty() ? "Default" : settings.enabledDrops.size()
                + "&7/&e" + GameLootDrops.values().length + " items")));
        TellAll(color("&fOP Item Drops: " + (settings.opDrops ? "&aEnabled" : "&cDisabled")));
        TellAll(color("&fLightning Drop Rate: &e" + settings.dropTimer + "s"));
        TellAll(color("&fTime of Day: &e" + timeOfDay));
        TellAll(color("&8&m------------------------------------"));
    }

    private static String formatSeconds(int seconds) {
        return seconds % 60 == 0 ? seconds / 60 + "m" : seconds + "s";
    }

    private static String formatSecondsOrDisabled(int seconds) {
        return seconds > 0 ? seconds + "s" : "&cDisabled";
    }

    /*
     * The leader clicked Game Settings during the countdown: stop it and hand back the
     * Game Modifiers / Start Game items so they can change settings and start again.
     */
    public void cancelCustomGameCountdown(Player leader) {
        if (!customGame || state != GameState.WAITING || !canEditCustomGame(leader)) return;

        // No countdown running: just give the items back
        if (!customGameCountdownStarted) {
            giveCustomGameHostItems(leader);
            return;
        }

        if (gameStartTime != null) {
            gameStartTime.cancel();
            gameStartTime = null;
        }
        customGameCountdownStarted = false;
        timeToStartSeconds = getSecondsUntilStart();
        removeVotePaper();
        restoreClassSelectors(); // Frenzy might get turned off

        for (Player p : players) {
            p.setLevel(0);
            TitleUtil.clear(p);
            FastBoard fb = boards.get(p.getUniqueId());
            if (fb != null) fb.updateLine(7, getWaitingLine(p));
        }

        TellAll(color("&e&l(!) &e" + leader.getName() + " &rstopped the countdown to change the game settings."));
        for (Player editor : getEditors()) giveCustomGameHostItems(editor);
    }

    /**
     * Retrieves the location of the game lobby for the current map.
     *
     * @return The Location of the game lobby.
     */
    public Location GetLobbyLoc() {
        MapInstance mapInstance = (map != null) ? map.GetInstance() : duosMap.GetInstance();
        Vector lobbyVector = mapInstance.lobbyLoc;
        return new Location(mapWorld, lobbyVector.getX(), lobbyVector.getY(), lobbyVector.getZ());
    }

    public boolean isOpen() {
        return state == GameState.WAITING && this.players.size() < getMaxPlayerCount();
    }

    /** Private games can hold far more players than the normal game type limit. */
    public int getMaxPlayerCount() {
        return customGame ? CUSTOM_GAME_MAX_PLAYERS : gameType.getMaxPlayers();
    }

    public void SendPlayerToMap(Player player) {
        player.teleport(GetLobbyLoc());
    }

    public GameReason AddSpectator(Player player) {
        if (state == GameState.STARTED) {
            if (!players.contains(player)) {
                spectators.add(player);
                for (Player gamePlayer : players) gamePlayer.hidePlayer(player);
                for (Player spectator : this.spectators) spectator.showPlayer(player);

                player.getInventory().clear();
                player.setAllowFlight(true);
                player.teleport(GetSpecLoc());
                player.setDisplayName(color(player.getName() + " &7&oSpectator&f"));

                // Remove lobby board if present (external manager map)
                gameManager.getMain().getScoreboardManager().removeLobbyBoard(player);

                setGameScore(player); // spectator scoreboard
                return GameReason.SPECTATOR;
            } else return GameReason.ALREADY_IN;
        } else return GameReason.FAIL;
    }

    @SuppressWarnings("deprecation")
    public GameReason AddPlayer(Player player) {
        anthony.SuperCraftBrawl.ScoreboardManager boardManager = getGameManager().getMain().getScoreboardManager();
        PlayerListener listener = getGameManager().getMain().getListener();

        if (this.state == GameState.WAITING) {
            if (!this.players.contains(player)) {
                if (isLobbyFull(player)) return GameReason.FULL;

                players.add(player);
                player.sendMessage(color("&2&l(!) &rYou have joined &e&l" + map.toString()));
                if (this.gameType == GameType.FRENZY)
                    TitleUtil.sendTitle(player, "&e&l" + map.toString(), "&fYour class will be randomly selected", 10, 60, 5);
                else
                    TitleUtil.sendTitle(player, "&e&l" + map.toString(), "&fChoose your class", 10, 60, 5);

                listener.resetDoubleJump(player);
                listener.resetArmor(player);
                for (Player gamePlayer : players) {
                    if (gamePlayer.getWorld() != getMapWorld()) {
                        SendPlayerToMap(gamePlayer);
                        CheckForGameStart();
                        boardManager.waitingLobbyBoard(player, this);
                    }

                    if (gamePlayer != player) {
                        if (this.getMap() != null) { // Update player count on board for Solos
                            boardManager.updatePlayerCountBoard(gamePlayer, this);
                        } else { // Update player count on board for Duos
                            FastBoard fb = boards.get(gamePlayer.getUniqueId());
                            if (fb != null) fb.updateLine(5, " " + ChatColor.RESET + players.size() + "/6");
                        }
                    }

                    gamePlayer.sendMessage(color("&2&l(!) &f" + player.getName() +
                            "&a joined (" + this.players.size() + "/" + getMaxPlayers() + ")"));
                }

                return GameReason.SUCCESS;
            } else return GameReason.ALREADY_IN;
        } else return GameReason.ALREADYPLAYING;
    }

    /*
     * This helper function checks if the game is either frenzy,
     * classic or duels to see what the max players for that
     * category are allowed
     */
    private String getMaxPlayers() {
        if (customGame) return "" + getMaxPlayerCount();
        String maxPlayers = ""
                + (map.GetInstance().gameType == GameType.FRENZY ? "" + gameType.getMaxPlayers() : "")
                + (map.GetInstance().gameType == GameType.CLASSIC ? "" + gameType.getMaxPlayers() : "")
                + (map.GetInstance().gameType == GameType.DUEL ? "" + gameType.getMaxPlayers() : "");
        return maxPlayers;
    }

    public boolean isLobbyFull(Player player) {
        if (this.map != null) {
            if (customGame) {
                if (players.size() < getMaxPlayerCount()) return false;
                player.sendMessage(color("&c&l(!) &fThis game is full!"));
                return true;
            }
            if (this.gameType == GameType.DUEL && players.size() >= 2) {
                player.sendMessage(color("&c&l(!) &fThis game is full!"));
                return true;
            }
            if (!(player.hasPermission("scb.bypassFull")) && gameType == GameType.CLASSIC && players.size() >= 6) {
                player.sendMessage(color("&c&l(!) &fThis game is full! Purchase a rank at &e&nminezone.club&f to bypass this restriction"));
                return true;
            }
            return false;
        } else {
            if (players.size() >= 7) {
                player.sendMessage(color("&c&l(!) &fThis game is full!"));
                return true;
            }
            players.add(player);
            player.sendMessage(this.gameManager.getMain().color("&2&l(!) &rYou have joined &r&l" + duosMap.toString()));
            player.sendMessage(this.gameManager.getMain().color("&2&l(!) &rSelect a team in your 2nd slot!"));
            return false;
        }
    }

    public FastBoard board;

    public void setClass(Player player, ClassType type) {
        classSelection.put(player, type);
        if (gameType != GameType.FRENZY && gameType != GameType.GUNGAME) {
            board = boards.get(player.getUniqueId());
            if (board != null) board.updateLine(3, color("&fClass: &a" + type.toString()));

            if (player.hasPermission("scb.chat"))
                player.setDisplayName("" + player.getName() + " " + type.getTag());
            else
                player.setDisplayName("" + player.getName() + " " + type.getTag() + ChatColor.GRAY);
        }
    }

    public void CheckForGameStart() {
        if (players.size() < 2) return;

        if (customGame && !customGameCountdownStarted) {
            return;
        }

        StartGameTimer();
    }

    public int getSecondsUntilStart() {
        if (!gameManager.getMain().tournament)
            return timeToStartSeconds = 30;
        return timeToStartSeconds = 60;
    }

    // Legacy start timer – now tracked
    public void StartGameTimer2() {
        SignManager sm = getGameManager().getMain().getSignManager();

        if (gameStartTime == null) {
            timeToStartSeconds = getSecondsUntilStart();
            gameStartTime = new BukkitRunnable() {
                @Override
                public void run() {
                    if (sm != null) sm.updateSignCountdown(s, timeToStartSeconds);
                    int ticks = timeToStartSeconds;

                    if (ticks == 0) {
                        StartGame();
                        GameScoreboard();
                        gameStartTime = null;
                        this.cancel();
                    }
                    timeToStartSeconds--;
                }
            };
            BukkitTask task = gameStartTime.runTaskTimer(gameManager.getMain(), 0, 20);
            trackInstanceTask(task);
        }
    }

    private void broadcastToWorld(World world, String plainMsg, BaseComponent... clickableMsg) {
        if (world == null) return;
        for (Player p : world.getPlayers()) {
            if (plainMsg != null) p.sendMessage(plainMsg);
            if (clickableMsg != null && clickableMsg.length > 0) p.spigot().sendMessage(clickableMsg);
        }
    }

    public void StartGameTimer() {
        if (gameStartTime != null) return;

        if (customGame && !customGameCountdownStarted) {
            return;
        }

        final SignManager sm = getGameManager().getMain().getSignManager();
        timeToStartSeconds = getSecondsUntilStart();

        gameStartTime = new BukkitRunnable() {
            @Override
            public void run() {
                final int ticks = timeToStartSeconds;

                if (sm != null) sm.updateSignCountdown(s, ticks);

                if (ticks == 0) {
                    StartGame();
                    GameScoreboard();
                    gameStartTime = null;
                    cancel();
                    return;
                }

                if (ticks == 60 && gameManager.getMain().tournament) {
                    TellAll(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "The game is now starting...");
                }
                if (ticks == 30) {
                    for (Player p : players) {
                        if (!p.getInventory().contains(votePaper)) p.getInventory().addItem(votePaper);
                    }
                    gameStartingMessage(map.toString());
                }

                if (ticks == 18) tipMessages();

                if (ticks == 25 || ticks == 20 || ticks == 15 || ticks == 10 || (ticks <= 5 && ticks >= 2))
                    for (Player p : players) p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                else if (ticks == 1)
                    for (Player p : players) p.playSound(p.getLocation(), Sound.NOTE_PLING, 5f, 7f);

                for (Player p : players) {
                    p.setLevel(ticks);
                    if (ticks <= 5 && ticks >= 1)
                        TitleUtil.sendTitle(p, "&a" + ticks, "", 0, 20, 0);

                    if (ticks <= 60 && ticks >= 1 && players.size() >= 2) {
                        FastBoard board = boards.get(p.getUniqueId());
                        if (board != null) board.updateLine(7, color("&fStarting In: &e" + ticks + "s"));

                        if (ticks > 5 && !p.getInventory().contains(votePaper)) {
                            p.getInventory().addItem(votePaper);
                        }
                    }
                }

                if (ticks == 5) {
                    getGameSettings().changeGameType(false);
                    getGameSettings().increaseLightningRate();
                    getGameSettings().setTimeOfDay();
                    getGameSettings().enableSantaFlyover();
                    removeVotePaper();
                    removeClassSelectors(); // after the game type vote, which can switch to Frenzy
                }

                timeToStartSeconds--;
            }
        };
        BukkitTask task = gameStartTime.runTaskTimer(gameManager.getMain(), 0L, 20L);
        trackInstanceTask(task);
    }

    private void gameStartingMessage(String mapName) {
        World lobby = getGameManager().getMain().getLobbyWorld();

        TextComponent join = new TextComponent("     " + color("&a&lClick here to join!"));
        join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + mapName));

        // Private custom games aren't announced. Public ones say CUSTOM, and the click joins this custom game
        // (/join only finds normal games)
        String custom = "";
        if (customGame) {
            if (!customGamePublic) join = null;
            else {
                custom = "&6&lCUSTOM ";
                join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/joincustomgame " + getCustomGameHostName()));
            }
        }

        if (join != null && gameType == GameType.FRENZY) {
            String msg = color("&2&l(!) &a&lA " + custom + "&7&oFrenzy &a&lgame on&f&l " + mapName + " &a&lis starting in 30 seconds");
            broadcastToWorld(lobby, msg, join);
        } else if (join != null && gameType == GameType.CLASSIC) {
            String msg = color("&2&l(!) &a&lA " + custom + "&a&lgame on&f&l " + mapName + " &a&lis starting in 30 seconds");
            broadcastToWorld(lobby, msg, join);
        }

        TellAll(color("&2&l(!) &r30 seconds to game start!"));

        for (Player player : players)
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
    }

    public boolean isClassSelectorRemoved() {
        return classSelectorRemoved;
    }

    /*
     * Frenzy and Class Switch pick everyone's class at random, so the class selector goes once that's final
     * (the countdown, or /frenzy).
     */
    public void removeClassSelectors() {
        if (gameType != GameType.FRENZY && !getGameSettings().randomClasses) return;
        classSelectorRemoved = true;

        for (Player player : players) {
            if (player.getOpenInventory() != null && player.getOpenInventory().getTitle().contains("Class"))
                player.closeInventory();
            player.getInventory().remove(Material.ENCHANTED_BOOK);
        }
    }

    /** The countdown stopped (or /frenzy switched back): everyone gets their class selector back. */
    public void restoreClassSelectors() {
        if (!classSelectorRemoved) return;
        classSelectorRemoved = false;

        for (Player player : players) {
            player.getInventory().setItem(0, LobbyItems.getClassSelectorItem());
        }
    }

    private void removeVotePaper() {
        for (Player player : players) {
            if (player.getInventory().contains(votePaper)) {
                if (player.getOpenInventory() != null && player.getOpenInventory().getTitle().contains("Vote"))
                    player.closeInventory();
                player.getInventory().removeItem(votePaper);
            }
        }
    }

    private void tipMessages() {
        Random rand = new Random();
        int chance = rand.nextInt(4);

        if (chance == 0)
            TellAll(color("&2[&aTip&2] &aExecute double jump by tapping the space bar twice!"));
        else if (chance == 1)
            TellAll(color("&2[&aTip&2] &aConsider purchasing a rank at our /store for more SCB perks!"));
        else if (chance == 2)
            TellAll(color("&2[&aTip&2] &aBe sure to select a class by using the enchanted book!"));
        else
            TellAll(color("&2[&aTip&2] &aUse the vote paper to ready up or vote for game settings!"));

        for (Player player : players)
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
    }

    public void initialSpawn() {
        MapInstance mapInstance = map != null ? map.GetInstance() : duosMap.GetInstance();
        List<Vector> spawnPoints = mapInstance.spawnPos;
        World world = getMapWorld();

        if (spawnPoints.isEmpty()) {
            Location defaultSpawn = new Location(world, 42, 2, 2.5);
            for (Player player : players) {
                player.teleport(defaultSpawn);
            }
            return;
        }

        ArrayList<Vector> shuffledSpawns = new ArrayList<>(spawnPoints);
        Collections.shuffle(shuffledSpawns);

        for (int i = 0; i < players.size(); i++) {
            Vector spawn = shuffledSpawns.get(i % shuffledSpawns.size());
            players.get(i).teleport(spawn.toLocation(world));
        }
    }

    public Location GetRespawnLoc() {
        MapInstance mapInstance = (map != null) ? map.GetInstance() : duosMap.GetInstance();

        if (mapInstance.spawnPos.size() == 0)
            return GetLobbyLoc().add(new Vector(42, 2, 2.5));
        else {
            int rand = random.nextInt(mapInstance.spawnPos.size());
            if (lastSpawn >= 0) {
                while (rand == lastSpawn) rand = random.nextInt(mapInstance.spawnPos.size());
            }
            lastSpawn = rand;
            Vector spawnPos = mapInstance.spawnPos.get(rand);
            return new Location(mapWorld, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        }
    }

    public Location GetSpecLoc() {
        MapInstance mapInstance = (map != null) ? map.GetInstance() : duosMap.GetInstance();
        Vector v = mapInstance.specLoc;
        return new Location(mapWorld, v.getX(), v.getY(), v.getZ());
    }

    public double boundsX, boundsZ;

    public boolean isInBounds(Location loc) {
        MapInstance mapInstance = (map != null) ? map.GetInstance() : duosMap.GetInstance();
        Vector v = mapInstance.center;
        Location centre = new Location(mapWorld, v.getX(), v.getY(), v.getZ());
        boundsX = mapInstance.boundsX;
        boundsZ = mapInstance.boundsZ;

        if (Math.abs(centre.getX() - loc.getX()) > boundsX) return false;
        if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ) return false;
        return true;
    }

    private void setTeams() {
        for (Player gamePlayer : players) {
            if (this.duosMap != null) {
                if (!(this.team.containsKey(gamePlayer))) {
                    if (redTeam.size() == 1 || redTeam.isEmpty()) {
                        redTeam.add(gamePlayer);
                        team.put(gamePlayer, "Red");
                        gamePlayer.sendMessage(this.gameManager.getMain().color("&2&l(!) &rYou are on &c&lRed Team"));
                    } else if (blueTeam.size() == 1 || blueTeam.isEmpty()) {
                        blueTeam.add(gamePlayer);
                        team.put(gamePlayer, "Blue");
                        gamePlayer.sendMessage(this.gameManager.getMain().color("&2&l(!) &rYou are on &b&lBlue Team"));
                    } else if (blackTeam.size() == 1 || blackTeam.isEmpty()) {
                        blackTeam.add(gamePlayer);
                        team.put(gamePlayer, "Black");
                        gamePlayer.sendMessage(this.gameManager.getMain().color("&2&l(!) &rYou are on &0&lBlack Team"));
                    }
                }
            }
        }
    }

    private void addAliveTeams() {
        if (redTeam != null && redTeam.size() > 0) aliveTeams++;
        if (blueTeam != null && blueTeam.size() > 0) aliveTeams++;
        if (blackTeam != null && blackTeam.size() > 0) aliveTeams++;
    }

    private void addAlivePlayers() {
        alivePlayers = players.size();
    }

    private void giveRandomItemDrop() {
        for (Player player : this.players) {
            BaseClass bc = this.classes.get(player);
            if (bc != null) {
                ItemStack item = getItemToDrop();
                item = checkIfExtraLife(item);
                if (bc.getType() == ClassType.Melon) {
                    if (item != null) player.getInventory().setItem(2, item);
                } else {
                    if (item != null) player.getInventory().addItem(item);
                }
            }
        }
    }

    private ItemStack checkIfExtraLife(ItemStack item) {
        while (true) {
            if (item != null && item.getType() != Material.PRISMARINE_SHARD) break;
            item = getItemToDrop();
        }
        return item;
    }

    public void StartGame() {
        if (sm != null && s != null) this.sm.updateSignInProgress(s);

        setTeams();
        startLightningDropsTimer();

        if (gameSettings.santaFlyover)
            new SantaFlyoverEvent(this).startEvent(30);

        TellAll(color("&e&l----------------------------------------"));
        TellAll(color("            &6&lSUPER CRAFT BROTHERS"));
        TellAll("");
        int startingLives = gameSettings.startingLives;
        TellAll(color("&r  " + startingLives + (startingLives == 1 ? " life" : " lives")
                + " each with different classes & unique"));
        TellAll(color("&r    abilities. Look out for lightning drops as"));
        TellAll(color("&r       they can spawn useful powerups."));
        TellAll(color("&r                   Good Luck!"));
        TellAll(color("&e&l----------------------------------------"));

        if (this.gameType == GameType.GUNGAME) {
            TellAll(color("&e&l----------------------------------------"));
            TellAll("" + ChatColor.AQUA + ChatColor.BOLD + "             Class Lineup");
            TellAll(Arrays.toString(this.classList.toArray()));
            TellAll(color("&e&l----------------------------------------"));
        }

        this.state = GameState.STARTED;
        if (customGame) CustomGamesGUI.refreshOpenMenus(); // Lobby -> Playing
        resetState();
        LoadClasses();
        // Private game "Lives" modifier (5 by default)
        for (BaseClass startingClass : classes.values()) {
            if (startingClass != null) startingClass.lives = gameSettings.startingLives;
        }
        GameScoreboard();
        addAlivePlayers();
        addAliveTeams();
        giveRandomItemDrop();
        initialSpawn();
        gameTicks();
        startRandomClassesTimer();

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player gamePlayer : players) sendScoreboardUpdate(gamePlayer);
            }
        };
        BukkitTask task = r.runTaskLater(getGameManager().getMain(), 20);
        trackInstanceTask(task);
    }

    private List<ItemStack> getSavedLootDropItems(Player player) {
        List<ItemStack> savedDrops = new ArrayList<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (GameLootDrops.isLootDropItem(item)) {
                savedDrops.add(item.clone());
            }
        }

        return savedDrops;
    }

    private void startRandomClassesTimer() {
        if (!getGameSettings().randomClasses) return;
        if (getGameSettings().randomClassCycleSeconds <= 0) return;

        BukkitRunnable runnable = new BukkitRunnable() {
            int seconds = getGameSettings().randomClassCycleSeconds;

            @Override
            public void run() {
                if (state != GameState.STARTED) {
                    cancel();
                    return;
                }

                if (!getGameSettings().randomClasses) {
                    cancel();
                    return;
                }

                if (seconds <= 0) {
                    TellAll(color("&d&l(!) &rRandomizing classes..."));

                    for (Player player : new ArrayList<>(players)) {
                        BaseClass oldClass = classes.get(player);

                        if (oldClass == null) continue;
                        if (oldClass.getLives() <= 0) continue;
                        if (oldClass.isDead) continue;

                        List<ItemStack> savedDrops = new ArrayList<>();

                        for (ItemStack item : player.getInventory().getContents()) {
                            if (GameLootDrops.isLootDropItem(item)) {
                                savedDrops.add(item.clone());
                            }
                        }

                        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.3F);
                        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 1.8F);

                        removeOldClassPotionEffects(player, oldClass);
                        getGameManager().getMain().getListener().resetPotionEffects(player);

                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);

                        reRandomizeClass(player);

                        BaseClass newClass = classes.get(player);

                        if (newClass != null) {
                            newClass.loadPlayer();

                            for (ItemStack savedDrop : savedDrops) {
                                if (savedDrop != null && savedDrop.getType() != Material.AIR) {
                                    player.getInventory().addItem(savedDrop);
                                }
                            }

                            player.updateInventory();

                            TitleUtil.sendTimes(player, 1, 30, 10);
                            TitleUtil.setSubtitle(player, "" + newClass.getType().getTag());
                            TitleUtil.setTitle(player, "");

                            player.playSound(player.getLocation(), Sound.HORSE_ARMOR, 1.0F, 1.0F);
                            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0F, 1.2F);
                        }
                    }

                    seconds = getGameSettings().randomClassCycleSeconds;
                    return;
                }

                // 3, 2, 1 subtitle countdown before the classes change
                if (seconds <= 3) {
                    for (Player player : players) {
                        BaseClass bc = classes.get(player);
                        if (bc == null || bc.getLives() <= 0 || bc.isDead) continue;

                        TitleUtil.sendTimes(player, 0, 25, 5);
                        TitleUtil.setSubtitle(player, "&e" + seconds);
                        TitleUtil.setTitle(player, "");
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
                    }
                }

                seconds--;
            }
        };

        BukkitTask task = runnable.runTaskTimer(gameManager.getMain(), 20L, 20L);
        trackInstanceTask(task);
        runnables.add(runnable);
    }

    private void removeOldClassPotionEffects(Player player, BaseClass oldClass) {
        if (player == null || oldClass == null) return;

        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            if (isOldClassPotionEffect(oldClass.getType(), effect)) {
                player.removePotionEffect(effect.getType());
            }
        }
    }

    private boolean isOldClassPotionEffect(ClassType classType, PotionEffect effect) {
        if (classType == null || effect == null) return false;

        PotionEffectType type = effect.getType();

        /*
         * Only remove long/passive class effects.
         * This prevents removing short effects like:
         * - Strength on Kill
         * - poison from another player
         * - regen from drops
         * - temporary ability effects
         */
        if (effect.getDuration() < 20 * 60) {
            return false;
        }

        switch (classType) {
            case Bat:
                return type == PotionEffectType.SPEED
                        || type == PotionEffectType.INVISIBILITY
                        || type == PotionEffectType.DAMAGE_RESISTANCE;

            case Bunny:
                return type == PotionEffectType.SPEED
                        || type == PotionEffectType.JUMP;

            case Rabbit:
                return type == PotionEffectType.SPEED
                        || type == PotionEffectType.JUMP;

            case Cloud:
                return type == PotionEffectType.JUMP;

            case Parrot:
                return type == PotionEffectType.JUMP;

            case Elf:
                return type == PotionEffectType.SPEED;

            case EnderDragon:
                return type == PotionEffectType.WEAKNESS;

            case Ice:
                return type == PotionEffectType.WEAKNESS;

            case Jeb:
                return type == PotionEffectType.WEAKNESS;

            case Notch:
                return type == PotionEffectType.WEAKNESS;

            case Herobrine:
                return type == PotionEffectType.DAMAGE_RESISTANCE;

            case Melon:
                return type == PotionEffectType.DAMAGE_RESISTANCE;

            case Mooshroom:
                return type == PotionEffectType.SPEED;

            case Noteblock:
                return type == PotionEffectType.SPEED
                        || type == PotionEffectType.DAMAGE_RESISTANCE;

            case Ocelot:
                return type == PotionEffectType.SPEED;

            case Potato:
                return type == PotionEffectType.SPEED;

            case Vindicator:
                return type == PotionEffectType.WEAKNESS;

            case Wizard:
                return type == PotionEffectType.SPEED
                        || type == PotionEffectType.JUMP;

            case Santa:
                return type == PotionEffectType.INCREASE_DAMAGE
                        || type == PotionEffectType.SLOW
                        || type == PotionEffectType.DAMAGE_RESISTANCE
                        || type == PotionEffectType.JUMP;

            default:
                return false;
        }
    }

    /** Low Gravity modifier: Jump Boost X (amplifier 9). */
    public static final int LOW_GRAVITY_AMPLIFIER = 9;

    /**
     * Keeps Low Gravity's Jump Boost on every alive player.
     * - A weaker Jump Boost (e.g. Bunny/Cloud passives) gets overridden.
     * - A stronger one (jump pads, class abilities) is left alone until it runs out,
     *   then Low Gravity is put straight back on.
     * Uses a short duration that keeps getting refreshed, so it wears off by itself
     * within ~2s once the player dies, becomes a spectator, leaves, or the game ends.
     */
    private void applyLowGravity() {
        for (Player player : players) {
            if (player == null || !player.isOnline() || player.getGameMode() == GameMode.SPECTATOR) continue;
            if (spectators.contains(player)) continue;

            BaseClass baseClass = classes.get(player);
            if (baseClass == null || baseClass.isDead || baseClass.getLives() <= 0) continue;

            PotionEffect current = null;
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (effect.getType().equals(PotionEffectType.JUMP)) {
                    current = effect;
                    break;
                }
            }

            if (current != null) {
                // Stronger boost active (jump pad, ability) -> let it run out first
                if (current.getAmplifier() > LOW_GRAVITY_AMPLIFIER) continue;
                // Ours (or equal) and not about to expire -> nothing to do
                if (current.getAmplifier() == LOW_GRAVITY_AMPLIFIER && current.getDuration() > 10) continue;
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, LOW_GRAVITY_AMPLIFIER, false, false), true);
        }
    }

    private void gameTicks() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameTicks % (60 * 20) == 0) {
                    time.getScoreboard().resetScores(time.getEntry());
                    time = o.getScore(color("&fTime: &a" + gameTime + "m"));
                    time.setScore(0);
                    gameTime++;
                }

                for (Entry<Player, BaseClass> playerClass : classes.entrySet())
                    playerClass.getValue().tick(gameTicks);

                for (Entity e : mapWorld.getEntities()) {
                    if (e instanceof Arrow) {
                        Arrow a = (Arrow) e;
                        if (a.isOnGround()) e.remove();
                    }
                }

                if (gameTicks % 20 == 0) {
                    for (Player gamePlayer : players) {
                        if (gamePlayer.getFireTicks() >= 200) gamePlayer.setFireTicks(110);
                    }
                }

                if (gameTicks % 5 == 0 && gameSettings.lowGravity && state == GameState.STARTED) {
                    applyLowGravity();
                }
                gameTicks++;
            }
        };
        BukkitTask task = runnable.runTaskTimer(gameManager.getMain(), 0, 1);
        trackInstanceTask(task);
        runnables.add(runnable);
    }

    @SuppressWarnings("deprecation")
    public void sendScoreboardUpdate(Player player) {
        ClassType classType = this.classes.get(player).getType();
        if (classType == null) return;

        // Prefix the team name with a rank-order number so the tablist stays
        // sorted by rank in-game, matching the lobby TablistManager ordering.
        Rank rank = getGameManager().getMain().getRankManager().getRank(player);
        String rankPrefix = String.format("%02d_", rankOrder(rank));
        String teamName = rankPrefix + player.getName();
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Scoreboard board = o.getScoreboard();

        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);

        if (!team.hasEntry(player.getName())) team.addEntry(player.getName());

        if (this.classes.get(player).getLives() > 0) {
            String baseName = classType.getSecondTag() != null ? classType.getSecondTag() : classType.getTag();
            baseName += " ";
            if (baseName.length() > 12) baseName = baseName.substring(0, Math.min(baseName.length(), 10)).trim() + " " + ChatColor.RESET;
            team.setPrefix(baseName);
        } else {
            team.setPrefix("");
        }
    }

    /** Mirrors TablistManager.orderFor — lower = higher in tab. */
    private int rankOrder(Rank r) {
        switch (r) {
            case OWNER:        return 0;
            case ADMIN:        return 1;
            case DEVELOPER:    return 2;
            case SR_MODERATOR: return 3;
            case MODERATOR:    return 4;
            case TRAINEE:      return 5;
            case QA:           return 6;
            case BUILDER:      return 7;
            case MEDIA:        return 8;
            case SUPREME:      return 9;
            case LEGENDARY: return 10;
            case PRO:          return 11;
            case VIP:          return 12;
            case DEFAULT:
            default:           return 13;
        }
    }

    public Location getItemSpawnLoc() {
        Random rand = new Random();
        int attempts = 0;
        Location respawnLoc = GetRespawnLoc();

        while (attempts < 100) {
            Location loc = respawnLoc.clone().add(rand.nextInt(51) - 25, 10, rand.nextInt(51) - 25);
            while (loc.getY() > 40) {
                Material mat = loc.getBlock().getType();
                if (mat.isSolid() && isNotWaterOrLava(loc.clone().add(0, 1, 0).getBlock().getType())) {
                    return loc.add(0, 1, 0);
                }
                loc.setY(loc.getY() - 1);
            }
            attempts++;
        }
        return respawnLoc;
    }

    public void startLightningDropsTimer() {
        int seconds = getGameSettings().dropTimer;
        lightningDropCountdown = seconds;
        nextItemToDrop = getItemToDrop();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (lightningDropCountdown <= 0) {
                    Location loc = getItemSpawnLoc();
                    ItemStack drop = nextItemToDrop;
                    Item item = mapWorld.dropItem(loc, drop);
                    item.setVelocity(new Vector(0, 0, 0));
                    mapWorld.strikeLightningEffect(loc);

                    int x = (int) loc.getX();
                    int y = (int) loc.getY();
                    int z = (int) loc.getZ();
                    loc = new Location(mapWorld, x, y, z);
                    recentDrop = loc;

                    for (Player gamePlayer : players) {
                        BaseClass bc = classes.get(gamePlayer);
                    }
                    lightningDropCountdown = seconds;
                    nextItemToDrop = getItemToDrop();
                } else {
                    lightningDropCountdown--;
                }
            }
        };
        BukkitTask task = runnable.runTaskTimer(gameManager.getMain(), 0, 20);
        trackInstanceTask(task);
        runnables.add(runnable);
    }

    public ItemStack getItemToDrop() {
        GameSettings settings = getGameSettings();
        return GameLootDrops.getDrop(settings.enabledDrops, settings.opDrops).getItem();
    }

    public String truncateString(String string, int length) {
        if (string.length() <= length) return string;
        else return string.substring(0, length);
    }

    private Scoreboard c;
    private Score time;
    private Objective o;

    public void GameScoreboard() {
        try {
            ScoreboardManager m = Bukkit.getScoreboardManager();
            c = m.getNewScoreboard();

            if (this.map != null) o = c.registerNewObjective("" + ChatColor.BOLD + this.map.toString(), "");
            else o = c.registerNewObjective("" + ChatColor.BOLD + this.duosMap.toString(), "");

            livesObjective = o;
            o.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (Player player : players) {
                getGameManager().getMain().getScoreboardManager().removeLobbyBoard(player);
                BaseClass playerClass = classes.get(player);
                PlayerData data = gameManager.getMain().getDataManager().getPlayerData(player);

                if (map != null) {
                    Score livesScore = o.getScore(truncateString(
                            playerClass.getType().getTag() + " " + ChatColor.RESET + player.getName(), 38));
                    livesScore.setScore(playerClass.lives);
                    playerClass.score = livesScore;
                } else {
                    if (team.get(player).equals("Blue"))
                        boardColor(o, player, ChatColor.BLUE);
                    else if (team.get(player).equals("Red"))
                        boardColor(o, player, ChatColor.RED);
                    else if (team.get(player).equals("Black"))
                        boardColor(o, player, ChatColor.BLACK);
                }
                Score line = o.getScore("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "--------------------");
                line.setScore(0);
                Score game = o.getScore(color("&fMode: &a" + this.gameType.getName()));
                game.setScore(0);
                time = o.getScore(color("&fTime: &a" + gameTime + "m"));
                time.setScore(0);
                player.setScoreboard(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void boardColor(Objective o, Player player, ChatColor c) {
        BaseClass bc = classes.get(player);
        if (bc != null) {
            Score livesScore = o.getScore(truncateString(bc.getType().getTag() + " " + c + player.getName() + "", 38));
            livesScore.setScore(bc.lives);
            bc.score = livesScore;
        }
    }

    private void setGameScore(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); // Reset to default
        player.setScoreboard(c); // For joining spectators
    }

    public int teamsAlive = 0;

    public void CheckForWin() {
        List<String> aliveTeam = new ArrayList<String>();

        if (map != null) {
            int alivePlayers = 0;
            Player alivePlayer = null;
            List<Player> lastAlive = new ArrayList<Player>();
            for (Entry<Player, BaseClass> entry : classes.entrySet())
                if (entry.getValue().getLives() > 0) {
                    alivePlayers++;
                    alivePlayer = entry.getKey();
                    lastAlive.add(alivePlayer);
                }
            if (alivePlayers == 1 && alivePlayer != null && lastAlive.size() == 1) {
                WinGame(lastAlive);
            } else if (alivePlayers <= 0)
                EndGame();
            else
                announcePlayersLeft(alivePlayers);

            this.alivePlayers = alivePlayers;
        } else {
            teamsAlive = 0;
            for (Entry<Player, BaseClass> entry : classes.entrySet()) {
                if (entry.getValue().getLives() > 0) {
                    if (!(aliveTeam.contains(team.get(entry.getKey())))) {
                        aliveTeam.add(team.get(entry.getKey()));
                        teamsAlive++;
                    }
                }
            }

            if (teamsAlive == 1) {
                if (aliveTeam.contains("Red"))
                    WinGame(redTeam);
                else if (aliveTeam.contains("Blue"))
                    WinGame(blueTeam);
                else if (aliveTeam.contains("Black"))
                    WinGame(blackTeam);
            } else if (teamsAlive == 0)
                EndGame();
        }
    }

    private void announcePlayersLeft(int alivePlayers) {
        TellAll(color("&2&l(!) &rThere are &e" + alivePlayers + "&r players left!"));
        for (Player gamePlayer : this.players) gamePlayer.playSound(gamePlayer.getLocation(), Sound.NOTE_PLING, 1, 1);
    }

    public void PlayerDeath(Player player) {
        BaseClass baseClassBeforeChecks = this.classes.get(player);
        // Already dead and waiting to respawn: one death only (e.g. several TNT exploding in the same tick)
        if (baseClassBeforeChecks == null || baseClassBeforeChecks.isDead) return;
        PlayerDeathEvent event = new PlayerDeathEvent(player, null, 0, null);
        baseClassBeforeChecks.isDead = true;
        baseClassBeforeChecks.onDeath(event);

        final BaseClass baseClass = this.classes.get(player);
        if (baseClass == null) return;

        try {
            // Immediate death handling
            player.getInventory().clear();
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(GetSpecLoc());

            // Handle class changes (Frenzy/GunGame) while dead
            if (this.gameType == GameType.FRENZY && baseClass.getLives() > 0) {
                reRandomizeClass(player);
            } else if (this.gameType == GameType.GUNGAME && baseClass.getLives() > 0) {
                nextClass(player);
            }
            final BaseClass finalBaseClass = this.classes.get(player);
            finalBaseClass.isDead = true; // Frenzy / GunGame just gave them a new class, which isn't marked dead yet

            // Countdown & respawn
            BukkitRunnable runTimer = new BukkitRunnable() {
                int ticks = gameSettings.respawnSeconds; // 0 = respawn instantly
                boolean timesSent = false;

                @SuppressWarnings("deprecation")
                public void run() {
                    if (GameInstance.this.state == GameState.ENDED) {
                        cancel();
                        return;
                    }

                    if (finalBaseClass.getLives() > 0) {
                        if (this.ticks == 0) {
                            // Finish countdown -> respawn if still valid
                            if (state != GameState.STARTED || !players.contains(player) || getWinnerList().contains(player)) {
                                cancel();
                                player.setAllowFlight(false);
                                player.setAllowFlight(true);
                                return;
                            }

                            player.teleport(GameInstance.this.GetRespawnLoc());
                            player.setGameMode(GameMode.ADVENTURE);
                            player.setMaxHealth(getMaxHealthPoints());
                            player.setHealth(getMaxHealthPoints());
                            player.setAllowFlight(true);
                            GameInstance.this.getGameManager().addSpawnProtection(player);

                            if (!GameInstance.this.players.contains(player)) {
                                GameInstance.this.getGameManager().getMain().ResetPlayer(player);
                            } else {
                                finalBaseClass.loadPlayer();

                                if (GameInstance.this.gameType == GameType.FRENZY
                                        || GameInstance.this.gameType == GameType.GUNGAME) {
                                    TitleUtil.sendTimes(player, 1, 30, 10);
                                    TitleUtil.setSubtitle(player, "" + finalBaseClass.getType().getTag());
                                    TitleUtil.setTitle(player, "");
                                } else {
                                    TitleUtil.sendTimes(player, 1, 30, 10);
                                    TitleUtil.setSubtitle(player, "");
                                    TitleUtil.setTitle(player, "&eRespawned");
                                }
                                finalBaseClass.isDead = false;
                            }
                            cancel();
                            return;
                        }

                        // During countdown
                        if (GameInstance.this.state == GameState.STARTED) {
                            if (!players.contains(player)) {
                                cancel();
                                return;
                            }
                            player.setGameMode(GameMode.SPECTATOR);

                            if (!timesSent) {
                                // Stays up for exactly the countdown so it matches the Respawn Time setting
                                TitleUtil.sendTitle(player, "&eRespawning In", "&c" + this.ticks, 1, this.ticks * 20, 1);
                                timesSent = true;
                            } else {
                                TitleUtil.updateTitleText(player, null, "&c" + this.ticks);
                            }
                        }
                    }
                    this.ticks--;
                }
            };

            BukkitTask task = runTimer.runTaskTimer(this.gameManager.getMain(), 0L, 20L);
            trackPerPlayerTask(player.getUniqueId(), task);
            this.runnables.add(runTimer);

            // Post-death housekeeping
            player.setHealth(player.getMaxHealth()); // not 20: the Max Health modifier can make it lower
            player.setAllowFlight(true);
            player.setGameMode(GameMode.ADVENTURE);

            // If out of lives -> convert to spectator permanently
            if (finalBaseClass.getLives() == 0) {
                this.playerPosition.add(player);

                if (this.players.size() > 2) {
                    TitleUtil.sendTitle(player, "&cYou have died!", "&fYou are now a Spectator", 0, 50, 10);
                    player.teleport(GetSpecLoc());
                }

                player.getPlayer().setGameMode(GameMode.ADVENTURE);
                player.spigot().setCollidesWithEntities(false);
                player.setAllowFlight(false);
                player.setAllowFlight(true);
                player.getInventory().clear();
                getGameManager().getMain().getLobbyItems().spectatorItems(player);
                getGameManager().getMain().getListener().resetArmor(player);

                for (Player gamePlayer : this.players) gamePlayer.hidePlayer(player);
                for (Player spectator : this.spectators) spectator.showPlayer(player);

                try { finalBaseClass.score.getScoreboard().resetScores(finalBaseClass.score.getEntry()); }
                catch (Exception e) { e.printStackTrace(); }

                CheckForWin();
            } else {
                player.getInventory().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void EndGameAnimation(List<Player> winner) {
        String winners = "";
        if (winner.size() == 1) winners = "" + winner.get(0).getName();
        else if (winner.size() > 1) winners = "" + winner.get(0).getName() + " " + winner.get(1).getName();

        for (Player player : players) {
            if (!(winner.contains(player))) {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendTitle("" + ChatColor.RED + ChatColor.BOLD + "GAME LOST",
                        "" + winners + ChatColor.GREEN + " won the game!");
            }
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
            player.setDisplayName("" + player.getName());
        }
        for (Player w : winner) {
            w.sendTitle("" + ChatColor.YELLOW + ChatColor.BOLD + "VICTORY", "" + ChatColor.GREEN + "You won the game!");
            w.setDisplayName("" + w.getName());
        }
    }

    private BukkitRunnable endGameAnimation;
    public int count = 0;

    // (Effects map left as Player->WinEffects because other files access it)
    public HashMap<Player, WinEffects> effects = new HashMap<Player, WinEffects>();

    public void EndGame() {
        state = GameState.ENDED;
        for (Entity en : mapWorld.getEntities()) if (!(en instanceof Player)) en.remove();

        if (!(winnerList.isEmpty())) {
            for (Player w : winnerList) {
                WinEffects we = new WinEffects(w, this);
                we.checkWinEffect();
                this.effects.put(w, we);
            }
        }

        // Save game data
        if (!getGameManager().getMain().getConfig().getBoolean("dev") && allClasses.size() > 1) {
            gameManager.getMain().getGameDataManager().saveMatch(gameType, map.getName(), gameTime, winnerList, allClasses, firstBlood);
        }

        endGameAnimation = new BukkitRunnable() {
            int ticks = 12;
            @Override
            public void run() {
                for (Player p : winnerList) {
                    if (map != null) {
                        if (p == null || p.getWorld() != mapWorld) ticks = 0;
                    } else {
                        if (p == null || p.getWorld() != mapWorld) {
                            count += 1;
                            if (winnerList.size() >= 2 && count >= 2) ticks = 0;
                            else if (winnerList.size() == 1 && count == 1) ticks = 0;
                        }
                    }
                }
                if (ticks == 0) {
                    for (Entry<Player, WinEffects> entry : effects.entrySet())
                        entry.getValue().removeWinEffects();

                    endGameAnimation = null;
                    this.cancel();
                    String mapName = (map != null) ? map.toString() : duosMap.toString();

                    for (Player gamePlayer : players) {
                        gamePlayer.setAllowFlight(false);
                        gamePlayer.setAllowFlight(true);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (gamePlayer != player) gamePlayer.showPlayer(player);
                        }
                    }

                    for (Player spectator : spectators) {
                        if (spectator.getWorld() == getMapWorld()) {
                            gameManager.getMain().ResetPlayer(spectator);
                            SetLobbyScoreboard(spectator);
                            spectator.setAllowFlight(false);
                            spectator.setAllowFlight(true);
                            spectator.setDisplayName(spectator.getName());
                            spectator.sendMessage(getGameManager().getMain().color("&2&l(!) &rThe game on &r&l"
                                    + mapName + " &rhas ended. Moving you back to spawn..."));
                            spectator.spigot().setCollidesWithEntities(true);
                            gameManager.getMain().restoreLobbyNameTag(spectator);
                            gameManager.getMain().sendScoreboardUpdate(spectator);

                            for (Player p : Bukkit.getOnlinePlayers()) p.showPlayer(spectator);
                        }
                    }

                    if (sm != null && s != null) sm.resetSign(s, map);

                    for (Player player : players) {
                        gameManager.getMain().getListener().resetArmor(player);
                        gameManager.getMain().ResetPlayer(player);
                        BaseClass bc = classes.get(player);
                        bc.GameEnd();
                        SetLobbyScoreboard(player);
                        player.setDisplayName(player.getName());
                        gameManager.getMain().restoreLobbyNameTag(player);
                        gameManager.getMain().sendScoreboardUpdate(player);
                        for (PotionEffect type : player.getActivePotionEffects())
                            player.removePotionEffect(type.getType());
                    }

                    for (BukkitRunnable runnable2 : runnables) runnable2.cancel();

                    // Custom games go straight back to a new custom game lobby (on the default map); others are removed,
                    // which also unloads and deletes the map copy
                    if (!gameManager.restartCustomGame(GameInstance.this)) gameManager.removeGame(GameInstance.this);
                }
                ticks--;
            }
        };
        BukkitTask eg = endGameAnimation.runTaskTimer(getGameManager().getMain(), 0, 20);
        trackInstanceTask(eg);
    }

    public void SetLobbyScoreboard(Player player) {
        gameManager.getMain().getScoreboardManager().lobbyBoard(player);
        gameManager.getMain().restoreLobbyNameTag(player);
        gameManager.getMain().gameStats.put(player, matchStats);
        TextComponent message = new TextComponent(getGameManager().getMain()
                .color("&2&l(!) &eThe match stats have been recorded. &e&lClick here to view!"));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gamestats"));
        player.spigot().sendMessage(message);
    }

    public void givePositionTokens(int position, int tokens) {
        if (playerPosition.size() >= position) {
            Player player = playerPosition.get(playerPosition.size() - position);
            PlayerData data = gameManager.getMain().getDataManager().getPlayerData(player);
            BaseClass baseClass = classes.get(player);

            if (baseClass == null) {
                player.sendMessage("Your base class is fackin null");
            } else if (data != null && savesStats()) {
                data.tokens += tokens;
                baseClass.totalTokens += tokens;
            }
        }
    }

    public void givePositionNum(int position, String message) {
        if (playerPosition.size() >= position) {
            Player player = playerPosition.get(playerPosition.size() - position);
            player.sendMessage("" + ChatColor.BOLD + message);
        }
    }

    public void givePoints(int position, int points) {
        if (playerPosition.size() >= position) {
            Player player = playerPosition.get(playerPosition.size() - position);
            PlayerData data = gameManager.getMain().getDataManager().getPlayerData(player);

            if (data != null && savesStats()) {
                if (gameType != GameType.DUEL) {
                    data.points += points;
                    getGameManager().getMain().tourney.put(player.getName(), data.points);
                }
            }
        }
    }

    private BaseClass matchMvp() {
        BaseClass matchMvp = null;
        for (Entry<Player, BaseClass> entry : this.allClasses.entrySet()) {
            if (entry.getKey() != null) {
                if (matchMvp == null || entry.getValue().totalKills > matchMvp.totalKills)
                    matchMvp = entry.getValue();
                else if (entry.getValue().totalKills == matchMvp.totalKills) {
                    if (this.getWinnerList().contains(entry.getKey())
                            || entry.getValue().totalDeaths < matchMvp.totalDeaths)
                        matchMvp = entry.getValue();
                }
            }
        }
        if (matchMvp.totalKills == 0) matchMvp = null;
        return matchMvp;
    }

    private void checkForMatchMvp() {
        if (savesStats() && matchMvp() != null) {
            for (Entry<Player, BaseClass> entry : this.allClasses.entrySet()) {
                if (entry.getKey() != null) {
                    if (matchMvp() == entry.getValue()) {
                        Player mvp = entry.getKey();
                        PlayerData data = gameManager.getMain().getDataManager().getPlayerData(mvp);
                        if (data != null) data.matchMvps++;
                        return;
                    }
                }
            }
        }
    }

    private void cancelTasks() {
        for (Player p : new ArrayList<>(players)) cancelPerPlayerTasks(p.getUniqueId());
        for (Player p : new ArrayList<>(spectators)) cancelPerPlayerTasks(p.getUniqueId());
    }

    public void WinGame(List<Player> winners) {
        cancelTasks();
        PlayerData data3 = null;
        checkForMatchMvp();
        for (Player winner : winners) {
            data3 = gameManager.getMain().getDataManager().getPlayerData(winner);
            winnerList.add(winner);
            playerPosition.add(winner);
            if (!savesStats()) continue;
            if (data3 != null) {
                BaseClass bc = classes.get(winner);
                int classID = bc.getType().getID();
                ClassDetails details = data3.playerClasses.get(classID);
                if (details == null) {
                    details = new ClassDetails();
                    data3.playerClasses.put(classID, details);
                }
                details.winGame();
            }

            Random r = new Random();
            int chance = r.nextInt(1000);
            if (chance >= 0 && chance < 1) {
                if (data3 != null) {
                    data3.mysteryChests++;
                    winner.sendMessage(getGameManager().getMain().color("&5&l(!) &rYou have found &e1 MysteryChest&r!"));
                }
            }
        }

        if (getGameManager().getMain().tournament == true && savesStats()) {
            givePoints(1, 10);
            givePoints(2, 7);
            givePoints(3, 5);
            givePoints(4, 3);
            givePoints(5, 1);
            getGameManager().getMain().sortTourney();
        }

        for (Player winner : winners) {
            winner.getInventory().clear();
            winner.setGameMode(GameMode.ADVENTURE);
            BaseClass baseClass = classes.get(winner);

            if (!savesStats()) {
                if (baseClass != null) baseClass.placement = 1;
                winner.sendMessage("" + ChatColor.BOLD + "===========================");
                winner.sendMessage("" + ChatColor.BOLD + "||");
                winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD + "  GAME WON");
                winner.sendMessage("" + ChatColor.BOLD + "||");
                winner.sendMessage("" + ChatColor.BOLD + "===========================");
                winner.sendMessage(color("&2&l(!) &rCustom games don't give Tokens, EXP or stats."));
                continue;
            }

            if (baseClass != null) {
                if (baseClass.getLives() < 5) {
                    PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winner);
                    baseClass.totalExp += 113;
                    baseClass.placement = 1;

                    winner.sendMessage("" + ChatColor.BOLD + "===========================");
                    winner.sendMessage("" + ChatColor.BOLD + "||");
                    winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD + "  GAME WON");
                    winner.sendMessage("" + ChatColor.BOLD + "||");
                    winner.sendMessage(ChatColor.BOLD + "||        " + ChatColor.RESET + "   Placed: #1: " + ChatColor.GREEN + "10 Tokens");
                    baseClass.totalTokens += 10;

                    if (baseClass.totalKills >= 0) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  "
                                + baseClass.totalKills + " Kills: " + ChatColor.RESET + ChatColor.GREEN
                                + (baseClass.totalKills * 2) + " Tokens");
                        baseClass.totalTokens += baseClass.totalKills;
                    }

                    if (this.firstBlood == winner) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET
                                + "  First Blood: " + ChatColor.GREEN + "10 Tokens");
                        data3.tokens += 10;
                    }

                    int rankBonus = getGameManager().getMain().getRankBonusTokens(winner, "scb.rankBonusOne");
                    if (rankBonus > 0) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Rank Bonus: "
                                + ChatColor.GREEN + rankBonus + " Tokens");
                        baseClass.totalTokens += rankBonus;
                    }
                    winner.sendMessage("" + ChatColor.BOLD + "||");
                    winner.sendMessage("" + ChatColor.BOLD + "===========================");
                    winner.sendMessage(color("&2&l(!) &rYou earned &a" + baseClass.totalTokens +
                            " &fTokens and &a" + baseClass.totalExp + "&f EXP!"));

                    if (data != null) data.tokens += baseClass.totalTokens;
                } else {
                    PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winner);
                    baseClass.totalTokens += 10;

                    if (data != null) {
                        data.flawlessWins += 1;
                        data.exp += 133;
                    }
                    baseClass.totalExp += 133;
                    baseClass.placement = 1;

                    winner.sendMessage("" + ChatColor.BOLD + "===========================");
                    winner.sendMessage("" + ChatColor.BOLD + "||");
                    winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD + "  GAME WON");
                    winner.sendMessage("" + ChatColor.BOLD + "||");
                    winner.sendMessage(ChatColor.BOLD + "||        " + ChatColor.RESET + "   Placed: #1: " + ChatColor.GREEN + "10 Tokens");

                    if (baseClass.totalKills >= 0) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  " + baseClass.totalKills
                                + " Kills: " + ChatColor.GREEN + (baseClass.totalKills * 2) + " Tokens");
                        baseClass.totalTokens += baseClass.totalKills;
                    }

                    if (this.firstBlood == winner) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET
                                + "  First Blood: " + ChatColor.GREEN + "10 Tokens");
                        data3.tokens += 10;
                    }

                    winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Flawless Win: "
                            + ChatColor.GREEN + "10 Tokens");
                    baseClass.totalTokens += 10;
                    int rankBonus = getGameManager().getMain().getRankBonusTokens(winner, "scb.rankBonus");
                    if (rankBonus > 0) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Rank Bonus: "
                                + ChatColor.GREEN + rankBonus + " Tokens");
                        baseClass.totalTokens += rankBonus;
                    }
                    winner.sendMessage("" + ChatColor.BOLD + "||");
                    winner.sendMessage("" + ChatColor.BOLD + "===========================");
                    winner.getInventory().clear();
                    winner.setGameMode(GameMode.ADVENTURE);

                    if (data != null) {
                        if (data.bonusTokens == 0) {
                            winner.sendMessage(getGameManager().getMain().color(
                                    "&2&l(!) &rYou have completed the &eDouble Tokens &rchallenge! You will recieve double tokens for this game"));
                            data.bonusTokens = 1;
                            baseClass.totalTokens *= 2;
                        }
                    }
                    winner.sendMessage(color("&2&l(!) &rYou earned &a" + baseClass.totalTokens +
                            " &fTokens and &a" + baseClass.totalExp + "&f EXP!"));

                    if (data != null) data.tokens += baseClass.totalTokens;
                }
            }
            PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winner);
            if (data != null) {
                data.wins += 1;
                data.winstreak += 1;
                data.exp += 113;
                gameManager.getMain().giveBattlePassXp(winner,
                        anthony.SuperCraftBrawl.battlepass.BattlePassSeason.XP_WIN);

                if (data.winstreak >= data.bestWinstreak) {
                    data.bestWinstreak = data.winstreak;
                }

                try {
                    if (gameManager.getMain().snapshotDAO != null) {
                        gameManager.getMain().snapshotDAO.recordPeriodWinstreak(
                                winner.getUniqueId().toString(),
                                data.winstreak
                        );
                    }
                } catch (Throwable ignored) {
                }

                getGameManager().getMain().getLevelManager().checkLevelUp(winner);
            }
        }
        for (Player player : players) {
            player.spigot().setCollidesWithEntities(true);
            player.getInventory().clear();
            for (PotionEffect type : player.getActivePotionEffects())
                player.removePotionEffect(type.getType());
        }

        EndGameAnimation(winnerList);
        EndGame();

        BaseClass baseClass = classes.get(winnerList.get(0));
        String tag = gameManager.getMain().getRankManager().getRank(winnerList.get(0)).getTagWithSpace();
        PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winnerList.get(0));

        if (map != null) {
            if (baseClass.getLives() >= 5) {
                if (data != null && savesStats()) {
                    if (getGameManager().getMain().tournament) {
                        if (gameType != GameType.DUEL) data.points += 5;
                    }
                }
                if (winnerList.get(0).hasPermission("scb.customWin")) {
                    if (data.cwm == 1) customFlawWinMsg(winnerList.get(0));
                    else Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust &r&lFLAWLESSLY &rwon on &e&l" + map.toString()));
                } else {
                    Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust &r&lFLAWLESSLY &rwon on &e&l" + map.toString()));
                }
            } else {
                if (winnerList.get(0).hasPermission("scb.customWin")) {
                    if (data.cwm == 1) customWinMsg(winnerList.get(0));
                    else Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust won on &e&l" + map.toString()));
                } else {
                    Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust won on &e&l" + map.toString()));
                }
            }
        } else {
            Player display = winnerList.get(0);
            if (winnerList.size() > 1) {
                BaseClass bc = classes.get(winnerList.get(1));
                if (bc != null) display = (baseClass.getLives() >= bc.getLives()) ? winnerList.get(0) : winnerList.get(1);
            }
            if (baseClass.getLives() >= 5) {
                if (data != null && savesStats()) {
                    if (getGameManager().getMain().tournament) {
                        if (gameType != GameType.DUEL) data.points += 5;
                    }
                }
                if (display.hasPermission("scb.customWin")) {
                    if (data.cwm == 1) customFlawWinMsg(display);
                    else Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
                            + " Team" + ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY "
                            + ChatColor.RESET + "won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW
                            + ChatColor.BOLD + duosMap.toString());
                } else {
                    Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
                            + " Team" + ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY " + ChatColor.RESET
                            + "won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD
                            + duosMap.toString());
                }
            } else {
                if (display.hasPermission("scb.customWin")) {
                    if (data.cwm == 1) customWinMsg(display);
                    else Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
                            + " Team" + ChatColor.WHITE + " just won on " + ChatColor.BOLD + ChatColor.WHITE
                            + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
                } else {
                    Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(display)
                            + " Team" + ChatColor.WHITE + " just won on " + ChatColor.BOLD + ChatColor.WHITE
                            + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
                }
            }
        }

        for (Player p : players) {
            PlayerData data4 = gameManager.getMain().getDataManager().getPlayerData(p);
            this.getGameManager().getMain().getDataManager().saveData(data4);
        }
    }

    private void customWinMsg(Player winner) {
        Random rand = new Random();
        int chance = rand.nextInt(3);

        if (map != null) {
            if (chance == 0) {
                Bukkit.broadcastMessage(
                        color("&2&l(!) &e" + winner.getName() + " &rgot a Victory Royale on &e&l" + map.toString()));
            } else if (chance == 1) {
                Bukkit.broadcastMessage(color("&2&l(!) &e" + winner.getName()
                        + " &rjust showed the entire lobby who's boss on &e&l" + map.toString()));
            } else if (chance == 2) {
                Bukkit.broadcastMessage(
                        color("&2&l(!) &e" + winner.getName() + " &rjust won on &e&l" + map.toString()));
            }
        } else {
            if (chance == 0) {
                Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
                        + ChatColor.WHITE + " got a Victory Royale on " + ChatColor.BOLD + ChatColor.WHITE
                        + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
            } else if (chance == 1) {
                Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
                        + ChatColor.WHITE + " just showed the entire lobby who's boss on " + ChatColor.BOLD
                        + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
            } else if (chance == 2) {
                Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
                        + ChatColor.WHITE + " just won on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW
                        + ChatColor.BOLD + duosMap.toString());
            }
        }
    }

    private void customFlawWinMsg(Player winner) {
        Random rand = new Random();
        int chance = rand.nextInt(4);
        String tag = gameManager.getMain().getRankManager().getRank(winner).getTagWithSpace();

        if (map != null) {
            if (chance == 0) {
                Bukkit.broadcastMessage(color("&2&l(!) &e" + winner.getName()
                        + " &rjust &r&lABSOLUTELY DESTROYED &ron &e&l" + map.toString()));
            } else if (chance == 1) {
                Bukkit.broadcastMessage(
                        color("&2&l(!) &e" + winner.getName() + " &rjust &r&lFLAWLESSLY &ron &e&l" + map.toString()));
            } else if (chance == 2) {
                Bukkit.broadcastMessage(color("&2&l(!) &rThe game on &e&l" + map.toString()
                        + " &rwas too easy for &e" + winner.getName()));
            } else if (chance == 3) {
                Bukkit.broadcastMessage(color("&2&l(!) &rGet &r&lOUTTA THE WAY &rfor &e" + winner.getName()
                        + "&r. They dominated on &e&l" + map.toString()));
            }
        } else {
            if (chance == 0) {
                Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
                        + ChatColor.WHITE + " just " + ChatColor.BOLD + "ABSOLUTELY DESTROYED " + ChatColor.RESET
                        + "everyone on " + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD
                        + duosMap.toString());
            } else if (chance == 1) {
                Bukkit.broadcastMessage("" + ChatColor.BOLD + "(!) " + ChatColor.YELLOW + team.get(winner) + " Team"
                        + ChatColor.WHITE + " just " + ChatColor.BOLD + "FLAWLESSLY " + ChatColor.RESET + "won on "
                        + ChatColor.BOLD + ChatColor.WHITE + ChatColor.YELLOW + ChatColor.BOLD + duosMap.toString());
            } else if (chance == 2) {
                Bukkit.broadcastMessage(this.getGameManager().getMain().color("&r&l(!) &rThe game on &e&l"
                        + duosMap.toString() + " &rwas too easy for " + tag + "&e" + winner.getName()));
            } else if (chance == 3) {
                Bukkit.broadcastMessage(this.getGameManager().getMain().color("&r&l(!) &rGet out of the way for " + tag
                        + "&e" + winner.getName() + ". &rThey &r&lDOMINATED &ron &e&l" + duosMap.toString()));
            }
        }
    }

    private void recordClassPlayed(Player player, ClassType type) {
        classHistory.computeIfAbsent(player.getUniqueId(), new java.util.function.Function<UUID, Set<ClassType>>() {
            @Override
            public Set<ClassType> apply(UUID id) {
                return EnumSet.noneOf(ClassType.class);
            }
        }).add(type);
    }

    /*
     * A random selectable class the player hasn't had yet this match. Once they've been through every one,
     * their history starts over (still avoiding the class they're on right now, so it always changes).
     */
    private ClassType pickUnplayedClass(Player player) {
        ClassType[] selectable = getSelectableClasses();
        Set<ClassType> played = classHistory.get(player.getUniqueId());
        BaseClass current = classes.get(player);

        List<ClassType> options = new ArrayList<>();
        for (ClassType type : selectable) {
            if (played == null || !played.contains(type)) options.add(type);
        }

        if (options.isEmpty()) {
            played.clear();
            for (ClassType type : selectable) {
                if (current == null || type != current.getType() || selectable.length == 1) options.add(type);
            }
        }

        ClassType picked = options.get(random.nextInt(options.size()));
        recordClassPlayed(player, picked);
        return picked;
    }

    private void reRandomizeClass(Player player) {
        BaseClass baseClass = classes.get(player);
        if (baseClass.getLives() > 0) {
            ClassType classType = pickUnplayedClass(player);
            BaseClass newBaseClass = classType.GetClassInstance(this, player);
            BaseClass oldBaseClass = classes.get(player);
            oldClasses.put(player, oldBaseClass);

            if (oldBaseClass.score != null) {
                try { oldBaseClass.score.getScoreboard().resetScores(oldBaseClass.score.getEntry()); }
                catch (Exception e) { e.printStackTrace(); }
            }

            newBaseClass.lives = oldBaseClass.lives;
            newBaseClass.tokens = oldBaseClass.tokens;
            newBaseClass.totalTokens = oldBaseClass.totalTokens;
            newBaseClass.totalExp = oldBaseClass.totalExp;
            newBaseClass.totalKills = oldBaseClass.totalKills;
            newBaseClass.totalDeaths = oldBaseClass.totalDeaths;
            newBaseClass.bountyTarget = oldBaseClass.bountyTarget;

            String scoreEntry = truncateString("" + classType.getTag() + " " + ChatColor.WHITE + player.getName(), 40);
            Score newScore = livesObjective.getScore(scoreEntry);
            newBaseClass.score = newScore;
            newScore.setScore(newBaseClass.lives);

            classes.put(player, newBaseClass);
            allClasses.put(player, newBaseClass);
            sendScoreboardUpdate(player);

            player.sendMessage(color("&2&l(!) &rYour class was randomly selected to " + classType.getTag()));

            if (player.hasPermission("scb.chat"))
                player.setDisplayName("" + player.getName() + " " + classType.getTag());
            else
                player.setDisplayName("" + player.getName() + " " + classType.getTag() + ChatColor.GRAY);
        }
    }

    private void nextClass(Player player) {
        BaseClass baseClass = classes.get(player);

        if (baseClass.gunGamePos < classList.size()) {
            baseClass.gunGamePos++;
            ClassType classType = classList.get(baseClass.gunGamePos);
            BaseClass newBaseClass = classType.GetClassInstance(this, player);
            BaseClass oldBaseClass = classes.get(player);
            oldClasses.put(player, oldBaseClass);

            Score newScore = livesObjective.getScore(
                    truncateString("" + classType.getTag() + " " + ChatColor.WHITE + player.getName() + "", 40));
            newBaseClass.lives = oldBaseClass.lives;
            newBaseClass.tokens = oldBaseClass.tokens;
            newBaseClass.score = newScore;
            newBaseClass.totalTokens = oldBaseClass.totalTokens;
            newBaseClass.totalExp = oldBaseClass.totalExp;
            newBaseClass.totalKills = oldBaseClass.totalKills;
            newBaseClass.bountyTarget = oldBaseClass.bountyTarget;
            newBaseClass.gunGamePos = oldBaseClass.gunGamePos;

            oldBaseClass.score.getScoreboard().resetScores(oldBaseClass.score.getEntry());

            classes.put(player, newBaseClass);
            allClasses.put(player, newBaseClass);
            sendScoreboardUpdate(player);

            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
                    + "Your class has been set to " + classType.getTag());

            if (player.hasPermission("scb.chat"))
                player.setDisplayName("" + player.getName() + " " + classType.getTag());
            else
                player.setDisplayName("" + player.getName() + " " + classType.getTag() + ChatColor.GRAY);
        } else {
            player.sendMessage("game over");
        }
    }

    private void LoadClasses() {
        Random rand = new Random();
        int attempts = 0;

        for (Player player : players) {
            player.getInventory().clear();
            PlayerData playerData = gameManager.getMain().getDataManager().getPlayerData(player);
            // Frenzy and Class Switch give everyone a random class, whatever they picked
            boolean randomClass = gameType == GameType.FRENZY || getGameSettings().randomClasses;
            ClassType selectedClass = !randomClass ? classSelection.get(player) : null;

            ClassType[] selectable = getSelectableClasses();

            // A class the private game leader disabled after it was picked gets re-rolled
            if (selectedClass != null && !isClassSelectable(selectedClass)) selectedClass = null;

            if (gameType == GameType.GUNGAME) selectedClass = classList.get(0);

            if (selectedClass == null) {
                if (!randomClass && this.favClassSelection.contains(player) && !playerData.customIntegers.isEmpty()) {
                    // Only favorites that are selectable in this game
                    List<ClassType> favorites = new ArrayList<>();
                    for (ClassType type : selectable) {
                        if (playerData.customIntegers.contains(type.getID())) favorites.add(type);
                    }
                    if (!favorites.isEmpty()) selectedClass = favorites.get(rand.nextInt(favorites.size()));
                }
                if (selectedClass == null) {
                    selectedClass = selectable[rand.nextInt(selectable.length)];
                    if (!randomClass) {
                        while (attempts <= 500) {
                            attempts++;
                            ClassType classType = selectable[rand.nextInt(selectable.length)];
                            Rank donor = classType.getMinRank();

                            if (ignoresClassRequirements(classType)) {
                                selectedClass = classType;
                                break;
                            }

                            if (playerData.playerClasses.get(classType.getID()) != null
                                    && playerData.playerClasses.get(classType.getID()).purchased
                                    || classType.getTokenCost() == 0) {
                                if (playerData.level >= classType.getLevel()) {
                                    if (classType != ClassType.Fisherman || this.getGameManager().getMain().getFishing()
                                            .hasUnlockedFisherman(player)) {
                                        if (classType != ClassType.Freddy || this.getGameManager().getMain()
                                                .getHalloweenManager().hasUnlockedFreddy(player))
                                            if (donor == null
                                                    || player.hasPermission("scb." + donor.toString().toLowerCase())) {
                                                selectedClass = classType;
                                                break;
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BaseClass baseClass = selectedClass.GetClassInstance(this, player);
            recordClassPlayed(player, selectedClass);

            if (player.hasPermission("scb.chat"))
                player.setDisplayName("" + player.getName() + " " + selectedClass.getTag());
            else
                player.setDisplayName("" + player.getName() + " " + selectedClass.getTag() + ChatColor.GRAY);
            classes.put(player, baseClass);
            allClasses.put(player, baseClass);
            player.setMaxHealth(getMaxHealthPoints());
            player.setHealth(getMaxHealthPoints());
            player.setFoodLevel(20);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            baseClass.loadPlayer();
            this.getGameManager().addSpawnProtection(player);
        }
    }

    public void TellAll(String msg) {
        for (Player player : players) player.sendMessage(ChatColorHelper.color(msg));
    }

    public void TellSpec(String msg) {
        for (Player spectator : spectators) spectator.sendMessage(msg);
    }

    public boolean HasPlayer(Player player) {
        return players.contains(player);
    }

    public boolean HasSpectator(Player spectator) {
        return spectators.contains(spectator);
    }

    // UPDATE TO TAKE IN ACCOUNT FOR DUOS
    public boolean RemovePlayer(Player player) {
        getGameManager().getMain().getScoreboardManager().removeLobbyBoard(player);
        BaseClass baseClass = this.classes.remove(player);
        PlayerData data = this.gameManager.getMain().getDataManager().getPlayerData(player);

        player.setAllowFlight(false);
        player.setAllowFlight(true);
        this.playerPosition.remove(player);
        removeSpectator(player);

        if (this.players.remove(player)) {
            player.setDisplayName(player.getName());
            try {
                if (baseClass != null) {
                    baseClass.score.getScoreboard().resetScores(baseClass.score.getEntry());
                    if (this.state != GameState.ENDED && this.state != GameState.WAITING && data != null
                            && savesStats()) {
                        data.losses++;
                        ClassType type = baseClass.getType();
                        ClassDetails details = data.playerClasses.get(type.getID());
                        if (details == null) {
                            details = new ClassDetails();
                            data.playerClasses.put(type.getID(), details);
                        }
                        details.playGame();
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }

            removeFromDuos(player);

            if (this.state == GameState.WAITING) {
                if (this.map != null) {
                    updateCountOnBoard();
                    TellAll(color("&2&l(!) &f" + player.getName() +
                            "&c left &a(" + this.players.size() + "/" + getMaxPlayers() + ")"));

                    if (checkIfMinPlayers() && this.gameStartTime != null) {
                        this.state = GameState.WAITING;
                        this.gameStartTime.cancel();
                        this.gameStartTime = null;
                        TellAll(color("&c&l(!) &rGame start cancelled, not enough players!"));

                        for (Player gamePlayer : this.players) {
                            // Private games keep the leader's modifiers
                            if (!customGame) this.gameSettings = new GameSettings(this); // reset vars
                            if (gamePlayer.getInventory().contains(Material.PAPER)) {
                                if (gamePlayer.getOpenInventory() != null
                                        && gamePlayer.getOpenInventory().getTitle().contains("Vote"))
                                    gamePlayer.closeInventory();
                                gamePlayer.getInventory().remove(Material.PAPER);
                            }

                            FastBoard fb = this.boards.get(gamePlayer.getUniqueId());
                            updateCountOnBoard();
                            if (fb != null) fb.updateLine(7, getWaitingLine(gamePlayer));
                        }

                        restoreClassSelectors(); // the game type can be voted on / changed again

                        // The leader has to press Start Game again, so give their leader items back
                        if (customGame) {
                            customGameCountdownStarted = false;
                            Player leader = Bukkit.getPlayer(customGameHost);
                            for (Player editor : getEditors()) giveCustomGameHostItems(editor);
                        }
                    }
                }
            } else if (this.state == GameState.STARTED) {
                if (this.map != null) checkIfGameOver();

                TellAll(color("&2&l(!) &e" + player.getName() + " &chas left the game!"));
                data.winstreak = 0;
                try { if (baseClass != null) baseClass.score.getScoreboard().resetScores(baseClass.score.getEntry()); }
                catch (Exception e) { e.printStackTrace(); }

                RemovePlayer(player); // ensure removal
                SetLobbyScoreboard(player);
                getGameManager().getMain().sendScoreboardUpdate(player);

                if (baseClass != null && baseClass.getType() == ClassType.LargeFern) {
                    LargeFernClass largeFernClass = (LargeFernClass) baseClass;
                    if (largeFernClass.transfernRunnable != null) largeFernClass.transfernRunnable.cancel();
                } else if (baseClass != null && baseClass.getType() == ClassType.Parrot) {
                    ParrotClass parrotClass = (ParrotClass) baseClass;
                    if (parrotClass.isDanceAbilityActive()) parrotClass.cleanupDanceAbility();
                } else if (baseClass != null && baseClass.getType() == ClassType.Pig) {
                    player.removePotionEffect(PotionEffectType.SPEED);
                }
            }

            if (this.s != null) {
                if (this.map != null) {
                    this.s.setLine(2, getGameManager().getMain().color("&0Players: " + this.players.size() + "/"
                            + getMaxPlayerCount()));
                } else {
                    this.s.setLine(2, getGameManager().getMain().color("&0Players: " + this.players.size() + "/6"));
                }
                this.s.update();
            }
            return true;
        }
        return false;
    }

    private void checkIfGameOver() {
        if (cancellingCustomGame) return;
        int alivePlayers = 0;
        Player alivePlayer = null;
        List<Player> lastAlive = new ArrayList<>();
        for (Map.Entry<Player, BaseClass> entry : this.classes.entrySet()) {
            if (entry.getValue().getLives() > 0) {
                alivePlayers++;
                alivePlayer = entry.getKey();
                lastAlive.add(alivePlayer);
            }
        }

        if (alivePlayers == 1 && alivePlayer != null && lastAlive.size() == 1)
            WinGame(lastAlive);
        else if (alivePlayers <= 0)
            EndGame();
    }

    public boolean checkIfMinPlayers() {
        return this.players.size() < 2;
    }

    private void updateCountOnBoard() {
        for (Player gamePlayer : this.players) {
            FastBoard board = this.boards.get(gamePlayer.getUniqueId());
            if (board != null) {
                board.updateLine(5, color("Players: &e"
                        + (getMap().GetInstance().gameType == GameType.FRENZY ? "" + ChatColor.YELLOW + players.size() + "/" + getMaxPlayerCount() : "")
                        + (getMap().GetInstance().gameType == GameType.CLASSIC ? "" + ChatColor.YELLOW + players.size() + "/" + getMaxPlayerCount() : "")
                        + (getMap().GetInstance().gameType == GameType.DUEL ? "" + ChatColor.YELLOW + players.size() + "/" + getMaxPlayerCount() : "")));
            }
        }
    }

    private void removeFromDuos(Player player) {
        if (this.duosMap != null) {
            if (this.redTeam.contains(player)) this.redTeam.remove(player);
            else if (this.blueTeam.contains(player)) this.blueTeam.remove(player);
            else if (this.blackTeam.contains(player)) this.blackTeam.remove(player);
            this.team.remove(player);
        }
    }

    private void removeSpectator(Player spec) {
        if (this.spectators.contains(spec)) {
            this.spectators.remove(spec);
            spec.setDisplayName("" + spec.getName());
        }
    }

    public boolean PlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (players.contains(player)) {
            if (classes.containsKey(player)) {
                BaseClass baseClass = classes.get(player);
                if (event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL
                        && event.getPlayer().getItemInHand().hasItemMeta()
                        && event.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Teleporter")
                        && (event.getAction() == Action.RIGHT_CLICK_AIR
                        || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

                    // Enderman has no pearl cooldown.
                    // Other classes, like EnderDragon, still use the normal pearlTimer cooldown.
                    if (baseClass.getType() != ClassType.Enderman) {
                        if (baseClass.pearlTimer.getTime() < 10000) {
                            int seconds = (10000 - baseClass.pearlTimer.getTime()) / 1000 + 1;
                            event.setCancelled(true);
                            player.sendMessage(color("&c&l(!) &rYour ender pearl is still on cooldown for &a" +
                                    seconds + "s"));
                        } else {
                            baseClass.pearlTimer.restart();
                        }
                    }
                }
                baseClass.useItem(event);
            }
            return true;
        }
        return false;
    }

    public Player getNearestPlayer(Player player, LivingEntity entity, double distance) {
        Player target = null;
        double closestDistance = distance;
        for (Player p : this.players) {
            if (p != player) {
                BaseClass baseClass = classes.get(p);
                if (!baseClass.checkIfDead(p, this)) {
                    if (target == null) {
                        if (p.getLocation().distance(entity.getLocation()) <= distance) {
                            target = p;
                            closestDistance = p.getLocation().distance(entity.getLocation());
                        }
                    } else {
                        if (p.getLocation().distance(entity.getLocation()) < closestDistance) target = p;
                    }
                }
            }
        }
        return target;
    }

    public boolean hasPlayerMovedPosition(Player player) {
        UUID playerId = player.getUniqueId();
        Location lastLocation = lastKnownLocations.get(playerId);

        if (lastLocation != null) {
            Location currentLocation = player.getLocation();
            if (lastLocation.getBlockX() != currentLocation.getBlockX()
                    || lastLocation.getBlockY() != currentLocation.getBlockY()
                    || lastLocation.getBlockZ() != currentLocation.getBlockZ()) {
                lastKnownLocations.put(playerId, currentLocation);
                return true;
            }
        } else {
            lastKnownLocations.put(playerId, player.getLocation());
        }
        return false;
    }

    public void clearLastPosition(Player player) {
        UUID playerId = player.getUniqueId();
        lastKnownLocations.remove(playerId);
    }

    public boolean isNotWaterOrLava(Material material) {
        return material != Material.WATER && material != Material.STATIONARY_WATER && material != Material.LAVA
                && material != Material.STATIONARY_LAVA;
    }

    public List<ClassType> generateClassList() {
        List<ClassType> classes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ClassType[] selectable = getSelectableClasses();
            ClassType classType = selectable[random.nextInt(selectable.length)];
            classes.add(classType);
        }
        return classes;
    }

    public List<ItemStack> getAllItemDrops() {
        return allItemDrops;
    }

    public int getLightningDropRemainingTime() {
        return lightningDropCountdown;
    }

    public ItemStack getNextItemToDrop() {
        return nextItemToDrop;
    }

    public List<Player> getWinnerList() {
        return winnerList;
    }

    public String color(String c) {
        return ChatColor.translateAlternateColorCodes('&', c);
    }

    public void resetState() {
        for (Player player : this.players) {
            player.setFireTicks(0);
            player.setLevel(0);
            player.setGameMode(GameMode.ADVENTURE);
            for (PotionEffect type : player.getActivePotionEffects())
                player.removePotionEffect(type.getType());
        }
    }

    // ✅ HARDENED CLEANUP: cancels everything & drops strong refs
    public void teardown() {
        cancelAllTasks();

        // Delete any FastBoards you created per-player
        for (FastBoard b : new ArrayList<>(boards.values())) {
            try { b.delete(); } catch (Throwable ignored) {}
        }
        boards.clear();

        // Clear transient state collections
        playerPosition.clear();
        allItemDrops.clear();
        favClassSelection.clear();
        classHistory.clear();
        lastKnownLocations.clear();

        // Do NOT null out players/classes here (other parts may still read stats),
        // but you can if you're about to discard the instance entirely.
        // players.clear(); spectators.clear(); classes.clear(); etc.
    }

    /** Stops everything this game still has scheduled (e.g. before its world is unloaded). */
    private void cancelAllTasks() {
        // Cancel scheduled instance tasks (safe even if already cancelled)
        for (BukkitTask t : new ArrayList<>(instanceTasks)) {
            try { t.cancel(); } catch (Throwable ignored) {}
        }
        instanceTasks.clear();
        for (BukkitTask t : new ArrayList<>(classTasks)) {
            try { t.cancel(); } catch (Throwable ignored) {}
        }
        classTasks.clear();

        // Cancel start/end runnables if still alive
        try { if (gameStartTime != null) { gameStartTime.cancel(); gameStartTime = null; } } catch (Throwable ignored) {}
        try { if (endGameAnimation != null) { endGameAnimation.cancel(); endGameAnimation = null; } } catch (Throwable ignored) {}

        // Cancel any raw runnables you kept for reference
        for (BukkitRunnable br : new ArrayList<>(runnables)) {
            try { br.cancel(); } catch (Throwable ignored) {}
        }
        runnables.clear();

        // Cancel and forget per-player tasks
        for (UUID id : new ArrayList<>(perPlayerTasks.keySet())) cancelPerPlayerTasks(id);
        perPlayerTasks.clear();
    }

    private void clearForPlayer(Player p) {
        cancelPerPlayerTasks(p.getUniqueId());
        FastBoard b = boards.remove(p.getUniqueId());
        if (b != null) {
            try { b.delete(); } catch (Throwable ignored) {}
        }
    }

    public void forceRemovePlayer(Player p) {
        players.remove(p);
        spectators.remove(p);
        clearForPlayer(p);
    }

    /*
     * For a repeating task of a class ability or game event: if it's still going once this game is removed, it's cancelled then,
     * so it can't run on in an unloaded world (or keep that world in memory). Returns the task.
     */
    public BukkitTask trackClassTask(BukkitTask t) {
        if (t == null) return null;
        if (worldDiscarded) {
            t.cancel();
            return t;
        }

        // Forget ones that already stopped, so the list doesn't grow with every ability used
        for (Iterator<BukkitTask> it = classTasks.iterator(); it.hasNext(); ) {
            int id = it.next().getTaskId();
            if (!Bukkit.getScheduler().isQueued(id) && !Bukkit.getScheduler().isCurrentlyRunning(id)) it.remove();
        }
        classTasks.add(t);
        return t;
    }

    private void trackInstanceTask(BukkitTask t) {
        if (t != null) instanceTasks.add(t);
    }

    private void trackPerPlayerTask(UUID id, BukkitTask t) {
        if (id == null || t == null) return;
        perPlayerTasks.computeIfAbsent(id, k -> new ArrayList<>()).add(t);
        instanceTasks.add(t); // also track globally
    }

    private void cancelPerPlayerTasks(UUID id) {
        List<BukkitTask> list = perPlayerTasks.remove(id);
        if (list != null) {
            for (BukkitTask t : list) {
                try { t.cancel(); } catch (Throwable ignored) {}
            }
        }
    }
}