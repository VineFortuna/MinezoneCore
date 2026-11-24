package anthony.flagwars.vote;

import anthony.flagwars.FWGameInstance;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks Flag Wars's lobby settings - mirrors SuperCraftBros' GameSettings.
 * Ready-up (the ready dye, see FWLobbyItems) fires immediately once every
 * player is ready, skipping the rest of the queued countdown (see
 * FWGameInstance#forceStartNow).
 * <p>
 * The night-time and double-generator-rate votes just need a majority and are
 * only read once - in FWGameInstance#lockInVoteSettings, 5 seconds before
 * the match begins, exactly like SCB locks its own settings in. Nothing in the
 * lobby lets players cast them since the vote paper was replaced by the ready
 * dye, so they never pass for now; they are kept for a settings menu to use.
 */
public class FWGameSettings {

	private final FWGameInstance instance;

	private int readyCount;
	private int nightVotes;
	private int doubleGeneratorVotes;

	private final List<Player> readyPlayers = new ArrayList<>();
	private final List<Player> nightVoters = new ArrayList<>();
	private final List<Player> doubleGeneratorVoters = new ArrayList<>();

	public FWGameSettings(FWGameInstance instance) {
		this.instance = instance;
	}

	public int getReadyCount() {
		return readyCount;
	}

	public boolean isReady(Player player) {
		return readyPlayers.contains(player);
	}

	/** Nobody is ready anymore, for when the countdown stops. */
	public void clearReady() {
		readyPlayers.clear();
		readyCount = 0;
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
	 * The player clicked the ready-up dye: ready <-> not ready. Mirrors SCB's GameSettings#toggleReady: every
	 * toggle is announced in chat, and every player has to be ready (not just a majority) before the game
	 * starts early, exactly matching SCB's total ready == players.size().
	 */
	public void toggleReady(Player player) {
		boolean nowReady = !readyPlayers.contains(player);
		readyCount = toggleVote(readyPlayers, player, readyCount);

		String status = nowReady ? "&a is Ready " : "&c is no longer Ready ";
		instance.TellAll(instance.getManager().getMain().color("&2&l(!) &r" + player.getName() + status
				+ "(" + readyCount + "/" + instance.countPlayers() + ")"));

		if (readyCount == instance.countPlayers()) {
			instance.forceStartNow();
		}
	}

	public void handleVoteNightTime(Player player) {
		nightVotes = toggleVote(nightVoters, player, nightVotes);
	}

	public void handleVoteDoubleGenerators(Player player) {
		doubleGeneratorVotes = toggleVote(doubleGeneratorVoters, player, doubleGeneratorVotes);
	}

	/** Strips a player from being ready and from every vote they cast - called when they leave mid-lobby. */
	public void removePlayer(Player player) {
		if (readyPlayers.remove(player)) readyCount--;
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
