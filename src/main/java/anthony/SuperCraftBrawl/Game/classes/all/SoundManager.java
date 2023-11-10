package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    // Play a sound to each game player from a specific player location
    public static void playSoundToAllGamePlayersFromAPlayerLocation(GameInstance gameInstance, Player player, Sound sound, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            gamePlayer.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public static void playSoundToAllGamePlayersFromALocation(GameInstance gameInstance, Location location, Sound sound, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            gamePlayer.playSound(location, sound, volume, pitch);
        }
    }

    // Play a sound to each game player on its own location
    public static void playSoundToAllGamePlayersOnEachPlayerLocation(GameInstance gameInstance, Sound sound, float volume, float pitch) {
        for (Player gamePlayer : gameInstance.players) {
            gamePlayer.playSound(gamePlayer.getLocation(), sound, volume, pitch);
        }
    }

    // Play a sound to a single player
    public static void playSoundToSinglePlayer(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    // Play the Successful Hit sound to a single player (Volume = 1; Pitch = 1)
    public static void playSoundSuccessfulHitToSinglePlayer(Player player) {
        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
    }

    // Play the Successful Hit sound to a single player
    public static void playSoundSuccessfulHitToSinglePlayer(Player player, float volume, float pitch) {
        player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, volume, pitch);
    }
}