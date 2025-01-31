package anthony.util;

import anthony.SuperCraftBrawl.Game.GameInstance;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    // Play a sound to each game player from a specific player location
    public static void playSoundToAllFromPlayerLocation(GameInstance gameInstance, Player player, Sound sound, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            gamePlayer.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    // Play a sound to each game player from a location
    public static void playSoundToAllFromLocation(GameInstance gameInstance, Location location, Sound sound, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            gamePlayer.playSound(location, sound, volume, pitch);
        }
    }

    // Play a sound to each game player on its own location
    public static void playSoundToAllAtTheirLocation(GameInstance gameInstance, Sound sound, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            gamePlayer.playSound(gamePlayer.getLocation(), sound, volume, pitch);
        }
    }

    // Play a sound to a single player
    public static void playSoundToPlayer(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    // Play the Successful Hit sound to a single player (Volume = 1; Pitch = 1)
    public static void playSuccessfulHit(Player player) {
        playSoundToPlayer(player, Sound.SUCCESSFUL_HIT, 1, 1);
    }

    // Play the Successful Hit sound to a single player
    public static void playSuccessfulHit(Player player, float volume, float pitch) {
        playSoundToPlayer(player, Sound.SUCCESSFUL_HIT, volume, pitch);
    }

    // Play the Note Bass pitched down sound to a single player
    public static void playErrorSound(Player player) {
        playSoundToPlayer(player, Sound.NOTE_BASS, 1, 0.5f);
    }

    // Play a subtle pitched down Click sound to a single player
    public static void playClickSound(Player player) {
        playSoundToPlayer(player, Sound.CLICK, 0.5f, 15);
    }
}