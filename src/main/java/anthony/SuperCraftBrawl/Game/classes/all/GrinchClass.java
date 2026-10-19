package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.CooldownNatowski;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleEffect;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/*
 * The Grinch: fights with his Roast Beast, and throws his Sticky Fingers hook to trade whatever weapon he's
 * holding for whatever weapon the player it hits is holding. The hook is never used up, only put on
 * cooldown, so a Grinch can keep passing stolen weapons around. Everyone keeps what they end up with until they die.
 */
public class GrinchClass extends BaseClass {

	private static final int HOOK_SLOT = 1;
	private static final double HOOK_COOLDOWN_SECONDS = 7;

	private final CooldownNatowski hookCooldown = new CooldownNatowski(HOOK_COOLDOWN_SECONDS);

	/*
	 * The weapon each player was last handed by a Grinch swap, shared across every Grinch so they can find a
	 * player's current weapon even when another Grinch swapped it in. Stale once the holder dies (they
	 * respawn with their own kit) or changes class.
	 */
	private static final Map<Player, HeldWeapon> HELD_WEAPONS = new WeakHashMap<>();

	private static class HeldWeapon {
		// Weak: the class holds its Player (this map's key, which would then never be cleared) and its game,
		// which holds the game's whole world
		final WeakReference<BaseClass> holderClass;
		final int deathsAtSwap;
		final ItemStack weapon;

		HeldWeapon(BaseClass holderClass, int deathsAtSwap, ItemStack weapon) {
			this.holderClass = new WeakReference<>(holderClass);
			this.deathsAtSwap = deathsAtSwap;
			this.weapon = weapon;
		}
	}

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
				"", instance.color("&7Throw it at an enemy to swap the"),
				instance.color("&7weapon you're holding for theirs."),
				instance.color("&7Reusable every &e7s &7- keep passing"),
				instance.color("&7stolen weapons around!"));
	}

	@Override
	public void setItems(Inventory playerInv) {
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(HOOK_SLOT, getStickyFingers());
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive())
			return;

		String msg;
		if (hookCooldown.isReady())
			msg = instance.color("&rThrow &2Sticky Fingers &rat an enemy");
		else
			msg = instance.color("&2Sticky Fingers &rready in &e" + getHookCooldownLeft() + "s");
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
		if (!hookCooldown.isReady()) {
			player.sendMessage(instance.color("&c&l(!) &rSticky Fingers is on cooldown for &e"
					+ getHookCooldownLeft() + "s"));
			return;
		}

		hookCooldown.use();
		player.getWorld().playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.7f);
		int grinchDeathsAtThrow = totalDeaths;

		ItemProjectile hook = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player hit) {
				// A hook from a Grinch who died mid-throw doesn't count
				if (totalDeaths != grinchDeathsAtThrow || !isPlayerAlive())
					return;

				if (hit != null && isValidTarget(hit))
					swapWeapons(hit);
			}
		}, getStickyFingers());
		instance.getGameManager().getProjManager().shootProjectile(hook, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(2.0D));
	}

	private long getHookCooldownLeft() {
		return (hookCooldown.getRemainingCooldownMillis() + 999) / 1000;
	}

	private boolean isValidTarget(Player target) {
		BaseClass bc = instance.classes.get(target);
		return target != player && bc != null && bc.isPlayerAlive()
				&& !instance.getGameManager().spawnProt.containsKey(target)
				&& (instance.duosMap == null
						|| !Objects.equals(instance.team.get(target), instance.team.get(player)));
	}

	/*
	 * Swaps whatever weapon the Grinch is holding right now (his Roast Beast, or one he stole earlier) with
	 * whatever weapon the target is holding right now. Nobody gets anything back - everyone keeps what they
	 * end up with until they die and respawn with their own kit, so weapons can get passed all around.
	 */
	private boolean swapWeapons(Player target) {
		Inventory targetInv = target.getInventory();
		Inventory grinchInv = player.getInventory();
		int targetSlot = findCurrentWeaponSlot(target);
		int grinchSlot = findCurrentWeaponSlot(player);
		if (targetSlot < 0 || grinchSlot < 0) {
			player.sendMessage(instance.color("&2&l(!) &e" + target.getName() + " &rhas no weapon to steal!"));
			return false;
		}

		ItemStack theirWeapon = targetInv.getItem(targetSlot);
		ItemStack ourWeapon = grinchInv.getItem(grinchSlot);
		targetInv.setItem(targetSlot, ourWeapon);
		grinchInv.setItem(grinchSlot, theirWeapon);
		setHeldWeapon(target, ourWeapon);
		setHeldWeapon(player, theirWeapon);

		player.getWorld().playSound(player.getLocation(), Sound.VILLAGER_HAGGLE, 1, 0.5f);
		target.getWorld().playSound(target.getLocation(), Sound.ITEM_PICKUP, 1, 0.6f);
		ParticleEffect.VILLAGER_ANGRY.display(target.getLocation().add(0, 2.2, 0));

		player.sendMessage(instance.color("&2&l(!) &rYou traded your " + itemName(ourWeapon) + " &rfor &e"
				+ target.getName() + "'s " + itemName(theirWeapon) + "&r!"));
		target.sendMessage(instance.color("&2&l(!) &rThe &2&l&oGrinch &rswiped your " + itemName(theirWeapon)
				+ " &rand left you with " + itemName(ourWeapon) + "&r! You're stuck with it until you die."));
		return true;
	}

	private void setHeldWeapon(Player holder, ItemStack weapon) {
		BaseClass holderClass = instance.classes.get(holder);
		HELD_WEAPONS.put(holder, new HeldWeapon(holderClass, holderClass.totalDeaths, weapon.clone()));
	}

	/*
	 * Slot of the weapon a player is currently fighting with: the last weapon a Grinch swapped into their
	 * hands if that's still live (same class, no death since), otherwise their class's own weapon.
	 */
	private int findCurrentWeaponSlot(Player holder) {
		BaseClass holderClass = instance.classes.get(holder);
		if (holderClass == null)
			return -1;

		HeldWeapon held = HELD_WEAPONS.get(holder);
		if (held != null) {
			if (held.holderClass.get() == holderClass && held.deathsAtSwap == holderClass.totalDeaths) {
				int slot = findWeaponSlot(holder.getInventory(), held.weapon);
				if (slot >= 0)
					return slot;
			}
			HELD_WEAPONS.remove(holder);
		}
		return findWeaponSlot(holder.getInventory(), holderClass.getAttackWeapon());
	}

	private static String itemName(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
				: "&e" + item.getType().name().toLowerCase().replace('_', ' ');
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
