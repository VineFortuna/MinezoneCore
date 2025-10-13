package anthony.SuperCraftBrawl.gui.games;

import java.util.Random;

import anthony.SuperCraftBrawl.gui.ClassicModeGUI;
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
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;

public class ParkourMapsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ParkourMapsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title(ChatColorHelper.color("&8Parkour Maps")).build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.fill(ClickableItem
				.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {
				}));

		//Main Parkour:
		ItemStack parkourMain = ItemHelper.setDetails(new ItemStack(Material.GRASS), "", "");
		
		//City Parkour:
		ItemStack parkourCity = ItemHelper.setDetails(new ItemStack(Material.GLASS), "", "");
		
		contents.set(0, 3, ClickableItem.of(
				ItemHelper.setDetails(parkourMain, "&eParkour", "&eArena: &rMain", "", "&7Click to teleport!"), e -> {
					SoundManager.playClickSound(player);
					mainParkour(player);
				}));
		contents.set(0, 5, ClickableItem.of(
				ItemHelper.setDetails(parkourCity, "&eParkour", "&eArena: &rCity", "", "&7Click to teleport!"), e -> {
					SoundManager.playClickSound(player);
					cityParkour(player);
				}));
	}
	
	private void mainParkour(Player player) {
		Location loc = new Location(main.getLobbyWorld(), 189.5, 106, 571.5, -179, 1);
		player.teleport(loc);
		player.sendMessage(main.color("&r&l(!) &rYou have been sent to &r&lMain"));
	}
	
	private void cityParkour(Player player) {
		Location loc = new Location(main.getLobbyWorld(), 129, 116, 699, 45, 1);
		player.teleport(loc);
		player.sendMessage(main.color("&r&l(!) &rYou have been sent to &r&lCity"));
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
