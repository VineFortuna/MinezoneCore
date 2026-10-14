package anthony.SuperCraftBrawl.gui.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.battlepass.BattlePassManager;
import anthony.SuperCraftBrawl.battlepass.BattlePassProgress;
import anthony.SuperCraftBrawl.battlepass.BattlePassReward;
import anthony.SuperCraftBrawl.battlepass.BattlePassRewards;
import anthony.SuperCraftBrawl.battlepass.BattlePassSeason;
import anthony.SuperCraftBrawl.battlepass.BattlePassTrack;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * One pass (Free or Premium), 7 tiers per page:
 *   row 1 = rewards, row 2 = tier progress, row 3 = navigation.
 */
public class BattlePassTrackGUI implements InventoryProvider {

	private static final int TIERS_PER_PAGE = 7;
	private static final int REWARD_ROW = 1, TIER_ROW = 2, NAV_ROW = 3;

	private final Core main;
	private final BattlePassTrack track;
	private final int page;
	public SmartInventory inv;

	public BattlePassTrackGUI(Core main, BattlePassTrack track) {
		this(main, track, -1);
	}

	/** @param page 0-based page, or -1 to open on the page with the player's current tier */
	public BattlePassTrackGUI(Core main, BattlePassTrack track, int page) {
		this.main = main;
		this.track = track;
		this.page = page;
		String title = track == BattlePassTrack.PREMIUM ? "Premium Pass" : "Free Pass";
		this.inv = SmartInventory.builder().id("battlePassTrack").provider(this).size(4, 9)
				.title(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + title).build();
	}

	private static int pageCount() {
		return (BattlePassSeason.MAX_TIER + TIERS_PER_PAGE - 1) / TIERS_PER_PAGE;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);
		if (progress == null)
			return;

		int currentTier = progress.getTier();
		int shownPage = page >= 0 ? page
				: Math.min(pageCount() - 1, Math.max(0, currentTier - 1) / TIERS_PER_PAGE);
		boolean premium = manager.hasPremium(progress);

		contents.fill(ClickableItem.empty(ItemHelper.getGlassFiller()));

		contents.set(0, 4, ClickableItem.empty(createInfoItem(main, progress, premium)));

		int firstTier = shownPage * TIERS_PER_PAGE + 1;
		for (int i = 0; i < TIERS_PER_PAGE; i++) {
			int tier = firstTier + i;
			if (tier > BattlePassSeason.MAX_TIER)
				break;
			int col = i + 1;

			contents.set(REWARD_ROW, col, createRewardItem(player, progress, tier, premium, shownPage));
			contents.set(TIER_ROW, col, ClickableItem.empty(createTierMarker(tier, progress)));
		}

