package anthony.SuperCraftBrawl.fishing;

import org.bukkit.ChatColor;

public enum FishRarity {
    // SEA CREATURES
    COMMON("Common", "&7", 40),
    RARE("Rare", "&a", 26),
    EPIC("Epic", "&d", 18),
    MYTHIC("Mythic", "&c", 10),
    LEGENDARY("Legendary", "&6", 4),

    JUNK("Junk", "&4", 12),
    TREASURE("Treasure", "&e", 3);
    
    private String display;
    private String color;
    private int chance;
    
    private FishRarity(String display, String color, int chance) {
        this.display = display;
        this.color = color;
        this.chance = chance;
    }
    
    public String getName() {
        return display;
    }
    public int getChance() { return this.chance; }
    public String getColor() { return this.color; }
}