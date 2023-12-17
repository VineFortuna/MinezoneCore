package anthony.SuperCraftBrawl.gui;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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

	// When a player has other effects enabled, disable them then enable the new one
	// selected
	private void resetWinEffects(PlayerData data) {
		data.broomWinEffect = 0;
		data.santaEffect = 0;
		data.enderDragonEffect = 0;
		data.fireParticlesEffect = 0;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);

		if (data != null) {
			contents.set(0, 0,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.WHEAT), main.color("&2&lMagic Broom"), "",
											main.color("&rFly around the map with this"),
											main.color("&rwhen you win a game"), "", "" + ChatColor.BLUE
													+ ChatColor.BOLD + "CAPTAIN" + ChatColor.RESET + "+ exclusive!"),
							e -> {
								resetWinEffects(data);
								data.broomWinEffect = 1;
								inv.close(player);
							}));

			contents.set(0, 0,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.DRAGON_EGG), main.color("&cEnderDragon Effect"),
											"", main.color("&rFly around the map with an"),
											main.color("&rEnderDragon when match is over!"), "", "" + ChatColor.BLUE
													+ ChatColor.BOLD + "CAPTAIN" + ChatColor.RESET + "+ exclusive!"),
							e -> {
								resetWinEffects(data);
								data.enderDragonEffect = 1;
								inv.close(player);
							}));
			ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
			SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
			meta.setOwner("SethBling");
			meta.setDisplayName("");
			playerskull.setItemMeta(meta);

			contents.set(0, 1, ClickableItem.of(ItemHelper.setDetails(playerskull, main.color("&cDefault Effect"), "",
					main.color("&rFireworks shoot up when winning"), main.color("&ra game!")), e -> {
						resetWinEffects(data);
						inv.close(player);
					}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
