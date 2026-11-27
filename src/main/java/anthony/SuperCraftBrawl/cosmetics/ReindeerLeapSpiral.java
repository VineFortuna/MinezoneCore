package anthony.SuperCraftBrawl.cosmetics;

import java.util.ArrayList;
import java.util.List;

/**
 * The shape of the Reindeer Leap double jump: a double helix under the player's feet, two strands 180 degrees apart that
 * start narrow at the feet and widen as they go down, like an inverted funnel. One strand is bright red (Rudolph's
 * nose), the other gold and amber. It is drawn a slice at a time over {@link #SPIRAL_TICKS} ticks, and is finished by a
 * small gold ring that grows over {@link #RING_TICKS} ticks at the bottom.
 *
 * This only works out where the dust goes, relative to the player's feet (x, y, z), so it needs no server and holds no
 * state. Everything is below the feet: y is never above {@code -TOP}.
 */
public final class ReindeerLeapSpiral {

    /** How far down the shape reaches from the feet, in blocks. */
    public static final double DEPTH = 1.5;
    /** The top of the helix starts this far below the feet, so that nothing is ever above them. */
    public static final double TOP = 0.05;
    public static final double START_RADIUS = 0.2;
    public static final double END_RADIUS = 0.9;

    /** The helix is drawn over this many ticks (one slice each). */
    public static final int SPIRAL_TICKS = 10;
    /** Then the ring grows over this many more. */
    public static final int RING_TICKS = 3;
    public static final int TOTAL_TICKS = SPIRAL_TICKS + RING_TICKS;

    /** How many times each strand goes round on the way down. */
    private static final double TURNS = 1.5;
    /** Dust dots per strand in each slice. */
    private static final int DOTS_PER_TICK = 5;

    /** The ring starts as wide as the bottom of the helix and grows by this much each tick. */
    private static final double RING_GROWTH = 0.25;
    /** About this many blocks between the dots of the ring. */
    private static final double RING_DOT_SPACING = 0.22;

    // Dust colours (0 to 255 per channel). Strand 0 is Rudolph's nose, strand 1 is gold and amber
    private static final int[][] RED = {{255, 25, 25}, {255, 75, 45}};
    private static final int[][] GOLD_AMBER = {{255, 210, 30}, {255, 160, 10}};
    private static final int[][] RING = {{255, 215, 0}, {255, 235, 120}};

    private ReindeerLeapSpiral() {
    }

    /** How far along the helix (0 at the top, 1 at the bottom) a slice's dot is. */
    private static double along(int tick, int dot) {
        return (double) (tick * DOTS_PER_TICK + dot + 1) / (SPIRAL_TICKS * DOTS_PER_TICK);
    }

    public static double radiusAt(double along) {
        return START_RADIUS + (END_RADIUS - START_RADIUS) * along;
    }

    /** How far below the feet (a positive number) a point of the helix is. */
    public static double depthAt(double along) {
        return TOP + (DEPTH - TOP) * along;
    }

    /** The angle of the first strand at a point along the helix. The other strand is half a turn behind it. */
    public static double angleAt(double along) {
        return along * TURNS * 2 * Math.PI;
    }

    /**
     * The dust of one slice of the helix, both strands: x, y, z (from the feet) and red, green, blue.
     * Ticks 0 to {@link #SPIRAL_TICKS} - 1; the two strands come in pairs, the second half a turn round from the first.
     */
    public static List<double[]> spiral(int tick) {
        List<double[]> dots = new ArrayList<double[]>();
        for (int i = 0; i < DOTS_PER_TICK; i++) {
            double along = along(tick, i);
            double radius = radiusAt(along);
            double angle = angleAt(along);
            double y = -depthAt(along);

            int[] red = RED[i % 2];
            dots.add(new double[]{Math.cos(angle) * radius, y, Math.sin(angle) * radius, red[0], red[1], red[2]});
            int[] gold = GOLD_AMBER[i % 2];
            dots.add(new double[]{Math.cos(angle + Math.PI) * radius, y, Math.sin(angle + Math.PI) * radius,
                    gold[0], gold[1], gold[2]});
        }
        return dots;
    }

    /**
     * Where a spark goes in a given slice, between the two strands: half a quarter turn from each, on the funnel.
     * x, y, z from the feet.
     *
     * @param within  how far through the slice, 0 to 1
     */
    public static double[] spark(int tick, double within) {
        double along = (tick + within) / SPIRAL_TICKS;
        double radius = radiusAt(along);
        double angle = angleAt(along) + Math.PI / 2;
        return new double[]{Math.cos(angle) * radius, -depthAt(along), Math.sin(angle) * radius};
    }

    /**
     * The gold ring at the bottom, as it is on one tick of its growth (0 to {@link #RING_TICKS} - 1): x, y, z and
     * colour. It starts as wide as the bottom of the helix.
     */
    public static List<double[]> ring(int step) {
        double radius = END_RADIUS + RING_GROWTH * step;
        int count = Math.max(8, (int) Math.round(2 * Math.PI * radius / RING_DOT_SPACING));

        List<double[]> dots = new ArrayList<double[]>();
        for (int i = 0; i < count; i++) {
            double angle = i * 2 * Math.PI / count;
            int[] color = RING[i % 2];
            dots.add(new double[]{Math.cos(angle) * radius, -DEPTH, Math.sin(angle) * radius, color[0], color[1], color[2]});
        }
        return dots;
    }
}
