package anthony.SuperCraftBrawl.sleigh;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.ParticleHelper;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * One player's sleigh. The player sits in the sleigh (on an invisible seat) and a horse in front pulls it.
 * Holding W makes the horse walk the way the player is looking.
 *
 * How it works, in the order things happen each tick:
 *  1. The player's keys (read from their steer packets) tell us whether they want to go. If so, the horse is
 *     told to walk towards where they look, through its own move controller, like any mob walking somewhere.
 *  2. The sleigh is held a fixed distance behind the horse by an imaginary drawbar: it's put exactly that
 *     far from the horse, on the line from the sleigh to the horse, and faces the horse. That makes it swing
 *     out wide on turns and follow the horse's path like a real towed sleigh.
 *  3. All the armor stands that make up the sleigh, plus the invisible seat the player rides, are moved there.
 *
 * Things that are done on purpose and easy to break:
 *  - The seat is moved with NMS setLocation: Bukkit's teleport() refuses to move an entity with a passenger.
 *  - The seat NEVER changes its yaw. A rider's view turns with their vehicle's yaw, so turning the seat with
 *    the sleigh would spin the player's camera, and since the horse follows where they look, it would feed back
 *    on itself.
 *  - Every entity is set to send position updates every tick. By default armor stands and horses only update
 *    every 3 ticks, each on its own schedule, so the parts of the sleigh would visibly slide apart at speed.
 *
 * Everything this creates is removed in remove(). Call it when tick() returns false (SleighManager does).
 */
final class Sleigh {

	/** Name given to the horse and every armor stand (never shown) so leftovers can be found after a crash. */
	static final String MARKER_NAME = "mz_sleigh";

	/** Distance from the sleigh's center to the horse's center, in blocks. */
	private static final double TOW_DISTANCE = 3.4;
	/** The horse's walking speed (vanilla horses top out around 0.34). */
	private static final double HORSE_SPEED = 0.25;
	/** A block on a normal armor stand's head sits this far above the stand's feet (worked out from the renderer). */
	private static final double HEAD_HEIGHT = 1.383;
	/** A block on a stand's head is 0.625 blocks wide, so the sleigh is built from cells of this size. */
	private static final double CELL = 0.625;
	private static final double SLAB = CELL / 2;

	/** How far above the sleigh's ground the player's seat is (the top of the seat floor). */
	private static final double SEAT_SURFACE = CELL;
	/**
	 * A rider's feet end up 0.35 below a zero-height (marker) stand, and a sitting player's seat contact is
	 * about 0.62 above their feet. So the stand goes 0.35 above where the feet should be.
	 */
	private static final double SEAT_STAND_OFFSET = SEAT_SURFACE - 0.62 + 0.35;
	/** How far behind the sleigh's center the player sits, so their back is against the seat back. */
	private static final double SEAT_BACK_OFFSET = 0.1;

	private static final Part[] PARTS = buildParts();

	/** One block of the sleigh, positioned relative to its center. */
	private static final class Part {
		final double right, forward, height; // forward = towards the horse; height = bottom of the block
		final ItemStack item;

		Part(double right, double forward, double height, ItemStack item) {
			this.right = right;
			this.forward = forward;
			this.height = height;
			this.item = item;
		}
	}

