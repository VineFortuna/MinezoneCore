package anthony.SuperCraftBrawl;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PersonalNPCPlugin implements Listener {
	
	/*private Core core;

    private static PersonalNPCPlugin instance;
    
    public PersonalNPCPlugin(Core core) {
    	this.core = core;
    	Bukkit.getServer().getPluginManager().registerEvents(this, core);
    }

    public static PersonalNPCPlugin getInstance() {
        return instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Delay a tick or two so skins/connection are fully ready
        new BukkitRunnable() {
            @Override public void run() { 
            	spawnPersonalNPC(e.getPlayer()); 
            	}
        }.runTaskLater(core, 20L);
    }

    private void spawnPersonalNPC(Player viewer) {
        // Where to place the NPC
        World bWorld = viewer.getWorld(); // or Bukkit.getWorld("worldName")
        Location loc = new Location(bWorld, 189.0, 105.0, 660.0, viewer.getLocation().getYaw(), 0f);

        // NMS setup
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) bWorld).getHandle();

        // Use the viewer's own skin/profile so it matches their skin
        GameProfile skinProfile = ((CraftPlayer) viewer).getProfile();

        // Create a "fake" player entity (not added to the world, only packet-spawned to viewer)
        EntityPlayer npc = new EntityPlayer(nmsServer, nmsWorld,
                new GameProfile(skinProfile.getId(), skinProfile.getName()),
                new PlayerInteractManager(nmsWorld));
        npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        PlayerConnection conn = ((CraftPlayer) viewer).getHandle().playerConnection;

        // 1) Add to tab list briefly so the client loads the skin
        conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));

        // 2) Spawn the named entity (only for this viewer)
        conn.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));

        // 3) Set head rotation to match yaw
        byte headYaw = (byte) ((int) (loc.getYaw() * 256.0F / 360.0F));
        conn.sendPacket(new PacketPlayOutEntityHeadRotation(npc, headYaw));

        // (Optional) remove from tab list after a short delay so they don't see it in TAB
        new BukkitRunnable() {
            @Override public void run() {
                conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
            }
        }.runTaskLater(core, 40L);

        // NOTE: If you ever want to despawn it later (e.g., on command), send:
        // conn.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }*/
}
