package anthony.SuperCraftBrawl.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import anthony.SuperCraftBrawl.gui.fishing.FishingAreasGUI;
import anthony.SuperCraftBrawl.gui.fishing.FishingGUI;
import anthony.flagwars.FWModeSelectGUI;
import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;

import anthony.util.SoundManager;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class GameSelectorGUI implements InventoryProvider {

	private static final List<String> SCB_DESCRIPTION = java.util.Arrays.asList(
			"&7Select from a wide range of classes, each",
			"&7with unique abilities. Fight Free For All",
			"&7to be the last one standing, or go 1v1 &",
			"&7fight to the death in Duels!"
	);

	private static final List<String> FLAG_WARS_DESCRIPTION = java.util.Arrays.asList(
			"&7Steal flags from enemies and rush it back",
			"&7to your base while defending your own.",
			"&7Capture the most flags to win! You can",
			"&7play Solo or team up in Duos!"
	);

	public Core main;
	public SmartInventory inv;

	public GameSelectorGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3
						, 9)
				.title(ChatColorHelper.color("&8&lGame Selector")).build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		ItemStack pane = ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " ");
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				if (row == 0 || row == 2 || col == 0 || col == 8) {
					contents.set(row, col, ClickableItem.of(pane, e -> {}));
				}
			}
		}

		// Super Craft Bros - opens a Classic/Duels sub-menu (the same two icons that used to live here directly)
		ItemStack scbIcon = ItemHelper.createSkullTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==");
		contents.set(1, 3,
				ClickableItem.of(
						modeSelectorDisplayItem(scbIcon, "&6&lSUPER CRAFT BROS", SCB_DESCRIPTION, scbPlayerCount()),
						e -> {
							SoundManager.playClickSound(player);
							new SCBModeSelectGUI(main).inv.open(player);
						}));

		// Flag Wars - replaces the old standalone Duels icon's slot
		contents.set(1, 5,
				ClickableItem.of(
						modeSelectorDisplayItem(blueBanner(), "&6&lFLAG WARS &r&lNEW!", FLAG_WARS_DESCRIPTION, flagWarsPlayerCount()),
						e -> {
							SoundManager.playClickSound(player);
							new FWModeSelectGUI(main.getFwGameManager()).open(player);
						}));

		// Fishing
		contents.set(1, 1,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD),
								"&6&lFISHING",
								"",
								"&7Fish for junk, fish and treasures",
								"&7Earn unique rewards",
								"",
							"&e&nLeft Click&r&e to see your Fishingpedia",
								"&e&nRight Click&r&e to see the Fishing Warps"),
						e -> {
							// If item was Left-clicked opens GUI to choose map
							if (e.isLeftClick()) {
								SoundManager.playClickSound(player);
								new FishingGUI(main, inv).inv.open(player);
								// If item was Right-clicked join random game
							} else if (e.isRightClick()) {
								SoundManager.playClickSound(player);
								new FishingAreasGUI(main, inv).inv.open(player);
							}
						}));

		// Parkour
		contents.set(1, 7,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.GRASS),
								"&6&lPARKOUR",
								"",
								"&7How fast are you?",
								"&7Earn rewards for completing the parkour",
								"",
								"&eClick to teleport"
						),
						e -> {
							player.teleport(new Location(main.getLobbyWorld(), 189.5, 106, 571.5, 180, 0));
							player.sendMessage(main.color("&3&l(!) &rLet's see how fast you are!"));
							SoundManager.playSuccessfulHit(player);
						}));
	}

		@Override
		public void update(Player player, InventoryContents contents) {
		}

		public Maps randomizeMap(GameType gameType) {
			Maps randomizedMap;
			boolean joined = false;

			do {
				List<Maps> curatedMaps = Maps.filterMaps(gameType, Maps.Category.CURATED, null, null);
				Random random = new Random();
				int randomizedNumber = random.nextInt(curatedMaps.size());
				randomizedMap = curatedMaps.get(randomizedNumber);
				GameInstance gameInstance = main.getGameManager().getInstanceOfMap(randomizedMap);

				// If gameInstance does not exist
				if (gameInstance == null) {
					joined = true;
				}
			} while (!joined);

			return randomizedMap;
		}

		/** Same lore template for every top-level mode tile: blank, description (can be multiple lines), blank, "Click to Play!", blank, live player count. */
		private ItemStack modeSelectorDisplayItem(ItemStack icon, String title, List<String> description, int playerCount) {
			List<String> lore = new ArrayList<>();
			lore.add("");
			for (String line : description) {
				lore.add(main.color(line));
			}
			lore.add("");
			lore.add(main.color("&eClick to Play!"));
			lore.add("");
			lore.add(main.color("&a" + playerCount + " &rplaying!"));

			return ItemHelper.setDetails(icon, main.color(title), lore);
		}

		private ItemStack blueBanner() {
			ItemStack banner = new ItemStack(Material.BANNER);
			BannerMeta meta = (BannerMeta) banner.getItemMeta();
			meta.setBaseColor(DyeColor.BLUE);
			banner.setItemMeta(meta);
			return banner;
		}

		/** Summed across every active Classic/Duels instance - both GameTypes share the same gameMap/Maps pool. */
		private int scbPlayerCount() {
			int total = 0;
			for (GameInstance instance : main.getGameManager().gameMap.values()) {
				if (instance != null) total += instance.players.size();
			}
			return total;
		}

		/** Summed across every concurrent Flag Wars instance (Solo + Duos) - FWGameManager can run more than one at once. */
		private int flagWarsPlayerCount() {
			int total = 0;
			for (anthony.flagwars.FWGameInstance instance : main.getFwGameManager().getActiveInstances()) {
				total += instance.countPlayers();
			}
			return total;
		}
}
