package anthony.SuperCraftBrawl.ranks;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /ranksub give <player> <rank> <time>  - one more payment: 1m (a month), 12m or 1y (a year), 28d, 2w, or a number of days.
 *                                         The same as "/setrank <player> <rank> <time>", which is what to use by hand.
 * /ranksub end <player>                 - ends it now (refund, chargeback), they go back to the rank they had before
 * /ranksub info <player>                - what they have and when it ends
 *
 * The console (the store) always passes the permission check.
 */
public class RankSubscriptionCommand implements CommandExecutor {

	private static final String PERMISSION = "scb.setrank";

	private final Core main;

	public RankSubscriptionCommand(Core main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission(PERMISSION)) {
			sender.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return true;
		}

		RankSubscriptionManager manager = main.getRankSubscriptionManager();
		if (manager == null || !manager.isEnabled()) {
			sender.sendMessage(main.color("&c&l(!) &rRank subscriptions are unavailable, the database table didn't start."));
			return true;
		}

		if (args.length >= 4 && args[0].equalsIgnoreCase("give")) {
			manager.giveFromCommand(sender, args[1], args[2], args[3]);
			return true;
		}
		if (args.length >= 2 && args[0].equalsIgnoreCase("end")) {
			manager.end(sender, args[1]);
			return true;
		}
		if (args.length >= 2 && args[0].equalsIgnoreCase("info")) {
			manager.info(sender, args[1]);
			return true;
		}

		sender.sendMessage(main.color("&e/" + label + " give <player> <rank> <time> &7- one more payment, e.g. LEGENDARY 1m or 1y"));
		sender.sendMessage(main.color("&e/" + label + " end <player> &7- end it now, they go back to their old rank"));
		sender.sendMessage(main.color("&e/" + label + " info <player>"));
		return true;
	}
}
