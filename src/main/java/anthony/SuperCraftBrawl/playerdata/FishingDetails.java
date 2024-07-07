package anthony.SuperCraftBrawl.playerdata;

public class FishingDetails {
    
    public boolean hasUpdated = false;
    public int timesCaught = 0;
    
    public FishingDetails() {
    
    }
    
    public FishingDetails(int timesCaught) {
        this.timesCaught = timesCaught;
    }
    
    public void addCaught(int caught) {
        hasUpdated = true;
        timesCaught += caught;
    }
    
    public void setCaught(int caught) {
        hasUpdated = true;
        timesCaught = caught;
    }
    
    @Override
    public String toString() {
        return "Times Caught: " + timesCaught + ", Has Updated: " + hasUpdated;
    }
    
}
