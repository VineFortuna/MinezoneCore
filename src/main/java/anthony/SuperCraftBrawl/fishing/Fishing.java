package anthony.SuperCraftBrawl.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("deprecation")
public class Fishing implements Listener {
    
    public Core main;
    private final Random rand = new Random();
    private final int size = FishType.values().length;
    private ArrayList<Item> fishItems = new ArrayList<>();
    private final ArrayList<FishType> fishTypes = new ArrayList<>();
    private final int common = FishRarity.COMMON.getChance();
    private final int rare = FishRarity.RARE.getChance();
    private final int epic = FishRarity.EPIC.getChance();
    private final int mythic = FishRarity.MYTHIC.getChance();
    private final int legendary = FishRarity.LEGENDARY.getChance();
    private final int junk = FishRarity.JUNK.getChance();
    private final int treasure = FishRarity.TREASURE.getChance();
    private HashMap<Player, Integer> caughtRecent = new HashMap<>();
    
    public Fishing(Core main) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, main);
        Collections.addAll(fishTypes, FishType.values());
    }
    
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) && event.getCaught() instanceof Item) {
            Item i = (Item) event.getCaught();
            event.setExpToDrop(0);
            Player p = event.getPlayer();
            PlayerData data = main.getDataManager().getPlayerData(p);
            FishType fish = getFish();
            FishingDetails details = data.playerFishing.get(fish.getId());
            
            i.getItemStack().setType(fish.getItem().getType());
            i.getItemStack().setDurability(fish.getItem().getDurability());
            i.getItemStack().setItemMeta(fish.getItem().getItemMeta());
            i.setPickupDelay(Integer.MAX_VALUE);
            fishItems.add(i);
            if (details == null) {
                details = new FishingDetails();
                data.playerFishing.put(fish.getId(), details);
            }
            p.sendMessage(main.color("&3&l(!) &rYou caught a "
                    + fish.getRarity().getColor() + fish.getName() + "&r!"));
            
            if (details.timesCaught == 0) {
                p.sendMessage(main.color("&2&l=============================================="));
                p.sendMessage(main.color("&2&l||"));
                if (fish.isFish()) {
                    p.sendMessage(main.color("&2&l|| &e&lCAUGHT " + fish.getRarity().getColor() + "&l"
                            + fish.getRarity().getName().toUpperCase() + " &e&lSEA CREATURE: " + fish.getName()));
                } else {
                    p.sendMessage(main.color("&2&l|| &e&lCAUGHT " + fish.getRarity().getColor() + "&l"
                            + fish.getRarity().getName().toUpperCase() + "&e&l: " + fish.getName()));
                }
                p.sendMessage(main.color("&2&l|| &7" + fish.getDesc()));
                p.sendMessage(main.color("&2&l||"));
                p.sendMessage(main.color("&2&l=============================================="));
                if (main.getTotalFish(p) == FishType.values().length) {
                    p.playSound(p.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1);
                    p.sendMessage(main.color("&3&l(!) &rCongratulations! You caught everything!"));
                }
            }
            if (fish == FishType.CRATE) {
                p.sendMessage(main.color("&3&l(!) &rYou have found &e1 Mystery Chest!"));
                data.mysteryChests++;
            } else if (fish == FishType.EXP) {
                int r = rand.nextInt(35) + 11;
                data.exp += r;
                p.sendMessage(main.color("&3&l(!) &rYou have gained &e" + r + " EXP!"));
                if (data.exp >= 2500) {
                    data.level++;
                    data.exp -= 2500;
                    p.sendMessage(main.color("&e&lLEVEL UPGRADED!"));
                    p.sendMessage(main.color("&r&l(!) &rYou are now Level " + data.level + "!"));
                }
            } else if (fish == FishType.TOKENS) {
                int r = rand.nextInt(25) + 11;
                data.tokens += r;
                p.sendMessage(main.color("&3&l(!) &rYou have found &e" + r + " Tokens!"));
            }
            reward(p, fish.getRarity());
            data.totalcaught++;
            data.caught++;
            details.addCaught(1);
            main.getDataManager().saveData(data);
            
            if (caughtRecent.containsKey(p))
                caughtRecent.put(p, caughtRecent.get(p) + 1);
            else
                caughtRecent.put(p, 1);
            
            if (data.friendship == 1)
                friendship(p, data.friendshipLevel);
            
            removeFish(i);
            if (main.getGameManager().GetInstanceOfPlayer(p) == null)
            	main.getScoreboardManager().lobbyBoard(p);
        }
    }
    
    public FishType getRandomFish() {
        ArrayList<FishType> fishes = new ArrayList<>();
        FishRarity rarity;
        int r = rand.nextInt(100) + 1;
        if (r <= legendary)
            rarity = FishRarity.LEGENDARY;
        else if (r <= legendary + mythic)
            rarity = FishRarity.MYTHIC;
        else if (r <= legendary + mythic + epic)
            rarity = FishRarity.EPIC;
        else if (r <= legendary + mythic + epic + rare)
            rarity = FishRarity.RARE;
        else {
            rarity = FishRarity.COMMON;
        }
        for (FishType t : fishTypes) {
            if (t.getRarity() == rarity)
                fishes.add(t);
        }
        return fishes.get(rand.nextInt(fishes.size()));
    }
    
    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        for (Item i : fishItems) {
            i.remove();
        }
    }
    public void removeFish(Item i) {
        Bukkit.getServer().getScheduler().runTaskLater(main, new Runnable(){
            public void run() {
                fishItems.remove(i);
                i.remove();
            }
        }, 80L);
    }
    public FishType getRandomLoot(FishRarity rarity) {
        ArrayList<FishType> loot = new ArrayList<>();
        for (FishType l : fishTypes) {
            if (l.getRarity() == rarity)
                loot.add(l);
        }
        return loot.get(rand.nextInt(loot.size()));
    }
    public FishType getFish() {
        int r = rand.nextInt(100)+1;
        if (r <= treasure)
            return getRandomLoot(FishRarity.TREASURE);
        else if (r <= treasure + junk)
            return getRandomLoot(FishRarity.JUNK);
        
        return getRandomFish();
    }
    
    public void reward(Player p, FishRarity rarity) {
        switch (rarity) {
            case JUNK:
                p.playSound(p.getLocation(), Sound.ZOMBIE_PIG_HURT, 1, 0);
                break;
            case COMMON:
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 0);
                break;
            case RARE:
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 1);
                break;
            case EPIC:
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 2);
                break;
            case MYTHIC:
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 3);
                break;
            case LEGENDARY:
                p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 4);
                break;
            case TREASURE:
                p.playSound(p.getLocation(), Sound.FIREWORK_TWINKLE2, 1, 0);
                break;
            default:
                break;
                
        }
        particles(p, rarity);
    }
    
    public void friendship(Player p, int level) {
        int radius = 0, times = 0, exp = 0;
        switch (level) {
            case 1:
                radius = 4;
                times = 5;
                exp = 10;
                break;
            case 2:
                radius = 6;
                times = 5;
                exp = 10;
                break;
            case 3:
                radius = 6;
                times = 3;
                exp = 10;
                break;
            case 4:
                radius = 6;
                times = 3;
                exp = 15;
                break;
        }
        if (caughtRecent.get(p) % times == 0) {
            for (int t = 0; t < 2 * Math.PI * radius; t += 1) {
                p.playEffect(p.getLocation().add(radius * Math.cos(t), 0,
                        radius * Math.sin((t))), Effect.HAPPY_VILLAGER, 1);
            }
            boolean found = false;
            for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e instanceof Player && e != p) {
                    found = true;
                    break;
                }
            }
            if (found) {
                PlayerData data = main.getDataManager().getPlayerData(p);
                data.exp += exp;
                p.sendMessage(main.color("&3&l(!) &rYou have gained &e" + exp + " EXP!"));
                if (data.exp >= 2500) {
                    data.level++;
                    data.exp -= 2500;
                    p.sendMessage(main.color("&e&lLEVEL UPGRADED!"));
                    p.sendMessage(main.color("&r&l(!) &rYou are now Level " + data.level + "!"));
                }
            }
        }
    }
    
    public void particles(Player p, FishRarity r) {
        Color c;
        switch (r) {
            case COMMON:
                c = Color.GRAY;
                break;
            case RARE:
                c = Color.LIME;
                break;
            case EPIC:
                c = Color.PURPLE;
                break;
            case MYTHIC:
                c = Color.RED;
                break;
            case LEGENDARY:
                c = Color.ORANGE;
                break;
            case JUNK:
                c = Color.BLACK;
                break;
            case TREASURE:
                c = Color.YELLOW;
                break;
            default:
                c = Color.NAVY;
                break;
        }
    }
}