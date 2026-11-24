package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks each player's cast fishing hook so hook trail cosmetics know where it is while flying. */
public class HookTracker implements Listener {

    private final Map<UUID, FishHook> hooks = new ConcurrentHashMap<>();
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();

    public HookTracker(Core main) {
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    /** Returns the hook's location while it is in the air, or null if there is no hook, it has stopped,
     *  or this is the first poll right after casting (so the trail doesn't start in the player's face). */
    public Location getFlyingLocation(Player player) {
        UUID uuid = player.getUniqueId();
        FishHook hook = hooks.get(uuid);
        if (hook == null) return null;

        Location location = hook.getLocation();
        Location previous = lastLocations.put(uuid, location);

        // Once the hook lands or hits water it stays stopped until the next cast.
        if (!hook.isValid() || location.getBlock().isLiquid() || location.equals(previous)) {
            stop(uuid);
            return null;
        }

        // previous is only null on the first poll right after a cast (lastLocations was cleared in onFish).
        if (previous == null) return null;
        return location;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            hooks.put(uuid, event.getHook());
            lastLocations.remove(uuid);
        } else {
            stop(uuid);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stop(event.getPlayer().getUniqueId());
    }

    private void stop(UUID uuid) {
        hooks.remove(uuid);
        lastLocations.remove(uuid);
    }
}