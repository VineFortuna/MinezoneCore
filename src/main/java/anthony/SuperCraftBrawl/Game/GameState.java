package anthony.SuperCraftBrawl.Game;

public enum GameState {
	WAITING("Lobby"), STARTED("In Progress"), ENDED ("Ended")/*, STARTING("Starting")*/;
	
	private String name;
	
	GameState(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
