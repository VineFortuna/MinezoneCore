package anthony.SuperCraftBrawl;

import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent; // 1.8
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

public class MysteryChestAnimation extends BukkitRunnable {

    private static final String META_KEY = "mystery_loot";
    private static boolean PICKUP_LISTENER_REGISTERED = false;

    private final Core main;
    private final Player player;
    private final UUID playerId;
    private final PlayerData data;
    private final Location base;

    private ArmorStand display; // floating spinning chest (helmet)
    private ArmorStand holo;    // rolling/countdown text
    private int ticks = 0;
    private double angle = 0.0;
    private final Random random = new Random();

    // timings (in ticks)
    private static final int WARMUP = 40;  // gentle spin
    private static final int BUILD  = 60;  // speed up
    private static final int SLOWMO = 20;  // slow down
    private static final int REVEAL = 1;
    private static final int TOTAL  = WARMUP + BUILD + SLOWMO + REVEAL + 10;

    // Per-session guard (unregistered on cleanup)
    private SessionGuard guard;

    public MysteryChestAnimation(Core main, Player player, PlayerData data, Location displayAboveBlock) {
        this.main = main;
        this.player = player;
        this.playerId = player.getUniqueId();
        this.data = data;
        this.base = displayAboveBlock.clone().add(0, 0.6, 0); // float a bit higher
    }

    public void start() {
        // register global pickup blocker once
        if (!PICKUP_LISTENER_REGISTERED) {
            Bukkit.getPluginManager().registerEvents(new PickupBlocker(), main);
            PICKUP_LISTENER_REGISTERED = true;
        }

        // register per-session guard
        guard = new SessionGuard();
        Bukkit.getPluginManager().registerEvents(guard, main);

        // spinning chest via armor stand helmet
        display = base.getWorld().spawn(base, ArmorStand.class);
        display.setVisible(false);
        display.setGravity(false);
        display.setSmall(true);
        try { display.setMarker(true); } catch (Throwable ignored) {}
        display.setHelmet(new ItemStack(Material.CHEST));
        display.setBasePlate(false);

        // hologram line
        holo = base.getWorld().spawn(base.clone().add(0, 0.6, 0), ArmorStand.class);
        holo.setVisible(false);
        holo.setGravity(false);
        holo.setSmall(true);
        try { holo.setMarker(true); } catch (Throwable ignored) {}
        holo.setCustomNameVisible(true);
        holo.setCustomName(color("&d&lRolling..."));

        // open sfx + initial particles
        base.getWorld().playSound(base, Sound.CHEST_OPEN, 0.9f, 1.0f);
        spiralParticles(18);

        runTaskTimer(main, 0L, 1L); // run sync
    }

    @Override
    public void run() {
        // Safety: if player left or swapped worlds, stop immediately
        if (player == null || !player.isOnline()
                || player.getWorld() != base.getWorld()
                || display == null || display.isDead()) {
            cleanup(false);
            return;
        }

        ticks++;

        // compute spin speed based on phase
        double speed;
        if (ticks <= WARMUP) {
            speed = 0.08 + 0.02 * (ticks / (double) WARMUP);
            pitchPling(ticks, 24);
        } else if (ticks <= WARMUP + BUILD) {
            int t = ticks - WARMUP;
            speed = 0.10 + 0.18 * (t / (double) BUILD);
            if (t % 2 == 0) pitchPling(ticks, 36);
            if (t % 8 == 0) base.getWorld().playSound(base, Sound.CLICK, 0.7f, 1.0f);
        } else if (ticks <= WARMUP + BUILD + SLOWMO) {
            int t = ticks - (WARMUP + BUILD);
            speed = 0.28 - 0.20 * (t / (double) SLOWMO);
            if (t % 7 == 0) base.getWorld().playSound(base, Sound.NOTE_BASS, 0.7f, 0.8f);
        } else {
            speed = 0.05;
        }

        angle += speed;
        display.setHeadPose(new EulerAngle(0, angle, 0));

        // smooth bobbing
        double bob = Math.sin(ticks / 8.0) * 0.04;
        display.teleport(base.clone().add(0, bob, 0));

        // ambient spiral particles
        if (ticks % 2 == 0) spiralParticles(8);

        // hologram updates / countdown
        if (ticks == WARMUP) holo.setCustomName(color("&d&lRolling.&7."));
        if (ticks == WARMUP + BUILD / 2) holo.setCustomName(color("&d&lRolling..&7."));
        if (ticks == WARMUP + BUILD) holo.setCustomName(color("&d&lRolling..."));
        if (ticks == WARMUP + BUILD + SLOWMO - 6) {
            holo.setCustomName(color("&e&l3"));
            base.getWorld().playSound(base, Sound.NOTE_PIANO, 1f, 0.8f);
        }
        if (ticks == WARMUP + BUILD + SLOWMO - 4) {
            holo.setCustomName(color("&e&l2"));
            base.getWorld().playSound(base, Sound.NOTE_PIANO, 1f, 0.9f);
        }
        if (ticks == WARMUP + BUILD + SLOWMO - 2) {
            holo.setCustomName(color("&e&l1"));
            base.getWorld().playSound(base, Sound.NOTE_PIANO, 1f, 1.0f);
        }

        if (ticks >= TOTAL) {
            revealReward();
            cleanup(true);
        }
    }

