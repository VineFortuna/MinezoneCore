package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.ChatColorHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ConfirmationGUI implements InventoryProvider {

    @FunctionalInterface
    public interface ConfirmationAction {
        void performAction(Player player);
    }

    public Core main;
    public SmartInventory inv;
    private final ConfirmationAction confirmAction;
    private final ConfirmationAction cancelAction;

    public ConfirmationGUI(Core main, String title, ConfirmationAction confirmAction, ConfirmationAction cancelAction) {
        this.main = main;
        this.confirmAction = confirmAction;
        this.cancelAction = cancelAction;

        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(1, 9)
                .title(ChatColorHelper.color("&8&l" + title))
                .build();
    }
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(0, 2, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.EMERALD_BLOCK),
                "&aConfirm"),
                e -> {
                    if (confirmAction != null) {
                        confirmAction.performAction(player);
                }
                }));

        contents.set(0, 6, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.REDSTONE_BLOCK),
                "&cCancel"),
                e -> {
                    if (cancelAction != null)
                        cancelAction.performAction(player);
                    inv.close(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
