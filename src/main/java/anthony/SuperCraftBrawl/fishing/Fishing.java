package anthony.SuperCraftBrawl.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.FishingDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.*;

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

    public Block treasureBlock;
    
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
            FishType fish = getFish(main.getFishingArea(event.getHook().getLocation()));
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

            boolean updateScoreboard = false;

            // When caught for the first time
            if (details.timesCaught == 0) {
                // Message
                p.sendMessage(main.color("&2&l============================================="));
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
                p.sendMessage(main.color("&2&l============================================="));
                
                // Firework
                if (getTotalFish(p) == FishType.values().length) {
                    p.playSound(p.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1);
                    p.sendMessage(main.color("&3&l(!) &rCongratulations! You caught everything!"));
                    Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    fwm.setPower(1);
                    fwm.addEffect(FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.BALL_LARGE)
                            .flicker(true).build());
                    fw.setFireworkMeta(fwm);
                }
            }
            if (fish == FishType.CRATE) {
                p.sendMessage(main.color("&3&l(!) &rYou have found &e1 MysteryChest&r!"));
                data.mysteryChests++;
            } else if (fish == FishType.EXP) {
                int r = rand.nextInt(40) + 481;
                data.exp += r;
                p.sendMessage(main.color("&3&l(!) &rYou have gained &e" + r + " EXP&r!"));
                if (data.exp >= 2500) {
                    data.level++;
                    data.exp -= 2500;
                    p.sendMessage(main.color("&8&m----------------------------------------"));
					p.sendMessage(main.color("&6&l✦✦ &e&lLEVEL UP! &6&l✦✦"));
					p.sendMessage(main.color("&7You are now &e&lLevel &6&l" + data.level + " &7— nice work!"));
					p.sendMessage(main.color("&8&m----------------------------------------"));

					// (optional but fun) little audio feedback on 1.8:
					p.playSound(p.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.15f);
                }
                updateScoreboard = true;
            } else if (fish == FishType.TOKENS) {
                int r = rand.nextInt(20) + 41;
                data.tokens += r;
                p.sendMessage(main.color("&3&l(!) &rYou have found &e" + r + " Tokens&r!"));
                updateScoreboard = true;
            } else if (fish == FishType.MAP) {
                if (data.treasureLoc.isEmpty()) {
                    Block b = randomTreasureBlock();
                    if (b != null) {
                        data.treasureLoc = treasureLocString(b.getLocation());
                    }
                }
                p.sendMessage(main.color("&3&l(!) &rTreasure Map added to collection!"));
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
            if (main.getGameManager().GetInstanceOfPlayer(p) == null && updateScoreboard)
            	main.getScoreboardManager().lobbyBoard(p);
        }
    }
    
    private FishType getRandomFish(FishArea area) {
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
        else
            rarity = FishRarity.COMMON;
        
        for (FishType t : fishTypes) {
            if (t.getRarity() == rarity) {
                if (area != null && t.getAreas() != null && !t.getAreas().isEmpty()) {
                    if (t.getAreas().contains(area))
                        fishes.add(t);
                } else {
                    fishes.add(t);
                }
            }
        }
        return fishes.get(rand.nextInt(fishes.size()));
    }
    
    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        for (Item i : fishItems) {
            i.remove();
        }
    }
    private void removeFish(Item i) {
        Bukkit.getServer().getScheduler().runTaskLater(main, new Runnable(){
            public void run() {
                fishItems.remove(i);
                i.remove();
            }
        }, 80L);
    }
    private FishType getRandomLoot(FishRarity rarity, FishArea area) {
        ArrayList<FishType> loot = new ArrayList<>();
        for (FishType l : fishTypes) {
            if (l.getRarity() == rarity) {
                if (area != null && l.getAreas() != null && !l.getAreas().isEmpty()) {
                    if (l.getAreas().contains(area))
                        loot.add(l);
                } else {
                    loot.add(l);
                }
            }
        }
        return loot.get(rand.nextInt(loot.size()));
    }
    public FishType getFish(FishArea area) {
        int r = rand.nextInt(100)+1;
        if (r <= treasure)
            return getRandomLoot(FishRarity.TREASURE, area);
        else if (r <= treasure + junk)
            return getRandomLoot(FishRarity.JUNK, area);
        
        return getRandomFish(area);
    }
    
    private void reward(Player p, FishRarity rarity) {
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
    
    private void friendship(Player p, int level) {
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
                if (main.getGameManager().GetInstanceOfPlayer(p) == null)
                    main.getScoreboardManager().lobbyBoard(p);
            }
        }
    }
    
    private void particles(Player p, FishRarity r) {
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

    public int getTotalFish(Player player, FishRarity... rarity) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        int totalFished = 0;
        for (FishType type : FishType.values()) {
            if (rarity == null || Arrays.asList(rarity).contains(type.getRarity())) {
                FishingDetails details = data.playerFishing.get(type.getId());
                if (details != null) {
                    totalFished++;
                }
            }
        }
        return totalFished;
    }

    public int getTotalFish(Player player) {
        return getTotalFish(player, null);
    }

    public boolean hasAllFish(Player player) {
        return getTotalFish(player) == FishType.values().length;
    }

    public boolean hasUnlockedFisherman(Player player) {
        return getFishermanProgress(player) == 50;
    }

    public int getFishermanProgress(Player player) {
        int commonFished = getTotalFish(player, FishRarity.COMMON);
        int rareFished = getTotalFish(player, FishRarity.RARE);
        int epicFished = getTotalFish(player, FishRarity.EPIC);
        int mythicFished = getTotalFish(player, FishRarity.MYTHIC);
        int legendaryFished = getTotalFish(player, FishRarity.LEGENDARY);
        int junkFished = getTotalFish(player, FishRarity.JUNK);
        int treasureFished = getTotalFish(player, FishRarity.TREASURE);

        // TO UNLOCK FISHERMAN
        // COMMON - 11
        // RARE - 8
        // EPIC - 8
        // MYTHIC - 7
        // LEGENDARY - 5
        // JUNK - 7
        // TREASURE - 4

        return Math.min(commonFished, 11) + Math.min(rareFished, 8) + Math.min(epicFished, 8)
                + Math.min(mythicFished, 7) + Math.min(legendaryFished, 5) + Math.min(junkFished, 7)
                + Math.min(treasureFished, 4);
    }

    public Block randomTreasureBlock() {
        FishArea[] areas = FishArea.values();
        Random rand = new Random();
        int areaId = rand.nextInt(areas.length);
        FishArea fishArea = areas[areaId];

        Location centerLoc = fishArea.getLocation().toLocation(main.getLobbyWorld());
        int centerX = centerLoc.getBlockX();
        int centerY = centerLoc.getBlockY();
        int centerZ = centerLoc.getBlockZ();

        for (int attempt = 0; attempt < 1000; attempt++) {
            int x = centerX - fishArea.getBoundsX() + rand.nextInt(fishArea.getBoundsX() * 2 + 1);
            int z = centerZ - fishArea.getBoundsZ() + rand.nextInt(fishArea.getBoundsZ() * 2 + 1);
            int y = centerY - fishArea.getBoundsY() + rand.nextInt(fishArea.getBoundsY() * 2 + 1);

            Block block = main.getLobbyWorld().getBlockAt(x, y, z);
            Block blockAbove = block.getRelative(BlockFace.UP);

            if (block.getType().isSolid() && (blockAbove.getType() == Material.WATER || blockAbove.getType() == Material.STATIONARY_WATER)
                    && fishArea.isInBounds(blockAbove.getLocation())) {
                return block;
            }
        }
        return null; // no valid block found after max attempts
    }

    public String treasureLocString(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x + "," + y + "," + z;
    }

    public Location getTreasureLoc(String loc) {
        String[] str = loc.split(",");
        return new Location(main.getLobbyWorld(), Double.parseDouble(str[0]), Double.parseDouble(str[1]), Double.parseDouble(str[2]));
    }

    @EventHandler
    public void findTreasure(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        PlayerData data = main.getDataManager().getPlayerData(p);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && data != null) {
            if (!data.treasureLoc.isEmpty() && event.getClickedBlock().getLocation()
                    .equals(getTreasureLoc(data.treasureLoc))) {

                Location loc = event.getClickedBlock().getLocation().add(0.5, 0, 0.5);
                Location chestLoc = loc.clone();

                // make it face the player
                chestLoc.setDirection(p.getLocation().toVector().subtract(chestLoc.toVector()));

                // ARMORSTAND CHEST ANIMATION
                final ArmorStand stand = loc.getWorld().spawn(chestLoc.add(0, -0.5, 0), ArmorStand.class);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setSmall(false);
                stand.setHelmet(new ItemStack(Material.CHEST));

                // animate chest rising slightly
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    stand.teleport(stand.getLocation().add(0, 0.5, 0));
                    loc.getWorld().playSound(loc, Sound.CHEST_OPEN, 1f, 1f);

                    ItemStack reward;
                    // pop out random loot
                    for (int i = 0; i < 5; i++) {
                        if (data.treasureOpened == 0 && i == 4) {
                            reward = ItemHelper.create(Material.GOLD_BLOCK);
                            p.sendMessage(main.color("&3&l(!) &rYou have found &6Treasure Hoard &rwin effect!"));
                            data.treasureOpened = 1;
                        } else {
                            reward = getRandomReward(p);
                        }
                        Item dropped = loc.getWorld().dropItem(loc.clone().add(0, 1, 0), reward);
                        dropped.setPickupDelay(Integer.MAX_VALUE);
                        fishItems.add(dropped);

                        removeFish(dropped);

                        org.bukkit.util.Vector v = new Vector(
                                (Math.random() - 0.5) * 0.5,
                                0.5 + (Math.random() * 0.3),
                                (Math.random() - 0.5) * 0.5
                        );
                        dropped.setVelocity(v);
                    }

                }, 20L); // 1 second later

                // remove chest after a few seconds
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    loc.getWorld().playSound(loc, Sound.CHEST_CLOSE, 1f, 1f);

                    // use 1.8 particle (CLOUD doesn't exist yet, so use SMOKE_NORMAL or EXPLOSION_NORMAL)
                    loc.getWorld().playEffect(loc.clone().add(0, 0.5, 0), Effect.SMOKE, 4);

                    stand.remove();
                }, 60L); // after 3 seconds
                // --- END ARMORSTAND CHEST ---

                // Update treasure map state
                FishingDetails details = data.playerFishing.get(FishType.MAP.getId());
                details.removeCarrying(1);
                if (details.carrying > 0) {
                    Block b = randomTreasureBlock();
                    if (b != null) {
                        data.treasureLoc = treasureLocString(b.getLocation());
                    }
                } else {
                    data.treasureLoc = "";
                }
                main.getDataManager().saveData(data);

                p.sendMessage(main.color("&3&l(!) &rYou opened the &eSunken Treasure Chest&r!"));
            }
        }
    }

    private ItemStack getRandomReward(Player p) {
        FishType reward;
        int amount = 0;
        boolean updateScoreboard = false;
        PlayerData data = main.getDataManager().getPlayerData(p);

        Random rand = new Random();
        int r = rand.nextInt(100) + 1;

        if (r <= 10) {
            reward = FishType.TOKENS;
            amount = rand.nextInt(50) + 226;
            data.tokens += amount;
            p.sendMessage(main.color("&3&l(!) &rYou have found &e" + amount + " Tokens&r!"));
            updateScoreboard = true;
        } else if (r <= 20) {
            reward = FishType.EXP;
            amount = rand.nextInt(40) + 481;
            data.exp += amount;
            p.sendMessage(main.color("&3&l(!) &rYou have gained &e" + r + " EXP&r!"));
            if (data.exp >= 2500) {
                data.level++;
                data.exp -= 2500;
                p.sendMessage(main.color("&e&lLEVEL UPGRADED!"));
                p.sendMessage(main.color("&r&l(!) &rYou are now Level " + data.level + "&r!"));
            }
            updateScoreboard = true;
        } else if (r <= 50) {
            reward = FishType.TOKENS;
            amount = rand.nextInt(20) + 31;
            data.tokens += amount;
            p.sendMessage(main.color("&3&l(!) &rYou have found &e" + amount + " Tokens&r!"));
            updateScoreboard = true;
        } else if (r <= 80) {
            reward = FishType.EXP;
            amount = rand.nextInt(40) + 81;
            data.exp += amount;
            p.sendMessage(main.color("&3&l(!) &rYou have gained &e" + amount + " EXP&r!"));
            if (data.exp >= 2500) {
                data.level++;
                data.exp -= 2500;
                p.sendMessage(main.color("&e&lLEVEL UPGRADED!"));
                p.sendMessage(main.color("&r&l(!) &rYou are now Level " + data.level + "&r!"));
            }
            updateScoreboard = true;
        } else {
            reward = FishType.CRATE;
            amount = 1;
            p.sendMessage(main.color("&3&l(!) &rYou have found &e1 MysteryChest&r!"));
            data.mysteryChests += amount;
            hologram(p);
        }

        if (main.getGameManager().GetInstanceOfPlayer(p) == null && updateScoreboard)
            main.getScoreboardManager().lobbyBoard(p);

        return reward.getItem();
    }

    private void hologram(Player player) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        if (data != null && main.msHologram.get(player) != null) {
            if (player.getWorld() == main.getLobbyWorld()) {
                EntityArmorStand stand = main.msHologram.get(player);
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(
                        stand.getId());
                ((CraftPlayer) player).getHandle().playerConnection
                        .sendPacket(destroyPacket);
                Location loc = new Location(main.getLobbyWorld(), 194.520, 115.7, 641.500);

                WorldServer s = ((CraftWorld) loc.getWorld()).getHandle();
                stand = new EntityArmorStand(s);

                stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
                stand.setCustomName(
                        main.color("&e&l" + data.mysteryChests + " &eto open!"));
                stand.setCustomNameVisible(true);
                stand.setGravity(false);
                stand.setInvisible(true);
                PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(
                        stand);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                main.msHologram.put(player, stand);
                main.getDataManager().saveData(data);
            }
        }
    }

}