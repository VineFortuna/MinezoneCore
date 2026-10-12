package anthony.flagwars;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FWCommand implements CommandExecutor, TabExecutor {

	private final FWGameManager fwGameManager;

	public FWCommand(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You can't use this in the console!");
			return true;
		}

		sendHelp((Player) sender);
		return true;
	}

	/** Join/leave moved to the shared /join and /leave commands - this is just a pointer to those now. */
	private void sendHelp(Player player) {
		player.sendMessage(fwGameManager.getMain().color("&6&lFLAG WARS COMMANDS"));
		player.sendMessage(fwGameManager.getMain().color("&e/join flagwars <solo|duo> -> &rJoin a match (defaults to solo)"));
		player.sendMessage(fwGameManager.getMain().color("&e/leave -> &rLeave the match"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Collections.singletonList("help"), new ArrayList<>());
		}

		return Collections.emptyList();
	}
}
