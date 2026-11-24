package anthony.SuperCraftBrawl.christmas;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Staff testing command:
 *   /presentreset [player] - wipes their Present Hunt progress so every present can be found again
 * Defaults to yourself.
 */
public class PresentResetCommand implements CommandExecutor {

	private static final String ADMIN_PERMISSION = "scb.christmas.admin";

	private final Core main;

	public PresentResetCommand(Core main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.isOp() && !sender.hasPermission(ADMIN_PERMISSION)) {
			sender.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return true;
		}

		ChristmasHuntManager hunt = main.getChristmasManager();
		if (hunt == null) {
			sender.sendMessage(main.color("&c&l(!) &rThe Present Hunt is currently unavailable."));
			return true;
		}

		Player target;
		if (args.length > 0) {
			target = Bukkit.getPlayerExact(args[0]);
			if (target == null) {
				sender.sendMessage(main.color("&c&l(!) &r" + args[0] + " is not online."));
				return true;
			}
		} else if (sender instanceof Player) {
			target = (Player) sender;
		} else {
			sender.sendMessage("Usage: /" + label + " <player>");
			return true;
		}

		hunt.getDao().reset(target.getUniqueId());
		hunt.refreshLobbyBoard(target);

		target.sendMessage(main.color("&6&l(!) &rYour Present Hunt progress was reset."));
		if (!target.equals(sender))
			sender.sendMessage(main.color("&6&l(!) &rReset &e" + target.getName() + "&r's Present Hunt progress."));
		return true;
	}
}
