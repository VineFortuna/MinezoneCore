package anthony.SuperCraftBrawl.playerdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import anthony.SuperCraftBrawl.Core;

public class DatabaseManager {
	String hostName = "mysql.apexhosting.gdn:3306", username = "apexMC437310", password = "Bacau17anthony";
	List<String> pendCommands = new ArrayList<>();

	private Connection c;
	private Core main;

	public DatabaseManager(Core main) {
		this.main = main;
		loadConnection();
	}

	public void loadConnection() {
		System.out.print("Loading database connection!");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			c = DriverManager.getConnection("jdbc:mysql://" + hostName + "?user=" + username + "&password=" + password
					+ "&autoReconnect=true&failOverReadOnly=false&maxReconnects=10");
			System.out.print("Loaded database connection!");
			c.setCatalog("apexMC437310");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return c;
	}

	public void multiExecuteUpdateCommand(String... updateCommand) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			for (String cmd : updateCommand) {
				try {
					Statement stmt = getConnection().createStatement();
					stmt.execute(cmd);
					//System.out.println("NOTIFICATION>" + cmd);
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void executeUpdateCommand(String updateCommand) {
		synchronized (pendCommands) {
			pendCommands.add(updateCommand);
		}
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			synchronized (pendCommands) {
				if (pendCommands.size() == 0)
					return;

				for (String cmd : pendCommands) {
					try {
						Statement stmt = getConnection().createStatement();
						stmt.execute(cmd);
						//System.out.println("NOTIFICATION>" + cmd);
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				pendCommands.clear();
			}
		});
	}

    public void ensureSnapshotTable() {
        // We already setCatalog("apexMC437310"), so no need to prefix the schema.
        final String sql =
                "CREATE TABLE IF NOT EXISTS scb_stat_snapshots (" +
                        "  metric       VARCHAR(32)  NOT NULL," +           // 'Wins','Kills','FlawlessWins','TotalCaught', etc.
                        "  uuid         VARCHAR(36)  NOT NULL," +
                        "  period       ENUM('DAILY','WEEKLY','MONTHLY') NOT NULL," +
                        "  period_start DATE         NOT NULL," +           // e.g., 2025-10-16 (daily), Monday of that week, or 1st of month
                        "  total_value  INT          NOT NULL DEFAULT 0," + // snapshot of the player's total at the period start
                        "  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "  PRIMARY KEY (metric, uuid, period, period_start)," +
                        "  INDEX (period, period_start)," +
                        "  INDEX (uuid, metric)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        executeUpdateCommand(sql);
    }

    public void executeQueryCommand(String updateCommand, ExecuteFunction func) {
		try {
			Statement stmt = getConnection().createStatement();
			ResultSet set = stmt.executeQuery(updateCommand);
			func.execute(set);
			set.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
