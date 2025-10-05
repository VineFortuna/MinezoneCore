package anthony.SuperCraftBrawl.npcs;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps a global map of spawned NPCs (by entityId) and routes right-clicks. */
public final class NPCRegistry {
    private static final Map<Integer, NPC> BY_ID = new ConcurrentHashMap<>();
    private NPCRegistry() {}

    public static void register(NPC npc) {
        BY_ID.put(npc.getEntityId(), npc);
    }

    public static void unregister(NPC npc) {
        BY_ID.remove(npc.getEntityId());
        npc.hideFromAll();
    }

    /** Called by ChannelInjector when it sees PacketPlayInUseEntity from a player. */
    public static void handleUseEntity(int entityId, boolean rightClick, Player player) {
        NPC npc = BY_ID.get(entityId);
        if (npc == null || !rightClick) return;

        // Ensure we run on the main thread
        Bukkit.getScheduler().runTask(Core.inst(), () -> npc.handleRightClick(player));
    }
}