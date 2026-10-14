package anthony.SuperCraftBrawl.battlepass;

import java.util.HashSet;
import java.util.Set;

/** One player's progress in the current season. Lives in BattlePassManager's cache while they're online. */
public class BattlePassProgress {

	public int xp = 0;
	/** Whether they bought the Premium Pass. See BattlePassManager#hasPremium for the testing override. */
	public boolean premium = false;

	public final Set<Integer> freeClaimed = new HashSet<>();
	public final Set<Integer> premiumClaimed = new HashSet<>();

	public int getTier() {
		return BattlePassSeason.tierForXp(xp);
	}

	public Set<Integer> getClaimed(BattlePassTrack track) {
		return track == BattlePassTrack.PREMIUM ? premiumClaimed : freeClaimed;
	}

	public boolean hasClaimed(BattlePassTrack track, int tier) {
		return getClaimed(track).contains(tier);
	}
}
