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

import java.util.ArrayList;
import java.util.List;

public class FreeClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public FreeClassesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Free Classes").build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int a = 0;
		int b = 0;
		
		PlayerData data = main.getDataManager().getPlayerData(player);

		for (ClassType type : ClassType.sortAlphabetically(ClassType.getFreeClasses(false))) {
			ItemStack item = type.getItem();
			
			if (type == ClassType.Fisherman &&  !main.fishing.hasUnlockedFisherman(player)) {
				continue;
			}
			if (type == ClassType.Freddy &&  !main.getHalloweenManager().hasUnlockedFreddy(player)) {
				continue;
			}

			// Lore
			List<String> lore = type.buildDescription();
			lore.add("");

				// Adds Unlocked lore text to Free but not regularly unlocked classes
			if (type == ClassType.Fisherman || type == ClassType.Freddy) {
				lore.addAll(getUnlockedDescriptionText(player, type));
			}

				// Click Messages
			String chooseClassString = "&e&nLeft Click&r &eto choose a class";
			String viewMasteryString = "&e&nRight Click&r &eto view mastery";
			String favoriteClassString = "&e&nShift Click&r &eto add a favorite class";
			lore.add(main.color(chooseClassString));
			lore.add(main.color(viewMasteryString));
			lore.add(main.color(favoriteClassString));
			lore.add("");

				// Class Mastery
			ClassDetails details = data.playerClasses.get(type.getID());
			lore.addAll(getNextRewardAndProgressBar(details));

			if (item == null) item = new ItemStack(Material.WOOD);
			contents.set(a, b,
					ClickableItem.of(
							ItemHelper.setDetails(
									ItemHelper.setHideFlags(item, true),
									type.getTag(),
									lore
							),
							e -> {
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
		
		contents.set(2, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.ARROW), String.valueOf(ChatColor.GRAY) + "Go Back"), e -> {
					new ClassesGUI(main).inv.open(player);
				}));
	}

	private List<String> getUnlockedDescriptionText(Player player, ClassType type) {
		List<String> stringList = new ArrayList<>();
		String unlocked = "&lUnlocked";
		String source = null;

		boolean isUnlocked = false;

		if (type == ClassType.Fisherman) {
			isUnlocked = main.getFishing().hasUnlockedFisherman(player);
			source = "Fishing";
		} else if (type == ClassType.Freddy) {
			isUnlocked = main.getHalloweenManager().hasUnlockedFreddy(player);
			source = "Halloween Hunt";
		}

		String color = isUnlocked ? "&e" : "&c";
		String through = isUnlocked ? "Through " : " through ";

		String firstLine = color + unlocked;
		String mean = through + source;
		firstLine += isUnlocked ? "" : mean;

		stringList.add(main.color(firstLine));
		if (isUnlocked) stringList.add(main.color(color	+ mean));
		stringList.add("");

		return stringList;
	}

	private List<String> getNextRewardAndProgressBar(ClassDetails details) {
		List<String> rewardLines = new ArrayList<>();

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

		rewardLines.add(main.color("&aNext reward:"));
		rewardLines.add(main.progressBar(played, nextLevel, 25));

		return rewardLines;
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

}
