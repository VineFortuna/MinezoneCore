package anthony.SuperCraftBrawl.halloween;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.command.CommandExecutor;

/**
 * The one switch for the playable Halloween content. While it is false nothing from Halloween can be played, but
 * none of the code is removed, so next October this is the only thing to flip back to true.
 * <p>
 * What it switches:
 * <ul>
 * <li>the Freddy and GrimReaper classes, which are vaulted (out of the class menu, random picks and /class)
 * <li>the basket hunt: HalloweenHuntManager doesn't register its listener, so the baskets in the lobby can't be
 * clicked, and /tricktitle and /treatsadmin only say Halloween isn't running
 * </ul>
 * What it does NOT switch: the six Halloween 2025 cosmetics (the Trick-or-Treater and Freddy Fazbear titles, the Candy
 * Aura trail, the Freddy outfit, the Pumpkin Pie death effect and the Ritual win effect). They were exclusive
 * rewards and stay with the players who earned them, so they are always registered, and the hunt manager is always
 * created because the saved basket progress that unlocks them is read through it. Nobody can earn new progress while
 * this is false.
 * <p>
 * The Halloween maps (HauntedMansion, WitchesBrew, TheCraftOf87) are not behind this switch: they are commented out
 * in Maps, and need uncommenting there. The Halloween Champ 2024 title is a tournament award, so it stays.
 */
public final class Halloween {

    public static final boolean ENABLED = false;

    private Halloween() {
    }

    /** What a Halloween command does while Halloween is off. */
    public static CommandExecutor offExecutor(final Core core) {
        return (sender, command, label, args) -> {
            sender.sendMessage(core.color("&c&l(!) &rHalloween isn't running right now."));
            return true;
        };
    }
}
