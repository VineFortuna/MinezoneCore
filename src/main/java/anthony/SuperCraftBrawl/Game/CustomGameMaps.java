package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.map.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Each player's default map: the map their own custom games start on (Maps item, Left Click). It's loaded from the
 * database when they join and dropped when they leave, so only online players are ever held. Everything runs off the
 * main thread, and the table is only created the first time it's needed, so a database problem can't affect startup.
 */
public class CustomGameMaps implements Listener {

    private static final String TABLE = "scb_custom_game_maps";
    private volatile boolean tableReady = false;

    private final Core main;
    private final Map<UUID, Maps> preferred = new ConcurrentHashMap<>(); // online players who chose one

    public CustomGameMaps(Core main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
        for (Player player : Bukkit.getOnlinePlayers()) load(player); // after a reload
    }

    /** The map this player chose as their default, or null when it's Random (what everyone starts with). */
    public Maps getPreferred(Player player) {
        return preferred.get(player.getUniqueId());
    }

    /** The map a new custom game of this player starts on: their default map, or a random one. */
    public Maps resolve(Player player) {
        Maps map = preferred.get(player.getUniqueId());
        return map != null ? map : randomMap(null);
    }

    /** A random Classic map out of the curated ones (what the map menu opens on), never the excluded one. */
    public static Maps randomMap(Maps excluded) {
        List<Maps> found = Maps.filterMaps(GameType.CLASSIC, Maps.Category.CURATED, null, null);
        List<Maps> pool = found == null ? new ArrayList<>() : new ArrayList<>(found);
        pool.remove(excluded);
        if (pool.isEmpty()) return excluded != null ? excluded : GameManager.defaultCustomGameMap();
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    /** Their new default map, or null for Random. */
    public void set(Player player, Maps map) {
        UUID uuid = player.getUniqueId();
        if (map == null) preferred.remove(uuid);
        else preferred.put(uuid, map);

        String name = map == null ? null : map.name();
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            String sql = name == null ? "DELETE FROM " + TABLE + " WHERE uuid = ?"
                    : "INSERT INTO " + TABLE + " (uuid, map) VALUES (?, ?) ON DUPLICATE KEY UPDATE map = VALUES(map)";
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                if (name != null) ps.setString(2, name);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void load(Player player) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Maps loaded = null;
            try (PreparedStatement ps = connection().prepareStatement("SELECT map FROM " + TABLE + " WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) loaded = Maps.valueOf(rs.getString("map"));
                }
            } catch (IllegalArgumentException ignored) {
                // a map that was renamed or removed: the default is used again
            } catch (Exception e) {
                e.printStackTrace();
            }

            Maps result = loaded;
            if (result == null) return;
            Bukkit.getScheduler().runTask(main, () -> {
                if (Bukkit.getPlayer(uuid) != null) preferred.put(uuid, result); // not if they left meanwhile
            });
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        preferred.remove(event.getPlayer().getUniqueId());
    }

    private Connection connection() throws Exception {
        Connection c = main.getDatabaseManager().getConnection();
        if (c == null) throw new IllegalStateException("No database connection");
        if (!tableReady) {
            try (Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                        + " uuid VARCHAR(36) NOT NULL,"
                        + " map VARCHAR(64) NOT NULL,"
                        + " PRIMARY KEY (uuid)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            }
            tableReady = true;
        }
        return c;
    }
}
