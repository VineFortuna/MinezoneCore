package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.types.KillEffect;

public class KillEffectsGUI extends GenericCosmeticTypeGUI {

    public KillEffectsGUI(Core main) {
        super(main, KillEffect.class, "Kill Effects", 6, 9);
    }
}
