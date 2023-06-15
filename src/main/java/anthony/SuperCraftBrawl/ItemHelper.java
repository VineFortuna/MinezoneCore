package anthony.SuperCraftBrawl;

import java.util.Arrays;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHelper {
	public static ItemStack setDetails(ItemStack item, String name, String...lore) {
		return setDetails(item, name, Arrays.asList(lore));
	}

	public static ItemStack setDetails(ItemStack item, String name, List<String> lore) {
		return setDetails(item, name, lore, (String) null);
	}

	public static ItemStack setDetails(ItemStack item, String name, List<String> lore, String... addon) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		if(addon != null) lore.addAll(Arrays.asList(addon));
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}

	public static ItemStack addEnchant(ItemStack item, Enchantment type, int level) {
		item.addUnsafeEnchantment(type, level);
		return item;
	}
}
