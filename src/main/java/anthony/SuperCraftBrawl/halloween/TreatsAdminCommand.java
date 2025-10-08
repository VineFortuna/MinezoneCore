package anthony.SuperCraftBrawl.halloween;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TreatsAdminCommand implements CommandExecutor {

    private final HalloweenHuntManager hunt;

    public TreatsAdminCommand(HalloweenHuntManager hunt) {
        this.hunt = hunt;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!sender.hasPermission("trick.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/treatsadmin resetme " + ChatColor.GRAY + "— reset your Halloween progress");
            return true;
        }

        if (args[0].equalsIgnoreCase("resetme")) {
            if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
            Player p = (Player) sender;
            hunt.getDao().reset(p.getUniqueId());
            sender.sendMessage(ChatColor.YELLOW + "Your Halloween progress has been reset.");
            hunt.refreshLobbyBoard(p);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        return true;
    }
}