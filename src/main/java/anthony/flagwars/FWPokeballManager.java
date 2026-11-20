package anthony.flagwars;

import anthony.SuperCraftBrawl.Game.projectile.AABBCollider;
import anthony.flagwars.FWGameInstance.PokeballMob;
import anthony.flagwars.items.PokeballItem;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Zombie / Skeleton Pokeballs in Flag Wars. The throw is the one Super Craft Bros uses for its Pokeballs
 * (ProjectileManager): the egg is dropped as an item flying at 2x the thrower's look direction, and the mob is
 * spawned where it stops against a block, runs into a player, or after 10 seconds at the latest. The mobs
 * are made the same way too (named after the thrower, Skeleton with a Punch bow, aimed at the nearest player
 * within 150 blocks).
 *
 * What's different here is that it's a team game: the ball flies through teammates, and the mobs fight for the
 * thrower's team (they only target enemy players, can't hurt teammates, and teammates can't hurt them). Kills they
 * make count for whoever threw them. The mobs wear the team's color (a Zombie full leather armor, a Skeleton a
 * leather chestplate) and are taken away after PokeballItem.MOB_LIFETIME_SECONDS.
 */
public class FWPokeballManager implements Listener {

	/** Same numbers as ProjectileManager / the Pokeball item handlers in Super Craft Bros. */
	private static final double THROW_SPEED = 2.0;
	private static final int MAX_AGE_TICKS = 20 * 10;
	private static final double TARGET_RANGE = 150;
	/** A mob is taken away this long after it was spawned, if it hasn't died by then. */
	private static final int MOB_LIFETIME_TICKS = 20 * PokeballItem.MOB_LIFETIME_SECONDS;
	private static final Vector PLAYER_BOX = new Vector(0.3, 0.8, 0.3);
	private static final Vector BALL_BOX = new Vector(0.3, 0.3, 0.3);

	private final FWGameManager fwGameManager;

	public FWPokeballManager(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
	}

	// ---------------------------------------------------------------- throwing

	/** Uses up one Pokeball from the player's hand and throws it. */
	public void throwPokeball(Player player, FWGameInstance instance, PokeballItem.Type type) {
		FWTeam team = instance.findTeamOf(player);
		if (team == null) return;

		ItemStack hand = player.getItemInHand();
		if (hand.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			hand.setAmount(hand.getAmount() - 1);
		}

		World world = player.getWorld();
		Location eye = player.getEyeLocation();

		// A random name so thrown balls never stack together
		ItemStack stack = ItemHelper.createMonsterEgg(type.getEntityType(), 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(UUID.randomUUID().toString());
		stack.setItemMeta(meta);

		Item ball = world.dropItem(eye, stack);
		ball.setVelocity(player.getLocation().getDirection().multiply(THROW_SPEED));
		ball.setPickupDelay(Integer.MAX_VALUE); // nobody can pick it up while it flies

		UUID ownerId = player.getUniqueId();
		String ownerName = player.getName();

		new BukkitRunnable() {
			private Location lastLoc = eye;
			private Vector lastVel = new Vector(0, 0, 0);
			private int age = 0;

			@Override
			public void run() {
				if (instance.getState() != FWGameState.IN_PROGRESS || !ball.isValid()
						|| fwGameManager.getInstanceForWorld(world) != instance || age++ > MAX_AGE_TICKS) {
					end();
					return;
				}

				Location now = ball.getLocation();
				Vector velocity = ball.getVelocity();

				if (hitsEnemy(instance, team, lastLoc, now) || stopped(velocity, lastVel)) {
					// Only if the thrower is still on the team (they may have left while it flew)
					if (team.getMembers().contains(ownerId)) spawnMob(instance, team, ownerId, ownerName, type, now);
					end();
					return;
				}

				lastLoc = now;
				lastVel = velocity;
			}

			private void end() {
				ball.remove();
				cancel();
			}
		}.runTaskTimer(fwGameManager.getMain(), 1L, 1L);
	}

	/** An enemy player in the path the ball took since last tick (the same box test Super Craft Bros uses). */
	private boolean hitsEnemy(FWGameInstance instance, FWTeam throwerTeam, Location from, Location to) {
		Vector direction = to.clone().subtract(from).toVector();

		for (Player player : to.getWorld().getPlayers()) {
			if (!isEnemyOf(instance, throwerTeam.getSiteId(), player)) continue;

			AABBCollider collider = new AABBCollider(player.getLocation().add(0, 0.8, 0).toVector(), PLAYER_BOX.clone());
			if (collider.calculateIntersection(from.toVector(), direction.clone(), BALL_BOX.clone(), true)) return true;
		}
		return false;
	}

	/** It landed: a velocity that was moving and now isn't (the ball ran into a block). */
	private boolean stopped(Vector now, Vector before) {
		return didStop(now.getX(), before.getX()) || didStop(now.getY(), before.getY()) || didStop(now.getZ(), before.getZ());
	}

	private boolean didStop(double newValue, double oldValue) {
		return Math.abs(oldValue) > 0.05 && Math.abs(newValue) < 0.01;
	}

	// ---------------------------------------------------------------- the mobs

	private void spawnMob(FWGameInstance instance, FWTeam team, UUID ownerId, String ownerName,
						  PokeballItem.Type type, Location where) {
		LivingEntity mob = (LivingEntity) where.getWorld().spawnEntity(where, type.getEntityType());
		PokeballMob info = instance.addPokeballMob(mob, ownerId, team);
		// Taken away when its time is up. The match forgetting the mob (it died, or the match ended) cancels this.
		info.setDespawnTask(Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(),
				() -> despawnMob(instance, mob), MOB_LIFETIME_TICKS));

		mob.setRemoveWhenFarAway(false);
		mob.setCanPickupItems(false);
		mob.setCustomName(ChatColorHelper.color("&c" + ownerName + "'s &e" + typeName(type.getEntityType())));
		mob.setCustomNameVisible(true);

		Color teamColor = team.getColor().getColor();
		if (mob instanceof Zombie) armorZombie((Zombie) mob, teamColor);
		else if (mob instanceof Skeleton) equipSkeleton((Skeleton) mob, teamColor);

		if (mob instanceof Creature) {
			Player target = nearestEnemy(instance, team.getSiteId(), mob.getLocation());
			if (target != null) ((Creature) mob).setTarget(target);
		}
	}

