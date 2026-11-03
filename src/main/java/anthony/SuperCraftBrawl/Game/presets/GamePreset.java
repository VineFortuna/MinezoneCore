package anthony.SuperCraftBrawl.Game.presets;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameSettings.HealthPotMode;
import anthony.SuperCraftBrawl.Game.GameSettings.OneForAllMode;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.ClassType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.EnumSet;

/*
 * A snapshot of a custom game's Game Settings: its modifiers (the same ones copyModifiersFrom carries over),
 * Frenzy and the time of day. Visibility, co-host and kicked players are never part of a preset.
 */
public class GamePreset {

    public static final long DAY = 1000;
    public static final int MAX_NAME_LENGTH = 24;

    public final GameSettings settings = new GameSettings(null); // only holds modifier values, never a game
    public boolean frenzy = false;
    public long timeOfDay = DAY;
    public String name = null; // a player's own name for it, null = just "Preset #n"

    /** A typed preset name made safe for the menus: no color codes, single spaces, trimmed and capped. Null if empty. */
    public static String cleanName(String typed) {
        if (typed == null) return null;
        String name = typed.replaceAll("[&§]", "").replaceAll("\\s+", " ").trim();
        if (name.length() > MAX_NAME_LENGTH) name = name.substring(0, MAX_NAME_LENGTH).trim();
        return name.isEmpty() ? null : name;
    }

    /** The current settings of a game. */
    public static GamePreset capture(GameInstance game) {
        GamePreset preset = new GamePreset();
        preset.settings.copyModifiersFrom(game.getGameSettings());
        preset.frenzy = game.gameType == GameType.FRENZY;
        preset.timeOfDay = game.getMapWorld().getTime();
        return preset;
    }

    /** Replaces the game's settings with this preset's (a Duel stays a Duel). */
    public void applyTo(GameInstance game) {
        game.getGameSettings().copyModifiersFrom(settings);
        if (game.gameType != GameType.DUEL) game.gameType = frenzy ? GameType.FRENZY : GameType.CLASSIC;
        game.getMapWorld().setTime(timeOfDay);
        game.updateClassSelectionItems();
        game.updateModeLine();
    }

    /** Classic: no Frenzy, Class Switch or One For All. */
    public boolean isDefaultClassSelection() {
        return !frenzy && !settings.randomClasses && settings.oneForAllMode == OneForAllMode.DISABLED;
    }

    /** How players get their class with this preset. */
    public String getClassSelection() {
        if (frenzy) return "Frenzy";
        if (settings.randomClasses) return "Class Switch (" + settings.randomClassCycleSeconds + "s)";
        if (settings.oneForAllMode != OneForAllMode.DISABLED) return "One For All (" + settings.oneForAllMode.getName() + ")";
        return "Classic";
    }

    // ===== Saving =====

