package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LobbyCosmeticsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public LobbyCosmeticsGUI(Core main,  SmartInventory parent) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Lobby Cosmetics")
                .parent(parent)
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        // Icon Items
        ItemStack gadgets = ItemHelper.create(Material.GOLD_BARDING, ChatColor.YELLOW + "Gadgets");

        ItemStack lobbyOutfits = ItemHelper.create(Material.GOLD_HELMET, ChatColor.YELLOW + "Outfits");
        
        ItemStack titles = ItemHelper.create(Material.NAME_TAG, main.color("&eTitles"));

        // Setting Icons
        contents.fill(ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

        contents.set(1, 2, ClickableItem.of(
                gadgets,
                e -> {
                    new GadgetsGUI(main, inv).inv.open(player);
                }));
        
        contents.set(1, 4, ClickableItem.of(
				titles,
				e -> {
					new TitlesCosmeticsGUI(main, inv).inv.open(player);
				}));

        contents.set(1, 6, ClickableItem.of(
                lobbyOutfits,
                e -> {
                    new SuitsGUI(main, inv).inv.open(player);
                }));

        contents.set(2, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }
        ));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
