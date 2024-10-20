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




		for (Entry<Maps, GameInstance> entry : main.getGameManager().gameMap.entrySet()) {
			String mapName = entry.getValue().getMap().toString();

			// Checking gameType to set due item
			GameType gameType = entry.getValue().gameType;
			ItemStack displayItem = null;

			if (gameType == GameType.CLASSIC) {
				//displayItem = ItemHelper.createSkullHeadPlayer(1, player.getName());
				displayItem = new ItemStack(Material.REDSTONE_BLOCK);
			} else if (gameType == GameType.DUEL) {
				displayItem = new ItemStack(Material.IRON_SWORD);
			} else if (gameType == GameType.FRENZY) {
				displayItem = new ItemStack(Material.TNT);
			}

			if (entry.getValue().state == GameState.WAITING) {
				if (entry.getValue().gameStartTime != null) {
					contents.set(count, i, ClickableItem.of(ItemHelper.setDetails(displayItem,
							"&e&l" + mapName,
							"&rStarting In: &e" + entry.getValue().timeToStartSeconds + "s",
							"&rPlayers: &e" + entry.getValue().players.size() + "/" + entry.getValue().gameType.getMaxPlayers(),
							"",
							"&r&nClick to join!"), e -> {
						main.getGameManager().JoinMap(player, entry.getValue().getMap());
						inv.close(player);
					}));
				} else {
					contents.set(count, i, ClickableItem.of(ItemHelper.setDetails(displayItem,
							"&e&l" + mapName,
							"&eWaiting for Players",
							"&rPlayers: &e" + entry.getValue().players.size() + "/" + entry.getValue().gameType.getMaxPlayers(),
							"",
							"&r&nClick to join!"), e -> {
						main.getGameManager().JoinMap(player, entry.getValue().getMap());
						inv.close(player);
					}));
				}
			} else if (entry.getValue().state == GameState.STARTED) {
				String state = "In Progress";
				contents.set(count, i, ClickableItem.of(ItemHelper.setDetails(ItemHelper.setGlowing(displayItem, true),
						"&e&l" + mapName,
						"&a" + state,
						"&rPlayers: &e" + entry.getValue().players.size() + "/" + entry.getValue().gameType.getMaxPlayers(),
						"&rSpectators: &e" + entry.getValue().spectators.size(),
						"",
						"&r&nClick to spectate!"), e -> {
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
