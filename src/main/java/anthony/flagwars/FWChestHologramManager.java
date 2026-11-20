package anthony.flagwars;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spawns a two-line "Punch to" / "Deposit!" text hologram at every location
 * registered via /fwsetup chesthologram <id> when a match starts, and removes
 * them on reset. Uses the same invisible small ArmorStand pattern as
 * FWGeneratorDisplay - one stand per line, stacked, since a single armor
 * stand's custom name can only ever show one line.
 */
public class FWChestHologramManager {

    /** Vertical gap between the two stacked lines. */
    private static final double LINE_HEIGHT_GAP = 0.25;

    private final FWGameInstance instance;
    private final List<ArmorStand> holograms = new ArrayList<>();

    public FWChestHologramManager(FWGameInstance instance) {
        this.instance = instance;
    }

    public void start() {
        Core main = instance.getManager().getMain();
        String topLine = main.color("&ePunch to");
        String bottomLine = main.color("&eDeposit!");

        Map<Integer, Location> configured = instance.getMapConfig().getChestHolograms();
        main.getLogger().info("[FW] Chest holograms: " + configured.size() + " location(s) in config.");

        Bukkit.getScheduler().runTaskLater(main, () -> {
            for (Map.Entry<Integer, Location> entry : configured.entrySet()) {
                Location loc = entry.getValue();
                if (loc == null || loc.getWorld() == null) {
                    main.getLogger().warning("[FW] Hologram #" + entry.getKey() + " skipped — null location or world.");
                    continue;
                }
                main.getLogger().info("[FW] Hologram #" + entry.getKey() + " -> "
                        + loc.getWorld().getName() + " " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
                loc.getChunk().load();
                holograms.add(spawnTextStand(loc.clone().add(0, LINE_HEIGHT_GAP, 0), topLine));
                holograms.add(spawnTextStand(loc, bottomLine));
            }
            main.getLogger().info("[FW] Spawned " + holograms.size() + "/" + (configured.size() * 2) + " chest hologram lines.");
        }, 10L);
    }

    public void stop() {
        for (ArmorStand stand : holograms) {
            if (!stand.isDead()) stand.remove();
        }
        holograms.clear();
    }

    private ArmorStand spawnTextStand(Location loc, String name) {
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(true);
        stand.setRemoveWhenFarAway(false);
        stand.setCustomName(name);
        stand.setCustomNameVisible(true);
        try {
            stand.setMarker(true);
        } catch (Throwable ignored) {}
        return stand;
    }
}
