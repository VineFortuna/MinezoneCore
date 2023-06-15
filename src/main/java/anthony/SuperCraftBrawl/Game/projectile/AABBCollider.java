package anthony.SuperCraftBrawl.Game.projectile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.util.Vector;

public class AABBCollider {

	private Vector bounds;
	private Vector location;

	private static double EPSILON = 0.0001;
	
	public AABBCollider(Vector location, Vector bounds) {
		this.bounds = bounds;
		this.location = location;
	}

	// HELPER FUNCTIONS
	
	private Vector sign(Vector vec) {
		vec.setX(vec.getX() >= 0 ? 1 : -1);
		vec.setY(vec.getY() >= 0 ? 1 : -1);
		vec.setZ(vec.getZ() >= 0 ? 1 : -1);
		return vec;
	}
	
	private List<Double> arrayVec(Vector vec) {
		return Arrays.asList(vec.getX(), vec.getY(), vec.getZ());
	}

	private String vectorString(Vector vec) {
		return "vec(" + String.format("%.2f", vec.getX()) + ", " + String.format("%.2f", vec.getY()) + ", "
				+ String.format("%.2f", vec.getZ()) + ")";
	}

	private double[] vectorArray(Vector vec) {
		return new double[] {vec.getX(), vec.getY(), vec.getZ()};
	}
	
	private double vectorIndex(Vector vec, int index) {
		if(index == 0)
			return vec.getX();
		if (index ==1)
			return vec.getY();
		if(index == 2)
			return vec.getZ();

		return 0.0D;
	}
	
	// COLLISION FUNCTIONS
	
	public boolean intersects(Vector pos, Vector bounds) {
		if(Math.abs(pos.getX() - location.getX()) > bounds.getX() + this.bounds.getX())
			return false;
		if(Math.abs(pos.getY() - location.getY()) > bounds.getY() + this.bounds.getY())
			return false;
		if(Math.abs(pos.getZ() - location.getZ()) > bounds.getZ() + this.bounds.getZ())
			return false;
		return true;
	}

	public boolean calculateIntersection(Vector start, Vector delta, Vector otherBounds) {
		return calculateIntersection(start, delta, otherBounds, false);
	}
	
	public boolean calculateIntersection(Vector start, Vector delta, Vector otherBounds, boolean collideInside) {
		
		Vector combinedBounds = bounds.clone().add(otherBounds);
		
		// Check for infinities
		for(int i = 0; i < 3; i ++) {
			if(Math.abs(vectorIndex(delta, i)) < EPSILON) {
				if(vectorIndex(start, i) <= (vectorIndex(location, i) - vectorIndex(combinedBounds, i)) || vectorIndex(start, i) >= (vectorIndex(location, i) + vectorIndex(combinedBounds, i)))
					return false;
			}
		}
		
		Vector scale = new Vector(1.0, 1.0, 1.0).divide(delta);
		Vector signVec = sign(scale.clone());

		Vector positionDelta = location.clone().subtract(start);
		Vector nearDist = positionDelta.clone().subtract(signVec.clone().multiply(combinedBounds));
		Vector farDist = positionDelta.clone().add(signVec.clone().multiply(combinedBounds));

		Vector nearTime = nearDist.clone().multiply(scale);
		Vector farTime = farDist.clone().multiply(scale);

		if (nearTime.getX() > farTime.getY() || nearTime.getX() > farTime.getZ() || nearTime.getY() > farTime.getX()
				|| nearTime.getY() > farTime.getZ() || nearTime.getZ() > farTime.getX()
				|| nearTime.getZ() > farTime.getY()) {
			return false;
		}

		double maxNearTime = Collections.max(arrayVec(nearTime)), minFarTime = Collections.min(arrayVec(farTime));

		if (maxNearTime >= 1 || minFarTime <= EPSILON) {
			return false;
		}
		
		// Don't want to collide if inside block
		if(!collideInside && maxNearTime < -EPSILON)
			return false;

		return true;
	}

}

