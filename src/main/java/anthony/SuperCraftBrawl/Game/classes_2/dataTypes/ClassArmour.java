package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import io.vavr.control.Either;
import org.bukkit.Material;

public record ClassArmour(
    Either<Material, String> helmet,
    Either<Material, String> chestplate,
    Either<Material, String> leggings,
    Either<Material, String> boots,
    int protectionLevel
) {}
