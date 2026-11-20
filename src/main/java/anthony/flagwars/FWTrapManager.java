package anthony.flagwars;

import anthony.flagwars.map.FWTeamSite;
import anthony.flagwars.shop.FWTrap;
import anthony.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Fires a team's queued traps on enemies inside its base. Mirrors Hypixel Bedwars traps: the front
 * trap of the queue is consumed when an enemy is in the base, then the team's traps are on a
 * cooldown before the next one can fire.
 *
 * This is a repeating check, not a one-off "stepped across the line" event. That way it also
 * catches an enemy who was already standing in the base when the trap was bought, one who stands
 * still, and one who teleports in (ender pearl, Teleport Bow), none of which a movement event sees.
 */
public class FWTrapManager {

	public static final int MAX_QUEUE_SIZE = 3;

	/** After a trap fires, the same team's next queued trap waits this long (Hypixel uses 30s too). Also stops one enemy standing in the base from burning the whole queue at once. */
	private static final long TRAP_COOLDOWN_MS = 30_000L;
	private static final long CHECK_INTERVAL_TICKS = 5L;
	/** Used only when a map never set this island's boundary (see /fwsetup boundary1/boundary2): same radius the Heal Pool falls back to. */
	private static final double FALLBACK_RADIUS = 8.0;

	/** The name that marks the bucket an MLG Trap hands out (see giveMlgBucket). */
	private static final String MLG_BUCKET_NAME = "MLG Water Bucket";
	private static final long MLG_CHECK_TICKS = 2L;
	/** Longest the watch waits for the landing before taking the bucket back anyway. */
	private static final int MLG_MAX_WATCH_TICKS = 15 * 20;
	/** If the launch hasn't lifted them off the ground by now (a low ceiling), they count as landed. */
	private static final int MLG_LAUNCH_GRACE_TICKS = 10;

	private final FWGameInstance instance;
	/** siteId → System.currentTimeMillis() when that team's traps can fire again. */
	private final Map<Integer, Long> cooldownEndsAt = new HashMap<>();
	/** Players launched by an MLG Trap who haven't landed yet. Each entry is removed when its watch ends. */
	private final Map<UUID, BukkitTask> mlgWatches = new HashMap<>();
	private final FWTemporaryBlocks tempBlocks;
	private final FWDecoyFlags decoys;
	private BukkitTask task;

	private static final long CAGE_TICKS = 4 * 20L;

	public FWTrapManager(FWGameInstance instance) {
		this.instance = instance;
		this.tempBlocks = new FWTemporaryBlocks(instance);
		this.decoys = new FWDecoyFlags(instance);
	}

