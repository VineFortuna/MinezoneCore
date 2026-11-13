package anthony.SuperCraftBrawl.cosmetics;

import org.bukkit.Location;

/**
 * Implemented by death effect cosmetics that draw their own animation instead of the classic ring
 * of dropped items. These play for anyone who unlocked them (the classic effects also need the
 * scb.deathParticles permission).
 */
public interface DeathEffectAnimation {

    /** Called on the main thread when the owner is eliminated. @param where where they died */
    void play(Location where);
}
