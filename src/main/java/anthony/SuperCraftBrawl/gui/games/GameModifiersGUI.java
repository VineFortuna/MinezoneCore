package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.CustomGameVisibility;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameSettings.HealthPotMode;
import anthony.SuperCraftBrawl.Game.GameSettings.OneForAllMode;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.presets.GamePreset;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
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

    // Package-visible so the read-only GameSettingsPreviewGUI can render the same options.
    static final int[] DROP_TIMES = {0, 1, 5, 10, 15, 20, 25, 30, 45, 60}; // 0 = disabled
    static final String[] DROP_TIME_LABELS = {"Disabled", "1s", "5s", "10s", "15s", "20s", "25s", "30s", "45s", "1m"};
    static final int[] CLASS_SWITCH_TIMES = {0, 1, 5, 15, 30, 45, 60}; // 0 = disabled
    static final String[] CLASS_SWITCH_LABELS = {"Disabled", "1s", "5s", "15s", "30s", "45s", "1m"};
    static final int[] TAG_TEAM_TIMES = {0, 15, 30, 60}; // 0 = disabled
    static final String[] TAG_TEAM_LABELS = {"Disabled", "15s", "30s", "1m"};
    static final int[] LIVES = {1, 2, 3, 4, 5, 10, 20, 50};
    static final String[] LIVES_LABELS = {"1", "2", "3", "4", "5", "10", "20", "50"};
    static final int[] RESPAWN_TIMES = {0, 1, 2, 3, 4, 5, 10};
    static final String[] RESPAWN_TIME_LABELS = {"None", "1s", "2s", "3s", "4s", "5s", "10s"};
    static final int[] SPAWN_PROTECTION_TIMES = {0, 3, 5}; // 0 = disabled
    static final String[] SPAWN_PROTECTION_LABELS = {"Disabled", "3s", "5s"};
    static final int[] STRENGTH_TIMES = {0, 5, 10, 20, 30}; // 0 = disabled
    static final String[] STRENGTH_LABELS = {"Disabled", "5s", "10s", "20s", "30s"};
    static final int[] ROULETTE_TIMES = {0, 5, 15, 30}; // 0 = disabled
    static final String[] ROULETTE_LABELS = {"Disabled", "5s", "15s", "30s"};
    static final int[] MAX_HEALTH_HEARTS = {1, 3, 5, 10, 15, 20, 40};
    static final String[] MAX_HEALTH_LABELS = {"1 Heart", "3 Hearts", "5 Hearts", "10 Hearts", "15 Hearts", "20 Hearts", "40 Hearts"};

    static final long DAY = 1000, SUNSET = 12500, NIGHT = 18000;
    static final long[] TIMES_OF_DAY = {DAY, SUNSET, NIGHT};
    static final String[] TIME_OF_DAY_LABELS = {"Day", "Sunset", "Night"};

    private static final String CYCLE_HINT = "&7<< &eLeft Click &8| &eRight Click &7>>";
    private static final int OPTIONS_LINE_LENGTH = 40; // options lists longer than this wrap onto another line

    // Class selection modifiers: only one can be active per game (none = Classic)
    private static final String FRENZY = "Frenzy", CLASS_SWITCH = "Class Switch", ONE_FOR_ALL = "One For All",
            TAG_TEAM = "Tag Team";

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
        addTagTeamModifier(contents, player);           // 0,3
        addEffectRouletteModifier(contents, player);    // 0,4
        addLowGravityModifier(contents, player);        // 0,5
        addStrengthOnKillModifier(contents, player);    // 0,6
        addHealingOnKillModifier(contents, player);     // 0,7
        addLightningDropRateModifier(contents, player); // 0,8

        // Bottom row
        addClassListModifier(contents, player);         // 2,0
        addItemListModifier(contents, player);          // 2,1
        addPassiveHealingModifier(contents, player);    // 2,3
        addMaxHealthModifier(contents, player);         // 2,4
        addLivesModifier(contents, player);             // 2,5
        addRespawnTimeModifier(contents, player);       // 2,6
        addSpawnProtectionModifier(contents, player);   // 2,7
        addTimeOfDayModifier(contents, player);         // 2,8

        // Last row
        addPresetsModifier(contents, player);           // 4,0
        addVisibilityModifier(contents, player);        // 4,3
        addKickPlayerModifier(contents, player);        // 4,4
        addCoHostModifier(contents, player);            // 4,5
        addResetSettingsModifier(contents, player);     // 4,8
    }

    // ===== Last row =====

    private void addResetSettingsModifier(InventoryContents contents, Player player) {
        ItemStack item = ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                main.color("&c&lReset Settings"),
                "",
                main.color("&7Resets every Game Setting"),
                main.color("&7back to its default value"),
                "",
                main.color("&eClick to reset"));

        contents.set(4, 8, ClickableItem.of(item, e -> {
            if (!canModify(player)) return;
            SoundManager.playClickSound(player);

            new GamePreset().applyTo(game);
            game.TellAll(main.color("&2&l(!) &rGame Settings reset to &edefault&r."));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addPresetsModifier(InventoryContents contents, Player player) {
        ItemStack item = ItemHelper.setDetails(new ItemStack(Material.CHEST),
                main.color("&e&lPresets"),
                "",
                main.color("&7Save the current settings as one"),
                main.color("&7of your own custom presets, or"),
                main.color("&7load one of our curated presets"),
                "",
                main.color("&7Every setting can still be"),
                main.color("&7edited after loading a preset"),
                "",
                main.color("&eClick to open"));

        contents.set(4, 0, ClickableItem.of(item, e -> {
            if (!canModify(player)) return;
            SoundManager.playClickSound(player);
            PresetsGUI.open(main, game, this, player);
        }));
    }

    private void addVisibilityModifier(InventoryContents contents, Player player) {
        CustomGameVisibility current = game.getCustomGameVisibility();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&fCurrent: " + current.getTag());
        lore.add("");
        if (current == CustomGameVisibility.PUBLIC) {
            lore.add("&7Anyone can join this game");
            lore.add("&7from the Custom Games NPC");
        } else if (current == CustomGameVisibility.FRIENDS) {
            lore.add("&7Only your friends and");
            lore.add("&7your party can join");
        } else {
            lore.add("&7Only your party can");
            lore.add("&7join this game");
        }
        lore.add("");
        lore.add("&7Next: " + current.next().getTag());
        lore.add("&eClick to cycle");

        ItemStack item = ItemHelper.setDetails(new ItemStack(current.getBlock()),
                main.color("&e&lVisibility"), lore.toArray(new String[0]));

        contents.set(4, 3, ClickableItem.of(item, e -> {
            if (!canModify(player)) return;
            if (!e.isLeftClick() && !e.isRightClick()) return;
            SoundManager.playClickSound(player);

            CustomGameVisibility now = game.getCustomGameVisibility();
            CustomGameVisibility next = now.next();
            game.setCustomGameVisibility(next);
            game.TellAll(main.color("&2&l(!) &rVisibility set to " + next.getTag() + "&r."));
            if (next == CustomGameVisibility.PUBLIC) game.announcePublicCustomGame(player);
            CustomGamesGUI.refreshOpenMenus(); // the game changes block for anyone looking, in Quick Join too

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
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
        contents.set(0, 0, ClickableItem.of(classSelectionTag(main, toggleItem(main, new ItemStack(Material.TNT), enabled, "&e&lFrenzy",
                "&7Everyone gets a random class",
                "&7on each life"
        )), e -> {
            if (!enabled && isOtherClassSelectionModifierActive(player, FRENZY)) return;
            SoundManager.playClickSound(player);

            game.gameType = game.gameType == GameType.FRENZY ? GameType.CLASSIC : GameType.FRENZY;
            game.TellAll(main.color("&2&l(!) &rFrenzy has been " + status(game.gameType == GameType.FRENZY)));
            game.updateModeLine();

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addClassSwitchModifier(InventoryContents contents, Player player) {
        GameSettings settings = game.getGameSettings();
        int index = indexOf(CLASS_SWITCH_TIMES, settings.randomClasses ? settings.randomClassCycleSeconds : 0);
        int seconds = CLASS_SWITCH_TIMES[index];

        contents.set(0, 1, ClickableItem.of(classSelectionTag(main, cycleItem(main, new ItemStack(Material.EYE_OF_ENDER), "&e&lClass Switch",
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
                game.TellAll(main.color("&2&l(!) &rClass Switch &aenabled&r: classes change every &e&l"
                        + CLASS_SWITCH_LABELS[next]));
            } else {
                game.TellAll(main.color("&2&l(!) &rClass Switch has been &cdisabled"));
            }
            game.updateModeLine();

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addOneForAllModifier(InventoryContents contents, Player player) {
        OneForAllMode mode = game.getGameSettings().oneForAllMode;
        OneForAllMode[] modes = OneForAllMode.values();
        String[] labels = new String[modes.length];
        for (int i = 0; i < modes.length; i++) labels[i] = modes[i].getName();

        contents.set(0, 2, ClickableItem.of(classSelectionTag(main, cycleItem(main, new ItemStack(Material.ARMOR_STAND), "&e&lOne For All",
                labels, mode.ordinal(), 1, mode != OneForAllMode.DISABLED,
                "&7All players play with the same", "&7class, chosen by the host/co-host", "&7or voted on by everyone"
        )), e -> {
            OneForAllMode next = modes[step(mode.ordinal(), modes.length, e)];
            if (mode == OneForAllMode.DISABLED && next != OneForAllMode.DISABLED
                    && isOtherClassSelectionModifierActive(player, ONE_FOR_ALL)) return;
            SoundManager.playClickSound(player);

            game.getGameSettings().oneForAllMode = next;
            if (next == OneForAllMode.DISABLED) {
                game.TellAll(main.color("&2&l(!) &rOne For All has been &cdisabled"));
            } else {
                game.TellAll(main.color("&2&l(!) &rOne For All was set to &e&l" + next.getName() + "&r."));
            }
            game.updateClassSelectionItems(); // the class selector only comes back once the game is started
            game.updateModeLine();

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addTagTeamModifier(InventoryContents contents, Player player) {
        GameSettings settings = game.getGameSettings();
        int index = indexOf(TAG_TEAM_TIMES, settings.tagTeamSeconds);
        int seconds = TAG_TEAM_TIMES[index];

        contents.set(0, 3, ClickableItem.of(classSelectionTag(main, cycleItem(main, new ItemStack(Material.NAME_TAG), "&e&lTag Team",
                TAG_TEAM_LABELS, index, seconds, seconds > 0, tagTeamDescription()
        )), e -> {
            int next = step(index, TAG_TEAM_TIMES.length, e);
            int nextSeconds = TAG_TEAM_TIMES[next];
            if (seconds == 0 && nextSeconds > 0 && isOtherClassSelectionModifierActive(player, TAG_TEAM)) return;
            SoundManager.playClickSound(player);

            settings.tagTeamSeconds = nextSeconds;

            if (nextSeconds > 0) {
                game.TellAll(main.color("&2&l(!) &rTag Team &aenabled&r: swap cooldown is &e&l" + TAG_TEAM_LABELS[next]));
            } else {
                game.TellAll(main.color("&2&l(!) &rTag Team has been &cdisabled"));
            }
            game.clearTagTeamSelections(); // the second classes only count while it's on
            game.updateModeLine();

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    /** What Tag Team does and how to swap, for the setting's icon here and in the settings preview. */
    static String[] tagTeamDescription() {
        return new String[] {
                "&7Everyone picks 2 classes and can", "&7swap between them every so often", "",
                "&eDrop any item (Q) to swap"
        };
    }

    private void addEffectRouletteModifier(InventoryContents contents, Player player) {
        int index = indexOf(ROULETTE_TIMES, game.getGameSettings().effectRouletteSeconds);
        int seconds = ROULETTE_TIMES[index];

        // Potion of Swiftness (potions don't stack, so there's no amount on the icon)
        ItemStack potion = ItemHelper.setHideFlags(new ItemStack(Material.POTION, 1, (short) 8194), true);

        contents.set(0, 4, ClickableItem.of(cycleItem(main, potion, "&e&lEffect Roulette",
                ROULETTE_LABELS, index, 1, seconds > 0,
                "&7Everyone receives a random", "&7positive effect every so often"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = ROULETTE_TIMES[step(index, ROULETTE_TIMES.length, e)];
            game.getGameSettings().effectRouletteSeconds = next;

            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rEffect Roulette &aenabled&r: a new effect every &e&l" + next + "s&r."));
            } else {
                game.TellAll(main.color("&2&l(!) &rEffect Roulette has been &cdisabled"));
            }

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addLowGravityModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().lowGravity;

        contents.set(0, 5, ClickableItem.of(toggleItem(main, new ItemStack(Material.FEATHER), enabled, "&e&lLow Gravity",
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

        contents.set(0, 6, ClickableItem.of(cycleItem(main, new ItemStack(Material.BLAZE_POWDER), "&e&lStrength on Kill",
                STRENGTH_LABELS, index, seconds, seconds > 0,
                "&7Killers gain &4Strength I", "&7for the chosen duration after", "&7getting a kill"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = STRENGTH_TIMES[step(index, STRENGTH_TIMES.length, e)];
            game.getGameSettings().strengthOnKillSeconds = next;

            if (next > 0) {
                game.TellAll(main.color("&2&l(!) &rStrength on Kill &aenabled&r: &e&l" + next
                        + "s &rafter every kill."));
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

        contents.set(0, 7, ClickableItem.of(cycleItem(main, potion, "&e&lHealing on Kill",
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

        contents.set(2, 4, ClickableItem.of(cycleItem(main, new ItemStack(Material.GOLDEN_APPLE), "&e&lMax Health",
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

        contents.set(0, 8, ClickableItem.of(cycleItem(main, new ItemStack(Material.NETHER_STAR), "&e&lLightning Drop Rate",
                DROP_TIME_LABELS, index, seconds, seconds > 0,
                "&7How often lightning drops", "&7spawn on the map"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = step(index, DROP_TIMES.length, e);
            game.getGameSettings().dropTimer = DROP_TIMES[next];
            if (DROP_TIMES[next] > 0) {
                game.TellAll(main.color("&2&l(!) &rLightning drops now spawn every &e&l" + DROP_TIME_LABELS[next] + "&r."));
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
                listIcon(game.getGameSettings().isDefaultClassList(), enabled),
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
                listIcon(game.getGameSettings().isDefaultItemList(), enabled),
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

    private void addPassiveHealingModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().passiveHealing;

        // Potion of Regeneration
        ItemStack potion = ItemHelper.setHideFlags(new ItemStack(Material.POTION, 1, (short) 8193), true);

        contents.set(2, 3, ClickableItem.of(toggleItem(main, potion, enabled, "&e&lPassive Healing",
                "&7Players slowly regenerate",
                "&7health over time"
        ), e -> {
            SoundManager.playClickSound(player);

            game.getGameSettings().passiveHealing = !game.getGameSettings().passiveHealing;
            game.TellAll(main.color("&2&l(!) &rPassive Healing has been " + status(game.getGameSettings().passiveHealing)));

            inv.open(player);
            game.refreshEditorMenus(player);
        }));
    }

    private void addLivesModifier(InventoryContents contents, Player player) {
        int index = indexOf(LIVES, game.getGameSettings().startingLives);

        contents.set(2, 5, ClickableItem.of(cycleItem(main, new ItemStack(Material.PRISMARINE_SHARD), "&e&lLives",
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

        contents.set(2, 6, ClickableItem.of(cycleItem(main, new ItemStack(Material.DIODE), "&e&lRespawn Time",
                RESPAWN_TIME_LABELS, index, seconds, seconds > 0,
                "&7How long players wait", "&7before respawning"
        ), e -> {
            SoundManager.playClickSound(player);

            int next = RESPAWN_TIMES[step(index, RESPAWN_TIMES.length, e)];
            game.getGameSettings().respawnSeconds = next;

            if (next == 0) {
                game.TellAll(main.color("&2&l(!) &rPlayers now respawn &e&linstantly&r."));
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
        contents.set(2, 7, ClickableItem.of(cycleItem(main, new ItemStack(Material.IRON_FENCE), "&e&lSpawn Protection",
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

        contents.set(2, 8, ClickableItem.of(cycleItem(main, new ItemStack(Material.WATCH), "&e&lTime of Day",
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

    /** Class List / Item List: once edited from the default, the book shows how many are enabled and glows. */
    static ItemStack listIcon(boolean isDefault, int enabled) {
        ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
        if (isDefault) return item;

        item.setAmount(Math.max(1, Math.min(64, enabled)));
        return ItemHelper.setGlowing(item, true);
    }

    private boolean canModify(Player player) {
        if (game.state == GameState.WAITING && game.canEditCustomGame(player) && !game.isCustomGameCountdownStarted())
            return true;
        player.sendMessage(main.color("&c&l(!) &rThis game can no longer be modified."));
        inv.close(player);
        return false;
    }

    /** Frenzy, Class Switch, One For All and Tag Team: a dark grey tag above the item's blank line and Status/Current. */
    static ItemStack classSelectionTag(Core main, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<String>();
        lore.add(0, main.color("&8Class Selection Modifier"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack toggleItem(Core main, ItemStack item, boolean enabled, String name, String... description) {
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
     * A cycling setting: the current option in green, or red when it's "Disabled" ("Current: ..." and in the
     * options list, others yellow), the icon's stack size matching the option's number, and enchanted unless
     * the option is an "off" one.
     */
    static ItemStack cycleItem(Core main, ItemStack item, String name, String[] options, int current, int amount, boolean active,
                                String... description) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        if (active) item = ItemHelper.setGlowing(item, true);

        List<String> lore = new ArrayList<>();
        lore.add("");
        String currentColor = options[current].equals("Disabled") ? "&c" : "&a";
        lore.add(main.color("&fCurrent: " + currentColor + options[current]));
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
            line.append(i == current ? currentColor : "&e").append(options[i]);
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
    static int indexOf(int[] values, int value) {
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
     * Frenzy, Class Switch, One For All and Tag Team all decide how players get their class, so only one can be on at a time.
     * If one other than the given one is already on, tells the leader to turn it off first and returns true.
     */
    private boolean isOtherClassSelectionModifierActive(Player player, String modifier) {
        GameSettings settings = game.getGameSettings();
        String active = null;
        if (game.gameType == GameType.FRENZY && !modifier.equals(FRENZY)) active = FRENZY;
        else if (settings.randomClasses && !modifier.equals(CLASS_SWITCH)) active = CLASS_SWITCH;
        else if (settings.oneForAllMode != OneForAllMode.DISABLED && !modifier.equals(ONE_FOR_ALL)) active = ONE_FOR_ALL;
        else if (settings.tagTeamSeconds > 0 && !modifier.equals(TAG_TEAM)) active = TAG_TEAM;
        if (active == null) return false;

        SoundManager.playErrorSound(player);
        player.sendMessage(main.color("&c&l(!) &rDisable &e" + active + " &rfirst to enable &e" + modifier + "&r."));
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

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
