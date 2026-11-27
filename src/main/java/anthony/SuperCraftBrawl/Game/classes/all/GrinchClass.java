package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.FakeLoot;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

/*
 * The Grinch: fights with his Roast Beast, and throws his Sticky Fingers hook to trade whatever weapon he's
 * holding for whatever weapon the player it hits is holding. The hook is never used up, only put on
 * cooldown, so a Grinch can keep passing stolen weapons around. Everyone keeps what they end up with until they die.
 *
 * His Counterfeit Loot throws a fake Lightning Drop on the ground. He can't pick it up, everyone else can, and
 * when they use it, it does something sneaky instead (see FakeLoot).
 */
public class GrinchClass extends BaseClass {

	private static final int HOOK_SLOT = 1;
	private static final double HOOK_COOLDOWN_SECONDS = 5;

	private static final int FAKE_DROP_SLOT = 2;
	private static final double FAKE_DROP_COOLDOWN_SECONDS = 15;
	/** How fast a fake drop is thrown out of his hands. */
	private static final double FAKE_DROP_THROW_SPEED = 0.6;

	// Both are shown on the action bar together, the same as Spider's Mutate and Web
	private final Ability hookAbility = new Ability("&2&lSticky Fingers", HOOK_COOLDOWN_SECONDS, player);
	private final Ability fakeDropAbility = new Ability("&6&lCounterfeit Loot", FAKE_DROP_COOLDOWN_SECONDS, player);

	/*
	 * The weapon each player was last handed by a Grinch swap, shared across every Grinch so they can find a
	 * player's current weapon even when another Grinch swapped it in. Stale once the holder dies (they
	 * respawn with their own kit) or changes class.
	 */
	private static final Map<Player, HeldWeapon> HELD_WEAPONS = new WeakHashMap<>();

	/**
	 * Whether this player is holding a weapon a Grinch swapped into their hands (same class, no death since, and
	 * still in their inventory). Classes that hand their own weapon back whenever it is missing must not do that,
	 * or the swap would be undone a tick later.
	 */
	public static boolean isHoldingSwappedWeapon(Player holder, BaseClass holderClass) {
		HeldWeapon held = HELD_WEAPONS.get(holder);
		if (held == null || held.holderClass.get() != holderClass || held.deathsAtSwap != holderClass.totalDeaths)
			return false;
		return findWeaponSlot(holder.getInventory(), held.weapon) >= 0;
	}

	private static class HeldWeapon {
		// Weak: the class holds its Player (this map's key, which would then never be cleared) and its game,
		// which holds the game's whole world
		final WeakReference<BaseClass> holderClass;
		final int deathsAtSwap;
		final ItemStack weapon;
		// The Grinch whose swap put it there, so it can be taken back when that Grinch dies
		final UUID grinch;

		HeldWeapon(UUID grinch, BaseClass holderClass, int deathsAtSwap, ItemStack weapon) {
			this.grinch = grinch;
			this.holderClass = new WeakReference<>(holderClass);
			this.deathsAtSwap = deathsAtSwap;
			this.weapon = weapon;
		}
	}

