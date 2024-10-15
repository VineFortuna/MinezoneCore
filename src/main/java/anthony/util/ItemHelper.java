package anthony.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.Dye;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemHelper {
	public static ItemStack setDetails(ItemStack item, String name, String...lore) {
		List<String> coloredLore = new ArrayList<>();

		if (lore != null) {
			for (String line : lore) {
				coloredLore.add(ChatColorHelper.color(line));
			}
		}

		return setDetails(item, name, coloredLore);
	}

	public static ItemStack setDetails(ItemStack item, String name, List<String> lore) {
		return setDetails(item, name, lore, (String) null);
	}

	public static ItemStack setDetails(ItemStack item, String name, List<String> lore, String... addon) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(ChatColorHelper.color(name));
		if(addon != null){
			if(addon.length >= 2 || addon[0] != null) lore.addAll(Arrays.asList(addon));
		}
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}

	public static ItemStack addEnchant(ItemStack item, Enchantment type, int level) {
		item.addUnsafeEnchantment(type, level);
		return item;
	}

	// killercreepr
	public static ItemStack create(Material mat) {
		return new ItemStack(mat);
	}

	public static ItemStack create(Material mat, String name) {
		ItemStack item = create(mat);
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;

		meta.setDisplayName(ChatColorHelper.color(name));
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack create(Material mat, String name, int amount) {
		ItemStack item = create(mat, name);

		item.setAmount(amount);
		return item;
	}

	public static ItemStack create(Material mat, String name, List<String> lore) {
		ItemStack item = create(mat, name);
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;
		meta.setLore(ChatColorHelper.colorList(lore));
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack create(Material mat, String name, String loreLine1) {
		ItemStack item = create(mat, name);
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;
		List<String> loreList = new ArrayList<>();

		loreList.add(ChatColorHelper.color(loreLine1));

		meta.setLore(loreList);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack create(Material mat, String name, String loreLine1, String loreLine2) {
		ItemStack item = create(mat, name);
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;
		List<String> loreList = new ArrayList<>();

		loreList.add(ChatColorHelper.color(loreLine1));
		loreList.add(ChatColorHelper.color(loreLine2));

		meta.setLore(loreList);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack create(Material mat, String name, List<String> lore, int amount) {
		ItemStack item = create(mat, name, lore);

		item.setAmount(amount);

		return item;
	}

	public static ItemStack create(Material mat, String name, List<String> lore, boolean hideFlags) {
		ItemStack item = create(mat, name, lore);
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;
		if (hideFlags)
			meta.addItemFlags(ItemFlag.values());
		else
			meta.removeItemFlags(ItemFlag.values());
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack create(Material mat, String name, List<String> lore, boolean hideFlags, boolean isGlowing) {
		ItemStack item = create(mat, name, lore, hideFlags);
		return setGlowing(item, isGlowing);
	}

	// Natowski
//	public static ItemStack createStainedClay() {
//		return new ItemStack(Material.STAINED_CLAY, (short) id)
//	}


	public static ItemStack createMonsterEgg(EntityType entityType, int amount) {
		return new ItemStack(Material.MONSTER_EGG, amount, entityType.getTypeId());
	}

	public static ItemStack createMonsterEgg(EntityType entityType, int amount, String displayName) {
		ItemStack itemStack = createMonsterEgg(entityType, amount);
		ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setDisplayName(ChatColorHelper.color(displayName));
		itemStack.setItemMeta(itemMeta);

		return itemStack;
	}

	public static ItemStack createSkullHead(int amount, SkullType skullType) {
		return new ItemStack(Material.SKULL_ITEM, 1, (short) skullType.ordinal());
	}

	public static ItemStack createSkullHead(int amount, SkullType skullType, String displayName) {
		ItemStack itemStack = createSkullHead(amount, skullType);
		SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

		skullMeta.setDisplayName(ChatColorHelper.color(displayName));
		itemStack.setItemMeta(skullMeta);

		return itemStack;
	}

	public static ItemStack createSkullHead(int amount, SkullType skullType, String displayName, List<String> lore) {
		ItemStack itemStack = createSkullHead(amount, skullType, displayName);
		SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

		skullMeta.setDisplayName(ChatColorHelper.color(displayName));
		skullMeta.setLore(ChatColorHelper.colorList(lore));
		itemStack.setItemMeta(skullMeta);

		return itemStack;
	}

	public static ItemStack createSkullHeadPlayer(int amount, String owner) {
		ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, amount, (short) SkullType.PLAYER.ordinal());
		SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

		skullMeta.setOwner(owner);
		itemStack.setItemMeta(skullMeta);

		return itemStack;
	}

	public static ItemStack createSkullHeadPlayer(int amount, String owner, String displayName) {
		ItemStack itemStack = createSkullHeadPlayer(amount, owner);
		SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

		skullMeta.setDisplayName(ChatColorHelper.color(displayName));
		itemStack.setItemMeta(skullMeta);

		return itemStack;
	}

	public static ItemStack createSkullHeadPlayer(int amount, String owner, String displayName, List<String> lore) {
		ItemStack itemStack = createSkullHeadPlayer(amount, owner, displayName);
		SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

		skullMeta.setLore(ChatColorHelper.colorList(lore));
		itemStack.setItemMeta(skullMeta);

		return itemStack;
	}
	
	public static ItemStack createSkullTexture(String textureUrl) {
		ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", textureUrl));
		Field profileField = null;
		try {
			profileField = skullMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(skullMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		itemStack.setItemMeta(skullMeta);
		return itemStack;
	}
	
	public static ItemStack createSkullTexture(String textureUrl, String displayName) {
		return setDetails(createSkullTexture(textureUrl), displayName);
	}
	public static ItemStack createSkullTexture(String textureUrl, String displayName, String... lore) {
		return setDetails(createSkullTexture(textureUrl), displayName, lore);
	}

	public static ItemStack createDye(DyeColor color) {
		Dye dye = new Dye();
		dye.setColor(color);

		return dye.toItemStack();
	}

	public static ItemStack createDye(DyeColor color, int amount) {
		ItemStack dyeItem = createDye(color);

		dyeItem.setAmount(amount);

		return dyeItem;
	}

	public static ItemStack createDye(DyeColor color, int amount, String displayName) {
		ItemStack dyeItem = createDye(color, amount);
		ItemMeta dyeItemMeta = dyeItem.getItemMeta();

		dyeItemMeta.setDisplayName(ChatColorHelper.color(displayName));
		dyeItem.setItemMeta(dyeItemMeta);

		return dyeItem;
	}

	public static ItemStack createDye(DyeColor color, int amount, String displayName, List<String> lore) {
		ItemStack dyeItem = createDye(color, amount, displayName);
		ItemMeta dyeItemMeta = dyeItem.getItemMeta();

		dyeItemMeta.setLore(ChatColorHelper.colorList(lore));
		dyeItem.setItemMeta(dyeItemMeta);

		return dyeItem;
	}

	public static ItemStack createColoredArmor(Material armorPiece, Color color) {
		ItemStack leatherArmor = new ItemStack(armorPiece);
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherArmor.getItemMeta();

		leatherArmorMeta.setColor(color);
		leatherArmor.setItemMeta(leatherArmorMeta);

		return leatherArmor;
	}

	public static ItemStack createColoredArmor(Material armorPiece, Color color, String displayName) {
		ItemStack leatherArmor = createColoredArmor(armorPiece, color);
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherArmor.getItemMeta();

		leatherArmorMeta.setDisplayName(ChatColorHelper.color(displayName));
		leatherArmor.setItemMeta(leatherArmorMeta);

		return leatherArmor;
	}

	public static ItemStack createColoredArmor(Material armorPiece, Color color, String displayName, List<String> lore) {
		ItemStack leatherArmor = createColoredArmor(armorPiece, color, displayName);
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherArmor.getItemMeta();

		leatherArmorMeta.setLore(ChatColorHelper.colorList(lore));
		leatherArmor.setItemMeta(leatherArmorMeta);

		return leatherArmor;
	}

	public static ItemStack createColoredArmor(Material armorPiece, String hexCode) {
		ItemStack leatherArmor = new ItemStack(armorPiece);
		LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) leatherArmor.getItemMeta();

		if (hexCode != null) {
			Color color = hexToColor(hexCode);

			if (color != null) {
				leatherArmorMeta.setColor(color);
				leatherArmor.setItemMeta(leatherArmorMeta);
			}
		}

		return leatherArmor;
	}
	
	public static ItemStack createColoredArmor(Material armorPiece, String hexCode, String displayName) {
		ItemStack leatherArmor = new ItemStack(armorPiece);
		
		if (hexCode != null) {
			Color color = hexToColor(hexCode);
			
			if (color != null) {
				leatherArmor = createColoredArmor(armorPiece, color, displayName);
			}
		}
		
		return leatherArmor;
	}

	public static ItemStack createPotionItem(PotionType potionType, int amplifier, int durationSeconds, boolean splashable, boolean particles, boolean ambientParticles) {
		ItemStack potionItem = new ItemStack(Material.POTION);

		Potion potion = new Potion(potionType);
		potion.setSplash(splashable);

		PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();

		potionMeta.addCustomEffect(new PotionEffect(potion.getType().getEffectType(), durationSeconds * 20, amplifier, ambientParticles, particles), true);
		potionItem.setItemMeta(potionMeta);
		potion.apply(potionItem);

		return potionItem;
	}

	public static ItemStack setUnbreakable(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(true);
		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack setBreakable(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.spigot().setUnbreakable(false);
		item.setItemMeta(meta);

		return item;
	}

	public static ItemStack setGlowing(ItemStack item, boolean glowing) {
		if (item == null)
			return null;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;
		if (glowing) {
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		} else {
			meta.removeEnchant(Enchantment.DURABILITY);
			meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack setHideFlags(ItemStack item, boolean hide) {
		if (item == null)
			return null;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;
		if (hide) {
			meta.addItemFlags(ItemFlag.values());
		} else {
			meta.removeItemFlags(ItemFlag.values());
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack setLore(ItemStack item, List<String> lore) {
		if (item == null)
			return null;
		ItemMeta meta = item.getItemMeta();
		if (meta == null)
			return item;
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack setColor(ItemStack item, Color color) {
		if (isAirOrNull(item))
			return item;
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof LeatherArmorMeta) {
			LeatherArmorMeta m = (LeatherArmorMeta) meta;
			m.setColor(color);
		} else if (meta instanceof FireworkEffectMeta) {
			FireworkEffectMeta m = (FireworkEffectMeta) meta;
			m.setEffect(FireworkEffect.builder().withColor(color).build());
		}
		item.setItemMeta(meta);
		return item;
	}

	public static boolean isAirOrNull(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}

	public static boolean isType(ItemStack item, Material mat) {
		return !isAirOrNull(item) && item.getType() == mat;
	}

	public enum StainedClayColor {
		WHITE(0),
		ORANGE(1),
		MAGENTA(2),
		LIGHT_BLUE(3),
		YELLOW(4),
		LIME(5),
		PINK(6),
		GRAY(7),
		LIGHT_GRAY(8),
		CYAN(9),
		PURPLE(10),
		BLUE(11),
		BROWN(12),
		GREEN(13),
		RED(14),
		BLACK(15);

		private final int colorCode;

		StainedClayColor(int colorCode) {
			this.colorCode = colorCode;
		}

		public int getColorCode() {
			return colorCode;
		}
	}

	// Convert hex code to Color
	private static Color hexToColor(String hexCode) {
		try {
			// Remove # if present
			if (hexCode.startsWith("#")) {
				hexCode = hexCode.substring(1);
			}

			// Parse hexadecimal to integer
			int rgb = Integer.parseInt(hexCode, 16);

			// Extract individual color components
			int r = (rgb >> 16) & 0xFF;
			int g = (rgb >> 8) & 0xFF;
			int b = rgb & 0xFF;

			return Color.fromRGB(r, g, b);
		} catch (NumberFormatException e) {
			// Handle invalid hex code
			e.printStackTrace();
			return null;
		}
	}
}
