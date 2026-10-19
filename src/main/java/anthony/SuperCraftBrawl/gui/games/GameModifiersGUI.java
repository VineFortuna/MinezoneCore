package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/*
 * Game Settings (a custom game's modifiers). Toggles flip on any click; cycling settings go back on left click and
 * forward on right click (Left Click << | >> Right Click).
 */
public class GameModifiersGUI implements InventoryProvider {

    private static final int[] DROP_TIMES = {0, 5, 10, 15, 20, 25, 30, 45}; // 0 = disabled
    private static final String[] DROP_TIME_LABELS = {"Disabled", "5s", "10s", "15s", "20s", "25s", "30s", "45s"};
    private static final int[] CLASS_SWITCH_TIMES = {0, 15, 30, 45, 60}; // 0 = disabled
    private static final String[] CLASS_SWITCH_LABELS = {"Disabled", "15s", "30s", "45s", "1m"};
    private static final int[] LIVES = {1, 2, 3, 4, 5, 10, 20, 50};
    private static final String[] LIVES_LABELS = {"1", "2", "3", "4", "5", "10", "20", "50"};
    private static final int[] RESPAWN_TIMES = {0, 1, 2, 3, 4, 5, 10};
    private static final String[] RESPAWN_TIME_LABELS = {"None", "1s", "2s", "3s", "4s", "5s", "10s"};
    private static final int[] SPAWN_PROTECTION_TIMES = {0, 3, 5}; // 0 = disabled
    private static final String[] SPAWN_PROTECTION_LABELS = {"Disabled", "3s", "5s"};
    private static final int[] STRENGTH_TIMES = {0, 5, 10, 20, 30}; // 0 = disabled
    private static final String[] STRENGTH_LABELS = {"Disabled", "5s", "10s", "20s", "30s"};
    private static final int[] MAX_HEALTH_HEARTS = {1, 3, 5, 10, 15, 20};
    private static final String[] MAX_HEALTH_LABELS = {"1 Heart", "3 Hearts", "5 Hearts", "10 Hearts", "15 Hearts", "20 Hearts"};

    private static final long DAY = 1000, SUNSET = 12500, NIGHT = 18000;
    private static final long[] TIMES_OF_DAY = {DAY, SUNSET, NIGHT};
    private static final String[] TIME_OF_DAY_LABELS = {"Day", "Sunset", "Night"};

    private static final String CYCLE_HINT = "&7<< &eLeft Click &8| &eRight Click &7>>";
    private static final int OPTIONS_LINE_LENGTH = 40; // options lists longer than this wrap onto another line

    // Class selection modifiers: only one can be active per game (none = Classic)
    private static final String FRENZY = "Frenzy", CLASS_SWITCH = "Class Switch", ONE_FOR_ALL = "One For All";

    private final Core main;
    private final GameInstance game;
    public final SmartInventory inv;

