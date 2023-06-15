package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class ChallengesGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public ChallengesGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Challenges").build();
		this.main = main;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		if (data != null) {
			Material mat = null;
			if (data.challenge1 == 0)
				mat = Material.NETHER_STAR;
			else if (data.challenge1 == 1)
				mat = Material.BARRIER;
			contents.set(0, 0,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(mat),
							main.color("&eNotch Class"), "", main.color("&rGet a win with Pig class")),
							e -> {
							}));
			if (data.challenge2 == 0)
				mat = Material.NETHER_STAR;
			else if (data.challenge2 == 1)
				mat = Material.BARRIER;
			contents.set(0, 1,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(mat),
							main.color("&e50 Bonus Tokens"), "", main.color("&rGet a win")),
							e -> {
							}));
			if (data.challenge3 == 0)
				mat = Material.NETHER_STAR;
			else if (data.challenge3 == 1)
				mat = Material.BARRIER;
			contents.set(0, 2,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(mat),
							main.color("&e1 Bonus Level"), "", main.color("&rFind & use an extra life")),
							e -> {
							}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
