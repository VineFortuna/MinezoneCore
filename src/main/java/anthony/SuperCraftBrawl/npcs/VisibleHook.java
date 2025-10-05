package anthony.SuperCraftBrawl.npcs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class VisibleHook implements Listener {
    private final NPC[] npcs;

    public VisibleHook(NPC... npcs) { this.npcs = npcs; }

    private void showAll(Player p) {
        for (NPC n : npcs) n.showTo(p);
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ChannelInjector.inject(p);
        // small delay so client is fully ready
        Bukkit.getScheduler().runTaskLater(NPCPlugin.getInstance(), () -> showAll(p), 5L);
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        ChannelInjector.uninject(e.getPlayer());
    }

    @EventHandler public void onWorldChange(PlayerChangedWorldEvent e) {
        showAll(e.getPlayer());
    }

    @EventHandler public void onRespawn(PlayerRespawnEvent e) {
        Bukkit.getScheduler().runTaskLater(NPCPlugin.getInstance(), () -> showAll(e.getPlayer()), 5L);
    }
}

