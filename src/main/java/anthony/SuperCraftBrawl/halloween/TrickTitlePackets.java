package anthony.SuperCraftBrawl.halloween;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Packet-only Trick-or-Treater subtitle (1.8 R3) – safe and jitter-free.
 * Titles are shown to OTHER players only (not the owner) so right-click items work.
 */
public final class TrickTitlePackets implements Listener {
    private final Plugin plugin;
    private final String lobbyWorldName;

    private final Set<UUID> enabledPref = new HashSet<>();

    private static final class StandData {
        EntityArmorStand nms; // not added to world
        int entityId;
        UUID uuid;
    }
    private final Map<UUID, StandData> active = new HashMap<>();

    private static final double OFFSET_BLOCKS = 1.10; // tweak ±0.02

    private int reattachTaskId = -1;

    public TrickTitlePackets(Plugin plugin, String lobbyWorldName) {
        this.plugin = plugin;
        this.lobbyWorldName = (lobbyWorldName == null) ? "" : lobbyWorldName;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startReattachPulse();
    }

    // --- public API ---
    public boolean toggle(Player p) { if (isEnabled(p)) { disable(p); return false; } enable(p); return true; }
    public void enable(Player p) {
        enabledPref.add(p.getUniqueId());
        if (isInLobby(p)) showFor(p);
        else p.sendMessage(ChatColor.GRAY + "(Title will appear in " + ChatColor.YELLOW + lobbyWorldName + ChatColor.GRAY + ".)");
    }
    public void disable(Player p) { enabledPref.remove(p.getUniqueId()); hideFor(p); }
    public boolean isEnabled(Player p) { return enabledPref.contains(p.getUniqueId()); }

