package anthony.SuperCraftBrawl.Game.projectile;

import org.bukkit.entity.Player;

public class ProjectileDamageHit extends ProjectileOnHit{

	double damage;
	public ProjectileDamageHit(double damage) {
		this.damage = damage;
	}
	
	@Override
	public void onHit(Player hit) {
		hit.damage(damage);
	}
	
}
