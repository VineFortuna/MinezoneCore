package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/*
 * Private game modifier: the leader picks which classes players can choose from in the Class Selector.
 * Left click enables a class, right click disables it. Vaulted classes are listed too so they can be enabled.
 * Laid out like the maps GUI: randomizer on top, page arrows on the sides, controls along the bottom.
 */
public class ClassListGUI implements InventoryProvider {

    private static final int ROWS = 6;
    private static final int[] RANDOM_COUNTS = {5, 10, 15, 20};
    private static final String RANDOMIZER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCJ9fX0=";

    private enum Category {
        ALL("All", ""), FREE("Free", "&a&lFREE"), TOKEN("Token", "&e&lTOKEN"),
        LEVEL("Level", "&d&lLEVEL"), DONOR("Donor", "&c&lDONOR"), VAULTED("Vaulted", "&6&lVAULTED"),
        HOLIDAY("Holiday", "&c&lHOLIDAY");

        final String name;
        final String label; // shown under a class's description

        Category(String name, String label) {
            this.name = name;
            this.label = label;
        }
    }

    private enum Status {
        ENABLED("Enabled"), DISABLED("Disabled"), ALL("All");

        final String name;

        Status(String name) {
            this.name = name;
        }
    }

    private enum Sorter {
        ALPHABETICAL("Alphabetical"), COST("Cost"), UNLOCKED("Unlocked");

        final String name;

        Sorter(String name) {
            this.name = name;
        }
    }

    private final Core main;
    private final GameInstance game;
    private final GameModifiersGUI parent;
    public final SmartInventory inv;

    private Category category = Category.ALL;
    private Status status = Status.ENABLED;
    private Sorter sorter = Sorter.ALPHABETICAL;
    private int randomCountIndex = 1; // 10 classes

