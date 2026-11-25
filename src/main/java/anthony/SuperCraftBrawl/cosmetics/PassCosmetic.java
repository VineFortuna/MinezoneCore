package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.battlepass.BattlePassManager;
import anthony.SuperCraftBrawl.battlepass.BattlePassRewards;
import anthony.util.ItemHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A cosmetic that's earned from the Battle Pass. Unlocking is stored permanently by the
 * BattlePassManager, and the "how to unlock" line is filled in from whichever pass tier hands it out
 * (see BattlePassRewards), so tier numbers are only ever written once.
 * <p>
 * Subclass anonymously and override whatever the cosmetic actually does (onEquip, tick, ...).
 */
public class PassCosmetic extends Cosmetic {

    protected final Core main;
    private final String iconName;
    private final Material iconMaterial;
    private final short iconData;
    private final String[] description;

    /**
     * @param iconName    name shown on the icon, with & color codes (the title cosmetics are bold)
     * @param description gray lore lines under the name (& color codes)
     */
    public PassCosmetic(Core main, CosmeticCategory category, String id, String displayName, String iconName,
                        Material iconMaterial, int iconData, String... description) {
        super(id, category, displayName, BattlePassRewards.sourceOf(category, id));
        this.main = main;
        this.iconName = iconName;
        this.iconMaterial = iconMaterial;
        this.iconData = (short) iconData;
        this.description = description;
    }

    public String[] getDescription() {
        return description.clone();
    }

    /** The plain item the icon is built on (a new one on every call). Override it for an icon that's a custom head. */
    protected ItemStack baseIcon() {
        return new ItemStack(iconMaterial, 1, iconData);
    }

    @Override
    public ItemStack getIcon(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        for (String line : description)
            lore.add(line);

        ItemStack icon = ItemHelper.setDetails(baseIcon(), iconName, lore.toArray(new String[0]));
        ItemHelper.setHideFlags(icon, true);
        return withRequirementLore(icon);
    }

    @Override
    public boolean isUnlocked(Player player) {
        BattlePassManager manager = main.getBattlePassManager();
        return manager != null && manager.hasCosmetic(player, category, id);
    }

    @Override
    public String getUnlockMessage(Player player) {
        return main.color("&c&l(!) &rYou can unlock this from the &6Battle Pass&r! Type &e/battlepass &rto view it.");
    }

    @Override
    public void onEquip(Player player) {
    }

    @Override
    public void onUnequip(Player player) {
    }
}
