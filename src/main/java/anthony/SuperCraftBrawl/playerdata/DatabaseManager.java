package anthony.SuperCraftBrawl.playerdata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;

import anthony.SuperCraftBrawl.Main;

public class DatabaseManager {
	String hostName = "mysql.apexhosting.gdn:3306", username = "apexMC437310", password = "Bacau17anthony";
	
	private Connection c;
	private Main main;
	
	public DatabaseManager(Main main) {
		this.main = main;
		loadConnection();
	}
	
	public void loadConnection() {
		System.out.print("Loading database connection!");
		try {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager
                    .getConnection("jdbc:mysql://" + hostName
                            + "?user=" + username + "&password=" + password + "&autoReconnect=true&failOverReadOnly=false&maxReconnects=10");
            System.out.print("Loaded database connection!");
            c.setCatalog("apexMC437310");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection() {
		return c;
	}
	
	public void executeUpdateCommand(String updateCommand) {
		Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
			try {
				Statement stmt = getConnection().createStatement();
				stmt.execute(updateCommand);
				System.out.println("NOTIFICATION>" + updateCommand);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        });
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
