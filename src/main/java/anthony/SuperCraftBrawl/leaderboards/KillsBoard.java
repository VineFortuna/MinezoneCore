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

public class KillsBoard extends LeaderboardBase {
    private final Core main;

    // Global (Lifetime, SCB - the default board) data
    private final List<UUID> lifetimeTopIds = new ArrayList<>();
    private final List<String> lifetimeTopNames = new ArrayList<>();
    private final Map<UUID, Integer> lifetimeKills = new HashMap<>();

    // Global entity IDs (destroy for everyone on global redraw)
    private final List<Integer> globalEntityIds = new ArrayList<>();

    // Scoped caches (used by per-viewer preview) - SCB stats
    private final Map<LeaderboardScope, List<UUID>> topIds = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, List<String>> topNames = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, Map<UUID, Integer>> topValues = new EnumMap<>(LeaderboardScope.class);

    // Same, but for Flag Wars stats (FWKills)
    private final Map<LeaderboardScope, List<UUID>> topIdsFW = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, List<String>> topNamesFW = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, Map<UUID, Integer>> topValuesFW = new EnumMap<>(LeaderboardScope.class);

    // Per-viewer holograms (IDs to destroy only for that viewer)
    private final Map<UUID, List<Integer>> viewerEntityIds = new HashMap<>();
    private static final AtomicInteger ENTITY_ID = new AtomicInteger(300000); // custom id base for 1.8

    // Board title location (Kills)
    private static final Location KILLS_TITLE_LOC = new Location(null, 193.5, 106.5, 685.5);

    public KillsBoard(Core main) {
        super(main);
        this.main = main;
        for (LeaderboardScope s : LeaderboardScope.values()) {
            topIds.put(s, new ArrayList<>());
            topNames.put(s, new ArrayList<>());
            topValues.put(s, new HashMap<>());
            topIdsFW.put(s, new ArrayList<>());
            topNamesFW.put(s, new ArrayList<>());
            topValuesFW.put(s, new HashMap<>());
        }
    }

    // ---------- SQL helpers ----------
    private String sqlForScope(LeaderboardScope scope, LeaderboardStatsMode mode) {
        final String col = mode == LeaderboardStatsMode.FLAG_WARS ? "FWKills" : "Kills";
        final String metric = col;
        final String label = "KillsVal";

        if (scope == LeaderboardScope.LIFETIME) {
            return "SELECT UUID, LastPlayerName, " + col + " AS " + label + ", RoleID " +
                    "FROM PlayerData " +
                    "WHERE " + col + " > 0 " +
                    "ORDER BY " + label + " DESC LIMIT 10";
        }

        final String table = mode == LeaderboardStatsMode.FLAG_WARS ? "fw_stat_snapshots" : "scb_stat_snapshots";
        java.sql.Date ps = main.snapshotDAO.startFor(scope);

        return "SELECT pd.UUID, pd.LastPlayerName, " +
                "(pd." + col + " - IFNULL(s.total_value, 0)) AS " + label + ", pd.RoleID " +
                "FROM PlayerData pd " +
                "LEFT JOIN " + table + " s " +
                "  ON s.uuid = pd.UUID AND s.metric = '" + metric + "' " +
                " AND s.period = '" + scope.name() + "' AND s.period_start = '" + ps + "' " +
                "HAVING " + label + " > 0 " +
                "ORDER BY " + label + " DESC LIMIT 10";
    }

    // ---------- Data refresh ----------
    @Override
    public void asyncUpdate() throws SQLException {
        try (Statement st = main.getDatabaseManager().getConnection().createStatement()) {
            for (LeaderboardScope scope : LeaderboardScope.values()) {
                fetchInto(st, scope, LeaderboardStatsMode.SCB, topIds, topNames, topValues);
                fetchInto(st, scope, LeaderboardStatsMode.FLAG_WARS, topIdsFW, topNamesFW, topValuesFW);
            }
        }

        // copy lifetime (SCB only) into global fields
        lifetimeTopIds.clear();
        lifetimeTopNames.clear();
        lifetimeKills.clear();
        lifetimeTopIds.addAll(topIds.getOrDefault(LeaderboardScope.LIFETIME, Collections.emptyList()));
        lifetimeTopNames.addAll(topNames.getOrDefault(LeaderboardScope.LIFETIME, Collections.emptyList()));
        lifetimeKills.putAll(topValues.getOrDefault(LeaderboardScope.LIFETIME, Collections.emptyMap()));
    }

    private void fetchInto(Statement st, LeaderboardScope scope, LeaderboardStatsMode mode,
                            Map<LeaderboardScope, List<UUID>> idsMap,
                            Map<LeaderboardScope, List<String>> namesMap,
                            Map<LeaderboardScope, Map<UUID, Integer>> valsMap) throws SQLException {
        List<UUID> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        Map<UUID, Integer> vals = new HashMap<>();

        try (ResultSet rs = st.executeQuery(sqlForScope(scope, mode))) {
            while (rs.next()) {
                String uuidStr = rs.getString("UUID");
                String name = rs.getString("LastPlayerName");
                int value = rs.getInt("KillsVal");
                if (uuidStr == null || name == null) continue;
                UUID id = UUID.fromString(uuidStr);
                ids.add(id);
                names.add(name);
                vals.put(id, value);
            }
        }

        idsMap.put(scope, ids);
        namesMap.put(scope, names);
        valsMap.put(scope, vals);
    }

