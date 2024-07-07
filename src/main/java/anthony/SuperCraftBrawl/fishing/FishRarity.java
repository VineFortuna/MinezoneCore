package anthony.SuperCraftBrawl.fishing;

import org.bukkit.ChatColor;

public enum FishRarity {
    JUNK("Junk", "&c", 16),
    COMMON("Common", "&7", 42),
    RARE("Rare", "&a", 26),
    EPIC("Epic", "&d", 18),
    MYTHIC("Mythic", "&c", 10),
    LEGENDARY("Legendary", "&6", 4),
    TREASURE("Treasure", "&6", 4);
    
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