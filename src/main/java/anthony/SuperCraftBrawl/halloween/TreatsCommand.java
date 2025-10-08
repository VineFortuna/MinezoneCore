package anthony.SuperCraftBrawl.halloween;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TreatsCommand implements CommandExecutor {
	private final HalloweenHuntManager hunt;

	public TreatsCommand(HalloweenHuntManager hunt) {
		this.hunt = hunt;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Players only.");
			return true;
		}
		Player p = (Player) sender;
		int found = hunt.getFoundCount(p.getUniqueId());
		p.sendMessage(ChatColor.GOLD + "You’ve found " + ChatColor.GREEN + found + ChatColor.GOLD + " of "
				+ HalloweenHuntManager.TOTAL + " baskets.");
		return true;
	}
}