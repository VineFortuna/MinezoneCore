package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * Every game plays on its own copy of the map's world (e.g. nightdragon_Copy_1), so the same map can have
 * several games going at once and the original world folder is never touched by a game. Copies are deleted
 * when their game is over, when the server stops, and on startup (in case the server crashed).
 *
 * The original worlds are only loaded through /scbsetup, to make changes to a map. Changes there are only
 * written to disk by /scbsetup save - never automatically, on leaving, or when the server stops.
 */
public class MapWorldManager implements CommandExecutor, TabCompleter, Listener {

    private static final String COPY_SUFFIX = "_Copy_";
    private static final String CUSTOM_GAME_SUFFIX = "_CustomGame";
    // <player name>_CustomGame (or _CustomGame_2, ...) - a custom game's copy, named after its host
    private static final Pattern CUSTOM_GAME_COPY = Pattern.compile("[A-Za-z0-9_]{1,16}" + CUSTOM_GAME_SUFFIX + "(_\\d+)?",
            Pattern.CASE_INSENSITIVE);
    // Copied worlds keep everything except these: a copied uid.dat makes Bukkit refuse to load the world
    // as a duplicate, and session.lock belongs to whichever server has the original open
    private static final Set<String> SKIPPED_FILES = new HashSet<>(Arrays.asList("uid.dat", "session.lock"));
    private static final int UNLOAD_TRIES_BEFORE_KICKING = 100; // ticks
    private static final int UNLOAD_TRIES_BEFORE_WARNING = 20 * 30;

    // Put in the folder of a map made by /scbsetup create until it is saved, so a world that was never saved
    // (the server stopped, or crashed, while it was being set up) is known to be one that can be thrown away
    private static final String UNSAVED_MARKER = ".scb-unsaved-map";

    private final Core main;
    private final MapSetupManager setup;
    private final Set<String> setupWorlds = new HashSet<>(); // original worlds loaded by /scbsetup (lowercase)
    private final Set<String> unsavedNewWorlds = new HashSet<>(); // new maps' worlds that haven't been saved yet (lowercase)

    public MapWorldManager(Core main) {
        this.main = main;
        this.setup = new MapSetupManager(main, this);
    }

    /** The items and menus for setting a map up. Core registers it as a listener. */
    public MapSetupManager getSetupManager() {
        return setup;
    }

    // ===== Game copies =====

    /** Copies the map's world folder to a new numbered copy and loads it. */
    public World createGameWorld(String worldName) {
        return copyAndLoad(worldName, getFreeCopyName(Bukkit.getWorldContainer(), worldName));
    }

    /*
     * A custom game's copy of the map, named <host>_CustomGame. While the host's previous one is still around
     * (changing map, the next round, or one that's still being deleted) it's <host>_CustomGame_2, _3 and so on.
     */
    public World createCustomGameWorld(String worldName, String hostName) {
        File container = Bukkit.getWorldContainer();
        String copyName = hostName + CUSTOM_GAME_SUFFIX;
        for (int i = 2; Bukkit.getWorld(copyName) != null || new File(container, copyName).exists(); i++) {
            copyName = hostName + CUSTOM_GAME_SUFFIX + "_" + i;
        }
        return copyAndLoad(worldName, copyName);
    }

    private World copyAndLoad(String worldName, String copyName) {
        File container = Bukkit.getWorldContainer();
        File source = new File(container, worldName);

        // A map with no world folder just gets an empty void world, same as before copies existed
        if (source.isDirectory()) {
            try {
                copyFolder(source.toPath(), new File(container, copyName).toPath());
            } catch (IOException e) {
                main.getLogger().severe("Couldn't copy map world " + worldName + " to " + copyName + ": " + e);
            }
        }

        World world = loadWorld(copyName);
        world.setAutoSave(false);
        return world;
    }

