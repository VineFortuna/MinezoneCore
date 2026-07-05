package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Core;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticDataDAO {

    private static final String TABLE = "scb_player_cosmetics";

    private final Core main;

    public CosmeticDataDAO(Core main) {
        this.main = main;
    }

    /** Synchronous, indexed PK lookup - called once per join, same pattern as PlayerDataManager's join-time load. */
    public Map<CosmeticCategory, String> loadEquipped(UUID uuid) {
        Map<CosmeticCategory, String> result = new EnumMap<>(CosmeticCategory.class);
        String sql = "SELECT category, cosmetic_id FROM " + TABLE + " WHERE uuid = ?";
        try (PreparedStatement ps = main.getDatabaseManager().getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        CosmeticCategory category = CosmeticCategory.valueOf(rs.getString("category"));
                        result.put(category, rs.getString("cosmetic_id"));
                    } catch (IllegalArgumentException ignored) {
                        // unknown/renamed category value in the DB - skip defensively
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /** Async fire-and-forget upsert; never blocks the calling (GUI-click) thread. */
    public void saveEquipped(UUID uuid, CosmeticCategory category, String cosmeticId) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            String sql = "INSERT INTO " + TABLE + " (uuid, category, cosmetic_id) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE cosmetic_id = VALUES(cosmetic_id)";
            try (PreparedStatement ps = main.getDatabaseManager().getConnection().prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, category.name());
                ps.setString(3, cosmeticId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteEquipped(UUID uuid, CosmeticCategory category) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            String sql = "DELETE FROM " + TABLE + " WHERE uuid = ? AND category = ?";
            try (PreparedStatement ps = main.getDatabaseManager().getConnection().prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, category.name());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}