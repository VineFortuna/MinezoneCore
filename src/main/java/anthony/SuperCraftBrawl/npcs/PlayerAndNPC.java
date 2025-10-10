package anthony.SuperCraftBrawl.npcs;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.gui.halloween.HalloweenNpcGUI;
import net.md_5.bungee.api.ChatColor;

public class PlayerAndNPC {

	private Core core;
	private int clicks = 0;

	public PlayerAndNPC(Core core, Player player) {
		this.core = core;
		this.clicks = 0;
	}

	public void sendMessage(Player player) {
		int progress = core.getListener().getHalloweenEventProgress(player);
		if (progress == 0) {
			if (clicks == 0) {
				clicks++;
				player.playSound(player.getLocation(), Sound.VILLAGER_IDLE, 1, 1);
				player.sendMessage(color("&6&l[HUNT] &rWelcome to the Minezone Halloween 2025 Hunt! &7[1/3]"));
			} else if (clicks == 1) {
				clicks++;
				player.playSound(player.getLocation(), Sound.VILLAGER_IDLE, 1, 1);
				player.sendMessage(color(
						"&6&l[HUNT] &rThere are 10 Halloween baskets scattered around the lobby, you need to find them to unlock some cool rewards! (I love FNAF) &7[2/3]"));
			} else if (clicks == 2) {
				clicks++;
				player.sendMessage(color("&6&l[HUNT] &rI think you're ready.. Freddy is waiting for you! &7[3/3]"));
				player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);
				new HalloweenNpcGUI(core).inv.open(player);
			} else
				new HalloweenNpcGUI(core).inv.open(player);
		} else 
			new HalloweenNpcGUI(core).inv.open(player);
		
		if (progress == 10)
			player.sendMessage(color("&6&l[HUNT] &rYou finished the hunt already! What more do you want?"));
	}

	public String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}

}