	private String typeName(EntityType entityType) {
		return entityType == EntityType.ZOMBIE ? "Zombie" : "Skeleton";
	}

	/** Full leather armor in the team's color and a diamond sword. */
	private void armorZombie(Zombie zombie, Color teamColor) {
		zombie.setBaby(false);

		EntityEquipment equipment = zombie.getEquipment();
		equipment.setHelmet(teamLeather(Material.LEATHER_HELMET, teamColor));
		equipment.setChestplate(teamLeather(Material.LEATHER_CHESTPLATE, teamColor));
		equipment.setLeggings(teamLeather(Material.LEATHER_LEGGINGS, teamColor));
		equipment.setBoots(teamLeather(Material.LEATHER_BOOTS, teamColor));
		equipment.setItemInHand(ItemHelper.setUnbreakable(ItemHelper.create(Material.DIAMOND_SWORD)));
	}

	/** A Punch bow and a leather chestplate in the team's color. */
	private void equipSkeleton(Skeleton skeleton, Color teamColor) {
		ItemStack punchBow = ItemHelper.create(Material.BOW);
		punchBow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
		ItemHelper.setUnbreakable(punchBow);

		EntityEquipment equipment = skeleton.getEquipment();
		equipment.setItemInHand(punchBow);
		equipment.setChestplate(teamLeather(Material.LEATHER_CHESTPLATE, teamColor));
	}

	/** A piece of leather armor dyed in the team's color, unbreakable like everything else the mobs carry. */
	private ItemStack teamLeather(Material piece, Color teamColor) {
		return ItemHelper.setUnbreakable(ItemHelper.setColor(new ItemStack(piece), teamColor));
	}

	/** The mob's time is up: take it away and tell its owner, unless it already died or the match ended and forgot it. */
	private void despawnMob(FWGameInstance instance, LivingEntity mob) {
		PokeballMob info = instance.getPokeballMob(mob);
		if (info == null) return;

		instance.removePokeballMob(mob);
		mob.remove();

		Player owner = Bukkit.getPlayer(info.owner);
		if (owner != null && instance.hasPlayer(owner)) {
			owner.sendMessage(fwGameManager.getMain().color("&2&l(!) &rYour &a" + typeName(mob.getType()) + " &rhas despawned!"));
		}
	}

	/** The closest enemy player (in the match, alive) within range of the mob, or null. */
	private Player nearestEnemy(FWGameInstance instance, int teamSiteId, Location from) {
		Player nearest = null;
		double nearestSquared = TARGET_RANGE * TARGET_RANGE;

		for (Player player : from.getWorld().getPlayers()) {
			if (!isEnemyOf(instance, teamSiteId, player)) continue;

			double squared = player.getLocation().distanceSquared(from);
			if (squared < nearestSquared) {
				nearest = player;
				nearestSquared = squared;
			}
		}
		return nearest;
	}

