package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.playerdata.DatabaseManager;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.playerdata.PlayerDataManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FishingRewardsGUI implements InventoryProvider {
    
    public Core main;
    public SmartInventory inv;
    
    public FishingRewardsGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Fishing Rewards").parent(parent).build();
        this.main = main;
        
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        int level = data.rewardLevel;
        int nextReward = 25 + 50*level;
        
        int next = 2;
    
        String[] rewards = {"&e100 Tokens", "&d150 EXP", "&aFish Death Effect", "&e400 Tokens", "&6Pirate Outfit"};
    
        List<String> rewardStrings = new ArrayList<>();
        rewardStrings.add(main.color("&7Claim these as many times as you'd like"));
        rewardStrings.add(main.color("&eNext reward:"));
        rewardStrings.add(main.progressBar(data.caught, next, 25));
        if (data.caught >= next) {
            rewardStrings.add(main.color("&aLeft Click to claim 25 Tokens"));
            rewardStrings.add(main.color("&aRight Click to claim 50 EXP"));
        } else {
            rewardStrings.add(main.color("&e25 Tokens or 50 EXP"));
        }
        rewardStrings.add("");
        
        if (data != null) {
            if (level < 5) {
                contents.set(1, 1, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR), main.color("&aMilestone"),
                                main.color("&7These rewards can only be claimed once"),
                                main.color("&eNext reward:"),
                                main.progressBar(data.totalcaught, nextReward, 25),
                                main.color(rewards[level]),
                                main.color(data.totalcaught >= nextReward ? "&aClick to claim" : "")), e -> {
                            if (data.totalcaught >= nextReward) {
                                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have earned " + rewards[level] + "Tokens!");
                                data.rewardLevel++;
                                switch (level) {
                                    case 0:
                                        data.tokens += 100;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + 100 + " Tokens!");
                                        break;
                                    case 1:
                                        data.exp += 150;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have gained " + ChatColor.GREEN + 150 + " EXP!");
                                        if (data.exp >= 2500) {
                                            data.level++;
                                            data.exp -= 2500;
                                            player.sendMessage("Level upgraded to " + data.level + "!");
                                        }
                                        break;
                                    case 2:
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + " Fish Death Effect!");
                                        break;
                                    case 3:
                                        data.tokens += 400;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + 400 + " Tokens!");
                                        break;
                                    case 4:
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + " Fisherman Outfit!");
                                        break;
                                }
                                if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                    main.LobbyBoard(player);
                                main.getDataManager().saveData(data);
                                new FishingRewardsGUI(main, inv.getParent().get()).inv.open(player);
                            } else {
                                player.sendMessage(main.color("&c&l(!) &rGo fish some more!"));
                            }
                        }));
            } else {
                contents.set(1, 1, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.MINECART),
                                main.color("&aMilestone"),
                                main.color("&7These rewards can only be claimed once"),
                                main.color("&eAll rewards claimed!")), e -> {
                        }));
            }
    
            contents.set(1, 4, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.DIAMOND), main.color("&aSell"),
                            rewardStrings), e -> {
                        if (data.caught >= next && (e.isLeftClick() || e.isRightClick())) {
                            data.caught -= next;
                            new FishingRewardsGUI(main, inv.getParent().get()).inv.open(player);
                            if (e.isLeftClick()) {
                                data.tokens += 25;
                                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have earned " + ChatColor.GREEN + 25 + " Tokens!");
                            } else if (e.isRightClick()){
                                data.exp += 50;
                                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have gained " + ChatColor.GREEN + 50 + " EXP!");
                                if (data.exp >= 2500) {
                                    data.level++;
                                    data.exp -= 2500;
                                    player.sendMessage("Level upgraded to " + data.level + "!");
                                }
                            }
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                main.LobbyBoard(player);
                            main.getDataManager().saveData(data);
                        } else {
                            player.sendMessage(main.color("&c&l(!) &rGo fish some more!"));
                        }
                    }));
        }
        int totalFished = 0, length = FishType.values().length;
        for (FishType type : FishType.values()) {
            FishingDetails details = data.playerFishing.get(type.getId());
            if (details != null) {
                totalFished++;
            }
        }
        int fished = totalFished;
        
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY1ZDM4MTlhNjVkYjc5YzQ1ZmQwMDE0MWMwODgyZTQ3YWQyMzRjMGU1Zjg5OTJiZjRhZjE4Y2VkMGUxZWNkYyJ9fX0=";
        contents.set(1, 7, ClickableItem.of(
                ItemHelper.setDetails(ItemHelper.createSkullTexture(texture), totalFished == length?
                        main.color("&6Fisherman Class"):main.color("&7???"),
                        totalFished == length ?
                        main.color("&a&lUNLOCKED"):main.progressBar(totalFished, length, length)), e -> {
                    if (fished < length) {
                        player.sendMessage(main.color("&c&l(!) &rGo fish some more!"));
                    }
                }));
        
        contents.set(2, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                inv.getParent().get().open(player);
                }));
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
    
}
