package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import anthony.SuperCraftBrawl.Timer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.EnumSet;
import java.util.List;

sealed interface ClassHeadArmour permits ClassHeadArmour.HeadBlock, ClassHeadArmour.HeadUrl {
    record HeadBlock(Material material) implements ClassHeadArmour {};
    record   HeadUrl(String textureUrl) implements ClassHeadArmour {};
}

sealed interface ClassBodyArmour permits ClassBodyArmour.MineralType, ClassBodyArmour.LeatherColor {
    record  MineralType(Material material) implements ClassBodyArmour {};
    record LeatherColor(String hexColor)   implements ClassBodyArmour {};
}

record ClassArmour(
    ClassHeadArmour helmet,
    ClassBodyArmour chestplate,
    ClassBodyArmour leggings,
    ClassBodyArmour boots,
    int protectionLevel
) {}

record ClassItem(
    Material item,
    String lore, // TODO: Needs to use format characters currently
    List<Pair<ClassItemEnchantment, Integer>> enchantments,

    EnumSet<ItemFlag> flags,
    Boolean hideFlags
) {}


public record ClassData(
    ClassArmour armour,
    List<Pair<Timer, Integer>> timers,
    ClassItem weapon,
    List<ClassItem> items
) {}