	public GrinchClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNhMWEzNjZhMmZlN2QwMzNlMWRkYzJiMjZmOWViYmJmOTZhZDVmZjgzNGU4ZjNkYmUwNmI0N2UzOGM3YjIyYiJ9fX0=",
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
				ItemHelper.setDetails(new ItemStack(Material.COOKED_BEEF), instance.color("&4&lRoast Beast"),
						"", instance.color("&7The Grinch's stolen Who-feast."),
						instance.color("&7Turns out it hits pretty hard...")),
				Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
	}

	private ItemStack getStickyFingers() {
		return ItemHelper.setDetails(new ItemStack(Material.TRIPWIRE_HOOK), instance.color(hookAbility.getAbilityNameRightClickMessage()),
				instance.color("&7Throw it at a player to swap weapons"),
				instance.color("&7with them, unabling abilities they"),
				instance.color("&7would activate with it"),
				"",
				instance.color("&7Reusable, so you can keep passing"),
				instance.color("&7stolen weapons around"));
	}

	private ItemStack getCounterfeitLoot() {
		return ItemHelper.setDetails(new ItemStack(Material.CHEST), instance.color(fakeDropAbility.getAbilityNameRightClickMessage()),
				instance.color("&7Throws a counterfeit Lightning Drop"),
				instance.color("&7item on the ground for other players"),
				instance.color("&7to pick up."),
				"",
				instance.color("&7They look like the real item but do"),
				instance.color("&7something sneaky when used: A healing"),
				instance.color("&7potion that hurts, a speed potion that"),
				instance.color("&7gives slowness and so on."));
	}

	@Override
	public void setItems(Inventory playerInv) {
		fakeDropAbility.getCooldownInstance().reset(); // a new life starts with Counterfeit Loot ready
		playerInv.setItem(0, getAttackWeapon());
		playerInv.setItem(HOOK_SLOT, getStickyFingers());
		playerInv.setItem(FAKE_DROP_SLOT, getCounterfeitLoot());
	}

	@Override
	public void tick(int gameTicks) {
		if (!isPlayerAlive())
			return;

		ActionBarManager.AbilityActionBar actionBar = new ActionBarManager.AbilityActionBar(this, getActionBarManager());
		// The short wording ("Name in 5s"): both names together are too long for "regenerates in"
		actionBar.setActionBarAbilityWhiteShort(player, hookAbility, fakeDropAbility);
	}

	@Override
	public void useItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();
		if (item == null || (item.getType() != Material.TRIPWIRE_HOOK && item.getType() != Material.CHEST)
				|| (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
				|| player.getGameMode() == GameMode.SPECTATOR)
			return;

		event.setCancelled(true); // for the chest too, so it can't be placed
		if (item.getType() == Material.CHEST) {
			throwFakeDrop();
			return;
		}

		if (!hookAbility.isReady()) {
			hookAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}

		hookAbility.use();
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

	/*
	 * Throws a fake Lightning Drop: a random drop that has a fake version, rolled like a real one (so the rarer
	 * ones are rarer), tagged as his so he can't pick it up (everyone else can, see FakeLoot).
	 */
	private void throwFakeDrop() {
		if (!fakeDropAbility.isReady()) {
			fakeDropAbility.sendPlayerRemainingCooldownChatMessage();
			return;
		}
		fakeDropAbility.use();

		GameLootDrops drop = FakeLoot.pickDrop(instance.getGameSettings().enabledDrops);

		Location from = player.getEyeLocation();
		Item thrown = dropFake(from, drop);
		thrown.setVelocity(from.getDirection().multiply(FAKE_DROP_THROW_SPEED));
		thrown.setPickupDelay(20);

		player.getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 0.8f);
		player.sendMessage(instance.color("&2&l(!) &rYou threw a fake " + itemName(thrown.getItemStack()) + "&r."));
	}

	/*
	 * He picked up a real drop: a fake copy of it stays where it lay (no message, so it looks like nothing happened),
	 * for whoever walks by next.
	 */
	public void leaveFakeBehind(Location where, GameLootDrops drop) {
		Item left = dropFake(where, drop);
		left.setVelocity(new Vector(0, 0, 0));
		left.setPickupDelay(10);
	}

	/**
	 * Drops a fake of this drop (his, so he can't take it) at a spot. Nothing removes it but the server: it despawns on
	 * its own after the same time as the real drops, which are given that time the same way.
	 */
	private Item dropFake(Location at, GameLootDrops drop) {
		Item dropped = at.getWorld().dropItem(at, FakeLoot.mark(drop.getItem(), player.getUniqueId()));
		GameLootDrops.setDespawnTime(dropped);
		return dropped;
	}

	/*
	 * He died: every weapon he swapped around goes back. Each player he left holding a swapped weapon (his Roast Beast, or
	 * one stolen from someone else) gets their own class weapon again, the same as if they had respawned.
	 */
	@Override
	public void onPlayerDied() {
		UUID grinchId = player.getUniqueId();
		List<Player> holders = new ArrayList<>();
		for (Map.Entry<Player, HeldWeapon> entry : HELD_WEAPONS.entrySet()) {
			if (grinchId.equals(entry.getValue().grinch))
				holders.add(entry.getKey());
		}

		for (Player holder : holders) {
			HeldWeapon held = HELD_WEAPONS.remove(holder);
			BaseClass holderClass = instance.classes.get(holder);
			// Himself: he respawns with his own kit. Anyone who died or changed class since already has theirs back
			if (held == null || holder == player || holderClass == null || !holder.isOnline()
					|| held.holderClass.get() != holderClass || held.deathsAtSwap != holderClass.totalDeaths
					|| holderClass.getLives() <= 0)
				continue;

			int slot = findWeaponSlot(holder.getInventory(), held.weapon);
			if (slot < 0)
				continue;

			holder.getInventory().setItem(slot, holderClass.getAttackWeapon().clone());
			holder.playSound(holder.getLocation(), Sound.ITEM_PICKUP, 1, 1.2f);
			holder.sendMessage(instance.color("&2&l(!) &rThe &2&l&oGrinch &rdied, you got your weapon back!"));
		}
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
		HELD_WEAPONS.put(holder, new HeldWeapon(player.getUniqueId(), holderClass, holderClass.totalDeaths, weapon.clone()));
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
