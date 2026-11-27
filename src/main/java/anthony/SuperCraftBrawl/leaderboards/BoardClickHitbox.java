package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.leaderboard.LeaderboardScopeGUI;
import anthony.util.SoundManager;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Makes the leaderboards clickable. The boards are packet armor stands, so the server has no entity to click and
 * Bukkit never fires an event for them. The click still arrives as a use entity packet with the id the client was
 * shown, which {@link anthony.SuperCraftBrawl.npcs.ChannelInjector} reads (it does the same for the NPCs).
 *
 * The lines themselves are too thin to click (0.5 blocks wide, the text is about 3 wide), so each board also gets a
 * few invisible armor stands that cover its text. They are thin on purpose: the first version was one big invisible
 * slime, and a slime can only be a cube, 3 to 5 blocks wide AND deep, so a player could stand inside it and every
 * click, a few blocks from the board and not even aimed at it, opened the settings. Armor stands are 0.5 blocks
 * across, can't be pushed, and are laid out in a plus shape (two rows crossing in the middle), because the text turns
 * to face whoever looks at it and no single row would be wide enough from every side. Seen from any direction the plus
 * covers the text, and a click anywhere else misses it.
 *
 * Every board class sends them together with its lines and keeps the ids in the same list as the lines, so they are
 * destroyed with them whenever the board is redrawn.
 *
 * Nothing is stored here for the hitboxes. Their ids come from a range of their own, so a click is recognised by
 * the id alone and there is no registry that could grow.
 */
public final class BoardClickHitbox {

    /** The ids start far above anything the server or the boards hand out, and wrap around inside their range. */
    private static final int ID_BASE = 1_000_000_000;
    private static final int ID_MASK = 0xFFFFFF;
    private static final AtomicInteger NEXT = new AtomicInteger();

    /** A standing (not small) armor stand is this tall, and 0.5 wide. */
    private static final double STAND_HEIGHT = 1.975;
    /** How far from the middle of the board the armor stands go along each of the two rows, which is about the text. */
    private static final double[] ROW_OFFSETS = {0.65, 1.3};

    /**
     * A line's text is drawn about 2.3 blocks above its armor stand, so the hitbox covers from a little above the
     * title's text to a little below the last line's.
     */
    private static final double ABOVE_TITLE = 2.6;
    private static final double ABOVE_LAST_LINE = 2.1;

    private static final Field ID_FIELD = findIdField();

    private BoardClickHitbox() {
    }

    private static Field findIdField() {
        try {
            Field field = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("id");
            field.setAccessible(true);
            return field;
        } catch (Throwable t) {
            return null; // the hitbox keeps the server's ids then, which are never recognised as a click
        }
    }

    /** The armor stand packets and the ids to destroy them with. Send them with {@link #sendTo}. */
    public static final class Hitbox {
        /** Every id of this hitbox: add all of them to the list the board's lines are destroyed from. */
        public final List<Integer> ids;
        private final List<PacketPlayOutSpawnEntityLiving> spawns;

        private Hitbox(List<Integer> ids, List<PacketPlayOutSpawnEntityLiving> spawns) {
            this.ids = Collections.unmodifiableList(ids);
            this.spawns = spawns;
        }

        public void sendTo(Player viewer) {
            for (PacketPlayOutSpawnEntityLiving spawn : spawns)
                ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(spawn);
        }
    }

    /**
     * @param title        where the board's title line stands
     * @param lowestLineY  where the lowest line stands, or a little lower if it isn't known exactly
     */
    public static Hitbox create(Location title, double lowestLineY) {
        double top = title.getY() + ABOVE_TITLE;
        double bottom = lowestLineY + ABOVE_LAST_LINE;
        net.minecraft.server.v1_8_R3.World world = ((CraftWorld) title.getWorld()).getHandle();

        List<Integer> ids = new ArrayList<>();
        List<PacketPlayOutSpawnEntityLiving> spawns = new ArrayList<>();

        for (double[] spot : standSpots(title.getX(), title.getZ(), bottom, top)) {
            EntityArmorStand stand = new EntityArmorStand(world);
            stand.setLocation(spot[0], spot[1], spot[2], 0, 0);
            stand.setInvisible(true);
            stand.setGravity(false);

            int id = stand.getId();
            if (ID_FIELD != null) {
                int custom = ID_BASE + (NEXT.getAndIncrement() & ID_MASK);
                try {
                    ID_FIELD.setInt(stand, custom);
                    id = custom;
                } catch (Throwable ignored) {
                }
            }

            ids.add(id);
            spawns.add(new PacketPlayOutSpawnEntityLiving(stand));
        }

        return new Hitbox(ids, spawns);
    }

    /**
     * Where the armor stands go, as x, feet y and z: a plus of columns around the middle of the board, each column
     * stacked stands to cover from the bottom to the top of the text.
     */
    static List<double[]> standSpots(double centerX, double centerZ, double bottom, double top) {
        double[][] columns = new double[1 + ROW_OFFSETS.length * 4][];
        int next = 0;
        columns[next++] = new double[]{0, 0};
        for (double offset : ROW_OFFSETS) {
            columns[next++] = new double[]{offset, 0};
            columns[next++] = new double[]{-offset, 0};
            columns[next++] = new double[]{0, offset};
            columns[next++] = new double[]{0, -offset};
        }

        // Stands stacked to cover the height, the lowest on the bottom and the highest on the top, spread evenly
        int levels = Math.max(1, (int) Math.ceil((top - bottom) / (STAND_HEIGHT - 0.1)));
        double[] feet = new double[levels];
        if (levels == 1) {
            feet[0] = (top + bottom) / 2.0 - STAND_HEIGHT / 2.0;
        } else {
            double step = ((top - bottom) - STAND_HEIGHT) / (levels - 1);
            for (int i = 0; i < levels; i++) feet[i] = bottom + i * step;
        }

        List<double[]> spots = new ArrayList<>();
        for (double[] column : columns)
            for (double y : feet) spots.add(new double[]{centerX + column[0], y, centerZ + column[1]});
        return spots;
    }

    /** Whether this is the id of one of the boards' hitboxes. */
    public static boolean isHitbox(int entityId) {
        return entityId >= ID_BASE && entityId <= ID_BASE + ID_MASK;
    }

    /** A click on a board. Comes in on the network thread, so the GUI opens on the main one. */
    public static void onClick(Player player) {
        Bukkit.getScheduler().runTask(Core.inst(), () -> openSettings(player));
    }

    private static void openSettings(Player player) {
        Core main = Core.inst();
        if (!player.isOnline() || !player.getWorld().equals(main.getLobbyWorld())) return;

        SoundManager.playClickSound(player);
        new LeaderboardScopeGUI(main).inv().open(player);
    }
}
