package anthony.SuperCraftBrawl;

import java.util.Arrays;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHelper {
	public static ItemStack setDetails(ItemStack item, String name, String...lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}
	
	public static ItemStack addEnchant(ItemStack item, Enchantment type, int level) {
		item.addUnsafeEnchantment(type, level);
		return item;
	}
}
