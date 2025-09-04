package anthony.util;

import anthony.SuperCraftBrawl.Game.GameInstance;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.Vec3D;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SoundManager {

    /**
     * Play a Spigot sound to all players from a player location.
     */
    public static void playSoundToAll(Player player, Sound sound, float volume, float pitch) {
        playSoundToAll(player, player.getLocation(), sound, volume, pitch);
    }

    /**
     * Play a Spigot sound to all players from a specific location.
     */
    public static void playSoundToAll(Player player, Location location, Sound sound, float volume, float pitch) {
        player.getWorld().playSound(location, sound, volume, pitch);
    }

    /**
     * Play a NMS sound to all players from a player location.
     */
    public static void playNMSSoundToAll(Player player, String soundPath, float volume, float pitch) {
        try {
            // Get NMS player and world
            EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            WorldServer world = (WorldServer) nmsPlayer.world;

            // Play the sound using the string path
            world.makeSound(
                    nmsPlayer,
                    soundPath,
                    volume,
                    pitch
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Play a Spigot sound to each game player at their own location.
     */
    public static void playSoundToEachPlayer(GameInstance gameInstance, Sound sound, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            gamePlayer.playSound(gamePlayer.getLocation(), sound, volume, pitch);
        }
    }

    /**
     * Play a NMS sound to each game player at their own location.
     */
    public static void playNMSSoundToEachPlayer(GameInstance gameInstance, String soundPath, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            playNMSSoundToPlayer(gamePlayer, soundPath, volume, pitch);
        }
    }

    /**
     * Play a Spigot sound to a single player.
     */
    public static void playSoundToPlayer(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Play a NMS sound to a single player.
     *
     * @param soundPath The NMS path as String of the sound.
     */
    public static void playNMSSoundToPlayer(Player player, String soundPath, float volume, float pitch) {
        try {
            // Create sound packet using the sound string path
            PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(
                    soundPath,
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ(),
                    volume,
                    pitch
            );

            // Get NMS player
            EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            // Send packet to player
            nmsPlayer.playerConnection.sendPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Play the Item Pickup sound to a single player.
     */
    public static void playItemPickup(Player player) {
        playSoundToPlayer(player, Sound.ITEM_PICKUP, 0.5f, 1.5f);
    }

    /**
     * Play the Successful Hit sound to a single player.
     */
    public static void playSuccessfulHit(Player player) {
        playSoundToPlayer(player, Sound.SUCCESSFUL_HIT, 1, 1);
    }

    /**
     * Play the Note Bass pitched down sound to a single player.
     */
    public static void playErrorSound(Player player) {
        playSoundToPlayer(player, Sound.NOTE_BASS, 1, 0.5f);
    }

    /**
     * Play a subtle pitched down Click sound to a single player.
     */
    public static void playClickSound(Player player) {
        playSoundToPlayer(player, Sound.CLICK, 0.5f, 15);
    }
}