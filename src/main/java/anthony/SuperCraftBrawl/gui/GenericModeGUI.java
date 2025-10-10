package anthony.SuperCraftBrawl.gui;

import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.*;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
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

	Maps.Category[] categories = {Maps.Category.CURATED, Maps.Category.CASUAL, Maps.Category.VAULTED, Maps.Category.HALLOWEEN, null};
	private Maps.Category currentCategory = Maps.Category.CURATED;
	private Maps.Category nextCategory = Maps.Category.CASUAL;

	Maps.Gameplay[] gameplays = {Maps.Gameplay.VOIDY, Maps.Gameplay.FLAT, Maps.Gameplay.ELEVATED, Maps.Gameplay.UNDERGROUND, Maps.Gameplay.INDOOR, null};
	private Maps.Gameplay currentGameplay = null;
	private Maps.Gameplay nextGameplay = Maps.Gameplay.VOIDY;

	Maps.Size[] sizes = {Maps.Size.SMALL, Maps.Size.MEDIUM, Maps.Size.LARGE, Maps.Size.HUGE, null};
	private Maps.Size currentSize = null;
	private Maps.Size nextSize = Maps.Size.SMALL;

	Maps.Sorter[] sorters = {Maps.Sorter.ALPHABETICAL, Maps.Sorter.SIZE, Maps.Sorter.GAMEPLAY};
	private Maps.Sorter currentSorter = Maps.Sorter.ALPHABETICAL;
	private Maps.Sorter nextSorter = Maps.Sorter.SIZE;

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

	@Override
	public void init(Player player, InventoryContents contents) {
		Pagination pagination = contents.pagination();

		if (maps == null) {
			maps = Maps.filterMaps(gamemode, Maps.Category.CURATED, null, null);
			Maps.sortMaps(maps, currentSorter);
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

		// Setting Buttons
		// Glass Filler
		contents.fillBorders(ClickableItem.of(ItemHelper.getGlassFiller(), e-> {}));

		// Previous Page
		if (!pagination.isFirst()) {contents.set(2, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> {
			SoundManager.playClickSound(player);
			inv.open(player, pagination.previous().getPage());
		}));}
		// Next Page
		if (!pagination.isLast()) {contents.set(2, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> {
			SoundManager.playClickSound(player);
			inv.open(player, pagination.next().getPage());
		}));}

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
			SoundManager.playClickSound(player);
			inv.close(player);
			if (inv.getParent().isPresent()) {inv.getParent().get().open(player);}
		}));

		// Maps
		SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1));
		iterator.allowOverride(false);
		pagination.addToIterator(iterator);
	}

	private void updateMaps(Player player) {
		SoundManager.playClickSound(player);
		maps = Maps.filterMaps(gamemode, currentCategory, currentSize, currentGameplay);
		if (maps == null) {
			System.out.println("No maps found.");
			return;
		}
		Maps.sortMaps(maps, currentSorter);
		inv.open(player);
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

		Material material = null;

		if (currentCategory == null) {
			material = Material.RECORD_9;
		} else {
			switch (currentCategory) {
				case CURATED:
					material = Material.RECORD_4;
					break;
				case CASUAL:
					material = Material.RECORD_12;
					break;
				case VAULTED:
					material = Material.RECORD_11;
					break;
				case HALLOWEEN:
					material = Material.RECORD_7;
					break;
			}
		}



		ItemStack item = ItemHelper.setDetails(
				new ItemStack(material),
				"&6Showing: &a" + currentCategoryString,
				"&7Show different map categories",
				"",
				"&7Next: &a" + nextCategoryString,
				"&eLeft click to cycle"
		);

		hideDiscInformation(item);
		
		return item;
	}

	private ItemStack getSorterItem() {
		String currentSorterString = currentSorter.toString();
		String nexSorterString = nextSorter.toString();

		return ItemHelper.setDetails(
				new ItemStack(Material.HOPPER_MINECART),
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
			ItemHelper.setGlowing(displayItem, false);
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
				String gameTime = "&rGame Time: &e" + game.gameTime + "m";
				String spectatorsInfo = "&rSpectators: &e" + game.spectators.size();
				return ItemHelper.setDetails(displayItem, mapName, mapGameplay, mapSize, "&aIn Progress", playersInfo, gameTime, spectatorsInfo, "", clickMessage);
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

				if (randomMap == null) {
					return;
				}
			}

			GameInstance game = main.getGameManager().getInstanceOfMap(randomMap);

			handleMapClick(player, randomMap, game);
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

	private void hideDiscInformation(org.bukkit.inventory.ItemStack item) {
		// Convert the Bukkit ItemStack to NMS ItemStack
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

		// Check if the item already has NBT data (tag)
		NBTTagCompound tag;
		if (!nmsItem.hasTag()) {
			tag = new NBTTagCompound();  // Create a new tag if none exists
		} else {
			tag = nmsItem.getTag();  // Get the existing tag
		}

		// Set the HideFlags tag to 32 (which hides the song info)
		tag.setInt("HideFlags", 32);

		// Apply the modified tag back to the NMS ItemStack
		nmsItem.setTag(tag);

		// Convert back to Bukkit ItemStack and set the meta back
		org.bukkit.inventory.ItemStack updatedItem = CraftItemStack.asBukkitCopy(nmsItem);
		item.setItemMeta(updatedItem.getItemMeta());  // Update the original item with the new meta
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
