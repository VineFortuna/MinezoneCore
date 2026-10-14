package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Game.classes.ClassType;
import org.bukkit.Material;

/**
 * Reward list for the current season. Index 0 = Tier 1, index 19 = Tier 20.
 * Placeholder rewards for now - swap in cosmetics/titles/etc. as they're made.
 */
public final class BattlePassRewards {

	private static final BattlePassReward[] FREE = {
			BattlePassReward.tokens(50),         // 1
			BattlePassReward.tokens(75),         // 2
			BattlePassReward.mysteryChests(1),   // 3
			BattlePassReward.tokens(100),        // 4
			BattlePassReward.levels(1),          // 5
			BattlePassReward.tokens(100),        // 6
			BattlePassReward.mysteryChests(1),   // 7
			BattlePassReward.tokens(125),        // 8
			BattlePassReward.tokens(125),        // 9
			BattlePassReward.levels(1),          // 10
			BattlePassReward.tokens(150),        // 11
			BattlePassReward.mysteryChests(2),   // 12
			BattlePassReward.tokens(150),        // 13
			BattlePassReward.tokens(175),        // 14
			BattlePassReward.levels(2),          // 15
			BattlePassReward.tokens(200),        // 16
			BattlePassReward.mysteryChests(2),   // 17
			BattlePassReward.tokens(250),        // 18
			BattlePassReward.tokens(300),        // 19
			BattlePassReward.mysteryChests(3),   // 20
	};

	private static final BattlePassReward[] PREMIUM = {
			BattlePassReward.tokens(150),        // 1
			BattlePassReward.mysteryChests(1),   // 2
			BattlePassReward.tokens(200),        // 3
			BattlePassReward.levels(1),          // 4
			BattlePassReward.tokens(250),        // 5
			BattlePassReward.mysteryChests(2),   // 6
			BattlePassReward.tokens(300),        // 7
			BattlePassReward.levels(2),          // 8
			BattlePassReward.tokens(300),        // 9
			BattlePassReward.mysteryChests(3),   // 10
			BattlePassReward.tokens(350),        // 11
			BattlePassReward.levels(2),          // 12
			BattlePassReward.tokens(400),        // 13
			BattlePassReward.mysteryChests(3),   // 14
			BattlePassReward.tokens(450),        // 15
			BattlePassReward.levels(3),          // 16
			BattlePassReward.tokens(500),        // 17
			BattlePassReward.mysteryChests(5),   // 18
			BattlePassReward.tokens(750),        // 19
			BattlePassReward.classUnlock(ClassType.Santa, Material.REDSTONE_BLOCK, 1000), // 20
	};

	static {
		if (FREE.length != BattlePassSeason.MAX_TIER || PREMIUM.length != BattlePassSeason.MAX_TIER)
			throw new IllegalStateException("Battle Pass reward lists must have exactly " + BattlePassSeason.MAX_TIER + " tiers");
	}

	private BattlePassRewards() {
	}

	/** @param tier 1-based tier number */
	public static BattlePassReward get(BattlePassTrack track, int tier) {
		BattlePassReward[] list = track == BattlePassTrack.PREMIUM ? PREMIUM : FREE;
		return list[tier - 1];
	}
}
