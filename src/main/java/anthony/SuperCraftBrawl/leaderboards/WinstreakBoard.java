package anthony.SuperCraftBrawl.leaderboards;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.EntityArmorStand;

public class WinstreakBoard extends LeaderboardBase {
	private Core main;
	private HashMap<UUID, Integer> winstreak;
	private HashMap<UUID, Rank> roleID;
	private ArrayList<UUID> lead;
	private ArrayList<String> lead2;
	private ResultSet set;
	private Connection c;
	private List<Integer> entityIds = new ArrayList<>();

	public WinstreakBoard(Core main) {
		super(main);
		this.main = main;
	}

	@Override
	public void asyncUpdate() throws SQLException {
		roleID = new HashMap<>();
		winstreak = new HashMap<>();
		lead = new ArrayList<>();
		lead2 = new ArrayList<>();
		c = main.getDatabaseManager().getConnection();
		winstreak.clear();
		lead.clear();
		roleID.clear();

		Statement s = c.createStatement();
		int a = 0;
		set = s.executeQuery(
				"SELECT UUID, LastPlayerName, BestWinstreak, RoleID FROM PlayerData ORDER BY BestWinstreak DESC");
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
			winstreak.put(id, set.getInt("BestWinstreak"));
			roleID.put(id, Rank.getRankFromID(set.getInt("RoleID")));
		}
	}

	@Override
	public void updateLeaderboard(boolean init) {
		removeOldLeaderboards();

		Location loc = new Location(main.getLobbyWorld(), 201.5, 106.5, 709.5);
		sendArmorStandPacket(loc, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Best Winstreak");
		loc.setY(loc.getY() - 0.4);

		int count = 1;
		for (UUID id : lead) {
			loc.setY(loc.getY() - 0.24);
			String name = lead2.get(count - 1);
			Integer win = winstreak.get(id);
			sendArmorStandPacket(loc, ChatColor.AQUA + "#" + count + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + win);
			count++;
		}
	}

	private void sendArmorStandPacket(Location loc, String customName) {
		EntityArmorStand armorStand = new EntityArmorStand(((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) loc.getWorld()).getHandle());
		armorStand.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
		armorStand.setCustomName(customName);
		armorStand.setCustomNameVisible(true);
		armorStand.setInvisible(true);
		armorStand.setGravity(false);

		int entityId = armorStand.getId();
		entityIds.add(entityId);

		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(armorStand);
		for (Player player : Bukkit.getOnlinePlayers()) {
			((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}

	private void removeOldLeaderboards() {
		if (!entityIds.isEmpty()) {
			for (int entityId : entityIds) {
				PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityId);
				for (Player player : Bukkit.getOnlinePlayers()) {
					((org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
				}
			}
			entityIds.clear();
		}
	}

	@Override
	public void close() {
		removeOldLeaderboards();
		winstreak.clear();
		lead.clear();
		c = null;
		roleID.clear();
	}
}
