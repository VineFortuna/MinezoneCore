package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.DuelsWagerManager;
import anthony.SuperCraftBrawl.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DuelsWagerGUI implements InventoryProvider {
    public Core main;
    public SmartInventory inv;
    public Player targetPlayer;

    List<ItemStack> itemIcons = new ArrayList<>();

    public DuelsWagerGUI(Core main, Player targetPlayer) {
        inv = SmartInventory.builder()
                .id("myInventory").provider(this)
                .size(1, 9)
                .title(ChatColorHelper.color("&rDueling &l" + targetPlayer.getName()))
                .build();

        this.main = main;
        this.targetPlayer = targetPlayer;

        // Setting possible wager values
        itemIcons.add(getItemIcon(0));
        itemIcons.add(getItemIcon(10));
        itemIcons.add(getItemIcon(25));
        itemIcons.add(getItemIcon(50));
        itemIcons.add(getItemIcon(100));
        itemIcons.add(getItemIcon(250));
        itemIcons.add(getItemIcon(500));
        itemIcons.add(getItemIcon(1000));
        itemIcons.add(getItemIcon(2500));
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        int column = 0;

        if (itemIcons.size() <= inv.getRows() * inv.getColumns()) {
            for (ItemStack itemStack : itemIcons) {
                contents.set(0, column,
                        ClickableItem.of(itemStack, e -> {
//                            DuelsWagerManager duelsWagerManager = new DuelsWagerManager(main, player, targetPlayer);
//
//                            duelsWagerManager.sendRequest(itemStack.getAmount());
//                            duelsWagerManager.

                        }));

                column++;
            }
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

    /**
     * Returns the itemStack to be set in the wager GUI.
     * Sets the item displayName to show the amount of tokens to be bet.
     *
     * @param amount The amount of tokens to be bet
     */
    public ItemStack getItemIcon(int amount) {
        ItemStack item = ItemHelper.setDetails(new ItemStack(Material.EMERALD, amount),
                "&a" + amount + " tokens");

        return item;
    }
}
