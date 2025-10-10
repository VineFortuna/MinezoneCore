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
 * - Renders only in the configured lobby world.
 * - If a player leaves that world, the aura is automatically DISABLED.
 */
public class CandyAuraManager {
    private final Plugin plugin;
    private final String lobbyWorldName;              // e.g. "lobby-1"
    private final Set<UUID> enabled = new HashSet<>();

    // how often to render (ticks). 5 = 4 times/second
    private static final long PERIOD_TICKS = 5L;

    public CandyAuraManager(Plugin plugin, String lobbyWorldName) {
        this.plugin = plugin;
        this.lobbyWorldName = (lobbyWorldName == null) ? "" : lobbyWorldName;

        // one repeating task for all players
        new BukkitRunnable() {
            @Override public void run() {
                if (enabled.isEmpty()) return;

                // copy to avoid CME when we remove during iteration
                for (UUID id : enabled.toArray(new UUID[0])) {
                    Player p = Bukkit.getPlayer(id);
                    if (p == null || !p.isOnline()) {
                        enabled.remove(id);
                        continue;
                    }

                    // If player left lobby, auto-disable so it won't play until re-enabled.
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
        if (enabled.contains(p.getUniqueId())) {
            enabled.remove(p.getUniqueId());
            return false;
        } else {
            if (!isInLobby(p)) return false; // don't enable outside lobby
            enabled.add(p.getUniqueId());
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

    // fun little swirl with mixed "candy" effects
    private void renderCandyAura(Player p) {
        Location base = p.getLocation().add(0, 0.1, 0);

        // light “sparkles” around feet
        p.getWorld().playEffect(base, Effect.HAPPY_VILLAGER, 0, 16);
        p.getWorld().playEffect(base, Effect.CRIT, 0, 16);

        // small purple-ish witch magic near waist
        Location waist = base.clone().add(0, 0.7, 0);
        p.getWorld().playEffect(waist, Effect.WITCH_MAGIC, 0, 16);

        // tiny swirl offsets
        final double r = 0.45;
        for (int i = 0; i < 6; i++) {
            double a = (System.currentTimeMillis() / 120.0 + i * Math.PI / 3.0);
            double x = Math.cos(a) * r;
            double z = Math.sin(a) * r;
            Location ring = waist.clone().add(x, 0.1, z);
            p.getWorld().playEffect(ring, Effect.HAPPY_VILLAGER, 0, 8);
        }
    }
}
