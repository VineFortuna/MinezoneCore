package anthony.SuperCraftBrawl.ranks;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /ranksub give <player> <rank> <time>  - one more payment: 1m (a month), 12m or 1y (a year), 30d, 2w, or a number of days
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
			Rank rank = Rank.getRankFromName(args[2]);
			// getRankFromName falls back to DEFAULT for a name it doesn't know
			if (rank == Rank.DEFAULT && !args[2].equalsIgnoreCase("default")) {
				sender.sendMessage(main.color("&c&l(!) &r" + args[2] + " is not a rank."));
				return true;
			}
			RankSubscriptionManager.Period period = RankSubscriptionManager.parsePeriod(args[3]);
			if (period == null) {
				sender.sendMessage(main.color("&c&l(!) &r" + args[3] + " is not a time. Use 1m, 12m, 1y, 30d, 2w or a number of days."));
				return true;
			}
			manager.give(sender, args[1], rank, period);
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
