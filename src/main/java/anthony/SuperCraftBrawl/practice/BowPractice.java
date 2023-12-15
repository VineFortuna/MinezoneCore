package anthony.SuperCraftBrawl.practice;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class BowPractice {

	public List<Player> players;
	private List<Integer> playerMaps;

	public BowPractice() {
		this.players = new ArrayList<Player>();
		this.playerMaps = new ArrayList<Integer>();
	}

	public void addPlayer(Player player) {
		if (!(hasPlayer(player))) {
			sendToMap(player);
			return;
		}

		player.sendMessage(color("&c&l(!) &rYou are already in a game!"));
	}

	@SuppressWarnings("deprecation")
	private void sendToMap(Player player) {
		Location loc = null;

		for (int i = 0; i < 5000; i += 100) {
			if (!(this.playerMaps.contains(i))) {
				this.playerMaps.add(i);
				this.players.add(player);
				loc = new Location(player.getWorld(), i, 100, 0);
				player.teleport(loc);
				player.sendMessage(color("&2&l(!) &rYou have joined &r&lBow Practice"));
				return;
			}
		}

		player.sendMessage(color("&c&l(!) &rLEVEL BUSY! Please try again soon"));
		player.sendTitle(color("&rLEVEL BUSY!"), "Please try again soon!");
	}

	public boolean hasPlayer(Player player) {
		return this.players.contains(player);
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
