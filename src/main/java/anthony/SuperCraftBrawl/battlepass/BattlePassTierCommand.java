package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Staff testing commands:
 *   /bpupgrade [player]   - moves them up one Battle Pass tier (shows the normal tier up message)
 *   /bpdowngrade [player] - moves them down one tier
 * Defaults to yourself. Same permission as the other /battlepass admin commands.
 */
public class BattlePassTierCommand implements CommandExecutor {

	private static final String ADMIN_PERMISSION = "scb.battlepass.admin";

	private final Core main;

	public BattlePassTierCommand(Core main) {
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.isOp() && !sender.hasPermission(ADMIN_PERMISSION)) {
			sender.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return true;
		}

		BattlePassManager manager = main.getBattlePassManager();
		if (manager == null || !manager.isEnabled()) {
			sender.sendMessage(main.color("&c&l(!) &rThe Battle Pass is currently unavailable."));
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

		BattlePassProgress progress = manager.getProgress(target);
		if (progress == null) {
			sender.sendMessage(main.color("&c&l(!) &r" + target.getName() + "'s Battle Pass hasn't loaded yet."));
			return true;
		}

		boolean upgrade = cmd.getName().equalsIgnoreCase("bpupgrade");
		int oldTier = progress.getTier();
		boolean self = target.equals(sender);
		String who = self ? "You are" : target.getName() + " is";

		if (upgrade && oldTier >= BattlePassSeason.MAX_TIER) {
			sender.sendMessage(main.color("&c&l(!) &r" + who + " already on the final tier."));
			return true;
		}
		if (!upgrade && oldTier <= 0) {
			sender.sendMessage(main.color("&c&l(!) &r" + who + " already on Tier 0."));
			return true;
		}

		// Going up shows the normal tier up announcement to the target
		int newTier = manager.shiftTier(target, upgrade ? 1 : -1);

		if (!upgrade)
			target.sendMessage(main.color("&6&l(!) &rYour Battle Pass was lowered to &eTier " + newTier + "&r."));
		if (!self)
			sender.sendMessage(main.color("&6&l(!) &e" + target.getName() + " &ris now on &eTier " + newTier + "&r."));
		return true;
	}
}
