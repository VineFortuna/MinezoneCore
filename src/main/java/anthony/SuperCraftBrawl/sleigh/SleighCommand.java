package anthony.SuperCraftBrawl.sleigh;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /sleigh toggles Santa's sleigh on and off.
 * Experimental, so it needs the scb.sleigh permission (ops have it). To open it up to everyone, remove
 * the permission check below.
 */
public class SleighCommand implements CommandExecutor {

	private static final String PERMISSION = "scb.sleigh";

	private final Core main;

	public SleighCommand(Core main) {
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

		SleighManager manager = main.getSleighManager();
		if (manager == null) {
			player.sendMessage(main.color("&c&l(!) &rThe sleigh is currently unavailable."));
			return true;
		}

		manager.toggle(player);
		return true;
	}
}
