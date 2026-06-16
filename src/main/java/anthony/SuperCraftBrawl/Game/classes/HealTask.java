package anthony.SuperCraftBrawl.Game.classes;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Game.GameInstance;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class HealTask implements Runnable {

	private static final int TOTAL_DURATION_TICKS = 20 * 3;
	private static final int TASK_INTERVAL_TICKS = 2;
	private static final int HEAL_INTERVAL_TICKS = 20;

	private static final double HEAL_AMOUNT = 1.0; // 0.5 hearts per second
	private static final double HEAL_RANGE = 10.0;
	private static final double HEAL_RANGE_SQUARED = HEAL_RANGE * HEAL_RANGE;

	/*
	 * Lower = beam updates more often.
	 * Higher = less particle trail.
	 *
	 * 4 ticks = updates 5 times per second.
	 */
	private static final int BEAM_REDRAW_INTERVAL_TICKS = 4;

	private final Player player;
	private final EnderCrystal crystal;
	private final GameInstance instance;

	private int elapsedTicks = 0;
	private int nextHealTick = HEAL_INTERVAL_TICKS;
	private int healsGiven = 0;
	private int beamTicks = 0;

	private BukkitTask task;
	private boolean cleanedUp = false;

	public HealTask(Player player, EnderCrystal crystal, GameInstance instance) {
		this.player = player;
		this.crystal = crystal;
		this.instance = instance;
	}

	public void set(BukkitTask task) {
		this.task = task;
	}

	@Override
	public void run() {
		if (shouldCancel()) {
			cleanup();
			return;
		}

		Location playerLocation = player.getLocation();
		Location crystalLocation = crystal.getLocation().add(0, 1, 0);

		if (playerLocation.getWorld() == null || crystalLocation.getWorld() == null
				|| !playerLocation.getWorld().equals(crystalLocation.getWorld())) {
			cleanup();
			return;
		}

		double distanceSquared = playerLocation.distanceSquared(crystalLocation);

		elapsedTicks += TASK_INTERVAL_TICKS;
		beamTicks += TASK_INTERVAL_TICKS;

		/*
		 * Do not draw a full new beam every single task tick.
		 * This makes it look more like one beam following the player
		 * instead of multiple old lines stacking behind them.
		 */
		if (distanceSquared <= HEAL_RANGE_SQUARED && beamTicks >= BEAM_REDRAW_INTERVAL_TICKS) {
			beamTicks = 0;
			playHealParticles(crystalLocation.clone(), getPlayerBeamTarget(player));
		}

		if (elapsedTicks >= nextHealTick) {
			if (distanceSquared <= HEAL_RANGE_SQUARED) {
				healPlayer();
			}

			nextHealTick += HEAL_INTERVAL_TICKS;
		}

		if (elapsedTicks >= TOTAL_DURATION_TICKS || healsGiven >= 3) {
			cleanup();
		}
	}

	private Location getPlayerBeamTarget(Player player) {
		return player.getLocation().clone().add(0, 1.0, 0);
	}

	private void healPlayer() {
		if (player.getHealth() >= player.getMaxHealth()) {
			return;
		}

		player.setHealth(Math.min(player.getHealth() + HEAL_AMOUNT, player.getMaxHealth()));
		healsGiven++;
	}

	private boolean shouldCancel() {
		if (cleanedUp) {
			return true;
		}

		if (player == null || crystal == null) {
			return true;
		}

		if (!player.isOnline() || player.isDead() || player.getHealth() <= 0.0 || player.getGameMode() == GameMode.SPECTATOR) {
			return true;
		}

		if (crystal.isDead() || !crystal.isValid()) {
			return true;
		}

		if (instance == null || !instance.players.contains(player) || !instance.classes.containsKey(player)) {
			return true;
		}

		BaseClass playerClass = instance.classes.get(player);

		return playerClass == null
				|| playerClass.getType() != ClassType.EnderDragon
				|| playerClass.getLives() <= 0
				|| playerClass.isDead;
	}

	private void playHealParticles(Location from, Location to) {
		Vector direction = to.toVector().subtract(from.toVector());
		double distance = direction.length();

		if (distance <= 0.1) {
			sendParticle(from);
			return;
		}

		/*
		 * Larger step = fewer particles per beam.
		 * This reduces old particle trails when the player moves.
		 */
		Vector step = direction.normalize().multiply(0.45);
		Location current = from.clone();

		int steps = Math.min(80, (int) (distance / 0.45));

		for (int i = 0; i <= steps; i++) {
			sendParticle(current);
			current.add(step);
		}
	}

	private void sendParticle(Location location) {
		/*
		 * SPELL_WITCH is purple and fades much faster than PORTAL.
		 * PORTAL particles drift and stay around too long, which makes the
		 * beam look like multiple old lines when the player moves.
		 */
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
				EnumParticle.SPELL_WITCH,
				true,
				(float) location.getX(),
				(float) location.getY(),
				(float) location.getZ(),
				0.01F,
				0.01F,
				0.01F,
				0.0F,
				1
		);

		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
		}
	}

	private void cleanup() {
		if (cleanedUp) {
			return;
		}

		cleanedUp = true;

		if (crystal != null && !crystal.isDead()) {
			crystal.remove();
		}

		if (task != null) {
			task.cancel();
		}
	}
}