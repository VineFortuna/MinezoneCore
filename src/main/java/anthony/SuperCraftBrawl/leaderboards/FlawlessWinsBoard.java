package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FlawlessWinsBoard {
	private Core main;
	private HashMap<UUID, Integer> flawlessWins;
	private HashMap<UUID, Rank> RoleID;
	private ArrayList<UUID> lead;
	private ArrayList<String> lead2;
	private ResultSet set;
	private Connection c;
	private int i;
	private List<ArmorStand> toRemove;

	public FlawlessWinsBoard(Core main) {
		this.main = main;
		Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
			for (Entity e : main.getLobbyWorld().getEntities()) {
				if (e instanceof ArmorStand) {
					e.remove();
				}
			}
		}, 0, 	(20 * 60) - 1);
		

		Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
			i = 0;
			RoleID = new HashMap<>();
			flawlessWins = new HashMap<>();
			lead = new ArrayList<>();
			lead2 = new ArrayList<>();
			toRemove = new ArrayList<ArmorStand>();
			c = main.getDatabaseManager().getConnection();
			flawlessWins.clear();
			lead.clear();
			RoleID.clear();
			try {
				Statement s = c.createStatement();
				int a = 0;
				set = s.executeQuery(
						"SELECT UUID, LastPlayerName, FlawlessWins, RoleID FROM PlayerData ORDER BY FlawlessWins DESC");
				while (set.next()) {
					if (a == 10) {
						break;
					}
					UUID id = UUID.fromString(set.getString("UUID"));
					String name = set.getString("LastPlayerName");
					if (name == null) {
						continue;
					}
					a++;
					lead.add(id);
					lead2.add(name);
					flawlessWins.put(id, set.getInt("FlawlessWins"));
					RoleID.put(id, Rank.getRankFromID(set.getInt("RoleID")));
				}
				if (i == 0) {
					i = 1;
					Bukkit.getScheduler().runTask(main, () -> {
						try {
							winstreakBoard();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					});
				} else {
					Bukkit.getScheduler().runTask(main, () -> {
						try {
							updateBoard();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}, 0, 20 * 60);
	}

	public void close() {
		for (ArmorStand stand : toRemove) {
			stand.remove();
		}

		toRemove.clear();
		flawlessWins.clear();
		lead.clear();
		c = null;
		RoleID.clear();
	}

	public void winstreakBoard() throws SQLException {
		for (ArmorStand stand : toRemove) {
			stand.remove();
		}

		toRemove.clear();
		Location loc = new Location(main.getLobbyWorld(), 178.5, 106.5, 709.5);
		ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		stand.setCustomName("" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "Lifetime Flawless Wins");
		toRemove.add(stand);

		int count = 1;
		loc.setY(loc.getY() - 0.4);

		for (UUID id : lead) {
			loc.setY(loc.getY() - 0.24);
			String name = lead2.get(count - 1);

			Integer win = flawlessWins.get(id);
			stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setCustomNameVisible(true);
			stand.setCustomName(main.color("&b#" + count + ":" + " &e" + name + " &r- " + win));
			toRemove.add(stand);

			count++;
		}
	}

	public void updateBoard() throws SQLException {
		for (ArmorStand stand : toRemove) {
			stand.remove();
		}

		toRemove.clear();
		Location loc = new Location(main.getLobbyWorld(), 178.5, 106.5, 709.5);
		ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		stand.setCustomName("" + ChatColor.YELLOW + ChatColor.BOLD + ChatColor.UNDERLINE + "Lifetime Flawless Wins");
		toRemove.add(stand);
		int count = 1;
		loc.setY(loc.getY() - 0.4);

		for (UUID id : lead) {
			loc.setY(loc.getY() - 0.24);
			String name = lead2.get(count - 1);

			Integer win = flawlessWins.get(id);
			stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setCustomNameVisible(true);
			stand.setCustomName(main.color("&b#" + count + ":" + " &e" + name + " &r- " + win));
			toRemove.add(stand);

			count++;
		}
	}

}
