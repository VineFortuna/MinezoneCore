package anthony.villagerdefense;

import anthony.SuperCraftBrawl.Core;
import anthony.villagerdefense.map.VDMapConfig;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Per-match scoreboards for VillagerDefense, built on the same FastBoard
 * library SCB's own ScoreboardManager wraps. One of these lives per
 * VDGameInstance and is torn down with it.
 */
public class VDScoreboardManager {

	private static final String FOOTER = "&ewww.minezone.club";

	private final VDGameInstance instance;
	private final Map<UUID, FastBoard> boards = new HashMap<>();

	public VDScoreboardManager(VDGameInstance instance) {
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
		board.updateTitle(main().color("&e&lVILLAGERDEFENSE"));

		int playerCount = instance.countPlayers();
		int maxPlayers = VDMapConfig.TEAM_COUNT * VDMapConfig.TEAM_CAPACITY;

		board.updateLines(
				"",
				main().color("&fMap: &a" + VDGameConstants.MAP_WORLD_NAME),
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

	/** Shows/refreshes the in-progress board for one player, listing every team's status. */
	public void showGame(Player player) {
		FastBoard board = boardFor(player);
		board.updateTitle(main().color("&e&lVILLAGERDEFENSE"));

		VDTeam own = instance.findTeamOf(player);

		List<String> lines = new ArrayList<>();
		lines.add("");
		lines.add(main().color(own != null
				? "&fYour Team: " + teamLabel(own)
				: "&7Spectating"));
		lines.add("");

		for (VDTeam team : instance.getTeams().values()) {
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

	private String teamLabel(VDTeam team) {
		return VDGameConstants.chatColorCodeFor(team.getColor()) + team.getName();
	}

	private String teamStatusLine(VDTeam team) {
		boolean alive = team.isVillagerAlive() && !team.isEliminated();
		String icon = alive ? "&a✔" : "&c✘";
		return icon + " " + teamLabel(team);
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
