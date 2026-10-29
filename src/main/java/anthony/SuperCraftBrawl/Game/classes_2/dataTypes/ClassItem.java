package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.EnumSet;
import java.util.List;

public record ClassItem(
    Material item,
    String lore, // TODO: Needs to use format characters currently
    List<Pair<ClassItemEnchantment, Integer>> enchantments,

    EnumSet<ItemFlag> flags,
    Boolean hideFlags
) {}
