package anthony.SuperCraftBrawl.practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import net.md_5.bungee.api.ChatColor;

public class BowPractice {

	private Core core;
	public List<Player> players;
	private List<Integer> playerMaps;

	public BowPractice() {
		this.players = new ArrayList<Player>();
		this.playerMaps = new ArrayList<Integer>();
	}

	public void addPlayer(Player player, Core core) {
		if (!(hasPlayer(player))) {
			this.core = core;
			sendToMap(player);
			return;
		}

		player.sendMessage(color("&c&l(!) &rYou are already in a game!"));
	}

	@SuppressWarnings("deprecation")
	private void sendToMap(Player player) {
		Location loc = null;

		for (int i = 0; i < 5000; i += 100) {
			if (!(this.playerMaps.contains(i))) {
				this.playerMaps.add(i);
				this.players.add(player);
				loc = new Location(player.getWorld(), i, 100, 0);
				player.teleport(loc);
				player.sendMessage(color("&2&l(!) &rYou have joined &r&lBow Practice"));
				startBowPractice(player);
				return;
			}
		}

		player.sendMessage(color("&c&l(!) &rLEVEL BUSY! Please try again soon"));
		player.sendTitle(color("&rLEVEL BUSY!"), "Please try again soon!");
	}

	public boolean hasPlayer(Player player) {
		return this.players.contains(player);
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
	
	// === Bow Practice: single-class implementation for Spigot 1.8 ===
	// Put these fields at the top of your existing class.
	private final Map<UUID, Location> bp_originByPlayer = new HashMap<>();
	private final Map<UUID, List<UUID>> bp_entitiesByPlayer = new HashMap<>();
	private int bp_nextLane = 0;

	// Tweakables
	private static final String BP_WORLD = "world";
	private static final Location BP_BASE_ORIGIN = new Location(Bukkit.getWorlds().get(0), 1000.5, 120, 1000.5);
	private static final int BP_LANE_SPACING = 40;   // distance between player lanes (X)
	private static final int BP_GAP_Z = 10;          // forward distance from shooter platform to target line
	private static final int BP_TARGET_LEN = 10;     // 1x10 target line
	private static final Material BP_SHOOT_BLOCK = Material.QUARTZ_BLOCK;
	private static final Material BP_TARGET_BLOCK = Material.STAINED_CLAY; // 1.8 name
	private static final byte BP_TARGET_COLOR = 14;  // red clay
	private static final String BP_TAG_META = "bowpractice";

	// === Call this to start for a player ===
	public void startBowPractice(Player p) {
	    if (bp_originByPlayer.containsKey(p.getUniqueId())) {
	        p.sendMessage("§cYou already have a bow practice active. Use /leave or stopBowPractice().");
	        return;
	    }

	    // Compute a unique lane origin (center of the 3x3 shooter platform)
	    Location origin = bp_computeLaneOrigin(bp_nextLane++);
	    bp_originByPlayer.put(p.getUniqueId(), origin);

	    // Build shooter platform (3x3 under feet)
	    bp_buildShooterPlatform(origin);

	    // Build target platform (1x10 across X, 10 blocks ahead on +Z)
	    Location targetCenter = origin.clone().add(0, 0, BP_GAP_Z);
	    bp_buildTargetLine(targetCenter);

	    // Spawn one zombie per target tile
	    List<UUID> spawned = new ArrayList<>();
	    bp_spawnZombiesAcross(targetCenter, spawned);
	    bp_entitiesByPlayer.put(p.getUniqueId(), spawned);

	    // Teleport & face +Z
	    Location tp = origin.clone();
	    tp.setYaw(0f); tp.setPitch(0f);
	    p.teleport(tp);

	    p.sendMessage("§aBow practice ready! Targets are 10 blocks ahead.");
	}

	// === Call this to stop/cleanup for a player ===
	public void stopBowPractice(Player p) {
	    UUID id = p.getUniqueId();
	    Location origin = bp_originByPlayer.remove(id);
	    if (origin == null) {
	        p.sendMessage("§cYou don't have a bow practice active.");
	        return;
	    }

	    // Remove platforms
	    bp_teardown(origin);

	    // Despawn tracked entities
	    List<UUID> list = bp_entitiesByPlayer.remove(id);
	    if (list != null) {
	        World w = origin.getWorld();
	        for (UUID eid : list) {
	            Entity e = null;
	            for (Entity nearby : w.getEntities()) {
	                if (nearby.getUniqueId().equals(eid)) { e = nearby; break; }
	            }
	            if (e != null) e.remove();
	        }
	    }

	    p.sendMessage("§aBow practice cleaned up.");
	}

	// === OPTIONAL: call this in onEnable() of the SAME class to auto-clean on quit ===
	// getServer().getPluginManager().registerEvents(this, this);

	// And include this listener method in the SAME class (since we're not making new classes)
	@org.bukkit.event.EventHandler
	public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent e) {
	    Player p = e.getPlayer();
	    if (bp_originByPlayer.containsKey(p.getUniqueId())) {
	        stopBowPractice(p);
	    }
	}

