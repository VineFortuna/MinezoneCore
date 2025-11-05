package anthony.SuperCraftBrawl.gui;

import java.util.Random;

import anthony.SuperCraftBrawl.gui.fishing.FishingAreasGUI;
import anthony.SuperCraftBrawl.gui.fishing.FishingGUI;
import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;

import anthony.util.SoundManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class GameSelectorGUI implements InventoryProvider {

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
		contents.fill(ClickableItem.of(ItemHelper.setDetails(
				new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

		// Classic Mode
		ItemStack scbClassic = ItemHelper.createSkullTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==");
		contents.set(1, 3,
				ClickableItem.of(
						ItemHelper.setDetails(scbClassic,
								"&e&lSuper Craft Brothers",
								"&eMode: &rClassic",
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

		// Duels Mode
		contents.set(1, 5,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD),
								"&e&lSuper Craft Brothers",
								"&eMode: &rDuels",
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

		// Fishing
		contents.set(1, 1,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.FISHING_ROD),
								"&e&lFishing",
								"&7Fish for junk, fish and treasures",
								"",
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
								"&e&lParkour",
								"&7How fast are you?",
								"",
								"&7Earn rewards for completing the parkour",
								"",
								"&e&nClick&r&e to teleport"
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
				Random random = new Random();
				int randomizedNumber = random.nextInt(Maps.filterMaps(gameType, Maps.Category.CURATED, null, null).size());

				randomizedMap = Maps.getGameType(gameType).get(randomizedNumber);
				GameInstance gameInstance = main.getGameManager().getInstanceOfMap(randomizedMap);

				// If gameInstance does not exist
				if (gameInstance == null) {
					joined = true;
				}
			} while (!joined);

			return randomizedMap;
		}
}
