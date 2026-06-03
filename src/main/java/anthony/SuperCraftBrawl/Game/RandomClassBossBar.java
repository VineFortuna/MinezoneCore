package anthony.SuperCraftBrawl.Game;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RandomClassBossBar {

    private static final Map<UUID, EntityWither> bars = new HashMap<>();

    public static void show(Player player, String message, double progress) {
        if (player == null || !player.isOnline()) return;

        progress = Math.max(0.01D, Math.min(1.0D, progress));

        EntityWither wither = bars.get(player.getUniqueId());

        if (wither == null) {
            wither = createWither(player, message, progress);
            bars.put(player.getUniqueId(), wither);

            sendPacket(player, new PacketPlayOutSpawnEntityLiving(wither));
            sendPacket(player, new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true));
            return;
        }

        Location location = getBossBarLocation(player);

        wither.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        wither.setCustomName(ChatColor.translateAlternateColorCodes('&', message));
        wither.setInvisible(true);
        wither.getDataWatcher().watch(0, (byte) 0x20);
        wither.r(0);
        wither.setHealth((float) (300.0D * progress));

        sendPacket(player, new PacketPlayOutEntityTeleport(wither));
        sendPacket(player, new PacketPlayOutEntityMetadata(wither.getId(), wither.getDataWatcher(), true));
    }

    public static void remove(Player player) {
        if (player == null) return;

        EntityWither wither = bars.remove(player.getUniqueId());

        if (wither == null) return;
        if (!player.isOnline()) return;

        sendPacket(player, new PacketPlayOutEntityDestroy(wither.getId()));
    }

    private static EntityWither createWither(Player player, String message, double progress) {
        EntityWither wither = new EntityWither(((CraftWorld) player.getWorld()).getHandle());
        Location location = getBossBarLocation(player);

        wither.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        wither.setCustomName(ChatColor.translateAlternateColorCodes('&', message));
        wither.setCustomNameVisible(false);

        // Makes the fake wither invisible without using potion effects.
        wither.setInvisible(true);

        // Force invisible entity metadata flag.
        // DataWatcher index 0 = entity flags.
        // 0x20 = invisible.
        wither.getDataWatcher().watch(0, (byte) 0x20);

        // Prevents the wither armor/invulnerable effect.
        wither.r(0);

        wither.setHealth((float) (300.0D * progress));

        return wither;
    }

    private static Location getBossBarLocation(Player player) {
        Location location = player.getEyeLocation().clone();

        return location
                .add(location.getDirection().normalize().multiply(30))
                .add(0, -20, 0);
    }

    private static void sendPacket(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((net.minecraft.server.v1_8_R3.Packet<?>) packet);
    }
}