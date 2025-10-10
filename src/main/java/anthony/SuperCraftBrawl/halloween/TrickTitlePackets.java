package anthony.SuperCraftBrawl.halloween;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
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
 * - Multiple titles (key -> text + yOffset)
 * - Exactly ONE active title per player
 * - Title shows to OTHER players (not the owner)
 * - Lobby-only by world name
 */
public final class TrickTitlePackets implements Listener {

    // ----- Title definition -----
    public static final class TitleDef {
        public final String text;    // already colorized
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

    // Player -> current active stand packets (shown to others)
    private final Map<UUID, StandData> active = new HashMap<>();

    // Player -> preferred active title key (even if not currently visible)
    private final Map<UUID, String> pref = new HashMap<>();

    // Used to avoid rapid double-processing after world changes
    private final Map<UUID, Long> lastWorldChange = new HashMap<>();

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
        // After owner updates, refresh all viewers in world
        for (Player viewer : p.getWorld().getPlayers()) {
            if (viewer != p) refreshForViewer(viewer);
        }
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
        // also refresh viewers so they destroy this owner’s stand
        for (Player viewer : p.getWorld().getPlayers()) {
            if (viewer != p) refreshForViewer(viewer);
        }
    }

    public boolean isEnabled(Player p) { return active.containsKey(p.getUniqueId()); }
    public boolean isEnabled(Player p, String key) {
        StandData sd = active.get(p.getUniqueId());
        return (sd != null && sd.key.equalsIgnoreCase(String.valueOf(key)));
    }
    public String getActiveKey(Player p) { return pref.get(p.getUniqueId()); }

