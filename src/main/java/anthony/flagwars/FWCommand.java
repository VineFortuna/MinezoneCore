package anthony.flagwars;

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

		Player player = (Player) sender;

		if (args.length == 0) {
			sendHelp(player);
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "join": {
				FWGameMode mode = FWGameMode.SOLO;
				if (args.length >= 2) {
					if (args[1].equalsIgnoreCase("duo") || args[1].equalsIgnoreCase("duos")) {
						mode = FWGameMode.DUO;
					} else if (!args[1].equalsIgnoreCase("solo") && !args[1].equalsIgnoreCase("solos")) {
						player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rTry: &e/fw join <solo|duo>"));
						break;
					}
				}
				fwGameManager.joinGame(player, mode);
				break;
			}

			case "leave":
				if (!fwGameManager.leaveGame(player)) {
					player.sendMessage(fwGameManager.getMain().color("&c&l(!) &rYou are not in Flag Wars!"));
				} else {
					player.sendMessage(fwGameManager.getMain().color("&2&l(!) &rYou left Flag Wars."));
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
		player.sendMessage(fwGameManager.getMain().color("&6&lFLAG WARS COMMANDS"));
		player.sendMessage(fwGameManager.getMain().color("&e/fw join <solo|duo> -> &rJoin a match (defaults to solo)"));
		player.sendMessage(fwGameManager.getMain().color("&e/fw leave -> &rLeave the match"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Arrays.asList("join", "leave", "help"), new ArrayList<>());
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
			return StringUtil.copyPartialMatches(args[1], Arrays.asList("solo", "duo"), new ArrayList<>());
		}

		return Collections.emptyList();
	}
}
