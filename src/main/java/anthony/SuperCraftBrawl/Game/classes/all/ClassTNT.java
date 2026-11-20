package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.classes.ClassBase;
import anthony.SuperCraftBrawl.Game.classes.dataTypes.ClassMetaData;
import anthony.SuperCraftBrawl.Game.classes.dataTypes.ClassCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@ClassMetaData(
        iconBlock = Material.TNT,
        iconHead = "",
        masteryHead = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQzNzUzY2QxZjRjNDkyM2ZlOWM3ZmZiNWZjZTExNGYzYWI5ODBjNzU2NDQ2NDYxZDNiNDczMzIyZGE4ZDE0YiJ9fX0==",
        title = "§c§lT§r§lN§r§c§lT§r",
        description = "Blow up your enemies with TNT!",
        category = ClassCategory.TOKEN,
        tokenCount = 0,
        uniqueIdentifier = 2
)

public class ClassTNT extends ClassBase {

    public ClassTNT(Player player) {
        super(player);
    }

}
