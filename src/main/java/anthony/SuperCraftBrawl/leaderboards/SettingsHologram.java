package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SettingsHologram {

    private final Core core;

    private final List<UUID> hologramEntities = new ArrayList<>();

    public SettingsHologram(Core core) {
        this.core = core;
    }

    // Removes old holograms left from reloads
    public void cleanupOldHolograms() {

        World w = core.getLobbyWorld();

        if (w == null) return;

        for (ArmorStand as : w.getEntitiesByClass(ArmorStand.class)) {

            String name = as.getCustomName();

            if (name == null) continue;

            String plain = ChatColor.stripColor(name)
                    .trim()
                    .toLowerCase();

            if (plain.equals("leaderboard settings")
                    || plain.equals("right-click to change scope")
                    || plain.equals("click to change settings")) {

                try {
                    as.remove();
                } catch (Throwable ignored) {}
            }
        }
    }

    public void spawnLeaderboardSettingsHologram(double x, double y, double z) {

        World w = core.getLobbyWorld();

        if (w == null) {
            core.getLogger().warning("[LB-Settings] Lobby world is null.");
            return;
        }

        // Ensure chunk loaded
        int cx = (int) Math.floor(x) >> 4;
        int cz = (int) Math.floor(z) >> 4;

        try {
            w.getChunkAt(cx, cz).load(true);
        } catch (Throwable ignored) {}

        Location loc = new Location(w, x, y, z);

        // Title line
        ArmorStand title = w.spawn(loc, ArmorStand.class);

        title.setGravity(false);
        title.setVisible(false);
        title.setSmall(true);
        title.setBasePlate(false);
        title.setCustomName(core.color("&e&nLeaderboard Settings"));
        title.setCustomNameVisible(true);
        title.setRemoveWhenFarAway(false);

        hologramEntities.add(title.getUniqueId());

        // Footer line
        ArmorStand footer = w.spawn(loc.clone().add(0, -0.40, 0), ArmorStand.class);

        footer.setGravity(false);
        footer.setVisible(false);
        footer.setSmall(true);
        footer.setBasePlate(false);
        footer.setCustomName(core.color("&bClick to change settings"));
        footer.setCustomNameVisible(true);
        footer.setRemoveWhenFarAway(false);

        hologramEntities.add(footer.getUniqueId());

        core.getLogger().info("[LB-Settings] Spawned hologram at "
                + x + ", " + y + ", " + z);
    }

    public boolean isSettingsHologram(UUID entityId) {
        return hologramEntities.contains(entityId);
    }
}