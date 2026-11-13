package anthony.SuperCraftBrawl.gui.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.battlepass.BattlePassManager;
import anthony.SuperCraftBrawl.battlepass.BattlePassProgress;
import anthony.SuperCraftBrawl.battlepass.BattlePassSeason;
import anthony.SuperCraftBrawl.battlepass.BattlePassTrack;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * /battlepass main menu: pick the Free Pass or the Premium Pass.
 * Laid out like the Statistics menu: gray border, your progress at the top, options in the middle row.
 */
public class BattlePassGUI implements InventoryProvider {

	private final Core main;
	public SmartInventory inv;

	public BattlePassGUI(Core main) {
		this(main, null);
	}

	public BattlePassGUI(Core main, SmartInventory parent) {
		this.main = main;
		SmartInventory.Builder builder = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Christmas Pass");
		if (parent != null)
			builder.parent(parent);
		this.inv = builder.build();
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		BattlePassManager manager = main.getBattlePassManager();
		BattlePassProgress progress = manager == null ? null : manager.getProgress(player);
		if (progress == null)
			return; // BattlePassCommand checks this before opening

		boolean premium = manager.hasPremium(progress);

		contents.fillBorders(ClickableItem.empty(ItemHelper.getGlassFiller()));

		contents.set(0, 4, ClickableItem.empty(createProgressHead(main, player, progress, premium)));

		contents.set(2, 2, ClickableItem.of(createFreePassItem(progress), e -> {
			SoundManager.playClickSound(player);
			new BattlePassTrackGUI(main, BattlePassTrack.FREE, inv).inv.open(player);
		}));

		contents.set(2, 4, ClickableItem.empty(createXpInfoItem()));

		contents.set(2, 6, ClickableItem.of(createPremiumPassItem(progress, premium), e -> {
			SoundManager.playClickSound(player);
			new BattlePassTrackGUI(main, BattlePassTrack.PREMIUM, inv).inv.open(player);
		}));

		if (inv.getParent().isPresent()) {
			contents.set(4, 8, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"),
					e -> inv.getParent().get().open(player)));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	// ---------------------------------------------------------------- items

	/** Your tier, XP and progress to the next tier. Shared with the track menus. */
	static ItemStack createProgressHead(Core main, Player player, BattlePassProgress progress, boolean premium) {
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
			lore.add(main.color("&fSeason ends: &a" + BattlePassSeason.END.getMonthValue() + "/" + BattlePassSeason.END.getDayOfMonth()));

		return ItemHelper.createSkullHeadPlayer(1, player.getName(), main.color("&e&lChristmas Pass"), lore);
	}

	private ItemStack createFreePassItem(BattlePassProgress progress) {
		return ItemHelper.setDetails(new ItemStack(Material.IRON_INGOT),
				main.color("&e&lFree Pass"),
				main.color("&7Rewards everyone can claim,"),
				main.color("&7including the " + ClassType.Elf.getTag() + "&7, " + ClassType.GingerBreadMan.getTag()),
				main.color("&7and " + ClassType.Santa.getTag() + " &7classes!"),
				"",
				main.color("&fClaimed: &a" + progress.getClaimed(BattlePassTrack.FREE).size() + "/" + BattlePassSeason.MAX_TIER),
				main.color("&fReady to claim: &a" + countReady(progress, BattlePassTrack.FREE)),
				"",
				main.color("&aClick to view the Free Pass"));
	}

	private ItemStack createPremiumPassItem(BattlePassProgress progress, boolean premium) {
		List<String> lore = new ArrayList<>();
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

		lore.add("");
		lore.add(main.color(premium ? "&aClick to view the Premium Pass" : "&aClick to preview the Premium Pass"));

		return ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR), main.color("&e&lPremium Pass"),
				lore.toArray(new String[0]));
	}

	private ItemStack createXpInfoItem() {
		return ItemHelper.setDetails(new ItemStack(Material.PAPER),
				main.color("&e&lEarn XP"),
				main.color("&fWin a game: &a+" + BattlePassSeason.XP_WIN + " XP"),
				main.color("&fGet a kill: &a+" + BattlePassSeason.XP_KILL + " XP"),
				main.color("&fPlay a game: &a+" + BattlePassSeason.XP_GAME_PLAYED + " XP"),
				"",
				main.color("&fPremium bonus: &a+" + Math.round((BattlePassSeason.PREMIUM_XP_MULTIPLIER - 1) * 100) + "% XP"));
	}

	/** How many unlocked rewards on this track haven't been claimed yet. */
	static int countReady(BattlePassProgress progress, BattlePassTrack track) {
		if (!BattlePassSeason.isActive())
			return 0;
		int count = 0;
		for (int tier = 1; tier <= progress.getTier(); tier++) {
			if (!progress.hasClaimed(track, tier))
				count++;
		}
		return count;
	}

	static String fmt(long n) {
		return NumberFormat.getIntegerInstance(Locale.US).format(n);
	}
}
