package anthony.SuperCraftBrawl.gui.halloween;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.halloween.BasketItemUtil;

import java.util.Arrays;
import java.util.function.BiFunction;

import anthony.SuperCraftBrawl.halloween.HalloweenDAO;
import anthony.SuperCraftBrawl.halloween.HalloweenHuntManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class HalloweenNpcGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public HalloweenNpcGUI(Core main) {
        this.main = main;
        this.inv = SmartInventory.builder()
                .id("halloweenHunt")
                .provider(this)
                .size(4, 9) // <-- need 2 rows since we use row index 1 below
                .title(String.valueOf(ChatColor.DARK_GRAY) + "Halloween Hunt")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

        // List of basket hints/names
        String[] basketNames = {
                "Map selection",
                "Houses",
                "City Parkour",
                "Cobweb Cave (A.K.A Lush Cave)",
                "The Woods",
                "Pond",
                "Markets",
                "City Hospital",
                "City Park",
                "Castle"
        };

        HalloweenDAO dao = main.getHalloweenManager().getDao();

        // Loop through all baskets and place them in inventory
        for (int i = 0; i < basketNames.length; i++) {
            int row, col;

            // Decide row/column based on index
            if (i < 5) {         // first row (1)
                row = 1;
                col = i + 2;     // columns 2-6
            } else {             // second row (2)
                row = 2;
                col = i - 3;     // columns 2-6
            }

            int id = i + 1;
            boolean status = dao.hasFound(player.getUniqueId(), i);

            String basketTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEzODVhN2FmYjM1NTJmYWY3MWMyYzVhOGU2YTViMWQyZTY3MmM3ODZlODA3NDQzM2ViNTgzOWFjZTgzYjQifX19";
            String notFoundTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjliYTdmZWY2YTFhOGJkODk5YWJhZTRhNWI1NGNiMGVjZTUzYmFkYzY3N2MxNjY4YmVlMGE0NjIxYTgifX19";

            ItemStack basketItem = ItemHelper.setDetails(ItemHelper.createSkullTexture(status ? basketTexture : notFoundTexture),
                    main.color("&6&lBasket &6#" + id + " &8[" + (status ? "&a✔" : "&c✖") + "&8]"),
                    main.color("&f&lHint&f: &7" + basketNames[i])
            );

            contents.set(row, col, ClickableItem.of(basketItem, e -> {}));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
