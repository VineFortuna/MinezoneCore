package anthony.SuperCraftBrawl;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.Arrays;
import java.util.List;

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

	//killercreepr
	public static ItemStack create(Material mat){ return new ItemStack(mat); }
	public static ItemStack create(Material mat, String name){
		ItemStack item = create(mat);
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return item;
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack create(Material mat, String name, List<String> lore){
		ItemStack item = create(mat, name);
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return item;
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack create(Material mat, String name, List<String> lore, boolean hideFlags){
		ItemStack item = create(mat, name, lore);
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return item;
		if(hideFlags) meta.addItemFlags(ItemFlag.values());
		else meta.removeItemFlags(ItemFlag.values());
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack create(Material mat, String name, List<String> lore, boolean hideFlags, boolean isGlowing){
		ItemStack item = create(mat, name, lore, hideFlags);
		return setGlowing(item, isGlowing);
	}

	public static ItemStack setGlowing(ItemStack item, boolean glowing){
		if(item == null) return null;
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return item;
		if(glowing){
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}else{
			meta.removeEnchant(Enchantment.DURABILITY);
			meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack setLore(ItemStack item, List<String> lore){
		if(item == null) return null;
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return item;
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack setColor(ItemStack item, Color color){
		if(isAirOrNull(item)) return item;
		ItemMeta meta = item.getItemMeta();
		if(meta instanceof LeatherArmorMeta){
			LeatherArmorMeta m = (LeatherArmorMeta) meta;
			m.setColor(color);
		}else if(meta instanceof FireworkEffectMeta){
			FireworkEffectMeta m = (FireworkEffectMeta) meta;
			m.setEffect(FireworkEffect.builder().withColor(color).build());
		}
		item.setItemMeta(meta);
		return item;
	}

	public static boolean isAirOrNull(ItemStack item){ return item == null || item.getType() == Material.AIR; }

	public static boolean isType(ItemStack item, Material mat){
		return !isAirOrNull(item) && item.getType() == mat;
	}
}
