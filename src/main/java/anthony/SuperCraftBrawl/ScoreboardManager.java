package anthony.SuperCraftBrawl;

import java.util.HashMap;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatColor;

public class ScoreboardManager {

	private Core main;
	public HashMap<Player, FastBoard> playersLobbyBoard = new HashMap<>();

	public ScoreboardManager(Core main) {
		this.main = main;
	}

	public void lobbyBoard(Player player) {
		FastBoard board = new FastBoard(player);
		PlayerData data = main.getDataManager().getPlayerData(player);
		this.playersLobbyBoard.put(player, board);

		if (main.getCommands() == null) {
			board.updateTitle("" + ChatColor.AQUA + ChatColor.BOLD + "MINEZONE");
			if (data != null) {
				board.updateLines("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.RESET + ChatColor.BOLD + "Server: " + ChatColor.GRAY + "Lobby-1", "",
						"" + ChatColor.RESET + ChatColor.BOLD + "Gems: " + ChatColor.GRAY + "0", "",
						"" + ChatColor.RESET + ChatColor.BOLD + "Rank: "
								+ main.getRankManager().getRank(player).getTag(),
						"", "" + ChatColor.RESET + ChatColor.BOLD + "Level: " + ChatColor.GRAY + data.level,
						"" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + data.exp + "/2500 EXP" + ChatColor.DARK_GRAY
								+ "]",
						"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.AQUA + "minezone.club");
			}
			return;
		}

		if (main.tournament == false) {
			String gameServer = "SUPER CRAFT BLOCKS";
			board.updateTitle("" + ChatColor.AQUA + ChatColor.BOLD + gameServer);
			if (data != null) {
				board.updateLines("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.RESET + ChatColor.BOLD + "Tokens: " + ChatColor.GRAY + data.tokens, "",
						"" + ChatColor.RESET + ChatColor.BOLD + "Rank: "
								+ main.getRankManager().getRank(player).getTag(),
						"", "" + ChatColor.RESET + ChatColor.BOLD + "Level: " + ChatColor.GRAY + data.level,
						"" + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + data.exp + "/2500 EXP" + ChatColor.DARK_GRAY
								+ "]",
						"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.AQUA + "minezone.club");
			}
		} else {
			board.updateTitle("" + ChatColor.AQUA + ChatColor.BOLD + "MINEZONE");
			if (data != null) {
				board.updateLines("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.WHITE + ChatColor.BOLD + "Tokens: " + ChatColor.GRAY + data.tokens, "",
						"" + ChatColor.WHITE + ChatColor.BOLD + "Rank: "
								+ main.getRankManager().getRank(player).getTag(),
						"", "" + ChatColor.WHITE + ChatColor.BOLD + "Points: " + ChatColor.GRAY + data.points,
						"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.AQUA + "minezone.club");
			}
		}
	}

	/**
	 * This function sets the waiting lobby scoreboard for when a player joins the
	 * game
	 * 
	 * @param player to give the scoreboard to
	 * @param game   which is the instance of the game player is in
	 */
	public void waitingLobbyBoard(Player player, GameInstance game) {
		FastBoard board = new FastBoard(player);
		game.boards.put(player, board);

		if (game.getMap() != null) {
			board.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + game.getMap()
					+ (game.getMap().GetInstance().gameType == GameType.FRENZY
							? "" + ChatColor.GRAY + ChatColor.ITALIC + " (frenzy)"
							: ""));
			board.updateLines("", "" + ChatColor.RESET + ChatColor.BOLD + "Class:", " " + ChatColor.RESET + "Random",
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Players:",
					" " + ChatColor.RESET
							+ (game.getMap().GetInstance().gameType == GameType.FRENZY
									? "" + ChatColor.RESET + game.players.size() + "/" + game.gameType.getMaxPlayers()
									: "")
							+ (game.getMap().GetInstance().gameType == GameType.CLASSIC
									? "" + ChatColor.RESET + game.players.size() + "/" + game.gameType.getMaxPlayers()
									: "")
							+ (game.getMap().GetInstance().gameType == GameType.DUEL
									? "" + ChatColor.RESET + game.players.size() + "/" + game.gameType.getMaxPlayers()
									: ""),
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Status:",
					"" + ChatColor.RESET + ChatColor.ITALIC + " Waiting..");

			game.boards.get(player)
					.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + game.getMap().toString()
							+ (game.getMap().GetInstance().gameType == GameType.FRENZY
									? "" + ChatColor.GRAY + ChatColor.ITALIC + " (frenzy)"
									: ""));
		} else {
			board.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + game.duosMap.toString());
			board.updateLines("", "" + ChatColor.RESET + ChatColor.BOLD + "Class:", " " + ChatColor.RESET + "Random",
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Players:",
					" " + ChatColor.RESET + game.players.size() + "/6", "",
					"" + ChatColor.RESET + ChatColor.BOLD + "Status:",
					"" + ChatColor.RESET + ChatColor.ITALIC + " Waiting..");
		}
	}

	public void updatePlayerCountBoard(Player player, GameInstance game) {
		if (game != null) {
			GameType gameType = game.getMap().GetInstance().gameType;
			int playerSize = game.players.size();
			int maxSize = game.gameType.getMaxPlayers();

			game.boards.get(player).updateLine(5,
					" " + (gameType == GameType.FRENZY ? "" + ChatColor.RESET + playerSize + "/" + maxSize : "")
							+ (gameType == GameType.CLASSIC ? "" + ChatColor.RESET + playerSize + "/" + maxSize : "")
							+ (gameType == GameType.DUEL ? "" + ChatColor.RESET + playerSize + "/" + maxSize : ""));
		}
	}
}