    // ---------- Global render (SCB Lifetime - the default board) ----------
    @Override
    public void updateLeaderboard(boolean init) {
        removeOldLeaderboards();

        Location title = ensureWorld(KILLS_TITLE_LOC, main.getLobbyWorld());
        // Send the global title/lines ONLY to players on SCB + Lifetime (the default combination)
        sendArmorStandPacketGlobalSelective(title, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Lifetime Kills");

        double y = title.getY() - 0.40;
        int rank = 1;
        for (int i = 0; i < lifetimeTopIds.size() && rank <= 10; i++, rank++) {
            UUID id = lifetimeTopIds.get(i);
            String name = (i < lifetimeTopNames.size() ? lifetimeTopNames.get(i) : "#");
            Integer v = lifetimeKills.getOrDefault(id, 0);

            Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());
            sendArmorStandPacketGlobalSelective(line,
                    ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + v);
            y -= 0.24;
        }

        // player's own lifetime line if not in top 10 — only for default-board viewers
        Location base = new Location(title.getWorld(), title.getX(), y, title.getZ());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!usesGlobalBoard(player)) continue;

            // IMPORTANT: clear any previous per-viewer lines for default-board viewers
            clearViewerHologram(player);

            PlayerData data = main.getDataManager().getPlayerData(player);
            if (data == null) continue;
            int val = data.kills;