	/*
	 * The sleigh, side view (the horse is on the right, the player sits at the S):
	 *
	 *       _                              white trim on the rear walls, red body, dark runners and floor
	 *      | |   gifts
	 *      |W|G_
	 *      |R|BS_                           B = seat back, S = seat
	 *   ___|R|_____|R|___                   the front of the floor is lowered: the footwell
	 *  (___________________)=====          runners curl up at the front, shafts reach the horse
	 *
	 * Everything is a full block or a slab, so it looks the same whichever way a stand turns. (A stair's
	 * high side depends on how the client renders it on a head, which can't be checked without the game.)
	 */
	private static Part[] buildParts() {
		ItemStack spruce = new ItemStack(Material.WOOD_STEP, 1, (short) 1); // dark spruce slab
		ItemStack trim = new ItemStack(Material.STEP, 1, (short) 7);        // quartz slab, the white trim
		ItemStack red = new ItemStack(Material.WOOL, 1, (short) 14);
		ItemStack white = new ItemStack(Material.WOOL, 1, (short) 0);
		ItemStack green = new ItemStack(Material.WOOL, 1, (short) 13);

		List<Part> parts = new ArrayList<>();
		double[] sides = { -CELL, CELL };
		double[] rows = { -CELL, 0, CELL };

		for (double side : sides) {
			for (double row : rows) {
				parts.add(new Part(side, row, 0, spruce));   // runner
				parts.add(new Part(side, row, SLAB, red));   // wall
			}
			// A U-shaped profile like a real sleigh: high at the back, low where you sit, curling up at the front
			parts.add(new Part(side, -CELL, SLAB + CELL, white)); // rear wall, second layer
			parts.add(new Part(side, CELL, SLAB + CELL, trim));   // front wall, white trim on top
			parts.add(new Part(side, 2 * CELL, SLAB, spruce));       // runner lifting at the front...
			parts.add(new Part(side, 2 * CELL, 2 * SLAB, spruce));   // ...curling up...
			parts.add(new Part(side, 2 * CELL, 3 * SLAB, trim));     // ...into a white scroll
		}

		// Floor: the seat part is raised, the front is lowered into a footwell so the player's legs fit
		parts.add(new Part(0, -CELL, SLAB, spruce));
		parts.add(new Part(0, 0, SLAB, spruce));
		parts.add(new Part(0, CELL, 0, spruce));

		parts.add(new Part(0, -CELL, SEAT_SURFACE, red)); // seat back

		// Deck behind the seat with a present on it
		parts.add(new Part(0, -2 * CELL, SLAB, spruce));
		parts.add(new Part(0, -2 * CELL, SEAT_SURFACE, green));

		// Shafts reaching the horse (the last of each tucks in beside its harness)
		for (double side : new double[] { -0.5, 0.5 }) {
			parts.add(new Part(side, 2 * CELL + CELL, 0.4, spruce));
			parts.add(new Part(side, 2 * CELL + 2 * CELL, 0.4, spruce));
		}
		return parts.toArray(new Part[0]);
	}

	private final Core main;
	private final Player player;
	private Horse horse;
	private ArmorStand seat;
	private final List<ArmorStand> stands = new ArrayList<>();

	// Where the sleigh is right now, and which way it's pointing (a unit vector towards the horse)
	private double sleighX, sleighY, sleighZ;
	private double headingX, headingZ;
	private int ticks;

	Sleigh(Core main, Player player) {
		this.main = main;
		this.player = player;
	}

	/** The horse, or null before start() / after remove(). */
	Horse getHorse() {
		return horse;
	}

	/** The seat the player rides, or null before start() / after remove(). */
	ArmorStand getSeat() {
		return seat;
	}

