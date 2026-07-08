package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The equip engine: keeps track of which Cosmetic is equipped per category per player,
 * enforces one-per-category mutual exclusion, persists the selection, and drives ticking cosmetics.
 */
public class CosmeticManager implements Listener {

    private final Core main;
    private final CosmeticRegistry registry;
    private final CosmeticDataDAO dao;
    private final Map<UUID, Map<CosmeticCategory, String>> equipped = new ConcurrentHashMap<>();
    private long tickCounter = 0L;

    public CosmeticManager(Core main, CosmeticRegistry registry, CosmeticDataDAO dao) {
        this.main = main;
        this.registry = registry;
        this.dao = dao;
        main.getServer().getPluginManager().registerEvents(this, main);
        startTickTask();
    }

    public CosmeticRegistry registry() {
        return registry;
    }

    /** Equips this cosmetic, unequipping whatever else was equipped in its category first. */
    public boolean equip(Player player, Cosmetic cosmetic) {
        if (cosmetic == null || !cosmetic.isUnlocked(player)) return false;

        UUID uuid = player.getUniqueId();
        Map<CosmeticCategory, String> playerMap = equipped.computeIfAbsent(uuid, k -> new EnumMap<>(CosmeticCategory.class));

        String currentId = playerMap.get(cosmetic.category);
        if (currentId != null) {
            Cosmetic old = registry.get(cosmetic.category, currentId);
            if (old != null) old.onUnequip(player);
        }

        cosmetic.onEquip(player);
        playerMap.put(cosmetic.category, cosmetic.id);
        dao.saveEquipped(uuid, cosmetic.category, cosmetic.id);
        return true;
    }

    /** Equip if not already equipped, unequip if it is. Returns the new equipped state. */
    public boolean toggle(Player player, Cosmetic cosmetic) {
        if (cosmetic == null) return false;
        if (cosmetic.id.equals(getEquippedId(player, cosmetic.category))) {
            unequip(player, cosmetic.category);
            return false;
        }
        return equip(player, cosmetic);
    }

    public void unequip(Player player, CosmeticCategory category) {
        UUID uuid = player.getUniqueId();
        Map<CosmeticCategory, String> playerMap = equipped.get(uuid);
        if (playerMap == null) return;

        String currentId = playerMap.remove(category);
        if (currentId == null) return;

        Cosmetic current = registry.get(category, currentId);
        if (current != null) current.onUnequip(player);

        dao.deleteEquipped(uuid, category);
    }

    public String getEquippedId(Player player, CosmeticCategory category) {
        Map<CosmeticCategory, String> playerMap = equipped.get(player.getUniqueId());
        return playerMap == null ? null : playerMap.get(category);
    }

    public Cosmetic getEquipped(Player player, CosmeticCategory category) {
        String id = getEquippedId(player, category);
        return id == null ? null : registry.get(category, id);
    }

    /** Re-applies every currently-equipped cosmetic's onEquip effect without touching persistence.
     *  Call this whenever something outside the equip system wipes armor/items/inventory (e.g. a
     *  player returning to the lobby from a game), so the equipped cosmetic doesn't just vanish. */
    public void reapply(Player player) {
        Map<CosmeticCategory, String> playerMap = equipped.get(player.getUniqueId());
        if (playerMap == null) return;

        for (Map.Entry<CosmeticCategory, String> entry : playerMap.entrySet()) {
            Cosmetic cosmetic = registry.get(entry.getKey(), entry.getValue());
            if (cosmetic != null && cosmetic.isUnlocked(player)) cosmetic.onEquip(player);
        }
    }

    // Equip gadgets after the hotbar has been populated so they can be placed in the correct slot.
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Map<CosmeticCategory, String> rows = dao.loadEquipped(uuid);
        Map<CosmeticCategory, String> playerMap = equipped.computeIfAbsent(uuid, k -> new EnumMap<>(CosmeticCategory.class));

        for (Map.Entry<CosmeticCategory, String> entry : rows.entrySet()) {
            Cosmetic cosmetic = registry.get(entry.getKey(), entry.getValue());
            if (cosmetic == null || !cosmetic.isUnlocked(player)) {
                continue; // skip invalid or locked equipped cosmetics
            }
            // GADGET lands in a hotbar slot the hit-glitch-fix wooden-sword fills.
            // Skip it here and let LobbyItems() re-apply it after.
            if (entry.getKey() != CosmeticCategory.GADGET) {
                cosmetic.onEquip(player);
            }
            playerMap.put(entry.getKey(), entry.getValue());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Map<CosmeticCategory, String> playerMap = equipped.remove(player.getUniqueId());
        if (playerMap == null) return;

        for (Map.Entry<CosmeticCategory, String> entry : playerMap.entrySet()) {
            Cosmetic cosmetic = registry.get(entry.getKey(), entry.getValue());
            if (cosmetic != null) cosmetic.onUnequip(player);
        }
    }

    /** Runs a repeating task that updates equipped cosmetics requiring periodic ticks. */
    private void startTickTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickCounter++;
                for (Cosmetic cosmetic : registry.getTicking()) {
                    if (tickCounter % cosmetic.getTickPeriod() != 0) continue;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (cosmetic.id.equals(getEquippedId(player, cosmetic.category))) {
                            cosmetic.tick(player);
                        }
                    }
                }
            }
        }.runTaskTimer(main, 1L, 1L);
    }
}