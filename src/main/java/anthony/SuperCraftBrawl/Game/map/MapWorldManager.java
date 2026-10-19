package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
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

    private final Core main;
    private final Set<String> setupWorlds = new HashSet<>(); // original worlds loaded by /scbsetup (lowercase)

    public MapWorldManager(Core main) {
        this.main = main;
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

    // ===== /scbsetup: editing the original maps =====

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
        } else if (sub.equals("save")) {
            saveCommand(player);
        } else if (sub.equals("leave")) {
            leaveCommand(player);
        } else {
            sendHelp(player);
        }
        return true;
    }

    private void teleportToMap(Player player, String mapName) {
        MapInstance map = findMap(mapName);
        if (map == null) {
            player.sendMessage(main.color("&c&l(!) &rThere's no map called &e" + mapName + "&r."));
            return;
        }
        if (main.getGameManager().GetInstanceOfPlayer(player) != null
                || main.getGameManager().GetInstanceOfSpectator(player) != null) {
            player.sendMessage(main.color("&c&l(!) &rLeave your game first."));
            return;
        }

        World world = Bukkit.getWorld(map.worldName);
        if (world == null) world = loadWorld(map.worldName);
        world.setAutoSave(false); // also keeps chunks from being saved as they unload - only /scbsetup save writes
        setupWorlds.add(world.getName().toLowerCase());

        Vector loc = map.lobbyLoc;
        player.teleport(new Location(world, loc.getX(), loc.getY(), loc.getZ()));
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(main.color("&a&l(!) &rYou're in the original &e" + map.worldName + " &rworld. Changes are "
                + "only kept if you use &e/scbsetup save&r - new games then use them. &e/scbsetup leave &rwhen you're done."));
    }

    private void saveCommand(Player player) {
        World world = player.getWorld();
        if (!isSetupWorld(world)) {
            player.sendMessage(main.color("&c&l(!) &rYou're not in a map opened with &e/scbsetup tp&r."));
            return;
        }

        world.save();
        player.sendMessage(main.color("&a&l(!) &rSaved &e" + world.getName() + "&r."));
    }

    private void leaveCommand(Player player) {
        final World world = player.getWorld();
        if (!isSetupWorld(world)) {
            player.sendMessage(main.color("&c&l(!) &rYou're not in a map opened with &e/scbsetup tp&r."));
            return;
        }

        main.ResetPlayer(player); // the world change unloads it (see onLeaveSetupWorld)
        player.sendMessage(main.color("&a&l(!) &rYou left &e" + world.getName() + "&r. Anything you didn't "
                + "&e/scbsetup save &rwas discarded."));
    }

    /*
     * However an editor leaves an original map (/scbsetup leave, /hub, quitting, any teleport), the world is
     * unloaded without saving once nobody is left in it. Otherwise it stays loaded with the unsaved changes and
     * the next /scbsetup tp goes right back into them.
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
                if (Bukkit.unloadWorld(world, false)) setupWorlds.remove(world.getName().toLowerCase());
            }
        }.runTaskLater(main, 1);
    }

    private void sendHelp(Player player) {
        player.sendMessage(main.color("&8&m------------------------------------"));
        player.sendMessage(main.color("&6&lMAP SETUP"));
        player.sendMessage(main.color("&e/scbsetup tp <map> -> &rGo to a map's original world to edit it"));
        player.sendMessage(main.color("&e/scbsetup save -> &rSave the map you're in"));
        player.sendMessage(main.color("&e/scbsetup leave -> &rGo back to the lobby (unsaved changes are discarded)"));
        player.sendMessage(main.color("&8&m------------------------------------"));
    }

    /** A map by its name (e.g. NightDragon) or world folder name, normal maps first, then Duos maps. */
    private static MapInstance findMap(String name) {
        for (Maps map : Maps.values()) {
            if (map.toString().equalsIgnoreCase(name) || map.GetInstance().worldName.equalsIgnoreCase(name))
                return map.GetInstance();
        }
        for (DuosMaps map : DuosMaps.values()) {
            if (map.toString().equalsIgnoreCase(name) || map.GetInstance().worldName.equalsIgnoreCase(name))
                return map.GetInstance();
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("tp", "save", "leave"), new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            List<String> names = new ArrayList<>();
            for (Maps map : Maps.values()) names.add(map.toString());
            for (DuosMaps map : DuosMaps.values()) names.add(map.toString());
            return StringUtil.copyPartialMatches(args[1], names, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
