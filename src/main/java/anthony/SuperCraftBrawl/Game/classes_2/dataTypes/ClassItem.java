package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import anthony.util.ItemHelper;
import anthony.util.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;

import static anthony.SuperCraftBrawl.Game.classes_2.dataTypes.ClassItemFlags.GLOWING;
import static anthony.SuperCraftBrawl.Game.classes_2.dataTypes.ClassItemFlags.HIDEFLAGS;

public record ClassItem(
    Material item,
    String name,
    List<String> lore, // TODO: Needs to use format characters currently
    List<Pair<ClassItemEnchantment, Integer>> enchantments,

    EnumSet<ItemFlag> flags,
    EnumSet<ClassItemFlags> classItemFlags
) {
   public ItemStack createItemStack() {
       ItemStack itemStack = ItemHelper.create(item, name, lore, classItemFlags.contains(HIDEFLAGS), classItemFlags.contains(GLOWING));

       for (int i = 0; i <= enchantments.size(); i++) {
           ItemHelper.addEnchant(itemStack,
                   enchantments.get(i).getLeft().getEnchantment(),
                   enchantments.get(i).getRight());
       }

       if (!classItemFlags.contains(HIDEFLAGS)) {
           ItemHelper.setFlags(itemStack, flags.toArray(new ItemFlag[0]));
       }

       return itemStack;
    }
}
