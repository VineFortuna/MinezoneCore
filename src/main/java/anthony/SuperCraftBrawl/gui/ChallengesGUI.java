package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ChallengesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ChallengesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Challenges").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		contents.set(0, 0, ClickableItem.of(
				ItemHelper.setDetails(new ItemStack(Material.BEDROCK), main.color("&cNo new challenges yet")), e -> {
				}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
