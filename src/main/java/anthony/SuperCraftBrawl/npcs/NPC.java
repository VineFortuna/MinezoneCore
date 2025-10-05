package anthony.SuperCraftBrawl.npcs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class NPC {
    private final UUID uuid = UUID.randomUUID();
    private final String name;
    private final Location baseLoc;
    private final GameProfile profile;
    private final int entityId;
    private final Consumer<Player> onRightClick;

    private final Set<UUID> viewers = Collections.synchronizedSet(new HashSet<>());
    private BukkitRunnable lookTask;

    public NPC(String name, Location loc, String skinValue, String skinSig, Consumer<Player> onRightClick) {
        this.name = name.length() > 16 ? name.substring(0, 16) : name;
        this.baseLoc = loc.clone();
        this.profile = new GameProfile(uuid, this.name);
        if (skinValue != null && skinSig != null) {
            this.profile.getProperties().put("textures", new Property("textures", skinValue, skinSig));
        }
        this.entityId = new Random().nextInt(Integer.MAX_VALUE);
        this.onRightClick = onRightClick;
    }

    public int getEntityId() { return entityId; }
    public Location getLocation() { return baseLoc.clone(); }
    public Consumer<Player> getClickAction() { return onRightClick; }

    /** Show to one player (packet-only fake player) */
    public void showTo(Player p) {
        if (!p.getWorld().equals(baseLoc.getWorld())) return;
        if (!viewers.add(p.getUniqueId())) return;

        // Create fake EntityPlayer (server-side only)
        WorldServer world = ((CraftPlayer) p).getHandle().getWorld();
        MinecraftServer srv = ((CraftPlayer) p).getHandle().server;
        PlayerInteractManager pim = new PlayerInteractManager(world);
        EntityPlayer ep = new EntityPlayer(srv, world, profile, pim);
        ep.setLocation(baseLoc.getX(), baseLoc.getY(), baseLoc.getZ(), baseLoc.getYaw(), baseLoc.getPitch());

        // Add to tab (required for 1.8 client to accept spawn)
        PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep);

        // Spawn named entity
        PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(ep);
        try {
            Field a = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("a"); // entityId
            a.setAccessible(true);
            a.setInt(spawn, entityId);
        } catch (Exception ignored) {}

        // Initial head alignment
        byte yaw = toPackedByte(baseLoc.getYaw());
        PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation();
        try {
            setInt(head, "a", entityId);
            setByte(head, "b", yaw);
        } catch (Exception ignored) {}

        // Remove from tab shortly (classic trick)
        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep);

        send(p, add);
        send(p, spawn);
        send(p, head);
        Bukkit.getScheduler().runTaskLater(NPCPlugin.getInstance(), () -> send(p, remove), 10L);

        // Register for click routing
        NPCRegistry.register(this);

        // Start per-viewer head tracking
        ensureLookTask();
    }

    /** Hide from one player */
    public void hideFrom(Player p) {
        if (!viewers.remove(p.getUniqueId())) return;
        send(p, new PacketPlayOutEntityDestroy(entityId));
        if (viewers.isEmpty() && lookTask != null) { lookTask.cancel(); lookTask = null; }
    }

    public void showToAll() { baseLoc.getWorld().getPlayers().forEach(this::showTo); }
    public void hideFromAll() { new HashSet<>(viewers).forEach(id -> {
        Player p = Bukkit.getPlayer(id); if (p != null) hideFrom(p);
    });}

    private void ensureLookTask() {
        if (lookTask != null) return;
        lookTask = new BukkitRunnable() {
            @Override public void run() {
                if (viewers.isEmpty()) { cancel(); lookTask = null; return; }
                for (UUID id : new HashSet<>(viewers)) {
                    Player p = Bukkit.getPlayer(id);
                    if (p == null || !p.isOnline() || !p.getWorld().equals(baseLoc.getWorld())) continue;
                    if (p.getLocation().distanceSquared(baseLoc) > 14*14) continue;

                    float[] yp = lookAt(baseLoc, p.getEyeLocation());
                    byte yaw = toPackedByte(yp[0]);
                    byte pitch = toPackedByte(yp[1]);

                    PacketPlayOutEntity.PacketPlayOutEntityLook body =
                            new PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, yaw, pitch, true);
                    PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation();
                    try { setInt(head, "a", entityId); setByte(head, "b", yaw); } catch (Exception ignored) {}

                    send(p, body);
                    send(p, head);
                }
            }
        };
        lookTask.runTaskTimer(NPCPlugin.getInstance(), 2L, 2L);
    }

    // === click routing ===
    void handleRightClick(Player clicker) {
        if (onRightClick != null) onRightClick.accept(clicker);
    }

    // === helpers ===
    private static void send(Player p, Packet<?> pkt) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(pkt);
    }
    private static byte toPackedByte(float deg) { return (byte)(int)(deg * 256.0F / 360.0F); }
    private static float[] lookAt(Location src, Location dst) {
        double dx = dst.getX() - src.getX();
        double dy = dst.getY() - (src.getY() + 1.62);
        double dz = dst.getZ() - src.getZ();
        double distXZ = Math.sqrt(dx*dx + dz*dz);
        float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float)Math.toDegrees(-Math.atan2(dy, distXZ));
        return new float[]{yaw, pitch};
    }
    private static void setInt(Object o, String f, int v) throws Exception { Field x=o.getClass().getDeclaredField(f); x.setAccessible(true); x.setInt(o,v); }
    private static void setByte(Object o, String f, byte v) throws Exception { Field x=o.getClass().getDeclaredField(f); x.setAccessible(true); x.setByte(o,v); }
}