	/**
	 * Spawns the horse in front of the player, builds the sleigh around them and sits them in it.
	 * Returns false (having cleaned up) if there's no room or anything went wrong.
	 */
	boolean start() {
		World world = player.getWorld();
		Location at = player.getLocation();

		// Find a direction with room for the horse; the player's own direction first
		double startYaw = Math.toRadians(at.getYaw());
		boolean found = false;
		for (int turn = 0; turn < 4 && !found; turn++) {
			double yaw = startYaw + turn * Math.PI / 2;
			double dirX = -Math.sin(yaw);
			double dirZ = Math.cos(yaw);
			double horseX = at.getX() + dirX * TOW_DISTANCE;
			double horseZ = at.getZ() + dirZ * TOW_DISTANCE;
			if (hasRoom(world, horseX, at.getY(), horseZ)) {
				headingX = dirX;
				headingZ = dirZ;
				found = true;
			}
		}
		if (!found)
			return false;

		sleighX = at.getX();
		sleighZ = at.getZ();
		sleighY = at.getY();
		double horseX = sleighX + headingX * TOW_DISTANCE;
		double horseZ = sleighZ + headingZ * TOW_DISTANCE;
		float yawDegrees = headingYaw();

		Horse spawned = (Horse) world.spawnEntity(new Location(world, horseX, groundY(world, horseX, horseZ, at.getY()),
				horseZ, yawDegrees, 0f), EntityType.HORSE);
		if (spawned == null || !spawned.isValid()) {
			if (spawned != null) spawned.remove();
			return false;
		}
		horse = spawned;
		setUpHorse(spawned);

		for (Part part : PARTS)
			stands.add(spawnStand(world, standLocation(world, part), part.item));

		seat = spawnStand(world, seatLocation(world), null);
		seat.setPassenger(player);
		if (seat.getPassenger() != player)
			return false; // the caller removes everything

		return true;
	}

	/**
	 * Drives the horse and moves the sleigh behind it. Returns false when the sleigh should be taken down:
	 * the rider got off, left the lobby, died or quit, or the horse or a stand disappeared.
	 */
	boolean tick() {
		if (horse == null || seat == null || !horse.isValid() || !seat.isValid() || !player.isOnline()
				|| player.isDead() || seat.getPassenger() != player || !main.isInLobby(player)
				|| player.getWorld() != horse.getWorld())
			return false;
		for (ArmorStand stand : stands) {
			if (!stand.isValid())
				return false;
		}

		ticks++;
		World world = horse.getWorld();
		drive(world);

		Location horseLoc = horse.getLocation();

		// The drawbar: the sleigh sits exactly TOW_DISTANCE from the horse, on the line from the sleigh to it
		double toHorseX = horseLoc.getX() - sleighX;
		double toHorseZ = horseLoc.getZ() - sleighZ;
		double distance = Math.sqrt(toHorseX * toHorseX + toHorseZ * toHorseZ);
		if (distance > 0.01) {
			headingX = toHorseX / distance;
			headingZ = toHorseZ / distance;
		}

		double newX = horseLoc.getX() - headingX * TOW_DISTANCE;
		double newZ = horseLoc.getZ() - headingZ * TOW_DISTANCE;

		// Follow the ground under the sleigh, smoothing small steps like stairs
		double targetY = groundY(world, newX, newZ, horseLoc.getY());
		double newY = Math.abs(targetY - sleighY) > 3 ? targetY : sleighY + (targetY - sleighY) * 0.5;

		double moved = Math.sqrt((newX - sleighX) * (newX - sleighX) + (newZ - sleighZ) * (newZ - sleighZ));
		boolean changed = moved > 0.001 || Math.abs(newY - sleighY) > 0.001;
		sleighX = newX;
		sleighY = newY;
		sleighZ = newZ;

		if (changed)
			moveSleigh(world);
		if (moved > 0.08)
			showTrail(world, ticks);
		return true;
	}

	/**
	 * A spot beside the sleigh to put the player once they're out of it, or null if they shouldn't be moved
	 * (they left the lobby or logged out) or there's no free spot.
	 */
	Location exitLocation() {
		if (horse == null || !player.isOnline() || !main.isInLobby(player) || player.getWorld() != horse.getWorld())
			return null;

		World world = horse.getWorld();
		double rightX = -headingZ;
		double rightZ = headingX;
		for (int side = 1; side >= -1; side -= 2) {
			double x = sleighX + rightX * 1.6 * side;
			double z = sleighZ + rightZ * 1.6 * side;
			if (hasRoom(world, x, sleighY, z)) {
				Location facing = player.getLocation();
				return new Location(world, x, groundY(world, x, z, sleighY), z, facing.getYaw(), facing.getPitch());
			}
		}
		return null;
	}

