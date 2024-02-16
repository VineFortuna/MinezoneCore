package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class WinEffectsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public WinEffectsGUI(Core main) {
		inv = SmartInventory.builder().id("myInventory").provider(this).size(6, 9)
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
		
		// Icons Items
		ItemStack lockedCosmetic = ItemHelper.createDye(DyeColor.GRAY, 1, ChatColor.GRAY + "&&&&&&&");
		
		
		// Setting Items
		contents.fillRect(1,1, 7,7, ClickableItem.of(
				lockedCosmetic,
				e -> {
				
				}));
		
		if (data != null) {
			contents.set(1, 2,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WHEAT), main.color("&cMagic Broom"),
							"", main.color("&rFly around the map with this"), main.color("&rwhen you win!"), "",
							"" + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN" + ChatColor.RESET + "+ exclusive!"), e -> {
								resetWinEffects(data);
								data.broomWinEffect = 1;
								inv.close(player);
								player.sendMessage(main.color("&e&l(!) &rYou have enabled &eMagic Broom &rwin effect"));
							}));

			contents.set(1, 4,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.DRAGON_EGG), main.color("&cEnderDragon"), "",
											main.color("&rFly around the map with an"),
											main.color("&rEnderDragon when you win!"), "", "" + ChatColor.BLUE
													+ ChatColor.BOLD + "CAPTAIN" + ChatColor.RESET + "+ exclusive!"),
							e -> {
								resetWinEffects(data);
								data.enderDragonEffect = 1;
								inv.close(player);
								player.sendMessage(main.color("&e&l(!) &rYou have enabled &eEnderDragon &rwin effect"));
							}));
			String sethblingTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==";
			ItemStack playerskull = ItemHelper.createSkullTexture(sethblingTexture);
			
			String santaTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";
			ItemStack santa = ItemHelper.createSkullTexture(santaTexture);

			contents.set(1, 3,
					ClickableItem.of(ItemHelper.setDetails(santa, main.color("&cSanta Claus Effect"), "",
							main.color("&rBecome old Saint Nick himself"), main.color("&rand ride along!"), "",
							"" + ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN" + ChatColor.RESET + "+ exclusive!"), e -> {
								resetWinEffects(data);
								data.santaEffect = 1;
								inv.close(player);
								player.sendMessage(main.color("&e&l(!) &rYou have enabled &eSanta Claus &rwin effect"));
							}));

			contents.set(1, 1, ClickableItem.of(ItemHelper.setDetails(playerskull, main.color("&cDefault Effect"), "",
					main.color("&rFireworks shoot up when winning"), main.color("&ra game!")), e -> {
						resetWinEffects(data);
						inv.close(player);
						player.sendMessage(main.color("&e&l(!) &rYou have enabled &eDefault &rwin effect"));
					}));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
