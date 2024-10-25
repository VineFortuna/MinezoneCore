package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameType;
import fr.minuskube.inv.SmartInventory;

public class FrenzyModeGUI extends GenericModeGUI {

    public FrenzyModeGUI(Core main, SmartInventory parent) {
        super(main, parent, GameType.FRENZY, "Frenzy", 3, 9);
    }
}
