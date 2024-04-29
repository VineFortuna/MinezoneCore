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
		PlayerData data = main.getDataManager().getPlayerData(player);
//		contents.set(0, 0, ClickableItem.of(
//				ItemHelper.setDetails(new ItemStack(Material.BEDROCK), main.color("&cNo new challenges yet")), e -> {
//				}));
		if (data != null) {
			Material mat = null;
			if (data.challenge3 == 0)
				mat = Material.NETHER_STAR;
			else if (data.challenge3 == 1)
				mat = Material.BARRIER;
			contents.set(0, 0,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(mat),
									main.color("&e1 Bonus Level"), data.challenge3 == 1?main.color("&aCOMPLETED"):"",
									main.color("&rFind & use an extra life")),
							e -> {
							}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
