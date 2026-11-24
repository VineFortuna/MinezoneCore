package anthony.SuperCraftBrawl.gui.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.battlepass.BattlePassManager;
import anthony.SuperCraftBrawl.battlepass.BattlePassProgress;
import anthony.SuperCraftBrawl.battlepass.BattlePassReward;
import anthony.SuperCraftBrawl.battlepass.BattlePassRewards;
import anthony.SuperCraftBrawl.battlepass.BattlePassSeason;
import anthony.SuperCraftBrawl.battlepass.BattlePassTrack;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /battlepass: the Free Pass and the Premium Pass in one menu, 5 tiers per page (page arrows on the sides, like the
 * maps menu). Every tier has a colored status pane (lime = reached, yellow = next, red = locked) with its reward
 * right underneath. The free rewards are the top section and the premium ones the bottom section, each labelled by
 * the glass pane next to it.
 *
 *   row 0  your progress (top center)
 *   row 1  free tier status       row 2  free rewards
 *   row 3  premium tier status    row 4  premium rewards
 *   row 5  how to earn XP (bottom center)
 */
public class BattlePassGUI implements InventoryProvider {

	private static final int TIERS_PER_PAGE = 5;
	/** Centers a page of 5 tiers in the 9 columns (columns 2-6). */
	private static final int FIRST_TIER_COLUMN = 2;
	private static final int FREE_STATUS_ROW = 1, FREE_REWARD_ROW = 2, PREMIUM_STATUS_ROW = 3, PREMIUM_REWARD_ROW = 4;
	/** The Free / Premium info panes, next to the page arrows' column. */
	private static final int SECTION_COLUMN = 1;

	private final Core main;
	public SmartInventory inv;

	public BattlePassGUI(Core main) {
		this.main = main;
		this.inv = SmartInventory.builder().id("myInventory").provider(this).size(6, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Christmas Pass").build();
	}

	/** Opens the menu on the page of the next reward to claim, or else of the next tier to reach. */
	public void open(Player player) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);
		if (progress == null) {
			inv.open(player);
			return;
		}

		boolean premium = manager.hasPremium(progress);
		int target = Math.min(BattlePassSeason.MAX_TIER, progress.getTier() + 1);

