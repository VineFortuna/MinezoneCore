package anthony.SuperCraftBrawl;

import java.util.HashMap;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
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
		Rank rank = main.getRankManager().getRank(player);
        String rankName = "";

        if (rank != null && rank.getTag() != null)
		    rankName = rank.getTag();

		if (rank == Rank.DEFAULT)
			rankName = main.color("&7Default");

		// EXP settings (tweak if you have a dynamic requirement)
		// EXP settings (tweak if you have a dynamic requirement)
		final int expRequired = 2500;

		// Hypixel-style micro bar: 10 tiny squares, with [ ] and % (no space before %)
		final String expBar = (data == null) ? ChatColor.WHITE + "[]0%" // placeholder when null (won't show long)
				: Bars.dotsBar(data.exp, expRequired, 10, ChatColor.AQUA, // filled color
						ChatColor.GRAY, // empty color
						'■', // filled glyph (try '•' or '▪' if you prefer)
						'■', // empty glyph
						true, // showBrackets -> [........]
						true // showPercent -> ]79%
				);

		if (main.getCommands() == null) {
			board.updateTitle("" + ChatColor.AQUA + ChatColor.BOLD + "MINEZONE");
			if (data != null) {
				board.updateLines("" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.RESET + ChatColor.BOLD + "Server: " + ChatColor.GRAY + "Lobby-1", "",
						"" + ChatColor.RESET + ChatColor.BOLD + "Gems: " + ChatColor.GRAY + "0", "",
						"" + ChatColor.RESET + ChatColor.BOLD + "Rank: " + rankName, "",
						"" + ChatColor.RESET + ChatColor.BOLD + "Level: " + ChatColor.WHITE + data.level, expBar, // ★
																													// micro
																													// EXP
																													// dots
																													// line
						"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.AQUA + "minezone.club");
			}
			return;
		}

		if (!main.tournament) {
			String gameServer = "MINEZONE";
			board.updateTitle(main.color("&e&l" + gameServer));
			if (data != null) {
				board.updateLines("",
						main.color("&fTokens: &a" + data.tokens), "", main.color("&fRank: &r" + rankName), "",
						// shows ✧ plus the level like your existing line
						main.color("&fLevel: &f" + data.checkPlayerLevel(player, data) + "✧" + data.level), expBar,

						"",
						main.color("&eminezone.club"));
			}
		} else {
			board.updateTitle("" + ChatColor.AQUA + ChatColor.BOLD + "MINEZONE");
			if (data != null) {
				board.updateLines("",
						"" + ChatColor.WHITE + ChatColor.BOLD + "Tokens: " + ChatColor.GRAY + data.tokens, "",
						"" + ChatColor.WHITE + ChatColor.BOLD + "Rank: " + rankName, "",
						"" + ChatColor.WHITE + ChatColor.BOLD + "Points: " + ChatColor.GRAY + data.points,
						"",
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
			board.updateTitle("" + ChatColor.AQUA + ChatColor.BOLD + game.getMap());
			board.updateLines("", "" + ChatColor.BOLD + "Game Mode:", " " + ChatColor.GRAY + game.gameType.getName(),
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Class:", main.color(" &cR&6a&en&ad&bo&3m"), "",
					"" + ChatColor.RESET + ChatColor.BOLD + "Players:",
					" " + ChatColor.GRAY
							+ (game.getMap().GetInstance().gameType == GameType.FRENZY
									? "" + ChatColor.GRAY + game.players.size() + "/" + game.gameType.getMaxPlayers()
									: "")
							+ (game.getMap().GetInstance().gameType == GameType.CLASSIC
									? "" + ChatColor.GRAY + game.players.size() + "/" + game.gameType.getMaxPlayers()
									: "")
							+ (game.getMap().GetInstance().gameType == GameType.DUEL
									? "" + ChatColor.GRAY + game.players.size() + "/" + game.gameType.getMaxPlayers()
									: ""),
					"", "" + ChatColor.RESET + ChatColor.BOLD + "Status:",
					"" + ChatColor.RESET + ChatColor.GRAY + ChatColor.ITALIC + " Waiting...");

			game.boards.get(player).updateTitle("" + ChatColor.AQUA + ChatColor.BOLD + game.getMap().toString());
		} else {
			board.updateTitle("" + ChatColor.YELLOW + ChatColor.BOLD + game.duosMap.toString());
			board.updateLines("", "" + ChatColor.RESET + ChatColor.BOLD + "Class:", " " + ChatColor.GOLD + "Random", "",
					"" + ChatColor.RESET + ChatColor.BOLD + "Players:",
					" " + ChatColor.RESET + game.players.size() + "/6", "",
					"" + ChatColor.RESET + ChatColor.BOLD + "Status:",
					"" + ChatColor.RESET + ChatColor.ITALIC + " Waiting...");
		}
	}

	public void updatePlayerCountBoard(Player player, GameInstance game) {
		if (game != null) {
			GameType gameType = game.getMap().GetInstance().gameType;
			int playerSize = game.players.size();
			int maxSize = game.gameType.getMaxPlayers();

			game.boards.get(player).updateLine(8,
					" " + ChatColor.GRAY
							+ (gameType == GameType.FRENZY ? "" + ChatColor.GRAY + playerSize + "/" + maxSize : "")
							+ (gameType == GameType.CLASSIC ? "" + ChatColor.GRAY + playerSize + "/" + maxSize : "")
							+ (gameType == GameType.DUEL ? "" + ChatColor.GRAY + playerSize + "/" + maxSize : ""));
		}
	}
}
