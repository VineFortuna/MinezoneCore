package anthony.SuperCraftBrawl.Game.map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * The file all the Super Craft Bros maps are kept in (maps.yml in the plugin's folder), and the {@link Maps} that the
 * game plays on, which are made from it.
 *
 * The first time the plugin runs it copies the maps.yml that ships in the jar (the maps that used to be written in
 * Maps.java). After that the file in the plugin folder is the real one: /scbsetup changes it, and every change is
 * written at once, safely (a temporary file first, and the last version is kept as maps.yml.bak). If it can't be read
 * the broken one is put aside and the one from the jar is used, so a typo never stops the plugin from loading.
 *
 * Everything in the file is kept here as {@link MapDraft}s, including maps that are switched off or only half set up.
 * Only the complete, switched on ones are playable, and those are registered in {@link Maps}.
 */
public final class MapStore {

    private static final String HEADER =
            "# Super Craft Bros maps. In game: /scbsetup create <map>, /scbsetup tp <map>, /scbsetup save.\n"
                    + "# This file is rewritten whenever a map is saved, so comments in it are not kept.\n"
                    + "# Edit it by hand only while the server is stopped. A map needs a world, a lobby, a spectator\n"
                    + "# location, at least 2 spawns, bounds, a source, size, gameplay, category and icon to be playable;\n"
                    + "# enabled: false keeps a complete map out of the game.\n";

    /** What a map is called: it is a file name and a database value. */
    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z][A-Za-z0-9]{2,23}");

    private final File file;
    private final Supplier<InputStream> defaults;
    private final Logger log;
    private final Map<String, MapDraft> entries = new LinkedHashMap<String, MapDraft>();

    /**
     * @param defaults  the maps.yml in the jar, a new stream every time it is asked, or null if there isn't one
     */
    public MapStore(File file, Supplier<InputStream> defaults, Logger log) {
        this.file = file;
        this.defaults = defaults;
        this.log = log;
    }

    /** YAML reads these bare words as true, false or nothing, so a map with one of them as its name could not be read back. */
    static final java.util.Set<String> YAML_WORDS = new java.util.HashSet<String>(
            java.util.Arrays.asList("yes", "off", "true", "false", "null"));

    public static boolean isValidId(String id) {
        return id != null && VALID_ID.matcher(id).matches() && !YAML_WORDS.contains(id.toLowerCase(java.util.Locale.ROOT));
    }

    // ---- the name players see (a map's id never changes, its name can)

    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 24;
    /**
     * Letters, numbers, spaces, hyphens and periods. No quotes (the name is saved with each finished game, in a query),
     * no colour codes (the menus add their own colours) and nothing that is hard to keep in a YAML file.
     */
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9 .\\-]*");

    /** What was typed, tidied: colour codes taken out, spaces at the ends removed and runs of spaces made one. */
    public static String cleanDisplayName(String typed) {
        if (typed == null) return "";
        String name = org.bukkit.ChatColor.stripColor(typed);
        return name.trim().replaceAll("\\s+", " ");
    }

    /** Whether a tidied name can be a map's name: its length and what it is made of. */
    public static boolean isValidDisplayName(String name) {
        return name != null && name.length() >= MIN_NAME_LENGTH && name.length() <= MAX_NAME_LENGTH
                && VALID_NAME.matcher(name).matches();
    }

    /** Whether another map (not {@code exceptId}) is already called this, or has it as its id. Any case. */
    public boolean nameTaken(String name, String exceptId) {
        for (MapDraft draft : entries.values()) {
            if (draft.id.equals(exceptId)) continue;
            if (draft.id.equalsIgnoreCase(name) || draft.displayName.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- reading

    /** Reads the file (making it first if there is none) and makes its playable maps the playable {@link Maps}. */
    public void load() {
        entries.clear();

        if (!file.exists()) seed();

        if (!read()) {
            if (file.exists()) {
                File broken = new File(file.getParentFile(), file.getName() + ".broken-" + System.currentTimeMillis());
                if (file.renameTo(broken)) log.severe("[Maps] maps.yml could not be read, it was kept as " + broken.getName());
            }
            entries.clear();
            seed();
            if (!read()) log.severe("[Maps] The maps that ship with the plugin couldn't be read either: no maps!");
        }

        publish(null);
        int playable = 0;
        for (MapDraft draft : entries.values()) if (draft.isPlayable()) playable++;
        log.info("[Maps] " + entries.size() + " maps in maps.yml, " + playable + " playable.");
    }

    /** Copies the maps.yml of the jar to the plugin folder. */
    private void seed() {
        if (defaults == null) return;
        try (InputStream in = defaults.get()) {
            if (in == null) {
                log.severe("[Maps] There is no maps.yml in the plugin jar to start from.");
                return;
            }
            File folder = file.getParentFile();
            if (folder != null) folder.mkdirs();
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("[Maps] Made " + file.getName() + " from the maps that ship with the plugin.");
        } catch (IOException e) {
            log.severe("[Maps] Couldn't make " + file.getName() + ": " + e);
        }
    }

    /** Parses the file into the entries. False if it is not valid YAML at all. */
    private boolean read() {
        YamlConfiguration yaml = new YamlConfiguration();
        try (Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
            yaml.load(reader);
        } catch (IOException | InvalidConfigurationException e) {
            log.severe("[Maps] " + file.getName() + " isn't valid: " + e.getMessage());
            return false;
        }

        ConfigurationSection maps = yaml.getConfigurationSection("maps");
        if (maps == null) {
            log.severe("[Maps] " + file.getName() + " has no maps: section");
            return false;
        }

        List<String> warnings = new ArrayList<String>();
        for (String id : maps.getKeys(false)) {
            ConfigurationSection section = maps.getConfigurationSection(id);
            if (section == null) {
                warnings.add(id + ": not a map");
                continue;
            }
            if (!isValidId(id)) warnings.add(id + ": not a good map name (letters and digits, 3 to 24 long)");
            entries.put(id, MapDraft.read(id, section, warnings));
        }
        for (String warning : warnings) log.warning("[Maps] " + warning);
        return true;
    }

    /** Makes the playable maps what the entries say, in the order of the file. */
    private void publish(String changedId) {
        List<MapDraft> playable = new ArrayList<MapDraft>();
        for (MapDraft draft : entries.values()) {
            if (draft.isPlayable()) playable.add(draft);
        }
        Maps.publish(playable, changedId);
    }

    // ---------------------------------------------------------------- looking maps up

    /** A copy of the entry with exactly this name (a map that is playable or not), or null. Changing it changes nothing. */
    public MapDraft get(String id) {
        MapDraft draft = entries.get(id);
        return draft == null ? null : draft.copy();
    }

    /** A copy of the map whose name or world folder is this, whatever case, or null. */
    public MapDraft find(String nameOrWorld) {
        for (MapDraft draft : entries.values()) {
            if (draft.id.equalsIgnoreCase(nameOrWorld) || draft.world.equalsIgnoreCase(nameOrWorld)) return draft.copy();
        }
        return null;
    }

    /** Every map in the file, in its order. */
    public List<String> ids() {
        return new ArrayList<String>(entries.keySet());
    }

    /** The world folders of every map in the file, playable or not (a game's copy of any of them can exist). */
    public List<String> worldNames() {
        List<String> names = new ArrayList<String>();
        for (MapDraft draft : entries.values()) names.add(draft.world);
        return names;
    }

    /** Whether another map already uses this world folder. */
    public boolean worldTaken(String world, String exceptId) {
        for (MapDraft draft : entries.values()) {
            if (!draft.id.equals(exceptId) && draft.world.equalsIgnoreCase(world)) return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- writing

    /**
     * Adds or changes a map, writes the file, and makes the map playable (or not) to match. If the file can't be
     * written nothing has changed.
     */
    public void save(MapDraft draft) throws IOException {
        MapDraft previous = entries.get(draft.id);
        entries.put(draft.id, draft.copy());

        try {
            write();
        } catch (IOException | RuntimeException e) {
            if (previous == null) entries.remove(draft.id);
            else entries.put(draft.id, previous);
            throw e;
        }

        publish(draft.id);
    }

    /** The whole file as text. */
    String render() {
        StringBuilder out = new StringBuilder(HEADER);
        out.append("maps:\n");
        for (MapDraft draft : entries.values()) draft.write(out);
        return out.toString();
    }

    private void write() throws IOException {
        File folder = file.getParentFile();
        if (folder != null) folder.mkdirs();

        File temporary = new File(folder, file.getName() + ".tmp");
        Files.write(temporary.toPath(), render().getBytes(StandardCharsets.UTF_8));

        if (file.exists()) {
            Files.copy(file.toPath(), new File(folder, file.getName() + ".bak").toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
