package anthony.SuperCraftBrawl.playerdata;

public class ClassDetails {

	public boolean purchased = false, hasUpdated = false,
			reward1 = false, reward2 = false, reward3 = false, reward4 = false, reward5 = false;
	public int timePurchased = 0, gamesPlayed = 0, gamesWon = 0;
    
    public ClassDetails() {

	}

	public ClassDetails(boolean purchased, int timePurchased, int gamesPlayed, int gamesWon,
						boolean reward1, boolean reward2, boolean reward3, boolean reward4, boolean reward5) {
		this.purchased = purchased;
		this.timePurchased = timePurchased;
		this.gamesPlayed = gamesPlayed;
		this.gamesWon = gamesWon;
		this.reward1 = reward1;
		this.reward2 = reward2;
		this.reward3 = reward3;
		this.reward4 = reward4;
		this.reward5 = reward5;
	}

	public void setPurchased() {
		purchased = true;
		hasUpdated = true;
		timePurchased = (int) (System.currentTimeMillis() / 1000);
	}
	
	public void winGame() {
		gamesWon++;
		gamesPlayed++;
		hasUpdated = true;
	}
	
	public void playGame() {
		gamesPlayed++;
		hasUpdated = true;
	}

	@Override
	public String toString() {
		return "Purchased: " + purchased + ", Has Updated: " + hasUpdated + ", Time Purchased: " + timePurchased
				+ ", Games Played: " + gamesPlayed + ", Games Won: " + gamesWon + ", Reward 1: " + reward1
				+ ", Reward 2: " + reward2 + ", Reward 3: " + reward3 + ", Reward 4: " + reward4 + ", Reward 5: " + reward5;
	}

}
