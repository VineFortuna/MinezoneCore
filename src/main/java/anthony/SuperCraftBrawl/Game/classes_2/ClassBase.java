package anthony.SuperCraftBrawl.Game.classes_2;

import anthony.SuperCraftBrawl.Game.classes_2.dataTypes.ClassData;
import org.bukkit.entity.Player;

public abstract class ClassBase {
    private static ClassData internalData;
    private static Player player;

    private void fetchDataFromConfig() {
        internalData = ClassDataManager.loadCurrentClassConfig();
    }

    ClassBase(Player player) {
        /* Get data from the config file and load it into a member variable */
        fetchDataFromConfig();

        /* Create and assign armour */

        /* Create and assign weapon */

        /* Create and assign additional items */

        /* ... */
    }

}
