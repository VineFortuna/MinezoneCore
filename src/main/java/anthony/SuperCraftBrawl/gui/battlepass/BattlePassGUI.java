package anthony.SuperCraftBrawl.gui.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.battlepass.BattlePassManager;
import anthony.SuperCraftBrawl.battlepass.BattlePassProgress;
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
 * /battlepass main menu. Pick the Free Pass or the Premium Pass.
 */
public class BattlePassGUI implements InventoryProvider {

	private final Core main;
	public SmartInventory inv;

	public BattlePassGUI(Core main) {
		this.main = main;
		this.inv = SmartInventory.builder().id("battlePass").provider(this).size(3, 9)
				.title(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Battle Pass").build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);
		if (progress == null)
			return; // BattlePassCommand checks this before opening

		boolean premium = manager.hasPremium(progress);

		contents.fill(ClickableItem.empty(ItemHelper.getGlassFiller()));

		contents.set(0, 4, ClickableItem.empty(BattlePassTrackGUI.createInfoItem(main, progress, premium)));

		contents.set(1, 3, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.IRON_INGOT),
				main.color("&f&lFREE PASS"),
				"&7Rewards everyone can claim.",
				"",
				"&7Unclaimed rewards: &e" + countClaimable(progress, BattlePassTrack.FREE),
				"",
				"&e&lCLICK TO VIEW"),
				e -> new BattlePassTrackGUI(main, BattlePassTrack.FREE).inv.open(player)));

		contents.set(1, 5, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
				main.color("&6&lPREMIUM PASS"),
				"&7Extra rewards, including the",
				"&7exclusive &c&lSanta &7class at &eTier " + BattlePassSeason.MAX_TIER + "&7!",
				"",
				premium ? "&7Unclaimed rewards: &e" + countClaimable(progress, BattlePassTrack.PREMIUM)
						: "&c&lLOCKED",
				"",
				"&e&lCLICK TO VIEW"),
				e -> new BattlePassTrackGUI(main, BattlePassTrack.PREMIUM).inv.open(player)));

		contents.set(2, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER), main.color("&cClose")),
				e -> inv.close(player)));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	private static int countClaimable(BattlePassProgress progress, BattlePassTrack track) {
		if (!BattlePassSeason.isActive())
			return 0;
		int count = 0;
		for (int tier = 1; tier <= progress.getTier(); tier++) {
			if (!progress.hasClaimed(track, tier))
				count++;
		}
		return count;
	}
}
