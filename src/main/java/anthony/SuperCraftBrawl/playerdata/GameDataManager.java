package anthony.SuperCraftBrawl.playerdata;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameDataManager {

    private final Core main;
    private final DatabaseManager db;

    public GameDataManager(Core main) {
        this.main = main;
        this.db = main.getDatabaseManager();
    }

    public void createGame(
            String gameType,
            String mapName,
            int durationMinutes,
            Consumer<Integer> callback
    ) {

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

            db.executeUpdateCommand(
                    "INSERT INTO scb_games (game_type, map_name, end_time, game_duration_minutes) VALUES ('"
                            + gameType + "', '"
                            + mapName + "', CURRENT_TIMESTAMP, "
                            + durationMinutes + ")"
            );

            final int[] id = new int[1];

            db.executeQueryCommand(
                    "SELECT MAX(game_id) AS id FROM scb_games",
                    rs -> {
                        if (rs.next()) id[0] = rs.getInt("id");
                    }
            );

            callback.accept(id[0]);
        });
    }

    public void insertGamePlayer(
            int gameId,
            Player player,
            int classId,
            int placement,
            int kills,
            int deaths,
            int lives,
            boolean win,
            boolean firstBlood
    ) {

        String sql =
                "INSERT INTO scb_game_players " +
                        "(game_id, uuid, class_id, placement, kills, deaths, lives, winner, firstblood) VALUES (" +
                        gameId + ", '" +
                        player.getUniqueId() + "', " +
                        classId + ", " +
                        placement + ", " +
                        kills + ", " +
                        deaths + ", " +
                        lives + ", " +
                        (win ? 1 : 0) + ", " +
                        (firstBlood ? 1 : 0) +
                        ")";

        db.executeUpdateCommand(sql);
    }

    public void saveMatch(
            GameType gameType,
            String mapName,
            int durationMinutes,
            List<Player> winners,
            List<Player> players,
            Map<Player, BaseClass> classes,
            Player firstBlood) {

        createGame(gameType.name(), mapName, durationMinutes, (gameId) -> {

            for (Player p : players) {

                BaseClass bc = classes.get(p);

                insertGamePlayer(
                        gameId,
                        p,
                        bc.getType().getID(),
                        bc.placement,
                        bc.totalKills,
                        bc.totalDeaths,
                        bc.getLives(),
                        winners.contains(p),
                        firstBlood.equals(p)
                );
            }
        });
    }
}