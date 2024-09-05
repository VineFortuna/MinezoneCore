package anthony.SuperCraftBrawl.Game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class GameSettings {

	private GameInstance game;
	public int totalStartVotes;
	public int totalTimeVotes;
	public int totalGameTypeVotes;
	public List<Player> startVotes;
	public List<Player> timeVotes;
	public List<Player> gameTypeVotes;

	public GameSettings(GameInstance game) {
		this.game = game;
		this.totalStartVotes = 0;
		this.totalTimeVotes = 0;
		this.totalGameTypeVotes = 0;
		this.startVotes = new ArrayList<Player>();
		this.timeVotes = new ArrayList<Player>();
		this.gameTypeVotes = new ArrayList<Player>();
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
	
	public void changeGameType() {
		if (game != null) {
			if (game.gameType == GameType.CLASSIC)
				game.gameType = GameType.FRENZY;
			else
				game.gameType = GameType.CLASSIC;
			
			game.TellAll(color("&2&l(!) &rThe game mode has been set to &e&l" + game.gameType.toString()));
		}
	}

	private String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
