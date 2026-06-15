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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class FloatingBlocks implements Listener {

    private static final String META_KEY = "scb-floating-id";
    private static final String NAME_TAG = "\u00a70FB:"; // hidden prefix; name stays invisible

    public static final class Entry {
        final UUID id;
        final Location baseLoc;
        final ItemStack blockItem;
        final String title;
        final String subtitle;
        final Function<org.bukkit.entity.Player, String> subtitleProvider;
        final Consumer<org.bukkit.entity.Player> onClick;

        ArmorStand blockStand;
        ArmorStand titleStand;
        ArmorStand subStand;
        double deg = 0;
        double spinSpeed = 6.0;
        boolean rewardAnimationRunning = false;

        /** Static subtitle. */
        Entry(Location loc, ItemStack item, String title, String subtitle,
              Consumer<org.bukkit.entity.Player> onClick) {
            this.id = UUID.randomUUID();
            this.baseLoc = loc.clone();
            this.blockItem = item;
            this.title = title;
            this.subtitle = subtitle;
            this.subtitleProvider = null;
            this.onClick = onClick;
        }

        /** Dynamic per-player subtitle via a Function. */
        Entry(Location loc, ItemStack item, String title,
              Function<org.bukkit.entity.Player, String> subtitleProvider,
              Consumer<org.bukkit.entity.Player> onClick) {
            this.id = UUID.randomUUID();
            this.baseLoc = loc.clone();
            this.blockItem = item;
            this.title = title;

            /*
             * IMPORTANT:
             * Do NOT store "Click to Claim!" as the real armor stand name.
             *
             * /save-all saves real armor stand names into the world.
             * Daily Reward is per-player, so the real subtitle armor stand stays blank.
             * Each player sees their own text through sendSubtitlePacket(...).
             */
            this.subtitle = "";

            this.subtitleProvider = subtitleProvider;
            this.onClick = onClick;
        }

        void despawn() {
            remove(blockStand);
            remove(titleStand);
            remove(subStand);
            blockStand = null;
            titleStand = null;
            subStand = null;
        }

        private void remove(Entity e) {
            if (e != null && !e.isDead()) {
                e.remove();
            }
        }
    }

    private final JavaPlugin plugin;
    private final Map<UUID, Entry> byClickable = new HashMap<>();
    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Entry> entryByKey = new HashMap<>();
    private BukkitRunnable tickTask;

    private static final double TITLE_OFFSET = 2.55;
    private static final double SUB_OFFSET   = 2.30;

    public FloatingBlocks(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private String keyFor(Location loc, String title) {
        World w = loc.getWorld();

        if (w == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        return w.getUID() + ":"
                + String.format(Locale.US, "%.3f:%.3f:%.3f", loc.getX(), loc.getY(), loc.getZ())
                + ":"
                + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', title))
                .toLowerCase(Locale.US);
    }

    /** Add a floating block with a static subtitle. */
    public Entry add(Location loc, ItemStack blockItem, String title, String subtitle,
                     Consumer<org.bukkit.entity.Player> onClick) {
        String key = keyFor(loc, title);
        Entry existing = entryByKey.get(key);

        if (existing != null) {
            return existing;
        }

        Entry e = new Entry(loc, blockItem, title, subtitle, onClick);
        entries.add(e);
        entryByKey.put(key, e);
        return e;
    }

    /** Add a floating block with a dynamic per-player subtitle. */
    public Entry add(Location loc, ItemStack blockItem, String title,
                     Function<org.bukkit.entity.Player, String> subtitleProvider,
                     Consumer<org.bukkit.entity.Player> onClick) {
        String key = keyFor(loc, title);
        Entry existing = entryByKey.get(key);

        if (existing != null) {
            return existing;
        }

        Entry e = new Entry(loc, blockItem, title, subtitleProvider, onClick);
        entries.add(e);
        entryByKey.put(key, e);
        return e;
    }

    /** Spawns all entries and starts the tick/subtitle-refresh task. */
    public void spawnAll() {
        removeAll();

        for (Entry e : entries) {
            spawnEntry(e);
        }

        tickTask = new BukkitRunnable() {
            int subtitleTick = 0;

            @Override
            public void run() {
                for (Entry e : entries) {
                    if (needsRespawn(e)) {
                        cleanupLeftovers(e);

                        if (e.blockStand != null) {
                            byClickable.remove(e.blockStand.getUniqueId());
                        }

                        spawnEntry(e);
                    }

                    if (e.blockStand == null || e.blockStand.isDead()) {
                        continue;
                    }

                    e.deg = (e.deg + e.spinSpeed) % 360;
                    e.blockStand.setHeadPose(new EulerAngle(0, Math.toRadians(e.deg), 0));
                }

                // Refresh per-player subtitles every 10 seconds
                subtitleTick++;

                if (subtitleTick >= 200) {
                    subtitleTick = 0;

                    for (Entry e : entries) {
                        if (e.subtitleProvider == null) {
                            continue;
                        }

                        for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                            sendSubtitlePacket(p, e);
                        }
                    }
                }
            }
        };

        tickTask.runTaskTimer(plugin, 1L, 1L);
    }

    public void playDailyRewardClaimAnimation(final Entry e) {
        if (e == null) return;
        if (e.blockStand == null || e.blockStand.isDead() || !e.blockStand.isValid()) return;
        if (e.rewardAnimationRunning) return;

        final World world = e.baseLoc.getWorld();
        if (world == null) return;

        e.rewardAnimationRunning = true;

        // Keep normal chest spin speed. Do NOT speed it up.
        e.spinSpeed = 6.0;

        final Location center = e.baseLoc.clone().add(0, 1.75, 0);

        world.playSound(center, org.bukkit.Sound.CHEST_OPEN, 1.0f, 1.5f);
        world.playSound(center, org.bukkit.Sound.NOTE_PLING, 1.0f, 2.0f);

        new BukkitRunnable() {
            int ticks = 0;
            boolean exploded = false;

            @Override
            public void run() {
                ticks++;

                if (e.blockStand == null || e.blockStand.isDead() || !e.blockStand.isValid()) {
                    e.spinSpeed = 6.0;
                    e.rewardAnimationRunning = false;
                    this.cancel();
                    return;
                }

                // Small charging effect while waiting, no purple/enderman particles
                if (ticks % 2 == 0) {
                    world.playEffect(center.clone().add(0, 0.4, 0), org.bukkit.Effect.MOBSPAWNER_FLAMES, 0);
                }

                // Burst after a short delay
                if (!exploded && ticks >= 18) {
                    exploded = true;

                    world.playSound(center, org.bukkit.Sound.EXPLODE, 1.0f, 1.35f);
                    world.playSound(center, org.bukkit.Sound.LEVEL_UP, 1.0f, 1.7f);

                    world.playEffect(center, org.bukkit.Effect.EXPLOSION_LARGE, 0);

                    spawnDailyRewardEmeraldBurst(center);
                }

                // Reset/finish
                if (ticks >= 40) {
                    e.spinSpeed = 6.0;
                    e.rewardAnimationRunning = false;
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnDailyRewardEmeraldBurst(Location center) {
        final World world = center.getWorld();
        if (world == null) return;

        Random random = new Random();

        for (int i = 0; i < 14; i++) {
            final org.bukkit.entity.Item emerald = world.dropItem(
                    center.clone().add(0, 0.15, 0),
                    new ItemStack(org.bukkit.Material.EMERALD, 1)
            );

            emerald.setPickupDelay(999999);

            double x = (random.nextDouble() - 0.5) * 0.7;
            double y = 0.25 + random.nextDouble() * 0.35;
            double z = (random.nextDouble() - 0.5) * 0.7;

            emerald.setVelocity(new org.bukkit.util.Vector(x, y, z));

            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    if (!emerald.isDead()) {
                        emerald.remove();
                    }
                }
            }, 40L);
        }
    }

    /**
     * Sends a per-player NMS metadata packet that updates the subtitle armor stand's
     * custom name for just that player.
     */
    public void sendSubtitlePacket(org.bukkit.entity.Player player, Entry e) {
        if (e.subtitleProvider == null) {
            return;
        }

        if (e.subStand == null || e.subStand.isDead()) {
            return;
        }

        String rawText = e.subtitleProvider.apply(player);

        if (rawText == null) {
            rawText = "";
        }

        String text = ChatColor.translateAlternateColorCodes('&', rawText);

        try {
            net.minecraft.server.v1_8_R3.Entity nmsEntity =
                    ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity) e.subStand).getHandle();

            net.minecraft.server.v1_8_R3.DataWatcher fakeWatcher =
                    new net.minecraft.server.v1_8_R3.DataWatcher(null);

            fakeWatcher.a(2, text);      // custom name
            fakeWatcher.a(3, (byte) 1);  // custom name visible

            net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata pkt =
                    new net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata(
                            nmsEntity.getId(),
                            fakeWatcher,
                            true
                    );

            ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player)
                    .getHandle().playerConnection.sendPacket(pkt);
        } catch (Throwable t) {
            plugin.getLogger().warning("[FloatingBlocks] subtitle packet error: " + t.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final org.bukkit.entity.Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Entry e : entries) {
                if (e.subtitleProvider != null) {
                    sendSubtitlePacket(player, e);
                }
            }
        }, 20L);
    }

    private boolean needsRespawn(Entry e) {
        if (e.blockStand == null || e.titleStand == null || e.subStand == null) {
            return true;
        }

        if (!sameWorld(e.baseLoc, e.blockStand)
                || !sameWorld(e.baseLoc, e.titleStand)
                || !sameWorld(e.baseLoc, e.subStand)) {
            return true;
        }

        if (e.blockStand.isDead() || !e.blockStand.isValid()) {
            return true;
        }

        if (e.titleStand.isDead() || !e.titleStand.isValid()) {
            return true;
        }

        return e.subStand.isDead() || !e.subStand.isValid();
    }

    private boolean sameWorld(Location base, Entity ent) {
        World bw = base.getWorld();
        World ew = ent.getWorld();

        return bw != null && ew != null && bw.equals(ew);
    }

    private void spawnEntry(Entry e) {
        World w = e.baseLoc.getWorld();

        if (w == null) {
            return;
        }

        try {
            int cx = e.baseLoc.getBlockX() >> 4;
            int cz = e.baseLoc.getBlockZ() >> 4;
            w.getChunkAt(cx, cz).load(true);
        } catch (Throwable ignored) {}

        cleanupLeftovers(e);

        ArmorStand s = (ArmorStand) w.spawnEntity(e.baseLoc, EntityType.ARMOR_STAND);
        s.setVisible(false);
        s.setGravity(false);
        s.setBasePlate(false);
        s.setArms(false);
        s.setSmall(false);
        s.setRemoveWhenFarAway(false);
        s.setHelmet(e.blockItem);
        s.setCustomName(NAME_TAG + e.id.toString() + ":block");
        s.setCustomNameVisible(false);

        try {
            s.setMetadata(META_KEY, new FixedMetadataValue(plugin, e.id.toString()));
        } catch (Throwable ignored) {}

        e.blockStand = s;
        byClickable.put(s.getUniqueId(), e);

        ArmorStand title = (ArmorStand) w.spawnEntity(e.baseLoc.clone().add(0, TITLE_OFFSET, 0), EntityType.ARMOR_STAND);
        title.setVisible(false);
        title.setGravity(false);
        title.setSmall(true);
        title.setBasePlate(false);
        title.setRemoveWhenFarAway(false);

        try {
            title.setMarker(true);
        } catch (Throwable ignored) {}

        title.setCustomName(ChatColor.translateAlternateColorCodes('&', e.title));
        title.setCustomNameVisible(true);

        try {
            title.setMetadata(META_KEY, new FixedMetadataValue(plugin, e.id.toString()));
        } catch (Throwable ignored) {}

        e.titleStand = title;

        ArmorStand sub = (ArmorStand) w.spawnEntity(e.baseLoc.clone().add(0, SUB_OFFSET, 0), EntityType.ARMOR_STAND);
        sub.setVisible(false);
        sub.setGravity(false);
        sub.setSmall(true);
        sub.setBasePlate(false);
        sub.setRemoveWhenFarAway(false);

        try {
            sub.setMarker(true);
        } catch (Throwable ignored) {}

        /*
         * Static subtitle = real name.
         * Dynamic subtitle = blank real name, player-specific packet name.
         */
        String realSubtitleName = e.subtitleProvider == null ? e.subtitle : "";

        sub.setCustomName(ChatColor.translateAlternateColorCodes('&', realSubtitleName));
        sub.setCustomNameVisible(true);

        try {
            sub.setMetadata(META_KEY, new FixedMetadataValue(plugin, e.id.toString()));
        } catch (Throwable ignored) {}

        e.subStand = sub;

        if (e.subtitleProvider != null) {
            for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> sendSubtitlePacket(p, e), 5L);
            }
        }
    }

    private void cleanupLeftovers(Entry e) {
        World w = e.baseLoc.getWorld();

        if (w == null) {
            return;
        }

        final double radiusSq = 1.75 * 1.75;
        Location titleLoc = e.baseLoc.clone().add(0, TITLE_OFFSET, 0);
        Location subLoc = e.baseLoc.clone().add(0, SUB_OFFSET, 0);

        for (ArmorStand as : new ArrayList<ArmorStand>(w.getEntitiesByClass(ArmorStand.class))) {
            if (as == null || as.isDead()) {
                continue;
            }

            Location l = as.getLocation();

            if (l == null || l.getWorld() == null || !l.getWorld().equals(w)) {
                continue;
            }

            boolean nearThisFloatingBlock =
                    l.distanceSquared(e.baseLoc) <= radiusSq
                            || l.distanceSquared(titleLoc) <= radiusSq
                            || l.distanceSquared(subLoc) <= radiusSq;

            if (!nearThisFloatingBlock) {
                continue;
            }

            if (isFloatingBlockLeftover(as, e)) {
                byClickable.remove(as.getUniqueId());

                try {
                    as.remove();
                } catch (Throwable ignored) {}
            }
        }
    }

    private boolean isFloatingBlockLeftover(ArmorStand as, Entry e) {
        try {
            if (as.hasMetadata(META_KEY)) {
                return true;
            }
        } catch (Throwable ignored) {}

        String name = as.getCustomName();
        String stripped = "";

        if (name != null) {
            stripped = ChatColor.stripColor(name).toLowerCase(Locale.US).trim();
        }

        if (name != null && name.startsWith(NAME_TAG)) {
            return true;
        }

        String titlePlain = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.title))
                .toLowerCase(Locale.US)
                .trim();

        String subtitlePlain = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                        e.subtitle == null ? "" : e.subtitle))
                .toLowerCase(Locale.US)
                .trim();

        if (!titlePlain.isEmpty() && stripped.contains(titlePlain)) {
            return true;
        }

        if (!subtitlePlain.isEmpty() && stripped.contains(subtitlePlain)) {
            return true;
        }

        // Old saved daily reward subtitles from before dynamic subtitle fix
        if (stripped.contains("daily reward")) {
            return true;
        }

        if (stripped.contains("click to claim")) {
            return true;
        }

        if (stripped.contains("next reward")) {
            return true;
        }

        // Other floating block labels
        if (stripped.contains("socials")) {
            return true;
        }

        if (stripped.contains("store")) {
            return true;
        }

        if (stripped.contains("scb classes")) {
            return true;
        }

        if (stripped.contains("right click")) {
            return true;
        }

        /*
         * This catches blank saved dynamic subtitle armor stands after this fix.
         * Only runs inside cleanupLeftovers near the exact floating block location.
         */
        return looksLikeFloatingBlockStand(as);
    }

    private boolean looksLikeFloatingBlockStand(ArmorStand as) {
        try {
            if (as.isVisible()) {
                return false;
            }

            if (as.hasBasePlate()) {
                return false;
            }

            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public void removeAll() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }

        for (Entry e : entries) {
            e.despawn();
        }

        byClickable.clear();

        for (World w : plugin.getServer().getWorlds()) {
            for (ArmorStand as : new ArrayList<ArmorStand>(w.getEntitiesByClass(ArmorStand.class))) {
                if (as == null || as.isDead()) {
                    continue;
                }

                String n = as.getCustomName();

                boolean shouldRemove = false;

                if (n != null && n.startsWith(NAME_TAG)) {
                    shouldRemove = true;
                }

                try {
                    if (as.hasMetadata(META_KEY)) {
                        shouldRemove = true;
                    }
                } catch (Throwable ignored) {}

                if (shouldRemove) {
                    try {
                        as.remove();
                    } catch (Throwable ignored) {}
                }
            }
        }
    }

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
        if (!(evt.getEntity() instanceof ArmorStand)) {
            return;
        }

        if (!(evt.getDamager() instanceof org.bukkit.entity.Player)) {
            return;
        }

        Entry e = byClickable.get(evt.getEntity().getUniqueId());

        if (e == null) {
            return;
        }

        evt.setCancelled(true);

        try {
            if (e.onClick != null) {
                e.onClick.accept((org.bukkit.entity.Player) evt.getDamager());
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[FloatingBlocks] onClick threw: " + t.getMessage());
        }
    }

    private void handleClick(Entity clicked, org.bukkit.entity.Player p, Cancellable evt) {
        if (clicked == null) {
            return;
        }

        Entry e = byClickable.get(clicked.getUniqueId());

        if (e == null) {
            return;
        }

        evt.setCancelled(true);

        try {
            if (e.onClick != null) {
                e.onClick.accept(p);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("[FloatingBlocks] onClick threw: " + t.getMessage());
        }
    }
}