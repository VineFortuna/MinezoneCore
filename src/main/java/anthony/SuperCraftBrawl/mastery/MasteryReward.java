package anthony.SuperCraftBrawl.mastery;

import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import org.bukkit.Material;

public class MasteryReward {

    private final int tokenReward;
    private final int expReward;
    private final Material material;

    public MasteryReward(int tokenReward, int expReward, Material material) {
        this.tokenReward = tokenReward;
        this.expReward = expReward;
        this.material = material;
    }

    public int getTokenReward() {
        return tokenReward;
    }

    public int getExpReward() {
        return expReward;
    }

    public Material getMaterial() {
        return material;
    }
}