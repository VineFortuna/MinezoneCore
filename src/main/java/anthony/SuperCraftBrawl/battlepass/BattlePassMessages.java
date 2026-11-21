package anthony.SuperCraftBrawl.battlepass;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ChatCenter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

/**
 * Chat messages for the Battle Pass. Everything is centered and framed with a red and white
 * candy cane stripe so it stands out from normal chat.
 */
final class BattlePassMessages {

	/** 16 blocks of 4 struck-through spaces, alternating red and white. */
	private static final String STRIPE = buildStripe(16);

	private BattlePassMessages() {
	}

	private static String buildStripe(int blocks) {
		StringBuilder stripe = new StringBuilder();
		for (int i = 0; i < blocks; i++)
			stripe.append(i % 2 == 0 ? "&c&m    " : "&f&m    ");
		return stripe.toString();
	}

	/** The big announcement when a player reaches a new tier. */
	static void sendTierUp(Core main, Player player, int tier, boolean premium) {
		boolean finalTier = tier >= BattlePassSeason.MAX_TIER;

		player.sendMessage("");
		player.sendMessage(centered(main, STRIPE));
		player.sendMessage("");
		player.sendMessage(centered(main, "&c&lCHRISTMAS &f&lPASS"));
		player.sendMessage(centered(main, finalTier ? "&6&lFINAL TIER UNLOCKED" : "&a&lTIER " + tier + " UNLOCKED"));
		player.sendMessage("");

		// What this tier gives, so they know whether it's worth opening the menu right now
		player.sendMessage(centered(main, "&a&lFREE  " + BattlePassRewards.get(BattlePassTrack.FREE, tier).getChatName(main, player)));
		String premiumReward = premium
				? BattlePassRewards.get(BattlePassTrack.PREMIUM, tier).getChatName(main, player)
				: "&8Locked";
		player.sendMessage(centered(main, "&6&lPREMIUM  " + premiumReward));
		player.sendMessage("");

		sendClaimButton(main, player);
		player.sendMessage("");
		player.sendMessage(centered(main, STRIPE));
		player.sendMessage("");
	}

	/** A clickable button that runs /battlepass. */
	private static void sendClaimButton(Core main, Player player) {
		String label = main.color("&2&l[ &a&lCLAIM REWARDS &2&l]");

		TextComponent line = new TextComponent(ChatCenter.padding(label));
		TextComponent button = new TextComponent(label);
		button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/battlepass"));
		button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new TextComponent[] { new TextComponent(main.color("&7Click to open the &c&lChristmas &f&lPass")) }));
		line.addExtra(button);

		player.spigot().sendMessage(line);
	}

	private static String centered(Core main, String text) {
		return ChatCenter.center(main.color(text));
	}
}
