package anthony.SuperCraftBrawl.gui.Classes;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassBase;
import anthony.SuperCraftBrawl.Game.classes.ClassMetaDataManager;
import anthony.SuperCraftBrawl.Game.classes.dataTypes.ClassCategory;
import anthony.SuperCraftBrawl.gui.ClassMasteryGUI;
import anthony.SuperCraftBrawl.mastery.MasteryTier;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import io.vavr.control.Either;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.floorDiv;

abstract public class ClassBaseGUI implements InventoryProvider {
    protected static int DEFAULT_GUI_WIDTH   = 3;
    protected static int DEFAULT_GUI_HEIGHT  = 9;
    protected ClassCategory class_category   = ClassCategory.INVALID;

    protected ClassMetaDataManager metaDataManager;
    protected SmartInventory inventory;
    protected Core core;

    public Core getCore() {
        return core;
    }

    public SmartInventory getInventory() {
        return inventory;
    }

    private SmartInventory createInventory(String GUIName, int rows, int cols) {
        return SmartInventory.builder().id("myInventory").provider(this).size(rows, cols)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + GUIName).build();

    }

    ClassBaseGUI(Core main, ClassCategory category, int rows, int cols, String GUIName) {
        /* Assign 'Core' object. */
        core = main;

        /* Assign the category this GUI belongs to. */
        class_category = category;

        /* Default initialize these parameters in ugly ways. */
        int gui_rows = (rows == 0) ? DEFAULT_GUI_WIDTH  : rows;
        int gui_cols = (cols == 0) ? DEFAULT_GUI_HEIGHT : cols;

        /* Create inventory object. */
        inventory = createInventory(GUIName, gui_rows, gui_cols);

        /* Initialize metadata manager */
        metaDataManager = new ClassMetaDataManager();
    }

    private ItemStack getBackArrow() {
        return ItemHelper.setDetails(ItemHelper.create(Material.ARROW), ChatColor.GRAY + "Go Back");
    }

    private List<String> createClassDescription(Class<? extends ClassBase> gameClass) {
        String text = ClassMetaDataManager.getClassDescription(gameClass);
        if (text == null)
            return null;

        final int maxLength = 30;

        String[] split = text.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : split) {
            if (current.length() + word.length() + 1 <= maxLength) {
                current.append(word).append(' ');
            } else {
                if (!current.isEmpty())
                    lines.add(net.md_5.bungee.api.ChatColor.GRAY + current.substring(0, current.length() - 1));
                current = new StringBuilder(word).append(' ');
            }
        }
        if (!current.isEmpty())
            lines.add(net.md_5.bungee.api.ChatColor.GRAY + current.substring(0, current.length() - 1));

        return lines;
    }

    private List<String> getButtonOptions() {
        return List.of(
            core.color(""                                               ),
            core.color("&e&nLeft Click&r &eto choose a class"           ),
            core.color("&e&nRight Click&r &eto view mastery"            ),
            core.color("&&e&nShift Click&r &eto remove a favorite class"),
            core.color("&e&nLeft Click&r &eto choose a class"           )
        );
    }

    private void handleShiftClick(Player player, Class<? extends ClassBase> gameClass) {
        PlayerData data = core.getDataManager().getPlayerData(player);
        if (data != null) {
            if (!data.customIntegers.contains(ClassMetaDataManager.getClassUniqueIdentifier(gameClass))) {
                data.customIntegers.add(ClassMetaDataManager.getClassUniqueIdentifier(gameClass));
                player.sendMessage(core.color("&2&l(!) &rAdded new favorite class: " + ClassMetaDataManager.getClassTitle(gameClass)));
                core.getDataManager().saveData(data);
            } else {
                player.sendMessage(core.color("&c&l(!) &r" + ClassMetaDataManager.getClassTitle(gameClass) + " &ris already one of your favorites!"));
            }
        }
    }

    private void handleLeftClick(Player player, Class<? extends ClassBase> gameClass) {
        core.getGameManager().playerSelectClass(player, gameClass);
        player.sendMessage(core.color("&2&l============================================="));
        player.sendMessage(core.color("&2&l|| "));
        player.sendMessage(core.color("&2&l|| "));
        player.sendMessage(core.color("&2&l|| " + "&e&lSelected Class: " + ClassMetaDataManager.getClassTitle(gameClass)));
        player.sendMessage(core.color("&2&l|| " + "&e&lClass Desc: &e" + ClassMetaDataManager.getClassDescription(gameClass)));
        player.sendMessage(core.color("&2&l|| "));
        player.sendMessage(core.color("&2&l|| "));
        player.sendMessage(core.color("&2&l============================================="));
        inventory.close(player);
    }

    private void handleRightClick(Player player, Class<? extends ClassBase> gameClass) {
        new ClassMasteryGUI(core, gameClass, inventory).inventory.open(player);
    }

    public void init(Player player, InventoryContents inventoryContents) {
        PlayerData data = core.getDataManager().getPlayerData(player);

        inventoryContents.set(GUI_WIDTH - 1, GUI_HEIGHT - 1, ClickableItem.of(
                getBackArrow(),
                e -> { /* new ClassesGUI( ... ) */ }
        ));

        Set<Class<? extends ClassBase>> relevantClasses =  metaDataManager.getAllClassesOfCategory(class_catgeory);

        int i = 0;
        for (Class<? extends ClassBase> gameClass : relevantClasses) {
            ClassDetails classDetails = data.playerClasses.get(0); // TODO: actually change the hashmap.

            List<String> lore = new ArrayList<>();
            lore.addAll(Objects.requireNonNull(createClassDescription(gameClass)));

            // TODO:
//            if (gameClass instanceof ClassFisherman) {
//                lore.addAll( ... );
//            }

            lore.addAll(getButtonOptions());
            lore.addAll(MasteryTier.getRewardAndProgressBar(classDetails));

            // TODO: Move somewhere else?
            ItemStack icon = ClassMetaDataManager.getClassIcon(gameClass).fold(
                    ItemHelper::createSkullBlock,
                    ItemHelper::createSkullTexture
            );

            inventoryContents.set(floorDiv(i, 9), i % 9, ClickableItem.of(
                    ItemHelper.setDetails(icon, ClassMetaDataManager.getClassTitle(gameClass), lore),
                    e -> {
                        if      (e.isShiftClick()) handleShiftClick(player, gameClass);
                        else if (e.isLeftClick())  handleLeftClick( player, gameClass);
                        else if (e.isRightClick()) handleRightClick(player, gameClass);
                    }
            ));

            i++;
        }
    }

    @Override // Not needed for now.
    public void update(Player player, InventoryContents inventoryContents) {
        /* ... */
    }
}
