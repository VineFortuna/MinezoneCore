package anthony.SuperCraftBrawl.Game;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class GameSettings {

	private GameInstance game;

	public GameSettings(GameInstance game) {
		this.game = game;
	}

	public void setTimeOfDay() {
		game.getMapWorld().setTime(13000);
	}

	/*
	 * This function starts the game if the command /startgame is used or if
	 * everyone in the waiting lobby votes to start
	 */
	public void forceStartGame() {
		if (game.gameStartTime != null) {
			if (game.ticksTilStart <= 60) {
				game.TellAll(color("&2&l(!) &rGame is now starting"));
				game.gameStartTime.cancel();
				game.StartGame();
			}
		}
	}

	private String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
