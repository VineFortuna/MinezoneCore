package anthony.SuperCraftBrawl.npcs;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.lobbyexplorer.LobbyExplorers;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Packet-only fake player NPC (v1_8_R3) with per-viewer head tracking,
 * right-click callback, and multi-line name via invisible armor stands.
 * Restores Amy -> LobbyExplorerManager.checkSelectedExplorer(...) behavior.
 */
public class NPC {
	private final UUID uuid = UUID.randomUUID();
	private final String name;
	private final Location baseLoc;
	private final GameProfile profile;
	private final int entityId;
	private boolean headTrackingEnabled = true;
	private boolean mimicViewerSkin = false;
	private Function<Player, List<String>> perViewerLines = null;

	// keep a Core ref so we can call the explorer manager (restored behavior)
	private final Core core;

	// optional direct click handler (overrides explorer default if non-null)
	private final Consumer<Player> onRightClick;

	// which lobby explorer this NPC represents (can be null)
	private final LobbyExplorers explorer;

	private final Set<UUID> viewers = Collections.synchronizedSet(new HashSet<>());
	private BukkitRunnable lookTask;

	// multi-line floating text state
	private final List<String> nameLines = new ArrayList<>();
	private final Map<UUID, List<Integer>> hologramIds = new HashMap<>();

	public NPC(Core core, String name, Location loc, String skinValue, String skinSig, Consumer<Player> onRightClick,
			LobbyExplorers explorerName) {

		this.core = core;
		this.name = name.length() > 16 ? name.substring(0, 16) : name;
		this.baseLoc = loc.clone();
		this.profile = new GameProfile(uuid, this.name);

		if (skinValue != null && skinSig != null) {
			this.profile.getProperties().put("textures", new Property("textures", skinValue, skinSig));
		}
		this.entityId = new Random().nextInt(Integer.MAX_VALUE);
		this.onRightClick = onRightClick;
		this.explorer = explorerName;
	}

	public NPC disableHeadTracking() {
		this.headTrackingEnabled = false;
		return this;
	}

	public NPC mimicViewerSkin() {
		this.mimicViewerSkin = true;
		return this;
	}

	public NPC perViewerLines(Function<Player, List<String>> fn) {
		this.perViewerLines = fn;
		return this;
	}

	/** Optional: set multiple lines to display above the NPC (top line first). */
	public NPC setNameLines(String... lines) {
		this.nameLines.clear();
		if (lines != null)
			Collections.addAll(this.nameLines, lines);
		return this;
	}

	public int getEntityId() {
		return entityId;
	}

	public Location getLocation() {
		return baseLoc.clone();
	}

	/** Show the NPC to a single player via spawn packets. */
	public void showTo(Player p) {
		if (!p.getWorld().equals(baseLoc.getWorld()))
			return;
		if (!viewers.add(p.getUniqueId()))
			return;

		// Build NMS world/server from the NPC's world (not the viewer's)
		WorldServer world = ((CraftWorld) baseLoc.getWorld()).getHandle();
		MinecraftServer srv = world.getMinecraftServer();
		PlayerInteractManager pim = new PlayerInteractManager(world);

		EntityPlayer ep = new EntityPlayer(srv, world, profile, pim);
		ep.setLocation(baseLoc.getX(), baseLoc.getY(), baseLoc.getZ(), baseLoc.getYaw(), baseLoc.getPitch());

		// Add to tab briefly, then spawn
		PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo(
				PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ep);

		PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(ep);
		try {
			// 1.8 field "a" is entityId
			Field a = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("a");
			a.setAccessible(true);
			a.setInt(spawn, entityId);
		} catch (Exception ignored) {
		}

		// Initial head align
		byte yaw = toPackedByte(baseLoc.getYaw());
		PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation();
		try {
			setInt(head, "a", entityId);
			setByte(head, "b", yaw);
		} catch (Exception ignored) {
		}

		// Remove from tab after a short delay
		PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(
				PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ep);

		send(p, add);
		send(p, spawn);
		send(p, head);
		Bukkit.getScheduler().runTaskLater(Core.inst(), () -> send(p, remove), 10L);

		// Register so clicks can be routed by ChannelInjector
		NPCRegistry.register(this);

		// spawn multi-line "name" hologram
		spawnNameHologramFor(p);

		ensureLookTask();
	}

	/** Hide the NPC from a single player. */
	public void hideFrom(Player p) {
		if (!viewers.remove(p.getUniqueId()))
			return;
		send(p, new PacketPlayOutEntityDestroy(entityId));
		destroyNameHologramFor(p);

		if (viewers.isEmpty() && lookTask != null) {
			lookTask.cancel();
			lookTask = null;
		}
	}

	public void showToAll() {
		baseLoc.getWorld().getPlayers().forEach(this::showTo);
	}

	public void hideFromAll() {
		for (UUID id : new HashSet<>(viewers)) {
			Player p = Bukkit.getPlayer(id);
			if (p != null)
				hideFrom(p);
		}
	}

