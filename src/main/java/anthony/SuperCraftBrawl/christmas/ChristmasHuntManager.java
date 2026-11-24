package anthony.SuperCraftBrawl.christmas;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.util.*;

public class ChristmasHuntManager implements Listener {

    public static final String EVENT_KEY = "CHRISTMAS2026";
    public static final int TOTAL = 10;
    private static final String PREFIX = "&8[&c&lHUNT&8] &r";

    private final Core core;
    private final ChristmasHuntDAO dao;

    private static final String WORLD = "lobby-1";
    private static final List<int[]> PRESENTS = Arrays.asList(
            new int[] { 177, 105, 671 }, // christmas tree freebie (#1)
            new int[] { 288, 109, 627 }, // lush cave (#2)
            new int[] { 253, 144, 638 }, // castle (#3)
            new int[] { 25, 114, 772 }, // casino (#4)
            new int[] { 63, 114, 873 }, // ice cream parlour (#5)
            new int[] { 77, 116, 926 }, // park (#6)
            new int[] { 185, 104, 566 }, // parkour (#7)
            new int[] { 115, 114, 643 }, // town house (#8)
            new int[] { 316, 93, 512 }, // pond house (#9)
            new int[] { 229, 86, 565 } // market (#10)
    );

    public ChristmasHuntManager(Core core) {
        this.core = core;
        this.dao = new ChristmasHuntDAO(core, EVENT_KEY, TOTAL);
        Bukkit.getPluginManager().registerEvents(this, core);
    }

    private int indexOfPresent(Block block) {
        if (block == null)
            return -1;
        if (!block.getWorld().getName().equalsIgnoreCase(WORLD))
            return -1;
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        for (int i = 0; i < PRESENTS.size(); i++) {
            int[] a = PRESENTS.get(i);
            if (a[0] == bx && a[1] == by && a[2] == bz)
                return i;
        }
        return -1;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        int idx = indexOfPresent(e.getClickedBlock());
        if (idx < 0)
            return; // not a present
        e.setCancelled(true);

        Player p = e.getPlayer();
        UUID id = p.getUniqueId();

        if (dao.hasFound(id, idx)) {
            p.sendMessage(core.color("&c&l(!) &rYou already found this present!"));
            p.playEffect(e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5), Effect.SMOKE, 1);
            return;
        }

