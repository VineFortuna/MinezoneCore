package anthony.SuperCraftBrawl.sleigh;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Santa's sleigh in the lobby, toggled with /sleigh. Getting out of the seat (Sneak) switches it off.
 * One repeating task moves every active sleigh; it only runs while someone has one out.
 */
public class SleighManager implements Listener {

	private final Core main;
	private final Map<UUID, Sleigh> byPlayer = new HashMap<>();
	private final Map<UUID, Sleigh> bySeat = new HashMap<>();
	private final Map<UUID, Sleigh> byHorse = new HashMap<>();
	private BukkitTask task;

	public SleighManager(Core main) {
		this.main = main;
		removeLeftovers();
		main.getServer().getPluginManager().registerEvents(this, main);
	}

	public boolean isRiding(Player player) {
		return byPlayer.containsKey(player.getUniqueId());
	}

	/** Switches the player's sleigh on or off. Returns true if it's now on. */
	public boolean toggle(Player player) {
		if (isRiding(player)) {
			stop(player, true);
			return false;
		}
		return start(player);
	}

	private boolean start(Player player) {
		if (!main.isInLobby(player)) {
			player.sendMessage(main.color("&c&l(!) &rYou can only use the sleigh in the lobby!"));
			return false;
		}
		if (player.isInsideVehicle()) {
			player.sendMessage(main.color("&c&l(!) &rGet off what you're riding first!"));
			return false;
		}

		Sleigh sleigh = new Sleigh(main, player);
		boolean started;
		try {
			started = sleigh.start();
		} catch (Throwable t) {
			main.getLogger().warning("[Sleigh] Could not start " + player.getName() + "'s sleigh: " + t);
			t.printStackTrace();
			started = false;
		}

		if (!started) {
			sleigh.remove();
			player.sendMessage(main.color("&c&l(!) &rThere isn't enough room here for the sleigh, try somewhere more open."));
			return false;
		}

		register(player, sleigh);
		startTask();

		player.sendMessage(main.color("&6&l(!) &rYour sleigh is ready! Hold &eW &rto go where you look. "
				+ "Sneak to get off, or type &e/sleigh &ragain."));
		return true;
	}

	private void register(Player player, Sleigh sleigh) {
		byPlayer.put(player.getUniqueId(), sleigh);
		bySeat.put(sleigh.getSeat().getUniqueId(), sleigh);
		byHorse.put(sleigh.getHorse().getUniqueId(), sleigh);
	}

	private void forget(UUID owner, Sleigh sleigh) {
		byPlayer.remove(owner);
		if (sleigh.getSeat() != null)
			bySeat.remove(sleigh.getSeat().getUniqueId());
		if (sleigh.getHorse() != null)
			byHorse.remove(sleigh.getHorse().getUniqueId());
	}

	/** Takes the player's sleigh down (does nothing if they don't have one out). */
	public void stop(Player player, boolean announce) {
		Sleigh sleigh = byPlayer.get(player.getUniqueId());
		if (sleigh == null)
			return;

		// Forget it first, so the dismount that removing it causes isn't handled as the rider getting off
		forget(player.getUniqueId(), sleigh);
		Location exit = announce ? sleigh.exitLocation() : null;
		sleigh.remove();

		if (byPlayer.isEmpty())
			stopTask();

		if (player.isOnline()) {
			if (exit != null)
				player.teleport(exit); // out of the sleigh and onto the ground beside it
			if (announce)
				player.sendMessage(main.color("&6&l(!) &rYour sleigh was put away."));
		}
	}

	/** Call from onDisable: the horses and stands would otherwise be saved into the lobby world. */
	public void shutdown() {
		for (Sleigh sleigh : new ArrayList<>(byPlayer.values()))
			sleigh.remove();
		byPlayer.clear();
		bySeat.clear();
		byHorse.clear();
		stopTask();
	}

	// ---------------------------------------------------------------- task

	private void startTask() {
		if (task != null)
			return;

		task = new BukkitRunnable() {
			@Override
			public void run() {
				for (Map.Entry<UUID, Sleigh> entry : new ArrayList<>(byPlayer.entrySet())) {
					Sleigh sleigh = entry.getValue();
					boolean alive;
					try {
						alive = sleigh.tick();
					} catch (Throwable t) {
						main.getLogger().warning("[Sleigh] Error moving a sleigh, removing it: " + t);
						t.printStackTrace();
						alive = false;
					}

					if (!alive) {
						Player player = Bukkit.getPlayer(entry.getKey());
						if (player != null) {
							stop(player, true);
						} else {
							forget(entry.getKey(), sleigh);
							sleigh.remove();
							if (byPlayer.isEmpty())
								stopTask();
						}
					}
				}
			}
		}.runTaskTimer(main, 1L, 1L);
	}

	private void stopTask() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	// ---------------------------------------------------------------- events

	/*
	 * Sneaking to get out of the seat switches the sleigh off. An armor stand isn't a Vehicle, so Bukkit's
	 * VehicleExitEvent never fires for it: this is Spigot's event for any entity getting off any other.
	 * (The tick also checks the rider is still seated, so this is just the quick path.)
	 */
	@EventHandler
	public void onDismount(EntityDismountEvent event) {
		if (!(event.getEntity() instanceof Player) || !bySeat.containsKey(event.getDismounted().getUniqueId()))
			return;

		final Player player = (Player) event.getEntity();
		// Not removed right now: the dismount is still in progress, so let it finish first
		Bukkit.getScheduler().runTask(main, new Runnable() {
			@Override
			public void run() {
				stop(player, true);
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		stop(event.getPlayer(), false);
	}

	/** The horse can't be hurt. */
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (byHorse.containsKey(event.getEntity().getUniqueId()))
			event.setCancelled(true);
	}

	/** Nobody can open the horse's inventory, feed it, ride it or take its saddle. */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (event.getInventory().getHolder() instanceof Horse
				&& byHorse.containsKey(((Horse) event.getInventory().getHolder()).getUniqueId()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEntityEvent event) {
		if (byHorse.containsKey(event.getRightClicked().getUniqueId()))
			event.setCancelled(true);
	}

	// ---------------------------------------------------------------- cleanup

	/** Horses and stands left behind by a crash (they're saved with the lobby world) are named MARKER_NAME. */
	private void removeLeftovers() {
		World lobby = main.getLobbyWorld();
		if (lobby == null)
			return;

		List<Entity> leftovers = new ArrayList<>();
		for (Entity entity : lobby.getEntities()) {
			if ((entity instanceof ArmorStand || entity instanceof Horse) && Sleigh.MARKER_NAME.equals(entity.getCustomName()))
				leftovers.add(entity);
		}
		for (Entity entity : leftovers)
			entity.remove();

		if (!leftovers.isEmpty())
			main.getLogger().info("[Sleigh] Removed " + leftovers.size() + " leftover sleigh entities.");
	}
}
