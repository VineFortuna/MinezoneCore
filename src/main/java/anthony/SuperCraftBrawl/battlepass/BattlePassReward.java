package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single reward on a Battle Pass tier. Use the static factories to build them.
 * Icon, name and description are resolved per player (not stored) so cosmetic rewards can take
 * them straight from the cosmetic itself.
 */
public class BattlePassReward {

	public interface Grant {
		void grant(Core main, Player player, PlayerData data);
	}

	private final Material icon;
	private final short iconData;
	private final int iconAmount;
	private final String name;
	private final Grant grant;

	public BattlePassReward(Material icon, short iconData, int iconAmount, String name, Grant grant) {
		this.icon = icon;
		this.iconData = iconData;
		this.iconAmount = iconAmount;
		this.name = name;
		this.grant = grant;
	}

	public ItemStack createIcon(Core main, Player player) {
		return new ItemStack(icon, Math.max(1, Math.min(64, iconAmount)), iconData);
	}

	/** Raw name with & color codes. */
	public String getName(Core main, Player player) {
		return name;
	}

	/** The name as the tier up chat message says it: anything that has a type (a Title, a Class...) says it too. */
	public String getChatName(Core main, Player player) {
		return getName(main, player);
	}

	/** Extra lines shown under the tier line in the GUI (raw & color codes). */
	public List<String> getDescription(Core main, Player player) {
		return Collections.emptyList();
	}

	/** True if claiming this reward unlocks that exact cosmetic. */
	public boolean grantsCosmetic(CosmeticCategory category, String cosmeticId) {
		return false;
	}

	public void grant(Core main, Player player, PlayerData data) {
		grant.grant(main, player, data);
	}

	// ---------------------------------------------------------------- factories

	public static BattlePassReward tokens(int amount) {
		return new BattlePassReward(Material.GOLD_NUGGET, (short) 0, 1, "&e" + amount + " Tokens",
				(main, player, data) -> {
					data.tokens += amount;
					player.sendMessage(main.color("&aYou received &e" + amount + " Tokens&a!"));
				});
	}

	public static BattlePassReward mysteryChests(int amount) {
		return new BattlePassReward(Material.ENDER_CHEST, (short) 0, amount,
				"&d" + amount + " Mystery Chest" + (amount == 1 ? "" : "s"),
				(main, player, data) -> {
					data.mysteryChests += amount;
					player.sendMessage(main.color("&aYou received &d" + amount + " Mystery Chest" + (amount == 1 ? "" : "s") + "&a!"));
				});
	}

	public static BattlePassReward levels(int amount) {
		return new BattlePassReward(Material.EXP_BOTTLE, (short) 0, amount,
				"&b" + amount + " Level" + (amount == 1 ? "" : "s"),
				(main, player, data) -> {
					data.level += amount;
					player.sendMessage(main.color("&aYou gained &b" + amount + " Level" + (amount == 1 ? "" : "s") + "&a!"));
				});
	}

	/**
	 * Unlocks a class. If they somehow already own it, they get tokens instead. The icon is the class's menu icon,
	 * and "Class" and the class description are the lore.
	 */
	public static BattlePassReward classUnlock(ClassType type, int fallbackTokens) {
		String className = type.getTag();
		Grant grant = (main, player, data) -> {
			ClassDetails details = data.playerClasses.get(type.getID());
			if (details == null) {
				details = new ClassDetails();
				data.playerClasses.put(type.getID(), details);
			}

			if (details.purchased) {
				data.tokens += fallbackTokens;
				player.sendMessage(main.color("&aYou already own " + className + "&a, so you received &e"
						+ fallbackTokens + " Tokens &ainstead!"));
				return;
			}

			details.setPurchased();
			player.sendMessage(main.color("&aYou unlocked the " + className + " &aClass!"));
		};

		return new BattlePassReward(Material.PAPER, (short) 0, 1, className, grant) {
			@Override
			public ItemStack createIcon(Core main, Player player) {
				return type.getItem().clone();
			}

			@Override
			public String getChatName(Core main, Player player) {
				return className + " &7Class";
			}

			@Override
			public List<String> getDescription(Core main, Player player) {
				List<String> lines = new ArrayList<>();
				lines.add("&7Class");
				List<String> description = type.buildDescription();
				if (description != null) {
					lines.add("");
					lines.addAll(description);
				}
				return lines;
			}
		};
	}

	/** Permanently unlocks a cosmetic that is registered with the CosmeticRegistry. */
	public static BattlePassReward cosmetic(CosmeticCategory category, String cosmeticId) {
		return new CosmeticReward(category, cosmeticId);
	}

	/** Several rewards on one tier. */
	public static BattlePassReward bundle(BattlePassReward... rewards) {
		return new BundleReward(rewards);
	}
}
