package anthony.SuperCraftBrawl.Game.classes;

public class Cooldown {
	private long lastTime = System.currentTimeMillis() - 100000;
	private long cooldownTime = 1000;

	public Cooldown(long cooldownTime) {
		this.cooldownTime = cooldownTime;
	}

	public boolean useAndResetCooldown() {

		if (System.currentTimeMillis() - lastTime > cooldownTime) {
			reset();
			return true;
		} else {
			return false;
		}
	}

	public void reset() {
		lastTime = System.currentTimeMillis();
	}
}
