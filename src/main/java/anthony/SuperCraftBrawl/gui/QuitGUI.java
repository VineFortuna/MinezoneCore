package anthony.SuperCraftBrawl.gui;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class QuitGUI implements InventoryProvider {

	public Core main;
	public SmartInventory invQuit;
	public GameInstance instance;

	public GameInstance getGameInstance() {
		return instance;

	}

	public QuitGUI(Core main) {
		invQuit = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Leave your game?").build();
		this.main = main;

	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(0, 2, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.EMERALD_BLOCK), "" + ChatColor.GREEN + "Confirm"), e -> {
					main.getCommands().leaveGame(player);
				}));

		contents.set(0, 6, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.REDSTONE_BLOCK), "" + ChatColor.RED + "Cancel"), e -> {
					invQuit.close(player);
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