            if (!lifetimeTopIds.contains(data.playerUUID)) {
                Location line1 = base.clone().add(0, -0.24, 0);
                sendStandToOnePlayerGlobalOnly(line1, "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------", player);

                Location line2 = base.clone().add(0, -0.44, 0);
                sendStandToOnePlayerGlobalOnly(
                        line2,
                        "" + ChatColor.GREEN + player.getName() + ChatColor.RESET + " - " + ChatColor.WHITE + val,
                        player
                );
            }
        }

        // Repaint scoped/moded selections so they don't get overwritten by the global refresh
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!usesGlobalBoard(p)) {
                hideGlobalForViewer(p);
                showToViewer(p, getViewerScopeOrLifetime(p));
            }
        }
    }

    // ---------- Per-viewer preview (with reset line + your stats) ----------
    public void showToViewer(Player viewer, LeaderboardScope scope) {
        if (viewer == null || !viewer.isOnline()) return;

        LeaderboardStatsMode mode = getViewerMode(viewer);

        // clear previous temp lines (now also clears default-board personal lines)
        clearViewerHologram(viewer);

        if (scope == LeaderboardScope.LIFETIME && mode == LeaderboardStatsMode.SCB) {
            return; // default combination uses the global board
        }
        hideGlobalForViewer(viewer);

        Location title = ensureWorld(KILLS_TITLE_LOC, main.getLobbyWorld());
        String statLabel = "Kills";
        sendLineToViewer(viewer, title, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE
                + scope.display() + " " + statLabel);

        double y = title.getY() - 0.40;
        Map<LeaderboardScope, List<UUID>> idsSrc = mode == LeaderboardStatsMode.FLAG_WARS ? topIdsFW : topIds;
        Map<LeaderboardScope, List<String>> namesSrc = mode == LeaderboardStatsMode.FLAG_WARS ? topNamesFW : topNames;
        Map<LeaderboardScope, Map<UUID, Integer>> valuesSrc = mode == LeaderboardStatsMode.FLAG_WARS ? topValuesFW : topValues;

        List<UUID> ids = idsSrc.getOrDefault(scope, Collections.emptyList());
        List<String> names = namesSrc.getOrDefault(scope, Collections.emptyList());
        Map<UUID, Integer> vals = valuesSrc.getOrDefault(scope, Collections.emptyMap());

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

        // --- your reset line --- (space it safely to avoid Z-fighting)
        String reset = resetLine(scope);
        if (reset != null) {
            Location resetLoc = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
            sendLineToViewer(viewer, resetLoc, ChatColor.GRAY + "" + ChatColor.ITALIC + reset);
            y -= 0.24;
        }

        // --- your own value for this scope, even if not in top 10 ---
        int yourVal = getScopedKillsFor(viewer, scope, mode);
        boolean youInTop = ids.contains(viewer.getUniqueId());

        if (!youInTop) {
            Location sep = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
            sendLineToViewer(viewer, sep, "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------");

            y -= 0.24;

            Location yours = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
            sendLineToViewer(
                    viewer,
                    yours,
                    ChatColor.GREEN + viewer.getName() + ChatColor.RESET + " - " + ChatColor.WHITE + yourVal
            );
        }
    }

    // Compute the viewer's stat value for the chosen scope + mode
    private int getScopedKillsFor(Player viewer, LeaderboardScope scope, LeaderboardStatsMode mode) {
        final String column = mode == LeaderboardStatsMode.FLAG_WARS ? "FWKills" : "Kills";
        final String table = mode == LeaderboardStatsMode.FLAG_WARS ? "fw_stat_snapshots" : "scb_stat_snapshots";

        try {
            PlayerData d = main.getDataManager().getPlayerData(viewer);

            if (scope == LeaderboardScope.LIFETIME) {
                return (d != null) ? (mode == LeaderboardStatsMode.FLAG_WARS ? d.fwKills : d.kills) : 0;
            }

            int current;
            if (d != null) current = mode == LeaderboardStatsMode.FLAG_WARS ? d.fwKills : d.kills;
            else {
                try (PreparedStatement ps = main.getDatabaseManager().getConnection()
                        .prepareStatement("SELECT " + column + " FROM PlayerData WHERE UUID=? LIMIT 1")) {
                    ps.setString(1, viewer.getUniqueId().toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        current = rs.next() ? rs.getInt(1) : 0;
                    }
                }
            }

            java.sql.Date psDate = main.snapshotDAO.startFor(scope);
            int snap = 0;
            try (PreparedStatement ps = main.getDatabaseManager().getConnection().prepareStatement(
                    "SELECT total_value FROM " + table + " WHERE uuid=? AND metric=? AND period=? AND period_start=? LIMIT 1")) {
                ps.setString(1, viewer.getUniqueId().toString());
                ps.setString(2, column);
                ps.setString(3, scope.name());
                ps.setDate(4, psDate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) snap = rs.getInt(1);
                }
            }

            return Math.max(0, current - snap);
        } catch (Exception e) {
            e.printStackTrace();
            PlayerData d = main.getDataManager().getPlayerData(viewer);
            if (d == null) return 0;
            return mode == LeaderboardStatsMode.FLAG_WARS ? d.fwKills : d.kills;
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
            case MONTHLY: end.add(Calendar.MONTH, 1); break;
            default: return null;
        }

        long remaining = Math.max(0L, end.getTimeInMillis() - nowMs);

        if (scope == LeaderboardScope.DAILY) {
            int hours = (int) Math.ceil(remaining / 3600000.0);
            return "Resets in " + hours + " hours";
        } else if (scope == LeaderboardScope.WEEKLY) {
            int days = (int) Math.ceil(remaining / 86400000.0);
            return "Resets in " + days + " days";
        } else { // MONTHLY
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

    /** Paint exactly what this player should see, based on scope + their current stats-mode selection. */
    public void paintFor(Player viewer, LeaderboardScope scope) {
        if (viewer == null || !viewer.isOnline()) return;
        if (scope == LeaderboardScope.LIFETIME && getViewerMode(viewer) == LeaderboardStatsMode.SCB) {
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
            if (usesGlobalBoard(p)) {
                ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) p).getHandle().playerConnection.sendPacket(spawn);
            }
        }
    }

    /**
     * Default-board personal line -> track as per-viewer so we can destroy later.
     */
    private void sendStandToOnePlayerGlobalOnly(Location loc, String customName, Player player) {
        if (!usesGlobalBoard(player)) return;
        // Reuse the per-viewer helper so the id is tracked in viewerEntityIds
        sendLineToViewer(player, loc, customName);
    }

    /**
     * per-viewer line with custom entity id, tracked in viewerEntityIds.
     */
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
        lifetimeKills.clear();
        for (LeaderboardScope s : LeaderboardScope.values()) {
            topIds.get(s).clear();
            topNames.get(s).clear();
            topValues.get(s).clear();
            topIdsFW.get(s).clear();
            topNamesFW.get(s).clear();
            topValuesFW.get(s).clear();
        }
    }

    // ---------- Helpers to know viewer's chosen scope/mode ----------
    /** True only for the default combination (Lifetime + SCB) - the only one that uses the cheap shared global board. */
    private boolean usesGlobalBoard(Player p) {
        try {
            return getViewerScopeOrLifetime(p) == LeaderboardScope.LIFETIME && getViewerMode(p) == LeaderboardStatsMode.SCB;
        } catch (Throwable t) {
            return true;
        }
    }

    private LeaderboardScope getViewerScopeOrLifetime(Player p) {
        if (main.leaderboardScopeByViewer == null) return LeaderboardScope.LIFETIME;
        return main.leaderboardScopeByViewer.getOrDefault(p.getUniqueId(), LeaderboardScope.LIFETIME);
    }

    private LeaderboardStatsMode getViewerMode(Player p) {
        if (main.leaderboardModeByViewer == null) return LeaderboardStatsMode.SCB;
        return main.leaderboardModeByViewer.getOrDefault(p.getUniqueId(), LeaderboardStatsMode.SCB);
    }
}
