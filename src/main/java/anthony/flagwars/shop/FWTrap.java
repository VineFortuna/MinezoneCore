package anthony.flagwars.shop;

public enum FWTrap {
	MINING_FATIGUE("Mining Fatigue Trap", 1),
	MLG("MLG Trap", 1),
	SLOWNESS_BLINDNESS("Slowness & Blindness Trap", 1),
	CAGE("Cage Trap", 2),
	DECOY_FLAG("Decoy Flag Trap", 2);

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

	/** Takes effect the moment it is bought, so it never goes in the trap queue and doesn't wait for an enemy. */
	public boolean isInstant() {
		return this == DECOY_FLAG;
	}

	/** Plain text for the shop to color gray. Use \n to start a new line. */
	public String getDescription() {
		switch (this) {
			case MINING_FATIGUE:      return "Gives enemies Mining Fatigue III\nfor 10 seconds.";
			case MLG:                 return "Launches enemies into the air with\na water bucket to land with.";
			case SLOWNESS_BLINDNESS:  return "Slows and blinds enemies\nfor 6 seconds.";
			case CAGE:                return "Traps the enemy in iron bars for\n4 seconds. Breaking out takes time.";
			case DECOY_FLAG:          return "Right away, hides your flag nearby\nand leaves a decoy in its place. An\nenemy who grabs the decoy is launched\nback, then your flag returns home.";
			default: return "";
		}
	}
}
