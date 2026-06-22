package anthony.flagdefense.resources;

import anthony.SuperCraftBrawl.Core;
import anthony.flagdefense.FDGameInstance;
import anthony.flagdefense.map.FDMapConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

/**
 * Hypixel-Bedwars-style cosmetic above each emerald/diamond generator: a
 * spinning, gently bobbing block of emerald/diamond with a two-line hologram
 * (title + live countdown) floating above it. Purely visual - FDResourceGenerator
 * still owns the actual drop timing and pushes the countdown text in here once
 * a second via updateCountdown().
 */
public class FDGeneratorDisplay {

	private static final double SPIN_DEGREES_PER_TICK = 4.0;
	private static final double BOB_AMPLITUDE = 0.15;
	private static final double BOB_RADIANS_PER_TICK = 0.05;
	private static final double BLOCK_HEIGHT = 1.0;
	// Same gap FloatingBlocks uses above its own floating item, so the text sits
	// clearly above the block regardless of its bob - never overlaps or clips it.
	private static final double COUNTDOWN_HEIGHT = BLOCK_HEIGHT + 2.30;
	private static final double TITLE_HEIGHT = BLOCK_HEIGHT + 2.55;

	private final FDGameInstance instance;
	private final List<Entry> entries = new ArrayList<>();
	private BukkitTask task;

	private static final class Entry {
		final Location baseLoc;
		final FDResourceType type;
		ArmorStand blockStand;
		ArmorStand titleStand;
		ArmorStand countdownStand;
		double angleDeg;
		double bobPhase;

		Entry(Location loc, FDResourceType type) {
			this.baseLoc = loc.clone();
			this.type = type;
			this.bobPhase = Math.random() * Math.PI * 2;
		}
	}

	public FDGeneratorDisplay(FDGameInstance instance) {
		this.instance = instance;
	}

	public void start() {
		if (task != null) return;

		FDMapConfig config = instance.getManager().getMapConfigManager().getConfig();
		for (Location loc : config.getEmeraldGenerators().values()) {
			entries.add(new Entry(loc, FDResourceType.EMERALD));
		}
		for (Location loc : config.getDiamondGenerators().values()) {
			entries.add(new Entry(loc, FDResourceType.DIAMOND));
		}

		for (Entry entry : entries) {
			spawn(entry);
		}

		task = Bukkit.getScheduler().runTaskTimer(instance.getManager().getMain(), () -> {
			for (Entry entry : entries) {
				animate(entry);
			}
		}, 0L, 1L);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		for (Entry entry : entries) {
			despawn(entry);
		}
		entries.clear();
	}

	/** Called by FDResourceGenerator once a second with the new countdown for every generator of this resource type. */
	public void updateCountdown(FDResourceType type, int secondsLeft) {
		Core main = instance.getManager().getMain();
		String line = main.color("&rRegenerates In: &a" + secondsLeft + "s");

		for (Entry entry : entries) {
			if (entry.type != type) continue;
			if (entry.countdownStand != null && !entry.countdownStand.isDead()) {
				entry.countdownStand.setCustomName(line);
			}
		}
	}

	private void spawn(Entry entry) {
		World world = entry.baseLoc.getWorld();
		if (world == null) return;

		Core main = instance.getManager().getMain();
		boolean emerald = entry.type == FDResourceType.EMERALD;

		ArmorStand block = (ArmorStand) world.spawnEntity(
				entry.baseLoc.clone().add(0, BLOCK_HEIGHT, 0), EntityType.ARMOR_STAND);
		block.setVisible(false);
		block.setGravity(false);
		block.setBasePlate(false);
		block.setArms(false);
		block.setSmall(false);
		block.setRemoveWhenFarAway(false);
		block.setHelmet(new ItemStack(emerald ? Material.EMERALD_BLOCK : Material.DIAMOND_BLOCK));
		setMarker(block);
		entry.blockStand = block;

		String titleText = main.color(emerald ? "&e&lEmerald Generator" : "&b&lDiamond Generator");
		entry.titleStand = spawnTextStand(world, entry.baseLoc.clone().add(0, TITLE_HEIGHT, 0), titleText);
		entry.countdownStand = spawnTextStand(world, entry.baseLoc.clone().add(0, COUNTDOWN_HEIGHT, 0), "");
	}

	private ArmorStand spawnTextStand(World world, Location loc, String name) {
		ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setBasePlate(false);
		stand.setArms(false);
		stand.setSmall(true);
		stand.setRemoveWhenFarAway(false);
		stand.setCustomName(name);
		stand.setCustomNameVisible(true);
		setMarker(stand);
		return stand;
	}

	private void setMarker(ArmorStand stand) {
		try {
			stand.setMarker(true);
		} catch (Throwable ignored) {
		}
	}

	/**
	 * Bukkit's Entity#teleport() updates the server-side position immediately,
	 * but the vanilla entity tracker only broadcasts ArmorStand position changes
	 * on its own (much slower) polling interval - calling teleport() alone every
	 * tick looks like "jump, pause, jump" instead of smooth motion. Forcing a
	 * teleport packet out ourselves every tick (same NMS-packet trick FloatingBlocks
	 * uses for its subtitle text) bypasses that throttling entirely.
	 */
	private void animate(Entry entry) {
		if (entry.blockStand == null || entry.blockStand.isDead()) return;

		entry.angleDeg = (entry.angleDeg + SPIN_DEGREES_PER_TICK) % 360;
		entry.blockStand.setHeadPose(new EulerAngle(0, Math.toRadians(entry.angleDeg), 0));

		entry.bobPhase += BOB_RADIANS_PER_TICK;
		double yOffset = Math.sin(entry.bobPhase) * BOB_AMPLITUDE;
		entry.blockStand.teleport(entry.baseLoc.clone().add(0, BLOCK_HEIGHT + yOffset, 0));
		broadcastPosition(entry.blockStand);
	}

	private void broadcastPosition(Entity entity) {
		try {
			net.minecraft.server.v1_8_R3.Entity nmsEntity =
					((org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity) entity).getHandle();
			net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport packet =
					new net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport(nmsEntity);

			for (Player player : entity.getWorld().getPlayers()) {
				((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player)
						.getHandle().playerConnection.sendPacket(packet);
			}
		} catch (Throwable ignored) {
		}
	}

	private void despawn(Entry entry) {
		remove(entry.blockStand);
		remove(entry.titleStand);
		remove(entry.countdownStand);
	}

	private void remove(Entity entity) {
		if (entity != null && !entity.isDead()) {
			entity.remove();
		}
	}
}
