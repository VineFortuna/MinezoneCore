package anthony.flagwars.vote;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.ConfirmationGUI;
import anthony.flagwars.FWGameManager;

/** Mirrors SuperCraftBros' own LeaveGameGUI - a yes/no confirmation before actually leaving the match. */
public class FWLeaveGameGUI extends ConfirmationGUI {

	public FWLeaveGameGUI(Core main, FWGameManager fwGameManager) {
		super(main, "Leave game?", player -> fwGameManager.leaveGame(player), null);
	}
}
