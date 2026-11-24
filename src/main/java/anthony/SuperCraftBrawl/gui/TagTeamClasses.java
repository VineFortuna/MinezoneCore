package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * What the class menus do while the player waits in a Tag Team lobby: left click picks the 1st class, right click the
 * 2nd, and the two they picked glow and say which they are. Anywhere else the menus work as always (right click views
 * the class's mastery).
 */
public final class TagTeamClasses {

	private TagTeamClasses() {
	}

	/** The player's game, if they're waiting in a Tag Team lobby. */
	private static GameInstance lobby(Core main, Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
		return game != null && game.state == GameState.WAITING && game.isTagTeam() ? game : null;
	}

	/**
	 * Adds the click instructions to a class's lore (the ones for choosing and for viewing the mastery). In a Tag Team
	 * lobby they're about the 1st and 2nd class instead, and a class the player picked says so at the top.
	 */
	public static void addChooseLines(Core main, Player player, ClassType type, List<String> lore) {
		GameInstance game = lobby(main, player);

		if (game == null) {
			lore.add(main.color("&e&nLeft Click&r &eto choose a class"));
			lore.add(main.color("&e&nRight Click&r &eto view mastery"));
			return;
		}

		if (game.classSelection.get(player) == type) {
			lore.add(0, "");
			lore.add(0, main.color("&a&l1st Class Selected"));
		} else if (game.getSecondClass(player) == type) {
			lore.add(0, "");
			lore.add(0, main.color("&a&l2nd Class Selected"));
		}

		lore.add(main.color("&e&nLeft Click&r &eto choose 1st class"));
		lore.add(main.color("&e&nRight Click&r &eto choose 2nd class"));
	}

	/** A class the player picked in a Tag Team lobby glows. */
	public static ItemStack glowIfPicked(Core main, Player player, ClassType type, ItemStack item) {
		GameInstance game = lobby(main, player);
		if (game == null) return item;

		boolean picked = game.classSelection.get(player) == type || game.getSecondClass(player) == type;
		return picked ? ItemHelper.setGlowing(item, true) : item;
	}

	/**
	 * The player clicked this class to make it their 1st (slot 1) or 2nd (slot 2) one. Returns false if they aren't in a
	 * Tag Team lobby, so the click does what it does anywhere else.
	 */
	public static boolean pick(Core main, Player player, ClassType type, int slot) {
		GameInstance game = lobby(main, player);
		if (game == null) return false;

		if (slot == 1) {
			game.setClass(player, type);
		} else if (!game.setSecondClass(player, type)) {
			return true; // they were told why
		}

		player.sendMessage(main.color("&2&l(!) &rYou have selected the " + type.getTag() + "&f class as your "
				+ (slot == 1 ? "1st" : "2nd") + " class!"));
		player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
		return true;
	}
}
