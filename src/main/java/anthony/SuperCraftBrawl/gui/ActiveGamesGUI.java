package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.map.Maps;
import anthony.SuperCraftBrawl.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map.Entry;

public class ActiveGamesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ActiveGamesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Quick Join").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int count = 0;
		int i = 0;
		for (Entry<Maps, GameInstance> entry : main.getGameManager().gameMap.entrySet()) {
			String mapName = entry.getValue().getMap().toString();

			if (entry.getValue().state == GameState.WAITING) {
				if (entry.getValue().gameStartTime != null) {
					contents.set(count, i, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EMERALD_BLOCK),
							String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + mapName,
							ChatColor.RESET + "Starting In: " + ChatColor.YELLOW + entry.getValue().ticksTilStart
									+ "s",
							ChatColor.RESET + "Players: " + ChatColor.YELLOW + entry.getValue().players.size()
									+ "/" + entry.getValue().gameType.getMaxPlayers(),
							"", String.valueOf(ChatColor.RESET) + ChatColor.UNDERLINE + "Click to join!"), e -> {
								main.getGameManager().JoinMap(player, entry.getValue().getMap());
								inv.close(player);
							}));
				} else {
					contents.set(count, i, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EMERALD_BLOCK),
							String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + mapName,
							ChatColor.YELLOW + "Waiting for Players",
							ChatColor.RESET + "Players: " + ChatColor.YELLOW + entry.getValue().players.size()
									+ "/" + entry.getValue().gameType.getMaxPlayers(),
							"", String.valueOf(ChatColor.RESET) + ChatColor.UNDERLINE + "Click to join!"), e -> {
								main.getGameManager().JoinMap(player, entry.getValue().getMap());
								inv.close(player);
							}));
				}
			} else if (entry.getValue().state == GameState.STARTED) {
				String state = "In Progress";
				contents.set(count, i, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EYE_OF_ENDER),
						String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + mapName, ChatColor.GREEN + state,
						ChatColor.RESET + "Players: " + ChatColor.YELLOW + entry.getValue().players.size() + "/"
								+ entry.getValue().gameType.getMaxPlayers(),
						ChatColor.RESET + "Spectators: " + ChatColor.YELLOW + entry.getValue().spectators.size(),
						"", String.valueOf(ChatColor.RESET) + ChatColor.UNDERLINE + "Click to spectate!"), e -> {
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
