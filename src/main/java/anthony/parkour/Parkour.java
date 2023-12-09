package anthony.parkour;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

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

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;

public class Parkour implements Listener {

	private Core main;
	public Map<Player, Arenas> players;
	public Map<Player, Location> checkpoint;
	public Map<Player, Integer> checkpointNum;
	public Map<Player, FastBoard> b;
	public Map<Player, String> time;
	public Map<Player, BukkitRunnable> runnables;

	public Parkour(Core main) {
		this.main = main;
		this.players = new HashMap<Player, Arenas>();
		this.checkpoint = new HashMap<Player, Location>();
		this.checkpointNum = new HashMap<Player, Integer>();
		this.b = new HashMap<Player, FastBoard>();
		this.time = new HashMap<Player, String>();
		this.runnables = new HashMap<Player, BukkitRunnable>();
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}

	public void AddPlayer(Player player, Arenas arena) {
		if (!hasPlayer(player)) {
			Vector ai = arena.getInstance().spawnLoc;
			players.put(player, arena);
			checkpoint.put(player, new Location(main.getLobbyWorld(), ai.getBlockX(), ai.getBlockY(), ai.getBlockZ()));
			checkpointNum.put(player, 0);
			double timeTaken = 0.0;
			DecimalFormat decimalFormat = new DecimalFormat("#.#");
			String formattedTime = decimalFormat.format(timeTaken);
			time.put(player, formattedTime);
			player.sendMessage(main.color("&e&l(!) &rYou have joined &r&l" + arena.toString()));
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
						"&r&lCheckpoints: &7" + checkpointNum.get(player) + "/" + players.get(player).getCheckpoints()),
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

	public boolean isInBounds(Player player, Location loc, Arenas arena) {
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
	}

	public boolean hasPlayer(Player player) {
		return players.containsKey(player);
	}

	// Events

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (player.getWorld() == main.getLobbyWorld()) {
			if (hasPlayer(player)) {
				if (!(isInBounds(player, player.getLocation(), players.get(player))))
					player.teleport(checkpoint.get(player));

				if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BEACON) {
					if (!(checkpoint.containsValue(player.getLocation()))) {
						Location old = checkpoint.get(player);
						int x = (int) old.getX();
						int y = (int) old.getY();
						int z = (int) old.getZ();
						int otherx = (int) player.getLocation().getX();
						int othery = (int) player.getLocation().getY();
						int otherz = (int) player.getLocation().getZ();
						if (x == otherx && y == othery && z == otherz)
							return;

						checkpoint.put(player, player.getLocation());
						checkpointNum.put(player, checkpointNum.get(player) + 1);
						b.get(player).updateLine(1, main.color("&r&lCheckpoints: &7" + checkpointNum.get(player) + "/"
								+ players.get(player).getCheckpoints()));
						player.sendMessage(main.color("&e&l(!) &rCheckpoint set!"));
					}
					/*
					 * } else if (player.getLocation().getBlock().getRelative(BlockFace.DOWN)
					 * .getType() == Material.GLOWSTONE) { player.sendTitle("PARKOUR COMPLETE!",
					 * main.color("&eSending to spawn.."));
					 * main.getParkour().players.remove(player); checkpointNum.remove(player);
					 * checkpoint.remove(player); b.remove(player); main.ResetPlayer(player);
					 * main.LobbyItems(player); main.LobbyBoard(player);
					 * player.setAllowFlight(true); }
					 */
				} else if (isPlayerInLava(player)) {
					Vector v = players.get(player).getInstance().spawnLoc;
					player.teleport(new Location(main.getLobbyWorld(), v.getBlockX(), v.getBlockY(), v.getBlockZ()));
					checkpoint.put(player,
							new Location(main.getLobbyWorld(), v.getBlockX(), v.getBlockY(), v.getBlockZ()));
					checkpointNum.put(player, 0);
					gameBoard(player);
					player.sendMessage(main.color("&e&l(!) &rSent back to start"));
				}
			} else {
				if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SEA_LANTERN) {
					for (Arenas arena : Arenas.values()) {
						if (isInBounds(player, player.getLocation(), arena)) {
							this.AddPlayer(player, arena);
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

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Material item = event.getMaterial();

		if (hasPlayer(player)) {
			if (item != null) {
				switch (item) {
				case BEACON:
					player.teleport(checkpoint.get(player));
					player.sendMessage(main.color("&e&l(!) &rSent back to checkpoint"));
					break;
				case SEA_LANTERN:
					Vector v = players.get(player).getInstance().spawnLoc;
					player.teleport(new Location(main.getLobbyWorld(), v.getBlockX(), v.getBlockY(), v.getBlockZ()));
					checkpoint.put(player,
							new Location(main.getLobbyWorld(), v.getBlockX(), v.getBlockY(), v.getBlockZ()));
					checkpointNum.put(player, 0);
					gameBoard(player);
					player.sendMessage(main.color("&e&l(!) &rSent back to start"));
					break;
				case BARRIER:
					this.time.remove(player);
					this.runnables.remove(player);
					main.getParkour().players.remove(player);
					main.ResetPlayer(player);
					main.LobbyItems(player);
					main.LobbyBoard(player);
					player.sendMessage(main.color("&r&l(!) &rYou have left parkour mode"));
					player.setAllowFlight(true);
					break;
				}
			}
		}
	}

}
