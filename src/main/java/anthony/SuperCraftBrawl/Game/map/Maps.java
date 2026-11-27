package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Game.GameType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A map of Super Craft Bros the game can be played on. They used to be the constants of an enum written out in this
 * file; now every map is an entry of maps.yml, read by {@link MapStore} when the plugin starts and changed in game with
 * /scbsetup. This class keeps the shape the rest of the game was written against: {@link #values()} (the playable
 * maps, in the order of the file), {@link #valueOf(String)}, {@link #name()}, {@link #GetInstance()} and the filters.
 *
 * A map has one object for as long as the server runs, however often it is edited, switched off and on or the file is
 * read again, so a game (or anything else) holding it keeps a valid reference.
 */
public final class Maps implements Comparable<Maps> {

	/** The playable maps (complete and switched on), in the order of the file. Replaced whole, never changed. */
	private static volatile List<Maps> registry = Collections.emptyList();
	/** Every map that has ever been playable, by name, so a map keeps its object when it is switched off and on again. */
	private static final Map<String, Maps> KNOWN = new ConcurrentHashMap<String, Maps>();

	/** What it is called in the database, in presets and in /scbsetup. Never changes. */
	private final String id;
	private volatile String name;
	private volatile Gameplay[] gameplay;
	private volatile Size size;
	private volatile Category category;
	private volatile ItemStack displayItem;
	private volatile MapInstance instance;
	private volatile GameType gamemode;

	private Maps(String id) {
		this.id = id;
	}

	// ---------------------------------------------------------------- the registry

	/** The playable maps, like an enum's values(). */
	public static Maps[] values() {
		return registry.toArray(new Maps[0]);
	}

	/** The playable map with this id, like an enum's valueOf: throws if there is none. */
	public static Maps valueOf(String id) {
		Maps map = get(id);
		if (map == null) throw new IllegalArgumentException("No map named " + id);
		return map;
	}

	/** The playable map with this id, or null. */
	public static Maps get(String id) {
		if (id == null) return null;
		for (Maps map : registry) {
			if (map.id.equals(id)) return map;
		}
		return null;
	}

	/**
	 * Makes exactly these the playable maps, in this order. A map keeps its object if it had one: only {@code changedId}
	 * is brought up to date (null: all of them).
	 */
	static synchronized void publish(List<MapDraft> playable, String changedId) {
		List<Maps> next = new ArrayList<Maps>();
		for (MapDraft draft : playable) {
			Maps map = KNOWN.get(draft.id);
			if (map == null) {
				map = new Maps(draft.id);
				map.apply(draft);
				KNOWN.put(draft.id, map);
			} else if (changedId == null || changedId.equals(draft.id)) {
				map.apply(draft);
			}
			next.add(map);
		}
		registry = Collections.unmodifiableList(next);
	}

	private void apply(MapDraft draft) {
		this.name = draft.displayName;
		this.gameplay = draft.gameplay.toArray(new Gameplay[0]);
		this.size = draft.size;
		this.category = draft.category;
		this.displayItem = draft.icon();
		this.instance = draft.toInstance();
		this.gamemode = draft.gameType;
	}

	/** The id, like an enum's name(). */
	public String name() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}

	/** In the order of the file, like an enum's natural order. */
	@Override
	public int compareTo(Maps other) {
		List<Maps> order = registry;
		return Integer.compare(order.indexOf(this), order.indexOf(other));
	}

	public static List<Maps> getGameType(GameType type) {
		List<Maps> maps = new ArrayList<>();
		
		for (Maps map : Maps.values()) {
			if (map.GetInstance().gameType == type) {
				maps.add(map);
			}
		}
		return maps;
	}
	
	public static List<Maps> filterMaps(GameType gamemode, Category category, Size size, Gameplay gameplay) {
		List<Maps> allMaps = Arrays.asList(Maps.values());
		
		// Filter by Gamemode
		List<Maps> filteredMaps = allMaps.stream()
				.filter(map -> map.getGamemode() == gamemode)
				.collect(Collectors.toList());
		
		// Filter by Type
		if (category == null) {
			filteredMaps = filteredMaps.stream()
					.filter(map -> map.getCategory() != Category.CHRISTMAS && map.getCategory() != Category.HALLOWEEN
						&& map.getCategory() != Category.EASTER) // Exclude Holiday maps
					.collect(Collectors.toList());
		} else {
			filteredMaps = filteredMaps.stream()
					.filter(map -> map.getCategory() == category)
					.collect(Collectors.toList());
		}
		
		// Filter by Size
		if (size != null) {
			filteredMaps = filteredMaps.stream()
					.filter(map -> map.getSize() == size)
					.collect(Collectors.toList());
		}
		
		// Filter by Gameplay
		if (gameplay != null) {
			filteredMaps = filteredMaps.stream()
					.filter(map -> Arrays.stream(map.getGameplay()).anyMatch(g -> g == gameplay))
					.collect(Collectors.toList());
		}
		
		return filteredMaps;
	}
	
	public static void sortMaps(List<Maps> maps, Sorter sorter) {
		Comparator<Maps> sortByName = Comparator.comparing(Maps::getName);
		Comparator<Maps> sortBySize = Comparator.comparing(Maps::getSize);
		Comparator<Maps> sortByGameplay = (map1, map2) -> {
			Maps.Gameplay[] gameplay1 = map1.getGameplay();
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
	
	public String getName() {
		return name;
	}
	
	public Gameplay[] getGameplay() {
		return gameplay;
	}
	
	public Size getSize() {
		return size;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public ItemStack getDisplayItem() {
		return displayItem;
	}
	
	public MapInstance GetInstance() {
		return instance;
	}
	
	public GameType getGamemode() {
		return gamemode;
	}
	
	public enum Gameplay {
		VOIDY,
		FLAT,
		ELEVATED,
		UNDERGROUND,
		INDOOR;
		
		@Override
		public String toString() {
			switch (this) {
				case VOIDY:
					return "Voidy";
				case FLAT:
					return "Flat";
				case ELEVATED:
					return "Elevated";
				case UNDERGROUND:
					return "Underground";
				case INDOOR:
					return "Indoor";
			}
			return "Unknown Gameplay";
		}
	}
	
	public enum Size {
		SMALL,
		MEDIUM,
		LARGE,
		HUGE;
		
		@Override
		public String toString() {
			switch (this) {
				case SMALL:
					return "Small";
				case MEDIUM:
					return "Medium";
				case LARGE:
					return "Large";
				case HUGE:
					return "Huge";
			}
			return "Unknown Size";
		}
	}
	
	public enum Category {
		CURATED,
		CASUAL,
		VAULTED,
		HALLOWEEN,
		CHRISTMAS,
		EASTER;
		
		@Override
		public String toString() {
			switch (this) {
				case CURATED:
					return "Curated";
				case CASUAL:
					return "Casual";
				case VAULTED:
					return "Vaulted";
				case HALLOWEEN:
					return "Halloween";
				case CHRISTMAS:
					return "Christmas";
				case EASTER:
					return "Easter";
			}
			return "Unknown Map " + this.getClass();
		}
	}
	
	public enum Sorter {
		ALPHABETICAL,
		SIZE,
		GAMEPLAY;
		
		@Override
		public String toString() {
			switch (this) {
				case ALPHABETICAL:
					return "A-Z";
				case SIZE:
					return "Size";
				case GAMEPLAY:
					return "Gameplay";
			}
			return "Unknown Sort";
		}
	}
}
