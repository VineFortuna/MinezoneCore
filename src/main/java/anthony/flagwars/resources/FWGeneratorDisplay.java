package anthony.flagwars.resources;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.map.FWMapConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Hypixel-Bedwars-style cosmetic above each emerald/diamond generator: a
 * spinning block of emerald/diamond with a two-line hologram (title + live
 * countdown) floating above it, plus a rotating ring of colored particles
 * that appears for the last few seconds before each spawn. Purely visual -
 * FWResourceGenerator still owns the actual drop timing and pushes the
 * countdown text and particle on/off state in here once a second, and reads
 * floatingLocation() back to know exactly where the block (and therefore the
 * dropped resource) actually sits.
 */
public class FWGeneratorDisplay {

	private static final double SPIN_DEGREES_PER_TICK = 4.0;
	private static final double BLOCK_HEIGHT = 1.0;
	// Same gap FloatingBlocks uses above its own floating item, so the text sits clearly above the block.
	private static final double COUNTDOWN_HEIGHT = BLOCK_HEIGHT + 2.30;
	private static final double TITLE_HEIGHT = BLOCK_HEIGHT + 2.55;

	private static final int RING_POINTS = 16;
	private static final double RING_RADIUS = 0.75;
	private static final double RING_DEGREES_PER_TICK = 18.0;
	private static final Color DIAMOND_PARTICLE_COLOR = new Color(85, 255, 255);
	private static final Color EMERALD_PARTICLE_COLOR = new Color(0, 200, 83);

	// Estimated height of the equipped helmet block's visual center above the armor stand's own
	// spawn point (a full-size stand's head sits well above its feet). This is the one number to
	// nudge in-game if the ring or the dropped resource still look off-center on the block.
	private static final double HEAD_VISUAL_OFFSET = 1.62;

	private final FWGameInstance instance;
	private final List<Entry> entries = new ArrayList<>();
	private BukkitTask task;

	private static final class Entry {
		final Location baseLoc;
		final FWResourceType type;
		ArmorStand blockStand;
		ArmorStand titleStand;
		ArmorStand countdownStand;
		double angleDeg;
		double particleAngleDeg;
		double particleFilledDeg;
		boolean particlesActive;

		Entry(Location loc, FWResourceType type) {
			this.baseLoc = loc.clone();
			this.type = type;
		}
	}

	public FWGeneratorDisplay(FWGameInstance instance) {
		this.instance = instance;
	}

	public void start() {
		if (task != null) return;

		FWMapConfig config = instance.getMapConfig();
		for (Location loc : config.getEmeraldGenerators().values()) {
			entries.add(new Entry(loc, FWResourceType.EMERALD));
		}
		for (Location loc : config.getDiamondGenerators().values()) {
			entries.add(new Entry(loc, FWResourceType.DIAMOND));
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

	/** Called by FWResourceGenerator once a second with the new countdown for every generator of this resource type. */
	public void updateCountdown(FWResourceType type, int secondsLeft) {
		Core main = instance.getManager().getMain();
		String line = main.color("&rRegenerates In: &a" + secondsLeft + "s");

		for (Entry entry : entries) {
			if (entry.type != type) continue;
			if (entry.countdownStand != null && !entry.countdownStand.isDead()) {
				entry.countdownStand.setCustomName(line);
			}
		}
	}

	/** Called by FWResourceGenerator to toggle the warning ring on/off for every generator of this resource type. */
	public void setParticlesActive(FWResourceType type, boolean active) {
		for (Entry entry : entries) {
			if (entry.type != type) continue;
			if (active && !entry.particlesActive) {
				entry.particleAngleDeg = 0;
				entry.particleFilledDeg = 0;
			}
			entry.particlesActive = active;
		}
	}

	/** The fixed spot the floating block sits at, one block above the configured ground point. */
	public static Location floatingLocation(Location base) {
		return base.clone().add(0, BLOCK_HEIGHT, 0);
	}

	/** Where the helmet block visually centers - used for the ring and so the resource appears to fall right out of the block. */
	public static Location blockCenterLocation(Location base) {
		return floatingLocation(base).add(0, HEAD_VISUAL_OFFSET, 0);
	}

	private void spawn(Entry entry) {
		World world = entry.baseLoc.getWorld();
		if (world == null) return;

		Core main = instance.getManager().getMain();
		boolean emerald = entry.type == FWResourceType.EMERALD;

		ArmorStand block = (ArmorStand) world.spawnEntity(floatingLocation(entry.baseLoc), EntityType.ARMOR_STAND);
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

	private void animate(Entry entry) {
		if (entry.blockStand == null || entry.blockStand.isDead()) return;

		entry.angleDeg = (entry.angleDeg + SPIN_DEGREES_PER_TICK) % 360;
		entry.blockStand.setHeadPose(new EulerAngle(0, Math.toRadians(entry.angleDeg), 0));

		if (entry.particlesActive) {
			spawnParticleRing(entry);
		}
	}

	/**
	 * Animates a single particle flying around the block. As it sweeps past each anchor point on
	 * the ring, that point starts getting refreshed every tick too, so it stays lit instead of
	 * fading - the ring visibly fills in over one lap instead of popping in all at once. Once a
	 * full lap has passed, every anchor point is filled and the ring just keeps spinning, lit in
	 * full, until setParticlesActive(type, false) stops new particles being sent entirely.
	 */
	private void spawnParticleRing(Entry entry) {
		entry.particleAngleDeg = (entry.particleAngleDeg + RING_DEGREES_PER_TICK) % 360;
		entry.particleFilledDeg = Math.min(360.0, entry.particleFilledDeg + RING_DEGREES_PER_TICK);

		Location center = blockCenterLocation(entry.baseLoc);
		Color color = entry.type == FWResourceType.DIAMOND ? DIAMOND_PARTICLE_COLOR : EMERALD_PARTICLE_COLOR;

		spawnDust(center, color, entry.particleAngleDeg);

		double stepDeg = 360.0 / RING_POINTS;
		for (double pointDeg = 0; pointDeg < entry.particleFilledDeg; pointDeg += stepDeg) {
			spawnDust(center, color, pointDeg);
		}
	}

	private void spawnDust(Location center, Color color, double angleDeg) {
		double angleRad = Math.toRadians(angleDeg);
		Location point = center.clone().add(Math.cos(angleRad) * RING_RADIUS, 0, Math.sin(angleRad) * RING_RADIUS);
		new ParticleBuilder(ParticleEffect.REDSTONE).setColor(color).setLocation(point).display();
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
