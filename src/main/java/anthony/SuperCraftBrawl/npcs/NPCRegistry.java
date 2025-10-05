package anthony.SuperCraftBrawl.npcs;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NPCRegistry {
    private static final Map<Integer, NPC> BY_ID = new ConcurrentHashMap<>();
    private NPCRegistry(){}

    public static void register(NPC npc) { BY_ID.put(npc.getEntityId(), npc); }
    public static void unregister(NPC npc) { BY_ID.remove(npc.getEntityId()); }

    /** Called by our Netty injector when client sends USE_ENTITY */
    public static void handleUseEntity(int entityId, boolean rightClick, Player player) {
        NPC npc = BY_ID.get(entityId);
        if (npc == null) return;
        if (rightClick) {
            // ensure main thread
            org.bukkit.Bukkit.getScheduler().runTask(NPCPlugin.getInstance(), () -> npc.handleRightClick(player));
        }
    }
}
