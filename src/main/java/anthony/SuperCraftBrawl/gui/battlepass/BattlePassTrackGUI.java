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

import java.util.ArrayList;
import java.util.List;

/**
 * One pass (Free or Premium), laid out like the class mastery menu: every tier has a colored status
 * pane (lime = reached, yellow = next, red = locked) with its reward right underneath.
 *
 *   row 0  your progress (top center)
 *   row 1  tiers 1-5 status       row 2  tiers 1-5 rewards
 *   row 3  tiers 6-10 status      row 4  tiers 6-10 rewards
 *   row 5  back arrow (bottom right)
 */
public class BattlePassTrackGUI implements InventoryProvider {

	private static final int TIERS_PER_BLOCK = 5;
	/** Centers a block of 5 tiers in the 9 columns (columns 2-6). */
	private static final int FIRST_TIER_COLUMN = 2;

	private final Core main;
	private final BattlePassTrack track;
	public SmartInventory inv;

	public BattlePassTrackGUI(Core main, BattlePassTrack track, SmartInventory parent) {
		this.main = main;
		this.track = track;
		String title = track == BattlePassTrack.PREMIUM ? "Premium Pass" : "Free Pass";
		this.inv = SmartInventory.builder().id("myInventory").provider(this).size(6, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + title).parent(parent).build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);
		if (progress == null)
			return;

		boolean premium = manager.hasPremium(progress);

		contents.fill(ClickableItem.empty(ItemHelper.getGlassFiller()));

		contents.set(0, 4, ClickableItem.empty(BattlePassGUI.createProgressHead(main, player, progress, premium)));

		for (int tier = 1; tier <= BattlePassSeason.MAX_TIER; tier++) {
			int block = (tier - 1) / TIERS_PER_BLOCK; // 0 = tiers 1-5, 1 = tiers 6-10
			int col = FIRST_TIER_COLUMN + (tier - 1) % TIERS_PER_BLOCK;
			int statusRow = 1 + block * 2;

			contents.set(statusRow, col, ClickableItem.empty(createTierItem(progress, tier)));
			contents.set(statusRow + 1, col, createRewardItem(player, progress, tier, premium));
		}

		if (inv.getParent().isPresent()) {
			contents.set(5, 8, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"),
					e -> inv.getParent().get().open(player)));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	// ---------------------------------------------------------------- items

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

	private ClickableItem createRewardItem(Player player, BattlePassProgress progress, int tier, boolean premium) {
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
					inv.open(player); // refresh
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
}
