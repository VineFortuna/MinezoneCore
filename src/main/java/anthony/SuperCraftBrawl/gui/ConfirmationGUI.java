package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class ConfirmationGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public ConfirmationGUI(Core main, String title) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(1, 9)
                .title(ChatColorHelper.color("&8&l" + title))
                .build();
        this.main = main;
    }
    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(0, 2, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.EMERALD_BLOCK),
                "&aConfirm"),
                e -> {
                    main.getCommands().leaveGame(player);
                }));

        contents.set(0, 6, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.REDSTONE_BLOCK),
                "&cCancel"),
                e -> {
                    inv.close(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
