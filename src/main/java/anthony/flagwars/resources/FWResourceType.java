package anthony.flagwars.resources;

import org.bukkit.Material;

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

	/** Title-case name for chat/lore display, e.g. "Iron", "Diamond". */
	public String getDisplayName() {
		return name().charAt(0) + name().substring(1).toLowerCase();
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
	 * Shared "Cost: ..." lore line for both the regular shop tabs and the Upgrades tab, so the
	 * two no longer drift apart in formatting. The "Cost:" label itself stays light gray (&amp;7)
	 * except when the whole cost is iron, which already renders light gray via getColorCode() -
	 * darkened to &amp;8 there so the label doesn't blend into the amount.
	 */
	public static String formatCost(Map<FWResourceType, Integer> cost) {
		StringBuilder builder = new StringBuilder("&rCost: ");

		boolean first = true;
		for (Map.Entry<FWResourceType, Integer> entry : cost.entrySet()) {
			if (!first) builder.append("&r, ");
			builder.append(entry.getKey().getColorCode()).append(entry.getValue()).append(' ').append(entry.getKey().getDisplayName());
			first = false;
		}
		return builder.toString();
	}
}
