package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.fishing.FishRarity;
import anthony.SuperCraftBrawl.fishing.FishType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

public class FishingGUI implements InventoryProvider {
    
    public Core main;
    public SmartInventory inv;
    
    public FishingGUI(Core main) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(6, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Fishing").build();
        this.main = main;
        
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        
        contents.fillRow(0, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
        contents.fillRow(5, ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
        
        
        int a = 1;
        int b = 0;
    
        for (FishType type : FishType.values()) {
            ItemStack item = type.getItem();
        
            if (item == null)
                item = new ItemStack(Material.BARRIER);
            contents.set(a, b,
                    ClickableItem.of(item,
                            e -> {
                            }));
        
            b++;
        
            if (b > 8) {
                a++;
                b = 0;
            }
        }
    
        if (data != null) {
            contents.set(0, 4,
                    ClickableItem.of(ItemHelper.createSkullHeadPlayer(1, data.playerName, main.color("&e" + data.playerName),
                            Arrays.asList(main.color("&aRank: &r" + data.getRank().getTag()),
                                    main.color("&aLevel: &r" + data.level),
                                    main.color("&aFish Caught: &r" + (data.totalcaught)))), e -> {
                    }));
        }
        
        contents.set(5, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    new StatsGUI(main).inv.open(player);
                }));
        contents.set(5, 3, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.EMERALD), ChatColor.GRAY + "Rewards"), e -> {
                }));
        contents.set(5, 5, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD), ChatColor.GRAY + "Upgrades"), e -> {
                }));
        contents.set(5, 0, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.PAPER), ChatColor.GRAY + "Chance Breakdown",
                        main.color("&eTotal chance:"),
                        main.color("&e- Junk " + FishRarity.JUNK.getChance() + "%"),
                        main.color("&e- Sea Creature " + (100-(FishRarity.JUNK.getChance() +
                                FishRarity.TREASURE.getChance())) + "%"),
                        main.color("&e- Treasure " + FishRarity.TREASURE.getChance() + "%"),
                        "",
                        main.color("&eSea Creature chance:"),
                        main.color("&e- Common " + FishRarity.COMMON.getChance() + "%"),
                        main.color("&e- Rare " + FishRarity.RARE.getChance() + "%"),
                        main.color("&e- Epic " + FishRarity.EPIC.getChance() + "%"),
                        main.color("&e- Mythic " + FishRarity.MYTHIC.getChance() + "%"),
                        main.color("&e- Legendary " + FishRarity.LEGENDARY.getChance() + "%")), e -> {
                }));
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
}
