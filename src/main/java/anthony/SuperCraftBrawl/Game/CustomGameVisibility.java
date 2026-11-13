package anthony.SuperCraftBrawl.Game;

import org.bukkit.Material;

/** Who can join a custom game: everyone, the host's friends (and party), or only the host's party. */
public enum CustomGameVisibility {

    PUBLIC("Public", "&a", Material.EMERALD_BLOCK),
    FRIENDS("Friends", "&e", Material.GOLD_BLOCK),
    PARTY("Party", "&c", Material.REDSTONE_BLOCK);

    private final String label;
    private final String color;
    private final Material block; // how menus show a game of this visibility

    CustomGameVisibility(String label, String color, Material block) {
        this.label = label;
        this.color = color;
        this.block = block;
    }

    public String getLabel() {
        return label;
    }

    /** The color code alone, e.g. "&a". */
    public String getColor() {
        return color;
    }

    /** The colored name, e.g. "&aPublic". */
    public String getTag() {
        return color + label;
    }

    public Material getBlock() {
        return block;
    }

    public CustomGameVisibility next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
