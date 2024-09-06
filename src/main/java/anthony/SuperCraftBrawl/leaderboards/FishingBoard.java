package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FishingBoard {
    private Core main;
    private HashMap<UUID, Integer> caught;
    private HashMap<UUID, Rank> RoleID;
    private ArrayList<UUID> lead;
    private ArrayList<String> lead2;
    private ResultSet set;
    private Connection c;
    private int i;
    
    public FishingBoard(Core main) {
        this.main = main;
        i = 0;
        RoleID = new HashMap<>();
        caught = new HashMap<>();
        lead = new ArrayList<>();
        lead2 = new ArrayList<>();
        c = main.getDatabaseManager().getConnection();
        Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
            caught.clear();
            lead.clear();
            RoleID.clear();
            try {
                Statement s = c.createStatement();
                int a = 0;
                set = s.executeQuery("SELECT UUID, LastPlayerName, TotalCaught, RoleID FROM PlayerData ORDER BY TotalCaught DESC");
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
                    caught.put(id, set.getInt("TotalCaught"));
                    RoleID.put(id, Rank.getRankFromID(set.getInt("RoleID")));
                }
                if (i == 0) {
                    i = 1;
                    Bukkit.getScheduler().runTask(main, () -> {
                        try {
                            caughtBoard();
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
        for (Entity e : main.getLobbyWorld().getEntities()) {
            if (e instanceof ArmorStand) {
                e.remove();
            }
        }
        caught.clear();
        lead.clear();
        c = null;
        RoleID.clear();
    }
    
    public void caughtBoard() throws SQLException {
        for (Entity entity : main.getLobbyWorld().getEntities()) {
            if (entity.getType() == EntityType.ARMOR_STAND) {
                ArmorStand st = (ArmorStand) entity;
                
                if (!(st.getItemInHand().getType() == Material.CHEST))
                    entity.remove();
            }
        }
        
        Location loc = new Location(main.getLobbyWorld(), 305.575, 92.5, 531.328);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(main.color("&e&l<==&nFISHING LEADERBOARD&r&e&l==>"));
        
        int count = 1;
        loc.setY(loc.getY() - 0.4);
        
        for (UUID id : lead) {
            loc.setY(loc.getY() - 0.24);
            String name = lead2.get(count - 1);
            
            Integer win = caught.get(id);
            stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(
                    main.color("&b#" + count + ":" + " &e" + name + " &r- " + win));
            
            count++;
        }
    }
    
    public void updateBoard() throws SQLException {
        for (Entity e : main.getLobbyWorld().getEntities())
            if (e instanceof ArmorStand)
                e.remove();
        
        Location loc = new Location(main.getLobbyWorld(), 305.575, 92.5, 531.328);
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(main.color("&e&l<==&nFISHING LEADERBOARD&r&e&l==>"));
        int count = 1;
        loc.setY(loc.getY() - 0.4);
        
        for (UUID id : lead) {
            loc.setY(loc.getY() - 0.24);
            String name = lead2.get(count - 1);
            
            Integer win = caught.get(id);
            stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(
                    main.color("&b#" + count + ":" + " &e" + name + " &r- " + win));
            
            count++;
        }
    }
    
}
