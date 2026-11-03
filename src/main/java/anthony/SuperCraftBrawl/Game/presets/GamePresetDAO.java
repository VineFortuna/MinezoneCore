package anthony.SuperCraftBrawl.Game.presets;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Consumer;

/*
 * Players' saved custom game presets (slots 1 to MAX_PRESETS). Everything runs off the main thread, and the table
 * is only created the first time someone opens their presets, so a database problem can't affect startup.
 */
public final class GamePresetDAO {

    public static final int MAX_PRESETS = 7;
    private static final String TABLE = "scb_custom_game_presets";

    private static volatile boolean tableReady = false;

    private GamePresetDAO() {
    }

    /** Loads the player's presets (index 0 = preset #1, null = free slot), then calls back on the main thread. */
    public static void load(Core main, UUID uuid, Consumer<GamePreset[]> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            GamePreset[] presets = new GamePreset[MAX_PRESETS];
            boolean ok = true;
            try {
                Connection c = connection(main);
                try (PreparedStatement ps = c.prepareStatement("SELECT slot, data FROM " + TABLE + " WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int slot = rs.getInt("slot");
                            if (slot < 1 || slot > MAX_PRESETS) continue;
                            presets[slot - 1] = GamePreset.deserialize(rs.getString("data"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ok = false;
            }

            GamePreset[] result = ok ? presets : null; // null = couldn't load
            Bukkit.getScheduler().runTask(main, () -> callback.accept(result));
        });
    }

    /** slot is 1 to MAX_PRESETS. */
    public static void save(Core main, UUID uuid, int slot, GamePreset preset) {
        String data = preset.serialize(); // read the settings on the main thread
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            String sql = "INSERT INTO " + TABLE + " (uuid, slot, data) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE data = VALUES(data)";
            try (PreparedStatement ps = connection(main).prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, slot);
                ps.setString(3, data);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void delete(Core main, UUID uuid, int slot) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            try (PreparedStatement ps = connection(main).prepareStatement(
                    "DELETE FROM " + TABLE + " WHERE uuid = ? AND slot = ?")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, slot);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static Connection connection(Core main) throws Exception {
        Connection c = main.getDatabaseManager().getConnection();
        if (c == null) throw new IllegalStateException("No database connection");
        if (!tableReady) {
            try (Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                        + " uuid VARCHAR(36) NOT NULL,"
                        + " slot TINYINT NOT NULL,"
                        + " data TEXT NOT NULL,"
                        + " PRIMARY KEY (uuid, slot)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            }
            tableReady = true;
        }
        return c;
    }
}
