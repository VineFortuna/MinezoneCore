package anthony.Managers;

import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;

import static anthony.util.ChatColorHelper.color;

public class ManagerServerMessage implements Manager {
    private static final String[] messages = { "&lReminder to thank the staff", "&lThank you for playing, you're awesome", "&lShare Minezone with your friends" };


    @Override
    public void onEnable() {
        /* ... */
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String message;

            /* // TODO: ManagerPlayerData
            PlayerData playerData = this.getDataManager().getPlayerData(player);
            if (playerData.getRank() == Rank.DEFAULT) {
                message = "&lSupport us, buy a rank!";
            } else */ {
                message = messages[new Random().nextInt(messages.length)];
            }

            player.kickPlayer(color("&c&lSERVER IS RESTARTING\n &e\n" + message));
        }

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(color("&4&l(!) &eServer Restarting..."));
        Bukkit.broadcastMessage("");
    }
}
