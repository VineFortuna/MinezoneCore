package anthony.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.Dye;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.potion.PotionEffectType.*;

public class ItemHelper {
    // TODO: add more descriptive names, ie, 'setDetailsWithAddon'.
    // TODO: 'addition' parameter is stupid, remove it and fix the usages.
	private static ItemStack setDetailsImpl(ItemStack item, String name, List<String> lore, List<String> addition) {
		List<String> coloredLore = new ArrayList<>();
		if (lore != null) {
            coloredLore = lore.stream().map(ChatColorHelper::color).collect(Collectors.toList());
		}

		if (addition != null) {
			if (addition.get(0) != null || addition.size() >= 2) {
				coloredLore.addAll(addition);
            }
		}

		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(ChatColorHelper.color(name));
		itemMeta.setLore(coloredLore);
		item.setItemMeta(itemMeta);

		return item;
	}

	public static ItemStack setDetails(ItemStack item, String name, String... lore) {
		return setDetailsImpl(item, name, Arrays.asList(lore), null);
	}

	public static ItemStack setDetails(ItemStack item, String name, List<String> lore) {
		return setDetailsImpl(item, name, lore, null);
	}

	public static ItemStack setDetails(ItemStack item, String name, List<String> lore, String... addon) {
		return setDetailsImpl(item, name, lore, Arrays.asList(addon));
	}

	public static ItemStack setDetails(ItemStack item, String name, List<String> lore, List<String> addon) {
		return setDetailsImpl(item, name, lore, addon);
	}

	public static ItemStack addEnchant(ItemStack item, Enchantment type, int level) {
		item.addUnsafeEnchantment(type, level);
		return item;
	}

    // TODO: Replace with more descriptive names, ie 'createWithGlowing', or 'createWithFlags', etc. And add vaargs for lores.
    private static ItemStack createImpl(Material mat, String name, int amount, List<String> lore, boolean hideFlags, boolean isGlowing) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (name != null)
                meta.setDisplayName(ChatColorHelper.color(name));

            if (lore != null)
                meta.setLore(ChatColorHelper.colorList(lore));

            if (hideFlags) {
                meta.addItemFlags(ItemFlag.values());
            } else {
                meta.removeItemFlags(ItemFlag.values());
            }

