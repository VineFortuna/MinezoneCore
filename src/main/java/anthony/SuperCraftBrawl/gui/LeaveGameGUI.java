package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.entity.HumanEntity;

public class LeaveGameGUI extends ConfirmationGUI {

	public LeaveGameGUI(Core main) {
		super(main, "Leave game?", player -> main.getCommands().leaveGame(player), HumanEntity::closeInventory);
	}
}
