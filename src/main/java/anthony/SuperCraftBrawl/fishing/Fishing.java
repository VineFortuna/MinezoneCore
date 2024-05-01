package anthony.SuperCraftBrawl.fishing;

import anthony.SuperCraftBrawl.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

@SuppressWarnings("deprecation")
public class Fishing implements Listener {
    
    public Core main;
    private final Random rand = new Random();
    private final int size = FishType.values().length;
    private ArrayList<Item> fishItems = new ArrayList<>();
    private final ArrayList<FishType> fishTypes = new ArrayList<>();
    private FishType fish;
    private final int common = FishRarity.COMMON.getChance();
    private final int rare = FishRarity.RARE.getChance();
    private final int epic = FishRarity.EPIC.getChance();
    private final int mythic = FishRarity.MYTHIC.getChance();
    private final int legendary = FishRarity.LEGENDARY.getChance();
    private final int junk = FishRarity.JUNK.getChance();
    private final int treasure = FishRarity.TREASURE.getChance();
    
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
            getFish();
            i.getItemStack().setType(fish.getItem().getType());
            i.getItemStack().setDurability(fish.getItem().getDurability());
            i.getItemStack().setItemMeta(fish.getItem().getItemMeta());
            i.setPickupDelay(Integer.MAX_VALUE);
            fishItems.add(i);
            p.sendMessage(main.color("&r&l(&3&l!&r&l) &rYou caught a "
                    + fish.getRarity().getColor() + fish.getName() + "&r!"));
    
            p.sendMessage(main.color("&2&l=============================================="));
            p.sendMessage(main.color("&2&l||"));
            if (fish.isFish()) {
                p.sendMessage(main.color("&2&l|| &e&lCAUGHT " + fish.getRarity().getColor() + "&l"
                        + fish.getRarity().getName().toUpperCase() + " &e&lFISH: " +  fish.getName()));
            } else {
                p.sendMessage(main.color("&2&l|| &e&lCAUGHT " + fish.getRarity().getColor() + "&l"
                        + fish.getRarity().getName().toUpperCase() + "&e&l: " +  fish.getName()));
            }
            p.sendMessage(main.color("&2&l|| &7" + fish.getDesc()));
            p.sendMessage(main.color("&2&l||"));
            p.sendMessage(main.color("&2&l=============================================="));
            reward(p, fish.getRarity());
            removeFish(i);
        }
    }
    
    public void getRandomFish() {
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
        fish = fishes.get(rand.nextInt(fishes.size()));
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
        }, 60L);
    }
    public void getRandomLoot(FishRarity rarity) {
        ArrayList<FishType> loot = new ArrayList<>();
        for (FishType l : fishTypes) {
            if (l.getRarity() == rarity)
                loot.add(l);
        }
        fish = loot.get(rand.nextInt(loot.size()));
    }
    public void getFish() {
        int r = rand.nextInt(100)+1;
        if (r <= treasure)
            getRandomLoot(FishRarity.TREASURE);
        else if (r <= treasure + junk)
            getRandomLoot(FishRarity.JUNK);
        else
            getRandomFish();
    }
    
    public void reward(Player p, FishRarity rarity) {
        switch (rarity) {
            case JUNK:
                p.playSound(p.getLocation(), Sound.ZOMBIE_PIG_HURT, 1, 0);
                break;
            case COMMON:
            case RARE:
            case EPIC:
            case MYTHIC:
            case LEGENDARY:
                break;
            case TREASURE:
                p.playSound(p.getLocation(), Sound.FIREWORK_TWINKLE2, 1, 0);
                break;
            default:
                break;
                
        }
        
    }
}