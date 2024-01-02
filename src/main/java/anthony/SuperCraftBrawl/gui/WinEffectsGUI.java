package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.cosmetics.types.WinEffect;
import anthony.SuperCraftBrawl.gui.cosmetics.GenericCosmeticTypeGUI;
import anthony.SuperCraftBrawl.Core;

public class WinEffectsGUI extends GenericCosmeticTypeGUI {

	public WinEffectsGUI(Core main) {
		super(main, WinEffect.class, "Win Effects", 6, 9);
	}
}