	/**
	 * Called by NPCRegistry when ChannelInjector detects a right click (with the
	 * Player).
	 */
	void handleRightClick(Player clicker) {
		if (onRightClick != null) { // explicit handler wins
			onRightClick.accept(clicker);
			return;
		}
		runExplorerAction(clicker); // fallback behavior by explorer enum
	}

	// ----- Explorer-specific default behaviors (Amy restored to use
	// LobbyExplorerManager) -----
	private void runExplorerAction(Player p) {
		if (explorer == null)
			return;

		switch (explorer) {
		case Amy:
			// RESTORED: this calls your LobbyExplorerManager, which will create
			// an AmyLobbyExplorer instance and manage per-player selection.
			if (core != null && core.getExplorerManager() != null) {
				core.getExplorerManager().checkSelectedExplorer(explorer, p);
			}
			break;

		case Steve:
			p.sendMessage(Core.inst().color("&aWelcome! Use &e/menu &ato begin."));
			break;

		// Add more cases for other LobbyExplorers as needed
		default:
			break;
		}
	}

	// --- Head tracking: face each viewer independently every 2 ticks ---
	private void ensureLookTask() {
		if (lookTask != null)
			return;
		lookTask = new BukkitRunnable() {
			@Override
			public void run() {
				if (viewers.isEmpty()) {
					cancel();
					lookTask = null;
					return;
				}

				for (UUID id : new HashSet<>(viewers)) {
					Player p = Bukkit.getPlayer(id);
					if (p == null || !p.isOnline() || !p.getWorld().equals(baseLoc.getWorld()))
						continue;
					if (p.getLocation().distanceSquared(baseLoc) > 14 * 14)
						continue;

					float[] yp = lookAt(baseLoc, p.getEyeLocation());
					byte yaw = toPackedByte(yp[0]);
					byte pitch = toPackedByte(yp[1]);

					PacketPlayOutEntity.PacketPlayOutEntityLook body = new PacketPlayOutEntity.PacketPlayOutEntityLook(
							entityId, yaw, pitch, true);
					PacketPlayOutEntityHeadRotation head = new PacketPlayOutEntityHeadRotation();
					try {
						setInt(head, "a", entityId);
						setByte(head, "b", yaw);
					} catch (Exception ignored) {
					}

					send(p, body);
					send(p, head);
				}
			}
		};
		lookTask.runTaskTimer(Core.inst(), 2L, 2L);
	}

	// ---------- Multi-line hologram (per viewer, packet-only) ----------
	private void spawnNameHologramFor(Player p) {
		if (nameLines.isEmpty())
			return;

		try {
			WorldServer world = ((CraftWorld) baseLoc.getWorld()).getHandle();

			// place lines a bit above the head; top line is highest
			double baseY = baseLoc.getY() + 1.50;
			double step = 0.25;

			List<Integer> ids = new ArrayList<>(nameLines.size());
			for (int i = 0; i < nameLines.size(); i++) {
				String raw = nameLines.get(i) == null ? "" : nameLines.get(i);
				String line = ChatColor.translateAlternateColorCodes('&', raw);

				EntityArmorStand stand = new EntityArmorStand(world);
				double y = baseY - (i * step);

				stand.setLocation(baseLoc.getX(), y, baseLoc.getZ(), 0f, 0f);
				stand.setCustomName(line);
				stand.setCustomNameVisible(true);
				stand.setInvisible(true);
				stand.setSmall(true);
				stand.setGravity(false);

				PacketPlayOutSpawnEntityLiving sp = new PacketPlayOutSpawnEntityLiving(stand);
				send(p, sp);

				ids.add(stand.getId());
			}
			hologramIds.put(p.getUniqueId(), ids);
		} catch (Throwable ignored) {
		}
	}

	private void destroyNameHologramFor(Player p) {
		List<Integer> ids = hologramIds.remove(p.getUniqueId());
		if (ids == null || ids.isEmpty())
			return;
		for (int id : ids) {
			send(p, new PacketPlayOutEntityDestroy(id));
		}
	}

	// --- helpers ---
	private static void send(Player p, Packet<?> pkt) {
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(pkt);
	}

	private static byte toPackedByte(float deg) {
		return (byte) (int) (deg * 256.0F / 360.0F);
	}

	/** Compute yaw/pitch from src to dst (entity eye height ≈ 1.62). */
	private static float[] lookAt(Location src, Location dst) {
		double dx = dst.getX() - src.getX();
		double dy = dst.getY() - (src.getY() + 1.62);
		double dz = dst.getZ() - src.getZ();
		double distXZ = Math.sqrt(dx * dx + dz * dz);

		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) Math.toDegrees(-Math.atan2(dy, distXZ));
		return new float[] { yaw, pitch };
	}

	private static void setInt(Object o, String f, int v) throws Exception {
		Field x = o.getClass().getDeclaredField(f);
		x.setAccessible(true);
		x.setInt(o, v);
	}

	private static void setByte(Object o, String f, byte v) throws Exception {
		Field x = o.getClass().getDeclaredField(f);
		x.setAccessible(true);
		x.setByte(o, v);
	}
}