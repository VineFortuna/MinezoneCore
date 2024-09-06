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
	public int lightningDropSec;
	public int dropTimer;
	public List<Player> startVotes;
	public List<Player> timeVotes;
	public List<Player> gameTypeVotes;
	public List<Player> lightningVotes;

	public GameSettings(GameInstance game) {
		this.game = game;
		this.totalStartVotes = 0;
		this.totalTimeVotes = 0;
		this.totalGameTypeVotes = 0;
		this.lightningDropSec = 0;
		this.dropTimer = 30; //Default lightning drop time
		this.startVotes = new ArrayList<Player>();
		this.timeVotes = new ArrayList<Player>();
		this.gameTypeVotes = new ArrayList<Player>();
		this.lightningVotes = new ArrayList<Player>();
	}

	// GETTER METHODS:

	public int getTotalStartVotes() {
		return this.totalStartVotes;
	}

	public int getTotalTimeVotes() {
		return this.totalTimeVotes;
	}

	public int getTotalGameTypeVotes() {
		return this.totalGameTypeVotes;
	}

	public int getLightningVotes() {
		return this.lightningDropSec;
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
				checkOtherSettings(); //Set other settings too if enough votes
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

	/**
	 * This function increases the lightning drop spawn rate on the map to 2x
	 * the speed if the votes are equal to the player size
	 */
	public void increaseLightningRate() {
		if (game != null) {
			if (getLightningVotes() == game.players.size()) {
				this.dropTimer /= 2;
				game.TellAll(color("&2&l(!) &rLoot drops spawn rate is now &e&l2x"));
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
	
	public void handleLightningRate(Player player, GameInstance game) {
		GameSettings gs = null;

		if (game != null && game.getGameSettings() != null) {
			gs = game.getGameSettings();
			if (!(gs.lightningVotes.contains(player))) {
				gs.lightningDropSec++;
				gs.lightningVotes.add(player);
			} else {
				gs.lightningDropSec--;
				gs.lightningVotes.remove(player);
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
	
	/**
	 * This function makes sure if the game is force started to check for votes
	 * on the other game settings
	 */
	private void checkOtherSettings() {
		changeGameType();
		increaseLightningRate();
	}
	
	/*
	 * This function handles removing players from start votes if they
	 * leave the server or the game lobby
	 */
	public void removeFromStartVotes(Player player) {
		if (game != null && game.getGameSettings() != null) {
			GameSettings gs = game.getGameSettings();
			
			if (gs.startVotes.contains(player)) {
				gs.totalStartVotes--;
				gs.startVotes.remove(player);
			}
		}
	}
	
	/*
	 * This function handles removing players from start votes if they
	 * leave the server or the game lobby
	 */
	public void removeFromGameTypeVotes(Player player) {
		if (game != null && game.getGameSettings() != null) {
			GameSettings gs = game.getGameSettings();
			
			if (gs.gameTypeVotes.contains(player)) {
				gs.totalGameTypeVotes--;
				gs.gameTypeVotes.remove(player);
			}
		}
	}
	
	/*
	 * This function handles removing players from start votes if they
	 * leave the server or the game lobby
	 */
	public void removeFromLightningVotes(Player player) {
		if (game != null && game.getGameSettings() != null) {
			GameSettings gs = game.getGameSettings();
			
			if (gs.lightningVotes.contains(player)) {
				gs.lightningDropSec--;
				gs.lightningVotes.remove(player);
			}
		}
	}

	private String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
