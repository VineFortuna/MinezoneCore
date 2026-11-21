package anthony.SuperCraftBrawl.leaderboards;

/** Which gamemode's stats the leaderboard holograms show - toggled in Leaderboard Settings alongside LeaderboardScope. */
public enum LeaderboardStatsMode {
    SCB, FLAG_WARS;

    public String display() {
        return this == FLAG_WARS ? "Flag Wars" : "SCB";
    }
}
