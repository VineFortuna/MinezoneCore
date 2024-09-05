package anthony.SuperCraftBrawl.Game;

public enum GameType {
	CLASSIC("Classic", 5), FRENZY("Frenzy", 100), DUEL("Duel", 2);
	
	private String name;
	private int maxPlayers;
	
	private GameType(String name, int maxPlayers) {
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
