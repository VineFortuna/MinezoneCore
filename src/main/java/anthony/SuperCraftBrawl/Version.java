package anthony.SuperCraftBrawl;

public enum Version {

	SCB(2.1), FISHING(1.1);
	
	private double version;
	
	Version(double version) {
		this.version = version;
	}
	
	public double getVersion() {
		return this.version;
	}
	
}
