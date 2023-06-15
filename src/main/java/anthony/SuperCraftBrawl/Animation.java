package anthony.SuperCraftBrawl;

import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class Animation extends BukkitRunnable {

	public ArmorStand stand;
	private double multiply = 0.0;
	private int ticks = 0;

	public Animation(ArmorStand stand) {
		this.stand = stand;
	}

	@Override
	public void run() {
		EulerAngle oldRot = stand.getRightArmPose();
		EulerAngle newRot = oldRot.add(0, this.multiply, 0);
		stand.setRightArmPose(newRot);
		
		if (ticks == 88)
			stand.setGravity(true);
		if (ticks % 10 == 0)
			this.multiply += 0.1;
		
		ticks++;
	}
}
