package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.entity.ArmorStand;

public class SettingsHologram {

    private Core core;
    private java.util.UUID lbSettingsStand;       // title line UUID
    private java.util.UUID lbSettingsStandHint;   // hint line UUID

    //Location constants
    private static final double LB_X = 193.377;
    private static final double LB_Y = 107.6;     // raise above floor so text is visible
    private static final double LB_Z = 702.500;

    public SettingsHologram(Core core) {
        this.core = core;
    }

    public void spawnLeaderboardSettingsHologram() {
        org.bukkit.World w = core.getLobbyWorld();
        if (w == null) {
            core.getLogger().warning("[LB-Settings] Lobby world is null; will retry later.");
            return;
        }

        //To ensure chunk is loaded
        int cx = (int)Math.floor(LB_X) >> 4;
        int cz = (int)Math.floor(LB_Z) >> 4;
        try { w.getChunkAt(cx, cz).load(true); } catch (Throwable ignored) {}

        // Remove any previous ones with same text (handles reloads)
        for (org.bukkit.entity.ArmorStand as : w.getEntitiesByClass(org.bukkit.entity.ArmorStand.class)) {
            String name = as.getCustomName();
            if (name == null) continue;
            String plain = org.bukkit.ChatColor.stripColor(name).trim().toLowerCase();
            if (plain.equals("leaderboard settings") || plain.equals("right-click to change scope")
                    || plain.equals("click to change settings")) {
                try { as.remove(); } catch (Throwable ignored) {}
            }
        }

        // Hologram to click to change settings
        org.bukkit.Location titleLoc = new org.bukkit.Location(w, LB_X, LB_Y, LB_Z);
        org.bukkit.entity.ArmorStand title = w.spawn(titleLoc, org.bukkit.entity.ArmorStand.class);
        String name = core.color("&e&nLeaderboard Settings");
        title.setGravity(false);
        title.setVisible(false);
        title.setSmall(true);
        title.setBasePlate(false);
        title.setCustomName(name);
        title.setCustomNameVisible(true);
        title.setRemoveWhenFarAway(false);
        this.lbSettingsStand = title.getUniqueId();

        ArmorStand hint = w.spawn(titleLoc.clone().add(0, -0.40, 0), ArmorStand.class);
        String footer = core.color("&bClick to change settings");
        hint.setGravity(false);
        hint.setVisible(false);
        hint.setSmall(true);
        hint.setBasePlate(false);
        hint.setCustomName(footer);
        hint.setCustomNameVisible(true);
        hint.setRemoveWhenFarAway(false);
        this.lbSettingsStandHint = hint.getUniqueId();

        core.getLogger().info("[LB-Settings] Spawned at " + LB_X + ", " + LB_Y + ", " + LB_Z);
    }
}
