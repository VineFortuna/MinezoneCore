package anthony.SuperCraftBrawl.halloween;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.util.*;

public class HalloweenHuntManager implements Listener {

    public static final String EVENT_KEY = "HALLOWEEN2025";
    public static final int TOTAL = 10;
    private static final int FULL_MASK = (1 << TOTAL) - 1;

    private final Core core;
    private final HalloweenDAO dao;

    // ===== EDIT THESE =====
    private static final String WORLD = "lobby-1";
    private static final List<int[]> BASKETS = Arrays.asList(
            new int[]{197, 105, 689}, // map selection (#1)
            new int[]{115, 114, 643}, // houses (#2)
            new int[]{126, 111, 702}, // city parkour (#3)
            new int[]{282, 112, 660}, // cobweb cave (#4)
            new int[]{205, 105, 574}, // woods (#5)
            new int[]{295, 94, 532},  // pond (#6)
            new int[]{229, 86, 569},  // market (#7)
            new int[]{8, 112, 841},   // hospital (#8)
            new int[]{72, 116, 929},  // city park (#9)
            new int[]{253, 143, 626}  // castle (#10)
    );

    public HalloweenHuntManager(Core core) {
        this.core = core;
        this.dao = new HalloweenDAO(core, EVENT_KEY);
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    private int indexOfBasket(Block block) {
        if (block == null) return -1;
        if (!block.getWorld().getName().equalsIgnoreCase(WORLD)) return -1;
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        for (int i = 0; i < BASKETS.size(); i++) {
            int[] a = BASKETS.get(i);
            if (a[0] == bx && a[1] == by && a[2] == bz) return i;
        }
        return -1;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        int idx = indexOfBasket(e.getClickedBlock());
        if (idx < 0) return; // not a basket
        e.setCancelled(true);

        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        if (dao.hasFound(id, idx)) {
            p.sendMessage(core.color("&c&l(!) &rYou already found this basket!"));
            p.playEffect(e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5), Effect.SMOKE, 1);
            return;
        }

        // mark THIS basket as found
        int newMask = dao.markFound(id, idx, TOTAL);
        int foundTotal = Integer.bitCount(newMask);
        int humanNumber = idx + 1; // 1..10 based on location

        // Per-basket feedback (location-based number)
        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1.2f);
        p.playEffect(e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5), Effect.HAPPY_VILLAGER, 1);
        p.sendMessage(core.color("&8[&6&lHUNT&8] &aYou found &cBasket #" + humanNumber + "&a! &7(&e" + foundTotal + "&7/&e" + TOTAL + "&7)"));

        // Milestone rewards based on TOTAL found (no basket number text here)
        grantMilestoneReward(p, foundTotal);

        if (newMask == FULL_MASK) reward(p);

        // Update lobby scoreboard
        refreshLobbyBoard(p);
    }

    /*
     * This function gives token rewards based on the amount that is called
     */
    private void giveTokensReward(Player p, int amount) {
        PlayerData data = core.getDataManager().getPlayerData(p);
        if (data != null) data.tokens += amount;
    }

    /*
     * This function gives EXP rewards based on the amount that is called
     */
    private void giveExpReward(Player p, int amount) {
        PlayerData data = core.getDataManager().getPlayerData(p);
        if (data != null) {
            data.exp += amount;
            core.getListener().checkIfLevelUp(p);
        }
    }

    /**
     * Milestone prizes by TOTAL found (no basket-number wording here).
     */
    private void grantMilestoneReward(Player p, int foundTotal) {
        switch (foundTotal) {
            case 1:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Reward: &e25 Tokens"));
                giveTokensReward(p, 25);
                break;
            case 2:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Unlocked: &6&lTrick-or-Treater &7title cosmetic"));
                break;
            case 3:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Unlocked: &6Ritual &7win effect"));
                break;
            case 4:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Unlocked: &9&lCandy Aura &7cosmetic"));
                break;
            case 5:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Reward: &e500 EXP"));
                giveExpReward(p, 500);
                break;
            case 6:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Unlocked: &6&lFreddy &7outfit cosmetic"));
                break;
            case 7:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Unlocked: &6Pumpkin Pie &7death particles"));
                break;
            case 8:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Unlocked: &6&lFreddy Fazbear &7title cosmetic"));
                break;
            case 9:
                p.sendMessage(core.color("&8[&6&lHUNT&8] &7Reward: &e30 Tokens &7and &e700 EXP"));
                giveTokensReward(p, 30);
                giveExpReward(p, 700);
                break;
            case 10:
                p.sendMessage(core.color("&6&l(!) &rYou found all &e10 &rbaskets!"));
                p.sendMessage(core.color("&6&l(!) &rYou unlocked &6&l&oFreddy&r class!"));
                break;
            default:
                // no-op
        }
    }

    private void reward(Player p) {
        Bukkit.broadcastMessage(core.color(
                "&8[&6&lHALLOWEEN HUNT&8] &e" + p.getName() + " &6has found all 10 Baskets! Happy Halloween!"
        ));
    }

    /*
     * Checks if player has unlocked Freddy class by getting all of the Halloween
     * baskets from the lobby event
     */
    public boolean hasUnlockedFreddy(Player p) {
        return core.getListener().getHalloweenEventProgress(p) == 10;
    }

    /** For scoreboard: number found from DB. */
    public int getFoundCount(UUID uuid) {
        return Integer.bitCount(dao.getProgress(uuid));
    }

    /** Expose DAO for admin cmd. */
    public HalloweenDAO getDao() {
        return dao;
    }

    /** Rebuild your lobby board. Replace with your actual call if needed. */
    public void refreshLobbyBoard(Player p) {
        try {
            core.getScoreboardManager().lobbyBoard(p);
        } catch (Throwable ignored) {}
    }

    public void placeHeadsOnEnable(final String base64Texture) {
        Bukkit.getScheduler().runTask(core, () -> {
            World w = Bukkit.getWorld(WORLD);
            if (w == null) {
                core.getLogger().severe("[Halloween] World not found: " + WORLD);
                return;
            }
            for (int[] c : BASKETS) {
                Location loc = new Location(w, c[0], c[1], c[2]);
                placeCustomHeadBlock(loc, base64Texture);
            }
            core.getLogger().info("[Halloween] Placed " + BASKETS.size() + " basket heads in " + WORLD + ".");
        });
    }

    private static void placeCustomHeadBlock(Location loc, String base64Texture) {
        Block b = loc.getBlock();
        b.setType(Material.SKULL);
        b.setData((byte) 1, false);

        Skull skull = (Skull) b.getState();
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", base64Texture));
            Field f = skull.getClass().getDeclaredField("profile");
            f.setAccessible(true);
            f.set(skull, profile);
        } catch (Exception ignored) {}
        skull.update(true, false);
    }
}
