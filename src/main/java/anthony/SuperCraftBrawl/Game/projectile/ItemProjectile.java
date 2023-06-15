package anthony.SuperCraftBrawl.Game.projectile;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Game.GameInstance;

public class ItemProjectile extends BaseProjectile {

	private final ItemStack item;
	
	public ItemProjectile(GameInstance instance, Player shooter, ProjectileOnHit onHit, ItemStack item) {
		super(instance, shooter, onHit, 20 * 3);
		this.item  = item;
	}
	
	public ItemStack getItem() {
		return item;
	}
}
