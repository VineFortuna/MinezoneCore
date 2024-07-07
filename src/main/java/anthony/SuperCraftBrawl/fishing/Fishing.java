package anthony.SuperCraftBrawl.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.DatabaseManager;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
            p.sendMessage(main.color("&r&l(&3&l!&r&l) &rYou caught a "
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
            }
            if (fish == FishType.CRATE) {
                p.sendMessage(main.color("&5&l(!) &rYou have found &e1 Mystery Chest!"));
                data.mysteryChests++;
            } else if (fish == FishType.TOKENS) {
                int r = rand.nextInt(15) + 11;
                p.sendMessage(main.color("&5&l(!) &rYou have found &e" + r + " Tokens!"));
                data.tokens += r;
                if (main.getGameManager().GetInstanceOfPlayer(p) == null)
                    main.LobbyBoard(p);
            }
            reward(p, fish.getRarity());
            data.totalcaught++;
            data.caught++;
            details.addCaught(1);
            //main.getDataManager().saveData(data);
            removeFish(i);
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
        }, 60L);
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
        particles(p, rarity);
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