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
 * Private game modifiers. Toggles flip on any click; cycling settings go back on left click and
 * forward on right click (Left Click << | >> Right Click).
 */
public class GameModifiersGUI implements InventoryProvider {

    private static final int[] DROP_TIMES = {5, 10, 15, 20, 25, 30, 45};
    private static final String[] DROP_TIME_LABELS = {"5s", "10s", "15s", "20s", "25s", "30s", "45s"};
    private static final int[] CLASS_SWITCH_TIMES = {0, 15, 30, 45, 60}; // 0 = disabled
    private static final String[] CLASS_SWITCH_LABELS = {"Disabled", "15s", "30s", "45s", "1m"};
    private static final int[] LIVES = {1, 2, 3, 4, 5, 10};
    private static final String[] LIVES_LABELS = {"1", "2", "3", "4", "5", "10"};
    private static final int[] RESPAWN_TIMES = {0, 1, 2, 3, 4, 5, 10};
    private static final String[] RESPAWN_TIME_LABELS = {"None", "1s", "2s", "3s", "4s", "5s", "10s"};
    private static final int[] SPAWN_PROTECTION_TIMES = {0, 3, 5}; // 0 = disabled
    private static final String[] SPAWN_PROTECTION_LABELS = {"Disabled", "3s", "5s"};
    private static final int[] STRENGTH_TIMES = {0, 5, 10}; // 0 = disabled
    private static final String[] STRENGTH_LABELS = {"Disabled", "5s", "10s"};
    private static final int[] MAX_HEALTH_HEARTS = {10, 15, 20};
    private static final String[] MAX_HEALTH_LABELS = {"10 Hearts", "15 Hearts", "20 Hearts"};

    private static final long DAY = 1000, SUNSET = 12500, NIGHT = 18000;
    private static final long[] TIMES_OF_DAY = {DAY, SUNSET, NIGHT};
    private static final String[] TIME_OF_DAY_LABELS = {"Day", "Sunset", "Night"};

    private static final String CYCLE_HINT = "&eLeft Click &7<< &8| &7>> &eRight Click";

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
        addLowGravityModifier(contents, player);        // 0,4
        addStrengthOnKillModifier(contents, player);    // 0,5
        addHealingOnKillModifier(contents, player);     // 0,6
        addMaxHealthModifier(contents, player);         // 0,7
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
        int index = indexOf(CLASS_SWITCH_TIMES, settings.randomClasses ? settings.randomClassCycleSeconds : 0);
        int seconds = CLASS_SWITCH_TIMES[index];

