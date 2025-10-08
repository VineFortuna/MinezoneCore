package anthony.SuperCraftBrawl.gui.halloween;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.halloween.BasketItemUtil;

import java.util.Arrays;

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
    private static final String BASKET_B64 =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzEzODVhN2FmYjM1NTJmYWY3MWMyYzVhOGU2YTViMWQyZTY3MmM3ODZlODA3NDQzM2ViNTgzOWFjZTgzYjQifX19";

    public HalloweenNpcGUI(Core main) {
        this.main = main;
        this.inv = SmartInventory.builder()
                .id("halloweenHunt")
                .provider(this)
                .size(2, 9) // <-- need 2 rows since we use row index 1 below
                .title(String.valueOf(ChatColor.DARK_GRAY) + "Halloween Hunt")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        // Base head (do NOT edit this directly for each slot)
        ItemStack base = BasketItemUtil.customHead(
                BASKET_B64,
                main.color("&6Candy Basket"),
                Arrays.asList(
                        main.color("&7Right-click these baskets around the lobby!"),
                        ChatColor.DARK_PURPLE + "Happy Halloween!"
                )
        );

        // helper to clone + set per-item details
        java.util.function.BiFunction<String, String, ItemStack> make =
                (title, hint) -> ItemHelper.setDetails(
                        base.clone(),                          // <-- clone so each slot has its own stack
                        main.color(title),
                        "",
                        main.color("&7Hint: " + hint)
                );

        contents.set(0, 0, ClickableItem.of(make.apply("&c&lBasket #1",  "Spawn area"), e -> {}));
        contents.set(0, 1, ClickableItem.of(make.apply("&c&lBasket #2",  "City Parkour"), e -> {}));
        contents.set(0, 2, ClickableItem.of(make.apply("&c&lBasket #3",  "The Woods"), e -> {}));
        contents.set(0, 3, ClickableItem.of(make.apply("&c&lBasket #4",  "Markets"), e -> {}));
        contents.set(0, 4, ClickableItem.of(make.apply("&c&lBasket #5",  "Houses"), e -> {}));
        contents.set(0, 5, ClickableItem.of(make.apply("&c&lBasket #6",  "City Park"), e -> {}));
        contents.set(0, 6, ClickableItem.of(make.apply("&c&lBasket #7",  "City Hospital"), e -> {}));
        contents.set(0, 7, ClickableItem.of(make.apply("&c&lBasket #8",  "Cobweb Cave (A.K.A Lush Cave)"), e -> {}));
        contents.set(0, 8, ClickableItem.of(make.apply("&c&lBasket #9",  "Pond"), e -> {}));

        // second row
        contents.set(1, 0, ClickableItem.of(make.apply("&c&lBasket #10", "Castle"), e -> {}));
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
