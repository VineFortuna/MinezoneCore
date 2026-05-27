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

import java.sql.PreparedStatement;
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

public class WinstreakBoard extends LeaderboardBase {

    private final Core main;

    private final List<UUID> lifetimeTopIds = new ArrayList<>();
    private final List<String> lifetimeTopNames = new ArrayList<>();
    private final Map<UUID, Integer> lifetimeValues = new HashMap<>();

    private final List<Integer> globalEntityIds = new ArrayList<>();

    private final Map<LeaderboardScope, List<UUID>> topIds = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, List<String>> topNames = new EnumMap<>(LeaderboardScope.class);
    private final Map<LeaderboardScope, Map<UUID, Integer>> topValues = new EnumMap<>(LeaderboardScope.class);

    private final Map<UUID, List<Integer>> viewerEntityIds = new HashMap<>();
    private static final AtomicInteger ENTITY_ID = new AtomicInteger(600000);

    private static final Location TITLE_LOC = new Location(null, 209.5, 107.5, 704.4);

    public WinstreakBoard(Core main) {
        super(main);
        this.main = main;

        for (LeaderboardScope scope : LeaderboardScope.values()) {
            topIds.put(scope, new ArrayList<UUID>());
            topNames.put(scope, new ArrayList<String>());
            topValues.put(scope, new HashMap<UUID, Integer>());
        }
    }

    private String sqlForScope(LeaderboardScope scope) {
        if (scope == LeaderboardScope.LIFETIME) {
            return "SELECT UUID, LastPlayerName, BestWinstreak AS WinstreakVal, RoleID " +
                    "FROM PlayerData " +
                    "WHERE BestWinstreak > 0 " +
                    "ORDER BY WinstreakVal DESC LIMIT 10";
        }

        java.sql.Date periodStart = main.snapshotDAO.startFor(scope);

        return "SELECT pd.UUID, pd.LastPlayerName, IFNULL(ws.best_value, 0) AS WinstreakVal, pd.RoleID " +
                "FROM PlayerData pd " +
                "LEFT JOIN scb_period_winstreaks ws " +
                "ON ws.uuid = pd.UUID " +
                "AND ws.period = '" + scope.name() + "' " +
                "AND ws.period_start = '" + periodStart + "' " +
                "HAVING WinstreakVal > 0 " +
                "ORDER BY WinstreakVal DESC LIMIT 10";
    }

    @Override
    public void asyncUpdate() throws SQLException {
        try (Statement st = main.getDatabaseManager().getConnection().createStatement()) {
            for (LeaderboardScope scope : LeaderboardScope.values()) {
                List<UUID> ids = new ArrayList<>();
                List<String> names = new ArrayList<>();
                Map<UUID, Integer> vals = new HashMap<>();

                try (ResultSet rs = st.executeQuery(sqlForScope(scope))) {
                    while (rs.next()) {
                        String uuid = rs.getString("UUID");
                        String name = rs.getString("LastPlayerName");

                        if (uuid == null || name == null) {
                            continue;
                        }

                        UUID id = UUID.fromString(uuid);
                        ids.add(id);
                        names.add(name);
                        vals.put(id, rs.getInt("WinstreakVal"));
                    }
                }

                topIds.put(scope, ids);
                topNames.put(scope, names);
                topValues.put(scope, vals);
            }
        }

        lifetimeTopIds.clear();
        lifetimeTopNames.clear();
        lifetimeValues.clear();

        lifetimeTopIds.addAll(topIds.getOrDefault(LeaderboardScope.LIFETIME, Collections.<UUID>emptyList()));
        lifetimeTopNames.addAll(topNames.getOrDefault(LeaderboardScope.LIFETIME, Collections.<String>emptyList()));
        lifetimeValues.putAll(topValues.getOrDefault(LeaderboardScope.LIFETIME, Collections.<UUID, Integer>emptyMap()));
    }

    @Override
    public void updateLeaderboard(boolean init) {
        removeOldLeaderboards();

        Location title = ensureWorld(TITLE_LOC);
        sendArmorStandPacketGlobalSelective(
                title,
                ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Best Winstreak"
        );

        double y = title.getY() - 0.40;
        int rank = 1;

        for (int i = 0; i < lifetimeTopIds.size() && rank <= 10; i++, rank++) {
            UUID id = lifetimeTopIds.get(i);
            String name = i < lifetimeTopNames.size() ? lifetimeTopNames.get(i) : "#";
            int value = lifetimeValues.getOrDefault(id, 0);

            Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());

            sendArmorStandPacketGlobalSelective(
                    line,
                    ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + value
            );

            y -= 0.24;
        }

