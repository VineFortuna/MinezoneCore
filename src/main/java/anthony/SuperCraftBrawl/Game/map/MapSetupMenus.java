package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * What the /scbsetup menus are made of: items in the look of the other menus of the game (gold names, gray panes,
 * yellow click lines), and the menu where an item of the player's own inventory is picked as the map's icon.
 * The settings menu itself is {@link MapSettingsMenu}.
 */
final class MapSetupMenus {

    private MapSetupMenus() {
    }

    /** An item with a name and lore (both with & colour codes). The attributes of weapons and tools are hidden. */
    static ItemStack item(Core main, ItemStack base, String name, List<String> lore) {
        ItemStack item = base.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(main.color(name));
        List<String> colored = new ArrayList<String>();
        for (String line : lore) colored.add(main.color(line));
        meta.setLore(colored);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    /** Makes the item shine, without showing the enchantment. */
    static ItemStack glow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    static ItemStack pane(Core main) {
        return item(main, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " ", new ArrayList<String>());
    }

    static List<String> lines(String... lines) {
        List<String> list = new ArrayList<String>();
        for (String line : lines) list.add(line);
        return list;
    }

    // ---------------------------------------------------------------- the icon

    /** Marks the icon menu, which is an ordinary inventory so that clicks in the player's own inventory can be seen. */
    static final class IconPickerHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    static boolean isIconPicker(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof IconPickerHolder;
    }

    /** The menu where clicking an item in your own inventory makes it the map's icon. */
    static void iconPicker(Core main, Player player, MapDraft draft) {
        IconPickerHolder holder = new IconPickerHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, main.color("&8&lPick the Icon"));
        holder.inventory = inventory;

        ItemStack pane = pane(main);
        for (int slot = 0; slot < 27; slot++) inventory.setItem(slot, pane);

        ItemStack current;
        if (draft.iconType == null) {
            current = item(main, new ItemStack(Material.BARRIER), "&6&lNo Icon Yet",
                    lines("", "&7Click an item in your own", "&7inventory to use it as the icon."));
        } else {
            current = item(main, draft.icon(), "&6&lCurrent Icon",
                    lines("", "&7Click an item in your own", "&7inventory to use it instead.", "",
                            "&fCurrently: &a" + iconName(draft)));
        }
        inventory.setItem(13, current);
        player.openInventory(inventory);
    }

    /** The icon as words, like "stained glass pane 5". */
    static String iconName(MapDraft draft) {
        if (draft.iconType == null) return "Not set";
        String name = draft.iconType.name().toLowerCase().replace('_', ' ');
        return draft.iconData == 0 ? name : name + " " + draft.iconData;
    }
}
