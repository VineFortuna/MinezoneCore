package anthony.SuperCraftBrawl;

public class Timer {

	long startTime = System.currentTimeMillis() - 100000;
	
	public void restart() {
		startTime = System.currentTimeMillis();
	}
	
	public int getTime() {
		return (int)(System.currentTimeMillis() - startTime);
	}
	
}
