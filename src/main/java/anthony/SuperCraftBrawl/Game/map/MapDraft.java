package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Game.GameType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything about one map as it is in maps.yml. It is also what /scbsetup edits: a map that is only half set up is a
 * draft with some of this still missing ({@link #missing()}), and only a complete one can be played.
 *
 * Only data and the file format live here, nothing that needs a server.
 */
public final class MapDraft {

    /** A map needs at least this many spawns: respawning picks a different one from the last, so one would hang. */
    public static final int MIN_SPAWNS = 2;

    /** What the map is called everywhere (the old enum constant's name). It is how it is saved in the database. */
    public final String id;
    public String displayName;
    /** The folder of the map's world in the server folder. */
    public String world;
    public GameType gameType = GameType.CLASSIC;
    public MapSource source;
    public Maps.Category category;
    public Maps.Size size;
    public final List<Maps.Gameplay> gameplay = new ArrayList<Maps.Gameplay>();
    public Material iconType;
    public short iconData;
    // The lobby, the spectator location and the spawns can be a FacingVector, if they face a direction
    public Vector lobby;
    public Vector spectator;
    public final List<Vector> spawns = new ArrayList<Vector>();
    /** The middle of the play area, and how far it reaches either way along x and z (the map's boundary rectangle). */
    public Vector center;
    public double boundsX;
    public double boundsZ;
    /** Off keeps a complete map out of the game, like the maps that used to be commented out. */
    public boolean enabled = true;

    public MapDraft(String id, String world) {
        this.id = id;
        this.displayName = id;
        this.world = world;
    }

    public MapDraft copy() {
        MapDraft copy = new MapDraft(id, world);
        copy.displayName = displayName;
        copy.gameType = gameType;
        copy.source = source;
        copy.category = category;
        copy.size = size;
        copy.gameplay.addAll(gameplay);
        copy.iconType = iconType;
        copy.iconData = iconData;
        copy.lobby = lobby == null ? null : lobby.clone();
        copy.spectator = spectator == null ? null : spectator.clone();
        for (Vector spawn : spawns) copy.spawns.add(spawn.clone());
        copy.center = center == null ? null : center.clone();
        copy.boundsX = boundsX;
        copy.boundsZ = boundsZ;
        copy.enabled = enabled;
        return copy;
    }

    // ---------------------------------------------------------------- what is still missing

    public boolean hasBounds() {
        return center != null && boundsX > 0 && boundsZ > 0;
    }

    /** What still has to be set before the map can be played, in words. Empty when it is complete. */
    public List<String> missing() {
        List<String> missing = new ArrayList<String>();
        if (lobby == null) missing.add("the lobby location");
        if (spawns.size() < MIN_SPAWNS)
            missing.add("spawn locations (" + spawns.size() + " set, at least " + MIN_SPAWNS + " needed)");
        if (spectator == null) missing.add("the spectator location");
        if (!hasBounds()) missing.add("the map boundaries (both positions)");
        if (source == null) missing.add("the source");
        if (size == null) missing.add("the map size");
        if (gameplay.isEmpty()) missing.add("the gameplay (at least one)");
        if (category == null) missing.add("the category");
        if (iconType == null) missing.add("the game selector icon");
        return missing;
    }

    public boolean isComplete() {
        return missing().isEmpty();
    }

    /** Whether the game uses it: complete and not switched off. */
    public boolean isPlayable() {
        return enabled && isComplete();
    }

    // ---------------------------------------------------------------- the map the game uses

    public ItemStack icon() {
        return new ItemStack(iconType, 1, iconData);
    }

    /** The world data the game works with. Only for a complete map. */
    public MapInstance toInstance() {
        MapInstance instance = new MapInstance(world);
        instance.setSpawnPos(copyOf(spawns));
        instance.setLobbyLoc(lobby.clone());
        instance.setSpecLoc(spectator.clone());
        instance.setBounds(center.clone(), boundsX, boundsZ);
        instance.setSource(source);
        instance.setGameType(gameType);
        return instance;
    }

    private static Vector[] copyOf(List<Vector> vectors) {
        Vector[] copy = new Vector[vectors.size()];
        for (int i = 0; i < copy.length; i++) copy[i] = vectors.get(i).clone();
        return copy;
    }

    // ---------------------------------------------------------------- the file

    /** One map's entry in maps.yml, written the same way every time so the file stays easy to read and to diff. */
    public void write(StringBuilder out) {
        out.append("  ").append(id).append(":\n");
        out.append("    name: ").append(yamlText(displayName)).append('\n');
        out.append("    world: ").append(world).append('\n');
        out.append("    type: ").append(gameType.name()).append('\n');
        if (source != null) out.append("    source: ").append(source.name()).append('\n');
        if (category != null) out.append("    category: ").append(category.name()).append('\n');
        if (size != null) out.append("    size: ").append(size.name()).append('\n');

        out.append("    gameplay: [");
        for (int i = 0; i < gameplay.size(); i++) out.append(i == 0 ? "" : ", ").append(gameplay.get(i).name());
        out.append("]\n");

        if (iconType != null)
            out.append("    icon: ").append(iconType.name()).append(iconData == 0 ? "" : ":" + iconData).append('\n');
        if (lobby != null) out.append("    lobby: ").append(vector(lobby)).append('\n');
        if (spectator != null) out.append("    spectator: ").append(vector(spectator)).append('\n');

        if (center != null) {
            out.append("    bounds:\n");
            out.append("      center: ").append(vector(center)).append('\n');
            out.append("      x: ").append(boundsX).append('\n');
            out.append("      z: ").append(boundsZ).append('\n');
        }

        if (spawns.isEmpty()) {
            out.append("    spawns: []\n");
        } else {
            out.append("    spawns:\n");
            for (Vector spawn : spawns) out.append("    - ").append(vector(spawn)).append('\n');
        }
        out.append("    enabled: ").append(enabled).append('\n');
    }

    /**
     * The text as a YAML value. Most names are written as they are; one that YAML would read as something else (a number,
     * true, nothing: 123 Maze, Yes) is put in quotes so it is read back as the same name.
     */
    private static String yamlText(String text) {
        boolean plain = text.matches("[A-Za-z][A-Za-z0-9 .\\-]*") && !text.endsWith(" ")
                && !MapStore.YAML_WORDS.contains(text.toLowerCase(java.util.Locale.ROOT));
        return plain ? text : "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** [x, y, z], or [x, y, z, yaw, pitch] for a point that faces a direction. */
    private static String vector(Vector v) {
        String position = v.getX() + ", " + v.getY() + ", " + v.getZ();
        if (v instanceof FacingVector) {
            FacingVector facing = (FacingVector) v;
            return "[" + position + ", " + facing.getYaw() + ", " + facing.getPitch() + "]";
        }
        return "[" + position + "]";
    }

    /**
     * Reads one map from its section of maps.yml. A value that is missing or is not valid (a typo in an enum name, say)
     * leaves that part unset, which just shows up as missing, so one bad line never loses the other maps.
     *
     * @param warnings  told about every value that was there but couldn't be used
     */
    public static MapDraft read(String id, ConfigurationSection section, List<String> warnings) {
        String world = section.getString("world");
        MapDraft draft = new MapDraft(id, world == null || world.isEmpty() ? id.toLowerCase() : world);
        if (world == null || world.isEmpty()) warnings.add(id + ": no world, using " + draft.world);

        draft.displayName = section.getString("name", id);
        draft.enabled = section.getBoolean("enabled", true);

        String type = section.getString("type", "CLASSIC");
        draft.gameType = enumOf(GameType.class, type, GameType.CLASSIC, id, "type", warnings);
        draft.source = enumOf(MapSource.class, section.getString("source"), null, id, "source", warnings);
        draft.category = enumOf(Maps.Category.class, section.getString("category"), null, id, "category", warnings);
        draft.size = enumOf(Maps.Size.class, section.getString("size"), null, id, "size", warnings);

        for (Object entry : section.getList("gameplay", new ArrayList<Object>())) {
            Maps.Gameplay gameplay = enumOf(Maps.Gameplay.class, String.valueOf(entry), null, id, "gameplay", warnings);
            if (gameplay != null && !draft.gameplay.contains(gameplay)) draft.gameplay.add(gameplay);
        }

        String icon = section.getString("icon");
        if (icon != null) {
            String[] parts = icon.split(":");
            Material material = Material.matchMaterial(parts[0]);
            if (material == null) {
                warnings.add(id + ": no item called " + parts[0]);
            } else {
                draft.iconType = material;
                try {
                    draft.iconData = parts.length > 1 ? Short.parseShort(parts[1].trim()) : 0;
                } catch (NumberFormatException e) {
                    warnings.add(id + ": icon data " + parts[1] + " is not a number");
                }
            }
        }

        draft.lobby = vectorOf(section.getList("lobby"), id, "lobby", warnings);
        draft.spectator = vectorOf(section.getList("spectator"), id, "spectator", warnings);

        ConfigurationSection bounds = section.getConfigurationSection("bounds");
        if (bounds != null) {
            draft.center = vectorOf(bounds.getList("center"), id, "bounds center", warnings);
            draft.boundsX = bounds.getDouble("x");
            draft.boundsZ = bounds.getDouble("z");
        }

        List<?> spawns = section.getList("spawns");
        if (spawns != null) {
            for (Object entry : spawns) {
                Vector spawn = entry instanceof List ? vectorOf((List<?>) entry, id, "a spawn", warnings) : null;
                if (spawn != null) draft.spawns.add(spawn);
                else if (!(entry instanceof List)) warnings.add(id + ": a spawn is not a list of three numbers");
            }
        }
        return draft;
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, String value, E fallback, String id, String what,
                                                List<String> warnings) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            warnings.add(id + ": " + what + " " + value + " is not one of " + java.util.Arrays.toString(type.getEnumConstants()));
            return fallback;
        }
    }

    private static Vector vectorOf(List<?> list, String id, String what, List<String> warnings) {
        if (list == null) return null;

        boolean valid = list.size() == 3 || list.size() == 5;
        for (Object value : list) {
            if (!(value instanceof Number)) valid = false;
        }
        if (!valid) {
            warnings.add(id + ": " + what + " should be [x, y, z], or [x, y, z, yaw, pitch] to face a direction");
            return null;
        }

        double x = ((Number) list.get(0)).doubleValue();
        double y = ((Number) list.get(1)).doubleValue();
        double z = ((Number) list.get(2)).doubleValue();
        if (list.size() == 3) return new Vector(x, y, z);
        return new FacingVector(x, y, z, ((Number) list.get(3)).floatValue(), ((Number) list.get(4)).floatValue());
    }
}
