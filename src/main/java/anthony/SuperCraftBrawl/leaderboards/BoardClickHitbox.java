package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.leaderboard.LeaderboardScopeGUI;
import anthony.util.SoundManager;
import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Makes the leaderboards clickable. The boards are packet armor stands, so the server has no entity to click and
 * Bukkit never fires an event for them. The click still arrives as a use entity packet with the id the client was
 * shown, which {@link anthony.SuperCraftBrawl.npcs.ChannelInjector} reads (it does the same for the NPCs).
 *
 * The lines themselves are too thin to click (0.5 blocks wide, the text is about 3 wide), so each board also gets one
 * invisible slime, sized to cover the whole board. Every board class sends it together with its lines and keeps the id
 * in the same list as the lines, so it is destroyed with them whenever the board is redrawn.
 *
 * Nothing is stored here for the hitboxes. Their ids come from a range of their own, so a click is recognised by
 * the id alone and there is no registry that could grow.
 */
public final class BoardClickHitbox {

    /** The ids start far above anything the server or the boards hand out, and wrap around inside their range. */
    private static final int ID_BASE = 1_000_000_000;
    private static final int ID_MASK = 0xFFFFFF;
    private static final AtomicInteger NEXT = new AtomicInteger();

    /** A slime of size n is 0.51 * n blocks wide and tall. */
    private static final double SLIME_UNIT = 0.51;
    private static final int MIN_SIZE = 6;
    private static final int MAX_SIZE = 10;

    /**
     * A line's text is drawn about 2.3 blocks above its armor stand, so the box covers from a little above the title's
     * text to a little below the last line's.
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
            return null; // the hitbox keeps the server's id then, which is never recognised as a click
        }
    }

    /** The slime packet and the id to destroy it with. Send it with {@link #sendTo}. */
    public static final class Hitbox {
        public final int id;
        private final PacketPlayOutSpawnEntityLiving spawn;

        private Hitbox(int id, PacketPlayOutSpawnEntityLiving spawn) {
            this.id = id;
            this.spawn = spawn;
        }

        public void sendTo(Player viewer) {
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

        int size = (int) Math.ceil((top - bottom) / SLIME_UNIT);
        size = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size));
        double feetY = (top + bottom) / 2.0 - size * SLIME_UNIT / 2.0;

        EntitySlime slime = new EntitySlime(((CraftWorld) title.getWorld()).getHandle());
        slime.setLocation(title.getX(), feetY, title.getZ(), 0, 0);
        slime.setSize(size);
        slime.setInvisible(true);

        int id = slime.getId();
        if (ID_FIELD != null) {
            int custom = ID_BASE + (NEXT.getAndIncrement() & ID_MASK);
            try {
                ID_FIELD.setInt(slime, custom);
                id = custom;
            } catch (Throwable ignored) {
            }
        }

        return new Hitbox(id, new PacketPlayOutSpawnEntityLiving(slime));
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
