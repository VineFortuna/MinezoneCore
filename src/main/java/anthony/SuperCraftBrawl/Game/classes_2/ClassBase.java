package anthony.SuperCraftBrawl.Game.classes_2;

import anthony.SuperCraftBrawl.Game.classes_2.dataTypes.ClassData;
import anthony.util.ItemHelper;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ClassBase {
    private static ClassData internalData;
    private static Player player;

    private void fetchDataFromConfig() {
        internalData = ClassDataManager.loadCurrentClassConfig();
    }

    private void assignArmour() {
        ItemStack helmet = internalData.armour().helmet().fold(
                ItemHelper::createSkullBlock,
                ItemHelper::createSkullTexture
        );

        ItemStack chestplate = internalData.armour().chestplate().fold(
                ItemHelper::create,
                ItemHelper::createSkullTexture
        );

        ItemStack leggings = internalData.armour().leggings().fold(
                ItemHelper::createSkullBlock,
                ItemHelper::createSkullTexture
        );

        ItemStack boots = internalData.armour().boots().fold(
                ItemHelper::createSkullBlock,
                ItemHelper::createSkullTexture
        );

        player.getEquipment().setHelmet(helmet);
        player.getEquipment().setChestplate(chestplate);
        player.getEquipment().setLeggings(leggings);
        player.getEquipment().setBoots(boots);
    }

    private void assignWeapon() {

    }

    ClassBase(Player player) {
        /* Get data from the config file and load it into a member variable */
        fetchDataFromConfig();

        /* Create and assign armour */
        assignArmour();

        /* Create and assign weapon */


        /* Create and assign additional items */

        /* ... */
    }

}
