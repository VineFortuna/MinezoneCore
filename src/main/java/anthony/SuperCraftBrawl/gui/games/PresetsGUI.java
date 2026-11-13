package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.presets.CuratedPreset;
import anthony.SuperCraftBrawl.Game.presets.GamePreset;
import anthony.SuperCraftBrawl.Game.presets.GamePresetDAO;
import anthony.util.AnvilInput;
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

import java.util.ArrayList;
import java.util.List;

/*
 * Game Settings > Presets. 2nd row: the player's own presets (#1 to #7) - green = saved, orange = free to save on.
 * 3rd and 4th rows: our curated presets, red glass for the slots without one. Loading a preset replaces every
 * setting, and they can all still be edited afterwards. A saved preset is loaded with a left click, renamed with
 * a right click (typed into an anvil) and deleted with a shift click.
 */
public class PresetsGUI implements InventoryProvider {

    private static final int ROWS = 5;
    private static final int CUSTOM_ROW = 1, CURATED_ROW = 2, CURATED_ROWS = 2, SLOTS_PER_ROW = 7;
    private static final short GRAY = 7, GREEN = 5, ORANGE = 1, RED = 14;

    private final Core main;
    private final GameInstance game;
    private final GameModifiersGUI parent;
    private final GamePreset[] customPresets; // this player's, index 0 = #1; null = free slot
    public final SmartInventory inv;

    private PresetsGUI(Core main, GameInstance game, GameModifiersGUI parent, GamePreset[] customPresets) {
        this.main = main;
        this.game = game;
        this.parent = parent;
        this.customPresets = customPresets;

        this.inv = SmartInventory.builder()
                .id("gamePresets")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Presets")
                .build();
    }

    /** Loads the player's saved presets from the database, then opens the menu. */
    public static void open(Core main, GameInstance game, GameModifiersGUI parent, Player player) {
        GamePresetDAO.load(main, player.getUniqueId(), presets -> {
            if (!player.isOnline() || game.state != GameState.WAITING) return; // e.g. the game started while loading
            if (presets == null) {
                SoundManager.playErrorSound(player);
                player.sendMessage(main.color("&c&l(!) &rCouldn't load your presets. Try again later."));
                return;
            }
            new PresetsGUI(main, game, parent, presets).inv.open(player);
        });
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.empty(glass(GRAY, " ")));

        contents.set(CUSTOM_ROW, 0, ClickableItem.empty(glass(GRAY, "&a&lYour Presets",
                "", "&7Save the current settings as", "&7one of your own presets")));
        contents.set(CURATED_ROW, 0, ClickableItem.empty(glass(GRAY, "&6&lCurated Presets",
                "", "&7Ready-made presets that", "&7everyone can use")));

        for (int i = 0; i < GamePresetDAO.MAX_PRESETS; i++) {
            setCustomSlot(contents, player, i);
        }

        CuratedPreset[] curated = CuratedPreset.values();
        for (int i = 0; i < CURATED_ROWS * SLOTS_PER_ROW; i++) {
            int row = CURATED_ROW + i / SLOTS_PER_ROW, column = 1 + i % SLOTS_PER_ROW;
            if (i < curated.length) {
                setCuratedSlot(contents, player, row, column, curated[i]);
            } else {
                contents.set(row, column, ClickableItem.empty(glass(RED, "&cNo Preset",
                        "", "&7More curated presets", "&7are coming soon")));
            }
        }

