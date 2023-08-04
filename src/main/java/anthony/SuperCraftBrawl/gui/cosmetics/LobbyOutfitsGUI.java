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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LobbyOutfitsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public LobbyOutfitsGUI(Core main) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Lobby Outfits")
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        // Button Items
        ItemStack lockedCosmetic = ItemHelper.createDye(DyeColor.GRAY, 1, ChatColor.GRAY + "&&&&&&&");

        ItemStack hats = ItemHelper.create(Material.IRON_HELMET, ChatColor.YELLOW + "Hats");

        ItemStack suits = ItemHelper.create(Material.DIAMOND_LEGGINGS, ChatColor.YELLOW + "Suits");

        // Setting Items
        contents.set(1, 2, ClickableItem.of(
                hats,
                e -> {
                    inv.close(player);
                    new HatsGUI(main).inv.open(player);
                }));

        contents.set(1, 6, ClickableItem.of(
                suits,
                e -> {
                    inv.close(player);
                    new SuitsGUI(main).inv.open(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
