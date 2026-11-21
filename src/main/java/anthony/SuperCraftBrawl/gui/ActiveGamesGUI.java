package anthony.SuperCraftBrawl.gui;

import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWGameState;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.SmartInvsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

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

		boolean noScbGames = main.getGameManager().getNumOfGames() == 0;
		boolean noFwGames = main.getFwGameManager().getActiveInstances().isEmpty();

		if (noScbGames && noFwGames) {
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
			if (game.isCustomGame()) mode += " &6(" + game.getCustomGameVisibility().getTag() + " &6Custom Game)";

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
							e -> joinClicked(player, game, false)));
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
							e -> joinClicked(player, game, false)));
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
						e -> joinClicked(player, game, true)));
			}
			if (i > 8) {
				count++;
				i = 0;
			}
			i++;
		}

		for (FWGameInstance fwGame : main.getFwGameManager().getActiveInstances()) {
			ItemStack displayItem = ItemHelper.createFlagWarsBanner();
			int maxPlayers = fwGame.getMaxPlayers();

			if (fwGame.getState() == FWGameState.WAITING || fwGame.getState() == FWGameState.STARTING) {
				String startLine = fwGame.getState() == FWGameState.STARTING
						? "&fStarting In: &a" + fwGame.getCountdownSecondsLeft() + "s"
						: "&fWaiting for Players";

				contents.set(count, i, ClickableItem.of(
						ItemHelper.setDetails(displayItem,
								"&e&lFlag Wars",
								"&fMap: &a" + fwGame.getMapName(),
								"&fMode: &a" + fwGame.getMode().getLabel(),
								"",
								startLine,
								"&fPlayers: &a" + fwGame.countPlayers() + "/" + maxPlayers,
								"", "&r&nClick to join!"),
						e -> {
							main.getFwGameManager().joinGame(player, fwGame.getMode());
							inv.close(player);
						}));
			} else if (fwGame.getState() == FWGameState.IN_PROGRESS) {
				contents.set(count, i, ClickableItem.of(
						ItemHelper.setDetails(ItemHelper.setGlowing(displayItem, true),
								"&e&lFlag Wars",
								"&fMap: &a" + fwGame.getMapName(),
								"&fMode: &a" + fwGame.getMode().getLabel(),
								"",
								"&fIn Progress",
								"&fPlayers: &a" + fwGame.countPlayers() + "/" + maxPlayers,
								"&fSpectators: &a" + fwGame.getSpectators().size(), "", "&r&nClick to spectate!"),
						e -> {
							main.getFwGameManager().spectateGame(player, fwGame);
							inv.close(player);
						}));
			} else {
				continue; // ENDING - mid-reset, don't list it
			}

			if (i > 8) {
				count++;
				i = 0;
			}
			i++;
		}
	}

	/** A custom game the player can't access plays the error sound, says why and leaves the menu open. */
	private void joinClicked(Player player, GameInstance game, boolean spectate) {
		if (game.isCustomGame() && !game.canAccessCustomGame(player)) {
			SoundManager.playErrorSound(player);
			player.sendMessage(game.getCustomGameAccessMessage());
			return;
		}

		if (spectate) main.getGameManager().SpectatorJoinGame(player, game);
		else main.getGameManager().JoinGame(player, game);
		inv.close(player);
	}

	/** Reopens every player's open Quick Join menu, so it shows the latest games (e.g. a custom game's visibility changed). */
	public static void refreshOpenMenus() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Optional<SmartInventory> open = SmartInvsPlugin.manager().getInventory(player);
			if (open.isPresent() && open.get().getProvider() instanceof ActiveGamesGUI) open.get().open(player);
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
