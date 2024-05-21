package anthony.SuperCraftBrawl.playerdata;

public class FishingDetails {
    
    public int timesCaught = 0;
    
    public FishingDetails() {
    
    }
    
    public FishingDetails(int timesCaught) {
        this.timesCaught = timesCaught;
    }
    
    public void addCaught(int caught) {
        timesCaught += caught;
    }
    
    public void setCaught(int caught) {
        timesCaught = caught;
    }
    
    @Override
    public String toString() {
        return "Times Caught: " + timesCaught;
    }
    
}
