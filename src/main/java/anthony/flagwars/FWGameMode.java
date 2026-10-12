package anthony.flagwars;

/** Solo (1 per island) or Duos (2 per island) - chosen when a player queues up. Actual max players scales with however many teams a given map has created; see FWGameInstance#getMaxPlayers. */
public enum FWGameMode {
	SOLO("Solo", 1, 2),
	DUO("Duos", 2, 4);

	private final String label;
	private final int teamCapacity;
	private final int minPlayers;

	FWGameMode(String label, int teamCapacity, int minPlayers) {
		this.label = label;
		this.teamCapacity = teamCapacity;
		this.minPlayers = minPlayers;
	}

	public String getLabel() {
		return label;
	}

	/** How many players share one island/team in this mode. */
	public int getTeamCapacity() {
		return teamCapacity;
	}

	/** How many players must be queued before the countdown starts. */
	public int getMinPlayers() {
		return minPlayers;
	}
}
