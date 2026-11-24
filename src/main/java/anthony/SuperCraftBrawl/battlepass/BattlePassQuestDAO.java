package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Quest progress, stored in the main MySQL database next to the rest of the Battle Pass.
 * One row per player, season and quest. Kept apart from BattlePassDAO so a problem with this table
 * can only switch the quests off, never the whole pass.
 */
public class BattlePassQuestDAO {

	private static final String TABLE = "scb_battlepass_quest_progress";

	private final Core main;

	public BattlePassQuestDAO(Core main) {
		this.main = main;
	}

	private Connection connection() throws SQLException {
		Connection c = main.getDatabaseManager().getConnection();
		if (c == null)
			throw new SQLException("MySQL is not connected");
		return c;
	}

	public void ensureTable() throws SQLException {
		String sql =
				"CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
						"  uuid       VARCHAR(36) NOT NULL," +
						"  season     VARCHAR(32) NOT NULL," +
						"  quest_id   VARCHAR(32) NOT NULL," +
						"  progress   INT NOT NULL DEFAULT 0," +
						"  completed  TINYINT(1) NOT NULL DEFAULT 0," +
						"  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
						"  PRIMARY KEY (uuid, season, quest_id)" +
						") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

		try (Statement st = connection().createStatement()) {
			st.execute(sql);
		}
	}

	/** Quest id to {progress, completed (0 or 1)} for the season. Quests they never touched have no entry. */
	public Map<String, int[]> load(UUID uuid, String season) throws SQLException {
		Map<String, int[]> rows = new HashMap<>();
		try (PreparedStatement ps = connection().prepareStatement(
				"SELECT quest_id, progress, completed FROM " + TABLE + " WHERE uuid=? AND season=?")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, season);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					rows.put(rs.getString("quest_id"), new int[]{rs.getInt("progress"), rs.getInt("completed")});
			}
		}
		return rows;
	}

	public void save(UUID uuid, String season, String questId, int progress, boolean completed) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"INSERT INTO " + TABLE + " (uuid, season, quest_id, progress, completed) VALUES (?,?,?,?,?) " +
						"ON DUPLICATE KEY UPDATE progress=VALUES(progress), completed=VALUES(completed)")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, season);
			ps.setString(3, questId);
			ps.setInt(4, progress);
			ps.setInt(5, completed ? 1 : 0);
			ps.executeUpdate();
		}
	}

	/** Wipes a player's quest progress for one season (admin/testing). */
	public void reset(UUID uuid, String season) throws SQLException {
		try (PreparedStatement ps = connection().prepareStatement(
				"DELETE FROM " + TABLE + " WHERE uuid=? AND season=?")) {
			ps.setString(1, uuid.toString());
			ps.setString(2, season);
			ps.executeUpdate();
		}
	}
}
