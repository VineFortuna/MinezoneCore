package anthony.SuperCraftBrawl.practice;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;

public class SCBPractice {

	public Map<Player, Game> game;
	
	public SCBPractice(Player player, Game game) {
		this.game = new HashMap<Player, Game>();
		this.game.put(player, game);
		checkMode(player);
	}
	
	
	public void checkMode(Player player) {
		if (this.game.get(player) == Game.BowPractice)
			Core.bowPractice.addPlayer(player);
	}
}
