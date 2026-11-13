package anthony.SuperCraftBrawl.cosmetics;

import org.bukkit.entity.Player;

/** Implemented by double jump cosmetics: what plays when the player double jumps. */
public interface DoubleJumpAnimation {

    /** Called on the main thread right after the player is launched. Their feet are at player.getLocation(). */
    void play(Player player);
}
