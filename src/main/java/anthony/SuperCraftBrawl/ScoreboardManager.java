package anthony.SuperCraftBrawl;

import java.util.HashMap;

import org.bukkit.entity.Player;

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
						"" + ChatColor.RESET + ChatColor.BOLD + "Rank: " + main.getRankManager().getRank(player).getTag(),
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
						"" + ChatColor.RESET + ChatColor.BOLD + "Rank: " + main.getRankManager().getRank(player).getTag(),
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
						"" + ChatColor.WHITE + ChatColor.BOLD + "Rank: " + main.getRankManager().getRank(player).getTag(),
						"", "" + ChatColor.WHITE + ChatColor.BOLD + "Points: " + ChatColor.GRAY + data.points,
						"" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-----------------",
						"" + ChatColor.AQUA + "minezone.club");
			}
		}
	}

}
