package anthony.SuperCraftBrawl.signs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import net.md_5.bungee.api.ChatColor;

public class SignManager {

	private Core core;

	public SignManager(Core core) {
		this.core = core;
	}

	/*
	 * This function updates the lobby sign when a player joins the game.
	 */
	public void updateSign(MapInstance mi, GameInstance instance) {
		Location loc = new Location(core.getLobbyWorld(), mi.signLoc.getX(), mi.signLoc.getY(), mi.signLoc.getZ());
		Block b = core.getLobbyWorld().getBlockAt(loc);

		if (b.getType() == Material.SIGN || b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
			if (instance != null) {
				Sign s = (Sign) b.getState();
				instance.setSign(s);
				s.setLine(2, core.color("&0Players: " + instance.players.size() + "/"
						+ instance.getMap().GetInstance().gameType.getMaxPlayers()));
				s.setLine(3, core.color("&0" + instance.timeToStartSeconds + "s"));
				s.update();
			}
		}
	}

	/*
	 * This updates the sign in the lobby to show map is in progress and to
	 * "Spectate" if clicked
	 */
	public void updateSignInProgress(Sign s) {
		if (s != null) {
			s.setLine(0, core.color("&2In Progress"));
			s.setLine(3, "" + ChatColor.BLACK + ChatColor.UNDERLINE + "Spectate");
			s.update();
		}
	}

	public void updateSignCountdown(Sign s, int timeToStartSeconds) {
		if (s != null) {
			s.setLine(3, core.color("&0" + timeToStartSeconds + "s"));
			s.update();
		}
	}

	public void resetSign(Sign s, Maps map) {
		if (s != null) {
			s.setLine(0, core.color("&2Lobby"));
			s.setLine(1, core.color("&0" + map.toString()));
			if (map != null)
				s.setLine(2, core.color("&0Players: 0/" + map.GetInstance().gameType.getMaxPlayers()));
			else
				s.setLine(2, core.color("&0Players: 0/6"));

			s.setLine(3, core.color("&030s"));
			s.update();
		}
	}

}
