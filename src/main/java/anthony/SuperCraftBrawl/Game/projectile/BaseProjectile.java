package anthony.SuperCraftBrawl.Game.projectile;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Game.GameInstance;

public abstract class BaseProjectile {
	private GameInstance instance;
	public Location lastLoc;
	private ProjectileOnHit onHit;
	private Player shooter;
	private Entity summonedEntity;
	private int maxAge, age = 0;
	public Vector lastVel = new Vector(0, 0, 0);
	
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
		return age > maxAge || summonedEntity.getVelocity().lengthSquared() < 0.01;
	}
	
	public Player getShooter() {
		return shooter;
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
