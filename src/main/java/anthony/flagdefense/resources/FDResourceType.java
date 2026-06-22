package anthony.flagdefense.resources;

import org.bukkit.Material;

public enum FDResourceType {
	IRON(Material.IRON_INGOT),
	GOLD(Material.GOLD_INGOT),
	EMERALD(Material.EMERALD),
	DIAMOND(Material.DIAMOND);

	private final Material material;

	FDResourceType(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	public static FDResourceType fromMaterial(Material material) {
		for (FDResourceType type : values()) {
			if (type.material == material) return type;
		}
		return null;
	}
}
