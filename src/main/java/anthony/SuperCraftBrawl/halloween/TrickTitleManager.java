package anthony.SuperCraftBrawl.halloween;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Lobby-only second nametag with precise height control.
 *
 * Design:
 *  - spacer: invisible ArmorStand that rides the player (perfectly follows).
 *  - label : invisible ArmorStand with custom name. Not a passenger; we
 *            teleport it EVERY TICK to spacerLoc + OFFSET_Y (smooth + precise).
 *
 * Teleport-safe: we always remove entities BEFORE teleports.
 */
public class TrickTitleManager implements Listener {
    private final Plugin plugin;
    private final String lobbyWorldName;

    /** Players who WANT the title enabled (preference), regardless of world. */
    private final Set<UUID> enabledPref = new HashSet<>();

    /** Active pair per player. */
    private static class Pair { ArmorStand spacer, label; }
    private final Map<UUID, Pair> active = new HashMap<>();

    private int watchdogTaskId = -1;
    private int followTaskId   = -1;

    // ---- Height tuning ----
    // Tweak this value to nudge the title up/down in tiny steps.
    // +0.02 ≈ 2 cm. Positive = higher, Negative = lower.
    private static final double OFFSET_Y = 0.24; // tuned to "just above" the vanilla name

    public TrickTitleManager(Plugin plugin, String lobbyWorldName) {
        this.plugin = plugin;
        this.lobbyWorldName = lobbyWorldName == null ? "" : lobbyWorldName;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startWatchdog();
        startFollower();
    }

    private boolean isLobbyName(String world) {
        if (lobbyWorldName.isEmpty()) return true;
        return world != null && world.equalsIgnoreCase(lobbyWorldName);
    }
    private boolean isInLobby(Player p) { return isLobbyName(p.getWorld().getName()); }

    // ----- public API (use via your command) -----
    public boolean toggle(Player p) {
        if (enabledPref.contains(p.getUniqueId())) { disable(p); return false; }
        else { enable(p); return true; }
    }
    public void enable(Player p) {
        enabledPref.add(p.getUniqueId());
        if (isInLobby(p) && !active.containsKey(p.getUniqueId())) {
            active.put(p.getUniqueId(), spawnPair(p));
        } else {
            p.sendMessage(ChatColor.GRAY + "(Title will appear in " + ChatColor.YELLOW + lobbyWorldName + ChatColor.GRAY + ".)");
        }
    }
    public void disable(Player p) {
        enabledPref.remove(p.getUniqueId());
        removePair(p.getUniqueId(), true);
    }
    public boolean isEnabled(Player p) { return enabledPref.contains(p.getUniqueId()); }

    public void shutdown() {
        if (watchdogTaskId != -1) { Bukkit.getScheduler().cancelTask(watchdogTaskId); watchdogTaskId = -1; }
        if (followTaskId   != -1) { Bukkit.getScheduler().cancelTask(followTaskId);   followTaskId   = -1; }
        for (UUID id : new HashSet<>(active.keySet())) removePair(id, true);
        active.clear();
        enabledPref.clear();
    }

    // ----- create/remove -----
    private Pair spawnPair(Player p) {
        Pair pair = new Pair();

        // Spacer: small + marker, rides the player (no hitbox; exact follow)
        pair.spacer = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
        pair.spacer.setVisible(false);
        pair.spacer.setGravity(false);
        pair.spacer.setSmall(true);
        try { pair.spacer.setMarker(true); } catch (Throwable ignored) {}
        p.setPassenger(pair.spacer);

        // Label: invisible stand showing the text (NOT a passenger)
        pair.label = p.getWorld().spawn(p.getLocation(), ArmorStand.class);
        pair.label.setVisible(false);
        pair.label.setGravity(false);
        pair.label.setSmall(true);
        try { pair.label.setMarker(true); } catch (Throwable ignored) {}
        pair.label.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Trick-or-Treater");
        pair.label.setCustomNameVisible(true);

        return pair;
    }