        contents.set(ROWS - 1, 4, ClickableItem.of(ItemHelper.getGoBackItem(), e -> {
            SoundManager.playClickSound(player);
            parent.inv.open(player);
        }));
    }

    private void setCustomSlot(InventoryContents contents, Player player, int index) {
        int number = index + 1;
        GamePreset preset = customPresets[index];

        if (preset == null) {
            contents.set(CUSTOM_ROW, number, ClickableItem.of(glass(ORANGE, "&6&lPreset #" + number,
                    "", "&7Empty slot", "",
                    "&7You can save the current", "&7Game Settings as a preset here", "",
                    "&eClick to save"
            ), e -> {
                if (!canModify(player)) return;
                save(player, index);
            }));
            return;
        }

        List<String> lore = new ArrayList<>();
        if (preset.name != null) lore.add("&8Preset #" + number);
        lore.add("");
        lore.addAll(summary(preset));
        lore.add("");
        lore.add("&eLeft Click &7to load");
        lore.add("&eRight Click &7to rename");
        lore.add("&eShift Click &7to delete");

        String title = getTitle(preset, number);
        contents.set(CUSTOM_ROW, number, ClickableItem.of(glass(GREEN, title, lore.toArray(new String[0])), e -> {
            if (!canModify(player)) return;

            if (e.isShiftClick()) {
                SoundManager.playClickSound(player);
                customPresets[index] = null;
                GamePresetDAO.delete(main, player.getUniqueId(), number);
                player.sendMessage(main.color("&2&l(!) &rPreset &e#" + number + " &rhas been &cdeleted&r."));
                inv.open(player);
            } else if (e.isRightClick()) {
                rename(player, index);
            } else if (e.isLeftClick()) {
                load(player, preset, title);
            }
        }));
    }

    private void setCuratedSlot(InventoryContents contents, Player player, int row, int column, CuratedPreset curated) {
        List<String> lore = new ArrayList<>();
        lore.add("&8Curated");
        lore.add("");
        for (String line : curated.getDescription()) lore.add(line);
        lore.add("");
        lore.addAll(summary(curated.build()));
        lore.add("");
        lore.add("&7Everything else is default, and");
        lore.add("&7everything can be edited after loading it");
        lore.add("");
        lore.add("&eClick to load");

        // Hide the sword's attack damage line
        ItemStack icon = ItemHelper.setHideFlags(ItemHelper.setDetails(new ItemStack(curated.getIcon()),
                main.color(curated.getName()), color(lore)), true);
        contents.set(row, column, ClickableItem.of(icon, e -> {
            if (!canModify(player)) return;
            load(player, curated.build(), curated.getName());
        }));
    }

    private static String getTitle(GamePreset preset, int number) {
        return preset.name != null ? "&a&l" + preset.name : "&a&lPreset #" + number;
    }

    private void save(Player player, int index) {
        SoundManager.playClickSound(player);
        int number = index + 1;
        GamePreset preset = GamePreset.capture(game);
        customPresets[index] = preset;
        GamePresetDAO.save(main, player.getUniqueId(), number, preset);
        player.sendMessage(main.color("&2&l(!) &rSaved as preset &e#" + number + "&r."));
        inv.open(player);
    }

    /** The player types a name for the preset in an anvil; clearing it goes back to "Preset #n". */
    private void rename(Player player, int index) {
        SoundManager.playClickSound(player);
        int number = index + 1;
        GamePreset preset = customPresets[index];
        String current = preset.name != null ? preset.name : "Preset #" + number;

        AnvilInput.open(main, player, current, typed -> {
            preset.name = GamePreset.cleanName(typed);
            GamePresetDAO.save(main, player.getUniqueId(), number, preset);
            player.sendMessage(main.color(preset.name == null
                    ? "&2&l(!) &rPreset &e#" + number + " &rhas been &creset &rto its default name."
                    : "&2&l(!) &rPreset &e#" + number + " &rhas been renamed to &e" + preset.name + "&r."));
            reopen(player);
        }, () -> reopen(player));
    }

    /** Back to this menu after the anvil, unless the game started or they can't edit it anymore meanwhile. */
    private void reopen(Player player) {
        if (player.isOnline() && game.state == GameState.WAITING && game.canEditCustomGame(player)
                && !game.isCustomGameCountdownStarted()) {
            inv.open(player);
        }
    }

    private void load(Player player, GamePreset preset, String name) {
        SoundManager.playClickSound(player);
        preset.applyTo(game);
        game.TellAll(main.color("&2&l(!) &rLoaded the " + name + " &rpreset."));

        // A preset saved on another map moves everyone to it (with the settings just loaded)
        if (preset.map != null && preset.map != game.getMap()) {
            GameInstance next = main.getGameManager().changeCustomGameMap(player, preset.map);
            if (next != null && next != game) {
                new GameModifiersGUI(main, next).inv.open(player); // the settings of the new game
                next.refreshEditorMenus(player);
                return;
            }
        }

        parent.inv.open(player); // straight back to the settings, showing the new values
        game.refreshEditorMenus(player);
    }

    /** The settings a preset's lore shows - anything still at its default value is left out. */
    private List<String> summary(GamePreset preset) {
        GameSettings s = preset.settings;
        GameSettings defaults = new GameSettings(null);

        int classes = 0;
        for (ClassType type : s.enabledClasses) {
            if (type.isPlayable()) classes++;
        }
        boolean defaultClasses = s.isDefaultClassList();
        boolean defaultItems = s.isDefaultItemList();

        List<String> lore = new ArrayList<>();
        if (preset.map != null) {
            lore.add("&fMap: &e" + preset.map.toString());
        }
        if (!preset.isDefaultClassSelection()) {
            lore.add("&fClass Selection: &e" + preset.getClassSelection());
        }
        if (!defaultClasses) {
            lore.add(classes == 1 ? "&fClass: &e" + onlyClass(preset) : "&fClass List: &e" + classes + " classes");
        }
        if (!defaultItems) {
            lore.add("&fItem List: &e" + s.enabledDrops.size() + "&7/&e" + GameLootDrops.values().length + " items");
        }
        if (s.maxHealthHearts != defaults.maxHealthHearts) {
            lore.add("&fMax Health: &e" + s.maxHealthHearts + (s.maxHealthHearts == 1 ? " Heart" : " Hearts"));
        }
        if (s.startingLives != defaults.startingLives) {
            lore.add("&fLives: &e" + s.startingLives);
        }
        if (s.respawnSeconds != defaults.respawnSeconds) {
            lore.add("&fRespawn Time: &e" + (s.respawnSeconds == 0 ? "None" : s.respawnSeconds + "s"));
        }
        if (s.spawnProtectionSeconds != defaults.spawnProtectionSeconds) {
            lore.add("&fSpawn Protection: " + secondsOrDisabled(s.spawnProtectionSeconds));
        }
        if (s.dropTimer != defaults.dropTimer) {
            lore.add("&fLightning Drop Rate: " + secondsOrDisabled(s.dropTimer));
        }
        if (s.lowGravity != defaults.lowGravity) {
            lore.add("&fLow Gravity: " + (s.lowGravity ? "&aEnabled" : "&cDisabled"));
        }
        if (s.effectRouletteSeconds != defaults.effectRouletteSeconds) {
            lore.add("&fEffect Roulette: " + secondsOrDisabled(s.effectRouletteSeconds));
        }
        return lore;
    }

    private static String onlyClass(GamePreset preset) {
        for (ClassType type : preset.settings.enabledClasses) {
            if (type.isPlayable()) return type.getTag();
        }
        return "Default";
    }

    private static String secondsOrDisabled(int seconds) {
        return seconds > 0 ? "&e" + seconds + "s" : "&cDisabled";
    }

    private boolean canModify(Player player) {
        if (game.state == GameState.WAITING && game.canEditCustomGame(player) && !game.isCustomGameCountdownStarted())
            return true;
        SoundManager.playErrorSound(player);
        player.sendMessage(main.color("&c&l(!) &rThis game can no longer be modified."));
        inv.close(player);
        return false;
    }

    private ItemStack glass(short color, String name, String... lore) {
        List<String> lines = new ArrayList<>();
        for (String line : lore) lines.add(line);
        return ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, color), main.color(name), color(lines));
    }

    private String[] color(List<String> lines) {
        String[] colored = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) colored[i] = main.color(lines.get(i));
        return colored;
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
