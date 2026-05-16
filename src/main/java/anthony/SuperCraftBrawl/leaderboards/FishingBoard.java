package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
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

public class FishingBoard extends LeaderboardBase {
	private final Core main;

	// Global (Lifetime) data
	private final List<UUID> lifetimeTopIds = new ArrayList<>();
	private final List<String> lifetimeTopNames = new ArrayList<>();
	private final Map<UUID, Integer> lifetimeCaught = new HashMap<>();

	// Global entity IDs (destroy for everyone on global redraw)
	private final List<Integer> globalEntityIds = new ArrayList<>();

	// Scoped caches (used by per-viewer preview)
	private final Map<LeaderboardScope, List<UUID>> topIds = new EnumMap<>(LeaderboardScope.class);
	private final Map<LeaderboardScope, List<String>> topNames = new EnumMap<>(LeaderboardScope.class);
	private final Map<LeaderboardScope, Map<UUID, Integer>> topValues = new EnumMap<>(LeaderboardScope.class);

	// Per-viewer holograms (IDs to destroy only for that viewer)
	private final Map<UUID, List<Integer>> viewerEntityIds = new HashMap<>();
	private static final AtomicInteger ENTITY_ID = new AtomicInteger(600000); // custom id base, offset from other boards

	// Board title location (Fishing)
	private static final Location FISHING_TITLE_LOC = new Location(null, 305.5, 93.5, 531.5);

	public FishingBoard(Core main) {
		super(main);
		this.main = main;
		for (LeaderboardScope s : LeaderboardScope.values()) {
			topIds.put(s, new ArrayList<>());
			topNames.put(s, new ArrayList<>());
			topValues.put(s, new HashMap<>());
		}
	}

	// ---------- SQL helpers ----------
	private String sqlForScope(LeaderboardScope scope) {
		final String col = "TotalCaught";
		final String metric = "TotalCaught";
		final String label = "CaughtVal";

		if (scope == LeaderboardScope.LIFETIME) {
			return "SELECT UUID, LastPlayerName, " + col + " AS " + label + ", RoleID " +
					"FROM PlayerData ORDER BY " + label + " DESC LIMIT 10";
		}

		java.sql.Date ps = main.snapshotDAO.startFor(scope);

		return "SELECT pd.UUID, pd.LastPlayerName, " +
				"(pd." + col + " - IFNULL(s.total_value, 0)) AS " + label + ", pd.RoleID " +
				"FROM PlayerData pd " +
				"LEFT JOIN scb_stat_snapshots s " +
				"  ON s.uuid = pd.UUID AND s.metric = '" + metric + "' " +
				" AND s.period = '" + scope.name() + "' AND s.period_start = '" + ps + "' " +
				"ORDER BY " + label + " DESC LIMIT 10";
	}

	// ---------- Data refresh ----------
	@Override
	public void asyncUpdate() throws SQLException {
		try (Statement st = main.getDatabaseManager().getConnection().createStatement()) {
			for (LeaderboardScope scope : LeaderboardScope.values()) {
				List<UUID> ids = new ArrayList<>();
				List<String> names = new ArrayList<>();
				Map<UUID, Integer> vals = new HashMap<>();

				try (ResultSet rs = st.executeQuery(sqlForScope(scope))) {
					while (rs.next()) {
						String uuidStr = rs.getString("UUID");
						String name = rs.getString("LastPlayerName");
						int value = rs.getInt("CaughtVal");
						if (uuidStr == null || name == null) continue;
						UUID id = UUID.fromString(uuidStr);
						ids.add(id);
						names.add(name);
						vals.put(id, value);
					}
				}

				topIds.put(scope, ids);
				topNames.put(scope, names);
				topValues.put(scope, vals);
			}
		}

		// copy lifetime into global fields
		lifetimeTopIds.clear();
		lifetimeTopNames.clear();
		lifetimeCaught.clear();
		lifetimeTopIds.addAll(topIds.getOrDefault(LeaderboardScope.LIFETIME, Collections.emptyList()));
		lifetimeTopNames.addAll(topNames.getOrDefault(LeaderboardScope.LIFETIME, Collections.emptyList()));
		lifetimeCaught.putAll(topValues.getOrDefault(LeaderboardScope.LIFETIME, Collections.emptyMap()));
	}

