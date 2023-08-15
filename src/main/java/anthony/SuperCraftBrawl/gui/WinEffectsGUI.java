package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;

public class WinEffectsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public WinEffectsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Win Effects").build();
		this.main = main;

	}
	
	//When a player has other effects enabled, disable them then enable the new one selected
	private void resetWinEffects(PlayerData data) {
		data.broomWinEffect = 0;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		if (data != null) {
			contents.set(0, 0,
					ClickableItem.of(
							ItemHelper.setDetails(new ItemStack(Material.WHEAT), main.color("&2&lMagic Broom"), "",
									main.color("&rFly around the map with this"), main.color("&rwhen you win a game")),
							e -> {
								resetWinEffects(data);
								data.broomWinEffect = 1;
								inv.close(player);
							}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
