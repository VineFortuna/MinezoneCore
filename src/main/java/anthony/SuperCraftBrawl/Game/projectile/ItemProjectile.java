package anthony.SuperCraftBrawl.Game.projectile;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Game.GameInstance;

public class ItemProjectile extends BaseProjectile {

	private final ItemStack item;
	
	public ItemProjectile(GameInstance instance, Player shooter, ProjectileOnHit onHit, ItemStack item) {
		this(instance, shooter, onHit, item, 20 * 10);
	}

	/** A projectile that is gone after this many ticks, if it hasn't hit anything before. */
	public ItemProjectile(GameInstance instance, Player shooter, ProjectileOnHit onHit, ItemStack item, int ageInTicks) {
		super(instance, shooter, onHit, ageInTicks);
		this.item  = item;
	}
	
	public ItemStack getItem() {
		return item;
	}
}
