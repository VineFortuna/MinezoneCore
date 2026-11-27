package anthony.SuperCraftBrawl.Game;

import org.bukkit.Material;

/**
 * Where to put a lightning drop that has ended up in lava or fire. They can't burn up there (see
 * GameManager#protectLootDrops), but an item in lava is thrown up and sideways every tick and is hidden by it, so
 * the drop would be there but out of sight and out of reach. This finds the closest bit of ground next to it.
 *
 * Only looks at block types, so it needs no server, see {@link Blocks}.
 */
public final class LootRescue {

    /** How far from the drop the ground is looked for. */
    private static final int MAX_DISTANCE = 8;
    /** Heights to try in a column, the drop's own first: ground can be higher or lower than the lava. */
    private static final int[] HEIGHTS = {0, 1, -1, 2, -2, 3, -3};

    private LootRescue() {}

    /** The block types of a world, or null for a block that isn't loaded. */
    public interface Blocks {
        Material typeAt(int x, int y, int z);
    }

    /** Whether something burns an item that is in this block. */
    public static boolean burns(Material type) {
        return type == Material.LAVA || type == Material.STATIONARY_LAVA || type == Material.FIRE;
    }

    /** A block an item can lie in: air and the like, not water, lava or fire. */
    private static boolean isFree(Material type) {
        return type != null && !type.isSolid() && !burns(type)
                && type != Material.WATER && type != Material.STATIONARY_WATER;
    }

    /** A block an item can lie on. */
    private static boolean isGround(Material type) {
        return type != null && type.isSolid() && type != Material.CACTUS;
    }

    private static boolean isSafe(Blocks blocks, int x, int y, int z, boolean awayFromFire) {
        if (y < 1 || y > 254) return false;
        if (!isFree(blocks.typeAt(x, y, z)) || !isGround(blocks.typeAt(x, y - 1, z))) return false;
        if (!awayFromFire) return true;

        // Nothing burning beside it or beside the ground it lies on, where it could be pushed back or set alight
        int[][] beside = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] side : beside) {
            if (burns(blocks.typeAt(x + side[0], y, z + side[1]))
                    || burns(blocks.typeAt(x + side[0], y - 1, z + side[1]))) return false;
        }
        return true;
    }

    /**
     * The block to put the drop in, as x, y, z, or null if there is no ground within reach. The closest wins, and it
     * is looked for twice: first with no lava or fire beside it, then anywhere that isn't in it.
     */
    public static int[] findSafeSpot(Blocks blocks, int x, int y, int z) {
        for (boolean awayFromFire : new boolean[]{true, false}) {
            for (int distance = 0; distance <= MAX_DISTANCE; distance++) {
                int[] best = null;
                long bestScore = Long.MAX_VALUE;

                for (int dx = -distance; dx <= distance; dx++) {
                    for (int dz = -distance; dz <= distance; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != distance) continue; // This ring only

                        for (int dy : HEIGHTS) {
                            if (!isSafe(blocks, x + dx, y + dy, z + dz, awayFromFire)) continue;

                            long score = (long) dx * dx + (long) dz * dz + (long) dy * dy;
                            if (score < bestScore) {
                                bestScore = score;
                                best = new int[]{x + dx, y + dy, z + dz};
                            }
                            break; // The first height that works in this column is the nearest to the drop
                        }
                    }
                }
                if (best != null) return best;
            }
        }
        return null;
    }
}
