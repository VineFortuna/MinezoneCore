package anthony.SuperCraftBrawl.floatingblock;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.function.Consumer;

public final class FloatingBlocks implements Listener {

    private static final String META_KEY = "scb-floating-id";
    private static final String NAME_TAG = "§0FB:"; // hidden prefix; name stays invisible

    public static final class Entry {
        final UUID id;
        final Location baseLoc;
        final ItemStack blockItem;
        final String title, subtitle;
        final Consumer<org.bukkit.entity.Player> onClick;

        ArmorStand blockStand; // spinning/clickable block
        ArmorStand titleStand; // text line 1 (marker)
        ArmorStand subStand;   // text line 2 (marker)
        double deg = 0;

        Entry(Location loc, ItemStack item, String title, String subtitle,
              Consumer<org.bukkit.entity.Player> onClick) {
            this.id = UUID.randomUUID();
            this.baseLoc = loc.clone();
            this.blockItem = item;
            this.title = title;
            this.subtitle = subtitle;
            this.onClick = onClick;
        }

        void despawn() {
            remove(blockStand);
            remove(titleStand);
            remove(subStand);
            blockStand = titleStand = subStand = null;
        }

        private void remove(Entity e) {
            if (e != null && !e.isDead()) e.remove();
        }
    }

    private final JavaPlugin plugin;
    private final Map<UUID, Entry> byClickable = new HashMap<>(); // blockStand UUID -> entry
    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Entry> entryByKey = new HashMap<>(); // dedup key -> entry
    private BukkitRunnable tickTask;

    private static final double TITLE_OFFSET = +2.55;
    private static final double SUB_OFFSET   = +2.30;

