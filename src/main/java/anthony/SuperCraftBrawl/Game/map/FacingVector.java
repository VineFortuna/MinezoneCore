package anthony.SuperCraftBrawl.Game.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

/**
 * A point of a map (lobby, spawn, spectator) that also remembers which way to face. It is a {@link Vector}, so
 * everything that takes the points of a map takes this too.
 *
 * The maps made before this had no direction and their points stay plain vectors, which turn into a location with the
 * direction a new {@link Location} has (0, 0), exactly as they always did. Only points set with /scbsetup carry one.
 * In maps.yml a point is [x, y, z], or [x, y, z, yaw, pitch] with a direction.
 */
public final class FacingVector extends Vector {

    private final float yaw;
    private final float pitch;

    public FacingVector(double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.yaw = normalizeYaw(yaw);
        this.pitch = normalizePitch(pitch);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public FacingVector clone() {
        return (FacingVector) super.clone();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && ((FacingVector) other).yaw == yaw && ((FacingVector) other).pitch == pitch;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + Float.floatToIntBits(yaw);
    }

    /** The location of a point of a map in a world: facing the way it says, or the default direction if it has none. */
    public static Location toLocation(World world, Vector point) {
        Location location = new Location(world, point.getX(), point.getY(), point.getZ());
        if (point instanceof FacingVector) {
            location.setYaw(((FacingVector) point).yaw);
            location.setPitch(((FacingVector) point).pitch);
        }
        return location;
    }

    /** Between -180 (not included) and 180, to a tenth of a degree. A player's yaw can be any number of turns. */
    static float normalizeYaw(float yaw) {
        float wrapped = yaw % 360f;
        if (wrapped > 180f) wrapped -= 360f;
        else if (wrapped <= -180f) wrapped += 360f;
        return Math.round(wrapped * 10f) / 10f;
    }

    /** Looking straight up or down at most, to a tenth of a degree. */
    static float normalizePitch(float pitch) {
        return Math.round(Math.max(-90f, Math.min(90f, pitch)) * 10f) / 10f;
    }

    /** The way a yaw faces, as words (0 is south, 90 is west). */
    public static String compass(float yaw) {
        String[] names = {"south", "south west", "west", "north west", "north", "north east", "east", "south east"};
        int index = Math.round(normalizeYaw(yaw) / 45f);
        return names[((index % 8) + 8) % 8];
    }
}
