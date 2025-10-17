package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import java.sql.*;
import java.util.Calendar;

public class StatSnapshotDAO {
    private final Core core;

    public StatSnapshotDAO(Core core) {
        this.core = core;
        // table is created in DatabaseManager.ensureSnapshotTable() (Step 1)
    }

    /** Start-of-day (local server time) */
    public java.sql.Date todayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    /** Start-of-week (Monday) */
    public java.sql.Date weekStartMonday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Normalize to Monday:
        int day = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday ... 7=Saturday
        int delta = (day == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - day);
        cal.add(Calendar.DAY_OF_MONTH, delta);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    /** Start-of-month (1st) */
    public java.sql.Date monthStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    public java.sql.Date startFor(LeaderboardScope scope) {
        switch (scope) {
            case DAILY:   return todayStart();
            case WEEKLY:  return weekStartMonday();
            case MONTHLY: return monthStart();
            default:      return null; // LIFETIME has no period start
        }
    }

    /**
     * Ensure a snapshot exists for this player/metric for the current period.
     * If missing, insert one with the player's current total so period math works: (current - snapshot).
     */
    public void ensureSnapshotForPlayer(String uuid, String metric, LeaderboardScope scope, int currentTotal) {
        if (scope == LeaderboardScope.LIFETIME) return;

        java.sql.Date periodStart = startFor(scope);
        final String checkSql =
                "SELECT total_value FROM scb_stat_snapshots " +
                        "WHERE metric='" + metric + "' AND uuid='" + uuid + "' " +
                        "AND period='" + scope.name() + "' AND period_start='" + periodStart + "'";

        core.getDatabaseManager().executeQueryCommand(checkSql, rs -> {
            try {
                boolean exists = rs.next();  // <-- can throw SQLException
                if (!exists) {
                    final String ins =
                            "INSERT INTO scb_stat_snapshots(metric, uuid, period, period_start, total_value) VALUES (" +
                                    "'" + metric + "','" + uuid + "','" + scope.name() + "','" + periodStart + "'," + currentTotal + ")";
                    core.getDatabaseManager().executeUpdateCommand(ins);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Do NOT rs.close() here; DatabaseManager closes it after func.execute(...)
        });
    }

    /**
     * Bulk ensure snapshots for ALL players for the current period using current totals.
     * `playerDataColumn` is the column name in PlayerData (e.g., Wins, Kills, FlawlessWins, TotalCaught).
     */
    public void ensureSnapshotsForAll(String metric, LeaderboardScope scope, String playerDataColumn) {
        if (scope == LeaderboardScope.LIFETIME) return;

        java.sql.Date periodStart = startFor(scope);
        final String sql =
                "INSERT IGNORE INTO scb_stat_snapshots(metric, uuid, period, period_start, total_value) " +
                        "SELECT '" + metric + "', pd.UUID, '" + scope.name() + "', '" + periodStart + "', pd." + playerDataColumn + " " +
                        "FROM PlayerData pd";

        core.getDatabaseManager().executeUpdateCommand(sql);
    }
}