	public void start() {
		if (task != null) return;
		task = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), this::checkAll,
				CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
	}

	/** Stops the check and forgets every cooldown. Safe to call when it was never started. */
	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		cooldownEndsAt.clear();
		for (BukkitTask watch : mlgWatches.values()) watch.cancel();
		mlgWatches.clear();
		tempBlocks.clearAll();
		decoys.clearAll();
	}

	/**
	 * Called when a player breaks a block. Returns true if it was one of a trap's blocks (the cage),
	 * which the caller then removes without dropping anything (see FWPlayerListener#onBlockBreak).
	 */
	public boolean takeTrapBlock(Block block) {
		return tempBlocks.take(block);
	}

	/**
	 * Called when a player clicks. Returns true if they were aiming at a decoy flag, so the click shouldn't
	 * also do something else (see FWPlayerListener#onClickFlag).
	 */
	public boolean clickDecoy(Player player) {
		return decoys.click(player);
	}

	/** Why the team can't set a decoy flag right now (short plain text), or null if it can. See FWShopGUI. */
	public String decoyProblem(FWTeam owner) {
		return decoys.problem(owner);
	}

	/** Sets the decoy flag. It is bought and used on the spot, never queued (see FWTrap#isInstant). */
	public void armDecoy(FWTeam owner) {
		decoys.arm(owner);
	}

	/** Called when a team's real flag is stolen (see FWFlagManager#attemptSteal), which ends its decoy. */
	public void onFlagStolen(FWTeam flagOwner) {
		decoys.cancel(flagOwner);
	}

	private void checkAll() {
		if (instance.getState() != FWGameState.IN_PROGRESS) return;
		for (FWTeam defender : instance.getTeams().values()) {
			tryTrigger(defender, false);
		}
	}

	/**
	 * Called the moment a team buys a trap: if an enemy is already in the base it goes off right
	 * now, even when another trap went off in the last 30 seconds (the cooldown only paces traps
	 * that were already waiting in the queue, it never makes a fresh purchase sit there doing nothing).
	 */
	public void onTrapPurchased(FWTeam defender) {
		logPurchaseCheck(defender);
		tryTrigger(defender, true);
	}

	/** Fires the team's next trap if it has one ready and an enemy is in its base right now. */
	private void tryTrigger(FWTeam defender, boolean ignoreCooldown) {
		if (instance.getState() != FWGameState.IN_PROGRESS) return;
		if (defender.getTrapQueueSize() == 0 || defender.isInactive()) return;
		if (!ignoreCooldown && System.currentTimeMillis() < cooldownEndsAt.getOrDefault(defender.getSiteId(), 0L)) return;

		FWTeamSite site = instance.getMapConfig().getTeamSite(defender.getSiteId());
		for (FWTeam other : instance.getTeams().values()) {
			if (other == defender) continue;

			for (UUID uuid : other.getMembers()) {
				Player intruder = Bukkit.getPlayer(uuid);
				if (!canTriggerTraps(intruder)) continue;
				if (!isInBase(site, intruder.getLocation())) continue;

				triggerNextTrap(defender, intruder);
				return;
			}
		}
	}

	/** Alive, playing (respawning players are in GameMode.SPECTATOR) and not still under spawn protection. */
	private boolean canTriggerTraps(Player player) {
		return player != null
				&& player.isOnline()
				&& player.getGameMode() == GameMode.SURVIVAL
				&& !instance.getManager().isSpawnProtected(player);
	}

	/** Inside the island's footprint at any height (same rule flag capture uses), or near the flag if the map never set a boundary. */
	private boolean isInBase(FWTeamSite site, Location location) {
		if (site.hasBoundary()) return site.isWithinFootprint(location);

		Location flag = site.getFlagLoc();
		return flag != null
				&& flag.getWorld().equals(location.getWorld())
				&& flag.distanceSquared(location) <= FALLBACK_RADIUS * FALLBACK_RADIUS;
	}

	/**
	 * One console line per trap purchase saying exactly what the trigger saw: the queue, the
	 * cooldown, and for every enemy their game mode, spawn protection and whether they count as in
	 * the base. If a trap ever sits there doing nothing, this line says why.
	 */
	private void logPurchaseCheck(FWTeam defender) {
		FWTeamSite site = instance.getMapConfig().getTeamSite(defender.getSiteId());
		long cooldownLeftMs = Math.max(0L, cooldownEndsAt.getOrDefault(defender.getSiteId(), 0L) - System.currentTimeMillis());

		StringBuilder line = new StringBuilder("[FW traps] ").append(defender.getName())
				.append(" bought a trap: queue=").append(defender.getTrapQueueSize())
				.append(" inactive=").append(defender.isInactive())
				.append(" state=").append(instance.getState())
				.append(" cooldownLeft=").append(cooldownLeftMs / 1000).append("s")
				.append(" boundary=").append(site.hasBoundary())
				.append(" enemies=[");

		boolean first = true;
		for (FWTeam other : instance.getTeams().values()) {
			if (other == defender) continue;
			for (UUID uuid : other.getMembers()) {
				Player enemy = Bukkit.getPlayer(uuid);
				if (!first) line.append(", ");
				first = false;
				if (enemy == null) {
					line.append(uuid).append(" offline");
					continue;
				}
				line.append(enemy.getName()).append(" mode=").append(enemy.getGameMode())
						.append(" spawnProt=").append(instance.getManager().isSpawnProtected(enemy))
						.append(" inBase=").append(isInBase(site, enemy.getLocation()));
			}
		}
		line.append("]");

		instance.getManager().getMain().getLogger().info(line.toString());
	}

	private void triggerNextTrap(FWTeam defender, Player intruder) {
		FWTrap trap = defender.pollTrap();
		if (trap == null) return;

		cooldownEndsAt.put(defender.getSiteId(), System.currentTimeMillis() + TRAP_COOLDOWN_MS);

		tellDefendersTriggered(defender, intruder, trap);
		applyTrap(trap, defender, intruder);
		tellIntruderTriggered(intruder, trap);
		intruder.playSound(intruder.getLocation(), Sound.ENDERDRAGON_GROWL, 0.5f, 1.5f);
	}

	/** The chat line (and alert sound) the defending team gets when someone sets off one of their traps. */
	private void tellDefendersTriggered(FWTeam defender, Player intruder, FWTrap trap) {
		for (UUID uuid : defender.getMembers()) {
			Player member = Bukkit.getPlayer(uuid);
			if (member == null) continue;
			member.sendMessage(instance.getManager().getMain().color(
					"&c&l(!) &e" + intruder.getName() + " &rtriggered your &e" + trap.getDisplayName() + "&r!"));
			member.playSound(member.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1.8f);
		}
	}

	/** The chat line the person who set off a trap gets. */
	private void tellIntruderTriggered(Player intruder, FWTrap trap) {
		intruder.sendMessage(instance.getManager().getMain().color(
				"&c&l(!) &rYou triggered a &e" + trap.getDisplayName() + "&r!"));
	}

	/**
	 * The same "triggered" chat lines every trap sends, for a trap that isn't set off by walking into the
	 * base. The Decoy Flag Trap uses it when someone grabs the decoy. No extra sound for the intruder.
	 */
	public void announceTriggered(FWTeam defender, Player intruder, FWTrap trap) {
		tellDefendersTriggered(defender, intruder, trap);
		tellIntruderTriggered(intruder, trap);
	}

	/**
	 * The victim gets a water bucket to MLG with and it's already in their hand, since the fall is only
	 * a few seconds. FWPlayerListener#onBucketEmpty already takes the empty bucket back one tick after
	 * any water is placed. If they land without using it, watchMlgLanding takes it back instead, so it
	 * is never a free item. The bucket has its own name so only this one is taken back, never a Water
	 * Bucket they bought from the shop.
	 */
	private void giveMlgBucket(Player target) {
		PlayerInventory inventory = target.getInventory();
		ItemStack bucket = mlgBucket();
		Item dropped = null;

		Map<Integer, ItemStack> noRoom = inventory.addItem(bucket);
		if (!noRoom.isEmpty()) {
			// Inventory completely full: drop it where they are (and take it back on landing too)
			dropped = target.getWorld().dropItemNaturally(target.getLocation(), bucket);
		} else {
			int slot = firstMlgBucketSlot(inventory);
			if (slot >= 0 && slot < 9) inventory.setHeldItemSlot(slot);
		}

		watchMlgLanding(target, dropped);
	}

	private ItemStack mlgBucket() {
		return ItemHelper.setDetails(new ItemStack(Material.WATER_BUCKET), "&b" + MLG_BUCKET_NAME,
				"", "&7Gone when you land.");
	}

	private boolean isMlgBucket(ItemStack item) {
		if (item == null || item.getType() != Material.WATER_BUCKET || !item.hasItemMeta()) return false;
		String name = item.getItemMeta().getDisplayName();
		return name != null && ChatColor.stripColor(name).equals(MLG_BUCKET_NAME);
	}

	private int firstMlgBucketSlot(PlayerInventory inventory) {
		for (int slot = 0; slot < inventory.getSize(); slot++) {
			if (isMlgBucket(inventory.getItem(slot))) return slot;
		}
		return -1;
	}

	/**
	 * Waits for the launched player to come back down, then takes the MLG bucket back if it's still
	 * around. Also ends if they leave, die, stop playing or it runs too long, so the task can't pile up.
	 * The bucket is taken back in every one of those cases.
	 */
	private void watchMlgLanding(Player target, Item dropped) {
		UUID id = target.getUniqueId();
		BukkitTask previous = mlgWatches.remove(id);
		if (previous != null) previous.cancel();

		BukkitTask watch = new BukkitRunnable() {
			int ticks = 0;
			boolean wasAirborne = false;

			@Override
			public void run() {
				ticks += (int) MLG_CHECK_TICKS;
				Player player = Bukkit.getPlayer(id);

				boolean finished = player == null || !player.isOnline() || player.isDead()
						|| player.getGameMode() != GameMode.SURVIVAL
						|| instance.getState() != FWGameState.IN_PROGRESS
						|| ticks >= MLG_MAX_WATCH_TICKS;
				if (!finished) {
					if (!player.isOnGround()) {
						wasAirborne = true;
					} else if (wasAirborne || ticks >= MLG_LAUNCH_GRACE_TICKS) {
						// Back on the ground, or the launch never got them off it (low ceiling)
						finished = true;
					}
				}
				if (!finished) return;

				cancel();
				mlgWatches.remove(id);
				if (dropped != null) dropped.remove();
				if (player != null) takeBackMlgBucket(player);
			}
		}.runTaskTimer(instance.getManager().getMain(), MLG_CHECK_TICKS, MLG_CHECK_TICKS);

		mlgWatches.put(id, watch);
	}

	/** Removes any MLG bucket the player still has, in their inventory or on their cursor. */
	private void takeBackMlgBucket(Player player) {
		PlayerInventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getSize(); slot++) {
			if (isMlgBucket(inventory.getItem(slot))) inventory.setItem(slot, null);
		}
		if (isMlgBucket(player.getItemOnCursor())) player.setItemOnCursor(new ItemStack(Material.AIR));
		player.updateInventory();
	}

	private void applyTrap(FWTrap trap, FWTeam defender, Player target) {
		switch (trap) {
			case CAGE:
				buildCage(defender, target);
				break;
			case MINING_FATIGUE:
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10 * 20, 2), true);
				break;
			case MLG:
				target.setVelocity(new Vector(0, 2.5, 0));
				giveMlgBucket(target);
				break;
			case SLOWNESS_BLINDNESS:
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,      6 * 20, 0), true);
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 6 * 20, 0), true);
				break;
		}
	}

	/**
	 * A 3x3 shell around the intruder: iron bars for the walls (two blocks high) and the ceiling, and
	 * wool in the color of the team that set the trap for the floor, because you can fall through a floor
	 * of bars. It clears after 4 seconds, or sooner if they break a bar. Bars take a long time to break
	 * by hand, much less with a pickaxe, so breaking out costs them time. Only empty blocks are filled,
	 * and nothing is placed where a player is standing, so nobody is trapped inside a block.
	 */
	@SuppressWarnings("deprecation")
	private void buildCage(FWTeam defender, Player target) {
		Block feet = target.getLocation().getBlock();

		// Stand them in the middle of their block first, so a wall never lands on part of their body
		if (feet.getType() == Material.AIR && feet.getRelative(0, 1, 0).getType() == Material.AIR) {
			Location middle = target.getLocation();
			middle.setX(feet.getX() + 0.5);
			middle.setZ(feet.getZ() + 0.5);
			target.teleport(middle);
		}
		Set<Block> occupied = blocksOccupiedByPlayers(target);

		List<Block> floor = new ArrayList<>();
		List<Block> bars = new ArrayList<>();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				for (int dy = -1; dy <= 2; dy++) {
					boolean wall = dx != 0 || dz != 0;
					boolean floorOrCeiling = dy == -1 || dy == 2;
					if (!wall && !floorOrCeiling) continue; // the space the intruder stands in

					Block block = feet.getRelative(dx, dy, dz);
					if (occupied.contains(block)) continue;
					if (dy == -1) floor.add(block);
					else bars.add(block);
				}
			}
		}

		tempBlocks.place(floor, Material.WOOL, defender.getColor().getWoolData(), CAGE_TICKS);
		tempBlocks.place(bars, Material.IRON_FENCE, (byte) 0, CAGE_TICKS);
		target.getWorld().playSound(target.getLocation(), Sound.ANVIL_LAND, 0.6f, 1.2f);
	}

	/** Every block the target's body, or any other player's near them, is touching, so a cage never fills in around someone. */
	private Set<Block> blocksOccupiedByPlayers(Player target) {
		List<Player> bodies = new ArrayList<>();
		bodies.add(target);
		for (Entity entity : target.getNearbyEntities(4, 4, 4)) {
			if (entity instanceof Player) bodies.add((Player) entity);
		}

		Set<Block> occupied = new HashSet<>();
		for (Player body : bodies) {
			Location at = body.getLocation();
			int minX = (int) Math.floor(at.getX() - 0.3), maxX = (int) Math.floor(at.getX() + 0.3);
			int minZ = (int) Math.floor(at.getZ() - 0.3), maxZ = (int) Math.floor(at.getZ() + 0.3);
			int minY = (int) Math.floor(at.getY()), maxY = (int) Math.floor(at.getY() + 1.8);
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						occupied.add(at.getWorld().getBlockAt(x, y, z));
					}
				}
			}
		}
		return occupied;
	}
}
