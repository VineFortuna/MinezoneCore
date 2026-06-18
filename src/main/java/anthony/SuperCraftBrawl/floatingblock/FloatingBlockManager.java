package anthony.SuperCraftBrawl.floatingblock;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;

public final class FloatingBlockManager {

    private Core core;
    private ArmorStand stand;
    private BukkitRunnable spinTask;

    // target
    private static final String WORLD_NAME = "lobby-1";
    private static final double X = 186.5, Y = 116.0, Z = 633.5;
    private static final long DAILY_REWARD_COOLDOWN = 24 * 60 * 60 * 1000L;

    public FloatingBlockManager(Core core) { this.core = core; }

    public void spawn() {
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) {
            this.core.getLogger().warning("[FloatingBlock] World '" + WORLD_NAME + "' is null. Retrying in 40t.");
            Bukkit.getScheduler().runTaskLater(core, this::spawn, 40L);
            return;
        }

        // ensure chunk is loaded
        int cx = (int)Math.floor(X) >> 4;
        int cz = (int)Math.floor(Z) >> 4;
        try { world.getChunkAt(cx, cz).load(true); } catch (Throwable ignored) {}

        remove(); // clean old one if any

        Location loc = new Location(world, X, Y, Z);
        stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(false);
        stand.setRemoveWhenFarAway(false);
        try { stand.setMarker(true); } catch (Throwable ignored) {}

        // block to show (white wool). Change durability for color.
        stand.setHelmet(new ItemStack(Material.WOOL, 1, (short) 0));

        // spin
        spinTask = new BukkitRunnable() {
            double deg = 0;
            @Override public void run() {
                if (stand == null || stand.isDead()) return;
                deg = (deg + 6) % 360; // ~1 rotation every ~3s
                stand.setHeadPose(new EulerAngle(0, Math.toRadians(deg), 0));
            }
        };
        spinTask.runTaskTimer(this.core, 1L, 1L);

