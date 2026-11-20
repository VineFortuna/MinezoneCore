package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The "Leaderboard Settings" holograms in the lobby. They are real armor stands, so they are saved into the world
 * with their chunk. Removing them when the plugin shuts down only reaches the chunks that are loaded at that moment
 * (and never happens after a crash), so the ones in unloaded chunks stayed behind and every start added a new
 * copy on top of them. The old copies also can't be clicked, since only the stands spawned in this run are
 * recognised. So before spawning, and whenever a lobby chunk loads, every stand with one of the hologram's lines
 * that this run didn't spawn is removed.
 */
public class SettingsHologram implements Listener {

    /** Every line this hologram has had, plain and lowercase, to recognise a stand left behind by an earlier run. */
    private static final List<String> HOLOGRAM_LINES = Arrays.asList(
            "leaderboard settings",
            "right-click to change scope",
            "click to change settings");

    private final Core core;

    /** The stands spawned in this run. They keep their UUID when their chunk unloads and loads again. */
    private final Set<UUID> hologramEntities = new HashSet<>();

    public SettingsHologram(Core core) {
        this.core = core;
    }

    /** Whether a stand's custom name (null if it has none) is one of the hologram's lines, whatever its colors. */
    static boolean isHologramLine(String customName) {
        if (customName == null) return false;
        return HOLOGRAM_LINES.contains(ChatColor.stripColor(customName).trim().toLowerCase());
    }

    /** Removes every hologram line in the chunk that this run didn't spawn. Returns how many there were. */
    private int removeLeftovers(Chunk chunk) {
        int removed = 0;

        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof ArmorStand)) continue;
            if (hologramEntities.contains(entity.getUniqueId())) continue;

            if (!isHologramLine(((ArmorStand) entity).getCustomName())) continue;

            try {
                entity.remove();
                removed++;
            } catch (Throwable ignored) {}
        }

        return removed;
    }

    /** Leftovers in a lobby chunk that loads later, like ones from a spot the hologram used to be at. */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (event.getWorld() != core.getLobbyWorld()) return;

        int removed = removeLeftovers(event.getChunk());
        if (removed > 0) {
            core.getLogger().info("[LB-Settings] Removed " + removed + " leftover hologram line(s) in chunk "
                    + event.getChunk().getX() + ", " + event.getChunk().getZ());
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
            Chunk chunk = w.getChunkAt(cx, cz);
            chunk.load(true);

            // The copies saved with this chunk by an earlier run go before the new one comes
            int removed = removeLeftovers(chunk);
            if (removed > 0) {
                core.getLogger().info("[LB-Settings] Removed " + removed + " leftover hologram line(s) at "
                        + x + ", " + y + ", " + z);
            }
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
