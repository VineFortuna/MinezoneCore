package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.ParkourDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.parkour.Arenas;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkourBoard extends LeaderboardBase {

    private final Core main;
    private final Arenas arena;

    private final List<UUID> lifetimeTopIds = new ArrayList<>();
    private final List<String> lifetimeTopNames = new ArrayList<>();
    private final Map<UUID, Long> lifetimeTimes = new HashMap<>();

    private final List<Integer> globalEntityIds = new ArrayList<>();

    private final Map<LeaderboardScope, List<UUID>> topIds = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, List<String>> topNames = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, Map<UUID, Long>> topValues = new EnumMap<>(LeaderboardScope.class);

    private final Map<UUID, List<Integer>> viewerEntityIds = new HashMap<>();
    private static final AtomicInteger ENTITY_ID = new AtomicInteger(700000);

    public ParkourBoard(Core main, Arenas arena) {
        super(main);
        this.main = main;
        this.arena = arena;

        for (LeaderboardScope scope : LeaderboardScope.values()) {
            topIds.put(scope, new ArrayList<UUID>());
            topNames.put(scope, new ArrayList<String>());
            topValues.put(scope, new HashMap<UUID, Long>());
        }
    }

    private String sqlForScope(LeaderboardScope scope) {
        if (scope == LeaderboardScope.LIFETIME) {
            return "SELECT p.LastPlayerName, p.RoleID, parkour.TotalTime AS ParkourTime, p.UUID " +
                    "FROM PlayerData p " +
                    "JOIN PlayerParkour parkour ON p.UUID = parkour.UUID " +
                    "WHERE parkour.ParkourID = '" + arena.getId() + "' " +
                    "AND parkour.TotalTime > 0 " +
                    "ORDER BY parkour.TotalTime ASC LIMIT 10";
        }

        java.sql.Date periodStart = main.snapshotDAO.startFor(scope);

        return "SELECT p.LastPlayerName, p.RoleID, ppt.best_time AS ParkourTime, p.UUID " +
                "FROM scb_period_parkour_times ppt " +
                "JOIN PlayerData p ON p.UUID = ppt.uuid " +
                "WHERE ppt.parkour_id = '" + arena.getId() + "' " +
                "AND ppt.period = '" + scope.name() + "' " +
                "AND ppt.period_start = '" + periodStart + "' " +
                "AND ppt.best_time > 0 " +
                "ORDER BY ppt.best_time ASC LIMIT 10";
    }

    @Override
    public void asyncUpdate() throws SQLException {
        try (Statement st = main.getDatabaseManager().getConnection().createStatement()) {
            for (LeaderboardScope scope : LeaderboardScope.values()) {
                List<UUID> ids = new ArrayList<>();
                List<String> names = new ArrayList<>();
                Map<UUID, Long> vals = new HashMap<>();

                try (ResultSet rs = st.executeQuery(sqlForScope(scope))) {
                    while (rs.next()) {
                        String uuidStr = rs.getString("UUID");
                        String name = rs.getString("LastPlayerName");

                        if (uuidStr == null || name == null) {
                            continue;
                        }

                        UUID id = UUID.fromString(uuidStr);
                        ids.add(id);
                        names.add(name);
                        vals.put(id, rs.getLong("ParkourTime"));
                    }
                }

                topIds.put(scope, ids);
                topNames.put(scope, names);
                topValues.put(scope, vals);
            }
        }

        lifetimeTopIds.clear();
        lifetimeTopNames.clear();
        lifetimeTimes.clear();

        lifetimeTopIds.addAll(topIds.getOrDefault(LeaderboardScope.LIFETIME, Collections.<UUID>emptyList()));
        lifetimeTopNames.addAll(topNames.getOrDefault(LeaderboardScope.LIFETIME, Collections.<String>emptyList()));
        lifetimeTimes.putAll(topValues.getOrDefault(LeaderboardScope.LIFETIME, Collections.<UUID, Long>emptyMap()));
    }

    @Override
    public void updateLeaderboard(boolean init) {
        removeOldLeaderboards();

        Location title = arena.getInstance().leaderboardLoc.toLocation(main.getLobbyWorld());

        sendArmorStandPacketGlobalSelective(
                title,
                ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Best Time"
        );

        double y = title.getY() - 0.40;
        int rank = 1;

        for (int i = 0; i < lifetimeTopIds.size() && rank <= 10; i++) {
            UUID id = lifetimeTopIds.get(i);
            String name = i < lifetimeTopNames.size() ? lifetimeTopNames.get(i) : "#";
            long value = lifetimeTimes.getOrDefault(id, 0L);

            if (value <= 0) {
                continue;
            }

            Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());

            sendArmorStandPacketGlobalSelective(
                    line,
                    ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + formatTime(value)
            );

            y -= 0.24;
            rank++;
        }

        Location base = new Location(title.getWorld(), title.getX(), y, title.getZ());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isViewerLifetime(player)) {
                continue;
            }

            clearViewerHologram(player);

            PlayerData data = main.getDataManager().getPlayerData(player);

            if (data == null || lifetimeTopIds.contains(data.playerUUID)) {
                continue;
            }

            Location line1 = base.clone().add(0, -0.24, 0);
            sendStandToOnePlayerLifetimeOnly(
                    line1,
                    "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------",
                    player
            );

            long playerTime = getScopedValueFor(player, LeaderboardScope.LIFETIME);
            String display = playerTime > 0 ? formatTime(playerTime) : "N/A";

            Location line2 = base.clone().add(0, -0.44, 0);
            sendStandToOnePlayerLifetimeOnly(
                    line2,
                    ChatColor.GREEN + player.getName() + ChatColor.RESET + " - " + ChatColor.WHITE + display,
                    player
            );
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isViewerLifetime(player)) {
                hideGlobalForViewer(player);

                LeaderboardScope scope = getViewerScopeOrLifetime(player);

                if (scope != LeaderboardScope.LIFETIME) {
                    showToViewer(player, scope);
                } else {
                    clearViewerHologram(player);
                }
            }
        }
    }

    public void showToViewer(Player viewer, LeaderboardScope scope) {
        if (viewer == null || !viewer.isOnline()) {
            return;
        }

        clearViewerHologram(viewer);

        if (scope == LeaderboardScope.LIFETIME) {
            return;
        }

        hideGlobalForViewer(viewer);

        Location title = arena.getInstance().leaderboardLoc.toLocation(main.getLobbyWorld());

        sendLineToViewer(
                viewer,
                title,
                ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + scope.display() + " Best Time"
        );

        double y = title.getY() - 0.40;

        List<UUID> ids = topIds.getOrDefault(scope, Collections.<UUID>emptyList());
        List<String> names = topNames.getOrDefault(scope, Collections.<String>emptyList());
        Map<UUID, Long> vals = topValues.getOrDefault(scope, Collections.<UUID, Long>emptyMap());

        int rank = 1;

        for (int i = 0; i < ids.size() && rank <= 10; i++) {
            UUID id = ids.get(i);
            String name = i < names.size() ? names.get(i) : "#";
            long value = vals.getOrDefault(id, 0L);

            if (value <= 0) {
                continue;
            }

            Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());

            sendLineToViewer(
                    viewer,
                    line,
                    ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + formatTime(value)
            );

            y -= 0.24;
            rank++;
        }

        String reset = resetLine(scope);

        if (reset != null) {
            Location resetLoc = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
            sendLineToViewer(viewer, resetLoc, ChatColor.GRAY + "" + ChatColor.ITALIC + reset);
            y -= 0.24;
        }

        if (!ids.contains(viewer.getUniqueId())) {
            Location sep = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
            sendLineToViewer(viewer, sep, "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------");

            y -= 0.24;

            long playerTime = getScopedValueFor(viewer, scope);
            String display = playerTime > 0 ? formatTime(playerTime) : "N/A";

            Location yours = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
            sendLineToViewer(
                    viewer,
                    yours,
                    ChatColor.GREEN + viewer.getName() + ChatColor.RESET + " - " + ChatColor.WHITE + display
            );
        }
    }

    public void paintFor(Player viewer, LeaderboardScope scope) {
        if (viewer == null || !viewer.isOnline()) {
            return;
        }

        if (scope == LeaderboardScope.LIFETIME) {
            clearViewerHologram(viewer);
            return;
        }

        clearViewerHologram(viewer);
        hideGlobalForViewer(viewer);
        showToViewer(viewer, scope);
    }

    private long getScopedValueFor(Player viewer, LeaderboardScope scope) {
        try {
            if (scope == LeaderboardScope.LIFETIME) {
                PlayerData data = main.getDataManager().getPlayerData(viewer);

                if (data == null) {
                    return 0L;
                }

                ParkourDetails details = data.playerParkour.get(arena.getId());
                return details != null ? details.totalTime : 0L;
            }

            java.sql.Date periodStart = main.snapshotDAO.startFor(scope);

            String sql =
                    "SELECT best_time FROM scb_period_parkour_times " +
                            "WHERE uuid = '" + viewer.getUniqueId().toString() + "' " +
                            "AND parkour_id = '" + arena.getId() + "' " +
                            "AND period = '" + scope.name() + "' " +
                            "AND period_start = '" + periodStart + "' " +
                            "LIMIT 1";

            final long[] value = new long[]{0L};

            main.getDatabaseManager().executeQueryCommand(sql, rs -> {
                try {
                    if (rs.next()) {
                        value[0] = rs.getLong("best_time");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            return value[0];
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return 0L;
    }

    private String resetLine(LeaderboardScope scope) {
        if (scope == LeaderboardScope.LIFETIME) {
            return null;
        }

        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis();

        Calendar start = Calendar.getInstance();
        java.sql.Date startDate = main.snapshotDAO.startFor(scope);
        start.setTimeInMillis(startDate.getTime());

        Calendar end = (Calendar) start.clone();

        switch (scope) {
            case DAILY:
                end.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEKLY:
                end.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case MONTHLY:
                end.add(Calendar.MONTH, 1);
                break;
            default:
                return null;
        }

        long remaining = Math.max(0L, end.getTimeInMillis() - nowMs);

        if (scope == LeaderboardScope.DAILY) {
            int hours = (int) Math.ceil(remaining / 3600000.0);
            return "Resets in " + hours + " hours";
        }

        if (scope == LeaderboardScope.WEEKLY) {
            int days = (int) Math.ceil(remaining / 86400000.0);
            return "Resets in " + days + " days";
        }

        int months = monthsBetween(now, end);

        if (months <= 0) {
            months = 1;
        }

        return "Resets in " + months + " months";
    }

    private int monthsBetween(Calendar a, Calendar b) {
        int ay = a.get(Calendar.YEAR);
        int am = a.get(Calendar.MONTH);

        int by = b.get(Calendar.YEAR);
        int bm = b.get(Calendar.MONTH);

        return (by - ay) * 12 + (bm - am);
    }

    public void clearViewerHologram(Player viewer) {
        List<Integer> ids = viewerEntityIds.remove(viewer.getUniqueId());

        if (ids == null || ids.isEmpty()) {
            return;
        }

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(ids.stream().mapToInt(Integer::intValue).toArray());
        ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(destroy);
    }

    public void hideGlobalForViewer(Player viewer) {
        if (viewer == null || !viewer.isOnline() || globalEntityIds.isEmpty()) {
            return;
        }

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(globalEntityIds.stream().mapToInt(Integer::intValue).toArray());
        ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(destroy);
    }

    private void sendArmorStandPacketGlobalSelective(Location loc, String customName) {
        EntityArmorStand stand = makeStand(loc, customName);
        int id = stand.getId();

        globalEntityIds.add(id);

        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(stand);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isViewerLifetime(player)) {
                ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(spawn);
            }
        }
    }

    private void sendStandToOnePlayerLifetimeOnly(Location loc, String customName, Player player) {
        if (!isViewerLifetime(player)) {
            return;
        }

        sendLineToViewer(player, loc, customName);
    }

    private void sendLineToViewer(Player viewer, Location loc, String text) {
        EntityArmorStand stand = makeStand(loc, text);
        int customId = ENTITY_ID.incrementAndGet();

        try {
            java.lang.reflect.Field idField = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.setInt(stand, customId);
        } catch (Throwable ignored) {
        }

        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving(stand);
        ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(spawn);

        viewerEntityIds.computeIfAbsent(viewer.getUniqueId(), k -> new ArrayList<Integer>()).add(customId);
    }

    private EntityArmorStand makeStand(Location loc, String name) {
        EntityArmorStand armorStand = new EntityArmorStand(
                ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) loc.getWorld()).getHandle()
        );

        armorStand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        armorStand.setCustomName(name);
        armorStand.setCustomNameVisible(true);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);

        return armorStand;
    }

    private void removeOldLeaderboards() {
        if (globalEntityIds.isEmpty()) {
            return;
        }

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(globalEntityIds.stream().mapToInt(Integer::intValue).toArray());

        for (Player player : Bukkit.getOnlinePlayers()) {
            ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(destroy);
        }

        globalEntityIds.clear();
    }

    @Override
    public void close() {
        removeOldLeaderboards();

        lifetimeTopIds.clear();
        lifetimeTopNames.clear();
        lifetimeTimes.clear();

        for (LeaderboardScope scope : LeaderboardScope.values()) {
            topIds.get(scope).clear();
            topNames.get(scope).clear();
            topValues.get(scope).clear();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            clearViewerHologram(player);
        }
    }

    private boolean isViewerLifetime(Player player) {
        try {
            return getViewerScopeOrLifetime(player) == LeaderboardScope.LIFETIME;
        } catch (Throwable t) {
            return true;
        }
    }

    private LeaderboardScope getViewerScopeOrLifetime(Player player) {
        if (main.leaderboardScopeByViewer == null) {
            return LeaderboardScope.LIFETIME;
        }

        return main.leaderboardScopeByViewer.getOrDefault(player.getUniqueId(), LeaderboardScope.LIFETIME);
    }

    public String formatTime(long nanoseconds) {
        if (nanoseconds <= 0) {
            return "";
        }

        double totalSeconds = nanoseconds / 1_000_000_000.0;
        long minutes = (long) (totalSeconds / 60);
        double seconds = totalSeconds % 60;

        if (minutes > 0) {
            return String.format("%dm %.3fs", minutes, seconds);
        }

        return String.format("%.3fs", seconds);
    }
}