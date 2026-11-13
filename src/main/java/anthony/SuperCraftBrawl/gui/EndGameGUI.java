package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;

/*
 * The host of a custom game that kept running without them (Red Dye while spectating it) ends it for everyone,
 * so the players can't hold the game hostage.
 */
public class EndGameGUI extends ConfirmationGUI {

	public EndGameGUI(Core main, GameInstance game) {
		super(main, "End game?", player -> {
			player.closeInventory();

			// The game may have finished by itself while the menu was open
			if (!main.getGameManager().games.contains(game) || game.state != GameState.STARTED
					|| !game.isCustomGameHost(player)) {
				player.sendMessage(main.color("&c&l(!) &rThat game already ended."));
				return;
			}

			main.getGameManager().endPlayingCustomGame(game, player);
		}, null);
	}
}