    public ClassListGUI(Core main, GameInstance game, GameModifiersGUI parent) {
        this.main = main;
        this.game = game;
        this.parent = parent;

        this.inv = SmartInventory.builder()
                .id("classList")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Class List")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        Set<ClassType> enabled = game.getGameSettings().enabledClasses;
        List<ClassType> shown = getShownClasses(player);

        ClickableItem[] items = new ClickableItem[shown.size()];
        for (int i = 0; i < shown.size(); i++) {
            ClassType type = shown.get(i);
            items[i] = ClickableItem.of(getClassItem(player, type, enabled.contains(type)),
                    e -> handleClassClick(e, player, type, pagination.getPage()));
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

    private ItemStack getClassItem(Player viewer, ClassType type, boolean enabled) {
        ItemStack item = type.getItem();
        if (item == null) item = new ItemStack(Material.WOOD);
        item = ItemHelper.setHideFlags(item, true);

        Category category = getCategory(type);
        List<String> lore = new ArrayList<>();
        List<String> description = type.buildDescription();
        if (description != null) lore.addAll(description);
        lore.add("");
        lore.add(category.label);
        lore.add("");
        lore.add("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled"));
        lore.add("&fRequirement: " + getCostString(type));
        // Free and vaulted classes are always playable here, so there's nothing to unlock
        if (category != Category.FREE && category != Category.VAULTED)
            lore.add("&fUnlocked: " + (isUnlocked(viewer, type) ? "&aYes" : "&cNo"));
        lore.add("");
        lore.add("&e&nLeft Click&r &eto enable");
        lore.add("&e&nRight Click&r &eto disable");

        return ItemHelper.setDetails(item, type.getTag(), lore.toArray(new String[0]));
    }

    private ItemStack getRandomizerItem(int shownCount) {
        int count = RANDOM_COUNTS[randomCountIndex];
        return ItemHelper.setDetails(ItemHelper.createSkullTexture(RANDOMIZER_TEXTURE),
                "&6Randomize List",
                "&7Enables &a" + count + " &7random classes out of",
                "&7the &a" + shownCount + " &7shown and disables the rest",
                "",
                "&eLeft Click to randomize",
                "&eRight click to cycle &namount");
    }

    private ItemStack getPresetsItem(int enabledCount) {
        return ItemHelper.setDetails(new ItemStack(Material.BOOKSHELF),
                "&6Class Presets",
                "&fEnabled: &a" + enabledCount + "&7/&a" + ClassType.getPlayableClasses().length,
                "",
                "&eLeft Click to enable all",
                "&eRight Click to disable all",
                "&eShift Click to reset to default",
                "&7(all non vaulted classes)");
    }

    private ItemStack getFilterItem() {
        return ItemHelper.setDetails(new ItemStack(Material.HOPPER),
                "&6Filtering: &a" + category.name + "/" + status.name,
                "&7Filter specific class types and statuses",
                "",
                "&7Next: &a" + nextCategory().name,
                "&eLeft click to cycle &ntype",
                "",
                "&7Next: &a" + next(Status.values(), status).name,
                "&eRight click to cycle &nstatus");
    }

    private ItemStack getSorterItem() {
        return ItemHelper.setDetails(new ItemStack(Material.HOPPER_MINECART),
                "&6Sorted by: &a" + sorter.name,
                "&7Sort the class list",
                "",
                "&7Next: &a" + next(Sorter.values(), sorter).name,
                "&eLeft click to cycle");
    }

    // ===== Clicks =====

    private void handleClassClick(InventoryClickEvent e, Player player, ClassType type, int page) {
        if (!canModify(player)) return;

        Set<ClassType> enabled = game.getGameSettings().enabledClasses;
        if (!e.isLeftClick() && !e.isRightClick()) return;
        SoundManager.playClickSound(player);

        if (e.isLeftClick()) {
            if (!enabled.add(type)) return;
            game.TellAll(main.color("&2&l(!) &r" + type.getTag() + " &rhas been &aenabled"));
        } else {
            if (!enabled.remove(type)) return;
            game.TellAll(main.color("&2&l(!) &r" + type.getTag() + " &rhas been &cdisabled"));
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

        List<ClassType> pool = getShownClasses(player);
        if (pool.isEmpty()) return;
        Collections.shuffle(pool);

        Set<ClassType> enabled = game.getGameSettings().enabledClasses;
        enabled.clear();
        enabled.addAll(pool.subList(0, Math.min(RANDOM_COUNTS[randomCountIndex], pool.size())));

        SoundManager.playClickSound(player);
        game.TellAll(main.color("&2&l(!) &rThe class list has been &erandomized&r: &a" + enabled.size()
                + " &rclasses are enabled."));
        inv.open(player);
        game.refreshEditorMenus(player);
    }

    private void handlePresetsClick(InventoryClickEvent e, Player player) {
        if (!canModify(player)) return;

        Set<ClassType> enabled = game.getGameSettings().enabledClasses;
        if (e.isShiftClick()) {
            enabled.clear();
            enabled.addAll(Arrays.asList(ClassType.getAvailableClasses()));
            game.TellAll(main.color("&2&l(!) &rClass selection has been &ereset &rto the default classes."));
        } else if (e.isLeftClick()) {
            enabled.addAll(Arrays.asList(ClassType.getPlayableClasses()));
            game.TellAll(main.color("&2&l(!) &rAll classes have been &aenabled&r, including vaulted ones."));
        } else if (e.isRightClick()) {
            enabled.clear();
            game.TellAll(main.color("&2&l(!) &rAll classes have been &cdisabled&r."));
            warnIfNoneEnabled(player);
        } else {
            return;
        }

        SoundManager.playClickSound(player);
        inv.open(player);
        game.refreshEditorMenus(player);
    }

    private void handleFilterClick(InventoryClickEvent e, Player player) {
        if (e.isLeftClick()) category = nextCategory();
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
        if (game.getGameSettings().enabledClasses.isEmpty())
            player.sendMessage(main.color("&e&l(!) &rNo classes are enabled - players will pick from the "
                    + "default classes until you enable some."));
    }

    private boolean canModify(Player player) {
        if (game.state == GameState.WAITING && game.canEditCustomGame(player) && !game.isCustomGameCountdownStarted())
            return true;
        player.sendMessage(main.color("&c&l(!) &rThis game can no longer be modified."));
        inv.close(player);
        return false;
    }

    // ===== Filtering & Sorting =====

    private List<ClassType> getShownClasses(Player viewer) {
        Set<ClassType> enabled = game.getGameSettings().enabledClasses;
        List<ClassType> shown = new ArrayList<>();

        for (ClassType type : ClassType.getPlayableClasses()) { // inactive holiday classes aren't listed
            if (category != Category.ALL && getCategory(type) != category) continue;
            if (status == Status.ENABLED && !enabled.contains(type)) continue;
            if (status == Status.DISABLED && enabled.contains(type)) continue;
            shown.add(type);
        }

        Comparator<ClassType> byName = Comparator.comparing(ClassType::toString);
        if (sorter == Sorter.COST) {
            shown.sort(Comparator.comparingInt(this::getCostTier).thenComparingInt(this::getCostAmount).thenComparing(byName));
        } else if (sorter == Sorter.UNLOCKED) {
            shown.sort(Comparator.comparing((ClassType type) -> !isUnlocked(viewer, type)).thenComparing(byName));
        } else {
            shown.sort(byName);
        }
        return shown;
    }

    private static Category getCategory(ClassType type) {
        if (type.isHoliday()) return Category.HOLIDAY;
        if (type.isVaulted()) return Category.VAULTED;
        if (type.getTokenCost() > 0) return Category.TOKEN;
        if (type.getLevel() > 0) return Category.LEVEL;
        if (type.getMinRank() != null) return Category.DONOR;
        return Category.FREE;
    }

    // Free (and vaulted) < Token < Level < Donor, then by the amount within each
    private int getCostTier(ClassType type) {
        if (type.isVaulted()) return 0;
        if (type.getTokenCost() > 0) return 1;
        if (type.getLevel() > 0) return 2;
        if (type.getMinRank() != null) return 3;
        return 0;
    }

    private int getCostAmount(ClassType type) {
        if (type.getTokenCost() > 0) return type.getTokenCost();
        if (type.getLevel() > 0) return type.getLevel();
        if (type.getMinRank() != null) return type.getMinRank().ordinal();
        return 0;
    }

    private String getCostString(ClassType type) {
        // Enabled vaulted classes are playable by anyone (their requirements are kept for if they're unvaulted)
        if (type.isVaulted()) return "&aNone";
        if (type.getTokenCost() > 0) return "&e" + type.getTokenCost() + " Tokens";
        if (type.getLevel() > 0) return "&dLevel " + type.getLevel();
        if (type.getMinRank() != null) return type.getMinRank().getTag();
        return "&aFree";
    }

    /** Whether the viewer could pick this class themselves (same checks as random class selection). */
    private boolean isUnlocked(Player viewer, ClassType type) {
        if (type.isVaulted()) return true;
        PlayerData data = main.getDataManager().getPlayerData(viewer);
        if (data == null) return false;

        ClassDetails details = data.playerClasses.get(type.getID());
        boolean bought = type.getTokenCost() == 0 || (details != null && details.purchased);
        if (!bought || data.level < type.getLevel()) return false;
        if (type == ClassType.Fisherman && !main.getFishing().hasUnlockedFisherman(viewer)) return false;
        return type.getMinRank() == null || viewer.hasPermission("scb." + type.getMinRank().toString().toLowerCase());
    }

    /** The next class type filter, skipping Holiday while no holiday class is active. */
    private Category nextCategory() {
        Category next = next(Category.values(), category);
        if (next == Category.HOLIDAY && !isAnyHolidayClassActive()) next = next(Category.values(), next);
        return next;
    }

    private static boolean isAnyHolidayClassActive() {
        for (ClassType type : ClassType.getPlayableClasses()) {
            if (type.isHoliday()) return true;
        }
        return false;
    }

    private static <T> T next(T[] values, T current) {
        return values[(Arrays.asList(values).indexOf(current) + 1) % values.length];
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
