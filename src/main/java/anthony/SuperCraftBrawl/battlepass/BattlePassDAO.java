package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Battle Pass tables, stored in the main MySQL database (same connection as PlayerData).
 * Every row is keyed by season, so a new season starts fresh without wiping the old one.
 */
public class BattlePassDAO {

	private static final String PROGRESS_TABLE = "scb_battlepass_progress";
	private static final String CLAIMS_TABLE = "scb_battlepass_claims";
	private static final String UNLOCKS_TABLE = "scb_battlepass_unlocks";

	private final Core main;

	public BattlePassDAO(Core main) {
		this.main = main;
	}

	private Connection connection() throws SQLException {
		Connection c = main.getDatabaseManager().getConnection();
		if (c == null)
			throw new SQLException("MySQL is not connected");
		return c;
	}

	public void ensureTables() throws SQLException {
		String progress =
				"CREATE TABLE IF NOT EXISTS " + PROGRESS_TABLE + " (" +
						"  uuid       VARCHAR(36) NOT NULL," +
						"  season     VARCHAR(32) NOT NULL," +
						"  xp         INT NOT NULL DEFAULT 0," +
						"  premium    TINYINT(1) NOT NULL DEFAULT 0," +
						"  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
						"  PRIMARY KEY (uuid, season)," +
						"  INDEX (season, xp)" +
						") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

		String claims =
				"CREATE TABLE IF NOT EXISTS " + CLAIMS_TABLE + " (" +
						"  uuid       VARCHAR(36) NOT NULL," +
						"  season     VARCHAR(32) NOT NULL," +
						"  track      ENUM('FREE','PREMIUM') NOT NULL," +
						"  tier       INT NOT NULL," +
						"  claimed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
						"  PRIMARY KEY (uuid, season, track, tier)" +
						") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

		// Not keyed by season for lookups: cosmetics from the pass are kept forever.
		// The season column only records where it came from (and lets a reset remove it).
		String unlocks =
				"CREATE TABLE IF NOT EXISTS " + UNLOCKS_TABLE + " (" +
						"  uuid         VARCHAR(36) NOT NULL," +
						"  cosmetic_key VARCHAR(64) NOT NULL," +
						"  season       VARCHAR(32) NOT NULL," +
						"  unlocked_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
						"  PRIMARY KEY (uuid, cosmetic_key)" +
						") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

		try (Statement st = connection().createStatement()) {
			st.execute(progress);
			st.execute(claims);
			st.execute(unlocks);
		}
	}

	/** Every cosmetic key this player has unlocked from the pass, in any season. */
	public Set<String> loadUnlocks(UUID uuid) throws SQLException {
		Set<String> keys = new HashSet<>();
		try (PreparedStatement ps = connection().prepareStatement(
				"SELECT cosmetic_key FROM " + UNLOCKS_TABLE + " WHERE uuid=?")) {
			ps.setString(1, uuid.toString());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					keys.add(rs.getString("cosmetic_key"));
			}
		}
		return keys;
	}

	public void insertUnlock(UUID uuid, String season, String cosmeticKey) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"INSERT IGNORE INTO " + UNLOCKS_TABLE + " (uuid, cosmetic_key, season) VALUES (?,?,?)")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, cosmeticKey);
			ps.setString(3, season);
			ps.executeUpdate();
		}
	}

	/** Returns the player's progress for the season, or a fresh empty one if they have none yet. */
	public BattlePassProgress load(UUID uuid, String season) throws SQLException {
		BattlePassProgress progress = new BattlePassProgress();

		try (PreparedStatement ps = connection().prepareStatement(
				"SELECT xp, premium FROM " + PROGRESS_TABLE + " WHERE uuid=? AND season=?")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, season);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					progress.xp = rs.getInt("xp");
					progress.premium = rs.getInt("premium") == 1;
				}
			}
		}

		try (PreparedStatement ps = connection().prepareStatement(
				"SELECT track, tier FROM " + CLAIMS_TABLE + " WHERE uuid=? AND season=?")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, season);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					BattlePassTrack track = BattlePassTrack.valueOf(rs.getString("track"));
					progress.getClaimed(track).add(rs.getInt("tier"));
				}
			}
		}

		return progress;
	}

	public void saveProgress(UUID uuid, String season, int xp, boolean premium) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"INSERT INTO " + PROGRESS_TABLE + " (uuid, season, xp, premium) VALUES (?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE xp=VALUES(xp), premium=VALUES(premium)")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, season);
			ps.setInt(3, xp);
			ps.setInt(4, premium ? 1 : 0);
			ps.executeUpdate();
		}
	}

	public void insertClaim(UUID uuid, String season, BattlePassTrack track, int tier) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"INSERT IGNORE INTO " + CLAIMS_TABLE + " (uuid, season, track, tier) VALUES (?,?,?,?)")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, season);
			ps.setString(3, track.name());
			ps.setInt(4, tier);
			ps.executeUpdate();
		}
	}

	/** Wipes a player's progress and claims for one season (admin/testing). */
	public void reset(UUID uuid, String season) throws SQLException {
		try (PreparedStatement a = connection().prepareStatement(
				"DELETE FROM " + PROGRESS_TABLE + " WHERE uuid=? AND season=?");
		     PreparedStatement b = connection().prepareStatement(
				     "DELETE FROM " + CLAIMS_TABLE + " WHERE uuid=? AND season=?");
		     PreparedStatement c = connection().prepareStatement(
				     "DELETE FROM " + UNLOCKS_TABLE + " WHERE uuid=? AND season=?")) {
			a.setString(1, uuid.toString());
			a.setString(2, season);
			a.executeUpdate();
			b.setString(1, uuid.toString());
			b.setString(2, season);
			b.executeUpdate();
			c.setString(1, uuid.toString());
			c.setString(2, season);
			c.executeUpdate();
		}
	}
}
