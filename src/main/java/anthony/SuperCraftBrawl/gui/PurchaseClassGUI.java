package anthony.SuperCraftBrawl.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class PurchaseClassGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;
	private final ClassType classType;
	private final Player player;
	private final PlayerData playerData;

	public PurchaseClassGUI(Core main, ClassType classType, Player player) {
		this.main = main;
		this.classType = classType;
		this.player = player;
		this.playerData = main.getPlayerDataManager().getPlayerData(player);

		inv = SmartInventory.builder()
				.id("myInventory").provider(this)
				.size(3, 9)
				.title(String.valueOf(ChatColor.YELLOW)
				+ ChatColor.BOLD + "Purchase " + classType + "?")
				.build();

		ClassDetails details = playerData.playerClasses.get(playerData);

		if (details != null && details.purchased) {
			player.sendMessage(String.valueOf(ChatColor.RESET) + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have already purchased this class");
		} else {
			inv.open(player);
		}
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(classType.getItem(),
				ChatColor.RESET + "Are you sure you want to purchase?", "",
				String.valueOf(ChatColor.RESET) + ChatColor.BOLD + "Class: " + classType.getTag(), String.valueOf(ChatColor.RESET)
						+ ChatColor.YELLOW + ChatColor.BOLD + "Tokens: " + ChatColor.RESET + classType.getTokenCost()),
				e -> {
					PlayerData data = main.getPlayerDataManager().getPlayerData(player);

					if (data.tokens >= classType.getTokenCost()) {
						player.sendMessage(String.valueOf(ChatColor.RESET) + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
								+ ChatColor.RESET + "You have purchased " + ChatColor.RESET + classType.getTag());

						int classID = classType.getID();
						PlayerData playerData = main.getPlayerDataManager().getPlayerData(player);
						ClassDetails details = playerData.playerClasses.get(classID);

						if (details == null) {
							details = new ClassDetails();
							playerData.playerClasses.put(classID, details);
						}
						details.setPurchased();
						data.tokens -= classType.getTokenCost();
						inv.close(player);
						main.getPlayerDataManager().saveData(data);
					} else {
						player.sendMessage(String.valueOf(ChatColor.RESET) + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) "
								+ ChatColor.RESET + "You don't have enough tokens to purchase " + classType.getTag());
						inv.close(player);
					}
				}));
		contents.set(0, 0, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 1, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 2, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 3, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 4, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 5, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 6, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 7, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(0, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 0, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 1, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 2, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 3, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 5, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 6, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 7, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(1, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 0, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 1, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 2, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 3, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 4, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 5, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 6, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 7, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
		contents.set(2, 8, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.RED.getData()), ""), e -> {
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
