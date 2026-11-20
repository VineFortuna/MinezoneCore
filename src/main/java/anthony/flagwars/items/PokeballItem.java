package anthony.flagwars.items;

import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Zombie / Skeleton Pokeballs: the same items (names and descriptions) Super Craft Bros hands out as loot drops.
 * Right-click one to throw it, and the mob is spawned where it lands, fighting for the thrower's team.
 *
 * Throwing and the mobs' behavior live in FWPokeballManager.
 */
public final class PokeballItem {

	public enum Type {
		ZOMBIE(EntityType.ZOMBIE, "&2&lZOMBIE POKEBALL", "Spawns a Zombie in diamond gear\nthat fights for your team."),
		SKELETON(EntityType.SKELETON, "&7&lSKELETON POKEBALL", "Spawns a Skeleton with a Punch\nbow that fights for your team.");

		private final EntityType entityType;
		private final String coloredName;
		private final String description;

		Type(EntityType entityType, String coloredName, String description) {
			this.entityType = entityType;
			this.coloredName = coloredName;
			this.description = description;
		}

		public EntityType getEntityType() {
			return entityType;
		}

		/** The name with no colors, e.g. "ZOMBIE POKEBALL" (what the shop lists it as). */
		public String getName() {
			return ChatColor.stripColor(ChatColorHelper.color(coloredName));
		}

		public String getDescription() {
			return description;
		}
	}

	private PokeballItem() {
	}

	public static ItemStack create(Type type) {
		ItemStack item = ItemHelper.createMonsterEgg(type.entityType, 1);

		List<String> lore = new ArrayList<>();
		for (String line : type.description.split("\n")) lore.add("&7" + line);
		lore.add("");
		lore.add("&7Right click to throw.");
		return ItemHelper.setDetails(item, type.coloredName, lore.toArray(new String[0]));
	}

	/** Which Pokeball this is, or null if the item isn't one. */
	public static Type typeOf(ItemStack item) {
		if (ItemHelper.isAirOrNull(item) || item.getType() != Material.MONSTER_EGG) return null;
		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.hasDisplayName()) return null;

		String name = ChatColor.stripColor(meta.getDisplayName());
		for (Type type : Type.values()) {
			if (name.equals(type.getName())) return type;
		}
		return null;
	}
}