        contents.set(0, 1, ClickableItem.of(cycleItem(new ItemStack(Material.EYE_OF_ENDER), "&e&lClass Switch",
                CLASS_SWITCH_LABELS, index, seconds, seconds > 0,
                "&7Everyone switches to a new", "&7random class every so often"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = step(index, CLASS_SWITCH_TIMES.length, e);
            int nextSeconds = CLASS_SWITCH_TIMES[next];
            settings.randomClasses = nextSeconds > 0;
            if (nextSeconds > 0) settings.randomClassCycleSeconds = nextSeconds;

            if (nextSeconds > 0) {
                game.TellAll(main.color("&2&l(!) &rClass Switch has been &aenabled&r. Classes will change every &e&l"
                        + CLASS_SWITCH_LABELS[next]));
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

        contents.set(0, 4, ClickableItem.of(toggleItem(new ItemStack(Material.FEATHER), enabled, "&e&lLow Gravity",
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
        int index = indexOf(STRENGTH_TIMES, game.getGameSettings().strengthOnKillSeconds);
        int seconds = STRENGTH_TIMES[index];

        contents.set(0, 5, ClickableItem.of(cycleItem(new ItemStack(Material.BLAZE_POWDER), "&e&lStrength on Kill",
                STRENGTH_LABELS, index, seconds, seconds > 0,
                "&7Killers gain &cStrength I", "&7for a few seconds after every kill"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = STRENGTH_TIMES[step(index, STRENGTH_TIMES.length, e)];
            game.getGameSettings().strengthOnKillSeconds = next;

            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rStrength on Kill has been &aenabled&r: &e&l" + next
                        + "s &rof Strength I after every kill."));
            } else {
                game.TellAll(main.color("&2&l(!) &rStrength on Kill has been &cdisabled"));
            }

            inv.open(player);
        }));
    }

    private void addHealingOnKillModifier(InventoryContents contents, Player player) {
        HealthPotMode mode = game.getGameSettings().healthPotMode;
        HealthPotMode[] modes = HealthPotMode.values();
        String[] labels = new String[modes.length];
        for (int i = 0; i < modes.length; i++) labels[i] = modes[i].getName();

        // Splash Potion of Instant Health
        ItemStack potion = ItemHelper.setHideFlags(new ItemStack(Material.POTION, 1, (short) 16421), true);

        contents.set(0, 6, ClickableItem.of(cycleItem(potion, "&e&lHealing on Kill",
                labels, mode.ordinal(), 1, mode != HealthPotMode.DISABLED,
                "&7How killers get healed",
                "&7after getting a kill"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            HealthPotMode next = modes[step(mode.ordinal(), modes.length, e)];
            game.getGameSettings().healthPotMode = next;
            game.TellAll(main.color("&2&l(!) &rHealing on Kill was set to &e&l" + next.getName() + "&r."));

            inv.open(player);
        }));
    }

    private void addMaxHealthModifier(InventoryContents contents, Player player) {
        int index = indexOf(MAX_HEALTH_HEARTS, game.getGameSettings().maxHealthHearts);

        contents.set(0, 7, ClickableItem.of(cycleItem(new ItemStack(Material.GOLDEN_APPLE), "&e&lMax Health",
                MAX_HEALTH_LABELS, index, MAX_HEALTH_HEARTS[index], true,
                "&7How much health everyone", "&7has on each life"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = step(index, MAX_HEALTH_HEARTS.length, e);
            game.getGameSettings().maxHealthHearts = MAX_HEALTH_HEARTS[next];
            game.TellAll(main.color("&2&l(!) &rMax Health was set to &e&l" + MAX_HEALTH_LABELS[next] + "&r."));

            inv.open(player);
        }));
    }

    private void addLightningDropRateModifier(InventoryContents contents, Player player) {
        int index = indexOf(DROP_TIMES, game.getGameSettings().dropTimer);

        contents.set(0, 8, ClickableItem.of(cycleItem(new ItemStack(Material.NETHER_STAR), "&e&lLightning Drop Rate",
                DROP_TIME_LABELS, index, DROP_TIMES[index], true,
                "&7How often lightning drops", "&7spawn on the map"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().dropTimer = DROP_TIMES[step(index, DROP_TIMES.length, e)];
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
                "&7(No Extra Lives)"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().opDrops = !game.getGameSettings().opDrops;
            game.TellAll(main.color("&2&l(!) &rOP Drops have been " + status(game.getGameSettings().opDrops)));

            inv.open(player);
        }));
    }

    private void addLivesModifier(InventoryContents contents, Player player) {
        int index = indexOf(LIVES, game.getGameSettings().startingLives);

        contents.set(2, 4, ClickableItem.of(cycleItem(new ItemStack(Material.PRISMARINE_SHARD), "&e&lLives",
                LIVES_LABELS, index, LIVES[index], true,
                "&7How many lives everyone", "&7starts with"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().startingLives = LIVES[step(index, LIVES.length, e)];
            game.TellAll(main.color("&2&l(!) &rEveryone will now start with &e&l"
                    + formatLives(game.getGameSettings().startingLives) + "&r."));

            inv.open(player);
        }));
    }

    private void addRespawnTimeModifier(InventoryContents contents, Player player) {
        int index = indexOf(RESPAWN_TIMES, game.getGameSettings().respawnSeconds);
        int seconds = RESPAWN_TIMES[index];

        contents.set(2, 5, ClickableItem.of(cycleItem(new ItemStack(Material.DIODE), "&e&lRespawn Time",
                RESPAWN_TIME_LABELS, index, seconds, seconds > 0,
                "&7How long players wait", "&7before respawning"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = RESPAWN_TIMES[step(index, RESPAWN_TIMES.length, e)];
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
        int index = indexOf(SPAWN_PROTECTION_TIMES, game.getGameSettings().spawnProtectionSeconds);
        int seconds = SPAWN_PROTECTION_TIMES[index];

        contents.set(2, 6, ClickableItem.of(cycleItem(new ItemStack(Material.IRON_CHESTPLATE), "&e&lSpawn Protection",
                SPAWN_PROTECTION_LABELS, index, seconds, seconds > 0,
                "&7How long players are protected", "&7when the game starts and", "&7after respawning"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = SPAWN_PROTECTION_TIMES[step(index, SPAWN_PROTECTION_TIMES.length, e)];
            game.getGameSettings().spawnProtectionSeconds = next;

            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rSpawn Protection was set to &e&l" + next + "s&r."));
            } else {
                game.TellAll(main.color("&2&l(!) &rSpawn Protection has been &cdisabled"));
            }

            inv.open(player);
        }));
    }

    private void addTimeOfDayModifier(InventoryContents contents, Player player) {
        int index = indexOf(TIMES_OF_DAY, getTimeOfDay());

        contents.set(2, 7, ClickableItem.of(cycleItem(new ItemStack(Material.WATCH), "&e&lTime of Day",
                TIME_OF_DAY_LABELS, index, 1, true,
                "&7The time of day on the map"
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            int next = step(index, TIMES_OF_DAY.length, e);
            game.getMapWorld().setTime(TIMES_OF_DAY[next]);
            game.TellAll(main.color("&2&l(!) &rPrivate game time was set to " + formatTimeOfDay(TIMES_OF_DAY[next]) + "&r."));

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

    /*
     * A cycling setting: the current option in green ("Current: ..." and in the options list, others yellow),
     * the icon's stack size matching the option's number, and enchanted unless the option is an "off" one.
     */
    private ItemStack cycleItem(ItemStack item, String name, String[] options, int current, int amount, boolean active,
                                String... description) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        if (active) item = ItemHelper.setGlowing(item, true);

        StringBuilder optionList = new StringBuilder();
        for (int i = 0; i < options.length; i++) {
            if (i > 0) optionList.append("&7, ");
            optionList.append(i == current ? "&a" : "&e").append(options[i]);
        }

        String[] lore = new String[description.length + 8];
        lore[0] = "";
        lore[1] = main.color("&fCurrent: &a" + options[current]);
        lore[2] = "";
        for (int i = 0; i < description.length; i++) lore[3 + i] = main.color(description[i]);
        int i = 3 + description.length;
        lore[i] = "";
        lore[i + 1] = main.color("&7Options:");
        lore[i + 2] = main.color(optionList.toString());
        lore[i + 3] = "";
        lore[i + 4] = main.color(CYCLE_HINT);

        return ItemHelper.setDetails(item, main.color(name), lore);
    }

    /** The option index after a click: left click goes back, right click goes forward (matching the << | >> hint). */
    private static int step(int index, int count, InventoryClickEvent e) {
        int delta = e.isRightClick() ? 1 : count - 1;
        return (index + delta) % count;
    }

    /** Index of value in values, or 0 if it isn't one of them. */
    private static int indexOf(int[] values, int value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) return i;
        }
        return 0;
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

    private String formatLives(int lives) {
        return lives + (lives == 1 ? " Life" : " Lives");
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