    public FloatingBlocks(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Build a stable key for de-duplication (world + coords + title text)
    private String keyFor(Location loc, String title) {
        World w = loc.getWorld();
        if (w == null) throw new IllegalArgumentException("World cannot be null");
        return w.getUID() + ":" +
                String.format(java.util.Locale.US, "%.3f:%.3f:%.3f", loc.getX(), loc.getY(), loc.getZ()) +
                ":" + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', title))
                .toLowerCase(java.util.Locale.US);
    }

    /** Add a new floating block definition (safe to call multiple times; duplicates ignored). */
    public Entry add(Location loc, ItemStack blockItem, String title, String subtitle,
                     Consumer<org.bukkit.entity.Player> onClick) {
        String key = keyFor(loc, title);
        Entry existing = entryByKey.get(key);
        if (existing != null) return existing;

        Entry e = new Entry(loc, blockItem, title, subtitle, onClick);
        entries.add(e);
        entryByKey.put(key, e);
        return e;
    }

    /** Spawns all added entries and starts ticking. Safe to call multiple times. */
    public void spawnAll() {
        removeAll(); // clear any previous entities/task

        for (Entry e : entries) spawnEntry(e);

        // Spin + self-heal task
        tickTask = new BukkitRunnable() {
            @Override public void run() {
                for (Entry e : entries) {
                    if (needsRespawn(e)) {
                        // purge any leftovers at the location for this entry
                        cleanupLeftovers(e);
                        // remove old clickable map entry
                        if (e.blockStand != null) byClickable.remove(e.blockStand.getUniqueId());
                        // respawn
                        spawnEntry(e);
                    }
                    if (e.blockStand == null || e.blockStand.isDead()) continue;
                    e.deg = (e.deg + 6) % 360; // speed
                    e.blockStand.setHeadPose(new EulerAngle(0, Math.toRadians(e.deg), 0));
                }
            }
        };
        tickTask.runTaskTimer(plugin, 1L, 1L);
    }

    private boolean needsRespawn(Entry e) {
        // If any of the three stands is missing/invalid or in a different world, we refresh the whole trio.
        if (e.blockStand == null || e.titleStand == null || e.subStand == null) return true;
        if (!sameWorld(e.baseLoc, e.blockStand) || !sameWorld(e.baseLoc, e.titleStand) || !sameWorld(e.baseLoc, e.subStand)) return true;
        if (e.blockStand.isDead() || !e.blockStand.isValid()) return true;
        if (e.titleStand.isDead() || !e.titleStand.isValid()) return true;
        if (e.subStand.isDead() || !e.subStand.isValid()) return true;
        return false;
    }

    private boolean sameWorld(Location base, Entity ent) {
        World bw = base.getWorld();
        World ew = ent.getWorld();
        return bw != null && ew != null && bw.equals(ew);
    }

    private void spawnEntry(Entry e) {
        World w = e.baseLoc.getWorld();
        if (w == null) return;

        // ensure chunk is loaded
        try {
            int cx = e.baseLoc.getBlockX() >> 4, cz = e.baseLoc.getBlockZ() >> 4;
            w.getChunkAt(cx, cz).load(true);
        } catch (Throwable ignored) {}

        // safety: nuke any old tagged stands first
        cleanupLeftovers(e);

        // Clickable/spinning block (NOT a marker so it has a hitbox)
        ArmorStand s = (ArmorStand) w.spawnEntity(e.baseLoc, EntityType.ARMOR_STAND);
        s.setVisible(false);
        s.setGravity(false);
        s.setBasePlate(false);
        s.setArms(false);
        s.setSmall(false);
        s.setRemoveWhenFarAway(false);
        s.setHelmet(e.blockItem);
        // tag it so we can always find/clean it later
        s.setCustomName(NAME_TAG + e.id.toString() + ":block");
        s.setCustomNameVisible(false);
        try { s.setMetadata(META_KEY, new FixedMetadataValue(plugin, e.id.toString())); } catch (Throwable ignored) {}
        e.blockStand = s;
        byClickable.put(s.getUniqueId(), e);

        // Title hologram (marker; no hitbox)
        ArmorStand title = (ArmorStand) w.spawnEntity(e.baseLoc.clone().add(0, TITLE_OFFSET, 0), EntityType.ARMOR_STAND);
        title.setVisible(false);
        title.setGravity(false);
        title.setSmall(true);
        title.setBasePlate(false);
        title.setRemoveWhenFarAway(false);
        try { title.setMarker(true); } catch (Throwable ignored) {}
        title.setCustomName(ChatColor.translateAlternateColorCodes('&', e.title));
        title.setCustomNameVisible(true);
        // tag
        try { title.setMetadata(META_KEY, new FixedMetadataValue(plugin, e.id.toString())); } catch (Throwable ignored) {}
        e.titleStand = title;

        // Subtitle hologram (marker; no hitbox)
        ArmorStand sub = (ArmorStand) w.spawnEntity(e.baseLoc.clone().add(0, SUB_OFFSET, 0), EntityType.ARMOR_STAND);
        sub.setVisible(false);
        sub.setGravity(false);
        sub.setSmall(true);
        sub.setBasePlate(false);
        sub.setRemoveWhenFarAway(false);
        try { sub.setMarker(true); } catch (Throwable ignored) {}
        sub.setCustomName(ChatColor.translateAlternateColorCodes('&', e.subtitle));
        sub.setCustomNameVisible(true);
        // tag
        try { sub.setMetadata(META_KEY, new FixedMetadataValue(plugin, e.id.toString())); } catch (Throwable ignored) {}
        e.subStand = sub;
    }

    /**
     * Remove any ArmorStands around the base location that are tagged for this entry.
     * This prevents duplicates after chunk reloads/world switches.
     */
    private void cleanupLeftovers(Entry e) {
        World w = e.baseLoc.getWorld();
        if (w == null) return;

        // small radius to catch previous spawns (including slightly offset titles)
        final double radiusSq = 1.2 * 1.2;
        for (ArmorStand as : w.getEntitiesByClass(ArmorStand.class)) {
            if (as.isDead()) continue;
            Location l = as.getLocation();
            if (!l.getWorld().equals(w)) continue;
            if (l.distanceSquared(e.baseLoc) > radiusSq &&
                    l.distanceSquared(e.baseLoc.clone().add(0, TITLE_OFFSET, 0)) > radiusSq &&
                    l.distanceSquared(e.baseLoc.clone().add(0, SUB_OFFSET, 0)) > radiusSq) {
                continue;
            }

            boolean match = false;

            // check metadata first (fast if present)
            try {
                if (as.hasMetadata(META_KEY)) {
                    for (org.bukkit.metadata.MetadataValue mv : as.getMetadata(META_KEY)) {
                        if (mv != null && e.id.toString().equalsIgnoreCase(String.valueOf(mv.value()))) {
                            match = true;
                            break;
                        }
                    }
                }
            } catch (Throwable ignored) {}

            // fallback: hidden name prefix we assign
            if (!match) {
                String n = as.getCustomName();
                if (n != null && n.startsWith(NAME_TAG)) {
                    // if it's *any* FB stand at this spot, just clear it to be safe
                    match = true;
                }
            }

            if (match) {
                // also keep our map clean if this was the clickable one
                byClickable.remove(as.getUniqueId());
                try { as.remove(); } catch (Throwable ignored) {}
            }
        }
    }

    public void removeAll() {
        if (tickTask != null) { tickTask.cancel(); tickTask = null; }
        for (Entry e : entries) e.despawn();
        byClickable.clear();
        // extra sweep: remove any tagged leftovers from a previous run/reload
        for (World w : plugin.getServer().getWorlds()) {
            for (ArmorStand as : w.getEntitiesByClass(ArmorStand.class)) {
                String n = as.getCustomName();
                if (n != null && n.startsWith(NAME_TAG)) {
                    try { as.remove(); } catch (Throwable ignored) {}
                } else if (as.hasMetadata(META_KEY)) {
                    try { as.remove(); } catch (Throwable ignored) {}
                }
            }
        }
    }

    // ===== Click handling (right-click and punch) =====

    @EventHandler
    public void onRightClickAt(PlayerInteractAtEntityEvent evt) {
        handleClick(evt.getRightClicked(), evt.getPlayer(), evt);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent evt) {
        handleClick(evt.getRightClicked(), evt.getPlayer(), evt);
    }

    @EventHandler
    public void onHitStand(EntityDamageByEntityEvent evt) {
        if (!(evt.getEntity() instanceof ArmorStand)) return;
        if (!(evt.getDamager() instanceof org.bukkit.entity.Player)) return;

        Entry e = byClickable.get(evt.getEntity().getUniqueId());
        if (e == null) return;

        evt.setCancelled(true); // don’t damage/move it
        try { if (e.onClick != null) e.onClick.accept((org.bukkit.entity.Player) evt.getDamager()); }
        catch (Throwable t) { plugin.getLogger().warning("[FloatingBlocks] onClick threw: " + t.getMessage()); }
    }

    private void handleClick(Entity clicked, org.bukkit.entity.Player p, Cancellable evt) {
        if (clicked == null) return;
        Entry e = byClickable.get(clicked.getUniqueId());
        if (e == null) return;

        evt.setCancelled(true);
        try { if (e.onClick != null) e.onClick.accept(p); }
        catch (Throwable t) { plugin.getLogger().warning("[FloatingBlocks] onClick threw: " + t.getMessage()); }
    }
}