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

	private static final List<String> FLAG_WARS_DESCRIPTION = java.util.Arrays.asList(
			"&7Steal flags from enemies and rush it back",
			"&7to your base while defending your own.",
			"&7Capture the most flags to win!"
	);

	public Core main;
	public SmartInventory inv;

	public GameSelectorGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
				.title(ChatColorHelper.color("&8&lGame Selector")).build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		ItemStack pane = ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " ");
		for (int row = 0; row < 5; row++) {
			for (int col = 0; col < 9; col++) {
				if (row == 0 || row == 4 || col == 0 || col == 8) {
					contents.set(row, col, ClickableItem.of(pane, e -> {}));
				}
			}
		}

		// SCB Classic: left click picks a map, right click joins a random curated one
		ItemStack scbClassic = ItemHelper.createSkullTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==");
		contents.set(1, 2,
				ClickableItem.of(
						ItemHelper.setDetails(scbClassic,
								"&e&lSuper Craft Brothers",
								"&fMode: &aClassic",
								"",
								"&7Free for all, kill everyone",
								"",
								"&e&nLeft Click&r&e to choose a map",
								"&e&nRight Click&r&e to join a random map"),
						e -> {
							// If item was Left-clicked opens GUI to choose map
							if (e.isLeftClick()) {
								SoundManager.playClickSound(player);
								new ClassicModeGUI(main, inv).inv.open(player);
							// If item was Right-clicked join random game
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.CLASSIC));
							}
						}));

		// SCB Duels: same, for 1v1 maps
		contents.set(1, 4,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD),
								"&e&lSuper Craft Brothers",
								"&fMode: &aDuels",
								"",
								"&71v1 someone to the death",
								"",
								"&e&nLeft Click&r&e to choose a map",
								"&e&nRight Click&r&e to join a random map"),
						e -> {
							// If item was Left-clicked opens GUI to choose map
							if (e.isLeftClick()) {
								SoundManager.playClickSound(player);
								new DuelsModeGUI(main, inv).inv.open(player);
							// If item was Right-clicked join random game
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.DUEL));
							}
						}));

		// Flag Wars
		contents.set(1, 6,
				ClickableItem.of(
						modeSelectorDisplayItem(blueBanner(), "&6&lFLAG WARS &r&lNEW!", FLAG_WARS_DESCRIPTION, flagWarsPlayerCount()),
						e -> {
							SoundManager.playClickSound(player);
							FWModeSelectGUI.openOrJoin(main.getFwGameManager(), player);
						}));

		// Fishing
		contents.set(3, 3,
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
		contents.set(3, 5,
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

		/** Summed across every concurrent Flag Wars instance - FWGameManager can run more than one at once. */
		private int flagWarsPlayerCount() {
			int total = 0;
			for (anthony.flagwars.FWGameInstance instance : main.getFwGameManager().getActiveInstances()) {
				total += instance.countPlayers();
			}
			return total;
		}
}
