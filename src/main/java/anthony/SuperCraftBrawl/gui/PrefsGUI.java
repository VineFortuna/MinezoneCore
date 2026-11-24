package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.RankManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PrefsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private RankManager rm;

	public PrefsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Preferences").build();
		this.main = main;
	}

	public RankManager getRankManager() {
		return rm;
	}

	/** A preference: name, a short description, then whether it's on (each set apart by an empty line). */
	private ItemStack prefItem(Material material, String name, boolean enabled, String... description) {
		return prefItem(material, name, new String[] { enabled ? "&aEnabled" : "&cDisabled" }, description);
	}

	/** A preference: name, a short description, then its status lines (each set apart by an empty line). */
	private ItemStack prefItem(Material material, String name, String[] status, String... description) {
		String[] lore = new String[description.length + status.length + 2];
		lore[0] = "";
		for (int i = 0; i < description.length; i++) lore[1 + i] = main.color(description[i]);
		lore[1 + description.length] = "";
		for (int i = 0; i < status.length; i++) lore[2 + description.length + i] = main.color(status[i]);
		return ItemHelper.setDetails(new ItemStack(material), main.color("&e" + name), lore);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		contents.fillBorders(ClickableItem.of(ItemHelper.setDetails(
				new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));

		contents.set(4, 6, ClickableItem.of(ItemHelper.setGlowing(ItemHelper.setDetails(new ItemStack(Material.REDSTONE_COMPARATOR),
				"" + ChatColor.RESET + ChatColor.YELLOW + "Preferences"), true), e -> {}));
		contents.set(4, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BOOK),
				"" + ChatColor.RESET + ChatColor.YELLOW + "My Stats"), e -> {
			new StatsGUI(main).inv.open(player);
		}));

		if (data != null) {
			// Clicking a preference changes it right here: the items are set again, the menu stays open

			// Row 1
			contents.set(1, 2, ClickableItem.of(prefItem(Material.DIAMOND, "Custom Win Messages", data.cwm != 0,
					"&7Show a custom message", "&7when you win a game"), e -> {
						if (player.hasPermission("scb.customWin")) {
							if (data.cwm == 1) {
								player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ "You have disabled " + ChatColor.YELLOW + "Custom Win Messages");
								data.cwm = 0;
							} else {
								data.cwm = 1;
								player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
										+ "You have enabled " + ChatColor.YELLOW + "Custom Win Messages");
							}
						} else {
							player.sendMessage(main.color("&c&l(!) &rYou need the rank " + ChatColor.BLUE
									+ ChatColor.BOLD + Rank.PRO.getTag() + "&rto use this feature!"));
						}
						init(player, contents);
					}));

			contents.set(1, 4, ClickableItem.of(prefItem(Material.DIAMOND_SWORD, "Custom Kill Messages", data.killMsgs != 0,
					"&7Show a custom message", "&7when you kill a player"), e -> {
						if (player.hasPermission("scb.customKillMsgs")) {
							if (data.killMsgs == 0) {
								player.sendMessage(main.color("&r&l(!) &rYou have enabled &eCustom Kill Messages"));
								data.killMsgs = 1;
							} else {
								player.sendMessage(main.color("&r&l(!) &rYou have disabled &eCustom Kill Messages"));
								data.killMsgs = 0;
							}
						} else {
							player.sendMessage(main.color("&c&l(!) &rYou need the rank " + Rank.PRO.getTag() + " &fto use this!"));
						}
						init(player, contents);
					}));

			contents.set(1, 6, ClickableItem.of(prefItem(Material.NOTE_BLOCK, "Game Starting Sound", data.gameStartSound == 1,
					"&7Receive a sound notification", "&7when a game is starting"), e -> {
						data.gameStartSound = data.gameStartSound == 1 ? 0 : 1;
						player.sendMessage(main.color("&r&l(!) &rYou have " + (data.gameStartSound == 1 ? "enabled" : "disabled")
								+ " &eGame Starting Sound"));
						init(player, contents);
					}));

			// Row 3
			// Private messages are stored the other way around: pm == 1 means they are off
			contents.set(3, 3, ClickableItem.of(prefItem(Material.PAPER, "Private Messages", data.pm != 1,
					"&7Receive private messages", "&7from other players"), e -> {
						if (data.pm == 0) {
							player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have disabled " + ChatColor.YELLOW + "Private Messages");
							data.pm = 1;
						} else {
							data.pm = 0;
							player.sendMessage("" + ChatColor.RESET + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "You have enabled " + ChatColor.YELLOW + "Private Messages");
						}
						init(player, contents);
					}));

			// Everyone sees it, but only LEGENDARY (and above) can use it: the others are told what they need
			boolean canBuild = main.getLobbyBuildManager().canBuild(player);
			String[] buildStatus = canBuild
					? new String[] { data.buildOnJoin == 1 ? "&aEnabled" : "&cDisabled" }
					: new String[] { "&cYou need &6LEGENDARY &cto", "&cuse Building Mode" };

			contents.set(3, 5, ClickableItem.of(prefItem(Material.WOOD, "Building Mode on Join", buildStatus,
					"&7Start in Building Mode", "&7when you join the server"), e -> {
						if (!main.getLobbyBuildManager().canBuild(player)) {
							player.sendMessage(main.color("&c&l(!) &rYou need the rank &6&lLEGENDARY &rto use this feature!"));
							return;
						}

						data.buildOnJoin = data.buildOnJoin == 1 ? 0 : 1;
						player.sendMessage(main.color("&r&l(!) &rYou have " + (data.buildOnJoin == 1 ? "enabled" : "disabled")
								+ " &eBuilding Mode on Join"));
						init(player, contents);
					}));

			contents.set(4, 2,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.SKULL_ITEM, 1, (short) 3),
							main.color("&eFriends")), e -> {
						new FriendsGUI(main, contents.inventory()).inv.open(player);
					}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
