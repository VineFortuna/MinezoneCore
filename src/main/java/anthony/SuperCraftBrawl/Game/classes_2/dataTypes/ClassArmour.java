package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import anthony.util.ItemHelper;
import io.vavr.control.Either;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record ClassArmour(
    Either<Material, String> helmet,
    Either<Material, String> chestplate,
    Either<Material, String> leggings,
    Either<Material, String> boots,
    int protectionLevel
) {
    public List<ItemStack> createArmour() {
        ItemStack helmet     = createHelmet();
        ItemStack chestplate = createChestplate();
        ItemStack leggings   = createLeggings();
        ItemStack boots      = createBoots();

        return List.of(helmet, chestplate, leggings, boots);
    }

    private ItemStack createHelmet() {
        return helmet().fold(
                ItemHelper::createSkullBlock,
                ItemHelper::createSkullTexture
        );
    }


    private ItemStack createChestplate() {
        return chestplate().fold(
                ItemHelper::create,
                ItemHelper::createSkullTexture
        );
    }


    private ItemStack createLeggings() {
        return leggings().fold(
                ItemHelper::createSkullBlock,
                ItemHelper::createSkullTexture
        );
    }


    private ItemStack createBoots() {
        return boots().fold(
                ItemHelper::createSkullBlock,
                ItemHelper::createSkullTexture
        );
    }
}