        Location base = new Location(title.getWorld(), title.getX(), y, title.getZ());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isViewerLifetime(player)) {
                continue;
            }

            clearViewerHologram(player);

            PlayerData data = main.getDataManager().getPlayerData(player);

            if (data != null && !lifetimeTopIds.contains(data.playerUUID)) {
                Location line1 = base.clone().add(0, -0.24, 0);
                sendLineToViewer(player, line1, "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + "-----------------");

                Location line2 = base.clone().add(0, -0.44, 0);
                sendLineToViewer(
                        player,
                        line2,
                        ChatColor.GREEN + player.getName() + ChatColor.RESET + " - " + ChatColor.WHITE + data.bestWinstreak
                );
            }
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

        Location title = ensureWorld(TITLE_LOC);

        sendLineToViewer(
                viewer,
                title,
                ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + scope.display() + " Winstreak"
        );

        double y = title.getY() - 0.40;

        List<UUID> ids = topIds.getOrDefault(scope, Collections.<UUID>emptyList());
        List<String> names = topNames.getOrDefault(scope, Collections.<String>emptyList());
        Map<UUID, Integer> vals = topValues.getOrDefault(scope, Collections.<UUID, Integer>emptyMap());

        int rank = 1;

        for (int i = 0; i < ids.size() && rank <= 10; i++, rank++) {
            UUID id = ids.get(i);
            String name = i < names.size() ? names.get(i) : "#";
            int value = vals.getOrDefault(id, 0);

            Location line = new Location(title.getWorld(), title.getX(), y, title.getZ());

            sendLineToViewer(
                    viewer,
                    line,
                    ChatColor.AQUA + "#" + rank + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + value
            );

            y -= 0.24;
        }

        String reset = resetLine(scope);

        if (reset != null) {
            Location resetLoc = new Location(title.getWorld(), title.getX(), y - 0.20, title.getZ());
            sendLineToViewer(viewer, resetLoc, ChatColor.GRAY + "" + ChatColor.ITALIC + reset);
            y -= 0.24;
        }

        int yourVal = getScopedValueFor(viewer, scope);

        if (!ids.contains(viewer.getUniqueId())) {
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

    private int getScopedValueFor(Player viewer, LeaderboardScope scope) {
        try {
            if (scope == LeaderboardScope.LIFETIME) {
                PlayerData data = main.getDataManager().getPlayerData(viewer);
                return data != null ? data.bestWinstreak : 0;
            }

            java.sql.Date periodStart = main.snapshotDAO.startFor(scope);

            try (PreparedStatement ps = main.getDatabaseManager().getConnection().prepareStatement(
                    "SELECT best_value FROM scb_period_winstreaks WHERE uuid=? AND period=? AND period_start=? LIMIT 1"
            )) {
                ps.setString(1, viewer.getUniqueId().toString());
                ps.setString(2, scope.name());
                ps.setDate(3, periodStart);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
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
        Location fixed = ensureWorld(loc);

        EntityArmorStand armorStand = new EntityArmorStand(
                ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) fixed.getWorld()).getHandle()
        );

        armorStand.setLocation(fixed.getX(), fixed.getY(), fixed.getZ(), 0, 0);
        armorStand.setCustomName(name);
        armorStand.setCustomNameVisible(true);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);

        return armorStand;
    }

    private Location ensureWorld(Location base) {
        if (base.getWorld() == null && main.getLobbyWorld() != null) {
            return new Location(main.getLobbyWorld(), base.getX(), base.getY(), base.getZ(), base.getYaw(), base.getPitch());
        }

        return base;
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
        lifetimeValues.clear();

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
        return getViewerScopeOrLifetime(player) == LeaderboardScope.LIFETIME;
    }

    private LeaderboardScope getViewerScopeOrLifetime(Player player) {
        if (main.leaderboardScopeByViewer == null) {
            return LeaderboardScope.LIFETIME;
        }

        return main.leaderboardScopeByViewer.getOrDefault(player.getUniqueId(), LeaderboardScope.LIFETIME);
    }
}