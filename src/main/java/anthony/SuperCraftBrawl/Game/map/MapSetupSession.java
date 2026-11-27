package anthony.SuperCraftBrawl.Game.map;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * One player setting up one map with the /scbsetup items. The {@link #draft} is the working copy of the map: nothing in it
 * is kept (or used by games) until /scbsetup save writes it to maps.yml, the same way the world's blocks are only kept by it.
 *
 * Several players can set up the same map at once (/scbsetup create, then /scbsetup tp from the others). They each have
 * their own session, but share the draft, the world and the boundary corners, so what one of them sets is what the others
 * see. What the spawn locations item is doing (cycling, editing) is each player's own. A session is gone when the player
 * leaves, and holds nothing that outlives that.
 */
final class MapSetupSession {

    /** What the spawn locations item is doing. */
    enum SpawnMode {
        /** Nothing in particular: it lists, adds and starts the cycling. */
        NORMAL,
        /** Left and right click go through the spawns, each one is teleported to. */
        CYCLE,
        /** The same, by itself, a spawn every few seconds. */
        AUTO,
        /** One spawn is being moved to where the player stands. */
        EDIT
    }

    /** The two corners of the boundary rectangle, as they were set. The bounds in the draft come from these. */
    static final class Corners {
        Vector pos1;
        Vector pos2;
    }

    final UUID player;
    final MapDraft draft;
    final World world;
    /** The map's world was made by /scbsetup create (and so is thrown away if it is never saved). */
    final boolean created;
    /** Shared with everyone else setting up this map. */
    final Corners corners;

    // The spawn locations item, for this player only
    SpawnMode spawnMode = SpawnMode.NORMAL;
    /** Cycle or normal: where cancelling (or finishing) an edit goes back to. */
    SpawnMode modeBeforeEdit = SpawnMode.NORMAL;
    /** The spawn that cycling or editing is on, the very object in draft.spawns. Null in the normal mode. */
    Vector current;
    private BukkitTask autoTask;

    // The last tool use, to ignore a click the client sends twice
    private SetupTool lastTool;
    private boolean lastLeft;
    private long lastUse;

    /** The first one to set a map up. */
    MapSetupSession(UUID player, MapDraft draft, World world, boolean created) {
        this.player = player;
        this.draft = draft;
        this.world = world;
        this.created = created;
        this.corners = new Corners();

        // A map that already has bounds starts with its two corners, so just one of them can be changed
        if (draft.hasBounds()) {
            corners.pos1 = new Vector(draft.center.getX() - draft.boundsX, draft.center.getY(), draft.center.getZ() - draft.boundsZ);
            corners.pos2 = new Vector(draft.center.getX() + draft.boundsX, draft.center.getY(), draft.center.getZ() + draft.boundsZ);
        }
    }

    /** Another player setting up the same map: the same draft, world and corners. */
    MapSetupSession(UUID player, MapSetupSession joined) {
        this.player = player;
        this.draft = joined.draft;
        this.world = joined.world;
        this.created = joined.created;
        this.corners = joined.corners;
    }

    /** True if this is the same click as the one just before it, which should only count once. */
    boolean isRepeat(SetupTool tool, boolean left) {
        long now = System.currentTimeMillis();
        boolean repeat = tool == lastTool && left == lastLeft && now - lastUse < 150;
        lastTool = tool;
        lastLeft = left;
        lastUse = now;
        return repeat;
    }

    /** Makes the boundary rectangle in the draft from the two corners, if both are set. */
    void applyBounds() {
        Vector pos1 = corners.pos1;
        Vector pos2 = corners.pos2;
        if (pos1 == null || pos2 == null) return;
        draft.center = new Vector((pos1.getX() + pos2.getX()) / 2, Math.max(pos1.getY(), pos2.getY()),
                (pos1.getZ() + pos2.getZ()) / 2);
        draft.boundsX = Math.abs(pos1.getX() - pos2.getX()) / 2;
        draft.boundsZ = Math.abs(pos1.getZ() - pos2.getZ()) / 2;
    }

    // ---------------------------------------------------------------- the automatic cycling

    void setAutoTask(BukkitTask task) {
        cancelAuto();
        autoTask = task;
    }

    /** Stops the automatic cycling if it is going. Safe to call any time. */
    void cancelAuto() {
        if (autoTask != null) {
            autoTask.cancel();
            autoTask = null;
        }
    }
}
