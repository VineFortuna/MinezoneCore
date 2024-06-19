package anthony.SuperCraftBrawl.gui;

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

public class FishingUpgradesGUI implements InventoryProvider {
    
    public Core main;
    public SmartInventory inv;
    
    public FishingUpgradesGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Fishing Upgrades").parent(parent).build();
        this.main = main;
        
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        int level = data.lureLevel;
        int lure = data.lure;
    
        contents.set(1, 3, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK), main.color("&eLure I"),
                        main.color("&7Increases catching frequency")), e -> {
                }));
        contents.set(1, 4, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK), main.color("&eLure II"),
                        main.color("&7Increases catching frequency")), e -> {
                }));
        contents.set(1, 5, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK), main.color("&eLure III"),
                        main.color("&7Increases catching frequency")), e -> {
                }));
        if (data != null) {
            if (level > 0) {
                contents.set(2, 3, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIME.getData()),
                                main.color("&aPurchased")), e -> {
                        }));
                if (lure == 0) {
                    contents.set(1, 2, ClickableItem.of(
                            ItemHelper.setDetails(ItemHelper.createDye(DyeColor.GRAY, 1), main.color("&cDisabled"),
                                    main.color("&eClick to enable")), e -> {
                                data.lure = 1;
                                main.getDataManager().saveData(data);
                                new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                            }));
                } else if (lure == 1) {
                    contents.set(1, 2, ClickableItem.of(
                            ItemHelper.setDetails(ItemHelper.createDye(DyeColor.LIME, 1), main.color("&aEnabled"),
                                    main.color("&eClick to disable")), e -> {
                                data.lure = 0;
                                main.getDataManager().saveData(data);
                                new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                            }));
                }
            } else {
                contents.set(2, 3, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                                main.color("&e500 Tokens"),
                                main.color("&aClick to purchase")), e -> {
                            if (data.tokens >= 500) {
                                data.tokens -= 500;
                                data.lureLevel++;
                                player.sendMessage(main.color("&2&l(!) &rPurchased &aLure I"));
                                main.getDataManager().saveData(data);
                                if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                    main.LobbyBoard(player);
                                new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                            } else {
                                player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                        + ChatColor.RESET + "You don't have enough tokens to purchase this");
                            }
                        }));
                contents.set(2, 4, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()),
                                main.color("&cPurchase Lure I first")), e -> {
                        }));
                contents.set(2, 5, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()),
                                main.color("&cPurchase Lure I first")), e -> {
                        }));
            }
            if (level > 1) {
                contents.set(2, 4, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIME.getData()),
                                main.color("&aPurchased")), e -> {
                        }));
            } else {
                contents.set(2, 4, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                                main.color("&e1000 Tokens"),
                                main.color("&aClick to purchase")), e -> {
                            if (data.tokens >= 1000) {
                                data.tokens -= 1000;
                                data.lureLevel++;
                                player.sendMessage(main.color("&2&l(!) &rPurchased &aLure II"));
                                main.getDataManager().saveData(data);
                                if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                    main.LobbyBoard(player);
                                new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                            } else {
                                player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                        + ChatColor.RESET + "You don't have enough tokens to purchase this");
                            }
                        }));
            }
            if (level > 2) {
                contents.set(2, 5, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIME.getData()),
                                main.color("&aPurchased")), e -> {
                        }));
            } else {
                contents.set(2, 5, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                                main.color("&e1500 Tokens"),
                                main.color("&aClick to purchase")), e -> {
                            if (data.tokens >= 1500) {
                                data.tokens -= 1500;
                                data.lureLevel++;
                                player.sendMessage(main.color("&2&l(!) &rPurchased &aLure III"));
                                main.getDataManager().saveData(data);
                                if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                    main.LobbyBoard(player);
                                new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                            } else {
                                player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                        + ChatColor.RESET + "You don't have enough tokens to purchase this");
                            }
                        }));
            }
        }
        
        contents.set(2, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }));
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
    
}
