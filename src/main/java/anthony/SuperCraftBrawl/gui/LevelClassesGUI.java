package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.ChatColorHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;

public class LevelClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	public int totalRows = 3;
	public int totalColumns = 9;

	public LevelClassesGUI(Core main) {
		inv = SmartInventory.builder()
				.id("myInventory")
				.provider(this).size(totalRows, totalColumns)
				.title(ChatColorHelper.color("&8&lLevel Classes"))
				.build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData playerData = main.getDataManager().getPlayerData(player);
		int a = 0;
		int b = 0;

		for (ClassType type : ClassType.values()) {
			if (type.getTokenCost() == 0 && type.getMinRank() != Rank.VIP && type.getLevel() > 0) {
				contents.set(a, b, ClickableItem.of(ItemHelper.setDetails(ItemHelper.setHideFlags(type.getItem(), true),
						type.getTag(), type.buildDescription(), "",
						playerData.level >= type.getLevel() ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Unlocked"
								: "" + ChatColor.RED + ChatColor.BOLD + "Unlocks at: " + ChatColor.YELLOW
										+ ChatColor.BOLD + "Level " + type.getLevel(),
						"",
						"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET + ChatColor.YELLOW
								+ " to choose a class",
						"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET + ChatColor.YELLOW
								+ " to add a favorite class"),
						e -> {
							if (!(playerData.level >= type.getLevel())) {
								player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this class yet!"));
								return;
							}

							if (e.isLeftClick()) {
								main.getGameManager().playerSelectClass(player, type);
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
										+ "==============================================");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
										+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: " + type.getTag());
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
										+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
										+ ChatColor.YELLOW + type.getClassDesc());
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
										+ "==============================================");
							} else if (e.isRightClick()) {
								PlayerData data = main.getDataManager().getPlayerData(player);

								if (data != null) {
									data.customIntegers.add(type.getID());
									player.sendMessage(main
											.color("&2&l(!) &rAdded new favorite class: " + type.getTag()));
									main.getDataManager().saveData(data);
								}
							}
							inv.close(player);
						}));
				b++;

				if (b > 8) {
					if (a == 0) {
						a = 1;
						b = 0;
					} else if (a == 1) {
						a = 2;
						b = 0;
					}
				}
			}
		}

		// Setting "Go Back" Button
		contents.set(totalRows - 1, totalColumns - 1, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER),
				"&7Go back"), e -> {
			new ClassSelectorGUI(main).inv.open(player);
		}));

	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

}