	/** Removes the seat (getting the player off), the horse and every stand. Safe to call more than once. */
	void remove() {
		if (seat != null) {
			ArmorStand old = seat;
			seat = null;
			old.eject();
			old.remove();
		}

		for (ArmorStand stand : stands)
			stand.remove();
		stands.clear();

		if (horse != null) {
			Horse old = horse;
			horse = null;
			old.remove();
		}
	}

	// ---------------------------------------------------------------- driving

	/** While the player holds W the horse walks the way they're looking; otherwise it's left to stop. */
	private void drive(World world) {
		EntityInsentient mob = (EntityInsentient) ((CraftLivingEntity) horse).getHandle();
		EntityPlayer rider = ((CraftPlayer) player).getHandle();
		if (rider.ba <= 0.1f)
			return; // W isn't held (the keys arrive in the player's steer packets)

		double yaw = Math.toRadians(player.getLocation().getYaw());
		double lookX = -Math.sin(yaw);
		double lookZ = Math.cos(yaw);
		Location at = horse.getLocation();

		// An unridden horse can only step up half a block, so it jumps up anything taller in its way
		if (horse.isOnGround()) {
			Block ahead = world.getBlockAt((int) Math.floor(at.getX() + lookX * 0.9), (int) Math.floor(at.getY()),
					(int) Math.floor(at.getZ() + lookZ * 0.9));
			if (ahead.getType().isSolid() && !ahead.getRelative(0, 1, 0).getType().isSolid()
					&& !ahead.getRelative(0, 2, 0).getType().isSolid())
				mob.getControllerJump().a();
		}

		// Walk towards a point well ahead in the direction they're looking (same height, so no jump is requested)
		mob.getControllerMove().a(at.getX() + lookX * 6, at.getY(), at.getZ() + lookZ * 6, 1.0);
	}

	private void setUpHorse(Horse spawned) {
		spawned.setCustomName(MARKER_NAME);
		spawned.setCustomNameVisible(false);
		spawned.setTamed(true);
		spawned.setAdult();
		spawned.setVariant(Horse.Variant.HORSE);
		spawned.setColor(Horse.Color.DARK_BROWN);
		spawned.setStyle(Horse.Style.NONE);
		spawned.getInventory().setSaddle(new ItemStack(Material.SADDLE));

		try {
			EntityInsentient mob = (EntityInsentient) ((CraftLivingEntity) spawned).getHandle();
			mob.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(HORSE_SPEED);
			clearGoals(mob);
		} catch (Throwable t) {
			main.getLogger().warning("[Sleigh] Could not set up the horse's AI: " + t);
		}
		sendUpdatesEveryTick(spawned);
	}

	/** Removes every AI goal so the horse never wanders, panics or follows anyone: it only goes where we send it. */
	private static void clearGoals(EntityInsentient mob) throws ReflectiveOperationException {
		// PathfinderGoalSelector.a() is its per-tick update, not a clear: the goals live in these two private lists
		for (String fieldName : new String[] { "b", "c" }) {
			Field field = PathfinderGoalSelector.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			((List<?>) field.get(mob.goalSelector)).clear();
		}
	}

	// ---------------------------------------------------------------- the sleigh

	private ArmorStand spawnStand(World world, Location at, ItemStack head) {
		ArmorStand stand = (ArmorStand) world.spawnEntity(at, EntityType.ARMOR_STAND);
		stand.setCustomName(MARKER_NAME);
		stand.setCustomNameVisible(false);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setBasePlate(false);
		stand.setMarker(true); // no hitbox, so it can't be hit, clicked or looted
		if (head != null)
			stand.setHelmet(head.clone());
		sendUpdatesEveryTick(stand);
		return stand;
	}

	/** Moves every stand and the seat to where the sleigh now is. */
	private void moveSleigh(World world) {
		for (int i = 0; i < PARTS.length; i++)
			place(stands.get(i), standLocation(world, PARTS[i]));
		place(seat, seatLocation(world));
	}

