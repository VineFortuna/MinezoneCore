package anthony.flagwars;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.resources.FWResourceGenerator;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Per-match scoreboards for Flag Wars, built on the same FastBoard
 * library SCB's own ScoreboardManager wraps. One of these lives per
 * FWGameInstance and is torn down with it.
 */
public class FWScoreboardManager {

	private static final String FOOTER = "&ewww.minezone.club";

	private final FWGameInstance instance;
	private final Map<UUID, FastBoard> boards = new HashMap<>();

	public FWScoreboardManager(FWGameInstance instance) {
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
		board.updateTitle(main().color("&6&lFLAG WARS"));

		int playerCount = instance.countPlayers();
		int maxPlayers = instance.getMode().getMaxPlayers();

		board.updateLines(
				"",
				main().color("&rMap: &a" + FWGameConstants.MAP_WORLD_NAME),
				"",
				main().color("&rMode: &a" + instance.getMode().getLabel()),
				"",
				main().color("&rPlayers: &a" + playerCount + "/" + maxPlayers),
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

	private static final String[] GENERATOR_TIER_ROMAN = {"I", "II", "III"};

	/** Shows/refreshes the in-progress board for one player, listing every team's capture progress. */
	public void showGame(Player player) {
		FastBoard board = boardFor(player);
		board.updateTitle(main().color("&6&lFLAG WARS"));

		FWTeam own = instance.findTeamOf(player);

		List<String> lines = new ArrayList<>();
		lines.add("");
		lines.add(main().color(diamondTierLine()));
		lines.add("");
		if (own != null) {
			lines.add(main().color("&rKills: &a" + instance.getKills(player)));
			lines.add(main().color("&rFlags: &a" + instance.getFlagCaptures(player)));
		} else {
			lines.add(main().color("&7Spectating"));
		}

		if (own != null && instance.getFlagManager().isCarrying(player)) {
			lines.add(main().color("&e&lCarrying a flag!"));
		}

		lines.add("");

		for (FWTeam team : instance.getTeams().values()) {
			if (team.isEmpty()) continue;
			lines.add(main().color(teamStatusLine(own, team)));
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

	/**
	 * Same idea Hypixel's own sidebar uses: counts down to the *next* tier, not the current one -
	 * e.g. while sitting at Tier I it reads "Diamond Tier II: 4m 59s", ticking down every second
	 * (driven by FWResourceGenerator#tick calling refreshScoreboards each second) until that tier
	 * kicks in, then it starts counting down to Tier III. No countdown once already at Tier III,
	 * since there's nothing left to upgrade to. No "0m" once under a minute - just "45s" - both to
	 * match what was asked and because FastBoard hard-caps every line at 30 chars on 1.8 ("Diamond
	 * Tier III in: 12m 0s" was 31 and crashed the board on every refresh past the 6-minute mark).
	 */
	private String diamondTierLine() {
		FWResourceGenerator generator = instance.getResourceGenerator();
		int secondsUntilNext = generator.getSecondsUntilNextTier();

		if (secondsUntilNext < 0) {
			return "&bDiamond Tier " + GENERATOR_TIER_ROMAN[generator.getCurrentTier() - 1] + " &7(MAX)";
		}

		int nextTier = generator.getCurrentTier() + 1;
		int minutes = secondsUntilNext / 60;
		int seconds = secondsUntilNext % 60;
		String time = minutes > 0 ? (minutes + "m " + seconds + "s") : (seconds + "s");
		return "&bDiamond Tier " + GENERATOR_TIER_ROMAN[nextTier - 1] + ": &a" + time;
	}

	private String teamLabel(FWTeam team) {
		return FWGameConstants.chatColorCodeFor(team.getColor()) + team.getName();
	}

	/** No checkmark by default - one only shows up next to a team once the viewer's own team has captured that team's flag. */
	private String teamStatusLine(FWTeam viewerTeam, FWTeam team) {
		String icon = viewerTeam != null && viewerTeam.hasCapturedFrom(team.getSiteId()) ? "&a✔ " : "";
		return icon + teamLabel(team) + " &7- &e" + team.getCapturedFlags()
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

	private static final String NAME_TAG_TEAM_PREFIX = "fw_";

	/** Lazily created - a Team's prefix is only broadcast to players who are viewing that same Scoreboard object, so the whole match shares one. */
	private Scoreboard nameTagScoreboard;

	private Scoreboard nameTagScoreboard() {
		if (nameTagScoreboard == null) {
			nameTagScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		}
		return nameTagScoreboard;
	}

	/**
	 * Colors a player's nametag above their head (and tab list entry) as "<color>[Team] <color>Name",
	 * replacing whatever rank tag they'd normally show - same setPrefix-bleeds-into-the-name trick
	 * SCB's own GameInstance#sendScoreboardUpdate uses for class tags.
	 */
	public void applyNameTag(Player player, FWTeam team) {
		player.setScoreboard(nameTagScoreboard());

		String teamKey = NAME_TAG_TEAM_PREFIX + team.getSiteId();
		Team sbTeam = nameTagScoreboard().getTeam(teamKey);
		if (sbTeam == null) sbTeam = nameTagScoreboard().registerNewTeam(teamKey);

		sbTeam.setPrefix(nameTagPrefix(team));
		if (!sbTeam.hasEntry(player.getName())) sbTeam.addEntry(player.getName());
	}

	/** Spectators don't get a team prefix of their own, but still need the shared scoreboard to see everyone else's. */
	public void joinNameTagScoreboard(Player player) {
		player.setScoreboard(nameTagScoreboard());
	}

	/**
	 * Compact "[Y]"/"[R]"-style tag (see FWGameConstants#abbreviateTeamName) rather than the full
	 * team name, both because there's barely any room above a player's head and because vanilla
	 * 1.8 caps team prefixes at 16 characters in the first place. Still falls back to dropping the
	 * trailing color (so the label shows without coloring the name) if that's ever not enough.
	 */
	private String nameTagPrefix(FWTeam team) {
		String color = FWGameConstants.chatColorCodeFor(team.getColor());
		String abbreviation = FWGameConstants.abbreviateTeamName(team.getName());

		String withNameColor = color + "[" + abbreviation + "] " + color;
		if (withNameColor.length() <= 16) return main().color(withNameColor);

		String withoutNameColor = color + "[" + abbreviation + "] ";
		if (withoutNameColor.length() <= 16) return main().color(withoutNameColor);

		return main().color(withoutNameColor).substring(0, 16);
	}

	/** Reverts a leaving participant to the default scoreboard so their nametag/tab list goes back to normal once they're out of Flag Wars. */
	public void leaveNameTagScoreboard(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}
}
