package anthony.SuperCraftBrawl.gui.christmas;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.christmas.ChristmasHuntDAO;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class ChristmasHuntGUI implements InventoryProvider {

    private static final String FOUND_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FhMDNmODE3MzQxY2FhNTZmYmZjNmQzZjI4MTFhZGI5ODliNjExNzgxMTYyYzEyYWY0YzU4YWYxOGM1M2M3MiJ9fX0=";
    private static final String NOT_FOUND_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjFiYzlkNDJiMDA0MWU4Zjk1Y2I5YjI2NjI4ZmRhZjUwY2QwZTM2ZjdiYjlkNmIzYTRkMmFmMzk0OWRhOTdkNiJ9fX0=";

    private static final String[] HINTS = {
            "Christmas Tree",
            "Lush Cave",
            "Castle",
            "City Casino",
            "City Ice Cream Parlour",
            "City Park",
            "Parkour",
            "Houses",
            "Pond",
            "Markets",
            "City Hospital",
            "City Park",
            "Castle"
    };

    public Core main;
    public SmartInventory inv;

    public ChristmasHuntGUI(Core main) {
        this.main = main;
        this.inv = SmartInventory.builder()
                .id("christmasHunt")
                .provider(this)
                .size(4, 9)
                .title(ChatColor.DARK_GRAY + "Present Hunt")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill(ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {}));

        ChristmasHuntDAO dao = main.getChristmasManager().getDao();

        for (int i = 0; i < HINTS.length; i++) {
            // 5 per row, columns 2-6, starting on row 1
            int row = 1 + (i / 5);
            int col = 2 + (i % 5);

            boolean found = dao.hasFound(player.getUniqueId(), i);

            ItemStack present = ItemHelper.setDetails(
                    ItemHelper.createSkullTexture(found ? FOUND_TEXTURE : NOT_FOUND_TEXTURE),
                    main.color("&c&lPresent &c#" + (i + 1) + " &8[" + (found ? "&a✔" : "&c✖") + "&8]"),
                    main.color("&f&lHint&f: &7" + HINTS[i]));

            contents.set(row, col, ClickableItem.of(present, e -> {}));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}