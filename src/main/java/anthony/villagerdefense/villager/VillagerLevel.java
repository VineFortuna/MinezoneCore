package anthony.villagerdefense.villager;

import anthony.villagerdefense.resources.VDResourceType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tier 1 is the starting level every team begins with. Tiers 2-3 are paid
 * upgrades that gate shop items and base-defense mechanics per the
 * VillagerDefense design doc.
 */
public enum VillagerLevel {
	LEVEL_1(1, null),
	LEVEL_2(2, buildCost(20, 10, 0, 0)),
	LEVEL_3(3, buildCost(30, 20, 5, 0));

	private final int tier;
	private final Map<VDResourceType, Integer> upgradeCost;

	VillagerLevel(int tier, Map<VDResourceType, Integer> upgradeCost) {
		this.tier = tier;
		this.upgradeCost = upgradeCost;
	}

	private static Map<VDResourceType, Integer> buildCost(int iron, int gold, int emerald, int diamond) {
		Map<VDResourceType, Integer> cost = new EnumMap<>(VDResourceType.class);
		if (iron > 0) cost.put(VDResourceType.IRON, iron);
		if (gold > 0) cost.put(VDResourceType.GOLD, gold);
		if (emerald > 0) cost.put(VDResourceType.EMERALD, emerald);
		if (diamond > 0) cost.put(VDResourceType.DIAMOND, diamond);
		return cost;
	}

	public int getTier() {
		return tier;
	}

	/** Null for LEVEL_1, since it's the free starting tier. */
	public Map<VDResourceType, Integer> getUpgradeCost() {
		return upgradeCost;
	}

	public VillagerLevel next() {
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
	public boolean hasDecoyVillager() { return tier >= 3; }
	public boolean hasRepairGolem() { return tier >= 3; }
	public boolean hasVillageBell() { return tier >= 3; }
	public boolean hasTeleportBow() { return tier >= 3; }
}
