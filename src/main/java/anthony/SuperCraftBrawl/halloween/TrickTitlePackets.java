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
 * Packet-only subtitle titles manager (1.8 R3).
 * - Supports registering multiple titles (key -> text + yOffset).
 * - Exactly ONE active title per player (easy to extend later).
 * - Title shows to OTHER players (not the owner) so right-click works.
 * - Lobby-only by world name.
 */
public final class TrickTitlePackets implements Listener {

    // ----- Title definition -----
    public static final class TitleDef {
        public final String text;    // already colorized (ChatColor)
        public final double yOffset; // blocks above feet
        public TitleDef(String text, double yOffset) {
            this.text = text; this.yOffset = yOffset;
        }
    }

    private static final class StandData {
        EntityArmorStand nms; // not added to world
        int entityId;
        UUID uuid;
        String key; // which title this stand represents
    }

    private final Plugin plugin;
    private final String lobbyWorldName;

    // Registry of available titles
    private final Map<String, TitleDef> registry = new LinkedHashMap<>();

    // Player -> current active stand packets
    private final Map<UUID, StandData> active = new HashMap<>();

    // Player -> preferred active title key (even if not currently visible)
    private final Map<UUID, String> pref = new HashMap<>();

    private int reattachTaskId = -1;

    public TrickTitlePackets(Plugin plugin, String lobbyWorldName) {
        this.plugin = plugin;
        this.lobbyWorldName = (lobbyWorldName == null) ? "" : lobbyWorldName;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startReattachPulse();
    }

    // ===== Registry API =====
    public void registerTitle(String key, String displayText, double yOffsetBlocks) {
        registry.put(key.toLowerCase(Locale.ROOT), new TitleDef(displayText, yOffsetBlocks));
    }
    public boolean hasTitle(String key) { return registry.containsKey(key.toLowerCase(Locale.ROOT)); }
    public Set<String> listKeys() { return Collections.unmodifiableSet(registry.keySet()); }
    public TitleDef getDef(String key) { return registry.get(key.toLowerCase(Locale.ROOT)); }

    // ===== Player API (one active per player) =====
    /** Set/switch the player's active title. Returns true if set OK. */
    public boolean setTitle(Player p, String key) {
        if (key == null) return false;
        key = key.toLowerCase(Locale.ROOT);
        TitleDef def = registry.get(key);
        if (def == null) return false;

        pref.put(p.getUniqueId(), key);

        StandData cur = active.get(p.getUniqueId());
        if (cur != null && key.equalsIgnoreCase(cur.key)) return true; // already shown

        hideFor(p); // replace current
        if (isInLobby(p)) showFor(p, def, key);
        return true;
    }

    /** Toggle a specific title key for the player. Returns enabled state after toggle. */
    public boolean toggleTitle(Player p, String key) {
        key = (key == null) ? "" : key.toLowerCase(Locale.ROOT);
        String current = pref.get(p.getUniqueId());
        if (key.equalsIgnoreCase(current)) {
            clearTitle(p);
            return false;
        } else {
            return setTitle(p, key);
        }
    }

    /** Clear any active title for the player. */
    public void clearTitle(Player p) {
        pref.remove(p.getUniqueId());
        hideFor(p);
    }

    public boolean isEnabled(Player p) { return active.containsKey(p.getUniqueId()); }
    public boolean isEnabled(Player p, String key) {
        StandData sd = active.get(p.getUniqueId());
        return (sd != null && sd.key.equalsIgnoreCase(String.valueOf(key)));
    }
    public String getActiveKey(Player p) { return pref.get(p.getUniqueId()); }

    // ===== Events (owner flow) =====
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        String key = pref.get(p.getUniqueId());
        if (key == null) return;