    // ===== Events =====

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        final Player p = e.getPlayer();
        if (e.getFrom() != null && e.getTo() != null && e.getFrom().getWorld() != e.getTo().getWorld()) {
            return; // handled in onChangedWorld
        }
        String key = pref.get(p.getUniqueId());
        if (key == null || !isInLobby(p)) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!p.isOnline() || !isInLobby(p)) return;
            refreshOwnerForAllViewers(p);
        });
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        lastWorldChange.put(p.getUniqueId(), System.currentTimeMillis());

        hideFor(p);

        String key = pref.get(p.getUniqueId());
        if (key != null && isInLobby(p)) {
            TitleDef def = registry.get(key);
            if (def != null) showFor(p, def, key);
        }

        // 3s later, do a HARD refresh for all viewers in this world
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player viewer : p.getWorld().getPlayers()) {
                refreshForViewerHard(viewer);
            }
        }, 60L);
    }

    @EventHandler public void onQuit(PlayerQuitEvent e)  { hideFor(e.getPlayer()); pref.remove(e.getPlayer().getUniqueId()); }
    @EventHandler public void onDeath(PlayerDeathEvent e){ hideFor(e.getEntity()); }

    // Send active titles to the NEW viewer (exclude self-view)
    @EventHandler public void onJoin(PlayerJoinEvent e) {
        // 3s later, HARD refresh for this viewer (vanilla-safe)
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshForViewerHard(e.getPlayer()), 60L);
    }

    // Viewer-side refresh hooks
    @EventHandler
    public void onViewerWorldChange(PlayerChangedWorldEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshForViewerHard(e.getPlayer()), 60L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onViewerTeleport(PlayerTeleportEvent e) {
        final Player viewer = e.getPlayer();
        Long last = lastWorldChange.get(viewer.getUniqueId());
        if (last != null && (System.currentTimeMillis() - last) < 300) return;
        Bukkit.getScheduler().runTask(plugin, () -> refreshForViewer(viewer));
    }

    // ===== Refresh logic =====

    // Soft refresh (no destroy): fine most of the time
    private void refreshForViewer(Player viewer) {
        if (!viewer.isOnline()) return;

        if (!isInLobby(viewer)) {
            for (StandData sd : active.values()) {
                send(viewer, new PacketPlayOutEntityDestroy(new int[]{ sd.entityId }));
            }
            return;
        }

        World vw = viewer.getWorld();
        for (Map.Entry<UUID, StandData> entry : active.entrySet()) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner == null || !owner.isOnline()) continue;
            if (owner == viewer) continue;
            if (owner.getWorld() != vw) continue;

            // vanilla-safe order (no destroy)
            sendSpawnAndAttach(viewer, owner, entry.getValue());
        }
    }

    // HARD refresh: destroy first, then re-spawn/attach (fixes vanilla dropping passengers)
    private void refreshForViewerHard(Player viewer) {
        if (!viewer.isOnline()) return;

        if (!isInLobby(viewer)) {
            for (StandData sd : active.values()) {
                send(viewer, new PacketPlayOutEntityDestroy(new int[]{ sd.entityId }));
            }
            return;
        }

        World vw = viewer.getWorld();
        for (Map.Entry<UUID, StandData> entry : active.entrySet()) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner == null || !owner.isOnline()) continue;
            if (owner == viewer) continue;
            if (owner.getWorld() != vw) continue;

            sendHardRefresh(viewer, owner, entry.getValue());
        }
    }

    private void refreshOwnerForAllViewers(Player owner) {
        StandData sd = active.get(owner.getUniqueId());
        if (sd == null) return;
        for (Player viewer : owner.getWorld().getPlayers()) {
            if (viewer == owner || !viewer.isOnline()) continue;
            // soft refresh here (owner moved/teleported recently)
            sendSpawnAndAttach(viewer, owner, sd);
        }
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

        for (Player viewer : owner.getWorld().getPlayers()) {
            if (!viewer.isOnline() || viewer == owner) continue;
            sendSpawnAndAttach(viewer, owner, sd);
        }
    }

    private void hideFor(Player owner) {
        StandData sd = active.remove(owner.getUniqueId());
        if (sd == null) return;
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(new int[]{ sd.entityId });
        for (Player p : Bukkit.getOnlinePlayers()) send(p, destroy);
    }

    /** Soft pipeline: spawn → metadata → (pre)teleport → attach (attach LAST) */
    private void sendSpawnAndAttach(Player viewer, Player owner, StandData sd) {
        TitleDef def = registry.get(sd.key);
        double yOff = (def != null ? def.yOffset : 1.10D);

        sd.nms.setLocation(owner.getLocation().getX(),
                           owner.getLocation().getY() + yOff,
                           owner.getLocation().getZ(), 0f, 0f);

        send(viewer, new PacketPlayOutSpawnEntityLiving(sd.nms));
        send(viewer, new PacketPlayOutEntityMetadata(sd.entityId, sd.nms.getDataWatcher(), true));
        send(viewer, makeTeleport(sd)); // pre-attach teleport to lock position
        send(viewer, new PacketPlayOutAttachEntity(0, sd.nms, ((CraftPlayer) owner).getHandle())); // attach LAST

        // tiny re-attach 1 tick later for vanilla edge cases
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!viewer.isOnline() || !owner.isOnline()) return;
            send(viewer, new PacketPlayOutAttachEntity(0, sd.nms, ((CraftPlayer) owner).getHandle()));
        }, 1L);
    }

    /** HARD pipeline: destroy → spawn → metadata → teleport → attach (attach LAST) */
    private void sendHardRefresh(Player viewer, Player owner, StandData sd) {
        // 0) Destroy first (even if client doesn't know it yet, it's harmless)
        send(viewer, new PacketPlayOutEntityDestroy(new int[]{ sd.entityId }));

        // 1..4) Re-spawn pipeline (same as soft, but after destroy)
        sendSpawnAndAttach(viewer, owner, sd);
    }

    private PacketPlayOutEntityTeleport makeTeleport(StandData sd) {
        return new PacketPlayOutEntityTeleport(
                sd.entityId,
                (int) Math.floor(sd.nms.locX * 32.0D),
                (int) Math.floor(sd.nms.locY * 32.0D),
                (int) Math.floor(sd.nms.locZ * 32.0D),
                (byte) 0,
                (byte) 0,
                false
        );
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

                    // soft re-attach is enough most of the time
                    send(viewer, new PacketPlayOutAttachEntity(0, sd.nms, ((CraftPlayer) owner).getHandle()));
                }
            }
        }, 20L, 20L).getTaskId();
    }

    // ===== helpers =====
    private static void send(Player to, Packet<?> packet) {
        ((CraftPlayer) to).getHandle().playerConnection.sendPacket(packet);
    }
    private static void setMarkerSafe(EntityArmorStand stand, boolean marker) {
        try {
            Method m = EntityArmorStand.class.getMethod("setMarker", boolean.class);
            m.invoke(stand, marker);
        } catch (Throwable ignored) {}
    }
}