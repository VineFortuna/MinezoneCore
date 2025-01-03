package anthony.SuperCraftBrawl.fishing;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public enum FishArea {
    
    // CLASSIC
    Pond(
            "Pond",
            new Vector(287, 94, 519),
            30, 15, 35,
            new Vector(302.5, 91, 527.5),
            new ItemStack(Material.GRASS)),
    LushCave(
            "Lush Cave",
            new Vector(265, 105, 629),
            32, 20, 41,
            new Vector(280.5, 107, 625.5),
            new ItemStack(Material.GLOWSTONE)),
    Park("City Park",
            new Vector(98, 115, 950),
            32, 20, 42,
            new Vector(96.5, 113, 923.5),
            new ItemStack(Material.APPLE)),
    /*Shores("Rocky Shores",
            new Vector(46, 80, 555),
            25, 15, 35,
            new Vector(56.5, 78, 569.5),
            new ItemStack(Material.CLAY)),*/
    Woods("Woods",
            new Vector(218, 107, 586),
            12, 14, 12,
            new Vector(204.5, 105, 582.5),
            new ItemStack(Material.LEAVES));
    
    private final String name;
    private final Vector location;
    private final int boundsX;
    private final int boundsY;
    private final int boundsZ;
    private final Vector spawnPoint;
    private final ItemStack displayItem;
    
    FishArea(String name, Vector location, int boundsX, int boundsY, int boundsZ,
             Vector spawnPoint, ItemStack displayItem) {
        this.name = name;
        this.location = location;
        this.boundsX = boundsX;
        this.boundsY = boundsY;
        this.boundsZ = boundsZ;
        this.spawnPoint = spawnPoint;
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
    public Location getSpawnPoint(World world) {
        return new Location(world, spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
    }
    
    public boolean isInBounds(Location loc) {
        Vector v = location;
        Location centre = new Location(loc.getWorld(), v.getX(), v.getY(), v.getZ());
        
        if (Math.abs(centre.getX() - loc.getX()) > boundsX)
            return false;
        if (Math.abs(centre.getY() - loc.getY()) > boundsY)
            return false;
        if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ)
            return false;
        return true;
    }

    public int getID() {
        return ordinal();
    }
}

