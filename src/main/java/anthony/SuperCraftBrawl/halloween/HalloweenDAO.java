package anthony.SuperCraftBrawl.halloween;

import anthony.SuperCraftBrawl.Core;

import java.sql.*;
import java.util.UUID;

public class HalloweenDAO {
    private final Core core;
    private final String TABLE = "scb_halloween_hunt";
    private final String EVENT_KEY; // e.g., "HALLOWEEN2025"

    public HalloweenDAO(Core core, String eventKey) {
        this.core = core;
        this.EVENT_KEY = eventKey;
        init();
    }

    private void init() {
        String sql =
            "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
          + "  event_key    VARCHAR(32) NOT NULL,"
          + "  uuid         VARCHAR(36) NOT NULL,"
          + "  progress     INT         NOT NULL DEFAULT 0,"
          + "  completed_at TIMESTAMP NULL DEFAULT NULL,"
          + "  PRIMARY KEY (event_key, uuid)"
          + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        try (Statement st = core.getDatabaseManager().getConnection().createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** 10-bit progress mask (0..1023). */
    public int getProgress(UUID uuid) {
        String q = "SELECT progress FROM " + TABLE + " WHERE event_key=? AND uuid=?";
        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(q)) {
            ps.setString(1, EVENT_KEY);
            ps.setString(2, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Set the bit for a basket, insert if needed, and return new mask. */
    public int markFound(UUID uuid, int basketIndex, int totalBaskets) {
        int bit = 1 << basketIndex;
        int fullMask = (1 << totalBaskets) - 1;

        String upd = "UPDATE " + TABLE + " SET progress=(progress | ?), " +
                     "completed_at = CASE WHEN (progress | ?) = ? THEN NOW() ELSE completed_at END " +
                     "WHERE event_key=? AND uuid=?";
        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(upd)) {
            ps.setInt(1, bit);
            ps.setInt(2, bit);
            ps.setInt(3, fullMask);
            ps.setString(4, EVENT_KEY);
            ps.setString(5, uuid.toString());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                String ins = "INSERT INTO " + TABLE + " (event_key, uuid, progress) VALUES (?,?,?) " +
                             "ON DUPLICATE KEY UPDATE progress=(progress|VALUES(progress))";
                try (PreparedStatement insPs = core.getDatabaseManager().getConnection().prepareStatement(ins)) {
                    insPs.setString(1, EVENT_KEY);
                    insPs.setString(2, uuid.toString());
                    insPs.setInt(3, bit);
                    insPs.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return getProgress(uuid);
    }

    public boolean hasFound(UUID uuid, int basketIndex) {
        return (getProgress(uuid) & (1 << basketIndex)) != 0;
    }

    public boolean isComplete(UUID uuid, int totalBaskets) {
        return getProgress(uuid) == ((1 << totalBaskets) - 1);
    }

    /**
     * Admin testing: reset your progress. 
     */
    public void reset(UUID uuid) {
        String sql = "UPDATE " + TABLE + " SET progress=0, completed_at=NULL WHERE event_key=? AND uuid=?";
        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(sql)) {
            ps.setString(1, EVENT_KEY);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}