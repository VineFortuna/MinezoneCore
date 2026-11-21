package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;

/**
 * Reward list for the current season. Index 0 = Tier 1, index 9 = Tier 10.
 * Cosmetic rewards only name the cosmetic (category + id); the cosmetics themselves live in
 * cosmetics/all and show this tier as their "how to unlock" line automatically.
 */
public final class BattlePassRewards {

	private static final BattlePassReward[] FREE = {
			BattlePassReward.classUnlock(ClassType.Elf, 500), // 1
			BattlePassReward.tokens(100),                                      // 2
			BattlePassReward.cosmetic(CosmeticCategory.DEATH_EFFECT, "snow_flurry"), // 3
			BattlePassReward.mysteryChests(2),                                 // 4
			BattlePassReward.classUnlock(ClassType.GingerBreadMan, 500), // 5
			BattlePassReward.cosmetic(CosmeticCategory.TITLE, "Jolly"),        // 6
			BattlePassReward.cosmetic(CosmeticCategory.TRAIL, "snowfall"),     // 7
			BattlePassReward.cosmetic(CosmeticCategory.DOUBLE_JUMP, "snow_puff"), // 8
			BattlePassReward.bundle(BattlePassReward.tokens(200), BattlePassReward.mysteryChests(1)), // 9
			BattlePassReward.classUnlock(ClassType.Santa, 1000), // 10
	};

	private static final BattlePassReward[] PREMIUM = {
			BattlePassReward.cosmetic(CosmeticCategory.TITLE, "Winter Warrior"), // 1
			BattlePassReward.cosmetic(CosmeticCategory.DOUBLE_JUMP, "snowflake_burst"), // 2
			BattlePassReward.mysteryChests(3),                                 // 3
			BattlePassReward.cosmetic(CosmeticCategory.DEATH_EFFECT, "gift_explosion"), // 4
			BattlePassReward.cosmetic(CosmeticCategory.WIN_EFFECT, "present_rain"), // 5
			BattlePassReward.tokens(500),                                      // 6
			BattlePassReward.cosmetic(CosmeticCategory.TRAIL, "holly_wreath"), // 7
			BattlePassReward.cosmetic(CosmeticCategory.DOUBLE_JUMP, "reindeer_leap"), // 8
			BattlePassReward.mysteryChests(5),                                 // 9
			BattlePassReward.bundle(                                           // 10
					BattlePassReward.cosmetic(CosmeticCategory.WIN_EFFECT, "winter_wonderland"),
					BattlePassReward.cosmetic(CosmeticCategory.TITLE, "Christmas Legend")),
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

	/**
	 * Where a cosmetic comes from, e.g. "Christmas 2026 Pass - Premium Tier 5".
	 * Null if no tier of this season hands it out.
	 */
	public static String sourceOf(CosmeticCategory category, String cosmeticId) {
		for (int i = 0; i < FREE.length; i++) {
			if (FREE[i].grantsCosmetic(category, cosmeticId))
				return BattlePassSeason.PLAIN_NAME + " - Free Tier " + (i + 1);
		}
		for (int i = 0; i < PREMIUM.length; i++) {
			if (PREMIUM[i].grantsCosmetic(category, cosmeticId))
				return BattlePassSeason.PLAIN_NAME + " - Premium Tier " + (i + 1);
		}
		return null;
	}
}
