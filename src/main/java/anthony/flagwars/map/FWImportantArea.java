package anthony.flagwars.map;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * A no-build zone on a team's island, set in-game with /fwsetup importantarea, centered on the block the
 * setup player stood on and reaching xRadius/yRadius/zRadius blocks out in both the positive and negative
 * direction of each axis (an X radius of 3 = 7 blocks wide).
 */
public class FWImportantArea {

	private final Location center;
	private final int xRadius;
	private final int yRadius;
	private final int zRadius;

	public FWImportantArea(Location center, int xRadius, int yRadius, int zRadius) {
		this.center = center;
		this.xRadius = xRadius;
		this.yRadius = yRadius;
		this.zRadius = zRadius;
	}

	public Location getCenter() {
		return center;
	}

	public int getXRadius() {
		return xRadius;
	}

	public int getYRadius() {
		return yRadius;
	}

	public int getZRadius() {
		return zRadius;
	}

	public boolean contains(Location loc) {
		if (center == null || !loc.getWorld().equals(center.getWorld())) return false;
		return Math.abs(loc.getBlockX() - center.getBlockX()) <= xRadius
				&& Math.abs(loc.getBlockY() - center.getBlockY()) <= yRadius
				&& Math.abs(loc.getBlockZ() - center.getBlockZ()) <= zRadius;
	}

	public FWImportantArea translateTo(World targetWorld) {
		Location moved = center == null ? null
				: new Location(targetWorld, center.getX(), center.getY(), center.getZ(), center.getYaw(), center.getPitch());
		return new FWImportantArea(moved, xRadius, yRadius, zRadius);
	}
}
