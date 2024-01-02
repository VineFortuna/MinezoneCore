package anthony.SuperCraftBrawl;

import net.md_5.bungee.api.ChatColor;
public enum Currencies {
    TOKENS("Token", "Tokens", ChatColor.YELLOW),
    GEMS("Gem", "Gems", ChatColor.GREEN),
    SHARDS("Shard", "Shards", ChatColor.LIGHT_PURPLE);

    private final String singularName;
    private final String pluralName;
    private final ChatColor color;

    Currencies(String singularName, String pluralName, ChatColor color) {
        this.singularName = singularName;
        this.pluralName = pluralName;
        this.color = color;
    }

    /**
     * Checks if returned Currency String name should be singular or plural.
     *
     * @param amount context amount of the currency to determine singular or plural.
     * @return the String name of the currency in desired singular/plural form.
     */

    public String getName(int amount) {
        if (amount == 1) {
            return getSingularName();
        } else if (amount > 1) {
            return getPluralName();
        } else return null;
    }

    public String getSingularName() {
        return singularName;
    }

    public String getPluralName() {
        return pluralName;
    }

    public ChatColor getColor() {
        return color;
    }
}