package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/*
 * What /gamestats shows for a game. While the game is still around it reads the game itself; once the game is
 * removed it keeps only a copy of the numbers, so a player's last game (its classes, entities and whole world)
 * isn't kept in memory until they log off.
 */
public class MatchStats {

    public static class Row {
        public final UUID uuid;
        public final String name;
        public final String classTag;
        public final int kills;
        public final int deaths;
        public final boolean winner;
        public final boolean firstBlood;

        Row(UUID uuid, String name, String classTag, int kills, int deaths, boolean winner, boolean firstBlood) {
            this.uuid = uuid;
            this.name = name;
            this.classTag = classTag;
            this.kills = kills;
            this.deaths = deaths;
            this.winner = winner;
            this.firstBlood = firstBlood;
        }
    }

    private GameInstance game; // null once the game is removed
    private boolean ended;
    private List<Row> rows;

    MatchStats(GameInstance game) {
        this.game = game;
    }

    public boolean hasEnded() {
        return game != null ? game.state == GameState.ENDED : ended;
    }

    public List<Row> getRows() {
        return game != null ? snapshot(game) : rows;
    }

    /** Called when the game is removed: keeps a copy of the numbers and lets go of the game. */
    void freeze() {
        if (game == null) return;
        ended = game.state == GameState.ENDED;
        rows = snapshot(game);
        game = null;
    }

    private static List<Row> snapshot(GameInstance game) {
        List<Row> rows = new ArrayList<>();
        for (Entry<Player, BaseClass> entry : game.allClasses.entrySet()) {
            Player player = entry.getKey();
            BaseClass bc = entry.getValue();
            if (player == null || bc == null) continue;
            rows.add(new Row(player.getUniqueId(), player.getName(), bc.getType().getTag(), bc.totalKills,
                    bc.totalDeaths, game.getWinnerList().contains(player), game.firstBlood == player));
        }
        return rows;
    }
}
