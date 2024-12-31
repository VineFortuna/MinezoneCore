package anthony.SuperCraftBrawl;

public enum Version {

	SCB(2.2), FISHING(2.0);
	
	private double version;
	
	Version(double version) {
		this.version = version;
	}
	
	public double getVersion() {
		return this.version;
	}
	
}
