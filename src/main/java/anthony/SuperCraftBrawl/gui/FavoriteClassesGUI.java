package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class FavoriteClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public FavoriteClassesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(5, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Favorite Classes").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		int x = 0, y = 0;
		
		contents.set(4, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.ARROW), String.valueOf(ChatColor.GRAY) + "Go Back"), e -> {
					inv.close(player);
					new ClassesGUI(main).inv.open(player);
				}));

		if (data != null) {
			if (!(data.customIntegers.isEmpty())) {
				for (ClassType type : ClassType.sortAlphabetically(ClassType.getAvailableClasses())) {
					if (data.customIntegers.contains(type.getID())) {

						ClassDetails details = data.playerClasses.get(type.getID());
						int played = details.gamesPlayed + 2 * details.gamesWon;
						int nextLevel = 10;

						if (played >= 75)
							nextLevel = 100;
						else if (played >= 50)
							nextLevel = 75;
						else if (played >= 25)
							nextLevel = 50;
						else if (played >= 10)
							nextLevel = 25;

						contents.set(y, x,
								ClickableItem.of(ItemHelper.setDetails(ItemHelper.setHideFlags(type.getItem(), true),
												type.getTag(), type.buildDescription(), "",
												"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET
														+ ChatColor.YELLOW + " to choose a class",
												"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET
														+ ChatColor.YELLOW + " to view mastery",
												"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Shift Click" + ChatColor.RESET
														+ ChatColor.YELLOW + " to remove a favorite class",
												"",
												main.color("&aNext reward:"),
												main.progressBar(played, nextLevel, 25)),
										e -> {
											if (e.isShiftClick()) {
												for (int i = 0; i < data.customIntegers.size(); i++) {
													if (data.customIntegers.get(i) == type.getID()) {
														data.customIntegers.remove(i);
														player.sendMessage(main
																.color("&2&l(!) &rRemoved from favorite classes: " + type.getTag()));
														main.getDataManager().saveData(data);
														inv.open(player);
													}
												}
											} else if (e.isLeftClick()) {
												main.getGameManager().playerSelectClass(player, type);
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
														+ "=============================================");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
														+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
														+ "Selected Class: " + type.getTag());
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
														+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: "
														+ ChatColor.RESET + ChatColor.YELLOW + type.getClassDesc());
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
														+ "=============================================");
												inv.close(player);
											} else if (e.isRightClick()) {
												new ClassRewardsGUI(main, type, inv).inv.open(player);
											}
										}));
						x++;

						if (x >= 9) {
							y++;
							x = 0;
						}
					}
				}
			}
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
