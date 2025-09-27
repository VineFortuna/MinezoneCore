package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class FreddyClass extends BaseClass {

	private boolean isUsed = false;

	public FreddyClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.2;
		createArmor(null,
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGRlNjkzZjEyYjY4MjJmYWQ1ZTZmMjgzYzU1YzM4NWJmZjI1NDhhZTRiMWIyOTQzYWQwNWI1N2VmNWQzOTNiYiJ9fX0=",
				"228B22", 6, "Freddy");
	}


	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.isUsed = false;
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(1, getStunAbility());
	}

	@EventHandler(ignoreCancelled = true)
	public void UseItem(PlayerInteractEvent e) {
		ItemStack item = e.getItem();
		Player p = e.getPlayer();
		
		if (item != null) {
			if (item.getType() == Material.REDSTONE) {
			    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 1, true, false));

			    Location cLoc = p.getLocation();
			    World w = cLoc.getWorld();

			    for (Player target : Bukkit.getOnlinePlayers()) {
			        if (target == p) continue;
			        if (!target.getWorld().equals(w)) continue;

			        // Check XZ distance
			        double dx = target.getLocation().getX() - cLoc.getX();
			        double dz = target.getLocation().getZ() - cLoc.getZ();
			        double distXZ = Math.hypot(dx, dz);
			        if (distXZ > 6.0) continue;

			        // Check Y difference (target up to +2 blocks above)
			        double dy = target.getLocation().getY() - cLoc.getY();
			        if (dy < 0.0 || dy > 2.0) continue;

			        // Apply stun effects
			        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 2, 0, true, false));
			        target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 3, 0, true, false));
			        target.playSound(target.getLocation(), Sound.GHAST_SCREAM, 0.6f, 1.2f);
			    }

			    spawnSubtleRing(w, cLoc);
			    w.playSound(cLoc, Sound.CREEPER_HISS, 0.8f, 1.0f);
			}
		}
	}

	private void spawnSubtleRing(World w, Location center) {
	    int points = 12;          // low particle count to avoid lag
	    double r = 6.0;
	    for (int i = 0; i < points; i++) {
	        double angle = (2 * Math.PI * i) / points;
	        double x = center.getX() + r * Math.cos(angle);
	        double z = center.getZ() + r * Math.sin(angle);
	        Location spot = new Location(w, x, center.getY() + 0.1, z);

	        // A little smoke + colored dust
	        w.spigot().playEffect(spot, Effect.SMOKE, 0, 0, 0, 0, 0, 0.01f, 1, 16);
	        w.spigot().playEffect(spot, Effect.COLOURED_DUST, 0, 0, 0.3f, 0.05f, 0.05f, 1f, 0, 16);
	    }
	}


	@Override
	public ClassType getType() {
		return ClassType.Freddy;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper
				.setUnbreakable(
						ItemHelper.addEnchant(ItemHelper.addEnchant(
								ItemHelper.setDetails(new ItemStack(Material.IRON_AXE), instance.color("&2&lFreddy's Axe")),
								Enchantment.DAMAGE_ALL, 1), Enchantment.KNOCKBACK, 1));
		return item;
	}

	public ItemStack getStunAbility() {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.REDSTONE, 1), instance.color("&cStun Ability"), "",
						instance.color("&7Right click to stun players"), instance.color("&7within 6 blocks!"));
		return item;
	}

}