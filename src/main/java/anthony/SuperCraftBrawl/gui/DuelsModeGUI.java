package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameType;
import fr.minuskube.inv.SmartInventory;


public class DuelsModeGUI extends GenericModeGUI {

    public DuelsModeGUI(Core main, SmartInventory parent) {
        super(main, parent, GameType.DUEL, "Duels Maps", 4, 9);
    }
}
