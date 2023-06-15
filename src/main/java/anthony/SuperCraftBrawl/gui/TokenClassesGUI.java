package anthony.SuperCraftBrawl.gui;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Main;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class TokenClassesGUI implements InventoryProvider {

	public Main main;
	public SmartInventory inv;

	public TokenClassesGUI(Main main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Token Classes").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData playerData = main.getDataManager().getPlayerData(player);
		int a = 0;
		int b = 0;
		
		for (ClassType type : ClassType.values()) {
			if (type.getTokenCost() > 0) {
				contents.set(a, b, ClickableItem.of(
						ItemHelper.setDetails(type.getItem(), type.getTag(),
								"" + ChatColor.GRAY + type.getClassDesc(), "",
								playerData.isPurchased(type)
										? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
										: "" + ChatColor.RESET + type.getTokenCost() + ChatColor.YELLOW + " tokens"),
						e -> {
							if (playerData.playerClasses.get(type.getID()) != null
									&& playerData.playerClasses.get(type.getID()).purchased) {
								main.getGameManager().playerSelectClass(player, type);
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
										+ "==============================================");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
										+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
										+ type.getTag());
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
										+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
										+ ChatColor.YELLOW + type.getClassDesc());
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
								player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
										+ "==============================================");
								inv.close(player);
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
		
		/*contents.set(0, 0, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STICK), "" + ChatColor.ITALIC + "Ninja",
						"" + ChatColor.GRAY + "Ninja 2.0 (idk xD)", "",
						playerData.isPurchased(ClassType.Ninja)
								? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Ninja.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Ninja.getID()) != null
							&& playerData.playerClasses.get(ClassType.Ninja.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Ninja);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Ninja.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Ninja.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Ninja, player);
					}
				}));
		contents.set(0, 1, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.TNT), "" + ChatColor.ITALIC + "TNT",
						"" + ChatColor.GRAY + "Blow up your enemies with TNT!", "",
						playerData.isPurchased(ClassType.TNT) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.TNT.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.TNT.getID()) != null
							&& playerData.playerClasses.get(ClassType.TNT.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.TNT);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.TNT.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.TNT.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.TNT, player);
					}
				}));
		contents.set(0, 2, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EGG),
				"" + ChatColor.ITALIC + "Chicken",
				"" + ChatColor.GRAY + "Bock bock backaaack! One of the best classes hehe tip", "",
				playerData.isPurchased(ClassType.Chicken) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
						: "" + ChatColor.RESET + ClassType.Chicken.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Chicken.getID()) != null
							&& playerData.playerClasses.get(ClassType.Chicken.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Chicken);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Chicken.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Chicken.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Chicken, player);
					}
				}));
		contents.set(0, 3, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.WHEAT), "" + ChatColor.ITALIC + "Witch",
						"" + ChatColor.GRAY + "She lives in daydreams with me! (She)", "",
						playerData.isPurchased(ClassType.Witch) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Witch.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Witch.getID()) != null
							&& playerData.playerClasses.get(ClassType.Witch.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Witch);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Witch.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Witch.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Witch, player);
					}
				}));
		contents.set(0, 4, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.WOOL), "" + ChatColor.ITALIC + "Sheep",
						"" + ChatColor.GRAY + "Different colors of wool gives",
						"" + ChatColor.GRAY + "you different powers!", "",
						playerData.isPurchased(ClassType.Sheep) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Sheep.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Sheep.getID()) != null
							&& playerData.playerClasses.get(ClassType.Sheep.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Sheep);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Sheep.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Sheep.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Sheep, player);
					}
				}));
		contents.set(0, 5, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL),
				"" + ChatColor.ITALIC + "SnowGolem", "" + ChatColor.GRAY + "This is a SnowGolem, not a Snowman",
				"" + ChatColor.GRAY + "Get it right pleb!", "",
				playerData.isPurchased(ClassType.SnowGolem) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
						: "" + ChatColor.RESET + ClassType.SnowGolem.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.SnowGolem.getID()) != null
							&& playerData.playerClasses.get(ClassType.SnowGolem.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.SnowGolem);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.SnowGolem.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.SnowGolem.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.SnowGolem, player);
					}
				}));
		contents.set(0, 6, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.GOLDEN_CARROT), "" + ChatColor.ITALIC + "Bunny",
						"" + ChatColor.GRAY + "Easter Bunny is coming to town!", "",
						playerData.isPurchased(ClassType.Bunny) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Bunny.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Bunny.getID()) != null
							&& playerData.playerClasses.get(ClassType.Bunny.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Bunny);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Bunny.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Bunny.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Bunny, player);
					}
				}));
		contents.set(0, 7, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.GOLD_INGOT), "" + ChatColor.ITALIC + "ButterBro",
						"" + ChatColor.GRAY + "Yo, you there Sky??", "",
						playerData.isPurchased(ClassType.ButterBro) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.ButterBro.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.ButterBro.getID()) != null
							&& playerData.playerClasses.get(ClassType.ButterBro.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.ButterBro);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.ButterBro.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.ButterBro.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.ButterBro, player);
					}
				}));
		contents.set(0, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.WOOD_PICKAXE), "" + ChatColor.ITALIC + "Steve",
						"" + ChatColor.GRAY + "OMG OMG GET HYPED!!!!", "",
						playerData.isPurchased(ClassType.Steve) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Steve.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Steve.getID()) != null
							&& playerData.playerClasses.get(ClassType.Steve.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Steve);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Steve.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Steve.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Steve, player);
					}
				}));
		contents.set(1, 0, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.GRASS), "" + ChatColor.ITALIC + "Notch",
						"" + ChatColor.GRAY + "The owner of Minecraft..", "",
						playerData.isPurchased(ClassType.Notch) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Notch.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Notch.getID()) != null
							&& playerData.playerClasses.get(ClassType.Notch.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Notch);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Notch.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Notch.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Notch, player);
					}
				}));
		contents.set(1, 1, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.POTATO_ITEM), "" + ChatColor.ITALIC + "Potato",
						"" + ChatColor.GRAY + "Who doesn't like potatoes?!", "",
						playerData.isPurchased(ClassType.Potato) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Potato.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Potato.getID()) != null
							&& playerData.playerClasses.get(ClassType.Potato.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Potato);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Potato.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Potato.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Potato, player);
					}
				}));
		contents.set(1, 2, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.RAW_FISH), "" + ChatColor.ITALIC + "Ocelot",
						"" + ChatColor.GRAY + "Chase down your opponents with your high speed and Purr Attack", "",
						playerData.isPurchased(ClassType.Ocelot) ? "" + ChatColor.YELLOW + ChatColor.BOLD + "Purchased"
								: "" + ChatColor.RESET + ClassType.Ocelot.getTokenCost() + ChatColor.YELLOW + " tokens"),
				e -> {
					if (playerData.playerClasses.get(ClassType.Ocelot.getID()) != null
							&& playerData.playerClasses.get(ClassType.Ocelot.getID()).purchased) {
						main.getGameManager().playerSelectClass(player, ClassType.Ocelot);
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Selected Class: "
								+ ClassType.Ocelot.getTag());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| " + ChatColor.RESET
								+ ChatColor.YELLOW + ChatColor.BOLD + "Class Desc: " + ChatColor.RESET
								+ ChatColor.YELLOW + ClassType.Ocelot.getClassDesc());
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "|| ");
						player.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD
								+ "==============================================");
						inv.close(player);
					} else {
						new PurchaseClassInventory(main, ClassType.Ocelot, player);
					}
				}));*/
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
