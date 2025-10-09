package anthony.SuperCraftBrawl.halloween;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrickTitleCommand implements CommandExecutor, TabExecutor {

	// Works with either your passenger or packet manager — both expose
	// enable/disable/toggle/isEnabled
	private final TrickTitlePackets manager;

	public TrickTitleCommand(TrickTitlePackets manager) {
		this.manager = manager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Players only.");
			return true;
		}
		Player p = (Player) sender;

		// Permission gate
		if (!p.hasPermission("cosmetic.tricktitle")) {
			p.sendMessage(ChatColor.RED + "You don't have access to this title.");
			return true;
		}

		String sub = (args.length == 0) ? "toggle" : args[0].toLowerCase();

		switch (sub) {
		case "on": {
			if (manager.isEnabled(p)) {
				p.sendMessage(ChatColor.GOLD + "Trick-or-Treater " + ChatColor.YELLOW + "is already enabled.");
				return true;
			}
			manager.toggleTitle(p, "trick");
			p.sendMessage(ChatColor.GOLD + "Trick-or-Treater " + ChatColor.GREEN + "enabled");
			return true;
		}
		case "off": {
			if (!manager.isEnabled(p)) {
				p.sendMessage(ChatColor.GOLD + "Trick-or-Treater " + ChatColor.YELLOW + "is already disabled.");
				return true;
			}
			manager.toggleTitle(p, "trick");
			p.sendMessage(ChatColor.GOLD + "Trick-or-Treater " + ChatColor.RED + "disabled");
			return true;
		}
		case "toggle":
		default: {
			boolean nowOn = manager.toggleTitle(p, "trick");
			p.sendMessage(ChatColor.GOLD + "Trick-or-Treater "
					+ (nowOn ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
			return true;
		}
		}
	}

	// Tab complete: /tricktitle [on|off|toggle]
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length == 1) {
			String a = args[0].toLowerCase();
			return Arrays.asList("on", "off", "toggle").stream().filter(opt -> opt.startsWith(a)).toList();
		}
		return Collections.emptyList();
	}
}