	/** A player in this match, alive (not spectating or waiting to respawn), and not on the given team. */
	private boolean isEnemyOf(FWGameInstance instance, int teamSiteId, Player player) {
		if (!instance.hasPlayer(player) || player.getGameMode() == GameMode.SPECTATOR) return false;

		FWTeam team = instance.findTeamOf(player);
		return team != null && team.getSiteId() != teamSiteId;
	}

	// ---------------------------------------------------------------- who owns what

	/** The Pokeball mob this entity is, or the one that shot it (an arrow), or null. */
	private PokeballMob mobBehind(FWGameInstance instance, Entity entity) {
		Entity source = entity;
		if (entity instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) entity).getShooter();
			if (shooter instanceof Entity) source = (Entity) shooter;
		}
		return instance.getPokeballMob(source);
	}

	/** The player behind this damager, if it's a Pokeball mob (or its arrow): whoever threw it, when they're online. */
	public Player ownerOf(Entity damager) {
		FWGameInstance instance = fwGameManager.getInstanceForWorld(damager.getWorld());
		if (instance == null) return null;

		PokeballMob mob = mobBehind(instance, damager);
		return mob == null ? null : Bukkit.getPlayer(mob.owner);
	}

	/** The player who is attacking: the player themselves, or the one who shot the arrow. */
	private Player playerBehind(Entity damager) {
		if (damager instanceof Player) return (Player) damager;
		if (damager instanceof Projectile) {
			ProjectileSource shooter = ((Projectile) damager).getShooter();
			if (shooter instanceof Player) return (Player) shooter;
		}
		return null;
	}

	// ---------------------------------------------------------------- events

	/** The mobs only go after enemy players, and look for the next one when theirs is gone. */
	@EventHandler
	public void onTarget(EntityTargetLivingEntityEvent event) {
		Entity entity = event.getEntity();
		FWGameInstance instance = fwGameManager.getInstanceForWorld(entity.getWorld());
		if (instance == null) return;

		PokeballMob info = instance.getPokeballMob(entity);
		if (info == null) return;

		LivingEntity target = event.getTarget();
		if (target == null) {
			retargetNextTick(instance, info, entity);
			return;
		}

		if (!(target instanceof Player) || !isEnemyOf(instance, info.teamSiteId, (Player) target)) {
			event.setCancelled(true);
		}
	}

	private void retargetNextTick(FWGameInstance instance, PokeballMob info, Entity entity) {
		Bukkit.getScheduler().runTaskLater(fwGameManager.getMain(), new Runnable() {
			@Override
			public void run() {
				if (!entity.isValid() || !(entity instanceof Creature)) return;

				Creature creature = (Creature) entity;
				if (creature.getTarget() != null) return;

				Player next = nearestEnemy(instance, info.teamSiteId, creature.getLocation());
				if (next != null) creature.setTarget(next);
			}
		}, 1L);
	}

	/** No friendly fire either way: a mob can't hurt its team, and the team can't hurt its mobs. */
	@EventHandler(priority = EventPriority.LOW)
	public void onDamage(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		FWGameInstance instance = fwGameManager.getInstanceForWorld(victim.getWorld());
		if (instance == null) return;

		PokeballMob attackerMob = mobBehind(instance, event.getDamager());
		if (attackerMob != null) {
			if (victim instanceof Player) {
				FWTeam victimTeam = instance.findTeamOf((Player) victim);
				if (victimTeam != null && victimTeam.getSiteId() == attackerMob.teamSiteId) event.setCancelled(true);
			} else {
				PokeballMob victimMob = instance.getPokeballMob(victim);
				if (victimMob != null && victimMob.teamSiteId == attackerMob.teamSiteId) event.setCancelled(true);
			}
			return;
		}

		PokeballMob victimMob = instance.getPokeballMob(victim);
		if (victimMob == null) return;

		Player attacker = playerBehind(event.getDamager());
		if (attacker == null) return;

		FWTeam attackerTeam = instance.findTeamOf(attacker);
		if (attackerTeam != null && attackerTeam.getSiteId() == victimMob.teamSiteId) event.setCancelled(true);
	}

	/** A dead mob is forgotten (the match also forgets all of them when it ends). */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onMobDeath(EntityDeathEvent event) {
		FWGameInstance instance = fwGameManager.getInstanceForWorld(event.getEntity().getWorld());
		if (instance != null) instance.removePokeballMob(event.getEntity());
	}
}
