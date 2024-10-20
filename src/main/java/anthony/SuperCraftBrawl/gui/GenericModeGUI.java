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
import fr.minuskube.inv.content.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GenericModeGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	public GameType gamemode;

	private List<Maps> maps;

	private Maps randomMap = null;
	private Maps previousRandomMap = null;

	Maps.Category[] categories = {Maps.Category.CURATED, Maps.Category.CASUAL, Maps.Category.VAULTED, null};
	private Maps.Category currentCategory = Maps.Category.CURATED;
	private Maps.Category nextCategory = Maps.Category.CASUAL;

	Maps.Gameplay[] gameplays = {Maps.Gameplay.VOIDY, Maps.Gameplay.FLAT, Maps.Gameplay.ELEVATED, Maps.Gameplay.UNDERGROUND, Maps.Gameplay.INDOOR, null};
	private Maps.Gameplay currentGameplay = null;
	private Maps.Gameplay nextGameplay = Maps.Gameplay.VOIDY;

	Maps.Size[] sizes = {Maps.Size.SMALL, Maps.Size.MEDIUM, Maps.Size.LARGE, Maps.Size.HUGE, null};
	private Maps.Size currentSize = null;
	private Maps.Size nextSize = Maps.Size.SMALL;

	Sorter[] sorters = {Sorter.ALPHABETICAL, Sorter.SIZE, Sorter.GAMEPLAY};
	private Sorter currentSorter = Sorter.ALPHABETICAL;
	private Sorter nextSorter = Sorter.SIZE;

	public int totalRows;
	public int totalColumns;

	public GenericModeGUI(Core main, SmartInventory parent, GameType gamemode, String title, int totalRows, int totalColumns) {
		this.gamemode = gamemode;
		this.totalRows = totalRows;
		this.totalColumns = totalColumns;

		inv = SmartInventory
				.builder()
				.id("myInventory")
				.provider(this)
				.size(totalRows, totalColumns)
				.title(ChatColorHelper.color("&8&l" + title))
				.parent(parent)
				.build();
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
		Pagination pagination = contents.pagination();

		// Sorting Order
		// Category (Curated, Casual, Vaulted)
		// Size (Small, Medium, Large, Huge)
		// Gameplay (Voidy, Flat, Elevated, Indoor, Underground)
		// GameState (Waiting, Empty, Full)
		// A-Z

//		List<Maps> allMaps = Arrays.asList(Maps.values());

		if (maps == null) {
			maps = Maps.filterMaps(gamemode, Maps.Category.CURATED, null, null);
			sortMaps(maps, currentSorter);
		}

		ClickableItem[] items = new ClickableItem[maps.size()];
		int i = 0;

		for (Maps map : maps) {
			GameInstance gameInstance = main.gameManager.getInstanceOfMap(map);
			items[i] = ClickableItem.of(getMapItem(map, gameInstance), event -> handleMapClick(player, map, gameInstance));
			i++;
		}

		pagination.setItems(items);
		pagination.setItemsPerPage(28);

//		// Sorting the filtered maps
//		List<Maps> sortedMaps = new ArrayList<>(filteredMaps);
//		sortedMaps.sort(getComparator());
//
//		// Looping through filtered and sorted map list and Setting items in inventory
//		for (Maps map : sortedMaps) {
//			GameInstance gameInstance = main.getGameManager().getInstanceOfMap(map);
//
//			// Checking game playersSize to set due item
//			if (gameInstance != null) {
//				int playersSize = gameInstance.players.size();
//
//				// If it
//				if (playersSize >= gamemode.getMaxPlayers()) {
//					displayItem = new ItemStack(Material.STAINED_CLAY, 1, DyeColor.RED.getData());
//				} else {
//					displayItem = new ItemStack(new ItemStack(Material.STAINED_CLAY, 1, DyeColor.YELLOW.getData()));
//				}
//			}
//
//			contents.set(rows, column, ClickableItem.of(
//					getMapItem(map, gameInstance),
//					e -> {
//						handleMapClick(player, map, gameInstance);
//						inv.close(player);
//					})
//			);
//
//			column++;
//
//			if (column > 8) {
//				rows++;
//				column = 0;
//			}
//		}

		// Setting Buttons
		// Glass Filler
		contents.fillBorders(ClickableItem.of(ItemHelper.getGlassFiller(), e-> {}));

		// Previous Page
		if (!pagination.isFirst()) {contents.set(2, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> inv.open(player, pagination.previous().getPage())));}
		// Next Page
		if (!pagination.isLast()) {contents.set(2, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> inv.open(player, pagination.next().getPage())));}

		// Randomizer
		setItem(contents, 0, 4, getRandomizerItem(), e -> handleRandomizerClick(e, player));

		// Map Category
		setItem(contents, totalRows - 1, 0, getMapCategoryItem(), e -> handleMapCategoryClick(e, player));

		// Filter
		setItem(contents, totalRows - 1, 3, getFilterItem(), e -> handleFilterClick(e, player));

		// Sorter
		setItem(contents, totalRows - 1, 5, getSorterItem(), e -> handleSortClick(e, player));

		// Setting "Go Back" Button
		contents.set(totalRows - 1, totalColumns - 1, ClickableItem.of(ItemHelper.getGoBackItem(), e -> {
			inv.close(player);
			if (inv.getParent().isPresent()) {inv.getParent().get().open(player);}
		}));

		// Maps
		SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1));
		iterator.allowOverride(false);
		pagination.addToIterator(iterator);
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

	private void updateMaps(Player player) {
		maps = Maps.filterMaps(gamemode, currentCategory, currentSize, currentGameplay);
		if (maps == null) {
			System.out.println("No maps found.");
			return;
		}
		sortMaps(maps, currentSorter);
		inv.open(player);
	}

	private void sortMaps(List<Maps> maps, Sorter sorter) {
		Comparator<Maps> sortByName = Comparator.comparing(Maps::getName);
		Comparator<Maps> sortBySize = Comparator.comparing(Maps::getSize);
		Comparator<Maps> sortByGameplay = (map1, map2) -> {
			Maps.Gameplay[] gameplay1 = map1.getGameplay();  // Assume getGameplay() returns an array
			Maps.Gameplay[] gameplay2 = map2.getGameplay();

			// Compare each gameplay type one by one, using the natural order of enums
			for (int i = 0; i < Math.min(gameplay1.length, gameplay2.length); i++) {
				int comparison = gameplay1[i].compareTo(gameplay2[i]);
				if (comparison != 0) {
					return comparison; // If a difference is found, return the comparison result
				}
			}

			// If all compared types are equal, compare by the size of the arrays (maps with fewer types come first)
			return Integer.compare(gameplay1.length, gameplay2.length);
		};

		Comparator<Maps> comparator;

		switch (sorter) {
			case ALPHABETICAL:
				comparator = sortByName;
				break;
			case SIZE:
				comparator = sortBySize;
				break;
			case GAMEPLAY:
				comparator = sortByGameplay;
				break;
			default:
				comparator = Comparator.naturalOrder();
		}

		maps.sort(comparator.thenComparing(sortByName));
	}

	private void setItem(InventoryContents contents, int row, int column, ItemStack itemStack, Consumer<InventoryClickEvent> clickHandler) {
		contents.set(row, column, ClickableItem.of(
				itemStack,
				clickHandler
		));
	}

	private ItemStack getRandomizerItem() {
		String nome = "&6Randomizer";
		String leftCLick = "&eLeft Click to randomize";
		String rightClick = "&eRight Click to join";

		ItemStack item = ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCJ9fX0=");
		String[] lore = {leftCLick, "", rightClick};

		if (randomMap != null) {
			String randomMapString = randomMap.getName();
			lore = new String[] {"&7Map: &a" + randomMapString, leftCLick, "", rightClick};
		}

		return ItemHelper.setDetails(item, nome, lore);
	}

	private ItemStack getMapCategoryItem() {
		String currentCategoryString = currentCategory == null ? "All" : currentCategory.toString();
		String nextCategoryString = nextCategory == null ? "All" : nextCategory.toString();

		return ItemHelper.setDetails(
				new ItemStack(Material.HOPPER),
				"&6Showing: &a" + currentCategoryString,
				"&7Show different map categories",
				"",
				"&7Next: &a" + nextCategoryString,
				"&eLeft click to cycle"
		);
	}

	private ItemStack getSorterItem() {
		String currentSorterString = currentSorter.toString();
		String nexSorterString = nextSorter.toString();

		return ItemHelper.setDetails(
				new ItemStack(Material.TRIPWIRE_HOOK),
				"&6Sorted by: &a" + currentSorterString,
				"&7Sort specific maps",
				"",
				"&7Next: &a" + nexSorterString,
				"&eLeft click to cycle"
		);
	}

	private ItemStack getFilterItem() {
		String currentGameplayString = currentGameplay == null ? "All" : currentGameplay.toString();
		String nextGameplayString = nextGameplay == null ? "All" : nextGameplay.toString();

		String currentSizeString = currentSize == null ? "All" : currentSize.toString();
		String nextSizeString = nextSize == null ? "All" : nextSize.toString();

		return ItemHelper.setDetails(
				new ItemStack(Material.HOPPER),
				"&6Filtering: &a" + currentGameplayString + "/" + currentSizeString,
				"&7Filter specific gameplay and sizes",
				"",
				"&7Next: &a" + nextGameplayString,
				"&eLeft click to cycle &ngameplay",
				"",
				"&7Next: &a" + nextSizeString,
				"&eRight click to cycle &nsize"
		);
	}

	private ItemStack getMapItem(Maps map, GameInstance game) {
		// Map information
		String mapName = "&e&l" + map.getName();
		ItemStack displayItem = map.getDisplayItem();
		String mapGameplay = "&7&o" + getGameplayString(map);
		String mapSize = "&7&o" + map.getSize().toString();

		// Player information
		int numberOfPlayers = (game != null) ? game.players.size() : 0;
		int maxNumberOfPlayers = (game != null) ? game.gameType.getMaxPlayers() : map.getGamemode().getMaxPlayers();
		String playersInfo = "&rPlayers: &e" + numberOfPlayers + "/" + maxNumberOfPlayers;
		String clickMessage = (game == null || game.state == GameState.WAITING) ? "&r&nClick to join!" : "&r&nClick to spectate!";

		if (game == null) {
			return ItemHelper.setDetails(displayItem, mapName, mapGameplay, mapSize, playersInfo, "", clickMessage);
		}

		ItemHelper.setGlowing(displayItem, true);

		switch (game.state) {
			case WAITING:
				String waitingTime = (game.gameStartTime != null) ?
						"&rStarting In: &e" + game.timeToStartSeconds + "s" :
						"&eWaiting for players";
				return ItemHelper.setDetails(displayItem, mapName, mapGameplay, mapSize, waitingTime, playersInfo, "", clickMessage);
			case STARTED:
				String spectatorsInfo = "&rSpectators: &e" + game.spectators.size();
				String gameTime = "&rGame Time: &e" + game.gameTime + "m";
				return ItemHelper.setDetails(displayItem, mapName, mapGameplay, mapSize, "&aIn Progress", playersInfo, spectatorsInfo, gameTime, "", clickMessage);
			default:
				return null;
		}
	}

	private String getGameplayString(Maps map) {
		Maps.Gameplay[] gameplays = map.getGameplay();
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < gameplays.length; i++) {
			stringBuilder.append(gameplays[i].toString());

			if (i < gameplays.length - 1) {
				stringBuilder.append(", ");
			}
		}
		return stringBuilder.toString();
	}

	private void handleRandomizerClick(InventoryClickEvent e, Player player) {
		if (!e.isLeftClick() && !e.isRightClick()) {
			return;
		}

		// Randomize Map
		if (e.isLeftClick()) {
			if (maps.size() == 0) return;
			randomizeMap();
			updateMaps(player);
		}
		// Join Map
		else if (e.isRightClick()) {
			if (randomMap == null) {
				randomizeMap();
			}
			handleMapClick(player, randomMap, main.gameManager.getInstanceOfMap(randomMap));
		}
	}

	private void handleSortClick(InventoryClickEvent e, Player player) {
		if (!e.isLeftClick() && !e.isRightClick()) {
			return;
		}

		if (e.isLeftClick()) {
			int currentIndex = Arrays.asList(sorters).indexOf(currentSorter);
			currentSorter = sorters[(currentIndex + 1) % sorters.length];
			nextSorter = sorters[(currentIndex + 2) % sorters.length];
		}
		if (e.isRightClick()) {

		}
		updateMaps(player);
	}

	private void handleFilterClick(InventoryClickEvent e, Player player) {
		if (!e.isLeftClick() && !e.isRightClick()) {
			return;
		}

		if (e.isLeftClick()) {
			int currentIndex = Arrays.asList(gameplays).indexOf(currentGameplay);
			currentGameplay = gameplays[(currentIndex + 1) % gameplays.length];
			nextGameplay = gameplays[(currentIndex + 2) % gameplays.length];
		} else if (e.isRightClick()) {
			int currentIndex = Arrays.asList(sizes).indexOf(currentSize);
			currentSize = sizes[(currentIndex + 1) % sizes.length];
			nextSize = sizes[(currentIndex + 2) % sizes.length];
		}
		updateMaps(player);
	}

	private void handleMapCategoryClick(InventoryClickEvent e, Player player) {
		if (!e.isLeftClick() && !e.isRightClick()) {
			return;
		}

		int currentIndex = Arrays.asList(categories).indexOf(currentCategory);
		currentCategory = categories[(currentIndex + 1) % categories.length];
		nextCategory = categories[(currentIndex + 2) % categories.length];
		updateMaps(player);
	}

	private void handleMapClick(Player player, Maps map, GameInstance game) {
		GameState state = (game != null) ? game.state : null;

		if (state == GameState.STARTED) {
			main.getGameManager().SpectatorJoinMap(player, map);
		} else if (state != GameState.ENDED) {
			main.getGameManager().JoinMap(player, map);
		}
	}

	private void randomizeMap() {
		List<Maps> availableMaps = maps.stream()
				.filter(map -> {
					GameInstance game = main.getGameManager().getInstanceOfMap(map);
					return !map.equals(previousRandomMap) && (game == null || !(game.state.equals(GameState.STARTED) || game.state.equals(GameState.ENDED)));
				})
				.collect(Collectors.toList());

		if (availableMaps.size() == 0) return;

		Random random = new Random();
		int randomInt = random.nextInt(availableMaps.size());
		randomMap = availableMaps.get(randomInt);

		previousRandomMap = randomMap;
	}

	private Comparator<? super Maps> getComparator() {
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

		return gameComparator;
	}

	private enum Sorter {
		ALPHABETICAL,
		SIZE,
		GAMEPLAY,
		STATE;

		@Override
		public String toString() {
			switch (this) {
				case ALPHABETICAL:
					return "A-Z";
				case SIZE:
					return "Size";
				case GAMEPLAY:
					return "Gameplay";
				case STATE:
					return "State";
			}
			return "Unknown Sort";
		}
	}
}
