package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TokenClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public TokenClassesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Token Classes").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		int a = 0;
		int b = 0;
		
		contents.set(2, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.ARROW), String.valueOf(ChatColor.GRAY) + "Go Back"), e -> {
					new ClassesGUI(main).inv.open(player);
				}));

		for (ClassType type : ClassType.sortAlphabetically(ClassType.getTokenClasses(false))) {
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

			contents.set(a, b,
					ClickableItem.of(ItemHelper.setDetails(ItemHelper.setHideFlags(type.getItem(), true),
									type.getTag(),
									getCostDescription(player, type),
									"",
									"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET
											+ ChatColor.YELLOW + " to choose a class",
									"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET
											+ ChatColor.YELLOW + " to view mastery",
									"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Shift Click" + ChatColor.RESET
											+ ChatColor.YELLOW + " to add a favorite class",
									"",
									main.color("&aNext reward:"),
									main.progressBar(played, nextLevel, 25)),
							e -> {

								if (data.playerClasses.get(type.getID()) != null
										&& data.playerClasses.get(type.getID()).purchased) {

									if (e.isShiftClick()) {
										if (data != null) {
											if (!data.customIntegers.contains(type.getID())) {
												data.customIntegers.add(type.getID());
												player.sendMessage(
														main.color("&2&l(!) &rAdded new favorite class: " + type.getTag()));
												main.getDataManager().saveData(data);
											} else {
												player.sendMessage(
														main.color("&c&l(!) &r" + type.getTag() + " &ris already one of your favorites!"));
											}
										}
									} else if (e.isLeftClick()) {
										main.getGameManager().playerSelectClass(player, type);
										player.sendMessage(main.color("&2&l============================================="));
										player.sendMessage(main.color("&2&l|| "));
										player.sendMessage(main.color("&2&l|| "));
										player.sendMessage(main.color("&2&l|| " + "&e&lSelected Class: " + type.getTag()));
										player.sendMessage(main.color("&2&l|| " + "&e&lClass Desc: &e" + type.getClassDesc()));
										player.sendMessage(main.color("&2&l|| "));
										player.sendMessage(main.color("&2&l|| "));
										player.sendMessage(main.color("&2&l============================================="));
										inv.close(player);
									} else if (e.isRightClick()) {
										new ClassMasteryGUI(main, type, inv).inv.open(player);
									}
								} else {
									new PurchaseClassInventory(main, type, player);
								}
							}));
			b++;
			if (b > 8) {
				b = 0;
				a++;
			}
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
	
	public List<String> getCostDescription(Player player, ClassType type) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		List<String> desc = type.buildDescription();
		desc.add("");
		if (data != null) {
			if (data.isPurchased(type)) {
				desc.add(main.color("&e&lPurchased"));
				desc.add(main.color("&e" + type.getTokenCost() + " Tokens"));
			} else {
				desc.add(main.tokenCostString(player, type.getTokenCost()));
			}
		}
		return desc;
	}
}
