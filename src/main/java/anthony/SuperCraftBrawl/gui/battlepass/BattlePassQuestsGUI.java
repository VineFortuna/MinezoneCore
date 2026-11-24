package anthony.SuperCraftBrawl.gui.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.battlepass.BattlePassManager;
import anthony.SuperCraftBrawl.battlepass.BattlePassProgress;
import anthony.SuperCraftBrawl.battlepass.BattlePassQuest;
import anthony.SuperCraftBrawl.battlepass.BattlePassQuestManager;
import anthony.SuperCraftBrawl.battlepass.BattlePassSeason;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
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
 * The Premium Pass quests. Every quest is open all season and can be finished once, whenever. Premium only.
 * Opened from the corner button of the Battle Pass menu.
 *
 *   row 0         your progress (top center)
 *   rows 1 and on the quests, 4 to a row (columns 1, 3, 5 and 7)
 *   last row      quests done (center), go back to the pass (right)
 */
public class BattlePassQuestsGUI implements InventoryProvider {

	private static final int QUESTS_PER_ROW = 4;

	private final Core main;
	private final int rows;
	public SmartInventory inv;

	public BattlePassQuestsGUI(Core main) {
		this.main = main;
		// Head row, the quest rows, then the bottom row (at most 6 rows in a chest menu)
		int questRows = (BattlePassQuest.values().length + QUESTS_PER_ROW - 1) / QUESTS_PER_ROW;
		this.rows = Math.max(3, Math.min(6, questRows + 2));
		this.inv = SmartInventory.builder().id("battlePassQuests").provider(this).size(rows, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Pass Quests").build();
	}

	/** Opens the quests, or tells the player why they can't. */
	public void open(Player player) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassQuestManager quests = manager == null ? null : manager.getQuests();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);

		if (quests == null || progress == null) {
			player.sendMessage(main.color("&c&l(!) &rQuests are currently unavailable."));
		} else if (!manager.hasPremium(progress)) {
			player.sendMessage(main.color("&c&l(!) &rYou need the &6Premium Pass &rfor quests!"));
		} else if (!BattlePassSeason.isActive()) {
			player.sendMessage(main.color("&c&l(!) &rThe Battle Pass season isn't active right now!"));
		} else if (!quests.canUse(player)) {
			player.sendMessage(main.color("&c&l(!) &rYour quests are still loading, try again in a moment!"));
		} else {
			SoundManager.playClickSound(player);
			inv.open(player);
		}
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);
		BattlePassQuestManager quests = manager == null ? null : manager.getQuests();
		if (progress == null || quests == null)
			return;

		contents.fillBorders(ClickableItem.empty(ItemHelper.getGlassFiller()));
		contents.set(0, 4, ClickableItem.empty(BattlePassMenuItems.progressHead(main, player, progress, manager.hasPremium(progress))));

		BattlePassQuest[] all = BattlePassQuest.values();
		for (int i = 0; i < all.length; i++) {
			int row = 1 + i / QUESTS_PER_ROW;
			if (row >= rows - 1)
				break; // no room left above the bottom row
			int col = 1 + (i % QUESTS_PER_ROW) * 2;
			contents.set(row, col, ClickableItem.empty(createQuestItem(quests, player, all[i])));
		}

		contents.set(rows - 1, 4, ClickableItem.empty(createSummaryItem(quests, player)));
		contents.set(rows - 1, 8, ClickableItem.of(ItemHelper.getGoBackItem(), e -> {
			SoundManager.playClickSound(player);
			new BattlePassGUI(main).open(player);
		}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	private ItemStack createQuestItem(BattlePassQuestManager quests, Player player, BattlePassQuest quest) {
		boolean completed = quests.isCompleted(player, quest);
		int progress = completed ? quest.getGoal() : quests.getProgress(player, quest);

		List<String> lore = new ArrayList<>();
		lore.add("");
		for (String line : quest.getDescription())
			lore.add(main.color("&7" + line));
		lore.add("");
		lore.add(main.color("&fProgress:"));
		lore.add(main.progressBar(progress, quest.getGoal(), 25));
		lore.add("");
		lore.add(main.color("&fReward: &a+" + BattlePassSeason.questRewardXp() + " Battle Pass XP"));
		if (completed) {
			lore.add("");
			lore.add(main.color("&a&lCOMPLETED"));
		}

		ItemStack icon = ItemHelper.setDetails(quest.createIcon(), main.color("&6&l" + quest.getName()), lore.toArray(new String[0]));
		ItemHelper.setHideFlags(icon, true);
		if (completed)
			ItemHelper.setGlowing(icon, true);
		return icon;
	}

	private ItemStack createSummaryItem(BattlePassQuestManager quests, Player player) {
		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(main.color("&7Finish them whenever you like."));
		lore.add("");
		lore.add(main.color("&fCompleted: &a" + quests.getCompletedCount(player) + "&7/&a" + BattlePassQuest.values().length));
		return ItemHelper.setDetails(new ItemStack(Material.BOOK), main.color("&6&lQuests"), lore.toArray(new String[0]));
	}
}
