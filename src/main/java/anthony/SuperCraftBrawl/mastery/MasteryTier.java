package anthony.SuperCraftBrawl.mastery;

import anthony.SuperCraftBrawl.playerdata.ClassDetails;

public class MasteryTier {

    private static final int MULTIPLIER = 2;

    private final int requiredPoints;
    private final MasteryReward reward;
    private final int tierIndex;

    public MasteryTier(int requiredPoints, MasteryReward reward, int tierIndex) {
        this.requiredPoints = requiredPoints;
        this.reward = reward;
        this.tierIndex = tierIndex;
    }

    public int getRequiredPoints() {
        return requiredPoints;
    }

    public MasteryReward getReward() {
        return reward;
    }

    public int getTierIndex() {
        return tierIndex;
    }

    public int getProgress(ClassDetails details) {
        return details.gamesPlayed + (details.gamesWon * MULTIPLIER);
    }

    public MasteryStatus getStatus(ClassDetails details) {
        if (isClaimed(details)) return MasteryStatus.CLAIMED;
        if (getProgress(details) >= requiredPoints) return MasteryStatus.UNLOCKED;
        return MasteryStatus.LOCKED;
    }

    public boolean isClaimed(ClassDetails details) {
        switch (tierIndex) {
            case 1: return details.reward1;
            case 2: return details.reward2;
            case 3: return details.reward3;
            case 4: return details.reward4;
            case 5: return details.reward5;
            default: return false;
        }
    }

    public void claim(ClassDetails details) {
        setClaimed(details, true);
    }

    public void unclaim(ClassDetails details) {
        setClaimed(details, false);
    }

    public void setClaimed(ClassDetails details, boolean value) {
        switch (tierIndex) {
            case 1: details.reward1 = value; break;
            case 2: details.reward2 = value; break;
            case 3: details.reward3 = value; break;
            case 4: details.reward4 = value; break;
            case 5: details.reward5 = value; break;
        }
    }
}