package anthony.SuperCraftBrawl.christmas;

import anthony.SuperCraftBrawl.Core;

import java.sql.*;
import java.time.*;
import java.util.UUID;

public class ChristmasRewardDAO {

    private final Core core;
    private final String TABLE = "scb_christmas_rewards";

    public ChristmasRewardDAO(Core core) {
        this.core = core;
        init();
    }

    private void init() {
        String sql =
                "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                        + "  uuid         VARCHAR(36) NOT NULL,"
                        + "  reward_day   INT NOT NULL,"
                        + "  claimed_at   TIMESTAMP NOT NULL,"
                        + "  PRIMARY KEY (uuid, reward_day)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

        try (Statement st = core.getDatabaseManager().getConnection().createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Returns true if the user already claimed this reward. */
    public boolean hasClaimed(UUID uuid, int rewardDay) {
        String sql = "SELECT 1 FROM " + TABLE + " WHERE uuid=? AND reward_day=?";

        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, rewardDay);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Marks a reward as claimed. */
    public void claim(UUID uuid, int rewardDay) {
        String sql =
                "INSERT INTO " + TABLE + " (uuid, reward_day, claimed_at) VALUES (?,?,?) "
                        + "ON DUPLICATE KEY UPDATE claimed_at=VALUES(claimed_at)";

        try (PreparedStatement ps = core.getDatabaseManager().getConnection().prepareStatement(sql)) {

            ZonedDateTime est = ZonedDateTime.now(ZoneId.of("America/New_York"));

            ps.setString(1, uuid.toString());
            ps.setInt(2, rewardDay);
            ps.setTimestamp(3, Timestamp.valueOf(est.toLocalDateTime()));

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
