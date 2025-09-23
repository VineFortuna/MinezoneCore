package anthony.SuperCraftBrawl.practice;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;

public class SCBPractice {

	public Map<Player, Game> game;
	public BowPractice bowPractice;
	private Core core;
	
	public SCBPractice(Player player, Game game, Core core) {
		this.game = new HashMap<Player, Game>();
		this.game.put(player, game);
		this.core = core;
		checkMode(player);
	}

	public void checkMode(Player player) {
		if (this.game.get(player) == Game.BowPractice) {
			if (this.bowPractice == null) {
				this.bowPractice = new BowPractice();
				this.bowPractice.addPlayer(player, core);
			} else {
				this.bowPractice.addPlayer(player, core);
			}
		}
	}
}
