package anthony.SuperCraftBrawl.leaderboards;

public enum LeaderboardScope {
    DAILY, WEEKLY, MONTHLY, LIFETIME;

    public String display() {
        switch (this) {
            case DAILY:   return "Daily";
            case WEEKLY:  return "Weekly";
            case MONTHLY: return "Monthly";
            default:      return "Lifetime";
        }
    }
}