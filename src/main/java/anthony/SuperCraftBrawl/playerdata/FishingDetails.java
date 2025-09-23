package anthony.SuperCraftBrawl.playerdata;

public class FishingDetails {
    
    public boolean hasUpdated = false;
    public int timesCaught = 0;
    public int carrying = 0;
    
    public FishingDetails() {
    
    }
    
    public FishingDetails(int timesCaught, int carrying) {
        this.timesCaught = timesCaught;
        this.carrying = carrying;
    }
    
    public void addCaught(int amount) {
        hasUpdated = true;
        timesCaught += amount;
        carrying += amount;
    }
    
    public void setCaught(int amount) {
        hasUpdated = true;
        timesCaught = amount;
    }

    public void setCarrying(int amount) {
        hasUpdated = true;
        carrying = amount;
    }

    public void removeCarrying(int amount) {
        hasUpdated = true;
        carrying -= amount;
    }
    
    @Override
    public String toString() {
        return "Times Caught: " + timesCaught + ", Carrying: " + carrying + ", Has Updated: " + hasUpdated;
    }
    
}
