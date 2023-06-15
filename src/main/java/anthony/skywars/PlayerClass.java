package anthony.skywars;

public class PlayerClass {
	
	public int kills;
	private GameInstance i;

	public PlayerClass(GameInstance i) {
		this.i = i;
		this.kills = 0; //Initial starting value
	}
	
	public int getKills() {
		return this.kills;
	}

}
