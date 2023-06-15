package anthony.SuperCraftBrawl.Game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Main;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;

public class SmmManager implements Listener {

	// Super Minezone Maker

	private final Main main;
	List<BukkitRunnable> runnables = new ArrayList<>();
	private BukkitRunnable time;
	private int ticks = 0;
	private FastBoard board;
	private World SmmWorld;

	public SmmManager(Main main) {
		this.main = main;
		this.main.getServer().getPluginManager().registerEvents(this, main);
		InitialiseMap();
	}

	public Main getMain() {
		return main;
	}

	public void InitialiseMap() {
		WorldCreator w = new WorldCreator("SMM").environment(World.Environment.NORMAL);
		w.generator(new VoidGenerator());
		SmmWorld = Bukkit.getServer().createWorld(w);
	}

	public World getWorld() {
		return SmmWorld;
	}

	// Epic Minecraft Quiz Level
	public void SendPlayerToQuiz(Player player) {
		player.teleport(new Location(getWorld(), -78.497, 163, 353.468));
		player.sendTitle("PLAY!", "Reach the level Diamond Block!");
		player.getInventory().clear();
		player.setAllowFlight(false);
		checkBlock(player);
		QuizGameBoard(player);
	}

	public void QuizGameBoard(Player player) {
		board = new FastBoard(player);
		PlayerData data = getMain().getDataManager().getPlayerData(player);

		if (data != null) {
			board.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + "SUPER MINEZONE MAKER");
			if (data.bestTime == 0) {
				board.updateLines("" + ChatColor.GRAY + ChatColor.BOLD + "Game:", "Epic Minecraft Quiz", "",
						"" + ChatColor.GRAY + ChatColor.BOLD + "Best Time:", "None", "",
						"" + ChatColor.GRAY + ChatColor.BOLD + "Your Time:", "" + ticks + "s");
			} else {
				board.updateLines("" + ChatColor.GRAY + ChatColor.BOLD + "Game:", "Epic Minecraft Quiz", "",
						"" + ChatColor.GRAY + ChatColor.BOLD + "Best Time:", "" + data.bestTime + "s", "",
						"" + ChatColor.GRAY + ChatColor.BOLD + "Your Time:", "" + ticks + "s");
			}
		}
	}

	public boolean isInBounds(Location loc) {
		Vector v = new Vector(-78.500, 174, 387.500);
		Location centre = new Location(SmmWorld, v.getX(), v.getY(), v.getZ());
		double boundsX = 10, boundsY = 20, boundsZ = 40;

		if (Math.abs(centre.getX() - loc.getX()) > boundsX)
			return false;
		if (Math.abs(centre.getY() - loc.getY()) > boundsY)
			return false;
		if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ)
			return false;
		return true;
	}

	public void checkBlock(Player player) {
		if (time == null) {
			time = new BukkitRunnable() {

				@Override
				public void run() {
					if (ticks >= 0) {
						final Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
						board.updateLine(7, "" + ticks + "s");
						if (block.getType() == Material.DIAMOND_BLOCK) {
							player.sendTitle("LEVEL CLEARED!", "Time Taken: " + ChatColor.YELLOW + ticks + "s");
							PlayerData data = getMain().getDataManager().getPlayerData(player);

							if (data.bestTime != 0) {
								if (ticks < data.bestTime) {
									data.bestTime = ticks;
									player.sendMessage(
											"" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
													+ "New personal best! " + ChatColor.YELLOW + data.bestTime + "s");
								} else {
									player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
											+ ChatColor.RESET + "No new personal best, rip :(");
								}
							} else {
								data.bestTime = ticks;
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ "New personal best! " + ChatColor.YELLOW + data.bestTime + "s");
							}

							getMain().SendPlayerToHub(player);
							getMain().LobbyBoard(player);
							getMain().LobbyItems(player);
							player.setAllowFlight(true);
							time = null;
							this.cancel();
						}
					}

					if (player.getWorld() != getWorld()) {
						time = null;
						this.cancel();
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
								+ "You have left Epic Minecraft Quiz");
					}

					ticks++;
				}
			};
			time.runTaskTimer(getMain(), 0, 20);
			runnables.add(time);
		}
	}

	@EventHandler
	public void bounds(PlayerMoveEvent event) {
		if (event.getPlayer().getWorld() == getWorld()) {
			if (!(isInBounds(event.getPlayer().getLocation()))) {
				SendPlayerToQuiz(event.getPlayer());
				this.ticks = 0;
				event.getPlayer().sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "You have died! Respawning..");
			}
		}
	}
}
