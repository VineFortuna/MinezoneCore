package anthony.SuperCraftBrawl.Game.classes.all;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayInSteerVehicle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

/*
 * Santa's flying Reindeer: a horse the player rides and steers. WASD flies horizontally (relative to where
 * they face), Space flies up, Sneak flies down, and letting go of every key hovers in place. Sneaking never
 * dismounts during the flight. Used by Santa's class and his win effect.
 */
public class ReindeerFlight {

	private static final int WARNING_TICKS = 3 * 20;
	private static final double SPEED = 0.7; // blocks per tick
	private static final double VERTICAL_SPEED = 0.5; // blocks per tick
	private static final Field RIDER_JUMP_FIELD = findRiderJumpField();
	private static final Field STEER_SNEAK_FIELD = findSteerSneakField();
	// Riders currently flying, looked up from the netty thread when their steer packets arrive
	private static final Map<UUID, ReindeerFlight> FLYING = new ConcurrentHashMap<>();
	private static final Color RED = new Color(220, 20, 20);
	private static final Color GREEN = new Color(20, 160, 40);

	private final Player player;
	private final int durationTicks; // 0 = until it's ended
	private final BooleanSupplier canFly;
	private Horse reindeer;
	private BukkitTask task;
	private int flightTicks;
	private volatile boolean sneakHeld;

	/** canFly is checked every tick: once it's false the flight ends. */
	public ReindeerFlight(Player player, int durationTicks, BooleanSupplier canFly) {
		this.player = player;
		this.durationTicks = durationTicks;
		this.canFly = canFly;
	}

	/** Spawns the Reindeer and mounts the player. Returns the flight's task (null if it's already flying). */
	public BukkitTask start(Plugin plugin) {
		if (reindeer != null)
			return null;

		Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
		horse.setTamed(true);
		horse.setAdult();
		horse.setVariant(Horse.Variant.HORSE);
		horse.setColor(Horse.Color.DARK_BROWN);
		horse.setStyle(Horse.Style.NONE);
		horse.setCustomName("" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Reindeer");
		horse.setOwner(player);
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.setPassenger(player);

		reindeer = horse;
		flightTicks = 0;
		sneakHeld = false;
		FLYING.put(player.getUniqueId(), this);

		// The rider's keys arrive in steer packets every tick and are stored on the NMS player
		EntityPlayer rider = ((CraftPlayer) player).getHandle();

		task = new BukkitRunnable() {
			@Override
			public void run() {
				flightTicks++;
				if ((durationTicks > 0 && flightTicks > durationTicks) || !horse.isValid()
						|| horse.getPassenger() != player || !player.isOnline() || !canFly.getAsBoolean()) {
					end();
					return;
				}

				float forward = Math.signum(rider.ba);
				float strafe = Math.signum(rider.aZ); // positive = A (left)
				int vertical = (isJumping(rider) ? 1 : 0) - (sneakHeld ? 1 : 0);

				double yaw = Math.toRadians(player.getLocation().getYaw());
				double dirX = -Math.sin(yaw);
				double dirZ = Math.cos(yaw);
				Vector horizontal = new Vector(dirX * forward + dirZ * strafe, 0, dirZ * forward - dirX * strafe);
				if (horizontal.lengthSquared() > 1) horizontal.normalize();

				horse.setVelocity(horizontal.multiply(SPEED).setY(vertical * VERTICAL_SPEED));

				// Flying never builds fall damage for the reindeer or its rider
				horse.setFallDistance(0);
				player.setFallDistance(0);

				showTrail(horse.getLocation());

				// Warning beeps for the last 3 seconds before the reindeer disappears
				if (durationTicks > 0 && flightTicks >= durationTicks - WARNING_TICKS && flightTicks % 10 == 0) {
					horse.getWorld().playSound(horse.getLocation(), Sound.NOTE_PLING, 0.6f, 2.0f);
				}
			}
		}.runTaskTimer(plugin, 1, 1);
		return task;
	}

	// Magic trail: sparks, snow and red/green dust behind the reindeer, plus sleigh bells
	private void showTrail(Location loc) {
		Location body = loc.clone().add(0, 0.8, 0);
		ParticleEffect.FIREWORKS_SPARK.display(body, 0.4f, 0.3f, 0.4f, 0.02f, 4, null);
		ParticleEffect.SNOW_SHOVEL.display(body, 0.5f, 0.3f, 0.5f, 0.02f, 3, null);
		ParticleEffect.REDSTONE.display(body.clone().add(0, 0.4, 0), flightTicks % 2 == 0 ? RED : GREEN);
		ParticleEffect.REDSTONE.display(loc.clone().add(0, 0.2, 0), flightTicks % 2 == 0 ? GREEN : RED);
		if (flightTicks % 20 == 0) loc.getWorld().playSound(loc, Sound.ORB_PICKUP, 0.4f, 1.8f);
	}

	public boolean isFlying() {
		return reindeer != null;
	}

	/** Ends the flight: stops the controller, drops the rider off and removes the reindeer. Safe to call twice. */
	public void end() {
		if (reindeer == null)
			return;

		Horse horse = reindeer;
		reindeer = null; // clear first so the eject below isn't blocked by shouldBlockExit
		FLYING.remove(player.getUniqueId(), this);
		if (task != null) task.cancel();
		task = null;
		horse.eject();
		horse.remove();
		player.setFallDistance(0);
	}

	/** Called from GameManager's VehicleExitEvent: keeps the rider mounted for the whole flight. */
	public boolean shouldBlockExit(Entity vehicle) {
		return reindeer != null && vehicle == reindeer && reindeer.isValid() && !player.isDead();
	}

	/*
	 * Called on the netty thread for every steer packet (ChannelInjector). A flying rider's Sneak flag is read
	 * as "fly down" and then cleared, so vanilla never tries to dismount them. Letting vanilla try and cancelling
	 * it instead makes the server resend the mount + a teleport, and since 1.8 ticks a rider on every move packet,
	 * newer clients (via ViaVersion) answer each teleport with more sneaking packets until they're kicked for spam.
	 */
	public static void onSteerPacket(UUID playerId, PacketPlayInSteerVehicle packet) {
		ReindeerFlight flight = FLYING.get(playerId);
		if (flight == null || STEER_SNEAK_FIELD == null)
			return;

		flight.sneakHeld = packet.d();
		if (flight.sneakHeld) {
			try {
				STEER_SNEAK_FIELD.setBoolean(packet, false);
			} catch (IllegalAccessException ignored) {
			}
		}
	}

	private static boolean isJumping(EntityPlayer rider) {
		try {
			return RIDER_JUMP_FIELD != null && RIDER_JUMP_FIELD.getBoolean(rider);
		} catch (IllegalAccessException e) {
			return false;
		}
	}

	// PacketPlayInSteerVehicle.d is the "sneak held" flag (1.8.8 mapping)
	private static Field findSteerSneakField() {
		try {
			Field field = PacketPlayInSteerVehicle.class.getDeclaredField("d");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	// EntityLiving.aY is the protected "jump key held" flag set from the rider's steer packet (1.8.8 mapping)
	private static Field findRiderJumpField() {
		try {
			Field field = EntityLiving.class.getDeclaredField("aY");
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			return null;
		}
	}
}
