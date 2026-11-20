package anthony.flagwars.vote;

import anthony.flagwars.FWGameInstance;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Tallies Flag Wars's lobby votes - mirrors SuperCraftBros' GameSettings.
 * The game-start vote fires immediately once every player is ready (skips
 * the rest of the queued countdown, see FWGameInstance#forceStartNow); the
 * night-time and double-generator-rate votes just need a majority and are
 * only read once - in FWGameInstance#lockInVoteSettings, 5 seconds before
 * the match begins, exactly like SCB locks its own settings in.
 */
public class FWGameSettings {

	private final FWGameInstance instance;

	private int startVotes;
	private int nightVotes;
	private int doubleGeneratorVotes;

	private final List<Player> startVoters = new ArrayList<>();
	private final List<Player> nightVoters = new ArrayList<>();
	private final List<Player> doubleGeneratorVoters = new ArrayList<>();

	public FWGameSettings(FWGameInstance instance) {
		this.instance = instance;
	}

	public int getStartVotes() {
		return startVotes;
	}

	public int getNightVotes() {
		return nightVotes;
	}

	public int getDoubleGeneratorVotes() {
		return doubleGeneratorVotes;
	}

	public boolean isNightVotePassed() {
		return majorityReached(nightVotes);
	}

	public boolean isDoubleGeneratorVotePassed() {
		return majorityReached(doubleGeneratorVotes);
	}

	/**
	 * Mirrors SCB's GameSettings#updateGameStartVoteStatus: every toggle is announced in chat, and
	 * - unlike the night-time/double-generator votes - this one requires every player ready (not
	 * just a majority) before it fires, exactly matching SCB's totalStartVotes == players.size().
	 */
	public void handleVoteGameStart(Player player) {
		boolean nowReady = !startVoters.contains(player);
		startVotes = toggleVote(startVoters, player, startVotes);

		String status = nowReady ? "&a is Ready " : "&c is no longer Ready ";
		instance.TellAll(instance.getManager().getMain().color("&2&l(!) &r" + player.getName() + status
				+ "(" + startVotes + "/" + instance.countPlayers() + ")"));

		if (startVotes == instance.countPlayers()) {
			instance.forceStartNow();
		}
	}

	public void handleVoteNightTime(Player player) {
		nightVotes = toggleVote(nightVoters, player, nightVotes);
	}

	public void handleVoteDoubleGenerators(Player player) {
		doubleGeneratorVotes = toggleVote(doubleGeneratorVoters, player, doubleGeneratorVotes);
	}

	/** Strips a player from every vote they cast - called when they leave mid-lobby. */
	public void removePlayer(Player player) {
		if (startVoters.remove(player)) startVotes--;
		if (nightVoters.remove(player)) nightVotes--;
		if (doubleGeneratorVoters.remove(player)) doubleGeneratorVotes--;
	}

	private int toggleVote(List<Player> voters, Player player, int currentVotes) {
		if (voters.contains(player)) {
			voters.remove(player);
			return currentVotes - 1;
		}
		voters.add(player);
		return currentVotes + 1;
	}

	private boolean majorityReached(int votes) {
		int total = Math.max(instance.countPlayers(), 1);
		return votes >= total / 2 + 1;
	}
}