	// ====================== helpers (stay in the same class) ======================
	private Location bp_computeLaneOrigin(int laneIndex) {
	    World w = Bukkit.getWorld(BP_WORLD);
	    if (w == null) w = BP_BASE_ORIGIN.getWorld();
	    int dx = laneIndex * BP_LANE_SPACING;
	    return new Location(w, BP_BASE_ORIGIN.getX() + dx, BP_BASE_ORIGIN.getY(), BP_BASE_ORIGIN.getZ(), 0f, 0f);
	}

	private void bp_buildShooterPlatform(Location center) {
	    World w = center.getWorld();
	    int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
	    for (int x = -1; x <= 1; x++) {
	        for (int z = -1; z <= 1; z++) {
	            Block b = w.getBlockAt(cx + x, cy - 1, cz + z);
	            b.setType(BP_SHOOT_BLOCK);
	        }
	    }
	}

	private void bp_buildTargetLine(Location center) {
	    World w = center.getWorld();
	    int cx = center.getBlockX(), cy = center.getBlockY(), cz = center.getBlockZ();
	    int half = BP_TARGET_LEN / 2; // 5
	    for (int x = -half; x < -half + BP_TARGET_LEN; x++) {
	        Block b = w.getBlockAt(cx + x, cy - 1, cz);
	        b.setType(BP_TARGET_BLOCK);
	        // setData is valid on 1.8
	        b.setData(BP_TARGET_COLOR);
	    }
	}

	private void bp_spawnZombiesAcross(Location targetCenter, List<UUID> outEntityIds) {
	    World w = targetCenter.getWorld();
	    int cx = targetCenter.getBlockX(), cy = targetCenter.getBlockY(), cz = targetCenter.getBlockZ();
	    int half = BP_TARGET_LEN / 2;

	    for (int x = -half; x < -half + BP_TARGET_LEN; x++) {
	        Location spawn = new Location(w, cx + x + 0.5, cy, cz + 0.5, 0f, 0f);
	        Zombie z = w.spawn(spawn, Zombie.class);
	        z.setBaby(false);
	        z.setCanPickupItems(false);
	        z.setRemoveWhenFarAway(false);
	        z.setCustomNameVisible(false);

	        // Prevent sun-burn: give helmet (1.8 approach)
	        EntityEquipment eq = z.getEquipment();
	        ItemStack helm = new ItemStack(Material.LEATHER_HELMET, 1);
	        helm.addEnchantment(Enchantment.DURABILITY, 3); // pseudo "unbreaking"
	        eq.setHelmet(helm);
	        eq.setHelmetDropChance(0f);

	        // Tag via metadata (for debugging / identification)
	        
	        if (this.core != null)
	        	z.setMetadata(BP_TAG_META, new org.bukkit.metadata.FixedMetadataValue(this.core, true));
	        else
	        	System.out.println("Core is null");

	        outEntityIds.add(z.getUniqueId());
	    }
	}

	private void bp_teardown(Location origin) {
	    World w = origin.getWorld();
	    int cx = origin.getBlockX(), cy = origin.getBlockY(), cz = origin.getBlockZ();

	    // remove 3x3
	    for (int x = -1; x <= 1; x++) {
	        for (int z = -1; z <= 1; z++) {
	            w.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.AIR);
	        }
	    }

	    // remove 1x10 target
	    int half = BP_TARGET_LEN / 2;
	    int tz = cz + BP_GAP_Z;
	    for (int x = -half; x < -half + BP_TARGET_LEN; x++) {
	        w.getBlockAt(cx + x, cy - 1, tz).setType(Material.AIR);
	    }
	}
}
