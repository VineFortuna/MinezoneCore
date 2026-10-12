package anthony.flagwars.shop;

public enum FWTrap {
	MINING_FATIGUE("Mining Fatigue Trap", 1),
	MLG("MLG Trap", 1),
	SLOWNESS_BLINDNESS("Slowness & Blindness Trap", 1);

	private final String displayName;
	private final int diamondCost;

	FWTrap(String displayName, int diamondCost) {
		this.displayName = displayName;
		this.diamondCost = diamondCost;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getDiamondCost() {
		return diamondCost;
	}

	public String getDescription() {
		switch (this) {
			case MINING_FATIGUE:      return "&7Gives enemies Mining Fatigue III for 10s.";
			case MLG:                 return "&7Launches enemies into the air. MLG or die!";
			case SLOWNESS_BLINDNESS:  return "&7Blinds and slows enemies for 6s.";
			default: return "";
		}
	}
}
