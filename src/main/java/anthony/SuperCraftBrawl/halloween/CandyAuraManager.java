package anthony.SuperCraftBrawl.halloween;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Candy Aura (Spigot 1.8).
 * STRICT PALETTE: ORANGE (COLOURED_DUST), BLACK (SMOKE), PURPLE (WITCH_MAGIC).
 * - Only runs in the configured lobby world (if provided).
 * - Auto-disables for players that leave that world or go offline.
 */
public class CandyAuraManager {
    private final Plugin plugin;
    private final String lobbyWorldName;              // e.g., "lobby-1"
    private final Set<UUID> enabled = new HashSet<>();

    // How often to render (ticks). 5 = 4 times/second
    private static final long PERIOD_TICKS = 5L;

    // ---------- ORANGE DUST COLOR (RGB 255,120,0) ----------
    private static final float ORANGE_R = 1.0f;           // 255/255
    private static final float ORANGE_G = 120f / 255f;    // 120/255
    private static final float ORANGE_B = 0.0f;           // 0/255

    // Tune counts/radius centrally
    private static final int DUST_FEET_COUNT = 6;
    private static final int DUST_FEET_RADIUS = 16;

    private static final int SMOKE_DATA = 0;              // small black smoke
    private static final int SMOKE_RADIUS = 12;

    private static final int WITCH_RADIUS = 16;

    public CandyAuraManager(Plugin plugin, String lobbyWorldName) {
        this.plugin = plugin;
        this.lobbyWorldName = (lobbyWorldName == null) ? "" : lobbyWorldName;

        // One repeating task for all enabled players
        new BukkitRunnable() {
            @Override public void run() {
                if (enabled.isEmpty()) return;

                for (UUID id : enabled.toArray(new UUID[0])) {
                    Player p = Bukkit.getPlayer(id);
                    if (p == null || !p.isOnline()) {
                        enabled.remove(id);
                        continue;
                    }
                    if (!isInLobby(p)) {
                        enabled.remove(id);
                        continue;
                    }
                    renderCandyAura(p);
                }
            }
        }.runTaskTimer(plugin, PERIOD_TICKS, PERIOD_TICKS);
    }

    // ---- Public API ----
    public boolean toggle(Player p) {
        UUID id = p.getUniqueId();
        if (enabled.contains(id)) {
            enabled.remove(id);
            return false;
        } else {
            if (!isInLobby(p)) return false; // don't enable outside lobby
            enabled.add(id);
            return true;
        }
    }

    public void enable(Player p) {
        if (isInLobby(p)) enabled.add(p.getUniqueId());
    }

    public void disable(Player p) { enabled.remove(p.getUniqueId()); }

    public boolean isEnabled(Player p) { return enabled.contains(p.getUniqueId()); }

    private boolean isInLobby(Player p) {
        if (lobbyWorldName.isEmpty()) return true;
        return p.getWorld().getName().equalsIgnoreCase(lobbyWorldName);
    }

    // --- Particle helpers (Spigot 1.8 "COLOURED_DUST" uses RGB via offsets; speed must be 1) ---
    private void colouredDust(Location loc, float r, float g, float b, int count, int radius) {
        // Only orange dust allowed here
        loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, r, g, b, 1.0f, count, radius);
    }

    private void orangeDust(Location loc, int count, int radius) {
        colouredDust(loc, ORANGE_R, ORANGE_G, ORANGE_B, count, radius);
    }

    private void blackSmoke(Location loc, int radius) {
        // BLACK ONLY: small smoke puff (SMOKE effect)
        loc.getWorld().playEffect(loc, Effect.SMOKE, SMOKE_DATA, radius);
    }

    private void purpleWitch(Location loc, int radius) {
        // PURPLE ONLY: witch magic
        loc.getWorld().playEffect(loc, Effect.WITCH_MAGIC, 0, radius);
    }

    // --- Renders ONLY orange, purple, black ---
    private void renderCandyAura(Player p) {
        Location base = p.getLocation().add(0, 0.05, 0);

        // ORANGE: soft dust at the feet
        orangeDust(base, DUST_FEET_COUNT, DUST_FEET_RADIUS);

        // BLACK: faint smoke wisp close to the ground
        blackSmoke(base, SMOKE_RADIUS);

        // PURPLE: witch magic near waist
        Location waist = base.clone().add(0, 0.7, 0);
        purpleWitch(waist, WITCH_RADIUS);

        // ORANGE: a small orbiting dust ring around waist height
        final double ringRadius = 0.45;
        long t = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            double a = (t / 120.0 + i * Math.PI / 3.0);
            double x = Math.cos(a) * ringRadius;
            double z = Math.sin(a) * ringRadius;
            Location ring = waist.clone().add(x, 0.10, z);
            orangeDust(ring, 1, 8);
        }
    }
}