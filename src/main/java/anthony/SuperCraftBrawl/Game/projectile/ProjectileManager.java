package anthony.SuperCraftBrawl.Game.projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Game.GameManager;

public class ProjectileManager {
	private static double JUMP_DISTANCE = 0.1;

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
				}
				baseProj.lastLoc = baseProj.getEntity().getLocation();
				baseProj.lastVel = baseProj.getEntity().getVelocity();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		shotProjectiles.removeAll(projectilesToRemove);
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
		if (newVel.lengthSquared() < 0.01 * 0.01)
			return true;
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
