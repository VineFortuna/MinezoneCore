package anthony.SuperCraftBrawl.christmas;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.battlepass.BattlePassManager;

import java.sql.*;
import java.util.UUID;

public class ChristmasHuntDAO {
    private final Core core;
    private final String eventKey; // e.g. "CHRISTMAS2026"
    private final int totalPresents;

    public ChristmasHuntDAO(Core core, String eventKey, int totalPresents) {
        this.core = core;
        this.eventKey = eventKey;
        this.totalPresents = totalPresents;
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS scb_christmas_hunt ("
                + "event_key VARCHAR(32) NOT NULL,"
                + "uuid VARCHAR(36) NOT NULL,"
                + "progress INT NOT NULL DEFAULT 0,"
                + "PRIMARY KEY (event_key, uuid))";

        try (Statement st = core.getDatabaseManager().getConnection().createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Each bit is one present. Bit 0 = present 0, etc.
    public int getProgress(UUID uuid) {
        String sql = "SELECT progress FROM scb_christmas_hunt WHERE event_key = ? AND uuid = ?";

        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(sql)) {
            ps.setString(1, eventKey);
            ps.setString(2, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int markFound(UUID uuid, int presentIndex) {
        int bit = 1 << presentIndex;
        String sql = "INSERT INTO scb_christmas_hunt (event_key, uuid, progress) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE progress = progress | ?";

        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(sql)) {
            ps.setString(1, eventKey);
            ps.setString(2, uuid.toString());
            ps.setInt(3, bit);
            ps.setInt(4, bit);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getProgress(uuid);
    }

    public boolean hasFound(UUID uuid, int presentIndex) {
        return (getProgress(uuid) & (1 << presentIndex)) != 0;
    }

    public boolean isComplete(UUID uuid) {
        int allFound = (1 << totalPresents) - 1;
        return getProgress(uuid) == allFound;
    }

    // For admin testing
    public void reset(UUID uuid) {
        String sql = "DELETE FROM scb_christmas_hunt WHERE event_key = ? AND uuid = ?";

        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(sql)) {
            ps.setString(1, eventKey);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}