    // --- owner flow ---
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (!isEnabled(p)) return;
        hideFor(p);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (p.isOnline() && isEnabled(p) && isInLobby(p)) showFor(p);
        });
    }

    @EventHandler public void onChangedWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        if (!isEnabled(p)) return;
        if (isInLobby(p)) showFor(p); else hideFor(p);
    }

    @EventHandler public void onQuit(PlayerQuitEvent e)  { hideFor(e.getPlayer()); }
    @EventHandler public void onDeath(PlayerDeathEvent e){ hideFor(e.getEntity()); }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        // When someone joins, send them active titles (exclude self-view)
        Player viewer = e.getPlayer();
        for (UUID id : active.keySet()) {
            Player owner = Bukkit.getPlayer(id);
            if (owner != null && owner.isOnline() && isInLobby(owner) && isEnabled(owner) && viewer != owner) {
                sendSpawnAndAttach(viewer, owner, active.get(id));
            }
        }
    }

    // --- viewer refresh (exclude owner as viewer) ---
    @EventHandler
    public void onViewerWorldChange(PlayerChangedWorldEvent e, @SuppressWarnings("unused") boolean ignore) {
        Player viewer = e.getPlayer();
        if (!isInLobby(viewer)) {
            for (StandData sd : active.values()) send(viewer, new PacketPlayOutEntityDestroy(new int[]{ sd.entityId }));
            return;
        }
        for (Map.Entry<UUID, StandData> entry : active.entrySet()) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner != null && owner.isOnline() && isEnabled(owner) && owner.getWorld() == viewer.getWorld() && viewer != owner) {
                sendSpawnAndAttach(viewer, owner, entry.getValue());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onViewerTeleport(PlayerTeleportEvent e, @SuppressWarnings("unused") boolean ignore) {
        final Player viewer = e.getPlayer();
        if (!isInLobby(viewer)) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!viewer.isOnline() || !isInLobby(viewer)) return;
            for (Map.Entry<UUID, StandData> entry : active.entrySet()) {
                Player owner = Bukkit.getPlayer(entry.getKey());
                if (owner != null && owner.isOnline() && isEnabled(owner) && owner.getWorld() == viewer.getWorld() && viewer != owner) {
                    sendSpawnAndAttach(viewer, owner, entry.getValue());
                }
            }
        });
    }

    // --- core ---
    private boolean isInLobby(Player p) {
        if (lobbyWorldName.isEmpty()) return true;
        World w = p.getWorld();
        return w != null && w.getName().equalsIgnoreCase(lobbyWorldName);
    }

    private void showFor(Player owner) {
        if (active.containsKey(owner.getUniqueId())) return;

        WorldServer nmsWorld = ((CraftWorld) owner.getWorld()).getHandle();
        EntityArmorStand stand = new EntityArmorStand(nmsWorld);
        stand.setLocation(owner.getLocation().getX(),
                          owner.getLocation().getY() + OFFSET_BLOCKS,
                          owner.getLocation().getZ(),
                          0f, 0f);
        stand.setInvisible(true);
        stand.setSmall(true);
        setMarkerSafe(stand, true);
        stand.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Trick-or-Treater");
        stand.setCustomNameVisible(true);

        StandData sd = new StandData();
        sd.nms = stand;
        sd.entityId = stand.getId();
        sd.uuid = stand.getUniqueID();
        active.put(owner.getUniqueId(), sd);

        // Show to everyone in owner's world EXCEPT the owner (prevents right-click interception)
        for (Player viewer : owner.getWorld().getPlayers()) {
            if (!viewer.isOnline() || viewer == owner) continue;
            sendSpawnAndAttach(viewer, owner, sd);
        }
    }

    private void hideFor(Player owner) {
        StandData sd = active.remove(owner.getUniqueId());
        if (sd == null) return;
        broadcastInWorld(owner.getWorld(), new PacketPlayOutEntityDestroy(new int[]{ sd.entityId }));
    }

    private void sendSpawnAndAttach(Player viewer, Player owner, StandData sd) {
        sd.nms.setLocation(owner.getLocation().getX(),
                           owner.getLocation().getY() + OFFSET_BLOCKS,
                           owner.getLocation().getZ(),
                           0f, 0f);

        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(sd.nms);
        send(viewer, spawn);

        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(sd.entityId, sd.nms.getDataWatcher(), true);
        send(viewer, meta);

        PacketPlayOutAttachEntity attach = new PacketPlayOutAttachEntity(0, sd.nms, ((CraftPlayer) owner).getHandle());
        send(viewer, attach);
    }

    // --- periodic reattach to avoid client unlink freezes (exclude owner as viewer) ---
    private void startReattachPulse() {
        if (reattachTaskId != -1) return;
        reattachTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (active.isEmpty()) return;

            final double MAX_DIST_SQ = 64 * 64;
            for (Map.Entry<UUID, StandData> entry : active.entrySet()) {
                Player owner = Bukkit.getPlayer(entry.getKey());
                StandData sd = entry.getValue();
                if (owner == null || !owner.isOnline() || sd == null) continue;

                for (Player viewer : owner.getWorld().getPlayers()) {
                    if (!viewer.isOnline() || viewer == owner) continue;
                    if (viewer.getLocation().distanceSquared(owner.getLocation()) > MAX_DIST_SQ) continue;

                    PacketPlayOutAttachEntity attach = new PacketPlayOutAttachEntity(
                            0, sd.nms, ((CraftPlayer) owner).getHandle());
                    send(viewer, attach);
                }
            }
        }, 20L, 20L).getTaskId();
    }

    // --- helpers ---
    private static void send(Player to, Packet<?> packet) {
        ((CraftPlayer) to).getHandle().playerConnection.sendPacket(packet);
    }
    private void broadcastInWorld(World world, Packet<?> packet) {
        for (Player p : world.getPlayers()) send(p, packet);
    }
    private static void setMarkerSafe(EntityArmorStand stand, boolean marker) {
        try {
            Method m = EntityArmorStand.class.getMethod("setMarker", boolean.class);
            m.invoke(stand, marker);
        } catch (Throwable ignored) {}
    }
}