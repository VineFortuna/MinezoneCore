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
            	Location loc = new Location(main.getLobbyWorld(), 189.500, 106, 703.513);

        		ArmorStand settingsStand = (ArmorStand) main.getLobbyWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        		settingsStand.setCustomName("Leaderboard Settings");
        		settingsStand.setCustomNameVisible(true);
        		settingsStand.setGravity(false);
        		settingsStand.setVisible(false);
        		settingsStand.setBasePlate(false);

        		// Create the "Click to change settings" line slightly below
        		Location secondLineLocation = loc.clone().subtract(0, 0.5, 0); // Adjust the y-axis for line spacing
        		ArmorStand clickToChangeStand = (ArmorStand) secondLineLocation.getWorld().spawnEntity(secondLineLocation,
        				EntityType.ARMOR_STAND);
        		clickToChangeStand.setCustomName("Click to change settings");
        		clickToChangeStand.setCustomNameVisible(true);
        		clickToChangeStand.setGravity(false);
        		clickToChangeStand.setVisible(false);
        		clickToChangeStand.setBasePlate(false);
            }
        }, 100L);
	}

}
