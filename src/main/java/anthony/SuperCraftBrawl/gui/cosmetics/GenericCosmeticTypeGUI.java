package anthony.SuperCraftBrawl.gui.cosmetics;


import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticManager;
import anthony.SuperCraftBrawl.cosmetics.types.KillEffect;
import anthony.SuperCraftBrawl.cosmetics.types.WinEffect;
import anthony.SuperCraftBrawl.gui.ConfirmationGUI;
import anthony.SuperCraftBrawl.gui.CosmeticsGUI;
import anthony.SuperCraftBrawl.gui.WinEffectsGUI;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GenericCosmeticTypeGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;
    public int totalRows;
    public int totalColumns;
    private final CosmeticManager cosmeticManager = new CosmeticManager();
    public Class<? extends Cosmetic> cosmeticType;

    public GenericCosmeticTypeGUI(Core main, Class<? extends Cosmetic> cosmeticType, String title, int totalRows, int totalColumns) {
        this.cosmeticType = cosmeticType;
        this.totalRows = totalRows;
        this.totalColumns = totalColumns;

        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(totalRows, totalColumns)
                .title(ChatColorHelper.color("&8&l" + title))
                .build();
        this.main = main;
    }

    /**
     * Filter all cosmetics of the cosmeticType parameter
     * Sort those cosmetics by 1.Rarity and 2.Name
     * Set those cosmetics in GUI
     * Add go back button to GUI
     *
     */
    @Override
    public void init(Player player, InventoryContents contents) {
        int row = 0;
        int column = 0;

        List<Cosmetic> cosmeticsOfType = cosmeticManager.getCosmeticsByType(cosmeticType);

        // Sorting cosmetics
        cosmeticsOfType.sort(
                Comparator.comparing(Cosmetic::getRarity)
                        .thenComparing(Cosmetic::getName));

        for (Cosmetic cosmetic : cosmeticsOfType) {
            contents.set(row, column, ClickableItem.of(ItemHelper.setDetails(
                    cosmetic.getDisplayItem(),
                    cosmetic.getRarity().getColor() + cosmetic.getName(),
                    "&8" + cosmetic.getCosmeticTypeString(cosmeticType), // Cosmetic Type
                    "",
                    "&7" + cosmetic.getDescription(), // Description
                    "",
                    "&7Rarity: " + cosmetic.getRarity().getColor() + cosmetic.getRarity().getName(), // Rarity
                    isCosmeticOwned(player, cosmetic) ? "&7Owned" : "&7Cost: " + cosmetic.getCurrency().getColor().toString() + cosmetic.getCost() + " " + cosmetic.getCurrency().getName(cosmetic.getCost()), // Cost
                    "",
                    isCosmeticOwned(player, cosmetic) ? "&eClick to select" : canPurchaseCosmetic(player, cosmetic) ? "&aClick to purchase" : "&cClick to purchase"
                    ),
                    event -> {
                        // Left Click cosmetic
                        if (event.isLeftClick()) {
                            // Cosmetic is owned
                            if (isCosmeticOwned(player, cosmetic)) {
                                PlayerData playerData = main.getPlayerDataManager().getPlayerData(player);
                                // Equip cosmetic
                                playerData.equipCosmetic(cosmeticType, cosmetic);
                                // Playing feedback sound
                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
                                // Send feedback message
                                player.sendMessage(ChatColorHelper.color("You have equipped " + cosmetic.getName() + " " + cosmeticType.getTypeName()));
                            }
                            // Cosmetic is not owned
                            else {
                                // Can purchase cosmetic
                                if (canPurchaseCosmetic(player, cosmetic)) {
                                    // Close inventory
                                    inv.close(player);
                                    // Open confirmation GUI
                                    new ConfirmationGUI(
                                            this.main,
                                            "&ePurchase " + cosmetic.getName(),
                                            // Purchase confirmed
                                            player1 -> confirmPurchase(player, cosmetic),
                                            // Purchase canceled
                                            player1 -> cancelPurchase(player, cosmeticType)
                                    ).inv.open(player);
                                }
                                // Can not purchase cosmetic (not enough currency)
                                else {
                                    // Playing sound
                                    player.playSound(player.getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 1);
                                }
                            }
                        }
                        // Right Click cosmetic
                        if (event.getClick().isRightClick()) {

                        }
                    }
            ));

            // Incrementing columns and rows
            column++;
            if (column > 8) {
                row++;
                column = 0;
            }
        }

        // Setting "Go Back" Button
        contents.set(totalRows - 1, totalColumns - 1, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.BARRIER),
                "&7Go back"), e -> {

            new CosmeticsGUI(main).inv.open(player);
        }));
    }

    private boolean isCosmeticOwned(Player player, Cosmetic cosmetic) {
        PlayerData playerData = main.getPlayerDataManager().getPlayerData(player);

        List<Cosmetic> ownedCosmetics = playerData.getOwnedCosmetics();

        return ownedCosmetics.contains(cosmetic);
    }

    private boolean canPurchaseCosmetic(Player player, Cosmetic cosmetic) {
        PlayerData playerData = main.playerDataManager.getPlayerData(player);

        return playerData.getCurrencyAmount(cosmetic.getCurrency()) >= cosmetic.getCost();
    }

    private void confirmPurchase(Player player, Cosmetic cosmetic) {
        // Adding cosmetic to owned cosmetics
        PlayerData playerData = main.playerDataManager.getPlayerData(player);
        playerData.addOwnedCosmetic(cosmetic);

        // Closing inventory
        inv.close(player);
        // Playing sound
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

        player.sendMessage("Owned cosmetics");
        for (Cosmetic cosmetic1 : playerData.getOwnedCosmetics()) {
            player.sendMessage(cosmetic1.getName());
        }


        if (playerData.getOwnedCosmetics().contains(cosmetic)) {
            player.sendMessage("------------------------");
            player.sendMessage("You own " + cosmetic.getName() + " cosmetic");
            player.sendMessage("------------------------");
        } else {
            player.sendMessage("------------------------");
            player.sendMessage("You dont own " + cosmetic.getName() + " cosmetic");
            player.sendMessage("------------------------");
        }
    }

    private void cancelPurchase(Player player, Class<? extends Cosmetic> cosmeticType) {
        // Close Inventory
        inv.close(player);

        // Check cosmeticType to open cosmeticTypeGUI
        if (cosmeticType.equals(KillEffect.class)) {
            new KillEffectsGUI(main).inv.open(player);
        } else if (cosmeticType.equals(WinEffect.class)) {
            new WinEffectsGUI(main).inv.open(player);
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }
}
