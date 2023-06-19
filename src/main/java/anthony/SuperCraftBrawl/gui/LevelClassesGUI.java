package anthony.SuperCraftBrawl.gui;

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

public class LevelClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public LevelClassesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Level Classes").build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData playerData = main.getDataManager().getPlayerData(player);
		int a = 0;
		int b = 0;

		for (ClassType type : ClassType.values()) {
			if (type.getTokenCost() == 0 && type.getMinRank() != Rank.VIP && type.getLevel() > 0) {
				contents.set(a, b, ClickableItem.of(ItemHelper.setDetails(ItemHelper.setHideFlags(type.getItem(), true), type.getTag(),
						type.buildDescription(), "",
						playerData.level >= type.getLevel() ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Unlocked"
								: "" + ChatColor.RED + ChatColor.BOLD + "Unlocks at: " + ChatColor.YELLOW
										+ ChatColor.BOLD + "Level " + type.getLevel()),
						e -> {
							if (!(playerData.level >= type.getLevel())) {
								player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this class yet!"));
								return;
							}

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
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

}
