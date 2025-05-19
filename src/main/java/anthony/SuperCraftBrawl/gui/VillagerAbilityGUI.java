package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.all.VillagerClass;
import anthony.util.ChatColorHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VillagerAbilityGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;
    public int totalRows = 1;
    public int totalColumns = 9;

    private GameInstance gameInstance;
    private VillagerClass villagerClass;
    private int emeraldsCount;
    private Map<ItemStack, Integer> tradeableItems;

    public VillagerAbilityGUI(Core main, GameInstance gameInstance, VillagerClass villagerClass) {
        this.main = main;
        this.gameInstance = gameInstance;
        this.villagerClass = villagerClass;
        this.emeraldsCount = villagerClass.getEmeraldsCount();

        buildInventory(emeraldsCount);
        setUpTradeableItems(gameInstance);
        sortTradeableItems();
    }

    private void buildInventory(int emeralds) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this).size(totalRows, totalColumns)
                .title(ChatColorHelper.color("&8&lTrades - &2&l" + emeralds + "&8&l emeralds"))
                .build()
        ;
    }

    private void setUpTradeableItems(GameInstance gameInstance) {
        tradeableItems = new HashMap<>();

        for (ItemStack item : gameInstance.allItemDrops) {
            Integer price = getPriceForItem(item);
            if (price != null) {
                    tradeableItems.put(item, price);
            }
        }
    }

    private void sortTradeableItems() {
        // Sorting tradeableItems by price (the value of the map)
        Map<ItemStack, Integer> sortedTradeableItems = tradeableItems.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())  // Sort by value (price) in ascending order
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,  // In case of a key conflict, keep the old value
                        LinkedHashMap::new                 // Collect into a LinkedHashMap to maintain order
                ))
        ;

        // Now tradeableItems is sorted by price
        tradeableItems = sortedTradeableItems;
    }

    private Integer getPriceForItem(ItemStack item) {
        Material type = item.getType() ;

        if (type == Material.MILK_BUCKET) {
            return 2;
        } else if (type == Material.SNOW_BALL) {
            return 3;
        } else if (type == Material.TNT) {
            return 4;
        } else if (item.getItemMeta().getDisplayName().toLowerCase().contains("speed")) {
            return 5;
        } else if (type == Material.ENDER_PEARL) {
            return 5;
        } else if (type == Material.GOLD_HOE) {
            return 6;
        } else if (type == Material.WHEAT) {
            return 8;
        } else if (type == Material.GOLDEN_APPLE && item.getDurability() == 0) {
            return 10;
        } else if (type == Material.IRON_SWORD) {
            return 14;
        } else return null;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        int index = 0;

        // Iterating over tradeableItems to place them into the gui
        for (Map.Entry<ItemStack, Integer> entry : tradeableItems.entrySet()) {

            if (index >= totalRows * totalColumns) break;
            ItemStack originalItem = entry.getKey();
            int price = entry.getValue();

            // Clone the item for GUI and set the price as the amount
            ItemStack guiItem = originalItem.clone();
            guiItem.setAmount(price);

            // Calculate row and column based on index
            int row = index / totalColumns;
            int column = index % totalColumns;

            // Set the item in the GUI
            contents.set(row, column, ClickableItem.of(
                    guiItem,
                    event -> {
                        if (event.isLeftClick() || event.isRightClick()) {
                            // If item was Left-Clicked buy item single time
                            // If item was Right-Clicked buy item until you're out of emeralds
                            trade(player, originalItem, price);
                        }
                    }
            ));
            index++;
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {}

    public void trade(Player player, ItemStack originalItem, int price) {
        // If not enough emeralds
        if (emeraldsCount < price) {
            // Playing unsuccessful Sound
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
            inv.close(player);
        } else {
            // Add traded item to the player's inventory
            ItemStack tradedItem = originalItem.clone();
            player.getInventory().addItem(tradedItem);
            // Subtract traded emeralds
            emeraldsCount -= price;
            // Update the VillagerClass emeraldCount and the player weapon
            villagerClass.setEmeraldsCount(emeraldsCount);
            // Ensure the weapon always has at least 1 item
            ItemStack weapon = villagerClass.getAttackWeapon();
            weapon.setAmount(Math.max(emeraldsCount, 1));  // Set amount to 1 if emeraldsCount is 0
            player.getInventory().setItem(0, weapon);
            // Playing successful Sound to player
            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
            // Playing villager Sound to all players
            SoundManager.playSoundToAll(player, Sound.VILLAGER_HAGGLE, 1, 1);
            // Spawn green particles
            spawnTradeParticles(player);
            updateTitle(player);
        }
    }

    private void spawnTradeParticles(Player player) {
        Location playerLocation = player.getLocation();
        int particleCount = 100; // Adjust based on how dense you want the particles
        double radius = 1.0; // Adjust the radius around the player

        for (int i = 0; i < particleCount; i++) {
            double angle = Math.random() * Math.PI * 2; // Random angle for circle
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location effectLocation = playerLocation.clone().add(x, 1, z); // 1 block above the player
            player.getWorld().playEffect(effectLocation, Effect.HAPPY_VILLAGER, 0);
        }
    }

    public void updateTitle(Player player) {
        buildInventory(emeraldsCount);
        inv.open(player);
    }
}