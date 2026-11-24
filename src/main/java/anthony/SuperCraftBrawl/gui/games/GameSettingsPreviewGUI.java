package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameSettings.HealthPotMode;
import anthony.SuperCraftBrawl.Game.GameSettings.OneForAllMode;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/*
 * Custom Games NPC > Create (Right Click): a read-only look at every Game Settings option and its default value,
 * for players deciding whether to create a custom game. Laid out exactly like GameModifiersGUI, but nothing here
 * can be clicked - there's no game behind it yet.
 */
public class GameSettingsPreviewGUI implements InventoryProvider {

    private final Core main;
    private final GameSettings defaults = new GameSettings(null); // only ever holds default values
    public final SmartInventory inv;

    public GameSettingsPreviewGUI(Core main) {
        this.main = main;

        this.inv = SmartInventory.builder()
                .id("gameSettingsPreview")
                .provider(this)
                .size(5, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Game Settings (Preview)")
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

        // Top row
        addFrenzyModifier(contents);            // 0,0
        addClassSwitchModifier(contents);       // 0,1
        addOneForAllModifier(contents);         // 0,2
        addTagTeamModifier(contents);           // 0,3
        addEffectRouletteModifier(contents);    // 0,4
        addLowGravityModifier(contents);        // 0,5
        addStrengthOnKillModifier(contents);    // 0,6
        addHealingOnKillModifier(contents);     // 0,7
        addLightningDropRateModifier(contents); // 0,8

        // Bottom row
        addClassListModifier(contents);         // 2,0
        addItemListModifier(contents);          // 2,1
        addPassiveHealingModifier(contents);    // 2,3
        addMaxHealthModifier(contents);         // 2,4
        addLivesModifier(contents);             // 2,5
        addRespawnTimeModifier(contents);       // 2,6
        addSpawnProtectionModifier(contents);   // 2,7
        addTimeOfDayModifier(contents);         // 2,8

        contents.set(4, 4, ClickableItem.of(ItemHelper.getGoBackItem(), e -> {
            SoundManager.playClickSound(player);
            inv.close(player);
        }));
    }

    private void addFrenzyModifier(InventoryContents contents) {
        contents.set(0, 0, inert(GameModifiersGUI.classSelectionTag(main, GameModifiersGUI.toggleItem(
                main, new ItemStack(Material.TNT), false, "&e&lFrenzy",
                "&7Everyone gets a random class",
                "&7on each life"
        ))));
    }

    private void addClassSwitchModifier(InventoryContents contents) {
        contents.set(0, 1, inert(GameModifiersGUI.classSelectionTag(main, GameModifiersGUI.cycleItem(
                main, new ItemStack(Material.EYE_OF_ENDER), "&e&lClass Switch",
                GameModifiersGUI.CLASS_SWITCH_LABELS, 0, 0, false,
                "&7Everyone starts with a random class", "&7and switches to a new random", "&7class every so often"
        ))));
    }

    private void addOneForAllModifier(InventoryContents contents) {
        OneForAllMode[] modes = OneForAllMode.values();
        String[] labels = new String[modes.length];
        for (int i = 0; i < modes.length; i++) labels[i] = modes[i].getName();

        contents.set(0, 2, inert(GameModifiersGUI.classSelectionTag(main, GameModifiersGUI.cycleItem(
                main, new ItemStack(Material.ARMOR_STAND), "&e&lOne For All",
                labels, OneForAllMode.DISABLED.ordinal(), 1, false,
                "&7All players play with the same", "&7class, chosen by the host/co-host", "&7or voted on by everyone"
        ))));
    }

    private void addTagTeamModifier(InventoryContents contents) {
        contents.set(0, 3, inert(GameModifiersGUI.classSelectionTag(main, GameModifiersGUI.cycleItem(
                main, new ItemStack(Material.NAME_TAG), "&e&lTag Team",
                GameModifiersGUI.TAG_TEAM_LABELS, 0, 0, false, GameModifiersGUI.tagTeamDescription()
        ))));
    }

    private void addEffectRouletteModifier(InventoryContents contents) {
        int index = GameModifiersGUI.indexOf(GameModifiersGUI.ROULETTE_TIMES, defaults.effectRouletteSeconds);
        ItemStack potion = ItemHelper.setHideFlags(new ItemStack(Material.POTION, 1, (short) 8194), true);

        contents.set(0, 4, inert(GameModifiersGUI.cycleItem(main, potion, "&e&lEffect Roulette",
                GameModifiersGUI.ROULETTE_LABELS, index, 1, GameModifiersGUI.ROULETTE_TIMES[index] > 0,
                "&7Everyone receives a random", "&7positive effect every so often"
        )));
    }

    private void addLowGravityModifier(InventoryContents contents) {
        contents.set(0, 5, inert(GameModifiersGUI.toggleItem(main, new ItemStack(Material.FEATHER), defaults.lowGravity,
                "&e&lLow Gravity",
                "&7Everyone gets permanent",
                "&bJump Boost X &7while alive"
        )));
    }

    private void addStrengthOnKillModifier(InventoryContents contents) {
        int index = GameModifiersGUI.indexOf(GameModifiersGUI.STRENGTH_TIMES, defaults.strengthOnKillSeconds);
        int seconds = GameModifiersGUI.STRENGTH_TIMES[index];

        contents.set(0, 6, inert(GameModifiersGUI.cycleItem(main, new ItemStack(Material.BLAZE_POWDER), "&e&lStrength on Kill",
                GameModifiersGUI.STRENGTH_LABELS, index, seconds, seconds > 0,
                "&7Killers gain &4Strength I", "&7for the chosen duration after", "&7getting a kill"
        )));
    }

    private void addHealingOnKillModifier(InventoryContents contents) {
        HealthPotMode mode = defaults.healthPotMode;
        HealthPotMode[] modes = HealthPotMode.values();
        String[] labels = new String[modes.length];
        for (int i = 0; i < modes.length; i++) labels[i] = modes[i].getName();

        ItemStack potion = ItemHelper.setHideFlags(new ItemStack(Material.POTION, 1, (short) 16421), true);

        contents.set(0, 7, inert(GameModifiersGUI.cycleItem(main, potion, "&e&lHealing on Kill",
                labels, mode.ordinal(), 1, mode != HealthPotMode.DISABLED,
                "&7How killers get healed",
                "&7after getting a kill"
        )));
    }

    private void addMaxHealthModifier(InventoryContents contents) {
        int index = GameModifiersGUI.indexOf(GameModifiersGUI.MAX_HEALTH_HEARTS, defaults.maxHealthHearts);

        contents.set(2, 4, inert(GameModifiersGUI.cycleItem(main, new ItemStack(Material.GOLDEN_APPLE), "&e&lMax Health",
                GameModifiersGUI.MAX_HEALTH_LABELS, index, GameModifiersGUI.MAX_HEALTH_HEARTS[index], false,
                "&7How much health everyone", "&7has on each life"
        )));
    }

    private void addLightningDropRateModifier(InventoryContents contents) {
        int index = GameModifiersGUI.indexOf(GameModifiersGUI.DROP_TIMES, defaults.dropTimer);
        int seconds = GameModifiersGUI.DROP_TIMES[index];

        contents.set(0, 8, inert(GameModifiersGUI.cycleItem(main, new ItemStack(Material.NETHER_STAR), "&e&lLightning Drop Rate",
                GameModifiersGUI.DROP_TIME_LABELS, index, seconds, seconds > 0,
                "&7How often lightning drops", "&7spawn on the map"
        )));
    }

    private void addClassListModifier(InventoryContents contents) {
        int enabled = 0;
        for (ClassType type : defaults.enabledClasses) {
            if (type.isPlayable()) enabled++;
        }

        contents.set(2, 0, inert(ItemHelper.setDetails(
                GameModifiersGUI.listIcon(defaults.isDefaultClassList(), enabled),
                main.color("&e&lClass List"),
                "",
                main.color("&fEnabled: &a" + enabled + "&7/&a" + ClassType.getPlayableClasses().length + " &7classes"),
                main.color("&fVaulted Enabled: &a0"),
                "",
                main.color("&7Choose which classes players"),
                main.color("&7can pick, including vaulted ones")
        )));
    }

    private void addItemListModifier(InventoryContents contents) {
        int enabled = defaults.enabledDrops.size();

        contents.set(2, 1, inert(ItemHelper.setDetails(
                GameModifiersGUI.listIcon(defaults.isDefaultItemList(), enabled),
                main.color("&e&lItem List"),
                "",
                main.color("&fEnabled: &a" + enabled + "&7/&a" + anthony.SuperCraftBrawl.Game.GameLootDrops.values().length + " &7items"),
                "",
                main.color("&7Choose which items lightning"),
                main.color("&7drops can spawn")
        )));
    }

    private void addPassiveHealingModifier(InventoryContents contents) {
        ItemStack potion = ItemHelper.setHideFlags(new ItemStack(Material.POTION, 1, (short) 8193), true);

        contents.set(2, 3, inert(GameModifiersGUI.toggleItem(main, potion, defaults.passiveHealing, "&e&lPassive Healing",
                "&7Players slowly regenerate",
                "&7health over time"
        )));
    }

    private void addLivesModifier(InventoryContents contents) {
        int index = GameModifiersGUI.indexOf(GameModifiersGUI.LIVES, defaults.startingLives);

        contents.set(2, 5, inert(GameModifiersGUI.cycleItem(main, new ItemStack(Material.PRISMARINE_SHARD), "&e&lLives",
                GameModifiersGUI.LIVES_LABELS, index, GameModifiersGUI.LIVES[index], true,
                "&7How many lives everyone", "&7starts with"
        )));
    }

    private void addRespawnTimeModifier(InventoryContents contents) {
        int index = GameModifiersGUI.indexOf(GameModifiersGUI.RESPAWN_TIMES, defaults.respawnSeconds);
        int seconds = GameModifiersGUI.RESPAWN_TIMES[index];

        contents.set(2, 6, inert(GameModifiersGUI.cycleItem(main, new ItemStack(Material.DIODE), "&e&lRespawn Time",
                GameModifiersGUI.RESPAWN_TIME_LABELS, index, seconds, seconds > 0,
                "&7How long players wait", "&7before respawning"
        )));
    }

    private void addSpawnProtectionModifier(InventoryContents contents) {
        int index = GameModifiersGUI.indexOf(GameModifiersGUI.SPAWN_PROTECTION_TIMES, defaults.spawnProtectionSeconds);
        int seconds = GameModifiersGUI.SPAWN_PROTECTION_TIMES[index];

        contents.set(2, 7, inert(GameModifiersGUI.cycleItem(main, new ItemStack(Material.IRON_FENCE), "&e&lSpawn Protection",
                GameModifiersGUI.SPAWN_PROTECTION_LABELS, index, seconds, seconds > 0,
                "&7How long players are protected", "&7when the game starts and", "&7after respawning"
        )));
    }

    private void addTimeOfDayModifier(InventoryContents contents) {
        contents.set(2, 8, inert(GameModifiersGUI.cycleItem(main, new ItemStack(Material.WATCH), "&e&lTime of Day",
                GameModifiersGUI.TIME_OF_DAY_LABELS, 0, 1, true,
                "&7The time of day on the map"
        )));
    }

    /** Nothing in this preview can be clicked. */
    private ClickableItem inert(ItemStack item) {
        return ClickableItem.of(item, e -> {});
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
