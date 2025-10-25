package anthony.parkour;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.ParkourDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import java.util.*;

public class Parkour implements Listener {

    private final Core main;

    // Per-player state
    public final Map<Player, Arenas> players = new HashMap<>();
    private final Map<Player, Integer> checkpoint = new HashMap<>();
    private final Map<Player, FastBoard> boards = new HashMap<>();
    private final Map<Player, BukkitTask> timers = new HashMap<>();
    private final Map<Player, Long> startTimeNs = new HashMap<>();
    private final Map<Player, List<EntityArmorStand>> checkpointHolograms = new HashMap<>();

    public Parkour(Core main) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, main);
    }

    /* =========================
       Public helpers
       ========================= */

    public boolean hasPlayer(Player p) {
        return players.containsKey(p);
    }

    public void addPlayer(Player player, Arenas arena) {
        if (hasPlayer(player)) {
            player.sendMessage(main.color("&c&l(!) &rYou are already in parkour mode!"));
            return;
        }

        main.getScoreboardManager().removeLobbyBoard(player);
        players.put(player, arena);
        player.sendMessage(main.color("&e&l(!) &rYou have joined &r&l" + arena.getName()));

        // Inventory/UI
        player.getInventory().clear();
        giveGameItems(player);
        createOrUpdateBoard(player, /*resetTime*/ true);
        player.setAllowFlight(false);
        player.closeInventory();

        // Remove effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Client-side checkpoint labels for THIS player (packet only)
        addHolograms(player, arena);
    }

    public void removePlayer(Player player) {
        // Holograms (client-side: send destroy packets, then drop references)
        removeHolograms(player);

        // Cancel timer
        BukkitTask t = timers.remove(player);
        if (t != null) {
            try { t.cancel(); } catch (Throwable ignored) {}
        }

        // Scoreboard
        FastBoard fb = boards.remove(player);
        if (fb != null) {
            try { fb.delete(); } catch (Throwable ignored) {}
        }

        // Clear state maps
        startTimeNs.remove(player);
        checkpoint.remove(player);
        players.remove(player);

        // Restore lobby state
        main.getScoreboardManager().lobbyBoard(player);
        player.getInventory().clear();
        main.LobbyItems(player);
        player.setAllowFlight(true);
        player.setFireTicks(0);
    }

    /** Cleanup everything (call onDisable) */
    public void cleanupAll() {
        for (Player p : new ArrayList<>(players.keySet())) {
            // Avoid duplicate lobby UI if server is stopping; just internal cleanup:
            removeHolograms(p);

            BukkitTask t = timers.remove(p);
            if (t != null) {
                try { t.cancel(); } catch (Throwable ignored) {}
            }

            FastBoard fb = boards.remove(p);
            if (fb != null) {
                try { fb.delete(); } catch (Throwable ignored) {}
            }
        }
        checkpointHolograms.clear();
        startTimeNs.clear();
        checkpoint.clear();
        players.clear();
    }

    /* =========================
       Holograms (packet-only)
       ========================= */

    private void addHolograms(Player player, Arenas arena) {
        // Drop any previous list for safety
        removeHolograms(player);

        List<EntityArmorStand> stands = new ArrayList<>();
        checkpointHolograms.put(player, stands);

        WorldServer world = ((CraftWorld) main.getLobbyWorld()).getHandle();
        int i = 1;

        for (Location l : arena.getInstance().checkpoints) {
            Location loc = new Location(main.getLobbyWorld(), l.getX() + 0.5, l.getY() - 0.75, l.getZ() + 0.5);

            EntityArmorStand stand = new EntityArmorStand(world);
            stand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
            stand.setCustomName(main.color("&e&lCheckpoint &b&l#" + i));
            stand.setCustomNameVisible(true);
            stand.setInvisible(true);
            stand.setGravity(false);

            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(stand);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

            stands.add(stand);
            i++;
        }
    }

    private void removeHolograms(Player player) {
        List<EntityArmorStand> stands = checkpointHolograms.remove(player);
        if (stands == null || stands.isEmpty()) return;

        CraftPlayer cp = (CraftPlayer) player;
        for (EntityArmorStand stand : stands) {
            try {
                PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(stand.getId());
                cp.getHandle().playerConnection.sendPacket(destroy);
            } catch (Throwable ignored) {}
        }
        stands.clear();
    }

    /* =========================
       Scoreboard & Timer
       ========================= */

    private void createOrUpdateBoard(Player player, boolean resetTime) {
        // FastBoard: create once per run, delete on removal
        FastBoard fb = boards.get(player);
        if (fb == null) {
            fb = new FastBoard(player);
            boards.put(player, fb);
        }

        fb.updateTitle(main.color("&6&lPARKOUR"));

        int reached = checkpoint.containsKey(player) ? (checkpoint.get(player) + 1) : 0;
        int total = players.get(player).getCheckpoints();
        String arenaName = this.players.get(player).getName();

        fb.updateLines(
                "",
                main.color("&fArena: &a" + arenaName),
                "",
                main.color("&fCheckpoints: &a" + reached + "/" + total),
                "",
                main.color("&fTime:&a 0.0s"),
                "",
                main.color("&eminezone.club")
        );

        if (resetTime) startTimer(player);
    }

    private void startTimer(Player player) {
        // Cancel old timer if any
        BukkitTask old = timers.remove(player);
        if (old != null) {
            try { old.cancel(); } catch (Throwable ignored) {}
        }

        long start = System.nanoTime();
        startTimeNs.put(player, start);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Stop if the player left parkour
                if (!hasPlayer(player)) {
                    cancel();
                    timers.remove(player);
                    return;
                }
                Long startNs = startTimeNs.get(player);
                FastBoard fb = boards.get(player);
                if (startNs == null || fb == null) return;

                long elapsed = System.nanoTime() - startNs;
                String formatted = formatTimeScoreboard(elapsed);
                fb.updateLine(5, main.color("&fTime:&a " + formatted));
            }
        }.runTaskTimer(main, 0L, 2L); // every 0.1s

        timers.put(player, task);
    }

    private void updateCheckpointLine(Player player, int indexNow) {
        FastBoard fb = boards.get(player);
        if (fb == null) return;
        int total = players.get(player).getCheckpoints();
        fb.updateLine(3, main.color("&fCheckpoints: &a" + (indexNow + 1) + "/" + total));
    }

    /* =========================
       Items & Teleport helpers
       ========================= */

    private void giveGameItems(Player player) {
        player.getInventory().setItem(0,
                ItemHelper.setDetails(new ItemStack(Material.BEACON), main.color("&eReturn to Checkpoint")));
        player.getInventory().setItem(1,
                ItemHelper.setDetails(new ItemStack(Material.SEA_LANTERN), main.color("&eRestart")));
        player.getInventory().setItem(2,
                ItemHelper.setDetails(new ItemStack(Material.BARRIER), main.color("&eLeave")));
    }

    private void teleportToStart(Player player) {
        Location loc = players.get(player).getInstance().startLoc;
        player.teleport(loc);
        player.setFireTicks(0);
    }

    private void teleportToCheckpoint(Player player) {
        Integer idx = checkpoint.get(player);
        if (idx != null) {
            Location loc = players.get(player).getCheckpoint(idx);
            player.teleport(loc);
        } else {
            teleportToStart(player);
        }
        player.setFireTicks(0);
    }

    /* =========================
       Events
       ========================= */

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getWorld() != main.getLobbyWorld()) return;
        if (event.getTo() == null || event.getFrom() == null) return;
        if (event.getTo().toVector().toBlockVector().equals(event.getFrom().toVector().toBlockVector())) return;

        if (hasPlayer(player)) {
            ArenaInstance arenaInstance = players.get(player).getInstance();

            // Fell off
            if (event.getTo().getY() < 50) {
                teleportToCheckpoint(player);
                return;
            }

            // Checkpoint on BEACON
            if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BEACON) {
                BlockVector bv = event.getTo().getBlock().getLocation().toVector().toBlockVector();
                if (arenaInstance.checkpointBlocks.contains(bv)) {
                    int newIdx = arenaInstance.checkpointBlocks.indexOf(bv);
                    Integer curIdx = checkpoint.get(player);

                    boolean first = (curIdx == null && newIdx == 0);
                    boolean next = (curIdx != null && newIdx == curIdx + 1);

                    if (first || next) {
                        checkpoint.put(player, newIdx);

                        long now = System.nanoTime();
                        long start = startTimeNs.getOrDefault(player, now);
                        long elapsed = now - start;

                        updateCheckpointLine(player, newIdx);
                        player.sendMessage(main.color("&e&l(!) &rYou reached checkpoint &e#" + (newIdx + 1) + "&r in &a" + formatTime(elapsed)));
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 0.6f);
                    }
                }
                return;
            }

            // Finish on GLOWSTONE
            if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GLOWSTONE) {
                for (Arenas arena : Arenas.values()) {
                    if (event.getTo().toVector().toBlockVector().equals(arena.getInstance().endLoc.toVector().toBlockVector())
                            && !event.getFrom().toVector().toBlockVector().equals(arena.getInstance().endLoc.toVector().toBlockVector())) {

                        if (checkpoint.containsKey(player) && checkpoint.get(player) == arena.getCheckpoints() - 1) {
                            long end = System.nanoTime();
                            long start = startTimeNs.getOrDefault(player, end);
                            long total = end - start;

                            player.sendTitle(main.color("&aPARKOUR COMPLETE!"), null);
                            player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

                            PlayerData data = main.getDataManager().getPlayerData(player);
                            int arenaID = players.get(player).getId();
                            ArenaInstance inst = players.get(player).getInstance();

                            ParkourDetails details = data.playerParkour.get(arenaID);
                            if (details == null) {
                                details = new ParkourDetails();
                                data.playerParkour.put(arenaID, details);
                                player.sendMessage(main.color("&d&l(!) &rYou have earned &e" + inst.tokenReward + " Tokens &rfor clearing this parkour for the first time!"));
                                data.tokens += inst.tokenReward;
                            }

                            if (details.totalTime == 0 || total < details.totalTime) {
                                details.completeParkour(total);
                                main.getDataManager().saveData(data);
                                player.sendMessage(main.color("&e&l(!) &rParkour completed in &a" + formatTime(total) + "&r! &e&lNEW RECORD"));
                            } else {
                                player.sendMessage(main.color("&e&l(!) &rParkour completed in &e" + formatTime(total) + "&r!"));
                                player.sendMessage(main.color("&e&l(!) &rYou did not beat your record of &a" + formatTime(details.totalTime)));
                            }

                            sendTeleportMessage(player, arenaID);
                            removePlayer(player);
                        } else {
                            player.sendMessage(main.color("&c&l(!) &rYou must reach every checkpoint before completing the parkour!"));
                        }
                        return;
                    }
                }
            }

            // Restart on SEA_LANTERN (stand on start plate)
            if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SEA_LANTERN) {
                if (event.getTo().toVector().toBlockVector().equals(players.get(player).getInstance().startLoc.toVector().toBlockVector())) {
                    checkpoint.remove(player);
                    createOrUpdateBoard(player, /*resetTime*/ true);
                    player.sendMessage(main.color("&e&l(!) &rReset your time"));
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 0.6f);
                }
            }
        } else {
            // Not in a run: auto-join when stepping on SEA_LANTERN start pad
            if (event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SEA_LANTERN) {
                for (Arenas arena : Arenas.values()) {
                    if (event.getTo().toVector().toBlockVector().equals(arena.getInstance().startLoc.toVector().toBlockVector())) {
                        addPlayer(player, arena);
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 0.6f);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material item = event.getMaterial();
        if (!hasPlayer(player) || item == null) return;

        switch (item) {
            case BEACON:
                teleportToCheckpoint(player);
                if (checkpoint.containsKey(player))
                    player.sendMessage(main.color("&e&l(!) &rSent back to checkpoint &e#" + (checkpoint.get(player) + 1)));
                else
                    player.sendMessage(main.color("&e&l(!) &rSent back to checkpoint"));
                break;

            case SEA_LANTERN:
                teleportToStart(player);
                checkpoint.remove(player);
                createOrUpdateBoard(player, /*resetTime*/ true);
                player.sendMessage(main.color("&e&l(!) &rSent back to start"));
                break;

            case BARRIER:
                removePlayer(player);
                player.sendMessage(main.color("&r&l(!) &rYou have left parkour mode"));
                break;

            default:
                break;
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        Player player = event.getPlayer();

        if (!msg.startsWith("/_teleportstart ")) return;
        event.setCancelled(true); // prevent command from reaching server

        if (hasPlayer(player)) {
            player.sendMessage(main.color("&c&l(!) &rYou are already in parkour mode!"));
            return;
        }

        String[] parts = msg.split(" ");
        if (parts.length < 2) return;

        try {
            int id = Integer.parseInt(parts[1]);
            Arenas arena = Arenas.getById(id);
            if (arena == null) return;

            Location targetLocation = arena.getInstance().startLoc;
            if (targetLocation == null) return;

            player.teleport(targetLocation);
            addPlayer(player, arena);
        } catch (NumberFormatException ignored) {}
    }

    // Extra safety: auto-clean on quit/kick/world change
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (hasPlayer(p)) removePlayer(p);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        if (hasPlayer(p)) removePlayer(p);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        if (hasPlayer(p)) removePlayer(p);
    }

    /* =========================
       Messaging helpers
       ========================= */

    public String formatTime(long nanoseconds) {
        double totalSeconds = nanoseconds / 1_000_000_000.0;
        long minutes = (long) (totalSeconds / 60);
        double seconds = totalSeconds % 60;
        if (minutes > 0) return String.format("%dm %.3fs", minutes, seconds);
        return String.format("%.3fs", seconds);
    }

    public String formatTimeScoreboard(long nanoseconds) {
        double totalSeconds = nanoseconds / 1_000_000_000.0;
        long minutes = (long) (totalSeconds / 60);
        double seconds = totalSeconds % 60;
        if (minutes > 0) return String.format("%dm %.1fs", minutes, seconds);
        return String.format("%.1fs", seconds);
    }

    public void sendTeleportMessage(Player player, int id) {
        TextComponent message = new TextComponent(main.color("&a&lClick to try again"));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/_teleportstart " + id));
        player.spigot().sendMessage(message);
    }
}