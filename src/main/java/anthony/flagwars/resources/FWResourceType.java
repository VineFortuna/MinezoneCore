package anthony.flagwars.resources;

import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.Map;

public enum FWResourceType {
	IRON(Material.IRON_INGOT),
	GOLD(Material.GOLD_INGOT),
	EMERALD(Material.EMERALD),
	DIAMOND(Material.DIAMOND);

	private final Material material;

	FWResourceType(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	public static FWResourceType fromMaterial(Material material) {
		for (FWResourceType type : values()) {
			if (type.material == material) return type;
		}
		return null;
	}

	/** Chat color code (with leading &amp;) this resource's amount is rendered in on shop/upgrade lore. */
	public String getColorCode() {
		switch (this) {
			case GOLD: return "&6";
			case DIAMOND: return "&b";
			case EMERALD: return "&a";
			default: return "&7";
		}
	}

	/**
	 * Distinct pickup sound per resource, escalating with rarity - the same sounds this codebase
	 * already uses for similarly-valenced events (ORB_PICKUP on a shop purchase, NOTE_PLING on a
	 * Quick Buy toggle, LEVEL_UP on a flag capture/team upgrade) rather than inventing new ones.
	 */
	public Sound getPickupSound() {
		switch (this) {
			case GOLD: return Sound.ORB_PICKUP;
			case EMERALD: return Sound.NOTE_PLING;
			case DIAMOND: return Sound.LEVEL_UP;
			default: return Sound.ITEM_PICKUP;
		}
	}

	/**
	 * Shared "Cost: ..." lore line for both the regular shop tabs and the Upgrades tab, so the
	 * two no longer drift apart in formatting. The "Cost:" label itself stays light gray (&amp;7)
	 * except when the whole cost is iron, which already renders light gray via getColorCode() -
	 * darkened to &amp;8 there so the label doesn't blend into the amount.
	 */
	public static String formatCost(Map<FWResourceType, Integer> cost) {
		boolean ironOnly = cost.size() == 1 && cost.containsKey(IRON);
		StringBuilder builder = new StringBuilder(ironOnly ? "&8Cost: " : "&7Cost: ");

		boolean first = true;
		for (Map.Entry<FWResourceType, Integer> entry : cost.entrySet()) {
			if (!first) builder.append("&7, ");
			builder.append(entry.getKey().getColorCode()).append(entry.getValue()).append(' ').append(entry.getKey().name());
			first = false;
		}
		return builder.toString();
	}
}
