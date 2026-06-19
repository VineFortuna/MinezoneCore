package anthony.villagerdefense.resources;

import org.bukkit.Material;

public enum VDResourceType {
	IRON,
	GOLD,
	EMERALD,
	DIAMOND;

	public static VDResourceType fromMaterial(Material material) {
		switch (material) {
			case IRON_INGOT: return IRON;
			case GOLD_INGOT: return GOLD;
			case EMERALD: return EMERALD;
			case DIAMOND: return DIAMOND;
			default: return null;
		}
	}
}