		if (shownPage > 0)
			contents.set(NAV_ROW, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(),
					e -> new BattlePassTrackGUI(main, track, shownPage - 1).inv.open(player)));
		if (shownPage < pageCount() - 1)
			contents.set(NAV_ROW, 8, ClickableItem.of(ItemHelper.getNextPageItem(),
					e -> new BattlePassTrackGUI(main, track, shownPage + 1).inv.open(player)));

		contents.set(NAV_ROW, 3, ClickableItem.of(ItemHelper.getGoBackItem(),
				e -> new BattlePassGUI(main).inv.open(player)));
		contents.set(NAV_ROW, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER), main.color("&cClose")),
				e -> inv.close(player)));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	// ---------------------------------------------------------------- items

	/** Season/progress summary, shared with the main menu. */
	static ItemStack createInfoItem(Core main, BattlePassProgress progress, boolean premium) {
		int tier = progress.getTier();
		String status;
		if (!BattlePassSeason.hasStarted())
			status = "&cStarts on &e" + BattlePassSeason.START.getMonthValue() + "/" + BattlePassSeason.START.getDayOfMonth();
		else if (BattlePassSeason.hasEnded())
			status = "&cThis season has ended!";
		else
			status = "&aSeason active!";

		String xpLine;
		if (tier >= BattlePassSeason.MAX_TIER) {
			xpLine = "&a&lMAX TIER REACHED!";
		} else {
			int into = progress.xp - tier * BattlePassSeason.XP_PER_TIER;
			xpLine = "&7Next tier: " + progressBar(into, BattlePassSeason.XP_PER_TIER) + " &e" + into + "&7/&e" + BattlePassSeason.XP_PER_TIER;
		}

		return ItemHelper.setDetails(new ItemStack(Material.BOOK), main.color(BattlePassSeason.NAME),
				status,
				"",
				"&7Tier: &e" + tier + "&7/&e" + BattlePassSeason.MAX_TIER,
				"&7Total XP: &e" + progress.xp,
				xpLine,
				"&7Pass: " + (premium ? "&6&lPREMIUM" : "&fFree"),
				"",
				"&6How to earn Battle Pass XP:",
				"&7- Win a game: &e+" + BattlePassSeason.XP_WIN + " XP",
				"&7- Get a kill: &e+" + BattlePassSeason.XP_KILL + " XP",
				"&7- Play a game: &e+" + BattlePassSeason.XP_GAME_PLAYED + " XP");
	}

	private ItemStack createTierMarker(int tier, BattlePassProgress progress) {
		int currentTier = progress.getTier();
		short color;
		String state;
		if (tier <= currentTier) {
			color = 5; // lime
			state = "&a&lUNLOCKED";
		} else if (tier == currentTier + 1) {
			color = 4; // yellow
			int into = progress.xp - currentTier * BattlePassSeason.XP_PER_TIER;
			state = "&e" + into + "&7/&e" + BattlePassSeason.XP_PER_TIER + " XP";
		} else {
			color = 14; // red
			state = "&c&lLOCKED";
		}

		return ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, tier, color),
				main.color("&e&lTIER " + tier), state);
	}

	private ClickableItem createRewardItem(Player player, BattlePassProgress progress, int tier, boolean premium, int shownPage) {
		BattlePassReward reward = BattlePassRewards.get(track, tier);
		boolean claimed = progress.hasClaimed(track, tier);
		boolean unlocked = tier <= progress.getTier();
		boolean needsPremium = track == BattlePassTrack.PREMIUM && !premium;

		String stateLine;
		if (claimed)
			stateLine = "&a&lCLAIMED";
		else if (needsPremium)
			stateLine = "&6Requires the Premium Pass";
		else if (!BattlePassSeason.isActive())
			stateLine = "&cSeason not active";
		else if (unlocked)
			stateLine = "&e&lCLICK TO CLAIM";
		else
			stateLine = "&cReach Tier " + tier + " to unlock";

		String trackName = track == BattlePassTrack.PREMIUM ? "&6Premium" : "&fFree";
		ItemStack icon = ItemHelper.setDetails(reward.createIcon(),
				main.color(reward.getName()),
				trackName + " &7- Tier " + tier,
				"",
				stateLine);
		ItemHelper.setHideFlags(icon, true);
		if (unlocked && !claimed && !needsPremium && BattlePassSeason.isActive())
			ItemHelper.setGlowing(icon, true);

		return ClickableItem.of(icon, e -> {
			BattlePassManager manager = main.getBattlePassManager();
			if (manager == null)
				return;

			switch (manager.claim(player, track, tier)) {
				case SUCCESS:
					new BattlePassTrackGUI(main, track, shownPage).inv.open(player); // refresh
					break;
				case ALREADY_CLAIMED:
					player.sendMessage(main.color("&c&l(!) &rYou already claimed this reward!"));
					break;
				case TIER_LOCKED:
					player.sendMessage(main.color("&c&l(!) &rYou need to reach &eTier " + tier + " &rfirst!"));
					break;
				case NEEDS_PREMIUM:
					player.sendMessage(main.color("&c&l(!) &rYou need the &6Premium Pass &rto claim this!"));
					break;
				case SEASON_INACTIVE:
					player.sendMessage(main.color("&c&l(!) &rThe Battle Pass season isn't active right now!"));
					break;
				case NOT_LOADED:
					player.sendMessage(main.color("&c&l(!) &rYour Battle Pass is still loading, try again in a moment!"));
					break;
			}
		});
	}

	private static String progressBar(int value, int max) {
		int bars = 20;
		int filled = max <= 0 ? 0 : Math.min(bars, value * bars / max);
		StringBuilder sb = new StringBuilder("&a");
		for (int i = 0; i < bars; i++) {
			if (i == filled)
				sb.append("&8");
			sb.append('|');
		}
		return sb.toString();
	}
}
