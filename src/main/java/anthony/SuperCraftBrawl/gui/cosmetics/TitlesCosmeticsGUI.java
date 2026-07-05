package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TitlesCosmeticsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public TitlesCosmeticsGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(4, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Titles").parent(parent).build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem border = ClickableItem
                .of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
                });

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                if (row == 0 || row == 3 || col == 0 || col == 8) {
                    contents.set(row, col, border);
                }
            }
        }

        int i = 0;
        for (Cosmetic title : main.getCosmeticManager().registry().getAll(CosmeticCategory.TITLE)) {
            int row = 1 + (i / 7);
            int col = 1 + (i % 7);
            contents.set(row, col, CosmeticGuiUtil.toggleItem(main, main.getCosmeticManager(), title, player,
                    title.getIcon(player), inv));
            i++;
        }

        contents.set(3, 8, ClickableItem
                .of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