    private void removePair(UUID id, boolean eject) {
        Pair pair = active.remove(id);
        if (pair == null) return;

        Player p = Bukkit.getPlayer(id);
        if (eject && p != null) {
            try { if (p.getPassenger() == pair.spacer) p.eject(); } catch (Throwable ignored) {}
        }
        if (pair.label  != null && !pair.label.isDead())  pair.label.remove();
        if (pair.spacer != null && !pair.spacer.isDead()) pair.spacer.remove();
    }

    // ----- tasks -----
    /** Reattach if something dismounts or world changes; remove outside lobby. */
    private void startWatchdog() {
        if (watchdogTaskId != -1) return;
        watchdogTaskId = new BukkitRunnable() {
            @Override public void run() {
                if (active.isEmpty()) return;
                for (UUID id : new HashSet<>(active.keySet())) {
                    Player p = Bukkit.getPlayer(id);
                    if (p == null || !p.isOnline()) { removePair(id, true); continue; }
                    if (!isInLobby(p) || !enabledPref.contains(id)) { removePair(id, true); continue; }

                    Pair pair = active.get(id);
                    boolean need =
                            pair == null || pair.spacer == null || pair.label == null ||
                            pair.spacer.isDead() || pair.label.isDead() ||
                            !pair.spacer.isValid() || !pair.label.isValid() ||
                            p.getPassenger() != pair.spacer ||
                            !pair.spacer.getWorld().equals(p.getWorld()) ||
                            !pair.label.getWorld().equals(p.getWorld());

                    if (need) {
                        removePair(id, true);
                        active.put(id, spawnPair(p));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L).getTaskId(); // once/sec watchdog
    }

    /** Move the label EVERY TICK to spacer position + offset (precise height). */
    private void startFollower() {
        if (followTaskId != -1) return;
        followTaskId = new BukkitRunnable() {
            @Override public void run() {
                if (active.isEmpty()) return;
                for (Map.Entry<UUID, Pair> e : active.entrySet()) {
                    Pair pair = e.getValue();
                    if (pair == null || pair.spacer == null || pair.label == null) continue;
                    if (pair.label.isDead() || pair.spacer.isDead()) continue;

                    Location base = pair.spacer.getLocation();
                    Location target = base.add(0.0, OFFSET_Y, 0.0);
                    // Only teleport when world matches (it should)
                    if (pair.label.getWorld() == target.getWorld()) {
                        pair.label.teleport(target);
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L).getTaskId(); // every tick
    }

    // ----- events -----
    @EventHandler public void onQuit(PlayerQuitEvent e) { removePair(e.getPlayer().getUniqueId(), true); }
    @EventHandler public void onDeath(PlayerDeathEvent e) { removePair(e.getEntity().getUniqueId(), true); }

    @EventHandler public void onWorldChange(PlayerChangedWorldEvent e) {
        if (!isInLobby(e.getPlayer())) { removePair(e.getPlayer().getUniqueId(), true); }
        else if (enabledPref.contains(e.getPlayer().getUniqueId()) && !active.containsKey(e.getPlayer().getUniqueId())) {
            active.put(e.getPlayer().getUniqueId(), spawnPair(e.getPlayer()));
        }
    }

    /** Remove entities BEFORE any teleport so cross-world teleports succeed. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (!active.containsKey(p.getUniqueId())) return;
        removePair(p.getUniqueId(), true);

        // Optional: auto-restore in lobby after teleport
        final boolean shouldRestore = enabledPref.contains(p.getUniqueId()) &&
                isLobbyName(e.getTo() != null ? e.getTo().getWorld().getName() : null);

        if (shouldRestore) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (p.isOnline() && isInLobby(p) && !active.containsKey(p.getUniqueId())) {
                    active.put(p.getUniqueId(), spawnPair(p));
                }
            });
        }
    }
}