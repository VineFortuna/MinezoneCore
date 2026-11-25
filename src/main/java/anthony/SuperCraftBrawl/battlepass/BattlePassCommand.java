package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.battlepass.BattlePassGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * /battlepass                              - open the menu (the quests are a button in its corner)
 * /battlepass completequest <player> <n>   - admin, finishes quest number n (or a quest id)
 * /battlepass givexp <player> <amount>      - admin
 * /battlepass setxp <player> <amount>       - admin
 * /battlepass premium <player> <true|false> - admin
 * /battlepass reset <player>                - admin, wipes their progress for this season
 */
public class BattlePassCommand implements CommandExecutor {

	private static final String ADMIN_PERMISSION = "scb.battlepass.admin";

	private final Core main;

	public BattlePassCommand(Core main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		BattlePassManager manager = main.getBattlePassManager();
		if (manager == null || !manager.isEnabled()) {
			sender.sendMessage(main.color("&c&l(!) &rThe Battle Pass is currently unavailable."));
			return true;
		}

		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Players only.");
				return true;
			}
			Player player = (Player) sender;
			if (manager.getProgress(player) == null) {
				player.sendMessage(main.color("&c&l(!) &rYour Battle Pass is still loading, try again in a moment!"));
				return true;
			}
			new BattlePassGUI(main).open(player);
			return true;
		}

		if (!sender.isOp() && !sender.hasPermission(ADMIN_PERMISSION)) {
			sender.sendMessage(main.color("&c&l(!) &rUsage: /" + label));
			return true;
		}

		String sub = args[0].toLowerCase();
		if (args.length < 2) {
			sendAdminHelp(sender, label);
			return true;
		}

		// The store runs this one: it works for players who are offline or still loading too
		if (sub.equals("premium")) {
			setPremium(sender, args, label, manager);
			return true;
		}

		Player target = Bukkit.getPlayerExact(args[1]);
		if (target == null) {
			sender.sendMessage(main.color("&c&l(!) &r" + args[1] + " is not online."));
			return true;
		}
		BattlePassProgress progress = manager.getProgress(target);
		if (progress == null && !sub.equals("reset")) {
			sender.sendMessage(main.color("&c&l(!) &r" + target.getName() + "'s Battle Pass hasn't loaded yet."));
			return true;
		}

		switch (sub) {
			case "givexp": {
				Integer amount = parseInt(sender, args, 2);
				if (amount == null) return true;
				manager.addXp(target, amount);
				sender.sendMessage(main.color("&aGave &e" + amount + " XP &ato " + target.getName()
						+ "&a. Now Tier &e" + progress.getTier() + " &a(" + progress.xp + " XP)."));
				if (!BattlePassSeason.isActive())
					sender.sendMessage(main.color("&cNote: the season isn't active, so no XP was added."));
				return true;
			}
			case "setxp": {
				Integer amount = parseInt(sender, args, 2);
				if (amount == null) return true;
				manager.setXp(target, amount);
				sender.sendMessage(main.color("&aSet " + target.getName() + "'s XP to &e" + progress.xp
						+ " &a(Tier &e" + progress.getTier() + "&a)."));
				return true;
			}
			case "completequest": {
				if (args.length < 3) {
					sendAdminHelp(sender, label);
					return true;
				}
				BattlePassQuestManager quests = manager.getQuests();
				if (quests == null) {
					sender.sendMessage(main.color("&c&l(!) &rQuests are disabled."));
					return true;
				}

				// A number is the quest's place in the menu (1 is the first), otherwise it's the quest id
				BattlePassQuest quest = null;
				BattlePassQuest[] all = BattlePassQuest.values();
				try {
					int number = Integer.parseInt(args[2]);
					if (number >= 1 && number <= all.length)
						quest = all[number - 1];
				} catch (NumberFormatException e) {
					quest = BattlePassQuest.byId(args[2].toLowerCase());
				}
				if (quest == null) {
					sender.sendMessage(main.color("&c&l(!) &rNo such quest. Use a number from 1 to " + all.length + " or a quest id."));
					return true;
				}

				if (!quests.forceComplete(target, quest))
					sender.sendMessage(main.color("&c&l(!) &r" + target.getName() + "'s quests haven't loaded yet."));
				else
					sender.sendMessage(main.color("&aCompleted &e" + quest.getName() + " &afor " + target.getName() + "&a."));
				return true;
			}
			case "reset":
				manager.reset(target);
				sender.sendMessage(main.color("&aReset " + target.getName() + "'s Battle Pass progress and quests for this season."));
				return true;
			default:
				sendAdminHelp(sender, label);
				return true;
		}
	}

	/** /battlepass premium <player> <true|false>: what the store runs when someone buys the Premium Pass. */
	private void setPremium(final CommandSender sender, String[] args, String label, BattlePassManager manager) {
		if (args.length < 3) {
			sendAdminHelp(sender, label);
			return;
		}
		final String name = args[1];
		if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
			sender.sendMessage(main.color("&c&l(!) &rUse true or false, not " + args[2] + "."));
			return;
		}
		final boolean premium = Boolean.parseBoolean(args[2]);

		manager.setPremiumByName(name, premium, new Consumer<Boolean>() {
			@Override
			public void accept(Boolean saved) {
				if (saved) {
					sender.sendMessage(main.color("&aSet " + name + "'s Premium Pass to &e" + premium + "&a."));
					if (BattlePassSeason.PREMIUM_FREE_FOR_TESTING)
						sender.sendMessage(main.color("&7(Premium is currently free for everyone, so this has no visible effect yet.)"));
				} else {
					sender.sendMessage(main.color("&c&l(!) &rCouldn't set " + name + "'s Premium Pass: they have never joined, or the database failed."));
					// A store command can't be retried by the store, so the console keeps a line to run by hand
					main.getLogger().severe("[BattlePass] Failed, run by hand: /battlepass premium " + name + " " + premium);
				}
			}
		});
	}

	private Integer parseInt(CommandSender sender, String[] args, int index) {
		if (args.length <= index) {
			sender.sendMessage(main.color("&c&l(!) &rMissing amount."));
			return null;
		}
		try {
			return Integer.parseInt(args[index]);
		} catch (NumberFormatException e) {
			sender.sendMessage(main.color("&c&l(!) &r" + args[index] + " is not a number."));
			return null;
		}
	}

	private void sendAdminHelp(CommandSender sender, String label) {
		sender.sendMessage(main.color("&e/" + label + " &7- open the Battle Pass"));
		sender.sendMessage(main.color("&e/" + label + " givexp <player> <amount>"));
		sender.sendMessage(main.color("&e/" + label + " setxp <player> <amount>"));
		sender.sendMessage(main.color("&e/" + label + " premium <player> <true|false>"));
		sender.sendMessage(main.color("&e/" + label + " completequest <player> <1-" + BattlePassQuest.values().length + ">"));
		sender.sendMessage(main.color("&e/" + label + " reset <player>"));
	}
}