		if (BattlePassSeason.isActive()) {
			for (int tier = 1; tier <= progress.getTier(); tier++) {
				boolean ready = !progress.hasClaimed(BattlePassTrack.FREE, tier)
						|| (premium && !progress.hasClaimed(BattlePassTrack.PREMIUM, tier));
				if (ready) {
					target = tier;
					break;
				}
			}
		}
		inv.open(player, (target - 1) / TIERS_PER_PAGE);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);
		if (progress == null)
			return; // BattlePassCommand checks this before opening

		boolean premium = manager.hasPremium(progress);

		// Only used to count the pages and tell the first and last one
		Pagination pagination = contents.pagination();
		pagination.setItems(new ClickableItem[BattlePassSeason.MAX_TIER]);
		pagination.setItemsPerPage(TIERS_PER_PAGE);
		int page = Math.min(pagination.getPage(), (BattlePassSeason.MAX_TIER - 1) / TIERS_PER_PAGE);
		pagination.page(page);

		contents.fill(ClickableItem.empty(ItemHelper.getGlassFiller()));

		contents.set(0, 4, ClickableItem.empty(createProgressHead(main, player, progress, premium)));

		// What each section is, on the glass next to it (like the presets menu)
		contents.set(FREE_STATUS_ROW, SECTION_COLUMN, ClickableItem.empty(createFreeSectionItem(progress)));
		contents.set(PREMIUM_STATUS_ROW, SECTION_COLUMN, ClickableItem.empty(createPremiumSectionItem(progress, premium)));

		for (int i = 0; i < TIERS_PER_PAGE; i++) {
			int tier = page * TIERS_PER_PAGE + i + 1;
			if (tier > BattlePassSeason.MAX_TIER)
				break;
			int col = FIRST_TIER_COLUMN + i;

			ClickableItem status = ClickableItem.empty(createTierItem(progress, tier));
			contents.set(FREE_STATUS_ROW, col, status);
			contents.set(FREE_REWARD_ROW, col, createRewardItem(player, progress, BattlePassTrack.FREE, tier, premium, page));
			contents.set(PREMIUM_STATUS_ROW, col, status);
			contents.set(PREMIUM_REWARD_ROW, col, createRewardItem(player, progress, BattlePassTrack.PREMIUM, tier, premium, page));
		}

		// Previous / Next Page, like the maps menu
		if (!pagination.isFirst()) {
			contents.set(2, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> {
				SoundManager.playClickSound(player);
				inv.open(player, pagination.previous().getPage());
			}));
		}
		if (!pagination.isLast()) {
			contents.set(2, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> {
				SoundManager.playClickSound(player);
				inv.open(player, pagination.next().getPage());
			}));
		}

		contents.set(5, 4, ClickableItem.empty(createXpInfoItem()));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	// ---------------------------------------------------------------- items

	/** Your tier, XP and progress to the next tier. */
	private static ItemStack createProgressHead(Core main, Player player, BattlePassProgress progress, boolean premium) {
		int tier = progress.getTier();

		List<String> lore = new ArrayList<>();
		lore.add(main.color("&fTier: &a" + tier + "/" + BattlePassSeason.MAX_TIER));
		lore.add(main.color("&fTotal XP: &a" + fmt(progress.xp)));
		lore.add(main.color("&fPass: " + (premium ? "&6Premium" : "&aFree")));
		lore.add("");

		if (tier >= BattlePassSeason.MAX_TIER) {
			lore.add(main.color("&a&lMAX TIER REACHED"));
		} else {
			lore.add(main.color("&fNext tier:"));
			lore.add(main.progressBar(progress.xp - tier * BattlePassSeason.XP_PER_TIER, BattlePassSeason.XP_PER_TIER, 25));
		}

		lore.add("");
		if (!BattlePassSeason.hasStarted())
			lore.add(main.color("&cStarts on &e" + BattlePassSeason.START.getMonthValue() + "/" + BattlePassSeason.START.getDayOfMonth()));
		else if (BattlePassSeason.hasEnded())
			lore.add(main.color("&cThis season has ended!"));
		else
			lore.add(main.color("&fSeason ends: &a" + String.format("%02d/%02d", BattlePassSeason.END.getMonthValue(), BattlePassSeason.END.getDayOfMonth())));

		return ItemHelper.createSkullHeadPlayer(1, player.getName(), main.color("&e&lChristmas Pass"), lore);
	}

	private ItemStack createFreeSectionItem(BattlePassProgress progress) {
		return ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), // black
				main.color("&e&lFree Pass"),
				"",
				main.color("&7Rewards everyone can claim,"),
				main.color("&7including the " + ClassType.Elf.getTag() + "&7, " + ClassType.GingerBreadMan.getTag()),
				main.color("&7and " + ClassType.Santa.getTag() + " &7classes!"),
				"",
				main.color("&fClaimed: &a" + progress.getClaimed(BattlePassTrack.FREE).size() + "/" + BattlePassSeason.MAX_TIER),
				main.color("&fReady to claim: &a" + countReady(progress, BattlePassTrack.FREE)));
	}

	private ItemStack createPremiumSectionItem(BattlePassProgress progress, boolean premium) {
		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color("&7Exclusive cosmetics and extra rewards,"));
		lore.add(main.color("&7plus &a+" + Math.round((BattlePassSeason.PREMIUM_XP_MULTIPLIER - 1) * 100)
				+ "% &7Battle Pass XP from"));
		lore.add(main.color("&7everything you do!"));
		lore.add("");

		if (premium) {
			lore.add(main.color("&fClaimed: &a" + progress.getClaimed(BattlePassTrack.PREMIUM).size() + "/" + BattlePassSeason.MAX_TIER));
			lore.add(main.color("&fReady to claim: &a" + countReady(progress, BattlePassTrack.PREMIUM)));
		} else {
			lore.add(main.color("&cYou do not own the Premium Pass"));
		}

		return ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15), // black
				main.color("&e&lPremium Pass"), lore.toArray(new String[0]));
	}

	private ItemStack createXpInfoItem() {
		return ItemHelper.setDetails(new ItemStack(Material.PAPER),
				main.color("&e&lEarn XP"),
				main.color("&fGetting a kill: &a+" + BattlePassSeason.XP_KILL + " XP"),
				main.color("&fPlaying a game: &a+" + BattlePassSeason.XP_GAME_PLAYED + " XP"),
				main.color("&fWinning a game: &a+" + BattlePassSeason.XP_WIN + " XP"),
				"",
				main.color("&fPremium bonus: &6+" + Math.round((BattlePassSeason.PREMIUM_XP_MULTIPLIER - 1) * 100) + "% XP"));
	}

	/** The colored pane above each reward. Its stack size is the tier number. */
	private ItemStack createTierItem(BattlePassProgress progress, int tier) {
		int currentTier = progress.getTier();

		short color;
		List<String> lore = new ArrayList<>();
		if (tier <= currentTier) {
			color = 5; // lime
			lore.add(main.color("&a&lREACHED"));
		} else if (tier == currentTier + 1) {
			color = 4; // yellow
			lore.add(main.color("&fProgress:"));
			lore.add(main.progressBar(progress.xp - currentTier * BattlePassSeason.XP_PER_TIER, BattlePassSeason.XP_PER_TIER, 25));
		} else {
			color = 14; // red
			lore.add(main.color("&c&lLOCKED"));
		}

		return ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, tier, color),
				main.color("&e&lTier " + tier), lore.toArray(new String[0]));
	}

	private ClickableItem createRewardItem(Player player, BattlePassProgress progress, BattlePassTrack track, int tier,
	                                       boolean premium, int page) {
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
			stateLine = "&eClick to claim reward";
		else
			stateLine = "&cReach Tier " + tier + " to unlock";

		// What the reward is (type and description), then its state
		List<String> lore = new ArrayList<>();
		for (String line : reward.getDescription(main, player))
			lore.add(main.color(line));
		lore.add("");
		lore.add(main.color(stateLine));

		ItemStack icon = ItemHelper.setDetails(reward.createIcon(main, player), main.color(reward.getName(main, player)),
				lore.toArray(new String[0]));
		ItemHelper.setHideFlags(icon, true);
		if (unlocked && !claimed && !needsPremium && BattlePassSeason.isActive())
			ItemHelper.setGlowing(icon, true);

		return ClickableItem.of(icon, e -> {
			BattlePassManager manager = main.getBattlePassManager();
			if (manager == null)
				return;

			switch (manager.claim(player, track, tier)) {
				case SUCCESS:
					inv.open(player, page); // refresh, on the same page
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

	/** How many unlocked rewards on this track haven't been claimed yet. */
	private static int countReady(BattlePassProgress progress, BattlePassTrack track) {
		if (!BattlePassSeason.isActive())
			return 0;
		int count = 0;
		for (int tier = 1; tier <= progress.getTier(); tier++) {
			if (!progress.hasClaimed(track, tier))
				count++;
		}
		return count;
	}

	private static String fmt(long n) {
		return NumberFormat.getIntegerInstance(Locale.US).format(n);
	}
}
