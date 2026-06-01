package anthony.SuperCraftBrawl.tablist;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;

public class TablistManager {

    private final Core main;
    public final ScoreboardManager scoreManager = Bukkit.getScoreboardManager();
    public final Scoreboard c;

    private final Map<Rank, Team> teamsByRank = new EnumMap<>(Rank.class);

    public TablistManager(Core main) {
        this.main = main;
        this.c = scoreManager.getNewScoreboard();
        registerTeams();
    }

    /** Lower number = higher in tab */
    private int orderFor(Rank r) {
        switch (r) {
            case OWNER:        return 0;
            case ADMIN:        return 1;
            case DEVELOPER:    return 2;  // Dev
            case SR_MODERATOR: return 3;  // Sr.Mod
            case MODERATOR:    return 4;  // Mod
            case TRAINEE:      return 5;
            case QA:           return 6;
            case BUILDER:      return 7;
            case MEDIA:        return 8;
            case SUPREME:      return 9;
            case PRO:      return 10;
            case VIP:          return 11;
            case DEFAULT:
            default:           return 12;
        }
    }

    private static String cut16(String s) {
        if (s == null) return "";
        if (s.length() <= 16) return s;
        String out = s.substring(0, 16);
        // Don’t leave a trailing '§' color introducer at end
        while (out.endsWith("§")) out = out.substring(0, out.length() - 1);
        return out;
    }

    private void registerTeams() {
        for (Rank rank : Rank.values()) {
            String key = String.format("%02d_%s", orderFor(rank), rank.name());
            if (key.length() > 16) key = key.substring(0, 16);

            Team team = c.getTeam(key);
            if (team == null) team = c.registerNewTeam(key);

            // Prefix must be ≤ 16 incl. color codes
            String tag = rank.getTagWithSpace();      // already colored
            team.setPrefix(cut16(tag));

            teamsByRank.put(rank, team);
        }
    }

    @SuppressWarnings("deprecation")
    public void setPlayerTeam(Player player) {
        Rank rank = main.getRankManager().getRank(player);

        for (Team t : c.getTeams()) t.removePlayer(player);

        Team team = teamsByRank.get(rank);
        if (team != null) team.addPlayer(player);

        player.setScoreboard(c);
    }
}