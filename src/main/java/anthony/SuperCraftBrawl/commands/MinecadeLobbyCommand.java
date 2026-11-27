package anthony.SuperCraftBrawl.commands;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Staff command: /minecadelobby (or /mcl) takes you to the world called MinecadeLobby, to 12 147 9.
 * If that world isn't loaded but its folder is in the server folder, it is loaded first. It never creates a world:
 * a folder that isn't there is an error, not a new empty world.
 */
public class MinecadeLobbyCommand implements CommandExecutor {

	private static final String WORLD_NAME = "MinecadeLobby";
	private static final String PERMISSION = "scb.minecadelobby";
	/** Where it takes you: the block 12 147 9 of that world. */
	private static final int X = 12;
	private static final int Y = 147;
	private static final int Z = 9;

	private final Core main;

	public MinecadeLobbyCommand(Core main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Players only.");
			return true;
		}
		Player player = (Player) sender;

		if (!player.isOp() && !player.hasPermission(PERMISSION)) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return true;
		}

		// Moving someone out of a game or the parkour from under it would leave them half in it
		if (main.isPlayerInAnyGame(player) || main.getParkour().hasPlayer(player)) {
			player.sendMessage(main.color("&c&l(!) &rLeave your game or parkour first."));
			return true;
		}

		World world = Bukkit.getWorld(WORLD_NAME);
		if (world == null) {
			File folder = new File(Bukkit.getWorldContainer(), WORLD_NAME);
			if (!new File(folder, "level.dat").exists()) {
				player.sendMessage(main.color("&c&l(!) &rThere is no world called &e" + WORLD_NAME
						+ "&r, there is no folder with that name (and a level.dat in it) in the server folder."));
				return true;
			}

			player.sendMessage(main.color("&e&l(!) &rLoading &e" + WORLD_NAME + "&r..."));
			world = Bukkit.createWorld(new WorldCreator(WORLD_NAME));
			if (world == null) {
				player.sendMessage(main.color("&c&l(!) &rCouldn't load &e" + WORLD_NAME + "&r, see the console."));
				return true;
			}
		}

		// The middle of the block 12 147 9
		player.teleport(new Location(world, X + 0.5, Y, Z + 0.5));
		player.sendMessage(main.color("&a&l(!) &rYou were taken to &e" + WORLD_NAME + "&r. &7(/hub takes you back)"));
		return true;
	}
}