	// ---------- Global render (Lifetime for everyone who DIDN'T pick a scoped view) ----------
	@Override
	public void updateLeaderboard(boolean init) {
		removeOldLeaderboards();

		Location title = ensureWorld(FISHING_TITLE_LOC, main.getLobbyWorld());
		sendArmorStandPacketGlobalSelective(title, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Lifetime Caught");

		double y = title.getY() - 0.40;
		int rank = 1;
		for (int i = 0; i < lifetimeTopIds.size() && rank <= 10; i++, rank++) {
			UUID id = lifetimeTopIds.get(i);
			String name = (i < lifetimeTopNames.size() ? lifetimeTopNames.get(i) : "#");
			Integer v = lifetimeCaught.getOrDefault(id, 0);

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
				int val = data.totalcaught;
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

	// ---------- Per-viewer preview (with reset line + your stats) ----------
	public void showToViewer(Player viewer, LeaderboardScope scope) {
		if (viewer == null || !viewer.isOnline()) return;

		clearViewerHologram(viewer);

		if (scope != LeaderboardScope.LIFETIME) {
			hideGlobalForViewer(viewer);
		} else {
			return; // lifetime uses the global board
		}

		Location title = ensureWorld(FISHING_TITLE_LOC, main.getLobbyWorld());
		sendLineToViewer(viewer, title, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE
				+ scope.display() + " Caught");

		double y = title.getY() - 0.40;
		List<UUID> ids = topIds.getOrDefault(scope, Collections.emptyList());
		List<String> names = topNames.getOrDefault(scope, Collections.emptyList());
		Map<UUID, Integer> vals = topValues.getOrDefault(scope, Collections.emptyMap());

		int rank = 1;
		for (int i = 0; i < ids.size() && rank <= 10; i++, rank++) {
			UUID id = ids.get(i);
			String name = (i < names.size() ? names.get(i) : "#");
			Integer v = vals.getOrDefault(id, 0);
			Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());
			sendLineToViewer(viewer, line,
					ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + v);
			y -= 0.24;
		}

		// --- reset line ---
		String reset = resetLine(scope);
		if (reset != null) {
			Location resetLoc = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
			sendLineToViewer(viewer, resetLoc, ChatColor.GRAY + "" + ChatColor.ITALIC + reset);
			y -= 0.24;
		}

		// --- your own value for this scope, even if not in top 10 ---
		int yourVal = getScopedCaughtFor(viewer, scope);
		boolean youInTop = ids.contains(viewer.getUniqueId());
		if (!youInTop) {
			Location sep = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
			sendLineToViewer(viewer, sep, "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------");
			y -= 0.24;
			Location yours = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
			sendLineToViewer(viewer, yours, ChatColor.YELLOW + viewer.getName() + ChatColor.RESET + " - " + yourVal);
		}
	}

	// Compute the viewer's stat value for the chosen scope
	private int getScopedCaughtFor(Player viewer, LeaderboardScope scope) {
		try {
			if (scope == LeaderboardScope.LIFETIME) {
				PlayerData d = main.getDataManager().getPlayerData(viewer);
				return (d != null) ? d.totalcaught : 0;
			}
			int current = 0;
			PlayerData d = main.getDataManager().getPlayerData(viewer);
			if (d != null) current = d.totalcaught;
			else {
				try (PreparedStatement ps = main.getDatabaseManager().getConnection()
						.prepareStatement("SELECT TotalCaught FROM PlayerData WHERE UUID=? LIMIT 1")) {
					ps.setString(1, viewer.getUniqueId().toString());
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) current = rs.getInt(1);
					}
				}
			}

			java.sql.Date psDate = main.snapshotDAO.startFor(scope);
			int snap = 0;
			try (PreparedStatement ps = main.getDatabaseManager().getConnection().prepareStatement(
					"SELECT total_value FROM scb_stat_snapshots WHERE uuid=? AND metric='TotalCaught' AND period=? AND period_start=? LIMIT 1")) {
				ps.setString(1, viewer.getUniqueId().toString());
				ps.setString(2, scope.name());
				ps.setDate(3, psDate);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) snap = rs.getInt(1);
				}
			}

