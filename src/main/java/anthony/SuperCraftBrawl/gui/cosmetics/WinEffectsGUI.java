package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WinEffectsGUI implements InventoryProvider {

	public Core main;
	public SmartInventory inv;

	public WinEffectsGUI(Core main, SmartInventory parent) {
		inv = SmartInventory.builder()
				.id("myInventory")
				.provider(this)
				.size(3, 9)
				.title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Win Effects")
				.parent(parent)
				.build();
		this.main = main;
	}

	// When a player has other effects enabled, disable them then enable the new one
	// selected
	private void resetWinEffects(PlayerData data) {
		data.broomWinEffect = 0;
		data.santaEffect = 0;
		data.enderDragonEffect = 0;
		data.fireParticlesEffect = 0;
		data.fishRainEffect = 0;
		data.floodEffect = 0;
		data.treasureEffect = 0;
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		
		// Icons Items
		String sethblingTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I4NmI4MjE1YjM2MTBlYWE2NDhjMjNjNGEyMGFkNjc1OWYyNTFlZjg1NDc2ODI5ZGQ2ZDE4NDI4MjNiMTEzIn19fQ==";
		ItemStack playerskull = ItemHelper.createSkullTexture(sethblingTexture);

		String santaTexture = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTExYjFiM2U3NzI4ZWQzZTI2NzMzZGZhYjljNTBhNmM3YzY4OTEzODk3MTU3ZDY4MmY4Njg3NTZkYzY2YWUifX19";
		ItemStack santa = ItemHelper.createSkullTexture(santaTexture);

		// Setting Items
		contents.fillBorders(ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
		
		if (data != null) {
			contents.set(1, 2,
					ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.WHEAT), main.color("&cMagic Broom"),
							"", main.color("&rFly around the map with this"), main.color("&rwhen you win!"), "",
							Rank.CAPTAIN.getTag() + ChatColor.RESET + "+ exclusive!"),e -> {
						if (player.hasPermission("scb.winEffects")) {
							resetWinEffects(data);
							data.broomWinEffect = 1;
							inv.close(player);
							player.sendMessage(main.color("&e&l(!) &rYou have enabled &eMagic Broom &rwin effect"));
						} else {
							player.sendMessage(main.color(
									"&c&l(!) &rYou need the rank " + Rank.CAPTAIN.getTag() + "&r to use this!"));
						}
					}));
			
			contents.set(1, 4,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.DRAGON_EGG), main.color("&cEnderDragon"), "",
											main.color("&rFly around the map with an"),
											main.color("&rEnderDragon when you win!"), "",
											Rank.CAPTAIN.getTag() + ChatColor.RESET + "+ exclusive!"),
							e -> {
								if (player.hasPermission("scb.winEffects")) {
									resetWinEffects(data);
									data.enderDragonEffect = 1;
									inv.close(player);
									player.sendMessage(main.color("&e&l(!) &rYou have enabled &eEnderDragon &rwin effect"));
								} else {
									player.sendMessage(main.color(
											"&c&l(!) &rYou need the rank " + Rank.CAPTAIN.getTag() + "&r to use this!"));
								}
							}));
			
			contents.set(1, 3,
					ClickableItem.of(ItemHelper.setDetails(santa, main.color("&cSanta Claus"), "",
							main.color("&rBecome old Saint Nick himself"), main.color("&rand ride along!"), "",
							Rank.CAPTAIN.getTag() + ChatColor.RESET + "+ exclusive!"), e -> {
						if (player.hasPermission("scb.winEffects")) {
							resetWinEffects(data);
							data.santaEffect = 1;
							inv.close(player);
							player.sendMessage(main.color("&e&l(!) &rYou have enabled &eSanta Claus &rwin effect"));
						} else {
							player.sendMessage(main.color(
									"&c&l(!) &rYou need the rank " + Rank.CAPTAIN.getTag() + "&r to use this!"));
						}
					}));
			contents.set(1, 5,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.RAW_FISH), main.color("&cFish Rain"), "",
											main.color("&rCover the map with fish"), main.color("&rfalling from the sky"),
											"", main.color("&aFishing reward!")),
							e -> {
								if (data.rewardLevel >= 4) {
									resetWinEffects(data);
									data.fishRainEffect = 1;
									inv.close(player);
									player.sendMessage(main.color("&e&l(!) &rYou have enabled &eFish Rain &rwin effect"));
								} else {
									player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "You have not unlocked this cosmetic yet!");
								}
							}));
			contents.set(1, 6,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.BOAT), main.color("&cFlood"), "",
											main.color("&rFlood the map and"), main.color("&rride a boat to safety"),
											"", main.color("&aFishing reward!")),
							e -> {
								if (main.getFishing().hasAllFish(player)) {
									resetWinEffects(data);
									data.floodEffect = 1;
									inv.close(player);
									player.sendMessage(main.color("&e&l(!) &rYou have enabled &eFlood &rwin effect"));
								} else {
									player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "You have not unlocked this cosmetic yet!");
								}
							}));
			contents.set(1, 6,
					ClickableItem.of(
							ItemHelper
									.setDetails(new ItemStack(Material.GOLD_BLOCK), main.color("&cTreasure Hoard"), "",
											main.color("&rPlunder shiny riches"), main.color("&rlike a true pirate"),
											"", main.color("&aFishing reward!")),
							e -> {
								if (data.treasureOpened == 1) {
									resetWinEffects(data);
									data.treasureEffect = 1;
									inv.close(player);
									player.sendMessage(main.color("&e&l(!) &rYou have enabled &eTreasure Hoard &rwin effect"));
								} else {
									player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "(!) " + ChatColor.RESET
											+ "You have not unlocked this cosmetic yet!");
								}
							}));


			contents.set(1, 1, ClickableItem.of(ItemHelper.setDetails(playerskull, main.color("&cDefault Effect"), "",
					main.color("&rFireworks shoot up when winning"), main.color("&ra game!")), e -> {
				resetWinEffects(data);
				inv.close(player);
				player.sendMessage(main.color("&e&l(!) &rYou have enabled &eDefault &rwin effect"));
			}));

			contents.set(2, 8, ClickableItem.of(
					ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
						inv.getParent().get().open(player);
					}
			));
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}
