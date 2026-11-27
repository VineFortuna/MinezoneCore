package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The menu of the settings item: everything about the map that isn't a place, on one screen. Each row is one setting,
 * a name on the left and its choices next to it; the chosen ones shine. A click changes the setting at once and the menu
 * stays open. The last row has the name, the selector icon, what the map still needs, and a Save button.
 */
final class MapSettingsMenu implements InventoryProvider {

    private static final String NOTE_SIZE = "Used by the size filter.";
    private static final String NOTE_GAMEPLAY = "Used by the gameplay filter.";

    private final Core main;
    private final MapSetupManager manager;
    private final MapSetupSession session;
    private final Player player;

    private MapSettingsMenu(Core main, MapSetupManager manager, Player player, MapSetupSession session) {
        this.main = main;
        this.manager = manager;
        this.player = player;
        this.session = session;
    }

    static void open(Core main, MapSetupManager manager, Player player, MapSetupSession session) {
        SmartInventory.builder().id("scb-map-settings").provider(new MapSettingsMenu(main, manager, player, session))
                .size(6, 9).title(main.color("&8&lMap Settings")).build().open(player);
    }

    @Override
    public void init(Player p, InventoryContents contents) {
        render(contents);
    }

    @Override
    public void update(Player p, InventoryContents contents) {
    }

    // ---------------------------------------------------------------- drawing

