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
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map.Entry;

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

		if (main.getGameManager().getNumOfGames() == 0) {
			contents.set(1, 4, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.BARRIER), main.color("&c&lNo games!"),
							main.color("&7Start a game by using"), main.color("&7the Game Selector")),
					e -> {

					}));
			return;
		}

		for (Entry<Maps, GameInstance> entry : main.getGameManager().gameMap.entrySet()) {
			GameInstance game = entry.getValue();
			Maps map = game.getMap();
			String mapName = map.getName();
			ItemStack displayItem = map.getDisplayItem();
			GameType gameType = entry.getValue().gameType;
			String mode = gameType.getName();

			System.out.println(mode);

			if (entry.getValue().state == GameState.WAITING) {
				if (entry.getValue().gameStartTime != null) {
					contents.set(count, i, ClickableItem.of(
							ItemHelper.setDetails(displayItem,
									"&e&l" + mapName,
									"&fMode: &a" + mode,
									"",
									"&fStarting In: &a" + entry.getValue().timeToStartSeconds + "s",
									"&fPlayers: &a" + entry.getValue().players.size() + "/"
											+ entry.getValue().gameType.getMaxPlayers(),
									"", "&r&nClick to join!"),
							e -> {
								main.getGameManager().JoinMap(player, entry.getValue().getMap());
								inv.close(player);
							}));
				} else {
					contents.set(count, i, ClickableItem.of(
							ItemHelper.setDetails(displayItem,
									"&e&l" + mapName,
									"&fMode: &a" + mode,
									"",
									"&6Waiting for Players",
									"&fPlayers: &a" + entry.getValue().players.size() + "/"
											+ entry.getValue().gameType.getMaxPlayers(),
									"", "&r&nClick to join!"),
							e -> {
								main.getGameManager().JoinMap(player, entry.getValue().getMap());
								inv.close(player);
							}));
				}
			} else if (entry.getValue().state == GameState.STARTED) {
				String state = "In Progress";
				contents.set(count, i, ClickableItem.of(
						ItemHelper.setDetails(ItemHelper.setGlowing(displayItem, true),
								"&e&l" + mapName,
								"&fMode: &a" + mode,
								"",
								"&6" + state,
								"&fPlayers: &a" + entry.getValue().players.size() + "/"
										+ entry.getValue().gameType.getMaxPlayers(),
								"&fSpectators: &a" + entry.getValue().spectators.size(), "", "&r&nClick to spectate!"),
						e -> {
							main.getGameManager().SpectatorJoinMap(player, entry.getValue().getMap());
							inv.close(player);
						}));
			}
			if (i > 8) {
				count++;
				i = 0;
			}
			i++;
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
