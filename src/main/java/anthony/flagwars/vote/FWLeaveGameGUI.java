package anthony.flagwars.vote;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.ConfirmationGUI;
import anthony.flagwars.FWGameManager;

/** Mirrors SuperCraftBros' own LeaveGameGUI - a yes/no confirmation before actually leaving the match. */
public class FWLeaveGameGUI extends ConfirmationGUI {

	public FWLeaveGameGUI(Core main, FWGameManager fwGameManager) {
		// Through /leave's own path, so the party leader takes the party with them like in every other game
		super(main, "Leave game?", player -> main.getCommands().leaveGame(player), null);
	}
}
