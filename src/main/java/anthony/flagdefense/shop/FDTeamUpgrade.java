package anthony.flagdefense.shop;

import anthony.flagdefense.resources.FDResourceType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Hypixel Bedwars-style team-wide upgrades, bought from the Upgrades tab of
 * the shop (see FDShopGUI) instead of a separate NPC. Tiers must be bought
 * in order; effects are applied by FDTeamUpgradeManager.
 */
public enum FDTeamUpgrade {
	FORGE("Forge", 4),
	SHARPENED_SWORDS("Sharpened Swords", 3),
	REINFORCED_ARMOR("Reinforced Armor", 4),
	HEAL_POOL("Heal Pool", 1);

	private final String displayName;
	private final int maxTier;

	FDTeamUpgrade(String displayName, int maxTier) {
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
	public Map<FDResourceType, Integer> costForTier(int nextTier) {
		if (nextTier > maxTier) return null;

		switch (this) {
			case FORGE:
				switch (nextTier) {
					case 2: return cost(FDResourceType.IRON, 40, FDResourceType.GOLD, 20);
					case 3: return cost(FDResourceType.DIAMOND, 8);
					case 4: return cost(FDResourceType.DIAMOND, 16);
					default: return null;
				}
			case SHARPENED_SWORDS:
				switch (nextTier) {
					case 1: return cost(FDResourceType.DIAMOND, 4);
					case 2: return cost(FDResourceType.DIAMOND, 8);
					case 3: return cost(FDResourceType.DIAMOND, 12);
					default: return null;
				}
			case REINFORCED_ARMOR:
				switch (nextTier) {
					case 1: return cost(FDResourceType.GOLD, 16);
					case 2: return cost(FDResourceType.GOLD, 24);
					case 3: return cost(FDResourceType.DIAMOND, 8);
					case 4: return cost(FDResourceType.DIAMOND, 16);
					default: return null;
				}
			case HEAL_POOL:
				return nextTier == 1 ? cost(FDResourceType.IRON, 40, FDResourceType.GOLD, 20) : null;
			default:
				return null;
		}
	}

	private static Map<FDResourceType, Integer> cost(FDResourceType type, int amount) {
		Map<FDResourceType, Integer> map = new EnumMap<>(FDResourceType.class);
		map.put(type, amount);
		return map;
	}

	private static Map<FDResourceType, Integer> cost(FDResourceType type1, int amount1, FDResourceType type2, int amount2) {
		Map<FDResourceType, Integer> map = new EnumMap<>(FDResourceType.class);
		map.put(type1, amount1);
		map.put(type2, amount2);
		return map;
	}
}