    private void revealReward() {
        // thump + flash
        base.getWorld().playSound(base, Sound.ANVIL_LAND, 0.6f, 0.7f);
        base.getWorld().playSound(base, Sound.LEVEL_UP, 0.8f, 1.0f);
        spiralParticles(22);

        // fireworks
        Firework fw = (Firework) base.getWorld().spawnEntity(base, EntityType.FIREWORK);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BURST)
                .withFlicker()
                .withColor(Color.FUCHSIA, Color.PURPLE, Color.WHITE)
                .build());
        fw.setFireworkMeta(meta);

        // reward logic (preserves your chances/messages)
        int chance = random.nextInt(100);
        ItemStack icon;
        if (chance <= 5) {
            if (data.astronaut == 0) {
                data.astronaut = 1;
                player.sendMessage(main.color("&9&l(!) &rYou unlocked &eAstronaut Outfit!"));
                player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&eAstronaut Outfit"));
            } else {
                data.tokens += 25;
                player.sendMessage(main.color("&9&l(!) &rYou received &e25 Tokens &rfor a duplicate item"));
                player.sendTitle(main.color("&c&lDUPLICATE"), main.color("&eAstronaut Outfit"));
            }
            icon = new ItemStack(Material.IRON_HELMET);
            lootBurst(icon, 8);
            repeatColorFireworks(Color.LIME, 5);
        } else if (chance <= 20) {
            if (data.santaoutfit == 0) {
                data.santaoutfit = 1;
                player.sendMessage(main.color("&9&l(!) &rYou unlocked &c&lSanta Outfit!"));
                player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&c&lSanta Outfit"));
            } else {
                data.tokens += 50;
                player.sendMessage(main.color("&9&l(!) &rYou received &e50 Tokens &rfor a duplicate item"));
                player.sendTitle(main.color("&c&lDUPLICATE"), main.color("&c&lSanta Outfit"));
            }
            icon = new ItemStack(Material.REDSTONE_BLOCK);
            lootBurst(icon, 8);
            singleColorFirework(Color.RED);
        } else if (chance <= 40) {
            data.melon += 14;
            player.sendMessage(main.color("&9&l(!) &rYou unlocked &e14 Melons!"));
            player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e14 Melons"));
            icon = new ItemStack(Material.MELON, 14);
            lootBurst(icon, 10);
            singleColorFirework(Color.LIME);
        } else if (chance <= 60) {
            data.melon += 20;
            player.sendMessage(main.color("&9&l(!) &rYou unlocked &e20 Melons!"));
            player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e20 Melons"));
            icon = new ItemStack(Material.MELON, 20);
            lootBurst(icon, 10);
            singleColorFirework(Color.ORANGE);
        } else if (chance <= 80) {
            data.paintball += 23;
            player.sendMessage(main.color("&9&l(!) &rYou unlocked &e23 Paintballs!"));
            player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e23 Paintballs"));
            icon = new ItemStack(Material.SNOW_BALL, 23);
            lootBurst(icon, 10);
            singleColorFirework(Color.BLUE);
        } else {
            data.paintball += 17;
            player.sendMessage(main.color("&9&l(!) &rYou unlocked &e17 Paintballs!"));
            player.sendTitle(main.color("&e&lUNLOCKED"), main.color("&e17 Paintballs"));
            icon = new ItemStack(Material.SNOW_BALL, 17);
            lootBurst(icon, 10);
            singleColorFirework(Color.BLUE);
        }

        // persist + update UI
        main.getDataManager().saveData(data);
        main.getScoreboardManager().lobbyBoard(player);
    }

    private void cleanup(boolean finished) {
        cancel();
        if (display != null && !display.isDead()) display.remove();
        if (holo != null && !holo.isDead()) holo.remove();
        if (guard != null) {
            HandlerList.unregisterAll(guard);
            guard = null;
        }
        if (main.getGameManager() != null) main.getGameManager().chestCanOpen = false;
    }

    // ===== visuals / audio helpers =====

    private void pitchPling(int t, int period) {
        if (t % period == 0) {
            float pitch = 0.8f + Math.min(1.2f, t / 120f);
            base.getWorld().playSound(base, Sound.NOTE_PLING, 0.9f, pitch);
        }
    }

    private void spiralParticles(int points) {
        World w = base.getWorld();
        double radius = 0.6;
        for (int i = 0; i < points; i++) {
            double theta = (i / (double) points) * 2 * Math.PI + ticks * 0.08;
            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;
            double y = 0.2 + (i / (double) points) * 0.6;
            try {
                w.spigot().playEffect(
                        base.clone().add(x, y, z),
                        Effect.PORTAL, 0, 0,
                        0, 0, 0, 0,
                        1, 16
                );
            } catch (Throwable ignored) {
                w.playEffect(base.clone().add(x, y, z), Effect.PORTAL, 0);
            }
        }
    }

    private void singleColorFirework(Color c) {
        Firework fw = (Firework) base.getWorld().spawnEntity(base, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.setPower(1);
        fwm.addEffect(FireworkEffect.builder().withColor(c).withFlicker().build());
        fw.setFireworkMeta(fwm);
    }

    private void repeatColorFireworks(final Color c, int count) {
        new BukkitRunnable() {
            int i = 0;
            @Override public void run() {
                if (i++ >= count) { cancel(); return; }
                singleColorFirework(c);
            }
        }.runTaskTimer(main, 0L, 12L);
    }

    private void lootBurst(ItemStack stack, int pieces) {
        World w = base.getWorld();
        for (int i = 0; i < pieces; i++) {
            Item item = w.dropItem(base.clone().add(0, 0.2, 0), stack);

            // make unpickable
            item.setPickupDelay(Integer.MAX_VALUE / 4);
            item.setMetadata(META_KEY, new FixedMetadataValue(main, true));

            // toss physics
            Vector v = new Vector(
                    (random.nextDouble() - 0.5) * 0.6,
                    0.3 + random.nextDouble() * 0.25,
                    (random.nextDouble() - 0.5) * 0.6
            );
            item.setVelocity(v);

            // auto-remove
            new BukkitRunnable() {
                @Override public void run() {
                    if (!item.isDead()) item.remove();
                }
            }.runTaskLater(main, 60L);
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    // ===== global pickup blocker for mystery_loot items =====
    private static class PickupBlocker implements Listener {
        @EventHandler
        public void onPickup(PlayerPickupItemEvent e) {
            if (e.getItem() != null && e.getItem().hasMetadata(META_KEY)) {
                e.setCancelled(true);
            }
        }
    }

    // ===== per-session guard: stops animation/hologram if player/world changes =====
    private class SessionGuard implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            if (e.getPlayer().getUniqueId().equals(playerId)) cleanup(false);
        }

        @EventHandler
        public void onWorldChange(PlayerChangedWorldEvent e) {
            if (e.getPlayer().getUniqueId().equals(playerId)) cleanup(false);
        }

        @EventHandler
        public void onTeleport(PlayerTeleportEvent e) {
            if (e.getPlayer().getUniqueId().equals(playerId)) {
                // If they teleport to a different world, stop immediately
                if (e.getTo() != null && e.getFrom() != null && e.getTo().getWorld() != e.getFrom().getWorld()) {
                    cleanup(false);
                }
            }
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent e) {
            // If our display is in this chunk, stop and remove to prevent stranding
            if (display != null && display.isValid() && display.getLocation().getChunk().equals(e.getChunk())) {
                cleanup(false);
            }
            if (holo != null && holo.isValid() && holo.getLocation().getChunk().equals(e.getChunk())) {
                cleanup(false);
            }
        }
    }
}