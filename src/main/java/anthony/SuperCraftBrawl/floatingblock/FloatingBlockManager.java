package anthony.SuperCraftBrawl.floatingblock;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public final class FloatingBlockManager {
    private final JavaPlugin plugin;
    private ArmorStand stand;
    private BukkitRunnable spinTask;

    // target
    private static final String WORLD_NAME = "lobby-1";
    private static final double X = 186.5, Y = 116.0, Z = 633.5;

    public FloatingBlockManager(JavaPlugin plugin) { this.plugin = plugin; }

    public void spawn() {
        World world = Bukkit.getWorld(WORLD_NAME);
        if (world == null) {
            plugin.getLogger().warning("[FloatingBlock] World '" + WORLD_NAME + "' is null. Retrying in 40t.");
            Bukkit.getScheduler().runTaskLater(plugin, this::spawn, 40L);
            return;
        }

        // ensure chunk is loaded
        int cx = (int)Math.floor(X) >> 4;
        int cz = (int)Math.floor(Z) >> 4;
        try { world.getChunkAt(cx, cz).load(true); } catch (Throwable ignored) {}

        remove(); // clean old one if any

        Location loc = new Location(world, X, Y, Z);
        stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(false);
        stand.setRemoveWhenFarAway(false);
        try { stand.setMarker(true); } catch (Throwable ignored) {}

        // block to show (white wool). Change durability for color.
        stand.setHelmet(new ItemStack(Material.WOOL, 1, (short) 0));

        // spin
        spinTask = new BukkitRunnable() {
            double deg = 0;
            @Override public void run() {
                if (stand == null || stand.isDead()) return;
                deg = (deg + 6) % 360; // ~1 rotation every ~3s
                stand.setHeadPose(new EulerAngle(0, Math.toRadians(deg), 0));
            }
        };
        spinTask.runTaskTimer(plugin, 1L, 1L);

        plugin.getLogger().info("[FloatingBlock] Spawned at " + X + ", " + Y + ", " + Z + " in " + WORLD_NAME);
    }

    public void remove() {
        if (spinTask != null) { spinTask.cancel(); spinTask = null; }
        if (stand != null && !stand.isDead()) { stand.remove(); }
        stand = null;
    }
}