        this.core.getLogger().info("[FloatingBlock] Spawned at " + X + ", " + Y + ", " + Z + " in " + WORLD_NAME);
    }

    public void remove() {
        if (spinTask != null) { spinTask.cancel(); spinTask = null; }
        if (stand != null && !stand.isDead()) { stand.remove(); }
        stand = null;
    }

    public void spawnFloatingBlocks() {
        Bukkit.getScheduler().runTaskLater(core, () -> {
            World w = Bukkit.getWorld("lobby-1");

            if (w == null) {
                this.core.getLogger().warning("[FloatingBlocks] lobby-1 is null. Retrying in 40 ticks.");
                spawnFloatingBlocks();
                return;
            }

            Location dailyRewardLoc = new Location(w, 192.5, 108.2, 632.5);
            Location socialsLoc     = new Location(w, 186.5, 108.2, 626.5);
            Location storeLoc       = new Location(w, 192.5, 108.2, 626.5);
            Location classesLoc     = new Location(w, 186.5, 108.2, 632.5);

            /*
             * Fixes duplicate floating blocks after /save-all + restart.
             *
             * /save-all saves the armor stands to the world.
             * On restart, Bukkit metadata is gone, so old title/subtitle stands
             * like "Click to Claim!" and "Next Reward..." can stay behind.
             *
             * This removes old saved floating-block armor stands around these locations
             * before spawning the fresh plugin-controlled ones.
             */
            cleanupSavedFloatingBlockStands(w, dailyRewardLoc, socialsLoc, storeLoc, classesLoc);

            // 1) DAILY REWARD — subtitle is per-player via NMS metadata packets
            this.core.dailyRewardEntry = this.core.floatingBlocks.add(
                    dailyRewardLoc,
                    new ItemStack(Material.CHEST, 1, (short) 0),
                    "&e&lDAILY REWARD",
                    (player) -> {
                        PlayerData d = this.core.getDataManager().getPlayerData(player);
                        if (d == null) return "&aClick to Claim!";

                        long elapsed = System.currentTimeMillis() - d.lastDailyReward;

                        if (d.lastDailyReward > 0 && elapsed < DAILY_REWARD_COOLDOWN) {
                            return "&rNext Reward: &a" + this.core.formatCooldown(DAILY_REWARD_COOLDOWN - elapsed);
                        }

                        return "&aClick to Claim!";
                    },
                    (player) -> {
                        this.core.getDailyRewards().claimDailyReward(player);
                    }
            );

            // 2) SOCIALS
            this.core.floatingBlocks.add(
                    socialsLoc,
                    new ItemStack(Material.BOOKSHELF, 1, (short) 0),
                    "&6&lSOCIALS",
                    "&aRight Click",
                    (player) -> {
                        player.performCommand("socials");
                    }
            );

            // 3) STORE
            this.core.floatingBlocks.add(
                    storeLoc,
                    new ItemStack(Material.EMERALD_BLOCK, 1, (short) 0),
                    "&6&lSTORE",
                    "&aRight Click",
                    (player) -> {
                        player.performCommand("store");
                    }
            );

            this.core.floatingBlocks.add(
                    classesLoc,
                    new ItemStack(Material.ENCHANTMENT_TABLE, 1, (short) 0),
                    "&6&lSCB CLASSES",
                    "&aRight Click",
                    (player) -> {
                        player.performCommand("classes");
                    }
            );

            this.core.floatingBlocks.spawnAll();
        }, 40L);
    }

    private void cleanupSavedFloatingBlockStands(World world, Location... baseLocations) {
        if (world == null || baseLocations == null) return;

        for (ArmorStand stand : new ArrayList<ArmorStand>(world.getEntitiesByClass(ArmorStand.class))) {
            if (stand == null || stand.isDead()) continue;

            boolean nearFloatingBlock = false;

            for (Location baseLoc : baseLocations) {
                if (baseLoc == null) continue;

                if (isNearFloatingBlockLocation(stand.getLocation(), baseLoc)) {
                    nearFloatingBlock = true;
                    break;
                }
            }

            if (!nearFloatingBlock) continue;

            if (isOldFloatingBlockStand(stand)) {
                try {
                    stand.remove();
                } catch (Throwable ignored) {}
            }
        }
    }

    private boolean isNearFloatingBlockLocation(Location standLoc, Location baseLoc) {
        if (standLoc == null || baseLoc == null) return false;
        if (standLoc.getWorld() == null || baseLoc.getWorld() == null) return false;
        if (!standLoc.getWorld().equals(baseLoc.getWorld())) return false;

        Location titleLoc = baseLoc.clone().add(0, 2.55, 0);
        Location subLoc   = baseLoc.clone().add(0, 2.30, 0);

        double blockRadiusSq = 1.25 * 1.25;
        double textRadiusSq  = 1.25 * 1.25;

        return standLoc.distanceSquared(baseLoc) <= blockRadiusSq
                || standLoc.distanceSquared(titleLoc) <= textRadiusSq
                || standLoc.distanceSquared(subLoc) <= textRadiusSq;
    }

    private boolean isOldFloatingBlockStand(ArmorStand stand) {
        String name = stand.getCustomName();
        String strippedName = "";

        if (name != null) {
            strippedName = org.bukkit.ChatColor.stripColor(name).toLowerCase(java.util.Locale.US);
        }

        if (strippedName.startsWith("fb:")) return true;
        if (strippedName.contains("daily reward")) return true;
        if (strippedName.contains("click to claim")) return true;
        if (strippedName.contains("next reward")) return true;
        if (strippedName.contains("socials")) return true;
        if (strippedName.contains("store")) return true;
        if (strippedName.contains("right click")) return true;

        try {
            ItemStack helmet = stand.getEquipment() == null ? null : stand.getEquipment().getHelmet();
            return helmet != null && helmet.getType() != Material.AIR;
        } catch (Throwable ignored) {
            return false;
        }
    }

}