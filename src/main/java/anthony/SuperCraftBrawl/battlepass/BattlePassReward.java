package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A single reward on a Battle Pass tier. Use the static factories to build them.
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

	public ItemStack createIcon() {
		return new ItemStack(icon, Math.max(1, Math.min(64, iconAmount)), iconData);
	}

	public String getName() {
		return name;
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

	/** Unlocks a class. If they somehow already own it, they get tokens instead. */
	public static BattlePassReward classUnlock(ClassType type, Material icon, int fallbackTokens) {
		String className = type.getTag();
		return new BattlePassReward(icon, (short) 0, 1, className + " &7Class",
				(main, player, data) -> {
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
				});
	}
}
