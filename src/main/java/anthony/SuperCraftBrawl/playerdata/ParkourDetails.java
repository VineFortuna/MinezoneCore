package anthony.SuperCraftBrawl.playerdata;

public class ParkourDetails {

    public boolean hasUpdated = false;
    public long totalTime = 0;

    public ParkourDetails() {

    }

    public ParkourDetails(long totalTime) {
        this.totalTime = totalTime;
    }

    public void completeParkour(long totalTime) {
        hasUpdated = true;
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "Total Time: " + totalTime + ", Has Updated: " + hasUpdated;
    }

}
