package anthony.SuperCraftBrawl.Game.projectile;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public abstract class ProjectileOnHit {

	private BaseProjectile baseProj;

	public BaseProjectile getBaseProj() {
		return baseProj;
	}

	public void setBaseProj(BaseProjectile baseProj) {
		this.baseProj = baseProj;
	}

	public List<Player> getNearby(double radius) {
		List<Player> players = new ArrayList<Player>();

		for (Player player : baseProj.getEntity().getLocation().getWorld().getPlayers())
			if (player.getLocation().add(0, 0.8, 0).distanceSquared(baseProj.getEntity().getLocation()) <= radius
					* radius)
				players.add(player);

		return players;
	}

	public abstract void onHit(Player hit);
}
