package anthony.SuperCraftBrawl.gui.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.battlepass.BattlePassProgress;
import anthony.SuperCraftBrawl.battlepass.BattlePassSeason;
import anthony.util.ItemHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Items every Battle Pass menu shares (the hub, the pass itself and the quests). */
final class BattlePassMenuItems {

	private BattlePassMenuItems() {
	}

	/** Your tier, XP and progress to the next tier. Goes in the top center of every Battle Pass menu. */
	static ItemStack progressHead(Core main, Player player, BattlePassProgress progress, boolean premium) {
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

	static String fmt(long n) {
		return NumberFormat.getIntegerInstance(Locale.US).format(n);
	}
}
