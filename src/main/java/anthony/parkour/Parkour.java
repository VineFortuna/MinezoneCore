package anthony.parkour;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Parkour implements Listener {

	private Core main;
	public Map<Player, Arenas> players;
	public Map<Player, Integer> checkpoint;
	public Map<Player, FastBoard> b;
	public Map<Player, String> time;
	public Map<Player, BukkitRunnable> runnables;

	public Parkour(Core main) {
		this.main = main;
		this.players = new HashMap<Player, Arenas>();
		this.checkpoint = new HashMap<Player, Integer>();
		this.b = new HashMap<Player, FastBoard>();
		this.time = new HashMap<Player, String>();
		this.runnables = new HashMap<Player, BukkitRunnable>();
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}

	public void addPlayer(Player player, Arenas arena) {
		if (!hasPlayer(player)) {
			players.put(player, arena);

			double timeTaken = 0.0;
			DecimalFormat decimalFormat = new DecimalFormat("#.#");
			String formattedTime = decimalFormat.format(timeTaken);
			time.put(player, formattedTime);

			player.sendMessage(main.color("&e&l(!) &rYou have joined &r&l" + arena.getName()));

			player.getInventory().clear();
			gameBoard(player);
			gameItems(player);
			timeTicking(player);
			player.setAllowFlight(false);
		} else {
			player.sendMessage(main.color("&c&l(!) &rYou are already in parkour mode!"));
		}
	}

	private void timeTicking(Player player) {
		BukkitRunnable r = new BukkitRunnable() {
			double timeTaken = 0.0;

			@Override
			public void run() {
				if (runnables.get(player) != null) {
					DecimalFormat decimalFormat = new DecimalFormat("#.##");
					String formattedTime = decimalFormat.format(timeTaken);
					time.put(player, formattedTime);
					b.get(player).updateLine(3, main.color("&r&lTime:&7 " + formattedTime + "s"));

					timeTaken += 0.25;
				} else {
					this.cancel();
					runnables.remove(player);
				}
			}
		};
		r.runTaskTimer(main, 0, 5);
		runnables.put(player, r);
	}

	public void gameBoard(Player player) {
		FastBoard b = new FastBoard(player);
		b.updateTitle(main.color("&e&lPARKOUR"));
		b.updateLines("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
				main.color(
						"&r&lCheckpoints: &7" + (checkpoint.containsKey(player) ? checkpoint.get(player) + 1 : 0) +
						"/" + players.get(player).getCheckpoints()),
				"", main.color("&r&lTime:&7 0.0s"),
				"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
				main.color("&7minezone.club"));
		this.b.put(player, b);
	}

	public void gameItems(Player player) {
		player.getInventory().setItem(0,
				ItemHelper.setDetails(new ItemStack(Material.BEACON), main.color("&7Return to Checkpoint")));
		player.getInventory().setItem(1,
				ItemHelper.setDetails(new ItemStack(Material.SEA_LANTERN), main.color("&7Return to Start")));
		player.getInventory().setItem(2, ItemHelper.setDetails(new ItemStack(Material.BARRIER), main.color("&cLeave")));
	}

/*	public boolean isInBounds(Player player, Location loc, Arenas arena) {
		ArenaInstance mapInstance = arena.getInstance();
		Vector v = mapInstance.center;
		Location centre = new Location(main.getLobbyWorld(), v.getX(), v.getY(), v.getZ());
		double boundsX = mapInstance.boundsX;
		double boundsY = mapInstance.boundsY;
		double boundsZ = mapInstance.boundsZ;

		if (Math.abs(centre.getX() - loc.getX()) > boundsX)
			return false;
		if (Math.abs(centre.getY() - loc.getY()) > boundsY)
			return false;
		if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ)
			return false;
		return true;
	}*/

	public boolean hasPlayer(Player player) {
		return players.containsKey(player);
	}

	// Events

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.getWorld() == main.getLobbyWorld()) {
			if (hasPlayer(player)) {
				ArenaInstance arenaInstance = players.get(player).getInstance();
				if (player.getLocation().getY() < 50)
					teleportToCheckpoint(player);

				if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BEACON) {
					Vector blockVector = player.getLocation().getBlock().getLocation().toVector();

					if (arenaInstance.checkpoints.contains(blockVector)) { // Check if checkpoint exists
						int newCheckpointIndex = arenaInstance.checkpoints.indexOf(blockVector);
						Integer currentCheckpointIndex = checkpoint.get(player);

						if (currentCheckpointIndex == null || newCheckpointIndex != currentCheckpointIndex) {
							checkpoint.put(player, newCheckpointIndex);
							b.get(player).updateLine(1, main.color("&r&lCheckpoints: &7" + (newCheckpointIndex + 1) + "/"
									+ players.get(player).getCheckpoints()));
							player.sendMessage(main.color("&e&l(!) &rCheckpoint set!"));
						}
					}

					/*
					 * } else if (player.getLocation().getBlock().getRelative(BlockFace.DOWN)
					 * .getType() == Material.GLOWSTONE) { player.sendTitle("PARKOUR COMPLETE!",
					 * main.color("&eSending to spawn..."));
					 * main.getParkour().players.remove(player); checkpointNum.remove(player);
					 * checkpoint.remove(player); b.remove(player); main.ResetPlayer(player);
					 * main.LobbyItems(player); main.LobbyBoard(player);
					 * player.setAllowFlight(true); }
					 */
				} else if (isPlayerInLava(player)) {
					teleportToCheckpoint(player);
					player.sendMessage(main.color("&e&l(!) &rSent back to checkpoint"));
					player.setFireTicks(0);
				}
				if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SEA_LANTERN) {
					for (Arenas arena : Arenas.values()) {
						if (player.getLocation().toVector().toBlockVector().equals(arena.getInstance().endLoc.toBlockVector())) {
							this.removePlayer(player);
							return;
						}
					}
				}
			} else {
				if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SEA_LANTERN) {
					for (Arenas arena : Arenas.values()) {
						if (player.getLocation().toVector().toBlockVector().equals(arena.getInstance().startLoc.toBlockVector())) {
							this.addPlayer(player, arena);
							return;
						}
					}
				}
			}
		}
	}

	private boolean isPlayerInLava(Player player) {
		Material material = player.getLocation().getBlock().getType();
		return material == Material.LAVA || material == Material.STATIONARY_LAVA;
	}
	
	public void removePlayer(Player player) {
		this.time.remove(player);
		this.runnables.remove(player);
		main.getParkour().players.remove(player);
		main.getScoreboardManager().lobbyBoard(player);
		player.getInventory().clear();
		main.LobbyItems(player);
		player.sendMessage(main.color("&r&l(!) &rYou have left parkour mode"));
		player.setAllowFlight(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Material item = event.getMaterial();

		if (hasPlayer(player)) {
			if (item != null) {
				switch (item) {
				case BEACON:
					teleportToCheckpoint(player);
					player.sendMessage(main.color("&e&l(!) &rSent back to checkpoint"));
					break;
				case SEA_LANTERN:
					teleportToStart(player);
					checkpoint.remove(player);

					runnables.get(player).cancel();
					runnables.remove(player);
					timeTicking(player);

					gameBoard(player);
					player.sendMessage(main.color("&e&l(!) &rSent back to start"));
					break;
				case BARRIER:
					removePlayer(player);
				}
			}
		}
	}

	public void teleportToStart(Player player) {
		Vector v = players.get(player).getInstance().startLoc;
		player.teleport(new Location(main.getLobbyWorld(), v.getBlockX(), v.getBlockY(), v.getBlockZ()));
	}

	public void teleportToCheckpoint(Player player) {
		if (checkpoint.containsKey(player)) {
			player.teleport(players.get(player).getCheckpoint(checkpoint.get(player)).toLocation(player.getWorld()));
		} else {
			teleportToStart(player);
		}
	}

}
