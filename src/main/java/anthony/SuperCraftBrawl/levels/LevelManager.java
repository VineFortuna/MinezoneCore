package anthony.SuperCraftBrawl.levels;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class LevelManager {

    private Core core;

    public LevelManager(Core core) {
        this.core = core;
    }

    public void checkLevelUp(Player player) {
        PlayerData data = this.core.getDataManager().getPlayerData(player);

        if (data == null) return;

        if (data.exp >= 2500) {
            data.level++;
            data.exp -= 2500;
            player.sendMessage(this.core.color("&8&m---------------------------"));
            player.sendMessage(this.core.color("&6&l✦✦ &e&lLEVEL UP! &6&l✦✦"));
            player.sendMessage(this.core.color("&rYou are now Level: &a" +
                    data.checkPlayerLevel(player, data) + "✧" + data.level));
            player.sendMessage(this.core.color("&8&m---------------------------"));
            player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.15f);
        }
    }

    public void setLevel(Player player, int num) {
        PlayerData data = this.core.getDataManager().getPlayerData(player);

        if (data == null) return;

        data.level = num;
        player.sendMessage(this.core.color("&8&m----------------------------------------"));
        player.sendMessage(this.core.color("&6&l✦✦ &e&lLEVEL UP! &6&l✦✦"));
        player.sendMessage(this.core.color("&rYou are now Level: &a" +
                data.checkPlayerLevel(player, data) + "✧" + data.level));
        player.sendMessage(this.core.color("&8&m----------------------------------------"));
        player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.15f);
    }

}
