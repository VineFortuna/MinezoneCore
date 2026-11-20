package anthony.flagwars;

import java.util.ArrayList;
import java.util.List;

/**
 * Solo (1 per island) or Duos (2 per island) - chosen when a player queues up. Actual max players scales with however many teams a given map has created; see FWGameInstance#getMaxPlayers.
 * <p>
 * A mode that isn't available can't be joined (FWGameManager#joinGame refuses it) and isn't offered in the mode
 * menu. Duos is built but not released yet, so it is switched off here. To release it, set its flag to true and
 * bring Duos back into the text that is written by hand: the lobby NPC (NPCManager), the Flag Wars description in
 * the game selector (GameSelectorGUI), and the /join usage in plugin.yml, Commands and FWCommand.
 */
public enum FWGameMode {
	SOLO("Solo", 1, 2, true),
	DUO("Duos", 2, 3, false);

	private final String label;
	private final int teamCapacity;
	private final int minPlayers;
	private final boolean available;

	FWGameMode(String label, int teamCapacity, int minPlayers, boolean available) {
		this.label = label;
		this.teamCapacity = teamCapacity;
		this.minPlayers = minPlayers;
		this.available = available;
	}

	public String getLabel() {
		return label;
	}

	/** How many players share one island/team in this mode. */
	public int getTeamCapacity() {
		return teamCapacity;
	}

	/** How many players must be queued before the countdown starts. */
	public int getMinPlayers() {
		return minPlayers;
	}

	/** Whether players can queue for this mode right now. */
	public boolean isAvailable() {
		return available;
	}

	/** Every mode players can queue for right now, in menu order. */
	public static List<FWGameMode> availableModes() {
		List<FWGameMode> modes = new ArrayList<>();
		for (FWGameMode mode : values()) {
			if (mode.available) modes.add(mode);
		}
		return modes;
	}
}
