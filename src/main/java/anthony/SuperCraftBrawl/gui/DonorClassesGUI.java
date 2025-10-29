package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DonorClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public DonorClassesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Donor Classes").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int a = 0;
		int b = 0;

		PlayerData data = main.getDataManager().getPlayerData(player);

		contents.set(2, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.ARROW), String.valueOf(ChatColor.GRAY) + "Go Back"), e -> {
					new ClassesGUI(main).inv.open(player);
				}));

		for (ClassType type : ClassType.sortAlphabetically(ClassType.getDonorClasses(false))) {
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
							type.getTag(), type.buildDescription(), "",
							"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET
									+ ChatColor.YELLOW + " to choose a class",
							"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET
									+ ChatColor.YELLOW + " to view mastery",
							"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Shift Click" + ChatColor.RESET
									+ ChatColor.YELLOW + " to add a favorite class",
							"", main.color("&aNext reward:"), main.progressBar(played, nextLevel, 25)), e -> {
						Rank donor = type.getMinRank();

						if (donor == null
								|| player.hasPermission("scb." + donor.toString().toLowerCase())) {
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
                                player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
								inv.close(player);
							} else if (e.isRightClick()) {
								new ClassMasteryGUI(main, type, inv).inv.open(player);
							}
						} else {
                            player.sendMessage(main.color("&c&l(!) &rYou need a rank to use this class!"));
                            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 0f);
                            inv.close(player);
						}
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

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
