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
        ALL("All"), FREE("Free"), TOKEN("Token"), LEVEL("Level"), RANK("Rank"), VAULTED("Vaulted"), HOLIDAY("Holiday");

        final String name;

        Category(String name) {
            this.name = name;
        }
    }

    private enum Status {
        ALL("All"), ENABLED("Enabled"), DISABLED("Disabled");

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
    private Status status = Status.ALL;
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

        contents.set(ROWS - 1, 0, ClickableItem.of(getEnableAllItem(enabled.size()), e -> handleEnableAllClick(e, player)));
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

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&fStatus: " + (enabled ? "&aEnabled" : "&cDisabled"));
        lore.add("&fCost: " + getCostString(type));
        lore.add("&fUnlocked: " + (isUnlocked(viewer, type) ? "&aYes" : "&cNo"));
        if (type.isVaulted()) lore.add("&6&lVAULTED");
        if (type.isHoliday()) lore.add("&c&lHOLIDAY");
        lore.add("");
        lore.add("&e&nLeft Click&r &eto enable");
        lore.add("&e&nRight Click&r &eto disable");

        return ItemHelper.setDetails(item, type.getTag(), lore.toArray(new String[0]));
    }

    private ItemStack getRandomizerItem(int shownCount) {
        int count = RANDOM_COUNTS[randomCountIndex];
        return ItemHelper.setDetails(ItemHelper.createSkullTexture(RANDOMIZER_TEXTURE),
                "&6Randomize List (" + count + ")",
                "&7Enables &a" + count + " &7random classes out of",
                "&7the &a" + shownCount + " &7shown and disables the rest",
                "",
                "&eLeft Click to randomize",
                "&eRight Click to change the amount");
    }

    private ItemStack getEnableAllItem(int enabledCount) {
        return ItemHelper.setDetails(new ItemStack(Material.EMERALD_BLOCK),
                "&a&lEnable All",
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
                "&7Next: &a" + next(Category.values(), category).name,
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
        if (e.isLeftClick()) {
            if (!enabled.add(type)) return;
            game.TellAll(main.color("&2&l(!) &r" + type.getTag() + " &rhas been &aenabled"));
        } else if (e.isRightClick()) {
            if (!enabled.remove(type)) return;
            game.TellAll(main.color("&2&l(!) &r" + type.getTag() + " &rhas been &cdisabled"));
            warnIfNoneEnabled(player);
        } else {
            return;
        }

        SoundManager.playSuccessfulHit(player);
        inv.open(player, page);
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

        SoundManager.playSuccessfulHit(player);
        game.TellAll(main.color("&2&l(!) &rThe class list has been &erandomized&r: &a" + enabled.size()
                + " &rclasses are enabled."));
        inv.open(player);
    }

    private void handleEnableAllClick(InventoryClickEvent e, Player player) {
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

        SoundManager.playSuccessfulHit(player);
        inv.open(player);
    }

    private void handleFilterClick(InventoryClickEvent e, Player player) {
        if (e.isLeftClick()) category = next(Category.values(), category);
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
        if (game.state == GameState.WAITING && game.isPrivateGameLeader(player)) return true;
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
        if (type.getMinRank() != null) return Category.RANK;
        return Category.FREE;
    }

    // Free < Token < Level < Rank, then by the amount within each
    private int getCostTier(ClassType type) {
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
        if (type.getTokenCost() > 0) return "&a" + type.getTokenCost() + " Tokens";
        if (type.getLevel() > 0) return "&bLevel " + type.getLevel();
        if (type.getMinRank() != null) return type.getMinRank().getTag();
        return "&aFree";
    }

    /** Whether the viewer could pick this class themselves (same checks as random class selection). */
    private boolean isUnlocked(Player viewer, ClassType type) {
        PlayerData data = main.getDataManager().getPlayerData(viewer);
        if (data == null) return false;

        ClassDetails details = data.playerClasses.get(type.getID());
        boolean bought = type.getTokenCost() == 0 || (details != null && details.purchased);
        if (!bought || data.level < type.getLevel()) return false;
        if (type == ClassType.Fisherman && !main.getFishing().hasUnlockedFisherman(viewer)) return false;
        if (type == ClassType.Freddy && !main.getHalloweenManager().hasUnlockedFreddy(viewer)) return false;
        return type.getMinRank() == null || viewer.hasPermission("scb." + type.getMinRank().toString().toLowerCase());
    }

    private static <T> T next(T[] values, T current) {
        return values[(Arrays.asList(values).indexOf(current) + 1) % values.length];
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
