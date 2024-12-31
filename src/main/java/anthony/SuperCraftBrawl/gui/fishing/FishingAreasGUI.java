package anthony.SuperCraftBrawl.gui.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.fishing.FishArea;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FishingAreasGUI implements InventoryProvider {
    
    public Core main;
    public SmartInventory inv;
    
    public FishingAreasGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Fishing Warps").parent(parent).build();
        this.main = main;
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);
    
        if (data != null) {
            for (FishArea area : FishArea.values()) {
                int id = area.getID();
                if (data.getFishingWarps().contains(id)) {
                    contents.add(
                            ClickableItem.of(
                                    ItemHelper
                                            .setDetails(area.getDisplayItem()
                                                    , "&e&l" + area.getName(),
                                                    "&7Click to teleport"),
                                    e -> {
                                        if (main.getGameManager().GetInstanceOfPlayer(player) == null &&
                                                main.getGameManager().GetInstanceOfSpectator(player) == null) {
                                            player.teleport(area.getSpawnPoint(main.getLobbyWorld()));
                                            player.sendMessage(main.color("&3&l(!) &rGrab a rod and go fishing!"));

                                            if (!(player.getInventory().contains(main.getFishingRod(player)))) {
                                                player.getInventory().setItem(5, main.getFishingRod(player));
                                                player.sendMessage("" + ChatColor.BLUE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                        + "You have equipped " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Fishing Rod");
                                                inv.close(player);
                                            }
                                        } else {
                                            player.sendMessage(main.color("&c&l(!) &rYou cannot do this while in a game!"));
                                        }
                                    }));
                } else {
                    contents.add(
                            ClickableItem.of(
                                    ItemHelper
                                            .setDetails(ItemHelper.createDye(DyeColor.GRAY, 1)
                                                    , "&c???",
                                                    "&7Visit location to unlock quick travel"),
                                    e -> {
                                    }));
                }
            }
        }
        
        if (inv.getParent().isPresent()) {
            contents.set(0, 8, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                        inv.getParent().get().open(player);
                    }));
        }
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
}
