package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
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

import java.util.ArrayList;

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
    
        String[] rewards = {"&e100 Tokens", "&d150 EXP", "&e250 Tokens", "&aFisherman Outfit", "&6???"};
        
        if (data != null) {
            if (level < 5) {
                contents.set(1, 1, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR), main.color("&aMilestone Reward"),
                                main.color("&eNext reward:"),
                                main.progressBar(data.totalcaught, nextReward, 25),
                                main.color(rewards[level]),
                                main.color(data.totalcaught >= nextReward ? "&aClick to claim" : "")), e -> {
                            if (data.totalcaught >= nextReward) {
                                String reward = main.color(rewards[level]);
                                if (level == 4) {
                                    reward = main.color("Fisherman Class");
                                }
                                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have earned " + reward + "Tokens!");
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
                                        data.tokens += 250;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + 250 + " Tokens!");
                                        break;
                                    case 3:
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + " Fisherman Outfit!");
                                        break;
                                    case 4:
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + " Fisherman Class!");
                                        break;
                                }
                                if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                    main.LobbyBoard(player);
                                main.getDataManager().saveData(data);
                                new FishingRewardsGUI(main, inv.getParent().get()).inv.open(player);
                            }
                        }));
            } else {
                contents.set(1, 1, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.MINECART),
                                main.color("&aMilestone Reward"),
                                main.color("&eAll rewards claimed!")), e -> {
                        }));
            }
    
            contents.set(1, 4, ClickableItem.of(
                    ItemHelper.setDetails(new ItemStack(Material.DIAMOND), main.color("&aReward"),
                            main.color("&eNext reward:"),
                            main.progressBar(data.caught, next, 25),
                            main.color(data.caught >= next?"&aLeft Click to claim 25 Tokens\n" +
                                    "&aRight Click to claim 50 EXP":"&725 Tokens or 50 EXP")), e -> {
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
                        }
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
    
    
}
