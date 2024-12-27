package anthony.SuperCraftBrawl.gui.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.fishing.FishType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
        int nextReward = 10 * (int) Math.pow(2, data.rewardLevel);
        
        int next = 25;
    
        String[] milestonerewards = {"&e100 Tokens", "&dMysteryChest", "&d150 EXP", "&cFish Rain Win Effect", "&e250 Tokens",
                "&3Pirate Outfit", "&d300 EXP"};
    
        List<String> rewardStrings = new ArrayList<>();
        rewardStrings.add(main.color("&7Claim these as many times as you'd like"));
        rewardStrings.add(main.color("&eNext reward:"));
        rewardStrings.add(main.progressBar(data.caught, next, 25));
        if (data.caught >= next) {
            rewardStrings.add(main.color("&aLeft Click to claim 15 Tokens"));
            rewardStrings.add(main.color("&aRight Click to claim 30 EXP"));
        } else {
            rewardStrings.add(main.color("&e15 Tokens or 30 EXP"));
        }
        rewardStrings.add("");
        
        if (data != null) {
            if (data.rewardLevel < 7) {
                contents.set(1, 1, ClickableItem.of(
                        ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR), main.color("&aMilestone"),
                                main.color("&7These rewards can only be claimed once"),
                                main.color("&eNext reward:"),
                                main.progressBar(data.totalcaught, nextReward, 25),
                                main.color(milestonerewards[data.rewardLevel]),
                                main.color(data.totalcaught >= nextReward ? "&aClick to claim" : "")), e -> {
                            if (data.totalcaught >= nextReward) {
                                data.rewardLevel++;
                                switch (data.rewardLevel) {
                                    case 1:
                                        data.tokens += 100;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + 100 + " Tokens!");
                                        break;
                                    case 2:
                                        data.mysteryChests++;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have found " + ChatColor.GREEN + 1 + " MysteryChest!");
                                        break;
                                    case 3:
                                        data.exp += 150;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have gained " + ChatColor.GREEN + 150 + " EXP!");
                                        if (data.exp >= 2500) {
                                            data.level++;
                                            data.exp -= 2500;
                                            player.sendMessage("Level upgraded to " + data.level + "!");
                                        }
                                        break;
                                    case 4:
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + " Fish Rain Win Effect!");
                                        break;
                                    case 5:
                                        data.tokens += 250;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + 250 + " Tokens!");
                                        break;
                                    case 6:
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have earned " + ChatColor.GREEN + " Pirate Outfit!");
                                        break;
                                    case 7:
                                        data.exp += 300;
                                        player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                                + "You have gained " + ChatColor.GREEN + 300 + " EXP!");
                                        if (data.exp >= 2500) {
                                            data.level++;
                                            data.exp -= 2500;
                                            player.sendMessage("Level upgraded to " + data.level + "!");
                                        }
                                        break;
                                }
                                if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                	main.getScoreboardManager().lobbyBoard(player);
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
                                data.tokens += 15;
                                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have earned " + ChatColor.GREEN + 15 + " Tokens!");
                            } else if (e.isRightClick()){
                                data.exp += 30;
                                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                                        + "You have gained " + ChatColor.GREEN + 30 + " EXP!");
                                if (data.exp >= 2500) {
                                    data.level++;
                                    data.exp -= 2500;
                                    player.sendMessage("Level upgraded to " + data.level + "!");
                                }
                            }
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                            	main.getScoreboardManager().lobbyBoard(player);
                            main.getDataManager().saveData(data);
                        } else {
                            player.sendMessage(main.color("&c&l(!) &rGo fish some more!"));
                        }
                    }));
        }
        int length = FishType.values().length;
        int fished = main.getTotalFish(player);
        
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY1ZDM4MTlhNjVkYjc5YzQ1ZmQwMDE0MWMwODgyZTQ3YWQyMzRjMGU1Zjg5OTJiZjRhZjE4Y2VkMGUxZWNkYyJ9fX0=";
        contents.set(1, 7, ClickableItem.of(
                ItemHelper.setDetails(ItemHelper.createSkullTexture(texture), fished == length?
                        main.color("&3Fisherman Class"):main.color("&7???"),
                        fished == length ?
                        main.color("&a&lUNLOCKED"):main.progressBar(fished, length, length)), e -> {
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
