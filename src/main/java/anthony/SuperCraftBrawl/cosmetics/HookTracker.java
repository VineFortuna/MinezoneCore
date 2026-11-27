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
    /** Where each hook was thrown from, so the trail can begin as soon as the hook is a little way out. */
    private final Map<UUID, Location> castLocations = new ConcurrentHashMap<>();

    /** The trail starts once the hook is this far (in blocks) from where it was thrown: not in the player's face. */
    private static final double MIN_START_DISTANCE = 1.5;

    public HookTracker(Core main) {
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    /** Returns the hook's location while it is in the air, or null if there is no hook, it has stopped,
     *  or it has only just been thrown (so the trail doesn't start in the player's face). */
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

        // previous is only null on the first poll right after a cast (lastLocations was cleared in onFish). The trail
        // starts there if the hook is already out of the player's face (its next poll is two ticks later, about
        // three blocks on), instead of always waiting for that one
        if (previous == null && !isOutOfFace(uuid, location)) return null;
        return location;
    }

    private boolean isOutOfFace(UUID uuid, Location hook) {
        Location thrownFrom = castLocations.get(uuid);
        return thrownFrom != null && thrownFrom.getWorld() == hook.getWorld()
                && thrownFrom.distanceSquared(hook) >= MIN_START_DISTANCE * MIN_START_DISTANCE;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            hooks.put(uuid, event.getHook());
            castLocations.put(uuid, event.getHook().getLocation());
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
        castLocations.remove(uuid);
    }
}