            item.setItemMeta(meta);
        }

        item.setAmount(amount);
        setGlowing(item, isGlowing);

        return item;
    }

    public static ItemStack create(Material mat, String name, int amount, List<String> lore, boolean hideFlags, boolean isGlowing) {
        return createImpl(mat, name, amount, lore, hideFlags, isGlowing);
    }

    public static ItemStack create(Material mat) {
		return createImpl(mat, null, 1, null, false, false);
	}

	public static ItemStack create(Material mat, String name) {
        return createImpl(mat, name, 1, null, false, false);
	}

	public static ItemStack create(Material mat, String name, int amount) {
        return createImpl(mat, name, amount, null, false, false);
	}

	public static ItemStack create(Material mat, String name, List<String> lore) {
        return createImpl(mat, name, 1, lore, false, false);
	}

	public static ItemStack create(Material mat, String name, String loreLine1) {
        return createImpl(mat, name, 1, List.of(loreLine1), false, false);
	}

    // Who the hell is using this overload? Fixme:
	public static ItemStack create(Material mat, String name, String loreLine1, String loreLine2) {
        return createImpl(mat, name, 1, List.of(loreLine1, loreLine2), false, false);
	}

	public static ItemStack create(Material mat, String name, List<String> lore, int amount) {
        return createImpl(mat, name, amount, lore, false, false);
	}

	public static ItemStack create(Material mat, String name, List<String> lore, boolean hideFlags) {
        return createImpl(mat, name, 1, lore, hideFlags, false);
	}

	public static ItemStack create(Material mat, String name, List<String> lore, boolean hideFlags, boolean isGlowing) {
        return createImpl(mat, name, 1, lore, hideFlags, isGlowing);
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

    private static ItemStack createSkullHeadImpl(SkullType skullType, int amount, String displayName, List<String> lore) {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, amount, (short) skullType.ordinal());
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        if (displayName != null) {
            skullMeta.setDisplayName(ChatColorHelper.color(displayName));
        }

        if (lore != null) {
            skullMeta.setLore(ChatColorHelper.colorList(lore));
        }

        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

	public static ItemStack createSkullHead(int amount, SkullType skullType) {
		return createSkullHeadImpl(skullType, amount, null, null);
	}

	public static ItemStack createSkullHead(int amount, SkullType skullType, String displayName) {
        return createSkullHeadImpl(skullType, amount, displayName, null);
	}

	public static ItemStack createSkullHead(int amount, SkullType skullType, String displayName, List<String> lore) {
        return createSkullHeadImpl(skullType, amount, displayName, lore);
	}

    private static ItemStack createSkullHeadPlayerImpl(String owner, int amount, String displayName, List<String> lore) {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, amount, (short) SkullType.PLAYER.ordinal());
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwner(owner);

        if (displayName != null) {
            skullMeta.setDisplayName(ChatColorHelper.color(displayName));
        }

        if (lore != null) {
            skullMeta.setLore(ChatColorHelper.colorList(lore));
        }

        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

	public static ItemStack createSkullHeadPlayer(int amount, String owner) {
        return createSkullHeadPlayerImpl(owner, amount, null, null);
    }

	public static ItemStack createSkullHeadPlayer(int amount, String owner, String displayName) {
        return createSkullHeadPlayerImpl(owner, amount, displayName, null);
	}

	public static ItemStack createSkullHeadPlayer(int amount, String owner, String displayName, List<String> lore) {
        return createSkullHeadPlayerImpl(owner, amount, displayName, lore);
	}

	public static ItemStack createSkullBlock(Material block) {
		return new ItemStack(block, 1, (short) SkullType.PLAYER.ordinal());
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

	public static ItemStack createPotionItem(PotionEffect effect, boolean splashable, boolean overrideSameEffect) {
		ItemStack potionItem = new ItemStack(Material.POTION, 1);
		PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();

		potionMeta.addCustomEffect(effect, overrideSameEffect);
		potionItem.setItemMeta(potionMeta);

		Potion pot = new Potion(getPotionTypeFromEffect(effect));
		if (splashable) pot.setSplash(true);
		pot.apply(potionItem);


		return potionItem;
	}

	private static PotionType getPotionTypeFromEffect(PotionEffect effect) {
        PotionEffectType type = effect.getType();
        if (type.equals(JUMP)) {return PotionType.JUMP;
        } else if (type.equals(SPEED)) {return PotionType.SPEED;
        } else if (type.equals(REGENERATION)) {return PotionType.REGEN;
        } else if (type.equals(FIRE_RESISTANCE)) {return PotionType.FIRE_RESISTANCE;
        } else if (type.equals(INCREASE_DAMAGE)) {return PotionType.STRENGTH;
        } else if (type.equals(DAMAGE_RESISTANCE)) {return PotionType.NIGHT_VISION;
		}
        return PotionType.WATER; // Fallback to water if no matching type is found
    }

	public static String getRomanNumeral(int number) {
		switch (number) {
			case 1: return "I";
			case 2: return "II";
			case 3: return "III";
			case 4: return "IV";
			case 5: return "V";
			case 6: return "VI";
			case 7: return "VII";
			case 8: return "VIII";
			case 9: return "IX";
			case 10: return "X";
			default: return "" + number; // Fallback for numbers > 10
		}
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

    public static ItemStack setFlag(ItemStack item, ItemFlag flag) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(flag);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack setFlags(ItemStack item, ItemFlag... flag) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(flag);
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

	public static void addRightClickMessage(ItemStack item) {
		ItemMeta itemMeta = item.getItemMeta();

		if (itemMeta.hasDisplayName()) {
			String name = itemMeta.getDisplayName();
			itemMeta.setDisplayName(ChatColorHelper.color( name + " &7(Right Click)"));
			item.setItemMeta(itemMeta);
		}
	}

	public static boolean isAirOrNull(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}

	public static boolean isType(ItemStack item, Material mat) {
		return !isAirOrNull(item) && item.getType() == mat;
	}

	public static ItemStack getNextPageItem() {
		return ItemHelper.createSkullTexture(
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19",
				"&7Next Page"
		);
	}
	public static ItemStack getPreviousPageItem() {
		return ItemHelper.createSkullTexture(
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==",
				"&7Previous Page"
		);
	}
	public static ItemStack getGoBackItem() {
		return ItemHelper.setDetails(new ItemStack(Material.ARROW), "&7Go back");
	}
	public static ItemStack getGlassFiller() {
		return ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " ");
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

	/**
	 * Format a double for display.
	 * Converts the double to an integer if it's a whole number.
	 *
	 * @param value The double to be formatted.
	 * @return A formatted string representation of the double value.
	 */
	public static String formatDouble(double value) {
		return (value % 1 == 0) ? String.valueOf((int) value) : String.valueOf(value);
	}
}
