package anthony.SuperCraftBrawl.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Random;

import anthony.util.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.map.Maps;

import org.bukkit.Bukkit;
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
		inv = SmartInventory.builder().id("myInventory").provider(this).size(4, 9)
				.title(ChatColorHelper.color("&8&lGame Selector")).build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int skywarsCount = GameManager.playercount.getOrDefault("sw-1", 0);

//		contents.set(1, 5,
//				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BOW),
//						"" + ChatColor.RESET + ChatColor.GREEN + "SkyWars " + ChatColor.DARK_GREEN + ChatColor.BOLD
//								+ "NEW GAME!",
//						"" + ChatColor.GRAY + "Loot up, build to center & ", ChatColor.GRAY + "claim the #1 spot!", "",
//						main.color("&e&lPlayers: &e" + skywarsCount)), e -> {
//							inv.close(player);
//							ByteArrayOutputStream b = new ByteArrayOutputStream();
//							DataOutputStream out = new DataOutputStream(b);
//
//							try {
//								out.writeUTF("Connect");
//								out.writeUTF("sw-1");
//								player.sendMessage(main.color("&e&l(!) &rConnecting to &esw-1"));
//							} catch (Exception ex) {
//								player.sendMessage(main.color("&c&l(!) &rThere was a problem connecting to &esw-1"));
//							}
//							player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
//						}));

		int scbCount = GameManager.playercount.getOrDefault("scb-1", 0)
				+ GameManager.playercount.getOrDefault("scb-2", 0);

		// Classic Mode Button
		//ItemStack skullPlayer = ItemHelper.createSkullHeadPlayer(1, player.getName());
		ItemStack sethBling = new ItemStack(Material.REDSTONE_BLOCK);

		contents.set(1, 2,
				ClickableItem.of(
						ItemHelper.setDetails(sethBling,
								"&eClassic",
								"&7Choose a class, kill everyone",
								"",
								"&e&nLeft Click&r&e to choose a map",
								"&e&nRight Click&r&e to join a random map"),
						e -> {
							// If item was Left-clicked opens GUI to choose map
							if (e.isLeftClick()) {
								new ClassicModeGUI(main).inv.open(player);
							// If item was Right-clicked join random game
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.CLASSIC));
							}
						}));

		// Duels Mode Button
		contents.set(1, 4,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.IRON_SWORD),
								"&eDuels",
								"&71v1 someone until death",
								"",
								"&e&nLeft Click&r&e to choose a map",
								"&e&nRight Click&r&e to join a random map"),
						e -> {
							// If item was Left-clicked opens GUI to choose map
							if (e.isLeftClick()) {
								new DuelsModeGUI(main).inv.open(player);
							// If item was Right-clicked join random game
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.DUEL));
							}
						}));

		// Frenzy Mode Button
		contents.set(1, 6,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.TNT),
								"&eFrenzy",
								"&7Random classes, big maps",
								"",
								"&e&nLeft Click&r&e to choose a map",
								"&e&nRight Click&r&e to join a random map"),
						e -> {
							// If item was Left-clicked opens GUI to choose map
							if (e.isLeftClick()) {
								new FrenzyModeGUI(main).inv.open(player);
							// If item was Right-clicked join random game
							} else if (e.isRightClick()) {
								main.getGameManager().JoinMap(player, randomizeMap(GameType.FRENZY));
							}
						}));

		// SCB Duos Button
		contents.set(3, 4,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.DIAMOND_SWORD),
								"&eDuos SCB",
								"&7SCB with teammates",
								"&e&lPlayers: &e" + GameManager.playercount.getOrDefault("scb-2", 0)),
						e -> {
							inv.close(player);

							// Sending player to SCB Duos
							Bukkit.getMessenger().registerOutgoingPluginChannel(main, "BungeeCord");

							ByteArrayOutputStream b = new ByteArrayOutputStream();
							DataOutputStream out = new DataOutputStream(b);

							try {
								out.writeUTF("Connect");
								out.writeUTF("scb-2");
								player.sendMessage(ChatColorHelper.color("&e&l(!) &rConnecting to &escb-2"));
							} catch (Exception ex) {
								player.sendMessage(ChatColorHelper.color("&c&l(!) &rThere was a problem connecting to &escb-2"));
							}
							player.sendPluginMessage(main, "BungeeCord", b.toByteArray());
						}));
		
		/*contents.set(3, 6,
				ClickableItem.of(
						ItemHelper.setDetails(new ItemStack(Material.ARMOR_STAND),
								"&eSCB Practice", "",
								"&7Practice different classes"),
						e -> {
							inv.close(player);
							new SCBPractice(player, Game.BowPractice);
						}));*/
				}

		@Override
		public void update(Player player, InventoryContents contents) {
		}

		public Maps randomizeMap(GameType gameType) {
			Maps randomizedMap;
			boolean joined = false;

			do {
				Random random = new Random();
				int randomizedNumber = random.nextInt(Maps.getGameType(gameType).size());

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
