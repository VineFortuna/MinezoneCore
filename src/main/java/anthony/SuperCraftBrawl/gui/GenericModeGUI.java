package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GenericModeGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	public GameType gameType;
	public int totalRows;
	public int totalColumns;

	public GenericModeGUI(Core main, GameType gameType, String title, int totalRows, int totalColumns) {
		this.gameType = gameType;
		this.totalRows = totalRows;
		this.totalColumns = totalColumns;

		inv = SmartInventory.builder().id("myInventory").provider(this).size(totalRows, totalColumns)
				.title(ChatColorHelper.color("&8&l" + title)).build();
		this.main = main;
	}

	/**
	 *
	 * Creates a Comparator to sort games by wanted order. Order: 1. Waiting Games
	 * (Lobby with at least a player on it) a. Number of players b. Map name 2.
	 * Started Games (Spectate) a. Map Name 3. Lobby Games (Lobby with 0 players on
	 * it) a. Map Name
	 *
	 */
	@Override
	public void init(Player player, InventoryContents contents) {
		int rows = 0;
		int column = 0;

		// Filtering maps on gameType
		List<Maps> filteredMaps = Maps.getGameType(gameType);

		// Comparator to sort maps through gameInstances and gameState
		Comparator<Maps> gameComparator = (map1, map2) -> {
			GameInstance gameInstance1 = main.getGameManager().getInstanceOfMap(map1);
			GameInstance gameInstance2 = main.getGameManager().getInstanceOfMap(map2);

			// If both maps have gameInstances, compare gameState then number of players
			if (gameInstance1 != null && gameInstance2 != null) {
				int gameStateComparison = Integer.compare(gameInstance1.state == GameState.WAITING ? 0 : 1,
						gameInstance2.state == GameState.WAITING ? 0 : 1);
				// If gameState is different, return the result of comparison between gameStates
				if (gameStateComparison != 0) {
					return gameStateComparison;
				}
				// If gameState is the same, compare by number of players
				return Integer.compare(gameInstance1.players.size(), gameInstance2.players.size());

			} else if (gameInstance1 != null) {
				// map1 has a gameInstance, so it comes first
				return -1;
			} else if (gameInstance2 != null) {
				// map2 has a gameInstance, so it comes first
				return 1;
			} else {
				// Lobby with 0 players maps
				// Sorting by name
				return map1.name().compareTo(map2.name());
			}
		};

		// Sorting the filtered maps
		List<Maps> sortedMaps = new ArrayList<>(filteredMaps);
		sortedMaps.sort(gameComparator);

		// Looping through filtered and sorted map list and Setting items in inventory
		for (Maps map : sortedMaps) {
			GameInstance gameInstance = main.getGameManager().getInstanceOfMap(map);
			String mapName = map.toString();

			ItemStack displayItem = null;

			// Checking game playersSize to set due item
			if (gameInstance != null) {
				int playersSize = gameInstance.players.size();

				// If it
				if (playersSize >= gameType.getMaxPlayers()) {
					displayItem = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.RED.getData());
				} else {
					displayItem = new ItemStack(new ItemStack(Material.STAINED_CLAY, 1, DyeColor.YELLOW.getData()));
				}
			}

			if (gameInstance != null) {
				// If game is waiting for players (have not started and have at least 1 player
				// in it)
				if (gameInstance.state == GameState.WAITING) {
					// If start time initiated
					if (gameInstance.gameStartTime != null) {
						contents.set(rows, column, ClickableItem.of(
								ItemHelper.setDetails(displayItem, "&e&l" + mapName,
										"&rStarting In: &e" + gameInstance.ticksTilStart + "s",
										"&rPlayers: &e" + gameInstance.players.size() + "/"
												+ gameInstance.gameType.getMaxPlayers(),
										"", "&r&nClick to join!"),
								e -> {
									main.getGameManager().JoinMap(player, map);
									inv.close(player);
								}));
					} else {
						// If start time has not initiated
						contents.set(rows, column, ClickableItem.of(
								ItemHelper.setDetails(displayItem, "&e&l" + mapName, "&eWaiting for players",
										"&rPlayers: &e" + gameInstance.players.size() + "/"
												+ gameInstance.gameType.getMaxPlayers(),
										"", "&r&nClick to join!"),
								e -> {
									main.getGameManager().JoinMap(player, map);
									inv.close(player);
								}));
					}
				} else if (gameInstance.state == GameState.STARTED) {
					String state = "In Progress";
					contents.set(rows, column, ClickableItem.of(
							ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER), "&e&l" + mapName, "&a" + state,
									"&rPlayers: &e" + gameInstance.players.size() + "/"
											+ gameInstance.gameType.getMaxPlayers(),
									"&rSpectators: &e" + gameInstance.spectators.size(),
									"&rGame Time: &e" + gameInstance.gameTime + "m", "", "&r&nClick to spectate!"),
							e -> {
								main.getGameManager().SpectatorJoinMap(player, map);
								inv.close(player);
							}));
				}
			} else {
				// Set item for null gameInstances (no players in the map)
				contents.set(rows, column, ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.STAINED_CLAY, 1, DyeColor.LIGHT_BLUE.getData()),
								"&e&l" + mapName, "&rPlayers: &e0/" + gameType.getMaxPlayers(), "&r&nClick to join!"),
						e -> {
							main.getGameManager().JoinMap(player, map);
							inv.close(player);
						}));
			}

			column++;

			if (column > 8) {
				rows++;
				column = 0;
			}
		}

		// Setting "Go Back" Button
		contents.set(totalRows - 1, totalColumns - 1,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.ARROW), "&7Go back"), e -> {
					inv.close(player);
					new GameSelectorGUI(main).inv.open(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
