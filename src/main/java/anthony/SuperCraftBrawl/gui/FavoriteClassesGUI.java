package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.mastery.MasteryTier;
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

import java.util.List;

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
					new ClassesGUI(main).inv.open(player);
				}));

		if (data != null) {
			if (!(data.customIntegers.isEmpty())) {
				for (ClassType type : ClassType.sortAlphabetically(ClassType.getAvailableClasses())) {
					if (data.customIntegers.contains(type.getID())) {

						ClassDetails details = data.playerClasses.get(type.getID());

						List<String> lore = type.buildDescription();
						lore.add("");
						lore.add(main.color("&e&nLeft Click&r &eto choose a class"));
						lore.add(main.color("&e&nRight Click&r &eto view mastery"));
						lore.add(main.color("&e&nShift Click&r &eto remove a favorite class"));
						lore.add("");
						lore.addAll(MasteryTier.getRewardAndProgressBar(details));


						contents.set(y, x,
								ClickableItem.of(ItemHelper.setDetails(ItemHelper.setHideFlags(type.getItem(), true),
												type.getTag(), lore),
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
												new ClassMasteryGUI(main, type, inv).inv.open(player);
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
