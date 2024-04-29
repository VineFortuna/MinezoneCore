package anthony.SuperCraftBrawl.playerdata;

public class ClassDetails {

	public boolean purchased = false, hasUpdated = false;
	public int timePurchased = 0, gamesPlayed = 0;

	public ClassDetails() {

	}

	public ClassDetails(boolean purchased, int timePurchased, int gamesPlayed) {
		this.purchased = purchased;
		this.timePurchased = timePurchased;
		this.gamesPlayed = gamesPlayed;
	}

	public void setPurchased() {
		purchased = true;
		hasUpdated = true;
		timePurchased = (int) (System.currentTimeMillis() / 1000);
	}

	@Override
	public String toString() {
		return "Purchased: " + purchased + ", Has Updated: " + hasUpdated + ", Time Purchased: " + timePurchased
				+ ", Games Played: " + gamesPlayed;
	}

}
