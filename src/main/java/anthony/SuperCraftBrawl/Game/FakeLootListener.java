package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The fake Lightning Drops that have no handler of their own in GameManager (see FakeLoot): the thrown potions, the
 * apples and the Hammer, plus everything that has to be found out about a throw or a hit before the real handlers
 * (which don't know about fakes) get to it: fake snowballs and pearls, the fake Nuke's TNT, and the changed knockback.
 * <p>
 * Everything it remembers per player is keyed by UUID, short lived, and dropped when they quit or die.
 */
public class FakeLootListener implements Listener {

	// ---- Fake Potions
	private static final int FAKE_EFFECT_TICKS = 15 * 20;

	// ---- Fake Golden Apple: Absorption (2 hearts) and Poison I for 8s
	private static final int GOLDEN_ABSORPTION_TICKS = 2 * 60 * 20;
	private static final int GOLDEN_POISON_TICKS = 8 * 20;

	// ---- Fake Notch Apple: its usual effects, but for 5 seconds, and more knockback taken for a while after
	private static final int NOTCH_EFFECT_TICKS = 5 * 20;
	private static final long NOTCH_PENALTY_MS = 20_000;
	private static final double NOTCH_KNOCKBACK_HORIZONTAL = 2.5;
	private static final double NOTCH_KNOCKBACK_VERTICAL = 1.5;

	// ---- Fake Hammer: who it hits gets the knockback of a regular hit (not Knockback 10), and whoever swings it is pushed back
	private static final double REGULAR_HIT_HORIZONTAL = 0.4;
	private static final double REGULAR_HIT_SPRINT_BONUS = 0.5; // a sprinting hit pushes further
	private static final double REGULAR_HIT_VERTICAL = 0.4;
	private static final double HAMMER_RECOIL_HORIZONTAL = 1.5;
	private static final double HAMMER_RECOIL_VERTICAL = 0.6;

	// ---- Fake Nuke: the TNT does no damage, only knockback
	private static final double NUKE_KNOCKBACK_BASE = 0.5;
	private static final double NUKE_KNOCKBACK_RANGE = 1.5;
	private static final double NUKE_KNOCKBACK_MAX_DISTANCE = 8.0;
	private static final double NUKE_KNOCKBACK_VERTICAL = 0.7;

	/** How long a knockback change waits for the player's velocity to be sent. It is used up when it is. */
	private static final long TWEAK_MS = 250;
	/** A right click with a fake snowball or pearl, waiting for the projectile it throws. */
	private static final long THROW_MS = 250;

	private static final class PendingThrow {
		final Material type;
		final long until;

		PendingThrow(Material type, long until) {
			this.type = type;
			this.until = until;
		}
	}

	private static final class Tweak {
		final long until;
		final boolean clamp; // true: the Hammer's, cut the knockback down. false: the Notch Apple's, scale it up
		final double maxHorizontal; // for the Hammer's: how far a regular hit pushes

		Tweak(long until, boolean clamp, double maxHorizontal) {
			this.until = until;
			this.clamp = clamp;
			this.maxHorizontal = maxHorizontal;
		}
	}

	private final Core main;
	private final Map<UUID, PendingThrow> pendingThrows = new HashMap<>();
	private final Map<UUID, Tweak> tweaks = new HashMap<>();
	private final Map<UUID, Long> notchPenaltyUntil = new HashMap<>();

	public FakeLootListener(Core main) {
		this.main = main;
	}

	private void forget(UUID uuid) {
		pendingThrows.remove(uuid);
		tweaks.remove(uuid);
		notchPenaltyUntil.remove(uuid);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		forget(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		forget(event.getEntity().getUniqueId());
	}

	// ---------------------------------------------------------------- fake snowballs and pearls

	/**
	 * Remembers a right click with a fake snowball or ender pearl, so the projectile it throws a moment later can be
	 * marked. The hand is no help at that point: a thrown item is already used up.
	 */
	// Not ignoreCancelled: on 1.8 a right click in the air counts as cancelled from the start (it has no block), so
	// that would skip every throw. What stops a throw is the item use being denied.
	@EventHandler(priority = EventPriority.MONITOR)
	public void onThrowClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (event.useItemInHand() == Event.Result.DENY)
			return;
		ItemStack item = event.getItem();
		if (item == null || (item.getType() != Material.SNOW_BALL && item.getType() != Material.ENDER_PEARL))
			return;

		UUID uuid = event.getPlayer().getUniqueId();
		if (FakeLoot.isFake(item))
			pendingThrows.put(uuid, new PendingThrow(item.getType(), System.currentTimeMillis() + THROW_MS));
		else
			pendingThrows.remove(uuid);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLaunch(ProjectileLaunchEvent event) {
		Projectile projectile = event.getEntity();
		if (!(projectile.getShooter() instanceof Player))
			return;
		if (!(projectile instanceof Snowball) && !(projectile instanceof EnderPearl))
			return;

		UUID uuid = ((Player) projectile.getShooter()).getUniqueId();
		PendingThrow pending = pendingThrows.remove(uuid);
		if (pending == null || pending.until < System.currentTimeMillis())
			return;

		Material thrown = projectile instanceof Snowball ? Material.SNOW_BALL : Material.ENDER_PEARL;
		if (pending.type == thrown) {
			FakeLoot.markEntity(projectile);
			if (projectile instanceof EnderPearl)
				FakeLoot.markThrown(projectile); // its teleport is 3s after the throw, not the landing
		}
	}

	// ---------------------------------------------------------------- fake potions

	@EventHandler
	public void onSplash(PotionSplashEvent event) {
		ThrownPotion potion = event.getEntity();
		ItemStack item = potion.getItem();
		if (!FakeLoot.isFake(item))
			return;

		GameLootDrops drop = GameLootDrops.fromItem(item);
		if (drop == null)
			return;

		event.setCancelled(true); // none of the real potion's effects

		// A fake Bomb is a dud: it only fizzles
		if (drop == GameLootDrops.BOMB) {
			Location where = potion.getLocation();
			where.getWorld().playSound(where, Sound.FIZZ, 1f, 1f);
			where.getWorld().playEffect(where, Effect.SMOKE, 4);
			return;
		}

		// A fake Healing potion does nothing, but it fizzles like the dud Bomb
		if (drop == GameLootDrops.HEALTH2) {
			Location where = potion.getLocation();
			where.getWorld().playSound(where, Sound.FIZZ, 1f, 1f);
			return;
		}

		for (LivingEntity target : event.getAffectedEntities()) {
			if (!(target instanceof Player) || ((Player) target).getGameMode() == GameMode.SPECTATOR)
				continue;
			int ticks = (int) (FAKE_EFFECT_TICKS * event.getIntensity(target) + 0.5);

			if (drop == GameLootDrops.SPEED2) // the Speed potion slows instead
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 1), true);
			else if (drop == GameLootDrops.SLOWNESS) // the Slowness potion speeds up instead
				target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 1), true);
			// the Healing potion does nothing at all
		}
	}

	// ---------------------------------------------------------------- fake apples

	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsume(PlayerItemConsumeEvent event) {
		ItemStack item = event.getItem();
		if (item == null || item.getType() != Material.GOLDEN_APPLE || !FakeLoot.isFake(item))
			return;

		Player player = event.getPlayer();
		event.setCancelled(true); // none of the real apple's effects, so it's used up by hand below
		useOneFromHand(player);
		player.getWorld().playSound(player.getLocation(), Sound.BURP, 0.5f, 1f);

		if (item.getDurability() == 1) {
			// Notch Apple: the same effects, for 5 seconds only
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, NOTCH_EFFECT_TICKS, 4), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, NOTCH_EFFECT_TICKS, 3), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, NOTCH_EFFECT_TICKS, 0), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, NOTCH_EFFECT_TICKS, 0), true);
			notchPenaltyUntil.put(player.getUniqueId(), System.currentTimeMillis() + NOTCH_PENALTY_MS);
		} else {
			// Golden Apple: Absorption, but Poison instead of Regeneration
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, GOLDEN_ABSORPTION_TICKS, 0), true);
			player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, GOLDEN_POISON_TICKS, 0), true);
		}
	}

	@SuppressWarnings("deprecation")
	private void useOneFromHand(Player player) {
		ItemStack hand = player.getItemInHand();
		if (hand == null || hand.getType() != Material.GOLDEN_APPLE)
			return;
		if (hand.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			hand.setAmount(hand.getAmount() - 1);
			player.setItemInHand(hand);
		}
		player.updateInventory();
	}

	// ---------------------------------------------------------------- the fake Nuke's TNT

	/** The fake Nuke's TNT hurts nobody: it cancels the damage and pushes by hand, which the cancelled hit would skip. */
	@EventHandler(priority = EventPriority.LOW)
	public void onNukeBlast(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof TNTPrimed) || !FakeLoot.isMarked(event.getDamager()))
			return;
		event.setCancelled(true);

		if (!(event.getEntity() instanceof Player) || ((Player) event.getEntity()).getGameMode() == GameMode.SPECTATOR)
			return;
		Player player = (Player) event.getEntity();

		Vector away = player.getLocation().toVector().subtract(event.getDamager().getLocation().toVector());
		double distance = away.length();
		away.setY(0);
		if (away.lengthSquared() < 1.0E-4)
			away = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);

		double closeness = 1.0 - Math.min(distance, NUKE_KNOCKBACK_MAX_DISTANCE) / NUKE_KNOCKBACK_MAX_DISTANCE;
		Vector push = away.normalize().multiply(NUKE_KNOCKBACK_BASE + NUKE_KNOCKBACK_RANGE * closeness);
		player.setVelocity(push.setY(NUKE_KNOCKBACK_VERTICAL));
	}

	// ---------------------------------------------------------------- the fake Pokeballs' feeble mobs

	/** A fake Zombie has 1 health: whatever hits it kills it, however little damage that does. */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onFeebleZombieHurt(EntityDamageEvent event) {
		if (event.getEntity() instanceof Zombie && FakeLoot.isMarked(event.getEntity()) && event.getDamage() > 0)
			event.setDamage(Math.max(event.getDamage(), 1000.0));
	}

	/** A fake Skeleton has no bow, so even if one ended up in its hand it can't shoot. */
	@EventHandler(ignoreCancelled = true)
	public void onFakeSkeletonShoot(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Skeleton && FakeLoot.isMarked(event.getEntity()))
			event.setCancelled(true);
	}

	// ---------------------------------------------------------------- the fake Hammer and the changed knockback

	/**
	 * A hit with the fake Hammer: its Knockback 10 is cut down for who it hits (see onVelocity), and the one
	 * who swung it is pushed back instead. Also starts the Notch Apple's extra knockback for whoever is under it.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHit(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player victim = (Player) event.getEntity();
		long now = System.currentTimeMillis();

		if (event.getDamager() instanceof Player) {
			Player attacker = (Player) event.getDamager();
			ItemStack hand = attacker.getItemInHand();
			if (hand != null && hand.getType() == Material.IRON_SWORD && FakeLoot.isFake(hand)) {
				double regularHit = REGULAR_HIT_HORIZONTAL + (attacker.isSprinting() ? REGULAR_HIT_SPRINT_BONUS : 0);
				tweaks.put(victim.getUniqueId(), new Tweak(now + TWEAK_MS, true, regularHit));
				recoil(attacker, victim);
				return;
			}
		}

		Long penaltyUntil = notchPenaltyUntil.get(victim.getUniqueId());
		if (penaltyUntil != null) {
			if (penaltyUntil < now)
				notchPenaltyUntil.remove(victim.getUniqueId());
			else
				tweaks.put(victim.getUniqueId(), new Tweak(now + TWEAK_MS, false, 0));
		}
	}

	/** Pushes the attacker away from who they hit. A tick later, after the hit has slowed them down. */
	private void recoil(final Player attacker, Player victim) {
		Vector back = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).setY(0);
		if (back.lengthSquared() < 1.0E-4)
			return;
		final Vector push = back.normalize().multiply(HAMMER_RECOIL_HORIZONTAL).setY(HAMMER_RECOIL_VERTICAL);

		Bukkit.getScheduler().runTask(main, new Runnable() {
			@Override
			public void run() {
				if (attacker.isOnline())
					attacker.setVelocity(push);
			}
		});
	}

	/**
	 * Changes the knockback a hit just gave, before it is sent to the player.
	 * <p>
	 * It starts from the player's own velocity, not the event's: for a melee hit the server fires this event with the
	 * velocity from BEFORE the hit, while the knockback itself is already in the player's motion (and is what gets sent
	 * unless the event changes it).
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onVelocity(PlayerVelocityEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		Tweak tweak = tweaks.remove(uuid);
		if (tweak == null || tweak.until < System.currentTimeMillis())
			return;

		Vector velocity = event.getPlayer().getVelocity();
		if (tweak.clamp) {
			double horizontal = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
			if (horizontal > tweak.maxHorizontal) {
				double scale = tweak.maxHorizontal / horizontal;
				velocity.setX(velocity.getX() * scale);
				velocity.setZ(velocity.getZ() * scale);
			}
			if (velocity.getY() > REGULAR_HIT_VERTICAL)
				velocity.setY(REGULAR_HIT_VERTICAL);
		} else {
			velocity.setX(velocity.getX() * NOTCH_KNOCKBACK_HORIZONTAL);
			velocity.setZ(velocity.getZ() * NOTCH_KNOCKBACK_HORIZONTAL);
			velocity.setY(velocity.getY() * NOTCH_KNOCKBACK_VERTICAL);
		}
		event.setVelocity(velocity);
	}
}
