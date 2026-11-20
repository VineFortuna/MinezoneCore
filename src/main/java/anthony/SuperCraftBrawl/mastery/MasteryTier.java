package anthony.SuperCraftBrawl.mastery;

import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.util.ChatColorHelper;

import java.util.ArrayList;
import java.util.List;

import static anthony.util.ChatColorHelper.color;

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

    public static int getProgress(ClassDetails details) {
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

    public static List<String> getRewardAndProgressBar(ClassDetails details) {
        List<String> lines = new ArrayList<>();

        int played = getProgress(details);
        int[] thresholds = {10, 25, 50, 75, 100};
        String[] numerals = {"I", "II", "III", "IV", "V"};

        for (int i = 0; i < thresholds.length; i++) {
            if (played < thresholds[i]) {
                if (i > 0) {
                    lines.add(color("&aMastery Tier " + numerals[i - 1]));
                }
                lines.add(color("&7Next tier:"));
                lines.add(buildMasteryProgressBar(played, thresholds[i], 25));

                int unclaimed = 0;
                for (int j = 0; j < thresholds.length - 1; j++) {
                    if (played >= thresholds[j] && !new MasteryTier(thresholds[j], null, j + 1).isClaimed(details)) {
                        unclaimed++;
                    }
                }

                if (unclaimed > 0) {
                    lines.add("");
                    lines.add(color("&e" + unclaimed + " unclaimed reward" + (unclaimed == 1 ? "" : "s")));
                }

                return lines;
            }
        }

        // Max tier reached
        lines.add(color("&aMastery Tier " + numerals[numerals.length - 1]));
        lines.add(color("&aMastered!"));

        int unclaimed = 0;
        for (int i = 0; i < thresholds.length - 1; i++) {
            if (!new MasteryTier(thresholds[i], null, i + 1).isClaimed(details)) {
                unclaimed++;
            }
        }

        if (unclaimed > 0) {
            lines.add("");
            lines.add(color("&e" + unclaimed + " unclaimed reward" + (unclaimed == 1 ? "" : "s")));
        }

        return lines;
    }

    public static String buildMasteryProgressBar(int progress, int nextLevel, int segments) {
        String str = "";
        str += color("&8[");
        double frac = (double) progress / nextLevel;
        for (int i = 0; i < segments; i++) {
            if (i < Math.floor(frac * segments))
                str += color("&a|");
            else
                str += color("&7|");
        }
        str += color("&8] &7(" + progress + "/" + nextLevel + ")");
        return str;
    }
}