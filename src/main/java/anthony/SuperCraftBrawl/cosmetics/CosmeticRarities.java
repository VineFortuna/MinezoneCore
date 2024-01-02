package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Currencies;
import net.md_5.bungee.api.ChatColor;

public enum CosmeticRarities {
    DEFAULT("Default", ChatColor.WHITE, 0, Currencies.TOKENS),
    COMMON("Common", ChatColor.YELLOW, 100, Currencies.TOKENS),
    UNCOMMON("Uncommon", ChatColor.GREEN, 250, Currencies.TOKENS),
    RARE("Rare", ChatColor.BLUE, 500, Currencies.TOKENS),
    EPIC("Epic", ChatColor.GOLD, 1000, Currencies.TOKENS),
    LEGENDARY("Legendary", ChatColor.RED, 1, Currencies.GEMS),
    MYTHICAL("Mythical", ChatColor.LIGHT_PURPLE, 1, Currencies.SHARDS);

    private final String name;
    private final ChatColor color;
    private final int baseCost;
    private final Currencies associatedCurrency;

    CosmeticRarities(String name, ChatColor color, int baseCost, Currencies associatedCurrency) {
        this.name = name;
        this.color = color;
        this.baseCost = baseCost;
        this.associatedCurrency = associatedCurrency;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public Currencies getAssociatedCurrency() {
        return associatedCurrency;
    }
}
