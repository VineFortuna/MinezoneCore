package anthony.flagdefense;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FDCommand implements CommandExecutor, TabExecutor {

	private final FDGameManager fdGameManager;

	public FDCommand(FDGameManager fdGameManager) {
		this.fdGameManager = fdGameManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You can't use this in the console!");
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0) {
			sendHelp(player);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "join":
				fdGameManager.joinGame(player);
				break;

			case "leave":
				if (!fdGameManager.leaveGame(player)) {
					player.sendMessage(fdGameManager.getMain().color("&c&l(!) &rYou are not in FlagDefense!"));
				} else {
					player.sendMessage(fdGameManager.getMain().color("&2&l(!) &rYou left FlagDefense."));
				}
				break;

			case "help":
				sendHelp(player);
				break;

			default:
				sendHelp(player);
				break;
		}

		return true;
	}

	private void sendHelp(Player player) {
		player.sendMessage(fdGameManager.getMain().color("&6&lFLAGDEFENSE COMMANDS"));
		player.sendMessage(fdGameManager.getMain().color("&e/fd join -> &rJoin the match"));
		player.sendMessage(fdGameManager.getMain().color("&e/fd leave -> &rLeave the match"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Arrays.asList("join", "leave", "help"), new ArrayList<>());
		}
		return Collections.emptyList();
	}
}