        hideFor(p);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!p.isOnline()) return;
            if (!isInLobby(p)) return;
            TitleDef def = registry.get(key);
            if (def != null) showFor(p, def, key);
        });
    }

    @EventHandler public void onChangedWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        String key = pref.get(p.getUniqueId());
        if (key == null) { hideFor(p); return; }
        TitleDef def = registry.get(key);
        if (def == null) { hideFor(p); return; }
        if (isInLobby(p)) showFor(p, def, key); else hideFor(p);
    }

    @EventHandler public void onQuit(PlayerQuitEvent e)  { hideFor(e.getPlayer()); pref.remove(e.getPlayer().getUniqueId()); }
    @EventHandler public void onDeath(PlayerDeathEvent e){ hideFor(e.getEntity()); }

    // Send active titles to the NEW viewer (exclude self-view)
    @EventHandler public void onJoin(PlayerJoinEvent e) {
        Player viewer = e.getPlayer();
        for (Map.Entry<UUID, StandData> entry : active.entrySet()) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner != null && owner.isOnline() && isInLobby(owner) && viewer != owner) {
                sendSpawnAndAttach(viewer, owner, entry.getValue());
            }
        }
    }

    // Re-send to viewer after their world change / teleport
    @EventHandler
    public void onViewerWorldChange(PlayerChangedWorldEvent e, @SuppressWarnings("unused") boolean ignore) {
        Player viewer = e.getPlayer();
        if (!isInLobby(viewer)) {
            for (StandData sd : active.values())
                send(viewer, new PacketPlayOutEntityDestroy(new int[]{ sd.entityId }));
            return;
        }
        for (Map.Entry<UUID, StandData> entry : active.entrySet()) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner != null && owner.isOnline() && isInLobby(owner) && viewer != owner) {
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
                if (owner != null && owner.isOnline() && viewer != owner) {
                    sendSpawnAndAttach(viewer, owner, entry.getValue());
                }
            }
        });
    }

    // ===== Core =====
    private boolean isInLobby(Player p) {
        if (lobbyWorldName.isEmpty()) return true;
        World w = p.getWorld();
        return w != null && w.getName().equalsIgnoreCase(lobbyWorldName);
    }

    private void showFor(Player owner, TitleDef def, String key) {
        WorldServer nmsWorld = ((CraftWorld) owner.getWorld()).getHandle();
        EntityArmorStand stand = new EntityArmorStand(nmsWorld);
        stand.setLocation(owner.getLocation().getX(),
                          owner.getLocation().getY() + def.yOffset,
                          owner.getLocation().getZ(), 0f, 0f);
        stand.setInvisible(true);
        stand.setSmall(true);
        setMarkerSafe(stand, true);
        stand.setCustomName(def.text);
        stand.setCustomNameVisible(true);

        StandData sd = new StandData();
        sd.nms = stand;
        sd.entityId = stand.getId();
        sd.uuid = stand.getUniqueID();
        sd.key = key;
        active.put(owner.getUniqueId(), sd);

        // Show to viewers (exclude owner)
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
        TitleDef def = registry.get(sd.key);
        double yOff = (def != null ? def.yOffset : 1.10D);

        sd.nms.setLocation(owner.getLocation().getX(),
                           owner.getLocation().getY() + yOff,
                           owner.getLocation().getZ(), 0f, 0f);

        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(sd.nms);
        send(viewer, spawn);

        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(sd.entityId, sd.nms.getDataWatcher(), true);
        send(viewer, meta);

        PacketPlayOutAttachEntity attach = new PacketPlayOutAttachEntity(0, sd.nms, ((CraftPlayer) owner).getHandle());
        send(viewer, attach);
    }

    // periodic reattach (prevents occasional client detaches)
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

                    PacketPlayOutAttachEntity attach =
                            new PacketPlayOutAttachEntity(0, sd.nms, ((CraftPlayer) owner).getHandle());
                    send(viewer, attach);
                }
            }
        }, 20L, 20L).getTaskId();
    }

    // ===== helpers =====
    private static void send(Player to, Packet<?> packet) {
        ((CraftPlayer) to).getHandle().playerConnection.sendPacket(packet);
    }
    private static void broadcastInWorld(World world, Packet<?> packet) {
        for (Player p : world.getPlayers()) send(p, packet);
    }
    private static void setMarkerSafe(EntityArmorStand stand, boolean marker) {
        try {
            Method m = EntityArmorStand.class.getMethod("setMarker", boolean.class);
            m.invoke(stand, marker);
        } catch (Throwable ignored) {}
    }
}