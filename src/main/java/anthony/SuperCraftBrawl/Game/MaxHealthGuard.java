package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/*
 * The private game Max Health modifier is the only thing allowed to raise a player's max health, and only
 * while they're a player in that private game. Everywhere else - the lobby, public games, other gamemodes,
 * spectating, and in the player's saved .dat file - it's forced back to the normal 10 hearts:
 * - first thing on join and on quit (so a raised value is never saved or carried into the next login)
 * - on every world change
 * - once a second for every online player, as a catch-all
 * - for everyone when the plugin shuts down
 */
public class MaxHealthGuard implements Listener {

    public static final double DEFAULT_MAX_HEALTH = 20.0;

    private final Core main;

    public MaxHealthGuard(Core main) {
        this.main = main;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) enforce(player);
            }
        }.runTaskTimer(main, 20L, 20L);
    }

    /** Only players actually in a private game may have more than the normal max health. */
    private boolean mayHaveRaisedMaxHealth(Player player) {
        if (main.getGameManager() == null) return false;
        GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
        return game != null && game.isPrivateGame() && player.getWorld() == game.getMapWorld();
    }

    public void enforce(Player player) {
        if (player.getMaxHealth() != DEFAULT_MAX_HEALTH && !mayHaveRaisedMaxHealth(player)) {
            reset(player);
        }
    }

    public static void reset(Player player) {
        if (player.getMaxHealth() != DEFAULT_MAX_HEALTH) {
            player.setMaxHealth(DEFAULT_MAX_HEALTH); // also clamps current health down to 20
        }
    }

    public void resetAll() {
        for (Player player : Bukkit.getOnlinePlayers()) reset(player);
    }

    // LOWEST so it runs before every other join handler - nothing that fails later on can skip it
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        reset(event.getPlayer());
    }

    // The player's .dat is saved after the quit event, so this keeps a raised value from ever being saved
    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        reset(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        enforce(event.getPlayer());
    }
}
