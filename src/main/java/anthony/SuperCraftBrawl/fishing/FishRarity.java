package anthony.SuperCraftBrawl.fishing;

import org.bukkit.ChatColor;

public enum FishRarity {
    JUNK("Junk", "&c", 20),
    COMMON("Common", "&7", 40),
    RARE("Rare", "&a", 30),
    EPIC("Epic", "&d", 20),
    MYTHIC("Mythic", "&c", 8),
    LEGENDARY("Legendary", "&6", 2),
    TREASURE("Treasure", "&6", 2);
    
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