    private void render(InventoryContents contents) {
        final MapDraft draft = session.draft;

        ItemStack pane = MapSetupMenus.pane(main);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) contents.set(row, col, ClickableItem.empty(pane));
        }

        // Source
        label(contents, 0, Material.BOOK, "Map Source", draft.source == null ? "&cNot set" : "&a" + draft.source,
                "Where the map comes from.");
        for (MapSource source : MapSource.values()) {
            final MapSource value = source;
            Material material = source == MapSource.COMMUNITY ? Material.EMERALD
                    : source == MapSource.MINECADE ? Material.DIAMOND : Material.COAL;
            String note = source == MapSource.COMMUNITY ? "A map from the community."
                    : source == MapSource.MINECADE ? "A map from Minecade." : "A map from somewhere else.";
            option(contents, 0, 3 + source.ordinal(), material, name(source.name()), draft.source == value, false,
                    () -> draft.source = value, note);
        }

        // Size
        label(contents, 1, Material.EMPTY_MAP, "Map Size", draft.size == null ? "&cNot set" : "&a" + draft.size,
                "How big the map is.");
        Material[] sizeIcons = {Material.CLAY_BALL, Material.CLAY_BRICK, Material.IRON_INGOT, Material.GOLD_INGOT};
        for (Maps.Size size : Maps.Size.values()) {
            final Maps.Size value = size;
            option(contents, 1, 3 + size.ordinal(), sizeIcons[size.ordinal()], size.toString(), draft.size == value, false,
                    () -> draft.size = value, NOTE_SIZE);
        }

        // Gameplay, any of them
        label(contents, 2, Material.FEATHER, "Gameplay", draft.gameplay.isEmpty() ? "&cNot set" : "&a" + joined(draft.gameplay),
                "How the map plays. Pick any.");
        Material[] gameplayIcons = {Material.OBSIDIAN, Material.GRASS, Material.COBBLESTONE_STAIRS, Material.STONE,
                Material.WOOD_DOOR};
        for (Maps.Gameplay gameplay : Maps.Gameplay.values()) {
            final Maps.Gameplay value = gameplay;
            option(contents, 2, 3 + gameplay.ordinal(), gameplayIcons[gameplay.ordinal()], gameplay.toString(),
                    draft.gameplay.contains(value), true, () -> {
                        if (!draft.gameplay.remove(value)) draft.gameplay.add(value);
                        Collections.sort(draft.gameplay); // always in the same order, whichever was picked first
                    }, NOTE_GAMEPLAY);
        }

        // Category
        label(contents, 3, Material.ENCHANTED_BOOK, "Category", draft.category == null ? "&cNot set" : "&a" + draft.category,
                "Which list the map is in.");
        Material[] categoryIcons = {Material.NETHER_STAR, Material.CAKE, Material.CHEST, Material.PUMPKIN, Material.SNOW_BALL,
                Material.EGG};
        String[][] categoryNotes = {
                {"The main maps.", "Also used for random picks."},
                {"Shown under the Casual filter."},
                {"Shown under the Vaulted filter."},
                {"A holiday map.", "Left out of the All filter."},
                {"A holiday map.", "Left out of the All filter."},
                {"A holiday map.", "Left out of the All filter."}};
        for (Maps.Category category : Maps.Category.values()) {
            final Maps.Category value = category;
            option(contents, 3, 3 + category.ordinal(), categoryIcons[category.ordinal()], category.toString(),
                    draft.category == value, false, () -> draft.category = value, categoryNotes[category.ordinal()]);
        }

        // Game type
        label(contents, 4, Material.IRON_SWORD, "Game Type", "&a" + draft.gameType.getName(), "Classic game or duel.");
        GameType[] types = {GameType.CLASSIC, GameType.DUEL};
        Material[] typeIcons = {Material.IRON_SWORD, Material.DIAMOND_SWORD};
        for (int i = 0; i < types.length; i++) {
            final GameType value = types[i];
            option(contents, 4, 3 + i, typeIcons[i], value.getName(), draft.gameType == value, false,
                    () -> draft.gameType = value, "Up to " + value.getMaxPlayers() + " players.");
        }

        // The last row: the name, the icon, what is missing, save
        nameTile(contents, draft);
        iconTile(contents, draft);
        statusTile(contents, draft);
        ItemStack save = MapSetupMenus.item(main, new ItemStack(Material.EMERALD), "&6&lSave",
                MapSetupMenus.lines("", "&7Saves the world and the map.", "&7The same as &f/scbsetup save&7.", "",
                        "&e&nClick&r&e to save"));
        contents.set(5, 7, ClickableItem.of(save, e -> {
            if (!manager.isActive(player, session)) {
                player.closeInventory();
                return;
            }
            SoundManager.playClickSound(player);
            player.closeInventory(); // so the messages of the save are seen
            manager.saveFromMenu(player);
        }));
    }

    /** The name of a setting, on the left of its row. It shows what is chosen and is not clicked. */
    private void label(InventoryContents contents, int row, Material material, String title, String current,
                       String description) {
        List<String> lore = MapSetupMenus.lines("", "&7" + description, "", "&fCurrently: " + current);
        contents.set(row, 1, ClickableItem.empty(
                MapSetupMenus.item(main, new ItemStack(material), "&6&l" + title, lore)));
    }

    /** One choice of a row. {@code multi}: a switch (on or off) rather than one of several. */
    private void option(final InventoryContents contents, int row, int col, Material material, String title, boolean on,
                        boolean multi, final Runnable change, String... description) {
        List<String> lore = new ArrayList<String>();
        lore.add("");
        for (String line : description) lore.add("&7" + line);
        lore.add("");
        if (multi) {
            lore.add(on ? "&aOn" : "&cOff");
            lore.add("&e&nClick&r&e to turn it " + (on ? "off" : "on"));
        } else {
            lore.add(on ? "&aSelected" : "&e&nClick&r&e to select");
        }

        ItemStack item = MapSetupMenus.item(main, new ItemStack(material), "&6&l" + title, lore);
        if (on) MapSetupMenus.glow(item);

        contents.set(row, col, ClickableItem.of(item, e -> {
            if (!manager.isActive(player, session)) {
                player.closeInventory();
                return;
            }
            SoundManager.playClickSound(player);
            change.run();
            manager.refresh(player, session); // the settings item says what is still missing
            render(contents);
        }));
    }

    /** The name players see for the map. It is typed into an anvil. */
    private void nameTile(final InventoryContents contents, MapDraft draft) {
        List<String> lore = MapSetupMenus.lines("", "&7The name in the game selector", "&7and the map lists.", "",
                "&fCurrently: &a" + draft.displayName, "&fMap ID: &7" + draft.id + " &7(stays the same)", "",
                "&e&nClick&r&e to rename it");

        contents.set(5, 1, ClickableItem.of(MapSetupMenus.item(main, new ItemStack(Material.NAME_TAG), "&6&lArena Name", lore), e -> {
            if (!manager.isActive(player, session)) {
                player.closeInventory();
                return;
            }
            SoundManager.playClickSound(player);
            manager.renameMap(player, session);
        }));
    }

    private void iconTile(final InventoryContents contents, MapDraft draft) {
        ItemStack base = draft.iconType == null ? new ItemStack(Material.BARRIER) : draft.icon();
        List<String> lore = MapSetupMenus.lines("", "&7The item that shows the map in", "&7the game selector.", "",
                "&fCurrently: " + (draft.iconType == null ? "&cNot set" : "&a" + MapSetupMenus.iconName(draft)), "",
                "&e&nClick&r&e to pick one");

        contents.set(5, 3, ClickableItem.of(MapSetupMenus.item(main, base, "&6&lSelector Icon", lore), e -> {
            if (!manager.isActive(player, session)) {
                player.closeInventory();
                return;
            }
            SoundManager.playClickSound(player);
            MapSetupMenus.iconPicker(main, player, session.draft);
        }));
    }

    private void statusTile(final InventoryContents contents, MapDraft draft) {
        List<String> lore = new ArrayList<String>();
        lore.add("");
        if (draft.isComplete()) {
            lore.add("&aNothing is missing.");
            lore.add("&7/scbsetup save makes it playable.");
        } else {
            lore.add("&cStill needs:");
            for (String missing : draft.missing()) lore.add("&c- " + missing);
        }
        lore.add("");
        lore.add("&e&nClick&r&e to see it in chat");

        contents.set(5, 5, ClickableItem.of(MapSetupMenus.item(main, new ItemStack(Material.PAPER), "&6&lMap Status", lore), e -> {
            SoundManager.playClickSound(player);
            manager.sendStatus(player, session);
        }));
    }

    /** "COMMUNITY" as "Community". */
    private static String name(String constant) {
        return constant.charAt(0) + constant.substring(1).toLowerCase();
    }

    private static <T> String joined(List<T> values) {
        StringBuilder out = new StringBuilder();
        for (T value : values) out.append(out.length() == 0 ? "" : ", ").append(value);
        return out.toString();
    }
}
