package anthony.SuperCraftBrawl.Game;

public enum GameType {
	CLASSIC("Classic", 6), FRENZY("Frenzy", 100), DUEL("Duels", 2), GUNGAME("Gun Game", 5);
	
	private String name;
	private int maxPlayers;
	
	GameType(String name, int maxPlayers) {
		this.name = name;
		this.maxPlayers = maxPlayers;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getMaxPlayers() {
		return this.maxPlayers;
	}
}
