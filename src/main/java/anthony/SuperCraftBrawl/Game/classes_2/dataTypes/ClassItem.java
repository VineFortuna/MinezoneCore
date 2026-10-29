package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import anthony.util.ItemHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;

public record ClassItem(
    Material item,
    String name,
    List<String> lore, // TODO: Needs to use format characters currently
    List<Pair<ClassItemEnchantment, Integer>> enchantments,

    EnumSet<ItemFlag> flags,
    Boolean hideFlags,
    Boolean isGlowing
) {
   public ItemStack createItemStack() {
       ItemStack itemStack = ItemHelper.create(item, name, lore, hideFlags, isGlowing);

       for (int i = 0; i <= enchantments.size(); i++) {
           ItemHelper.addEnchant(itemStack,
                   enchantments.get(i).getLeft().getEnchantment(),
                   enchantments.get(i).getRight());
       }

       if (!hideFlags) {
           ItemHelper.setFlags(itemStack, flags.toArray(new ItemFlag[0]));
       }

       return itemStack;
    }
}
