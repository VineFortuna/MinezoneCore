package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameType;
import fr.minuskube.inv.SmartInventory;

public class ClassicModeGUI extends GenericModeGUI {

    public ClassicModeGUI(Core main, SmartInventory parent) {
        super(main, parent, GameType.CLASSIC, "Classic Maps", 6, 9);
    }
}
