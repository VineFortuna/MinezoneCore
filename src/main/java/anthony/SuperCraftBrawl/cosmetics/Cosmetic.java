package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Currencies;
import anthony.SuperCraftBrawl.cosmetics.types.KillEffect;
import anthony.SuperCraftBrawl.cosmetics.types.WinEffect;
import org.bukkit.inventory.ItemStack;

public class Cosmetic {
    private String name;
    private CosmeticRarities rarity;
    private String description;
    private ItemStack displayItem;
    private Class<? extends Cosmetic> cosmeticType;

    public Cosmetic(String name, CosmeticRarities rarity, String description, ItemStack displayItem, Class<? extends Cosmetic> cosmeticType) {
        this.name = name;
        this.rarity = rarity;
        this.description = description;
        this.displayItem = displayItem;
        this.cosmeticType = cosmeticType;
    }

    public String getName() {
        return name;
    }

    public CosmeticRarities getRarity() {
        return rarity;
    }

    public String getDescription() {
        return description;
    }

    public Currencies getCurrency() {
        return rarity.getAssociatedCurrency();
    }

    public int getCost() {
        return rarity.getBaseCost();
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public Class<? extends Cosmetic> getCosmeticType() {
        return cosmeticType;
    }

    public String getCosmeticTypeString(Class<? extends Cosmetic> cosmeticType) {
        String cosmeticTypeString;

        if (cosmeticType.equals(KillEffect.class)) {
            cosmeticTypeString = "Kill Effect";
        } else if (cosmeticType.equals(WinEffect.class)) {
            cosmeticTypeString = "Win Effect";
        } else return null;

        return cosmeticTypeString;
    }
}
