package anthony.SuperCraftBrawl.leaderboards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Location;

public class FishingBoard extends ScopedStatLeaderboard {

    public FishingBoard(Core main) {
        super(main);
    }

    @Override
    protected String columnName() {
        return "TotalCaught";
    }

    @Override
    protected String metricName() {
        return "TotalCaught";
    }

    @Override
    protected String statLabel() {
        return "Caught";
    }

    @Override
    protected String lifetimeTitle() {
        return "Lifetime Caught";
    }

    @Override
    protected Location titleLocation() {
        return new Location(main.getLobbyWorld(), 228.5, 106.5, 629.5);
    }

    @Override
    protected int getPlayerValue(PlayerData data) {
        return data.totalcaught;
    }
}