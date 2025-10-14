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

import java.util.Collection;
import java.util.function.Supplier;

public class VisibleHook implements Listener {
    private final Supplier<Collection<NPC>> npcsSupplier;

    public VisibleHook(Supplier<Collection<NPC>> npcsSupplier) {
        this.npcsSupplier = npcsSupplier;
    }

    private void showAll(Player p) {
        for (NPC n : npcsSupplier.get()) {
            if (n != null) n.showTo(p);
        }
    }

    private void hideAll(Player p) {
        for (NPC n : npcsSupplier.get()) {
            if (n != null) n.hideFrom(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        ChannelInjector.inject(p);
        Bukkit.getScheduler().runTaskLater(Core.inst(), () -> showAll(p), 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        hideAll(p);                // clears viewers for ALL current NPCs
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