    public GameModifiersGUI(Core main, GameInstance game) {
        this.main = main;
        this.game = game;

        this.inv = SmartInventory.builder()
                .id("gameModifiers")
                .provider(this)
                .size(5, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Game Settings")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem divider = ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {});
        for (int i = 0; i < 9; i++) {
            contents.set(1, i, divider);
            contents.set(3, i, divider);
        }

        if (game == null || game.state != GameState.WAITING) {
            contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&cUnavailable"),
                    "",
                    main.color("&7This game can no longer be modified.")
            ), e -> SoundManager.playErrorSound(player)));
            return;
        }

        if (!game.canEditCustomGame(player)) {
            contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&cHost Only"),
                    "",
                    main.color("&7Only this custom game's host and"),
                    main.color("&7co-host can modify it.")
            ), e -> SoundManager.playErrorSound(player)));
            return;
        }

        if (game.isCustomGameCountdownStarted()) {
            contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&cCountdown Started"),
                    "",
                    main.color("&7The host can stop the countdown"),
                    main.color("&7with the Game Settings item.")
            ), e -> SoundManager.playErrorSound(player)));
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
        addClassListModifier(contents, player);         // 2,0
        addItemListModifier(contents, player);          // 2,1
        addOpDropsModifier(contents, player);           // 2,2
        addLivesModifier(contents, player);             // 2,4
        addRespawnTimeModifier(contents, player);       // 2,5
        addSpawnProtectionModifier(contents, player);   // 2,6
        addTimeOfDayModifier(contents, player);         // 2,7

        // Last row
        addVisibilityModifier(contents, player);        // 4,3
        addKickPlayerModifier(contents, player);        // 4,4
        addCoHostModifier(contents, player);            // 4,5
    }

    // ===== Last row =====

    private void addVisibilityModifier(InventoryContents contents, Player player) {
        boolean isPublic = game.isCustomGamePublic();

        ItemStack item = ItemHelper.setDetails(new ItemStack(isPublic ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK),
                main.color("&e&lVisibility"),
                "",
                main.color("&fCurrent: " + (isPublic ? "&aPublic" : "&cPrivate")),
                "",
                main.color(isPublic ? "&7Anyone can join this game from" : "&7Only the host's party can join"),
                main.color(isPublic ? "&7the Custom Games NPC" : "&7this game"),
                "",
                main.color("&eClick to make it " + (isPublic ? "&cprivate" : "&apublic")));

        contents.set(4, 3, ClickableItem.of(item, e -> {
            if (!canModify(player)) return;
            SoundManager.playClickSound(player);

            game.setCustomGamePublic(!isPublic);
            game.TellAll(main.color("&2&l(!) &rThis custom game is now " + (!isPublic
                    ? "&apublic&r - anyone can join it from the Custom Games NPC." : "&cprivate&r - only the host's party can join it.")));
            if (!isPublic) announcePublicGame();
            CustomGamesGUI.refreshOpenMenus(); // the game shows up / changes block for anyone looking

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    /** A custom game just went public: everyone in the lobby hears about it, with a click to join. */
    private void announcePublicGame() {
        String host = game.getCustomGameHostName();
        String message = main.color("&6&l(!) &e" + host + " &rcreated a &6&lCUSTOM &rgame!");
        TextComponent join = new TextComponent(main.color("     &a&lClick here to Join"));
        join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/joincustomgame " + host));

        for (Player p : main.getLobbyWorld().getPlayers()) {
            p.sendMessage(message);
            p.spigot().sendMessage(join);
        }
    }

    private void addCoHostModifier(InventoryContents contents, Player player) {
        Player coHost = game.getCustomGameCoHost() == null ? null : Bukkit.getPlayer(game.getCustomGameCoHost());
        boolean isHost = game.isCustomGameHost(player);

        ItemStack head = coHost != null ? ItemHelper.createSkullHeadPlayer(1, coHost.getName())
                : ItemHelper.createSkullHead(1, SkullType.PLAYER);
        head = ItemHelper.setDetails(head,
                main.color("&e&lCo-Host"),
                "",
                main.color("&fCurrent: " + (coHost != null ? "&a" + coHost.getName() : "&7None")),
                "",
                main.color("&7A player who can change the"),
                main.color("&7Game Settings with the host"),
                "",
                main.color(isHost ? "&eClick to choose" : "&cOnly the host can choose the co-host"));

        contents.set(4, 5, ClickableItem.of(head, e -> {
            if (!isHost) {
                SoundManager.playErrorSound(player);
                return;
            }
            if (!canModify(player)) return;

            SoundManager.playClickSound(player);
            new CoHostGUI(main, game, this).inv.open(player);
        }));
    }

    private void addKickPlayerModifier(InventoryContents contents, Player player) {
        ItemStack item = ItemHelper.setDetails(new ItemStack(Material.IRON_DOOR),
                main.color("&e&lKick Player"),
                "",
                main.color("&7Remove a player from this game."),
                main.color("&7They can't join it again."),
                "",
                main.color("&eClick to choose"));

        contents.set(4, 4, ClickableItem.of(item, e -> {
            if (!canModify(player)) return;
            SoundManager.playClickSound(player);
            new KickPlayerGUI(main, game, this).inv.open(player);
        }));
    }

    // ===== Top row =====

    private void addFrenzyModifier(InventoryContents contents, Player player) {
        if (game.gameType == GameType.DUEL) {
            contents.set(0, 0, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&c&lFrenzy Locked"),
                    "",
                    main.color("&7Duels cannot change game type.")
            ), e -> SoundManager.playErrorSound(player)));
            return;
        }

        boolean enabled = game.gameType == GameType.FRENZY;
        contents.set(0, 0, ClickableItem.of(classSelectionTag(toggleItem(new ItemStack(Material.TNT), enabled, "&e&lFrenzy",
                "&7Everyone gets a random class",
                "&7on each life"
        )), e -> {
            if (!enabled && isOtherClassSelectionModifierActive(player, FRENZY)) return;
            SoundManager.playClickSound(player);

            game.gameType = game.gameType == GameType.FRENZY ? GameType.CLASSIC : GameType.FRENZY;
            game.TellAll(main.color("&2&l(!) &rFrenzy has been " + status(game.gameType == GameType.FRENZY)));
            updateModeOnBoard();

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addClassSwitchModifier(InventoryContents contents, Player player) {
        GameSettings settings = game.getGameSettings();
        int index = indexOf(CLASS_SWITCH_TIMES, settings.randomClasses ? settings.randomClassCycleSeconds : 0);
        int seconds = CLASS_SWITCH_TIMES[index];

        contents.set(0, 1, ClickableItem.of(classSelectionTag(cycleItem(new ItemStack(Material.EYE_OF_ENDER), "&e&lClass Switch",
                CLASS_SWITCH_LABELS, index, seconds, seconds > 0,
                "&7Everyone starts with a random class", "&7and switches to a new random", "&7class every so often"
        )), e -> {
            int next = step(index, CLASS_SWITCH_TIMES.length, e);
            int nextSeconds = CLASS_SWITCH_TIMES[next];
            if (seconds == 0 && nextSeconds > 0 && isOtherClassSelectionModifierActive(player, CLASS_SWITCH)) return;
            SoundManager.playClickSound(player);

            settings.randomClasses = nextSeconds > 0;
            if (nextSeconds > 0) settings.randomClassCycleSeconds = nextSeconds;

            if (nextSeconds > 0) {
                game.TellAll(main.color("&2&l(!) &rClass Switch has been &aenabled&r. Classes will change every &e&l"
                        + CLASS_SWITCH_LABELS[next]));
            } else {
                game.TellAll(main.color("&2&l(!) &rClass Switch has been &cdisabled"));
            }

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addOneForAllModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().oneForAll;

        contents.set(0, 2, ClickableItem.of(classSelectionTag(toggleItem(new ItemStack(Material.ARMOR_STAND), enabled, "&e&lOne For All",
                "&7All players play with",
                "&7the same class"
        )), e -> {
            if (!enabled && isOtherClassSelectionModifierActive(player, ONE_FOR_ALL)) return;
            SoundManager.playClickSound(player);

            game.getGameSettings().oneForAll = !game.getGameSettings().oneForAll;
            game.TellAll(main.color("&2&l(!) &rOne For All has been " + status(game.getGameSettings().oneForAll)));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addLowGravityModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().lowGravity;

        contents.set(0, 4, ClickableItem.of(toggleItem(new ItemStack(Material.FEATHER), enabled, "&e&lLow Gravity",
                "&7Everyone gets permanent",
                "&bJump Boost X &7while alive"
        ), e -> {
            SoundManager.playClickSound(player);

            game.getGameSettings().lowGravity = !game.getGameSettings().lowGravity;
            game.TellAll(main.color("&2&l(!) &rLow Gravity has been " + status(game.getGameSettings().lowGravity)));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addStrengthOnKillModifier(InventoryContents contents, Player player) {
        int index = indexOf(STRENGTH_TIMES, game.getGameSettings().strengthOnKillSeconds);
        int seconds = STRENGTH_TIMES[index];

        contents.set(0, 5, ClickableItem.of(cycleItem(new ItemStack(Material.BLAZE_POWDER), "&e&lStrength on Kill",
                STRENGTH_LABELS, index, seconds, seconds > 0,
                "&7Killers gain &cStrength I", "&7for a few seconds after every kill"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = STRENGTH_TIMES[step(index, STRENGTH_TIMES.length, e)];
            game.getGameSettings().strengthOnKillSeconds = next;

            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rStrength on Kill has been &aenabled&r: &e&l" + next
                        + "s &rof Strength I after every kill."));
            } else {
                game.TellAll(main.color("&2&l(!) &rStrength on Kill has been &cdisabled"));
            }

            inv.open(player);
            game.refreshEditorMenus(player);
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
            SoundManager.playClickSound(player);

            HealthPotMode next = modes[step(mode.ordinal(), modes.length, e)];
            game.getGameSettings().healthPotMode = next;
            game.TellAll(main.color("&2&l(!) &rHealing on Kill was set to &e&l" + next.getName() + "&r."));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addMaxHealthModifier(InventoryContents contents, Player player) {
        int index = indexOf(MAX_HEALTH_HEARTS, game.getGameSettings().maxHealthHearts);

        contents.set(0, 7, ClickableItem.of(cycleItem(new ItemStack(Material.GOLDEN_APPLE), "&e&lMax Health",
                MAX_HEALTH_LABELS, index, MAX_HEALTH_HEARTS[index], false,
                "&7How much health everyone", "&7has on each life"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = step(index, MAX_HEALTH_HEARTS.length, e);
            game.getGameSettings().maxHealthHearts = MAX_HEALTH_HEARTS[next];
            game.TellAll(main.color("&2&l(!) &rMax Health was set to &e&l" + MAX_HEALTH_LABELS[next] + "&r."));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addLightningDropRateModifier(InventoryContents contents, Player player) {
        int index = indexOf(DROP_TIMES, game.getGameSettings().dropTimer);
        int seconds = DROP_TIMES[index];

        contents.set(0, 8, ClickableItem.of(cycleItem(new ItemStack(Material.NETHER_STAR), "&e&lLightning Drop Rate",
                DROP_TIME_LABELS, index, seconds, seconds > 0,
                "&7How often lightning drops", "&7spawn on the map"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = DROP_TIMES[step(index, DROP_TIMES.length, e)];
            game.getGameSettings().dropTimer = next;
            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rLightning drops will now spawn every &e&l" + next + "s&r."));
            } else {
                game.TellAll(main.color("&2&l(!) &rLightning drops have been &cdisabled"));
            }

            inv.open(player);
            game.refreshEditorMenus(player);
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

        contents.set(2, 0, ClickableItem.of(ItemHelper.setDetails(
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

    private void addItemListModifier(InventoryContents contents, Player player) {
        int enabled = game.getGameSettings().enabledDrops.size();

        contents.set(2, 1, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.BOOK_AND_QUILL),
                main.color("&e&lItem List"),
                "",
                main.color("&fEnabled: &a" + enabled + "&7/&a" + GameLootDrops.values().length + " &7items"),
                "",
                main.color("&7Choose which items lightning"),
                main.color("&7drops can spawn"),
                "",
                main.color("&eClick to open")
        ), e -> {
            SoundManager.playClickSound(player);
            new ItemListGUI(main, game, this).inv.open(player);
        }));
    }

    private void addOpDropsModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().opDrops;

        contents.set(2, 2, ClickableItem.of(toggleItem(new ItemStack(Material.WHEAT), enabled, "&e&lOP Item Drops",
                "&7Lightning drops only spawn",
                "&6&lLEGENDARY &7and &9&lRARE &7items.",
                "&7(No Extra Lives)"
        ), e -> {
            SoundManager.playClickSound(player);

            game.getGameSettings().opDrops = !game.getGameSettings().opDrops;
            game.TellAll(main.color("&2&l(!) &rOP Drops have been " + status(game.getGameSettings().opDrops)));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addLivesModifier(InventoryContents contents, Player player) {
        int index = indexOf(LIVES, game.getGameSettings().startingLives);

        contents.set(2, 4, ClickableItem.of(cycleItem(new ItemStack(Material.PRISMARINE_SHARD), "&e&lLives",
                LIVES_LABELS, index, LIVES[index], true,
                "&7How many lives everyone", "&7starts with"
        ), e -> {
            SoundManager.playClickSound(player);

            game.getGameSettings().startingLives = LIVES[step(index, LIVES.length, e)];
            game.TellAll(main.color("&2&l(!) &rEveryone will now start with &e&l"
                    + formatLives(game.getGameSettings().startingLives) + "&r."));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addRespawnTimeModifier(InventoryContents contents, Player player) {
        int index = indexOf(RESPAWN_TIMES, game.getGameSettings().respawnSeconds);
        int seconds = RESPAWN_TIMES[index];

        contents.set(2, 5, ClickableItem.of(cycleItem(new ItemStack(Material.DIODE), "&e&lRespawn Time",
                RESPAWN_TIME_LABELS, index, seconds, seconds > 0,
                "&7How long players wait", "&7before respawning"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = RESPAWN_TIMES[step(index, RESPAWN_TIMES.length, e)];
            game.getGameSettings().respawnSeconds = next;

            if (next == 0) {
                game.TellAll(main.color("&2&l(!) &rPlayers will now respawn &e&linstantly&r."));
            } else {
                game.TellAll(main.color("&2&l(!) &rRespawn time was set to &e&l" + next + "s&r."));
            }

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addSpawnProtectionModifier(InventoryContents contents, Player player) {
        int index = indexOf(SPAWN_PROTECTION_TIMES, game.getGameSettings().spawnProtectionSeconds);
        int seconds = SPAWN_PROTECTION_TIMES[index];

        // Has to be a stackable item, or the icon can't show the option's amount
        contents.set(2, 6, ClickableItem.of(cycleItem(new ItemStack(Material.IRON_FENCE), "&e&lSpawn Protection",
                SPAWN_PROTECTION_LABELS, index, seconds, seconds > 0,
                "&7How long players are protected", "&7when the game starts and", "&7after respawning"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = SPAWN_PROTECTION_TIMES[step(index, SPAWN_PROTECTION_TIMES.length, e)];
            game.getGameSettings().spawnProtectionSeconds = next;

            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rSpawn Protection was set to &e&l" + next + "s&r."));
            } else {
                game.TellAll(main.color("&2&l(!) &rSpawn Protection has been &cdisabled"));
            }

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addTimeOfDayModifier(InventoryContents contents, Player player) {
        int index = indexOf(TIMES_OF_DAY, getTimeOfDay());

        contents.set(2, 7, ClickableItem.of(cycleItem(new ItemStack(Material.WATCH), "&e&lTime of Day",
                TIME_OF_DAY_LABELS, index, 1, true,
                "&7The time of day on the map"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = step(index, TIMES_OF_DAY.length, e);
            game.getMapWorld().setTime(TIMES_OF_DAY[next]);
            game.TellAll(main.color("&2&l(!) &rTime of day was set to " + formatTimeOfDay(TIMES_OF_DAY[next]) + "&r."));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    // ===== Helpers =====

    private boolean canModify(Player player) {
        if (game.state == GameState.WAITING && game.canEditCustomGame(player) && !game.isCustomGameCountdownStarted())
            return true;
        player.sendMessage(main.color("&c&l(!) &rThis game can no longer be modified."));
        inv.close(player);
        return false;
    }

    /** Frenzy, Class Switch and One For All: a dark grey tag above the item's blank line and Status/Current. */
    private ItemStack classSelectionTag(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<String>();
        lore.add(0, main.color("&8Class Selection Modifier"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

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

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(main.color("&fCurrent: &a" + options[current]));
        lore.add("");
        for (String line : description) lore.add(main.color(line));
        lore.add("");
        lore.add(main.color("&7Options:"));

        // Options list, wrapped onto more lines when it gets too long
        StringBuilder line = new StringBuilder();
        int lineLength = 0;
        for (int i = 0; i < options.length; i++) {
            if (i > 0) {
                line.append("&7,");
                lineLength++;
                if (lineLength + 1 + options[i].length() > OPTIONS_LINE_LENGTH) {
                    lore.add(main.color(line.toString()));
                    line = new StringBuilder();
                    lineLength = 0;
                } else {
                    line.append(' ');
                    lineLength++;
                }
            }
            line.append(i == current ? "&a" : "&e").append(options[i]);
            lineLength += options[i].length();
        }
        lore.add(main.color(line.toString()));

        lore.add("");
        lore.add(main.color(CYCLE_HINT));

        return ItemHelper.setDetails(item, main.color(name), lore.toArray(new String[0]));
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

    /*
     * Frenzy, Class Switch and One For All all decide how players get their class, so only one can be on at a time.
     * If one other than the given one is already on, tells the leader to turn it off first and returns true.
     */
    private boolean isOtherClassSelectionModifierActive(Player player, String modifier) {
        GameSettings settings = game.getGameSettings();
        String active = null;
        if (game.gameType == GameType.FRENZY && !modifier.equals(FRENZY)) active = FRENZY;
        else if (settings.randomClasses && !modifier.equals(CLASS_SWITCH)) active = CLASS_SWITCH;
        else if (settings.oneForAll && !modifier.equals(ONE_FOR_ALL)) active = ONE_FOR_ALL;
        if (active == null) return false;

        SoundManager.playErrorSound(player);
        player.sendMessage(main.color("&c&l(!) &rOnly one class selection modifier (&eFrenzy&r, &eClass Switch &ror "
                + "&eOne For All&r) can be active at a time. Disable &e" + active + " &rto enable &e" + modifier + "&r."));
        return true;
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
