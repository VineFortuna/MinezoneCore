package anthony.SuperCraftBrawl.leaderboards;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import anthony.SuperCraftBrawl.Core;
import net.md_5.bungee.api.ChatColor;

public class BoardSettings {

	private Core main;

	public BoardSettings(Core main) {
		this.main = main;
		changeSettings();
	}

	private void changeSettings() {
		Bukkit.getScheduler().runTaskLater(main, new Runnable() {
            @Override
            public void run() {
            	Location loc = new Location(main.getLobbyWorld(), 189.518, 105, 676.499);

        		ArmorStand settingsStand = (ArmorStand) main.getLobbyWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        		settingsStand.setCustomName(main.color("&c&lChristmas Rewards"));
        		settingsStand.setCustomNameVisible(true);
        		settingsStand.setGravity(false);
        		settingsStand.setVisible(false);
        		settingsStand.setBasePlate(false);
            }
        }, 100L);
	}

}
