package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.playerdata.ParkourDetails;
import anthony.parkour.Arenas;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkourBoard extends LeaderboardBase {
	private final Core main;
	private final Arenas arena;

	// Global (Lifetime) data
	private final List<UUID> lifetimeTopIds = new ArrayList<>();
	private final List<String> lifetimeTopNames = new ArrayList<>();
	private final Map<UUID, String> lifetimeTimes = new HashMap<>();

	// Global entity IDs (destroy for everyone on global redraw)
	private final List<Integer> globalEntityIds = new ArrayList<>();

	// Scoped caches (used by per-viewer preview)
	private final Map<LeaderboardScope, List<UUID>> topIds = new EnumMap<>(LeaderboardScope.class);
	private final Map<LeaderboardScope, List<String>> topNames = new EnumMap<>(LeaderboardScope.class);
	private final Map<LeaderboardScope, Map<UUID, String>> topValues = new EnumMap<>(LeaderboardScope.class);

	// Per-viewer holograms (IDs to destroy only for that viewer)
	private final Map<UUID, List<Integer>> viewerEntityIds = new HashMap<>();
	private static final AtomicInteger ENTITY_ID = new AtomicInteger(700000); // custom id base, offset from other boards

	public ParkourBoard(Core main, Arenas arena) {
		super(main);
		this.main = main;
		this.arena = arena;
		for (LeaderboardScope s : LeaderboardScope.values()) {
			topIds.put(s, new ArrayList<>());
			topNames.put(s, new ArrayList<>());
			topValues.put(s, new HashMap<>());
		}
	}

	// ---------- SQL helpers ----------
	// Parkour ranks by best time (ASC), so scoped periods don't apply — all scopes use lifetime
	private String sqlForScope() {
		return "SELECT p.LastPlayerName, p.RoleID, parkour.TotalTime, p.UUID " +
				"FROM PlayerData p " +
				"JOIN PlayerParkour parkour ON p.UUID = parkour.UUID " +
				"WHERE parkour.ParkourID = '" + arena.getId() + "' " +
				"ORDER BY parkour.TotalTime ASC LIMIT 10";
	}

	// ---------- Data refresh ----------
	@Override
	public void asyncUpdate() throws SQLException {
		try (Statement st = main.getDatabaseManager().getConnection().createStatement()) {
			List<UUID> ids = new ArrayList<>();
			List<String> names = new ArrayList<>();
			Map<UUID, String> vals = new HashMap<>();

			try (ResultSet rs = st.executeQuery(sqlForScope())) {
				while (rs.next()) {
					String uuidStr = rs.getString("UUID");
					String name = rs.getString("LastPlayerName");
					if (uuidStr == null || name == null) continue;
					UUID id = UUID.fromString(uuidStr);
					ids.add(id);
					names.add(name);
					vals.put(id, formatTime(rs.getLong("TotalTime")));
				}
			}

			// All scopes share the same data for parkour
			for (LeaderboardScope scope : LeaderboardScope.values()) {
				topIds.put(scope, new ArrayList<>(ids));
				topNames.put(scope, new ArrayList<>(names));
				topValues.put(scope, new HashMap<>(vals));
			}

			lifetimeTopIds.clear();
			lifetimeTopNames.clear();
			lifetimeTimes.clear();
			lifetimeTopIds.addAll(ids);
			lifetimeTopNames.addAll(names);
			lifetimeTimes.putAll(vals);
		}
	}

	// ---------- Global render ----------
	@Override
	public void updateLeaderboard(boolean init) {
		removeOldLeaderboards();

		Location title = arena.getInstance().leaderboardLoc.toLocation(main.getLobbyWorld());
		sendArmorStandPacketGlobalSelective(title, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Best Time");

		double y = title.getY() - 0.40;
		int rank = 1;
		for (int i = 0; i < lifetimeTopIds.size() && rank <= 10; i++, rank++) {
			UUID id = lifetimeTopIds.get(i);
			String name = (i < lifetimeTopNames.size() ? lifetimeTopNames.get(i) : "#");
			String v = lifetimeTimes.getOrDefault(id, "N/A");

			Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());
			sendArmorStandPacketGlobalSelective(line,
					ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + v);
			y -= 0.24;
		}

		// player's own lifetime line if not in top 10 — only for Lifetime viewers
		Location base = new Location(title.getWorld(), title.getX(), y, title.getZ());
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!isViewerLifetime(player)) continue;

			clearViewerHologram(player);

			PlayerData data = main.getDataManager().getPlayerData(player);
			if (data == null) continue;
			if (!lifetimeTopIds.contains(data.playerUUID)) {
				ParkourDetails details = data.playerParkour.get(arena.getId());
				String val = (details != null ? formatTime(details.totalTime) : "N/A");
				Location line1 = base.clone().add(0, -0.24, 0);
				sendStandToOnePlayerLifetimeOnly(line1, "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------", player);
				Location line2 = base.clone().add(0, -0.44, 0);
				sendStandToOnePlayerLifetimeOnly(line2, "" + ChatColor.YELLOW + player.getName() + ChatColor.RESET + " - " + val, player);
			}
		}

		// Repaint scoped selections so they don't get overwritten by the global refresh
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!isViewerLifetime(p)) {
				hideGlobalForViewer(p);
				LeaderboardScope sel = getViewerScopeOrLifetime(p);
				if (sel != LeaderboardScope.LIFETIME) {
					showToViewer(p, sel);
				} else {
					clearViewerHologram(p);
				}
			}
		}
	}

	// ---------- Per-viewer preview ----------
	public void showToViewer(Player viewer, LeaderboardScope scope) {
		if (viewer == null || !viewer.isOnline()) return;

		clearViewerHologram(viewer);

		if (scope != LeaderboardScope.LIFETIME) {
			hideGlobalForViewer(viewer);
		} else {
			return; // lifetime uses the global board
		}

		Location title = arena.getInstance().leaderboardLoc.toLocation(main.getLobbyWorld());
		sendLineToViewer(viewer, title, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Best Time");

		double y = title.getY() - 0.40;
		List<UUID> ids = topIds.getOrDefault(scope, Collections.emptyList());
		List<String> names = topNames.getOrDefault(scope, Collections.emptyList());
		Map<UUID, String> vals = topValues.getOrDefault(scope, Collections.emptyMap());

		int rank = 1;
		for (int i = 0; i < ids.size() && rank <= 10; i++, rank++) {
			UUID id = ids.get(i);
			String name = (i < names.size() ? names.get(i) : "#");
			String v = vals.getOrDefault(id, "N/A");
			Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());
			sendLineToViewer(viewer, line,
					ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + v);
			y -= 0.24;
		}

		// --- your own value, even if not in top 10 ---
		boolean youInTop = ids.contains(viewer.getUniqueId());
		if (!youInTop) {
			PlayerData data = main.getDataManager().getPlayerData(viewer);
			String yourVal = "N/A";
			if (data != null) {
				ParkourDetails details = data.playerParkour.get(arena.getId());
				if (details != null) yourVal = formatTime(details.totalTime);
			}
			Location sep = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
			sendLineToViewer(viewer, sep, "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------");
			y -= 0.24;
			Location yours = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
			sendLineToViewer(viewer, yours, ChatColor.YELLOW + viewer.getName() + ChatColor.RESET + " - " + yourVal);
		}
	}

	// ---------- Per-viewer hologram control ----------
	public void clearViewerHologram(Player viewer) {
		List<Integer> ids = viewerEntityIds.remove(viewer.getUniqueId());
		if (ids == null || ids.isEmpty()) return;
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(ids.stream().mapToInt(Integer::intValue).toArray());
		((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(destroy);
	}

	public void hideGlobalForViewer(Player viewer) {
		if (viewer == null || !viewer.isOnline() || globalEntityIds.isEmpty()) return;
		int[] ids = globalEntityIds.stream().mapToInt(Integer::intValue).toArray();
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(ids);
		((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(destroy);
	}

	public void paintFor(Player viewer, LeaderboardScope scope) {
		if (viewer == null || !viewer.isOnline()) return;
		if (scope == LeaderboardScope.LIFETIME) {
			clearViewerHologram(viewer);
			return;
		}
		clearViewerHologram(viewer);
		hideGlobalForViewer(viewer);
		showToViewer(viewer, scope);
	}

	// ---------- Packet helpers ----------
	private void sendArmorStandPacketGlobalSelective(Location loc, String customName) {
		EntityArmorStand stand = makeStand(loc, customName);
		int id = stand.getId();
		globalEntityIds.add(id);
		PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(stand);
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (isViewerLifetime(p)) {
				((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) p).getHandle().playerConnection.sendPacket(spawn);
			}
		}
	}

	private void sendStandToOnePlayerLifetimeOnly(Location loc, String customName, Player player) {
		if (!isViewerLifetime(player)) return;
		sendLineToViewer(player, loc, customName);
	}

	private void sendLineToViewer(Player viewer, Location loc, String text) {
		EntityArmorStand stand = makeStand(loc, text);
		int customId = ENTITY_ID.incrementAndGet();
		try {
			java.lang.reflect.Field idField = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.setInt(stand, customId);
		} catch (Throwable ignored) {}
		PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(stand);
		((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(spawn);
		viewerEntityIds.computeIfAbsent(viewer.getUniqueId(), k -> new ArrayList<>()).add(customId);
	}

	private EntityArmorStand makeStand(Location loc, String name) {
		EntityArmorStand armorStand = new EntityArmorStand(
				((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) loc.getWorld()).getHandle());
		armorStand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
		armorStand.setCustomName(name);
		armorStand.setCustomNameVisible(true);
		armorStand.setInvisible(true);
		armorStand.setGravity(false);
		return armorStand;
	}

	private void removeOldLeaderboards() {
		if (globalEntityIds.isEmpty()) return;
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(globalEntityIds.stream().mapToInt(Integer::intValue).toArray());
		for (Player p : Bukkit.getOnlinePlayers()) {
			((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) p).getHandle().playerConnection.sendPacket(destroy);
		}
		globalEntityIds.clear();
	}

	@Override
	public void close() {
		removeOldLeaderboards();
		lifetimeTopIds.clear();
		lifetimeTopNames.clear();
		lifetimeTimes.clear();
		for (LeaderboardScope s : LeaderboardScope.values()) {
			topIds.get(s).clear();
			topNames.get(s).clear();
			topValues.get(s).clear();
		}
	}

	// ---------- Helpers to know viewer's chosen scope ----------
	private boolean isViewerLifetime(Player p) {
		try {
			return getViewerScopeOrLifetime(p) == LeaderboardScope.LIFETIME;
		} catch (Throwable t) {
			return true;
		}
	}

	private LeaderboardScope getViewerScopeOrLifetime(Player p) {
		if (main.leaderboardScopeByViewer == null) return LeaderboardScope.LIFETIME;
		return main.leaderboardScopeByViewer.getOrDefault(p.getUniqueId(), LeaderboardScope.LIFETIME);
	}

	public String formatTime(long nanoseconds) {
		double totalSeconds = nanoseconds / 1_000_000_000.0;
		long minutes = (long) (totalSeconds / 60);
		double seconds = totalSeconds % 60;
		if (minutes > 0) {
			return String.format("%dm %.3fs", minutes, seconds);
		} else {
			return String.format("%.3fs", seconds);
		}
	}
}