        int newMask = dao.markFound(id, idx);
        int foundTotal = Integer.bitCount(newMask);
        int humanNumber = idx + 1;

        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1.2f);
        p.playEffect(e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5), Effect.HAPPY_VILLAGER, 1);
        p.sendMessage(core.color(PREFIX + "You found &aPresent #" + humanNumber + "&a! &7(&e" + foundTotal
                + "&7/&e" + TOTAL + "&7)"));

        grantMilestoneReward(p, foundTotal);

        if (foundTotal == TOTAL)
            reward(p);

        refreshLobbyBoard(p);
    }

    private void giveTokensReward(Player p, int amount) {
        PlayerData data = core.getDataManager().getPlayerData(p);
        if (data != null)
            data.tokens += amount;
    }

    private void giveExpReward(Player p, int amount) {
        PlayerData data = core.getDataManager().getPlayerData(p);
        if (data != null) {
            data.exp += amount;
            core.getListener().checkIfLevelUp(p);
        }
    }

    private void giveMysteryChests(Player p, int amount) {
        PlayerData data = core.getDataManager().getPlayerData(p);
        if (data != null)
            data.mysteryChests += amount;
    }

    // Prizes based on how many presents the player has found in total
    private void grantMilestoneReward(Player p, int foundTotal) {
        int bpXp = (foundTotal == 5 || foundTotal == 10) ? 50 : 25;
        core.getBattlePassManager().addXp(p, bpXp);
        p.sendMessage(core.color(PREFIX + "You were rewarded with &e" + bpXp + " Battle Pass XP"));

        switch (foundTotal) {
            case 2:
                p.sendMessage(core.color(PREFIX + "You were rewarded with &e2 Mystery Chests"));
                giveMysteryChests(p, 2);
                break;
            case 3:
                p.sendMessage(core.color(PREFIX + "You unlocked: &a&lMR. GRINCH &rtitle cosmetic"));
                break;
            case 4:
                p.sendMessage(core.color(PREFIX + "You were rewarded with &e50 Tokens"));
                giveTokensReward(p, 50);
                break;
            case 5:
                p.sendMessage(core.color(PREFIX + "You unlocked: &a&lGrinch &routfit cosmetic"));
                break;
            case 6:
                p.sendMessage(core.color(PREFIX + "You were rewarded with &e150 EXP"));
                giveExpReward(p, 150);
                break;
            case 7:
                p.sendMessage(core.color(PREFIX + "You unlocked: &bSnowflake &rhook trail"));
                break;
            case 8:
                p.sendMessage(core.color(PREFIX + "You unlocked: &c&lRudolph &rpet cosmetic"));
                break;
            case 10:
                p.sendMessage(core.color("&c&l(!) &rYou found all &e10 &rpresents!"));
                p.sendMessage(core.color("&c&l(!) &rYou unlocked &a&l&oGrinch&r class!"));
                break;
        }
    }

    private void reward(Player p) {
        Bukkit.broadcastMessage(core
                .color("&8[&c&lPRESENT HUNT&8] &e" + p.getName() + " &6has found all 10 Presents! Merry Christmas!"));

        World w = p.getWorld();
        Location base = p.getLocation();
        Random rand = new Random();

        org.bukkit.Color[] palette = new org.bukkit.Color[] {
                org.bukkit.Color.RED,
                org.bukkit.Color.GREEN,
                org.bukkit.Color.WHITE,
                org.bukkit.Color.YELLOW
        };

        FireworkEffect.Type[] shapes = new FireworkEffect.Type[] {
                FireworkEffect.Type.BALL,
                FireworkEffect.Type.BALL_LARGE,
                FireworkEffect.Type.STAR,
                FireworkEffect.Type.BURST
        };

        for (int i = 0; i < 10; i++) {
            // small random offset around the player
            Location spawn = base.clone().add((rand.nextDouble() - 0.5) * 2.5, 0, (rand.nextDouble() - 0.5) * 2.5);

            Firework fw = (Firework) w.spawnEntity(spawn, EntityType.FIREWORK);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.setPower(1 + rand.nextInt(2));

            FireworkEffect effect = FireworkEffect.builder()
                    .with(shapes[rand.nextInt(shapes.length)])
                    .withColor(palette[rand.nextInt(palette.length)], palette[rand.nextInt(palette.length)])
                    .withFade(palette[rand.nextInt(palette.length)])
                    .flicker(rand.nextBoolean())
                    .trail(true)
                    .build();

            meta.addEffect(effect);
            fw.setFireworkMeta(meta);
        }
    }

    // True once the player has found every present (unlocks Grinch class)
    public boolean hasCompletedHunt(Player p) {
        return dao.isComplete(p.getUniqueId());
    }

    // For the scoreboard
    public int getFoundCount(UUID uuid) {
        return Integer.bitCount(dao.getProgress(uuid));
    }

    // For the admin command
    public ChristmasHuntDAO getDao() {
        return dao;
    }

    public void refreshLobbyBoard(Player p) {
        try {
            core.getScoreboardManager().lobbyBoard(p);
        } catch (Throwable ignored) {
        }
    }

    public void placeHeadsOnEnable(final String base64Texture) {
        Bukkit.getScheduler().runTask(core, () -> {
            World w = Bukkit.getWorld(WORLD);
            if (w == null) {
                core.getLogger().severe("[Christmas] World not found: " + WORLD);
                return;
            }
            for (int[] c : PRESENTS) {
                Location loc = new Location(w, c[0], c[1], c[2]);
                placeCustomHeadBlock(loc, base64Texture);
            }
            core.getLogger().info("[Christmas] Placed " + PRESENTS.size() + " present heads in " + WORLD + ".");
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
        } catch (Exception ignored) {
        }
        skull.update(true, false);
    }
}