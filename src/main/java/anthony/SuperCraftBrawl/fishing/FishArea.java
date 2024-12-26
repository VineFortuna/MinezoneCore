package anthony.SuperCraftBrawl.Game.map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public enum FishArea {
    
    // CLASSIC
    Pond("Pond", new Vector(216.500, 160, 382.500), 35, 45, new ItemStack(Material.GRASS)),
    LushCave("Lush Cave", new Vector(261.500, 105, 629.500), 35, 45, new ItemStack(Material.LEAVES));
    
    private final String name;
    private final Vector location;
    private final int boundsX;
    private final int boundsZ;
    private final ItemStack displayItem;
    
    FishArea(String name, Vector location, int boundsX, int boundsZ, ItemStack displayItem) {
        this.name = name;
        this.location = location;
        this.boundsX = boundsX;
        this.boundsZ = boundsZ;
        this.displayItem = displayItem;
    }
    
    public String getName() {
        return name;
    }
    
    public Vector getLocation() {
        return location;
    }
    
    public ItemStack getDisplayItem() {
        return displayItem;
    }
    
    public boolean isInBounds(Location loc) {
        Vector v = location;
        Location centre = new Location(loc.getWorld(), v.getX(), v.getY(), v.getZ());
        
        if (Math.abs(centre.getX() - loc.getX()) > boundsX)
            return false;
        return !(Math.abs(centre.getZ() - loc.getZ()) > boundsZ);
    }
}

