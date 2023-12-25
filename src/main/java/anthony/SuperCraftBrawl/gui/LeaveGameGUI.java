package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;

public class LeaveGameGUI extends ConfirmationGUI {

	public LeaveGameGUI(Core main) {
		super(main, "Leave game?", player -> main.getCommands().leaveGame(player), null);
	}
}