    /*
     * e.g. "lives=20;respawn=0;...;classesoff=Chicken,Bunny" - unknown keys and values are skipped on load.
     * The class list is saved as what differs from the default list (classesoff = default classes that are disabled,
     * classeson = other classes that are enabled), so holiday classes follow the default list: active ones are
     * included automatically, and inactive ones are never saved.
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        if (name != null) put(sb, "name", encode(name));
        put(sb, "drop", settings.dropTimer);
        put(sb, "strength", settings.strengthOnKillSeconds);
        put(sb, "healing", settings.healthPotMode.name());
        put(sb, "hearts", settings.maxHealthHearts);
        put(sb, "ofa", settings.oneForAllMode.name());
        put(sb, "spawnprot", settings.spawnProtectionSeconds);
        put(sb, "switch", settings.randomClasses);
        put(sb, "switchsec", settings.randomClassCycleSeconds);
        put(sb, "lives", settings.startingLives);
        put(sb, "respawn", settings.respawnSeconds);
        put(sb, "gravity", settings.lowGravity);
        put(sb, "passive", settings.passiveHealing);
        put(sb, "frenzy", frenzy);
        put(sb, "time", timeOfDay);
        putClassList(sb);
        put(sb, "drops", join(settings.enabledDrops.toArray(new Enum[0])));
        return sb.toString();
    }

    private void putClassList(StringBuilder sb) {
        EnumSet<ClassType> enabled = EnumSet.noneOf(ClassType.class);
        for (ClassType type : settings.enabledClasses) {
            if (type.isPlayable()) enabled.add(type);
        }
        if (enabled.isEmpty()) return; // none enabled is the default list too (nothing to save)

        EnumSet<ClassType> disabled = EnumSet.copyOf(Arrays.asList(ClassType.getAvailableClasses()));
        EnumSet<ClassType> extra = EnumSet.copyOf(enabled);
        disabled.removeAll(enabled);
        extra.removeAll(Arrays.asList(ClassType.getAvailableClasses()));

        put(sb, "classesoff", join(disabled.toArray(new Enum[0])));
        put(sb, "classeson", join(extra.toArray(new Enum[0])));
    }

    public static GamePreset deserialize(String data) {
        GamePreset preset = new GamePreset();
        GameSettings s = preset.settings;
        for (String entry : data.split(";")) {
            int eq = entry.indexOf('=');
            if (eq < 0) continue;
            String key = entry.substring(0, eq), value = entry.substring(eq + 1);
            try {
                switch (key) {
                    case "drop": s.dropTimer = Integer.parseInt(value); break;
                    case "strength": s.strengthOnKillSeconds = Integer.parseInt(value); break;
                    case "healing": s.healthPotMode = HealthPotMode.valueOf(value); break;
                    case "hearts": s.maxHealthHearts = Integer.parseInt(value); break;
                    case "ofa": s.oneForAllMode = OneForAllMode.valueOf(value); break;
                    case "spawnprot": s.spawnProtectionSeconds = Integer.parseInt(value); break;
                    case "switch": s.randomClasses = Boolean.parseBoolean(value); break;
                    case "switchsec": s.randomClassCycleSeconds = Integer.parseInt(value); break;
                    case "lives": s.startingLives = Integer.parseInt(value); break;
                    case "respawn": s.respawnSeconds = Integer.parseInt(value); break;
                    case "gravity": s.lowGravity = Boolean.parseBoolean(value); break;
                    case "passive": s.passiveHealing = Boolean.parseBoolean(value); break;
                    case "frenzy": preset.frenzy = Boolean.parseBoolean(value); break;
                    case "time": preset.timeOfDay = Long.parseLong(value); break;
                    case "name": preset.name = cleanName(decode(value)); break;
                    case "classesoff": // the default classes it has disabled; the rest of the default list stays on
                        for (String className : split(value)) {
                            ClassType type = classByName(className);
                            if (type != null) s.enabledClasses.remove(type);
                        }
                        break;
                    case "classeson": // the classes outside the default list it has enabled (e.g. vaulted ones)
                        for (String className : split(value)) {
                            ClassType type = classByName(className);
                            if (type != null && type.isPlayable()) s.enabledClasses.add(type);
                        }
                        break;
                    case "classes": // a whole class list (how presets were first saved)
                        s.enabledClasses.clear();
                        for (String className : split(value)) {
                            ClassType type = classByName(className);
                            if (type != null && type.isPlayable()) s.enabledClasses.add(type);
                        }
                        break;
                    case "drops":
                        s.enabledDrops.clear();
                        for (String name : split(value)) {
                            try {
                                s.enabledDrops.add(GameLootDrops.valueOf(name));
                            } catch (IllegalArgumentException ignored) {
                                // renamed/removed item - skip it
                            }
                        }
                        break;
                    default: break;
                }
            } catch (IllegalArgumentException ignored) {
                // a bad value keeps that setting's default
            }
        }
        return preset;
    }

    /** Null for a renamed/removed class, which is skipped. */
    private static ClassType classByName(String name) {
        try {
            return ClassType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Names are stored URL-encoded so they can't break the ;-separated format or the database's character set
    private static String encode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e); // UTF-8 is always there
        }
    }

    private static String decode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void put(StringBuilder sb, String key, Object value) {
        if (sb.length() > 0) sb.append(';');
        sb.append(key).append('=').append(value);
    }

    private static String join(Enum<?>[] values) {
        StringBuilder sb = new StringBuilder();
        for (Enum<?> value : values) {
            if (sb.length() > 0) sb.append(',');
            sb.append(value.name());
        }
        return sb.toString();
    }

    private static String[] split(String value) {
        return value.isEmpty() ? new String[0] : value.split(",");
    }
}