    /*
     * Unloads a game's world copy and deletes its folder. Unloading waits for everyone to leave the world
     * (players are normally sent to the lobby right before this), and sends anyone still there after a few
     * seconds to the lobby.
     */
    public void discardGameWorld(final World world) {
        if (world == null || !isCopy(world.getName())) return;
        final String name = world.getName();

        new BukkitRunnable() {
            int tries = 0;

            @Override
            public void run() {
                if (++tries > UNLOAD_TRIES_BEFORE_KICKING) {
                    for (Player player : world.getPlayers()) {
                        main.ResetPlayer(player);
                        // ResetPlayer does nothing for a player whose data isn't loaded - they'd keep the world loaded
                        if (player.getWorld() == world) player.teleport(main.LobbyLoc());
                    }
                }
                if (tries == UNLOAD_TRIES_BEFORE_WARNING) {
                    main.getLogger().warning("Map copy " + name + " still won't unload after " + tries
                            + " ticks - it stays in memory until it does (still trying)");
                }

                if (Bukkit.getWorld(name) != null && !Bukkit.unloadWorld(world, false)) return;
                cancel();

                final File folder = new File(Bukkit.getWorldContainer(), name);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        deleteFolder(folder);
                    }
                }.runTaskAsynchronously(main);
            }
        }.runTaskTimer(main, 1, 1);
    }

    /*
     * Spigot loads (and keeps loaded) the ~625 chunks around a new world's spawn right after this event, which
     * for a game copy is wasted memory and a lag spike every time a game is made. Its chunks load as players
     * get there instead.
     */
    @EventHandler
    public void onCopyInit(WorldInitEvent event) {
        if (isCopy(event.getWorld().getName())) event.getWorld().setKeepSpawnInMemory(false);
    }

    /** Deletes every map copy folder that isn't loaded - leftovers from a crash, or every copy on shutdown. */
    public void deleteUnloadedCopies() {
        File[] folders = Bukkit.getWorldContainer().listFiles();
        if (folders == null) return;

        for (File folder : folders) {
            if (!folder.isDirectory() || !isCopy(folder.getName())) continue;
            if (Bukkit.getWorld(folder.getName()) != null) continue;
            deleteFolder(folder);
        }
    }

    private String getFreeCopyName(File container, String worldName) {
        for (int i = 1; ; i++) {
            String name = worldName + COPY_SUFFIX + i;
            if (Bukkit.getWorld(name) == null && !new File(container, name).exists()) return name;
        }
    }

    /*
     * Only ever true for "<a map's world name>_Copy_<number>" or "<player name>_CustomGame", so nothing else
     * can be deleted by mistake.
     */
    private boolean isCopy(String folderName) {
        if (CUSTOM_GAME_COPY.matcher(folderName).matches()) return true;
        for (String worldName : getMapWorldNames()) {
            Pattern copy = Pattern.compile(Pattern.quote(worldName) + COPY_SUFFIX + "\\d+", Pattern.CASE_INSENSITIVE);
            if (copy.matcher(folderName).matches()) return true;
        }
        return false;
    }

    private static Set<String> getMapWorldNames() {
        Set<String> names = new HashSet<>();
        for (Maps map : Maps.values()) names.add(map.GetInstance().worldName);
        for (DuosMaps map : DuosMaps.values()) names.add(map.GetInstance().worldName);
        // A map switched off while a game was still on it has no Maps, but its game's copy must still be recognised
        MapStore store = Core.inst().getMapStore();
        if (store != null) names.addAll(store.worldNames());
        return names;
    }

    private World loadWorld(String name) {
        WorldCreator creator = new WorldCreator(name).environment(World.Environment.NORMAL);
        creator.generator(new VoidGenerator());
        World world = Bukkit.getServer().createWorld(creator);
        world.setDifficulty(Difficulty.EASY);
        return world;
    }

    private static void copyFolder(final Path source, final Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!SKIPPED_FILES.contains(file.getFileName().toString().toLowerCase())) {
                    Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteFolder(File folder) {
        try {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // Whatever's left gets another go on the next startup
            main.getLogger().warning("Couldn't fully delete map copy " + folder.getName() + ": " + e);
        }
    }

    // ===== /scbsetup: setting maps up and editing the original worlds =====

    public boolean isSetupWorld(World world) {
        return world != null && setupWorlds.contains(world.getName().toLowerCase());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp() && !player.hasPermission("scb.setup")) {
            player.sendMessage(main.color("&c&l(!) &rYou don't have permission to do this!"));
            return true;
        }

        String sub = args.length == 0 ? "" : args[0].toLowerCase();
        if (sub.equals("tp") && args.length >= 2) {
            teleportToMap(player, args[1]);
        } else if (sub.equals("create")) {
            if (args.length >= 2) createMap(player, args[1]);
            else error(player, "Say what the map is called: &e/scbsetup create <map>");
        } else if (sub.equals("save")) {
            saveCommand(player);
        } else if (sub.equals("leave")) {
            leaveCommand(player);
        } else if (sub.equals("status")) {
            MapSetupSession session = requireSession(player);
            if (session != null) setup.sendStatus(player, session);
        } else if (sub.equals("tools")) {
            MapSetupSession session = requireSession(player);
            if (session != null) {
                setup.resetTools(player, session);
                player.sendMessage(main.color("&a&l(!) &rYour setup items are back in their slots."));
            }
        } else if (sub.equals("mode")) {
            MapSetupSession session = requireSession(player);
            if (session != null) setup.toggleMode(player, session);
        } else if (sub.equals("icon")) {
            iconCommand(player, args);
        } else if ((sub.equals("enable") || sub.equals("disable")) && args.length >= 2) {
            setEnabled(player, args[1], sub.equals("enable"));
        } else {
            sendHelp(player);
        }
        return true;
    }

    private void error(Player player, String message) {
        player.sendMessage(main.color("&c&l(!) &r" + message));
    }

    private boolean inAGame(Player player) {
        return main.getGameManager().GetInstanceOfPlayer(player) != null
                || main.getGameManager().GetInstanceOfSpectator(player) != null;
    }

    private MapSetupSession requireSession(Player player) {
        MapSetupSession session = setup.get(player);
        if (session == null) {
            error(player, "You're not setting up a map. Use &e/scbsetup create <map>&r or &e/scbsetup tp <map>&r.");
        }
        return session;
    }

    // ----- create: a new map in a new void world

    private void createMap(Player player, String id) {
        MapStore store = main.getMapStore();
        if (store == null) {
            error(player, "The maps file didn't load, so no map can be made. Check the console.");
            return;
        }
        if (!MapStore.isValidId(id)) {
            error(player, "A map's name is 3 to 24 letters and numbers, and starts with a letter.");
            return;
        }
        if (setup.get(player) != null) {
            error(player, "You're already setting up a map. &e/scbsetup save&r it if you want it, then &e/scbsetup leave&r.");
            return;
        }
        if (inAGame(player)) {
            error(player, "Leave your game first.");
            return;
        }

        String worldName = id.toLowerCase(Locale.ROOT);
        if (store.find(id) != null || store.worldTaken(worldName, null) || findDuosMap(id) != null
                || findDuosMap(worldName) != null) {
            error(player, "There's already a map called &e" + id + "&r. &e/scbsetup tp " + id + " &redits it.");
            return;
        }

        File folder = new File(Bukkit.getWorldContainer(), worldName);
        if (Bukkit.getWorld(worldName) != null) {
            error(player, "A world called &e" + worldName + "&r is already loaded.");
            return;
        }
        if (folder.exists()) {
            // Left by a new map that was never saved (the server stopped while someone was setting it up)
            if (new File(folder, UNSAVED_MARKER).exists()) deleteFolder(folder);
            if (folder.exists()) {
                error(player, "There's already a folder called &e" + worldName + "&r in the server folder.");
                return;
            }
        }

        World world;
        try {
            world = loadWorld(worldName);
        } catch (Exception e) {
            main.getLogger().severe("Couldn't make the world for the new map " + id + ": " + e);
            error(player, "Couldn't make the world, see the console.");
            return;
        }
        world.setAutoSave(false); // only /scbsetup save writes it
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(1000); // like the other arenas
        world.setSpawnLocation(0, 64, 0);
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) world.getBlockAt(x, 63, z).setType(Material.STONE);
        }

        setupWorlds.add(worldName);
        unsavedNewWorlds.add(worldName);
        try {
            new File(folder, UNSAVED_MARKER).createNewFile();
        } catch (IOException e) {
            main.getLogger().warning("Couldn't mark " + worldName + " as a new map, it is kept if the server crashes: " + e);
        }

        player.teleport(new Location(world, 0.5, 64, 0.5));
        setup.start(player, new MapDraft(id, worldName), world, true);
        player.sendMessage(main.color("&a&l(!) &rYou're in the new world for &e" + id + "&r. Use the items to set the "
                + "map up, then &e/scbsetup save&r."));
        player.sendMessage(main.color("&7&e/scbsetup status &7shows what is still missing, &e/scbsetup leave &7throws "
                + "an unsaved map away."));
    }

    // ----- tp: an existing map

    private void teleportToMap(Player player, String mapName) {
        MapStore store = main.getMapStore();
        MapDraft draft = store == null ? null : store.find(mapName);
        MapInstance duos = draft == null ? findDuosMap(mapName) : null;
        if (draft == null && duos == null) {
            error(player, "There's no map called &e" + mapName + "&r.");
            return;
        }
        if (inAGame(player)) {
            error(player, "Leave your game first.");
            return;
        }
        if (setup.get(player) != null) {
            error(player, "You're already setting up a map. &e/scbsetup save&r it if you want it, then &e/scbsetup leave&r.");
            return;
        }
        if (draft != null) {
            Player other = setup.editorOf(draft.id);
            if (other != null) {
                error(player, "&e" + other.getName() + " &ris setting up &e" + draft.id + "&r right now.");
                return;
            }
        }

        String worldName = draft != null ? draft.world : duos.worldName;
        World world = Bukkit.getWorld(worldName);
        if (world == null) world = loadWorld(worldName);
        world.setAutoSave(false); // also keeps chunks from being saved as they unload - only /scbsetup save writes
        setupWorlds.add(world.getName().toLowerCase());

        Vector loc = draft != null ? draft.lobby : duos.lobbyLoc;
        if (loc != null) player.teleport(FacingVector.toLocation(world, loc)); // facing the way the lobby was set, if it was
        else player.teleport(world.getSpawnLocation().add(0.5, 0, 0.5));

        if (draft != null) {
            setup.start(player, draft, world, false);
            player.sendMessage(main.color("&a&l(!) &rYou're in the original &e" + world.getName() + " &rworld. Use the "
                    + "items to change the map. Changes are only kept if you use &e/scbsetup save&r. "
                    + "&e/scbsetup leave &rwhen you're done."));
            if (!draft.isComplete()) {
                player.sendMessage(main.color("&e&l(!) &r" + draft.id + " can't be played yet. &e/scbsetup status &rshows why."));
            }
        } else {
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(main.color("&a&l(!) &rYou're in the original &e" + world.getName() + " &rworld. Changes are "
                    + "only kept if you use &e/scbsetup save&r - new games then use them. &e/scbsetup leave &rwhen you're done."));
        }
    }

    // ----- save and leave

    void saveCommand(Player player) {
        World world = player.getWorld();
        if (!isSetupWorld(world)) {
            player.sendMessage(main.color("&c&l(!) &rYou're not in a map opened with &e/scbsetup tp&r or made with "
                    + "&e/scbsetup create&r."));
            return;
        }

        world.save();
        player.sendMessage(main.color("&a&l(!) &rSaved the world &e" + world.getName() + "&r."));

        MapSetupSession session = setup.get(player);
        if (session != null && session.world == world) setup.saveMap(player, session);
    }

    /** The map's file entry is written, so its world folder is a real map now and is kept. */
    void markSaved(World world) {
        String lower = world.getName().toLowerCase();
        if (unsavedNewWorlds.remove(lower)) {
            new File(new File(Bukkit.getWorldContainer(), world.getName()), UNSAVED_MARKER).delete();
        }
    }

    private void leaveCommand(Player player) {
        final World world = player.getWorld();
        if (!isSetupWorld(world)) {
            player.sendMessage(main.color("&c&l(!) &rYou're not in a map opened with &e/scbsetup tp&r or made with "
                    + "&e/scbsetup create&r."));
            return;
        }

        boolean wasNew = unsavedNewWorlds.contains(world.getName().toLowerCase());
        setup.end(player); // takes the setup items back
        main.ResetPlayer(player); // the world change unloads it (see onLeaveSetupWorld)
        player.sendMessage(main.color("&a&l(!) &rYou left &e" + world.getName() + "&r. Anything you didn't "
                + "&e/scbsetup save &rwas " + (wasNew ? "thrown away, including the new world." : "discarded.")));
    }

    // ----- the rest

    private void iconCommand(Player player, String[] args) {
        MapSetupSession session = requireSession(player);
        if (session == null) return;
        if (args.length < 2) {
            error(player, "Say which item: &e/scbsetup icon <item>&r, like &e/scbsetup icon STAINED_GLASS:5&r. "
                    + "The icon item also lets you click an item in your inventory.");
            return;
        }

        String[] parts = args[1].split(":");
        Material material = Material.matchMaterial(parts[0]);
        if (material == null || material == Material.AIR) {
            error(player, "There's no item called &e" + parts[0] + "&r.");
            return;
        }
        short data = 0;
        if (parts.length > 1) {
            try {
                data = Short.parseShort(parts[1]);
            } catch (NumberFormatException e) {
                error(player, "&e" + parts[1] + " &risn't a number.");
                return;
            }
        }
        setup.setIcon(player, session, material, data);
    }

    private void setEnabled(Player player, String mapName, boolean enabled) {
        MapStore store = main.getMapStore();
        MapDraft draft = store == null ? null : store.find(mapName);
        if (draft == null) {
            error(player, "There's no map called &e" + mapName + "&r.");
            return;
        }
        Player editor = setup.editorOf(draft.id);
        if (editor != null) {
            error(player, "&e" + draft.id + " &ris being set up by &e" + editor.getName() + "&r. Save and leave first.");
            return;
        }
        if (draft.enabled == enabled) {
            error(player, "&e" + draft.id + " &ris already " + (enabled ? "on" : "off") + ".");
            return;
        }

        draft.enabled = enabled;
        try {
            store.save(draft);
        } catch (IOException | RuntimeException e) {
            main.getLogger().severe("[Maps] Couldn't save " + draft.id + " to maps.yml: " + e);
            error(player, "Couldn't write maps.yml, see the console.");
            return;
        }

        player.sendMessage(main.color("&a&l(!) &r" + draft.id + " is " + (enabled ? "on" : "off") + "."));
        if (enabled && !draft.isComplete()) {
            player.sendMessage(main.color("&e&l(!) &rIt can't be played yet, it still needs:"));
            for (String missing : draft.missing()) player.sendMessage(main.color("&c- " + missing));
        }
    }

    /*
     * However an editor leaves an original map (/scbsetup leave, /hub, quitting, any teleport), the world is
     * unloaded without saving once nobody is left in it. Otherwise it stays loaded with the unsaved changes and
     * the next /scbsetup tp goes right back into them. A new map that was never saved is deleted with it.
     */
    @EventHandler
    public void onLeaveSetupWorld(PlayerChangedWorldEvent event) {
        unloadIfEmpty(event.getFrom());
    }

    @EventHandler
    public void onQuitInSetupWorld(PlayerQuitEvent event) {
        unloadIfEmpty(event.getPlayer().getWorld());
    }

    private void unloadIfEmpty(final World world) {
        if (!isSetupWorld(world)) return;

        // Next tick, once the player is actually out of the world
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isSetupWorld(world) || !world.getPlayers().isEmpty()) return;
                if (!Bukkit.unloadWorld(world, false)) return;

                String lower = world.getName().toLowerCase();
                setupWorlds.remove(lower);
                if (unsavedNewWorlds.remove(lower)) deleteNewMapFolder(world.getName());
            }
        }.runTaskLater(main, 1);
    }

    /** Deletes the folder of a new map that was never saved. Only ever one that still has its marker. */
    private void deleteNewMapFolder(String worldName) {
        final File folder = new File(Bukkit.getWorldContainer(), worldName);
        if (!new File(folder, UNSAVED_MARKER).exists()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                deleteFolder(folder);
            }
        }.runTaskAsynchronously(main);
    }

    /** On startup: the world folders of new maps that were never saved, left by a stop or a crash while they were set up. */
    public void deleteAbandonedNewMaps() {
        File[] folders = Bukkit.getWorldContainer().listFiles();
        if (folders == null) return;

        MapStore store = main.getMapStore();
        for (File folder : folders) {
            if (!folder.isDirectory() || !new File(folder, UNSAVED_MARKER).exists()) continue;
            if (Bukkit.getWorld(folder.getName()) != null) continue;
            if (store != null && store.worldTaken(folder.getName(), null)) continue; // it is a saved map after all

            main.getLogger().info("Deleting the new map world " + folder.getName() + ", it was never saved.");
            deleteFolder(folder);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(main.color("&8&m------------------------------------"));
        player.sendMessage(main.color("&6&lMAP SETUP"));
        player.sendMessage(main.color("&e/scbsetup create <map> &rMake a new map in a new void world"));
        player.sendMessage(main.color("&e/scbsetup tp <map> &rGo to a map's original world to edit it"));
        player.sendMessage(main.color("&e/scbsetup save &rSave the map you're in. It's playable once it has everything"));
        player.sendMessage(main.color("&e/scbsetup status &rWhat the map has and still needs"));
        player.sendMessage(main.color("&e/scbsetup tools &rPut the setup items back in their slots"));
        player.sendMessage(main.color("&e/scbsetup mode &rSwitch between creative and spectator"));
        player.sendMessage(main.color("&e/scbsetup icon <item> &rSet the game selector icon by name, like STAINED_GLASS:5"));
        player.sendMessage(main.color("&e/scbsetup enable|disable <map> &rPut a map in or out of the game"));
        player.sendMessage(main.color("&e/scbsetup leave &rGo back to the lobby (unsaved changes are discarded)"));
        player.sendMessage(main.color("&8&m------------------------------------"));
    }

    /** A Duos map (they are not in maps.yml) by its name or world folder name. */
    private static MapInstance findDuosMap(String name) {
        for (DuosMaps map : DuosMaps.values()) {
            if (map.toString().equalsIgnoreCase(name) || map.GetInstance().worldName.equalsIgnoreCase(name))
                return map.GetInstance();
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("create", "tp", "save", "status", "tools", "mode", "icon",
                    "enable", "disable", "leave"), new ArrayList<>());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("tp") || sub.equals("enable") || sub.equals("disable")) {
                List<String> names = new ArrayList<>();
                MapStore store = main.getMapStore();
                if (store != null) names.addAll(store.ids());
                if (sub.equals("tp")) {
                    for (DuosMaps map : DuosMaps.values()) names.add(map.toString());
                }
                return StringUtil.copyPartialMatches(args[1], names, new ArrayList<>());
            }
            if (sub.equals("icon")) {
                List<String> names = new ArrayList<>();
                for (Material material : Material.values()) names.add(material.name());
                return StringUtil.copyPartialMatches(args[1], names, new ArrayList<>());
            }
        }
        return new ArrayList<>();
    }
}
