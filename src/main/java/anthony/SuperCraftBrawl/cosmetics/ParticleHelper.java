package anthony.SuperCraftBrawl.cosmetics;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Sends particle packets only to players in the same world and within range, instead of to every
 * online player. Holds no state, so there's nothing to clean up.
 */
public final class ParticleHelper {

	/** Particles are sent as "long distance" (visible up to 256 blocks), so cap who we send to. */
	private static final double MAX_DISTANCE_SQUARED = 64.0 * 64.0;

	private ParticleHelper() {
	}

	/** A normal particle burst. Offsets spread the particles out; count is how many. */
	public static void play(Location loc, EnumParticle particle, float offsetX, float offsetY, float offsetZ,
	                        float speed, int count) {
		sendToNearby(loc, new PacketPlayOutWorldParticles(particle, true,
				(float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
				offsetX, offsetY, offsetZ, speed, count));
	}

	/** One coloured dust particle (0-255 per channel). */
	public static void dust(Location loc, int red, int green, int blue) {
		sendToNearby(loc, dustPacket(loc, red, green, blue));
	}

	/** Same as play(), but only the given player sees it. */
	public static void playTo(Player viewer, Location loc, EnumParticle particle, float offsetX, float offsetY,
	                          float offsetZ, float speed, int count) {
		send(viewer, new PacketPlayOutWorldParticles(particle, true,
				(float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
				offsetX, offsetY, offsetZ, speed, count));
	}

	private static PacketPlayOutWorldParticles dustPacket(Location loc, int red, int green, int blue) {
		// With a count of 0 the 1.8 client reads the offsets as the colour (0-1). A red of exactly 0
		// is treated as full red, so keep it just above 0 for colours like green.
		float r = Math.max(red / 255f, 0.001f);
		return new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, true,
				(float) loc.getX(), (float) loc.getY(), (float) loc.getZ(),
				r, green / 255f, blue / 255f, 1f, 0);
	}

	private static void sendToNearby(Location loc, Packet<?> packet) {
		World world = loc.getWorld();
		if (world == null)
			return;

		for (Player viewer : world.getPlayers()) {
			if (viewer.getLocation().distanceSquared(loc) <= MAX_DISTANCE_SQUARED)
				send(viewer, packet);
		}
	}

	private static void send(Player viewer, Packet<?> packet) {
		if (viewer instanceof CraftPlayer)
			((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
	}
}
