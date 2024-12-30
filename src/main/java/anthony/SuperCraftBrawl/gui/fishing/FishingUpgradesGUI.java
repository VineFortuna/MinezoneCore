package anthony.SuperCraftBrawl.gui.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
    
        String lureTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ1OWFmNDEzNWMxZWI3OTMyN2ExM2M3YTU5ZjFmOGE5ZWExZTE0NjViNDgwYWQ5YmU3MTQxOGI2ZjkwZGM4ZiJ9fX0=";
        String friendTexture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZjYmFlNzI0NmNjMmM2ZTg4ODU4NzE5OGM3OTU5OTc5NjY2YjRmNWE0MDg4ZjI0ZTI2ZTA3NWYxNDBhZTZjMyJ9fX0=";
        
        contents.set(1, 3, ClickableItem.of(
                ItemHelper.createSkullTexture(lureTexture, main.color("&eLure"),
                        main.color("&7Increases catching frequency")), e -> {
                }));
        
        contents.set(1, 7, ClickableItem.of(
                ItemHelper.createSkullTexture(friendTexture, main.color("&eCrew's Bounty"),
                        main.color("&7Gain bonuses for fishing with other players")), e -> {
                }));
    
        if (data.lureLevel > 0) {
            if (data.lure == 0) {
                contents.set(1, 1, ClickableItem.of(
                        ItemHelper.setDetails(ItemHelper.createDye(DyeColor.GRAY, 1), main.color("&cDisabled"),
                                main.color("&eClick to enable")), e -> {
                            data.lure = 1;
                            main.getDataManager().saveData(data);
                            addLure(player, data.lureLevel);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        }));
            } else if (data.lure == 1) {
                contents.set(1, 1, ClickableItem.of(
                        ItemHelper.setDetails(ItemHelper.createDye(DyeColor.LIME, 1), main.color("&aEnabled"),
                                main.color("&eClick to disable")), e -> {
                            data.lure = 0;
                            main.getDataManager().saveData(data);
                            disableLure(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        }));
            }
        }
        if (data.lureLevel == 0) {
            contents.set(1, 2, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                            main.color("&eLure I"), main.color("&7Enchant your rod with Lure I"),
                            "",
                            main.tokenCostString(player, 500),
                            main.color("&aClick to purchase")), e -> {
                        if (data.tokens >= 500) {
                            data.tokens -= 500;
                            data.lure = 1;
                            data.lureLevel++;
                            player.sendMessage(main.color("&2&l(!) &rPurchased &aLure I"));
                            main.getDataManager().saveData(data);
                            addLure(player, data.lureLevel);
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        } else {
                            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + ChatColor.RESET + "You don't have enough tokens to purchase this");
                        }
                    }));
        } else if (data.lureLevel == 1) {
            contents.set(1, 2, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                            main.color("&eLure II"), main.color("&7Enchant your rod with Lure II"),
                            "",
                            main.tokenCostString(player, 1000),
                            main.color("&aClick to purchase")), e -> {
                        if (data.tokens >= 1000) {
                            data.tokens -= 1000;
                            data.lure = 1;
                            data.lureLevel++;
                            player.sendMessage(main.color("&2&l(!) &rPurchased &aLure II"));
                            main.getDataManager().saveData(data);
                            addLure(player, data.lureLevel);
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        } else {
                            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + ChatColor.RESET + "You don't have enough tokens to purchase this");
                        }
                    }));
        } else if (data.lureLevel == 2) {
            contents.set(1, 2, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                            main.color("&eLure III"), main.color("&7Enchant your rod with Lure III"),
                            "",
                            main.tokenCostString(player, 1500),
                            main.color("&aClick to purchase")), e -> {
                        if (data.tokens >= 1500) {
                            data.tokens -= 1500;
                            data.lure = 1;
                            data.lureLevel++;
                            player.sendMessage(main.color("&2&l(!) &rPurchased &aLure III"));
                            main.getDataManager().saveData(data);
                            addLure(player, data.lureLevel);
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        } else {
                            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + ChatColor.RESET + "You don't have enough tokens to purchase this");
                        }
                    }));
        } else {
            contents.set(1, 2, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIME.getData()),
                            main.color("&eLure III"), main.color("&7Enchant your rod with Lure III"), "",
                            main.color("&aPurchased")), e -> {
                    }));
        }
        
        // FRIENDSHIP
        if (data.friendshipLevel > 0) {
            if (data.friendship == 0) {
                contents.set(1, 5, ClickableItem.of(
                        ItemHelper.setDetails(ItemHelper.createDye(DyeColor.GRAY, 1), main.color("&cDisabled"),
                                main.color("&eClick to enable")), e -> {
                            data.friendship = 1;
                            main.getDataManager().saveData(data);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        }));
            } else if (data.friendship == 1) {
                contents.set(1, 5, ClickableItem.of(
                        ItemHelper.setDetails(ItemHelper.createDye(DyeColor.LIME, 1), main.color("&aEnabled"),
                                main.color("&eClick to disable")), e -> {
                            data.friendship = 0;
                            main.getDataManager().saveData(data);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        }));
            }
        }
        if (data.friendshipLevel == 0) {
            contents.set(1, 6, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                            main.color("&eCrew's Bounty I"),
                            main.color("&7Receive 10 EXP for every 5 fish caught "),
                            main.color("&7within 4 blocks of another player"),
                            "",
                            main.tokenCostString(player, 200),
                            main.color("&aClick to purchase")), e -> {
                        if (data.tokens >= 200) {
                            data.tokens -= 200;
                            data.friendship = 1;
                            data.friendshipLevel++;
                            player.sendMessage(main.color("&2&l(!) &rPurchased &aCrew's Bounty I"));
                            main.getDataManager().saveData(data);
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        } else {
                            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + ChatColor.RESET + "You don't have enough tokens to purchase this");
                        }
                    }));
        } else if (data.friendshipLevel == 1) {
            contents.set(1, 6, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                            main.color("&eCrew's Bounty II"),
                            main.color("&7Receive 10 EXP for every 5 fish caught "),
                            main.color("&7within 6 blocks of another player"),
                            "",
                            main.tokenCostString(player, 400),
                            main.color("&aClick to purchase")), e -> {
                        if (data.tokens >= 400) {
                            data.tokens -= 400;
                            data.friendship= 1;
                            data.friendshipLevel++;
                            player.sendMessage(main.color("&2&l(!) &rPurchased &aCrew's Bounty II"));
                            main.getDataManager().saveData(data);
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        } else {
                            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + ChatColor.RESET + "You don't have enough tokens to purchase this");
                        }
                    }));
        } else if (data.friendshipLevel == 2) {
            contents.set(1, 6, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                            main.color("&eCrew's Bounty III"),
                            main.color("&7Receive 10 EXP for every 3 fish caught "),
                            main.color("&7within 6 blocks of another player"),
                            "",
                            main.tokenCostString(player, 600),
                            main.color("&aClick to purchase")), e -> {
                        if (data.tokens >= 600) {
                            data.tokens -= 600;
                            data.friendship = 1;
                            data.friendshipLevel++;
                            player.sendMessage(main.color("&2&l(!) &rPurchased &aCrew's Bounty III"));
                            main.getDataManager().saveData(data);
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        } else {
                            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + ChatColor.RESET + "You don't have enough tokens to purchase this");
                        }
                    }));
        } else if (data.friendshipLevel == 3) {
            contents.set(1, 6, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.YELLOW.getData()),
                            main.color("&eCrew's Bounty IV"),
                            main.color("&7Receive 15 EXP for every 3 fish caught "),
                            main.color("&7within 6 blocks of another player"),
                            "",
                            main.tokenCostString(player, 800),
                            main.color("&aClick to purchase")), e -> {
                        if (data.tokens >= 800) {
                            data.tokens -= 800;
                            data.friendship = 1;
                            data.friendshipLevel++;
                            player.sendMessage(main.color("&2&l(!) &rPurchased &aCrew's Bounty IV"));
                            main.getDataManager().saveData(data);
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            new FishingUpgradesGUI(main, inv.getParent().get()).inv.open(player);
                        } else {
                            player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
                                    + ChatColor.RESET + "You don't have enough tokens to purchase this");
                        }
                    }));
        } else {
            contents.set(1, 6, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.LIME.getData()),
                            main.color("&eCrew's Bounty IV"),
                            main.color("&7Receive 15 EXP for every 3 fish caught "),
                            main.color("&7within 6 blocks of another player"),
                            "", main.color("&aPurchased")), e -> {
                    }));
        }
        contents.set(2, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }));
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
    public void addLure(Player player, int level) {
        if (player.getInventory().getItem(5) != null &&
                player.getInventory().getItem(5).getType() == Material.FISHING_ROD) {
            ItemStack item = player.getInventory().getItem(5);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.LURE, level, true);
            item.setItemMeta(meta);
        }
    }
    public void disableLure(Player player) {
        if (player.getInventory().getItem(5) != null &&
                player.getInventory().getItem(5).getType() == Material.FISHING_ROD) {
            ItemStack item = player.getInventory().getItem(5);
            ItemMeta meta = item.getItemMeta();
            meta.removeEnchant(Enchantment.LURE);
            item.setItemMeta(meta);
        }
    }
}
