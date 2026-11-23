package anthony.SuperCraftBrawl.mysterychest;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.MysteryChestsGUI;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MysteryChestManager implements Listener {

    // The two hologram lines above the chest
    private static final double HOLOGRAM_X = 198.5, HOLOGRAM_Z = 652.5;
    private static final double TITLE_Y = 105.5, COUNT_Y = 105.2;

    // The title flashes purple 3 times (pink, purple, pink, purple, pink, purple, pink), a third of a second per
    // step, then holds pink for 2 seconds and repeats. A third of a second isn't a whole number of ticks, so the
    // steps last 7, 7 and 6 ticks in turn, which averages out to exactly 333 ms.
    private static final String TITLE_PINK = "&d&lMYSTERY CHEST";
    private static final String TITLE_PURPLE = "&5&lMYSTERY CHEST";
    private static final int TITLE_STEPS_PER_SECOND = 3;
    private static final int TITLE_BLINK_STEPS = 7;
    private static final int TITLE_PAUSE_STEPS = 5; // plus the last pink blink step: 6 steps, 2 seconds
    private static final int TITLE_CYCLE_TICKS = (TITLE_BLINK_STEPS + TITLE_PAUSE_STEPS) * 20 / TITLE_STEPS_PER_SECOND;
    private static final double TITLE_VIEW_DISTANCE_SQUARED = 64 * 64;

    // The portal swirl that always plays around the chest, same numbers as:
    // /particle portal 198.5 106 652.5 0.4 0.8 0.4 0.1 10
    private static final float PORTAL_X = 198.5f, PORTAL_Y = 106f, PORTAL_Z = 652.5f;
    private static final long PORTAL_PERIOD_TICKS = 5L;
    private static final double PORTAL_VIEW_DISTANCE_SQUARED = 40 * 40;

    private Core main;
    public boolean chestCanOpen = false;
    private final PacketPlayOutWorldParticles portalPacket;
    private BukkitTask portalTask;

    // The title is one packet-only stand shared by every player (same entity id on every client), so blinking it
    // is a single prebuilt metadata packet per step and nothing is kept per player. Built the first time it's needed.
    private PacketPlayOutSpawnEntityLiving titleSpawnPacket;
    private PacketPlayOutEntityMetadata titlePinkPacket, titlePurplePacket;
    private BukkitTask titleTask;

    public MysteryChestManager(Core main) {
        this.main = main;
        this.portalPacket = new PacketPlayOutWorldParticles(EnumParticle.PORTAL, false,
                PORTAL_X, PORTAL_Y, PORTAL_Z, 0.4f, 0.8f, 0.4f, 0.1f, 10);
        this.main.getServer().getPluginManager().registerEvents(this, main);
        startPortalParticles();
        startTitleBlink();
    }

    /** The second hologram line: how many chests the player can open, or a nudge to craft one. */
    public String countLine(PlayerData data) {
        if (data.mysteryChests > 0)
            return main.color("&e" + data.mysteryChests + " to open!");
        return main.color("&fOpen to craft!");
    }

    /** Shows the whole hologram (blinking title and the player's own count line) to a player in the lobby. */
    public void spawnHologram(Player player) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        if (data == null) return;

        World lobby = main.getLobbyWorld();
        if (lobby == null) return;

        buildTitle(lobby);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(titleSpawnPacket);
        spawnCountStand(player, data, lobby);
    }

    /** Redraws the count line after the player's chests changed. */
    public void removeAndAddMysteryStand(Player player) {
        PlayerData data = main.getDataManager().getPlayerData(player);
        if (data == null) return;

        World lobby = main.getLobbyWorld();
        if (lobby == null) return;

        if (player.getWorld() != lobby) return;

        EntityArmorStand oldStand = main.msHologram.get(player);

        if (oldStand != null) {
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(oldStand.getId());
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroyPacket);
        }

        spawnCountStand(player, data, lobby);
    }

    private void spawnCountStand(Player player, PlayerData data, World lobby) {
        EntityArmorStand stand = newStand(((CraftWorld) lobby).getHandle(), COUNT_Y, countLine(data));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(stand));
        main.msHologram.put(player, stand);
    }

    private EntityArmorStand newStand(WorldServer world, double y, String name) {
        EntityArmorStand stand = new EntityArmorStand(world);

        stand.setLocation(HOLOGRAM_X, y, HOLOGRAM_Z, 0, 0);
        stand.setCustomName(name);
        stand.setCustomNameVisible(true);
        stand.setGravity(false);
        stand.setInvisible(true);
        return stand;
    }

    /**
     * Builds the shared title once. The spawn packet reads its stand live and the metadata packets read theirs when
     * they're sent, so each packet gets a stand of its own that is never changed afterwards.
     */
    private void buildTitle(World lobby) {
        if (titleSpawnPacket != null) return;

        WorldServer world = ((CraftWorld) lobby).getHandle();
        EntityArmorStand shown = newStand(world, TITLE_Y, main.color(TITLE_PINK));
        EntityArmorStand pink = newStand(world, TITLE_Y, main.color(TITLE_PINK));
        EntityArmorStand purple = newStand(world, TITLE_Y, main.color(TITLE_PURPLE));

        titlePinkPacket = new PacketPlayOutEntityMetadata(shown.getId(), pink.getDataWatcher(), true);
        titlePurplePacket = new PacketPlayOutEntityMetadata(shown.getId(), purple.getDataWatcher(), true);
        titleSpawnPacket = new PacketPlayOutSpawnEntityLiving(shown);
    }

    private void startTitleBlink() {
        titleTask = new BukkitRunnable() {
            private int tick = 0;
            private boolean purple = false;

            @Override
            public void run() {
                if (titleSpawnPacket == null) return; // nobody has been shown it yet

                // Steps 0 to 6 are pink, purple, pink, purple, pink, purple, pink, the rest is the pause (pink)
                int step = tick * TITLE_STEPS_PER_SECOND / 20;
                tick = (tick + 1) % TITLE_CYCLE_TICKS;
                boolean wantPurple = step < TITLE_BLINK_STEPS && step % 2 == 1;

                if (wantPurple == purple) return;
                purple = wantPurple;
                sendToLobbyPlayersNear(purple ? titlePurplePacket : titlePinkPacket,
                        HOLOGRAM_X, TITLE_Y, HOLOGRAM_Z, TITLE_VIEW_DISTANCE_SQUARED);
            }
        }.runTaskTimer(main, 20L, 1L);
    }

    /** Replays the portal particles around the chest for everyone in the lobby close enough to see them. */
    private void startPortalParticles() {
        portalTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendToLobbyPlayersNear(portalPacket, PORTAL_X, PORTAL_Y, PORTAL_Z, PORTAL_VIEW_DISTANCE_SQUARED);
            }
        }.runTaskTimer(main, 20L, PORTAL_PERIOD_TICKS);
    }

    private void sendToLobbyPlayersNear(Packet<?> packet, double x, double y, double z, double maxDistanceSquared) {
        World lobby = main.getLobbyWorld();
        if (lobby == null) return;

        for (Player player : lobby.getPlayers()) {
            Location at = player.getLocation();
            double dx = at.getX() - x;
            double dy = at.getY() - y;
            double dz = at.getZ() - z;

            if (dx * dx + dy * dy + dz * dz <= maxDistanceSquared)
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void shutdown() {
        if (portalTask != null) {
            portalTask.cancel();
            portalTask = null;
        }
        if (titleTask != null) {
            titleTask.cancel();
            titleTask = null;
        }
    }

    @EventHandler
    public void mysteryChest(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.CHEST) return;

        if (player.getWorld() == main.getLobbyWorld()) {
            e.setCancelled(true);

            if (!chestCanOpen) {
                new MysteryChestsGUI(main, e.getClickedBlock().getLocation()).inv.open(player);
            } else {
                player.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "(!) " + ChatColor.RESET
                        + "This MysteryChest is already in use!");
            }
        }
    }
}
