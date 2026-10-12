package anthony.SuperCraftBrawl.cosmetics;

import anthony.util.ItemHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** A full set of worn armor for an OUTFIT cosmetic. */
public final class Outfit {

    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;

    /** Stamps the same display name onto every piece, so callers only ever have to define it once. */
    public Outfit(String name, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        this.helmet = ItemHelper.setDetails(helmet, name);
        this.chestplate = ItemHelper.setDetails(chestplate, name);
        this.leggings = ItemHelper.setDetails(leggings, name);
        this.boots = ItemHelper.setDetails(boots, name);
    }

    public void equip(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);
        player.updateInventory();
    }
}