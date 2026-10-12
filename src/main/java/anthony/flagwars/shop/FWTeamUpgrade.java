package anthony.flagwars.shop;

import anthony.flagwars.resources.FWResourceType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Hypixel Bedwars-style team-wide upgrades, bought from the Upgrades tab of
 * the shop (see FWShopGUI) instead of a separate NPC. Tiers must be bought
 * in order; effects are applied by FWTeamUpgradeManager.
 */
public enum FWTeamUpgrade {
	RESOURCE_RUSH("Resource Rush", 4),
	SHARPENED_SWORDS("Sharpened Swords", 1),
	REINFORCED_ARMOR("Reinforced Armor", 4),
	HEAL_POOL("Heal Pool", 1),
	FEATHER_FALLING("Feather Falling", 1);

	private final String displayName;
	private final int maxTier;

	FWTeamUpgrade(String displayName, int maxTier) {
		this.displayName = displayName;
		this.maxTier = maxTier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getMaxTier() {
		return maxTier;
	}

	/** Tier every team starts the match at - 0 for all upgrades (nothing unlocked by default). */
	public int getBaseTier() {
		return 0;
	}

	/** Resource cost to go from (nextTier - 1) to nextTier. Null once nextTier is past the max. */
	public Map<FWResourceType, Integer> costForTier(int nextTier) {
		if (nextTier > maxTier) return null;

		switch (this) {
			case RESOURCE_RUSH:
				switch (nextTier) {
					case 1: return cost(FWResourceType.DIAMOND, 2);
					case 2: return cost(FWResourceType.DIAMOND, 4);
					case 3: return cost(FWResourceType.DIAMOND, 8);
					case 4: return cost(FWResourceType.DIAMOND, 16);
					default: return null;
				}
			case SHARPENED_SWORDS:
				// Capped at Sharpness I (maxTier 1) - it was way too strong with the old Sharpness III ceiling.
				return nextTier == 1 ? cost(FWResourceType.DIAMOND, 4) : null;
			case REINFORCED_ARMOR:
				switch (nextTier) {
					case 1: return cost(FWResourceType.DIAMOND, 2);
					case 2: return cost(FWResourceType.DIAMOND, 4);
					case 3: return cost(FWResourceType.DIAMOND, 8);
					case 4: return cost(FWResourceType.DIAMOND, 16);
					default: return null;
				}
			case HEAL_POOL:
				return nextTier == 1 ? cost(FWResourceType.DIAMOND, 1) : null;
			case FEATHER_FALLING:
				return nextTier == 1 ? cost(FWResourceType.DIAMOND, 2) : null;
			default:
				return null;
		}
	}

	private static Map<FWResourceType, Integer> cost(FWResourceType type, int amount) {
		Map<FWResourceType, Integer> map = new EnumMap<>(FWResourceType.class);
		map.put(type, amount);
		return map;
	}

	private static Map<FWResourceType, Integer> cost(FWResourceType type1, int amount1, FWResourceType type2, int amount2) {
		Map<FWResourceType, Integer> map = new EnumMap<>(FWResourceType.class);
		map.put(type1, amount1);
		map.put(type2, amount2);
		return map;
	}
}
