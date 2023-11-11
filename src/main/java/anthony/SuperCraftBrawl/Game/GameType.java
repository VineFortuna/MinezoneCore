package anthony.SuperCraftBrawl.Game;

public enum GameType {
	CLASSIC(5), FRENZY(100), DUEL(2);
	
	private int maxPlayers;
	
	private GameType(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	public int getMaxPlayers() {
		return maxPlayers;
	}
}
