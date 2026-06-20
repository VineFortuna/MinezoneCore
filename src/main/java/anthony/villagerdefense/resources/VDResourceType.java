package anthony.villagerdefense.resources;

import org.bukkit.Material;

public enum VDResourceType {
	IRON(Material.IRON_INGOT),
	GOLD(Material.GOLD_INGOT),
	EMERALD(Material.EMERALD),
	DIAMOND(Material.DIAMOND);

	private final Material material;

	VDResourceType(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	public static VDResourceType fromMaterial(Material material) {
		for (VDResourceType type : values()) {
			if (type.material == material) return type;
		}
		return null;
	}
}
