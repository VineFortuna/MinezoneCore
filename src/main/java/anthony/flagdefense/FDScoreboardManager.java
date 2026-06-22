package anthony.flagdefense;

import anthony.SuperCraftBrawl.Core;
import anthony.flagdefense.map.FDMapConfig;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Per-match scoreboards for FlagDefense, built on the same FastBoard
 * library SCB's own ScoreboardManager wraps. One of these lives per
 * FDGameInstance and is torn down with it.
 */
public class FDScoreboardManager {

	private static final String FOOTER = "&ewww.minezone.club";

	private final FDGameInstance instance;
	private final Map<UUID, FastBoard> boards = new HashMap<>();

	public FDScoreboardManager(FDGameInstance instance) {
		this.instance = instance;
	}

	private Core main() {
		return instance.getManager().getMain();
	}

	private FastBoard boardFor(Player player) {
		return boards.computeIfAbsent(player.getUniqueId(), id -> new FastBoard(player));
	}

	/** Shows/refreshes the waiting-room board for one player. */
	public void showWaiting(Player player, String statusLine) {
		FastBoard board = boardFor(player);
		board.updateTitle(main().color("&e&lFLAGDEFENSE"));

		int playerCount = instance.countPlayers();
		int maxPlayers = FDMapConfig.TEAM_COUNT * FDMapConfig.TEAM_CAPACITY;

		board.updateLines(
				"",
				main().color("&fMap: &a" + FDGameConstants.MAP_WORLD_NAME),
				"",
				main().color("&fPlayers: &a" + playerCount + "&7/&a" + maxPlayers),
				"",
				main().color(statusLine),
				"",
				main().color(FOOTER)
		);
	}

	public void refreshAllWaiting(String statusLine) {
		for (Player player : instance.onlineParticipants()) {
			showWaiting(player, statusLine);
		}
	}

	/** Shows/refreshes the in-progress board for one player, listing every team's capture progress. */
	public void showGame(Player player) {
		FastBoard board = boardFor(player);
		board.updateTitle(main().color("&e&lFLAGDEFENSE"));

		FDTeam own = instance.findTeamOf(player);

		List<String> lines = new ArrayList<>();
		lines.add("");
		lines.add(main().color(own != null
				? "&fYour Team: " + teamLabel(own)
				: "&7Spectating"));

		if (own != null && instance.getFlagManager().isCarrying(player)) {
			lines.add(main().color("&e&lCarrying a flag!"));
		}

		lines.add("");

		for (FDTeam team : instance.getTeams().values()) {
			if (team.isEmpty()) continue;
			lines.add(main().color(teamStatusLine(team)));
		}

		lines.add("");
		lines.add(main().color(FOOTER));

		board.updateLines(lines.toArray(new String[0]));
	}

	public void refreshAllGame() {
		for (Player player : instance.onlineParticipants()) {
			showGame(player);
		}
	}

	private String teamLabel(FDTeam team) {
		return FDGameConstants.chatColorCodeFor(team.getColor()) + team.getName();
	}

	private String teamStatusLine(FDTeam team) {
		String icon = team.isInactive() ? "&7✘" : "&a✔";
		return icon + " " + teamLabel(team) + " &7- &e" + team.getCapturedFlags()
				+ "&7/&e" + instance.getRequiredCaptures();
	}

	public void remove(Player player) {
		FastBoard board = boards.remove(player.getUniqueId());
		if (board != null) board.delete();
	}

	public void removeAll() {
		for (FastBoard board : boards.values()) {
			board.delete();
		}
		boards.clear();
	}
}
