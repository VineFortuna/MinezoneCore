package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClassesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ClassesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Class Selector").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		boolean updated = false;
		PlayerData data = main.getDataManager().getPlayerData(player);
		for (ClassType type : ClassType.getAvailableClasses()) {
			ClassDetails details = data.playerClasses.get(type.getID());
			if (details == null) {
				updated = true;
				details = new ClassDetails();
				data.playerClasses.put(type.getID(), details);
			}
		}
		if (updated)
			main.getDataManager().saveData(data);

		contents.set(1, 2,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.DIAMOND),
						String.valueOf(ChatColor.RED) + ChatColor.BOLD + "DONOR CLASSES",
						ChatColor.GRAY + "Purchase a rank to access Donor Classes"), e -> {
							new DonorClassesGUI(main).inv.open(player);
						}));

		contents.set(1, 4,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.ENCHANTED_BOOK),
						String.valueOf(ChatColor.YELLOW) + ChatColor.BOLD + "FREE CLASSES",
						ChatColor.GRAY + "All the free classes!"), e -> {
							new FreeClassesGUI(main).inv.open(player);
						}));

		contents.set(1, 6,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EMERALD),
						String.valueOf(ChatColor.GREEN) + ChatColor.BOLD + "TOKEN CLASSES",
						ChatColor.GRAY + "You can buy these classes with coins!"), e -> {
							new TokenClassesGUI(main).inv.open(player);
						}));
		contents.set(2, 4,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
						String.valueOf(ChatColor.LIGHT_PURPLE) + ChatColor.BOLD + "LEVEL CLASSES",
						ChatColor.GRAY + "Classes that can be unlocked with Levels"), e -> {
							new LevelClassesGUI(main).inv.open(player);
						}));
		contents.set(0, 4,
				ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.GOLD_BLOCK),
						String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + "FAVORITE CLASSES",
						ChatColor.GRAY + "Your favorite classes here!", "",
						"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Left Click" + ChatColor.RESET + ChatColor.YELLOW
								+ " to choose a class",
						"" + ChatColor.YELLOW + ChatColor.UNDERLINE + "Right Click" + ChatColor.RESET + ChatColor.YELLOW
								+ " to choose random favorite class"),
						e -> {

							if (e.isLeftClick())
								new FavoriteClassesGUI(main).inv.open(player);
							else if (e.isRightClick()) {
								GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);

								if (instance != null && instance.state == GameState.WAITING) {
									if (!data.customIntegers.isEmpty()) {
										instance.boards.get(player).updateLine(5, " " + ChatColor.GRAY + "Random Fav");
										instance.favClassSelection.add(player);
										if (instance.classSelection.containsKey(player)) {
											instance.classSelection.remove(player);
										}
										player.sendMessage(
												main.color("&2&l(!) &rYou selected to go a random favorite class!"));
									} else {
										player.sendMessage(
												main.color("&c&l(!) &rYou don't have any favorite classes!"));
									}
								} else {
									new FavoriteClassesGUI(main).inv.open(player);
								}
							}
						}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
