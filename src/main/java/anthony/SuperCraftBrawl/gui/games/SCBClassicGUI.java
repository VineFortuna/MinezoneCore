package anthony.SuperCraftBrawl.gui.games;

import java.util.Random;

import anthony.SuperCraftBrawl.gui.ClassicModeGUI;
import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;

import anthony.util.SoundManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class SCBClassicGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public SCBClassicGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title(ChatColorHelper.color("&8SCB Classic")).build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.fill(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		// Classic Mode
		ItemStack scbClassic = ItemHelper.createSkullTexture(
				"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==");
		contents.set(1, 4,
				ClickableItem.of(ItemHelper.setDetails(scbClassic, "&eSuper Craft Brothers", "&eMode: &rClassic", "",
						"&7Free for all, kill everyone", "", "&e&nLeft Click&r&e to choose a map",
						"&e&nRight Click&r&e to join a random map"), e -> {
							// If item was Left-clicked opens GUI to choose map
							if (e.isLeftClick()) {
								SoundManager.playClickSound(player);
								new ClassicModeGUI(main, inv).inv.open(player);
								// If item was Right-clicked join random game
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.CLASSIC));
							}
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
