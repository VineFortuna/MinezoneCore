package anthony.villagerdefense;

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

public class VDCommand implements CommandExecutor, TabExecutor {

	private final VDGameManager vdGameManager;

	public VDCommand(VDGameManager vdGameManager) {
		this.vdGameManager = vdGameManager;
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
				vdGameManager.joinGame(player);
				break;

			case "leave":
				if (!vdGameManager.leaveGame(player)) {
					player.sendMessage(vdGameManager.getMain().color("&c&l(!) &rYou are not in VillagerDefense!"));
				} else {
					player.sendMessage(vdGameManager.getMain().color("&2&l(!) &rYou left VillagerDefense."));
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
		player.sendMessage(vdGameManager.getMain().color("&6&lVILLAGERDEFENSE COMMANDS"));
		player.sendMessage(vdGameManager.getMain().color("&e/vd join -> &rJoin the match"));
		player.sendMessage(vdGameManager.getMain().color("&e/vd leave -> &rLeave the match"));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Arrays.asList("join", "leave", "help"), new ArrayList<>());
		}
		return Collections.emptyList();
	}
}
