package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
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
import org.bukkit.inventory.ItemStack;

public class GameModifiersGUI implements InventoryProvider {

    private final Core main;
    private final GameInstance game;
    public final SmartInventory inv;

    private final int[] DROP_TIMES = {5, 10, 15, 20, 25, 30, 45};

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
        ClickableItem border = ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {});

        for (int i = 0; i < 9; i++) {
            contents.set(2, i, border);
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

        addTimeModifier(contents, player);
        addLootDropModifier(contents, player);
        addOpDropsModifier(contents, player);
        addGameTypeModifier(contents, player);
        addStrengthOnKillModifier(contents, player);
        addDisableHealthPotsModifier(contents, player);
        addDisableSpawnProtectionModifier(contents, player);
        addRandomClassesModifier(contents, player);
        addLivesModifier(contents, player);
        addRespawnTimeModifier(contents, player);
        addLowGravityModifier(contents, player);
        addClassListModifier(contents, player);
    }

    private void addClassListModifier(InventoryContents contents, Player player) {
        int enabled = 0;
        int vaulted = 0;
        for (ClassType type : game.getGameSettings().enabledClasses) {
            if (!type.isPlayable()) continue;
            enabled++;
            if (type.isVaulted()) vaulted++;
        }

        contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.ARMOR_STAND),
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

    private void addLowGravityModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().lowGravity;

        ItemStack item = new ItemStack(Material.FEATHER);
        if (enabled) item = ItemHelper.setGlowing(item, true);

        contents.set(1, 3, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lLow Gravity"),
                "",
                main.color("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled")),
                "",
                main.color("&7Everyone gets permanent"),
                main.color("&bJump Boost X &7while alive"),
                "",
                main.color("&eClick to toggle")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().lowGravity = !game.getGameSettings().lowGravity;

            if (game.getGameSettings().lowGravity) {
                game.TellAll(main.color("&2&l(!) &rLow Gravity has been &aenabled"));
            } else {
                game.TellAll(main.color("&2&l(!) &rLow Gravity has been &cdisabled"));
            }

            inv.open(player);
        }));
    }

    private void addLivesModifier(InventoryContents contents, Player player) {
        int lives = game.getGameSettings().startingLives;

        contents.set(1, 1, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.GOLDEN_APPLE),
                main.color("&e&lLives"),
                "",
                main.color("&fCurrent: &e" + formatLives(lives)),
                "",
                main.color("&7Click to cycle:"),
                main.color("&e1&7, &e2&7, &e3&7, &e4&7, &e5")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().startingLives = getNextLives(game.getGameSettings().startingLives);

            game.TellAll(main.color("&2&l(!) &rEveryone will now start with &e&l"
                    + formatLives(game.getGameSettings().startingLives) + "&r."));

            inv.open(player);
        }));
    }

    private void addRespawnTimeModifier(InventoryContents contents, Player player) {
        int seconds = game.getGameSettings().respawnSeconds;

        contents.set(1, 2, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.BED),
                main.color("&e&lRespawn Time"),
                "",
                main.color("&fCurrent: &e" + formatRespawnTime(seconds)),
                "",
                main.color("&7Click to cycle:"),
                main.color("&eNone&7, &e1s&7, &e2s&7, &e3s&7,"),
                main.color("&e4s&7, &e5s")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().respawnSeconds = getNextRespawnTime(game.getGameSettings().respawnSeconds);

            int newSeconds = game.getGameSettings().respawnSeconds;
            if (newSeconds == 0) {
                game.TellAll(main.color("&2&l(!) &rPlayers will now respawn &e&linstantly&r."));
            } else {
                game.TellAll(main.color("&2&l(!) &rRespawn time was set to &e&l" + newSeconds + "s&r."));
            }

            inv.open(player);
        }));
    }

    private int getNextLives(int current) {
        // 1 -> 2 -> 3 -> 4 -> 5 -> 1
        return current >= 5 || current < 1 ? 1 : current + 1;
    }

    private String formatLives(int lives) {
        return lives + (lives == 1 ? " Life" : " Lives");
    }

    private int getNextRespawnTime(int current) {
        // None -> 1s -> 2s -> 3s -> 4s -> 5s -> None
        return current >= 5 || current < 0 ? 0 : current + 1;
    }

    private String formatRespawnTime(int seconds) {
        return seconds <= 0 ? "None" : seconds + "s";
    }

    private void addRandomClassesModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().randomClasses;

        ItemStack item = new ItemStack(Material.EYE_OF_ENDER);
        if (enabled) item = ItemHelper.setGlowing(item, true);

        contents.set(0, 8, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lRandom Classes"),
                "",
                main.color("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled")),
                enabled
                        ? main.color("&fCycle Time: &e" + formatRandomClassTime(game.getGameSettings().randomClassCycleSeconds))
                        : main.color("&fCycle Time: &cDisabled"),
                "",
                main.color("&7Players will receive a new"),
                main.color("&7random class during the game"),
                "",
                main.color("&7Click to cycle:"),
                main.color("&e15s&7, &e30s&7, &e45s&7, &e1m&7, &cDisabled")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            cycleRandomClassSetting();

            if (game.getGameSettings().randomClasses) {
                game.TellAll(main.color("&2&l(!) &rRandom Classes have been &aenabled&r. Classes will change every &e&l"
                        + formatRandomClassTime(game.getGameSettings().randomClassCycleSeconds)));
            } else {
                game.TellAll(main.color("&2&l(!) &rRandom Classes have been &cdisabled"));
            }

            inv.open(player);
        }));
    }

    private void cycleRandomClassSetting() {
        if (!game.getGameSettings().randomClasses) {
            game.getGameSettings().randomClasses = true;
            game.getGameSettings().randomClassCycleSeconds = 15;
            return;
        }

        int current = game.getGameSettings().randomClassCycleSeconds;

        if (current == 15) {
            game.getGameSettings().randomClassCycleSeconds = 30;
        } else if (current == 30) {
            game.getGameSettings().randomClassCycleSeconds = 45;
        } else if (current == 45) {
            game.getGameSettings().randomClassCycleSeconds = 60;
        } else {
            game.getGameSettings().randomClasses = false;
            game.getGameSettings().randomClassCycleSeconds = 15;
        }
    }

    private String formatRandomClassTime(int seconds) {
        if (seconds >= 60) {
            return "1m";
        }

        return seconds + "s";
    }

    private void addDisableSpawnProtectionModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().disableSpawnProtection;

        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        if (enabled) item = ItemHelper.setGlowing(item, true);

        contents.set(0, 7, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lDisable Spawn Prot"),
                "",
                main.color("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled")),
                "",
                main.color("&7Players will not receive"),
                main.color("&7spawn protection when the"),
                main.color("&7game starts or after respawning"),
                "",
                main.color("&eClick to toggle")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().disableSpawnProtection = !game.getGameSettings().disableSpawnProtection;

            if (game.getGameSettings().disableSpawnProtection) {
                game.TellAll(main.color("&2&l(!) &rSpawn Protection has been &cdisabled"));
            } else {
                game.TellAll(main.color("&2&l(!) &rSpawn Protection has been &aenabled"));
            }

            inv.open(player);
        }));
    }

    private void addTimeModifier(InventoryContents contents, Player player) {
        boolean isNight = game.getMapWorld().getTime() >= 13000 && game.getMapWorld().getTime() <= 23000;

        ItemStack item = new ItemStack(Material.WATCH);
        if (isNight) item = ItemHelper.setGlowing(item, true);

        contents.set(0, 1, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lTime of Day"),
                "",
                main.color("&fCurrent: " + (isNight ? "&cNight" : "&eDay")),
                "",
                main.color("&7Click to switch between"),
                main.color("&eDay &7and &cNight&7.")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            boolean currentlyNight = game.getMapWorld().getTime() >= 13000 && game.getMapWorld().getTime() <= 23000;

            if (currentlyNight) {
                game.getMapWorld().setTime(1000);
                game.TellAll(main.color("&2&l(!) &rPrivate game time was set to &e&lDay&r."));
            } else {
                game.getMapWorld().setTime(18000);
                game.TellAll(main.color("&2&l(!) &rPrivate game time was set to &c&lNight&r."));
            }

            inv.open(player);
        }));
    }

    private void addLootDropModifier(InventoryContents contents, Player player) {
        contents.set(0, 2, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.NETHER_STAR),
                main.color("&e&lLightning Drops"),
                "",
                main.color("&fCurrent: &eEvery " + game.getGameSettings().dropTimer + "s"),
                "",
                main.color("&7Click to cycle:"),
                main.color("&e5s&7, &e10s&7, &e15s&7, &e20s&7,"),
                main.color("&e25s&7, &e30s&7, &e45s")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().dropTimer = getNextDropTime(game.getGameSettings().dropTimer);

            game.TellAll(main.color("&2&l(!) &rLightning drops will now spawn every &e&l"
                    + game.getGameSettings().dropTimer + "s&r."));

            inv.open(player);
        }));
    }

    private void addOpDropsModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().opDrops;

        ItemStack item = new ItemStack(Material.DIAMOND);
        if (enabled) item = ItemHelper.setGlowing(item, true);

        contents.set(0, 3, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lOP Item Drops"),
                "",
                main.color("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled")),
                "",
                main.color("&7Lightning drops only spawn"),
                main.color("&6&lLEGENDARY &7and &d&lRARE &7items."),
                main.color("&7(Extra Lives are removed)"),
                "",
                main.color("&eClick to toggle")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().opDrops = !game.getGameSettings().opDrops;

            if (game.getGameSettings().opDrops) {
                game.TellAll(main.color("&2&l(!) &rOP Drops have been &aenabled"));
            } else {
                game.TellAll(main.color("&2&l(!) &rOP Drops have been &cdisabled"));
            }

            inv.open(player);
        }));
    }

    private void addGameTypeModifier(InventoryContents contents, Player player) {
        if (game.gameType == GameType.DUEL) {
            contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&c&lGame Type Locked"),
                    "",
                    main.color("&7Duels cannot change game type.")
            ), e -> {}));
            return;
        }

        boolean isFrenzy = game.gameType == GameType.FRENZY;

        ItemStack item = new ItemStack(Material.TNT);
        if (isFrenzy) item = ItemHelper.setGlowing(item, true);

        contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lGame Type"),
                "",
                main.color("&fCurrent: &a" + game.gameType.getName()),
                "",
                main.color("&7Click to switch between"),
                main.color("&eClassic &7and &eFrenzy")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            if (game.gameType == GameType.FRENZY) {
                game.gameType = GameType.CLASSIC;
            } else {
                game.gameType = GameType.FRENZY;
            }

            game.TellAll(main.color("&2&l(!) &rPrivate game mode was set to &e&l"
                    + game.gameType.getName()));

            updateModeOnBoard();

            inv.open(player);
        }));
    }

    private void addStrengthOnKillModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().strengthOnKill;

        ItemStack item = new ItemStack(Material.BLAZE_POWDER);
        if (enabled) item = ItemHelper.setGlowing(item, true);

        contents.set(0, 5, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lStrength on Kill"),
                "",
                main.color("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled")),
                "",
                main.color("&7Gain &c5 seconds of Strength I"),
                main.color("&7after every kill."),
                "",
                main.color("&eClick to toggle")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().strengthOnKill = !game.getGameSettings().strengthOnKill;

            if (game.getGameSettings().strengthOnKill) {
                game.TellAll(main.color("&2&l(!) &rStrength on Kill has been &aenabled"));
            } else {
                game.TellAll(main.color("&2&l(!) &rStrength on Kill has been &cdisabled"));
            }

            inv.open(player);
        }));
    }

    private void addDisableHealthPotsModifier(InventoryContents contents, Player player) {
        boolean enabled = game.getGameSettings().disableHealthPots;

        ItemStack item = new ItemStack(Material.POTION);
        if (enabled) item = ItemHelper.setGlowing(item, true);

        contents.set(0, 6, ClickableItem.of(ItemHelper.setDetails(
                item,
                main.color("&e&lDisable Health Pots"),
                "",
                main.color("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled")),
                "",
                main.color("&7Players will not receive"),
                main.color("&7health pots after kills"),
                "",
                main.color("&eClick to toggle")
        ), e -> {
            SoundManager.playSuccessfulHit(player);

            game.getGameSettings().disableHealthPots = !game.getGameSettings().disableHealthPots;

            if (game.getGameSettings().disableHealthPots) {
                game.TellAll(main.color("&2&l(!) &rHealth Pots on kill have been &cdisabled"));
            } else {
                game.TellAll(main.color("&2&l(!) &rHealth Pots on kill have been &aenabled"));
            }

            inv.open(player);
        }));
    }

    private int getNextDropTime(int current) {
        for (int i = 0; i < DROP_TIMES.length; i++) {
            if (DROP_TIMES[i] == current) {
                return DROP_TIMES[(i + 1) % DROP_TIMES.length];
            }
        }

        return 5;
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