package anthony.SuperCraftBrawl.Game.projectile;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Game.GameInstance;

public abstract class BaseProjectile {
	private final GameInstance instance;
	public Location lastLoc;
	private final ProjectileOnHit onHit;
	private final Player shooter;
	private Entity summonedEntity;
	private final int maxAge;
    private int age = 0;
	public Vector lastVel = new Vector(0, 0, 0);
	/** The colors of the particle trail the projectile leaves while it flies. No trail without any. */
	private Color[] trailColors = new Color[0];
	/** A trail of dashes: how long each is, and how far it is from the start of one to the start of the next. 0 for no gaps. */
	private double trailDashLength;
	private double trailDashPeriod;
	/** How far along its flight the trail has been drawn, to know where the dashes are. */
	private double trailDistance;

	public BaseProjectile(GameInstance instance, Player shooter, ProjectileOnHit onHit, int ageInTicks) {
		this.instance = instance;
		this.shooter = shooter;
		this.maxAge = ageInTicks;
		this.onHit = onHit;
		this.onHit.setBaseProj(this);
	}
	
	public void destroy() {
		summonedEntity.remove();
	}
	
	public void addAge(int age) {
		this.age += age;
	}
	
	public boolean tooOld() {
		return age > maxAge;
	}
	
	public Player getShooter() {
		return shooter;
	}

	public boolean hasTrail() {
		return trailColors.length > 0;
	}

	/** One of the trail colors, picked at random for each particle. */
	public Color randomTrailColor() {
		return trailColors[ThreadLocalRandom.current().nextInt(trailColors.length)];
	}

	/** Makes the projectile leave a trail of particles while it flies. With more than one color they are mixed. */
	public void setTrailColor(Color... colors) {
		this.trailColors = colors;
	}

	/** Makes the trail dashes of this length, one starting every period blocks of flight, instead of one that never stops. */
	public void setTrailDashes(double length, double period) {
		this.trailDashLength = length;
		this.trailDashPeriod = period;
	}

	public boolean isTrailDashed() {
		return trailDashPeriod > 0;
	}

	/** Whether a spot this far along the flight is in a dash, or any spot when the trail isn't dashed. */
	public boolean isInTrailDash(double distance) {
		return !isTrailDashed() || distance % trailDashPeriod < trailDashLength;
	}

	public double getTrailDistance() {
		return trailDistance;
	}

	public void setTrailDistance(double trailDistance) {
		this.trailDistance = trailDistance;
	}
	
	public GameInstance getInstance() {
		return instance;
	}
	
	public ProjectileOnHit getOnHit() {
		return onHit;
	}
	
	public Entity getEntity() {
		return summonedEntity;
	}
	
	public void setEntity(Entity entity) {
		summonedEntity = entity;
	}
}
