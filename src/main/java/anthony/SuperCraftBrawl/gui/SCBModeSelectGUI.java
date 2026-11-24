package anthony.SuperCraftBrawl.gui;

import java.util.List;
import java.util.Random;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Opened from the Game Selector's Super Craft Bros icon - lets a player pick Classic or Duels, same two icons that used to live directly on the Game Selector itself. */
public class SCBModeSelectGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public SCBModeSelectGUI(Core main) {
		inv = SmartInventory.builder().id("scbModeSelect").provider(this).size(3, 9)
				.title(ChatColorHelper.color("&8&lSuper Craft Bros")).build();
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

		// Classic Mode
		ItemStack scbClassic = ItemHelper.createSkullTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==");
		contents.set(1, 3,
				ClickableItem.of(
						ItemHelper.setDetails(scbClassic,
								"&6&lSUPER CRAFT BROS",
								"&fMode: &aClassic",
								"",
								"&7Free for all, kill everyone",
								"",
								"&e&nLeft Click&r&e to choose a map",
								"&e&nRight Click&r&e to join a random map"),
						e -> {
							if (e.isLeftClick()) {
								SoundManager.playClickSound(player);
								new ClassicModeGUI(main, inv).inv.open(player);
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.CLASSIC));
							}
						}));

		// Duels Mode
		contents.set(1, 5,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD),
								"&6&lSUPER CRAFT BROS",
								"&fMode: &aDuels",
								"",
								"&71v1 someone to the death",
								"",
								"&e&nLeft Click&r&e to choose a map",
								"&e&nRight Click&r&e to join a random map"),
						e -> {
							if (e.isLeftClick()) {
								SoundManager.playClickSound(player);
								new DuelsModeGUI(main, inv).inv.open(player);
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.DUEL));
							}
						}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	private Maps randomizeMap(GameType gameType) {
		Maps randomizedMap;
		boolean joined = false;

		do {
			List<Maps> curatedMaps = Maps.filterMaps(gameType, Maps.Category.CURATED, null, null);
			Random random = new Random();
			int randomizedNumber = random.nextInt(curatedMaps.size());
			randomizedMap = curatedMaps.get(randomizedNumber);
			GameInstance gameInstance = main.getGameManager().getInstanceOfMap(randomizedMap);

			if (gameInstance == null) {
				joined = true;
			}
		} while (!joined);

		return randomizedMap;
	}
}
