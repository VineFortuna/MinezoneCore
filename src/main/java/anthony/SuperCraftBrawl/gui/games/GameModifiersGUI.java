package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameSettings.HealthPotMode;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/*
 * Private game modifiers. Toggles flip on any click; cycling settings go forward on left click and
 * backward on right click.
 */
public class GameModifiersGUI implements InventoryProvider {

    private static final int[] DROP_TIMES = {5, 10, 15, 20, 25, 30, 45};
    private static final int[] CLASS_SWITCH_TIMES = {0, 15, 30, 45, 60}; // 0 = disabled
    private static final int[] LIVES = {1, 2, 3, 4, 5};
    private static final int[] RESPAWN_TIMES = {0, 1, 2, 3, 4, 5};

    private static final long DAY = 1000, SUNSET = 12000, NIGHT = 18000;
    private static final long[] TIMES_OF_DAY = {DAY, SUNSET, NIGHT};

    private static final String CYCLE_HINT = "&eLeft Click: next &7| &eRight Click: previous";

    private final Core main;
    private final GameInstance game;
    public final SmartInventory inv;

    public GameModifiersGUI(Core main, GameInstance game) {
        this.main = main;
        this.game = game;

        this.inv = SmartInventory.builder()
                .id("gameModifiers")
                .provider(this)
                .size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Game Modifiers")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem divider = ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {});
        for (int i = 0; i < 9; i++) {
            contents.set(1, i, divider);
        }

        if (game == null || game.state != GameState.WAITING) {
            contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&cUnavailable"),
                    "",
                    main.color("&7This game can no longer be modified.")
            ), e -> {}));
            return;
        }

        if (!game.isPrivateGameLeader(player)) {
            contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&cParty Leader Only"),
                    "",
                    main.color("&7Only the party leader can modify this game.")
            ), e -> {}));
            return;
        }

        // Top row
        addFrenzyModifier(contents, player);            // 0,0
        addClassSwitchModifier(contents, player);       // 0,1
        addOneForAllModifier(contents, player);         // 0,2
        addLowGravityModifier(contents, player);        // 0,5
        addStrengthOnKillModifier(contents, player);    // 0,6
        addHealthPotsModifier(contents, player);        // 0,7
        addLightningDropRateModifier(contents, player); // 0,8

        // Bottom row
        addClassListModifier(contents, player);         // 2,1
        addOpDropsModifier(contents, player);           // 2,2
        addLivesModifier(contents, player);             // 2,4
        addRespawnTimeModifier(contents, player);       // 2,5
        addSpawnProtectionModifier(contents, player);   // 2,6
        addTimeOfDayModifier(contents, player);         // 2,7
    }

    // ===== Top row =====

    private void addFrenzyModifier(InventoryContents contents, Player player) {
        if (game.gameType == GameType.DUEL) {
            contents.set(0, 0, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&c&lFrenzy Locked"),
                    "",
                    main.color("&7Duels cannot change game type.")
            ), e -> {}));
            return;
        }

        boolean enabled = game.gameType == GameType.FRENZY;
        contents.set(0, 0, ClickableItem.of(toggleItem(new ItemStack(Material.TNT), enabled, "&e&lFrenzy",
                "&7Everyone gets a random class",
                "&7on each life"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.gameType = game.gameType == GameType.FRENZY ? GameType.CLASSIC : GameType.FRENZY;
            game.TellAll(main.color("&2&l(!) &rFrenzy has been " + status(game.gameType == GameType.FRENZY)));
            updateModeOnBoard();

            inv.open(player);
        }));
    }

    private void addClassSwitchModifier(InventoryContents contents, Player player) {
        GameSettings settings = game.getGameSettings();
        int current = settings.randomClasses ? settings.randomClassCycleSeconds : 0;

        contents.set(0, 1, ClickableItem.of(cycleItem(new ItemStack(Material.EYE_OF_ENDER), current > 0,
                "&e&lClass Switch",
                "&fCurrent: &e" + formatClassSwitch(current),
                new String[]{"&7Everyone switches to a new", "&7random class every so often"},
                "&e15s&7, &e30s&7, &e45s&7, &e1m&7, &cDisabled"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = cycle(CLASS_SWITCH_TIMES, current, isForward(e));
            settings.randomClasses = next > 0;
            if (next > 0) settings.randomClassCycleSeconds = next;

            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rClass Switch has been &aenabled&r. Classes will change every &e&l"
                        + formatClassSwitch(next)));
            } else {
                game.TellAll(main.color("&2&l(!) &rClass Switch has been &cdisabled"));
            }

            inv.open(player);
        }));
    }

    private void addOneForAllModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().oneForAll;

        contents.set(0, 2, ClickableItem.of(toggleItem(new ItemStack(Material.ARMOR_STAND), enabled, "&e&lOne For All",
                "&7All players play with",
                "&7the same class"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().oneForAll = !game.getGameSettings().oneForAll;
            game.TellAll(main.color("&2&l(!) &rOne For All has been " + status(game.getGameSettings().oneForAll)));

            inv.open(player);
        }));
    }

    private void addLowGravityModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().lowGravity;

        contents.set(0, 5, ClickableItem.of(toggleItem(new ItemStack(Material.FEATHER), enabled, "&e&lLow Gravity",
                "&7Everyone gets permanent",
                "&bJump Boost X &7while alive"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().lowGravity = !game.getGameSettings().lowGravity;
            game.TellAll(main.color("&2&l(!) &rLow Gravity has been " + status(game.getGameSettings().lowGravity)));

            inv.open(player);
        }));
    }

    private void addStrengthOnKillModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().strengthOnKill;

        contents.set(0, 6, ClickableItem.of(toggleItem(new ItemStack(Material.BLAZE_POWDER), enabled, "&e&lStrength on Kill",
                "&7Gain &c5 seconds of Strength I",
                "&7after every kill."
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().strengthOnKill = !game.getGameSettings().strengthOnKill;
            game.TellAll(main.color("&2&l(!) &rStrength on Kill has been " + status(game.getGameSettings().strengthOnKill)));

            inv.open(player);
        }));
    }

    private void addHealthPotsModifier(InventoryContents contents, Player player) {
        HealthPotMode mode = game.getGameSettings().healthPotMode;

        // Splash Potion of Instant Health
        ItemStack potion = ItemHelper.setHideFlags(new ItemStack(Material.POTION, 1, (short) 16421), true);

        contents.set(0, 7, ClickableItem.of(cycleItem(potion, mode != HealthPotMode.DISABLED,
                "&e&lHealth Pots",
                "&fCurrent: &e" + mode.getName(),
                new String[]{"&7When killers are given a", "&7health pot:",
                        "&cDisabled&7: never", "&eHalf Health&7: at or below half health", "&aAlways&7: every kill"},
                "&cDisabled&7, &eHalf Health&7, &aAlways"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            HealthPotMode[] modes = HealthPotMode.values();
            int step = isForward(e) ? 1 : modes.length - 1;
            HealthPotMode next = modes[(mode.ordinal() + step) % modes.length];
            game.getGameSettings().healthPotMode = next;

            game.TellAll(main.color("&2&l(!) &rHealth Pots were set to &e&l" + next.getName() + "&r."));

            inv.open(player);
        }));
    }

    private void addLightningDropRateModifier(InventoryContents contents, Player player) {
        int current = game.getGameSettings().dropTimer;

        contents.set(0, 8, ClickableItem.of(cycleItem(new ItemStack(Material.NETHER_STAR), false,
                "&e&lLightning Drop Rate",
                "&fCurrent: &eEvery " + current + "s",
                new String[]{"&7How often lightning drops", "&7spawn on the map"},
                "&e5s&7, &e10s&7, &e15s&7, &e20s&7, &e25s&7, &e30s&7, &e45s"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().dropTimer = cycle(DROP_TIMES, current, isForward(e));
            game.TellAll(main.color("&2&l(!) &rLightning drops will now spawn every &e&l"
                    + game.getGameSettings().dropTimer + "s&r."));

            inv.open(player);
        }));
    }

    // ===== Bottom row =====

    private void addClassListModifier(InventoryContents contents, Player player) {
        int enabled = 0;
        int vaulted = 0;
        for (ClassType type : game.getGameSettings().enabledClasses) {
            if (!type.isPlayable()) continue;
            enabled++;
            if (type.isVaulted()) vaulted++;
        }

        contents.set(2, 1, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.BOOK_AND_QUILL),
                main.color("&e&lClass List"),
                "",
                main.color("&fEnabled: &a" + enabled + "&7/&a" + ClassType.getPlayableClasses().length + " &7classes"),
                main.color("&fVaulted Enabled: &a" + vaulted),
                "",
                main.color("&7Choose which classes players"),
                main.color("&7can pick, including vaulted ones"),
                "",
                main.color("&eClick to open")
        ), e -> {
            SoundManager.playClickSound(player);
            new ClassListGUI(main, game, this).inv.open(player);
        }));
    }

    private void addOpDropsModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().opDrops;

        contents.set(2, 2, ClickableItem.of(toggleItem(new ItemStack(Material.WHEAT), enabled, "&e&lOP Item Drops",
                "&7Lightning drops only spawn",
                "&6&lLEGENDARY &7and &d&lRARE &7items.",
                "&7(Extra Lives are removed)"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().opDrops = !game.getGameSettings().opDrops;
            game.TellAll(main.color("&2&l(!) &rOP Drops have been " + status(game.getGameSettings().opDrops)));

            inv.open(player);
        }));
    }

    private void addLivesModifier(InventoryContents contents, Player player) {
        int lives = game.getGameSettings().startingLives;

        contents.set(2, 4, ClickableItem.of(cycleItem(new ItemStack(Material.PRISMARINE_SHARD), false,
                "&e&lLives",
                "&fCurrent: &e" + formatLives(lives),
                new String[]{"&7How many lives everyone", "&7starts with"},
                "&e1&7, &e2&7, &e3&7, &e4&7, &e5"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().startingLives = cycle(LIVES, lives, isForward(e));
            game.TellAll(main.color("&2&l(!) &rEveryone will now start with &e&l"
                    + formatLives(game.getGameSettings().startingLives) + "&r."));

            inv.open(player);
        }));
    }

    private void addRespawnTimeModifier(InventoryContents contents, Player player) {
        int seconds = game.getGameSettings().respawnSeconds;

        contents.set(2, 5, ClickableItem.of(cycleItem(new ItemStack(Material.DIODE), false,
                "&e&lRespawn Time",
                "&fCurrent: &e" + formatRespawnTime(seconds),
                new String[]{"&7How long players wait", "&7before respawning"},
                "&eNone&7, &e1s&7, &e2s&7, &e3s&7, &e4s&7, &e5s"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = cycle(RESPAWN_TIMES, seconds, isForward(e));
            game.getGameSettings().respawnSeconds = next;

            if (next == 0) {
                game.TellAll(main.color("&2&l(!) &rPlayers will now respawn &e&linstantly&r."));
            } else {
                game.TellAll(main.color("&2&l(!) &rRespawn time was set to &e&l" + next + "s&r."));
            }

            inv.open(player);
        }));
    }

    private void addSpawnProtectionModifier(InventoryContents contents, Player player) {
        boolean enabled = !game.getGameSettings().disableSpawnProtection;

        contents.set(2, 6, ClickableItem.of(toggleItem(new ItemStack(Material.IRON_CHESTPLATE), enabled, "&e&lSpawn Protection",
                "&7Players get spawn protection",
                "&7when the game starts and",
                "&7after respawning"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().disableSpawnProtection = !game.getGameSettings().disableSpawnProtection;
            game.TellAll(main.color("&2&l(!) &rSpawn Protection has been "
                    + status(!game.getGameSettings().disableSpawnProtection)));

            inv.open(player);
        }));
    }

    private void addTimeOfDayModifier(InventoryContents contents, Player player) {
        long current = getTimeOfDay();

        contents.set(2, 7, ClickableItem.of(cycleItem(new ItemStack(Material.WATCH), current != DAY,
                "&e&lTime of Day",
                "&fCurrent: " + formatTimeOfDay(current),
                new String[]{"&7The time of day on the map"},
                "&eDay&7, &6Sunset&7, &cNight"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int index = indexOf(TIMES_OF_DAY, current);
            int step = isForward(e) ? 1 : TIMES_OF_DAY.length - 1;
            long next = TIMES_OF_DAY[(index + step) % TIMES_OF_DAY.length];
            game.getMapWorld().setTime(next);

            game.TellAll(main.color("&2&l(!) &rPrivate game time was set to " + formatTimeOfDay(next) + "&r."));

            inv.open(player);
        }));
    }

    // ===== Helpers =====

    private ItemStack toggleItem(ItemStack item, boolean enabled, String name, String... description) {
        if (enabled) item = ItemHelper.setGlowing(item, true);

        String[] lore = new String[description.length + 5];
        lore[0] = "";
        lore[1] = main.color("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled"));
        lore[2] = "";
        for (int i = 0; i < description.length; i++) lore[3 + i] = main.color(description[i]);
        lore[3 + description.length] = "";
        lore[4 + description.length] = main.color("&eClick to toggle");

        return ItemHelper.setDetails(item, main.color(name), lore);
    }

    private ItemStack cycleItem(ItemStack item, boolean glowing, String name, String current, String[] description,
                                String options) {
        if (glowing) item = ItemHelper.setGlowing(item, true);

        String[] lore = new String[description.length + 8];
        lore[0] = "";
        lore[1] = main.color(current);
        lore[2] = "";
        for (int i = 0; i < description.length; i++) lore[3 + i] = main.color(description[i]);
        int i = 3 + description.length;
        lore[i] = "";
        lore[i + 1] = main.color("&7Options:");
        lore[i + 2] = main.color(options);
        lore[i + 3] = "";
        lore[i + 4] = main.color(CYCLE_HINT);

        return ItemHelper.setDetails(item, main.color(name), lore);
    }

    private static boolean isForward(InventoryClickEvent e) {
        return !e.isRightClick();
    }

    /** The value after (or before) current in values, wrapping around. Unknown values start from the first one. */
    private static int cycle(int[] values, int current, boolean forward) {
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) index = i;
        }
        if (index < 0) return values[0];

        int step = forward ? 1 : values.length - 1;
        return values[(index + step) % values.length];
    }

    private static int indexOf(long[] values, long value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) return i;
        }
        return 0;
    }

    private String status(boolean enabled) {
        return enabled ? "&aenabled" : "&cdisabled";
    }

    private long getTimeOfDay() {
        long time = game.getMapWorld().getTime();
        if (time >= 13000 && time <= 23000) return NIGHT;
        if (time >= 11000 && time < 13000) return SUNSET;
        return DAY;
    }

    private String formatTimeOfDay(long time) {
        if (time == NIGHT) return "&c&lNight";
        if (time == SUNSET) return "&6&lSunset";
        return "&e&lDay";
    }

    private String formatClassSwitch(int seconds) {
        if (seconds <= 0) return "&cDisabled";
        return seconds >= 60 ? "1m" : seconds + "s";
    }

    private String formatLives(int lives) {
        return lives + (lives == 1 ? " Life" : " Lives");
    }

    private String formatRespawnTime(int seconds) {
        return seconds <= 0 ? "None" : seconds + "s";
    }

    private void updateModeOnBoard() {
        for (Player gamePlayer : game.players) {
            FastBoard board = game.boards.get(gamePlayer.getUniqueId());

            if (board != null) {
                board.updateLine(1, game.color("&fMode: &a" + game.gameType.getName()));
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
