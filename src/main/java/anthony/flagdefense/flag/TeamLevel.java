package anthony.flagdefense.flag;

import anthony.flagdefense.resources.FDResourceType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tier 1 is the starting level every team begins with. Tiers 2-3 are paid
 * upgrades that gate shop items and base-defense mechanics per the
 * FlagDefense design doc. Bought from the shop's Upgrades tab (see
 * FDTeamUpgradeManager#upgradeTeamLevel) since there's no longer an
 * objective entity to right-click.
 */
public enum TeamLevel {
	LEVEL_1(1, null),
	LEVEL_2(2, buildCost(20, 10, 0, 0)),
	LEVEL_3(3, buildCost(30, 20, 5, 0));

	private final int tier;
	private final Map<FDResourceType, Integer> upgradeCost;

	TeamLevel(int tier, Map<FDResourceType, Integer> upgradeCost) {
		this.tier = tier;
		this.upgradeCost = upgradeCost;
	}

	private static Map<FDResourceType, Integer> buildCost(int iron, int gold, int emerald, int diamond) {
		Map<FDResourceType, Integer> cost = new EnumMap<>(FDResourceType.class);
		if (iron > 0) cost.put(FDResourceType.IRON, iron);
		if (gold > 0) cost.put(FDResourceType.GOLD, gold);
		if (emerald > 0) cost.put(FDResourceType.EMERALD, emerald);
		if (diamond > 0) cost.put(FDResourceType.DIAMOND, diamond);
		return cost;
	}

	public int getTier() {
		return tier;
	}

	/** Null for LEVEL_1, since it's the free starting tier. */
	public Map<FDResourceType, Integer> getUpgradeCost() {
		return upgradeCost;
	}

	public TeamLevel next() {
		switch (this) {
			case LEVEL_1: return LEVEL_2;
			case LEVEL_2: return LEVEL_3;
			default: return null;
		}
	}

	public boolean isMaxLevel() {
		return this == LEVEL_3;
	}

	// Feature gates, per the user's Level 2/3 unlock list:
	public boolean hasGrapplingHook() { return tier >= 2; }
	public boolean hasThrowableTnt() { return tier >= 2; }
	public boolean hasBetterArmor() { return tier >= 2; }
	public boolean hasZombieGuard() { return tier >= 2; }
	public boolean hasSnareTrap() { return tier >= 3; }
	public boolean hasBarricade() { return tier >= 3; }
	public boolean hasDecoyFlag() { return tier >= 3; }
	public boolean hasRepairGolem() { return tier >= 3; }
	public boolean hasVillageBell() { return tier >= 3; }
	public boolean hasTeleportBow() { return tier >= 3; }
}
