package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
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
					new ClassSelectorGUI(main).inv.open(player);
				}));

		if (data != null) {
			if (!(data.customIntegers.isEmpty())) {
				for (ClassType type : ClassType.values()) {
					if (data.customIntegers.contains(type.getID())) {
						contents.set(y, x,
								ClickableItem.of(ItemHelper.setDetails(type.getItem(), "" + type.getTag(),
										"" + ChatColor.GRAY + type.getClassDesc(), "",
										"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET
												+ ChatColor.YELLOW + " to choose a class",
										"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET
												+ ChatColor.YELLOW + " to remove a favorite class"),
										e -> {
											inv.close(player);

											if (e.isLeftClick()) {
												main.getGameManager().playerSelectClass(player, type);
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
														+ "==============================================");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
														+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
														+ "Selected Class: " + type.getTag());
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| "
														+ ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD
														+ "Class Desc: " + ChatColor.RESET + ChatColor.YELLOW
														+ type.getClassDesc());
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
												player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
														+ "==============================================");
											} else if (e.isRightClick()) {
												for (int i = 0; i < data.customIntegers.size(); i++) {
													if (data.customIntegers.get(i) == type.getID()) {
														data.customIntegers.remove(i);
														player.sendMessage(main
																.color("&2&l(!) &rRemoved from favorite classes: " + type.getTag()));
														main.getDataManager().saveData(data);
													}
												}
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
