package anthony.SuperCraftBrawl.halloween;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public final class BasketItemUtil {

	private BasketItemUtil() {
		
	}

	/** base64 = value for textures property (NOT the URL). */
	public static ItemStack customHead(String base64Texture, String name, List<String> lore) {
		// 1.8 player head item = SKULL_ITEM with durability 3
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		ItemMeta im = skull.getItemMeta();
		if (!(im instanceof SkullMeta))
			return skull;

		SkullMeta meta = (SkullMeta) im;

		try {
			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
			profile.getProperties().put("textures", new Property("textures", base64Texture));
			Field f = meta.getClass().getDeclaredField("profile");
			f.setAccessible(true);
			f.set(meta, profile);
		} catch (Exception ignored) {
		}

		if (name != null)
			meta.setDisplayName(name);
		if (lore != null && !lore.isEmpty())
			meta.setLore(lore);
		skull.setItemMeta(meta);
		return skull;
	}

	/** Helper if you only have the textures.minecraft.net URL. */
	public static String base64FromTextureUrl(String url) {
		String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
		return Base64.getEncoder().encodeToString(json.getBytes());
	}
}
