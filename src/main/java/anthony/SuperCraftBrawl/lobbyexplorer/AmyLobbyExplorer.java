package anthony.SuperCraftBrawl.lobbyexplorer;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class AmyLobbyExplorer {

	private int clicks;
	private int maxClicks = 2;

	public AmyLobbyExplorer() {
		this.clicks = 0;
	}

	public void sendMessage(Player player) {
		this.clicks++;

		if (clicks == 1) {
			player.sendMessage(color(
					"&2[Amy] &rWelcome to Minezone! We have a lot to offer around the lobby. Click again for more info &7["
							+ this.clicks + "/" + this.maxClicks + "]"));
		} else {
			player.sendMessage("Test");
		}
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
