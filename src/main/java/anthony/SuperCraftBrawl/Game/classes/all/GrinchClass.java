package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.Objects;

/*
 * The Grinch: fights with his Roast Beast, and throws his Sticky Fingers hook to trade the Roast Beast
 * for the weapon of whoever it hits. Both keep each other's weapon until one of them dies.
 */
public class GrinchClass extends BaseClass {

	private static final int HOOK_SLOT = 1;
	private static final double HOOK_VOID_Y = 50; // a hook that falls below this is destroyed and handed back

	// Hook currently flying - null once it hits, misses or falls into the void
	private ItemProjectile thrownHook;
	private int grinchDeathsAtThrow;

	// Active trade - victim holds our Roast Beast, we hold stolenWeapon
	private Player victim;
	private ItemStack stolenWeapon;
	private int victimDeathsAtSteal;
	private int grinchDeathsAtSteal;

	public GrinchClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWVkYmRjNDFiZjQ4OWNjMWU5NjhmNDA0YWFjNWYyNDEwZTllZjk1MzViYjBhYWU3ZGEyZDljYTQxYWI1ZmY4MyJ9fX0=",
				"5DB77A", 6, "Grinch");
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public ClassType getType() {
		return ClassType.Grinch;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.COOKED_BEEF), instance.color("&2&lRoast Beast"),
						"", instance.color("&7The Grinch's stolen Who-feast."),
						instance.color("&7Turns out it hits pretty hard...")),
				Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
	}

	private ItemStack getStickyFingers() {
		return ItemHelper.setDetails(new ItemStack(Material.TRIPWIRE_HOOK), instance.color("&2&lSticky Fingers"),
				"", instance.color("&7Throw it at an enemy to swap your"),
				instance.color("&7Roast Beast for their weapon."),
				instance.color("&7You keep each other's weapons until"),
				instance.color("&7one of you dies!"));
	}

	@Override
	public void setItems(Inventory playerInv) {
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(HOOK_SLOT, getStickyFingers());
	}

	@Override
	public void tick(int gameTicks) {
		checkTradeEnded();
		checkHookInVoid();

		if (!isPlayerAlive())
			return;

		String msg;
		if (victim != null)
			msg = instance.color("&rHolding &e" + victim.getName() + "'s &rweapon");
		else if (thrownHook != null)
			msg = instance.color("&2Sticky Fingers &ris flying...");
		else
			msg = instance.color("&rThrow &2Sticky Fingers &rat an enemy");
		getActionBarManager().setActionBar(player, "grinch.cooldown", msg, 2);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();
		if (item == null || item.getType() != Material.TRIPWIRE_HOOK
				|| (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
				|| player.getGameMode() == GameMode.SPECTATOR)
			return;

		event.setCancelled(true);
		if (victim != null || thrownHook != null)
			return;

		// A barrier holds the hook's slot until it comes back
		player.getInventory().setItem(player.getInventory().getHeldItemSlot(), getHookPlaceholder());
		player.getWorld().playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.7f);
		grinchDeathsAtThrow = totalDeaths;

		thrownHook = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				if (thrownHook != this.getBaseProj())
					return; // already resolved by the void check
				thrownHook = null;

				// A Grinch who died mid-throw already got a fresh hook on respawn
				if (totalDeaths != grinchDeathsAtThrow || !isPlayerAlive())
					return;

				if (hit == null || !isValidTarget(hit) || !stealWeapon(hit))
					returnHook();
			}
		}, getStickyFingers());
		instance.getGameManager().getProjManager().shootProjectile(thrownHook, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(2.0D));
	}

	/*
	 * Projectiles that never land (e.g. thrown off the map) only expire after 10s and never call onHit,
	 * so destroy the hook once it drops into the void and hand it straight back.
	 */
	private void checkHookInVoid() {
		if (thrownHook == null)
			return;

		Entity hookEntity = thrownHook.getEntity();
		if (hookEntity.isValid() && hookEntity.getLocation().getY() >= HOOK_VOID_Y)
			return;

		hookEntity.remove();
		thrownHook = null;
		if (totalDeaths == grinchDeathsAtThrow && isPlayerAlive())
			returnHook();
	}

	private ItemStack getHookPlaceholder() {
		return ItemHelper.setDetails(new ItemStack(Material.BARRIER), instance.color("&c&lSticky Fingers is out!"),
				"", instance.color("&7Your hook comes back here when it misses,"),
				instance.color("&7or once your weapon trade ends."));
	}

	/** Puts the hook back where its barrier placeholder is (normally the 2nd slot). */
	private void returnHook() {
		Inventory inv = player.getInventory();
		int slot = inv.first(Material.BARRIER);
		if (slot < 0)
			slot = inv.getItem(HOOK_SLOT) == null ? HOOK_SLOT : inv.firstEmpty();
		if (slot >= 0)
			inv.setItem(slot, getStickyFingers());
	}

	private boolean isValidTarget(Player target) {
		BaseClass bc = instance.classes.get(target);
		return target != player && bc != null && bc.isPlayerAlive()
				&& !instance.getGameManager().spawnProt.containsKey(target)
				&& (instance.duosMap == null
						|| !Objects.equals(instance.team.get(target), instance.team.get(player)));
	}

	private boolean stealWeapon(Player target) {
		BaseClass targetClass = instance.classes.get(target);
		if (isTradeVictim(target)) {
			player.sendMessage(instance.color("&2&l(!) &e" + target.getName()
					+ " &ralready had their weapon stolen by a Grinch!"));
			return false;
		}

		Inventory targetInv = target.getInventory();
		Inventory grinchInv = player.getInventory();
		int targetSlot = findWeaponSlot(targetInv, targetClass.getAttackWeapon());
		int grinchSlot = findWeaponSlot(grinchInv, getAttackWeapon());
		if (targetSlot < 0 || grinchSlot < 0) {
			player.sendMessage(instance.color("&2&l(!) &e" + target.getName() + " &rhas no weapon to steal!"));
			return false;
		}

		ItemStack theirWeapon = targetInv.getItem(targetSlot);
		targetInv.setItem(targetSlot, grinchInv.getItem(grinchSlot));
		grinchInv.setItem(grinchSlot, theirWeapon);

		victim = target;
		stolenWeapon = theirWeapon.clone();
		victimDeathsAtSteal = targetClass.totalDeaths;
		grinchDeathsAtSteal = totalDeaths;

		player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 1, 0.5f);
		target.getWorld().playSound(target.getLocation(), Sound.ITEM_PICKUP, 1, 0.6f);
		ParticleEffect.VILLAGER_ANGRY.display(target.getLocation().add(0, 2.2, 0));

		player.sendMessage(instance.color("&2&l(!) &rYou snatched &e" + target.getName()
				+ "'s &rweapon! They're stuck with your &2&lRoast Beast &runtil one of you dies."));
		target.sendMessage(instance.color("&2&l(!) &rThe &2&l&oGrinch &rstole your weapon! You're stuck with his "
				+ "&2&lRoast Beast &runtil one of you dies."));
		return true;
	}

	/*
	 * Ends the trade once either side dies. Whoever died gets their own kit back on respawn, so only the
	 * survivor needs their weapon handed back (plus the hook, if the Grinch survived).
	 */
	private void checkTradeEnded() {
		if (victim == null)
			return;

		BaseClass victimClass = instance.classes.get(victim);
		boolean victimDied = victimClass == null || !victim.isOnline()
				|| victimClass.totalDeaths != victimDeathsAtSteal || victimClass.getLives() <= 0;
		boolean grinchDied = totalDeaths != grinchDeathsAtSteal || getLives() <= 0;
		if (!victimDied && !grinchDied)
			return;

		if (!grinchDied) {
			int slot = findWeaponSlot(player.getInventory(), stolenWeapon);
			if (slot >= 0)
				player.getInventory().setItem(slot, getAttackWeapon());
			returnHook();
			player.sendMessage(instance.color("&2&l(!) &rYour &2&lRoast Beast &rand &2&lSticky Fingers &rare back!"));
		}
		if (!victimDied) {
			int slot = findWeaponSlot(victim.getInventory(), getAttackWeapon());
			if (slot >= 0)
				victim.getInventory().setItem(slot, stolenWeapon);
			victim.sendMessage(instance.color("&2&l(!) &rThe &2&l&oGrinch &ris gone - you got your weapon back!"));
		}

		victim = null;
		stolenWeapon = null;
	}

	private boolean isTradeVictim(Player target) {
		for (BaseClass bc : instance.classes.values()) {
			if (bc instanceof GrinchClass && ((GrinchClass) bc).victim == target)
				return true;
		}
		return false;
	}

	/** Slot holding an item with the same type and display name as weapon, or -1. */
	private static int findWeaponSlot(Inventory inv, ItemStack weapon) {
		if (weapon == null)
			return -1;
		String weaponName = weapon.hasItemMeta() ? weapon.getItemMeta().getDisplayName() : null;

		for (int slot = 0; slot < inv.getSize(); slot++) {
			ItemStack item = inv.getItem(slot);
			if (item != null && item.getType() == weapon.getType()
					&& Objects.equals(item.hasItemMeta() ? item.getItemMeta().getDisplayName() : null, weaponName))
				return slot;
		}
		return -1;
	}
}
