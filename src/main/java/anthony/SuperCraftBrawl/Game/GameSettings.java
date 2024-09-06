package anthony.SuperCraftBrawl.Game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.playerdata.PlayerData;
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

	/**
	 * This function sets the time of day in game
	 */
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
			if (this.totalGameTypeVotes == game.players.size()) {
				if (game.gameType == GameType.CLASSIC)
					game.gameType = GameType.FRENZY;
				else
					game.gameType = GameType.CLASSIC;

				game.TellAll(color("&2&l(!) &rThe game mode has been set to &e&l" + game.gameType.toString()));
			}
		}
	}

	public void handleVoteGameStart(Player player, GameInstance game) {
		GameSettings gs = null;

		if (game != null && game.getGameSettings() != null) {
			gs = game.getGameSettings();
			if (!(gs.startVotes.contains(player))) {
				gs.totalStartVotes++;
				gs.startVotes.add(player);
				updateGameStartVoteStatus(player, game, ChatColor.GREEN);
			} else {
				gs.totalStartVotes--;
				gs.startVotes.remove(player);
				updateGameStartVoteStatus(player, game, ChatColor.RED);
			}
		}
	}

	public void handleVoteGameType(Player player, GameInstance game) {
		GameSettings gs = null;

		if (game != null && game.getGameSettings() != null) {
			gs = game.getGameSettings();
			if (!(gs.gameTypeVotes.contains(player))) {
				gs.totalGameTypeVotes++;
				gs.gameTypeVotes.add(player);
			} else {
				gs.totalGameTypeVotes--;
				gs.gameTypeVotes.remove(player);
			}
		}
	}

	public void updateGameStartVoteStatus(Player player, GameInstance game, ChatColor color) {
		String status = color + (color == ChatColor.GREEN ? " is Ready " : " is no longer Ready ");
		String message = ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "(!) " + ChatColor.RESET + ChatColor.YELLOW
				+ player.getName() + ChatColor.BOLD + status + ChatColor.RED + "(" + ChatColor.GREEN
				+ game.getGameSettings().totalStartVotes + "/" + game.players.size() + ChatColor.RED + ")";
		game.TellAll(message);

		if (game.getGameSettings().totalStartVotes == game.players.size())
			game.getGameSettings().forceStartGame();
	}

	private String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
