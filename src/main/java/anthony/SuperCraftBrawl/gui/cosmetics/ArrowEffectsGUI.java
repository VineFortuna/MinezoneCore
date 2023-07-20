package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArrowEffectsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public ArrowEffectsGUI(Core main) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(6, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Arrow Effects")
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        // Icons Items
        ItemStack lockedCosmetic = ItemHelper.createDye(DyeColor.GRAY, 1, ChatColor.GRAY + "&&&&&&&");


        // Setting Items
        contents.fillRect(1,1, 7,7, ClickableItem.of(
                lockedCosmetic,
                e -> {

                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
