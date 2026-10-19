package anthony.SuperCraftBrawl.gui;

import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ActiveGamesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ActiveGamesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title(ChatColorHelper.color("&8&lQuick Join")).build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int count = 0;
		int i = 0;

		if (main.getGameManager().getNumOfGames() == 0) {
			contents.set(1, 4, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.BARRIER), main.color("&c&lNo games!"),
							main.color("&7Start a game by using"), main.color("&7the Game Selector")),
					e -> {

					}));
			return;
		}

		for (GameInstance game : new ArrayList<>(main.getGameManager().games)) {
			Maps map = game.getMap();
			String mapName = map.getName();
			ItemStack displayItem = map.getDisplayItem();
			GameType gameType = game.gameType;
			// Custom games show how players get their class (Classic, Frenzy, Class Switch or One For All)
			String mode = game.isCustomGame() ? game.getClassSelectionName() : gameType.getName();
			if (game.isCustomGame()) mode += game.isCustomGamePublic() ? " &6(Custom Game)" : " &c(Private Custom Game)";

			System.out.println(mode);

			if (game.state == GameState.WAITING) {
				if (game.gameStartTime != null) {
					contents.set(count, i, ClickableItem.of(
							ItemHelper.setDetails(displayItem,
									"&e&l" + mapName,
									"&fMode: &a" + mode,
									"",
									"&fStarting In: &a" + game.timeToStartSeconds + "s",
									"&fPlayers: &a" + game.players.size() + "/"
											+ game.getMaxPlayerCount(),
									"", "&r&nClick to join!"),
							e -> {
								main.getGameManager().JoinGame(player, game);
								inv.close(player);
							}));
				} else {
					contents.set(count, i, ClickableItem.of(
							ItemHelper.setDetails(displayItem,
									"&e&l" + mapName,
									"&fMode: &a" + mode,
									"",
									"&fWaiting for Players",
									"&fPlayers: &a" + game.players.size() + "/"
											+ game.getMaxPlayerCount(),
									"", "&r&nClick to join!"),
							e -> {
								main.getGameManager().JoinGame(player, game);
								inv.close(player);
							}));
				}
			} else if (game.state == GameState.STARTED) {
				String state = "In Progress";
				contents.set(count, i, ClickableItem.of(
						ItemHelper.setDetails(ItemHelper.setGlowing(displayItem, true),
								"&e&l" + mapName,
								"&fMode: &a" + mode,
								"",
								"&f" + state,
								"&fPlayers: &a" + game.players.size() + "/"
										+ game.getMaxPlayerCount(),
								"&fSpectators: &a" + game.spectators.size(), "", "&r&nClick to spectate!"),
						e -> {
							main.getGameManager().SpectatorJoinGame(player, game);
							inv.close(player);
						}));
			}
			if (i > 8) {
				count++;
				i = 0;
			}
			i++;
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