			return Math.max(0, current - snap);
		} catch (Exception e) {
			e.printStackTrace();
			PlayerData d = main.getDataManager().getPlayerData(viewer);
			return (d != null) ? d.totalcaught : 0;
		}
	}

	// Build the reset line text for the scope
	private String resetLine(LeaderboardScope scope) {
		if (scope == LeaderboardScope.LIFETIME) return null;

		Calendar now = Calendar.getInstance();
		long nowMs = now.getTimeInMillis();

		Calendar start = Calendar.getInstance();
		java.sql.Date startDate = main.snapshotDAO.startFor(scope);
		start.setTimeInMillis(startDate.getTime());

		Calendar end = (Calendar) start.clone();
		switch (scope) {
			case DAILY:   end.add(Calendar.DAY_OF_MONTH, 1); break;
			case WEEKLY:  end.add(Calendar.DAY_OF_MONTH, 7); break;
			case MONTHLY: end.add(Calendar.MONTH, 1);        break;
			default: return null;
		}

		long remaining = Math.max(0L, end.getTimeInMillis() - nowMs);

		if (scope == LeaderboardScope.DAILY) {
			int hours = (int) Math.ceil(remaining / 3600000.0);
			return "Resets in " + hours + " hours";
		} else if (scope == LeaderboardScope.WEEKLY) {
			int days = (int) Math.ceil(remaining / 86400000.0);
			return "Resets in " + days + " days";
		} else {
			int months = monthsBetween(now, end);
			if (months <= 0) months = 1;
			return "Resets in " + months + " months";
		}
	}

	private int monthsBetween(Calendar a, Calendar b) {
		int ay = a.get(Calendar.YEAR), am = a.get(Calendar.MONTH);
		int by = b.get(Calendar.YEAR), bm = b.get(Calendar.MONTH);
		return (by - ay) * 12 + (bm - am);
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
		Location fixed = ensureWorld(loc, main.getLobbyWorld());
		EntityArmorStand armorStand = new EntityArmorStand(
				((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) fixed.getWorld()).getHandle());
		armorStand.setLocation(fixed.getX(), fixed.getY(), fixed.getZ(), 0, 0);
		armorStand.setCustomName(name);
		armorStand.setCustomNameVisible(true);
		armorStand.setInvisible(true);
		armorStand.setGravity(false);
		return armorStand;
	}

	private Location ensureWorld(Location base, org.bukkit.World world) {
		if (base.getWorld() == null && world != null) {
			return new Location(world, base.getX(), base.getY(), base.getZ(), base.getYaw(), base.getPitch());
		}
		return base;
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
		lifetimeCaught.clear();
		for (LeaderboardScope s : LeaderboardScope.values()) {
			topIds.get(s).clear();
			topNames.get(s).clear();
			topValues.get(s).clear();
		}
	}

	// ---------- Helpers to know viewer's chosen scope ----------
	private boolean isViewerLifetime(Player p) {
		try {
			LeaderboardScope sel = getViewerScopeOrLifetime(p);
			return sel == LeaderboardScope.LIFETIME;
		} catch (Throwable t) {
			return true;
		}
	}

	private LeaderboardScope getViewerScopeOrLifetime(Player p) {
		if (main.leaderboardScopeByViewer == null) return LeaderboardScope.LIFETIME;
		return main.leaderboardScopeByViewer.getOrDefault(p.getUniqueId(), LeaderboardScope.LIFETIME);
	}
}