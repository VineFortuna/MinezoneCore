package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
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

public class CraftableItemsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public CraftableItemsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
				.title("" + ChatColor.RESET + ChatColor.BOLD + "Craftable Items").build();
		this.main = main;
		
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
		BaseClass bc = instance.classes.get(player);
		ItemStack coal = new ItemStack(Material.COAL, 4);
		ItemStack ironIngot = new ItemStack(Material.IRON_INGOT, 4);
		ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT, 4);
		ItemStack diamond = new ItemStack(Material.DIAMOND, 4);
		
		ItemStack coalBlock = new ItemStack(Material.COAL_BLOCK);
		ItemStack ironBlock = new ItemStack(Material.IRON_BLOCK);
		ItemStack goldBlock = new ItemStack(Material.GOLD_BLOCK);
		ItemStack diamondBlock = new ItemStack(Material.DIAMOND_BLOCK);
		
		if (player.getInventory().contains(coal)) {
			contents.set(1, 1,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.COAL_BLOCK), "",
							"" + ChatColor.YELLOW + ChatColor.BOLD + "Click here to craft " + ChatColor.RESET
									+ ChatColor.BLACK + ChatColor.BOLD + "Coal Block"),
							e -> {
								if (!(player.getInventory().contains(coalBlock))) {
									inv.close(player);
									player.getInventory().addItem(coalBlock);
									player.sendMessage("Successfully crafted " + ChatColor.RESET + ChatColor.BLACK
											+ ChatColor.BOLD + "Coal Block");
									player.getInventory().remove(coal);
									player.playSound(player.getLocation(), Sound.ANVIL_USE, 0.8f, 0);
									bc.coalAmt = 0;
								} else {
									player.sendMessage("Use your other coal first before crafting a new one!");
								}
							}));
		}
		if (player.getInventory().contains(ironIngot)) {
			contents.set(1, 3,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.IRON_BLOCK), "",
							"" + ChatColor.YELLOW + ChatColor.BOLD + "Click here to craft " + ChatColor.RESET
									+ ChatColor.BOLD + "Iron Block"),
							e -> {
								if (!(player.getInventory().contains(ironBlock))) {
									inv.close(player);
									player.getInventory().addItem(ironBlock);
									player.sendMessage(
											"Successfully crafted " + ChatColor.RESET + ChatColor.BOLD + "Iron Block");
									player.getInventory().remove(ironIngot);
								} else {
									player.sendMessage("Use your other iron first before crafting a new one!");
								}
							}));
		}
		if (player.getInventory().contains(goldIngot)) {
			contents.set(1, 5,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.GOLD_BLOCK), "",
											"" + ChatColor.YELLOW + ChatColor.BOLD + "Click here to craft "
													+ ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD + "Gold Block"),
							e -> {
								if (!(player.getInventory().contains(goldBlock))) {
									inv.close(player);
									player.getInventory().addItem(goldBlock);
									player.sendMessage("Successfully crafted " + ChatColor.RESET + ChatColor.GOLD
											+ ChatColor.BOLD + "Gold Block");
									player.getInventory().remove(goldIngot);
									player.playSound(player.getLocation(), Sound.ANVIL_USE, 0.8f, 0);
									bc.goldAmt = 0;
								} else {
									player.sendMessage("Use your other gold first before crafting a new one!");
								}
							}));
		}
		
		if (player.getInventory().contains(diamond)) {
			contents.set(1, 7,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.DIAMOND_BLOCK), "",
							"" + ChatColor.YELLOW + ChatColor.BOLD + "Click here to craft " + ChatColor.RESET
									+ ChatColor.AQUA + ChatColor.BOLD + "Diamond Block"),
							e -> {
								if (!(player.getInventory().contains(diamondBlock))) {
									inv.close(player);
									player.getInventory().addItem(diamondBlock);
									player.sendMessage("Successfully crafted " + ChatColor.RESET + ChatColor.AQUA
											+ ChatColor.BOLD + "Diamond Block");
									player.getInventory().remove(diamond);
									player.playSound(player.getLocation(), Sound.ANVIL_USE, 0.8f, 0);
									bc.diaAmt = 0;
								} else {
									player.sendMessage("Use your other diamond first before crafting a new one!");
								}
							}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
