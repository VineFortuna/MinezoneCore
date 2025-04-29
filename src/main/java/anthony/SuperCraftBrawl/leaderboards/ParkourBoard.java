package anthony.SuperCraftBrawl.leaderboards;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import anthony.parkour.Arenas;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.EntityArmorStand;

public class ParkourBoard extends LeaderboardBase {
    private Core main;
    private HashMap<UUID, String> caught;
    private HashMap<UUID, Rank> roleID;
    private ArrayList<UUID> lead;
    private ArrayList<String> lead2;
    private ResultSet set;
    private Connection c;
    private List<Integer> entityIds = new ArrayList<>();
    private Arenas arena;

    public ParkourBoard(Core main, Arenas arena) {
        super(main);
        this.main = main;
        this.arena = arena;
    }

    @Override
    public void asyncUpdate() throws SQLException {
        roleID = new HashMap<>();
        caught = new HashMap<>();
        lead = new ArrayList<>();
        lead2 = new ArrayList<>();
        c = main.getDatabaseManager().getConnection();
        caught.clear();
        lead.clear();
        roleID.clear();

        Statement s = c.createStatement();
        int a = 0;
        set = s.executeQuery(
                "SELECT p.LastPlayerName, p.RoleID, parkour.TotalTime, p.UUID " +
                        "FROM PlayerData p " +
                        "JOIN PlayerParkour parkour ON p.UUID = parkour.UUID " +
                        "WHERE parkour.ParkourID = '" + arena.getId() + "'");
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
            caught.put(id, formatTime(set.getLong("TotalTime")));
            roleID.put(id, Rank.getRankFromID(set.getInt("RoleID")));
        }
    }

    @Override
    public void updateLeaderboard(boolean init) {
        removeOldLeaderboards();

        Location loc = arena.getInstance().leaderboardLoc.toLocation(main.getLobbyWorld());
        sendArmorStandPacket(loc, ChatColor.YELLOW + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "Best Time");
        loc.setY(loc.getY() - 0.4);

        int count = 1;
        for (UUID id : lead) {
            loc.setY(loc.getY() - 0.24);
            String name = lead2.get(count - 1);
            String win = caught.get(id);
            sendArmorStandPacket(loc, ChatColor.AQUA + "#" + count + ": " + ChatColor.YELLOW + name + ChatColor.RESET + " - " + win);
            count++;
        }
    }

    private void sendArmorStandPacket(Location loc, String customName) {
        EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) loc.getWorld()).getHandle());
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
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
                }
            }
            entityIds.clear();
        }
    }

    @Override
    public void close() {
        removeOldLeaderboards();
        caught.clear();
        lead.clear();
        c = null;
        roleID.clear();
    }

    public String formatTime(long milliseconds) {
        long minutes = milliseconds / 60000;
        double seconds = (milliseconds % 60000) / 1000.0;

        if (minutes > 0) {
            return String.format("%dm %.1fs", minutes, seconds);
        } else {
            return String.format("%.1fs", seconds);
        }
    }
}
