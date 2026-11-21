package anthony.SuperCraftBrawl.halloween;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.command.CommandExecutor;

/**
 * The one switch for all Halloween content. While it is false nothing from Halloween can be played or used, but
 * none of the code is removed, so next October this is the only thing to flip back to true.
 * <p>
 * What it switches:
 * <ul>
 * <li>the Freddy and GrimReaper classes, which are vaulted (out of the class menu, random picks and /class)
 * <li>the basket hunt: HalloweenHuntManager and CandyAuraManager aren't started, so the baskets in the lobby
 * can't be clicked, and /tricktitle and /treatsadmin only say Halloween isn't running
 * <li>the six cosmetics only the hunt unlocks: the Trick-or-Treater and Freddy Fazbear titles, the Candy Aura trail,
 * the Freddy outfit, the Pumpkin Pie death effect and the Ritual win effect. Their ids stay in the players'
 * saved data, so they come back with the switch
 * </ul>
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
