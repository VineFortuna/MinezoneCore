package anthony.SuperCraftBrawl.npcs;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Injects players' Netty channels for click detection and shows NPCs
 * whenever a player (re)appears in the relevant world.
 */
public class VisibleHook implements Listener {
    private final NPC[] npcs;

    public VisibleHook(NPC... npcs) { this.npcs = npcs; }

    private void showAll(Player p) {
        for (NPC n : npcs) {
            if (n != null) n.showTo(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ChannelInjector.inject(p);
        // short delay so the client is fully ready before spawn packets
        Bukkit.getScheduler().runTaskLater(Core.inst(), () -> showAll(p), 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
    	Player p = e.getPlayer();

        // ✅ make sure we forget this viewer so rejoin works
        for (NPC n : npcs) {
            if (n != null) n.hideFrom(p);
        }

        ChannelInjector.uninject(p);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        showAll(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Bukkit.getScheduler().runTaskLater(Core.inst(), () -> showAll(e.getPlayer()), 5L);
    }
}