package anthony.SuperCraftBrawl.signs;

import anthony.SuperCraftBrawl.Game.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.SuperCraftBrawl.Game.map.Maps;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignManager implements Listener {

	private Core core;

	public SignManager(Core core) {
		this.core = core;
        this.core.getServer().getPluginManager().registerEvents(this, core);
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

    //EVENT HANDLERS:

    /*
    * This event listener handles when players interact with a game
    * sign in the lobby. Player will either be able to join a game,
    * or spectate if the game is playing already
     */
    @EventHandler
    public void onGameSignInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getClickedBlock() == null)
            return;
        if (e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST
                || e.getClickedBlock().getType() == Material.SIGN) {
            Sign s = (Sign) e.getClickedBlock().getState();
            for (Maps map : Maps.values()) {
                if (s.getLine(1).equalsIgnoreCase(map.toString())) {
                    core.getGameManager().JoinMap(player, map);
                    GameInstance i = null;

                    if (core.getGameManager().gameMap.containsKey(map)) {
                        i = core.getGameManager().gameMap.get(map);

                        if (i != null) {
                            if (i.state == GameState.WAITING) {
                                s.setLine(2, core.color("&0Players: " + i.players.size() + "/"
                                        + i.getMap().GetInstance().gameType.getMaxPlayers()));
                                s.setLine(3, core.color("&0" + i.timeToStartSeconds + "s"));
                                s.update();
                            } else if (i.state == GameState.STARTED) {
                                player.sendMessage(core.color(
                                        "&2&l(!) &rSince the game you tried joining has started, you've joined as a Spectator"));
                                core.getGameManager().SpectatorJoinMap(player, map);
                            }
                        }
                        i.setSign(s);
                    }
                }
            }
        }
    }

}