	/*
	 * NMS setLocation instead of Bukkit's teleport(), which refuses to move an entity that has a passenger
	 * (the seat). The rider is carried along by the seat on their own tick.
	 */
	private static void place(org.bukkit.entity.Entity entity, Location to) {
		((CraftEntity) entity).getHandle().setLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
	}

	/** Where a part's stand goes: the sleigh's position plus the part's offset, turned to face the horse. */
	private Location standLocation(World world, Part part) {
		double rightX = -headingZ; // the sleigh's right-hand side
		double rightZ = headingX;
		double x = sleighX + rightX * part.right + headingX * part.forward;
		double z = sleighZ + rightZ * part.right + headingZ * part.forward;
		double y = sleighY + part.height - HEAD_HEIGHT;
		return new Location(world, x, y, z, headingYaw(), 0f);
	}

	/** The seat: always yaw 0, see the class comment. */
	private Location seatLocation(World world) {
		return new Location(world, sleighX - headingX * SEAT_BACK_OFFSET, sleighY + SEAT_STAND_OFFSET,
				sleighZ - headingZ * SEAT_BACK_OFFSET, 0f, 0f);
	}

	private float headingYaw() {
		return (float) Math.toDegrees(Math.atan2(-headingX, headingZ));
	}

	/** Snow kicked up behind the runners, and sleigh bells now and then. */
	private void showTrail(World world, int tick) {
		if (tick % 2 == 0) {
			Location rear = new Location(world, sleighX - headingX * CELL * 2.5, sleighY + 0.15, sleighZ - headingZ * CELL * 2.5);
			ParticleHelper.play(rear, EnumParticle.SNOW_SHOVEL, 0.35f, 0.05f, 0.35f, 0.02f, 3);
			ParticleHelper.play(rear, EnumParticle.CLOUD, 0.25f, 0.02f, 0.25f, 0.01f, 1);
		}
		if (tick % 16 == 0)
			world.playSound(new Location(world, sleighX, sleighY, sleighZ), Sound.ORB_PICKUP, 0.4f, 1.8f);
	}

	// ---------------------------------------------------------------- helpers

	/**
	 * Armor stands and horses send their position to players every 3 ticks (each on its own timer), so the
	 * parts of a moving sleigh would drift apart. Setting the interval to 1 keeps them all in step.
	 */
	private static void sendUpdatesEveryTick(org.bukkit.entity.Entity entity) {
		try {
			net.minecraft.server.v1_8_R3.Entity handle = ((CraftEntity) entity).getHandle();
			EntityTrackerEntry entry = ((WorldServer) handle.world).tracker.trackedEntities.get(handle.getId());
			if (entry != null)
				entry.c = 1;
		} catch (Throwable ignored) {
			// Worst case the sleigh looks a little choppier
		}
	}

	/** Whether there's room for a player/horse standing here: the two blocks above the ground are free. */
	private static boolean hasRoom(World world, double x, double nearY, double z) {
		double y = groundY(world, x, z, nearY);
		int blockX = (int) Math.floor(x);
		int blockZ = (int) Math.floor(z);
		int blockY = (int) Math.floor(y);
		return !world.getBlockAt(blockX, blockY, blockZ).getType().isSolid()
				&& !world.getBlockAt(blockX, blockY + 1, blockZ).getType().isSolid();
	}

	/** The top surface of the highest solid block under the point, looking a few blocks around fromY. */
	private static double groundY(World world, double x, double z, double fromY) {
		int blockX = (int) Math.floor(x);
		int blockZ = (int) Math.floor(z);
		int top = (int) Math.floor(fromY) + 1;

		for (int y = top; y >= top - 4; y--) {
			Block block = world.getBlockAt(blockX, y, blockZ);
			if (block.getType().isSolid())
				return y + 1;
		}
		return fromY;
	}
}
