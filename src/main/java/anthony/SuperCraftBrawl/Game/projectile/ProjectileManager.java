package anthony.SuperCraftBrawl.Game.projectile;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Game.GameManager;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

public class ProjectileManager {
	private static double JUMP_DISTANCE = 0.1;
	/** A trail has about one particle for each block flown, and never more than this many in a tick. */
	private static final double TRAIL_SPACING = 1.0;
	private static final int TRAIL_MAX_PARTICLES_PER_TICK = 3;

	private GameManager manager;
	private List<BaseProjectile> shotProjectiles = new ArrayList<>();

	public ProjectileManager(GameManager manager) {
		this.manager = manager;

		BukkitRunnable checkCollisions = new BukkitRunnable() {
			@Override
			public void run() {
				checkAllCollision();
			}
		};
		checkCollisions.runTaskTimer(manager.getMain(), 0, 1);
	}

	public boolean isProjectile(Entity entity) {
		for (BaseProjectile proj : shotProjectiles)
			if (proj.getEntity().equals(entity))
				return true;
		return false;
	}

	public void shootProjectile(ItemProjectile itemProj, Location loc, Vector vel) {
		ItemStack itemStack = itemProj.getItem().clone();
		itemProj.lastLoc = loc;
		ItemMeta im = itemStack.getItemMeta();
		im.setDisplayName(UUID.randomUUID().toString());
		itemStack.setItemMeta(im);
		Item item = loc.getWorld().dropItem(loc, itemStack);
		item.setVelocity(vel);
		itemProj.setEntity(item);
		shotProjectiles.add(itemProj);
	}

	public void checkAllCollision() {
		List<BaseProjectile> projectilesToRemove = new ArrayList<>();
		for (BaseProjectile baseProj : shotProjectiles) {
			try {
				if (baseProj.tooOld() || checkCollision(baseProj) || checkBlockCollision(baseProj)) {
					projectilesToRemove.add(baseProj);
					baseProj.destroy();
				} else {
					baseProj.addAge(1);
					showTrail(baseProj);
				}
				baseProj.lastLoc = baseProj.getEntity().getLocation();
				baseProj.lastVel = baseProj.getEntity().getVelocity();
			} catch (Exception e) {
				e.printStackTrace();
				baseProj.addAge(1); // so one that keeps failing still times out, instead of being kept forever
			}
		}

		shotProjectiles.removeAll(projectilesToRemove);
	}

	/**
	 * Leaves a light trail of colored dust along the way the projectile flew since the last tick, if it has a trail
	 * color. Only about one particle for each block it flew, so a fast projectile doesn't flood the players.
	 */
	private void showTrail(BaseProjectile proj) {
		Color color = proj.getTrailColor();
		if (color == null || proj.lastLoc == null) return;

		Location from = proj.lastLoc;
		Location to = proj.getEntity().getLocation();
		if (from.getWorld() != to.getWorld()) return;

		Vector step = to.toVector().subtract(from.toVector());
		int particles = (int) Math.max(1, Math.min(TRAIL_MAX_PARTICLES_PER_TICK, step.length() / TRAIL_SPACING));
		step.multiply(1.0 / particles);

		Location point = from.clone();
		for (int i = 0; i < particles; i++) {
			point.add(step);
			ParticleBuilder particle = new ParticleBuilder(ParticleEffect.REDSTONE);
			particle.setColor(color);
			particle.setLocation(point.clone());
			particle.setOffset(0.0f, 0.0f, 0.0f);
			particle.display(proj.getInstance().players);
		}
	}

	private boolean didStop(double newValue, double oldValue) {
		if (Math.abs(oldValue) > 0.05 && Math.abs(newValue) < 0.01)
			return true;
		return false;
	}

	private boolean isBlockCollision(BaseProjectile proj) {
		Vector newVel = proj.getEntity().getVelocity();
		Vector oldVel = proj.lastVel;

		if (didStop(newVel.getX(), oldVel.getX()))
			return true;
		if (didStop(newVel.getY(), oldVel.getY()))
			return true;
		if (didStop(newVel.getZ(), oldVel.getZ()))
			return true;
		//if (newVel.lengthSquared() < 0.01 * 0.01)
			//return true;
		return false;
	}

	public boolean checkBlockCollision(BaseProjectile proj) {
		if (this.isBlockCollision(proj)) {
			try {
			proj.getOnHit().onHit(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public boolean checkCollision(BaseProjectile proj) {
		Entity entity = proj.getEntity();
		// Starting and end locations
		Location entLoc = entity.getLocation();
		Location lastLoc = proj.lastLoc;

		// Calculate direction and length
		Vector dir = entLoc.clone().subtract(lastLoc).toVector();

		// Loop through each player
		for (Player player : proj.getInstance().players) {
			if (player != proj.getShooter()) {
				AABBCollider collider = new AABBCollider(player.getLocation().add(0, 0.8, 0).toVector(),
						new Vector(0.3, 0.8, 0.3));
				if (collider.calculateIntersection(lastLoc.toVector(), dir.clone(), new Vector(0.3, 0.3, 0.3), true)) {
					try {
						proj.getOnHit().onHit(player);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			}
		}
		return false;
	}

}
