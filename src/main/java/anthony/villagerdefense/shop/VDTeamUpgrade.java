package anthony.villagerdefense.shop;

import anthony.villagerdefense.resources.VDResourceType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Hypixel Bedwars-style team-wide upgrades, bought from the Upgrades tab of
 * the shop (see VDShopGUI) instead of a separate NPC. Tiers must be bought
 * in order; effects are applied by VDTeamUpgradeManager.
 */
public enum VDTeamUpgrade {
	FORGE("Forge", 4),
	SHARPENED_SWORDS("Sharpened Swords", 3),
	REINFORCED_ARMOR("Reinforced Armor", 4),
	HEAL_POOL("Heal Pool", 1);

	private final String displayName;
	private final int maxTier;

	VDTeamUpgrade(String displayName, int maxTier) {
		this.displayName = displayName;
		this.maxTier = maxTier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getMaxTier() {
		return maxTier;
	}

	/** Tier every team starts the match at - Forge starts at 1 (base speed), everything else at 0 (not bought). */
	public int getBaseTier() {
		return this == FORGE ? 1 : 0;
	}

	/** Resource cost to go from (nextTier - 1) to nextTier. Null once nextTier is past the max. */
	public Map<VDResourceType, Integer> costForTier(int nextTier) {
		if (nextTier > maxTier) return null;

		switch (this) {
			case FORGE:
				switch (nextTier) {
					case 2: return cost(VDResourceType.IRON, 40, VDResourceType.GOLD, 20);
					case 3: return cost(VDResourceType.DIAMOND, 8);
					case 4: return cost(VDResourceType.DIAMOND, 16);
					default: return null;
				}
			case SHARPENED_SWORDS:
				switch (nextTier) {
					case 1: return cost(VDResourceType.DIAMOND, 4);
					case 2: return cost(VDResourceType.DIAMOND, 8);
					case 3: return cost(VDResourceType.DIAMOND, 12);
					default: return null;
				}
			case REINFORCED_ARMOR:
				switch (nextTier) {
					case 1: return cost(VDResourceType.GOLD, 16);
					case 2: return cost(VDResourceType.GOLD, 24);
					case 3: return cost(VDResourceType.DIAMOND, 8);
					case 4: return cost(VDResourceType.DIAMOND, 16);
					default: return null;
				}
			case HEAL_POOL:
				return nextTier == 1 ? cost(VDResourceType.IRON, 40, VDResourceType.GOLD, 20) : null;
			default:
				return null;
		}
	}

	private static Map<VDResourceType, Integer> cost(VDResourceType type, int amount) {
		Map<VDResourceType, Integer> map = new EnumMap<>(VDResourceType.class);
		map.put(type, amount);
		return map;
	}

	private static Map<VDResourceType, Integer> cost(VDResourceType type1, int amount1, VDResourceType type2, int amount2) {
		Map<VDResourceType, Integer> map = new EnumMap<>(VDResourceType.class);
		map.put(type1, amount1);
		map.put(type2, amount2);
		return map;
	}
}
