package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.GameLootDrops.Rarity;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/*
 * Private game modifier: the leader picks which items lightning drops can be.
 * Left click enables an item, right click disables it. Same layout as the Class List.
 */
public class ItemListGUI implements InventoryProvider {

    private static final int ROWS = 6;
    private static final int[] RANDOM_COUNTS = {1, 3, 5, 10, 15};
    private static final String RANDOMIZER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCJ9fX0=";

    private enum Status {
        ENABLED("Enabled"), DISABLED("Disabled"), ALL("All");

        final String name;

        Status(String name) {
            this.name = name;
        }
    }

    private enum Sorter {
        RARITY("Rarity"), ALPHABETICAL("Alphabetical");

        final String name;

        Sorter(String name) {
            this.name = name;
        }
    }

    private final Core main;
    private final GameInstance game;
    private final GameModifiersGUI parent;
    public final SmartInventory inv;

    private Rarity rarity = null; // null = all rarities
    private Status status = Status.ENABLED;
    private Sorter sorter = Sorter.RARITY;
    private int randomCountIndex = 3; // 10 items

    public ItemListGUI(Core main, GameInstance game, GameModifiersGUI parent) {
        this.main = main;
        this.game = game;
        this.parent = parent;

        this.inv = SmartInventory.builder()
                .id("itemList")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Item List")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        Set<GameLootDrops> enabled = game.getGameSettings().enabledDrops;
        List<GameLootDrops> shown = getShownItems();

        ClickableItem[] items = new ClickableItem[shown.size()];
        for (int i = 0; i < shown.size(); i++) {
            GameLootDrops drop = shown.get(i);
            items[i] = ClickableItem.of(getDropItem(drop, enabled.contains(drop)),
                    e -> handleItemClick(e, player, drop, pagination.getPage()));
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(28);

        contents.fillBorders(ClickableItem.of(ItemHelper.getGlassFiller(), e -> {}));

        // Randomizer
        contents.set(0, 4, ClickableItem.of(getRandomizerItem(shown.size()), e -> handleRandomizerClick(e, player)));

        // Previous / Next Page
        if (!pagination.isFirst()) {
            contents.set(2, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.previous().getPage());
            }));
        }
        if (!pagination.isLast()) {
            contents.set(2, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.next().getPage());
            }));
        }

        contents.set(ROWS - 1, 0, ClickableItem.of(getPresetsItem(enabled.size()), e -> handlePresetsClick(e, player)));
        contents.set(ROWS - 1, 3, ClickableItem.of(getFilterItem(), e -> handleFilterClick(e, player)));
        contents.set(ROWS - 1, 5, ClickableItem.of(getSorterItem(), e -> handleSortClick(e, player)));
        contents.set(ROWS - 1, 8, ClickableItem.of(ItemHelper.getGoBackItem(), e -> {
            SoundManager.playClickSound(player);
            parent.inv.open(player);
        }));

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1));
        iterator.allowOverride(false);
        pagination.addToIterator(iterator);
    }

    // ===== Items =====

    private ItemStack getDropItem(GameLootDrops drop, boolean enabled) {
        ItemStack item = ItemHelper.setHideFlags(drop.getItem(), true);
        ItemMeta meta = item.getItemMeta();

        // The drop's own lore already ends with its rarity
        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) lore.addAll(meta.getLore());
        lore.add("");
        lore.add("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled"));
        lore.add("");
        lore.add("&e&nLeft Click&r &eto enable");
        lore.add("&e&nRight Click&r &eto disable");

        return ItemHelper.setDetails(item, meta.getDisplayName(), lore.toArray(new String[0]));
    }

    private ItemStack getRandomizerItem(int shownCount) {
        int count = RANDOM_COUNTS[randomCountIndex];
        return ItemHelper.setDetails(ItemHelper.createSkullTexture(RANDOMIZER_TEXTURE),
                "&6Randomize List",
                "&7Enables &a" + count + " &7random " + (count == 1 ? "item" : "items") + " out of",
                "&7the &a" + shownCount + " &7shown and disables the rest",
                "",
                "&eLeft Click to randomize",
                "&eRight click to cycle &namount");
    }

    private ItemStack getPresetsItem(int enabledCount) {
        return ItemHelper.setDetails(new ItemStack(Material.BOOKSHELF),
                "&6Item Presets",
                "&fEnabled: &a" + enabledCount + "&7/&a" + GameLootDrops.values().length,
                "",
                "&eLeft Click to enable all",
                "&eRight Click to disable all");
    }

    private ItemStack getFilterItem() {
        return ItemHelper.setDetails(new ItemStack(Material.HOPPER),
                "&6Filtering: &a" + rarityName(rarity) + "/" + status.name,
                "&7Filter specific item rarities and statuses",
                "",
                "&7Next: &a" + rarityName(nextRarity()),
                "&eLeft click to cycle &nrarity",
                "",
                "&7Next: &a" + next(Status.values(), status).name,
                "&eRight click to cycle &nstatus");
    }

    private ItemStack getSorterItem() {
        return ItemHelper.setDetails(new ItemStack(Material.HOPPER_MINECART),
                "&6Sorted by: &a" + sorter.name,
                "&7Sort the item list",
                "",
                "&7Next: &a" + next(Sorter.values(), sorter).name,
                "&eLeft click to cycle");
    }

    // ===== Clicks =====

    private void handleItemClick(InventoryClickEvent e, Player player, GameLootDrops drop, int page) {
        if (!canModify(player)) return;

        Set<GameLootDrops> enabled = game.getGameSettings().enabledDrops;
        if (!e.isLeftClick() && !e.isRightClick()) return;
        SoundManager.playClickSound(player);

        String name = drop.getItem().getItemMeta().getDisplayName();
        if (e.isLeftClick()) {
            if (!enabled.add(drop)) return;
            game.TellAll(main.color("&2&l(!) &r" + name + " &rhas been &aenabled"));
        } else {
            if (!enabled.remove(drop)) return;
            game.TellAll(main.color("&2&l(!) &r" + name + " &rhas been &cdisabled"));
            warnIfNoneEnabled(player);
        }

        inv.open(player, page);
        game.refreshEditorMenus(player); // the other of host / co-host sees it too
    }

    private void handleRandomizerClick(InventoryClickEvent e, Player player) {
        if (e.isRightClick()) {
            SoundManager.playClickSound(player);
            randomCountIndex = (randomCountIndex + 1) % RANDOM_COUNTS.length;
            inv.open(player);
            return;
        }
        if (!e.isLeftClick() || !canModify(player)) return;

        List<GameLootDrops> pool = getShownItems();
        if (pool.isEmpty()) return;
        Collections.shuffle(pool);

        Set<GameLootDrops> enabled = game.getGameSettings().enabledDrops;
        enabled.clear();
        enabled.addAll(pool.subList(0, Math.min(RANDOM_COUNTS[randomCountIndex], pool.size())));

        SoundManager.playClickSound(player);
        game.TellAll(main.color("&2&l(!) &rThe item list has been &erandomized&r: &a" + enabled.size()
                + " &r" + (enabled.size() == 1 ? "item is" : "items are") + " enabled."));
        inv.open(player);
        game.refreshEditorMenus(player);
    }

    private void handlePresetsClick(InventoryClickEvent e, Player player) {
        if (!canModify(player)) return;

        Set<GameLootDrops> enabled = game.getGameSettings().enabledDrops;
        if (e.isLeftClick()) {
            enabled.addAll(Arrays.asList(GameLootDrops.values()));
            game.TellAll(main.color("&2&l(!) &rAll lightning drop items have been &aenabled&r."));
        } else if (e.isRightClick()) {
            enabled.clear();
            status = Status.DISABLED; // show what was just disabled
            game.TellAll(main.color("&2&l(!) &rAll lightning drop items have been &cdisabled&r."));
            warnIfNoneEnabled(player);
        } else {
            return;
        }

        SoundManager.playClickSound(player);
        inv.open(player);
        game.refreshEditorMenus(player);
    }

    private void handleFilterClick(InventoryClickEvent e, Player player) {
        if (e.isLeftClick()) rarity = nextRarity();
        else if (e.isRightClick()) status = next(Status.values(), status);
        else return;

        SoundManager.playClickSound(player);
        inv.open(player);
    }

    private void handleSortClick(InventoryClickEvent e, Player player) {
        if (!e.isLeftClick()) return;

        sorter = next(Sorter.values(), sorter);
        SoundManager.playClickSound(player);
        inv.open(player);
    }

    private void warnIfNoneEnabled(Player player) {
        if (game.getGameSettings().enabledDrops.isEmpty())
            player.sendMessage(main.color("&e&l(!) &rNo items are enabled - lightning drops will use every "
                    + "item until you enable some."));
    }

    private boolean canModify(Player player) {
        if (game.state == GameState.WAITING && game.canEditCustomGame(player) && !game.isCustomGameCountdownStarted())
            return true;
        player.sendMessage(main.color("&c&l(!) &rThis game can no longer be modified."));
        inv.close(player);
        return false;
    }

    // ===== Filtering & Sorting =====

    private List<GameLootDrops> getShownItems() {
        Set<GameLootDrops> enabled = game.getGameSettings().enabledDrops;
        List<GameLootDrops> shown = new ArrayList<>();

        for (GameLootDrops drop : GameLootDrops.values()) {
            if (rarity != null && drop.getRarity() != rarity) continue;
            if (status == Status.ENABLED && !enabled.contains(drop)) continue;
            if (status == Status.DISABLED && enabled.contains(drop)) continue;
            shown.add(drop);
        }

        Comparator<GameLootDrops> byName = Comparator.comparing(ItemListGUI::getPlainName);
        if (sorter == Sorter.RARITY) {
            shown.sort(Comparator.comparing(GameLootDrops::getRarity).thenComparing(byName));
        } else {
            shown.sort(byName);
        }
        return shown;
    }

    private static String getPlainName(GameLootDrops drop) {
        return ChatColor.stripColor(drop.getItem().getItemMeta().getDisplayName());
    }

    /** The next rarity filter: All, then each rarity from Common to Legendary. */
    private Rarity nextRarity() {
        Rarity[] rarities = Rarity.values();
        if (rarity == null) return rarities[0];
        return rarity.ordinal() + 1 < rarities.length ? rarities[rarity.ordinal() + 1] : null;
    }

    private static String rarityName(Rarity rarity) {
        return rarity == null ? "All" : rarity.getName();
    }

    private static <T> T next(T[] values, T current) {
        return values[(Arrays.asList(values).indexOf(current) + 1) % values.length];
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
