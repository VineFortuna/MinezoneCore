package anthony.SuperCraftBrawl.Game.map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * The items /scbsetup gives you, in the slots they go in (0 is the first hotbar slot). The locations and the boundaries
 * have an item each; everything else about the map (source, size, gameplay, category, game type, icon) is set in the
 * one menu of the settings item.
 */
enum SetupTool {
    MODE(0, Material.COMPASS, "Spectator Mode"),
    SPAWNS(1, Material.BLAZE_ROD, "Spawn Locations"),
    BOUNDS(2, Material.GOLD_AXE, "Map Boundaries"),
    LOBBY(3, Material.NETHER_STAR, "Lobby Location"),
    SPECTATOR(4, Material.EYE_OF_ENDER, "Spectator Location"),
    SETTINGS(8, Material.BOOK, "Map Settings");

    /** The slot it is put in, and put back in by /scbsetup tools. */
    final int slot;
    final Material material;
    final String title;

    SetupTool(int slot, Material material, String title) {
        this.slot = slot;
        this.material = material;
        this.title = title;
    }

    /** Which tool this item is, or null if it is not one (it has to have the tool's item and its name). */
    static SetupTool of(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return null;

        String name = ChatColor.stripColor(meta.getDisplayName());
        for (SetupTool tool : values()) {
            if (tool.material == item.getType() && tool.title.equals(name)) return tool;
        }
        return null;
    }
}
