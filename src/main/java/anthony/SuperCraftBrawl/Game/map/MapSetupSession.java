package anthony.SuperCraftBrawl.Game.map;

import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * One player setting up one map with the /scbsetup items. The {@link #draft} is their working copy: nothing in it is
 * kept (or used by games) until /scbsetup save writes it to maps.yml, the same way the world's blocks are only kept by it.
 * Gone when they leave, and holds nothing that outlives that.
 */
final class MapSetupSession {

    final UUID player;
    final MapDraft draft;
    final World world;
    /** The map's world was made by /scbsetup create (and so is thrown away if it is never saved). */
    final boolean created;

    /** The two corners of the boundary rectangle, as the player set them. The bounds in the draft come from these. */
    Vector pos1;
    Vector pos2;

    // The last tool use, to ignore a click the client sends twice
    private SetupTool lastTool;
    private boolean lastLeft;
    private long lastUse;

    MapSetupSession(UUID player, MapDraft draft, World world, boolean created) {
        this.player = player;
        this.draft = draft;
        this.world = world;
        this.created = created;

        // A map that already has bounds starts with its two corners, so just one of them can be changed
        if (draft.hasBounds()) {
            pos1 = new Vector(draft.center.getX() - draft.boundsX, draft.center.getY(), draft.center.getZ() - draft.boundsZ);
            pos2 = new Vector(draft.center.getX() + draft.boundsX, draft.center.getY(), draft.center.getZ() + draft.boundsZ);
        }
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
        if (pos1 == null || pos2 == null) return;
        draft.center = new Vector((pos1.getX() + pos2.getX()) / 2, Math.max(pos1.getY(), pos2.getY()),
                (pos1.getZ() + pos2.getZ()) / 2);
        draft.boundsX = Math.abs(pos1.getX() - pos2.getX()) / 2;
        draft.boundsZ = Math.abs(pos1.getZ() - pos2.getZ()) / 2;
    }
}
