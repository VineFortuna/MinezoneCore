package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.all.LargeFernClass;
import anthony.SuperCraftBrawl.Game.classes.all.ParrotClass;
import anthony.SuperCraftBrawl.Game.map.DuosMaps;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.PlayerListener;
import anthony.SuperCraftBrawl.Timer;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.SuperCraftBrawl.signs.SignManager;
import anthony.SuperCraftBrawl.titles.TitleUtil;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import anthony.util.ItemHelper;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
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

    // ✅ FIXED: Use UUID keys to avoid pinning Player (and the whole world graph)
    public Map<UUID, FastBoard> boards = new HashMap<>();

    public final HashMap<Player, ClassType> classSelection = new HashMap<>();
    public HashMap<Player, Timer> cooldowns = new HashMap<Player, Timer>();
    private final List<Player> winnerList;
    public BukkitRunnable gameStartTime;
    public int timeToStartSeconds = 30;
    List<BukkitRunnable> runnables = new ArrayList<>();
    public int blindness = 0;
    public ItemStack votePaper = ItemHelper.setDetails(new ItemStack(Material.PAPER),
            "" + ChatColor.YELLOW + ChatColor.BOLD + "Vote");
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
    public List<ClassType> classList = generateClassList();
    private SignManager sm;

    // ✅ FIXED: Track all tasks to cancel reliably
    private final List<BukkitTask> instanceTasks = new ArrayList<>();
    // ✅ FIXED: Per-player tasks keyed by UUID (not Player)
    private final Map<UUID, List<BukkitTask>> perPlayerTasks = new HashMap<>();

    // DUEL COMMAND
    public boolean isDuel = false;

    // Constructors:
    public GameInstance(GameManager gameManager, Maps map) {
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
        initialiseMap();
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
     * This function initializes the world of the game map when a player joins. If
     * the map doesn't exist in the files, it'll create a brand new void world
     */
    public void initialiseMap() {
        WorldCreator w = null;
        if (map != null)
            w = new WorldCreator(map.GetInstance().worldName).environment(World.Environment.NORMAL);
        else
            w = new WorldCreator(duosMap.GetInstance().worldName).environment(World.Environment.NORMAL);
        w.generator(new VoidGenerator());
        mapWorld = Bukkit.getServer().createWorld(w);
        mapWorld.setAutoSave(false);

        if (getMap() != Maps.WitchesBrew)
            mapWorld.setTime(1000);
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
        return state == GameState.WAITING && this.players.size() < gameType.getMaxPlayers();
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
                player.sendMessage(color("&2&l(!) &rYou have joined &r&l" + map.toString()));
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

                    gamePlayer.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + player.getName() + ChatColor.GREEN + " joined " + ChatColor.RED + "(" + ChatColor.GREEN
                            + (map.GetInstance().gameType == GameType.FRENZY ? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers() : "")
                            + (map.GetInstance().gameType == GameType.CLASSIC ? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers() : "")
                            + (map.GetInstance().gameType == GameType.DUEL ? "" + ChatColor.RESET + players.size() + "/" + gameType.getMaxPlayers() : "")
                            + ChatColor.RED + ")");
                }

                return GameReason.SUCCESS;
            } else return GameReason.ALREADY_IN;
        } else return GameReason.ALREADYPLAYING;
    }

    private boolean isLobbyFull(Player player) {
        if (this.map != null) {
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
        if (players.size() == 2)
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
                    removeVotePaper();
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

        if (gameType == GameType.FRENZY) {
            String msg = color("&2&l(!) &a&lA &7&oFrenzy &a&lgame on&f&l " + mapName + " &a&lis starting in 30 seconds");
            broadcastToWorld(lobby, msg, join);
        } else if (gameType == GameType.CLASSIC) {
            String msg = color("&2&l(!) &a&lA game on&f&l " + mapName + " &a&lis starting in 30 seconds");
            broadcastToWorld(lobby, msg, join);
        }

        TellAll(color("&2&l(!) &r30 seconds to game start!"));

        for (Player player : players)
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
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

        TellAll(color("&e&l----------------------------------------"));
        TellAll("" + ChatColor.AQUA + ChatColor.BOLD + "          Super Craft Brothers");
        TellAll("");
        TellAll(color("&r  5 lives each with different classes & unique"));
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
        resetState();
        LoadClasses();
        GameScoreboard();
        addAlivePlayers();
        addAliveTeams();
        giveRandomItemDrop();
        initialSpawn();
        gameTicks();

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player gamePlayer : players) sendScoreboardUpdate(gamePlayer);
            }
        };
        BukkitTask task = r.runTaskLater(getGameManager().getMain(), 20);
        trackInstanceTask(task);
    }

    private void gameTicks() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (gameTicks % (60 * 20) == 0) {
                    time.getScoreboard().resetScores(time.getEntry());
                    time = o.getScore(color("&eGame Time: &f" + gameTime + "m"));
                    time.setScore(0);
                    gameTime++;
                }

                for (Entry<Player, BaseClass> playerClass : classes.entrySet())
                    playerClass.getValue().Tick(gameTicks);

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

        String teamName = player.getName();
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
        GameLootDrops lootDrop = GameLootDrops.getDrop();
        return lootDrop.getItem();
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
                    livesScore.setScore(5);
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
                Score game = o.getScore(color("&eGame Mode: &r&o" + this.gameType.getName()));
                game.setScore(0);
                time = o.getScore(color("&eGame Time: &r" + gameTime + "m"));
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
            livesScore.setScore(5);
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
        PlayerDeathEvent event = new PlayerDeathEvent(player, null, 0, null);
        baseClassBeforeChecks.isDead = true;
        baseClassBeforeChecks.Death(event);
        final BaseClass baseClass = this.classes.get(player);
        if (baseClass == null) return;

        try {
            player.getInventory().clear();
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(GetSpecLoc());

            if (this.gameType == GameType.FRENZY && baseClass.getLives() > 0) reRandomizeClass(player);
            else if (this.gameType == GameType.GUNGAME && baseClass.getLives() > 0) nextClass(player);
            final BaseClass finalBaseClass = this.classes.get(player);

            BukkitRunnable runTimer = new BukkitRunnable() {
                int ticks = 3;
                @SuppressWarnings("deprecation")
                public void run() {
                    if (GameInstance.this.state == GameState.ENDED) {
                        cancel();
                    }
                    if (finalBaseClass.getLives() > 0)
                        if (this.ticks == 0) {
                            if (state != GameState.STARTED || !players.contains(player) || getWinnerList().contains(player)) {
                                cancel();
                                player.setAllowFlight(false);
                                player.setAllowFlight(true);
                                return;
                            }
                            player.teleport(GameInstance.this.GetRespawnLoc());
                            player.setGameMode(GameMode.ADVENTURE);
                            player.setHealth(20.0D);
                            player.setAllowFlight(true);
                            GameInstance.this.getGameManager().addSpawnProtection(player);
                            if (!GameInstance.this.players.contains(player)) {
                                GameInstance.this.getGameManager().getMain().ResetPlayer(player);
                            } else {
                                finalBaseClass.loadPlayer();
                                if (GameInstance.this.gameType == GameType.FRENZY
                                        || GameInstance.this.gameType == GameType.GUNGAME) {
                                    TitleUtil.sendTitle(player, "&eNew Class", "" + finalBaseClass.getType().getTag(), 0, 30, 10);
                                } else {
                                    TitleUtil.sendTitle(player, "&eRespawned", "", 0, 30, 10);
                                }
                                finalBaseClass.isDead = false;
                            }
                            cancel();
                        } else if (this.ticks <= 3 && GameInstance.this.state == GameState.STARTED) {
                            if (!players.contains(player)) {
                                cancel();
                            } else {
                                TitleUtil.sendTitle(player, "&eRespawning In", "&c" + this.ticks, 0, 20, 0);
                                player.setGameMode(GameMode.SPECTATOR);
                            }
                        }
                    this.ticks--;
                }
            };
            BukkitTask task = runTimer.runTaskTimer(this.gameManager.getMain(), 0L, 20L);
            trackPerPlayerTask(player.getUniqueId(), task);
            this.runnables.add(runTimer);

            player.setHealth(20.0D);
            player.setAllowFlight(true);
            player.setGameMode(GameMode.ADVENTURE);
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
                            gameManager.getMain().sendScoreboardUpdate(spectator);

                            for (Player p : Bukkit.getOnlinePlayers()) p.showPlayer(spectator);
                        }
                    }

                    if (sm != null && s != null) sm.resetSign(s, map);

                    for (Player player : players) {
                        gameManager.getMain().ResetPlayer(player);
                        BaseClass bc = classes.get(player);
                        bc.GameEnd();
                        SetLobbyScoreboard(player);
                        gameManager.getMain().sendScoreboardUpdate(player);
                        for (PotionEffect type : player.getActivePotionEffects())
                            player.removePotionEffect(type.getType());
                        gameManager.getMain().getListener().resetArmor(player);
                    }

                    for (BukkitRunnable runnable2 : runnables) runnable2.cancel();

                    BukkitRunnable r = new BukkitRunnable() {
                        @Override
                        public void run() {
                            System.out.println("Unloading world");
                            if (Bukkit.unloadWorld(mapWorld, false)) {
                                System.out.println("World unloaded: " + mapWorld.getName());
                                this.cancel();
                            }
                        }
                    };
                    BukkitTask task = r.runTaskTimer(getGameManager().getMain(), 0, 1);
                    trackInstanceTask(task);

                    if (map != null) gameManager.RemoveMap(map);
                    else gameManager.RemoveDuosMap(duosMap);
                }
                ticks--;
            }
        };
        BukkitTask eg = endGameAnimation.runTaskTimer(getGameManager().getMain(), 0, 20);
        trackInstanceTask(eg);
    }

    public void SetLobbyScoreboard(Player player) {
        gameManager.getMain().getScoreboardManager().lobbyBoard(player);
        gameManager.getMain().gameStats.put(player, this);
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
            } else if (data != null) {
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

            if (data != null) {
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
        if (matchMvp() != null) {
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
            if (data3 != null) {
                BaseClass bc = classes.get(winner);
                int classID = bc.getType().getID();
                ClassDetails details = data3.playerClasses.get(classID);
                if (details == null) {
                    details = new ClassDetails();
                    data3.playerClasses.put(classID, details);
                }
                details.winGame();
                if (data3.challenge1 == 0) {
                    if (bc != null) {
                        if (bc.getType() == ClassType.Pig) {
                            winner.sendMessage(getGameManager().getMain()
                                    .color("&9&l(!) &rYou got a win with " + bc.getType().getTag()
                                            + " &rand have now unlocked the " + ClassType.Notch.getTag()
                                            + " &rclass!"));
                            data3.challenge1 = 1;
                            classID = 29;
                            ClassDetails notchdetails = data3.playerClasses.get(classID);

                            if (notchdetails == null) {
                                notchdetails = new ClassDetails();
                                data3.playerClasses.put(classID, notchdetails);
                            }
                            notchdetails.setPurchased();
                        }
                    }
                }
                if (data3.challenge2 == 0) {
                    winner.sendMessage(getGameManager().getMain()
                            .color("&9&l(!) &rYou got a win and you are now rewarded with &e50 Bonus Tokens"));
                    data3.challenge2 = 1;

                    if (bc != null)
                        bc.totalTokens += 50;
                }
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

        if (getGameManager().getMain().tournament == true) {
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

                    if (winner.hasPermission("scb.rankBonusOne")) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Rank Bonus: "
                                + ChatColor.GREEN + "10 Tokens");
                        baseClass.totalTokens += 10;
                    }
                    winner.sendMessage("" + ChatColor.BOLD + "||");
                    winner.sendMessage("" + ChatColor.BOLD + "===========================");
                    winner.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have earned "
                            + ChatColor.GREEN + baseClass.totalTokens + " Tokens!");

                    if (data != null) data.tokens += baseClass.totalTokens;
                } else {
                    PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winner);
                    baseClass.totalTokens += 10;

                    if (data != null) {
                        data.flawlessWins += 1;
                        data.exp += 133;
                    }
                    baseClass.totalExp += 133;

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
                    if (winner.hasPermission("scb.rankBonus")) {
                        winner.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.RESET + "  Rank Bonus: "
                                + ChatColor.GREEN + "10 Tokens");
                        baseClass.totalTokens += 10;
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
                    winner.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have earned "
                            + ChatColor.GREEN + baseClass.totalTokens + " Tokens!");

                    if (data != null) data.tokens += baseClass.totalTokens;
                }
            }
            PlayerData data = gameManager.getMain().getDataManager().getPlayerData(winner);
            if (data != null) {
                data.wins += 1;
                data.winstreak += 1;
                data.exp += 113;

                if (data.winstreak >= data.bestWinstreak) data.bestWinstreak = data.winstreak;

                if (data.exp >= 2500) {
                    data.level++;
                    data.exp -= 2500;
                    winner.sendMessage(getGameManager().getMain().color("&8&m----------------------------------------"));
                    winner.sendMessage(getGameManager().getMain().color("&6&l✦✦ &e&lLEVEL UP! &6&l✦✦"));
                    winner.sendMessage(getGameManager().getMain()
                            .color("&7You are now &e&lLevel &6&l" + data.level + " &7— nice work!"));
                    winner.sendMessage(getGameManager().getMain().color("&8&m----------------------------------------"));
                    winner.playSound(winner.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.15f);
                }
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
                if (data != null) {
                    if (getGameManager().getMain().tournament) {
                        if (gameType != GameType.DUEL) data.points += 5;
                    }
                }
                if (winnerList.get(0).hasPermission("scb.customWin")) {
                    if (data.cwm == 1) customFlawWinMsg(winnerList.get(0));
                    else Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust &r&lFLAWLESSLY &rwon on &b&l" + map.toString()));
                } else {
                    Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust &r&lFLAWLESSLY &rwon on &b&l" + map.toString()));
                }
            } else {
                if (winnerList.get(0).hasPermission("scb.customWin")) {
                    if (data.cwm == 1) customWinMsg(winnerList.get(0));
                    else Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust won on &b&l" + map.toString()));
                } else {
                    Bukkit.broadcastMessage(color("&2&l(!) &e" + winnerList.get(0).getName()
                            + " &rjust won on &b&l" + map.toString()));
                }
            }
        } else {
            Player display = winnerList.get(0);
            if (winnerList.size() > 1) {
                BaseClass bc = classes.get(winnerList.get(1));
                if (bc != null) display = (baseClass.getLives() >= bc.getLives()) ? winnerList.get(0) : winnerList.get(1);
            }
            if (baseClass.getLives() >= 5) {
                if (data != null) {
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
                        color("&2&l(!) &e" + winner.getName() + " &rgot a Victory Royale on &b&l" + map.toString()));
            } else if (chance == 1) {
                Bukkit.broadcastMessage(color("&2&l(!) &e" + winner.getName()
                        + " &rjust showed the entire lobby who's boss on &b&l" + map.toString()));
            } else if (chance == 2) {
                Bukkit.broadcastMessage(
                        color("&2&l(!) &e" + winner.getName() + " &rjust won on &b&l" + map.toString()));
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
                        + " &rjust &r&lABSOLUTELY DESTROYED &ron &b&l" + map.toString()));
            } else if (chance == 1) {
                Bukkit.broadcastMessage(
                        color("&2&l(!) &e" + winner.getName() + " &rjust &r&lFLAWLESSLY &ron &b&l" + map.toString()));
            } else if (chance == 2) {
                Bukkit.broadcastMessage(color("&2&l(!) &rThe game on &b&l" + map.toString()
                        + " &rwas too easy for &e" + winner.getName()));
            } else if (chance == 3) {
                Bukkit.broadcastMessage(color("&2&l(!) &rGet &r&lOUTTA THE WAY &rfor &e" + winner.getName()
                        + "&r. They dominated on &b&l" + map.toString()));
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

    private void reRandomizeClass(Player player) {
        BaseClass baseClass = classes.get(player);
        if (baseClass.getLives() > 0) {
            ClassType classType = ClassType.getAvailableClasses()[random
                    .nextInt(ClassType.getAvailableClasses().length)];
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
            newBaseClass.bountyTarget = oldBaseClass.bountyTarget;

            String scoreEntry = truncateString("" + classType.getTag() + " " + ChatColor.WHITE + player.getName(), 40);
            Score newScore = livesObjective.getScore(scoreEntry);
            newBaseClass.score = newScore;
            newScore.setScore(newBaseClass.lives);

            classes.put(player, newBaseClass);
            allClasses.put(player, newBaseClass);
            sendScoreboardUpdate(player);

            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
                    + "Your class has been randomly selected to " + classType.getTag());

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
            ClassType selectedClass = gameType != GameType.FRENZY ? classSelection.get(player) : null;

            if (gameType == GameType.GUNGAME) selectedClass = classList.get(0);

            if (selectedClass == null) {
                if (this.favClassSelection.contains(player) && !playerData.customIntegers.isEmpty()) {
                    if (playerData != null) {
                        int randomIndex = rand.nextInt(playerData.customIntegers.size());
                        int randValue = playerData.customIntegers.get(randomIndex);
                        for (ClassType type : ClassType.getAvailableClasses()) {
                            if (type.getID() == randValue) { selectedClass = type; break; }
                        }
                    }
                } else {
                    selectedClass = ClassType.getAvailableClasses()[rand
                            .nextInt(ClassType.getAvailableClasses().length)];
                    if (gameType != GameType.FRENZY) {
                        while (attempts <= 500) {
                            attempts++;
                            ClassType classType = ClassType.getAvailableClasses()[rand
                                    .nextInt(ClassType.getAvailableClasses().length)];
                            Rank donor = classType.getMinRank();

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

            if (player.hasPermission("scb.chat"))
                player.setDisplayName("" + player.getName() + " " + selectedClass.getTag());
            else
                player.setDisplayName("" + player.getName() + " " + selectedClass.getTag() + ChatColor.GRAY);
            classes.put(player, baseClass);
            allClasses.put(player, baseClass);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            baseClass.loadPlayer();
            this.getGameManager().addSpawnProtection(player);
        }
    }

    public void TellAll(String msg) {
        for (Player player : players) player.sendMessage(msg);
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
                    if (this.state != GameState.ENDED && this.state != GameState.WAITING && data != null) {
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
                    TellAll("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET + player.getName()
                            + ChatColor.RED + " left " + ChatColor.RED + "(" + ChatColor.GREEN + (
                            ((this.map.GetInstance()).gameType == GameType.FRENZY) ? ("" + ChatColor.RESET + this.players.size() + "/" + this.gameType.getMaxPlayers()) : "")
                            + (((this.map.GetInstance()).gameType == GameType.CLASSIC) ? ("" + ChatColor.RESET + this.players.size() + "/" + this.gameType.getMaxPlayers()) : "")
                            + (((this.map.GetInstance()).gameType == GameType.DUEL) ? ("" + ChatColor.RESET + this.players.size() + "/" + this.gameType.getMaxPlayers()) : "")
                            + ChatColor.RED + ")");

                    if (checkIfMinPlayers() && this.gameStartTime != null) {
                        this.state = GameState.WAITING;
                        this.gameStartTime.cancel();
                        this.gameStartTime = null;
                        TellAll(color("&c&l(!) &rGame start cancelled, not enough players!"));

                        for (Player gamePlayer : this.players) {
                            this.gameSettings = new GameSettings(this); // reset vars
                            if (gamePlayer.getInventory().contains(Material.PAPER)) {
                                if (gamePlayer.getOpenInventory() != null
                                        && gamePlayer.getOpenInventory().getTitle().contains("Vote"))
                                    gamePlayer.closeInventory();
                                gamePlayer.getInventory().remove(Material.PAPER);
                            }

                            FastBoard fb = this.boards.get(gamePlayer.getUniqueId());
                            updateCountOnBoard();
                            if (fb != null) fb.updateLine(7, color("&7&oWaiting for &a1 &7&oplayer"));
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
                            + (getMap().GetInstance()).gameType.getMaxPlayers()));
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
                        + (getMap().GetInstance().gameType == GameType.FRENZY ? "" + ChatColor.YELLOW + players.size() + "/" + gameType.getMaxPlayers() : "")
                        + (getMap().GetInstance().gameType == GameType.CLASSIC ? "" + ChatColor.YELLOW + players.size() + "/" + gameType.getMaxPlayers() : "")
                        + (getMap().GetInstance().gameType == GameType.DUEL ? "" + ChatColor.YELLOW + players.size() + "/" + gameType.getMaxPlayers() : "")));
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
                        && event.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Teleporters")
                        && (event.getAction() == Action.RIGHT_CLICK_AIR
                        || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    if (baseClass.pearlTimer.getTime() < 10000) {
                        int seconds = (10000 - baseClass.pearlTimer.getTime()) / 1000 + 1;
                        event.setCancelled(true);
                        player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You have to wait " + ChatColor.YELLOW
                                + seconds + " seconds " + ChatColor.RESET + "to use this item again");
                    } else baseClass.pearlTimer.restart();
                }
                baseClass.UseItem(event);
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
            ClassType classType = ClassType.getAvailableClasses()[random
                    .nextInt(ClassType.getAvailableClasses().length)];
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
        // Cancel scheduled instance tasks (safe even if already cancelled)
        for (BukkitTask t : new ArrayList<>(instanceTasks)) {
            try { t.cancel(); } catch (Throwable ignored) {}
        }
        instanceTasks.clear();

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

        // Delete any FastBoards you created per-player
        for (FastBoard b : new ArrayList<>(boards.values())) {
            try { b.delete(); } catch (Throwable ignored) {}
        }
        boards.clear();

        // Clear transient state collections
        playerPosition.clear();
        allItemDrops.clear();
        favClassSelection.clear();
        lastKnownLocations.clear();

        // Do NOT null out players/classes here (other parts may still read stats),
        // but you can if you're about to discard the instance entirely.
        // players.clear(); spectators.clear(); classes.clear(); etc.
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