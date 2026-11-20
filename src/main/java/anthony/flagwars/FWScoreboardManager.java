package anthony.flagwars;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.resources.FWResourceGenerator;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.NameTagVisibility;
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
		int maxPlayers = instance.getMaxPlayers();

		board.updateLines(fit(
				"",
				main().color("&rMap: &a" + instance.getMapName()),
				"",
				main().color("&rMode: &a" + instance.getMode().getLabel()),
				"",
				main().color("&rPlayers: &a" + playerCount + "/" + maxPlayers),
				"",
				main().color(statusLine),
				"",
				main().color(FOOTER)
		));
	}

	/**
	 * FastBoard on 1.8 throws on a line over MAX_LINE_LENGTH characters, and these boards are refreshed from inside
	 * joining and leaving a match, so one long line (a long map name, a long status) would break the join itself.
	 * Cuts such a line short instead, without leaving half of a color code at the end.
	 */
	private static String[] fit(String... lines) {
		String[] fitted = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.length() > MAX_LINE_LENGTH) {
				line = line.substring(0, MAX_LINE_LENGTH);
				if (line.charAt(line.length() - 1) == ChatColor.COLOR_CHAR) line = line.substring(0, line.length() - 1);
			}
			fitted[i] = line;
		}
		return fitted;
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
		}

		if (own != null && instance.getFlagManager().isCarrying(player)) {
			lines.add(main().color("&e&lCarrying a flag!"));
		}

		lines.add("");

		for (FWTeam team : instance.getTeams().values()) {
			// A team that never had a player isn't listed. One that left stays, since its flag is still at its base.
			if (team.isEmpty() && !team.isInactive()) continue;
			lines.add(main().color(teamStatusLine(own, team)));
		}

		lines.add("");
		lines.add(main().color(FOOTER));

		board.updateLines(fit(lines.toArray(new String[0])));
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

	/**
	 * U+27A1, picked by measuring the 1.8 client font: about 8 px wide and 5 px tall, centered on the
	 * text line, so it sits level with the letters. U+2192 is only 3 px wide and U+279C is as tall as a
	 * capital letter. Written as an escape so a look-alike arrow can't sneak in through a copy and paste.
	 */
	private static final String ARROW = "\u27A1";
	private static final String YOU_TAG = " &7&oYou";
	/** FastBoard on 1.8 throws on a line over 30 characters (color codes count). */
	private static final int MAX_LINE_LENGTH = 30;

	/**
	 * "Red (arrow) 0/3", with the flag count green. The viewer's own team ends with "You". A team that left
	 * is all gray and crossed out. The client draws a strikethrough in the color of the text it
	 * crosses, so a line in a different color than its text isn't possible. No checkmark by default, one only shows up next to a team once the viewer's
	 * own team has captured that team's flag.
	 */
	private String teamStatusLine(FWTeam viewerTeam, FWTeam team) {
		String icon = viewerTeam != null && viewerTeam.hasCapturedFrom(team.getSiteId()) ? "&a✔ " : "";
		String score = team.getCapturedFlags() + "/" + instance.getRequiredCaptures();

		if (team.isEmpty()) {
			// One color code for the whole line, since any other one would end the strikethrough
			return icon + "&7&m" + team.getName() + " " + ARROW + " " + score;
		}

		String line = icon + teamLabel(team) + " &7" + ARROW + " &a" + score;
		if (team == viewerTeam && (line + YOU_TAG).length() <= MAX_LINE_LENGTH) line += YOU_TAG;
		return line;
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

	private static final String HIDDEN_NAME_TAG_TEAM = "fw_hidden";

	/**
	 * Invisibility Potion: no name tag above their head. 1.8 clients still show an invisible player's tag when
	 * their team's tags are always visible, so they're moved to a team that never shows it until applyNameTag
	 * puts them back.
	 */
	public void hideNameTag(Player player) {
		Team hidden = nameTagScoreboard().getTeam(HIDDEN_NAME_TAG_TEAM);
		if (hidden == null) {
			hidden = nameTagScoreboard().registerNewTeam(HIDDEN_NAME_TAG_TEAM);
			hidden.setNameTagVisibility(NameTagVisibility.NEVER);
		}
		if (!hidden.hasEntry(player.getName())) hidden.addEntry(player.getName());
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
}
